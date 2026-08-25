package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum ATMCardCombine {
    /**
     客戶未申請
     */
    NotApply(0),

    /**
     BSP收件
     */
    BSPAccept(1),

    /**
     ASP建檔
     */
    Create(2),

    /**
     ASP核卡中
     */
    Checking(3),

    /**
     ASP拒絕
     */
    Reject(4),

    /**
     ASP發卡
     */
    SendOut(5),

    /**
     客戶啟用兩卡合一功能
     */
    CombineStart(6),

    /**
     ASP已掛失(不補發)
     */
    LoseWithoutReSend(7),
    /**

     ASP已掛失(補發中)
     */
    LoseWithReSending(8),

    /**
     ASP續卡中
     */
    Extending(9);
    private int value;

    private ATMCardCombine(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ATMCardCombine fromValue(int value) {
        for (ATMCardCombine e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static ATMCardCombine parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (ATMCardCombine e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
