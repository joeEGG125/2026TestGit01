package com.syscom.fep.web.controller.atmmon;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.IOUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.model.Msgkb;
import com.syscom.fep.mybatis.util.DB2Util;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SessionKey;
import com.syscom.fep.web.entity.atmmon.MsgkbNotify;
import com.syscom.fep.web.form.atmmon.UI_060620_B_Form;
import com.syscom.fep.web.form.atmmon.UI_060620_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.util.WebUtil;
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
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * 事件管理系統(EMS)日誌查詢
 *
 * @author Chenyu
 */
@Controller
public class UI_060620Controller extends BaseController {
    private static final String URL_DO_QUERY = "/atmmon/UI_060620/saveIntervalBtn_Click";
    private static final String ATTRIBUTE_NAME_MSGKBNOTIFY = "msgkbNotify";
    @Autowired
    private AtmService atmService;
    private boolean status;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 先給Session中存入臨時全域臨時變量
        WebUtil.putInSession(SessionKey.TemporaryRestoreData, new TemporaryRestoreData());
        // 初始化表單資料
        UI_060620_Form form = new UI_060620_Form();
        String time = "30";
        form.setTime(time);
        form.setDtTransactDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        form.setTxTransactTimeBEG("00:00");
        form.setTxTransactTimeEND("23:59");
        form.setUrl(URL_DO_QUERY);
        this.saveIntervalBtn_Click(form, mode);
    }

    private PageInfo<HashMap<String, Object>> queryData(ModelMap mode, UI_060620_Form form) {
        String txTransactDate = "";
        String txDateTimeBeg = "";
        String txDateTimeEnd = "";
        String sLevel = "";
        String sAtmNo = "";
        String sIP = "";
        String sApplication = "";
        if (!"".equals(form.getDtTransactDate())) {
            txTransactDate = StringUtils.replace(form.getDtTransactDate(), "-", StringUtils.EMPTY);
        } else {
            String tradingDate = StringUtils.replace(form.getDtTransactDate(), "-", StringUtils.EMPTY);
            form.setDtTransactDate(tradingDate);
            txTransactDate = form.getDtTransactDate();
        }
        if (!"".equals(form.getTxTransactTimeBEG())) {
            txDateTimeBeg = form.getTxTransactTimeBEG();
        } else {
            txDateTimeBeg = "00:00";
            form.setTxTransactTimeBEG("00:00");
        }
        if (!"".equals(form.getTxTransactTimeEND())) {
            txDateTimeEnd = form.getTxTransactTimeEND();
        } else {
            txDateTimeEnd = "23:59";
            form.setTxTransactTimeEND("23:59");
        }
        sAtmNo = form.getTxtATMNo();
        sIP = form.getTxtIP();
        sLevel = "";
        sApplication = form.getApplicationDdl();
        String subsys = form.getSubSystemDdl();
        // 記錄auditLog
        AuditLog auditLog = new AuditLog();
        // 塞入auditLog.action
        auditLog.setAction("查詢");
        // 塞入auditLog.params
        if (StringUtils.isNotBlank(form.getTime()))
            auditLog.addParam("畫面更新間隔時間", form.getTime() + "秒");
        if (StringUtils.isNotBlank(form.getSubSystemDdl()))
            auditLog.addParam("子系統", form.getSubSystemDdl());
        if (StringUtils.isNotBlank(form.getDtTransactDate()))
            auditLog.addParam("警示日期:", form.getDtTransactDate());
        if (StringUtils.isNotBlank(form.getTxTransactTimeBEG()) && StringUtils.isNotBlank(form.getTxTransactTimeEND()))
            auditLog.addParam("警示時間起訖", form.getTxTransactTimeBEG() + "~" + form.getTxTransactTimeEND());
        if (StringUtils.isNotBlank(form.getTxtATMNo()))
            auditLog.addParam("ATM代號", form.getTxtATMNo());
        if (StringUtils.isNotBlank(form.getApplicationDdl()))
            auditLog.addParam("訊息類別", form.getApplicationDdl());
        if (StringUtils.isNotBlank(form.getTxtArErdescription()))
            auditLog.addParam("訊息內容", form.getTxtArErdescription());
        // 最後塞入ThreadLocal變量中
        setAuditLog(auditLog);
        PageInfo<HashMap<String, Object>> dt = getResultData(form, mode, subsys, txTransactDate, txDateTimeBeg, txDateTimeEnd, sLevel, sAtmNo, sIP, sApplication);
        WebUtil.putInAttribute(mode, AttributeName.PageData, dt);
        this.getSessionData().pageData = dt;
        return dt;
    }

    private PageInfo<HashMap<String, Object>> getResultData(UI_060620_Form form, ModelMap mode, String AR_SUBSYS, String sDate, String sTimeBeg, String sTimeEnd, String sLevel, String sATMNo, String sIP, String sApplication) {
        try {
            String begDateTime = sDate + StringUtils.replace(sTimeBeg, ":", StringUtils.EMPTY) + "00";
            String endDateTime = sDate + StringUtils.replace(sTimeEnd, ":", StringUtils.EMPTY) + "00";
            PageInfo<HashMap<String, Object>> dt = atmService.queryAlertData(AR_SUBSYS, begDateTime, endDateTime, sLevel, sATMNo, sIP, sApplication, form.getTxtArErdescription(), form.getPageNum(), form.getPageSize());
            if (!dt.getList().isEmpty()) {
                int tempVar = dt.getList().size();
                for (int i = 0; i < tempVar; i++) {
                    String APMessagexml = DB2Util.getClobValue(dt.getList().get(i).get("AR_MESSAGE"), StringUtils.EMPTY).replace("&#x", "");
                    APMessagexml = "<APMessage>" + APMessagexml + "</APMessage>";
                    APMessagexml = APMessagexml.replace("<LogData>", "").replace("</LogData>", "");
                    // Element root = XmlUtil.dom4jLoad(APMessagexml);
                    String root = APMessagexml;
                    if (!dt.getList().get(i).containsKey("AR_ERDESCRIPTION")) {
                        dt.getList().get(i).put("AR_ERDESCRIPTION", "");
                    }
                    dt.getList().get(i).put("EJ", XmlUtil.getChildElementValue(root, "EJ", StringUtils.EMPTY));
                    dt.getList().get(i).put("MSGID", XmlUtil.getChildElementValue(root, "MessageId", StringUtils.EMPTY));
                    dt.getList().get(i).put("Channel", XmlUtil.getChildElementValue(root, "Channel", StringUtils.EMPTY));
                    dt.getList().get(i).put("ATMNO", XmlUtil.getChildElementValue(root, "ATMNo", StringUtils.EMPTY));
                    dt.getList().get(i).put("STAN", XmlUtil.getChildElementValue(root, "STAN", StringUtils.EMPTY));
                    dt.getList().get(i).put("SBK", XmlUtil.getChildElementValue(root, "Bkno", StringUtils.EMPTY));
                    dt.getList().get(i).put("DBK", XmlUtil.getChildElementValue(root, "DesBkno", StringUtils.EMPTY));
                    dt.getList().get(i).put("TxUser", XmlUtil.getChildElementValue(root, "TxUser", StringUtils.EMPTY));
                }
            }
            return dt;
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, "讀取資料有誤");
            return null;
        }
    }

    // UpdatePanel設定
    @PostMapping(value = URL_DO_QUERY)
    protected String saveIntervalBtn_Click(@ModelAttribute UI_060620_Form form, ModelMap mode) {
        this.doKeepFormData(mode, form);
        if (StringUtils.isBlank(form.getDtTransactDate())) {
            this.showMessage(mode, MessageType.WARNING, "警示日期不得為空!");
            WebUtil.putInAttribute(mode, AttributeName.Form, form);
            return Router.UI_060620.getView();
        }
        int time = 30;
        try {
            time = Integer.parseInt(form.getTime());
            // 必須大於30秒, 小於120秒
            if (time < 30 || time > 120) {
                time = 30;
            }
        } catch (NumberFormatException e) {
            this.warnMessage(e, e.getMessage());
        }
        form.setTime(Integer.toString(time));
        updateDataPnl1_Tick(mode, form);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        return Router.UI_060620.getView();
    }

    // 計時器發動的事件
    private void updateDataPnl1_Tick(ModelMap mode, UI_060620_Form form) {
        String time = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
        StringBuffer str = new StringBuffer(time);
        str.insert(4, "年");
        str.insert(7, "月");
        str.insert(10, "日");
        str.insert(13, "點");
        str.insert(16, "分");
        str.insert(19, "秒");
        mode.addAttribute("newtime", str);
        queryData(mode, form);
    }

    // Grid中第一列查詢按鈕
    @PostMapping(value = "/atmmon/UI_060620_A/inquiryDetail")
    public String doInquiryDetail(@ModelAttribute UI_060620_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            String AR_NO = form.getArNo();
            String APMessage = "";
            APMessage = atmService.queryAR_Message(AR_NO);
            APMessage = "<APMessage>" + APMessage + "</APMessage>";
            APMessage = APMessage.replace("<LogData>", "").replace("</LogData>", "");
            // Element root = XmlUtil.dom4jLoad(APMessage);
            String root = APMessage;
            List<HashMap<String, String>> list = new ArrayList<>();
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("name", "EJNumber");
            hashMap.put("value", XmlUtil.getChildElementValue(root, "EJ", StringUtils.EMPTY));
            list.add(hashMap);
            hashMap = new HashMap<>();
            hashMap.put("name", "ATMNo");
            hashMap.put("value", XmlUtil.getChildElementValue(root, "ATMNo", StringUtils.EMPTY));
            list.add(hashMap);
            hashMap = new HashMap<>();
            hashMap.put("name", "ATMSeq");
            hashMap.put("value", XmlUtil.getChildElementValue(root, "ATMSeq", StringUtils.EMPTY));
            list.add(hashMap);
            hashMap = new HashMap<>();
            hashMap.put("name", "STAN");
            hashMap.put("value", XmlUtil.getChildElementValue(root, "STAN", StringUtils.EMPTY));
            list.add(hashMap);
            hashMap = new HashMap<>();
            hashMap.put("name", "轉出行");
            hashMap.put("value", XmlUtil.getChildElementValue(root, "Bkno", StringUtils.EMPTY));
            list.add(hashMap);
            hashMap = new HashMap<>();
            hashMap.put("name", "轉入行");
            hashMap.put("value", XmlUtil.getChildElementValue(root, "DesBkno", StringUtils.EMPTY));
            list.add(hashMap);
            hashMap = new HashMap<>();
            hashMap.put("name", "FiscRC");
            hashMap.put("value", XmlUtil.getChildElementValue(root, "FiscRC", StringUtils.EMPTY));
            list.add(hashMap);
            hashMap = new HashMap<>();
            hashMap.put("name", "TxChannel");
            hashMap.put("value", XmlUtil.getChildElementValue(root, "Channel", StringUtils.EMPTY));
            list.add(hashMap);
            hashMap = new HashMap<>();
            hashMap.put("name", "TxProgram");
            hashMap.put("value", XmlUtil.getChildElementValue(root, "ProgramName", StringUtils.EMPTY));
            list.add(hashMap);
            hashMap = new HashMap<>();
            hashMap.put("name", "使用者");
            hashMap.put("value", XmlUtil.getChildElementValue(root, "TxUser", StringUtils.EMPTY));
            list.add(hashMap);
            hashMap = new HashMap<>();
            hashMap.put("name", "TxMessage");
            hashMap.put("value", XmlUtil.getChildElementValue(root, "Message", StringUtils.EMPTY));
            list.add(hashMap);
            hashMap = new HashMap<>();
            hashMap.put("name", "TxSource");
            hashMap.put("value", XmlUtil.getChildElementValue(root, "TxSource", StringUtils.EMPTY));
            list.add(hashMap);
            hashMap = new HashMap<>();
            hashMap.put("name", "TxDesc");
            hashMap.put("value", XmlUtil.getChildElementValue(root, "TxErrDesc", StringUtils.EMPTY));
            list.add(hashMap);
            hashMap = new HashMap<>();
            hashMap.put("name", "ExStack");
            hashMap.put("value", XmlUtil.getChildElementValue(root, "ExStack", StringUtils.EMPTY));
            list.add(hashMap);
            WebUtil.putInAttribute(mode, AttributeName.DetailMap, list);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_060620_A.getView();
    }

    // Grid中最後一列查詢按鈕
    @PostMapping(value = "/atmmon/UI_060620_B/inquiryDetail")
    public String doinQuireMsg(@ModelAttribute UI_060620_B_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        TemporaryRestoreData sessionData = this.getSessionData();
        try {
            sessionData.index = Integer.parseInt(form.getIndex());
        } catch (NumberFormatException e) {
            this.warnMessage(e, e.getMessage());
        }
        try {
            getAlertData(sessionData.index);
            bindFormViewData(mode);
        } catch (Exception ex) {
            this.handleInquiryException(mode, ex);
        }
        return Router.UI_060620_B.getView();
    }

    private List<Msgkb> getResultDatas() {
        TemporaryRestoreData sessionData = this.getSessionData();
        return atmService.get_AlertDetailForUI060620B(sessionData.arERCODE, sessionData.txExternalCode, sessionData.txExSubCode);
    }

    private void getAlertData(int index) throws Exception {
        TemporaryRestoreData sessionData = this.getSessionData();
        HashMap<String, Object> hashMap = sessionData.pageData.getList().get(index);
        String apmessage = DB2Util.getClobValue(hashMap.get("AR_MESSAGE"), StringUtils.EMPTY);
        if (StringUtils.isNotBlank(apmessage)) {
            apmessage = "<EMS>" + apmessage + "</EMS>";
            apmessage = apmessage.replace("<LogData>", "").replace("</LogData>", "");
            // Element root = XmlUtil.dom4jLoad(APMessagexml);
            String root = apmessage;
            String nTxExternalCode = XmlUtil.getChildElementValue(root, "TxExternalCode", StringUtils.EMPTY);
            if (StringUtils.isNotBlank(nTxExternalCode)) {
                sessionData.txExternalCode = nTxExternalCode;
            }
            // 2024-11-07 Richard modified for
            // UI_060620裏, 本來是AR_DESCRIPTION沒值時, 訊息欄位會抓xml的Remark
            // 這裏你幫我改成,
            // lbtmp2.Text = dr["AR_ERRDESCRIPTION"]
            // lbtmp2.Text += nRemark.Value
            // 就是一律把Remark加上去就好
            // 這樣如果前前SendEMS有把自訂訊息塞Remark, UI也可以同時跟exception訊息一起顯示
            // by Ashiang
            String nRemark = XmlUtil.getChildElementValue(root, "Remark", StringUtils.EMPTY);
            // if (StringUtils.isNotBlank(nRemark)) {
            //     sessionData.arREMARK = nRemark;
            // } else {
            //     sessionData.arREMARK = (String) hashMap.get("AR_ERCODE");
            // }
            sessionData.arREMARK = StringUtils.join((String) hashMap.get("AR_ERRDESCRIPTION"), nRemark);

            String nExSubCode = XmlUtil.getChildElementValue(root, "ExSubCode", StringUtils.EMPTY);
            if (StringUtils.isNotBlank(nExSubCode)) {
                sessionData.txExSubCode = nExSubCode;
            }
        }
        sessionData.arSYS = (String) hashMap.get("AR_SYS");
        sessionData.arSUBSYS = (String) hashMap.get("AR_SUBSYS");
        sessionData.arDATE = hashMap.get("AR_DATETIME") != null ? hashMap.get("AR_DATETIME").toString().substring(0, 10) : StringUtils.EMPTY;
        sessionData.arTIME = hashMap.get("AR_DATETIME") != null ? hashMap.get("AR_DATETIME").toString().substring(11, 19) : StringUtils.EMPTY;
        sessionData.arHOSTNAME = (String) hashMap.get("AR_HOSTNAME");
        sessionData.arERCODE = (String) hashMap.get("AR_ERCODE");
        sessionData.arLEVEL = (String) hashMap.get("AR_LEVEL");
        sessionData.arERDESCRIPTION = (String) hashMap.get("AR_ERDESCRIPTION");
    }

    /**
     * <modifier>Anna Lin</modifier>
     * <reason>Spec. 修改</reason>
     * <date>2011/7/25</date>
     * <modifier>Anna Lin</modifier>
     * <reason>MSGKB 新增欄位</reason>
     * <date>2012/4/23</date>
     */
    private void bindFormViewData(ModelMap mode) {
        TemporaryRestoreData sessionData = this.getSessionData();
        List<Msgkb> dt = getResultDatas();
        HashMap<String, String> hashMap = new HashMap<>();
        MsgkbNotify msgkbNotify = new MsgkbNotify();
        if (dt.isEmpty()) {
            hashMap.put("lblErrCode", sessionData.arERCODE);
            hashMap.put("lblSeverity", sessionData.arLEVEL);
            hashMap.put("lblExternalCode", sessionData.txExternalCode);
            hashMap.put("lblExSubCode", sessionData.txExSubCode);
            hashMap.put("description", sessionData.arERDESCRIPTION);
            hashMap.put("remark", sessionData.arREMARK);
            hashMap.put("chbNotify", "0");
            hashMap.put("responsible", "");
            hashMap.put("notifyMail", "");
            hashMap.put("action", "");
            hashMap.put("msgPattern", sessionData.arERDESCRIPTION);
        } else {
            for (int i = 0; i < dt.size(); i++) {
                if (dt.get(i).getMsgpattern() != null && StringUtils.isNotBlank(dt.get(i).getMsgpattern()) && sessionData.arERDESCRIPTION.contains(dt.get(i).getMsgpattern())) {
                    hashMap.put("msgkbNo", dt.get(i).getMsgkbNo().toString());
                    hashMap.put("lblErrCode", dt.get(i).getErrorcode());
                    hashMap.put("chbNotify", dt.get(i).getNotify().toString());
                    hashMap.put("lblExternalCode", dt.get(i).getExternalcode());
                    hashMap.put("lblExSubCode", dt.get(i).getExsubcode());
                    hashMap.put("responsible", dt.get(i).getResponsible());
                    hashMap.put("notifyMail", dt.get(i).getNotifymail());
                    hashMap.put("action", dt.get(i).getAction());
                    hashMap.put("lblSeverity", dt.get(i).getSeverity());
                    // 訊息與說明欄一律以EMS的為主
                    hashMap.put("description", dt.get(i).getDescription());
                    hashMap.put("remark", dt.get(i).getRemark());
                    hashMap.put("msgPattern", dt.get(i).getMsgpattern());
                    status = false;
                    // msgkbNotify塞入值
                    msgkbNotify.getNotifyFromMsgkb(dt.get(i), false);
                    break;
                } else {
                    status = true;
                }
            }
            if (status) {
                hashMap.put("lblErrCode", sessionData.arERCODE);
                hashMap.put("lblSeverity", sessionData.arLEVEL);
                hashMap.put("lblExternalCode", sessionData.txExternalCode);
                hashMap.put("lblExSubCode", sessionData.txExSubCode);
                hashMap.put("description", sessionData.arERDESCRIPTION);
                hashMap.put("remark", sessionData.arREMARK);
                hashMap.put("chbNotify", "0");
                hashMap.put("responsible", "");
                hashMap.put("notifyMail", "");
                hashMap.put("action", "");
                hashMap.put("msgPattern", sessionData.arERDESCRIPTION);
            }
        }
        hashMap.put("lblDate", sessionData.arDATE);
        hashMap.put("lblTime", sessionData.arTIME);
        hashMap.put("lblHost", sessionData.arHOSTNAME);
        hashMap.put("lblSys", sessionData.arSYS);
        hashMap.put("lblSubSys", sessionData.arSUBSYS);
        sessionData.thisDate = hashMap;
        sessionData.msgkbNotify = msgkbNotify;
        WebUtil.putInAttribute(mode, AttributeName.DetailMap, hashMap);
        mode.put(ATTRIBUTE_NAME_MSGKBNOTIFY, msgkbNotify);
    }

    @PostMapping(value = "/atmmon/UI_060620_B/updateDetail")
    @ResponseBody
    protected BaseResp<?> updateDetail(@RequestBody UI_060620_B_Form form) {
        TemporaryRestoreData sessionData = this.getSessionData();
        BaseResp<?> response = new BaseResp<>();
        Msgkb defMSGKB = new Msgkb();
        defMSGKB.setNotifymail(StringUtils.EMPTY); // 預設塞入空串, 后面form.getMsgkbNotify().setNotifyToMsgkb时会塞入
        defMSGKB.setMsgkbNotifyphone(StringUtils.EMPTY); // 預設塞入空串, 后面form.getMsgkbNotify().setNotifyToMsgkb时会塞入
        // 保存時, 將mail和phone存入msgkb對應的欄位中
        if (form.getMsgkbNotify() != null)
            form.getMsgkbNotify().setNotifyToMsgkb(defMSGKB);
        int iRes = 0;
        boolean succeed = false;
        try {
            String msgkbNo = sessionData.thisDate.get("msgkbNo");
            String sERCode = sessionData.thisDate.get("lblErrCode"); // 錯誤代碼
            String TxExternalCode = sessionData.thisDate.get("lblExternalCode"); // 輸出代碼
            String TxExSubCode = sessionData.thisDate.get("lblExSubCode"); // 輸出子代碼
            String AR_LEVEL = sessionData.thisDate.get("lblSeverity"); // 訊息等級
            String bNotify = form.getChbNotify(); // 自動通知
            String sAction = form.getAction(); // 處理方式
            String sRemark = form.getRemark(); // 說明
            String sResponsible = form.getResponsible(); // 負責人
            String sDescription = form.getDescription(); // 訊息
            String msgPattern = form.getMsgPattern(); // 訊息比對
            // 若勾選自動通知,則電子郵件一欄必須有資料
            if (DbHelper.toBoolean(bNotify)) {
                if (form.getMsgkbNotify().isFepNotifyMail_Customize()) {
                    if (StringUtils.isBlank(form.getMsgkbNotify().getMsgkbNotifymail())) {
                        response.setMessage(MessageType.DANGER, "未輸入自定Mail通知");
                        return response;
                    } else {
                        RefString invalidMail = new RefString();
                        if (!form.getMsgkbNotify().isFepNotifyMailCustomizeValid(invalidMail)) {
                            response.setMessage(MessageType.DANGER, StringUtils.join("自定Mail通知", String.format(Const.KEY_WORDS_IN_MESSAGE_S, invalidMail.get()), "格式不正確"));
                            return response;
                        }
                    }
                }
                if (form.getMsgkbNotify().isFepNotifyPhone_Customize() && StringUtils.isBlank(form.getMsgkbNotify().getMsgkbNotifyphone())) {
                    response.setMessage(MessageType.DANGER, "未輸入自定簡訊通知");
                    return response;
                } else if (StringUtils.isBlank(defMSGKB.getNotifymail()) && StringUtils.isBlank(defMSGKB.getMsgkbNotifyphone())) {
                    response.setMessage(MessageType.DANGER, "Mail或簡訊通知至少有一個欄位必須有值");
                    return response;
                }
            }
//            else {
//                if (form.getMsgkbNotify().isFepNotifyMail_APD() || form.getMsgkbNotify().isFepNotifyMail_SYS() || form.getMsgkbNotify().isFepNotifyMail_Customize() ||
//                        form.getMsgkbNotify().isFepNotifyPhone_APD() || form.getMsgkbNotify().isFepNotifyPhone_SYS() || form.getMsgkbNotify().isFepNotifyPhone_Customize()) {
//                    response.setMessage(MessageType.DANGER, StringUtils.join(String.format(Const.KEY_WORDS_IN_MESSAGE_S, "自動通知"), "未勾選"));
//                    return response;
//                }
//            }
            defMSGKB.setErrorcode(sERCode);
            defMSGKB.setExternalcode(TxExternalCode);
            defMSGKB.setExsubcode(StringUtils.isBlank(TxExSubCode) ? StringUtils.EMPTY : TxExSubCode);
            defMSGKB.setSeverity(AR_LEVEL);
            defMSGKB.setDescription(sDescription);
            defMSGKB.setRemark(sRemark);
            defMSGKB.setNotify(Short.parseShort(bNotify));
            defMSGKB.setAction(sAction);
            defMSGKB.setResponsible(sResponsible);
            defMSGKB.setUicolor(StringUtils.EMPTY);
            defMSGKB.setMsgpattern(StringUtils.isBlank(msgPattern) ? StringUtils.EMPTY : msgPattern);
            defMSGKB.setVisible((short) 1); // 預設塞入1
            Msgkb msgkb;
            if (msgkbNo != null) {
                msgkb = atmService.getMsgkb(Long.parseLong(msgkbNo));
                if (msgkb != null) {
                    defMSGKB.setMsgkbNo(msgkb.getMsgkbNo());
                    defMSGKB.setErrorcode(null); // 這個欄位不用更新, 所以要塞入null
                    defMSGKB.setExternalcode(null); // 這個欄位不用更新, 所以要塞入null
                    defMSGKB.setSeverity(null); // 這個欄位不用更新, 所以要塞入null
                    defMSGKB.setVisible(null); // 這個欄位不用更新, 所以要塞入null
                    defMSGKB.setUicolor(null); // 這個欄位不用更新, 所以要塞入null
                    defMSGKB.setExsubcode(null); // 這個欄位不用更新, 所以要塞入null
                    // update
                    iRes = atmService.updateMSGKB(defMSGKB);
                    if (iRes > 0) {
                        response.setMessage(MessageType.SUCCESS, UpdateSuccess);
                        succeed = true;
                    } else {
                        response.setMessage(MessageType.DANGER, UpdateFail);
                    }
                } else {
                    if (insertMsgKb(defMSGKB) > 0) {
                        response.setMessage(MessageType.SUCCESS, InsertSuccess);
                        succeed = true;
                    } else {
                        response.setMessage(MessageType.DANGER, InsertFail);
                    }
                }
            } else {
                // insert
                if (insertMsgKb(defMSGKB) > 0) {
                    response.setMessage(MessageType.SUCCESS, InsertSuccess);
                    succeed = true;
                } else {
                    response.setMessage(MessageType.DANGER, InsertFail);
                }
            }
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            response.setMessage(MessageType.DANGER, programError);
        }
//        finally {
//            // 如果UI上勾選不通知, 則要清除重置表單中的通知相關的欄位
//            if (succeed && !DbHelper.toBoolean(form.getChbNotify()) && form.getMsgkbNotify() != null) {
//                form.getMsgkbNotify().clearNotify();
//            }
//        }
        return response;
    }

    private Integer insertMsgKb(Msgkb msgkb) {
        if (msgkb.getMsgpattern().length() >= 25) {
            msgkb.setMsgpattern(msgkb.getMsgpattern().substring(0, 25));
        }
        return atmService.insertMSGKB(msgkb);
    }

    // 上一筆
    @PostMapping(value = "/atmmon/UI_060620_B/inquiryPrev")
    public String inquiryPrev(@ModelAttribute UI_060620_B_Form form, ModelMap mode) {
        TemporaryRestoreData sessionData = this.getSessionData();
        try {
            if (sessionData.index == 0) {
                WebUtil.putInAttribute(mode, AttributeName.DetailMap, sessionData.thisDate);
                mode.put(ATTRIBUTE_NAME_MSGKBNOTIFY, sessionData.msgkbNotify);
                this.showMessage(mode, MessageType.INFO, FirstRecord);
            } else {
                sessionData.index = sessionData.index - 1;
                getAlertData(sessionData.index);
                bindFormViewData(mode);
            }
        } catch (Exception ex) {
            this.handleInquiryException(mode, ex);
        }
        return Router.UI_060620_B.getView();
    }

    // 下一筆
    @PostMapping(value = "/atmmon/UI_060620_B/inquiryNext")
    public String inquiryNext(@ModelAttribute UI_060620_B_Form form, ModelMap mode) {
        TemporaryRestoreData sessionData = this.getSessionData();
        try {
            if (sessionData.index == sessionData.pageData.getList().size() - 1) {
                WebUtil.putInAttribute(mode, AttributeName.DetailMap, sessionData.thisDate);
                mode.put(ATTRIBUTE_NAME_MSGKBNOTIFY, sessionData.msgkbNotify);
                this.showMessage(mode, MessageType.INFO, LastRecord);
            } else {
                sessionData.index = sessionData.index + 1;
                getAlertData(sessionData.index);
                bindFormViewData(mode);
            }
        } catch (Exception ex) {
            this.handleInquiryException(mode, ex);
        }
        return Router.UI_060620_B.getView();
    }

    /**
     * 查詢明細資料出現異常時, 要預設塞入資料
     *
     * @param mode
     * @param ex
     */
    private void handleInquiryException(ModelMap mode, Exception ex) {
        this.errorMessage(ex, ex.getMessage());
        this.showMessage(mode, MessageType.DANGER, programError);
        WebUtil.putInAttribute(mode, AttributeName.DetailMap, new HashMap<>());
        mode.put(ATTRIBUTE_NAME_MSGKBNOTIFY, new MsgkbNotify());
    }

    private TemporaryRestoreData getSessionData() {
        return WebUtil.getFromSession(SessionKey.TemporaryRestoreData);
    }

    private class TemporaryRestoreData {
        public PageInfo<HashMap<String, Object>> pageData;
        public int index = 0;
        public String arERCODE = "";
        public String txExternalCode = "";
        public String arHOSTNAME = "";
        public String arDATE = "";
        public String arTIME = "";
        public String arSUBSYS = "";
        public String arSYS = "";
        public String arLEVEL = "";
        public String arERDESCRIPTION = "";
        public String arREMARK = "";
        public String txExSubCode = "";
        public HashMap<String, String> thisDate;
        public MsgkbNotify msgkbNotify;
    }

    @PostMapping(value = "/atmmon/UI_060620/download")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> download(@RequestBody UI_060620_Form form, ModelMap mode) {
        this.infoMessage("開始下載檔案, 條件 = [", form.toString(), "]");
        try {
            // List<HashMap<String, Object>> hashMapList = queryDataAll(mode, form);
            // if (CollectionUtils.isEmpty(hashMapList)) {
            //     return this.handleDownloadError("查無資料無法下載!!!");
            // }
            String date = "export";
            if (!"".equals(form.getDtTransactDate())) {
                date = StringUtils.replace(form.getDtTransactDate(), "-", StringUtils.EMPTY);
            }
            this.showMessage(mode, MessageType.INFO, "下載成功");
            // return this.download(StringUtils.join(date, "_ems.csv"), MediaType.APPLICATION_OCTET_STREAM_VALUE, os -> convertListToCsv(os, hashMapList));
            return this.download(StringUtils.join(date, "_ems.csv"), MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    os -> queryDataAndMakeCsv(mode, form, os));
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            return this.handleDownloadException(e);
        }
    }

    private void queryDataAndMakeCsv(ModelMap mode, UI_060620_Form form, OutputStream os) throws Exception {
        String txTransactDate = "";
        String txDateTimeBeg = "";
        String txDateTimeEnd = "";
        String sLevel = "";
        String sAtmNo = "";
        String sIP = "";
        String sApplication = "";
        if (!"".equals(form.getDtTransactDate())) {
            txTransactDate = StringUtils.replace(form.getDtTransactDate(), "-", StringUtils.EMPTY);
        } else {
            String tradingDate = StringUtils.replace(form.getDtTransactDate(), "-", StringUtils.EMPTY);
            form.setDtTransactDate(tradingDate);
            txTransactDate = form.getDtTransactDate();
        }
        if (!"".equals(form.getTxTransactTimeBEG())) {
            txDateTimeBeg = form.getTxTransactTimeBEG();
        } else {
            txDateTimeBeg = "00:00";
            form.setTxTransactTimeBEG("00:00");
        }
        if (!"".equals(form.getTxTransactTimeEND())) {
            txDateTimeEnd = form.getTxTransactTimeEND();
        } else {
            txDateTimeEnd = "23:59";
            form.setTxTransactTimeEND("23:59");
        }
        sAtmNo = form.getTxtATMNo();
        sIP = form.getTxtIP();
        sLevel = "";
        sApplication = form.getApplicationDdl();
        String subsys = form.getSubSystemDdl();
        // 記錄auditLog
        AuditLog auditLog = new AuditLog();
        // 塞入auditLog.action
        auditLog.setAction("下載CSV");
        // 塞入auditLog.params
        if (StringUtils.isNotBlank(form.getTime()))
            auditLog.addParam("畫面更新間隔時間", form.getTime() + "秒");
        if (StringUtils.isNotBlank(form.getSubSystemDdl()))
            auditLog.addParam("子系統", form.getSubSystemDdl());
        if (StringUtils.isNotBlank(form.getDtTransactDate()))
            auditLog.addParam("警示日期:", form.getDtTransactDate());
        if (StringUtils.isNotBlank(form.getTxTransactTimeBEG()) && StringUtils.isNotBlank(form.getTxTransactTimeEND()))
            auditLog.addParam("警示時間起訖", form.getTxTransactTimeBEG() + "~" + StringUtils.isNotBlank(form.getTxTransactTimeEND()));
        if (StringUtils.isNotBlank(form.getTxtATMNo()))
            auditLog.addParam("ATM代號", form.getTxtATMNo());
        if (StringUtils.isNotBlank(form.getApplicationDdl()))
            auditLog.addParam("訊息類別", form.getApplicationDdl());
        // 最後塞入ThreadLocal變量中
        setAuditLog(auditLog);
        String begDateTime = txTransactDate + StringUtils.replace(txDateTimeBeg, ":", StringUtils.EMPTY) + "00";
        String endDateTime = txTransactDate + StringUtils.replace(txDateTimeEnd, ":", StringUtils.EMPTY) + "00";
        RefBoolean queryNoData = new RefBoolean(true);
        // write header
        os.write(0xef);
        os.write(0xbb);
        os.write(0xbf);
        // write field name
        os.write(String.join(",", getFieldNames()).getBytes(StandardCharsets.UTF_8));
        os.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        // query data and write each field value
        atmService.jdbcQueryAlert(subsys, begDateTime, endDateTime, sLevel, sAtmNo, sIP, sApplication, form.getTxtArErdescription(), rs -> {
            try {
                String APMessagexml = DB2Util.readClobData(rs.getClob("AR_MESSAGE"));
                APMessagexml = "<APMessage>" + APMessagexml + "</APMessage>";
                APMessagexml = APMessagexml.replace("<LogData>", "").replace("</LogData>", "");
                // Element root = XmlUtil.dom4jLoad(APMessagexml);
                String root = APMessagexml;
                String[] fields = new String[] {
                        nullToEmptyStr(rs.getString("AR_LEVEL")),
                        FormatUtil.dateTimeFormat(rs.getDate("AR_DATETIME"), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH),
                        FormatUtil.timeFormat(rs.getDate("AR_DATETIME")),
                        "=\"" + nullToEmptyStr(rs.getString("AR_HOSTNAME")) + "\"",
                        "=\"" + nullToEmptyStr(XmlUtil.getChildElementValue(root, "EJ", StringUtils.EMPTY)) + "\"",
                        "=\"" + nullToEmptyStr(XmlUtil.getChildElementValue(root, "MessageId", StringUtils.EMPTY)) + "\"",
                        "=\"" + nullToEmptyStr(XmlUtil.getChildElementValue(root, "Channel", StringUtils.EMPTY)) + "\"",
                        "=\"" + nullToEmptyStr(XmlUtil.getChildElementValue(root, "ATMNo", StringUtils.EMPTY)) + "\"",
                        "=\"" + nullToEmptyStr(XmlUtil.getChildElementValue(root, "STAN", StringUtils.EMPTY)) + "\"",
                        "=\"" + nullToEmptyStr(XmlUtil.getChildElementValue(root, "Bkno", StringUtils.EMPTY)) + "\"",
                        "=\"" + nullToEmptyStr(XmlUtil.getChildElementValue(root, "DesBkno", StringUtils.EMPTY)) + "\"",
                        "=\"" + nullToEmptyStr(XmlUtil.getChildElementValue(root, "TxUser", StringUtils.EMPTY)) + "\"",
                        "=\"" + nullToEmptyStr(rs.getString("AR_ERDESCRIPTION")) + "\""
                };
                // 2025-07-08 Richard modified for [Improper Resource Access Authorization]
                // os.write(String.join(",", fields).getBytes(StandardCharsets.UTF_8));
                IOUtil.write(os, String.join(",", fields).getBytes(StandardCharsets.UTF_8));
                os.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
                queryNoData.set(false);
            } catch (Exception e) {
                if (e instanceof SQLException)
                    throw (SQLException) e;
                else
                    throw ExceptionUtil.createSQLException(e, e.getMessage());
            }
        });
        if (queryNoData.get())
            throw ExceptionUtil.createException("查無資料無法下載!!!");
    }

