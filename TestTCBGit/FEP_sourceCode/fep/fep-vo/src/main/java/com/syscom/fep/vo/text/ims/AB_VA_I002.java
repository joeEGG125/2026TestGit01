package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class AB_VA_I002 extends IMSTextBase {
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
	private String CBSMAC = StringUtils.EMPTY;

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

    @Field(length = 4)
	private BigDecimal CHARGCUS;

    @Field(length = 2)
	private String VACATE = StringUtils.EMPTY;

    @Field(length = 2)
	private String AEIPYTP = StringUtils.EMPTY;

    @Field(length = 2)
	private String AEIPYUES = StringUtils.EMPTY;

    @Field(length = 2)
	private String AELFTP = StringUtils.EMPTY;

    @Field(length = 2)
	private String AEILFRC1 = StringUtils.EMPTY;

    @Field(length = 2)
	private String AEILFRC2 = StringUtils.EMPTY;

    @Field(length = 2)
	private String OPENACT = StringUtils.EMPTY;

    @Field(length = 10)
	private String IDNO = StringUtils.EMPTY;

    @Field(length = 8)
	private String CLCPYCI = StringUtils.EMPTY;

    @Field(length = 8)
	private String PAYUNTNO = StringUtils.EMPTY;

    @Field(length = 5)
	private String TAXTYPE = StringUtils.EMPTY;

    @Field(length = 4)
	private String PAYFEENO = StringUtils.EMPTY;

    @Field(length = 10)
	private String MOBILENO = StringUtils.EMPTY;

    @Field(length = 16)
	private String AEIPYAC2 = StringUtils.EMPTY;

    @Field(length = 16)
	private String AEIPYAC = StringUtils.EMPTY;

    @Field(length = 10)
	private String INSURE_IDNO = StringUtils.EMPTY;

    @Field(length = 8)
	private String BIRTHDAY = StringUtils.EMPTY;

    @Field(length = 10)
	private String TELHOME = StringUtils.EMPTY;

    @Field(length = 3)
	private String AEIPYBK = StringUtils.EMPTY;

    @Field(length = 4)
	private String AEIPYBH = StringUtils.EMPTY;

    @Field(length = 1)
	private String SSLTYPE = StringUtils.EMPTY;

    @Field(length = 249)
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
	
	public String getCBSMAC(){
        return this.CBSMAC;
	}
	
	public void setCBSMAC(String CBSMAC){
        this.CBSMAC = CBSMAC;
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
	
	public BigDecimal getCHARGCUS(){
        return this.CHARGCUS;
	}
	
	public void setCHARGCUS(BigDecimal CHARGCUS){
        this.CHARGCUS = CHARGCUS;
	}
	
	public String getVACATE(){
        return this.VACATE;
	}
	
	public void setVACATE(String VACATE){
        this.VACATE = VACATE;
	}
	
	public String getAEIPYTP(){
        return this.AEIPYTP;
	}
	
	public void setAEIPYTP(String AEIPYTP){
        this.AEIPYTP = AEIPYTP;
	}
	
	public String getAEIPYUES(){
        return this.AEIPYUES;
	}
	
	public void setAEIPYUES(String AEIPYUES){
        this.AEIPYUES = AEIPYUES;
	}
	
	public String getAELFTP(){
        return this.AELFTP;
	}
	
	public void setAELFTP(String AELFTP){
        this.AELFTP = AELFTP;
	}
	
	public String getAEILFRC1(){
        return this.AEILFRC1;
	}
	
	public void setAEILFRC1(String AEILFRC1){
        this.AEILFRC1 = AEILFRC1;
	}
	
	public String getAEILFRC2(){
        return this.AEILFRC2;
	}
	
	public void setAEILFRC2(String AEILFRC2){
        this.AEILFRC2 = AEILFRC2;
	}
	
	public String getOPENACT(){
        return this.OPENACT;
	}
	
	public void setOPENACT(String OPENACT){
        this.OPENACT = OPENACT;
	}
	
	public String getIDNO(){
        return this.IDNO;
	}
	
	public void setIDNO(String IDNO){
        this.IDNO = IDNO;
	}
	
	public String getCLCPYCI(){
        return this.CLCPYCI;
	}
	
	public void setCLCPYCI(String CLCPYCI){
        this.CLCPYCI = CLCPYCI;
	}
	
	public String getPAYUNTNO(){
        return this.PAYUNTNO;
	}
	
	public void setPAYUNTNO(String PAYUNTNO){
        this.PAYUNTNO = PAYUNTNO;
	}
	
	public String getTAXTYPE(){
        return this.TAXTYPE;
	}
	
	public void setTAXTYPE(String TAXTYPE){
        this.TAXTYPE = TAXTYPE;
	}
	
	public String getPAYFEENO(){
        return this.PAYFEENO;
	}
	
	public void setPAYFEENO(String PAYFEENO){
        this.PAYFEENO = PAYFEENO;
	}
	
	public String getMOBILENO(){
        return this.MOBILENO;
	}
	
	public void setMOBILENO(String MOBILENO){
        this.MOBILENO = MOBILENO;
	}
	
	public String getAEIPYAC2(){
        return this.AEIPYAC2;
	}
	
	public void setAEIPYAC2(String AEIPYAC2){
        this.AEIPYAC2 = AEIPYAC2;
	}
	
	public String getAEIPYAC(){
        return this.AEIPYAC;
	}
	
	public void setAEIPYAC(String AEIPYAC){
        this.AEIPYAC = AEIPYAC;
	}
	
	public String getINSURE_IDNO(){
        return this.INSURE_IDNO;
	}
	
	public void setINSURE_IDNO(String INSURE_IDNO){
        this.INSURE_IDNO = INSURE_IDNO;
	}
	
	public String getBIRTHDAY(){
        return this.BIRTHDAY;
	}
	
	public void setBIRTHDAY(String BIRTHDAY){
        this.BIRTHDAY = BIRTHDAY;
	}
	
	public String getTELHOME(){
        return this.TELHOME;
	}
	
	public void setTELHOME(String TELHOME){
        this.TELHOME = TELHOME;
	}
	
	public String getAEIPYBK(){
        return this.AEIPYBK;
	}
	
	public void setAEIPYBK(String AEIPYBK){
        this.AEIPYBK = AEIPYBK;
	}
	
	public String getAEIPYBH(){
        return this.AEIPYBH;
	}
	
	public void setAEIPYBH(String AEIPYBH){
        this.AEIPYBH = AEIPYBH;
	}
	
	public String getSSLTYPE(){
        return this.SSLTYPE;
	}
	
	public void setSSLTYPE(String SSLTYPE){
        this.SSLTYPE = SSLTYPE;
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
        this.setCBSMAC(EbcdicConverter.fromHex(CCSID.English,tita.substring(232, 240)));
        this.setICCHIPSTAN(EbcdicConverter.fromHex(CCSID.English,tita.substring(240, 256)));
        this.setTERM_CHECKNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(256, 272)));
        this.setTERMTXN_DATETIME(EbcdicConverter.fromHex(CCSID.English,tita.substring(272, 300)));
		this.setICMEMO(tita.substring(300, 360));
		this.setTXNICCTAC(tita.substring(360, 380));
        this.setTXNAMT(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(380, 408)).trim(), false, 2));
        this.setFROMACT(EbcdicConverter.fromHex(CCSID.English,tita.substring(408, 440)));
        this.setCHARGCUS(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(440, 448)).trim(), false, 0));
        this.setVACATE(EbcdicConverter.fromHex(CCSID.English,tita.substring(448, 452)));
        this.setAEIPYTP(EbcdicConverter.fromHex(CCSID.English,tita.substring(452, 456)));
        this.setAEIPYUES(EbcdicConverter.fromHex(CCSID.English,tita.substring(456, 460)));
        this.setAELFTP(EbcdicConverter.fromHex(CCSID.English,tita.substring(460, 464)));
        this.setAEILFRC1(EbcdicConverter.fromHex(CCSID.English,tita.substring(464, 468)));
        this.setAEILFRC2(EbcdicConverter.fromHex(CCSID.English,tita.substring(468, 472)));
        this.setOPENACT(EbcdicConverter.fromHex(CCSID.English,tita.substring(472, 476)));
        this.setIDNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(476, 496)));
        this.setCLCPYCI(EbcdicConverter.fromHex(CCSID.English,tita.substring(496, 512)));
        this.setPAYUNTNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(512, 528)));
        this.setTAXTYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(528, 538)));
        this.setPAYFEENO(EbcdicConverter.fromHex(CCSID.English,tita.substring(538, 546)));
        this.setMOBILENO(EbcdicConverter.fromHex(CCSID.English,tita.substring(546, 566)));
        this.setAEIPYAC2(EbcdicConverter.fromHex(CCSID.English,tita.substring(566, 598)));
        this.setAEIPYAC(EbcdicConverter.fromHex(CCSID.English,tita.substring(598, 630)));
        this.setINSURE_IDNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(630, 650)));
        this.setBIRTHDAY(EbcdicConverter.fromHex(CCSID.English,tita.substring(650, 666)));
        this.setTELHOME(EbcdicConverter.fromHex(CCSID.English,tita.substring(666, 686)));
        this.setAEIPYBK(EbcdicConverter.fromHex(CCSID.English,tita.substring(686, 692)));
        this.setAEIPYBH(EbcdicConverter.fromHex(CCSID.English,tita.substring(692, 700)));
        this.setSSLTYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(700, 702)));
        this.setDRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(702, 1200)));
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
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCBSMAC(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getICCHIPSTAN(), 8) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERM_CHECKNO(), 8) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERMTXN_DATETIME(), 14) 
        	+ this.getICMEMO()  
        	+ this.getTXNICCTAC()  
            + CodeGenUtil.bigDecimalToEbcdic(this.getTXNAMT(), 11, false, 2, true) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFROMACT(), 16) 
            + CodeGenUtil.bigDecimalToEbcdic(this.getCHARGCUS(), 4, false, 0, false) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getVACATE(), 2) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAEIPYTP(), 2) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAEIPYUES(), 2) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAELFTP(), 2) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAEILFRC1(), 2) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAEILFRC2(), 2) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOPENACT(), 2) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIDNO(), 10) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCLCPYCI(), 8) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPAYUNTNO(), 8) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTAXTYPE(), 5) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPAYFEENO(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMOBILENO(), 10) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAEIPYAC2(), 16) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAEIPYAC(), 16) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINSURE_IDNO(), 10) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getBIRTHDAY(), 8) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTELHOME(), 10) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAEIPYBK(), 3) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAEIPYBH(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSSLTYPE(), 1) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 249) 
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
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getCBSMAC(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getICCHIPSTAN(), StringUtils.EMPTY), 8," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTERM_CHECKNO(), StringUtils.EMPTY), 8," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTERMTXN_DATETIME(), StringUtils.EMPTY), 14," ")
        	+ this.getICMEMO()  
        	+ this.getTXNICCTAC()  
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getTXNAMT(), 11, false, 2, true)
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getFROMACT(), StringUtils.EMPTY), 16," ")
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getCHARGCUS(), 4, false, 0, false)
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getVACATE(), StringUtils.EMPTY), 2," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAEIPYTP(), StringUtils.EMPTY), 2," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAEIPYUES(), StringUtils.EMPTY), 2," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAELFTP(), StringUtils.EMPTY), 2," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAEILFRC1(), StringUtils.EMPTY), 2," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAEILFRC2(), StringUtils.EMPTY), 2," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOPENACT(), StringUtils.EMPTY), 2," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getIDNO(), StringUtils.EMPTY), 10," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getCLCPYCI(), StringUtils.EMPTY), 8," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPAYUNTNO(), StringUtils.EMPTY), 8," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTAXTYPE(), StringUtils.EMPTY), 5," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPAYFEENO(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getMOBILENO(), StringUtils.EMPTY), 10," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAEIPYAC2(), StringUtils.EMPTY), 16," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAEIPYAC(), StringUtils.EMPTY), 16," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getINSURE_IDNO(), StringUtils.EMPTY), 10," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getBIRTHDAY(), StringUtils.EMPTY), 8," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTELHOME(), StringUtils.EMPTY), 10," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAEIPYBK(), StringUtils.EMPTY), 3," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAEIPYBH(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getSSLTYPE(), StringUtils.EMPTY), 1," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getDRVS(), StringUtils.EMPTY), 249," ")
		;
	}
}
