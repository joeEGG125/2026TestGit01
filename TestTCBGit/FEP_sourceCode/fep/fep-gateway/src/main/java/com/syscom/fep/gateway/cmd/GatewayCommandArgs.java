package com.syscom.fep.gateway.cmd;

public enum GatewayCommandArgs {
    Help("h"),
    Function("f"),
    Data("d"),
    IP("i"),
    Port("p");

    private String code;
    private String param;

    private GatewayCommandArgs(String code) {
        this.code = code;
        this.param = "-" + this.code;
    }

    public String getCode() {
        return code;
    }

    public String getParam() {
        return param;
    }
}
