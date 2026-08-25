package com.syscom.fep.vo.constant;

public interface TOSVCSType {

	/** 
	 掛失
	*/
	public static final String Lost = "1";

	/** 
	 取消掛失
	*/
	public static final String LostCancel = "2";

	/** 
	 餘額轉置(大於0)
	*/
	public static final String BalanceTransPositive = "3";

	/** 
	 餘額轉置(小於0)
	*/
	public static final String BalanceTransNegative = "4";

	/** 
	 月票退卡餘額轉置(大於0)
	*/
	public static final String MonthlyRefundPositive = "5";

	/** 
	 月票退卡餘額轉置(小於0)
	*/
	public static final String MonthlyRefundNegative = "6";

	/** 
	 餘額自動返回
	*/
	public static final String AutoLoadRefund = "7";
}
