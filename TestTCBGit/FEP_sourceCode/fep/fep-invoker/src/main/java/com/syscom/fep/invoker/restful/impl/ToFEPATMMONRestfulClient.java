package com.syscom.fep.invoker.restful.impl;

import com.syscom.fep.invoker.restful.RestfulClient;
import com.syscom.fep.vo.communication.BaseCommu;
import com.syscom.fep.vo.communication.BaseXmlCommu;
import com.syscom.fep.vo.enums.ClientType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * 發送Restful請求給FEP
 *
 * @author Richard
 */
public class ToFEPATMMONRestfulClient extends RestfulClient<BaseCommu, String> {

    public ToFEPATMMONRestfulClient(String uri) {
        super(uri);
    }

    @Override
    protected String doRequest(BaseCommu request, int timeout) {
        // RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // SimpleClientHttpRequestFactory
        if (timeout > 0) {
            restTemplate.setRequestFactory(this.getSimpleClientHttpRequestFactory(timeout));
        }
        String response = restTemplate.postForObject(this.getURI(), request.toString(), String.class);
        return response;
    }

    @Override
    protected String getURI() {
        String uri = super.getURI();
        // 如果沒有塞值，則返回預設的url
        return StringUtils.isBlank(uri) ? "http://10.0.7.6:8080/APIPublic" : uri;
    }

    @Override
    protected String getName() {
        return ClientType.TO_FEP_ATMMON.name();
    }
}