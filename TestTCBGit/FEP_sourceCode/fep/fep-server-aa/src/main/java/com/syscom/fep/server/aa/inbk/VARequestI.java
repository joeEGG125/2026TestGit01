package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.enums.*;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.VacateMapper;
import com.syscom.fep.mybatis.mapper.VatxnMapper;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * 負責處理財金發動的約定及核驗服務交易Req電文
 *
 * @author Joseph
 */
public class VARequestI extends INBKAABase {
    private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;

    @SuppressWarnings("unused")
    private boolean isIC = false;
    private Vatxn defVATXN = new Vatxn();
    @SuppressWarnings("unused")
    private Intltxn oriINTLTXN = new Intltxn();
    private VatxnMapper dbVATXN = SpringBeanFactoryUtil.getBean(VatxnMapper.class);

    private boolean isEC = false;

    /**
     * AA的建構式,在這邊初始化及設定其他相關變數
     *
     * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
     *                <p>
     *                初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
     *                ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
     *                FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
     * @throws Exception VARequestIException
     */
    public VARequestI(FISCData txnData) throws Exception {
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
            // (1) 	檢核財金電文 Header
            _rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);
            String sFiscRc = TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext());
            if("10".equals(sFiscRc.substring(0, 2))) {
                /* 程式結束 FISC RC:Garbled Message */
                getFiscBusiness().setFeptxn(null);
                getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), _rtnCode, getFiscReq());
                return StringUtils.EMPTY;
            }

            //2.AddTxData:新增交易記錄(FEPTXN)
            _rtnCode2 = this.addTxData();
            if (_rtnCode2 != FEPReturnCode.Normal) {
                // 新增 FEPTXN、FEPTXNTCB 失敗 就結束程式
                getLogContext().setProgramName(ProgramName + ".addTxData");
                getLogContext().setMessage(_rtnCode2.toString());
                getLogContext().setRemark("新增交易記錄有誤!!");
                sendEMS(getLogContext());
                return StringUtils.EMPTY;
            }

            //3.商業邏輯檢核 & 電文Body檢核
            if (_rtnCode == FEPReturnCode.Normal) { /*CheckHeader Error*/
                _rtnCode = checkBusinessRule();
            }

            //4.SendToCBS
            if (_rtnCode == FEPReturnCode.Normal) { /*CheckHeader Error*/
                _rtnCode2 = sendToCBS();
                /*若扣帳Timeout則不組回應電文給財金, 程式結束, 若主機回應扣帳失敗
				則仍需組回應電文給財金 */

                if (feptxn.getFeptxnCbsTimeout() == 1) {
                    /* 主機TimeOut 不組回應電文給財金, 程式結束 */
                    getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode2.getValue());
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("S");  /*Reject-abnormal*/
                    getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/
                    getFiscBusiness().updateTxData();//更新交易記錄
                    return StringUtils.EMPTY;
                }
            }

            //5. 	label_END_OF_FUNC

            //6.組回傳財金Response電文
            _rtnCode = this.prepareForFISC();

            //7.UpdateTxData: 更新交易記錄(FEPTXN)
            _rtnCode = this.updateTxData();
            //程式結束
            if(_rtnCode != FEPReturnCode.Normal) {
                getLogContext().setRemark("updateTxData error");
                logMessage(getLogContext());
                return StringUtils.EMPTY;
            }

            //8.ProcessAPTOT:更新跨行代收付
            _rtnCode = this.processAPTOT();

            //9.將組好的財金電文送給財金
            _rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);

            //10.判斷是否需傳送2160電文給財金
            /* 2025/7/1 修改 for 傳送 2160電文給財金 */
            /* "A" : 成功或失敗均需傳送 */
            if("A".equals(feptxn.getFeptxnSend2160())
                    && !"4001".equals( feptxn.getFeptxnRepRc()) ) {
                _rtnCode = insertINBK2160();
            }

        } catch (Exception e) {
            getLogContext().setProgramException(e);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            sendEMS(getLogContext());
        } finally {
            getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getLogContext().setMessage("FiscResponse:" + this.getFiscRes().getFISCMessage());
            getLogContext().setProgramName(this.aaName);
            getLogContext().setMessageFlowType(MessageFlow.Response);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
            logMessage(Level.DEBUG, getLogContext());
        }

        return StringUtils.EMPTY;
    }


    /**
     * 2. 新增交易記錄
     *
     * @return FEPReturnCode
     */
    private FEPReturnCode addTxData() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        RefBase<Vatxn> vatxnRefBase = new RefBase<>(defVATXN);
        try {
            //(1) 	Prepare() 交易記錄初始資料
            rtnCode = getFiscBusiness().prepareFEPTXN();
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
                getLogContext().setRemark("PREPARE FEPTXN  ERROR");
                sendEMS(getLogContext());
                return rtnCode;
            }

            rtnCode = getFiscBusiness().prepareFEPTXNTCB();
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                getLogContext().setProgramName(ProgramName + ".prepareFEPTXNTCB");
                getLogContext().setRemark("PREPARE FEPTXNTCB ERROR");
                sendEMS(getLogContext());
                return rtnCode;
            }

            rtnCode = getFiscBusiness().prepareVATXN(vatxnRefBase);
            defVATXN = vatxnRefBase.get();
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                getLogContext().setProgramName(ProgramName + ".PrepareVATXN");
                getLogContext().setRemark("PREPARE VATXN ERROR");
                sendEMS(getLogContext());
                return rtnCode;
            }

            //(2) 	新增交易記錄
            PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
            TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

            try {
                rtnCode = getFiscBusiness().insertFEPTxn();
                if (rtnCode != FEPReturnCode.Normal) {
                    return rtnCode;
                }

                rtnCode = getFiscBusiness().insertFEPTXNTCB();
                if (rtnCode != FEPReturnCode.Normal) {
                    return rtnCode;
                }

                if (dbVATXN.insertSelective(defVATXN) < 1) {
                    return IOReturnCode.UpdateFail;
                }

                transactionManager.commit(txStatus);
                return rtnCode;
            } catch (Exception ex) {
                getLogContext().setProgramException(ex);
                getLogContext().setProgramName(StringUtils.join(ProgramName, ".addTxData"));
                sendEMS(getLogContext());
                return FEPReturnCode.ProgramException;
            }finally {
                if ( !txStatus.isCompleted()) {
                    transactionManager.rollback(txStatus);
                }
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".addTxData"));
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 3. 	CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
     * @return FEPReturnCode
     */
    private FEPReturnCode checkBusinessRule() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
        try {
            //(1)	檢核約定及核驗服務業務
            Vacate vacate = new Vacate();
            if (StringUtils.isNotBlank(feptxn.getFeptxnNoticeId())) {
                vacate.setVacateNo(feptxn.getFeptxnNoticeId().substring(0, 2));
            }
            VacateMapper vacateMapper = SpringBeanFactoryUtil.getBean(VacateMapper.class);
            if ( vacateMapper.selectByPrimaryKey(vacate.getVacateNo()) == null) {
                this.logContext.setRemark("業務類別未上線 VACATE_NO=" + vacate.getVacateNo());
                logMessage(getLogContext());
                return FEPReturnCode.MessageFormatError; /* 0101:訊息格式或內容編輯錯誤*/
            }

            /* 2024/7/4 全繳線上約定作業檢核委託單位代號 */
            //(2) 	全繳線上約定作業檢核委託單位代號
            if ( "02".equals(defVATXN.getVatxnCate()) ) {
                rtnCode = getFiscBusiness().checkNpsunit(feptxn);
                if (rtnCode != FEPReturnCode.Normal) {
                    this.logContext.setRemark("checkNpsunit error:" + rtnCode.toString());
                    logMessage(this.logContext);
                    return rtnCode;
                }
            }

            //(3) 	檢核MAC
            rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
            getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
            getLogContext().setRemark("after checkFiscMac RC:" + rtnCode.toString());
            logMessage(getLogContext());
            if (rtnCode != FEPReturnCode.Normal) {
                return rtnCode;
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "checkBusinessRule");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 4. SendToCBS:送往CBS主機處理
     * @return FEPReturnCode
     */
    private FEPReturnCode sendToCBS() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())) {
                /* 交易前置處理查詢處理 */
                String txType = "3";

                this.getTxData().setVatxn(defVATXN);

                String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
                rtnCode = new CBS(hostAA, getTxData()).sendToCBS(txType);
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".SendToCBSAndAsc");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 6. 	PrepareFISC:準備回財金的相關資料
     * @return FEPReturnCode
     */
    private FEPReturnCode prepareForFISC() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;

        //6.1 判斷 rtnCode 是否 Normal
        if (_rtnCode != FEPReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc( _rtnCode.getValue() );
            if ( StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc()) ) {
                getLogContext().setProgramName( StringUtils.join(ProgramName));
                getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext()));
            }
        } else {
            if ( StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc()) ) {
                getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK); /*+REP*/
            }
        }

        //6.2 產生 Response 電文Header:
        rtnCode = getFiscBusiness().prepareHeader("0210");
        if (rtnCode != FEPReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
        }

        //6.3 產生 Response 電文Body:
        String wk_BITMAP = null;
        if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
            // +REP
            wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
        } else {
            // -REP
            wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
        }
        // 依據wk_BITMAP(判斷是否搬值)
        for (int i = 2; i <= 63; i++) {
            // Loop IDX from 3 to 64
            if (wk_BITMAP.charAt(i) == '1') {
                switch (i) {
                    case 5: { /* 代付單位 CD/ATM 代號 */
                        getFiscRes().setATMNO(StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnAtmno(), 8, "0"));
                        break;
                    }
                    case 14: { /* 跨行手續費 */
                        getFiscRes().setFeeAmt(getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct().toString());
                        break;
                    }
                    case 45: { /* 附言欄*/
//                        String fromAct = this.getImsPropertiesValue(tota, ImsMethodName.O_FROM_ACT.getValue());
//                        String AEILFRC1 = this.getImsPropertiesValue(tota, ImsMethodName.O_LF_AEILFRC1.getValue());
//                        String AEILFRC2 = this.getImsPropertiesValue(tota, ImsMethodName.O_LF_AEILFRC2.getValue());
//                        String OPENACT = this.getImsPropertiesValue(tota, ImsMethodName.O_LF_OPENACT.getValue());

                        switch (defVATXN.getVatxnCate()) {
                            case "01":
                                getFiscRes().setMEMO(getFiscReq().getMEMO().substring(0, 65) + getFiscBusiness().getFeptxn().getFeptxnRemark());
                                break;
                            case "02":
                                getFiscRes().setMEMO(getFiscReq().getMEMO().substring(0, 65) + getFiscBusiness().getFeptxn().getFeptxnRemark() + getFiscReq().getMEMO().substring(81, 91));
                                break;
                            case "11": // '統一發票中奬入帳帳號檢核
                                getFiscRes().setMEMO(getFiscReq().getMEMO());
                                break;
                            case "10":
                                /* 2024/7/18 修改 for 行動電話異動核驗 */
                                if (getFiscBusiness().getFeptxn().getFeptxnRepRc().equals("4001") && StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnRemark())) {
                                    getFiscRes().setMEMO(getFiscReq().getMEMO().substring(0, 90) + getFiscBusiness().getFeptxn().getFeptxnRemark().substring(0, 7) + getFiscReq().getMEMO().substring(97, 100) );
                                } else {
                                    getFiscRes().setMEMO(getFiscReq().getMEMO());
                                }
                                break;
                        }
                        break;
                    }
                }
            }
        }

        //6.4 產生 MAC
        RefString refMac = new RefString(getFiscRes().getMAC());
        rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFiscMac(getFiscRes().getMessageType(), refMac);
        getFiscRes().setMAC(refMac.get());
        if (rtnCode != FEPReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
            getFiscRes().setMAC("00000000");
        }

        //6.5 產生Bit Map
        rtnCode = getFiscBusiness().makeBitmap(getFiscRes().getMessageType(), getFiscRes().getProcessingCode(), MessageFlow.Response);
        getLogContext().setRemark("after makeBitmap RC:" + rtnCode.toString());
        logMessage(getLogContext());
        if (rtnCode != FEPReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
            getFiscRes().setBitMapConfiguration("0000000000000000");
        }

        rtnCode = getFiscRes().makeFISCMsg();
        return rtnCode;
    }

    /**
     * 7. 	UpdateTxData: 更新交易記錄(FEPTXN )
     * @return FEPReturnCode
     */
    private FEPReturnCode updateTxData() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            //(1) 	更新 FEPTXN
            if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
                if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {// (3 way)
                    getFiscBusiness().getFeptxn().setFeptxnPending((short) 1); // Pending
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("B"); // Pending
                } else {// (2 way)
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 成功
                }
                if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
                    if (!FISCPCode.PCode2430.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                            && !FISCPCode.PCode2470.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {// 非國際提款沖銷
                        isEC = false;
                        getFiscBusiness().getFeptxn().setFeptxnClrType((short) 1);
                    }
                }
            } else if(StringUtils.equals(getFiscBusiness().getFeptxn().getFeptxnTxrust(), "0")) {
                getFiscBusiness().getFeptxn().setFeptxnTxrust("R");  /*拒絕-正常*/
            }
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // F2-FISC Response
            getFiscBusiness().getFeptxn().setFeptxnAaComplete((short) 1); /*AA Close*/

            rtnCode = getFiscBusiness().updateTxData(); // 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2])
            if (rtnCode != FEPReturnCode.Normal) {// 若更新失敗則不送回應電文, 人工處理
                return rtnCode;
            }

            //(2) 	判斷是否需更新 VATXN
            dbVATXN = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
            defVATXN.setVatxnBrno(getFiscBusiness().getFeptxn().getFeptxnBrno());
            defVATXN.setVatxnZoneCode(getFiscBusiness().getFeptxn().getFeptxnZoneCode());
            defVATXN.setVatxnRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
            defVATXN.setVatxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());

            if (dbVATXN.updateByPrimaryKeySelective(defVATXN) < 1) { // '若更新失敗則不送回應電文，人工處理
                return IOReturnCode.UpdateFail;
            }

            transactionManager.commit(txStatus);
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "updateTxData");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }finally {
            if ( !txStatus.isCompleted()) {
                transactionManager.rollback(txStatus);
            }
        }
    }

    /**
     * 8. 	ProcessAPTOT:更新跨行代收付
     * @return FEPReturnCode
     */
    private FEPReturnCode processAPTOT() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        /* 2018/12/18 原存金融帳戶核驗(類別=10), 新增寫入APTOT */
        if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())
                && "10".equals(defVATXN.getVatxnCate().trim())
                && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
            rtnCode = getFiscBusiness().processAptot(isEC);
            if (rtnCode != FEPReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                getFiscBusiness().updateTxData();
            }
        }
        return rtnCode;
    }




    /**
     * 10. 	判斷是否需傳送2160電文給財金
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
}
