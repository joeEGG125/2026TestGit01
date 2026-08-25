package com.syscom.fep.web.controller.atmmon;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ems.model.Feplog;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.base.FEPWebBase;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.entity.SQLSortExpression.SQLSortOrder;
import com.syscom.fep.web.form.atmmon.*;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.ChannelService;
import com.syscom.fep.web.service.EmsService;
import com.syscom.fep.web.service.HceService;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.mybatis.vo.SyscomGroupVo;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 交易日誌(FEPTXN)查詢
 *
 * @author Richard
 */
@Controller
public class UI_060550Controller extends BaseController {
    @Autowired
    private AtmService atmService;
    @Autowired
    private EmsService emsSvr;
    @Autowired
    private HceService hceService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private SysstatExtMapper sysstatExtMapper;
    private final String pleaseChoose = "所有";

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_060550_FormMain form = new UI_060550_FormMain();
        form.setFeptxnTbsdyFisc(this.getTbsdy(mode));
        form.setFeptxnExcludeTxCode("5202;3113");// 調整預設排除查詢的PCODE
        form.setFeptxnTxDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        try {
            this.setChannelOptions(mode);//通道下拉選單
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        form.setHasFepService(true);
        // 取得當前使用者群組
        List<SyscomGroupVo> syscomGroupVoList = WebUtil.getFromSession(SessionKey.Group);
        if (syscomGroupVoList != null) {
            for(SyscomGroupVo syscomGroupVo :syscomGroupVoList){
                if ("FEP_Service".equals(syscomGroupVo.getRoleNo()) || "FEP_Monitor".equals(syscomGroupVo.getRoleNo())){
                    form.setHasFepService(false);
                }
            }
        }
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    private String getTbsdy(ModelMap mode) {
        try {
//            Sysstat sysstat = atmService.getStatus();
            Sysstat sysstat = sysstatExtMapper.selectByPrimaryKey("006");
            if (sysstat != null) {
                String sysstatTbsdyFisc = sysstat.getSysstatTbsdyFisc();
                if (StringUtils.isNotBlank(sysstatTbsdyFisc)) {
                    return charDateToDate(sysstatTbsdyFisc, "-");
                }
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, "財金營業日", DATA_INQUIRY_EXCEPTION_OCCUR);
        }
        return StringUtils.EMPTY;
    }
    /**
     * 通道下拉選單
     *
     * @param mode
     * @throws Exception
     */
    private void setChannelOptions(ModelMap mode) throws Exception {
        List<Channel> channelList = channelService.queryAllDataAsc();//調整成要顯示的才取出
        List<SelectOption<String>> selectOptionList = new ArrayList<>();
        selectOptionList.add(new SelectOption<String>(this.pleaseChoose, StringUtils.EMPTY));
        selectOptionList.add(new SelectOption<String>("FISC", "FISC"));
        for (int i = 0; i < channelList.size(); i++) {
            selectOptionList.add(new SelectOption<String>(channelList.get(i).getChannelName(), channelList.get(i).getChannelNameS()));
        }
        WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList);
    }

