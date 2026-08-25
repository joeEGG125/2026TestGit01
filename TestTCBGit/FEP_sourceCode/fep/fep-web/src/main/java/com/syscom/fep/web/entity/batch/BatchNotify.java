package com.syscom.fep.web.entity.batch;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.vo.enums.FEPNotify;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BatchNotify implements Serializable {
    private boolean fepNotifyMail_APD;
    private boolean fepNotifyMail_SYS;
    private boolean fepNotifyMail_Customize;
    private String batchNotifymail;
    private boolean fepNotifyPhone_APD;
    private boolean fepNotifyPhone_SYS;
    private boolean fepNotifyPhone_Customize;
    private String batchNotifyphone;

    /**
     * 清除資料
     */
    public void clearNotify() {
        this.fepNotifyMail_APD = false;
        this.fepNotifyMail_SYS = false;
        this.fepNotifyMail_Customize = false;
        this.batchNotifymail = StringUtils.EMPTY;
        this.fepNotifyPhone_APD = false;
        this.fepNotifyPhone_SYS = false;
        this.fepNotifyPhone_Customize = false;
        this.batchNotifyphone = StringUtils.EMPTY;
    }

    /**
     * 從batch中取出notify mail phone相關信息
     *
     * @param batch
     * @param appendFepNotifyDescription
     */
    public void getNotifyFromBatch(Batch batch, boolean appendFepNotifyDescription) {
        if (batch != null) {
            batchNotifymail = batch.getBatchNotifymail();
            if (StringUtils.isNotBlank(batchNotifymail)) {
                List<String> mails = StringUtil.split(batchNotifymail, ',', ';');
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
                this.batchNotifymail = StringUtils.join(list, ";");
            }
            batchNotifyphone = batch.getBatchNotifyphone();
            if (StringUtils.isNotBlank(batchNotifyphone)) {
                List<String> phones = StringUtil.split(batchNotifyphone, ',', ';');
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
                this.batchNotifyphone = StringUtils.join(list, ";");
            }
        }
    }

    /**
     * 保存時, 將mail和phone存入batch對應的欄位中
     *
     * @param batch
     */
    public void setNotifyToBatch(Batch batch) {
        // batchNotifymail
        List<String> list = new ArrayList<>();
        if (this.fepNotifyMail_APD)
            list.add(FEPNotify.FEPNotifyMail_APD.name());
        if (this.fepNotifyMail_SYS)
            list.add(FEPNotify.FEPNotifyMail_SYS.name());
        if (this.fepNotifyMail_Customize && StringUtils.isNotBlank(this.batchNotifymail))
            list.addAll(StringUtil.split(this.batchNotifymail, ',', ';'));
        batch.setBatchNotifymail(StringUtils.join(list, ","));
        // batchNotifyphone
        list.clear();
        if (this.fepNotifyPhone_APD)
            list.add(FEPNotify.FEPNotifyPhone_APD.name());
        if (this.fepNotifyPhone_SYS)
            list.add(FEPNotify.FEPNotifyPhone_SYS.name());
        if (this.fepNotifyPhone_Customize && StringUtils.isNotBlank(this.batchNotifyphone))
            list.addAll(StringUtil.split(this.batchNotifyphone, ',', ';'));
        batch.setBatchNotifyphone(StringUtils.join(list, ","));
    }

    /**
     * 檢核自定義輸入的mail是否合法
     *
     * @param invalidMail
     * @return
     */
    public boolean isFepNotifyMailCustomizeValid(RefString invalidMail) {
        if (StringUtils.isNotBlank(this.batchNotifymail)) {
            String regx = "^[A-Za-z0-9+_.-]+@(.+)$";
            Pattern p = Pattern.compile(regx);
            List<String> mails = StringUtil.split(this.batchNotifymail, ';', ',');
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

    public String getBatchNotifymail() {
        return batchNotifymail;
    }

    public void setBatchNotifymail(String batchNotifymail) {
        this.batchNotifymail = batchNotifymail;
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

    public String getBatchNotifyphone() {
        return batchNotifyphone;
    }

    public void setBatchNotifyphone(String batchNotifyphone) {
        this.batchNotifyphone = batchNotifyphone;
    }

    @Override
    public String toString() {
        return "BatchNotify{" +
                "fepNotifyMail_APD=" + fepNotifyMail_APD +
                ", fepNotifyMail_SYS=" + fepNotifyMail_SYS +
                ", fepNotifyMail_Customize=" + fepNotifyMail_Customize +
                ", batchNotifymail='" + batchNotifymail + '\'' +
                ", fepNotifyPhone_APD=" + fepNotifyPhone_APD +
                ", fepNotifyPhone_SYS=" + fepNotifyPhone_SYS +
                ", fepNotifyPhone_Customize=" + fepNotifyPhone_Customize +
                ", batchNotifyphone='" + batchNotifyphone + '\'' +
                '}';
    }
}
