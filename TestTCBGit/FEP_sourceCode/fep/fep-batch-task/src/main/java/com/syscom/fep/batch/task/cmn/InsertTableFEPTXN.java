package com.syscom.fep.batch.task.cmn;

import com.syscom.fep.batch.base.task.TaskBase;
import com.syscom.fep.batch.base.task.TaskConstant;
import com.syscom.fep.frmcommon.ref.RefString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 新增資料進FEPTXN ejfno +1遞增
 *
 * @author Chen LeYun
 */
public class InsertTableFEPTXN extends TaskBase {
    // 總資料筆數
    private static int TOTAL_RECORDS = 0;
    // 每多少筆資料執行批次寫入
    private static final int BATCH_SIZE = 100;

    private static String DATETIME_DATE = "";

    public static void main(String[] args) {
        new InsertTableFEPTXN().executeMain(args);
    }

    /**
     * 顯示Usage
     */
    @Override
    public void displayUsage() {
        System.out.println("USAGE:");
        System.out.println(" InsertTableFEPTXN Options");
        System.out.println();
        System.out.println(" Options:");
        System.out.println(" /? Display this help message.");
        System.out.println(" /TxDate Optional.");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println(" InsertTableFEPTXN /TxDate:20250717 /TOTAL:200000");
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
        try {
            String tody = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
            DATETIME_DATE = findArg(args, "TxDate", tody);
            this.log("取得參數 TxDate = " + DATETIME_DATE);

            String total = findArg(args, "TOTAL", "100000");
            TOTAL_RECORDS = Integer.parseInt(total);
            // 2. 執行核心業務邏輯
            runBatchInsert();

            resultMessage.set("批次新增成功");
            return true;

        } catch (Exception e) {
            this.log("批次新增失敗！", e);
            resultMessage.set(e.getMessage());
            return false;
        }
    }

