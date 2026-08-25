package com.syscom.fep.batch.task.cmn;

import com.syscom.fep.batch.base.task.TaskBase;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import org.apache.commons.lang3.StringUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.codecs.DB2Codec;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 根據控制檔ArchivingTable定義,將線上資料庫FEPDB的Table歸檔至歷史資料庫
 *
 * @author xingyun
 */
public class ArchivingTable extends TaskBase {
    private String exporttableSh = "";
    private String copytableSh = "";
    private String importtableSh = "";
    private String CONTROLFIELD = "";
    private String ArchivingStart = "";
    private String TABLENAME = "";
    private boolean _canDelete = false;
    private boolean _backupOnly = false;
    private String deleteHISDB = "";

    public static void main(String[] args) {
        new ArchivingTable().executeMain(args);
    }

    /**
     * 顯示Usage
     */
    @Override
    public void displayUsage() {
        System.out.println("USAGE:");
        System.out.println(" ArchivingTable Options");
        System.out.println();
        System.out.println(" Options:");
        System.out.println(" /? Display this help message.");
        System.out.println(" /TxDate Optional.");
        System.out.println(" /BatchLogPath Optional");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println(" ArchivingTable /TxDate:20240822");
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
        //要讀取ArchivingTable資料表的PK
        TABLENAME = findArg(args, "TABLENAME", "");
        this.log("Begin run task");
        //匯出線上資料庫資料至匯出檔
        exporttableSh = findArg(args, "ExporttableSh", "fepdb_exporttable.sh");
        //匯入檔案資料至歷史資料庫(整個覆蓋)
        copytableSh = findArg(args, "CopytableSh", "fepdb_copytable.sh");
        //匯入檔案資料至至歷史資料庫(只新增)
        importtableSh = findArg(args, "ImporttableSh", "fepdb_importtable.sh");
        //
        deleteHISDB = findArg(args, "DeleteHISDB", "");
        _backupOnly = findArg(args, "BackupOnly", "N").equalsIgnoreCase("Y");
        return batchStart(txDate);
        //return true;
    }

