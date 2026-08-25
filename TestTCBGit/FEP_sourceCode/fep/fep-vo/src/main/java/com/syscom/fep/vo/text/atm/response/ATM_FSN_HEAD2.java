package com.syscom.fep.vo.text.atm.response;

import java.math.BigDecimal;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralResponse;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.atm.ATMTextBase;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.frmcommon.annotation.Field;

public class ATM_FSN_HEAD2 extends ATMTextBase {
	@Field(length = 5)
	private String WSID = StringUtils.EMPTY;
	
	@Field(length = 1)
	private String S1 = StringUtils.EMPTY;
	
	@Field(length = 1)
	private String RECFMT = StringUtils.EMPTY;
	
	@Field(length = 1)
	private String POAPUSE = StringUtils.EMPTY;
	
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
	
	@Field(length = 8)
	private String MACCODE = StringUtils.EMPTY;
	
	private static final int _TotalLength = 38;

	public String getWSID(){
		return WSID;
	}
	
	public void setWSID(String WSID){
		this.WSID = WSID;
	}
    
	public String getS1(){
		return S1;
	}
	
	public void setS1(String S1){
		this.S1 = S1;
	}
    
	public String getRECFMT(){
		return RECFMT;
	}
	
	public void setRECFMT(String RECFMT){
		this.RECFMT = RECFMT;
	}
    
	public String getPOAPUSE(){
		return POAPUSE;
	}
	
	public void setPOAPUSE(String POAPUSE){
		this.POAPUSE = POAPUSE;
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
		this.setS1(StringUtils.rightPad(this.getS1(),1,StringUtils.SPACE));
		this.setRECFMT(StringUtils.rightPad(this.getRECFMT(),1,StringUtils.SPACE));
		this.setPOAPUSE(StringUtils.rightPad(this.getPOAPUSE(),1,StringUtils.SPACE));
		this.setMSGCAT(StringUtils.rightPad(this.getMSGCAT(),1,StringUtils.SPACE));
		this.setMSGTYP(StringUtils.rightPad(this.getMSGTYP(),2,StringUtils.SPACE));
		this.setTRANDATE(StringUtils.rightPad(this.getTRANDATE(),6,StringUtils.SPACE));
		this.setTRANTIME(StringUtils.rightPad(this.getTRANTIME(),6,StringUtils.SPACE));
		this.setTRANSEQ(StringUtils.rightPad(this.getTRANSEQ(),4,StringUtils.SPACE));
		this.setTDRSEG(StringUtils.rightPad(this.getTDRSEG(),2,StringUtils.SPACE));
		this.setPRCRDACT(StringUtils.rightPad(this.getPRCRDACT(),1,StringUtils.SPACE));
		this.setMACCODE(StringUtils.rightPad(this.getMACCODE(),8,StringUtils.SPACE));

		return CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getWSID(),5) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS1(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getRECFMT(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPOAPUSE(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMSGCAT(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMSGTYP(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRANDATE(),6) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRANTIME(),6) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRANSEQ(),4) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTDRSEG(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPRCRDACT(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMACCODE(),8)
		      ;
	}

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {
	}
}
