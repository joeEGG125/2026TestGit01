package com.syscom.fep.server.controller.restful;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.notify.NotifyHelper;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.model.GraylistRequestExt;
import com.syscom.fep.mybatis.ext.model.GraylistResponseExt;
import com.syscom.fep.mybatis.model.Graylist;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.controller.BaseController;
import com.syscom.fep.server.helper.GraylistHelper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//@RestController
@StackTracePointCut(caller = SvrConst.SVR_GrayList)
public class GRAYLISTQryController extends BaseController {
    @Autowired
    private GraylistHelper graylistHelper;

    @Override
    public String getName() {
        return SvrConst.SVR_GrayList;
    }

    @Value("${gray.list.apikey:apiKey}")
    private String apikey;
    private String[] keys;
    private final NotifyHelper notifyHelper = SpringBeanFactoryUtil.getBean(NotifyHelper.class);  //2024-05-09 新增Notify
    private final LogHelper log = new LogHelper();
    private LogData logData = new LogData();

    @PostConstruct
    public void init() {
        keys = (apikey != null) ? apikey.split(",") : new String[0];
    }

    @RequestMapping(value = "/WebUtils/GRAYLISTQuery", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> GRAYLISTPost(@RequestBody GraylistRequestExt request) {
        LogMDC.put(Const.MDC_PROFILE, this.getName());
        logData = new LogData();
        String bankNo = request.getI_ALLBANK_BKNO();
        String actNo = request.getI_TROUT_ACTNO();
        String atmNo = request.getI_ATM_ATMNO();
        String txDateTime = request.getI_TxDateTime();
        String systemID = request.getI_SystemID();
        String secretKey = request.getI_SecK();
        String txType = request.getI_TxType();

        int logEj = request.getI_LogEj();
        if (logEj == 0) {
            logEj = TxHelper.generateEj();
            logData.setEj(logEj);
        } else {
            logData.setEj(logEj);
        }
        logData.setRemark("request:" + request.toString());
        this.logMessage(logData);

        GraylistResponseExt graylist;
        // 1. 比對灰名單
        try {
            graylist = compareGrayList(bankNo, actNo, atmNo, txDateTime, systemID, secretKey, txType);
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".compareGrayList"));
            logData.setRemark("compareGrayList 異常.");
            this.logMessage(logData);
            throw ExceptionUtil.createRuntimeException(e);
        }

        // 2. 發送簡訊
        if (graylist != null) {
            if ("A".equals(graylist.getQryrc())) {
                try {
                    logData.setRemark("比對 Graylist 有結果，發送簡訊通知");
                    this.logMessage(logData);
                    Map<String, String> parameter = new HashMap<>();
//                    parameter.put("TX_BKNO", bankNo);
//                    parameter.put("TX_ACTNO5", actNo.substring(actNo.length() - 5));
//                    parameter.put("TXDATETIME", txDateTime);
//                    parameter.put("ATM_ATMAD", graylist.getAtmAddressC());
//                    parameter.put("TXTYPE", txType);
//                    parameter.put("ATM_ATMNO", atmNo);
//                    parameter.put("phone1", graylist.getPhone1());
//                    parameter.put("phone2", graylist.getPhone2());
                    // 2025-07-08 Richard modified for [Reflected XSS All Clients]
                    ReflectUtil.envokeMethod(parameter, "put", new Class[] {Object.class, Object.class}, new Object[] {"TX_ACTNO5", actNo.substring(actNo.length() - 5)});
                    ReflectUtil.envokeMethod(parameter, "put", new Class[] {Object.class, Object.class}, new Object[] {"TXNDATETIME", txDateTime});
                    ReflectUtil.envokeMethod(parameter, "put", new Class[] {Object.class, Object.class}, new Object[] {"ATM_ATMAD", graylist.getAtmAddressC()});
                    ReflectUtil.envokeMethod(parameter, "put", new Class[] {Object.class, Object.class}, new Object[] {"TXTYPE", txType});
                    ReflectUtil.envokeMethod(parameter, "put", new Class[] {Object.class, Object.class}, new Object[] {"ATM_ATMNO", atmNo});
                    notifyHelper.sendSimpleSMS("M0A", graylist.getMp1(), parameter, true, logEj);
                    if (StringUtils.isNotBlank(graylist.getMp2()) && !graylist.getMp2().equals(graylist.getMp1())) {
                        notifyHelper.sendSimpleSMS("M0A", graylist.getMp2(), parameter, true, logEj);
                    }
                } catch (Exception e) {
                    logData.setProgramException(e);
                    logData.setProgramName(StringUtils.join(ProgramName, ".SendSMS"));
                    logData.setRemark("sendSimpleSMS 異常.");
                    this.logMessage(logData);
                    throw ExceptionUtil.createRuntimeException(e);
                }
            }
            logData.setRemark("比對結果: " + graylist.toString());
            this.logMessage(logData);
        } else {
            logData.setRemark("比對 Graylist 無結果，不發送簡訊通知");
            this.logMessage(logData);
        }

