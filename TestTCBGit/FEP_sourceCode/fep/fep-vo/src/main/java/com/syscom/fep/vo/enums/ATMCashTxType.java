package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum ATMCashTxType {

	/**
	 * 扣帳
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
	 * 錢箱交易
	 * 
	 * 
	 */
	Box(3),
	/**
	 * 硬幣存款
	 * 
	 * 
	 */
	Coin(4),
	/**
	 * 硬幣提款
	 * 
	 * 
	 */
	CoinICW(5);

	private int value;

	private ATMCashTxType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static ATMCashTxType fromValue(int value) {
		for (ATMCashTxType e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static ATMCashTxType parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (ATMCashTxType e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
