package com.syscom.fep.server.aa.nb;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.mapper.FeptxntcbMapper;
import com.syscom.fep.mybatis.mapper.VatxnMapper;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;

/**
 * @author Jaime
 */
public class NBInq extends ATMPAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
    private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);
    private FeptxntcbMapper feptxntcbMapper = SpringBeanFactoryUtil.getBean(FeptxntcbMapper.class);

    private VatxnMapper vatxnMapper = SpringBeanFactoryUtil.getBean(VatxnMapper.class);

    public NBInq(NBData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getmNBtxData().getTxNbfepObject().getRequest().getBody().getRq().getHeader();
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
            rtnMessage = this.response();

            // 5. UpdateTxData: 更新交易記錄(FEPTxn)
//            this.updateTxData();

        } catch (Exception ex) {
            rtnMessage = getResNBStr(atmReqheader);
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
     * 2. CheckBusinessRule: 商業邏輯檢核
     *
     * @return
     * @throws Exception
     */
    private FEPReturnCode checkBusinessRule() throws Exception {

        RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header header = this.getmNBtxData().getTxNbfepObject().getRequest().getBody().getRq().getHeader();
        RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq nbbody = this.getmNBtxData().getTxNbfepObject().getRequest().getBody().getRq().getSvcRq();
        Feptxn dbfeptxn = feptxnDao.getOldFeptxndata(nbbody.getINDATE(), header.getCLIENTTRACEID(), header.getCHANNEL());
        feptxntcb = null;
        if (dbfeptxn != null) /* 3/3 添加取用原交易feptxntcb，供回應電文取用 */
            feptxntcb = feptxntcbMapper.selectByPrimaryKey( dbfeptxn.getFeptxnTxDate(),dbfeptxn.getFeptxnEjfno());

        setFeptxn(dbfeptxn);
        getmNBtxData().setFeptxn(dbfeptxn);
        if (dbfeptxn == null || feptxntcb ==null) {
            return FEPReturnCode.FEPTXNNotFound;
        } else {
            int EJ = dbfeptxn.getFeptxnEjfno(); // 暫存當下交易EJ，避免被原交易覆蓋
            feptxn.setFeptxnEjfno(EJ);
            getmNBtxData().setEj(EJ);
            getLogContext().setEj(EJ);
            if (!nbbody.getTXNTYPE().equals("RQ")
//                    && nbbody.getTERMINALID().equals(getFeptxn().getFeptxnAtmno())
                    && nbbody.getFSCODE().equals(getFeptxn().getFeptxnTxCode().trim())
                    && nbbody.getPCODE().equals(getFeptxn().getFeptxnPcode())
                    && nbbody.getTRNSFROUTBANK().substring(0, 3).equals(getFeptxn().getFeptxnTroutBkno())
                    && StringUtils.leftPad(nbbody.getTRNSFROUTACCNT(), 16, "0").equals(getFeptxn().getFeptxnTroutActno().trim())
                    && nbbody.getTRANSAMT().compareTo(getFeptxn().getFeptxnTxAmt()) == 0
            ) {
                rtnCode = FEPReturnCode.Normal;
            } else {
                rtnCode = FEPReturnCode.FEPTXNNotFound;/* 其他類檢核錯誤 */
            }
//            if (rtnCode != null) {
//                if ("10203".equals(String.valueOf(rtnCode.getValue()))) {
//                    String replyCode = msgfileExtMapper.selectByPrimaryKey(7, String.valueOf(rtnCode.getValue())).getMsgfileAtm();
//                    if (replyCode == null || StringUtils.isBlank(replyCode)) {
//                        replyCode = AbnormalRC.ATM_Error;
//                    }
//                    feptxn.setFeptxnReplyCode(replyCode);
//                } else {
//                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
//                            FEPChannel.FEP, getmNBtxData().getTxChannel(), logContext));
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
        RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getmNBReq().getBody().getRq().getSvcRq();

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
            RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = null;
            RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbodyVA = null;
            String fscode = "";
            String fscodeVA = "";
            if (this.getmNBReq() != null) {
                if (this.getmNBReq().getBody() != null) {
                    if (this.getmNBReq().getBody().getRq() != null) {
                        if (this.getmNBReq().getBody().getRq().getSvcRq() != null) {
                            atmReqbody = this.getmNBReq().getBody().getRq().getSvcRq();
                            fscode = atmReqbody.getFSCODE();
                        }
                    }
                }
            }

            if (this.getmVAReq() != null) {
                if (this.getmVAReq().getBody() != null) {
                    if (this.getmVAReq().getBody().getRq() != null) {
                        if (this.getmVAReq().getBody().getRq().getSvcRq() != null) {
                            atmReqbodyVA = this.getmVAReq().getBody().getRq().getSvcRq();
                            fscodeVA = atmReqbodyVA.getFSCODE();
                        }
                    }
                }
            }
            ;

            RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getmNBReq().getBody().getRq().getHeader();
            SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
            SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS_Body();
            SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS_Body_MsgRs();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
            rs.setBody(rsbody);
            rs.getBody().setRs(msgrs);
            rs.getBody().getRs().setHeader(header);
            rs.getBody().getRs().setSvcRs(body);

            rs.getBody().getRs().getHeader().setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
            rs.getBody().getRs().getHeader().setCHANNEL(atmReqheader.getCHANNEL());
            rs.getBody().getRs().getHeader().setMSGID(atmReqheader.getMSGID());
            rs.getBody().getRs().getHeader().setCLIENTDT(atmReqheader.getCLIENTDT());
            rs.getBody().getRs().getHeader().setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short) 1, "NATM_SYSTEMID_ATM").getSysconfValue());
            rs.getBody().getRs().getHeader().setSEVERITY("INFO");
            rs.getBody().getRs().getHeader().setSTATUSCODE("4001");
            rs.getBody().getRs().setSvcRs(setNbBody(atmReqbody, true));
            if (StringUtils.equals("4001", this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue()))) {
                String FEP_IMS_RC3_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC3_TCB.getValue());
                String FEP_IMS_RC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC4_FISC.getValue());
                //CB_IQTX_O001
                if (feptxn.getFeptxnFiscFlag() == 0) {
                    //自行3
                    if ( FEP_IMS_RC4_FISC == null || FEP_IMS_RC4_FISC.trim().equals("") ){
                        if (FEP_IMS_RC3_TCB == null || FEP_IMS_RC3_TCB.trim().equals("") || "000".equals(FEP_IMS_RC3_TCB.trim())){
                            rs.getBody().getRs().getSvcRs().setTCBRTNCODE("4001");
                        }else {
                            rs.getBody().getRs().getSvcRs().setTCBRTNCODE(FEP_IMS_RC3_TCB);
                        }
                    }else{
                        //自行4
                        rs.getBody().getRs().getSvcRs().setTCBRTNCODE(this.getImsPropertiesValue(tota, FEP_IMS_RC4_FISC ));
                    }
                } else {
                    //跨行
                    rs.getBody().getRs().getSvcRs().setTCBRTNCODE(this.getImsPropertiesValue(tota, FEP_IMS_RC4_FISC));
                }
            } else {
                //CB_IQTX_O002
                String OUTRTC = this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue());
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(OUTRTC);
            }
            if (feptxn == null) {
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE("T450");
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG("N");
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG("N");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxn.getFeptxnCbsRc());
            } else if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbOutrtc()) && !StringUtils.equalsAny(feptxntcb.getFeptxntcbOutrtc(), "4001", "000")) {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG("N");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxntcb.getFeptxntcbOutrtc());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc())) {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG("N");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxn.getFeptxnRepRc());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnReplyCode()) && !StringUtils.equalsAny(feptxn.getFeptxnReplyCode(), "000", "4001")) {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG("N");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxn.getFeptxnReplyCode());
            } else {
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.ACTBALANCE.getValue()))) {
                    rs.getBody().getRs().getSvcRs().setTRANSFROUTBAL(new BigDecimal(this.getImsPropertiesValue(tota, ImsMethodName.ACTBALANCE.getValue())));
                } else {
                    rs.getBody().getRs().getSvcRs().setTRANSFROUTBAL(BigDecimal.ZERO.setScale(2));
                }
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.AVAILABLE_BALANCE.getValue()))) {
                    rs.getBody().getRs().getSvcRs().setTRANSOUTAVBL(new BigDecimal(this.getImsPropertiesValue(tota, ImsMethodName.AVAILABLE_BALANCE.getValue())));
                } else {
                    rs.getBody().getRs().getSvcRs().setTRANSOUTAVBL(BigDecimal.ZERO.setScale(2));
                }

                BigDecimal amt;
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.TRANSAMTOUT.getValue()))) {
                    amt = new BigDecimal(this.getImsPropertiesValue(tota, ImsMethodName.TRANSAMTOUT.getValue()));
                } else {
                    amt = feptxn.getFeptxnTxAmt().add(feptxn.getFeptxnFeeCustpay());
                }
                rs.getBody().getRs().getSvcRs().setTRANSAMTOUT(amt.setScale(2));
                if (feptxn.getFeptxnFeeCustpay() != null) {
                    rs.getBody().getRs().getSvcRs().setCUSTPAYFEE(feptxn.getFeptxnFeeCustpay());
                }
                rs.getBody().getRs().getSvcRs().setCUSTPAYFEE(feptxn.getFeptxnNpsFeeCustpay());
                rs.getBody().getRs().getSvcRs().setFISCFEE(feptxn.getFeptxnNpsFeeFisc());
                rs.getBody().getRs().getSvcRs().setOTHERBANKFEE(feptxn.getFeptxnNpsFeeRcvr());
                rs.getBody().getRs().getSvcRs().setCHAFEE_BRANCH(this.getImsPropertiesValue(tota, ImsMethodName.CHAFEE_BRANCH.getValue()));
                if (StringUtils.equals(getLogContext().getChannel().getNameS(), FEPChannel.MCH.getNameS())) {
                    String chafeeamt = this.getImsPropertiesValue(tota, ImsMethodName.E_CHAFEEAMT.getValue());
                    rs.getBody().getRs().getSvcRs().setCHAFEEAMT(new BigDecimal(StringUtils.isBlank(chafeeamt) ? "0" : chafeeamt));
                }
                if (feptxn.getFeptxnReplyCode() != null) {
                    rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxn.getFeptxnReplyCode());
                } else {
                    rs.getBody().getRs().getSvcRs().setTCBRTNCODE("T001");
                }
            }
            if (feptxn != null) {
                rs.getBody().getRs().getSvcRs().setOUTDATE(feptxn.getFeptxnTxDate());
                rs.getBody().getRs().getSvcRs().setOUTTIME(feptxn.getFeptxnTxTime());
                rs.getBody().getRs().getSvcRs().setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
                rs.getBody().getRs().getSvcRs().setTXNSTAN(feptxn.getFeptxnStan());
                if (atmReqbody != null && StringUtils.equals(getLogContext().getChannel().getNameS(), FEPChannel.EOI.getNameS()))
                    rs.getBody().getRs().getSvcRs().setCUSTOMERID(atmReqbody.getCUSTCODE());
                else
                    rs.getBody().getRs().getSvcRs().setCUSTOMERID(feptxn.getFeptxnIdno());
                rs.getBody().getRs().getSvcRs().setTXNTYPE("IQ");
                rs.getBody().getRs().getSvcRs().setFSCODE(feptxn.getFeptxnTxCode());
                if(feptxn.getFeptxnTbsdy()!= null && feptxn.getFeptxnTbsdy().length()>=8){
                    rs.getBody().getRs().getSvcRs().setACCTDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTbsdy()));
                }else {
                    rs.getBody().getRs().getSvcRs().setACCTDATE(" ");
                }
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue()))) {
                    rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue()));
                }
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue()))) {
                    rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue()));
                }

                if (feptxn.getFeptxnTxAmt() != null) {
                    rs.getBody().getRs().getSvcRs().setTRANSAMT(new BigDecimal(feptxn.getFeptxnTxAmt().toString()));
                } else {
                    rs.getBody().getRs().getSvcRs().setTRANSAMT(BigDecimal.ZERO.setScale(2));
                }

                rs.getBody().getRs().getSvcRs().setCLEANBRANCHOUT(feptxn.getFeptxnBrno());
                if (atmReqbody != null) {
                    rs.getBody().getRs().getSvcRs().setTRNSFROUTIDNO(atmReqbody.getTRNSFROUTIDNO());
                    rs.getBody().getRs().getSvcRs().setTRNSFRINNOTE(atmReqbody.getTRNSFRINNOTE());
                }
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.E_TRNSFROUTNAME.getValue()))) {
                    rs.getBody().getRs().getSvcRs().setTRNSFROUTNAME(this.getImsPropertiesValue(tota, ImsMethodName.E_TRNSFROUTNAME.getValue()));
                }

                rs.getBody().getRs().getSvcRs().setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno7());
                rs.getBody().getRs().getSvcRs().setTRNSFROUTACCNT(feptxn.getFeptxnTroutActno());
                rs.getBody().getRs().getSvcRs().setTRNSFRINBANK(feptxn.getFeptxnTrinBkno7());
                rs.getBody().getRs().getSvcRs().setTRNSFRINACCNT(feptxn.getFeptxnTrinActno());
                rs.getBody().getRs().getSvcRs().setCLEANBRANCHIN(feptxn.getFeptxnTrinBrno());
                rs.getBody().getRs().getSvcRs().setTRNSFROUTNOTE(feptxn.getFeptxnPsbremFD());
                rs.getBody().getRs().getSvcRs().setPAYEREMAIL(this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_EMAIL.getValue()));
                if (StringUtils.equals("4001", this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue()))) {
                    String FEP_IMS_RC3_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC3_TCB.getValue());
                    String FEP_IMS_RC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC4_FISC.getValue());
                    //CB_IQTX_O001
                    if (feptxn.getFeptxnFiscFlag() == 0) {
                        //自行3
                        if ( FEP_IMS_RC4_FISC == null || FEP_IMS_RC4_FISC.trim().equals("") ){
                            if (FEP_IMS_RC3_TCB == null || FEP_IMS_RC3_TCB.trim().equals("") || "000".equals(FEP_IMS_RC3_TCB.trim())){
                                rs.getBody().getRs().getSvcRs().setTCBRTNCODE("4001");
                            }else {
                                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(FEP_IMS_RC3_TCB);
                            }
                        }else{
                            //自行4
                            rs.getBody().getRs().getSvcRs().setTCBRTNCODE(this.getImsPropertiesValue(tota, FEP_IMS_RC4_FISC ));
                        }
                    } else {
                        //跨行
                        rs.getBody().getRs().getSvcRs().setTCBRTNCODE(this.getImsPropertiesValue(tota, FEP_IMS_RC4_FISC));
                    }
                } else {
                    //CB_IQTX_O002
                    String OUTRTC = this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue());
                    rs.getBody().getRs().getSvcRs().setTCBRTNCODE(OUTRTC);
                }
            } else {
                String nullString = " ";
                rs.getBody().getRs().getSvcRs().setOUTDATE(nullString);
                rs.getBody().getRs().getSvcRs().setOUTTIME(nullString);
                rs.getBody().getRs().getSvcRs().setFEP_EJNO(String.valueOf(nullString));
                rs.getBody().getRs().getSvcRs().setTXNSTAN(nullString);
                if (atmReqbody != null && StringUtils.equals(getLogContext().getChannel().getNameS(), FEPChannel.EOI.getNameS()))
                    rs.getBody().getRs().getSvcRs().setCUSTOMERID(atmReqbody.getCUSTCODE());
                else
                    rs.getBody().getRs().getSvcRs().setCUSTOMERID(nullString);
                rs.getBody().getRs().getSvcRs().setTXNTYPE("IQ");
                rs.getBody().getRs().getSvcRs().setFSCODE(nullString);
                rs.getBody().getRs().getSvcRs().setACCTDATE(nullString);

                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue()))) {
                    rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue()));
                } else {
                    rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(nullString);
                }
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue()))) {
                    rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue()));
                } else {
                    rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(nullString);
                }

                rs.getBody().getRs().getSvcRs().setTRANSAMT(BigDecimal.ZERO.setScale(2));

                rs.getBody().getRs().getSvcRs().setCLEANBRANCHOUT(nullString);
                if (atmReqbody != null) {
                    rs.getBody().getRs().getSvcRs().setTRNSFROUTIDNO(atmReqbody.getTRNSFROUTIDNO());
                    rs.getBody().getRs().getSvcRs().setTRNSFRINNOTE(atmReqbody.getTRNSFRINNOTE());
                }
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.E_TRNSFROUTNAME.getValue()))) {
                    rs.getBody().getRs().getSvcRs().setTRNSFROUTNAME(this.getImsPropertiesValue(tota, ImsMethodName.E_TRNSFROUTNAME.getValue()));
                } else {
                    rs.getBody().getRs().getSvcRs().setTRNSFROUTNAME(nullString);
                }

                rs.getBody().getRs().getSvcRs().setTRNSFROUTBANK(nullString);
                rs.getBody().getRs().getSvcRs().setTRNSFROUTACCNT(nullString);
                rs.getBody().getRs().getSvcRs().setTRNSFRINBANK(nullString);
                rs.getBody().getRs().getSvcRs().setTRNSFRINACCNT(nullString);
                rs.getBody().getRs().getSvcRs().setCLEANBRANCHIN(nullString);
                rs.getBody().getRs().getSvcRs().setTRNSFROUTNOTE(nullString);
                rs.getBody().getRs().getSvcRs().setPAYEREMAIL(this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_EMAIL.getValue()));
                if (StringUtils.equals("4001", this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue()))) {
                    //CB_IQTX_O001
                    if (feptxn.getFeptxnFiscFlag() == 0) {
                        //自行
                        String FEP_IMS_RC3_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC3_TCB.getValue()); //透過msgfile轉4碼
                        if (FEP_IMS_RC3_TCB == null || FEP_IMS_RC3_TCB.trim().equals("") || "000".equals(FEP_IMS_RC3_TCB.trim())){
                            rs.getBody().getRs().getSvcRs().setTCBRTNCODE("4001");
                        }else {
                            if (ImsMethodName.IMS_RC4_FISC.getValue() == null || ImsMethodName.IMS_RC4_FISC.getValue().trim().equals("") ){
                                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(FEP_IMS_RC3_TCB);
                            }else{
                                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(ImsMethodName.IMS_RC4_FISC.getValue());
                            }
                        }
                    } else {
                        rs.getBody().getRs().getSvcRs().setTCBRTNCODE(this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC4_FISC.getValue()));
                    }
                } else {
                    //CB_IQTX_O001
                    String OUTRTC = this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue());
