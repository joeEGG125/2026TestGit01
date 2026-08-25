package com.syscom.fep.vo.text.hce;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.CodeGenUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("soap:Envelope")
public class SEND_HCE_GeneralTrans_RS {
    @XStreamAsAttribute
    @XStreamAlias("xmlns:soap")
    private String xmlnsSoap = "http://schemas.xmlsoap.org/soap/envelope/";
    @XStreamAsAttribute
    @XStreamAlias("xmlns:xsi")
    private String xmlnsXsi = "http://www.w3.org/2001/XMLSchema-instance";
    @XStreamAlias("soap:Header")
    private String header = StringUtils.EMPTY;
    @XStreamAlias("soap:Body")
    private SEND_HCE_GeneralTrans_RS_Body body;

    public SEND_HCE_GeneralTrans_RS_Body getBody() {
        return body;
    }
    public void setBody(SEND_HCE_GeneralTrans_RS_Body body) {
        this.body = body;
    }
    @XStreamAlias("soap:Body")
    public static class SEND_HCE_GeneralTrans_RS_Body {
        @XStreamAlias("NS1:MsgRs")
        private SEND_HCE_GeneralTrans_RS_Body_MsgRs rs;
        public SEND_HCE_GeneralTrans_RS_Body_MsgRs getRs() {
            return rs;
        }
        public void setRs(SEND_HCE_GeneralTrans_RS_Body_MsgRs rs) {
            this.rs = rs;
        }
    }
    @XStreamAlias("NS1:MsgRs")
    public static class SEND_HCE_GeneralTrans_RS_Body_MsgRs {
        @XStreamAsAttribute
        @XStreamAlias("xmlns:NS1")
        private String xmlnsNs1="http://www.ibm.com.tw/esb";
        @XStreamAlias("Header")
        private SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header header;
        @XStreamAlias("SvcRs")
        private SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs svcRs;
        public SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header getHeader() {
            return header;
        }
        public void setHeader(SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header header) {
            this.header = header;
        }
        public SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs getSvcRs() {
            return svcRs;
        }
        public void setSvcRs(SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs svcRs) {
            this.svcRs = svcRs;
        }
    }

    @XStreamAlias("Header")
    public static class SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header {
        private String CLIENTTRACEID;
        private String CHANNEL;
        private String MSGID;
        private String CLIENTDT;
        private String SYSTEMID;
        private String STATUSCODE;
        private String SEVERITY;
        private String STATUSDESC;

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
        public String getCLIENTDT(){
            return this.CLIENTDT;
        }
        public void setCLIENTDT(String value) {
            this.CLIENTDT = value;
        }
        public String getSYSTEMID(){
            return this.SYSTEMID;
        }
        public void setSYSTEMID(String value) {
            this.SYSTEMID = value;
        }
        public String getSTATUSCODE(){
            return this.STATUSCODE;
        }
        public void setSTATUSCODE(String value) {
            this.STATUSCODE = value;
        }
        public String getSEVERITY(){
            return this.SEVERITY;
        }
        public void setSEVERITY(String value) {
            this.SEVERITY = value;
        }
        public String getSTATUSDESC(){
            return this.STATUSDESC;
        }
        public void setSTATUSDESC(String value) {
            this.STATUSDESC = value;
        }
    }

    @XStreamAlias("SvcRs")
    public static class SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs {
        private String OUTDATE;
        private String OUTTIME;
        private String FEP_EJNO;
        private String TXNSTAN;
        private String CUSTOMERID;
        private String TXNTYPE;
        private String FSCODE;
        private String ACCTDATE;
        private String HOSTACC_FLAG;
        private String HOSTRVS_FLAG;
        private String TRANSAMT;
        private String TRANSFROUTBAL;
        private String TRANSOUTAVBL;
        private String TRNSFROUTBANK;
        private String TRNSFROUTACCNT;
        private String CLEANBRANCHOUT;
        private String TRNSFRINBANK;
        private String TRNSFRINACCNT;
        private String CLEANBRANCHIN;
        private String CUSTPAYFEE;
        private String TCBRTNCODE;

