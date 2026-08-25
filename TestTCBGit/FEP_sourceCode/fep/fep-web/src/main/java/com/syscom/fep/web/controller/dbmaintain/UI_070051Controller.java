package com.syscom.fep.web.controller.dbmaintain;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.web.audit.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.ref.RefLong;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.dbmaintain.UI_070051_Form;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
/**
 * 跨行系統參數維護
 *
 * @author Joseph
 * @create 2022/05/23
 */
@Controller
public class UI_070051Controller extends BaseController {

    @Autowired
    private InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_070051_Form form = new UI_070051_Form();
        form.setUrl("/dbmaintain/UI_070051/queryClick");
        form.setCbspendTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        this.queryClick(form, mode);
    }

    @PostMapping(value = "/dbmaintain/UI_070051/queryClick")
    private String queryClick(@ModelAttribute UI_070051_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        RefLong count = new RefLong(0);
        BindGridData(form, mode, count);
        return Router.UI_070051.getView();
    }

    public void BindGridData(UI_070051_Form form, ModelMap mode, RefLong count) {
        PageInfo<HashMap<String, Object>> dt = null;
        try {
            String txdate = StringUtils.replace((form.getCbspendTxDate()), "-", StringUtils.EMPTY);

            AuditLog auditLog = new AuditLog();
            auditLog.setAction("查詢");
            if (StringUtils.isNotBlank(txdate)) {
                auditLog.addParam("交易日期", txdate);
            }
            if (form.getCbspendSuccessFlag() != null) {
                auditLog.addParam("狀態", form.getCbspendSuccessFlag());
            }
            if (form.getCbspendSubsys() != null) {
                auditLog.addParam("系統別", form.getCbspendSubsys());
            }
            if (StringUtils.isNotBlank(form.getCbspendZone())) {
                auditLog.addParam("地區代號", form.getCbspendZone());
            }
            if (StringUtils.isNotBlank(form.getCbspendCbsTxCode())) {
                auditLog.addParam("主機交易代號", form.getCbspendCbsTxCode());
            }
            setAuditLog(auditLog);

            BigDecimal summary = inbkService.getCbspendSummary(txdate, form.getCbspendSuccessFlag(),
                    form.getCbspendSubsys(), form.getCbspendZone(), form.getCbspendCbsTxCode());
            dt = PageHelper
                    .startPage(form.getPageNum(), form.getPageSize(), form.getPageNum() > 0 && form.getPageSize() > 0)
                    .doSelectPageInfo(new ISelect() {
                        @Override
                        public void doSelect() {
                                inbkService.GetCBSPENDByTXDATE(txdate, form.getCbspendSuccessFlag(),
                                        form.getCbspendSubsys(), form.getCbspendZone(), form.getCbspendCbsTxCode());
                        }
                    });
            count.set(dt.getTotal());
            form.setTotalCNT(dt.getTotal());
            form.setTotalAMT(summary);
            PageData<UI_070051_Form, HashMap<String, Object>> pageData = new PageData<>(dt, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception exception) {
        	this.errorMessage(exception, exception.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
        }
    }
}
