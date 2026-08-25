package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * SendToCredit的交易類別
 */
public enum CreditTxType {

	/**
	 * 入扣帳
	 */
	Accounting(1),
	/**
	 * 沖正Error Correct
	 */
	EC(3);

	private int value;

	private CreditTxType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static CreditTxType fromValue(int value) {
		for (CreditTxType e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static CreditTxType parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (CreditTxType e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
