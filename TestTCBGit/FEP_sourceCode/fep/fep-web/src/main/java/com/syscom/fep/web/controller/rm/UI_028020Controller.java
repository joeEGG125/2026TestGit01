package com.syscom.fep.web.controller.rm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import com.syscom.fep.server.common.handler.FEPHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.mapper.MsgoutExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmoutExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.model.Msgout;
import com.syscom.fep.mybatis.model.Rmout;
import com.syscom.fep.server.common.handler.FISCHandler;
import com.syscom.fep.vo.constant.MSGOUTStatus;
import com.syscom.fep.vo.constant.RMOUTStatus;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_RM;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.rm.UI_028020_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 對財金匯出交易狀況查詢
 *
 * @author Chen_yu
 * @create 2021/9/28
 */
@Controller
public class UI_028020Controller extends BaseController {

    @Autowired
    RmService rmService;
    @Autowired
    RmoutExtMapper rmoutExtMapper;
    @Autowired
    MsgoutExtMapper msgoutExtMapper;


    FISCGeneral FISCData = new FISCGeneral();
    Msgout defMsgout = new Msgout();

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_028020_Form form = new UI_028020_Form();
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/rm/UI_028020/queryClick")
    @ResponseBody
    public BaseResp<UI_028020_Form> queryClick(@RequestBody UI_028020_Form form) {
        this.infoMessage("查詢明細數據, 條件 = [", form, "]");
        BaseResp<UI_028020_Form> response = new BaseResp<>();

//        @SuppressWarnings("unused")
//        RmService obj = new RmService();
//        boolean isSendToAA = true;
        //Jim, 2012/2/21, add log
        getLogContext().setRemark("AA1511查詢, 查詢方式=" + form.getInqFlag() + ", SNO=" + form.getSno() + ", 查詢類別=" + form.getOrgKind());
        logMessage(Level.INFO, logContext);
        Integer resCount = getData(form, response, true);

        if (resCount == 1) {
//            if (isSendToAA) {
                sendToAA(form, response);
//            } else {
//                response.setMessage(MessageType.INFO, "中斷中最後一筆尚未Timeout, 請稍候再查詢");
//            }
        } else if (resCount > 1) {
            response.setMessage(MessageType.DANGER, "查到大於一筆資料,請檢查資料正確性");
        } else if (resCount == 0) {
            if (!"0".equals(form.getInqFlag())) {
                response.setMessage(MessageType.INFO, QueryNoData);
            } else {
                response.setMessage(MessageType.INFO, "目前無中斷中筆數, 請稍候再查詢");
            }
        }
        response.setData(form);
        return response;
    }

