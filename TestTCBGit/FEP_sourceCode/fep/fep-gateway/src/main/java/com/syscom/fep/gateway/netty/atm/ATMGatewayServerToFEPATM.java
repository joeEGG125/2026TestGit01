package com.syscom.fep.gateway.netty.atm;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.exception.FEPBaseException;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.roundrobin.RoundRobin;
import com.syscom.fep.frmcommon.scheduler.AbstractScheduledTask;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SocketUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.entity.AtmStatus;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.ToFEPATM;
import com.syscom.fep.invoker.SimpleNettyClientFactory;
import com.syscom.fep.vo.communication.*;
import com.syscom.fep.vo.enums.ClientType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 送訊息給ATM Service的物件
 *
 * @author Richard
 */
public class ATMGatewayServerToFEPATM extends FEPBase {
    @Autowired
    private ATMGatewayServerConfiguration configuration;
    @Autowired
    private SimpleNettyClientFactory factory;
    private ATMServiceCheckTimer timer = null;
    private RoundRobin<ToFEPATM> roundRobin = null;
    private final AtomicReference<ToFEPATM> toFEPATM = new AtomicReference<>(); // 用來臨時設定固定送哪台ATM Service
    private static ATMGatewayServerToFEPATM instance = null;

    public static ATMGatewayServerToFEPATM getInstance() {
        if (instance == null) {
            instance = new ATMGatewayServerToFEPATM();
            instance.initialize();
        }
        return instance;
    }

    /**
     * 非SpringBean下初始化
     */
    private void initialize() {
        this.configuration = SpringBeanFactoryUtil.getBean(ATMGatewayServerConfiguration.class);
        SimpleNettyClientFactory.registerToFEPATMComponents(); // 2026-03-17 Richard add 這裡提前註冊ToFEPATM元件
        this.factory = SpringBeanFactoryUtil.getBean(SimpleNettyClientFactory.class);
        this.postConstruct();
    }

    @PostConstruct
    public void postConstruct() {
        roundRobin = new RoundRobin<>(this.configuration.getTofepatm());
        // 開始診測所有的ATM Service是否可以正常連線
        timer = new ATMServiceCheckTimer();
        timer.scheduleAtFixedRate(0, this.configuration.getCheckAtmServiceInterval(), TimeUnit.SECONDS);
    }

    @PreDestroy
    public void preDestroy() {
        if (timer != null) {
            timer.destroy();
        }
    }

    /**
     * 透過ATM Service依據ATM IP從ATM主檔獲取資料
     *
     * @param logData
     * @param atmIp
     * @param timeout
     * @return
     */
    public ToATMCommuAtmmstr getAtmmstrByAtmIp(LogData logData, String atmIp, int timeout) {
        ToFEPATMCommuAtmmstr request = new ToFEPATMCommuAtmmstr();
        request.setAtmIp(atmIp);
        return this.getAtmmstr(logData, request, timeout);
    }

    /**
     * 透過ATM Service依據ATM NO從ATM主檔獲取資料
     *
     * @param logData
     * @param atmNo
     * @param timeout
     * @return
     */
    public ToATMCommuAtmmstr getAtmmstrByAtmNo(LogData logData, String atmNo, int timeout) {
        ToFEPATMCommuAtmmstr request = new ToFEPATMCommuAtmmstr();
        request.setAtmNo(atmNo);
        return this.getAtmmstr(logData, request, timeout);
    }

