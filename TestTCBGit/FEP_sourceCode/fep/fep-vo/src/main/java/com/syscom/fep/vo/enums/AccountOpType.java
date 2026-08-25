package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum AccountOpType {
	/**
	 * 扣帳
	 * 
	 * 
	 */
	Withdraw(1),
	/**
	 * 入帳
	 * 
	 * 
	 */
	Deposit(2);

	private int value;

	private AccountOpType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static AccountOpType fromValue(int value) {
		for (AccountOpType e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static AccountOpType parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (AccountOpType e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
