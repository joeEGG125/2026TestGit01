package com.syscom.fep.vo.text.hce;

import com.syscom.fep.vo.CodeGenUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.math.BigDecimal;

@XStreamAlias("soapenv:Envelope")
public class RCV_HCE_GeneralTrans_RQ {
    @XStreamAsAttribute
    @XStreamAlias("xmlns:soapenv")
    private String xmlnsSoapEnv;
    @XStreamAsAttribute
    @XStreamAlias("xmlns:xsi")
    private String xmlnsXsi;
    @XStreamAlias("soapenv:Header")
    private RCV_HCE_GeneralTrans_RQ_Header header;
    @XStreamAlias("soapenv:Body")
    private RCV_HCE_GeneralTrans_RQ_Body body;

    public String getXmlnsSoapEnv() {
        return xmlnsSoapEnv;
    }
    public void setXmlnsSoapEnv(String xmlnsSoapEnv) {
        this.xmlnsSoapEnv = xmlnsSoapEnv;
    }
    public String getXmlnsXsi() {
        return xmlnsXsi;
    }
    public void setXmlnsXsi(String xmlnsXsi) {
        this.xmlnsXsi = xmlnsXsi;
    }
    public RCV_HCE_GeneralTrans_RQ_Header getHeader() {
        return header;
    }
    public void setHeader(RCV_HCE_GeneralTrans_RQ_Header header) {
        this.header = header;
    }
    public RCV_HCE_GeneralTrans_RQ_Body getBody() {
        return body;
    }
    public void setBody(RCV_HCE_GeneralTrans_RQ_Body body) {
        this.body = body;
    }
    @XStreamAlias("soapenv:Header")
    public static class RCV_HCE_GeneralTrans_RQ_Header {
    }
    @XStreamAlias("soapenv:Body")
    public static class RCV_HCE_GeneralTrans_RQ_Body {
        @XStreamAlias("esb:MsgRq")
        private RCV_HCE_GeneralTrans_RQ_Body_MsgRq rq;
        public RCV_HCE_GeneralTrans_RQ_Body_MsgRq getRq() {
            return rq;
        }
        public void setRq(RCV_HCE_GeneralTrans_RQ_Body_MsgRq rq) {
            this.rq = rq;
        }
    }
    @XStreamAlias("esb:MsgRq")
    public static class RCV_HCE_GeneralTrans_RQ_Body_MsgRq {
        @XStreamAlias("Header")
        private RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header header;
        @XStreamAlias("SvcRq")
        private RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq svcRq;
        public RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header getHeader() {
            return header;
        }
        public void setHeader(RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header header) {
            this.header = header;
        }
        public RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq getSvcRq() {
            return svcRq;
        }
        public void setSvcRq(RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq svcRq) {
            this.svcRq = svcRq;
        }
    }

    @XStreamAlias("Header")
    public static class RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header {
        private String CLIENTTRACEID;
        private String CHANNEL;
        private String MSGID;
        private String MSGKIND;
        private String TXNID;
        private String BRANCHID;
        private String TERMID;
        private String CLIENTDT;

        public String getCLIENTTRACEID(){
            return this.CLIENTTRACEID;
        }
        public void setCLIENTTRACEID(String value) {
            this.CLIENTTRACEID = value;
        }
        public String getCHANNEL(){
            return this.CHANNEL;
        }
        public void setCHANNEL(String value) {
            this.CHANNEL = value;
        }
        public String getMSGID(){
            return this.MSGID;
        }
        public void setMSGID(String value) {
            this.MSGID = value;
        }
        public String getMSGKIND(){
            return this.MSGKIND;
        }
        public void setMSGKIND(String value) {
            this.MSGKIND = value;
        }
        public String getTXNID(){
            return this.TXNID;
        }
        public void setTXNID(String value) {
            this.TXNID = value;
        }
        public String getBRANCHID(){
            return this.BRANCHID;
        }
        public void setBRANCHID(String value) {
            this.BRANCHID = value;
        }
        public String getTERMID(){
            return this.TERMID;
        }
        public void setTERMID(String value) {
            this.TERMID = value;
        }
        public String getCLIENTDT(){
            return this.CLIENTDT;
        }
        public void setCLIENTDT(String value) {
            this.CLIENTDT = value;
        }
    }

