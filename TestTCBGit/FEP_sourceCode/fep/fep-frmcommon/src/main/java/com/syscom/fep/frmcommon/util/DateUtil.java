package com.syscom.fep.frmcommon.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil extends org.apache.commons.lang3.time.DateUtils {

    public static boolean isNotYyyymmdd(String dateStr) {
        if (dateStr == null) {
            return true;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
            return false;
        } catch (ParseException e) {
            return true;
        }
    }

    /**
     * 取得目前西元日期
     *
     * @return java.lang.String
     */
    public static String getNowWestDate() {
        return getNowWestDate(FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
    }

    /**
     * 取得目前西元日期
     *
     * @return java.lang.String
     */
    public static String getNowWestDate(String format) {
        DateFormat df = new SimpleDateFormat(format, Locale.getDefault());
        Date date = new Date();
        date.setTime(System.currentTimeMillis());
        return df.format(date);
    }

}
