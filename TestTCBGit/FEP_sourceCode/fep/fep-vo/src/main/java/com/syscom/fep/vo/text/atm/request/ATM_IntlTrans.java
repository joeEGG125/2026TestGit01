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

public class ATM_IntlTrans extends ATMTextBase {
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
	private String SSCODE = StringUtils.EMPTY;
	@Field(length = 2)
	private String LANGID = StringUtils.EMPTY;
	@Field(length = 2)
	private String FSCODE = StringUtils.EMPTY;
	@Field(length = 2)
	private String ACCODE = StringUtils.EMPTY;
	@Field(length = 3)
	private String PICDSEQ = StringUtils.EMPTY;
	@Field(length = 4)
	private String PIPOSENT = StringUtils.EMPTY;
	@Field(length = 4)
	private String PITRMTYP = StringUtils.EMPTY;
	@Field(length = 1)
	private String PITK2FRM = StringUtils.EMPTY;
	@Field(length = 1)
	private String PIARPCRC = StringUtils.EMPTY;
	@Field(length = 7)
	private String FILLER2 = StringUtils.EMPTY;
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
	private String TRK2 = StringUtils.EMPTY;
	@Field(length = 5)
	private String FILLER3 = StringUtils.EMPTY;
	@Field(length = 4)
	private String PICCPCOD = StringUtils.EMPTY;
	@Field(length = 2)
	private String PIARQCLN = StringUtils.EMPTY;
	@Field(length = 185)
	private String PIARQC = StringUtils.EMPTY;
	@Field(length = 8)
	private String PICCMACD = StringUtils.EMPTY;
	@Field(length = 125)
	private String FILLER4 = StringUtils.EMPTY;
    private static final int _TotalLength = 508;

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
	public String getSSCODE(){
	return SSCODE;
	}
	public void setSSCODE(String SSCODE){
	this.SSCODE = SSCODE;
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
	public String getACCODE(){
	return ACCODE;
	}
	public void setACCODE(String ACCODE){
	this.ACCODE = ACCODE;
	}
	public String getPICDSEQ(){
	return PICDSEQ;
	}
	public void setPICDSEQ(String PICDSEQ){
	this.PICDSEQ = PICDSEQ;
	}
	public String getPIPOSENT(){
	return PIPOSENT;
	}
	public void setPIPOSENT(String PIPOSENT){
	this.PIPOSENT = PIPOSENT;
	}
	public String getPITRMTYP(){
	return PITRMTYP;
	}
	public void setPITRMTYP(String PITRMTYP){
	this.PITRMTYP = PITRMTYP;
	}
	public String getPITK2FRM(){
	return PITK2FRM;
	}
	public void setPITK2FRM(String PITK2FRM){
	this.PITK2FRM = PITK2FRM;
	}
	public String getPIARPCRC(){
	return PIARPCRC;
	}
	public void setPIARPCRC(String PIARPCRC){
	this.PIARPCRC = PIARPCRC;
	}
	public String getFILLER2(){
	return FILLER2;
	}
	public void setFILLER2(String FILLER2){
	this.FILLER2 = FILLER2;
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
	public String getTRK2(){
	return TRK2;
	}
	public void setTRK2(String TRK2){
	this.TRK2 = TRK2;
	}
	public String getFILLER3(){
	return FILLER3;
	}
	public void setFILLER3(String FILLER3){
	this.FILLER3 = FILLER3;
	}
	public String getPICCPCOD(){
	return PICCPCOD;
	}
	public void setPICCPCOD(String PICCPCOD){
	this.PICCPCOD = PICCPCOD;
	}
	public String getPIARQCLN(){
	return PIARQCLN;
	}
	public void setPIARQCLN(String PIARQCLN){
	this.PIARQCLN = PIARQCLN;
	}
	public String getPIARQC(){
	return PIARQC;
	}
	public void setPIARQC(String PIARQC){
	this.PIARQC = PIARQC;
	}
	public String getPICCMACD(){
	return PICCMACD;
	}
	public void setPICCMACD(String PICCMACD){
	this.PICCMACD = PICCMACD;
	}
	public String getFILLER4(){
	return FILLER4;
	}
	public void setFILLER4(String FILLER4){
	this.FILLER4 = FILLER4;
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
        request.setSSCODE(EbcdicConverter.fromHex(CCSID.English,this.getSSCODE()));
        request.setLANGID(EbcdicConverter.fromHex(CCSID.English,this.getLANGID()));
        request.setFSCODE(EbcdicConverter.fromHex(CCSID.English,this.getFSCODE()));
        request.setACCODE(EbcdicConverter.fromHex(CCSID.English,this.getACCODE()));
        request.setPICDSEQ(EbcdicConverter.fromHex(CCSID.English,this.getPICDSEQ()));
        request.setPIPOSENT(EbcdicConverter.fromHex(CCSID.English,this.getPIPOSENT()));
        request.setPITRMTYP(EbcdicConverter.fromHex(CCSID.English,this.getPITRMTYP()));
        request.setPITK2FRM(EbcdicConverter.fromHex(CCSID.English,this.getPITK2FRM()));
        request.setPIARPCRC(EbcdicConverter.fromHex(CCSID.English,this.getPIARPCRC()));
        request.setFILLER2(EbcdicConverter.fromHex(CCSID.English,this.getFILLER2()));
        request.setTACODE(EbcdicConverter.fromHex(CCSID.English,this.getTACODE()));
        request.setTADATA(EbcdicConverter.fromHex(CCSID.English,this.getTADATA()));
        request.setAMTNOND(this.getAMTNOND());
        request.setAMTDISP(this.getAMTDISP());
        request.setDOCLASS(EbcdicConverter.fromHex(CCSID.English,this.getDOCLASS()));
        request.setCARDFMT(EbcdicConverter.fromHex(CCSID.English,this.getCARDFMT()));
        request.setTRK2(EbcdicConverter.fromHex(CCSID.English,this.getTRK2()));
        request.setFILLER3(EbcdicConverter.fromHex(CCSID.English,this.getFILLER3()));
        request.setPICCPCOD(EbcdicConverter.fromHex(CCSID.English,this.getPICCPCOD()));
        request.setPIARQCLN(this.getPIARQCLN());
        request.setPIARQC(this.getPIARQC());
        request.setPICCMACD(EbcdicConverter.fromHex(CCSID.English,this.getPICCMACD()));
        request.setFILLER4(EbcdicConverter.fromHex(CCSID.English,this.getFILLER4()));
    }
}