//                    if (OUTRTC != null && !StringUtils.isNumeric(OUTRTC)) {
//                        OUTRTC = feptxn.getFeptxnReplyCode();
//                    }
                    rs.getBody().getRs().getSvcRs().setTCBRTNCODE(OUTRTC);
                }
            }

            if (rs.getBody().getRs().getSvcRs().getTCBRTNCODE() == null || "".equals(rs.getBody().getRs().getSvcRs().getTCBRTNCODE().trim())) {
                if (feptxn.getFeptxnReplyCode() == null || "".equals(feptxn.getFeptxnReplyCode().trim())) {
                    rs.getBody().getRs().getSvcRs().setTCBRTNCODE("T012");
                } else {
                    rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxn.getFeptxnReplyCode());
                }
            }
            if (StringUtils.equals(getLogContext().getChannel().getNameS(), FEPChannel.MBQ.getNameS()))
                rtnMessage = XmlUtil.toXML(rs).replace("+", " ");
            else
                rtnMessage = XmlUtil.toXML(rs);
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
        body.setTXNTYPE("IQ");
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
        header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short) 1, "NATM_SYSTEMID_FEP").getSysconfValue());
        header.setSTATUSCODE("T099");
        header.setSEVERITY("ERROR");
        header.setSTATUSDESC("發生exception");
        if (StringUtils.equals(getLogContext().getChannel().getNameS(), FEPChannel.MCH.getNameS()))
            header.setTXNID(nbheader.getTXNID());
        if (nbRs.getBody().getRs().getSvcRs().getTCBRTNCODE() == null || "".equals(nbRs.getBody().getRs().getSvcRs().getTCBRTNCODE().trim())) {
            if (feptxn.getFeptxnReplyCode() == null || "".equals(feptxn.getFeptxnReplyCode().trim())) {
                nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE("T012");
            } else {
                nbRs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxn.getFeptxnReplyCode());
            }
        }
        ResStr = XmlUtil.toXML(nbRs);
        return ResStr;
    }

    private void sendToCBS() throws Exception {
        /* 交易記帳處理 */
        String AATxTYPE = "0"; // 上CBS入扣帳
        String AA = getmNBtxData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmNBtxData());
        rtnCode = new CBS(hostAA, getmNBtxData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
    }

    private SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs setNbBody(RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq tita, boolean isGt90) {
        SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
        if (isGt90) {
            body.setOUTDATE(" ");
            body.setOUTTIME(" ");
            body.setFEP_EJNO(" ");
            body.setTXNSTAN(" ");
            body.setCUSTOMERID(" ");
            body.setTXNTYPE(tita.getTXNTYPE());
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
        } else {
            String TRNSFRINACCNT = "";
            if (tita.getTRNSFRINACCNT() != null) {
                TRNSFRINACCNT = tita.getTRNSFRINACCNT();
            }
            String TRNSFROUTACCNT = "";
            if (tita.getTRNSFROUTACCNT() != null) {
                TRNSFROUTACCNT = tita.getTRNSFROUTACCNT();
            }
            String NPPAYNO = "";
            if (tita.getPAYDATA() != null && tita.getPAYDATA().getNPPAYNO() != null) {
                NPPAYNO = tita.getPAYDATA().getNPPAYNO();
            }
            if (TRNSFROUTACCNT.length() > 16 || TRNSFRINACCNT.length() > 16 || NPPAYNO.length() > 16) {
                body.setOUTDATE(" ");
                body.setOUTTIME(" ");
                body.setFEP_EJNO(" ");
                body.setTXNSTAN(" ");
                body.setCUSTOMERID(" ");
                body.setTXNTYPE(tita.getTXNTYPE());
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
            }
        }
        return body;
    }
}
