package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum CurrencyType {

	TWD(0),
	USD(1),
	MOP(30),
	HKD(3),
	JPY(8),
	EUR(22),
	OTH(99),
	CNY(28);

	private int code;

	private CurrencyType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static CurrencyType fromCode(int code) {
		for (CurrencyType e : values()) {
			if (e.getCode() == code) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid code = [" + code + "]!!!");
	}

	public static CurrencyType parse(Object nameOrCode) {
		if (nameOrCode instanceof Number) {
			return fromCode(((Number) nameOrCode).intValue());
		} else if (nameOrCode instanceof String) {
			String nameOrCodeStr = (String) nameOrCode;
			if (StringUtils.isNumeric(nameOrCodeStr)) {
				return fromCode(Integer.parseInt(nameOrCodeStr));
			}
			for (CurrencyType e : values()) {
				if (e.name().equalsIgnoreCase(nameOrCodeStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or code = [" + nameOrCode + "]!!!");
	}
}
