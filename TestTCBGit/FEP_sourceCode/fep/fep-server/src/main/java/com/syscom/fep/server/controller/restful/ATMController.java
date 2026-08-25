package com.syscom.fep.server.controller.restful;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.GWConfig;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.delegate.MessageAsynchronousWaitReceiver;
import com.syscom.fep.frmcommon.delegate.MessageAsynchronousWaitReceiverManager;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.thread.ThreadPoolFactory;
import com.syscom.fep.frmcommon.thread.ThreadWrapper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.AtmmstrExtMapper;
import com.syscom.fep.mybatis.ext.mapper.AtmstatExtMapper;
import com.syscom.fep.mybatis.model.Atmmstr;
import com.syscom.fep.mybatis.model.Atmstat;
import com.syscom.fep.mybatis.util.DB2Util;
import com.syscom.fep.server.common.handler.ATMHandlerP2;
import com.syscom.fep.server.controller.BaseController;
import com.syscom.fep.server.controller.configuration.ATMConfiguration;
import com.syscom.fep.vo.communication.*;
import com.syscom.fep.vo.communication.ToATMCommuAtmstatList.ToATMCommuAtmstat;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 接收來自ATM GW的Restful/Socket請求
 *
 * @author Richard
 */
@StackTracePointCut(caller = SvrConst.SVR_ATM)
public class ATMController extends BaseController {
    @Autowired
    private AtmmstrExtMapper atmmstrMapper;
    @Autowired
    private AtmstatExtMapper atmstatMapper;
    // 配置檔對應物件
    private static ATMConfiguration configuration;
    // 執行緒池
    private static ExecutorService executor;
    /**
     * AppDynamics使用的關聯key
     */
    private String correlationKey;

    @PostConstruct
    public void postConstruct() {
        configuration = SpringBeanFactoryUtil.registerBean(ATMConfiguration.class);
        // 2025-08-14 Richard add
        // 只有設定是異步處理, 才需要建Thread Pool
        if (configuration.isTxProcessAsync()) {
            LogHelperFactory.getGeneralLogger().info("Create Executor Pool, size:", configuration.getExecutorCorePoolSize(), ", keepAliveTime:", configuration.getExecutorKeepAliveTime(), ", queueCapacity:", configuration.getExecutorQueueCapacity());
            executor = ThreadPoolFactory.newFixedThreadPool(
                    configuration.getExecutorCorePoolSize(),
                    configuration.getExecutorKeepAliveTime(),
                    TimeUnit.MILLISECONDS,
                    configuration.getExecutorQueueCapacity(),
                    new SimpleThreadFactory(getName()),
                    new ThreadPoolExecutor.CallerRunsPolicy());
        }
    }

    @PreDestroy
    public void preDestroy() {
        ThreadPoolFactory.shutdown(executor, getName());
    }

