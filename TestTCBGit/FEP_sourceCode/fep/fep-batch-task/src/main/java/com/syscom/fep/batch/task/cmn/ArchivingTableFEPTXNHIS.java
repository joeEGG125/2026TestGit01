package com.syscom.fep.batch.task.cmn;

import com.syscom.fep.batch.base.task.TaskBase;
import com.syscom.fep.frmcommon.ref.RefString;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
/**
 * 每月固定時間執行,將歷史資料庫FEPTXN及FEPTXNTCB歸檔至外部存放媒體及清檔
 *
 * @author xingyun
 */
public class ArchivingTableFEPTXNHIS extends TaskBase {

    private String detachpartitionSh = "";
    private String exportdataSh = "";

    public static void main(String[] args) {
        new ArchivingTableFEPTXNHIS().executeMain(args);
    }

    /**
     * 顯示Usage
     */
    @Override
    public void displayUsage() {
        System.out.println("USAGE:");
        System.out.println(" ArchivingTableFEPTXNHIS Options");
        System.out.println();
        System.out.println(" Options:");
        System.out.println(" /? Display this help message.");
        System.out.println(" /TxDate Optional.");
        System.out.println(" /BatchLogPath Optional");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println(" ArchivingTableFEPTXNHIS /TxDate:20240625");
    }

    /**
     * 執行
     *
     * @param args
     * @param resultMessage
     */
    @Override
    protected boolean process(String[] args, RefString resultMessage) throws Exception {
        // TXDATE: 執行批次的日期, 格式為yyyyMMdd, 非必要, 若未傳入, 則以系統日期為TXDATE
        String today = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        String txDate = findArg(args, "TxDate", today);

        // 2025-02-27 Richard add 若fep/logs/dbarchive目錄不存在程式主動先建立
        File parent = new File("/fep/dbarchive/");
        if (!parent.exists()) {
            if (parent.mkdirs())
                this.log("mkdirs ", parent.getAbsolutePath(), " Success");
            else
                this.log(Level.SEVERE, "mkdirs ", parent.getAbsolutePath(), " failed");
        }

        // 2. 判斷匯出目錄下是否有存在之前匯出檔(檔名月份小於ArchivingMonth), 若有將之刪除
        // rm /fep/dbarchive/FEPTXN_202311.del
        // rm /fep/dbarchive/FEPTXNTCB_202311.del
        deleteFilesBefore("/fep/dbarchive/", getArchivingMonth(txDate));
        // File fileFEPTXN = new File("/fep/dbarchive/FEPTXN_" + getArchivingMonth(txDate) + ".del");
        // File fileFEPTXNTCB = new File("/fep/dbarchive/FEPTXNTCB_" + getArchivingMonth(txDate) + ".del");

        // if (fileFEPTXN.exists() && fileFEPTXNTCB.exists()) {
        //     fileFEPTXN.delete();
        //     fileFEPTXNTCB.delete();
        // }

        detachpartitionSh = findArg(args, "DetachPartitionSh", "feptxnhis_detachpartition.sh");
        exportdataSh = findArg(args, "ExportDataSh", "feptxnhis_exportdata.sh");

        // 3. 開始執行Archiving FEPTXN流程
        // 呼叫ArchivingFEPTXN()
        archiving(txDate, "FEPTXN", resultMessage);
        // 4. 開始執行Archiving FEPTXNTCB流程
        // 呼叫ArchivingFEPTXNTCB()
        archiving(txDate, "FEPTXNTCB", resultMessage);
        return true;
    }

