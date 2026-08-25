package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class CB_CS_O001 extends IMSTextBase {
    @Field(length = 8)
        private String IMS_TRANS = StringUtils.EMPTY;

    @Field(length = 4)
        private String SYSCODE = StringUtils.EMPTY;

    @Field(length = 14)
        private String SYS_DATETIME = StringUtils.EMPTY;

    @Field(length = 8)
        private String FEP_EJNO = StringUtils.EMPTY;

    @Field(length = 2)
        private String FSCODE = StringUtils.EMPTY;

    @Field(length = 4)
        private String IMSRC4_FISC = StringUtils.EMPTY;

    @Field(length = 3)
        private String IMSRC_TCB = StringUtils.EMPTY;

    @Field(length = 7)
        private String IMSBUSINESS_DATE = StringUtils.EMPTY;

    @Field(length = 1)
        private String IMSACCT_FLAG = StringUtils.EMPTY;

    @Field(length = 1)
        private String IMSRVS_FLAG = StringUtils.EMPTY;

    @Field(length = 6)
        private String IMS_TXN_TIME = StringUtils.EMPTY;

    @Field(length = 10)
        private String IMS_OUT_STAN = StringUtils.EMPTY;

    @Field(length = 4)
        private String IMS_FMMBR = StringUtils.EMPTY;

    @Field(length = 4)
        private String IMS_TMMBR = StringUtils.EMPTY;

    @Field(length = 20)
        private String HRVS = StringUtils.EMPTY;

    @Field(length = 4)
        private String IMS_MAC = StringUtils.EMPTY;

    @Field(length = 2)
        private String TXNTYPE_CODE = StringUtils.EMPTY;

    @Field(length = 2)
        private String MSGTYP = StringUtils.EMPTY;

    @Field(length = 4)
        private String C07LENTH = StringUtils.EMPTY;

    @Field(length = 7)
        private BigDecimal C07AMT1;

    @Field(length = 7)
        private BigDecimal C07AMT2;

    @Field(length = 7)
        private BigDecimal C07AMT3;

    @Field(length = 7)
        private BigDecimal C07AMT4;

    @Field(length = 7)
        private BigDecimal C07AMT5;

    @Field(length = 7)
        private BigDecimal C07AMT6;

    @Field(length = 7)
        private BigDecimal C07AMT7;

    @Field(length = 7)
        private BigDecimal C07AMT8;

    @Field(length = 7)
        private BigDecimal C07AMT9;

    @Field(length = 7)
        private BigDecimal C07AMT10;

    @Field(length = 7)
        private BigDecimal C07AMT11;

    @Field(length = 7)
        private BigDecimal C07AMT12;

    @Field(length = 7)
        private BigDecimal C07AMT13;

    @Field(length = 7)
        private BigDecimal C07AMT14;

    @Field(length = 7)
        private BigDecimal C07AMT15;

    @Field(length = 7)
        private BigDecimal C07AMT16;

    @Field(length = 7)
        private BigDecimal C07AMT17;

    @Field(length = 7)
        private BigDecimal C07AMT18;

    @Field(length = 7)
        private BigDecimal C07AMT19;

    @Field(length = 7)
        private BigDecimal C07AMT20;

    @Field(length = 7)
        private BigDecimal C07AMT21;

    @Field(length = 7)
        private BigDecimal C07AMT22;

    @Field(length = 7)
        private BigDecimal C07AMT23;

    @Field(length = 7)
        private BigDecimal C07AMT24;

    @Field(length = 7)
        private BigDecimal C07AMT25;

    @Field(length = 7)
        private BigDecimal C07AMT26;

    @Field(length = 310)
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
        public String getFSCODE(){
        return this.FSCODE;
        }
        public void setFSCODE(String FSCODE){
        this.FSCODE = FSCODE;
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
        public String getIMSBUSINESS_DATE(){
        return this.IMSBUSINESS_DATE;
        }
        public void setIMSBUSINESS_DATE(String IMSBUSINESS_DATE){
        this.IMSBUSINESS_DATE = IMSBUSINESS_DATE;
        }
        public String getIMSACCT_FLAG(){
        return this.IMSACCT_FLAG;
        }
        public void setIMSACCT_FLAG(String IMSACCT_FLAG){
        this.IMSACCT_FLAG = IMSACCT_FLAG;
        }
        public String getIMSRVS_FLAG(){
        return this.IMSRVS_FLAG;
        }
        public void setIMSRVS_FLAG(String IMSRVS_FLAG){
        this.IMSRVS_FLAG = IMSRVS_FLAG;
        }
        public String getIMS_TXN_TIME(){
        return this.IMS_TXN_TIME;
        }
        public void setIMS_TXN_TIME(String IMS_TXN_TIME){
        this.IMS_TXN_TIME = IMS_TXN_TIME;
        }
        public String getIMS_OUT_STAN(){
        return this.IMS_OUT_STAN;
        }
        public void setIMS_OUT_STAN(String IMS_OUT_STAN){
        this.IMS_OUT_STAN = IMS_OUT_STAN;
        }
        public String getIMS_FMMBR(){
        return this.IMS_FMMBR;
        }
        public void setIMS_FMMBR(String IMS_FMMBR){
        this.IMS_FMMBR = IMS_FMMBR;
        }
        public String getIMS_TMMBR(){
        return this.IMS_TMMBR;
        }
        public void setIMS_TMMBR(String IMS_TMMBR){
        this.IMS_TMMBR = IMS_TMMBR;
        }
        public String getHRVS(){
        return this.HRVS;
        }
        public void setHRVS(String HRVS){
        this.HRVS = HRVS;
        }
        public String getIMS_MAC(){
        return this.IMS_MAC;
        }
        public void setIMS_MAC(String IMS_MAC){
        this.IMS_MAC = IMS_MAC;
        }
        public String getTXNTYPE_CODE(){
        return this.TXNTYPE_CODE;
        }
        public void setTXNTYPE_CODE(String TXNTYPE_CODE){
        this.TXNTYPE_CODE = TXNTYPE_CODE;
        }
        public String getMSGTYP(){
        return this.MSGTYP;
        }
        public void setMSGTYP(String MSGTYP){
        this.MSGTYP = MSGTYP;
        }
        public String getC07LENTH(){
        return this.C07LENTH;
        }
        public void setC07LENTH(String C07LENTH){
        this.C07LENTH = C07LENTH;
        }
        public BigDecimal getC07AMT1(){
        return this.C07AMT1;
        }
        public void setC07AMT1(BigDecimal C07AMT1){
        this.C07AMT1 = C07AMT1;
        }
        public BigDecimal getC07AMT2(){
        return this.C07AMT2;
        }
        public void setC07AMT2(BigDecimal C07AMT2){
        this.C07AMT2 = C07AMT2;
        }
        public BigDecimal getC07AMT3(){
        return this.C07AMT3;
        }
        public void setC07AMT3(BigDecimal C07AMT3){
        this.C07AMT3 = C07AMT3;
        }
        public BigDecimal getC07AMT4(){
        return this.C07AMT4;
        }
        public void setC07AMT4(BigDecimal C07AMT4){
        this.C07AMT4 = C07AMT4;
        }
        public BigDecimal getC07AMT5(){
        return this.C07AMT5;
        }
        public void setC07AMT5(BigDecimal C07AMT5){
        this.C07AMT5 = C07AMT5;
        }
        public BigDecimal getC07AMT6(){
        return this.C07AMT6;
        }
        public void setC07AMT6(BigDecimal C07AMT6){
        this.C07AMT6 = C07AMT6;
        }
        public BigDecimal getC07AMT7(){
        return this.C07AMT7;
        }
        public void setC07AMT7(BigDecimal C07AMT7){
        this.C07AMT7 = C07AMT7;
        }
        public BigDecimal getC07AMT8(){
        return this.C07AMT8;
        }
        public void setC07AMT8(BigDecimal C07AMT8){
        this.C07AMT8 = C07AMT8;
        }
        public BigDecimal getC07AMT9(){
        return this.C07AMT9;
        }
        public void setC07AMT9(BigDecimal C07AMT9){
        this.C07AMT9 = C07AMT9;
        }
        public BigDecimal getC07AMT10(){
        return this.C07AMT10;
        }
        public void setC07AMT10(BigDecimal C07AMT10){
        this.C07AMT10 = C07AMT10;
        }
        public BigDecimal getC07AMT11(){
        return this.C07AMT11;
        }
        public void setC07AMT11(BigDecimal C07AMT11){
        this.C07AMT11 = C07AMT11;
        }
        public BigDecimal getC07AMT12(){
        return this.C07AMT12;
        }
        public void setC07AMT12(BigDecimal C07AMT12){
        this.C07AMT12 = C07AMT12;
        }
        public BigDecimal getC07AMT13(){
        return this.C07AMT13;
        }
        public void setC07AMT13(BigDecimal C07AMT13){
        this.C07AMT13 = C07AMT13;
        }
        public BigDecimal getC07AMT14(){
        return this.C07AMT14;
        }
        public void setC07AMT14(BigDecimal C07AMT14){
        this.C07AMT14 = C07AMT14;
        }
        public BigDecimal getC07AMT15(){
        return this.C07AMT15;
        }
        public void setC07AMT15(BigDecimal C07AMT15){
        this.C07AMT15 = C07AMT15;
        }
        public BigDecimal getC07AMT16(){
        return this.C07AMT16;
        }
        public void setC07AMT16(BigDecimal C07AMT16){
        this.C07AMT16 = C07AMT16;
        }
        public BigDecimal getC07AMT17(){
        return this.C07AMT17;
        }
        public void setC07AMT17(BigDecimal C07AMT17){
        this.C07AMT17 = C07AMT17;
        }
        public BigDecimal getC07AMT18(){
        return this.C07AMT18;
        }
        public void setC07AMT18(BigDecimal C07AMT18){
        this.C07AMT18 = C07AMT18;
        }
        public BigDecimal getC07AMT19(){
        return this.C07AMT19;
        }
        public void setC07AMT19(BigDecimal C07AMT19){
        this.C07AMT19 = C07AMT19;
        }
        public BigDecimal getC07AMT20(){
        return this.C07AMT20;
        }
        public void setC07AMT20(BigDecimal C07AMT20){
        this.C07AMT20 = C07AMT20;
        }
        public BigDecimal getC07AMT21(){
        return this.C07AMT21;
        }
        public void setC07AMT21(BigDecimal C07AMT21){
        this.C07AMT21 = C07AMT21;
        }
        public BigDecimal getC07AMT22(){
        return this.C07AMT22;
        }
        public void setC07AMT22(BigDecimal C07AMT22){
        this.C07AMT22 = C07AMT22;
        }
        public BigDecimal getC07AMT23(){
        return this.C07AMT23;
        }
        public void setC07AMT23(BigDecimal C07AMT23){
        this.C07AMT23 = C07AMT23;
        }
        public BigDecimal getC07AMT24(){
        return this.C07AMT24;
        }
        public void setC07AMT24(BigDecimal C07AMT24){
        this.C07AMT24 = C07AMT24;
        }
        public BigDecimal getC07AMT25(){
        return this.C07AMT25;
        }
        public void setC07AMT25(BigDecimal C07AMT25){
        this.C07AMT25 = C07AMT25;
        }
        public BigDecimal getC07AMT26(){
        return this.C07AMT26;
        }
        public void setC07AMT26(BigDecimal C07AMT26){
        this.C07AMT26 = C07AMT26;
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
        this.setFSCODE(EbcdicConverter.fromHex(CCSID.English,tita.substring(68, 72)));
        this.setIMSRC4_FISC(EbcdicConverter.fromHex(CCSID.English,tita.substring(72, 80)));
        this.setIMSRC_TCB(EbcdicConverter.fromHex(CCSID.English,tita.substring(80, 86)));
        this.setIMSBUSINESS_DATE(EbcdicConverter.fromHex(CCSID.English,tita.substring(86, 100)));
        this.setIMSACCT_FLAG(EbcdicConverter.fromHex(CCSID.English,tita.substring(100, 102)));
        this.setIMSRVS_FLAG(EbcdicConverter.fromHex(CCSID.English,tita.substring(102, 104)));
        this.setIMS_TXN_TIME(EbcdicConverter.fromHex(CCSID.English,tita.substring(104, 116)));
        this.setIMS_OUT_STAN(EbcdicConverter.fromHex(CCSID.English,tita.substring(116, 136)));
        this.setIMS_FMMBR(EbcdicConverter.fromHex(CCSID.English,tita.substring(136, 144)));
        this.setIMS_TMMBR(EbcdicConverter.fromHex(CCSID.English,tita.substring(144, 152)));
        this.setHRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(152, 192)));
        this.setIMS_MAC(EbcdicConverter.fromHex(CCSID.English,tita.substring(192, 200)));
        this.setTXNTYPE_CODE(EbcdicConverter.fromHex(CCSID.English,tita.substring(200, 204)));
        this.setMSGTYP(EbcdicConverter.fromHex(CCSID.English,tita.substring(204, 208)));
        this.setC07LENTH(EbcdicConverter.fromHex(CCSID.English,tita.substring(208, 216)));
        this.setC07AMT1(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(216, 230).trim()), false, 0));
        this.setC07AMT2(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(230, 244).trim()), false, 0));
        this.setC07AMT3(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(244, 258).trim()), false, 0));
        this.setC07AMT4(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(258, 272).trim()), false, 0));
        this.setC07AMT5(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(272, 286).trim()), false, 0));
        this.setC07AMT6(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(286, 300).trim()), false, 0));
        this.setC07AMT7(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(300, 314).trim()), false, 0));
        this.setC07AMT8(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(314, 328).trim()), false, 0));
        this.setC07AMT9(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(328, 342).trim()), false, 0));
        this.setC07AMT10(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(342, 356).trim()), false, 0));
        this.setC07AMT11(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(356, 370).trim()), false, 0));
        this.setC07AMT12(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(370, 384).trim()), false, 0));
        this.setC07AMT13(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(384, 398).trim()), false, 0));
        this.setC07AMT14(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(398, 412).trim()), false, 0));
        this.setC07AMT15(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(412, 426).trim()), false, 0));
        this.setC07AMT16(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(426, 440).trim()), false, 0));
        this.setC07AMT17(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(440, 454).trim()), false, 0));
        this.setC07AMT18(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(454, 468).trim()), false, 0));
        this.setC07AMT19(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(468, 482).trim()), false, 0));
        this.setC07AMT20(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(482, 496).trim()), false, 0));
        this.setC07AMT21(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(496, 510).trim()), false, 0));
        this.setC07AMT22(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(510, 524).trim()), false, 0));
        this.setC07AMT23(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(524, 538).trim()), false, 0));
        this.setC07AMT24(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(538, 552).trim()), false, 0));
        this.setC07AMT25(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(552, 566).trim()), false, 0));
        this.setC07AMT26(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(566, 580).trim()), false, 0));
        this.setDRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(580, 1200)));
}

