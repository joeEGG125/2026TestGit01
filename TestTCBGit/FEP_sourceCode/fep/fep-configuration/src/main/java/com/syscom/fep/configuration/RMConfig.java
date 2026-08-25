package com.syscom.fep.configuration;

import java.util.List;

import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.base.enums.Protocol;

public class RMConfig {
    public static int SubSystemNo = 2;

    private static RMConfig _instance = new RMConfig();
    /**
     * 實收手續費會計科子目代號
     */
    private String _ACTFeeRM_GLACC;

    /**
     * AML 每次最大發送筆數
     */
    private int _AMLCount;

    /**
     * AML LOGIN 
     */
    private String _AMLPASS;

    /**
     * AMLServiceUrl
     */
    private String _AMLServiceUrl;

    /**
     * RM Batch程式的輸入檔目錄
     */
    private String _BatchInputPath;

    /**
     * RM Batch程式的Log目錄
     */
    private String _BatchLogPath;

    /**
     * RM Batch程式的輸出檔目錄
     */
    private String _BatchOutputPath;

    /**
     * 批次平台呼叫RM Batch Program root path
     */
    private String _BatchServicePath;

    /**
     * 信用卡繳款資料明細檔檔名單
     */
    private String _BTSCARDRT_DAT;

    /**
     * 信用卡繳款資料明細表(匯款)
     */
    private String _BTSCARDRT_RPT;

    /**
     * ServiceCalcFISCTime Sleep Seconds
     */
    private int _CalcFISCTime_INTERVAL;

    /**
     * 手續費支出會計科子目代號
     */
    private String _ChargeLossRM_GLACC;

    /**
     * 手續費收入會計科子目代號
     */
    private String _ChargeProfitRM_GLACC;

    /**
     * 奇美實業帳號
     */
    private String _ChiMei_ACTNO;

    /**
     * ServiceCLRMoniter需發出警示的門檻值
     */
    private int _CLR_ALTER_CNT;

    /**
     * ServiceCLRMoniter Sleep Seconds
     */
    private int _CLR_Interval;

    /**
     * 跨行基金水位增加金額
     */
    private int _CLRPLUSAmout;

    /**
     * 跨行餘額第2水位 增加金額
     */
    private int _CLRPLUSAmout2;

    /**
     * 跨行基金水位計算比例
     */
    private double _CLRRate;

    /**
     * 信用卡帳號
     * <remarks>2021/11/10 榮升 Fortify Privacy Violation: Heap Inspection RENAME CCard_ACTNO -> CDCTNO</remarks>
     */
    private String _CDCTNO;

    /**
     * FISCGW_RM Timeout後移至此queue
     */
    private String _DEAD_FISC;

    /**
     * ServiceFCSMoniter Sleep Seconds
     */
    private int _FCS_Interval;

    /**
     * FCS檔案處理後move目錄
     */
    private String _FCSDealFilePath;

    /**
     * FTP Server FCS檔案處理後move目錄
     */
    private String _FCSDealFilePath_FTP;

    /**
     * FCS回饋檔名稱
     */
    private String _FCSFileName;

    /**
     * FCSFTPServer的資料夾
     */
    private String _FCSFTPFolder;

    /**
     * FCSFTPServer使用者密碼
     */
    private String _FCSFTPSscode;

    /**
     * FTP Server 端口
     */
    private String _FCSFTPPort;

    /**
     * FCSFTPServer
     */
    private String _FCSFTPServer;

    /**
     * FCSFTPServer使用者ID
     */
    private String _FCSFTPUserId;

    /**
     * FCS回饋檔輸入檔目錄
     */
    private String _FCSInPath;

    /**
     * FTP Server FCS下載目錄
     */
    private String _FCSInPath_FTP;

    /**
     * FCS回饋檔輸出檔目錄
     */
    private String _FCSOutPath;

    /**
     * FTP Server FCS上傳目錄
     */
    private String _FCSOutPath_FTP;

    /**
     * GL明細檔轉成FlatFile檔名稱
     */
    private String _GLDtlOutFile;

    /**
     * GL明細檔轉成FlatFile檔輸出檔目錄
     */
    private String _GLDtlOutPath;

    /**
     * GL明細檔名for Upload
     */
    private String _GLExportFile;

    /**
     * INQueue11X1 名稱
     */
    private String _INQueue11X1;

