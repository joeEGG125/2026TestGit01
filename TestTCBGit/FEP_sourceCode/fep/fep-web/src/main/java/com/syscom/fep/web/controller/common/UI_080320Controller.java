package com.syscom.fep.web.controller.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.model.Bctl;
import com.syscom.fep.mybatis.model.Fepgroup;
import com.syscom.fep.mybatis.model.Fepuser;
import com.syscom.fep.web.base.FEPWebBase;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SelectOption;
import com.syscom.fep.web.entity.WebCodeConstant;
import com.syscom.fep.web.entity.common.FepuserTmp;
import com.syscom.fep.web.form.common.UI_080320_FormDetail;
import com.syscom.fep.web.form.common.UI_080320_FormMain;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.MemberShipService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 使用者資料查詢
 * @author bruce
 *
 */
@Controller
public class UI_080320Controller extends BaseController{
	
	@Autowired
	private MemberShipService memberShipService;
	
	@Autowired
	private AtmService atmService;
	
	private final String pleaseChoose = "請選擇";

	@Override
	public void pageOnLoad(ModelMap mode) {
		UI_080320_FormMain form = new UI_080320_FormMain();
		Map<String, Object> argsMap = form.toMap();
		argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
		this.getGroupId(mode);//取得權限群組下拉選單
		this.getAtmbctl(mode);//取得分行代號下拉選單
		form.setUrl("/common/UI_080320/bindGrid");
		this.bindGridData(form,argsMap,mode);
	}
	
	@PostMapping(value = "/common/UI_080320/bindGridDetail")
	public String bindGridDetail(@ModelAttribute UI_080320_FormDetail form, ModelMap mode) {
//		DateFormat dateTimeformat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
//		form.setUpdateTime(dateTimeformat.format(new Date(form.getUpdateTime())));	
//		form.setUrl("/common/UI_080320/bindGrid");
		this.getGroupId(mode);//取得權限群組下拉選單
		this.getAtmbctl(mode);//取得分行代號下拉選單
		this.doKeepFormData(mode, form);// 保存當前表單資料
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
		return Router.UI_080320_Detail.getView();
	}
	
	/**
	 * 查詢按鈕
	 * @param form
	 * @param mode
	 * @return
	 */
	@PostMapping( value = "/common/UI_080320/bindGrid")
	public String bindGrid(@ModelAttribute UI_080320_FormMain form, ModelMap mode) {
		Map<String, Object> argsMap = form.toMap();
		argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
		this.getGroupId(mode);//取得權限群組下拉選單
		this.getAtmbctl(mode);//取得分行代號下拉選單
		this.bindGridData(form, argsMap, mode);
		return Router.UI_080320.getView();
	}
	
