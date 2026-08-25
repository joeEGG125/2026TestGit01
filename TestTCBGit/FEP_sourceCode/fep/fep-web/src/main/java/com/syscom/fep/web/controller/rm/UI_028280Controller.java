package com.syscom.fep.web.controller.rm;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.model.Rmin;
import com.syscom.fep.mybatis.model.Rmint;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.AMLAdapter;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028280_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.InbkService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * 電文重送AML人工作業
 *
 * @author xingyun_yang
 * @create 2021/11/30
 */
@Controller
public class UI_028280Controller extends BaseController {
    @Autowired
    RmService rmService;
    @Autowired
    InbkService inbkService;
    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_028280_Form form = new UI_028280_Form();
        form.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    /**
     * 查詢按鈕
     */
    @PostMapping(value = "/rm/UI_028280/queryClick")
    public String queryClick(@ModelAttribute UI_028280_Form form, ModelMap mode) {
        this.infoMessage("UI_028280查詢, 條件 = [", form, "]");
        this.doKeepFormData(mode, form);
        bindGridData(form, mode);
        return Router.UI_028280.getView();
    }

    /**
     * 按下執行按鈕
     */
    @PostMapping(value = "/rm/UI_028280/executeClick")
    @ResponseBody
    public BaseResp<?> executeClick(@RequestBody List<UI_028280_Form> formList) {
        this.infoMessage("執行UI_028280, 條件 = [", formList.toString(), "]");
        BaseResp<?> response = new BaseResp<>();
        Boolean msg = true;
        for (int i = 0; i < formList.size(); i++) {
            Rmin defRmin = new Rmin();
            defRmin.setRminTxdate(formList.get(i).getRmInTxDate());
            defRmin.setRminBrno(formList.get(i).getRmInBrno());
            defRmin.setRminFepno(formList.get(i).getRmInFepNo());
            PlatformTransactionManager dbH = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
            TransactionStatus db = dbH.getTransaction(new DefaultTransactionDefinition());

            try {
                defRmin = rmService.queryRminByPrimaryKeyWithUpdLock(defRmin);
                if (!"98".equals(defRmin.getRminStat())) {
                    //可能剛好被UI送出了
                    continue;
                }
                AMLAdapter amlAdapter = new AMLAdapter();
                AMLAdapter.AMLRequest amlReq = new AMLAdapter.AMLRequest();
                AMLAdapter.AMLResponse amlRes = new AMLAdapter.AMLResponse();
                amlReq = prepareKYCMessage(defRmin, amlReq);
                amlAdapter.setRequest(amlReq);
                if (!sendTOAML(amlAdapter, defRmin, dbH, response).equals(FEPReturnCode.Normal)) {
                    msg = false;
                    dbH.rollback(db);
                } else {
                    dbH.commit(db);
                }
            } catch (Exception ex) {
                msg = false;
                logMessage(ex.getMessage());
                dbH.rollback(db);
                response.setMessage(MessageType.DANGER, programError);
                return response;
            }
        }
        if (msg) {
            response.setMessage(MessageType.INFO, "執行成功");
        }
        return response;
    }

    public AMLAdapter.AMLRequest prepareKYCMessage(Rmin def, AMLAdapter.AMLRequest req) {
        List<AMLAdapter.AMLRequest.Person> list = new ArrayList<>();
        req.setUserName("FEP");
        req.setsCode(RMConfig.getInstance().getAMLPASS());
        // (交易審查) : 非本行客戶，針對單次交易掃描
        req.setType("TWTS");
        req.setPersons(list);
        AMLAdapter.AMLRequest.Person perData = new AMLAdapter.AMLRequest.Person();
        //Fly 2019/06/24 For AML 需先宣告物件
        perData.setCustomFields(new Hashtable<>());
        perData.setPersonID(StringUtils.join(def.getRminTxdate(), def.getRminFepno()));
        if (def.getRminOutName().length() <= 32) {
            perData.setLastName(def.getRminOutName());
        }else {
            perData.setLastName(def.getRminOutName().substring(0, 32));
            perData.getCustomFields().put("KYC_SP_10", def.getRminOutName().substring(32));
        }
        perData.setZipCode(StringUtils.join("BR0" + ATMPConfig.getInstance().getProcessCenter()));
        req.getPersons().add(perData);
        return req;
    }

