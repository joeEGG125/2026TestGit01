package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Clrtotal;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019102_Form;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * 查詢財金跨行結帳資料-5102I
 *
 * @author xingyun_yang
 * @create 2021/9/13
 */
@Controller
public class UI_019102Controller extends BaseController {

    @Autowired
    InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_019102_Form form = new UI_019102_Form();
        form.setClrtotalStDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        // 財金STAN
        String bankNo = StringUtils.EMPTY;
        try {
            bankNo = SysStatus.getPropertyValue().getSysstatHbkno();
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER,"查詢SYSSTAT出現異常");
        }
        form.setLblBankNo(bankNo);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }
    @PostMapping(value = "/inbk/UI_019102/queryClick", produces = "application/json;charset=utf-8")
    public String queryClick(@ModelAttribute UI_019102_Form form, ModelMap mode) {
        bindFormViewData(form,mode);
        return Router.UI_019102.getView();
    }

    protected void bindFormViewData(UI_019102_Form form, ModelMap mode){
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction("查詢");
            if (StringUtils.isNotBlank(form.getLblBankNo())) {
                auditLog.addParam("銀行代號", form.getLblBankNo());
            }
            if (StringUtils.isNotBlank(form.getClrtotalStDate())) {
                auditLog.addParam("查詢日期", form.getClrtotalStDate().replace("-",StringUtils.EMPTY));
            }
            if (StringUtils.isNotBlank(form.getLblTxTime())) {
                auditLog.addParam("查詢時間", form.getLblTxTime());
            }
            if (StringUtils.isNotBlank(form.getLblBknoStan())) {
                auditLog.addParam("查詢序號", form.getLblBknoStan());
            }
            setAuditLog(auditLog);

            String stdate =  StringUtils.replace(form.getClrtotalStDate(),"-", StringUtils.EMPTY);
            String cur = "000";
            Short source = 2;
            BigDecimal totalBal;
            Clrtotal clrtotal = inbkService.getCLRTOTALByPrimaryKey(stdate,cur,source);
            if (clrtotal == null || "".equals(clrtotal.toString().trim())){
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }else {
                totalBal = clrtotal.getClrtotalRevolAmt().add(clrtotal.getClrtotalActBal());
                mode.addAttribute("totalBal", totalBal);
                mode.addAttribute("lblTxTime", clrtotal.getClrtotalTxTime());
                mode.addAttribute("lblBkno", clrtotal.getClrtotalBkno());
                mode.addAttribute("lblStan", clrtotal.getClrtotalStan());
                WebUtil.putInAttribute(mode, AttributeName.DetailEntity,clrtotal);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }
}
