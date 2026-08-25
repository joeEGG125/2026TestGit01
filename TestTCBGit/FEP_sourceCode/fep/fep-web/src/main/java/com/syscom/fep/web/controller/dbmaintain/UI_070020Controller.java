package com.syscom.fep.web.controller.dbmaintain;

import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.dbmaintain.UI_070020_Form;
import com.syscom.fep.web.form.dbmaintain.UI_070020_FormDetail;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class UI_070020Controller extends BaseController {
	
	private String webType = WebConfiguration.getInstance().getWebType();

	@Autowired
	private AtmService atmservice;
	@Autowired
	private InbkService inbkService;

	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單資料
		UI_070020_Form form = new UI_070020_Form();
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		form.setTxtBSDAYS_YEAR(sdf.format(date)); // 欄位帶入今年
		BindGridData(form, mode);//查詢現在時間今年
		// 'Fly 2018/02/14 SSTQ系統時取消新增/修改/刪除功能
		form.setWebType(this.webType);
		form.setUrl("/dbmaintain/UI_070020/inquiryMain");
		this.doKeepFormData(mode, form);
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/dbmaintain/UI_070020/inquiryMain")
	public String doInquiryMain(@ModelAttribute UI_070020_Form form, ModelMap mode) {
		this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
		form.setUrl("/dbmaintain/UI_070020/inquiryMain");
		this.doKeepFormData(mode, form);
		try {
			BindGridData(form,mode);
			form.setTxtBSDAYS_YEAR(form.getTxtBSDAYS_YEAR());
			form.setWebType(this.webType);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_070020.getView();
	}
	
	private void BindGridData(UI_070020_Form form, ModelMap mode) {

		try {
			if (form.getTxtBSDAYS_YEAR().length() != 4) {
				this.showMessage(mode, MessageType.DANGER, "請輸入西元年正確格式");
			} else {
				// 記錄auditLog
				AuditLog auditLog = new AuditLog();
				// 塞入auditLog.action
				auditLog.setAction("查詢");
				// 塞入auditLog.params
				if (StringUtils.isNotBlank(form.getLblBSDAYS_ZONE_CODE()))
					auditLog.addParam("地區", form.getLblBSDAYS_ZONE_CODE());
				if (StringUtils.isNotBlank(form.getTxtBSDAYS_YEAR()))
					auditLog.addParam("年", form.getTxtBSDAYS_YEAR());
				// 最後塞入ThreadLocal變量中
				setAuditLog(auditLog);
				List<Map<String, Object>> tempData = atmservice.getBSDAYSByYearAndZone(form.getTxtBSDAYS_YEAR(),(Integer.parseInt(form.getTxtBSDAYS_YEAR())+1)+"",form.getLblBSDAYS_ZONE_CODE());
				String activeDay = "";
				String cleanDay = "" ;
				for(int i=0;i<tempData.size();i++) {
					if(0 == Short.parseShort(tempData.get(i).get("BSDAYS_WORKDAY").toString())) {
						if("".equals(activeDay)) {
							activeDay += tempData.get(i).get("BSDAYS_DATE").toString();
						}else {
							activeDay = activeDay + "," + tempData.get(i).get("BSDAYS_DATE").toString();
						}
					}else{
						if("".equals(activeDay)) {
							cleanDay += tempData.get(i).get("BSDAYS_DATE").toString();
						}else {
							cleanDay = cleanDay + "," + tempData.get(i).get("BSDAYS_DATE").toString();
						}
					}
				}
				form.setActiveCalendar(activeDay);
				form.setCleanCalendar(cleanDay);
			}
			Sysstat sysstat = inbkService.getStatus();
			if (sysstat == null) {
				return;
			}
			form.setSysstatNbsdyFisc(sysstat.getSysstatNbsdyFisc().substring(0,4)+"/"+sysstat.getSysstatNbsdyFisc().substring(4,6)+"/"+sysstat.getSysstatNbsdyFisc().substring(6,8));
			form.setSysstatTbsdyFisc(sysstat.getSysstatTbsdyFisc().substring(0,4)+"/"+sysstat.getSysstatTbsdyFisc().substring(4,6)+"/"+sysstat.getSysstatTbsdyFisc().substring(6,8));
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
	}
	
	/**
	 * 新增按鈕 
	 * @param form
	 * @param mode
	 * @return
	 */
	@PostMapping( value = "/dbmaintain/UI_070020/insertClick")
	public String insertClick(@ModelAttribute UI_070020_Form form, ModelMap mode) {
		
		UI_070020_FormDetail form1 = new UI_070020_FormDetail();
		form1.setWebType(WebConfiguration.getInstance().getWebType());
		form1.setStyle("insert");
		//this.doKeepFormData(mode, form1);
		
		WebUtil.putInAttribute(mode, AttributeName.Form, form1);
		return Router.UI_070020_Detail.getView();
	}
	

	/**
	 * 新增按鈕
	 * @param form
	 * @param mode
	 * @return
	 */
	@PostMapping( value = "/dbmaintain/UI_070020/insertActionClick")
	public String insertActionClick(@ModelAttribute UI_070020_FormDetail form, ModelMap mode) {
		
		form.setWebType(WebConfiguration.getInstance().getWebType());
		form.setTxtBSDAYS_NBSDY(form.getTxtBSDAYS_NBSDY().replaceAll("-", ""));
		form.setTxtBSDAYS_ST_DATE_ATM(form.getTxtBSDAYS_ST_DATE_ATM().replaceAll("-", ""));
		form.setTxtBSDAYS_ST_DATE_RM(form.getTxtBSDAYS_ST_DATE_RM().replaceAll("-", ""));
		form.setTxtBSDAYS_DATE(form.getTxtBSDAYS_DATE().replaceAll("-", ""));
		
		Bsdays bsdayTable = new Bsdays();
		
		if(CheckAllField(bsdayTable,form,mode)) {
			try{
				if(!CheckDate(form)){
					this.showMessage(mode, MessageType.WARNING, "「下營業日」、「ATM清算日」和「RM清算日」不可小於日曆日");
					WebUtil.putInAttribute(mode, AttributeName.Form, form);
					return Router.UI_070020_Detail.getView();
				}
				
				List<Map<String, Object>> haveDate = atmservice.getBSDAYSByYearAndZoneAndDate(form.getBSDAYS_ZONE_CODEDdl(),form.getTxtBSDAYS_DATE());
				// 記錄auditLog
				AuditLog auditLog = new AuditLog();

				// 塞入auditLog.params
				if (StringUtils.isNotBlank(form.getBSDAYS_ZONE_CODEDdl()))
					auditLog.addParam("分行代號", form.getBSDAYS_ZONE_CODEDdl());
				if (StringUtils.isNotBlank(form.getTxtBSDAYS_DATE()))
					auditLog.addParam("日曆日", form.getTxtBSDAYS_DATE());
				if (StringUtils.isNotBlank(form.getTxtBSDAYS_JDAY()))
					auditLog.addParam("本年的第幾天", form.getTxtBSDAYS_JDAY());
				if (StringUtils.isNotBlank(form.getBSDAYS_WORKDAYDdl()))
					auditLog.addParam("工作日記號", form.getBSDAYS_WORKDAYDdl());
				if (StringUtils.isNotBlank(form.getTxtBSDAYS_WEEKNO()))
					auditLog.addParam("星期幾", form.getTxtBSDAYS_WEEKNO());
				if (StringUtils.isNotBlank(form.getTxtBSDAYS_NBSDY()))
					auditLog.addParam("下營業日", form.getTxtBSDAYS_NBSDY());
				if (StringUtils.isNotBlank(form.getTxtBSDAYS_ST_DATE_ATM()))
					auditLog.addParam("ATM清算日", form.getTxtBSDAYS_ST_DATE_ATM());
				if (StringUtils.isNotBlank(form.getTxtBSDAYS_ST_DATE_RM()))
					auditLog.addParam("RM清算日", form.getTxtBSDAYS_ST_DATE_RM());
				auditLog.addParam("UpdateUserid", WebUtil.getUser().getUserId());

				//有資料 Update
				if(haveDate.size() > 0) {
					// 塞入auditLog.action
					auditLog.setAction("修改");
					// 最後塞入ThreadLocal變量中
					setAuditLog(auditLog);
					form.setStyle("update");
					atmservice.updateBSDAYS(form,Long.parseLong(WebUtil.getUser().getUserId()));
					this.showMessage(mode, MessageType.INFO,UpdateSuccess);
					form.setUrl("/dbmaintain/UI_070020/inquiryMain");
				}else {	//沒資料 insert
					// 塞入auditLog.action
					auditLog.setAction("新增");
					// 最後塞入ThreadLocal變量中
					setAuditLog(auditLog);
					bsdayTable.setUpdateTime(new Date());
					atmservice.insertBSDAYS(bsdayTable);
					form.setStyle("insert");
					form.setUrl("/dbmaintain/UI_070020/insertClick");
					this.showMessage(mode, MessageType.INFO,InsertSuccess);
				}
				form.setTxtBSDAYS_NBSDY(form.getTxtBSDAYS_NBSDY().substring(0,4)+"-"+form.getTxtBSDAYS_NBSDY().substring(4,6)+"-"+form.getTxtBSDAYS_NBSDY().substring(6,8));
				form.setTxtBSDAYS_ST_DATE_ATM(form.getTxtBSDAYS_ST_DATE_ATM().substring(0,4)+"-"+form.getTxtBSDAYS_ST_DATE_ATM().substring(4,6)+"-"+form.getTxtBSDAYS_ST_DATE_ATM().substring(6,8));
				form.setTxtBSDAYS_ST_DATE_RM(form.getTxtBSDAYS_ST_DATE_RM().substring(0,4)+"-"+form.getTxtBSDAYS_ST_DATE_RM().substring(4,6)+"-"+form.getTxtBSDAYS_ST_DATE_RM().substring(6,8));
				form.setTxtBSDAYS_DATE(form.getTxtBSDAYS_DATE().substring(0,4)+"-"+form.getTxtBSDAYS_DATE().substring(4,6)+"-"+form.getTxtBSDAYS_DATE().substring(6,8));
				this.doKeepFormData(mode, form);
				WebUtil.putInAttribute(mode, AttributeName.Form, form);
			}catch(Exception e) {
				this.errorMessage(e, e.getMessage());
	        	this.showMessage(mode, MessageType.DANGER, programError);
			}
		}
		return Router.UI_070020_Detail.getView();
	}
	
	private boolean CheckDate(UI_070020_FormDetail form) {
		
		if(Integer.parseInt(form.getTxtBSDAYS_NBSDY()) < Integer.parseInt(form.getTxtBSDAYS_DATE())) {
			return false;
		}else if(Integer.parseInt(form.getTxtBSDAYS_ST_DATE_ATM()) < Integer.parseInt(form.getTxtBSDAYS_DATE())){
			return false;
		}else if(Integer.parseInt(form.getTxtBSDAYS_ST_DATE_RM()) < Integer.parseInt(form.getTxtBSDAYS_DATE())){
			return false;
		}
		return true;
	}
	private boolean CheckAllField(Bsdays bsdayTable, UI_070020_FormDetail form,ModelMap mode) {
		
		try {
				
			bsdayTable.setBsdaysZoneCode(form.getBSDAYS_ZONE_CODEDdl());
			bsdayTable.setBsdaysDate(form.getTxtBSDAYS_DATE());
			bsdayTable.setBsdaysWorkday(Short.parseShort(form.getBSDAYS_WORKDAYDdl()));
			bsdayTable.setBsdaysJday(Integer.parseInt(form.getTxtBSDAYS_JDAY()));
			bsdayTable.setBsdaysWeekno(Short.parseShort(form.getTxtBSDAYS_WEEKNO()));	
			bsdayTable.setBsdaysStFlag(Short.parseShort("0"));
			bsdayTable.setBsdaysNbsdy(form.getTxtBSDAYS_NBSDY());
			bsdayTable.setBsdaysStDateAtm(form.getTxtBSDAYS_ST_DATE_ATM());
			bsdayTable.setBsdaysStDateRm(form.getTxtBSDAYS_ST_DATE_RM()); 
			bsdayTable.setUpdateUserid(Integer.parseInt(WebUtil.getUser().getUserId()));
			
			return true;
		}catch(Exception e) {
			this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
			return false;
		}
	}
	
	@PostMapping( value = "/dbmaintain/UI_070020/updateClick")
	public String updateClick(@ModelAttribute UI_070020_FormDetail form, ModelMap mode) {
		
		form.setWebType(WebConfiguration.getInstance().getWebType());
		form.setStyle("update");
			try{
				List<Map<String,Object>> data = atmservice.getBSDAYSByYearAndZoneAndDate(form.getBSDAYS_ZONE_CODEDdl(),form.getTxtBSDAYS_DATE().replaceAll("/", ""));
				if(data.size() == 0) {
					//當搜尋找不到值時返回前頁
					UI_070020_Form form1 = new UI_070020_Form();
					String year = form.getTxtBSDAYS_DATE().substring(0, 4);//取出年份
					form1.setTxtBSDAYS_YEAR(year);
					form1.setWebType(this.webType);
					BindGridData(form1, mode);
					this.showMessage(mode, MessageType.WARNING, QueryNoData);
					WebUtil.putInAttribute(mode, AttributeName.Form, form1);
					
					return Router.UI_070020.getView();
				}
				
				String temp = data.get(0).get("BSDAYS_NBSDY").toString().substring(0,4)+"-"+data.get(0).get("BSDAYS_NBSDY").toString().substring(4,6)+"-"+data.get(0).get("BSDAYS_NBSDY").toString().substring(6,8);
				
				form.setBSDAYS_ZONE_CODEDdl(data.get(0).get("BSDAYS_ZONE_CODE").toString());
				form.setTxtBSDAYS_JDAY(data.get(0).get("BSDAYS_JDAY").toString());
				form.setBSDAYS_WORKDAYDdl(data.get(0).get("BSDAYS_WORKDAY").toString());
				form.setTxtBSDAYS_WEEKNO(data.get(0).get("BSDAYS_WEEKNO").toString());
				form.setTxtBSDAYS_ST_FLAG(data.get(0).get("BSDAYS_ST_FLAG").toString());
				form.setTxtBSDAYS_NBSDY(temp);
				
				temp = data.get(0).get("BSDAYS_ST_DATE_ATM").toString().substring(0,4)+"-"+data.get(0).get("BSDAYS_ST_DATE_ATM").toString().substring(4,6)+"-"+data.get(0).get("BSDAYS_ST_DATE_ATM").toString().substring(6,8);
				form.setTxtBSDAYS_ST_DATE_ATM(temp);
				
				temp = data.get(0).get("BSDAYS_ST_DATE_RM").toString().substring(0,4)+"-"+data.get(0).get("BSDAYS_ST_DATE_RM").toString().substring(4,6)+"-"+data.get(0).get("BSDAYS_ST_DATE_RM").toString().substring(6,8);
				form.setTxtBSDAYS_ST_DATE_RM(temp);
				
				temp = data.get(0).get("BSDAYS_DATE").toString().substring(0,4)+"-"+data.get(0).get("BSDAYS_DATE").toString().substring(4,6)+"-"+data.get(0).get("BSDAYS_DATE").toString().substring(6,8);
				form.setTxtBSDAYS_DATE(temp);
				
			}catch(Exception e) {
				this.errorMessage(e, e.getMessage());
	        	this.showMessage(mode, MessageType.DANGER, programError);
			}
			WebUtil.putInAttribute(mode, AttributeName.Form, form);
		
		return Router.UI_070020_Detail.getView();
	}
	
	@PostMapping(value = "/dbmaintain/UI_070020/updateFiscDate")
    @ResponseBody
    protected BaseResp<?> updateFiscDate(@RequestBody Bsdays bsdays){
		BaseResp<?> response = new BaseResp<>();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
			String sysDate = sdf.format(new Date());
			String sysstatHbkno = "006";
			String tDate = bsdays.getBsdaysDate();
			Short bsdaysWorkday = bsdays.getBsdaysWorkday();
			String nDate = bsdays.getBsdaysNbsdy();
			String work = null;
			String ldate = null;
			if(bsdaysWorkday != null) {
				work = bsdaysWorkday.toString();
			}
			if(StringUtils.isBlank(tDate) || StringUtils.isBlank(work) || StringUtils.isBlank(nDate)) {
				response.setMessage(MessageType.INFO, "無法取得資料請重新再試!");
	            return response;
			}
			
			if(sysDate.compareTo(tDate) > 0) {
				response.setMessage(MessageType.INFO, "此日期早於系統日期，不能指定為財金營業日!");
	            return response;
			}
			
			if(!StringUtils.equals(work, "1")) {
				response.setMessage(MessageType.INFO, "此日期非工作日，不能指定為財金營業日!");
	            return response;
			}
			
			tDate = sdf2.format(sdf.parse(tDate));
			nDate = sdf2.format(sdf.parse(nDate));
			Bsdays LBsdays = atmservice.getLBsdays(tDate);
			if(LBsdays == null) {
				response.setMessage(MessageType.INFO, "找不到上期財金營業日資料，無法更新財金營業日，請確認日曆設定資料!");
	            return response;
			}
			ldate = LBsdays.getBsdaysDate();
			if(StringUtils.isBlank(ldate)) {
				response.setMessage(MessageType.INFO, "找不到上期財金營業日資料，無法更新財金營業日，請確認資料庫資料!");
	            return response;
			}
			
			if(inbkService.updateSyssTatLTNbsdyFisc(sysstatHbkno, ldate, tDate, nDate) > 0){
				 response.setMessage(MessageType.SUCCESS, "設定財金營業日成功");
	        } else {
	            response.setMessage(MessageType.DANGER, "設定財金營業日失敗");
	        }
		} catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            response.setMessage(MessageType.DANGER, programError);
        }
		return response;
	}
	
}
