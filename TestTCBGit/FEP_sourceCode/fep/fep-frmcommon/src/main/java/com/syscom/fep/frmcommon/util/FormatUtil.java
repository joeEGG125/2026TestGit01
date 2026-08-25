package com.syscom.fep.frmcommon.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FormatUtil {
    public static final String FORMAT_DATE_YYYYMMDDHHMM_PLAIN = "yyyyMMddHHmm";
    public static final String FORMAT_DATE_YYYYMMDDHHMM = "yyyy/MM/dd HH:mm";
    public static final String FORMAT_DATE_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String FORMAT_DATE_YYYYMMDD_T_HHMM = "yyyy/MM/ddTHH:mm";
    public static final String FORMAT_DATE_YYYY_MM_DD_T_HH_MM = "yyyy-MM-ddTHH:mm";
    public static final String FORMAT_DATE_YYYYMMDDHHMMSSS = "yyyyMMdd-HHmmssSSS";
    public static final String FORMAT_DATE_YYYYMMDDHHMMSS_TAX = "yyyy-MM-dd-HH.mm.ss.SSS";
    public static final String FORMAT_DATE_YYYYMMDDHHMMSSSS = "yyyyMMdd-HHmmss-SSS";

    public static final String FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN = "yyyyMMddHHmmss";
    public static final String FORMAT_DATE_YYMMDDHHMMSS_PLAIN = "yyMMddHHmmss";
    public static final String FORMAT_DATE_YYYYMMDDHHMMSS = "yyyy/MM/dd HH:mm:ss";
    public static final String FORMAT_DATE_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_DATE_YYYYMMDD_T_HHMMSS = "yyyy/MM/ddTHH:mm:ss";
    public static final String FORMAT_DATE_YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-ddTHH:mm:ss";

    public static final String FORMAT_DATE_YYYYMMDDHHMMSSSSS_PLAIN = "yyyyMMddHHmmssSSS";
    public static final String FORMAT_DATE_YYYYMMDDHHMMSSSSSSSSSSSS_PLAIN = "yyyyMMddHHmmssSSSSSSS";
    public static final String FORMAT_DATE_YYYYMMDDHHMMSSSSS = "yyyy/MM/dd HH:mm:ss.SSS";
    public static final String FORMAT_DATE_YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String FORMAT_DATE_YYYYMMDD_T_HHMMSSSSS = "yyyy/MM/ddTHH:mm:ss.SSS";
    public static final String FORMAT_DATE_YYYY_MM_DD_T_HH_MM_SS_SSS = "yyyy-MM-ddTHH:mm:ss.SSS";

    public static final String FORMAT_DATE_YYYYMM_PLAIN = "yyyyMM";
    public static final String FORMAT_DATE_YYYYMMDD_PLAIN = "yyyyMMdd";
    public static final String FORMAT_DATE_DDMMYYYY_PLAIN = "ddMMyyyy";
    public static final String FORMAT_DATE_YYYY_MM_DD_SLASH = "yyyy/MM/dd";
    public static final String FORMAT_DATE_YYYY_MM_DD_DASH = "yyyy-MM-dd";

    public static final String FORMAT_DATE_YYY_MM_DD_SLASH = "yyy/MM/dd";
    public static final String FORMAT_DATE_YYY_MM_DD_DASH = "yyy-MM-dd";

    public static final String FORMAT_TIME_HHMMSS_PLAIN = "HHmmss";
    public static final String FORMAT_TIME_HHMMSSSSS_PLAIN = "HHmmssSSS";
    public static final String FORMAT_TIME_HH_MM_SS = "HH:mm:ss";
    public static final String FORMAT_TIME_HH_MM_SS_SSS = "HH:mm:ss:SSS";
    public static final String FORMAT_TIME_HH_MM_SSSSS = "HH:mm:ss.SSS";
    public static final String FORMAT_TIME_HH_MM = "HH:mm";

    private static final DecimalFormat longFormat, doubleFormat, rateFormat, decimalFormat;
    private static final SimpleDateFormat dateFormat, timeFormat, timeInMillisFormat, dateTimeFormat, dateTimeInMillisFormat;

    /**
     * Binary prefixes, used in IEC Standard for naming bytes.
     * (https://en.wikipedia.org/wiki/International_Electrotechnical_Commission)
     * <p>
     * Should be used for most representations of bytes
     */
    private static final long KIBI = 1L << 10;
    public static final long MEBI = 1L << 20;
    private static final long GIBI = 1L << 30;
    private static final long TEBI = 1L << 40;
    private static final long PEBI = 1L << 50;
    private static final long EXBI = 1L << 60;

    /**
     * Decimal prefixes, used for Hz and other metric units and for bytes by hard drive manufacturers
     */
    private static final long KILO = 1_000L;
    private static final long MEGA = 1_000_000L;
    private static final long GIGA = 1_000_000_000L;
    private static final long TERA = 1_000_000_000_000L;
    private static final long PETA = 1_000_000_000_000_000L;
    private static final long EXA = 1_000_000_000_000_000_000L;

    static {
        Locale Locale = new Locale("en", "US");
        longFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale);
        longFormat.applyPattern("#,###");
        doubleFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale);
        doubleFormat.applyPattern("#,###.###");
        decimalFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale);
        decimalFormat.applyPattern("#,###.##");
        rateFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale);
        rateFormat.applyPattern("#0.00%");
        dateFormat = new SimpleDateFormat(FORMAT_DATE_YYYY_MM_DD_SLASH, Locale);
        timeFormat = new SimpleDateFormat(FORMAT_TIME_HH_MM_SS, Locale);
        timeInMillisFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale);
        dateTimeFormat = new SimpleDateFormat(FORMAT_DATE_YYYYMMDDHHMMSS, Locale);
        dateTimeInMillisFormat = new SimpleDateFormat(FORMAT_DATE_YYYYMMDDHHMMSSSSS, Locale);
    }

    private FormatUtil() {}

    public static String dateFormat(int date) {
        Calendar cal = CalendarUtil.parseDateValue(date);
        return dateFormat(cal.getTime());
    }

    public static String dateFormat(Date date) {
        // 2024-09-29 Richard modified for 【Race Condition Format Flaw】
        synchronized (dateFormat) {
            return dateFormat.format(date);
        }
    }

    public static String dateFormat(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return dateFormat(cal.getTime());
    }

    public static String timeFormat(int time) {
        Calendar cal = CalendarUtil.parseTimeValue(time);
        return timeFormat(cal.getTime());
    }

    public static String timeFormat(Date date) {
        // 2024-09-29 Richard modified for 【Race Condition Format Flaw】
        synchronized (timeFormat) {
            return timeFormat.format(date);
        }
    }

    public static String rateFormat(double value) {
        // 2024-09-29 Richard modified for 【Race Condition Format Flaw】
        synchronized (rateFormat) {
            return rateFormat.format(value);
        }
    }

    public static String longFormat(long value) {
        // 2024-09-29 Richard modified for 【Race Condition Format Flaw】
        synchronized (longFormat) {
            return longFormat.format(value);
        }
    }

    public static String decimalFormat(BigDecimal value) {
        synchronized (decimalFormat) {
            return decimalFormat.format(value);
        }
    }

    public static String doubleFormat(double value) {
        synchronized (doubleFormat) {
            doubleFormat.setMinimumFractionDigits(0);
            doubleFormat.setMaximumFractionDigits(100);
            return doubleFormat.format(value);
        }
    }

    public static String doubleFormat(double value, int fixFractionDigits) {
        synchronized (doubleFormat) {
            doubleFormat.setMaximumFractionDigits(fixFractionDigits);
            doubleFormat.setMinimumFractionDigits(fixFractionDigits);
            return doubleFormat.format(value);
        }
    }

    public static String timeInMillisFormat(int time) {
        Calendar cal = CalendarUtil.parseTimeValue(time);
        return timeInMillisFormat(cal.getTime());
    }

    public static String timeInMillisFormat(Date date) {
        // 2024-09-29 Richard modified for 【Race Condition Format Flaw】
        synchronized (timeInMillisFormat) {
            return timeInMillisFormat.format(date);
        }
    }

    public static String dateTimeFormat(int date, int time) {
        Calendar cal = CalendarUtil.parseDateTimeValue(date, time);
        return dateTimeFormat(cal.getTime());
    }

    public static String dateTimeFormat(Date date) {
        // 2024-09-29 Richard modified for 【Race Condition Format Flaw】
        synchronized (dateTimeFormat) {
            return dateTimeFormat.format(date);
        }
    }

    public static String dateTimeInMillisFormat(Date date) {
        // 2024-09-29 Richard modified for 【Race Condition Format Flaw】
        synchronized (dateTimeInMillisFormat) {
            return dateTimeInMillisFormat.format(date);
        }
    }

    public static String dateTimeFormat(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return dateTimeFormat(cal.getTime());
    }

    public static String dateTimeInMillisFormat(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return dateTimeInMillisFormat(cal.getTime());
    }

    public static String dateTimeFormat(Calendar cal, String format) {
        int index = format.indexOf("T");
        if (index < 0) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(cal.getTime());
        } else {
            StringBuilder sb = new StringBuilder();
            String formatDate = format.substring(0, index);
            if (StringUtils.isNotBlank(formatDate)) {
                sb.append(dateTimeFormat(cal, formatDate)).append("T");
            }
            String formatTime = format.substring(index + 1);
            if (StringUtils.isNotBlank(formatTime)) {
                sb.append(dateTimeFormat(cal, formatTime));
            }
            return sb.toString();
        }
    }

    public static String dateTimeFormat(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static String longFormat(long value, String pattern) {
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(value);
    }

    public static String doubleFormat(double value, String pattern) {
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(value);
    }

    public static String rateFormat(double value, String pattern) {
        DecimalFormat df = new DecimalFormat(StringUtils.join(pattern, "%"));
        return df.format(value);
    }

    public static String decimalFormat(BigDecimal value, String pattern) {
        if (value == null)
            return StringUtils.EMPTY;
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(value);
    }

    public static Date parseDataTime(String dateTime, String format) throws ParseException {
        int index = format.indexOf("T");
        if (index < 0) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setLenient(false);
            return sdf.parse(dateTime);
        } else {
            format = StringUtils.join(format.substring(0, index), StringUtils.SPACE, format.substring(index + 1));
            return parseDataTime(dateTime, format);
        }
    }

    public static ThreadLocal<MessageFormat> createMessageFormat(final String pattern) {
        return ThreadLocal.withInitial(() -> new MessageFormat(pattern));
    }

    /**
     * 用來解決【Race Condition Format Flaw】
     *
     * @param messageFormatThreadLocal
     * @param pattern
     * @param args
     * @return
     */
    public static String messageFormat(ThreadLocal<MessageFormat> messageFormatThreadLocal, String pattern, Object... args) {
        if (messageFormatThreadLocal == null) {
            messageFormatThreadLocal = createMessageFormat(pattern);
        }
        return messageFormatThreadLocal.get().format(args);
    }

    /**
     * 將yyyy-MM-dd HH:mm:ss.SSS轉換成yyyy-MM-dd
     *
     * @param dateTimeString
     * @return String
     */
    public static String convertDateTimeToDate(String dateTimeString) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, inputFormatter);
        return dateTime.format(outputFormatter);
    }

    /**
     * 轉成 response 台幣金額格式 (****,***,***.**)
     *
     * @param num
     * @return
     */
    public static String dollarTWDFormat(BigDecimal num) {
        return dollarFormat(num, 15, true);
    }

    /**
     * 轉成 response 外幣金額格式 (*********.**)
     *
     * @param num
     * @return
     */
    public static String dollarForeignFormat(BigDecimal num) {
        return dollarFormat(num, 12, false);
    }

    /**
     * BigDemical 轉金額格式 String (****,***,***.**)
     *
     * @param num          金額(BigDemical)
     * @param formatLength (格式總長度)
     * @param needComma    (格式是否含 comma)
     * @return
     */
    public static String dollarFormat(BigDecimal num, int formatLength, boolean needComma) {
        if (needComma) {
            formatLength -= 2;
        }
        String newNum = StringUtils.leftPad(num.setScale(2, RoundingMode.HALF_UP).toString(), formatLength, "*");
        return needComma ? newNum.substring(0, 4) + "," + newNum.substring(4, 7) + "," + newNum.substring(7) : newNum;
    }

    /**
     * 用replace方式實現MessageFormat功能, 用以解決【Race Condition Format Flaw】
     *
     * @param pattern
     * @param arguments
     * @return
     */
    public static String messageFormat(String pattern, Object... arguments) {
        if (StringUtils.isBlank(pattern)) {
            return StringUtils.EMPTY;
        } else if (ArrayUtils.isEmpty(arguments)) {
            return pattern;
        }
        String message = pattern;
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] == null) continue;
            message = StringUtils.replace(message, StringUtils.join("{", i, "}"), arguments[i].toString());
        }
        return message;
    }

    /**
     * Format bytes into a rounded string representation using IEC standard (matches Mac/Linux). For hard drive
     * capacities, use @link {@link #formatBytesDecimal(long)}. For Windows displays for KB, MB and GB, in JEDEC units,
     * edit the returned string to remove the 'i' to display the (incorrect) JEDEC units.
     *
     * @param bytes Bytes.
     * @return Rounded string representation of the byte size.
     */
    public static String formatBytes(long bytes) {
        if (bytes == 1L) { // bytes
            return String.format(Locale.ROOT, "%d byte", bytes);
        } else if (bytes < KIBI) { // bytes
            return String.format(Locale.ROOT, "%d bytes", bytes);
        } else if (bytes < MEBI) { // KiB
            return formatUnits(bytes, KIBI, "KiB");
        } else if (bytes < GIBI) { // MiB
            return formatUnits(bytes, MEBI, "MiB");
        } else if (bytes < TEBI) { // GiB
            return formatUnits(bytes, GIBI, "GiB");
        } else if (bytes < PEBI) { // TiB
            return formatUnits(bytes, TEBI, "TiB");
        } else if (bytes < EXBI) { // PiB
            return formatUnits(bytes, PEBI, "PiB");
        } else { // EiB
            return formatUnits(bytes, EXBI, "EiB");
        }
    }

    /**
     * Format units as exact integer or fractional decimal based on the prefix, appending the appropriate units
     *
     * @param value  The value to format
     * @param prefix The divisor of the unit multiplier
     * @param unit   A string representing the units
     * @return A string with the value
     */
    private static String formatUnits(long value, long prefix, String unit) {
        if (value % prefix == 0) {
            return String.format(Locale.ROOT, "%d %s", value / prefix, unit);
        }
        return String.format(Locale.ROOT, "%.1f %s", (double) value / prefix, unit);
    }

    /**
     * Format bytes into a rounded string representation using decimal SI units. These are used by hard drive
     * manufacturers for capacity. Most other storage should use {@link #formatBytes(long)}.
     *
     * @param bytes Bytes.
     * @return Rounded string representation of the byte size.
     */
    public static String formatBytesDecimal(long bytes) {
        if (bytes == 1L) { // bytes
            return String.format(Locale.ROOT, "%d byte", bytes);
        } else if (bytes < KILO) { // bytes
            return String.format(Locale.ROOT, "%d bytes", bytes);
        } else {
            return formatValue(bytes, "B");
        }
    }

    /**
     * Format hertz into a string to a rounded string representation.
     *
     * @param hertz Hertz.
     * @return Rounded string representation of the hertz size.
     */
    public static String formatHertz(long hertz) {
        return formatValue(hertz, "Hz");
    }

    /**
     * Format arbitrary units into a string to a rounded string representation.
     *
     * @param value The value
     * @param unit  Units to append metric prefix to
     * @return Rounded string representation of the value with metric prefix to extension
     */
    public static String formatValue(long value, String unit) {
        if (value < KILO) {
            return String.format(Locale.ROOT, "%d %s", value, unit).trim();
        } else if (value < MEGA) { // K
            return formatUnits(value, KILO, "K" + unit);
        } else if (value < GIGA) { // M
            return formatUnits(value, MEGA, "M" + unit);
        } else if (value < TERA) { // G
            return formatUnits(value, GIGA, "G" + unit);
        } else if (value < PETA) { // T
            return formatUnits(value, TERA, "T" + unit);
        } else if (value < EXA) { // P
            return formatUnits(value, PETA, "P" + unit);
        } else { // E
            return formatUnits(value, EXA, "E" + unit);
        }
    }

    /**
     * Formats an elapsed time in seconds as days, hh:mm:ss.
     *
     * @param secs Elapsed seconds
     * @return A string representation of elapsed time
     */
    public static String formatElapsedSecs(long secs) {
        long eTime = secs;
        final long days = TimeUnit.SECONDS.toDays(eTime);
        eTime -= TimeUnit.DAYS.toSeconds(days);
        final long hr = TimeUnit.SECONDS.toHours(eTime);
        eTime -= TimeUnit.HOURS.toSeconds(hr);
        final long min = TimeUnit.SECONDS.toMinutes(eTime);
        eTime -= TimeUnit.MINUTES.toSeconds(min);
        final long sec = eTime;
        return String.format(Locale.ROOT, "%d days, %02d:%02d:%02d", days, hr, min, sec);
    }

    public static String formatBytes(long value, long prefix) {
        return value % prefix == 0L ? String.format("%d", value / prefix) : String.format("%.1f", (double) value / (double) prefix);
    }
}