    public void runBatchInsert() throws Exception {
        Instant start = Instant.now();
        //Statement stmtFEPDB = null;
        this.log("準備開始批次新增" + TOTAL_RECORDS + "筆資料...");

        String insertSql = "INSERT INTO FEP.FEPTXN\n" +
                "(FEPTXN_TX_DATE, FEPTXN_EJFNO, FEPTXN_TXSEQ, FEPTXN_CON_TXSEQ, FEPTXN_VIR_TXSEQ, FEPTXN_CON_VIR_TXSEQ, FEPTXN_TX_DATE_ATM, FEPTXN_ATM_SEQNO, FEPTXN_CON_ATM_SEQNO1, FEPTXN_CON_ATM_SEQNO2, FEPTXN_CBS_TX_CODE, FEPTXN_CBS_RRN, FEPTXN_EC_CBS_RRN, FEPTXN_STAN, FEPTXN_ORI_STAN, FEPTXN_TX_CUR_ACT, FEPTXN_TX_CUR, FEPTXN_BKNO, FEPTXN_ATMNO, FEPTXN_TX_TIME, FEPTXN_CON_TX_TIME, FEPTXN_TX_CODE, FEPTXN_CON_TX_CODE, FEPTXN_TBSDY_FISC, FEPTXN_TBSDY, FEPTXN_TBSDY_ACT, FEPTXN_MAJOR_ACTNO, FEPTXN_CARD_SEQ, FEPTXN_TRIN_BKNO, FEPTXN_TRIN_ACTNO, FEPTXN_TRIN_ACTNO_ACTUAL, FEPTXN_TRIN_KIND, FEPTXN_TROUT_BKNO, FEPTXN_TROUT_ACTNO, FEPTXN_TROUT_KIND, FEPTXN_TX_AMT_ACT, FEPTXN_TX_AMT, FEPTXN_CBS_RC, FEPTXN_ASC_RC, FEPTXN_EXCP_CODE, FEPTXN_CON_EXCP_CODE, FEPTXN_REPLY_CODE, FEPTXN_CON_REPLY_CODE, FEPTXN_ACC_TYPE, FEPTXN_CLR_TYPE, FEPTXN_NEED_SEND_CBS, FEPTXN_ATMOD, FEPTXN_BALA, FEPTXN_BALB, FEPTXN_CBS_TIMEOUT, FEPTXN_ASC_TIMEOUT, FEPTXN_FISC_FLAG, FEPTXN_PENDING, FEPTXN_REQ_RC, FEPTXN_REP_RC, FEPTXN_CON_RC, FEPTXN_AA_RC, FEPTXN_FISC_TIMEOUT, FEPTXN_MSGFLOW, FEPTXN_SUBSYS, FEPTXN_CHANNEL, FEPTXN_PCODE, FEPTXN_TRACE_EJFNO, FEPTXN_CHANNEL_EJFNO, FEPTXN_TXRUST, FEPTXN_TXNMODE, FEPTXN_FEE_CUR, FEPTXN_FEE_CUSTPAY_ACT, FEPTXN_FEE_CUSTPAY, FEPTXN_DES_BKNO, FEPTXN_TX_DATETIME_FISC, FEPTXN_AA_COMPLETE, FEPTXN_TMO_FLAG, FEPTXN_FISCSNO, FEPTXN_RMSNO, FEPTXN_ORGRMSNO, FEPTXN_ORGDATE, FEPTXN_SENDER_BANK, FEPTXN_RECEIVER_BANK, FEPTXN_MSGID, FEPTXN_REQ_DATETIME, FEPTXN_CASH_AMT, FEPTXN_COIN_AMT, FEPTXN_FISC_CUR_MEMO, FEPTXN_BRNO, FEPTXN_DEPT, FEPTXN_TRIN_BRNO, FEPTXN_TRIN_DEPT, FEPTXN_TX_BRNO, FEPTXN_TX_DEPT, FEPTXN_ZONE_CODE, FEPTXN_ATM_BRNO, FEPTXN_ATM_ZONE, FEPTXN_ATMNO_VIR, FEPTXN_ORDER_DATE, FEPTXN_ATM_CHK, FEPTXN_ATM_TYPE, FEPTXN_TBSDY_INTL, FEPTXN_ICMARK, FEPTXN_YYMMDD, FEPTXN_TRK2, FEPTXN_TRK3, FEPTXN_IC_SEQNO, FEPTXN_IC_TAC, FEPTXN_PINBLOCK, FEPTXN_INCRE, FEPTXN_AUTHCD, FEPTXN_TX_ACTNO, FEPTXN_EXRATE, FEPTXN_SCASH, FEPTXN_ACRATE, FEPTXN_DIFRATE, FEPTXN_CBS_DSCPT, FEPTXN_SELFCD, FEPTXN_TX_CODE_ATMC, FEPTXN_TX_FEE_DR, FEPTXN_TX_FEE_CR, FEPTXN_TX_FEE_MBNK_DR, FEPTXN_TX_FEE_MBNK_CR, FEPTXN_ATM_PROFIT, FEPTXN_ACT_PROFIT, FEPTXN_ACT_LOSS, FEPTXN_APID, FEPTXN_NOTICE_ID, FEPTXN_WAY, FEPTXN_ORDER_NO, FEPTXN_MERCHANT_ID, FEPTXN_PAYTYPE, FEPTXN_PAYNO, FEPTXN_TAX_UNIT, FEPTXN_IDNO, FEPTXN_DUE_DATE, FEPTXN_RECON_SEQNO, FEPTXN_FISC_RRN, FEPTXN_BUSINESS_UNIT, FEPTXN_NPS_FEE_RCVR, FEPTXN_NPS_FEE_AGENT, FEPTXN_NPS_FEE_TROUT, FEPTXN_NPS_FEE_TRIN, FEPTXN_NPS_FEE_FISC, FEPTXN_NPS_FEE_CUSTPAY, FEPTXN_NPS_CLR, FEPTXN_NPS_MONTHLY_FG, FEPTXN_REMARK, FEPTXN_RS_CODE, FEPTXN_TX_DATETIME_PREAUTH, FEPTXN_IC_SEQNO_PREAUTH, FEPTXN_TX_AMT_PREAUTH, FEPTXN_CBS_INT_ACTNO, FEPTXN_CBS_SUP_ACTNO, FEPTXN_CBS_FEE_ACTNO, FEPTXN_CBS_VALUE_DATE, FEPTXN_CBS_TX_TIME, FEPTXN_TX_CUR_SET, FEPTXN_TX_AMT_SET, FEPTXN_ERR_MSG, FEPTXN_RWTSEQ, FEPTXN_BOX_CNT, FEPTXN_DSPCNT1, FEPTXN_DSPCNT2, FEPTXN_DSPCNT3, FEPTXN_DSPCNT4, FEPTXN_DSPCNT5, FEPTXN_DSPCNT6, FEPTXN_DSPCNT7, FEPTXN_DSPCNT8, FEPTXN_VIR_CBS_TIMEOUT, FEPTXN_VIR_ACC_TYPE, FEPTXN_VIR_CBS_TX_CODE, FEPTXN_VIR_CBS_RRN, FEPTXN_VIR_CBS_RC, FEPTXN_VIR_ERR_MSG, FEPTXN_VIR_BRNO, FEPTXN_VIR_TRIN_BRNO, FEPTXN_VIR_CBS_INT_ACTNO, FEPTXN_VIR_CBS_SUP_ACTNO, FEPTXN_PBTYPE, FEPTXN_TELEPHONE, FEPTXN_CLIENTIP, FEPTXN_CASH_WAMT, FEPTXN_COIN_WAMT, FEPTXN_ACCT_SUP, FEPTXN_BENEFIT, FEPTXN_CHREM, FEPTXN_PSBREM_F_D, FEPTXN_PSBREM_F_C, FEPTXN_ACTIVITY_CODE, FEPTXN_NONPROM_AMT, FEPTXN_FMREM, FEPTXN_NPS_AGB_FEE, UPDATE_USERID, UPDATE_TIME, FEPTXN_NOTICE_TYPE, FEPTXN_TROUT_BKNO7, FEPTXN_TRIN_BKNO7, FEPTXN_MSGKIND, FEPTXN_MULTICUR, FEPTXN_MTP, FEPTXN_DEPOSITOR, FEPTXN_COVID19, FEPTXN_DR_CUST, FEPTXN_CR_CUST, FEPTXN_SEND_2160, FEPTXN_LUCKYNO, FEPTXN_CBS_PROC)\n" +
                "VALUES(?, ?, ' ', ' ', ' ', ' ', '20250716', '0163', ' ', ' ', NULL, '', ' ', 'E0028A6', ' ', 'TWD', 'TWD', '006', 'T9998S02', '102907', NULL, 'W3 ', NULL, '20250715', '20240522', ' ', '1100600000010854', NULL, NULL, NULL, NULL, ' ', '006', '1100600000010854', ' ', 1000.00, 1000.00, NULL, NULL, 'IN', NULL, NULL, NULL, 0, 0, 0, 0, 0.00, 0.00, 0, 0, 0, 0, NULL, NULL, NULL, 15002, 0, 'A1', 3, 'ATM', '2510', 0, NULL, 'R', 1, NULL, 0.00, 0.00, NULL, '20250716100728', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 'W3', '20250716100742', 0.00, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'TWN', NULL, ' ', '00000163', 'A50B', NULL, '393939383736353530303235313031526350250716112741D870BCF1DDB0', NULL, NULL, NULL, '00000054', '4A94B7FCCF378CA1', NULL, NULL, NULL, '                ', 0.0000000, 0.0000000, 0.0000000, 0.0000000, NULL, 1, NULL, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, NULL, NULL, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, NULL, NULL, NULL, NULL, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, 0.00, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.0, 0, '2025-07-16 10:29:48.000000', NULL, NULL, NULL, NULL, ' ', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y');";
        Connection connFEPDB = null;
        try {
            connFEPDB = this.getConnection(DBName.FEPDB);
            try {
                // 1. 關閉自動提交
                connFEPDB.setAutoCommit(false);
                PreparedStatement ps = connFEPDB.prepareStatement(insertSql);
                for (int i = 1; i <= TOTAL_RECORDS; i++) {
                    ps.setString(1, DATETIME_DATE);
                    ps.setInt(2, i);
                    ps.addBatch();

                    if (i % BATCH_SIZE == 0 || i == TOTAL_RECORDS) {
                        ps.executeBatch(); // 執行批次
                    }
                }

                // 2. 所有批次執行完畢，提交交易
                this.log("所有資料執行完畢，正在提交交易 (Commit)...");
                connFEPDB.commit();
                this.log("交易成功提交！新增筆數:" + TOTAL_RECORDS);

            } catch (SQLException e) {
                this.log("交易失敗，資料庫操作錯誤，執行回滾 (Rollback)...", e);
                connFEPDB.rollback();
                throw e;
            }
        } catch (SQLException e) {
            this.log("無法建立資料庫連線或處理交易時發生嚴重錯誤！", e);
            throw e;
        } finally {
            Instant end = Instant.now();
            long timeElapsed = Duration.between(start, end).toSeconds();
            this.log("批次新增作業完成，總共花費時間: " + timeElapsed + "秒");
        }
    }

}
