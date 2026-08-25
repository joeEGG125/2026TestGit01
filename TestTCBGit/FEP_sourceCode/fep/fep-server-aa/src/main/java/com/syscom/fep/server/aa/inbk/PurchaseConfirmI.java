package com.syscom.fep.server.aa.inbk;


import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Feptxntcb;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.IOReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * 處理財金發動的跨行繳稅確認電文
 *
 * @author Joseph
 */

public class PurchaseConfirmI extends INBKAABase {
    ///#Region "共用變數宣告"
    private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
    private boolean isExitProgram = false;  //for主流程第三點更新交易記錄(FEPTXN & VATXN)，儲存更新失敗的FEPReturnCode
    /**
     * AA的建構式,在這邊初始化及設定其他相關變數
     *
     * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
     * @throws Exception PurchaseConfirmIException
     */
    public PurchaseConfirmI(FISCData txnData) throws Exception {
        super(txnData);
    }


    /**
     * 程式進入點
     *
     * @return Response電文
     */
    @Override
    public String processRequestData() {
        try {
            //1.    拆解並檢核財金電文
            _rtnCode = getFiscBusiness().checkHeader(getFiscCon(), true);
            String sFiscRc = TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext());
            if("10".equals(sFiscRc.substring(0, 2))) {
                /* 程式結束 FISC RC:Garbled Message */
                getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), _rtnCode, getFiscCon());
                return StringUtils.EMPTY;
            }
            //取得OriginalFEPTxn
            getFiscBusiness().setFeptxn(getFiscBusiness().getOriginalFEPTxn());
            getTxData().setFeptxn(getFiscBusiness().getFeptxn());

            // 2.   商業邏輯檢核＆電文Body檢核
            if (_rtnCode == FEPReturnCode.Normal) {
                _rtnCode = checkBusinessRule();
                if(isExitProgram){
                    return StringUtils.EMPTY;
                }
            }

            //3.    UpdateTxData:更新交易記錄 (FEPTXN )
            if (_rtnCode == FEPReturnCode.Normal) {
                _rtnCode = updateTxData();
                if(_rtnCode != FEPReturnCode.Normal){
                    getLogContext().setMessage(_rtnCode.toString());
                    getLogContext().setRemark("更新交易記錄錯誤!!");
                    logMessage(Level.INFO, getLogContext());
                    return StringUtils.EMPTY;
                }
            }

            //4.    判斷是否沖轉跨行代收付ProcessAPTOT及主機帳務SendToCBS/ASC)
            if (_rtnCode == FEPReturnCode.Normal) {
                _rtnCode = processAPTOTSendToCBSASC();
            }

            //5. 	label_END_OF_FUNC:
            //6.    更新交易記錄(FEPTXN) (查不到原始資料則不更新FEPTXN)
            _rtnCode = this.updateFEPTXN();

            //7.    簡訊/EMAL/推播:
            if (getFiscBusiness().getFeptxn() != null
                    && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())
                    && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) {
                getFiscBusiness().sendToNotify();
            }

            //8.    判斷是否需傳送2160電文給財金
                /* 2025/7/1 修改 for 傳送 2160電文給財金 */
                /* “A” : 成功或失敗均需傳送      */
                /* “Y” : 僅成功交易傳送         */
            if("4001".equals(getFiscBusiness().getFeptxn().getFeptxnConRc())){
                if ("Y".equals(getFiscBusiness().getFeptxn().getFeptxnSend2160()) || "A".equals(getFiscBusiness().getFeptxn().getFeptxnSend2160())) {
                    /* 寫入2160發送資料檔 */
                    _rtnCode = insertINBK2160();
                }
            }else{
                if ( "A".equals(getFiscBusiness().getFeptxn().getFeptxnSend2160())) {
                    /* 寫入2160發送資料檔 */
                    _rtnCode = insertINBK2160();
                }
            }

