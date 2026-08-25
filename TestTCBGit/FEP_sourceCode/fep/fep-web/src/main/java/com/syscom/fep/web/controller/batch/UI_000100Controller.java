package com.syscom.fep.web.controller.batch;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.batch.base.configurer.BatchBaseConfiguration;
import com.syscom.fep.batch.base.configurer.BatchBaseConfigurationHost;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Jobs;
import com.syscom.fep.mybatis.model.Task;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.mybatis.vo.JobsContinueOnFail;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.entity.batch.BatchNotify;
import com.syscom.fep.web.entity.batch.BatchDailyRepetitionType;
import com.syscom.fep.web.entity.batch.MaintainBatch;
import com.syscom.fep.web.entity.batch.MaintainTask;
import com.syscom.fep.web.form.batch.UI_000100_Detail_Form;
import com.syscom.fep.web.form.batch.UI_000100_Form;
import com.syscom.fep.web.form.batch.UI_000100_Main_Form;
import com.syscom.fep.web.form.batch.UI_000100_Task_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.BatchService;
import com.syscom.fep.web.service.CommonService;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.mybatis.vo.SyscomroleAndCulture;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static javax.swing.SortOrder.ASCENDING;

/**
 * 批次管理
 *
 * @author Richard
 */
@Controller
public class UI_000100Controller extends BaseController {
    private static final String URL_DO_QUERY = "/batch/UI_000100/queryClick";
    private static final Integer EMPTY_BATCH_ID = -1;
    private static final String DEFAULT_SORT_COLUMN = "BATCH_NAME";
    private static final int MAX_LOOPS = Integer.MAX_VALUE; // 2024-04-24 Richard add add for 【Unchecked Input for Loop Condition】
    @Autowired
    private BatchService obj;
    @Autowired
    private BatchBaseConfiguration batchBaseConfiguration;
    @Autowired
    private CommonService commonService;
    @Autowired
    private AtmService atmService;
    @Autowired
    private BatchService batchService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        MaintainBatch maintainBatch = new MaintainBatch();
        WebUtil.putInSession(SessionKey.TemporaryRestoreData, maintainBatch);
        UI_000100_Form form = new UI_000100_Form();
        form.setUrl(URL_DO_QUERY);
        // 一載入就Query
        // Modify By Matt 2010/06/08
        this.queryClick(form, mode);
    }

    private void bindGrid(UI_000100_Form form, ModelMap mode, String sortExpression, String direction) {
        try {
            PageInfo<HashMap<String, Object>> pageInfo = getResultData(form, mode);
            if (Objects.isNull(pageInfo) || Objects.isNull(pageInfo.getList()) || pageInfo.getList().isEmpty()) {
                this.showMessage(mode, MessageType.WARNING, QueryNoData);
            }
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageInfo);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    private PageInfo<HashMap<String, Object>> getResultData(UI_000100_Form form, ModelMap mode) throws Exception {
        if (StringUtils.isNotBlank(form.getBatchName())) {
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("查詢");
            // 塞入auditLog.params
            auditLog.addParam("批次簡稱", form.getBatchName());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
        }
        return obj.getAllBatch(form.getBatchName(), WebConfiguration.getInstance().getSubsysList(), form.getPageNum(), form.getPageSize());
    }

    @PostMapping(value = URL_DO_QUERY, produces = "application/json;charset=utf-8")
    public String queryClick(@ModelAttribute UI_000100_Form form, ModelMap mode) {
        this.infoMessage("查詢資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        MaintainBatch maintainBatch = this.getSessionData();
        maintainBatch.setBatchName(form.getBatchName());
        maintainBatch.getTasks().clear();
        this.bindGrid(form, mode, DEFAULT_SORT_COLUMN, ASCENDING.name());
        return Router.UI_000100.getView();
    }

    @PostMapping(value = "/batch/UI_000100/queryDetails", produces = "application/json;charset=utf-8")
    public String queryDetails(@ModelAttribute UI_000100_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            MaintainBatch maintainBatch = this.getSessionData();
            if (StringUtils.isBlank(form.getBatchId())) {
                // 2024-04-12 Richard modified 按下新增後也可以設定排程
                // maintainBatch.setDetail("insert");
                maintainBatch.setDetail("update");
                maintainBatch.setBatch(new Batch());
                maintainBatch.getBatch().setBatchBatchid(EMPTY_BATCH_ID);
                maintainBatch.getBatch().setBatchEnable((short) 1);
                maintainBatch.getBatch().setBatchSubsys((short) 0);
                maintainBatch.getBatch().setBatchCheckbusinessdate((short) 0);
                // 初始化通知方式
                this.resetNotify(maintainBatch.getBatch());
                // 初始化是否排程
                this.resetScheduleData(maintainBatch.getBatch(), true);
            } else {
                maintainBatch.setDetail("update");
                maintainBatch.setBatch(obj.getBatchByID(Integer.parseInt(form.getBatchId())));
            }
            List<SelectOption<String>> options = new ArrayList<>();
            List<String> apHostNameList = batchBaseConfiguration.getHost().stream().map(BatchBaseConfigurationHost::getName).collect(Collectors.toList());
            options.add(new SelectOption<>(" ", StringUtils.EMPTY));
            for (String apHostName : apHostNameList) {
                options.add(new SelectOption<>(apHostName, apHostName));
            }
            mode.addAttribute("options", options);
            List<SelectOption<String>> zones = new ArrayList<>();
            List<String> zonesList = this.commonService.getAreaCode().stream().map(Zone::getZoneCode).collect(Collectors.toList());
            zones.add(new SelectOption<>(" ", StringUtils.EMPTY));
            for (String zoneCode : zonesList) {
                zones.add(new SelectOption<>(getZoneName(zoneCode), zoneCode));
            }
            mode.addAttribute("zones", zones);
            mode.addAttribute("resetBatchResult", !Objects.equals(maintainBatch.getBatch().getBatchBatchid(), EMPTY_BATCH_ID));
            mode.addAttribute("btnQuartzEnable", !Objects.equals(maintainBatch.getBatch().getBatchBatchid(), EMPTY_BATCH_ID)
                    && DbHelper.toBoolean(maintainBatch.getBatch().getBatchSchedule(), false));
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_000100_Detail.getView();
    }

    @PostMapping(value = "/batch/UI_000100/delClick", produces = "application/json;charset=utf-8")
    public String delClick(String[] delChecks, ModelMap mode) {
        this.infoMessage("刪除資料, 條件 = [", StringUtils.join(delChecks, ','), "]");
        UI_000100_Form form = new UI_000100_Form();
        form.setBatchName(this.getSessionData().getBatchName());
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        try {
            int iFaultCount = 0;
            List<String> sbFaultPK = new ArrayList<>();
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.params
            auditLog.addParam("批次序號", StringUtils.join(delChecks, ","));
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            if (ArrayUtils.isNotEmpty(delChecks)) {
                // 塞入auditLog.action
                auditLog.setAction("刪除");
                for (String gr : delChecks) {
                    if (obj.deleteBatch(Integer.parseInt(gr)) == 0) {
                        iFaultCount = iFaultCount + 1;
                        sbFaultPK.add(gr);
                    }
                }
            } else {
                this.bindGrid(form, mode, DEFAULT_SORT_COLUMN, ASCENDING.name());
                this.showMessage(mode, MessageType.WARNING, "請選擇批次");
                return Router.UI_000100.getView();
            }
            // 更新畫面
            this.bindGrid(form, mode, DEFAULT_SORT_COLUMN, ASCENDING.name());
            // 回應訊息
            if (iFaultCount == 0) {
                this.showMessage(mode, MessageType.INFO, DeleteSuccess);
            } else {
                this.showMessage(mode, MessageType.DANGER, sbFaultPK.size() == delChecks.length ? DeleteFail : "部分刪除失敗, " + StringUtils.join(sbFaultPK, ","));
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_000100.getView();
    }

    @PostMapping(value = "/batch/UI_000100/details")
    @ResponseBody
    public UI_000100_Detail_Form details() {
        this.infoMessage("查看明細資料");
        MaintainBatch maintainBatch = this.getSessionData();
        maintainBatch.getTasks().clear(); // 在新增模式下按下放棄按鈕需要清空
        UI_000100_Detail_Form form = new UI_000100_Detail_Form();
        try {
            form.setDetail(maintainBatch.getDetail());
            form.setSubsys(obj.getSubsysAll());
            // 設置可啓動群組
            form.setStartGroup(this.bindGroupListBox());
            if ("update".equals(maintainBatch.getDetail())) {
                List<HashMap<String, String>> mList = new ArrayList<>();
                for (int i = 1; i < 13; i++) {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("value", ((int) Math.pow(2, i - 1)) + StringUtils.EMPTY);
                    hashMap.put("name", i + "月");
                    mList.add(hashMap);
                }
                List<HashMap<String, String>> mdList = new ArrayList<>();
                for (int i = 1; i < 32; i++) {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("value", i + StringUtils.EMPTY);
                    hashMap.put("name", i + "日");
                    mdList.add(hashMap);
                }
                if (maintainBatch.getBatch().getBatchScheduleStarttime() != null) {
                    SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat time = new SimpleDateFormat("HH:mm");
                    form.setDate(date.format(maintainBatch.getBatch().getBatchScheduleStarttime()));
                    form.setTime(time.format(maintainBatch.getBatch().getBatchScheduleStarttime()));
                }
                form.setTaskList(obj.getTaskAll());
                form.setTasks(bindJobGrid());
                if (StringUtils.isNotBlank(maintainBatch.getBatch().getBatchScheduleType()) && maintainBatch.getBatch().getBatchScheduleType().equals("O")) {
                    form.setRadioType("mw");
                } else {
                    form.setRadioType("m");
                }
                form.setMdList(mdList);
                form.setmList(mList);
                form.setBatch(maintainBatch.getBatch());
                // 從batch中取出notify mail phone相關信息
                BatchNotify batchNotify = new BatchNotify();
                batchNotify.getNotifyFromBatch(form.getBatch(), false);
                form.setNotify(batchNotify);
            }
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            form.setMessage(MessageType.DANGER, QueryFail);
        }
        return form;
    }

    private List<HashMap<String, String>> bindGroupListBox() throws Exception {
        List<SyscomroleAndCulture> roleList = commonService.getAllRoles();
        List<HashMap<String, String>> list = new ArrayList<>();
        for (SyscomroleAndCulture role : roleList) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("name", role.getRoleno() + "-" + role.getRolename());
            hashMap.put("value", Integer.toString(role.getRoleid()));
            list.add(hashMap);
        }
        return list;
    }

    @PostMapping(value = "/batch/UI_000100/saveClick")
    @ResponseBody
    public BaseResp<?> saveClick(@RequestBody UI_000100_Main_Form form) {
        this.infoMessage("變更存儲, 條件 = [", form.toString(), "]");
        BaseResp<?> response = new BaseResp<>();
        Batch batch = form.getBatch();
        batch.setBatchNotifymail(StringUtils.EMPTY); // 預設塞入空串, 后面form.getNotify().setNotifyToBatch时会塞入
        batch.setBatchNotifyphone(StringUtils.EMPTY); // 預設塞入空串, 后面form.getNotify().setNotifyToBatch时会塞入
        // 保存時, 將mail和phone存入batch對應的欄位中
        if (form.getNotify() != null) {
            form.getNotify().setNotifyToBatch(batch);
        }
        int iRes = 0;
        String userId = WebUtil.getUser().getUserId();
        MaintainBatch maintainBatch = this.getSessionData();
        boolean succeed = false;
        try {
            if (checkAllBatchField(batch, response, form)) {
                // 如果通知方式改為不通知, 則清除之前通知方式的設定
//                if (batch.getBatchNotifytype() == null || batch.getBatchNotifytype() == (short) 0) {
//                    this.resetNotify(batch);
//                }
                // 如果是否排程沒有勾選, 則清除排程相關所有的設定
                if (batch.getBatchSchedule() == null || batch.getBatchSchedule() == (short) 0) {
                    this.resetScheduleData(batch, true);
                }
                // 記錄auditLog
                AuditLog auditLog = new AuditLog();
                if (StringUtils.isNotBlank(batch.getBatchName()))
                    auditLog.addParam("批次名稱", batch.getBatchName());
                if (batch.getBatchEnable() != null)
                    auditLog.addParam("是否啓用", batch.getBatchEnable());
                if (StringUtils.isNotBlank(batch.getBatchDescription()))
                    auditLog.addParam("批次説明", batch.getBatchDescription());
                if (StringUtils.isNotBlank(batch.getBatchExecuteHostName()))
                    auditLog.addParam("執行主機", batch.getBatchExecuteHostName());
                if (StringUtils.isNotBlank(batch.getBatchZone()))
                    auditLog.addParam("地區別", batch.getBatchZone());
                if (batch.getBatchSubsys() != null)
                    auditLog.addParam("子系統別", batch.getBatchSubsys());
                if (batch.getBatchSchedule() != null)
                    auditLog.addParam("是否排程", batch.getBatchSchedule());
                if (batch.getBatchSingletime() != null)
                    auditLog.addParam("每天只能做一次", batch.getBatchSingletime());
                if (batch.getBatchCheckbusinessdate() != null)
                    auditLog.addParam("檢查營業日", batch.getBatchCheckbusinessdate());
                if (StringUtils.isNotBlank(form.getGroupChk()))
                    auditLog.addParam("可啓動群組", form.getGroupChk());
                if (batch.getBatchNotifytype() != null) {
                    String batchNotifytype = "";
                    switch (batch.getBatchNotifytype()) {
                        case (short) 0:
                            batchNotifytype = "不通知";
                            break;
                        case (short) 1:
                            batchNotifytype = "成功時通知";
                            break;
                        case (short) 2:
                            batchNotifytype = "失敗時通知";
                            break;
                        default:
                            batchNotifytype = "成功失敗都通知";
                            break;
                    }
                    auditLog.addParam("通知方式", batchNotifytype);
                }
                if (StringUtils.isNotBlank(batch.getBatchNotifymail()))
                    auditLog.addParam("Mail通知", batch.getBatchNotifymail());
                if (StringUtils.isNotBlank(batch.getBatchNotifyphone()))
                    auditLog.addParam("簡訊通知", batch.getBatchNotifyphone());
                if (StringUtils.isNotBlank(batch.getBatchAction()))
                    auditLog.addParam("處理方式", batch.getBatchAction());
                if (StringUtils.isNotBlank(batch.getBatchScheduleType())) {
                    String scheduleType = "";
                    switch (batch.getBatchScheduleType()) {
                        case "D":
                            scheduleType = "每日";
                            break;
                        case "W":
                            scheduleType = "每周";
                            break;
                        case "M":
                            scheduleType = "每月";
                            break;
                    }
                    auditLog.addParam("批次排程設定", scheduleType);
                }
                if (batch.getBatchDenyconcurrentexec() != null)
                    auditLog.addParam("不允許重覆執行", batch.getBatchDenyconcurrentexec());
                if (StringUtils.isNotBlank(batch.getBatchDailyRepetitionType()))
                    auditLog.addParam("每日重複執行類別", batch.getBatchDailyRepetitionType());
                // 最後塞入ThreadLocal變量中
                setAuditLog(auditLog);
                if ("insert".equals(maintainBatch.getDetail()) || Objects.equals(maintainBatch.getBatch().getBatchBatchid(), EMPTY_BATCH_ID)) {
                    // 塞入auditLog.action
                    auditLog.setAction("新增");
                    batch.setBatchBatchid(null); // 新增模式下, 這裡記得塞入null, 由DB創建PK值
                    iRes = obj.insertBatchJobTask(batch, maintainBatch.getTasks());
                    if (iRes > 0) {
                        if (batch.getBatchSchedule() == 1) {
                            obj.createScheduleTask(batch.getBatchBatchid());
                        }
                        maintainBatch.setBatch(batch);
                        response.setMessage(MessageType.INFO, InsertSuccess);
                    } else {
                        response.setMessage(MessageType.DANGER, InsertFail);
                    }
                } else {
                    // 塞入auditLog.action
                    auditLog.setAction("變更儲存(修改)");
                    batch.setBatchResult(null); // 2025-08-21 Richard add 這個欄位不要更新
                    batch.setBatchLastruntime(null); // 2025-08-21 Richard add 這個欄位不要更新
                    batch.setBatchCurrentid(null); // 2025-08-21 Richard add 這個欄位不要更新
                    batch.setBatchNextruntime(null); // 2025-08-21 Richard add 這個欄位不要更新
                    iRes = obj.updateBatch(batch, userId, true); // 2025-12-18 Richard modified 更新完batch檔之後, 要將BatchNextruntime清掉, 故最後一個參數為true
                    // 更新DB成功後通知批次服務建立此Batch的Task
                    if (batch.getBatchSchedule() == 1) {
                        // 如果hostName有異動, 則要先通知原本的主機刪除排程中的Task
                        if (StringUtils.isBlank(maintainBatch.getBatch().getBatchExecuteHostName()) && StringUtils.isNotBlank(batch.getBatchExecuteHostName())
                                || StringUtils.isNotBlank(maintainBatch.getBatch().getBatchExecuteHostName()) && !maintainBatch.getBatch().getBatchExecuteHostName().equals(batch.getBatchExecuteHostName())) {
                            obj.deleteScheduleTask(maintainBatch.getBatch().getBatchExecuteHostName(), batch.getBatchBatchid(), batch.getBatchName(), false);
                        }
                        obj.createScheduleTask(batch.getBatchBatchid());
                    } else {
                        obj.deleteScheduleTask(maintainBatch.getBatch().getBatchExecuteHostName(), batch.getBatchBatchid(), batch.getBatchName(), false);
                    }
                    if (iRes > 0) {
                        maintainBatch.setBatch(batch);
                        response.setMessage(MessageType.INFO, UpdateSuccess);
                    } else {
                        response.setMessage(MessageType.DANGER, UpdateFail);
                    }
                }
                succeed = true;
            }
            return response;
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.setMessage(MessageType.DANGER, programError);
            return response;
        }
//        finally {
//            // 如果UI上勾選不通知, 則要清除重置表單中的通知相關的欄位
//            if (succeed && (batch.getBatchNotifytype() == null || batch.getBatchNotifytype() == (short) 0) && form.getNotify() != null) {
//                form.getNotify().clearNotify();
//            }
//        }
    }

    private boolean checkAllBatchField(Batch batch, BaseResp<?> response, UI_000100_Main_Form form) {
        MaintainBatch maintainBatch = this.getSessionData();
        try {
            if (!"update".equals(maintainBatch.getDetail())) {
                batch.setBatchBatchid(null);
            }
            if (batch.getBatchSubsys() == 0) {
                response.setMessage(MessageType.DANGER, "未輸入系統別");
                return false;
            }
            if (batch.getBatchCheckbusinessdate() == 1 && StringUtils.isBlank(batch.getBatchZone())) {
                response.setMessage(MessageType.DANGER, "檢核營業日必須輸入地區別");
                return false;
            }
            // 2025-12-19 Richard add 避免排程相關的設定沒有設定造成後面出現NullPointerException, 故這裡針對沒有設定的排程塞入預設值
            this.resetScheduleData(batch, false);
            batch.setBatchEditgroup(StringUtils.EMPTY);
            batch.setBatchStartgroup(form.getGroupChk());
            if ("update".equals(maintainBatch.getDetail())) {
                if (form.getJobId() == 0) {
                    batch.setBatchStartjobid(0);
                } else {
                    batch.setBatchStartjobid(form.getJobId());
                }
            } else {
                batch.setBatchStartjobid(0);
            }
            if ("update".equals(maintainBatch.getDetail()) && batch.getBatchSchedule() == 1) {
                if ("M".equals(batch.getBatchScheduleType()) && "mw".equals(form.getRadioType())) {
                    // 選每月的第幾週
                    batch.setBatchScheduleType("O");
                }
                if (StringUtils.isBlank(form.getDateTime())) {
                    response.setMessage(MessageType.DANGER, "未設置批次開始日期時間");
                    return false;
                }
                try {
                    batch.setBatchScheduleStarttime(FormatUtil.parseDataTime(form.getDateTime(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_HH_MM));
                } catch (ParseException e) {
                    this.errorMessage(e, e.getMessage());
                    response.setMessage(MessageType.DANGER, "批次開始日期時間格式不正確");
                    return false;
                }
                switch (batch.getBatchScheduleType()) {
                    case "D": {
                        if (batch.getBatchScheduleRepetitioninterval() >= 1440) {
                            if (BatchDailyRepetitionType.TIME.getValue().equals(batch.getBatchDailyRepetitionType())) {
                                response.setMessage(MessageType.DANGER, "重覆時間必須小於1440分!");
                                return false;
                            } else {
                                batch.setBatchScheduleRepetitioninterval((short) 0);
                            }
                        }
                        if (batch.getBatchScheduleRepetitioninduration() >= 24) {
                            if (BatchDailyRepetitionType.TIME.getValue().equals(batch.getBatchDailyRepetitionType())) {
                                response.setMessage(MessageType.DANGER, "持續時間必須小於24小時!");
                                return false;
                            } else {
                                batch.setBatchScheduleRepetitioninduration((short) 0);
                            }
                        }
                        break;
                    }
                    case "W": {
                        if (form.getMwChk().length() == 1 && "0".equals(form.getMwChk())) {
                            form.setMwChk(StringUtils.EMPTY);
                        } else if (form.getMwChk().length() > 2 && "0,".equals(form.getMwChk().substring(0, 2))) {
                            form.setMwChk(form.getMwChk().substring(2));
                        }
                        if (StringUtils.isBlank(form.getMwChk())) {
                            response.setMessage(MessageType.DANGER, "尚未選取任何星期!");
                            return false;
                        }
                        batch.setBatchScheduleWeekdays(form.getMwChk());
                        break;
                    }
                    case "M": {
                        if (form.getmChk().length() == 1 && "0".equals(form.getmChk())) {
                            form.setmChk(StringUtils.EMPTY);
                        } else if (form.getmChk().length() > 2 && "0,".equals(form.getmChk().substring(0, 2))) {
                            form.setmChk(form.getmChk().substring(2));
                        }
                        if (StringUtils.isBlank(form.getmChk())) {
                            response.setMessage(MessageType.DANGER, "尚未選取任何月份!");
                            return false;
                        }
                        batch.setBatchScheduleMonths(form.getmChk());
                        if (form.getMdChk().length() == 1 && "0".equals(form.getMdChk())) {
                            form.setMdChk(StringUtils.EMPTY);
                        } else if (form.getMdChk().length() > 2 && "0,".equals(form.getMdChk().substring(0, 2))) {
                            form.setMdChk(form.getMdChk().substring(2));
                        }
                        if (StringUtils.isNotBlank(form.getMdChk())) {
                            batch.setBatchScheduleMonthdays(form.getMdChk());
                        } else {
                            response.setMessage(MessageType.DANGER, "尚未選取任何日期!");
                            return false;
                        }
                        break;
                    }
                    case "O": {
                        if (form.getmChk().length() == 1 && "0".equals(form.getmChk())) {
                            form.setmChk(StringUtils.EMPTY);
                        } else if (form.getmChk().length() > 2 && "0,".equals(form.getmChk().substring(0, 2))) {
                            form.setmChk(form.getmChk().substring(2));
                        }
                        if (StringUtils.isBlank(form.getmChk())) {
                            response.setMessage(MessageType.DANGER, "尚未選取任何月份!");
                            return false;
                        }
                        batch.setBatchScheduleMonths(form.getmChk());
                        if (form.getMwChk().length() == 1 && "0".equals(form.getMwChk())) {
                            form.setMwChk(StringUtils.EMPTY);
                        } else if (form.getMwChk().length() > 2 && "0,".equals(form.getMwChk().substring(0, 2))) {
                            form.setMwChk(form.getMwChk().substring(2));
                        }
                        if (StringUtils.isBlank(form.getMwChk())) {
                            response.setMessage(MessageType.DANGER, "尚未選取任何星期!");
                            return false;
                        }
                        batch.setBatchScheduleWeekdays(form.getMwChk());
                        if (form.getWmChk().length() == 1 && "0".equals(form.getWmChk())) {
                            form.setWmChk(StringUtils.EMPTY);
                        } else if (form.getWmChk().length() > 2 && "0,".equals(form.getWmChk().substring(0, 2))) {
                            form.setWmChk(form.getWmChk().substring(2));
                        }
                        if (StringUtils.isBlank(form.getWmChk())) {
                            response.setMessage(MessageType.DANGER, "尚未選取哪一週!");
                            return false;
                        }
                        batch.setBatchScheduleWhickweeks(form.getWmChk());
                        break;
                    }
                    default: {
                        response.setMessage(MessageType.DANGER, "尚未選取排程方式");
                        return false;
                    }
                }
            }
            if (batch.getBatchNotifytype() != 0) {
                if (form.getNotify().isFepNotifyMail_Customize()) {
                    if (StringUtils.isBlank(form.getNotify().getBatchNotifymail())) {
                        response.setMessage(MessageType.DANGER, "未輸入自定Mail通知");
                        return false;
                    } else {
                        RefString invalidMail = new RefString();
                        if (!form.getNotify().isFepNotifyMailCustomizeValid(invalidMail)) {
                            response.setMessage(MessageType.DANGER, StringUtils.join("自定Mail通知", String.format(Const.KEY_WORDS_IN_MESSAGE_S, invalidMail.get()), "格式不正確"));
                            return false;
                        }
                    }
                }
                if (form.getNotify().isFepNotifyPhone_Customize() && StringUtils.isBlank(form.getNotify().getBatchNotifyphone())) {
                    response.setMessage(MessageType.DANGER, "未輸入自定簡訊通知");
                    return false;
                } else if (StringUtils.isBlank(batch.getBatchNotifymail()) && StringUtils.isBlank(batch.getBatchNotifyphone())) {
                    response.setMessage(MessageType.DANGER, "Mail或簡訊通知至少有一個欄位必須有值");
                    return false;
                }
            }
//            else {
//                if (form.getNotify().isFepNotifyMail_APD() || form.getNotify().isFepNotifyMail_SYS() || form.getNotify().isFepNotifyMail_Customize() ||
//                        form.getNotify().isFepNotifyPhone_APD() || form.getNotify().isFepNotifyPhone_SYS() || form.getNotify().isFepNotifyPhone_Customize()) {
//                    response.setMessage(MessageType.DANGER, StringUtils.join("請確認", String.format(Const.KEY_WORDS_IN_MESSAGE_S, "通知方式"), "是否選擇正確"));
//                    return false;
//                }
//            }
            if (StringUtils.isBlank(batch.getBatchExecuteHostName()) && DbHelper.toBoolean(batch.getBatchDenyconcurrentexec())) {
                response.setMessage(MessageType.DANGER, "如果要設定不允許重覆執行, 則批次必須只能在一台主機執行");
                return false;
            }
            return true;
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.setMessage(MessageType.DANGER, programError);
            return false;
        }
    }

    private List<HashMap<String, Object>> bindJobGrid() {
        MaintainBatch maintainBatch = this.getSessionData();
        if (Objects.equals(maintainBatch.getBatch().getBatchBatchid(), EMPTY_BATCH_ID)) {
            return new ArrayList<>();
        }
        List<HashMap<String, Object>> result = obj.getJobTaskByBatchId(maintainBatch.getBatch().getBatchBatchid());
        // 如果JOBS_CONTINUEONFAIL沒有值, 則塞入預設值
        result.forEach(t -> {
            t.computeIfAbsent("JOBS_CONTINUEONFAIL", k -> JobsContinueOnFail.Interrupt.ordinal());
        });
        return result;
    }

    @PostMapping(value = "/batch/UI_000100/saveTaskClick")
    @ResponseBody
    public BaseResp<ArrayList<HashMap<String, Object>>> saveTaskClick(@RequestBody UI_000100_Task_Form form) {
        this.infoMessage("存儲批次Task, 條件 = [", form.toString(), "]");
        BaseResp<ArrayList<HashMap<String, Object>>> resp = new BaseResp<>();
        MaintainBatch maintainBatch = this.getSessionData();
        try {
            // 在新增模式下
            if ("insert".equals(maintainBatch.getDetail()) || Objects.equals(maintainBatch.getBatch().getBatchBatchid(), EMPTY_BATCH_ID)) {
                Task task = getTaskByTaskId(form.getTskId(), resp);
                if (task != null) {
                    MaintainTask maintainTask = new MaintainTask(task);
                    maintainTask.setJobsContinueonfail(Integer.parseInt(form.getJobsContinueOnFail()));
                    int found = -1;
                    for (int i = 0; i < maintainBatch.getTasks().size(); i++) {
                        if (i == form.getJobId()) {
                            found = i;
                            break;
                        }
                    }
                    if (found != -1) {
                        maintainBatch.getTasks().set(found, maintainTask);
                    } else {
                        maintainBatch.getTasks().add(maintainTask);
                    }
                    resp.setMessage(MessageType.INFO, "存儲Task成功");
                }
                resp.setData(maintainBatch.makeGridData());
            }
            // 在編輯模式下
            else {
                Jobs job = new Jobs();
                if (checkAllJobField(job, form, resp)) {
                    job.setJobsBatchid(maintainBatch.getBatch().getBatchBatchid());
                    // 記錄auditLog
                    AuditLog auditLog = new AuditLog();
                    // 塞入auditLog.action
                    auditLog.setAction("批次程式清單（儲存）");
                    auditLog.addParam("程序編號", form.getTskId());
                    // 最後塞入ThreadLocal變量中
                    setAuditLog(auditLog);
                    int ret = obj.updateJob(job, form.getTskId());
                    if (ret > 0)
                        resp.setMessage(MessageType.INFO, "存儲Task成功");
                    else
                        resp.setMessage(MessageType.DANGER, "存儲Task失敗");
                }
                resp.setData(new ArrayList<>(bindJobGrid()));
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            resp.setMessage(MessageType.DANGER, programError);
        }
        return resp;
    }

    @PostMapping(value = "/batch/UI_000100/insertTaskClick")
    @ResponseBody
    public BaseResp<ArrayList<HashMap<String, Object>>> insertTaskClick(@RequestBody UI_000100_Task_Form form) {
        this.infoMessage("新增批次Task, 條件 = [", form.toString(), "]");
        BaseResp<ArrayList<HashMap<String, Object>>> resp = new BaseResp<>();
        MaintainBatch maintainBatch = this.getSessionData();
        try {
            // 在新增模式下
            if ("insert".equals(maintainBatch.getDetail()) || Objects.equals(maintainBatch.getBatch().getBatchBatchid(), EMPTY_BATCH_ID)) {
                Task task = getTaskByTaskId(form.getTskId(), resp);
                if (task != null) {
                    MaintainTask maintainTask = new MaintainTask(task);
                    maintainTask.setJobsContinueonfail(Integer.parseInt(form.getJobsContinueOnFail()));
                    maintainBatch.getTasks().add(maintainTask);
                    resp.setMessage(MessageType.INFO, "新增Task成功");
                }
                resp.setData(maintainBatch.makeGridData());
            }
            // 在編輯模式下
            else {
                Jobs job = new Jobs();
                if (checkAllJobField(job, form, resp)) {
                    job.setJobsBatchid(maintainBatch.getBatch().getBatchBatchid());
                    int ret = obj.insertJobAndTask(job, form.getTskId());
                    // 2024-04-18 Richard modified 只有設定為排程才需要通知批次服務平台
                    if (maintainBatch.getBatch().getBatchSchedule() == 1) {
                        obj.createScheduleTask(maintainBatch.getBatch().getBatchBatchid());
                    }
                    if (ret > 0)
                        resp.setMessage(MessageType.INFO, "新增Task成功");
                    else
                        resp.setMessage(MessageType.DANGER, "新增Task失敗");
                }
                resp.setData(new ArrayList<>(bindJobGrid()));
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            resp.setMessage(MessageType.DANGER, programError);
        }
        return resp;
    }

    private boolean checkAllJobField(Jobs job, UI_000100_Task_Form form, BaseResp<?> resp) {
        try {
            Task tsk = obj.getTaskById(form.getTskId());
            if (tsk == null) {
                resp.setMessage(MessageType.DANGER, "Task不存在");
                return false;
            }
            if (form.getSender() == 0) {
                // Add from EmptyDataTemplate
                job.setJobsName(tsk.getTaskName());
                job.setJobsDescription(tsk.getTaskDescription());
                job.setJobsDelay(0);
                job.setJobsSeq(1);
                job.setJobsStarttaskid(0);
            } else if (form.getSender() > 0) {
                // Add from FooterDataTemplate
                job.setJobsName(tsk.getTaskName());
                job.setJobsDescription(tsk.getTaskDescription());
                job.setJobsDelay(0);
                job.setJobsSeq(form.getSender() + 1);
                job.setJobsStarttaskid(0);
            } else {
                job.setJobsJobid(form.getJobId());
                job.setJobsName(tsk.getTaskName());
                job.setJobsDescription(tsk.getTaskDescription());
            }
            job.setJobsContinueonfail(Short.parseShort(form.getJobsContinueOnFail())); // 塞入UI設定的JobsContinueonfail
            return true;
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            resp.setMessage(MessageType.DANGER, programError);
            return false;
        }
    }

    @PostMapping(value = "/batch/UI_000100/delTaskClick")
    @ResponseBody
    public BaseResp<ArrayList<HashMap<String, Object>>> delTaskClick(@RequestBody UI_000100_Task_Form form) {
        this.infoMessage("刪除批次Task, 條件 = [", form.toString(), "]");
        BaseResp<ArrayList<HashMap<String, Object>>> resp = new BaseResp<>();
        MaintainBatch maintainBatch = this.getSessionData();
        try {
            // 在新增模式下
            if ("insert".equals(maintainBatch.getDetail()) || Objects.equals(maintainBatch.getBatch().getBatchBatchid(), EMPTY_BATCH_ID)) {
                if (!maintainBatch.getTasks().isEmpty()) {
                    maintainBatch.getTasks().remove(form.getJobId());
                }
                resp.setData(maintainBatch.makeGridData());
                resp.setMessage(MessageType.INFO, "刪除Task成功");
            }
            // 在編輯模式下
            else {
                int ret = obj.deleteJob(form.getJobId());
                resp.setData(new ArrayList<>(bindJobGrid()));
                // 2025-08-19 Richard add 只有設定為排程才需要通知批次服務平台
                if (maintainBatch.getBatch().getBatchSchedule() == 1) {
                    obj.createScheduleTask(maintainBatch.getBatch().getBatchBatchid());
                }
                if (ret > 0)
                    resp.setMessage(MessageType.INFO, "刪除Task成功");
                else
                    resp.setMessage(MessageType.DANGER, "刪除Task失敗");
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            resp.setMessage(MessageType.DANGER, programError);
        }
        return resp;
    }

    @PostMapping(value = "/batch/UI_000100/changeTaskOrder")
    @ResponseBody
    public UI_000100_Task_Form changeTaskOrder(@RequestBody UI_000100_Task_Form form) {
        this.infoMessage("變更Task順序, 條件 = [", form.toString(), "]");
        try {
            if (form.getSender() == null) {
                form.setMessage(MessageType.DANGER, "批次Task清單不可以為空！");
                return form;
            }
            // 2024-04-24 Richard add start for 【Unchecked Input for Loop Condition】
            int nTask = form.getSender();
            if (nTask > MAX_LOOPS) {
                nTask = MAX_LOOPS;
            }
            // 2024-04-24 Richard add start for 【Unchecked Input for Loop Condition】
            String[] jobsSeqs = new String[nTask]; // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
            String[] jobsJobIDs = new String[nTask]; // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
            String[] taskIDs = new String[nTask]; // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
            String[] seqs = new String[nTask]; // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
            String[] jobsContinueOnFails = new String[nTask];
            if (nTask > 0) { // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
                seqs = form.getJobsSeq().split(",");
                jobsSeqs = form.getJobsSeq().split(",");
                jobsJobIDs = form.getJobsJobID().split(",");
                taskIDs = form.getTaskID().split(",");
                jobsContinueOnFails = form.getJobsContinueOnFail().split(",");
            }
            ArrayList<String> tmp = new ArrayList<>();
            if (nTask > MAX_LOOPS) {
                nTask = MAX_LOOPS;
            }
            for (int i = 0; i < nTask; i++) { // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
                tmp.add((i + 1) + StringUtils.EMPTY);
            }
            Arrays.sort(seqs);
            String[] strArr = null;
            strArr = tmp.toArray(new String[tmp.size()]);
            if (!Arrays.equals(strArr, seqs)) {
                form.setMessage(MessageType.DANGER, "順序必須由1開始且不能跳號也不能重覆！");
                return form;
            }
            MaintainBatch maintainBatch = this.getSessionData();
            // 在新增模式下
            if ("insert".equals(maintainBatch.getDetail()) || Objects.equals(maintainBatch.getBatch().getBatchBatchid(), EMPTY_BATCH_ID)) {
                List<MaintainTask> tasks = new ArrayList<>(nTask); // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
                if (nTask > MAX_LOOPS) {
                    nTask = MAX_LOOPS;
                }
                for (int i = 0; i < nTask; i++) { // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
                    tasks.add(null);
                }
                if (nTask > MAX_LOOPS) {
                    nTask = MAX_LOOPS;
                }
                for (int i = 0; i < nTask; i++) { // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
                    Task task = this.getTaskByTaskId(Integer.parseInt(taskIDs[i]), form);
                    MaintainTask maintainTask = new MaintainTask(task);
                    maintainTask.setJobsContinueonfail(Integer.parseInt(jobsContinueOnFails[i]));
                    tasks.set(Integer.parseInt(jobsSeqs[i]) - 1, maintainTask);
                }
                tasks.removeIf(Objects::isNull); // 上面查詢到的Task有可能不存在, 則要移除掉null的task
                maintainBatch.getTasks().clear();
                maintainBatch.getTasks().addAll(tasks);
                form.setData(maintainBatch.makeGridData());
                form.setMessage(MessageType.INFO, "變更Task順序成功");
            }
            // 在編輯模式下
            else {
                int startJob = 0;
                if (nTask > MAX_LOOPS) {
                    nTask = MAX_LOOPS;
                }
                int ret = 0;
                for (int i = 0; i < nTask; i++) { // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
                    int seq = Integer.parseInt(jobsSeqs[i]);
                    Jobs job = new Jobs();
                    job.setJobsJobid(Integer.parseInt(jobsJobIDs[i]));
                    job.setJobsSeq(seq);
                    Integer tskId = Integer.parseInt(taskIDs[i]);
                    ret += obj.updateJob(job, tskId);
                    if (seq == 1) {
                        startJob = job.getJobsJobid();
                    }
                }
                Batch batch1 = new Batch();
                batch1.setBatchBatchid(maintainBatch.getBatch().getBatchBatchid());
                batch1.setBatchZone(null); // 避免原本的欄位被更新掉
                if (nTask == 0) { // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
                    batch1.setBatchStartjobid(0);
                } else {
                    batch1.setBatchStartjobid(startJob);
                }
                ret += obj.updateBatch(maintainBatch.getBatch(), WebUtil.getUser().getUserId(), false);  // 2025-12-18 Richard modified 更新完batch檔不用將BatchNextruntime清掉, 故最後一個參數為false
                form.setData(new ArrayList<>(bindJobGrid()));
                // 2025-08-19 Richard add 只有設定為排程才需要通知批次服務平台
                if (maintainBatch.getBatch().getBatchSchedule() == 1) {
                    obj.createScheduleTask(maintainBatch.getBatch().getBatchBatchid());
                }
                if (ret > 0)
                    form.setMessage(MessageType.INFO, "變更Task順序成功");
                else
                    form.setMessage(MessageType.DANGER, "變更Task順序失敗");
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            form.setMessage(MessageType.DANGER, programError);
        }
        return form;
    }

    /**
     * 初始化通知方式
     *
     * @param batch
     */
    private void resetNotify(Batch batch) {
        batch.setBatchNotifytype((short) 0);
        batch.setBatchNotifymail(StringUtils.EMPTY);
        batch.setBatchAction(StringUtils.EMPTY);
        batch.setBatchNotifyphone(StringUtils.EMPTY);
    }

    /**
     * 初始化是否排程
     *
     * @param batch
     * @param resetBatchScheduleType
     */
    private void resetScheduleData(Batch batch, boolean resetBatchScheduleType) {
        if (batch.getBatchSchedule() == null)
            batch.setBatchSchedule((short) 0);
        if (resetBatchScheduleType) {
            batch.setBatchScheduleType(null);
            batch.setBatchDailyRepetitionType(null);
        }
        if (batch.getBatchScheduleDayinterval() == null)
            batch.setBatchScheduleDayinterval((short) 0);
        if (batch.getBatchScheduleRepetitioninterval() == null)
            batch.setBatchScheduleRepetitioninterval((short) 0);
        if (batch.getBatchScheduleRepetitioninduration() == null)
            batch.setBatchScheduleRepetitioninduration((short) 0);
        if (batch.getBatchScheduleWeekinterval() == null)
            batch.setBatchScheduleWeekinterval((short) 0);
        if (batch.getBatchScheduleMonths() == null)
            batch.setBatchScheduleMonths(StringUtils.EMPTY);
        if (batch.getBatchScheduleWeekdays() == null)
            batch.setBatchScheduleWeekdays(StringUtils.EMPTY);
        if (batch.getBatchScheduleWhickweeks() == null)
            batch.setBatchScheduleWhickweeks(StringUtils.EMPTY);
        if (batch.getBatchScheduleMonthdays() == null)
            batch.setBatchScheduleMonthdays(StringUtils.EMPTY);
        if (StringUtils.isBlank(batch.getBatchDailyRepetitionType()))
            batch.setBatchDailyRepetitionType(BatchDailyRepetitionType.DAY.getValue());
    }

    /**
     * 從Session獲取全局臨時變數
     *
     * @return
     */
    private MaintainBatch getSessionData() {
        return WebUtil.getFromSession(SessionKey.TemporaryRestoreData);
    }

    /**
     * 根據TaskId獲取Task
     *
     * @param taskId
     * @param resp
     * @return
     */
    private Task getTaskByTaskId(Integer taskId, BaseResp<?> resp) {
        Task task = obj.getTaskById(taskId);
        if (task == null) {
            resp.setMessage(MessageType.DANGER, "Task不存在");
        }
        return task;
    }

    @PostMapping(value = "/batch/UI_000100/btnChangeHost", produces = "application/json;charset=utf-8")
    public String btnChangeHost(@ModelAttribute UI_000100_Form form, ModelMap mode) {
        this.infoMessage("切換執行主機頁面, 條件 = [", form.toString(), "]");
        form = new UI_000100_Form();
        this.doKeepFormData(mode, form);
        try {
            List<SelectOption<String>> options = new ArrayList<>();
            List<String> apHostNameList = batchBaseConfiguration.getHost().stream().map(BatchBaseConfigurationHost::getName).collect(Collectors.toList());
            options.add(new SelectOption<String>(" ", StringUtils.EMPTY));
            for (String apHostName : apHostNameList) {
                options.add(new SelectOption<String>(apHostName, apHostName));
            }
            mode.addAttribute("options", options);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_000100_C.getView();
    }

    @PostMapping(value = "/batch/UI_000100_C/confirmClick", produces = "application/json;charset=utf-8")
    public String confirmClick(@ModelAttribute UI_000100_Form formC, ModelMap mode) {
        this.infoMessage("執行主機確認, 條件 = [", formC.toString(), "]");
        this.doKeepFormData(mode, formC);
        try {
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            auditLog.setAction("切換執行主機");
            // 塞入auditLog.params
            auditLog.addParam("SourceHost", formC.getSourceHost());
            auditLog.addParam("TargetHost", formC.getTargetHost());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            int res = batchService.updateBatchHost(formC.getSourceHost(), formC.getTargetHost());
            MaintainBatch maintainBatch = new MaintainBatch();
            WebUtil.putInSession(SessionKey.TemporaryRestoreData, maintainBatch);
            UI_000100_Form form = new UI_000100_Form();
            form.setUrl(URL_DO_QUERY);
            String view = this.queryClick(form, mode);
            if (res > 0)
                this.showMessage(mode, MessageType.SUCCESS, "切換執行主機成功");
            else
                this.showMessage(mode, MessageType.DANGER, "切換執行主機失敗");
            return view;
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
            return Router.UI_000100_C.getView();
        }
    }

    @PostMapping(value = "/batch/UI_000100/doRestBatchStatus")
    @ResponseBody
    private BaseResp<?> doRestBatchStatus() {
        BaseResp<?> response = new BaseResp<>();
        MaintainBatch maintainBatch = this.getSessionData();
        this.infoMessage("重設批次狀態, BatchBatchid = [", maintainBatch.getBatch().getBatchBatchid(), "]");
        // 新增模式下無法重設
        if ("insert".equals(maintainBatch.getDetail()) || Objects.equals(maintainBatch.getBatch().getBatchBatchid(), EMPTY_BATCH_ID)) {
            response.setMessage(MessageType.DANGER, "無法重設批次狀態");
        } else {
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            auditLog.setAction("重設批次狀態");
            // 塞入auditLog.params
            auditLog.addParam("Batch_Id", String.valueOf(maintainBatch.getBatch().getBatchBatchid()));
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            try {
                int result = batchService.updateBatchResult(maintainBatch.getBatch().getBatchBatchid(), null);
                if (result > 0) {
                    maintainBatch.getBatch().setBatchResult(null); // 2025-08-21 Richard add 這裡記得要塞成null
                    response.setMessage(MessageType.SUCCESS, "重設批次狀態成功");
                } else {
                    response.setMessage(MessageType.DANGER, "重設批次狀態失敗");
                }
            } catch (Exception e) {
                this.errorMessage(e, e.getMessage());
                response.setMessage(MessageType.DANGER, programError);
            }
        }
        return response;
    }

    /**
     * 主頁上批量重建排程
     *
     * @return
     */
    @PostMapping(value = "/batch/UI_000100/makeQuartz")
    @ResponseBody
    public BaseResp<?> makeQuartz() {
        BaseResp<?> response = new BaseResp<>();
        // 記錄auditLog
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("重建排程");
        // 最後塞入ThreadLocal變量中
        setAuditLog(auditLog);
        try {
            List<HashMap<String, Object>> batchList = obj.getAllBatch(WebConfiguration.getInstance().getSubsysList());
            if (CollectionUtils.isEmpty(batchList)) {
                response.setMessage(MessageType.WARNING, "查無排程資料");
            } else {
                for (HashMap<String, Object> batch : batchList) {
                    int batchId = DbHelper.getMapValue(batch, "BATCH_BATCHID", -1);
                    String batchName = DbHelper.getMapValue(batch, "BATCH_NAME", StringUtils.EMPTY);
                    boolean batchSchedule = DbHelper.toBoolean(DbHelper.getMapValue(batch, "BATCH_SCHEDULE", 0).shortValue(), false);
                    if (batchId != -1) {
                        // 因為不確定之前是否有建排程, 也不清楚哪個host上有建, 故預設先全部刪除
                        obj.deleteScheduleTask(null, batchId, batchName, true);
                        if (batchSchedule) {
                            obj.createScheduleTask(batchId);
                        }
                        this.infoMessage("重建排程成功, batchId:", batchId, ",batchName:", batchName, ",batchSchedule:", batchSchedule);
                    }
                }
                response.setMessage(MessageType.SUCCESS, "重建排程成功");
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.setMessage(MessageType.DANGER, programError);
        }
        return response;
    }

    /**
     * 明細編輯頁面上重建排程
     *
     * @return
     */
    @PostMapping(value = "/batch/UI_000100/makeQuartzForBatch")
    @ResponseBody
    public BaseResp<?> makeQuartzForBatch() {
        BaseResp<?> response = new BaseResp<>();
        MaintainBatch maintainBatch = this.getSessionData();
        this.infoMessage("重建排程, BatchBatchid = [", maintainBatch.getBatch().getBatchBatchid(), "]");
        // 新增模式下無法重建排程
        if ("insert".equals(maintainBatch.getDetail()) || Objects.equals(maintainBatch.getBatch().getBatchBatchid(), EMPTY_BATCH_ID)) {
            response.setMessage(MessageType.DANGER, "無法重建排程");
        } else {
            try {
                Batch batch = batchService.getBatchByID(maintainBatch.getBatch().getBatchBatchid());
                if (batch == null) {
                    response.setMessage(MessageType.DANGER, "無法重建排程, 資料不存在");
                } else {
                    // 記錄auditLog
                    AuditLog auditLog = new AuditLog();
                    auditLog.setAction("重建排程");
                    // 塞入auditLog.params
                    auditLog.addParam("Batch_Id", batch.getBatchBatchid());
                    auditLog.addParam("Batch_Name", batch.getBatchName());
                    auditLog.addParam("Batch_Schedule", DbHelper.toBoolean(batch.getBatchSchedule(), false));
                    auditLog.addParam("forceDelete", true);
                    // 最後塞入ThreadLocal變量中
                    setAuditLog(auditLog);
                    boolean batchSchedule = DbHelper.toBoolean(batch.getBatchSchedule(), false);
                    // 因為不確定之前是否有建排程, 也不清楚哪個host上有建, 故預設先全部刪除
                    obj.deleteScheduleTask(null, batch.getBatchBatchid(), batch.getBatchName(), true);
                    if (batchSchedule) {
                        obj.createScheduleTask(batch.getBatchBatchid());
                    }
                    this.infoMessage("重建排程成功, batchId:", batch.getBatchBatchid(), ",batchName:", batch.getBatchName(), ",batchSchedule:", batchSchedule);
                    response.setMessage(MessageType.SUCCESS, "重建排程成功");
                }
            } catch (Exception e) {
                this.errorMessage(e, e.getMessage());
                response.setMessage(MessageType.DANGER, programError);
            }
        }
        return response;
    }
}
