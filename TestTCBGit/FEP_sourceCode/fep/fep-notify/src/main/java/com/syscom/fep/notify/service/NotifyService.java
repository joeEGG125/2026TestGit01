package com.syscom.fep.notify.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.http.HttpClientConfigureConstant;
import com.syscom.fep.frmcommon.cryptography.Jasypt;
import com.syscom.fep.frmcommon.net.http.HttpClient2;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.frmcommon.util.UUIDUtil;
import com.syscom.fep.mybatis.ext.mapper.NotifycontentExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NotifyrequestExtMapper;
import com.syscom.fep.mybatis.mapper.NotifycontentMapper;
import com.syscom.fep.mybatis.mapper.NotifyrequestMapper;
import com.syscom.fep.mybatis.model.Notifycontent;
import com.syscom.fep.mybatis.model.Notifyrequest;
import com.syscom.fep.mybatis.model.Notifyrule;
import com.syscom.fep.mybatis.model.Notifytemplate;
import com.syscom.fep.notify.common.handler.*;
import com.syscom.fep.notify.config.NotifyConfig;
import com.syscom.fep.notify.dto.request.MiddlePlatformRequestForm;
import com.syscom.fep.notify.dto.request.NotifyMiddlePlatformRequestForm;
import com.syscom.fep.notify.dto.request.NotifyRequestContent;
import com.syscom.fep.notify.dto.request.NotifyRequestForm;
import com.syscom.fep.notify.dto.response.LogNotifyResponse;
import com.syscom.fep.notify.dto.response.MiddlePlatFormApiResponse;
import com.syscom.fep.notify.dto.response.MiddlePlatFormResponse;
import com.syscom.fep.notify.dto.response.NotifyResponse;
import com.syscom.fep.notify.enums.NotifyStatusCode;
import com.syscom.fep.notify.exception.NotifyException;
import com.syscom.fep.notify.model.NotifyContentResponse;
import com.syscom.fep.notify.model.NotifyRuleSetExt;
import com.syscom.fep.notify.util.DateTimeConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.syscom.fep.notify.cnst.NotifyConstant.*;
import static com.syscom.fep.notify.enums.NotifyStatusCode.*;
import static com.syscom.fep.notify.util.NotifyExpressionParse.expressionParse;

@Slf4j
@Service
@EnableAsync
public class NotifyService extends FEPBase {
    // private static LogHelper logger = LogHelperFactory.getGeneralLogger();

    @Autowired
    private NotifyTemplateService notifyTemplateService;

    @Autowired
    private NotifyRuleSetService notifyRuleSetService;

    @Autowired
    private NotifyRuleService notifyRuleService;

    @Autowired
    private SystemVarsService systemVarsService;

    @Autowired
    private CustomerRuleHandler customerRuleHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotifyEmailHandler notifyEmailHandler;

    @Autowired
    private NotifyNoClassifiedHandler notifyNoClassifiedHandler;
    @Autowired
    private NotifySMSHandler notifySMSHandler;

    //2024-05-02新增Push
    @Autowired
    private NotifyPushHandler notifyPushHandler;
    @Autowired
    private NotifyrequestMapper notifyrequestMapper;

    @Autowired
    private NotifyrequestExtMapper notifyrequestExtMapper;

    @Autowired
    private NotifycontentMapper notifycontentMapper;

    @Autowired
    private NotifycontentExtMapper notifycontentExtMapper;

    @Autowired
    private NotifyConfig notifyConfig;

    @Autowired
    @Qualifier(HttpClientConfigureConstant.BEAN_NAME_NOTIFY)
    private HttpClient2 httpClient2;

