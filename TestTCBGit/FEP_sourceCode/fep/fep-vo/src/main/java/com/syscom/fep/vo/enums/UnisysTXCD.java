package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum UnisysTXCD {
    J00001(1),
    K00001(2),
    J10206(3),
    K10206(4),
    J10033(5),
    K10033(6),
    J10060(7),
    K10060(8),
    G08002(9),
    J10207(10),
    K10207(11),
    J10208(12),
    K10208(13),
    J10209(14),
    K10209(15),
    K10205(16),
    J10205(17);


    private int value;

    private UnisysTXCD(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static UnisysTXCD fromValue(int value) {
        for (UnisysTXCD e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static UnisysTXCD parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (UnisysTXCD e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
