package com.syscom.fep.web.entity.atmmon;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.model.Msgkb;
import com.syscom.fep.vo.enums.FEPNotify;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MsgkbNotify implements Serializable {
    private boolean fepNotifyMail_APD;
    private boolean fepNotifyMail_SYS;
    private boolean fepNotifyMail_Customize;
    private String msgkbNotifymail;
    private boolean fepNotifyPhone_APD;
    private boolean fepNotifyPhone_SYS;
    private boolean fepNotifyPhone_Customize;
    private String msgkbNotifyphone;

    /**
     * 清除資料
     */
    public void clearNotify() {
        this.fepNotifyMail_APD = false;
        this.fepNotifyMail_SYS = false;
        this.fepNotifyMail_Customize = false;
        this.msgkbNotifymail = StringUtils.EMPTY;
        this.fepNotifyPhone_APD = false;
        this.fepNotifyPhone_SYS = false;
        this.fepNotifyPhone_Customize = false;
        this.msgkbNotifyphone = StringUtils.EMPTY;
    }

    /**
     * 從msgbk中取出notify mail phone相關信息
     *
     * @param msgkb
     * @param appendFepNotifyDescription
     */
    public void getNotifyFromMsgkb(Msgkb msgkb, boolean appendFepNotifyDescription) {
        if (msgkb != null) {
            msgkbNotifymail = msgkb.getNotifymail();
            if (StringUtils.isNotBlank(msgkbNotifymail)) {
                List<String> mails = StringUtil.split(msgkbNotifymail, ',', ';');
                List<String> list = new ArrayList<>();
                for (String mail : mails) {
                    if (StringUtils.isNotBlank(mail)) {
                        if (mail.equalsIgnoreCase(FEPNotify.FEPNotifyMail_APD.name())) {
                            this.fepNotifyMail_APD = true;
                        } else if (mail.equalsIgnoreCase(FEPNotify.FEPNotifyMail_SYS.name())) {
                            this.fepNotifyMail_SYS = true;
                        } else {
                            this.fepNotifyMail_Customize = true;
                            list.add(mail);
                        }
                    }
                }
                if (appendFepNotifyDescription) {
                    if (this.fepNotifyMail_SYS)
                        list.add(0, FEPNotify.FEPNotifyMail_SYS.getDescription());
                    if (this.fepNotifyMail_APD)
                        list.add(0, FEPNotify.FEPNotifyMail_APD.getDescription());
                }
                this.msgkbNotifymail = StringUtils.join(list, ";");
            }
            msgkbNotifyphone = msgkb.getMsgkbNotifyphone();
            if (StringUtils.isNotBlank(msgkbNotifyphone)) {
                List<String> phones = StringUtil.split(msgkbNotifyphone, ',', ';');
                List<String> list = new ArrayList<>();
                for (String phone : phones) {
                    if (StringUtils.isNotBlank(phone)) {
                        if (phone.equalsIgnoreCase(FEPNotify.FEPNotifyPhone_APD.name())) {
                            this.fepNotifyPhone_APD = true;
                        } else if (phone.equalsIgnoreCase(FEPNotify.FEPNotifyPhone_SYS.name())) {
                            this.fepNotifyPhone_SYS = true;
                        } else {
                            this.fepNotifyPhone_Customize = true;
                            list.add(phone);
                        }
                    }
                }
                if (appendFepNotifyDescription) {
                    if (this.fepNotifyPhone_SYS)
                        list.add(0, FEPNotify.FEPNotifyPhone_SYS.getDescription());
                    if (this.fepNotifyPhone_APD)
                        list.add(0, FEPNotify.FEPNotifyPhone_APD.getDescription());
                }
                this.msgkbNotifyphone = StringUtils.join(list, ";");
            }
        }
    }

    /**
     * 保存時, 將mail和phone存入msgkb對應的欄位中
     *
     * @param msgkb
     */
    public void setNotifyToMsgkb(Msgkb msgkb) {
        // msgkbNotifymail
        List<String> list = new ArrayList<>();
        if (this.fepNotifyMail_APD)
            list.add(FEPNotify.FEPNotifyMail_APD.name());
        if (this.fepNotifyMail_SYS)
            list.add(FEPNotify.FEPNotifyMail_SYS.name());
        if (this.fepNotifyMail_Customize && StringUtils.isNotBlank(this.msgkbNotifymail))
            list.addAll(StringUtil.split(this.msgkbNotifymail, ',', ';'));
        msgkb.setNotifymail(StringUtils.join(list, ","));
        // msgkbNotifyphone
        list.clear();
        if (this.fepNotifyPhone_APD)
            list.add(FEPNotify.FEPNotifyPhone_APD.name());
        if (this.fepNotifyPhone_SYS)
            list.add(FEPNotify.FEPNotifyPhone_SYS.name());
        if (this.fepNotifyPhone_Customize && StringUtils.isNotBlank(this.msgkbNotifyphone))
            list.addAll(StringUtil.split(this.msgkbNotifyphone, ',', ';'));
        msgkb.setMsgkbNotifyphone(StringUtils.join(list, ","));
    }

    /**
     * 檢核自定義輸入的mail是否合法
     *
     * @param invalidMail
     * @return
     */
    public boolean isFepNotifyMailCustomizeValid(RefString invalidMail) {
        if (StringUtils.isNotBlank(this.msgkbNotifymail)) {
            String regx = "^[A-Za-z0-9+_.-]+@(.+)$";
            Pattern p = Pattern.compile(regx);
            List<String> mails = StringUtil.split(this.msgkbNotifymail, ';', ',');
            for (String mail : mails) {
                if (!p.matcher(mail).matches()) {
                    if (invalidMail != null)
                        invalidMail.set(mail);
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isFepNotifyMail_APD() {
        return fepNotifyMail_APD;
    }

    public void setFepNotifyMail_APD(boolean fepNotifyMail_APD) {
        this.fepNotifyMail_APD = fepNotifyMail_APD;
    }

    public boolean isFepNotifyMail_SYS() {
        return fepNotifyMail_SYS;
    }

    public void setFepNotifyMail_SYS(boolean fepNotifyMail_SYS) {
        this.fepNotifyMail_SYS = fepNotifyMail_SYS;
    }

    public boolean isFepNotifyMail_Customize() {
        return fepNotifyMail_Customize;
    }

    public void setFepNotifyMail_Customize(boolean fepNotifyMail_Customize) {
        this.fepNotifyMail_Customize = fepNotifyMail_Customize;
    }

    public String getMsgkbNotifymail() {
        return msgkbNotifymail;
    }

    public void setMsgkbNotifymail(String msgkbNotifymail) {
        this.msgkbNotifymail = msgkbNotifymail;
    }

    public boolean isFepNotifyPhone_APD() {
        return fepNotifyPhone_APD;
    }

    public void setFepNotifyPhone_APD(boolean fepNotifyPhone_APD) {
        this.fepNotifyPhone_APD = fepNotifyPhone_APD;
    }

    public boolean isFepNotifyPhone_SYS() {
        return fepNotifyPhone_SYS;
    }

    public void setFepNotifyPhone_SYS(boolean fepNotifyPhone_SYS) {
        this.fepNotifyPhone_SYS = fepNotifyPhone_SYS;
    }

    public boolean isFepNotifyPhone_Customize() {
        return fepNotifyPhone_Customize;
    }

    public void setFepNotifyPhone_Customize(boolean fepNotifyPhone_Customize) {
        this.fepNotifyPhone_Customize = fepNotifyPhone_Customize;
    }

    public String getMsgkbNotifyphone() {
        return msgkbNotifyphone;
    }

    public void setMsgkbNotifyphone(String msgkbNotifyphone) {
        this.msgkbNotifyphone = msgkbNotifyphone;
    }

    @Override
    public String toString() {
        return "MsgkbNotify{" +
                "fepNotifyMail_APD=" + fepNotifyMail_APD +
                ", fepNotifyMail_SYS=" + fepNotifyMail_SYS +
                ", fepNotifyMail_Customize=" + fepNotifyMail_Customize +
                ", msgkbNotifymail='" + msgkbNotifymail + '\'' +
                ", fepNotifyPhone_APD=" + fepNotifyPhone_APD +
                ", fepNotifyPhone_SYS=" + fepNotifyPhone_SYS +
                ", fepNotifyPhone_Customize=" + fepNotifyPhone_Customize +
                ", msgkbNotifyphone='" + msgkbNotifyphone + '\'' +
                '}';
    }
}
