package com.syscom.fep.mybatis.configuration;

import com.syscom.fep.base.configurer.PKIConfig;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.cryptography.Jasypt;
import com.syscom.fep.frmcommon.ibatis.SqlSessionFactoryWrapper;
import com.syscom.fep.frmcommon.jdbc.DataSourceConnectionDetector;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.frmcommon.util.EnvPropertiesUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil.BeanOperationListener;
import com.syscom.fep.mybatis.interceptor.AuditTrailInterceptor;
import com.syscom.fep.mybatis.interceptor.LoggerInterceptor;
import com.syscom.fep.mybatis.logging.DataSourceLogImpl;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
@MapperScan(
        basePackages = {DataSourceConstant.PACKAGE_MAPPER, DataSourceConstant.PACKAGE_EXT_MAPPER},
        annotationClass = Resource.class,
        sqlSessionTemplateRef = DataSourceConstant.BEAN_NAME_SQL_SESSION_TEMPLATE,
        sqlSessionFactoryRef = DataSourceConstant.BEAN_NAME_SQL_SESSION_FACTORY,
        lazyInitialization = "${" + DataSourceConstant.CONFIGURATION_PROPERTIES_LAZY_INITIALIZATION + ":true}"
)
// @RefreshScope
public class DataSourceConfiguration implements DataSourceConstant, BeanOperationListener {
    // @Value("${spring.fep.mybatis.interceptor.connection.detected.enable:false}")
    // private boolean connectionDetectedEnable;
    @Value("${" + CONFIGURATION_PROPERTIES_JNDI_NAME + ":}")
    private String jndiName;
    // @Value("${" + CONFIGURATION_PROPERTIES_MASTER_JNDI_NAME + ":}")
    // private String jndiNameMaster;
    // @Value("${" + CONFIGURATION_PROPERTIES_SLAVE_JNDI_NAME + ":}")
    // private String jndiNameSlave;
    @Autowired
    private PKIConfig pkiConfig;
    @Value("${spring.fep.mybatis.useCustomLogging:false}")
    private boolean useCustomLogging;
    @Value("${" + CONFIGURATION_PROPERTIES_LAZY_INITIALIZATION + ":true}")
    private String lazyInitialization;

