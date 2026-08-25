package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 帳務別
 */
public enum ATMCBSMode {
	/**
	 * 日間連線
	 * 
	 * 日間連線
	 */
	DateTimeConnection(1),
	/**
	 * Half-online
	 * 
	 * Half-online
	 */
	HalfOnline(2),
	/**
	 * 夜間連線
	 * 
	 * 夜間連線
	 */
	NightConnection(3),
	/**
	 * 追帳Reentry
	 * 
	 * 追帳Reentry
	 */
	Reentry(4),
	/**
	 * Go to Online
	 * 
	 * Go to Online
	 */
	GoToOnline(5);

	private int value;

	private ATMCBSMode(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static ATMCBSMode fromValue(int value) {
		for (ATMCBSMode e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static ATMCBSMode parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (ATMCBSMode e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
