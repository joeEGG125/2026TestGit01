package com.syscom.fep.vo.text.credit.request;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.credit.CreditGeneral;
import com.syscom.fep.vo.text.credit.CreditGeneralRequest;
import com.syscom.fep.vo.text.credit.CreditTextBase;
import org.apache.commons.lang3.StringUtils;

public class B09_Request extends CreditTextBase {
    //交易序號
    @Field(length = 6)
    private String _NUMBER = "";

    //卡號
    @Field(length = 16)
    private String _CARD_NO = "";

    //金融卡帳號
    @Field(length = 16)
    private String _ACCOUNT = "";

    //訊息型態
    @Field(length = 1)
    private String _CD1 = "";

    //來源別
    @Field(length = 1)
    private String _CD2 = "";

    //MSG FLOW
    @Field(length = 1)
    private String _CD3 = "";

    //系統日
    @Field(length = 8)
    private String _TXDATE = "";

    //輸入行
    @Field(length = 3)
    private String _KINBR = "";

    //櫃台機號
    @Field(length = 2)
    private String _WSNO = "";

    //交易傳輸編號
    @Field(length = 5)
    private String _TXSEQ = "";

    //營業日
    @Field(length = 8)
    private String _TBSDY = "";

    //MODE
    @Field(length = 1)
    private String _MODE = "";

    //交易代號
    @Field(length = 3)
    private String _TXCD = "";

    //拒絕代碼
    @Field(length = 4)
    private String _RC = "";

    //信用卡交易序號
    @Field(length = 6)
    private String _ASPSTAN = "";

    //信用卡號
    @Field(length = 16)
    private String _CARDNO = "";

    //金融卡號
    @Field(length = 14)
    private String _ACTNO = "";

    //金融卡序號
    @Field(length = 2)
    private String _SEQNO = "";

    //交易種類
    @Field(length = 3, optional = true)
    private String _APID = "";

    //Format Pin Black
    @Field(length = 16, optional = true)
    private String _FPB = "";

    //第三軌資料
    @Field(length = 104, optional = true)
    private String _TRACK3 = "";

    //帳號序號
    @Field(length = 2, optional = true)
    private String _ACT_IDX = "";

    //約定轉入銀行別
    @Field(length = 3, optional = true)
    private String _ACTBK1 = "";

    //約定轉入帳號
    @Field(length = 16, optional = true)
    private String _ACTNO1 = "";

    //約定轉入銀行別
    @Field(length = 3, optional = true)
    private String _ACTBK2 = "";

    //約定轉入帳號
    @Field(length = 16, optional = true)
    private String _ACTNO2 = "";

    //約定轉入銀行別
    @Field(length = 3, optional = true)
    private String _ACTBK3 = "";

    //約定轉入帳號
    @Field(length = 16, optional = true)
    private String _ACTNO3 = "";

    //約定轉入銀行別
    @Field(length = 3, optional = true)
    private String _ACTBK4 = "";

    //約定轉入帳號
    @Field(length = 16, optional = true)
    private String _ACTNO4 = "";

    //約定轉入銀行別
    @Field(length = 3, optional = true)
    private String _ACTBK5 = "";

    //約定轉入帳號
    @Field(length = 16, optional = true)
    private String _ACTNO5 = "";

    //約定轉入銀行別
    @Field(length = 3, optional = true)
    private String _ACTBK6 = "";

    //約定轉入帳號
    @Field(length = 16, optional = true)
    private String _ACTNO6 = "";

    //約定轉入銀行別
    @Field(length = 3, optional = true)
    private String _ACTBK7 = "";

    //約定轉入帳號
    @Field(length = 16, optional = true)
    private String _ACTNO7 = "";

    //約定轉入銀行別
    @Field(length = 3, optional = true)
    private String _ACTBK8 = "";

    //約定轉入帳號
    @Field(length = 16, optional = true)
    private String _ACTNO8 = "";

    //約定轉入銀行別
    @Field(length = 3, optional = true)
    private String _ACTBK9 = "";

    //約定轉入帳號
    @Field(length = 16, optional = true)
    private String _ACTNO9 = "";

    //約定轉入銀行別
    @Field(length = 3, optional = true)
    private String _ACTBK10 = "";

    //約定轉入帳號
    @Field(length = 16, optional = true)
    private String _ACTNO10 = "";

    //約定轉入銀行別
    @Field(length = 3, optional = true)
    private String _ACTBK11 = "";

    //約定轉入帳號
    @Field(length = 16, optional = true)
    private String _ACTNO11 = "";

    //約定轉入銀行別
    @Field(length = 3, optional = true)
    private String _ACTBK12 = "";

