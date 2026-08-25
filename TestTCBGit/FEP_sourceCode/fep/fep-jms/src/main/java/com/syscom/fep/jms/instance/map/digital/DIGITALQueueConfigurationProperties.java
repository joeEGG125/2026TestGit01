package com.syscom.fep.jms.instance.map.digital;

import com.syscom.fep.jms.instance.map.MapQueueConfigurationProperties;

public class DIGITALQueueConfigurationProperties extends MapQueueConfigurationProperties<String> {
    private String replyTo;

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    /**
     * 一個Key對應一個JMS相關的實例物件
     *
     * @return
     */
    @Override
    public String getKey() {
        return this.replyTo;
    }
}
