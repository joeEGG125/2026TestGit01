package com.syscom.fep.frmcommon.jms;

import com.syscom.fep.frmcommon.jms.entity.PlainTextMessage;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public class JmsPayload<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = -6680940705620609552L;

    private JmsKind kind;
    private String destination;
    private T payload;

    public JmsPayload() {
    }

    public JmsPayload(JmsKind kind, String destination, T payload) {
        this.kind = kind;
        this.destination = destination;
        this.payload = payload;
    }

    public JmsKind getKind() {
        return kind;
    }

    public void setKind(JmsKind kind) {
        this.kind = kind;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T extends Serializable> JmsPayload<T> toJmsPayload(JmsKind kind, String destination, T message) {
        if (message instanceof String) {
            PlainTextMessage payload = new PlainTextMessage(kind, destination, (String) message);
            return (JmsPayload<T>) payload;
        } else if (message instanceof JmsPayload) {
            JmsPayload payload = (JmsPayload) message;
            payload.setDestination(destination);
            payload.setKind(kind);
            return payload;
        } else {
            return new JmsPayload<T>(kind, destination, message);
        }
    }
}
