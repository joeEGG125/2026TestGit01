package com.syscom.fep.base.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 交易種類
 */
public enum FunctionType {
	/**
	 * 查詢
	 */
	Inquire(0),

	/**
	 * 提款
	 */
	Withdraw(1),

	/**
	 * 轉出
	 */
	TransferOut(2),

	/**
	 * 轉入
	 */
	TransferIn(3),

	/**
	 * 繳費
	 */
	Payment(4),

	/**
	 * 其他
	 */
	Other(5);

	private int value;

	private FunctionType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static FunctionType fromValue(int value) {
		for (FunctionType e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static FunctionType parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (FunctionType e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
