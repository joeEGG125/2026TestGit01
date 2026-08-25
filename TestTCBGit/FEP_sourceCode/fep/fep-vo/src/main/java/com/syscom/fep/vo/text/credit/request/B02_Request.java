package com.syscom.fep.vo.text.credit.request;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.credit.CreditGeneral;
import com.syscom.fep.vo.text.credit.CreditGeneralRequest;
import com.syscom.fep.vo.text.credit.CreditTextBase;
import org.apache.commons.lang3.StringUtils;

public class B02_Request extends CreditTextBase {
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

    //卡片效期
    @Field(length = 4, optional = true)
    private String _VAILDDT = "";

    //出生年月日
    @Field(length = 8, optional = true)
    private String _BIRTHDAY = "";

    //執行動作
    @Field(length = 1, optional = true)
    private String _ACTION = "";

    //停卡理由
    @Field(length = 2, optional = true)
    private String _ASPSTOP = "";

    //
    @Field(length = 514, optional = true)
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
     卡片效期

     <remark></remark>
     */
    public String getVAILDDT() {
        return _VAILDDT;
    }
    public void setVAILDDT(String value) {
        _VAILDDT = value;
    }

    /**
     出生年月日

     <remark></remark>
     */
    public String getBIRTHDAY() {
        return _BIRTHDAY;
    }
    public void setBIRTHDAY(String value) {
        _BIRTHDAY = value;
    }

    /**
     執行動作

     <remark>=1 開卡</remark>
     */
    public String getACTION() {
        return _ACTION;
    }
    public void setACTION(String value) {
        _ACTION = value;
    }

    /**
     停卡理由

     <remark>空白</remark>
     */
    public String getASPSTOP() {
        return _ASPSTOP;
    }
    public void setASPSTOP(String value) {
        _ASPSTOP = value;
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
    public int getTotalLength() {
        // TODO 自動生成的方法存根
        return _TotalLength;
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
        _CD3 =StringUtils.leftPad( tempVar.getCD3(),1,'0'); //MSG FLOW
        _TXDATE =StringUtils.leftPad( tempVar.getTXDATE(),8,'0'); //系統日
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
        _VAILDDT = StringUtils.rightPad(tempVar.getVALIDDT(),4,' '); //卡片效期
        _BIRTHDAY = StringUtils.rightPad(tempVar.getBIRTHDAY(),8,' '); //出生年月日
        _ACTION = StringUtils.rightPad(tempVar.getACTION(),1,' '); //執行動作
        _ASPSTOP = StringUtils.rightPad(tempVar.getASPSTOP(),1,' '); //停卡理由
        _FILLER1 = tempVar.getFILLER1();

        return _NUMBER + _CARD_NO + _ACCOUNT + _CD1 + _CD2 + _CD3 + _TXDATE + _KINBR + _WSNO + _TXSEQ + _TBSDY + _MODE + _TXCD + _RC + _ASPSTAN + _CARDNO + _ACTNO + _SEQNO + _VAILDDT + _BIRTHDAY + _ACTION + _ASPSTOP + _FILLER1;

    }

    @Override
    public void toGeneral(CreditGeneral general) throws Exception {

    }
}
