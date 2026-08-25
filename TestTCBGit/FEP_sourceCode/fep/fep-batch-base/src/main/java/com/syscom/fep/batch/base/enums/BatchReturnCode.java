package com.syscom.fep.batch.base.enums;

import org.apache.commons.lang3.StringUtils;

public enum BatchReturnCode {
    // 以下是客戶定義的
    Succeed(0),
    JavaException(1),
    DbConnectionException(4),
    SqlException(5),
    MftTransferFailed(7),
    Failed(10),
    // 以下是FEP定義的
    Abnormal(-1),
    ProgramException(-2),
    TableNotFound(-3),
    FileNotFound(-4),
    DBIOError(-5),
    MissingArgument(-6),
    InvalidArgument(-7);

    private final int value;

    BatchReturnCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static BatchReturnCode fromValue(int value) {
        for (BatchReturnCode e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static BatchReturnCode parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (BatchReturnCode e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
