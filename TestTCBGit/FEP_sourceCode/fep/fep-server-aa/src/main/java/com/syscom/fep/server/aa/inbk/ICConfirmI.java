package com.syscom.fep.server.aa.inbk;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.IctltxnExtMapper;
import com.syscom.fep.mybatis.model.Feptxntcb;
import com.syscom.fep.mybatis.model.Ictltxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * 處理財金發動晶片金融卡跨國確認電文
 * @author User	-> Ben (SA=Sarah)
 *
 */
public class ICConfirmI extends INBKAABase{
    private Ictltxn defICTLTXN = new Ictltxn();

    private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
    private boolean isExitProgram = false;
    
    public ICConfirmI(FISCData txnData) throws Exception {
        super(txnData);
    }

    /**
     * 程式進入點
     */
    @Override
    public String processRequestData() {
        try {
        	//1.拆解並檢核財金電文(CheckHeader內含CheckBitMap)，若為Garble則組回覆訊息(SendGarbledMessage)，程式結束
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
            
        	if (FEPReturnCode.Normal.equals(_rtnCode)) {
                //2.CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
                _rtnCode = this.checkBusinessRule();
        		
        		//EXIT PROGRAM
    			if(isExitProgram) {
    				return StringUtils.EMPTY;	
    			}
            }

            // 3.更新交易記錄 (FEPTXN & ICTLTXN)
            if (_rtnCode == FEPReturnCode.Normal) {
                _rtnCode = updateTxData();
                if(_rtnCode != FEPReturnCode.Normal){
                    return StringUtils.EMPTY;
                }
            }
    		
        	if (FEPReturnCode.Normal.equals(_rtnCode)) {
        		//4.判斷是否沖轉跨行代收付ProcessAPTOT及主機帳務SendToCBS)
        		_rtnCode = processAPTOTSendToCBS();
        	}
        	
        	//5. 	label_END_OF_FUNC:
            
        	//6.更新交易記錄(FEPTXN) : if need
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
        	
//            //8.FEP通知主機交易結束
//        	_rtnCode = SendToCBS();
        } catch (Exception ex) {
        	_rtnCode = FEPReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequestData");
            sendEMS(getLogContext());
        }
        logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
        logMessage(Level.DEBUG, getLogContext());
        return StringUtils.EMPTY;
    }

