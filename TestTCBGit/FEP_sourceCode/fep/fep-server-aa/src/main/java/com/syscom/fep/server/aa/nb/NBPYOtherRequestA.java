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
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.NpsdtlExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.model.Npsdtl;
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
 * @author Jaime
 */
public class NBPYOtherRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode1 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode3 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode4 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode_error = FEPReturnCode.Normal;
    private NpsunitExtMapper dbNPSUNIT = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);
    private NpsdtlExtMapper npsdtlExtMapper = SpringBeanFactoryUtil.getBean(NpsdtlExtMapper.class);

    private String AATxTYPE = "";
    public NBPYOtherRequestA(NBData txnData) throws Exception {
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
            getFeptxn().setFeptxnStan(getFiscBusiness().getStan()); /* 先取 STAN 以供主機電文使用 */
            getLogContext().setStan(getFeptxn().getFeptxnStan());

            // 1. Prepare : 交易記錄初始資料
            rtnCode = getFiscBusiness().nb_PrepareFEPTxn();
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
                getLogContext().setRemark("PREPARE FEPTXN ERROR");
                sendEMS(getLogContext());
                rtnMessage = getResNBStr(atmReqheader);
                return rtnMessage; // EXIT PROGRAM
            }

            rtnCode = getFiscBusiness().other_prepareFEPTXNTCB();
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                getLogContext().setProgramName(ProgramName + ".prepareFEPTXNTCB");
                getLogContext().setRemark("PREPARE FEPTXNTCB ERROR");
                sendEMS(getLogContext());
                rtnMessage = getResNBStr(atmReqheader);
                return rtnMessage; // EXIT PROGRAM
            }

            // 2. AddTxData: 新增交易記錄(FEPTXN)
            this.addTxData();
            if (rtnCode != FEPReturnCode.Normal) {
                rtnMessage = getResNBStr(atmReqheader);
                return rtnMessage; // EXIT PROGRAM
            }

            // 3. CheckBusinessRule: 商業邏輯檢核
            if (rtnCode == FEPReturnCode.Normal) {
                RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = getnBData().getTxNbfepObject().getRequest().getBody().getRq().getSvcRq();
                rtnCode = getFiscBusiness().checkRequestFromOtherChannel(getnBData(), tita.getINTIME());
                if (rtnCode != FEPReturnCode.Normal) {
                    feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
                    if (rtnCode_error == FEPReturnCode.Normal) {
                        rtnCode_error = rtnCode;
                    }
                    // GO TO  8  /* 更新交易紀錄 */
                }
            }

            boolean firstTocbs = false;
            // 4. SendToCBS/ASC(if need): 送往CBS主機處理/進帳務主機(查詢帳號)
            if (rtnCode == FEPReturnCode.Normal) {
                this.sendToCBS();
                firstTocbs = true;
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                    // GO TO  8    /* 更新交易紀錄 */
                }
            }

            // 5. 組送往 FISC 之 Request 電文並等待財金之 Response 電文
            FISC fiscBusiness = getFiscBusiness();
            if (rtnCode == FEPReturnCode.Normal) {
                rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                    // GO TO  8    /* 更新交易紀錄 */
                }
            }

            // 6. CheckResponseFromFISC:檢核回應電文是否正確
            boolean repRcEq4001 = false;
            if (rtnCode == FEPReturnCode.Normal) {
                rtnCode = fiscBusiness.checkResponseMessage();
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                    // GO TO  8    /* 更新交易紀錄 */
                }
                repRcEq4001 = "4001".equals(feptxn.getFeptxnRepRc());
            }

            // 7. SendToCBS/ASC(if need): 送往CBS主機處理/進帳務主機(入扣帳手續費)
            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                this.sendToCBS2();
                if (rtnCode2 != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode2;
                }
            }

            // 8. 更新交易記錄(FEPTXN)
            this.updateTxData();

            // 9. 組NB回應電文 & 回 NBMsgHandler
            rtnMessage = getFiscBusiness().prepareNBPYResponseData(tota, getLogContext().getChannel(), AATxTYPE);

            // 10. SendToCBS/ASC(if need):送往CBS主機處理/進帳務主機(入扣帳手續費) TimeOut沖正

            // 11. 交易通知 (if need)
            getFiscBusiness().sendToNotify();

            // 12. 交易結束通知主機(By PCODE)
            //判斷第一道是否有送 如果沒送第一道不須回傳END
            if (firstTocbs){
                this.transactionCloseConnect();
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
        return rtnMessage.replace("&#xf;", "     ");
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
            FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
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
     * 4. SendToCBS/ASC(if need): 送往CBS主機處理/進帳務主機(查詢帳號)
     */
    private void sendToCBS() throws Exception {
        try {
            String etflag = "N"; //非繳費移轉計畫
            if (StringUtils.equals(feptxn.getFeptxnBusinessUnit(), "18888888")
                    && StringUtils.equals(feptxn.getFeptxnPaytype(), "59999")
                    && StringUtils.equals(feptxn.getFeptxnPayno(), "9999")
                    && StringUtils.equals(feptxn.getFeptxnDueDate(), "99991231")) {
                etflag = "Y"; //繳費移轉計畫(轉帳三萬元限制)
            }

            if (StringUtils.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())
                    || StringUtils.equals(feptxn.getFeptxnTrinBkno(), SysStatus.getPropertyValue().getSysstatHbkno())
                    || "N".equals(etflag)
                    || "NAM".equals(feptxn.getFeptxnChannel())) {
                // 轉出方或轉入方為本行時,先送CBS查詢帳號
                // NPAY 交易(2263/2264需檢核帳務代理行相關訊息)
                feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
                AATxTYPE = "0"; // 上CBS查詢、檢核

                String AA;
                //繳汽燃稅存款檢核(ABACCI01)
                if (StringUtils.equals(feptxn.getFeptxnPcode(), "2561")
                        && StringUtils.startsWith(feptxn.getFeptxnTxCode(), "EF")) {
                    AA = getnBData().getMsgCtl().getMsgctlTwcbstxid1();
                } else { //其他交易近主機檢核(ABPYEAII001)
                    AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
                }

                feptxn.setFeptxnCbsTxCode(AA);
                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
                rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
                tota = hostAA.getTota();

                if (rtnCode != FEPReturnCode.Normal) {
                    if (feptxn.getFeptxnCbsTimeout() == 0) { // HostResponse無Timeout
                        // 回前端主機的處理結果
                        feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
                    } else { // HostResponseTimeout
                        if ("2561".equals(feptxn.getFeptxnPcode()) && "EF".equals(feptxn.getFeptxnTxCode().substring(0, 2))) {
                            //繳汽燃稅存款檢核(ABACCI01)，存款主機Timeout需給特定代碼讓前端辨識
                            feptxn.setFeptxnReplyCode("T001");
                            feptxn.setFeptxnErrMsg("存款主機回應逾時");
                        } else {
                            feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                        }
                    }
                    // GO TO  8    /* 更新交易紀錄 */
                }
            }
        } catch (Exception ex) {
            rtnCode = FEPReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendToCBS");
            sendEMS(getLogContext());
        }
    }

    /**
     * 7. SendToCBS/ASC(if need): 進帳務主機入扣帳/手續費
     *
     * @throws Exception
     */
    private void sendToCBS2() throws Exception {
        try {
            if (DbHelper.toBoolean(getnBData().getMsgCtl().getMsgctlCbsFlag())) {
                RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = getnBData().getTxNbfepObject().getRequest().getBody().getRq().getHeader();
                /* 進主機入扣帳/手續費 */
                AATxTYPE = "1"; // 上CBS入扣帳
                String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
                feptxn.setFeptxnCbsTxCode(AA);
                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
                rtnCode2 = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
                tota = hostAA.getTota();
                if (rtnCode2 != FEPReturnCode.Normal) {
                    if (feptxn.getFeptxnCbsTimeout() == 0) {
                        // HostResponse無Timeout，回前端主機的處理結果
                        feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
                    } else { // HostResponseTimeout
                        feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode2.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                        if ("NAM".equals(feptxn.getFeptxnChannel())) { //整批轉即時
                            AATxTYPE = "2"; //主機Timeout，再送一次電文讓主機沖正
                            AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
                            feptxn.setFeptxnCbsTxCode(AA);
                            feptxn.setFeptxnCbsRc("2999");
                            feptxn.setFeptxnFeeCustpay(BigDecimal.valueOf(0));
                            feptxn.setFeptxnFeeCustpayAct(BigDecimal.valueOf(0));
                            hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
                            rtnCode4 = new CBS(hostAA, getnBData()).sendToCBSForNoCheckCBS(AATxTYPE);
                            tota = hostAA.getTota();
                            if (rtnCode4 != FEPReturnCode.Normal) {
                                getLogContext().setReturnCode(rtnCode4);
                                getLogContext().setRemark("NAM整批轉即時交易逾時，送主機沖正失敗");
                                getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToCBS2"));
                                sendEMS(getLogContext());
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            rtnCode2 = FEPReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendToCBS2");
            sendEMS(getLogContext());
        }
    }

    /**
     * 8. 更新交易記錄(FEPTXN)
     *
     */
    private void updateTxData() {
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
        } else if (rtnCode2 != CommonReturnCode.Normal) { //主機扣帳錯誤
            feptxn.setFeptxnTxrust("R"); /* Reject-normal */
            if (feptxn.getFeptxnCbsTimeout() == 1) { //主機逾時，回-con 給財金
                if ( "2".equals(AATxTYPE) ) { //整批轉即時主機逾時沖正
                    feptxn.setFeptxnConRc(feptxn.getFeptxnCbsRc());
                } else {
                    feptxn.setFeptxnConRc("0601"); /*+CON*/
                }
            } else {
                feptxn.setFeptxnConRc(feptxntcb.getFeptxntcbImsrc4Fisc());
            }
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
        } else if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbOutrtc()) && !"4001".equals(feptxntcb.getFeptxntcbOutrtc())) {
            //汽燃費存款主機檢核錯誤
            feptxn.setFeptxnReplyCode(feptxntcb.getFeptxntcbOutrtc());
        } else if (feptxn.getFeptxnFiscTimeout() != null && feptxn.getFeptxnFiscTimeout() == 1){
            //財金timeout回覆財金-Con
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

        // 20260420 整批轉即時交易更新NPSDTL
        if ("NAM".equals(feptxn.getFeptxnChannel())) {
            rtnCode2 = updateNpsdtl();
            if (rtnCode2 != FEPReturnCode.Normal) { // Update錯誤
                feptxn.setFeptxnReplyCode("T452"); //FEPTXNUpdateError
                rtnCode = rtnCode2;
                getLogContext().setReturnCode(rtnCode);
                getLogContext().setRemark("updateNpsdtl Error");
                getLogContext().setProgramName(StringUtils.join(ProgramName, ".updateNpsdtl"));
                sendEMS(getLogContext());
            }
        }


    }

    /**
     * 12. 交易結束通知主機(By PCODE)
     */
    public void transactionCloseConnect() {
        try {
            //1.財金REP失敗需傳送END通知
            //2.NPAY交易主機timeout會送沖正電文，成功交易結束需傳送通知
            if ((StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc()))
                || (StringUtils.isNotBlank(feptxn.getFeptxnConRc()) && !"4001".equals(feptxn.getFeptxnConRc()))
                || ("NAM".equals(feptxn.getFeptxnChannel()) && "A".equals(feptxn.getFeptxnTxrust()))) {
                // 交易失敗
                String AATxTYPE = ""; // 不需提供此值
                String AATxRs = "N"; // 不需等待主機回應
                String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid1();
                feptxn.setFeptxnCbsTxCode(AA);
                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
                rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE, AATxRs);
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".transactionCloseConnect"));
            sendEMS(this.logContext);
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
        header.setSTATUSDESC("發生exception");
        if (StringUtils.equals(getLogContext().getChannel().getNameS(), FEPChannel.MCH.getNameS()))
            header.setTXNID(nbheader.getTXNID());

        ResStr = XmlUtil.toXML(nbRs);
        return ResStr;
    }

    /**
     * 整批轉即時交易更新NPSDTL
     */
    public FEPReturnCode updateNpsdtl() {
        String BatNo = feptxn.getFeptxnChannelEjfno().substring(5, 34);
        int seqNo = Integer.parseInt(feptxn.getFeptxnChannelEjfno().substring(34, 44));
        Npsdtl npsdtl = npsdtlExtMapper.selectByPrimaryKey(BatNo,seqNo);
        if (npsdtl == null){
            getLogContext().setRemark("updateNPSDTL時，找不到原交易的NPSDTL資料");
            getLogContext().setProgramName(ProgramName + ".updateNPSDTL");
            sendEMS(getLogContext());
            return FEPReturnCode.UpdateFail;
        }
        try {
            //NPAY交易更新 NPSDTL
            if ("A".equals(feptxn.getFeptxnTxrust())) {
                npsdtl.setNpsdtlResult("00");
            } else {
                npsdtl.setNpsdtlResult("01");
            }
            npsdtl.setNpsdtlTbsdy(feptxn.getFeptxnTbsdy());
            //交易結果
            if ( !"A".equals(feptxn.getFeptxnTxrust())) {
                if ( feptxn.getFeptxnConRc()!=null && !"4001".equals(feptxn.getFeptxnConRc()) ) {
                    npsdtl.setNpsdtlReplyCode(feptxn.getFeptxnConRc());
                }else if( feptxn.getFeptxnRepRc()!=null && !"4001".equals(feptxn.getFeptxnRepRc()) ){
                    npsdtl.setNpsdtlReplyCode(feptxn.getFeptxnRepRc());
                }else if( feptxntcb.getFeptxntcbImsrc4Fisc()!=null ){
                    npsdtl.setNpsdtlReplyCode(feptxntcb.getFeptxntcbImsrc4Fisc());
                }else {
                    npsdtl.setNpsdtlReplyCode(feptxn.getFeptxnReplyCode());
                }
            }else{
                npsdtl.setNpsdtlReplyCode(feptxntcb.getFeptxntcbImsrc4Fisc());
            }
            npsdtl.setNpsdtlErrMsg(feptxn.getFeptxnErrMsg());
            npsdtl.setNpsdtlEjfno(String.valueOf(feptxn.getFeptxnEjfno()));
            npsdtl.setNpsdtlTbsdyFisc(feptxn.getFeptxnTbsdyFisc());
            npsdtl.setNpsdtlStan(feptxn.getFeptxnStan());
            npsdtl.setNpsdtlFee(feptxn.getFeptxnFeeCustpayAct());
            if (feptxn.getFeptxnFeeCustpayAct() != null) {
                npsdtl.setNpsdtlHostCharge(feptxn.getFeptxnFeeCustpayAct().intValue());
            }
            npsdtl.setNpsdtlTxTime(feptxn.getFeptxnTxTime());
            npsdtl.setNpsdtlHostBrch(feptxntcb.getFeptxntcbPyHostBrch());
            npsdtl.setNpsdtlCbsRc(feptxntcb.getFeptxntcbImsrcTcb());
            npsdtl.setNpsdtlHostChargeFlag(feptxntcb.getFeptxntcbPyChargeFlag());

            if ( "2".equals(AATxTYPE) ) {
                //主機扣帳逾時，FEP已送沖正給主機
                npsdtl.setNpsdtlCbsRc("P02");
                npsdtl.setNpsdtlReplyCode("2999");
                npsdtl.setNpsdtlHostCharge(0); //手續費
            }

            //更NPSDTL
            npsdtl.setNpsdtlBatNo(BatNo);
            npsdtl.setNpsdtlSeqNo(seqNo);

            if (npsdtlExtMapper.updateByPrimaryKeySelective(npsdtl) > 0) {
                return FEPReturnCode.Normal;
            } else {
                return FEPReturnCode.UpdateFail;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateNPSDTL");
            sendEMS(getLogContext());
            return FEPReturnCode.UpdateFail;
        }
    }
}