    private void archiving(String txDate, String tableName, RefString resultMessage) throws Exception {
        Connection connFEPDB = null;
        Statement stmtFEPDB = null;
        ResultSet rs = null;
        try {
            // Prepare DB Connection
            connFEPDB = this.getConnection(DBName.FEPHIS_BATCH);
            stmtFEPDB = connFEPDB.createStatement();

            String partitionName = tableName + "PAR_" + getArchivingMonth(txDate);

            // 2. 查詢要歸檔的FEPTXN partion是否存在,以TXDATE(或啟動日期參數) – 30天日存放在變數partition_name,
            // 比如TXDATE為20231231, partition_name為FEPTXNPAR_20231201
            String selectSql = "SELECT * FROM SYSCAT.DATAPARTITIONS  WHERE TABNAME='" + tableName +
                    "' AND DATAPARTITIONNAME = '" + partitionName + "'";
            this.log("check Partition_Name exist SQL:" + selectSql);
            rs = stmtFEPDB.executeQuery(selectSql);

            // 理結果
            boolean selectSqlRes = false;
            boolean flgSuccess = false;

            // 3. 執行以下SQL,查詢partition_name的partition是否存在
            if (rs.next()) {
                String dataPartitionName = rs.getString("DATAPARTITIONNAME");
                if (dataPartitionName.equals(partitionName)) {
                    selectSqlRes = true;
                    this.log(partitionName + " partition exist!");
                }
            } else {
                this.log(partitionName + " partition not exist!");
                resultMessage.set("沒有資料需要轉移");
            }

            if (selectSqlRes) {
                // 4. 若partition存在，則執行feptxnhis_detachpartition.sh , 將該 partition DETACH至暫存Table
                // connect to FEPHISDB;
                // ALTER TABLE FEP.@tableName DETACH PARTITION @partition_name INTO FEP.
                // @partition_name;
                flgSuccess = executeShell(Arrays.asList(detachpartitionSh, tableName, partitionName));
                if (flgSuccess) {
                    this.log(" feptxnhis_detachpartition.sh Success");
                } else {
                    this.log(Level.SEVERE, " feptxnhis_detachpartition.sh failed");
                }
            } else {
                resultMessage.set("沒有資料需要轉移");
            }

            boolean tmpExists = false;
            String sql2 = "SELECT * FROM SysCat.Tables WHERE TabName ='" + partitionName + "'";
            ResultSet rs2 = stmtFEPDB.executeQuery(sql2);
            if (rs2.next()) {
                tmpExists = true;
                this.log(partitionName + " table exist!");
            } else {
                this.log(partitionName + " table not exist!");
            }

            // else {

            // 2025-02-27 Richard add 若fep/logs/dbarchive目錄不存在程式主動先建立
            File parent = new File("/fep/dbarchive/");
            if (!parent.exists()) {
                if (parent.mkdirs())
                    this.log("mkdirs ", parent.getAbsolutePath(), " Success");
                else
                    this.log(Level.SEVERE, "mkdirs ", parent.getAbsolutePath(), " failed");
            }

            // 5. 若該日的partition已不存在, 則繼續做下一步驟
            File file = new File("/fep/dbarchive/" + partitionName + ".del");
            // 6. 檢查 /fep/dbarchive/@partition_name.del是否存在
            if (!file.exists() && tmpExists) {
                // 若不存在，
                // 則執行feptxnhis_exportdata.sh將detach的暫存資料表資料匯出，feptxnhis_exportdata.sh內容如下:
                flgSuccess = executeShell(Arrays.asList(exportdataSh, tableName, partitionName));
                if (flgSuccess) {
                    this.log(" feptxnhis_exportdata.sh Success");
                } else {
                    this.log(Level.SEVERE, " feptxnhis_exportdata.sh failed");
                }
            } else {
                resultMessage.set("沒有資料需要轉移");
            }

            if (file.exists() && tmpExists) {
                this.log(file.getAbsolutePath() + " exist ");
                // }
                // 7. 若/fep/dbarchive/@partition_name.del的匯出檔已存在, 則繼續做下一步驟
                // 8. /fep/dbarchive目錄將會由備份軟體在當月定時將備份目錄下的匯出檔備份到磁帶或其他永久儲存媒體
                // 9. 若歷史資料庫已匯出partition的FEPTXN暫存表己存在,則將暫存表刪除
                String dropSql = "DROP TABLE FEP." + partitionName + " IF EXISTS ";
                stmtFEPDB.execute(dropSql);

                this.log(" Drop table FEP." + partitionName + " OK");
            } else {
                resultMessage.set("沒有資料需要轉移");
            }

        } finally {
            this.closeDbConnection(connFEPDB, stmtFEPDB, rs);
        }
    }

    private String getArchivingMonth(String txDate) {
        String before180Years = "";
        try {
            // (3) 以TXDATE(或啟動日期參數) – 180個月的月份存入ArchivingMonth變數, 比如TXDATE為20381231,
            // ArchivingMonth = 202312
            before180Years = Integer.parseInt(txDate.substring(0, 4)) - 15 + txDate.substring(4, 6);
        } catch (NumberFormatException e) {
            this.log(Level.SEVERE, e, " parse date error txDate:", txDate);
            throw e;
        }
        return before180Years;
    }

    private void deleteFilesBefore(String directoryPath, String dateThreshold) throws IOException {
        //List<String> deletedFiles = new ArrayList<>();
        
        // 驗證日期格式
        // if (!isValidDate(dateThreshold)) {
        //     throw new IllegalArgumentException("日期格式錯誤，應為 YYYYMMDD 格式");
        // }
        this.log(Level.SEVERE, "準備刪除" + directoryPath + "下小於" + dateThreshold + "的del檔案");    

        File directory = new File(directoryPath);
        
        // 檢查目錄是否存在
        if (!directory.exists()) {
            throw new IOException("目錄不存在: " + directoryPath);
        }
        
        if (!directory.isDirectory()) {
            throw new IOException("指定路徑不是目錄: " + directoryPath);
        }
        
        // 定義檔案名稱的正則表達式模式 (FEP_YYYYMM.del)
        Pattern feptxn_pattern = Pattern.compile("FEPTXNPAR_(\\d{6})\\.del");
        Pattern feptxntcb_pattern = Pattern.compile("FEPTXNTCBPAR_(\\d{6})\\.del");
        
        // 列出目錄中的所有檔案
        File[] files = directory.listFiles();
        
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    Matcher matcher_feptxn = feptxn_pattern.matcher(fileName);
                    Matcher matcher_feptxn_tcb = feptxntcb_pattern.matcher(fileName);
                    // 如果檔名符合模式
                    String fileDate;
                    if (matcher_feptxn.matches()) {
                        fileDate = matcher_feptxn.group(1);
                    } else if (matcher_feptxn_tcb.matches()) {
                        fileDate = matcher_feptxn_tcb.group(1);                       
                    } else {
                        continue; // 檔名不符合模式，跳過
                    }
                    // 比較日期（字串比較即可，因為格式為 YYYYMM）
                    if (fileDate.compareTo(dateThreshold) < 0) {
                        try {
                            Path filePath = file.toPath();
                            Files.delete(filePath);
                            //deletedFiles.add(fileName);
                            this.log(Level.SEVERE, "已刪除檔案: " + fileName);                            
                        } catch (IOException e) {
                            this.log(Level.SEVERE, e, "刪除檔案失敗:", fileName);                            
                        }
                    }
                }
            }
        }        
        
    }
}
