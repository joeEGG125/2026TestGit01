package com.syscom.fep.web.controller.inbk;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.form.inbk.UI_019530_Form;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.mybatis.vo.SyscomGroupVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalTime;
import java.util.*;

/**
 * 資金調撥Pending交易確認及回覆
 *
 * @author LeYun
 * @create 2026/6/17
 */
@Controller
public class UI_019530Controller extends BaseController {
    @Autowired
    InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_019530_Form form = new UI_019530_Form();
        // UI_營業日預設營業日
        form.setFeptxnTbsdyFisc(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);

        LocalTime cutoffTime = LocalTime.of(16, 50); // 設定基準時間 16:50
        form.setBefore1650(LocalTime.now().isBefore(cutoffTime));
        form.setHasFepEbmk(false);
        form.setHasFepEbck(false);
        // 取得當前使用者群組
        List<SyscomGroupVo> syscomGroupVoList = WebUtil.getFromSession(SessionKey.Group);
        if (syscomGroupVoList != null) {
            for(SyscomGroupVo syscomGroupVo :syscomGroupVoList){
                if ("FEP_EBMK".equals(syscomGroupVo.getRoleNo())){
                    form.setHasFepEbmk(true);
                }
                if ("FEP_EBCK".equals(syscomGroupVo.getRoleNo())) {
                    form.setHasFepEbck(true);
                }
            }
        }
    }

    @PostMapping(value = "/inbk/UI_019530/queryClick", produces = "application/json;charset=utf-8")
    public String queryClick(@ModelAttribute UI_019530_Form form, ModelMap mode) {
        bindData(form, mode);
        return Router.UI_019530.getView();
    }

    protected void bindData(UI_019530_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);

        LocalTime cutoffTime = LocalTime.of(16, 50); // 設定基準時間 16:50
        form.setBefore1650(LocalTime.now().isBefore(cutoffTime));
        form.setHasFepEbmk(false);
        form.setHasFepEbck(false);
        // 取得當前使用者群組
        List<SyscomGroupVo> syscomGroupVoList = WebUtil.getFromSession(SessionKey.Group);
        if (syscomGroupVoList != null) {
            for(SyscomGroupVo syscomGroupVo :syscomGroupVoList){
                if ("FEP_EBMK".equals(syscomGroupVo.getRoleNo())){
                    form.setHasFepEbmk(true);
                }
                if ("FEP_EBCK".equals(syscomGroupVo.getRoleNo())) {
                    form.setHasFepEbck(true);
                }
            }
        }
        try {
            // 檢核輸入資料
            String feptxnTbsdyFisc = form.getFeptxnTbsdyFisc();
            if (StringUtils.isBlank(feptxnTbsdyFisc)) {
                this.showMessage(mode, MessageType.WARNING, "欄位必須輸入資料");
                return;
            }

            String queryDate = StringUtils.replace(feptxnTbsdyFisc, "-", StringUtils.EMPTY);
            String transSelect = form.getTransSelect();
            Integer size = form.getPageSize() == null ? 10 : form.getPageSize();

            //查詢
            PageInfo<HashMap<String, Object>> dt = inbkService.query2700TxnResult(queryDate, transSelect, form.getPageNum(), size, "019530");

            if (dt == null || dt.getList().size() == 0 || dt.getList().get(0) == null) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
                PageData<UI_019530_Form, HashMap<String, Object>> pageData = new PageData<>(dt, form);
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
                    hashMap.put("UI_CON_RC", dt.getList().get(i).get("FEPTXN_CON_EXCP_CODE") == null ? "" : dt.getList().get(i).get("FEPTXN_CON_EXCP_CODE").toString());
                    hashMap.put("UI_FXML_MEMO", dt.getList().get(i).get("FEPTXN_FXML_MEMO") == null ? "" : dt.getList().get(i).get("FEPTXN_FXML_MEMO").toString());

                    dtList.add(hashMap);
                }

                // 將組合好的清單放回 dt ，綁定到 PageData 傳遞給前端
                dt.setList(dtList);
                PageData<UI_019530_Form, HashMap<String, Object>> pageData = new PageData<>(dt, form);
                WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    /**
     * 處理前端按鈕 AJAX 呼叫
     */
    @PostMapping(value = "/inbk/UI_019530/processAction", produces = "application/json;charset=utf-8")
    @ResponseBody
    public Map<String, Object> processAction(@RequestParam("action") String action, @RequestParam("seqNo") String seqNo, @RequestParam("txTime") String txTime) {
        Map<String, Object> response = new HashMap<>();
        try {
            this.infoMessage("執行 UI_019530 動作, action = [", action, "], seqNo = [", seqNo, "]");

            if (StringUtils.isBlank(seqNo) || seqNo.length() < 10) {
                throw new IllegalArgumentException("交易序號格式錯誤");
            }
            if (StringUtils.isBlank(txTime) || txTime.length() < 8) {
                throw new IllegalArgumentException("交易時間格式錯誤");
            }

            String bkno = StringUtils.left(seqNo, 3);
            String stan = StringUtils.right(seqNo, 7);
            String txDate = StringUtils.left(txTime, 8);

            String resultMsg = inbkService.process019530Action(txDate, bkno, stan, action);
            boolean isSuccess = StringUtils.isBlank(resultMsg) ||
                    resultMsg.contains("已記錄交易成功") ||
                    resultMsg.contains("已記錄交易失敗") ||
                    resultMsg.contains("收款覆核，交易已完成") ||
                    resultMsg.contains("扣款覆核，交易已完成");

            if (isSuccess) {
                response.put("success", true);
                response.put("message", "處理成功");
            } else {
                response.put("success", false);
                response.put("message", resultMsg);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
