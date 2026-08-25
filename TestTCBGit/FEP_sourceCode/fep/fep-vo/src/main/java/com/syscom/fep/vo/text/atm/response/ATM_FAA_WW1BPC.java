package com.syscom.fep.vo.text.atm.response;

import java.math.BigDecimal;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralResponse;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.atm.ATMTextBase;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.frmcommon.annotation.Field;

public class ATM_FAA_WW1BPC extends ATMTextBase {
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
	
	@Field(length = 2)
	private String DATATYPE = StringUtils.EMPTY;
	
	@Field(length = 3)
	private String DATALEN = StringUtils.EMPTY;
	
	@Field(length = 1)
	private String ACKNOW = StringUtils.EMPTY;
	
	@Field(length = 3)
	private String PAGENO = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String PTYPE = StringUtils.EMPTY;
	
	@Field(length = 3)
	private String PLEN = StringUtils.EMPTY;
	
	@Field(length = 6)
	private String PBMPNO = StringUtils.EMPTY;
	
	@Field(length = 8)
	private String PDATE = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String S2 = ";/";
	
	@Field(length = 8)
	private String PTIME = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String S3 = ";/";
	
	@Field(length = 2)
	private String PTXTYPE = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String S4 = ";/";
	
	@Field(length = 8)
	private String PTID = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String S5 = ";/";
	
	@Field(length = 11)
	private String PTXAMT = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String S6 = ";/";
	
	@Field(length = 4)
	private String PFEE = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String S7 = ";/";
	
	@Field(length = 17)
	private String PBAL = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String S8 = ";/";
	
	@Field(length = 16)
	private String PACCNO = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String S9 = ";/";
	
	@Field(length = 16)
	private String PTRINACCT = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String S10 = ";/";
	
	@Field(length = 3)
	private String PATXBKNO = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String S11 = ";/";
	
	@Field(length = 7)
	private String PSTAN = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String S12 = ";/";
	
	@Field(length = 8)
	private String PBUSINESSDATE = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String S13 = ";/";
	
	@Field(length = 4)
	private String PRCCODE = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String S14 = ";/";
	
	@Field(length = 2)
	private String PFILLER1 = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String S15 = ";/";
	
	@Field(length = 3)
	private String POTXBKNO = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String S16 = ";/";
	
	@Field(length = 3)
	private String PITXBKNO = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String S17 = ";#";
	
	@Field(length = 8)
	private String MACCODE = StringUtils.EMPTY;
	
	private static final int _TotalLength = 210;

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
    
	public String getDATATYPE(){
		return DATATYPE;
	}
	
	public void setDATATYPE(String DATATYPE){
		this.DATATYPE = DATATYPE;
	}
    
	public String getDATALEN(){
		return DATALEN;
	}
	
	public void setDATALEN(String DATALEN){
		this.DATALEN = DATALEN;
	}
    
	public String getACKNOW(){
		return ACKNOW;
	}
	
	public void setACKNOW(String ACKNOW){
		this.ACKNOW = ACKNOW;
	}
    
	public String getPAGENO(){
		return PAGENO;
	}
	
	public void setPAGENO(String PAGENO){
		this.PAGENO = PAGENO;
	}
    
	public String getPTYPE(){
		return PTYPE;
	}
	
	public void setPTYPE(String PTYPE){
		this.PTYPE = PTYPE;
	}
    
	public String getPLEN(){
		return PLEN;
	}
	
	public void setPLEN(String PLEN){
		this.PLEN = PLEN;
	}
    
	public String getPBMPNO(){
		return PBMPNO;
	}
	
	public void setPBMPNO(String PBMPNO){
		this.PBMPNO = PBMPNO;
	}
    
	public String getPDATE(){
		return PDATE;
	}
	
	public void setPDATE(String PDATE){
		this.PDATE = PDATE;
	}
    
	public String getS2(){
		return S2;
	}
	
	public void setS2(String S2){
		this.S2 = S2;
	}
    
	public String getPTIME(){
		return PTIME;
	}
	
	public void setPTIME(String PTIME){
		this.PTIME = PTIME;
	}
    
	public String getS3(){
		return S3;
	}
	
	public void setS3(String S3){
		this.S3 = S3;
	}
    
	public String getPTXTYPE(){
		return PTXTYPE;
	}
	
	public void setPTXTYPE(String PTXTYPE){
		this.PTXTYPE = PTXTYPE;
	}
    
	public String getS4(){
		return S4;
	}
	
