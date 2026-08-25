package com.syscom.fep.gateway.netty.atm;

import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.SocketType;
import com.syscom.fep.gateway.entity.ToFEPATM;
import com.syscom.fep.gateway.netty.NettyTransmissionServerConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "spring.fep.gateway.transmission.atm")
// @RefreshScope
public class ATMGatewayServerConfiguration extends NettyTransmissionServerConfiguration {
    public static final String BYPASSCHECKATMIP_ALL = "ALL";
    private String bypassCheckAtmIp;
    private boolean checkAtmmstrByAtmNo = false; // 是否以Atmno檢核ATMMSTR檔資料
    private boolean forwardTransmissionToFep = true; // 是否將交易類電文轉發給FEP, 否則直接返回給Client
    private int checkClientFailedTimesLimit = 3; // 連線超過checkClientFailedTimesLimit次error進入黑名單
    private int clearBlackListInterval = 3; // 黑名單每隔clearBlackListInterval分鐘自動清除
    private int checkAtmServiceInterval = 30; // 偵測已斷線fep-server-atm是否恢復連線的間隔,單位為秒
    @NestedConfigurationProperty
    private final List<ToFEPATM> tofepatm = new ArrayList<>(); // 連線ATMService的設定
    private int keepAliveTimesForUpdateAtmstate = 5; // 收到多少次KeepAlive電文後更新ATMSTATE檔
    private int checkBlackListInterval = 60; // 定時檢核黑名單的時間間隔, 單位秒
    private boolean loadTestMode = false; // 是否為壓力測試模式 just for test
    private boolean fetchCertSync = false; // 是否同步下獲取憑證, 會導致性能下降 just for test
    private boolean duplicateClientIp = false ;// 是否允許重複ClientIp just for test

    @PostConstruct
    public void initATMGatewayServer() {
        super.setSocketType(SocketType.Server);
        super.setGateway(Gateway.ATMGW);
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
    public final void setSocketType(SocketType socketType) { }

    public String getBypassCheckAtmIp() {
        return bypassCheckAtmIp;
    }

    public void setBypassCheckAtmIp(String bypassCheckAtmIp) {
        this.bypassCheckAtmIp = bypassCheckAtmIp;
    }

    public boolean isCheckAtmmstrByAtmNo() {
        return checkAtmmstrByAtmNo;
    }

    public void setCheckAtmmstrByAtmNo(boolean checkAtmmstrByAtmNo) {
        this.checkAtmmstrByAtmNo = checkAtmmstrByAtmNo;
    }

    public boolean isForwardTransmissionToFep() {
        return forwardTransmissionToFep;
    }

    public void setForwardTransmissionToFep(boolean forwardTransmissionToFep) {
        this.forwardTransmissionToFep = forwardTransmissionToFep;
    }

    public int getCheckClientFailedTimesLimit() {
        return checkClientFailedTimesLimit;
    }

    public void setCheckClientFailedTimesLimit(int checkClientFailedTimesLimit) {
        this.checkClientFailedTimesLimit = checkClientFailedTimesLimit;
    }

    public int getClearBlackListInterval() {
        return clearBlackListInterval;
    }

    public void setClearBlackListInterval(int clearBlackListInterval) {
        this.clearBlackListInterval = clearBlackListInterval;
    }

    public int getCheckAtmServiceInterval() {
        return checkAtmServiceInterval;
    }

    public void setCheckAtmServiceInterval(int checkAtmServiceInterval) {
        this.checkAtmServiceInterval = checkAtmServiceInterval;
    }

    public List<ToFEPATM> getTofepatm() {
        return tofepatm;
    }

    public int getKeepAliveTimesForUpdateAtmstate() {
        return keepAliveTimesForUpdateAtmstate;
    }

    public void setKeepAliveTimesForUpdateAtmstate(int keepAliveTimesForUpdateAtmstate) {
        this.keepAliveTimesForUpdateAtmstate = keepAliveTimesForUpdateAtmstate;
    }

    public int getCheckBlackListInterval() {
        return checkBlackListInterval;
    }

    public void setCheckBlackListInterval(int checkBlackListInterval) {
        this.checkBlackListInterval = checkBlackListInterval;
    }

    public boolean isLoadTestMode() {
        return loadTestMode;
    }

    public void setLoadTestMode(boolean loadTestMode) {
        this.loadTestMode = loadTestMode;
    }

    public boolean isFetchCertSync() {
        return fetchCertSync;
    }

    public void setFetchCertSync(boolean fetchCertSync) {
        this.fetchCertSync = fetchCertSync;
    }

    public boolean isDuplicateClientIp() {
        return duplicateClientIp;
    }

    public void setDuplicateClientIp(boolean duplicateClientIp) {
        this.duplicateClientIp = duplicateClientIp;
    }
}
