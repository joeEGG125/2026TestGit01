package com.syscom.fep.batch.task.cmn;

import com.syscom.fep.batch.base.task.TaskBase;
import com.syscom.fep.frmcommon.ref.RefString;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

/**
 * 顯示7個資料DESLOG count數
 *
 * @author Chen LeYun
 */
public class SelectTableDESLOG extends TaskBase {

//    private BatchJobLibrary job = null;
//    private String batchLogPath = StringUtils.EMPTY;

    private static List<String> tableNames = Arrays.asList(
            "DESLOG1", "DESLOG2", "DESLOG3", "DESLOG4",
            "DESLOG5", "DESLOG6", "DESLOG7"
    );

    public static void main(String[] args) {
        new SelectTableDESLOG().executeMain(args);
    }

    /**
     * 顯示Usage
     */
    @Override
    public void displayUsage() {
        System.out.println("USAGE:");
        System.out.println(" selectTableDESLOG Options");
        System.out.println();
        System.out.println(" Options:");
        System.out.println(" /? Display this help message.");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println(" selectTableDESLOG");
    }

    /**
     * 執行
     *
     * @param args
     * @param resultMessage
     * @return
     */
    @Override
    protected boolean process(String[] args, RefString resultMessage) throws Exception {
//        batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
//        if (StringUtils.isBlank(batchLogPath))
//            batchLogPath = "/fep/logs";
//        // 2. 初始化BatchJob物件,傳入工作執行參數
//        job = new BatchJobLibrary(this, args, batchLogPath);

        try {
            // 執行查詢
            runQueryCount(tableNames);
            resultMessage.set("批次執行成功");
            return true;

        } catch (Exception e) {
            this.log("批次執行失敗！", e);
            resultMessage.set(e.getMessage());
            return false;
        }
    }

    public void runQueryCount(List<String> tableNames) throws Exception {
        Connection connDESLOGDB = null;
        Statement stmtDESLOGDB = null;

        try {
            connDESLOGDB = this.getConnection(DBName.DESLOGDB);
            if (connDESLOGDB == null) {
                throw new SQLException("無法獲取 DESLOGDB 資料庫連線！");
            }
            stmtDESLOGDB = connDESLOGDB.createStatement();

            for (String tableName : tableNames) {
                String selectSql = "SELECT COUNT(*) AS total_count FROM FEP." + tableName;

                try (ResultSet rs = stmtDESLOGDB.executeQuery(selectSql)){

                    // 顯示結果
                    if (rs.next()) {
                        long count = rs.getLong("total_count");
                        System.out.printf("資料表 [%s] 筆數: %d%n", tableName, count);
                        this.log("資料表" + tableName + " 筆數:" + count);
                    }
                } catch (SQLException e) {
                    // 資料表查詢失敗或不存在，印出錯誤訊息並繼續
                    System.err.printf("查詢資料表 [%s] 時發生錯誤: %s%n", tableName, e.getMessage());
                    this.log("查詢資料表" +tableName+ "時發生錯誤:" + e.getMessage());
                }
            }
        } catch (SQLException e) {
            this.log("資料庫連線或查詢時發生錯誤！", e);
            throw e;
        }
        finally {
            this.closeDbConnection(connDESLOGDB, stmtDESLOGDB, null);
        }
    }
}