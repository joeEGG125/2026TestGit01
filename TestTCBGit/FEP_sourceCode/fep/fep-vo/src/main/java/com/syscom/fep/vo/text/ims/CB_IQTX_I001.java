package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class CB_IQTX_I001 extends IMSTextBase {
    @Field(length = 8)
	private String INTRAN = StringUtils.EMPTY;

    @Field(length = 1)
	private String INRSV1 = StringUtils.EMPTY;

    @Field(length = 24)
	private String INMSGID = StringUtils.EMPTY;

    @Field(length = 10)
	private String INSTAN = StringUtils.EMPTY;

    @Field(length = 8)
	private String INDATE = StringUtils.EMPTY;

    @Field(length = 6)
	private String INTIME = StringUtils.EMPTY;

    @Field(length = 4)
	private String INSERV = StringUtils.EMPTY;

    @Field(length = 1)
	private String MQIDCNV = StringUtils.EMPTY;

    @Field(length = 8)
	private String INTD = StringUtils.EMPTY;

    @Field(length = 5)
	private String INAP = StringUtils.EMPTY;

    @Field(length = 10)
	private String INID = StringUtils.EMPTY;

    @Field(length = 1)
	private String INFF = StringUtils.EMPTY;

    @Field(length = 3)
	private String INPGNO = StringUtils.EMPTY;

    @Field(length = 4)
	private String INV1CT = StringUtils.EMPTY;

    @Field(length = 4)
	private String INRTC = StringUtils.EMPTY;

    @Field(length = 21)
	private String INRSV2 = StringUtils.EMPTY;

    @Field(length = 3)
	private String INQ_ACQ_BID = StringUtils.EMPTY;

    @Field(length = 7)
	private String INQ_TX_STAN = StringUtils.EMPTY;

    @Field(length = 7)
	private String INQ_FG_TXDATE = StringUtils.EMPTY;

    @Field(length = 83)
	private String DRVS = StringUtils.EMPTY;

	public String getINTRAN(){
        return this.INTRAN;
	}

	public void setINTRAN(String INTRAN){
        this.INTRAN = INTRAN;
	}

	public String getINRSV1(){
        return this.INRSV1;
	}

	public void setINRSV1(String INRSV1){
        this.INRSV1 = INRSV1;
	}

	public String getINMSGID(){
        return this.INMSGID;
	}

	public void setINMSGID(String INMSGID){
        this.INMSGID = INMSGID;
	}

	public String getINSTAN(){
        return this.INSTAN;
	}

	public void setINSTAN(String INSTAN){
        this.INSTAN = INSTAN;
	}

	public String getINDATE(){
        return this.INDATE;
	}

	public void setINDATE(String INDATE){
        this.INDATE = INDATE;
	}

	public String getINTIME(){
        return this.INTIME;
	}

	public void setINTIME(String INTIME){
        this.INTIME = INTIME;
	}

	public String getINSERV(){
        return this.INSERV;
	}

	public void setINSERV(String INSERV){
        this.INSERV = INSERV;
	}

	public String getMQIDCNV(){
        return this.MQIDCNV;
	}

	public void setMQIDCNV(String MQIDCNV){
        this.MQIDCNV = MQIDCNV;
	}

	public String getINTD(){
        return this.INTD;
	}

	public void setINTD(String INTD){
        this.INTD = INTD;
	}

	public String getINAP(){
        return this.INAP;
	}

	public void setINAP(String INAP){
        this.INAP = INAP;
	}

	public String getINID(){
        return this.INID;
	}

	public void setINID(String INID){
        this.INID = INID;
	}

	public String getINFF(){
        return this.INFF;
	}

	public void setINFF(String INFF){
        this.INFF = INFF;
	}

	public String getINPGNO(){
        return this.INPGNO;
	}

	public void setINPGNO(String INPGNO){
        this.INPGNO = INPGNO;
	}

	public String getINV1CT(){
        return this.INV1CT;
	}

	public void setINV1CT(String INV1CT){
        this.INV1CT = INV1CT;
	}

	public String getINRTC(){
        return this.INRTC;
	}

	public void setINRTC(String INRTC){
        this.INRTC = INRTC;
	}

	public String getINRSV2(){
        return this.INRSV2;
	}

	public void setINRSV2(String INRSV2){
        this.INRSV2 = INRSV2;
	}

	public String getINQ_ACQ_BID(){
        return this.INQ_ACQ_BID;
	}

	public void setINQ_ACQ_BID(String INQ_ACQ_BID){
        this.INQ_ACQ_BID = INQ_ACQ_BID;
	}

	public String getINQ_TX_STAN(){
        return this.INQ_TX_STAN;
	}

	public void setINQ_TX_STAN(String INQ_TX_STAN){
        this.INQ_TX_STAN = INQ_TX_STAN;
	}

	public String getINQ_FG_TXDATE(){
        return this.INQ_FG_TXDATE;
	}

	public void setINQ_FG_TXDATE(String INQ_FG_TXDATE){
        this.INQ_FG_TXDATE = INQ_FG_TXDATE;
	}

	public String getDRVS(){
        return this.DRVS;
	}

	public void setDRVS(String DRVS){
        this.DRVS = DRVS;
	}


	public void parseCbsTele(String tita) throws ParseException{
        this.setINTRAN(EbcdicConverter.fromHex(CCSID.English,tita.substring(0, 16)));
        this.setINRSV1(EbcdicConverter.fromHex(CCSID.English,tita.substring(16, 18)));
        this.setINMSGID(EbcdicConverter.fromHex(CCSID.English,tita.substring(18, 66)));
        this.setINSTAN(EbcdicConverter.fromHex(CCSID.English,tita.substring(66, 86)));
        this.setINDATE(EbcdicConverter.fromHex(CCSID.English,tita.substring(86, 102)));
        this.setINTIME(EbcdicConverter.fromHex(CCSID.English,tita.substring(102, 114)));
        this.setINSERV(EbcdicConverter.fromHex(CCSID.English,tita.substring(114, 122)));
        this.setMQIDCNV(EbcdicConverter.fromHex(CCSID.English,tita.substring(122, 124)));
        this.setINTD(EbcdicConverter.fromHex(CCSID.English,tita.substring(124, 140)));
        this.setINAP(EbcdicConverter.fromHex(CCSID.English,tita.substring(140, 150)));
        this.setINID(EbcdicConverter.fromHex(CCSID.English,tita.substring(150, 170)));
        this.setINFF(EbcdicConverter.fromHex(CCSID.English,tita.substring(170, 172)));
        this.setINPGNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(172, 178)));
        this.setINV1CT(EbcdicConverter.fromHex(CCSID.English,tita.substring(178, 186)));
        this.setINRTC(EbcdicConverter.fromHex(CCSID.English,tita.substring(186, 194)));
        this.setINRSV2(EbcdicConverter.fromHex(CCSID.English,tita.substring(194, 236)));
        this.setINQ_ACQ_BID(EbcdicConverter.fromHex(CCSID.English,tita.substring(236, 242)));
        this.setINQ_TX_STAN(EbcdicConverter.fromHex(CCSID.English,tita.substring(242, 256)));
        this.setINQ_FG_TXDATE(EbcdicConverter.fromHex(CCSID.English,tita.substring(256, 270)));
        this.setDRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(270, 436)));
	}

	public String makeMessage() {
		return "" //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINTRAN(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINRSV1(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINMSGID(), 24) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINSTAN(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINDATE(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINTIME(), 6) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINSERV(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMQIDCNV(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINTD(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINAP(), 5) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINID(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINFF(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINPGNO(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINV1CT(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINRTC(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINRSV2(), 21) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINQ_ACQ_BID(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINQ_TX_STAN(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINQ_FG_TXDATE(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 83) //
		;
	}

	public String makeMessageAscii() {
		return "" //
            + StringUtils.rightPad(this.getINTRAN(), 8," ") //
            + StringUtils.rightPad(this.getINRSV1(), 1," ") //
            + StringUtils.rightPad(this.getINMSGID(), 24," ") //
            + StringUtils.rightPad(this.getINSTAN(), 10," ") //
            + StringUtils.rightPad(this.getINDATE(), 8," ") //
            + StringUtils.rightPad(this.getINTIME(), 6," ") //
            + StringUtils.rightPad(this.getINSERV(), 4," ") //
            + StringUtils.rightPad(this.getMQIDCNV(), 1," ") //
            + StringUtils.rightPad(this.getINTD(), 8," ") //
            + StringUtils.rightPad(this.getINAP(), 5," ") //
            + StringUtils.rightPad(this.getINID(), 10," ") //
            + StringUtils.rightPad(this.getINFF(), 1," ") //
            + StringUtils.rightPad(this.getINPGNO(), 3," ") //
            + StringUtils.rightPad(this.getINV1CT(), 4," ") //
            + StringUtils.rightPad(this.getINRTC(), 4," ") //
            + StringUtils.rightPad(this.getINRSV2(), 21," ") //
            + StringUtils.rightPad(this.getINQ_ACQ_BID(), 3," ") //
            + StringUtils.rightPad(this.getINQ_TX_STAN(), 7," ") //
            + StringUtils.rightPad(this.getINQ_FG_TXDATE(), 7," ") //
            + StringUtils.rightPad(this.getDRVS(), 83," ") //
		;
	}
}
