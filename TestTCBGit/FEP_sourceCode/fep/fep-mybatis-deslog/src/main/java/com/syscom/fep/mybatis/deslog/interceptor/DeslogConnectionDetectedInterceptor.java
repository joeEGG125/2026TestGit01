package com.syscom.fep.mybatis.deslog.interceptor;

//import com.syscom.fep.common.log.LogHelperFactory;
//import com.syscom.fep.frmcommon.jdbc.DataSourceConnectionDetector;
//import com.syscom.fep.frmcommon.jdbc.DynamicDataSource;
//import com.syscom.fep.frmcommon.jdbc.DynamicDataSourceType;
//import com.syscom.fep.frmcommon.log.LogHelper;
//import com.syscom.fep.frmcommon.util.ExceptionUtil;
//import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
//import com.syscom.fep.mybatis.deslog.configuration.DataSourceDeslogConstant;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.ibatis.cache.CacheKey;
//import org.apache.ibatis.executor.Executor;
//import org.apache.ibatis.mapping.BoundSql;
//import org.apache.ibatis.mapping.MappedStatement;
//import org.apache.ibatis.plugin.*;
//import org.apache.ibatis.session.ResultHandler;
//import org.apache.ibatis.session.RowBounds;
//import org.springframework.jdbc.CannotGetJdbcConnectionException;
//
//import javax.sql.DataSource;
//import java.sql.SQLException;
//import java.util.Properties;
//import java.util.concurrent.TimeUnit;
//
//@Intercepts({
//        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
//        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
//        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
//})
public class DeslogConnectionDetectedInterceptor
//        implements Interceptor, DataSourceDeslogConstant
    {
//    private static final String CLASS_NAME = DeslogConnectionDetectedInterceptor.class.getSimpleName();
//    private static final LogHelper logger = LogHelperFactory.getGeneralLogger();
//    private DataSourceConnectionDetector masterConnectionDetector;
//
//    public DeslogConnectionDetectedInterceptor() {
//        logger.info("[", CLASS_NAME, "]start to intercept...");
//        detectInitializeMasterConnection();
//    }
//
//    @Override
//    public Object intercept(Invocation invocation) throws Throwable {
//        try {
//            return invocation.proceed();
//        } catch (Throwable t) {
//            handleConnectionException(t);
//            throw t;
//        }
//    }
//
//    /**
//     * 初始化的時候, 先檢查一下Master連線, 如果連線不成功, 則直接切到Slave
//     */
//    private void detectInitializeMasterConnection() {
//        DataSource dataSource = SpringBeanFactoryUtil.getBean(StringUtils.join(SpringBeanFactoryUtil.BEANNAME_PREFIX_SCOPEDTARGET, BEAN_NAME_DATASOURCE_DYNAMIC));
//        if (dataSource instanceof DynamicDataSource) {
//            logger.info("[", CLASS_NAME, "]start to detect initialize master connection...");
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
//    /**
//     * 如果是連線異常, 則切換到Slave資料源
//     *
//     * @param t
//     * @throws Exception
//     */
//    private void handleConnectionException(Throwable t) throws Exception {
//        // 如果無法建立連線, 則切換資料源
//        Throwable found = ExceptionUtil.find(t, (cause) -> cause instanceof CannotGetJdbcConnectionException);
//        if (found != null) {
//            DataSource dataSource = SpringBeanFactoryUtil.getBean(StringUtils.join(SpringBeanFactoryUtil.BEANNAME_PREFIX_SCOPEDTARGET, BEAN_NAME_DATASOURCE_DYNAMIC));
//            if (dataSource instanceof DynamicDataSource) {
//                DynamicDataSource dynamicDataSource = ((DynamicDataSource) dataSource);
//                dynamicDataSource.setDataSourceType(DynamicDataSourceType.SLAVE);
//                detectMasterConnection(dynamicDataSource);
//            }
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
//
//    @Override
//    public Object plugin(Object target) {
//        return Plugin.wrap(target, this);
//    }
//
//    @Override
//    public void setProperties(Properties properties) {
//    }
}
