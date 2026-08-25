package com.syscom.fep.base.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * FEP子系統代號
 * 
 * @author Richard
 */
public enum SubSystem {
	None(0),
	/**
	 * 財金公司跨行前置處理系統
	 */
	INBK(1),
	/**
	 * 跨行通匯系統(含國內匯兌)
	 */
	RM(2),
	/**
	 * ATM 前置處理系統
	 */
	ATMP(3),
	/**
	 * 金融卡卡片管理系統
	 */
	Card(4),
	/**
	 * HSM基碼管理系統
	 */
	HSM(5),
	/**
	 * ATM及上項各前置處理系統監控系統
	 */
	FEPMonitor(6),
	/**
	 * 入扣帳帳務比對系統
	 */
	RECS(7),
	/**
	 * ATM集中化
	 */
	GW(8),
	/**
	 * 公用模組
	 */
	CMN(9),
	// Add by David Tai on 2014-02-05 for MMA悠遊Debit
	/**
	 * 中文匯款平台
	 */
	MRM(10),
	/**
	 * 現金管理平台
	 */
	CMS(11),
	/**
	 * MMA悠遊Debit
	 */
	SVCS(12),
	/**
	 * 台灣行動支付
	 */
	PSPTSM(13),
	/**
	 * IPIN
	 */
	IPIN(14),
	/**
	 * DP
	 */
	DP(15),
	/**
	 * eWEB
	 */
	EWEB(16),
	/**
	 * ATMMon 跨行前置系統
	 */
	ATMMON(17),
	
	HCE(18),
	
	EAT(19),
	NB(20),
	MFT(21),
	TWMP(22),
	IVR(22),
	CBSPEND(23),
	BATCH(24),
	MCH(25);
	private int value;

	private SubSystem(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static SubSystem fromValue(int value) {
		for (SubSystem e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}
	
	public static SubSystem parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (SubSystem e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}