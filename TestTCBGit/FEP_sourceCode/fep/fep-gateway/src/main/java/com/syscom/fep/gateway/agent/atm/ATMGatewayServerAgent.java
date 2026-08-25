package com.syscom.fep.gateway.agent.atm;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.GetApLogFilesUtil;
import com.syscom.fep.frmcommon.io.StreamGobbler;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.gateway.configuration.GatewayWebConfig;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.vo.enums.RestfulResultCode;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ATMGatewayServerAgent extends FEPBase {
    @Autowired
    private GatewayWebConfig webConfig;
    @Autowired
    private ATMGatewayServerAgentConfiguration configuration;
    private HttpClient httpClient;

    private void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, StringUtils.join(Gateway.ATMGW.name()));
    }

    @PostConstruct
    public void initialization() {
        httpClient = new HttpClient(configuration.isRecordHttpLog());
    }

    /**
     * 可以使用如下指令啟動ATMGW
     * <p>
     * curl -X POST http://localhost:8302/recv/atm/start
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/recv/atm/start", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageStart(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageStart"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setRemark(StringUtils.join("Start to execute command = [", configuration.getCmdStart(), "], operator = [", operator, "]"));
        this.logMessage(logData);
        ProcessBuilder processBuilder = new ProcessBuilder().command(configuration.getCmdStart().split("\\s+"));
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            Consumer<String> consumer = configuration.isPrintInputStream() ? LogHelperFactory.getTraceLogger()::debug : null;
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), consumer);
            new Thread(streamGobbler).start();
            logData.setOperator(operator);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageStart"));
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setRemark(StringUtils.join("Execute successful, command = [", configuration.getCmdStart(), "], operator = [", operator, "]"));
            this.logMessage(logData);
            return Const.REPLY_OK;
        } catch (Exception e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageStart"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("Execute command = [", configuration.getCmdStart(), "], operator = [", operator, "], with exception occur, ", e.getMessage()));
            sendEMS(logData);
            return StringUtils.join("Start ATMGateway Server failed with exception occur, ", e.getMessage());
        }
    }

    /**
     * 可以使用如下指令
     * <p>
     * curl "http://localhost:8302/fepgwagent/GetAPLog?operator=richard&logType=aplog&logDate=2024-09-13&fepLogPath=%2Ffep%2Flogs&fepLogArchivesPath=%2Ffep%2Flogs%2Farchives"
     *
     * @param operator
     * @param logType
     * @param logDate
     * @param fepLogPath
     * @param fepLogArchivesPath
     * @return
     */
    @RequestMapping(value = "/fepgwagent/GetAPLog", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> getApLog(
            @RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator,
            @RequestParam(value = "logType") String logType,
            @RequestParam(value = "logDate") String logDate,
            @RequestParam(value = "fepLogPath") String fepLogPath,
            @RequestParam(value = "fepLogArchivesPath") String fepLogArchivesPath,
            @RequestParam(value = "logTimeBegin", required = false, defaultValue = StringUtils.EMPTY) String logTimeBegin,
            @RequestParam(value = "logTimeEnd", required = false, defaultValue = StringUtils.EMPTY) String logTimeEnd,
            @RequestParam(value = "selectGZLog", required = false, defaultValue = StringUtils.EMPTY) String selectGZLog,
            @RequestParam(value = "sizeLimit", required = false, defaultValue = StringUtils.EMPTY) String sizeLimit,
            @RequestParam(value = "appName", required = false, defaultValue = StringUtils.EMPTY) String appName) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramName(StringUtils.join(ProgramName, ".getApLog"));
        logData.setMessageFlowType(MessageFlow.Request);
        return GetApLogFilesUtil.getApLog(operator, logType, logDate, fepLogPath, null, fepLogArchivesPath, null, logTimeBegin, logTimeEnd,
                // 2025-09-08 Richard modified for [Excessive Data Exposure]
                sizeLimit, webConfig.getGetApLogTotalSizeLimit(), this::logMessage, this::sendEMS, selectGZLog, appName);
    }

    @RequestMapping(value = "/fepgwagent/GetAPLogNames", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<String>> getApLogNames(
            @RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator,
            @RequestParam(value = "logType") String logType,
            @RequestParam(value = "logDate") String logDate,
            @RequestParam(value = "fepLogPath") String fepLogPath,
            @RequestParam(value = "fepLogArchivesPath") String fepLogArchivesPath,
            @RequestParam(value = "logTimeBegin", required = false, defaultValue = StringUtils.EMPTY) String logTimeBegin,
            @RequestParam(value = "logTimeEnd", required = false, defaultValue = StringUtils.EMPTY) String logTimeEnd) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramName(StringUtils.join(ProgramName, ".getApLog"));
        logData.setMessageFlowType(MessageFlow.Request);
        try {
            List<String> body = GetApLogFilesUtil.getArchiveFileNames(GetApLogFilesUtil.ApLogType.valueOf(logType), logDate, fepLogPath, null, fepLogArchivesPath, null, logTimeBegin, logTimeEnd);
            return new ResponseEntity<>(body, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 指定需要將電文打到那台ATM Service
     * <p>
     * curl -d "host=127.0.0.1" -X POST http://localhost:8302/recv/atm/changefepap
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/recv/atm/changefepap", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageChangeFEPAP(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator,
                                       @RequestParam(value = "host", required = false, defaultValue = StringUtils.EMPTY) String host) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChangeFEPAP"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setRemark(StringUtils.join("Start to do http post = [", configuration.getHttpChangeFEPAP(), "], operator = [", operator, "]"));
        this.logMessage(logData);
        try {
            Map<String, String> args = new HashMap<>();
            // args.put("operator", StringUtils.isNotBlank(operator) ? operator : FEPConfig.getInstance().getApplicationName().toUpperCase());
            ReflectUtil.envokeMethod(args, "put", new Class[] {Object.class, Object.class}, new Object[] {"operator", StringUtils.isNotBlank(operator) ? operator : FEPConfig.getInstance().getApplicationName().toUpperCase()}); // 2024-09-23 Richard add for 【Reflected XSS All Clients】
            // args.put("host", host);
            ReflectUtil.envokeMethod(args, "put", new Class[] {Object.class, Object.class}, new Object[] {"host", host}); // 2024-09-23 Richard add for 【Reflected XSS All Clients】
            String response = httpClient.doPost(configuration.getHttpChangeFEPAP(), MediaType.APPLICATION_FORM_URLENCODED, args, true);
            logData.setOperator(operator);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChangeFEPAP"));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(StringUtils.join("Get response = [", response, "], url = [", configuration.getHttpChangeFEPAP(), "], operator = [", operator, "]"));
            this.logMessage(logData);
            return response;
        } catch (Throwable e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChangeFEPAP"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("Do http post = [", configuration.getHttpChangeFEPAP(), "], operator = [", operator, "], with exception occur, ", e.getMessage()));
            sendEMS(logData);
            return StringUtils.join(this.getErrorCode(e), "Change FEP ATM Service failed with exception occur, ", e.getMessage());
        }
    }

    /**
     * 取得當前將電文打到哪台ATM Service
     * <p>
     * curl -X POST http://localhost:8302/recv/atm/checkfepap
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/recv/atm/checkfepap", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageCheckFEPAP(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageCheckFEPAP"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setRemark(StringUtils.join("Start to do http post = [", configuration.getHttpCheckFEPAP(), "], operator = [", operator, "]"));
        this.logMessage(logData);
        try {
            Map<String, String> args = new HashMap<>();
            // args.put("operator", StringUtils.isNotBlank(operator) ? operator : FEPConfig.getInstance().getApplicationName().toUpperCase());
            ReflectUtil.envokeMethod(args, "put", new Class[] {Object.class, Object.class}, new Object[] {"operator", StringUtils.isNotBlank(operator) ? operator : FEPConfig.getInstance().getApplicationName().toUpperCase()}); // 2024-09-23 Richard add for 【Reflected XSS All Clients】
            String response = httpClient.doPost(configuration.getHttpCheckFEPAP(), MediaType.APPLICATION_FORM_URLENCODED, args, true);
            logData.setOperator(operator);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageCheckFEPAP"));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(StringUtils.join("Get response = [", response, "], url = [", configuration.getHttpCheckFEPAP(), "], operator = [", operator, "]"));
            this.logMessage(logData);
            return response;
        } catch (Throwable e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageCheckFEPAP"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("Do http post = [", configuration.getHttpCheckFEPAP(), "], operator = [", operator, "], with exception occur, ", e.getMessage()));
            sendEMS(logData);
            return StringUtils.join(this.getErrorCode(e), "Check FEP ATM Service failed with exception occur, ", e.getMessage());
        }
    }

    private String getErrorCode(Throwable e) {
        if (RestfulResultCode.CONNECTION_REFUSED.name().equals(e.getMessage())) {
            return StringUtils.join("[", RestfulResultCode.CONNECTION_REFUSED.name(), "]");
        } else if (RestfulResultCode.READ_TIMED_OUT.name().equals(e.getMessage())) {
            return StringUtils.join("[", RestfulResultCode.READ_TIMED_OUT.name(), "]");
        }
        return StringUtils.join("[", ExceptionUtil.EXCEPTION_OCCUR, "]");
    }

    private void logMessage(Level level, String msg) {
        LogData logData = new LogData();
        logData.setProgramName(StringUtils.join(ProgramName, ".getApLog"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setRemark(msg);
        logMessage(level, logData);
    }

    private void sendEMS(Exception e, String msg) {
        LogData logData = new LogData();
        logData.setProgramName(StringUtils.join(ProgramName, ".getApLog"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramException(e);
        logData.setRemark(msg);
        sendEMS(logData);
    }
}
