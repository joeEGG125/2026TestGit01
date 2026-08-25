package com.syscom.fep.enchelper.enums;

import org.apache.commons.lang3.StringUtils;

public enum ENCKeyType {
	None(0),
	S1(1),
	T2(2),
	T3(3);

	private int value;

	private ENCKeyType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static ENCKeyType fromValue(int value) {
		for (ENCKeyType e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static ENCKeyType parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (ENCKeyType e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
