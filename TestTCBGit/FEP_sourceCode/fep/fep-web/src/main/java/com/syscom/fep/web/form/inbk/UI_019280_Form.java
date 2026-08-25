package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

/**
 * @author xingyun_yang
 * @create 2021/8/31
 */
public class UI_019280_Form extends BaseForm {

    private UI_019280_Form.RadioOption radioOption = RadioOption.BOTH;
    private String datetime;
    private String stime;
    private String etime;
    private String datetimeo;
    private String bkno;
    private String stan;
    private String trad;

    public UI_019280_Form() {
        super();
    }

    public UI_019280_Form(String way, String datetime, String stime, String etime, String datetimeo, String bkno, String stan, String trad) {
        this.datetime = datetime;
        this.stime = stime;
        this.etime = etime;
        this.datetimeo = datetimeo;
        this.bkno = bkno;
        this.stan = stan;
        this.trad = trad;
    }

    public RadioOption getRadioOption() {
        return radioOption;
    }

    public void setRadioOption(RadioOption radioOption) {
        this.radioOption = radioOption;
    }

    public String getDatetime() { return datetime; }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
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

    public String getDatetimeo() {
        return datetimeo;
    }

    public void setDatetimeo(String datetimeo) {
        this.datetimeo = datetimeo;
    }

    public String getBkno() {
        return bkno;
    }

    public void setBkno(String bkno) {
        this.bkno = bkno;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getTrad() { return trad; }

    public void setTrad(String trad) { this.trad = trad; }

    public static enum RadioOption {
        /**
         * 原存行
         */
        ORI,
        /**
         * 代理行
         */
        AGENCY,
        /**
         * 兩者
         */
        BOTH,
    }

}
