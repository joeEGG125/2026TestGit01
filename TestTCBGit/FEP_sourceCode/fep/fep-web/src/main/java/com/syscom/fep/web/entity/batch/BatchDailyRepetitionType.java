package com.syscom.fep.web.entity.batch;

/**
 * 每日重複執行類別
 */
public enum BatchDailyRepetitionType {
    /**
     * 每隔多少天
     */
    DAY("D"),
    /**
     * 每隔多長時間
     */
    TIME("T");

    private final String value;

    BatchDailyRepetitionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
