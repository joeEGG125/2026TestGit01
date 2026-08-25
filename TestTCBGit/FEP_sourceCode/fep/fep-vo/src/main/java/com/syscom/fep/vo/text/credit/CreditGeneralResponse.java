package com.syscom.fep.vo.text.credit;

import java.math.BigDecimal;

import com.syscom.fep.vo.text.FEPRsHeader.FEPRsHeaderOverride;

public class CreditGeneralResponse {
    private String _ChlEJNo = "";
    private String _EJNo = "";
    private String _RqTime = "";
    private String _RsTime = "";
    private FEPRsHeaderOverride[] _Overrides;
    private String _RsStat_RsStateCode = "";
    private String _RsStat_RsStateCode_type = "";
    private String _RsStat_Desc = "";
    private String _T24Application = "";
    private String _T24Version = "";
    private String _T24Operation = "";
    private String _T24RspCode = "";

    private String _VALIDDT = "";
    private String _BIRTHDAY = "";
    private String _ACTION = "";
    private String _ASPSTOP = "";
    private String _FILLER1 = "";
    private String _FILLER2 = "";
    private String _TRACK2 = "";
    private String _TRACK1 = "";
    private String _WTRK2 = "";
    private String _WTRK1 = "";
    private String _FILLER3 = "";
    private String _FILLER4 = "";
    private BigDecimal _CURRBAL = new BigDecimal(0);
    private BigDecimal _DUEAMT = new BigDecimal(0);
    private String _BILLDATE = "";
    private String _LTXDT = "";
    private BigDecimal _CARD_LIMIT = new BigDecimal(0);
    private String _CARD_SIGN = "";
    private BigDecimal _CARD_BAL = new BigDecimal(0);
    private BigDecimal _CA_LIMIT = new BigDecimal(0);
    private String _CA_SIGN = "";
    private BigDecimal _CA_BAL = new BigDecimal(0);
    private String _FILLER5 = "";
    private String _ASP = "";
    private String _BSP = "";
    private String _APID = "";
    private String _BANK_W = "";
    private String _ACTNO_W = "";
    private String _BANK_D = "";
    private String _ACTNO_D = "";
    private BigDecimal _TXAMT = new BigDecimal(0);
    private BigDecimal _FEE = new BigDecimal(0);
    private String _AUTHCD = "";
    private String _ID = "";
    private String _FPB = "";
    private String _TRACK3 = "";
    private String _AVBAL_S = "";
    private BigDecimal _AVBAL = new BigDecimal(0);
    private String _ACTBAL_S = "";
    private BigDecimal _ACTBAL = new BigDecimal(0);
    private String _CLASS = "";
    private String _NOTICENO = "";
    private String _ATMID = "";
    private String _STAN = "";
    private String _MEMNO = "";
    private String _MAC = "";
    private String _ICMARK = "";
    private String _POSENTRY = "";
    private String _ACT_IDX = "";
    private String _ACTBK1 = "";
    private String _ACTNO1 = "";
    private String _ACTBK2 = "";
    private String _ACTNO2 = "";
    private String _ACTBK3 = "";
    private String _ACTNO3 = "";
    private String _ACTBK4 = "";
    private String _ACTNO4 = "";
    private String _ACTBK5 = "";
    private String _ACTNO5 = "";
    private String _ACTBK6 = "";
    private String _ACTNO6 = "";
    private String _ACTBK7 = "";
    private String _ACTNO7 = "";
    private String _ACTBK8 = "";
    private String _ACTNO8 = "";
    private String _ACTBK9 = "";
    private String _ACTNO9 = "";
    private String _ACTBK10 = "";
    private String _ACTNO10 = "";
    private String _ACTBK11 = "";
    private String _ACTNO11 = "";
    private String _ACTBK12 = "";
    private String _ACTNO12 = "";
    private String _ACTBK13 = "";
    private String _ACTNO13 = "";
    private String _ACTBK14 = "";
    private String _ACTNO14 = "";
    private String _ACTBK15 = "";
    private String _ACTNO15 = "";
    private String _PKEY = "";
    private String _PSYNC = "";
    private String _CKEY = "";
    private String _CSYNC = "";
    private String _AVBAL3_S = "";
    private BigDecimal _AVBAL3 = new BigDecimal(0);
    private String _AVBAL4_S = "";
    private BigDecimal _AVBAL4 = new BigDecimal(0);
    private String _TripleDES = "";
    private String _CHKID = "";
    private String _SCARDNO = "";
    private String _GIFTCARD = "";
    private String _REFERENCE = "";
    private String _AUTOLOAD = "";
    private String _AGEFLG = "";
    private String _FCTIMES = ""; //二次記名
    private String _FCACTNO = ""; //外幣帳號
    private String _NUMBER = "";
    private String _CARD_NO = "";
    private String _ACCOUNT = "";
    private String _CD1 = "";
    private String _CD2 = "";
    private String _CD3 = "";
    private String _TXDATE = "";
    private String _TXDATE2 = ""; //add by 榮升 2015/02/11 增加IPIN用欄位
    private String _TXTIME = ""; //add by 榮升 2015/02/11 增加IPIN用欄位
    private String _PIN = ""; //add by 榮升 2015/02/11 增加IPIN用欄位
    private String _KINBR = "";
    private String _WSNO = "";
    private String _TXSEQ = "";
    private String _TBSDY = "";
    private String _MODE = "";
    private String _TXCD = "";
    private String _RC = "";
    private String _ASPSTAN = "";
    private String _CARDNO = "";
    private String _ACTNO = "";
    private String _SEQNO = "";
    private String _IN_TEXT = "";
    private String _ASPSTON = "";
    private String _KEYID = "";
    private String _TXDATE_O = "";
    private String _TXTNO_O = "";

