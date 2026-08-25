package com.syscom.fep.vo.text.credit.response;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.credit.CreditGeneral;
import com.syscom.fep.vo.text.credit.CreditTextBase;

public class B09_Response extends CreditTextBase {
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

    //
    @Field(length = 107, optional = true)
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
        return this.parseFlatfile(this.getClass(), flatfile);
    }

    @Override
    public String makeMessageFromGeneral(CreditGeneral general) throws Exception {
        return null;
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
