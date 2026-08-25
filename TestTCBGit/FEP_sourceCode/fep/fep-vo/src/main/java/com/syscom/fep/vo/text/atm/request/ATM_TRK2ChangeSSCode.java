package com.syscom.fep.vo.text.atm.request;

import java.math.BigDecimal;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.atm.ATMTextBase;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.frmcommon.annotation.Field;

public class ATM_TRK2ChangeSSCode extends ATMTextBase {
	@Field(length = 8)
	private String APTRAN = StringUtils.EMPTY;
	@Field(length = 1)
	private String FILLER1 = StringUtils.EMPTY;
	@Field(length = 5)
	private String WSID = StringUtils.EMPTY;
	@Field(length = 1)
	private String MACMODE = StringUtils.EMPTY;
	@Field(length = 1)
	private String RECFMT = StringUtils.EMPTY;
	@Field(length = 1)
	private String APPLUSE = StringUtils.EMPTY;
	@Field(length = 1)
	private String MSGCAT = StringUtils.EMPTY;
	@Field(length = 2)
	private String MSGTYP = StringUtils.EMPTY;
	@Field(length = 6)
	private String TRANDATE = StringUtils.EMPTY;
	@Field(length = 6)
	private String TRANTIME = StringUtils.EMPTY;
	@Field(length = 4)
	private String TRANSEQ = StringUtils.EMPTY;
	@Field(length = 2)
	private String TDRSEG = StringUtils.EMPTY;
	@Field(length = 15)
	private String STATUS = StringUtils.EMPTY;
	@Field(length = 2)
	private String PIPTRIES = StringUtils.EMPTY;
	@Field(length = 16)
	private String PINCODE = StringUtils.EMPTY;
	@Field(length = 2)
	private String LANGID = StringUtils.EMPTY;
	@Field(length = 2)
	private String FSCODE = StringUtils.EMPTY;
	@Field(length = 2)
	private String FACODE = StringUtils.EMPTY;
	@Field(length = 20)
	private String FADATA = StringUtils.EMPTY;
	@Field(length = 2)
	private String TACODE = StringUtils.EMPTY;
	@Field(length = 20)
	private String TADATA = StringUtils.EMPTY;
	@Field(length = 11)
	private BigDecimal AMTNOND;
	@Field(length = 10)
	private BigDecimal AMTDISP;
	@Field(length = 1)
	private String DOCLASS = StringUtils.EMPTY;
	@Field(length = 1)
	private String CARDFMT = StringUtils.EMPTY;
	@Field(length = 50)
	private String CARDDATA = StringUtils.EMPTY;
	@Field(length = 21)
	private String CARDPART1 = StringUtils.EMPTY;
	@Field(length = 1)
	private String PART1 = StringUtils.EMPTY;
	@Field(length = 40)
	private String CARDPART2 = StringUtils.EMPTY;
	@Field(length = 1)
	private String PART2 = StringUtils.EMPTY;
	@Field(length = 37)
	private String CARDPART3 = StringUtils.EMPTY;
	@Field(length = 1)
	private String PART3 = StringUtils.EMPTY;
	@Field(length = 1)
	private String PART4 = StringUtils.EMPTY;
	@Field(length = 1)
	private String PARTV = StringUtils.EMPTY;
	@Field(length = 1)
	private String PART5 = StringUtils.EMPTY;
	@Field(length = 2)
	private String FILLER2 = StringUtils.EMPTY;
	@Field(length = 8)
	private String PICCMACD = StringUtils.EMPTY;
    private static final int _TotalLength = 306;