    private boolean batchStart(String txDate) throws Exception {
        Connection connFEPDB = null;
        //Connection connFEPHISDB = null;
        PreparedStatement stmtFEPDB = null;
        ResultSet dr = null;
        //boolean result = false;

        try {
            // Prepare DB Connection
            if (deleteHISDB.equalsIgnoreCase("Y")) {
                //    connFEPHISDB = this.getConnection(DBName.FEPHIS_BATCH);
                connFEPDB = this.getConnection(DBName.FEPHIS_BATCH);
            } else {
                connFEPDB = this.getConnection(DBName.FEPDB_BATCH);
            }


            // 1.	根據TABLENAME參數, 讀取ArchivingTable資料表存放在dr變數中, 若讀不到則SendEMS回報批次執行失敗
            final String selectSql = "SELECT * FROM ARCHIVINGTABLES WHERE TABLENAME = ?";
            this.log(ProgramName, "讀取ArchivingTable資料表存放在dr變數中:", StringUtils.replace(selectSql, "?", "\'" + TABLENAME + "\'"));
            stmtFEPDB = connFEPDB.prepareStatement(selectSql);
            stmtFEPDB.setString(1, ESAPI.encoder().encodeForSQL(new DB2Codec(), TABLENAME));
            dr = stmtFEPDB.executeQuery();

            boolean selectSqlRes = false;

            //	執行以下SQL,查詢tableName是否存在
            if (dr.next()) {
                String tblName = dr.getString("TABLENAME");
                if (tblName.equals(TABLENAME)) {
                    selectSqlRes = true;
                    this.log(TABLENAME, " exist");
                } else {
                    this.log(Level.SEVERE, TABLENAME, " not exist");
                    return false;
                }
            }


            if (selectSqlRes) {

                //2.	將dr.CONTROLFIELD值存入CONTROLFIELD全域變數中
                CONTROLFIELD = dr.getString("CONTROLFIELD");

                //3.	若dr.ISDUPLICATE<>1, 則根據FREQUENCY及OPERKIND決定要Archiving的資料範圍存入ArchivingStart全域變數中
                if (!"1".equals(dr.getString("ISDUPLICATE"))) {
                    if ("-1".equals(dr.getString("INTERVAL"))) {
                        //全部範圍資料
                        ArchivingStart = "ALL";
                    } else {
                        Integer interVal = Integer.parseInt(dr.getString("INTERVAL"));
                        if ("Y".equals(dr.getString("FREQUENCY"))) { //年區間
                            if ("1".equals(dr.getString("OPERKIND"))) {  //日期型態
                                //ArchivingStart = DateTime.Parse(TXDATE).AddYear(–1 * dr.INTERVAL).ToString(“yyyy/MM/dd”)
                                String dateTime = this.convertToDatetime(txDate, interVal, "year");
                                ArchivingStart = dateTime.substring(0, 4) + "-" + dateTime.substring(4, 6) + "-" + dateTime.substring(6);
                            } else if ("2".equals(dr.getString("OPERKIND"))) { ////西元日期8碼
                                ArchivingStart = this.convertToDatetime(txDate, interVal, "year");
                            } else if ("3".equals(dr.getString("OPERKIND"))) {//民國日期7碼
                                ArchivingStart = this.convertToROCDate(txDate, interVal, "year");
                            }
                        } else if ("M".equals(dr.getString("FREQUENCY"))) {//月區間
                            if ("1".equals(dr.getString("OPERKIND"))) {  //日期型態
                                //ArchivingStart = DateTime.Parse(TXDATE).AddYear(–1 * dr.INTERVAL).ToString(“yyyy/MM/dd”)
                                String dateTime = this.convertToDatetime(txDate, interVal, "month");
                                ArchivingStart = dateTime.substring(0, 4) + "-" + dateTime.substring(4, 6) + "-" + dateTime.substring(6);
                            } else if ("2".equals(dr.getString("OPERKIND"))) { ////西元日期8碼
                                ArchivingStart = this.convertToDatetime(txDate, interVal, "month");
                            } else if ("3".equals(dr.getString("OPERKIND"))) {//民國日期7碼
                                ArchivingStart = this.convertToROCDate(txDate, interVal, "month");
                            }
                        } else if ("D".equals(dr.getString("FREQUENCY"))) {//日區間
                            if ("1".equals(dr.getString("OPERKIND"))) {  //日期型態
                                //ArchivingStart = DateTime.Parse(TXDATE).AddYear(–1 * dr.INTERVAL).ToString(“yyyy/MM/dd”)
                                String dateTime = this.convertToDatetime(txDate, interVal, "day");
                                ArchivingStart = dateTime.substring(0, 4) + "-" + dateTime.substring(4, 6) + "-" + dateTime.substring(6);
                            } else if ("2".equals(dr.getString("OPERKIND"))) { ////西元日期8碼
                                ArchivingStart = this.convertToDatetime(txDate, interVal, "day");
                            } else if ("3".equals(dr.getString("OPERKIND"))) {//民國日期7碼
                                ArchivingStart = this.convertToROCDate(txDate, interVal, "day");
                            }
                        }
                    }
                }
                // 2024/11/20 for 明祥
                _canDelete = true;
                boolean flgSuccess = true;
                //4.	根據ISDUPLICATE, ISARCHIVING, ISDELETE 決定Archiving的流程
                if ("1".equals(dr.getString("ISDUPLICATE"))) {
                    flgSuccess = duplicateTable();
                } else {
                    if ("1".equals(dr.getString("ISARCHIVING"))) {
                        flgSuccess = archivingTable();
                    }
                }
                if (flgSuccess && _canDelete && "1".equals(dr.getString("ISDELETE"))) {
                    // if(deleteHISDB.equalsIgnoreCase("Y") &&
                    // !"1".equals(dr.getString("ISDUPLICATE")) &&
                    // !"1".equals(dr.getString("ISARCHIVING"))) {
                    flgSuccess = deleteTable(connFEPDB);

                    // else{
                    //     flgSuccess = deleteTable(connFEPDB);
                    // }

                }
                return flgSuccess;
            } else {
                return false;
            }
        } catch (Exception ex) {
            this.log(Level.SEVERE, ex, "batch process error");
            return false;
        } finally {
            this.closeDbConnection(connFEPDB, stmtFEPDB, dr);
        }
    }

