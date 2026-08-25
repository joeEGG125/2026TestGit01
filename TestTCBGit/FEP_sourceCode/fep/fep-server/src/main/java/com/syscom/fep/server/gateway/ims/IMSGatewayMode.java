package com.syscom.fep.server.gateway.ims;

public enum IMSGatewayMode {
    primary("主要線路"),
    secondary("備援線路");

    private final String description;

    IMSGatewayMode(String description) {
        this.description = description;
    }

    public String getDescription() {return description;}
}
