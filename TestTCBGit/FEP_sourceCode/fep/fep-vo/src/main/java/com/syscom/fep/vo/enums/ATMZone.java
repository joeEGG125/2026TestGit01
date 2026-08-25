package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum ATMZone {
	/**
	 * 台幣主機
	 */
	TWN(0),
	/**
	 * 香港主機
	 */
	HKG(1),
	/**
	 * 澳門主機
	 */
	MAC(2);

	private int code;

	private ATMZone(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static ATMZone fromCode(int code) {
		for (ATMZone e : values()) {
			if (e.getCode() == code) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid code = [" + code + "]!!!");
	}

	public static ATMZone parse(Object nameOrCode) {
		if (nameOrCode instanceof Number) {
			return fromCode(((Number) nameOrCode).intValue());
		} else if (nameOrCode instanceof String) {
			String nameOrCodeStr = (String) nameOrCode;
			if (StringUtils.isNumeric(nameOrCodeStr)) {
				return fromCode(Integer.parseInt(nameOrCodeStr));
			}
			for (ATMZone e : values()) {
				if (e.name().equalsIgnoreCase(nameOrCodeStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or code = [" + nameOrCode + "]!!!");
	}
}
