package com.syscom.fep.batch.configurer;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import jakarta.annotation.PostConstruct;
import org.quartz.CronTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

@Configuration
// @RefreshScope
public class BatchConfiguration implements DataSourceBatchConstant {
    private static final String BEAN_NAME_BATCH_QUARTZ_PROPERTIES = "batchQuartzProperties";
    public static final String SCHEDULER_JOB_FACTORY_SCHEDULER_NAME = "batchSchedulerFactoryBean";
    @Autowired
    private BatchFactory batchFactory;
    // @Value("${" + CONFIGURATION_PROPERTIES_JNDI_NAME + ":}")
    // private String jndiName;
    // @Value("${" + CONFIGURATION_PROPERTIES_MASTER_JNDI_NAME + ":}")
    // private String jndiNameMaster;
    // @Value("${" + CONFIGURATION_PROPERTIES_SLAVE_JNDI_NAME + ":}")
    // private String jndiNameSlave;
    @Value("${spring.fep.batch.subsys:2}")
    private String subSys;
    @Value("${spring.fep.batch.mailsender:}")
    private String mailSender;
    @Value("${spring.fep.batch.maillist:}")
    private String mailList;
    @Value("${spring.fep.batch.updateNextRunTimeTimerEnabled:false}")
    private boolean updateNextRunTimeTimerEnabled;
    @Value("${spring.fep.batch.updateNextRunTimeInitialDelayMilliseconds:5000}")
    private long updateNextRunTimeInitialDelayMilliseconds;
    @Value("${spring.fep.batch.updateNextRunTimeIntervalMilliseconds:60000}")
    private long updateNextRunTimeIntervalMilliseconds;
    @Value("${spring.fep.batch.quartzProperties:quartz-batch.properties}")
    private String quartzProperties;
    @Value("${spring.fep.batch.execute.develop:false}")
    private boolean executeDevelop = false;
    @Value("${spring.fep.batch.execute.shell.path:}")
    private String executeShellPath;
    @Value("${spring.fep.batch.execute.shell.output:false}")
    private boolean executeShellOutput;
    @Value("${spring.fep.batch.execute.shell.charsetName:UTF-8}")
    private String executeShellCharsetName = StandardCharsets.UTF_8.displayName();
    @Value("${spring.fep.batch.resultFilePath:}")
    private String resultFilePath;
    @Value("${spring.fep.batch.misfirePolicy:2}")
    private int misfirePolicy = CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING;
    @Value("#{'${spring.fep.batch.smsTelList:}'.split(',')}")
    private String[] smsTelList;
    @Value("${spring.fep.batch.checkAndNotifyAllMisfiredInitialDelaySeconds:1800}")
    private long checkAndNotifyAllMisfiredInitialDelaySeconds;
    @Value("${spring.fep.batch.checkAndNotifyAllMisfiredIntervalSeconds:1800}")
    private int checkAndNotifyAllMisfiredIntervalSeconds;
    @Value("${spring.fep.batch.checkAndNotifyAllMisfiredToleranceSeconds:60}")
    private int checkAndNotifyAllMisfiredToleranceSeconds;

//    @Autowired
//    private PKIConfig pkiConfig;

//    @Bean(name = BEAN_NAME_HIKARI_CONFIG)
//    @ConfigurationProperties(prefix = HIKARI_CONFIG_PROPERTIES_PREFIX)
//    public HikariConfig hikariConfig() {
//        return new HikariConfig();
//    }

//    @Bean(name = BEAN_NAME_DATASOURCE)
//    @ConfigurationProperties(prefix = CONFIGURATION_PROPERTIES_PREFIX)
//    @RefreshScope
//    public DataSource dataSource(@Qualifier(BEAN_NAME_HIKARI_CONFIG) HikariConfig hikariConfig) throws Exception {
//        // 先嘗試抓JNDI的設置, 如果沒有則抓JDBC的設置
//        if (StringUtils.isNotBlank(this.jndiName)) {
//            try {
//                JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
//                bean.setJndiName(this.jndiName);
//                bean.setProxyInterface(DataSource.class);
//                bean.setLookupOnStartup(false);
//                bean.afterPropertiesSet();
//                return (DataSource) bean.getObject();
//            } catch (NamingException e) {
//                LogHelperFactory.getTraceLogger().exceptionMsg(e, "bind JNDI name = [", this.jndiName, "] failed!!!");
//                throw e; // 直接丟異常出去, 啟動就會失敗
//            }
//        }
//        File acctFile = pkiConfig.getPkiAcctFile(FEPDBName.FEPDB);
//        File sscodeFile = pkiConfig.getPkiSscodeFile(FEPDBName.FEPDB);
//        try (InputStream acctIn = IOUtil.openInputStream(acctFile.getPath());
//             InputStream sscodeIn = IOUtil.openInputStream(sscodeFile.getPath());) {
//            Properties acctProp = new Properties();
//            Properties sscodeProp = new Properties();
//            acctProp.load(acctIn);
//            sscodeProp.load(sscodeIn);
//            if (hikariConfig == null) {
//                hikariConfig = new HikariConfig();
//            }
//            hikariConfig.setPoolName(POOL_NAME);
//            hikariConfig.setUsername(acctProp.getProperty(CONFIGURATION_PROPERTIES_USERNAME));
//            hikariConfig.setPassword(Jasypt.decrypt(sscodeProp.getProperty(CONFIGURATION_PROPERTIES_PASSWORD)));
//            hikariConfig.setDriverClassName(EnvPropertiesUtil.getProperty(CONFIGURATION_PROPERTIES_DRIVER_CLASS_NAME, null));
//            hikariConfig.setJdbcUrl(EnvPropertiesUtil.getProperty(CONFIGURATION_PROPERTIES_JDBC_URL, null));
//            printHikariConfig(hikariConfig);
//            HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
//            return DataSourceConnectionDetector.connectionDetect(hikariDataSource);
//        } catch (Exception e) {
//            LogHelperFactory.getTraceLogger().error(e, "Load PKI file with exception occur, ", e.getMessage());
//            // return DataSourceBuilder.create().build();
//            throw e; // 直接丟異常出去, 啟動就會失敗
//        }
//    }

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
//            } catch (NamingException e) {
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
//            } catch (NamingException e) {
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
//        DynamicDataSource dynamicDataSource = new DynamicDataSource(FEPDBName.BATCHDB.name());
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