	public void setS4(String S4){
		this.S4 = S4;
	}
    
	public String getPTID(){
		return PTID;
	}
	
	public void setPTID(String PTID){
		this.PTID = PTID;
	}
    
	public String getS5(){
		return S5;
	}
	
	public void setS5(String S5){
		this.S5 = S5;
	}
    
	public String getPTXAMT(){
		return PTXAMT;
	}
	
	public void setPTXAMT(String PTXAMT){
		this.PTXAMT = PTXAMT;
	}
    
	public String getS6(){
		return S6;
	}
	
	public void setS6(String S6){
		this.S6 = S6;
	}
    
	public String getPFEE(){
		return PFEE;
	}
	
	public void setPFEE(String PFEE){
		this.PFEE = PFEE;
	}
    
	public String getS7(){
		return S7;
	}
	
	public void setS7(String S7){
		this.S7 = S7;
	}
    
	public String getPBAL(){
		return PBAL;
	}
	
	public void setPBAL(String PBAL){
		this.PBAL = PBAL;
	}
    
	public String getS8(){
		return S8;
	}
	
	public void setS8(String S8){
		this.S8 = S8;
	}
    
	public String getPACCNO(){
		return PACCNO;
	}
	
	public void setPACCNO(String PACCNO){
		this.PACCNO = PACCNO;
	}
    
	public String getS9(){
		return S9;
	}
	
	public void setS9(String S9){
		this.S9 = S9;
	}
    
	public String getPTRINACCT(){
		return PTRINACCT;
	}
	
	public void setPTRINACCT(String PTRINACCT){
		this.PTRINACCT = PTRINACCT;
	}
    
	public String getS10(){
		return S10;
	}
	
	public void setS10(String S10){
		this.S10 = S10;
	}
    
	public String getPATXBKNO(){
		return PATXBKNO;
	}
	
	public void setPATXBKNO(String PATXBKNO){
		this.PATXBKNO = PATXBKNO;
	}
    
	public String getS11(){
		return S11;
	}
	
	public void setS11(String S11){
		this.S11 = S11;
	}
    
	public String getPSTAN(){
		return PSTAN;
	}
	
	public void setPSTAN(String PSTAN){
		this.PSTAN = PSTAN;
	}
    
	public String getS12(){
		return S12;
	}
	
	public void setS12(String S12){
		this.S12 = S12;
	}
    
	public String getPBUSINESSDATE(){
		return PBUSINESSDATE;
	}
	
	public void setPBUSINESSDATE(String PBUSINESSDATE){
		this.PBUSINESSDATE = PBUSINESSDATE;
	}
    
	public String getS13(){
		return S13;
	}
	
	public void setS13(String S13){
		this.S13 = S13;
	}
    
	public String getPRCCODE(){
		return PRCCODE;
	}
	
	public void setPRCCODE(String PRCCODE){
		this.PRCCODE = PRCCODE;
	}
    
	public String getS14(){
		return S14;
	}
	
	public void setS14(String S14){
		this.S14 = S14;
	}
    
	public String getPFILLER1(){
		return PFILLER1;
	}
	
	public void setPFILLER1(String PFILLER1){
		this.PFILLER1 = PFILLER1;
	}
    
	public String getS15(){
		return S15;
	}
	
	public void setS15(String S15){
		this.S15 = S15;
	}
    
	public String getPOTXBKNO(){
		return POTXBKNO;
	}
	
	public void setPOTXBKNO(String POTXBKNO){
		this.POTXBKNO = POTXBKNO;
	}
    
	public String getS16(){
		return S16;
	}
	
	public void setS16(String S16){
		this.S16 = S16;
	}
    
	public String getPITXBKNO(){
		return PITXBKNO;
	}
	
	public void setPITXBKNO(String PITXBKNO){
		this.PITXBKNO = PITXBKNO;
	}
    
	public String getS17(){
		return S17;
	}
	