    //約定轉入帳號
    @Field(length = 16, optional = true)
    private String _ACTNO12 = "";

    //約定轉入銀行別
    @Field(length = 3, optional = true)
    private String _ACTBK13 = "";

    //約定轉入帳號
    @Field(length = 16, optional = true)
    private String _ACTNO13 = "";

    //約定轉入銀行別
    @Field(length = 3, optional = true)
    private String _ACTBK14 = "";

    //約定轉入帳號
    @Field(length = 16, optional = true)
    private String _ACTNO14 = "";

    //約定轉入銀行別
    @Field(length = 3, optional = true)
    private String _ACTBK15 = "";

    //約定轉入帳號
    @Field(length = 16, optional = true)
    private String _ACTNO15 = "";

    //備註
    @Field(length = 4, optional = true)
    private String _MEMNO = "";

    //訊息押碼
    @Field(length = 8, optional = true)
    private String _MAC = "";

    //押碼方式
    @Field(length = 1, optional = true)
    private String _TripleDES = "";

    //
    @Field(length = 106, optional = true)
    private String _FILLER1 = "";

    private static final int _TotalLength = 642;


    /**
     交易序號

     <remark>空白 (MQ HEADER)</remark>
     */
    public String getNUMBER() {
        return _NUMBER;
    }
    public void setNUMBER(String value) {
        _NUMBER = value;
    }

    /**
     卡號

     <remark>空白 (MQ HEADER)</remark>
     */
    public String getCardNo() {
        return _CARD_NO;
    }
    public void setCardNo(String value) {
        _CARD_NO = value;
    }

    /**
     金融卡帳號

     <remark>空白 (MQ HEADER)</remark>
     */
    public String getACCOUNT() {
        return _ACCOUNT;
    }
    public void setACCOUNT(String value) {
        _ACCOUNT = value;
    }

    /**
     訊息型態

     <remark>A</remark>
     */
    public String getCD1() {
        return _CD1;
    }
    public void setCD1(String value) {
        _CD1 = value;
    }

    /**
     來源別

     <remark>1：REQ FROM BSP</remark>
     */
    public String getCD2() {
        return _CD2;
    }
    public void setCD2(String value) {
        _CD2 = value;
    }

    /**
     MSG FLOW

     <remark>1：REQUEST MSG</remark>
     */
    public String getCD3() {
        return _CD3;
    }
    public void setCD3(String value) {
        _CD3 = value;
    }

    /**
     系統日

     <remark></remark>
     */
    public String getTXDATE() {
        return _TXDATE;
    }
    public void setTXDATE(String value) {
        _TXDATE = value;
    }

    /**
     輸入行

     <remark></remark>
     */
    public String getKINBR() {
        return _KINBR;
    }
    public void setKINBR(String value) {
        _KINBR = value;
    }

    /**
     櫃台機號

     <remark></remark>
     */
    public String getWSNO() {
        return _WSNO;
    }
    public void setWSNO(String value) {
        _WSNO = value;
    }

    /**
     交易傳輸編號

     <remark></remark>
     */
    public String getTXSEQ() {
        return _TXSEQ;
    }
    public void setTXSEQ(String value) {
        _TXSEQ = value;
    }

    /**
     營業日

     <remark></remark>
     */
    public String getTBSDY() {
        return _TBSDY;
    }
    public void setTBSDY(String value) {
        _TBSDY = value;
    }

    /**
     MODE

     <remark>1：ONLINE	2：HALF	3：NIGHT</remark>
     */
    public String getMODE() {
        return _MODE;
    }
    public void setMODE(String value) {
        _MODE = value;
    }

    /**
     交易代號

     <remark>B16 or B20</remark>
     */
    public String getTXCD() {
        return _TXCD;
    }
    public void setTXCD(String value) {
        _TXCD = value;
    }

    /**
     拒絕代碼

     <remark>空白</remark>
     */
    public String getRC() {
        return _RC;
    }
    public void setRC(String value) {
        _RC = value;
    }

    /**
     信用卡交易序號

     <remark></remark>
     */
    public String getASPSTAN() {
        return _ASPSTAN;
    }
    public void setASPSTAN(String value) {
        _ASPSTAN = value;
    }

    /**
     信用卡號

     <remark></remark>
     */
    public String getCARDNO() {
        return _CARDNO;
    }
    public void setCARDNO(String value) {
        _CARDNO = value;
    }

    /**
     金融卡號

     <remark></remark>
     */
    public String getACTNO() {
        return _ACTNO;
    }
    public void setACTNO(String value) {
        _ACTNO = value;
    }

