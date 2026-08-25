package com.syscom.fep.web.controller.common;

import com.google.gson.Gson;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.vo.communication.ToFEPIMSGetAllLineStatus;
import com.syscom.fep.vo.communication.ToFEPIMSGetAllLineStatus.IMSGatewayMode;
import com.syscom.fep.vo.communication.ToFEPIMSGetAllLineStatus.Line;
import com.syscom.fep.vo.communication.ToFEPIMSGetAllLineStatus.LineType;
import com.syscom.fep.vo.enums.RestfulResultCode;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.configurer.WebIMSConfiguration;
import com.syscom.fep.web.configurer.WebIMSConfiguration.WebIMSGateway;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SessionKey;
import com.syscom.fep.web.form.common.UI_080180_Form;
import com.syscom.fep.web.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 調整及監控IMSGW的線路狀態
 *
 * @author Ashiang, Richard
 */
@Controller
public class UI_080180Controller extends BaseController {
    private static final String URL_DO_QUERY = "/common/UI_080180/doQuery";
    private static final String URL_DO_CHANGE_LINE_STATUS = "/common/UI_080180/doChangeLineStatus";

    @Override
    public void pageOnLoad(ModelMap mode) {
        SpringBeanFactoryUtil.registerBean(WebIMSConfiguration.class);
        this.doInquiryMain(mode);
    }

    /**
     * 查詢
     *
     * @param mode
     * @return
     */
    @PostMapping(value = URL_DO_QUERY)
    public String doInquiryMain(ModelMap mode) {
        this.infoMessage("開始查詢...");
        return doInquiryMain(mode, true);
    }

    private String doInquiryMain(ModelMap mode, boolean needAuditLog) {
        if (needAuditLog) {
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("查詢");
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
        }
        List<UI_080180_Form> list = new ArrayList<>();
        WebIMSConfiguration imsConfiguration = SpringBeanFactoryUtil.getBean(WebIMSConfiguration.class);
        List<WebIMSGateway> gateways = imsConfiguration.getGw();
        if (gateways.isEmpty()) {
            this.showMessage(mode, MessageType.INFO, QueryNoData);
        } else {
            boolean hasSucceed = false, hasError = false;
            for (WebIMSGateway gateway : gateways) {
                try {
                    ToFEPIMSGetAllLineStatus status = this.getAllLineStatus(gateway);
                    if (status != null) {
                        gateway.setHostName(status.getHostName()); // 這裡塞入hostName便於後面更改狀態知道要送request到哪台IMSGW
                        if (list.isEmpty()) {
                            list.add(new UI_080180_Form(LineType.Primary));
                            list.add(new UI_080180_Form(LineType.Alternative));
                        }
                        list.stream().filter(t -> LineType.Primary.equals(t.getLineType())).findFirst().get().getStatus().add(this.filter(status, LineType.Primary));
                        list.stream().filter(t -> LineType.Alternative.equals(t.getLineType())).findFirst().get().getStatus().add(this.filter(status, LineType.Alternative));
                    }
                    hasSucceed = true;
                } catch (Exception e) {
                    this.errorMessage(e, "查詢出現異常, ", e.getMessage());
                    this.showMessage(mode, MessageType.DANGER, programError);
                    hasError = true;
                }
            }
            if (!hasError)
                this.showMessage(mode, MessageType.INFO, QuerySuccess);
            else if (hasSucceed)
                this.showMessage(mode, MessageType.WARNING, "部分IMSGW查詢失敗, 無法進行「變更狀態」, 請稍後再嘗試查詢");
            else
                this.showMessage(mode, MessageType.DANGER, "所有IMSGW查詢失敗, 無法進行「變更狀態」, 請稍後再嘗試查詢");
        }
        WebUtil.putInAttribute(mode, AttributeName.List, list);
        mode.put("showBtnChangeLineStatus", !list.isEmpty());
        WebUtil.putInSession(SessionKey.TemporaryRestoreData, list); // 存入session
        return Router.UI_080180.getView();
    }

