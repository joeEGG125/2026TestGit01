package com.syscom.fep.batch.task.cmn;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.Map;

public class UpdateSysstat extends FEPBase implements Task {
    private BatchJobLibrary job = null;
    private XXOptions xx;
    private YYOptions yy;

    @Override
    public void displayUsage() {
        System.out.println("USAGE:");
        System.out.println(" UpdateSysstat Options");
        System.out.println();
        System.out.println(" Options:");
        System.out.println(" /? Display this help message.");
        System.out.println(" /XX Required, must be 04 or 05 or 06 or 12 or 20");
        System.out.println(" /YY Required, must be 0 or 1");
        System.out.println(" /CallBatchJob Optional, true (by default) or false");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println(" UpdateSysstat /XX:04 /YY:1 /BatchLogPath:/fep/BATCH/logs /CallBatchJob:true");
    }

    /**
     * Batch主流程, 從主流程呼叫各子流程, 若子流程發生ex則throw至主流程並結束AbortTask
     * 主流程每一個步驟依據_batchResult判斷是否往下執行
     * <p>
     * 2023-01-30 Richard modified 加入返回值返回code用於fep-batch-cmdline
     *
     * @param args
     * @return
     */
    @Override
    public BatchReturnCode execute(String[] args) {
        BatchReturnCode returnCode = BatchReturnCode.Succeed;
        if (args == null || args.length == 0 || "?".equals(args[0])) {
            displayUsage();
            return returnCode;
        }
        try {
            // 1. 初始化相關批次物件及拆解傳入參數
            returnCode = initialBatch(args);
            if (returnCode != BatchReturnCode.Succeed)
                return returnCode;
            // 2. 檢核批次參數是否正確, 若正確則啟動批次工作
            job.writeLog("------------------------------------------------------------------");
            job.writeLog(ProgramName + "開始");
            job.startTask();
            // 3. 批次主要處理流程
            returnCode = mainProcess();
            // 4. 通知批次作業管理系統工作正常結束
            if (returnCode == BatchReturnCode.Succeed) {
                job.writeLog(ProgramName + "正常結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.endTask();
            } else {
                job.writeLog(ProgramName + "不正常結束，停止此批次作業!!");
                job.writeLog("------------------------------------------------------------------");
                job.abortTask();
            }
            return returnCode;
        } catch (Exception e) {
            logContext.setProgramException(e);
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
            if (job != null) {
                job.writeErrorLog(e, e.getMessage());
                job.writeLog(ProgramName + "失敗!!");
                job.writeLog("------------------------------------------------------------------");
                // 通知批w作業管理系統工作失敗,暫停後面流程
                try {
                    job.abortTask();
                } catch (Exception ex) {
                    logContext.setProgramException(ex);
                    logContext.setProgramName(ProgramName);
                    sendEMS(logContext);
                }
            }
            return BatchReturnCode.ProgramException;
        } finally {
            if (job != null) {
                job.writeLog(ProgramName + "結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.dispose();
                job = null;
            }
            if (logContext != null) {
                logContext = null;
            }
        }
    }

    /**
     * 初始化相關批次物件及拆解傳入參數初始化相關批次物件及拆解傳入參數
     *
     * @param args
     */
    private BatchReturnCode initialBatch(String[] args) {
        // 0. 初始化logContext物件,傳入工作執行參數
        logContext = new LogData();
        logContext.setChannel(FEPChannel.BATCH);
        logContext.setEj(0);
        logContext.setProgramName(ProgramName);
        String batchLogPath = null;
        // 1. 檢查Batch Log目錄參數
        // 如果沒有傳入BatchLogPath, 則需要從db中獲取設定值
        if (Arrays.stream(args).noneMatch(t -> StringUtils.startsWithIgnoreCase(t, "/BatchLogPath")))
            batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
        if (StringUtils.isBlank(batchLogPath))
            batchLogPath = "/fep/logs";
        // 2. 初始化BatchJob物件,傳入工作執行參數
        job = new BatchJobLibrary(this, args, batchLogPath);
        // 列印參數
        if (ArrayUtils.isNotEmpty(args)) {
            for (String arg : args) {
                job.writeLog("接收的參數:", arg);
            }
        }
        // XX
        String value = job.getArguments().get("XX");
        if (StringUtils.isBlank(value)) {
            job.writeErrorLog(null, "XX未設定!!!");
            return BatchReturnCode.MissingArgument;
        } else if (!XXOptions.check(value)) {
            job.writeErrorLog(null, "XX不正確!!!XX:", value);
            return BatchReturnCode.InvalidArgument;
        } else {
            this.xx = XXOptions.fromOption(value);
        }
        // YY
        value = job.getArguments().get("YY");
        if (StringUtils.isBlank(value)) {
            job.writeErrorLog(null, "YY未設定!!!");
            return BatchReturnCode.MissingArgument;
        } else if (!YYOptions.check(value)) {
            job.writeErrorLog(null, "YY不正確!!!YY:", value);
            return BatchReturnCode.InvalidArgument;
        } else {
            this.yy = YYOptions.fromOption(value);
        }
        return BatchReturnCode.Succeed;
    }

    /**
     * 批次主要處理流程
     *
     * @return
     * @throws Exception
     */
    private BatchReturnCode mainProcess() throws Exception {
        JdbcTemplate jdbcTemplate = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_JDBC_TEMPLATE);
        if (jdbcTemplate == null) {
            job.writeLog("無法取到JdbcTemplate實例");
            return BatchReturnCode.ProgramException;
        }
        BatchReturnCode rtnCode = BatchReturnCode.Succeed;
        // 步驟一：
        // 查詢 & LOG 記錄更新前 SELECT 結果：
        // SELECT SYSSTAT_OPCKEYST, SYSSTAT_ATMKEYST, SYSSTAT_RMKEYST, SYSSTAT_3ENCKEYST, SYSSTAT_KEYBLOCKST FROM SYSSTAT;
        job.writeLog("查詢記錄更新前 SELECT 結果:", querySysstat(jdbcTemplate));
        // 步驟二：
        // 透過程式引數，執行以下script：XX 代表SET 的對應欄位，YY 是SET 欄位的VALUE(只能是數字)
        // XX 不是 04、05、06、12 或 VALUE 不是0 或 1，提示錯誤，中止執行。
        // 1.XX = 04
        // UPDATE FEP.SYSSTAT SET SYSSTAT_OPCKEYST = YY  WHERE SYSSTAT_HBKNO = '006';
        // 2.XX = 05
        // UPDATE FEP.SYSSTAT SET SYSSTAT_ ATMKEYST = YY WHERE SYSSTAT_HBKNO = '006';
        // 3.XX = 06
        // UPDATE FEP.SYSSTAT SET SYSSTAT_ RMKEYST = YY WHERE SYSSTAT_HBKNO = '006';
        // 4.XX = 12
        // UPDATE FEP.SYSSTAT SET SYSSTAT_3ENCKEYST = YY WHERE SYSSTAT_HBKNO = '006';
        // 5.XX = 20
        // UPDATE FEP.SYSSTAT SET SYSSTAT_KEYBLOCKST = YY WHERE SYSSTAT_HBKNO = '006';
        String sql = String.format(this.xx.getSql(), this.yy.getOption());
        job.writeLog("Start to execute, sql:", sql);
        try {
            int result = jdbcTemplate.update(sql);
            job.writeLog("Execute Succeed, sql:", sql, ", result:", result);
        } catch (Exception e) {
            job.writeErrorLog(e, "Execute Failed, sql:", sql);
            rtnCode = BatchReturnCode.DBIOError;
        }
        // 步驟三：
        // 查詢 & LOG 記錄更新後 SELECT 結果：
        // SELECT SYSSTAT_OPCKEYST, SYSSTAT_ATMKEYST, SYSSTAT_RMKEYST, SYSSTAT_3ENCKEYST, SYSSTAT_KEYBLOCKST FROM SYSSTAT;
        job.writeLog("查詢記錄更新後 SELECT 結果:", querySysstat(jdbcTemplate));
        return rtnCode;
    }

    private Map<String, Object> querySysstat(JdbcTemplate jdbcTemplate) {
        String sql = "SELECT SYSSTAT_OPCKEYST, SYSSTAT_ATMKEYST, SYSSTAT_RMKEYST, SYSSTAT_3ENCKEYST, SYSSTAT_KEYBLOCKST FROM SYSSTAT";
        job.writeLog("Start to execute, sql:", sql);
        try {
            Map<String, Object> map = jdbcTemplate.queryForMap(sql);
            job.writeLog("Execute Succeed, sql:", sql);
            return map;
        } catch (Exception e) {
            job.writeErrorLog(e, "Execute Failed, sql:", sql);
        }
        return null;
    }

    private enum XXOptions {
        OPTION_04("04", "UPDATE SYSSTAT SET SYSSTAT_OPCKEYST = %s WHERE SYSSTAT_HBKNO = '006'"),
        OPTION_05("05", "UPDATE SYSSTAT SET SYSSTAT_ATMKEYST = %s WHERE SYSSTAT_HBKNO = '006'"),
        OPTION_06("06", "UPDATE SYSSTAT SET SYSSTAT_RMKEYST = %s WHERE SYSSTAT_HBKNO = '006'"),
        OPTION_12("12", "UPDATE SYSSTAT SET SYSSTAT_3ENCKEYST = %s WHERE SYSSTAT_HBKNO = '006'"),
        OPTION_20("20", "UPDATE SYSSTAT SET SYSSTAT_KEYBLOCKST = %s WHERE SYSSTAT_HBKNO = '006'");

        private final String option, sql;

        XXOptions(String option, String sql) {
            this.option = option;
            this.sql = sql;
        }

        public String getOption() {
            return option;
        }

        public String getSql() {
            return sql;
        }

        public static boolean check(String option) {
            for (XXOptions opt : XXOptions.values()) {
                if (opt.getOption().equals(option))
                    return true;
            }
            return false;
        }

        public static XXOptions fromOption(String option) {
            for (XXOptions opt : XXOptions.values()) {
                if (opt.getOption().equals(option))
                    return opt;
            }
            return null;
        }
    }

    private enum YYOptions {
        OPTION_0("0"),
        OPTION_1("1");

        private final String option;

        YYOptions(String option) {
            this.option = option;
        }

        public String getOption() {
            return option;
        }

        public static boolean check(String option) {
            for (YYOptions opt : YYOptions.values()) {
                if (opt.getOption().equals(option))
                    return true;
            }
            return false;
        }

        public static YYOptions fromOption(String option) {
            for (YYOptions opt : YYOptions.values()) {
                if (opt.getOption().equals(option))
                    return opt;
            }
            return null;
        }
    }
}
