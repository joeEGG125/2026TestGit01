package com.syscom.fep.vo.constant;

public interface FEPTxnMessageFlow {

	public static final String ATM_Request = "A1";
	public static final String ATM_Response = "A2";
	public static final String ATM_Confirm_Request = "A3";
	public static final String ATM_Confirm_Response = "A4";
	public static final String FISC_Request = "F1";
	public static final String FISC_Response = "F2";
	public static final String FISC_Confirm = "F3";
	public static final String FISC_2290_INQ = "F9";
	public static final String CBS_Request = "H1";
	public static final String CBS_Response = "H2";
	public static final String CBS_INQ_Request = "I1";
	public static final String CBS_INQ_Response = "I2";
	public static final String CBS_EC_Request = "X1";
	public static final String CBS_EC_Response = "X2";
	public static final String CBS_CR_Request = "N1";
	public static final String CBS_CR_Response = "N2";
	public static final String ASC_TO_Resquest = "C1";
	public static final String ASC_TO_Response = "C2";
	public static final String ASC_CREC_TO_Resquest = "C3";
	public static final String ASC_CREC_TO_Response = "C4";
	public static final String ASC_FROM_Resquest = "CA";
	public static final String ASC_FROM_Response = "CB";
	public static final String ASC_CREC_FROM_Resquest = "CC";
	public static final String ASC_CREC_FROM_Response = "CD";
	public static final String BRS_Request = "B1";
	public static final String BRS_Response = "B2";
	public static final String T24_Request = "T1";
	public static final String T24_Response = "T2";
	//2014-02-06 Modify by Ruling for MMA悠遊Debit
	public static final String SVCS_FROM_Resquest = "DA";
	public static final String SVCS_FROM_Response = "D2";
	public static final String SVCS_TO_Response = "DB";
	public static final String SVCS_EC_Request = "DC";
	public static final String SVCS_EC_Response = "DD";
	public static final String AML_Request = "K1";
	public static final String AML_Response = "K2";

}
