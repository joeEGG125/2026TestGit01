package com.syscom.fep.web.controller.atmmon;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.atmmon.UI_060070_Form;
import com.syscom.fep.web.form.atmmon.UI_060070_FormDetail;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

/**
 * 系統管理 共用參數設定
 *
 * @author Han
 */
@Controller
public class UI_060070Controller extends BaseController {

    @Autowired
    private AtmService atmService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        this.showMessage(mode, MessageType.INFO, "");

        // 初始化表單資料
        UI_060070_Form form = new UI_060070_Form();
        form.setSubsysList(atmService.queryAllData(""));
        form.setUrl("/atmmon/UI_060070/queryComfirm");
        this.doKeepFormData(mode, form);
        PageInfo<List<Map<String, String>>> pageInfo = PageHelper
                .startPage(form.getPageNum(), form.getPageSize(), form.getPageNum() > 0 && form.getPageSize() > 0)
                .doSelectPageInfo(new ISelect() {
                    @Override
                    public void doSelect() {
                        bindGridData(form, mode);
                    }
                });
        if (null == pageInfo || pageInfo.getList().size() == 0) {
            this.showMessage(mode, MessageType.WARNING, QueryNoData);
        } else {
            this.showMessage(mode, MessageType.INFO, QuerySuccess);
        }

        PageData<UI_060070_Form, List<Map<String, String>>> pageData = new PageData<>(pageInfo, form);

        WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/atmmon/UI_060070/queryComfirm")
    public String queryComfirm(@ModelAttribute UI_060070_Form form, ModelMap mode) {
        this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
        form.setUrl("/atmmon/UI_060070/queryComfirm");
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        this.doKeepFormData(mode, form);
        try {
            form.setSubsysList(atmService.queryAllData(""));
            PageInfo<List<Map<String, String>>> pageInfo = PageHelper
                    .startPage(form.getPageNum(), form.getPageSize(), form.getPageNum() > 0 && form.getPageSize() > 0)
                    .doSelectPageInfo(new ISelect() {
                        @Override
                        public void doSelect() {
                            bindGridData(form, mode);
                        }
                    });

            if (null == pageInfo || pageInfo.getList().size() == 0) {
                this.showMessage(mode, MessageType.WARNING, QueryNoData);
            } else {
                this.showMessage(mode, MessageType.INFO, QuerySuccess);
            }

            PageData<UI_060070_Form, List<Map<String, String>>> pageData = new PageData<>(pageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
            WebUtil.putInAttribute(mode, AttributeName.Form, form);

            if (null == pageInfo) {
                this.showMessage(mode, MessageType.WARNING, QueryNoData);
            } else {
                this.showMessage(mode, MessageType.INFO, QuerySuccess);
            }

        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_060070.getView();
    }

    @PostMapping(value = "/atmmon/UI_060070/inquiryDetail")
    public String inquiryDetail(@ModelAttribute UI_060070_FormDetail form, ModelMap mode) {
        this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        UI_060070_FormDetail tempForm = new UI_060070_FormDetail();
        try {

            if ("否".equals(form.getSYSCONF_READONLYC().toString())) {

                AuditLog auditLog = new AuditLog();
                auditLog.setAction("查詢");
                if (StringUtils.isNotBlank(form.getSYSCONF_SUBSYSNO()))
                    auditLog.addParam("系統簡稱", form.getSYSCONF_SUBSYSNO());
                if (StringUtils.isNotBlank(form.getSYSCONF_NAME()))
                    auditLog.addParam("變數名稱", form.getSYSCONF_NAME());
                setAuditLog(auditLog);

                Map<String, Object> dt = atmService.querySysConfByPK2(form.getSYSCONF_SUBSYSNO(),
                        form.getSYSCONF_NAME());

                if (null == dt.get("SYSCONF_VALUE") || "".equals(dt.get("SYSCONF_VALUE"))) {
                    form.setSysconfValue("");
                }

                if ("否".equals(dt.get("SYSCONF_ENCRYPT").toString())) {
                    tempForm.setSysconfEncrypt("false");
                } else {
                    tempForm.setSysconfEncrypt("true");
                }

                tempForm.setSysconfSubsysno(dt.get("SYSCONF_SUBSYSNO").toString());
                tempForm.setSysconfName(dt.get("SYSCONF_NAME").toString());
                tempForm.setSysconfRemark(dt.get("SYSCONF_REMARK").toString());
                tempForm.setSysconfType(dt.get("SYSCONF_TYPE").toString());
                tempForm.setSysconfSubsysnoC(dt.get("SYSCONF_SUBSYSNOC").toString());
                tempForm.setSysconfReadonlyC(dt.get("SYSCONF_READONLYC").toString());
                tempForm.setSysconfDatatype(dt.get("SYSCONF_DATATYPE").toString().trim());
                tempForm.setSysconfReadonly(dt.get("SYSCONF_READONLY").toString());
                tempForm.setSysconfDatatype(dt.get("SYSCONF_DATATYPE").toString().trim());
                tempForm.setSysconfValue(dt.get("SYSCONF_VALUE").toString());

                tempForm.setSYSCONF_NAME(form.getSYSCONF_NAME());
                tempForm.setSYSCONF_READONLYC(form.getSYSCONF_READONLYC());
                tempForm.setSYSCONF_SUBSYSNO(form.getSYSCONF_SUBSYSNO());
                tempForm.setPageNum(form.getPageNum());
                tempForm.setPageSize(form.getPageSize());


            } else {
                this.showMessage(mode, MessageType.INFO, "此筆資料不允許修改");
            }
            WebUtil.putInAttribute(mode, AttributeName.Form, tempForm);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
//		finally {
//			tempForm = null;
//		}

        return Router.UI_060070_Detail.getView();
    }

    @PostMapping(value = "/atmmon/UI_060070/updateDetail")
    public String updateDetail(@ModelAttribute UI_060070_FormDetail form, ModelMap mode) {
        this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
        // this.doKeepFormData(mode, form);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        try {
            if ("SSTQ".equals(form.getSubsysNameS())) {
                this.showMessage(mode, MessageType.INFO, "此筆資料不允許修改");
                WebUtil.putInAttribute(mode, AttributeName.Form, form);
                return Router.UI_060070_Detail.getView();
            }
            // 因為db撈出來時把值 as 成 是&否  進去要改回0,1
            if ("false".equals(form.getSysconfEncrypt())) {
                form.setSysconfEncrypt("0");
            }
            if ("true".equals(form.getSysconfEncrypt())) {
                form.setSysconfEncrypt("1");
            }
            if ("否".equals(form.getSysconfReadonly())) {
                form.setSysconfReadonly("0");
            }
            if ("是".equals(form.getSysconfReadonly())) {
                form.setSysconfReadonly("1");
            }

            AuditLog auditLog = new AuditLog();
            auditLog.setAction("變更");
            if (StringUtils.isNotBlank(form.getSysconfSubsysno()))
                auditLog.addParam("系統簡稱", form.getSysconfSubsysno());
            if (StringUtils.isNotBlank(form.getSysconfName()))
                auditLog.addParam("變數名稱", form.getSysconfName());
            if (StringUtils.isNotBlank(form.getSysconfName()))
                auditLog.addParam("變數名稱", form.getSysconfName());
            if (StringUtils.isNotBlank(form.getSysconfType()))
                auditLog.addParam("變數型態", form.getSysconfDatatype());
            if (StringUtils.isNotBlank(form.getSysconfDatatype()))
                auditLog.addParam("設定類別", form.getSysconfType());
            if (StringUtils.isNotBlank(form.getSysconfValue()))
                auditLog.addParam("變數值", form.getSysconfValue());
            if (StringUtils.isNotBlank(form.getSysconfReadonly()))
                auditLog.addParam("唯讀", form.getSysconfReadonly());
            if (StringUtils.isNotBlank(form.getSysconfRemark()))
                auditLog.addParam("說明", form.getSysconfRemark());
            setAuditLog(auditLog);

            // todo 如果空值就塞空值，還需判斷嗎,因為也能改成空值 defSYSCONF.vb -> Function
            // UpdateByPrimaryKey(原始檔function)
            atmService.updateSysConf(Integer.parseInt(form.getSysconfSubsysno()), form.getSysconfName(),
                    form.getSysconfValue(), form.getSysconfRemark(), form.getSysconfType(), Integer.parseInt(form.getSysconfReadonly()), form.getSysconfDatatype());
            this.showMessage(mode, MessageType.INFO, UpdateSuccess);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, UpdateFail);
        }
        return Router.UI_060070_Detail.getView();
    }

    private List<Map<String, String>> bindGridData(UI_060070_Form form, ModelMap mode) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("查詢");
        if (StringUtils.isNotBlank(form.getSysconfSubsysno()))
            auditLog.addParam("系統簡稱", form.getSysconfSubsysno());
        if (StringUtils.isNotBlank(form.getSysconfName()))
            auditLog.addParam("變數名稱", form.getSysconfName());
        setAuditLog(auditLog);

        return atmService.querySysConfByPK(form.getSysconfSubsysno(), form.getSysconfName());
    }

}
