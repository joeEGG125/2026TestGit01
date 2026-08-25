package com.syscom.fep.vo.text.app;

public class APPGeneral {
    private APPGeneralRequest privatemRequest;
    public  APPGeneralRequest getmRequest()
    {
        return privatemRequest;
    }
    public void setmRequest(APPGeneralRequest value)
    {
        privatemRequest = value;
    }

    private APPGeneralResponse privatemResponse;
    public APPGeneralResponse getmResponse()
    {
        return privatemResponse;
    }
    public void setmResponse(APPGeneralResponse value)
    {
        privatemResponse = value;
    }

    public APPGeneral()
    {
        setmRequest(new APPGeneralRequest());
        setmResponse(new APPGeneralResponse());
    }
}
