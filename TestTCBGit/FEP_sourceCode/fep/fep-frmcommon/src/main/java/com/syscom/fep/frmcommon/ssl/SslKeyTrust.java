package com.syscom.fep.frmcommon.ssl;

import com.syscom.fep.frmcommon.annotation.IgnoreSerial;

import java.util.Objects;

public class SslKeyTrust {
    @IgnoreSerial
    private int index = 0;
    private String sslKeyPath;
    private String sslKeySscode;
    private SslKeyTrustType sslKeyType;
    private String sslTrustPath;
    private String sslTrustSscode;
    private SslKeyTrustType sslTrustType;
    private boolean deactivated;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getSslKeyPath() {
        return sslKeyPath;
    }

    public void setSslKeyPath(String sslKeyPath) {
        this.sslKeyPath = sslKeyPath;
    }

    public String getSslKeySscode() {
        return sslKeySscode;
    }

    public void setSslKeySscode(String sslKeySscode) {
        this.sslKeySscode = sslKeySscode;
    }

    public SslKeyTrustType getSslKeyType() {
        return sslKeyType;
    }

    public void setSslKeyType(SslKeyTrustType sslKeyType) {
        this.sslKeyType = sslKeyType;
    }

    public String getSslTrustPath() {
        return sslTrustPath;
    }

    public void setSslTrustPath(String sslTrustPath) {
        this.sslTrustPath = sslTrustPath;
    }

    public String getSslTrustSscode() {
        return sslTrustSscode;
    }

    public void setSslTrustSscode(String sslTrustSscode) {
        this.sslTrustSscode = sslTrustSscode;
    }

    public SslKeyTrustType getSslTrustType() {
        return sslTrustType;
    }

    public void setSslTrustType(SslKeyTrustType sslTrustType) {
        this.sslTrustType = sslTrustType;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public void setDeactivated(boolean deactivated) {
        this.deactivated = deactivated;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SslKeyTrust)) return false;
        SslKeyTrust that = (SslKeyTrust) o;
        return Objects.equals(sslKeyPath, that.sslKeyPath) && Objects.equals(sslTrustPath, that.sslTrustPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sslKeyPath, sslTrustPath);
    }
}
