package com.syscom.fep.base.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * DB IO Action
 * 
 * @author Richard
 *
 */
public enum DBIOMethod {
	Query(0),
	Insert(1),
	Update(2),
	Delete(3),
	Execute(4);

	private int value;

	private DBIOMethod(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static DBIOMethod fromValue(int value) {
		for (DBIOMethod e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static DBIOMethod parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (DBIOMethod e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}