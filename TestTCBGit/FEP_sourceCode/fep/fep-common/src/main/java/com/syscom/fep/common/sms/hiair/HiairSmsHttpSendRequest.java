package com.syscom.fep.common.sms.hiair;

import com.google.gson.annotations.SerializedName;

public class HiairSmsHttpSendRequest extends HiairSmsBaseRequest {
    /**
     * 接收門號(必填)
     * 門號長度: 國內: 10碼、國外: 20碼。
     * *國外門號請以+號開頭，例如: +18008339939。
     * *門號若為 +8869 或 +88609 皆視為國內門號。
     */
    private String mobile;
    /**
     * 簡訊內容(必填)
     * 簡訊內容規則說明:
     * <p>
     * 【智慧簡訊分割長度】
     * 純英數字，每則最多159字，超過自動以長簡訊進行發訊。
     * 非純英數字，每則最多70字，超過自動以長簡訊進行發訊。
     * <p>
     * 【短簡訊分割長度】
     * 純英數字，每則最多159字，超過自動裁減並發送簡訊。
     * 非純英數字，每則最多70字，超過自動裁減並發送簡訊。
     * <p>
     * 【長簡訊分割長度】
     * 簡訊內容限制最長1530個純英數字或670個中文字(包含英數字)。
     * 長簡訊自動分則計算方式為: 純英數字，每分則最多153字。
     * 中文字(包含英數字)，每分則最多67字。
     * 長簡訊分則上限10則，超過分則上限，取消發送簡訊並回傳錯誤訊息。
     */
    private String message;
    /**
     * 重送截止時間(選填)
     * 即簡訊有效期限，輸入有效範圍: 1~1440，單位：分鐘。
     * *有效範圍外之數值，將預設為1440分鐘。
     * *若不需指定重送截止時間，可不送此欄位；若有送此欄位但無輸入值，則會回傳錯誤。
     * *此欄位為整數型別，若參數開頭為0，請刪除前導0 (例如:0001，請調整為1)，避免格式驗證失敗
     */
    @SerializedName("limit_time")
    private Integer limitTime;
    /**
     * 預約時間(選填)
     * 格式為yyMMddHHmmss，格式說明如後：
     * yy：不含世紀之西元年，例如2022年請輸入22
     * MM：月份，從01~12
     * dd：日期，從01~31
     * HH：24小時，從00~23
     * mm：分鐘，從01~59
     * ss：秒鐘，從01~59
     * 例如: 預約發送時間為2022/02/01 10:30:00，則傳入格式為220201103000。
     */
    @SerializedName("order_time")
    private String orderTime;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getLimitTime() {
        return limitTime;
    }

    public void setLimitTime(Integer limitTime) {
        this.limitTime = limitTime;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }
}