    /**
     * 複製整個Table資料至歷史資料庫, 流程如下:
     *
     * @throws Exception
     */
    private boolean duplicateTable() throws Exception {
        //1.	設一變數delname = {TABLENAME}
        String delname = TABLENAME;
        //2.	設一變數condition= “1=1”
        String condition = "1=1";
        boolean flgSuccess = false;
        _canDelete = false;
        try {
            // 2025-02-27 Richard add 若fep/logs/dbdump目錄不存在程式主動先建立
            File parent = new File("/fep/logs/dbdump");
            if (!parent.exists()) {
                if (parent.mkdirs())
                    this.log("mkdirs ", parent.getAbsolutePath(), " Success");
                else
                    this.log(Level.SEVERE, "mkdirs ", parent.getAbsolutePath(), " failed");
            }

            //3.	檢查 /fep/logs/dbdump/@delname.del是否存在，
            File file = new File("/fep/logs/dbdump/" + delname + ".del");

            if (!file.exists() || _backupOnly) {
                //若不存在，則執行fepdb_exporttable.sh將資料表資料匯出，fepdb_exporttable.sh內容如下
                //connect to FEPDB;
                //EXPORT TO /fep/logs/dbdump/@delname.del OF DEL SELECT * FROM FEP. @TABLENAME WHERE @condition;
                flgSuccess = executeShell(Arrays.asList(exporttableSh, delname, condition));
                if (flgSuccess) {
                    this.log(" fepdb_exporttable.sh Success");
                } else {
                    this.log(Level.SEVERE, " fepdb_exporttable.sh failed");
                    return false;
                }
            }
            //只做備份不Import
            if (_backupOnly)
                return true;

            File fileMsg = new File("/fep/logs/dbdump/" + delname + ".msg");
            if (file.exists()) {
                //執行Import前先清除之前殘留的MSG file
                if (fileMsg.exists()) {
                    fileMsg.delete();
                }
                //4.	若/fep/logs/dbdump/@delname.del的匯出檔已存在, 則繼續做下一步驟
                //5.	執行fepdb_copytable.sh將匯出檔案匯入至歷史資料庫中
                //connect to FEPHISDB;
                //IMPORT FROM /fep/logs/dbdump/@delname.del OF DEL modified by delprioritychar CommitCount 5000 messages
                // /fep/logs/dbdump/@delname.msg REPLACE INTO FEP.@TABLENAME;
                flgSuccess = executeShell(Arrays.asList(copytableSh, delname, TABLENAME));
                if (flgSuccess) {
                    this.log(" fepdb_copytable.sh Success");
                } else {
                    this.log(Level.SEVERE, " fepdb_copytable.sh failed");
                    return false;
                }
            }
            //6.	檢查匯出檔@delname.del是否已產生及匯出訊息檔@delname.msg
            File fileDel = new File("/fep/logs/dbdump/" + delname + ".del");

            if (fileDel.exists() && fileMsg.exists()) {
                //是否有出現以下內容
                //SQL3149N  "100" rows were processed from the input file.
                //7. 若有出現代表匯出資料已成功(100為本次匯入的筆數,請將之存入至變數impcount中)，
                // 然後記BatchLog “已匯入{impcount}筆資料至@TABLENAME”
                if (isSuccess(fileMsg)) {
                    //刪除
                    fileDel.delete();
                    fileMsg.delete();
                    _canDelete = true;
                }
            }
            return true;
        } catch (Exception ex) {
            this.log(Level.SEVERE, ex, "duplicateTable()執行錯誤");
            return false;
        }
    }

    /**
     * 依條件Archiving 指定日期前的資料至歷史資料庫, 流程如下:
     *
     * @throws Exception
     */
    private boolean archivingTable() throws Exception {
        //1.	設一變數delname = {TABLENAME}
        //String delname = TABLENAME + "_" + ArchivingStart;
        String delname = TABLENAME;
        _canDelete = false;
        //2.	設一變數condition= “1=1”
        String condition = "";
        if ("ALL".equals(ArchivingStart)) {
            condition = "1=1";
        } else {
            condition = CONTROLFIELD + "<'" + ArchivingStart + "'";
        }
        boolean flgSuccess = false;
        try {
            // 2025-02-27 Richard add 若fep/logs/dbdump目錄不存在程式主動先建立
            File parent = new File("/fep/logs/dbdump");
            if (!parent.exists()) {
                if (parent.mkdirs())
                    this.log("mkdirs ", parent.getAbsolutePath(), " Success");
                else
                    this.log(Level.SEVERE, "mkdirs ", parent.getAbsolutePath(), " failed");
            }

            //3.	檢查 /fep/logs/dbdump/@delname.del是否存在，
            File file = new File("/fep/logs/dbdump/" + delname + ".del");

            if (!file.exists()) {
                //若不存在，則執行fepdb_exporttable.sh將資料表資料匯出，fepdb_exporttable.sh內容如下:
                //connect to FEPDB;
                //EXPORT TO /fep/logs/dbdump/@delname.del OF DEL SELECT * FROM FEP. @TABLENAME WHERE @condition;
                flgSuccess = executeShell(Arrays.asList(exporttableSh, delname, condition));
                if (flgSuccess) {
                    this.log(" fepdb_exporttable.sh Success");
                } else {
                    this.log(Level.SEVERE, " fepdb_exporttable.sh failed");
                    return false;
                }
            }

            File fileMsg = new File("/fep/logs/dbdump/" + delname + ".msg");

            if (file.exists()) {
                //執行Import前先清除之前殘留的MSG file
                if (fileMsg.exists()) {
                    fileMsg.delete();
                }
                //4.	若/fep/logs/dbdump/@delname.del的匯出檔已存在, 則繼續做下一步驟
                //5.	執行fepdb_importtable.sh將匯出檔案匯入至歷史資料庫中
                //cconnect to FEPHISDB;
                //IMPORT FROM /fep/logs/dbdump/@delname.del OF DEL modified by delprioritychar CommitCount 5000 messages
                // /fep/logs/dbdump/@delname.msg INSERT INTO FEP.@TABLENAME;
                flgSuccess = executeShell(Arrays.asList(importtableSh, delname, TABLENAME));
                if (flgSuccess) {
                    this.log(" fepdb_importtable.sh Success");
                } else {
                    this.log(Level.SEVERE, " fepdb_importtable.sh failed");
                    return false;
                }
            }
            //6.	檢查匯出檔@delname.del是否已產生及匯出訊息檔@delname.msg
            File fileDel = new File("/fep/logs/dbdump/" + delname + ".del");

            if (fileDel.exists() && fileMsg.exists()) {
                //是否有出現以下內容
                //SQL3149N  "100" rows were processed from the input file.
                //7.	若有出現代表匯出資料已成功(100為本次匯入的筆數,請將之存入至變數
                // impcount中)，然後記BatchLog “已匯入{impcount}筆資料至@TABLENAME”
                if (isSuccess(fileMsg)) {
                    //刪除
                    fileDel.delete();
                    fileMsg.delete();
                    _canDelete = true;
                }
            }
            return true;
        } catch (Exception ex) {
            this.log(Level.SEVERE, ex, "duplicateTable()執行錯誤" + ex.getMessage());
            return false;
        }
    }