    /**
     * 聯行往來會計科子目代號
     */
    private String _InterbranchTransfer_GLACC;

    /**
     * 應解匯款-電匯應解匯款會計科子目代號
     */
    private String _InwardRemittance_GLACC;

    /**
     * 大額匯款金額需即時查FISC PCODE=5202之跨行基金餘額
     */
    private double _LargeAmount;

    /**
     * 大額匯款金額需sleep毫秒數
     */
    private int _LargeAmountSleepSec;

    /**
     * 台幣大額匯出入彙總收寄件者Mail信箱
     */
    private String _LargeAmtMailFrom;

    /**
     * 台幣大額匯出入彙總收件者Mail信箱
     */
    private String _LargeAmtMailTo;

    /**
     * 大額匯款通報產表Email金額
     */
    private double _LargeAmtReport;

    /**
     * 其他應付款-跨行匯款手續費會計科子目代號
     */
    private String _OtherPayablesChargeRM_GLACC;

    /**
     * 其他預付款-跨行匯款會計科子目代號
     */
    private String _OtherPrepaymentsRM_GLACC;

    /**
     * 其他預收款會計科子目代號
     */
    private String _OtherRevenueRM_GLACC;

    /**
     * 匯出匯款檢查是否超過跨行基金水位金額
     */
    private int _OutwardCheckAmount;

    /**
     * 遠東銀行代匯款檔名
     */
    private String _R805R1001_Filename;

    /**
     * G:\FEP10\batch\805R1001
     */
    private String _R805R1001_FilePath;

    /**
     * AA Service接收財金送回Response的Queue
     */
    private String _RCV_FISC;

    /**
     * 財金的接收端IP(RM)
     */
    private String _ReceiveIP;

    /**
     * 財金的接收端Port(RM)
     */
    private int _ReceivePort;

    /**
     * FISC Service接收財金發動交易的Queue(RM用)
     */
    private String _REQ_FISC;

    /**
     * 豐掌櫃匯款專戶帳號
     */
    private String _RM_BSPTeller_ACTNO;

    /**
     * CBSPEND QUEUE FOR RM
     */
    private String _RM_CBSPEND;

    /**
     * 匯款檢核用的ProcessCenter
     */
    private String _RM_ProcessCenters;

    /**
     * 財金滯留磁帶檔
     */
    private String _RMBACK;

    /**
     * 錯誤記錄檔
     */
    private String _RMFileExp;

    /**
     * 新檔名
     */
    private String _RMFileIn;

    /**
     * 原檔名
     */
    private String _RMFileOIn;

    /**
     * 跨行匯款匯款中最多筆數
     */
    private int _SendCount;

    /**
     * 財金的傳送端IP(RM)
     */
    private String _SenderIP;

    /**
     * 財金的傳送端Port(RM)
     */
    private int _SenderPort;

    /**
     * Service11X1 Sleep Seconds
     */
    private int _Service11X1_INTERVAL;

    /**
     * Service11X2 Sleep Seconds
     */
    private int _Service11X2_INTERVAL;

    /**
     * Service1411 Sleep Seconds
     */
    private int _Service1411_INTERVAL;

    /**
     * Service1641 Sleep Seconds
     */
    private int _Service1641_INTERVAL;

    /**
     * Service5202 Sleep Milliseconds
     */
    private int _Service5202_INTERVAL;

    /**
     * ServiceCLRCheck Sleep Second
     */
    private int _ServiceCLRCheck_Interval;

    /**
     * ServiceOutBatch Sleep Seconds
     */
    private int _ServiceOutBatch_INTERVAL;

    /**
     * FEDI Sup A/C-跨行匯款會計科子目代號
     */
    private String _SuspenseActnoFEDI_GLACC;

    /**
     * PSP Sup A/C-跨行匯款會計科子目代
     */
    private String _SuspenseActnoPSP_GLACC;

    /**
     * Sup A/C-跨行匯款會計科子目代號
     */
    private String _SuspenseActnoRM_GLACC;

    /**
     * 緊急匯款 Sup A/C-跨行匯款會計科子目代號
     */
    private String _SuspenseActnoRM2_GLACC;

    /**
     * ServiceT24Moniter Sleep Seconds
     */
    private int _T24_Interval;

    /**
     * T24 匯款往來明細檔名
     */
    private String _T24FTPFileName;

    /**
     * T24上行電文內的 SSCode (匯款)
     */
    private String _T24SSCode;

