package com.syscom.fep.vo.text.webatm;

import java.math.BigDecimal;

import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.CodeGenUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("SOAP-ENV:Envelope")
public class RCV_EATM_GeneralTrans_RQ extends WebatmXmlBase {
    @XStreamAsAttribute
    @XStreamAlias("xmlns:SOAP-ENV")
    private String xmlnsSoapEnv;
    @XStreamAsAttribute
    @XStreamAlias("xmlns:xsi")
    private String xmlnsXsi;
    @XStreamAlias("SOAP-ENV:Header")
    private RCV_EATM_GeneralTrans_RQ_Header header;
    @XStreamAlias("SOAP-ENV:Body")
    private RCV_EATM_GeneralTrans_RQ_Body body;

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
    public RCV_EATM_GeneralTrans_RQ_Header getHeader() {
        return header;
    }
    public void setHeader(RCV_EATM_GeneralTrans_RQ_Header header) {
        this.header = header;
    }
    public RCV_EATM_GeneralTrans_RQ_Body getBody() {
        return body;
    }
    public void setBody(RCV_EATM_GeneralTrans_RQ_Body body) {
        this.body = body;
    }
    @XStreamAlias("SOAP-ENV:Header")
    public static class RCV_EATM_GeneralTrans_RQ_Header {
    }
    @XStreamAlias("SOAP-ENV:Body")
    public static class RCV_EATM_GeneralTrans_RQ_Body {
        @XStreamAlias("esb:MsgRq")
        private RCV_EATM_GeneralTrans_RQ_Body_EsbMsgRq rq;
        public RCV_EATM_GeneralTrans_RQ_Body_EsbMsgRq getRq() {
            return rq;
        }
        public void setRq(RCV_EATM_GeneralTrans_RQ_Body_EsbMsgRq rq) {
            this.rq = rq;
        }
    }
    @XStreamAlias("esb:MsgRq")
    public static class RCV_EATM_GeneralTrans_RQ_Body_EsbMsgRq {
        @XStreamAlias("Header")
        private RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header header;
        @XStreamAlias("SvcRq")
        private RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq svcRq;
        public RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header getHeader() {
            return header;
        }
        public void setHeader(RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header header) {
            this.header = header;
        }
        public RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq getSvcRq() {
            return svcRq;
        }
        public void setSvcRq(RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq svcRq) {
            this.svcRq = svcRq;
        }
    }

    @XStreamAlias("Header")
    public static class RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header {
        private String CLIENTTRACEID;
        private String CHANNEL;
        private String MSGID;
        private String MSGKIND;
        private String TXNID;
        private String BRANCHID;
        private String TERMID;
        private String CLIENTDT;

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
        public String getMSGKIND(){
            return MSGKIND;
        }
        public void setMSGKIND(String value) {
            this.MSGKIND = value;
        }
        public String getTXNID(){
            return TXNID;
        }
        public void setTXNID(String value) {
            this.TXNID = value;
        }
        public String getBRANCHID(){
            return BRANCHID;
        }
        public void setBRANCHID(String value) {
            this.BRANCHID = value;
        }
        public String getTERMID(){
            return TERMID;
        }
        public void setTERMID(String value) {
            this.TERMID = value;
        }
        public String getCLIENTDT(){
            return CLIENTDT;
        }
        public void setCLIENTDT(String value) {
            this.CLIENTDT = value;
        }
    }

