package com.syscom.fep.server.controller.restful;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.thread.ThreadPoolFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.server.common.handler.CBSHandler;
import com.syscom.fep.server.controller.BaseController;
import com.syscom.fep.server.controller.configuration.IMSConfiguration;
import com.syscom.fep.vo.communication.BaseXmlCommu;
import com.syscom.fep.vo.communication.ToFEPCommuAction;
import com.syscom.fep.vo.communication.ToFEPCommuSysconf;
import com.syscom.fep.vo.communication.ToFEPFISCCommu;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@StackTracePointCut(caller = SvrConst.SVR_IMS)
public class IMSController extends BaseController {
    // 配置檔對應物件
    private static IMSConfiguration configuration;
    // 執行緒池
    private static ExecutorService executor;

    @PostConstruct
    public void postConstruct() {
        configuration = SpringBeanFactoryUtil.registerBean(IMSConfiguration.class);
        LogHelperFactory.getGeneralLogger().info("Create Executor Pool, size:", configuration.getExecutorCorePoolSize(), ", keepAliveTime:", configuration.getExecutorKeepAliveTime(), ", queueCapacity:", configuration.getExecutorQueueCapacity());
        executor = ThreadPoolFactory.newFixedThreadPool(
                configuration.getExecutorCorePoolSize(),
                configuration.getExecutorKeepAliveTime(),
                TimeUnit.MILLISECONDS,
                configuration.getExecutorQueueCapacity(),
                new SimpleThreadFactory(getName()),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @PreDestroy
    public void preDestroy() {
        ThreadPoolFactory.shutdown(executor, getName());
    }

    @Override
    public String getName() {
        return SvrConst.SVR_IMS;
    }

    @RequestMapping(value = "/recv/ims", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
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
    protected String processRequestData(ProgramFlow programFlow, String messageIn) {
        LogMDC.put(Const.MDC_PROFILE, this.getName());
        LogHelperFactory.getTraceLogger().trace(this.getName(), " Receive Message:", messageIn);
        String messageOut = StringUtils.EMPTY;
        LogData logData = new LogData();
        Object commu = null;
        try {
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }
            commu = BaseXmlCommu.fromXML(messageIn);
            // 為null, 說明電文xml中沒有<classname />, 則預設按照ToFEPFISCCommu來轉
            if (commu == null) {
                commu = ToFEPFISCCommu.fromXML(messageIn, ToFEPFISCCommu.class);
            }
            // FISCGW進來的交易類電文, 收到的是ToFEPFISCCommu對應的XML字串, 送出的是HEX String
            if (commu instanceof ToFEPFISCCommu) {
                this.processRequestData(programFlow, logData, (ToFEPFISCCommu) commu);
            }
            // FISCGW進來的查詢Sysconf資料的電文, 收到的是ToFEPCommuSysconf對應的XML字串, 送出的是ToGWCommuSysconf對應的XML字串
            else if (commu instanceof ToFEPCommuSysconf) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPCommuSysconf) commu);
            }
            // FISCGW進來的沒有請求參數查詢資料的電文, 收到的是ToFEPCommuAction對應的XML字串, 送出的是ToGWCommuAction對應的XML字串
            else if (commu instanceof ToFEPCommuAction) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPCommuAction) commu);
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
            if (!(commu instanceof ToFEPFISCCommu)) {
                if (StringUtils.isNotBlank(messageOut)) {
                    LogHelperFactory.getTraceLogger().trace(this.getName(), " Send Message: ", messageOut);
                } else {
                    LogHelperFactory.getTraceLogger().trace(this.getName(), " Send Empty Message");
                }
            }
        }
        return messageOut;
    }

    /**
     * FISCGW進來的交易類電文, 收到的是ToFEPFISCCommu對應的XML字串, 送出的是HEX String
     *
     * @param programFlow
     * @param logData
     * @param toFEPFISCCommu
     * @return
     */
    private void processRequestData(final ProgramFlow programFlow, final LogData logData, final ToFEPFISCCommu toFEPFISCCommu) {
        logData.setProgramFlowType(programFlow);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        logData.setStep(toFEPFISCCommu.getStep());
        logData.setEj(toFEPFISCCommu.getEj());
        logData.setTxRquid(toFEPFISCCommu.getTxRquid());
        logData.setStan(toFEPFISCCommu.getStan());
        logData.setMessage(toFEPFISCCommu.getMessage());
        logData.setRemark(
                StringUtils.join(this.getName(), " Get IMS Request",
                        " EJ:", logData.getEj(),
                        " Stan:", logData.getStan(),
                        " Step:", logData.getStep(),
                        " TxRquid:", logData.getTxRquid())
        );
        this.logMessage(logData);
        // RefBase<SubSystem> refSubSystem = new RefBase<SubSystem>(null);
        // RefBase<FISCSubSystem> refFISCSubSystem = new RefBase<FISCSubSystem>(null);
        //this.getFISCSubSystem(toFEPFISCCommu.getMessage(), refSubSystem, refFISCSubSystem);
        // logData.setSubSys(refSubSystem.get());
        if (toFEPFISCCommu.isSync()) {
            this.invokeAA(programFlow, logData, toFEPFISCCommu.getMessage());
        } else {
            executor.execute(() -> {
                // 因為是新起了一個線程在跑，所以這裡必須要再塞入一次
                LogMDC.put(Const.MDC_PROFILE, this.getName());
                this.invokeAA(programFlow, logData, toFEPFISCCommu.getMessage());
            });
        }
    }

    private void invokeAA(final ProgramFlow programFlow, final LogData logData, final String message) {
        try {
            CBSHandler cBSHandler = new CBSHandler();
            cBSHandler.setEj(logData.getEj());
            cBSHandler.setTxRquid(logData.getTxRquid());
            cBSHandler.setLogContext(logData);
            String rtn = cBSHandler.dispatch(FEPChannel.CBS, message);
            logData.setProgramFlowType(this.getOutProgramFlow(programFlow));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".invokeAA"));
            logData.setRemark(StringUtils.join(this.getName(), " Get AA Response OK"));
            logData.setMessage(rtn);
            this.logMessage(logData);
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramFlowType(programFlow);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            logData.setRemark(StringUtils.join(this.getName(), " Get IMS Response with exception occur, ", e.getMessage()));
            logData.setMessage(message);
            this.logMessage(logData);
            throw e;
        }
    }
}
