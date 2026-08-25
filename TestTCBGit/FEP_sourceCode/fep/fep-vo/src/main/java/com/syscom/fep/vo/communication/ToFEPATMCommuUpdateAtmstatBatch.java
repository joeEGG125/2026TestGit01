package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * 接收來自ATMGW請求批量更新Atmstat
 *
 * @author Richard
 */
@XStreamAlias("request")
public class ToFEPATMCommuUpdateAtmstatBatch extends BaseXmlCommu {

    @XStreamAlias("atmstats")
    private List<ToFEPATMCommuUpdateAtmstatBatchData> atmstats;
    private boolean async = true;

    public List<ToFEPATMCommuUpdateAtmstatBatchData> getAtmstats() {
        return atmstats;
    }

    public void setAtmstats(List<ToFEPATMCommuUpdateAtmstatBatchData> atmstats) {
        this.atmstats = atmstats;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    @Override
    public final boolean isCompressed() {
        return CollectionUtils.isNotEmpty(atmstats);
    }

    @XStreamAlias("atmstat")
    public static class ToFEPATMCommuUpdateAtmstatBatchData {
        private String atmAtmpIp;
        private String atmstatAtmno;
        private short atmstatStatus;
        private short atmstatSocket;
        private short atmstatSec;
        private short atmstatInikey;
        private String atmstatApVersionN;

        public String getAtmAtmpIp() {
            return atmAtmpIp;
        }

        public void setAtmAtmpIp(String atmAtmpIp) {
            this.atmAtmpIp = atmAtmpIp;
        }

        public String getAtmstatAtmno() {
            return atmstatAtmno;
        }

        public void setAtmstatAtmno(String atmstatAtmno) {
            this.atmstatAtmno = atmstatAtmno;
        }

        public short getAtmstatStatus() {
            return atmstatStatus;
        }

        public void setAtmstatStatus(short atmstatStatus) {
            this.atmstatStatus = atmstatStatus;
        }

        public short getAtmstatSocket() {
            return atmstatSocket;
        }

        public void setAtmstatSocket(short atmstatSocket) {
            this.atmstatSocket = atmstatSocket;
        }

        public short getAtmstatSec() {
            return atmstatSec;
        }

        public void setAtmstatSec(short atmstatSec) {
            this.atmstatSec = atmstatSec;
        }

        public short getAtmstatInikey() {
            return atmstatInikey;
        }

        public void setAtmstatInikey(short atmstatInikey) {
            this.atmstatInikey = atmstatInikey;
        }

        public String getAtmstatApVersionN() {
            return atmstatApVersionN;
        }

        public void setAtmstatApVersionN(String atmstatApVersionN) {
            this.atmstatApVersionN = atmstatApVersionN;
        }
    }
}
