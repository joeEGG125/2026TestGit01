package com.syscom.fep.batch.base.enums;

import org.apache.commons.lang3.StringUtils;

public enum BatchResult {
	Running(0),
	Successful(1),
	Failed(2),
	PartialFailed(3);

	private final int value;

	private BatchResult(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static BatchResult fromValue(int value) {
		for (BatchResult e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static BatchResult parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (BatchResult e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
