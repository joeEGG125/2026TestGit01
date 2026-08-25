package com.syscom.fep.web.controller.common;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.vo.enums.RestfulResultCode;
import com.syscom.fep.web.configurer.WebFISCConfiguration;
import com.syscom.fep.web.configurer.WebFISCConfiguration.WebFISCAgent;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.common.UI_080500_Form;
import com.syscom.fep.web.form.common.UI_080500_FormAction;
import com.syscom.fep.web.form.common.UI_080500_FormAction.Action;
import com.syscom.fep.web.form.common.UI_080500_FormAction.Channel;
import com.syscom.fep.web.form.common.UI_080500_FormAction.Target;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UI_080500Controller extends BaseController {
    private static final String URL_DO_QUERY = "/common/UI_080500/doQuery";
    private static final String URL_DO_ACTION = "/common/UI_080500/doAction";

    @Override
    public void pageOnLoad(ModelMap mode) {
        SpringBeanFactoryUtil.registerBean(WebFISCConfiguration.class);
        this.doInquiryMain(mode);
    }

    @PostMapping(value = URL_DO_QUERY)
    public String doInquiryMain(ModelMap mode) {
        this.infoMessage("開始查詢...");
        List<UI_080500_Form> list = new ArrayList<>();
        WebFISCConfiguration fiscConfiguration = SpringBeanFactoryUtil.getBean(WebFISCConfiguration.class);
        List<WebFISCAgent> agents = fiscConfiguration.getAgent();
        if (agents.isEmpty()) {
            this.showMessage(mode, MessageType.INFO, QueryNoData);
        } else {
            StringBuilder error = new StringBuilder();
            for (WebFISCAgent agent : agents) {
                UI_080500_Form form = new UI_080500_Form();
                form.setAgent(agent);
                list.add(form);
                try {
                    String response = this.sendReceive(agent, null, Action.check);
                    if (StringUtils.isNotBlank(response)) {
                        if (response.contains(RestfulResultCode.CONNECTION_REFUSED.name())) {
                            error.append("「").append(agent.getName()).append("」程式無法呼叫!! ");
                        } else if (response.contains(RestfulResultCode.READ_TIMED_OUT.name())) {
                            error.append("「").append(agent.getName()).append("」程式呼叫超時!! ");
                        } else if (response.contains(ExceptionUtil.EXCEPTION_OCCUR)) {
                            error.append("「").append(agent.getName()).append("」程式呼叫出現錯誤!!請洽資訊人員! ");
                        } else {
                            form.setAlive(true);
                            // 回應的訊息中含有primary字樣, 表示primary線路有啟動
                            if (response.contains(Channel.primary.name())) {
                                form.setPrimaryAlive(true);
                            }
                            // 回應的訊息中含有secondary字樣, 表示secondary線路有啟動
                            if (response.contains(Channel.secondary.name())) {
                                form.setSecondaryAlive(true);
                            }
                        }
                    } else {
                        error.append("「").append(agent.getName()).append("」Agent服務程式回應空白訊息!! ");
                    }
                } catch (Exception e) {
                    this.errorMessage(e, e.getMessage());
                    if (RestfulResultCode.CONNECTION_REFUSED.name().equals(e.getMessage())) {
                        error.append("「").append(agent.getName()).append("」Agent服務程式無法呼叫!! ");
                    } else if (RestfulResultCode.READ_TIMED_OUT.name().equals(e.getMessage())) {
                        error.append("「").append(agent.getName()).append("」Agent服務程式呼叫超時!! ");
                    } else {
                        error.append("「").append(agent.getName()).append("」Agent服務程式呼叫出現錯誤!!請洽資訊人員! ");
                    }
                }
            }
            if (error.length() == 0)
                this.showMessage(mode, MessageType.INFO, QuerySuccess);
            else
                this.showMessage(mode, MessageType.DANGER, error.toString());
        }
        WebUtil.putInAttribute(mode, AttributeName.List, list);
        return Router.UI_080500.getView();
    }

    @PostMapping(value = URL_DO_ACTION)
    public String doAction(@ModelAttribute UI_080500_FormAction form, ModelMap mode) {
        this.infoMessage("執行動作, 條件 = [", form.toString(), "]");
        List<UI_080500_Form> list = new ArrayList<>();
        WebFISCConfiguration fiscConfiguration = SpringBeanFactoryUtil.getBean(WebFISCConfiguration.class);
        List<WebFISCAgent> agents = fiscConfiguration.getAgent();
        String succeedMessage = null, errorMessage = null;
        if (agents.isEmpty()) {
            this.showMessage(mode, MessageType.INFO, QueryNoData);
        } else {
            WebFISCAgent agent = agents.stream().filter(t -> t.getName().equals(form.getName())).findFirst().orElse(null);
            if (agent != null) {
                try {
                    // 線路切換
                    if (form.getTarget() == Target.channel) {
                        // 若有不同FISCGW的主要及備援線路同時勾選的情況
                        // 比如FISCGW1勾主要, FISCGW2勾備援, 或FISCGW1勾備援, FISCGW2勾主要
                        // 按確定時出現警告『不可同時在不同主機啟用同一種線路!』離開不做任何處理
                        if (form.isDifferentChannel()) {
                            errorMessage = "不可同時在不同主機啟用同一種線路!";
                        } else {
                            succeedMessage = this.doSwitchChannel(agent, form);
                        }
                    }
                    // 服務切換
                    else {
                        succeedMessage = this.doSwitchService(agent, form);
                        // 這裡要sleep一下, 等待足夠的時間讓FISCGW啟動或者停止下來
                        if (fiscConfiguration.getWaitInMillisecondsAfterStartAndStop() > 0) {
                            try {
                                Thread.sleep(fiscConfiguration.getWaitInMillisecondsAfterStartAndStop());
                            } catch (InterruptedException e) {
                                this.warnMessage(e, e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    this.errorMessage(e, e.getMessage());
                    errorMessage = e.getMessage();
                }
            } else {
                errorMessage = QueryNoData;
            }
        }
        String view = this.doInquiryMain(mode);
        // 如果上面處理有成功訊息, 則要覆蓋上面doInquiryMain時的訊息
        if (StringUtils.isNotBlank(succeedMessage)) {
            this.showMessage(mode, MessageType.SUCCESS, succeedMessage);
        }
        // 如果上面處理有錯誤訊息, 則要覆蓋上面doInquiryMain時的訊息
        else if (StringUtils.isNotBlank(errorMessage)) {
            this.showMessage(mode, MessageType.DANGER, errorMessage);
        }
        return view;
    }

    /**
     * 線路切換
     *
     * @param agent
     * @param form
     * @throws Exception
     */
    private String doSwitchChannel(WebFISCAgent agent, UI_080500_FormAction form) throws Exception {
        String[] operate = new String[2];
        int index = 0;
        boolean isMyError = false;
        try {
            String response = null;
            // 只勾選主要線路, 出現警告『你確定要將FISCGWX的線路切換至Primary腳位嗎?』, 按確定後, 執行startChannel primary 以及stopChannel secondary
            if (CollectionUtils.isNotEmpty(form.getPrimary()) && form.getPrimary().contains(agent.getName())
                    && (CollectionUtils.isEmpty(form.getSecondary()) || !form.getSecondary().contains(agent.getName()))) {
                // 啟動主要線路
                operate[index] = StringUtils.join("的線路切換至主要線路", agent.getPrimaryNameSuffix());
                response = this.sendReceive(agent, Channel.primary, Action.start);
                index++;
                // 停止備援線路
                operate[index] = StringUtils.join("的備援線路", agent.getSecondaryNameSuffix(), "停止");
                this.sendReceive(agent, Channel.secondary, Action.stop);
                index++;
            }
            // 只勾選備援線路, 出現警告『你確定要將FISCGWX的線路切換至Secondary腳位嗎?』, 按確定後,執行startChannel secondary以及stopChannel primary
            else if (CollectionUtils.isNotEmpty(form.getSecondary()) && form.getSecondary().contains(agent.getName())
                    && (CollectionUtils.isEmpty(form.getPrimary()) || !form.getPrimary().contains(agent.getName()))) {
                // 啟動備援線路
                operate[index] = StringUtils.join("的線路切換至備援線路", agent.getSecondaryNameSuffix());
                response = this.sendReceive(agent, Channel.secondary, Action.start);
                index++;
                // 停止主線路
                operate[index] = StringUtils.join("的主要線路", agent.getPrimaryNameSuffix(), "停止");
                this.sendReceive(agent, Channel.primary, Action.stop);
                index++;
            }
            // 2個都勾, 出現警告『你確定要將FISCGWX同時啟用Primary及Secondary腳位嗎?』, 按確定後, 執行startChannel all
            else if (CollectionUtils.isNotEmpty(form.getPrimary()) && form.getPrimary().contains(agent.getName())
                    && CollectionUtils.isNotEmpty(form.getSecondary()) && form.getSecondary().contains(agent.getName())) {
                operate[index] = StringUtils.join("的主要線路", agent.getPrimaryNameSuffix(), "和備援線路", agent.getSecondaryNameSuffix(), "同時啟用");
                response = this.sendReceive(agent, Channel.all, Action.start);
                index++;
            }
            // 2個都不勾, 出現警告『你確定要將FISCGWX同時停用Primary及Secondary腳位嗎?』, 按確定後,執行stopChannel all
            else if ((CollectionUtils.isEmpty(form.getPrimary()) || !form.getPrimary().contains(agent.getName()))
                    && (CollectionUtils.isEmpty(form.getSecondary()) || !form.getSecondary().contains(agent.getName()))) {
                operate[index] = StringUtils.join("的主要線路", agent.getPrimaryNameSuffix(), "和備援線路", agent.getSecondaryNameSuffix(), "同時停用");
                response = this.sendReceive(agent, Channel.all, Action.stop);
                index++;
            }
            if (StringUtils.isNotBlank(response)) {
                isMyError = true;
                if (response.contains(RestfulResultCode.CONNECTION_REFUSED.name())) {
                    throw ExceptionUtil.createException(agent.getName(), operate[0], "失敗!!「", agent.getName(), "」程式無法呼叫!! ");
                } else if (response.contains(RestfulResultCode.READ_TIMED_OUT.name())) {
                    throw ExceptionUtil.createException(agent.getName(), operate[0], "失敗!!「", agent.getName(), "」程式呼叫超時!! ");
                } else if (response.contains(ExceptionUtil.EXCEPTION_OCCUR)) {
                    throw ExceptionUtil.createException(agent.getName(), operate[0], "失敗!!「", agent.getName(), "」程式呼叫出現錯誤!!請洽資訊人員! ");
                } else {
                    return StringUtils.join(agent.getName(), operate[0], "成功!! ");
                }
            } else {
                throw ExceptionUtil.createException(agent.getName(), operate[0], "結果未知!!「", agent.getName(), "」Agent服務程式回應空白訊息!! ");
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            if (isMyError) {
                throw e;
            }
            if (RestfulResultCode.CONNECTION_REFUSED.name().equals(e.getMessage())) {
                throw ExceptionUtil.createException(e, agent.getName(), operate[index], "失敗!!「", agent.getName(), "」Agent服務程式無法呼叫!! ");
            } else if (RestfulResultCode.READ_TIMED_OUT.name().equals(e.getMessage())) {
                throw ExceptionUtil.createException(e, agent.getName(), operate[index], "失敗!!「", agent.getName(), "」Agent服務程式呼叫超時!! ");
            } else {
                throw ExceptionUtil.createException(e, agent.getName(), operate[index], "失敗!!「", agent.getName(), "」Agent服務程式呼叫出現錯誤!!請洽資訊人員! ");
            }
        }
    }

    /**
     * 服務切換
     *
     * @param agent
     * @param form
     * @throws Exception
     */
    private String doSwitchService(WebFISCAgent agent, UI_080500_FormAction form) throws Exception {
        String operate = StringUtils.join(form.getAction().getDescription(), agent.getName(), "服務");
        boolean isMyError = false;
        try {
            String response = this.sendReceive(agent, null, form.getAction());
            if (StringUtils.isNotBlank(response)) {
                isMyError = true;
                if (response.contains(RestfulResultCode.CONNECTION_REFUSED.name())) {
                    throw ExceptionUtil.createException(operate, "失敗!!「", agent.getName(), "」程式無法呼叫!! ");
                } else if (response.contains(RestfulResultCode.READ_TIMED_OUT.name())) {
                    throw ExceptionUtil.createException(operate, "失敗!!「", agent.getName(), "」程式呼叫超時!! ");
                } else if (response.contains(ExceptionUtil.EXCEPTION_OCCUR)) {
                    throw ExceptionUtil.createException(operate, "失敗!!「", agent.getName(), "」程式呼叫出現錯誤!!請洽資訊人員! ");
                } else {
                    if (Const.REPLY_OK.equals(response)) {
                        return StringUtils.join(operate, "成功!! ");
                    }
                    throw ExceptionUtil.createException(operate, "失敗!!「", agent.getName(), "」Agent服務程式回應失敗!! ");
                }
            } else {
                throw ExceptionUtil.createException(agent.getName(), operate, "結果未知!!「", agent.getName(), "」Agent服務程式回應空白訊息!! ");
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            if (isMyError) {
                throw e;
            }
            if (RestfulResultCode.CONNECTION_REFUSED.name().equals(e.getMessage())) {
                throw ExceptionUtil.createException(e, operate, "失敗!!「", agent.getName(), "」Agent服務程式無法呼叫!! ");
            } else if (RestfulResultCode.READ_TIMED_OUT.name().equals(e.getMessage())) {
                throw ExceptionUtil.createException(e, operate, "失敗!!「", agent.getName(), "」Agent服務程式呼叫超時!! ");
            } else {
                throw ExceptionUtil.createException(e, operate, "失敗!!「", agent.getName(), "」Agent服務程式呼叫出現錯誤!!請洽資訊人員! ");
            }
        }
    }

    /**
     * 1. 如果action為check, 則正常情況下返回
     * <p>
     * primary FISC(B889A01I) 172.X.X.X:5001 Connected
     * primary FISC(B889A01O) 172.X.X.X:5002 Connected
     * 若有接手secondary線路(因heartbeat自動接手或曾下過start Secondary指令), 則顯示以下訊息, 若無 則不顯示
     * secondary FISC(B889A02I) 172.X.X.X:5003 Connected
     * secondary FISC(B889A02O) 172.X.X.X:5004 Connected
     *
     * @param agent
     * @param channel
     * @param action
     * @return
     * @throws Exception
     */
    private String sendReceive(WebFISCAgent agent, Channel channel, Action action) throws Exception {
        // send Restful request
        String uri = null;
        Map<String, String> args = new HashMap<>();
        args.put("operator", WebUtil.getUser().getLoginId());
        try {
            // 檢查FISCGW線路
            if (action == Action.check) {
                uri = agent.getUri().getCheck();
            }
            // 啟動/停止FISCGW服務
            else if (channel == null) {
                if (action == Action.start) {
                    uri = agent.getUri().getStart();
                } else if (action == Action.stop) {
                    uri = agent.getUri().getStop();
                }
            }
            // 啟動/停止FISCGW線路
            else {
                uri = agent.getUri().getChannel();
                args.put("mode", channel.name());
                args.put("action", action.name());
            }
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setRequestFactory(HttpClientConfiguration.createSimpleClientHttpRequestFactory(FEPConfig.getInstance().getRestfulTimeout()));
            this.debugMessage("[", uri, "]", Const.MESSAGE_OUT, args.toString());
            String response = restTemplate.postForObject(HttpClient.toUriString(uri), this.createHttpEntity(args), String.class);
            this.debugMessage("[", uri, "]", Const.MESSAGE_IN, response);
            return response;
        } catch (Exception e) {
            LogData logData = new LogData();
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("呼叫遠程[", agent.getName(), "]的HTTP請求[", uri, "]出現異常"));
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
    }

    private HttpEntity<MultiValueMap<String, Object>> createHttpEntity(Map<String, String> args) {
        // HttpHeaders
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // MultiValueMap
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> entry : args.entrySet()) {
            map.add(entry.getKey(), entry.getValue());
        }
        // HttpEntity
        return new HttpEntity<>(map, headers);
    }
}
