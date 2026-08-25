package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

/**
 無卡提款卡片狀態


 */
public enum ATMNCCardStatus {
    /**
     未申請
     */
    NotApply(0),

    /**
     設定
     */
    Apply(1),

    /**
     關閉
     */
    Close(6);

    private int value;

    private ATMNCCardStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ATMNCCardStatus fromValue(int value) {
        for (ATMNCCardStatus e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static ATMNCCardStatus parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (ATMNCCardStatus e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
