package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum ATMType {

	/**
	 * 提款機
	 */
	ATM(0),
	/**
	 * 外幣提款機
	 */
	FATM(1),
	/**
	 * 存提款機
	 */
	ADM(2),
	/**
	 * WebATM ATM
	 */
	WebATM(3);

	private int value;

	private ATMType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static ATMType fromValue(int value) {
		for (ATMType e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static ATMType parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (ATMType e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
