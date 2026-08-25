package com.syscom.fep.vo.text.credit.response;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.credit.CreditGeneral;
import com.syscom.fep.vo.text.credit.CreditTextBase;

public class B17_Response extends CreditTextBase {
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

    //轉出銀行別
    @Field(length = 7, optional = true)
    private String _BANK_W = "";

    //轉出帳號
    @Field(length = 16, optional = true)
    private String _ACTNO_W = "";

    //轉入銀行別
    @Field(length = 7, optional = true)
    private String _BANK_D = "";

    //轉入帳號
    @Field(length = 16, optional = true)
    private String _ACTNO_D = "";

    //交易金額
    @Field(length = 10, optional = true)
    private String _TXAMT = "";

    //身分證字號
    @Field(length = 508, optional = true)
    private String _ID = "";

    //財金序號
    @Field(length = 10, optional = true)
    private String _STAN = "";

    //是否檢查ID
    @Field(length = 1, optional = true)
    private String _CHKID = "";

    //訊息押碼
    @Field(length = 8, optional = true)
    private String _MAC = "";

    //PINBLOCK押碼方式
    @Field(length = 1, optional = true)
    private String _TripleDES = "";

    //
    @Field(length = 442, optional = true)
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
     轉出銀行別

     <remark></remark>
     */
    public String getBankW() {
        return _BANK_W;
    }
    public void setBankW(String value) {
        _BANK_W = value;
    }

    /**
     轉出帳號

     <remark></remark>
     */
    public String getActnoW() {
        return _ACTNO_W;
    }
    public void setActnoW(String value) {
        _ACTNO_W = value;
    }

    /**
     轉入銀行別

     <remark></remark>
     */
    public String getBankD() {
        return _BANK_D;
    }
    public void setBankD(String value) {
        _BANK_D = value;
    }

    /**
     轉入帳號

     <remark></remark>
     */
    public String getActnoD() {
        return _ACTNO_D;
    }
    public void setActnoD(String value) {
        _ACTNO_D = value;
    }

    /**
     交易金額

     <remark></remark>
     */
    public String getTXAMT() {
        return _TXAMT;
    }
    public void setTXAMT(String value) {
        _TXAMT = value;
    }

    /**
     身分證字號

     <remark></remark>
     */
    public String getID() {
        return _ID;
    }
    public void setID(String value) {
        _ID = value;
    }

    /**
     財金序號

     <remark></remark>
     */
    public String getSTAN() {
        return _STAN;
    }
    public void setSTAN(String value) {
        _STAN = value;
    }

    /**
     是否檢查ID

     <remark>0:不檢查	1:檢查</remark>
     */
    public String getCHKID() {
        return _CHKID;
    }
    public void setCHKID(String value) {
        _CHKID = value;
    }

    /**
     訊息押碼

     <remark></remark>
     */
    public String getMAC() {
        return _MAC;
    }
    public void setMAC(String value) {
        _MAC = value;
    }

    /**
     PINBLOCK押碼方式

     <remark>1：3DES	0：SINGLE DES</remark>
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
