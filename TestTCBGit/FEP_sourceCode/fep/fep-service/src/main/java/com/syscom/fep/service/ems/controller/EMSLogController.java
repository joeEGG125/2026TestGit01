package com.syscom.fep.service.ems.controller;

/**
 * 接收其他服務透過logback api記錄的FEP Message
 *
 * 這支程式不需要了, 改用{@link com.syscom.fep.service.ems.queue.EMSLogReceiver}
 * 
 * @author Richard
 *
 */
public class EMSLogController {
//	private static final LogHelper SERVICELOGGER = LogHelperFactory.getServiceLogger();
//
//	private EMSLogMessageParser parser;
//
//	@PostConstruct
//	public void registerParser() {
//		parser = SpringBeanFactoryUtil.registerBean(EMSLogMessageParser.class);
//	}
//
//	@RequestMapping(value = "/api/EMS/SendMessage")
//	@ResponseBody
//	public String sendMessage(HttpServletRequest request) {
//		LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_EMS);
//		List<String> jsonStrList = new ArrayList<>();
//		try (BufferedReader bufferedReader = request.getReader()) {
//			String readLine;
//			while ((readLine = bufferedReader.readLine()) != null) {
//				jsonStrList.add(readLine);
//			}
//		} catch (Exception e) {
//			SERVICELOGGER.exceptionMsg(e, e.getMessage());
//		}
//		if (!jsonStrList.isEmpty()) {
//			List<EMSLogMessage> emsLogMessageList = new ArrayList<>();
//			Gson gson = new Gson();
//			for (String jsonStr : jsonStrList) {
//				try {
//					EMSLogMessage emsLogMessage = gson.fromJson(jsonStr, EMSLogMessage.class);
//					SERVICELOGGER.debug("EMS Web service Message Type:", emsLogMessage.getMessageType(), ", MessageTarget=", emsLogMessage.getMessageTarget(), ",message=", emsLogMessage.toString());
//					emsLogMessageList.add(emsLogMessage);
//				} catch (Exception e) {
//					SERVICELOGGER.exceptionMsg(e, "Parse JSON failed, json str = [", jsonStr, "]");
//				}
//			}
//			try {
//				parser.parseLog(emsLogMessageList);
//			} catch (Exception e) {
//				SERVICELOGGER.exceptionMsg(e, e.getMessage());
//			}
//		}
//		return "OK";
//	}
}
