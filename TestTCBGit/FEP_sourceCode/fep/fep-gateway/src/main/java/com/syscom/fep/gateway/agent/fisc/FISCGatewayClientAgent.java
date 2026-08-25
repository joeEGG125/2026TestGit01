package com.syscom.fep.gateway.agent.fisc;

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
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.gateway.configuration.GatewayWebConfig;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.LogKind;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayCmdAction;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayMode;
import com.syscom.fep.vo.enums.RestfulResultCode;
import jakarta.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang3.ArrayUtils;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;

public class FISCGatewayClientAgent extends FEPBase {
    @Autowired
    private GatewayWebConfig webConfig;
    @Autowired
    private FISCGatewayClientAgentConfiguration configuration;
    private HttpClient httpClient;

    private void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, StringUtils.join(Gateway.FISCGW.name()));
    }

    @PostConstruct
    public void initialization() {
        httpClient = new HttpClient(configuration.isRecordHttpLog());
    }

    /**
     * 可以使用如下指令啟動FISCGW
     * <p>
     * curl -X POST http://localhost:8304/recv/fisc/start
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/recv/fisc/start", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
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
            logData.setRemark(StringUtils.join("Execute successful, command = [", configuration.getCmdStart(), "], operator = [", operator, "]"));
            this.logMessage(logData);
            return Const.REPLY_OK;
        } catch (Exception e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageStart"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("Execute command = [", configuration.getCmdStart(), "], operator = [", operator, "], with exception occur, ", e.getMessage()));
            sendEMS(logData);
            return StringUtils.join(this.getErrorCode(e), "Start FISCGateway failed with exception occur, ", e.getMessage());
        }
    }

    /**
     * 可以使用如下指令啟動FISCGW
     * <p>
     * curl -X POST http://localhost:8304/recv/fisc/stop
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/recv/fisc/stop", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageStop(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageStop"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setRemark(StringUtils.join("Start to do http post = [", configuration.getHttpTerminate(), "], operator = [", operator, "]"));
        this.logMessage(logData);
        try {
            Map<String, String> args = new HashMap<>();
            // args.put("operator", StringUtils.isNotBlank(operator) ? operator : FEPConfig.getInstance().getApplicationName().toUpperCase());
            ReflectUtil.envokeMethod(args, "put", new Class[] {Object.class, Object.class}, new Object[] {"operator", StringUtils.isNotBlank(operator) ? operator : FEPConfig.getInstance().getApplicationName().toUpperCase()}); // 2024-09-23 Richard add for 【Reflected XSS All Clients】
            String response = httpClient.doPost(configuration.getHttpTerminate(), MediaType.APPLICATION_FORM_URLENCODED, args, true);
            logData.setOperator(operator);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageStop"));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(StringUtils.join("Get response = [", response, "], url = [", configuration.getHttpTerminate(), "], operator = [", operator, "]"));
            this.logMessage(logData);
            return response;
        } catch (Throwable e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageStop"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("Do http post = [", configuration.getHttpTerminate(), "], operator = [", operator, "], with exception occur, ", e.getMessage()));
            sendEMS(logData);
            return StringUtils.join(this.getErrorCode(e), "Stop FISCGateway failed with exception occur, ", e.getMessage());
        }
    }

    /**
     * 可以使用如下指令操作FISCGW Channel
     * <p>
     * curl -d "mode=primary&action=start" -X POST http://localhost:8304/recv/fisc/channel
     * <p>
     * curl -d "mode=secondary&action=start" -X POST http://localhost:8304/recv/fisc/channel
     * <p>
     * curl -d "mode=all&action=start" -X POST http://localhost:8304/recv/fisc/channel
     * <p>
     * curl -d "mode=primary&action=stop" -X POST http://localhost:8304/recv/fisc/channel
     * <p>
     * curl -d "mode=secondary&action=stop" -X POST http://localhost:8304/recv/fisc/channel
     * <p>
     * curl -d "mode=all&action=stop" -X POST http://localhost:8304/recv/fisc/channel
     *
     * @param operator
     * @param mode
     * @param action
     * @return
     */
    @RequestMapping(value = "/recv/fisc/channel", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageChannel(
            @RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator,
            @RequestParam(value = "mode", required = true) FISCGatewayMode mode,
            @RequestParam(value = "action", required = true) FISCGatewayCmdAction action) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChannel"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setRemark(StringUtils.join("Start to do http post = [", configuration.getHttpOperate(), "], operator = [", operator, "], mode = [", mode, "], action = [", action, "]"));
        this.logMessage(logData);
        try {
            Map<String, String> args = new HashMap<>();
            // args.put("operator", StringUtils.isNotBlank(operator) ? operator : FEPConfig.getInstance().getApplicationName().toUpperCase());
            ReflectUtil.envokeMethod(args, "put", new Class[] {Object.class, Object.class}, new Object[] {"operator", StringUtils.isNotBlank(operator) ? operator : FEPConfig.getInstance().getApplicationName().toUpperCase()}); // 2024-09-23 Richard add for 【Reflected XSS All Clients】
            args.put("mode", mode.name());
            args.put("action", action.name());
            String response = httpClient.doPost(configuration.getHttpOperate(), MediaType.APPLICATION_FORM_URLENCODED, args, true);
            logData.setOperator(operator);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChannel"));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(StringUtils.join("Get response = [", response, "], url = [", configuration.getHttpOperate(), "], operator = [", operator, "], mode = [", mode, "], action = [", action, "]"));
            this.logMessage(logData);
            return response;
        } catch (Throwable e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChannel"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("Do http post = [", configuration.getHttpOperate(), "], operator = [", operator, "], with exception occur, ", e.getMessage()));
            sendEMS(logData);
            return StringUtils.join(this.getErrorCode(e), "FISCGateway ", mode, " Channel ", action, " failed with exception occur, ", e.getMessage());
        }
    }

    /**
     * 可以使用如下指令check FISCGW
     * <p>
     * curl -d "action=check" -X POST http://localhost:8304/recv/fisc/check
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/recv/fisc/check", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageCheck(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageCheck"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setRemark(StringUtils.join("Start to do http post = [", configuration.getHttpOperate(), "], operator = [", operator, "]"));
        this.logMessage(logData);
        try {
            Map<String, String> args = new HashMap<>();
            // args.put("operator", StringUtils.isNotBlank(operator) ? operator : FEPConfig.getInstance().getApplicationName().toUpperCase());
            ReflectUtil.envokeMethod(args, "put", new Class[] {Object.class, Object.class}, new Object[] {"operator", StringUtils.isNotBlank(operator) ? operator : FEPConfig.getInstance().getApplicationName().toUpperCase()}); // 2024-09-23 Richard add for 【Reflected XSS All Clients】
            args.put("action", FISCGatewayCmdAction.check.name());
            String response = httpClient.doPost(configuration.getHttpOperate(), MediaType.APPLICATION_FORM_URLENCODED, args, true);
            logData.setOperator(operator);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageCheck"));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setMessage(response);
            logData.setRemark(StringUtils.join("Get response = [", response, "], url = [", configuration.getHttpOperate(), "], operator = [", operator, "]"));
            this.logMessage(logData);
            return response;
        } catch (Throwable e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageCheck"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("Do http post = [", configuration.getHttpOperate(), "], operator = [", operator, "], with exception occur, ", e.getMessage()));
            sendEMS(logData);
            return StringUtils.join(this.getErrorCode(e), "Check FISCGateway Status failed with exception occur, ", e.getMessage());
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

    /**
     * 可以使用如下指令, 獲取FISCGW對應的log檔內容
     * <p>
     * curl -d "logKind=txcurrent" -X POST http://localhost:8304/recv/fisc/showlog
     * <p>
     * curl -d "logKind=tx&date=20230922" -X POST http://localhost:8304/recv/fisc/showlog -o fiscgw.log
     *
     * @param operator
     * @param kind
     * @param date
     * @return
     */
    @RequestMapping(value = "/recv/fisc/showlog", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public byte[] onMessageShowLog(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator,
                                   @RequestParam(value = "logKind", required = false, defaultValue = "tx") LogKind kind,
                                   @RequestParam(value = "date", required = false, defaultValue = StringUtils.EMPTY) String date,
                                   @RequestParam(value = "lastRows", required = false, defaultValue = "-1") int lastRows) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageShowLog"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setRemark(StringUtils.join("Show FISCGateway Log, kind:", kind.name(), ",date:", date, ",operator:", operator));
        this.logMessage(logData);
        try {
            String logDate = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH);
            if (kind != LogKind.txcurrent && StringUtils.isNotBlank(date)) {
                try {
                    Date d = FormatUtil.parseDataTime(date, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
                    logDate = FormatUtil.dateTimeFormat(CalendarUtil.clone(d), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH);
                } catch (ParseException e) {
                    throw ExceptionUtil.createIllegalArgumentException(e, "incorrect date format");
                }
            }
            String logPath = StringUtils.replace(this.configuration.getGwLogPath(), "{yyyy-MM-dd}", logDate);
            logPath = StringUtils.replace(logPath, "{logKind}", kind.getLogName());
            if (kind == LogKind.txcurrent && lastRows <= 0) {
                lastRows = this.configuration.getLastRows();
            }
            byte[] compressedBytes = this.readLog(logData, operator, logPath, lastRows);
            String response = null;
            logData.setOperator(operator);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageShowLog"));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(StringUtils.join("Show FISCGateway Log succeed, kind:", kind.name(), ",date:", date, ",operator:", operator));
            this.logMessage(logData);
            return compressedBytes;
        } catch (Throwable e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageShowLog"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("Show FISCGateway Log with exception occur, kind:", kind.name(), ",date:", date, ",operator:", operator));
            sendEMS(logData);
            return StringUtils.join("[ERROR]Show FISCGateway Log failed with exception occur, ", e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * 讀取log檔內容
     *
     * @param logData
     * @param operator
     * @param logPath
     * @param lastRows
     * @return
     * @throws Exception
     */
    protected byte[] readLog(LogData logData, String operator, String logPath, int lastRows) throws Exception {
        File file = new File(CleanPathUtil.cleanString(logPath));
        File parent = file.getParentFile();
        if (!parent.exists()) {
            logData.setOperator(operator);
            logData.setProgramName(StringUtils.join(ProgramName, ".readLog"));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(StringUtils.join("Directory not Exist, ", parent.getPath(), ", logPath:", logPath, ",operator:", operator));
            logMessage(Level.WARN, logData);
            // throw ExceptionUtil.createException("Directory not Exist, ", parent.getPath());
            return new byte[0]; // 2024-06-24 Richard modified for 檔不存在顯示空白就好 by Ashiang
        }
        FileFilter filefilter = new RegexFileFilter(file.getName(), IOCase.INSENSITIVE);
        File[] files = parent.listFiles(filefilter);
        if (ArrayUtils.isNotEmpty(files)) {
            logData.setOperator(operator);
            logData.setProgramName(StringUtils.join(ProgramName, ".readLog"));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(StringUtils.join("Get Log File List succeed, logPath:", logPath, ",operator:", operator, ",", files.length, " files total"));
            this.logMessage(logData);
            // 抓所有的檔案
            if (lastRows < 0) {
                // 先排序
                this.sort(files, false);
                int index = 0;
                File f = files[index];
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    while (true) {
                        logData.setOperator(operator);
                        logData.setProgramName(StringUtils.join(ProgramName, ".readLog"));
                        logData.setMessageFlowType(MessageFlow.Response);
                        logData.setRemark(StringUtils.join("Start to read file, ", f.getPath(), ",operator:", operator));
                        this.logMessage(logData);
                        bos.write(FileUtils.readFileToByteArray(f));
                        bos.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
                        logData.setOperator(operator);
                        logData.setProgramName(StringUtils.join(ProgramName, ".readLog"));
                        logData.setMessageFlowType(MessageFlow.Response);
                        logData.setRemark(StringUtils.join("Read file succeed, ", f.getPath(), ",operator:", operator, ",", f.length(), " bytes total"));
                        this.logMessage(logData);
                        index++;
                        if (index == files.length) {
                            break;
                        }
                        f = files[index];
                    }
                    byte[] bytes = bos.toByteArray();
                    // 避免太大, 壓縮一下
                    byte[] compressedBytes = CompressionUtil.compress(bytes);
                    logData.setOperator(operator);
                    logData.setProgramName(StringUtils.join(ProgramName, ".readLog"));
                    logData.setMessageFlowType(MessageFlow.Response);
                    logData.setRemark(StringUtils.join("Read all Log File succeed, logPath:", logPath, ",operator:", operator, ",", files.length, " files total,", bytes.length, " bytes total,", compressedBytes.length, " compressed bytes total"));
                    this.logMessage(logData);
                    return compressedBytes;
                } catch (IOException e) {
                    throw ExceptionUtil.createException(e, "File Read error, ", f.getPath());
                }
            }
            // 只抓最後一個檔案
            else if (lastRows == 0) {
                // 先倒序排序
                this.sort(files, true);
                File f = files[0];
                try {
                    logData.setOperator(operator);
                    logData.setProgramName(StringUtils.join(ProgramName, ".readLog"));
                    logData.setMessageFlowType(MessageFlow.Response);
                    logData.setRemark(StringUtils.join("Start to read file, ", f.getPath(), ",operator:", operator));
                    this.logMessage(logData);
                    byte[] bytes = FileUtils.readFileToByteArray(f);
                    // 避免太大, 壓縮一下
                    byte[] compressedBytes = CompressionUtil.compress(bytes);
                    logData.setOperator(operator);
                    logData.setProgramName(StringUtils.join(ProgramName, ".readLog"));
                    logData.setMessageFlowType(MessageFlow.Response);
                    logData.setRemark(StringUtils.join("Read file succeed, ", f.getPath(), ",operator:", operator, ",", bytes.length, " bytes total,", compressedBytes.length, " compressed bytes total"));
                    this.logMessage(logData);
                    return compressedBytes;
                } catch (IOException e) {
                    throw ExceptionUtil.createException(e, "File Read error, ", f.getPath());
                }
            }
            // 倒序抓最後lastRow行
            else {
                // 先倒序排序
                this.sort(files, true);
                int index = 0;
                File f = files[index];
                String readLine;
                StringBuilder sb = new StringBuilder();
                int readFiles = 0, readRows = 0, readAllRows = 0;
                while (true) {
                    logData.setOperator(operator);
                    logData.setProgramName(StringUtils.join(ProgramName, ".readLog"));
                    logData.setMessageFlowType(MessageFlow.Response);
                    logData.setRemark(StringUtils.join("Start to read file, ", f.getPath(), ",operator:", operator));
                    this.logMessage(logData);
                    try (ReversedLinesFileReader br = new ReversedLinesFileReader(f, StandardCharsets.UTF_8)) {
                        readFiles++;
                        readRows = 0;
                        while ((readLine = br.readLine()) != null) {
                            // 2025-03-19 Richard modified for 顯示fiscgw log 的功能, 原來logkind=txcurrent時是依時間倒排,幫我改成由小到大, 也就是最新的log在最下面
                            // sb.append(readLine).append(System.lineSeparator());
                            sb.insert(0, System.lineSeparator()).insert(0, readLine);
                            readRows++;
                            readAllRows++;
                            if (readAllRows == lastRows)
                                break;
                        }
                        logData.setOperator(operator);
                        logData.setProgramName(StringUtils.join(ProgramName, ".readLog"));
                        logData.setMessageFlowType(MessageFlow.Response);
                        logData.setRemark(StringUtils.join("Read file succeed, ", f.getPath(), ",operator:", operator, ",", readRows, " rows total,"));
                        this.logMessage(logData);
                    } catch (IOException e) {
                        throw ExceptionUtil.createException(e, "File Read error, ", f.getPath());
                    }
                    if (readAllRows == lastRows)
                        break;
                    index++;
                    if (index == files.length) {
                        break;
                    }
                    f = files[index];
                }
                byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
                // 避免太大, 壓縮一下
                byte[] compressedBytes = CompressionUtil.compress(bytes);
                logData.setOperator(operator);
                logData.setProgramName(StringUtils.join(ProgramName, ".readLog"));
                logData.setMessageFlowType(MessageFlow.Response);
                logData.setRemark(StringUtils.join("Read all Log File succeed, logPath:", logPath, ",operator:", operator, ",", readAllRows, " rows total,", readFiles, " files total,", bytes.length, " bytes total,", compressedBytes.length, " compressed bytes total"));
                this.logMessage(logData);
                return compressedBytes;
            }
        } else {
            logData.setOperator(operator);
            logData.setProgramName(StringUtils.join(ProgramName, ".readLog"));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(StringUtils.join("Empty Directory, ", parent.getPath(), ", logPath:", logPath, ",operator:", operator));
            logMessage(Level.WARN, logData);
            // throw ExceptionUtil.createException("Empty Directory, cannot find any file matches ", file.getName(), " in directory ", parent.getPath());
            return new byte[0]; // 2024-06-24 Richard modified for 檔不存在顯示空白就好 by Ashiang
        }
    }

    /**
     * 按照檔案名進行排序
     *
     * @param files
     * @param reverse
     */
    private void sort(File[] files, boolean reverse) {
        // 按照倒序排序
        Arrays.sort(files, (f1, f2) -> {
            String name1 = f1.getName();
            String name2 = f2.getName();
            try {
                int sequence1 = Integer.parseInt(StringUtils.substring(name1, name1.lastIndexOf("-") + 1, name1.lastIndexOf(".log")));
                int sequence2 = Integer.parseInt(StringUtils.substring(name2, name2.lastIndexOf("-") + 1, name2.lastIndexOf(".log")));
                return reverse ? sequence2 - sequence1 : sequence1 - sequence2;
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().error(e, e.getMessage());
            }
            return reverse ? name2.compareToIgnoreCase(name1) : name1.compareToIgnoreCase(name2);
        });
    }

    /**
     * 可以使用如下指令
     * <p>
     * curl "http://localhost:8304/fepgwagent/GetAPLog?operator=richard&logType=aplog&logDate=2024-09-13&fepLogPath=%2Ffep%2Flogs&fepLogArchivesPath=%2Ffep%2Flogs%2Farchives"
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
     * 指定需要將電文打到那台FISC Service
     * <p>
     * curl -d "host=127.0.0.1" -X POST http://localhost:8304/recv/fisc/changefepap
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/recv/fisc/changefepap", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
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
            return StringUtils.join(this.getErrorCode(e), "Change FEP FISC Service failed with exception occur, ", e.getMessage());
        }
    }

    /**
     * 取得當前將電文打到哪台FISC Service
     * <p>
     * curl -X POST http://localhost:8304/recv/fisc/checkfepap
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/recv/fisc/checkfepap", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
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
            return StringUtils.join(this.getErrorCode(e), "Check FEP FISC Service failed with exception occur, ", e.getMessage());
        }
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
