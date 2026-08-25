package com.syscom.fep.frmcommon.jdbc;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.scheduler.AbstractScheduledTask;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceConnectionDetector extends AbstractScheduledTask {
    protected LogHelper logger = new LogHelper();
    private DataSource dataSource;

    public DataSourceConnectionDetector(String dsName, DataSource dataSource) {
        super(dsName);
        this.dataSource = dataSource;
    }

    protected abstract void connectionEstablished();

    protected void connectionBreakage(SQLException e) {}

    public static void connectionDetect(String beanName) throws SQLException {
        DataSource dataSource = SpringBeanFactoryUtil.getBean(beanName);
        if (dataSource != null) {
            connectionDetect(dataSource);
        }
    }

    public static void connectionDetect(String beanName, String username, String password) throws SQLException {
        DataSource dataSource = SpringBeanFactoryUtil.getBean(beanName);
        if (dataSource != null) {
            connectionDetect(dataSource, username, password);
        }
    }

    public static DataSource connectionDetect(DataSource dataSource) throws SQLException {
        if (dataSource != null) {
            Connection conn = null;
            try {
                conn = dataSource.getConnection();
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                    }
                }
            }
        }
        return dataSource;
    }

    public static void connectionDetect(DataSource dataSource, String username, String password) throws SQLException {
        Connection conn = null;
        try {
            conn = dataSource.getConnection(username, password);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    @Override
    public void execute() {
        try {
            connectionDetect(this.dataSource);
            logger.info("[", this.taskName, "]connection is established");
            this.connectionEstablished();
        } catch (SQLException e) {
            logger.warn("[", this.taskName, "]connection is still broken, ", e.getMessage());
            connectionBreakage(e);
        }
    }
}