    /**
     * 變更狀態
     *
     * @param mode
     * @return
     */
    @PostMapping(value = URL_DO_CHANGE_LINE_STATUS)
    public String doChangeLineStatus(ModelMap mode, HttpServletRequest request) {
        this.infoMessage("開始變更狀態...");
        String view = Router.UI_080180.getView();
        WebIMSConfiguration imsConfiguration = SpringBeanFactoryUtil.getBean(WebIMSConfiguration.class);
        List<WebIMSGateway> gateways = imsConfiguration.getGw();
        boolean gatewaysExist = true, hasError = false, hasSucceed = false, hasChanged = false;
        if (gateways.isEmpty()) {
            this.showMessage(mode, MessageType.INFO, QueryNoData);
            gatewaysExist = false;
        } else {
            List<UI_080180_Form> list = WebUtil.getFromSession(SessionKey.TemporaryRestoreData);
            if (list != null) {
                // 記錄auditLog
                AuditLog auditLog = new AuditLog();
                // 塞入auditLog.action
                auditLog.setAction("變更狀態");
                for (UI_080180_Form form : list) {
                    List<ToFEPIMSGetAllLineStatus> status = form.getStatus();
                    if (CollectionUtils.isNotEmpty(status)) {
                        for (ToFEPIMSGetAllLineStatus st : status) {
                            List<IMSGatewayMode> gatewayModes = st.getModes();
                            if (CollectionUtils.isNotEmpty(gatewayModes)) {
                                for (IMSGatewayMode gatewayMode : gatewayModes) {
                                    List<Line> lines = gatewayMode.getLines();
                                    if (CollectionUtils.isNotEmpty(lines)) {
                                        for (Line line : lines) {
                                            String value = request.getParameter(Integer.toString(hashCode(st.getHostName(), gatewayMode.getModeName(), line.getLineType(), line.getsClientId())));
                                            if (StringUtils.isNotBlank(value)) {
                                                value = value.trim();
                                                if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                                                    boolean changeStatus = Boolean.parseBoolean(value);
                                                    // 只改變有異動的
                                                    if (changeStatus != line.isEnable()) {
                                                        hasChanged = true;
                                                        Map<String, Object> param = new HashMap<>();
                                                        param.put("hostName", st.getHostName());
                                                        param.put("modeName", gatewayMode.getModeName());
                                                        param.put("lineType", line.getLineType().name());
                                                        param.put("sClientId", line.getsClientId());
                                                        param.put("enable", changeStatus);
                                                        auditLog.addParams(param);
                                                        try {
                                                            hasSucceed |= this.changeLineStatus(gateways, st.getHostName(), line.getsClientId(), changeStatus);
                                                        } catch (Exception e) {
                                                            this.errorMessage(e, "變更狀態出現異常, ", e.getMessage(), ", hostName:", st.getHostName(), ",sClientId:", line.getsClientId(), ",enable:", line.isEnable(), "->", changeStatus);
                                                            this.showMessage(mode, MessageType.DANGER, programError);
                                                            hasError = true;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // 最後塞入ThreadLocal變量中
                setAuditLog(auditLog);
            }
            if (hasChanged) {
                // 全部執行完成後, delay 1秒後再呼叫一次GetAllLineStatus重新製作表格內容
                try {
                    Thread.sleep(imsConfiguration.getWaitInMillisecondsAfterChangeLineStatus());
                } catch (InterruptedException e) {
                    warnMessage(e, e.getMessage());
                }
            }
        }
        view = this.doInquiryMain(mode, false);
        if (gatewaysExist)
            if (!hasChanged)
                this.showMessage(mode, MessageType.WARNING, "未選擇需要變更狀態的IMSGW");
            else if (!hasError)
                this.showMessage(mode, MessageType.INFO, "IMSGW變更狀態完成");
            else if (hasSucceed)
                this.showMessage(mode, MessageType.WARNING, "部分IMSGW變更狀態失敗");
            else
                this.showMessage(mode, MessageType.DANGER, "IMSGW變更狀態失敗");
        return view;
    }

    private ToFEPIMSGetAllLineStatus getAllLineStatus(WebIMSGateway gateway) throws Exception {
        String uri = gateway.getUri().getGetAllLineStatus();
        try {
            Map<String, String> args = new HashMap<>();
            args.put("operator", WebUtil.getUser().getLoginId());
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setRequestFactory(HttpClientConfiguration.createSimpleClientHttpRequestFactory(FEPConfig.getInstance().getRestfulTimeout()));
            this.debugMessage("[", uri, "]", Const.MESSAGE_OUT, args.toString());
            String response = restTemplate.postForObject(HttpClient.toUriString(uri), HttpClient.createHttpEntity(args), String.class);
            this.debugMessage("[", uri, "]", Const.MESSAGE_IN, response);
            if (StringUtils.isNotBlank(response)) {
                return new Gson().fromJson(response, ToFEPIMSGetAllLineStatus.class);
            }
        } catch (Exception e) {
            this.handleException(uri, e);
        }
        return null;
    }

    private boolean changeLineStatus(List<WebIMSGateway> gateways, String hostName, String sClientId, boolean enable) throws Exception {
        WebIMSGateway gateway = gateways.stream().filter(t -> hostName.equals(t.getHostName())).findFirst().orElse(null);
        if (gateway == null) {
            this.warnMessage("Cannot changeLineStatus, cause WebIMSGateway object which hostName was ", hostName, " not found, sClientId:", sClientId, ",enable:", enable);
            return false;
        }
        String uri = gateway.getUri().getChangeLineStatus();
        try {
            Map<String, String> args = new HashMap<>();
            args.put("operator", WebUtil.getUser().getLoginId());
            args.put("clientId", sClientId);
            args.put("enable", Boolean.toString(enable));
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setRequestFactory(HttpClientConfiguration.createSimpleClientHttpRequestFactory(FEPConfig.getInstance().getRestfulTimeout()));
            this.debugMessage("[", uri, "]", Const.MESSAGE_OUT, args.toString());
            String response = restTemplate.postForObject(HttpClient.toUriString(uri), HttpClient.createHttpEntity(args), String.class);
            this.debugMessage("[", uri, "]", Const.MESSAGE_IN, response);
            if (StringUtils.isNotBlank(response)) {
                return Boolean.parseBoolean(response);
            }
        } catch (Exception e) {
            this.handleException(uri, e);
        }
        return false;
    }

    private void handleException(String uri, Exception e) throws Exception {
        LogData logData = new LogData();
        logData.setProgramName(StringUtils.join(ProgramName, ".handleException"));
        logData.setProgramException(e);
        logData.setRemark(StringUtils.join("HTTP請求[", uri, "]出現異常"));
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

    private ToFEPIMSGetAllLineStatus filter(ToFEPIMSGetAllLineStatus status, LineType lineType) {
        ToFEPIMSGetAllLineStatus clone = ToFEPIMSGetAllLineStatus.clone(status);
        List<IMSGatewayMode> gatewayModes = clone.getModes();
        if (CollectionUtils.isNotEmpty(gatewayModes)) {
            for (IMSGatewayMode gatewayMode : gatewayModes) {
                List<Line> lines = gatewayMode.getLines();
                if (CollectionUtils.isNotEmpty(lines)) {
                    lines.removeIf(t -> !lineType.equals(t.getLineType()));
                }
            }
        }
        return clone;
    }

    /**
     * html會呼叫這個方法, 不能刪除
     *
     * @param status
     * @return
     */
    public static int getRowSpan(ToFEPIMSGetAllLineStatus status) {
        int rowSpan = 0;
        if (status != null && CollectionUtils.isNotEmpty(status.getModes())) {
            status.setShow(true);
            for (IMSGatewayMode gatewayMode : status.getModes()) {
                rowSpan += CollectionUtils.isNotEmpty(gatewayMode.getLines()) ? gatewayMode.getLines().size() : 1;
            }
        }
        return rowSpan == 0 ? 1 : rowSpan;
    }

    /**
     * html會呼叫這個方法, 不能刪除
     *
     * @param hostName
     * @param gatewayMode
     * @param lineType
     * @param sClientId
     * @return
     */
    public static int hashCode(String hostName, String gatewayMode, LineType lineType, String sClientId) {
        return StringUtils.join(new Object[] {hostName, gatewayMode, lineType, sClientId}, '-').hashCode();
    }
}
