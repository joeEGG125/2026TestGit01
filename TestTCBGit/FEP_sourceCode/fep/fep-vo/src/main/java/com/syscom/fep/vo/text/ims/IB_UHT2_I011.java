package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class IB_UHT2_I011 extends IMSTextBase {
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

    @Field(length = 16)
	private String UH_Bill_No = StringUtils.EMPTY;

    @Field(length = 30)
	private String UH_Merchant_ID = StringUtils.EMPTY;

    @Field(length = 10)
	private String UH_ORG_STAN = StringUtils.EMPTY;

    @Field(length = 2)
	private String UH_Reason_Code = StringUtils.EMPTY;

    @Field(length = 2)
	private String UH_special_flag = StringUtils.EMPTY;

    @Field(length = 1)
	private String O_Redeem_State = StringUtils.EMPTY;

    @Field(length = 10)
	private String O_Add_Point = StringUtils.EMPTY;

    @Field(length = 10)
	private String O_Deduct_Point = StringUtils.EMPTY;

    @Field(length = 10)
	private String O_Extra_Amount = StringUtils.EMPTY;

    @Field(length = 13)
	private String O_Final_TX_amt = StringUtils.EMPTY;

    @Field(length = 12)
	private String MD_NAME = StringUtils.EMPTY;

    @Field(length = 264)
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
	
	public String getUH_Bill_No(){
        return this.UH_Bill_No;
	}
	
	public void setUH_Bill_No(String UH_Bill_No){
        this.UH_Bill_No = UH_Bill_No;
	}
	
	public String getUH_Merchant_ID(){
        return this.UH_Merchant_ID;
	}
	
	public void setUH_Merchant_ID(String UH_Merchant_ID){
        this.UH_Merchant_ID = UH_Merchant_ID;
	}
	
	public String getUH_ORG_STAN(){
        return this.UH_ORG_STAN;
	}
	
	public void setUH_ORG_STAN(String UH_ORG_STAN){
        this.UH_ORG_STAN = UH_ORG_STAN;
	}
	
	public String getUH_Reason_Code(){
        return this.UH_Reason_Code;
	}
	
	public void setUH_Reason_Code(String UH_Reason_Code){
        this.UH_Reason_Code = UH_Reason_Code;
	}
	
	public String getUH_special_flag(){
        return this.UH_special_flag;
	}
	
	public void setUH_special_flag(String UH_special_flag){
        this.UH_special_flag = UH_special_flag;
	}
	
	public String getO_Redeem_State(){
        return this.O_Redeem_State;
	}
	
	public void setO_Redeem_State(String O_Redeem_State){
        this.O_Redeem_State = O_Redeem_State;
	}
	
	public String getO_Add_Point(){
        return this.O_Add_Point;
	}
	
	public void setO_Add_Point(String O_Add_Point){
        this.O_Add_Point = O_Add_Point;
	}
	
	public String getO_Deduct_Point(){
        return this.O_Deduct_Point;
	}
	
	public void setO_Deduct_Point(String O_Deduct_Point){
        this.O_Deduct_Point = O_Deduct_Point;
	}
	
	public String getO_Extra_Amount(){
        return this.O_Extra_Amount;
	}
	
	public void setO_Extra_Amount(String O_Extra_Amount){
        this.O_Extra_Amount = O_Extra_Amount;
	}
	
	public String getO_Final_TX_amt(){
        return this.O_Final_TX_amt;
	}
	
	public void setO_Final_TX_amt(String O_Final_TX_amt){
        this.O_Final_TX_amt = O_Final_TX_amt;
	}
	
	public String getMD_NAME(){
        return this.MD_NAME;
	}
	
	public void setMD_NAME(String MD_NAME){
        this.MD_NAME = MD_NAME;
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
		this.setICMEMO(tita.substring(300, 360));
		this.setTXNICCTAC(tita.substring(360, 380));
        this.setTXNAMT(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(380, 408)).trim(), false, 2));
        this.setFROMACT(EbcdicConverter.fromHex(CCSID.English,tita.substring(408, 440)));
        this.setUH_Bill_No(EbcdicConverter.fromHex(CCSID.English,tita.substring(440, 472)));
        this.setUH_Merchant_ID(EbcdicConverter.fromHex(CCSID.English,tita.substring(472, 532)));
        this.setUH_ORG_STAN(EbcdicConverter.fromHex(CCSID.English,tita.substring(532, 552)));
        this.setUH_Reason_Code(EbcdicConverter.fromHex(CCSID.English,tita.substring(552, 556)));
        this.setUH_special_flag(EbcdicConverter.fromHex(CCSID.English,tita.substring(556, 560)));
        this.setO_Redeem_State(EbcdicConverter.fromHex(CCSID.English,tita.substring(560, 562)));
        this.setO_Add_Point(EbcdicConverter.fromHex(CCSID.English,tita.substring(562, 582)));
        this.setO_Deduct_Point(EbcdicConverter.fromHex(CCSID.English,tita.substring(582, 602)));
        this.setO_Extra_Amount(EbcdicConverter.fromHex(CCSID.English,tita.substring(602, 622)));
        this.setO_Final_TX_amt(EbcdicConverter.fromHex(CCSID.English,tita.substring(622, 648)));
		this.setMD_NAME(tita.substring(648, 672));
        this.setDRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(672, 1200)));
	}

	public String makeMessage() {
		return "" 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_TRANS(), 8) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSYSCODE(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSYS_DATETIME(), 14) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFEP_EJNO(), 8) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTXN_FLOW(), 1) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMSG_CAT(), 2) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSOURCE_CHANNEL(), 3) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPCODE(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFSCODE(), 2) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPROCESS_TYPE(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getBUSINESS_DATE(), 7) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getACQUIRER_BANK(), 3) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTXNSTAN(), 7) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERMINALID(), 8) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERMINAL_TYPE(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCARDISSUE_BANK(), 3) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCARDTYPE(), 1) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getRESPONSE_CODE(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getATM_TRANSEQ(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getHRVS(), 25) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_MAC(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getICCHIPSTAN(), 8) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERM_CHECKNO(), 8) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERMTXN_DATETIME(), 14) 
        	+ this.getICMEMO()  
        	+ this.getTXNICCTAC()  
            + CodeGenUtil.bigDecimalToEbcdic(this.getTXNAMT(), 11, false, 2, true) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFROMACT(), 16) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getUH_Bill_No(), 16) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getUH_Merchant_ID(), 30) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getUH_ORG_STAN(), 10) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getUH_Reason_Code(), 2) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getUH_special_flag(), 2) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_Redeem_State(), 1) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_Add_Point(), 10) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_Deduct_Point(), 10) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_Extra_Amount(), 10) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_Final_TX_amt(), 13) 
        	+ this.getMD_NAME()  
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 264) 
		;
	}

	public String makeMessageAscii() {
		return "" 
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getIMS_TRANS(), StringUtils.EMPTY), 8," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getSYSCODE(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getSYS_DATETIME(), StringUtils.EMPTY), 14," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getFEP_EJNO(), StringUtils.EMPTY), 8," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTXN_FLOW(), StringUtils.EMPTY), 1," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getMSG_CAT(), StringUtils.EMPTY), 2," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getSOURCE_CHANNEL(), StringUtils.EMPTY), 3," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPCODE(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getFSCODE(), StringUtils.EMPTY), 2," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPROCESS_TYPE(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getBUSINESS_DATE(), StringUtils.EMPTY), 7," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getACQUIRER_BANK(), StringUtils.EMPTY), 3," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTXNSTAN(), StringUtils.EMPTY), 7," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTERMINALID(), StringUtils.EMPTY), 8," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTERMINAL_TYPE(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getCARDISSUE_BANK(), StringUtils.EMPTY), 3," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getCARDTYPE(), StringUtils.EMPTY), 1," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getRESPONSE_CODE(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getATM_TRANSEQ(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getHRVS(), StringUtils.EMPTY), 25," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getIMS_MAC(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getICCHIPSTAN(), StringUtils.EMPTY), 8," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTERM_CHECKNO(), StringUtils.EMPTY), 8," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTERMTXN_DATETIME(), StringUtils.EMPTY), 14," ")
        	+ this.getICMEMO()  
        	+ this.getTXNICCTAC()  
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getTXNAMT(), 11, false, 2, true)
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getFROMACT(), StringUtils.EMPTY), 16," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getUH_Bill_No(), StringUtils.EMPTY), 16," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getUH_Merchant_ID(), StringUtils.EMPTY), 30," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getUH_ORG_STAN(), StringUtils.EMPTY), 10," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getUH_Reason_Code(), StringUtils.EMPTY), 2," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getUH_special_flag(), StringUtils.EMPTY), 2," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getO_Redeem_State(), StringUtils.EMPTY), 1," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getO_Add_Point(), StringUtils.EMPTY), 10," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getO_Deduct_Point(), StringUtils.EMPTY), 10," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getO_Extra_Amount(), StringUtils.EMPTY), 10," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getO_Final_TX_amt(), StringUtils.EMPTY), 13," ")
        	+ this.getMD_NAME()  
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getDRVS(), StringUtils.EMPTY), 264," ")
		;
	}
}
