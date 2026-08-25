package com.syscom.fep.server.aa.nb;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.AbnormalRC;
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
import java.util.Objects;

/**
 * @author
 */
public class NBXOOtherRequestA extends INBKAABase {
    private Object tota = null;
    private Object tota1 = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode1 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode3 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode4 = FEPReturnCode.Normal;
    private String AATxTYPE = "";

    private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);

    public NBXOOtherRequestA(NBData txnData) throws Exception {
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
                return rtnMessage;
            }

            rtnCode = getFiscBusiness().other_prepareFEPTXNTCB();
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                getLogContext().setProgramName(ProgramName + ".prepareFEPTXNTCB");
                getLogContext().setRemark("PREPARE FEPTXNTCB ERROR");
                sendEMS(getLogContext());
                return rtnMessage;
            }


            // 2. AddTxData: 新增交易記錄(FEPTXN)
            this.addTxData();
            if (rtnCode != FEPReturnCode.Normal) {
                return rtnMessage;
            }


            // 3. CheckBusinessRule: 商業邏輯檢核
            RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = getnBData().getTxNbfepObject().getRequest().getBody().getRq().getSvcRq();
            rtnCode = getFiscBusiness().checkRequestFromOtherChannel(getnBData(), tita.getINTIME());
            if (rtnCode != FEPReturnCode.Normal) {
                feptxn.setFeptxnTxrust("S"); /*Reject-abnormal*/
                // GO TO  8    /* 更新交易紀錄 */
            }


            if (rtnCode == FEPReturnCode.Normal) {
                // 4. SendToCBS/ASC: 送帳務主機查詢帳號
                this.sendToCBS();
            }

            FISC fiscBusiness = getFiscBusiness();
            if (rtnCode == FEPReturnCode.Normal) {
                // 5. 組送往 FISC 之 Request 電文並等待財金之 Response
                rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());

            }

            boolean repRcEq4001 = true;
            if (rtnCode == FEPReturnCode.Normal) {
                // 6. CheckResponseFromFISC:檢核回應電文是否正確
                rtnCode = fiscBusiness.checkResponseMessage();

                repRcEq4001 = "4001".equals(feptxn.getFeptxnRepRc());
            }

            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                // 7. SendToCBS/ASC: 送主機處理帳務
                if (getnBData().getMsgCtl().getMsgctlCbsFlag() == 1) {
                    this.sendToCBS2();
                }
            }

            // 8. 	更新交易記錄(FEPTXN)
            this.updateTxData();

            // 9. 	組NB回應電文 & 回 NBMsgHandler
            rtnMessage = getFiscBusiness().prepareNBXOResponseData(tota, getLogContext().getChannel());


            // 10. 交易通知 (if need)
            getFiscBusiness().sendToNotify();

            // 11. 	交易結束通知主機(By PCODE)
            if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc())) { // 財金回覆失敗再送
                AATxTYPE = "";
                String AATxRs = "N";
                String AA = "CBTXEND";
                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
                rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE,AATxRs);
            }

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
            String codeToMsg = String.valueOf(rtnCode.getValue());
            FEPChannel channel = FEPChannel.FEP;
            if (rtnCode == CommonReturnCode.Normal) {
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
    private void sendToCBS() throws Exception {
        if ("TS".equals(StringUtils.left(this.feptxn.getFeptxnTxCode(), 2)) && "2521".equals(this.feptxn.getFeptxnPcode())) {
            feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
            AATxTYPE = "0"; // 上CBS查詢、檢核
            String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid1();
            feptxn.setFeptxnCbsTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
            rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
            tota1 = hostAA.getTota();
            if (rtnCode != CommonReturnCode.Normal) {
                // HostResponse無Timeout
                if (feptxn.getFeptxnCbsTimeout() == 0) {
                    // 回前端主機的處理結果
                    feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
                } else { // HostResponseTimeout
                    feptxn.setFeptxnReplyCode("T001"); //存款主機Timeout需給特定代碼讓前端辨識
                    feptxn.setFeptxnErrMsg("存款主機回應逾時");            }
                // GO TO  8    /* 更新交易紀錄 */
            }

        }
    }

    /**
     * 7. SendToCBS/ASC: 送主機處理帳務
     *
     * @throws Exception
     */
    private FEPReturnCode sendToCBS2() throws Exception {
        /* 進主機入扣帳/手續費 */
        AATxTYPE = "1"; // 上CBS入扣帳
        String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
        rtnCode2 = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
        if (rtnCode2 != CommonReturnCode.Normal) {
            if (feptxn.getFeptxnCbsTimeout() == 0) {
                // HostResponse無Timeout，回前端主機的處理結果
                feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
            } else { // HostResponseTimeout
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode2.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
            }
        }

        return rtnCode2;
    }

    /**
     * 8. 更新交易記錄(FEPTXN)
     */
    private void updateTxData() throws Exception {
        if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())) {
            getFeptxn().setFeptxnPending((short) 2);  /*解除 Pending*/
        }
        if (rtnCode == CommonReturnCode.Normal && rtnCode2 == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) { /*+REP*/
            getFeptxn().setFeptxnReplyCode("4001"); /*回覆 ATM正常*/
            getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // A成功
            getFeptxn().setFeptxnConRc(NormalRC.FISC_ATM_OK); /*+CON*/
            if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {
                rtnCode1 = getFiscBusiness().sendConfirmToFISC();
                if (rtnCode1 != CommonReturnCode.Normal) {
                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode1.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                    getLogContext().setReturnCode(rtnCode1);
                    getLogContext().setRemark("SendConfirmToFISC Error");
                    getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendConfirmToFISC"));
                    sendEMS(getLogContext());
                }
            }
            //跨行清算統計
            if (getnBData().getMsgCtl().getMsgctlUpdateAptot() == 1) {
                rtnCode3 = getFiscBusiness().processAptot(false);
                if (rtnCode3 != CommonReturnCode.Normal) {
                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode3.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                    getLogContext().setReturnCode(rtnCode3);
                    getLogContext().setRemark("ProcessAptot Error");
                    getLogContext().setProgramName(StringUtils.join(ProgramName, ".processAptot"));
                    sendEMS(getLogContext());
                }
            }
        } else if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc()) && !NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) { //財金回覆失敗
            feptxn.setFeptxnTxrust("R"); /*Reject-normal*/
            feptxn.setFeptxnReplyCode(getFeptxn().getFeptxnRepRc());
            feptxn.setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(getFeptxn().getFeptxnRepRc(), FEPChannel.FISC));
            //EAI沖正存款主機手續費優惠次數
            AATxTYPE = "4";
            String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid1();
            feptxn.setFeptxnCbsTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
            rtnCode4 = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
            tota = hostAA.getTota();
            if (rtnCode4 != CommonReturnCode.Normal) {
                if (feptxn.getFeptxnCbsTimeout() == 0) {
                    // HostResponse無Timeout，回前端主機的處理結果
                    feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
                } else { // HostResponseTimeout
                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode4.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                }
            }

        } else if (rtnCode2 != CommonReturnCode.Normal) { //主機錯誤
            feptxn.setFeptxnTxrust("R"); /* Reject-normal */
            if (feptxn.getFeptxnCbsTimeout() == 1) { //主機逾時，回-con 給財金
                feptxn.setFeptxnConRc("0601"); /*+CON*/
            } else {
                feptxn.setFeptxnConRc(feptxntcb.getFeptxntcbImsrc4Fisc());
                feptxn.setFeptxnReplyCode(getFeptxn().getFeptxnConRc());
            }
            feptxn.setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(getFeptxn().getFeptxnConRc(), FEPChannel.FISC));
            rtnCode1= getFiscBusiness().sendConfirmToFISC();
            if (rtnCode1 != CommonReturnCode.Normal) {
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode1.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                getLogContext().setReturnCode(rtnCode1);
                getLogContext().setRemark("SendConfirmToFISC Error");
                getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendConfirmToFISC"));
                sendEMS(getLogContext());
            }

            //EAI沖正存款主機手續費優惠次數
            AATxTYPE = "4";
            String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid1();
            feptxn.setFeptxnCbsTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
            rtnCode4 = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
            tota = hostAA.getTota();
            if (rtnCode4 != CommonReturnCode.Normal) {
                if (feptxn.getFeptxnCbsTimeout() == 0) {
                    // HostResponse無Timeout，回前端主機的處理結果
                    feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
                } else { // HostResponseTimeout
                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode4.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                }
            }
        } else if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbOutrtc()) && !"4001".equals(feptxntcb.getFeptxntcbOutrtc())) {
            // 存款主機檢核錯誤
            feptxn.setFeptxnReplyCode(feptxntcb.getFeptxntcbOutrtc());
        } else if (feptxn.getFeptxnFiscTimeout() != null && feptxn.getFeptxnFiscTimeout() == 1 ){ // 財金timeout回覆財金-Con
            feptxn.setFeptxnTxrust("R"); /* Reject-normal */
            feptxn.setFeptxnConRc("0601"); // 交易逾時
            feptxn.setFeptxnReplyCode(getFeptxn().getFeptxnConRc());
            feptxn.setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(getFeptxn().getFeptxnConRc(), FEPChannel.FISC));
            rtnCode1 = getFiscBusiness().sendConfirmToFISC();
            if (rtnCode1 != CommonReturnCode.Normal) {
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode1.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                getLogContext().setReturnCode(rtnCode1);
                getLogContext().setRemark("SendConfirmToFISC Error");
                getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendConfirmToFISC"));
                sendEMS(getLogContext());
            }
        } else {  // FEP錯誤
            if (StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                feptxn.setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP));
            }
        }

        feptxn.setFeptxnMsgflow("A2"); /* ATM Response*/
        if (rtnCode1 != FEPReturnCode.Normal) {
            getFeptxn().setFeptxnAaRc(rtnCode1.getValue());
        } else if (rtnCode2 != FEPReturnCode.Normal) {
            getFeptxn().setFeptxnAaRc(rtnCode2.getValue());
        } else if (rtnCode3 != FEPReturnCode.Normal) {
            getFeptxn().setFeptxnAaRc(rtnCode3.getValue());
        } else if (rtnCode4 != FEPReturnCode.Normal) {
            getFeptxn().setFeptxnAaRc(rtnCode4.getValue());
        } else if (rtnCode != FEPReturnCode.Normal) {
            getFeptxn().setFeptxnAaRc(rtnCode.getValue());
        } else {
            getFeptxn().setFeptxnAaRc(FEPReturnCode.Normal.getValue());
        }
        getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/

        //回寫檔案 (FEPTxn)
        rtnCode4 = getFiscBusiness().updateTxData();
        if (rtnCode4 != FEPReturnCode.Normal) { // Update錯誤
            if (feptxn.getFeptxnAaRc() == FEPReturnCode.Normal.getValue()) {
                feptxn.setFeptxnReplyCode("T452"); //FEPTXNUpdateError
                rtnCode = rtnCode4;
            }
            getLogContext().setReturnCode(rtnCode);
            getLogContext().setRemark("UpdateTxData Error");
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
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
        header.setSYSTEMID("ATM");
        header.setSTATUSCODE("T099");
        header.setSEVERITY("ERROR");
        header.setSTATUSDESC("程式發生例外");
        if(StringUtils.equals(getLogContext().getChannel().getNameS() , FEPChannel.MCH.getNameS()))
            header.setTXNID(nbheader.getTXNID());

        ResStr = XmlUtil.toXML(nbRs);
        return ResStr;
    }
}
