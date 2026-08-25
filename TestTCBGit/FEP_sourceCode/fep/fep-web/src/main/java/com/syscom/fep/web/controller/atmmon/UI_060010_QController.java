package com.syscom.fep.web.controller.atmmon;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Atmmstr;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.form.atmmon.UI_060010_Q_FormDetail;
import com.syscom.fep.web.form.atmmon.UI_060010_Q_FormMain;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * ATM基本資料查詢
 *
 * @author bruce
 */
@Controller
public class UI_060010_QController extends BaseController {

    @Autowired
    private AtmService atmService;

    private final String pleaseChoose = "全部";

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_060010_Q_FormMain form = new UI_060010_Q_FormMain();
        form.setUrl("/atmmon/UI_060010_Q/queryClick");
//		form.setRbtnDelFlg("2");//預設全部
        try {
            //廠牌下拉選單
            //this.setVendor(mode);//20230322 Bruce 先註解
            //gatewayip下拉選單
            this.getAtmpIpDrop(mode);
            //apversion下拉選單
            this.getApVersionDrop(mode); //20260330 zonghao新增
            this.bindGridData(form, mode);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    /**
     * 查詢
     *
     * @param form
     * @param mode
     * @return
     */
    @PostMapping(value = "/atmmon/UI_060010_Q/queryClick")
    public String queryClick(@ModelAttribute UI_060010_Q_FormMain form, ModelMap mode) {
        this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
        //gatewayip下拉選單
        this.getAtmpIpDrop(mode);
        //apversion下拉選單
        this.getApVersionDrop(mode); //20260330 zonghao新增
        this.bindGridData(form, mode);
        this.doKeepFormData(mode, form);
        return Router.UI_060010_Q.getView();
    }

    /**
     * ATM代號超連結
     *
     * @param mode
     * @return
     */
    @PostMapping(value = "/atmmon/UI_060010_Q/resultGrdvRowCommand")
    public String resultGrdvRowCommand(@ModelAttribute UI_060010_Q_FormDetail formDetail, ModelMap mode) {
        this.infoMessage("查詢主檔資料, 條件 = [", formDetail.toString(), "]");
        this.doKeepFormData(mode, formDetail);
        try {
            formDetail.setAtmAtmTypeCodeTxt(formDetail.getAtmAtmType() + "-" + formDetail.getAtmAtmTypeTxt());
            formDetail.setAtmLocCodeTxt(formDetail.getAtmLoc() + "-" + formDetail.getAtmLocTxt());
//			form.setAtmBrNoMaTxt(atmService.getBRAlias(form.getAtmBrNoMa()));
            //this.setVendor(mode);//廠牌下拉選單20230322 Bruce 先註解
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_060010_Q_Detail.getView();
    }

    @PostMapping(value = "/atmmon/UI_060010_Q/doDownload")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> doDownload(@RequestBody UI_060010_Q_FormMain form, @ModelAttribute ModelMap mode) throws Exception {
        List<Map<String, Object>> dt;
        try {
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("下載");
            // 塞入auditLog.params
            if (StringUtils.isNotBlank(form.getAtmstatStatus()))
                auditLog.addParam("通訊元件連線狀態", form.getAtmstatStatus());
            if (StringUtils.isNotBlank(form.getAtmAtmpIp()))
                auditLog.addParam("GATEWAY IP", form.getAtmAtmpIp());
            if (StringUtils.isNotBlank(form.getAtmVendor()))
                auditLog.addParam("廠牌", form.getAtmVendor());
            if (StringUtils.isNotBlank(form.getAtmFepConnection()))
                auditLog.addParam("是否連線至FEP", form.getAtmFepConnection());
            if (StringUtils.isNotBlank(form.getAtmIpTxt()))
                auditLog.addParam("ATM IP", form.getAtmIpTxt());
            if (StringUtils.isNotBlank(form.getAtmAtmNoTxt()))
                auditLog.addParam("機器代號", form.getAtmAtmNoTxt());
            if (StringUtils.isNotBlank(form.getAtmCertaliasTxt()))
                auditLog.addParam("憑證版本", form.getAtmCertaliasTxt());
            if (StringUtils.isNotBlank(form.getAtmStatApVersionNTxt()))
                auditLog.addParam("通訊元件版本", form.getAtmStatApVersionNTxt());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            //gatewayip下拉選單
            this.getAtmpIpDrop(mode);
            //apversion下拉選單
            this.getApVersionDrop(mode); //20260330 zonghao新增
            Map<String, Object> argsMap = form.toMap();
            argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
            dt = atmService.getAtmBasicCSV(argsMap);
            if (CollectionUtils.isEmpty(dt)) {
                return this.handleDownloadError("查無資料無法下載!!!");
            }
            this.showMessage(mode, MessageType.INFO, "下載成功");
            return this.download(StringUtils.join("ATMDATA_", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN), ".xlsx"),
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    os -> makeXlsx(os, dt));
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            return this.handleDownloadException(e);
        }
    }

    private void makeXlsx(OutputStream os, List<Map<String, Object>> dt) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("ATMData");

            Row headerRow = sheet.createRow(0);

            //檔案標題
            String[] headerTitles = {"分行", "設備功能", "憑證版本", "廠牌", "行內外點", "機型", "IP Address", "機器代號","是否連線至FEP(Y/N)", "備註", "縣市", "啟用日期", "地址", "裝置地點", "原廠機器序號", "通訊元件版本"};
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setWrapText(true);

            for (int i = 0; i < headerTitles.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headerTitles[i]);
                cell.setCellStyle(cellStyle);
            }

