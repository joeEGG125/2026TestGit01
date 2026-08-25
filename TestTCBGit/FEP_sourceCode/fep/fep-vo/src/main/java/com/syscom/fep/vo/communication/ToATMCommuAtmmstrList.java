package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * 回應ATMGW的查詢Atmmstr List
 *
 * @author Richard
 */
@XStreamAlias("response")
public class ToATMCommuAtmmstrList extends BaseXmlCommu {
    @XStreamAlias("atmmstrs")
    private List<ToATMCommuAtmmstr> atmmstrs;

    public List<ToATMCommuAtmmstr> getAtmmstrs() {
        return atmmstrs;
    }

    public void setAtmmstrs(List<ToATMCommuAtmmstr> atmmstrs) {
        this.atmmstrs = atmmstrs;
    }

    @Override
    protected boolean isSerializedToHex() {
        return true;
    }

    @Override
    protected boolean isCompressed() {
        return CollectionUtils.isNotEmpty(atmmstrs);
    }

    @XStreamAlias("atmmstr")
    public static class ToATMCommuAtmmstr {
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
}
