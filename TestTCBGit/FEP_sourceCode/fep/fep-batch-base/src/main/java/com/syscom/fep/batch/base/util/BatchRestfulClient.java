package com.syscom.fep.batch.base.util;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.batch.base.configurer.BatchBaseConfiguration;
import com.syscom.fep.batch.base.configurer.BatchBaseConfigurationHost;
import com.syscom.fep.batch.base.configurer.BatchBaseConstant;
import com.syscom.fep.batch.base.vo.restful.BatchBaseRestful;
import com.syscom.fep.batch.base.vo.restful.BatchScheduler;
import com.syscom.fep.batch.base.vo.restful.request.ListSchedulerRequest;
import com.syscom.fep.batch.base.vo.restful.request.OperateSchedulerRequest;
import com.syscom.fep.batch.base.vo.restful.response.ListSchedulerResponse;
import com.syscom.fep.batch.base.vo.restful.response.OperateSchedulerResponse;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.owasp.esapi.ESAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = BatchBaseConstant.CONFIGURATION_PROPERTIES_PREFIX, name = {BatchBaseConstant.CONFIGURATION_PROPERTIES_HOST_0_NAME})
@Lazy
public class BatchRestfulClient extends FEPBase {
    private static final LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private FEPConfig fepConfig;
    @Autowired
    private BatchBaseConfiguration batchBaseConfiguration;

    public ListSchedulerResponse listScheduler(ListSchedulerRequest request) {
        ListSchedulerResponse rtn = new ListSchedulerResponse();
        rtn.setBatchSchedulerList(new ArrayList<>());
        String url = null;
        for (BatchBaseConfigurationHost host : batchBaseConfiguration.getHost()) {
            ListSchedulerRequest r = this.createRequest(request, host);
            if (r == null) continue; // 為null說明不需要查詢這個主機
            url = StringUtils.join(host.getUrl(), "/listScheduler");
            ListSchedulerResponse response = this.sendReceive(host.getName(), url, r, ListSchedulerResponse.class);
            this.merge(rtn, response);
        }
        this.sort(rtn.getBatchSchedulerList());
        return rtn;
    }

    /**
     * 只創建對應主機的請求物件
     *
     * @param request
     * @param host
     * @return
     */
    private ListSchedulerRequest createRequest(ListSchedulerRequest request, BatchBaseConfigurationHost host) {
        List<BatchScheduler> batchSchedulerList =
                request.getBatchSchedulerList().stream().filter(
                        t -> StringUtils.isBlank(t.getHostName()) || host.getName().equals(t.getHostName())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(batchSchedulerList)) {
            return null;
        }
        ListSchedulerRequest r = new ListSchedulerRequest();
        r.setOperator(request.getOperator());
        r.setBatchSchedulerList(batchSchedulerList);
        return r;
    }

    /**
     * 將主機返回的物件進行merge
     *
     * @param rtn
     * @param response
     */
    private void merge(ListSchedulerResponse rtn, ListSchedulerResponse response) {
        if (response == null) {
            return;
        }
        if (CollectionUtils.isNotEmpty(response.getBatchSchedulerList())) {
            rtn.getBatchSchedulerList().addAll(response.getBatchSchedulerList());
        }
        if (!response.isResult()) {
            rtn.setResult(response.isResult());
            if (StringUtils.isNotBlank(rtn.getMessage())) {
                rtn.setMessage(StringUtils.join(rtn.getMessage(), "\r\n"));
            }
            rtn.setMessage(StringUtils.join(rtn.getMessage(), response.getMessage()));
        }
        rtn.setOperator(response.getOperator());
    }

    public OperateSchedulerResponse operateScheduler(OperateSchedulerRequest request) {
        OperateSchedulerResponse rtn = new OperateSchedulerResponse();
        rtn.setBatchSchedulerList(new ArrayList<>());
        String url = null;
        for (BatchBaseConfigurationHost host : batchBaseConfiguration.getHost()) {
            OperateSchedulerRequest r = this.createRequest(request, host);
            if (r == null) continue; // 為null說明不需要針對這個主機做動作
            url = StringUtils.join(host.getUrl(), "/operateScheduler");
            OperateSchedulerResponse response = this.sendReceive(host.getName(), url, r, OperateSchedulerResponse.class);
            this.merge(rtn, response);
        }
        this.sort(rtn.getBatchSchedulerList());
        return rtn;
    }

