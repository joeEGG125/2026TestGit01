package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 接收來自ATMGW的交易類請求電文
 *
 * @author Richard
 */
@XStreamAlias("request")
public class ToFEPATMCommu extends BaseXmlCommu {
    private String atmno;
    private String ej;
    private String message;
    private boolean sync;
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
