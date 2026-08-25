package com.syscom.fep.web.controller.common;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.ext.model.ReportdepExt;
import com.syscom.fep.mybatis.model.Reportdep;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.common.UI_130900_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.CommonService;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.mybatis.model.Syscomuser;

/**
 * UI130900 報表分行維護??????????????????
 *
 * @author Joe
 * @create 2024/05/09
 */
@Controller
public class UI_130900Controller extends BaseController {
    private static final String URL_DO_QUERY = "/common/UI_130900/queryClick";

    @Autowired
    private CommonService commonService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_130900_Form form = new UI_130900_Form();
        form.setUrl(URL_DO_QUERY);
        this.queryClick(form, mode);
    }

    @PostMapping(value = URL_DO_QUERY)
    private String queryClick(@ModelAttribute UI_130900_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        bindGrid(form, mode, "ASC");
        return Router.UI_130900.getView();
    }

    /**
     * 設定畫面Button Delete的Event。
     * 透過CheckBox選取刪除者，請務必注意aspx中CheckBox要加入Javascript的DeleteChk_click，該function請參考aspx最下面的Code
     */
    @RequestMapping(value = "/common/UI_130900/btnDelete")
    @ResponseBody
    private BaseResp<UI_130900_Form> deleteClick(@RequestBody UI_130900_Form dForm) throws Exception {
        BaseResp<UI_130900_Form> response = new BaseResp<>();
        Reportdep reportdep = new Reportdep();
        reportdep.setReportdepDepno(dForm.getReportDep_depNo());
       
        commonService.deleteBank(reportdep);
        response.setData(dForm);
        return response;
    }

    @PostMapping(value = "/common/UI_130900/showDetail")
    private String showDetail(@ModelAttribute UI_130900_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        form.setBtnType(nullToEmptyStr(form.getBtnType()));
        form.setReportDep_depNo(nullToEmptyStr(form.getReportDep_depNo()));
        form.setReportDep_depName(nullToEmptyStr(form.getReportDep_depName()));

        if (!"insert".equals(form.getBtnType())) {
            Reportdep reportdep = bindFormViewData(form.getReportDep_depNo());
            form.setBtnType(nullToEmptyStr(form.getBtnType()));
            form.setReportDep_depName(nullToEmptyStr(reportdep.getReportdepDepname()));
            form.setReportDep_type(nullToEmptyStr(reportdep.getReportdepType()));
        }
        
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        return Router.UI_130900_Detail.getView();
    }

    @PostMapping(value = "/common/UI_130900/saveClick")
    private String saveClick(@ModelAttribute UI_130900_Form form, ModelMap mode, RedirectAttributes redirectAttributes, HttpServletRequest request) throws ParseException {
        this.infoMessage("保存分行, 條件 = [", form.toString(), "]");
        Reportdep reportdep = new Reportdep();
        Integer save;
        if (checkAllField(form, redirectAttributes)) {
        	 reportdep.setReportdepDepno(StringUtils.upperCase(form.getReportDep_depNo()));
             reportdep.setReportdepDepname(form.getReportDep_depName());
             reportdep.setReportdepType(form.getReportDep_type());
             reportdep.setUpdateUserid(Integer.parseInt(WebUtil.getUser().getUserId()));
            if ("insert".equals(form.getBtnType())) {
            	save = commonService.insertBank(reportdep);
                if (save > 0) {
                	WebUtil.getUser().getPrevPageForm().setPageNum(1);
                    this.showMessage(redirectAttributes, MessageType.INFO, InsertSuccess);
                    return this.doRedirectForPrevPage(redirectAttributes, request);
                } else {
                    this.showMessage(redirectAttributes, MessageType.INFO, InsertFail);
                }
            } else {
            	save = commonService.updateBank(reportdep);
                if (save == 1) {
                	WebUtil.getUser().getPrevPageForm().setPageNum(1);
                    this.showMessage(redirectAttributes, MessageType.INFO, UpdateSuccess);
                    return this.doRedirectForPrevPage(redirectAttributes, request);
                } else {
                    this.showMessage(redirectAttributes, MessageType.INFO, UpdateFail);
                }
            }
        }
        
        return this.doRedirectForCurrentPage(redirectAttributes, request);
    }

    /**
     * 資料整理
     */
    private void bindGrid(UI_130900_Form form, ModelMap mode, String direction) {
        PageInfo<Reportdep> pageInfo = null;
        try {
            Reportdep reportdepSel = new Reportdep();
            reportdepSel.setReportdepDepno(StringUtils.upperCase(form.getReportDep_depNo()));
            reportdepSel.setReportdepDepname(form.getReportDep_depName());
            reportdepSel.setReportdepType(form.getReportDep_type());
            pageInfo = commonService.getBankData(reportdepSel, form.getPageNum(), form.getPageSize());
            if (pageInfo.getList() == null || pageInfo.getList().size() == 0) {
                this.showMessage(mode, MessageType.WARNING, QueryNoData);
                return;
            } else {
                this.clearMessage(mode);
                PageInfo<ReportdepExt> newPageInfo = this.changeObject(pageInfo);
                BeanUtils.copyProperties(pageInfo, newPageInfo, "list");
                for(ReportdepExt reportdep : newPageInfo.getList()) {
                	Integer userId = reportdep.getUpdateUserid();
                	String userName = String.valueOf(userId);
                	if(userId != null) {
                		userName = getUserEmployeeid(userId);
                	}
                	reportdep.setUpdateUserName(userName);
                	reportdep.setReportdepTypeTxt(getReportdepTypeTxt(reportdep.getReportdepType()));
                }
                PageData<UI_130900_Form, ReportdepExt> pageData = new PageData<>(newPageInfo, form);
                WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
            }
        } catch (Exception exception) {
            this.errorMessage(exception, exception.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    private Reportdep bindFormViewData(String pk) {
        return commonService.getBankByDepNo(pk);
    }
    
	private PageInfo<ReportdepExt> changeObject(PageInfo<Reportdep> pageInfo) {
		List<Reportdep> reportdepList = pageInfo.getList();
		List<ReportdepExt> reportdepExtList = new ArrayList<>(reportdepList.size());
		for (Reportdep reportdep : reportdepList) {
			ReportdepExt reportdepExt = new ReportdepExt(reportdep);
			reportdepExtList.add(reportdepExt);
		}
		return PageInfo.of(reportdepExtList);
	}
	
	private String getUserEmployeeid(Integer userId) {
		String employeeid = null;
		try {
			if(userId == 0) {
				employeeid = "批次匯入";
			}else {
				Syscomuser user = commonService.queryUsersById(userId);
				if(user != null) {
					employeeid = user.getEmployeeid();
	    		}
			}
		} catch (Exception exception) {
			employeeid = String.valueOf(userId);
		}
		return employeeid;
	}
	
    private boolean checkAllField(UI_130900_Form form, RedirectAttributes redirectAttributes) {
        try {
        	
        	if(StringUtil.isBlank(form.getReportDep_depNo())) {
        		this.showMessage(redirectAttributes, MessageType.WARNING, "分行代號不可為空");
        		return false;
        	}
        	
        	if(StringUtil.isBlank(form.getReportDep_depName())) {
        		this.showMessage(redirectAttributes, MessageType.WARNING, "分行名稱不可為空");
        		return false;
        	}
        	
            if ("insert".equals(form.getBtnType())) {
                if(bindFormViewData(form.getReportDep_depNo().toUpperCase()) != null) {
                	this.showMessage(redirectAttributes, MessageType.WARNING, "分行代號已存在");
                	return false;
                }
            }
            return true;
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(redirectAttributes, MessageType.DANGER, programError);
            return false;
        }
    }
    
    private String getReportdepTypeTxt(String reportdepType) {
    	String reportdepTypeTxt = StringUtils.EMPTY;;
    	if(StringUtils.isBlank(reportdepType)) {
    		return reportdepTypeTxt;
    	}
    	
    	switch(reportdepType) {
    	case "1":
    		reportdepTypeTxt = "總行單位";
    		break;
    	case "2":
    		reportdepTypeTxt = "分行";
    		break;
    	case "9":
    		reportdepTypeTxt = "測試單位";
    		break;
    	}
    	
    	return reportdepTypeTxt;
    }
}