//            //8.    FEP通知主機交易結束
//            _rtnCode = this.SendToCBS();
        } catch (Exception ex) {
            _rtnCode = FEPReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequestData");
            sendEMS(this.logContext);
        } finally {
            getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getLogContext().setMessage("FiscResponse:"+getFiscRes().getFISCMessage());
            getLogContext().setProgramName(this.aaName);
            getLogContext().setMessageFlowType(MessageFlow.Response);
            getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
            getLogContext().setProgramName(this.aaName);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
            logMessage(Level.DEBUG, getLogContext());
        }
        return StringUtils.EMPTY;
    }


    /**
     * 2. 	CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
     *
     * @return FEPReturnCode
     */
    private FEPReturnCode checkBusinessRule() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

        try {
            //(2.1) 	檢核 Mapping 欄位
            /* 9/9 修改, 改抓 FEPTXN_REQ_DATETIME欄位 */
            String dateTime =  (CalendarUtil.rocStringToADString(StringUtils.leftPad(this.getFiscCon().getTxnInitiateDateAndTime().substring(0,6), 7, "0"))) + this.getFiscCon().getTxnInitiateDateAndTime().substring(6,12);
            if (!getFiscBusiness().getFeptxn().getFeptxnReqDatetime().equals(dateTime) ||
                    (!getFiscBusiness().getFeptxn().getFeptxnDesBkno().equals(this.getFiscCon().getTxnDestinationInstituteId().substring(0, 3))) ||
                    (StringUtils.isNotBlank(this.getFiscCon().getATMNO()) && !getFiscBusiness().getFeptxn().getFeptxnAtmno().equals(this.getFiscCon().getATMNO()))
                    || (StringUtils.isNotBlank(this.getFiscCon().getTxAmt()) && !new DecimalFormat("0.00").format(getFiscBusiness().getFeptxn().getFeptxnTxAmt()).equals(new DecimalFormat("0.00").format(new BigDecimal(this.getFiscCon().getTxAmt()))))) {
                return FEPReturnCode.OriginalMessageDataError;
            }

            //(2.2) 檢核交易是否未完成
            if (!getFiscBusiness().getFeptxn().getFeptxnTxrust().equals("B")) {
                /* 10/20 修改, 財金錯誤代碼改為 ‘0101’ */
                isExitProgram = true;
                return FEPReturnCode.MessageFormatError; //11011  **相關欄位檢查錯誤
            }

            /* 9/22 修改 for CON 送2次 */
            if (!getFiscBusiness().getFeptxn().getFeptxnTraceEjfno().equals(0)) {
                this.getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
                isExitProgram = true;
                return FEPReturnCode.MessageFormatError; //11011  **相關欄位檢查錯誤
            }

            /* 2024/7/1 以FEPTXN的PK讀取FEPTXNTCB Data */
            Feptxntcb tempFeptxntcb = getFiscBusiness().checkFeptxntcbData(getFiscBusiness().getFeptxn());
            getFiscBusiness().setFeptxntcb(tempFeptxntcb);
            getTxData().setFeptxntcb(tempFeptxntcb);
            if(getFiscBusiness().getFeptxntcb() == null) {
                return FEPReturnCode.MessageFormatError;
            }

            //(2.3) 檢核 MAC
            getFiscBusiness().getFeptxn().setFeptxnConRc(this.getFiscCon().getResponseCode());
            /* 11/16 修改, 收到財金確認電文時間寫入FEPTXN */
            getFiscBusiness().getFeptxn().setFeptxnConTxTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            rtnCode = encHelper.checkFiscMac(this.getFiscCon().getMessageType(), getFiscCon().getMAC());
            this.logContext.setRemark("after checkFiscMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
            if (rtnCode != FEPReturnCode.Normal) {
                this.getFeptxn().setFeptxnConRc(null);
                return FEPReturnCode.ENCCheckMACError;//**訊息押碼錯誤
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 3. 	UpdateTxData:更新交易記錄 (FEPTXN )
     *
     * @return FEPReturnCode
     */
    private FEPReturnCode updateTxData() {
        try {
            getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(false));/*AA Initial*/
            if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) { /*+REP*/
                if (NormalRC.FISC_ATM_OK.equals(this.getFiscCon().getResponseCode())) { /*+CON*/
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); /*成功*/
                } else {
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("C"); /*Accept-Reverse*/
                }
                getFiscBusiness().getFeptxn().setFeptxnPending((short) 2); /*解除 PENDING */
            }
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm); // 'F3  FISC CONFIRM
            getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(getTxData().getEj());

            /* 2025/5/9 修改 for  CON電文送2次*/
//            getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscCon().getResponseCode());
            int i = this.feptxnDao.updateConfirmByPrimaryKey(getFiscBusiness().getFeptxn());
            if (i <= 0) {
                getLogContext().setProgramName(ProgramName + ".updateTxData");
                getLogContext().setRemark("收到CON電文無法更新FEPTXN，EJFNO:"+getFiscBusiness().getFeptxn().getFeptxnEjfno());
                sendEMS(getLogContext());
                return IOReturnCode.FEPTXNUpdateError;
            }

            return FEPReturnCode.Normal;
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
            sendEMS(this.logContext);
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 4.判斷是否沖轉跨行代收付ProcessAPTOT及主機帳務SendToCBS/ASC
     *
     * @return FEPReturnCode
     * @throws Exception sendToCBSException
     */
    private FEPReturnCode processAPTOTSendToCBSASC() throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) { /* +REP */
            String TxType = "";
            if (!NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) { /* -CON */
                /*沖轉跨行代收付*/
                rtnCode = getFiscBusiness().processAptot(true);
                this.logContext.setProgramName(ProgramName);
                /*沖轉主機帳務*/
                if ( StringUtils.equalsAny( getFiscBusiness().getFeptxn().getFeptxnPcode() ,"2541", "2542", "2525") ){
                    TxType = "2"; //沖正
                    this.getTxData().setFiscreq(this.getFiscCon());
                    String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
                    ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, this.getTxData());
                    getFiscCon().setTxnSourceInstituteId(this.getFiscCon().getTxnSourceInstituteId());
                    rtnCode = new CBS(hostAA, this.getTxData()).sendToCBS(TxType);
                }else{ //2025/8/24 修改
                    /* PCODE=2543, -CON 送交易結束通知(END)給主機 */
                    rtnCode = this.SendToCBSEnd();
                }
                //由GetMessageFromFEPReturnCode執行 SendEMS
                TxHelper.getMessageFromFEPReturnCode(getFiscBusiness().getFeptxn().getFeptxnConRc(), FEPChannel.FISC, getLogContext());
            }else{
                if ( StringUtils.equals( getFiscBusiness().getFeptxn().getFeptxnPcode() , "2543") ){
                    TxType = "1";  //入帳
                    this.getTxData().setFiscreq(this.getFiscCon());
                    String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
                    ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, this.getTxData());
                    getFiscCon().setTxnSourceInstituteId(this.getFiscCon().getTxnSourceInstituteId());
                    rtnCode = new CBS(hostAA, this.getTxData()).sendToCBS(TxType);
                }else{ //2025/8/24 修改
                    /* 2541/2542/2525 , +CON 送交易結束通知(END)給主機 */
                    rtnCode = this.SendToCBSEnd();
                }
            }
        }
        return rtnCode;
    }

    /**
     * 6. 	更新交易記錄(FEPTXN) : if need
     *
     * @return FEPReturnCode
     * @throws Exception updateFEPTXNException
     */
    private FEPReturnCode updateFEPTXN() throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        if (getFiscBusiness().getFeptxn().getFeptxnAaRc().equals(FEPReturnCode.Normal.getValue())) {
            if (this._rtnCode != FEPReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(this._rtnCode.getValue());
            }
        }
        getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/
        /* 2025/10/9 修改 for 原存CON交易主機處理異常 */
        if ( "PEND".equals(getFiscBusiness().getFeptxn().getFeptxnCbsRc()) ) {
            /* 2025/10/7 修改 for 合庫要求, FEP重發pending */
            getFiscBusiness().getFeptxn().setFeptxnChannelEjfno(String.valueOf(getFiscBusiness().getFeptxn().getFeptxnTraceEjfno()));
            getFiscBusiness().getFeptxn().setFeptxnPending( (short) 1 );
            getFiscBusiness().getFeptxn().setFeptxnMsgflow( FEPTxnMessageFlow.FISC_Response);
            getFiscBusiness().getFeptxn().setFeptxnTxrust("B");
            getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(0);
        }

        rtnCode = getFiscBusiness().updateTxData();
        return rtnCode;
    }

    /**
     * 7. 	判斷是否需傳送2160電文給財金
     *
     * @return FEPReturnCode
     */
    private FEPReturnCode insertINBK2160() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            //檢核Header
            rtnCode = getFiscBusiness().prepareInbk2160();
            if(rtnCode != FEPReturnCode.Normal){
                getLogContext().setMessage(rtnCode.toString());
                getLogContext().setProgramName(ProgramName + ".insertINBK2160");
                getLogContext().setRemark("寫入INBK2160發生錯誤!!");
                logMessage(getLogContext());
                return FEPReturnCode.INBK2160InsertError;
            }else{
                return FEPReturnCode.Normal;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".insertINBK2160");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 8.   FEP通知主機交易結束
     *
     * @return FEPReturnCode
     * @throws Exception sendToCBSException
     */
    private FEPReturnCode SendToCBSEnd() throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        /*沖轉主機帳務*/
        String AATxTYPE = "";
        String AATxRs = "N";
        try {
            String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid1();
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, this.getTxData());
            rtnCode = new CBS(hostAA, this.getTxData()).sendToCBS(AATxTYPE,AATxRs);
            if(rtnCode != FEPReturnCode.Normal){
                getLogContext().setMessage(rtnCode.toString());
                getLogContext().setRemark("通知主機交易結束時發生錯誤!!");
                sendEMS(getLogContext());
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".SendToCBSEnd");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }
}