    /**
     * T24 WebService Url(RM)
     */
    private String _T24ServiceUrl;

    /**
     * 決定送哪一個24 WebService Flag(1:FEP, 2:BRS)
     */
    private int _T24Type;

    /**
     * T24上行電文內的 User Name(匯款)
     */
    private String _T24UserName;

    /**
     * AA Service送交易給財金用的Queue(RM用)
     */
    private String _TO_FISC;

    /**
     * ServiceCalcFISCTime需發出警示的門檻值
     */
    private int _Warning_CalcFISCTime;

    /**
     * Richard add
     */
    private String _TO_FISC_HOST;
    /**
     * Richard add
     */
    private int _TO_FISC_PORT;
    /**
     * Richard add
     */
    private Protocol _TO_FISC_PROTOCOL;

    private RMConfig() {
        fillDataToProperty();
    }

    /**
     * 實收手續費會計科子目代號
     *
     * @return
     */
    public String getACTFeeRMGLACC() {
        return _ACTFeeRM_GLACC;
    }

    /**
     * AML 每次最大發送筆數
     *
     * @return
     */
    public int getAMLCount() {
        return _AMLCount;
    }

    /**
     * AML LOGIN 
     *
     * @return
     */
    public String getAMLPASS() {
        return _AMLPASS;
    }

    /**
     * AMLServiceUrl
     *
     * @return
     */
    public String getAMLServiceUrl() {
        return _AMLServiceUrl;
    }

    /**
     * 信用卡繳款資料明細檔檔名單
     *
     * @return
     */
    public String getBtscardrtDat() {
        return _BTSCARDRT_DAT;
    }

    /**
     * 信用卡繳款資料明細表(匯款)
     *
     * @return
     */
    public String getBtscardrtRpt() {
        return _BTSCARDRT_RPT;
    }

    /**
     * ServiceCalcFISCTime Sleep Seconds
     *
     * @return
     */
    public int getCalcFISCTimeINTERVAL() {
        return _CalcFISCTime_INTERVAL;
    }

    /**
     * 手續費支出會計科子目代號
     *
     * @return
     */
    public String getChargeLossRMGLACC() {
        return _ChargeLossRM_GLACC;
    }

    /**
     * 手續費收入會計科子目代號
     *
     * @return
     */
    public String getChargeProfitRMGLACC() {
        return _ChargeProfitRM_GLACC;
    }

    /**
     * 奇美實業帳號
     *
     * @return
     */
    public String getChiMeiACTNO() {
        return _ChiMei_ACTNO;
    }

    /**
     * ServiceCLRMoniter需發出警示的門檻值
     *
     * @return
     */
    public int getClrAlterCnt() {
        return _CLR_ALTER_CNT;
    }

    /**
     * ServiceCLRMoniter Sleep Seconds
     *
     * @return
     */
    public int getCLRInterval() {
        return _CLR_Interval;
    }

    /**
     * 跨行基金水位增加金額
     *
     * @return
     */
    public int getCLRPLUSAmout() {
        return _CLRPLUSAmout;
    }

    /**
     * 跨行餘額第2水位 增加金額
     *
     * @return
     */
    public int getCLRPLUSAmout2() {
        return _CLRPLUSAmout2;
    }

    /**
     * 跨行基金水位計算比例
     *
     * @return
     */
    public double getCLRRate() {
        return _CLRRate;
    }

    /**
     * 信用卡帳號
     *
     * @return
     */
    public String getCDCTNO() {
        return _CDCTNO;
    }

    /**
     * ServiceFCSMoniter Sleep Seconds
     *
     * @return
     */
    public int getFCSInterval() {
        return _FCS_Interval;
    }

    /**
     * FCS檔案處理後move目錄
     *
     * @return
     */
    public String getFCSDealFilePath() {
        return _FCSDealFilePath;
    }

    /**
     * FTP Server FCS檔案處理後move目錄
     *
     * @return
     */
    public String getFCSDealFilePathFTP() {
        return _FCSDealFilePath_FTP;
    }

    /**
     * FCS回饋檔名稱
     *
     * @return
     */
    public String getFCSFileName() {
        return _FCSFileName;
    }

    /**
     * FCSFTPServer的資料夾
     *
     * @return
     */
    public String getFCSFTPFolder() {
        return _FCSFTPFolder;
    }

