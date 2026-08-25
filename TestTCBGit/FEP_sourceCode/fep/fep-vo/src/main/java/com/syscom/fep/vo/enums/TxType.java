package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum TxType {
	/**
	 * 上行電文
	 */
	TITA(0),
	/**
	 * 下行電文
	 */
	TOTA(1);

	private int value;

	private TxType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static TxType fromValue(int value) {
		for (TxType e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static TxType parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (TxType e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
