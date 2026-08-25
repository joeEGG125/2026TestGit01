package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Aptot;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019202_Form;
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
import java.util.HashMap;
import java.util.List;

/**
 * 查詢清算類別檔資料(本行)
 *
 * @author xingyun_yang
 * @create 2021/9/13
 */
@Controller
public class UI_019202Controller extends BaseController {

    @Autowired
    private InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_019202_Form form = new UI_019202_Form();
        form.setClearDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        // 財金STAN
        String bankNo = StringUtils.EMPTY;
        try {
            bankNo = SysStatus.getPropertyValue().getSysstatHbkno();
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, "查詢SYSSTAT出現異常");
        }
        form.setLblBankNo(bankNo);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/inbk/UI_019202/queryClick", produces = "application/json;charset=utf-8")
    public String queryClick(@ModelAttribute UI_019202_Form form, ModelMap mode) {
        bindData(form, mode);
        return Router.UI_019202.getView();
    }

    protected void bindData(UI_019202_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction("查詢");
            if (StringUtils.isNotBlank(form.getLblBankNo())) {
                auditLog.addParam("銀行代號", form.getLblBankNo());
            }
            if (StringUtils.isNotBlank(form.getClearDate())) {
                auditLog.addParam("清算日期", form.getClearDate().replace("-",StringUtils.EMPTY));
            }
            if (StringUtils.isNotBlank(form.getApId())) {
                auditLog.addParam("跨行業務代號", form.getApId());
            }
            setAuditLog(auditLog);

            Aptot aptot = new Aptot();
            String stdate = StringUtils.replace(form.getClearDate(), "-", StringUtils.EMPTY);
            String apId = form.getApId();
            String ascFlag = "0";
            List<HashMap<String, Object>> dt = inbkService.getAPTOTSumAmtByStDateAPIDKind(stdate, apId, ascFlag);
            if (dt == null || dt.size() == 0 || dt.get(0) == null) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }else{
                aptot.setAptotCntDr(Integer.parseInt(String.valueOf(dt.get(0).get("CNT_DR"))));
                aptot.setAptotAmtDr(new BigDecimal(dt.get(0).get("AMT_DR").toString()));
                aptot.setAptotCntCr(Integer.parseInt(String.valueOf(dt.get(0).get("CNT_CR"))));
                aptot.setAptotAmtCr(new BigDecimal(dt.get(0).get("AMT_CR").toString()));
                aptot.setAptotEcCntDr(Integer.parseInt(String.valueOf(dt.get(0).get("EC_CNT_DR"))));
                aptot.setAptotEcAmtDr(new BigDecimal(dt.get(0).get("EC_AMT_DR").toString()));
                aptot.setAptotEcCntCr(Integer.parseInt(String.valueOf(dt.get(0).get("EC_CNT_CR"))));
                aptot.setAptotEcAmtCr(new BigDecimal(dt.get(0).get("EC_AMT_CR").toString()));
                aptot.setAptotFeeCntDr(Integer.parseInt(String.valueOf(dt.get(0).get("FEE_CNT_DR"))));
                aptot.setAptotFeeAmtDr(new BigDecimal(dt.get(0).get("FEE_AMT_DR").toString()));
                aptot.setAptotFeeCntCr(Integer.parseInt(String.valueOf(dt.get(0).get("FEE_CNT_CR"))));
                aptot.setAptotFeeAmtCr(new BigDecimal(dt.get(0).get("FEE_AMT_CR").toString()));
                aptot.setAptotEcFeeCntDr(Integer.parseInt(String.valueOf(dt.get(0).get("EC_FEE_CNT_DR"))));
                aptot.setAptotEcFeeAmtDr(new BigDecimal(dt.get(0).get("EC_FEE_AMT_DR").toString()));
                aptot.setAptotEcFeeCntCr(Integer.parseInt(String.valueOf(dt.get(0).get("EC_FEE_CNT_CR"))));
                aptot.setAptotEcFeeAmtCr(new BigDecimal(dt.get(0).get("EC_FEE_AMT_CR").toString()));
                WebUtil.putInAttribute(mode, AttributeName.DetailEntity, aptot);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }
}