        // 3. 回傳結果
        return ResponseEntity.ok("OK");
    }

    private GraylistResponseExt compareGrayList(String bankNo, String actNo, String atmNo, String txDateTtime, String systemID, String secretKey, String txtype) throws Exception {
        GraylistResponseExt graylist;
        // 1. 驗證輸入
        try {
            validateSecretKey(secretKey);
            validateSystemID(systemID);
            logData.setRemark("驗證成功");
            this.logMessage(logData);
        } catch (ValidationException e) {
            return createErrorResponseList(bankNo + actNo, txDateTtime, e.getErrorCode(), e.getMessage());
        }

        // 2. 獲取資料
        try {
            graylist = getGraylistData(bankNo, actNo, txDateTtime);
            String atmAddress = graylistHelper.getATMAddressCByAtmno(atmNo);
            if (StringUtils.isBlank(atmAddress)) {
                atmAddress = "比對不到ATM地址。";
            }
            graylist.setAtmNo(atmNo);
            graylist.setAtmAddressC(atmAddress);
            graylist.setTxType(txtype);
        } catch (Exception e) {
            log.error(e.getMessage());
            logData.setRemark("獲取資料異常");
            this.logMessage(logData);
            throw e;
        }
        return graylist;
    }

    private void validateSystemID(String systemID) throws ValidationException {
        if (!"ATM".equals(systemID)) {
            throw new ValidationException("V", "系統代號錯誤: " + systemID);
        }
    }

    private void validateSecretKey(String secretKey) throws ValidationException {
        if (Arrays.stream(keys).noneMatch(key -> key.equals(secretKey))) {
            throw new ValidationException("V", "金鑰錯誤");
        }
    }

    private GraylistResponseExt getGraylistData(String bankNo, String actNo, String txDateTtime) throws Exception {
        GraylistResponseExt graylistExt = new GraylistResponseExt();
        Graylist graylist = graylistHelper.getGraylistByAct(bankNo, actNo + "%", txDateTtime.substring(0, 8));
        if (graylist == null) {
//            throw new ValidationException("F", "比對結果:查無資料");
            graylistExt.setQryrc("F");
            logData.setRemark("查無資料");
        } else {
            graylistExt.setMp1(graylist.getPhone1());
            graylistExt.setMp2(graylist.getPhone2());
            graylistExt.setQryrc("A");
            logData.setRemark("比對命中");
        }
        this.logMessage(logData);
        graylistExt.setBankNo(bankNo);
        graylistExt.setAccountNo(actNo);
        graylistExt.setTxDateTime(txDateTtime);

        return graylistExt;
    }

    private GraylistResponseExt createErrorResponseList(String accountNo, String txDateTtime, String errorCode, String message) {
        GraylistResponseExt errorModel = new GraylistResponseExt();
        errorModel.setAccountNo(accountNo);
        errorModel.setTxDateTime(txDateTtime);
        errorModel.setQryrc(errorCode);
        log.error(message);
        logData.setRemark(message);
        this.logMessage(logData);
        return errorModel;
    }

    // 自定義例外類別，用於封裝驗證錯誤
    @Getter
    private static class ValidationException extends Exception {
        private final String errorCode;

        public ValidationException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }
    }

    @Override
    protected String processRequestData(final ProgramFlow programFlow, final String messageIn) {
        return null;
    }
}
