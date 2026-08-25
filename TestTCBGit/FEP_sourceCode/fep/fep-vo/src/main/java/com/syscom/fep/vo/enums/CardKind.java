package com.syscom.fep.vo.enums;
// 卡片類別

import org.apache.commons.lang3.StringUtils;

public enum CardKind {
	/**
	 * 永豐GIF卡
	 */
	G(1),
	/**
	 * 錢卡
	 */
	M(2);
	
	private int code;

	private CardKind(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static CardKind fromCode(int code) {
		for (CardKind e : values()) {
			if (e.getCode() == code) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid code = [" + code + "]!!!");
	}

	public static CardKind parse(Object nameOrCode) {
		if (nameOrCode instanceof Number) {
			return fromCode(((Number) nameOrCode).intValue());
		} else if (nameOrCode instanceof String) {
			String nameOrCodeStr = (String) nameOrCode;
			if (StringUtils.isNumeric(nameOrCodeStr)) {
				return fromCode(Integer.parseInt(nameOrCodeStr));
			}
			for (CardKind e : values()) {
				if (e.name().equalsIgnoreCase(nameOrCodeStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or code = [" + nameOrCode + "]!!!");
	}
}
