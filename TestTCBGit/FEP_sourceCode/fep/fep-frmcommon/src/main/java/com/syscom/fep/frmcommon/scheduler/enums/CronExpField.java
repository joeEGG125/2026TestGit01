package com.syscom.fep.frmcommon.scheduler.enums;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public enum CronExpField {
	SECOND(0, "*"),
	MINUTE(1, "*"),
	HOUR(2, "*"),
	DAY_OF_MONTH(3, "*"),
	MONTH(4, "*"),
	DAY_OF_WEEK(5, "?"),
	YEAR(6, "*");

	private final int index;
	private final String value;

	private CronExpField(int index, String value) {
		this.index = index;
		this.value = value;
	}

	public int getIndex() {
		return index;
	}

	public String getValue() {
		return value;
	}

	public static List<String> getCronExpressionList() {
		List<String> cronExpressionList = new ArrayList<>(YEAR.getIndex() + 1);
		for (int i = 0; i <= YEAR.getIndex(); i++) {
			cronExpressionList.add(fromIndex(i).getValue());
		}
		return cronExpressionList;
	}

	public static CronExpField fromIndex(int index) {
		for (CronExpField e : values()) {
			if (e.getIndex() == index) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid index = [" + index + "]!!!");
	}

	public static CronExpField parse(Object nameOrIndex) {
		if (nameOrIndex instanceof Integer) {
			return fromIndex((Integer) nameOrIndex);
		} else if (nameOrIndex instanceof Short) {
			return fromIndex((Short) nameOrIndex);
		}
		if (nameOrIndex instanceof String) {
			String nameOrValueStr = (String) nameOrIndex;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromIndex(Integer.parseInt(nameOrValueStr));
			}
			for (CronExpField e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or index = [" + nameOrIndex + "]!!!");
	}
}
