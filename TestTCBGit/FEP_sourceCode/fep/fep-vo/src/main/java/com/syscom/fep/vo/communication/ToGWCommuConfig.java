package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("response")
public class ToGWCommuConfig extends BaseXmlCommu {
    @XStreamAlias("cmn")
    private CMN cmn;
    @XStreamAlias("gw")
    private GW gw;
    // @XStreamAlias("inbk")
    // private INBK inbk;

    public CMN getCmn() {
        return cmn;
    }

    public void setCmn(CMN cmn) {
        this.cmn = cmn;
    }

    public GW getGw() {
        return gw;
    }

    public void setGw(GW gw) {
        this.gw = gw;
    }

    // public INBK getInbk() {
    //     return inbk;
    // }

    // public void setInbk(INBK inbk) {
    //     this.inbk = inbk;
    // }

    @XStreamAlias("cmn")
    public static class CMN {
        //private int listenBacklog;
        private int keepAliveTime;
        private int keepAliveInterval;

        // public int getListenBacklog() {
        //     return listenBacklog;
        // }

        // public void setListenBacklog(int listenBacklog) {
        //     this.listenBacklog = listenBacklog;
        // }

        public int getKeepAliveTime() {
            return keepAliveTime;
        }

        public void setKeepAliveTime(int keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
        }

        public int getKeepAliveInterval() {
            return keepAliveInterval;
        }

        public void setKeepAliveInterval(int keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
        }
    }

    @XStreamAlias("gw")
    public static class GW {
        private int aaTimeout;
        private String atmCertNo;
        private String atmCertNoOld;

        public String getAtmCertNo() {
            return atmCertNo;
        }

        public void setAtmCertNo(String atmCertNo) {
            this.atmCertNo = atmCertNo;
        }

        public String getAtmCertNoOld() {
            return atmCertNoOld;
        }

        public void setAtmCertNoOld(String atmCertNoOld) {
            this.atmCertNoOld = atmCertNoOld;
        }
        public int getAaTimeout() {
            return aaTimeout;
        }

        public void setAaTimeout(int aaTimeout) {
            this.aaTimeout = aaTimeout;
        }
    }

    // @XStreamAlias("inbk")
    // public static class INBK {
    //     private String atmCertNo;
    //     private String atmCertNoOld;

    //     public String getAtmCertNo() {
    //         return atmCertNo;
    //     }

    //     public void setAtmCertNo(String atmCertNo) {
    //         this.atmCertNo = atmCertNo;
    //     }

    //     public String getAtmCertNoOld() {
    //         return atmCertNoOld;
    //     }

    //     public void setAtmCertNoOld(String atmCertNoOld) {
    //         this.atmCertNoOld = atmCertNoOld;
    //     }
    // }
}
