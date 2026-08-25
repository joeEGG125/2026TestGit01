package com.syscom.fep.web.base;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.ext.mapper.BctlExtMapper;
import com.syscom.fep.mybatis.model.Cardtype;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.entity.SessionKey;
import com.syscom.fep.web.entity.User;
import com.syscom.fep.web.entity.WebCodeConstant;
import com.syscom.fep.web.interceptor.WebAuditInterceptor;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.util.Date;

public class FEPWebBase extends FEPBase implements WebCodeConstant {
    private static final LogHelper logger = LogHelperFactory.getFEPWebMessageLogger();
    // protected static final ThreadLocal<AuditLog> auditLogThreadLocal = new ThreadLocal<>();

    /**
     * 這裡塞入AuditLog物件, 然後在{@link WebAuditInterceptor#postHandle}取得
     *
     * @param auditLog
     */
    protected void setAuditLog(AuditLog auditLog) {
        if (WebConfiguration.getInstance().isAuditEnable())
            // auditLogThreadLocal.set(auditLog);
            WebUtil.putInSession(SessionKey.AuditLog, auditLog);
    }

    /**
     * 取出當前塞入的AuditLog物件
     *
     * @return
     */
    protected AuditLog getAuditLog() {
        if (WebConfiguration.getInstance().isAuditEnable())
            // return auditLogThreadLocal.get();
            return WebUtil.getFromSession(SessionKey.AuditLog);
        return null;
    }

