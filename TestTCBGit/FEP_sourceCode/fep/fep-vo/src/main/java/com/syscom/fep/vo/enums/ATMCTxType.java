package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum ATMCTxType {

	/**
	 * 入帳
	 * 
	 * 
	 */
	Accounting(1),
	/**
	 * 沖正Error Correct
	 * 
	 * 
	 */
	EC(2),
	/**
	 * 換日後第一筆交易
	 * 
	 * 
	 */
	ChangeDay(3),
	/**
	 * ATM庫存現金
	 * 
	 * 
	 */
	COBCash(4);

	private int value;

	private ATMCTxType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static ATMCTxType fromValue(int value) {
		for (ATMCTxType e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static ATMCTxType parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (ATMCTxType e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