    /**
     * 2.CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
     * @return
     */
    private FEPReturnCode checkBusinessRule() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
        try {
            /* Confirm電文比對原交易 */
            String aa=  (CalendarUtil.rocStringToADString(StringUtils.leftPad(this.getFiscCon().getTxnInitiateDateAndTime().substring(0,6),7,"0"))) + this.getFiscCon().getTxnInitiateDateAndTime().substring(6,12);
            if(!getFiscBusiness().getFeptxn().getFeptxnReqDatetime().equals(aa) ||
                    (!getFiscBusiness().getFeptxn().getFeptxnDesBkno().equals(this.getFiscCon().getTxnDestinationInstituteId().substring(0,3))) ||
                    (StringUtils.isNotBlank(this.getFiscCon().getATMNO()) && !getFiscBusiness().getFeptxn().getFeptxnAtmno().equals(this.getFiscCon().getATMNO()))
                    ||(StringUtils.isNotBlank(this.getFiscCon().getTxAmt()) && !new DecimalFormat("0.00").format(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct()).equals(new DecimalFormat("0.00").format(new BigDecimal(this.getFiscCon().getTxAmt()))))) {
                return FEPReturnCode.OriginalMessageDataError;
            }

			//2.2 檢核交易是否未完成
			if (!"B".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {
				//10/20 修改, 財金錯誤代碼改為 '0101' 
				rtnCode = FISCReturnCode.MessageFormatError;   //**相關欄位檢查錯誤
				return rtnCode;
			}

				/*9/22 修改 for CON 送2次 */
            if (getFiscBusiness().getFeptxn().getFeptxnTraceEjfno() != 0) {
                getLogContext().setRemark("已有Confirm電文, FEPTXN_TRACE_EJFNO=" + getFiscBusiness().getFeptxn().getFeptxnTraceEjfno().toString());
                logMessage(Level.INFO, getLogContext());
                rtnCode = FISCReturnCode.MessageFormatError; // **相關欄位檢查錯誤
                getFiscBusiness().setFeptxn(null); //第2次Confirm則不更新FEPTXN
                getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
                isExitProgram = true;
                return rtnCode;
            }
            
            /* 2024/7/1 以FEPTXN的PK讀取FEPTXNTCB Data */
            Feptxntcb tempFeptxntcb = getFiscBusiness().checkFeptxntcbData(getFiscBusiness().getFeptxn());
            getFiscBusiness().setFeptxntcb(tempFeptxntcb);
            getTxData().setFeptxntcb(tempFeptxntcb);
            if(getFiscBusiness().getFeptxntcb() == null) {
            	return FEPReturnCode.MessageFormatError;
            }

            //2.3 檢核 MAC
            getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscCon().getResponseCode());
			// '2017/11/17 Modify by Ruling for 收到財金確認電文時間寫入FEPTXN
			getFiscBusiness().getFeptxn().setFeptxnConTxTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            // 24-05-20 改用CheckFISCICMAC
            rtnCode = encHelper.checkFISCICMAC(getFiscCon().getMessageType(), getFiscCon().getMAC());
            this.logContext.setRemark("after checkFiscICMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
            if (rtnCode != FEPReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnConRc(null);
                return FEPReturnCode.ENCCheckMACError;//**訊息押碼錯誤

            }
			return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 3.UpdateTxData:更新交易記錄 (FEPTXN& ICTLTXN )
     * @return
     */
    private FEPReturnCode updateTxData() {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        IctltxnExtMapper dbICTLTXN = SpringBeanFactoryUtil.getBean(IctltxnExtMapper.class);
        try {
            getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(false));
            if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
                if (NormalRC.FISC_ATM_OK.equals(getFiscCon().getResponseCode())) {
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("A");
                } else {
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("C"); //Accept-Reverse
                }
                getFiscBusiness().getFeptxn().setFeptxnPending((short) 2);
            }
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm); //F3
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
            
            defICTLTXN.setIctltxnTxDate(getFiscBusiness().getFeptxn().getFeptxnTxDate());
            defICTLTXN.setIctltxnEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());
            defICTLTXN = dbICTLTXN.selectByPrimaryKey(defICTLTXN.getIctltxnTxDate(),defICTLTXN.getIctltxnEjfno());
            defICTLTXN.setIctltxnConRc(getFiscBusiness().getFeptxn().getFeptxnConRc());
            defICTLTXN.setIctltxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
            //Fly 2019/02/13 for 晶片卡跨國提款/消費扣款沖正(2573/2549)
            defICTLTXN.setIctltxnTxFeeCr(getFiscBusiness().getFeptxn().getFeptxnTxFeeCr());
            defICTLTXN.setIctltxnActProfit(getFiscBusiness().getFeptxn().getFeptxnActProfit());
            int iRes = dbICTLTXN.updateByPrimaryKeySelective(defICTLTXN);
            if (iRes <= 0) {
                return IOReturnCode.UpdateFail;
            }
            transactionManager.commit(txStatus);
            return FEPReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateTxData");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }finally {
            if ( !txStatus.isCompleted()) {
                transactionManager.rollback(txStatus);
            }
        }

    }

    /**
     * 4.判斷是否沖轉跨行代收付ProcessAPTOT及主機帳務SendToCBS)
     * @return
     * @throws Exception
     */
    private FEPReturnCode processAPTOTSendToCBS() throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
            //+REP
            if (!NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) {
                //-CON
            	/*沖轉跨行代收付*/
            	rtnCode = getFiscBusiness().processICAptot(true);

                getLogContext().setProgramName(ProgramName);
                getLogContext().setRemark(StringUtils.join("FEPTXN_CBS_TX_CODE=",getFiscBusiness().getFeptxn().getFeptxnCbsTxCode()));
                logMessage(Level.DEBUG, getLogContext());

                /*沖轉主機帳務*/
                if (getTxData().getMsgCtl().getMsgctlCbsFlag() == 2) {//沖正
                	String TxType = "2";		//上CBS沖正
                    this.getTxData().setIctlTxn(defICTLTXN);
                    String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
        			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
        			rtnCode = new CBS(hostAA, getTxData()).sendToCBS(TxType);
                }
                //由GetMessageFromFEPReturnCode執行 SendEMS
                TxHelper.getMessageFromFEPReturnCode(getFiscBusiness().getFeptxn().getFeptxnConRc(), FEPChannel.FISC, getLogContext());
            }else{  //2025/8/24 修改
                /* +CON 送交易結束通知(END)給主機 */
                rtnCode = this.SendToCBSEnd();
            }
        }
        return rtnCode;
    } 
    
    /**
     * 6. 更新交易記錄(FEPTXN) : if need
     * @return
     */
    private FEPReturnCode updateFEPTXN() throws Exception{
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        if (getFiscBusiness().getFeptxn().getFeptxnAaRc() == FEPReturnCode.Normal.getValue()) {
            if (_rtnCode != FEPReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
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
     * 7.判斷是否需傳送2160電文給財金
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
