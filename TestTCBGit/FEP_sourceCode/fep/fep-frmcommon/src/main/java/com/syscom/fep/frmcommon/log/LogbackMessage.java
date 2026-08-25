package com.syscom.fep.frmcommon.log;

import com.google.gson.Gson;

/**
 * 
 * @author Richard
 *
 * @param <MDC>
 */
public class LogbackMessage<MDC> {
	private String timestamp;
	private String level;
	private String thread;
	private String logger;
	private String message;
	private String context;
	private MDC mdc;

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getThread() {
		return thread;
	}

	public void setThread(String thread) {
		this.thread = thread;
	}

	public String getLogger() {
		return logger;
	}

	public void setLogger(String logger) {
		this.logger = logger;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public MDC getMdc() {
		return mdc;
	}

	public void setMdc(MDC mdc) {
		this.mdc = mdc;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}
