package com.syscom.fep.batch.task.cmn;

import com.syscom.fep.batch.base.task.TaskBase;
import com.syscom.fep.frmcommon.ref.RefString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 每天執行固定時間執行,將線上資料庫FEPTXN及FEPTXNTCB歸檔至歷史資料庫
 *
 * @author ChenYang
 */
public class ArchivingTableFEPTXN extends TaskBase {
    private String detachpartitionSh = "";
    private String exportdataSh = "";
    private String importdataSh = "";

    public static void main(String[] args) {
        new ArchivingTableFEPTXN().executeMain(args);
    }

    /**
     * 顯示Usage
     */
    @Override
    public void displayUsage() {
        System.out.println("USAGE:");
        System.out.println(" ArchivingTableFEPTXN Options");
        System.out.println();
        System.out.println(" Options:");
        System.out.println(" /? Display this help message.");
        System.out.println(" /TxDate Optional.");
        System.out.println(" /TxDateEnd Optional. If provided, process all dates from TxDate to TxDateEnd.");
        System.out.println("            TxDate and TxDateEnd must be in the same month (YYYYMM must match).");
        System.out.println(" /BatchLogPath Optional");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println(" ArchivingTableFEPTXN /TxDate:20240625");
        System.out.println(" ArchivingTableFEPTXN /TxDate:20240625 /TxDateEnd:20240628");
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
        // TXDATE: 執行批次的日期, 格式為yyyyMMdd, 非必要
        String today = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        String txDate = findArg(args, "TxDate", null);
        String txDateEnd = findArg(args, "TxDateEnd", null);
        
        //若TxDate及TxDate都未傳入, 則以系統日期-30為TxDate及TxDateEnd
        if(txDate == null && txDateEnd == null){
            txDate = getBefore30Days(today);
            txDateEnd = txDate;
        } else if(txDate != null && txDateEnd == null){
            //若有傳TxDate但未傳TxDateEnd, 則以TxDate = TxDate-30 ,TxDateEnd=TxDate
            txDate = getBefore30Days(txDate);
            txDateEnd = txDate;
        } else if(txDate == null && txDateEnd != null){
            this.log(Level.SEVERE, "TxDate is required when specified TxDateEnd.");
            return false;
        }   
        // 若TxDate及TxDateEnd都有傳入, 則繼續處理
        // 驗證日期格式（此時 txDate 和 txDateEnd 必定不為 null）
        if (txDate == null || txDateEnd == null ||
            txDate.length() != 8 || txDateEnd.length() != 8) {
            this.log(Level.SEVERE, "TxDate or TxDateEnd format error. Must be yyyyMMdd format.");
            return false;
        }

        if (txDate.compareTo(txDateEnd) > 0) {
            this.log(Level.SEVERE, "TxDateEnd must be greater than or equal to TxDate. TxDate:", txDate, " TxDateEnd:", txDateEnd);
            return false;
        }
        
        //ArchivingType: 必要參數, 代表批次要執行動作, 可為detach及import, 當參數為detach時, 執行DETACH partition動作;當參數為import時, 執行export及import及drop table等動作
        String archivingType = findArg(args, "ArchivingType", "import");

        detachpartitionSh = findArg(args, "DetachPartitionSh", "feptxn_detachpartition.sh");
        exportdataSh = findArg(args, "ExportDataSh", "feptxn_exportdata.sh");
        importdataSh = findArg(args, "ImportDataSh", "feptxn_importdata.sh");
        
        // 確認TxDate與TxDateEnd是否在同一月份
        String txDateYM = txDate.substring(0, 6);
        String txDateEndYM = txDateEnd.substring(0, 6);
        if (!txDateYM.equals(txDateEndYM)) {
            this.log(Level.SEVERE, "TxDate and TxDateEnd must be in the same month. TxDate:", txDate, " TxDateEnd:", txDateEnd);
            return false;
        }
        this.log("Begin Processing from TxDate:", txDate, " to TxDateEnd:", txDateEnd);

        boolean isSuccess = false;

        // 逐日處理從TxDate到TxDateEnd
        String currentDate = txDate;
        while (currentDate.compareTo(txDateEnd) <= 0) {
            this.log("Begin processing for date:", currentDate);
            if ("detach".equals(archivingType)) {
                //處理Archiving 線上資料庫FEPTXN至歷史資料庫
                isSuccess = detachPartition(currentDate, "FEPTXN");
                if (!isSuccess) {
                    this.log(Level.SEVERE, "detachPartition failed for date:", currentDate, " table: FEPTXN");
                    return false;
                }
                //處理Archiving 線上資料庫FEPTXNTCB至歷史資料庫
                isSuccess = detachPartition(currentDate, "FEPTXNTCB");
                if (!isSuccess) {
                    this.log(Level.SEVERE, "detachPartition failed for date:", currentDate, " table: FEPTXNTCB");
                    return false;
                }
            } else {
                //處理Archiving 線上資料庫FEPTXN至歷史資料庫
                isSuccess = archiving(currentDate, "FEPTXN");
                if (!isSuccess) {
                    this.log(Level.SEVERE, "archiving failed for date:", currentDate, " table: FEPTXN");
                    return false;
                }
                //處理Archiving 線上資料庫FEPTXNTCB至歷史資料庫
                isSuccess = archiving(currentDate, "FEPTXNTCB");
                if (!isSuccess) {
                    this.log(Level.SEVERE, "archiving failed for date:", currentDate, " table: FEPTXNTCB");
                    return false;
                }
            }
            this.log("Completed processing for date:", currentDate);
            // 若尚未到達結束日期，則將日期加1天繼續處理
            //if (currentDate.compareTo(txDateEnd) < 0) {
            currentDate = getNextDay(currentDate);
            this.log("Completed processing. Next date:", currentDate);
            //} 
        }
        return isSuccess;
    }

