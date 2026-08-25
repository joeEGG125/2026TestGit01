package com.syscom.fep.common.util;

import org.apache.commons.lang3.StringUtils;

public class ChipICPIN {

	private ChipICPIN() {}

	public static String getSCode(String cardno) {
		String expression = StringUtils.substring(cardno, 8, 14);
		String str = StringUtils.reverse(expression);
		int value = Integer.parseInt(expression) + Integer.parseInt(str) + 0x92dcf;
		return StringUtils.right(String.valueOf(value), 6);
	}

//	public static void main(String[] args) {
//		System.out.println(getPwd("1234567890"));
//		System.out.println(getPwd("303230303235313035343933393534383037303030303935303030303031303036303831373136323930303030"));
//	}
}
