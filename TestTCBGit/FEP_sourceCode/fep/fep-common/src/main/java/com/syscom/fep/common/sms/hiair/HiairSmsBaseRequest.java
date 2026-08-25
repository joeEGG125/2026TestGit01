package com.syscom.fep.common.sms.hiair;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class HiairSmsBaseRequest implements Serializable {
    /**
     * 使用者帳號(必填)
     */
    @SerializedName("username")
    private String userName;
    /**
     * 使用者密碼(必填)
     */
    @SerializedName("password")
    private String sscode;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSscode() {
        return sscode;
    }

    public void setSscode(String sscode) {
        this.sscode = sscode;
    }
}
