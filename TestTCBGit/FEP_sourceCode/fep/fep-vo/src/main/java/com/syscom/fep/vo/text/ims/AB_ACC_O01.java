package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class AB_ACC_O01 extends IMSTextBase {
    @Field(length = 24)
	private String OUTMSGID = StringUtils.EMPTY;

    @Field(length = 1)
	private String OUTMSGF = StringUtils.EMPTY;

    @Field(length = 10)
	private String OUTSTAN = StringUtils.EMPTY;

    @Field(length = 8)
	private String OUTDATE = StringUtils.EMPTY;

    @Field(length = 6)
	private String OUTTIME = StringUtils.EMPTY;

    @Field(length = 4)
	private String OUTSERV = StringUtils.EMPTY;

    @Field(length = 8)
	private String OUTTD = StringUtils.EMPTY;

    @Field(length = 5)
	private String OUTAP = StringUtils.EMPTY;

    @Field(length = 10)
	private String OUTID = StringUtils.EMPTY;

    @Field(length = 1)
	private String OUTFF = StringUtils.EMPTY;

    @Field(length = 3)
	private String OUTPGNO = StringUtils.EMPTY;

    @Field(length = 4)
	private String OUTFIX = StringUtils.EMPTY;

    @Field(length = 4)
	private String OUTV1CT = StringUtils.EMPTY;

    @Field(length = 4)
	private String OUTRTC = StringUtils.EMPTY;

    @Field(length = 1)
	private String OUTBILL = StringUtils.EMPTY;

    @Field(length = 2)
	private String OUTTIMES = StringUtils.EMPTY;

    @Field(length = 22)
	private String OUTRSV = StringUtils.EMPTY;

    @Field(length = 1)
	private String RECTYPE = StringUtils.EMPTY;

    @Field(length = 7)
	private String ACCDATE = StringUtils.EMPTY;

    @Field(length = 10)
	private String TRNSFRIDNO = StringUtils.EMPTY;

    @Field(length = 7)
	private String TRNSFROUTBANK = StringUtils.EMPTY;

    @Field(length = 16)
	private String TRNSFROUTACCNT = StringUtils.EMPTY;

    @Field(length = 10)
	private String TRNSFRINIDNO = StringUtils.EMPTY;

    @Field(length = 7)
	private String TRNSFRINBANK = StringUtils.EMPTY;

    @Field(length = 16)
	private String TRNSFRINACCNT = StringUtils.EMPTY;

    @Field(length = 17)
	private BigDecimal TRANS_AMT;

    @Field(length = 17)
	private BigDecimal BLOCK_AMT;

    @Field(length = 17)
	private BigDecimal UNBLOCK_AMT;

    @Field(length = 2)
	private String FEEPAYMENTTYPE = StringUtils.EMPTY;

    @Field(length = 7)
	private BigDecimal CUSTPAY_FEE;

    @Field(length = 7)
	private BigDecimal FISC_FEE;

    @Field(length = 7)
	private BigDecimal OTHERBANK_FEE;

    @Field(length = 4)
	private String CHAFEE_BRANCH = StringUtils.EMPTY;

    @Field(length = 7)
	private BigDecimal CHAFEE_AMT;

    @Field(length = 17)
	private BigDecimal TRANSFROUTBAL;

    @Field(length = 17)
	private BigDecimal TRANSAMTOUT;

    @Field(length = 17)
	private BigDecimal TRANSAMTIN;

    @Field(length = 4)
	private String CLEANBRANCH_OUT = StringUtils.EMPTY;

    @Field(length = 4)
	private String CLEANBRANCH_IN = StringUtils.EMPTY;

    @Field(length = 18)
	private String RECEIVENAME = StringUtils.EMPTY;

    @Field(length = 18)
	private String PAYERNOTE = StringUtils.EMPTY;

    @Field(length = 42)
	private String TRNSFRINNAME = StringUtils.EMPTY;

    @Field(length = 42)
	private String TRNSFROUTNAME = StringUtils.EMPTY;

    @Field(length = 50)
	private String PAYEREMAIL = StringUtils.EMPTY;

    @Field(length = 1)
	private String PWATXDAY = StringUtils.EMPTY;

    @Field(length = 1)
	private String CHAFEE_TYPE = StringUtils.EMPTY;

    @Field(length = 7)
	private BigDecimal FAXFEE;

    @Field(length = 7)
	private BigDecimal TRANSFEE;

    @Field(length = 1)
	private String TRANSOUTCUST = StringUtils.EMPTY;

    @Field(length = 4)
	private String TAX_BRANCH = StringUtils.EMPTY;

    @Field(length = 20)
	private String DRVS = StringUtils.EMPTY;

	public String getOUTMSGID(){
        return this.OUTMSGID;
	}

	public void setOUTMSGID(String OUTMSGID){
        this.OUTMSGID = OUTMSGID;
	}

	public String getOUTMSGF(){
        return this.OUTMSGF;
	}

	public void setOUTMSGF(String OUTMSGF){
        this.OUTMSGF = OUTMSGF;
	}

	public String getOUTSTAN(){
        return this.OUTSTAN;
	}

	public void setOUTSTAN(String OUTSTAN){
        this.OUTSTAN = OUTSTAN;
	}

	public String getOUTDATE(){
        return this.OUTDATE;
	}

	public void setOUTDATE(String OUTDATE){
        this.OUTDATE = OUTDATE;
	}

	public String getOUTTIME(){
        return this.OUTTIME;
	}

	public void setOUTTIME(String OUTTIME){
        this.OUTTIME = OUTTIME;
	}

	public String getOUTSERV(){
        return this.OUTSERV;
	}

	public void setOUTSERV(String OUTSERV){
        this.OUTSERV = OUTSERV;
	}

	public String getOUTTD(){
        return this.OUTTD;
	}

	public void setOUTTD(String OUTTD){
        this.OUTTD = OUTTD;
	}

	public String getOUTAP(){
        return this.OUTAP;
	}

	public void setOUTAP(String OUTAP){
        this.OUTAP = OUTAP;
	}

	public String getOUTID(){
        return this.OUTID;
	}

	public void setOUTID(String OUTID){
        this.OUTID = OUTID;
	}

	public String getOUTFF(){
        return this.OUTFF;
	}

	public void setOUTFF(String OUTFF){
        this.OUTFF = OUTFF;
	}

	public String getOUTPGNO(){
        return this.OUTPGNO;
	}

	public void setOUTPGNO(String OUTPGNO){
        this.OUTPGNO = OUTPGNO;
	}

	public String getOUTFIX(){
        return this.OUTFIX;
	}

	public void setOUTFIX(String OUTFIX){
        this.OUTFIX = OUTFIX;
	}

	public String getOUTV1CT(){
        return this.OUTV1CT;
	}

	public void setOUTV1CT(String OUTV1CT){
        this.OUTV1CT = OUTV1CT;
	}

	public String getOUTRTC(){
        return this.OUTRTC;
	}

	public void setOUTRTC(String OUTRTC){
        this.OUTRTC = OUTRTC;
	}

	public String getOUTBILL(){
        return this.OUTBILL;
	}

	public void setOUTBILL(String OUTBILL){
        this.OUTBILL = OUTBILL;
	}

	public String getOUTTIMES(){
        return this.OUTTIMES;
	}

	public void setOUTTIMES(String OUTTIMES){
        this.OUTTIMES = OUTTIMES;
	}

	public String getOUTRSV(){
        return this.OUTRSV;
	}

	public void setOUTRSV(String OUTRSV){
        this.OUTRSV = OUTRSV;
	}

	public String getRECTYPE(){
        return this.RECTYPE;
	}

	public void setRECTYPE(String RECTYPE){
        this.RECTYPE = RECTYPE;
	}

	public String getACCDATE(){
        return this.ACCDATE;
	}

	public void setACCDATE(String ACCDATE){
        this.ACCDATE = ACCDATE;
	}

	public String getTRNSFRIDNO(){
        return this.TRNSFRIDNO;
	}

	public void setTRNSFRIDNO(String TRNSFRIDNO){
        this.TRNSFRIDNO = TRNSFRIDNO;
	}

	public String getTRNSFROUTBANK(){
        return this.TRNSFROUTBANK;
	}

	public void setTRNSFROUTBANK(String TRNSFROUTBANK){
        this.TRNSFROUTBANK = TRNSFROUTBANK;
	}

	public String getTRNSFROUTACCNT(){
        return this.TRNSFROUTACCNT;
	}

	public void setTRNSFROUTACCNT(String TRNSFROUTACCNT){
        this.TRNSFROUTACCNT = TRNSFROUTACCNT;
	}

	public String getTRNSFRINIDNO(){
        return this.TRNSFRINIDNO;
	}

	public void setTRNSFRINIDNO(String TRNSFRINIDNO){
        this.TRNSFRINIDNO = TRNSFRINIDNO;
	}

	public String getTRNSFRINBANK(){
        return this.TRNSFRINBANK;
	}

	public void setTRNSFRINBANK(String TRNSFRINBANK){
        this.TRNSFRINBANK = TRNSFRINBANK;
	}

	public String getTRNSFRINACCNT(){
        return this.TRNSFRINACCNT;
	}

	public void setTRNSFRINACCNT(String TRNSFRINACCNT){
        this.TRNSFRINACCNT = TRNSFRINACCNT;
	}

	public BigDecimal getTRANS_AMT(){
        return this.TRANS_AMT;
	}

	public void setTRANS_AMT(BigDecimal TRANS_AMT){
		this.TRANS_AMT = TRANS_AMT;
	}

	public BigDecimal getBLOCK_AMT(){
        return this.BLOCK_AMT;
	}

	public void setBLOCK_AMT(BigDecimal BLOCK_AMT){
		this.BLOCK_AMT = BLOCK_AMT;
	}

	public BigDecimal getUNBLOCK_AMT(){
        return this.UNBLOCK_AMT;
	}

	public void setUNBLOCK_AMT(BigDecimal UNBLOCK_AMT){
		this.UNBLOCK_AMT = UNBLOCK_AMT;
	}

	public String getFEEPAYMENTTYPE(){
        return this.FEEPAYMENTTYPE;
	}

	public void setFEEPAYMENTTYPE(String FEEPAYMENTTYPE){
        this.FEEPAYMENTTYPE = FEEPAYMENTTYPE;
	}

	public BigDecimal getCUSTPAY_FEE(){
        return this.CUSTPAY_FEE;
	}

	public void setCUSTPAY_FEE(BigDecimal CUSTPAY_FEE){
		this.CUSTPAY_FEE = CUSTPAY_FEE;
	}

	public BigDecimal getFISC_FEE(){
        return this.FISC_FEE;
	}

	public void setFISC_FEE(BigDecimal FISC_FEE){
		this.FISC_FEE = FISC_FEE;
	}

	public BigDecimal getOTHERBANK_FEE(){
        return this.OTHERBANK_FEE;
	}

	public void setOTHERBANK_FEE(BigDecimal OTHERBANK_FEE){
		this.OTHERBANK_FEE = OTHERBANK_FEE;
	}

	public String getCHAFEE_BRANCH(){
        return this.CHAFEE_BRANCH;
	}

	public void setCHAFEE_BRANCH(String CHAFEE_BRANCH){
        this.CHAFEE_BRANCH = CHAFEE_BRANCH;
	}

	public BigDecimal getCHAFEE_AMT(){
        return this.CHAFEE_AMT;
	}

	public void setCHAFEE_AMT(BigDecimal CHAFEE_AMT){
		this.CHAFEE_AMT = CHAFEE_AMT;
	}

	public BigDecimal getTRANSFROUTBAL(){
        return this.TRANSFROUTBAL;
	}

	public void setTRANSFROUTBAL(BigDecimal TRANSFROUTBAL){
		this.TRANSFROUTBAL = TRANSFROUTBAL;
	}

	public BigDecimal getTRANSAMTOUT(){
        return this.TRANSAMTOUT;
	}

	public void setTRANSAMTOUT(BigDecimal TRANSAMTOUT){
		this.TRANSAMTOUT = TRANSAMTOUT;
	}

	public BigDecimal getTRANSAMTIN(){
        return this.TRANSAMTIN;
	}

	public void setTRANSAMTIN(BigDecimal TRANSAMTIN){
		this.TRANSAMTIN = TRANSAMTIN;
	}

	public String getCLEANBRANCH_OUT(){
        return this.CLEANBRANCH_OUT;
	}

	public void setCLEANBRANCH_OUT(String CLEANBRANCH_OUT){
        this.CLEANBRANCH_OUT = CLEANBRANCH_OUT;
	}

	public String getCLEANBRANCH_IN(){
        return this.CLEANBRANCH_IN;
	}

	public void setCLEANBRANCH_IN(String CLEANBRANCH_IN){
        this.CLEANBRANCH_IN = CLEANBRANCH_IN;
	}

	public String getRECEIVENAME(){
        return this.RECEIVENAME;
	}

	public void setRECEIVENAME(String RECEIVENAME){
        this.RECEIVENAME = RECEIVENAME;
	}

	public String getPAYERNOTE(){
        return this.PAYERNOTE;
	}

	public void setPAYERNOTE(String PAYERNOTE){
        this.PAYERNOTE = PAYERNOTE;
	}

	public String getTRNSFRINNAME(){
        return this.TRNSFRINNAME;
	}

	public void setTRNSFRINNAME(String TRNSFRINNAME){
        this.TRNSFRINNAME = TRNSFRINNAME;
	}

	public String getTRNSFROUTNAME(){
        return this.TRNSFROUTNAME;
	}

	public void setTRNSFROUTNAME(String TRNSFROUTNAME){
        this.TRNSFROUTNAME = TRNSFROUTNAME;
	}

	public String getPAYEREMAIL(){
        return this.PAYEREMAIL;
	}

	public void setPAYEREMAIL(String PAYEREMAIL){
        this.PAYEREMAIL = PAYEREMAIL;
	}

	public String getPWATXDAY(){
        return this.PWATXDAY;
	}

	public void setPWATXDAY(String PWATXDAY){
        this.PWATXDAY = PWATXDAY;
	}

	public String getCHAFEE_TYPE(){
        return this.CHAFEE_TYPE;
	}

	public void setCHAFEE_TYPE(String CHAFEE_TYPE){
        this.CHAFEE_TYPE = CHAFEE_TYPE;
	}

	public BigDecimal getFAXFEE(){
        return this.FAXFEE;
	}

	public void setFAXFEE(BigDecimal FAXFEE){
		this.FAXFEE = FAXFEE;
	}

	public BigDecimal getTRANSFEE(){
        return this.TRANSFEE;
	}

	public void setTRANSFEE(BigDecimal TRANSFEE){
		this.TRANSFEE = TRANSFEE;
	}

	public String getTRANSOUTCUST(){
        return this.TRANSOUTCUST;
	}

	public void setTRANSOUTCUST(String TRANSOUTCUST){
        this.TRANSOUTCUST = TRANSOUTCUST;
	}

	public String getTAX_BRANCH(){
        return this.TAX_BRANCH;
	}

	public void setTAX_BRANCH(String TAX_BRANCH){
        this.TAX_BRANCH = TAX_BRANCH;
	}

	public String getDRVS(){
        return this.DRVS;
	}

	public void setDRVS(String DRVS){
        this.DRVS = DRVS;
	}


	public void parseCbsTele(String tita) throws ParseException{
        this.setOUTMSGID(EbcdicConverter.fromHex(CCSID.English,tita.substring(0, 48)));
        this.setOUTMSGF(EbcdicConverter.fromHex(CCSID.English,tita.substring(48, 50)));
        this.setOUTSTAN(EbcdicConverter.fromHex(CCSID.English,tita.substring(50, 70)));
        this.setOUTDATE(EbcdicConverter.fromHex(CCSID.English,tita.substring(70, 86)));
        this.setOUTTIME(EbcdicConverter.fromHex(CCSID.English,tita.substring(86, 98)));
        this.setOUTSERV(EbcdicConverter.fromHex(CCSID.English,tita.substring(98, 106)));
        this.setOUTTD(EbcdicConverter.fromHex(CCSID.English,tita.substring(106, 122)));
        this.setOUTAP(EbcdicConverter.fromHex(CCSID.English,tita.substring(122, 132)));
        this.setOUTID(EbcdicConverter.fromHex(CCSID.English,tita.substring(132, 152)));
        this.setOUTFF(EbcdicConverter.fromHex(CCSID.English,tita.substring(152, 154)));
        this.setOUTPGNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(154, 160)));
        this.setOUTFIX(EbcdicConverter.fromHex(CCSID.English,tita.substring(160, 168)));
        this.setOUTV1CT(EbcdicConverter.fromHex(CCSID.English,tita.substring(168, 176)));
        this.setOUTRTC(EbcdicConverter.fromHex(CCSID.English,tita.substring(176, 184)));
        this.setOUTBILL(EbcdicConverter.fromHex(CCSID.English,tita.substring(184, 186)));
        this.setOUTTIMES(EbcdicConverter.fromHex(CCSID.English,tita.substring(186, 190)));
        this.setOUTRSV(EbcdicConverter.fromHex(CCSID.English,tita.substring(190, 234)));
        this.setRECTYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(234, 236)));
        this.setACCDATE(EbcdicConverter.fromHex(CCSID.English,tita.substring(236, 250)));
        this.setTRNSFRIDNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(250, 270)));
        this.setTRNSFROUTBANK(EbcdicConverter.fromHex(CCSID.English,tita.substring(270, 284)));
        this.setTRNSFROUTACCNT(EbcdicConverter.fromHex(CCSID.English,tita.substring(284, 316)));
        this.setTRNSFRINIDNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(316, 336)));
        this.setTRNSFRINBANK(EbcdicConverter.fromHex(CCSID.English,tita.substring(336, 350)));
        this.setTRNSFRINACCNT(EbcdicConverter.fromHex(CCSID.English,tita.substring(350, 382)));
        this.setTRANS_AMT(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(382, 416).trim()), false, 2));
        this.setBLOCK_AMT(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(416, 450).trim()), false, 2));
        this.setUNBLOCK_AMT(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(450, 484).trim()), false, 2));
        this.setFEEPAYMENTTYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(484, 488)));
        this.setCUSTPAY_FEE(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(488, 502).trim()), false, 0));
        this.setFISC_FEE(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(502, 516).trim()), false, 0));
        this.setOTHERBANK_FEE(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(516, 530).trim()), false, 0));
        this.setCHAFEE_BRANCH(EbcdicConverter.fromHex(CCSID.English,tita.substring(530, 538)));
        this.setCHAFEE_AMT(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(538, 552).trim()), false, 0));
        this.setTRANSFROUTBAL(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(552, 586).trim()), false, 2));
        this.setTRANSAMTOUT(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(586, 620).trim()), false, 2));
        this.setTRANSAMTIN(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(620, 654).trim()), false, 2));
        this.setCLEANBRANCH_OUT(EbcdicConverter.fromHex(CCSID.English,tita.substring(654, 662)));
        this.setCLEANBRANCH_IN(EbcdicConverter.fromHex(CCSID.English,tita.substring(662, 670)));
        this.setRECEIVENAME(EbcdicConverter.changeChinese(tita.substring(670, 706)));
        this.setPAYERNOTE(EbcdicConverter.changeChinese(tita.substring(706, 742)));
        this.setTRNSFRINNAME(EbcdicConverter.changeChinese(tita.substring(742, 826)));
        this.setTRNSFROUTNAME(EbcdicConverter.changeChinese(tita.substring(826, 910)));
        this.setPAYEREMAIL(EbcdicConverter.fromHex(CCSID.English,tita.substring(910, 1010)));
        this.setPWATXDAY(EbcdicConverter.fromHex(CCSID.English,tita.substring(1010, 1012)));
        this.setCHAFEE_TYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(1012, 1014)));
        this.setFAXFEE(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(1014, 1028).trim()), false, 0));
        this.setTRANSFEE(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(1028, 1042).trim()), false, 0));
        this.setTRANSOUTCUST(EbcdicConverter.fromHex(CCSID.English,tita.substring(1042, 1044)));
        this.setTAX_BRANCH(EbcdicConverter.fromHex(CCSID.English,tita.substring(1044, 1052)));
        this.setDRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(1052, 1092)));
	}

	public String makeMessage() {
		return "" //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTMSGID(), 24) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTMSGF(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTSTAN(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTDATE(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTTIME(), 6) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTSERV(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTTD(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTAP(), 5) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTID(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTFF(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTPGNO(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTFIX(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTV1CT(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTRTC(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTBILL(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTTIMES(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTRSV(), 22) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getRECTYPE(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getACCDATE(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFRIDNO(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFROUTBANK(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFROUTACCNT(), 16) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFRINIDNO(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFRINBANK(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFRINACCNT(), 16) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getTRANS_AMT(), 14, false, 2, true) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getBLOCK_AMT(), 14, false, 2, true) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getUNBLOCK_AMT(), 14, false, 2, true) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFEEPAYMENTTYPE(), 2) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getCUSTPAY_FEE(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getFISC_FEE(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getOTHERBANK_FEE(), 7, false, 0, false) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCHAFEE_BRANCH(), 4) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getCHAFEE_AMT(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getTRANSFROUTBAL(), 14, false, 2, true) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getTRANSAMTOUT(), 14, false, 2, true) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getTRANSAMTIN(), 14, false, 2, true) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCLEANBRANCH_OUT(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCLEANBRANCH_IN(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getRECEIVENAME(), 18) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPAYERNOTE(), 18) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFRINNAME(), 42) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFROUTNAME(), 42) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPAYEREMAIL(), 50) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPWATXDAY(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCHAFEE_TYPE(), 1) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getFAXFEE(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getTRANSFEE(), 7, false, 0, false) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRANSOUTCUST(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTAX_BRANCH(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 20) //
		;
	}

	public String makeMessageAscii() {
		return "" //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTMSGID(), StringUtils.EMPTY), 24," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTMSGF(), StringUtils.EMPTY), 1," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTSTAN(), StringUtils.EMPTY), 10," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTDATE(), StringUtils.EMPTY), 8," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTTIME(), StringUtils.EMPTY), 6," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTSERV(), StringUtils.EMPTY), 4," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTTD(), StringUtils.EMPTY), 8," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTAP(), StringUtils.EMPTY), 5," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTID(), StringUtils.EMPTY), 10," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTFF(), StringUtils.EMPTY), 1," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTPGNO(), StringUtils.EMPTY), 3," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTFIX(), StringUtils.EMPTY), 4," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTV1CT(), StringUtils.EMPTY), 4," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTRTC(), StringUtils.EMPTY), 4," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTBILL(), StringUtils.EMPTY), 1," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTTIMES(), StringUtils.EMPTY), 2," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getOUTRSV(), StringUtils.EMPTY), 22," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getRECTYPE(), StringUtils.EMPTY), 1," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getACCDATE(), StringUtils.EMPTY), 7," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTRNSFRIDNO(), StringUtils.EMPTY), 10," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTRNSFROUTBANK(), StringUtils.EMPTY), 7," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTRNSFROUTACCNT(), StringUtils.EMPTY), 16," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTRNSFRINIDNO(), StringUtils.EMPTY), 10," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTRNSFRINBANK(), StringUtils.EMPTY), 7," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTRNSFRINACCNT(), StringUtils.EMPTY), 16," ") //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getTRANS_AMT(), 14, false, 2, true) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getBLOCK_AMT(), 14, false, 2, true) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getUNBLOCK_AMT(), 14, false, 2, true) //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getFEEPAYMENTTYPE(), StringUtils.EMPTY), 2," ") //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getCUSTPAY_FEE(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getFISC_FEE(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getOTHERBANK_FEE(), 7, false, 0, false) //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getCHAFEE_BRANCH(), StringUtils.EMPTY), 4," ") //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getCHAFEE_AMT(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getTRANSFROUTBAL(), 14, false, 2, true) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getTRANSAMTOUT(), 14, false, 2, true) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getTRANSAMTIN(), 14, false, 2, true) //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getCLEANBRANCH_OUT(), StringUtils.EMPTY), 4," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getCLEANBRANCH_IN(), StringUtils.EMPTY), 4," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getRECEIVENAME(), StringUtils.EMPTY), 18," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPAYERNOTE(), StringUtils.EMPTY), 18," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTRNSFRINNAME(), StringUtils.EMPTY), 42," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTRNSFROUTNAME(), StringUtils.EMPTY), 42," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPAYEREMAIL(), StringUtils.EMPTY), 50," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPWATXDAY(), StringUtils.EMPTY), 1," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getCHAFEE_TYPE(), StringUtils.EMPTY), 1," ") //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getFAXFEE(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getTRANSFEE(), 7, false, 0, false) //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTRANSOUTCUST(), StringUtils.EMPTY), 1," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTAX_BRANCH(), StringUtils.EMPTY), 4," ") //
            + StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getDRVS(), StringUtils.EMPTY), 20," ") //
		;
	}
}
