package com.syscom.fep.batch.configurer;

//import com.syscom.fep.common.log.LogHelperFactory;
//import com.syscom.fep.frmcommon.jdbc.DataSourceConnectionDetector;
//import com.syscom.fep.frmcommon.jdbc.DynamicDataSource;
//import com.syscom.fep.frmcommon.jdbc.DynamicDataSourceType;
//import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.jdbc.DataSourceConnectionDetector;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
//import org.apache.commons.lang3.StringUtils;
import org.quartz.utils.ConnectionProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
//import java.sql.SQLTransientConnectionException;
//import java.util.concurrent.TimeUnit;

public class BatchConnectionProvider implements ConnectionProvider, DataSourceBatchConstant {
    private String dataSourceName = BEAN_NAME_DATASOURCE;
    private DataSource batchDataSource;
//    private boolean connectionDetectedEnable;
//    private DataSourceConnectionDetector masterConnectionDetector;

    @Override
    public Connection getConnection() throws SQLException {
//        try {
            return batchDataSource.getConnection();
//        } catch (Throwable t) {
//            handleConnectionException(t);
//            throw t;
//        }
    }

    @Override
    public void shutdown() throws SQLException {
    }

    @Override
    public void initialize() throws SQLException {
        this.batchDataSource = SpringBeanFactoryUtil.getBean(this.getDataSourceName());
        DataSourceConnectionDetector.connectionDetect(this.batchDataSource);
//        detectInitializeMasterConnection();
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

//    public boolean isConnectionDetectedEnable() {
//        return connectionDetectedEnable;
//    }
//
//    public void setConnectionDetectedEnable(boolean connectionDetectedEnable) {
//        this.connectionDetectedEnable = connectionDetectedEnable;
//    }
//
//    /**
//     * 如果是連線異常, 則切換到Slave資料源
//     *
//     * @param t
//     * @throws SQLException
//     */
//    private void handleConnectionException(Throwable t) throws SQLException {
//        if (!connectionDetectedEnable) {
//            return;
//        }
//        // 如果無法建立連線, 則切換資料源
//        Throwable found = ExceptionUtil.find(t, (cause) -> cause instanceof SQLTransientConnectionException);
//        if (found != null) {
//            DataSource dataSource = SpringBeanFactoryUtil.getBean(StringUtils.join(SpringBeanFactoryUtil.BEANNAME_PREFIX_SCOPEDTARGET, BEAN_NAME_DATASOURCE_DYNAMIC));
//            if (dataSource instanceof DynamicDataSource) {
//                DynamicDataSource dynamicDataSource = ((DynamicDataSource) dataSource);
//                try {
//                    dynamicDataSource.setDataSourceType(DynamicDataSourceType.SLAVE);
//                    detectMasterConnection(dynamicDataSource);
//                } catch (Exception e) {
//                    throw new SQLException(e);
//                }
//            }
//        }
//    }
//
//    /**
//     * 初始化的時候, 先檢查一下Master連線, 如果連線不成功, 則直接切到Slave
//     */
//    private void detectInitializeMasterConnection() {
//        if (!connectionDetectedEnable) {
//            return;
//        }
//        DataSource dataSource = SpringBeanFactoryUtil.getBean(StringUtils.join(SpringBeanFactoryUtil.BEANNAME_PREFIX_SCOPEDTARGET, BEAN_NAME_DATASOURCE_DYNAMIC));
//        if (dataSource instanceof DynamicDataSource) {
//            LogHelperFactory.getGeneralLogger().info("[", DS_NAME, "]start to detect initialize master connection...");
//            DynamicDataSource dynamicDataSource = ((DynamicDataSource) dataSource);
//            DataSource master = SpringBeanFactoryUtil.getBean(StringUtils.join(SpringBeanFactoryUtil.BEANNAME_PREFIX_SCOPEDTARGET, BEAN_NAME_DATASOURCE_MASTER));
//            new DataSourceConnectionDetector(DS_NAME, master) {
//                @Override
//                protected void connectionEstablished() {
//                    // 檢測到一次就好
//                    this.cancel();
//                }
//
//                @Override
//                protected void connectionBreakage(SQLException e) {
//                    try {
//                        // 初始化階段連線無法建立, 則將DataSource切換到Slave
//                        dynamicDataSource.setDataSourceType(DynamicDataSourceType.SLAVE);
//                        // 同時定時檢測Master連線
//                        detectMasterConnection(dynamicDataSource);
//                    } catch (Exception ex) {
//                        logger.warn(ex, ex.getMessage());
//                    } finally {
//                        // 檢測到一次就好
//                        this.cancel();
//                    }
//                }
//            }.schedule(5, TimeUnit.SECONDS);
//        }
//    }
//
//    private void detectMasterConnection(DynamicDataSource dynamicDataSource) {
//        // 啟動一個scheduler檢測master是否有回復連線
//        if (masterConnectionDetector == null) {
//            DataSource master = SpringBeanFactoryUtil.getBean(StringUtils.join(SpringBeanFactoryUtil.BEANNAME_PREFIX_SCOPEDTARGET, BEAN_NAME_DATASOURCE_MASTER));
//            masterConnectionDetector = new DataSourceConnectionDetector(DS_NAME, master) {
//                @Override
//                protected void connectionEstablished() {
//                    // 連線恢復, 則將DataSource切換到Master
//                    try {
//                        dynamicDataSource.setDataSourceType(DynamicDataSourceType.MASTER);
//                    } catch (Exception e) {
//                        logger.warn(e, e.getMessage());
//                    } finally {
//                        // 連線恢復, 則cancel
//                        this.cancel();
//                    }
//                }
//            };
//        }
//        // 30秒檢測一次連線
//        masterConnectionDetector.scheduleAtFixedRate(5, 30, TimeUnit.SECONDS);
//    }
}
