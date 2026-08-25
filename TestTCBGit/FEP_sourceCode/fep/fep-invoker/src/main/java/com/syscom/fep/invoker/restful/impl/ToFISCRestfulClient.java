package com.syscom.fep.invoker.restful.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.syscom.fep.invoker.restful.RestfulClient;
import com.syscom.fep.vo.communication.ToFISCCommu;
import com.syscom.fep.vo.enums.ClientType;

/**
 * <p>發送Restful請求給FISC GW</p>
 * 
 * <ul>
 * <li>
 * 送出的是{@code ToFISCCommu}對應的XML字串
 * </li>
 * <li>
 * 收到的是{@code HEX String}
 * </li>
 * </ul>
 * 
 * @author Richard
 *
 */
public class ToFISCRestfulClient extends RestfulClient<ToFISCCommu, String> {

	public ToFISCRestfulClient(String uri) {
		super(uri);
	}

	@Override
	protected String doRequest(ToFISCCommu request, int timeout) {
		// HttpHeaders
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		// MultiValueMap
		MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
		postParameters.add("ej", request.getEj());
		postParameters.add("message", request.getMessage());
		postParameters.add("messageId", request.getMessageId());
		postParameters.add("stan", request.getStan());
		postParameters.add("timeout", request.getTimeout());
		postParameters.add("txRquid", request.getTxRquid());
		// HttpEntity
		HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(postParameters, headers);
		// RestTemplate
		RestTemplate restTemplate = new RestTemplate();
		// SimpleClientHttpRequestFactory
		if (timeout > 0) {
			restTemplate.setRequestFactory(this.getSimpleClientHttpRequestFactory(timeout));
		}
		String response = restTemplate.postForObject(this.getURI(), httpEntity, String.class);
		if (response.contains("\"")) {
			// 因返回為Json格式所以會在內容前後加雙引號, 你收到內容後看是要用replace或用Json Parse後再取出值即可
			response = StringUtils.replace(response, "\"", StringUtils.EMPTY);
		}
		return response;
	}

	@Override
	protected String getURI() {
		String uri = super.getURI();
		// 如果db中沒有設置，則返回預設的測試url
		return StringUtils.isBlank(uri) ? "http://192.168.0.30:8912/api/VFISC/SendReceive" : uri;
	}

	@Override
	protected String getName() {
		return ClientType.TO_FISC_GW.name();
	}
}
