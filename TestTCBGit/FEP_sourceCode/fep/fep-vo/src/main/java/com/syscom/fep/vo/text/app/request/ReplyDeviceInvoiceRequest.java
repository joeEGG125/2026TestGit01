package com.syscom.fep.vo.text.app.request;

import com.syscom.fep.vo.text.app.APPGeneral;

public class ReplyDeviceInvoiceRequest {
    /**
     帳號

     <remark></remark>
     */
    private String cardNumber;
    public String getCardNumber() {
        return cardNumber;
    }
    public void setCardNumber(String value) {
        cardNumber = value;
    }

    /**
     從 General 對應回來

     <remark></remark>
     */
    public String makeMessageFromGeneral(APPGeneral general) {
        String msg = "{";
        msg += "\"cardNumber\":\"" + general.getmRequest().getCardNumber() + "\",";
        msg = msg.substring(0, msg.length() - 1);
        msg += "}";
        return msg;
    }
}
