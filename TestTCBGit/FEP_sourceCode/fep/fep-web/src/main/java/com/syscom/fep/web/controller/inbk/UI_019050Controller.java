package com.syscom.fep.web.controller.inbk;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ems.model.Feplog;
import com.syscom.fep.mybatis.model.Feptxntcb;
import com.syscom.fep.mybatis.model.Inbk2160;
import com.syscom.fep.mybatis.model.Npsbatch;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.atmmon.UI_060550_FormDetail;
import com.syscom.fep.web.form.inbk.UI_019050_Form;
import com.syscom.fep.web.form.inbk.UI_019050_FormDetail;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.EmsService;
import com.syscom.fep.web.service.HceService;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.util.*;

/**
 * 查詢全國繳費整批轉即時交易結果
 *
 * @author xingyun_yang
 * @create 2021/8/25
 */
@Controller
public class UI_019050Controller extends BaseController {

    @Autowired
    private InbkService inbkService;
    @Autowired
    private EmsService emsSvr;
    @Autowired
    private AtmService atmService;
    @Autowired
    private HceService hceService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_019050_Form form = new UI_019050_Form();
        // 營業日期
        form.setTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }
    @PostMapping(value = "/inbk/UI_019050/doInquiryDetail")
    public String doInquiryDetail(@ModelAttribute UI_019050_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            // 傳送檔名
            String fileid = form.getFileid();
            //扣賬日期
            String txdate = form.getTxdate().replace("-", "");
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("查詢");
            // 塞入auditLog.params
            if (StringUtils.isNotBlank(String.valueOf(form.getFileid())))
                auditLog.addParam("傳送檔名", String.valueOf(form.getFileid()));
            if (StringUtils.isNotBlank(form.getTxdate()))
                auditLog.addParam("扣帳日期", form.getTxdate());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            PageInfo<Npsbatch> pageInfo = inbkService.queryNPSBATCH(fileid,txdate,form.getPageNum(),form.getPageSize());
            PageData<UI_019050_Form, Npsbatch> pageData = new PageData<>(pageInfo, form);
            if(pageInfo.getSize() == 0){
                this.showMessage(mode, MessageType.INFO,QueryNoData);
            } else {
            	  int tempVar = pageData.getList().size();
                  for (int i = 0; i < tempVar; i++) {
                	  Npsbatch data = pageData.getList().get(i);
                	  String npsbatchResult = data.getNpsbatchResult();
						if (StringUtils.isBlank(npsbatchResult)) { // 無值顯示”處理中”
							data.setNpsbatchResult("處理中");
						} else if ("00".equals(npsbatchResult)) { // “00”顯示”交易發送完成"
							data.setNpsbatchResult("交易發送完成");
						} else if ("02".equals(npsbatchResult)) { // “02”顯示”已回結果檔"
							data.setNpsbatchResult("已回結果檔");
						} else { // “01”顯示”整批剔退”
							data.setNpsbatchResult("整批剔退");
						}
                  }
            }
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_019050.getView();
    }

    @PostMapping(value = "/inbk/UI_019050/ShowDetail")
    public String ShowDetail(@ModelAttribute UI_019050_FormDetail form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
        	 // 傳送檔名
            String fileid = form.getFileid().substring(0,8);
            //扣賬日期
            String txdate = form.getTxdate().replace("-", "").toString().trim();
            //批號
            String npsbatchBatchNo = form.getNpsbatchBatchNo().toString().trim();
            //batno 序號
            String batno = fileid+txdate+npsbatchBatchNo;
            mode.addAttribute("fileid",fileid);
            mode.addAttribute("txdate",txdate);
            mode.addAttribute("npsbatchBatchNo",npsbatchBatchNo);
            PageInfo<HashMap<String,Object>> pageInfo = inbkService.showDetail(batno,form.getPageNum(),form.getPageSize());
            if(pageInfo.getSize() == 0){
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            PageData<UI_019050_FormDetail,HashMap<String,Object>> pageData = new PageData<>(pageInfo, form);
            List<HashMap<String,Object>> nbsdtlExtHashs= new ArrayList<>(pageData.getList().size());
            if (pageData.getList().size() > 0) {
                int tempVar = pageData.getList().size();
                for (int i = 0; i < tempVar; i++) {
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("NPSDTL_SEQ_NO",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_SEQ_NO")));
                    hashMap.put("NPSDTL_TROUT_BKNO",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_TROUT_BKNO")));
                    hashMap.put("NPSDTL_TROUT_ACTNO", nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_TROUT_ACTNO")));
                    hashMap.put("NPSDTL_TRIN_BKNO",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_TRIN_BKNO")));
                    hashMap.put("NPSDTL_TRIN_ACTNO",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_TRIN_ACTNO")));
                    hashMap.put("NPSDTL_TX_AMT",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_TX_AMT")));
                    hashMap.put("NPSDTL_BUSINESS_UNIT",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_BUSINESS_UNIT")));
                    hashMap.put("NPSDTL_PAYTYPE",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_PAYTYPE")));
                    hashMap.put("NPSDTL_PAYNO",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_PAYNO")));
                    hashMap.put("NPSDTL_RECON_SEQ",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_RECON_SEQ")));
                    hashMap.put("NPSDTL_STAN",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_STAN")));
                    hashMap.put("NPSDTL_EJFNO",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_EJFNO")));
                    hashMap.put("NPSDTL_TBSDY",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_TBSDY")));
					HashMap<String, Object> data = pageData.getList().get(i);
					String npsdtlResult = Objects.toString(data.get("NPSDTL_RESULT"));
					if (StringUtils.isBlank(npsdtlResult)) { // 無值:處理中
						hashMap.put("NPSDTL_RESULT", "處理中");
					} else if ("00".equals(npsdtlResult)) { // 00:交易成功
						hashMap.put("NPSDTL_RESULT", "交易成功");
					} else if ("11".equals(npsdtlResult)) { // 11:停止交易
						hashMap.put("NPSDTL_RESULT", "停止交易");
					} else { // 01:交易失敗，
						hashMap.put("NPSDTL_RESULT", "交易失敗");
					}
                    hashMap.put("NPSDTL_REPLY_CODE", nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_REPLY_CODE")));
                    hashMap.put("NPSDTL_ERR_MSG", nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_ERR_MSG")));
                    nbsdtlExtHashs.add(hashMap);
                }
            }
            pageData.setList(nbsdtlExtHashs);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_019050_Detail.getView();
    }
    @PostMapping(value = "/inbk/UI_019050/inquiryFeplogList")
    public String doInquiryFeplog(@ModelAttribute UI_019050_FormDetail form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        return doInquiryFeplogForOtherSubSys(form, mode);
    }
    private String doInquiryFeplogForOtherSubSys(UI_019050_FormDetail form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            // 傳送檔名
            String fileid = form.getFileid().substring(0,8);
            //扣賬日期
            String txdate = form.getTxdate().replace("/", "").toString().trim();
            //批號
            String npsbatchBatchNo = form.getNpsbatchBatchNo().toString().trim();
            //batno 序號
            String batno = fileid+txdate+npsbatchBatchNo;
            mode.addAttribute("fileid",fileid);
            mode.addAttribute("txdate",txdate);
            mode.addAttribute("npsbatchBatchNo",npsbatchBatchNo);
            PageInfo<Feplog> pageInfo = emsSvr.getFeplog_UI019050(fileid,txdate,npsbatchBatchNo,form.getPageNum(),form.getPageSize());
            if(pageInfo.getSize() == 0){
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            if (pageInfo.getSize() == 0) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            PageData<UI_019050_FormDetail, Feplog> pageData = new PageData(pageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_019050_A.getView();
    }
    //2026.1.27 LeYun 修正'紀錄'按鈕無法連結問題
    @PostMapping(value = "/inbk/UI_019050/inquiryFeptxnList")
    public String inquiryFeptxnList(UI_019050_FormDetail form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            String tableNameSuffix = StringUtils.EMPTY;
            Integer ejfno = form.getEjfno();

            // 取得主檔資料
            Map<String, Object> detail = atmService.getFeptxnIntltxnFor019050(tableNameSuffix, ejfno);
            if (MapUtils.isEmpty(detail)) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
                return Router.UI_019050.getView();
            }

            // 實例化 UI060550 Form
            UI_060550_FormDetail targetForm = new UI_060550_FormDetail();

            // detail 後續查詢必要欄位
            String feptxnTxDate = (String) detail.get("FEPTXN_TX_DATE");
            Integer feptxnEjfno = (Integer) detail.get("FEPTXN_EJFNO");
            String feptxnCbsProc = (String) detail.getOrDefault("FEPTXN_CBS_PROC", null);
            String idno = (String) detail.get("FEPTXN_IDNO");
            String txCode = (String) detail.get("FEPTXN_TX_CODE");

            // 補查詢 Feptxntcb
            Feptxntcb feptxntcb = null;
            if (StringUtils.equals(feptxnCbsProc, "N")) {
                feptxntcb = atmService.getFeptxntcb(feptxnTxDate, feptxnEjfno);
            }
            targetForm.setFeptxntcb(feptxntcb);

            // 補查詢普發紀錄
            if (MapUtils.isNotEmpty(detail)) {
                String healthId = null;
                String activityCode = null;
                if (feptxntcb != null) {
                    healthId = feptxntcb.getFeptxntcbHealthcard();
                    activityCode = feptxntcb.getFeptxntcbFacode();
                }

                if (txCode != null && ((txCode.startsWith("LF") && "TT".equals(activityCode)) || txCode.startsWith("WP"))) {
                    String stan = (String) detail.get("FEPTXN_STAN");
                    Map<String, Object> ucdidData = atmService.getUcdidDataByLogic(idno, healthId, txCode, feptxnTxDate, stan);
                    if (MapUtils.isNotEmpty(ucdidData)) {
                        detail.putAll(ucdidData);
                    }
                }
            }

            // 補查詢 Inbk2160
            Inbk2160 inbk2160 = null;
            inbk2160 = hceService.getINBK2160ByFeptxn(feptxnTxDate, feptxnEjfno);
            targetForm.setInbk2160(inbk2160); // 塞入 Form

            // 處理匯率顯示(同060550)
            BigDecimal exrate = (BigDecimal) detail.get("INTLTXN_EXRATE");
            if (exrate == null || exrate.compareTo(BigDecimal.ZERO) == 0) {
                detail.put("INTLTXN_EXRATE", StringUtils.EMPTY);
            }

            // 應該只會有一筆資料
            WebUtil.putInAttribute(mode, AttributeName.DetailMap, detail);
            mode.addAttribute("form", targetForm);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        // 2026.2.10 跳轉到 UI060550 FEPTXN交易查詢
        return Router.UI_060550_Detail.getView();
    }

}
