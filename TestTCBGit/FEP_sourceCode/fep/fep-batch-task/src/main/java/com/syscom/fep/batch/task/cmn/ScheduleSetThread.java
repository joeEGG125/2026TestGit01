package com.syscom.fep.batch.task.cmn;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.communication.BaseResponse;
import com.syscom.fep.frmcommon.jms.entity.JmsInfoConcurrency;
import com.syscom.fep.frmcommon.jms.entity.JmsMonitorResponse;
import com.syscom.fep.frmcommon.netty.NettyEventExecutorData;
import com.syscom.fep.frmcommon.netty.NettyEventExecutorDataErrorCode;
import com.syscom.fep.frmcommon.netty.NettyEventExecutorRequest;
import com.syscom.fep.frmcommon.jms.entity.JmsMonitorConcurrencyRequest;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.frmcommon.parse.GsonParser;
import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 線程數排程設定作業(不同的通道)
 *
 * @author Richard
 */
public class ScheduleSetThread extends FEPBase implements Task {
    private BatchJobLibrary job;
    private String uri;
    private Type type;
    private String threads;
    private int timeout;

    private enum Type {
        queue, socket;
    }

    @Override
    public void displayUsage() {
        System.out.println("USAGE:");
        System.out.println(" ScheduleSetThread Options");
        System.out.println();
        System.out.println(" Options:");
        System.out.println(" /? Display this help message.");
        System.out.println(" /URL Required");
        System.out.println(" /Type Required, " + StringUtils.join(Type.values(), ","));
        System.out.println(" /Threads Optional, threads number, -1 by default for reset threads number");
        System.out.println(" /Timeout Optional, http request timeout in seconds, 30 by default");
        System.out.println(" /CallBatchJob Optional, true (by default) or false");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println(" ScheduleSetThread /URL:http://127.0.0.1:8080/consumers/EATMServerReceiver/setConcurrency /Type:queue /Threads:10 /Timeout:30 /BatchLogPath:/home/tcb/log /CallBatchJob:true");
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
        // URL
        String value = job.getArguments().get("URL");
        if (StringUtils.isBlank(value)) {
            job.writeErrorLog(null, "URL未設定!!!");
            return BatchReturnCode.MissingArgument;
        } else {
            this.uri = value;
        }
        // Type
        value = job.getArguments().get("Type");
        if (StringUtils.isBlank(value)) {
            job.writeErrorLog(null, "Type未設定!!!");
            return BatchReturnCode.MissingArgument;
        } else {
            try {
                this.type = Type.valueOf(value);
            } catch (IllegalArgumentException e) {
                job.writeErrorLog(e, "Type設定不正確!!! Type:", value);
                return BatchReturnCode.InvalidArgument;
            }
        }
        // Threads
        value = job.getArguments().get("Threads");
        if (StringUtils.isNotBlank(value)) {
            if (this.type == Type.queue && value.matches("\\d+-\\d+")) {
                String[] array = value.split("-");
                // low和high都是數字
                if (!StringUtils.isNumeric(array[0]) || !StringUtils.isNumeric(array[1])) {
                    job.writeErrorLog(null, "Threads設定格式不正確!!! Threads:", value);
                    return BatchReturnCode.InvalidArgument;
                }
            } else if (!StringUtils.isNumeric(value)) {
                job.writeErrorLog(null, "Threads必須是數字!!! Threads:", value);
                return BatchReturnCode.InvalidArgument;
            }
            this.threads = value;
        } else {
            if (type == Type.queue)
                this.threads = "-1";
            else if (type == Type.socket)
                this.threads = "0";
        }
        // Timeout
        this.timeout = Integer.parseInt(job.getArguments().getOrDefault("Timeout", "30"));
        return BatchReturnCode.Succeed;
    }

