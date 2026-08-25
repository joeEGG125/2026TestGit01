package com.syscom.fep.web.controller.atmmon;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.util.GetApLogFilesUtil;
import com.syscom.fep.common.util.GetApLogFilesUtil.ApLogType;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.ems.model.Feplog;
import com.syscom.fep.mybatis.model.Channel;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.configurer.WebApLogConfiguration;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.form.atmmon.UI_060550_A_FormDetail;
import com.syscom.fep.web.form.atmmon.UI_060610_FormDetail;
import com.syscom.fep.web.form.atmmon.UI_060610_FormMain;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.ChannelService;
import com.syscom.fep.web.service.EmsService;
import com.syscom.fep.web.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 交易流程日誌(FEPLOG)查詢
 *
 * @author bruce
 */
@Controller
public class UI_060610Controller extends BaseController {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private EmsService emsService;

    @Autowired
    private AtmService atmService;

    private final String pleaseChoose = "所有";

    private final String yyyyMMdd = "yyyy-MM-dd";

    private final String yyyyMMddHHmmss = "yyyy/MM/dd HH:mm:ss";

    private final String timeBegin = "00.00.00";

    private final String timeEnd = "23.59.00";

    private final String changeTitleFor550_A = "交易日誌(FEPTXN)查詢";

    private final long totalSizeLimit = WebConfiguration.getInstance().getGetApLogTotalSizeLimit() * 1024L * 1024L * 1024L;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_060610_FormMain form = new UI_060610_FormMain();
        DateFormat dateTimeformat = new SimpleDateFormat(this.yyyyMMdd);
        form.setTransactDate(dateTimeformat.format(new Date())); //交易日期
        try {
            this.setChannelOptions(mode);
            this.setServerOptions(mode);
            this.setServerDownload(mode, form);
            this.setLogTypeOptions(mode, form);
            this.setChannelDownloadOptions(mode, form);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }//通道下拉選單
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    /**
     * 2023-10-08 Richard modified
     *
     * @param form
     * @return
     */
    @PostMapping(value = "/atmmon/UI_060610/download")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> download(@RequestBody UI_060610_FormMain form, ModelMap mode) {
        this.infoMessage("開始下載檔案, 條件 = [", form.toString(), "]");
        try {
            String fepLogPath = WebConfiguration.getInstance().getFepLogPath();
            String fepWasLogPath = WebConfiguration.getInstance().getFepWasLogPath();
            String fepLogArchivesPath = WebConfiguration.getInstance().getFepLogArchivesPath();
            String fepWasLogArchivesPath = WebConfiguration.getInstance().getFepWasLogArchivesPath();
            String server = form.getServer();
            String logType = form.getLogType();
            String logDate = form.getTransactDate();
            if (StringUtils.isBlank(logDate)) {
                return this.handleDownloadError("未選擇交易日期");
            }
            if (StringUtils.isBlank(form.getFeptxnTxTimeBeginTxt()) && StringUtils.isNotBlank(form.getFeptxnTxTimeEndTxt())) {
                form.setFeptxnTxTimeBeginTxt("00:00:00");
            }
            if (StringUtils.isNotBlank(form.getFeptxnTxTimeBeginTxt()) && StringUtils.isBlank(form.getFeptxnTxTimeEndTxt())) {
                form.setFeptxnTxTimeEndTxt("23:59:59");
            }
            String logTimeBegin = form.getFeptxnTxTimeBeginTxt();
            String logTimeEnd = form.getFeptxnTxTimeEndTxt();
            String selectGZLog = form.getSelectGZLog();
            if (StringUtils.isBlank(selectGZLog))
                selectGZLog = StringUtils.EMPTY;
            // 2026-02-12 Richard add 這裡根據channel獲取對應的appName
            String appName = StringUtils.EMPTY;
            String channelDownload = form.getChannelDownload();
            if (StringUtils.isNotBlank(channelDownload) && ApLogType.valueOf(logType) == ApLogType.aplog) {
                Map<String, String> channelToAppName = getChannelToAppNameMap(server);
                appName = MapUtils.isNotEmpty(channelToAppName) ? channelToAppName.get(channelDownload) : StringUtils.EMPTY;
                if (StringUtils.isBlank(appName))
                    return this.handleDownloadError("沒有找到通道", FormatUtil.messageFormat(Const.KEY_WORDS_IN_MESSAGE, channelDownload), "對應的服務名稱設定");
            }
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("下載Log");
            // 塞入auditLog.params
            auditLog.addParam("交易日期", form.getTransactDate());
            if (StringUtils.isNotBlank(form.getFeptxnTxTimeBegin()) && StringUtils.isNotBlank(form.getFeptxnTxTimeEnd()))
                auditLog.addParam("交易時間起訖", form.getFeptxnTxTimeBegin() + "~" + form.getFeptxnTxTimeEnd());
            if (StringUtils.isNotBlank(form.getServer()))
                auditLog.addParam("下載主機", form.getServer());
            if (StringUtils.isNotBlank(form.getLogType()))
                auditLog.addParam("選擇下載的Log", form.getLogType());
            if (StringUtils.isNotBlank(form.getChannelDownload()))
                auditLog.addParam("選擇下載的通道", form.getChannelDownload());
            if (StringUtils.isNotBlank(appName))
                auditLog.addParam("選擇下載的服務名稱", appName);
            if (StringUtils.isNotBlank(form.getSelectGZLog()))
                auditLog.addParam("gz檔", form.getSelectGZLog());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            // 如果選擇的主機名稱等於spring.fep.hostname, 代表要取得的檔案在本機上, 就直接呼叫GetAPLogFiles function就不用再透過WebAPI了
            if (server.equals(FEPConfig.getInstance().getHostName())) {
                this.infoMessage("從本機抓取APLog檔案");
                // 先取出檔案的總大小
                long totalSize = GetApLogFilesUtil.getApLogFilesTotalSize(ApLogType.valueOf(logType), logDate, fepLogPath, fepWasLogPath, fepLogArchivesPath, fepWasLogArchivesPath, logTimeBegin, logTimeEnd, appName);
                // 如果總大小為0就回報錯誤訊息
                if (totalSize == 0) {
                    return this.handleDownloadError("指定區間的LOG大小總共為", FormatUtil.formatBytes(totalSize), ", 請確認查詢區間是否正確");
                }
                // 如果超過限制就回報錯誤訊息
                else if (totalSize > totalSizeLimit) {
                    return this.handleDownloadError("指定區間的LOG大小總共為", FormatUtil.formatBytes(totalSize), ", 已超過系統限制", FormatUtil.formatBytes(totalSizeLimit), ", 請縮小查詢區間");
                }
                this.showMessage(mode, MessageType.INFO, "下載成功");
                if (StringUtils.isBlank(selectGZLog)) {
                    String finalAppName = appName;
                    return this.download(getDownloadFileName(appName, logDate, logTimeBegin, logTimeEnd, logType, null),
                            MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            os -> {
                                try {
                                    GetApLogFilesUtil.getApLogFiles(os, ApLogType.valueOf(logType), logDate, fepLogPath, fepWasLogPath, fepLogArchivesPath, fepWasLogArchivesPath, logTimeBegin, logTimeEnd, StringUtils.EMPTY, finalAppName);
                                } catch (Exception e) {
                                    this.errorMessage(e, e.getMessage());
                                    String errorMessage = e.getMessage();
                                    String prefix = "ERROR:";
                                    if (StringUtils.isNotBlank(errorMessage) && errorMessage.startsWith(prefix)) {
                                        throw ExceptionUtil.createException(e, e.getMessage().substring(prefix.length()));
                                    }
                                    throw e;
                                }
                            }
                    );
                } else {
                    // 下載本地指定的tar檔
                    String logPath = fepLogArchivesPath + "/" + logDate + "/" + selectGZLog;
                    return this.download(logPath);
                }
            }
            // 根據選擇的主機名稱call 各主機上的GetAPLog WebAPI, 傳送logType及logDate給WebAPI後取回檔案byte array再Response回Client
            else {
                List<WebApLogConfiguration> aplogList = WebConfiguration.getInstance().getAplog();
                WebApLogConfiguration webApLogConfiguration = aplogList.stream().filter(t -> t.getServer().equals(server)).findFirst().orElse(null);
                if (webApLogConfiguration != null) {
                    this.infoMessage("透過", webApLogConfiguration.getService(), "遠程抓取APLog檔案");
                    StringBuilder sb = new StringBuilder();
                    sb.append(webApLogConfiguration.getService())
                            .append("?").append("operator=").append(WebUtil.getUser().getLoginId())
                            .append("&").append("logType=").append(logType)
                            .append("&").append("logDate=").append(logDate)
                            .append("&").append("fepLogPath=").append(fepLogPath)
                            .append("&").append("fepLogArchivesPath=").append(fepLogArchivesPath)
                            .append("&").append("logTimeBegin=").append(logTimeBegin)
                            .append("&").append("logTimeEnd=").append(logTimeEnd)
                            .append("&").append("selectGZLog=").append(selectGZLog)
                            .append("&").append("sizeLimit=").append(WebConfiguration.getInstance().getGetApLogTotalSizeLimit())
                            .append("&").append("appName=").append(appName);
                    String url = sb.toString();
                    // send Restful request
                    RestTemplate restTemplate = new RestTemplate();
                    // 2025-10-17 Richard modified
                    if (HttpClient.isHttps(url)) {
                        restTemplate.setRequestFactory(HttpClientConfiguration.createTrustAnyHttpComponentsClientHttpRequestFactory(SslContextFactory.PROTOCOL, FEPConfig.getInstance().getRestfulTimeout()));
                    } else {
                        restTemplate.setRequestFactory(HttpClientConfiguration.createSimpleClientHttpRequestFactory(FEPConfig.getInstance().getRestfulTimeout()));
                    }
                    this.debugMessage("[", url, "]", Const.MESSAGE_OUT);
                    this.showMessage(mode, MessageType.INFO, "下載成功");
                    return this.download(getDownloadFileName(appName, logDate, logTimeBegin, logTimeEnd, logType, selectGZLog),
                            MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            os -> {
                                try {
                                    restTemplate.execute(HttpClient.toUriString(url), HttpMethod.GET, null,
                                            response -> {
                                                this.debugMessage("[", url, "]", Const.MESSAGE_IN);
                                                InputStream in = response.getBody();
                                                // 先取出前5個byte, 如果是ERROR就讀取剩下的byte, 並轉換成String, 然後拋出異常
                                                byte[] prefix = new byte[5];
                                                if (in.read(prefix) != -1) {
                                                    String str = new String(prefix, StandardCharsets.UTF_8);
                                                    if ("ERROR".equals(str)) {
                                                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                                        IOUtils.copy(in, bos);
                                                        // 跳過第一個字節, 剩下的全是錯誤信息
                                                        throw ExceptionUtil.createIOException(StringUtils.join("ERROR:從遠端獲取APLog檔案失敗, ", bos.toString(StandardCharsets.UTF_8).substring(1)));
                                                    }
                                                }
                                                os.write(prefix);
                                                // 將遠端服務器返回的日誌檔案流(in)複製到響應輸出流(os)中，實現檔案下載功能
                                                IOUtils.copy(in, os);
                                                return null;
                                            });
                                } catch (RestClientException e) {
                                    this.errorMessage(e, e.getMessage());
                                    if (e.getCause() instanceof IOException && e.getCause() != null) {
                                        String errorMessage = e.getCause().getMessage();
                                        String prefix = "ERROR:";
                                        if (StringUtils.isNotBlank(errorMessage) && errorMessage.startsWith(prefix)) {
                                            throw ExceptionUtil.createException(e, e.getCause().getMessage().substring(prefix.length()));
                                        }
                                    }
                                    throw ExceptionUtil.createException(e, "無法從遠端獲取APLog檔案!!!");
                                }
                            }
                    );
                } else {
                    return this.handleDownloadError("沒有找到對應的APLog設定");
                }
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            String errorMessage = e.getMessage();
            String prefix = "ERROR:";
            if (StringUtils.isNotBlank(errorMessage) && errorMessage.startsWith(prefix)) {
                return this.handleDownloadError(e.getMessage().substring(prefix.length()));
            }
            return this.handleDownloadException(e);
        }
    }

    /**
     * 下載檔案的名字
     *
     * @param appName
     * @param logDate
     * @param logTimeBegin
     * @param logTimeEnd
     * @param logType
     * @param selectGZLog
     * @return
     */
    private String getDownloadFileName(String appName, String logDate, String logTimeBegin, String logTimeEnd, String logType, String selectGZLog) {
        if (StringUtils.isNotBlank(selectGZLog)) {
            return selectGZLog;
        }
        StringBuilder downloadFileName = new StringBuilder();
        // 只有下載當日的log檔, 才會帶入AppName
        if (StringUtils.isNotBlank(appName) && logDate.equals(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH))) { // AppName
            downloadFileName.append(StringUtil.split(appName, ',', ';').get(0)).append("-");
        }
        downloadFileName.append(logDate); // yyyy-MM-dd
        if (StringUtils.isNotBlank(logTimeBegin)) { // HH-HH
            downloadFileName.append("-").append(logTimeBegin, 0, 2).append("-").append(logTimeEnd, 0, 2);
        }
        if (logType.equals(ApLogType.waslog.name())) { // -waslog
            downloadFileName.append("-waslog");
        }
        downloadFileName.append(".tar.gz");
        return downloadFileName.toString();
    }

// 2023-10-08 Richard marked start
//    /**
//     * 20230504 Bruce add log檔下載
//     *
//     * @param form
//     * @param mode
//     * @return
//     * @throws IOException
//     */
//    @PostMapping(value = "/atmmon/UI_060610/download")
//    public void download(@ModelAttribute UI_060610_FormMain form, HttpServletResponse response) {
//
//        boolean os = false;
//
//        String filePathLogs = "";
//        String fileName = "";
//        String fileNameGz = "";
//        OutputStream out = null;
//        InputStream in = null;
//        String[] param = {};
//        try {
//            if (System.getProperty("os.name") != null) {
//                os = System.getProperty("os.name").toLowerCase().contains("windows");
//            }
//            if (os) {
//                out = response.getOutputStream();
//                fileName = form.getTransactDate();
//                param = new String[4];
//                param[0] = "cmd";
//                param[1] = "/C";
//                param[2] = "Big5";
////				if (form.getSelectLog().equals("waslogs")) {
////					filePathLogs = WebConfiguration.getInstance().getFepWasLogPath();//"/home/syscom/fep-app/waslogs";
////					fileNameGz = fileName + "-waslog.tar.gz";
////                } else {
////					filePathLogs = WebConfiguration.getInstance().getFepLogPath();//"/home/syscom/fep-app/logs";
////					fileNameGz = fileName + ".tar.gz";
////                }
//                param[3] = "cd /fep/" + form.getSelectLog() + " && tar -czvf " + fileNameGz + " " + fileName;
//                this.debugMessage("command:" + param[3]);
//                // 先下指令將log folder tar 起來
//                Process process = new ProcessBuilder(new String[] {param[0], param[1], param[3]}).start();
////        inputStreamReader = new BufferedReader(new InputStreamReader(process.getInputStream(), param[2]));
////				process.waitFor();
//                if (process.waitFor(1000, TimeUnit.MILLISECONDS)) {
//                    System.out.println("process exited");
//                } else {
//                    System.out.println("process is still running");
//                }
//                // tar完後直接下載
//                response.setHeader("Content-Disposition",
//                        "attachment; filename=\"" + URLEncoder.encode(fileNameGz, param[2]) + "\"");
//                in = new FileInputStream(filePathLogs + fileNameGz);
//                this.debugMessage("logPath:" + filePathLogs + File.separator + fileNameGz);
//                byte[] buffer = new byte[1024];
//                int len = 0;
//                while ((len = in.read(buffer)) != -1) {
//                    out.write(buffer, 0, len);
//                }
//                out.flush();
//                out.close();
//            } else {
//                Process process = null;
//                out = response.getOutputStream();
//                fileName = form.getTransactDate();
//                param = new String[4];
//                param[0] = "/bin/sh";
//                param[1] = "-c";
//                param[2] = "UTF-8";
//                if (form.getSelectLog().equals("waslogs")) {
//                    filePathLogs = WebConfiguration.getInstance().getFepWasLogPath();
//                    fileNameGz = fileName + "-waslog.tar.gz";
//                } else {
//                    //判斷log資料夾是否存在
//                    filePathLogs = WebConfiguration.getInstance().getFepLogPath();
//                    File file = new File(filePathLogs + File.separator + fileName);
//                    //如果不存在就把路徑換到archives底下
//                    if (!file.exists()) {
//                        filePathLogs = WebConfiguration.getInstance().getFepLogArcivesPath();
//                        fileNameGz = fileName + ".tar.gz";
//                        //param[3] = "cd " + filePathLogs;
//                        //process = new ProcessBuilder(new String[] { param[0], param[1], param[3] }).start();
//                        this.debugMessage("command:" + param[3]);
//                    } else {
////						fileNameGz = fileName + ".tar.gz";
//                        fileNameGz = fileName + ".gz";
////						param[3] = "cd " + filePathLogs + " && tar -cvf " + fileNameGz + " " + fileName;
//                        param[3] = "cd " + filePathLogs + " && tar cf - " + fileName + " | gzip > " + fileNameGz;
//                        // 先下指令將log folder tar 起來
//                        process = new ProcessBuilder(new String[] {param[0], param[1], param[3]}).start();
//                        this.debugMessage("command:" + param[3]);
//                        process.waitFor(3000, TimeUnit.SECONDS);
//                    }
//                }
//
//                in = new FileInputStream(filePathLogs + File.separator + fileNameGz);
//                this.debugMessage("logPath:" + filePathLogs + File.separator + fileNameGz);
//                // waslog tar完後直接下載
//                response.setHeader("Content-Disposition",
//                        "attachment; filename=\"" + URLEncoder.encode(fileNameGz, param[2]) + "\"");
//                byte[] buffer2 = new byte[1024];
//                int len2 = 0;
//                while ((len2 = in.read(buffer2)) != -1) {
//                    out.write(buffer2, 0, len2);
//                }
//                process = null;
//                param[3] = "cd " + filePathLogs + " && rm -rf " + fileNameGz;
//                process = new ProcessBuilder(new String[] {param[0], param[1], param[3]}).start();
//                process.waitFor();
//            }
//        } catch (
//                Exception e) {
//            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
//        } finally {
//            if (in != null) {
//                try {
//                    in.close();
//                } catch (IOException ie) {
//                    LogHelperFactory.getTraceLogger().warn(ie, ie.getMessage());
//                }
//            }
//            if (out != null) {
//                try {
//                    out.flush();
//                    out.close();
//                } catch (IOException ie) {
//                    LogHelperFactory.getTraceLogger().warn(ie, ie.getMessage());
//                }
//            }
//        }
//    }
// 2023-10-08 Richard marked end