    public NotifyResponse sendNotify(LogData logData, NotifyRequestForm inputRequestForm, Boolean isMiddlePlatform) throws NotifyException {
        // 檢查前端傳送的變數和通知模版
        List<Notifycontent> notifyContents = new ArrayList<>();
        Set<String> failures = new HashSet<>();
        try {
            this.setContentsAndFailures(logData, inputRequestForm, notifyContents, failures, isMiddlePlatform);
        } catch (RuntimeException e) {
            throw new NotifyException(e, SYSTEM_ERROR, inputRequestForm.getClientId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (!isMiddlePlatform && notifyContents.size() == 0) {
            // logger.info("檢查前端傳送的變數和通知模版，沒有任何 template 符合發送通知。");
            logData.setRemark("檢查前端傳送的變數和通知模版，沒有任何 template 符合發送通知。");
            logData.setProgramName(StringUtils.join(ProgramName, ".sendNotify"));
            logMessage(logData);
            Map<String, Object> msg = new HashMap<>();
            msg.put(NOTIFY_MESSAGE_FAILURES, failures);       // 有問題的TemplateId
            throw new NotifyException(NOTIFY_RULES_UNLESS, inputRequestForm.getClientId(), msg);
        }
        //檢查ID,如果沒有則生成一個ClientId
        if (StringUtils.isBlank(inputRequestForm.getClientId())) {
            if (StringUtils.isBlank(inputRequestForm.getEJNo())) {
                // 生成一個15位的隨機數
                // 2024-09-28 Richard modified for 【Use of Non Cryptographic Random】
                // Random random = new Random();
                SecureRandom random = new SecureRandom();
                long random15DigitNumber = (long) (Math.pow(10, 14) + random.nextDouble() * Math.pow(10, 14));
                inputRequestForm.setEJNo(Long.toString(random15DigitNumber));
            }
            if (StringUtils.isBlank(inputRequestForm.getTXDate())) {
                inputRequestForm.setTXDate(DateTimeConvertUtil.nowDateTimeString2());
            }
            UUID uniqueUUID = generateUniqueUUID(LocalDateTime.parse(inputRequestForm.getTXDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), inputRequestForm.getEJNo());
            inputRequestForm.setClientId(uniqueUUID.toString());
        }

        NotifyResponse notifyResponse = new NotifyResponse();
        notifyResponse.setClientId(inputRequestForm.getClientId());
        notifyResponse.setEJNo(inputRequestForm.getEJNo());
        notifyResponse.setTXDate(inputRequestForm.getTXDate());
        notifyResponse.setCode(NOTIFY_PROCESSING.getCode());
        if (failures.size() > 0 && failures.size() == inputRequestForm.getContents().size()) {
            Map<String, Object> message = new HashMap<>();
            message.put(NOTIFY_MESSAGE_FAILURES, failures);
            notifyResponse.setCode(NOTIFY_FAILURE.getCode());
            notifyResponse.setMessage(message);
        }
        if (failures.size() > 0 && failures.size() < inputRequestForm.getContents().size()) {
            Map<String, Object> message = new HashMap<>();
            message.put(NOTIFY_MESSAGE_FAILURES, failures);
            notifyResponse.setMessage(message);
        }

        try {
            createNotify(logData, inputRequestForm, notifyContents, failures, isMiddlePlatform);       // 發送通知
        } catch (JsonProcessingException e) {
            //e.printStackTrace()
            // com.syscom.fep.common.log.LogHelperFactory.getTraceLogger().error(e, e.getMessage());
            throw new NotifyException(e, SYSTEM_ERROR, inputRequestForm.getClientId());
        }

        return notifyResponse;
    }

    /**
     * @param logData
     * @param inputRequestForm
     * @param notifyContents
     * @param failures
     * @param isMiddlePlatform
     * @throws NotifyException
     * @throws JsonProcessingException
     */
    @Async
    public void createNotify(LogData logData, NotifyRequestForm inputRequestForm, List<Notifycontent> notifyContents, Set<String> failures, Boolean isMiddlePlatform) throws NotifyException, JsonProcessingException {
        Notifyrequest notifyRequest = new Notifyrequest();
        notifyRequest.setClientId(inputRequestForm.getClientId());
        notifyRequest.setRequestParms(objectMapper.writeValueAsString(inputRequestForm.getContents()));
        notifyRequest.setStatus(NOTIFY_PROCESSING.getCode());
        notifyRequest.setUpdateUserId(UPDATE_USER);
        notifyRequest.setUpdateTime(new Date());
        if (!Objects.isNull(inputRequestForm.getRuleSetId()) && !Strings.isBlank(inputRequestForm.getRuleSetId())) {
            notifyRequest.setRulesetId(Long.valueOf(inputRequestForm.getRuleSetId()));
        }
        try {
            notifyrequestMapper.insert(notifyRequest);
        } catch (DuplicateKeyException e) {
            logData.setRemark("clientId:" + notifyRequest.getClientId() + " is Duplicated, ClientId will combine Ej:" + inputRequestForm.getClientId() + inputRequestForm.getEJNo());
            logData.setProgramName(StringUtils.join(ProgramName, ".createNotify"));
            logMessage(logData);
            // logger.warn(CLIENTID_DUPLICATION.getDesc());
//            throw new NotifyException(CLIENTID_DUPLICATION, inputRequestForm.getClientId(), CLIENTID_DUPLICATION.getDesc());
            notifyRequest.setClientId(inputRequestForm.getClientId() + inputRequestForm.getEJNo());
            notifyrequestMapper.insert(notifyRequest);
        } catch (Exception e) {
            // logger.error(e);
            throw new NotifyException(e, SYSTEM_ERROR, inputRequestForm.getClientId());
        }
        if (isMiddlePlatform) {//中台呼叫,則只寫入一筆
            Notifycontent notifycontentDb = new Notifycontent();
            notifycontentDb.setRequestId(notifyRequest.getRequestId());
            notifycontentDb.setRequestSeq(1);
            notifycontentDb.setTemplateId(inputRequestForm.getContents().get(0).getTemplateId());
            notifycontentDb.setContentIndex(inputRequestForm.getContents().get(0).getContentIndex());
            notifycontentDb.setContent(objectMapper.writeValueAsString(inputRequestForm.getContents()));
            notifycontentDb.setStatus(inputRequestForm.getContents().get(0).getStatus());
            notifycontentDb.setMessage(inputRequestForm.getContents().get(0).getMessage());
            notifycontentDb.setUpdateUserId(UPDATE_USER);
            notifycontentDb.setUpdateTime(new Date());
            // 寫入DB notifyContent
            notifycontentMapper.insert(notifycontentDb);
        } else {
            // 發送通知
            for (Notifycontent notifyContent : notifyContents) {
                // 開始發送通知
                Gson gson = new Gson();
                Map contents = gson.fromJson(notifyContent.getContent(), Map.class);
                NotifyStatusCode sendStatus = SEND_FINISH;
                sendStatus = send(logData, contents);
                notifyContent.setStatus(sendStatus.getCode());
                if (sendStatus.equals(SEND_FAILURE)) {
                    // failures.add(String.valueOf(notifyContent.getContentIndex()));
                    failures.add(notifyContent.getContentIndex()); // Privacy Violation: Heap Inspection
                }
                // 將 notifyContent 轉成 DB notifyocntent
                Notifycontent notifycontentDb = new Notifycontent();
                notifycontentDb.setRequestId(notifyRequest.getRequestId());
                notifycontentDb.setRequestSeq(notifyContent.getRequestSeq());
                notifycontentDb.setTemplateId(notifyContent.getTemplateId());
                notifycontentDb.setContentIndex(notifyContent.getContentIndex());
                notifycontentDb.setContent(objectMapper.writeValueAsString(StringUtils.isBlank(notifyContent.getContent()) ? "" : notifyContent.getContent()));
                if (isMiddlePlatform) {
                    notifycontentDb.setStatus(inputRequestForm.getContents().get(0).getStatus());
                } else {
                    notifycontentDb.setStatus(notifyContent.getStatus());
                }
                notifycontentDb.setMessage(StringUtils.isBlank(notifyContent.getMessage()) ? sendStatus.getDesc() : notifyContent.getMessage());
                notifycontentDb.setUpdateUserId(UPDATE_USER);
                notifycontentDb.setUpdateTime(new Date());
                // 寫入DB notifyContent
                notifycontentMapper.insert(notifycontentDb);
            }

            if (!isMiddlePlatform) { //非中台呼叫時發送後update狀態
                if (failures.size() == 0) {
                    notifyRequest.setStatus(NOTIFY_FINISH.getCode());
                }
                if (failures.size() > 0 && failures.size() == inputRequestForm.getContents().size()) {
                    notifyRequest.setStatus(NOTIFY_FAILURE.getCode());
                    notifyRequest.setFailures(objectMapper.writeValueAsString(failures));
                    Map<String, Object> message = new HashMap<>();
                    message.put(NOTIFY_MESSAGE_FAILURES, failures);
                }
                if (failures.size() > 0 && failures.size() < inputRequestForm.getContents().size()) {
                    notifyRequest.setStatus(NOTIFY_PARTIAL_FAILURE.getCode());
                    notifyRequest.setFailures(objectMapper.writeValueAsString(failures));
                    Map<String, Object> message = new HashMap<>();
                    message.put(NOTIFY_MESSAGE_FAILURES, failures);
                }
                notifyRequest.setUpdateTime(new Date());
                // update DB notifyRequest status
                notifyrequestMapper.updateByPrimaryKey(notifyRequest);
            }
        }
    }

    private NotifyStatusCode send(LogData logData, Map<String, String> content) {
        try {
            // logger.info("NotifyService--Begin NotifyService.send()...");
            logData.setRemark("NotifyService--Begin NotifyService.send()...");
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logMessage(logData);
            if (NOTIFY_TYPE_EMAIL.equals(content.get("Type"))) {
                // logger.info("Begin NotifyEmailHandler.send()...");
                logData.setRemark("Begin NotifyEmailHandler.send()...");
                logData.setProgramName(StringUtils.join(ProgramName, ".send"));
                logMessage(logData);
                notifyEmailHandler.send(logData, content);
            }
            if (NOTIFY_TYPE_SMS.equals(content.get("Type"))) {
                // logger.info("Begin NotifySMSHandler.send()...");
                logData.setRemark("Begin NotifySMSHandler.send()...");
                logData.setProgramName(StringUtils.join(ProgramName, ".send"));
                logMessage(logData);
                notifySMSHandler.send(logData, content);
            }
            //2024-05-02新增Push推播
            if (NOTIFY_TYPE_PUSH.equals(content.get("Type"))) {
                // logger.info("Begin NotifyPushHandler.send()...");
                logData.setRemark("Begin NotifyPushHandler.send()...");
                logData.setProgramName(StringUtils.join(ProgramName, ".send"));
                logMessage(logData);
                notifyPushHandler.send(logData, content);
            }
            if (NOTIFY_TYPE_NOCLASSIFIED.equals(content.get("Type"))) {
                logData.setRemark("Begin NotifyNoClassifiedHandler.send()...");
                logData.setProgramName(StringUtils.join(ProgramName, ".send"));
                logMessage(logData);
                notifyNoClassifiedHandler.send(logData, content);
            }
            return SEND_FINISH;
        } catch (Exception e) {
            // logger.error(SEND_FAILURE.getDesc());
            logData.setRemark(SEND_FAILURE.getDesc());
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logData.setProgramException(e);
            sendEMS(logData);
            return SEND_FAILURE;
        }
    }

    public LogNotifyResponse logNotify(LogData logData, NotifyRequestForm inputRequestForm, Notifyrequest notifyRequest) throws NotifyException, JsonProcessingException {
        LogNotifyResponse logNotifyResponse = new LogNotifyResponse();

        notifyRequest.setClientId(inputRequestForm.getClientId());
        notifyRequest.setRequestParms(objectMapper.writeValueAsString(inputRequestForm.getContents()));
        notifyRequest.setStatus(NOTIFY_PROCESSING.getCode());
        notifyRequest.setUpdateUserId(UPDATE_USER);
        notifyRequest.setUpdateTime(new Date());
        if (!Objects.isNull(inputRequestForm.getRuleSetId()) && !Strings.isBlank(inputRequestForm.getRuleSetId())) {
            notifyRequest.setRulesetId(Long.valueOf(inputRequestForm.getRuleSetId()));
        }
        try {
            //拿到NOTIFYREQUEST的REQUESTID後去查
            Notifyrequest notifyrequest = notifyrequestExtMapper.byClientId(notifyRequest.getClientId());
            if (notifyrequest.getStatus() == NOTIFY_PROCESSING.getCode()) {//還在處理中就直接回傳
                throw new NotifyException(NOTIFY_PROCESSING, inputRequestForm.getClientId(), NOTIFY_PROCESSING.getDesc());
            }
            logNotifyResponse.setRequestId(String.valueOf(notifyrequest.getRequestId()));//通知請求ID
            logNotifyResponse.setStatus(notifyrequest.getStatus());//狀態

            List<Notifycontent> notifyContents = notifycontentExtMapper.getNotifycontentsByRequestId(notifyrequest.getRequestId().toString());
            if (notifyContents.size() == 0) {
                // logger.info("檢查前端傳送的變數，查詢條件無符合資料。");
                logData.setRemark("檢查前端傳送的變數，查詢條件無符合資料。");
                logData.setProgramName(StringUtils.join(ProgramName, ".logNotify"));
                logMessage(logData);
                Map<String, Object> msg = new HashMap<>();
                msg.put("訊息", "查詢NotifyContent條件無符合資料");       // 沒有查到通知請求記錄檔
                throw new NotifyException(NOTIFY_RULES_UNLESS, inputRequestForm.getClientId(), msg);
            }

            NotifyContentResponse notifyContentResponse = new NotifyContentResponse();
            List<NotifyContentResponse> notifyContentResponses = new ArrayList<>();
            for (Notifycontent notifycontent : notifyContents) {
                notifyContentResponse.setContentIndex(notifycontent.getContentIndex());
                notifyContentResponse.setContentStatus(notifycontent.getStatus());
                notifyContentResponse.setMessage(notifycontent.getMessage());
                notifyContentResponses.add(notifyContentResponse);
            }
            logNotifyResponse.setClientId(inputRequestForm.getClientId());
            logNotifyResponse.setEJNo(inputRequestForm.getEJNo());
            logNotifyResponse.setTXDate(inputRequestForm.getTXDate());
            logNotifyResponse.setNotifyContentResponses(notifyContentResponses);//NotifyContent

        } catch (DuplicateKeyException e) {
            // logger.warn(CLIENTID_DUPLICATION.getDesc());
            throw new NotifyException(CLIENTID_DUPLICATION, inputRequestForm.getClientId(), CLIENTID_DUPLICATION.getDesc());
        } catch (NotifyException e) {
            // logger.error(e);
            throw e;
        } catch (Exception e) {
            // logger.error(e);
            throw new NotifyException(e, SYSTEM_ERROR, inputRequestForm.getClientId());
        }

        return logNotifyResponse;
    }

    /**
     * 塞相關資料到NotifyContent後call中台api
     *
     * @param logData
     * @param inputRequestForm
     * @return
     * @throws NotifyException
     * @throws JsonProcessingException
     */
    public MiddlePlatFormResponse createNotifyFromMiddlePlatform(LogData logData, NotifyMiddlePlatformRequestForm inputRequestForm) throws NotifyException {
        MiddlePlatFormResponse response = new MiddlePlatFormResponse();
        try {
            MiddlePlatformRequestForm middlePlatformRequestForm = this.setMiddlePlatformRequestForm(inputRequestForm);
            MiddlePlatformRequestForm.Content content = middlePlatformRequestForm.getServiceRequest().getContent();
            String ejNo = inputRequestForm.getEJNo();
            String txDate = inputRequestForm.getTXDate();

            response = this.doMiddlePlatFormApi(logData, middlePlatformRequestForm);

            // Create NotifyRequestForm並使用sendNotify發送通知
            NotifyRequestForm notifyRequestForm = new NotifyRequestForm();
            notifyRequestForm.setEJNo(ejNo);
            notifyRequestForm.setTXDate(txDate);
            notifyRequestForm.setClientId("");//因有傳ejNo,txDate會組成ClientId而不需要ClientId
            NotifyRequestContent notifyRequestContent = new NotifyRequestContent();
            notifyRequestContent.setTemplateId(content.getNtfButype()); //Set templateId
            notifyRequestContent.setParmVars(content.getMessageFields());
            notifyRequestContent.setContentIndex(UUIDUtil.randomUUID());//Set contentIndex
            notifyRequestContent.setStatus(response.getCode());
            notifyRequestContent.setMessage(response.getDescription());
            notifyRequestForm.setContents(Collections.singletonList(notifyRequestContent));
            sendNotify(logData, notifyRequestForm, true);

        } catch (NotifyException e) {
            // logger.error(e);
            throw new NotifyException(e, SYSTEM_ERROR, inputRequestForm.getEJNo(), inputRequestForm.getTXDate());
        }

        return response;
    }

    /**
     * 檢查前端傳送的變數和通知模版後塞入NotifyContent和failures
     *
     * @param logData
     * @param inputRequestForm
     * @param notifyContents
     * @param failures
     * @throws NotifyException
     */
    private void setContentsAndFailures(LogData logData, NotifyRequestForm inputRequestForm, List<Notifycontent> notifyContents, Set<String> failures, Boolean isMiddlePlatform) throws NotifyException, JsonProcessingException {
        List<NotifyRequestContent> requestContents = inputRequestForm.getContents();    //  前端傳送變數 Contents，
        String ruleSetId = inputRequestForm.getRuleSetId();
        int seq = 0;

        // 若 RuleSet 不存在將中止作業.並回應 Code 100 錯誤
        if (Strings.isNotBlank(ruleSetId)) {
            try {
                notifyRuleSetService.getNotifyRuleSetById(logData, Long.valueOf(ruleSetId));
            } catch (NotifyException e) {
                e.setClientId(inputRequestForm.getClientId());
                throw e;
            }
        }

        for (NotifyRequestContent requestContent : requestContents) {
            // 取得Notifytemplates(cache)
            List<Notifytemplate> notifyTemplates = notifyTemplateService.getNotifyTemplatesById(logData, requestContent.getTemplateId());
            for (Notifytemplate notifyTemplate : notifyTemplates) {// 一個 template 可能有多個通知方式
                //如果Content沒有塞ContentIndex就生成一個
                if (StringUtils.isBlank(requestContent.getContentIndex())) {
                    requestContent.setContentIndex(UUIDUtil.randomUUID());
                }
                // 檢查template必要參數
                if (Objects.isNull(notifyTemplate)) {
                    // logger.warn("clientId: " + inputRequestForm.getClientId() + ", 沒有 templateId:" + requestContent.getTemplateId() + " 的模版");
                    logData.setRemark("clientId: " + inputRequestForm.getClientId() + ", 沒有 templateId:" + requestContent.getTemplateId() + " 的模版");
                    logData.setProgramName(StringUtils.join(ProgramName, ".setContentsAndFailures"));
                    logMessage(Level.WARN, logData);
                    failures.add(String.valueOf(requestContent.getContentIndex()));
                    continue;// 中止這個模版後面的檢查。檢查下一個模版
                }

                Map<String, String> parmVars = requestContent.getParmVars();
                String type = notifyTemplate.getType();
                if (!isMiddlePlatform) { //非中台呼叫時才檢查parameter
                    if (NOTIFY_TYPE_EMAIL.equals(type) && Objects.isNull(parmVars.get(NOTIFY_EMAIL_PARM_NAME))) {
                        // logger.info("clientId: " + inputRequestForm.getClientId() + ", type 為 M 時, ##Email## 為必要變數!");
                        logData.setRemark("clientId: " + inputRequestForm.getClientId() + ", type 為 M 時, ##Email## 為必要變數!");
                        logData.setProgramName(StringUtils.join(ProgramName, ".setContentsAndFailures"));
                        logMessage(logData);
                        failures.add(String.valueOf(requestContent.getContentIndex()));
                        continue;   // 中止這個模版後面的檢查。檢查下一個模版
                    }
                    if (NOTIFY_TYPE_SMS.equals(type) && Objects.isNull(parmVars.get(NOTIFY_PHONE_PARM_NAME))) {
                        // logger.info("clientId: " + inputRequestForm.getClientId() + ", type 為 S 時, ##Phone## 為必要變數!");
                        logData.setRemark("clientId: " + inputRequestForm.getClientId() + ", type 為 S 時, ##Phone## 為必要變數!");
                        logData.setProgramName(StringUtils.join(ProgramName, ".setContentsAndFailures"));
                        logMessage(logData);
                        failures.add(String.valueOf(requestContent.getContentIndex()));
                        continue;   // 中止這個模版後面的檢查。繼續檢查下一個模版
                    }
                }

                if (Strings.isNotBlank(ruleSetId)) {
                    // 這則通知須通過檢查 Rule 後,才可發送。若沒通過 Rule 檢查。中止後面的檢查。繼續檢查下一個模版
                    try {
                        checkRule(logData, ruleSetId, parmVars, failures, requestContent);
                    } catch (NotifyException e) {
                        // 若沒通過 Rule, 將檢查下一個模版. 明祥表示只要有 Template 符合條件仍需發送通知
                        // logger.warn("clientId: " + inputRequestForm.getClientId() + ", RuleSet expression Error!");
                        logData.setRemark("clientId: " + inputRequestForm.getClientId() + ", RuleSet expression Error!");
                        logData.setProgramName(StringUtils.join(ProgramName, ".setContentsAndFailures"));
                        logData.setProgramException(e);
                        sendEMS(logData);
                        failures.add(String.valueOf(requestContent.getContentIndex()));
                        continue;
                    } catch (SpelParseException e) {
                        // logger.error(e, "clientId: " + inputRequestForm.getClientId() + ",spel expression Error!");
                        logData.setRemark("clientId: " + inputRequestForm.getClientId() + ",spel expression Error!");
                        logData.setProgramName(StringUtils.join(ProgramName, ".setContentsAndFailures"));
                        logData.setProgramException(e);
                        sendEMS(logData);
                        //e.printStackTrace()
                        failures.add(String.valueOf(requestContent.getContentIndex()));
                        continue;
                    } catch (RuntimeException e) {
                        // logger.error(e, "clientId: " + inputRequestForm.getClientId() + ",RuleSet expression Error!");
                        logData.setRemark("clientId: " + inputRequestForm.getClientId() + ",RuleSet expression Error!");
                        logData.setProgramName(StringUtils.join(ProgramName, ".setContentsAndFailures"));
                        logData.setProgramException(e);
                        sendEMS(logData);
                        //e.printStackTrace()
                        failures.add(String.valueOf(requestContent.getContentIndex()));
                        continue;
                    }
                }

                // 將組合通知內容
                String body = "";
                if (isMiddlePlatform) {
                    body = inputRequestForm.getContents().get(0).getParmVars().getOrDefault("REMARK", "");
                } else {
                    body = parser(logData, parmVars, notifyTemplate.getTemplate());
                }
                if (body.lastIndexOf(INDEPENDEN_VAR_SYMBOL) > -1 || body.lastIndexOf(SYSTEM_VAR_SYMBOL) > -1) {
                    // logger.warn("前端傳送變數和系統變數無法滿足模版。");
                    // logger.warn(body);
                    logData.setRemark("前端傳送變數和系統變數無法滿足模版。 body:" + body);
                    logData.setProgramName(StringUtils.join(ProgramName, ".setContentsAndFailures"));
                    logMessage(Level.WARN, logData);
                    failures.add(String.valueOf(requestContent.getContentIndex()));
                    continue;   // 中止這個模版後面的檢查。檢查下一個模版
                }

                Map<String, String> content = new HashMap<>();
                content.put(NOTIFY_MESSAGE_CONTENT_TYPE, type);
                content.put(NOTIFY_MESSAGE_CONTENT_SUBJECT, notifyTemplate.getSubject());
                content.put(NOTIFY_MESSAGE_CONTENT_BODY, body);
                content.put(NOTIFY_MESSAGE_CONTENT_CLIENTID, inputRequestForm.getClientId());
                if (type.equalsIgnoreCase(NOTIFY_TYPE_EMAIL)) { // 郵件
                    String[] providers = notifyTemplate.getProvider().split(":");
                    if (providers.length == 2) {
                        content.put(NOTIFY_MESSAGE_PROVIDER, providers[0]);
                        content.put(NOTIFY_MESSAGE_PROVIDER_PORT, providers[1]);
                    } else {
                        throw new NotifyException(PROVIDER_ERROR, inputRequestForm.getClientId());
                    }
                    // 2025-02-20 Richard add 如果有附加主旨, 則進行拼接
                    String subjectSuffix = parmVars.remove(NOTIFY_MESSAGE_CONTENT_SUBJECT_SUFFIX);
                    if (StringUtils.isNoneBlank(notifyTemplate.getSubject()) && StringUtils.isNotEmpty(subjectSuffix)) {
                        content.put(NOTIFY_MESSAGE_CONTENT_SUBJECT, StringUtils.join(notifyTemplate.getSubject(), "-", subjectSuffix));
                    }
                } else if (type.equalsIgnoreCase(NOTIFY_TYPE_SMS)) { // 簡訊
                    if (StringUtils.isBlank(notifyTemplate.getProvider())) {
                        throw new NotifyException(PROVIDER_ERROR, inputRequestForm.getClientId());
                    }
                    content.put(NOTIFY_MESSAGE_DOMAIN, notifyTemplate.getProvider());
                }
                try {
                    content.put(NOTIFY_MESSAGE_ACCOUNT, Jasypt.decrypt(notifyTemplate.getAccount())); //ENC解密
                } catch (Exception e) {
                    logData.setRemark(StringUtils.join("decrypt account failed, ", e.getMessage(), ", account:", notifyTemplate.getAccount()));
                    logData.setProgramName(StringUtils.join(ProgramName, ".setContentsAndFailures"));
                    logMessage(Level.WARN, logData);
                    content.put(NOTIFY_MESSAGE_ACCOUNT, notifyTemplate.getAccount());
                }
                try {
                    content.put(NOTIFY_MESSAGE_SSCODE, Jasypt.decrypt(notifyTemplate.getSscode())); //ENC解密
                } catch (Exception e) {
                    logData.setRemark(StringUtils.join("decrypt sscode failed, ", e.getMessage(), ", sscode:", notifyTemplate.getSscode()));
                    logData.setProgramName(StringUtils.join(ProgramName, ".setContentsAndFailures"));
                    logMessage(Level.WARN, logData);
                    content.put(NOTIFY_MESSAGE_SSCODE, notifyTemplate.getSscode());
                }
                // content.put(NOTIFY_MESSAGE_SSLONCONNECT, "false"); //TODO未來是否需要加入這個欄位???

                parmVars.forEach((k, v) -> {
                    content.put(k, v);
                });

                Notifycontent notifyContent = new Notifycontent();
                notifyContent.setTemplateId(requestContent.getTemplateId());
                notifyContent.setContentIndex(requestContent.getContentIndex());
                notifyContent.setRequestSeq(++seq);
                notifyContent.setContent(objectMapper.writeValueAsString(content));
                notifyContents.add(notifyContent);
            }
        }
    }

    private void checkRule(LogData logData, String ruleSetId, Map<String, String> parmVars, Set<String> failures, NotifyRequestContent requestContent) throws NotifyException {
        // 前端傳送變數 ruleSetId 不為空時, 從 Cache 取出 ruleSet 的 運算式。
        NotifyRuleSetExt notifyRuleSetExt = notifyRuleSetService.getNotifyRuleSetById(logData, Long.valueOf(ruleSetId));
        String expressionOrg = notifyRuleSetExt.getExpression();
        String customerComponent = notifyRuleSetExt.getCustomerComponent();
        // 當 customComponent 有值時，不用檢查 expression
        if (Strings.isNotBlank(customerComponent)) {
            // todo : 僅訂出 customerComponent 殻，還沒有實例
            try {
                if (!customerRuleHandler.proccess(logData, customerComponent, requestContent)) {
                    // todo : 尚須確認目前作法是任一模版 NotifyRules 不過。即中止這請求，回應異常。
                    throw new NotifyException();
                    //failures.add(String.valueOf(templateParmVars.getTemplateId()));
                }
            } catch (Exception e) {
                // logger.warn("customerComponent:" + customerComponent + " 沒有通過檢查");
                logData.setRemark("customerComponent:" + customerComponent + " 沒有通過檢查");
                logData.setProgramName(StringUtils.join(ProgramName, ".checkRule"));
                logMessage(Level.WARN, logData);
                throw new NotifyException(SYSTEM_ERROR, e.getMessage());
            }
        } else if (Strings.isNotBlank(expressionOrg)) {
            String ruleExpression = notifyRuleSetExt.getRuleExpression();
            Map<String, String> expressionVars = new HashMap<>();
            getExpressParms(logData, ruleExpression, parmVars, expressionVars);

            String expression = parser(logData, expressionVars, expressionOrg);
            if (expression.lastIndexOf(INDEPENDEN_VAR_SYMBOL) > -1 || expression.lastIndexOf(SYSTEM_VAR_SYMBOL) > -1) {
                // 模版 NotifyRules 不過。
                String errorMsg = String.format("前端傳送變數和系統變數無法滿足 RuleSet expression。 RuleSetId:%s， Expression:%s", ruleSetId, expression);
                // logger.warn(errorMsg);
                throw new NotifyException(NOTIFY_RULES_UNLESS, errorMsg);
            }

            // 解析組合後的 RuleSet expression 是否符合發送通知條件
            ExpressionParser parser = new SpelExpressionParser();
            boolean evaluate = parser.parseExpression(expression).getValue(Boolean.class);
            if (!evaluate) {
                String errorMsg = String.format("無法滿足發送通知的條件，TemplateId:%s， RuleSetId:%s， Expression:%s", requestContent.getTemplateId(), ruleSetId, expression);
                // logger.info(errorMsg);
                throw new NotifyException(NOTIFY_RULES_UNLESS, errorMsg);
            }
        } else {
            // logger.warn("RuleSetId:", ruleSetId, ", 沒有定義 customComponent 也沒有定義 notifyexpression!");
            logData.setRemark(StringUtils.join("RuleSetId:", ruleSetId, ", 沒有定義 customComponent 也沒有定義 notifyexpression!"));
            logData.setProgramName(StringUtils.join(ProgramName, ".checkRule"));
            logMessage(Level.WARN, logData);
        }
    }

    /**
     * 當變數為 Number 時，要去除千分位符號.當變數為 Date 時，要將 - 置換成 /
     *
     * @param logData
     * @param expression
     * @param parmVars
     * @param expressionVars
     * @throws NotifyException
     */
    private void getExpressParms(LogData logData, String expression, Map<String, String> parmVars, Map<String, String> expressionVars) throws NotifyException {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        char[] chars = expression.toCharArray();
        int pos = chars.length;
        String v = "";
        v += chars[--pos];
        boolean checkFirst = false;
        boolean checkSecond = false;
        List<String> list = new ArrayList<>();
        for (int i = pos; 0 < i; ) {
            checkFirst = (Character.isDigit(chars[i])) ? true : false;
            checkSecond = (Character.isDigit(chars[--i])) ? true : false;
            if (checkFirst == checkSecond) {
                v = chars[i] + v;
            } else {
                if (pattern.matcher(v).matches()) list.add(v);
                v = "" + chars[i];
            }
        }
        if (pattern.matcher(v).matches()) {
            list.add(v);
        }

        for (String ruleId : list) {
            Notifyrule rule = notifyRuleService.getNotifyRuleById(logData, Long.valueOf(ruleId));
            String parmName = rule.getParmVarName();
            String parmValue = parmVars.get(parmName);
            if ("NUMBER".equals(rule.getParmValueType())) {
                // 當變數為 Number 時，要去除千分位符號
                parmValue = parmValue.replaceAll(",", "");
            }
            if ("DATE".equals(rule.getParmValueType())) {
                // 當變數為 Date 時，要將 - 置換成 /
                parmValue = DateTimeConvertUtil.dateTimeString(parmValue);
            }
            expressionVars.put(parmName, parmValue);
        }
    }


    private String parser(LogData logData, Map<String, String> vars, String expressionOrg) {
        AtomicReference<String> ref = new AtomicReference<>(expressionOrg);
        Set<String> independentVars = new HashSet<>();        // 前端傳送變數
        Set<String> systemVars = new HashSet<>();             // 系統參數
        expressionParse(logData, independentVars, INDEPENDEN_VAR_SYMBOL, ref.get());    // 解析字串，將內容有標記的前端傳送變數放到 independentVars
        expressionParse(logData, systemVars, SYSTEM_VAR_SYMBOL, ref.get());             // 解析字串，將內容有標記的系統變數放到 systemVars

        independentVars.forEach((v) -> {
            // 置換內容標記前端傳送變數
            String var = v.replace(INDEPENDEN_VAR_SYMBOL, "");
            if (Objects.isNull(vars.get(var))) return;
            ref.updateAndGet(s -> s.replaceAll(v, String.valueOf(vars.get(var))));

        });
        systemVars.forEach((v) -> {
            // 置換內容標記系統變數
            if ("#@SYSDATE#@".equals(v)) {
                // 系統變數不是從資料庫取得
                ref.updateAndGet(s -> s.replaceAll(v, String.valueOf(DateTimeConvertUtil.nowDateString())));
            }
            if ("#@SYSDATETIME#@".equals(v)) {
                // 系統變數不是從資料庫取得
                ref.updateAndGet(s -> s.replaceAll(v, String.valueOf(DateTimeConvertUtil.nowDateTimeString())));
            }
            if (!Objects.isNull(systemVarsService.getSysStatCache(logData, v))) {
                ref.updateAndGet(s -> s.replaceAll(v, String.valueOf(systemVarsService.getSysStatCache(logData, v))));
            }
        });

        return ref.get();
    }

    public static UUID generateUniqueUUID(LocalDateTime transactionDate, String transactionId) {
        // 將交易日期轉換為納秒數,且固定時區
        long timestamp = transactionDate.toInstant(ZoneOffset.UTC).toEpochMilli() * 1000000;

        // 將交易ID轉換為長整型
        long idAsLong = Long.parseLong(transactionId);

        // 使用UUID的構造方法將時間戳和交易ID組合成UUID
        return new UUID(timestamp, idAsLong);
    }

    /**
     * 塞中台requestForm,不做NotifyTemplate檢查
     *
     * @param inputRequestForm
     * @return
     */
    private MiddlePlatformRequestForm setMiddlePlatformRequestForm(NotifyMiddlePlatformRequestForm inputRequestForm) throws NotifyException {
        MiddlePlatformRequestForm middlePlatformRequestForm = new MiddlePlatformRequestForm();
        MiddlePlatformRequestForm.ServiceRequest serviceRequest = new MiddlePlatformRequestForm.ServiceRequest();
        //拆解參數
        String[] params = new String[] {inputRequestForm.getParam1(), inputRequestForm.getParam2(), inputRequestForm.getParam3(),
                inputRequestForm.getParam4(), inputRequestForm.getParam5(), inputRequestForm.getParam6(), inputRequestForm.getParam7()};
        Map<String, String> messageFields = new HashMap<>();
        for (int i = 0; i < params.length; i++) {
            if (params[i] != null && params[i].contains(">")) {
                String[] parts = params[i].split(">", 2);
                String key = parts[0].substring(1); // Remove the leading '<'
                String value = parts[1];
                if (key != null && value != null) {
                    messageFields.put(key, value);
                }
            }
        }
        MiddlePlatformRequestForm.Content content = new MiddlePlatformRequestForm.Content();
        content.setNtfButype(inputRequestForm.getTemplateId());
        content.setNtfAcctNo(inputRequestForm.getNtfAcctNo());
        content.setNtfSendObj("");
        content.setMessageFields(messageFields);

        content.setDefMail(inputRequestForm.getDefMail());
        // 2025-04-09 Richard modified for [Privacy Violation]
        // content.setDefPhone(inputRequestForm.getDefphne_number());
        ReflectUtil.setFieldValue(content, "defPhone", inputRequestForm.getDefphne_number());
        content.setDefSms(inputRequestForm.getDefSms());
        content.setDefPush(inputRequestForm.getDefPush());
        content.setDefEmail(inputRequestForm.getDefEmail());

        serviceRequest.setContent(content);
        middlePlatformRequestForm.setServiceRequest(serviceRequest);

        MiddlePlatformRequestForm.Header header = new MiddlePlatformRequestForm.Header();
        //塞預設值
        String now = DateTimeConvertUtil.nowDateTimeString2();
        header.setClientSystemId("FEP");
        header.setClientTimestamp(now);
        header.setClientSeqNo("1");
        header.setValidFlag("N");
        header.setTellerId(inputRequestForm.getTellerId());
        middlePlatformRequestForm.setHeader(header);

        MiddlePlatformRequestForm.ServiceInfo serviceInfo = new MiddlePlatformRequestForm.ServiceInfo();
        serviceInfo.setTxnCode("0826");
        serviceInfo.setTxTimestamp(now);
        serviceInfo.setGlobalCounter(0);
        serviceInfo.setBranchCode("99901");
        serviceRequest.setServiceInfo(serviceInfo);

        return middlePlatformRequestForm;
    }

    private MiddlePlatFormResponse doMiddlePlatFormApi(LogData logData, MiddlePlatformRequestForm middlePlatformRequestForm) throws NotifyException {
        MiddlePlatFormResponse response = new MiddlePlatFormResponse();
        int times = 0;
        NotifyException thrown = null;
        while (times++ < notifyConfig.getRetryCount()) {
            try {
                // Send POST request
                String request = objectMapper.writeValueAsString(middlePlatformRequestForm);
                String resultStr = httpClient2.postForObject(notifyConfig.getSendMiddlePlatForm_URL(), MediaType.APPLICATION_JSON, request, String.class);
                XmlMapper xmlMapper = new XmlMapper();
                MiddlePlatFormApiResponse middlePlatFormApiResponse = xmlMapper.readValue(resultStr, MiddlePlatFormApiResponse.class);

                // 2025-02-08 Richard modified for 【Privacy Violation】
                // response.setCode(middlePlatFormApiResponse.getStatus().getCode().substring(3, 6));//取後3碼
                // response.setDescription(middlePlatFormApiResponse.getStatus().getDescription());
                ReflectUtil.setFieldValue(response, "code", middlePlatFormApiResponse.getStatus().getCode().substring(3, 6));//取後3碼
                ReflectUtil.setFieldValue(response, "description", middlePlatFormApiResponse.getStatus().getDescription());

                return response;
            } catch (SocketTimeoutException e) {
                // Handle the timeout exception
                // logger.warn("Request timed out", e);
                logData.setRemark("Request timed out");
                logData.setProgramName(StringUtils.join(ProgramName, ".doMiddlePlatFormApi"));
                logData.setProgramException(e);
                sendEMS(logData);
            } catch (Exception e) {
                // logger.warn("Request failed", e);
                logData.setRemark("Request failed");
                logData.setProgramName(StringUtils.join(ProgramName, ".doMiddlePlatFormApi"));
                logData.setProgramException(e);
                sendEMS(Level.WARN, logData);
                thrown = (NotifyException) e;
            }
            try {
                Thread.sleep(notifyConfig.getRetrySleep());
            } catch (InterruptedException e) {
                // logger.warn("Request timed out", e);
            }
        }
        String errorMessage = StringUtils.join("doMiddlePlatFormApi ", " still failed after ", times - 1, " times, stop retry");
        // logger.error(errorMessage);
        throw new NotifyException(thrown, SYSTEM_ERROR, errorMessage);
    }

}