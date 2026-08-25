package com.syscom.fep.vo.text.app.response;

import com.syscom.fep.vo.text.app.APPGeneral;

public class TaiwanPayResponse {
    /**
     呼叫狀態

     <remark></remark>
     */
    private String success;
    public String getSuccess() {
        return success;
    }
    public void setSuccess(String value) {
        success = value;
    }

    /**
     錯誤代碼

     <remark></remark>
     */
    private String retuenCode;
    public String getRetuenCode() {
        return retuenCode;
    }
    public void setRetuenCode(String value) {
        retuenCode = value;
    }

    /**
     錯誤訊息

     <remark></remark>
     */
    private String message;
    public String getMessage() {
        return message;
    }
    public void setMessage(String value) {
        message = value;
    }

    public void parseFlatfile(String flatfile, APPGeneral general) {
        if (flatfile.contains("[")) {
            flatfile = flatfile.substring(flatfile.indexOf("[") + 1, flatfile.indexOf("[") + 1 + flatfile.indexOf("]") - flatfile.indexOf("[") - 1);
        }
        for (String str : flatfile.replace("{", ",").replace("}", "").split("[,]", -1)) {
            String[] strs = str.split("[:]", -1);
            if (strs.length == 2) {
                if (strs[0].replace("\"", "").equals("Success")) {
                    general.getmResponse().setSuccess(strs[1].replace("\"", ""));
                } else if (strs[0].replace("\"", "").equals("RetuenCode")) {
                    general.getmResponse().setRetuenCode(strs[1].replace("\"", ""));
                } else if (strs[0].replace("\"", "").equals("Message")) {
                    general.getmResponse().setMessage(strs[1].replace("\"", ""));
                }
            }
        }
    }

}
