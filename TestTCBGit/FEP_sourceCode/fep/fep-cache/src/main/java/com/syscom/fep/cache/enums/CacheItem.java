package com.syscom.fep.cache.enums;

import org.apache.commons.lang3.StringUtils;

public enum CacheItem {
	SYSSTAT(0), 
	MSGCTL(1), 
	ZONE(2),
	SYSCONF(3),
	CURCD(4),
	ALL(99);

	private int value;

	private CacheItem(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static CacheItem fromValue(int value) {
		for (CacheItem e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static CacheItem parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (CacheItem e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
