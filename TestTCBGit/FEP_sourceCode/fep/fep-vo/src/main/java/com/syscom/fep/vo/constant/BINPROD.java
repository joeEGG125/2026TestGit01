package com.syscom.fep.vo.constant;

/**
 * BIN產品別
 * 
 * A:國際金融卡(PLUS/CIRRUS) C:信用卡 F:海外分行卡(PLUS) G:Gift Card M:COMBO卡 S:Settlement V:VisaDebit "":一般帳號 E:MMA悠遊Debit D:簽帳卡
 */
public interface BINPROD {
	public static final String PLUS_Cirrus = "A";
	public static final String Credit = "C";
	public static final String PLUS = "F";
	public static final String Gift = "G";
	public static final String Combo = "M";
	public static final String Settlement = "S";
	public static final String VisaDebit = "V";
	public static final String NormalAccount = "";
	// 2014-02-06 Modify by Ruling for MMA悠遊Debit
	public static final String Easy = "E";
	// 2018-05-22 Modify by Ruling for MASTER DEBIT加悠遊
	public static final String Debit = "D";
}