    /**
     金融卡序號

     <remark></remark>
     */
    public String getSEQNO() {
        return _SEQNO;
    }
    public void setSEQNO(String value) {
        _SEQNO = value;
    }

    /**
     交易種類

     <remark>AAC：約定轉入帳號查詢AIT：利率查詢密碼驗證</remark>
     */
    public String getAPID() {
        return _APID;
    }
    public void setAPID(String value) {
        _APID = value;
    }

    /**
     Format Pin Black

     <remark></remark>
     */
    public String getFPB() {
        return _FPB;
    }
    public void setFPB(String value) {
        _FPB = value;
    }

    /**
     第三軌資料

     <remark></remark>
     */
    public String getTRACK3() {
        return _TRACK3;
    }
    public void setTRACK3(String value) {
        _TRACK3 = value;
    }

    /**
     帳號序號

     <remark></remark>
     */
    public String getActIdx() {
        return _ACT_IDX;
    }
    public void setActIdx(String value) {
        _ACT_IDX = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public String getACTBK1() {
        return _ACTBK1;
    }
    public void setACTBK1(String value) {
        _ACTBK1 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public String getACTNO1() {
        return _ACTNO1;
    }
    public void setACTNO1(String value) {
        _ACTNO1 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public String getACTBK2() {
        return _ACTBK2;
    }
    public void setACTBK2(String value) {
        _ACTBK2 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public String getACTNO2() {
        return _ACTNO2;
    }
    public void setACTNO2(String value) {
        _ACTNO2 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public String getACTBK3() {
        return _ACTBK3;
    }
    public void setACTBK3(String value) {
        _ACTBK3 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public String getACTNO3() {
        return _ACTNO3;
    }
    public void setACTNO3(String value) {
        _ACTNO3 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public String getACTBK4() {
        return _ACTBK4;
    }
    public void setACTBK4(String value) {
        _ACTBK4 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public String getACTNO4() {
        return _ACTNO4;
    }
    public void setACTNO4(String value) {
        _ACTNO4 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public String getACTBK5() {
        return _ACTBK5;
    }
    public void setACTBK5(String value) {
        _ACTBK5 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public String getACTNO5() {
        return _ACTNO5;
    }
    public void setACTNO5(String value) {
        _ACTNO5 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public String getACTBK6() {
        return _ACTBK6;
    }
    public void setACTBK6(String value) {
        _ACTBK6 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public String getACTNO6() {
        return _ACTNO6;
    }
    public void setACTNO6(String value) {
        _ACTNO6 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public String getACTBK7() {
        return _ACTBK7;
    }
    public void setACTBK7(String value) {
        _ACTBK7 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public String getACTNO7() {
        return _ACTNO7;
    }
    public void setACTNO7(String value) {
        _ACTNO7 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public String getACTBK8() {
        return _ACTBK8;
    }
    public void setACTBK8(String value) {
        _ACTBK8 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public String getACTNO8() {
        return _ACTNO8;
    }
    public void setACTNO8(String value) {
        _ACTNO8 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public String getACTBK9() {
        return _ACTBK9;
    }
    public void setACTBK9(String value) {
        _ACTBK9 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public String getACTNO9() {
        return _ACTNO9;
    }
    public void setACTNO9(String value) {
        _ACTNO9 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public String getACTBK10() {
        return _ACTBK10;
    }
    public void setACTBK10(String value) {
        _ACTBK10 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public String getACTNO10() {
        return _ACTNO10;
    }
    public void setACTNO10(String value) {
        _ACTNO10 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public String getACTBK11() {
        return _ACTBK11;
    }
    public void setACTBK11(String value) {
        _ACTBK11 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public String getACTNO11() {
        return _ACTNO11;
    }
    public void setACTNO11(String value) {
        _ACTNO11 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public String getACTBK12() {
        return _ACTBK12;
    }
    public void setACTBK12(String value) {
        _ACTBK12 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public String getACTNO12() {
        return _ACTNO12;
    }
    public void setACTNO12(String value) {
        _ACTNO12 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public String getACTBK13() {
        return _ACTBK13;
    }
    public void setACTBK13(String value) {
        _ACTBK13 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public String getACTNO13() {
        return _ACTNO13;
    }
    public void setACTNO13(String value) {
        _ACTNO13 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public String getACTBK14() {
        return _ACTBK14;
    }
    public void setACTBK14(String value) {
        _ACTBK14 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public String getACTNO14() {
        return _ACTNO14;
    }
    public void setACTNO14(String value) {
        _ACTNO14 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public String getACTBK15() {
        return _ACTBK15;
    }
    public void setACTBK15(String value) {
        _ACTBK15 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public String getACTNO15() {
        return _ACTNO15;
    }
    public void setACTNO15(String value) {
        _ACTNO15 = value;
    }

    /**
     備註

     <remark></remark>
     */
    public String getMEMNO() {
        return _MEMNO;
    }
    public void setMEMNO(String value) {
        _MEMNO = value;
    }

    /**
     訊息押碼

     <remark>當為”AAC”時才有值，否則為空白.</remark>
     */
    public String getMAC() {
        return _MAC;
    }
    public void setMAC(String value) {
        _MAC = value;
    }

    /**
     押碼方式

     <remark>1：3DES                    	0：SINGLE DES</remark>
     */
    public String getTripleDES() {
        return _TripleDES;
    }
    public void setTripleDES(String value) {
        _TripleDES = value;
    }

    /**


     <remark></remark>
     */
    public String getFILLER1() {
        return _FILLER1;
    }
    public void setFILLER1(String value) {
        _FILLER1 = value;
    }

    @Override
    public CreditGeneral parseFlatfile(String flatfile) throws Exception {
        return null;
    }

    @Override
    public String makeMessageFromGeneral(CreditGeneral general) throws Exception {
        CreditGeneralRequest tempVar = general.getRequest();
        _NUMBER = StringUtils.rightPad(tempVar.getNUMBER(),6,' '); //交易序號
        _CARD_NO = StringUtils.rightPad(tempVar.getCardNo(),16,' '); //卡號
        _ACCOUNT = StringUtils.rightPad(tempVar.getACCOUNT(),16,' '); //金融卡帳號
        _CD1 = StringUtils.rightPad(tempVar.getCD1(),1,' '); //訊息型態
        _CD2 = StringUtils.leftPad(tempVar.getCD2(),1,'0'); //來源別
        _CD3 = StringUtils.leftPad(tempVar.getCD3(),1,'0'); //MSG FLOW
        _TXDATE = StringUtils.leftPad(tempVar.getTXDATE(),8,'0'); //系統日
        _KINBR = StringUtils.leftPad(tempVar.getKINBR(),3,'0'); //輸入行
        _WSNO = StringUtils.leftPad(tempVar.getWSNO(),2,'0'); //櫃台機號
        _TXSEQ = StringUtils.leftPad(tempVar.getTXSEQ(),5,'0'); //交易傳輸編號
        _TBSDY = StringUtils.leftPad(tempVar.getTBSDY(),8,'0'); //營業日
        _MODE = StringUtils.leftPad(tempVar.getMODE(),1,'0'); //MODE
        _TXCD = StringUtils.rightPad(tempVar.getTXCD(),3,' '); //交易代號
        _RC = StringUtils.rightPad(tempVar.getRC(),4,' '); //拒絕代碼
        _ASPSTAN = StringUtils.leftPad(tempVar.getASPSTAN(),6,'0'); //信用卡交易序號
        _CARDNO = StringUtils.rightPad(tempVar.getCARDNO(),16,' '); //信用卡號
        _ACTNO = StringUtils.rightPad(tempVar.getACTNO(),14,' '); //金融卡號
        _SEQNO = StringUtils.leftPad(tempVar.getSEQNO(),2,'0'); //金融卡序號
        _APID = StringUtils.rightPad(tempVar.getAPID(),3,' '); //交易種類
        _FPB = StringUtils.rightPad(tempVar.getFPB(),16,' '); //Format Pin Black
        _TRACK3 = StringUtils.rightPad(tempVar.getTRACK3(),104,' '); //第三軌資料
        _ACT_IDX = StringUtils.leftPad(tempVar.getActIdx(),2,'0'); //帳號序號
        _ACTBK1 = StringUtils.leftPad(tempVar.getACTBK1(),3,'0'); //約定轉入銀行別
        _ACTNO1 = StringUtils.leftPad(tempVar.getACTNO1(),16,'0'); //約定轉入帳號
        _ACTBK2 = StringUtils.leftPad(tempVar.getACTBK2(),3,'0'); //約定轉入銀行別
        _ACTNO2 = StringUtils.leftPad(tempVar.getACTNO2(),16,'0'); //約定轉入帳號
        _ACTBK3 = StringUtils.leftPad(tempVar.getACTBK3(),3,'0'); //約定轉入銀行別
        _ACTNO3 = StringUtils.leftPad(tempVar.getACTNO3(),16,'0'); //約定轉入帳號
        _ACTBK4 = StringUtils.leftPad(tempVar.getACTBK4(),3,'0'); //約定轉入銀行別
        _ACTNO4 = StringUtils.leftPad(tempVar.getACTNO4(),16,'0'); //約定轉入帳號
        _ACTBK5 = StringUtils.leftPad(tempVar.getACTBK5(),3,'0'); //約定轉入銀行別
        _ACTNO5 = StringUtils.leftPad(tempVar.getACTNO5(),16,'0'); //約定轉入帳號
        _ACTBK6 = StringUtils.leftPad(tempVar.getACTBK6(),3,'0'); //約定轉入銀行別
        _ACTNO6 = StringUtils.leftPad(tempVar.getACTNO6(),16,'0'); //約定轉入帳號
        _ACTBK7 = StringUtils.leftPad(tempVar.getACTBK7(),3,'0'); //約定轉入銀行別
        _ACTNO7 = StringUtils.leftPad(tempVar.getACTNO7(),16,'0'); //約定轉入帳號
        _ACTBK8 = StringUtils.leftPad(tempVar.getACTBK8(),3,'0'); //約定轉入銀行別
        _ACTNO8 = StringUtils.leftPad(tempVar.getACTNO8(),16,'0'); //約定轉入帳號
        _ACTBK9 = StringUtils.leftPad(tempVar.getACTBK9(),3,'0'); //約定轉入銀行別
        _ACTNO9 = StringUtils.leftPad(tempVar.getACTNO9(),16,'0'); //約定轉入帳號
        _ACTBK10 = StringUtils.leftPad(tempVar.getACTBK10(),3,'0'); //約定轉入銀行別
        _ACTNO10 = StringUtils.leftPad(tempVar.getACTNO10(),16,'0'); //約定轉入帳號
        _ACTBK11 = StringUtils.leftPad(tempVar.getACTBK11(),3,'0'); //約定轉入銀行別
        _ACTNO11 = StringUtils.leftPad(tempVar.getACTNO11(),16,'0'); //約定轉入帳號
        _ACTBK12 = StringUtils.leftPad(tempVar.getACTBK12(),3,'0'); //約定轉入銀行別
        _ACTNO12 = StringUtils.leftPad(tempVar.getACTNO12(),16,'0'); //約定轉入帳號
        _ACTBK13 = StringUtils.leftPad(tempVar.getACTBK13(),3,'0'); //約定轉入銀行別
        _ACTNO13 = StringUtils.leftPad(tempVar.getACTNO13(),16,'0'); //約定轉入帳號
        _ACTBK14 = StringUtils.leftPad(tempVar.getACTBK14(),3,'0'); //約定轉入銀行別
        _ACTNO14 = StringUtils.leftPad(tempVar.getACTNO14(),16,'0'); //約定轉入帳號
        _ACTBK15 = StringUtils.leftPad(tempVar.getACTBK15(),3,'0'); //約定轉入銀行別
        _ACTNO15 = StringUtils.leftPad(tempVar.getACTNO15(),16,'0'); //約定轉入帳號
        _MEMNO = StringUtils.rightPad(tempVar.getMEMNO(),4,' '); //備註
        _MAC = StringUtils.rightPad(tempVar.getMAC(),8,' '); //訊息押碼
        _TripleDES = StringUtils.rightPad(tempVar.getTripleDES(),1,' '); //押碼方式
        _FILLER1 = tempVar.getFILLER1();

        return _NUMBER + _CARD_NO + _ACCOUNT + _CD1 + _CD2 + _CD3 + _TXDATE + _KINBR + _WSNO + _TXSEQ + _TBSDY + _MODE + _TXCD + _RC + _ASPSTAN + _CARDNO + _ACTNO + _SEQNO + _APID + _FPB + _TRACK3 + _ACT_IDX + _ACTBK1 + _ACTNO1 + _ACTBK2 + _ACTNO2 + _ACTBK3 + _ACTNO3 + _ACTBK4 + _ACTNO4 + _ACTBK5 + _ACTNO5 + _ACTBK6 + _ACTNO6 + _ACTBK7 + _ACTNO7 + _ACTBK8 + _ACTNO8 + _ACTBK9 + _ACTNO9 + _ACTBK10 + _ACTNO10 + _ACTBK11 + _ACTNO11 + _ACTBK12 + _ACTNO12 + _ACTBK13 + _ACTNO13 + _ACTBK14 + _ACTNO14 + _ACTBK15 + _ACTNO15 + _MEMNO + _MAC + _TripleDES + _FILLER1;
    }

    @Override
    public void toGeneral(CreditGeneral general) throws Exception {

    }

    @Override
    public int getTotalLength() {
        // TODO 自動生成的方法存根
        return _TotalLength;
    }
}
