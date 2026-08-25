package com.syscom.fep.vo.text.credit;

public class CreditGeneral {
    private CreditGeneralRequest mRequest;
    private CreditGeneralResponse mResponse;

    public CreditGeneral()
    {
        mRequest = new CreditGeneralRequest();
        mResponse = new CreditGeneralResponse();
    }

    public CreditGeneralRequest getRequest()
    {
        return mRequest;
    }

    public CreditGeneralResponse getResponse()
    {
        return mResponse;
    }
}
