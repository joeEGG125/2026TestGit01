package com.syscom.fep.frmcommon.util;

import org.apache.commons.lang3.StringUtils;

public class EnumUtil {
	private EnumUtil() {}

	public static String getEnumName(@SuppressWarnings("rawtypes") Enum e) {
		return e == null ? StringUtils.EMPTY : e.name();
	}
}
