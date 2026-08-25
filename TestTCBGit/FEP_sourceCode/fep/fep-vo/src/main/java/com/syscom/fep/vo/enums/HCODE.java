package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

/**
 更正記號


 */
public enum HCODE {
    /**
     正常
     */
    Normal(0),

    /**
     更正
     */
    NonNormal(1);

    private int value;

    private HCODE(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static HCODE fromValue(int value) {
        for (HCODE e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static HCODE parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (HCODE e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
