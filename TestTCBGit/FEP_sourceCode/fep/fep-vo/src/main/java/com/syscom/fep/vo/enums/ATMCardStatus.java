package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 卡片狀態
 */
public enum ATMCardStatus {
	/**
	 * 晶片卡轉卡
	 */
	Change(0),

	/**
	 * 新申請
	 */
	Apply(1),

	/**
	 * 製卡
	 */
	Create(2),

	/**
	 * 領用
	 */
	Receive(3),

	/**
	 * 啟用
	 */
	Start(4),

	/**
	 * 掛失
	 */
	Lose(5),

	/**
	 * 註銷
	 */
	Cancel(6),

	/**
	 * 在途未啟用註銷
	 */
	WithoutUsageCancel(7),
	/**
	 * 
	 * 掛失未啟用金融信用卡
	 */
	LoseWithoutStart(8),

	/**
	 * 在途未啟用
	 */
	WithoutUsage(9);

	private int value;

	private ATMCardStatus(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static ATMCardStatus fromValue(int value) {
		for (ATMCardStatus e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static ATMCardStatus parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (ATMCardStatus e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
