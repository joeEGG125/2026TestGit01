package com.syscom.fep.gateway.entity;

import org.apache.commons.lang3.StringUtils;

public enum InitKeyStatus {
	Initial(0),
	Done(2);

	private int value;

	private InitKeyStatus(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static InitKeyStatus fromValue(int value) {
		for (InitKeyStatus e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static InitKeyStatus parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (InitKeyStatus e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