    /**
     * 依條件刪除線上資料庫(FEPDB_BATCH)指定日期前的資料, 流程如下:
     *
     * @param connFEPDB
     */
    private boolean deleteTable(Connection connFEPDB) {
        //1.	設一變數condition, 若ArchivingStart = “ALL” THEN condition = “1=1”, ELSE
        // condition = CONTROLFIELD + “<” + ArchivingStart
        //2.	執行以下SQL
        //DELETE FROM FEP.@TABLENAME WHERE @condition;
        //然後記BatchLog “@TABLENAME刪除成功”
        String condition = "";
        if ("ALL".equals(ArchivingStart)) {
            condition = "1=1";
        } else {
            condition = CONTROLFIELD + "< ?";
        }
        PreparedStatement stmt = null;
        try {
            final String dropSql = "DELETE FROM FEP." + TABLENAME + " WHERE " + condition;
            this.log(" begin run sql ", dropSql.replace("?", ArchivingStart));
            stmt = connFEPDB.prepareStatement(dropSql);
            stmt.setString((int) 1, ESAPI.encoder().encodeForSQL(new DB2Codec(), ArchivingStart));
            // 2025-05-22 Richard modified for [Second Order SQL Injection]
            // int res = stmt.executeUpdate();
            int res = ReflectUtil.envokeMethod(stmt, "executeUpdate", new Class<?>[] {}, new Object[] {}, 0);
            this.log(TABLENAME, " Delete OK execute:", res);
            return true;
        } catch (SQLException e) {
            this.log(Level.SEVERE, e, TABLENAME, " Delete fail:", e.getMessage());
            return false;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
            }
        }
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

    private String convertToDatetime(String date, Integer interVal, String type) throws ParseException {
        //將8碼西元日期字串轉換成日期型態datetime ps:xingyun 根據 type=year /month /day 減去 interVal
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date toDate = formatter.parse(date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(toDate);
        if ("year".equals(type)) {
            String year = String.valueOf(Integer.parseInt(date.substring(0, 4)) - interVal);
            return year + date.substring(4, 6) + date.substring(6);
        } else if ("month".equals(type)) {
            calendar.add(Calendar.MONTH, -interVal);
            date = formatter.format(calendar.getTime());
            return date.substring(0, 4) + date.substring(5, 7) + date.substring(8);
        } else if ("day".equals(type)) {
            calendar.add(Calendar.DAY_OF_MONTH, -interVal);
            date = formatter.format(calendar.getTime());
            return date.substring(0, 4) + date.substring(5, 7) + date.substring(8);
        } else {
            return null;
        }
    }

    private String convertToROCDate(String date, Integer interVal, String type) throws ParseException {
        //將日期型態轉換成民國日期7碼字串
        String year = String.valueOf(Integer.parseInt(date.substring(0, 4)) - 1911);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date toDate = formatter.parse(date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(toDate);
        if ("year".equals(type)) {
            year = String.valueOf(Integer.parseInt(year) - interVal);
            return year + date.substring(4, 6) + date.substring(6);
        } else if ("month".equals(type)) {
            calendar.add(Calendar.MONTH, -interVal);
            date = formatter.format(calendar.getTime());
            return year + date.substring(5, 7) + date.substring(8);
        } else if ("day".equals(type)) {
            calendar.add(Calendar.DAY_OF_MONTH, -interVal);
            date = formatter.format(calendar.getTime());
            return year + date.substring(5, 7) + date.substring(8);
        } else {
            return null;
        }
    }

}