    @PostMapping(value = "/atmmon/UI_060550/inquiryMain")
    public String doInquiryMain(@ModelAttribute UI_060550_FormMain form, ModelMap mode) {
        this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
        form.setHasFepService(true);
        // 取得當前使用者群組
        List<SyscomGroupVo> syscomGroupVoList = WebUtil.getFromSession(SessionKey.Group);
        if (syscomGroupVoList != null) {
            for(SyscomGroupVo syscomGroupVo :syscomGroupVoList){
                if ("FEP_Service".equals(syscomGroupVo.getRoleNo()) || "FEP_Monitor".equals(syscomGroupVo.getRoleNo())){
                    form.setHasFepService(false);
                }
            }
        }
        this.doKeepFormData(mode, form);
        try {
            if (this.doValidateForm(form, mode)) {
                // 首次按下查詢時預設的排序
                if (form.getSqlSortExpressionCount() == 0) {
                    form.addSqlSortExpression("FEPTXN_TX_DATE,FEPTXN_TX_TIME", SQLSortOrder.ASC);
                }
                // 轉成map對象供最後mybatis查詢資料使用
                Map<String, Object> argsMap = this.prepareArgsMap(form);
                // 記錄auditLog
                AuditLog auditLog = new AuditLog();
                // 塞入auditLog.action
                auditLog.setAction("查詢");
                // 塞入auditLog.params
                auditLog.addParam("財金營業日", (String) argsMap.get("feptxnTbsdyFisc"));
                if (StringUtils.isNotBlank((String) argsMap.get("feptxnTxTimeBegin")) && StringUtils.isNotBlank((String) argsMap.get("feptxnTxTimeEnd")))
                    auditLog.addParam("交易時間起訖", argsMap.get("feptxnTxTimeBegin") + "~" + argsMap.get("feptxnTxTimeEnd"));
                if (StringUtils.isNotBlank(form.getFeptxnTxDate()))
                    auditLog.addParam("交易日期", form.getFeptxnTxDate());
                if (StringUtils.isNotBlank(form.getFeptxnTxCurAct()))
                    auditLog.addParam("提領幣別", form.getFeptxnTxCurAct());
                if (StringUtils.isNotBlank(form.getFeptxnTxAmt()))
                    auditLog.addParam("交易金額", form.getFeptxnTxAmt());
                if (StringUtils.isNotBlank(form.getFeptxnTroutBkno()) || StringUtils.isNotBlank(form.getFeptxnTroutActno()))
                    auditLog.addParam("扣款帳號", form.getFeptxnTroutBkno() + form.getFeptxnTroutActno());
                if (StringUtils.isNotBlank(form.getFeptxnTrinBkno()) || StringUtils.isNotBlank(form.getFeptxnTrinActno()))
                    auditLog.addParam("轉入帳號", form.getFeptxnTrinBkno() + form.getFeptxnTrinActno());
                if ("0".equals(argsMap.get("fiscFlag")))
                    auditLog.addParam("自行or跨行", "自行");
                else if ("1".equals(argsMap.get("fiscFlag")))
                    auditLog.addParam("自行or跨行", "跨行");
                if (StringUtils.isNotBlank(form.getFeptxnExcludeTxCode()))
                    auditLog.addParam("排除交易別", form.getFeptxnExcludeTxCode());
                if (StringUtils.isNotBlank(form.getFeptxnTxrust()))
                    auditLog.addParam("交易結果", form.getFeptxnTxrust());
                if (StringUtils.isNotBlank(form.getFeptxnAtmno()))
                    auditLog.addParam("ATM代號", form.getFeptxnAtmno());
                if (StringUtils.isNotBlank(form.getFeptxnCbsRrn()))
                    auditLog.addParam("發卡區T24序號", form.getFeptxnCbsRrn());
                // if (StringUtils.isNotBlank(form.getFeptxnVirCbsRrn()))
                //      auditLog.addParam("交易區T24序號", form.getFeptxnVirCbsRrn());
                if (StringUtils.isNotBlank(form.getFeptxnAtmSeqno()))
                    auditLog.addParam("ATM交易序號", form.getFeptxnAtmSeqno());
                if (StringUtils.isNotBlank(form.getFeptxnConAtmSeqno2()))
                    auditLog.addParam("ATM CON交易序號", form.getFeptxnConAtmSeqno2());
                if (StringUtils.isNotBlank(form.getFeptxnEjfno()))
                    auditLog.addParam("EJ序號", form.getFeptxnEjfno());
                if (StringUtils.isNotBlank(form.getFeptxnTraceEjfno()))
                    auditLog.addParam("CON EJ序號", form.getFeptxnTraceEjfno());
                if (StringUtils.isNotBlank(form.getFeptxnChannelEjfno()))
                    auditLog.addParam("ClientTraceId", form.getFeptxnChannelEjfno());
                if (StringUtils.isNotBlank(form.getFeptxnTxCode()))
                    auditLog.addParam("ATM交易代號", form.getFeptxnTxCode());
                if (StringUtils.isNotBlank(form.getFeptxnBkno()) || StringUtils.isNotBlank(form.getFeptxnStan()))
                    auditLog.addParam("財金 STAN", form.getFeptxnBkno() + form.getFeptxnStan());
                if (StringUtils.isNotBlank(form.getFeptxnPcode()))
                    auditLog.addParam("財金交易代號(PCODE)", form.getFeptxnPcode());
                if (StringUtils.isNotBlank(form.getFeptxnAccType()))
                    auditLog.addParam("記帳類別", form.getFeptxnAccType());
                if (StringUtils.isNotBlank(form.getFeptxnTroutBkno()) || StringUtils.isNotBlank(form.getFeptxnMajorActno()))
                    auditLog.addParam("卡片帳號", form.getFeptxnTroutBkno() + form.getFeptxnMajorActno());
                if (StringUtils.isNotBlank(form.getFeptxnMsgid()))
                    auditLog.addParam("訊息代號", form.getFeptxnMsgid());
                // 最後塞入ThreadLocal變量中
                setAuditLog(auditLog);
                PageInfo<Feptxn> pageInfo = atmService.getFeptxn(argsMap);
                if (pageInfo.getSize() == 0) {
                    this.showMessage(mode, MessageType.INFO, QueryNoData);
                }
                PageData<UI_060550_FormMain, Feptxn> pageData = new PageData<UI_060550_FormMain, Feptxn>(pageInfo, form);
                WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
                mode.addAttribute("totalCount", pageInfo.getTotal());
                // 查詢總金額
                Map<String, Object> summary = atmService.getFeptxnSummary(argsMap);
                BigDecimal sumOfFeptxnTxAmt = new BigDecimal("0");
                if (MapUtils.isNotEmpty(summary)) {
                    sumOfFeptxnTxAmt = DbHelper.getMapValue(summary, "FEPTXN_TX_AMT", sumOfFeptxnTxAmt);
                }
                mode.addAttribute("sumOfFeptxnTxAmt", sumOfFeptxnTxAmt);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        try {
            this.setChannelOptions(mode);//通道下拉選單
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Router.UI_060550.getView();
    }

    /**
     * 檢核表單
     *
     * @param form
     * @param mode
     * @return
     */
    private boolean doValidateForm(UI_060550_FormMain form, ModelMap mode) {
        int cnt = 0;
        if (StringUtils.isNotBlank(form.getFeptxnEjfno())) {
            char[] chars = form.getFeptxnEjfno().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (!Character.isDigit(chars[i]) && chars[i] != ',') {
                    this.showMessage(mode, MessageType.DANGER, EJFNOComma);
                    return false;
                }
            }
        }
        if (!form.isCheckTrin() && !form.isCheckTrout()) {
            form.setCheckTrin(true);
            form.setCheckTrout(true);
        }
        if ((form.isCheckTrin() && !form.isCheckTrout())
                || (!form.isCheckTrin() && form.isCheckTrout())
                || (form.isCheckTrin() && form.isCheckTrout())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTbsdyFisc())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTxDate())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTxrust())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnAtmno())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnCbsRrn())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnVirCbsRrn())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTroutBkno())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTroutActno())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTrinBkno())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTrinActno())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnEjfno())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTxCode())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTxAmt())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnBkno())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnStan())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnPcode())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnAccType())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnMsgid())) {
            cnt += 1;
        }
        if (cnt < 2) {
            this.showMessage(mode, MessageType.DANGER, QueryConditionCnt);
            return false;
        }
        return true;
    }

    @PostMapping(value = "/atmmon/UI_060550/inquiryDetail")
    public String doInquiryDetail(@ModelAttribute UI_060550_FormDetail form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        // RM
        if (form.getFeptxnSubsys() != null && form.getFeptxnSubsys() == '2') {
            this.showMessage(mode, MessageType.INFO, "FEPTXN_SUBSYS == '2', 功能尚未實作!!!");
            return Router.UI_060550.getView();
        } else {
            return doInquiryDetailForOtherSubSys(form, mode);
        }
    }

    /**
     * @param form
     * @param mode
     * @return
     */
    private String doInquiryDetailForOtherSubSys(UI_060550_FormDetail form, ModelMap mode) {
        try {
            String tableNameSuffix = StringUtils.EMPTY;
            if (StringUtils.isNotBlank(form.getFeptxnTbsdyFisc()) && form.getFeptxnTbsdyFisc().length() == 8) {
                tableNameSuffix = form.getFeptxnTbsdyFisc().substring(6, 8);
            }
            Map<String, Object> detail = atmService.getFeptxnIntltxn(tableNameSuffix, form.getFeptxnEjfno(), form.getFeptxnTxDate());
            // 2010-10-14 by kyo for 跨區交易時營業日會使用卡片地區營業日
            if (MapUtils.isEmpty(detail)) {
                // 取出查詢主頁資料時候的表單資料
                UI_060550_FormMain formMain = (UI_060550_FormMain) WebUtil.getUser().getPrevPageForm();
                String feptxnTbsdyFisc = formMain.getFeptxnTbsdyFisc();
                if (StringUtils.isNotBlank(feptxnTbsdyFisc)) {
                    feptxnTbsdyFisc = StringUtils.replace(feptxnTbsdyFisc, "-", StringUtils.EMPTY);
                } else {
                    Calendar now = Calendar.getInstance();
                    feptxnTbsdyFisc = FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
                    formMain.setFeptxnTbsdyFisc(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
                }
                if (StringUtils.isNotBlank(feptxnTbsdyFisc) && feptxnTbsdyFisc.length() == 8) {
                    tableNameSuffix = feptxnTbsdyFisc.substring(6, 8);
                }
                detail = atmService.getFeptxnIntltxn(tableNameSuffix, form.getFeptxnEjfno(), form.getFeptxnTxDate());
            }
            if (MapUtils.isEmpty(detail)) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            Feptxntcb feptxntcb = null;
            String feptxnCbsProc = (String) detail.getOrDefault("FEPTXN_CBS_PROC", null);
            if (StringUtils.equals(feptxnCbsProc, "N")) {
                feptxntcb = atmService.getFeptxntcb(form.getFeptxnTxDate(), form.getFeptxnEjfno());
            }
            form.setFeptxntcb(feptxntcb);

            if (MapUtils.isNotEmpty(detail)) {
                String txCode = (String) detail.get("FEPTXN_TX_CODE");
                String idno = (String) detail.get("FEPTXN_IDNO");
                String healthId = null;
                String activityCode = null;
                if (feptxntcb != null) {
                    healthId = feptxntcb.getFeptxntcbHealthcard();
                    activityCode = feptxntcb.getFeptxntcbFacode();
                }

                if (txCode != null && ((txCode.startsWith("LF") && "TT".equals(activityCode)) || txCode.startsWith("WP"))) {
                    String txDate = (String) detail.get("FEPTXN_TX_DATE");
                    String stan = (String) detail.get("FEPTXN_STAN");

                    Map<String, Object> ucdidData = atmService.getUcdidDataByLogic(idno, healthId, txCode, txDate, stan);
                    if (MapUtils.isNotEmpty(ucdidData)) {
                        detail.putAll(ucdidData);
                    } else {
                        this.infoMessage("ucdidData is empty!");
                    }
                }
            }

            Inbk2160 inbk2160 = null;
            String feptxnChannel = (String) detail.getOrDefault("FEPTXN_CHANNEL", null);
//            if(StringUtils.equals(feptxnChannel, "HCE") || StringUtils.equals(feptxnChannel, "HCA")) {
            	String feptxnTxDate = (String) detail.getOrDefault("FEPTXN_TX_DATE", null);
            	Integer feptxnEjfno = (Integer) detail.getOrDefault("FEPTXN_EJFNO", null);
            	inbk2160 = hceService.getINBK2160ByFeptxn(feptxnTxDate, feptxnEjfno);
//            }
            form.setInbk2160(inbk2160);

            BigDecimal exrate = (BigDecimal) detail.get("INTLTXN_EXRATE");
            if (exrate == null || exrate.compareTo(BigDecimal.ZERO) == 0) {
                detail.put("INTLTXN_EXRATE", StringUtils.EMPTY);
            }

            if (detail.get("FEPTXN_ACC_TYPE") != null) {
                detail.put("FEPTXN_ACC_TYPE", getTypeName(detail.get("FEPTXN_ACC_TYPE")));
            }

            // 應該只會有一筆資料
            WebUtil.putInAttribute(mode, AttributeName.Form, form);
            WebUtil.putInAttribute(mode, AttributeName.DetailMap, detail);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_060550_Detail.getView();
    }

    @PostMapping(value = "/atmmon/UI_060550/ucdidHistory")
    public String doUcdidHistory(UI_060550_Ucdid_Form form, ModelMap mode) {
        this.changeTitle(mode, Router.UI_060550_Ucdid.getName());
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            if (StringUtils.isBlank(form.getUcdidIdno()) || StringUtils.isBlank(form.getUcdidHealthid())) {
                this.showMessage(mode, MessageType.INFO, "未代入身分證或健保卡號");
//                return Router.UI_060550_Detail.getView();
            }
            Map<String, Object> argsMap = form.toMap();
            PageInfo<Map<String, Object>> pageInfo = emsSvr.getUcdidList(argsMap);
            if (pageInfo.getSize() == 0) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            } else {
                this.showMessage(mode, MessageType.INFO, QuerySuccess);
            }
            PageData<UI_060550_Ucdid_Form, Map<String, Object>> pageData = new PageData<>(pageInfo, form);

            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_060550_Ucdid.getView();
    }

    @PostMapping(value = "/atmmon/UI_060550/inquiryFeplogList")
    public String doInquiryFeplog(@ModelAttribute UI_060610_A_FormMain form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        if (form.getFeptxnSubsys() != null && form.getFeptxnSubsys() == '2') {
            this.showMessage(mode, MessageType.INFO, "FEPTXN_SUBSYS == '2', 功能尚未實作!!!");
            return Router.UI_060550_Detail.getView();
        } else {
            return doInquiryFeplogForOtherSubSys(form, mode);
        }
    }

    private String doInquiryFeplogForOtherSubSys(UI_060610_A_FormMain form, ModelMap mode) {
        this.changeTitle(mode, Router.UI_060610_A.getName());
        try {
            List<Long> ejfnoList = new ArrayList<>();
            if (form.getFeptxnEjfno() != null) {
                ejfnoList.add(form.getFeptxnEjfno());
            }
            if (form.getEjfnO1() != null) {
                ejfnoList.add(form.getEjfnO1());
            }
            if (form.getEjfnO2() != null) {
                ejfnoList.add(form.getEjfnO2());
            }
            if (form.getEjfnO3() != null) {
                ejfnoList.add(form.getEjfnO3());
            }
            if (form.getEjfnO4() != null) {
                ejfnoList.add(form.getEjfnO4());
            }
            if (form.getEjfnO5() != null) {
                ejfnoList.add(form.getEjfnO5());
            }
            PageInfo<Feplog> pageInfo = emsSvr.getFeplog_UI060550(ejfnoList, form.getFeptxnTraceEjfno(), form.getFeptxnTxDate(), form.getPageNum(), form.getPageSize());
            if (pageInfo.getSize() == 0) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            PageData<UI_060610_A_FormMain, Feplog> pageData = new PageData<UI_060610_A_FormMain, Feplog>(pageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
            mode.addAttribute("txnEjfno",form.getFeptxnEjfno());
            mode.addAttribute("ejfnO1",form.getEjfnO1());
            mode.addAttribute("ejfnO2",form.getEjfnO2());
            mode.addAttribute("ejfnO3",form.getEjfnO3());
            mode.addAttribute("ejfnO4",form.getEjfnO4());
            mode.addAttribute("traceEjfno",form.getFeptxnTraceEjfno());
            mode.addAttribute("txDate",form.getFeptxnTxDate());
            mode.addAttribute("flag",true);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_060610_A.getView();
    }

    @PostMapping(value = "/atmmon/UI_060550/download")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> download(@RequestBody UI_060610_A_FormMain form, ModelMap mode) {
        this.infoMessage("開始下載檔案, 條件 = [", form.toString(), "]");
        try {
            this.changeTitle(mode, Router.UI_060610_A.getName());
            List<Long> ejfnoList = new ArrayList<>();
            if (form.getFeptxnEjfno() != null) {
                ejfnoList.add(form.getFeptxnEjfno());
            }
            if (form.getEjfnO1() != null) {
                ejfnoList.add(form.getEjfnO1());
            }
            if (form.getEjfnO2() != null) {
                ejfnoList.add(form.getEjfnO2());
            }
            if (form.getEjfnO3() != null) {
                ejfnoList.add(form.getEjfnO3());
            }
            if (form.getEjfnO4() != null) {
                ejfnoList.add(form.getEjfnO4());
            }
            if (form.getEjfnO5() != null) {
                ejfnoList.add(form.getEjfnO5());
            }
            List<Feplog> feploglist= emsSvr.getAllFeplog(ejfnoList, form.getFeptxnTraceEjfno(), form.getFeptxnTxDate());
            if (CollectionUtils.isEmpty(feploglist)) {
                return this.handleDownloadError("查無資料無法下載!!!");
            }
            String date = "export";
            this.showMessage(mode, MessageType.INFO, "下載成功");
            return this.download(StringUtils.join(date, "_FepLog.csv"), MediaType.APPLICATION_OCTET_STREAM_VALUE, os -> convertListToCsv(os, feploglist));
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            return this.handleDownloadException(e);
        }
    }
    public void convertListToCsv(OutputStream os, List<Feplog> dataList) throws Exception {
        os.write(0xef);
        os.write(0xbb);
        os.write(0xbf);
        os.write(String.join(",", getFieldNames()).getBytes(StandardCharsets.UTF_8));
        os.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        for (Feplog obj : dataList) {
            String[] fields = getFieldValues(obj);
            os.write(String.join(",", fields).getBytes(StandardCharsets.UTF_8));
            os.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        }
    }

    private String[] getFieldNames() {
        return new String[] {"EJ序號", "ATM序號", "訊息ID", "轉出帳號", "通道", "程式流程", "程式名稱", "訊息流程", "Stan序號", "記錄日期/時間", "備註","訊息內容"};
    }

    private String[] getFieldValues(Feplog feplog) {
        SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat hms = new SimpleDateFormat("HH:mm:ss");
        //依合庫需求加上excel指定文字格式的處理
        return new String[] {
                "=\"" +nullToEmptyStr(feplog.getEj()) + "\"",
                "=\"" + nullToEmptyStr(feplog.getAtmseq()) + "\"",
                "=\"" + nullToEmptyStr(feplog.getMessageid()) + "\"",
                "=\"" + nullToEmptyStr(feplog.getTroutactno()) + "\"",
                "=\"" + nullToEmptyStr(feplog.getChannel()) + "\"",
                "=\"" + nullToEmptyStr(feplog.getProgramflow()) + "\"",
                "=\"" + nullToEmptyStr(feplog.getProgramname()) + "\"",
                "=\"" + nullToEmptyStr(feplog.getMessageflow()) + "\"",
                "=\"" + nullToEmptyStr(feplog.getStan()) + "\"",
                "\"" + nullToEmptyStr(ymd.format(feplog.getLogdate()) +" "+ hms.format(feplog.getLogdate())) + "\"",
                "=\"" + nullToEmptyStr(feplog.getRemark()) + "\"",
                 "\"" + nullToEmptyStr(feplog.getTxmessage()) + "\"",
        };
    }

    @PostMapping(value = "/atmmon/UI_060550/downloadFeptxn")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> downloadFeptxn(@ModelAttribute UI_060550_FormMain form, ModelMap mode) {
        this.infoMessage("開始下載檔案, 條件 = [", form.toString(), "]");
        try {
            UI_060550_FormMain queryForm = (UI_060550_FormMain) WebUtil.getUser().getCurrentPageForm();
            if (queryForm == null) {
                return this.handleDownloadError("無法獲取查詢條件，請重新查詢！");
            }
            // 轉成map對象供最後mybatis查詢資料使用
            Map<String, Object> argsMap = this.prepareArgsMap(queryForm);
            // 下載所有資料不分頁
            argsMap.put("pageNum", 1);
            argsMap.put("pageSize", -1);
            PageInfo<Feptxn> pageInfo = atmService.getFeptxn(argsMap);
            List<Feptxn> feptxnList = pageInfo.getList();
            if (CollectionUtils.isEmpty(feptxnList)) {
                return this.handleDownloadWarn("查無資料可下載");
            }
            this.showMessage(mode, MessageType.INFO, "下載成功");
            String fileName = StringUtils.join("Feptxn_", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()), ".csv");
            return this.download(fileName, MediaType.APPLICATION_OCTET_STREAM_VALUE, os -> convertFeptxnToCsv(os, feptxnList));
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            return this.handleDownloadException(e);
        }
    }

    private Map<String, Object> prepareArgsMap(UI_060550_FormMain form) {
        Map<String, Object> argsMap = form.toMap();
        String feptxnTbsdyFisc = form.getFeptxnTbsdyFisc();
        if (StringUtils.isNotBlank(feptxnTbsdyFisc)) {
            feptxnTbsdyFisc = StringUtils.replace(feptxnTbsdyFisc, "-", StringUtils.EMPTY);
        }
        String feptxnTxTimeBegin = form.getFeptxnTxTimeBegin();
        if (StringUtils.isNotBlank(feptxnTxTimeBegin)) {
            feptxnTxTimeBegin = StringUtils.replace(feptxnTxTimeBegin, ":", StringUtils.EMPTY);
        } else {
            feptxnTxTimeBegin = "000000";
            form.setFeptxnTxTimeBegin("00:00:00");
        }
        String feptxnTxTimeEnd = form.getFeptxnTxTimeEnd();
        if (StringUtils.isNotBlank(feptxnTxTimeEnd)) {
            feptxnTxTimeEnd = StringUtils.replace(feptxnTxTimeEnd, ":", StringUtils.EMPTY);
        } else {
            feptxnTxTimeEnd = "235959";
            form.setFeptxnTxTimeEnd("23:59:59");
        }
        String feptxnTxDate = form.getFeptxnTxDate();
        if (StringUtils.isNotBlank(feptxnTxDate)) {
            feptxnTxDate = StringUtils.replace(feptxnTxDate, "-", StringUtils.EMPTY);
        }
        BigDecimal feptxnTxAmt = null;
        if (StringUtils.isNotBlank(form.getFeptxnTxAmt())) {
            feptxnTxAmt = new BigDecimal(form.getFeptxnTxAmt());
        } else {
            feptxnTxAmt = new BigDecimal("-1");
        }
        String fiscFlag = null;
        if (form.isCheckTrin() && !form.isCheckTrout()) {
            fiscFlag = "0"; // 只查自行
        } else if (!form.isCheckTrin() && form.isCheckTrout()) {
            fiscFlag = "1"; // 只查跨行
        } else {
            fiscFlag = "2"; // 不限制
        }
        List<Long> feptxnEjfnoList = new ArrayList<>();
        if (StringUtils.isNotBlank(form.getFeptxnEjfno())) {
            if (form.getFeptxnEjfno().contains(",")) {
                String[] feptxnEjfnos = StringUtils.split(form.getFeptxnEjfno(), ",");
                for (String feptxnEjfno : feptxnEjfnos) {
                    feptxnEjfnoList.add(Long.parseLong(feptxnEjfno));
                }
            } else {
                feptxnEjfnoList.add(Long.parseLong(form.getFeptxnEjfno()));
            }
        }
        String feptxnChannel = null;
        if (StringUtils.isNotBlank(form.getFeptxnChannel())) {
            feptxnChannel = form.getFeptxnChannel();
        }
        String feptxnChannelEjfno = null;
        if (StringUtils.isNotBlank(form.getFeptxnChannelEjfno())) {
            feptxnChannelEjfno = form.getFeptxnChannelEjfno();
        }
        List<String> feptxnExcludeTxCodeList = new ArrayList<>();
        if (StringUtils.isNotBlank(form.getFeptxnExcludeTxCode()) && form.getFeptxnExcludeTxCode().contains(";")) {
            String[] feptxnExcludeTxCodes = StringUtils.split(form.getFeptxnExcludeTxCode(), ";");
            Collections.addAll(feptxnExcludeTxCodeList, feptxnExcludeTxCodes);
        } else {
            feptxnExcludeTxCodeList.add(form.getFeptxnExcludeTxCode());
        }
        String feptxnCbsProc = null;
        if (StringUtils.isNotBlank(form.getFeptxnCbsProc())) {
            feptxnCbsProc = form.getFeptxnCbsProc();
        }

        // 覆蓋或者增加map對象中的值
        argsMap.put("feptxnTbsdyFisc", feptxnTbsdyFisc);
        argsMap.put("feptxnTxTimeBegin", feptxnTxTimeBegin);
        argsMap.put("feptxnTxTimeEnd", feptxnTxTimeEnd);
        argsMap.put("feptxnTxDate", feptxnTxDate);
        argsMap.put("feptxnTxAmt", feptxnTxAmt);
        argsMap.put("fiscFlag", fiscFlag);
        argsMap.put("feptxnEjfno", feptxnEjfnoList);
        argsMap.put("feptxnChannel", feptxnChannel);
        argsMap.put("feptxnChannelEjfno", feptxnChannelEjfno);
        argsMap.put("feptxnExcludeTxCode", feptxnExcludeTxCodeList);
        /* 5/13 為了讓查詢FEPTXN時,營業日或交易日填一個即可搜尋 */
        if (StringUtils.isNotBlank(feptxnTbsdyFisc) && feptxnTbsdyFisc.length() >= 8) {
            argsMap.put("tableNameSuffix", feptxnTbsdyFisc.substring(6, 8));
        } else {
            argsMap.put("tableNameSuffix", "00");
        }
        argsMap.put("sqlSortExpression", form.getSqlSortExpression());
        argsMap.put("feptxnCbsProc", feptxnCbsProc);
        argsMap.put("pageSize", form.getPageSize());
        return argsMap;
    }

    private void convertFeptxnToCsv(OutputStream os, List<Feptxn> dataList) throws Exception {
        os.write(0xef);
        os.write(0xbb);
        os.write(0xbf);
        os.write(String.join(",", getFeptxnFieldNames()).getBytes(StandardCharsets.UTF_8));
        os.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        for (Feptxn obj : dataList) {
            String[] fields = getFeptxnFieldValues(obj);
            os.write(String.join(",", fields).getBytes(StandardCharsets.UTF_8));
            os.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        }
    }

    private String[] getFeptxnFieldNames() {
        return new String[]{"交易日期/時間", "ATM交易代號", "財金交易代號", "交易結果", "卡片帳號", "扣款帳號", "轉入帳號", "提領幣別", "交易金額", "ATM代號", "發卡區CBS序號", "交易區CBS序號", "EJ序號", "財金STAN", "失敗RC"};
    }

    private String[] getFeptxnFieldValues(Feptxn feptxn) {
        String atmTxCode;
        if ("ATM".equals(feptxn.getFeptxnChannel()) && StringUtils.isNotBlank(feptxn.getFeptxnMsgid()) && feptxn.getFeptxnMsgid().length() >= 2 && "FC".equals(feptxn.getFeptxnMsgid().substring(0, 2))) {
            atmTxCode = StringUtils.joinWith("-", feptxn.getFeptxnMsgid(), feptxn.getFeptxnTxCode());
        } else {
            atmTxCode = feptxn.getFeptxnTxCode();
        }
        String feptxnRcCode = "";
        if (!"A".equals(feptxn.getFeptxnTxrust())) {
            if (feptxn.getFeptxnConRc() != null && !"4001".equals(feptxn.getFeptxnConRc())) {
                feptxnRcCode = feptxn.getFeptxnConRc();
            } else if (feptxn.getFeptxnRepRc() != null && !"4001".equals(feptxn.getFeptxnRepRc())) {
                feptxnRcCode = feptxn.getFeptxnRepRc();
            } else {
                feptxnRcCode = feptxn.getFeptxnReplyCode();
            }
        }
        return new String[]{
                "\"" + nullToEmptyStr(FEPWebBase.formatYMDHMS(feptxn.getFeptxnTxDate() + " " + feptxn.getFeptxnTxTime())) + "\"",
                "\"\t" + nullToEmptyStr(atmTxCode) + "\"",
                "\"" + nullToEmptyStr(feptxn.getFeptxnPcode()) + "\"",
                "\"" + nullToEmptyStr(feptxn.getFeptxnTxrust()) + "\"",
                "\"\t" + nullToEmptyStr(FEPWebBase.join("-", feptxn.getFeptxnTroutBkno(), feptxn.getFeptxnMajorActno())) + "\"",
                "\"\t" + nullToEmptyStr(FEPWebBase.join("-", feptxn.getFeptxnTroutBkno(), feptxn.getFeptxnTroutActno())) + "\"",
                "\"\t" + nullToEmptyStr(FEPWebBase.join("-", feptxn.getFeptxnTrinBkno(), feptxn.getFeptxnTrinActno())) + "\"",
                "\"" + nullToEmptyStr(feptxn.getFeptxnTxCur()) + "\"",
                "\"" + (feptxn.getFeptxnTxAmt() != null ? feptxn.getFeptxnTxAmt().setScale(2, RoundingMode.HALF_UP).toPlainString() : "") + "\"",
                "\"" + nullToEmptyStr(FEPWebBase.join("-", feptxn.getFeptxnAtmno(), feptxn.getFeptxnAtmSeqno())) + "\"",
                "\"" + nullToEmptyStr(feptxn.getFeptxnCbsRrn()) + "\"",
                "\"" + nullToEmptyStr(feptxn.getFeptxnVirCbsRrn()) + "\"",
                "\"" + nullToEmptyStr(feptxn.getFeptxnEjfno()) + "\"",
                "\"\t" + nullToEmptyStr(FEPWebBase.join("-", feptxn.getFeptxnBkno(), feptxn.getFeptxnStan())) + "\"",
                "\"\t" + nullToEmptyStr(feptxnRcCode) + "\""
        };
    }
}
