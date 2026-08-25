package com.syscom.fep.configuration;

import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.mybatis.model.Sysconf;

import java.util.List;

public class GWConfig {
    public static final int SubSystemNo = 8;
    private static GWConfig _instance = new GWConfig();
    private int _AATimeout;
    private String _DEAD_ATM;
    private String _ListenIP;
    private int _ListenPort;
    private String _RCV_ATM;
    private String _TO_ATM;
    private String _ATMCertNo;
    private String _ATMCertNo_Old;
    private String _ATMMUrl;

    private GWConfig() {
        fillDataToProperty();
    }

    /**
     * 等待AA回應的Timeout時間
     *
     * @return
     */
    public int getAATimeout() {
        return _AATimeout;
    }

    /**
     * ATMGW Timeout後移至此queue
     *
     * @return
     */
    public String getDeadAtm() {
        return _DEAD_ATM;
    }

    /**
     * ATMGW服務的IP
     *
     * @return
     */
    public String getListenIP() {
        return _ListenIP;
    }

    /**
     * ATMGW服務的Port
     *
     * @return
     */
    public int getListenPort() {
        return _ListenPort;
    }

    /**
     * ATMGW送給AA Service的Queue
     *
     * @return
     */
    public String getRcvAtm() {
        return _RCV_ATM;
    }

    /**
     * AA Service送回ATMGW的Queue
     *
     * @return
     */
    public String getToAtm() {
        return _TO_ATM;
    }

    /**
     * ATM目前憑證版號
     *
     * @return
     */
    public String getATMCertNo() {
        return _ATMCertNo;
    }

    /**
     * ATM舊憑證版號
     *
     * @return
     */
    public String getATMCertNo_Old() {
        return _ATMCertNo_Old;
    }

    /**
     * ATMM Url
     *
     * @return
     */
    public String getATMMUrl() {
        return _ATMMUrl;
    }

    public static GWConfig getInstance() {
        return _instance;
    }

    private void fillDataToProperty() {
        List<Sysconf> sysconfList = FEPCache.getSysconfList(SubSystemNo);
        String sysconfValue = null;
        for (Sysconf sysconf : sysconfList) {
            sysconfValue = sysconf.getSysconfValue();
            switch (sysconf.getSysconfName()) {
                case "AATimeout":
                    this._AATimeout = DbHelper.toInteger(sysconfValue);
                    break;
                case "DEAD_ATM":
                    this._DEAD_ATM = DbHelper.toString(sysconfValue);
                    break;
                case "ListenIP":
                    this._ListenIP = DbHelper.toString(sysconfValue);
                    break;
                case "ListenPort":
                    this._ListenPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "RCV_ATM":
                    this._RCV_ATM = DbHelper.toString(sysconfValue);
                    break;
                case "TO_ATM":
                    this._TO_ATM = DbHelper.toString(sysconfValue);
                    break;
                case "ATMCertNo":
                    _ATMCertNo = DbHelper.toString(sysconfValue);
                    break;
                case "ATMCertNo_Old":
                    _ATMCertNo_Old = DbHelper.toString(sysconfValue);
                    break;
                case "ATMMUrl":
                    _ATMMUrl = DbHelper.toString(sysconfValue);
                    break;
            }
        }
    }
}
