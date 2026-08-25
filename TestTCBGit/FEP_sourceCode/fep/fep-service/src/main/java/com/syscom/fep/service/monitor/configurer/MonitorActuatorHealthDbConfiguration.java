package com.syscom.fep.service.monitor.configurer;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.frmcommon.util.JdbcUtil;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.autoconfigure.jdbc.DataSourceHealthIndicatorProperties;
import org.springframework.boot.actuate.health.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 覆寫監控DB的Indicator
 *
 * @author Richard
 */
@Configuration
@ConfigurationProperties(prefix = "spring.fep.service.monitor.actuator.health.db")
@ConditionalOnProperty(prefix = "spring.fep.service.monitor.actuator.health.db", name = "enable", havingValue = "true")
public class MonitorActuatorHealthDbConfiguration {
    private final LogHelper logger = LogHelperFactory.getServiceLogger();
    /**
     * 驗證DB連線有效性的SQL
     */
    private String validationQuery = "SELECT 1 FROM SYSIBM.SYSDUMMY1";
    /**
     * 建立DB連線以及SQL查詢的超時時間, 單位秒
     */
    private int queryTimeout = 1;
    /**
     * 是否列印pool信息
     */
    private boolean printPoolInfo = false;

    @Bean
    @ConditionalOnMissingBean(name = {"dbHealthIndicator", "dbHealthContributor"})
    public HealthContributor dbHealthContributor(Map<String, DataSource> dataSources, DataSourceHealthIndicatorProperties dataSourceHealthIndicatorProperties) {
        if (dataSourceHealthIndicatorProperties.isIgnoreRoutingDataSources()) {
            Map<String, DataSource> filteredDataSources = dataSources.entrySet()
                    .stream()
                    .filter((e) -> !(e.getValue() instanceof AbstractRoutingDataSource))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return createContributor(filteredDataSources);
        }
        return createContributor(dataSources);
    }

    private HealthContributor createContributor(Map<String, DataSource> beans) {
        Assert.notEmpty(beans, "Beans must not be empty");
        if (beans.size() == 1) {
            return createContributor(beans.values().iterator().next());
        }
        return CompositeHealthContributor.fromMap(beans, this::createContributor);
    }

    private HealthContributor createContributor(DataSource source) {
        return new DataSourceHealthIndicator(source, this.validationQuery);
    }

    @PostConstruct
    public void postConstruct() {
        LogHelperFactory.getTraceLogger().info(ConfigurationPropertiesUtil.info(this, "Monitor Actuator Health Db Configuration"));
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public boolean isPrintPoolInfo() {
        return printPoolInfo;
    }

    public void setPrintPoolInfo(boolean printPoolInfo) {
        this.printPoolInfo = printPoolInfo;
    }

    private final class DataSourceHealthIndicator extends AbstractHealthIndicator implements InitializingBean {
        private final DataSource dataSource;
        private final String query;

        public DataSourceHealthIndicator() {
            this(null, null);
        }

        public DataSourceHealthIndicator(DataSource dataSource) {
            this(dataSource, null);
        }

        public DataSourceHealthIndicator(DataSource dataSource, String query) {
            super("DataSource health check failed");
            this.dataSource = dataSource;
            this.query = query;
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            Assert.state(this.dataSource != null, "DataSource for DataSourceHealthIndicator must be specified");
        }

        @Override
        protected void doHealthCheck(Health.Builder builder) throws Exception {
            if (this.dataSource == null) {
                builder.up().withDetail("database", "unknown");
            } else {
                doDataSourceHealthCheck(builder);
            }
        }

        private void doDataSourceHealthCheck(Health.Builder builder) throws SQLException {
            builder.up().withDetail("database", getProduct());
            String validationQuery = this.query;
            if (StringUtils.hasText(validationQuery)) {
                builder.withDetail("validationQuery", validationQuery);
                printPoolInfo("doDataSourceHealthCheck", "before");
                List<Object> results = JdbcUtil.query(this.getConnection(), validationQuery, queryTimeout, new SingleColumnRowMapper());
                printPoolInfo("doDataSourceHealthCheck", "before");
                Object result = DataAccessUtils.requiredSingleResult(results);
                builder.withDetail("result", result);
            } else {
                builder.withDetail("validationQuery", "isValid()");
                boolean valid = isConnectionValid();
                builder.status((valid) ? Status.UP : Status.DOWN);
            }
        }

        private Connection getConnection() throws SQLException {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                HikariPool hikariPool = (HikariPool) hikariDataSource.getHikariPoolMXBean();
                return hikariPool.getConnection(queryTimeout * 1000L);
            }
            return dataSource.getConnection();
        }

        private String getProduct() throws SQLException {
            printPoolInfo("getProduct", "before");
            try (Connection conn = this.getConnection()) {
                printPoolInfo("getProduct", "fetched");
                return conn.getMetaData().getDatabaseProductName();
            } finally {
                printPoolInfo("getProduct", "after");
            }
        }

        private Boolean isConnectionValid() throws SQLException {
            printPoolInfo("isConnectionValid", "before");
            try (Connection conn = this.getConnection()) {
                printPoolInfo("isConnectionValid", "fetched");
                return conn.isValid(queryTimeout);
            } finally {
                printPoolInfo("isConnectionValid", "after");
            }
        }

        private final class SingleColumnRowMapper implements RowMapper<Object> {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                ResultSetMetaData metaData = rs.getMetaData();
                int columns = metaData.getColumnCount();
                if (columns != 1) {
                    throw new IncorrectResultSetColumnCountException(1, columns);
                }
                return JdbcUtils.getResultSetValue(rs, 1);
            }
        }

        private void printPoolInfo(String methodName, String action) {
            if (!printPoolInfo)
                return;
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                HikariPool hikariPool = (HikariPool) hikariDataSource.getHikariPoolMXBean();
                logger.debug("[", hikariDataSource.getPoolName(), "][", methodName, "][", action, "]Total:", hikariPool.getTotalConnections(), ",Active:", hikariPool.getActiveConnections(), ",Idle:", hikariPool.getIdleConnections(), ",ThreadsAwaiting:", hikariPool.getThreadsAwaitingConnection());
            }
        }
    }
}
