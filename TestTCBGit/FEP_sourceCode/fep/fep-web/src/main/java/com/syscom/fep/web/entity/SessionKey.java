package com.syscom.fep.web.entity;

import org.apache.commons.lang3.StringUtils;

public enum SessionKey {

    /**
     * 用來記錄當前登入者信息
     */
    LogonUser,
    /**
     * 用來記錄Fepuser資料
     */
    Fepuser,
    /**
     * 使用者群組
     */
    Group,
    /**
     * 用來臨時存儲頁面查詢的資料
     */
    TemporaryRestoreData,
    /**
     * 用來存儲AuditLog
     */
    AuditLog;

    @Override
    public String toString() {
        return StringUtils.join(this.name().substring(0, 1).toLowerCase(), this.name().substring(1));
    }
}
