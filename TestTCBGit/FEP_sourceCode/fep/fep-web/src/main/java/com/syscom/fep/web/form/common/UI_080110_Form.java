package com.syscom.fep.web.form.common;

import com.syscom.fep.frmcommon.jms.entity.JmsInfoConcurrency;
import com.syscom.fep.web.configurer.WebQueueConfiguration.WebQueueReceiver;
import com.syscom.fep.web.form.BaseForm;

public class UI_080110_Form extends BaseForm {
    private WebQueueReceiver receiver;
    private JmsInfoConcurrency concurrency;
    private boolean offline;

    public WebQueueReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(WebQueueReceiver receiver) {
        this.receiver = receiver;
    }

    public JmsInfoConcurrency getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(JmsInfoConcurrency concurrency) {
        this.concurrency = concurrency;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }
}
