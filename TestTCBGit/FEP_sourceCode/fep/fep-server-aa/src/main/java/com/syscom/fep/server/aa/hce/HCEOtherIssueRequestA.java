package com.syscom.fep.server.aa.hce;

import com.syscom.fep.base.aa.HCEData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS;
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
public class HCEOtherIssueRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode1 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode3 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode4 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode_error = FEPReturnCode.Normal;
    private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);

    int fepfail = 0;  //預設FEP處理成功
    int fiscfail = 0; //預設FISC處理成功
    int cbsfail = 0;  //預設CBS處理成功
    int cbsqryfail = 0;  //預設CBS查詢處理成功

    public HCEOtherIssueRequestA(HCEData txnData) throws Exception {
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
            getFiscBusiness().setmHCEtxData(getmHCEtxData());

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
                    getLogContext().setMessage("PREPARE FEPTXNTCB ERROR");
                    sendEMS(getLogContext());
                    rtnCode_error = rtnCode;
                }
//                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
//                    rtnCode_error = rtnCode;
//                }
            }

            // 2. AddTxData: 新增交易記錄(FEPTXN)
            if (rtnCode == FEPReturnCode.Normal) {
                addTxData();
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }

            // 3. CheckBusinessRule: 商業邏輯檢核
            if (rtnCode == FEPReturnCode.Normal) {
                RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
                rtnCode = getFiscBusiness().checkRequestFromOtherChannel(this.getmHCEtxData(), tita.getINTIME());
                if (rtnCode != FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                    fepfail = 1; // FEP有誤
                    /*GO TO  8 更新交易紀錄 */
                }
            }

            // 4. SendToCBS/ASC(if need): 本行轉入-進帳務主機查詢帳號
            if (rtnCode == FEPReturnCode.Normal
                    && (StringUtils.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno()))
                    || (StringUtils.equals(feptxn.getFeptxnTrinBkno(), SysStatus.getPropertyValue().getSysstatHbkno()))) {
                // 轉出方或轉入方為本行時,先送CBS查詢帳號
                this.sendToCBS();
                if (rtnCode != FEPReturnCode.Normal) {
                    cbsqryfail = 1; // CBS查詢有誤
                    Short timeout = feptxn.getFeptxnCbsTimeout();
                    // HostResponse無Timeout
                    if (timeout != null && timeout == 0) {
                        // 回前端主機的處理結果
                        feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
                    } else { // HostResponseTimeout
                        feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                    }
                    rtnCode_error = rtnCode;
                    /*GO TO  8 更新交易紀錄 */
                }
            }

            // 5. 組送往 FISC 之 Request 電文並等待財金之 Response
            FISC fiscBusiness = getFiscBusiness();
            if (rtnCode == FEPReturnCode.Normal) {
                rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());

                if (rtnCode != FEPReturnCode.Normal){
                    fepfail = 1; // FEP傳送FISC電文有誤
                    rtnCode_error = rtnCode;
                    /*GO TO  8 更新交易紀錄 */
                }

                // 6. CheckResponseFromFISC:檢核回應電文是否正確
                RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
                rtnCode = fiscBusiness.checkResponseMessageForHce();
                if (rtnCode != FEPReturnCode.Normal) {
                    fepfail = 1; // FEP檢核FISC電文有誤
                } else if (!"4001".equals(feptxn.getFeptxnRepRc())) {
                    fiscfail = 1; // FISC回覆錯誤
                }
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }

            // 2026.2.4 LeYun 修正財金錯誤後仍送主機問題
            if (fepfail == 0 && fiscfail == 0) {
                // 7. SendToCBS/ASC(if need): 進帳務主機入扣帳/手續費
                /* CBSProcess_ABTREAI001(組送CBS 主機代理轉帳第二道扣帳交易電文).doc*/
                Short cbsflag = getTxData().getMsgCtl().getMsgctlCbsFlag();
                if (rtnCode == FEPReturnCode.Normal && cbsflag != null && cbsflag == 1) {
                    this.sendToCBS2();
                    if (rtnCode2 != FEPReturnCode.Normal) {
                        cbsfail = 1; // CBS有誤
                        Short timeout = feptxn.getFeptxnCbsTimeout();
                        if (timeout != null && timeout == 0) {
                            // HostResponse無Timeout，回前端主機的處理結果
                            feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
                        } else { // HostResponseTimeout
                            feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode2.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                        }
                        rtnCode_error = rtnCode;
                        /*GO TO  8 更新交易紀錄 */
                    }
                }
            }


            // 8. 更新交易記錄(FEPTXN)
            this.updateTxData();
            //主機入扣帳逾時不回覆前端
            if (cbsfail == 1 && feptxn.getFeptxnCbsTimeout() == 1) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                getLogContext().setProgramName(ProgramName + ".updateTxData");
                getLogContext().setMessage("CBS TIMEOUT.");
                sendEMS(getLogContext());
                return rtnMessage; // 回空字串，不回覆ATM
            }

            // 9. 組HCE回應電文 & 回 HCEMsgHandler
            rtnMessage = getFiscBusiness().prepareHCEResponseData(tota);

            // 10. 交易通知 (if need)
            getFiscBusiness().sendToNotify();

            // 11. 交易結束通知主機 (By PCODE)
            this.transactionCloseConnect();


            // 12. 寫入傳送授權結果通知訊息初始資料 INBK2160 (if need)
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
                rtnCode_error = rtnCode;
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
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
            getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getTxData().getLogContext().setMessage(rtnMessage);
            getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
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
     * 4. SendToCBS/ASC(if need): 本行轉入-進帳務主機查詢帳號
     *
     * @throws Exception
     */
    private void sendToCBS() throws Exception {
        feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
        String AATxTYPE = "0"; // 上CBS查詢、檢核
        String AA = getmHCEtxData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmHCEtxData());
        rtnCode = new CBS(hostAA, getmHCEtxData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
    }

    /**
     * 7. SendToCBS/ASC(if need): 進帳務主機入扣帳/手續費
     * 0:查詢,1:入扣帳,2:沖正,3:授權,4:沖正手續費優惠次數,5:註記,6:確認
     * @throws Exception
     */
    private void sendToCBS2() throws Exception {
        String AATxTYPE = "1"; // 上CBS入扣帳
        String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmHCEtxData());
        rtnCode2 = new CBS(hostAA, getmHCEtxData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
    }


    /**
     * 8. 更新交易記錄(FEPTXN)
     */
    private void updateTxData() {
        if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
            feptxn.setFeptxnPending((short)2);  /*解除 Pending*/
        }

        if (rtnCode == FEPReturnCode.Normal && fepfail == 0 && cbsqryfail == 0 && fiscfail == 0 && cbsfail == 0) {
            feptxn.setFeptxnReplyCode("4001");
            feptxn.setFeptxnTxrust("A");
            feptxn.setFeptxnConRc("4001");
            if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {
                rtnCode3 = getFiscBusiness().sendConfirmToFISC();
                if (rtnCode3 != FEPReturnCode.Normal) {
                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode3.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                    // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                    getLogContext().setProgramName(ProgramName + ".updateTxData");
                    getLogContext().setMessage("UPDATE TXDATE ERROR");
                    sendEMS(getLogContext());
                }
            } else { //for 測試pending交易的控制
                feptxn.setFeptxnWay((short)3); // 配合人工補confirm 條件
            }

            //跨行清算統計
            Short updateAptot= getTxData().getMsgCtl().getMsgctlUpdateAptot();
            if (updateAptot != null && updateAptot == 1) {
                rtnCode2 = getFiscBusiness().processAptot(false);
                if (rtnCode2 != FEPReturnCode.Normal) {
                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode2.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                    // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                    getLogContext().setProgramName(ProgramName + ".updateTxData");
                    getLogContext().setMessage("UPDATE TXDATE ERROR");
                    sendEMS(getLogContext());
                }
            }

        } else if (fiscfail == 1) { //財金回覆失敗
            feptxn.setFeptxnTxrust("R"); /*Reject-normal*/
            feptxn.setFeptxnReplyCode(feptxn.getFeptxnRepRc());
            feptxn.setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(feptxn.getFeptxnRepRc(), FEPChannel.FISC));
        } else if (cbsqryfail == 1) { //主機檢核錯誤
            feptxn.setFeptxnReplyCode(feptxntcb.getFeptxntcbImsrc4Fisc());
            feptxn.setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(feptxn.getFeptxnReplyCode(), FEPChannel.FISC));
        } else if (cbsfail == 1) { /*CBS 失敗*/
            feptxn.setFeptxnTxrust("R"); /*Reject-normal*/
            if (feptxn.getFeptxnCbsTimeout() == 1) { //主機逾時，回-con 給財金
                feptxn.setFeptxnConRc("0601");
            } else {
                feptxn.setFeptxnConRc(feptxntcb.getFeptxntcbImsrc4Fisc());
                feptxn.setFeptxnReplyCode(feptxn.getFeptxnConRc());
            }
            feptxn.setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(feptxn.getFeptxnConRc(), FEPChannel.FISC));

            rtnCode3 = getFiscBusiness().sendConfirmToFISC();
            if (rtnCode3 != FEPReturnCode.Normal) {
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode3.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                getLogContext().setProgramName(ProgramName + ".updateTxData");
                getLogContext().setMessage("UPDATE TXDATE ERROR");
                sendEMS(getLogContext());
            }

        } else { //FEP錯誤
            if (feptxn.getFeptxnFiscTimeout() != null) {
                feptxn.setFeptxnTxrust("R"); /* Reject-normal*/
                //回覆財金-Con
                if (feptxn.getFeptxnFiscTimeout() == 1) {
                    feptxn.setFeptxnConRc("0601"); //交易逾時
                } else {
                    feptxn.setFeptxnConRc("0501"); //端末機故障
                }
                feptxn.setFeptxnReplyCode(feptxn.getFeptxnConRc());
                feptxn.setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(feptxn.getFeptxnConRc(), FEPChannel.FISC));
                rtnCode3 = getFiscBusiness().sendConfirmToFISC();
                if (rtnCode3 != FEPReturnCode.Normal) {
                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode3.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                    // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                    getLogContext().setProgramName(ProgramName + ".updateTxData");
                    getLogContext().setMessage("UPDATE TXDATE ERROR");
                    sendEMS(getLogContext());
                }

            } else {
                feptxn.setFeptxnTxrust("S"); /*Reject-abnormal*/
            }
            if (StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
            }
        }

        feptxn.setFeptxnMsgflow("A2"); /* ATM Response*/
        if (rtnCode3 != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode3.getValue());
        } else if (rtnCode2 != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode2.getValue());
        } else if (rtnCode != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode.getValue());
        } else {
            feptxn.setFeptxnAaRc(FEPReturnCode.Normal.getValue());
        }
        feptxn.setFeptxnAaComplete((short) 1); /* AA Close */

        //回寫檔案 (FEPTxn)
        rtnCode4 = updateFeptxn();
        if (rtnCode4 != FEPReturnCode.Normal) { // Update錯誤
            if (feptxn.getFeptxnAaRc() == FEPReturnCode.Normal.getValue()) {
                feptxn.setFeptxnReplyCode("T452"); //FEPTXNUpdateError
                rtnCode = rtnCode4;
            }
            // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
            getLogContext().setProgramName(ProgramName + ".updateTxData");
            getLogContext().setMessage("UPDATE TXDATE ERROR");
            sendEMS(getLogContext());
        }
    }

    /**
     * 更新feptxn
     *
     * @return
     */
    private FEPReturnCode updateFeptxn() {
        try {
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料

            return  FEPReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFeptxn");
            sendEMS(getLogContext());
            return  FEPReturnCode.FEPTXNUpdateError;
        }
    }

    /**
     * 字串指定區間的每個字元皆取代為'*'
     *
     * @param value
     * @param starIndex 開始位置索引
     * @param endIndex  結束位置索引
     * @return
     */
    private String setRangeCharAtStar(String value, int starIndex, int endIndex) {
        StringBuilder sb = new StringBuilder(value);
        for (int i = 9; i <= 11; i++) {
            sb.setCharAt(i, '*');
        }
        return sb.toString();
    }

    /**
     * 13. 交易結束通知主機 (By PCODE)
     */
    private void transactionCloseConnect() {
        try {
            if ((StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc()))
                    && ("2521".equals(feptxn.getFeptxnPcode()) || "2522".equals(feptxn.getFeptxnPcode()))) { // 財金回覆失敗再送
                String AATxTYPE = ""; //不需提供此值
                String AATxRs = "N";  //不需等待主機回應
                String AA = getmHCEtxData().getMsgCtl().getMsgctlTwcbstxid1();
                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmHCEtxData());
                rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE, AATxRs);

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
        header.setSYSTEMID("ATM");
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

        if (tota != null) {
            if (StringUtils.equals("4001", this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue()))) {
                String FEP_IMS_RC3_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC3_TCB.getValue());
                String FEP_IMS_RC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC4_FISC.getValue());
                //CB_IQTX_O001
                if (feptxn.getFeptxnFiscFlag() == 0) {
                    //自行3
                    if (StringUtils.isBlank(FEP_IMS_RC4_FISC)) {
                        if (StringUtils.isBlank(FEP_IMS_RC3_TCB) || "000".equals(FEP_IMS_RC3_TCB.trim())) {
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
        }

        if(StringUtils.isBlank(nbRs.getBody().getRs().getSvcRs().getTCBRTNCODE())){
            if(StringUtils.isBlank(feptxn.getFeptxnReplyCode()) || "0000".equals(feptxn.getFeptxnReplyCode().trim())){
                nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE("T001");
            }else{
                nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxn.getFeptxnReplyCode());
            }
        }

        ResStr = XmlUtil.toXML(nbRs);
        return ResStr;
    }
}