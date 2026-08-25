package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 接收來自CBSGW的電文
 *
 * @author Richard
 */
@XStreamAlias("request")
public class ToFEPCBSCommu extends BaseXmlCommu {
    private String channel;
    private int ej;
    private String cbsId;
    private String message;
    private String clientID;

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

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
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
