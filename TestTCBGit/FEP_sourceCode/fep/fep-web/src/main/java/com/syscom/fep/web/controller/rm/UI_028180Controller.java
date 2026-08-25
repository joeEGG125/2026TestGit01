package com.syscom.fep.web.controller.rm;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.RMHandler;
import com.syscom.fep.vo.constant.RMOUTStatus;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.vo.text.rm.RMGeneral;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SelectOption;
import com.syscom.fep.web.form.rm.UI_028180_Form;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.util.*;

/**
 * 匯款確認取消交易輸入(OP)
 *
 * @author xingyun_yang
 * @create 2021/11/30
 */
@Controller
public class UI_028180Controller extends BaseController {
    @Autowired
    RmService rmService;
    @Autowired
    AtmService atmService;

    private final String _normal = "0000";
    private final String fepCancle = "12453";

    @Override
    public void pageOnLoad(ModelMap mode) {
        try {
            this.bindConstant(mode);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        UI_028180_Form form = new UI_028180_Form();
        mode.addAttribute("execute", "false");
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    /**
     * 為頁面綁定一些常量
     *
     * @param mode ModelMap
     */
    private void bindConstant(ModelMap mode) throws Exception {
        // 初始化brno下拉選單
        List<SelectOption<String>> selectOptionList = new ArrayList<>();
        List<Bctl> bctlList = atmService.getAllBctlBrno();
        for (Bctl bctl : bctlList) {
            selectOptionList.add(new SelectOption<>(bctl.getBctlBrno() + "-" + bctl.getBctlAlias(), bctl.getBctlBrno()));
        }
        WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList);
    }

    @PostMapping(value = "/rm/UI_028180/queryClick")
    public String queryClick(@ModelAttribute UI_028180_Form form, ModelMap mode) throws Exception {
        this.infoMessage("執行UI_028180, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        queryRMOUT(form, mode);
        this.bindConstant(mode);
        return Router.UI_028180.getView();
    }

    @PostMapping(value = "/rm/UI_028180/executeClick")
    public String executeClick(@ModelAttribute UI_028180_Form form, ModelMap mode) throws Exception {
        Rmout _defRmout = new Rmout();
        this.infoMessage("執行UI_028180, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        RMGeneral rmGeneral = new RMGeneral();
        RMHandler handler = new RMHandler();
        try {
            _defRmout.setRmoutTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            _defRmout.setRmoutBrno(form.getBrno());
            _defRmout.setRmoutOriginal(form.getOriginal());
            _defRmout.setRmoutFepno(StringUtils.leftPad(form.getFepNo(), 7, '0'));
            //_defRMOUT.RMOUT_BATCHNO = "000"
            List<Rmout> dtResult = rmService.getRmoutByDef(_defRmout);
            if (dtResult.size() < 1) {
                this.showMessage(mode, MessageType.DANGER, "匯出主檔" + QueryNoData);
                return "";
            }

            //modify by Jim, 2011/05/04, if original = 5, call C1700

//            Boolean result = false;
            Boolean result = false;
            String aaName = "";

            //modified by maxine on 2011/08/02 for 補送EMS

            String strMsg = "PK(RMOUT_TXDATE=" + _defRmout.getRmoutTxdate() +
                    ";RMOUT_BRNO=" + _defRmout.getRmoutBrno() +
                    ";RMOUT_ORIGINAL=" + _defRmout.getRmoutOriginal() +
                    ";RMOUT_FEPNO=" + _defRmout.getRmoutFepno() + ")";
            String strResult = "";

            //modified by Maxine on 2011/07/20 for 增加original 1-FCS/2-FEDI/4-MMAB2B
            switch (form.getOriginal()) {
                case "0":
                    aaName = "C1100";
                    rmGeneral = makeC1100RequestMessage(dtResult, rmGeneral, _defRmout);
                    result = handler.dispatch(FEPChannel.FEP, rmGeneral, aaName);
                    if (result) {
                        strMsg = DealSuccess + strMsg;
                        this.showMessage(mode, MessageType.INFO, DealSuccess);
                    } else {
                        strMsg = rmGeneral.getResponse().getRsStatDesc() + strMsg;
                        this.showMessage(mode, MessageType.DANGER, "呼叫" + aaName + "錯誤");
                    }
                    break;
                case "1":
                    strResult = updateRMOUTAndRMOUTTAndRMBTCH(dtResult, form, mode);
                    if (strResult.equals(DealSuccess)) {
                        strMsg = DealSuccess + strMsg;
                        this.showMessage(mode, MessageType.INFO, DealSuccess);
                    } else {
                        strMsg = strResult + strMsg;
                    }
                    break;
                case "2":
                    strResult = updateRMOUTAndRMOUTT(form, mode);
                    if (strResult.equals(DealSuccess)) {
                        strMsg = DealSuccess + strMsg;
                        this.showMessage(mode, MessageType.INFO, DealSuccess);
                    } else {
                        strMsg = strResult + strMsg;
                    }
                    break;
                case "4":
                    strResult = updateRMOUTAndRMOUTT(form, mode);
                    if (strResult.equals(DealSuccess)) {
                        this.showMessage(mode, MessageType.INFO, DealSuccess);
                    } else {
                        strMsg = strResult + strMsg;
                    }
                    break;
                case "5":
                    aaName = "C1700";
                    rmGeneral = makeC1700RequestMessage(dtResult, rmGeneral, _defRmout);
                    result = handler.dispatch(FEPChannel.FEP, rmGeneral, aaName);
                    if (result) {
                        this.showMessage(mode, MessageType.INFO, DealSuccess);
                    } else {
                        strMsg = rmGeneral.getResponse().getRsStatDesc() + strMsg;
                        this.showMessage(mode, MessageType.DANGER, "呼叫" + aaName + "錯誤");
                    }
                    break;
                default:
                    break;
            }
            prepareAndSendEMSData(mode, strMsg);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        this.bindConstant(mode);
        return Router.UI_028180.getView();
    }

    /**
     * 輸入登錄分行, 登錄序號及金額後, 自動查詢相關匯出主檔資料
     */
    protected void queryRMOUT(UI_028180_Form form, ModelMap mode) throws Exception {
        Rmout _defRmout = new Rmout();
        //Modified by ChenLi on 2014/12/01 for 個資log紀錄查詢
        // 記錄AuditLog
        AuditLog auditLog = new AuditLog();
        // 塞入auditLog.action
        auditLog.setAction("查詢");
        // 塞入auditLog.params
        _defRmout.setRmoutTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        auditLog.addParam("登錄分行", form.getBrno());
        _defRmout.setRmoutBrno(form.getBrno());
        auditLog.addParam("來源別", form.getOriginal());
        _defRmout.setRmoutOriginal(form.getOriginal());
        if ("1".equals(_defRmout.getRmoutOriginal())) {
            auditLog.addParam("批號", form.getBatchNo());
            _defRmout.setRmoutBatchno(form.getBatchNo());
        }
        auditLog.addParam("FEP/T24登錄序號", form.getFepNo());
        _defRmout.setRmoutFepno(StringUtils.leftPad(form.getFepNo(), 7, '0'));
        auditLog.addParam("匯款金額", form.getTxAmt());
        // 最後塞入ThreadLocal變量中
        setAuditLog(auditLog);
        _defRmout = rmService.getSingleRMOUT(_defRmout);
        BigDecimal txAmt = new BigDecimal(0);
        if (StringUtils.isNotBlank(form.getTxAmt())) {
            txAmt = new BigDecimal(form.getTxAmt());
        }
        if (_defRmout == null) {
            this.showMessage(mode, MessageType.DANGER, "匯出主檔" + QueryNoData);
            WebUtil.putInAttribute(mode, AttributeName.Form, form);
            return;
        } else if (_defRmout.getRmoutTxamt().compareTo(txAmt) != 0) {
            this.showMessage(mode, MessageType.DANGER, "匯出主檔金額內容不符");
            WebUtil.putInAttribute(mode, AttributeName.Form, form);
            return;
        } else if (!_defRmout.getRmoutStat().equals(RMOUTStatus.Passed) &&
                !_defRmout.getRmoutStat().equals(RMOUTStatus.FISCReject) &&
                !_defRmout.getRmoutStat().equals(RMOUTStatus.SystemProblem)) {
            //放行/財金拒絶，才可確認取消
            this.showMessage(mode, MessageType.DANGER, "匯出主檔金額內容不符");
            WebUtil.putInAttribute(mode, AttributeName.Form, form);
            return;
        } else {
            Allbank defAllbank = new Allbank();
            mode.addAttribute("RMOUT_SENDER_BANK", _defRmout.getRmoutSenderBank());
            defAllbank.setAllbankBkno(_defRmout.getRmoutSenderBank().substring(0, 3));
            defAllbank.setAllbankBrno(_defRmout.getRmoutSenderBank().substring(3, 6));
            List<Allbank> allbankList = rmService.getALLBANKDataTableByPK(defAllbank);
            if (allbankList.size() > 0) {
                mode.addAttribute("RMOUT_SENDER_BANK", allbankList.get(0).getAllbankAliasname());
            }
            mode.addAttribute("RMOUT_AMT_TYPE", _defRmout.getRmoutAmtType());
            mode.addAttribute("RMOUT_REMTYPE", _defRmout.getRmoutRemtype());
            mode.addAttribute("RMOUT_SERVAMT_TYPE", _defRmout.getRmoutServamtType());
            mode.addAttribute("RMOUT_RECFEE", _defRmout.getRmoutRecfee());
            mode.addAttribute("RMOUT_ACTFEE", _defRmout.getRmoutActfee());
            mode.addAttribute("RMOUT_CIF", _defRmout.getRmoutCif());
            mode.addAttribute("RMOUT_RECEIVER_BANK", _defRmout.getRmoutReceiverBank());
            mode.addAttribute("RMOUT_IN_ACC_ID_NO", _defRmout.getRmoutInAccIdNo());
            mode.addAttribute("RMOUT_IN_NAME", _defRmout.getRmoutInName());
            mode.addAttribute("RMOUT_REMCIF", _defRmout.getRmoutRemcif());
            mode.addAttribute("RMOUT_OUT_NAME", _defRmout.getRmoutOutName());
            mode.addAttribute("RMOUT_REMTEL", _defRmout.getRmoutRemtel());
            mode.addAttribute("RMOUT_MEMO", _defRmout.getRmoutMemo());
            mode.addAttribute("RMOUT_TAXNO", _defRmout.getRmoutTaxno());
            mode.addAttribute("execute", "true");
        }
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    private RMGeneral makeC1100RequestMessage(List<Rmout> dtResult, RMGeneral rmGeneral, Rmout _defRmout) {
        rmGeneral.getRequest().setKINBR(dtResult.get(0).getRmoutBrno());
        rmGeneral.getRequest().setTRMSEQ("0");
        rmGeneral.getRequest().setFEPNO(dtResult.get(0).getRmoutFepno());
        rmGeneral.getRequest().setENTTLRNO(StringUtils.leftPad("", 5, '9'));
        rmGeneral.getRequest().setSUPNO1(WebUtil.getUser().getUserId());
        rmGeneral.getRequest().setSUPNO2("0");
        rmGeneral.getRequest().setTBSDY("0");
        rmGeneral.getRequest().setTBSDY(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        rmGeneral.getRequest().setTIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
        rmGeneral.getRequest().setREMDATE(rmGeneral.getRequest().getTBSDY());
        rmGeneral.getRequest().setORGBRNO(_defRmout.getRmoutBrno());
        rmGeneral.getRequest().setRECCIF(dtResult.get(0).getRmoutInAccIdNo());
        rmGeneral.getRequest().setREMAMT(dtResult.get(0).getRmoutTxamt());
        rmGeneral.getRequest().setREMBANK(dtResult.get(0).getRmoutSenderBank());
        rmGeneral.getRequest().setRECBANK(dtResult.get(0).getRmoutReceiverBank());
        rmGeneral.getRequest().setREMNM(dtResult.get(0).getRmoutOutName());
        rmGeneral.getRequest().setRECNM(dtResult.get(0).getRmoutInName());
        return rmGeneral;
    }

    private RMGeneral makeC1700RequestMessage(List<Rmout> dtResult, RMGeneral rmGeneral, Rmout _defRmout) {
        rmGeneral.getRequest().setKINBR(dtResult.get(0).getRmoutBrno());
        rmGeneral.getRequest().setTRMSEQ("0");
        rmGeneral.getRequest().setFEPNO(dtResult.get(0).getRmoutFepno());
        rmGeneral.getRequest().setENTTLRNO(StringUtils.leftPad("", 5, '9'));
        rmGeneral.getRequest().setSUPNO1(WebUtil.getUser().getUserId());
        rmGeneral.getRequest().setSUPNO2("0");
        rmGeneral.getRequest().setTBSDY("0");
        rmGeneral.getRequest().setTBSDY(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        rmGeneral.getRequest().setTIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
        rmGeneral.getRequest().setREMDATE(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        rmGeneral.getRequest().setORGBRNO(_defRmout.getRmoutBrno());
        rmGeneral.getRequest().setRECCIF(dtResult.get(0).getRmoutInAccIdNo());
        rmGeneral.getRequest().setREMAMT(dtResult.get(0).getRmoutTxamt());
        rmGeneral.getRequest().setREMBANK(dtResult.get(0).getRmoutSenderBank());
        rmGeneral.getRequest().setRECBANK(dtResult.get(0).getRmoutReceiverBank());
        rmGeneral.getRequest().setREMNM(dtResult.get(0).getRmoutOutName());
        rmGeneral.getRequest().setRECNM(dtResult.get(0).getRmoutInName());
        return rmGeneral;
    }

    //'add by Maxine on 2011/07/20 for 增加original 1-FCS/2-FEDI/4-MMAB2B
    private String updateRMOUTAndRMOUTTAndRMBTCH(List<Rmout> dtResult, UI_028180_Form form, ModelMap mode) {
        Rmout defRmout = new Rmout();
        Rmoutt defRmoutt = new Rmoutt();
        Rmbtch defRmbtch = new Rmbtch();
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            defRmout.setRmoutTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            defRmout.setRmoutBrno(form.getBrno());
            defRmout.setRmoutOriginal(form.getOriginal());
            defRmout.setRmoutFepno(StringUtils.leftPad(form.getFepNo(), 7, '0'));
            //11-磁片整批匯出失敗
            defRmout.setRmoutStat(RMOUTStatus.DiskBatchRMOutFail);
            defRmout.setRmoutSupno1(WebUtil.getUser().getUserId());
            defRmout.setRmoutApdate("");
            defRmout.setRmoutAptime("");

            if (rmService.updateRMOUTbyPK(defRmout) < 1) {
                this.showMessage(mode, MessageType.DANGER, UpdateFail + "(RMOUT)");
                transactionManager.rollback(txStatus);
                return UpdateFail + "(RMOUT)";
            }
            defRmoutt.setRmouttTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            defRmoutt.setRmouttBrno(form.getBrno());
            defRmoutt.setRmouttOriginal(form.getOriginal());
            defRmoutt.setRmouttFepno(StringUtils.leftPad(form.getFepNo(), 7, '0'));
            defRmoutt.setRmouttStat(RMOUTStatus.DiskBatchRMOutFail);
            defRmoutt.setRmouttSupno1(WebUtil.getUser().getUserId());
            defRmoutt.setRmouttApdate("");
            defRmoutt.setRmouttAptime("");

            if (rmService.updateRMOUTTByDefSelective(defRmoutt) < 1) {
                this.showMessage(mode, MessageType.DANGER, UpdateFail + "(RMOUTT)");
                transactionManager.rollback(txStatus);
                return UpdateFail + "(RMOUTT)";
            }
            defRmbtch.setRmbtchSenderBank(dtResult.get(0).getRmoutSenderBank());
            defRmbtch.setRmbtchRemdate(dtResult.get(0).getRmoutTxdate());
            defRmbtch.setRmbtchTimes(dtResult.get(0).getRmoutBatchno());
            defRmbtch.setRmbtchFepno(dtResult.get(0).getRmoutFepno());
            defRmbtch.setRmbtchFepRc(TxHelper.getRCFromErrorCode(fepCancle, FEPChannel.FEP, FEPChannel.RM));
            defRmbtch.setRmbtchErrmsg(TxHelper.getMessageFromFEPReturnCode(fepCancle, FEPChannel.FEP));
            if (rmService.updateRMBTCHByDef(defRmbtch) < 1) {
                this.showMessage(mode, MessageType.DANGER, UpdateFail + "(RMBTCH)");
                transactionManager.rollback(txStatus);
                return UpdateFail + "(RMBTCH)";
            }
            transactionManager.commit(txStatus);
        } catch (Exception ex) {
            if (!txStatus.isCompleted()) {
                transactionManager.rollback(txStatus);
                this.showMessage(mode, MessageType.DANGER, ex.getMessage());
                return ex.getMessage();
            }
        }
        return DealSuccess;
    }


    private String updateRMOUTAndRMOUTT(UI_028180_Form form, ModelMap mode) {
        Rmout defRmout = new Rmout();
        Rmoutt defRmoutt = new Rmoutt();
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            defRmout.setRmoutTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            defRmout.setRmoutBrno(form.getBrno());
            defRmout.setRmoutOriginal(form.getOriginal());
            defRmout.setRmoutFepno(StringUtils.leftPad(form.getFepNo(), 7, '0'));
            //財金拒絕
            defRmout.setRmoutStat(RMOUTStatus.FISCReject);
            defRmout.setRmoutSupno1(WebUtil.getUser().getUserId());
            defRmout.setRmoutApdate("");
            defRmout.setRmoutAptime("");

            if (rmService.updateRMOUTbyPK(defRmout) < 1) {
                this.showMessage(mode, MessageType.DANGER, UpdateFail + "(RMOUT)");
                transactionManager.rollback(txStatus);
                return UpdateFail + "(RMOUT)";
            }
            defRmoutt.setRmouttTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            defRmoutt.setRmouttBrno(form.getBrno());
            defRmoutt.setRmouttOriginal(form.getOriginal());
            defRmoutt.setRmouttFepno(StringUtils.leftPad(form.getFepNo(), 7, '0'));
            //財金拒絕
            defRmout.setRmoutStat(RMOUTStatus.FISCReject);
            defRmoutt.setRmouttSupno1(WebUtil.getUser().getUserId());
            defRmoutt.setRmouttApdate("");
            defRmoutt.setRmouttAptime("");

            if (rmService.updateRMOUTTByDefSelective(defRmoutt) < 1) {
                this.showMessage(mode, MessageType.DANGER, UpdateFail + "(RMOUTT)");
                transactionManager.rollback(txStatus);
                return UpdateFail + "(RMOUTT)";
            }
            transactionManager.commit(txStatus);
        } catch (Exception ex) {
            if (!txStatus.isCompleted()) {
                transactionManager.rollback(txStatus);
                this.showMessage(mode, MessageType.DANGER, ex.getMessage());
                return ex.getMessage();
            }
        }
        return DealSuccess;
    }


    /**
     * 執行成功送成功信息
     */
    private void prepareAndSendEMSData(ModelMap mode, String strMsg) throws Exception {

        try {
            List<Sysstat> _dtSYSSTAT = rmService.getStatus();
            if (_dtSYSSTAT.size() < 1) {
                this.showMessage(mode, MessageType.DANGER, "SYSSTAT無資料!!");
                return;
            }
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, ex.getMessage());
            return;
        }
        logContext.setChannel(FEPChannel.FEP);
        logContext.setSubSys(SubSystem.RM);
        logContext.setProgramName("UI_028180");
        logContext.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        logContext.setDesBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        logContext.setMessageId("UI028180");
        /*Rm*/
        logContext.setMessageGroup("4");
        logContext.setMessageParm13("匯款確認取消交易輸入:" + strMsg);
        logContext.setTxUser(WebUtil.getUser().getUserId());
        logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(RMReturnCode.SendRMOUTCancel, logContext));
        rmService.logMessage(logContext, Level.INFO);
    }
}