    /**
     * 透過ATM Service從ATM主檔獲取資料
     *
     * @param logData
     * @param request
     * @param timeout
     * @return
     */
    private ToATMCommuAtmmstr getAtmmstr(LogData logData, ToFEPATMCommuAtmmstr request, int timeout) {
        try {
            String response = this.sendReceive(logData, "getAtmmstr", request, timeout);
            ToATMCommuAtmmstr toATMCommuAtmmstr = ToATMCommuAtmmstr.fromXML(response);
            if (toATMCommuAtmmstr.getCode() == FEPReturnCode.Normal) {
                return toATMCommuAtmmstr;
            } else {
                throw ExceptionUtil.createException(toATMCommuAtmmstr.getErrmsg());
            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".getAtmmstr"));
            FEPBase.sendEMS(logData);
        }
        return null;
    }

    /**
     * 透過ATM Service從ATM主檔獲取資料
     *
     * @param logData
     * @param atmFepConnection
     * @param timeout
     * @return
     */
    public ToATMCommuAtmmstrList getAtmmstrList(LogData logData, Short atmFepConnection, int timeout) {
        ToFEPATMCommuAtmmstr request = new ToFEPATMCommuAtmmstr();
        request.setAtmFepConnection(atmFepConnection);
        try {
            String response = this.sendReceive(logData, "getAtmmstrList", request, timeout);
            ToATMCommuAtmmstrList toATMCommuAtmmstrList = ToATMCommuAtmmstrList.fromXML(response);
            if (toATMCommuAtmmstrList.getCode() == FEPReturnCode.Normal) {
                return toATMCommuAtmmstrList;
            } else {
                throw ExceptionUtil.createException(toATMCommuAtmmstrList.getErrmsg());
            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".getAtmmstrList"));
            FEPBase.sendEMS(logData);
        }
        return null;
    }

    /**
     * 更新ATM主檔
     *
     * @param logData
     * @param request
     * @param timeout
     * @return
     */
    public int updateAtmmstr(LogData logData, ToFEPATMCommuUpdateAtmmstr request, int timeout) {
        try {
            String response = this.sendReceive(logData, "updateAtmmstr", request, timeout);
            ToGWCommuDbOptResult toGWCommuDbOptResult = ToGWCommuDbOptResult.fromXML(response);
            if (toGWCommuDbOptResult.getCode() == FEPReturnCode.Normal) {
                return toGWCommuDbOptResult.getResult();
            } else {
                throw ExceptionUtil.createException(toGWCommuDbOptResult.getErrmsg());
            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".updateAtmmstr"));
            sendEMS(logData);
        }
        return -1;
    }

    /**
     * 更新ATM狀態檔
     *
     * @param logData
     * @param request
     * @param timeout
     * @return
     */
    public int updateAtmstat(LogData logData, ToFEPATMCommuUpdateAtmstat request, int timeout) {
        try {
            String response = this.sendReceive(logData, "updateAtmstat", request, timeout);
            ToGWCommuDbOptResult toGWCommuDbOptResult = ToGWCommuDbOptResult.fromXML(response);
            if (toGWCommuDbOptResult.getCode() == FEPReturnCode.Normal) {
                return toGWCommuDbOptResult.getResult();
            } else {
                throw ExceptionUtil.createException(toGWCommuDbOptResult.getErrmsg());
            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".updateAtmstat"));
            sendEMS(logData);
        }
        return -1;
    }

    /**
     * 獲取ATM狀態檔資料
     *
     * @param logData
     * @param atmStatus
     * @return
     * @throws Exception
     */
    public ToATMCommuAtmstatList getAtmstatList(LogData logData, AtmStatus atmStatus, boolean onlyFetchCount) throws Exception {
        try {
            ToFEPATMCommuAtmstatList request = new ToFEPATMCommuAtmstatList();
            if (atmStatus != null) {
                request.setAtmstatStatus(atmStatus.getValue());
            }
            request.setOnlyFetchCount(onlyFetchCount);
            request.setAtmAtmpIp(this.configuration.getHost()); // 2025-06-02 Richard add
            String response = this.sendReceive(logData, "getAtmstatList", request, 120000);
            ToATMCommuAtmstatList toATMCommuAtmstatList = ToATMCommuAtmstatList.fromXML(response);
            if (toATMCommuAtmstatList.getCode() == FEPReturnCode.Normal) {
                return toATMCommuAtmstatList;
            } else {
                throw ExceptionUtil.createException(toATMCommuAtmstatList.getErrmsg());
            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".getAtmstatList"));
            sendEMS(logData);
            throw e;
        }
    }

    /**
     * 從Sysconf檔獲取資料
     *
     * @param logData
     * @param sysconfSubsysno
     * @param sysconfName
     * @param timeout
     * @return
     */
    public String getSysconfValue(LogData logData, Short sysconfSubsysno, String sysconfName, int timeout) {
        try {
            ToFEPCommuSysconf request = new ToFEPCommuSysconf();
            request.setSysconfSubsysno(sysconfSubsysno);
            request.setSysconfName(sysconfName);
            String response = this.sendReceive(logData, "getSysconfValue", request, timeout);
            ToGWCommuSysconf toGWCommuSysconf = ToGWCommuSysconf.fromXML(response);
            if (toGWCommuSysconf.getCode() == FEPReturnCode.Normal) {
                return toGWCommuSysconf.getSysconfValue();
            } else {
                throw ExceptionUtil.createException(toGWCommuSysconf.getErrmsg());
            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".getSysconfValue"));
            sendEMS(logData);
        }
        return null;
    }

    /**
     * 批量更新ATM狀態檔
     *
     * @param logData
     * @param request
     * @param timeout
     * @return
     */
    public int updateAtmstatBatch(LogData logData, ToFEPATMCommuUpdateAtmstatBatch request, int timeout) {
        try {
            String response = this.sendReceive(logData, "updateAtmstatBatch", request, timeout);
            ToGWCommuDbOptResult toGWCommuDbOptResult = ToGWCommuDbOptResult.fromXML(response);
            if (toGWCommuDbOptResult.getCode() == FEPReturnCode.Normal) {
                return toGWCommuDbOptResult.getResult();
            } else {
                throw ExceptionUtil.createException(toGWCommuDbOptResult.getErrmsg());
            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".updateAtmstatBatch"));
            sendEMS(logData);
        }
        return -1;
    }

    /**
     * 用輪播的方式, 送訊息給ATM Service並接收到回應
     *
     * @param logData
     * @param methodName
     * @param request
     * @param timeout
     * @return
     * @throws Exception
     */
    public String sendReceive(LogData logData, String methodName, BaseCommu request, int timeout) throws Exception {
        String messageFrom = StringUtils.EMPTY;
        String messageTo = request.toString();
        while (true) {
            ToFEPATM toFEPATM = this.getNextServerATMInfo();
            if (toFEPATM != null) {
                request.setHost(toFEPATM.getHost());
                request.setPort(toFEPATM.getPort());
                // logger
                logData.setSubSys(SubSystem.GW);
                logData.setChannel(FEPChannel.ATM);
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setProgramFlowType(ProgramFlow.ATMGatewayOut);
                logData.setProgramName(StringUtils.join(ProgramName, ".", methodName));
                logData.setRemark(StringUtils.join("ATM Gateway Send Data to ATM Service, host:", request.getHost(), ",port:", request.getPort(), ",timeout:", timeout));
                logData.setMessage(messageTo);
                this.logMessage(logData);
                try {
                    //factory.setCorrelationKey(java.util.UUID.randomUUID().toString());
                    messageFrom = factory.sendReceive(ClientType.TO_FEP_ATM, request, timeout);
                    // logger
                    logData.setSubSys(SubSystem.GW);
                    logData.setChannel(FEPChannel.ATM);
                    logData.setMessageFlowType(MessageFlow.Response);
                    logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
                    logData.setProgramName(StringUtils.join(ProgramName, ".", methodName));
                    logData.setRemark(StringUtils.join("ATM Gateway Receive Data from ATM Service, host:", request.getHost(), ",port:", request.getPort(), ",timeout:", timeout));
                    logData.setMessage(messageFrom);
                    this.logMessage(logData);
                    break;
                } catch (Exception e) {
                    FEPReturnCode rtnCode = FEPReturnCode.ProgramException;
                    if (e instanceof FEPBaseException) {
                        FEPBaseException fepBaseException = (FEPBaseException) e;
                        if (fepBaseException.getRtnCode() != null) {
                            rtnCode = fepBaseException.getRtnCode();
                        }
                    }
                    // 發生例外時需sendEMS
                    logData.setProgramName(StringUtils.join(ProgramName, ".", methodName));
                    logData.setProgramException(e);
                    logData.setMessage(messageTo);
                    logData.setReturnCode(rtnCode);
                    logData.setRemark(StringUtils.join("ATM Gateway Send Data to ATM Service failed, host:", request.getHost(), ",port:", request.getPort(), ",timeout:", timeout));
                    logMessage(Level.WARN, logData);
                    if (rtnCode == FEPReturnCode.CanNotConnectRemoteHost) {
                        // 若取出的fep-server-atm連接失敗時, 將此組參數從enabled設為false
                        toFEPATM.setEnable(false);
                        // 並再次呼叫GetNextServerATMInfo取出ATMService參數物件
                        continue;
                    }
                    throw e;
                }
            } else {
                // 若取不到時代表所有的fep-server-atm目前都無法連線, SendEMS(“無法取得fep-server-atm連線資訊”)
                Exception e = ExceptionUtil.createException("無法取得FEP-SERVER-ATM連線資訊");
                logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
                logData.setProgramException(e);
                logData.setMessage(messageTo);
                logData.setReturnCode(FEPReturnCode.ProgramException);
                logData.setRemark("無法取得FEP-SERVER-ATM連線資訊");
                sendEMS(logData);
                throw e;
            }
        }
        return messageFrom;
    }

    /**
     * 呼叫GetNextServerATMInfo取出ATMService參數物件
     *
     * @return
     */
    private synchronized ToFEPATM getNextServerATMInfo() {
        if (roundRobin.size() == 0)
            throw ExceptionUtil.createUnsupportedOperationException("List is empty.");
        // 如果有設定固定的值, 則取固定的ATM Service
        if (this.toFEPATM.get() != null)
            return this.toFEPATM.get();
        int retryCnt = 0;
        // retry至每一台都連不上為止
        while (retryCnt < roundRobin.size()) {
            ToFEPATM toFEPATM = roundRobin.select();
            if (toFEPATM.isEnable()) {
                return toFEPATM;
            }
            retryCnt++;
        }
        return null;
    }

    /**
     * 用來診測所有的ATM Service是否可以正常連線
     *
     * @author Richard
     */
    private class ATMServiceCheckTimer extends AbstractScheduledTask {
        private final LogData logData = new LogData();

        public ATMServiceCheckTimer() {
            super("ATMGatewayServer-ATMServiceCheckTimer");
        }

        /**
         * Execute Task
         */
        @Override
        public void execute() {
            LogMDC.put(Const.MDC_PROFILE, Gateway.ATMGW.name());
            if (toFEPATM.get() != null) {
                detect(toFEPATM.get());
            } else {
                // 偵測所有不在FEP_SERVER_ATMS的fep-server-atm參數是否可以連接成功
                for (ToFEPATM toFEPATM : configuration.getTofepatm()) {
                    detect(toFEPATM);
                }
            }
            // 如果全部都可以連線成功, 則logData.clear
            if (configuration.getTofepatm().stream().filter(ToFEPATM::isEnable).count() == configuration.getTofepatm().size()) {
                this.logData.clear();
            }
        }

        private void detect(ToFEPATM toFEPATM) {
            // 只檢測無法連線的
            if (toFEPATM.isEnable())
                return;
            this.logData.setProgramName(StringUtils.join(this.taskName, ".execute"));
            this.logData.setRemark(StringUtils.join("Try to detect ATM Service [host:", toFEPATM.getHost(), ",port:", toFEPATM.getPort(), "] Connective"));
            logMessage(Level.WARN, this.logData);
            if (SocketUtil.isTcpAvailable(toFEPATM.getHost(), toFEPATM.getPort())) {
                // 若可以, 則將該組參數enabled設回true
                toFEPATM.setEnable(true);
                this.logData.setProgramName(StringUtils.join(this.taskName, ".execute"));
                this.logData.setRemark(StringUtils.join("ATM Service [host:", toFEPATM.getHost(), ",port:", toFEPATM.getPort(), "] is Connectable, and set Enable = [", toFEPATM.isEnable(), "]"));
                logMessage(this.logData);
            }
        }
    }

    /**
     * 如果host是IP字串, 則從this.configuration.getTofepatm()中找出對應的數據
     * <p>
     * 如果host是int類型, 則取this.configuration.getTofepatm().get(host)
     *
     * @param host
     * @return
     */
    public String setToFEPATM(String host) {
        LogData logData = new LogData();
        logData.setProgramName(StringUtils.join(ProgramName, ".setToFEPATMHost"));
        logData.setRemark(StringUtils.join("Change FEPAP according to host:", host));
        logMessage(logData);
        if (StringUtils.isBlank(host)) {
            this.toFEPATM.set(null);
        } else {
            if (StringUtils.isNumeric(host)) {
                int index = Integer.parseInt(host);
                if (index >= 0 && index < configuration.getTofepatm().size()) {
                    this.toFEPATM.set(this.configuration.getTofepatm().get(index));
                } else {
                    this.toFEPATM.set(null);
                }
            } else {
                this.toFEPATM.set(this.configuration.getTofepatm().stream().filter(t -> t.getHost().equals(host)).findFirst().orElse(null));
            }
        }
        logData.setProgramName(StringUtils.join(ProgramName, ".setToFEPATMHost"));
        logData.setRemark(StringUtils.join("Set ToFEPATM ", this.toFEPATM.get() == null ? "[null]" : StringUtils.join("[host:", this.toFEPATM.get().getHost(), ",port:", this.toFEPATM.get().getPort(), "]")));
        logMessage(logData);
        return this.getToFEPATM();
    }

    public String getToFEPATM() {
        ToFEPATM toFEPATM = this.toFEPATM.get();
        if (toFEPATM != null) {
            return StringUtils.join("All Message from ATM will forward to ATM Service [", toFEPATM.getHost(), ":", toFEPATM.getPort(), "]");
        }
        return "All Message from ATM will forward to ATM Service by RoundRobin";
    }
}
