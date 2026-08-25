package com.syscom.fep.vo.text.ivr;

import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.vo.text.nb.NbXmlBase;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.math.BigDecimal;

@XStreamAlias("SOAP-ENV:Envelope")
public class RCV_VO_GeneralTrans_RQ extends NbXmlBase {
    @XStreamAsAttribute
    @XStreamAlias("xmlns:SOAP-ENV")
    private String xmlnsSoapEnv;
    @XStreamAsAttribute
    @XStreamAlias("xmlns:xsi")
    private String xmlnsXsi;
    @XStreamAlias("SOAP-ENV:Header")
    private RCV_VO_GeneralTrans_RQ_Header header;
    @XStreamAlias("SOAP-ENV:Body")
    private RCV_VO_GeneralTrans_RQ_Body body;

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
    public RCV_VO_GeneralTrans_RQ_Header getHeader() {
        return header;
    }
    public void setHeader(RCV_VO_GeneralTrans_RQ_Header header) {
        this.header = header;
    }
    public RCV_VO_GeneralTrans_RQ_Body getBody() {
        return body;
    }
    public void setBody(RCV_VO_GeneralTrans_RQ_Body body) {
        this.body = body;
    }
    @XStreamAlias("SOAP-ENV:Header")
    public static class RCV_VO_GeneralTrans_RQ_Header {
    }
    @XStreamAlias("SOAP-ENV:Body")
    public static class RCV_VO_GeneralTrans_RQ_Body {
        @XStreamAlias("esb:MsgRq")
        private RCV_VO_GeneralTrans_RQ_Body_MsgRq rq;
        public RCV_VO_GeneralTrans_RQ_Body_MsgRq getRq() {
            return rq;
        }
        public void setRq(RCV_VO_GeneralTrans_RQ_Body_MsgRq rq) {
            this.rq = rq;
        }
    }
    @XStreamAlias("esb:MsgRq")
    public static class RCV_VO_GeneralTrans_RQ_Body_MsgRq {
        @XStreamAlias("Header")
        private RCV_VO_GeneralTrans_RQ_Body_MsgRq_Header header;
        @XStreamAlias("SvcRq")
        private RCV_VO_GeneralTrans_RQ_Body_MsgRq_SvcRq svcRq;
        public RCV_VO_GeneralTrans_RQ_Body_MsgRq_Header getHeader() {
            return header;
        }
        public void setHeader(RCV_VO_GeneralTrans_RQ_Body_MsgRq_Header header) {
            this.header = header;
        }
        public RCV_VO_GeneralTrans_RQ_Body_MsgRq_SvcRq getSvcRq() {
            return svcRq;
        }
        public void setSvcRq(RCV_VO_GeneralTrans_RQ_Body_MsgRq_SvcRq svcRq) {
            this.svcRq = svcRq;
        }
    }

    @XStreamAlias("Header")
    public static class RCV_VO_GeneralTrans_RQ_Body_MsgRq_Header {
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
    public static class RCV_VO_GeneralTrans_RQ_Body_MsgRq_SvcRq {
        @XStreamAlias("PAYDATA")
        private RCV_VO_GeneralTrans_RQ_Body_MsgRq_PAYDATA PAYDATA;
        public RCV_VO_GeneralTrans_RQ_Body_MsgRq_PAYDATA getPAYDATA() {
            return PAYDATA;
        }
        public void setPAYDATA(RCV_VO_GeneralTrans_RQ_Body_MsgRq_PAYDATA PAYDATA) {
            this.PAYDATA = PAYDATA;
        }