    /**
     * FCSFTPServer使用者密碼
     *
     * @return
     */
    public String getFCSFTPSscode() {
        return _FCSFTPSscode;
    }

    /**
     * FTP Server 端口
     *
     * @return
     */
    public String getFCSFTPPort() {
        return _FCSFTPPort;
    }

    /**
     * FCSFTPServer
     *
     * @return
     */
    public String getFCSFTPServer() {
        return _FCSFTPServer;
    }

    /**
     * FCSFTPServer使用者ID
     *
     * @return
     */
    public String getFCSFTPUserId() {
        return _FCSFTPUserId;
    }

    /**
     * FCS回饋檔輸入檔目錄
     *
     * @return
     */
    public String getFCSInPath() {
        return _FCSInPath;
    }

    /**
     * FTP Server FCS下載目錄
     *
     * @return
     */
    public String getFCSInPathFTP() {
        return _FCSInPath_FTP;
    }

    /**
     * FCS回饋檔輸出檔目錄
     *
     * @return
     */
    public String getFCSOutPath() {
        return _FCSOutPath;
    }

    /**
     * FTP Server FCS上傳目錄
     *
     * @return
     */
    public String getFCSOutPathFTP() {
        return _FCSOutPath_FTP;
    }

    /**
     * GL明細檔轉成FlatFile檔名稱
     *
     * @return
     */
    public String getGLDtlOutFile() {
        return _GLDtlOutFile;
    }

    /**
     * GL明細檔轉成FlatFile檔輸出檔目錄
     *
     * @return
     */
    public String getGLDtlOutPath() {
        return _GLDtlOutPath;
    }

    /**
     * GL明細檔名for Upload
     *
     * @return
     */
    public String getGLExportFile() {
        return _GLExportFile;
    }

    /**
     * INQueue11X1 名稱
     *
     * @return
     */
    public String getINQueue11X1() {
        return _INQueue11X1;
    }

    /**
     * 聯行往來會計科子目代號
     *
     * @return
     */
    public String getInterbranchTransferGLACC() {
        return _InterbranchTransfer_GLACC;
    }

    /**
     * 應解匯款-電匯應解匯款會計科子目代號
     *
     * @return
     */
    public String getInwardRemittanceGLACC() {
        return _InwardRemittance_GLACC;
    }

    /**
     * 大額匯款金額需即時查FISC PCODE=5202之跨行基金餘額
     *
     * @return
     */
    public double getLargeAmount() {
        return _LargeAmount;
    }

    /**
     * 大額匯款金額需sleep毫秒數
     *
     * @return
     */
    public int getLargeAmountSleepSec() {
        return _LargeAmountSleepSec;
    }

    /**
     * 台幣大額匯出入彙總寄件者Mail信箱
     *
     * @return
     */
    public String getLargeAmtMailFrom() {
        return _LargeAmtMailFrom;
    }

    /**
     * 台幣大額匯出入彙總收件者Mail信箱
     *
     * @return
     */
    public String getLargeAmtMailTo() {
        return _LargeAmtMailTo;
    }

    /**
     * 大額匯款通報產表Email金額
     *
     * @return
     */
    public double getLargeAmtReport() {
        return _LargeAmtReport;
    }

    /**
     * 其他應付款-跨行匯款手續費會計科子目代號
     *
     * @return
     */
    public String getOtherPayablesChargeRMGLACC() {
        return _OtherPayablesChargeRM_GLACC;
    }

    /**
     * 其他預付款-跨行匯款會計科子目代號
     *
     * @return
     */
    public String getOtherPrepaymentsRMGLACC() {
        return _OtherPrepaymentsRM_GLACC;
    }

    /**
     * 其他預收款會計科子目代號
     *
     * @return
     */
    public String getOtherRevenueRMGLACC() {
        return _OtherRevenueRM_GLACC;
    }

    /**
     * 匯出匯款檢查是否超過跨行基金水位金額
     *
     * @return
     */
    public int getOutwardCheckAmount() {
        return _OutwardCheckAmount;
    }

    /**
     * 遠東銀行代匯款檔名
     *
     * @return
     */
    public String getR805R1001Filename() {
        return _R805R1001_Filename;
    }

    /**
     * G:\FEP10\batch\805R1001
     *
     * @return
     */
    public String getR805R1001FilePath() {
        return _R805R1001_FilePath;
    }

