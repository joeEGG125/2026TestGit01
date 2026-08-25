package com.syscom.fep.server.aa.hce;

import com.syscom.fep.base.aa.HCEData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Msgfile;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author Jaime
 */
public class HCEInq extends ATMPAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);
    SysconfExtMapper sysconfExtMapper;
    public HCEInq(HCEData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header header = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getHeader();
        try {

            // 1. 記錄文字記錄檔Log (MessageText)
            // Do Nothing

            // 2. CheckBusinessRule: 商業邏輯檢核
            rtnCode = this.checkBusinessRule();
            if (rtnCode == FEPReturnCode.Normal) {
                //goto  5
                if ((short)1 == feptxn.getFeptxnAccType() && "A".equals(feptxn.getFeptxnTxrust())){
                    rtnMessage = this.response();
                    return rtnMessage;
                }
            }else {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                sendEMS(getLogContext());
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 3. 以ATM_TITA相關資料, 寫入 FEPTXN
                this.prepareFEPTXN();
            }

            if (rtnCode == FEPReturnCode.Normal) {
                this.sendToCBS();
            }

            // 4. 組回應電文
            if (StringUtils.isBlank(getmHCEtxData().getTxResponseMessage())) {
                rtnMessage = this.response();
            } else {
                rtnMessage = getmHCEtxData().getTxResponseMessage();
            }

            // 5. UpdateTxData: 更新交易記錄(FEPTxn)
//            this.updateTxData();

        } catch (Exception ex) {
            rtnMessage = getResHCEStr(header);
            rtnCode = FEPReturnCode.ProgramException;
            this.getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {
            logContext.setProgramFlowType(ProgramFlow.AAOut);
            logContext.setMessage(rtnMessage);
            logContext.setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            logContext.setMessageFlowType(MessageFlow.Response);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
            logMessage(Level.DEBUG, this.logContext);
        }
        return rtnMessage;
    }

    /**
     * 1. Prepare():記錄MessageText & 準備回覆電文資料
     *
     * @return
     * @throws Exception
     */
    private FEPReturnCode checkBusinessRule() throws Exception {

        RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header header = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getHeader();
        RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq hcebody = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
        Feptxn dbfeptxn = feptxnDao.getOldFeptxndata(hcebody.getINDATE(), header.getCLIENTTRACEID(), header.getCHANNEL());
        setFeptxn(dbfeptxn);
        getmHCEtxData().setFeptxn(dbfeptxn);
        if (getFeptxn() == null) {
            return FEPReturnCode.FEPTXNNotFound;
        } else {
            int EJ = dbfeptxn.getFeptxnEjfno(); // 暫存當下交易EJ，避免被原交易覆蓋
            feptxn.setFeptxnEjfno(EJ);
            getmHCEtxData().setEj(EJ);
            getLogContext().setEj(EJ);
            if (!hcebody.getTXNTYPE().equals("RQ")
//                    && hcebody.getTERMINALID().equals(getFeptxn().getFeptxnAtmno())
                    && hcebody.getFSCODE().equals(getFeptxn().getFeptxnTxCode().trim())
                    && hcebody.getPCODE().equals(getFeptxn().getFeptxnPcode())
                    && hcebody.getTRNSFROUTBANK().substring(0, 3).equals(getFeptxn().getFeptxnTroutBkno())
                    && hcebody.getTRNSFROUTACCNT().equals(getFeptxn().getFeptxnTroutActno().trim())
                    && hcebody.getTRANSAMT().compareTo(getFeptxn().getFeptxnTxAmt()) == 0
            ) {
                rtnCode = FEPReturnCode.Normal;
            } else {
                rtnCode = FEPReturnCode.FEPTXNNotFound;/* 其他類檢核錯誤 */
            }
//            if(rtnCode != null){
//                if ( "10203".equals(String.valueOf(rtnCode.getValue())) ) {
//                    String replyCode = msgfileExtMapper.selectByPrimaryKey(7, String.valueOf(rtnCode.getValue())).getMsgfileAtm();
//                    if (replyCode == null || StringUtils.isBlank(replyCode)) {
//                        replyCode = AbnormalRC.ATM_Error;
//                    }
//                    feptxn.setFeptxnReplyCode(replyCode);
//                } else {
//                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
//                            FEPChannel.FEP, getmHCEtxData().getTxChannel(), logContext));
//                }
//            }
        }
        return rtnCode;
    }

    /**
     * 3. 以ATM_TITA相關資料, 寫入 FEPTXN
     *
     * @throws Exception
     */
    private void prepareFEPTXN() throws Exception {
        RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();

        feptxn.setFeptxnMsgflow("A3"); //INQ Request
        feptxn.setFeptxnTraceEjfno(getEj());
        feptxn.setFeptxnConTxCode(atmReqbody.getFSCODE() + "C"); //交易代號
        feptxn.setFeptxnConTxTime(atmReqbody.getINTIME()); //交易時間
        feptxn.setFeptxnConExcpCode(atmReqbody.getTXNTYPE()); //交易處理類別

        String traceEjfno = StringUtils.leftPad(String.valueOf(feptxn.getFeptxnTraceEjfno()), 7, "0");
        feptxn.setFeptxnConTxseq(traceEjfno.substring(traceEjfno.length() - 7));

    }


    /**
     * 4. 組回應電文
     *
     * @throws Exception
     */
    private String response() throws Exception {
        String rtnMessage = "";
        try {
            /* 組 ATM Response OUT-TEXT */
            RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getHeader();
            SEND_HCE_GeneralTrans_RS hceRs = new SEND_HCE_GeneralTrans_RS();
            SEND_HCE_GeneralTrans_RS_Body rsbody = new SEND_HCE_GeneralTrans_RS_Body();
            SEND_HCE_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_HCE_GeneralTrans_RS_Body_MsgRs();
            SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs();
            hceRs.setBody(rsbody);
            hceRs.getBody().setRs(msgrs);
            hceRs.getBody().getRs().setHeader(header);
            hceRs.getBody().getRs().setSvcRs(body);

            hceRs.getBody().getRs().getHeader().setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
            hceRs.getBody().getRs().getHeader().setCHANNEL(atmReqheader.getCHANNEL());
            hceRs.getBody().getRs().getHeader().setMSGID(atmReqheader.getMSGID());
            hceRs.getBody().getRs().getHeader().setCLIENTDT(atmReqheader.getCLIENTDT());
            sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
            hceRs.getBody().getRs().getHeader().setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short)1,"NATM_SYSTEMID_ATM").getSysconfValue());
            hceRs.getBody().getRs().getHeader().setSEVERITY("INFO");
            hceRs.getBody().getRs().getHeader().setSTATUSCODE("4001");
            hceRs.getBody().getRs().getHeader().setSTATUSDESC(" ");

            hceRs.getBody().getRs().getSvcRs().setOUTDATE(" ");
            hceRs.getBody().getRs().getSvcRs().setOUTTIME(" ");
            hceRs.getBody().getRs().getSvcRs().setFEP_EJNO(" ");
            hceRs.getBody().getRs().getSvcRs().setTXNSTAN(" ");
            hceRs.getBody().getRs().getSvcRs().setCUSTOMERID(" ");
            hceRs.getBody().getRs().getSvcRs().setTXNTYPE("IQ");
            hceRs.getBody().getRs().getSvcRs().setFSCODE(" ");
            if(feptxn!= null && feptxn.getFeptxnTbsdy()!= null && feptxn.getFeptxnTbsdy().length()>=8){
                hceRs.getBody().getRs().getSvcRs().setACCTDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTbsdy()));
            }else {
                hceRs.getBody().getRs().getSvcRs().setACCTDATE(" ");
            }
            hceRs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(" ");
            hceRs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(" ");
            hceRs.getBody().getRs().getSvcRs().setTRANSAMT(new BigDecimal("0"));
            hceRs.getBody().getRs().getSvcRs().setTRANSFROUTBAL(new BigDecimal("0"));
            hceRs.getBody().getRs().getSvcRs().setTRANSOUTAVBL(new BigDecimal("0"));
            hceRs.getBody().getRs().getSvcRs().setTRNSFROUTBANK(" ");
            hceRs.getBody().getRs().getSvcRs().setTRNSFROUTACCNT(" ");
            hceRs.getBody().getRs().getSvcRs().setCLEANBRANCHOUT(" ");
            hceRs.getBody().getRs().getSvcRs().setTRNSFRINBANK(" ");
            hceRs.getBody().getRs().getSvcRs().setTRNSFRINACCNT(" ");
            hceRs.getBody().getRs().getSvcRs().setCLEANBRANCHIN(" ");
            hceRs.getBody().getRs().getSvcRs().setCUSTPAYFEE(new BigDecimal("0"));
            if (StringUtils.equals("4001", this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue()))) {
                String FEP_IMS_RC3_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC3_TCB.getValue());
                String FEP_IMS_RC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC4_FISC.getValue());
                //CB_IQTX_O001
                if (feptxn.getFeptxnFiscFlag() == 0) {
                    //自行3
                    if ( FEP_IMS_RC4_FISC == null || FEP_IMS_RC4_FISC.trim().equals("") ){
                        if (FEP_IMS_RC3_TCB == null || FEP_IMS_RC3_TCB.trim().equals("") || "000".equals(FEP_IMS_RC3_TCB.trim())){
                            hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE("4001");
                        }else {
                            hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE(FEP_IMS_RC3_TCB);
                        }
                    }else{
                        //自行4
                        hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE(this.getImsPropertiesValue(tota, FEP_IMS_RC4_FISC ));
                    }
                } else {
                    //跨行
                    hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE(this.getImsPropertiesValue(tota, FEP_IMS_RC4_FISC));
                }
            } else {
                //CB_IQTX_O002
                String OUTRTC = this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue());
                hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE(OUTRTC);
            }

            if(feptxn == null){
                hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE("T450");
                hceRs.getBody().getRs().getSvcRs().setHOSTACC_FLAG("N");
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) {
                hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxn.getFeptxnCbsRc());
                hceRs.getBody().getRs().getSvcRs().setHOSTACC_FLAG("N");
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc())) {
                hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxn.getFeptxnRepRc());
                hceRs.getBody().getRs().getSvcRs().setHOSTACC_FLAG("N");
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnReplyCode()) && !StringUtils.equalsAny(feptxn.getFeptxnReplyCode(), "4001","000")) {
                hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxn.getFeptxnReplyCode());
                hceRs.getBody().getRs().getSvcRs().setHOSTACC_FLAG("N");
            } else {
                if(feptxn.getFeptxnReplyCode() != null){
                    hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxn.getFeptxnReplyCode());
                }else {
                    hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE("T001");
                }
                if (feptxn.getFeptxnAccType() == 1) {
                    hceRs.getBody().getRs().getSvcRs().setHOSTACC_FLAG("Y");
                } else {
                    hceRs.getBody().getRs().getSvcRs().setHOSTACC_FLAG("N");
                }

                if (feptxn.getFeptxnBalb().compareTo(BigDecimal.ZERO) != 0 || feptxn.getFeptxnBalb().compareTo(BigDecimal.ZERO) != 0.00) {
                    hceRs.getBody().getRs().getSvcRs().setTRANSFROUTBAL(feptxn.getFeptxnBalb());
                }

                if (feptxn.getFeptxnBala().compareTo(BigDecimal.ZERO) != 0 || feptxn.getFeptxnBala().compareTo(BigDecimal.ZERO) != 0.00) {
                    hceRs.getBody().getRs().getSvcRs().setTRANSOUTAVBL(feptxn.getFeptxnBala());
                }
            }

            if (feptxn != null){
                hceRs.getBody().getRs().getSvcRs().setOUTDATE(feptxn.getFeptxnTxDate());
                hceRs.getBody().getRs().getSvcRs().setOUTTIME(feptxn.getFeptxnTxTime());
                hceRs.getBody().getRs().getSvcRs().setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
                hceRs.getBody().getRs().getSvcRs().setTXNSTAN(feptxn.getFeptxnStan());
                hceRs.getBody().getRs().getSvcRs().setCUSTOMERID(feptxn.getFeptxnIdno());
                hceRs.getBody().getRs().getSvcRs().setTXNTYPE("IQ");
                hceRs.getBody().getRs().getSvcRs().setTRANSAMT(feptxn.getFeptxnTxAmt());
                hceRs.getBody().getRs().getSvcRs().setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno7());
                hceRs.getBody().getRs().getSvcRs().setTRNSFROUTACCNT(feptxn.getFeptxnTroutActno());
                hceRs.getBody().getRs().getSvcRs().setTRNSFRINBANK(feptxn.getFeptxnTrinBkno7());
                hceRs.getBody().getRs().getSvcRs().setTRNSFRINACCNT(feptxn.getFeptxnTrinActno());
                if (feptxn.getFeptxnBrno() == null || "".equals(feptxn.getFeptxnBrno().trim())){
                    hceRs.getBody().getRs().getSvcRs().setCLEANBRANCHOUT(" ");
                }else {
                    hceRs.getBody().getRs().getSvcRs().setCLEANBRANCHOUT(feptxn.getFeptxnBrno());
                }
                hceRs.getBody().getRs().getSvcRs().setCUSTPAYFEE(feptxn.getFeptxnFeeCustpay());
                if (StringUtils.equals("4001", this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue()))){
                    //CB_IQTX_O001
                    if (feptxn.getFeptxnFiscFlag() == 0){
                        //自行
                        String FEP_IMS_RC3_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC3_TCB.getValue()); //透過msgfile轉4碼
                        if (FEP_IMS_RC3_TCB == null || FEP_IMS_RC3_TCB.trim().equals("") || "000".equals(FEP_IMS_RC3_TCB.trim())){
                            hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE("4001");
                        }else {
                            if (ImsMethodName.IMS_RC4_FISC.getValue() == null || ImsMethodName.IMS_RC4_FISC.getValue().trim().equals("") ){
                                hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE(FEP_IMS_RC3_TCB);
                            }else{
                                hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE(ImsMethodName.IMS_RC4_FISC.getValue());
                            }
                        }
                    }else{
                        //跨行
                        hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE(this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC4_FISC.getValue()));
                    }
                }else{
                    //CB_IQTX_O001
                    String OUTRTC = this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue());
