package com.syscom.fep.web.controller.osm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Atmfee;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SelectOption;
import com.syscom.fep.web.entity.osm.UI_130100_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.OsmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * For Demo
 * 
 * @author Richard
 */
@Controller
public class UI_130100Controller extends BaseController {
	@Autowired
	private OsmService osmService;
	
	@Override
	public void pageOnLoad(ModelMap mode) {
		try {
			this.bindConstant(mode);
			// 初始化表單資料
			UI_130100_Form form = new UI_130100_Form();
			// 交易日期
			form.setTbxTX_MM(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMM_PLAIN));
			bindConstant(mode);
//			bindGridData(form, mode);
//			this.bindConstant(mode);
//			this.doKeepFormData(mode, form);
			WebUtil.putInAttribute(mode, AttributeName.Form, form);
        }catch (Exception e){
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
        }
	}

	/**
	 * 為頁面綁定一些常量
	 * 
	 * @param mode
	 */
	private void bindConstant(ModelMap mode) {
		// 初始化PCODE下拉選單
		List<SelectOption<String>> options = new ArrayList<>();
		options.add(new SelectOption<String>("", ""));
		List<Atmfee> list = (List<Atmfee>) osmService.selectAllAtmfee();
		for(Atmfee obj : list) {
			String title = "";
			String value = "";
			if(obj != null && StringUtils.isNotBlank(obj.getAtmfeeSeqNo().toString())) {
				title = obj.getAtmfeeSeqNo().toString().trim();
				value = obj.getAtmfeeSeqNo().toString().trim();
			}
			if(obj != null && obj.getAtmfeeName() != null && StringUtils.isNotBlank(obj.getAtmfeeName().toString())) {
				title = title + "-"+ obj.getAtmfeeName().toString().trim();
			}
			options.add(new SelectOption<String>(title, value));
		}	
		
		WebUtil.putInAttribute(mode, AttributeName.Options, options);
	}
	
