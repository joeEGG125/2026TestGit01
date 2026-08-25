package com.syscom.fep.vo.text.webatm;
import com.syscom.fep.vo.CodeGenUtil;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.annotation.Field;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("soap:Envelope")
public class SEND_EATM_FAA_CC1B1PN extends WebatmXmlBase {
@XStreamAsAttribute
@XStreamAlias("xmlns:soap")
private String xmlnsSoap = "http://schemas.xmlsoap.org/soap/envelope/";
@XStreamAsAttribute
@XStreamAlias("xmlns:xsi")
private String xmlnsXsi;
@XStreamAlias("soap:Header")
private String header = StringUtils.EMPTY;
@XStreamAlias("soap:Body")
private SEND_EATM_FAA_CC1B1PN_Body body;

public SEND_EATM_FAA_CC1B1PN_Body getBody() {
return body;
}
public void setBody(SEND_EATM_FAA_CC1B1PN_Body body) {
this.body = body;
}
@XStreamAlias("soap:Body")
public static class SEND_EATM_FAA_CC1B1PN_Body {
@XStreamAlias("NS1:MsgRs")
private SEND_EATM_FAA_CC1B1PN_Body_NS1MsgRs rs;
public SEND_EATM_FAA_CC1B1PN_Body_NS1MsgRs getRs() {
return rs;
}
public void setRs(SEND_EATM_FAA_CC1B1PN_Body_NS1MsgRs rs) {
this.rs = rs;
}
}
@XStreamAlias("NS1:MsgRs")
public static class SEND_EATM_FAA_CC1B1PN_Body_NS1MsgRs {
@XStreamAsAttribute
@XStreamAlias("xmlns:NS1")
private String xmlnsNs1="http://www.ibm.com.tw/esb";
@XStreamAlias("Header")
private SEND_EATM_FAA_CC1B1PN_Body_MsgRs_Header header;
@XStreamAlias("SvcRs")
private SEND_EATM_FAA_CC1B1PN_Body_MsgRs_SvcRs svcRs;
public SEND_EATM_FAA_CC1B1PN_Body_MsgRs_Header getHeader() {
return header;
}
public void setHeader(SEND_EATM_FAA_CC1B1PN_Body_MsgRs_Header header) {
this.header = header;
}
public SEND_EATM_FAA_CC1B1PN_Body_MsgRs_SvcRs getSvcRs() {
return svcRs;
}
public void setSvcRq(SEND_EATM_FAA_CC1B1PN_Body_MsgRs_SvcRs svcRs) {
this.svcRs = svcRs;
}
}

@XStreamAlias("Header")
public static class SEND_EATM_FAA_CC1B1PN_Body_MsgRs_Header {
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
public static class SEND_EATM_FAA_CC1B1PN_Body_MsgRs_SvcRs {
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
            private String PTYPE = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 3)
            private String PLEN = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 6)
            private String PBMPNO = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 9)
            private String PDATE = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S2 =  ";/";
            @XStreamOmitField
            @Field(length = 8)
            private String PTIME = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S3 =  ";/";
            @XStreamOmitField
            @Field(length = 2)
            private String PTXTYPE = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S4 =  ";/";
            @XStreamOmitField
            @Field(length = 8)
            private String PTID = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S5 =  ";/";
            @XStreamOmitField
            @Field(length = 11)
            private String PTXAMT = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S6 =  ";/";
            @XStreamOmitField
            @Field(length = 4)
            private String PFEE = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S7 =  ";/";
            @XStreamOmitField
            @Field(length = 18)
            private String PBAL = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S8 =  ";/";
            @XStreamOmitField
            @Field(length = 16)
            private String PACCNO = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S9 =  ";/";
            @XStreamOmitField
            @Field(length = 16)
            private String PTRINACCT = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S10 =  ";/";
            @XStreamOmitField
            @Field(length = 3)
            private String PATXBKNO = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S11 =  ";/";
            @XStreamOmitField
            @Field(length = 7)
            private String PSTAN = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S12 =  ";/";
            @XStreamOmitField
            @Field(length = 8)
            private String PBUSINESSDATE = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S13 =  ";/";
            @XStreamOmitField
            @Field(length = 4)
            private String PRCCODE = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S14 =  ";/";
            @XStreamOmitField
            @Field(length = 2)
            private String PFILLER1 = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S15 =  ";/";
            @XStreamOmitField
            @Field(length = 3)
            private String POTXBKNO = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S16 =  ";/";
            @XStreamOmitField
            @Field(length = 3)
            private String PITXBKNO = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S17 =  ";/";
            @XStreamOmitField
            @Field(length = 11)
            private String PAUTHAMT = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S18 =  ";/";
            @XStreamOmitField
            @Field(length = 16)
            private String PARPC = StringUtils.EMPTY;
            @XStreamOmitField
            @Field(length = 2)
            private String S19 = ";#";
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
        public String getPTYPE(){
        return PTYPE;
        }
        public void setPTYPE(String value) {
        this.PTYPE = value;
        }
        public String getPLEN(){
        return PLEN;
        }
        public void setPLEN(String value) {
        this.PLEN = value;
        }
        public String getPBMPNO(){
        return PBMPNO;
        }
        public void setPBMPNO(String value) {
        this.PBMPNO = value;
        }
        public String getPDATE(){
        return PDATE;
        }
        public void setPDATE(String value) {
        this.PDATE = value;
        }
        public String getS2(){
        return S2;
        }
        public void setS2(String value) {
        this.S2 = value;
        }
        public String getPTIME(){
        return PTIME;
        }
        public void setPTIME(String value) {
        this.PTIME = value;
        }
        public String getS3(){
        return S3;
        }
        public void setS3(String value) {
        this.S3 = value;
        }
        public String getPTXTYPE(){
        return PTXTYPE;
        }
        public void setPTXTYPE(String value) {
        this.PTXTYPE = value;
        }
        public String getS4(){
        return S4;
        }
        public void setS4(String value) {
        this.S4 = value;
        }
        public String getPTID(){
        return PTID;
        }
        public void setPTID(String value) {
        this.PTID = value;
        }
        public String getS5(){
        return S5;
        }
        public void setS5(String value) {
        this.S5 = value;
        }
        public String getPTXAMT(){
        return PTXAMT;
        }
        public void setPTXAMT(String value) {
        this.PTXAMT = value;
        }
        public String getS6(){
        return S6;
        }
        public void setS6(String value) {
        this.S6 = value;
        }
        public String getPFEE(){
        return PFEE;
        }
        public void setPFEE(String value) {
        this.PFEE = value;
        }
        public String getS7(){
        return S7;
        }
        public void setS7(String value) {
        this.S7 = value;
        }
        public String getPBAL(){
        return PBAL;
        }
        public void setPBAL(String value) {
        this.PBAL = value;
        }
        public String getS8(){
        return S8;
        }
        public void setS8(String value) {
        this.S8 = value;
        }
        public String getPACCNO(){
        return PACCNO;
        }
        public void setPACCNO(String value) {
        this.PACCNO = value;
        }
        public String getS9(){
        return S9;
        }
        public void setS9(String value) {
        this.S9 = value;
        }
        public String getPTRINACCT(){
        return PTRINACCT;
        }
        public void setPTRINACCT(String value) {
        this.PTRINACCT = value;
        }
        public String getS10(){
        return S10;
        }
        public void setS10(String value) {
        this.S10 = value;
        }
        public String getPATXBKNO(){
        return PATXBKNO;
        }
        public void setPATXBKNO(String value) {
        this.PATXBKNO = value;
        }
        public String getS11(){
        return S11;
        }
        public void setS11(String value) {
        this.S11 = value;
        }
        public String getPSTAN(){
        return PSTAN;
        }
        public void setPSTAN(String value) {
        this.PSTAN = value;
        }
        public String getS12(){
        return S12;
        }
        public void setS12(String value) {
        this.S12 = value;
        }
        public String getPBUSINESSDATE(){
        return PBUSINESSDATE;
        }
        public void setPBUSINESSDATE(String value) {
        this.PBUSINESSDATE = value;
        }
        public String getS13(){
        return S13;
        }
        public void setS13(String value) {
        this.S13 = value;
        }
        public String getPRCCODE(){
        return PRCCODE;
        }
        public void setPRCCODE(String value) {
        this.PRCCODE = value;
        }
        public String getS14(){
        return S14;
        }
        public void setS14(String value) {
        this.S14 = value;
        }
        public String getPFILLER1(){
        return PFILLER1;
        }
        public void setPFILLER1(String value) {
        this.PFILLER1 = value;
        }
        public String getS15(){
        return S15;
        }
        public void setS15(String value) {
        this.S15 = value;
        }
        public String getPOTXBKNO(){
        return POTXBKNO;
        }
        public void setPOTXBKNO(String value) {
        this.POTXBKNO = value;
        }
        public String getS16(){
        return S16;
        }
        public void setS16(String value) {
        this.S16 = value;
        }
        public String getPITXBKNO(){
        return PITXBKNO;
        }
        public void setPITXBKNO(String value) {
        this.PITXBKNO = value;
        }
        public String getS17(){
        return S17;
        }
        public void setS17(String value) {
        this.S17 = value;
        }
        public String getPAUTHAMT(){
        return PAUTHAMT;
        }
        public void setPAUTHAMT(String value) {
        this.PAUTHAMT = value;
        }
        public String getS18(){
        return S18;
        }
        public void setS18(String value) {
        this.S18 = value;
        }
        public String getPARPC(){
        return PARPC;
        }
        public void setPARPC(String value) {
        this.PARPC = value;
        }
        public String getS19(){
        return S19;
        }
        public void setS19(String value) {
        this.S19 = value;
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
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPTYPE(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPLEN(), 3) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPBMPNO(), 6) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPDATE(), 9) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS2(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPTIME(), 8) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS3(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPTXTYPE(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS4(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPTID(), 8) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS5(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPTXAMT(), 11) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS6(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPFEE(), 4) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS7(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPBAL(), 18) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS8(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPACCNO(), 16) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS9(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPTRINACCT(), 16) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS10(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPATXBKNO(), 3) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS11(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPSTAN(), 7) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS12(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPBUSINESSDATE(), 8) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS13(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPRCCODE(), 4) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS14(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPFILLER1(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS15(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPOTXBKNO(), 3) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS16(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPITXBKNO(), 3) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS17(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPAUTHAMT(), 11) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS18(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPARPC(), 16) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS19(), 2) //
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMACCODE(), 8) //
;
}
}

@Override
public void parseMessage(String data) throws Exception {
}


}