    /**
     * 查詢功能
     *
     * @param form
     * @param mode
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/atmmon/UI_060610/queryClick")
    public String queryClick(@ModelAttribute UI_060610_FormMain form, ModelMap mode, HttpServletResponse response, HttpServletRequest request) throws IOException {
        this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
        form.setUrl("/atmmon/UI_060610/queryClick");
        //分頁
        try {
            this.setChannelOptions(mode);//通道下拉選單
            this.setServerDownload(mode, form);//下載主機下拉選單
            this.setServerOptions(mode);
            this.setLogTypeOptions(mode, form);
            this.setChannelDownloadOptions(mode, form);
            this.doKeepFormData(mode, form);
            if (this.checkAllField(form, mode)) {
                this.setData(form);
                Map<String, Object> argsMap = form.toMap();
                argsMap.put("tableNameSuffix", this.getDayOfWeek(form.getTransactDate()));
                argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
                // 記錄auditLog
                AuditLog auditLog = new AuditLog();
                // 塞入auditLog.action
                auditLog.setAction("查詢");
                // 塞入auditLog.params
                auditLog.addParam("交易日期", form.getTransactDate());
                if (StringUtils.isNotBlank(form.getFeptxnTxTimeBegin()) && StringUtils.isNotBlank(form.getFeptxnTxTimeEnd()))
                    auditLog.addParam("交易時間起訖", form.getFeptxnTxTimeBegin() + "~" + form.getFeptxnTxTimeEnd());
                if (StringUtils.isNotBlank(form.getTxtAtmNo()))
                    auditLog.addParam("ATM代號", form.getTxtAtmNo());
                if (StringUtils.isNotBlank(form.getAtmSeqTxt()))
                    auditLog.addParam("ATM序號", form.getAtmSeqTxt());
                if (StringUtils.isNotBlank(form.getTxtMessageId()))
                    auditLog.addParam("訊息ID", form.getTxtMessageId());
                if (StringUtils.isNotBlank(form.getTxtTroutActNo()))
                    auditLog.addParam("轉出帳號", form.getTxtTroutActNo());
                if (StringUtils.isNotBlank(form.getEjNoUc()))
                    auditLog.addParam("EJ序號", form.getEjNoUc());
                if (StringUtils.isNotBlank(form.getChannelUc()))
                    auditLog.addParam("通道", form.getChannelUc());
                if (StringUtils.isNotBlank(form.getMsgFlowUc2()))
                    auditLog.addParam("訊息流程", form.getMsgFlowUc2());
                if (StringUtils.isNotBlank(form.getTxtBkNo()))
                    auditLog.addParam("財金 STAN", form.getTxtBkNo() + "-" + form.getStanTxt());
                if (StringUtils.isNotBlank(form.getServer()))
                    auditLog.addParam("下載主機", form.getServer());
                if (StringUtils.isNotBlank(form.getHostname()))
                    auditLog.addParam("查詢主機", form.getHostname());
                // 最後塞入ThreadLocal變量中
                setAuditLog(auditLog);
                PageInfo<Feplog> pageInfo = emsService.getfepLog(argsMap);
                if (pageInfo.getSize() == 0) {
                    this.showMessage(mode, MessageType.INFO, QueryNoData);
                } else {
                    this.showMessage(mode, MessageType.INFO, QuerySuccess);
                }
                PageData<UI_060610_FormMain, Feplog> pageData = new PageData<UI_060610_FormMain, Feplog>(pageInfo, form);
                WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
            }
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_060610.getView();
    }

    /**
     * 由EJ及LogDate查回Feptxn
     *
     * @param form
     * @param mode
     * @return
     */
    @PostMapping(value = "/atmmon/UI_060610/queryFepTxnClick")
    public String queryFepTxnClick(@ModelAttribute UI_060610_FormDetail form, ModelMap mode) {
        this.infoMessage("明細主檔資料, 條件 = [", form.toString(), "]");
        this.changeTitle(mode, changeTitleFor550_A);
        this.doKeepFormData(mode, form);
        try {
            int[] ejArray = Arrays.stream(form.getEj().toString().split(","))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            String sTime = form.getLogdateTxt().toString().substring(11, 16).replace(":", "");
            String transactDate = form.getLogdateTxt().toString().replace("/", "").substring(0, 8);
            //若大於1530則換一日
            if (Integer.parseInt(sTime) > 1530) {
                transactDate = atmService.getNbsDays(transactDate);
            }
            Map<String, Object> argsMap = form.toMap();
            argsMap.put("tableNameSuffix", transactDate.length() == 10 ? transactDate.substring(8, 10) : transactDate.substring(6, 8));
            argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
            argsMap.put("feptxnEjfno", ejArray);
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("明細查詢");
            // 塞入auditLog.params
            auditLog.addParam("EJ", argsMap.get("ej"));
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            PageInfo<Feptxn> pageInfo = atmService.getFeptxnByEjfno(argsMap);
            if (pageInfo.getSize() == 0) {
                this.getPageInfo(pageInfo, argsMap, "TWN", transactDate);
                if (pageInfo.getSize() == 0) {
                    this.getPageInfo(pageInfo, argsMap, "HKG", transactDate);
                    if (pageInfo.getSize() == 0) {
                        this.getPageInfo(pageInfo, argsMap, "MAC", transactDate);
                        if (pageInfo.getSize() == 0) {
                            this.showMessage(mode, MessageType.INFO, QueryNoData);
                        }
                    }
                }
            } else {
                this.showMessage(mode, MessageType.INFO, QuerySuccess);
            }
            PageData<UI_060610_FormDetail, Feptxn> pageData = new PageData<UI_060610_FormDetail, Feptxn>(pageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_060550_A.getView();
    }

    /**
     * EJ超連結
     *
     * @param form
     * @param mode
     * @return
     */
    @PostMapping(value = "/atmmon/UI_060610/bindGridDetail")
    public String bindGridDetail(@ModelAttribute UI_060610_FormDetail form, ModelMap mode) {
        this.infoMessage("明細主檔資料, 條件 = [", form.toString(), "]");
//        this.changeTitle(mode, changeTitleFor550_A);
        this.doKeepFormData(mode, form);
        try {
            if (StringUtils.isNotBlank(form.getBkno()) || StringUtils.isNotBlank(form.getStan())) {
                form.setBknoStan(form.getBkno() + "-" + form.getStan());
            }
            if (form.getLogdate() != null) {
                SimpleDateFormat inputDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
                Date logDate = inputDateFormat.parse(form.getLogdate());
                SimpleDateFormat sdf = new SimpleDateFormat(this.yyyyMMddHHmmss);
                form.setLogdateTxt(sdf.format(logDate));
            }
            //2023-04-28 Bruce add EBCDIC to ASCII
//            if (form.getProgramflow().equals("AAIn") || form.getProgramflow().equals("AdapterIn")) {
//                if (form.getTxmessage().length() == 1018) {
//                    form.setTxMessageAscii(ebcdicToAscii1018(form.getTxmessage()));
//                } else if (form.getTxmessage().length() == 1016) {//國際卡EMV、國際卡密碼變更
//                    form.setTxMessageAscii(ebcdicToAscii1016(form.getTxmessage()));
//                } else {//磁條密碼變更交易、FC_非晶片(查詢企業名稱FC6)、換KEY(PP、P3)
//                    if (form.getTxmessage().length() == 612) {//磁條密碼變更交易
//                        form.setTxMessageAscii(ebcdicToAscii612(form.getTxmessage()));
//                    } else if (form.getTxmessage().length() == 508) {//FC_非晶片(查詢企業名稱FC6)
//                        form.setTxMessageAscii(ebcdicToAscii508(form.getTxmessage()));
//                    } else {
//                        this.showMessage(mode, MessageType.DANGER, "無此電文");
//                    }
//                }
//            } else {
            form.setTxMessageAscii(EbcdicConverter.fromHex(CCSID.English, form.getTxmessage()));
//            }
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
            return Router.UI_060610_Detail.getView();
        }
        return Router.UI_060610_Detail.getView();
    }

    private String ebcdicToAscii1018(String ebcdic) throws DecoderException {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("APTRAN", ebcdic.substring(0, 16));
        map.put("FILLER1", ebcdic.substring(16, 18));
        map.put("WSID", ebcdic.substring(18, 28));
        map.put("MACMODE", ebcdic.substring(28, 30));
        map.put("RECFMT", ebcdic.substring(30, 32));
        map.put("APPLUSE", ebcdic.substring(32, 34));
        map.put("MSGCAT", ebcdic.substring(34, 36));
        map.put("MSGTYP", ebcdic.substring(36, 40));
        map.put("TRANDATE", ebcdic.substring(40, 52));
        map.put("TRANTIME", ebcdic.substring(52, 64));
        map.put("TRANSEQ", ebcdic.substring(64, 72));
        map.put("TDRSEG", ebcdic.substring(72, 76));
        map.put("STATUS", ebcdic.substring(76, 106));
        map.put("IPYDATA", ebcdic.substring(106, 142));
        map.put("LANGID", ebcdic.substring(142, 146));
        map.put("FSCODE", ebcdic.substring(146, 150));
        map.put("FADATA", ebcdic.substring(150, 194));
        map.put("TACODE", ebcdic.substring(194, 198));
        map.put("TADATA", ebcdic.substring(198, 238));
        map.put("AMTNOND", ebcdic.substring(238, 260));
        map.put("AMTDISP", ebcdic.substring(260, 280));
        map.put("DOCLASS", ebcdic.substring(280, 282));
        map.put("CARDFMT", ebcdic.substring(282, 284));
        map.put("CARDDATA", ebcdic.substring(284, 358));
        map.put("PICCDID", ebcdic.substring(358, 360));
        map.put("FILLER5", ebcdic.substring(360, 362));
        map.put("PICCDLTH", ebcdic.substring(362, 368));
        map.put("PICCPCOD", ebcdic.substring(368, 376));
        map.put("PICCTXID", ebcdic.substring(376, 378));
        map.put("PICCBI9", ebcdic.substring(378, 394));
        map.put("PICCBI11", ebcdic.substring(394, 410));
        map.put("PICCBI19", ebcdic.substring(410, 438));
        map.put("PICCBI28", ebcdic.substring(438, 446));
        map.put("PICCBI55", ebcdic.substring(446, 506));
        map.put("SPECIALDATA", ebcdic.substring(506, 742));
        map.put("PICCMACD", ebcdic.substring(742, 758));
        map.put("PICCTACL", ebcdic.substring(758, 762));
        map.put("PICCTAC", ebcdic.substring(762, 1018));
//	    System.out.println("ASCII length="+ascii.length());
//	    System.out.println("ASCII="+ascii);
        return eToA(map);
    }

    private String ebcdicToAscii1016(String ebcdic) throws DecoderException {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("APTRAN", ebcdic.substring(0, 16));
        map.put("FILLER1", ebcdic.substring(16, 18));
        map.put("WSID", ebcdic.substring(18, 28));
        map.put("MACMODE", ebcdic.substring(28, 30));
        map.put("RECFMT", ebcdic.substring(30, 32));
        map.put("APPLUSE", ebcdic.substring(32, 34));
        map.put("MSGCAT", ebcdic.substring(34, 36));
        map.put("MSGTYP", ebcdic.substring(36, 40));
        map.put("TRANDATE", ebcdic.substring(40, 52));
        map.put("TRANTIME", ebcdic.substring(52, 64));
        map.put("TRANSEQ", ebcdic.substring(64, 72));
        map.put("TDRSEG", ebcdic.substring(72, 76));
        map.put("STATUS", ebcdic.substring(76, 106));
        map.put("PIPTRIES", ebcdic.substring(106, 110));
        map.put("SSCODE", ebcdic.substring(110, 142));
        map.put("LANGID", ebcdic.substring(142, 146));
        map.put("FSCODE", ebcdic.substring(146, 150));
        map.put("ACCODE", ebcdic.substring(150, 154));
        map.put("PICDSEQ", ebcdic.substring(154, 160));
        map.put("PIPOSENT", ebcdic.substring(160, 168));
        map.put("PITRMTYP", ebcdic.substring(168, 176));
        map.put("PITK2FRM", ebcdic.substring(176, 178));
        map.put("PIARPCRC", ebcdic.substring(178, 180));
        map.put("FILLER2", ebcdic.substring(180, 194));
        map.put("TACODE", ebcdic.substring(194, 198));
        map.put("TADATA", ebcdic.substring(198, 238));
        map.put("AMTNOND", ebcdic.substring(238, 260));
        map.put("AMTDISP", ebcdic.substring(260, 280));
        map.put("DOCLASS", ebcdic.substring(280, 282));
        map.put("CARDFMT", ebcdic.substring(282, 284));
        map.put("TRK2", ebcdic.substring(284, 358));
        map.put("FILLER3", ebcdic.substring(358, 368));
        map.put("PICCPCOD", ebcdic.substring(368, 376));
        map.put("PIARQCLN", ebcdic.substring(376, 380));
        map.put("PIARQC", ebcdic.substring(380, 750));
        map.put("PICCMACD", ebcdic.substring(750, 766));
        map.put("FILLER4", ebcdic.substring(766, 1016));
//	    System.out.println("ASCII length="+ascii.length());
//	    System.out.println("ASCII="+ascii);
        return eToA(map);
    }

    private String ebcdicToAscii612(String ebcdic) throws DecoderException {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("APTRAN", ebcdic.substring(0, 16));
        map.put("FILLER1", ebcdic.substring(16, 18));
        map.put("WSID", ebcdic.substring(18, 28));
        map.put("MACMODE", ebcdic.substring(28, 30));
        map.put("RECFMT", ebcdic.substring(30, 32));
        map.put("APPLUSE", ebcdic.substring(32, 34));
        map.put("MSGCAT", ebcdic.substring(34, 36));
        map.put("MSGTYP", ebcdic.substring(36, 40));
        map.put("TRANDATE", ebcdic.substring(40, 52));
        map.put("TRANTIME", ebcdic.substring(52, 64));
        map.put("TRANSEQ", ebcdic.substring(64, 72));
        map.put("TDRSEG", ebcdic.substring(72, 76));
        map.put("STATUS", ebcdic.substring(76, 106));
        map.put("PIPTRIES", ebcdic.substring(106, 110));
        map.put("PINCODE", ebcdic.substring(110, 142));
        map.put("LANGID", ebcdic.substring(142, 146));
        map.put("FSCODE", ebcdic.substring(146, 150));
        map.put("FACODE", ebcdic.substring(150, 154));
        map.put("FADATA", ebcdic.substring(154, 194));
        map.put("TACODE", ebcdic.substring(194, 198));
        map.put("TADATA", ebcdic.substring(198, 238));
        map.put("AMTNOND", ebcdic.substring(238, 260));
        map.put("AMTDISP", ebcdic.substring(260, 280));
        map.put("DOCLASS", ebcdic.substring(280, 282));
        map.put("CARDFMT", ebcdic.substring(282, 284));
        map.put("CARDDATA", ebcdic.substring(284, 384));
        map.put("CARDPART1", ebcdic.substring(384, 426));
        map.put("PART1", ebcdic.substring(426, 428));
        map.put("CARDPART2", ebcdic.substring(428, 508));
        map.put("PART2", ebcdic.substring(508, 510));
        map.put("CARDPART3", ebcdic.substring(510, 584));
        map.put("PART3", ebcdic.substring(584, 586));
        map.put("PART4", ebcdic.substring(586, 588));
        map.put("PARTV", ebcdic.substring(588, 590));
        map.put("PART5", ebcdic.substring(590, 592));
        map.put("FILLER2", ebcdic.substring(592, 596));
        map.put("MACODE", ebcdic.substring(596, 612));
//	    System.out.println("ASCII length="+ascii.length());
//	    System.out.println("ASCII="+ascii);
        return eToA(map);
    }

    private String ebcdicToAscii508(String ebcdic) throws DecoderException {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("APTRAN", ebcdic.substring(0, 16));
        map.put("FILLER1", ebcdic.substring(16, 18));
        map.put("WSID", ebcdic.substring(18, 28));
        map.put("MACMODE", ebcdic.substring(28, 30));
        map.put("RECFMT", ebcdic.substring(30, 32));
        map.put("APPLUSE", ebcdic.substring(32, 34));
        map.put("MSGCAT", ebcdic.substring(34, 36));
        map.put("MSGTYP", ebcdic.substring(36, 40));
        map.put("TRANDATE", ebcdic.substring(40, 52));
        map.put("TRANTIME", ebcdic.substring(52, 64));
        map.put("TRANSEQ", ebcdic.substring(64, 72));
        map.put("TDRSEG", ebcdic.substring(72, 76));
        map.put("STATUS", ebcdic.substring(76, 106));
        map.put("FILLER2", ebcdic.substring(106, 126));
        map.put("PITMID", ebcdic.substring(126, 142));
        map.put("PITMID", ebcdic.substring(126, 142));
        map.put("LANGID", ebcdic.substring(142, 146));
        map.put("FSCODE", ebcdic.substring(146, 150));
        map.put("FACODE", ebcdic.substring(150, 154));
        map.put("FADATA", ebcdic.substring(154, 194));
        map.put("TACODE", ebcdic.substring(194, 198));
        map.put("TADATA", ebcdic.substring(198, 238));
        map.put("AMTNOND", ebcdic.substring(238, 260));
        map.put("AMTDISP", ebcdic.substring(260, 280));
        map.put("DOCLASS", ebcdic.substring(280, 282));
        map.put("CARDFMT", ebcdic.substring(282, 284));
        map.put("FILLER3", ebcdic.substring(284, 290));
        map.put("T3BNKID", ebcdic.substring(290, 296));
        map.put("FILLER4", ebcdic.substring(296, 492));
        map.put("PICCMACD", ebcdic.substring(492, 508));
//	    System.out.println("ASCII length="+ascii.length());
//	    System.out.println("ASCII="+ascii);
        return eToA(map);
    }

    private String eToA(Map<String, String> maps) throws DecoderException {
        StringBuilder sp = new StringBuilder();
        for (String map : maps.keySet()) {
//			if(!map.equals("PICCBI55") && !map.equals("PICCTAC") && !map.equals("PICCTACL")) {
            if (!map.equals("PICCBI55") && !map.equals("PICCTACL")) {
                System.out.println(map + " EbcdicToAscii=" + EbcdicConverter.fromHex(CCSID.English, maps.get(map)));
                sp.append(EbcdicConverter.fromHex(CCSID.English, maps.get(map)));
                //}else if(map.equals("PICCBI55") || map.equals("PICCTACL") || map.equals("PICCTAC")){
            } else {
                System.out.println(map + " EBCDIC=" + maps.get(map));
                System.out.println(map + " StringUtilToAscii=" + StringUtil.fromHex(maps.get(map)));
                sp.append(StringUtil.fromHex(maps.get(map)));
                //}else {
                //	System.out.println(map+" EBCDIC="+maps.get(map));
                //	System.out.println(map+" Ascii="+maps.get(map));
                //	sp.append(maps.get(map));
            }
        }
        return sp.toString();
    }

    /**
     * 查詢FEPTXN明細頁
     *
     * @param form
     * @param mode
     * @return
     */
    @PostMapping(value = "/atmmon/UI_060610/syscomDetail")
    public String syscomDetail(@ModelAttribute UI_060550_A_FormDetail form, ModelMap mode) {
        this.infoMessage("明細主檔資料, 條件 = [", form.toString(), "]");
        this.changeTitle(mode, changeTitleFor550_A);
        if (StringUtils.isNotBlank(form.getFeptxnTroutActno()) || StringUtils.isNotBlank(form.getFeptxnTroutBkno())) {
            form.setFeptxnTroutActnoBknoTxt(form.getFeptxnTroutBkno() + "-" + form.getFeptxnTroutActno());
        }

        if (StringUtils.isNotBlank(form.getFeptxnTrinActno()) || StringUtils.isNotBlank(form.getFeptxnTrinBkno())) {
            form.setFeptxnTrinBknoActnoTxt(form.getFeptxnTrinBkno() + "-" + form.getFeptxnTrinActno());
        }
        this.doKeepFormData(mode, form);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        return Router.UI_060550_A_Detail.getView();
    }

    /**
     * 通道下拉選單
     *
     * @param mode
     * @throws Exception
     */
    private void setChannelOptions(ModelMap mode) throws Exception {
        List<Channel> channelList = channelService.queryAllDataAsc();//調整成要顯示的才取出
        List<SelectOption<String>> selectOptionList = new ArrayList<>();
        selectOptionList.add(new SelectOption<>(this.pleaseChoose, StringUtils.EMPTY));
        selectOptionList.add(new SelectOption<>("FISC", "FISC"));
        for (int i = 0; i < channelList.size(); i++) {
            selectOptionList.add(new SelectOption<>(channelList.get(i).getChannelName(), channelList.get(i).getChannelNameS()));
        }
        WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList);
    }

    private void setServerOptions(ModelMap mode) throws Exception {
        List<WebApLogConfiguration> apLogList = WebConfiguration.getInstance().getAplog();
        List<SelectOption<String>> selectOptionList = new ArrayList<>();
        selectOptionList.add(new SelectOption<>(this.pleaseChoose, StringUtils.EMPTY));
        for (WebApLogConfiguration apLog : apLogList) {
            selectOptionList.add(new SelectOption<>(apLog.getServer(), apLog.getServer()));
        }
        WebUtil.putInAttribute(mode, AttributeName.SelectServer, selectOptionList);
    }

    private void setServerDownload(ModelMap mode, UI_060610_FormMain formMain) throws Exception {
        List<SelectOption<String>> selectOptionList = new ArrayList<>();
        List<WebApLogConfiguration> apLogList = WebConfiguration.getInstance().getAplog();
        for (WebApLogConfiguration apLog : apLogList) {
            selectOptionList.add(new SelectOption<>(apLog.getServer(), apLog.getServer()));
        }
        WebUtil.putInAttribute(mode, AttributeName.SelectServerDownload, selectOptionList);
        // 2026-02-13 Richard add 如果表單沒有塞入server, 這裡要塞入
        if (StringUtils.isBlank(formMain.getServer())) {
            formMain.setServer(apLogList.get(0).getServer());
        }
    }

    private void setLogTypeOptions(ModelMap mode, UI_060610_FormMain form) throws Exception {
        List<WebApLogConfiguration> apLogList = WebConfiguration.getInstance().getAplog();
        List<String> logtypeList = null;
        if (form == null || StringUtils.isBlank(form.getServer())) {
            logtypeList = apLogList.get(0).getLogtype();
        } else {
            WebApLogConfiguration webApLogConfiguration = apLogList.stream().filter(t -> t.getServer().equals(form.getServer())).findFirst().orElse(null);
            if (webApLogConfiguration != null) {
                logtypeList = webApLogConfiguration.getLogtype();
            }
        }
        List<SelectOption<String>> selectOptionList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(logtypeList)) {
            for (String logType : logtypeList) {
                selectOptionList.add(new SelectOption<>(logType, logType));
            }
        }
        WebUtil.putInAttribute(mode, AttributeName.SelectLogType, selectOptionList);
    }

