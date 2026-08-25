package com.syscom.fep.gateway.ims.cbs;

import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.ims.IMSTransmissionConfiguration;
import jakarta.annotation.PostConstruct;

public class CBSGatewayClientConfiguration extends IMSTransmissionConfiguration {
    /**
     * 電文格式,有CBS, FISC, 473X三種設定
     */
    private String cbsType;
    /**
     * 映射的腳位序號
     */
    private int mappingIndex;
    /**
     * 是否有啟用
     */
    private boolean enable;
    /**
     * CBSGatewayLineType
     */
    private CBSGatewayLineType lineType;
    /**
     * 是否暫停
     */
    private boolean pause;
    /**
     * com.syscom.fep.gateway.ims.cbs.CBSGatewayGroup#connStateChanged方法內用來進行sync lock的物件
     */
    private String lockConnStateChanged;

    /**
     * 專門用於非SpringBean模式下初始化
     */
    @Override
    public void initialization() {
        this.postConstruct();
        super.initialization();
    }

    @PostConstruct
    public void postConstruct() {
        super.setGateway(Gateway.CBSGW);
    }

    @Override
    public final Gateway getGateway() {
        return Gateway.CBSGW;
    }

    @Override
    public final void setGateway(Gateway gateway) {}

    public String getCbsType() {
        return cbsType;
    }

    public void setCbsType(String cbsType) {
        this.cbsType = cbsType;
    }

    public int getMappingIndex() {
        return mappingIndex;
    }

    public void setMappingIndex(int mappingIndex) {
        this.mappingIndex = mappingIndex;
    }

    public synchronized boolean isEnable() {
        return enable;
    }

    public synchronized void setEnable(boolean enable) {
        this.enable = enable;
    }

    public synchronized boolean isPause() {
        return pause;
    }

    public synchronized void setPause(boolean pause) {
        this.pause = pause;
    }

    public CBSGatewayLineType getLineType() {
        return lineType;
    }

    public void setLineType(CBSGatewayLineType lineType) {
        this.lineType = lineType;
    }

    public boolean isActived() {
        return isEnable() && !isPause();
    }

    public String getLockConnStateChanged() {
        return lockConnStateChanged;
    }

    public void setLockConnStateChanged(String lockConnStateChanged) {
        this.lockConnStateChanged = lockConnStateChanged;
    }

    @Override
    public String getName() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getName());
        sb.append("-").append(lineType);
        sb.append("-").append(cbsType);
        return sb.toString();
    }

    @Override
    public String forLogging() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.forLogging());
        sb.append(",lineType:").append(lineType);
        sb.append(",cbsType:").append(cbsType);
        sb.append(",mappingIndex:").append(mappingIndex);
        sb.append(",enable:").append(enable);
        return sb.toString();
    }
}
