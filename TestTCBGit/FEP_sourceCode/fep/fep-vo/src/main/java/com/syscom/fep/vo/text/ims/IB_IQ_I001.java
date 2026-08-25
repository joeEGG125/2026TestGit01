package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class IB_IQ_I001 extends IMSTextBase {
    @Field(length = 8)
        private String IMS_TRANS = StringUtils.EMPTY;

    @Field(length = 4)
        private String SYSCODE = StringUtils.EMPTY;

    @Field(length = 14)
        private String SYS_DATETIME = StringUtils.EMPTY;

    @Field(length = 8)
        private String FEP_EJNO = StringUtils.EMPTY;

    @Field(length = 1)
        private String TXN_FLOW = StringUtils.EMPTY;

    @Field(length = 2)
        private String MSG_CAT = StringUtils.EMPTY;

    @Field(length = 3)
        private String SOURCE_CHANNEL = StringUtils.EMPTY;

    @Field(length = 4)
        private String PCODE = StringUtils.EMPTY;

    @Field(length = 2)
        private String FSCODE = StringUtils.EMPTY;

    @Field(length = 4)
        private String PROCESS_TYPE = StringUtils.EMPTY;

    @Field(length = 7)
        private String BUSINESS_DATE = StringUtils.EMPTY;

    @Field(length = 3)
        private String ACQUIRER_BANK = StringUtils.EMPTY;

    @Field(length = 7)
        private String TXNSTAN = StringUtils.EMPTY;

    @Field(length = 8)
        private String TERMINALID = StringUtils.EMPTY;

    @Field(length = 4)
        private String TERMINAL_TYPE = StringUtils.EMPTY;

    @Field(length = 3)
        private String CARDISSUE_BANK = StringUtils.EMPTY;

    @Field(length = 1)
        private String CARDTYPE = StringUtils.EMPTY;

    @Field(length = 4)
        private String RESPONSE_CODE = StringUtils.EMPTY;

    @Field(length = 4)
        private String ATM_TRANSEQ = StringUtils.EMPTY;

    @Field(length = 25)
        private String HRVS = StringUtils.EMPTY;

    @Field(length = 4)
        private String IMS_MAC = StringUtils.EMPTY;

    @Field(length = 8)
        private String ICCHIPSTAN = StringUtils.EMPTY;

    @Field(length = 8)
        private String TERM_CHECKNO = StringUtils.EMPTY;

    @Field(length = 14)
        private String TERMTXN_DATETIME = StringUtils.EMPTY;

    @Field(length = 30)
        private String ICMEMO = StringUtils.EMPTY;

    @Field(length = 10)
        private String TXNICCTAC = StringUtils.EMPTY;

    @Field(length = 14)
        private BigDecimal TXNAMT;

    @Field(length = 16)
        private String FROMACT = StringUtils.EMPTY;

    @Field(length = 3)
        private String BIT35_JRNATION = StringUtils.EMPTY;

    @Field(length = 3)
        private String BIT35_JRCCD = StringUtils.EMPTY;

    @Field(length = 7)
        private BigDecimal BIT35_JTAIFEE;

    @Field(length = 14)
        private BigDecimal BIT35_WAMTDISP;

    @Field(length = 353)
        private String DRVS = StringUtils.EMPTY;

        public String getIMS_TRANS(){
        return this.IMS_TRANS;
        }
        public void setIMS_TRANS(String IMS_TRANS){
        this.IMS_TRANS = IMS_TRANS;
        }
        public String getSYSCODE(){
        return this.SYSCODE;
        }
        public void setSYSCODE(String SYSCODE){
        this.SYSCODE = SYSCODE;
        }
        public String getSYS_DATETIME(){
        return this.SYS_DATETIME;
        }
        public void setSYS_DATETIME(String SYS_DATETIME){
        this.SYS_DATETIME = SYS_DATETIME;
        }
        public String getFEP_EJNO(){
        return this.FEP_EJNO;
        }
        public void setFEP_EJNO(String FEP_EJNO){
        this.FEP_EJNO = FEP_EJNO;
        }
        public String getTXN_FLOW(){
        return this.TXN_FLOW;
        }
        public void setTXN_FLOW(String TXN_FLOW){
        this.TXN_FLOW = TXN_FLOW;
        }
        public String getMSG_CAT(){
        return this.MSG_CAT;
        }
        public void setMSG_CAT(String MSG_CAT){
        this.MSG_CAT = MSG_CAT;
        }
        public String getSOURCE_CHANNEL(){
        return this.SOURCE_CHANNEL;
        }
        public void setSOURCE_CHANNEL(String SOURCE_CHANNEL){
        this.SOURCE_CHANNEL = SOURCE_CHANNEL;
        }
        public String getPCODE(){
        return this.PCODE;
        }
        public void setPCODE(String PCODE){
        this.PCODE = PCODE;
        }
        public String getFSCODE(){
        return this.FSCODE;
        }
        public void setFSCODE(String FSCODE){
        this.FSCODE = FSCODE;
        }
        public String getPROCESS_TYPE(){
        return this.PROCESS_TYPE;
        }
        public void setPROCESS_TYPE(String PROCESS_TYPE){
        this.PROCESS_TYPE = PROCESS_TYPE;
        }
        public String getBUSINESS_DATE(){
        return this.BUSINESS_DATE;
        }
        public void setBUSINESS_DATE(String BUSINESS_DATE){
        this.BUSINESS_DATE = BUSINESS_DATE;
        }
        public String getACQUIRER_BANK(){
        return this.ACQUIRER_BANK;
        }
        public void setACQUIRER_BANK(String ACQUIRER_BANK){
        this.ACQUIRER_BANK = ACQUIRER_BANK;
        }
        public String getTXNSTAN(){
        return this.TXNSTAN;
        }
        public void setTXNSTAN(String TXNSTAN){
        this.TXNSTAN = TXNSTAN;
        }
        public String getTERMINALID(){
        return this.TERMINALID;
        }
        public void setTERMINALID(String TERMINALID){
        this.TERMINALID = TERMINALID;
        }
        public String getTERMINAL_TYPE(){
        return this.TERMINAL_TYPE;
        }
        public void setTERMINAL_TYPE(String TERMINAL_TYPE){
        this.TERMINAL_TYPE = TERMINAL_TYPE;
        }
        public String getCARDISSUE_BANK(){
        return this.CARDISSUE_BANK;
        }
        public void setCARDISSUE_BANK(String CARDISSUE_BANK){
        this.CARDISSUE_BANK = CARDISSUE_BANK;
        }
        public String getCARDTYPE(){
        return this.CARDTYPE;
        }
        public void setCARDTYPE(String CARDTYPE){
        this.CARDTYPE = CARDTYPE;
        }
        public String getRESPONSE_CODE(){
        return this.RESPONSE_CODE;
        }
        public void setRESPONSE_CODE(String RESPONSE_CODE){
        this.RESPONSE_CODE = RESPONSE_CODE;
        }
        public String getATM_TRANSEQ(){
        return this.ATM_TRANSEQ;
        }
        public void setATM_TRANSEQ(String ATM_TRANSEQ){
        this.ATM_TRANSEQ = ATM_TRANSEQ;
        }
        public String getHRVS(){
        return this.HRVS;
        }
        public void setHRVS(String HRVS){
        this.HRVS = HRVS;
        }
        public String getIMS_MAC(){
        return this.IMS_MAC;
        }
        public void setIMS_MAC(String IMS_MAC){
        this.IMS_MAC = IMS_MAC;
        }
        public String getICCHIPSTAN(){
        return this.ICCHIPSTAN;
        }
        public void setICCHIPSTAN(String ICCHIPSTAN){
        this.ICCHIPSTAN = ICCHIPSTAN;
        }
        public String getTERM_CHECKNO(){
        return this.TERM_CHECKNO;
        }
        public void setTERM_CHECKNO(String TERM_CHECKNO){
        this.TERM_CHECKNO = TERM_CHECKNO;
        }
        public String getTERMTXN_DATETIME(){
        return this.TERMTXN_DATETIME;
        }
        public void setTERMTXN_DATETIME(String TERMTXN_DATETIME){
        this.TERMTXN_DATETIME = TERMTXN_DATETIME;
        }
        public String getICMEMO(){
        return this.ICMEMO;
        }
        public void setICMEMO(String ICMEMO){
        this.ICMEMO = ICMEMO;
        }
        public String getTXNICCTAC(){
        return this.TXNICCTAC;
        }
        public void setTXNICCTAC(String TXNICCTAC){
        this.TXNICCTAC = TXNICCTAC;
        }
        public BigDecimal getTXNAMT(){
        return this.TXNAMT;
        }
        public void setTXNAMT(BigDecimal TXNAMT){
        this.TXNAMT = TXNAMT;
        }
        public String getFROMACT(){
        return this.FROMACT;
        }
        public void setFROMACT(String FROMACT){
        this.FROMACT = FROMACT;
        }
        public String getBIT35_JRNATION(){
        return this.BIT35_JRNATION;
        }
        public void setBIT35_JRNATION(String BIT35_JRNATION){
        this.BIT35_JRNATION = BIT35_JRNATION;
        }
        public String getBIT35_JRCCD(){
        return this.BIT35_JRCCD;
        }
        public void setBIT35_JRCCD(String BIT35_JRCCD){
        this.BIT35_JRCCD = BIT35_JRCCD;
        }
        public BigDecimal getBIT35_JTAIFEE(){
        return this.BIT35_JTAIFEE;
        }
        public void setBIT35_JTAIFEE(BigDecimal BIT35_JTAIFEE){
        this.BIT35_JTAIFEE = BIT35_JTAIFEE;
        }
        public BigDecimal getBIT35_WAMTDISP(){
        return this.BIT35_WAMTDISP;
        }
        public void setBIT35_WAMTDISP(BigDecimal BIT35_WAMTDISP){
        this.BIT35_WAMTDISP = BIT35_WAMTDISP;
        }
        public String getDRVS(){
        return this.DRVS;
        }
        public void setDRVS(String DRVS){
        this.DRVS = DRVS;
        }

public void parseCbsTele(String tita) throws ParseException{
        this.setIMS_TRANS(EbcdicConverter.fromHex(CCSID.English,tita.substring(0, 16)));
        this.setSYSCODE(EbcdicConverter.fromHex(CCSID.English,tita.substring(16, 24)));
        this.setSYS_DATETIME(EbcdicConverter.fromHex(CCSID.English,tita.substring(24, 52)));
        this.setFEP_EJNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(52, 68)));
        this.setTXN_FLOW(EbcdicConverter.fromHex(CCSID.English,tita.substring(68, 70)));
        this.setMSG_CAT(EbcdicConverter.fromHex(CCSID.English,tita.substring(70, 74)));
        this.setSOURCE_CHANNEL(EbcdicConverter.fromHex(CCSID.English,tita.substring(74, 80)));
        this.setPCODE(EbcdicConverter.fromHex(CCSID.English,tita.substring(80, 88)));
        this.setFSCODE(EbcdicConverter.fromHex(CCSID.English,tita.substring(88, 92)));
        this.setPROCESS_TYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(92, 100)));
        this.setBUSINESS_DATE(EbcdicConverter.fromHex(CCSID.English,tita.substring(100, 114)));
        this.setACQUIRER_BANK(EbcdicConverter.fromHex(CCSID.English,tita.substring(114, 120)));
        this.setTXNSTAN(EbcdicConverter.fromHex(CCSID.English,tita.substring(120, 134)));
        this.setTERMINALID(EbcdicConverter.fromHex(CCSID.English,tita.substring(134, 150)));
        this.setTERMINAL_TYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(150, 158)));
        this.setCARDISSUE_BANK(EbcdicConverter.fromHex(CCSID.English,tita.substring(158, 164)));
        this.setCARDTYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(164, 166)));
        this.setRESPONSE_CODE(EbcdicConverter.fromHex(CCSID.English,tita.substring(166, 174)));
        this.setATM_TRANSEQ(EbcdicConverter.fromHex(CCSID.English,tita.substring(174, 182)));
        this.setHRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(182, 232)));
        this.setIMS_MAC(EbcdicConverter.fromHex(CCSID.English,tita.substring(232, 240)));
        this.setICCHIPSTAN(EbcdicConverter.fromHex(CCSID.English,tita.substring(240, 256)));
        this.setTERM_CHECKNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(256, 272)));
        this.setTERMTXN_DATETIME(EbcdicConverter.fromHex(CCSID.English,tita.substring(272, 300)));
        this.setICMEMO(EbcdicConverter.fromHex(CCSID.English,tita.substring(300, 360)));
        this.setTXNICCTAC(EbcdicConverter.fromHex(CCSID.English,tita.substring(360, 380)));
        this.setTXNAMT(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(380, 408).trim()), false, 2));
        this.setFROMACT(EbcdicConverter.fromHex(CCSID.English,tita.substring(408, 440)));
        this.setBIT35_JRNATION(EbcdicConverter.fromHex(CCSID.English,tita.substring(440, 446)));
        this.setBIT35_JRCCD(EbcdicConverter.fromHex(CCSID.English,tita.substring(446, 452)));
        this.setBIT35_JTAIFEE(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(452, 466).trim()), false, 0));
        this.setBIT35_WAMTDISP(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(466, 494).trim()), false, 2));
        this.setDRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(494, 1200)));
}

