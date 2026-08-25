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

public class ATM_GeneralTrans extends ATMTextBase {
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
	@Field(length = 18)
	private String IPYDATA = StringUtils.EMPTY;
	@Field(length = 2)
	private String LANGID = StringUtils.EMPTY;
	@Field(length = 2)
	private String FSCODE = StringUtils.EMPTY;
	@Field(length = 22)
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
	@Field(length = 37)
	private String CARDDATA = StringUtils.EMPTY;
	@Field(length = 1)
	private String PICCDID = StringUtils.EMPTY;
	@Field(length = 1)
	private String FILLER2 = StringUtils.EMPTY;
	@Field(length = 3)
	private String PICCDLTH = StringUtils.EMPTY;
	@Field(length = 4)
	private String PICCPCOD = StringUtils.EMPTY;
	@Field(length = 1)
	private String PICCTXID = StringUtils.EMPTY;
	@Field(length = 8)
	private String PICCBI9 = StringUtils.EMPTY;
	@Field(length = 8)
	private String PICCBI11 = StringUtils.EMPTY;
	@Field(length = 14)
	private String PICCBI19 = StringUtils.EMPTY;
	@Field(length = 4)
	private String PICCBI28 = StringUtils.EMPTY;
	@Field(length = 30)
	private String PICCBI55 = StringUtils.EMPTY;
	@Field(length = 118)
	private String SPECIALDATA = StringUtils.EMPTY;
	@Field(length = 8)
	private String PICCMACD = StringUtils.EMPTY;
	@Field(length = 10)
	private String PICCTAC = StringUtils.EMPTY;
	@Field(length = 120)
	private String FILLER3 = StringUtils.EMPTY;
    private static final int _TotalLength = 509;

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
	public String getIPYDATA(){
	return IPYDATA;
	}
	public void setIPYDATA(String IPYDATA){
	this.IPYDATA = IPYDATA;
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
	public String getPICCDID(){
	return PICCDID;
	}
	public void setPICCDID(String PICCDID){
	this.PICCDID = PICCDID;
	}
	public String getFILLER2(){
	return FILLER2;
	}
	public void setFILLER2(String FILLER2){
	this.FILLER2 = FILLER2;
	}
	public String getPICCDLTH(){
	return PICCDLTH;
	}
	public void setPICCDLTH(String PICCDLTH){
	this.PICCDLTH = PICCDLTH;
	}
	public String getPICCPCOD(){
	return PICCPCOD;
	}
	public void setPICCPCOD(String PICCPCOD){
	this.PICCPCOD = PICCPCOD;
	}
	public String getPICCTXID(){
	return PICCTXID;
	}
	public void setPICCTXID(String PICCTXID){
	this.PICCTXID = PICCTXID;
	}
	public String getPICCBI9(){
	return PICCBI9;
	}
	public void setPICCBI9(String PICCBI9){
	this.PICCBI9 = PICCBI9;
	}
	public String getPICCBI11(){
	return PICCBI11;
	}
	public void setPICCBI11(String PICCBI11){
	this.PICCBI11 = PICCBI11;
	}
	public String getPICCBI19(){
	return PICCBI19;
	}
	public void setPICCBI19(String PICCBI19){
	this.PICCBI19 = PICCBI19;
	}
	public String getPICCBI28(){
	return PICCBI28;
	}
	public void setPICCBI28(String PICCBI28){
	this.PICCBI28 = PICCBI28;
	}
	public String getPICCBI55(){
	return PICCBI55;
	}
	public void setPICCBI55(String PICCBI55){
	this.PICCBI55 = PICCBI55;
	}
	public String getSPECIALDATA(){
	return SPECIALDATA;
	}
	public void setSPECIALDATA(String SPECIALDATA){
	this.SPECIALDATA = SPECIALDATA;
	}
	public String getPICCMACD(){
	return PICCMACD;
	}
	public void setPICCMACD(String PICCMACD){
	this.PICCMACD = PICCMACD;
	}
	public String getPICCTAC(){
	return PICCTAC;
	}
	public void setPICCTAC(String PICCTAC){
	this.PICCTAC = PICCTAC;
	}
	public String getFILLER3(){
	return FILLER3;
	}
	public void setFILLER3(String FILLER3){
	this.FILLER3 = FILLER3;
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
        request.setIPYDATA(EbcdicConverter.fromHex(CCSID.English,this.getIPYDATA()));
        request.setLANGID(EbcdicConverter.fromHex(CCSID.English,this.getLANGID()));
        request.setFSCODE(EbcdicConverter.fromHex(CCSID.English,this.getFSCODE()));
        request.setFADATA(EbcdicConverter.fromHex(CCSID.English,this.getFADATA()));
        request.setTACODE(EbcdicConverter.fromHex(CCSID.English,this.getTACODE()));
        request.setTADATA(EbcdicConverter.fromHex(CCSID.English,this.getTADATA()));
        request.setAMTNOND(this.getAMTNOND());
        request.setAMTDISP(this.getAMTDISP());
        request.setDOCLASS(EbcdicConverter.fromHex(CCSID.English,this.getDOCLASS()));
        request.setCARDFMT(EbcdicConverter.fromHex(CCSID.English,this.getCARDFMT()));
        request.setCARDDATA(EbcdicConverter.fromHex(CCSID.English,this.getCARDDATA()));
        request.setPICCDID(EbcdicConverter.fromHex(CCSID.English,this.getPICCDID()));
        request.setFILLER2(EbcdicConverter.fromHex(CCSID.English,this.getFILLER2()));
        request.setPICCDLTH(EbcdicConverter.fromHex(CCSID.English,this.getPICCDLTH()));
        request.setPICCPCOD(EbcdicConverter.fromHex(CCSID.English,this.getPICCPCOD()));
        request.setPICCTXID(EbcdicConverter.fromHex(CCSID.English,this.getPICCTXID()));
        request.setPICCBI9(EbcdicConverter.fromHex(CCSID.English,this.getPICCBI9()));
        request.setPICCBI11(EbcdicConverter.fromHex(CCSID.English,this.getPICCBI11()));
        request.setPICCBI19(EbcdicConverter.fromHex(CCSID.English,this.getPICCBI19()));
        request.setPICCBI28(EbcdicConverter.fromHex(CCSID.English,this.getPICCBI28()));
        request.setPICCBI55(this.getPICCBI55());
        request.setSPECIALDATA(EbcdicConverter.fromHex(CCSID.English,this.getSPECIALDATA()));
        request.setPICCMACD(EbcdicConverter.fromHex(CCSID.English,this.getPICCMACD()));
        request.setPICCTAC(this.getPICCTAC());
        request.setFILLER3(EbcdicConverter.fromHex(CCSID.English,this.getFILLER3()));
    }
}
