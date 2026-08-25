package com.syscom.fep.notify.common.email;

import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.mail.Mail;
import com.syscom.fep.frmcommon.mail.MailData;
import com.syscom.fep.frmcommon.mail.MailPriority;
import com.syscom.fep.frmcommon.mail.MailProperties;
import com.syscom.fep.notify.common.SenderBase;
import com.syscom.fep.notify.common.config.Email001Config;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.syscom.fep.notify.cnst.NotifyConstant.*;

@Component
public class Email001<T> extends SenderBase<T> {
    @Autowired
    private Email001Config emailConfig;
    // private final LogHelper logger = LogHelperFactory.getGeneralLogger();

    @Override
    public void send(LogData logData, Map<String, T> content) {
        try {
            logData.setRemark("Email001--Ready to Send Email");
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logMessage(logData);
            String sslOnConnect = (String) content.get(NOTIFY_MESSAGE_SSLONCONNECT);
            MailPriority priority = null;
            try {
                priority = MailPriority.valueOf((String) content.get(NOTIFY_MESSAGE_PRIORITY));
            } catch (Exception e) {}
            // MailProperties
            MailProperties properties = new MailProperties();
            properties.setSmtp(emailConfig.isUseConfigSmtp() ? emailConfig.getSmtp() : (String) content.get(NOTIFY_MESSAGE_PROVIDER));
            properties.setPort(emailConfig.isUseConfigSmtpPort() ? emailConfig.getPort() : Integer.parseInt((String) content.get(NOTIFY_MESSAGE_PROVIDER_PORT)));
            properties.setAccount((String) content.get(NOTIFY_MESSAGE_ACCOUNT));
            properties.setSscode((String) content.get(NOTIFY_MESSAGE_SSCODE));
            properties.setSslOnConnect(StringUtils.isBlank(sslOnConnect) ? emailConfig.isSslOnConnect() : Boolean.parseBoolean(sslOnConnect));
            // MailData
            MailData mailData = new MailData();
            mailData.setFrom(emailConfig.getFrom());
            mailData.setTo((String) content.get(NOTIFY_EMAIL_PARM_NAME));
            mailData.setSubject((String) content.get(NOTIFY_MESSAGE_CONTENT_SUBJECT));
            mailData.setBody((String) content.get(NOTIFY_MESSAGE_CONTENT_BODY));
            mailData.setPriority(priority);
            Mail.sendHtmlEmail(properties, mailData);
            // logger.info("Email001--Send Email Success!! ");
            logData.setRemark("Email001--Send Email Success");
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logMessage(logData);
        } catch (Exception e) {
            // logger.error(e, "Email001--Send Email Fail !!");
            logData.setRemark("Email001--Send Email Fail !!");
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logData.setProgramException(e);
            sendEMS(logData);
        }
    }
}