    /**
     * 只創建對應主機的請求物件
     *
     * @param request
     * @param host
     * @return
     */
    private OperateSchedulerRequest createRequest(OperateSchedulerRequest request, BatchBaseConfigurationHost host) {
        List<BatchScheduler> batchSchedulerList =
                request.getBatchSchedulerList().stream().filter(
                        t -> StringUtils.isBlank(t.getHostName()) || host.getName().equals(t.getHostName())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(batchSchedulerList)) {
            return null;
        }
        OperateSchedulerRequest r = new OperateSchedulerRequest();
        r.setOperator(request.getOperator());
        r.setAction(request.getAction());
        r.setBatchSchedulerList(batchSchedulerList);
        return r;
    }

    /**
     * 將主機返回的物件進行merge
     *
     * @param rtn
     * @param response
     */
    private void merge(OperateSchedulerResponse rtn, OperateSchedulerResponse response) {
        if (response == null) {
            return;
        }
        if (CollectionUtils.isNotEmpty(response.getBatchSchedulerList())) {
            rtn.getBatchSchedulerList().addAll(response.getBatchSchedulerList());
        }
        if (!response.isResult()) {
            rtn.setResult(response.isResult());
            if (StringUtils.isNotBlank(rtn.getMessage())) {
                rtn.setMessage(StringUtils.join(rtn.getMessage(), "\r\n"));
            }
            rtn.setMessage(StringUtils.join(rtn.getMessage(), response.getMessage()));
        }
        rtn.setOperator(response.getOperator());
    }

    private void sort(List<BatchScheduler> batchSchedulerList) {
        Collections.sort(batchSchedulerList, (o1, o2) -> {
            int result = o1.getBatchId().compareTo(o2.getBatchId());
            if (result == 0) {
                return o1.getHostName().compareTo(o2.getHostName());
            }
            return result;
        });
    }

    private <Request, Response extends BatchBaseRestful> Response sendReceive(String hostName, String url, Request request, Class<Response> responseCls) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            // SimpleClientHttpRequestFactory
            if (fepConfig.getRestfulTimeout() > 0) {
                restTemplate.setRequestFactory(HttpClientConfiguration.createSimpleClientHttpRequestFactory(fepConfig.getRestfulTimeout()));
            }
            String messageOut = serializeToXml(request);
            logger.info("[", BatchRestfulClient.class.getSimpleName(), "][", url, "]", Const.MESSAGE_OUT, messageOut);
            messageOut = ESAPI.encoder().encodeForHTML(messageOut); // 21023-09-18 Richard add 這裡要轉義一下
            String messageIn = restTemplate.postForObject(url, messageOut, String.class);
            messageIn = ESAPI.encoder().decodeForHTML(messageIn); // 21023-09-18 Richard add 這裡要轉義一下
            logger.info("[", BatchRestfulClient.class.getSimpleName(), "][", url, "]", Const.MESSAGE_IN, messageIn);
            return deserializeFromXml(messageIn, responseCls);
        } catch (RestClientException e) {
            try {
                Response response = responseCls.getDeclaredConstructor().newInstance();
                response.setResult(false);
                if (e.getCause() instanceof ConnectException) {
                    response.setMessage(StringUtils.join("Batch Control Service [", hostName, "] is unreachable"));
                } else if (e.getCause() instanceof SocketTimeoutException) {
                    response.setMessage(StringUtils.join("Call Batch Control Service [", hostName, "] timeout"));
                } else {
                    response.setMessage(StringUtils.join("Call Batch Control Service [", hostName, "] exception occur, ", e.getMessage()));
                }
                logger.exceptionMsg(e, response.getMessage());
                return response;
            } catch (Exception ex) {
                logger.warn(ex, ex.getMessage());
            }
        }
        return null;
    }
}
