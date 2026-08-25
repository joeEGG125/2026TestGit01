package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 信用卡的交易代號
 */
public enum CreditTXCD {
	C01(1),
	C02(2),
	C04(3),
	C06(4),
	C07(5),
	B01(6),
	B02(7),
	B03(8),
	B04(9),
	B05(10),
	B06(11),
	B07(12),
	B09(13),
	B10(14),
	B11(15),
	B12(16),
	B14(17),
	B15(18),
	B16(19),
	B20(20),
	B17(21), // add by maxine on 2011/09/14 for 新增信用卡電文B17
	C22(22); // add by Ruling on 2013/11/25 for 新增現金儲值卡自動加值C20

	private int value;

	private CreditTXCD(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static CreditTXCD fromValue(int value) {
		for (CreditTXCD e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static CreditTXCD parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (CreditTXCD e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