    @Override
    public String getName() {
        return SvrConst.SVR_ATM;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    /**
     * 接口方法
     *
     * @param messageIn
     * @return
     */
    @RequestMapping(value = "/recv/atm", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sendReceive(@RequestBody String messageIn) {
        // Restful進來的電文
        return this.processRequestData(ProgramFlow.RESTFulIn, messageIn);
    }

    /**
     * 處理進來的電文並回應
     *
     * @param programFlow
     * @param messageIn
     * @return
     */
    @Override
    protected String processRequestData(final ProgramFlow programFlow, final String messageIn) {
        // Just for test
        if (Boolean.parseBoolean(System.getProperty("fep.server.atm.socket.test"))) {
            return sendReceiveSocketTest(programFlow, messageIn);
        }
        LogMDC.put(Const.MDC_PROFILE, this.getName());
        LogHelperFactory.getTraceLogger().trace(this.getName(), " Receive Message:", messageIn);
        LogData logData = new LogData();
        String messageOut = StringUtils.EMPTY;
        try {
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }
            String unescapeXml = StringEscapeUtils.unescapeXml(messageIn); // 2025-06-24 Richard add這裡轉義一下避免下面fromXML方法出現異常
            Object commu = BaseXmlCommu.fromXML(unescapeXml);
            // 為null, 說明電文xml中沒有<classname />, 則預設按照ToFEPATMCommu來轉
            if (commu == null) {
                commu = ToFEPATMCommu.fromXML(unescapeXml, ToFEPATMCommu.class);
            }
            // ATMGW進來的交易類電文, 收到的是ToFEPATMCommu對應的XML字串, 送出的是ToATMCommu對應的XML字串
            if (commu instanceof ToFEPATMCommu) {
                // Just for test
                if (Boolean.parseBoolean(System.getProperty("fep.server.atm.process.test"))) {
                    return sendReceiveToFEPATMCommuTest();
                }
                messageOut = this.processRequestData(programFlow, logData, (ToFEPATMCommu) commu);
            }
            // ATMGW進來的沒有請求參數查詢資料的電文, 收到的是ToFEPCommuAction對應的XML字串, 送出的是ToGWCommuAction對應的XML字串
            else if (commu instanceof ToFEPCommuAction) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPCommuAction) commu);
            }
            //  ATMGW進來的更新ATM主檔的電文, 收到的是ToFEPATMCommuUpdateAtmmstr對應的XML字串, 送出的是ToGWCommuDbOptResult對應的XML字串
            else if (commu instanceof ToFEPATMCommuUpdateAtmmstr) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPATMCommuUpdateAtmmstr) commu);
            }
            // ATMGW進來的更新Atmstat的電文, 收到的是ToFEPATMCommuUpdateAtmstat對應的XML字串, 送出的是ToGWCommuDbOptResult對應的XML字串
            else if (commu instanceof ToFEPATMCommuUpdateAtmstat) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPATMCommuUpdateAtmstat) commu);
            }
            // ATMGW進來的查詢Atmmstr的電文, 收到的是ToFEPATMCommuAtmmstr對應的XML字串, 送出的是ToATMCommuAtmmstr對應的XML字串
            else if (commu instanceof ToFEPATMCommuAtmmstr) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPATMCommuAtmmstr) commu);
            }
            // ATMGW進來查詢Zone資料的電文, 收到的是ToFEPCommuZone對應的XML字串, 送出的是ToGWCommuZone對應的XML字串
            else if (commu instanceof ToFEPCommuZone) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPCommuZone) commu);
            }
            // ATMGW進來查詢Config資料的電文, 收到的是ToFEPCommuConfig對應的XML字串, 送出的是ToGWCommuConfig對應的XML字串
            else if (commu instanceof ToFEPCommuConfig) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPCommuConfig) commu);
            }
            // ATMGW進來查詢Atmstat資料的電文, 收到的是ToFEPATMCommuAtmstatList對應的XML字串, 送出的是ToATMCommuAtmstatList對應的XML字串
            else if (commu instanceof ToFEPATMCommuAtmstatList) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPATMCommuAtmstatList) commu);
            }
            // ATMGW進來的查詢Sysconf資料的電文, 收到的是ToFEPCommuSysconf對應的XML字串, 送出的是ToGWCommuSysconf對應的XML字串
            else if (commu instanceof ToFEPCommuSysconf) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPCommuSysconf) commu);
            }
            // ATMGW進來批量更新Atmstat的電文, 收到的是ToFEPATMCommuUpdateAtmstatBatch對應的XML字串, 送出的是ToGWCommuDbOptResult對應的XML字串
            else if (commu instanceof ToFEPATMCommuUpdateAtmstatBatch) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPATMCommuUpdateAtmstatBatch) commu);
            }
            // 視情況是否需要補充
            else {
                throw ExceptionUtil.createIllegalArgumentException("無效的請求, ", commu.getClass().getName());
            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            logData.setRemark(e.getMessage());
            logData.setMessage(messageIn);
            sendEMS(logData);
        } finally {
            if (StringUtils.isNotBlank(messageOut)) {
                LogHelperFactory.getTraceLogger().trace(this.getName(), " Send Message: ", messageOut);
            } else {
                LogHelperFactory.getTraceLogger().trace(this.getName(), " Send Empty Message");
            }
        }
        return messageOut;
    }

    /**
     * ATMGW進來的交易類電文, 收到的是ToFEPATMCommu對應的XML字串, 送出的是ToATMCommu對應的XML字串
     *
     * @param programFlow
     * @param logData
     * @param toFEPATMCommu
     * @return
     */
    private String processRequestData(final ProgramFlow programFlow, final LogData logData, final ToFEPATMCommu toFEPATMCommu) throws Exception {
        String messageOut;
        String responseFromAA = null;
        logData.setMessageCorrelationId(UUID.randomUUID().toString());
        logData.setProgramFlowType(programFlow);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        logData.setAtmNo(toFEPATMCommu.getAtmno());
        logData.setEj(Integer.parseInt(toFEPATMCommu.getEj()));
        logData.setMessage(toFEPATMCommu.getMessage());
        logData.setTxRquid(toFEPATMCommu.getTxRquid());
        logData.setRemark(
                StringUtils.join(this.getName(), " Get ATM Request",
                        " EJ:", logData.getEj(),
                        " ATMNo:", logData.getAtmNo(),
                        " ATMSeq:", logData.getAtmSeq(),
                        " MessageId:", logData.getMessageId(),
                        " Step:", logData.getStep(),
                        " TxRquid:", logData.getTxRquid())
        );
        this.logMessage(logData);
        try {
            // Call Handler
            final ATMHandlerP2 atmHandler = new ATMHandlerP2();
            atmHandler.setEj(logData.getEj());
            atmHandler.setMessageId(logData.getMessageId());
            atmHandler.setAtmNo(logData.getAtmNo());
            atmHandler.setAtmSeq(logData.getAtmSeq());
            atmHandler.setLogContext(logData);
            atmHandler.setMessageCorrelationId(logData.getMessageCorrelationId());
            atmHandler.setTxRquid(logData.getTxRquid());
            // 這裡的callback是for ATMAdapter回response給ATM的
            // 因為有一種情形，ATMHandler在處理完後，直接由ATMAdapter送response，所以這裡的callback要接一下
            final MessageAsynchronousWaitReceiver<String, String> callback = new MessageAsynchronousWaitReceiver<>(logData.getMessageCorrelationId(), String.class);
            MessageAsynchronousWaitReceiverManager.subscribe(this, callback);
            boolean waitSucceed = true;
            // 2025-08-14 Richard modified
            // if (toFEPATMCommu.isSync()) {
            // executor為null, 說明同步處理
            if (executor == null) {
                this.handlerDispatch(atmHandler, toFEPATMCommu.getMessage(), callback);
            } else {
                Future<Boolean> future = executor.submit(() -> {
                    // 因為是新起了一個線程在跑，所以這裡必須要再塞入一次
                    LogMDC.put(Const.MDC_PROFILE, this.getName());
                    this.handlerDispatch(atmHandler, toFEPATMCommu.getMessage(), callback);
                    // Wait for ATMAdapter callback or Handler return response
                    int timeout = GWConfig.getInstance().getAATimeout() * 1000;
                    return callback.waitMessage(this, timeout);
                });
                // wait future completely
                try {
                    waitSucceed = future.get();
                } catch (Exception e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
            }
            responseFromAA = callback.getMessage();
            if (!waitSucceed)
                throw ExceptionUtil.createSocketTimeoutException(this.getName(), " Invoke AA Timeout");
            else if (StringUtils.isBlank(responseFromAA)) {
                // throw ExceptionUtil.createException(this.getName(), " AA Response is null");
                logData.setRemark(StringUtils.join(this.getName(), " AA Response is null"));
            } else {
                logData.setRemark(StringUtils.join(this.getName(), " Get ATM Response OK"));
            }
            logData.setProgramFlowType(this.getOutProgramFlow(programFlow));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            logData.setMessage(responseFromAA);
            this.logMessage(logData);
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramFlowType(programFlow);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            logData.setRemark(StringUtils.join(this.getName(), " Get ATM Response with exception occur, ", e.getMessage()));
            logData.setMessage(toFEPATMCommu.getMessage());
            this.logMessage(logData);
            throw e;
        } finally {
            // unsubscribe
            MessageAsynchronousWaitReceiverManager.unsubscribe(this, logData.getMessageCorrelationId(), String.class);
            // ToATMCommu
            ToATMCommu toATMCommu = new ToATMCommu();
            toATMCommu.setAtmno(toFEPATMCommu.getAtmno());
            toATMCommu.setEj(toFEPATMCommu.getEj());
            toATMCommu.setTxRquid(toFEPATMCommu.getTxRquid());
            toATMCommu.setMessage(responseFromAA);
            messageOut = toATMCommu.toString();
        }
        return messageOut;
    }

    private void handlerDispatch(ATMHandlerP2 atmHandler, String request, MessageAsynchronousWaitReceiver<String, String> callback) {
        String atmNo = atmHandler.getAtmNo();
        String response = atmHandler.dispatch(FEPChannel.ATM, atmNo, request);
        callback.messageArrived(this, response);
    }

    /**
     * ATMGW進來的更新ATM主檔的電文, 收到的是ToFEPATMCommuUpdateAtmmstr對應的XML字串, 送出的是ToGWCommuDbOptResult對應的XML字串
     *
     * @param programFlow
     * @param logData
     * @param toFEPATMCommuUpdateAtmmstr
     * @return
     */
    private String processRequestData(ProgramFlow programFlow, LogData logData, ToFEPATMCommuUpdateAtmmstr toFEPATMCommuUpdateAtmmstr) {
        loggingLogData(programFlow, MessageFlow.Request, logData, toFEPATMCommuUpdateAtmmstr);
        ToGWCommuDbOptResult toGWCommuDbOptResult = new ToGWCommuDbOptResult();
        Atmmstr atmmstr = new Atmmstr();
        atmmstr.setAtmAtmno(toFEPATMCommuUpdateAtmmstr.getAtmAtmno());
        atmmstr.setAtmAtmpIp(toFEPATMCommuUpdateAtmmstr.getAtmAtmpIp());
        atmmstr.setAtmAtmpPort(toFEPATMCommuUpdateAtmmstr.getAtmAtmpPort());
        atmmstr.setAtmCertalias(toFEPATMCommuUpdateAtmmstr.getAtmCertalias());
        try {
            int result = atmmstrMapper.updateAtmmstrByAtmNoSelective(atmmstr);
            toGWCommuDbOptResult.setResult(result);
        } catch (Exception e) {
            handleException(programFlow, logData, toGWCommuDbOptResult, e);
        }
        loggingLogData(programFlow, MessageFlow.Response, logData, toGWCommuDbOptResult);
        return toGWCommuDbOptResult.toString();
    }

    /**
     * ATMGW進來的更新Atmstat的電文, 收到的是ToFEPATMCommuUpdateAtmstat對應的XML字串, 送出的是ToGWCommuDbOptResult對應的XML字串
     *
     * @param programFlow
     * @param logData
     * @param toFEPATMCommuUpdateAtmstat
     * @return
     */
    private String processRequestData(ProgramFlow programFlow, LogData logData, ToFEPATMCommuUpdateAtmstat toFEPATMCommuUpdateAtmstat) {
        loggingLogData(programFlow, MessageFlow.Request, logData, toFEPATMCommuUpdateAtmstat);
        ToGWCommuDbOptResult toGWCommuDbOptResult = new ToGWCommuDbOptResult();
        try {
            Atmstat atmstat = new Atmstat();
            atmstat.setAtmstatAtmno(toFEPATMCommuUpdateAtmstat.getAtmstatAtmno());
            atmstat.setAtmstatStatus(toFEPATMCommuUpdateAtmstat.getAtmstatStatus());
            atmstat.setAtmstatSocket(toFEPATMCommuUpdateAtmstat.getAtmstatSocket());
            atmstat.setAtmstatSec(toFEPATMCommuUpdateAtmstat.getAtmstatSec());
            atmstat.setAtmstatInikey(toFEPATMCommuUpdateAtmstat.getAtmstatInikey());
            atmstat.setAtmstatApVersionN(toFEPATMCommuUpdateAtmstat.getAtmstatApVersionN());
            int result = atmstatMapper.updateAtmstatByAtmAtmpIp(atmstat, toFEPATMCommuUpdateAtmstat.getAtmAtmpIp());
            toGWCommuDbOptResult.setResult(result);
        } catch (Exception e) {
            handleException(programFlow, logData, toGWCommuDbOptResult, e);
        }
        loggingLogData(programFlow, MessageFlow.Response, logData, toGWCommuDbOptResult);
        return toGWCommuDbOptResult.toString();
    }

    /**
     * ATMGW進來的查詢Atmmstr的電文, 收到的是ToFEPATMCommuAtmmstr對應的XML字串, 送出的是ToATMCommuAtmmstr對應的XML字串
     *
     * @param programFlow
     * @param logData
     * @param toFEPATMCommuAtmmstr
     * @return
     */
    private String processRequestData(ProgramFlow programFlow, LogData logData, ToFEPATMCommuAtmmstr toFEPATMCommuAtmmstr) {
        loggingLogData(programFlow, MessageFlow.Request, logData, toFEPATMCommuAtmmstr);
        BaseCommu response = null;
        // 查詢by ATM NO
        if (StringUtils.isNotBlank(toFEPATMCommuAtmmstr.getAtmNo())) {
            response = new ToATMCommuAtmmstr();
        }
        // 查詢by ATM IP
        else if (StringUtils.isNotBlank(toFEPATMCommuAtmmstr.getAtmIp())) {
            response = new ToATMCommuAtmmstr();
        }
        // 查詢by FEP Connection
        else if (toFEPATMCommuAtmmstr.getAtmFepConnection() != null) {
            response = new ToATMCommuAtmmstrList();
        }
        try {
            Map<String, Object> atmmstrMap = null;
            List<Map<String, Object>> atmmstrMaps = null;
            // 查詢by ATM NO
            if (StringUtils.isNotBlank(toFEPATMCommuAtmmstr.getAtmNo())) {
                atmmstrMap = atmmstrMapper.getAtmmstrByAtmNo(toFEPATMCommuAtmmstr.getAtmNo());
            }
            // 查詢by ATM IP
            else if (StringUtils.isNotBlank(toFEPATMCommuAtmmstr.getAtmIp())) {
                atmmstrMap = atmmstrMapper.getAtmmstrByAtmIp(toFEPATMCommuAtmmstr.getAtmIp());
            }
            // 查詢by FEP Connection
            else if (toFEPATMCommuAtmmstr.getAtmFepConnection() != null) {
                atmmstrMaps = atmmstrMapper.getAtmmstrByAtmFepConnection(toFEPATMCommuAtmmstr.getAtmFepConnection() == (short) 0 || toFEPATMCommuAtmmstr.getAtmFepConnection() == (short) 1 ? toFEPATMCommuAtmmstr.getAtmFepConnection() : null);
            }
            // 查詢主檔一筆資料
            if (MapUtils.isNotEmpty(atmmstrMap)) {
                ((ToATMCommuAtmmstr)response).setAtmAtmno((String) atmmstrMap.get("ATM_ATMNO"));
                ((ToATMCommuAtmmstr)response).setAtmZone((String) atmmstrMap.get("ATM_ZONE"));
                ((ToATMCommuAtmmstr)response).setAtmCheckMac(DbHelper.toBoolean(((Integer) atmmstrMap.get("ATM_CHECK_MAC")).shortValue()));
                ((ToATMCommuAtmmstr)response).setAtmstatSec(((BigDecimal) atmmstrMap.get("ATMSTAT_SEC")).shortValue());
                ((ToATMCommuAtmmstr)response).setAtmstatSocket(((BigDecimal) atmmstrMap.get("ATMSTAT_SOCKET")).intValue());
                ((ToATMCommuAtmmstr)response).setAtmstatInikey(((BigDecimal) atmmstrMap.get("ATMSTAT_INIKEY")).intValue());
                ((ToATMCommuAtmmstr)response).setAtmIp((String) atmmstrMap.get("ATM_IP"));
                ((ToATMCommuAtmmstr)response).setAtmAtmpPort((String) atmmstrMap.get("ATM_ATMP_PORT"));
                ((ToATMCommuAtmmstr)response).setAtmCertAlias((String) atmmstrMap.get("ATM_CERTALIAS"));
                ((ToATMCommuAtmmstr)response).setAtmFepConnection(((Integer) atmmstrMap.get("ATM_FEP_CONNECTION")).shortValue());
            }
            // 查詢主檔多筆資料
            else if (CollectionUtils.isNotEmpty(atmmstrMaps)) {
                response = new ToATMCommuAtmmstrList();
                ((ToATMCommuAtmmstrList) response).setAtmmstrs(new ArrayList<>());
                for (Map<String, Object> map : atmmstrMaps) {
                    ToATMCommuAtmmstrList.ToATMCommuAtmmstr toATMCommuAtmmstr = new ToATMCommuAtmmstrList.ToATMCommuAtmmstr();
                    toATMCommuAtmmstr.setAtmAtmno((String) map.get("ATM_ATMNO"));
                    toATMCommuAtmmstr.setAtmZone((String) map.get("ATM_ZONE"));
                    toATMCommuAtmmstr.setAtmCheckMac(DbHelper.toBoolean(((Integer) map.get("ATM_CHECK_MAC")).shortValue()));
                    toATMCommuAtmmstr.setAtmstatSec(((BigDecimal) map.get("ATMSTAT_SEC")).shortValue());
                    toATMCommuAtmmstr.setAtmstatSocket(((BigDecimal) map.get("ATMSTAT_SOCKET")).intValue());
                    toATMCommuAtmmstr.setAtmstatInikey(((BigDecimal) map.get("ATMSTAT_INIKEY")).intValue());
                    toATMCommuAtmmstr.setAtmIp((String) map.get("ATM_IP"));
                    toATMCommuAtmmstr.setAtmAtmpPort((String) map.get("ATM_ATMP_PORT"));
                    toATMCommuAtmmstr.setAtmCertAlias((String) map.get("ATM_CERTALIAS"));
                    toATMCommuAtmmstr.setAtmFepConnection(((Integer) map.get("ATM_FEP_CONNECTION")).shortValue());
                    ((ToATMCommuAtmmstrList) response).getAtmmstrs().add(toATMCommuAtmmstr);
                }
            } else {
                // 查詢不到資料
                response.setErrmsg(
                        StringUtils.join("Cannot found Atmmstr",
                                StringUtils.isNotBlank(toFEPATMCommuAtmmstr.getAtmNo())
                                        ? StringUtils.join(", AtmNo = [", toFEPATMCommuAtmmstr.getAtmNo(), "]")
                                        : StringUtils.EMPTY,
                                StringUtils.isNotBlank(toFEPATMCommuAtmmstr.getAtmIp())
                                        ? StringUtils.join(", AtmIp = [", toFEPATMCommuAtmmstr.getAtmIp(), "]")
                                        : StringUtils.EMPTY,
                                toFEPATMCommuAtmmstr.getAtmFepConnection() != null
                                        ? StringUtils.join(", AtmFepConnection = [", toFEPATMCommuAtmmstr.getAtmFepConnection(), "]")
                                        : StringUtils.EMPTY));
            }
        } catch (Exception e) {
            handleException(programFlow, logData, response, e);
        }
        loggingLogData(programFlow, MessageFlow.Response, logData, response);
        return response.toString();
    }

    private static void setField(ToATMCommuAtmmstr toATMCommuAtmmstr, Map<String, Object> atmmstrMap) {
    }

    /**
     * GW進來查詢Atmstat資料的電文, 收到的是ToFEPATMCommuAtmstatList對應的XML字串, 送出的是ToATMCommuAtmstatList對應的XML字串
     *
     * @param programFlow
     * @param logData
     * @param toFEPATMCommuAtmstatList
     * @return
     */
    private String processRequestData(ProgramFlow programFlow, LogData logData, ToFEPATMCommuAtmstatList toFEPATMCommuAtmstatList) {
        loggingLogData(programFlow, MessageFlow.Request, logData, toFEPATMCommuAtmstatList);
        ToATMCommuAtmstatList toATMCommuAtmstatList = new ToATMCommuAtmstatList();
        try {
            if (toFEPATMCommuAtmstatList.isOnlyFetchCount()) {
                int count = atmstatMapper.selectAtmstatListCount(toFEPATMCommuAtmstatList.getAtmstatAtmnoList(), (short) toFEPATMCommuAtmstatList.getAtmstatStatus(), toFEPATMCommuAtmstatList.getAtmAtmpIp());
                toATMCommuAtmstatList.setCount(count);
            } else {
                List<Map<String, Object>> atmstatList = atmstatMapper.selectAtmstatList(toFEPATMCommuAtmstatList.getAtmstatAtmnoList(), (short) toFEPATMCommuAtmstatList.getAtmstatStatus(), toFEPATMCommuAtmstatList.getAtmAtmpIp());
                if (CollectionUtils.isNotEmpty(atmstatList)) {
                    List<ToATMCommuAtmstat> list = new ArrayList<>();
                    for (Map<String, Object> orig : atmstatList) {
                        ToATMCommuAtmstat dest = new ToATMCommuAtmstat();
                        dest.setAtmstatAtmno((String) orig.get("ATMSTAT_ATMNO"));
                        dest.setAtmstatStatus(((BigDecimal) orig.get("ATMSTAT_STATUS")).intValue());
                        //20230504 Bruce add 加入連線及斷線時間
                        if (orig.get("ATMSTAT_LAST_OPEN") != null) {
                            dest.setAtmstatLastOpen(FormatUtil.dateTimeFormat(CalendarUtil.clone(((Timestamp) orig.get("ATMSTAT_LAST_OPEN")).getTime()), FormatUtil.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS));
                        }
                        if (orig.get("ATMSTAT_LAST_CLOSE") != null) {
                            dest.setAtmstatLastClose(FormatUtil.dateTimeFormat(CalendarUtil.clone(((Timestamp) orig.get("ATMSTAT_LAST_CLOSE")).getTime()), FormatUtil.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS));
                        }
                        if (orig.get("ATM_ATMP_IP") != null) {
                            dest.setAtmmstrAtmpIp(orig.get("ATM_ATMP_IP").toString());
                        }
                        list.add(dest);
                    }
                    toATMCommuAtmstatList.setAtmstatList(list);
                }
            }
        } catch (Exception e) {
            handleException(programFlow, logData, toATMCommuAtmstatList, e);
        }
        loggingLogData(programFlow, MessageFlow.Response, logData, toATMCommuAtmstatList);
        return toATMCommuAtmstatList.toString();
    }

    /**
     * ATMGW進來的批量更新Atmstat的電文, 收到的是ToFEPATMCommuUpdateAtmstatBatch對應的XML字串, 送出的是ToGWCommuDbOptResult對應的XML字串
     *
     * @param programFlow
     * @param logData
     * @param toFEPATMCommuUpdateAtmstatBatch
     * @return
     */
    private String processRequestData(ProgramFlow programFlow, LogData logData, ToFEPATMCommuUpdateAtmstatBatch toFEPATMCommuUpdateAtmstatBatch) {
        loggingLogData(programFlow, MessageFlow.Request, logData, toFEPATMCommuUpdateAtmstatBatch);
        ToGWCommuDbOptResult toGWCommuDbOptResult = new ToGWCommuDbOptResult();
        int result = 0;
        List<ToFEPATMCommuUpdateAtmstatBatch.ToFEPATMCommuUpdateAtmstatBatchData> atmstats = toFEPATMCommuUpdateAtmstatBatch.getAtmstats();
        if (CollectionUtils.isNotEmpty(atmstats)) {
            // 如果是異步執行, 則啟動一個執行緒去處理
            if (toFEPATMCommuUpdateAtmstatBatch.isAsync()) {
                executor.execute(() -> updateAtmstatBatch(programFlow, logData, atmstats, toGWCommuDbOptResult));
            } else {
                result = updateAtmstatBatch(programFlow, logData, atmstats, toGWCommuDbOptResult);
            }
        }
        toGWCommuDbOptResult.setResult(result);
        loggingLogData(programFlow, MessageFlow.Response, logData, toGWCommuDbOptResult);
        return toGWCommuDbOptResult.toString();
    }

    private int updateAtmstatBatch(ProgramFlow programFlow, LogData logData, List<ToFEPATMCommuUpdateAtmstatBatch.ToFEPATMCommuUpdateAtmstatBatchData> atmstats, ToGWCommuDbOptResult toGWCommuDbOptResult) {
        int result = 0;
        SqlSessionFactory sqlSessionFactory = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_SQL_SESSION_FACTORY);
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
            AtmstatExtMapper mapper = sqlSession.getMapper(AtmstatExtMapper.class);
            int total = 1;
            for (ToFEPATMCommuUpdateAtmstatBatch.ToFEPATMCommuUpdateAtmstatBatchData toFEPATMCommuUpdateAtmstat : atmstats) {
                Atmstat atmstat = new Atmstat();
                atmstat.setAtmstatAtmno(toFEPATMCommuUpdateAtmstat.getAtmstatAtmno());
                atmstat.setAtmstatStatus(toFEPATMCommuUpdateAtmstat.getAtmstatStatus());
                atmstat.setAtmstatSocket(toFEPATMCommuUpdateAtmstat.getAtmstatSocket());
                atmstat.setAtmstatSec(toFEPATMCommuUpdateAtmstat.getAtmstatSec());
                atmstat.setAtmstatInikey(toFEPATMCommuUpdateAtmstat.getAtmstatInikey());
                atmstat.setAtmstatApVersionN(toFEPATMCommuUpdateAtmstat.getAtmstatApVersionN());
                result += mapper.updateAtmstatByAtmAtmpIp(atmstat, toFEPATMCommuUpdateAtmstat.getAtmAtmpIp());
                if (configuration.getFlushStatementsTotal() > 0 && total++ % configuration.getFlushStatementsTotal() == 0) {
                    sqlSession.flushStatements();
                }
            }
            sqlSession.commit();
        } catch (Exception e) {
            DB2Util.handleBatchExecutorException(e);
            handleException(programFlow, logData, toGWCommuDbOptResult, e);
        } finally {
            IOUtils.closeQuietly(sqlSession);
        }
        return result;
    }

    private String sendReceiveSocketTest(ProgramFlow programFlow, String messageIn) {
        LogMDC.put(Const.MDC_PROFILE, this.getName());
        LogData logData = new LogData();
        // recv
        logData.setProgramFlowType(programFlow);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        logData.setMessage(messageIn);
        logData.setRemark("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        this.logMessage(logData);
        Thread t = new Thread(ThreadWrapper.wrap(() -> {
            logData.setProgramFlowType(programFlow);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            logData.setMessage(messageIn);
            logData.setRemark("hahahahahahahahahahahahahaha");
            this.logMessage(logData);
        }));
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            LogHelperFactory.getTraceLogger().error(e, e.getMessage());
        }
        // ack
        String messageOut = "Ack Response";
        logData.setProgramFlowType(programFlow);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        logData.setMessage(messageOut);
        logData.setRemark(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        this.logMessage(logData);
        return messageOut;
    }

    private String sendReceiveToFEPATMCommuTest() {
        ToATMCommu toATMCommu = new ToATMCommu();
        toATMCommu.setAtmno("039698");
        toATMCommu.setEj("0");
        toATMCommu.setMessage("045AF0F3F9F6F9F8D6404040F1F04BF1F04BF74BF5F240404040404040404040404040404040404040E3E2C1D7C2F0F0000000000140F080808080F0F1F6F0F7F0F4F0F3F9F6F9F8F0F0F0F0F0F0F0F0F0F0F040F2F0F003C3F0F140F2F0F2F3F1F1F1F00E50D4524953F70F404040404040F0F560F160F0F860F0F060F1F0F0F3F9F660F340E4E2C440404040404040405BF9F9F9F96BF8F8F36BF7F7F74BF4F54040404040404040404040F2F0F2F4F1F0F0F440E3D94040E4E2C440400E50424E414DEE5DA950F550F54FF30F4040404040404040405BF16BF0F0F04BF0F0405BF9F9F9F96BF8F8F46BF7F7F74BF4F540F0F54040404040404040F2F0F2F4F1F0F2F440E3D94040C5E4D94040E3C5E2E34040404040404040404040404040404040404040405BF2F2F24BF2F2404040404040405BF2F36BF8F3F54BF0F640F0F540404040404040404040F2F0F2F4F1F1F1F340E3D94040C5E4D94040E3C5E2E34040404040404040404040404040404040404040405BF2F2F24BF2F2404040404040405BF2F46BF0F5F74BF2F840F0F540404040404040404040F2F0F2F4F1F1F1F340E3D94040C5E4D94040E3C5E2E34040404040404040404040404040404040404040405BF2F2F24BF2F2404040404040405BF2F46BF2F7F94BF5F040F0F540404040404040404040F2F0F2F4F1F1F1F340E3D94040C5E4D94040E3C5E2E34040404040404040404040404040404040404040405BF2F2F24BF2F2404040404040405BF2F46BF5F0F14BF7F240F0F540404040404040404040F2F0F2F5F0F1F2F140E3D94040E4E2C4404040404040404040404040404040404040404040404040405BF16BF0F0F04BF0F0405BF9F9F9F96BF8F8F56BF7F7F74BF4F540F0F540404040404040404040F2F0F2F5F0F1F2F140E3D94040E4E2C440404040404040404040404040404040404040404040404040404040405BF14BF0F0405BF9F9F9F96BF8F8F56BF7F7F84BF4F540F0F540404040404040404040F2F0F2F5F0F1F2F140E3D94040E4E2C440404040404040404040404040404040404040404040404040404040405BF24BF0F0405BF9F9F9F96BF8F8F56BF7F8F04BF4F540F0F540404040404040404040F2F0F2F5F0F1F2F140E3D94040C1E4C440400E42D342E742E70F40404040404040404040404040404040404040405BF2F04BF0F04040404040405BF5F8F46BF5F8F84BF0F740F0F54040404040404040F2F0F2F5F0F1F2F140E3D94040C1E4C440400E42D342E742E70F40404040404040404040404040404040404040405BF2F04BF0F04040404040405BF5F8F46BF6F0F84BF0F740F0F54040404040404040F2F0F2F5F0F1F2F140E3D94040C1E4C440400E42D342E742E70F4040404040404040404040404040404040404040405BF34BF0F04040404040405BF5F8F46BF6F1F14BF0F740F0F5404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040");
        toATMCommu.setTxRquid("8eee6b1a43f34d258d6a444def5adff3");
        return toATMCommu.toString();
    }

}