package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum LogLevel {
	Debug(0),
	Info(1),
	Warning(2),
	Error(3),
	Fatal(4);

	private int value;

	private LogLevel(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static LogLevel fromValue(int value) {
		for (LogLevel e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static LogLevel parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (LogLevel e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
