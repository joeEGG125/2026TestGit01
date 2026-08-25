package com.syscom.fep.invoker.netty;

public enum SimpleNettyAttributeKey {
    /**
     * 收進來的消息
     */
    MessageIn,
    /**
     * 存儲MDC
     */
    MDCMap,
    /**
     * 是否拒絕客戶端連線
     */
    CHANNEL_REJECTED,
    /**
     * 是否不列印日誌
     */
    CHANNEL_LOGGING_DISABLE;
}