        private String INDATE;
        private String INTIME;
        private String IPADDR;
        private String TXNTYPE;
        private String TERMINALID;
        private String TERMINAL_TYPE;
        private String TERMINAL_CHECKNO;
        private String FSCODE;
        private String PCODE;
        private String TRNSFLAG;
        private String BUSINESSTYPE;
        private String TRANSAMT;
        private String TRANBRANCH;
        private String TRNSFROUTIDNO;
        private String TRNSFROUTBANK;
        private String TRNSFROUTACCNT;
        private String TRNSFRINIDNO;
        private String TRNSFRINBANK;
        private String TRNSFRINACCNT;
        private String FEEPAYMENTTYPE;
        private String CUSTPAYFEE;
        private String FISCFEE;
        private String CHAFEE_BRANCH;
        private String CHAFEE_AMT;
        private String FAXFEE;
        private String TRANSFEE;
        private String OTHERBANKFEE;
        private String CHAFEE_TYPE;
        private String TRNSFRINNOTE;
        private String TRNSFROUTNOTE;
        private String ORITXSTAN;
        private String AFFAIRSCODE;
        private String CUSTCODE;
        private String TRNSFROUTNAME;
        private String SSLTYPE;
        private String LIMITTYPE;
        private String TRANSTYPEFLAG;
        private String TEXTMARK;

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
        public String getTRNSFLAG(){
            return this.TRNSFLAG;
        }
        public void setTRNSFLAG(String value) {
            this.TRNSFLAG = value;
        }
        public String getBUSINESSTYPE(){
            return this.BUSINESSTYPE;
        }
        public void setBUSINESSTYPE(String value) {
            this.BUSINESSTYPE = value;
        }
        public BigDecimal getTRANSAMT(){
            return CodeGenUtil.asciiToBigDecimal(this.TRANSAMT, false, 2);
        }
        public void setTRANSAMT(BigDecimal value) {
            this.TRANSAMT = CodeGenUtil.bigDecimalToAscii(value, 13, false, 2, true);
        }
        public String getTRANBRANCH(){
            return this.TRANBRANCH;
        }
        public void setTRANBRANCH(String value) {
            this.TRANBRANCH = value;
        }
        public String getTRNSFROUTIDNO(){
            return this.TRNSFROUTIDNO;
        }
        public void setTRNSFROUTIDNO(String value) {
            this.TRNSFROUTIDNO = value;
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
        public String getTRNSFRINIDNO(){
            return this.TRNSFRINIDNO;
        }
        public void setTRNSFRINIDNO(String value) {
            this.TRNSFRINIDNO = value;
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
        public String getFEEPAYMENTTYPE(){
            return this.FEEPAYMENTTYPE;
        }
        public void setFEEPAYMENTTYPE(String value) {
            this.FEEPAYMENTTYPE = value;
        }
        public BigDecimal getCUSTPAYFEE(){
            return CodeGenUtil.asciiToBigDecimal(this.CUSTPAYFEE, false, 0);
        }
        public void setCUSTPAYFEE(BigDecimal value) {
            this.CUSTPAYFEE = CodeGenUtil.bigDecimalToAscii(value, 7, false, 0, false);
        }
        public BigDecimal getFISCFEE(){
            return CodeGenUtil.asciiToBigDecimal(this.FISCFEE, false, 0);
        }
        public void setFISCFEE(BigDecimal value) {
            this.FISCFEE = CodeGenUtil.bigDecimalToAscii(value, 7, false, 0, false);
        }
        public String getCHAFEE_BRANCH(){
            return this.CHAFEE_BRANCH;
        }
        public void setCHAFEE_BRANCH(String value) {
            this.CHAFEE_BRANCH = value;
        }
        public BigDecimal getCHAFEE_AMT(){
            return CodeGenUtil.asciiToBigDecimal(this.CHAFEE_AMT, false, 0);
        }
        public void setCHAFEE_AMT(BigDecimal value) {
            this.CHAFEE_AMT = CodeGenUtil.bigDecimalToAscii(value, 7, false, 0, false);
        }
        public BigDecimal getFAXFEE(){
            return CodeGenUtil.asciiToBigDecimal(this.FAXFEE, false, 0);
        }
        public void setFAXFEE(BigDecimal value) {
            this.FAXFEE = CodeGenUtil.bigDecimalToAscii(value, 7, false, 0, false);
        }
        public BigDecimal getTRANSFEE(){
            return CodeGenUtil.asciiToBigDecimal(this.TRANSFEE, false, 0);
        }
        public void setTRANSFEE(BigDecimal value) {
            this.TRANSFEE = CodeGenUtil.bigDecimalToAscii(value, 7, false, 0, false);
        }
        public BigDecimal getOTHERBANKFEE(){
            return CodeGenUtil.asciiToBigDecimal(this.OTHERBANKFEE, false, 0);
        }
        public void setOTHERBANKFEE(BigDecimal value) {
            this.OTHERBANKFEE = CodeGenUtil.bigDecimalToAscii(value, 7, false, 0, false);
        }
        public String getCHAFEE_TYPE(){
            return this.CHAFEE_TYPE;
        }
        public void setCHAFEE_TYPE(String value) {
            this.CHAFEE_TYPE = value;
        }
        public String getTRNSFRINNOTE(){
            return this.TRNSFRINNOTE;
        }
        public void setTRNSFRINNOTE(String value) {
            this.TRNSFRINNOTE = value;
        }
        public String getTRNSFROUTNOTE(){
            return this.TRNSFROUTNOTE;
        }
        public void setTRNSFROUTNOTE(String value) {
            this.TRNSFROUTNOTE = value;
        }
        public String getORITXSTAN(){
            return this.ORITXSTAN;
        }
        public void setORITXSTAN(String value) {
            this.ORITXSTAN = value;
        }
        public String getAFFAIRSCODE(){
            return this.AFFAIRSCODE;
        }
        public void setAFFAIRSCODE(String value) {
            this.AFFAIRSCODE = value;
        }
        public String getCUSTCODE(){
            return this.CUSTCODE;
        }
        public void setCUSTCODE(String value) {
            this.CUSTCODE = value;
        }
        public String getTRNSFROUTNAME(){
            return this.TRNSFROUTNAME;
        }
        public void setTRNSFROUTNAME(String value) {
            this.TRNSFROUTNAME = value;
        }
        public String getSSLTYPE(){
            return this.SSLTYPE;
        }
        public void setSSLTYPE(String value) {
            this.SSLTYPE = value;
        }
        public String getLIMITTYPE(){
            return this.LIMITTYPE;
        }
        public void setLIMITTYPE(String value) {
            this.LIMITTYPE = value;
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
    }

    @XStreamAlias("PAYDATA")
    public static class RCV_VO_GeneralTrans_RQ_Body_MsgRq_PAYDATA {
        private String PAYCATEGORY;
        private String PAYNO;
        private String PAYENDDATE;
        private String ORGAN;
        private String CID;
        private String FILLER;
        private String NPOPID;
        private String NPPAYTYPE;
        private String NPFEENO;
        private String NPID;
        private String NPPAYNO;
        private String NPPAYENDDATE;
        private String NPBRANCH;
        private String IDENTIFIER;

        public String getPAYCATEGORY(){
            return this.PAYCATEGORY;
        }
        public void setPAYCATEGORY(String value) {
            this.PAYCATEGORY = value;
        }
        public String getPAYNO(){
            return this.PAYNO;
        }
        public void setPAYNO(String value) {
            this.PAYNO = value;
        }
        public String getPAYENDDATE(){
            return this.PAYENDDATE;
        }
        public void setPAYENDDATE(String value) {
            this.PAYENDDATE = value;
        }
        public String getORGAN(){
            return this.ORGAN;
        }
        public void setORGAN(String value) {
            this.ORGAN = value;
        }
        public String getCID(){
            return this.CID;
        }
        public void setCID(String value) {
            this.CID = value;
        }
        public String getFILLER(){
            return this.FILLER;
        }
        public void setFILLER(String value) {
            this.FILLER = value;
        }
        public String getNPOPID(){
            return this.NPOPID;
        }
        public void setNPOPID(String value) {
            this.NPOPID = value;
        }
        public String getNPPAYTYPE(){
            return this.NPPAYTYPE;
        }
        public void setNPPAYTYPE(String value) {
            this.NPPAYTYPE = value;
        }
        public String getNPFEENO(){
            return this.NPFEENO;
        }
        public void setNPFEENO(String value) {
            this.NPFEENO = value;
        }
        public String getNPID(){
            return this.NPID;
        }
        public void setNPID(String value) {
            this.NPID = value;
        }
        public String getNPPAYNO(){
            return this.NPPAYNO;
        }
        public void setNPPAYNO(String value) {
            this.NPPAYNO = value;
        }
        public String getNPPAYENDDATE(){
            return this.NPPAYENDDATE;
        }
        public void setNPPAYENDDATE(String value) {
            this.NPPAYENDDATE = value;
        }
        public String getNPBRANCH(){
            return this.NPBRANCH;
        }
        public void setNPBRANCH(String value) {
            this.NPBRANCH = value;
        }
        public String getIDENTIFIER(){
            return this.IDENTIFIER;
        }
        public void setIDENTIFIER(String value) {
            this.IDENTIFIER = value;
        }
    }

}
