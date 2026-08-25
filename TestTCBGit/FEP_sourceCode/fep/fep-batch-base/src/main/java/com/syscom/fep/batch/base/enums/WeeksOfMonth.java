package com.syscom.fep.batch.base.enums;

import org.apache.commons.lang3.StringUtils;

public enum WeeksOfMonth {
	FirstWeek(1),
	SecondWeek(2),
	ThirdWeek(4),
	FourthWeek(8),
	LastWeek(16),
	AllWeeks(31);

	private final int value;

	private WeeksOfMonth(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static WeeksOfMonth fromValue(int value) {
		for (WeeksOfMonth e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static WeeksOfMonth parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (WeeksOfMonth e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
