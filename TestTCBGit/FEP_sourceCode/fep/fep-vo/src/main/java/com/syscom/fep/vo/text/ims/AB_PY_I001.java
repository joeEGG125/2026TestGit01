package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class AB_PY_I001 extends IMSTextBase {
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

    @Field(length = 2)
	private String PY_SPECIAL_FLAG = StringUtils.EMPTY;

    @Field(length = 7)
	private String PY_HOST_BRANCH = StringUtils.EMPTY;

    @Field(length = 11)
	private String PY_IDNO = StringUtils.EMPTY;

    @Field(length = 8)
	private String PY_PAYUNTNO = StringUtils.EMPTY;

    @Field(length = 5)
	private String PY_TAXTYPE = StringUtils.EMPTY;

    @Field(length = 4)
	private String PY_PAYFEENO = StringUtils.EMPTY;

    @Field(length = 16)
	private String PY_PAYTXNOL = StringUtils.EMPTY;

    @Field(length = 8)
	private String PY_PAYDDATE = StringUtils.EMPTY;

    @Field(length = 12)
	private String PY_PAYMEMO_FILL = StringUtils.EMPTY;

    @Field(length = 4)
	private BigDecimal PY_CHARGCUS;

    @Field(length = 20)
	private BigDecimal PY_CHARGUNT;

    @Field(length = 24)
	private String PY_PAYTXNOL1 = StringUtils.EMPTY;

    @Field(length = 1)
	private String EATM_RESERVE_FLAG = StringUtils.EMPTY;

    @Field(length = 14)
	private String ORIGINAL_TX_DAYTIME = StringUtils.EMPTY;

    @Field(length = 2)
	private BigDecimal CHANNEL_CHARGE;

    @Field(length = 212)
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

	public String getPY_SPECIAL_FLAG(){
        return this.PY_SPECIAL_FLAG;
	}

	public void setPY_SPECIAL_FLAG(String PY_SPECIAL_FLAG){
        this.PY_SPECIAL_FLAG = PY_SPECIAL_FLAG;
	}

	public String getPY_HOST_BRANCH(){
        return this.PY_HOST_BRANCH;
	}

	public void setPY_HOST_BRANCH(String PY_HOST_BRANCH){
        this.PY_HOST_BRANCH = PY_HOST_BRANCH;
	}

	public String getPY_IDNO(){
        return this.PY_IDNO;
	}

	public void setPY_IDNO(String PY_IDNO){
        this.PY_IDNO = PY_IDNO;
	}

	public String getPY_PAYUNTNO(){
        return this.PY_PAYUNTNO;
	}

	public void setPY_PAYUNTNO(String PY_PAYUNTNO){
        this.PY_PAYUNTNO = PY_PAYUNTNO;
	}

	public String getPY_TAXTYPE(){
        return this.PY_TAXTYPE;
	}

	public void setPY_TAXTYPE(String PY_TAXTYPE){
        this.PY_TAXTYPE = PY_TAXTYPE;
	}

	public String getPY_PAYFEENO(){
        return this.PY_PAYFEENO;
	}

	public void setPY_PAYFEENO(String PY_PAYFEENO){
        this.PY_PAYFEENO = PY_PAYFEENO;
	}

	public String getPY_PAYTXNOL(){
        return this.PY_PAYTXNOL;
	}

	public void setPY_PAYTXNOL(String PY_PAYTXNOL){
        this.PY_PAYTXNOL = PY_PAYTXNOL;
	}

	public String getPY_PAYDDATE(){
        return this.PY_PAYDDATE;
	}

	public void setPY_PAYDDATE(String PY_PAYDDATE){
        this.PY_PAYDDATE = PY_PAYDDATE;
	}

	public String getPY_PAYMEMO_FILL(){
        return this.PY_PAYMEMO_FILL;
	}

	public void setPY_PAYMEMO_FILL(String PY_PAYMEMO_FILL){
        this.PY_PAYMEMO_FILL = PY_PAYMEMO_FILL;
	}

	public BigDecimal getPY_CHARGCUS(){
        return this.PY_CHARGCUS;
	}

	public void setPY_CHARGCUS(BigDecimal PY_CHARGCUS){
		this.PY_CHARGCUS = PY_CHARGCUS;
	}

	public BigDecimal getPY_CHARGUNT(){
        return this.PY_CHARGUNT;
	}

	public void setPY_CHARGUNT(BigDecimal PY_CHARGUNT){
		this.PY_CHARGUNT = PY_CHARGUNT;
	}

	public String getPY_PAYTXNOL1(){
        return this.PY_PAYTXNOL1;
	}

	public void setPY_PAYTXNOL1(String PY_PAYTXNOL1){
        this.PY_PAYTXNOL1 = PY_PAYTXNOL1;
	}

	public String getEATM_RESERVE_FLAG(){
        return this.EATM_RESERVE_FLAG;
	}

	public void setEATM_RESERVE_FLAG(String EATM_RESERVE_FLAG){
        this.EATM_RESERVE_FLAG = EATM_RESERVE_FLAG;
	}

	public String getORIGINAL_TX_DAYTIME(){
        return this.ORIGINAL_TX_DAYTIME;
	}

	public void setORIGINAL_TX_DAYTIME(String ORIGINAL_TX_DAYTIME){
        this.ORIGINAL_TX_DAYTIME = ORIGINAL_TX_DAYTIME;
	}

	public BigDecimal getCHANNEL_CHARGE(){
        return this.CHANNEL_CHARGE;
	}

	public void setCHANNEL_CHARGE(BigDecimal CHANNEL_CHARGE){
		this.CHANNEL_CHARGE = CHANNEL_CHARGE;
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
        this.setPY_SPECIAL_FLAG(EbcdicConverter.fromHex(CCSID.English,tita.substring(500, 504)));
        this.setPY_HOST_BRANCH(EbcdicConverter.fromHex(CCSID.English,tita.substring(504, 518)));
        this.setPY_IDNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(518, 540)));
        this.setPY_PAYUNTNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(540, 556)));
        this.setPY_TAXTYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(556, 566)));
        this.setPY_PAYFEENO(EbcdicConverter.fromHex(CCSID.English,tita.substring(566, 574)));
        this.setPY_PAYTXNOL(EbcdicConverter.fromHex(CCSID.English,tita.substring(574, 606)));
        this.setPY_PAYDDATE(EbcdicConverter.fromHex(CCSID.English,tita.substring(606, 622)));
        this.setPY_PAYMEMO_FILL(EbcdicConverter.fromHex(CCSID.English,tita.substring(622, 646)));
        this.setPY_CHARGCUS(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(646, 654).trim()), false, 1));
        this.setPY_CHARGUNT(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(654, 694).trim()), false, 0));
        this.setPY_PAYTXNOL1(EbcdicConverter.fromHex(CCSID.English,tita.substring(694, 742)));
        this.setEATM_RESERVE_FLAG(EbcdicConverter.fromHex(CCSID.English,tita.substring(742, 744)));
        this.setORIGINAL_TX_DAYTIME(EbcdicConverter.fromHex(CCSID.English,tita.substring(744, 772)));
        this.setCHANNEL_CHARGE(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(772, 776).trim()), false, 0));
        this.setDRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(776, 1200)));
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
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_SPECIAL_FLAG(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_HOST_BRANCH(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_IDNO(), 11) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_PAYUNTNO(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_TAXTYPE(), 5) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_PAYFEENO(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_PAYTXNOL(), 16) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_PAYDDATE(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_PAYMEMO_FILL(), 12) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getPY_CHARGCUS(), 3, false, 1, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getPY_CHARGUNT(), 20, false, 0, false) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_PAYTXNOL1(), 24) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getEATM_RESERVE_FLAG(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getORIGINAL_TX_DAYTIME(), 14) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getCHANNEL_CHARGE(), 2, false, 0, false) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 212) //
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
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPY_SPECIAL_FLAG(), StringUtils.EMPTY), 2," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPY_HOST_BRANCH(), StringUtils.EMPTY), 7," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPY_IDNO(), StringUtils.EMPTY), 11," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPY_PAYUNTNO(), StringUtils.EMPTY), 8," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPY_TAXTYPE(), StringUtils.EMPTY), 5," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPY_PAYFEENO(), StringUtils.EMPTY), 4," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPY_PAYTXNOL(), StringUtils.EMPTY), 16," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPY_PAYDDATE(), StringUtils.EMPTY), 8," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPY_PAYMEMO_FILL(), StringUtils.EMPTY), 12," ") //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getPY_CHARGCUS(), 3, false, 1, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getPY_CHARGUNT(), 20, false, 0, false) //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPY_PAYTXNOL1(), StringUtils.EMPTY), 24," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getEATM_RESERVE_FLAG(), StringUtils.EMPTY), 1," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getORIGINAL_TX_DAYTIME(), StringUtils.EMPTY), 14," ") //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getCHANNEL_CHARGE(), 2, false, 0, false) //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getDRVS(), StringUtils.EMPTY), 212," ") //
		;
	}
}