    public final String getChlEJNo()
    {
        return _ChlEJNo;
    }
    public final void setChlEJNo(String value)
    {
        _ChlEJNo = value;
    }

    public final String getEJNo()
    {
        return _EJNo;
    }
    public final void setEJNo(String value)
    {
        _EJNo = value;
    }

    public final String getRqTime()
    {
        return _RqTime;
    }
    public final void setRqTime(String value)
    {
        _RqTime = value;
    }

    public final String getRsTime()
    {
        return _RsTime;
    }
    public final void setRsTime(String value)
    {
        _RsTime = value;
    }

    //VB TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: <System.Xml.Serialization.XmlArrayItemAttribute("Override", IsNullable:=True)> Public Property Overrides() As FEPRsHeaderOverride()
    public final FEPRsHeaderOverride[] getOverrides()
    {
        return _Overrides;
    }
    public final void setOverrides(FEPRsHeaderOverride[] value)
    {
        _Overrides = value;
    }

    public final String getRsStatRsStateCode()
    {
        return _RsStat_RsStateCode;
    }
    public final void setRsStatRsStateCode(String value)
    {
        _RsStat_RsStateCode = value;
    }

    public final String getRsStatRsStateCodeType()
    {
        return _RsStat_RsStateCode_type;
    }
    public final void setRsStatRsStateCodeType(String value)
    {
        _RsStat_RsStateCode_type = value;
    }

    public final String getRsStatDesc()
    {
        return _RsStat_Desc;
    }
    public final void setRsStatDesc(String value)
    {
        _RsStat_Desc = value;
    }

    public final String getT24Application()
    {
        return _T24Application;
    }
    public final void setT24Application(String value)
    {
        _T24Application = value;
    }

    public final String getT24Version()
    {
        return _T24Version;
    }
    public final void setT24Version(String value)
    {
        _T24Version = value;
    }

    public final String getT24Operation()
    {
        return _T24Operation;
    }
    public final void setT24Operation(String value)
    {
        _T24Operation = value;
    }

    public final String getT24RspCode()
    {
        return _T24RspCode;
    }
    public final void setT24RspCode(String value)
    {
        _T24RspCode = value;
    }


    /**
     有效月年

     <remark></remark>
     */
    public final String getVALIDDT()
    {
        return _VALIDDT;
    }
    public final void setVALIDDT(String value)
    {
        _VALIDDT = value;
    }

    /**
     出生年月日

     <remark></remark>
     */
    public final String getBIRTHDAY()
    {
        return _BIRTHDAY;
    }
    public final void setBIRTHDAY(String value)
    {
        _BIRTHDAY = value;
    }