    @PostMapping(value = "/atmmon/UI_060610/getSelectLog")
    @ResponseBody
    public Map<String, List<SelectOption<String>>> selectData(@RequestBody UI_060610_FormMain formMain) {
        this.infoMessage("下拉選單連動, 條件 = [", formMain.toString(), "]");
        // 記錄auditLog
        AuditLog auditLog = new AuditLog();
        // 塞入auditLog.action
        auditLog.setAction("選擇下載主機連動選擇下載的通道和選擇下載的Log下拉選單內容");
        // 塞入auditLog.params
        auditLog.addParam("下載主機", formMain.getServer());
        // 最後塞入ThreadLocal變量中
        setAuditLog(auditLog);
        List<SelectOption<String>> logtypeList = new ArrayList<>();
        try {
            List<WebApLogConfiguration> apLogList = WebConfiguration.getInstance().getAplog();
            WebApLogConfiguration webApLogConfiguration = apLogList.stream().filter(t -> t.getServer().equals(formMain.getServer())).findFirst().orElse(null);
            if (webApLogConfiguration != null && CollectionUtils.isNotEmpty(webApLogConfiguration.getLogtype())) {
                webApLogConfiguration.getLogtype().forEach(t -> logtypeList.add(new SelectOption<>(t, t)));
            }
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
        }
        List<SelectOption<String>> channelDownloadList = new ArrayList<>();
        channelDownloadList.add(new SelectOption<>(this.pleaseChoose, StringUtils.EMPTY));
        Map<String, String> channelToAppName = getChannelToAppNameMap(formMain.getServer());
        if (MapUtils.isNotEmpty(channelToAppName)) {
            channelToAppName.keySet().forEach(key -> channelDownloadList.add(new SelectOption<>(key, key)));
        }
        Map<String, List<SelectOption<String>>> map = new HashMap<>();
        map.put("logType", logtypeList);
        map.put("channelDownload", channelDownloadList);
        return map;
    }

