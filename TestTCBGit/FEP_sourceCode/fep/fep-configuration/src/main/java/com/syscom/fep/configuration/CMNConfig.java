package com.syscom.fep.configuration;

import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.mybatis.model.Sysconf;

import java.util.List;

public class CMNConfig {
    public static final int SubSystemNo = 9;
    private static final CMNConfig _instance = new CMNConfig();
    public String _CBSIP;
    public int _CBSPort;
    private String _ATMCASHFTPFolder;
    private int _ATMMaxThreads;
    private String _ATMMON_QUEUE;
    private int _ATMP;
    private int _ATMPREQ;
    private String _ATMTXSummaryFile;
    private String _ATMTXSummaryFTPFolder;
    private String _BATCH_QUEUE;
    private String _BatchInputPath;
    private String _BatchLogPath;
    private String _BatchOutputPath;
    private String _BatchServicePath;
    private String _BDMFTPServer;
    private String _BranchBroadcastUrl;
    private String _BT010010File;
    private String _BT010020File;
    private String _BT010090File;
    private int _CARD;
    private int _CBSTimeoutRerunMaxThreads;
    private String _CCardBRNO;
    private int _CreditMQExpiry;
    private String _CreditMQServerChannel;
    private String _CreditMQServerIP;
    private int _CreditMQServerPort;
    private String _CreditMQServerPutQueue01;
    private String _CreditMQServerPutQueue02;
    private String _CreditMQServerPutQueue03;
    private String _CreditMQServerPutQueue05;
    private String _CreditMQServerPutQueue06;
    private String _CreditMQServerPutQueue07;
    private String _CreditMQServerPutQueue09;
    private String _CreditMQServerPutQueue10;
    private String _CreditMQServerPutQueue11;
    private String _CreditMQServerPutQueue15;
    private String _CreditMQServerPutQueue16;
    private String _CreditMQServerPutQueue17;
    private String _CreditMQServerPutQueue20;
    private String _CreditMQServerPutQueue21;
    private String _CreditMQServerPutQueue23;
    private String _CreditMQServerQueueMgr;
    private String _CreditMQServerReplyQueue01;
    private String _CreditMQServerReplyQueue02;
    private String _CreditMQServerReplyQueue06;
    private String _CreditMQServerReplyQueue07;
    private String _CreditMQServerReplyQueue22;
    private String _CreditMQServerRequestQueue;
    private String _CreditMQServerResponseQueue;
    private int _CreditTimeout;
    private String _CSHotline;
    private String _DEAD_ATMP;
    private String _DEAD_SC;
    private int _ENCLibRetryCount;
    private int _ENCLibRetryInterval;
    private String _EchoTestReply;
    private String _EJFTPServer;
    private String _EJSourceFTPServer;
    private String _EMS_ListenIP;
    private int _EMS_ListenPort;
    private String _EMSQueue;
    private String _ENoticeFTPServer;
    private String _FCSServiceUrl;
    private String _FEDIServiceUrl;
    private String _FEPBinPath;
    private String _FEPServerIP;
    private String _fepservice_queue;
    private int _FISCMaxThreads;
    private String _FTPHKSTATMNT;
    private String _FTPMOSTATMNT;
    private String _FTPServer1;
    private String _FTPServer2;
    private String _FTPServer3;
    private String _FTPServer4;
    private String _FTPServer5;
    private String _FTPServer6;
    private String _FTPServer7;
    private String _FTPServer8;
    private String _FTPServer9;
    private String _FunCashierFTPServer;
    private String _GARBAGE;
    private String _GLLinkSrvName;
    private String _GLSysCode;
    private String _GLUpdUserID;
    private String _HisEMSQueue;
    private String _HKCutTime;
    private int _HKG;
    private String _HKGLLinkSrvName;
    private String _HKGLSysCode;
    private String _HKGLUpdUserID;
    private String _HKGUnisysIP;
    private int _HKGUnisysLocalPort;
    private int _HKGUnisysLocalPortN;
    private int _HKGUnisysPort;
    private int _HKGUnisysPortN;
    private String _HKSMSHotLine;
    private String _HKSMSPassword;
    private String _HKSMSServiceUrl;
    private String _HKSMSText1;
    private String _HKSMSText2;
    private String _HKSMSUserName;
    private String _HKTransferSMSUrl;
    private String _ImportATMDataFile;
    private String _ImportCOIDFile;
    private String _ImportEF1001File;
    private String _ImportEF1002File;
    private String _ImportFEPUserFile;
    private String _ImportFEPUserOutput;
    private int _IPADueDay;
    private int _KeepAliveInterval;
    private int _KeepAliveTime;
    private int _LateResponseMaxThreads;
    private int _ListenBacklog;
    private int _MAC;
    private String _MACUnisysIP;
    private int _MACUnisysLocalPort;
    private int _MACUnisysLocalPortN;
    private int _MACUnisysPort;
    private int _MACUnisysPortN;
    private String _MailFromAddress;
    private String _MailServerIP;
    private String _MailToAddressEZISAM;
    private String _MailToAddressEZLost;
    private String _MailToAddressSEC;
    private String _MailToAddressTOGO;
    private String _MaxBrno;
    private String _MMAB2BServiceUrl;
    private String _MO205ChargeFG;
    private String _MOCutTime;
    private String _MOGLLinkSrvName;
    private String _MOGLSysCode;
    private String _MOGLUpdUserID;
    private int _Phase;
    private String _PRNAllowIP;
    private String _PRNCodeLetterIP;
    private int _PRNCodeLetterPort;
    private int _PRNSocketTimeOut;
    private String _RCV_ATMP;
    private String _RCV_SC;
    private String _RCV_T24;
    private String _REP_ATMP;
    private String _ReportingFTPFolder;
    private String _REQ_ATMP;
    private String _REQ_SC;
    private int _SCMaxThreads;
    private String _SECFTPServer;
    private int _SECMaxThreads;
    private String _SFTPGroupCode;
    private String _SFTPSender;
    private String _SFTPUrl;
    private String _StopNotification;
    private String _SuipAddress;
    private int _SuipTimeout;
    private String _T24FTPDirName;
    private String _T24MailFromAddress;
    private String _T24MailToAddress;
    private int _T24MaxThreads;
    private String _T24MQServerChannel;
    private String _T24MQServerIP;
    private int _T24MQServerPort;
    private String _T24MQServerQueueMgr;
    private String _T24MQServerReplyQueue;
    private String _T24MQServerRequestQueue;
    private int _T24MQTimeout;
    private String _T24ServiceUrl;
    private int _T24Timeout;
    private int _TimeToDeadQueue;
    private String _TO_ATMP;
    private String _TO_ATMP_RM;
    private String _TO_SC;
    private String _TO_T24;
    private String _TO_WEBATM;
    private String _UnisysATMPIP;
    private int _UnisysATMPLocalPort;
    private int _UnisysATMPPort;
    private int _UnisysATMPRequestLocalPort;
    private int _UnisysATMPRequestPort;
    private int _UnisysCardLocalPort;
    private int _UnisysCardRequestLocalPort;
    private int _UnisysCardRequestPort;
    private int _UnisysCardTimeout;
    private boolean _UnisysEnableATMP;
    private boolean _UnisysEnableATMPRequest;
    private boolean _UnisysEnableCard;
    private boolean _UnisysEnableCardRequest;
    private boolean _UnisysEnableHKG;
    private boolean _UnisysEnableMAC;
    private boolean _UnisysEnableRM;
    private boolean _UnisysEnableRMRequest;
    private int _UnisysGWMaxThreads;
    private int _UnisysRequestMaxThreads;
    private String _UnisysRMIP;
    private int _UnisysRMLocalPort;
    private int _UnisysRMPort;
    private int _UnisysRMRequestLocalPort;
    private int _UnisysTimeout;
    private String _VirtualActno;
    private String _WebATMIP;
    private String _HeadOffice;
    private int _FiscPassRetryCnt;
    private String _NBDesBankID;
    private String _NotifyApiUrl;
    private String _GPG_Passphrase;
    private String _GPG_Path;
    private String _GPG_PrivateKeyId;
    private String _GPG_PublicKeyId;
    private String _IMSHostName;
    private int _IMSPort;
    private String _IMSClientId;
    private int _CBSTimeout;
    private String _IMSDatastoreName;
    private String _SMSDomain;
    private String _SMSUserName;
    private String _SMSSscode;
    private String _isCBSTest;
    private String _MobileQueryUrl;
    private String _TrinAccountQueryUrl;
    private String _EnableAutoRestart;
    private String SFTPServer;
    private String NBMQServerIP;
    private String NBMQServerPort;
    private String NBMQServerChannel;
    private String NBMQServerQueueMgr;
    private String NBMQServerRequestQueue;
    private String NBMQServerReplyQueue;
    private String NBTimeout;
    private String NBMaxThreads;
    private String SFTPSCode;
    private String SFTPUCode;
    private String DiskValue;
    private String TCBNHCMFTPath;
    private String TCBNHCImportPath;
    private String TCBNHCPath;

