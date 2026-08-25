package com.syscom.fep.notify.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeConvertUtil {
    final static String reqex1 = "(\\d{4})-(\\d{1,2})-(\\d{1,2})";
    final static String reqex2 = "(\\d{4})/(\\d{1,2})/(\\d{1,2})";
    final static String reqex3 = "(\\d{4})-(\\d{1,2})-(\\d{1,2}) (\\d{2}):(\\d{2})";
    final static String reqex4 = "(\\d{4})/(\\d{1,2})/(\\d{1,2}) (\\d{2}):(\\d{2})";
    final static String reqex5 = "(\\d{4})-(\\d{1,2})-(\\d{1,2}) (\\d{2}):(\\d{2}):(\\d{2})";
    final static String reqex6 = "(\\d{4})/(\\d{1,2})/(\\d{1,2}) (\\d{2}):(\\d{2}):(\\d{2})";

    static Pattern pattern1 = Pattern.compile(reqex1);
    static Pattern pattern2 = Pattern.compile(reqex2);
    static Pattern pattern3 = Pattern.compile(reqex3);
    static Pattern pattern4 = Pattern.compile(reqex4);
    static Pattern pattern5 = Pattern.compile(reqex5);
    static Pattern pattern6 = Pattern.compile(reqex6);


    public static String dateTimeString(String s) {
        String year = "0000", month = "00", day = "00", hour = "00", minute = "00", second = "00";
        Matcher matche = null;

        matche = pattern1.matcher(s);
        if (matche.matches()) {
            year = matche.group(1);
            month = matche.group(2);
            day = matche.group(3);
        }

        matche = pattern2.matcher(s);
        if (matche.matches()) {
            year = matche.group(1);
            month = matche.group(2);
            day = matche.group(3);
        }

        matche = pattern3.matcher(s);
        if (matche.matches()) {
            year = matche.group(1);
            month = matche.group(2);
            day = matche.group(3);
            hour = matche.group(4);
            minute = matche.group(5);
        }

        matche = pattern4.matcher(s);
        if (matche.matches()) {
            year = matche.group(1);
            month = matche.group(2);
            day = matche.group(3);
            hour = matche.group(4);
            minute = matche.group(5);
        }

        matche = pattern5.matcher(s);
        if (matche.matches()) {
            year = matche.group(1);
            month = matche.group(2);
            day = matche.group(3);
            hour = matche.group(4);
            minute = matche.group(5);
            second = matche.group(6);
        }

        matche = pattern6.matcher(s);
        if (matche.matches()) {
            year = matche.group(1);
            month = matche.group(2);
            day = matche.group(3);
            hour = matche.group(4);
            minute = matche.group(5);
            second = matche.group(6);
        }
        return String.format("%s/%s/%s %s:%s:%s", year, month, day, hour, minute, second);
    }

    public static String nowDateString() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime localDateTime = LocalDateTime.now();
        return dateFormatter.format(localDateTime);
    }

    public static String nowDateTimeString() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.now();
        return dateFormatter.format(localDateTime);
    }
    public static String nowString() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.now();
        return dateFormatter.format(localDateTime);
    }
    public static String nowDateTimeString2() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.now();
        return dateFormatter.format(localDateTime);
    }

}
