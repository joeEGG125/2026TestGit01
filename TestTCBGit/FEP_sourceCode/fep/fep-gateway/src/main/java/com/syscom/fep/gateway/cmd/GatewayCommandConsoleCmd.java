package com.syscom.fep.gateway.cmd;

public enum GatewayCommandConsoleCmd {
    YES("y", "Y(YES)"),
    NO("n", "N(NO)"),
    EXIT("e", "E(Exit)");

    private String input;
    private String description;

    private GatewayCommandConsoleCmd(String input, String description) {
        this.input = input;
        this.description = description;
    }

    public String getInput() {
        return this.input;
    }

    public String getDescription() {
        return this.description;
    }
}
