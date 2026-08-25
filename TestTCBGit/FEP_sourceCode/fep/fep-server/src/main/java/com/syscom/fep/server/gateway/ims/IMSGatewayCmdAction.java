package com.syscom.fep.server.gateway.ims;

public enum IMSGatewayCmdAction {
    start("啟動"), stop("停止"), check("查看狀態");

    private final String description;

    private IMSGatewayCmdAction(String description) {this.description = description;}

    public String getDescription() {return this.description;}
}
