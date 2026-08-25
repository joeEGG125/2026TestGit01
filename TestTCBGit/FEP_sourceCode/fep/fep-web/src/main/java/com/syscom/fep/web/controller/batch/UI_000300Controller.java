package com.syscom.fep.web.controller.batch;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Subsys;
import com.syscom.fep.mybatis.util.DB2Util;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.form.batch.UI_000300_Form;
import com.syscom.fep.web.service.BatchService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
public class UI_000300Controller extends BaseController {

    @Autowired
    private BatchService batchService;

    private final String pleaseChoose = "所有";        //批次名稱、系統別： 下拉選單的預設值
    private final String yyyyMMdd = "yyyy-MM-dd";    //批次啟動日期格式

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_000300_Form form = new UI_000300_Form();
        form.setUrl("/batch/UI_000300/queryClick");
        DateFormat dateTimeformat = new SimpleDateFormat(this.yyyyMMdd);
        form.setBatchStartDate(dateTimeformat.format(new Date()));
        this.setBatchNameOptions(mode);
        this.setSubsysOptions(mode);
        this.queryClick(form, mode);
    }

    @PostMapping(value = "/batch/UI_000300/queryClick")
    private String queryClick(@ModelAttribute UI_000300_Form form, ModelMap mode) {
        bindGridData(form, mode);
        return Router.UI_000300.getView();
    }

    /**
     * 資料整理 依查詢條件查詢主程式
     */
    private void bindGridData(UI_000300_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        FileInputStream fis = null;
        // 記錄auditLog
        AuditLog auditLog = new AuditLog();
        // 塞入auditLog.action
        auditLog.setAction("查詢");
        // 塞入auditLog.params
        if (StringUtils.isNotBlank(String.valueOf(form.getBatchName())))
            auditLog.addParam("批次名稱", String.valueOf(form.getBatchName()));
        if (StringUtils.isNotBlank(String.valueOf(form.getBatchStartDate())))
            auditLog.addParam("批次啟動日期", String.valueOf(form.getBatchStartDate()));
        if (StringUtils.isNotBlank(String.valueOf(form.getBatchShortName())))
            auditLog.addParam("批次簡稱", String.valueOf(form.getBatchShortName()));
        if (StringUtils.isNotBlank(String.valueOf(form.getSubsys())))
            auditLog.addParam("系統別", String.valueOf(form.getSubsys()));
        // 最後塞入ThreadLocal變量中
        setAuditLog(auditLog);
        try {
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper
                    .startPage(form.getPageNum(), form.getPageSize(), form.getPageNum() > 0 && form.getPageSize() > 0)
                    .doSelectPageInfo(new ISelect() {
                        @Override
                        public void doSelect() {
                            batchService.getHistoryQuery(form.getBatchName(), form.getBatchStartDate(),
                                    form.getBatchShortName(), form.getSubsys());
                        }
                    });
            if (pageInfo.getSize() == 0) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            PageData<UI_000300_Form, HashMap<String, Object>> pageData = new PageData<>(pageInfo, form);

            List<HashMap<String, Object>> dtlist = pageData.getList();
            List<HashMap<String, Object>> datalist = new ArrayList<>(pageData.getList().size());
            int tempVar = dtlist.size();
            for (int i = 0; i < tempVar; i++) {
                HashMap<String, Object> hashMap = new HashMap<>();
                //批次名稱(Map取值須先檢查是否為null)
                if (dtlist.get(i).get("BATCH_NAME") != null) {
                    hashMap.put("BATCH_NAME", dtlist.get(i).get("BATCH_NAME").toString());
                } else {
                    hashMap.put("BATCH_NAME", "");
                }
                //順序
                if (dtlist.get(i).get("JOBS_SEQ") != null) {
                    hashMap.put("JOBS_SEQ", dtlist.get(i).get("JOBS_SEQ").toString());
                } else {
                    hashMap.put("JOBS_SEQ", "");
                }
                //執行程式
                if (dtlist.get(i).get("TASK_COMMAND") != null) {
                    String taskCommand = dtlist.get(i).get("TASK_COMMAND").toString();
                    int s = taskCommand.lastIndexOf("\\");
                    if (s > 0) {
                        hashMap.put("TASK_COMMAND", taskCommand.substring(s + 1));
                    } else {
                        hashMap.put("TASK_COMMAND", taskCommand);
                    }
                } else {
                    hashMap.put("TASK_COMMAND", "");
                }
                //訊息
                if (dtlist.get(i).get("HISTORY_MESSAGE") != null) {
                    hashMap.put("HISTORY_MESSAGE", dtlist.get(i).get("HISTORY_MESSAGE").toString());
                } else {
                    hashMap.put("HISTORY_MESSAGE", "");
                }
                //執行狀態
                if (dtlist.get(i).get("HISTORY_STATUS") != null) {
                    String status = dtlist.get(i).get("HISTORY_STATUS").toString();
                    switch (status) {
                        case "1":
                            hashMap.put("HISTORY_STATUS", "工作開始");
                            break;
                        case "2":
                            hashMap.put("HISTORY_STATUS", "執行中");
                            break;
                        case "3":
                            hashMap.put("HISTORY_STATUS", "工作結束");
                            break;
                        case "4":
                            hashMap.put("HISTORY_STATUS", "工作失敗");
                            break;
                        default:
                            hashMap.put("HISTORY_STATUS", "");
                            break;
                    }
                } else {
                    hashMap.put("HISTORY_STATUS", "");
                }
                //批次啟動時間
                if (dtlist.get(i).get("HISTORY_STARTTIME") != null) {
                    String starttime = dtlist.get(i).get("HISTORY_STARTTIME").toString();
                    hashMap.put("HISTORY_STARTTIME", starttime.substring(11, 19));
                } else {
                    hashMap.put("HISTORY_STARTTIME", "");
                }
                //程式啟動時間
                if (dtlist.get(i).get("HISTORY_TASKBEGINTIME") != null) {
                    String taskbegintime = dtlist.get(i).get("HISTORY_TASKBEGINTIME").toString();
                    hashMap.put("HISTORY_TASKBEGINTIME", taskbegintime.substring(11));
                } else {
                    hashMap.put("HISTORY_TASKBEGINTIME", "");
                }
                //程式結束時間
                if (dtlist.get(i).get("HISTORY_TASKENDTIME") != null) {
                    String taskendtime = dtlist.get(i).get("HISTORY_TASKENDTIME").toString();
                    hashMap.put("HISTORY_TASKENDTIME", taskendtime.substring(11));
                } else {
                    hashMap.put("HISTORY_TASKENDTIME", "");
                }
                //執行秒數
                if (dtlist.get(i).get("HISTORY_DURATION") != null) {
                    hashMap.put("HISTORY_DURATION", dtlist.get(i).get("HISTORY_DURATION").toString());
                } else {
                    hashMap.put("HISTORY_DURATION", "");
                }
                //LOG檔案內容(處理邏輯：db的欄位HISTORY_LOGFILECONTENT如有值，則放此內容，若無則放HISTORY_LOGFILE欄位內容中所指定的檔案內容，再無則為空。
                String historyLogfilecontent = "";
                if (dtlist.get(i).get("HISTORY_LOGFILECONTENT") != null) {
                    historyLogfilecontent = DB2Util.getClobValue(dtlist.get(i).get("HISTORY_LOGFILECONTENT"), StringUtils.EMPTY);
                } else {
                    if (dtlist.get(i).get("HISTORY_LOGFILE") != null) {
                        String historyLogfile = dtlist.get(i).get("HISTORY_LOGFILE").toString();
                        File file = new File(CleanPathUtil.cleanString(historyLogfile));
                        if (file.exists()) {
                            fis = FileUtils.openInputStream(file);
                            List<String> lineList = IOUtils.readLines(fis, StandardCharsets.UTF_8);
                            historyLogfilecontent = StringUtils.join(lineList, "<br/>");
                        }
                    }
                }
                hashMap.put("HISTORY_LOGFILECONTENT", historyLogfilecontent);
                //子系統 BATCH_SUBSYS(因查詢條件需要)
                if (dtlist.get(i).get("BATCH_SUBSYS") != null) {
                    hashMap.put("BATCH_SUBSYS", dtlist.get(i).get("BATCH_SUBSYS").toString());
                } else {
                    hashMap.put("BATCH_SUBSYS", "");
                }
                if (dtlist.get(i).get("HISTORY_RUNHOST") != null) {
                    hashMap.put("HISTORY_RUNHOST", dtlist.get(i).get("HISTORY_RUNHOST").toString());
                } else {
                    hashMap.put("HISTORY_RUNHOST", "");
                }
                datalist.add(hashMap);
            }
            if (datalist == null || datalist.size() == 0) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            } else {
                pageData.setList(datalist);
            }
            this.setBatchNameOptions(mode);
            this.setSubsysOptions(mode);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    /**
     * 設定[批次名稱]下拉選單內容
     *
     * @param mode
     */
    private void setBatchNameOptions(ModelMap mode) {
        try {
            List<Batch> batchList = batchService.getBatchAll();
            List<SelectOption<String>> selectOptionList = new ArrayList<SelectOption<String>>();
            selectOptionList.add(new SelectOption<String>(this.pleaseChoose, StringUtils.EMPTY));
            for (int i = 0; i < batchList.size(); i++) {
                selectOptionList.add(
                        new SelectOption<String>(batchList.get(i).getBatchName(), batchList.get(i).getBatchName()));
            }
            WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    /**
     * 設定[系統別]下拉選單內容
     *
     * @param mode
     */
    private void setSubsysOptions(ModelMap mode) {
        try {
            List<Subsys> subsysList = batchService.getSubsysAll();
            List<SelectOption<String>> selectOptionList = new ArrayList<SelectOption<String>>();
            selectOptionList.add(new SelectOption<String>(this.pleaseChoose, StringUtils.EMPTY));
            for (int i = 0; i < subsysList.size(); i++) {
                selectOptionList.add(new SelectOption<String>(subsysList.get(i).getSubsysNameS() + "-" + subsysList.get(i).getSubsysName(),
                        subsysList.get(i).getSubsysSubsysno().toString()));
            }
            WebUtil.putInAttribute(mode, AttributeName.Options2, selectOptionList);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }
}
