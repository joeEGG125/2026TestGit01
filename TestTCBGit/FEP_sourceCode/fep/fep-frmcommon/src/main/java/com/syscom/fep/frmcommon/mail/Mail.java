package com.syscom.fep.frmcommon.mail;

import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.apache.poi.ss.formula.functions.T;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Mail {

    private Mail() {}

    public static void sendSimpleEmail(MailProperties properties, MailData data) throws Exception {
        Email email = createSimpleEmail(properties);
        if (email == null)
            return;
        email.setMsg(data.getBody());
        sendEmail(email, properties, data);
    }

    public static void sendHtmlEmail(MailProperties properties, MailData data) throws Exception {
        HtmlEmail email = createHtmlEmail(properties);
        if (email == null)
            return;
        email.setHtmlMsg(data.getBody());
        sendEmail(email, properties, data);
    }

    private static void sendEmail(Email email, MailProperties properties, MailData data) throws Exception {
        if (email == null)
            return;
        if (StringUtils.isBlank(data.getTo()))
            return;
        try {
            List<String> tos = StringUtil.split(data.getTo(), ',', ';');
            for (String to : tos) {
                if (StringUtils.isNotBlank(to))
                    email.addTo(to);
            }
            if (StringUtils.isNotBlank(data.getCc())) {
                List<String> ccs = StringUtil.split(data.getCc(), ',', ';');
                for (String cc : ccs) {
                    email.addCc(cc);
                }
            }
            if (StringUtils.isNotBlank(data.getBcc())) {
                List<String> bccs = StringUtil.split(data.getBcc(), ',', ';');
                for (String bcc : bccs) {
                    email.addBcc(bcc);
                }
            }
            email.setFrom(data.getFrom());
            email.setSubject(data.getSubject());
            if (data.getPriority() != null)
                email.addHeader("X-Priority", String.valueOf(data.getPriority().getValue()));
            email.setSSLOnConnect(properties.isSslOnConnect());
            email.send();
        } catch (Exception e) {
            String message = StringUtils.join("Send mail failed, from:", data.getFrom(), ", to:", data.getTo(), ", cc:", data.getCc(), ", subject:", data.getSubject());
            throw ExceptionUtil.createException(e, message);
        }
    }

    private static SimpleEmail createSimpleEmail(MailProperties properties) {
        return createEmail(new SimpleEmail(), properties);
    }

    private static HtmlEmail createHtmlEmail(MailProperties properties) {
        return createEmail(new HtmlEmail(), properties);
    }

    private static <T extends Email> T createEmail(T email, MailProperties properties) {
        if (StringUtils.isBlank(properties.getSmtp()) || properties.getPort() == -1 || StringUtils.isBlank(properties.getAccount())) {
            return null;
        }
        email.setHostName(properties.getSmtp());
        email.setSmtpPort(properties.getPort());
        email.setAuthentication(properties.getAccount(), properties.getSscode());
        email.setCharset(StringUtils.isBlank(properties.getCharset()) ? "UTF-8" : properties.getCharset());
        if (properties.getSocketConnectionTimeout() > 0)
            email.setSocketConnectionTimeout(Duration.ofMillis(properties.getSocketConnectionTimeout()));
        if (properties.getSocketTimeout() > 0)
            email.setSocketTimeout(Duration.ofMillis(properties.getSocketTimeout()));
        email.setSSLOnConnect(properties.isSslOnConnect());
        return email;
    }
}
