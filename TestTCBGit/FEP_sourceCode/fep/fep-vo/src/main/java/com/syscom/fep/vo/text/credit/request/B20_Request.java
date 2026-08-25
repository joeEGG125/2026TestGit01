package com.syscom.fep.vo.text.credit.request;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.credit.CreditGeneral;
import com.syscom.fep.vo.text.credit.CreditGeneralRequest;
import com.syscom.fep.vo.text.credit.CreditTextBase;
import org.apache.commons.lang3.StringUtils;

public class B20_Request extends CreditTextBase {
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

    //手續費
    @Field(length = 8, optional = true)
    private String _FEE = "";

    //信用卡授權碼
    @Field(length = 6, optional = true)
    private String _AUTHCD = "";

    //身分證字號
    @Field(length = 508, optional = true)
    private String _ID = "";

    //PIN BLOCK
    @Field(length = 16, optional = true)
    private String _FPB = "";

    //錢卡第三軌資料
    @Field(length = 104, optional = true)
    private String _TRACK3 = "";

    //代理ATM代號
    @Field(length = 8, optional = true)
    private String _ATMID = "";

    //財金序號
    @Field(length = 10, optional = true)
    private String _STAN = "";

    //正負號
    @Field(length = 4, optional = true)
    private String _AVBAL_S = "";

    //可用餘額
    @Field(length = 13, optional = true)
    private String _AVBAL = "";

    //正負號
    @Field(length = 1, optional = true)
    private String _ACTBAL_S = "";

    //信用卡額度
    @Field(length = 13, optional = true)
    private String _ACTBAL = "";

    //正負號3
    @Field(length = 1, optional = true)
    private String _AVBAL3_S = "";

    //可用餘額3
    @Field(length = 13, optional = true)
    private String _AVBAL3 = "";

    //正負號4
    @Field(length = 1, optional = true)
    private String _AVBAL4_S = "";

    //可用餘額4
    @Field(length = 13, optional = true)
    private String _AVBAL4 = "";

    //訊息押碼
    @Field(length = 8, optional = true)
    private String _MAC = "";

    //PINBLOCK押碼方式
    @Field(length = 1, optional = true)
    private String _TripleDES = "";

    //
    @Field(length = 242, optional = true)
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