    @XStreamAlias("SvcRq")
    public static class RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq {
        private String ATMDATA = StringUtils.EMPTY;
        private String APTRAN = StringUtils.EMPTY;
        private String FILLER1 = StringUtils.EMPTY;
        private String WSID = StringUtils.EMPTY;
        private String MACMODE = StringUtils.EMPTY;
        private String RECFMT = StringUtils.EMPTY;
        private String APPLUSE = StringUtils.EMPTY;
        private String MSGCAT = StringUtils.EMPTY;
        private String MSGTYP = StringUtils.EMPTY;
        private String TRANDATE = StringUtils.EMPTY;
        private String TRANTIME = StringUtils.EMPTY;
        private String TRANSEQ = StringUtils.EMPTY;
        private String TDRSEG = StringUtils.EMPTY;
        private String STATUS = StringUtils.EMPTY;
        private String IPYDATA = StringUtils.EMPTY;
        private String LANGID = StringUtils.EMPTY;
        private String FSCODE = StringUtils.EMPTY;
        private String FADATA = StringUtils.EMPTY;
        private String TACODE = StringUtils.EMPTY;
        private String TADATA = StringUtils.EMPTY;
        private String AMTNOND = StringUtils.EMPTY;
        private String AMTDISP = StringUtils.EMPTY;
        private String DOCLASS = StringUtils.EMPTY;
        private String CARDFMT = StringUtils.EMPTY;
        private String CARDDATA = StringUtils.EMPTY;
        private String PICCDID = StringUtils.EMPTY;
        private String FILLER2 = StringUtils.EMPTY;
        private String PICCDLTH = StringUtils.EMPTY;
        private String PICCPCOD = StringUtils.EMPTY;
        private String PICCTXID = StringUtils.EMPTY;
        private String PICCBI9 = StringUtils.EMPTY;
        private String PICCBI11 = StringUtils.EMPTY;
        private String PICCBI19 = StringUtils.EMPTY;
        private String PICCBI28 = StringUtils.EMPTY;
        private String PICCBI55 = StringUtils.EMPTY;
        private String SPECIALDATA = StringUtils.EMPTY;
        private String PIEODT = StringUtils.EMPTY;
        private String FILLER4 = StringUtils.EMPTY;
        private String PIEOSTAN = StringUtils.EMPTY;
        private String FILLER5 = StringUtils.EMPTY;
        private String PIETNOTE = StringUtils.EMPTY;
        private String PIEFNOTE = StringUtils.EMPTY;
        private String FILLER6 = StringUtils.EMPTY;
        private String PICCMACD = StringUtils.EMPTY;
        private String PICCTAC = StringUtils.EMPTY;
        private String FILLER3 = StringUtils.EMPTY;