    private CMNConfig() {
        fillDataToProperty();
    }

    public String getCBSIP() {
        return _CBSIP;
    }

    public int getCBSPort() {
        return _CBSPort;
    }

    public String getNBMQServerIP() {
        return NBMQServerIP;
    }

    public String getNBMQServerPort() {
        return NBMQServerPort;
    }

    public String getNBMQServerChannel() {
        return NBMQServerChannel;
    }

    public String getNBMQServerQueueMgr() {
        return NBMQServerQueueMgr;
    }

    public String getNBMQServerRequestQueue() {
        return NBMQServerRequestQueue;
    }

    public String getNBMQServerReplyQueue() {
        return NBMQServerReplyQueue;
    }

    public String getNBTimeout() {
        return NBTimeout;
    }

    public String getNBMaxThreads() {
        return NBMaxThreads;
    }

    /**
     * 行外ATM增補鈔狀況表使用之FTP Folder
     *
     * @return
     */
    public String getATMCASHFTPFolder() {
        return _ATMCASHFTPFolder;
    }

    /**
     * AAService同時可處理ATM交易的最大Thread數
     *
     * @return
     */
    public int getATMMaxThreads() {
        return _ATMMaxThreads;
    }

    /**
     * ATMGW用來接收ATMMON的queue
     *
     * @return
     */
    public String getAtmmonQueue() {
        return _ATMMON_QUEUE;
    }

    /**
     * 各個優利服務的啟用狀態,0為停用,1為啟用
     *
     * @return
     */
    public int getATMP() {
        return _ATMP;
    }

    /**
     * 各個優利服務的啟用狀態,0為停用,1為啟用
     *
     * @return
     */
    public int getATMPREQ() {
        return _ATMPREQ;
    }

    /**
     * 緊急事件通知機制產出檔名
     *
     * @return
     */
    public String getATMTXSummaryFile() {
        return _ATMTXSummaryFile;
    }

    /**
     * 緊急事件通知機制產出FTP目錄
     *
     * @return
     */
    public String getATMTXSummaryFTPFolder() {
        return _ATMTXSummaryFTPFolder;
    }

    /**
     * 批次平台所用的QUEUE
     *
     * @return
     */
    public String getBatchQueue() {
        return _BATCH_QUEUE;
    }

    /**
     * Batch程式的輸入檔目錄
     *
     * @return
     */
    public String getBatchInputPath() {
        return _BatchInputPath;
    }

    /**
     * Batch程式的Log目錄
     *
     * @return
     */
    public String getBatchLogPath() {
        return _BatchLogPath;
    }

    /**
     * Batch程式的輸出檔目錄
     *
     * @return
     */
    public String getBatchOutputPath() {
        return _BatchOutputPath;
    }

    /**
     * 批次平台呼叫Batch Program root path
     *
     * @return
     */
    public String getBatchServicePath() {
        return _BatchServicePath;
    }

    /**
     * 企業入金FTP主機參數
     *
     * @return
     */
    public String getBDMFTPServer() {
        return _BDMFTPServer;
    }

    /**
     * 通知分行廣播 Service Url
     *
     * @return
     */
    public String getBranchBroadcastUrl() {
        return _BranchBroadcastUrl;
    }

    /**
     * 全國性繳費委託單位檔維護轉檔檔名
     *
     * @return
     */
    public String getBT010010File() {
        return _BT010010File;
    }

    /**
     * 消費扣款特約商店檔維護檔名
     *
     * @return
     */
    public String getBT010020File() {
        return _BT010020File;
    }

    /**
     * 銀聯卡 BIN 檔維護轉檔檔名
     *
     * @return
     */
    public String getBT010090File() {
        return _BT010090File;
    }

    /**
     * 各個優利服務的啟用狀態,0為停用,1為啟用
     *
     * @return
     */
    public int getCARD() {
        return _CARD;
    }

    /**
     * 可同時處理T24 Timeout Rerun的最大Thread數
     *
     * @return
     */
    public int getCBSTimeoutRerunMaxThreads() {
        return _CBSTimeoutRerunMaxThreads;
    }

    /**
     * 信用卡虛擬分行代號
     *
     * @return
     */
    public String getCCardBRNO() {
        return _CCardBRNO;
    }

    /**
     * 信用卡電文存留在MQServer上的間時(1/10秒)
     *
     * @return
     */
    public int getCreditMQExpiry() {
        return _CreditMQExpiry;
    }

    /**
     * 信用卡主機的MQServer Channel
     *
     * @return
     */
    public String getCreditMQServerChannel() {
        return _CreditMQServerChannel;
    }

    /**
     * 信用卡主機的MQServer IP
     *
     * @return
     */
    public String getCreditMQServerIP() {
        return _CreditMQServerIP;
    }

