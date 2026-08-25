package com.syscom.fep.common.mail;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.mail.Mail;
import com.syscom.fep.frmcommon.mail.MailData;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.thread.ThreadPoolFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = "spring.fep.mail", name = "enable", havingValue = "true")
@Lazy
public class MailSender {
    private static final String ProgramName = MailSender.class.getSimpleName();
    private final LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private MailConfiguration configuration;
    private ExecutorService executor;

    @PostConstruct
    public void postConstruct() {
        executor = ThreadPoolFactory.newFixedThreadPool(
                this.configuration.getExecutorCorePoolSize(),
                this.configuration.getExecutorKeepAliveTime(),
                TimeUnit.MILLISECONDS,
                this.configuration.getExecutorQueueCapacity(),
                new SimpleThreadFactory("MailSenderExecutor"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @PreDestroy
    public void preDestroy() {
        ThreadPoolFactory.shutdown(executor, "MailSenderExecutor");
    }

    public void sendSimpleEmail(MailData data) {
        sendSimpleEmail(data, true);
    }

    public void sendSimpleEmail(MailData data, boolean async) {
        if (async) {
            executor.execute(() -> {
                _sendSimpleEmail(data);
            });
        } else {
            _sendSimpleEmail(data);
        }
    }

    public void sendHtmlEmail(MailData data) {
        sendHtmlEmail(data, true);
    }

    public void sendHtmlEmail(MailData data, boolean async) {
        if (async) {
            executor.execute(() -> {
                _sendHtmlEmail(data);
            });
        } else {
            _sendHtmlEmail(data);
        }
    }

    private void _sendSimpleEmail(MailData data) {
        logger.debug(ProgramName,"._sendSimpleEmail--begin, from:", data.getFrom(), ", to:", data.getTo(), ", cc:", data.getCc(), ", subject:", data.getSubject());
        try {
            Mail.sendSimpleEmail(configuration, data);
        } catch (Exception e) {
            logger.error(e, ProgramName,"._sendSimpleEmail failed!!");
        } finally {
            logger.debug(ProgramName,"._sendSimpleEmail--finished, from:", data.getFrom(), ", to:", data.getTo(), ", cc:", data.getCc(), ", subject:", data.getSubject());
        }
    }

    private void _sendHtmlEmail(MailData data) {
        logger.debug(ProgramName,"._sendHtmlEmail--begin, from:", data.getFrom(), ", to:", data.getTo(), ", cc:", data.getCc(), ", subject:", data.getSubject());
        try {
            Mail.sendHtmlEmail(configuration, data);
        } catch (Exception e) {
            logger.error(e, ProgramName,"._sendHtmlEmail failed!!");
        } finally {
            logger.debug(ProgramName,"._sendHtmlEmail--finished, from:", data.getFrom(), ", to:", data.getTo(), ", cc:", data.getCc(), ", subject:", data.getSubject());
        }
    }
}