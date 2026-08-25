package com.syscom.fep.gateway.netty.fisc;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.FISCEncoding;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.gateway.entity.Direction;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.LogKind;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

public class FISCGatewayUtil {
    private final static LogHelper txLogger = new LogHelper("FISCGatewayTxLogger");
    private final static LogHelper rsmLogger = new LogHelper("FISCGatewayRSMLogger");
    private final static LogHelper disconnectLogger = new LogHelper("FISCGatewayDisconnectLogger");

    private FISCGatewayUtil() {}

    /**
     * 交易電文的進出log,每個方向各記1筆, 檔名為fep-gateway-fisc-FISCGW-transaction-yyyy-MM-dd, 格式如下:
     * <p>
     * 第1欄位日期時間(到毫秒),
     * <p>
     * 第2欄為若是收送財金電文,則放該port對應的ClientID, 若為收送FEP電文, 收FEP顯示的收PORT, 送FEP 則顯示送的hostname
     * <p>
     * 第3欄為Send跟Receive, 從FISCGW送出為Send, FISCGW收進來為Receive
     * <p>
     * 第4欄以後為財金電文解析內容,財金電文去掉前6個0後取90byte為header , 直接將ebcdic轉ascii後取各欄位長度如下
     * <p>
     * 第4欄為MsgType
     * <p>
     * 第5欄為Pcode
     * <p>
     * 第6欄為Stan
     * <p>
     * 第7欄為轉入行, header.substring(15, 15+3)
     * <p>
     * 第8欄為轉出行, header.substring(18, 18+3)
     * <p>
     * 第9欄為交易日期時間, header.substring(21, 21+12)
     * <p>
     * 第10欄為RC, header.substring(33, 33+4)
     *
     * @param clientID
     * @param direction
     * @param message
     */
    public static void logTxMessage(String clientID, Direction direction, String message) {
        try {
            // 去掉000000
            String header = fromHex(message.substring(6, 90 + 6));
            // 取MsgType後兩碼判斷
            String msgType = header.substring(0, 4);
            String pcode = header.substring(4, 4 + 4);
            String stan = header.substring(8, 7 + 8);
            String trinBank = header.substring(15, 15 + 3);
            String troutBank = header.substring(22, 22 + 3);
            String txDate = header.substring(29, 29 + 12);
            String rc = header.substring(41, 41 + 4);
            LogMDC.put("logKind", LogKind.tx.getLogName());
            LogMDC.put("clientID", clientID);
            LogMDC.put("direction", direction.name());
            LogMDC.put("msgType", msgType);
            LogMDC.put("pcode", pcode);
            LogMDC.put("stan", stan);
            LogMDC.put("trinBank", trinBank);
            LogMDC.put("troutBank", troutBank);
            LogMDC.put("txDate", txDate);
            LogMDC.put("rc", rc);
            txLogger.info(StringUtils.EMPTY);
            FEPBase.clearMDC();
        } catch (DecoderException e) {
            LogHelperFactory.getTraceLogger().error(e, e.getMessage());
        }
    }


    /**
     * 判斷若收到財金的電文是RSM
     * if (message.length() == 40 && RSM_ID.equals(message.substring(8, 16 + 8)))
     * <p>
     * 則記錄以下內容至檔名為fep-gateway-fisc-FISCGW-RSM-yyyy-MM-dd, 格式如下:
     * <p>
     * 第1欄位日期時間(到毫秒),
     * <p>
     * 第2欄是ReturnCode 取message.substring(30, 2 + 30)
     * <p>
     * 第3欄是ReasonCode 取message.substring(38, 2 + 38)
     *
     * @param message
     */
    public static void logRSMMessage(String message) {
        String returnCode = message.substring(30, 2 + 30);
        String reasonCode = message.substring(38, 2 + 38);
        LogMDC.put("logKind", LogKind.rsm.getLogName());
        LogMDC.put("returnCode", returnCode);
        LogMDC.put("reasonCode", reasonCode);
        rsmLogger.info(StringUtils.join(StringUtils.SPACE, message)); // 2024-07-30 Richard modified for 最後一欄顯示RSM電文
        FEPBase.clearMDC();
    }

    /**
     * 若發生斷線, 則記錄一筆至fep-gateway-fisc-FISCGW-disconnect-yyyy-MM-dd,格式如下:
     * <p>
     * 第1欄位日期時間(到毫秒),
     * <p>
     * 第2欄位為該port對應的ClientID
     * <p>
     * 第3欄位為字串”IP:Port disconnected!” ,IP跟Port為財金的IP與Port
     *
     * @param clientId
     * @param host
     * @param port
     */
    public static void logDisconnectMessage(String clientId, String host, int port) {
        LogMDC.put("logKind", LogKind.disconn.getLogName());
        LogMDC.put("clientID", clientId);
        LogMDC.put("ip", host);
        LogMDC.put("port", Integer.toString(port));
        disconnectLogger.info(StringUtils.EMPTY);
        FEPBase.clearMDC();
    }

    /**
     * 根據${spring.fep.server.encoding.fisc}的設定值是否用EBCDIC還是ASCII的方式轉換
     *
     * @param hex
     * @return
     * @throws DecoderException
     */
    public static String fromHex(String hex) throws DecoderException {
        if (FEPConfig.getInstance().getFiscencoding() == FISCEncoding.ebcdic) {
            return EbcdicConverter.fromHex(CCSID.English, hex);
        }
        return StringUtil.fromHex(hex);
    }
}
