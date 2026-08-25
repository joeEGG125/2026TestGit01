package com.syscom.fep.web.controller.atmmon;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.mybatis.model.Msgkb;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.atmmon.MsgkbNotify;
import com.syscom.fep.web.form.atmmon.UI_060621_FormDetail;
import com.syscom.fep.web.form.atmmon.UI_060621_FormMain;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 事件管理系統(EMS)日誌通知維護
 */
@Controller
public class UI_060621Controller extends BaseController {

    @Autowired
    private AtmService atmService;

    private final int pageSize = 20;

    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_060621_FormMain form = new UI_060621_FormMain();
        Map<String, Object> argsMap = form.toMap();
        argsMap.put("pageSize", pageSize);
        form.setUrl("/atmmon/UI_060621/bindGrid");
        this.bindGridData(form, argsMap, mode);
    }

    /**
     * 查詢按鈕
     *
     * @param form
     * @param mode
     * @return
     */
    @PostMapping(value = "/atmmon/UI_060621/bindGrid")
    public String bindGrid(@ModelAttribute UI_060621_FormMain form, ModelMap mode) {
        Map<String, Object> argsMap = form.toMap();
        argsMap.put("pageSize", pageSize);
        this.bindGridData(form, argsMap, mode);
        return Router.UI_060621.getView();
    }

    /**
     * 查詢
     *
     * @param argsMap
     * @param mode
     * @return
     */
    private void bindGridData(UI_060621_FormMain form, Map<String, Object> argsMap, ModelMap mode) {
        this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);// 保存當前表單資料
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction("查詢");
            if (StringUtils.isNotBlank(form.getErrorcode())) {
                auditLog.addParam("錯誤代碼", form.getErrorcode());
            }
            if (StringUtils.isNotBlank(form.getExternalcode())) {
                auditLog.addParam("輸出代碼", form.getExternalcode());
            }
            if (form.getNotify() != null) {
                auditLog.addParam("通知", form.getNotify());
            }
            setAuditLog(auditLog);
            PageInfo<Msgkb> pageInfo = atmService.getMsgkbList(argsMap);
            for (Msgkb msgkb : pageInfo.getList()) {
                if (StringUtils.isBlank(msgkb.getMsgpattern()))
                    msgkb.setMsgpattern("連接");
                MsgkbNotify msgkbNotify = new MsgkbNotify();
                msgkbNotify.getNotifyFromMsgkb(msgkb, true);
                msgkb.setNotifymail(msgkbNotify.getMsgkbNotifymail());
                msgkb.setMsgkbNotifyphone(msgkbNotify.getMsgkbNotifyphone());
            }
            if (pageInfo.getList().isEmpty()) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageInfo);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    /**
     * @param form
     * @param mode
     * @return
     */
    @PostMapping(value = "/atmmon/UI_060621/bindGridDetail")
    public String bindGridDetail(@ModelAttribute UI_060621_FormDetail form, ModelMap mode, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("查詢明細資料");
        auditLog.addParam("PKEY", form.getMsgkbNo());
        setAuditLog(auditLog);
        Msgkb msgkb = atmService.getMsgkb(form.getMsgkbNo());
        if (msgkb == null) {
            this.showMessage(redirectAttributes, MessageType.DANGER, "查無明細資料");
            return this.doRedirectForPrevPage(redirectAttributes, request);
        }
        form.setMsgkbNo(msgkb.getMsgkbNo());
        form.setSeverity(msgkb.getSeverity());
        form.setNotify(DbHelper.toBoolean(msgkb.getNotify()));
        form.setResponsible(msgkb.getResponsible());
        form.setAction(msgkb.getAction());
        form.setMsgpattern(msgkb.getMsgpattern());
        // 從msgbk中取出notify mail phone相關信息
        MsgkbNotify msgkbNotify = new MsgkbNotify();
        msgkbNotify.getNotifyFromMsgkb(msgkb, false);
        form.setMsgkbNotify(msgkbNotify);
        this.doKeepFormData(mode, form);
        return Router.UI_060621_Detail.getView();
    }

    @PostMapping(value = "/atmmon/UI_060621/updateDetail")
    public String updateDetail(@ModelAttribute UI_060621_FormDetail form, ModelMap mode) {
        this.infoMessage("執行更新動作, 條件 = [", form.toString(), "]");
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        boolean succeed = false;
        try {
            Msgkb msgkb = new Msgkb();
            msgkb.setNotifymail(StringUtils.EMPTY); // 預設塞入空串, 后面form.getMsgkbNotify().setNotifyToMsgkb时会塞入
            msgkb.setMsgkbNotifyphone(StringUtils.EMPTY); // 預設塞入空串, 后面form.getMsgkbNotify().setNotifyToMsgkb时会塞入
            // 保存時, 將mail和phone存入msgkb對應的欄位中
            if (form.getMsgkbNotify() != null)
                form.getMsgkbNotify().setNotifyToMsgkb(msgkb);
            // 若勾選自動通知,則電子郵件一欄必須有資料
            if (form.getNotify()) {
                if (form.getMsgkbNotify().isFepNotifyMail_Customize()) {
                    if (StringUtils.isBlank(form.getMsgkbNotify().getMsgkbNotifymail())) {
                        this.showMessage(mode, MessageType.DANGER, "未輸入自定Mail通知");
                        return Router.UI_060621_Detail.getView();
                    } else {
                        RefString invalidMail = new RefString();
                        if (!form.getMsgkbNotify().isFepNotifyMailCustomizeValid(invalidMail)) {
                            this.showMessage(mode, MessageType.DANGER, "自定Mail通知", String.format(Const.KEY_WORDS_IN_MESSAGE_S, invalidMail.get()), "格式不正確");
                            return Router.UI_060621_Detail.getView();
                        }
                    }
                }
                if (form.getMsgkbNotify().isFepNotifyPhone_Customize() && StringUtils.isBlank(form.getMsgkbNotify().getMsgkbNotifyphone())) {
                    this.showMessage(mode, MessageType.DANGER, "未輸入自定簡訊通知");
                    return Router.UI_060621_Detail.getView();
                } else if (StringUtils.isBlank(msgkb.getNotifymail()) && StringUtils.isBlank(msgkb.getMsgkbNotifyphone())) {
                    this.showMessage(mode, MessageType.DANGER, "Mail或簡訊通知至少有一個欄位必須有值");
                    return Router.UI_060621_Detail.getView();
                }
            }
//            else {
//                if (form.getMsgkbNotify().isFepNotifyMail_APD() || form.getMsgkbNotify().isFepNotifyMail_SYS() || form.getMsgkbNotify().isFepNotifyMail_Customize() ||
//                        form.getMsgkbNotify().isFepNotifyPhone_APD() || form.getMsgkbNotify().isFepNotifyPhone_SYS() || form.getMsgkbNotify().isFepNotifyPhone_Customize()) {
//                    this.showMessage(mode, MessageType.DANGER, String.format(Const.KEY_WORDS_IN_MESSAGE_S, "是否通知"), "未勾選");
//                    return Router.UI_060621_Detail.getView();
//                }
//            }
            if (StringUtils.isBlank(form.getMsgpattern())) {
                this.showMessage(mode, MessageType.DANGER, "訊息關鍵字不可為空");
                return Router.UI_060621_Detail.getView();
            }
            // msgkb
            msgkb.setMsgkbNo(form.getMsgkbNo());
            msgkb.setErrorcode(null); // 這個欄位不用更新, 所以要塞入null
            msgkb.setExternalcode(null); // 這個欄位不用更新, 所以要塞入null
            msgkb.setSeverity(null); // 這個欄位不用更新, 所以要塞入null
            msgkb.setDescription(null); // 這個欄位不用更新, 所以要塞入null
            msgkb.setRemark(null); // 這個欄位不用更新, 所以要塞入null
            msgkb.setVisible(null); // 這個欄位不用更新, 所以要塞入null
            msgkb.setUicolor(null); // 這個欄位不用更新, 所以要塞入null
            msgkb.setExsubcode(null); // 這個欄位不用更新, 所以要塞入null
            msgkb.setNotify(DbHelper.toShort(form.getNotify()));
            msgkb.setResponsible(form.getResponsible());
            msgkb.setAction(form.getAction());
            msgkb.setMsgpattern(form.getMsgpattern());
            // AuditLog
            AuditLog auditLog = new AuditLog();
            auditLog.setAction("更新");
            auditLog.addParam("PKEY", msgkb.getMsgkbNo());
            auditLog.addParam("是否通知", msgkb.getNotify());
            auditLog.addParam("負責人", msgkb.getResponsible());
            auditLog.addParam("處理方法", msgkb.getAction());
            auditLog.addParam("訊息關鍵字", msgkb.getMsgpattern());
            auditLog.addParam("電子郵件", msgkb.getNotifymail());
            auditLog.addParam("通知手機", msgkb.getMsgkbNotifyphone());
            setAuditLog(auditLog);
            if (atmService.updateMSGKB(msgkb) > 0) {
                this.showMessage(mode, MessageType.SUCCESS, UpdateSuccess);
            } else {
                this.showMessage(mode, MessageType.DANGER, UpdateFail);
            }
            succeed = true;
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
//        finally {
//            // 如果UI上勾選不通知, 則要清除重置表單中的通知相關的欄位
//            if (succeed && !form.getNotify() && form.getMsgkbNotify() != null) {
//                form.getMsgkbNotify().clearNotify();
//            }
//        }
        return Router.UI_060621_Detail.getView();
    }

    @PostMapping(value = "/atmmon/UI_060621/btnDelete")
    @ResponseBody
    public BaseResp<UI_060621_FormMain> btnDelete(@RequestBody List<UI_060621_FormMain> list) {
        this.infoMessage("執行刪除動作, 條件 = [", list.toString(), "]");
        BaseResp<UI_060621_FormMain> response = new BaseResp<>();
        try {
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("刪除");
            auditLog.addParam("PKEY", StringUtils.join(list.stream().map(UI_060621_FormMain::getMsgkbNo).collect(Collectors.toList()), ","));
            setAuditLog(auditLog);
            Msgkb msgkb = new Msgkb();
            for (UI_060621_FormMain form : list) {
                msgkb.setMsgkbNo(form.getMsgkbNo());
                atmService.deleteMSGKB(msgkb);
            }
            response.setMessage(MessageType.INFO, DeleteSuccess);
            //清空查詢條件
            UI_060621_FormMain f = WebUtil.getUser().getCurrentPageForm();
            UI_060621_FormMain nf = new UI_060621_FormMain();
            nf.setUrl(f.getUrl());
            BeanUtils.copyProperties(nf, f);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            response.setMessage(MessageType.DANGER, DeleteFail);
        }
        return response;
    }
}
