package com.syscom.fep.service.svr;

import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

// @Validated
public class SvrProcessorRegister {
	@NotNull
	private String mainClassName;
	private String subClassName;

	public String getMainClassName() {
		return mainClassName;
	}

	public void setMainClassName(String mainClassName) {
		this.mainClassName = mainClassName;
	}

	public String getSubClassName() {
		return subClassName;
	}

	public void setSubClassName(String subClassName) {
		this.subClassName = subClassName;
	}
}