	public String getAPTRAN(){
	return APTRAN;
	}
	public void setAPTRAN(String APTRAN){
	this.APTRAN = APTRAN;
	}
	public String getFILLER1(){
	return FILLER1;
	}
	public void setFILLER1(String FILLER1){
	this.FILLER1 = FILLER1;
	}
	public String getWSID(){
	return WSID;
	}
	public void setWSID(String WSID){
	this.WSID = WSID;
	}
	public String getMACMODE(){
	return MACMODE;
	}
	public void setMACMODE(String MACMODE){
	this.MACMODE = MACMODE;
	}
	public String getRECFMT(){
	return RECFMT;
	}
	public void setRECFMT(String RECFMT){
	this.RECFMT = RECFMT;
	}
	public String getAPPLUSE(){
	return APPLUSE;
	}
	public void setAPPLUSE(String APPLUSE){
	this.APPLUSE = APPLUSE;
	}
	public String getMSGCAT(){
	return MSGCAT;
	}
	public void setMSGCAT(String MSGCAT){
	this.MSGCAT = MSGCAT;
	}
	public String getMSGTYP(){
	return MSGTYP;
	}
	public void setMSGTYP(String MSGTYP){
	this.MSGTYP = MSGTYP;
	}
	public String getTRANDATE(){
	return TRANDATE;
	}
	public void setTRANDATE(String TRANDATE){
	this.TRANDATE = TRANDATE;
	}
	public String getTRANTIME(){
	return TRANTIME;
	}
	public void setTRANTIME(String TRANTIME){
	this.TRANTIME = TRANTIME;
	}
	public String getTRANSEQ(){
	return TRANSEQ;
	}
	public void setTRANSEQ(String TRANSEQ){
	this.TRANSEQ = TRANSEQ;
	}
	public String getTDRSEG(){
	return TDRSEG;
	}
	public void setTDRSEG(String TDRSEG){
	this.TDRSEG = TDRSEG;
	}
	public String getSTATUS(){
	return STATUS;
	}
	public void setSTATUS(String STATUS){
	this.STATUS = STATUS;
	}
	public String getPIPTRIES(){
	return PIPTRIES;
	}
	public void setPIPTRIES(String PIPTRIES){
	this.PIPTRIES = PIPTRIES;
	}
	public String getPINCODE(){
	return PINCODE;
	}
	public void setPINCODE(String PINCODE){
	this.PINCODE = PINCODE;
	}
	public String getLANGID(){
	return LANGID;
	}
	public void setLANGID(String LANGID){
	this.LANGID = LANGID;
	}
	public String getFSCODE(){
	return FSCODE;
	}
	public void setFSCODE(String FSCODE){
	this.FSCODE = FSCODE;
	}
	public String getFACODE(){
	return FACODE;
	}
	public void setFACODE(String FACODE){
	this.FACODE = FACODE;
	}
	public String getFADATA(){
	return FADATA;
	}
	public void setFADATA(String FADATA){
	this.FADATA = FADATA;
	}
	public String getTACODE(){
	return TACODE;
	}
	public void setTACODE(String TACODE){
	this.TACODE = TACODE;
	}
	public String getTADATA(){
	return TADATA;
	}
	public void setTADATA(String TADATA){
	this.TADATA = TADATA;
	}
	public BigDecimal getAMTNOND(){
	return AMTNOND;
	}
	public void setAMTNOND(BigDecimal AMTNOND){
	this.AMTNOND = AMTNOND;
	}
	public BigDecimal getAMTDISP(){
	return AMTDISP;
	}
	public void setAMTDISP(BigDecimal AMTDISP){
	this.AMTDISP = AMTDISP;
	}
	public String getDOCLASS(){
	return DOCLASS;
	}
	public void setDOCLASS(String DOCLASS){
	this.DOCLASS = DOCLASS;
	}
	public String getCARDFMT(){
	return CARDFMT;
	}
	public void setCARDFMT(String CARDFMT){
	this.CARDFMT = CARDFMT;
	}
	public String getCARDDATA(){
	return CARDDATA;
	}
	public void setCARDDATA(String CARDDATA){
	this.CARDDATA = CARDDATA;
	}
	public String getCARDPART1(){
	return CARDPART1;
	}
	public void setCARDPART1(String CARDPART1){
	this.CARDPART1 = CARDPART1;
	}
	public String getPART1(){
	return PART1;
	}
	public void setPART1(String PART1){
	this.PART1 = PART1;
	}
	public String getCARDPART2(){
	return CARDPART2;
	}
	public void setCARDPART2(String CARDPART2){
	this.CARDPART2 = CARDPART2;
	}
	public String getPART2(){
	return PART2;
	}
	public void setPART2(String PART2){
	this.PART2 = PART2;
	}
	public String getCARDPART3(){
	return CARDPART3;
	}
	public void setCARDPART3(String CARDPART3){
	this.CARDPART3 = CARDPART3;
	}
	public String getPART3(){
	return PART3;
	}
	public void setPART3(String PART3){
	this.PART3 = PART3;
	}
	public String getPART4(){
	return PART4;
	}
	public void setPART4(String PART4){
	this.PART4 = PART4;
	}
	public String getPARTV(){
	return PARTV;
	}
	public void setPARTV(String PARTV){
	this.PARTV = PARTV;
	}
	public String getPART5(){
	return PART5;
	}
	public void setPART5(String PART5){
	this.PART5 = PART5;
	}
	public String getFILLER2(){
	return FILLER2;
	}
	public void setFILLER2(String FILLER2){
	this.FILLER2 = FILLER2;
	}
	public String getPICCMACD(){
	return PICCMACD;
	}
	public void setPICCMACD(String PICCMACD){
	this.PICCMACD = PICCMACD;
	}

