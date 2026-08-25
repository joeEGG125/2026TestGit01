package com.syscom.fep.vo.text.nb;

import com.syscom.fep.vo.text.FEPRsHeader.FEPRsHeaderOverride;

public class NBGeneralResponse {
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

    private String _CUSTID = "";
    private String _USERCODE = "";
    private String _SSCODE = "";
    private String _RETRY = "";
    private String _APPLYLIMITDATE = "";
    private String _CPRTCD = "";
    private String _CPRTCDTIMESTAMP = "";
    private String _WWWSTATUS = "";
    private String _WWWDATE = "";
    private String _WWWCOUNT = "";
    private String _WWWTIMESTAMP = "";
    private String _WWWMEMO = "";
    private String _WWWSSCODE = "";
    private String _SMSSTATUS = "";
    private String _SMSMOBILE = "";
    private String _MOBILECOUNT = "";
    private String _SMSDATE = "";
    private String _SMSCOUNT = "";
    private String _SMSTIMESTAMP = "";
    private String _SMSMEMO = "";
    private String _Acct = "";
    private String _SecurityCode = "";

    public String getChlEJNo()
    {
        return _ChlEJNo;
    }
    public void setChlEJNo(String value)
    {
        _ChlEJNo = value;
    }

    public String getEJNo()
    {
        return _EJNo;
    }
    public void setEJNo(String value)
    {
        _EJNo = value;
    }

    public String getRqTime()
    {
        return _RqTime;
    }
    public void setRqTime(String value)
    {
        _RqTime = value;
    }

    public String getRsTime()
    {
        return _RsTime;
    }
    public void setRsTime(String value)
    {
        _RsTime = value;
    }

    //VB TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: <System.Xml.Serialization.XmlArrayItemAttribute("Override", IsNullable:=True)> Public Property Overrides() As FEPRsHeaderOverride()
    public FEPRsHeaderOverride[] getOverrides()
    {
        return _Overrides;
    }
    public void setOverrides(FEPRsHeaderOverride[] value)
    {
        _Overrides = value;
    }

    public String getRsStatRsStateCode()
    {
        return _RsStat_RsStateCode;
    }
    public void setRsStatRsStateCode(String value)
    {
        _RsStat_RsStateCode = value;
    }

    public String getRsStatRsStateCodeType()
    {
        return _RsStat_RsStateCode_type;
    }
    public void setRsStatRsStateCodeType(String value)
    {
        _RsStat_RsStateCode_type = value;
    }

    public String getRsStatDesc()
    {
        return _RsStat_Desc;
    }
    public void setRsStatDesc(String value)
    {
        _RsStat_Desc = value;
    }

    public String getT24Application()
    {
        return _T24Application;
    }
    public void setT24Application(String value)
    {
        _T24Application = value;
    }

    public String getT24Version()
    {
        return _T24Version;
    }
    public void setT24Version(String value)
    {
        _T24Version = value;
    }

    public String getT24Operation()
    {
        return _T24Operation;
    }
    public void setT24Operation(String value)
    {
        _T24Operation = value;
    }

    public String getT24RspCode()
    {
        return _T24RspCode;
    }
    public void setT24RspCode(String value)
    {
        _T24RspCode = value;
    }


    /**
     身份證號/統編,含檢查碼

     <remark></remark>
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
     使用者代碼

     <remark></remark>
     */
    public String getUSERCODE()
    {
        return _USERCODE;
    }
    public void setUSERCODE(String value)
    {
        _USERCODE = value;
    }

    /**
     密碼函密碼

     <remark></remark>
     */
    public String getPASSWORD()
    {
        return _SSCODE;
    }
    public void setPASSWORD(String value)
    {
        _SSCODE = value;
    }

    /**
     申請次數

     <remark></remark>
     */
    public String getRETRY()
    {
        return _RETRY;
    }
    public void setRETRY(String value)
    {
        _RETRY = value;
    }

    /**
     有效使用期限

     <remark></remark>
     */
    public String getAPPLYLIMITDATE()
    {
        return _APPLYLIMITDATE;
    }
    public void setAPPLYLIMITDATE(String value)
    {
        _APPLYLIMITDATE = value;
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
     網路服務註記更新時間

     <remark></remark>
     */
    public String getCPRTCDTIMESTAMP()
    {
        return _CPRTCDTIMESTAMP;
    }
    public void setCPRTCDTIMESTAMP(String value)
    {
        _CPRTCDTIMESTAMP = value;
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
     密碼函日期

     <remark></remark>
     */
    public String getWWWDATE()
    {
        return _WWWDATE;
    }
    public void setWWWDATE(String value)
    {
        _WWWDATE = value;
    }

    /**
     密碼函錯誤次數

     <remark></remark>
     */
    public String getWWWCOUNT()
    {
        return _WWWCOUNT;
    }
    public void setWWWCOUNT(String value)
    {
        _WWWCOUNT = value;
    }

    /**
     密碼函更新時間

     <remark></remark>
     */
    public String getWWWTIMESTAMP()
    {
        return _WWWTIMESTAMP;
    }
    public void setWWWTIMESTAMP(String value)
    {
        _WWWTIMESTAMP = value;
    }

    /**
     密碼函註銷原因

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
     密碼函密碼

     <remark></remark>
     */
    public String getWWWPWD()
    {
        return _WWWSSCODE;
    }
    public void setWWWPWD(String value)
    {
        _WWWSSCODE = value;
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
     簡訊手機號碼

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
     手機錯誤次數

     <remark></remark>
     */
    public String getMOBILECOUNT()
    {
        return _MOBILECOUNT;
    }
    public void setMOBILECOUNT(String value)
    {
        _MOBILECOUNT = value;
    }

    /**
     簡訊申請日期

     <remark></remark>
     */
    public String getSMSDATE()
    {
        return _SMSDATE;
    }
    public void setSMSDATE(String value)
    {
        _SMSDATE = value;
    }

    /**
     簡訊錯誤次數

     <remark></remark>
     */
    public String getSMSCOUNT()
    {
        return _SMSCOUNT;
    }
    public void setSMSCOUNT(String value)
    {
        _SMSCOUNT = value;
    }

    /**
     訊狀態異動時間

     <remark></remark>
     */
    public String getSMSTIMESTAMP()
    {
        return _SMSTIMESTAMP;
    }
    public void setSMSTIMESTAMP(String value)
    {
        _SMSTIMESTAMP = value;
    }

    /**
     簡訊註銷原因

     <remark></remark>
     */
    public String getSMSMEMO()
    {
        return _SMSMEMO;
    }
    public void setSMSMEMO(String value)
    {
        _SMSMEMO = value;
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

     <remark></remark>
     */
    public String getSecurityCode()
    {
        return _SecurityCode;
    }
    public void setSecurityCode(String value)
    {
        _SecurityCode = value;
    }

}
