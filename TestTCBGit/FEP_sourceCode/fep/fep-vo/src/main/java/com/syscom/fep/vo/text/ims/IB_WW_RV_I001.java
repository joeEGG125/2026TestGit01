package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class IB_WW_RV_I001 extends IMSTextBase {
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

    @Field(length = 104)
	private String TRK2 = StringUtils.EMPTY;

    @Field(length = 8)
	private String PINBLOCK = StringUtils.EMPTY;

    @Field(length = 12)
	private BigDecimal BIT36_OGSLAMT;

    @Field(length = 12)
	private BigDecimal BIT36_OGTXAMT;

    @Field(length = 3)
	private String BIT36_OGTXCCD = StringUtils.EMPTY;

    @Field(length = 3)
	private String BIT36_OGSLCCD = StringUtils.EMPTY;

    @Field(length = 8)
	private String BIT36_OGSLCRT = StringUtils.EMPTY;

    @Field(length = 12)
	private BigDecimal BIT36_RPSLAMT;

    @Field(length = 8)
	private String BIT36_TMSTAN1 = StringUtils.EMPTY;

    @Field(length = 1)
	private String ORG_CIWAFLG = StringUtils.EMPTY;

    @Field(length = 7)
	private String ORG_B006RATE = StringUtils.EMPTY;

    @Field(length = 30)
	private String ORG_RVTXNUSE = StringUtils.EMPTY;

    @Field(length = 172)
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
	
	public String getTRK2(){
        return this.TRK2;
	}
	
	public void setTRK2(String TRK2){
        this.TRK2 = TRK2;
	}
	
	public String getPINBLOCK(){
        return this.PINBLOCK;
	}
	
	public void setPINBLOCK(String PINBLOCK){
        this.PINBLOCK = PINBLOCK;
	}
	
	public BigDecimal getBIT36_OGSLAMT(){
        return this.BIT36_OGSLAMT;
	}
	
	public void setBIT36_OGSLAMT(BigDecimal BIT36_OGSLAMT){
        this.BIT36_OGSLAMT = BIT36_OGSLAMT;
	}
	
	public BigDecimal getBIT36_OGTXAMT(){
        return this.BIT36_OGTXAMT;
	}
	
	public void setBIT36_OGTXAMT(BigDecimal BIT36_OGTXAMT){
        this.BIT36_OGTXAMT = BIT36_OGTXAMT;
	}
	
	public String getBIT36_OGTXCCD(){
        return this.BIT36_OGTXCCD;
	}
	
	public void setBIT36_OGTXCCD(String BIT36_OGTXCCD){
        this.BIT36_OGTXCCD = BIT36_OGTXCCD;
	}
	
	public String getBIT36_OGSLCCD(){
        return this.BIT36_OGSLCCD;
	}
	
	public void setBIT36_OGSLCCD(String BIT36_OGSLCCD){
        this.BIT36_OGSLCCD = BIT36_OGSLCCD;
	}
	
	public String getBIT36_OGSLCRT(){
        return this.BIT36_OGSLCRT;
	}
	
	public void setBIT36_OGSLCRT(String BIT36_OGSLCRT){
        this.BIT36_OGSLCRT = BIT36_OGSLCRT;
	}
	
	public BigDecimal getBIT36_RPSLAMT(){
        return this.BIT36_RPSLAMT;
	}
	
	public void setBIT36_RPSLAMT(BigDecimal BIT36_RPSLAMT){
        this.BIT36_RPSLAMT = BIT36_RPSLAMT;
	}
	
	public String getBIT36_TMSTAN1(){
        return this.BIT36_TMSTAN1;
	}
	
	public void setBIT36_TMSTAN1(String BIT36_TMSTAN1){
        this.BIT36_TMSTAN1 = BIT36_TMSTAN1;
	}
	
	public String getORG_CIWAFLG(){
        return this.ORG_CIWAFLG;
	}
	
	public void setORG_CIWAFLG(String ORG_CIWAFLG){
        this.ORG_CIWAFLG = ORG_CIWAFLG;
	}
	
	public String getORG_B006RATE(){
        return this.ORG_B006RATE;
	}
	
	public void setORG_B006RATE(String ORG_B006RATE){
        this.ORG_B006RATE = ORG_B006RATE;
	}
	
	public String getORG_RVTXNUSE(){
        return this.ORG_RVTXNUSE;
	}
	
	public void setORG_RVTXNUSE(String ORG_RVTXNUSE){
        this.ORG_RVTXNUSE = ORG_RVTXNUSE;
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
        this.setTXNAMT(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(380, 408)).trim(), false, 2));
        this.setFROMACT(EbcdicConverter.fromHex(CCSID.English,tita.substring(408, 440)));
        this.setTRK2(EbcdicConverter.fromHex(CCSID.English,tita.substring(440, 648)));
		this.setPINBLOCK(tita.substring(648, 664));
        this.setBIT36_OGSLAMT(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(664, 688)).trim(), false, 2));
        this.setBIT36_OGTXAMT(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(688, 712)).trim(), false, 0));
        this.setBIT36_OGTXCCD(EbcdicConverter.fromHex(CCSID.English,tita.substring(712, 718)));
        this.setBIT36_OGSLCCD(EbcdicConverter.fromHex(CCSID.English,tita.substring(718, 724)));
        this.setBIT36_OGSLCRT(EbcdicConverter.fromHex(CCSID.English,tita.substring(724, 740)));
        this.setBIT36_RPSLAMT(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(740, 764)).trim(), false, 0));
        this.setBIT36_TMSTAN1(EbcdicConverter.fromHex(CCSID.English,tita.substring(764, 780)));
        this.setORG_CIWAFLG(EbcdicConverter.fromHex(CCSID.English,tita.substring(780, 782)));
        this.setORG_B006RATE(EbcdicConverter.fromHex(CCSID.English,tita.substring(782, 796)));
        this.setORG_RVTXNUSE(EbcdicConverter.fromHex(CCSID.English,tita.substring(796, 856)));
        this.setDRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(856, 1200)));
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
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getICMEMO(), 30) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTXNICCTAC(), 10) 
            + CodeGenUtil.bigDecimalToEbcdic(this.getTXNAMT(), 11, false, 2, true) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFROMACT(), 16) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRK2(), 104) 
        	+ this.getPINBLOCK()  
            + CodeGenUtil.bigDecimalToEbcdic(this.getBIT36_OGSLAMT(), 10, false, 2, false) 
            + CodeGenUtil.bigDecimalToEbcdic(this.getBIT36_OGTXAMT(), 12, false, 0, false) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getBIT36_OGTXCCD(), 3) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getBIT36_OGSLCCD(), 3) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getBIT36_OGSLCRT(), 8) 
            + CodeGenUtil.bigDecimalToEbcdic(this.getBIT36_RPSLAMT(), 12, false, 0, false) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getBIT36_TMSTAN1(), 8) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getORG_CIWAFLG(), 1) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getORG_B006RATE(), 7) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getORG_RVTXNUSE(), 30) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 172) 
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
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getICMEMO(), StringUtils.EMPTY), 30," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTXNICCTAC(), StringUtils.EMPTY), 10," ")
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getTXNAMT(), 11, false, 2, true)
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getFROMACT(), StringUtils.EMPTY), 16," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTRK2(), StringUtils.EMPTY), 104," ")
        	+ this.getPINBLOCK()  
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getBIT36_OGSLAMT(), 10, false, 2, false)
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getBIT36_OGTXAMT(), 12, false, 0, false)
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getBIT36_OGTXCCD(), StringUtils.EMPTY), 3," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getBIT36_OGSLCCD(), StringUtils.EMPTY), 3," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getBIT36_OGSLCRT(), StringUtils.EMPTY), 8," ")
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getBIT36_RPSLAMT(), 12, false, 0, false)
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getBIT36_TMSTAN1(), StringUtils.EMPTY), 8," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getORG_CIWAFLG(), StringUtils.EMPTY), 1," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getORG_B006RATE(), StringUtils.EMPTY), 7," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getORG_RVTXNUSE(), StringUtils.EMPTY), 30," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getDRVS(), StringUtils.EMPTY), 172," ")
		;
	}
}
