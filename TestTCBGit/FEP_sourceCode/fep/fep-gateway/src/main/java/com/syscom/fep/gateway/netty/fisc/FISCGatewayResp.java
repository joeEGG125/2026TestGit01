package com.syscom.fep.gateway.netty.fisc;

public class FISCGatewayResp {
    private FISCGatewayMode mode;
    private FISCGatewayCmdAction action;
    private boolean result;
    private String message;

    public FISCGatewayMode getMode() {
        return mode;
    }

    public void setMode(FISCGatewayMode mode) {
        this.mode = mode;
    }

    public FISCGatewayCmdAction getAction() {
        return action;
    }

    public void setAction(FISCGatewayCmdAction action) {
        this.action = action;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
