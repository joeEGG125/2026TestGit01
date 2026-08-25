package com.syscom.fep.server.aa.hce;

import com.syscom.fep.base.aa.HCEData;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.model.Msgfile;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
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

/**
 * @author Jaime
 */
public class HCETRSelfIssue extends ATMPAABase {
    private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode_error = FEPReturnCode.Normal;

    public HCETRSelfIssue(HCEData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        try {
            getFeptxn().setFeptxnStan(getATMBusiness().getStan());/*先取 STAN 以供主機電文使用*/
            getLogContext().setStan(getFeptxn().getFeptxnStan());

            // 1. Prepare():記錄MessageText & 準備回覆電文資料
            rtnCode = getATMBusiness().hce_PrepareFEPTxn();
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
                getLogContext().setRemark("PREPARE FEPTXN ERROR");
                sendEMS(getLogContext());
                rtnCode_error = rtnCode;
            }

            if (rtnCode == FEPReturnCode.Normal) {
                rtnCode = getATMBusiness().other_prepareFEPTXNTCB();
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
                // 2. AddTxData: 新增交易記錄(FEPTxn)
                addTxData(); // 新增交易記錄(FEPTxn)
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 3. CheckBusinessRule: 商業邏輯檢核
                RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
                rtnCode = getATMBusiness().checkRequestFromOtherChannel(getmHCEtxData(), tita.getINTIME());
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 4. SendToCBS:送往CBS主機處理
                this.sendToCBS();
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }

            // 5. UpdateTxData: 更新交易記錄(FEPTxn)
            this.updateTxData();

        } catch (Exception ex) {
            RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getHeader();
            rtnMessage = getResHCEStr(atmReqheader);
            rtnCode = FEPReturnCode.ProgramException;
            this.getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {
//            if (StringUtils.isBlank(getmHCEtxData().getTxResponseMessage())) {
//				6. 	組HCE回應電文 & 回 HCEMsgHandler
//			       電文內容格式請參照: SEND_HCE_GeneralTrans_RS 
                rtnMessage = getATMBusiness().prepareHCEResponseData(tota);
//            } else {
//                rtnMessage = getmHCEtxData().getTxResponseMessage();
//            }
            // 7. 交易通知 (if need)
            getATMBusiness().sendToNotify();

            //7.	寫入傳送授權結果通知訊息初始資料 INBK2160 (if need)
            if ("Y".equals(feptxn.getFeptxnSend2160()) && "000".equals(feptxn.getFeptxnCbsRc())) {
                /*Prepare : 寫入傳送授權結果通知訊息初始資料 INBK2160*/
                rtnCode = getATMBusiness().prepareInbk2160();
            } else if ("A".equals(feptxn.getFeptxnSend2160())) {
                /*Prepare : 寫入傳送授權結果通知訊息初始資料 INBK2160*/
                rtnCode = getATMBusiness().prepareInbk2160();
            }
            // else if("000".equals(feptxn.getFeptxnCbsRc()) ) {
            // 	/*Prepare : 寫入傳送授權結果通知訊息初始資料 INBK2160*/
            // 	/*該判斷僅在開發套測試，佈版至其它套需拿掉*/
            // 	LogData logData = new LogData();
            // 	logData.setProgramFlowType(ProgramFlow.AAOut);
            // 	logData.setMessageFlowType(MessageFlow.Response);
            // 	logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            // 	logData.setMessage("Do INBK2160 Strat");
            // 	logMessage(Level.DEBUG, logData);
            //
            // 	rtnCode = getATMBusiness().prepareInbk2160();
            //
            // 	logData.setProgramFlowType(ProgramFlow.AAOut);
            // 	logData.setMessageFlowType(MessageFlow.Response);
            // 	logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            // 	logData.setMessage("Do INBK2160 End");
            // 	logMessage(Level.DEBUG, logData);
            // }
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                sendEMS(getLogContext());
                if (rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }
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
     * 4. SendToCBS:送往CBS主機處理
     *
     * @throws Exception
     */
    private void sendToCBS() throws Exception {

        /* 交易記帳處理 */
        String AATxTYPE = "1"; // 上CBS入扣帳
        String AA = getmHCEtxData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmHCEtxData());
        rtnCode = new CBS(hostAA, getmHCEtxData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
    }

    /**
     * 5. UpdateTxData: 更新交易記錄(FEPTxn)
     */
    private void updateTxData() {
        feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
        if (rtnCode != FEPReturnCode.Normal) {
            String replyCode = " ";
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
                replyCode = msgfileExtMapper.selectByPrimaryKey(7,String.valueOf(rtnCode.getValue())).getMsgfileAtm();
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

        feptxn.setFeptxnAaRc(rtnCode.getValue());
        feptxn.setFeptxnAaComplete((short) 1); /* AA Complete */
        /* For報表, 寫入處理結果 */
        if (rtnCode == FEPReturnCode.Normal) {
            if (feptxn.getFeptxnWay() == 3) {
                feptxn.setFeptxnTxrust("B"); /* 處理結果=Pending */
            } else {
                feptxn.setFeptxnTxrust("A"); /* 處理結果=成功 */
            }
        } else {
            feptxn.setFeptxnTxrust("R"); /* 處理結果=Reject */
        }

        // 回寫 FEPTXN
        /* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
        FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
        try {
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            String tel = null;
            if (this.feptxn.getFeptxnTelephone() != null) {
                tel = this.feptxn.getFeptxnTelephone().substring(this.feptxn.getFeptxnTelephone().length() - 10);
            }
            this.feptxn.setFeptxnTelephone(tel);
            feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
        } catch (Exception ex) {
            rtnCode2 = FEPReturnCode.FEPTXNUpdateError;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateTxData");
            sendEMS(getLogContext());
        }

        if (rtnCode2 != FEPReturnCode.Normal) {
            // 回寫檔案 (FEPTxn) 發生錯誤
            this.feptxn.setFeptxnReplyCode("T452");
            sendEMS(getLogContext());
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