    //"資料庫相關事件"
    //讀取訊息匯出檔
    private Integer getMsgout(UI_028020_Form form, Msgout defMsgout, BaseResp<UI_028020_Form> response, RefBase<List<Msgout>> drs) throws Exception {
        PageInfo<Msgout> dtResult = new PageInfo<Msgout>();
//        Integer rtn = 0;
        int rtn = 0;
        defMsgout.setMsgoutTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        switch (form.getInqFlag()) {
            case "0":
                drs.set(msgoutExtMapper.getmsgoutByMaxFiscsno(defMsgout.getMsgoutTxdate(), defMsgout.getMsgoutFiscsno()));
                if(drs.get().size() > 0){
//                    defMsgout = drs.get().get(0);
                    rtn = 1;
                }
                break;
            case "1":
                defMsgout.setMsgoutStan(StringUtils.leftPad(form.getSno().toString(), 7, "0"));
                dtResult = rmService.getMsgOutByDef(defMsgout,form.getPageNum(),form.getPageSize());
                break;
            case "2":
                defMsgout.setMsgoutFiscsno(StringUtils.leftPad(form.getSno().toString(), 7, "0"));
                dtResult = rmService.getMsgOutByDef(defMsgout,form.getPageNum(),form.getPageSize());
                break;
            case "3":
                defMsgout.setMsgoutEjno(Integer.parseInt(form.getSno()));
                dtResult = rmService.getMsgOutByDef(defMsgout,form.getPageNum(),form.getPageSize());
                break;
        }
        if (dtResult.getList() != null) {
            if (dtResult.getList().size() > 1) {
                drs.set(dtResult.getList().stream().filter(item -> !item.getMsgoutStat().equals(MSGOUTStatus.FISCRefuse)).collect(Collectors.toList()));
                rtn = drs.get().size();
                if (rtn > 1) {
                    getLogContext().setRemark(StringUtils.join("狀態非財金拒絕的RMOUT筆數>1筆, 請查明原因, INQFLAG.SelectedValue=" , form.getInqFlag() , ", 序號=" , form.getSno()));
                    logMessage(Level.INFO, getLogContext());
                }
            }else{
                drs.set(dtResult.getList());
                rtn = dtResult.getList().size();
            }
        }
        return rtn;
    }
    //讀取匯出主檔
    private List<Rmout> getRmout (UI_028020_Form form, BaseResp < UI_028020_Form > response, List<Rmout> drs) throws Exception {
        Rmout defRmout = new Rmout();
        //Dim dtResult As DataTable = Nothing
        List<Rmout> dtResult = null;
        Integer rtn = 0;
        defRmout.setRmoutTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        switch (form.getInqFlag()) {
            case "0":
                defRmout.setRmoutStat(RMOUTStatus.Transfered);
                drs= rmService.getRmoutByDef(defRmout);
                drs.size();
                //Jim, 2012/2/21, 如果>1筆也要擋不能做
                if (drs.size() > 1) {
                    form.setSno(drs.get(0).getRmoutFiscsno());
                    defRmout.setRmoutFiscsno(form.getSno());
                    dtResult = rmService.getRmoutByDef(defRmout);
                    //LogContext.Remark = "狀態非財金拒絕的RMOUT筆數>1筆, 請查明原因, INQFLAG.SelectedValue=" & INQFLAG.SelectedValue & ", 序號=" & drs(0).Item("RMOUT_FISCSNO").ToString
                    //rmSrv.LogMessage(LogContext, LogLevel.Info)
                }
                //rtn = rmSrv.GetRMOUTByMaxFISCSNO(defRMOUT)
                break;
            case "1":
                defRmout.setRmoutStat(StringUtils.leftPad(form.getSno().toString(), 7, "0"));
                dtResult = rmService.getRmoutByDef(defRmout);
                break;
            case "2":
                defRmout.setRmoutFiscsno(StringUtils.leftPad(form.getSno().toString(), 7, "0"));
                dtResult = rmService.getRmoutByDef(defRmout);
                break;
            case "3":
                defRmout.setRmoutEjno1(Integer.parseInt(form.getSno()));
                dtResult = rmService.getRmoutByDef(defRmout);
                break;
        }
        if (dtResult != null) {
            if (dtResult.size() > 1) {
                drs = dtResult.stream().filter(item -> !item.getRmoutStat().equals(RMOUTStatus.FISCReject)).collect(Collectors.toList());
                rtn = drs.size();
                if (rtn > 1) {
                    getLogContext().setRemark(StringUtils.join("狀態非財金拒絕的RMOUT筆數>1筆, 請查明原因, INQFLAG.SelectedValue=" , form.getInqFlag() , ", 序號=" , form.getSno()));
                    logMessage(getLogContext());
                }

            } else {
                drs = dtResult;
            }
        }
        return drs;
    }
    //讀取資料主流程
    private Integer getData (UI_028020_Form form, BaseResp < UI_028020_Form > response,boolean isSendToAA){
        Integer rtn = 0;
        try {
            switch (form.getOrgKind()) {
                case "1":
                    List<Rmout> drs = new ArrayList<>();
                    drs = getRmout(form, response, drs);
                    rtn = drs.size();
                    if (rtn == 1) {
                        FISCData.setRMRequest(new FISC_RM());
                        if ("0".equals(form.getInqFlag())) {
                            if ("".equals(form.getSno())) {
                                form.setSno("");
                            }else {
                                form.setSno(drs.get(0).getRmoutFiscsno().toString()); //可使user淸楚以那一電文序號至財金查詢狀態
                            }
                            FISCData.getRMRequest().setFiscNo(drs.get(0).getRmoutFiscsno().toString());
                            FISCData.getRMRequest().setOrgPcode(drs.get(0).getRmoutFiscSndCode().toString());
                            FeptxnExt defFeptxn = new FeptxnExt();
                            defFeptxn.setFeptxnTxDate(drs.get(0).getRmoutTxdate().toString());
                            defFeptxn.setFeptxnStan(drs.get(0).getRmoutStan().toString());
                            defFeptxn.setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno());
                            if (rmService.getFeptxnByStan(defFeptxn) != null) {
                                if (DbHelper.toBoolean(defFeptxn.getFeptxnFiscTimeout())) {
                                    return 1;
                                } 
//                                else {
//                                    isSendToAA = false;
//                                }
                            }
                        } else {
                            FISCData.getRMRequest().setFiscNo(drs.get(0).getRmoutFiscsno().toString());
                            FISCData.getRMRequest().setOrgPcode(drs.get(0).getRmoutFiscSndCode().toString());
                            return 1;
                        }
                    }
                    break;
                case "4":
                    Msgout defMsgout = new Msgout();
                    List<Msgout> msgoutList = new ArrayList<>();
                    RefBase<List<Msgout>> drsMSG = new RefBase<List<Msgout>>(msgoutList);
                    rtn = getMsgout(form, defMsgout, response, drsMSG);
                    msgoutList = drsMSG.get();
                    if (rtn == 1) {
                        FISCData.setRMRequest(new FISC_RM());
                        if ("0".equals(form.getInqFlag())) {
                            form.setSno(msgoutList.get(0).getMsgoutFiscsno()); //可使user淸楚以那一電文序號至財金查詢狀態
                            FISCData.getRMRequest().setFiscNo(msgoutList.get(0).getMsgoutFiscsno());
                            FISCData.getRMRequest().setOrgPcode(msgoutList.get(0).getMsgoutFiscSndCode());
                        } else {
                            FISCData.getRMRequest().setFiscNo(msgoutList.get(0).getMsgoutFiscsno().toString());
                            FISCData.getRMRequest().setOrgPcode(msgoutList.get(0).getMsgoutFiscSndCode().toString());
                        }
                        return 1;
                    }
                    break;
            }
            return rtn;
        } catch (Exception ex) {
            String ExceptionPolicyName = "Business Policy";
            response.setMessage(MessageType.DANGER, ExceptionPolicyName);
        }
        return rtn;
    }
    //"與AA相關處理"
    private void sendToAA (UI_028020_Form form, BaseResp < UI_028020_Form > response){
        FEPHandler fepHandler = new FEPHandler();
        try {
            FISCData.getRMRequest().setProcessingCode("1511");
            FISCData.getRMRequest().setMessageKind(MessageFlow.Request);
            FISCData.setSubSystem(FISCSubSystem.RM);
            FISCData.getRMRequest().setMessageType("0200");
            fepHandler.dispatch(FEPChannel.FEP, FISCData);
            if ("處理成功".equals(FISCData.getDescription().trim())) {
                response.setMessage(MessageType.INFO, FISCData.getDescription());
            } else {
                response.setMessage(MessageType.DANGER, FISCData.getDescription());
            }
            //組ReturnCode
            form.setRc(FISCData.getRMResponse().getResponseCode());
            switch (FISCData.getRMResponse().getSTATUS()) {
                case "00":
                case "01":
                case "02":
                case "03":
                case "04":
                    form.setStatus(FISCData.getRMResponse().getSTATUS());
                    break;
                default:
                    form.setStatus("00");
                    break;
            }
        } catch (Exception ex) {
            response.setMessage(MessageType.DANGER, ex.getMessage());
        }
    }
}