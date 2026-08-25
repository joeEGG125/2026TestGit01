package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class CB_APIDSTAT_I001 extends IMSTextBase {
    @Field(length = 8)
	private String IMS_TRANS = StringUtils.EMPTY;

    @Field(length = 2)
	private String APIDTYPE = StringUtils.EMPTY;

    @Field(length = 4)
	private String SYSCODE = StringUtils.EMPTY;

    @Field(length = 1)
	private String MSGTYPE = StringUtils.EMPTY;

    @Field(length = 4)
	private String PCODE = StringUtils.EMPTY;

    @Field(length = 10)
	private String TXNSTAN = StringUtils.EMPTY;

    @Field(length = 4)
	private String APID = StringUtils.EMPTY;

    @Field(length = 14)
	private String SYS_DATETIME = StringUtils.EMPTY;

    @Field(length = 8)
	private String FEP_EJNO = StringUtils.EMPTY;

    @Field(length = 45)
	private String DRVS = StringUtils.EMPTY;

	public String getIMS_TRANS(){
        return this.IMS_TRANS;
	}
	
	public void setIMS_TRANS(String IMS_TRANS){
        this.IMS_TRANS = IMS_TRANS;
	}
	
	public String getAPIDTYPE(){
        return this.APIDTYPE;
	}
	
	public void setAPIDTYPE(String APIDTYPE){
        this.APIDTYPE = APIDTYPE;
	}
	
	public String getSYSCODE(){
        return this.SYSCODE;
	}
	
	public void setSYSCODE(String SYSCODE){
        this.SYSCODE = SYSCODE;
	}
	
	public String getMSGTYPE(){
        return this.MSGTYPE;
	}
	
	public void setMSGTYPE(String MSGTYPE){
        this.MSGTYPE = MSGTYPE;
	}
	
	public String getPCODE(){
        return this.PCODE;
	}
	
	public void setPCODE(String PCODE){
        this.PCODE = PCODE;
	}
	
	public String getTXNSTAN(){
        return this.TXNSTAN;
	}
	
	public void setTXNSTAN(String TXNSTAN){
        this.TXNSTAN = TXNSTAN;
	}
	
	public String getAPID(){
        return this.APID;
	}
	
	public void setAPID(String APID){
        this.APID = APID;
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

	public String getDRVS(){
        return this.DRVS;
	}
	
	public void setDRVS(String DRVS){
        this.DRVS = DRVS;
	}
	
	public void parseCbsTele(String tita) throws ParseException{
        this.setIMS_TRANS(EbcdicConverter.fromHex(CCSID.English,tita.substring(0, 16)));
        this.setAPIDTYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(16, 20)));
        this.setSYSCODE(EbcdicConverter.fromHex(CCSID.English,tita.substring(20, 28)));
        this.setMSGTYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(28, 30)));
        this.setPCODE(EbcdicConverter.fromHex(CCSID.English,tita.substring(30, 38)));
        this.setTXNSTAN(EbcdicConverter.fromHex(CCSID.English,tita.substring(38, 58)));
        this.setAPID(EbcdicConverter.fromHex(CCSID.English,tita.substring(58, 66)));
        this.setSYS_DATETIME(EbcdicConverter.fromHex(CCSID.English,tita.substring(66, 94)));
        this.setFEP_EJNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(94, 110)));
        this.setDRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(110, 200)));
	}

	public String makeMessage() {
		return "" 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_TRANS(), 8) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAPIDTYPE(), 2) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSYSCODE(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMSGTYPE(), 1) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPCODE(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTXNSTAN(), 10) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAPID(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSYS_DATETIME(), 14) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFEP_EJNO(), 8) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 45)
		;
	}

	public String makeMessageAscii() {
		return "" 
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getIMS_TRANS(), StringUtils.EMPTY), 8," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAPIDTYPE(), StringUtils.EMPTY), 2," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getSYSCODE(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getMSGTYPE(), StringUtils.EMPTY), 1," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPCODE(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTXNSTAN(), StringUtils.EMPTY), 10," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAPID(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getSYS_DATETIME(), StringUtils.EMPTY), 14," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getFEP_EJNO(), StringUtils.EMPTY), 8," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getDRVS(), StringUtils.EMPTY), 45," ")
		;
	}
}
