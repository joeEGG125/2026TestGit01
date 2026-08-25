package com.syscom.fep.vo.text.webatm;
import com.syscom.fep.vo.CodeGenUtil;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.annotation.Field;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("soap:Envelope")
public class SEND_EATM_FC2_FR2 extends WebatmXmlBase {
@XStreamAsAttribute
@XStreamAlias("xmlns:soap")
private String xmlnsSoap = "http://schemas.xmlsoap.org/soap/envelope/";
@XStreamAsAttribute
@XStreamAlias("xmlns:xsi")
private String xmlnsXsi;
@XStreamAlias("soap:Header")
private String header = StringUtils.EMPTY;
@XStreamAlias("soap:Body")
private SEND_EATM_FC2_FR2_Body body;

public SEND_EATM_FC2_FR2_Body getBody() {
return body;
}
public void setBody(SEND_EATM_FC2_FR2_Body body) {
this.body = body;
}
@XStreamAlias("soap:Body")
public static class SEND_EATM_FC2_FR2_Body {
@XStreamAlias("NS1:MsgRs")
private SEND_EATM_FC2_FR2_Body_NS1MsgRs rs;
public SEND_EATM_FC2_FR2_Body_NS1MsgRs getRs() {
return rs;
}
public void setRs(SEND_EATM_FC2_FR2_Body_NS1MsgRs rs) {
this.rs = rs;
}
}
@XStreamAlias("NS1:MsgRs")
public static class SEND_EATM_FC2_FR2_Body_NS1MsgRs {
@XStreamAsAttribute
@XStreamAlias("xmlns:NS1")
private String xmlnsNs1="http://www.ibm.com.tw/esb";
@XStreamAlias("Header")
private SEND_EATM_FC2_FR2_Body_MsgRs_Header header;
@XStreamAlias("SvcRs")
private SEND_EATM_FC2_FR2_Body_MsgRs_SvcRs svcRs;
public SEND_EATM_FC2_FR2_Body_MsgRs_Header getHeader() {
return header;
}
public void setHeader(SEND_EATM_FC2_FR2_Body_MsgRs_Header header) {
this.header = header;
}
public SEND_EATM_FC2_FR2_Body_MsgRs_SvcRs getSvcRs() {
return svcRs;
}
public void setSvcRq(SEND_EATM_FC2_FR2_Body_MsgRs_SvcRs svcRs) {
this.svcRs = svcRs;
}
}

@XStreamAlias("Header")
public static class SEND_EATM_FC2_FR2_Body_MsgRs_Header {
        private String CLIENTTRACEID;
        private String CHANNEL;
        private String MSGID;
        private String CLIENTDT;
        private String SYSTEMID;
        private String STATUSCODE;
        private String SEVERITY;
        private String STATUSDESC;