    @Override
    public int getTotalLength() {
       return _TotalLength;
    }

    @Override
    public ATMGeneral parseFlatfile(String flatfile) throws Exception {
       return this.parseFlatfile(this.getClass(), flatfile);
    }

    @Override
    public String makeMessageFromGeneral(ATMGeneral general) {
       return null;
    }

    public String makeMessage() {
       return null;
    }

    @Override
    public void toGeneral(ATMGeneral general) throws Exception {
        ATMGeneralRequest request = general.getRequest();
        request.setAPTRAN(EbcdicConverter.fromHex(CCSID.English,this.getAPTRAN()));
        request.setFILLER1(EbcdicConverter.fromHex(CCSID.English,this.getFILLER1()));
        request.setWSID(EbcdicConverter.fromHex(CCSID.English,this.getWSID()));
        request.setMACMODE(EbcdicConverter.fromHex(CCSID.English,this.getMACMODE()));
        request.setRECFMT(EbcdicConverter.fromHex(CCSID.English,this.getRECFMT()));
        request.setAPPLUSE(EbcdicConverter.fromHex(CCSID.English,this.getAPPLUSE()));
        request.setMSGCAT(EbcdicConverter.fromHex(CCSID.English,this.getMSGCAT()));
        request.setMSGTYP(EbcdicConverter.fromHex(CCSID.English,this.getMSGTYP()));
        request.setTRANDATE(EbcdicConverter.fromHex(CCSID.English,this.getTRANDATE()));
        request.setTRANTIME(EbcdicConverter.fromHex(CCSID.English,this.getTRANTIME()));
        request.setTRANSEQ(EbcdicConverter.fromHex(CCSID.English,this.getTRANSEQ()));
        request.setTDRSEG(EbcdicConverter.fromHex(CCSID.English,this.getTDRSEG()));
        request.setSTATUS(EbcdicConverter.fromHex(CCSID.English,this.getSTATUS()));
        request.setPIPTRIES(EbcdicConverter.fromHex(CCSID.English,this.getPIPTRIES()));
        request.setPINCODE(EbcdicConverter.fromHex(CCSID.English,this.getPINCODE()));
        request.setLANGID(EbcdicConverter.fromHex(CCSID.English,this.getLANGID()));
        request.setFSCODE(EbcdicConverter.fromHex(CCSID.English,this.getFSCODE()));
        request.setFACODE(EbcdicConverter.fromHex(CCSID.English,this.getFACODE()));
        request.setFADATA(EbcdicConverter.fromHex(CCSID.English,this.getFADATA()));
        request.setTACODE(EbcdicConverter.fromHex(CCSID.English,this.getTACODE()));
        request.setTADATA(EbcdicConverter.fromHex(CCSID.English,this.getTADATA()));
        request.setAMTNOND(this.getAMTNOND());
        request.setAMTDISP(this.getAMTDISP());
        request.setDOCLASS(EbcdicConverter.fromHex(CCSID.English,this.getDOCLASS()));
        request.setCARDFMT(EbcdicConverter.fromHex(CCSID.English,this.getCARDFMT()));
        request.setCARDDATA(EbcdicConverter.fromHex(CCSID.English,this.getCARDDATA()));
        request.setCARDPART1(EbcdicConverter.fromHex(CCSID.English,this.getCARDPART1()));
        request.setPART1(EbcdicConverter.fromHex(CCSID.English,this.getPART1()));
        request.setCARDPART2(EbcdicConverter.fromHex(CCSID.English,this.getCARDPART2()));
        request.setPART2(EbcdicConverter.fromHex(CCSID.English,this.getPART2()));
        request.setCARDPART3(EbcdicConverter.fromHex(CCSID.English,this.getCARDPART3()));
        request.setPART3(EbcdicConverter.fromHex(CCSID.English,this.getPART3()));
        request.setPART4(EbcdicConverter.fromHex(CCSID.English,this.getPART4()));
        request.setPARTV(EbcdicConverter.fromHex(CCSID.English,this.getPARTV()));
        request.setPART5(EbcdicConverter.fromHex(CCSID.English,this.getPART5()));
        request.setFILLER2(EbcdicConverter.fromHex(CCSID.English,this.getFILLER2()));
        request.setPICCMACD(EbcdicConverter.fromHex(CCSID.English,this.getPICCMACD()));
    }
}
