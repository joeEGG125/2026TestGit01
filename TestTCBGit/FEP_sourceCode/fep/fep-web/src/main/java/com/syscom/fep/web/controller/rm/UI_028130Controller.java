package com.syscom.fep.web.controller.rm;

import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.rm.RM;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.rm.UI_028130_Form;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UI_028130Controller extends BaseController {

    @Autowired
    RmService rmService;

    //add by maxine on 2011/06/27 for SYSSTAT自行查
    private LogData logContext = new LogData();


    @PostMapping(value = "/rm/UI_028130/queryClick")
    @ResponseBody
    protected UI_028130_Form queryClick(@RequestBody UI_028130_Form form) {
        try {
            Rmout defRMOUT = new Rmout();
            //Modified by ChenLi on 2014/12/01 for 個資log紀錄查詢
            // 記錄AuditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("查詢");
            // 塞入auditLog.params
            auditLog.addParam("調整標準", form.getUiItem());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            defRMOUT.setRmoutTxdate(form.getUiItem().substring(0, 8)); //交易日期
            defRMOUT.setRmoutBrno(form.getUiItem().substring(8, 11)); //登錄分行別
            defRMOUT.setRmoutOriginal(form.getUiItem().substring(11, 12)); //交易來源
            //defRMOUT.RMOUT_BATCHNO = UIITEM.Text.Substring(12, 3) '批號
            defRMOUT.setRmoutFepno(form.getUiItem().substring(12, 19)); //FEP匯款登錄序號
            //Modify by Jim, 2011/05/04, 如果是單筆匯款，需要查詢出資料show在畫面
            defRMOUT = rmService.getRMOUTbyPK(defRMOUT);
            if (defRMOUT != null) {
                RM rmBusiness = new RM();
                form.getRmout().setRmoutTxdate(defRMOUT.getRmoutTxdate().substring(0, 4) + "/" + defRMOUT.getRmoutTxdate().substring(4, 6) + "/" + defRMOUT.getRmoutTxdate().substring(6));
                form.getRmout().setRmoutBrno(defRMOUT.getRmoutBrno());
                form.getRmout().setRmoutOriginal(rmBusiness.mapRMOUTOriginal(defRMOUT.getRmoutOriginal()));
                form.getRmout().setRmoutFepno(defRMOUT.getRmoutFepno());
                form.getRmout().setRmoutTxamt(defRMOUT.getRmoutTxamt());
                form.getRmout().setRmoutRemtype(rmBusiness.mapRMOUTRemtype(defRMOUT.getRmoutRemtype()));
                form.getRmout().setRmoutStat(rmBusiness.mapRMOUTStat(defRMOUT.getRmoutStat()));
                form.getRmout().setRmoutSenderBank(defRMOUT.getRmoutSenderBank());
                form.getRmout().setRmoutReceiverBank(defRMOUT.getRmoutReceiverBank());
                form.getRmout().setRmoutOutName(defRMOUT.getRmoutOutName());
                form.getRmout().setRmoutInName(defRMOUT.getRmoutInName());
            } else {
                form.setMessage(MessageType.DANGER, QueryNoData);
            }
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER, DealFail);
            logContext.setRemark("UI_028130查詢單筆RMOUT發生例外");
            logContext.setProgramException(ex);
            rmService.logMessage(logContext, Level.INFO);
        }
        return form;
    }

    @PostMapping(value = "/rm/UI_028130/executeClick")
    @ResponseBody
    public UI_028130_Form executeClick(@RequestBody UI_028130_Form form) throws Exception {
        accessDataBase(form);
        return form;
    }

    /**
     * 依查詢條件查詢的主程式。
     */
    private void accessDataBase(UI_028130_Form form) {
        int rmoutResult = 0;
        int rmouttResult = 0;
        try {
            form.setMessage(MessageType.INFO, "");
            if (checkAllField(form)) {

                switch (form.getKind()) {
                    case "1": {
                        Rmout defRMOUT = new Rmout();
                        defRMOUT.setRmoutTxdate(form.getUiItem().substring(0, 8)); //交易日期
                        defRMOUT.setRmoutBrno(form.getUiItem().substring(8, 11)); //登錄分行別
                        defRMOUT.setRmoutOriginal(form.getUiItem().substring(11, 12)); //交易來源
                        //defRMOUT.RMOUT_BATCHNO = UIITEM.Text.Substring(12, 3) '批號
                        defRMOUT.setRmoutFepno(form.getUiItem().substring(12, 19)); //FEP匯款登錄序號
                        defRMOUT = rmService.getRMOUTbyPK(defRMOUT);
                        //Modify by Jim, 2011/05/04, 如果是單筆匯款，需要查詢出資料show在畫面
                        if (defRMOUT != null) {
                            RM rmBusiness = new RM();
                            form.getRmout().setRmoutTxdate(defRMOUT.getRmoutTxdate().substring(0, 4) + "/" + defRMOUT.getRmoutTxdate().substring(4, 6) + "/" + defRMOUT.getRmoutTxdate().substring(6));
                            form.getRmout().setRmoutBrno(defRMOUT.getRmoutBrno());
                            form.getRmout().setRmoutOriginal(rmBusiness.mapRMOUTOriginal(defRMOUT.getRmoutOriginal()));
                            form.getRmout().setRmoutFepno(defRMOUT.getRmoutFepno());
                            form.getRmout().setRmoutTxamt(defRMOUT.getRmoutTxamt());
                            form.getRmout().setRmoutRemtype(rmBusiness.mapRMOUTRemtype(defRMOUT.getRmoutRemtype()));
                            form.getRmout().setRmoutStat(rmBusiness.mapRMOUTStat(defRMOUT.getRmoutStat()));
                            form.getRmout().setRmoutSenderBank(defRMOUT.getRmoutSenderBank());
                            form.getRmout().setRmoutReceiverBank(defRMOUT.getRmoutReceiverBank());
                            form.getRmout().setRmoutOutName(defRMOUT.getRmoutOutName());
                            form.getRmout().setRmoutInName(defRMOUT.getRmoutInName());

                            defRMOUT.setRmoutOwpriority(form.getOwpriority()); //匯出優先順序
                            rmoutResult = rmService.updateRMOUTbyPK(defRMOUT);
                            if (rmoutResult < 1) {
                                prepareAndSendEMSData("匯出主檔(RMOUT)更新 0 筆", form.getKind(), form.getUiItem(), form.getOwpriority());
                            }
                            Rmoutt defRMOUTT = new Rmoutt();
                            defRMOUTT.setRmouttTxdate(form.getUiItem().substring(0, 8)); //交易日期
                            defRMOUTT.setRmouttBrno(form.getUiItem().substring(8, 11)); //登錄分行別
                            defRMOUTT.setRmouttOriginal(form.getUiItem().substring(11, 12)); //交易來源
                            //defRMOUTT.RMOUTT_BATCHNO = UIITEM.Text.Substring(12, 3) '批號
                            defRMOUTT.setRmouttFepno(form.getUiItem().substring(12, 19)); //FEP匯款登錄序號
                            defRMOUTT.setRmouttOwpriority(form.getOwpriority()); //匯出優先順序

                            rmouttResult = rmService.updateRMOUTTbyPK(defRMOUTT);
                            //2011/8/12, RMOUTT 更新不到的不要視為ERROR
                            //If rmouttResult < 1 Then
                            //    PrepareAndSendEMSData("匯出主檔(RMOUTT)更新失敗")
                            //End If

                            if (rmoutResult > 0) {
                                prepareAndSendEMSData("調整成功", form.getKind(), form.getUiItem(), form.getOwpriority());
                            }
                        } else {
                            //PrepareAndSendEMSData("匯出主檔(RMOUT)無此資料")
                        }
                        break;
                    }
                    case "2":
                    case "3":
                    case "4":
                    case "5": {
                        List<Rmout> rmoutDt = new ArrayList();
                        List<Rmoutt> rmouttDt = new ArrayList();
                        //Begin Modify By Candy 2013-07-30
                        Rmout defRMOUT = new Rmout();
                        Rmoutt defRMOUTT = new Rmoutt();

                        //GetData(rmoutDt, rmouttDt)
                        RefBase<List<Rmout>> refBase = new RefBase<>(rmoutDt);
                        RefBase<List<Rmoutt>> refBase1 = new RefBase<>(rmouttDt);
                        RefBase<Rmout> rmoutRefBase = new RefBase<>(defRMOUT);
                        RefBase<Rmoutt> rmouttRefBase = new RefBase<>(defRMOUTT);
                        getData(refBase, refBase1, rmoutRefBase, rmouttRefBase, form);
                        rmoutDt = refBase.get();
                        rmouttDt = refBase1.get();
                        defRMOUT = rmoutRefBase.get();
                        defRMOUTT = rmouttRefBase.get();
                        //End Modify By Candy 2013-07-30
                        if (rmoutDt.size() < 1) {
                            prepareAndSendEMSData("匯出主檔(RMOUT)無此資料", form.getKind(), form.getUiItem(), form.getOwpriority());
                        } else {
                            //Begin 2013-07-30 Modify by Candy
                            //rmoutResult = obj.UpdateRMOUTbyDataSet(rmoutDt)
                            rmoutResult = rmService.updateRMOUTByDef(form.getOwpriority(), defRMOUT);
                            //End 2013-07-30 Modify by Candy
                            if (rmoutResult < 1) {
                                prepareAndSendEMSData("匯出主檔(RMOUT)更新 0 筆", form.getKind(), form.getUiItem(), form.getOwpriority());
                            }
                        }
                        if (rmouttDt.size() < 1) {
                            prepareAndSendEMSData("匯出暫存檔(RMOUTT)無此資料", form.getKind(), form.getUiItem(), form.getOwpriority());
                        } else {
                            //Begin 2013-07-30 Modify by Candy
                            // rmouttResult = obj.UpdateRMOUTTbyDataSet(rmouttDt)
                            rmouttResult = rmService.updateRMOUTTByDef(form.getOwpriority(), defRMOUTT);
                            //End 2013-07-30 Modify by Candy
                            //2011/8/12, RMOUTT 更新不到的不要視為ERROR
                            //If rmouttResult < 1 Then
                            //    PrepareAndSendEMSData("匯出主檔(RMOUTT)更新失敗")
                            //End If
                        }

                        if (rmoutResult > 0) {
                            prepareAndSendEMSData("調整成功", form.getKind(), form.getUiItem(), form.getOwpriority());
                        }
                        break;
                    }
                }
                form.setMessage(MessageType.INFO, "更新匯出主檔" + rmoutResult + "筆; 更新匯出暫存檔" + rmouttResult + "筆");
            }

        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER, ex.toString());
            logContext.setRemark("UI_028130_AccessDataBase發生例外");
            logContext.setProgramException(ex);
            rmService.logMessage(logContext, Level.INFO);
        }
    }

    /**
     * 檢核查詢條件輸入
     *
     * @return
     */
    public boolean checkAllField(UI_028130_Form form) {
        boolean rtn = true;
        String kindItem = "";
        switch (form.getKind()) {
            case "1":
                kindItem = "單筆匯款";
                break;
            case "2":
                kindItem = "特定銀行或特定分行";
                break;
            case "3":
                kindItem = "特定金額(大於等於)";
                break;
            case "4":
                kindItem = "特定帳號";
                break;
            case "5":
                kindItem = "系統暫停";
                break;
            default:
                break;
        }
        switch (Integer.parseInt(form.getKind())) {
            case 1:
                if (form.getUiItem().length() != 19) {
                    form.setMessage(MessageType.DANGER, kindItem + " 長度應為 19");
                    rtn = false;
                }
                break;
            case 2:
                if (form.getUiItem().length() != 3 && form.getUiItem().length() != 7) {
                    form.setMessage(MessageType.DANGER, kindItem + " 長度應為 3 or 7");
                    rtn = false;
                }
                break;
            case 3:
                if (form.getUiItem().length() > 11 || form.getUiItem().length() == 0) {
                    form.setMessage(MessageType.DANGER, kindItem + " 長度應小於等於 11");
                    rtn = false;
                }
                break;
            case 4:
                if (form.getUiItem().length() != 14) {
                    form.setMessage(MessageType.DANGER, kindItem + " 長度應為 14");
                    rtn = false;
                }
                break;
        }

        return rtn;

    }

    private void getData(RefBase<List<Rmout>> rmoutDt, RefBase<List<Rmoutt>> rmouttDt, RefBase<Rmout> defRMOUT, RefBase<Rmoutt> defRMOUTT, UI_028130_Form form) throws Exception {

        //Dim defRMOUT As New Tables.DefRMOUT
        //Dim defRMOUTT As New Tables.DefRMOUTT
        switch (form.getKind()) {
            case "2": //特定銀行或特定分行
                defRMOUT.get().setRmoutTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                defRMOUT.get().setRmoutReceiverBank(form.getUiItem());
                defRMOUTT.get().setRmouttTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                defRMOUTT.get().setRmouttReceiverBank(form.getUiItem());
                rmoutDt.set(rmService.getRMOUTbyReceiverBANK(form.getUiItem()));
                rmouttDt.set(rmService.getRMOUTTbyReceiverBANK(form.getUiItem()));
                break;
            case "3": //特定金額(大於等於)
                defRMOUT.get().setRmoutTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                defRMOUT.get().setRmoutTxamt(new BigDecimal(form.getUiItem()));
                defRMOUTT.get().setRmouttTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                defRMOUTT.get().setRmouttTxamt(new BigDecimal(form.getUiItem()));
                rmoutDt.set(rmService.getRMOUTbyTXAMT(form.getUiItem()));
                rmouttDt.set(rmService.getRMOUTTbyTXAMT(form.getUiItem()));
                break;
            case "4": //特定帳號
                //defRMOUT.RMOUT_TXDATE = SysStatus.PropertyValue.SYSSTAT_TBSDY_FISC()
                defRMOUT.get().setRmoutTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                defRMOUT.get().setRmoutInAccIdNo(form.getUiItem());
                defRMOUTT.get().setRmouttTxdate(defRMOUT.get().getRmoutTxdate());
                defRMOUTT.get().setRmouttInAccIdNo(defRMOUT.get().getRmoutInAccIdNo());
                rmoutDt.set(rmService.getRmoutByDef(defRMOUT.get()));
                defRMOUTT.get().setRmouttFepno(null);
                rmouttDt.set(rmService.getRMOUTTbyDef(defRMOUTT.get()));
                //Fly 2019/05/09 For 跨行餘額內控
                break;
            case "5": //系統暫停
                defRMOUT.get().setRmoutTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                defRMOUT.get().setRmoutOwpriority("8");
                defRMOUTT.get().setRmouttTxdate(defRMOUT.get().getRmoutTxdate());
                defRMOUTT.get().setRmouttOwpriority("8");
                rmoutDt.set(rmService.getRmoutByDef(defRMOUT.get()));
                defRMOUTT.get().setRmouttFepno(null);
                rmouttDt.set(rmService.getRMOUTTbyDef(defRMOUTT.get()));
                break;
        }

        for (Rmout dr : rmoutDt.get()) {
            dr.setRmoutOwpriority(form.getOwpriority());
        }

        for (Rmoutt dr : rmouttDt.get()) {
            dr.setRmouttOwpriority(form.getOwpriority());
        }
    }


    private void prepareAndSendEMSData(String strMsg, String kind, String uiItem, String owpriority) throws Exception {
        String kindItem = "";
        String owpriorityItem = "";
        switch (kind) {
            case "1":
                kindItem = "單筆匯款";
                break;
            case "2":
                kindItem = "特定銀行或特定分行";
                break;
            case "3":
                kindItem = "特定金額(大於等於)";
                break;
            case "4":
                kindItem = "特定帳號";
                break;
            case "5":
                kindItem = "系統暫停";
                break;
            default:
                break;
        }
        switch (owpriority) {
            case "0":
                owpriorityItem = "Normal";
                break;
            case "1":
                owpriorityItem = "LOW";
                break;
            case "8":
                owpriorityItem = "System Suspend";
                break;
            case "9":
                owpriorityItem = "Suspend";
                break;
            case "*":
                owpriorityItem = "High";
                break;
            default:
                break;
        }
        LogData logContext = new LogData();

        logContext.setChannel(FEPChannel.FEP);
        logContext.setSubSys(SubSystem.RM);
        logContext.setProgramName("UI_028130");
        logContext.setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        logContext.setDesBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        logContext.setMessageId("UI028130");
        logContext.setMessageGroup("4"); // /*RM*/
        logContext.setRemark(StringUtils.join(kindItem, "-調整標準[", uiItem, "]-調整優先順序[", owpriorityItem, "], ", strMsg));
        logContext.setTxUser(WebUtil.getUser().getUserId());
        logContext.setReturnCode(RMReturnCode.ChangePriority);
        TxHelper.getRCFromErrorCode(logContext.getReturnCode(), FEPChannel.FEP, logContext);
        //FEPBase.SendEMS(logContext)
    }

}