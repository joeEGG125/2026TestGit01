package com.syscom.fep.web.controller.inbk;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019400_Form;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * 地區分行清算日結檔
 *
 * @author xingyun_yang
 * @create 2021/9/16
 */
@Controller
public class UI_019400Controller extends BaseController {

    @Autowired
    InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_019400_Form form = new UI_019400_Form();
        form.setLblStDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/inbk/UI_019400/queryClick", produces = "application/json;charset=utf-8")
    public String queryClick(@ModelAttribute UI_019400_Form form, ModelMap mode) {
        bindData(form, mode);
        return Router.UI_019400.getView();
    }

    protected void bindData(UI_019400_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            String ddlZoneBrap = form.getDdlZoneBrap();
            String stdate = StringUtils.replace(form.getLblStDate(), "-", StringUtils.EMPTY);
            String pcode = form.getPcode();
            String apId = form.getApId();
            String brapTxType = form.getBrapTxType();
            String brapBrno = form.getBrapBrno();
            String brapDeptCode = form.getBrapDeptCode();
            String brapCur = form.getBrapCur();
            Integer size = 10;
            PageInfo<HashMap<String, Object>> dt = inbkService.getBrap(ddlZoneBrap, stdate, pcode, apId, brapTxType, brapBrno, brapDeptCode,form.getPageNum(),size, brapCur);
            if (dt == null || dt.getList().size() == 0 || dt.getList().get(0) == null) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
                PageData<UI_019400_Form,HashMap<String, Object>> pageData = new PageData<>(dt, form);
                WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
            } else {
                int i = 0;
                List<HashMap<String, Object>> dtList = new ArrayList<>(dt.getList().size());
                int tempVar = dt.getList().size();
                for (i = 0; i < tempVar; i++) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("BRAP_SEQNO", dt.getList().get(i).get("BRAP_SEQNO").toString());
                    hashMap.put("BRAP_ST_DATE", dt.getList().get(i).get("BRAP_ST_DATE").toString());
                    hashMap.put("BRAP_APID", dt.getList().get(i).get("BRAP_APID").toString());
                    hashMap.put("BRAP_BRNO", dt.getList().get(i).get("BRAP_BRNO").toString());
                    switch (dt.getList().get(i).get("BRAP_TX_TYPE").toString()){
                        case "A":
                            hashMap.put("BRAP_TX_TYPE","A-代理");
                            break;
                        case "I":
                            hashMap.put("BRAP_TX_TYPE","I-轉入");
                            break;
                        case "O":
                            hashMap.put("BRAP_TX_TYPE","O-轉出");
                            break;
                        default:
                            hashMap.put("BRAP_TX_TYPE", dt.getList().get(i).get("BRAP_TX_TYPE").toString());
                            break;
                    }
                    switch (dt.getList().get(i).get("BRAP_ACC_TYPE").toString()){
                        case "0":
                            hashMap.put("BRAP_ACC_TYPE","0-未記帳");
                            break;
                        case "1":
                            hashMap.put("BRAP_ACC_TYPE","1-已記帳");
                            break;
                        case "2":
                            hashMap.put("BRAP_ACC_TYPE","2-已更正");
                            break;
                        case "3":
                            hashMap.put("BRAP_ACC_TYPE","3-更正/入帳失敗");
                            break;
                        case "4":
                            hashMap.put("BRAP_ACC_TYPE","4-未明");
                            break;
                        case "5":
                            hashMap.put("BRAP_ACC_TYPE","5-待解");
                            break;
                        default:
                            hashMap.put("BRAP_ACC_TYPE", dt.getList().get(i).get("BRAP_ACC_TYPE").toString());
                            break;
                    }
                    switch (dt.getList().get(i).get("BRAP_MONTHLY_FLAG").toString()){
                        case "1":
                            hashMap.put("BRAP_MONTHLY_FLAG","是");
                            break;
                        default:
                            hashMap.put("BRAP_MONTHLY_FLAG","否");
                            break;
                    }
                    hashMap.put("BRAP_PCODE", dt.getList().get(i).get("BRAP_PCODE").toString());
                    hashMap.put("BRAP_ZONE_CODE", dt.getList().get(i).get("BRAP_ZONE_CODE").toString());
                    hashMap.put("BRAP_CUR", dt.getList().get(i).get("BRAP_CUR").toString());
                    hashMap.put("BRAP_TX_CNT_DR", dt.getList().get(i).get("BRAP_TX_CNT_DR").toString());
                    hashMap.put("BRAP_TX_AMT_DR", dt.getList().get(i).get("BRAP_TX_AMT_DR").toString());
                    hashMap.put("BRAP_TX_CNT_CR", dt.getList().get(i).get("BRAP_TX_CNT_CR").toString());
                    hashMap.put("BRAP_TX_AMT_CR", dt.getList().get(i).get("BRAP_TX_AMT_CR").toString());
                    hashMap.put("BRAP_FEE_CUSTPAY", dt.getList().get(i).get("BRAP_FEE_CUSTPAY").toString());
                    hashMap.put("BRAP_MBANK_CNT_DR", dt.getList().get(i).get("BRAP_MBANK_CNT_DR").toString());
                    hashMap.put("BRAP_MBANK_FEE_DR", dt.getList().get(i).get("BRAP_MBANK_FEE_DR").toString());
                    hashMap.put("BRAP_MBANK_CNT_CR", dt.getList().get(i).get("BRAP_MBANK_CNT_CR").toString());
                    hashMap.put("BRAP_MBANK_FEE_CR", dt.getList().get(i).get("BRAP_MBANK_FEE_CR").toString());
                    hashMap.put("BRAP_FISC_CNT_DR", dt.getList().get(i).get("BRAP_FISC_CNT_DR").toString());
                    hashMap.put("BRAP_FISC_FEE_DR", dt.getList().get(i).get("BRAP_FISC_FEE_DR").toString());
                    hashMap.put("BRAP_FISC_CNT_CR", dt.getList().get(i).get("BRAP_FISC_CNT_CR").toString());
                    hashMap.put("BRAP_FISC_FEE_CR", dt.getList().get(i).get("BRAP_FISC_FEE_CR").toString());
                    hashMap.put("BRAP_PROFIT_CNT", dt.getList().get(i).get("BRAP_PROFIT_CNT").toString());
                    hashMap.put("BRAP_PROFIT_AMT", dt.getList().get(i).get("BRAP_PROFIT_AMT").toString());
                    hashMap.put("BRAP_LOSS_CNT", dt.getList().get(i).get("BRAP_LOSS_CNT").toString());
                    hashMap.put("BRAP_LOSS_AMT", dt.getList().get(i).get("BRAP_LOSS_AMT").toString());
                    hashMap.put("BRAP_DEPT_CODE", dt.getList().get(i).get("BRAP_DEPT_CODE").toString());
                    hashMap.put("BRAP_PBTYPE", dt.getList().get(i).get("BRAP_PBTYPE").toString());
                    hashMap.put("BRAP_MOD_TX_CNT_DR", dt.getList().get(i).get("BRAP_MOD_TX_CNT_DR").toString());
                    hashMap.put("BRAP_MOD_TX_AMT_DR", dt.getList().get(i).get("BRAP_MOD_TX_AMT_DR").toString());
                    hashMap.put("BRAP_MOD_CNT_CUSTPAY", dt.getList().get(i).get("BRAP_MOD_CNT_CUSTPAY").toString());
                    hashMap.put("BRAP_MOD_FEE_CUSTPAY", dt.getList().get(i).get("BRAP_MOD_FEE_CUSTPAY").toString());
                    hashMap.put("BRAP_MOD_PROFIT_CNT", dt.getList().get(i).get("BRAP_MOD_PROFIT_CNT").toString());
                    hashMap.put("BRAP_MOD_PROFIT_AMT", dt.getList().get(i).get("BRAP_MOD_PROFIT_AMT").toString());
                    hashMap.put("BRAP_MOD_TX_CNT_CR", dt.getList().get(i).get("BRAP_MOD_TX_CNT_CR").toString());
                    hashMap.put("BRAP_MOD_TX_AMT_CR", dt.getList().get(i).get("BRAP_MOD_TX_AMT_CR").toString());
                    hashMap.put("BRAP_MOD_FISC_CNT_CR", dt.getList().get(i).get("BRAP_MOD_FISC_CNT_CR").toString());
                    hashMap.put("BRAP_MOD_FISC_FEE_CR", dt.getList().get(i).get("BRAP_MOD_FISC_FEE_CR").toString());
                    hashMap.put("BRAP_MOD_LOSS_CNT", dt.getList().get(i).get("BRAP_MOD_LOSS_CNT").toString());
                    hashMap.put("BRAP_MOD_LOSS_AMT", dt.getList().get(i).get("BRAP_MOD_LOSS_AMT").toString());
                    dtList.add(hashMap);
                }
                PageData<UI_019400_Form,HashMap<String, Object>> pageData = new PageData<>(dt, form);
                WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }
}
