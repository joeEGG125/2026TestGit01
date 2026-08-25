package com.syscom.fep.service.svr.restful;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.frmcommon.log.LogMDC;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.common.log.LogHelperFactory;

public class InQueue11X1Controller {
	private static final String CLASS_NAME = InQueue11X1Controller.class.getSimpleName();
	private List<String> messageInList = Collections.synchronizedList(new ArrayList<String>());

	@RequestMapping(value = "/inqueue/11X1", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String onMessage(@RequestBody String messageIn) {
		LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_11X1);
		LogHelperFactory.getTraceLogger().info("[", CLASS_NAME, "][onMessage]", Const.MESSAGE_IN, messageIn);
		messageInList.add(messageIn);
		return Boolean.TRUE.toString();
	}

	public String[] getAllMessages() {
		LogHelperFactory.getTraceLogger().info("[", CLASS_NAME, "][getAllMessages]message size = [", messageInList.size(), "]");
		String[] mesages = new String[messageInList.size()];
		messageInList.toArray(mesages);
		messageInList.clear();
		return mesages;
	}
}