public String makeMessage() {
return "" //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_TRANS(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSYSCODE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSYS_DATETIME(), 14) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFEP_EJNO(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFSCODE(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMSRC4_FISC(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMSRC_TCB(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMSBUSINESS_DATE(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMSACCT_FLAG(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMSRVS_FLAG(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_TXN_TIME(), 6) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_OUT_STAN(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_FMMBR(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_TMMBR(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getHRVS(), 20) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_MAC(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTXNTYPE_CODE(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMSGTYP(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getC07LENTH(), 4) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT1(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT2(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT3(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT4(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT5(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT6(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT7(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT8(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT9(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT10(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT11(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT12(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT13(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT14(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT15(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT16(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT17(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT18(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT19(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT20(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT21(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT22(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT23(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT24(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT25(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getC07AMT26(), 7, false, 0, false) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 310) //
;
}

public String makeMessageAscii() {
return "" //
            + StringUtils.leftPad(this.getIMS_TRANS(), 8," ") //
            + StringUtils.leftPad(this.getSYSCODE(), 4," ") //
            + StringUtils.leftPad(this.getSYS_DATETIME(), 14," ") //
            + StringUtils.leftPad(this.getFEP_EJNO(), 8," ") //
            + StringUtils.leftPad(this.getFSCODE(), 2," ") //
            + StringUtils.leftPad(this.getIMSRC4_FISC(), 4," ") //
            + StringUtils.leftPad(this.getIMSRC_TCB(), 3," ") //
            + StringUtils.leftPad(this.getIMSBUSINESS_DATE(), 7," ") //
            + StringUtils.leftPad(this.getIMSACCT_FLAG(), 1," ") //
            + StringUtils.leftPad(this.getIMSRVS_FLAG(), 1," ") //
            + StringUtils.leftPad(this.getIMS_TXN_TIME(), 6," ") //
            + StringUtils.leftPad(this.getIMS_OUT_STAN(), 10," ") //
            + StringUtils.leftPad(this.getIMS_FMMBR(), 4," ") //
            + StringUtils.leftPad(this.getIMS_TMMBR(), 4," ") //
            + StringUtils.leftPad(this.getHRVS(), 20," ") //
            + StringUtils.leftPad(this.getIMS_MAC(), 4," ") //
            + StringUtils.leftPad(this.getTXNTYPE_CODE(), 2," ") //
            + StringUtils.leftPad(this.getMSGTYP(), 2," ") //
            + StringUtils.leftPad(this.getC07LENTH(), 4," ") //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT1(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT2(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT3(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT4(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT5(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT6(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT7(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT8(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT9(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT10(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT11(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT12(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT13(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT14(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT15(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT16(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT17(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT18(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT19(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT20(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT21(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT22(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT23(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT24(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT25(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getC07AMT26(), 7, false, 0, false) //
            + StringUtils.leftPad(this.getDRVS(), 310," ") //
;
}
}
