package com.syscom.fep.web.controller.batch;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.batch.base.configurer.BatchBaseConfiguration;
import com.syscom.fep.batch.base.configurer.BatchBaseConfigurationHost;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.frmcommon.esapi.ESAPIValidator;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Jobs;
import com.syscom.fep.mybatis.util.DB2Util;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.batch.UI_000120_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.BatchService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UI_000110 跳轉
 *
 * @author xingyun_yang
 * @create 2022/1/25
 */
@Controller
public class UI_000120Controller extends BaseController {
    @Autowired
    private BatchService batchService;
    @Autowired
    private BatchBaseConfiguration batchBaseConfiguration;

    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_000120_Form form = WebUtil.getFromAttribute(mode, AttributeName.Form);
        if (form == null) {
            form = new UI_000120_Form();
        }
        bindGridData("", "", form, mode);
    }

    private void bindGridData(String sort, String order, UI_000120_Form form, ModelMap mode) {
        try {
            // 2022-08-12 Richard add start
            // 這裡一定要再取一次BatchCurrentId
            Batch def = new Batch();
            def.setBatchBatchid(Integer.valueOf(form.getBatchId()));
            def = batchService.getBatchQueryByPrimaryKey(def);
            if (def != null) {
                form.setBatchCurrentId(def.getBatchCurrentid());
                mode.addAttribute("BATCH_NAME", def.getBatchName());
                mode.addAttribute("BATCH_DESCRIPTION", def.getBatchDescription());
                mode.addAttribute("BATCH_CURRENTID", def.getBatchCurrentid());
                mode.addAttribute("BATCH_EXECUTE_HOST_NAME", def.getBatchExecuteHostName());
                String result = " ";
                if ("0".equals(def.getBatchResult())) {
                    result = "執行中";
                } else if ("1".equals(def.getBatchResult())) {
                    result = "成功";
                } else if ("2".equals(def.getBatchResult())) {
                    result = "失敗";
                }
                mode.addAttribute("BATCH_RESULT", result);
            }
            // 2022-08-12 Richard add end
            PageInfo<HashMap<String, Object>> dtResult = getHistory(form);
            if (dtResult.getList().size() > 0) {
                if (StringUtils.isNotBlank(sort) || StringUtils.isNotBlank(order)) {
                    // dtResult.DefaultView.Sort = "JOBS_SEQ ASC"
                } else {
                    // dtResult.DefaultView.Sort = sort & " " & order
                }
                mode.addAttribute("BATCH_NAME", dtResult.getList().get(0).get("BATCH_NAME"));
                mode.addAttribute("BATCH_DESCRIPTION", dtResult.getList().get(0).get("BATCH_DESCRIPTION"));
                mode.addAttribute("BATCH_CURRENTID", dtResult.getList().get(0).get("BATCH_CURRENTID"));
                mode.addAttribute("BATCH_EXECUTE_HOST_NAME", dtResult.getList().get(0).get("BATCH_EXECUTE_HOST_NAME"));
                String result = " ";
                if ("0".equals(dtResult.getList().get(0).get("BATCH_RESULT"))) {
                    result = "執行中";
                } else if ("1".equals(dtResult.getList().get(0).get("BATCH_RESULT"))) {
                    result = "成功";
                } else if ("2".equals(dtResult.getList().get(0).get("BATCH_RESULT"))) {
                    result = "失敗";
                }
                mode.addAttribute("BATCH_RESULT", result);
                PageData<UI_000120_Form, HashMap<String, Object>> pageData = new PageData<>(dtResult, form);
                WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
            } else {
                this.showMessage(mode, MessageType.WARNING, "無最近一次執行記錄");
            }
            mode.addAttribute("testTimeLbl", new Date());
            if (StringUtils.isBlank((String) mode.getAttribute("BATCH_EXECUTE_HOST_NAME"))) {
                List<String> apHostNameList = batchBaseConfiguration.getHost().stream().map(BatchBaseConfigurationHost::getName).collect(Collectors.toList());
                mode.addAttribute("BATCH_EXECUTE_HOST_NAME", StringUtils.join(apHostNameList, ", "));
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    private PageInfo<HashMap<String, Object>> getHistory(UI_000120_Form form) {
        // 批次序號,啟始日期,啟始日期
        PageInfo<HashMap<String, Object>> dtResult = batchService.getHistoryByInstanceId(Integer.parseInt(form.getBatchId()), form.getBatchCurrentId(), form.getPageNum(), form.getPageSize());
        List<HashMap<String, Object>> list = new ArrayList<>(dtResult.getList().size());
        String time = StringUtils.EMPTY;
        if (CollectionUtils.isNotEmpty(dtResult.getList())) {
            long lastJobId = -1;
            int tempVar = dtResult.getList().size();
            for (int i = 0; i < tempVar; i++) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("PAGEHELPER_ROW_ID", nullToEmptyStr(dtResult.getList().get(i).get("PAGEHELPER_ROW_ID")));
                hashMap.put("BATCH_RESULT", nullToEmptyStr(dtResult.getList().get(i).get("BATCH_RESULT")));
                hashMap.put("TASK_COMMAND", nullToEmptyStr(dtResult.getList().get(i).get("TASK_COMMAND")));
                hashMap.put("BATCH_BATCHID", nullToEmptyStr(dtResult.getList().get(i).get("BATCH_BATCHID")));
                hashMap.put("BATCH_EXECUTE_HOST_NAME", nullToEmptyStr(dtResult.getList().get(i).get("BATCH_EXECUTE_HOST_NAME")));
                hashMap.put("HISTORY_LOGFILE", nullToEmptyStr(dtResult.getList().get(i).get("HISTORY_LOGFILE")));
                time = nullToEmptyStr(dtResult.getList().get(i).get("HISTORY_TASKENDTIME"));
                if (!StringUtils.EMPTY.equals(time)) {
                    time = time.replaceAll("-", "/").substring(0, 19);
                }
                hashMap.put("HISTORY_TASKENDTIME", time);
                hashMap.put("JOBS_JOBID", nullToEmptyStr(dtResult.getList().get(i).get("JOBS_JOBID")));
                hashMap.put("HISTORY_STATUS", getTASKStatusName(nullToEmptyStr(dtResult.getList().get(i).get("HISTORY_STATUS"))));
                hashMap.put("BATCH_CURRENTID", nullToEmptyStr(dtResult.getList().get(i).get("BATCH_CURRENTID")));
                hashMap.put("JOBS_NAME", nullToEmptyStr(dtResult.getList().get(i).get("JOBS_NAME")));
                hashMap.put("HISTORY_DURATION", nullToEmptyStr(dtResult.getList().get(i).get("HISTORY_DURATION")));
                hashMap.put("JOBS_SEQ", nullToEmptyStr(dtResult.getList().get(i).get("JOBS_SEQ")));
                hashMap.put("BATCH_DESCRIPTION", nullToEmptyStr(dtResult.getList().get(i).get("BATCH_DESCRIPTION")));
                hashMap.put("HISTORY_RUNHOST", nullToEmptyStr(dtResult.getList().get(i).get("HISTORY_RUNHOST")));
                time = nullToEmptyStr(dtResult.getList().get(i).get("HISTORY_TASKBEGINTIME"));
                if (!StringUtils.EMPTY.equals(time)) {
                    time = time.replaceAll("-", "/").substring(0, 19);
                }
                hashMap.put("HISTORY_TASKBEGINTIME", time);
                hashMap.put("BATCH_NAME", nullToEmptyStr(dtResult.getList().get(i).get("BATCH_NAME")));
                hashMap.put("HISTORY_MESSAGE", nullToEmptyStr(dtResult.getList().get(i).get("HISTORY_MESSAGE")));
                list.add(hashMap);
                // 2022-02-21 Richard add
                HashMap<String, Object> drv = dtResult.getList().get(i);
                // 工作失敗時啟用重作工作及跳過工作功能
                if (lastJobId != ((Integer) drv.get("JOBS_JOBID")).longValue()
                        && "2".equals((String) drv.get("BATCH_RESULT")) && "4".equals((String) drv.get("HISTORY_STATUS"))) {
                    hashMap.put("btnReturn.enable", true);
                    // 如果有下一個job才可以做Skip
                    // int nextJob = ((Integer) drv.get("JOBS_SEQ")).intValue() + 1;
                    long count = dtResult.getList().stream().filter(t -> t.get("JOBS_JOBID").equals(drv.get("JOBS_JOBID"))
                            && (Integer) t.get("JOBS_SEQ") == ((Integer) drv.get("JOBS_SEQ") + 1)).count();
                    if (count > 0) {
                        hashMap.put("btnSkip.enable", true);
                    } else {
                        hashMap.put("btnSkip.enable", false);
                    }
                    hashMap.put("style", "color:red;");
                } else {
                    hashMap.put("btnSkip.enable", false);
                    hashMap.put("btnReturn.enable", false);
                }
                if (StringUtils.isBlank((String) drv.get("HISTORY_STATUS"))) {
                    hashMap.put("style", "color:gray;");
                }
                lastJobId = ((Integer) drv.get("JOBS_JOBID")).longValue();
            }
        }
        dtResult.setList(list);
        return dtResult;
    }

    @PostMapping(value = "/batch/UI_000120/btnTime")
    private String queryClick(ModelMap mode, UI_000120_Form form) {
        this.infoMessage("查詢資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        bindGridData("", "", form, mode);
        return Router.UI_000120.getView();
    }

    @PostMapping(value = "/batch/UI_000120/updateTime")
    private String updateIntervalTmr_Tick(UI_000120_Form form, ModelMap mode) {
        form.setAutoRefresh(true);
        this.infoMessage("自動刷新查詢資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        bindGridData("", "", form, mode);
        return Router.UI_000120.getView();
    }

    @PostMapping(value = "/batch/UI_000120/doViewLogContent")
    @ResponseBody
    public BaseResp<?> doViewLogContent(@RequestBody Map<String, String> data) {
        this.infoMessage("檢視記錄儅, 條件 = [", data.toString(), "]");
        BaseResp<String> response = new BaseResp<>();
        String historyLogfile = data.get("historyLogfile");
        if (StringUtils.isNotBlank(historyLogfile)) {
            try {
                // 驗證Log路徑
                String sanitizedPath = ESAPIValidator.getValidFilePath(historyLogfile);
                Map<String, Object> result = batchService.getLogByLogFile(sanitizedPath);
                logContext.setMessage(StringUtils.join("Begin clob:result=", result));
                logMessage(Level.INFO, getLogContext());
                String historyLogfilecontent = null;
                if (MapUtils.isNotEmpty(result)) {
                    historyLogfilecontent = DB2Util.getClobValue(result.get("HISTORY_LOGFILECONTENT"), StringUtils.EMPTY);
                }
                logContext.setMessage("After clob:historyLogfilecontent=" + historyLogfilecontent);
                logMessage(Level.INFO, getLogContext());
                if (StringUtils.isBlank(historyLogfilecontent)) {
                    logContext.setMessage("historyLogfilecontent is null");
                    logMessage(Level.INFO, getLogContext());
                    Path logAbsolutePath = Paths.get(sanitizedPath).toAbsolutePath().normalize();
                    // 2024-04-24 Richard modified start for 【Absolute Path Traversal】
                    // File file = new File(CleanPathUtil.cleanString(logAbsolutePath.toString()));
                    File file = logAbsolutePath.toFile();
                    // 2024-04-24 Richard modified end for 【Absolute Path Traversal】
                    if (file.exists()) {
                        FileInputStream fis = null;
                        try {
                            fis = FileUtils.openInputStream(file);
                            List<String> lineList = IOUtils.readLines(fis, StandardCharsets.UTF_8);
                            historyLogfilecontent = StringUtils.join(lineList, "<br/>");
                        } catch (Exception e) {
                            this.errorMessage(e, e.getMessage());
                            response.setMessage(MessageType.DANGER, "讀取批次執行記錄檔發生例外");
                            return response;
                        } finally {
                            IOUtils.closeQuietly(fis);
                        }
                    }
                }
                if (StringUtils.isNotBlank(historyLogfilecontent)) {
                    response.setData(historyLogfilecontent);
                } else {
                    response.setMessage(MessageType.DANGER, StringUtils.join("批次執行記錄檔", historyLogfile, "不存在"));
                }
            } catch (Exception e) {
                this.errorMessage(e, e.getMessage());
                response.setMessage(MessageType.DANGER, "讀取批次執行記錄檔發生例外");
            }
        } else {
            response.setMessage(MessageType.DANGER, "批次執行記錄檔不存在");
        }
        return response;
    }

    @PostMapping(value = "/batch/UI_000120/doReturn")
    @ResponseBody
    public BaseResp<?> doReturn(@RequestBody Map<String, String> data) {
        this.infoMessage("開始重新執行, 條件 = [", data.toString(), "]");
        BaseResp<String> response = new BaseResp<>();
        try {
            String batchCurrentId = data.get("batchCurrentId");
            String batchId = data.get("batchId");
            String jobId = data.get("jobId");
            String hostName = data.get("hostName");
            BatchJobLibrary batchLib = new BatchJobLibrary();
            batchLib.rerunBatch(
                    hostName,
                    batchCurrentId,
                    batchId,
                    jobId);
            response.setMessage(StringUtils.join("批次已重新執行工作(編號:", jobId, ")"));
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.setMessage(MessageType.DANGER, "批次重新執行工作發生例外");
        }
        return response;
    }

    @PostMapping(value = "/batch/UI_000120/doSkip")
    @ResponseBody
    public BaseResp<?> doSkip(@RequestBody Map<String, String> data) {
        this.infoMessage("開始跳過執行, 條件 = [", data.toString(), "]");
        BaseResp<String> response = new BaseResp<>();
        try {
            String batchCurrentId = data.get("batchCurrentId");
            String batchId = data.get("batchId");
            String jobId = data.get("jobId");
            String hostName = data.get("hostName");
            BatchJobLibrary batchLib = new BatchJobLibrary();
            List<Jobs> list = batchService.getJobsByBatchId(Integer.parseInt(batchId));
            String nextJobId = null;
            // 找出下一個JOB
            for (int i = 0; i < list.size() - 1; i++) {
                if (jobId.equals(list.get(i).getJobsJobid().toString())) {
                    nextJobId = list.get(i + 1).getJobsJobid().toString();
                    break;
                }
            }
            if (StringUtils.isNotBlank(nextJobId)) {
                batchLib.rerunBatch(
                        hostName,
                        batchCurrentId,
                        batchId,
                        nextJobId);
                response.setMessage(StringUtils.join("批次已跳過執行下一工作(編號:", nextJobId, ")"));
            } else {
                response.setMessage(MessageType.DANGER, StringUtils.join("批次無法跳過執行下一工作, 無法找到下一工作"));
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.setMessage(MessageType.DANGER, "批次跳過執行下一工作發生例外");
        }
        return response;
    }
}
