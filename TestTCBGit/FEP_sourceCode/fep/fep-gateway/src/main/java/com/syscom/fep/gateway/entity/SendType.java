package com.syscom.fep.gateway.entity;

import org.apache.commons.lang3.StringUtils;

public enum SendType {
	S(0),
	// Tx Data
	R(1),
	// Resume Tpipe
	A(2),
	// Ack
	N(3),
	// NAck
	CS(4),
	// Cancel for Sender
	CR(5);
	// Cancel for Receive

	private int value;

	private SendType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static SendType fromValue(int value) {
		for (SendType e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static SendType parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (SendType e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