    /**
     * AA Service接收財金送回Response的Queue
     *
     * @return
     */
    public String getRcvFisc() {
        return _RCV_FISC;
    }

    /**
     * 財金的接收端IP(RM)
     *
     * @return
     */
    public String getReceiveIP() {
        return _ReceiveIP;
    }

    /**
     * 財金的接收端Port(RM)
     *
     * @return
     */
    public int getReceivePort() {
        return _ReceivePort;
    }

    /**
     * FISC Service接收財金發動交易的Queue(RM用)
     *
     * @return
     */
    public String getReqFisc() {
        return _REQ_FISC;
    }

    /**
     * 豐掌櫃匯款專戶帳號
     *
     * @return
     */
    public String getRMBSPTellerACTNO() {
        return _RM_BSPTeller_ACTNO;
    }

    /**
     * CBSPEND QUEUE FOR RM
     *
     * @return
     */
    public String getRmCbspend() {
        return _RM_CBSPEND;
    }

    /**
     * 匯款檢核用的ProcessCenter
     *
     * @return
     */
    public String getRMProcessCenters() {
        return _RM_ProcessCenters;
    }

    /**
     * 財金滯留磁帶檔
     *
     * @return
     */
    public String getRMBACK() {
        return _RMBACK;
    }

    /**
     * 錯誤記錄檔
     *
     * @return
     */
    public String getRMFileExp() {
        return _RMFileExp;
    }

    /**
     * 新檔名
     *
     * @return
     */
    public String getRMFileIn() {
        return _RMFileIn;
    }

    /**
     * 原檔名
     *
     * @return
     */
    public String getRMFileOIn() {
        return _RMFileOIn;
    }

    /**
     * 跨行匯款匯款中最多筆數
     *
     * @return
     */
    public int getSendCount() {
        return _SendCount;
    }

    /**
     * 財金的傳送端IP(RM)
     *
     * @return
     */
    public String getSenderIP() {
        return _SenderIP;
    }

    /**
     * 財金的傳送端Port(RM)
     *
     * @return
     */
    public int getSenderPort() {
        return _SenderPort;
    }

    /**
     * Service11X1 Sleep Seconds
     *
     * @return
     */
    public int getService11X1INTERVAL() {
        return _Service11X1_INTERVAL;
    }

    /**
     * Service11X2 Sleep Seconds
     *
     * @return
     */
    public int getService11X2INTERVAL() {
        return _Service11X2_INTERVAL;
    }

    /**
     * Service1411 Sleep Seconds
     *
     * @return
     */
    public int getService1411INTERVAL() {
        return _Service1411_INTERVAL;
    }

    /**
     * Service1641 Sleep Seconds
     *
     * @return
     */
    public int getService1641INTERVAL() {
        return _Service1641_INTERVAL;
    }

    /**
     * Service5202 Sleep Milliseconds
     *
     * @return
     */
    public int getService5202INTERVAL() {
        return _Service5202_INTERVAL;
    }

    /**
     * ServiceCLRCheck Sleep Second
     *
     * @return
     */
    public int getServiceCLRCheckInterval() {
        return _ServiceCLRCheck_Interval;
    }

    /**
     * ServiceOutBatch Sleep Seconds
     *
     * @return
     */
    public int getServiceOutBatchINTERVAL() {
        return _ServiceOutBatch_INTERVAL;
    }

    /**
     * FEDI Sup A/C-跨行匯款會計科子目代號
     *
     * @return
     */
    public String getSuspenseActnoFEDIGLACC() {
        return _SuspenseActnoFEDI_GLACC;
    }

    /**
     * Sup A/C-跨行匯款會計科子目代號
     *
     * @return
     */
    public String getSuspenseActnoRMGLACC() {
        return _SuspenseActnoRM_GLACC;
    }

    /**
     * 緊急匯款 Sup A/C-跨行匯款會計科子目代號
     *
     * @return
     */
    public String getSuspenseActnoRM2GLACC() {
        return _SuspenseActnoRM2_GLACC;
    }

    /**
     * ServiceT24Moniter Sleep Seconds
     *
     * @return
     */
    public int getT24Interval() {
        return _T24_Interval;
    }

    /**
     * T24 匯款往來明細檔名
     *
     * @return
     */
    public String getT24FTPFileName() {
        return _T24FTPFileName;
    }

    /**
     * T24上行電文內的  (匯款)
     *
     * @return
     */
    public String getT24SSCode() {
        return _T24SSCode;
    }