    public FEPReturnCode sendTOAML(AMLAdapter amlAdapter, Rmin def, PlatformTransactionManager dbH, BaseResp<?> response) {
        FEPReturnCode rtn = FEPReturnCode.Abnormal;

        for (int i = 0; i <= 2; i++) {
            try {
                rtn = amlAdapter.sendReceive();
                prepareAndSendEMSData(def);
                if (rtn.equals(FEPReturnCode.Normal)) {
                    break;
                }
                if (!rtn.equals(FEPReturnCode.Normal)) {
                    return rtn;
                }
            } catch (Exception ex) {
                logMessage(ex.toString());
                response.setMessage(MessageType.DANGER, programError);
                return rtn;
            }
            try {
                if (amlAdapter.getResponse().getReturnCode() == 0 && amlAdapter.getResponse().getResultData()[0] != null &&
                        amlAdapter.getResponse().getResultData()[0].getCustNo().length() >= 15 &&
                        amlAdapter.getResponse().getResultData()[0].getCustNo().substring(0, 8).equals(def.getRminTxdate()) &&
                        amlAdapter.getResponse().getResultData()[0].getCustNo().substring(8, 15).equals(def.getRminFepno())) {
                    if (amlAdapter.getResponse().getResultData()[0].getKycActions()[0].getShortText().trim().equals("HIT")) {
                        //疑似HIT AML
                        def.setRminStat("08");
                        //HIT
                        def.setRminAmlstat("1");
                        //Fly 2019/10/23 增加update時需判斷STAT
                        if (rmService.updateRminByPrimaryKeyWithStat(def, "98") <= 0) {
                            logMessage(String.format("更新RMIN FEPNO={0}失敗，該筆狀態<>98", def.getRminFepno()));
                        }

                        Rmint defRmint = new Rmint();
                        defRmint.setRmintTxdate(def.getRminTxdate());
                        defRmint.setRmintBrno(def.getRminBrno());
                        defRmint.setRmintFepno(def.getRminFepno());
                        //疑似HIT AML
                        defRmint.setRmintStat("08");
                        //HIT
                        defRmint.setRmintAmlstat("1");
                        //Fly 2019/10/23 增加update時需判斷STAT
                        if (rmService.updateRmintByPrimaryKeyWithStat(defRmint, "98") <= 0) {
                            logMessage(String.format("更新RMINT FEPNO={0}失敗，該筆狀態<>98", defRmint.getRmintFepno()));
                        }
                    } else {
                        //傳送中(CBS)
                        def.setRminStat("99");
                        // NO HIT
                        def.setRminAmlstat("0");
                        //Fly 2019/10/23 增加update時需判斷STAT
                        if (rmService.updateRminByPrimaryKeyWithStat(def, "98") <= 0) {
                            logMessage(String.format("更新RMIN FEPNO={0}失敗，該筆狀態<>98", def.getRminFepno()));
                        }

                        Rmint defRmint = new Rmint();
                        defRmint.setRmintTxdate(def.getRminTxdate());
                        defRmint.setRmintBrno(def.getRminBrno());
                        defRmint.setRmintFepno(def.getRminFepno());
                        //傳送中(CBS)
                        defRmint.setRmintStat("99");
                        //NO HIT
                        defRmint.setRmintAmlstat("0");
                        //Fly 2019/10/23 增加update時需判斷STAT
                        if (rmService.updateRmintByPrimaryKeyWithStat(defRmint, "98") <= 0) {
                            logMessage(String.format("更新RMINT FEPNO={0}失敗，該筆狀態<>98", defRmint.getRmintFepno()));
                        }
                    }
                }
            } catch (Exception e) {
            	this.errorMessage(e, e.getMessage());
            }
        }
        return rtn;
    }


    /**
     * PrepareAndSendEMSData
     */
    private void prepareAndSendEMSData(Rmin def) {
        LogData logData = new LogData();
        logData.setDesBkno("807");
        logData.setMessageGroup("4");
        logData.setSubSys(SubSystem.RM);
        logData.setChannel(FEPChannel.FEP);
        logData.setProgramName("UI_028280");
        logData.setMessageParm13(String.format("匯款日期:{0} 登錄分行:{1} FEP登錄序號:{2}", def.getRminTxdate(), def.getRminBrno(), def.getRminFepno()));
        logData.setReturnCode(RMReturnCode.BypassAML);
        //由GetMessageFromFEPReturnCode明細 SendEMS
        TxHelper.getMessageFromFEPReturnCode(logData.getReturnCode(), logData);
    }

    /**
     * 資料整理
     */
    private PageInfo<HashMap<String, Object>> getGridData(String txDateS, int pageNum, int pageSize, ModelMap mode) {
        PageInfo<HashMap<String, Object>> dt = null;
        try {
            String txDate_s = StringUtils.replace(txDateS, "-", StringUtils.EMPTY);
            dt = rmService.getAMLReSendData(txDate_s, pageNum, pageSize);
        } catch (Exception ex) {
            logMessage(ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return dt;
    }

    private void bindGridData(UI_028280_Form form, ModelMap mode) {
        try {
            PageInfo<HashMap<String, Object>> dtResult = getGridData(form.getTxDate(), form.getPageNum(), form.getPageSize(), mode);
            if (dtResult.getList() == null || dtResult.getList().size() == 0) {
                this.showMessage(mode, MessageType.WARNING, "匯入主檔(RMIN)無符合資料");
            } else {
                this.clearMessage(mode);
            }
            PageData<UI_028280_Form, HashMap<String, Object>> pageData = new PageData<>(dtResult, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, programError);
        }

    }

//    private void prepareKYCMessage(Rmin def,Adap)


    protected String getSNDCode(String msg) {
        if (msg.length() < 4) {
            return msg;
        }
        switch (msg.substring(2, 4)) {
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

    private void logMessage(String msg) {
        logContext.setRemark(msg);
        inbkService.inbkLogMessage(Level.INFO, logContext);
    }
}
