package com.syscom.fep.gateway.entity;

import org.apache.commons.lang3.StringUtils;

public enum SocketType {
	Sender(0), 
	Receiver(1),
	Client(98),
	Server(99);

	private int value;

	private SocketType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static SocketType fromValue(int value) {
		for (SocketType e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static SocketType parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (SocketType e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
