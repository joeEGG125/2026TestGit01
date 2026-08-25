package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * 發送給CBSGW的請求電文
 *
 * @author Richard
 */
@XStreamAlias("request")
public class ToCBSCommu extends BaseXmlCommu {
    private String channel;
    private int ej;
    private int timeout;
    private String cbsId;
    private String message;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
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

    public String getCbsId() {
        return cbsId;
    }

    public void setCbsId(String cbsId) {
        this.cbsId = cbsId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
