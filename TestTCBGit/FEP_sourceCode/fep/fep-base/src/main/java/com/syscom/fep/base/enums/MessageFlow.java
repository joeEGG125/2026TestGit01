package com.syscom.fep.base.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 表示電文的方向及種類
 * 
 * @author Richard
 *
 */
public enum MessageFlow {
	None(0),
	Request(1),
	Response(2),
	Confirmation(3),
	ResponseConfirmation(4),
	RequestRepeat(5),
	ConfirmationRepeat(6);

	private int value;

	private MessageFlow(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static MessageFlow fromValue(int value) {
		for (MessageFlow e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static MessageFlow parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (MessageFlow e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}