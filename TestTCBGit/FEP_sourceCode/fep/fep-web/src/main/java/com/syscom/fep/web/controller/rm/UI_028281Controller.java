package com.syscom.fep.web.controller.rm;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Rmin;
import com.syscom.fep.mybatis.model.Rmint;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028281_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * 電文BypassAML人工作業
 *
 * @author xingyun_yang
 * @create 2021/11/30
 */
@Controller
public class UI_028281Controller  extends BaseController {
    @Autowired
    RmService rmService;
    @Autowired
    InbkService inbkService;
    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_028281_Form form = new UI_028281_Form();
        form.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    /**
     * 查詢按鈕
     */
    @PostMapping(value = "/rm/UI_028281/queryClick")
    public String queryClick(@ModelAttribute UI_028281_Form form, ModelMap mode) {
        this.infoMessage("UI_028281查詢, 條件 = [", form, "]");
        this.doKeepFormData(mode, form);
        bindGridData(form,mode);
        return Router.UI_028281.getView();
    }

    /**
     *  按下執行按鈕
     */
    @PostMapping(value = "/rm/UI_028281/executeClick")
    @ResponseBody
    public BaseResp<?> executeClick(@RequestBody List<UI_028281_Form> formList) {
        this.infoMessage("執行UI_028281, 條件 = [", formList.toString(), "]");
        BaseResp<?> response = new BaseResp<>();
        try {
            for (UI_028281_Form form : formList) {
                Rmin defRmin = new Rmin();
                defRmin.setRminTxdate(form.getRmInTxDate());
                defRmin.setRminBrno(form.getRmInBrno());
                defRmin.setRminFepno(form.getRmInFepNo());
                Rmin newRmin = rmService.getRminQueryByPrimaryKey(defRmin);
                if (!"98".equals(newRmin.getRminStat())) {
                    //可能剛好被服務送出了
                    continue;
                }
                defRmin.setRminStat("99");
                defRmin.setRminAmlbypass("1");

                Rmint defRmint = new Rmint();
                defRmint.setRmintTxdate(form.getRmInTxDate());
                defRmint.setRmintBrno(form.getRmInBrno());
                defRmint.setRmintFepno(form.getRmInFepNo());
                Rmint newRmint = rmService.getRmintQueryByPrimaryKey(defRmint);
                if (newRmint == null) {
                    response.setMessage(MessageType.DANGER, "DB中無資料，請檢查後在執行");
                    return response;
                }
                if (!"98".equals(newRmint.getRmintStat())) {
                    //可能剛好被服務送出了
                    continue;
                }
                defRmint.setRmintStat("99");
                defRmint.setRmintAmlbypass("1");
                if (rmService.updateRminByPrimaryKeyWithStat(defRmin, "98") <= 0) {
                    logMessage(String.format("更新RMIN FEPNO={0}失敗，該筆狀態<>98", defRmin.getRminFepno()));
                }
                if (rmService.updateRmintByPrimaryKeyWithStat(defRmint, "98") <= 0) {
                    logMessage(String.format("更新RMINT FEPNO={0}失敗，該筆狀態<>98", defRmin.getRminFepno()));
                }
                //Fly 2019/10/16 AML ByPass增加紀錄EMS
                prepareAndSendEMSData(defRmint.getRmintFepno());
            }
            response.setMessage(MessageType.INFO,"執行成功");
        } catch (Exception ex) {
            response.setMessage(MessageType.DANGER,programError);
            return response;
        }
        return response;
    }
    /**
     *  按下全部執行按鈕
     */
    @PostMapping(value = "/rm/UI_028281/executeBtn_Click")
    @ResponseBody
    public BaseResp<?> executeBtn_Click(@RequestBody String txDate) {
        this.infoMessage("全部執行UI_028281, 條件 = [", txDate.toString(), "]");
        BaseResp<?> response = new BaseResp<>();

        try {
            List<HashMap<String,Object>> dtResult = rmService.getAMLReSendData(StringUtils.replace(StringUtils.replace(txDate,"-", StringUtils.EMPTY),"\"",""));
            if (dtResult==null || dtResult.size()==0){
                response.setMessage(MessageType.DANGER,"查無資料無法執行");
                return response;
            }
            for (HashMap<String, Object> stringObjectHashMap : dtResult) {
                Rmin defRmin = new Rmin();
                defRmin.setRminTxdate(stringObjectHashMap.get("RMIN_TXDATE").toString());
                defRmin.setRminBrno(stringObjectHashMap.get("RMIN_BRNO").toString());
                defRmin.setRminFepno(stringObjectHashMap.get("RMIN_FEPNO").toString());
                Rmin newRmin = rmService.getRminQueryByPrimaryKey(defRmin);

                if (!"98".equals(newRmin.getRminStat())) {
                    //可能剛好被服務送出了
                    continue;
                }
                defRmin.setRminStat("99");
                defRmin.setRminAmlbypass("1");
                if (rmService.updateRminByPrimaryKeyWithStat(defRmin, "98") <= 0) {
                    logMessage(String.format("更新RMIN FEPNO={0}失敗，該筆狀態<>98", defRmin.getRminFepno()));
                }

                Rmint defRmint = new Rmint();
                defRmint.setRmintTxdate(stringObjectHashMap.get("RMIN_TXDATE").toString());
                defRmint.setRmintBrno(stringObjectHashMap.get("RMIN_BRNO").toString());
                defRmint.setRmintFepno(stringObjectHashMap.get("RMIN_FEPNO").toString());
                Rmint newRmint = rmService.getRmintQueryByPrimaryKey(defRmint);
                if (newRmint == null) {
                    response.setMessage(MessageType.DANGER, "DB中無資料，請檢查後在執行");
                    return response;
                }
                if (!"98".equals(newRmint.getRmintStat())) {
                    //可能剛好被服務送出了
                    continue;
                }
                defRmint.setRmintStat("99");
                defRmint.setRmintAmlbypass("1");
                if (rmService.updateRmintByPrimaryKeyWithStat(defRmint, "98") <= 0) {
                    logMessage(String.format("更新RMINT FEPNO={0}失敗，該筆狀態<>98", defRmint.getRmintFepno()));
                }
                //Fly 2019/10/16 AML ByPass增加紀錄EMS
                prepareAndSendEMSData(defRmint.getRmintFepno());
            }
            response.setMessage(MessageType.INFO,"執行成功");
        } catch (Exception ex) {
            response.setMessage(MessageType.DANGER,programError);
            return response;
        }
        return response;
    }
    /**
     * PrepareAndSendEMSData
     */
    private void prepareAndSendEMSData(String uNISEQNO) throws Exception {
        LogData logData = new LogData();
        logData.setDesBkno("807");
        logData.setMessageGroup("4");
        logData.setSubSys(SubSystem.RM);
        logData.setChannel(FEPChannel.FEP);
        logData.setProgramName("UI_028281");
        logData.setMessageParm13(String.format("FEP序號 = {0}",uNISEQNO));
        logData.setReturnCode(RMReturnCode.BypassAML);
        //由GetMessageFromFEPReturnCode明細 SendEMS
        TxHelper.getMessageFromFEPReturnCode(logData.getReturnCode(),logData);
    }
    /**
     * 資料整理
     */
    private PageInfo<HashMap<String, Object>> getGridData(String txDateS, int pageNum, int pageSize, ModelMap mode){
        PageInfo<HashMap<String, Object>> dt = null;
        try {
            String txDate_s =  StringUtils.replace(txDateS,"-", StringUtils.EMPTY);
            dt = rmService.getAMLReSendData(txDate_s,pageNum,pageSize);
        } catch (Exception ex) {
            logMessage(ex.getMessage());
            this.showMessage(mode, MessageType.DANGER,programError);
        }
        return dt;
    }
    private void bindGridData(UI_028281_Form form,ModelMap mode){
        try {
            PageInfo<HashMap<String, Object>> dtResult = getGridData(form.getTxDate(),form.getPageNum(),form.getPageSize(),mode);
            if (dtResult.getList() ==null ||dtResult.getList().size()==0){
                this.showMessage(mode, MessageType.WARNING,"匯入主檔(RMIN)無符合資料");
            }else {
                this.clearMessage(mode);
            }
            PageData<UI_028281_Form, HashMap<String, Object>> pageData = new PageData<>(dtResult, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER,programError);
        }
    }
    public static String getSNDCode(String msg){
        if (msg.length()<4){
            return msg;
        }
        switch (msg.substring(2,4)){
            case "11":
                return "入戶";
            case "12":
                return "國庫";
            case "13":
                return "同業";
            case "17":
                return "退匯";
            case "18":
                return "證券";
            case "19":
                return "票券";
            default:
                return msg;
        }
    }
    private void logMessage(String msg){
        logContext.setRemark(msg);
        inbkService.inbkLogMessage(Level.INFO,logContext);
    }
}
