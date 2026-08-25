package com.syscom.fep.web.controller.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.communication.BaseResponse;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.frmcommon.netty.NettyEventExecutorData;
import com.syscom.fep.frmcommon.netty.NettyEventExecutorDataErrorCode;
import com.syscom.fep.frmcommon.netty.NettyEventExecutorRequest;
import com.syscom.fep.frmcommon.parse.GsonParser;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.vo.enums.RestfulResultCode;
import com.syscom.fep.web.configurer.WebSimpleNettyConfiguration;
import com.syscom.fep.web.configurer.WebSimpleNettyConfiguration.WebSimpleNettyServer;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.common.UI_080120_Form;
import com.syscom.fep.web.form.common.UI_080120_FormAction;
import com.syscom.fep.web.form.common.UI_080120_FormAction.Action;
import com.syscom.fep.web.form.common.UI_080120_FormActionResp;
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
public class UI_080120Controller extends BaseController {
    private static final String URL_DO_QUERY = "/common/UI_080120/doQuery";
    private static final String URL_DO_ACTION = "/common/UI_080120/doAction";
    private static final int COLUMN_LIMIT = 3;

    @Override
    public void pageOnLoad(ModelMap mode) {
        SpringBeanFactoryUtil.registerBean(WebSimpleNettyConfiguration.class);
        this.doInquiryMain(mode);
    }

