package com.syscom.fep.invoker.netty;

public enum SimpleNettyConnState {
    // Client Side
    CLIENT_CONNECTING("客戶端嘗試連線中..."),
    CLIENT_CONNECTING_FAILED("客戶端嘗試連線失敗"),
    CLIENT_CONNECTED("客戶端連線中"),
    CLIENT_CONNECTED_IDLE("客戶端連線中(IDLE)"),
    CLIENT_DISCONNECTED("客戶端已經斷線"),
    CLIENT_MESSAGE_INCOMING("服務端接收到客戶端來的新電文"),
    CLIENT_SHUTTING_DOWN("客戶端終止連線關閉中"),
    CLIENT_SHUT_DOWN("客戶端已經終止連線並關閉"),
    CLIENT_SYSTEM_EXIT("客戶端終止程序"),
    CLIENT_READY_TO_RUN("客戶端準備開始運行..."),
    CLIENT_RUNNING("客戶端運行中..."),
    // Server Side
    SERVER_BINDING("服務端綁定中..."),
    SERVER_BIND_FAILED("服務端綁定失敗"),
    SERVER_BOUND("服務端綁定中"),
    SERVER_UNBINDING("服務端解除綁定中..."),
    SERVER_UNBINDING_FAILED("服務端解除綁定失敗"),
    SERVER_NOT_BOUND("服務端未綁定"),
    SERVER_NOT_BOUND_ON_EXCEPTION_OCCUR("服務端出現異常導致未綁定"),
    SERVER_SHUTTING_DOWN("服務端終止運行關閉中"),
    SERVER_SHUT_DOWN("服務端終止運行並關閉"),
    SERVER_MESSAGE_INCOMING("客戶端接收到服務端來的新電文"),
    SSL_CERTIFICATE_ACCEPT("SSL憑證檔已應用"),
    CHANNEL_CLOSE_BY_MANUAL("手動關閉通道");

    private String description;

    private SimpleNettyConnState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isClientConnected(SimpleNettyConnState state) {
        return state == CLIENT_CONNECTED || state == SERVER_MESSAGE_INCOMING || state == CLIENT_CONNECTED_IDLE || state == CLIENT_MESSAGE_INCOMING;
    }
}