    /**
     執行動作

     <remark>1:開卡</remark>
     */
    public final String getACTION()
    {
        return _ACTION;
    }
    public final void setACTION(String value)
    {
        _ACTION = value;
    }

    /**
     停卡理由

     <remark>空白</remark>
     */
    public final String getASPSTOP()
    {
        return _ASPSTOP;
    }
    public final void setASPSTOP(String value)
    {
        _ASPSTOP = value;
    }

    /**


     <remark></remark>
     */
    public final String getFILLER1()
    {
        return _FILLER1;
    }
    public final void setFILLER1(String value)
    {
        _FILLER1 = value;
    }

    /**


     <remark></remark>
     */
    public final String getFILLER2()
    {
        return _FILLER2;
    }
    public final void setFILLER2(String value)
    {
        _FILLER2 = value;
    }

    /**
     磁條二軌資料

     <remark></remark>
     */
    public final String getTRACK2()
    {
        return _TRACK2;
    }
    public final void setTRACK2(String value)
    {
        _TRACK2 = value;
    }

    /**
     磁條一軌資料

     <remark></remark>
     */
    public final String getTRACK1()
    {
        return _TRACK1;
    }
    public final void setTRACK1(String value)
    {
        _TRACK1 = value;
    }

    /**
     回寫二軌記號

     <remark>1:要回寫磁軌	0:不回寫	</remark>
     */
    public final String getWTRK2()
    {
        return _WTRK2;
    }
    public final void setWTRK2(String value)
    {
        _WTRK2 = value;
    }

    /**
     回寫一軌記號

     <remark>1:要回寫磁軌	0:不回寫</remark>
     */
    public final String getWTRK1()
    {
        return _WTRK1;
    }
    public final void setWTRK1(String value)
    {
        _WTRK1 = value;
    }

    /**


     <remark></remark>
     */
    public final String getFILLER3()
    {
        return _FILLER3;
    }
    public final void setFILLER3(String value)
    {
        _FILLER3 = value;
    }

    /**


     <remark></remark>
     */
    public final String getFILLER4()
    {
        return _FILLER4;
    }
    public final void setFILLER4(String value)
    {
        _FILLER4 = value;
    }

    /**
     應繳金額

     <remark></remark>
     */
    public final BigDecimal getCURRBAL()
    {
        return _CURRBAL;
    }
    public final void setCURRBAL(BigDecimal value)
    {
        _CURRBAL = value;
    }

    /**
     最低應繳金額

     <remark></remark>
     */
    public final BigDecimal getDUEAMT()
    {
        return _DUEAMT;
    }
    public final void setDUEAMT(BigDecimal value)
    {
        _DUEAMT = value;
    }

    /**
     結帳日

     <remark></remark>
     */
    public final String getBILLDATE()
    {
        return _BILLDATE;
    }
    public final void setBILLDATE(String value)
    {
        _BILLDATE = value;
    }

    /**
     最近一次繳款日

     <remark></remark>
     */
    public final String getLTXDT()
    {
        return _LTXDT;
    }
    public final void setLTXDT(String value)
    {
        _LTXDT = value;
    }

    /**
     卡片信用額度

     <remark></remark>
     */
    public final BigDecimal getCardLimit()
    {
        return _CARD_LIMIT;
    }
    public final void setCardLimit(BigDecimal value)
    {
        _CARD_LIMIT = value;
    }

    /**
     正負號

     <remark></remark>
     */
    public final String getCardSign()
    {
        return _CARD_SIGN;
    }
    public final void setCardSign(String value)
    {
        _CARD_SIGN = value;
    }

    /**
     卡片可用餘額

     <remark></remark>
     */
    public final BigDecimal getCardBal()
    {
        return _CARD_BAL;
    }
    public final void setCardBal(BigDecimal value)
    {
        _CARD_BAL = value;
    }

    /**
     預借現金額度

     <remark></remark>
     */
    public final BigDecimal getCaLimit()
    {
        return _CA_LIMIT;
    }
    public final void setCaLimit(BigDecimal value)
    {
        _CA_LIMIT = value;
    }

