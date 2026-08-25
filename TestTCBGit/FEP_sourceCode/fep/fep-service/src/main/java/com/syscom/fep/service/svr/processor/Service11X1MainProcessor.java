package com.syscom.fep.service.svr.processor;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.dao.RmoutDao;
import com.syscom.fep.mybatis.dao.RmouttDao;
import com.syscom.fep.mybatis.ext.mapper.*;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.Rmfiscout1Mapper;
import com.syscom.fep.mybatis.mapper.RmoutsnoMapper;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.mybatis.util.SpCaller;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.FISCAdapter;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.server.common.handler.FISCHandler;
import com.syscom.fep.service.svr.SvrProcessor;
import com.syscom.fep.service.svr.restful.InQueue11X1Controller;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.RMOUTStatus;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_CLR;
import com.syscom.fep.vo.text.fisc.FISC_RM;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.syscom.fep.vo.constant.FEPTxnMessageFlow.FISC_Request;
import static com.syscom.fep.vo.constant.NormalRC.FISC_OK;

@StackTracePointCut(caller = SvrConst.SVR_11X1)
public class Service11X1MainProcessor extends SvrProcessor {
    private static final String ProgramName = Service11X1MainProcessor.class.getSimpleName() + ".";
    public static ArrayList<String> CurrentSendingData;
    public static AtomicInteger CurrentSendingCnt = new AtomicInteger();
    public static AtomicLong CurrentSendingAmt = new AtomicLong();
    private FISC TxFISCBusiness;
    private FISCData TxFISCData;
    private FISC_RM FISCRMReq;
    private FISC_RM FISCRMRes;
    private FeptxnExt FepTxn;
    private Rmoutt _defRMOUTT;
    private String wkPCODE = StringUtils.EMPTY;
    private boolean readInque = false;
    private String PK;
    public boolean stopFlag = false;

    private FeptxnDao DBFEPTXN = SpringBeanFactoryUtil.getBean("feptxnDao");
    @Autowired
    private RmouttExtMapper dbRMOUTT;
    @Autowired
    private RmoutExtMapper dbRMOUT;
    @SuppressWarnings("unused")
    @Autowired
    private Rmfiscout1Mapper dbRMFISCOUT1;
    @SuppressWarnings("unused")
    @Autowired
    private RmoutsnoMapper dbRMOUTSNO;
    private InQueue11X1Controller inQueue11X1Controller;
    @Autowired
    private RmouttDao rmouttDao;
    @Autowired
    private RmoutDao rmoutDao;

    /**
     * .NET版程式的constructor翻寫到此方法中
     */
    @Override
    protected void initialization() throws Exception {
        inQueue11X1Controller = SpringBeanFactoryUtil.registerController(InQueue11X1Controller.class);

        // 紀錄Log
        setLogContext(new LogData());

        getLogContext().setProgramFlowType(ProgramFlow.AAServiceIn);
        getLogContext().setSubSys(SubSystem.RM);
        getLogContext().setProgramName(ProgramName);
        getLogContext().setMessage("");
        getLogContext().setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        // Jim, 2012/5/29, channel改成FEP
        getLogContext().setChannel(FEPChannel.FEP);
        getLogContext().setRemark("Service11X1 Start");
        getLogContext().setBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        getLogContext().setMessageGroup("4");
        logMessage(Level.INFO, getLogContext());

        // 準備FEPTXN對象
        FepTxn = new FeptxnExt();
        DBFEPTXN.setTableNameSuffix(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(6, 8), StringUtils.join(ProgramName, "initialization"));
        // 初始化TxFISCData,TxFiscBusiness,Memory
        initBusiness("0");

    }

