package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.mapper.BrapExtMapper;
import com.syscom.fep.mybatis.model.Brap;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019302_Form;
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
 * 查詢分行清算日結檔
 *
 * @author xingyun_yang
 * @create 2021/9/13
 */
@Controller
public class UI_019302Controller extends BaseController {

    @Autowired
    BrapExtMapper brapExtMapper;

    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_019302_Form form = new UI_019302_Form();
        form.setClearDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
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
    @PostMapping(value = "/inbk/UI_019302/queryClick", produces = "application/json;charset=utf-8")
    public String queryClick(@ModelAttribute UI_019302_Form form, ModelMap mode) {
        bindData(form, mode);
        return Router.UI_019302.getView();
    }

    protected void bindData(UI_019302_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction("查詢");
            if (StringUtils.isNotBlank(form.getLblBankNo())) {
                auditLog.addParam("銀行代號", form.getLblBankNo());
            }
            if (StringUtils.isNotBlank(form.getClearDate())) {
                auditLog.addParam("清算日期", StringUtils.replace(form.getClearDate(), "-", StringUtils.EMPTY));
            }
            if (StringUtils.isNotBlank(form.getApId())) {
                auditLog.addParam("跨行業務代號", form.getApId());
            }
            setAuditLog(auditLog);

            Brap brap = new Brap();
            String stdate = StringUtils.replace(form.getClearDate(), "-", StringUtils.EMPTY);
            String apId = form.getApId();
            if (!"*".equals(apId.substring(3,4))){
                apId=apId;
            }else {
                apId=apId.substring(0,3);
            }
            List<HashMap<String, Object>> dt = brapExtMapper.getBRAPSumAmtByStDateAPIDKind(stdate,apId);
            if (dt == null || dt.size() == 0 || dt.get(0) == null) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }else{
                brap.setBrapTxCntDr(Integer.parseInt(dt.get(0).get("CNT_DR").toString()));
                brap.setBrapTxAmtDr(new BigDecimal(dt.get(0).get("AMT_DR").toString()));
                brap.setBrapTxCntCr(Integer.parseInt(dt.get(0).get("CNT_CR").toString()));
                brap.setBrapTxAmtCr(new BigDecimal(dt.get(0).get("AMT_CR").toString()));
                brap.setBrapModTxCntDr(Integer.parseInt(dt.get(0).get("TX_CNT_DR").toString()));
                brap.setBrapModTxAmtDr(new BigDecimal(dt.get(0).get("TX_AMT_DR").toString()));
                brap.setBrapModTxCntCr(Integer.parseInt(dt.get(0).get("TX_CNT_CR").toString()));
                brap.setBrapModTxAmtCr(new BigDecimal(dt.get(0).get("TX_AMT_CR").toString()));
                brap.setBrapProfitCnt(Integer.parseInt(dt.get(0).get("PROFIT_CNT").toString()));
                brap.setBrapProfitAmt(new BigDecimal(dt.get(0).get("PROFIT_AMT").toString()));
                brap.setBrapLossCnt(Integer.parseInt(dt.get(0).get("LOSS_CNT").toString()));
                brap.setBrapLossAmt(new BigDecimal(dt.get(0).get("LOSS_AMT").toString()));
                brap.setBrapModProfitCnt(Integer.parseInt(dt.get(0).get("MOD_PROFIT_CNT").toString()));
                brap.setBrapModProfitAmt(new BigDecimal(dt.get(0).get("MOD_PROFIT_AMT").toString()));
                brap.setBrapModLossCnt(Integer.parseInt(dt.get(0).get("MOD_LOSS_CNT").toString()));
                brap.setBrapModLossAmt(new BigDecimal(dt.get(0).get("MOD_LOSS_AMT").toString()));
                WebUtil.putInAttribute(mode, AttributeName.DetailEntity, brap);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }
}
