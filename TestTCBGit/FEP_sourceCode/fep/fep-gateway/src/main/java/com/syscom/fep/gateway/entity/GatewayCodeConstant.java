package com.syscom.fep.gateway.entity;

public interface GatewayCodeConstant {

    public static final long FIXED_MILLISECONDS = 0L;

    public static final String ToATMChangeFirst = "0F0F0F00001201{0}1A0F"; // ATMGW要求ATM換Init Key
    public static final String FromATMChangeFirstPattern = "0F0F0F00001201[0-9]{6}1A0F"; // ATM回應ATMGW換Init Key指令
    public static final String ToATMChangeFirstKey = "0F0F0F00007601{0}1C0F{1}"; // ATMGW回應ATM Init Key

    public static final String ToATMChangeKey = "0F0F0F00001201{0}1B0F"; // ATMGW要求ATM換MAC KEY
    public static final String FromATMChangeKeyPattern = "0F0F0F00001201[0-9]{6}1B0F"; // ATM回應ATMGW換MAC KEY
    public static final String ToATMNewMacKey = "0F0F0F00004401{0}1D0F{1}"; // ATMGW回應ATM新MAC KEY

    public static final String FromATMMacDataPattern = "^0F0F0F00002801[0-9]{6}2A0F"; // ATM送出之押碼交易
    public static final String ToATMCorrectMacData = "0F0F0F00004401{0}2B0F{1}00000000"; // ATMGW檢查MAC正確送出之新MAC Key
    public static final String ToATMWrongMacData = "0F0F0F00004401{0}2C0F{1}00000000"; // ATMGW檢查MAC不正確送出之新MAC Key

    public static final String ToATMNoSecurityData = "0F0F0F00001201{0}4A0F"; // ATMGW送給ATM未通過ENC檢查之交易
    public static final String FromATMENCErrorPattern = "^0F0F0F00001201[0-9]{6}3A0F"; // ATM送給ATMGW ENC系統異常之交易

    public static final String FromATMKeepAliveData = "[0-9A-F]{2}KEEPALIVEREQ[0-9]{14}"; // ATM沒交易時會發keepalive電文過來送給ATMGW, 格式LLKEEPALIVEREQYYYYMMDDHHMMSS, LL是長度, YYYYMMDDHHMMSS是日期間
    public static final String ToATMKeepAliveData = "KEEPALIVERSP{0}"; // 回keepalive給ATM, LLKEEPALIVERSPYYYYMMDDHHMMSS, LL是長度, YYYYMMDDHHMMSS是日期間

    public static final String EXTENSION_BAK = ".bak";

    public static final String FISCGWKeepAliveRequest = "HELLO";
    public static final String FISCGWKeepAliveResponse = "HELLOOK";
}
