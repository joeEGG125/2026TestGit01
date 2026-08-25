package com.syscom.fep.frmcommon.socket;

public enum SckConnState {
    CLIENT_CONNECTING("客戶端嘗試連線中..."),
    CLIENT_CONNECT_FAILED("客戶端嘗試連線失敗"),
    CLIENT_CONNECTED("客戶端連線中"),
    CLIENT_DISCONNECTED("客戶端已經斷線"),
    SERVER_MESSAGE_INCOMING("客戶端接收到服務端來的新電文"),
    SERVER_BINDING("服務端綁定中..."),
    SERVER_BOUND_FAILED("服務端綁定失敗"),
    SERVER_BOUND("服務端綁定中"),
    SERVER_NOT_BOUND("服務端未綁定"),
    CLIENT_MESSAGE_INCOMING("服務端接收到客戶端來的新電文");

    private String description;

    private SckConnState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
