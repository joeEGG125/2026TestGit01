package com.syscom.fep.server.aa.va;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.mapper.VatxnMapper;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Msgfile;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS;
import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author Jaime
 */
public class VAInq extends ATMPAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private VatxnMapper vatxnMapper = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
    private SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
    private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);

    public VAInq(NBData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header header = this.getmNBtxData().getTxVafepObject().getRequest().getBody().getRq().getHeader();

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

            // 4. 	組回應電文回給 前端
            if (StringUtils.isBlank(getmNBtxData().getTxResponseMessage())) {
                rtnMessage = this.response();
            } else {
                rtnMessage = getmNBtxData().getTxResponseMessage();
            }

            // 5. UpdateTxData: 更新交易記錄(FEPTxn)
//            this.updateTxData();

        } catch (Exception ex) {
            rtnMessage = getResVAStr(header);
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

        // 1.1 檢核 FEPTXN 是否存在
        RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header header = this.getmNBtxData().getTxVafepObject().getRequest().getBody().getRq().getHeader();
        RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq nbbody = this.getmNBtxData().getTxVafepObject().getRequest().getBody().getRq().getSvcRq();
        Feptxn dbfeptxn = feptxnDao.getOldFeptxndata(nbbody.getINDATE(), header.getCLIENTTRACEID(), header.getCHANNEL());
        setFeptxn(dbfeptxn);
        getmNBtxData().setFeptxn(dbfeptxn);

        if (getFeptxn() == null ) {
            return FEPReturnCode.FEPTXNNotFound;
        } else {
            Vatxn vatxn = vatxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(),feptxn.getFeptxnEjfno());
            if (vatxn == null) {
                return FEPReturnCode.VATXNNotFound;
            }
            int EJ = dbfeptxn.getFeptxnEjfno(); // 暫存當下交易EJ，避免被原交易覆蓋
            feptxn.setFeptxnEjfno(EJ);
            getmNBtxData().setEj(EJ);
            getLogContext().setEj(EJ);
            if (!nbbody.getTXNTYPE().equals("RQ")
//                    && nbbody.getTERMINALID().equals(feptxn.getFeptxnAtmno())
                    && nbbody.getFSCODE().equals(feptxn.getFeptxnTxCode().trim())
                    && nbbody.getPCODE().equals(feptxn.getFeptxnPcode())
                    && nbbody.getVACATE().equals(vatxn.getVatxnCate())
                    && nbbody.getAEIPYTP().equals(vatxn.getVatxnType())
                    && nbbody.getTAXIDNO().equals(feptxn.getFeptxnIdno().trim())
            ) {
                rtnCode = FEPReturnCode.Normal;
            } else {
                rtnCode = FEPReturnCode.OtherCheckError;/* 其他類檢核錯誤 */
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
//                            FEPChannel.FEP, getmNBtxData().getTxChannel(), logContext));
//                }
//            }
        }

        // 1.2 檢核 VATXN 是否存在
        Vatxn queryVatxn = vatxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
        if (queryVatxn != null) {
            RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getmVAReq().getBody().getRq().getSvcRq();
            if (!queryVatxn.getVatxnPcode().equals(atmReqbody.getPCODE())
                    || !queryVatxn.getVatxnCate().equals(atmReqbody.getVACATE())
                    || !queryVatxn.getVatxnType().equals(atmReqbody.getAEIPYTP())) {
                rtnCode = FEPReturnCode.OtherCheckError;
            }
        }
        return rtnCode;
    }

    /**
     * 3. 以ATM_TITA相關資料, 寫入 FEPTXN
     *
     * @throws Exception
     */
    private void prepareFEPTXN() throws Exception {
        RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getmVAReq().getBody().getRq().getSvcRq();

        feptxn.setFeptxnMsgflow("A3"); //INQ Request
        feptxn.setFeptxnTraceEjfno(getEj());
        feptxn.setFeptxnConTxCode(atmReqbody.getFSCODE() + "C"); //交易代號
        feptxn.setFeptxnConTxTime(atmReqbody.getINTIME()); //交易時間
        feptxn.setFeptxnConExcpCode(atmReqbody.getTXNTYPE()); //交易處理類別

        String traceEjfno = StringUtils.leftPad(String.valueOf(feptxn.getFeptxnTraceEjfno()), 7, "0");
        feptxn.setFeptxnConTxseq(traceEjfno.substring(traceEjfno.length() - 7)); // (只取7位)

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
            RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getmVAReq().getBody().getRq().getHeader();
            RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getmVAReq().getBody().getRq().getSvcRq();
            RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_SENDDATA senddata = this.getmVAReq().getBody().getRq().getSvcRq().getSENDDATA();
            SEND_VA_GeneralTrans_RS rs = new SEND_VA_GeneralTrans_RS();
            SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body rsbodyVA = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body();
            SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs msgrsVA = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs();
            SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_Header headerVA = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs bodyVA = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs();
            SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA replydata = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA();
            rs.setBody(rsbodyVA);
            rs.getBody().setReplyData(replydata);
            rs.getBody().setRs(msgrsVA);
            rs.getBody().getRs().setHeader(headerVA);
            rs.getBody().getRs().setSvcRs(bodyVA);

            rs.getBody().getRs().getHeader().setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
            rs.getBody().getRs().getHeader().setCHANNEL(atmReqheader.getCHANNEL());
            rs.getBody().getRs().getHeader().setMSGID(atmReqheader.getMSGID());
            rs.getBody().getRs().getHeader().setCLIENTDT(atmReqheader.getCLIENTDT());
            rs.getBody().getRs().getHeader().setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short)1,"NATM_SYSTEMID_ATM").getSysconfValue());
            rs.getBody().getRs().getHeader().setSEVERITY("INFO");
            rs.getBody().getRs().getHeader().setSTATUSCODE("4001");
            rs.getBody().getRs().setSvcRs(setBody(atmReqbody,true));
            if(feptxn == null){
                rs.getBody().getRs().getSvcRs().setRSPRESULT("T450");
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) &&  !"000".equals(feptxn.getFeptxnCbsRc()) && StringUtils.isBlank(atmReqbody.getSENDDATA().getAEIPYUES())) {
                rs.getBody().getRs().getSvcRs().setRSPRESULT(feptxn.getFeptxnCbsRc());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !NormalRC.FISC_ATM_OK.equals(feptxn.getFeptxnRepRc())) {
                rs.getBody().getRs().getSvcRs().setRSPRESULT(feptxn.getFeptxnRepRc());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnReplyCode()) && !StringUtils.equalsAny(feptxn.getFeptxnReplyCode(), "4001","000") ) {
                rs.getBody().getRs().getSvcRs().setRSPRESULT(feptxn.getFeptxnReplyCode());
            } else {
                if(feptxn.getFeptxnReplyCode() != null){
                    rs.getBody().getRs().getSvcRs().setRSPRESULT(feptxn.getFeptxnReplyCode());
                }else {
                    rs.getBody().getRs().getSvcRs().setRSPRESULT("T001");
                }
            }

            if(feptxn != null) {
                Vatxn Vatxn = vatxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
                rs.getBody().getRs().getSvcRs().setOUTDATE(feptxn.getFeptxnTxDate());
                rs.getBody().getRs().getSvcRs().setOUTTIME(feptxn.getFeptxnTxTime());
                rs.getBody().getRs().getSvcRs().setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
                rs.getBody().getRs().getSvcRs().setTXNSTAN(feptxn.getFeptxnStan());
                rs.getBody().getRs().getSvcRs().setCUSTOMERID(feptxn.getFeptxnIdno());
                rs.getBody().getRs().getSvcRs().setTXNTYPE("IQ");
                rs.getBody().getRs().getSvcRs().setFSCODE(feptxn.getFeptxnTxCode());
                rs.getBody().getRs().getSvcRs().setTRANSAMT(atmReqbody.getTRANSAMT() == null ? "" :
                        atmReqbody.getTRANSAMT().multiply(new BigDecimal(100)).toString());
                rs.getBody().getRs().getSvcRs().setAEIPYTP(atmReqbody.getAEIPYTP());
                rs.getBody().getRs().getSvcRs().setTAXIDNO(Vatxn.getVatxnIdno());
                rs.getBody().getRs().getSvcRs().setAEIPCRBK(atmReqbody.getAEIPCRBK());
                rs.getBody().getRs().getSvcRs().setCLACTNO(feptxn.getFeptxnTroutActno());
                if (StringUtils.equals("4001", this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue()))) {
                    String FEP_IMS_RC3_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC3_TCB.getValue());
                    String FEP_IMS_RC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC4_FISC.getValue());
                    //CB_IQTX_O001
                    if (feptxn.getFeptxnFiscFlag() == 0) {
                        //自行3
                        if (FEP_IMS_RC4_FISC == null || FEP_IMS_RC4_FISC.trim().equals("")) {
                            if (FEP_IMS_RC3_TCB == null || FEP_IMS_RC3_TCB.trim().equals("") || "000".equals(FEP_IMS_RC3_TCB.trim())) {
                                rs.getBody().getRs().getSvcRs().setRSPRESULT("4001");
                            } else {
                                rs.getBody().getRs().getSvcRs().setRSPRESULT(FEP_IMS_RC3_TCB);
                            }
                        } else {
                            //自行4
                            rs.getBody().getRs().getSvcRs().setRSPRESULT(this.getImsPropertiesValue(tota, FEP_IMS_RC4_FISC));
                        }
                    } else {
                        //跨行
                        rs.getBody().getRs().getSvcRs().setRSPRESULT(this.getImsPropertiesValue(tota, FEP_IMS_RC4_FISC));
                    }
                } else {
                    //CB_IQTX_O002
                    String OUTRTC = this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue());
                    rs.getBody().getRs().getSvcRs().setRSPRESULT(OUTRTC);
                }
            }
            Vatxn queryVatxn = vatxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
            if (queryVatxn != null) {
                rs.getBody().getRs().getSvcRs().setTAXIDNO(queryVatxn.getVatxnIdno());
                if (queryVatxn.getVatxnCate().equals("02")) {
                    rs.getBody().getReplyData().setPAYUNTNO(queryVatxn.getVatxnBusinessUnit());
                    rs.getBody().getReplyData().setTAXTYPE(queryVatxn.getVatxnPaytype());
                    rs.getBody().getReplyData().setPAYFEENO(queryVatxn.getVatxnFeeno());
                } else {
                    rs.getBody().getReplyData().setAELFTP(queryVatxn.getVatxnItem());
                    rs.getBody().getReplyData().setACRESULT(queryVatxn.getVatxnAcresult());
                    rs.getBody().getReplyData().setRESULT(queryVatxn.getVatxnResult());
                    rs.getBody().getReplyData().setACSTAT(queryVatxn.getVatxnAcstat());
                    rs.getBody().getReplyData().setCHKCELL(queryVatxn.getVatxnTelresult());
                    rs.getBody().getReplyData().setAEIPYUES(senddata.getAEIPYUES());
                }

            }
            if(rs.getBody().getRs().getSvcRs().getRSPRESULT() ==null || "".equals(rs.getBody().getRs().getSvcRs().getRSPRESULT().trim())){
                if(feptxn.getFeptxnReplyCode() == null || "".equals(feptxn.getFeptxnReplyCode().trim()) ){
                    rs.getBody().getRs().getSvcRs().setRSPRESULT("T012");
                }else{
                    rs.getBody().getRs().getSvcRs().setRSPRESULT(feptxn.getFeptxnReplyCode());

                }
            }
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

    private String getResVAStr(RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header nbheader) {

        String ResStr = "";
        SEND_VA_GeneralTrans_RS nbRs = new SEND_VA_GeneralTrans_RS();
        SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body rsbody = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body();
        SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs();
        SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_Header();
        SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs();
        msgrs.setHeader(header);
        body.setOUTDATE(" ");
        body.setOUTTIME(" ");
        body.setFEP_EJNO(" ");
        body.setTXNSTAN(" ");
        body.setCUSTOMERID(" ");
        body.setTXNTYPE("IQ");
        body.setFSCODE(" ");
        body.setTRANSAMT("0");
        body.setAEIPYTP(" ");
        body.setTAXIDNO(" ");
        body.setAEIPCRBK(" ");
        body.setCLACTNO(" ");
        body.setRSPRESULT(" ");
        body.setTXN_DATETIME(" ");
        body.setTERMINAL_TYPE(" ");
        body.setTERMINALID(" ");
        RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmVAReq().getBody().getRq().getSvcRq();
        body.setREPLYDATA(new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA());
        if (tita.getVACATE() != null && "10".equals(tita.getVACATE())){
            //2566-10
            body.getREPLYDATA().setAELFTP(" ");
            body.getREPLYDATA().setRESULT(" ");
            body.getREPLYDATA().setACRESULT(" ");
            body.getREPLYDATA().setACSTAT(" ");
            body.getREPLYDATA().setAEIPYUES(" ");
            body.getREPLYDATA().setCHKCELL(" ");
            body.getREPLYDATA().setFILLER(" ");
        }else {
            //2566-02
            body.getREPLYDATA().setPAYUNTNO(" ");
            body.getREPLYDATA().setTAXTYPE(" ");
            body.getREPLYDATA().setPAYFEENO(" ");
            body.getREPLYDATA().setFILLER1(" ");
        }
        msgrs.setSvcRs(body);
        rsbody.setRs(msgrs);
        nbRs.setBody(rsbody);

        header.setCLIENTTRACEID(nbheader.getCLIENTTRACEID());
        header.setCHANNEL(nbheader.getCHANNEL());
        header.setMSGID(nbheader.getMSGID());
        header.setCLIENTDT(nbheader.getCLIENTDT());
        header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short)1,"NATM_SYSTEMID_FEP").getSysconfValue());
        header.setSTATUSCODE("T099");
        header.setSEVERITY("ERROR");
        header.setSTATUSDESC("發生exception");
        if(nbRs.getBody().getRs().getSvcRs().getRSPRESULT() ==null || "".equals(nbRs.getBody().getRs().getSvcRs().getRSPRESULT().trim())){
            if(feptxn == null || feptxn.getFeptxnReplyCode() == null || "".equals(feptxn.getFeptxnReplyCode().trim()) ){
                nbRs.getBody().getRs().getSvcRs().setRSPRESULT("T012");
            }else{
                nbRs.getBody().getRs().getSvcRs().setRSPRESULT(feptxn.getFeptxnReplyCode());
            }
        }
        ResStr = XmlUtil.toXML(nbRs);
        return ResStr;
    }

    private void sendToCBS() throws Exception {
        /* 交易記帳處理 */
        String AATxTYPE = "0";
        String AA = getmNBtxData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmNBtxData());
        rtnCode = new CBS(hostAA, getmNBtxData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
    }
    private SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs setBody(RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq tita,boolean isGt90){
        SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs();
        body.setOUTDATE(" ");
        body.setOUTTIME(" ");
        body.setFEP_EJNO(" ");
        body.setTXNSTAN(" ");
        body.setCUSTOMERID(" ");
        body.setTXNTYPE(tita.getTXNTYPE());
        body.setFSCODE(" ");
        body.setTRANSAMT("0");
        body.setAEIPYTP(" ");
        body.setTAXIDNO(" ");
        body.setAEIPCRBK(" ");
        body.setCLACTNO(" ");
        body.setRSPRESULT(" ");
        body.setTXN_DATETIME(" ");
        body.setTERMINAL_TYPE(" ");
        body.setTERMINALID(" ");
        if (isGt90){
            body.setREPLYDATA(new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA());
            if (tita.getVACATE() != null && "10".equals(tita.getVACATE())){
                //2566-10
                body.getREPLYDATA().setAELFTP(" ");
                body.getREPLYDATA().setRESULT(" ");
                body.getREPLYDATA().setACRESULT(" ");
                body.getREPLYDATA().setACSTAT(" ");
                body.getREPLYDATA().setAEIPYUES(" ");
                body.getREPLYDATA().setCHKCELL(" ");
                body.getREPLYDATA().setFILLER(" ");
            }else {
                //2566-02
                body.getREPLYDATA().setPAYUNTNO(" ");
                body.getREPLYDATA().setTAXTYPE(" ");
                body.getREPLYDATA().setPAYFEENO(" ");
                body.getREPLYDATA().setFILLER1(" ");
            }
        }else {
            //2566-02
            String AEIPYAC = "";
            if (tita.getAEIPYAC() != null){
                AEIPYAC = tita.getAEIPYAC();
            }
            String AEIPCRAC = "";
            if (tita.getAEIPCRAC()!=null){
                AEIPCRAC = tita.getAEIPCRAC();
            }
            if (AEIPYAC.length()>16 || AEIPCRAC.length() > 16){
                body.setREPLYDATA(new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA());
                if (tita.getVACATE() != null && "10".equals(tita.getVACATE())){
                    //2655-10
                    body.getREPLYDATA().setAELFTP(" ");
                    body.getREPLYDATA().setRESULT(" ");
                    body.getREPLYDATA().setACRESULT(" ");
                    body.getREPLYDATA().setACSTAT(" ");
                    body.getREPLYDATA().setAEIPYUES(" ");
                    body.getREPLYDATA().setCHKCELL(" ");
                    body.getREPLYDATA().setFILLER(" ");
                }else {
                    //2566-02
                    body.getREPLYDATA().setPAYUNTNO(" ");
                    body.getREPLYDATA().setTAXTYPE(" ");
                    body.getREPLYDATA().setPAYFEENO(" ");
                    body.getREPLYDATA().setFILLER1(" ");
                }
            }

        }
        return body;
    }
}