    /**
     * T24 WebService Url(RM)
     *
     * @return
     */
    public String getT24ServiceUrl() {
        return _T24ServiceUrl;
    }

    /**
     * 決定送哪一個24 WebService Flag(1:FEP, 2:BRS)
     *
     * @return
     */
    public int getT24Type() {
        return _T24Type;
    }

    /**
     * T24上行電文內的 User Name(匯款)
     *
     * @return
     */
    public String getT24UserName() {
        return _T24UserName;
    }

    /**
     * AA Service送交易給財金用的Queue(RM用)
     *
     * @return
     */
    public String getToFisc() {
        return _TO_FISC;
    }

    /**
     * ServiceCalcFISCTime需發出警示的門檻值
     *
     * @return
     */
    public int getWarningCalcFISCTime() {
        return _Warning_CalcFISCTime;
    }

    public static RMConfig getInstance() {
        return _instance;
    }

    /**
     * RM Batch程式的Log目錄
     *
     * @return
     */
    public String getBatchLogPath() {
        return _BatchLogPath;
    }

    public String getBatchInputPath() {
        return _BatchInputPath;
    }

    public String getBatchOutputPath() {
        return _BatchOutputPath;
    }

    public String getBatchServicePath() {
        return _BatchServicePath;
    }

    public String getToFiscHost() {
        return _TO_FISC_HOST;
    }

    public int getToFiscPort() {
        return _TO_FISC_PORT;
    }

    public Protocol getToFiscProtocol() {
        return _TO_FISC_PROTOCOL;
    }

    public String getDEAD_FISC() {
        return _DEAD_FISC;
    }

    public String getSuspenseActnoPSP_GLACC() {
        return _SuspenseActnoPSP_GLACC;
    }