        public String getATMDATA(){
            return this.ATMDATA;
        }
        public void setATMDATA(String value) {
            this.ATMDATA = value;
        }
        public String getAPTRAN(){
            return this.APTRAN;
        }
        public void setAPTRAN(String value) {
            this.APTRAN = value;
        }
        public String getFILLER1(){
            return this.FILLER1;
        }
        public void setFILLER1(String value) {
            this.FILLER1 = value;
        }
        public String getWSID(){
            return this.WSID;
        }
        public void setWSID(String value) {
            this.WSID = value;
        }
        public String getMACMODE(){
            return this.MACMODE;
        }
        public void setMACMODE(String value) {
            this.MACMODE = value;
        }
        public String getRECFMT(){
            return this.RECFMT;
        }
        public void setRECFMT(String value) {
            this.RECFMT = value;
        }
        public String getAPPLUSE(){
            return this.APPLUSE;
        }
        public void setAPPLUSE(String value) {
            this.APPLUSE = value;
        }
        public String getMSGCAT(){
            return this.MSGCAT;
        }
        public void setMSGCAT(String value) {
            this.MSGCAT = value;
        }
        public String getMSGTYP(){
            return this.MSGTYP;
        }
        public void setMSGTYP(String value) {
            this.MSGTYP = value;
        }
        public String getTRANDATE(){
            return this.TRANDATE;
        }
        public void setTRANDATE(String value) {
            this.TRANDATE = value;
        }
        public String getTRANTIME(){
            return this.TRANTIME;
        }
        public void setTRANTIME(String value) {
            this.TRANTIME = value;
        }
        public String getTRANSEQ(){
            return this.TRANSEQ;
        }
        public void setTRANSEQ(String value) {
            this.TRANSEQ = value;
        }
        public String getTDRSEG(){
            return this.TDRSEG;
        }
        public void setTDRSEG(String value) {
            this.TDRSEG = value;
        }
        public String getSTATUS(){
            return this.STATUS;
        }
        public void setSTATUS(String value) {
            this.STATUS = value;
        }
        public String getIPYDATA(){
            return this.IPYDATA;
        }
        public void setIPYDATA(String value) {
            this.IPYDATA = value;
        }
        public String getLANGID(){
            return this.LANGID;
        }
        public void setLANGID(String value) {
            this.LANGID = value;
        }
        public String getFSCODE(){
            return this.FSCODE;
        }
        public void setFSCODE(String value) {
            this.FSCODE = value;
        }
        public String getFADATA(){
            return this.FADATA;
        }
        public void setFADATA(String value) {
            this.FADATA = value;
        }
        public String getTACODE(){
            return this.TACODE;
        }
        public void setTACODE(String value) {
            this.TACODE = value;
        }
        public String getTADATA(){
            return this.TADATA;
        }
        public void setTADATA(String value) {
            this.TADATA = value;
        }
        public BigDecimal getAMTNOND(){
            return CodeGenUtil.asciiToBigDecimal(this.AMTNOND, false, 0);
        }
        public void setAMTNOND(BigDecimal value) {
            this.AMTNOND = CodeGenUtil.bigDecimalToAscii(value, 0, false, 0, false);
        }
        public BigDecimal getAMTDISP(){
            return CodeGenUtil.asciiToBigDecimal(this.AMTDISP, false, 0);
        }
        public void setAMTDISP(BigDecimal value) {
            this.AMTDISP = CodeGenUtil.bigDecimalToAscii(value, 0, false, 0, false);
        }
        public String getDOCLASS(){
            return this.DOCLASS;
        }
        public void setDOCLASS(String value) {
            this.DOCLASS = value;
        }
        public String getCARDFMT(){
            return this.CARDFMT;
        }
        public void setCARDFMT(String value) {
            this.CARDFMT = value;
        }
        public String getCARDDATA(){
            return this.CARDDATA;
        }
        public void setCARDDATA(String value) {
            this.CARDDATA = value;
        }
        public String getPICCDID(){
            return this.PICCDID;
        }
        public void setPICCDID(String value) {
            this.PICCDID = value;
        }
        public String getFILLER2(){
            return this.FILLER2;
        }
        public void setFILLER2(String value) {
            this.FILLER2 = value;
        }
        public String getPICCDLTH(){
            return this.PICCDLTH;
        }
        public void setPICCDLTH(String value) {
            this.PICCDLTH = value;
        }
        public String getPICCPCOD(){
            return this.PICCPCOD;
        }
        public void setPICCPCOD(String value) {
            this.PICCPCOD = value;
        }
        public String getPICCTXID(){
            return this.PICCTXID;
        }
        public void setPICCTXID(String value) {
            this.PICCTXID = value;
        }
        public String getPICCBI9(){
            return this.PICCBI9;
        }
        public void setPICCBI9(String value) {
            this.PICCBI9 = value;
        }
        public String getPICCBI11(){
            return this.PICCBI11;
        }
        public void setPICCBI11(String value) {
            this.PICCBI11 = value;
        }
        public String getPICCBI19(){
            return this.PICCBI19;
        }
        public void setPICCBI19(String value) {
            this.PICCBI19 = value;
        }
        public String getPICCBI28(){
            return this.PICCBI28;
        }
        public void setPICCBI28(String value) {
            this.PICCBI28 = value;
        }
        public String getPICCBI55(){
            return this.PICCBI55;
        }
        public void setPICCBI55(String value) {
            this.PICCBI55 = value;
        }
        public String getSPECIALDATA(){
            return this.SPECIALDATA;
        }
        public void setSPECIALDATA(String value) {
            this.SPECIALDATA = value;
        }
        public String getPIEODT(){
            return this.PIEODT;
        }
        public void setPIEODT(String value) {
            this.PIEODT = value;
        }
        public String getFILLER4(){
            return this.FILLER4;
        }
        public void setFILLER4(String value) {
            this.FILLER4 = value;
        }
        public String getPIEOSTAN(){
            return this.PIEOSTAN;
        }
        public void setPIEOSTAN(String value) {
            this.PIEOSTAN = value;
        }
        public String getFILLER5(){
            return this.FILLER5;
        }
        public void setFILLER5(String value) {
            this.FILLER5 = value;
        }
        public String getPIETNOTE(){
            return this.PIETNOTE;
        }
        public void setPIETNOTE(String value) {
            this.PIETNOTE = value;
        }
        public String getPIEFNOTE(){
            return this.PIEFNOTE;
        }
        public void setPIEFNOTE(String value) {
            this.PIEFNOTE = value;
        }
        public String getFILLER6(){
            return this.FILLER6;
        }
        public void setFILLER6(String value) {
            this.FILLER6 = value;
        }
        public String getPICCMACD(){
            return this.PICCMACD;
        }
        public void setPICCMACD(String value) {
            this.PICCMACD = value;
        }
        public String getPICCTAC(){
            return this.PICCTAC;
        }
        public void setPICCTAC(String value) {
            this.PICCTAC = value;
        }
        public String getFILLER3(){
            return this.FILLER3;
        }
        public void setFILLER3(String value) {
            this.FILLER3 = value;
        }
    }

