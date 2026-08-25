package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum CBSTxType {
    /**
     沖正Error Correct


     */
    EC(0),
    /**
     記帳


     */
    Accounting(1);

    private int value;

    private CBSTxType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CBSTxType fromValue(int value) {
        for (CBSTxType e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static CBSTxType parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (CBSTxType e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
