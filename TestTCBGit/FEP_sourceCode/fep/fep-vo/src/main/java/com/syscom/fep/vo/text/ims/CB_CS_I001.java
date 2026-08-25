package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class CB_CS_I001 extends IMSTextBase {
    @Field(length = 8)
        private String IMS_TRANS = StringUtils.EMPTY;

    @Field(length = 4)
        private String SYSCODE = StringUtils.EMPTY;

    @Field(length = 14)
        private String SYS_DATETIME = StringUtils.EMPTY;

    @Field(length = 8)
        private String FEP_EJNO = StringUtils.EMPTY;

    @Field(length = 1)
        private String TXN_FLOW = StringUtils.EMPTY;

    @Field(length = 2)
        private String MSG_CAT = StringUtils.EMPTY;

    @Field(length = 3)
        private String SOURCE_CHANNEL = StringUtils.EMPTY;

    @Field(length = 4)
        private String PCODE = StringUtils.EMPTY;

    @Field(length = 2)
        private String FSCODE = StringUtils.EMPTY;

    @Field(length = 4)
        private String PROCESS_TYPE = StringUtils.EMPTY;

    @Field(length = 7)
        private String BUSINESS_DATE = StringUtils.EMPTY;

    @Field(length = 3)
        private String ACQUIRER_BANK = StringUtils.EMPTY;

    @Field(length = 7)
        private String TXNSTAN = StringUtils.EMPTY;

    @Field(length = 8)
        private String TERMINALID = StringUtils.EMPTY;

    @Field(length = 4)
        private String TERMINAL_TYPE = StringUtils.EMPTY;

    @Field(length = 3)
        private String CARDISSUE_BANK = StringUtils.EMPTY;

    @Field(length = 1)
        private String CARDTYPE = StringUtils.EMPTY;

    @Field(length = 4)
        private String RESPONSE_CODE = StringUtils.EMPTY;

    @Field(length = 4)
        private String ATMTRANSEQ = StringUtils.EMPTY;

    @Field(length = 25)
        private String HRVS = StringUtils.EMPTY;

    @Field(length = 4)
        private String CBSMAC = StringUtils.EMPTY;

    @Field(length = 2)
        private String PICCMDCD = StringUtils.EMPTY;

    @Field(length = 2)
        private String PICCMDST = StringUtils.EMPTY;

    @Field(length = 1)
        private String PICSTATE = StringUtils.EMPTY;

    @Field(length = 24)
        private String PICAVAIL = StringUtils.EMPTY;

    @Field(length = 447)
        private String PICVARDT = StringUtils.EMPTY;

    @Field(length = 4)
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
        public String getTXN_FLOW(){
        return this.TXN_FLOW;
        }
        public void setTXN_FLOW(String TXN_FLOW){
        this.TXN_FLOW = TXN_FLOW;
        }
        public String getMSG_CAT(){
        return this.MSG_CAT;
        }
        public void setMSG_CAT(String MSG_CAT){
        this.MSG_CAT = MSG_CAT;
        }
        public String getSOURCE_CHANNEL(){
        return this.SOURCE_CHANNEL;
        }
        public void setSOURCE_CHANNEL(String SOURCE_CHANNEL){
        this.SOURCE_CHANNEL = SOURCE_CHANNEL;
        }
        public String getPCODE(){
        return this.PCODE;
        }
        public void setPCODE(String PCODE){
        this.PCODE = PCODE;
        }
        public String getFSCODE(){
        return this.FSCODE;
        }
        public void setFSCODE(String FSCODE){
        this.FSCODE = FSCODE;
        }
        public String getPROCESS_TYPE(){
        return this.PROCESS_TYPE;
        }
        public void setPROCESS_TYPE(String PROCESS_TYPE){
        this.PROCESS_TYPE = PROCESS_TYPE;
        }
        public String getBUSINESS_DATE(){
        return this.BUSINESS_DATE;
        }
        public void setBUSINESS_DATE(String BUSINESS_DATE){
        this.BUSINESS_DATE = BUSINESS_DATE;
        }
        public String getACQUIRER_BANK(){
        return this.ACQUIRER_BANK;
        }
        public void setACQUIRER_BANK(String ACQUIRER_BANK){
        this.ACQUIRER_BANK = ACQUIRER_BANK;
        }
        public String getTXNSTAN(){
        return this.TXNSTAN;
        }
        public void setTXNSTAN(String TXNSTAN){
        this.TXNSTAN = TXNSTAN;
        }
        public String getTERMINALID(){
        return this.TERMINALID;
        }
        public void setTERMINALID(String TERMINALID){
        this.TERMINALID = TERMINALID;
        }
        public String getTERMINAL_TYPE(){
        return this.TERMINAL_TYPE;
        }
        public void setTERMINAL_TYPE(String TERMINAL_TYPE){
        this.TERMINAL_TYPE = TERMINAL_TYPE;
        }
        public String getCARDISSUE_BANK(){
        return this.CARDISSUE_BANK;
        }
        public void setCARDISSUE_BANK(String CARDISSUE_BANK){
        this.CARDISSUE_BANK = CARDISSUE_BANK;
        }
        public String getCARDTYPE(){
        return this.CARDTYPE;
        }
        public void setCARDTYPE(String CARDTYPE){
        this.CARDTYPE = CARDTYPE;
        }
        public String getRESPONSE_CODE(){
        return this.RESPONSE_CODE;
        }
        public void setRESPONSE_CODE(String RESPONSE_CODE){
        this.RESPONSE_CODE = RESPONSE_CODE;
        }
        public String getATMTRANSEQ(){
        return this.ATMTRANSEQ;
        }
        public void setATMTRANSEQ(String ATMTRANSEQ){
        this.ATMTRANSEQ = ATMTRANSEQ;
        }
        public String getHRVS(){
        return this.HRVS;
        }
        public void setHRVS(String HRVS){
        this.HRVS = HRVS;
        }
        public String getCBSMAC(){
        return this.CBSMAC;
        }
        public void setCBSMAC(String CBSMAC){
        this.CBSMAC = CBSMAC;
        }
        public String getPICCMDCD(){
        return this.PICCMDCD;
        }
        public void setPICCMDCD(String PICCMDCD){
        this.PICCMDCD = PICCMDCD;
        }
        public String getPICCMDST(){
        return this.PICCMDST;
        }
        public void setPICCMDST(String PICCMDST){
        this.PICCMDST = PICCMDST;
        }
        public String getPICSTATE(){
        return this.PICSTATE;
        }
        public void setPICSTATE(String PICSTATE){
        this.PICSTATE = PICSTATE;
        }
        public String getPICAVAIL(){
        return this.PICAVAIL;
        }
        public void setPICAVAIL(String PICAVAIL){
        this.PICAVAIL = PICAVAIL;
        }
        public String getPICVARDT(){
        return this.PICVARDT;
        }
        public void setPICVARDT(String PICVARDT){
        this.PICVARDT = PICVARDT;
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
        this.setTXN_FLOW(EbcdicConverter.fromHex(CCSID.English,tita.substring(68, 70)));
        this.setMSG_CAT(EbcdicConverter.fromHex(CCSID.English,tita.substring(70, 74)));
        this.setSOURCE_CHANNEL(EbcdicConverter.fromHex(CCSID.English,tita.substring(74, 80)));
        this.setPCODE(EbcdicConverter.fromHex(CCSID.English,tita.substring(80, 88)));
        this.setFSCODE(EbcdicConverter.fromHex(CCSID.English,tita.substring(88, 92)));
        this.setPROCESS_TYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(92, 100)));
        this.setBUSINESS_DATE(EbcdicConverter.fromHex(CCSID.English,tita.substring(100, 114)));
        this.setACQUIRER_BANK(EbcdicConverter.fromHex(CCSID.English,tita.substring(114, 120)));
        this.setTXNSTAN(EbcdicConverter.fromHex(CCSID.English,tita.substring(120, 134)));
        this.setTERMINALID(EbcdicConverter.fromHex(CCSID.English,tita.substring(134, 150)));
        this.setTERMINAL_TYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(150, 158)));
        this.setCARDISSUE_BANK(EbcdicConverter.fromHex(CCSID.English,tita.substring(158, 164)));
        this.setCARDTYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(164, 166)));
        this.setRESPONSE_CODE(EbcdicConverter.fromHex(CCSID.English,tita.substring(166, 174)));
        this.setATMTRANSEQ(EbcdicConverter.fromHex(CCSID.English,tita.substring(174, 182)));
        this.setHRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(182, 232)));
        this.setCBSMAC(EbcdicConverter.fromHex(CCSID.English,tita.substring(232, 240)));
        this.setPICCMDCD(EbcdicConverter.fromHex(CCSID.English,tita.substring(240, 244)));
        this.setPICCMDST(EbcdicConverter.fromHex(CCSID.English,tita.substring(244, 248)));
        this.setPICSTATE(EbcdicConverter.fromHex(CCSID.English,tita.substring(248, 250)));
        this.setPICAVAIL(EbcdicConverter.fromHex(CCSID.English,tita.substring(250, 298)));
        this.setPICVARDT(EbcdicConverter.fromHex(CCSID.English,tita.substring(298, 1192)));
        this.setDRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(1192, 1200)));
}

