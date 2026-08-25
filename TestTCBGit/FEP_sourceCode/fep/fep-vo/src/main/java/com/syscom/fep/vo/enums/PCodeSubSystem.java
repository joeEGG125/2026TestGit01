package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 財金PCode前2碼所區分之子系統別
 */
public enum PCodeSubSystem {
	P01(0),
	P11(1),
	P14(2),
	P15(3),
	P16(4),
	P21(5),
	P22(6),
	P24(7),
	P25(8),
	P31(9),
	P32(10),
	P50(11),
	P51(12),
	P52(13),
	P53(14),
	P58(15),
	P59(19),
	P61(20),
	P62(21),
	P63(22),
	P65(23),
	P66(24),
	P67(25);

	private int value;

	private PCodeSubSystem(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static PCodeSubSystem fromValue(int value) {
		for (PCodeSubSystem e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static PCodeSubSystem parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (PCodeSubSystem e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
