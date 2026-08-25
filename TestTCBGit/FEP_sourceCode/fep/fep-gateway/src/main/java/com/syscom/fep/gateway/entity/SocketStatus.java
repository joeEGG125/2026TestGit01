package com.syscom.fep.gateway.entity;

import org.apache.commons.lang3.StringUtils;

public enum SocketStatus {
	Close(0),
	Open(3);

	private int value;

	private SocketStatus(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static SocketStatus fromValue(int value) {
		for (SocketStatus e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static SocketStatus parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (SocketStatus e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
