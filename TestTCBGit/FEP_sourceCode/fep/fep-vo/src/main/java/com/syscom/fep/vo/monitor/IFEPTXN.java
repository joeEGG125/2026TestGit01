package com.syscom.fep.vo.monitor;

import java.io.Serializable;

/**
 * FEPTXN
 */
public class IFEPTXN implements Serializable {

    private String source;

    private String count;

    private String tocount;

    private String timeoutcount;

    private String timeouttotalcount;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


    public String getCount() {
        return count;
    }

    public void setCount(String count) {this.count = count; }


    public String getTocount() {
        return tocount;
    }

    public void setTocount(String tocount) {
        this.tocount = tocount;
    }


    public String getTimeoutcount() {
        return timeoutcount;
    }

    public void setTimeoutcount(String timeoutcount) {
        this.timeoutcount = timeoutcount;
    }


    public String getTimeouttotalcount() {
        return timeouttotalcount;
    }

    public void setTimeouttotalcount(String timeouttotalcount) {
        this.timeouttotalcount = timeouttotalcount;
    }

}
