package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum FeptxnEXCD {
	/**
	 * 進入行員功能
	 */
	A003(1),
	/**
	 * 離開行員功能
	 */
	A004(2),
	/**
	 * ATM跑馬燈訊息播放
	 */
	R002(3),
	/**
	 * ATM跑馬燈訊息停止播放
	 */
	R003(4),
	/**
	 * ATM 開始服務
	 */
	B001(5),
	/**
	 * ATM 暫停服務
	 */
	B002(6);

	private int value;

	private FeptxnEXCD(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static FeptxnEXCD fromValue(int value) {
		for (FeptxnEXCD e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static FeptxnEXCD parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (FeptxnEXCD e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
