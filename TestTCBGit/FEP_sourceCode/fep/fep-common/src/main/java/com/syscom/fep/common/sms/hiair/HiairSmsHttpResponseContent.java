package com.syscom.fep.common.sms.hiair;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class HiairSmsHttpResponseContent implements Serializable {
    /**
     * request api不同，回傳的代碼亦不同
     */
    @SerializedName("ret_code")
    private int retCode;
    /**
     * request api不同，回傳的內容亦不同
     */
    @SerializedName("ret_content")
    private String retContent;
    /**
     * 回傳數值：2:短簡訊、12:長簡訊。
     * 告知用戶該request api是以哪種簡訊類型進行 發送/查詢/取消。
     * *http status code為200時，才會回傳此欄位。
     */
    @SerializedName("msg_type")
    private int msgType;

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public String getRetContent() {
        return retContent;
    }

    public void setRetContent(String retContent) {
        this.retContent = retContent;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }
}
