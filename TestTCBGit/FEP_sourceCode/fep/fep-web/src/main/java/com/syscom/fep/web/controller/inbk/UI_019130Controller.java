package com.syscom.fep.web.controller.inbk;

import java.util.Calendar;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.form.inbk.UI_019130_Form;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.syscom.fep.mybatis.model.Inbkpend;
import com.syscom.fep.web.controller.BaseController;

/**
 * 查詢傳送未完成交易結果-2130I
 *
 * @author ChenYu
 */
@Controller
public class UI_019130Controller extends BaseController {
    @Autowired
    InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_019130_Form form = new UI_019130_Form();
        //交易日期
        form.setDatetime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }
    @PostMapping(value = "/inbk/UI_019130/getINBKPendList")
    public String getINBKPendList(@ModelAttribute UI_019130_Form form, ModelMap mode) {
        this.infoMessage("查詢主檔資料, 條件 = [", form, "]");
        this.doKeepFormData(mode, form);
        try {
            // 轉成map對象供最後mybatis查詢資料使用
            String datetime = form.getDatetime();
            if (StringUtils.isNotBlank(datetime)) {
                datetime = StringUtils.replace(datetime, "-", StringUtils.EMPTY);
            } else {
                Calendar now = Calendar.getInstance();
                datetime = FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
                form.setDatetime(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
            }
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("查詢");
            // 塞入auditLog.params
            if (StringUtils.isNotBlank(form.getDatetime()))
                auditLog.addParam("交易日期", form.getDatetime());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            String inbkpendPcode =  "2130";
            PageInfo<Inbkpend> pageInfo = inbkService.getINBKPendList(datetime,inbkpendPcode,form.getPageNum(),form.getPageSize());
            if ( pageInfo.getSize() == 0) {
                this.showMessage(mode, MessageType.INFO,QueryNoData);
            }else {//20220908 Bruce add 查詢成功 start
            	this.showMessage(mode, MessageType.INFO, QuerySuccess);
            }//20220908 Bruce add 查詢成功 end
            PageData<UI_019130_Form, Inbkpend> pageData = new PageData<>(pageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            //20220908 Bruce Modify start
            //this.showMessage(mode, MessageType.DANGER,ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
            //20220908 Bruce Modify end
        }
        return Router.UI_019130.getView();
    }
}
