package com.syscom.fep.common.sms.hiair;

public enum HiairSmsHttpResponseCode {
    COMMON_400_90(400, 90, "Json format error", "Json格式錯誤"),
    COMMON_400_91(400, 91, "Missing field", "Json欄位缺漏"),
    COMMON_400_92(400, 92, "Json variable type error", "Json欄位型別錯誤"),
    COMMON_400_93(400, 93, "Json field empty", "Json欄位空值"),
    COMMON_504_94(504, 94, "Socket connect error", "Socket連線失敗"),
    COMMON_400_95(400, 95, "Send type error(number)", "傳送型式錯誤"),
    COMMON_400_96(400, 96, "msg_type value error, needs to be 2 or 12.", "msg_type數值錯誤"),
    COMMON_200_30(200, 30, "Message length is smaller than definition", "傳送的訊息長度有誤"),
    COMMON_200_31(200, 31, "network error, try again", "網路傳輸發生錯誤"),
    COMMON_200_32(200, 32, "msg_type not know", "訊息種類無法辨識"),
    COMMON_200_40(200, 40, "database error", "系統內部錯誤"),
    COMMON_200_41(200, 41, "System internal error, try again later", "系統內部錯誤"),
    COMMON_200_50(200, 50, "ID/Password has not been checked", "尚未經過密碼檢查就發送"),
    COMMON_200_51(200, 51, "ID/Password checking again", "已通過密碼檢查，又送來帳號密碼檢查"),
    COMMON_200_52(200, 52, "text Service not apply yet", "文字簡訊傳送未申請"),
    COMMON_200_53(200, 53, "receive text service not apply yet", "接收文字簡訊未申請"),
    COMMON_200_57(200, 57, "long message not apply yet", "長簡訊傳送未申請"),
    COMMON_200_58(200, 58, "foreign message not apply yet", "國際簡訊傳送未申請"),
    COMMON_200_80(200, 80, "ID/Password check successful", "帳號/密碼檢查成功"),
    COMMON_401_81(401, 81, "Account or password error", "帳號或密碼錯誤"),
    COMMON_200_83(200, 83, "Over the maximum allowed connection number", "超過允許的最大連線數目"),
    COMMON_200_84(200, 84, "The account status not correct", "帳號狀態不正確或已退租"),
    COMMON_200_85(200, 85, "get account data error", "無法取得帳號資料"),
    COMMON_200_86(200, 86, "get password data error", "無法取得密碼資料"),
    COMMON_200_87(200, 87, "System error, try again later", "暫時無法檢查帳號/密碼"),
    SEND_200_0(200, 0, "MessageID(用於查詢傳送結果)", "訊息傳送成功"),
    SEND_200_1(200, 1, "Country code format error", "國別格式錯誤"),
    SEND_200_2(200, 2, "Coding format error", "編碼格式錯誤"),
    SEND_200_3(200, 3, "Priority format error", "優先權格式錯誤"),
    SEND_200_4(200, 4, "Msg_content_len format error", "Msg_content_len格式錯誤"),
    SEND_200_5(200, 5, "Msg_content_len not the same with msg_content", "Msg_content_len與msg_content的長度不相符"),
    SEND_200_6(200, 6, "Telephone number format error", "接收手機號碼格式錯誤"),
    SEND_200_7(200, 7, "Transfer type format error", "傳送型式的格式錯誤"),
    SEND_200_8(200, 8, "Limit time format error", "重送截止時間格式錯誤"),
    SEND_200_9(200, 9, "Ordered time format error", "預約傳送格式錯誤"),
    SEND_200_10(200, 10, "send to foreign not allow now", "目前暫不開放傳送至國外"),
    SEND_200_11(200, 11, "Message sending failure, try again", "系統暫時無法傳送訊息"),
    SEND_200_12(200, 12, "Message out of sequence", "長簡訊訊息次序錯誤"),
    SEND_200_15(200, 15, "billing user_hn format error", "加值出帳HN格式錯誤"),
    SEND_200_16(200, 16, "message has 9-10 digits tel number", "簡訊內容包含連續9-10碼的電話號碼，不合規定。"),
    SEND_200_17(200, 17, "More than 10 text messages to comprise a long message", "長簡訊超過10則分則上限"),
    SEND_200_22(200, 22, "Message content deny", "簡訊內容不合規定(疑似詐財簡訊)"),
    QUERY_200_0(200, 0, "Successful", "訊息已送達接收方(包含送達時間)"),
    QUERY_200_1(200, 1, "Mobile turn off/Mobile out of scope", "手機未開或在受訊範圍外(系統會Retry)(可重查)"),
    QUERY_200_2(200, 2, "System contains no data", "系統無此messageID的資料"),
    QUERY_200_3(200, 3, "MessageID format error", "MessageID 資料有誤"),
    QUERY_200_4(200, 4, "has send to SMC, query no complete", "已送至簡訊中心，尚未完成查詢(可重查)"),
    QUERY_200_5(200, 5, "Ordered time beyond system limit", "預約傳送時間超過系統限制"),
    QUERY_200_6(200, 6, "Send binary data to pager", "傳送二進位訊息到呼叫器"),
    QUERY_200_7(200, 7, "Code transfer fail", "訊息轉碼失敗"),
    QUERY_200_8(200, 8, "telephone number or message content format error", "手機號碼或簡訊內容格式錯誤"),
    QUERY_200_9(200, 9, "has expired at queue server", "簡訊在Queue Server端，已過期"),
    QUERY_200_15(200, 15, "Message status unknown", "訊息狀態有誤"),
    QUERY_200_16(200, 16, "message sending failure", "傳送失敗(例如預約時間小於目前時間等問題)"),
    QUERY_200_17(200, 17, "Message can not send to GSM/Pager", "訊息無法送達對方"),
    QUERY_200_18(200, 18, "other error", "無法判斷的錯誤訊息"),
    QUERY_200_19(200, 19, "Message is submitted to SMSC", "訊息已送至簡訊中心(可重查)"),
    QUERY_200_23(200, 23, "Message is barred by customer", "受訊客戶要求拒收加值簡訊，請不要重送"),
    QUERY_SHORT_200_20(200, 20, "reserve message, waiting send", "預約簡訊，等待傳送中。(可重查)"),
    QUERY_SHORT_200_21(200, 21, "reserve message, cancel send", "預約簡訊，已取消傳送。"),
    QUERY_SHORT_200_22(200, 22, "message content deny", "簡訊內容不合規定(疑似詐財簡訊)"),
    QUERY_LONG_200_11(200, 11, "The long message is processing: n of m succeeded", "長簡訊傳送中(可重查)"),
    QUERY_LONG_200_20(200, 20, "reserve message, waiting send, total:n", "預約長簡訊，等待傳送中。 (可重查)"),
    QUERY_LONG_200_21(200, 21, "reserv message, cancel send, total:n canceled:n succeeded:n", "預約長簡訊，有分則被取消"),
    QUERY_LONG_200_22(200, 22, "message content deny, total:n denied:n succeeded:n", "長簡訊分則內容不合規定(疑似詐財簡訊)"),
    QUERY_LONG_200_24(200, 24, "long message can not be delivered, n of n failed", "長簡訊傳送失敗(有分則已確定失敗)"),
    CANCEL_200_0(200, 0, "Successful", "取消成功"),
    CANCEL_200_1(200, 1, "Message is not reserving", "此簡訊目前非預約狀態"),
    CANCEL_200_2(200, 2, "System contains no data", "系統無此訊息"),
    CANCEL_200_3(200, 3, "MessageID format error MessageID", "資料有誤");

    private final int code;
    private final int retCode;
    private final String retContent;
    private final String description;

    HiairSmsHttpResponseCode(int code, int retCode, String retContent, String description) {
        this.code = code;
        this.retCode = retCode;
        this.retContent = retContent;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public int getRetCode() {
        return retCode;
    }

    public String getRetContent() {
        return retContent;
    }

    public String getDescription() {
        return description;
    }
}
