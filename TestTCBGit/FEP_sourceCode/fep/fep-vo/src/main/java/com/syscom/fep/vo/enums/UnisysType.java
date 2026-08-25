package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum UnisysType {
    /**
     台幣主機


     */
    TWN(0),
    /**
     香港主機


     */
    HKG(1),
    /**
     澳門主機


     */
    MAC(2),

    /**
     製發卡主機


     */
    Card(3),

    /**
     製發卡主動發動交易主機


     */
    CardRequest(4),

    /**
     RM交易


     */
    RM(5),

    /**
     RM交易(優利主動發動)


     */
    RMRequest(6);

    private int value;

    private UnisysType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static UnisysType fromValue(int value) {
        for (UnisysType e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static UnisysType parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (UnisysType e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
