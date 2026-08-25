package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum DAPPAppMsg {
    /**
     * 開啟無卡提款
     */
    Start(1),
    /**
     * 重設無卡提款
     */
    ReStart(2),
    /**
     * 關閉無卡提款
     */
    Close(3),
    /**
     * 密碼錯誤達上限
     */
    SSCodeErrorLimit(4),
    /**
     * 完成無卡提款
     */
    Complete(5);
    private int value;

    private DAPPAppMsg(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DAPPAppMsg fromValue(int value) {
        for (DAPPAppMsg e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static DAPPAppMsg parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (DAPPAppMsg e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}