    /**
     正負號

     <remark></remark>
     */
    public final String getCaSign()
    {
        return _CA_SIGN;
    }
    public final void setCaSign(String value)
    {
        _CA_SIGN = value;
    }

    /**
     預現可用餘額

     <remark></remark>
     */
    public final BigDecimal getCaBal()
    {
        return _CA_BAL;
    }
    public final void setCaBal(BigDecimal value)
    {
        _CA_BAL = value;
    }

    /**


     <remark></remark>
     */
    public final String getFILLER5()
    {
        return _FILLER5;
    }
    public final void setFILLER5(String value)
    {
        _FILLER5 = value;
    }

    /**
     信用卡主機狀態

     <remark>1:啟動	0:停止	</remark>
     */
    public final String getASP()
    {
        return _ASP;
    }
    public final void setASP(String value)
    {
        _ASP = value;
    }

    /**
     銀行主機狀態

     <remark>1:啟動	0:停止	</remark>
     */
    public final String getBSP()
    {
        return _BSP;
    }
    public final void setBSP(String value)
    {
        _BSP = value;
    }

    /**
     交易種類

     <remark>CAV / CFV: VISA預借現金	CAM / CFM: Master預借現金	CAA / CFA: AE預借現金	CAJ / CFJ: JCB預借現金</remark>
     */
    public final String getAPID()
    {
        return _APID;
    }
    public final void setAPID(String value)
    {
        _APID = value;
    }

    /**
     轉出銀行別

     <remark></remark>
     */
    public final String getBankW()
    {
        return _BANK_W;
    }
    public final void setBankW(String value)
    {
        _BANK_W = value;
    }

    /**
     轉出帳號

     <remark></remark>
     */
    public final String getActnoW()
    {
        return _ACTNO_W;
    }
    public final void setActnoW(String value)
    {
        _ACTNO_W = value;
    }

    /**
     轉入銀行別

     <remark></remark>
     */
    public final String getBankD()
    {
        return _BANK_D;
    }
    public final void setBankD(String value)
    {
        _BANK_D = value;
    }

    /**
     轉入帳號

     <remark></remark>
     */
    public final String getActnoD()
    {
        return _ACTNO_D;
    }
    public final void setActnoD(String value)
    {
        _ACTNO_D = value;
    }

    /**
     交易金額

     <remark></remark>
     */
    public final BigDecimal getTXAMT()
    {
        return _TXAMT;
    }
    public final void setTXAMT(BigDecimal value)
    {
        _TXAMT = value;
    }

    /**
     手續費

     <remark>空白</remark>
     */
    public final BigDecimal getFEE()
    {
        return _FEE;
    }
    public final void setFEE(BigDecimal value)
    {
        _FEE = value;
    }

    /**
     信用卡授權碼

     <remark></remark>
     */
    public final String getAUTHCD()
    {
        return _AUTHCD;
    }
    public final void setAUTHCD(String value)
    {
        _AUTHCD = value;
    }

    /**
     身份證字號

     <remark></remark>
     */
    public final String getID()
    {
        return _ID;
    }
    public final void setID(String value)
    {
        _ID = value;
    }

    /**
     PIN BLOCK

     <remark></remark>
     */
    public final String getFPB()
    {
        return _FPB;
    }
    public final void setFPB(String value)
    {
        _FPB = value;
    }

    /**
     第三軌資料

     <remark></remark>
     */
    public final String getTRACK3()
    {
        return _TRACK3;
    }
    public final void setTRACK3(String value)
    {
        _TRACK3 = value;
    }

    /**
     正負號

     <remark></remark>
     */
    public final String getAvbalS()
    {
        return _AVBAL_S;
    }
    public final void setAvbalS(String value)
    {
        _AVBAL_S = value;
    }

    /**
     可用額度

     <remark>SIGN LEADING SEPARATE</remark>
     */
    public final BigDecimal getAVBAL()
    {
        return _AVBAL;
    }
    public final void setAVBAL(BigDecimal value)
    {
        _AVBAL = value;
    }

    /**
     正負號

     <remark></remark>
     */
    public final String getActbalS()
    {
        return _ACTBAL_S;
    }
    public final void setActbalS(String value)
    {
        _ACTBAL_S = value;
    }

