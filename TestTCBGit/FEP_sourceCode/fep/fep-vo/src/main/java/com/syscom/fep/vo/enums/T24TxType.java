package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum T24TxType {

	/**
	 * 查詢
	 */
	Query(0),
	/**
	 * 入扣帳
	 */
	Accounting(1),
	/**
	 * 沖正
	 */
	EC(2),
	/**
	 * 授權
	 */
	Authorized(3),
	/**
	 * 解圈
	 */
	UnLock(4);

	private int value;

	private T24TxType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static T24TxType fromValue(int value) {
		for (T24TxType e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static T24TxType parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (T24TxType e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
