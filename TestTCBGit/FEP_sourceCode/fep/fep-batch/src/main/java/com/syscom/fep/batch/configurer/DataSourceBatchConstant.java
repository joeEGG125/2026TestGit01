package com.syscom.fep.batch.configurer;

import com.syscom.fep.base.enums.FEPDBName;
import com.syscom.fep.common.log.LogHelperFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.util.PropertyElf;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.TreeSet;

public interface DataSourceBatchConstant {
    /**
     * DataSource Name with lower case, please refer {@link FEPDBName#BATCHDB}
     */
    public static final String DS_NAME = "batchdb";
    /**
     * DataSource Configuration Pool Name
     */
    public static final String POOL_NAME = DS_NAME.toUpperCase() + "Pool";
    /**
     * properties key
     */
    public static final String CONFIGURATION_PROPERTIES_PREFIX = "spring.datasource." + DS_NAME;
    public static final String CONFIGURATION_PROPERTIES_USERNAME = CONFIGURATION_PROPERTIES_PREFIX + ".username";
//    public static final String CONFIGURATION_PROPERTIES_PASSWORD = CONFIGURATION_PROPERTIES_PREFIX + ".password";
    public static final String CONFIGURATION_PROPERTIES_DRIVER_CLASS_NAME = CONFIGURATION_PROPERTIES_PREFIX + ".driver-class-name";
    public static final String CONFIGURATION_PROPERTIES_JDBC_URL = CONFIGURATION_PROPERTIES_PREFIX + ".jdbc-url";
    /**
     * properties key for master
     */
    public static final String CONFIGURATION_PROPERTIES_PREFIX_MASTER = CONFIGURATION_PROPERTIES_PREFIX + ".master";
    public static final String CONFIGURATION_PROPERTIES_MASTER_USERNAME = CONFIGURATION_PROPERTIES_PREFIX_MASTER + ".username";
//    public static final String CONFIGURATION_PROPERTIES_MASTER_PASSWORD = CONFIGURATION_PROPERTIES_PREFIX_MASTER + ".password";
    public static final String CONFIGURATION_PROPERTIES_MASTER_DRIVER_CLASS_NAME = CONFIGURATION_PROPERTIES_PREFIX_MASTER + ".driver-class-name";
    public static final String CONFIGURATION_PROPERTIES_MASTER_JDBC_URL = CONFIGURATION_PROPERTIES_PREFIX_MASTER + ".jdbc-url";
    /**
     * properties key for slave
     */
    public static final String CONFIGURATION_PROPERTIES_PREFIX_SLAVE = CONFIGURATION_PROPERTIES_PREFIX + ".slave";
    public static final String CONFIGURATION_PROPERTIES_SLAVE_USERNAME = CONFIGURATION_PROPERTIES_PREFIX_SLAVE + ".username";
//    public static final String CONFIGURATION_PROPERTIES_SLAVE_PASSWORD = CONFIGURATION_PROPERTIES_PREFIX_SLAVE + ".password";
    public static final String CONFIGURATION_PROPERTIES_SLAVE_DRIVER_CLASS_NAME = CONFIGURATION_PROPERTIES_PREFIX_SLAVE + ".driver-class-name";
    public static final String CONFIGURATION_PROPERTIES_SLAVE_JDBC_URL = CONFIGURATION_PROPERTIES_PREFIX_SLAVE + ".jdbc-url";
    /**
     * properties key for jndi name
     */
    public static final String CONFIGURATION_PROPERTIES_JNDI_NAME = CONFIGURATION_PROPERTIES_PREFIX + ".jndi-name";
    /**
     * properties key for master jndi name
     */
    public static final String CONFIGURATION_PROPERTIES_MASTER_JNDI_NAME = CONFIGURATION_PROPERTIES_PREFIX_MASTER + ".jndi-name";
    /**
     * properties key for slave jndi name
     */
    public static final String CONFIGURATION_PROPERTIES_SLAVE_JNDI_NAME = CONFIGURATION_PROPERTIES_PREFIX_SLAVE + ".jndi-name";
    /**
     * DataSource Bean Name
     */
    public static final String BEAN_NAME_DATASOURCE = DS_NAME + "DataSource";
    /**
     * DataSource Master Bean Name
     */
    public static final String BEAN_NAME_DATASOURCE_MASTER = BEAN_NAME_DATASOURCE + "Master";
    /**
     * DataSource Slave Bean Name
     */
    public static final String BEAN_NAME_DATASOURCE_SLAVE = BEAN_NAME_DATASOURCE + "Slave";
    /**
     * DataSource Dynamic Bean Name
     */
    public static final String BEAN_NAME_DATASOURCE_DYNAMIC = BEAN_NAME_DATASOURCE + "Dynamic";
    /**
     * Properties Bean Name
     */
    public static final String BEAN_NAME_DATASOURCE_PROPERTIES = BEAN_NAME_DATASOURCE + "Properties";
    /**
     * SqlSessionFactory Bean Name
     */
    public static final String BEAN_NAME_SQL_SESSION_FACTORY = DS_NAME + "SqlSessionFactory";
    /**
     * SqlSessionTemplate Bean Name
     */
    public static final String BEAN_NAME_SQL_SESSION_TEMPLATE = DS_NAME + "SqlSessionTemplate";
    /**
     * TransactionManager Bean Name
     */
    public static final String BEAN_NAME_TRANSACTION_MANAGER = DS_NAME + "TransactionManager";
    /**
     * TransactionTemplate Bean Name
     */
    public static final String BEAN_NAME_TRANSACTION_TEMPLATE = DS_NAME + "TransactionTemplate";
    /**
     * Hikari Config Prefix
     */
    public static final String HIKARI_CONFIG_PROPERTIES_PREFIX = CONFIGURATION_PROPERTIES_PREFIX + ".hikari";
    /**
     * Hikari Config Bean Name
     */
    public static final String BEAN_NAME_HIKARI_CONFIG = DS_NAME + "HikariConfig";

