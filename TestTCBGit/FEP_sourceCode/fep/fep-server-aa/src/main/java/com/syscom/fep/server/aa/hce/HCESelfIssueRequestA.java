package com.syscom.fep.server.aa.hce;

import com.syscom.fep.base.aa.HCEData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.model.Msgfile;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @author
 */
public class HCESelfIssueRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode_error = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;

    private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);

    public HCESelfIssueRequestA(HCEData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        try {
            getFeptxn().setFeptxnStan(getFiscBusiness().getStan());/*先取 STAN 以供主機電文使用*/
            getLogContext().setStan(getFeptxn().getFeptxnStan());

            // 1. Prepare : 交易記錄初始資料
            rtnCode = getFiscBusiness().hce_PrepareFEPTxn();
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
                getLogContext().setRemark("PREPARE FEPTXN ERROR");
                sendEMS(getLogContext());
                rtnCode_error = rtnCode;

            }

            if (rtnCode == FEPReturnCode.Normal) {
                rtnCode = getFiscBusiness().other_prepareFEPTXNTCB();
                if (rtnCode != FEPReturnCode.Normal) {
                    // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                    getLogContext().setProgramName(ProgramName + ".prepareFEPTXNTCB");
                    getLogContext().setRemark("PREPARE FEPTXNTCB ERROR");
                    sendEMS(getLogContext());
                    if (rtnCode_error == FEPReturnCode.Normal) {
                        rtnCode_error = rtnCode;
                    }
                }
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 2. AddTxData: 新增交易記錄(FEPTXN)
                rtnCode = addTxData();
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 3. CheckBusinessRule: 商業邏輯檢核
                RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
                rtnCode = this.getFiscBusiness().checkRequestFromOtherChannel(this.getmHCEtxData(), tita.getINTIME());
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }
            Boolean firstTocbs = false;
            if (rtnCode == FEPReturnCode.Normal) {
                // 4. SendToCBS/ASC: 送主機檢核帳戶資料
                this.sendToCBS();
                firstTocbs = true;
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }

            FISC fiscBusiness = getFiscBusiness();
            String cbsRc = getFiscBusiness().getFeptxn().getFeptxnCbsRc();
            if ((rtnCode == FEPReturnCode.Normal && StringUtils.isBlank(cbsRc))
                    ||  (rtnCode == FEPReturnCode.Normal && StringUtils.equals(cbsRc, NormalRC.CBS_OK))){
                //RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
                //getATMRequest().setICMARK(tita.getICMARK());
                //getATMRequest().setIC_TAC(tita.getIC_TAC());
                //getATMRequest().setIC_TAC_LEN(tita.getIC_TAC_LEN());
                // 5. 組送往 FISC 之 Request 電文並等待財金之 Response
                rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }
            boolean repRcEq4001 = true;
            if (rtnCode == FEPReturnCode.Normal && StringUtils.equals(cbsRc, NormalRC.CBS_OK)) {
                // 6. CheckResponseFromFISC:檢核回應電文是否正確
                rtnCode = fiscBusiness.checkResponseMessageForHce();
                RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
                repRcEq4001 = "4001".equals(feptxn.getFeptxnRepRc());
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }


            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                // 7. SendToCBS/ASC: 送主機處理帳務
                this.sendToCBS2();
                if (rtnCode != FEPReturnCode.Normal) {
                    if (1 == getmHCEtxData().getMsgCtl().getMsgctlUpdateAptot()) {
                        /* 沖回跨行代收付(APTOT) */
                        rtnCode2 = fiscBusiness.processAptot(true);
                        if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                            rtnCode_error = rtnCode;
                        }
                    }
                    // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
                }
            }

            // 8. label_END_OF_FUNC :判斷是組 CON 電文回財金( if need)
            this.labelEndOfFunc();

            //9.組HCE回應電文 & 回 HCEMsgHandler
            ////CALL  PrepareHCEesponseData  /* 組 HCE 回應電文 */
            rtnMessage = getFiscBusiness().prepareHCEResponseData(tota);

            //10. 	更新交易記錄(FEPTXN)
            this.updateTxData();

            //11. 交易通知(if need)
            getFiscBusiness().sendToNotify();

            //12. 交易結束通知主機(By PCODE)
            //判斷第一道是否有送 如果沒送第一道不須回傳END
            if (firstTocbs){
                this.transactionCloseConnect();
            }

            //13. 	寫入傳送授權結果通知訊息初始資料 INBK2160 (if need)
            if ("Y".equals(feptxn.getFeptxnSend2160()) && ((feptxn.getFeptxnFiscFlag() == 0 && "000".equals(feptxn.getFeptxnCbsRc()) && "4001".equals(feptxn.getFeptxnConRc()))
                    || (feptxn.getFeptxnFiscFlag() == 1 && "4001".equals(feptxn.getFeptxnConRc())))) {
                /*Prepare : 寫入傳送授權結果通知訊息初始資料 INBK2160*/
                rtnCode = getFiscBusiness().prepareInbk2160();
            } else if ("A".equals(feptxn.getFeptxnSend2160())) {
                /*Prepare : 寫入傳送授權結果通知訊息初始資料 INBK2160*/
                rtnCode = getFiscBusiness().prepareInbk2160();
            }
            logContext.setRemark("FeptxnSend2160=" + feptxn.getFeptxnSend2160() + ",FeptxnFiscFlag=" + feptxn.getFeptxnFiscFlag() + ",CBSRC=" + feptxn.getFeptxnCbsRc() + ",FiscRc=" + feptxn.getFeptxnConRc());
            logMessage(Level.DEBUG, this.logContext);
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                if (rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
                sendEMS(getLogContext());
            }
        } catch (Exception ex) {
            RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getHeader();
            rtnMessage = getResHCEStr(atmReqheader);
            rtnCode = FEPReturnCode.ProgramException;
            this.getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {
            getmHCEtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getmHCEtxData().getLogContext().setMessage(rtnMessage);
            getmHCEtxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getmHCEtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode_error,getLogContext()));
            logMessage(Level.DEBUG, this.logContext);
        }
        return rtnMessage;
    }


    /**
     * 2. AddTxData: 新增交易記錄( FEPTxn)
     *
     * @return
     * @throws Exception
     */
    private FEPReturnCode addTxData() throws Exception {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // 新增交易記錄(FEPTxn) Returning FEPReturnCode
            /* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            int insertCount = feptxnDao.insertSelective(this.feptxn); // 新增資料
            int insertCount2 = feptxnDao.insertSelective(feptxntcb);
            if (insertCount <= 0 || insertCount2 <= 0) { // 更新失敗
                throw new Exception(); //2025.07.15 Transaction call review 調整
            }
            transactionManager.commit(txStatus);
            this.logContext.setRemark("transaction sucess.");
            logMessage(Level.INFO, logContext);
        } catch (Exception ex) { // 新增失敗
            transactionManager.rollback(txStatus);
            this.logContext.setRemark("transaction fail.");
            logMessage(Level.INFO, logContext);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".addTxData");
            sendEMS(getLogContext());
            rtnCode = FEPReturnCode.FEPTXNInsertError;
            return rtnCode;
        }
        return CommonReturnCode.Normal;
    }


    /**
     * 4. SendToCBS/ASC: 送主機檢核帳戶資料
     *
     * @throws Exception
     */
    private void sendToCBS() throws Exception {
        // 轉入方為本行時,先送CBS查詢帳號
        feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */

        String AATxTYPE = "0"; // 上CBS查詢、檢核
        String AA = getmHCEtxData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmHCEtxData());
        rtnCode = new CBS(hostAA, getmHCEtxData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
    }
//		}

    /**
     * 8. SendToCBS/ASC: 送主機處理帳務
     *
     * @throws Exception
     */
    private void sendToCBS2() throws Exception {
        feptxn.setFeptxnTxrust("S");
        if (getmHCEtxData().getMsgCtl().getMsgctlCbsFlag() == 1) {
            /* 進主機入扣帳/手續費 */
            String AATxTYPE = "1"; // 上CBS入扣帳
            String AA = getmHCEtxData().getMsgCtl().getMsgctlTwcbstxid();
            feptxn.setFeptxnCbsTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmHCEtxData());
            rtnCode = new CBS(hostAA, getmHCEtxData()).sendToCBS(AATxTYPE);
            tota = hostAA.getTota();
        }
    }

    /**
     * 9. label_END_OF_FUNC :判斷是組 CON 電文回財金( if need)
     *
     * @throws Exception
     */
    private FEPReturnCode labelEndOfFunc() throws Exception {
        try {
            if (rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
                if (DbHelper.toBoolean(getmHCEtxData().getMsgCtl().getMsgctlAtm2way())) {
                    getFeptxn().setFeptxnReplyCode("4001");/*回覆 ATM正常*/
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // 成功
                    getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                    getFeptxn().setFeptxnConRc(NormalRC.FISC_ATM_OK);
                    if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {
                        rtnCode2 = getFiscBusiness().sendConfirmToFISC();
                    }
                } else {
                    /* for代理提款-ATM_3way 交易 */
                    feptxn.setFeptxnTxrust("B"); /* PENDING */
                    feptxn.setFeptxnMsgflow("A2"); /* ATM Response */
                    this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
                }
            } else {
                getLogContext().setProgramName(ProgramName);
                // 交易失敗
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())) {
                    getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                    if ("4001".equals(getFeptxn().getFeptxnRepRc())) { // +REP
                        if (!DbHelper.toBoolean(getmHCEtxData().getMsgCtl().getMsgctlAtm2way())) {

                            if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) {
                                feptxn.setFeptxnConRc(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
                            } else {
                                feptxn.setFeptxnConRc(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
                                        FEPChannel.FEP, FEPChannel.FISC, getnBData().getLogContext()));
                            }
                            getFeptxn().setFeptxnErrMsg(this.getLogContext().getResponseMessage());
                            getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal); // Accept-Reverse
                        } else {
                            getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
                            getFeptxn().setFeptxnErrMsg(this.getLogContext().getResponseMessage());
                        }
                        String FEP_IMS_RC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC4_FISC.getValue());
                        if (FEP_IMS_RC4_FISC == null || StringUtils.isBlank(FEP_IMS_RC4_FISC)){
                            getFeptxn().setFeptxnConRc(AbnormalRC.ATM_Error);
                        }else {
                            if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {
                                getFeptxn().setFeptxnConRc(FEP_IMS_RC4_FISC);
                            }
                        }
                        if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {
                            rtnCode2 = getFiscBusiness().sendConfirmToFISC();
                        }
                    } else { // -REP
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
                        getFeptxn().setFeptxnErrMsg(this.getLogContext().getResponseMessage());
                    }
                } else { // fepReturnCode <> Normal
                    getLogContext().setRemark(StringUtils.join("FepTxn.FEPTXN_REPLY_CODE before:", getFeptxn().getFeptxnReplyCode()));
                    if (StringUtils.isNotBlank(getFeptxn().getFeptxnCbsRc())) {
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
                    } else {
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
                    }

                    if (StringUtils.isBlank(getFeptxn().getFeptxnReplyCode())) {
                        if ( "10203".equals(String.valueOf(rtnCode.getValue())) ) {
                            String replyCode = msgfileExtMapper.selectByPrimaryKey(7, String.valueOf(rtnCode.getValue())).getMsgfileAtm();
                            if (replyCode == null || StringUtils.isBlank(replyCode)) {
                                replyCode = AbnormalRC.ATM_Error;
                            }
                            getFeptxn().setFeptxnReplyCode(replyCode);
                        } else {
                            getFeptxn().setFeptxnReplyCode(getFeptxn().getFeptxnRepRc());
                        }
                        getFeptxn().setFeptxnErrMsg(this.getLogContext().getResponseMessage());
                    }
                    getLogContext().setRemark(StringUtils.join("FepTxn.FEPTXN_REPLY_CODE after:", getFeptxn().getFeptxnReplyCode()));
                    logMessage(Level.DEBUG, getLogContext());
                }
                getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
                if (rtnCode != FEPReturnCode.Normal) {
                    List<Msgfile> msgfileList = msgfileExtMapper.selectByMsgfileErrorcode(String.valueOf(rtnCode.getValue()));
                    if(msgfileList.size()!=0){
                        feptxn.setFeptxnErrMsg(msgfileList.get(0).getMsgfileShortmsg());
                    }
                }
                String replyCode = " ";
                String rtn = "";
                if (FEPReturnCode.CBSCheckError.equals(rtnCode) ){
                    String FEP_IMS_RC3_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC3_TCB.getValue());
                    String FEP_IMS_RC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC4_FISC.getValue());
                    if (FEP_IMS_RC4_FISC == null || FEP_IMS_RC4_FISC.trim().equals("") ){
                        rtn = FEP_IMS_RC3_TCB;
                    }else{
                        rtn = FEP_IMS_RC4_FISC;
                    }
                }
                if (rtn.length() == 4 || rtn.length() == 3 ){
                    replyCode = rtn;
                }else {
                    replyCode = msgfileExtMapper.selectByPrimaryKey(7,String.valueOf(rtnCode.getValue())).getMsgfileAtm();
                    if (replyCode==null || StringUtils.isBlank(replyCode)){
                        replyCode = AbnormalRC.ATM_Error;
                    }
                }
                getFeptxn().setFeptxnReplyCode(replyCode);
                getFiscBusiness().updateTxData();
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToConfirm"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 11. 更新交易記錄(FEPTXN)
     */
    private void updateTxData() {
        String replyCode = " ";
        if ( "4001".equals(feptxn.getFeptxnRepRc()) || StringUtils.isBlank(feptxn.getFeptxnRepRc()) ){
            if (rtnCode != FEPReturnCode.Normal) {
                String rtn = "";
                if (FEPReturnCode.CBSCheckError.equals(rtnCode) ){
                    String FEP_IMS_RC3_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC3_TCB.getValue());
                    String FEP_IMS_RC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC4_FISC.getValue());
                    if (FEP_IMS_RC4_FISC == null || FEP_IMS_RC4_FISC.trim().equals("") ){
                        if (FEP_IMS_RC3_TCB == null || FEP_IMS_RC3_TCB.trim().equals("") ){
                            if (feptxn.getFeptxnAscRc() == null || feptxn.getFeptxnAscRc().trim().equals("") || feptxn.getFeptxnAscRc().trim().equals("000") || feptxn.getFeptxnAscRc().trim().equals("4001") ){
                                rtn = feptxn.getFeptxnCbsRc();
                            }else{
                                rtn = feptxn.getFeptxnAscRc();
                            }
                        }else{
                            rtn = FEP_IMS_RC3_TCB;
                        }
                    }else{
                        rtn = FEP_IMS_RC4_FISC;
                    }
                }
                if (rtn.length() == 4 || rtn.length() == 3 ){
                    replyCode = rtn;
                }else {
                    if(rtnCode == FEPReturnCode.Normal){
                        replyCode = "4001";
                    }else {
                        replyCode = msgfileExtMapper.selectByPrimaryKey(7,String.valueOf(rtnCode.getValue())).getMsgfileAtm();
                    }
                    if (replyCode==null || StringUtils.isBlank(replyCode)){
                        replyCode = AbnormalRC.ATM_Error;
                    }
                }
                getFeptxn().setFeptxnReplyCode(replyCode);
                List<Msgfile> msgfileList = msgfileExtMapper.selectByMsgfileErrorcode(String.valueOf(rtnCode.getValue()));
                if(msgfileList.size()!=0){
                    feptxn.setFeptxnErrMsg(msgfileList.get(0).getMsgfileShortmsg());
                }
            } else {
                feptxn.setFeptxnReplyCode("4001");
            }

        }else{
            // RepRc 有值且不是 4001 時
            replyCode = feptxn.getFeptxnRepRc();
            getFeptxn().setFeptxnReplyCode(replyCode);
        }

        if (rtnCode != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode.getValue());
        } else if (rtnCode2 != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode2.getValue());
        } else {
            feptxn.setFeptxnAaRc(FEPReturnCode.Normal.getValue());
        }
        feptxn.setFeptxnMsgflow("A2");
        feptxn.setFeptxnAaComplete((short) 1); /* AA Close */
        this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
    }

    /**
     * 更新feptxn
     *
     * @return
     */
    private void updateFeptxn() {
        try {
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFeptxn");
            sendEMS(getLogContext());
        }
    }

    /**
     * 轉民國年,格式化為YY/MM/DD,年度取後碼,ex :11/06/29
     *
     * @param dateStr
     * @return
     */
    private String dateStrToYYMMDD(String dateStr) {
        String rtnDate;
        if ("00000000".equals(dateStr) || dateStr.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
            rtnDate = "00/00/00";
        } else {
            dateStr = CalendarUtil.adStringToROCString(dateStr);
            int dateStrLength = dateStr.length();
            dateStr = dateStr.substring(dateStrLength - 6, dateStrLength).replaceAll("(.{2})", "$1/");
            rtnDate = dateStr.substring(0, dateStr.length() - 1);
        }
        return rtnDate;
    }

    /**
     * 13. 交易結束通知主機 (By PCODE)
     */

    public void transactionCloseConnect() {
        try {
            String AATxTYPE = ""; //不需提供此值
            String AATxRs = "N";//不需等待主機回應
            String AA = getmHCEtxData().getMsgCtl().getMsgctlTwcbstxid1();
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmHCEtxData());
            rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE, AATxRs);
            if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                rtnCode_error = rtnCode;
            }
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
            sendEMS(this.logContext);
        }
    }

    private String getResHCEStr(RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header nbheader) {

        String ResStr = "";
        SEND_HCE_GeneralTrans_RS nbRs = new SEND_HCE_GeneralTrans_RS();
        SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body rsbody = new SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body();
        SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs();
        SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header();
        SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs();
        msgrs.setHeader(header);
        msgrs.setSvcRs(body);
        rsbody.setRs(msgrs);
        nbRs.setBody(rsbody);

        header.setCLIENTTRACEID(nbheader.getCLIENTTRACEID());
        header.setCHANNEL(nbheader.getCHANNEL());
        header.setMSGID(nbheader.getMSGID());
        header.setCLIENTDT(nbheader.getCLIENTDT());
        SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
        sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
        header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short)1,"NATM_SYSTEMID_FEP").getSysconfValue());
        header.setSTATUSCODE("T099");
        header.setSEVERITY("ERROR");
        header.setSTATUSDESC("發生exception");

        body.setOUTDATE(" ");
        body.setOUTTIME(" ");
        body.setFEP_EJNO(" ");
        body.setTXNSTAN(" ");
        body.setCUSTOMERID(" ");
        body.setTXNTYPE("RQ");
        body.setFSCODE(" ");
        body.setACCTDATE(" ");
        body.setHOSTACC_FLAG(" ");
        body.setHOSTRVS_FLAG(" ");
        body.setTRANSAMT(new BigDecimal("0"));
        body.setTRANSFROUTBAL(new BigDecimal("0"));
        body.setTRANSOUTAVBL(new BigDecimal("0"));
        body.setTRNSFROUTBANK(" ");
        body.setTRNSFROUTACCNT(" ");
        body.setCLEANBRANCHOUT(" ");
        body.setTRNSFRINBANK(" ");
        body.setTRNSFRINACCNT(" ");
        body.setCLEANBRANCHIN(" ");
        body.setCUSTPAYFEE(new BigDecimal("0"));
        if (StringUtils.equals("4001", this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue()))) {
            String FEP_IMS_RC3_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC3_TCB.getValue());
            String FEP_IMS_RC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC4_FISC.getValue());
            //CB_IQTX_O001
            if (feptxn.getFeptxnFiscFlag() == 0) {
                //自行3
                if (FEP_IMS_RC4_FISC == null || FEP_IMS_RC4_FISC.trim().equals("")) {
                    if (FEP_IMS_RC3_TCB == null || FEP_IMS_RC3_TCB.trim().equals("") || "000".equals(FEP_IMS_RC3_TCB.trim())) {
                        nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE("4001");
                    } else {
                        nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE(FEP_IMS_RC3_TCB);
                    }
                } else {
                    //自行4
                    nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE(this.getImsPropertiesValue(tota, FEP_IMS_RC4_FISC));
                }
            } else {
                //跨行
                nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE(this.getImsPropertiesValue(tota, FEP_IMS_RC4_FISC));
            }
        } else {
            //CB_IQTX_O002
            String OUTRTC = this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue());
            nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE(OUTRTC);
        }
        if(nbRs.getBody().getRs().getSvcRs().getTCBRTNCODE() ==null || "".equals(nbRs.getBody().getRs().getSvcRs().getTCBRTNCODE().trim())){
            if(feptxn.getFeptxnReplyCode() == null || "".equals(feptxn.getFeptxnReplyCode().trim()) ){
                nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE("T001");
            }else{
                nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxn.getFeptxnReplyCode());
                if("0000".equals(feptxn.getFeptxnReplyCode().trim())){
                    nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE("T001");
                }
            }
        }

        ResStr = XmlUtil.toXML(nbRs);
        return ResStr;
    }
}
