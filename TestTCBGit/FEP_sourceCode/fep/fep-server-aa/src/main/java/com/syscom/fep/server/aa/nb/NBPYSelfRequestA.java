package com.syscom.fep.server.aa.nb;

import com.syscom.fep.base.aa.NBData;
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

import static com.syscom.fep.base.enums.FEPReturnCode.HostResponseTimeout;

/**
 * @author
 */
public class NBPYSelfRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode3 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode_correction = FEPReturnCode.Normal;

    private FEPReturnCode rtnCode_error = FEPReturnCode.Normal;
    private String AATxTYPE = "";
    private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);


    public NBPYSelfRequestA(NBData txnData) throws Exception {
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
                rtnCode = getFiscBusiness().prepareFEPTXNTCB();
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

            Boolean firstTocbs = false;
            if (rtnCode == FEPReturnCode.Normal) {
                // 4. SendToCBS/ASC: 送主機檢核帳戶資料
                try {
                    this.sendToCBS();
                }catch (Exception e){
                    rtnCode = HostResponseTimeout;
                }
                firstTocbs = true;
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

            // 7. ProcessAPTOT:更新跨行代收付
            if (1 == getnBData().getMsgCtl().getMsgctlUpdateAptot()&& repRcEq4001) {
                rtnCode = fiscBusiness.processAptot(false);
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }

            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                // 8. SendToCBS/ASC: 送主機檢核帳戶資料
                if(getnBData().getMsgCtl().getMsgctlCbsFlag() == 1) {
                    this.sendToCBS2();
                    if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                        rtnCode_error = rtnCode;
                    }
                    if (rtnCode == FEPReturnCode.Normal && rtnCode2 == FEPReturnCode.Normal && repRcEq4001 && "000".equals(feptxn.getFeptxnCbsRc()) && "1".equals(feptxn.getFeptxnAccType().toString())) {
                        if (1 == getnBData().getMsgCtl().getMsgctlUpdateAptot()&& repRcEq4001) {
                            /* 沖回跨行代收付(APTOT) */
                            rtnCode2 = fiscBusiness.processAptot(true);
                        }
                    }
                }
            }

            // 9. label_END_OF_FUNC :判斷是否需組 CON 電文回財金
            this.labelEndOfFunc();

            //10. 	更新交易記錄(FEPTXN)
            this.updateTxData();

