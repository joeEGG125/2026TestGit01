package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum  WebExceptionStatus {
    //
    // 摘要:
    //     未遇到任何錯誤。
    Success(0),
    //
    // 摘要:
    //     名稱解析服務未能解析主機名。
    NameResolutionFailure(1),
    //
    // 摘要:
    //     無法在傳輸級別聯系到遠程服務點。
    ConnectFailure(2),
    //
    // 摘要:
    //     無法從遠程服務器接收完整的響應。
    ReceiveFailure(3),
    //
    // 摘要:
    //     無法向遠程服務器發送完整的請求。
    SendFailure(4),
    //
    // 摘要:
    //     該請求是管線請求，并且連接未接收到響應即關閉。
    PipelineFailure(5),
    //
    // 摘要:
    //     取消了請求，調用了 System.Net.WebRequest.Abort 方法，或者發生了不可分類的錯誤。 這是 System.Net.WebException.Status
    //     的預設值。
    RequestCanceled(6),
    //
    // 摘要:
    //     從服務器接收的響應是完整的，但指示了一個協議級別的錯誤。 例如，HTTP 協議錯誤（如 401 訪問被拒絕）可能使用此狀態。
    ProtocolError(7),
    //
    // 摘要:
    //     連接過早關閉。
    ConnectionClosed(8),
    //
    // 摘要:
    //     無法驗證服務器證書。
    TrustFailure(9),
    //
    // 摘要:
    //     使用 SSL 建立連接時發生錯誤。
    SecureChannelFailure(10),
    //
    // 摘要:
    //     服務器響應不是有效的 HTTP 響應。
    ServerProtocolViolation(11),
    //
    // 摘要:
    //     指定 Keep-alive 標頭的請求連接意外關閉。
    KeepAliveFailure(12),
    //
    // 摘要:
    //     內部異步請求處于掛起狀態。
    Pending(13),
    //
    // 摘要:
    //     在請求的超時期限內未收到任何響應。
    Timeout(14),
    //
    // 摘要:
    //     名稱解析程序服務無法解析代理主機名。
    ProxyNameResolutionFailure(15),
    //
    // 摘要:
    //     出現未知類型的異常。
    UnknownError(16),
    //
    // 摘要:
    //     從服務器發送請求或接收響應時，接收到的消息超出指定限制。
    MessageLengthLimitExceeded(17),
    //
    // 摘要:
    //     未找到指定的緩存項。
    CacheEntryNotFound(18),
    //
    // 摘要:
    //     緩存策略不允許該請求。 一般而言，當請求不可緩存和有效策略禁止向服務器發送請求時會發生這種情況。 如果請求方法暗示請求正文存在，請求方法需要與服務器直接交互，或者請求包含條件標頭，則使用者可能會收到此狀態。
    RequestProhibitedByCachePolicy(19),
    //
    // 摘要:
    //     代理不允許此請求。
    RequestProhibitedByProxy(20);

    private int value;

    private WebExceptionStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static WebExceptionStatus fromValue(int value) {
        for (WebExceptionStatus e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static WebExceptionStatus parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (WebExceptionStatus e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