    @Bean(name = BEAN_NAME_HIKARI_CONFIG)
    @ConfigurationProperties(prefix = HIKARI_CONFIG_PROPERTIES_PREFIX)
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean(name = BEAN_NAME_DATASOURCE)
    // @ConfigurationProperties(prefix = CONFIGURATION_PROPERTIES_PREFIX)
    // @RefreshScope
    public DataSource dataSource(@Qualifier(BEAN_NAME_HIKARI_CONFIG) HikariConfig hikariConfig) throws Exception {
        // 先嘗試抓JNDI的設置, 如果沒有則抓JDBC的設置
        if (StringUtils.isNotBlank(this.jndiName)) {
            try {
                JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
                bean.setJndiName(this.jndiName);
                bean.setProxyInterface(DataSource.class);
                bean.setLookupOnStartup(false);
                bean.afterPropertiesSet();
                return DataSourceConnectionDetector.connectionDetect((DataSource) bean.getObject());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().exceptionMsg(e, "bind JNDI name = [", this.jndiName, "] failed!!!");
                throw e; // 直接丟異常出去, 啟動就會失敗
            }
        }
        LogHelperFactory.getGeneralLogger().info(" fep username  : " + pkiConfig.getProperty(CONFIGURATION_PROPERTIES_USERNAME));
        LogHelperFactory.getGeneralLogger().info(" fep password  : ***" + Jasypt.decrypt(pkiConfig.getProperty(CONFIGURATION_PROPERTIES_PASSWORD)));
        if (hikariConfig == null) {
            hikariConfig = new HikariConfig();
        }
        hikariConfig.setPoolName(POOL_NAME);
        hikariConfig.setUsername(pkiConfig.getProperty(CONFIGURATION_PROPERTIES_USERNAME));
        hikariConfig.setPassword(Jasypt.decrypt(pkiConfig.getProperty(CONFIGURATION_PROPERTIES_PASSWORD)));
        hikariConfig.setDriverClassName(EnvPropertiesUtil.getProperty(CONFIGURATION_PROPERTIES_DRIVER_CLASS_NAME, null));
        hikariConfig.setJdbcUrl(EnvPropertiesUtil.getProperty(CONFIGURATION_PROPERTIES_JDBC_URL, null));
        printHikariConfig(hikariConfig);
        HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
        return DataSourceConnectionDetector.connectionDetect(hikariDataSource);
    }

//    @Bean(name = BEAN_NAME_DATASOURCE_MASTER)
//    @ConfigurationProperties(prefix = CONFIGURATION_PROPERTIES_PREFIX_MASTER)
//    @RefreshScope
//    public DataSource masterDataSource() throws Exception {
//        // 先嘗試抓JNDI的設置, 如果沒有則抓JDBC的設置
//        if (StringUtils.isNotBlank(this.jndiNameMaster)) {
//            try {
//                JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
//                bean.setJndiName(this.jndiNameMaster);
//                bean.setProxyInterface(DataSource.class);
//                bean.setLookupOnStartup(false);
//                bean.afterPropertiesSet();
//                return (DataSource) bean.getObject();
//            } catch (Exception e) {
//                LogHelperFactory.getTraceLogger().exceptionMsg(e, "bind JNDI name = [", this.jndiNameMaster, "] failed!!!");
//                throw e; // 直接丟異常出去, 啟動就會失敗
//            }
//        }
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean(name = BEAN_NAME_DATASOURCE_SLAVE)
//    @ConfigurationProperties(prefix = CONFIGURATION_PROPERTIES_PREFIX_SLAVE)
//    @RefreshScope
//    public DataSource slaveDataSource() throws Exception {
//        // 先嘗試抓JNDI的設置, 如果沒有則抓JDBC的設置
//        if (StringUtils.isNotBlank(this.jndiNameSlave)) {
//            try {
//                JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
//                bean.setJndiName(this.jndiNameSlave);
//                bean.setProxyInterface(DataSource.class);
//                bean.setLookupOnStartup(false);
//                bean.afterPropertiesSet();
//                return (DataSource) bean.getObject();
//            } catch (Exception e) {
//                LogHelperFactory.getTraceLogger().exceptionMsg(e, "bind JNDI name = [", this.jndiNameSlave, "] failed!!!");
//                throw e; // 直接丟異常出去, 啟動就會失敗
//            }
//        }
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean(BEAN_NAME_DATASOURCE_DYNAMIC)
//    @RefreshScope
//    public DataSource dynamicDataSource(@Qualifier(BEAN_NAME_DATASOURCE) DataSource dataSource,
//                                        @Qualifier(BEAN_NAME_DATASOURCE_MASTER) DataSource masterDataSource,
//                                        @Qualifier(BEAN_NAME_DATASOURCE_SLAVE) DataSource slaveDataSource) {
//        // 如果沒有設置master/slave, 則預設取single設置
//        if (EnvPropertiesUtil.hasProperty(CONFIGURATION_PROPERTIES_JNDI_NAME)
//                ||
//                (EnvPropertiesUtil.hasProperty(CONFIGURATION_PROPERTIES_DRIVER_CLASS_NAME)
//                        && EnvPropertiesUtil.hasProperty(CONFIGURATION_PROPERTIES_JDBC_URL))) {
//        return dataSource;
//        }
//        // 如果有設置主從DataSource, 則採用動態DataSource
//        DynamicDataSource dynamicDataSource = new DynamicDataSource(FEPDBName.FEPDB.name());
//        // 將 master 資料源作為預設指定的資料源
//        dynamicDataSource.setDefaultDataSource(masterDataSource);
//        // 將 master 和 slave 資料源作為指定的資料源
//        Map<Object, Object> dataSourceMap = new HashMap<>(2);
//        dataSourceMap.put(DynamicDataSourceType.MASTER, masterDataSource);
//        // 如果有設置slave, 則put
//        if (EnvPropertiesUtil.hasProperty(CONFIGURATION_PROPERTIES_SLAVE_JNDI_NAME)
//                ||
//                (EnvPropertiesUtil.hasProperty(CONFIGURATION_PROPERTIES_SLAVE_DRIVER_CLASS_NAME)
//                        && EnvPropertiesUtil.hasProperty(CONFIGURATION_PROPERTIES_SLAVE_JDBC_URL))) {
//            // 如果Slave DataSource有配置, 則放到DynamicDataSource中
//            dataSourceMap.put(DynamicDataSourceType.SLAVE, slaveDataSource);
//        }
//        dynamicDataSource.setDataSources(dataSourceMap);
//        return dynamicDataSource;
//    }

