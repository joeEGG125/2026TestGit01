package com.syscom.fep.vo.text.app;

public class APPGeneralResponse {
    /**
     載具條碼

     <remark></remark>
     */
    private String privateDeviceInvoice;
    public String getDeviceInvoice()
    {
        return privateDeviceInvoice;
    }
    public void setDeviceInvoice(String value)
    {
        privateDeviceInvoice = value;
    }

    /**
     呼叫狀態

     <remark></remark>
     */
    private String privateSuccess;
    public String getSuccess()
    {
        return privateSuccess;
    }
    public void setSuccess(String value)
    {
        privateSuccess = value;
    }

    /**
     錯誤代碼

     <remark></remark>
     */
    private String privateRetuenCode;
    public String getRetuenCode()
    {
        return privateRetuenCode;
    }
    public void setRetuenCode(String value)
    {
        privateRetuenCode = value;
    }

    /**
     錯誤訊息

     <remark></remark>
     */
    private String privateMessage;
    public String getMessage()
    {
        return privateMessage;
    }
    public void setMessage(String value)
    {
        privateMessage = value;
    }

}