    private void fillDataToProperty() {
        List<Sysconf> sysconfList = FEPCache.getSysconfList(SubSystemNo);
        String sysconfValue = null;
        for (Sysconf sysconf : sysconfList) {
            sysconfValue = sysconf.getSysconfValue();
            switch (sysconf.getSysconfName()) {
                case "ACTFeeRM_GLACC":
                    _ACTFeeRM_GLACC = DbHelper.toString(sysconfValue);
                    break;
                case "AMLCount":
                    _AMLCount = DbHelper.toInteger(sysconfValue);
                    break;
                case "AMLPASS":
                    _AMLPASS = DbHelper.toString(sysconfValue);
                    break;
                case "AMLServiceUrl":
                    _AMLServiceUrl = DbHelper.toString(sysconfValue);
                    break;
                case "BTSCARDRT_DAT":
                    _BTSCARDRT_DAT = DbHelper.toString(sysconfValue);
                    break;
                case "BTSCARDRT_RPT":
                    _BTSCARDRT_RPT = DbHelper.toString(sysconfValue);
                    break;
                case "CalcFISCTime_INTERVAL":
                    _CalcFISCTime_INTERVAL = DbHelper.toInteger(sysconfValue);
                    break;
                case "ChargeLossRM_GLACC":
                    _ChargeLossRM_GLACC = DbHelper.toString(sysconfValue);
                    break;
                case "ChargeProfitRM_GLACC":
                    _ChargeProfitRM_GLACC = DbHelper.toString(sysconfValue);
                    break;
                case "ChiMei_ACTNO":
                    _ChiMei_ACTNO = DbHelper.toString(sysconfValue);
                    break;
                case "CLR_ALTER_CNT":
                    _CLR_ALTER_CNT = DbHelper.toInteger(sysconfValue);
                    break;
                case "CLR_Interval":
                    _CLR_Interval = DbHelper.toInteger(sysconfValue);
                    break;
                case "CLRPLUSAmout":
                    _CLRPLUSAmout = DbHelper.toInteger(sysconfValue);
                    break;
                case "CLRPLUSAmout2":
                    _CLRPLUSAmout2 = DbHelper.toInteger(sysconfValue);
                    break;
                case "CLRRate":
                    _CLRRate = DbHelper.toDouble(sysconfValue);
                    break;
                case "CCard_ACTNO":
                case "CDCTNO":
                    _CDCTNO = DbHelper.toString(sysconfValue);
                    break;
                case "FCS_Interval":
                    _FCS_Interval = DbHelper.toInteger(sysconfValue);
                    break;
                case "FCSDealFilePath":
                    _FCSDealFilePath = DbHelper.toString(sysconfValue);
                    break;
                case "FCSDealFilePath_FTP":
                    _FCSDealFilePath_FTP = DbHelper.toString(sysconfValue);
                    break;
                case "FCSFileName ":
                    _FCSFileName = DbHelper.toString(sysconfValue);
                    break;
                case "FCSFTPFolder":
                    _FCSFTPFolder = DbHelper.toString(sysconfValue);
                    break;
                case "FCSFTPPassword":
                case "FCSFTPSCode":
                    _FCSFTPSscode = DbHelper.toString(sysconfValue);
                    break;
                case "FCSFTPPort":
                    _FCSFTPPort = DbHelper.toString(sysconfValue);
                    break;
                case "FCSFTPServer ":
                    _FCSFTPServer = DbHelper.toString(sysconfValue);
                    break;
                case "FCSFTPUserId":
                    _FCSFTPUserId = DbHelper.toString(sysconfValue);
                    break;
                case "FCSInPath":
                    _FCSInPath = DbHelper.toString(sysconfValue);
                    break;
                case "FCSInPath_FTP":
                    _FCSInPath_FTP = DbHelper.toString(sysconfValue);
                    break;
                case "FCSOutPath":
                    _FCSOutPath = DbHelper.toString(sysconfValue);
                    break;
                case "FCSOutPath_FTP":
                    _FCSOutPath_FTP = DbHelper.toString(sysconfValue);
                    break;
                case "GLDtlOutFile":
                    _GLDtlOutFile = DbHelper.toString(sysconfValue);
                    break;
                case "GLDtlOutPath":
                    _GLDtlOutPath = DbHelper.toString(sysconfValue);
                    break;
                case "GLExportFile":
                    _GLExportFile = DbHelper.toString(sysconfValue);
                    break;
                case "INQueue11X1":
                    _INQueue11X1 = DbHelper.toString(sysconfValue);
                    break;
                case "InterbranchTransfer_GLACC":
                    _InterbranchTransfer_GLACC = DbHelper.toString(sysconfValue);
                    break;
                case "InwardRemittance_GLACC":
                    _InwardRemittance_GLACC = DbHelper.toString(sysconfValue);
                    break;
                case "LargeAmount":
                    _LargeAmount = DbHelper.toDouble(sysconfValue);
                    break;
                case "LargeAmountSleepSec":
                    _LargeAmountSleepSec = DbHelper.toInteger(sysconfValue);
                    break;
                case "LargeAmtMailFrom":
                    _LargeAmtMailFrom = DbHelper.toString(sysconfValue);
                    break;
                case "LargeAmtMailTo":
                    _LargeAmtMailTo = DbHelper.toString(sysconfValue);
                    break;
                case "LargeAmtReport":
                    _LargeAmtReport = DbHelper.toDouble(sysconfValue);
                    break;
                case "OtherPayablesChargeRM_GLACC":
                    _OtherPayablesChargeRM_GLACC = DbHelper.toString(sysconfValue);
                    break;
                case "OtherPrepaymentsRM_GLACC":
                    _OtherPrepaymentsRM_GLACC = DbHelper.toString(sysconfValue);
                    break;
                case "OtherRevenueRM_GLACC":
                    _OtherRevenueRM_GLACC = DbHelper.toString(sysconfValue);
                    break;
                case "OutwardCheckAmount":
                    _OutwardCheckAmount = DbHelper.toInteger(sysconfValue);
                    break;
                case "R805R1001_Filename":
                    _R805R1001_Filename = DbHelper.toString(sysconfValue);
                    break;
                case "R805R1001_FilePath":
                    _R805R1001_FilePath = DbHelper.toString(sysconfValue);
                    break;
                case "RCV_FISC":
                    _RCV_FISC = DbHelper.toString(sysconfValue);
                    break;
                case "ReceiveIP":
                    _ReceiveIP = DbHelper.toString(sysconfValue);
                    break;
                case "ReceivePort":
                    _ReceivePort = DbHelper.toInteger(sysconfValue);
                    break;
                case "REQ_FISC":
                    _REQ_FISC = DbHelper.toString(sysconfValue);
                    break;
                case "RM_BSPTeller_ACTNO":
                    _RM_BSPTeller_ACTNO = DbHelper.toString(sysconfValue);
                    break;
                case "RM_CBSPEND":
                    _RM_CBSPEND = DbHelper.toString(sysconfValue);
                    break;
                case "RM_ProcessCenters":
                    _RM_ProcessCenters = DbHelper.toString(sysconfValue);
                    break;
                case "RMBACK":
                    _RMBACK = DbHelper.toString(sysconfValue);
                    break;
                case "RMFileExp":
                    _RMFileExp = DbHelper.toString(sysconfValue);
                    break;
                case "RMFileIn":
                    _RMFileIn = DbHelper.toString(sysconfValue);
                    break;
                case "RMFileOIn":
                    _RMFileOIn = DbHelper.toString(sysconfValue);
                    break;
                case "SendCount":
                    _SendCount = DbHelper.toInteger(sysconfValue);
                    break;
                case "SenderIP":
                    _SenderIP = DbHelper.toString(sysconfValue);
                    break;
                case "SenderPort":
                    _SenderPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "Service11X1_INTERVAL":
                    _Service11X1_INTERVAL = DbHelper.toInteger(sysconfValue);
                    break;
                case "Service11X2_INTERVAL":
                    _Service11X2_INTERVAL = DbHelper.toInteger(sysconfValue);
                    break;
                case "Service1411_INTERVAL":
                    _Service1411_INTERVAL = DbHelper.toInteger(sysconfValue);
                    break;
                case "Service1641_INTERVAL":
                    _Service1641_INTERVAL = DbHelper.toInteger(sysconfValue);
                    break;
                case "Service5202_INTERVAL":
                    _Service5202_INTERVAL = DbHelper.toInteger(sysconfValue);
                    break;
                case "ServiceCLRCheck_Interval":
                    _ServiceCLRCheck_Interval = DbHelper.toInteger(sysconfValue);
                    break;
                case "ServiceOutBatch_INTERVAL":
                    _ServiceOutBatch_INTERVAL = DbHelper.toInteger(sysconfValue);
                    break;
                case "SuspenseActnoFEDI_GLACC":
                    _SuspenseActnoFEDI_GLACC = DbHelper.toString(sysconfValue);
                    break;
                case "SuspenseActnoRM_GLACC":
                    _SuspenseActnoRM_GLACC = DbHelper.toString(sysconfValue);
                    break;
                case "SuspenseActnoRM2_GLACC":
                    _SuspenseActnoRM2_GLACC = DbHelper.toString(sysconfValue);
                    break;
                case "T24_Interval":
                    _T24_Interval = DbHelper.toInteger(sysconfValue);
                    break;
                case "T24FTPFileName":
                    _T24FTPFileName = DbHelper.toString(sysconfValue);
                    break;
                case "T24Password":
                case "T24SSCode":
                    _T24SSCode = DbHelper.toString(sysconfValue);
                    break;
                case "T24ServiceUrl":
                    _T24ServiceUrl = DbHelper.toString(sysconfValue);
                    break;
                case "T24Type":
                    _T24Type = DbHelper.toInteger(sysconfValue);
                    break;
                case "T24UserName":
                    _T24UserName = DbHelper.toString(sysconfValue);
                    break;
                case "TO_FISC":
                    _TO_FISC = DbHelper.toString(sysconfValue);
                    break;
                case "Warning_CalcFISCTime":
                    _Warning_CalcFISCTime = DbHelper.toInteger(sysconfValue);
                    break;
                case "BatchInputPath":
                    _BatchInputPath = DbHelper.toString(sysconfValue);
                    break;
                case "BatchLogPath":
                    _BatchLogPath = DbHelper.toString(sysconfValue);
                    break;
                case "BatchOutputPath":
                    _BatchOutputPath = DbHelper.toString(sysconfValue);
                    break;
                case "BatchServicePath":
                    _BatchServicePath = DbHelper.toString(sysconfValue);
                    break;
                case "TO_FISC_HOST":
                    _TO_FISC_HOST = DbHelper.toString(sysconfValue);
                    break;
                case "TO_FISC_PORT":
                    _TO_FISC_PORT = DbHelper.toInteger(sysconfValue);
                    break;
                case "TO_FISC_PROTOCOL":
                    _TO_FISC_PROTOCOL = Enum.valueOf(Protocol.class, DbHelper.toString(sysconfValue).toLowerCase());
                    break;
                case "DEAD_FISC":
                    _DEAD_FISC = DbHelper.toString(sysconfValue);
                    break;
            }
        }
    }
}