    @Bean(name = BEAN_NAME_BATCH_QUARTZ_PROPERTIES)
    public Properties batchQuartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource(this.quartzProperties));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    @Bean(name = SCHEDULER_JOB_FACTORY_SCHEDULER_NAME)
    public SchedulerFactoryBean batchSchedulerFactoryBean(@Qualifier(BEAN_NAME_BATCH_QUARTZ_PROPERTIES) Properties properties) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setBeanName(SCHEDULER_JOB_FACTORY_SCHEDULER_NAME);
        schedulerFactoryBean.setJobFactory(batchFactory);
        schedulerFactoryBean.setOverwriteExistingJobs(true);
        schedulerFactoryBean.setStartupDelay(20);
        schedulerFactoryBean.setQuartzProperties(properties);
        schedulerFactoryBean.setSchedulerName(properties.getProperty("org.quartz.scheduler.instanceName", "Batch"));
        schedulerFactoryBean.setAutoStartup(true);
        schedulerFactoryBean.setSchedulerListeners(new BatchSchedulerListener(schedulerFactoryBean));
        schedulerFactoryBean.setGlobalTriggerListeners(new BatchTriggerListener(schedulerFactoryBean));
        return schedulerFactoryBean;
    }

    public String getSubSys() {
        return subSys;
    }

    public List<String> getSubSysList() {
        return StringUtil.split(subSys, ';', ',');
    }

    public String getMailSender() {
        return mailSender;
    }

    public String getMailList() {
        return mailList;
    }

    public boolean isUpdateNextRunTimeTimerEnabled() {
        return updateNextRunTimeTimerEnabled;
    }

    public void setUpdateNextRunTimeTimerEnabled(boolean updateNextRunTimeTimerEnabled) {
        this.updateNextRunTimeTimerEnabled = updateNextRunTimeTimerEnabled;
    }

    public long getUpdateNextRunTimeIntervalMilliseconds() {
        return updateNextRunTimeIntervalMilliseconds;
    }

    public long getUpdateNextRunTimeInitialDelayMilliseconds() {
        return updateNextRunTimeInitialDelayMilliseconds;
    }

    public boolean isExecuteDevelop() {
        return executeDevelop;
    }

    public String getExecuteShellPath() {
        return executeShellPath;
    }

    public boolean isExecuteShellOutput() {
        return executeShellOutput;
    }

    public String getExecuteShellCharsetName() {
        return executeShellCharsetName;
    }

    public String getResultFilePath() {
        return resultFilePath;
    }

    public int getMisfirePolicy() {
        return misfirePolicy;
    }

    public String[] getSmsTelList() {return smsTelList;}

    public void setSmsTelList(String[] smsTelList) {this.smsTelList = smsTelList;}

    public long getCheckAndNotifyAllMisfiredInitialDelaySeconds() {
        return checkAndNotifyAllMisfiredInitialDelaySeconds;
    }

    public void setCheckAndNotifyAllMisfiredInitialDelaySeconds(long checkAndNotifyAllMisfiredInitialDelaySeconds) {
        this.checkAndNotifyAllMisfiredInitialDelaySeconds = checkAndNotifyAllMisfiredInitialDelaySeconds;
    }

    public int getCheckAndNotifyAllMisfiredIntervalSeconds() {
        return checkAndNotifyAllMisfiredIntervalSeconds;
    }

    public void setCheckAndNotifyAllMisfiredIntervalSeconds(int checkAndNotifyAllMisfiredIntervalSeconds) {
        this.checkAndNotifyAllMisfiredIntervalSeconds = checkAndNotifyAllMisfiredIntervalSeconds;
    }

    public int getCheckAndNotifyAllMisfiredToleranceSeconds() {
        return checkAndNotifyAllMisfiredToleranceSeconds;
    }

    public void setCheckAndNotifyAllMisfiredToleranceSeconds(int checkAndNotifyAllMisfiredToleranceSeconds) {
        this.checkAndNotifyAllMisfiredToleranceSeconds = checkAndNotifyAllMisfiredToleranceSeconds;
    }

    @PostConstruct
    public void print() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "Batch Job Configuration", "BEAN_NAME_BATCH_QUARTZ_PROPERTIES", "SCHEDULER_JOB_FACTORY_SCHEDULER_NAME", "batchFactory"));
    }
}
