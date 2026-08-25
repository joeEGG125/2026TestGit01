package com.syscom.fep.mybatis.enums;

public enum RelationalOperators {
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<="),
    EQ("="),
    NE("<>");

    private String operator;

    private RelationalOperators(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

    public static RelationalOperators fromOperator(String operator) {
        for (RelationalOperators e : values()) {
            if (e.getOperator().equals(operator)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid operator = [" + operator + "]!!!");
    }

    public static RelationalOperators parse(String nameOrOperator) {
        for (RelationalOperators e : values()) {
            if (e.getOperator().equals(nameOrOperator)) {
                return e;
            }
        }
        for (RelationalOperators e : values()) {
            if (e.name().equalsIgnoreCase(nameOrOperator)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid name or operator = [" + nameOrOperator + "]!!!");
    }
}
