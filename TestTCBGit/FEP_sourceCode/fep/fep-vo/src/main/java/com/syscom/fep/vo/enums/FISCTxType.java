package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum FISCTxType {

	/**
	 * 代理
	 */
	Acquire(0),
	/**
	 * 被代理
	 */
	Issue(1),
	/**
	 * 被代理跨行轉出
	 */
	Out(2);

	private int value;

	private FISCTxType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static FISCTxType fromValue(int value) {
		for (FISCTxType e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static FISCTxType parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (FISCTxType e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
