package com.syscom.fep.web.entity;

import org.apache.commons.lang3.StringUtils;

public enum MessageType {
	INFO(StringUtils.EMPTY), SUCCESS("成功"), WARNING("警告"), DANGER("錯誤");

	private String description;

	private MessageType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
