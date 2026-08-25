package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class AB_ACC_O02 extends IMSTextBase {
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

    @Field(length = 80)
    private String MEMO = StringUtils.EMPTY;

    public String getOUTMSGID() {
        return this.OUTMSGID;
    }

    public void setOUTMSGID(String OUTMSGID) {
        this.OUTMSGID = OUTMSGID;
    }

    public String getOUTMSGF() {
        return this.OUTMSGF;
    }

    public void setOUTMSGF(String OUTMSGF) {
        this.OUTMSGF = OUTMSGF;
    }

    public String getOUTSTAN() {
        return this.OUTSTAN;
    }

    public void setOUTSTAN(String OUTSTAN) {
        this.OUTSTAN = OUTSTAN;
    }

    public String getOUTDATE() {
        return this.OUTDATE;
    }

    public void setOUTDATE(String OUTDATE) {
        this.OUTDATE = OUTDATE;
    }

    public String getOUTTIME() {
        return this.OUTTIME;
    }

    public void setOUTTIME(String OUTTIME) {
        this.OUTTIME = OUTTIME;
    }

    public String getOUTSERV() {
        return this.OUTSERV;
    }

    public void setOUTSERV(String OUTSERV) {
        this.OUTSERV = OUTSERV;
    }

    public String getOUTTD() {
        return this.OUTTD;
    }

    public void setOUTTD(String OUTTD) {
        this.OUTTD = OUTTD;
    }

    public String getOUTAP() {
        return this.OUTAP;
    }

    public void setOUTAP(String OUTAP) {
        this.OUTAP = OUTAP;
    }

    public String getOUTID() {
        return this.OUTID;
    }

    public void setOUTID(String OUTID) {
        this.OUTID = OUTID;
    }

    public String getOUTFF() {
        return this.OUTFF;
    }

    public void setOUTFF(String OUTFF) {
        this.OUTFF = OUTFF;
    }

    public String getOUTPGNO() {
        return this.OUTPGNO;
    }

    public void setOUTPGNO(String OUTPGNO) {
        this.OUTPGNO = OUTPGNO;
    }

    public String getOUTFIX() {
        return this.OUTFIX;
    }

    public void setOUTFIX(String OUTFIX) {
        this.OUTFIX = OUTFIX;
    }

    public String getOUTV1CT() {
        return this.OUTV1CT;
    }

    public void setOUTV1CT(String OUTV1CT) {
        this.OUTV1CT = OUTV1CT;
    }

    public String getOUTRTC() {
        return this.OUTRTC;
    }

    public void setOUTRTC(String OUTRTC) {
        this.OUTRTC = OUTRTC;
    }

    public String getOUTBILL() {
        return this.OUTBILL;
    }

    public void setOUTBILL(String OUTBILL) {
        this.OUTBILL = OUTBILL;
    }

    public String getOUTTIMES() {
        return this.OUTTIMES;
    }

    public void setOUTTIMES(String OUTTIMES) {
        this.OUTTIMES = OUTTIMES;
    }

    public String getOUTRSV() {
        return this.OUTRSV;
    }

    public void setOUTRSV(String OUTRSV) {
        this.OUTRSV = OUTRSV;
    }

    public String getMEMO() {
        return this.MEMO;
    }

    public void setMEMO(String MEMO) {
        this.MEMO = MEMO;
    }


    public void parseCbsTele(String tita) throws ParseException {
        this.setOUTMSGID(EbcdicConverter.fromHex(CCSID.English, tita.substring(0, 48)));
        this.setOUTMSGF(EbcdicConverter.fromHex(CCSID.English, tita.substring(48, 50)));
        this.setOUTSTAN(EbcdicConverter.fromHex(CCSID.English, tita.substring(50, 70)));
        this.setOUTDATE(EbcdicConverter.fromHex(CCSID.English, tita.substring(70, 86)));
        this.setOUTTIME(EbcdicConverter.fromHex(CCSID.English, tita.substring(86, 98)));
        this.setOUTSERV(EbcdicConverter.fromHex(CCSID.English, tita.substring(98, 106)));
        this.setOUTTD(EbcdicConverter.fromHex(CCSID.English, tita.substring(106, 122)));
        this.setOUTAP(EbcdicConverter.fromHex(CCSID.English, tita.substring(122, 132)));
        this.setOUTID(EbcdicConverter.fromHex(CCSID.English, tita.substring(132, 152)));
        this.setOUTFF(EbcdicConverter.fromHex(CCSID.English, tita.substring(152, 154)));
        this.setOUTPGNO(EbcdicConverter.fromHex(CCSID.English, tita.substring(154, 160)));
        this.setOUTFIX(EbcdicConverter.fromHex(CCSID.English, tita.substring(160, 168)));
        this.setOUTV1CT(EbcdicConverter.fromHex(CCSID.English, tita.substring(168, 176)));
        this.setOUTRTC(EbcdicConverter.fromHex(CCSID.English, tita.substring(176, 184)));
        this.setOUTBILL(EbcdicConverter.fromHex(CCSID.English, tita.substring(184, 186)));
        this.setOUTTIMES(EbcdicConverter.fromHex(CCSID.English, tita.substring(186, 190)));
        this.setOUTRSV(EbcdicConverter.fromHex(CCSID.English, tita.substring(190, 234)));
        this.setMEMO(EbcdicConverter.changeChinese(tita.substring(234, 394)));
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
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMEMO(), 80) //
                ;
    }

    public String makeMessageAscii() {
        return "" //
                + StringUtils.leftPad(this.getOUTMSGID(), 24, " ") //
                + StringUtils.leftPad(this.getOUTMSGF(), 1, " ") //
                + StringUtils.leftPad(this.getOUTSTAN(), 10, " ") //
                + StringUtils.leftPad(this.getOUTDATE(), 8, " ") //
                + StringUtils.leftPad(this.getOUTTIME(), 6, " ") //
                + StringUtils.leftPad(this.getOUTSERV(), 4, " ") //
                + StringUtils.leftPad(this.getOUTTD(), 8, " ") //
                + StringUtils.leftPad(this.getOUTAP(), 5, " ") //
                + StringUtils.leftPad(this.getOUTID(), 10, " ") //
                + StringUtils.leftPad(this.getOUTFF(), 1, " ") //
                + StringUtils.leftPad(this.getOUTPGNO(), 3, " ") //
                + StringUtils.leftPad(this.getOUTFIX(), 4, " ") //
                + StringUtils.leftPad(this.getOUTV1CT(), 4, " ") //
                + StringUtils.leftPad(this.getOUTRTC(), 4, " ") //
                + StringUtils.leftPad(this.getOUTBILL(), 1, " ") //
                + StringUtils.leftPad(this.getOUTTIMES(), 2, " ") //
                + StringUtils.leftPad(this.getOUTRSV(), 22, " ") //
                + StringUtils.leftPad(this.getMEMO(), 80, " ") //
                ;
    }
}