    default void printConstant() {
        Field[] fields = DataSourceBatchConstant.class.getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            StringBuilder sb = new StringBuilder();
            sb.append("Data Source ").append(DS_NAME.toUpperCase()).append(" Constant:\r\n");
            for (Field field : fields) {
                sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                        .append(field.getName())
                        .append(" = ").append(ReflectionUtils.getField(field, this)).append("\r\n");
            }
            LogHelperFactory.getGeneralLogger().info(sb.toString());
        }
    }

    default void printHikariConfig(HikariConfig hikariConfig) {
        if (hikariConfig == null) return;
        final Set<String> propertyNames = new TreeSet<>(PropertyElf.getPropertyNames(HikariConfig.class));
        if (CollectionUtils.isNotEmpty(propertyNames)) {
            int repeat = 2;
            StringBuilder sb = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();
            sb.append(hikariConfig.getPoolName()).append(" Configuration:").append("\r\n");
            String[] kernelProp = {"username", "password", "jdbcUrl", "driverClassName"};
            for (String prop : propertyNames) {
                try {
                    Object value = PropertyElf.getProperty(prop, hikariConfig);
                    if (ArrayUtils.contains(kernelProp, prop)) {
                        sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                                .append(CONFIGURATION_PROPERTIES_PREFIX).append(".")
                                .append(prop)
                                .append("=")
                                .append("password".equals(prop) ? StringUtils.repeat('*', value.toString().length()) : value).append("\r\n");
                    } else {
                        sb2.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                                .append(HIKARI_CONFIG_PROPERTIES_PREFIX).append(".")
                                .append(prop)
                                .append("=")
                                .append(value).append("\r\n");
                    }
                } catch (Exception e) {
                    // continue
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
            }
            LogHelperFactory.getGeneralLogger().info(sb.toString(), sb2.toString());
        }
    }
}
