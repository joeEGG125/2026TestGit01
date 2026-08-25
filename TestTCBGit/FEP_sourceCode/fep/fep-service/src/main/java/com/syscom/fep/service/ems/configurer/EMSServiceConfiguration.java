package com.syscom.fep.service.ems.configurer;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

// @RefreshScope
public class EMSServiceConfiguration {
    @Value("${spring.fep.service.ems.resetNotifyInterval:3}")
    private int resetNotifyInterval;
    @Value("${spring.fep.service.ems.repeatInterval:3}")
    private int repeatInterval;
    @Value("#{'${spring.fep.service.ems.mailList:}'.split(',')}")
    private List<String> mailList;
    @Value("#{'${spring.fep.service.ems.mailListNps2262:}'.split(',')}")
    private List<String> mailListNps2262;
    @Value("${spring.fep.service.ems.mailSender:}")
    private String mailSender;
    @Value("${spring.fep.service.ems.warningPattern:無法取得ENCLib;RC:10,;RC:11,;RC:17,;RC:81,;RC:82,;RC:83,;RC:84,;RC:94,;RC:95,;RC:98,;RC:99,}")
    private String warningPattern;
    @Value("#{'${spring.fep.service.ems.exclude.messageId:}'.split(',')}")
    private List<String> excludeMessageIdList;
    @Value("${spring.fep.service.ems.flushStatementsTotal:1000}")
    private int flushStatementsTotal;
    @Value("${spring.fep.service.ems.sendMail:true}")
    private boolean sendMail;
    @Value("#{'${spring.fep.service.ems.smsTelList:}'.split(',')}")
    private String[] smsTelList;
    @Value("${spring.fep.service.ems.sendSms:true}")
    private boolean sendSms;
    @Value("${spring.fep.service.ems.smsMessageLengthLimit:80}")
    private int smsMessageLengthLimit;

    public int getResetNotifyInterval() {
        return resetNotifyInterval;
    }

    public void setResetNotifyInterval(int resetNotifyInterval) {
        this.resetNotifyInterval = resetNotifyInterval;
    }

    public int getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(int repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public List<String> getMailList() {
        return mailList;
    }

    public void setMailList(List<String> mailList) {
        this.mailList = mailList;
    }

    public List<String> getMailListNps2262() {
        return mailListNps2262;
    }

    public void setMailListNps2262(List<String> mailListNps2262) {
        this.mailListNps2262 = mailListNps2262;
    }

    public String getMailSender() {
        return mailSender;
    }

    public void setMailSender(String mailSender) {
        this.mailSender = mailSender;
    }

    public String getWarningPattern() {
        return warningPattern;
    }

    public void setWarningPattern(String warningPattern) {
        this.warningPattern = warningPattern;
    }

    public List<String> getExcludeMessageIdList() {
        return excludeMessageIdList;
    }

    public void setExcludeMessageIdList(List<String> excludeMessageIdList) {
        this.excludeMessageIdList = excludeMessageIdList;
    }

    public int getFlushStatementsTotal() {
        return flushStatementsTotal;
    }

    public void setFlushStatementsTotal(int flushStatementsTotal) {
        this.flushStatementsTotal = flushStatementsTotal;
    }

    public boolean isSendMail() {
        return sendMail;
    }

    public void setSendMail(boolean sendMail) {
        this.sendMail = sendMail;
    }

    public String[] getSmsTelList() {
        return smsTelList;
    }

    public void setSmsTelList(String[] smsTelList) {
        this.smsTelList = smsTelList;
    }

    public boolean isSendSms() {
        return sendSms;
    }

    public void setSendSms(boolean sendSms) {
        this.sendSms = sendSms;
    }

    public int getSmsMessageLengthLimit() {
        return smsMessageLengthLimit;
    }

    public void setSmsMessageLengthLimit(int smsMessageLengthLimit) {
        this.smsMessageLengthLimit = smsMessageLengthLimit;
    }

    @PostConstruct
    public void print() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "Service EMS Configuration"));
    }
}