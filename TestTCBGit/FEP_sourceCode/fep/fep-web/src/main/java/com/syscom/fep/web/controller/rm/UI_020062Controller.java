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
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SelectOption;
import com.syscom.fep.web.form.rm.UI_020062_Form;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 調整往來行庫服務狀態
 *
 * @author xingyun_yang
 * @create 2021/11/22
 */
@Controller
public class UI_020062Controller extends BaseController {

    @Autowired
    RmService rmService;
    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_020062_Form form = new UI_020062_Form();
        getCountyList(mode);
        form.setFlagDDL("1");
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/rm/UI_020062/btnExecute")
    public String btnExecute(@ModelAttribute UI_020062_Form form, ModelMap mode) {
        this.infoMessage("執行UI_020062, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        @SuppressWarnings("unused")
        List<Allbank> dtALLBNAK = new ArrayList<>();
        try {
            String queryAddress = StringUtils.EMPTY;
            if (!"".equals(form.getElseTb().trim())){
                queryAddress = form.getElseTb().trim();
            }else {
                if (!"".equals(form.getCountyDDL().trim())){
                    queryAddress = form.getCountyDDL().trim();
                }
                if (!"".equals(form.getRegionDDL().trim())){
                    queryAddress += form.getRegionDDL().trim();
                }
            }
            if (!"".equals(queryAddress)){
                Integer updateResult = 0;
                String updateStr = StringUtils.EMPTY;
                dtALLBNAK = rmService.getALLBANKByAddressLike(queryAddress);
                if (dtALLBNAK==null || dtALLBNAK.size()==0){
                    this.showMessage(mode, MessageType.INFO, "全國銀行檔無此行庫資料");
                }else {
                    switch (form.getFlagDDL()){
                        case "0":
                            updateResult = rmService.updateALLBANKByAddressLike(form.getFlagDDL(),"2",queryAddress);
                            updateStr = queryAddress +": 設定可匯出";
                            break;
                        case "1":
                            updateResult = rmService.updateALLBANKByAddressLike("2","0",queryAddress);
                            updateStr = queryAddress +"設定暫停匯出";
                            break;
                        default:
                            break;
                    }
                    updateStr +=", 更新筆數:"+updateResult;
                    if (updateResult>0){
                        prepareAndSendEMSData(updateStr);
                    }
                    this.showMessage(mode, MessageType.INFO, updateStr);
                }
            }else if ("9".equals(form.getFlagDDL())){
                //Fly 2016/10/06 遇颱風天時會定颱風暫停往來行庫，增加功能以利隔天清除颱風天記號
                Integer updateResult = 0;
                String updateStr = StringUtils.EMPTY;
                updateResult=rmService.updateALLBANKByAddressLike("0","2","");
                updateStr = "全面解除, 更新筆數:"+updateResult;
                if (updateResult>0){
                    prepareAndSendEMSData(updateStr);
                }
                this.showMessage(mode, MessageType.INFO, updateStr);
            }else {
                this.showMessage(mode, MessageType.INFO, "區域或其它必須擇一輸入");
            }
        } catch (Exception e) {
            getCountyList(mode);
        }
        getCountyDDL_SelectedIndexChanged(form,mode);
        return Router.UI_020062.getView();
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
    @PostMapping(value = "/rm/UI_020062/countyDDL_SelectedIndexChanged")
    protected String countyDDL_SelectedIndexChanged(@ModelAttribute UI_020062_Form form,ModelMap mode){
        this.doKeepFormData(mode, form);
        getCountyDDL_SelectedIndexChanged(form,mode);
        return Router.UI_020062.getView();
    }
    private void getCountyDDL_SelectedIndexChanged(UI_020062_Form form,ModelMap mode){
        Allbank allbank = null;
        try {
            if ("".equals(form.getCountyDDL())){
                getCountyList(mode);
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
    }
    /**
     * 執行成功送成功信息
     */
    private void prepareAndSendEMSData(String strMsg) throws Exception {
        logContext.setChannel(FEPChannel.FEP);
        logContext.setSubSys(SubSystem.RM);
        logContext.setProgramName("UI_020062");
        logContext.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        logContext.setDesBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        logContext.setMessageId("UI020062");
        /*Rm*/
        logContext.setMessageGroup("4");
        logContext.setRemark(strMsg);
        logContext.setTxUser(WebUtil.getUser().getUserId());
        logContext.setReturnCode(FEPReturnCode.AllbankStopService);
        logContext.setMessageParm13("往來行庫資料維護:"+strMsg);
        TxHelper.getRCFromErrorCode(logContext.getReturnCode(),FEPChannel.FEP,logContext);
    }
}
