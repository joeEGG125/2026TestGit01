package com.syscom.fep.server.aa.nb;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
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
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS;
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
public class NBSelfIssueRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode3 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode4 = FEPReturnCode.Normal;
    private String AATxTYPE = "";
    private FEPReturnCode rtnCode_error = FEPReturnCode.Normal;

    private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);

    public NBSelfIssueRequestA(NBData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = getnBData().getTxNbfepObject().getRequest().getBody().getRq().getHeader();
        try {
            getFeptxn().setFeptxnStan(getFiscBusiness().getStan());/*先取 STAN 以供主機電文使用*/
            getLogContext().setStan(getFeptxn().getFeptxnStan());

            // 1. Prepare : 交易記錄初始資料
            rtnCode = getFiscBusiness().nb_PrepareFEPTxn();
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
                this.addTxData();
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 3. CheckBusinessRule: 商業邏輯檢核
                RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = getnBData().getTxNbfepObject().getRequest().getBody().getRq().getSvcRq();
                rtnCode = getFiscBusiness().checkRequestFromOtherChannel(getnBData(), tita.getINTIME());
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 4. SendToCBS/ASC: 送主機檢核帳戶資料
                rtnCode = this.sendToCBS();
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }

            FISC fiscBusiness = getFiscBusiness();
            if (feptxn.getFeptxnFiscFlag() != 0 && rtnCode == FEPReturnCode.Normal) {
                // 5. 組送往 FISC 之 Request 電文並等待財金之 Response
                rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }

            boolean repRcEq4001 = true;
            if (feptxn.getFeptxnFiscFlag() != 0 && rtnCode == FEPReturnCode.Normal) {
                // 6. CheckResponseFromFISC:檢核回應電文是否正確
                rtnCode = fiscBusiness.checkResponseMessage();

                repRcEq4001 = "4001".equals(feptxn.getFeptxnRepRc());
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }

            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                // 7. SendToCBS/ASC: 送主機處理帳務
                rtnCode = this.sendToCBS2();
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
                rtnCode2 = rtnCode;
//                if (rtnCode == FEPReturnCode.Normal && rtnCode2 == FEPReturnCode.Normal && repRcEq4001 && "000".equals(feptxn.getFeptxnCbsRc()) && "1".equals(feptxn.getFeptxnAccType())) {
//                    //  ProcessAPTOT:更新跨行代收付
//                    if (1 == getnBData().getMsgCtl().getMsgctlUpdateAptot()) {
//                        rtnCode = fiscBusiness.processAptot(false);
//                    }
//                }
            }
            // 8. label_END_OF_FUNC : 判斷是否需組 CON 電文回財金
            this.labelEndOfFunc();

            // 9. 	更新交易記錄(FEPTXN)
            this.updateTxData();

            // 10. 	組NB回應電文 & 回 NBMsgHandler
            if(StringUtils.equals(getLogContext().getChannel().getNameS(),FEPChannel.VO.getNameS())){
                rtnMessage = getFiscBusiness().prepareVOResponseData(tota);
            } else {
                rtnMessage = getFiscBusiness().prepareNBResponseData(tota, getLogContext().getChannel());
            }


            // 11. 交易通知 (if need)
            getFiscBusiness().sendToNotify();

        } catch (Exception ex) {
            rtnMessage = getResNBStr(atmReqheader);
            rtnCode = FEPReturnCode.ProgramException;
            getFiscBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {
            getnBData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getnBData().getLogContext().setMessage(rtnMessage);
            getnBData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getnBData().getLogContext().setMessageFlowType(MessageFlow.Response);
            String codeToMsg = String.valueOf((rtnCode_error != CommonReturnCode.Normal ? rtnCode_error : rtnCode).getValue());
            FEPChannel channel = FEPChannel.FEP;
            // 2026.1.30 LeYun 交易異常發送mail
            if (rtnCode_error == CommonReturnCode.Normal && rtnCode == CommonReturnCode.Normal) {
                if (rtnCode2 != CommonReturnCode.Normal) {
                    codeToMsg = String.valueOf(rtnCode2.getValue()); // 主機錯誤
                } else if (getFeptxn() != null
                        && StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())
                        && !"4001".equals(getFeptxn().getFeptxnRepRc())) {
                    codeToMsg = getFeptxn().getFeptxnRepRc();
                    channel = FEPChannel.FISC; // 使用財金 Channel 查錯誤碼
                }
            }

            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(codeToMsg, channel, getLogContext()));
            logMessage(Level.DEBUG, this.logContext);

        }
        return rtnMessage.replace("&#xf;","     ");
    }

    /**
     * 2. AddTxData: 新增交易記錄( FEPTxn)
     *
     * @return
     * @throws Exception
     */
    private void addTxData() throws Exception {
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
        }
    }


    /**
     * 4. SendToCBS/ASC: 送主機檢核帳戶資料
     *
     * @throws Exception
     */
    private FEPReturnCode sendToCBS() throws Exception {
        feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
        AATxTYPE = "0"; // 上CBS查詢、檢核
        String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid1();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
        rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
        if (!"4001".equals(feptxn.getFeptxnAscRc())) {
            if ("T203".equals(feptxn.getFeptxnAscRc())) {
                feptxn.setFeptxnReplyCode("T001");
                feptxn.setFeptxnAscRc("T001");
            }else{
                feptxn.setFeptxnReplyCode(feptxn.getFeptxnAscRc());
            }
        }
        tota = hostAA.getTota();
        return rtnCode;
    }

    /**
     * 7. SendToCBS/ASC: 送主機處理帳務
     *
     * @throws Exception
     */
    private FEPReturnCode sendToCBS2() throws Exception {
        feptxn.setFeptxnTxrust("S");

        /* 進主機入扣帳/手續費 */
        AATxTYPE = "1"; // 上CBS入扣帳

        String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
        rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
        return rtnCode;
    }

    /**
     * 8. label_END_OF_FUNC :判斷是否需組 CON 電文回財金
     *
     * @throws Exception
     */
    private FEPReturnCode labelEndOfFunc() throws Exception {
        try {
            if (rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) { /*+REP*/
                if (DbHelper.toBoolean(getnBData().getMsgCtl().getMsgctlAtm2way())) {
                    getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                    getFeptxn().setFeptxnReplyCode("4001");/*回覆 ATM正常*/
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // 成功
                    /*轉帳交易直接送 Confirm 給財金*/
                    getFeptxn().setFeptxnConRc(NormalRC.FISC_ATM_OK);/*+CON*/
                    if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {
                        rtnCode3 = getFiscBusiness().sendConfirmToFISC();
                    }
                } else {
                    /* for代理提款-ATM_3way 交易 */
                    getFeptxn().setFeptxnReplyCode("4001");/*回覆 ATM正常*/
                    feptxn.setFeptxnTxrust("B"); /* PENDING */
                }
            } else {
                getLogContext().setProgramName(ProgramName);
                // 交易失敗
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())) {
                    getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                    if (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) { // +REP
                        if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) {
                            feptxn.setFeptxnConRc(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
                            if ("TS".equals(feptxn.getFeptxnTxCode())) {
                                /*EAI沖正存款主機手續費優惠次數(原解圈) */
                                AATxTYPE = "4";
                                String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid1();
                                feptxn.setFeptxnCbsTxCode(AA);
                                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
                                rtnCode4 = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
                                tota = hostAA.getTota();
                            }
                        } else {
                            feptxn.setFeptxnConRc(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
                                    FEPChannel.FEP, FEPChannel.FISC, getnBData().getLogContext()));
                        }
                        feptxn.setFeptxnErrMsg(getLogContext().getResponseMessage().length() > 256 ? getLogContext().getResponseMessage().substring(0, 256) : getLogContext().getResponseMessage());
                        feptxn.setFeptxnTxrust("R"); /* Accept-Reverse */
                        String FEP_IMS_RC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC4_FISC.getValue());
                        if (FEP_IMS_RC4_FISC == null || StringUtils.isBlank(FEP_IMS_RC4_FISC)){
                            getFeptxn().setFeptxnConRc(AbnormalRC.ATM_Error);
                        }else {
                            if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {
                                getFeptxn().setFeptxnConRc(FEP_IMS_RC4_FISC);
                            }
                        }
                        if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {
                            rtnCode3 = getFiscBusiness().sendConfirmToFISC();
                        }
                    } else { // -REP
                        feptxn.setFeptxnTxrust("R"); /* Reject-normal */
                        if ("10203".equals(String.valueOf(rtnCode.getValue())) ) {
                            String replyCode = msgfileExtMapper.selectByPrimaryKey(7, String.valueOf(rtnCode.getValue())).getMsgfileAtm();
                            if (replyCode == null || StringUtils.isBlank(replyCode)) {
                                replyCode = AbnormalRC.ATM_Error;
                            }
                            feptxn.setFeptxnReplyCode(replyCode);
                        } else {
                            getFeptxn().setFeptxnReplyCode(getFeptxn().getFeptxnRepRc());
                        }
                        feptxn.setFeptxnErrMsg(getLogContext().getResponseMessage().length() > 256 ? getLogContext().getResponseMessage().substring(0, 256) : getLogContext().getResponseMessage());
                        if ("TS".equals(feptxn.getFeptxnTxCode())) {
                            /*EAI沖正存款主機手續費優惠次數(原解圈) */
                            AATxTYPE = "4";
                            String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid1();
                            feptxn.setFeptxnCbsTxCode(AA);
                            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
                            rtnCode4 = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
    						tota = hostAA.getTota();
                        }
                    }
                } else { // fepReturnCode <> Normal
                    if ("TS".equals(feptxn.getFeptxnTxCode())) {
                        /*EAI沖正存款主機手續費優惠次數(原解圈) */
                        AATxTYPE = "4";
                        String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid1();
                        feptxn.setFeptxnCbsTxCode(AA);
                        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
                        rtnCode4 = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
                        tota = hostAA.getTota();
                    }
                    getLogContext().setRemark(StringUtils.join("FepTxn.FEPTXN_REPLY_CODE before:", getFeptxn().getFeptxnReplyCode()));
                    logMessage(Level.DEBUG, getLogContext());
                    if (StringUtils.isNotBlank(getFeptxn().getFeptxnCbsRc())) {
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
                    } else {
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
                    }
                    getLogContext().setRemark(StringUtils.join("FepTxn.FEPTXN_REPLY_CODE after:", getFeptxn().getFeptxnReplyCode()));
                    logMessage(Level.DEBUG, getLogContext());
                }
                getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
                setFeptxn(feptxn);
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
     * 9. 更新交易記錄(FEPTXN)
     */
    private void updateTxData() {
        String replyCode = " ";
        if ( "4001".equals(feptxn.getFeptxnRepRc()) || StringUtils.isBlank(feptxn.getFeptxnRepRc()) ){
            // 內部錯誤時才更新 ReplyCode
            if (rtnCode != FEPReturnCode.Normal || rtnCode2 != FEPReturnCode.Normal) {
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
                }else if("10203".equals(String.valueOf(rtnCode.getValue()))){
                    //主機回應timeout
                    feptxn.setFeptxnAscRc("T001");
                    replyCode = "T001";
                }else {
                    replyCode = msgfileExtMapper.selectByPrimaryKey(7,String.valueOf(rtnCode.getValue())).getMsgfileAtm();
                    if (replyCode==null || StringUtils.isBlank(replyCode)){
                        replyCode = AbnormalRC.ATM_Error;
                    }
                }
                logContext.setRemark(String.valueOf(rtnCode2.getValue()));
                logMessage(logContext);
                if("10203".equals(String.valueOf(rtnCode2.getValue()))){
                    //第二次送主機timeout >>>T203
                    feptxn.setFeptxnAscRc("T203");
                    replyCode = "T203";
                }
                getFeptxn().setFeptxnReplyCode(replyCode);
                List<Msgfile> msgfileList = msgfileExtMapper.selectByMsgfileErrorcode(String.valueOf(rtnCode.getValue()));
                if(msgfileList.size()!=0){
                    feptxn.setFeptxnErrMsg(msgfileList.get(0).getMsgfileShortmsg());
                }
            }

        } else {
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
        feptxn.setFeptxnAaComplete((short) 1); /* AA Close */
        feptxn.setFeptxnMsgflow("A2"); /* ATM Response*/
        this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
    }

    /**
     * 9. 更新交易記錄
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

    private String getResNBStr(RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header nbheader) {

        String ResStr = "";
        SEND_NB_GeneralTrans_RS nbRs = new SEND_NB_GeneralTrans_RS();
        SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body();
        SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs();
        SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
        SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
        msgrs.setHeader(header);
        body.setOUTDATE(" ");
        body.setOUTTIME(" ");
        body.setFEP_EJNO(" ");
        body.setTXNSTAN(" ");
        body.setCUSTOMERID(" ");
        body.setTXNTYPE("RQ");
        body.setHOSTACC_FLAG(" ");
        body.setTRANSAMT(new BigDecimal("0"));
        body.setTRANSFROUTBAL(new BigDecimal("0"));
        body.setTRANSOUTAVBL(new BigDecimal("0"));
        body.setTRANSAMTOUT(new BigDecimal("0"));
        body.setTRNSFROUTIDNO(" ");
        body.setTRNSFROUTNAME(" ");
        body.setTRNSFROUTBANK(" ");
        body.setTRNSFROUTACCNT(" ");
        body.setTRNSFRINBANK(" ");
        body.setTRNSFRINACCNT(" ");
        body.setCLEANBRANCHOUT(" ");
        body.setCLEANBRANCHIN(" ");
        body.setCUSTPAYFEE(new BigDecimal("0"));
        body.setFISCFEE(new BigDecimal("0"));
        body.setOTHERBANKFEE(new BigDecimal("0"));
        body.setCHAFEE_BRANCH(" ");
        body.setCHAFEEAMT(new BigDecimal("0"));
        body.setTRNSFRINNOTE(" ");
        body.setTRNSFROUTNOTE(" ");
        body.setPAYEREMAIL(" ");
        body.setCUSTOMERNATURE(" ");
        body.setTCBRTNCODE(" ");
        msgrs.setSvcRs(body);
        rsbody.setRs(msgrs);
        nbRs.setBody(rsbody);

        header.setCLIENTTRACEID(nbheader.getCLIENTTRACEID());
        header.setCHANNEL(nbheader.getCHANNEL());
        header.setMSGID(nbheader.getMSGID());
        header.setCLIENTDT(nbheader.getCLIENTDT());
        SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
        header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short)1,"NATM_SYSTEMID_FEP").getSysconfValue());
        header.setSTATUSCODE("T099");
        header.setSEVERITY("ERROR");
        header.setSTATUSDESC("發生exception");
        if(StringUtils.equals(getLogContext().getChannel().getNameS() , FEPChannel.MCH.getNameS()))
            header.setTXNID(nbheader.getTXNID());

        ResStr = XmlUtil.toXML(nbRs);
        return ResStr;
    }
}
