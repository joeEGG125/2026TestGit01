package com.syscom.fep.web.controller.dbmaintain;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.mybatis.model.Channel;
import com.syscom.fep.mybatis.model.Msgfile;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.entity.dbmaintain.MsgfileNotify;
import com.syscom.fep.web.entity.dbmaintain.MsgfileTmp;
import com.syscom.fep.web.form.dbmaintain.UI_070060_FormDetail;
import com.syscom.fep.web.form.dbmaintain.UI_070060_FormMain;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.ChannelService;
import com.syscom.fep.web.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 錯誤訊息定義資料維護
 *
 * @author bruce
 */
@Controller
public class UI_070060Controller extends BaseController {
    @Autowired
    private AtmService atmService;
    @Autowired
    private ChannelService channelService;

    private final String pleaseChoose = "所有";
    private HashMap<String, String> channelMaps = new HashMap<>();

    private String webType = WebConfiguration.getInstance().getWebType();

    private final String regx = "^[A-Za-z0-9+_.-]+@(.+)$";

    private final String ERRORCODE = "訊息代碼";

    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_070060_FormMain form = new UI_070060_FormMain();
        Map<String, Object> argsMap = form.toMap();
        argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
        //'Fly 2018/02/14 SSTQ系統時取消新增/修改/刪除功能
        form.setWebType(this.webType);
        form.setUrl("/dbmaintain/UI_070060/bindGrid");
        this.bindGridData(form, argsMap, mode);
    }

    /**
     * 查詢按鈕
     *
     * @param form
     * @param mode
     * @return
     */
    @PostMapping(value = "/dbmaintain/UI_070060/bindGrid")
    public String bindGrid(@ModelAttribute UI_070060_FormMain form, ModelMap mode) {
        Map<String, Object> argsMap = form.toMap();
        argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
        this.bindGridData(form, argsMap, mode);
        return Router.UI_070060.getView();
    }

    /**
     * 新增按鈕
     *
     * @param form
     * @param mode
     * @return
     */
    @PostMapping(value = "/dbmaintain/UI_070060/insertClick")
    public String insertClick(@ModelAttribute UI_070060_FormDetail form, ModelMap mode) {
        try {
            this.setChannelOptions(mode);
            this.setSubsysOptions(mode);
            form.setNotify(new MsgfileNotify());
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        this.doKeepFormData(mode, form);
        return Router.UI_070060_Detail.getView();
    }

    /**
     * 儲存按鈕
     *
     * @param form
     * @param mode
     * @return
     */
    @PostMapping(value = "/dbmaintain/UI_070060/saveClick")
    public String saveClick(@ModelAttribute UI_070060_FormDetail form, ModelMap mode) {
        this.infoMessage("執行更新動作, 條件 = [", form.toString(), "]");
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        try {
            this.setChannelOptions(mode);
            this.setSubsysOptions(mode);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
            return Router.UI_070060_Detail.getView();
        }
        form.setUrl("/dbmaintain/UI_070060/bindGrid");
        Msgfile msgfile = this.setData(form);
        msgfile.setMsgfileResponsible(StringUtils.EMPTY); // 預設塞入空串, 后面form.getNotify().setNotifyToMsgfile时会塞入
        msgfile.setMsgfileNotifyphone(StringUtils.EMPTY); // 預設塞入空串, 后面form.getNotify().setNotifyToMsgfile时会塞入
        // 保存時, 將mail和phone存入msgkb對應的欄位中
        if (form.getNotify() != null)
            form.getNotify().setNotifyToMsgfile(msgfile);
        String errorMsg = this.checkAllField(form, msgfile);
        if (StringUtils.isBlank(errorMsg)) {
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.params
            if (StringUtils.isNotBlank(form.getMsgfileChannel()))
                auditLog.addParam("來源通道", form.getMsgfileChannel());
            if (StringUtils.isNotBlank(form.getMsgfileErrorcode()))
                auditLog.addParam("訊息代碼:", form.getMsgfileErrorcode());
            if (StringUtils.isNotBlank(form.getMsgfileSubsys()))
                auditLog.addParam("子系統", form.getMsgfileSubsys());
            if (StringUtils.isNotBlank(form.getMsgfileSeverity()))
                auditLog.addParam("訊息嚴重性", form.getMsgfileSeverity());
            if (StringUtils.isNotBlank(form.getMsgfileSendEms()))
                auditLog.addParam("是否送事件監控", form.getMsgfileSendEms());
            if (StringUtils.isNotBlank(form.getMsgfileRetain()))
                auditLog.addParam("留置卡片FLAG", form.getMsgfileRetain());
            if (StringUtils.isNotBlank(form.getMsgfileAuth()))
                auditLog.addParam("是否需要授權", form.getMsgfileAuth());
            if (StringUtils.isNotBlank(form.getMsgfileWarning()))
                auditLog.addParam("是否要提示", form.getMsgfileWarning());
            if (StringUtils.isNotBlank(form.getMsgfileExternal()))
                auditLog.addParam("輸出訊息代碼", form.getMsgfileExternal());
            if (StringUtils.isNotBlank(form.getMsgfileFisc()))
                auditLog.addParam("財金訊息代碼", form.getMsgfileFisc());
            if (StringUtils.isNotBlank(form.getMsgfileAtm()))
                auditLog.addParam("ATM訊息代碼", form.getMsgfileAtm());
            if (StringUtils.isNotBlank(form.getMsgfileUatmp()))
                auditLog.addParam("CBS訊息代碼", form.getMsgfileUatmp());
            if (StringUtils.isNotBlank(form.getMsgfileT24()))
                auditLog.addParam("CBS主機訊息代碼", form.getMsgfileT24());
            if (StringUtils.isNotBlank(form.getMsgfileCredit()))
                auditLog.addParam("信用卡訊息代號", form.getMsgfileCredit());
            if (StringUtils.isNotBlank(form.getMsgfileShortmsg()))
                auditLog.addParam("訊息簡述", form.getMsgfileShortmsg());
            if (StringUtils.isNotBlank(form.getMsgfileMsgdscpt()))
                auditLog.addParam("訊息內容", form.getMsgfileMsgdscpt());
            if (StringUtils.isNotBlank(form.getMsgfileAction()))
                auditLog.addParam("訊息處理說明", form.getMsgfileAction());
            if (StringUtils.isNotBlank(form.getMsgfileNotify()))
                auditLog.addParam("是否通知", form.getMsgfileNotify());
            if (StringUtils.isNotBlank(msgfile.getMsgfileResponsible()))
                auditLog.addParam("負責人EMail", msgfile.getMsgfileResponsible());
            if (StringUtils.isNotBlank(msgfile.getMsgfileNotifyphone()))
                auditLog.addParam("簡訊通知", msgfile.getMsgfileNotifyphone());
            boolean succeed = true;
            try {
                if ("E".equals(form.getActionType())) {
                    // 塞入auditLog.action
                    auditLog.setAction("修改");
                    // 最後塞入ThreadLocal變量中
                    setAuditLog(auditLog);
                    succeed = atmService.updateMsgFile(msgfile) > 0;
                    this.showMessage(mode, MessageType.INFO, UpdateSuccess);
                } else {
                    // 塞入auditLog.action
                    auditLog.setAction("新增");
                    // 最後塞入ThreadLocal變量中
                    setAuditLog(auditLog);
                    int ret = atmService.insertMsgFile(msgfile);
                    succeed = ret > 0;
                    if (ret > 0) {
                        this.showMessage(mode, MessageType.INFO, InsertSuccess);
                    } else if (ret == -1) {
                        this.showMessage(mode, MessageType.DANGER, Multiple);
                    } else {
                        this.showMessage(mode, MessageType.DANGER, InsertFail);
                    }
                }
            } catch (Exception e) {
                succeed = false;
                this.errorMessage(e, e.getMessage());
                this.showMessage(mode, MessageType.DANGER, programError);
            }
//            finally {
//                // 如果UI上勾選不通知, 則要清除重置表單中的通知相關的欄位
//                if (succeed && !form.isMsgfileNotifyB() && form.getNotify() != null) {
//                    form.getNotify().clearNotify();
//                }
//            }
        } else {
            this.showMessage(mode, MessageType.DANGER, errorMsg);
            WebUtil.putInAttribute(mode, AttributeName.Form, form);
        }
        return Router.UI_070060_Detail.getView();
    }

    /**
     * 來源通道超連結
     *
     * @param form
     * @param mode
     * @return
     */
    @PostMapping(value = "/dbmaintain/UI_070060/bindGridDetail")
    public String bindGridDetail(@ModelAttribute UI_070060_FormDetail form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        try {
            this.setChannelOptions(mode);
            this.setSubsysOptions(mode);
            //轉成布林值
            form.setMsgfileSendEmsB(DbHelper.toBoolean(form.getMsgfileSendEms()));
            form.setMsgfileRetainB(DbHelper.toBoolean(form.getMsgfileRetain()));
            form.setMsgfileAuthB(DbHelper.toBoolean(form.getMsgfileAuth()));
            form.setMsgfileWarningB(DbHelper.toBoolean(form.getMsgfileWarning()));
            form.setMsgfileNotifyB(DbHelper.toBoolean(form.getMsgfileNotify()));
            // 從form中取出notify responsible phone相關信息
            Msgfile msgfile = new Msgfile();
            msgfile.setMsgfileNotifyphone(form.getMsgfileNotifyphone());
            msgfile.setMsgfileResponsible(form.getMsgfileResponsible());
            MsgfileNotify msgfileNotify = new MsgfileNotify();
            msgfileNotify.getNotifyFromMsgfile(msgfile, false);
            form.setNotify(msgfileNotify);
            this.doKeepFormData(mode, form);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_070060_Detail.getView();
    }

    /**
     * 查詢
     *
     * @param argsMap
     * @param mode
     * @return
     */
    private void bindGridData(UI_070060_FormMain form, Map<String, Object> argsMap, ModelMap mode) {
        this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);// 保存當前表單資料
        try {
            this.setChannelOptions(mode);
            this.setSubsysOptions(mode);
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("查詢");
            // 塞入auditLog.params
            if (StringUtils.isNotBlank(form.getMsgfileChannel()))
                auditLog.addParam("來源通道", form.getMsgfileChannel());
            if (StringUtils.isNotBlank(form.getMsgfileErrorcode()))
                auditLog.addParam("訊息代碼:", form.getMsgfileErrorcode());
            if (StringUtils.isNotBlank(form.getMsgfileSubsys()))
                auditLog.addParam("子系統", form.getMsgfileSubsys());
            if (StringUtils.isNotBlank(form.getMsgfileSeverity()))
                auditLog.addParam("訊息嚴重性", form.getMsgfileSeverity());
            if (StringUtils.isNotBlank(form.getMsgfileShortmsg()))
                auditLog.addParam("訊息簡述", form.getMsgfileShortmsg());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);

            PageInfo<Msgfile> pageInfo = atmService.queryByDefJoinChannel(argsMap);
            //將Msgfile改成使用MsgfileTmp
            PageInfo<MsgfileTmp> newPageInfo = this.changeObject(pageInfo);
            BeanUtils.copyProperties(pageInfo, newPageInfo, "list");
            if (newPageInfo.getSize() == 0) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            } else {
                this.changeFieldContent(newPageInfo);
                this.showMessage(mode, MessageType.INFO, QuerySuccess);
            }
            PageData<UI_070060_FormMain, MsgfileTmp> pageData = new PageData<UI_070060_FormMain, MsgfileTmp>(newPageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    /**
     * 儲存時檢核欄位是否符合規格
     *
     * @param form
     * @return
     */
    private String checkAllField(UI_070060_FormDetail form, Msgfile msgfile) {
        if (StringUtils.isBlank(form.getMsgfileErrorcode())) {
            return this.ERRORCODE + HasData;
        }
        if (form.isMsgfileNotifyB()) {
            if (form.getNotify().isFepNotifyMail_Customize()) {
                if (StringUtils.isBlank(form.getNotify().getMsgfileResponsible())) {
                    return "未輸入自定負責人Mail通知";
                } else {
                    RefString invalidMail = new RefString();
                    if (!form.getNotify().isFepNotifyMailCustomizeValid(invalidMail)) {
                        return StringUtils.join("自定負責人Mail通知", String.format(Const.KEY_WORDS_IN_MESSAGE_S, invalidMail.get()), "格式不正確");
                    }
                }
            }
            if (form.getNotify().isFepNotifyPhone_Customize() && StringUtils.isBlank(form.getNotify().getMsgfileNotifyphone())) {
                return "未輸入自定簡訊通知";
            } else if (StringUtils.isBlank(msgfile.getMsgfileResponsible()) && StringUtils.isBlank(msgfile.getMsgfileNotifyphone())) {
                return "負責人Mail或簡訊通知至少有一個欄位必須有值";
            }
        }
//        else {
//            if (form.getNotify().isFepNotifyMail_APD() || form.getNotify().isFepNotifyMail_SYS() || form.getNotify().isFepNotifyMail_Customize() ||
//                    form.getNotify().isFepNotifyPhone_APD() || form.getNotify().isFepNotifyPhone_SYS() || form.getNotify().isFepNotifyPhone_Customize()) {
//                return StringUtils.join(String.format(Const.KEY_WORDS_IN_MESSAGE_S, "是否通知"), "未勾選");
//            }
//        }
        return null;
    }

    /**
     * 因為要將物件欄位做二次加工，所以新增一個MsgfileTmp繼承Msgfile
     *
     * @param pageInfo
     * @return
     */
    private PageInfo<MsgfileTmp> changeObject(PageInfo<Msgfile> pageInfo) {
        List<Msgfile> msgfileList = pageInfo.getList();
        List<MsgfileTmp> msgfileTmpList = new ArrayList<>(msgfileList.size());
        for (Msgfile msgfile : msgfileList) {
            MsgfileTmp msgfileTmp = new MsgfileTmp(msgfile);
            msgfileTmpList.add(msgfileTmp);
        }
        return PageInfo.of(msgfileTmpList);
    }

    /**
     * 改變特定欄位內容
     *
     * @param pageInfo
     */
    private void changeFieldContent(PageInfo<MsgfileTmp> pageInfo) {
        for (MsgfileTmp msgfile : pageInfo.getList()) {
            msgfile.setMsgfileChannelTxt(this.getChannelName(String.valueOf(msgfile.getMsgfileChannel())));//來源通道
            msgfile.setMsgfileSubsysTxt(this.getSubSystemName(String.valueOf(msgfile.getMsgfileSubsys())));//子系統
            msgfile.setMsgfileSendEmsTxt(this.getSendEmsToCh(String.valueOf(msgfile.getMsgfileSendEms())));//送事件監控
        }
    }

    /**
     * 將送事件監控轉成"是"或"否"
     *
     * @param sendEms
     * @return
     */
    private String getSendEmsToCh(String sendEms) {
        switch (sendEms) {
            case "0": return "否";
            case "1": return "是";
            default: return "";
        }
    }

    /**
     * 將子系統轉換成英文
     *
     * @param subsys
     * @return
     */
    private String getSubSystemName(String subsys) {
        if (StringUtils.isBlank(subsys)) {
            return "";
        } else {
            switch (subsys) {
                case "0": return "None";
                case "1": return "INBK";
                case "2": return "RM";
                case "3": return "ATMP";
                case "4": return "CARD";
                case "5": return "HSM";
                case "6": return "MON";
                case "7": return "RECS";
                case "8": return "GW";
                case "9": return "CMN";
                default: return subsys;
            }
        }
    }

    /**
     * 將來源通道轉換成英文
     *
     * @param channel
     * @return
     */
    private String getChannelName(String channel) {
        if (channelMaps.containsKey(channel)) {
            return channelMaps.get(channel);
        }
        return "";
    }

    /**
     * 將資料塞Msgfile
     *
     * @param form
     * @return
     */
    private Msgfile setData(UI_070060_FormDetail form) {
        MsgfileTmp msgfile = new MsgfileTmp();
        if ("I".equals(form.getActionType())) {
            msgfile.setMsgfileSubsys(Integer.parseInt(form.getMsgfileSubsys()));
            msgfile.setMsgfileRetain((short) (form.isMsgfileRetainB() ? 1 : 0));
            msgfile.setMsgfileAuth((short) (form.isMsgfileAuthB() ? 1 : 0));
            msgfile.setMsgfileWarning((short) (form.isMsgfileWarningB() ? 1 : 0));
            msgfile.setMsgfileExternal(form.getMsgfileExternal());
            msgfile.setMsgfileFisc(form.getMsgfileFisc());
            msgfile.setMsgfileAtm(form.getMsgfileAtm());
            msgfile.setMsgfileUatmp(form.getMsgfileUatmp());
            msgfile.setMsgfileT24(form.getMsgfileT24());
            msgfile.setMsgfileCredit(form.getMsgfileCredit());
            msgfile.setMsgfileVisible((short) 1);
        }
        //update及insert都要的欄位
        msgfile.setMsgfileChannel(Integer.parseInt(form.getMsgfileChannel()));
        msgfile.setMsgfileErrorcode(form.getMsgfileErrorcode());
        msgfile.setMsgfileSeverity(this.checkField(form.getMsgfileSeverity()));
        msgfile.setMsgfileSendEms((short) (form.isMsgfileSendEmsB() ? 1 : 0));
        msgfile.setMsgfileShortmsg(this.checkField(form.getMsgfileShortmsg()));
        msgfile.setMsgfileMsgdscpt(this.checkField(form.getMsgfileMsgdscpt()));
        msgfile.setMsgfileAction(this.checkField(form.getMsgfileAction()));
        msgfile.setMsgfileResponsible(this.checkField(form.getMsgfileResponsible()));
        msgfile.setMsgfileNotify((short) (form.isMsgfileNotifyB() ? 1 : 0));
        msgfile.setUpdateUserid(Integer.parseInt(WebUtil.getUser().getUserId()));
        return msgfile;
    }

    /**
     * 如果欄位是空的就放空字串
     *
     * @param field
     * @return
     */
    private String checkField(String field) {
        if (StringUtils.isBlank(field)) {
            return "";
        } else {
            return field;
        }
    }

    private void setChannelOptions(ModelMap mode) throws Exception {
        if (channelMaps.size() != 0) {
            channelMaps = new HashMap<>();
        }
        List<Channel> channelList = channelService.queryAllDataByCE();
        List<SelectOption<String>> selectOptionList = new ArrayList<SelectOption<String>>();
        selectOptionList.add(new SelectOption<String>(this.pleaseChoose, StringUtils.EMPTY));
        for (int i = 0; i < channelList.size(); i++) {
            String name = channelList.get(i).getChannelName();
            String channelno = channelList.get(i).getChannelChannelno().toString();
            selectOptionList.add(new SelectOption<String>(name, channelno));
            channelMaps.put(channelno, name);
        }
        WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList);
    }

    private void setSubsysOptions(ModelMap mode) throws Exception {
        List<SelectOption<String>> selectOptionList = new ArrayList<SelectOption<String>>();
        selectOptionList.add(new SelectOption<String>("全部", StringUtils.EMPTY));
        selectOptionList.add(new SelectOption<String>("None", "0"));
        selectOptionList.add(new SelectOption<String>("INBK", "1"));
        selectOptionList.add(new SelectOption<String>("RM", "2"));
        selectOptionList.add(new SelectOption<String>("ATMP", "3"));
        selectOptionList.add(new SelectOption<String>("CARD", "4"));
        selectOptionList.add(new SelectOption<String>("HSM", "5"));
        selectOptionList.add(new SelectOption<String>("MON", "6"));
        selectOptionList.add(new SelectOption<String>("RECS", "7"));
        selectOptionList.add(new SelectOption<String>("GW", "8"));
        selectOptionList.add(new SelectOption<String>("CMN", "9"));
        WebUtil.putInAttribute(mode, AttributeName.Options2, selectOptionList);
    }
}