//    public void convertListToCsv(OutputStream os, List<HashMap<String, Object>> dataList) throws Exception {
//        os.write(0xef);
//        os.write(0xbb);
//        os.write(0xbf);
//        os.write(String.join(",", getFieldNames()).getBytes(StandardCharsets.UTF_8));
//        os.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
//        for (HashMap<String, Object> obj : dataList) {
//            String[] fields = getFieldValues(obj);
//            os.write(String.join(",", fields).getBytes(StandardCharsets.UTF_8));
//            os.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
//        }
//    }

    private String[] getFieldNames() {
        return new String[] {"等級", "日期", "時間", "Server Name", "EJ", "交易代號", "來源", "ATMNO", "STAN", "轉出行", "轉入行", "使用者", "訊息"};
    }

//    private String[] getFieldValues(HashMap<String, Object> hashMap) {
//        SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
//        SimpleDateFormat hms = new SimpleDateFormat("HH:mm:ss");
//        //依合庫需求加上excel指定文字格式的處理
//        return new String[] {
//                nullToEmptyStr(hashMap.get("AR_LEVEL").toString()),
//                ymd.format(hashMap.get("AR_DATETIME")),
//                hms.format(hashMap.get("AR_DATETIME")),
//                "=\"" + nullToEmptyStr(hashMap.get("AR_HOSTNAME").toString()) + "\"",
//                "=\"" + nullToEmptyStr(hashMap.get("EJ").toString()) + "\"",
//                "=\"" + nullToEmptyStr(hashMap.get("MSGID").toString()) + "\"",
//                "=\"" + nullToEmptyStr(hashMap.get("Channel").toString()) + "\"",
//                "=\"" + nullToEmptyStr(hashMap.get("ATMNO").toString()) + "\"",
//                "=\"" + nullToEmptyStr(hashMap.get("STAN").toString()) + "\"",
//                "=\"" + nullToEmptyStr(hashMap.get("SBK").toString()) + "\"",
//                "=\"" + nullToEmptyStr(hashMap.get("DBK").toString()) + "\"",
//                "=\"" + nullToEmptyStr(hashMap.get("TxUser").toString()) + "\"",
//                "\"" + nullToEmptyStr(hashMap.get("AR_ERDESCRIPTION").toString()) + "\""
//        };
//    }
//
//    private List<HashMap<String, Object>> queryDataAll(ModelMap mode, UI_060620_Form form) {
//        String txTransactDate = "";
//        String txDateTimeBeg = "";
//        String txDateTimeEnd = "";
//        String sLevel = "";
//        String sAtmNo = "";
//        String sIP = "";
//        String sApplication = "";
//        if (!"".equals(form.getDtTransactDate())) {
//            txTransactDate = StringUtils.replace(form.getDtTransactDate(), "-", StringUtils.EMPTY);
//        } else {
//            String tradingDate = StringUtils.replace(form.getDtTransactDate(), "-", StringUtils.EMPTY);
//            form.setDtTransactDate(tradingDate);
//            txTransactDate = form.getDtTransactDate();
//        }
//        if (!"".equals(form.getTxTransactTimeBEG())) {
//            txDateTimeBeg = form.getTxTransactTimeBEG();
//        } else {
//            txDateTimeBeg = "00:00";
//            form.setTxTransactTimeBEG("00:00");
//        }
//        if (!"".equals(form.getTxTransactTimeEND())) {
//            txDateTimeEnd = form.getTxTransactTimeEND();
//        } else {
//            txDateTimeEnd = "23:59";
//            form.setTxTransactTimeEND("23:59");
//        }
//        sAtmNo = form.getTxtATMNo();
//        sIP = form.getTxtIP();
//        sLevel = "";
//        sApplication = form.getApplicationDdl();
//        String subsys = form.getSubSystemDdl();
//        // 記錄auditLog
//        AuditLog auditLog = new AuditLog();
//        // 塞入auditLog.action
//        auditLog.setAction("下載CSV");
//        // 塞入auditLog.params
//        if (StringUtils.isNotBlank(form.getTime()))
//            auditLog.addParam("畫面更新間隔時間", form.getTime() + "秒");
//        if (StringUtils.isNotBlank(form.getSubSystemDdl()))
//            auditLog.addParam("子系統", form.getSubSystemDdl());
//        if (StringUtils.isNotBlank(form.getDtTransactDate()))
//            auditLog.addParam("警示日期:", form.getDtTransactDate());
//        if (StringUtils.isNotBlank(form.getTxTransactTimeBEG()) && StringUtils.isNotBlank(form.getTxTransactTimeEND()))
//            auditLog.addParam("警示時間起訖", form.getTxTransactTimeBEG() + "~" + StringUtils.isNotBlank(form.getTxTransactTimeEND()));
//        if (StringUtils.isNotBlank(form.getTxtATMNo()))
//            auditLog.addParam("ATM代號", form.getTxtATMNo());
//        if (StringUtils.isNotBlank(form.getApplicationDdl()))
//            auditLog.addParam("訊息類別", form.getApplicationDdl());
//        // 最後塞入ThreadLocal變量中
//        setAuditLog(auditLog);
//        List<HashMap<String, Object>> dt = getResultDataAll(form, mode, subsys, txTransactDate, txDateTimeBeg, txDateTimeEnd, sLevel, sAtmNo, sIP, sApplication);
//        return dt;
//    }
//
//    private List<HashMap<String, Object>> getResultDataAll(UI_060620_Form form, ModelMap mode, String AR_SUBSYS, String sDate, String sTimeBeg, String sTimeEnd, String sLevel, String sATMNo, String sIP, String sApplication) {
//        try {
//            String begDateTime = sDate + StringUtils.replace(sTimeBeg, ":", StringUtils.EMPTY) + "00";
//            String endDateTime = sDate + StringUtils.replace(sTimeEnd, ":", StringUtils.EMPTY) + "00";
//            List<HashMap<String, Object>> dt = atmService.queryAlertDataAll(AR_SUBSYS, begDateTime, endDateTime, sLevel, sATMNo, sIP, sApplication, form.getTxtArErdescription());
//            if (!dt.isEmpty()) {
//                int tempVar = dt.size();
//                for (int i = 0; i < tempVar; i++) {
//                    String APMessagexml = DB2Util.getClobValue(dt.get(i).get("AR_MESSAGE"), StringUtils.EMPTY);
//                    APMessagexml = "<APMessage>" + APMessagexml + "</APMessage>";
//                    APMessagexml = APMessagexml.replace("<LogData>", "").replace("</LogData>", "");
//                    // Element root = XmlUtil.dom4jLoad(APMessagexml);
//                    String root = APMessagexml;
//                    if (!dt.get(i).containsKey("AR_ERDESCRIPTION")) {
//                        dt.get(i).put("AR_ERDESCRIPTION", "");
//                    }
//                    dt.get(i).put("EJ", XmlUtil.getChildElementValue(root, "EJ", StringUtils.EMPTY));
//                    dt.get(i).put("MSGID", XmlUtil.getChildElementValue(root, "MessageId", StringUtils.EMPTY));
//                    dt.get(i).put("Channel", XmlUtil.getChildElementValue(root, "Channel", StringUtils.EMPTY));
//                    dt.get(i).put("ATMNO", XmlUtil.getChildElementValue(root, "ATMNo", StringUtils.EMPTY));
//                    dt.get(i).put("STAN", XmlUtil.getChildElementValue(root, "STAN", StringUtils.EMPTY));
//                    dt.get(i).put("SBK", XmlUtil.getChildElementValue(root, "Bkno", StringUtils.EMPTY));
//                    dt.get(i).put("DBK", XmlUtil.getChildElementValue(root, "DesBkno", StringUtils.EMPTY));
//                    dt.get(i).put("TxUser", XmlUtil.getChildElementValue(root, "TxUser", StringUtils.EMPTY));
//                }
//            }
//            return dt;
//        } catch (Exception ex) {
//            this.errorMessage(ex, ex.getMessage());
//            this.showMessage(mode, MessageType.DANGER, "讀取資料有誤");
//            return null;
//        }
//    }
}
