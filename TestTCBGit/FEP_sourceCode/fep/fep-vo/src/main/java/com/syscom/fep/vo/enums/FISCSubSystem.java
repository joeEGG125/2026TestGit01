package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum FISCSubSystem {

	RM(1),
	CLR(2),
	OPC(3),
	INBK(4),
	FCCLR(5),
	FCRM(6),
	EMVIC(7);
	private int value;

	private FISCSubSystem(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static FISCSubSystem fromValue(int value) {
		for (FISCSubSystem e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static FISCSubSystem parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (FISCSubSystem e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
