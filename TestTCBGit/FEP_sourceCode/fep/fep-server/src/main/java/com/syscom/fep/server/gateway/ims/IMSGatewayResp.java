package com.syscom.fep.server.gateway.ims;

public class IMSGatewayResp {
    private IMSGatewayMode mode;
    private IMSGatewayCmdAction action;
    private boolean result;
    private String message;
    private Boolean isConnected;

    public IMSGatewayMode getMode() {
        return mode;
    }

    public void setMode(IMSGatewayMode mode) {
        this.mode = mode;
    }

    public IMSGatewayCmdAction getAction() {
        return action;
    }

    public void setAction(IMSGatewayCmdAction action) {
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

    public Boolean getIsConnected() {
        return isConnected;
    }

    public void setIsConnected(Boolean isConnected) {
        this.isConnected = isConnected;
    }
}
