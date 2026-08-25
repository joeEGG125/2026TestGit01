package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum AccountingType {

	/**
	 * 未記帳
	 * 
	 * 
	 */
	UnAccounting(0),
	/**
	 * 已記帳
	 * 
	 * 
	 */
	Accounting(1),
	/**
	 * 已更正
	 * 
	 * 
	 */
	EC(2),
	/**
	 * 更正失敗
	 * 
	 * 
	 */
	ECFail(3),
	/**
	 * 未明
	 * 
	 * 
	 */
	UnKnow(4),
	/**
	 * 待解
	 * 
	 * 
	 */
	Pending(5);

	private int value;

	private AccountingType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static AccountingType fromValue(int value) {
		for (AccountingType e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static AccountingType parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (AccountingType e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
