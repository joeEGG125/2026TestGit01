package com.syscom.fep.vo.text.nb;

import java.math.BigDecimal;

public class NBGeneralRequest {
    private String _MsgID = "";
    private String _MsgType = "";
    private String _ChlName = "";
    private String _ChlEJNo = "";
    private String _ChlSendTime;
    private String _TxnID = "";
    private String _BranchID = "";
    private String _TermID = "";
    private String _SignID = "";
    private String _UserID = "";
    private String _CUSTID = "";
    private String _CARDNO = "";
    private String _CPRTCD = "";
    private String _GENSSCODE = "";
    private String _WWWSTATUS = "";
    private String _WWWMEMO = "";
    private String _SMSSTATUS = "";
    private String _SMSMOBILE = "";
    private String _BankCode = "";
    private String _SendMailToo = "";
    private String _IDNO = "";
    private String _ATMNO = "";
    private String _Acct = "";
    private String _SecurityCode = "";
    private BigDecimal _Amount = new BigDecimal(0);
    private String _Currency = "";
    private BigDecimal _Fee = new BigDecimal(0);
    private String _Memo = "";
    private String _TxnDate = "";
    private String _TransactionID = "";

    public String getMsgID()
    {
        return _MsgID;
    }
    public void setMsgID(String value)
    {
        _MsgID = value;
    }

    public String getMsgType()
    {
        return _MsgType;
    }
    public void setMsgType(String value)
    {
        _MsgType = value;
    }

    public String getChlName()
    {
        return _ChlName;
    }
    public void setChlName(String value)
    {
        _ChlName = value;
    }

    public String getChlEJNo()
    {
        return _ChlEJNo;
    }
    public void setChlEJNo(String value)
    {
        _ChlEJNo = value;
    }

    public String getChlSendTime()
    {
        return _ChlSendTime;
    }
    public void setChlSendTime(String value)
    {
        _ChlSendTime = value;
    }

    public String getTxnID()
    {
        return _TxnID;
    }
    public void setTxnID(String value)
    {
        _TxnID = value;
    }

    public String getBranchID()
    {
        return _BranchID;
    }
    public void setBranchID(String value)
    {
        _BranchID = value;
    }

    public String getTermID()
    {
        return _TermID;
    }
    public void setTermID(String value)
    {
        _TermID = value;
    }

    public String getUserID()
    {
        return _UserID;
    }
    public void setUserID(String value)
    {
        _UserID = value;
    }

    public String getSignID()
    {
        return _SignID;
    }
    public void setSignID(String value)
    {
        _SignID = value;
    }


    /**
     身分證字號含檢查碼

     <remark>限個人戶</remark>
     */
    public String getCUSTID()
    {
        return _CUSTID;
    }
    public void setCUSTID(String value)
    {
        _CUSTID = value;
    }

    /**
     晶片卡主帳號

     <remark></remark>
     */
    public String getCARDNO()
    {
        return _CARDNO;
    }
    public void setCARDNO(String value)
    {
        _CARDNO = value;
    }

    /**
     網路服務註記

     <remark></remark>
     */
    public String getCPRTCD()
    {
        return _CPRTCD;
    }
    public void setCPRTCD(String value)
    {
        _CPRTCD = value;
    }

    /**
     網路密碼函申請

     <remark></remark>
     */
    public String getGENPWD()
    {
        return _GENSSCODE;
    }
    public void setGENPWD(String value)
    {
        _GENSSCODE = value;
    }

    /**
     密碼函狀態

     <remark></remark>
     */
    public String getWWWSTATUS()
    {
        return _WWWSTATUS;
    }
    public void setWWWSTATUS(String value)
    {
        _WWWSTATUS = value;
    }

    /**
     密碼註銷原因

     <remark></remark>
     */
    public String getWWWMEMO()
    {
        return _WWWMEMO;
    }
    public void setWWWMEMO(String value)
    {
        _WWWMEMO = value;
    }

    /**
     簡訊狀態

     <remark></remark>
     */
    public String getSMSSTATUS()
    {
        return _SMSSTATUS;
    }
    public void setSMSSTATUS(String value)
    {
        _SMSSTATUS = value;
    }

    /**
     手機號碼

     <remark></remark>
     */
    public String getSMSMOBILE()
    {
        return _SMSMOBILE;
    }
    public void setSMSMOBILE(String value)
    {
        _SMSMOBILE = value;
    }

    /**
     金融代號

     <remark></remark>
     */
    public String getBankCode()
    {
        return _BankCode;
    }
    public void setBankCode(String value)
    {
        _BankCode = value;
    }

    /**
     是否同時寄送郵件

     <remark>true=同時寄送郵件 false=不寄送郵件</remark>
     */
    public String getSendMailToo()
    {
        return _SendMailToo;
    }
    public void setSendMailToo(String value)
    {
        _SendMailToo = value;
    }

    /**
     身分證字號

     <remark></remark>
     */
    public String getIDNO()
    {
        return _IDNO;
    }
    public void setIDNO(String value)
    {
        _IDNO = value;
    }

    /**
     ATM機台號碼

     <remark></remark>
     */
    public String getATMNO()
    {
        return _ATMNO;
    }
    public void setATMNO(String value)
    {
        _ATMNO = value;
    }

    /**
     提款帳號

     <remark></remark>
     */
    public String getAcct()
    {
        return _Acct;
    }
    public void setAcct(String value)
    {
        _Acct = value;
    }

    /**
     提款序號

     <remark>需檢核是否為16碼數字</remark>
     */
    public String getSecurityCode()
    {
        return _SecurityCode;
    }
    public void setSecurityCode(String value)
    {
        _SecurityCode = value;
    }

    /**
     提款金額

     <remark>檢核是否與預約之提款金額一致</remark>
     */
    public BigDecimal getAmount()
    {
        return _Amount;
    }
    public void setAmount(BigDecimal value)
    {
        _Amount = value;
    }

    /**
     幣別

     <remark>檢核是否與預約之提款幣別一致</remark>
     */
    public String getCurrency()
    {
        return _Currency;
    }
    public void setCurrency(String value)
    {
        _Currency = value;
    }

    /**
     手續費

     <remark></remark>
     */
    public BigDecimal getFee()
    {
        return _Fee;
    }
    public void setFee(BigDecimal value)
    {
        _Fee = value;
    }

    /**
     備註

     <remark></remark>
     */
    public String getMemo()
    {
        return _Memo;
    }
    public void setMemo(String value)
    {
        _Memo = value;
    }

    /**
     申請日期時間

     <remark>必輸申請日期時間</remark>
     */
    public String getTxnDate()
    {
        return _TxnDate;
    }
    public void setTxnDate(String value)
    {
        _TxnDate = value;
    }

    /**
     交易序號

     <remark>ATM提領成功時的交易序號</remark>
     */
    public String getTransactionID()
    {
        return _TransactionID;
    }
    public void setTransactionID(String value)
    {
        _TransactionID = value;
    }

}
