package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

/**
 記帳類別


 */
public enum SelfCDType {
    /**
     本行


     */
    IssuerBank(1),
    /**
     聯行


     */
    InterBank(2),
    /**
     跨行


     */
    IntraBank(3),
    /**
     跨國


     */
    InternationBank(4),
    /**
     跨國(收費)


     */
    InternationBankHaveFee(5);

    private int value;

    private SelfCDType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }


    public static SelfCDType fromValue(int value) {
        for (SelfCDType e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static SelfCDType parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (SelfCDType e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}

