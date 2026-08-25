package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CalendarUtil {
    // public static String PLAIN_DATE_TIME = "PLAIN_DATE_TIME";
    // public static String PLAIN_DATE_TIME_IN_MILLISECOND = "PLAIN_DATE_TIME_IN_MILLISECOND";
    // public static String PLAIN_DATE = "PLAIN_DATE";
    // public static String PLAIN_TIME = "PLAIN_TIME";
    // public static String PLAIN_TW_DATE = "PLAIN_TW_DATE";
    // public static String PLAIN_CENTI_SECOND_TIME = "PLAIN_CENTI_SECOND_TIME";
    // public static String PLAIN_COLON_TIME = "PLAIN_COLON_TIME";
    private static final LogHelper logger = new LogHelper();
    private static TimeZone timeZone = null;

    private CalendarUtil() {
    }

    public static void setTimeZone(TimeZone timeZone) {
        CalendarUtil.timeZone = timeZone;
    }

    public static int dateValueYearMonth(Calendar cal) {
        if (timeZone != null) {
            cal.setTimeZone(timeZone);
        }
        return cal.get(Calendar.YEAR) * 100 + (cal.get(Calendar.MONTH) + 1);
    }

    public static int getLocalDateValue() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) * 10000 +
                (cal.get(Calendar.MONTH) + 1) * 100 + cal.get(Calendar.DAY_OF_MONTH);
    }

    public static int getLocalTimeValue() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.HOUR_OF_DAY) * 10000000 +
                cal.get(Calendar.MINUTE) * 100000 + cal.get(Calendar.SECOND) * 1000 +
                cal.get(Calendar.MILLISECOND);
    }

    public static long getLocalDateTimeValue() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) * 10000000000000L +
                (cal.get(Calendar.MONTH) + 1) * 100000000000L +
                cal.get(Calendar.DAY_OF_MONTH) * 1000000000L +
                cal.get(Calendar.HOUR_OF_DAY) * 10000000 +
                cal.get(Calendar.MINUTE) * 100000 + cal.get(Calendar.SECOND) * 1000 +
                cal.get(Calendar.MILLISECOND);
    }

    public static int dateValue(Calendar cal) {
        if (timeZone != null) {
            cal.setTimeZone(timeZone);
        }
        return cal.get(Calendar.YEAR) * 10000 +
                (cal.get(Calendar.MONTH) + 1) * 100 + cal.get(Calendar.DAY_OF_MONTH);
    }

    public static int yearValue(Calendar cal) {
        if (timeZone != null) {
            cal.setTimeZone(timeZone);
        }
        return cal.get(Calendar.YEAR);
    }

    public static int monthDayValue(Calendar cal) {
        if (timeZone != null) {
            cal.setTimeZone(timeZone);
        }
        return (cal.get(Calendar.MONTH) + 1) * 100 + cal.get(Calendar.DAY_OF_MONTH);
    }

    public static int timeValue(Calendar cal) {
        if (timeZone != null) {
            cal.setTimeZone(timeZone);
        }
        return cal.get(Calendar.HOUR_OF_DAY) * 10000000 +
                cal.get(Calendar.MINUTE) * 100000 + cal.get(Calendar.SECOND) * 1000 +
                cal.get(Calendar.MILLISECOND);
    }

    public static int timeValueInHourMinute(Calendar cal) {
        if (timeZone != null) {
            cal.setTimeZone(timeZone);
        }
        return cal.get(Calendar.HOUR_OF_DAY) * 10000000 +
                cal.get(Calendar.MINUTE) * 100000;
    }

    public static int timeValueInHourMinuteSecond(Calendar cal) {
        if (timeZone != null) {
            cal.setTimeZone(timeZone);
        }
        return cal.get(Calendar.HOUR_OF_DAY) * 10000000 +
                cal.get(Calendar.MINUTE) * 100000 + cal.get(Calendar.SECOND) * 1000;
    }

    public static long dateTimeValue(Calendar cal) {
        if (timeZone != null) {
            cal.setTimeZone(timeZone);
        }
        return cal.get(Calendar.YEAR) * 10000000000000L +
                (cal.get(Calendar.MONTH) + 1) * 100000000000L +
                cal.get(Calendar.DAY_OF_MONTH) * 1000000000L +
                cal.get(Calendar.HOUR_OF_DAY) * 10000000 +
                cal.get(Calendar.MINUTE) * 100000 +
                cal.get(Calendar.SECOND) * 1000 + cal.get(Calendar.MILLISECOND);
    }

    public static long dateTimeValueInHourMinute(Calendar cal) {
        if (timeZone != null) {
            cal.setTimeZone(timeZone);
        }
        return cal.get(Calendar.YEAR) * 10000000000000L +
                (cal.get(Calendar.MONTH) + 1) * 100000000000L +
                cal.get(Calendar.DAY_OF_MONTH) * 1000000000L +
                cal.get(Calendar.HOUR_OF_DAY) * 10000000 +
                cal.get(Calendar.MINUTE) * 100000;
    }

    public static Calendar parseDateTimeValue(long dateTime) {
        Calendar cal = Calendar.getInstance();
        if (timeZone != null) {
            cal.setTimeZone(timeZone);
        }
        cal.set(Calendar.YEAR, (int) (dateTime / 10000000000000L));
        cal.set(Calendar.MONTH, (int) ((dateTime % 10000000000000L) / 100000000000L) - 1);
        cal.set(Calendar.DAY_OF_MONTH, (int) ((dateTime % 100000000000L) / 1000000000));
        cal.set(Calendar.HOUR_OF_DAY, (int) ((dateTime % 1000000000) / 10000000));
        cal.set(Calendar.MINUTE, (int) ((dateTime % 10000000) / 100000));
        cal.set(Calendar.SECOND, (int) ((dateTime % 100000) / 1000));
        cal.set(Calendar.MILLISECOND, (int) (dateTime % 1000));
        return cal;
    }

    public static Calendar parseDateTimeValue(int date, int time) {
        Calendar cal = Calendar.getInstance();
        if (timeZone != null) {
            cal.setTimeZone(timeZone);
        }
        cal.set(Calendar.YEAR, date / 10000);
        cal.set(Calendar.MONTH, (date % 10000) / 100 - 1);
        cal.set(Calendar.DAY_OF_MONTH, date % 100);
        cal.set(Calendar.HOUR_OF_DAY, time / 10000000);
        cal.set(Calendar.MINUTE, (time % 10000000) / 100000);
        cal.set(Calendar.SECOND, (time % 100000) / 1000);
        cal.set(Calendar.MILLISECOND, time % 1000);
        return cal;
    }

    public static Calendar parseDateValueYearMonth(int date) {
        Calendar cal = Calendar.getInstance();
        if (timeZone != null) {
            cal.setTimeZone(timeZone);
        }
        cal.set(Calendar.YEAR, date / 100);
        cal.set(Calendar.MONTH, (date % 100) - 1);
        cal.set(Calendar.DAY_OF_MONTH, 15);
        return cal;
    }

    public static Calendar parseDateValue(int date) {
        Calendar cal = Calendar.getInstance();
        if (timeZone != null) {
            cal.setTimeZone(timeZone);
        }
        cal.set(Calendar.YEAR, date / 10000);
        cal.set(Calendar.MONTH, (date % 10000) / 100 - 1);
        cal.set(Calendar.DAY_OF_MONTH, date % 100);
        return cal;
    }

    public static Calendar parseTimeValue(int time) {
        Calendar cal = Calendar.getInstance();
        if (timeZone != null) {
            cal.setTimeZone(timeZone);
        }
        cal.set(Calendar.HOUR_OF_DAY, time / 10000000);
        cal.set(Calendar.MINUTE, (time % 10000000) / 100000);
        cal.set(Calendar.SECOND, (time % 100000) / 1000);
        cal.set(Calendar.MILLISECOND, time % 1000);
        return cal;
    }

    // public static String format(Calendar time, String format) {
    // if (timeZone != null) {
    // time.setTimeZone(timeZone);
    // }
    // StringBuilder str = new StringBuilder();
    // if (format.equals(PLAIN_DATE_TIME)) {
    // str.append(format(time, PLAIN_DATE)).append(format(time, PLAIN_TIME));
    // } else if (format.equals(PLAIN_DATE)) {
    // str.append(time.get(Calendar.DAY_OF_MONTH));
    // while (str.length() < 2) {
    // str.insert(0, 0);
    // }
    // str.insert(0, time.get(Calendar.MONTH) + 1);
    // while (str.length() < 4) {
    // str.insert(0, 0);
    // }
    // str.insert(0, time.get(Calendar.YEAR));
    // while (str.length() < 8) {
    // str.insert(0, 0);
    // }
    // } else if (format.equals(PLAIN_TIME)) {
    // str.append(time.get(Calendar.MILLISECOND));
    // while (str.length() < 3) {
    // str.insert(0, 0);
    // }
    // str.insert(0, time.get(Calendar.SECOND));
    // while (str.length() < 5) {
    // str.insert(0, 0);
    // }
    // str.insert(0, time.get(Calendar.MINUTE));
    // while (str.length() < 7) {
    // str.insert(0, 0);
    // }
    // str.insert(0, time.get(Calendar.HOUR_OF_DAY));
    // while (str.length() < 9) {
    // str.insert(0, 0);
    // }
    // } else if (format.equals(PLAIN_TW_DATE)) {
    // str.append(time.get(Calendar.DAY_OF_MONTH));
    // while (str.length() < 2) {
    // str.insert(0, 0);
    // }
    // str.insert(0, time.get(Calendar.MONTH) + 1);
    // while (str.length() < 4) {
    // str.insert(0, 0);
    // }
    // str.insert(0, time.get(Calendar.YEAR) - 1911);
    // while (str.length() < 6) {
    // str.insert(0, 0);
    // }
    // } else if (format.equals(PLAIN_CENTI_SECOND_TIME)) {
    // str.append(format(time, PLAIN_TIME)).setLength(8);
    // } else if (format.equals(PLAIN_COLON_TIME)) {
    // str.append(time.get(Calendar.SECOND));
    // while (str.length() < 2) {
    // str.insert(0, 0);
    // }
    // str.insert(0, ":");
    // str.insert(0, time.get(Calendar.MINUTE));
    // while (str.length() < 5) {
    // str.insert(0, 0);
    // }
    // str.insert(0, ":");
    // str.insert(0, time.get(Calendar.HOUR_OF_DAY));
    // while (str.length() < 8) {
    // str.insert(0, 0);
    // }
    // } else {
    // throw ExceptionUtil.createIllegalArgumentException("Invalid format: ", format);
    // }
    // return str.toString();
    // }
    //
    // public static Calendar parse(String str, String format) {
    // Calendar time = Calendar.getInstance();
    // if (timeZone != null) {
    // time.setTimeZone(timeZone);
    // }
    // if (format.equals(PLAIN_DATE_TIME_IN_MILLISECOND)) {
    // time.set(Calendar.YEAR, Integer.parseInt(str.substring(0, 4)));
    // time.set(Calendar.MONTH, Integer.parseInt(str.substring(4, 6)) - 1);
    // time.set(Calendar.DAY_OF_MONTH, Integer.parseInt(str.substring(6, 8)));
    // time.set(Calendar.HOUR_OF_DAY, Integer.parseInt(str.substring(8, 10)));
    // time.set(Calendar.MINUTE, Integer.parseInt(str.substring(10, 12)));
    // time.set(Calendar.SECOND, Integer.parseInt(str.substring(12, 14)));
    // time.set(Calendar.MILLISECOND, Integer.parseInt(str.substring(14, 17)));
    // } else if (format.equals(PLAIN_DATE_TIME)) {
    // time.set(Calendar.YEAR, Integer.parseInt(str.substring(0, 4)));
    // time.set(Calendar.MONTH, Integer.parseInt(str.substring(4, 6)) - 1);
    // time.set(Calendar.DAY_OF_MONTH, Integer.parseInt(str.substring(6, 8)));
    // time.set(Calendar.HOUR_OF_DAY, Integer.parseInt(str.substring(8, 10)));
    // time.set(Calendar.MINUTE, Integer.parseInt(str.substring(10, 12)));
    // time.set(Calendar.SECOND, Integer.parseInt(str.substring(12, 14)));
    // } else if (format.equals(PLAIN_DATE)) {
    // time.set(Calendar.YEAR, Integer.parseInt(str.substring(0, 4)));
    // time.set(Calendar.MONTH, Integer.parseInt(str.substring(4, 6)) - 1);
    // time.set(Calendar.DAY_OF_MONTH, Integer.parseInt(str.substring(6, 8)));
    // } else if (format.equals(PLAIN_TIME)) {
    // time.set(Calendar.HOUR_OF_DAY, Integer.parseInt(str.substring(0, 2)));
    // time.set(Calendar.MINUTE, Integer.parseInt(str.substring(2, 4)));
    // time.set(Calendar.SECOND, Integer.parseInt(str.substring(4, 6)));
    // time.set(Calendar.MILLISECOND, Integer.parseInt(str.substring(6, 9)));
    // } else if (format.equals(PLAIN_TW_DATE)) {
    // time.set(Calendar.YEAR, Integer.parseInt(str.substring(0, 2)) + 1911);
    // time.set(Calendar.MONTH, Integer.parseInt(str.substring(2, 4)) - 1);
    // time.set(Calendar.DAY_OF_MONTH, Integer.parseInt(str.substring(4, 6)));
    // } else if (format.equals(PLAIN_CENTI_SECOND_TIME)) {
    // time.set(Calendar.HOUR_OF_DAY, Integer.parseInt(str.substring(0, 2)));
    // time.set(Calendar.MINUTE, Integer.parseInt(str.substring(2, 4)));
    // time.set(Calendar.SECOND, Integer.parseInt(str.substring(4, 6)));
    // time.set(Calendar.MILLISECOND, Integer.parseInt(str.substring(6, 8)) * 10);
    // } else {
    // throw ExceptionUtil.createIllegalArgumentException();
    // }
    // return time;
    // }

    public static Calendar getCurrentWeekday(int weekday) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int date = Calendar.getInstance().get(Calendar.DATE);
        GregorianCalendar c = new GregorianCalendar(year, month, date);
        int todayweekday = c.get(Calendar.DAY_OF_WEEK);
        c.add(Calendar.DATE, weekday - todayweekday);
        return c;
    }

    public static int getDayDiff(Calendar c1, Calendar c2) {
        int year1 = 0;
        int month1 = 0;
        int date1 = 0;
        int year2 = 0;
        int month2 = 0;
        int date2 = 0;
        if (c1.after(c2)) {
            year2 = c1.get(Calendar.YEAR);
            month2 = c1.get(Calendar.MONTH);
            date2 = c1.get(Calendar.DATE);
            year1 = c2.get(Calendar.YEAR);
            month1 = c2.get(Calendar.MONTH);
            date1 = c2.get(Calendar.DATE);
        } else {
            year1 = c1.get(Calendar.YEAR);
            month1 = c1.get(Calendar.MONTH);
            date1 = c1.get(Calendar.DATE);
            year2 = c2.get(Calendar.YEAR);
            month2 = c2.get(Calendar.MONTH);
            date2 = c2.get(Calendar.DATE);
        }
        int dayDiff = 0;
        while (true) {
            if (year1 == year2) {
                if (month1 == month2) {
                    dayDiff += date2 - date1;
                    break;
                } else {
                    dayDiff += getDaysOfMonth(year1, month1) - date1 + 1;
                    date1 = 1;
                    month1++;
                    for (; month1 < month2; ) {
                        dayDiff += getDaysOfMonth(year1, month1);
                        month1++;
                    }
                }
            } else {
                dayDiff += getDaysOfMonth(year1, month1) - date1 + 1;
                date1 = 1;
                month1++;
                if (month1 == 12) {
                    month1 = 0;
                    year1++;
                } else {
                    while (true) {
                        dayDiff += getDaysOfMonth(year1, month1);
                        month1++;
                        if (month1 == 12) {
                            month1 = 0;
                            year1++;
                            break;
                        }
                    }
                }
                for (; year1 < year2; year1++) {
                    dayDiff += getDaysOfYear(year1);
                }
            }
        }
        return dayDiff;
    }

    public static int getDaysOfMonth(int year, int month) {
        GregorianCalendar c = new GregorianCalendar(year, month, 1);
        int daysOfMonth = 0;
        switch (month) {
            case 0:
                daysOfMonth = 31;
                break;
            case 1:
                if (c.isLeapYear(year)) {
                    daysOfMonth = 29;
                } else {
                    daysOfMonth = 28;
                }
                break;
            case 2:
                daysOfMonth = 31;
                break;
            case 3:
                daysOfMonth = 30;
                break;
            case 4:
                daysOfMonth = 31;
                break;
            case 5:
                daysOfMonth = 30;
                break;
            case 6:
                daysOfMonth = 31;
                break;
            case 7:
                daysOfMonth = 31;
                break;
            case 8:
                daysOfMonth = 30;
                break;
            case 9:
                daysOfMonth = 31;
                break;
            case 10:
                daysOfMonth = 30;
                break;
            case 11:
                daysOfMonth = 31;
                break;
        }
        return daysOfMonth;
    }

    public static int getDaysOfYear(int year) {
        GregorianCalendar c = new GregorianCalendar(year, 0, 1);
        int daysOfYear = 0;
        if (c.isLeapYear(year)) {
            daysOfYear = 366;
        } else {
            daysOfYear = 365;
        }
        return daysOfYear;
    }

    public static int getDayPeriod(Calendar c1, Calendar c2) {
        return (int) getDiffMilliseconds(Calendar.DAY_OF_YEAR, Math.abs(c1.getTimeInMillis() - c2.getTimeInMillis()));
    }

    public static double getDiffTimeInMilliseconds(int unit, long milliseconds) {
        if (Calendar.SECOND == unit) {
            return (double) milliseconds / 1000;
        } else if (Calendar.MINUTE == unit) {
            return (double) milliseconds / 60000;
        } else if (Calendar.HOUR == unit) {
            return (double) milliseconds / 3600000;
        } else if (Calendar.DAY_OF_YEAR == unit) {
            return (double) milliseconds / 86400000;
        } else {
            return 0.0;
        }
    }

    public static long getDiffMilliseconds(int unit, long milliseconds) {
        if (Calendar.SECOND == unit) {
            return milliseconds / 1000;
        } else if (Calendar.MINUTE == unit) {
            return milliseconds / 60000;
        } else if (Calendar.HOUR == unit) {
            return milliseconds / 3600000;
        } else if (Calendar.DAY_OF_YEAR == unit) {
            return milliseconds / 86400000;
        } else {
            return 0;
        }
    }

    /**
     * return X Day X Hour X Min. X Sec. X.
     *
     * @param milliseconds long
     * @return String
     */
    public static String getDiffMilliseconds(long milliseconds) {
        StringBuilder sb = new StringBuilder();
        long diff = getDiffMilliseconds(Calendar.DAY_OF_YEAR, milliseconds);
        if (diff > 0) {
            sb.append(diff).append(" days. ");
            milliseconds = milliseconds % 86400000;
        }
        diff = getDiffMilliseconds(Calendar.HOUR, milliseconds);
        if (diff > 0) {
            sb.append(diff).append(" hours. ");
            milliseconds = milliseconds % 3600000;
        }
        diff = getDiffMilliseconds(Calendar.MINUTE, milliseconds);
        if (diff > 0) {
            sb.append(diff).append(" minutes. ");
            milliseconds = milliseconds % 60000;
        }
        diff = getDiffMilliseconds(Calendar.SECOND, milliseconds);
        if (diff > 0) {
            sb.append(diff).append(" seconds. ");
            milliseconds = milliseconds % 1000;
        }
        diff = milliseconds;
        sb.append(diff).append(" milliseconds. ");
        return sb.toString();
    }

    /**
     * 民國年轉西元年
     *
     * @param roc
     * @return
     */
    public static Calendar rocStringToADDate(String roc) {
        Calendar ret = null;
        if (((roc.length() == 7 || roc.length() == 8 || roc.length() == 13 || roc.length() == 14) ? 0 : 1) != 0) {
            ret = null;
        } else if (!StringUtils.isNumeric(roc)) {
            ret = null;
        } else {
            if (roc.length() == 7) {
                roc = StringUtils.join("0", roc, "000000");
            }
            if (roc.length() == 8) {
                roc = StringUtils.join(roc, "000000");
            }
            int num = Integer.parseInt(roc.substring(0, 4));
            if (num < 70) {
                num = 100 + num;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(num + 0x777).append("/"); // year
            sb.append(roc.substring(4, 6)).append("/"); // month
            sb.append(roc.substring(6, 8)).append(StringUtils.SPACE); // day
            sb.append(roc.substring(8, 10)).append(":"); // hour
            sb.append(roc.substring(10, 12)).append(":"); // minute
            sb.append(roc.substring(12, 14)); // second
            RefBase<Calendar> ref = new RefBase<Calendar>(ret);
            if (validateDateTime(sb.toString(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS, ref)) {
                ret = ref.get();
            }
        }
        return ret;
    }

    /**
     * 民國年轉西元年
     * 1111128142004
     *
     * @param roc
     * @return
     */
    public static Calendar rocStringToADDatefor14(String roc) {
        Calendar ret = null;
        if (((roc.length() == 7 || roc.length() == 8 || roc.length() == 12 || roc.length() == 13 || roc.length() == 14) ? 0 : 1) != 0) {
            ret = null;
        } else if (!StringUtils.isNumeric(roc)) {
            ret = null;
        } else {
//            if (roc.length() == 7) {
//                roc = StringUtils.join("0", roc, "000000");
//            }
            if (roc.length() == 7) {
                roc = StringUtils.join(roc, "0000000");
            }
            if (roc.length() == 8) {
                roc = StringUtils.join(roc, "000000");
            }
            if (roc.length() == 12) {
                //處理財金格式YYMMDDHHMMSS
                roc = StringUtils.join("1", roc);
            }
            int num = Integer.parseInt(roc.substring(0, 3));
            if (num < 70) {
                num = 100 + num;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(num + 0x777).append("/"); // year
            sb.append(roc.substring(3, 5)).append("/"); // month
            sb.append(roc.substring(5, 7)).append(StringUtils.SPACE); // day
            sb.append(roc.substring(7, 9)).append(":"); // hour
            sb.append(roc.substring(9, 11)).append(":"); // minute
            sb.append(roc.substring(11, 13)); // second
            RefBase<Calendar> ref = new RefBase<Calendar>(ret);
            if (validateDateTime(sb.toString(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS, ref)) {
                ret = ref.get();
            }
        }
        return ret;
    }

    /**
     * 民國年轉西元年字符串
     *
     * @param roc
     * @return
     */
    public static String rocStringToADString(String roc) {
        Calendar cal = rocStringToADDate(roc);
        if (cal == null) {
            return StringUtils.EMPTY;
        } else if (roc.length() > 8) {
            return FormatUtil.dateTimeFormat(cal, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
        }
        return FormatUtil.dateTimeFormat(cal, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
    }

    /**
     * 民國年轉西元年字符串
     * 1111128142004
     *
     * @param roc
     * @return
     */
    public static String rocStringToADString14(String roc) {
        Calendar cal = rocStringToADDatefor14(roc);
        if (cal == null) {
            return StringUtils.EMPTY;
        } else if (roc.length() > 8) {
            return FormatUtil.dateTimeFormat(cal, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
        }
        return FormatUtil.dateTimeFormat(cal, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
    }

    /**
     * 西元年字串轉西元年
     *
     * @param ad
     * @return
     */
    public static Calendar adStringToADDate(String ad) {
        Calendar ret = null;
        if (!(ad.length() == 8 || ad.length() == 14)) {
            ret = null;
        } else if (!StringUtils.isNumeric(ad)) {
            ret = null;
        } else {
            if (ad.length() == 8) {
                ad = StringUtils.join(ad, "000000000");
            }
            StringBuilder sb = new StringBuilder();
            sb.append(ad.substring(0, 4)).append("/"); // year
            sb.append(ad.substring(4, 6)).append("/"); // month
            sb.append(ad.substring(6, 8)).append(StringUtils.SPACE); // day
            sb.append(ad.substring(8, 10)).append(":"); // hour
            sb.append(ad.substring(10, 12)).append(":"); // minute
            sb.append(ad.substring(12, 14)); // second
            RefBase<Calendar> ref = new RefBase<Calendar>(ret);
            if (validateDateTime(sb.toString(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS, ref)) {
                ret = ref.get();
            }
        }
        return ret;
    }

    /**
     * 西元年轉民國年字串
     *
     * @param ad
     * @return
     */
    public static String adStringToROCString(String ad) {
        if (adStringToADDate(ad) == null) {
            return StringUtils.EMPTY;
        }
        return StringUtils.join(Integer.parseInt(ad.substring(0, 4)) - 0x777, ad.substring(4));
    }

    /**
     * 西元年轉民國年字串
     *
     * @param ad
     * @param format
     * @return
     */
    public static String adStringToROCString(Calendar ad, String format) {
        if (ad == null) {
            return StringUtils.EMPTY;
        }
        String dateTime = FormatUtil.dateTimeFormat(ad, format);
        return StringUtils.join(Integer.parseInt(dateTime.substring(0, 4)) - 0x777, dateTime.substring(4));
    }

    /**
     * 判斷一個字符串是否是一個有效的日期
     *
     * @param dateTime
     * @param format
     * @return
     */
    public static boolean validateDateTime(String dateTime, String format) {
        return validateDateTime(dateTime, format, null);
    }

    /**
     * 判斷一個字符串是否是一個有效的日期
     * ZhaoKai add
     *
     * @param dateTime
     * @param format
     * @param rtn
     * @return
     */
    public static boolean validateDateTime(String dateTime, String format, RefBase<Calendar> rtn) {
        if (StringUtils.isBlank(dateTime)) {
            return false;
        }
        try {
            Date date = FormatUtil.parseDataTime(dateTime, format);
            if (rtn != null) {
                Calendar cal = clone(date);
                rtn.set(cal);
            }
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * 獲取一周中的哪一天
     * 注意週一是1, 週二是2, 以此類推, 週日是7
     *
     * @param cal
     * @return
     */
    public static int getDayOfWeek(Calendar cal) {
        if (cal == null) {
            return 0;
        }
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        // 週日是1，所以減掉1後為0，所以這裡判斷一下
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }
        return dayOfWeek;
    }

    /**
     * 獲取一周中的哪一天
     * 注意週日是1, 週一是2, 週二是3, 以此類推, 週六是7
     *
     * @param cal
     * @return
     */
    public static int getDayOfWeekForQuartz(Calendar cal) {
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek;
    }

    /**
     * 判斷是否為當月最後一天
     *
     * @param cal
     * @return
     */
    public static boolean isLastDayOfMonth(Calendar cal) {
        if (cal == null) {
            cal = Calendar.getInstance();
        }
        return cal.get(Calendar.DAY_OF_MONTH) == cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 判斷是否為當月最後一周
     *
     * @param cal
     * @return
     */
    public static boolean isLastWeekOfMonth(Calendar cal) {
        if (cal == null) {
            cal = Calendar.getInstance();
        }
        return cal.get(Calendar.WEEK_OF_MONTH) == cal.getActualMaximum(Calendar.WEEK_OF_MONTH);
    }

    /**
     * 獲取指定的日期是所在月份的第幾周(週一為一周的第一天)
     *
     * @param cal
     * @return
     */
    public static int getWeekOfMonth(Calendar cal) {
        return getWeekOfMonth(cal, Calendar.MONDAY);
    }

    /**
     * 獲取指定的日期是所在月份的第幾周, 並指定週幾為一周的第一天
     *
     * @param cal
     * @param firstDayOfWeek
     * @return
     */
    public static int getWeekOfMonth(Calendar cal, int firstDayOfWeek) {
        Calendar date = clone(cal.getTime());
        date.setFirstDayOfWeek(firstDayOfWeek);
        int weekOfMonth = date.get(Calendar.WEEK_OF_MONTH);
        return weekOfMonth;
    }

    /**
     * 指定format比較兩個日期類型數據
     *
     * @param date1
     * @param date2
     * @param format
     * @return
     */
    public static boolean equals(Date date1, Date date2, String format) {
        if (date1 == null && date2 == null) {
            return true;
        } else if (date1 == null || date2 == null) {
            return false;
        } else {
            if (StringUtils.isBlank(format)) {
                format = FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN;
            }
            Calendar cal1 = clone(date1);
            Calendar cal2 = clone(date2);
            String str1 = FormatUtil.dateTimeFormat(cal1, format);
            String str2 = FormatUtil.dateTimeFormat(cal2, format);
            return str1.equals(str2);
        }
    }

    public static Calendar clone(Calendar cal) {
        Calendar calendar = null;
        if (cal == null) {
            calendar = Calendar.getInstance();
        } else {
            calendar = clone(cal.getTime());
        }
        return calendar;
    }

    public static Calendar clone(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static Calendar clone(long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        return cal;
    }

    public static Calendar add(Date date, int field, int amount) {
        return add(clone(date), field, amount);
    }

    public static Calendar add(Calendar cal, int field, int amount) {
        Calendar calendar = clone(cal);
        if (amount != 0) {
            calendar.add(field, amount);
        }
        return calendar;
    }

    public static Date max(Date... dates) {
        if (ArrayUtils.isEmpty(dates)) {
            return null;
        }
        List<Calendar> list = Arrays.asList(dates).stream().map(t -> clone(t)).collect(Collectors.toList());
        Calendar[] cals = new Calendar[list.size()];
        list.toArray(cals);
        Calendar max = max(cals);
        return max == null ? dates[0] : max.getTime();
    }

    public static Date min(Date... dates) {
        if (ArrayUtils.isEmpty(dates)) {
            return null;
        }
        List<Calendar> list = Arrays.asList(dates).stream().map(t -> clone(t)).collect(Collectors.toList());
        Calendar[] cals = new Calendar[list.size()];
        list.toArray(cals);
        Calendar min = min(cals);
        return min == null ? dates[0] : min.getTime();
    }

    public static Calendar max(Calendar... cals) {
        if (ArrayUtils.isEmpty(cals)) {
            return null;
        }
        long maxTimeInMillis = cals[0] == null ? Long.MIN_VALUE : cals[0].getTimeInMillis();
        for (int i = 1; i < cals.length; i++) {
            maxTimeInMillis = Math.max(maxTimeInMillis, cals[i] == null ? Long.MIN_VALUE : cals[i].getTimeInMillis());
        }
        return maxTimeInMillis == Long.MIN_VALUE ? cals[0] : clone(maxTimeInMillis);
    }

    public static Calendar min(Calendar... cals) {
        if (ArrayUtils.isEmpty(cals)) {
            return null;
        }
        long minTimeInMillis = cals[0] == null ? Long.MAX_VALUE : cals[0].getTimeInMillis();
        for (int i = 1; i < cals.length; i++) {
            minTimeInMillis = Math.min(minTimeInMillis, cals[i] == null ? Long.MAX_VALUE : cals[i].getTimeInMillis());
        }
        return minTimeInMillis == Long.MAX_VALUE ? cals[0] : clone(minTimeInMillis);
    }

    /**
     * 民國年轉西元年
     * chen yang add
     * 20230515  --->1120515
     * 2023/05/15 -->112/05/15
     * 2023-05-15  --->112-05-15
     * 2023/05/15 00:00:00 -->112/05/15 00:00:00
     * 2023-05-15 00:00:00  --->112-05-15 00:00:00
     *
     * @param date
     * @return
     */
    public static String getRocDate(String date) {
        try {
            if (StringUtils.isNotBlank(date)) {
                if (date.length() >= 10) {
                    int iYear = Integer.parseInt(date.substring(0, 4)) - 1911;
                    String strYear = String.valueOf(iYear);
                    return StringUtils.join(strYear, date.substring(4));
                } else if (date.length() == 8) {
                    return StringUtils.join(Integer.parseInt(date.substring(0, 4)) - 0x777, date.substring(4));
                }
            }
        } catch (Exception ex) {
            logger.warn(ex, ex.getMessage());
        }
        return "";
    }

    /**
     * 由字串日期取得當天是星期幾
     *
     * @param transactDate
     * @return
     * @throws ParseException
     */
    public static int getDayOfWeek(String transactDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(FormatUtil.FORMAT_DATE_YYY_MM_DD_DASH);
        Date date = sdf.parse(transactDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return CalendarUtil.getDayOfWeek(cal);
    }
}
