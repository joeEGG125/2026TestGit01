package com.syscom.fep.vo.monitor;

import com.syscom.fep.frmcommon.os.data.DBPoolData;
import com.syscom.fep.frmcommon.os.data.JvmData;

import java.util.List;

public class ServiceOthers {
    private JvmData jvmData;
    private List<DBPoolData> dbPools;
    private DBPoolData dbPoolStatistics;

    public JvmData getJvmData() {
        return jvmData;
    }

    public void setJvmData(JvmData jvmData) {
        this.jvmData = jvmData;
    }

    public List<DBPoolData> getDbPools() {
        return dbPools;
    }

    public void setDbPools(List<DBPoolData> dbPools) {
        this.dbPools = dbPools;
    }

    public DBPoolData getDbPoolStatistics() {
        return dbPoolStatistics;
    }

    public void setDbPoolStatistics(DBPoolData dbPoolStatistics) {
        this.dbPoolStatistics = dbPoolStatistics;
    }
}
