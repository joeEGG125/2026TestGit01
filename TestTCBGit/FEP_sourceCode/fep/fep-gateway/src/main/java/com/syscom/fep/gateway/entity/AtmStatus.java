package com.syscom.fep.gateway.entity;

import org.apache.commons.lang3.StringUtils;

public enum AtmStatus {
    Connected(0),
    Disconnected(1);

    private int value;

    private AtmStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean validate(String value) {
        try {
            fromValue(Integer.parseInt(value));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static AtmStatus fromValue(int value) {
        for (AtmStatus e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static AtmStatus parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (AtmStatus e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
