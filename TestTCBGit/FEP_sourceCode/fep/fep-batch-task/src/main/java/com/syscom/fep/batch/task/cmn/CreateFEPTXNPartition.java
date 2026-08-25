package com.syscom.fep.batch.task.cmn;

import com.syscom.fep.batch.base.task.TaskBase;
import com.syscom.fep.frmcommon.ref.RefString;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.logging.Level;

/**
 * 每天執行或每月固定時間執行,建立新的FEPTXN partition
 */
public class CreateFEPTXNPartition extends TaskBase {
    public static void main(String[] args) {
        new CreateFEPTXNPartition().executeMain(args);
    }

    /**
     * 顯示Usage
     */
    @Override
    public void displayUsage() {
        System.out.println("USAGE:");
        System.out.println(" CreateFEPTXNPartition Options");
        System.out.println();
        System.out.println(" Options:");
        System.out.println(" /? Display this help message.");
        System.out.println(" /TxDate Optional.");
        System.out.println(" /Partition Required");
        System.out.println(" /BatchLogPath Optional");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println(" CreateFEPTXNPartition /TxDate:20240625 /Partition:d");
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
        // TXDATE: 執行批次的日期, 格式為yyyyMMdd, 非必要, 若未傳入, 則以系統日期為TXDATE
        String today = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        String txDate = findArg(args, "TxDate", today);

        // 建立partition的規則, d代表每天一個partition, m代表每月一個partition, 必須傳入
        // 2025/11/03 by Ashiang 改為建立從TXDate起算未來14天所有未建立的Partition,歷史資料庫的是未來3個月所有未建立的partition
        String partition = findArg(args, "Partition", "");
        if (isBlank("Partition", partition))
            return false;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(txDate, formatter);
        // 處理建立FEPDB FEPTXN Partition,每天一個partition 當partitionBy=d, 跑一個固定14次的迴圈建立未來14天未建立的partition
        if ("d".equals(partition)) {
            for (int i = 1; i <= 14; i++) {
                //date = date.plusDays(i);
                txDate = date.plusDays(i).format(formatter);
                this.createFEPDBPartition(txDate);
            }
        }else if ("m".equals(partition)) {
            // 處理建立FEPHISDB FEPTXN Partition,每月一個partition 當partitionBy=m, 跑一個固定3次的迴圈建立未來3個月未建立的partition
            for (int i = 1; i <= 3; i++) {
                //date = date.plusMonths(i);
                txDate = date.plusMonths(i).format(formatter);
                this.createFEPHISDBPartition(txDate);
            }
        } else {
            this.log(Level.SEVERE, "partition輸入錯誤 參考值 d/m");
        }
        return true;
    }

    /**
     * 處理建立FEPDB FEPTXN Partition,每天一個partition
     *
     * @param pdate
     * @throws Exception
     */
    private void createFEPDBPartition(String pdate) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            // Prepare DB Connection
            conn = this.getConnection(DBName.FEPDB_BATCH);
            stmt = conn.createStatement();

            // 處理建立FEPDB FEPTXN Partition,每天一個partition, 流程如下:
            // 1. 查詢要建立的FEPTXN partion是否存在,以pdate日作為Partition_Name (yyyyMMdd), 比如pdate為20240114, Partition_Name為FEPTXNPAR_20240114
//            Calendar cal = Calendar.getInstance();
//            cal.setTime(new SimpleDateFormat("yyyyMMdd").parse(pdate));
//            cal.add(Calendar.DATE, +14);
//            String selectTxDate = new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
            // 2. 執行以下SQL,查詢Partition_Name的partition是否存在
            String selectSql = "SELECT * FROM SYSCAT.DATAPARTITIONS WHERE TABNAME='FEPTXN' AND DATAPARTITIONNAME = 'FEPTXNPAR_" + pdate + "'";
            this.log("check partition  exists, running SQL:" + selectSql);
            rs = stmt.executeQuery(selectSql);
            // 處理結果
            boolean selectSqlRes = true;
            if (rs.next()) {
                String dataPartitionName = rs.getString("DATAPARTITIONNAME");
                if (dataPartitionName.equals("FEPTXNPAR_" + pdate)) {
                    selectSqlRes = false;
                    this.log("FEPTXNPAR_" + pdate + " partition exists!");
                }
            }