    /**
     * 將資料庫Char(6)/Char(8)日期欄位加入/
     *
     * @param charDate
     * @param delimiter <history>
     *                  <Add>Daniel</Add>
     *                  <reason>For web page show Date</reason>
     *                  <date>2010/3/29</date>
     *                  </history>
     */
    public static String charDateToDate(Object charDate, String... delimiter) {
        if (charDate != null) {
            String dateStr = charDate.toString().trim();
            String separator = "/";
            if (ArrayUtils.isNotEmpty(delimiter)) {
                separator = delimiter[0];
            }
            if (dateStr.length() == 8) {
                return StringUtils.join(dateStr.substring(0, 4), separator, dateStr.substring(4, 6), separator, dateStr.substring(6, 8));
            } else if (dateStr.length() == 6) {
                return StringUtils.join(dateStr.substring(0, 2), separator, dateStr.substring(2, 4), separator, dateStr.substring(4, 6));
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * 將資料庫Char(6)時間欄位加入:
     *
     * @param charTime <history>
     *                 <Add>Daniel</Add>
     *                 <reason>For web page show Time</reason>
     *                 <date>2010/3/29</date>
     *                 </history>
     */
    public static String charTimeToTime(Object charTime) {
        if (charTime != null) {
            String timeStr = charTime.toString().trim();
            if (timeStr.length() == 6) {
                return StringUtils.join(timeStr.substring(0, 2), ":" + timeStr.substring(2, 4), ":", timeStr.substring(4, 6));
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * PENDING field to Name
     *
     * @param field <history>
     *              <Add>henny</Add>
     *              <reason>For web page show PENDING field</reason>
     *              <date>2010/6/18</date>
     *              </history>
     */
    public static String getPendingName(Object field) {
        if (field != null) {
            switch (field.toString()) {
                case "2":
                    return "解除PENDING";
                case "1":
                    return "PENDING";
                case "0":
                    return "正常";
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * FISC_TIMEOUT field to Name
     *
     * @param field <history>
     *              <Add>henny</Add>
     *              <reason>For web page show FISC_TIMEOUT field</reason>
     *              <date>2010/6/18</date>
     *              </history>
     */
    public static String getFiscTimeoutName(Object field) {
        if (field != null) {
            switch (field.toString()) {
                case "True":
                    return "逾時";
                case "False":
                    return "正常";
            }
        }
        return StringUtils.EMPTY;
    }

    public static String getMsgFlowName(Object field) {
        if (field != null) {
            // A1= ATM REQUEST, A2= ATM RESPONSE, A3= ATM CONFIRM, F1= FISC REQUEST, F2= FISC RESPONSE, F3= FISC CONFIRM, F9= FISC 2290 INQ,
            // H1= CBS REQUEST, H2= CBS RESPONSE, I1= CBS INQ REQUEST, I2= CBS INQ RESPONSE, X1= CBS EC REQUEST, X2= CBS EC RESPONSE,
            // N1=CBS CR REQUEST, N2=CBS CR RESPONSE,
            // C1= ASC TO REQUEST, C2= ASC TO RESPONSE, C3= ASC CR/EC TO REQUEST, C4= ASC CR/EC TO RESPONSE,CA: ASC FROM REQUEST,
            // CB: ASC FROM RESPONSE, CC:ASC CR/EC FROM REQUEST, CD: ASC CR/EC FROM RESPONSE,
            // B1=BRS Request, B2=BRS Response
            // for 消費扣款
            switch (field.toString()) {
                case "A1":
                    return "A1:ATM REQUEST";
                case "A2":
                    return "A2:ATM RESPONSE";
                case "A3":
                    return "A3:ATM CONFIRM";
                // Fly 2018/11/09 增加說明
                case "A4":
                    return "A4:ATM CONFIRM RESPONSE";
                case "F1":
                    return "F1:FISC REQUEST";
                case "F2":
                    return "F2:FISC RESPONSE";
                case "F3":
                    return "F3:FISC CONFIRM";
                case "F9":
                    return "F9:FISC 2290 INQ";
                case "H1":
                    return "H1:CBS REQUEST";
                case "H2":
                    return "H2:CBS RESPONSE";
                case "I1":
                    return "I1:CBS INQ REQUEST";
                case "I2":
                    return "I2:CBS INQ RESPONSE";
                case "X1":
                    return "X1:CBS EC REQUEST";
                case "X2":
                    return "X2:CBS EC RESPONSE";
                case "N1":
                    return "N1:CBS CR REQUEST";
                case "N2":
                    return "N2:CBS CR RESPONSE";
                case "C1":
                    return "C1:ASC TO REQUEST";
                case "C2":
                    return "C2:ASC TO RESPONSE";
                case "C3":
                    return "C3:ASC CR/EC TO REQUEST";
                case "C4":
                    return "C4:ASC CR/EC TO RESPONSE";
                case "CA":
                    return "CA:ASC FROM REQUEST";
                case "CB":
                    return "CB:ASC FROM RESPONSE";
                case "CC":
                    return "CC:ASC CR/EC FROM REQUEST";
                case "CD":
                    return "CD:ASC CR/EC FROM RESPONSE";
                case "B1":
                    return "B1:BRS Request";
                case "B2":
                    return "B2:ASC TO RESPONSE";
                case "K1":
                    return "K1:AML_RequestE";
                case "K2":
                    return "K2:AML_Response";
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * char field Null to ''
     *
     * @param charField
     * @return
     */
    public static String nullToEmptyStr(Object charField) {
        if (charField == null || "null".equals(charField)) {
            return StringUtils.EMPTY;
        } else {
            return charField.toString();
        }
    }

    /**
     * Format 日期/時間
     *
     * @param sDateTime <history>
     *                  <Add>Anna Lin</Add>
     *                  <reason>Format 日期/時間</reason>
     *                  <date>2010/5/12</date>
     *                  <modifier>Anna Lin</modifier>
     *                  <reason>Format 日期/時間</reason>
     *                  <date>2010/5/21</date>
     *                  </history>
     */
    public static String formatYMDHMS(String sDateTime) {
        if (sDateTime == null) {
            return StringUtils.EMPTY;
        }
        if (sDateTime.length() == 15) {
            String yy = sDateTime.substring(0, 4);
            String mth = sDateTime.substring(4, 6);
            String dd = sDateTime.substring(6, 8);
            String hh = sDateTime.substring(9, 11);
            String mm = sDateTime.substring(11, 13);
            String ss = sDateTime.substring(13, 15);
            return StringUtils.join(yy, "/", mth, "/", dd, " ", hh, ":", mm, ":", ss);
        } else if (sDateTime.length() == 14) {
            String yy = sDateTime.substring(0, 4);
            String mth = sDateTime.substring(4, 6);
            String dd = sDateTime.substring(6, 8);
            String hh = sDateTime.substring(8, 10);
            String mm = sDateTime.substring(10, 12);
            String ss = sDateTime.substring(12, 14);
            return StringUtils.join(yy, "/", mth, "/", dd, " ", hh, ":", mm, ":", ss);
        }
        return sDateTime;
    }

    /**
     * Format 日期/時間
     *
     * @param sDateTime <history>
     *                  <Add>Anna Lin</Add>
     *                  <reason>Format 日期/時間</reason>
     *                  <date>2010/5/12</date>
     *                  <modifier>Anna Lin</modifier>
     *                  <reason>Format 日期/時間</reason>
     *                  <date>2010/5/21</date>
     *                  </history>
     */
    public static String formatYMDHM(String sDateTime) {
        if (sDateTime == null) {
            return StringUtils.EMPTY;
        }
        if (sDateTime.length() == 8) {
            String yy = sDateTime.substring(0, 4);
            String mth = sDateTime.substring(4, 6);
            String dd = sDateTime.substring(6, 8);
            return StringUtils.join(yy, "/", mth, "/", dd);
        } else if (sDateTime.length() == 6) {
            String yy = sDateTime.substring(0, 2);
            String mth = sDateTime.substring(2, 4);
            String dd = sDateTime.substring(4, 6);
            return StringUtils.join(yy, "/", mth, "/", dd);
        } else if (sDateTime.length() == 12) {
            String yy = sDateTime.substring(0, 4);
            String mth = sDateTime.substring(4, 6);
            String dd = sDateTime.substring(6, 8);
            String hh = sDateTime.substring(8, 10);
            String mm = sDateTime.substring(10, 12);
            return StringUtils.join(yy, "/", mth, "/", dd, " ", hh, ":", mm);
        }
        return sDateTime;
    }

    /**
     * Format 日期/時間
     *
     * @param sDate
     * @return
     */
    public static String formatYMD(String sDate) {
        if (sDate != null) {
            sDate = sDate.trim();
            if (sDate.toString().length() == 8) {
                String yy = sDate.substring(0, 4);
                String mth = sDate.substring(4, 6);
                String dd = sDate.substring(6, 8);
                return StringUtils.join(yy, "/", mth, "/", dd);
            } else if (sDate.toString().length() == 10) {
                return sDate;
            } else {
                return sDate;
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Bruce add Format 日期/時間
     *
     * @param sDate
     * @return
     */
    public static String formatHMS(String sDate) {
        if (sDate != null) {
            sDate = sDate.trim();
            if (sDate.toString().length() == 6) {
                String hh = sDate.substring(0, 2);
                String mm = sDate.substring(2, 4);
                String ss = sDate.substring(4, 6);
                return StringUtils.join(hh, ":", mm, ":", ss);
            } else if (sDate.toString().length() == 10) {
                return sDate;
            } else {
                return sDate;
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * 拼接成字串
     *
     * @param separator
     * @param fields
     * @return
     */
    public static String join(String separator, Object... fields) {
        if (!ArrayUtils.isEmpty(fields)) {
            StringBuffer sb = new StringBuffer();
            for (Object field : fields) {
                if (field == null) {
                    continue;
                } else if (StringUtils.isBlank(field.toString())) {
                    continue;
                }
                sb.append(field.toString()).append(separator);
            }
            if (sb.length() > 0 && separator != null) {
                sb.delete(sb.length() - separator.length(), sb.length());
            }
            return sb.toString();
        }
        return StringUtils.EMPTY;
    }

    /**
     * UI060550需要顯示Reply_Code一致格式給使用者
     *
     * @param field
     * @return
     */
    public static String formatExpCd(Object field) {
        if (field != null) {
            return StringUtils.leftPad(field.toString(), 4, '0');
        }
        return StringUtils.EMPTY;
    }

    /**
     * UI060550需要對應AARC中文訊息給使用者
     *
     * @param field
     * @return
     */
    public static String mapMsgFromAaRc(Object field) {
        if (field != null) {
            try {
                FEPReturnCode returnCode = FEPReturnCode.parse(field);
                return StringUtils.join(field.toString(), ":", TxHelper.getMessageFromFEPReturnCode(returnCode));
            } catch (IllegalArgumentException e) {
                return "error";
            }
        }
        return StringUtils.EMPTY;
    }

    public static String mapMsgFromFISCRc(Object field) {
        if (field != null) {
            try {
                String fiscCode = String.valueOf(field);
                fiscCode = StringUtils.leftPad(fiscCode, 4, "0");
                return TxHelper.getMessageFromFEPReturnCode(fiscCode, FEPChannel.FISC);
            } catch (IllegalArgumentException e) {
                return "error";
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * 將DB中Short類型的字段, 轉為布爾形態
     *
     * @param field
     * @return
     */
    public static String toBoolean(Object field) {
        if (field != null) {
            if (field instanceof Number) {
                return DbHelper.toBoolean(((Number) field).shortValue()).toString();
            } else if (field instanceof String) {
                return Boolean.valueOf((String) field).toString();
            }
        }
        return Boolean.FALSE.toString();
    }

    public static String isPcodeShowed(Short fiscflag, String pcode) {
        if (DbHelper.toBoolean(fiscflag)) {
            return pcode;
        }
        return StringUtils.EMPTY;
    }

    public static String getTypeName(Object field) {
        if (field != null) {
            switch (String.valueOf(field)) {
                case "0":
                    return "0:未記帳";
                case "1":
                    return "1:已記帳";
                case "2":
                    return "2:已沖正";
                case "3":
                    return "3:沖正/轉入失敗";
                case "4":
                    return "4:未明";
                case "5":
                    return "5:待解";
            }
        }
        return StringUtils.EMPTY;
    }

    public static String shortRemark(String remark) {
        if (remark != null) {
            String oriStr = remark.trim();
            String result = "";
            // 2011-04-01 by kyo 修改for 當remark太長的時候，gridview縮短顯示
            if (StringUtils.isNotBlank(oriStr) && oriStr.length() > 100) {
                try {
                    if (oriStr.contains("TxErrDesc")) {
                        int startIndex = oriStr.indexOf("TxErrDesc&gt;") + "TxErrDesc&gt;".length();
                        int endIndex = oriStr.indexOf("&lt;/TxErrDesc");
                        result = oriStr.substring(startIndex, endIndex);
                    } else {
                        result = StringUtils.join(oriStr.substring(0, 99), "...");
                    }
                } catch (Exception e) {
                    result = remark;
                }
                return result;
            }
        }
        return remark;
    }

    /**
     * CARD_ACTNO Substring
     *
     * @param field
     * @return
     */
    public static String formatCardActNo(Object field) {
        if (field != null) {
            // 2010/6/13 modify by Daniel 增加不足長度的字串判斷
            if (field.toString().length() >= 16) {
                return StringUtils.join(
                        field.toString().substring(0, 2),
                        "-", field.toString().substring(2, 5),
                        "-", field.toString().substring(5, 8),
                        "-", field.toString().substring(8, 15),
                        "-", field.toString().substring(15));
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * 日期format為yyyy/MM/dd HH:mm:ss.SSS
     *
     * @param o
     * @return
     */
    public static String dateTimeToSDateTime(Object o) {
        return StringUtils.join(dateTimeToChrDate(o), StringUtils.SPACE, dateTimeToChrTime(o));

    }

    /**
     * 日期format為yyyy/MM/dd
     *
     * @param o
     * @return
     */
    public static String dateTimeToChrDate(Object o) {
        if (o != null && o instanceof Date) {
            return FormatUtil.dateFormat((Date) o);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 時間format為HH:mm:ss.SSS
     *
     * @param o
     * @return
     */
    public static String dateTimeToChrTime(Object o) {
        if (o != null && o instanceof Date) {
            return FormatUtil.timeInMillisFormat((Date) o);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 檢查身份證字號
     *
     * @param oSrc
     * @return
     */
    protected boolean utl_CheckId(String oSrc) {
        boolean UTL_CheckId = true;
        try {
            String strUserId = null; // 身份證字號
            int intX = 0; // 計數變數
            int lngAreaNo = 0; // 區域碼變數
            long lngCheckSum = 0; // 檢核碼變數
            String strAreaCode = null; // 區域碼變數
            strUserId = oSrc;
            // 確定身分證號不為空白或長度為10碼
            if (StringUtils.isBlank(strUserId) || strUserId.length() != 10) {
                UTL_CheckId = false;
                return UTL_CheckId;
            }
            strUserId = strUserId.toUpperCase();
            strAreaCode = strUserId.substring(0, 1);
            // 確定首碼在 A-Z 之間。
            if (strAreaCode.compareTo("A") < 0 || strAreaCode.compareTo("Z") > 0) {
                UTL_CheckId = false;
                return UTL_CheckId;
            }
            // 確定 2-10 碼是數字。
            if (!StringUtils.isNumeric(strUserId.substring(1))) {
                UTL_CheckId = false;
                return UTL_CheckId;
            }
            // 取得首碼對應的區域碼，A ->10, B->11, ..H->17,I->34, J->18...
            // 原先明祥寫的是ABCDEFGHJKLMNPQRSTUVWXYZIO,正確的是ABCDEFGHJKLMNPQRSTUVXYWZIO.
            lngAreaNo = "ABCDEFGHJKLMNPQRSTUVXYWZIO".indexOf(strAreaCode) + 1 + 9;
            strUserId = StringUtils.join(String.valueOf(lngAreaNo), strUserId.substring(1));
            // 取得 lngCheckSum 的值
            lngCheckSum = Long.parseLong(strUserId.substring(0, 1)) + Long.parseLong(strUserId.substring(10, 11));
            for (intX = 2; intX <= 10; intX++) {
                lngCheckSum = lngCheckSum + Long.parseLong(strUserId.substring(intX - 1, intX - 1 + 1)) * (11 - intX);
            }
            if (lngCheckSum % 10 > 0) {
                UTL_CheckId = false;
            }
        } catch (Exception e) {
            return false;
        }
        return UTL_CheckId;
    }

    /**
     * TxMessage 每一列HEX和每一列的ASCII要對應
     *
     * @param data
     * @return
     */
    public static String txMessageNLine(String data) {
        String hexStr = data;
        String result = StringUtils.EMPTY;
        int i = 0;
        try {
            if (StringUtils.isNotBlank(hexStr) && StringUtil.isHex(hexStr)) {
                // 若為 HEX 則每行 32 chars
                for (i = 0; i < hexStr.length(); i += 32) {
                    if (i + 32 <= hexStr.length()) {
                        result = StringUtils.join(result, hexStr.substring(i, i + 32), "\r\n");
                    } else {
                        result = StringUtils.join(result, hexStr.substring(i, hexStr.length()), "\r\n");
                    }
                    if (i + 32 >= hexStr.length()) {
                        break;
                    }
                }
                return result;
            } else {
                // 非 HEX 每行 16 chars
                for (i = 0; i < hexStr.length(); i += 16) {
                    if (i + 16 <= hexStr.length()) {
                        result = StringUtils.join(result, hexStr.substring(i, i + 16), "\r\n");
                    } else {
                        result = StringUtils.join(result, hexStr.substring(i, hexStr.length()), "\r\n");
                    }
                    if (i + 16 >= hexStr.length()) {
                        break;
                    }
                }
                return result;
            }
        } catch (Exception e) {
            return "error";
        }
    }

    /**
     * 原 data 若有換行字元, 以空白取代(0x0D==>0x00)
     *
     * @param data
     * @return
     */
    public static String getAsciiStringFromHex(String data) {
        String hexStr = data == null ? null : data.trim();
        String result = StringUtils.EMPTY;
        int i = 0;
        String tmp = StringUtils.EMPTY;
        String newHexStr = StringUtils.EMPTY;
        // 2011-03-30 by kyo 修改for HEX轉ASCII 改判斷是否為HEX而不是用長度去mod
        if (hexStr != null && StringUtils.isNotBlank(hexStr) && StringUtil.isHex(hexStr)) {
            try {
                for (i = 0; i < hexStr.length(); i += 2) {
                    if (i > hexStr.length() - 1) {
                        break;
                    }
                    tmp = StringUtils.join(hexStr.substring(i, i + 1), hexStr.substring(i + 1, i + 1 + 1));
                    if ("0D".equals(tmp)) {
                        newHexStr = StringUtils.join(newHexStr, "00");
                    } else {
                        newHexStr = StringUtils.join(newHexStr, tmp);
                    }
                }
                result = StringUtil.fromHex(newHexStr);
            } catch (Exception e) {
                return "error";
            }
            return result;
        }
        return data;
    }

    public static String getAsciiStringFromEbcdic(String data) {
        String hexStr = data == null ? null : data.trim();
        String result = StringUtils.EMPTY;
        int i = 0;
        String tmp = StringUtils.EMPTY;
        String newHexStr = StringUtils.EMPTY;
        // 2011-03-30 by kyo 修改for HEX轉ASCII 改判斷是否為HEX而不是用長度去mod
        if (hexStr != null && StringUtils.isNotBlank(hexStr) && StringUtil.isHex(hexStr)) {
            try {
                for (i = 0; i < hexStr.length(); i += 2) {
                    if (i > hexStr.length() - 1) {
                        break;
                    }
                    tmp = StringUtils.join(hexStr.substring(i, i + 1), hexStr.substring(i + 1, i + 1 + 1));
                    if ("0D".equals(tmp)) {
                        newHexStr = StringUtils.join(newHexStr, "00");
                    } else {
                        newHexStr = StringUtils.join(newHexStr, tmp);
                    }
                }
                result = EbcdicConverter.fromHex(CCSID.English, newHexStr);
            } catch (Exception e) {
                return "error";
            }
            return result;
        }
        return data;
    }

    protected void infoMessage(Object... msgs) {
        this.logMessage(Level.INFO, msgs);
    }

    protected void debugMessage(Object... msgs) {
        this.logMessage(Level.DEBUG, msgs);
    }

    protected void warnMessage(Object... msgs) {
        this.warnMessage(null, msgs);
    }

    protected void warnMessage(Throwable t, Object... msgs) {
        this.logWebMessage(Level.WARN, t, msgs);
    }

    protected void errorMessage(Object... msgs) {
        this.errorMessage(null, msgs);
    }

    protected void errorMessage(Throwable t, Object... msgs) {
        this.logWebMessage(Level.ERROR, t, msgs);
    }

    protected void traceMessage(Object... msgs) {
        this.logMessage(Level.TRACE, msgs);
    }

    private void logMessage(Level level, Object... msgs) {
        this.logWebMessage(level, null, msgs);
    }

    private void logWebMessage(Level level, Throwable t, Object... msgs) {
        setLogContextField();
        switch (level) {
            case ERROR:
                if (t == null) {
                    logger.error(msgs);
                } else {
                    logger.exceptionMsg(t, msgs);
                }
                break;
            case INFO:
                logger.info(msgs);
                break;
            case DEBUG:
                logger.debug(msgs);
                break;
            case WARN:
                if (t == null) {
                    logger.warn(msgs);
                } else {
                    logger.warn(t, msgs);
                }
                break;
            case TRACE:
                logger.trace(msgs);
                break;
        }
        clearMDC();
    }

    private void setLogContextField() {
        User user = WebUtil.getUser();
        if (user != null) {
            LogMDC.put(Const.MDC_WEB_LOGINID, user.getLoginId());
            LogMDC.put(Const.MDC_WEB_USERID, user.getUserId());
            LogMDC.put(Const.MDC_WEB_REMOTE_IP, user.getSrcIp());
            if (user.getSelectedMenu() != null) {
                LogMDC.put(Const.MDC_WEB_MENU_NAME, user.getSelectedMenu().getName());
                LogMDC.put(Const.MDC_WEB_MENU_VIEW, user.getSelectedMenu().getView());
                LogMDC.put(Const.MDC_WEB_MENU_URL, user.getSelectedMenu().getUrl());
            }
        }
    }

    protected String strDataIsEmpty(String strData, String str) {
        String dataStr = "";
        if (StringUtils.isBlank(strData)) {
            return "";
        } else {
            dataStr = strData.replace(str, "").trim();
            if ((dataStr == null ? 0 : dataStr.length()) == 0) {
                return "";
            } else {
                return strData;
            }
        }
    }

    protected String getSoctName(Object field) {
        if (field == null) {
            return "";
        } else {
            switch (field.toString()) {
                case "0":
                    return "日終 House Keeping 完成";
                case "1":
                    return "日初 House Keeping 完成";
                case "9":
                    return "關機";
                default:
                    return "";
            }
        }
    }

    protected String getKeystName(Object field) {
        if (field == null) {
            return "";
        } else {
            switch (field.toString()) {
                case "1":
                    return "換Key中";
                case "0":
                    return "正常";
                default:
                    return "";
            }
        }
    }

    protected String getAoctName(Object field) {
        if (field == null) {
            return "";
        } else {
            switch (field.toString()) {
                case "0":
                    return "日終 House Keeping 完成";
                case "1":
                    return "日初 House Keeping 完成";
                case "2":
                    return "財金公司AP停止(Stop)作業";
                case "A":
                    return "財金公司通匯'匯出類'STOP SERVICE完成";
                case "B":
                    return "財金公司通匯'匯入類'STOP SERVICE完成";
                case "3":
                    return "AP 預定Checkout, 匯入類仍可處理(匯入類未Exceptional checkout) (AP Pre-checkout交易由參加單位啟動)";
                case "4":
                    return "AP 預定Checkout, 匯入類仍可處理(匯入類未Exceptional checkout) (AP Pre-checkout交易由財金公司啟動)";
                case "C":
                    return "AP 預定Checkout, 匯入類仍可處理(匯入類為Exceptional checkout狀態) (AP Pre-checkout交易由參加單位啟動)";
                case "D":
                    return "AP 預定Checkout, 匯入類仍可處理(匯入類為Exceptional checkout狀態) (AP Pre-checkout交易由財金公司啟動)";
                case "5":
                    return "AP Checkout(由參加單位啟動)";
                case "7":
                    return "AP Checkout(由財金公司啟動)";
                case "9":
                    return "財金公司於結清算帳時停止作業";
                default:
                    return "";
            }
        }
    }

    protected String getMboctName(Object field) {
        if (field == null) {
            return "";
        } else {
            switch (field.toString()) {
                case "0":
                    return "日終 House Keeping 完成";
                case "1":
                    return "日初 House Keeping 完成";
                case "2":
                    return "FISC Key-Syn Call完成";
                case "3":
                    return "FISC Notice Call完成";
                case "9":
                    return "參加單位關機交易完成";
                case "D":
                    return "參加單位不營業";
                default:
                    return "";
            }
        }
    }

    protected String getMbactName(Object field) {
        if (field == null) {
            return "";
        } else {
            switch (field.toString()) {
                case "0":
                    return "日終 House Keeping 完成";
                case "1":
                    return "AP Checkin";
                case "2":
                    return "AP Exceptional Checkout";
                case "A":
                    return "通匯匯出類作業Exceptional Checkout完成";
                case "B":
                    return "通匯匯入類作業Exceptional Checkout完成";
                case "3":
                    return "AP 預定Checkout, 匯入類仍可處理(匯入類未Exceptional checkout) (AP Pre-checkout交易由參加單位啟動)";
                case "4":
                    return "AP 預定Checkout, 匯入類仍可處理(匯入類未Exceptional checkout) (AP Pre-checkout交易由財金公司啟動)";
                case "C":
                    return "AP 預定Checkout, 匯入類仍可處理(匯入類為Exceptional checkout狀態) (AP Pre-checkout交易由參加單位啟動)";
                case "D":
                    return "AP 預定Checkout, 匯入類仍可處理(匯入類為Exceptional checkout狀態) (AP Pre-checkout交易由財金公司啟動)";
                case "5":
                    return "AP Checkout(由參加單位啟動)";
                case "7":
                    return "AP Checkout(由財金公司啟動)";
                case "9":
                    return "Evening Call(由參加單位啟動)";
                default:
                    return "";
            }
        }
    }

    protected String getRMOUTStatusName(Object field) {
        if (field == null) {
            return "";
        } else {
            switch (field.toString()) {
                case "01":
                    return "已登錄";
                case "02":
                    return "待補登";
                case "03":
                    return "待放行";
                case "04":
                    return "已放行";
                case "05":
                    return "傳送中";
                case "06":
                    return "已解款";
                case "07":
                    return "財金拒絕";
                case "08":
                    return "退匯";
                case "09":
                    return "已刪除未更帳";
                case "10":
                    return "已刪除已更帳";
                case "11":
                    return "磁片整批匯出失敗";
                case "12":
                    return "緊急匯款已更帳";
                case "13":
                    return "已刪除";
                case "14":
                    return "同業代匯";
                case "99":
                    return "系統問題";
                default:
                    return "";
            }
        }
    }

    protected String getRMBTCH_DATA_FLAGName(Object field) {
        if (field == null) {
            return "";
        } else {
            switch (field.toString()) {
                case "v":
                    return "v-登錄成功";
                case "x":
                    return "x-登錄失敗";
                default:
                    return "";
            }
        }
    }

    /**
     * add by Maxine on 2011/08/01 for UI028200 UI使用
     */
    protected String getRMBTCHMTR_FLAGName(Object field) {
        if (field == null) {
            return "";
        } else {
            switch (field.toString()) {
                case "0":
                    return "0-未回饋";
                case "1":
                    return "1-已回饋";
                default:
                    return "";
            }
        }
    }

    /**
     * add by Maxine on 2011/08/01 for UI028200 UI使用
     */
    protected String getAMT_TYPEName(Object field) {
        if (field == null) {
            return "";
        } else {
            switch (field.toString()) {
                case "001":
                    return "001-現金";
                case "002":
                    return "002-轉帳";
                case "003":
                    return "003-轉帳連動";
                default:
                    return "";
            }
        }
    }

    /**
     * add  for UI028200 UI使用
     */
    protected String getTASKStatusName(Object field) {
        if (field == null) {
            return "";
        }
        switch (field.toString()) {
            case "1":
                return "工作開始";
            case "2":
                return "執行中";
            case "3":
                return "工作結束";
            case "4":
                return "工作失敗";
            case "5":
                return "工作中止";
            default:
                return "";
        }
    }

    /**
     * add  for UI028200 UI使用
     */
    protected String getBatchResultName(String batchResult) {
        switch (batchResult) {
            case "0":
                return "執行中";
            case "1":
                return "成功";
            case "2":
                return "失敗";
            case "3":
                return "部分失敗";
            default:
                return "";
        }
    }


    public static String getSTATUSTYPEName(Object field) {
        if (field == null) {
            return "";
        } else {
            switch (field.toString().trim()) {
                case "1":
                    return "寄送";
                case "2":
                    return "庫內";
                case "3":
                    return "庫外";
                case "4":
                    return "在途";
                case "5":
                    return "客戶(啟用)";
                case "6":
                    return "作廢";
                default:
                    return field.toString();
            }
        }
    }

    public static String bitToYesNo(Object bitField) {
        if (bitField == null) {
            return "";
        } else {
            if (bitField.toString().trim().equals("0")) {
                return "否";
            } else if ((bitField.toString().trim().equals("1"))) {
                return "是";
            }
            return "";
        }
    }

    public static String getLetterName(Object field) {
        if (field == null) {
            return "";
        } else {
            switch (field.toString().trim()) {
                case "0":
                    return "是";
                case "":
                case "1":
                    return "否";
                default:
                    return "";
            }
        }
    }

    public static String formatCARDNO(Object field) {
        if (field == null) {
            return "";
        } else {
            if (field.toString().length() == 16) {
                return field.toString().substring(0, 3) + "-" + field.toString().substring(3, 6) + "-" + field.toString().substring(6, 13) +
                        field.toString().substring(13, 14) + "-" + field.toString().substring(14, 16);
            } else {
                return field.toString();
            }
        }
    }

    /**
     * Add for UI_060510
     *
     * @param code
     * @return
     */
    public static String getCardTypeName(Object code) {
        if (null == code) {
            return "";
        } else {
            switch (code.toString()) {
                case "0":
                    return "一般卡";
                case "1":
                    return "磁條金融卡";
                case "2":
                    return "華納卡";
                case "3":
                    return "磁條COMBO卡";
                case "11":
                    return "晶片金融卡";
                case "13":
                    return "晶片COMBO卡";
                case "17":
                    return "企業識別卡";
                case "18":
                    return "集團識別卡";
                case "41":
                    return "香港PLUS卡";
                case "42":
                    return "澳門PLUS卡";
                case "71":
                    return "IBT財金卡";
                case "72":
                    return "IBT BOBO卡";
                case "73":
                    return "IBT TMA卡";
                case "74":
                    return "IBT TMA_VIP卡";
                case "75":
                    return "IBT GAGA女卡";
                case "76":
                    return "IBT GAGA男卡";
                case "81":
                    return "IBT財金IC卡";
                case "82":
                    return "IBT BOBO IC卡";
                case "83":
                    return "IBT TMA IC卡";
                case "84":
                    return "IBT TMA_VIP IC卡";
                case "85":
                    return "IBT GAGA 女IC";
                case "86":
                    return "IBT GAGA 男IC";
                case "87":
                    return "IBT企業IC卡";
                case "88":
                    return "IBT集團IC卡";
                default:
                    Cardtype cardType = TxHelper.getCardType(Byte.parseByte(code.toString()));
                    if (null != cardType && !StringUtils.isNotBlank(cardType.getTypename())) {
                        return cardType.getTypename();
                    }
                    return code.toString();
            }
        }
    }

    public static String getCardStatusName(Object field) {
        if (null == field) {
            return "";
        } else {
            switch (field.toString()) {
                case "0":
                    return "晶片金融卡";
                case "1":
                    return "新申請";
                case "2":
                    return "製卡";
                case "3":
                    return "領用";
                case "4":
                    return "啟用";
                case "5":
                    return "掛失";
                case "6":
                    return "註銷";
                case "7":
                    return "在途未啟用註銷";
                case "8":
                    return "掛失未啟用金融信用卡";
                case "9":
                    return "在途未啟用";
                default:
                    return field.toString();
            }
        }
    }

    public static String getORIGINALName(String field, String apbrno, String brno) {
        if (StringUtils.isBlank(field)) {
            return "";
        } else {
            if (apbrno.equals(brno)) {
                BctlExtMapper bctlExtMapper = SpringBeanFactoryUtil.getBean(BctlExtMapper.class);
                return bctlExtMapper.getBRAlias(brno);
            } else {
                //來源
                return "製卡廠";
            }
        }
    }

    public static String getORIGINALName(String field) {
        if (StringUtils.isBlank(field)) {
            return "";
        } else {
            switch (field) {
                case "0":
                    return field + "-臨櫃";
                case "1":
                    return field + "-FCS";
                case "2":
                    return field + "-FEDI";
                case "3":
                    return field + "-FISC";
                case "4":
                    return field + "-MMAB2B";
                case "5":
                    return field + "-緊急匯款";
                case "6":
                    return field + "-匯入款退匯";
                default:
                    return field;
            }
        }
    }

    public static String formatBATCHNO(Object field) {
        if (field == null) {
            return "";
        } else {
            if (field.toString().length() == 14) {
                return field.toString().substring(0, 8) + "-" + field.toString().substring(8, 11) + "-" + field.toString().substring(11, 14);
            } else {
                return field.toString();
            }
        }
    }

    public static String toMask(Object field) {
        if (field == null) {
            return "";
        } else {
            String vaule = field.toString();
            int len = vaule.length();
            if (len > 4) {
                return StringUtils.rightPad(vaule.substring(0, 4), len, "*");
            }
            return vaule;
        }
    }

    /**
     * 取得幣別中文名稱
     *
     * @param cur
     * @return
     */
    protected String getZoneCurName(String cur) {
        switch (cur) {
            case "TWD":
                return "台幣";
            case "HKD":
                return "港幣";
            case "MOP":
                return "葡幣";
            case "USD":
                return "美金";
            case "JPY":
                return "日圓";
            case "EUR":
                return "歐元";
            default:
                return "";
        }
    }

    protected static BigDecimal isnullTz(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            bigDecimal = new BigDecimal(0);
            return bigDecimal;
        }
        return bigDecimal;
    }

    /**
     * xy add  如果是null 返回空字串
     *
     * @param object
     * @return
     */
    protected static String isnullTz(Object object) {
        if (object == null) {
            return "";
        }
        return object.toString();
    }

    /**
     * 取得區域中文名稱
     *
     * @param zoneCode
     * @return
     */
    protected static String getZoneName(String zoneCode) {
        switch (zoneCode) {
            case "HKG": return "香港";
            case "MAC": return "澳門";
            case "TWN": return "台灣";
            default: return zoneCode;
        }
    }
}
