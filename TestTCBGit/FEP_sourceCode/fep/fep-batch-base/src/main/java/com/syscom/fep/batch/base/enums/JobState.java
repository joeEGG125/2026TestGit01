package com.syscom.fep.batch.base.enums;

import org.apache.commons.lang3.StringUtils;

public enum JobState {
    Start(1, "開始執行"),
    Running(2, "執行中"),
    End(3, "執行成功"),
    Failed(4, "執行失敗"),
    Abort(5, "中止");

    private final int value;
    private final String description;

    private JobState(int value, String description) {
        this.value = value;
        this.description = StringUtils.join("工作", description);
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static JobState fromValue(int value) {
        for (JobState e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static JobState parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (JobState e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