    @Override
    public void parseMessage(String data) throws Exception {
    	RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq svcRq = this.getBody().getRq().getSvcRq();
        svcRq.APTRAN = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(0, 16));
        svcRq.FILLER1 = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(16, 18));
        svcRq.WSID = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(18, 28));
        svcRq.MACMODE = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(28, 30));
        svcRq.RECFMT = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(30, 32));
        svcRq.APPLUSE = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(32, 34));
        svcRq.MSGCAT = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(34, 36));
        svcRq.MSGTYP = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(36, 40));
        svcRq.TRANDATE = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(40, 52));
        svcRq.TRANTIME = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(52, 64));
        svcRq.TRANSEQ = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(64, 72));
        svcRq.TDRSEG = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(72, 76));
        svcRq.STATUS = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(76, 106));
        svcRq.IPYDATA = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(106, 142));
        svcRq.LANGID = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(142, 146));
        svcRq.FSCODE = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(146, 150));
        svcRq.FADATA = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(150, 194));
        svcRq.TACODE = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(194, 198));
        svcRq.TADATA = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(198, 238));
        svcRq.AMTNOND = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(238, 260));
        svcRq.AMTDISP = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(260, 280));
        svcRq.DOCLASS = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(280, 282));
        svcRq.CARDFMT = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(282, 284));
        svcRq.CARDDATA = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(284, 358));
        svcRq.PICCDID = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(358, 360));
        svcRq.FILLER2 = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(360, 362));
        svcRq.PICCDLTH = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(362, 368));
        svcRq.PICCPCOD = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(368, 376));
        svcRq.PICCTXID = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(376, 378));
        svcRq.PICCBI9 = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(378, 394));
        svcRq.PICCBI11 = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(394, 410));
        svcRq.PICCBI19 = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(410, 438));
        svcRq.PICCBI28 = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(438, 446));
        svcRq.PICCBI55 = data.substring(446, 506);
        svcRq.SPECIALDATA = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(506, 742));
        svcRq.PIEODT = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(506, 534));
        svcRq.FILLER4 = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(534, 598));
        svcRq.PIEOSTAN = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(598, 612));
        svcRq.FILLER5 = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(612, 656));
        svcRq.PIETNOTE = data.substring(656, 688);
        svcRq.PIEFNOTE = data.substring(688, 720);
        svcRq.FILLER6 = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(720, 742));
        svcRq.PICCMACD = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(742, 758));
        svcRq.PICCTAC = data.substring(758, 778);
        svcRq.FILLER3 = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(778, 1018));
    }
}