    /**
     * 檢核所欄位
     *
     * @param form
     * @param mode
     * @return
     * @throws ParseException
     */
    private boolean checkAllField(UI_060610_FormMain form, ModelMap mode) throws ParseException {
        //如果EJ不為空 判斷是否為數字或是逗號
        if (StringUtils.isNotBlank(form.getEjNoUc())) {
            char[] chars = form.getEjNoUc().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (!Character.isDigit(chars[i]) && chars[i] != ',') {
                    this.showMessage(mode, MessageType.DANGER, EJFNOComma);
                    return false;
                }
            }
        }
        if (StringUtils.isNotBlank(form.getFeptxnTxTimeBeginTxt()) && StringUtils.isNotBlank(form.getFeptxnTxTimeEndTxt())) {
            int begin = Integer.valueOf(form.getFeptxnTxTimeBeginTxt().replace(":", "").replace(".", ""));
            int end = Integer.valueOf(form.getFeptxnTxTimeEndTxt().replace(":", "").replace(".", ""));
            if (begin > end) {
                this.showMessage(mode, MessageType.DANGER, "交易時間(起)不能大於交易時間(訖)");
                return false;
            }
        }

        if ("RM".equals(form.getChannelUc()) && StringUtils.isBlank(form.getStanTxt()) && StringUtils.isBlank(form.getEjNoUc())) {
            this.showMessage(mode, MessageType.DANGER, NoStanEjfnoChannelIsRm);
            return false;
        }
        return true;
    }

    /**
     * 由日期取得當天是星期幾
     *
     * @param transactDate
     * @return
     * @throws ParseException
     */
    private int getDayOfWeek(String transactDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(this.yyyyMMdd);
        Date date = sdf.parse(transactDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return CalendarUtil.getDayOfWeek(cal);
    }

    /**
     * 條件設定
     *
     * @param form
     */
    private void setData(UI_060610_FormMain form) {
        //交易日期
        if (StringUtils.isBlank(form.getTransactDate())) {
            SimpleDateFormat sdf = new SimpleDateFormat(this.yyyyMMdd);
            form.setTransactDate(sdf.format(new Date()));
        }
        //交易時間起
        String transactDate = form.getTransactDate();
        if (StringUtils.isNotBlank(form.getFeptxnTxTimeBeginTxt())) {
            form.setFeptxnTxTimeBegin(transactDate + " " + form.getFeptxnTxTimeBeginTxt());
        } else {
            form.setFeptxnTxTimeBegin(transactDate + " " + timeBegin);
            form.setFeptxnTxTimeBeginTxt(timeBegin);
        }
//	    this.setMsgFlowuc2Options(mode);//訊息流程下拉選單
//		Calendar calendar = GregorianCalendar.getInstance();
        // gets hour in 12h format
//		form.setFeptxnTxTimeBegin(calendar.get(Calendar.HOUR) + ":"+ calendar.get(Calendar.MINUTE)); ;
        //交易時間迄
        if (StringUtils.isNotBlank(form.getFeptxnTxTimeEndTxt())) {
            form.setFeptxnTxTimeEnd(transactDate + " " + form.getFeptxnTxTimeEndTxt());
        } else {
            form.setFeptxnTxTimeEnd(transactDate + " " + timeEnd);
            form.setFeptxnTxTimeEndTxt(timeEnd);
        }
        //EJ序號
        if (StringUtils.isNotBlank(form.getEjNoUc())) {
            form.setEjfnoList(new ArrayList<>(Arrays.asList(form.getEjNoUc().split(","))));
        }
    }

    /**
     * 依照zoneCode回查feptxn找資料
     *
     * @param pageInfo
     * @param argsMap
     * @param zoneCode
     * @param transactDate
     * @throws Exception
     */
    private void getPageInfo(PageInfo<Feptxn> pageInfo, Map<String, Object> argsMap, String zoneCode, String transactDate) throws Exception {
        argsMap.put("zoneCode", zoneCode);
        argsMap.put("transactDate", transactDate);
        atmService.getNextBusinessDate(argsMap);
        argsMap.put("tableNameSuffix", argsMap.get("nextDate").toString().substring(6, 8));
        atmService.getFeptxnByEjfno(argsMap);
    }

    /**
     * 可以使用如下指令
     * <p>
     * curl "http://localhost:8081/UI060610/GetAPLog?logType=aplog&logDate=2023-10-07"
     *
     * @param logType
     * @param logDate
     * @return
     */
    @RequestMapping(value = "/UI060610/GetAPLog", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> getApLog(
            @RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator,
            @RequestParam(value = "logType") String logType,
            @RequestParam(value = "logDate") String logDate,
            @RequestParam(value = "logTimeBegin", required = false, defaultValue = StringUtils.EMPTY) String logTimeBegin,
            @RequestParam(value = "logTimeEnd", required = false, defaultValue = StringUtils.EMPTY) String logTimeEnd,
            @RequestParam(value = "selectGZLog", required = false, defaultValue = StringUtils.EMPTY) String selectGZLog,
            @RequestParam(value = "appName", required = false, defaultValue = StringUtils.EMPTY) String appName
    ) {
        String fepLogPath = WebConfiguration.getInstance().getFepLogPath();
        String fepWasLogPath = WebConfiguration.getInstance().getFepWasLogPath();
        String fepLogArchivesPath = WebConfiguration.getInstance().getFepLogArchivesPath();
        String fepWasLogArchivesPath = WebConfiguration.getInstance().getFepWasLogArchivesPath();
        return GetApLogFilesUtil.getApLog(
                operator, logType, logDate, fepLogPath, fepWasLogPath, fepLogArchivesPath, fepWasLogArchivesPath, logTimeBegin, logTimeEnd,
                Integer.toString(WebConfiguration.getInstance().getGetApLogTotalSizeLimit()), WebConfiguration.getInstance().getGetApLogTotalSizeLimit(),
                // 2025-09-08 Richard modified for [Excessive Data Exposure]
                this::logMessage, this::sendEMS, selectGZLog, appName);
    }

    @PostMapping(value = "/atmmon/UI_060610/getLogNames")
    @ResponseBody
    public List<String> getLogNames(@RequestBody UI_060610_FormMain form) {
        try {
            String fepLogPath = WebConfiguration.getInstance().getFepLogPath();
            String fepWasLogPath = WebConfiguration.getInstance().getFepWasLogPath();
            String fepLogArchivesPath = WebConfiguration.getInstance().getFepLogArchivesPath();
            String fepWasLogArchivesPath = WebConfiguration.getInstance().getFepWasLogArchivesPath();
            String server = form.getServer();
            String logType = form.getLogType();
            String logDate = form.getTransactDate();
            String logTimeBegin = form.getFeptxnTxTimeBegin();
            String logTimeEnd = form.getFeptxnTxTimeEnd();
            // 如果選擇的主機名稱等於spring.fep.hostname, 代表要取得的檔案在本機上, 就直接呼叫GetAPLogFiles function就不用再透過WebAPI了
            if (server.equals(FEPConfig.getInstance().getHostName())) {
                return GetApLogFilesUtil.getArchiveFileNames(ApLogType.valueOf(logType), logDate, fepLogPath, fepWasLogPath, fepLogArchivesPath, fepWasLogArchivesPath, logTimeBegin, logTimeEnd);
            } else {
                List<WebApLogConfiguration> aplogList = WebConfiguration.getInstance().getAplog();
                WebApLogConfiguration webApLogConfiguration = aplogList.stream().filter(t -> t.getServer().equals(server)).findFirst().orElse(null);
                if (webApLogConfiguration != null) {
                    this.infoMessage("透過", webApLogConfiguration.getService(), "遠程抓取APLog檔案");
                    StringBuilder sb = new StringBuilder();
                    sb.append(webApLogConfiguration.getService().replace("GetAPLog", "GetAPLogNames"))
                            .append("?").append("operator=").append(WebUtil.getUser().getLoginId())
                            .append("&").append("logType=").append(logType)
                            .append("&").append("logDate=").append(logDate)
                            .append("&").append("fepLogPath=").append(fepLogPath)
                            .append("&").append("fepLogArchivesPath=").append(fepLogArchivesPath)
                            .append("&").append("logTimeBegin=").append(logTimeBegin)
                            .append("&").append("logTimeEnd=").append(logTimeEnd)
                            .append("&").append("sizeLimit=").append(WebConfiguration.getInstance().getGetApLogTotalSizeLimit());
                    String url = sb.toString();
                    RestTemplate restTemplate = new RestTemplate();
                    // 2025-10-17 Richard modified
                    if (HttpClient.isHttps(url)) {
                        restTemplate.setRequestFactory(HttpClientConfiguration.createTrustAnyHttpComponentsClientHttpRequestFactory(SslContextFactory.PROTOCOL, FEPConfig.getInstance().getRestfulTimeout()));
                    } else {
                        restTemplate.setRequestFactory(HttpClientConfiguration.createSimpleClientHttpRequestFactory(FEPConfig.getInstance().getRestfulTimeout()));
                    }
                    this.debugMessage("[", url, "]", Const.MESSAGE_OUT);

                    ResponseEntity<List> res = restTemplate.exchange(url, HttpMethod.GET, null, List.class);
                    return res.getBody();
                } else {
                    return new ArrayList<>();
                }
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            return new ArrayList<>();
        }
    }

    @RequestMapping(value = "/UI060610/GetAPLogNames", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<String>> getApLogNames(
            @RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator,
            @RequestParam(value = "logType") String logType,
            @RequestParam(value = "logDate") String logDate,
            @RequestParam(value = "logTimeBegin") String logTimeBegin,
            @RequestParam(value = "logTimeEnd") String logTimeEnd) {
        String fepLogPath = WebConfiguration.getInstance().getFepLogPath();
        String fepWasLogPath = WebConfiguration.getInstance().getFepWasLogPath();
        String fepLogArchivesPath = WebConfiguration.getInstance().getFepLogArchivesPath();
        String fepWasLogArchivesPath = WebConfiguration.getInstance().getFepWasLogArchivesPath();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramName(StringUtils.join(ProgramName, ".getApLog"));
        logData.setMessageFlowType(MessageFlow.Request);
        try {
            List<String> body = GetApLogFilesUtil.getArchiveFileNames(ApLogType.valueOf(logType), logDate, fepLogPath, fepWasLogPath, fepLogArchivesPath, fepWasLogArchivesPath, logTimeBegin, logTimeEnd);
            return new ResponseEntity<>(body, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    /**
     * 選擇下載的通道下拉選單
     *
     * @param mode
     * @param form
     */
    private void setChannelDownloadOptions(ModelMap mode, UI_060610_FormMain form) {
        Map<String, String> channelToAppName = getChannelToAppNameMap(form.getServer());
        List<SelectOption<String>> selectOptionList = new ArrayList<>();
        selectOptionList.add(new SelectOption<>(this.pleaseChoose, StringUtils.EMPTY));
        if (MapUtils.isNotEmpty(channelToAppName)) {
            channelToAppName.keySet().forEach(key -> selectOptionList.add(new SelectOption<>(key, key)));
        }
        WebUtil.putInAttribute(mode, AttributeName.ChannelDownloadOptions, selectOptionList);
    }

    private static Map<String, String> getChannelToAppNameMap(String server) {
        Map<String, String> channelToAppName = null;
        if (StringUtils.isNotBlank(server) && server.contains("ap")) {
            channelToAppName = WebConfiguration.getInstance().getApChannelToAppName();
        } else if (StringUtils.isNotBlank(server) && server.contains("gw")) {
            channelToAppName = WebConfiguration.getInstance().getGwChannelToAppName();
        }
        return channelToAppName;
    }
}
