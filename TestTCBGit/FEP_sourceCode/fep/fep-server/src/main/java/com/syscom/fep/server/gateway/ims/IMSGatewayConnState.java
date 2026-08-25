package com.syscom.fep.server.gateway.ims;

public enum IMSGatewayConnState {
    SHUTTING_DOWN("終止連線關閉中"),
    SHUT_DOWN("已經終止連線並關閉"),
    READY_TO_RUN("準備開始運行..."),
    RUNNING("運行中..."),
    CONNECTING("嘗試連線中..."),
    CONNECTING_FAILED("嘗試連線失敗"),
    CONNECTED("連線中"),
    CONNECTED_IDLE("連線中(IDLE)"),
    DISCONNECTING("嘗試斷線中..."),
    DISCONNECTING_FAILED("嘗試斷線失敗"),
    DISCONNECTED("已經斷線"),
    DISCONNECTED_ON_EXCEPTION_OCCUR("因為出現異常導致斷線"),
    MESSAGE_INCOMING("接收到服務端來的新電文");

    private String description;

    private IMSGatewayConnState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isConnected(IMSGatewayConnState state) {
        return state == CONNECTED || state == MESSAGE_INCOMING || state == CONNECTED_IDLE;
    }
}