     <remark>CAV / CFV：VISA預借現金	CAM / CFM：Master預借現金	CAA / CFA：AE預借現金	CAJ / CFJ：JCB預借現金</remark>
     */
    public String getAPID() {
        return _APID;
    }
    public void setAPID(String value) {
        _APID = value;
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
     手續費

     <remark>空白</remark>
     */
    public String getFEE() {
        return _FEE;
    }
    public void setFEE(String value) {
        _FEE = value;
    }

    /**
     信用卡授權碼

     <remark>REQ時空白收到+REP時，CON會有值</remark>
     */
    public String getAUTHCD() {
        return _AUTHCD;
    }
    public void setAUTHCD(String value) {
        _AUTHCD = value;
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
     PIN BLOCK

     <remark>CONFIRM時，放置原交易日期及序號</remark>
     */
    public String getFPB() {
        return _FPB;
    }
    public void setFPB(String value) {
        _FPB = value;
    }

    /**
     錢卡第三軌資料

     <remark></remark>
     */
    public String getTRACK3() {
        return _TRACK3;
    }
    public void setTRACK3(String value) {
        _TRACK3 = value;
    }

    /**
     代理ATM代號

     <remark></remark>
     */
    public String getATMID() {
        return _ATMID;
    }
    public void setATMID(String value) {
        _ATMID = value;
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
     正負號

     <remark></remark>
     */
    public String getAvbalS() {
        return _AVBAL_S;
    }
    public void setAvbalS(String value) {
        _AVBAL_S = value;
    }

    /**
     可用餘額

     <remark>SIGN LEADING SEPARATE</remark>
     */
    public String getAVBAL() {
        return _AVBAL;
    }
    public void setAVBAL(String value) {
        _AVBAL = value;
    }

    /**
     正負號

     <remark></remark>
     */
    public String getActbalS() {
        return _ACTBAL_S;
    }
    public void setActbalS(String value) {
        _ACTBAL_S = value;
    }

    /**
     信用卡額度

     <remark>SIGN LEADING SEPARATE</remark>
     */
    public String getACTBAL() {
        return _ACTBAL;
    }
    public void setACTBAL(String value) {
        _ACTBAL = value;
    }

    /**
     正負號3

     <remark></remark>
     */
    public String getAvbal3S() {
        return _AVBAL3_S;
    }
    public void setAvbal3S(String value) {
        _AVBAL3_S = value;
    }

    /**
     可用餘額3

     <remark></remark>
     */
    public String getAVBAL3() {
        return _AVBAL3;
    }
    public void setAVBAL3(String value) {
        _AVBAL3 = value;
    }

    /**
     正負號4

     <remark></remark>
     */
    public String getAvbal4S() {
        return _AVBAL4_S;
    }
    public void setAvbal4S(String value) {
        _AVBAL4_S = value;
    }

    /**
     可用餘額4

     <remark></remark>
     */
    public String getAVBAL4() {
        return _AVBAL4;
    }
    public void setAVBAL4(String value) {
        _AVBAL4 = value;
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
        return null;
    }

    @Override
    public String makeMessageFromGeneral(CreditGeneral general) throws Exception {
        CreditGeneralRequest tempVar = general.getRequest();
        _NUMBER = StringUtils.rightPad(tempVar.getNUMBER(),6, ' '); //交易序號
        _CARD_NO = StringUtils.rightPad(tempVar.getCardNo(),16, ' '); //卡號
        _ACCOUNT = StringUtils.rightPad(tempVar.getACCOUNT(),16, ' '); //金融卡帳號
        _CD1 = StringUtils.rightPad(tempVar.getCD1(),1, ' '); //訊息型態
        _CD2 = StringUtils.leftPad(tempVar.getCD2(),1, '0'); //來源別
        _CD3 = StringUtils.leftPad(tempVar.getCD3(),1, '0'); //MSG FLOW
        _TXDATE = StringUtils.leftPad(tempVar.getTXDATE(),8, '0'); //系統日
        _KINBR = StringUtils.leftPad(tempVar.getKINBR(),3, '0'); //輸入行
        _WSNO = StringUtils.leftPad(tempVar.getWSNO(),2, '0'); //櫃台機號
        _TXSEQ = StringUtils.leftPad(tempVar.getTXSEQ(),5, '0'); //交易傳輸編號
        _TBSDY = StringUtils.leftPad(tempVar.getTBSDY(),8, '0'); //營業日
        _MODE =StringUtils.leftPad( tempVar.getMODE(),1, '0'); //MODE
        _TXCD = StringUtils.rightPad(tempVar.getTXCD(),3, ' '); //交易代號
        _RC = StringUtils.rightPad(tempVar.getRC(),4, ' '); //拒絕代碼
        _ASPSTAN = StringUtils.leftPad(tempVar.getASPSTAN(),6, '0'); //信用卡交易序號
        _CARDNO = StringUtils.rightPad(tempVar.getCARDNO(),16, ' '); //信用卡號
        _ACTNO = StringUtils.rightPad(tempVar.getACTNO(),14, ' '); //金融卡號
        _SEQNO = StringUtils.leftPad(tempVar.getSEQNO(),2, '0'); //金融卡序號
        _APID = StringUtils.rightPad(tempVar.getAPID(),3, ' '); //交易種類
        _BANK_W = StringUtils.leftPad(tempVar.getBankW(),7, '0'); //轉出銀行別
        _ACTNO_W = StringUtils.leftPad(tempVar.getActnoW(),16, '0'); //轉出帳號
        _BANK_D =StringUtils.leftPad( tempVar.getBankD(),7, '0'); //轉入銀行別
        _ACTNO_D = StringUtils.leftPad(tempVar.getActnoD(),16, '0'); //轉入帳號
        _TXAMT = StringUtils.leftPad(String.valueOf(Math.floor(tempVar.getTXAMT().doubleValue() * (10 ^ 0))), 10); //交易金額
        _FEE = StringUtils.leftPad(String.valueOf(Math.floor(tempVar.getFEE().doubleValue() * (10 ^ 0))), 8); //手續費
        _AUTHCD = StringUtils.leftPad(tempVar.getAUTHCD(),6, '0'); //信用卡授權碼
        _ID = StringUtils.rightPad(tempVar.getID(),11, ' '); //身分證字號
        _FPB = StringUtils.rightPad(tempVar.getFPB(),16, ' '); //PIN BLOCK
        _TRACK3 = StringUtils.rightPad(tempVar.getTRACK3(),104, ' '); //錢卡第三軌資料
        _ATMID = StringUtils.rightPad(tempVar.getATMID(),8,' '); //代理ATM代號
        _STAN = StringUtils.rightPad(tempVar.getSTAN(),10,' '); //財金序號
        _AVBAL_S = StringUtils.rightPad(tempVar.getAvbalS(),1, ' '); //正負號
        _AVBAL = StringUtils.leftPad(String.valueOf(Math.floor(tempVar.getAVBAL().doubleValue() * (10 ^ 2))), 13); //可用餘額
        _ACTBAL_S = StringUtils.rightPad(tempVar.getActbalS(),1, ' '); //正負號
        _ACTBAL = StringUtils.leftPad(String.valueOf(Math.floor(tempVar.getACTBAL().doubleValue() * (10 ^ 2))), 13); //信用卡額度
        _AVBAL3_S = StringUtils.rightPad(tempVar.getAvbal3S(),1,' '); //正負號3
        _AVBAL3 = StringUtils.leftPad(String.valueOf(Math.floor(tempVar.getAVBAL3().doubleValue() * (10 ^ 2))), 13); //可用餘額3
        _AVBAL4_S = StringUtils.rightPad(tempVar.getAvbal4S(),1,' '); //正負號4
        _AVBAL4 = StringUtils.leftPad(String.valueOf(Math.floor(tempVar.getAVBAL4().doubleValue() * (10 ^ 2))), 13); //可用餘額4
        _MAC = StringUtils.rightPad(tempVar.getMAC(),8, ' '); //訊息押碼
        _TripleDES = StringUtils.rightPad(tempVar.getTripleDES(),1, ' '); //PINBLOCK押碼方式
        _FILLER1 = tempVar.getFILLER1();

        return _NUMBER + _CARD_NO + _ACCOUNT + _CD1 + _CD2 + _CD3 + _TXDATE + _KINBR + _WSNO + _TXSEQ + _TBSDY + _MODE + _TXCD + _RC + _ASPSTAN + _CARDNO + _ACTNO + _SEQNO + _APID + _BANK_W + _ACTNO_W + _BANK_D + _ACTNO_D + _TXAMT + _FEE + _AUTHCD + _ID + _FPB + _TRACK3 + _ATMID + _STAN + _AVBAL_S + _AVBAL + _ACTBAL_S + _ACTBAL + _AVBAL3_S + _AVBAL3 + _AVBAL4_S + _AVBAL4 + _MAC + _TripleDES + _FILLER1;

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