    private boolean archiving(String txDate, String tableName) throws Exception {
        Connection connFEPDB = null;
        Statement stmtFEPDB = null;
        ResultSet rs = null;
        try {
            // Prepare DB Connection
            connFEPDB = this.getConnection(DBName.FEPDB_BATCH);
            stmtFEPDB = connFEPDB.createStatement();


            String partitionName = tableName + "PAR_" + txDate;

            // 2.	查詢要歸檔的FEPTXN partion是否存在,以TXDATE(或啟動日期參數) – 30天日存放在變數partition_name, 比如TXDATE為20231231, partition_name為FEPTXNPAR_20231201
            String selectSql = "SELECT * FROM SYSCAT.DATAPARTITIONS WHERE TABNAME='" + tableName + "' AND DATAPARTITIONNAME = '" + partitionName + "'";
            this.log("Check Partition_Name exist, execut SQL:" + selectSql);
            rs = stmtFEPDB.executeQuery(selectSql);

            // 處理結果
            boolean selectSqlRes = false;
            boolean flgSuccess = false;

            //3.	執行以下SQL,查詢partition_name的partition是否存在
            if (rs.next()) {
                String dataPartitionName = rs.getString("DATAPARTITIONNAME");
                if (dataPartitionName.equals(partitionName)) {
                    selectSqlRes = true;
                    this.log(partitionName + " partition is exist!");
                }
            }

            if (selectSqlRes) {
                // 4.	若partition存在，則執行feptxn_detachpartition.sh , 將該 partition DETACH至暫存Table
                //String alterSql = "ALTER TABLE FEP." + tableName + " DETACH PARTITION " + partitionName + " INTO FEP." + partitionName;
                //stmtFEPDB.execute(alterSql);
                flgSuccess = executeShell(Arrays.asList(detachpartitionSh, tableName, partitionName));
                if (flgSuccess) {
                    this.log(" feptxn_detachpartition.sh Success");
                } else {
                    this.log(Level.SEVERE, " feptxn_detachpartition.sh failed");
                    return false; // 若detach失敗, 則不繼續執行後續動作
                }
            }
            // else {

            // 2025-02-27 Richard add 若fep/logs/dbdump目錄不存在程式主動先建立
            File parent = new File("/fep/logs/dbdump");
            if (!parent.exists()) {
                if (parent.mkdirs())
                    this.log("mkdirs ", parent.getAbsolutePath(), " Success");
                else
                    this.log(Level.SEVERE, "mkdirs ", parent.getAbsolutePath(), " failed");
                    return false; // 若建立失敗, 則不繼續執行後續動作
            }

            //5.	若該日的partition已不存在, 則繼續做下一步驟
            File file = new File("/fep/logs/dbdump/" + partitionName + ".del");
            //6.	檢查 /fep/logs/dbdump/@partition_name.del是否存在
            if (!file.exists()) {
                //若不存在，則執行feptxn_exportdata.sh將detach的暫存資料表資料匯出，feptxn_exportdata.sh內容
                //String exportSql = "EXPORT TO /fep/logs/dbdump/" + partitionName + ".del OF DEL SELECT * FROM FEP." + partitionName;
                //stmtFEPDB.execute(exportSql);
                flgSuccess = executeShell(Arrays.asList(exportdataSh, tableName, partitionName));
                if (flgSuccess) {
                    this.log(" feptxn_exportdata.sh Success");
                } else {
                    this.log(Level.SEVERE, " feptxn_exportdata.sh failed");
                    return false; // 若匯出失敗, 則不繼續執行後續動作
                }
            }
            File fileMsg = new File("/fep/logs/dbdump/" + partitionName + ".msg");
            //若匯出成功會產生del檔
            if (file.exists()) {
                //執行Import前先清除之前殘留的MSG file
                if(fileMsg.exists()) {
                    fileMsg.delete();
                }
                //7.	若/fep/logs/dbdump/@partition_name.del的匯出檔已存在, 則繼續做下一步驟
                //8.	執行feptxn_importdata.sh將匯出檔案匯入至歷史資料庫的FEPTXN表中
                //String importSql = "IMPORT FROM /fep/logs/dbdump/" + partitionName + ".del OF DEL modified by delprioritychar CommitCount 5000 messages /fep/logs/dbdump/" + partitionName + ".message INSERT INTO FEP." + partitionName;
                //stmtFEPHIS.execute(importSql);
                flgSuccess = executeShell(Arrays.asList(importdataSh, tableName, partitionName));
                if (flgSuccess) {
                    this.log(" feptxn_importdata.sh Success");
                } else {
                    this.log(Level.SEVERE, " feptxn_importdata.sh failed");
                    return false; // 若匯入失敗, 則不繼續執行後續動作
                }
            }
            // }

            //9.	檢查匯出檔@partition_name.del是否已產生及匯出訊息檔@partition_name.msg
            File fileDel = new File("/fep/logs/dbdump/" + partitionName + ".del");            

            //10.	若匯出檔己存在則刪除
            if (fileDel.exists() && fileMsg.exists()) {
                //是否有出現以下內容
                //SQL3149N  "100" rows were processed from the input file.
                if (isSuccess(fileMsg)) {
                    //刪除
                    fileDel.delete();
                    this.log("/fep/logs/dbdump/" + partitionName + ".del delete Success");
                    fileMsg.delete();
                    this.log("/fep/logs/dbdump/" + partitionName + ".msg delete Success");
                }
            }

            //11.	若線上資料庫的FEPTXNTCB暫存表己存在,則將暫存表刪除
            String dropSql = "DROP TABLE FEP." + partitionName + " IF EXISTS ";
            stmtFEPDB.execute(dropSql);
            this.log("Drop table FEP." + partitionName + " Success");

        } finally {
            this.closeDbConnection(connFEPDB, stmtFEPDB, rs);
        }
        return true;
    }

