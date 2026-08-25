package com.syscom.fep.invoker.restful;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;

/**
 * @author Richard
 */
public abstract class RestfulClient<Request, Response> {
	protected LogHelper logHelper = LogHelperFactory.getRestfulClientLogger();
	private String uri;

	public RestfulClient(String uri) {
		this.uri = uri;
	}

	public Response sendReceive(Request request, int timeout) throws Exception {
		try {
			logHelper.info("[", this.getName(), "][", this.getURI(), "]", Const.MESSAGE_OUT, request);
			Response response = this.doRequest(request, timeout);
			logHelper.info("[", this.getName(), "][", this.getURI(), "]", Const.MESSAGE_IN, response);
			return response;
		} catch (RestClientException e) {
			throw e;
		}
	}

	protected String getURI() {
		return this.uri;
	}

	protected abstract Response doRequest(Request request, int timeout);

	protected abstract String getName();

	protected SimpleClientHttpRequestFactory getSimpleClientHttpRequestFactory(int timeout) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(timeout);
		requestFactory.setReadTimeout(timeout);
		return requestFactory;
	}
}