public String makeMessage() {
return "" //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_TRANS(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSYSCODE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSYS_DATETIME(), 14) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFEP_EJNO(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTXN_FLOW(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMSG_CAT(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSOURCE_CHANNEL(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPCODE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFSCODE(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPROCESS_TYPE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getBUSINESS_DATE(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getACQUIRER_BANK(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTXNSTAN(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERMINALID(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERMINAL_TYPE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCARDISSUE_BANK(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCARDTYPE(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getRESPONSE_CODE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getATM_TRANSEQ(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getHRVS(), 25) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_MAC(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getICCHIPSTAN(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERM_CHECKNO(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERMTXN_DATETIME(), 14) //
        + this.getICMEMO()  //
        + this.getTXNICCTAC()  //
            + CodeGenUtil.bigDecimalToEbcdic(this.getTXNAMT(), 11, false, 2, true) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFROMACT(), 16) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getBIT35_JRNATION(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getBIT35_JRCCD(), 3) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getBIT35_JTAIFEE(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getBIT35_WAMTDISP(), 11, false, 2, true) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 353) //
;
}

public String makeMessageAscii() {
return "" //
            + StringUtils.leftPad(this.getIMS_TRANS(), 8," ") //
            + StringUtils.leftPad(this.getSYSCODE(), 4," ") //
            + StringUtils.leftPad(this.getSYS_DATETIME(), 14," ") //
            + StringUtils.leftPad(this.getFEP_EJNO(), 8," ") //
            + StringUtils.leftPad(this.getTXN_FLOW(), 1," ") //
            + StringUtils.leftPad(this.getMSG_CAT(), 2," ") //
            + StringUtils.leftPad(this.getSOURCE_CHANNEL(), 3," ") //
            + StringUtils.leftPad(this.getPCODE(), 4," ") //
            + StringUtils.leftPad(this.getFSCODE(), 2," ") //
            + StringUtils.leftPad(this.getPROCESS_TYPE(), 4," ") //
            + StringUtils.leftPad(this.getBUSINESS_DATE(), 7," ") //
            + StringUtils.leftPad(this.getACQUIRER_BANK(), 3," ") //
            + StringUtils.leftPad(this.getTXNSTAN(), 7," ") //
            + StringUtils.leftPad(this.getTERMINALID(), 8," ") //
            + StringUtils.leftPad(this.getTERMINAL_TYPE(), 4," ") //
            + StringUtils.leftPad(this.getCARDISSUE_BANK(), 3," ") //
            + StringUtils.leftPad(this.getCARDTYPE(), 1," ") //
            + StringUtils.leftPad(this.getRESPONSE_CODE(), 4," ") //
            + StringUtils.leftPad(this.getATM_TRANSEQ(), 4," ") //
            + StringUtils.leftPad(this.getHRVS(), 25," ") //
            + StringUtils.leftPad(this.getIMS_MAC(), 4," ") //
            + StringUtils.leftPad(this.getICCHIPSTAN(), 8," ") //
            + StringUtils.leftPad(this.getTERM_CHECKNO(), 8," ") //
            + StringUtils.leftPad(this.getTERMTXN_DATETIME(), 14," ") //
        + this.getICMEMO()  //
        + this.getTXNICCTAC()  //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getTXNAMT(), 11, false, 2, true) //
            + StringUtils.leftPad(this.getFROMACT(), 16," ") //
            + StringUtils.leftPad(this.getBIT35_JRNATION(), 3," ") //
            + StringUtils.leftPad(this.getBIT35_JRCCD(), 3," ") //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getBIT35_JTAIFEE(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getBIT35_WAMTDISP(), 11, false, 2, true) //
            + StringUtils.leftPad(this.getDRVS(), 353," ") //
;
}
}
