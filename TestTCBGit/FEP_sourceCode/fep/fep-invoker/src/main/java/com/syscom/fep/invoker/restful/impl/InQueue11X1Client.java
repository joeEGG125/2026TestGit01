package com.syscom.fep.invoker.restful.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.syscom.fep.invoker.restful.RestfulClient;
import com.syscom.fep.vo.enums.ClientType;

/**
 * <p>發送Restful請求給FEP Service 11x1</p>
 * 
 * <ul>
 * <li>
 * 送出的是{@code String}
 * </li>
 * <li>
 * 收到的是{@code TRUE}
 * </li>
 * </ul>
 * 
 * @author Richard
 *
 */
public class InQueue11X1Client extends RestfulClient<String, String> {

	public InQueue11X1Client(String uri) {
		super(uri);
	}

	@Override
	protected String doRequest(String request, int timeout) {
		// RestTemplate
		RestTemplate restTemplate = new RestTemplate();
		// SimpleClientHttpRequestFactory
		if (timeout > 0) {
			restTemplate.setRequestFactory(this.getSimpleClientHttpRequestFactory(timeout));
		}
		String response = restTemplate.postForObject(this.getURI(), request, String.class);
		return response;
	}

	@Override
	protected String getURI() {
		String uri = super.getURI();
		// 如果沒有塞值，則返回預設的url
		return StringUtils.isBlank(uri) ? "http://localhost:8082/inqueue/11X1" : uri;
	}

	@Override
	protected String getName() {
		return ClientType.InQueue11X1.name();
	}
}