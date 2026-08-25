package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum ATMCardTFRFlag {
    /**
     未申請轉帳
     */
    NotApply(0),

    /**
     申請不約定帳號轉帳
     */
    WithoutContract(1),

    /**
     申請約定帳號轉帳
     */
    WithContract(2),

    /**
     申請大額約定帳號轉帳
     */
    BigAmount(3);

    private int value;

    private ATMCardTFRFlag(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ATMCardTFRFlag fromValue(int value) {
        for (ATMCardTFRFlag e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static ATMCardTFRFlag parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (ATMCardTFRFlag e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