    @PostMapping(value = URL_DO_QUERY)
    public String doInquiryMain(ModelMap mode) {
        this.infoMessage("開始查詢...");
        List<List<UI_080120_Form>> row = new ArrayList<>();
        WebSimpleNettyConfiguration simpleNettyConfiguration = SpringBeanFactoryUtil.getBean(WebSimpleNettyConfiguration.class);
        List<WebSimpleNettyServer> servers = simpleNettyConfiguration.getServer();
        if (servers.isEmpty()) {
            this.showMessage(mode, MessageType.INFO, QueryNoData);
        } else {
            boolean hasSucceed = false, hasError = false, offline = true;
            for (int i = 0; i < servers.size(); i++) {
                List<UI_080120_Form> column = null;
                if (i % COLUMN_LIMIT == 0) {
                    row.add(new ArrayList<>());
                }
                // server
                WebSimpleNettyServer server = servers.get(i);
                // eventExecutorData
                NettyEventExecutorData eventExecutorData = null;
                // reset offline
                offline = true;
                try {
                    BaseResponse<NettyEventExecutorData, NettyEventExecutorDataErrorCode> response = this.sendReceive(server, Action.GET, null);
                    if (response != null) {
                        if (response.isResult()) {
                            eventExecutorData = response.getData();
                            offline = false;
                        } else {
                            LogData logData = new LogData();
                            logData.setProgramName(StringUtils.join(ProgramName, ".doInquiryMain"));
                            logData.setRemark(StringUtils.join("查詢[", server.getName(), "]的線程數返回失敗訊息:[", response.getErrorCode(), "]", response.getError()));
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
                if (eventExecutorData == null) {
                    eventExecutorData = new NettyEventExecutorData();
                }
                UI_080120_Form form = new UI_080120_Form();
                form.setServer(server);
                form.setEventExecutorData(eventExecutorData);
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
        return Router.UI_080120.getView();
    }

    @PostMapping(value = URL_DO_ACTION)
    @ResponseBody
    public UI_080120_FormActionResp doAction(@RequestBody UI_080120_FormAction formAction) {
        this.infoMessage("執行動作, 條件 = [", formAction.toString(), "]");
        UI_080120_FormActionResp response = new UI_080120_FormActionResp();
        WebSimpleNettyConfiguration queueConfiguration = SpringBeanFactoryUtil.getBean(WebSimpleNettyConfiguration.class);
        List<WebSimpleNettyServer> servers = queueConfiguration.getServer();
        if (servers.isEmpty()) {
            response.setMessage(MessageType.DANGER, QueryNoData);
        } else {
            WebSimpleNettyServer server = servers.stream().filter(t -> t.getProgramName().equals(formAction.getProgramName())).findFirst().orElse(null);
            if (server != null) {
                NettyEventExecutorRequest request = null;
                if (formAction.getAction() == Action.SET) {
                    try {
                        request = new NettyEventExecutorRequest(Integer.parseInt(formAction.getnThreads()));
                    } catch (NumberFormatException e) {
                        this.warnMessage(e, e.getMessage());
                        request = new NettyEventExecutorRequest(0);
                    }
                } else if (formAction.getAction() == Action.RESET) {
                    request = new NettyEventExecutorRequest(0);
                }
                try {
                    BaseResponse<NettyEventExecutorData, NettyEventExecutorDataErrorCode> resp = this.sendReceive(server, formAction.getAction(), request);
                    if (resp != null) {
                        response.setData(resp.getData());
                        if (!resp.isResult()) {
                            LogData logData = new LogData();
                            logData.setProgramName(StringUtils.join(ProgramName, ".doAction"));
                            logData.setRemark(StringUtils.join(formAction.getAction().getDescription(), "[", server.getName(), "]的線程數返回失敗訊息:[", resp.getErrorCode(), "]", resp.getError()));
                            sendEMS(logData);
                            if (resp.getErrorCode() == NettyEventExecutorDataErrorCode.HANDLER_NOT_EXIST) {
                                response.setMessage(MessageType.DANGER, StringUtils.join("「", server.getName(), "」", formAction.getAction().getDescription(), "失敗!!!服務程式不存在"));
                                response.setOffline(true); // 視為服務不存在
                            }
                            // 設定值超過配置檔預設值指定的倍數
                            else if (resp.getErrorCode() == NettyEventExecutorDataErrorCode.SET_EVENT_EXECUTOR_THREADS_OVER_MAX_THREADS_RATIO) {
                                response.setMessage(MessageType.DANGER, StringUtils.join(String.format(Const.KEY_WORDS_IN_MESSAGE_S, server.getName()), resp.getError()));
                            } else {
                                response.setMessage(MessageType.DANGER, StringUtils.join("「", server.getName(), "」", formAction.getAction().getDescription(), "失敗!!!"));
                            }
                        } else {
                            response.setMessage(MessageType.SUCCESS, StringUtils.join("「", server.getName(), "」", formAction.getAction().getDescription(), "成功"));
                        }
                    } else {
                        response.setMessage(MessageType.DANGER, QueryNoData);
                    }
                } catch (Exception e) {
                    this.errorMessage(e, e.getMessage());
                    if (RestfulResultCode.CONNECTION_REFUSED.name().equals(e.getMessage())) {
                        response.setMessage(MessageType.DANGER, StringUtils.join("「", server.getName(), "」", formAction.getAction().getDescription(), "失敗!!!服務程式無法呼叫"));
                    } else if (RestfulResultCode.READ_TIMED_OUT.name().equals(e.getMessage())) {
                        response.setMessage(MessageType.DANGER, StringUtils.join("「", server.getName(), "」", formAction.getAction().getDescription(), "失敗!!!服務程式呼叫超時"));
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

    private BaseResponse<NettyEventExecutorData, NettyEventExecutorDataErrorCode> sendReceive(WebSimpleNettyServer server, Action action, NettyEventExecutorRequest request) throws Exception {
        // send Restful request
        String uri = null;
        try {
            if (action == Action.GET) {
                uri = server.getUri().getGetEventExecutorData();
            } else if (action == Action.SET || action == Action.RESET) {
                uri = server.getUri().getSetEventExecutorThreads();
            }
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setRequestFactory(HttpClientConfiguration.createSimpleClientHttpRequestFactory(FEPConfig.getInstance().getRestfulTimeout()));
            this.debugMessage("[", uri, "]", Const.MESSAGE_OUT, request != null ? new Gson().toJson(request) : StringUtils.EMPTY);
            String response = restTemplate.postForObject(HttpClient.toUriString(uri), request, String.class);
            this.debugMessage("[", uri, "]", Const.MESSAGE_IN, response);
            if (StringUtils.isNotBlank(response)) {
                GsonParser<BaseResponse<NettyEventExecutorData, NettyEventExecutorDataErrorCode>> gsonParser = new GsonParser<>(new TypeToken<BaseResponse<NettyEventExecutorData, NettyEventExecutorDataErrorCode>>() {}.getType());
                return gsonParser.readIn(response);
            }
        } catch (Exception e) {
            LogData logData = new LogData();
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("呼叫遠程[", server.getName(), "]的HTTP請求[", uri, "]出現異常"));
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
