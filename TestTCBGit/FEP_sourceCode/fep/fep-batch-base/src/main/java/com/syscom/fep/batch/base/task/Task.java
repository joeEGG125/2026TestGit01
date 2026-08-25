package com.syscom.fep.batch.base.task;

import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.frmcommon.cryptography.Jasypt;
import com.syscom.fep.frmcommon.esapi.ESAPIUtil;
import com.syscom.fep.frmcommon.util.CommandLineUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public interface Task extends TaskConstant {
    /**
     * Batch主流程, 從主流程呼叫各子流程, 若子流程發生ex則throw至主流程並結束AbortTask
     * 主流程每一個步驟依據_batchResult判斷是否往下執行
     * <p>
     * 2023-01-30 Richard modified 加入返回值返回code用於fep-batch-cmdline
     *
     * @param args
     * @return
     */
    BatchReturnCode execute(String[] args);

    /**
     * 顯示Usage
     */
    default void displayUsage() {}

    /**
     * 檢核參數是否符合規範, 若不符合則拋出異常
     *
     * @param commandArgs
     * @throws Exception
     */
    default void checkCommandArgs(String commandArgs) throws Exception {}

    /**
     * 獲取批次程式執行時所在的資料夾
     *
     * @return
     */
    default File getCurrentDir() {
        return ESAPIUtil.toFile(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
    }

    /**
     * 尋找傳入的變數
     *
     * @param args
     * @param found
     * @param defaultValue
     * @return
     */
    default String findArg(String[] args, String found, String defaultValue) {
        if (args == null) return defaultValue;
        try {
            for (String arg : args) {
                if (arg.startsWith("/" + found)) {
                    int index = arg.indexOf(":");
                    if (index != -1) {
                        String value = arg.substring(index + 1).trim();
                        if (value.isEmpty())
                            return defaultValue;
                        return value;
                    }
                    break;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            log(Level.WARNING, e, "find arg \"", found, "\" failed");
        }
        return defaultValue;
    }

    /**
     * 進行jasypt解碼
     *
     * @param input
     * @return
     */
    default String decrypt(String input) {
        return Jasypt.decrypt(input);
    }

    /**
     * 根據DBName取得Connection
     *
     * @param dbName
     * @return
     * @throws IOException
     * @throws SQLException
     */
    default Connection getConnection(DBName dbName) throws SQLException {
        return getDataSource(dbName).getConnection();
    }

    /**
     * 根據DBName取得DataSource
     * 預設優先取SpringBean
     *
     * @param dbName
     * @return
     */
    default HikariDataSource getDataSource(DBName dbName) {
        return StringUtils.isBlank(dbName.getDataSourceName()) ? null : SpringBeanFactoryUtil.getBean(dbName.getDataSourceName());
    }


    /**
     * 執行SQL
     *
     * @param dbName
     * @param sql
     * @param handler
     * @throws Throwable
     */
    default void executeQuery(DBName dbName, String sql, ResultSetHandler handler) throws Throwable {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            this.log(Level.FINE, "嘗試取得資料庫連線, DBName:", dbName.name());
            conn = this.getConnection(dbName);
            this.log(Level.FINE, "取得資料庫連線成功, DBName:", dbName.name());
            this.log(Level.FINE, "嘗試執行SQL, DBName:", dbName.name(), ", SQL:", sql);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            this.log(Level.FINE, "執行SQL成功, DBName:", dbName.name(), ", SQL:", sql);
            if (handler != null)
                handler.handle(rs);
        } catch (Throwable t) {
            this.log(Level.SEVERE, t, "執行SQL出現異常, DBName:", dbName.name(), ", SQL:", sql);
            throw t;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                this.log(Level.WARNING, e, e.getMessage());
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                this.log(Level.WARNING, e, e.getMessage());
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                this.log(Level.WARNING, e, e.getMessage());
            }
        }
    }

    /**
     * 執行SQL並返回結果
     *
     * @param pstmt
     * @return
     * @throws SQLException
     */
    default int executeUpdate(PreparedStatement pstmt) throws SQLException {
        try {
            return ReflectUtil.envokeMethod(pstmt, "executeUpdate", new Class<?>[] {}, new Object[] {}, 0, true);
        } catch (Throwable t) {
            if (t.getCause() instanceof SQLException) {
                throw (SQLException) t.getCause();
            }
            throw new SQLException(t.getMessage(), t.getCause());
        }
    }

    /**
     * 關閉DB連線
     *
     * @param conn
     * @param stmt
     * @param rs
     */
    default void closeDbConnection(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            this.log(Level.WARNING, e, e.getMessage());
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            this.log(Level.WARNING, e, e.getMessage());
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            this.log(Level.WARNING, e, e.getMessage());
        }
    }

    /**
     * 執行Shell
     *
     * @param cmd
     * @return
     */
    default boolean executeShell(List<String> cmd) {
        return executeShell(cmd, Arrays.asList(DBName.FEPDB_BATCH, DBName.FEPHIS_BATCH));
    }

    /**
     * 執行Shell
     *
     * @param cmd
     * @return
     * @throws IOException
     */
    default boolean executeShell(List<String> cmd, List<DBName> dbNames) {
        if (cmd == null) {
            cmd = new ArrayList<>();
        }
        // 2024-10-08 Richard add 呼叫sh檔時, 傳入DB的帳密
        List<String> command = new ArrayList<>(cmd);
        List<String> commandForLogging = new ArrayList<>(cmd);
        if (CollectionUtils.isNotEmpty(dbNames)) {
            for (DBName dbName : dbNames) {
                command.add(this.getDataSource(dbName).getUsername());
                command.add(this.getDataSource(dbName).getPassword());
                commandForLogging.add("[" + dbName.getDataSourceNameProperties() + "-account]");
                commandForLogging.add("[" + dbName.getDataSourceNameProperties() + "-password]");
            }
        }
        this.log("begin execute shell command: " + String.join(" ", commandForLogging));
        // 2024-11-28 Richard modified for 【Stored Command Injection】
        // ProcessBuilder pb = new ProcessBuilder(command);
        // pb.redirectErrorStream(true);
        try {
            // Process process = pb.start();
            String[] commands = new String[command.size()];
            command.toArray(commands);
            Process process = CommandLineUtil.getProcess(commands);
            String line = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null) {
                this.log(line);
            }
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            this.log(Level.SEVERE, e, "執行Shell出現異常, command:" + String.join(" ", cmd));
        }
        this.log("shell command exec completed.");
        return false;
    }

    /**
     * 將參數合併成一行字串
     *
     * @param args
     * @return
     */
    default String append(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg).append(" ");
        }
        if (sb.length() > 0)
            sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }


    /**
     * 檢核輸入的參數是否為空白
     *
     * @param name
     * @param value
     * @return
     */
    default boolean isBlank(String name, String value) {
        return isBlank(name, value, Level.INFO);
    }

    /**
     * 檢核輸入的參數是否為空白
     *
     * @param name
     * @param value
     * @return
     */
    default boolean isBlank(String name, String value, Level level) {
        if (StringUtils.isBlank(value)) {
            this.log(level, "必須輸入", name);
            return true;
        }
        return false;
    }

    /**
     * 記錄日誌訊息
     *
     * @param messages
     * @return
     */
    default String log(Object... messages) {
        return log(Level.INFO, null, messages);
    }

    /**
     * 記錄日誌訊息
     *
     * @param level
     * @param messages
     * @return
     */
    default String log(Level level, Object... messages) {
        return log(level, null, messages);
    }

    /**
     * 記錄日誌訊息
     *
     * @param level
     * @param t
     * @param messages
     * @return
     */
    default String log(Level level, Throwable t, Object... messages) {
        return StringUtils.EMPTY;
    }

    /**
     * 處理ResultSet
     */
    interface ResultSetHandler {
        void handle(ResultSet rs) throws SQLException;
    }

    /**
     * 驗證日期格式
     *
     * @param txDate
     * @param pattern
     * @throws ParseException
     */
    default void validateDate(String txDate, String pattern) throws ParseException {
        if (StringUtils.isBlank(txDate) || txDate.length() != pattern.length())
            throw new ParseException("Invalid date format", 0);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(false);
        sdf.parse(txDate);
    }
}
