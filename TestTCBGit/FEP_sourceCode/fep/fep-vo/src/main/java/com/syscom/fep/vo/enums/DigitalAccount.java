package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum DigitalAccount {
    Dig1_1(11),
    Dig3_1(31),
    Dig3_2(32),
    //add by 榮升 2019/08/23 增加數1-2、數1-3、數2-1
    Dig1_2(12),
    Dig1_3(13),
    Dig2_1(21);

    private int value;

    private DigitalAccount(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DigitalAccount fromValue(int value) {
        for (DigitalAccount e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static DigitalAccount parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (DigitalAccount e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