//			11. 	組NB回應電文 & 回 NBMsgHandler
            rtnMessage = getFiscBusiness().prepareNBResponseData(tota, getLogContext().getChannel());

            //2025/5/21	SendToCBS/ASC(if need):送往CBS主機處理/進帳務主機(入扣帳手續費) TimeOut沖正
            if(FEPReturnCode.HostResponseTimeout.equals(rtnCode_correction)
                    || FEPReturnCode.ReceiverBankOperationStop.equals(rtnCode_correction)
                    || FEPReturnCode.SenderBankServiceStop.equals(rtnCode_correction)){
                if("NAM".equals(feptxn.getFeptxnChannel())) {
                    AATxTYPE = "2"; //上CBS沖正
                    String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
                    feptxn.setFeptxnCbsTxCode(AA);
                    ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
                    rtnCode = new CBS(hostAA, getnBData()).sendToCBSForNoCheckCBS(AATxTYPE);
                    tota = hostAA.getTota();
                }
            }
            if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                rtnCode_error = rtnCode;
            }
            // 12. 交易通知 (if need)
            getFiscBusiness().sendToNotify();

            //13. 交易結束通知主機(By PCODE)
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
    private void addTxData() throws Exception {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // 新增交易記錄(FEPTxn) Returning FEPReturnCode
            /* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
//			int insertCount = feptxnDao.insertSelective(this.feptxn); // 新增資料
//			if (insertCount <= 0) { // 新增失敗
//				rtnCode = FEPReturnCode.FEPTXNInsertError;
//			}
            rtnCode = getFiscBusiness().insertFEPTxn();
            rtnCode2 = getFiscBusiness().insertFEPTXNTCB();
            if (rtnCode != FEPReturnCode.Normal || rtnCode2 != FEPReturnCode.Normal) { // 更新失敗
                throw new Exception(); //2025.07.15 Transaction call review 調整
            }
            transactionManager.commit(txStatus);
            this.logContext.setRemark("transaction sucess.");
            logMessage(Level.INFO, logContext);
        } catch (Exception ex) { // 新增失敗
            transactionManager.rollback(txStatus);
            this.logContext.setRemark("transaction fail.");
            logMessage(Level.INFO, logContext);
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
        feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
        AATxTYPE = "0"; // 上CBS查詢、檢核
        String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
        rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
        String charge = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE.getValue());
        String brch = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_BRCH.getValue());
        String chargeFlag = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE_FLAG.getValue());
        if (StringUtils.isNotBlank(charge)) {
            BigDecimal hostCharge = new BigDecimal(charge);
            getnBData().setCharge(hostCharge);
        }
        getnBData().setBrch(brch);
        getnBData().setChargeFlag(chargeFlag);
    }

    /**
     * 8. SendToCBS/ASC: 送主機檢核帳戶資料
     *
     * @throws Exception
     */
    private void sendToCBS2() throws Exception {
        feptxn.setFeptxnTxrust("S");

        /* 進主機入扣帳/手續費 */
        AATxTYPE = "1"; // 上CBS入扣帳

        String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
        rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
        String charge = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE.getValue());
        String brch = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_BRCH.getValue());
        String chargeFlag = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE_FLAG.getValue());
        if (StringUtils.isNotBlank(charge)) {
            BigDecimal hostCharge = new BigDecimal(charge);
            getnBData().setCharge(hostCharge);
        }
        getnBData().setBrch(brch);
        getnBData().setChargeFlag(chargeFlag);
    }

    /**
     * 9. label_END_OF_FUNC :判斷是否需組 CON 電文回財金
     *
     * @throws Exception
     */
    private FEPReturnCode labelEndOfFunc() throws Exception {
        try {
            if (rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
                if (DbHelper.toBoolean(getnBData().getMsgCtl().getMsgctlAtm2way())) {
                    getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                    getFeptxn().setFeptxnReplyCode("4001");/*回覆 ATM正常*/
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // 成功
                    getFeptxn().setFeptxnConRc(NormalRC.FISC_ATM_OK);
                    if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {
                        rtnCode3 = getFiscBusiness().sendConfirmToFISC();
                    }
                } else {
                    /* for代理提款-ATM_3way 交易 */
                    feptxn.setFeptxnTxrust("B"); /* PENDING */
                    getFeptxn().setFeptxnReplyCode("4001");/*回覆 ATM正常*/
                }
            } else {
                getLogContext().setProgramName(ProgramName);
                // 交易失敗
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())) {
                    getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                    if (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) { // +REP
                        if(StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) {
                            feptxn.setFeptxnConRc(this.getImsPropertiesValue(tota,ImsMethodName.IMSRC4_FISC.getValue()));
                        }else {
                            feptxn.setFeptxnConRc(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
                                    FEPChannel.FEP, FEPChannel.FISC, getnBData().getLogContext()));
                        }
                        feptxn.setFeptxnErrMsg(getLogContext().getResponseMessage().length()>256?getLogContext().getResponseMessage().substring(0, 256):getLogContext().getResponseMessage());
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
                        if ( "10203".equals(String.valueOf(rtnCode.getValue())) ) {
                            String replyCode = msgfileExtMapper.selectByPrimaryKey(7, String.valueOf(rtnCode.getValue())).getMsgfileAtm();
                            if (replyCode == null || StringUtils.isBlank(replyCode)) {
                                replyCode = AbnormalRC.ATM_Error;
                            }

                            feptxn.setFeptxnReplyCode(replyCode);
                        } else {
                            getFeptxn().setFeptxnReplyCode(getFeptxn().getFeptxnRepRc());
                        }
                        feptxn.setFeptxnErrMsg(getLogContext().getResponseMessage().length()>256?getLogContext().getResponseMessage().substring(0, 256):getLogContext().getResponseMessage());

                    }
                } else { // fepReturnCode <> Normal
                    getLogContext().setRemark(StringUtils.join("FepTxn.FEPTXN_REPLY_CODE before:", getFeptxn().getFeptxnReplyCode()));
                    logMessage(Level.DEBUG, getLogContext());
                    if (StringUtils.isNotBlank(getFeptxn().getFeptxnCbsRc())) {
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
                    } else {
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
                    }
                    getLogContext().setRemark(StringUtils.join("FepTxn.FEPTXN_REPLY_CODE after:", getFeptxn().getFeptxnReplyCode()));
                    logMessage(Level.DEBUG, getLogContext());
                    feptxn.setFeptxnErrMsg(getLogContext().getResponseMessage().length()>256?getLogContext().getResponseMessage().substring(0, 256):getLogContext().getResponseMessage());
                }
                getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
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
     * 10. 更新交易記錄(FEPTXN)
     */
    private void updateTxData() {
        String replyCode = " ";
        if ( "4001".equals(feptxn.getFeptxnRepRc()) || StringUtils.isBlank(feptxn.getFeptxnRepRc()) ){
            if (rtnCode != FEPReturnCode.Normal) {
                List<Msgfile> msgfileList = msgfileExtMapper.selectByMsgfileErrorcode(String.valueOf(rtnCode.getValue()));
                if(msgfileList.size()!=0){
                    feptxn.setFeptxnErrMsg(msgfileList.get(0).getMsgfileShortmsg());
                }
            }
            String rtn = "";
            String FEP_IMS_RC3_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC3_TCB.getValue());
            String FEP_IMS_RC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC4_FISC.getValue());

            if (FEP_IMS_RC4_FISC == null || FEP_IMS_RC4_FISC.trim().equals("") ){
                rtn = FEP_IMS_RC3_TCB;
            }else{
                rtn = FEP_IMS_RC4_FISC;
            }

            if (rtn.length() == 4 || rtn.length() == 3 ){
                replyCode = rtn;
            }else {
                if(rtnCode == FEPReturnCode.Normal){
                    replyCode = "4001";
                }else {
                    // 處理內部錯誤代碼轉換
                    replyCode = msgfileExtMapper.selectByPrimaryKey(7,String.valueOf(rtnCode.getValue())).getMsgfileAtm();
                }
                if (replyCode==null || StringUtils.isBlank(replyCode)){
                    replyCode = AbnormalRC.ATM_Error;
                }
            }

        } else {
            // RepRc 有值且不是 4001 時
            replyCode = feptxn.getFeptxnRepRc();
        }
        getFeptxn().setFeptxnReplyCode(replyCode);

        if (rtnCode != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode.getValue());
        } else if (rtnCode2 != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode2.getValue());
        } else if (rtnCode3 != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode3.getValue());
        }
        else {
            feptxn.setFeptxnAaRc(FEPReturnCode.Normal.getValue());
        }
        feptxn.setFeptxnAaComplete((short) 1); /* AA Close */
        feptxn.setFeptxnMsgflow("A2"); /* ATM Response*/
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
     * 13. 交易結束通知主機(By PCODE)
     */
    public void transactionCloseConnect() {
        try {
            String AATxTYPE = ""; //不需提供此值
            String AATxRs = "N";//不需等待主機回應
            String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid1();
            feptxn.setFeptxnCbsTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
            rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE, AATxRs);
            if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                rtnCode_error = rtnCode;
            }
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
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
