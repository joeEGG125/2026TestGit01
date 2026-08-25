package com.syscom.fep.web.controller.inbk;

import java.util.Calendar;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019140_Form;
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
 * 查詢代理單位人工沖正請求-2140I
 *
 * @author xingyun_yang
 * @create 2021/8/19
 */
@Controller
public class UI_019140Controller extends BaseController {
    @Autowired
    InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_019140_Form form = new UI_019140_Form();
        // 交易日期
        form.setDatetime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }
    @PostMapping(value = "/inbk/UI_019140/getINBKPendList")
    public String getINBKPendList(@ModelAttribute UI_019140_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form, "]");
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
            String inbkpendPcode =form.getInbkpendPcode();
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("查詢");
            // 塞入auditLog.params
            if (StringUtils.isNotBlank(form.getDatetime()))
                auditLog.addParam("交易日期", form.getDatetime());
            if (StringUtils.isNotBlank(form.getInbkpendPcode()))
                auditLog.addParam("PCODE", form.getInbkpendPcode());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            PageInfo<Inbkpend> pageInfo =
                    inbkService.getINBKPendList(datetime,inbkpendPcode,form.getPageNum(),form.getPageSize());
            if (pageInfo.getSize() == 0) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }else {//20220908 Bruce add 查詢成功 start
            	this.showMessage(mode, MessageType.INFO, QuerySuccess);
            }//20220908 Bruce add 查詢成功 end
            PageData<UI_019140_Form, Inbkpend> pageData = new PageData<>(pageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            //20220907 Bruce Modify start
            //this.showMessage(mode, MessageType.DANGER,ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
            //20220907 Bruce Modify end
        }
        return Router.UI_019140.getView();
    }
}
