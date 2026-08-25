package com.syscom.fep.vo.text.fido.response;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class FidoResponse {
	
	public FidoResponse() {
		
	}
	
	public FidoResponse(String jsonString) {
		if(StringUtils.isNotBlank(jsonString)) {
			if(jsonString.startsWith("{") && jsonString.endsWith("}")) {
				try {
					JSONObject jsonObject = new JSONObject(jsonString);
					JSONObject data = null;
					
					if(jsonObject.has("code")) {
						code = jsonObject.getInt("code");
					}
					if(jsonObject.has("message")) {
						message = jsonObject.getString("message");
					}
					if(jsonObject.has("statusCode")) {
						statusCode = jsonObject.getString("statusCode");
					}
					if(jsonObject.has("data")) {
						data = jsonObject.getJSONObject("data");
						if(data != null) {
							if(data.has("qrContent")) {
								qrContent = data.getString("qrContent");
							}
							if(data.has("expireDateTime")) {
								expireDateTime = data.getString("expireDateTime");
							}
							
							if(data.has("image")) {
								image = data.getString("image");
								
							}
						}
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private Integer code;
	
	private String statusCode;
	
	private String message;
	
	private String qrContent;
	
	private String expireDateTime;
	
	private String image;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getQrContent() {
		return qrContent;
	}

	public void setQrContent(String qrContent) {
		this.qrContent = qrContent;
	}

	public String getExpireDateTime() {
		return expireDateTime;
	}

	public void setExpireDateTime(String expireDateTime) {
		this.expireDateTime = expireDateTime;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public String toString() {
		return "FidoResponse [code=" + code + ", statusCode=" + statusCode + ", message=" + message + ", qrContent="
				+ qrContent + ", expireDateTime=" + expireDateTime + ", image=" + image + "]";
	}
	
}
