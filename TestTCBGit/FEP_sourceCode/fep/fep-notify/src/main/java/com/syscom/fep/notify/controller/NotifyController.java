package com.syscom.fep.notify.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.MDCKeyConst;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.mybatis.model.Notifyrequest;
import com.syscom.fep.notify.dto.request.NotifyMiddlePlatformRequestForm;
import com.syscom.fep.notify.dto.request.NotifyRequestForm;
import com.syscom.fep.notify.dto.response.LogNotifyResponse;
import com.syscom.fep.notify.dto.response.MiddlePlatFormResponse;
import com.syscom.fep.notify.dto.response.NotifyResponse;
import com.syscom.fep.notify.enums.NotifyStatusCode;
import com.syscom.fep.notify.exception.NotifyException;
import com.syscom.fep.notify.service.NotifyService;
import com.syscom.fep.notify.util.DateTimeConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.syscom.fep.notify.enums.NotifyStatusCode.SYSTEM_ERROR;

@RestController
public class NotifyController extends FEPBase {
    // private static final LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private NotifyService notifyService;

    @RequestMapping(value = "sendNotify", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NotifyResponse sendNotify(@Valid @RequestBody NotifyRequestForm inputRequestForm) throws NotifyException {
        LogMDC.put(MDCKeyConst.MDC_PROFILE, SvrConst.SVR_NOTIFY);
        LogData logData = new LogData();
        logData.setEj(StringUtils.isNumeric(inputRequestForm.getEJNo()) ? Integer.parseInt(inputRequestForm.getEJNo()) : 0);
        logData.setTxDate(inputRequestForm.getTXDate());
        logData.setTxRquid(inputRequestForm.getClientId());
        try {
            logData.setMessage(objectMapper.writeValueAsString(inputRequestForm));
        } catch (JsonProcessingException e) {
            com.syscom.fep.common.log.LogHelperFactory.getGeneralLogger().warn(e, e.getMessage());
        }
        logData.setServiceUrl("/sendNotify");
        logData.setRemark(StringUtils.join(SvrConst.SVR_NOTIFY, " Receive Request"));
        logData.setProgramName(StringUtils.join(ProgramName, ".sendNotify"));
        logMessage(logData);
        NotifyResponse notifyResponse = null;
        try {
            notifyResponse = notifyService.sendNotify(logData, inputRequestForm, false);
            return notifyResponse;
        } catch (NotifyException | RuntimeException e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".sendNotify"));
            logData.setProgramException(e);
            sendEMS(logData);
            throw e;
        } finally {
            if (notifyResponse != null) {
                try {
                    logData.setMessage(objectMapper.writeValueAsString(notifyResponse));
                } catch (JsonProcessingException e) {
                    com.syscom.fep.common.log.LogHelperFactory.getGeneralLogger().warn(e, e.getMessage());
                }
                logData.setServiceUrl("/sendNotify");
                logData.setRemark(StringUtils.join(SvrConst.SVR_NOTIFY, " Send Response"));
                logData.setProgramName(StringUtils.join(ProgramName, ".sendNotify"));
                logMessage(logData);
            }
        }
    }

    @RequestMapping(value = "logNotify", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public LogNotifyResponse logNotify(@Valid @RequestBody NotifyRequestForm inputRequestForm) throws NotifyException, JsonProcessingException {
        LogMDC.put(MDCKeyConst.MDC_PROFILE, SvrConst.SVR_NOTIFY);
        LogData logData = new LogData();
        logData.setEj(StringUtils.isNumeric(inputRequestForm.getEJNo()) ? Integer.parseInt(inputRequestForm.getEJNo()) : 0);
        logData.setTxDate(inputRequestForm.getTXDate());
        logData.setTxRquid(inputRequestForm.getClientId());
        try {
            logData.setMessage(objectMapper.writeValueAsString(inputRequestForm));
        } catch (JsonProcessingException e) {
            com.syscom.fep.common.log.LogHelperFactory.getGeneralLogger().warn(e, e.getMessage());
        }
        logData.setServiceUrl("/logNotify");
        logData.setRemark(StringUtils.join(SvrConst.SVR_NOTIFY, " Receive Request"));
        logData.setProgramName(StringUtils.join(ProgramName, ".logNotify"));
        logMessage(logData);
        if (StringUtils.isBlank(inputRequestForm.getTXDate())) {
            inputRequestForm.setTXDate(DateTimeConvertUtil.nowDateTimeString2());
        }
        LogNotifyResponse logNotifyResponse = null;
        try {
            // 如果clientID不是empty就拿id去查，如果沒有clientid檢查交易序號跟交易時間有沒有帶入，有就轉換成clientid去查，沒有就throw new NotifyException();
            if (StringUtils.isBlank(inputRequestForm.getClientId())) {
                UUID uniqueUUID = notifyService.generateUniqueUUID(LocalDateTime.parse(inputRequestForm.getTXDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), inputRequestForm.getEJNo());
                inputRequestForm.setClientId(uniqueUUID.toString());
            } else if ((StringUtils.isBlank(inputRequestForm.getEJNo()) || StringUtils.isBlank(inputRequestForm.getTXDate())) && StringUtils.isBlank(inputRequestForm.getClientId())) {
                throw new NotifyException(SYSTEM_ERROR, "");
            }
            // 查詢
            Notifyrequest notifyRequest = new Notifyrequest();
            logNotifyResponse = notifyService.logNotify(logData, inputRequestForm, notifyRequest);
            return logNotifyResponse;
        } catch (NotifyException | RuntimeException e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".logNotify"));
            logData.setProgramException(e);
            sendEMS(logData);
            throw e;
        } finally {
            if (logNotifyResponse != null) {
                try {
                    logData.setMessage(objectMapper.writeValueAsString(logNotifyResponse));
                } catch (JsonProcessingException e) {
                    com.syscom.fep.common.log.LogHelperFactory.getGeneralLogger().warn(e, e.getMessage());
                }
                logData.setServiceUrl("/logNotify");
                logData.setRemark(StringUtils.join(SvrConst.SVR_NOTIFY, " Send Response"));
                logData.setProgramName(StringUtils.join(ProgramName, ".logNotify"));
                logMessage(logData);
            }
        }
    }

    @RequestMapping(value = "sendMiddlePlatForm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public MiddlePlatFormResponse sendMiddlePlatform(@Valid @RequestBody NotifyMiddlePlatformRequestForm inputRequestForm) throws NotifyException {
        LogMDC.put(MDCKeyConst.MDC_PROFILE, SvrConst.SVR_NOTIFY);
        LogData logData = new LogData();
        logData.setEj(StringUtils.isNumeric(inputRequestForm.getEJNo()) ? Integer.parseInt(inputRequestForm.getEJNo()) : 0);
        logData.setTxDate(inputRequestForm.getTXDate());
        logData.setTxRquid(inputRequestForm.getTemplateId());
        try {
            logData.setMessage(objectMapper.writeValueAsString(inputRequestForm));
        } catch (JsonProcessingException e) {
            com.syscom.fep.common.log.LogHelperFactory.getGeneralLogger().warn(e, e.getMessage());
        }
        logData.setServiceUrl("/sendMiddlePlatForm");
        logData.setRemark(StringUtils.join(SvrConst.SVR_NOTIFY, " Receive Request"));
        logData.setProgramName(StringUtils.join(ProgramName, ".sendMiddlePlatForm"));
        logMessage(logData);
        // 2025-04-09 Richard modified for [Reflected XSS All Clients]
        // return notifyService.createNotifyFromMiddlePlatform(logData, inputRequestForm);
        MiddlePlatFormResponse middlePlatFormResponse = null;
        try {
            try {
                Method method = ReflectionUtils.findMethod(notifyService.getClass(), "createNotifyFromMiddlePlatform", LogData.class, NotifyMiddlePlatformRequestForm.class);
                if (method != null) {
                    ReflectionUtils.makeAccessible(method);
                    middlePlatFormResponse = (MiddlePlatFormResponse) ReflectionUtils.invokeMethod(method, notifyService, logData, inputRequestForm);
                    return middlePlatFormResponse;
                }
            } catch (Throwable e) {
                Throwable t = ExceptionUtil.reflectionInvokeExceptionOccur(e);
                if (t instanceof NotifyException) {
                    throw (NotifyException) t;
                } else if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                }
                throw new NotifyException(e, NotifyStatusCode.SYSTEM_ERROR, inputRequestForm.getEJNo(), inputRequestForm.getTXDate());
            }
            throw new NotifyException(NotifyStatusCode.SYSTEM_ERROR, inputRequestForm.getEJNo(), inputRequestForm.getTXDate());
        } catch (NotifyException | RuntimeException e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".sendMiddlePlatForm"));
            logData.setProgramException(e);
            sendEMS(logData);
            throw e;
        } finally {
            if (middlePlatFormResponse != null) {
                try {
                    logData.setMessage(objectMapper.writeValueAsString(middlePlatFormResponse));
                } catch (JsonProcessingException e) {
                    com.syscom.fep.common.log.LogHelperFactory.getGeneralLogger().warn(e, e.getMessage());
                }
                logData.setServiceUrl("/sendMiddlePlatForm");
                logData.setRemark(StringUtils.join(SvrConst.SVR_NOTIFY, " Send Response"));
                logData.setProgramName(StringUtils.join(ProgramName, ".sendMiddlePlatForm"));
                logMessage(logData);
            }
        }
    }
}
