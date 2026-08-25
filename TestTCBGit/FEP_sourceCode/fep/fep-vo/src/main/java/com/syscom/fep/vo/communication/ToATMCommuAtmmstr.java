package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 回應ATMGW的查詢Atmmstr
 *
 * @author Richard
 */
@XStreamAlias("response")
public class ToATMCommuAtmmstr extends BaseXmlCommu {
    private String atmAtmno;
    private String atmZone;
    private boolean atmCheckMac;
    private short atmstatSec;
    private int atmstatSocket;
    private int atmstatInikey;
    private String atmAtmpPort;
    private String atmIp;
    private String atmCertAlias;
    private short atmFepConnection;

    public String getAtmAtmno() {
        return atmAtmno;
    }

    public void setAtmAtmno(String atmAtmno) {
        this.atmAtmno = atmAtmno;
    }

    public String getAtmZone() {
        return atmZone;
    }

    public void setAtmZone(String atmZone) {
        this.atmZone = atmZone;
    }

    public boolean isAtmCheckMac() {
        return atmCheckMac;
    }

    public void setAtmCheckMac(boolean atmCheckMac) {
        this.atmCheckMac = atmCheckMac;
    }

    public short getAtmstatSec() {
        return atmstatSec;
    }

    public void setAtmstatSec(short atmstatSec) {
        this.atmstatSec = atmstatSec;
    }

    public int getAtmstatSocket() {
        return atmstatSocket;
    }

    public void setAtmstatSocket(int atmstatSocket) {
        this.atmstatSocket = atmstatSocket;
    }

    public int getAtmstatInikey() {
        return atmstatInikey;
    }

    public void setAtmstatInikey(int atmstatInikey) {
        this.atmstatInikey = atmstatInikey;
    }

    public String getAtmAtmpPort() {
        return atmAtmpPort;
    }

    public void setAtmAtmpPort(String atmAtmpPort) {
        this.atmAtmpPort = atmAtmpPort;
    }

    public String getAtmIp() {
        return atmIp;
    }

    public void setAtmIp(String atmIp) {
        this.atmIp = atmIp;
    }

    public String getAtmCertAlias() {
        return atmCertAlias;
    }

    public void setAtmCertAlias(String atmCertAlias) {
        this.atmCertAlias = atmCertAlias;
    }

    public short getAtmFepConnection() {
        return atmFepConnection;
    }

    public void setAtmFepConnection(short atmFepConnection) {
        this.atmFepConnection = atmFepConnection;
    }
}
