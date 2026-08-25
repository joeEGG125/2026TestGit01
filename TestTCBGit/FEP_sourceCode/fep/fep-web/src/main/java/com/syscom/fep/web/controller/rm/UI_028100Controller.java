package com.syscom.fep.web.controller.rm;

import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.rm.RM;
import com.syscom.fep.vo.constant.*;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.SessionKey;
import com.syscom.fep.web.form.rm.UI_028100_Form;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UI_028100Controller extends BaseController {

    @Autowired
    RmService rmService;

    @Autowired
    FeptxnDao _dbFEPTXN;

    // private static Rmin _defRMIN = new Rmin();
    // private Feptxn _defFEPTXN;

    //add by maxine on 2011/06/27 for SYSSTAT自行查
    // private List<Sysstat> _dtSYSSTAT;
    // private String _SYSSTAT_FBKNO;

    @PostMapping(value = "/rm/UI_028100/pageLoad")
    @ResponseBody
    public UI_028100_Form pageLoad() {
        //add by maxine on 2011/06/24 for SYSSTAT自行查
        UI_028100_Form form = new UI_028100_Form();
        querySYSSTAT(form);
        form.setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        return form;
    }

    private void querySYSSTAT(UI_028100_Form form) {
        try {
            List<Sysstat> _dtSYSSTAT = rmService.getStatus();
            if (_dtSYSSTAT.size() < 1) {
                form.setMessage(MessageType.DANGER, "SYSSTAT無資料!!");
                return;
            }
            SessionData sessionData = new SessionData();
            WebUtil.putInSession(SessionKey.TemporaryRestoreData, sessionData);
            sessionData._SYSSTAT_FBKNO = _dtSYSSTAT.get(0).getSysstatFbkno();
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER, ex.toString());
        }
    }

    @PostMapping(value = "/rm/UI_028100/queryClick")
    @ResponseBody
    protected UI_028100_Form queryClick(@RequestBody UI_028100_Form form) {
        Rmstat defRMSTAT = new Rmstat();
        try {
            getRMINData(form);
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER, ex.getMessage());
        }
        return form;
    }

    @PostMapping(value = "/rm/UI_028100/executeClick")
    @ResponseBody
    public UI_028100_Form executeClick() throws Exception {
        UI_028100_Form form = new UI_028100_Form();
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        //modified by maxine on 2011/08/02 for 補送EMS
        String strMsg = "調整成功";

        SessionData sessionData = this.getSessionData();
        Rmin _defRMIN = sessionData._defRMIN;
        String _SYSSTAT_FBKNO = sessionData._SYSSTAT_FBKNO;
        try {
            //判斷是否可補送中心
            if (!RMPending.Pending.equals(_defRMIN.getRminPending())) {
                form.setMessage(MessageType.WARNING, "該筆匯入已入帳成功, 不能再補送");
                strMsg = "該筆匯入已入帳成功, 不能再補送";
                prepareAndSendEMSData(strMsg);
                return form;
            }
            if (!RMINStatus.Transferring.equals(_defRMIN.getRminStat())) {
                form.setMessage(MessageType.DANGER, "匯入主檔狀態不符");
                strMsg = "匯入主檔狀態不符";
                prepareAndSendEMSData(strMsg);
                return form;
            }

            //'更新交易記錄(FEPTXN) Candy modify  -> Query FEPTXN
            FeptxnExt defFEPTXN = new FeptxnExt();
            _dbFEPTXN.setTableNameSuffix(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(6, 8), "executeClick");
            // _defFEPTXN = new FeptxnExt();
            defFEPTXN.setFeptxnTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            //modified By Maxine on 2011/06/24 for SYSSTAT 自行查
            defFEPTXN.setFeptxnBkno(_SYSSTAT_FBKNO);
            //defFEPTXN.FEPTXN_BKNO = SysStatus.PropertyValue.SYSSTAT_FBKNO

            defFEPTXN.setFeptxnStan(_defRMIN.getRminStan());
            Feptxn _defFEPTXN = _dbFEPTXN.getFeptxnByStan(defFEPTXN.getFeptxnTxDate(), defFEPTXN.getFeptxnBkno(), defFEPTXN.getFeptxnStan());
            if (_defFEPTXN == null) {
                form.setMessage(MessageType.DANGER, "FEP交易明細檔" + QueryFail);
                strMsg = "FEP交易明細檔" + QueryFail;
                prepareAndSendEMSData(strMsg);
                return form;
            }

            if (updateRMINAndRMINT(form) != CommonReturnCode.Normal) {
                form.setMessage(MessageType.DANGER, "匯入主檔" + UpdateFail);
                strMsg = "匯入主檔" + UpdateFail;
                prepareAndSendEMSData(strMsg);
                return form;
            }

            //TODO:{Jim} type?
            RM rmBusiness = new RM();
            RMData txRMData = new RMData();

            //Begin Candy modifiy
            LogData logContext = new LogData();
            logContext.setSubSys(SubSystem.RM);
            logContext.setChannel(FEPChannel.FEP);
            logContext.setMessage("");
            logContext.setProgramFlowType(ProgramFlow.AAIn);
            logContext.setMessage("");
            logContext.setMessageFlowType(MessageFlow.Request);
            logContext.setProgramName("UI_028100");
            logContext.setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            logContext.setChannel(FEPChannel.FEP);
            logContext.setRemark("UI_028100 SendToCBS");
            txRMData.setEj(TxHelper.generateEj());
            txRMData.setTxChannel(FEPChannel.FEP);
            txRMData.setMessageFlowType(MessageFlow.Request);
            txRMData.setLogContext(logContext);
            //Endn Candy modifiy
            rmBusiness.setLogContext(logContext);
            rmBusiness.setmRMData(txRMData);
            rmBusiness.setFeptxn(defFEPTXN);
            rmBusiness.setFeptxnDao(_dbFEPTXN);
            //rmBusiness.
            defFEPTXN.setFeptxnMsgflow(FEPTxnMessageFlow.CBS_Request);
            rmBusiness.setFeptxn(rmService.getFeptxnByStan(rmBusiness.getFeptxn()));
            if (rmBusiness.getFeptxn() == null) {
                form.setMessage(MessageType.DANGER, "FEP交易明細檔" + QueryNoData);
                strMsg = "FEP交易明細檔" + QueryNoData;
                prepareAndSendEMSData(strMsg);
                return form;
            }

            rtnCode = rmBusiness.sendToCBS("R1900", (byte) 1, _defRMIN, null, null);

            //Modify by Jim, 2011/05/11, 如果是發生例外的話，要回主機連線錯誤訊息
            if (rtnCode == CommonReturnCode.HostResponseTimeout) {
                form.setMessage(MessageType.DANGER, "該筆匯入帳失敗, 主機Timeout, 請查明原因" + UpdateFail);
                strMsg = "該筆匯入帳失敗, 主機Timeout, 請查明原因" + UpdateFail;
                prepareAndSendEMSData(strMsg);
                return form;
            } else if (rtnCode == CommonReturnCode.ProgramException) {
                form.setMessage(MessageType.DANGER, "主機連線發生錯誤");
                strMsg = "主機連線發生錯誤";
                prepareAndSendEMSData(strMsg);
                return form;
            } else if (rtnCode != CommonReturnCode.Normal) {
                form.setMessage(MessageType.DANGER, "解款失敗-" + TxHelper.getMessageFromFEPReturnCode(CommonReturnCode.CBSResponseError));
                strMsg = "解款失敗-" + TxHelper.getMessageFromFEPReturnCode(CommonReturnCode.CBSResponseError);
                prepareAndSendEMSData(strMsg);
                return form;
            }

            if (_defRMIN.getRminReceiverBank().length() >= 6) {
                rtnCode = rmBusiness.processRMTOTAndRMTOTAL("SV11X2", _defRMIN.getRminReceiverBank().substring(3, 6), REMTXTP.SeriesOfTransfer, _defRMIN.getRminTxdate(), _defRMIN.getRminTxamt());
            } else {
                form.setMessage(MessageType.DANGER, "RMTOT/RMTOTAL 更新失敗, 解款行銀行代號長度不符");
                strMsg = "RMTOT/RMTOTAL 更新失敗, 解款行銀行代號長度不符";
                prepareAndSendEMSData(strMsg);
                return form;
            }

            if (rtnCode != CommonReturnCode.Normal) {
                //SEND EMS
                form.setMessage(MessageType.DANGER, "RMTOT/RMTOTAL 更新失敗" + UpdateFail);
                strMsg = "RMTOT/RMTOTAL 更新失敗";
                prepareAndSendEMSData(strMsg);
                return form;
            }
            form.setMessage(MessageType.SUCCESS, "交易成功");
            strMsg = "交易成功";
            prepareAndSendEMSData(strMsg);
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER, ex.getMessage());
            strMsg = ex.getMessage();
            prepareAndSendEMSData(strMsg);
            form.setMessage(MessageType.DANGER, ex.toString());
        }
        return form;
    }

    private void getRMINData(UI_028100_Form form) throws Exception {

        //Fly 2014/11/20 For個資LOG紀錄
        // 記錄AuditLog
        AuditLog auditLog = new AuditLog();
        // 塞入auditLog.action
        auditLog.setAction("查詢");
        // 塞入auditLog.params
        Rmin _defRMIN = this.getSessionData()._defRMIN;
        _defRMIN.setRminTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))); //一定是當日，只有當日才可以補送
        auditLog.addParam("交易日期", _defRMIN.getRminTxdate());
        _defRMIN.setRminFiscsno(StringUtils.leftPad(form.getFiscSno(), 7, '0'));
        auditLog.addParam("電文序號", _defRMIN.getRminFiscsno());
        _defRMIN.setRminSenderBank(form.getSenderBank());
        auditLog.addParam("發信行", _defRMIN.getRminSenderBank());
        _defRMIN.setRminRmsno(StringUtils.leftPad(form.getRmSno(), 7, '0'));
        auditLog.addParam("通匯序號", _defRMIN.getRminRmsno());

        _defRMIN.setRminFiscRtnCode(RMIN_FISC_RTN_CODE.Normal); //財金回應正常
        _defRMIN.setRminFiscSndCode("1172"); //退匯，查詢是用於<>條件
        _defRMIN.setRminEjno2(0); //為自動入帳時送T24電文之EJNO，查詢是用於<>條件
        if (StringUtils.isNotBlank(form.getStan()) && Integer.parseInt(form.getStan()) != 0) {
            _defRMIN.setRminStan(form.getStan());
            auditLog.addParam("STAN", _defRMIN.getRminStan());
        }
        if (StringUtils.isNotBlank(form.getFepNo()) && Integer.parseInt(form.getFepNo()) != 0) {
            _defRMIN.setRminFepno(StringUtils.leftPad(form.getFepNo(), 7, '0'));
            auditLog.addParam("FEP登錄序號", _defRMIN.getRminFepno());
        }
        if (StringUtils.isNotBlank(form.getEjfno()) && Integer.parseInt(form.getEjfno()) != 0) {
            _defRMIN.setRminEjno1(Integer.parseInt(form.getEjfno())); //EJNO1: 財金匯入時EJNO
            auditLog.addParam("EJFNO", _defRMIN.getRminEjno1());
        }
        // 最後塞入ThreadLocal變量中
        setAuditLog(auditLog);
        RefBase<Rmin> rminRefBase = new RefBase<>(_defRMIN);
        int iRes = rmService.getRMINForResend(rminRefBase);
        _defRMIN = rminRefBase.get();
        if (iRes < 1) {
            form.setMessage(MessageType.DANGER, "匯入主檔" + QueryNoData);
        } else if (iRes > 1) {
            form.setMessage(MessageType.DANGER, "匯入主檔有多筆資料, 請查明後再處理");
        } else {
            form.setoTxDate(_defRMIN.getRminTxdate());
            form.setoFepNo(_defRMIN.getRminFepno());
            form.setoEjno(_defRMIN.getRminEjno1().toString());
            form.setActno(_defRMIN.getRminInAccIdNo());
            form.setAmt(_defRMIN.getRminTxamt().toString());
            form.setoRmSno(_defRMIN.getRminRmsno());
            form.setoFiscSno(_defRMIN.getRminFiscsno());
            form.setoSenderBank(_defRMIN.getRminSenderBank());
            form.setReceiverBank(_defRMIN.getRminReceiverBank());
            form.setInName(_defRMIN.getRminInName());
            form.setOutName(_defRMIN.getRminOutName());
            form.setMemo(_defRMIN.getRminMemo());
        }
    }

    private FEPReturnCode updateRMINAndRMINT(UI_028100_Form form) throws Exception {
        Rmin _defRMIN = this.getSessionData()._defRMIN;
        if (_defRMIN.getRminEjno2() == null || _defRMIN.getRminEjno2() == 0) {
            Rmint defRMINT = new Rmint();
            _defRMIN.setRminEjno2(TxHelper.generateEj());
            defRMINT.setRmintTxdate(_defRMIN.getRminTxdate());
            defRMINT.setRmintBrno(_defRMIN.getRminBrno());
            defRMINT.setRmintFepno(_defRMIN.getRminFepno());
            defRMINT.setRmintEjno2(_defRMIN.getRminEjno2());
            if (rmService.updateRMINAndRMINTByPK(_defRMIN, defRMINT) != 1) {
                form.setMessage(MessageType.DANGER, "匯入主檔或匯入暫存檔" + UpdateFail);
                return IOReturnCode.RMINUpdateError;
            }
        }
        return CommonReturnCode.Normal;
    }

    private void prepareAndSendEMSData(String strMsg) throws Exception {
        Rmin _defRMIN = this.getSessionData()._defRMIN;
        LogData logContext = new LogData();
        logContext.setChannel(FEPChannel.FEP);
        logContext.setSubSys(SubSystem.RM);
        logContext.setProgramName("UI_028100");
        logContext.setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        logContext.setDesBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        logContext.setMessageId("UI028100");
        logContext.setMessageGroup("4"); // /*RM*/
        logContext.setRemark(StringUtils.join("FEP匯入序號=", _defRMIN.getRminFepno(), strMsg));
        logContext.setTxUser(WebUtil.getUser().getUserId());
        logContext.setReturnCode(RMReturnCode.SendRMINTX);
        TxHelper.getRCFromErrorCode(logContext.getReturnCode(), FEPChannel.FEP, logContext);
    }

    private class SessionData {
        Rmin _defRMIN = new Rmin();
        String _SYSSTAT_FBKNO;
    }

    private SessionData getSessionData() {
        return WebUtil.getFromSession(SessionKey.TemporaryRestoreData);
    }
}