            //檔案內容
            int rowNum = 1;
            for (Map<String, Object> dr : dt) {
                Row row = sheet.createRow(rowNum++);
                String ATM_BRANCH_NAME_C = "";
                String ATM_TYPE_CODETXT = "";
                String ATM_CERTALIAS = "";
                String ATM_VENDOR = "";
                String ATM_LOC = "";
                String ATM_MODELNO = "";
                String ATM_IP = "";
                String ATM_ATMNO = "";
                String ATM_FEP_CONNECTION = "";//2025.4.14 增加是否連線至FEP
                String ATM_MEMO = "";
                String ATM_CITY_C = "";
                String ATM_START_DATE = "";
                String ATM_ADDRESS_C = "";
                String ATM_LOCATION = "";
                String ATM_SNO = "";
                String ATMSTAT_AP_VERSION_N = "";

                if (dr.get("ATM_BRANCH_NAME_C") == null || StringUtils.isBlank(dr.get("ATM_BRANCH_NAME_C").toString())) {
                    ATM_BRANCH_NAME_C = "";
                } else {
                    ATM_BRANCH_NAME_C = dr.get("ATM_BRANCH_NAME_C").toString();
                }

                if (dr.get("ATM_ATMTYPE") == null || StringUtils.isBlank(dr.get("ATM_ATMTYPE").toString())) {
                    ATM_TYPE_CODETXT = "";
                } else {
                    ATM_TYPE_CODETXT = dr.get("ATM_ATMTYPE").toString() + "-" + this.getAtmTypeName(dr.get("ATM_ATMTYPE").toString() == null ? "" : dr.get("ATM_ATMTYPE").toString());
                }

                if (dr.get("ATM_CERTALIAS") == null || StringUtils.isBlank(dr.get("ATM_CERTALIAS").toString())) {
                    ATM_CERTALIAS = "";
                } else {
                    ATM_CERTALIAS = dr.get("ATM_CERTALIAS").toString();
                }

                if (dr.get("ATMSTAT_AP_VERSION_N") == null || StringUtils.isBlank(dr.get("ATMSTAT_AP_VERSION_N").toString())) {
                    ATMSTAT_AP_VERSION_N = "";
                } else {
                    ATMSTAT_AP_VERSION_N = dr.get("ATMSTAT_AP_VERSION_N").toString();
                }

                if (dr.get("ATM_VENDOR") == null) {
                    ATM_VENDOR = "";
                } else {
                    if (dr.get("ATM_VENDOR").toString().equals("1")) {
                        ATM_VENDOR = "三商";
                    } else if (dr.get("ATM_VENDOR").toString().equals("6")) {
                        ATM_VENDOR = "迪堡多富";
                    } else {
                        ATM_VENDOR = "未知";
                    }
                }

                if (dr.get("ATM_LOC") == null || StringUtils.isBlank(dr.get("ATM_LOC").toString())) {
                    ATM_LOC = "";
                } else {
                    ATM_LOC = dr.get("ATM_LOC").toString() + "-" + this.getAtmLocName(dr.get("ATM_LOC").toString() == null ? "" : dr.get("ATM_LOC").toString());
                }

                if (dr.get("ATM_MODELNO") == null || StringUtils.isBlank(dr.get("ATM_MODELNO").toString())) {
                    ATM_MODELNO = "";
                } else {
                    ATM_MODELNO = dr.get("ATM_MODELNO").toString();
                }

                if (dr.get("ATM_IP") == null || StringUtils.isBlank(dr.get("ATM_IP").toString())) {
                    ATM_IP = "";
                } else {
                    ATM_IP = dr.get("ATM_IP").toString();
                }

                if (dr.get("ATM_ATMNO") == null || StringUtils.isBlank(dr.get("ATM_ATMNO").toString())) {
                    ATM_ATMNO = "";
                } else {
                    ATM_ATMNO = dr.get("ATM_ATMNO").toString();
                }

                if (dr.get("ATM_FEP_CONNECTION") == null || StringUtils.isBlank(dr.get("ATM_FEP_CONNECTION").toString())) {
                    ATM_FEP_CONNECTION = "";
                } else {
                    if (Objects.equals(dr.get("ATM_FEP_CONNECTION").toString(), "0"))
                        ATM_FEP_CONNECTION = "N";
                    else if (Objects.equals(dr.get("ATM_FEP_CONNECTION").toString(), "1"))
                        ATM_FEP_CONNECTION = "Y";
                    else
                    ATM_FEP_CONNECTION = dr.get("ATM_FEP_CONNECTION").toString();
                }

                if (dr.get("ATM_MEMO") == null || StringUtils.isBlank(dr.get("ATM_MEMO").toString())) {
                    ATM_MEMO = "";
                } else {
                    ATM_MEMO = dr.get("ATM_MEMO").toString();
                }

                if (dr.get("ATM_CITY_C") == null || StringUtils.isBlank(dr.get("ATM_CITY_C").toString())) {
                    ATM_CITY_C = "";
                } else {
                    ATM_CITY_C = dr.get("ATM_CITY_C").toString();
                }

                if (dr.get("ATM_START_DATE") == null || StringUtils.isBlank(dr.get("ATM_START_DATE").toString())) {
                    ATM_START_DATE = "";
                } else {
                    ATM_START_DATE = dr.get("ATM_START_DATE").toString();
                }

                if (dr.get("ATM_ADDRESS_C") == null || StringUtils.isBlank(dr.get("ATM_ADDRESS_C").toString())) {
                    ATM_ADDRESS_C = "";
                } else {
                    ATM_ADDRESS_C = dr.get("ATM_ADDRESS_C").toString();
                }

                if (dr.get("ATM_LOCATION") == null || StringUtils.isBlank(dr.get("ATM_LOCATION").toString())) {
                    ATM_LOCATION = "";
                } else {
                    ATM_LOCATION = dr.get("ATM_LOCATION").toString();
                }

                if (dr.get("ATM_SNO") == null || StringUtils.isBlank(dr.get("ATM_SNO").toString())) {
                    ATM_SNO = "";
                } else {
                    ATM_SNO = dr.get("ATM_SNO").toString();
                }

                String[] body = {ATM_BRANCH_NAME_C, ATM_TYPE_CODETXT, ATM_CERTALIAS, ATM_VENDOR, ATM_LOC, ATM_MODELNO, ATM_IP, ATM_ATMNO, ATM_FEP_CONNECTION
                        , ATM_MEMO, ATM_CITY_C, ATM_START_DATE, ATM_ADDRESS_C, ATM_LOCATION, ATM_SNO, ATMSTAT_AP_VERSION_N};
                cellStyle = workbook.createCellStyle();
                cellStyle.setAlignment(HorizontalAlignment.LEFT);
                cellStyle.setWrapText(false);

                for (int i = 0; i < body.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(body[i]);
                    cell.setCellStyle(cellStyle);
                }
            }
            workbook.write(os);
        }
    }

    /**
     * 將錯誤及查無ATM的編號串起來丟至前端畫面
     * @param atmNo
     * @return
     */
