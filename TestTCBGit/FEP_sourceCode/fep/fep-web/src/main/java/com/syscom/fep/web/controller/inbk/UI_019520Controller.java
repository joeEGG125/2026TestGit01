package com.syscom.fep.web.controller.inbk;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019520_Form;
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
 * 查詢2700付款(代理)交易處理結果
 *
 * @author LeYun
 * @create 2026/6/17
 */
@Controller
public class UI_019520Controller extends BaseController {
    @Autowired
    InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_019520_Form form = new UI_019520_Form();
        // UI_營業日預設營業日
        form.setFeptxnTbsdyFisc(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/inbk/UI_019520/queryClick", produces = "application/json;charset=utf-8")
    public String queryClick(@ModelAttribute UI_019520_Form form, ModelMap mode) {
        bindData(form, mode);
        return Router.UI_019520.getView();
    }

    protected void bindData(UI_019520_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            // 檢核輸入資料
            String feptxnTbsdyFisc = form.getFeptxnTbsdyFisc();
            if (StringUtils.isBlank(feptxnTbsdyFisc)) {
                this.showMessage(mode, MessageType.WARNING, "欄位必須輸入資料");
                return;
            }

            String queryDate = StringUtils.replace(feptxnTbsdyFisc, "-", StringUtils.EMPTY);
            String brapSelect = form.getBrapSelect();
            Integer size = form.getPageSize() == null ? 10 : form.getPageSize();

            //查詢 (付款/代理)
            PageInfo<HashMap<String, Object>> dt = inbkService.query2700TxnResult(queryDate, brapSelect, form.getPageNum(), size, "019520");

            if (dt == null || dt.getList().size() == 0 || dt.getList().get(0) == null) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
                PageData<UI_019520_Form, HashMap<String, Object>> pageData = new PageData<>(dt, form);
                WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
            } else {
                int i = 0;
                List<HashMap<String, Object>> dtList = new ArrayList<>(dt.getList().size());
                int tempVar = dt.getList().size();
                for (i = 0; i < tempVar; i++) {
                    HashMap<String, Object> hashMap = new HashMap<>();

                    String txDate = dt.getList().get(i).get("FEPTXN_TX_DATE") == null ? "" : dt.getList().get(i).get("FEPTXN_TX_DATE").toString();
                    String txTime = dt.getList().get(i).get("FEPTXN_TX_TIME") == null ? "" : dt.getList().get(i).get("FEPTXN_TX_TIME").toString();
                    hashMap.put("UI_TX_TIME", txDate + " " + txTime);

                    hashMap.put("UI_TBSDY_FISC", dt.getList().get(i).get("FEPTXN_TBSDY_FISC") == null ? "" : dt.getList().get(i).get("FEPTXN_TBSDY_FISC").toString());

                    String bkno = dt.getList().get(i).get("FEPTXN_BKNO") == null ? "" : dt.getList().get(i).get("FEPTXN_BKNO").toString();
                    String stan = dt.getList().get(i).get("FEPTXN_STAN") == null ? "" : dt.getList().get(i).get("FEPTXN_STAN").toString();
                    hashMap.put("UI_TX_SEQ", bkno + "-" + stan);

                    hashMap.put("UI_FROM_NAME", dt.getList().get(i).get("FEPTXN_FROM_NAME") == null ? "" : dt.getList().get(i).get("FEPTXN_FROM_NAME").toString());
                    hashMap.put("UI_TO_NAME", dt.getList().get(i).get("FEPTXN_TO_NAME") == null ? "" : dt.getList().get(i).get("FEPTXN_TO_NAME").toString());
                    hashMap.put("UI_TX_AMT", dt.getList().get(i).get("FEPTXN_TX_AMT") == null ? "" : dt.getList().get(i).get("FEPTXN_TX_AMT").toString());

                    String troutBkno = dt.getList().get(i).get("FEPTXN_TROUT_BKNO") == null ? "" : dt.getList().get(i).get("FEPTXN_TROUT_BKNO").toString();
                    String troutActno = dt.getList().get(i).get("FEPTXN_TROUT_ACTNO") == null ? "" : dt.getList().get(i).get("FEPTXN_TROUT_ACTNO").toString();
                    hashMap.put("UI_TROUT_ACTNO", troutBkno + "-" + troutActno);

                    String trinBkno = dt.getList().get(i).get("FEPTXN_TRIN_BKNO") == null ? "" : dt.getList().get(i).get("FEPTXN_TRIN_BKNO").toString();
                    String trinActno = dt.getList().get(i).get("FEPTXN_TRIN_ACTNO") == null ? "" : dt.getList().get(i).get("FEPTXN_TRIN_ACTNO").toString();
                    hashMap.put("UI_TRIN_ACTNO", trinBkno + "-" + trinActno);

                    hashMap.put("UI_TRIN_ACTNO_ACTUAL", dt.getList().get(i).get("FEPTXN_TRIN_ACTNO_ACTUAL") == null ? "" : dt.getList().get(i).get("FEPTXN_TRIN_ACTNO_ACTUAL").toString());
                    hashMap.put("UI_CBS_RC", dt.getList().get(i).get("FEPTXN_CBS_RC") == null ? "" : dt.getList().get(i).get("FEPTXN_CBS_RC").toString());
                    hashMap.put("UI_CON_RC", dt.getList().get(i).get("FEPTXN_CON_RC") == null ? "" : dt.getList().get(i).get("FEPTXN_CON_RC").toString());
                    hashMap.put("UI_FXML_MEMO", dt.getList().get(i).get("FEPTXN_FXML_MEMO") == null ? "" : dt.getList().get(i).get("FEPTXN_FXML_MEMO").toString());

                    dtList.add(hashMap);
                }

                // 將組合好的清單放回 dt ，綁定到 PageData 傳遞給前端
                dt.setList(dtList);
                PageData<UI_019520_Form, HashMap<String, Object>> pageData = new PageData<>(dt, form);
                WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }
}
