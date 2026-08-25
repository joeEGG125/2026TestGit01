package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class CB_APIDSTAT_O001 extends IMSTextBase {
    @Field(length = 8)
	private String IMS_TRANS = StringUtils.EMPTY;

    @Field(length = 4)
	private String SYSCODE = StringUtils.EMPTY;

    @Field(length = 14)
	private String SYS_DATETIME = StringUtils.EMPTY;

    @Field(length = 8)
	private String FEP_EJNO = StringUtils.EMPTY;

    @Field(length = 4)
	private String PCODE = StringUtils.EMPTY;

    @Field(length = 10)
	private String TXNSTAN = StringUtils.EMPTY;

    @Field(length = 4)
	private String APID = StringUtils.EMPTY;

    @Field(length = 4)
	private String IMSRC4_FISC = StringUtils.EMPTY;

    @Field(length = 3)
	private String IMSRC_TCB = StringUtils.EMPTY;

    @Field(length = 1)
	private String IMSAPID_STATUS = StringUtils.EMPTY;

    @Field(length = 14)
	private String IMS_TXN_TIME = StringUtils.EMPTY;

    @Field(length = 46)
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
	
	public String getIMSRC4_FISC(){
        return this.IMSRC4_FISC;
	}
	
	public void setIMSRC4_FISC(String IMSRC4_FISC){
        this.IMSRC4_FISC = IMSRC4_FISC;
	}
	
	public String getIMSRC_TCB(){
        return this.IMSRC_TCB;
	}
	
	public void setIMSRC_TCB(String IMSRC_TCB){
        this.IMSRC_TCB = IMSRC_TCB;
	}
	
	public String getIMSAPID_STATUS(){
        return this.IMSAPID_STATUS;
	}
	
	public void setIMSAPID_STATUS(String IMSAPID_STATUS){
        this.IMSAPID_STATUS = IMSAPID_STATUS;
	}
	
	public String getIMS_TXN_TIME(){
        return this.IMS_TXN_TIME;
	}
	
	public void setIMS_TXN_TIME(String IMS_TXN_TIME){
        this.IMS_TXN_TIME = IMS_TXN_TIME;
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
        this.setPCODE(EbcdicConverter.fromHex(CCSID.English,tita.substring(68, 76)));
        this.setTXNSTAN(EbcdicConverter.fromHex(CCSID.English,tita.substring(76, 96)));
        this.setAPID(EbcdicConverter.fromHex(CCSID.English,tita.substring(96, 104)));
        this.setIMSRC4_FISC(EbcdicConverter.fromHex(CCSID.English,tita.substring(104, 112)));
        this.setIMSRC_TCB(EbcdicConverter.fromHex(CCSID.English,tita.substring(112, 118)));
        this.setIMSAPID_STATUS(EbcdicConverter.fromHex(CCSID.English,tita.substring(118, 120)));
        this.setIMS_TXN_TIME(EbcdicConverter.fromHex(CCSID.English,tita.substring(120, 148)));
        this.setDRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(148, 240)));
	}

	public String makeMessage() {
		return "" 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_TRANS(), 8) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSYSCODE(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSYS_DATETIME(), 14) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFEP_EJNO(), 8) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPCODE(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTXNSTAN(), 10) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAPID(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMSRC4_FISC(), 4) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMSRC_TCB(), 3) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMSAPID_STATUS(), 1) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_TXN_TIME(), 14) 
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 46)
		;
	}

	public String makeMessageAscii() {
		return "" 
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getIMS_TRANS(), StringUtils.EMPTY), 8," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getSYSCODE(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getSYS_DATETIME(), StringUtils.EMPTY), 14," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getFEP_EJNO(), StringUtils.EMPTY), 8," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getPCODE(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getTXNSTAN(), StringUtils.EMPTY), 10," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getAPID(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getIMSRC4_FISC(), StringUtils.EMPTY), 4," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getIMSRC_TCB(), StringUtils.EMPTY), 3," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getIMSAPID_STATUS(), StringUtils.EMPTY), 1," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getIMS_TXN_TIME(), StringUtils.EMPTY), 14," ")
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.getDRVS(), StringUtils.EMPTY), 46," ")
		;
	}
}
