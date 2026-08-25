package com.syscom.fep.gateway.netty.cbs.server;

import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.SocketType;
import com.syscom.fep.gateway.netty.NettyTransmissionServerConfiguration;

import jakarta.annotation.PostConstruct;

public class CBSGatewayServerConfiguration extends NettyTransmissionServerConfiguration {
    /**
     * 電文格式,有CBS, FISC, 473X三種設定
     */
    private String cbsType;
    /**
     * 是否有啟用
     */
    private boolean enable = true;
    /**
     * 是否嘗試重送電文到IMS
     */
    private boolean retrySendToIMS = true;

    /**
     * 專門用於非SpringBean模式下初始化
     */
    @Override
    public void initialization() {
        super.initialization();
        this.init();
    }

    @PostConstruct
    public void init() {
        super.setSocketType(SocketType.Server);
        super.setGateway(Gateway.CBSGW);
    }

    /**
     * @return
     */
    @Override
    public final Gateway getGateway() {
        return super.getGateway();
    }

    /**
     * @param gateway
     */
    @Override
    public final void setGateway(Gateway gateway) {}

    /**
     * @return
     */
    @Override
    public final SocketType getSocketType() {
        return super.getSocketType();
    }

    /**
     * @param socketType
     */
    @Override
    public final void setSocketType(SocketType socketType) {}

    public String getCbsType() {
        return cbsType;
    }

    public void setCbsType(String cbsType) {
        this.cbsType = cbsType;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isRetrySendToIMS() {
        return retrySendToIMS;
    }

    public void setRetrySendToIMS(boolean retrySendToIMS) {
        this.retrySendToIMS = retrySendToIMS;
    }
}
