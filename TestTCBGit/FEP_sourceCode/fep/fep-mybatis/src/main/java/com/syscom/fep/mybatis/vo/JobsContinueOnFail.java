package com.syscom.fep.mybatis.vo;

import org.apache.commons.lang.StringUtils;

/**
 * 失敗時決定是否要繼續執行Job
 *
 * @author Richard
 */
public enum JobsContinueOnFail {
    /**
     * 中止
     */
    Interrupt("中止"),
    /**
     * 繼續
     */
    Continue("繼續");

    private final String description;

    private JobsContinueOnFail(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static String description(String ordinal) {
        JobsContinueOnFail[] values = JobsContinueOnFail.values();
        try {
            return values[Integer.parseInt(ordinal)].description;
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }
}