	public void setS17(String S17){
		this.S17 = S17;
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
		this.setDATATYPE(StringUtils.rightPad(this.getDATATYPE(),2,StringUtils.SPACE));
		this.setDATALEN(StringUtils.rightPad(this.getDATALEN(),3,StringUtils.SPACE));
		this.setACKNOW(StringUtils.rightPad(this.getACKNOW(),1,StringUtils.SPACE));
		this.setPAGENO(StringUtils.rightPad(this.getPAGENO(),3,StringUtils.SPACE));
		this.setPTYPE(StringUtils.rightPad(this.getPTYPE(),2,StringUtils.SPACE));
		this.setPLEN(StringUtils.rightPad(this.getPLEN(),3,StringUtils.SPACE));
		this.setPBMPNO(StringUtils.rightPad(this.getPBMPNO(),6,StringUtils.SPACE));
		this.setPDATE(StringUtils.rightPad(this.getPDATE(),8,StringUtils.SPACE));
		this.setS2(StringUtils.rightPad(this.getS2(),2,StringUtils.SPACE));
		this.setPTIME(StringUtils.rightPad(this.getPTIME(),8,StringUtils.SPACE));
		this.setS3(StringUtils.rightPad(this.getS3(),2,StringUtils.SPACE));
		this.setPTXTYPE(StringUtils.rightPad(this.getPTXTYPE(),2,StringUtils.SPACE));
		this.setS4(StringUtils.rightPad(this.getS4(),2,StringUtils.SPACE));
		this.setPTID(StringUtils.rightPad(this.getPTID(),8,StringUtils.SPACE));
		this.setS5(StringUtils.rightPad(this.getS5(),2,StringUtils.SPACE));
		this.setPTXAMT(StringUtils.rightPad(this.getPTXAMT(),11,StringUtils.SPACE));
		this.setS6(StringUtils.rightPad(this.getS6(),2,StringUtils.SPACE));
		this.setPFEE(StringUtils.rightPad(this.getPFEE(),4,StringUtils.SPACE));
		this.setS7(StringUtils.rightPad(this.getS7(),2,StringUtils.SPACE));
		this.setPBAL(StringUtils.rightPad(this.getPBAL(),17,StringUtils.SPACE));
		this.setS8(StringUtils.rightPad(this.getS8(),2,StringUtils.SPACE));
		this.setPACCNO(StringUtils.rightPad(this.getPACCNO(),16,StringUtils.SPACE));
		this.setS9(StringUtils.rightPad(this.getS9(),2,StringUtils.SPACE));
		this.setPTRINACCT(StringUtils.rightPad(this.getPTRINACCT(),16,StringUtils.SPACE));
		this.setS10(StringUtils.rightPad(this.getS10(),2,StringUtils.SPACE));
		this.setPATXBKNO(StringUtils.rightPad(this.getPATXBKNO(),3,StringUtils.SPACE));
		this.setS11(StringUtils.rightPad(this.getS11(),2,StringUtils.SPACE));
		this.setPSTAN(StringUtils.rightPad(this.getPSTAN(),7,StringUtils.SPACE));
		this.setS12(StringUtils.rightPad(this.getS12(),2,StringUtils.SPACE));
		this.setPBUSINESSDATE(StringUtils.rightPad(this.getPBUSINESSDATE(),8,StringUtils.SPACE));
		this.setS13(StringUtils.rightPad(this.getS13(),2,StringUtils.SPACE));
		this.setPRCCODE(StringUtils.rightPad(this.getPRCCODE(),4,StringUtils.SPACE));
		this.setS14(StringUtils.rightPad(this.getS14(),2,StringUtils.SPACE));
		this.setPFILLER1(StringUtils.rightPad(this.getPFILLER1(),2,StringUtils.SPACE));
		this.setS15(StringUtils.rightPad(this.getS15(),2,StringUtils.SPACE));
		this.setPOTXBKNO(StringUtils.rightPad(this.getPOTXBKNO(),3,StringUtils.SPACE));
		this.setS16(StringUtils.rightPad(this.getS16(),2,StringUtils.SPACE));
		this.setPITXBKNO(StringUtils.rightPad(this.getPITXBKNO(),3,StringUtils.SPACE));
		this.setS17(StringUtils.rightPad(this.getS17(),2,StringUtils.SPACE));
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
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDATATYPE(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDATALEN(),3) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getACKNOW(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPAGENO(),3) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPTYPE(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPLEN(),3) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPBMPNO(),6) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPDATE(),8) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS2(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPTIME(),8) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS3(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPTXTYPE(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS4(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPTID(),8) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS5(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPTXAMT(),11) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS6(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPFEE(),4) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS7(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPBAL(),17) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS8(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPACCNO(),16) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS9(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPTRINACCT(),16) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS10(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPATXBKNO(),3) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS11(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPSTAN(),7) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS12(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPBUSINESSDATE(),8) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS13(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPRCCODE(),4) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS14(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPFILLER1(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS15(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPOTXBKNO(),3) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS16(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPITXBKNO(),3) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS17(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMACCODE(),8)
		      ;
	}

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {
	}
}