        public String getOUTDATE(){
            return this.OUTDATE;
        }
        public void setOUTDATE(String value) {
            this.OUTDATE = value;
        }
        public String getOUTTIME(){
            return this.OUTTIME;
        }
        public void setOUTTIME(String value) {
            this.OUTTIME = value;
        }
        public String getFEP_EJNO(){
            return this.FEP_EJNO;
        }
        public void setFEP_EJNO(String value) {
            this.FEP_EJNO = value;
        }
        public String getTXNSTAN(){
            return this.TXNSTAN;
        }
        public void setTXNSTAN(String value) {
            this.TXNSTAN = value;
        }
        public String getCUSTOMERID(){
            return this.CUSTOMERID;
        }
        public void setCUSTOMERID(String value) {
            this.CUSTOMERID = value;
        }
        public String getTXNTYPE(){
            return this.TXNTYPE;
        }
        public void setTXNTYPE(String value) {
            this.TXNTYPE = value;
        }
        public String getFSCODE(){
            return this.FSCODE;
        }
        public void setFSCODE(String value) {
            this.FSCODE = value;
        }
        public String getACCTDATE(){
            return this.ACCTDATE;
        }
        public void setACCTDATE(String value) {
            this.ACCTDATE = value;
        }
        public String getHOSTACC_FLAG(){
            return this.HOSTACC_FLAG;
        }
        public void setHOSTACC_FLAG(String value) {
            this.HOSTACC_FLAG = value;
        }
        public String getHOSTRVS_FLAG(){
            return this.HOSTRVS_FLAG;
        }
        public void setHOSTRVS_FLAG(String value) {
            this.HOSTRVS_FLAG = value;
        }
        public BigDecimal getTRANSAMT(){
            return CodeGenUtil.asciiToBigDecimal(this.TRANSAMT, false, 2);
        }
        public void setTRANSAMT(BigDecimal value) {
            this.TRANSAMT = CodeGenUtil.bigDecimalToAscii(value, 9, false, 2, false);
        }
        public BigDecimal getTRANSFROUTBAL(){
            return CodeGenUtil.asciiToBigDecimal(this.TRANSFROUTBAL, false, 2);
        }
        public void setTRANSFROUTBAL(BigDecimal value) {
            this.TRANSFROUTBAL = CodeGenUtil.bigDecimalToAscii(value, 11, false, 2, true);
        }
        public BigDecimal getTRANSOUTAVBL(){
            return CodeGenUtil.asciiToBigDecimal(this.TRANSOUTAVBL, false, 2);
        }
        public void setTRANSOUTAVBL(BigDecimal value) {
            this.TRANSOUTAVBL = CodeGenUtil.bigDecimalToAscii(value, 11, false, 2, true);
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
        public String getCLEANBRANCHOUT(){
            return this.CLEANBRANCHOUT;
        }
        public void setCLEANBRANCHOUT(String value) {
            this.CLEANBRANCHOUT = value;
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
        public String getCLEANBRANCHIN(){
            return this.CLEANBRANCHIN;
        }
        public void setCLEANBRANCHIN(String value) {
            this.CLEANBRANCHIN = value;
        }
        public BigDecimal getCUSTPAYFEE(){
            return CodeGenUtil.asciiToBigDecimal(this.CUSTPAYFEE, false, 2);
        }
        public void setCUSTPAYFEE(BigDecimal value) {
            this.CUSTPAYFEE = CodeGenUtil.bigDecimalToAscii(value, 11, false, 2, false);
        }
        public String getTCBRTNCODE(){
            return this.TCBRTNCODE;
        }
        public void setTCBRTNCODE(String value) {
            this.TCBRTNCODE = value;
        }
    }

}
