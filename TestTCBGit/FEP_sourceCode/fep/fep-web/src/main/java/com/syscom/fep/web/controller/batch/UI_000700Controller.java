package com.syscom.fep.web.controller.batch;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.batch.base.util.BatchRestfulClient;
import com.syscom.fep.batch.base.vo.restful.BatchScheduler;
import com.syscom.fep.batch.base.vo.restful.request.ListSchedulerRequest;
import com.syscom.fep.batch.base.vo.restful.response.ListSchedulerResponse;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.entity.batch.ScheduledBatch;
import com.syscom.fep.web.form.batch.UI_000700_Form;
import com.syscom.fep.web.service.BatchService;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UI_000700Controller extends BaseController {
    @Autowired
    private BatchService batchService;
    @Autowired
    private BatchRestfulClient client;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_000700_Form form = new UI_000700_Form();
        form.setBatchExecuteDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/batch/UI_000700/queryClick")
    private String doQuery(@ModelAttribute UI_000700_Form form, ModelMap mode) {
        this.infoMessage("查詢數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            // 首次按下查詢時預設的排序
            if (form.getSqlSortExpressionCount() == 0) {
                form.addSqlSortExpression("BATCH_NAME", SQLSortExpression.SQLSortOrder.ASC);
            }
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("查詢");
            if (StringUtils.isNotBlank(form.getBatchExecuteDate()))
                auditLog.addParam("批次執行日期", form.getBatchExecuteDate());
            if (StringUtils.isNotBlank(form.getBatchName()))
                auditLog.addParam("批次簡稱", form.getBatchName());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            List<Batch> batchList = batchService.getAllBatchByLastRunTime(form.getBatchName(), form.getSqlSortExpression());
            List<BatchScheduler> batchSchedulerList = listScheduler(batchList);
            List<ScheduledBatch> scheduledBatchList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(batchList)) {
                String batchExecuteDate = StringUtils.replace(form.getBatchExecuteDate(), "-", StringUtils.EMPTY);
                for (Batch batch : batchList) {
                    if (CollectionUtils.isNotEmpty(batchSchedulerList)) {
                        List<BatchScheduler> list = batchSchedulerList.stream().filter(t -> t.getBatchId().equals(batch.getBatchBatchid().toString())).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(list)) {
                            for (BatchScheduler batchScheduler : list) {
                                if (batchScheduler.getNextFireTime() != null) {
                                    String nextExecutedDateTime = FormatUtil.dateTimeFormat(CalendarUtil.clone(batchScheduler.getNextFireTime()), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
                                    if (batchExecuteDate.equals(nextExecutedDateTime)) {
                                        ScheduledBatch scheduledBatch = new ScheduledBatch(batch);
                                        scheduledBatch.setBatchNextruntime(batchScheduler.getNextFireTime());
                                        scheduledBatchList.add(scheduledBatch);
                                    }
                                }
                            }
                        }
                    } else {
                        if (batch.getBatchNextruntime() != null) {
                            String batchNextRuntime = FormatUtil.dateTimeFormat(CalendarUtil.clone(batch.getBatchNextruntime()), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
                            if (batchExecuteDate.equals(batchNextRuntime)) {
                                ScheduledBatch scheduledBatch = new ScheduledBatch(batch);
                                scheduledBatch.setBatchNextruntime(batch.getBatchNextruntime());
                                scheduledBatchList.add(scheduledBatch);
                            }
                        }
                    }
                }
            }
            if (CollectionUtils.isEmpty(scheduledBatchList)) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            PageInfo<ScheduledBatch> pageInfo = clientPaged(scheduledBatchList, form.getPageNum(), form.getPageSize());
            PageData<UI_000700_Form, ScheduledBatch> pageData = new PageData<>(pageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_000700.getView();
    }


    /**
     * 從Batch Control Service取排程的訊息
     *
     * @param batchList
     * @return
     * @throws Exception
     */
    private List<BatchScheduler> listScheduler(List<Batch> batchList) throws Exception {
        if (CollectionUtils.isEmpty(batchList)) {
            return null;
        }
        List<BatchScheduler> batchSchedulerList = new ArrayList<>(batchList.size());
        for (Batch batch : batchList) {
            BatchScheduler batchScheduler = new BatchScheduler();
            batchScheduler.setBatchId(batch.getBatchBatchid().toString());
            batchSchedulerList.add(batchScheduler);
        }
        ListSchedulerRequest request = new ListSchedulerRequest();
        request.setBatchSchedulerList(batchSchedulerList);
        request.setOperator(WebUtil.getUser().getUserId());
        ListSchedulerResponse response = client.listScheduler(request);
        if (response != null) {
            if (!response.isResult()) {
                throw ExceptionUtil.createException("排程訊息查詢失敗, ", response.getMessage());
            }
            List<BatchScheduler> list = response.getBatchSchedulerList();
            return list;
        } else {
            throw ExceptionUtil.createException("排程訊息查詢失敗!!!");
        }
    }
}
