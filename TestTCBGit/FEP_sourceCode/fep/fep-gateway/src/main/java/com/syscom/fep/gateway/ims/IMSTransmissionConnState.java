package com.syscom.fep.gateway.ims;

public enum IMSTransmissionConnState {
    CLIENT_CONNECTING("客戶端嘗試連線中..."),
    CLIENT_CONNECTING_FAILED("客戶端嘗試連線失敗"),
    CLIENT_CONNECTED("客戶端連線中"),
    CLIENT_CONNECTED_IDLE("客戶端連線中(IDLE)"),
    CLIENT_DISCONNECTING("客戶端嘗試斷線中..."),
    CLIENT_DISCONNECTING_FAILED("客戶端嘗試斷線失敗"),
    CLIENT_DISCONNECTED("客戶端已經斷線"),
    CLIENT_SHUTTING_DOWN("客戶端終止連線關閉中"),
    CLIENT_SHUT_DOWN("客戶端已經終止連線並關閉"),
    CLIENT_SYSTEM_EXIT("客戶端終止程序"),
    CLIENT_READY_TO_RUN("客戶端準備開始運行..."),
    CLIENT_RUNNING("客戶端運行中..."),
    CLIENT_PROCESS_WITH_EXCEPTION_OCCUR("客戶端處理出現異常"),
    SERVER_MESSAGE_INCOMING("客戶端接收到服務端來的新電文");

    private final String description;

    IMSTransmissionConnState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isClientConnected(IMSTransmissionConnState state) {
        return state == CLIENT_CONNECTED || state == SERVER_MESSAGE_INCOMING || state == CLIENT_CONNECTED_IDLE;
    }
}
