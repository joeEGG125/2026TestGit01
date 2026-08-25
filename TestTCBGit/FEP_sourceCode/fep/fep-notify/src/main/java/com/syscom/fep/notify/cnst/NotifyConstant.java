package com.syscom.fep.notify.cnst;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NotifyConstant {
    public static final String SYSTEM_VAR_SYMBOL = "#@";        // 系統參數 Symbol
    public static final String INDEPENDEN_VAR_SYMBOL = "##";    // 前端傳送變數 Symbol
    public static final String NOTIFY_TYPE_EMAIL = "M";                 // EMAIL
    public static final String NOTIFY_TYPE_SMS = "S";                   // SMS
    public static final String NOTIFY_TYPE_PUSH = "P";                   // 推播
    public static final String NOTIFY_TYPE_NOCLASSIFIED = "U";          // 不分類
    public static final String NOTIFY_EMAIL_PARM_NAME = "Email";
    public static final String NOTIFY_PHONE_PARM_NAME = "Phone";
    public static final String NOTIFY_PERSONID_PARM_NAME = "PersonId";        //2024-05-02新增PERSONID
    public static final String NOTIFY_MESSAGE_FAILURES = "Failures";
    public static final String NOTIFY_MESSAGE_CONTENT_TYPE = "Type";
    public static final String NOTIFY_MESSAGE_CONTENT_SUBJECT = "Subject";
    public static final String NOTIFY_MESSAGE_CONTENT_SUBJECT_SUFFIX = "Subject_Suffix";
    public static final String NOTIFY_MESSAGE_CONTENT_BODY = "Body";
    public static final String NOTIFY_MESSAGE_CONTENT_CLIENTID = "ClientId";
    public static final String NOTIFY_MESSAGE_DESC = "Desc";
    public static final String NOTIFY_MESSAGE_PROVIDER = "Provider";
    public static final String NOTIFY_MESSAGE_PROVIDER_PORT = "Port";
    public static final String NOTIFY_MESSAGE_ACCOUNT = "Account";
    public static final String NOTIFY_MESSAGE_SSCODE = "Sscode";
    public static final String NOTIFY_MESSAGE_SSLONCONNECT = "SslOnConnect";
    public static final String NOTIFY_MESSAGE_DOMAIN = "Domain";
    public static final String NOTIFY_MESSAGE_PRIORITY = "Priority";
    public static final String NOTIFY_MESSAGE_APSYSKEY = "ApSysKey";
    public static final int UPDATE_USER = 0;

}
