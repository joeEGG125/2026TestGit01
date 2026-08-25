package com.syscom.fep.batch.base.enums;

import org.apache.commons.lang3.StringUtils;

public enum MonthOfYear {
	January(1),
	February(2),
	March(4),
	April(8),
	May(16),
	June(32),
	July(64),
	August(128),
	September(256),
	October(512),
	November(1024),
	December(2048),
	AllMonths(4095);

	private final int value;

	private MonthOfYear(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static MonthOfYear fromValue(int value) {
		for (MonthOfYear e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static MonthOfYear parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (MonthOfYear e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