    /**
     信用卡額度

     <remark>SIGN LEADING SEPARATE</remark>
     */
    public final BigDecimal getACTBAL()
    {
        return _ACTBAL;
    }
    public final void setACTBAL(BigDecimal value)
    {
        _ACTBAL = value;
    }

    /**
     繳款類別

     <remark></remark>
     */
    public final String getCLASS()
    {
        return _CLASS;
    }
    public final void setCLASS(String value)
    {
        _CLASS = value;
    }

    /**
     銷帳編號

     <remark></remark>
     */
    public final String getNOTICENO()
    {
        return _NOTICENO;
    }
    public final void setNOTICENO(String value)
    {
        _NOTICENO = value;
    }

    /**
     代理ATM代號

     <remark></remark>
     */
    public final String getATMID()
    {
        return _ATMID;
    }
    public final void setATMID(String value)
    {
        _ATMID = value;
    }

    /**
     財金序號

     <remark></remark>
     */
    public final String getSTAN()
    {
        return _STAN;
    }
    public final void setSTAN(String value)
    {
        _STAN = value;
    }

    /**
     備註

     <remark></remark>
     */
    public final String getMEMNO()
    {
        return _MEMNO;
    }
    public final void setMEMNO(String value)
    {
        _MEMNO = value;
    }

    /**
     訊息押碼

     <remark></remark>
     */
    public final String getMAC()
    {
        return _MAC;
    }
    public final void setMAC(String value)
    {
        _MAC = value;
    }

    /**
     晶片卡REMARK欄位資料

     <remark></remark>
     */
    public final String getICMARK()
    {
        return _ICMARK;
    }
    public final void setICMARK(String value)
    {
        _ICMARK = value;
    }

    /**
     POS ENTRY MODE

     <remark>中間兩碼80為 fallback交易 ; 	若欄位為空白,表示尚未升級EMV機台</remark>
     */
    public final String getPOSENTRY()
    {
        return _POSENTRY;
    }
    public final void setPOSENTRY(String value)
    {
        _POSENTRY = value;
    }