    private String getBefore30Days(String txDate) throws ParseException {
        String before30Days = "";
        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            date = format.parse(txDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, -30);
            before30Days = format.format(cal.getTime());
        } catch (ParseException e) {
            this.log(Level.SEVERE, e, " parse date error txDate:", txDate);
            throw e;
        }
        return before30Days;
    }

    private String getNextDay(String txDate) throws ParseException {
        String nextDay = "";
        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            date = format.parse(txDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 1);
            nextDay = format.format(cal.getTime());
        } catch (ParseException e) {
            this.log(Level.SEVERE, e, " parse date error txDate:", txDate);
            throw e;
        }
        return nextDay;
    }

    private boolean isSuccess(File fileMsg) {
        StringBuilder sql3149n = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(fileMsg.toPath()), StandardCharsets.UTF_8));) {
            String line = null;
            while ((line = br.readLine()) != null) {
                // 找到SQL3149N這一行, 則連續讀兩行
                if (line.contains("SQL3149N")) {
                    sql3149n.append(line);
                    line = br.readLine();
                    if (line != null)
                        sql3149n.append(" ").append(line);
                }
            }
        } catch (IOException e) {
            this.log(Level.SEVERE, e, " read file ", fileMsg, " error .");
        }
        if (sql3149n.length() > 0) {
            // 另外幫我把這行寫到LOG裏 SQL 3149N這行
            this.log(Level.INFO, "found line SQL3149N in file:", fileMsg.getAbsolutePath(), ", line:", sql3149n.toString());
            // 解讀取出相關的數字
            // SOL3149N "100" rows were processed from the input file. "100" rows were successfully inserted into the table. "0" rows were rejected.
            Pattern pattern = Pattern.compile("\"\\d+\"");
            Matcher matcher = pattern.matcher(sql3149n.toString());
            String found = "";
            int processed = -1, successfully = -1;
            if (matcher.find()) {
                found = matcher.group();
                try {
                    processed = Integer.parseInt(found.substring(1, found.length() - 1));
                } catch (NumberFormatException e) {
                    this.log(Level.WARNING, "parse processed error:", found);
                }
            }
            if (matcher.find()) {
                found = matcher.group();
                try {
                    successfully = Integer.parseInt(found.substring(1, found.length() - 1));
                } catch (NumberFormatException e) {
                    this.log(Level.WARNING, "parse successfully error:", found);
                }
            }
            return successfully != -1 && processed == successfully;
        } else {
            this.log(Level.WARNING, "not found line SQL3149N in file:", fileMsg.getAbsolutePath());
        }
        return false;
    }

    private boolean detachPartition(String txDate, String tableName) throws Exception {
        Connection connFEPDB = null;
        Statement stmtFEPDB = null;
        ResultSet rs = null;
        try {
            // Prepare DB Connection
            connFEPDB = this.getConnection(DBName.FEPDB_BATCH);
            stmtFEPDB = connFEPDB.createStatement();


            String partitionName = tableName + "PAR_" + txDate;

            // 1.	查詢要歸檔的FEPTXN partion是否存在,以TXDATE(或啟動日期參數) – 30天日存放在變數partition_name, 比如TXDATE為20231231, partition_name為FEPTXNPAR_20231201
            String selectSql = "SELECT * FROM SYSCAT.DATAPARTITIONS WHERE TABNAME='" + tableName + "' AND DATAPARTITIONNAME = '" + partitionName + "'";
            this.log("check partition exist, execute SQL:" + selectSql);
            rs = stmtFEPDB.executeQuery(selectSql);

            // 處理結果
            boolean selectSqlRes = false;
            boolean flgSuccess = false;

            //2.	連至線上資料庫(FEPDB_BATCH)執行以下SQL,查詢partition_name的partition是否存在
            if (rs.next()) {
                String dataPartitionName = rs.getString("DATAPARTITIONNAME");
                if (dataPartitionName.equals(partitionName)) {
                    selectSqlRes = true;
                    this.log(partitionName + " partition is exist!");
                }
            }

            if (selectSqlRes) {
                // 3.	若partition存在，則執行feptxn_detachpartition.sh , 將該 partition DETACH至暫存Table
                //String alterSql = "ALTER TABLE FEP." + tableName + " DETACH PARTITION " + partitionName + " INTO FEP." + partitionName;
                //stmtFEPDB.execute(alterSql);
                flgSuccess = executeShell(Arrays.asList(detachpartitionSh, tableName, partitionName));
                if (flgSuccess) {
                    this.log(" feptxn_detachpartition.sh Success");
                } else {
                    this.log(Level.SEVERE, " feptxn_detachpartition.sh failed");
                    return false; // 若detach失敗, 則不繼續執行後續動作
                }
            } else {
                //4.	若該日的partition已不存在, 則記LOG後返回
                this.log(" partition is not exist!");
            }
        } finally {
            this.closeDbConnection(connFEPDB, stmtFEPDB, rs);
        }
        return true;
    }
}
