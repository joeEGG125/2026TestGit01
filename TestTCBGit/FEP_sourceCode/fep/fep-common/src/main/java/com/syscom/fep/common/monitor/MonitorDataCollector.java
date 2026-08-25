package com.syscom.fep.common.monitor;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.esapi.ESAPIUtil;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClient2;
import com.syscom.fep.frmcommon.os.OperationSystemDataCollector;
import com.syscom.fep.frmcommon.os.data.DBPoolData;
import com.syscom.fep.frmcommon.os.data.FileStoreData;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.PathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.jndi.JndiObjectTargetSource;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MonitorDataCollector {
    private static final LogHelper logger = LogHelperFactory.getTraceLogger();

    private MonitorDataCollector() {}

    /**
     * 獲取系統CPU使用率
     *
     * @param httpClient
     * @param url
     * @return
     * @throws Throwable
     */
    public static Number fetchSystemCpuUsage(final HttpClient2 httpClient, final String url) throws Throwable {
        // String promQL = "/actuator/metrics/system.cpu.usage";
        // String jsonStr = httpClient.getForObject(StringUtils.join(url, promQL), String.class);
        // JSONObject rootObject = new JSONObject(jsonStr);
        // JSONArray measurementsJsonArray = rootObject.getJSONArray("measurements");
        // if (measurementsJsonArray != null && !measurementsJsonArray.isEmpty()) {
        //     JSONObject resultObject = measurementsJsonArray.getJSONObject(0);
        //     double value = resultObject.getDouble("value");
        //     if (value >= 0) {
        //         return value * 100 * 100;
        //     }
        // }
        // return 0;
        // 2024-07-18 Richard modified 這裡取UserUsage
        return OperationSystemDataCollector.getSysCpuData().getUserUsage() * 100 * 100;
    }

    /**
     * 獲取系統記憶體已經使用MB
     *
     * @return
     * @throws Throwable
     */
    public static Number fetchSystemMemoryUsage() throws Throwable {
        // OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        // return (double) (bean.getTotalPhysicalMemorySize() - bean.getFreePhysicalMemorySize()) / 1024;
        return OperationSystemDataCollector.getSysMemory().getUsed() / 1024;
    }

    /**
     * 獲取磁盤信息
     *
     * @param hostName
     * @param hostIP
     * @return
     */
    public static List<MonitorDataDisk> fetchSystemHardDisk(final String hostName, final String hostIP) {
        List<MonitorDataDisk> list = new ArrayList<>();
        // File[] roots = File.listRoots();
        // for (File root : roots) {
        //     MonitorDataDisk disk = new MonitorDataDisk();
        //     disk.setHostName(hostName);
        //     disk.setName(root.getPath());
        //     disk.setIp(hostIP);
        //     disk.setTotal(root.getTotalSpace());
        //     disk.setFree(root.getFreeSpace());
        //     disk.setUsed(disk.getTotal() - disk.getFree());
        //     list.add(disk);
        // }
        List<FileStoreData> fileStoreDataList = OperationSystemDataCollector.getSysFileStoreData();
        for (FileStoreData fileStoreData : fileStoreDataList) {
            MonitorDataDisk disk = new MonitorDataDisk();
            disk.setHostName(hostName);
            disk.setName(fileStoreData.getMount()); // 這裡改為獲取Mount
            disk.setIp(hostIP);
            disk.setTotal(fileStoreData.getTotalSpace());
            disk.setFree(fileStoreData.getFreeSpace());
            disk.setUsed(fileStoreData.getTotalSpace() - fileStoreData.getFreeSpace());
            list.add(disk);
        }
        return list;
    }

    /**
     * 獲取指定資料夾的信息
     *
     * @param hostName
     * @param hostIP
     * @param mount
     */
    public static MonitorDataDisk fetchMonitorDataDisk(final String hostName, final String hostIP, final String mount) {
        MonitorDataDisk monitorDataDisk = new MonitorDataDisk();
        monitorDataDisk.setHostName(hostName);
        monitorDataDisk.setName(mount);
        monitorDataDisk.setIp(hostIP);
        monitorDataDisk.setTotal(-1); // 預設塞-1, 如果UI上顯示-1表示有異常無法獲取到數值
        monitorDataDisk.setFree(-1); // 預設塞-1, 如果UI上顯示-1表示有異常無法獲取到數值
        monitorDataDisk.setUsed(-1); // 預設塞-1, 如果UI上顯示-1表示有異常無法獲取到數值
        try {
            Object directory = ESAPIUtil.toFile(CleanPathUtil.cleanString(mount));
            monitorDataDisk.setTotal(((File) directory).getTotalSpace());
            monitorDataDisk.setFree(((File) directory).getUsableSpace());
            monitorDataDisk.setUsed(OperationSystemDataCollector.sizeOfDirectory((File) directory)); // 2026-07-29 Richard modified
            return monitorDataDisk;
        } catch (Throwable t) {
            logger.warn("fetchMonitorDataDisk error cause ", t.getMessage(), ", hostName:", hostName, ", hostIP:", hostIP, ", mount:", mount);
        }
        return monitorDataDisk;
    }

    /**
     * 獲取資料庫Pool信息
     *
     * @param dataSourceBeanName
     * @return
     */
    public static DBPoolData fetchDBPoolData(String dataSourceBeanName) {
        DataSource dataSource = SpringBeanFactoryUtil.getBeanQuietly(dataSourceBeanName);
        if (dataSource != null) {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                HikariPoolMXBean hikariPoolMXBean = hikariDataSource.getHikariPoolMXBean();
                if (hikariPoolMXBean != null) {
                    DBPoolData dbPoolData = new DBPoolData();
                    dbPoolData.setName(hikariDataSource.getPoolName());
                    dbPoolData.setActiveConnections(hikariPoolMXBean.getActiveConnections());
                    dbPoolData.setIdleConnections(hikariPoolMXBean.getIdleConnections());
                    dbPoolData.setThreadsAwaitingConnection(hikariPoolMXBean.getThreadsAwaitingConnection());
                    dbPoolData.setTotalConnections(hikariPoolMXBean.getTotalConnections());
                    dbPoolData.setMinimumIdle(hikariDataSource.getMinimumIdle());
                    dbPoolData.setMaximumPoolSize(hikariDataSource.getMaximumPoolSize());
                    return dbPoolData;
                }
            } else if (dataSource instanceof Advised) {
                Advised advised = (Advised) dataSource;
                TargetSource targetSource = advised.getTargetSource();
                if (targetSource instanceof JndiObjectTargetSource) {
                    JndiObjectTargetSource jndiObjectTargetSource = (JndiObjectTargetSource) targetSource;
                    String jndiName = jndiObjectTargetSource.getJndiName();
                    DBPoolData dbPoolData = new DBPoolData();
                    dbPoolData.setName(jndiName);
                    String objectName = "WebSphere:type=ConnectionPoolStats,name=" + jndiName;
                    try {
                        // 1.獲取平台 MBean 服務器
                        MBeanServer platformMBeanServer = OperationSystemDataCollector.getPlatformMBeanServer();
                        // 2.定義你要查詢的連接池 MBean 的 ObjectName
                        ObjectName poolObjectName = new ObjectName(objectName);
                        // 總連接數（包括空閑和正在使用的）
                        try {
                            Long managedConnectionCount = (Long) platformMBeanServer.getAttribute(poolObjectName, "ManagedConnectionCount");
                            dbPoolData.setTotalConnections(managedConnectionCount.intValue());
                        } catch (Exception e) {
                            logger.warn("getAttribute 'ManagedConnectionCount' failed, ", e.getMessage(), ", please check 'monitor-1.0' feature is enabled, and ObjectName is correct or not, poolObjectName:", poolObjectName.toString());
                        }
                        // 當前空閑連接數
                        try {
                            Long freeConnectionCount = (Long) platformMBeanServer.getAttribute(poolObjectName, "FreeConnectionCount");
                            dbPoolData.setIdleConnections(freeConnectionCount.intValue());
                        } catch (Exception e) {
                            logger.warn("getAttribute 'FreeConnectionCount' failed, ", e.getMessage(), ", please check 'monitor-1.0' feature is enabled, and ObjectName is correct or not, poolObjectName:", poolObjectName.toString());
                        }
                        // 當前正在使用的連接數
                        try {
                            Long connectionHandleCount = (Long) platformMBeanServer.getAttribute(poolObjectName, "ConnectionHandleCount");
                            dbPoolData.setActiveConnections(connectionHandleCount.intValue());
                        } catch (Exception e) {
                            logger.warn("getAttribute 'ConnectionHandleCount' failed, ", e.getMessage(), ", please check 'monitor-1.0' feature is enabled, and ObjectName is correct or not, poolObjectName:", poolObjectName.toString());
                        }
                        // 連接池能容納的最大連接數
                        try {
                            Long maxConnectionsLimit = (Long) platformMBeanServer.getAttribute(poolObjectName, "MaxConnectionsLimit");
                            dbPoolData.setMaximumPoolSize(maxConnectionsLimit.intValue());
                        } catch (Exception e) {
                            logger.warn("getAttribute 'MaxConnectionsLimit' failed, ", e.getMessage(), ", please check 'monitor-1.0' feature is enabled, and ObjectName is correct or not, poolObjectName:", poolObjectName.toString());
                        }
                    } catch (MalformedObjectNameException e) {
                        logger.warn("getMXBean failed, ", e.getMessage(), ", please check 'monitor-1.0' feature is enabled, and ObjectName is correct or not, objectName:", objectName);
                    }
                    return dbPoolData;
                } else {
                    logger.warn("Cannot handle '", dataSource.getClass().getName(), "' which is not a JndiObjectTargetSource, dataSourceBeanName:", dataSourceBeanName);
                }
            } else {
                logger.warn("Cannot handle '", dataSource.getClass().getName(), "', dataSourceBeanName:", dataSourceBeanName);
            }
        }
        return null;
    }
}
