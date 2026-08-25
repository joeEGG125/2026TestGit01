package com.syscom.fep.gateway.netty.fisc.client;

import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.netty.NettyTransmissionClientConfiguration;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;

// @Validated
public class FISCGatewayClientConfiguration extends NettyTransmissionClientConfiguration {
    /**
     * 財金編碼,可能值為ascii or ebcdic
     */
    @NotNull
    private String encoding;
    /**
     * RM OPC交易的ClientID
     */
    @NotNull
    private String clientId;
    /**
     * 轉成Hex後的ClientId
     */
    private String clientIdHex;
    /**
     * checkCode
     */
    @NotNull
    private String checkCode;
    /**
     * 轉成Hex後的checkCode
     */
    private String checkCodeHex;

    /**
     * 專門用於非SpringBean模式下初始化
     */
    @Override
    public void initialization() {
        super.initialization();
        this.initFISCGatewayClient();
    }

    @PostConstruct
    public void initFISCGatewayClient() {
        super.setGateway(Gateway.FISCGW);
    }

    @Override
    public final Gateway getGateway() {
        return super.getGateway();
    }

    @Override
    public final void setGateway(Gateway gateway) {
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientIdHex() {
        return clientIdHex;
    }

    public void setClientIdHex(String clientIdHex) {
        this.clientIdHex = clientIdHex;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public String getCheckCodeHex() {
        return checkCodeHex;
    }

    public void setCheckCodeHex(String checkCodeHex) {
        this.checkCodeHex = checkCodeHex;
    }
}
