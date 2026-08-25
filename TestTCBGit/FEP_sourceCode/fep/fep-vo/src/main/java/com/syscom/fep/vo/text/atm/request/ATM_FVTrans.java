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

public class ATM_FVTrans extends ATMTextBase {
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
	@Field(length = 10)
	private String PIFVCID1 = StringUtils.EMPTY;
	@Field(length = 8)
	private String PITMID = StringUtils.EMPTY;
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
	@Field(length = 408)
	private String FVDATA = StringUtils.EMPTY;
    private static final int _TotalLength = 507;

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
	public String getPIFVCID1(){
	return PIFVCID1;
	}
	public void setPIFVCID1(String PIFVCID1){
	this.PIFVCID1 = PIFVCID1;
	}
	public String getPITMID(){
	return PITMID;
	}
	public void setPITMID(String PITMID){
	this.PITMID = PITMID;
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
	public String getFVDATA(){
	return FVDATA;
	}
	public void setFVDATA(String FVDATA){
	this.FVDATA = FVDATA;
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
        request.setPIFVCID1(EbcdicConverter.fromHex(CCSID.English,this.getPIFVCID1()));
        request.setPITMID(EbcdicConverter.fromHex(CCSID.English,this.getPITMID()));
        request.setLANGID(EbcdicConverter.fromHex(CCSID.English,this.getLANGID()));
        request.setFSCODE(EbcdicConverter.fromHex(CCSID.English,this.getFSCODE()));
        request.setFACODE(EbcdicConverter.fromHex(CCSID.English,this.getFACODE()));
        request.setFADATA(EbcdicConverter.fromHex(CCSID.English,this.getFADATA()));
        request.setTACODE(EbcdicConverter.fromHex(CCSID.English,this.getTACODE()));
        request.setFVDATA(this.getFVDATA());
    }
}
