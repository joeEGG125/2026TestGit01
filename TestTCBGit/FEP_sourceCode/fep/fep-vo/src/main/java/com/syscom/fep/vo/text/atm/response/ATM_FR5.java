package com.syscom.fep.vo.text.atm.response;

import java.math.BigDecimal;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralResponse;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.atm.ATMTextBase;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.frmcommon.annotation.Field;

public class ATM_FR5 extends ATMTextBase {
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
	
	@Field(length = 1)
	private String PRCRDACT = StringUtils.EMPTY;
	
	@Field(length = 69)
	private String RATEDATA = StringUtils.EMPTY;
	
	@Field(length = 8)
	private String MACCODE = StringUtils.EMPTY;
	
	private static final int _TotalLength = 107;

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
    
	public String getPRCRDACT(){
		return PRCRDACT;
	}
	
	public void setPRCRDACT(String PRCRDACT){
		this.PRCRDACT = PRCRDACT;
	}
    
	public String getRATEDATA(){
		return RATEDATA;
	}
	
	public void setRATEDATA(String RATEDATA){
		this.RATEDATA = RATEDATA;
	}
    
	public String getMACCODE(){
		return MACCODE;
	}
	
	public void setMACCODE(String MACCODE){
		this.MACCODE = MACCODE;
	}
    
	@Override
	public int getTotalLength() {
		return _TotalLength;
	}

	@Override
	public ATMGeneral parseFlatfile(String flatfile) throws Exception {
		return null;
	}

	@Override
	public String makeMessageFromGeneral(ATMGeneral general) {
		return null;
	}

	public String makeMessage() {
		this.setWSID(StringUtils.rightPad(this.getWSID(),5,StringUtils.SPACE));
		this.setMACMODE(StringUtils.rightPad(this.getMACMODE(),1,StringUtils.SPACE));
		this.setRECFMT(StringUtils.rightPad(this.getRECFMT(),1,StringUtils.SPACE));
		this.setAPPLUSE(StringUtils.rightPad(this.getAPPLUSE(),1,StringUtils.SPACE));
		this.setMSGCAT(StringUtils.rightPad(this.getMSGCAT(),1,StringUtils.SPACE));
		this.setMSGTYP(StringUtils.rightPad(this.getMSGTYP(),2,StringUtils.SPACE));
		this.setTRANDATE(StringUtils.rightPad(this.getTRANDATE(),6,StringUtils.SPACE));
		this.setTRANTIME(StringUtils.rightPad(this.getTRANTIME(),6,StringUtils.SPACE));
		this.setTRANSEQ(StringUtils.rightPad(this.getTRANSEQ(),4,StringUtils.SPACE));
		this.setTDRSEG(StringUtils.rightPad(this.getTDRSEG(),2,StringUtils.SPACE));
		this.setPRCRDACT(StringUtils.rightPad(this.getPRCRDACT(),1,StringUtils.SPACE));
		this.setRATEDATA(StringUtils.rightPad(this.getRATEDATA(),69,StringUtils.SPACE));
		this.setMACCODE(StringUtils.rightPad(this.getMACCODE(),8,StringUtils.SPACE));

		return CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getWSID(),5) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMACMODE(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getRECFMT(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAPPLUSE(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMSGCAT(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMSGTYP(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRANDATE(),6) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRANTIME(),6) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRANSEQ(),4) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTDRSEG(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPRCRDACT(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getRATEDATA(),69) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMACCODE(),8)
		      ;
	}

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {
	}
}
