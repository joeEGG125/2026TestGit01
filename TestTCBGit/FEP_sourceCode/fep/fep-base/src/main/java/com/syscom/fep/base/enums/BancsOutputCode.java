package com.syscom.fep.base.enums;

public enum BancsOutputCode {
    Normal("88"),
    Error("01"),
    QueryResult("03"),
    OKResult("08");

    private String value;

    private BancsOutputCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
