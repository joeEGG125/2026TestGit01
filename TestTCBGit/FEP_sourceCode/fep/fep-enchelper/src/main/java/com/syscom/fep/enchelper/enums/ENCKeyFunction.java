package com.syscom.fep.enchelper.enums;

import org.apache.commons.lang3.StringUtils;

public enum ENCKeyFunction {
	FISC(0),
	RMR(1),
	RMS(2),
	ATM(3),
	RMF(4),
	OPC(5),
	C0(6),
	C1(7),
	C2(8),
	C3(9),
	C4(10),
	C5(11),
	C6(12),
	D6(13),
	D7(14),
	SC(15),
	// 2011-01-03 by kyo for IVRCheckPasswoerd
	IVR(16),
	// 2014-11-18 by Ruling for TSM
	C8(17),
	DEK(18),
	KEK(19),
	// 2015-09-16 by 榮升 for TSM/ACH
	ZCMK(20),
	KAC(21),
	KMC(22),
	KENC(23),
	// 2015/11/17 by Ashiang form TSM/ACH
	DKac(24),
	CDK(25),
	// 2016/03/17 by Ruling form FISC (MASTER 國際組織要求回覆發卡行回覆ARPC)
	// 2016/06/24 by Ruling form FISC (MASTER 國際組織要求回覆發卡行回覆ARPC) 由CIRR改為MC
	MC(26),
	// 2016/08/23 by Ruling form 繳費網PHASE2
	API(27),
	//2019-10-24 moidfy by David Tai for RBANK 製卡檔加密
	// Modify by David Tai on 2020-10-06 for 配合Loadkey規則，TMK改為TPK
	TPK(28),
	IMS(29), //流水編號，新增至最後的編號即可(Kuo)
	KBPK(30);
	private int value;

	private ENCKeyFunction(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static ENCKeyFunction fromValue(int value) {
		for (ENCKeyFunction e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static ENCKeyFunction parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (ENCKeyFunction e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
