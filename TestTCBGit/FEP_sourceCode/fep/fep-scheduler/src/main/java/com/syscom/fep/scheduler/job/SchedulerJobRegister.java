package com.syscom.fep.scheduler.job;

import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.validation.annotation.Validated;

// @Validated
public class SchedulerJobRegister {
	@NotNull
	private String className;
	@NotNull
	private String configClassName;
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getConfigClassName() {
		return configClassName;
	}

	public void setConfigClassName(String configClassName) {
		this.configClassName = configClassName;
	}

	@Override
	public String toString(){
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SIMPLE_STYLE);
	}
}
