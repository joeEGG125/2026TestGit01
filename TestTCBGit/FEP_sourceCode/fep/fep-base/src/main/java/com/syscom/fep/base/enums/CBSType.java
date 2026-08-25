package com.syscom.fep.base.enums;

public enum CBSType {
    CBS("CBS"),
    _473X("473X"),
    FISC("FISC"),
    NONATM("NONATM"),
    FISCTCB("FISCTCB");

    private final String code;

    private CBSType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static CBSType fromCode(String code) {
        for (CBSType e : values()) {
            if (code.equals(e.getCode())) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid code = [" + code + "]!!!");
    }

    public static CBSType parse(Object nameOrCode) {
        for (CBSType e : values()) {
            if (nameOrCode.equals(e.getCode()) || nameOrCode.equals(e.name())) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid name or code = [" + nameOrCode + "]!!!");
    }
}