            Calendar logDate = parseDateValue(Integer.parseInt(pdate));
            int week = getDayOfWeek(logDate);

            if (selectSqlRes) {
                // 3. 若不存在, 則建立新的partition，存放在尾碼為partition name所在日期的星期N的tablespace中, 例如Partition_Name為20240114，該日期為星期天，存放的tablespace尾數為07, 執行以下指令建立partition
                String addSql1 = "ALTER TABLE FEPTXN ADD PARTITION FEPTXNPAR_" + pdate +
                        " STARTING '" + pdate + "' ENDING '" + pdate +
                        "' IN FEPDATA_0" + week + " INDEX IN FEPINDEX_0" + week;
                this.log("FEPTXNPAR_" + pdate + " not exist, create new partition sql:" + addSql1);
                stmt.executeUpdate(addSql1);
                this.log("add new partition FEPTXNPAR_" + pdate + " success!");
            }
            // 4. 若該日的partition已存在, 則繼續做下一步驟
            // 5. 查詢要建立的FEPTXNTCB partion是否存在,以pdate日作為Partition_Name (yyyyMMdd), 比如TXDATE為20240114, Partition_Name為FEPTXNTCBPAR_20240114

            // 6. 執行以下SQL,查詢Partition_Name的partition是否存在
            String selectTcbSql = "SELECT * FROM SYSCAT.DATAPARTITIONS WHERE TABNAME='FEPTXNTCB' AND DATAPARTITIONNAME = 'FEPTXNTCBPAR_" + pdate + "'";
            this.log("check partition  exists, running SQL:" + selectTcbSql);
            rs = stmt.executeQuery(selectTcbSql);
            // 處理結果
            boolean selectTcbSqlRes = true;
            while (rs.next()) {
                String dataPartitionName = rs.getString("DATAPARTITIONNAME");
                this.log("dataPartitionName:" + dataPartitionName);
                if (dataPartitionName.equals("FEPTXNTCBPAR_" + pdate)) {
                    selectTcbSqlRes = false;
                    this.log("FEPTXNTCBPAR_" + pdate + " partition exists!");
                }
            }

            if (selectTcbSqlRes) {
                // 7. 若不存在, 則建立新的partition，存放在尾碼為partition name所在日期的星期N的tablespace中, 例如Partition_Name為20240114，該日期為星期天，存放的tablespace尾數為07, 執行以下指令建立partition
                String addSql = "ALTER TABLE FEPTXNTCB ADD PARTITION FEPTXNTCBPAR_" + pdate +
                        " STARTING '" + pdate + "' ENDING '" + pdate +
                        "' IN FEPDATA_0" + week + " INDEX IN FEPINDEX_0" + week;
                this.log("FEPTXNTCBPAR_" + pdate + " not exist, create new partition sql:" + addSql);
                stmt.executeUpdate(addSql);
                this.log("add new partition FEPTXNTCBPAR_" + pdate + " success!");
            }
        } finally {
            this.closeDbConnection(conn, stmt, rs);
        }
    }

    /**
     * 處理建立FEPHISDB FEPTXN Partition,每月一個partition
     *
     * @param pdate
     * @throws Exception
     */
    private void createFEPHISDBPartition(String pdate) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            // Prepare DB Connection
            conn = this.getConnection(DBName.FEPHIS_BATCH);
            stmt = conn.createStatement();

            // 處理建立FEPHISDB FEPTXN Partition,每月一個partition, 流程如下:
            // 1. 讀取PKI設定檔的FEPHISDB連線參數，查詢要建立的FEPTXN partion是否存在,以pdate的月份作為Partition_Name (yyyyMM), 比如TXDATEpdate為20240301, 新的partition name為FEPTXNHISPAR_202403
            pdate = pdate.substring(0, 6);
