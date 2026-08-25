package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 回應ATMGW的交易類電文
 *
 * @author Richard
 */
@XStreamAlias("response")
public class ToATMCommu extends BaseXmlCommu {
    private String atmno;
    private String ej;
    private String message;
    private String txRquid;

    public String getAtmno() {
        return atmno;
    }

    public void setAtmno(String atmno) {
        this.atmno = atmno;
    }

    public String getEj() {
        return ej;
    }

    public void setEj(String ej) {
        this.ej = ej;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTxRquid() {
        return txRquid;
    }

    public void setTxRquid(String txRquid) {
        this.txRquid = txRquid;
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