//                    if (OUTRTC !=null && !StringUtils.isNumeric(OUTRTC)){
//                        OUTRTC = feptxn.getFeptxnReplyCode();
//                    }
                    hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE(OUTRTC);
                }
            }
            if(hceRs.getBody().getRs().getSvcRs().getTCBRTNCODE() ==null || "".equals(hceRs.getBody().getRs().getSvcRs().getTCBRTNCODE().trim())){
                if(feptxn == null || feptxn.getFeptxnReplyCode() == null || "".equals(feptxn.getFeptxnReplyCode().trim()) ){
                    hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE("T012");
                }else{
                    hceRs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxn.getFeptxnReplyCode());
                }
            }

            rtnMessage = XmlUtil.toXML(hceRs);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    /**
     * 5. UpdateTxData: 更新交易記錄(FEPTxn)
     */
    private void updateTxData() {

        feptxn.setFeptxnMsgflow("A4"); //Confirm RESPONSE

        if (rtnCode != FEPReturnCode.Normal) {
            feptxn.setFeptxnConReplyCode(StringUtils.rightPad(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.FEP, getLogContext()), 4, " "));
        }

        try {
            FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
        } catch (Exception ex) {
            rtnCode = FEPReturnCode.FEPTXNUpdateError;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateTxData");
            sendEMS(getLogContext());
        }

        if (rtnCode != FEPReturnCode.Normal) {
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
        body.setTXNTYPE("IQ");
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
                if ( FEP_IMS_RC4_FISC == null || FEP_IMS_RC4_FISC.trim().equals("") ){
                    if (FEP_IMS_RC3_TCB == null || FEP_IMS_RC3_TCB.trim().equals("") || "000".equals(FEP_IMS_RC3_TCB.trim())){
                        nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE("4001");
                    }else {
                        nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE(FEP_IMS_RC3_TCB);
                    }
                }else{
                    //自行4
                    nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE(this.getImsPropertiesValue(tota, FEP_IMS_RC4_FISC ));
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
            if(feptxn == null || feptxn.getFeptxnReplyCode() == null || "".equals(feptxn.getFeptxnReplyCode().trim()) ){
                nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE("T012");
            }else{
                nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxn.getFeptxnReplyCode());
            }
        }

        ResStr = XmlUtil.toXML(nbRs);
        return ResStr;
    }

    private void sendToCBS() throws Exception {
        /* 交易記帳處理 */
        String AATxTYPE = "0";
        String AA = getmHCEtxData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmHCEtxData());
        rtnCode = new CBS(hostAA, getmHCEtxData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
    }
}
