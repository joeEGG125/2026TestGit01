package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

/**
 * @author Joseph
 * @create 2023/01/18
 */
public class UI_018300_Form extends BaseForm {

    private String sdatetime;
    private String edatetime;

    public String getSdatetime() {
        return sdatetime;
    }

    public void setSdatetime(String sdatetime) {
        this.sdatetime = sdatetime;
    }

    public String getEdatetime() {
        return edatetime;
    }

    public void setEdatetime(String edatetime) {
        this.edatetime = edatetime;
    }

    public String getStime() {
        return stime;
    }

    public void setStime(String stime) {
        this.stime = stime;
    }

    public String getEtime() {
        return etime;
    }

    public void setEtime(String etime) {
        this.etime = etime;
    }

    private String stime;
    private String etime;
}