//	private StringBuilder getAtmNoToStringBuilder(List<String> atmNo) {
//		StringBuilder sb = new StringBuilder();
//		//將查無ATM編號串起來
//		for(int n = 0 ; n < atmNo.size() ; n++) {
//			if(atmNo.size() == 1) {
//				sb.append("["+atmNo.get(n)+"]");//只有一筆的時候
//			}else if(n == 0){
//				sb.append("["+atmNo.get(n)+",");//不止一筆的第一筆
//			}else if(atmNo.size() - 1 == n){
//				sb.append(atmNo.get(n)+"]");//不止一筆的最後一筆
//			}else {
//				sb.append(atmNo.get(n)+",");//不止一筆的過程
//			}
//		}
//		return sb;
//	}

//	/**
//	 * 因為有可能是先上傳檔案再使用超連結，所以上一頁的功能會有問題
//	 * @param form
//	 * @param mode
//	 * @return
//	 */
//	@PostMapping(value = "/atmmon/UI_060010_Q/prevPage")
//	public String prevPage(@ModelAttribute UI_060010_Q_FormDetail formDetail, ModelMap mode) {
//		this.infoMessage("查詢主檔資料, 條件 = [", formDetail.toString(), "]");
//		UI_060010_Q_FormMain formMain = new UI_060010_Q_FormMain();
//		formMain.setAtmAtmNoTxt(formDetail.getAtmAtmNo());
//		formMain.setRbtnDelFlg(formDetail.getRbtnDelFlg());
//		formMain.setInsBrno(formDetail.getInsBrno());
//		formMain.setCheckCtrlEmv(formDetail.getCheckCtrlEmvB());
//		formMain.setRbtnOS(formDetail.getRbtnOS());
//		formMain.setTypeQuery(formDetail.getTypeQuery());
//		formMain.setVendor(formDetail.getVendor());
//		WebUtil.putInAttribute(mode, AttributeName.Form, formMain);
//		try {
//			this.bindGridData(formMain, mode);
//		} catch (Exception e) {
//			this.showMessage(mode, MessageType.INFO, UpdateFail);
//			this.bindGridData(formMain, mode);
////			return Router.UI_060010_Q_Detail.getView();
//			return Router.UI_060010_Q.getView();
//		}
//		this.showMessage(mode, MessageType.INFO, UpdateSuccess);
//		this.bindGridData(formMain, mode);
////		return Router.UI_060010_Q_Detail.getView();
//		return Router.UI_060010_Q.getView();
//	}

    /**
     * 修改頁面憑證版本更新 20230324 Bruce add
     *
     * @param form
     * @param mode
     * @return
     */
    @PostMapping(value = "/atmmon/UI_060010_Q/updateCertalias")
    public String updateDetail(@ModelAttribute UI_060010_Q_FormDetail form, ModelMap mode) {
        this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
        form.setUrl("/atmmon/UI_060010_Q/queryClick");
        this.doKeepFormData(mode, form);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        try {
            Map<String, Object> map = form.toMap();
            atmService.updateAtmmstr(map);
        } catch (Exception e) {
            this.showMessage(mode, MessageType.INFO, UpdateFail);
            return Router.UI_060010_Q_Detail.getView();
        }
        this.showMessage(mode, MessageType.INFO, UpdateSuccess);
        return Router.UI_060010_Q_Detail.getView();
    }

    ///
    @PostMapping(value = "/atmmon/UI_060010_Q/updateFepConnect")
    @ResponseBody
    public BaseResp<UI_060010_Q_FormMain> updateFepConnect(@RequestBody List<UI_060010_Q_FormMain> formList, @ModelAttribute ModelMap mode) throws Exception {
        //this.infoMessage("執行更新動作, 條件 = [", formList.toString(), "]");
        BaseResp<UI_060010_Q_FormMain> response = new BaseResp<>();
        try {
//			List<Atmmstr> atmmstr = new ArrayList<Atmmstr>();
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("更新");
            Atmmstr atmmstr = null;
            int atmmstrCount = 0;
            for (UI_060010_Q_FormMain form : formList) {
                // 塞入auditLog.params
                auditLog.addParam("Atmno", form.getAtmAtmNoTxt());
                auditLog.addParam("FepConnection", form.getAtmFepConnection());
                auditLog.addParam("AtmIp", form.getAtmIpTxt());
                atmmstr = new Atmmstr();
                atmmstr.setAtmAtmno(form.getAtmAtmNoTxt());
                atmmstr.setAtmFepConnection(Short.valueOf(form.getAtmFepConnection()));
                atmmstr.setAtmIp(form.getAtmIpTxt());
                atmmstrCount = atmService.updateAtmmstrByFepConnect(atmmstr);
            }
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            if (atmmstrCount > 0) {
                response.setMessage(MessageType.INFO, UpdateSuccess);
            } else {
                response.setMessage(MessageType.INFO, "ATMMSTR資料仍有使用此警示編號，因此無法更新");
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            // show錯誤訊息到前台頁面
            response.setMessage(MessageType.DANGER, DeleteFail);
        }
        return response;
    }

    /**
     * 資料查詢
     *
     * @param form
     * @param mode
     */
    private void bindGridData(UI_060010_Q_FormMain form, ModelMap mode) {
        this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        //分頁
        try {
//			if(form.isCheckCtrlEmvB()) {
//				form.setCheckCtrlEmv("1");
//			}else {
//				form.setCheckCtrlEmv("0");
//			}
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("查詢");
            // 塞入auditLog.params
            if (StringUtils.isNotBlank(form.getAtmstatStatus()))
                auditLog.addParam("通訊元件連線狀態", form.getAtmstatStatus());
            if (StringUtils.isNotBlank(form.getAtmAtmpIp()))
                auditLog.addParam("GATEWAY IP", form.getAtmAtmpIp());
            if (StringUtils.isNotBlank(form.getAtmVendor()))
                auditLog.addParam("廠牌", form.getAtmVendor());
            if (StringUtils.isNotBlank(form.getAtmFepConnection()))
                auditLog.addParam("是否連線至FEP", form.getAtmFepConnection());
            if (StringUtils.isNotBlank(form.getAtmIpTxt()))
                auditLog.addParam("ATM IP", form.getAtmIpTxt());
            if (StringUtils.isNotBlank(form.getAtmAtmNoTxt()))
                auditLog.addParam("機器代號", form.getAtmAtmNoTxt());
            if (StringUtils.isNotBlank(form.getAtmCertaliasTxt()))
                auditLog.addParam("憑證版本", form.getAtmCertaliasTxt());
            if (StringUtils.isNotBlank(form.getAtmStatApVersionNTxt()))
                auditLog.addParam("通訊元件版本", form.getAtmStatApVersionNTxt());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            Map<String, Object> argsMap = form.toMap();
            argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
            PageInfo<Map<String, Object>> pageInfo = atmService.getAtmBasicList(argsMap);
            if (pageInfo.getSize() == 0) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            } else {
                //欄位加工
                this.setFields(pageInfo.getList(), mode);
                this.showMessage(mode, MessageType.INFO, QuerySuccess);
            }
            PageData<UI_060010_Q_FormMain, Map<String, Object>> pageData = new PageData<UI_060010_Q_FormMain, Map<String, Object>>(pageInfo, form);
            //this.setVendor(mode);//廠牌下拉選單 20230322 Bruce 先註解
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    /**
     * 24小時服務及連線層換KEY 轉換是或否
     * @param code
     * @return
     */
//	private String bitToYesNo(String code) {
//		switch(code) {
//			case "0":return "否";
//			case "1":return "是";
//			default :return "";
//		}
//	}

    /**
     * 取得保全中文
     * @param guardCode
     * @param mode
     * @return
     * @throws Exception
     */
//	private String getGuardName(String guardCode, ModelMap mode) throws Exception {
//		Guard guard = atmService.getGuardName(guardCode);
//		if (guard != null) {
//			return guard.getGuardNameS();
//		}
//		return "";
//	}

    /**
     * 廠牌下拉選單 20230322 先註解
     * @throws Exception
     */
//	private void setVendor(ModelMap mode) throws Exception {
//		List<Vendor> vendorList = atmService.qetAllVendor();
//		List<SelectOption<String>> selectOptionList = new ArrayList<SelectOption<String>>();
//		selectOptionList.add(new SelectOption<String>(StringUtils.EMPTY, StringUtils.EMPTY));
//		for (int i = 0; i < vendorList.size(); i++) {
//			selectOptionList.add(new SelectOption<String>(vendorList.get(i).getVendorNameS(), vendorList.get(i).getVendorNo()));
//		}
//		WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList);
//	}

    /**
     * 畫面顯示欄位加工
     *
     * @throws Exception
     */
    private void setFields(List<Map<String, Object>> argsMapList, ModelMap mode) throws Exception {
        for (Map<String, Object> map : argsMapList) {
//			map.put("ATM_BRNO_ST_TXT", atmService.getBRAlias(map.get("ATM_BRNO_ST").toString()));
            map.put("ATM_BRNO_ST_TXT", "");
            map.put("ATM_CUR_ST_TXT", this.getZoneCurName(map.get("ATM_CUR_ST").toString()));
            map.put("ATM_ZONE_TXT", this.getAtmZoneName(map.get("ATM_ZONE") == null ? "" : map.get("ATM_ZONE").toString()));
            map.put("ATM_BRNO_ST_ALIAS_TXT", map.get("ATM_BRNO_ST") == null ? "" : map.get("ATM_BRNO_ST").toString() + "-" + this.getAtmZoneName(
                    map.get("ATM_BRNO_ST_ALIAS") == null ? "" : map.get("ATM_BRNO_ST_ALIAS").toString()));
            map.put("ATM_ATMTYPE_TXT", this.getAtmTypeName(map.get("ATM_ATMTYPE") == null ? "" : map.get("ATM_ATMTYPE").toString()));
            map.put("ATM_AREA_TXT", this.getAtmAreaName(map.get("ATM_AREA") == null ? "" : map.get("ATM_AREA").toString()));
            map.put("ATM_CHANNEL_TYPE_TXT", this.getAtmChannelTypeName(map.get("ATM_CHANNEL_TYPE") == null ? "" : map.get("ATM_CHANNEL_TYPE").toString()));
            map.put("ATM_LOC_TXT", this.getAtmLocName(map.get("ATM_LOC") == null ? "" : map.get("ATM_LOC").toString()));
            //為了更新後帶回前端
            map.put("rbtnDelFlg", map.get("ATM_DELETE_FG"));
            map.put("checkCtrlEmv", map.get("ATM_EMV"));
            map.put("rbtnOS", map.get("ATM_OS"));
            map.put("vendor", map.get("ATM_VENDOR"));
            map.put("typeQuery", map.get("ATM_ATMTYPE"));
            map.put("insBrno", map.get("ATM_INS_BRNO"));
            if (StringUtil.isNotBlank(map.get("ATMSTAT_SERVICE").toString())) {
                map.put("ATMSTAT_SERVICE", map.get("ATMSTAT_SERVICE").toString().equals("1") ? "服務中" : "停止服務");
            }
            //20230913依照明祥需求增加atmstat的欄位
            if (StringUtil.isNotBlank(map.get("ATMSTAT_STATUS").toString())) {
                map.put("ATMSTAT_STATUS", map.get("ATMSTAT_STATUS").toString().equals("0") ? "連線" : "斷線");
            }
            if (map.get("ATMSTAT_ENABLE") != null) {
                map.put("ATMSTAT_ENABLE", map.get("ATMSTAT_ENABLE").toString().equals("true") ? "啟用" : "關閉");
            }
            if (map.get("ATMSTAT_LAST_OPEN") != null) {
                map.put("ATMSTAT_LAST_OPEN", map.get("ATMSTAT_LAST_OPEN").toString().substring(0, 19));
            }
            if (map.get("ATMSTAT_LAST_CLOSE") != null) {
                map.put("ATMSTAT_LAST_CLOSE", map.get("ATMSTAT_LAST_CLOSE").toString().substring(0, 19));
            }
            if (StringUtil.isNotBlank(map.get("ATM_FEP_CONNECTION").toString())) {
                map.put("ATM_FEP_CONNECTIONTXT", map.get("ATM_FEP_CONNECTION").toString().equals("1") ? "是" : "否");
            }
            if (map.get("ATM_VENDOR") != null) {
                if (map.get("ATM_VENDOR").toString().equals("1")) {
                    map.put("ATM_VENDOR", "三商");
                    map.put("ATM_VENDOR_TXT", "三商");
                } else if (map.get("ATM_VENDOR").toString().equals("6")) {
                    map.put("ATM_VENDOR", "迪堡多富");
                    map.put("ATM_VENDOR_TXT", "迪堡多富");
                } else {
                    map.put("ATM_VENDOR", "未知");
                    map.put("ATM_VENDOR_TXT", "未知");
                }
            }
//			map.put("insBrno", map.get("ATM_INS_BRNO"));
        }
    }

    /**
     * 轉換國家中文
     *
     * @param atmZone
     */
    private String getAtmZoneName(String atmZone) {
        if (StringUtils.isBlank(atmZone)) {
            return "";
        } else {
            switch (atmZone) {
                case "HKG": return "香港";
                case "MAC": return "澳門";
                case "TWN": return "台灣";
                default: return atmZone;
            }
        }
    }

    /**
     * 將廠牌轉成中文
     * @param atmVendor
     * @return
     * @throws Exception
     */
//	private String getAtmDevVendorName(String atmVendor,ModelMap mode) throws Exception {
//		return atmService.queryVendorNameByPK(atmVendor);
//	}

    /**
     * 型態別轉中文
     *
     * @param atmType
     * @return
     */
    private String getAtmTypeName(String atmType) {
        if (StringUtils.isBlank(atmType)) {
            return "";
        }
        switch (atmType) {
            case "0": return "提款機";
            case "1": return "台外幣提款機";
            case "2": return "存提款機";
            case "3": return "WebATM";
            case "4": return "存提補機";
            default: return "";
        }
    }

    /**
     * 地區代碼轉中文
     *
     * @param atmArea
     * @return
     */
    private String getAtmAreaName(String atmArea) {
        if (StringUtils.isBlank(atmArea)) {
            return "";
        }
        switch (atmArea) {
            case "1": return "北";
            case "2": return "中";
            case "3": return "南";
            case "4": return "新竹";
            case "5": return "高雄";
            default: return "";
        }
    }

    /**
     * 通路轉中文
     *
     * @param atmChannelType
     * @return
     */
    private String getAtmChannelTypeName(String atmChannelType) {
        if (StringUtils.isBlank(atmChannelType)) {
            return "";
        }
        switch (atmChannelType) {
            case "0": return "分行";
            case "1": return "策略設置";
            case "2": return "業務配合";
            case "3": return "證券";
            case "4": return "萊爾富";
            default: return "";
        }
    }

    /**
     * 行內外別轉中文
     *
     * @return
     */
    private String getAtmLocName(String atmLoc) {
        if (StringUtils.isBlank(atmLoc)) {
            return "";
        } else {
            switch (atmLoc) {
                case "0": return "行內";
                case "1": return "證券自";
                case "2": return "行外778結帳";
                default: return atmLoc;
            }
        }
    }

    /**
     * gateway ip 下拉選單
     */
    private void getAtmpIpDrop(ModelMap mode) {
        try {
            List<Map<String, String>> atmmstrList = atmService.getAtmAtmpIp();
            List<SelectOption<String>> selectOptionList = new ArrayList<SelectOption<String>>();
            selectOptionList.add(new SelectOption<String>(this.pleaseChoose, StringUtils.EMPTY));

            for (int i = 0; i < atmmstrList.size(); i++) {
                selectOptionList.add(new SelectOption<String>(atmmstrList.get(i).get("ATM_ATMP_IP"), atmmstrList.get(i).get("ATM_ATMP_IP")));
            }
            WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    /**
     * Ap Version 下拉選單
     */
    private void getApVersionDrop(ModelMap mode) {
        try {
            List<Map<String, String>> atmmstrList = atmService.getAtmApVersion();
            List<SelectOption<String>> selectOptionList = new ArrayList<SelectOption<String>>();
            selectOptionList.add(new SelectOption<String>(this.pleaseChoose, StringUtils.EMPTY));

            for (int i = 0; i < atmmstrList.size(); i++) {
                selectOptionList.add(new SelectOption<String>(atmmstrList.get(i).get("ATMSTAT_AP_VERSION_N"), atmmstrList.get(i).get("ATMSTAT_AP_VERSION_N")));
            }
            WebUtil.putInAttribute(mode, AttributeName.Options2, selectOptionList);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }
}

