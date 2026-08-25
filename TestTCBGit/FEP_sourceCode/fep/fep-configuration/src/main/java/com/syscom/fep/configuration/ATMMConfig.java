package com.syscom.fep.configuration;

import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.mybatis.model.Sysconf;

import java.util.List;

public class ATMMConfig {
    public static final int SubSystemNo = 9;
    private static ATMMConfig _instance = new ATMMConfig();
    public String atmmurl;

    public static ATMMConfig getInstance() {
        return _instance;
    }

    public String getAtmmurl() {
        return atmmurl;
    }

    public void setAtmmurl(String atmmurl) {
        this.atmmurl = atmmurl;
    }

    public static int getSubsystemno() {
        return SubSystemNo;
    }
}
