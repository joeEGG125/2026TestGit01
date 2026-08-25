package com.syscom.fep.web.entity;

import org.apache.commons.lang3.StringUtils;

public enum AttributeName {

    UserId,
    BankId,
    Message,
    MessageType,
    ErrorMessage,
    Form,
    Options,
    Options2,
    Options3,
    GridData,
    DetailEntity,
    DetailMap,
    PageData,
    ChangedTitle,
    DvRMOUT,
    DvRMIN,
    CalendarYear,
    CalendarActiveDate,
    ExecStackTrace,
    SelectServer,
    SelectServerDownload,
    SelectLogType,
    Str,
    LogoutUserId,
    List,
    Maps,
    ChannelDownloadOptions;

    @Override
    public String toString() {
        return StringUtils.join(this.name().substring(0, 1).toLowerCase(), this.name().substring(1));
    }
}
