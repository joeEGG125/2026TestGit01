package com.syscom.fep.ws.client.entity;

public class BaseMessage<T> extends BaseEntity {
    private static final long serialVersionUID = -9086795905496333839L;

    protected T message;

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }
}
