package com.syscom.fep.vo.text.credit.response;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.credit.CreditGeneral;
import com.syscom.fep.vo.text.credit.CreditTextBase;

public class B15_Response extends CreditTextBase {
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

    //應繳金額
    @Field(length = 12, optional = true)
    private String _CURRBAL = "";

    //最低應繳金額
    @Field(length = 12, optional = true)
    private String _DUEAMT = "";

    //結帳日
    @Field(length = 8, optional = true)
    private String _BILLDATE = "";

    //最近一次繳款日
    @Field(length = 8, optional = true)
    private String _LTXDT = "";

    //卡片信用額度
    @Field(length = 9, optional = true)
    private String _CARD_LIMIT = "";

    //正負號
    @Field(length = 1, optional = true)
    private String _CARD_SIGN = "";

    //卡片可用餘額
    @Field(length = 9, optional = true)
    private String _CARD_BAL = "";

    //預借現金額度
    @Field(length = 9, optional = true)
    private String _CA_LIMIT = "";

    //正負號
    @Field(length = 1, optional = true)
    private String _CA_SIGN = "";

    //預現可用餘額
    @Field(length = 9, optional = true)
    private String _CA_BAL = "";


    //
    @Field(length = 451, optional = true)
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
     應繳金額

     <remark></remark>
     */
    public String getCURRBAL() {
        return _CURRBAL;
    }
    public void setCURRBAL(String value) {
        _CURRBAL = value;
    }

    /**
     最低應繳金額

     <remark></remark>
     */
    public String getDUEAMT() {
        return _DUEAMT;
    }
    public void setDUEAMT(String value) {
        _DUEAMT = value;
    }

    /**
     結帳日

     <remark></remark>
     */
    public String getBILLDATE() {
        return _BILLDATE;
    }
    public void setBILLDATE(String value) {
        _BILLDATE = value;
    }

    /**
     最近一次繳款日

     <remark></remark>
     */
    public String getLTXDT() {
        return _LTXDT;
    }
    public void setLTXDT(String value) {
        _LTXDT = value;
    }

    /**
     卡片信用額度

     <remark></remark>
     */
    public String getCardLimit() {
        return _CARD_LIMIT;
    }
    public void setCardLimit(String value) {
        _CARD_LIMIT = value;
    }

    /**
     正負號

     <remark></remark>
     */
    public String getCardSign() {
        return _CARD_SIGN;
    }
    public void setCardSign(String value) {
        _CARD_SIGN = value;
    }

    /**
     卡片可用餘額

     <remark></remark>
     */
    public String getCardBal() {
        return _CARD_BAL;
    }
    public void setCardBal(String value) {
        _CARD_BAL = value;
    }

    /**
     預借現金額度

     <remark></remark>
     */
    public String getCaLimit() {
        return _CA_LIMIT;
    }
    public void setCaLimit(String value) {
        _CA_LIMIT = value;
    }

    /**
     正負號

     <remark></remark>
     */
    public String getCaSign() {
        return _CA_SIGN;
    }
    public void setCaSign(String value) {
        _CA_SIGN = value;
    }

    /**
     預現可用餘額

     <remark></remark>
     */
    public String getCaBal() {
        return _CA_BAL;
    }
    public void setCaBal(String value) {
        _CA_BAL = value;
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
