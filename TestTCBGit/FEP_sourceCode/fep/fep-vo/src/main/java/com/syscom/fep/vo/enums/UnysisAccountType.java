package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum UnysisAccountType {
    Unknow(0),
    M(1),
    C(2);

    private int value;

    private UnysisAccountType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static UnysisAccountType fromValue(int value) {
        for (UnysisAccountType e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static UnysisAccountType parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (UnysisAccountType e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
