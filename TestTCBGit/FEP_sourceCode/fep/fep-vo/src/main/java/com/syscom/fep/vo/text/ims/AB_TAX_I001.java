package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class AB_TAX_I001 extends IMSTextBase {
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
	private String ATMTRANSEQ = StringUtils.EMPTY;

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

    @Field(length = 7)
	private String TRIN_BANKNO = StringUtils.EMPTY;

    @Field(length = 7)
	private String TROUT_BANKNO = StringUtils.EMPTY;

    @Field(length = 16)
	private String TOACT = StringUtils.EMPTY;

    @Field(length = 5)
	private String TAX_TYPE = StringUtils.EMPTY;

    @Field(length = 6)
	private String TAX_END_DATE = StringUtils.EMPTY;

    @Field(length = 3)
	private String TAX_ORGAN = StringUtils.EMPTY;

    @Field(length = 11)
	private String TAX_CID = StringUtils.EMPTY;

    @Field(length = 16)
	private String TAX_BILL_NO = StringUtils.EMPTY;

    @Field(length = 10)
	private String AE_TRNSFROUTIDNO = StringUtils.EMPTY;

    @Field(length = 1)
	private String AE_TRNSFLAG = StringUtils.EMPTY;

    @Field(length = 1)
	private String AE_BUSINESSTYPE = StringUtils.EMPTY;

    @Field(length = 2)
	private String AE_AEIEFEET = StringUtils.EMPTY;

    @Field(length = 18)
	private String AE_TRNSFROUTNOTE = StringUtils.EMPTY;

    @Field(length = 1)
	private String AE_AEISLLTY = StringUtils.EMPTY;

    @Field(length = 2)
	private String AE_LIMITTYPE = StringUtils.EMPTY;

    @Field(length = 2)
	private BigDecimal AE_AEIFIXFE;

    @Field(length = 2)
	private BigDecimal AE_AEINETFE;

    @Field(length = 2)
	private BigDecimal AE_AEIEDIFE;

    @Field(length = 12)
	private String AE_AEICIRCU = StringUtils.EMPTY;

    @Field(length = 256)
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

	public String getATMTRANSEQ(){
        return this.ATMTRANSEQ;
	}

	public void setATMTRANSEQ(String ATMTRANSEQ){
        this.ATMTRANSEQ = ATMTRANSEQ;
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

	public String getTRIN_BANKNO(){
        return this.TRIN_BANKNO;
	}

	public void setTRIN_BANKNO(String TRIN_BANKNO){
        this.TRIN_BANKNO = TRIN_BANKNO;
	}

	public String getTROUT_BANKNO(){
        return this.TROUT_BANKNO;
	}

	public void setTROUT_BANKNO(String TROUT_BANKNO){
        this.TROUT_BANKNO = TROUT_BANKNO;
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

	public String getTAX_ORGAN(){
        return this.TAX_ORGAN;
	}

	public void setTAX_ORGAN(String TAX_ORGAN){
        this.TAX_ORGAN = TAX_ORGAN;
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

	public String getAE_TRNSFROUTIDNO(){
        return this.AE_TRNSFROUTIDNO;
	}

	public void setAE_TRNSFROUTIDNO(String AE_TRNSFROUTIDNO){
        this.AE_TRNSFROUTIDNO = AE_TRNSFROUTIDNO;
	}

	public String getAE_TRNSFLAG(){
        return this.AE_TRNSFLAG;
	}

	public void setAE_TRNSFLAG(String AE_TRNSFLAG){
        this.AE_TRNSFLAG = AE_TRNSFLAG;
	}

	public String getAE_BUSINESSTYPE(){
        return this.AE_BUSINESSTYPE;
	}

	public void setAE_BUSINESSTYPE(String AE_BUSINESSTYPE){
        this.AE_BUSINESSTYPE = AE_BUSINESSTYPE;
	}

	public String getAE_AEIEFEET(){
        return this.AE_AEIEFEET;
	}

	public void setAE_AEIEFEET(String AE_AEIEFEET){
        this.AE_AEIEFEET = AE_AEIEFEET;
	}

	public String getAE_TRNSFROUTNOTE(){
        return this.AE_TRNSFROUTNOTE;
	}

	public void setAE_TRNSFROUTNOTE(String AE_TRNSFROUTNOTE){
        this.AE_TRNSFROUTNOTE = AE_TRNSFROUTNOTE;
	}

	public String getAE_AEISLLTY(){
        return this.AE_AEISLLTY;
	}

	public void setAE_AEISLLTY(String AE_AEISLLTY){
        this.AE_AEISLLTY = AE_AEISLLTY;
	}

	public String getAE_LIMITTYPE(){
        return this.AE_LIMITTYPE;
	}

	public void setAE_LIMITTYPE(String AE_LIMITTYPE){
        this.AE_LIMITTYPE = AE_LIMITTYPE;
	}

	public BigDecimal getAE_AEIFIXFE(){
        return this.AE_AEIFIXFE;
	}

	public void setAE_AEIFIXFE(BigDecimal AE_AEIFIXFE){
		this.AE_AEIFIXFE = AE_AEIFIXFE;
	}

	public BigDecimal getAE_AEINETFE(){
        return this.AE_AEINETFE;
	}

	public void setAE_AEINETFE(BigDecimal AE_AEINETFE){
		this.AE_AEINETFE = AE_AEINETFE;
	}

	public BigDecimal getAE_AEIEDIFE(){
        return this.AE_AEIEDIFE;
	}

	public void setAE_AEIEDIFE(BigDecimal AE_AEIEDIFE){
		this.AE_AEIEDIFE = AE_AEIEDIFE;
	}

	public String getAE_AEICIRCU(){
        return this.AE_AEICIRCU;
	}

	public void setAE_AEICIRCU(String AE_AEICIRCU){
        this.AE_AEICIRCU = AE_AEICIRCU;
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
        this.setATMTRANSEQ(EbcdicConverter.fromHex(CCSID.English,tita.substring(174, 182)));
        this.setHRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(182, 232)));
        this.setCBSMAC(EbcdicConverter.fromHex(CCSID.English,tita.substring(232, 240)));
        this.setICCHIPSTAN(EbcdicConverter.fromHex(CCSID.English,tita.substring(240, 256)));
        this.setTERM_CHECKNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(256, 272)));
        this.setTERMTXN_DATETIME(EbcdicConverter.fromHex(CCSID.English,tita.substring(272, 300)));
        this.setICMEMO(EbcdicConverter.fromHex(CCSID.English,tita.substring(300, 360)));
        this.setTXNICCTAC(EbcdicConverter.fromHex(CCSID.English,tita.substring(360, 380)));
        this.setTXNAMT(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(380, 408).trim()), false, 2));
        this.setFROMACT(EbcdicConverter.fromHex(CCSID.English,tita.substring(408, 440)));
        this.setTRIN_BANKNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(440, 454)));
        this.setTROUT_BANKNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(454, 468)));
        this.setTOACT(EbcdicConverter.fromHex(CCSID.English,tita.substring(468, 500)));
        this.setTAX_TYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(500, 510)));
        this.setTAX_END_DATE(EbcdicConverter.fromHex(CCSID.English,tita.substring(510, 522)));
        this.setTAX_ORGAN(EbcdicConverter.fromHex(CCSID.English,tita.substring(522, 528)));
        this.setTAX_CID(EbcdicConverter.fromHex(CCSID.English,tita.substring(528, 550)));
        this.setTAX_BILL_NO(EbcdicConverter.fromHex(CCSID.English,tita.substring(550, 582)));
        this.setAE_TRNSFROUTIDNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(582, 602)));
        this.setAE_TRNSFLAG(EbcdicConverter.fromHex(CCSID.English,tita.substring(602, 604)));
        this.setAE_BUSINESSTYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(604, 606)));
        this.setAE_AEIEFEET(EbcdicConverter.fromHex(CCSID.English,tita.substring(606, 610)));
        this.setAE_TRNSFROUTNOTE(EbcdicConverter.fromHex(CCSID.English,tita.substring(610, 646)));
        this.setAE_AEISLLTY(EbcdicConverter.fromHex(CCSID.English,tita.substring(646, 648)));
        this.setAE_LIMITTYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(648, 652)));
        this.setAE_AEIFIXFE(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(652, 656).trim()), false, 0));
        this.setAE_AEINETFE(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(656, 660).trim()), false, 0));
        this.setAE_AEIEDIFE(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(660, 664).trim()), false, 0));
        this.setAE_AEICIRCU(EbcdicConverter.fromHex(CCSID.English,tita.substring(664, 688)));
        this.setDRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(688, 1200)));
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
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getATMTRANSEQ(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getHRVS(), 25) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCBSMAC(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getICCHIPSTAN(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERM_CHECKNO(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERMTXN_DATETIME(), 14) //
        + this.getICMEMO()  //
        + this.getTXNICCTAC()  //
            + CodeGenUtil.bigDecimalToEbcdic(this.getTXNAMT(), 11, false, 2, true) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFROMACT(), 16) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRIN_BANKNO(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTROUT_BANKNO(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTOACT(), 16) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTAX_TYPE(), 5) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTAX_END_DATE(), 6) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTAX_ORGAN(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTAX_CID(), 11) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTAX_BILL_NO(), 16) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_TRNSFROUTIDNO(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_TRNSFLAG(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_BUSINESSTYPE(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_AEIEFEET(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_TRNSFROUTNOTE(), 18) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_AEISLLTY(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_LIMITTYPE(), 2) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getAE_AEIFIXFE(), 2, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getAE_AEINETFE(), 2, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getAE_AEIEDIFE(), 2, false, 0, false) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_AEICIRCU(), 12) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 256) //
		;
	}

	public String makeMessageAscii() {
		return "" //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getIMS_TRANS(), StringUtils.EMPTY), 8," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getSYSCODE(), StringUtils.EMPTY), 4," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getSYS_DATETIME(), StringUtils.EMPTY), 14," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getFEP_EJNO(), StringUtils.EMPTY), 8," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTXN_FLOW(), StringUtils.EMPTY), 1," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getMSG_CAT(), StringUtils.EMPTY), 2," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getSOURCE_CHANNEL(), StringUtils.EMPTY), 3," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPCODE(), StringUtils.EMPTY), 4," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getFSCODE(), StringUtils.EMPTY), 2," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPROCESS_TYPE(), StringUtils.EMPTY), 4," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getBUSINESS_DATE(), StringUtils.EMPTY), 7," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getACQUIRER_BANK(), StringUtils.EMPTY), 3," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTXNSTAN(), StringUtils.EMPTY), 7," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTERMINALID(), StringUtils.EMPTY), 8," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTERMINAL_TYPE(), StringUtils.EMPTY), 4," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getCARDISSUE_BANK(), StringUtils.EMPTY), 3," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getCARDTYPE(), StringUtils.EMPTY), 1," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getRESPONSE_CODE(), StringUtils.EMPTY), 4," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getATMTRANSEQ(), StringUtils.EMPTY), 4," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getHRVS(), StringUtils.EMPTY), 25," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getCBSMAC(), StringUtils.EMPTY), 4," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getICCHIPSTAN(), StringUtils.EMPTY), 8," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTERM_CHECKNO(), StringUtils.EMPTY), 8," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTERMTXN_DATETIME(), StringUtils.EMPTY), 14," ") //
        + this.getICMEMO()  //
        + this.getTXNICCTAC()  //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getTXNAMT(), 11, false, 2, true) //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getFROMACT(), StringUtils.EMPTY), 16," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTRIN_BANKNO(), StringUtils.EMPTY), 7," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTROUT_BANKNO(), StringUtils.EMPTY), 7," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTOACT(), StringUtils.EMPTY), 16," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTAX_TYPE(), StringUtils.EMPTY), 5," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTAX_END_DATE(), StringUtils.EMPTY), 6," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTAX_ORGAN(), StringUtils.EMPTY), 3," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTAX_CID(), StringUtils.EMPTY), 11," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTAX_BILL_NO(), StringUtils.EMPTY), 16," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAE_TRNSFROUTIDNO(), StringUtils.EMPTY), 10," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAE_TRNSFLAG(), StringUtils.EMPTY), 1," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAE_BUSINESSTYPE(), StringUtils.EMPTY), 1," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAE_AEIEFEET(), StringUtils.EMPTY), 2," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAE_TRNSFROUTNOTE(), StringUtils.EMPTY), 18," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAE_AEISLLTY(), StringUtils.EMPTY), 1," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAE_LIMITTYPE(), StringUtils.EMPTY), 2," ") //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getAE_AEIFIXFE(), 2, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getAE_AEINETFE(), 2, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getAE_AEIEDIFE(), 2, false, 0, false) //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAE_AEICIRCU(), StringUtils.EMPTY), 12," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getDRVS(), StringUtils.EMPTY), 256," ") //
		;
	}
}