    @Bean(name = BEAN_NAME_SQL_SESSION_FACTORY)
    // @RefreshScope
    public SqlSessionFactory testSqlSessionFactory(@Qualifier(BEAN_NAME_DATASOURCE) DataSource dataSource) throws Exception {
        LoggerInterceptor loggerInterceptor = SpringBeanFactoryUtil.getBean(LoggerInterceptor.class, false);
        if (useCustomLogging || loggerInterceptor != null) {
            LogFactory.useCustomLogging(DataSourceLogImpl.class);
        }
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        if (Boolean.parseBoolean(this.lazyInitialization)) {
            SpringBeanFactoryUtil.addBeanOperationListener(this);
        } else {
            bean.setMapperLocations(
                    ArrayUtils.addAll(
                            new PathMatchingResourcePatternResolver().getResources("classpath*:" + ClassUtils.convertClassNameToResourcePath(PACKAGE_MAPPER) + "/xml/*.xml"),
                            new PathMatchingResourcePatternResolver().getResources("classpath*:" + ClassUtils.convertClassNameToResourcePath(PACKAGE_EXT_MAPPER) + "/xml/*.xml")));
        }
        bean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("classpath:mybatis-config.xml"));
        // 增加攔截器
        List<Interceptor> interceptorList = new ArrayList<Interceptor>();
        AuditTrailInterceptor auditTrailInterceptor = SpringBeanFactoryUtil.getBean(AuditTrailInterceptor.class, false);
        if (auditTrailInterceptor != null) {
            interceptorList.add(auditTrailInterceptor);
        } else {
            LogHelperFactory.getTraceLogger().warn("##########AuditTrailInterceptor has been not register as Spring Object, so that no need to record AuditTrail!!!");
        }
        if (loggerInterceptor != null) {
            interceptorList.add(loggerInterceptor);
        } else {
            LogHelperFactory.getTraceLogger().warn("##########LoggerInterceptor has been not register as Spring Object, so that no need to logging SQL statements!!!");
        }
//        if (dataSource instanceof DynamicDataSource && this.connectionDetectedEnable) {
//            ConnectionDetectedInterceptor connectionDetectedInterceptor = new ConnectionDetectedInterceptor();
//            interceptorList.add(connectionDetectedInterceptor);
//        }
        // 分頁插件
        // PageInterceptor pageInterceptor = new PageInterceptor();
        // pageInterceptor.setProperties(new Properties() {
        // private static final long serialVersionUID = 1L;
        // {
        // setProperty("pageSizeZero", Boolean.TRUE.toString());
        // }
        // });
        // interceptorList.add(pageInterceptor);
        if (!interceptorList.isEmpty()) {
            Interceptor[] interceptors = new Interceptor[interceptorList.size()];
            interceptorList.toArray(interceptors);
            bean.setPlugins(interceptors);
        }
        SqlSessionFactory sqlSessionFactory = bean.getObject();
        return Boolean.parseBoolean(this.lazyInitialization) ? new SqlSessionFactoryWrapper(sqlSessionFactory) : sqlSessionFactory;
    }

    @Bean(name = BEAN_NAME_SQL_SESSION_TEMPLATE)
    @Lazy
    // @RefreshScope
    public SqlSessionTemplate testSqlSessionTemplate(@Qualifier(BEAN_NAME_SQL_SESSION_FACTORY) SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = BEAN_NAME_TRANSACTION_MANAGER)
    @Lazy
    public DataSourceTransactionManager testTransactionManager(@Qualifier(BEAN_NAME_DATASOURCE) DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = BEAN_NAME_TRANSACTION_TEMPLATE)
    @Lazy
    public TransactionTemplate testTransactionTemplate(@Qualifier(BEAN_NAME_TRANSACTION_MANAGER) DataSourceTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean(name = BEAN_NAME_JDBC_TEMPLATE)
    @Lazy
    public JdbcTemplate jdbcTemplate(@Qualifier(BEAN_NAME_DATASOURCE) DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @PostConstruct
    public void print() {
        printConstant();
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, StringUtils.join(DS_NAME.toUpperCase(), " Configuration"), "pkiConfig"));
    }

    /**
     * 從mybatis-config.xml中獲取<property name="currentSchema" />值
     * 此方法給以下地方使用：
     * 1. UserDefineExtMapper.xml中call SP的方法
     *
     * @return
     */
    public static String getPropCurrentSchema() {
        SqlSessionFactory factory = SpringBeanFactoryUtil.getBean(BEAN_NAME_SQL_SESSION_FACTORY);
        String currentSchema = (String) factory.getConfiguration().getVariables().get("currentSchema");
        return currentSchema;
    }

    /**
     * 動態載入Mapper的XML檔
     *
     * @param bean
     * @param beanName
     */
    @Override
    public void postProcessAfterInitialization(Object bean, String beanName) {
        if (!(bean instanceof MapperFactoryBean))
            return;
        Class<?> clazz = ((MapperFactoryBean<?>) bean).getMapperInterface();
        String mapperPackage = PACKAGE_MAPPER, extMapperPackage = PACKAGE_EXT_MAPPER;
        if (mapperPackage.equals(clazz.getPackage().getName()) || extMapperPackage.equals(clazz.getPackage().getName())) {
            String className = clazz.getSimpleName();
            String suffixMapper = "Mapper", suffixExtMapper = "ExtMapper";
            String mapperName = StringUtils.EMPTY, extMapperName = StringUtils.EMPTY;
            if (className.endsWith(suffixExtMapper)) {
                mapperName = StringUtils.join(className.substring(0, className.lastIndexOf(suffixExtMapper)), suffixMapper);
                extMapperName = className;
            } else if (className.endsWith(suffixMapper)) {
                mapperName = className;
                extMapperName = StringUtils.join(className.substring(0, className.lastIndexOf(suffixMapper)), suffixExtMapper);
            }
            List<org.springframework.core.io.Resource> mapperLocations = new ArrayList<>();
            mapperLocations.add(new PathMatchingResourcePatternResolver().getResource("classpath:" + ClassUtils.convertClassNameToResourcePath(mapperPackage) + "/xml/" + mapperName + ".xml"));
            mapperLocations.add(new PathMatchingResourcePatternResolver().getResource("classpath:" + ClassUtils.convertClassNameToResourcePath(extMapperPackage) + "/xml/" + extMapperName + ".xml"));
            SqlSessionFactory factory = SpringBeanFactoryUtil.getBean(BEAN_NAME_SQL_SESSION_FACTORY);
            org.apache.ibatis.session.Configuration configuration = factory.getConfiguration();
            for (org.springframework.core.io.Resource mapperLocation : mapperLocations) {
                if (mapperLocation == null || !mapperLocation.exists()) {
                    continue;
                }
                try {
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(mapperLocation.getInputStream(), configuration, mapperLocation.toString(), configuration.getSqlFragments());
                    xmlMapperBuilder.parse();
                    LogHelperFactory.getTraceLogger().debug("==> Succeed Lazy Initialization Mapper XML File: '", mapperLocation, "'");
                } catch (Exception e) {
                    LogHelperFactory.getTraceLogger().error(e, "Failed to Lazy Initialization Mapper XML File: '", mapperLocation, "' ", e.getMessage());
                } finally {
                    ErrorContext.instance().reset();
                }
            }
        }
    }
}