package com.syscom.fep.vo.text.app.response;

import com.syscom.fep.vo.text.app.APPGeneral;

public class ReplyDeviceInvoiceResponse {
	/**
	 * 載具條碼
	 * 
	 * <remark></remark>
	 */
	private String deviceInvoice;

	private String getDeviceInvoice() {
		return deviceInvoice;
	}

	private void setDeviceInvoice(String value) {
		deviceInvoice = value;
	}

	/**
	 * 呼叫狀態
	 * 
	 * <remark></remark>
	 */
	private String success;

	private String getSuccess() {
		return success;
	}

	private void setSuccess(String value) {
		success = value;
	}

	/**
	 * 錯誤代碼
	 * 
	 * <remark></remark>
	 */
	private String retuenCode;

	private String getRetuenCode() {
		return retuenCode;
	}

	private void setRetuenCode(String value) {
		retuenCode = value;
	}

	/**
	 * 錯誤訊息
	 * 
	 * <remark></remark>
	 */
	private String message;

	private String getMessage() {
		return message;
	}

	private void setMessage(String value) {
		message = value;
	}

	public void parseFlatfile(String flatfile, APPGeneral general) {
		if (flatfile.contains("[")) {
			flatfile = flatfile.substring(flatfile.indexOf("[") + 1, flatfile.indexOf("[") + 1 + flatfile.indexOf("]") - flatfile.indexOf("[") - 1);
		}
		for (String str : flatfile.replace("{", ",").replace("}", "").split("[,]", -1)) {
			String[] strs = str.split("[:]", -1);
			if (strs.length == 2) {
				if (strs[0].replace("\"", "").equals("DeviceInvoice")) {
					general.getmResponse().setDeviceInvoice(strs[1].replace("\"", ""));
				} else if (strs[0].replace("\"", "").equals("Success")) {
					general.getmResponse().setSuccess(strs[1].replace("\"", ""));
				} else if (strs[0].replace("\"", "").equals("RetuenCode")) {
					general.getmResponse().setRetuenCode(strs[1].replace("\"", ""));
				} else if (strs[0].replace("\"", "").equals("Message")) {
					general.getmResponse().setMessage(strs[1].replace("\"", ""));
				}
			}
		}
	}
}