//          String selectTxYearOrM = selectTxYearOrM(txYearOrM);
            String month = pdate.substring(4, 6);
            // 2. 執行以下SQL,查詢Partition_Name的partition是否存在
            String selectSql = "SELECT * FROM SYSCAT.DATAPARTITIONS WHERE TABNAME='FEPTXN' AND DATAPARTITIONNAME = 'FEPTXNPAR_" + pdate + "'";
            this.log("查詢Partition_Name的partition是否存在,執行以下SQL:" + selectSql);
            rs = stmt.executeQuery(selectSql);
            // 處理結果
            boolean selectSqlRes = true;
            while (rs.next()) {
                String dataPartitionName = rs.getString("DATAPARTITIONNAME");
                if (dataPartitionName.equals("FEPTXNPAR_" + pdate)) {
                    selectSqlRes = false;
                    this.log("FEPTXNPAR_" + pdate + " partition存在");
                }
            }

            if (selectSqlRes) {
                // 3. 若不存在, 則建立新的partition，存放在尾碼為partition name所在日期的星期N的tablespace中, 例如Partition_Name為20240114，該日期為星期天，存放的tablespace尾數為07, 執行以下指令建立partition
                String addSql = "ALTER TABLE FEPTXN ADD PARTITION FEPTXNPAR_" + pdate +
                        " STARTING '" + pdate + "01' ENDING '" + pdate +
                        "31' IN FEPDATA_" + month + " INDEX IN FEPINDEX_" + month;
                this.log("FEPTXNPAR_" + pdate + "不存在, 則建立新的partition,sql:" + addSql);
                stmt.executeUpdate(addSql);
                this.log("FEPTXNPAR_" + pdate + "新增成功");
            }
            // 4. 若該月的partition已存在, 則繼續做下一步驟
            // 5.查詢要建立的FEPTXNTCB partition是否存在,以pdate的月份作為Partition_Name (yyyyMM), 比如TXDATE為20240301, 新的partition name為FEPTXNTCBHISPAR_202403
            // 6. 執行以下SQL,查詢Partition_Name的partition是否存在
            String selectTcbSql = "SELECT * FROM SYSCAT.DATAPARTITIONS WHERE TABNAME='FEPTXNTCB' AND DATAPARTITIONNAME = 'FEPTXNTCBPAR_" + pdate + "'";
            rs = stmt.executeQuery(selectTcbSql);
            // 處理結果
            boolean selectTcbRes = true;
            while (rs.next()) {
                String dataPartitionName = rs.getString("DATAPARTITIONNAME");
                if (dataPartitionName.equals("FEPTXNTCBPAR_" + pdate)) {
                    selectTcbRes = false;
                    this.log("FEPTXNTCBPAR_" + pdate + " partition存在");
                }
            }

            if (selectTcbRes) {
                // 7. 若不存在, 則建立新的partition，存放在尾碼為partition name後2碼的tablespace中, 例如Partition_Name為202403，存放的tablespace尾數為03, 執行以下指令建立partition, STARTING最後2碼固定放01, ENDING後2碼固定放31
                String addSql1 = "ALTER TABLE FEPTXNTCB ADD PARTITION FEPTXNTCBPAR_" + pdate +
                        " STARTING '" + pdate + "01' ENDING '" + pdate +
                        "31' IN FEPDATA_" + month + " INDEX IN FEPINDEX_" + month;
                this.log("FEPTXNTCBPAR_" + pdate + "不存在, 則建立新的partition,sql:" + addSql1);
                stmt.executeUpdate(addSql1);
                this.log("FEPTXNTCBPAR_" + pdate + "新增成功");
            }
        } finally {
            this.closeDbConnection(conn, stmt, rs);
        }
    }

    private Calendar parseDateValue(int date) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, date / 10000);
        cal.set(Calendar.MONTH, (date % 10000) / 100 - 1);
        cal.set(Calendar.DAY_OF_MONTH, date % 100);
        return cal;
    }

    private int getDayOfWeek(Calendar cal) {
        if (cal == null) {
            return 0;
        }
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        // 週日是1，所以減掉1後為0，所以這裡判斷一下
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }
        return dayOfWeek;
    }

    private String selectTxYearOrM(String txYearOrM) {
        String month = txYearOrM.substring(4, 6);
        switch (month) {
            case "10":
            case "11":
            case "12":
                month = String.valueOf(Integer.parseInt(txYearOrM) + 91);
                break;
            default:
                month = String.valueOf(Integer.parseInt(txYearOrM) + 3);
                break;
        }
        return month;
    }
}