    @XStreamAlias("SvcRq")
    public static class RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq {
        private String INDATE;
        private String INTIME;
        private String IPADDR;
        private String TXNTYPE;
        private String TERMINALID;
        private String TERMINAL_TYPE;
        private String TERMINAL_CHECKNO;
        private String FSCODE;
        private String PCODE;
        private String TRANSAMT;
        private String TRNSFROUTBANK;
        private String TRNSFROUTACCNT;
        private String IC_SEQNO;
        private String ICMARK;
        private String IC_TAC_LEN;
        private String IC_TAC;
        private String TRNSFRINBANK;
        private String TRNSFRINACCNT;
        private String TRANSTYPEFLAG;
        private String TEXTMARK;
        private String ORITXSTAN;
        private String IC_TAC_DATE;
        private String IC_TAC_TIME;

        public String getINDATE(){
            return this.INDATE;
        }
        public void setINDATE(String value) {
            this.INDATE = value;
        }
        public String getINTIME(){
            return this.INTIME;
        }
        public void setINTIME(String value) {
            this.INTIME = value;
        }
        public String getIPADDR(){
            return this.IPADDR;
        }
        public void setIPADDR(String value) {
            this.IPADDR = value;
        }
        public String getTXNTYPE(){
            return this.TXNTYPE;
        }
        public void setTXNTYPE(String value) {
            this.TXNTYPE = value;
        }
        public String getTERMINALID(){
            return this.TERMINALID;
        }
        public void setTERMINALID(String value) {
            this.TERMINALID = value;
        }
        public String getTERMINAL_TYPE(){
            return this.TERMINAL_TYPE;
        }
        public void setTERMINAL_TYPE(String value) {
            this.TERMINAL_TYPE = value;
        }
        public String getTERMINAL_CHECKNO(){
            return this.TERMINAL_CHECKNO;
        }
        public void setTERMINAL_CHECKNO(String value) {
            this.TERMINAL_CHECKNO = value;
        }
        public String getFSCODE(){
            return this.FSCODE;
        }
        public void setFSCODE(String value) {
            this.FSCODE = value;
        }
        public String getPCODE(){
            return this.PCODE;
        }
        public void setPCODE(String value) {
            this.PCODE = value;
        }
        public BigDecimal getTRANSAMT(){
            return CodeGenUtil.asciiToBigDecimal(this.TRANSAMT, false, 2);
        }
        public void setTRANSAMT(BigDecimal value) {
            this.TRANSAMT = CodeGenUtil.bigDecimalToAscii(value, 13, false, 2, true);
        }
        public String getTRNSFROUTBANK(){
            return this.TRNSFROUTBANK;
        }
        public void setTRNSFROUTBANK(String value) {
            this.TRNSFROUTBANK = value;
        }
        public String getTRNSFROUTACCNT(){
            return this.TRNSFROUTACCNT;
        }
        public void setTRNSFROUTACCNT(String value) {
            this.TRNSFROUTACCNT = value;
        }
        public String getIC_SEQNO(){
            return this.IC_SEQNO;
        }
        public void setIC_SEQNO(String value) {
            this.IC_SEQNO = value;
        }
        public String getICMARK(){
            return this.ICMARK;
        }
        public void setICMARK(String value) {
            this.ICMARK = value;
        }
        public String getIC_TAC_LEN(){
            return this.IC_TAC_LEN;
        }
        public void setIC_TAC_LEN(String value) {
            this.IC_TAC_LEN = value;
        }
        public String getIC_TAC(){
            return this.IC_TAC;
        }
        public void setIC_TAC(String value) {
            this.IC_TAC = value;
        }
        public String getTRNSFRINBANK(){
            return this.TRNSFRINBANK;
        }
        public void setTRNSFRINBANK(String value) {
            this.TRNSFRINBANK = value;
        }
        public String getTRNSFRINACCNT(){
            return this.TRNSFRINACCNT;
        }
        public void setTRNSFRINACCNT(String value) {
            this.TRNSFRINACCNT = value;
        }
        public String getTRANSTYPEFLAG(){
            return this.TRANSTYPEFLAG;
        }
        public void setTRANSTYPEFLAG(String value) {
            this.TRANSTYPEFLAG = value;
        }
        public String getTEXTMARK(){
            return this.TEXTMARK;
        }
        public void setTEXTMARK(String value) {
            this.TEXTMARK = value;
        }
        public String getORITXSTAN(){
            return this.ORITXSTAN;
        }
        public void setORITXSTAN(String value) {
            this.ORITXSTAN = value;
        }
        public String getIC_TAC_DATE(){
            return this.IC_TAC_DATE;
        }
        public void setIC_TAC_DATE(String value) {
            this.IC_TAC_DATE = value;
        }
        public String getIC_TAC_TIME(){
            return this.IC_TAC_TIME;
        }
        public void setIC_TAC_TIME(String value) {
            this.IC_TAC_TIME = value;
        }
    }

}
