package com.syscom.fep.vo.communication;

import com.syscom.fep.base.enums.Protocol;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * 發送給FISCGW的請求電文
 *
 * @author Richard
 */
@XStreamAlias("request")
public class ToFISCCommu extends BaseXmlCommu {
    private String message;
    private String stan;
    private int ej;
    private int timeout;
    private String messageId;
    private int step;
    private String txRquid;
    @XStreamOmitField
    private Protocol protocol;
    @XStreamOmitField
    private String restfulUrl;

    public ToFISCCommu() {}

    public ToFISCCommu(String message, String stan, int ej, int timeout, String messageId, String txRquid) {
        this.message = message;
        this.stan = stan;
        this.ej = ej;
        this.timeout = timeout;
        this.messageId = messageId;
        this.txRquid = txRquid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public int getEj() {
        return ej;
    }

    public void setEj(int ej) {
        this.ej = ej;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getTxRquid() {
        return txRquid;
    }

    public void setTxRquid(String txRquid) {
        this.txRquid = txRquid;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public String getRestfulUrl() {
        return restfulUrl;
    }

    public void setRestfulUrl(String restfulUrl) {
        this.restfulUrl = restfulUrl;
    }

    /**
     * 是否對序列化後的字串, 轉為HEX字串
     *
     * @return
     */
    @Override
    protected boolean isSerializedToHex() {
        return true;
    }
}
