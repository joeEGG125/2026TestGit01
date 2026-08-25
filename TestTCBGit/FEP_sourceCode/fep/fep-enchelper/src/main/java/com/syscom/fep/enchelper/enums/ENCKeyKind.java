package com.syscom.fep.enchelper.enums;

import org.apache.commons.lang3.StringUtils;

public enum ENCKeyKind {
	MSK(0),
	CDK(1),
	MAC(2),
	PVK(3),
	PPK(4),
	ICC(5),
	WKK(6),
	CVK(7),
	CPVK(8),
	MKAC(9),
	TWMP(10),
	ACH(11),
	// 2019-10-24 moidfy by David Tai for RBANK 製卡檔加密
	// Modify by David Tai on 2020-10-06 for 配合Loadkey規則，TMK改為TPK
	TPK(12),
	IMK(13),
	ICK(14),
	KBPK(15);  //TR31 key block

	private int value;

	private ENCKeyKind(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static ENCKeyKind fromValue(int value) {
		for (ENCKeyKind e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static ENCKeyKind parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (ENCKeyKind e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
