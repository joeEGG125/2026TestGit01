package com.syscom.fep.web.controller.inbk;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.web.audit.AuditLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.FeptxnExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_012280_Form;
import com.syscom.fep.web.form.inbk.UI_012280_FormDetail;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 請求傳送滯留訊息-2280(本營業日)
 *
 * @author ChenYu
 */
@Controller
public class UI_012280Controller extends BaseController {
    @Autowired
    InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_012280_Form form = new UI_012280_Form();
        // 交易日期
        form.setTradingDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        // 財金STAN
//        try {
//            form.setBkno(SysStatus.getPropertyValue().getSysstatFbkno());
//        } catch (Exception ex) {
//            this.showMessageWithArgs(mode, MessageType.DANGER, DATA_INQUIRY_EXCEPTION_OCCUR, "財金STAN", ex.getMessage());
//            this.errorMessage(ex, ex.getMessage());
//        }
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/inbk/UI_012280/inquiryMain")
    public String getpendingDateStanBkno(@ModelAttribute UI_012280_Form form, ModelMap mode) {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
//        InbkpendExtMapper inbkpendExtMapper = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
        FeptxnExtMapper feptxnExtMapper = SpringBeanFactoryUtil.getBean(FeptxnExtMapper.class);
        String status = "";
        Feptxn aFEPTXN = new FeptxnExt();
        String wk_SYS_DATETIME;
        String wk_DATE;
        List<Zone> zoneList = null;
        Map<String,Zone> zoneCodeMap = new HashMap<>();
        try {
            form.setQueryflagtxt("");
            if (zoneList == null){
                zoneList = inbkService.selectAll();
            }
            for (int i = 0; i < zoneList.size(); i++) {
                zoneCodeMap.put(zoneList.get(i).getZoneCode(),zoneList.get(i));
//                zoneCodeMap.put(zoneList.get(i).getZoneVirtualBrno(),zoneList.get(i));
            }
//            If Zones Is Nothing Then
//            Using db As New Tables.DBZONE(FEPConfig.DBName)
//            Zones = db.QueryAll("")
//            End Using
//            End If
            aFEPTXN.setFeptxnTxDate(form.getTradingDate().replace("-", ""));
            aFEPTXN.setFeptxnBkno(form.getBkno().trim());
            aFEPTXN.setFeptxnStan(StringUtils.leftPad(form.getStan().trim(), 7, "0"));
            if (Double.valueOf(new SimpleDateFormat("HHmm").format(new Date())) >= 1500 && Double.valueOf(new SimpleDateFormat("HHmm").format(new Date())) <= 1600 && DbHelper.toBoolean(zoneCodeMap.get("TWN").getZoneChgday())) {
                wk_DATE = SysStatus.getPropertyValue().getSysstatLbsdyFisc(); //財金前營業日
            } else {
                wk_DATE = SysStatus.getPropertyValue().getSysstatTbsdyFisc(); //財金本營業日
            }
            wk_SYS_DATETIME = LocalDateTime.now().plusMinutes(-5).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("查詢");
            // 塞入auditLog.params
            if (StringUtils.isNotBlank(form.getTradingDate()))
                auditLog.addParam("交易日期", form.getTradingDate());
            if (StringUtils.isNotBlank(form.getBkno()))
                auditLog.addParam("財金STAN:bkno", form.getBkno());
            if (StringUtils.isNotBlank(form.getStan()))
                auditLog.addParam("財金STAN:stan", form.getStan());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            aFEPTXN = inbkService.getFEPTXNFor2280(aFEPTXN, wk_SYS_DATETIME, SysStatus.getPropertyValue().getSysstatHbkno(), wk_DATE.substring(6, 8));

            if (aFEPTXN == null) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
                form.setQueryflagtxt("True");
                mode.put("queryflagtxt",form.getQueryflagtxt());
            } else if(!StringUtils.equals(aFEPTXN.getFeptxnCbsProc(), "N")) { //只查詢3-2的判斷
            	this.showMessage(mode, MessageType.INFO, QueryNoData);
                form.setQueryflagtxt("True");
                mode.put("queryflagtxt",form.getQueryflagtxt());
            } else {
                form.setQueryflagtxt("True");
                mode.put("queryflagtxt",form.getQueryflagtxt());
                WebUtil.putInAttribute(mode, AttributeName.DetailEntity, aFEPTXN);
            }
        }catch (Exception ex){
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
//            this.showMessage(mode, MessageType.DANGER,ex.getMessage());
        }

        return Router.UI_012280.getView();
    }

    @PostMapping(value = "/inbk/UI_012280/inquiryDetail")
    @ResponseBody
    public BaseResp execute(@RequestBody UI_012280_FormDetail form, ModelMap mode){
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        BaseResp response = new BaseResp();

        try{
            if ( !"True".equals(form.getQueryflagtxt())) {
                response.setMessage(MessageType.DANGER, PleaseDoQuery);
                return response;
            }
            FISCGeneral aData = new FISCGeneral();
            aData.setINBKRequest(new FISC_INBK());
            aData.setSubSystem(FISCSubSystem.INBK);
            aData.getINBKRequest().setMessageKind(MessageFlow.Request);
            aData.getINBKRequest().setProcessingCode("2280");
            aData.getINBKRequest().setMessageType("0200");
            aData.getINBKRequest().setTxnInitiateDateAndTime(CalendarUtil.adStringToROCString(form.getTradingDate().replace("-","")) +
                    new SimpleDateFormat("HHmm").format(new Date()));
            aData.getINBKRequest().setTxnSourceInstituteId(StringUtils.rightPad(form.getBkno().trim(),7,"0"));
            aData.getINBKRequest().setSystemTraceAuditNo(StringUtils.leftPad(form.getStan(),7,"0"));
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("確認");
            // 塞入auditLog.params
            if (StringUtils.isNotBlank(form.getTradingDate()))
                auditLog.addParam("交易日期", form.getTradingDate());
            if (StringUtils.isNotBlank(form.getBkno()))
                auditLog.addParam("財金STAN:bkno", form.getBkno());
            if (StringUtils.isNotBlank(form.getStan()))
                auditLog.addParam("財金STAN:stan", form.getStan());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            // Call AA
            this.infoMessage("Start to Call AA via FEPHandler.dispatch(FEPChannel.FEP, FISCGeneral) by condition = ", form.toString());
            FEPHandler fepHandler = new FEPHandler();
            fepHandler.dispatch(FEPChannel.FEP, aData);
            // 將AA RC 顯示在UI上
            if (StringUtils.isBlank(aData.getDescription())) {
                aData.setDescription(MessageError);
            }
            String[] message = aData.getDescription().split("-");
            // 若AA回的是財金RC(RC-Description)而且財金RC=0001，UI顯示處理成功
            if (message != null && message.length == 2 && NormalRC.FISC_OK.equals(message[0])) {
                response.setMessage(MessageType.INFO, aData.getDescription());
            } else {
                response.setMessage(MessageType.DANGER, aData.getDescription());
            }
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            response.setMessage(MessageType.DANGER,programError);
        }
        return response;
    }
}