    /**
     * .NET版doBusiness()方法翻寫到此方法中
     */
    @Override
    protected FEPReturnCode doBusiness() throws Exception {
        FEPReturnCode rtnCode = null;

        RefString repUNITBANK = new RefString("");
        boolean isFEPTXN = false;
        getLogContext().setProgramName(ProgramName + "doBusiness");

        try {
            // Modify by Jim, 2011/06/01, 強迫快取重新整理
            FEPCache.reloadCache(CacheItem.SYSSTAT);

            SERVICELOGGER.info("start");
            // 2.檢核財金及參加單位之系統狀態
            rtnCode = checkSYSSTATAndRMSTAT(true, false); // 新增傳入參數，決定是否需要檢查RMFISCOUT1_NO = RMFISCOUT1_REP_NO
            SERVICELOGGER.info("At Beginning, After CheckSYSSTATAndRMSTAT, rtncode=" + rtnCode.toString());
            if (rtnCode != CommonReturnCode.Normal) {
                if (rtnCode != CommonReturnCode.Abnormal) {
                    getLogContext().setReturnCode(rtnCode);
                    TxHelper.getRCFromErrorCode(rtnCode, FEPChannel.RM, getLogContext());
                }
                return CommonReturnCode.Normal;
            } else {
                // modify 2011/01/24, 通知FISCAdapter 目前開始傳送FISC之電文序號
                Rmfiscout1 defRMFISCOUT1 = new Rmfiscout1();
                Rmfiscout1Mapper dbRMFISCOUT1 = SpringBeanFactoryUtil.getBean(Rmfiscout1Mapper.class);
                @SuppressWarnings("unused")
                String fiscsno = "";
                defRMFISCOUT1.setRmfiscout1SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
                defRMFISCOUT1.setRmfiscout1ReceiverBank(SysStatus.getPropertyValue().getSysstatFbkno());
                defRMFISCOUT1 = dbRMFISCOUT1.selectByPrimaryKey(defRMFISCOUT1.getRmfiscout1SenderBank(), defRMFISCOUT1.getRmfiscout1ReceiverBank());
                if (defRMFISCOUT1 != null) {
                    FISCAdapter.initialFiscNo(getLogContext(), defRMFISCOUT1.getRmfiscout1No().toString());
                }
            }

            // Modify by Jim, 2011/9/15, 讀Queue應該移到判度CurrentSendingCnt外面，因為讀Queue的目的就是要減掉CurrentSendingCnt
            // 1. 讀取11X1-INQueue
            readINQueue11X1();

            while (CurrentSendingCnt.get() < RMConfig.getInstance().getSendCount()) {
                // (1) 初始化DB相關對象
                dbRMOUTT = SpringBeanFactoryUtil.getBean(RmouttExtMapper.class);
                dbRMOUT = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);
                dbRMFISCOUT1 = SpringBeanFactoryUtil.getBean(Rmfiscout1Mapper.class);
                dbRMOUTSNO = SpringBeanFactoryUtil.getBean(RmoutsnoMapper.class);
                isFEPTXN = false;
                PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
                TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
                try {

                    // (2) 讀取 匯入暫存檔RMOUTT
                    if (queryTop1RMOUTTWithLock() != 1) {// 查詢無資料,則SLEEP Service11X1_INTERVAL
                        break;
                    }

                    wkPCODE = StringUtils.join("1", _defRMOUTT.getRmouttRemtype(), "1");

                    // (3). 初始化TxFISCData,TxFiscBusiness
                    rtnCode = initBusiness("1");

                    // (4) 紀錄處理資料PK
                    PK = StringUtils.join("RMOUTT PK(RMOUTT_TXDATE = [", _defRMOUTT.getRmouttTxdate(), "], RMOUTT_BRNO = [", _defRMOUTT.getRmouttBrno(), "], RMOUTT_ORIGINAL = [",
                            _defRMOUTT.getRmouttOriginal(), "], RMOUTT_FEPNO = [", _defRMOUTT.getRmouttFepno(), "]); STAN=", getLogContext().getStan());

                    // (5). CheckBusinessRule: 商業邏輯檢核
                    if (rtnCode == CommonReturnCode.Normal) {
                        rtnCode = checkBusinessRule(repUNITBANK);
                        SERVICELOGGER.info(StringUtils.join("CheckBusinessRule rtnCode=", rtnCode.toString(), "; repUNITBANK=", repUNITBANK, ", ", PK));
                    }

                    // (6). Prepare : 交易記錄初始資料，AddTxData: 新增交易記錄(FEPTXN)
                    if (rtnCode == CommonReturnCode.Normal) {
                        rtnCode = prepareAndInsertFEPTXN();
                        if (rtnCode == CommonReturnCode.Normal) {
                            isFEPTXN = true;
                        }
                        SERVICELOGGER.info(StringUtils.join("PrepareAndInsertFEPTXN rtnCode=", rtnCode.toString(), "; ", PK));
                    }

                    SERVICELOGGER.info(StringUtils.join("After PrepareAndInsertFEPTXN, Logcontext.Stan=", getLogContext().getStan(), ", FiscData.stan=", TxFISCData.getStan(),
                            ", FiscData.Logcontext.Stan=", TxFISCData.getLogContext().getStan(), ", feptxn.stan=", FepTxn.getFeptxnStan()));
                    // (7).組送往 FISC 之 Request 電文
                    if (rtnCode == CommonReturnCode.Normal) {
                        rtnCode = prepareToFISC(repUNITBANK.get());
                        SERVICELOGGER.info(StringUtils.join("PrepareToFISC rtnCode=", rtnCode.toString(), "; ", PK));
                    }

                    // (8) 更新Memory資料
                    if (rtnCode == CommonReturnCode.Normal) {
                        updateMemory();
                        SERVICELOGGER.info(StringUtils.join("UpdateMemory rtnCode=", rtnCode.toString(), "; ", PK));
                    }

                    if (rtnCode == CommonReturnCode.Normal) {
                        transactionManager.commit(txStatus);
                        startSubProcess(repUNITBANK.get());
                    } else {
                        transactionManager.rollback(txStatus);
                    }
                } catch (Exception ex) {
                    transactionManager.rollback(txStatus);
                    updateRMSTAT();
                    getLogContext().setProgramException(ex);
                    sendEMS(getLogContext());
                    break;
                    // Return CommonReturnCode.ProgramException
                } finally {
                    if (!txStatus.isCompleted()) {
                        transactionManager.commit(txStatus);
                    }
                }

                // (9) 若有錯則紀錄FEPTXN-錯誤代碼
                if (rtnCode != CommonReturnCode.Normal) {
                    SERVICELOGGER.info(StringUtils.join("MainProcess-doBusiness, After Start SubProcess, rtnCode=", rtnCode.toString(), ", ", PK)); // ZK ADD
                    getLogContext().setRemark(StringUtils.join("MainProcess-doBusiness, After Start SubProcess, rtnCode=", rtnCode.toString(), ", ", PK));
                    getLogContext().setReturnCode(rtnCode);
                    TxHelper.getRCFromErrorCode(rtnCode, FEPChannel.RM, getLogContext());
                    if (isFEPTXN) {
                        FepTxn.setFeptxnAaRc(rtnCode.getValue());
                        updateFEPTXN();
                    }
                    // Modify by Jim, 2011/01/26, 如果以上有任何錯誤，皆須Update RMSTAT_FISCO_FLAG1 = N
                    updateRMSTAT();

                    // Modify by Jim, 2010/12/21, 如果有error要再Check一次避免跳不出while
                    if (checkSYSSTATAndRMSTAT(true, false) != CommonReturnCode.Normal) {
                        break;
                    }
                }

                Thread.sleep(1); // Sleep 0.001秒後處理下筆資料
            }

            // Return rtnCode

        } catch (Exception ex) {
            updateRMSTAT();
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        } finally {
            // 2014-02-12 Modify by Candy, 多筆匯出時,傳送中筆數> 可傳送筆數時不需Sleep太久, SYSCONF參數是給DB查無資料時使用
            // Trace.WriteLine("Service11X1-doBusiness, Sleep=" & (RMConfig.Instance().Service11X1_INTERVAL * 1000).ToString())
            // Thread.Sleep(RMConfig.Instance().Service11X1_INTERVAL * 1000)
            if (CurrentSendingCnt.get() < RMConfig.getInstance().getSendCount()) {
                SERVICELOGGER.info(StringUtils.join("Service11X1-doBusiness, Sleep=", RMConfig.getInstance().getService11X1INTERVAL() * 1000));
                Thread.sleep(RMConfig.getInstance().getService11X1INTERVAL() * 1000);
            } else {
                SERVICELOGGER.info("Service11X1-doBusiness, Sleep=50 mSecs");
                Thread.sleep(50);
            }
            // If Not StopFlag Then
            // doBusiness()
            // End If
        }
        return CommonReturnCode.Normal;
    }

    /**
     * .NET版StopService()方法翻寫到此方法中
     */
    @Override
    protected void doStop() throws Exception {
        // 紀錄Log
        getLogContext().setProgramFlowType(ProgramFlow.AAServiceOut);
        getLogContext().setSubSys(SubSystem.RM);
        getLogContext().setProgramName(ProgramName);
        getLogContext().setMessage("");
        getLogContext().setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        // Jim, 2012/5/29, channel改成FEP
        getLogContext().setChannel(FEPChannel.FEP);
        getLogContext().setRemark("Service11X1 Stop");
        getLogContext().setMessageGroup("4");
        logMessage(Level.INFO, getLogContext());

        if (TxFISCBusiness != null) {
            TxFISCBusiness = null;
        }
        if (DBFEPTXN != null) {
            DBFEPTXN = null;
        }
        // _Memory = Nothing
        CurrentSendingData.clear();

        SpringBeanFactoryUtil.unregisterController(InQueue11X1Controller.class);
    }

    /**
     * 此方法暫時不用實作
     */
    @Override
    protected void doPause() throws Exception {
    }

    /**
     * 呼叫Service11X1SubProcessor
     *
     * @param repUNITBANK
     */
    private void startSubProcess(String repUNITBANK) throws Exception {
        final String startToRunTime = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSSSSS_PLAIN);
        SERVICELOGGER.info("start to run Service11X1SubProcessor..., repUNITBANK = [", repUNITBANK, "], startToRunTime = [", startToRunTime, "]");
        Service11X1SubProcessor oSubProcess = new Service11X1SubProcessor() {
            /**
             * 覆寫getName方法, log檔中可以區分線程的名稱
             */
            @Override
            protected String getName() {
                return StringUtils.join("Service11X1Sub-", startToRunTime);
            }
        };
        oSubProcess.initialization(TxFISCData, FepTxn, _defRMOUTT, repUNITBANK);
        oSubProcess.start();
    }

    private FEPReturnCode initBusiness(String sFlag) throws Exception {
        int iNewEJ = 0;
        String sNewSTAN = "0000000";

        if ("0".equals(sFlag)) {
            TxFISCData = new FISCData();
            TxFISCData.setTxChannel(FEPChannel.FISC);
            TxFISCData.setTxSubSystem(SubSystem.RM);
            TxFISCData.setFiscTeleType(FISCSubSystem.RM);
            TxFISCData.setTxRequestMessage("");
            TxFISCData.setMessageFlowType(MessageFlow.Request);
            TxFISCData.setLogContext(getLogContext());
            TxFISCData.setMessageID("11X100");
            TxFISCData.setAaName(ProgramName);

            CurrentSendingData = new ArrayList<String>();
            CurrentSendingCnt = new AtomicInteger(0);
            CurrentSendingAmt = new AtomicLong(0);

            // _Memory = New DataTable()
            // _Memory.Columns.Add(New DataColumn("FISCSNO", Type.GetType("System.String")))
            // _Memory.Columns.Add(New DataColumn("SendTime", Type.GetType("System.DateTime")))
            // _Memory.Columns.Add(New DataColumn("EndTime", Type.GetType("System.DateTime")))
            // _Memory.Columns.Add(New DataColumn("Status", Type.GetType("System.String")))
            // _Memory.Columns.Add(New DataColumn("ReadInque", Type.GetType("System.Boolean")))
            // _Memory.PrimaryKey = New DataColumn() {_Memory.Columns("FISCSNO")}

        } else {
            iNewEJ = TxHelper.generateEj();
            sNewSTAN = TxFISCBusiness.getStan();

            TxFISCData.setMessageID(wkPCODE + "00");
            TxFISCData.setMsgCtl(FEPCache.getMsgctrl(TxFISCData.getMessageID()));
            if (TxFISCData.getMsgCtl().getMsgctlStatus() == 1) {
                TxFISCData.setTxStatus(true);
            } else {
                TxFISCData.setTxStatus(false);
            }

            getLogContext().setStep(0);
            // Modify by Jim, 2011/02/17, 避免如果Service開著過營業日相關日期會錯誤，需要重新取
            getLogContext().setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            DBFEPTXN.setTableNameSuffix(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(6, 8), StringUtils.join(ProgramName + "initBusiness"));
        }

        getLogContext().setEj(iNewEJ);
        getLogContext().setStan(sNewSTAN);
        getLogContext().setMessageId(TxFISCData.getMessageID());
        getLogContext().setMessageFlowType(MessageFlow.Request);
        getLogContext().setProgramFlowType(ProgramFlow.AAIn);
        getLogContext().setBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        getLogContext().setMessageGroup("4");
        FepTxn = new FeptxnExt();
        FISCRMReq = new FISC_RM();
        FISCRMRes = new FISC_RM();
        TxFISCData.setTxObject(new FISCGeneral());
        TxFISCData.getTxObject().setRMRequest(FISCRMReq);
        TxFISCData.getTxObject().setRMResponse(FISCRMRes);
        TxFISCData.setEj(iNewEJ);
        TxFISCData.setStan(sNewSTAN);
        // Modify by Jim, 2011/01/27, 記得Logcontext都需要給!!
        TxFISCData.setLogContext(getLogContext());

        FISCRMReq.setEj(iNewEJ);

        // 建立FISC Business物件
        TxFISCBusiness = new FISC(TxFISCData);
        TxFISCBusiness.setFeptxn(FepTxn);
        TxFISCBusiness.setFeptxnDao(DBFEPTXN);
        TxFISCBusiness.setEj(iNewEJ);
        TxFISCBusiness.setLogContext(getLogContext());
        return CommonReturnCode.Normal;
    }

    private FEPReturnCode readINQueue11X1() {
        String[] messages = inQueue11X1Controller.getAllMessages();
        SERVICELOGGER.info("[readINQueue11X1]messages size = [", messages.length, "]");

        Queue11X1 msg11X1 = new Queue11X1();

        try {

            readInque = false;

            for (String msg : messages) {
                msg11X1 = new Queue11X1();
                msg11X1.setMsgFISCNO(msg.substring(0, 7));
                msg11X1.setMsgSTAT(msg.substring(7, 9));
                long l = 0;
                boolean isOK = false;
                try {
                    l = Long.parseLong(msg.substring(9));
                    isOK = true;
                } catch (Exception e) {
                    isOK = false;
                }
                if (isOK) {
                    msg11X1.setMsgAMT(l);
                }

                getLogContext().setRemark(StringUtils.join("Service11X1-Read INQueue11X1, msgArray.lenth=", messages.length, ", MSG_FISCSNO=", msg11X1.getMsgFISCNO(), ", MSG_STAT=",
                        msg11X1.getMsgSTAT(), ", original CurrentSendingCnt=", CurrentSendingCnt, ", is CurrentSendingData.Contains(msg11X1.MSG_FISCNO)",
                        CurrentSendingData.contains(msg11X1.getMsgFISCNO()),
                        ", CurrentSendingAmt=", CurrentSendingAmt, ", MSG_AMT=", msg11X1.getMsgAMT()));
                logMessage(Level.DEBUG, getLogContext());

                if (CurrentSendingData.contains(msg11X1.getMsgFISCNO())) {
                    CurrentSendingData.remove(msg11X1.getMsgFISCNO());
                    // If msg11X1.MSG_STAT = RMOUTStatus.GrantMoney Then
                    // ReadInque = True
                    // End If
                    CurrentSendingCnt.decrementAndGet();
                    CurrentSendingAmt.getAndAdd(msg11X1.getMsgAMT() * (-1));
                }

                getLogContext().setRemark(StringUtils.join("Service11X1-Read INQueue11X1, After Check", ", MSG_FISCSNO=", msg11X1.getMsgFISCNO(), ", MSG_STAT=", msg11X1.getMsgSTAT(),
                        ", new CurrentSendingCnt=", CurrentSendingCnt, ", is CurrentSendingData.Contains(msg11X1.MSG_FISCNO)", CurrentSendingData.contains(msg11X1.getMsgFISCNO()),
                        ", new CurrentSendingAmt=",
                        CurrentSendingAmt, ", MSG_AMT=", msg11X1.getMsgAMT()));
                logMessage(Level.DEBUG, getLogContext());

                // drArray = _Memory.Select("FISCSNO='" & msg11X1.MSG_FISCNO & "'")
                // If drArray.Length > 0 Then
                // _Memory.Rows.Remove(drArray(0))
                // If msg11X1.MSG_STAT = RMOUTStatus.BackExchange Then
                // ReadInque = True
                // End If
                // '_CurrentSendingCnt = _CurrentSendingCnt - 1
                // Interlocked.Decrement(CurrentSendingCnt)
                // End If
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            sendEMS(logContext);
            return CommonReturnCode.ProgramException;
        }

    }

    @SuppressWarnings("unused")
    private FEPReturnCode checkSYSSTATAndRMSTAT(boolean isCheckRMFISCOUT1) {
        return checkSYSSTATAndRMSTAT(isCheckRMFISCOUT1, false);
    }

    private FEPReturnCode checkSYSSTATAndRMSTAT(boolean isCheckRMFISCOUT1, boolean isBegin) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        RmstatExtMapper dbRMSTAT = SpringBeanFactoryUtil.getBean(RmstatExtMapper.class);
        Rmstat defRMSTAT = new Rmstat();
        Rmfiscout1 defRMFISCOUT1 = new Rmfiscout1();
        Rmfiscout1 defRMFISCOUT1_2 = new Rmfiscout1();
        Rmfiscout1Mapper dbRMFISCOUT1 = SpringBeanFactoryUtil.getBean(Rmfiscout1Mapper.class);

        try {
            if (CurrentSendingCnt.get() >= RMConfig.getInstance().getSendCount()) {
                SERVICELOGGER.info("CurrentSendingCnt: " + CurrentSendingCnt.get() + ", SendCount: " + RMConfig.getInstance().getSendCount());
                readINQueue11X1();
                SERVICELOGGER.info("Service11X1-CheckSYSSTATAndRMSTAT, _CurrentSendingCnt = RMConfig.Instance.SendCount; Read INQUEUE11X1");
                Thread.sleep(200);
                return CommonReturnCode.Abnormal;
            }

            // (a) 檢核匯出狀態
            if (StringUtils.isBlank(wkPCODE)) {
                wkPCODE = "1100";
            }
            rtnCode = TxFISCBusiness.checkRMStatus(wkPCODE, true);
            if (rtnCode == CommonReturnCode.Normal) {

                // (b) 檢核是否可傳送財金
                defRMSTAT.setRmstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
                defRMSTAT = dbRMSTAT.selectByPrimaryKey(defRMSTAT.getRmstatHbkno());
                if (defRMSTAT != null) {
                    if ("N".equals(defRMSTAT.getRmstatFiscoFlag1())) {
                        SERVICELOGGER.info("Service11X1-CheckSYSSTATAndRMSTAT--RMSTAT_FISCO_FLAG1 = N");
                        rtnCode = CommonReturnCode.Abnormal;
                        return rtnCode;
                    }
                } else {
                    SERVICELOGGER.info("Service11X1--CheckSYSSTATAndRMSTAT--RMSTATNotFound");
                    rtnCode = IOReturnCode.RMSTATNotFound;
                    return rtnCode;
                }
                if (isBegin) {
                    getLogContext().setRemark(StringUtils.join("Begin Transaction,", PK));
                    // Jim, 2012/5/29, channel改成FEP
                    getLogContext().setChannel(FEPChannel.FEP);
                    getLogContext().setProgramFlowType(ProgramFlow.AAIn);
                    logMessage(Level.DEBUG, getLogContext());
                }
            } else {
                SERVICELOGGER.info(StringUtils.join("Service11X1--CheckSYSSTATAndRMSTAT--TxFISCBusiness.CheckRMStatus error, rtncode = ", rtnCode.getValue()));
                return rtnCode;
            }

            if (isCheckRMFISCOUT1) {
                defRMFISCOUT1.setRmfiscout1SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
                defRMFISCOUT1.setRmfiscout1ReceiverBank(SysStatus.getPropertyValue().getSysstatFbkno());

                // Fly 2015/05/11 因RMFISCOUT1 Block 造成FEDIGW SELECT RMOUT Timeout 把RMFISCOUT1拆二筆
                defRMFISCOUT1_2.setRmfiscout1SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
                defRMFISCOUT1_2.setRmfiscout1ReceiverBank("999");
                defRMFISCOUT1 = dbRMFISCOUT1.selectByPrimaryKey(defRMFISCOUT1.getRmfiscout1SenderBank(), defRMFISCOUT1.getRmfiscout1ReceiverBank());
                defRMFISCOUT1_2 = dbRMFISCOUT1.selectByPrimaryKey(defRMFISCOUT1_2.getRmfiscout1SenderBank(), defRMFISCOUT1_2.getRmfiscout1ReceiverBank());
                if (defRMFISCOUT1 != null && defRMFISCOUT1_2 != null) {
                    if (!defRMFISCOUT1_2.getRmfiscout1RepNo().equals(defRMFISCOUT1.getRmfiscout1No())) {
                        // log only when error
                        SERVICELOGGER.info(StringUtils.join("Service11X1--CheckSYSSTATAndRMSTAT--defRMFISCOUT1.RMFISCOUT1_NO=", defRMFISCOUT1.getRmfiscout1No(),
                                ", defRMFISCOUT1_2.RMFISCOUT1_REP_NO=", defRMFISCOUT1_2.getRmfiscout1RepNo()));
                        rtnCode = RMReturnCode.RemitDataNotComplete;
                        return rtnCode;
                    }
                } else {
                    SERVICELOGGER.info("Service11X1--CheckSYSSTATAndRMSTAT--RMFISCOUT1NotFound");
                    rtnCode = IOReturnCode.RMFISCOUT1NotFound;
                    return rtnCode;
                }
            }

            // Fly 2019/10/18 增加檢核匯款暫禁記號
            Clrdtl _defCLRDTL = new Clrdtl();
            _defCLRDTL.setClrdtlTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            _defCLRDTL.setClrdtlApId("10000");
            _defCLRDTL.setClrdtlPaytype(" ");
            ClrdtlExtMapper dbCLR = SpringBeanFactoryUtil.getBean(ClrdtlExtMapper.class);
            try {
                dbCLR.selectByPrimaryKey(_defCLRDTL.getClrdtlTxdate(), _defCLRDTL.getClrdtlApId(), _defCLRDTL.getClrdtlPaytype());
            } catch (Exception ex) {
                getLogContext().setProgramException(ex);
                sendEMS(getLogContext());
                rtnCode = CommonReturnCode.ProgramException;
                return rtnCode;
            }
            if ("N".equals(_defCLRDTL.getClrdtlRmstat())) {
                SERVICELOGGER.info("Service11X1--CheckSYSSTATAndRMSTAT--_defCLRDTL.ClrdtlRmstat = N");
                rtnCode = FEPReturnCode.Abnormal;
                return rtnCode;
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            rtnCode = CommonReturnCode.ProgramException;
            return rtnCode;
        } finally {
            defRMSTAT = null;
        }
        return rtnCode;
    }

    @SuppressWarnings("unused")
    private FEPReturnCode checkALLBANKFlag() {
        Allbank defALLBANK = new Allbank();
        AllbankExtMapper dbALLBANK = SpringBeanFactoryUtil.getBean(AllbankExtMapper.class);

        try {
            defALLBANK.setAllbankBkno(_defRMOUTT.getRmouttReceiverBank().substring(0, 3));
            defALLBANK.setAllbankBrno("000");
            defALLBANK = dbALLBANK.selectByPrimaryKey(defALLBANK.getAllbankBkno(), defALLBANK.getAllbankBrno());
            if (defALLBANK != null) {
                defALLBANK.setAllbankBkno(defALLBANK.getAllbankUnitBank());
                defALLBANK.setAllbankBrno("000");
                defALLBANK = dbALLBANK.selectByPrimaryKey(defALLBANK.getAllbankBkno(), defALLBANK.getAllbankBrno());
                if (defALLBANK != null) {
                    if ("1".equals(defALLBANK.getAllbankRmforward())) {
                        SERVICELOGGER.info("Service11X1--CheckSYSSTATAndRMSTAT--CheckALLBANK error, ALLBANK_RMFORWARD = 1");
                        return CommonReturnCode.Abnormal;
                    }
                }
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    private int queryTop1RMOUTTWithLock() {
        int iRet = -1;

        try {
            // Fly 2020/11/17
            // 跨行餘額檢核改成撈取DB內CLRDTL資料，用該資料計算目前匯出資料中哪筆可以匯出
            // 例如餘額為18億3千萬，第一水位為18億，則可以匯出3千萬以下的匯款資料
            // 以及第二水位為13億，也允許200萬以下，但不會導致低於13億的資料
            Clrdtl _defCLRDTL = new Clrdtl();
            _defCLRDTL.setClrdtlTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            _defCLRDTL.setClrdtlApId("10000");
            _defCLRDTL.setClrdtlPaytype(" ");
            ClrdtlExtMapper dbCLR = SpringBeanFactoryUtil.getBean(ClrdtlExtMapper.class);
            try {
                _defCLRDTL = dbCLR.selectByPrimaryKey(_defCLRDTL.getClrdtlTxdate(), _defCLRDTL.getClrdtlApId(), _defCLRDTL.getClrdtlPaytype());
                if (_defCLRDTL == null) {
                    // 查不到就先不送
                    return 0;
                }
            } catch (Exception ex) {
                getLogContext().setProgramException(ex);
                sendEMS(getLogContext());
                return 0;
            }
            _defCLRDTL.setClrdtlUseBal(BigDecimal.valueOf(_defCLRDTL.getClrdtlUseBal().longValue() - CurrentSendingAmt.get()));
            BigDecimal FundLevel2 = BigDecimal.valueOf(_defCLRDTL.getClrdtlPreFund().doubleValue() * RMConfig.getInstance().getCLRRate() + RMConfig.getInstance().getCLRPLUSAmout2());
            BigDecimal OutwardCheckAmount = BigDecimal.valueOf(RMConfig.getInstance().getOutwardCheckAmount());
            BigDecimal FundLevel = BigDecimal.valueOf(_defCLRDTL.getClrdtlPreFund().doubleValue() * RMConfig.getInstance().getCLRRate() + RMConfig.getInstance().getCLRPLUSAmout());

            _defRMOUTT = new Rmoutt();

            _defRMOUTT.setRmouttTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            _defRMOUTT.setRmouttOwpriority("9");
            _defRMOUTT.setRmouttFiscRtnCode(FISC_OK);
            _defRMOUTT.setRmouttSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());

            // Jim, 2012/2/23, 先查已經有電文序號的資料，而且要序號小的先送
            _defRMOUTT.setRmouttFiscsno("");
            // Fly 2019/04/18 匯款金額大於200萬且超過跨行基金水位RMOUTT_OWPRIORITY=’8’ 不能匯出
            String[] owprioritys = {"8", "9"};
            _defRMOUTT = dbRMOUTT.getRMOUTTForService11X1(_defRMOUTT, owprioritys, FundLevel, FundLevel2, _defCLRDTL.getClrdtlUseBal(), OutwardCheckAmount);
            // iRet = dbRMOUTT.GetRMOUTTForService11X1(_defRMOUTT)
            if (_defRMOUTT == null) {
                iRet = 0;
                // 避免XXXPassed屬性有不乾淨的，再new一次defRMOUTT
                _defRMOUTT = new Rmoutt();
                _defRMOUTT.setRmouttTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                _defRMOUTT.setRmouttOwpriority("9");
                _defRMOUTT.setRmouttFiscRtnCode(FISC_OK);
                _defRMOUTT.setRmouttSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
                // Fly 2019/04/18 匯款金額大於200萬且超過跨行基金水位RMOUTT_OWPRIORITY=’8’ 不能匯出
                Rmoutt rmoutt = dbRMOUTT.getRMOUTTForService11X1(_defRMOUTT, owprioritys, FundLevel, FundLevel2, _defCLRDTL.getClrdtlUseBal(), OutwardCheckAmount);
                SERVICELOGGER.info("RMOUTT 查詢條件：Txdate： " + _defRMOUTT.getRmouttTxdate()
                        + " ,Owpriority： " + _defRMOUTT.getRmouttOwpriority()
                        + " ,FiscRtnCode： " + FISC_OK
                        + " ,SenderBank： " + _defRMOUTT.getRmouttSenderBank()
                        + " ,FundLevel： " + FormatUtil.decimalFormat(FundLevel, "#,###.###")
                        + " ,FundLevel2： " + FormatUtil.decimalFormat(FundLevel2, "#,###.###")
                        + " ,bal： " + FormatUtil.decimalFormat(_defCLRDTL.getClrdtlUseBal(), "#,###.###")
                        + " ,OutwardCheckAmount： " + OutwardCheckAmount
                );
                if (rmoutt != null) {
                    _defRMOUTT = rmoutt;
                    iRet = 1;
                }
                // iRet = dbRMOUTT.GetRMOUTTForService11X1(_defRMOUTT)
            } else {
                iRet = 1;
            }

            // Fly 2019/10/18 For跨行餘額內控需求調整
            if (checkCLRBAL()) {
                return 0;
            }

            if (iRet != 1) {// 查詢無資料,則SLEEP Service11X1_INTERVAL
                SERVICELOGGER.info("Service11X1-Query RMOUTTTTTTT no data");
                return 0;
            } else {
                _defRMOUTT.setRmouttTxdate(_defRMOUTT.getRmouttTxdate());
                _defRMOUTT.setRmouttBrno(_defRMOUTT.getRmouttBrno());
                _defRMOUTT.setRmouttOriginal(_defRMOUTT.getRmouttOriginal());
                _defRMOUTT.setRmouttFepno(_defRMOUTT.getRmouttFepno());
                SERVICELOGGER.info("Service11X1-RMOUTT-QueryByPrimaryKeyWithUpdLock-START");
                Rmoutt rmoutt = rmouttDao.queryByPrimaryKeyWithUpdLock(_defRMOUTT.getRmouttTxdate(), _defRMOUTT.getRmouttBrno(), _defRMOUTT.getRmouttOriginal(), _defRMOUTT.getRmouttFepno());
                SERVICELOGGER.info("Service11X1-RMOUTT-QueryByPrimaryKeyWithUpdLock-END");
                if (rmoutt == null) {
                    return 0;
                } else {
                    _defRMOUTT = rmoutt;
                    // 中途狀態又被改掉了
                    // Fly 2019/05/15 For 跨行餘額內控 可能已被CLRCheck改成系統暫停，需先檢查
                    if ((!RMOUTStatus.Passed.equals(_defRMOUTT.getRmouttStat()) && !RMOUTStatus.SystemProblem.equals(_defRMOUTT.getRmouttStat())) || "8".equals(_defRMOUTT.getRmouttOwpriority())) {
                        SERVICELOGGER.info(StringUtils.join("RmouttStat:", _defRMOUTT.getRmouttStat(), ", RmouttOwpriority:", _defRMOUTT.getRmouttOwpriority())); // ZK ADD
                        return 0;
                    } else {
                        Rmout tmpRMOUT = new Rmout();
                        tmpRMOUT.setRmoutTxdate(_defRMOUTT.getRmouttTxdate());
                        tmpRMOUT.setRmoutBrno(_defRMOUTT.getRmouttBrno());
                        tmpRMOUT.setRmoutOriginal(_defRMOUTT.getRmouttOriginal());
                        tmpRMOUT.setRmoutFepno(_defRMOUTT.getRmouttFepno());
                        SERVICELOGGER.info("Service11X1-RMOUT-QueryByPrimaryKeyWithUpdLock-START");
                        Rmout rmout = rmoutDao.queryByPrimaryKeyWithUpdLock(tmpRMOUT.getRmoutTxdate(), tmpRMOUT.getRmoutBrno(), tmpRMOUT.getRmoutOriginal(), tmpRMOUT.getRmoutFepno());
                        SERVICELOGGER.info("Service11X1-RMOUT-QueryByPrimaryKeyWithUpdLock-END");
                        if (rmout == null) {
                            SERVICELOGGER.info("Service11X1-Query RMOUT no data");
                            return 0;
                        } else {
                            tmpRMOUT = rmout;
                            // Fly 2019/05/15 For 跨行餘額內控 可能已被CLRCheck改成系統暫停，需先檢查
                            if ((!RMOUTStatus.Passed.equals(tmpRMOUT.getRmoutStat()) && !RMOUTStatus.SystemProblem.equals(tmpRMOUT.getRmoutStat())) || "8".equals(tmpRMOUT.getRmoutOwpriority())) {
                                SERVICELOGGER.info("Service11X1-Query RMOUT, STAT <> 04, 99");
                                return 0;
                            } else {
                                SERVICELOGGER.info("RMOUTT.RMOUTT_TXAMT： " + FormatUtil.doubleFormat(_defRMOUTT.getRmouttTxamt().doubleValue(), "#,###.###"));
                                if (_defRMOUTT.getRmouttTxamt().doubleValue() >= RMConfig.getInstance().getLargeAmount()) {
                                    FISCGeneral aData = new FISCGeneral();
                                    FEPHandler fepHandler = new FEPHandler();
                                    String[] message = null;
                                    aData.setCLRRequest(new FISC_CLR());
                                    aData.setSubSystem(FISCSubSystem.CLR);
                                    aData.getCLRRequest().setMessageKind(MessageFlow.Request);
                                    aData.getCLRRequest().setProcessingCode("5202");
                                    aData.getCLRRequest().setMessageType("0500");
                                    aData.getCLRRequest().setAPID5("10000");
                                    aData.getCLRRequest().setLogContext(new LogData());
                                    // Call AA
                                    getLogContext().setRemark("Call AA5202 START");
                                    logMessage(Level.INFO, getLogContext());
                                    getLogContext().setMessageId("AA5202");
                                    fepHandler.dispatch(FEPChannel.FEP, aData);
                                    getLogContext().setRemark(StringUtils.join("Call AA5202 FINISH |text ", aData.getDescription()));
                                    logMessage(Level.INFO, getLogContext());
                                    message = aData.getDescription().split("[-]", -1);
                                    if (message.length == 2 && message[0].equals(FISC_OK)) {
                                        getLogContext().setRemark(StringUtils.join("USE_BAL: ", aData.getCLRResponse().getFundAvail()));
                                        logMessage(Level.INFO, getLogContext());
                                        _defRMOUTT.setRmouttUseBal(new BigDecimal(aData.getCLRResponse().getFundAvail()));
                                        dbRMOUTT.updateByPrimaryKeySelective(_defRMOUTT);
                                        Rmout defRMOUT = new Rmout();
                                        defRMOUT.setRmoutTxdate(_defRMOUTT.getRmouttTxdate());
                                        defRMOUT.setRmoutBrno(_defRMOUTT.getRmouttBrno());
                                        defRMOUT.setRmoutOriginal(_defRMOUTT.getRmouttOriginal());
                                        defRMOUT.setRmoutFepno(_defRMOUTT.getRmouttFepno());
                                        defRMOUT.setRmoutUseBal(_defRMOUTT.getRmouttUseBal());
                                        dbRMOUT.updateByPrimaryKeySelective(defRMOUT);
                                        if ((StringUtils.isBlank(aData.getCLRResponse().getFundAvail()) ? 0
                                                : Double.parseDouble(aData.getCLRResponse().getFundAvail()) - _defRMOUTT.getRmouttTxamt().doubleValue()) < FundLevel.doubleValue()) {
                                            getLogContext().setRemark(StringUtils.join("該筆匯出超過水位 RMOUTT_FEPNO[", _defRMOUTT.getRmouttFepno(), "] RMOUTT_BRNO[", _defRMOUTT.getRmouttBrno(),
                                                    "] RMOUTT_TXAMT[", _defRMOUTT.getRmouttTxamt(), "] CLRDTL_USE_BAL[", _defRMOUTT.getRmouttUseBal(), "] FundLevel[", FundLevel, "]"));
                                            logMessage(Level.INFO, getLogContext());
                                            return 0;
                                        }
                                    } else {
                                        getLogContext().setRemark("查詢5202失敗");
                                        logMessage(Level.INFO, getLogContext());
                                        return 0;
                                    }
                                }

                                return 1;
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
        }
        return iRet;
    }

    public boolean checkCLRBAL() {
        Sysconf defSys = new Sysconf();
        defSys.setSysconfSubsysno((short) 2);
        defSys.setSysconfName("CLRPLUSAmout2");
        SysconfExtMapper dbSys = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
        Sysconf sysconf = dbSys.selectByPrimaryKey(defSys.getSysconfSubsysno(), defSys.getSysconfName());
        if (sysconf != null) {
            defSys = sysconf;
        }
        Clrdtl _defCLRDTL = new Clrdtl();
        _defCLRDTL.setClrdtlTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        _defCLRDTL.setClrdtlApId("10000");
        _defCLRDTL.setClrdtlPaytype(" ");
        ClrdtlExtMapper dbCLR = SpringBeanFactoryUtil.getBean(ClrdtlExtMapper.class);
        _defCLRDTL = dbCLR.selectByPrimaryKey(_defCLRDTL.getClrdtlTxdate(), _defCLRDTL.getClrdtlApId(), _defCLRDTL.getClrdtlPaytype());
        if (_defCLRDTL == null) {
            // 查不到就先不送
            return true;
        }

        BigDecimal FundLevel2 = BigDecimal.valueOf(
                _defCLRDTL.getClrdtlPreFund().doubleValue() * RMConfig.getInstance().getCLRRate() + (StringUtils.isBlank(defSys.getSysconfValue()) ? 0 : Double.parseDouble(defSys.getSysconfValue())));
        int i = 0;
        if (_defCLRDTL.getClrdtlUseBal().doubleValue() < FundLevel2.doubleValue()) {
            // 低於第二水位
            if ("Y".equals(_defCLRDTL.getClrdtlRmstat())) {
                _defCLRDTL.setClrdtlRmstat("R");
                i = dbCLR.updateByPrimaryKeySelective(_defCLRDTL);
                if (i > 0) {
                    SERVICELOGGER.info("跨行基金低於第二水位，將狀態改為R. 更新筆數" + i); // ZK ADD
                    getLogContext().setRemark("跨行基金低於第二水位，將狀態改為R");
                    logMessage(Level.DEBUG, getLogContext());
                    FEPReturnCode rc = null;
                    getLogContext().setChannel(FEPChannel.FEP);
                    getLogContext().setMessageGroup("4");
                    rc = FEPReturnCode.LowCLRFundLevel2;
                    TxHelper.getRCFromErrorCode(rc, FEPChannel.FEP, getLogContext());
                } else {
                    getLogContext().setRemark("更新CLRDTL失敗");
                    logMessage(Level.DEBUG, getLogContext());
                }
            }
            return true;
        } else {
            if ("R".equals(_defCLRDTL.getClrdtlRmstat())) {
                _defCLRDTL.setClrdtlRmstat("Y");
                i = dbCLR.updateByPrimaryKeySelective(_defCLRDTL);
                if (i > 0) {
                    SERVICELOGGER.info("跨行基金高於第二水位，將狀態改為Y. 更新筆數" + i); // ZK ADD
                    getLogContext().setRemark("跨行基金高於第二水位，將狀態改為Y");
                    logMessage(Level.DEBUG, getLogContext());
                } else {
                    getLogContext().setRemark("更新CLRDTL失敗");
                    logMessage(Level.DEBUG, getLogContext());
                }

            }
            return false;
        }
    }

    private FEPReturnCode checkBusinessRule(RefString repUNITBANK) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        try {
            repUNITBANK.set("");

            // Modify by Jim, 2011/02/10, 移至QUERY RMOUTT時直接過濾
            // (2) 判斷解款行庫是否可匯出
            // rtnCode = CheckALLBANKFlag()
            // If rtnCode <> CommonReturnCode.Normal Then
            // Trace.WriteLine("MainProcess-CheckBusinessRule Error, ALLBANK_RMFORWARD = 1, rtnCode=" + rtnCode.ToString)
            // Return rtnCode
            // End If

            // 檢核一般通訊匯出狀態/檢核是否可傳送財金
            rtnCode = checkSYSSTATAndRMSTAT(false, true);
            if (rtnCode != CommonReturnCode.Normal) {
                SERVICELOGGER.info("Service11X1--CheckBusinessRule--CheckSYSSTATAndRMSTAT error, rtnCode = " + rtnCode.toString());
                return rtnCode;
            }

            // 檢核匯出資料
            FISCRMReq.setProcessingCode(StringUtils.join("1", _defRMOUTT.getRmouttRemtype(), "1"));
            rtnCode = TxFISCBusiness.checkOutDataByRMOUTT(_defRMOUTT, repUNITBANK);
            if (rtnCode != CommonReturnCode.Normal) {
                SERVICELOGGER.info("Service11X1--CheckBusinessRule--CheckOutDataByRMOUTT error, rtnCode = " + rtnCode.toString());
                return rtnCode;
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return rtnCode;
    }

    private FEPReturnCode prepareAndInsertFEPTXN() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        try {
            rtnCode = TxFISCBusiness.prepareFEPTXNBy11X1(_defRMOUTT);
            if (rtnCode == CommonReturnCode.Normal) {

                if (DBFEPTXN.insertSelective(FepTxn) != 1) {
                    SERVICELOGGER.info("Service11X1-PrepareAndInsertFEPTXN, Insert FepTxn insert error");
                    rtnCode = IOReturnCode.FEPTXNInsertError;
                }

            }

            return rtnCode;

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        } finally {
            if (rtnCode != CommonReturnCode.Normal && rtnCode != CommonReturnCode.ProgramException) {
                TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, TxFISCData.getTxChannel(), getLogContext());
            }
        }
    }

    private FEPReturnCode prepareToFISC(String repUNITBANK) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        getLogContext().setProgramName(ProgramName + "PrepareToFISC");

        try {

            // (1) 取STAN,電文序號,通匯序號及更新到匯出主檔/匯出暫存檔
            rtnCode = updateRMOUTTForReq(repUNITBANK);

            // 組Req電文
            if (rtnCode == CommonReturnCode.Normal) {
                rtnCode = prepareRM11X1Req(repUNITBANK);
            }

            // 更新匯出狀態為05-傳送中
            if (rtnCode == CommonReturnCode.Normal) {
                rtnCode = updateRMOUTTForTransfered();
            }

            if (rtnCode != CommonReturnCode.Normal) {
                updateRMSTAT();
                getLogContext().setReturnCode(rtnCode);
                getLogContext().setRemark(StringUtils.join("PrepareReqToFisc not Normal, rtnCode = ", rtnCode.toString(), ", Update RMSTAT_FISCO_FLAG1 = N "));
                logMessage(Level.DEBUG, getLogContext());
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    // (1) 取STAN,電文序號,通匯序號及更新到匯出主檔/匯出暫存檔
    private FEPReturnCode updateRMOUTTForReq(String reqUNITBANK) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        Rmoutt defRMOUTT = new Rmoutt();

        Rmout defRMOUT = new Rmout();

        Rmfiscout1 defRMFISCOUT1 = new Rmfiscout1();

        Rmoutsno defRMOUTSNO = new Rmoutsno();

        try {
            defRMOUTT.setRmouttTxdate(_defRMOUTT.getRmouttTxdate());
            defRMOUTT.setRmouttBrno(_defRMOUTT.getRmouttBrno());
            defRMOUTT.setRmouttOriginal(_defRMOUTT.getRmouttOriginal());
            defRMOUTT.setRmouttFepno(_defRMOUTT.getRmouttFepno());

            // Fly 2015/05/11 因RMFISCOUT1 Block 造成FEDIGW SELECT RMOUT Timeout 把RMFISCOUT1拆二筆
            if (readInque) {
                defRMFISCOUT1.setRmfiscout1SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
                defRMFISCOUT1.setRmfiscout1ReceiverBank("999");
            } else {
                defRMFISCOUT1.setRmfiscout1SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
                defRMFISCOUT1.setRmfiscout1ReceiverBank(SysStatus.getPropertyValue().getSysstatFbkno());
            }
            SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);
            defRMOUTT.setRmouttFiscsno(StringUtils.leftPad(spCaller.getRMFISCOUT1NO(defRMFISCOUT1, readInque).toString(), 7, '0'));

            defRMOUTSNO.setRmoutsnoSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
            // Modify by Jim, 2011/11/9, 抓RMOUTSNO要用UNIT_BANK
            // defRMOUTSNO.RMOUTSNO_RECEIVER_BANK = _defRMOUTT.RMOUTT_RECEIVER_BANK.Substring(0, 3)
            defRMOUTSNO.setRmoutsnoReceiverBank(reqUNITBANK);
            defRMOUTT.setRmouttRmsno(StringUtils.leftPad(spCaller.getRMOUTSNONO(defRMOUTSNO, readInque).toString(), 7, '0'));

            defRMOUTT.setRmouttStan(TxFISCData.getStan());
            defRMOUTT.setRmouttEjno1(TxFISCData.getEj());
            defRMOUTT.setRmouttStanBkno(SysStatus.getPropertyValue().getSysstatHbkno());
            if (dbRMOUTT.updateByPrimaryKeySelective(defRMOUTT) != 1) {
                rtnCode = IOReturnCode.RMOUTUpdateError;
                return rtnCode;
            }

            defRMOUT.setRmoutTxdate(_defRMOUTT.getRmouttTxdate());
            defRMOUT.setRmoutBrno(_defRMOUTT.getRmouttBrno());
            defRMOUT.setRmoutOriginal(_defRMOUTT.getRmouttOriginal());
            defRMOUT.setRmoutFepno(_defRMOUTT.getRmouttFepno());

            defRMOUT.setRmoutFiscsno(defRMOUTT.getRmouttFiscsno());
            defRMOUT.setRmoutRmsno(defRMOUTT.getRmouttRmsno());
            defRMOUT.setRmoutStan(defRMOUTT.getRmouttStan());
            defRMOUT.setRmoutEjno1(defRMOUTT.getRmouttEjno1());
            defRMOUT.setRmoutStanBkno(defRMOUTT.getRmouttStanBkno());

            if (dbRMOUT.updateByPrimaryKeySelective(defRMOUT) != 1) {
                rtnCode = IOReturnCode.RMOUTUpdateError;
                return rtnCode;
            }

            _defRMOUTT.setRmouttFiscsno(defRMOUTT.getRmouttFiscsno());
            _defRMOUTT.setRmouttRmsno(defRMOUTT.getRmouttRmsno());
            _defRMOUTT.setRmouttStan(defRMOUTT.getRmouttStan());
            _defRMOUTT.setRmouttEjno1(defRMOUTT.getRmouttEjno1());
            _defRMOUTT.setRmouttStanBkno(defRMOUTT.getRmouttStanBkno());
            getLogContext().setRemark(StringUtils.join("取相關序號, FISCSNO=", defRMOUTT.getRmouttFiscsno(), ", RMSNO=", defRMOUTT.getRmouttRmsno()));
            logMessage(Level.DEBUG, getLogContext());

            // Fly 2020/09/24 跨行基金及大額通報需求 發送大額匯款後先sleep一小段時間
            if (_defRMOUTT.getRmouttTxamt().doubleValue() >= RMConfig.getInstance().getLargeAmount()) {
                Thread.sleep(RMConfig.getInstance().getLargeAmountSleepSec());
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return rtnCode;
    }

    private FEPReturnCode prepareRM11X1Req(String repUNITBANK) {
        String reqMAC = "";
        ENCHelper encHelper = null;
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            // 組header
            FISCRMReq.setSystemSupervisoryControlHeader("00");
            FISCRMReq.setSystemNetworkIdentifier("00");
            FISCRMReq.setAdderssControlField("00");
            FISCRMReq.setMessageType("0200");
            FISCRMReq.setProcessingCode(StringUtils.join("1", _defRMOUTT.getRmouttRemtype(), "1"));
            FISCRMReq.setSystemTraceAuditNo(FepTxn.getFeptxnStan());
            FISCRMReq.setTxnDestinationInstituteId(StringUtils.rightPad("950", 7, '0'));
            FISCRMReq.setTxnSourceInstituteId(StringUtils.rightPad(_defRMOUTT.getRmouttSenderBank().substring(0, 3), 7, '0'));
            FISCRMReq.setTxnInitiateDateAndTime(CalendarUtil.adStringToROCString(FepTxn.getFeptxnTxDate()).substring(1, 7) + FepTxn.getFeptxnTxTime()); // (轉成民國年)
            FISCRMReq.setResponseCode(NormalRC.FISC_REQ_RC);
            FISCRMReq.setSyncCheckItem(StringUtils.leftPad(SysStatus.getPropertyValue().getSysstatTrmsync(), 8, ' '));

            // 組Body
            switch (FISCRMReq.getProcessingCode()) {
                case "1111":
                case "1121":
                case "1131":
                case "1181":
                case "1191":
                    FISCRMReq.setInActno(_defRMOUTT.getRmouttInAccIdNo());
                    // FISCRMReq.TX_AMT = "+" & (CDbl(_defRMOUTT.RMOUTT_TXAMT) * 100).ToString.PadLeft(13, "0"c)
                    FISCRMReq.setTxAmt(_defRMOUTT.getRmouttTxamt().toString());
                    FISCRMReq.setFiscNo(_defRMOUTT.getRmouttFiscsno());
                    FISCRMReq.setBankNo(_defRMOUTT.getRmouttRmsno());
                    FISCRMReq.setSenderBank(_defRMOUTT.getRmouttSenderBank());
                    FISCRMReq.setReceiverBank(_defRMOUTT.getRmouttReceiverBank());
                    RefString inName = new RefString(FISCRMReq.getInName());
                    if (TxFISCBusiness.convertFiscEncode(_defRMOUTT.getRmouttInName(), inName)) {
                        FISCRMReq.setInName(inName.get());
                        FISCRMReq.setInName(StringUtil.convertFromAnyBaseString(String.valueOf((FISCRMReq.getInName().length() + 4) / 2), 10, 16, 4) + FISCRMReq.getInName());
                    } else {
                        return RMReturnCode.ChineseRowError;
                    }
                    RefString outName = new RefString(FISCRMReq.getOutName());
                    if (TxFISCBusiness.convertFiscEncode(_defRMOUTT.getRmouttOutName(), outName)) {
                        FISCRMReq.setOutName(outName.get());
                        FISCRMReq.setOutName(StringUtil.convertFromAnyBaseString(String.valueOf((FISCRMReq.getOutName().length() + 4) / 2), 10, 16, 4) + FISCRMReq.getOutName());
                    } else {
                        return RMReturnCode.ChineseRowError;
                    }
                    if (StringUtils.isNotBlank(_defRMOUTT.getRmouttMemo().trim())) {
                        // Modify by Jim, 2011/05/25, 不需要判斷第一碼是否為中文, 都需要呼叫轉碼
                        // If Encoding.GetEncoding("Big5").GetByteCount(_defRMOUTT.RMOUTT_MEMO.Substring(0, 1)) = 1 Then
                        // FISCRMReq.CHINESE_MEMO = StringLib.AsciiStringToHex(_defRMOUTT.RMOUTT_MEMO)
                        // LogContext.Remark = "Service11X1-PrepareRM11X1Req, FISCRMReq.CHINESE_MEMO = " & FISCRMReq.CHINESE_MEMO & "; " & PK & "; PCODE = " & FISCRMReq.ProcessingCode
                        // LogMessage(LogLevel.Info, LogContext)

                        // FISCRMReq.CHINESE_MEMO = StringLib.ConvertFromAnyBaseString( _
                        // ((FISCRMReq.CHINESE_MEMO.Length() + 4) / 2).ToString(), 10, 16, 4) & FISCRMReq.CHINESE_MEMO
                        // Else
                        RefString memoStr = new RefString(FISCRMReq.getChineseMemo());
                        if (TxFISCBusiness.convertFiscEncode(_defRMOUTT.getRmouttMemo(), memoStr)) {
                            FISCRMReq.setChineseMemo(memoStr.get());
                            FISCRMReq.setChineseMemo(StringUtil.convertFromAnyBaseString(String.valueOf((FISCRMReq.getChineseMemo().length() + 4) / 2), 10, 16, 4) + FISCRMReq.getChineseMemo());
                        } else {
                            return RMReturnCode.ChineseRowError;
                        }
                        // End If
                    }

                    FISCRMReq.setTxDate(CalendarUtil.adStringToROCString(_defRMOUTT.getRmouttTxdate()).substring(1, 7));
                    break;
                case "1171":
                    FISCRMReq.setInActno(_defRMOUTT.getRmouttInAccIdNo());
                    // FISCRMReq.TX_AMT = "+" & (CDbl(_defRMOUTT.RMOUTT_TXAMT) * 100).ToString.PadLeft(13, "0"c)
                    FISCRMReq.setTxAmt(_defRMOUTT.getRmouttTxamt().toString());
                    FISCRMReq.setFiscNo(_defRMOUTT.getRmouttFiscsno());
                    FISCRMReq.setBankNo(_defRMOUTT.getRmouttRmsno());
                    FISCRMReq.setOrgBankNo(_defRMOUTT.getRmouttOrgrmsno());
                    FISCRMReq.setSenderBank(_defRMOUTT.getRmouttSenderBank());
                    FISCRMReq.setReceiverBank(_defRMOUTT.getRmouttReceiverBank());
                    FISCRMReq.setSTATUS(_defRMOUTT.getRmouttBackReason());
                    RefString insName = new RefString(FISCRMReq.getInName());
                    if (TxFISCBusiness.convertFiscEncode(_defRMOUTT.getRmouttInName(), insName)) {
                        FISCRMReq.setInName(insName.get());
                        FISCRMReq.setInName(StringUtil.convertFromAnyBaseString(String.valueOf((FISCRMReq.getInName().length() + 4) / 2), 10, 16, 4) + FISCRMReq.getInName());
                    } else {
                        return RMReturnCode.ChineseRowError;
                    }
                    RefString outsName = new RefString(FISCRMReq.getOutName());
                    if (TxFISCBusiness.convertFiscEncode(_defRMOUTT.getRmouttOutName(), outsName)) {
                        FISCRMReq.setOutName(outsName.get());
                        FISCRMReq.setOutName(StringUtil.convertFromAnyBaseString(String.valueOf((FISCRMReq.getOutName().length() + 4) / 2), 10, 16, 4) + FISCRMReq.getOutName());
                    } else {
                        return RMReturnCode.ChineseRowError;
                    }
                    if (StringUtils.isNotBlank(_defRMOUTT.getRmouttMemo().trim())) {
                        // Modify by Jim, 2011/05/25, 不需要判斷第一碼是否為中文, 都需要呼叫轉碼
                        // If Encoding.GetEncoding("Big5").GetByteCount(_defRMOUTT.RMOUTT_MEMO.Substring(0, 1)) = 1 Then
                        // FISCRMReq.CHINESE_MEMO = StringLib.AsciiStringToHex(_defRMOUTT.RMOUTT_MEMO)
                        // LogContext.Remark = "Service11X1-PrepareRM11X1Req, FISCRMReq.CHINESE_MEMO = " & FISCRMReq.CHINESE_MEMO & "; " & PK & "; PCODE = " & FISCRMReq.ProcessingCode
                        // LogMessage(LogLevel.Info, LogContext)

                        // FISCRMReq.CHINESE_MEMO = StringLib.ConvertFromAnyBaseString( _
                        // ((FISCRMReq.CHINESE_MEMO.Length() + 4) / 2).ToString(), 10, 16, 4) & FISCRMReq.CHINESE_MEMO
                        // Else
                        RefString memoRefName = new RefString(FISCRMReq.getChineseMemo());
                        if (TxFISCBusiness.convertFiscEncode(_defRMOUTT.getRmouttMemo(), memoRefName)) {
                            FISCRMReq.setChineseMemo(memoRefName.get());
                            FISCRMReq.setChineseMemo(StringUtil.convertFromAnyBaseString(String.valueOf((FISCRMReq.getChineseMemo().length() + 4) / 2), 10, 16, 4) + FISCRMReq.getChineseMemo());
                        } else {
                            return RMReturnCode.ChineseRowError;
                        }
                        // End If
                    }

                    FISCRMReq.setTxDate(CalendarUtil.adStringToROCString(_defRMOUTT.getRmouttOrgdate()).substring(1, 7));
                    FISCRMReq.setOrgPcode(StringUtils.join("1", _defRMOUTT.getRmouttOrgremtype().trim(), "2"));
                    break;
            }

            // 產生MAC,MB_SYNC,MB_MAC
            encHelper = new ENCHelper(TxFISCData);
            RefString mbMac = new RefString(FISCRMReq.getMbMac());
            RefString mbSyno = new RefString(FISCRMReq.getMbSync());
            rtnCode = encHelper.makeRMMBankMACAndSync(repUNITBANK, mbMac, mbSyno);
            FISCRMReq.setMbMac(mbMac.get());
            FISCRMReq.setMbSync(mbSyno.get());
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            FISCRMReq.setMbMac(StringUtils.leftPad(FISCRMReq.getMbMac(), 8, '0'));
            FISCRMReq.setMbSync(StringUtils.leftPad(FISCRMReq.getMbSync(), 8, '0'));

            encHelper = new ENCHelper(TxFISCData);
            RefString macRef = new RefString(reqMAC);
            rtnCode = encHelper.makeRMFISCMAC(macRef);
            reqMAC = macRef.get();
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            FISCRMReq.setMAC(StringUtils.leftPad(reqMAC, 8, '0'));

            // MakeBitmap
            if (rtnCode == CommonReturnCode.Normal) {
                rtnCode = TxFISCBusiness.makeBitmap(FISCRMReq.getMessageType(), FISCRMReq.getProcessingCode(), MessageFlow.Request);
            }
            SERVICELOGGER.info(StringUtils.join("after MakeBitmap rtnCode=", rtnCode.toString(), StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0')));

            SERVICELOGGER
                    .info(StringUtils.join("Service11X1-PrepareRM11X1Req, FISCRMReq.FISC_NO = [", FISCRMReq.getFiscNo(), "], FISCRMReq.BANK_NO = [", FISCRMReq.getBankNo(), "]"));
            if (rtnCode == CommonReturnCode.Normal) {
                FISCRMReq.makeFISCMsg();
                TxFISCData.setTxRequestMessage(FISCRMReq.getFISCMessage());
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return rtnCode;
    }

    private FEPReturnCode updateRMOUTTForTransfered() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        Rmoutt defRMOUTT = new Rmoutt();

        Rmout defRMOUT = new Rmout();

        try {

            defRMOUTT.setRmouttTxdate(_defRMOUTT.getRmouttTxdate());
            defRMOUTT.setRmouttBrno(_defRMOUTT.getRmouttBrno());
            defRMOUTT.setRmouttOriginal(_defRMOUTT.getRmouttOriginal());
            defRMOUTT.setRmouttFepno(_defRMOUTT.getRmouttFepno());
            defRMOUTT.setRmouttSendtime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
            defRMOUTT.setRmouttSenddate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            defRMOUTT.setRmouttStat(RMOUTStatus.Transfered);

            if (dbRMOUTT.updateByPrimaryKeySelective(defRMOUTT) != 1) {
                rtnCode = IOReturnCode.RMOUTUpdateError;
                return rtnCode;
            }

            defRMOUT.setRmoutTxdate(_defRMOUTT.getRmouttTxdate());
            defRMOUT.setRmoutBrno(_defRMOUTT.getRmouttBrno());
            defRMOUT.setRmoutOriginal(_defRMOUTT.getRmouttOriginal());
            defRMOUT.setRmoutFepno(_defRMOUTT.getRmouttFepno());
            defRMOUT.setRmoutSendtime(defRMOUTT.getRmouttSendtime());
            defRMOUT.setRmoutSenddate(defRMOUTT.getRmouttSenddate());
            defRMOUT.setRmoutStat(RMOUTStatus.Transfered);

            if (dbRMOUT.updateByPrimaryKeySelective(defRMOUT) != 1) {
                rtnCode = IOReturnCode.RMOUTUpdateError;
                return rtnCode;
            }

            _defRMOUTT.setRmouttStat(defRMOUTT.getRmouttStat());

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return rtnCode;
    }

    private FEPReturnCode updateMemory() {

        synchronized (Service11X1MainProcessor.CurrentSendingData) {
            if (!Service11X1MainProcessor.CurrentSendingData.contains(_defRMOUTT.getRmouttFiscsno())) {
                Service11X1MainProcessor.CurrentSendingData.add(_defRMOUTT.getRmouttFiscsno());
                Service11X1MainProcessor.CurrentSendingCnt.incrementAndGet();
                Service11X1MainProcessor.CurrentSendingAmt.getAndAdd(_defRMOUTT.getRmouttTxamt().longValue());
            }
        }

        // Dim dr As DataRow
        // SyncLock MainProcess._Memory
        // dr = MainProcess._Memory.NewRow()

        // dr.Item("FISCSNO") = _defRMOUTT.RMOUTT_FISCSNO

        // If MainProcess._Memory.Select("FISCSNO='" & dr.Item("FISCSNO").ToString() & "'").Length = 0 Then
        // MainProcess._Memory.Rows.Add(dr)
        // 'MainProcess._CurrentSendingCnt = MainProcess._CurrentSendingCnt + 1
        // Interlocked.Increment(MainProcess._CurrentSendingCnt)
        // End If
        // End SyncLock
        return null;
    }

    private FEPReturnCode updateFEPTXN() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        try {
            FepTxn.setFeptxnMsgflow(FISC_Request);
            // FepTxn.FEPTXN_ERR_MF = FepTxn.FEPTXN_MSGFLOW
            // FepTxn.FEPTXN_ERR_RC = FepTxn.FEPTXN_AA_RC.ToString().PadLeft(4, "0"c)
            rtnCode = TxFISCBusiness.updateFepTxnForRM(FepTxn);
            if (rtnCode != CommonReturnCode.Normal) {
                getLogContext().setReturnCode(rtnCode);
                getLogContext().setRemark(StringUtils.join("UpdateFEPTXN not normal, rtnCode=", rtnCode.toString()));
                logMessage(Level.DEBUG, getLogContext());
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return rtnCode;
    }

    private FEPReturnCode updateRMSTAT() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        RmstatExtMapper dbRMSTAT = SpringBeanFactoryUtil.getBean(RmstatExtMapper.class);
        Rmstat defRMSTAT = new Rmstat();

        try {
            // Jim, 2012/2/20, 永豐IT要求, 財金checkin時才update
            if ("1".equals(SysStatus.getPropertyValue().getSysstatAoct1000()) && "1".equals(SysStatus.getPropertyValue().getSysstatMbact1000())) {

                defRMSTAT.setRmstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
                defRMSTAT.setRmstatFiscoFlag1("N");

                if (dbRMSTAT.updateByPrimaryKeySelective(defRMSTAT) < 1) {
                    rtnCode = IOReturnCode.RMSTATUpdateError;
                    TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, TxFISCData.getTxChannel(), getLogContext());
                } else {
                    getLogContext().setRemark("Service11X1-UpdateRMSTAT, UPDATE RMSTAT_FISCO_FLAG1='N'");
                    logMessage(Level.DEBUG, getLogContext());
                }
            } else {
                getLogContext().setRemark("Service11X1-非Check in 狀態，不更新RMSTAT");
                logMessage(Level.DEBUG, getLogContext());
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

        return rtnCode;
    }

    public static class Queue11X1 {
        public String msgFISCNO;
        public String msgSTAT;
        public long msgAMT;

        public String getMsgFISCNO() {
            return msgFISCNO;
        }

        public void setMsgFISCNO(String msgFISCNO) {
            this.msgFISCNO = msgFISCNO;
        }

        public String getMsgSTAT() {
            return msgSTAT;
        }

        public void setMsgSTAT(String msgSTAT) {
            this.msgSTAT = msgSTAT;
        }

        public long getMsgAMT() {
            return msgAMT;
        }

        public void setMsgAMT(long msgAMT) {
            this.msgAMT = msgAMT;
        }
    }

    @Override
    protected String getMDCProfileName() {
        return "Service11X1";
    }
}
