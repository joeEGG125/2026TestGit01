package com.syscom.fep.notify.enums;

public enum NotifyStatusCode {
    NOTIFY_PROCESSING("001", "處理中"),
    NOTIFY_FINISH("002", "全部通知發送完成"),
    NOTIFY_FAILURE("003", "全部通知發送失敗"),
    NOTIFY_PARTIAL_FAILURE("004", "部分通知發送失敗"),
    RULESET_NOT_EXIST("100", "RULESET 不存在!"),
    NOTIFY_RULES_UNLESS("101", "通知條件不成立"),
    RULE_EXPRESSION_ERROR("102", "Rule Expression Error!"),
    PROVIDER_ERROR("103", "Provider Error!"),
    SEND_FINISH("001", "發送完成"),
    SEND_FAILURE("002", "發送失敗"),
    CLIENTID_DUPLICATION("701", "CLIENTID 重復!"),
    SYSTEM_ERROR("999", "系統錯誤");
    private final String code;
    private final String desc;
    private NotifyStatusCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public String getCode(){
        return code;
    }
    public String getDesc(){
        return desc;
    }

}
