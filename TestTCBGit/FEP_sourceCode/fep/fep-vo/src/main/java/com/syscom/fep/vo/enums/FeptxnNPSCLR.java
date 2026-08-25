package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum FeptxnNPSCLR {

	/** 
	 非手續費清算單位
	*/
	None(0),
	/** 
	 
	 
	 轉出負擔手續費
	*/
	TROut(1),
	/** 
	 
	 
	 轉入負擔手續費
	*/
	TRIn(2);
	private int value;

	private FeptxnNPSCLR(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static FeptxnNPSCLR fromValue(int value) {
		for (FeptxnNPSCLR e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static FeptxnNPSCLR parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (FeptxnNPSCLR e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
