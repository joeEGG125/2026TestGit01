package com.syscom.fep.frmcommon.jdbc;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;

public class DynamicDataSource extends AbstractRoutingDataSource {
    private LogHelper logger = new LogHelper();

    private DynamicDataSourceContextHolder contextHolder;

    public DynamicDataSource(String dsName) {
        contextHolder = new DynamicDataSourceContextHolder(dsName);
    }

    /**
     * 如果不希望數據源在啟動配置時就加載好，可以定制這個方法，從任何你希望的地方讀取並返回數據源
     * 比如從資料庫、檔案、外部接口等讀取數據源信息，並最終返回一個DataSource實現類對象即可
     */
    @Override
    protected DataSource determineTargetDataSource() {
        return super.determineTargetDataSource();
    }

    /**
     * 如果希望所有數據源在啟動配置時就加載好，這裏通過設置數據源Key值來切換數據，定製這個方法
     *
     * @return
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return contextHolder.obtainDataSourceType();
    }

    /**
     * 設置預設數據源
     *
     * @param defaultDataSource
     */
    public void setDefaultDataSource(Object defaultDataSource) {
        super.setDefaultTargetDataSource(defaultDataSource);
    }

    /**
     * 設置數據源
     *
     * @param dataSources
     */
    public void setDataSources(Map<Object, Object> dataSources) {
        super.setTargetDataSources(dataSources);
        // 將數據源的 key 放到數據源上下文的 key 集合中，用於切換時判斷數據源是否有效
        contextHolder.addDataSourceType(dataSources.keySet());
    }

    /**
     * 切換數據源
     *
     * @throws Exception
     */
    public void switchDataSource() throws Exception {
        contextHolder.switchDataSourceType();
    }

    /**
     * 設置數據源
     *
     * @throws Exception
     */
    public void setDataSourceType(DynamicDataSourceType dynamicDataSourceType) throws Exception {
        contextHolder.setDataSourceType(dynamicDataSourceType);
    }
}