public String makeMessage() {
return "" //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_TRANS(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSYSCODE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSYS_DATETIME(), 14) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFEP_EJNO(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTXN_FLOW(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMSG_CAT(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSOURCE_CHANNEL(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPCODE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFSCODE(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPROCESS_TYPE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getBUSINESS_DATE(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getACQUIRER_BANK(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTXNSTAN(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERMINALID(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERMINAL_TYPE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCARDISSUE_BANK(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCARDTYPE(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getRESPONSE_CODE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getATMTRANSEQ(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getHRVS(), 25) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCBSMAC(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPICCMDCD(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPICCMDST(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPICSTATE(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPICAVAIL(), 24) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPICVARDT(), 447) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 4) //
;
}

public String makeMessageAscii() {
return "" //
            + StringUtils.leftPad(this.getIMS_TRANS(), 8," ") //
            + StringUtils.leftPad(this.getSYSCODE(), 4," ") //
            + StringUtils.leftPad(this.getSYS_DATETIME(), 14," ") //
            + StringUtils.leftPad(this.getFEP_EJNO(), 8," ") //
            + StringUtils.leftPad(this.getTXN_FLOW(), 1," ") //
            + StringUtils.leftPad(this.getMSG_CAT(), 2," ") //
            + StringUtils.leftPad(this.getSOURCE_CHANNEL(), 3," ") //
            + StringUtils.leftPad(this.getPCODE(), 4," ") //
            + StringUtils.leftPad(this.getFSCODE(), 2," ") //
            + StringUtils.leftPad(this.getPROCESS_TYPE(), 4," ") //
            + StringUtils.leftPad(this.getBUSINESS_DATE(), 7," ") //
            + StringUtils.leftPad(this.getACQUIRER_BANK(), 3," ") //
            + StringUtils.leftPad(this.getTXNSTAN(), 7," ") //
            + StringUtils.leftPad(this.getTERMINALID(), 8," ") //
            + StringUtils.leftPad(this.getTERMINAL_TYPE(), 4," ") //
            + StringUtils.leftPad(this.getCARDISSUE_BANK(), 3," ") //
            + StringUtils.leftPad(this.getCARDTYPE(), 1," ") //
            + StringUtils.leftPad(this.getRESPONSE_CODE(), 4," ") //
            + StringUtils.leftPad(this.getATMTRANSEQ(), 4," ") //
            + StringUtils.leftPad(this.getHRVS(), 25," ") //
            + StringUtils.leftPad(this.getCBSMAC(), 4," ") //
            + StringUtils.leftPad(this.getPICCMDCD(), 2," ") //
            + StringUtils.leftPad(this.getPICCMDST(), 2," ") //
            + StringUtils.leftPad(this.getPICSTATE(), 1," ") //
            + StringUtils.leftPad(this.getPICAVAIL(), 24," ") //
            + StringUtils.leftPad(this.getPICVARDT(), 447," ") //
            + StringUtils.leftPad(this.getDRVS(), 4," ") //
;
}
}