    /**
     帳號序號

     <remark>IDX=0表無約定帳號</remark>
     */
    public final String getActIdx()
    {
        return _ACT_IDX;
    }
    public final void setActIdx(String value)
    {
        _ACT_IDX = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public final String getACTBK1()
    {
        return _ACTBK1;
    }
    public final void setACTBK1(String value)
    {
        _ACTBK1 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public final String getACTNO1()
    {
        return _ACTNO1;
    }
    public final void setACTNO1(String value)
    {
        _ACTNO1 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public final String getACTBK2()
    {
        return _ACTBK2;
    }
    public final void setACTBK2(String value)
    {
        _ACTBK2 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public final String getACTNO2()
    {
        return _ACTNO2;
    }
    public final void setACTNO2(String value)
    {
        _ACTNO2 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public final String getACTBK3()
    {
        return _ACTBK3;
    }
    public final void setACTBK3(String value)
    {
        _ACTBK3 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public final String getACTNO3()
    {
        return _ACTNO3;
    }
    public final void setACTNO3(String value)
    {
        _ACTNO3 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public final String getACTBK4()
    {
        return _ACTBK4;
    }
    public final void setACTBK4(String value)
    {
        _ACTBK4 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public final String getACTNO4()
    {
        return _ACTNO4;
    }
    public final void setACTNO4(String value)
    {
        _ACTNO4 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public final String getACTBK5()
    {
        return _ACTBK5;
    }
    public final void setACTBK5(String value)
    {
        _ACTBK5 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public final String getACTNO5()
    {
        return _ACTNO5;
    }
    public final void setACTNO5(String value)
    {
        _ACTNO5 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public final String getACTBK6()
    {
        return _ACTBK6;
    }
    public final void setACTBK6(String value)
    {
        _ACTBK6 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public final String getACTNO6()
    {
        return _ACTNO6;
    }
    public final void setACTNO6(String value)
    {
        _ACTNO6 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public final String getACTBK7()
    {
        return _ACTBK7;
    }
    public final void setACTBK7(String value)
    {
        _ACTBK7 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public final String getACTNO7()
    {
        return _ACTNO7;
    }
    public final void setACTNO7(String value)
    {
        _ACTNO7 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public final String getACTBK8()
    {
        return _ACTBK8;
    }
    public final void setACTBK8(String value)
    {
        _ACTBK8 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public final String getACTNO8()
    {
        return _ACTNO8;
    }
    public final void setACTNO8(String value)
    {
        _ACTNO8 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public final String getACTBK9()
    {
        return _ACTBK9;
    }
    public final void setACTBK9(String value)
    {
        _ACTBK9 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public final String getACTNO9()
    {
        return _ACTNO9;
    }
    public final void setACTNO9(String value)
    {
        _ACTNO9 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public final String getACTBK10()
    {
        return _ACTBK10;
    }
    public final void setACTBK10(String value)
    {
        _ACTBK10 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public final String getACTNO10()
    {
        return _ACTNO10;
    }
    public final void setACTNO10(String value)
    {
        _ACTNO10 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public final String getACTBK11()
    {
        return _ACTBK11;
    }
    public final void setACTBK11(String value)
    {
        _ACTBK11 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public final String getACTNO11()
    {
        return _ACTNO11;
    }
    public final void setACTNO11(String value)
    {
        _ACTNO11 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public final String getACTBK12()
    {
        return _ACTBK12;
    }
    public final void setACTBK12(String value)
    {
        _ACTBK12 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public final String getACTNO12()
    {
        return _ACTNO12;
    }
    public final void setACTNO12(String value)
    {
        _ACTNO12 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public final String getACTBK13()
    {
        return _ACTBK13;
    }
    public final void setACTBK13(String value)
    {
        _ACTBK13 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public final String getACTNO13()
    {
        return _ACTNO13;
    }
    public final void setACTNO13(String value)
    {
        _ACTNO13 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public final String getACTBK14()
    {
        return _ACTBK14;
    }
    public final void setACTBK14(String value)
    {
        _ACTBK14 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public final String getACTNO14()
    {
        return _ACTNO14;
    }
    public final void setACTNO14(String value)
    {
        _ACTNO14 = value;
    }

    /**
     約定轉入銀行別

     <remark></remark>
     */
    public final String getACTBK15()
    {
        return _ACTBK15;
    }
    public final void setACTBK15(String value)
    {
        _ACTBK15 = value;
    }

    /**
     約定轉入帳號

     <remark></remark>
     */
    public final String getACTNO15()
    {
        return _ACTNO15;
    }
    public final void setACTNO15(String value)
    {
        _ACTNO15 = value;
    }

    /**
     NEW PPK

     <remark></remark>
     */
    public final String getPKEY()
    {
        return _PKEY;
    }
    public final void setPKEY(String value)
    {
        _PKEY = value;
    }

    /**
     PPK SYNC

     <remark></remark>
     */
    public final String getPSYNC()
    {
        return _PSYNC;
    }
    public final void setPSYNC(String value)
    {
        _PSYNC = value;
    }

    /**
     NEW PMK

     <remark></remark>
     */
    public final String getCKEY()
    {
        return _CKEY;
    }
    public final void setCKEY(String value)
    {
        _CKEY = value;
    }

    /**
     PMK SYNC

     <remark></remark>
     */
    public final String getCSYNC()
    {
        return _CSYNC;
    }
    public final void setCSYNC(String value)
    {
        _CSYNC = value;
    }

    /**
     正負號

     <remark></remark>
     */
    public final String getAvbal3S()
    {
        return _AVBAL3_S;
    }
    public final void setAvbal3S(String value)
    {
        _AVBAL3_S = value;
    }

    /**
     可用餘額3

     <remark></remark>
     */
    public final BigDecimal getAVBAL3()
    {
        return _AVBAL3;
    }
    public final void setAVBAL3(BigDecimal value)
    {
        _AVBAL3 = value;
    }

    /**
     正負號

     <remark></remark>
     */
    public final String getAvbal4S()
    {
        return _AVBAL4_S;
    }
    public final void setAvbal4S(String value)
    {
        _AVBAL4_S = value;
    }

    /**
     可用餘額4

     <remark></remark>
     */
    public final BigDecimal getAVBAL4()
    {
        return _AVBAL4;
    }
    public final void setAVBAL4(BigDecimal value)
    {
        _AVBAL4 = value;
    }

    /**
     押碼方式

     <remark>1:3DES	0:SINGLE DES	</remark>
     */
    public final String getTripleDES()
    {
        return _TripleDES;
    }
    public final void setTripleDES(String value)
    {
        _TripleDES = value;
    }

    /**
     是否檢查ID

     <remark>0:不檢查	1:檢查</remark>
     */
    public final String getCHKID()
    {
        return _CHKID;
    }
    public final void setCHKID(String value)
    {
        _CHKID = value;
    }

    /**
     卡片庫存流水號

     <remark></remark>
     */
    public final String getSCARDNO()
    {
        return _SCARDNO;
    }
    public final void setSCARDNO(String value)
    {
        _SCARDNO = value;
    }

    /**
     原GIFT COMBO卡號

     <remark>若無帶空白</remark>
     */
    public final String getGIFTCARD()
    {
        return _GIFTCARD;
    }
    public final void setGIFTCARD(String value)
    {
        _GIFTCARD = value;
    }

    /**
     推薦人

     <remark></remark>
     */
    public final String getREFERENCE()
    {
        return _REFERENCE;
    }
    public final void setREFERENCE(String value)
    {
        _REFERENCE = value;
    }

    /**
     自動加值記號

     <remark>0:關閉 1:開啟</remark>
     */
    public final String getAUTOLOAD()
    {
        return _AUTOLOAD;
    }
    public final void setAUTOLOAD(String value)
    {
        _AUTOLOAD = value;
    }

    /**
     年齡註記

     <remark>0:小於15歲 1:大於等於15歲</remark>
     */
    public final String getAGEFLG()
    {
        return _AGEFLG;
    }
    public final void setAGEFLG(String value)
    {
        _AGEFLG = value;
    }

    /**
     二次記名

     <remark>A:第一次，B:第二次</remark>
     */
    public final String getFCTIMES()
    {
        return _FCTIMES;
    }
    public final void setFCTIMES(String value)
    {
        _FCTIMES = value;
    }

    /**
     外幣帳號

     <remark>綁定外幣組存帳號</remark>
     */
    public final String getFCACTNO()
    {
        return _FCACTNO;
    }
    public final void setFCACTNO(String value)
    {
        _FCACTNO = value;
    }

    /**
     交易序號

     <remark>空白 (MQ HEADER)</remark>
     */
    public final String getNUMBER()
    {
        return _NUMBER;
    }
    public final void setNUMBER(String value)
    {
        _NUMBER = value;
    }

    /**
     卡號

     <remark>空白 (MQ HEADER)</remark>
     */
    public final String getCardNo()
    {
        return _CARD_NO;
    }
    public final void setCardNo(String value)
    {
        _CARD_NO = value;
    }

    /**
     金融卡帳號

     <remark>空白 (MQ HEADER)</remark>
     */
    public final String getACCOUNT()
    {
        return _ACCOUNT;
    }
    public final void setACCOUNT(String value)
    {
        _ACCOUNT = value;
    }

    /**
     訊息型態

     <remark>A</remark>
     */
    public final String getCD1()
    {
        return _CD1;
    }
    public final void setCD1(String value)
    {
        _CD1 = value;
    }

    /**
     來源別

     <remark>2:REQ FROM ASP</remark>
     */
    public final String getCD2()
    {
        return _CD2;
    }
    public final void setCD2(String value)
    {
        _CD2 = value;
    }

    /**
     MSG FLOW

     <remark>1:REQUEST MSG</remark>
     */
    public final String getCD3()
    {
        return _CD3;
    }
    public final void setCD3(String value)
    {
        _CD3 = value;
    }

    /**
     系統日

     <remark></remark>
     */
    public final String getTXDATE()
    {
        return _TXDATE;
    }
    public final void setTXDATE(String value)
    {
        _TXDATE = value;
    }

    //add by 榮升 2015/02/11 增加IPIN用欄位
    /**
     交易日期

     <remark></remark>
     */
    public final String getTXDATE2()
    {
        return _TXDATE2;
    }
    public final void setTXDATE2(String value)
    {
        _TXDATE2 = value;
    }

    //add by 榮升 2015/02/11 增加IPIN用欄位
    /**
     交易時間

     <remark></remark>
     */
    public final String getTXTIME()
    {
        return _TXTIME;
    }
    public final void setTXTIME(String value)
    {
        _TXTIME = value;
    }

    //add by 榮升 2015/02/11 增加IPIN用欄位
    /**
     交易時間

     <remark></remark>
     */
    public final String getPIN()
    {
        return _PIN;
    }
    public final void setPIN(String value)
    {
        _PIN = value;
    }

    /**
     輸入行

     <remark></remark>
     */
    public final String getKINBR()
    {
        return _KINBR;
    }
    public final void setKINBR(String value)
    {
        _KINBR = value;
    }

    /**
     櫃台機號

     <remark></remark>
     */
    public final String getWSNO()
    {
        return _WSNO;
    }
    public final void setWSNO(String value)
    {
        _WSNO = value;
    }

    /**
     交易傳輸編號

     <remark></remark>
     */
    public final String getTXSEQ()
    {
        return _TXSEQ;
    }
    public final void setTXSEQ(String value)
    {
        _TXSEQ = value;
    }

    /**
     營業日

     <remark></remark>
     */
    public final String getTBSDY()
    {
        return _TBSDY;
    }
    public final void setTBSDY(String value)
    {
        _TBSDY = value;
    }

    /**
     MODE

     <remark></remark>
     */
    public final String getMODE()
    {
        return _MODE;
    }
    public final void setMODE(String value)
    {
        _MODE = value;
    }

    /**
     交易代號

     <remark>C01</remark>
     */
    public final String getTXCD()
    {
        return _TXCD;
    }
    public final void setTXCD(String value)
    {
        _TXCD = value;
    }

    /**
     拒絕代碼

     <remark></remark>
     */
    public final String getRC()
    {
        return _RC;
    }
    public final void setRC(String value)
    {
        _RC = value;
    }

    /**
     信用卡交易序號

     <remark></remark>
     */
    public final String getASPSTAN()
    {
        return _ASPSTAN;
    }
    public final void setASPSTAN(String value)
    {
        _ASPSTAN = value;
    }

    /**
     信用卡號

     <remark></remark>
     */
    public final String getCARDNO()
    {
        return _CARDNO;
    }
    public final void setCARDNO(String value)
    {
        _CARDNO = value;
    }

    /**
     金融卡號

     <remark></remark>
     */
    public final String getACTNO()
    {
        return _ACTNO;
    }
    public final void setACTNO(String value)
    {
        _ACTNO = value;
    }

    /**
     金融卡序號

     <remark></remark>
     */
    public final String getSEQNO()
    {
        return _SEQNO;
    }
    public final void setSEQNO(String value)
    {
        _SEQNO = value;
    }

    /**


     <remark></remark>
     */
    public final String getInText()
    {
        return _IN_TEXT;
    }
    public final void setInText(String value)
    {
        _IN_TEXT = value;
    }

    /**
     開卡註記

     <remark>強迫停卡：E，M，G， B，J，K，Z，W                           	卡片掛失：L，S，F，N</remark>
     */
    public final String getASPSTON()
    {
        return _ASPSTON;
    }
    public final void setASPSTON(String value)
    {
        _ASPSTON = value;
    }

    /**
     換Key種類

     <remark>1： PPK           0： MAC</remark>
     */
    public final String getKEYID()
    {
        return _KEYID;
    }
    public final void setKEYID(String value)
    {
        _KEYID = value;
    }

    /**
     原交易系統日

     <remark></remark>
     */
    public final String getTxdateO()
    {
        return _TXDATE_O;
    }
    public final void setTxdateO(String value)
    {
        _TXDATE_O = value;
    }

    /**
     原交易傳輸編號

     <remark></remark>
     */
    public final String getTxtnoO()
    {
        return _TXTNO_O;
    }
    public final void setTxtnoO(String value)
    {
        _TXTNO_O = value;
    }
}
