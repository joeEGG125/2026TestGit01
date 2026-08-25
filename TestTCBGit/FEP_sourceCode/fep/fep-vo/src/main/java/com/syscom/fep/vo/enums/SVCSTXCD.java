package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum SVCSTXCD {
	Unknown(0),
	SD1(1),
	SD2(2),
	SD3(3),
	SD4(4),
	SD5(5),
	SD6(6),
	SD7(7);
	private int value;

	private SVCSTXCD(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static SVCSTXCD fromValue(int value) {
		for (SVCSTXCD e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static SVCSTXCD parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (SVCSTXCD e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