    /**
     * 批次主要處理流程
     *
     * @return
     * @throws Exception
     */
    private BatchReturnCode mainProcess() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        if (HttpClient.isHttps(uri)) {
            restTemplate.setRequestFactory(HttpClientConfiguration.createTrustAnyHttpComponentsClientHttpRequestFactory(SslContextFactory.PROTOCOL, this.timeout * 1000));
        } else {
            restTemplate.setRequestFactory(HttpClientConfiguration.createSimpleClientHttpRequestFactory(this.timeout * 1000));
        }
        Serializable request = createRequest(this.type, this.threads, true);
        job.writeLog("[", uri, "]", Const.MESSAGE_OUT, new Gson().toJson(request));
        String response = restTemplate.postForObject(HttpClient.toUriString(uri), request, String.class);
        job.writeLog("[", uri, "]", Const.MESSAGE_IN, response);
        return BatchReturnCode.Succeed;
    }

    private Serializable createRequest(Type type, String threads, boolean isModified) {
        if (type == Type.queue) {
            return new JmsMonitorConcurrencyRequest(threads, isModified);
        } else if (type == Type.socket) {
            return new NettyEventExecutorRequest(Integer.parseInt(threads), isModified);
        }
        throw ExceptionUtil.createUnsupportedOperationException("Type:", type);
    }

    /**
     * 檢核參數是否符合規範, 若不符合則拋出異常
     *
     * @param commandArgs
     * @throws Exception
     */
    @Override
    public void checkCommandArgs(String commandArgs) throws Exception {
        if (StringUtils.isBlank(commandArgs))
            return;
        String[] args = commandArgs.split("\\s+");
        // URL
        String uri = BatchJobLibrary.findArg(args, "URL", null); ;
        if (StringUtils.isBlank(uri))
            throw ExceptionUtil.createIllegalArgumentException("URL未設定!!!");
        // Type
        String value = BatchJobLibrary.findArg(args, "Type", null);
        if (StringUtils.isBlank(value)) {
            throw ExceptionUtil.createIllegalArgumentException("Type未設定!!!");
        }
        Type type;
        try {
            type = Type.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw ExceptionUtil.createIllegalArgumentException("Type設定不正確!!!");
        }
        // Threads
        String threads = BatchJobLibrary.findArg(args, "Threads", null); ;
        if (StringUtils.isNotBlank(threads)) {
            if (this.type == Type.queue && threads.matches("\\d+-\\d+")) {
                String[] array = threads.split("-");
                // low和high都是數字
                if (!StringUtils.isNumeric(array[0]) || !StringUtils.isNumeric(array[1])) {
                    throw ExceptionUtil.createIllegalArgumentException("Threads設定格式不正確!!! Threads:", threads);
                }
            } else if (!StringUtils.isNumeric(threads)) {
                throw ExceptionUtil.createIllegalArgumentException("Threads必須是數字!!! Threads:", threads);
            }
        }
        RestTemplate restTemplate = new RestTemplate();
        if (HttpClient.isHttps(uri)) {
            restTemplate.setRequestFactory(HttpClientConfiguration.createTrustAnyHttpComponentsClientHttpRequestFactory(SslContextFactory.PROTOCOL, this.timeout * 1000));
        } else {
            restTemplate.setRequestFactory(HttpClientConfiguration.createSimpleClientHttpRequestFactory(this.timeout * 1000));
        }
        Serializable request = createRequest(type, threads, false); // 只是check參數是否合法, 不改變
        LogHelperFactory.getTraceLogger().debug("[", uri, "]", Const.MESSAGE_OUT, new Gson().toJson(request));
        String response = null;
        try {
            response = restTemplate.postForObject(HttpClient.toUriString(uri), request, String.class);
            LogHelperFactory.getTraceLogger().debug("[", uri, "]", Const.MESSAGE_IN, response);
        } catch (RestClientException e) {
            throw ExceptionUtil.createIllegalArgumentException("遠程服務訪問失敗!!");
        }
        String result = parseResponse(type, response);
        if (StringUtils.isNotBlank(result))
            throw ExceptionUtil.createIllegalArgumentException(result);
    }

    private String parseResponse(Type type, String response) throws Exception {
        if (StringUtils.isBlank(response))
            return response;
        if (type == Type.queue) {
            GsonParser<JmsMonitorResponse<JmsInfoConcurrency>> gsonParser = new GsonParser<>(new TypeToken<JmsMonitorResponse<JmsInfoConcurrency>>() {}.getType());
            JmsMonitorResponse<JmsInfoConcurrency> serializable = gsonParser.readIn(response);
            if (serializable.isResult())
                return StringUtils.EMPTY;
            return serializable.getError();
        } else if (type == Type.socket) {
            GsonParser<BaseResponse<NettyEventExecutorData, NettyEventExecutorDataErrorCode>> gsonParser = new GsonParser<>(new TypeToken<BaseResponse<NettyEventExecutorData, NettyEventExecutorDataErrorCode>>() {}.getType());
            BaseResponse<NettyEventExecutorData, NettyEventExecutorDataErrorCode> serializable = gsonParser.readIn(response);
            if (serializable.isResult())
                return StringUtils.EMPTY;
            return serializable.getError();
        }
        throw ExceptionUtil.createUnsupportedOperationException("Type:", type);
    }
}
