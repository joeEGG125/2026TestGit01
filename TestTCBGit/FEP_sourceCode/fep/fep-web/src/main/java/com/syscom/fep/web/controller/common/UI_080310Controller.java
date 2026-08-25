package com.syscom.fep.web.controller.common;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.model.Fepgroup;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.WebCodeConstant;
import com.syscom.fep.web.form.common.UI_080310_FormMain;
import com.syscom.fep.web.service.MemberShipService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 群組與功能查詢
 * @author bruce
 *
 */
@Controller
public class UI_080310Controller extends BaseController{
	
	@Autowired
	private MemberShipService memberShipService;

	@Override
	public void pageOnLoad(ModelMap mode) {
		UI_080310_FormMain form = new UI_080310_FormMain();
		Map<String, Object> argsMap = form.toMap();
		argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
		form.setUrl("/common/UI_080310/bindGrid");
		this.bindGridData(form,argsMap,mode);
	}
	
	/**
	 * 查詢按鈕
	 * @param form
	 * @param mode
	 * @return
	 */
	@PostMapping( value = "/common/UI_080310/bindGrid")
	public String bindGrid(@ModelAttribute UI_080310_FormMain form, ModelMap mode) {
		Map<String, Object> argsMap = form.toMap();
		argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
		this.bindGridData(form, argsMap, mode);
		return Router.UI_080310.getView();
	}
	
	/**
	 * 查詢
	 * @param argsMap
	 * @param mode
	 * @return
	 */
	private void bindGridData(UI_080310_FormMain form, Map<String, Object> argsMap, ModelMap mode) {
		this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);// 保存當前表單資料
		try {
			PageInfo<Fepgroup> pageInfo = memberShipService.queryFepGroupByPkLike(argsMap);
			if (pageInfo.getSize() == 0) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
			} else {
				this.showMessage(mode, MessageType.INFO, QuerySuccess);
			}
			PageData<UI_080310_FormMain, Fepgroup> pageData = new PageData<UI_080310_FormMain, Fepgroup>(pageInfo, form);
			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		} catch (Exception ex) {
			this.errorMessage(ex,ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
	}
	
	/**
	 * 使用者群組代碼超連結
	 * @param form
	 * @param mode
	 * @return
	 */
	@PostMapping( value = "/common/UI_080310/bindGridDetail")
	public String bindGridDetail(@ModelAttribute UI_080310_FormMain form, ModelMap mode) {
		this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		try {
			//已授權程式
			List<Map<String, Object>> prortList = memberShipService.queryPrortByGroupIdLike(form.getFepgroupGroupId());
//			List<Map<String, Object>> prortTempList = new ArrayList<Map<String, Object>>(prortList);
			WebUtil.putInAttribute(mode, AttributeName.DetailEntity, prortList);		
			//未授權程式(要把已授權程式移除)
			List<Map<String, Object>> programList = memberShipService.queryProgramf();
			for(int i = programList.size()-1 ; i >= 0 ; i--) {
				for(int j = 0 ; j < prortList.size() ; j++) {
					if(programList.get(i).get("PROGRAM_ID").equals(prortList.get(j).get("PROGRAM_ID"))) {
						programList.remove(i);
						continue;
					}
				}
			}
//			programList.addAll(prortTempList);
//			List<Map<String, Object>> programViewList = programList.stream()
//					.collect(Collectors.groupingBy(group -> group.get("PROGRAM_ID").toString()))
//					.entrySet().stream()
//					.map(map -> {
//						Map<String, Object> collect = map.getValue().stream()
//								.flatMap(o -> o.entrySet().stream())
//								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,(m1,m2)->m2));
//						return collect;
//	 				})
//					.collect(Collectors.toList());
			WebUtil.putInAttribute(mode, AttributeName.GridData, programList);			
		}catch (Exception ex) {
			this.errorMessage(ex,ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_080310_Detail.getView();
	}
}
