package com.syscom.fep.batch.base.vo.restful;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

public class BatchBaseRestful implements Serializable {
	private static final long serialVersionUID = 1L;

	@XStreamAlias("Operator")
	private String operator;
	@XStreamAlias("Result")
	private boolean result = true;
	@XStreamAlias("Message")
	private String message;

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