//    private void bindGridData(UI_130100_Form form, ModelMap mode) throws Exception {
//        try {
//        	String fiscFlag = null;
//        	if(form.isCheck_INTRA() && !form.isCheck_OUT()) {
//        		fiscFlag = "0";    //'只查自行
//        	}else if(!form.isCheck_INTRA() && form.isCheck_OUT()) {
//        		fiscFlag = "1";    //'只查跨行
//        	}
//        	
//        	PageInfo<Atmfee> pageInfo = osmService.selectByPKLike(fiscFlag, form.getDdlSEQ_NO(), form.getTbxTX_MM(), form.getPageNum(), form.getPageSize());
//			if (pageInfo.getSize() == 0) {
//				this.showMessage(mode, MessageType.INFO, QueryNoData);
//			}
//			PageData<UI_130100_Form, Atmfee> pageData = new PageData<UI_130100_Form, Atmfee>(pageInfo, form);
//			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
//        }catch (Exception e){
//			this.errorMessage(e, e.getMessage());
//			this.showMessage(mode, MessageType.DANGER, programError);
//        }
//    }	
    
	@PostMapping(value = "/osm/UI_130100/inquiryMain")
	public String doInquiryMain(@ModelAttribute UI_130100_Form form, ModelMap mode) {
		this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
		this.bindConstant(mode);
		this.doKeepFormData(mode, form);
		try {
        	String fiscFlag = null;
        	if(form.isCheck_INTRA() && !form.isCheck_OUT()) {
        		fiscFlag = "0";    //'只查自行
        	}else if(!form.isCheck_INTRA() && form.isCheck_OUT()) {
        		fiscFlag = "1";    //'只查跨行
        	}
        	String seqno = null;
        	String txmm = null;
        	if(StringUtils.isNotBlank(form.getDdlSEQ_NO()))seqno=form.getDdlSEQ_NO();
        	if(StringUtils.isNotBlank(form.getTbxTX_MM()))txmm=form.getTbxTX_MM();
        	
        	PageInfo<Atmfee> pageInfo = osmService.selectByPKLike(fiscFlag, seqno, txmm, form.getPageNum(), form.getPageSize());
			if (pageInfo.getSize() == 0) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
			}
			PageData<UI_130100_Form, Atmfee> pageData = new PageData<UI_130100_Form, Atmfee>(pageInfo, form);
			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_130100.getView();
	}
	
	/**
	 * 新增鈕、Grid中第二列修改按鈕 Event(共用畫面)
	 * 區別方式：form.getBtnType()= "insert"、"update" 
	 */
	@PostMapping(value = "/osm/UI_130100/showDetail")
	private String showDetail(@ModelAttribute UI_130100_Form form, ModelMap mode){
		this.infoMessage("查詢明細資料_1, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		try {
			//按下 新增鈕
			if ("insert".equals(form.getBtnType())){
				form = this.clearFormControl();
			//按下 Grid中第二列修改按鈕(針對唯一記錄執行明細修改作業)
			}else {
				Atmfee atmfee = osmService.selectByPrimaryKey(form.getAtmfeeTxMm(),form.getAtmfeeSeqNo());
				form.setAtmfeeCur(atmfee.getAtmfeeCur());
				form.setAtmfeeFee(atmfee.getAtmfeeFee());
				form.setAtmfeeFiscFlag(atmfee.getAtmfeeFiscFlag());
				form.setAtmfeeName(atmfee.getAtmfeeName());
				form.setAtmfeePcode(atmfee.getAtmfeePcode());
				form.setAtmfeeTxMm(atmfee.getAtmfeeTxMm());
				form.setAtmfeeSeqNo(atmfee.getAtmfeeSeqNo());
			}
			this.infoMessage("查詢明細資料_2, 條件 = [", form.toString(), "]");
			WebUtil.putInAttribute(mode, AttributeName.Form, form);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_130100_Detail.getView();
	}	
	
	/**
	 * 設定畫面Button Delete的Event
	 * @param formList
	 * @param mode
	 * @return
	 */
	@PostMapping(value = "/osm/UI_130100/deleteList")
	@ResponseBody
	public BaseResp<?> deleteList(@RequestBody List<UI_130100_Form> formList, ModelMap mode) {
		this.infoMessage("執行刪除動作, 條件 = [", formList.toString(), "]");
		BaseResp<?> response = new BaseResp<>();
		try {
			for (UI_130100_Form form : formList) {
				Atmfee atmfee = new Atmfee();
				atmfee.setAtmfeeTxMm(form.getAtmfeeTxMm());
				atmfee.setAtmfeeSeqNo(form.getAtmfeeSeqNo());
				osmService.deleteByPrimaryKey(atmfee);
			}
			response.setMessage(MessageType.INFO, DeleteSuccess);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			// show錯誤訊息到前台頁面
			response.setMessage(MessageType.DANGER, DeleteFail);
		}
		return response;
	}	
	
	@PostMapping(value = "/osm/UI_130100/saveClick")
	private String saveClick(@ModelAttribute UI_130100_Form form, RedirectAttributes redirectAttributes, HttpServletRequest request) {
		this.infoMessage("存檔, 表單完整內容 = [", form.toString(), "]");
		Atmfee atmfee = new Atmfee();
		int resultCount=0;
		if (checkAllField(atmfee, form, redirectAttributes)) {
			try {
				if ("insert".equals(form.getBtnType())) {
					this.infoMessage("新增, 主鍵 = 序號"+atmfee.getAtmfeeSeqNo() + ",年月"+atmfee.getAtmfeeTxMm());

					Atmfee obj = osmService.selectByPrimaryKey(form.getAtmfeeTxMm(),form.getAtmfeeSeqNo());
					if (obj == null) {
						resultCount = osmService.insertSelective(atmfee);
						if (resultCount > 0) {
							this.showMessage(redirectAttributes, MessageType.INFO, InsertSuccess);
						} else {
							this.showMessage(redirectAttributes, MessageType.INFO, InsertFail);
						}
					}else {
						this.showMessage(redirectAttributes, MessageType.INFO, "手續費序號重複");
					}
				}else{
					this.infoMessage("修改, 主鍵 = 序號" +atmfee.getAtmfeeSeqNo() + ",年月" +atmfee.getAtmfeeTxMm());
					resultCount = osmService.updateByPrimaryKeySelective(atmfee);
					if (resultCount > 0) {
						this.showMessage(redirectAttributes, MessageType.INFO, UpdateSuccess);
						return this.doRedirectForCurrentPage(redirectAttributes, request);
					} else {
						this.showMessage(redirectAttributes, MessageType.INFO, UpdateFail);
					}
				}
			} catch (Exception e) {
				this.errorMessage(e, e.getMessage());
				this.showMessage(redirectAttributes, MessageType.WARNING, programError);
			}
		}
		return this.doRedirectForCurrentPage(redirectAttributes, request);
	}	
	
	/**
	 * 檢核資料正確後，即送入ALARM中，以供後續新增或修改之用
	 * @param alarm
	 * @param form
	 * @param redirectAttributes
	 * @return
	 */
	private boolean checkAllField(Atmfee alarm, UI_130100_Form form, RedirectAttributes redirectAttributes) {
		this.infoMessage("檢查表單內容, 完整內容 = [", form.toString(), "]");
		try {
			alarm.setAtmfeeCur(form.getAtmfeeCur());
			alarm.setAtmfeeFee(form.getAtmfeeFee());
			alarm.setAtmfeeFiscFlag(form.getAtmfeeFiscFlag());
			alarm.setAtmfeeName(form.getAtmfeeName());
			alarm.setAtmfeeTxMm(form.getAtmfeeTxMm());
			alarm.setAtmfeeSeqNo(form.getAtmfeeSeqNo());
			if(StringUtils.isNotBlank(form.getAtmfeePcode())) {
				alarm.setAtmfeePcode(form.getAtmfeePcode());
			}
			return true;
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(redirectAttributes, MessageType.WARNING, programError);
			return false;
		}
	}	

	/**
	 * 連續新增模式,清除單筆表單控制項內容
	 * @return
	 */
	private UI_130100_Form clearFormControl() {
		UI_130100_Form form = new UI_130100_Form();
		form.setBtnType("insert");
		form.setAtmfeeCur("");
		form.setAtmfeeFee(null);
		form.setAtmfeeFiscFlag("");
		form.setAtmfeeName("");
		form.setAtmfeePcode("");
		form.setAtmfeeTxMm("");
		form.setAtmfeeSeqNo("");
		return form;
	}	
	
	
}
