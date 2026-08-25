package com.syscom.fep.web.controller.dbmaintain;

import java.util.ArrayList;
import java.util.List;

import com.syscom.fep.web.audit.AuditLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.model.Hotbin;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.dbmaintain.UI_070510_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.HostService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 偽BIN資料檔HOTBIN
 * 
 * @author Han 2022/06/17
 *
 */
@Controller
public class UI_070510Controller extends BaseController {

	@Autowired
	HostService hostService;

	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單資料
		UI_070510_Form form = new UI_070510_Form();
		form.setUrl("/dbmaintain/UI_070510/queryClick");
		this.doKeepFormData(mode, form);
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/dbmaintain/UI_070510/queryClick")
	private String queryClick(@ModelAttribute UI_070510_Form form, ModelMap mode) {
		form.setUrl("/dbmaintain/UI_070510/queryClick");
		this.doKeepFormData(mode, form);
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
		BindGridData(form, mode);

		return Router.UI_070510.getView();
	}

	private void BindGridData(UI_070510_Form form, ModelMap mode) {
		// 記錄auditLog
		AuditLog auditLog = new AuditLog();
		// 塞入auditLog.action
		auditLog.setAction("查詢");
		// 塞入auditLog.params
		if (StringUtils.isNotBlank(form.getTxtBinNo()))
			auditLog.addParam("CREDIT CARD BIN", form.getTxtBinNo());
		if (StringUtils.isNotBlank(form.getTxtBinOrg()))
			auditLog.addParam("發卡組織", form.getTxtBinOrg());
		// 最後塞入ThreadLocal變量中
		setAuditLog(auditLog);
		if (StringUtils.isNotEmpty(form.getTxtBinNo())) {
			form.setTxtBinNo(form.getTxtBinNo().trim());
		}
		if (StringUtils.isNotEmpty(form.getTxtBinOrg())) {
			form.setTxtBinOrg(form.getTxtBinOrg().trim());
		}

		PageInfo<List<Hotbin>> pageInfo;

		try {
			pageInfo = hostService.getAlarmByPKLike(form, form.getPageNum(), form.getPageSize());

			if (pageInfo.getSize() == 0) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
				return;
			}
			
			PageData<UI_070510_Form, List<Hotbin>> pageData = new PageData<>(pageInfo, form);
			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);

			this.showMessage(mode, MessageType.INFO, QuerySuccess);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, QueryFail);
		}
//		finally {
////			pageInfo = null;
//		}
	}

	@PostMapping(value = "/dbmaintain/UI_070510/insertPage")
	private String insertPage(@ModelAttribute UI_070510_Form form, ModelMap mode) {


		try {
			form.setTxtBinNoInsert("");
			form.setTxtBinOrgInsert("");
			form.setFocusInsert("");
			WebUtil.putInAttribute(mode, AttributeName.Form, form);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_070510_Insert.getView();
	}

	@PostMapping(value = "/dbmaintain/UI_070510/insertClick")
	private String insertClick(@ModelAttribute UI_070510_Form form, ModelMap mode) {

		try {
			
			//因為insert時,主建蟲附會抱錯跳出壞掉，只好改寫先查詢後insert
			Hotbin data = hostService.selectByPrimaryKey(form.getTxtBinNoInsert());
			if (data != null) {
				this.showMessage(mode, MessageType.DANGER, Multiple); // 主鍵值重覆
			} else {
				// 記錄auditLog
				AuditLog auditLog = new AuditLog();
				// 塞入auditLog.action
				auditLog.setAction("新增");
				// 塞入auditLog.params
				if (StringUtils.isNotBlank(form.getTxtBinNoInsert()))
					auditLog.addParam("CREDIT CARD BIN", form.getTxtBinNoInsert());
				if (StringUtils.isNotBlank(form.getTxtBinOrgInsert()))
					auditLog.addParam("發卡組織", form.getTxtBinOrgInsert());
				// 最後塞入ThreadLocal變量中
				setAuditLog(auditLog);
				hostService.insertHOTBIN(form.getTxtBinNoInsert(), form.getTxtBinOrgInsert(),
						WebUtil.getUser().getUserId());
				this.showMessage(mode, MessageType.SUCCESS, InsertSuccess);
			}

		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, InsertFail);
		}
		
		form.setFocusInsert(form.getTxtBinNoInsert()); //紀錄回上一頁需要焦點的列
		form.setTxtBinNoInsert("");
		form.setTxtBinOrgInsert("");
		
		this.doKeepFormData(mode, form);
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
		return Router.UI_070510_Insert.getView();
	}
	@PostMapping(value = "/dbmaintain/UI_070510/prevPage")
	private String prevPage(@ModelAttribute UI_070510_Form form, ModelMap mode) {
		
		try {
			
			form.setUrl("/dbmaintain/UI_070510/queryClick");
			this.doKeepFormData(mode, form);
			WebUtil.putInAttribute(mode, AttributeName.Form, form);
			BindGridData(form, mode);
			
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
		}
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
		
		return Router.UI_070510.getView();
	}
	
	@PostMapping(value = "/dbmaintain/UI_070510/deleteList")
	@ResponseBody
	public BaseResp<UI_070510_Form> deleteList(@RequestBody List<UI_070510_Form> formList, ModelMap mode) {
		this.infoMessage("執行刪除動作, 條件 = [", formList.toString(), "]");
		BaseResp<UI_070510_Form> response = new BaseResp<>();
		try {
			List<String> binNoList = new ArrayList<String>();

			if(formList.size() >0) {
				// 記錄auditLog
				AuditLog auditLog = new AuditLog();
				// 塞入auditLog.action
				auditLog.setAction("刪除");

				for(int i = 0; i <formList.size(); i++) {
					// 塞入auditLog.params
					auditLog.addParam("CREDIT CARD BIN", formList.get(i).getBinNo().trim());
					binNoList.add(formList.get(i).getBinNo().trim());
					hostService.deleteHOTBIN(formList.get(i).getBinNo().trim());
				}

				// 最後塞入ThreadLocal變量中
				setAuditLog(auditLog);
			}
			
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			// show錯誤訊息到前台頁面
			response.setMessage(MessageType.DANGER, DeleteFail);
		}
		response.setMessage(MessageType.SUCCESS, DeleteSuccess);
		return response;
	}
}
