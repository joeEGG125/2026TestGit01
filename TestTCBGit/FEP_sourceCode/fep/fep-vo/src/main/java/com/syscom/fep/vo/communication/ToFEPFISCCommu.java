package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 接收來自FISCGW的請求電文
 *
 * @author Richard
 */
@XStreamAlias("request")
public class ToFEPFISCCommu extends BaseXmlCommu {
    private String stan;
    private int step;
    private int ej;
    private String message;
    private boolean sync;
    private String txRquid;
    private String RClientID;
    private String pcode;

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getEj() {
        return ej;
    }

    public void setEj(int ej) {
        this.ej = ej;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public String getTxRquid() {
        return txRquid;
    }

    public void setTxRquid(String txRquid) {
        this.txRquid = txRquid;
    }

    public String getRClientID() {
        return RClientID;
    }

    public void setRClientID(String RClientID) {
        this.RClientID = RClientID;
    }

    public String getPcode() {
        return pcode;
    }

    public void setPcode(String pcode) {
        this.pcode = pcode;
    }

    /**
     * 是否對序列化後的字串, 轉為HEX字串
     *
     * @return
     */
    @Override
    public boolean isSerializedToHex() {
        return true;
    }
}
