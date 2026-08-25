package com.syscom.fep.web.controller.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.jms.entity.JmsInfoConcurrency;
import com.syscom.fep.frmcommon.jms.entity.JmsMonitorConcurrencyRequest;
import com.syscom.fep.frmcommon.jms.entity.JmsMonitorResponse;
import com.syscom.fep.frmcommon.jms.entity.JmsMonitorResponseErrorCode;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.frmcommon.parse.GsonParser;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.vo.enums.RestfulResultCode;
import com.syscom.fep.web.configurer.WebQueueConfiguration;
import com.syscom.fep.web.configurer.WebQueueConfiguration.WebQueueReceiver;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.common.UI_080110_Form;
import com.syscom.fep.web.form.common.UI_080110_FormAction;
import com.syscom.fep.web.form.common.UI_080110_FormAction.Action;
import com.syscom.fep.web.form.common.UI_080110_FormActionResp;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UI_080110Controller extends BaseController {
    private static final String URL_DO_QUERY = "/common/UI_080110/doQuery";
    private static final String URL_DO_ACTION = "/common/UI_080110/doAction";
    private static final int COLUMN_LIMIT = 3;

    @Override
    public void pageOnLoad(ModelMap mode) {
        SpringBeanFactoryUtil.registerBean(WebQueueConfiguration.class);
        this.doInquiryMain(mode);
    }

    @PostMapping(value = URL_DO_QUERY)
    public String doInquiryMain(ModelMap mode) {
        this.infoMessage("開始查詢...");
        List<List<UI_080110_Form>> row = new ArrayList<>();
        WebQueueConfiguration queueConfiguration = SpringBeanFactoryUtil.getBean(WebQueueConfiguration.class);
        List<WebQueueReceiver> receivers = queueConfiguration.getReceiver();
        if (receivers.isEmpty()) {
            this.showMessage(mode, MessageType.INFO, QueryNoData);
        } else {
            boolean hasSucceed = false, hasError = false, offline = true;
            for (int i = 0; i < receivers.size(); i++) {
                List<UI_080110_Form> column = null;
                if (i % COLUMN_LIMIT == 0) {
                    row.add(new ArrayList<>());
                }
                // receiver
                WebQueueReceiver receiver = receivers.get(i);
                // concurrency
                JmsInfoConcurrency concurrency = null;
                // reset offline
                offline = true;
                try {
                    JmsMonitorResponse<JmsInfoConcurrency> monitorResponse = this.sendReceive(receiver, Action.GET, null);
                    if (monitorResponse != null) {
                        if (monitorResponse.isResult()) {
                            concurrency = monitorResponse.getData();
                            offline = false;
                        } else {
                            LogData logData = new LogData();
                            logData.setProgramName(StringUtils.join(ProgramName, ".doInquiryMain"));
                            logData.setRemark(StringUtils.join("查詢[", receiver.getName(), "]的MQ線程數返回失敗訊息:[", monitorResponse.getErrorCode(), "]", monitorResponse.getError()));
                            sendEMS(logData);
                            hasError = true;
                        }
                    }
                    hasSucceed = true;
                } catch (Exception e) {
                    this.errorMessage(e, e.getMessage());
                    this.showMessage(mode, MessageType.DANGER, programError);
                    hasError = true;
                }
                if (concurrency == null) {
                    concurrency = new JmsInfoConcurrency();
                }
                UI_080110_Form form = new UI_080110_Form();
                form.setReceiver(receiver);
                form.setConcurrency(concurrency);
                form.setOffline(offline);
                row.get(row.size() - 1).add(form);
            }
            if (!hasError)
                this.showMessage(mode, MessageType.INFO, QuerySuccess);
            else if (hasSucceed)
                this.showMessage(mode, MessageType.WARNING, "部分服務查詢失敗, 無法進行「設置」或「重置」操作, 請稍後再嘗試查詢");
            else
                this.showMessage(mode, MessageType.DANGER, "所有服務查詢失敗, 無法進行「設置」或「重置」操作, 請稍後再嘗試查詢");
        }
        WebUtil.putInAttribute(mode, AttributeName.List, row);
        return Router.UI_080110.getView();
    }

    @PostMapping(value = URL_DO_ACTION)
    @ResponseBody
    public UI_080110_FormActionResp doAction(@RequestBody UI_080110_FormAction formAction) {
        this.infoMessage("執行動作, 條件 = [", formAction.toString(), "]");
        UI_080110_FormActionResp response = new UI_080110_FormActionResp();
        WebQueueConfiguration queueConfiguration = SpringBeanFactoryUtil.getBean(WebQueueConfiguration.class);
        List<WebQueueReceiver> receivers = queueConfiguration.getReceiver();
        if (receivers.isEmpty()) {
            response.setMessage(MessageType.DANGER, QueryNoData);
        } else {
            WebQueueReceiver receiver = receivers.stream().filter(t -> t.getProgramName().equals(formAction.getProgramName())).findFirst().orElse(null);
            if (receiver != null) {
                JmsMonitorConcurrencyRequest request = null;
                if (formAction.getAction() == Action.SET) {
                    // 純數字
                    if (StringUtils.isNumeric(formAction.getConcurrency())) {
                        request = new JmsMonitorConcurrencyRequest(formAction.getConcurrency());
                    }
                    // 符合low-high的設置
                    else if (formAction.getConcurrency().matches("\\d+-\\d+")) {
                        String[] array = formAction.getConcurrency().split("-");
                        // low和high都是數字
                        if (array.length == 2 && StringUtils.isNumeric(array[0]) && StringUtils.isNumeric(array[1])) {
                            request = new JmsMonitorConcurrencyRequest(formAction.getConcurrency());
                        }
                        // 不合法預設為重置
                        else {
                            request = new JmsMonitorConcurrencyRequest("-1");
                        }
                    }
                    // 輸入非純數字, 並且也不是low-high的設置
                    else {
                        request = new JmsMonitorConcurrencyRequest("-1");
                    }
                } else if (formAction.getAction() == Action.RESET) {
                    request = new JmsMonitorConcurrencyRequest("-1");
                }
                try {
                    JmsMonitorResponse<JmsInfoConcurrency> monitorResponse = this.sendReceive(receiver, formAction.getAction(), request);
                    if (monitorResponse != null) {
                        response.setData(monitorResponse.getData());
                        if (!monitorResponse.isResult()) {
                            LogData logData = new LogData();
                            logData.setProgramName(StringUtils.join(ProgramName, ".doAction"));
                            logData.setRemark(StringUtils.join(formAction.getAction().getDescription(), "[", receiver.getName(), "]的MQ線程數返回失敗訊息:[", monitorResponse.getErrorCode(), "]", monitorResponse.getError()));
                            sendEMS(logData);
                            if (monitorResponse.getErrorCode() == JmsMonitorResponseErrorCode.CANNOT_REDUCE_CONCURRENCY) {
                                response.setMessage(MessageType.DANGER, StringUtils.join("「", receiver.getName(), "」", formAction.getAction().getDescription(), "失敗!!!執行中的線程數大於要設定的線程數"));
                            } else if (monitorResponse.getErrorCode() == JmsMonitorResponseErrorCode.CONSUMERS_NOT_EXIST) {
                                response.setMessage(MessageType.DANGER, StringUtils.join("「", receiver.getName(), "」", formAction.getAction().getDescription(), "失敗!!!接收MQ的服務程式不存在"));
                                response.setOffline(true); // 視為服務不存在
                            }
                            // 設定值超過配置檔預設值指定的倍數
                            else if (monitorResponse.getErrorCode() == JmsMonitorResponseErrorCode.SET_CONCURRENCY_OVER_MAX_THREADS_RATIO) {
                                response.setMessage(MessageType.DANGER, StringUtils.join(String.format(Const.KEY_WORDS_IN_MESSAGE_S, receiver.getName()), monitorResponse.getError()));
                            } else {
                                response.setMessage(MessageType.DANGER, StringUtils.join("「", receiver.getName(), "」", formAction.getAction().getDescription(), "失敗!!!"));
                            }
                        } else {
                            response.setMessage(MessageType.SUCCESS, StringUtils.join("「", receiver.getName(), "」", formAction.getAction().getDescription(), "成功"));
                        }
                    } else {
                        response.setMessage(MessageType.DANGER, QueryNoData);
                    }
                } catch (Exception e) {
                    this.errorMessage(e, e.getMessage());
                    if (RestfulResultCode.CONNECTION_REFUSED.name().equals(e.getMessage())) {
                        response.setMessage(MessageType.DANGER, StringUtils.join("「", receiver.getName(), "」", formAction.getAction().getDescription(), "失敗!!!接收MQ的服務程式無法呼叫"));
                    } else if (RestfulResultCode.READ_TIMED_OUT.name().equals(e.getMessage())) {
                        response.setMessage(MessageType.DANGER, StringUtils.join("「", receiver.getName(), "」", formAction.getAction().getDescription(), "失敗!!!接收MQ的服務程式呼叫超時"));
                    } else {
                        response.setMessage(MessageType.DANGER, programError);
                    }
                    response.setOffline(true); // 視為服務不存在
                }
            } else {
                response.setMessage(MessageType.DANGER, QueryNoData);
            }
        }
        return response;
    }

    private JmsMonitorResponse<JmsInfoConcurrency> sendReceive(WebQueueReceiver receiver, Action action, JmsMonitorConcurrencyRequest request) throws Exception {
        // send Restful request
        String uri = null;
        try {
            if (action == Action.GET) {
                uri = receiver.getUri().getGetConcurrency();
            } else if (action == Action.SET || action == Action.RESET) {
                uri = receiver.getUri().getSetConcurrency();
            }
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setRequestFactory(HttpClientConfiguration.createSimpleClientHttpRequestFactory(FEPConfig.getInstance().getRestfulTimeout()));
            this.debugMessage("[", uri, "]", Const.MESSAGE_OUT, request != null ? new Gson().toJson(request) : StringUtils.EMPTY);
            String response = restTemplate.postForObject(HttpClient.toUriString(uri), request, String.class);
            this.debugMessage("[", uri, "]", Const.MESSAGE_IN, response);
            if (StringUtils.isNotBlank(response)) {
                GsonParser<JmsMonitorResponse<JmsInfoConcurrency>> gsonParser = new GsonParser<>(new TypeToken<JmsMonitorResponse<JmsInfoConcurrency>>() {}.getType());
                return gsonParser.readIn(response);
            }
        } catch (Exception e) {
            LogData logData = new LogData();
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("呼叫遠程[", receiver.getName(), "]的HTTP請求[", uri, "]出現異常"));
            sendEMS(logData);
            if (e instanceof RestClientException) {
                if (e.getCause() instanceof ConnectException) {
                    throw ExceptionUtil.createException(e, RestfulResultCode.CONNECTION_REFUSED);
                } else if (e.getCause() instanceof SocketTimeoutException) {
                    throw ExceptionUtil.createException(e, RestfulResultCode.READ_TIMED_OUT);
                }
                throw ExceptionUtil.createException(e, e.getMessage());
            }
            throw e;
        }
        return null;
    }
}