	/**
	 * 查詢
	 * @param form
	 * @param argsMap
	 * @param mode
	 */
	private String bindGridData(UI_080320_FormMain form,Map<String, Object> argsMap,ModelMap mode) {
		this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);// 保存當前表單資料
		try {
			PageInfo<Fepuser> pageInfo = atmService.queryFepUser(argsMap);
			//將Fepuser改成使用FepuserTmp
			PageInfo<FepuserTmp> newPageInfo = this.changeObject(pageInfo);
			BeanUtils.copyProperties(pageInfo, newPageInfo, "list");
			if (newPageInfo.getSize() == 0) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
			} else {
				this.changeFieldContent(newPageInfo,mode);
				this.showMessage(mode, MessageType.INFO, QuerySuccess);
			}
			PageData<UI_080320_FormMain, FepuserTmp> pageData = new PageData<UI_080320_FormMain, FepuserTmp>(newPageInfo, form);
			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		} catch (Exception ex) {
			this.errorMessage(ex,ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_080320.getView();
	}
	
	/**
	 * 改變特定欄位內容
	 * @param pageInfo
	 * @throws Exception 
	 */
	private void changeFieldContent(PageInfo<FepuserTmp> pageInfo, ModelMap mode) throws Exception {
		for (FepuserTmp fepUserTmp : pageInfo.getList()) {
			// 分行代號 + 中文
			fepUserTmp.setFepuserBrnoTxt(fepUserTmp.getFepuserBrno() + "-" + this.getBrNoCh(mode, fepUserTmp.getFepuserBrno()));
			// 權限群組 + 中文
			fepUserTmp.setFepUserGroupTxt(fepUserTmp.getFepuserGroup() + "-" + this.getGroupCh(mode, fepUserTmp.getFepuserGroup()));
			// 上次登錄日期 format yyyy/mm/dd
			fepUserTmp.setFepuserLuDateTxt(FEPWebBase.formatYMD(fepUserTmp.getFepuserLuDate()));
			// 上次登錄時間 format hh:mm:ss
			fepUserTmp.setFepuserLuTimeTxt(FEPWebBase.formatHMS(fepUserTmp.getFepuserLuTime()));
			// 上次修改人員 Logonid + Name
			fepUserTmp.setUpdateUseridTxt(this.getUserId(mode, fepUserTmp.getUpdateUserid(), "userId"));
			// 直屬查核人員 Logonid + Name
			if (StringUtils.isNotBlank(fepUserTmp.getFepuserBoss())) {
				fepUserTmp.setFepuserBossTxt(this.getUserId(mode, Integer.parseInt(fepUserTmp.getFepuserBoss()), "bossId"));
			}
			// 上次修改日期 format yyyy/mm/dd HH:mm:ss
			fepUserTmp.setUserUpdateTimeTxt(this.formatUpdateTime(fepUserTmp.getUserUpdateTime()));
		}
	}
	
	/**
	 * 格式化上次修改日期
	 * @param userUpdateTime
	 * @return
	 * @throws Exception 
	 */
	private String formatUpdateTime(Date userUpdateTime) throws Exception{
        return new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(userUpdateTime);	
	}
	
	/**
	 * 取得上次修改人員 Logonid 及 Name
	 * @param updateUserId 
	 * @return
	 * @throws Exception 
	 */
	private String getUserId(ModelMap mode, int updateUserId,String paramType) throws Exception {
		try {
			return atmService.getFepUserId(updateUserId,paramType);
		}catch(Exception ex) {
			this.errorMessage(ex,ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return "";
	}
	
	/**
	 * 取得權限群組 中文名稱
	 * @param groupId
	 * @return
	 * @throws Exception 
	 */
    private String getGroupCh(ModelMap mode,String groupId) throws Exception {
    	try {
    		return atmService.getGroupIdNameByID(groupId);
    	}catch(Exception ex) {
			this.errorMessage(ex,ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
    	}
    	return "";
    }
	
	/**
	 * 取得分行代號中文名稱
	 * @param brNo
	 * @return
	 * @throws Exception 
	 */
	private String getBrNoCh(ModelMap mode,String brNo) throws Exception {
		try {
			return atmService.getBctlNameByBrno(brNo);
		}catch(Exception ex) {
			this.errorMessage(ex,ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return "";
	}
	
	/**
	 * 因為要將物件欄位做二次加工，所以新增一個FepuserTmp繼承Fepuser
	 * @param pageInfo
	 * @return
	 */
	private PageInfo<FepuserTmp> changeObject(PageInfo<Fepuser> pageInfo) {
		List<Fepuser> fepUserList = pageInfo.getList();
		List<FepuserTmp> fepUserTmpList = new ArrayList<>(fepUserList.size());
		for (Fepuser fepUser : fepUserList) {
			FepuserTmp fepUserTmp = new FepuserTmp(fepUser);
			fepUserTmpList.add(fepUserTmp);
		}
		return PageInfo.of(fepUserTmpList);
	}
	
	/**
	 * 取得權限群組下拉選單
	 * @param mode
	 */
	private void getGroupId(ModelMap mode) {
		try {
			List<Fepgroup> fepgroup = this.memberShipService.getDistinctGroupIdName();
			List<SelectOption<String>> selectOptionList = new ArrayList<SelectOption<String>>();
			selectOptionList.add(new SelectOption<String>(this.pleaseChoose, StringUtils.EMPTY));
			for(int i= 0 ; i < fepgroup.size() ; i++) {
				selectOptionList.add(new SelectOption<String>(fepgroup.get(i).getFepgroupGroupId() +"-"+ fepgroup.get(i).getFepgroupName(), fepgroup.get(i).getFepgroupGroupId()));
			}			
			WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList);
		} catch (Exception ex) {
			this.errorMessage(ex,ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
	}
	
	/**
	 * 取得分行代號下拉選單
	 * @param mode
	 */
	private void getAtmbctl(ModelMap mode) {
		try {
			List<Bctl> bctlList = this.atmService.getAllBCTLBrno();
			List<SelectOption<String>> selectOptionList = new ArrayList<SelectOption<String>>();
			selectOptionList.add(new SelectOption<String>(this.pleaseChoose, StringUtils.EMPTY));
			for(int i= 0 ; i < bctlList.size() ; i++) {
				selectOptionList.add(new SelectOption<String>(bctlList.get(i).getBctlBrno() +"-"+ bctlList.get(i).getBctlAlias(), bctlList.get(i).getBctlBrno()));
			}			
			WebUtil.putInAttribute(mode, AttributeName.GridData, selectOptionList);
		} catch (Exception ex) {
			this.errorMessage(ex,ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
	}
}