    /**
     * 信用卡主機的MQServer Port
     *
     * @return
     */
    public int getCreditMQServerPort() {
        return _CreditMQServerPort;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerPutQueue01() {
        return _CreditMQServerPutQueue01;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerPutQueue02() {
        return _CreditMQServerPutQueue02;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerPutQueue03() {
        return _CreditMQServerPutQueue03;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerPutQueue05() {
        return _CreditMQServerPutQueue05;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerPutQueue06() {
        return _CreditMQServerPutQueue06;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerPutQueue07() {
        return _CreditMQServerPutQueue07;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerPutQueue09() {
        return _CreditMQServerPutQueue09;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerPutQueue10() {
        return _CreditMQServerPutQueue10;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerPutQueue11() {
        return _CreditMQServerPutQueue11;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerPutQueue15() {
        return _CreditMQServerPutQueue15;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerPutQueue16() {
        return _CreditMQServerPutQueue16;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerPutQueue17() {
        return _CreditMQServerPutQueue17;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerPutQueue20() {
        return _CreditMQServerPutQueue20;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerPutQueue21() {
        return _CreditMQServerPutQueue21;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerPutQueue23() {
        return _CreditMQServerPutQueue23;
    }

    /**
     * 信用卡主機的MQ Queue Manager Name
     *
     * @return
     */
    public String getCreditMQServerQueueMgr() {
        return _CreditMQServerQueueMgr;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerReplyQueue01() {
        return _CreditMQServerReplyQueue01;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerReplyQueue02() {
        return _CreditMQServerReplyQueue02;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerReplyQueue06() {
        return _CreditMQServerReplyQueue06;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerReplyQueue07() {
        return _CreditMQServerReplyQueue07;
    }

    /**
     * 傳送至信用卡主機的Queue
     *
     * @return
     */
    public String getCreditMQServerReplyQueue22() {
        return _CreditMQServerReplyQueue22;
    }

    /**
     * 從信用卡主機接收回應訊息的Queue
     *
     * @return
     */
    public String getCreditMQServerRequestQueue() {
        return _CreditMQServerRequestQueue;
    }

    /**
     * 從信用卡主機接收回應訊息的Queue
     *
     * @return
     */
    public String getCreditMQServerResponseQueue() {
        return _CreditMQServerResponseQueue;
    }

    /**
     * 信用卡主機的Timeout時間
     *
     * @return
     */
    public int getCreditTimeout() {
        return _CreditTimeout;
    }

    /**
     * 客服專線
     *
     * @return
     */
    public String getCSHotline() {
        return _CSHotline;
    }

    /**
     * Unisys Timeout Queue
     *
     * @return
     */
    public String getDeadAtmp() {
        return _DEAD_ATMP;
    }

    /**
     * Credit Timeout Queue
     *
     * @return
     */
    public String getDeadSc() {
        return _DEAD_SC;
    }

    /**
     * ENCLib發生RC95時重試次數
     *
     * @return
     */
    public int getENCLibRetryCount() {
        return _ENCLibRetryCount;
    }

    /**
     * ENCLib發生RC95時重試間隔,單位毫秒
     *
     * @return
     */
    public int getENCLibRetryInterval() {
        return _ENCLibRetryInterval;
    }

    /**
     * 用來做Echo Test的Queue
     *
     * @return
     */
    public String getEchoTestReply() {
        return _EchoTestReply;
    }

    /**
     * EJ FTP主機參數
     *
     * @return
     */
    public String getEJFTPServer() {
        return _EJFTPServer;
    }

    /**
     * EJ來源FTP主機參數
     *
     * @return
     */
    public String getEJSourceFTPServer() {
        return _EJSourceFTPServer;
    }

    /**
     * EMS IP
     *
     * @return
     */
    public String getEMSListenIP() {
        return _EMS_ListenIP;
    }

    /**
     * EMS Port
     *
     * @return
     */
    public int getEMSListenPort() {
        return _EMS_ListenPort;
    }

    /**
     * EMS接收的Queue
     *
     * @return
     */
    public String getEMSQueue() {
        return _EMSQueue;
    }

    /**
     * ENotice FTP主機參數
     *
     * @return
     */
    public String getENoticeFTPServer() {
        return _ENoticeFTPServer;
    }

    /**
     * FCS WebService Url
     *
     * @return
     */
    public String getFCSServiceUrl() {
        return _FCSServiceUrl;
    }

    /**
     * FEDI WebService Url
     *
     * @return
     */
    public String getFEDIServiceUrl() {
        return _FEDIServiceUrl;
    }

    /**
     * FEP Object目錄
     *
     * @return
     */
    public String getFEPBinPath() {
        return _FEPBinPath;
    }

    /**
     * FEP Server的IP
     *
     * @return
     */
    public String getFEPServerIP() {
        return _FEPServerIP;
    }

    /**
     * FEP服務管理用的Queue
     *
     * @return
     */
    public String getFepserviceQueue() {
        return _fepservice_queue;
    }

    /**
     * FISCService同時可處理財金交易的最大Thread數
     *
     * @return
     */
    public int getFISCMaxThreads() {
        return _FISCMaxThreads;
    }

    /**
     * FTP主機參數-香港STATMNT
     *
     * @return
     */
    public String getFTPHKSTATMNT() {
        return _FTPHKSTATMNT;
    }

    /**
     * FTP主機參數-澳門STATMNT
     *
     * @return
     */
    public String getFTPMOSTATMNT() {
        return _FTPMOSTATMNT;
    }

    /**
     * FTP主機參數-香港
     *
     * @return
     */
    public String getFTPServer1() {
        return _FTPServer1;
    }

    /**
     * FTP主機參數-澳門
     *
     * @return
     */
    public String getFTPServer2() {
        return _FTPServer2;
    }

    /**
     * FTP主機參數-台灣
     *
     * @return
     */
    public String getFTPServer3() {
        return _FTPServer3;
    }

    /**
     * FTP主機參數-FCS
     *
     * @return
     */
    public String getFTPServer4() {
        return _FTPServer4;
    }

    /**
     * FTP主機參數-OnDeMand(報表)
     *
     * @return
     */
    public String getFTPServer5() {
        return _FTPServer5;
    }

    /**
     * FTP主機參數-SVCS
     *
     * @return
     */
    public String getFTPServer6() {
        return _FTPServer6;
    }

    /**
     * FTP主機參數-HK OnDeMand(報表)
     *
     * @return
     */
    public String getFTPServer7() {
        return _FTPServer7;
    }

    /**
     * FTP主機參數-MO OnDeMand(報表)
     *
     * @return
     */
    public String getFTPServer8() {
        return _FTPServer8;
    }

    /**
     * FTP主機參數-Old OnDeMand(報表)
     *
     * @return
     */
    public String getFTPServer9() {
        return _FTPServer9;
    }

    /**
     * 豐掌櫃 FTP主機參數
     *
     * @return
     */
    public String getFunCashierFTPServer() {
        return _FunCashierFTPServer;
    }

    /**
     * Garbage Queue
     *
     * @return
     */
    public String getGARBAGE() {
        return _GARBAGE;
    }

    /**
     * 外圍系統呼叫GL的ServerName
     *
     * @return
     */
    public String getGLLinkSrvName() {
        return _GLLinkSrvName;
    }

    /**
     * 帳務系統代號
     *
     * @return
     */
    public String getGLSysCode() {
        return _GLSysCode;
    }

    /**
     * 上傳人員編號
     *
     * @return
     */
    public String getGLUpdUserID() {
        return _GLUpdUserID;
    }

    /**
     * 歷史資料庫EMSQueue
     *
     * @return
     */
    public String getHisEMSQueue() {
        return _HisEMSQueue;
    }

    /**
     * 香港拋帳切換時間
     *
     * @return
     */
    public String getHKCutTime() {
        return _HKCutTime;
    }

    /**
     * 各個優利服務的啟用狀態,0為停用,1為啟用
     *
     * @return
     */
    public int getHKG() {
        return _HKG;
    }

    /**
     * 外圍系統呼叫香港GL的ServerName
     *
     * @return
     */
    public String getHKGLLinkSrvName() {
        return _HKGLLinkSrvName;
    }

    /**
     * 香港帳務系統代號
     *
     * @return
     */
    public String getHKGLSysCode() {
        return _HKGLSysCode;
    }

    /**
     * 香港上傳人員編號
     *
     * @return
     */
    public String getHKGLUpdUserID() {
        return _HKGLUpdUserID;
    }

    /**
     * 香港優利主機IP
     *
     * @return
     */
    public String getHKGUnisysIP() {
        return _HKGUnisysIP;
    }

    /**
     * 連香港優利ATMP的LocalPort(日間測試)
     *
     * @return
     */
    public int getHKGUnisysLocalPort() {
        return _HKGUnisysLocalPort;
    }

    /**
     * 連香港優利ATMP的LocalPort(夜間測試)
     *
     * @return
     */
    public int getHKGUnisysLocalPortN() {
        return _HKGUnisysLocalPortN;
    }

    /**
     * 香港優利主機Port(日間)
     *
     * @return
     */
    public int getHKGUnisysPort() {
        return _HKGUnisysPort;
    }

    /**
     * 香港優利主機Port(夜間)
     *
     * @return
     */
    public int getHKGUnisysPortN() {
        return _HKGUnisysPortN;
    }

    /**
     * HK SMS 客服電話號碼
     *
     * @return
     */
    public String getHKSMSHotLine() {
        return _HKSMSHotLine;
    }

    /**
     * HK SMS Web Service 使用者密碼
     *
     * @return
     */
    public String getHKSMSPassword() {
        return _HKSMSPassword;
    }

    /**
     * HK SMS Web Service Url
     *
     * @return
     */
    public String getHKSMSServiceUrl() {
        return _HKSMSServiceUrl;
    }

    /**
     * HK SMS 簡訊內容1
     *
     * @return
     */
    public String getHKSMSText1() {
        return _HKSMSText1;
    }

    /**
     * HK SMS 簡訊內容2
     *
     * @return
     */
    public String getHKSMSText2() {
        return _HKSMSText2;
    }

    /**
     * HK SMS Web Service 使用者帳號
     *
     * @return
     */
    public String getHKSMSUserName() {
        return _HKSMSUserName;
    }

    /**
     * HK SMS 轉送 Url(網銀Server)
     *
     * @return
     */
    public String getHKTransferSMSUrl() {
        return _HKTransferSMSUrl;
    }

    /**
     * ATM資料檔
     *
     * @return
     */
    public String getImportATMDataFile() {
        return _ImportATMDataFile;
    }

    /**
     * 企業識別卡員工資料檔
     *
     * @return
     */
    public String getImportCOIDFile() {
        return _ImportCOIDFile;
    }

    /**
     * CARD 1001轉出帳號檔
     *
     * @return
     */
    public String getImportEF1001File() {
        return _ImportEF1001File;
    }

    /**
     * CARD 1002轉入帳號檔
     *
     * @return
     */
    public String getImportEF1002File() {
        return _ImportEF1002File;
    }

    /**
     * 員工資料檔
     *
     * @return
     */
    public String getImportFEPUserFile() {
        return _ImportFEPUserFile;
    }

    /**
     * ImportFEPUser產生檔案
     *
     * @return
     */
    public String getImportFEPUserOutput() {
        return _ImportFEPUserOutput;
    }

    /**
     * 預約轉帳期限(單位:月)
     *
     * @return
     */
    public int getIPADueDay() {
        return _IPADueDay;
    }

    /**
     * ATMGW socket keep alive interval的間隔, 單位是秒
     *
     * @return
     */
    public int getKeepAliveInterval() {
        return _KeepAliveInterval;
    }

    /**
     * ATMGW多久發一次keep alive, 時間單位是秒
     *
     * @return
     */
    public int getKeepAliveTime() {
        return _KeepAliveTime;
    }

    /**
     * LateResponse Service 同時可處理財金Late交易的最大Threads
     *
     * @return
     */
    public int getLateResponseMaxThreads() {
        return _LateResponseMaxThreads;
    }

    /**
     * ATMGW Listen socket backlog
     *
     * @return
     */
    public int getListenBacklog() {
        return _ListenBacklog;
    }

    /**
     * 各個優利服務的啟用狀態,0為停用,1為啟用
     *
     * @return
     */
    public int getMAC() {
        return _MAC;
    }

    /**
     * 澳門優利主機IP
     *
     * @return
     */
    public String getMACUnisysIP() {
        return _MACUnisysIP;
    }

    /**
     * 連澳門優利ATMP的LocalPort(日間測試)
     *
     * @return
     */
    public int getMACUnisysLocalPort() {
        return _MACUnisysLocalPort;
    }

    /**
     * 連澳門優利ATMP的LocalPort(夜間測試)
     *
     * @return
     */
    public int getMACUnisysLocalPortN() {
        return _MACUnisysLocalPortN;
    }

    /**
     * 澳門優利主機Port
     *
     * @return
     */
    public int getMACUnisysPort() {
        return _MACUnisysPort;
    }

    /**
     * 澳門優利主機Port(夜間)
     *
     * @return
     */
    public int getMACUnisysPortN() {
        return _MACUnisysPortN;
    }

    /**
     * 寄件者Mail信箱
     *
     * @return
     */
    public String getMailFromAddress() {
        return _MailFromAddress;
    }

    /**
     * Mail Server IP
     *
     * @return
     */
    public String getMailServerIP() {
        return _MailServerIP;
    }

    /**
     * 悠遊製卡回饋檔未回饋通知收件者Mail信箱
     *
     * @return
     */
    public String getMailToAddressEZISAM() {
        return _MailToAddressEZISAM;
    }

    /**
     * 悠遊卡掛失失敗通知群組Mail信箱
     *
     * @return
     */
    public String getMailToAddressEZLost() {
        return _MailToAddressEZLost;
    }

    /**
     * 資安收件者Mail信箱
     *
     * @return
     */
    public String getMailToAddressSEC() {
        return _MailToAddressSEC;
    }

    /**
     * TOGO郵簡密碼函列印通知Mail信箱
     *
     * @return
     */
    public String getMailToAddressTOGO() {
        return _MailToAddressTOGO;
    }

    /**
     * 最大分行數
     *
     * @return
     */
    public String getMaxBrno() {
        return _MaxBrno;
    }

    /**
     * MMAB2B WebService Url
     *
     * @return
     */
    public String getMMAB2BServiceUrl() {
        return _MMAB2BServiceUrl;
    }

    /**
     * 澳門本地他行跨國提款(2410)外幣收費記號
     *
     * @return
     */
    public String getMO205ChargeFG() {
        return _MO205ChargeFG;
    }

    /**
     * 澳門拋帳切換時間
     *
     * @return
     */
    public String getMOCutTime() {
        return _MOCutTime;
    }

    /**
     * 外圍系統呼叫澳門GL的ServerName
     *
     * @return
     */
    public String getMOGLLinkSrvName() {
        return _MOGLLinkSrvName;
    }

    /**
     * 澳門帳務系統代號
     *
     * @return
     */
    public String getMOGLSysCode() {
        return _MOGLSysCode;
    }

    /**
     * 澳門上傳人員編號
     *
     * @return
     */
    public String getMOGLUpdUserID() {
        return _MOGLUpdUserID;
    }

    /**
     * 用來判斷卡管子系統送T24或優利(1:優利, 2:T24)
     *
     * @return
     */
    public int getPhase() {
        return _Phase;
    }

    /**
     * @return
     */
    public String getPRNAllowIP() {
        return _PRNAllowIP;
    }

    /**
     * @return
     */
    public String getPRNCodeLetterIP() {
        return _PRNCodeLetterIP;
    }

    /**
     * @return
     */
    public int getPRNCodeLetterPort() {
        return _PRNCodeLetterPort;
    }

    /**
     * @return
     */
    public int getPRNSocketTimeOut() {
        return _PRNSocketTimeOut;
    }

    /**
     * 優利主機送回給FEP的Queue
     *
     * @return
     */
    public String getRcvAtmp() {
        return _RCV_ATMP;
    }

    /**
     * 信用卡主機送給AA Service的Response Queue
     *
     * @return
     */
    public String getRcvSc() {
        return _RCV_SC;
    }

    /**
     * T24主機送回給FEP的Queue
     *
     * @return
     */
    public String getRcvT24() {
        return _RCV_T24;
    }

    /**
     * 回給優利主機發動交易的Reponse Queue
     *
     * @return
     */
    public String getRepAtmp() {
        return _REP_ATMP;
    }

    /**
     * 報表FTP目錄
     *
     * @return
     */
    public String getReportingFTPFolder() {
        return _ReportingFTPFolder;
    }

    /**
     * 從優利主機發動交易的Queue
     *
     * @return
     */
    public String getReqAtmp() {
        return _REQ_ATMP;
    }

    /**
     * 信用卡主機送給AA Service的Request Queue
     *
     * @return
     */
    public String getReqSc() {
        return _REQ_SC;
    }

    /**
     * ATMService同時可處理信用卡交易的最大Thread數
     *
     * @return
     */
    public int getSCMaxThreads() {
        return _SCMaxThreads;
    }

    /**
     * 永豐證券FTP主機參數
     *
     * @return
     */
    public String getSECFTPServer() {
        return _SECFTPServer;
    }

    /**
     * 整批轉即時可處理交易的最大Thread數
     *
     * @return
     */
    public int getSECMaxThreads() {
        return _SECMaxThreads;
    }

    /**
     * 內部檔案傳送SFTPGroupCode
     *
     * @return
     */
    public String getSFTPGroupCode() {
        return _SFTPGroupCode;
    }

    /**
     * 內部檔案傳送SFTPSender
     *
     * @return
     */
    public String getSFTPSender() {
        return _SFTPSender;
    }

    /**
     * 內部檔案傳送SFTP網址
     *
     * @return
     */
    public String getSFTPUrl() {
        return _SFTPUrl;
    }

    /**
     * AppMonitor暫停發送通知
     *
     * @return
     */
    public String getStopNotification() {
        return _StopNotification;
    }

    /**
     * ENCLib呼叫Suip的IP及Port,最少一組,最多組,中間用分號區隔
     *
     * @return
     */
    public String getSuipAddress() {
        return _SuipAddress;
    }

    /**
     * ENCLib呼叫Suip的逾時時間,單位為秒
     *
     * @return
     */
    public int getSuipTimeout() {
        return _SuipTimeout;
    }

    /**
     * T24 FTPServer資料夾
     *
     * @return
     */
    public String getT24FTPDirName() {
        return _T24FTPDirName;
    }

    /**
     * T24帳號每分鐘超過N筆之批次的寄件者Mail信箱
     *
     * @return
     */
    public String getT24MailFromAddress() {
        return _T24MailFromAddress;
    }

    /**
     * T24帳號每分鐘超過N筆之批次的收件者Mail信箱
     *
     * @return
     */
    public String getT24MailToAddress() {

        return _T24MailToAddress;
    }

    /**
     * T24 Gateway同時可處理交易的最大Thread數
     *
     * @return
     */
    public int getT24MaxThreads() {
        return _T24MaxThreads;
    }

    /**
     * T24主機的MQServer Channel
     *
     * @return
     */
    public String getT24MQServerChannel() {
        return _T24MQServerChannel;
    }

    /**
     * T24主機的MQServer IP
     *
     * @return
     */
    public String getT24MQServerIP() {
        return _T24MQServerIP;
    }

    /**
     * T24主機的MQServer Port
     *
     * @return
     */
    public int getT24MQServerPort() {
        return _T24MQServerPort;
    }

    /**
     * T24主機的MQ Queue Manager Name
     *
     * @return
     */
    public String getT24MQServerQueueMgr() {
        return _T24MQServerQueueMgr;
    }

    /**
     * 從T24主機接收回應訊息的Queue
     *
     * @return
     */
    public String getT24MQServerReplyQueue() {
        return _T24MQServerReplyQueue;
    }

    /**
     * 從T24主機接收回應訊息的Queue
     *
     * @return
     */
    public String getT24MQServerRequestQueue() {
        return _T24MQServerRequestQueue;
    }

    /**
     * T24 MQ的Timeout時間
     *
     * @return
     */
    public int getT24MQTimeout() {
        return _T24MQTimeout;
    }

    /**
     * T24 WebService Url
     *
     * @return
     */
    public String getT24ServiceUrl() {
        return _T24ServiceUrl;
    }

    /**
     * T24的Timeout時間
     *
     * @return
     */
    public int getT24Timeout() {
        return _T24Timeout;
    }

    /**
     * 放至REQ的message多?沒收走自動移至DEAD Queue
     *
     * @return
     */
    public int getTimeToDeadQueue() {
        return _TimeToDeadQueue;
    }

    /**
     * FEP送給優利主機的Queue
     *
     * @return
     */
    public String getToAtmp() {
        return _TO_ATMP;
    }

    /**
     * FEP送給優利主機的Queue(RM)
     *
     * @return
     */
    public String getToAtmpRm() {
        return _TO_ATMP_RM;
    }

    /**
     * AA Service送給信用卡主機的Queue
     *
     * @return
     */
    public String getToSc() {
        return _TO_SC;
    }

    /**
     * FEP送給T24主機的Queue
     *
     * @return
     */
    public String getToT24() {
        return _TO_T24;
    }

    /**
     * AA Service送回WEBATMGW的Queue
     *
     * @return
     */
    public String getToWebatm() {
        return _TO_WEBATM;
    }

    /**
     * 台灣優利ATMP IP
     *
     * @return
     */
    public String getUnisysATMPIP() {
        return _UnisysATMPIP;
    }

    /**
     * 連優利ATMP的LocalPort
     *
     * @return
     */
    public int getUnisysATMPLocalPort() {
        return _UnisysATMPLocalPort;
    }

    /**
     * 優利ATMP Port
     *
     * @return
     */
    public int getUnisysATMPPort() {
        return _UnisysATMPPort;
    }

    /**
     * 接收優利ATMP發動交易Local Port
     *
     * @return
     */
    public int getUnisysATMPRequestLocalPort() {
        return _UnisysATMPRequestLocalPort;
    }

    /**
     * 接收優利ATMP發動交易 Port
     *
     * @return
     */
    public int getUnisysATMPRequestPort() {
        return _UnisysATMPRequestPort;
    }

    /**
     * 連優利ATMP的LocalPort
     *
     * @return
     */
    public int getUnisysCardLocalPort() {
        return _UnisysCardLocalPort;
    }

    /**
     * 接收優利Card發動交易Local Port
     *
     * @return
     */
    public int getUnisysCardRequestLocalPort() {
        return _UnisysCardRequestLocalPort;
    }

    /**
     * 接收優利Card發動交易Remote Port
     *
     * @return
     */
    public int getUnisysCardRequestPort() {
        return _UnisysCardRequestPort;
    }

    /**
     * 優利主機(Card)的Timeout時間
     *
     * @return
     */
    public int getUnisysCardTimeout() {
        return _UnisysCardTimeout;
    }

    /**
     * 是否認允許ATMP服務
     *
     * @return
     */
    public boolean getUnisysEnableATMP() {
        return _UnisysEnableATMP;
    }

    /**
     * 是否認允許ATMP Request服務
     *
     * @return
     */
    public boolean getUnisysEnableATMPRequest() {
        return _UnisysEnableATMPRequest;
    }

    /**
     * 是否認允許Card服務
     *
     * @return
     */
    public boolean getUnisysEnableCard() {
        return _UnisysEnableCard;
    }

    /**
     * 是否認允許Card Request服務
     *
     * @return
     */
    public boolean getUnisysEnableCardRequest() {
        return _UnisysEnableCardRequest;
    }

    /**
     * 是否認允許HKG服務
     *
     * @return
     */
    public boolean getUnisysEnableHKG() {
        return _UnisysEnableHKG;
    }

    /**
     * 是否認允許MAC服務
     *
     * @return
     */
    public boolean getUnisysEnableMAC() {
        return _UnisysEnableMAC;
    }

    /**
     * 是否認允許RM服務
     *
     * @return
     */
    public boolean getUnisysEnableRM() {
        return _UnisysEnableRM;
    }

    /**
     * 是否認允許優利主機發動RM服務
     *
     * @return
     */
    public boolean getUnisysEnableRMRequest() {
        return _UnisysEnableRMRequest;
    }

    /**
     * UnisysGW同時可處理信用卡交易的最大Thread數
     *
     * @return
     */
    public int getUnisysGWMaxThreads() {
        return _UnisysGWMaxThreads;
    }

    /**
     * 可同時處理Unisys發動交易的最大Thread數
     *
     * @return
     */
    public int getUnisysRequestMaxThreads() {
        return _UnisysRequestMaxThreads;
    }

    /**
     * 處理RM交易的優利主機 IP
     *
     * @return
     */
    public String getUnisysRMIP() {
        return _UnisysRMIP;
    }

    /**
     * 處理RM交易的優利主機Local Port
     *
     * @return
     */
    public int getUnisysRMLocalPort() {
        return _UnisysRMLocalPort;
    }

    /**
     * 處理RM交易的優利主機Port
     *
     * @return
     */
    public int getUnisysRMPort() {
        return _UnisysRMPort;
    }

    /**
     * 處理優利主機主動發動交易的Local Port
     *
     * @return
     */
    public int getUnisysRMRequestLocalPort() {
        return _UnisysRMRequestLocalPort;
    }

    /**
     * 優利主機的Timeout時間
     *
     * @return
     */
    public int getUnisysTimeout() {
        return _UnisysTimeout;
    }

    /**
     * 虛擬帳號前五碼for繳信用卡款
     *
     * @return
     */
    public String getVirtualActno() {
        return _VirtualActno;
    }

    /**
     * WebATM 的 IP Address
     *
     * @return
     */
    public String getWebATMIP() {
        return _WebATMIP;
    }

    /**
     * 總行代號
     *
     * @return
     */
    public String getHeadOffice() {
        return _HeadOffice;
    }

    /**
     * 磁條密碼錯誤可嘗試次數
     *
     * @return
     */
    public int getFiscPassRetryCnt() {
        return _FiscPassRetryCnt;
    }

    /**
     * IBMB DES BankID
     *
     * @return
     */
    public String get_NBDesBankID() {
        return _NBDesBankID;
    }

    /**
     * 訊息通知平台WebAPI URL
     *
     * @return
     */
    public String getNotifyApiUrl() {
        return _NotifyApiUrl;
    }

    public String getGpgPassphrase() {
        return _GPG_Passphrase;
    }

    public String getGpgPath() {
        return _GPG_Path;
    }

    public String getGpgPrivateKeyId() {
        return _GPG_PrivateKeyId;
    }

    public String getGpgPublicKeyId() {
        return _GPG_PublicKeyId;
    }

    public String getIMSHostName() {
        return _IMSHostName;
    }

    public int getIMSPort() {
        return _IMSPort;
    }

    public String getIMSClientId() {
        return _IMSClientId;
    }

    public int getCBSTimeout() {
        return _CBSTimeout;
    }

    public String getIMSDatastoreName() {
        return _IMSDatastoreName;
    }

    public String getSMSDomain() {
        return _SMSDomain;
    }

    public String getSMSUserName() {
        return _SMSUserName;
    }

    public String getSMSSscode() {
        return _SMSSscode;
    }

    public String getIsCBSTest() {
        return _isCBSTest;
    }

    public String getMobileQueryUrl() {
        return _MobileQueryUrl;
    }

    public String getTrinAccountQueryUrl() {
        return _TrinAccountQueryUrl;
    }

    public String getEnableAutoRestart() {
        return _EnableAutoRestart;
    }

    public String getSFTPServer() {
        return SFTPServer;
    }

    public String getSFTPUCode() {
        return SFTPUCode;
    }

    public String getSFTPSCode() {
        return SFTPSCode;
    }

    public String getDiskValue() { return DiskValue; }
    public String getTCBNHCMFTPath() {
        return TCBNHCMFTPath;
    }
    public String getTCBNHCImportPath() {
        return TCBNHCImportPath;
    }
    public String getTCBNHCPath() {
        return TCBNHCPath;
    }
    public static CMNConfig getInstance() {
        return _instance;
    }

    private void fillDataToProperty() {
        List<Sysconf> sysconfList = FEPCache.getSysconfList(SubSystemNo);
        String sysconfValue = null;
        for (Sysconf sysconf : sysconfList) {
            sysconfValue = sysconf.getSysconfValue();
            switch (sysconf.getSysconfName()) {
                case "ATMCASHFTPFolder":
                    _ATMCASHFTPFolder = DbHelper.toString(sysconfValue);
                    break;
                case "ATMMaxThreads":
                    _ATMMaxThreads = DbHelper.toInteger(sysconfValue);
                    break;
                case "ATMMON_QUEUE":
                    _ATMMON_QUEUE = DbHelper.toString(sysconfValue);
                    break;
                case "ATMP":
                    _ATMP = DbHelper.toInteger(sysconfValue);
                    break;
                case "ATMPREQ":
                    _ATMPREQ = DbHelper.toInteger(sysconfValue);
                    break;
                case "ATMTXSummaryFile":
                    _ATMTXSummaryFile = DbHelper.toString(sysconfValue);
                    break;
                case "ATMTXSummaryFTPFolder":
                    _ATMTXSummaryFTPFolder = DbHelper.toString(sysconfValue);
                    break;
                case "BATCH_QUEUE":
                    _BATCH_QUEUE = DbHelper.toString(sysconfValue);
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
                case "BDMFTPServer":
                    _BDMFTPServer = DbHelper.toString(sysconfValue);
                    break;
                case "BranchBroadcastUrl":
                    _BranchBroadcastUrl = DbHelper.toString(sysconfValue);
                    break;
                case "BT010010File":
                    _BT010010File = DbHelper.toString(sysconfValue);
                    break;
                case "BT010020File":
                    _BT010020File = DbHelper.toString(sysconfValue);
                    break;
                case "BT010090File":
                    _BT010090File = DbHelper.toString(sysconfValue);
                    break;
                case "CARD":
                    _CARD = DbHelper.toInteger(sysconfValue);
                    break;
                case "CBSTimeoutRerunMaxThreads":
                    _CBSTimeoutRerunMaxThreads = DbHelper.toInteger(sysconfValue);
                    break;
                case "CCardBRNO":
                    _CCardBRNO = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQExpiry":
                    _CreditMQExpiry = DbHelper.toInteger(sysconfValue);
                    break;
                case "CreditMQServerChannel":
                    _CreditMQServerChannel = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerIP":
                    _CreditMQServerIP = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerPort":
                    _CreditMQServerPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "CreditMQServerPutQueue01":
                    _CreditMQServerPutQueue01 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerPutQueue02":
                    _CreditMQServerPutQueue02 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerPutQueue03":
                    _CreditMQServerPutQueue03 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerPutQueue05":
                    _CreditMQServerPutQueue05 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerPutQueue06":
                    _CreditMQServerPutQueue06 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerPutQueue07":
                    _CreditMQServerPutQueue07 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerPutQueue09":
                    _CreditMQServerPutQueue09 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerPutQueue10":
                    _CreditMQServerPutQueue10 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerPutQueue11":
                    _CreditMQServerPutQueue11 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerPutQueue15":
                    _CreditMQServerPutQueue15 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerPutQueue16":
                    _CreditMQServerPutQueue16 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerPutQueue17":
                    _CreditMQServerPutQueue17 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerPutQueue20":
                    _CreditMQServerPutQueue20 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerPutQueue21":
                    _CreditMQServerPutQueue21 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerPutQueue23":
                    _CreditMQServerPutQueue23 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerQueueMgr":
                    _CreditMQServerQueueMgr = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerReplyQueue01":
                    _CreditMQServerReplyQueue01 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerReplyQueue02":
                    _CreditMQServerReplyQueue02 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerReplyQueue06":
                    _CreditMQServerReplyQueue06 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerReplyQueue07":
                    _CreditMQServerReplyQueue07 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerReplyQueue22":
                    _CreditMQServerReplyQueue22 = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerRequestQueue":
                    _CreditMQServerRequestQueue = DbHelper.toString(sysconfValue);
                    break;
                case "CreditMQServerResponseQueue":
                    _CreditMQServerResponseQueue = DbHelper.toString(sysconfValue);
                    break;
                case "CreditTimeout":
                    _CreditTimeout = DbHelper.toInteger(sysconfValue);
                    break;
                case "CSHotline":
                    _CSHotline = DbHelper.toString(sysconfValue);
                    break;
                case "DEAD_ATMP":
                    _DEAD_ATMP = DbHelper.toString(sysconfValue);
                    break;
                case "DEAD_SC":
                    _DEAD_SC = DbHelper.toString(sysconfValue);
                    break;
                case "ENCLibRetryCount":
                    _ENCLibRetryCount = DbHelper.toInteger(sysconfValue);
                    break;
                case "ENCLibRetryInterval":
                    _ENCLibRetryInterval = DbHelper.toInteger(sysconfValue);
                    break;
                case "EchoTestReply":
                    _EchoTestReply = DbHelper.toString(sysconfValue);
                    break;
                case "EJFTPServer":
                    _EJFTPServer = DbHelper.toString(sysconfValue);
                    break;
                case "EJSourceFTPServer":
                    _EJSourceFTPServer = DbHelper.toString(sysconfValue);
                    break;
                case "EMS_ListenIP":
                    _EMS_ListenIP = DbHelper.toString(sysconfValue);
                    break;
                case "EMS_ListenPort":
                    _EMS_ListenPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "EMSQueue":
                    _EMSQueue = DbHelper.toString(sysconfValue);
                    break;
                case "ENoticeFTPServer":
                    _ENoticeFTPServer = DbHelper.toString(sysconfValue);
                    break;
                case "FCSServiceUrl":
                    _FCSServiceUrl = DbHelper.toString(sysconfValue);
                    break;
                case "FEDIServiceUrl":
                    _FEDIServiceUrl = DbHelper.toString(sysconfValue);
                    break;
                case "FEPBinPath":
                    _FEPBinPath = DbHelper.toString(sysconfValue);
                    break;
                case "FEPServerIP":
                    _FEPServerIP = DbHelper.toString(sysconfValue);
                    break;
                case "fepservice_queue":
                    _fepservice_queue = DbHelper.toString(sysconfValue);
                    break;
                case "FISCMaxThreads":
                    _FISCMaxThreads = DbHelper.toInteger(sysconfValue);
                    break;
                case "FTPHKSTATMNT":
                    _FTPHKSTATMNT = DbHelper.toString(sysconfValue);
                    break;
                case "FTPMOSTATMNT":
                    _FTPMOSTATMNT = DbHelper.toString(sysconfValue);
                    break;
                case "FTPServer1":
                    _FTPServer1 = DbHelper.toString(sysconfValue);
                    break;
                case "FTPServer2":
                    _FTPServer2 = DbHelper.toString(sysconfValue);
                    break;
                case "FTPServer3":
                    _FTPServer3 = DbHelper.toString(sysconfValue);
                    break;
                case "FTPServer4":
                    _FTPServer4 = DbHelper.toString(sysconfValue);
                    break;
                case "FTPServer5":
                    _FTPServer5 = DbHelper.toString(sysconfValue);
                    break;
                case "FTPServer6":
                    _FTPServer6 = DbHelper.toString(sysconfValue);
                    break;
                case "FTPServer7":
                    _FTPServer7 = DbHelper.toString(sysconfValue);
                    break;
                case "FTPServer8":
                    _FTPServer8 = DbHelper.toString(sysconfValue);
                    break;
                case "FTPServer9":
                    _FTPServer9 = DbHelper.toString(sysconfValue);
                    break;
                case "FunCashierFTPServer":
                    _FunCashierFTPServer = DbHelper.toString(sysconfValue);
                    break;
                case "GARBAGE":
                    _GARBAGE = DbHelper.toString(sysconfValue);
                    break;
                case "GLLinkSrvName":
                    _GLLinkSrvName = DbHelper.toString(sysconfValue);
                    break;
                case "GLSysCode":
                    _GLSysCode = DbHelper.toString(sysconfValue);
                    break;
                case "GLUpdUserID":
                    _GLUpdUserID = DbHelper.toString(sysconfValue);
                    break;
                case "HisEMSQueue":
                    _HisEMSQueue = DbHelper.toString(sysconfValue);
                    break;
                case "HKCutTime":
                    _HKCutTime = DbHelper.toString(sysconfValue);
                    break;
                case "HKG":
                    _HKG = DbHelper.toInteger(sysconfValue);
                    break;
                case "HKGLLinkSrvName":
                    _HKGLLinkSrvName = DbHelper.toString(sysconfValue);
                    break;
                case "HKGLSysCode":
                    _HKGLSysCode = DbHelper.toString(sysconfValue);
                    break;
                case "HKGLUpdUserID":
                    _HKGLUpdUserID = DbHelper.toString(sysconfValue);
                    break;
                case "HKGUnisysIP":
                    _HKGUnisysIP = DbHelper.toString(sysconfValue);
                    break;
                case "HKGUnisysLocalPort":
                    _HKGUnisysLocalPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "HKGUnisysLocalPortN":
                    _HKGUnisysLocalPortN = DbHelper.toInteger(sysconfValue);
                    break;
                case "HKGUnisysPort":
                    _HKGUnisysPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "HKGUnisysPortN":
                    _HKGUnisysPortN = DbHelper.toInteger(sysconfValue);
                    break;
                case "HKSMSHotLine":
                    _HKSMSHotLine = DbHelper.toString(sysconfValue);
                    break;
                case "HKSMSPassword":
                    _HKSMSPassword = DbHelper.toString(sysconfValue);
                    break;
                case "HKSMSServiceUrl":
                    _HKSMSServiceUrl = DbHelper.toString(sysconfValue);
                    break;
                case "HKSMSText1":
                    _HKSMSText1 = DbHelper.toString(sysconfValue);
                    break;
                case "HKSMSText2":
                    _HKSMSText2 = DbHelper.toString(sysconfValue);
                    break;
                case "HKSMSUserName":
                    _HKSMSUserName = DbHelper.toString(sysconfValue);
                    break;
                case "HKTransferSMSUrl":
                    _HKTransferSMSUrl = DbHelper.toString(sysconfValue);
                    break;
                case "ImportATMDataFile":
                    _ImportATMDataFile = DbHelper.toString(sysconfValue);
                    break;
                case "ImportCOIDFile":
                    _ImportCOIDFile = DbHelper.toString(sysconfValue);
                    break;
                case "ImportEF1001File":
                    _ImportEF1001File = DbHelper.toString(sysconfValue);
                    break;
                case "ImportEF1002File":
                    _ImportEF1002File = DbHelper.toString(sysconfValue);
                    break;
                case "ImportFEPUserFile":
                    _ImportFEPUserFile = DbHelper.toString(sysconfValue);
                    break;
                case "ImportFEPUserOutput":
                    _ImportFEPUserOutput = DbHelper.toString(sysconfValue);
                    break;
                case "IPADueDay":
                    _IPADueDay = DbHelper.toInteger(sysconfValue);
                    break;
                case "KeepAliveInterval":
                    _KeepAliveInterval = DbHelper.toInteger(sysconfValue);
                    break;
                case "KeepAliveTime":
                    _KeepAliveTime = DbHelper.toInteger(sysconfValue);
                    break;
                case "LateResponseMaxThreads":
                    _LateResponseMaxThreads = DbHelper.toInteger(sysconfValue);
                    break;
                case "ListenBacklog":
                    _ListenBacklog = DbHelper.toInteger(sysconfValue);
                    break;
                case "MAC":
                    _MAC = DbHelper.toInteger(sysconfValue);
                    break;
                case "MACUnisysIP":
                    _MACUnisysIP = DbHelper.toString(sysconfValue);
                    break;
                case "MACUnisysLocalPort":
                    _MACUnisysLocalPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "MACUnisysLocalPortN":
                    _MACUnisysLocalPortN = DbHelper.toInteger(sysconfValue);
                    break;
                case "MACUnisysPort":
                    _MACUnisysPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "MACUnisysPortN":
                    _MACUnisysPortN = DbHelper.toInteger(sysconfValue);
                    break;
                case "MailFromAddress":
                    _MailFromAddress = DbHelper.toString(sysconfValue);
                    break;
                case "MailServerIP":
                    _MailServerIP = DbHelper.toString(sysconfValue);
                    break;
                case "MailToAddressEZISAM":
                    _MailToAddressEZISAM = DbHelper.toString(sysconfValue);
                    break;
                case "MailToAddressEZLost":
                    _MailToAddressEZLost = DbHelper.toString(sysconfValue);
                    break;
                case "MailToAddressSEC":
                    _MailToAddressSEC = DbHelper.toString(sysconfValue);
                    break;
                case "MailToAddressTOGO":
                    _MailToAddressTOGO = DbHelper.toString(sysconfValue);
                    break;
                case "MaxBrno":
                    _MaxBrno = DbHelper.toString(sysconfValue);
                    break;
                case "MMAB2BServiceUrl":
                    _MMAB2BServiceUrl = DbHelper.toString(sysconfValue);
                    break;
                case "MO205ChargeFG":
                    _MO205ChargeFG = DbHelper.toString(sysconfValue);
                    break;
                case "MOCutTime":
                    _MOCutTime = DbHelper.toString(sysconfValue);
                    break;
                case "MOGLLinkSrvName":
                    _MOGLLinkSrvName = DbHelper.toString(sysconfValue);
                    break;
                case "MOGLSysCode":
                    _MOGLSysCode = DbHelper.toString(sysconfValue);
                    break;
                case "MOGLUpdUserID":
                    _MOGLUpdUserID = DbHelper.toString(sysconfValue);
                    break;
                case "Phase":
                    _Phase = DbHelper.toInteger(sysconfValue);
                    break;
                case "PRNAllowIP":
                    _PRNAllowIP = DbHelper.toString(sysconfValue);
                    break;
                case "PRNCodeLetterIP":
                    _PRNCodeLetterIP = DbHelper.toString(sysconfValue);
                    break;
                case "PRNCodeLetterPort":
                    _PRNCodeLetterPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "PRNSocketTimeOut":
                    _PRNSocketTimeOut = DbHelper.toInteger(sysconfValue);
                    break;
                case "RCV_ATMP":
                    _RCV_ATMP = DbHelper.toString(sysconfValue);
                    break;
                case "RCV_SC":
                    _RCV_SC = DbHelper.toString(sysconfValue);
                    break;
                case "RCV_T24":
                    _RCV_T24 = DbHelper.toString(sysconfValue);
                    break;
                case "REP_ATMP":
                    _REP_ATMP = DbHelper.toString(sysconfValue);
                    break;
                case "ReportingFTPFolder":
                    _ReportingFTPFolder = DbHelper.toString(sysconfValue);
                    break;
                case "REQ_ATMP":
                    _REQ_ATMP = DbHelper.toString(sysconfValue);
                    break;
                case "REQ_SC":
                    _REQ_SC = DbHelper.toString(sysconfValue);
                    break;
                case "SCMaxThreads":
                    _SCMaxThreads = DbHelper.toInteger(sysconfValue);
                    break;
                case "SECFTPServer":
                    _SECFTPServer = DbHelper.toString(sysconfValue);
                    break;
                case "SECMaxThreads":
                    _SECMaxThreads = DbHelper.toInteger(sysconfValue);
                    break;
                case "SFTPGroupCode":
                    _SFTPGroupCode = DbHelper.toString(sysconfValue);
                    break;
                case "SFTPSender":
                    _SFTPSender = DbHelper.toString(sysconfValue);
                    break;
                case "SFTPUrl":
                    _SFTPUrl = DbHelper.toString(sysconfValue);
                    break;
                case "StopNotification":
                    _StopNotification = DbHelper.toString(sysconfValue);
                    break;
                case "SuipAddress":
                    _SuipAddress = DbHelper.toString(sysconfValue);
                    break;
                case "SuipTimeout":
                    _SuipTimeout = DbHelper.toInteger(sysconfValue);
                    break;
                case "T24FTPDirName":
                    _T24FTPDirName = DbHelper.toString(sysconfValue);
                    break;
                case "T24MailFromAddress":
                    _T24MailFromAddress = DbHelper.toString(sysconfValue);
                    break;
                case "T24MailToAddress":
                    _T24MailToAddress = DbHelper.toString(sysconfValue);
                    break;
                case "T24MaxThreads":
                    _T24MaxThreads = DbHelper.toInteger(sysconfValue);
                    break;
                case "T24MQServerChannel":
                    _T24MQServerChannel = DbHelper.toString(sysconfValue);
                    break;
                case "T24MQServerIP":
                    _T24MQServerIP = DbHelper.toString(sysconfValue);
                    break;
                case "T24MQServerPort":
                    _T24MQServerPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "T24MQServerQueueMgr":
                    _T24MQServerQueueMgr = DbHelper.toString(sysconfValue);
                    break;
                case "T24MQServerReplyQueue":
                    _T24MQServerReplyQueue = DbHelper.toString(sysconfValue);
                    break;
                case "T24MQServerRequestQueue":
                    _T24MQServerRequestQueue = DbHelper.toString(sysconfValue);
                    break;
                case "T24MQTimeout":
                    _T24MQTimeout = DbHelper.toInteger(sysconfValue);
                    break;
                case "T24ServiceUrl":
                    _T24ServiceUrl = DbHelper.toString(sysconfValue);
                    break;
                case "T24Timeout":
                    _T24Timeout = DbHelper.toInteger(sysconfValue);
                    break;
                case "TimeToDeadQueue":
                    _TimeToDeadQueue = DbHelper.toInteger(sysconfValue);
                    break;
                case "TO_ATMP":
                    _TO_ATMP = DbHelper.toString(sysconfValue);
                    break;
                case "TO_ATMP_RM":
                    _TO_ATMP_RM = DbHelper.toString(sysconfValue);
                    break;
                case "TO_SC":
                    _TO_SC = DbHelper.toString(sysconfValue);
                    break;
                case "TO_T24":
                    _TO_T24 = DbHelper.toString(sysconfValue);
                    break;
                case "TO_WEBATM":
                    _TO_WEBATM = DbHelper.toString(sysconfValue);
                    break;
                case "UnisysATMPIP":
                    _UnisysATMPIP = DbHelper.toString(sysconfValue);
                    break;
                case "UnisysATMPLocalPort":
                    _UnisysATMPLocalPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "UnisysATMPPort":
                    _UnisysATMPPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "UnisysATMPRequestLocalPort":
                    _UnisysATMPRequestLocalPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "UnisysATMPRequestPort":
                    _UnisysATMPRequestPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "UnisysCardLocalPort":
                    _UnisysCardLocalPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "UnisysCardRequestLocalPort":
                    _UnisysCardRequestLocalPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "UnisysCardRequestPort":
                    _UnisysCardRequestPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "UnisysCardTimeout":
                    _UnisysCardTimeout = DbHelper.toInteger(sysconfValue);
                    break;
                case "UnisysEnableATMP":
                    _UnisysEnableATMP = DbHelper.toBoolean(sysconfValue);
                    break;
                case "UnisysEnableATMPRequest":
                    _UnisysEnableATMPRequest = DbHelper.toBoolean(sysconfValue);
                    break;
                case "UnisysEnableCard":
                    _UnisysEnableCard = DbHelper.toBoolean(sysconfValue);
                    break;
                case "UnisysEnableCardRequest":
                    _UnisysEnableCardRequest = DbHelper.toBoolean(sysconfValue);
                    break;
                case "UnisysEnableHKG":
                    _UnisysEnableHKG = DbHelper.toBoolean(sysconfValue);
                    break;
                case "UnisysEnableMAC":
                    _UnisysEnableMAC = DbHelper.toBoolean(sysconfValue);
                    break;
                case "UnisysEnableRM":
                    _UnisysEnableRM = DbHelper.toBoolean(sysconfValue);
                    break;
                case "UnisysEnableRMRequest":
                    _UnisysEnableRMRequest = DbHelper.toBoolean(sysconfValue);
                    break;
                case "UnisysGWMaxThreads":
                    _UnisysGWMaxThreads = DbHelper.toInteger(sysconfValue);
                    break;
                case "UnisysRequestMaxThreads":
                    _UnisysRequestMaxThreads = DbHelper.toInteger(sysconfValue);
                    break;
                case "UnisysRMIP":
                    _UnisysRMIP = DbHelper.toString(sysconfValue);
                    break;
                case "UnisysRMLocalPort":
                    _UnisysRMLocalPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "UnisysRMPort":
                    _UnisysRMPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "UnisysRMRequestLocalPort":
                    _UnisysRMRequestLocalPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "UnisysTimeout":
                    _UnisysTimeout = DbHelper.toInteger(sysconfValue);
                    break;
                case "VirtualActno":
                    _VirtualActno = DbHelper.toString(sysconfValue);
                    break;
                case "WebATMIP":
                    _WebATMIP = DbHelper.toString(sysconfValue);
                    break;
                case "HeadOffice":
                    _HeadOffice = DbHelper.toString(sysconfValue);
                    break;
                case "FiscPassRetryCnt":
                    _FiscPassRetryCnt = DbHelper.toInteger(sysconfValue);
                    break;
                case "NBDesBankID":
                    _NBDesBankID = DbHelper.toString(sysconfValue);
                    break;
                case "NotifyApiUrl":
                    _NotifyApiUrl = DbHelper.toString(sysconfValue);
                    break;
                case "CBSIP":
                    _CBSIP = DbHelper.toString(sysconfValue);
                    break;
                case "CBSPort":
                    _CBSPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "IMSHostName":
                    _IMSHostName = DbHelper.toString(sysconfValue);
                    break;
                case "IMSPort":
                    _IMSPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "IMSClientId":
                    _IMSClientId = DbHelper.toString(sysconfValue);
                    break;
                case "CBSTimeout":
                    _CBSTimeout = DbHelper.toInteger(sysconfValue);
                    break;
                case "IMSDatastoreName":
                    _IMSDatastoreName = DbHelper.toString(sysconfValue);
                    break;
                case "SMSDomain":
                    _SMSDomain = DbHelper.toString(sysconfValue);
                    break;
                case "SMSUserName":
                    _SMSUserName = DbHelper.toString(sysconfValue);
                    break;
                case "SMSSscode":
                case "SMSPassword":
                    _SMSSscode = DbHelper.toString(sysconfValue);
                    break;
                case "IsCBSTest":
                    _isCBSTest = DbHelper.toString(sysconfValue);
                    break;
                case "MobileQueryUrl":
                    _MobileQueryUrl = DbHelper.toString(sysconfValue);
                    break;
                case "TrinAccountQueryUrl":
                    _TrinAccountQueryUrl = DbHelper.toString(sysconfValue);
                    break;
                case "SFTPServer":
                    SFTPServer = DbHelper.toString(sysconfValue);
                    break;
                case "SFTPUCode":
                    SFTPUCode = DbHelper.toString(sysconfValue);
                    break;
                case "NBMQServerIP":
                    NBMQServerIP = DbHelper.toString(sysconfValue);
                    break;
                case "NBMQServerPort":
                    NBMQServerPort = DbHelper.toString(sysconfValue);
                    break;
                case "NBMQServerChannel":
                    NBMQServerChannel = DbHelper.toString(sysconfValue);
                    break;
                case "NBMQServerQueueMgr":
                    NBMQServerQueueMgr = DbHelper.toString(sysconfValue);
                    break;
                case "NBMQServerRequestQueue":
                    NBMQServerRequestQueue = DbHelper.toString(sysconfValue);
                    break;
                case "NBMQServerReplyQueue":
                    NBMQServerReplyQueue = DbHelper.toString(sysconfValue);
                    break;
                case "NBTimeout":
                    NBTimeout = DbHelper.toString(sysconfValue);
                    break;
                case "NBMaxThreads":
                    NBMaxThreads = DbHelper.toString(sysconfValue);
                    break;
                case "EnableAutoRestart":
                    _EnableAutoRestart = DbHelper.toString(sysconfValue);
                    break;
                case "DiskValue":
                    DiskValue = DbHelper.toString(sysconfValue);
                    break;
                case "TCBNHCMFTPath":
                    TCBNHCMFTPath = DbHelper.toString(sysconfValue);
                    break;
                case "TCBNHCImportPath":
                    TCBNHCImportPath=DbHelper.toString(sysconfValue);
                    break;
                case "TCBNHCPath":
                    TCBNHCPath=DbHelper.toString(sysconfValue);
                    break;
            }
        }
    }
}
