package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 信用卡主機Message flow
 * 
 * 
 */
public enum MsgSource {
	Unknown(0),
	/**
	 * request from BSP
	 */
	RequestFromBSP(1),

	/**
	 * request from ASP
	 */
	RequestFromASP(2);

	private int value;

	private MsgSource(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static MsgSource fromValue(int value) {
		for (MsgSource e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static MsgSource parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (MsgSource e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
