package com.syscom.fep.web.controller.rm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Allbank;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SelectOption;
import com.syscom.fep.web.form.rm.UI_020061_Form;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 往來行庫資料維護
 * @author xingyun_yang
 * @create 2021/11/23
 */
@Controller
public class UI_020061_CController extends BaseController {
    @Autowired
    RmService rmService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_020061_Form form = new UI_020061_Form();
        form.setAllbankBkno("");
        form.setAllbankBrno("");
        form.setCountyDDL("");
        form.setRegionDDL("");
        query(form,mode);
        getCountyList(mode);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }
    private void getCountyList(ModelMap mode) {
        try {
            List<String> address = rmService.getCountyList();
            List<SelectOption<String>> selectOptionList = new ArrayList<>();
            for (String string : address) {
                if ("".equals(string.trim()) || "　　　".equals(string.trim())) {
                    continue;
                }
                selectOptionList.add(new SelectOption<>(string, string));
            }
            mode.addAttribute("oCountyDDLs",selectOptionList);
        } catch (Exception e) {
            this.showMessage(mode, MessageType.DANGER, "載入縣市選單發生錯誤");
        }
    }

    /**
     * 資料查詢
     */
    @PostMapping(value = "/rm/UI_020061_C/index")
    private String queryClick(@ModelAttribute UI_020061_Form form, ModelMap mode) {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        getCountyDDL_SelectedIndexChanged(form, mode);
        return Router.UI_020061_C.getView();
    }

    /**
     * 資料修改
     * 選取資料的處理。自資料庫中取出該筆明細資料，並切換至FormView中提供編輯功能
     */
    @PostMapping(value = "/rm/UI_020061_C/resultGrdv_RowCommand")
    private String resultGrdv_RowCommand(UI_020061_Form form, ModelMap mode) {
        Allbank defAllbank = null;
        try {
            defAllbank = new Allbank();
            defAllbank.setAllbankBkno(form.getAllbankBkno());
            defAllbank.setAllbankBrno(form.getAllbankBrno());
            defAllbank.setAllbankRmforward("0");
            if (rmService.updateALLBANK(defAllbank)<1){
                this.showMessage(mode, MessageType.DANGER, "往來行庫資料更新失敗");
            }else {
                this.showMessage(mode, MessageType.INFO, "往來行庫資料更新成功");
                this.prepareAndSendEMSData("往來行庫資料更新成功: 銀行別="+defAllbank.getAllbankBkno()
                +", 分行別="+form.getAllbankBrno());
            }
            //Query again
            form.setAllbankBkno("");
            form.setAllbankBrno("");
            form.setCountyDDL("");
            form.setRegionDDL("");
            getCountyDDL_SelectedIndexChanged(form, mode);
        } catch (Exception e) {
        	this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
        }

        return Router.UI_020061_C.getView();
    }

    /**
     * 下拉框改變時間 查詢市縣鎮區
     */
    @PostMapping(value = "/rm/UI_020061_C_Detail/countyDDL_SelectedIndexChanged")
    protected String countyDDL_SelectedIndexChanged(@ModelAttribute UI_020061_Form form, ModelMap mode){
        this.doKeepFormData(mode, form);
        getCountyDDL_SelectedIndexChanged(form, mode);
        return Router.UI_020061_C.getView();
    }
    private void getCountyDDL_SelectedIndexChanged(UI_020061_Form form, ModelMap mode){
        Allbank allbank = null;
        try {
            if ("".equals(form.getCountyDDL())){
                getCountyList(mode);
                query(form,mode);
                return;
            }
            allbank = new Allbank();
            allbank.setAllbankAddrress(form.getCountyDDL());
            List<Allbank> addressList = rmService.getALLBANKByPKLike(allbank);
            List<String> region = new ArrayList<>();
            for (Allbank address : addressList) {
                if (address.getAllbankAddrress().substring(3).contains("市")) {
                    address.setAllbankAddrress(address.getAllbankAddrress().substring(3, address.getAllbankAddrress().indexOf("市") + 1));
                } else if (address.getAllbankAddrress().substring(3).contains("鄉")) {
                    address.setAllbankAddrress(address.getAllbankAddrress().substring(3, address.getAllbankAddrress().indexOf("鄉") + 1));
                } else if (address.getAllbankAddrress().substring(3).contains("鎮")) {
                    address.setAllbankAddrress(address.getAllbankAddrress().substring(3, address.getAllbankAddrress().indexOf("鎮") + 1));
                } else if (address.getAllbankAddrress().substring(3).contains("區")) {
                    address.setAllbankAddrress(address.getAllbankAddrress().substring(3, address.getAllbankAddrress().indexOf("區") + 1));
                }else {
                    continue;
                }
                region.add(address.getAllbankAddrress());
            }
            HashSet<String> h = new HashSet<String>(region);
            region.clear();
            region.addAll(h);
            List<SelectOption<String>> selectOptionList = new ArrayList<>();
            for (String string : region) {
                if ("".equals(string.trim()) || "　　　".equals(string.trim())) {
                    continue;
                }
                selectOptionList.add(new SelectOption<>(string, string));
            }
            mode.addAttribute("oRegionDDLs",selectOptionList);
        } catch (Exception e) {
            this.showMessage(mode, MessageType.DANGER, "載入縣市選單發生錯誤");
        }
        getCountyList(mode);
        query(form,mode);
    }
    /**
     * 依查詢條件查詢的主程式。
     */
    private void query(UI_020061_Form form, ModelMap mode){
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            Allbank defAllbank = new Allbank();
            if (StringUtils.isNotBlank(form.getAllbankBkno().trim())){
                defAllbank.setAllbankBkno(form.getAllbankBkno().trim());
            }
            if (StringUtils.isNotBlank(form.getAllbankBrno().trim())){
                defAllbank.setAllbankBrno(form.getAllbankBrno().trim());
            }
            if (StringUtils.isNotBlank(form.getCountyDDL().trim())){
                defAllbank.setAllbankAddrress(form.getCountyDDL().trim());
            }
            if (StringUtils.isNotBlank(form.getRegionDDL().trim())){
                defAllbank.setAllbankAddrress(defAllbank.getAllbankAddrress()+form.getRegionDDL().trim());
            }
            defAllbank.setAllbankRmforward("2");
            PageInfo<Allbank> pageInfo = rmService.getALLBANKByPKRmPageINfo(form.getPageNum(),form.getPageSize(),defAllbank);
            if(pageInfo.getSize() == 0){
                this.showMessage(mode, MessageType.INFO, "全國銀行檔無暫停服務之行庫資料");
                return;
            }
            PageData<UI_020061_Form, Allbank> pageData = new PageData<>(pageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
        	this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    /**
     * 執行成功送成功信息
     */
    private void prepareAndSendEMSData(String strMsg) throws Exception {
        logContext.setChannel(FEPChannel.FEP);
        logContext.setSubSys(SubSystem.RM);
        logContext.setProgramName("UI_020061");
        logContext.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        logContext.setDesBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        logContext.setMessageId("UI020061");
        /*Rm*/
        logContext.setMessageGroup("4");
        logContext.setRemark(strMsg);
        logContext.setTxUser(WebUtil.getUser().getUserId());
        logContext.setReturnCode(FEPReturnCode.AllbankStopService);
        logContext.setMessageParm13("往來行庫資料維護:"+strMsg);
        TxHelper.getRCFromErrorCode(logContext.getReturnCode(),FEPChannel.FEP,logContext);
    }
}