        public String getCLIENTTRACEID(){
        return CLIENTTRACEID;
        }
        public void setCLIENTTRACEID(String value) {
        this.CLIENTTRACEID = value;
        }
        public String getCHANNEL(){
        return CHANNEL;
        }
        public void setCHANNEL(String value) {
        this.CHANNEL = value;
        }
        public String getMSGID(){
        return MSGID;
        }
        public void setMSGID(String value) {
        this.MSGID = value;
        }
        public String getCLIENTDT(){
        return CLIENTDT;
        }
        public void setCLIENTDT(String value) {
        this.CLIENTDT = value;
        }
        public String getSYSTEMID(){
        return SYSTEMID;
        }
        public void setSYSTEMID(String value) {
        this.SYSTEMID = value;
        }
        public String getSTATUSCODE(){
        return STATUSCODE;
        }
        public void setSTATUSCODE(String value) {
        this.STATUSCODE = value;
        }
        public String getSEVERITY(){
        return SEVERITY;
        }
        public void setSEVERITY(String value) {
        this.SEVERITY = value;
        }
        public String getSTATUSDESC(){
        return STATUSDESC;
        }
        public void setSTATUSDESC(String value) {
        this.STATUSDESC = value;
        }
}

@XStreamAlias("SvcRs")
public static class SEND_EATM_FC2_FR2_Body_MsgRs_SvcRs {
            private String OUTDATA = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 5)
            private String WSID = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 1)
            private String S1 = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 1)
            private String RECFMT = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 1)
            private String POAPUSE = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 1)
            private String MSGCAT = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String MSGTYP = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 6)
            private String TRANDATE = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 6)
            private String TRANTIME = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 4)
            private String TRANSEQ = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String TDRSEG = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 1)
            private String CARDACT = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String R0 = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 3)
            private String R0LEN = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S0 = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 5)
            private String S0LEN = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String ACT_COUNT = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String ACT_LENGTH = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 463)
            private String ACTDATA = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 8)
            private String MACCODE = StringUtils.EMPTY;

        public String getOUTDATA(){
        return OUTDATA;
        }
        public void setOUTDATA(String value) {
        this.OUTDATA = value;
        }
        public String getWSID(){
        return WSID;
        }
        public void setWSID(String value) {
        this.WSID = value;
        }
        public String getS1(){
        return S1;
        }
        public void setS1(String value) {
        this.S1 = value;
        }
        public String getRECFMT(){
        return RECFMT;
        }
        public void setRECFMT(String value) {
        this.RECFMT = value;
        }
        public String getPOAPUSE(){
        return POAPUSE;
        }
        public void setPOAPUSE(String value) {
        this.POAPUSE = value;
        }
        public String getMSGCAT(){
        return MSGCAT;
        }
        public void setMSGCAT(String value) {
        this.MSGCAT = value;
        }
        public String getMSGTYP(){
        return MSGTYP;
        }
        public void setMSGTYP(String value) {
        this.MSGTYP = value;
        }
        public String getTRANDATE(){
        return TRANDATE;
        }
        public void setTRANDATE(String value) {
        this.TRANDATE = value;
        }
        public String getTRANTIME(){
        return TRANTIME;
        }
        public void setTRANTIME(String value) {
        this.TRANTIME = value;
        }
        public String getTRANSEQ(){
        return TRANSEQ;
        }
        public void setTRANSEQ(String value) {
        this.TRANSEQ = value;
        }
        public String getTDRSEG(){
        return TDRSEG;
        }
        public void setTDRSEG(String value) {
        this.TDRSEG = value;
        }
        public String getCARDACT(){
        return CARDACT;
        }
        public void setCARDACT(String value) {
        this.CARDACT = value;
        }
        public String getR0(){
        return R0;
        }
        public void setR0(String value) {
        this.R0 = value;
        }
        public String getR0LEN(){
        return R0LEN;
        }
        public void setR0LEN(String value) {
        this.R0LEN = value;
        }
        public String getS0(){
        return S0;
        }
        public void setS0(String value) {
        this.S0 = value;
        }
        public String getS0LEN(){
        return S0LEN;
        }
        public void setS0LEN(String value) {
        this.S0LEN = value;
        }
        public String getACT_COUNT(){
        return ACT_COUNT;
        }
        public void setACT_COUNT(String value) {
        this.ACT_COUNT = value;
        }
        public String getACT_LENGTH(){
        return ACT_LENGTH;
        }
        public void setACT_LENGTH(String value) {
        this.ACT_LENGTH = value;
        }
        public String getACTDATA(){
        return ACTDATA;
        }
        public void setACTDATA(String value) {
        this.ACTDATA = value;
        }
        public String getMACCODE(){
        return MACCODE;
        }
        public void setMACCODE(String value) {
        this.MACCODE = value;
        }
public String makeMessage() {
return "" //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getWSID(), 5) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS1(), 1) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getRECFMT(), 1) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPOAPUSE(), 1) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMSGCAT(), 1) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMSGTYP(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRANDATE(), 6) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRANTIME(), 6) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRANSEQ(), 4) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTDRSEG(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCARDACT(), 1) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getR0(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getR0LEN(), 3) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS0(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS0LEN(), 5) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getACT_COUNT(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getACT_LENGTH(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getACTDATA(), 463) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMACCODE(), 8) //
;
}
}

@Override
public void parseMessage(String data) throws Exception {
}


}
