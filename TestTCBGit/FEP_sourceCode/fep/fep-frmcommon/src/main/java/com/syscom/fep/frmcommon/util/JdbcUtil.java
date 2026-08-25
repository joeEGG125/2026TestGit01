package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;

import java.sql.*;
import java.util.List;

public class JdbcUtil {
    private static final LogHelper logger = new LogHelper();

    /**
     * 執行SQL
     *
     * @param conn
     * @param sql
     * @param rowMapper
     * @param <T>
     * @return
     * @throws Throwable
     */
    public static <T> List<T> query(Connection conn, String sql, RowMapper<T> rowMapper) throws SQLException {
        return query(conn, sql, 0, rowMapper);
    }

    /**
     * 執行SQL
     *
     * @param conn
     * @param sql
     * @param queryTimeout
     * @param rowMapper
     * @param <T>
     * @return
     * @throws Throwable
     */
    public static <T> List<T> query(Connection conn, String sql, int queryTimeout, RowMapper<T> rowMapper) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            if (queryTimeout > 0)
                stmt.setQueryTimeout(queryTimeout);
            rs = stmt.executeQuery(sql);
            if (rowMapper != null) {
                RowMapperResultSetExtractor<T> rse = new RowMapperResultSetExtractor<>(rowMapper);
                return rse.extractData(rs);
            }
        } catch (SQLException t) {
            logger.error(t, "執行SQL出現異常, SQL:" + sql);
            throw t;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                logger.warn(e, e.getMessage());
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.warn(e, e.getMessage());
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.warn(e, e.getMessage());
            }
        }
        return null;
    }

    /**
     * 驗證連線
     *
     * @param driverClassName
     * @param url
     * @param user
     * @param sscode
     * @param loginTimeout
     * @throws Exception
     */
    public static void validateConnection(String driverClassName, String url, String user, String sscode, int loginTimeout) throws Exception {
        try {
            Class.forName(driverClassName);
            logger.debug(driverClassName, " register successful!!!");
        } catch (ClassNotFoundException e) {
            logger.error(e, "Error: unable to load driver class, ", driverClassName);
            throw e;
        }
        if (loginTimeout > 0)
            DriverManager.setLoginTimeout(loginTimeout);
        // 2025-05-22 Richard modified for [SSRF]
        // try (Connection conn = DriverManager.getConnection(url, user, sscode)) {
        try (Connection conn = ReflectUtil.envokeStaticMethod(DriverManager.class, "getConnection", new Class[] {String.class, String.class, String.class}, new Object[] {url, user, sscode}, null)) {
            if (conn == null) {
                throw new SQLException("unable to build connection!!!");
            }
            logger.debug("build connection successful!!!");
        } catch (SQLException e) {
            logger.error(e, "Error: unable to build connection!");
            throw e;
        }
    }
}
