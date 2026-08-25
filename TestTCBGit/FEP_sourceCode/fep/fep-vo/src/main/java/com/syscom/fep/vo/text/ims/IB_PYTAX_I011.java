package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class IB_PYTAX_I011 extends IMSTextBase {
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

    @Field(length = 7)
        private String Transferee_Bank_ID = StringUtils.EMPTY;

    @Field(length = 7)
        private String Transferor_Bank_ID = StringUtils.EMPTY;

    @Field(length = 16)
        private String TOACT = StringUtils.EMPTY;

    @Field(length = 5)
        private String TAX_TYPE = StringUtils.EMPTY;

    @Field(length = 6)
        private String TAX_END_DATE = StringUtils.EMPTY;

    @Field(length = 3)
        private String TAX_organ = StringUtils.EMPTY;

    @Field(length = 11)
        private String TAX_CID = StringUtils.EMPTY;

    @Field(length = 16)
        private String TAX_BILL_NO = StringUtils.EMPTY;

    @Field(length = 2)
        private String TAX_STATUS = StringUtils.EMPTY;

    @Field(length = 4)
        private String TAX_CHARGCUS = StringUtils.EMPTY;

    @Field(length = 20)
        private String TAX_CHARGUNT = StringUtils.EMPTY;

    @Field(length = 40)
        private String TAX_MEMO = StringUtils.EMPTY;

    @Field(length = 243)
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
        public String getTransferee_Bank_ID(){
        return this.Transferee_Bank_ID;
        }
        public void setTransferee_Bank_ID(String Transferee_Bank_ID){
        this.Transferee_Bank_ID = Transferee_Bank_ID;
        }
        public String getTransferor_Bank_ID(){
        return this.Transferor_Bank_ID;
        }
        public void setTransferor_Bank_ID(String Transferor_Bank_ID){
        this.Transferor_Bank_ID = Transferor_Bank_ID;
        }
        public String getTOACT(){
        return this.TOACT;
        }
        public void setTOACT(String TOACT){
        this.TOACT = TOACT;
        }
        public String getTAX_TYPE(){
        return this.TAX_TYPE;
        }
        public void setTAX_TYPE(String TAX_TYPE){
        this.TAX_TYPE = TAX_TYPE;
        }
        public String getTAX_END_DATE(){
        return this.TAX_END_DATE;
        }
        public void setTAX_END_DATE(String TAX_END_DATE){
        this.TAX_END_DATE = TAX_END_DATE;
        }
        public String getTAX_organ(){
        return this.TAX_organ;
        }
        public void setTAX_organ(String TAX_organ){
        this.TAX_organ = TAX_organ;
        }
        public String getTAX_CID(){
        return this.TAX_CID;
        }
        public void setTAX_CID(String TAX_CID){
        this.TAX_CID = TAX_CID;
        }
        public String getTAX_BILL_NO(){
        return this.TAX_BILL_NO;
        }
        public void setTAX_BILL_NO(String TAX_BILL_NO){
        this.TAX_BILL_NO = TAX_BILL_NO;
        }
        public String getTAX_STATUS(){
        return this.TAX_STATUS;
        }
        public void setTAX_STATUS(String TAX_STATUS){
        this.TAX_STATUS = TAX_STATUS;
        }
        public String getTAX_CHARGCUS(){
        return this.TAX_CHARGCUS;
        }
        public void setTAX_CHARGCUS(String TAX_CHARGCUS){
        this.TAX_CHARGCUS = TAX_CHARGCUS;
        }
        public String getTAX_CHARGUNT(){
        return this.TAX_CHARGUNT;
        }
        public void setTAX_CHARGUNT(String TAX_CHARGUNT){
        this.TAX_CHARGUNT = TAX_CHARGUNT;
        }
        public String getTAX_MEMO(){
        return this.TAX_MEMO;
        }
        public void setTAX_MEMO(String TAX_MEMO){
        this.TAX_MEMO = TAX_MEMO;
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
        this.setTransferee_Bank_ID(EbcdicConverter.fromHex(CCSID.English,tita.substring(440, 454)));
        this.setTransferor_Bank_ID(EbcdicConverter.fromHex(CCSID.English,tita.substring(454, 468)));
        this.setTOACT(EbcdicConverter.fromHex(CCSID.English,tita.substring(468, 500)));
        this.setTAX_TYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(500, 510)));
        this.setTAX_END_DATE(EbcdicConverter.fromHex(CCSID.English,tita.substring(510, 522)));
        this.setTAX_organ(EbcdicConverter.fromHex(CCSID.English,tita.substring(522, 528)));
        this.setTAX_CID(EbcdicConverter.fromHex(CCSID.English,tita.substring(528, 550)));
        this.setTAX_BILL_NO(EbcdicConverter.fromHex(CCSID.English,tita.substring(550, 582)));
        this.setTAX_STATUS(EbcdicConverter.fromHex(CCSID.English,tita.substring(582, 586)));
        this.setTAX_CHARGCUS(EbcdicConverter.fromHex(CCSID.English,tita.substring(586, 594)));
        this.setTAX_CHARGUNT(EbcdicConverter.fromHex(CCSID.English,tita.substring(594, 634)));
        this.setTAX_MEMO(EbcdicConverter.fromHex(CCSID.English,tita.substring(634, 714)));
        this.setDRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(714, 1200)));
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
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTransferee_Bank_ID(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTransferor_Bank_ID(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTOACT(), 16) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTAX_TYPE(), 5) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTAX_END_DATE(), 6) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTAX_organ(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTAX_CID(), 11) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTAX_BILL_NO(), 16) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTAX_STATUS(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTAX_CHARGCUS(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTAX_CHARGUNT(), 20) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTAX_MEMO(), 40) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 243) //
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
            + StringUtils.leftPad(this.getTransferee_Bank_ID(), 7," ") //
            + StringUtils.leftPad(this.getTransferor_Bank_ID(), 7," ") //
            + StringUtils.leftPad(this.getTOACT(), 16," ") //
            + StringUtils.leftPad(this.getTAX_TYPE(), 5," ") //
            + StringUtils.leftPad(this.getTAX_END_DATE(), 6," ") //
            + StringUtils.leftPad(this.getTAX_organ(), 3," ") //
            + StringUtils.leftPad(this.getTAX_CID(), 11," ") //
            + StringUtils.leftPad(this.getTAX_BILL_NO(), 16," ") //
            + StringUtils.leftPad(this.getTAX_STATUS(), 2," ") //
            + StringUtils.leftPad(this.getTAX_CHARGCUS(), 4," ") //
            + StringUtils.leftPad(this.getTAX_CHARGUNT(), 20," ") //
            + StringUtils.leftPad(this.getTAX_MEMO(), 40," ") //
            + StringUtils.leftPad(this.getDRVS(), 243," ") //
;
}
}
