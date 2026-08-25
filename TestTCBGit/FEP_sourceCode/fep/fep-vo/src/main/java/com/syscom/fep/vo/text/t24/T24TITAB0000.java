package com.syscom.fep.vo.text.t24;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class T24TITAB0000 {
	public boolean IsIDNOPassed;
	public boolean IsOPEN_DATEPassed;

	// B0001
	// 因為需要@ID的Field，所以以a取代@
	public boolean IsaIDPassed;
	public boolean IsACCT_NOPassed;
	public boolean IsCURRENCYPassed;
	public boolean IsT_REG_TFR_TYPEPassed;

	// B0002
	public boolean IsENQ_ACCT_IDPassed;
	public boolean IsENQ_CUST_IDPassed;
	public boolean IsENQ_AC_TYPEPassed;

	// B0003
	public boolean IsCUSTOMER_NOPassed;

	// B0005
	public boolean IsENQ_ACCT_NOPassed;
	public boolean IsENQ_CHK_TYPEPassed;
	public boolean IsENQ_CHK_ITEMPassed;
	public boolean IsENQ_T_MNEMONICPassed;
	public boolean IsENQ_DATE_OF_BIRTHPassed;
	public boolean IsENQ_SMS_1Passed;
	public boolean IsENQ_T_PHONEPassed;
	public boolean IsENQ_SUB_ACCT_NOPassed;

	// B4000
	public boolean IsACCOUNT_NUMBERPassed;
	public boolean IsLOCK_AMOUNTPassed;
	public boolean IsDESCRIPTIONPassed;

	// B5000/B5001
	public boolean IsDEBIT_ACCT_NOPassed;
	public boolean IsDEBIT_CURRENCYPassed;
	public boolean IsDEBIT_AMOUNTPassed;
	public boolean IsCREDIT_BANK_CODEPassed;
	public boolean IsCREDIT_ACCT_NOPassed;
	public boolean IsCREDIT_CURRENCYPassed;
	public boolean IsCREDIT_AMOUNTPassed;
	public boolean IsTXN_DATEPassed;
	public boolean IsFISC_PCODEPassed;
	public boolean IsATM_IC_SEQNOPassed;
	public boolean IsATM_IC_MARKPassed;
	public boolean IsATM_CHK_TYPEPassed;
	public boolean IsATM_MCH_TYPEPassed;
	public boolean IsT_NWF_INSTPassed;
	public boolean IsT_NWF_TYPEPassed;
	public boolean IsT_NWF_CODEPassed;
	public boolean IsATM_TAX_UNITPassed;
	public boolean IsATM_DUE_DATEPassed;
	public boolean IsATM_RCN_SEQNOPassed;
	public boolean IsATM_CUST_IDPassed;
	public boolean IsT_PSB_MEMO_DPassed;
	public boolean IsT_PSB_REM_S_DPassed;
	public boolean IsT_PSB_REM_F_DPassed;
	public boolean IsT_PSB_REM_F_D_MUTIPassed;
	public boolean IsT_PSB_RINF_DPassed;
	public boolean IsT_PSB_MEMO_CPassed;
	public boolean IsT_PSB_REM_S_CPassed;
	public boolean IsT_PSB_REM_F_CPassed;
	public boolean IsT_PSB_REM_F_C_MUTIPassed;
	public boolean IsT_PSB_RINF_CPassed;
	public boolean IsVR_AC_PREF_CPassed;
	public boolean IsT_TXN_CODEPassed;

	// Prviate part ----------------------------------------------------------
	// B0011

	// B0004
	private String _IDNO;
	private String _OPEN_DATE;

	// B0001
	private String _aID;
	private String _ACCT_NO;
	private String _CURRENCY;

	// B0002
	private String _ENQ_ACCT_ID;
	private String _ENQ_CUST_ID;
	private String _ENQ_AC_TYPE;

	// B0003
	private String _CUSTOMER_NO;

	// B0005
	private String _ENQ_ACCT_NO;
	private String _ENQ_CHK_TYPE;
	private String _ENQ_CHK_ITEM;
	private String _ENQ_T_MNEMONIC;
	private String _ENQ_DATE_OF_BIRTH;
	private String _ENQ_SMS_1;
	private String _ENQ_T_PHONE;
	private String _ENQ_SUB_ACCT_NO;

	// B4000
	private String _ACCOUNT_NUMBER;
	private BigDecimal _LOCKED_AMOUNT = new BigDecimal(0);
	private String _DESCRIPTION;

	// B5000/B5001
	private String _DEBIT_ACCT_NO;
	private String _DEBIT_CURRENCY;
	private BigDecimal _DEBIT_AMOUNT = new BigDecimal(0);
	private String _CREDIT_BANK_CODE;
	private String _CREDIT_ACCT_NO;
	private String _CREDIT_CURRENCY;
	private BigDecimal _CREDIT_AMOUNT = new BigDecimal(0);
	private String _TXN_DATE;
	private String _FISC_PCODE;
	private String _ATM_IC_SEQNO;
	private String _ATM_IC_MARK;
	private String _ATM_CHK_TYPE;
	private String _ATM_MCH_TYPE;
	private String _T_NWF_INST;
	private String _T_NWF_TYPE;
	private String _T_NWF_CODE;
	private String _ATM_TAX_UNIT;
	private String _ATM_DUE_DATE;
	private String _ATM_RCN_SEQNO;
	private String _ATM_CUST_ID;
	private String _T_PSB_MEMO_D;
	private String _T_PSB_REM_S_D;
	private String _T_PSB_REM_F_D;
	private HashMap<Integer, String> _T_PSB_REM_F_D_MUTI = new HashMap<Integer, String>();
	private HashMap<Integer, String> _T_PSB_RINF_D = new HashMap<Integer, String>();
	private String _T_PSB_MEMO_C;
	private String _T_PSB_REM_S_C;
	private String _T_PSB_REM_F_C;
	private HashMap<Integer, String> _T_PSB_REM_F_C_MUTI = new HashMap<Integer, String>();
	private HashMap<Integer, String> _T_PSB_RINF_C = new HashMap<Integer, String>();
	private String _VR_AC_PREF_C;
	private String _T_REG_TFR_TYPE;
	private String _T_TXN_CODE;

	// Property part -----------------------------------------------------------------
	// B0011

	// B0004
	public String getIDNO()
	{
		return _IDNO;
	}
	public void setIDNO(String value) {
		_IDNO = value;
		IsIDNOPassed = true;
	}

	public String getOpenDate()
	{
		return _OPEN_DATE;
	}
	public void setOpenDate(String value) {
		_OPEN_DATE = value;
		IsOPEN_DATEPassed = true;
	}

	// B0001
	public String getAID()
	{
		return _aID;
	}
	public void setAID(String value) {
		_aID = value;
		IsaIDPassed = true;
	}

	public String getAcctNo()
	{
		return _ACCT_NO;
	}
	public void setAcctNo(String value) {
		_ACCT_NO = value;
		IsACCT_NOPassed = true;
	}

	public String getCURRENCY()
	{
		return _CURRENCY;
	}
	public void setCURRENCY(String value) {
		_CURRENCY = value;
		IsCURRENCYPassed = true;
	}

	// B0002
	public String getEnqAcctId()
	{
		return _ENQ_ACCT_ID;
	}
	public void setEnqAcctId(String value) {
		_ENQ_ACCT_ID = value;
		IsENQ_ACCT_IDPassed = true;
	}

	public String getEnqCustId()
	{
		return _ENQ_CUST_ID;
	}
	public void setEnqCustId(String value) {
		_ENQ_CUST_ID = value;
		IsENQ_CUST_IDPassed = true;
	}

	public String getEnqAcType()
	{
		return _ENQ_AC_TYPE;
	}
	public void setEnqAcType(String value) {
		_ENQ_AC_TYPE = value;
		IsENQ_AC_TYPEPassed = true;
	}

	// B0003
	public String getCustomerNo()
	{
		return _CUSTOMER_NO;
	}
	public void setCustomerNo(String value) {
		_CUSTOMER_NO = value;
		IsCUSTOMER_NOPassed = true;
	}

	// B0005
	public String getEnqAcctNo()
	{
		return _ENQ_ACCT_NO;
	}
	public void setEnqAcctNo(String value) {
		_ENQ_ACCT_NO = value;
		IsENQ_ACCT_NOPassed = true;
	}

	public String getEnqChkType()
	{
		return _ENQ_CHK_TYPE;
	}
	public void setEnqChkType(String value) {
		_ENQ_CHK_TYPE = value;
		IsENQ_CHK_TYPEPassed = true;
	}

	public String getEnqChkItem()
	{
		return _ENQ_CHK_ITEM;
	}
	public void setEnqChkItem(String value) {
		_ENQ_CHK_ITEM = value;
		IsENQ_CHK_ITEMPassed = true;
	}

	public String getEnqTMnemonic()
	{
		return _ENQ_T_MNEMONIC;
	}
	public void setEnqTMnemonic(String value) {
		_ENQ_T_MNEMONIC = value;
		IsENQ_T_MNEMONICPassed = true;
	}

	public String getEnqDateOfBirth()
	{
		return _ENQ_DATE_OF_BIRTH;
	}
	public void setEnqDateOfBirth(String value) {
		_ENQ_DATE_OF_BIRTH = value;
		IsENQ_DATE_OF_BIRTHPassed = true;
	}

	public String getEnqSms_1()
	{
		return _ENQ_SMS_1;
	}
	public void setEnqSms_1(String value) {
		_ENQ_SMS_1 = value;
		IsENQ_SMS_1Passed = true;
	}

	public String getEnqTPhone()
	{
		return _ENQ_T_PHONE;
	}
	public void setEnqTPhone(String value) {
		_ENQ_T_PHONE = value;
		IsENQ_T_PHONEPassed = true;
	}

	public String getEnqSubAcctNo()
	{
		return _ENQ_SUB_ACCT_NO;
	}
	public void setEnqSubAcctNo(String value) { 
		_ENQ_SUB_ACCT_NO = value;
		IsENQ_SUB_ACCT_NOPassed = true;
	}

	// B4000
	public String getAccountNumber()
	{
		return _ACCOUNT_NUMBER;
	}
	public void setAccountNumber(String value) {
		_ACCOUNT_NUMBER = value;
		IsACCOUNT_NUMBERPassed = true;
	}

	public BigDecimal getLockedAmount()
	{
		return _LOCKED_AMOUNT;
	}
	public void setLockedAmount(BigDecimal value) {
		_LOCKED_AMOUNT = value;
		IsLOCK_AMOUNTPassed = true;
	}

	public String getDESCRIPTION()
	{
		return _DESCRIPTION;
	}
	public void setDESCRIPTION(String value) {
		_DESCRIPTION = value;
		IsDESCRIPTIONPassed = true;
	}

	// B5000/B5001
	public String getDebitAcctNo()
	{
		return _DEBIT_ACCT_NO;
	}
	public void setDebitAcctNo(String value) {
		_DEBIT_ACCT_NO = value;
		IsDEBIT_ACCT_NOPassed = true;
	}

	public String getDebitCurrency()
	{
		return _DEBIT_CURRENCY;
	}
	public void setDebitCurrency(String value) {
		_DEBIT_CURRENCY = value;
		IsDEBIT_CURRENCYPassed = true;
	}

	public BigDecimal getDebitAmount()
	{
		return _DEBIT_AMOUNT;
	}
	public void setDebitAmount(BigDecimal value) {
		_DEBIT_AMOUNT = value;
		IsDEBIT_AMOUNTPassed = true;
	}

	public String getCreditBankCode()
	{
		return _CREDIT_BANK_CODE;
	}
	public void setCreditBankCode(String value){
		_CREDIT_BANK_CODE = value;
		IsCREDIT_BANK_CODEPassed = true;
	}

	public String getCreditAcctNo()
	{
		return _CREDIT_ACCT_NO;
	}
	public void setCreditAcctNo(String value) {
		_CREDIT_ACCT_NO = value;
		IsCREDIT_ACCT_NOPassed = true;
	}

	public String getCreditCurrency()
	{
		return _CREDIT_CURRENCY;
	}
	public void setCreditCurrency(String value) {
		_CREDIT_CURRENCY = value;
		IsCREDIT_CURRENCYPassed = true;
	}

	public BigDecimal getCreditAmount()
	{
		return _CREDIT_AMOUNT;
	}
	public void setCreditAmount(BigDecimal value) {
		_CREDIT_AMOUNT = value;
		IsCREDIT_AMOUNTPassed = true;
	}

	public String getTxnDate()
	{
		return _TXN_DATE;
	}
	public void setTxnDate(String value) {
		_TXN_DATE = value;
		IsTXN_DATEPassed = true;
	}

	public String getFiscPcode()
	{
		return _FISC_PCODE;
	}
	public void setFiscPcode(String value) {
		_FISC_PCODE = value;
		IsFISC_PCODEPassed = true;
	}

	public String getAtmIcSeqno()
	{
		return _ATM_IC_SEQNO;
	}
	public void setAtmIcSeqno(String value) {
		_ATM_IC_SEQNO = value;
		IsATM_IC_SEQNOPassed = true;
	}

	public String getAtmIcMark()
	{
		return _ATM_IC_MARK;
	}
	public void setAtmIcMark(String value) {
		_ATM_IC_MARK = value;
		IsATM_IC_MARKPassed = true;
	}

	public String getAtmChkType()
	{
		return _ATM_CHK_TYPE;
	}
	public void setAtmChkType(String value) {
		_ATM_CHK_TYPE = value;
		IsATM_CHK_TYPEPassed = true;
	}

	public String getAtmMchType()
	{
		return _ATM_MCH_TYPE;
	}
	public void setAtmMchType(String value) {
		_ATM_MCH_TYPE = value;
		IsATM_MCH_TYPEPassed = true;
	}

	public String getTNwfInst()
	{
		return _T_NWF_INST;
	}
	public void setTNwfInst(String value) {
		_T_NWF_INST = value;
		IsT_NWF_INSTPassed = true;
	}

	public String getTNwfType()
	{
		return _T_NWF_TYPE;
	}
	public void setTNwfType(String value) {
		_T_NWF_TYPE = value;
		IsT_NWF_TYPEPassed = true;
	}

	public String getTNwfCode()
	{
		return _T_NWF_CODE;
	}
	public void setTNwfCode(String value) {
		_T_NWF_CODE = value;
		IsT_NWF_CODEPassed = true;
	}

	public String getAtmTaxUnit()
	{
		return _ATM_TAX_UNIT;
	}
	public void setAtmTaxUnit(String value) {
		_ATM_TAX_UNIT = value;
		IsATM_TAX_UNITPassed = true;
	}

	public String getAtmDueDate()
	{
		return _ATM_DUE_DATE;
	}
	public void setAtmDueDate(String value) {
		_ATM_DUE_DATE = value;
		IsATM_DUE_DATEPassed = true;
	}

	public String getAtmRcnSeqno()
	{
		return _ATM_RCN_SEQNO;
	}
	public void setAtmRcnSeqno(String value) {
		_ATM_RCN_SEQNO = value;
		IsATM_RCN_SEQNOPassed = true;
	}

	public String getAtmCustId()
	{
		return _ATM_CUST_ID;
	}
	public void setAtmCustId(String value) {
		_ATM_CUST_ID = value;
		IsATM_CUST_IDPassed = true;
	}

	public String getTPsbMemoD()
	{
		return _T_PSB_MEMO_D;
	}
	public void setTPsbMemoD(String value) {
		_T_PSB_MEMO_D = value;
		IsT_PSB_MEMO_DPassed = true;
	}

	public String getTPsbRemSD()
	{
		return _T_PSB_REM_S_D;
	}
	public void setTPsbRemSD(String value) {
		_T_PSB_REM_S_D = value;
		IsT_PSB_REM_S_DPassed = true;
	}

	public String getTPsbRemFD()
	{
		return _T_PSB_REM_F_D;
	}
	public void setTPsbRemFD(String value) {
		_T_PSB_REM_F_D = value;
		IsT_PSB_REM_F_DPassed = true;
	}

	public HashMap<Integer, String> getTPsbRemFDMuti()
	{
		return _T_PSB_REM_F_D_MUTI;
	}
	public void setTPsbRemFDMuti(HashMap<Integer, String> value) {
		_T_PSB_REM_F_D_MUTI = value;
		IsT_PSB_REM_F_D_MUTIPassed = true;
	}

	public HashMap<Integer, String> getTPsbRinfD()
	{
		return _T_PSB_RINF_D;
	}
	public void setTPsbRinfD(HashMap<Integer, String> value) {
		_T_PSB_RINF_D = value;
		IsT_PSB_RINF_DPassed = true;
	}

	public String getTPsbMemoC()
	{
		return _T_PSB_MEMO_C;
	}
	public void setTPsbMemoC(String value) {
		_T_PSB_MEMO_C = value;
		IsT_PSB_MEMO_CPassed = true;
	}

	public String getTPsbRemSC()
	{
		return _T_PSB_REM_S_C;
	}
	public void setTPsbRemSC(String value) {
		_T_PSB_REM_S_C = value;
		IsT_PSB_REM_S_CPassed = true;
	}

	public String getTPsbRemFC()
	{
		return _T_PSB_REM_F_C;
	}
	public void setTPsbRemFC(String value) {
		_T_PSB_REM_F_C = value;
		IsT_PSB_REM_F_CPassed = true;
	}

	public HashMap<Integer, String> getTPsbRemFCMuti()
	{
		return _T_PSB_REM_F_C_MUTI;
	}
	public void setTPsbRemFCMuti(HashMap<Integer, String> value) {
		_T_PSB_REM_F_C_MUTI = value;
		IsT_PSB_REM_F_C_MUTIPassed = true;
	}

	public HashMap<Integer, String> getTPsbRinfC()
	{
		return _T_PSB_RINF_C;
	}
	public void setTPsbRinfC(HashMap<Integer, String> value) {
		_T_PSB_RINF_C = value;
		IsT_PSB_RINF_CPassed = true;
	}

	public String getVrAcPrefC()
	{
		   return _VR_AC_PREF_C;
	}
	public void setVrAcPrefC(String value) {
		_VR_AC_PREF_C = value;
		IsVR_AC_PREF_CPassed = true;
	}

	public String getTRegTfrType()
	{
		return _T_REG_TFR_TYPE;
	}
	public void setTRegTfrType(String value) {
		_T_REG_TFR_TYPE = value;
		IsT_REG_TFR_TYPEPassed = true;
	}

	public String getTTxnCode()
	{
		return _T_TXN_CODE;
	}
	public void setTTxnCode(String value) {
		_T_TXN_CODE = value;
		IsT_TXN_CODEPassed = true;
	}

	public boolean genDictionary(HashMap<String, String> TITABody) {
		// B0004 目前FS仍未定義，故只是先寫
		if (IsIDNOPassed) {
			TITABody.put("IDNO", getIDNO());
		}
		if (IsOPEN_DATEPassed) {
			TITABody.put("OPEN.DATE", getOpenDate());
		}

		// B0001
		if (IsaIDPassed) {
			TITABody.put("@ID", getAID());
		}
		if (IsACCT_NOPassed) {
			TITABody.put("ACCT.NO", getAcctNo());
		}
		if (IsCUSTOMER_NOPassed) {
			TITABody.put("CUSTOMER.NO", getCustomerNo());
		}
		if (IsCURRENCYPassed) {
			TITABody.put("CURRENCY", getCURRENCY());
		}

		// B0002
		if (IsENQ_ACCT_IDPassed) {
			TITABody.put("ENQ.ACCT.ID", getEnqAcctId());
		}
		if (IsENQ_CUST_IDPassed) {
			TITABody.put("ENQ.CUST.ID", getEnqCustId());
		}
		if (IsENQ_AC_TYPEPassed) {
			TITABody.put("ENQ.AC.TYPE", getEnqAcType());
		}

		// B4000
		if (IsACCOUNT_NUMBERPassed) {
			TITABody.put("ACCOUNT.NUMBER", getAccountNumber());
		}
		if (IsLOCK_AMOUNTPassed) {
			TITABody.put("LOCKED.AMOUNT", getLockedAmount().toString());
		}
		if (IsDESCRIPTIONPassed) {
			TITABody.put("DESCRIPTION", getDESCRIPTION());
		}

		// B0005
		if (IsENQ_ACCT_NOPassed) {
			TITABody.put("ENQ.ACCT.NO", getEnqAcctNo());
		}
		if (IsENQ_CHK_TYPEPassed) {
			TITABody.put("ENQ.CHK.TYPE", getEnqChkType());
		}
		if (IsENQ_CHK_ITEMPassed) {
			TITABody.put("ENQ.CHK.ITEM", getEnqChkItem());
		}
		if (IsENQ_T_MNEMONICPassed) {
			TITABody.put("ENQ.T.MNEMONIC", getEnqTMnemonic());
		}
		if (IsENQ_DATE_OF_BIRTHPassed) {
			TITABody.put("ENQ.DATE.OF.BIRTH", getEnqDateOfBirth());
		}
		if (IsENQ_SMS_1Passed) {
			TITABody.put("ENQ.SMS.1", getEnqSms_1());
		}
		if (IsENQ_T_PHONEPassed) {
			TITABody.put("ENQ.T.PHONE", getEnqTPhone());
		}
		if (IsENQ_SUB_ACCT_NOPassed) {
			TITABody.put("ENQ.SUB.ACCT.NO", getEnqSubAcctNo());
		}

		// B5000
		// FS檔案對應尚未確認
		if (IsDEBIT_ACCT_NOPassed) {
			TITABody.put("DEBIT.ACCT.NO", getDebitAcctNo());
		}
		if (IsDEBIT_CURRENCYPassed) {
			TITABody.put("DEBIT.CURRENCY", getDebitCurrency());
		}
		if (IsDEBIT_AMOUNTPassed) {
			TITABody.put("DEBIT.AMOUNT", getDebitAmount().toString());
		}
		if (IsCREDIT_BANK_CODEPassed) {
			TITABody.put("CREDIT.BANK.CODE", getCreditBankCode());
		}
		if (IsCREDIT_ACCT_NOPassed) {
			TITABody.put("CREDIT.ACCT.NO", getCreditAcctNo());
		}
		if (IsCREDIT_CURRENCYPassed) {
			TITABody.put("CREDIT.CURRENCY", getCreditCurrency());
		}
		if (IsCREDIT_AMOUNTPassed) {
			TITABody.put("CREDIT.AMOUNT", getCreditAmount().toString());
		}
		if (IsTXN_DATEPassed) {
			TITABody.put("TXN.DATE", getTxnDate());
		}
		if (IsFISC_PCODEPassed) {
			TITABody.put("FISC.PCODE", getFiscPcode());
		}
		if (IsATM_IC_SEQNOPassed) {
			TITABody.put("ATM.IC.SEQNO", getAtmIcSeqno());
		}
		if (IsATM_IC_MARKPassed) {
			TITABody.put("ATM.IC.MARK", getAtmIcMark());
		}
		if (IsATM_CHK_TYPEPassed) {
			TITABody.put("ATM.CHK.TYPE", getAtmChkType());
		}
		if (IsATM_MCH_TYPEPassed) {
			TITABody.put("ATM.MCH.TYPE", getAtmMchType());
		}
		if (IsT_NWF_INSTPassed) {
			TITABody.put("T.NWF.INST", getTNwfInst());
		}
		if (IsT_NWF_TYPEPassed) {
			TITABody.put("T.NWF.TYPE", getTNwfType());
		}
		if (IsT_NWF_CODEPassed) {
			TITABody.put("T.NWF.CODE", getTNwfCode());
		}
		if (IsATM_TAX_UNITPassed) {
			TITABody.put("ATM.TAX.UNIT", getAtmTaxUnit());
		}
		if (IsATM_DUE_DATEPassed) {
			TITABody.put("ATM.DUE.DATE", getAtmDueDate());
		}
		if (IsATM_RCN_SEQNOPassed) {
			TITABody.put("ATM.RCN.SEQNO", getAtmRcnSeqno());
		}
		if (IsATM_CUST_IDPassed) {
			TITABody.put("ATM.CUST.ID", getAtmCustId());
		}

		if (IsT_PSB_MEMO_DPassed) {
			TITABody.put("T.PSB.MEMO.D", getTPsbMemoD());
		}
		if (IsT_PSB_REM_S_DPassed) {
			TITABody.put("T.PSB.REM.S.D", getTPsbRemSD());
		}
		if (IsT_PSB_REM_F_DPassed) {
			TITABody.put("T.PSB.REM.F.D", getTPsbRemFD());
		}
		//2020/05/19 Modify by Ruling for ATM中文備註
		// Dictionary ---------------------------------------------------------------------
		for (Map.Entry<Integer, String> item : getTPsbRemFDMuti().entrySet()) {
			 if (IsT_PSB_REM_F_DPassed) {
				if (TITABody.containsKey("T.PSB.REM.F.D")) {
					TITABody.remove("T.PSB.REM.F.D");
					IsT_PSB_REM_F_DPassed = false;
				}
			 }
			TITABody.put("T.PSB.REM.F.D_" + String.valueOf(item.getKey()), item.getValue().toString());
		}
		//If IsT_PSB_RINF_DPassed Then
		for (Map.Entry<Integer, String> item : getTPsbRinfD().entrySet()) {
			TITABody.put("T.PSB.RINF.D_" + String.valueOf(item.getKey()), item.getValue().toString());
		}
		//End If
		if (IsT_PSB_MEMO_CPassed) {
			TITABody.put("T.PSB.MEMO.C", getTPsbMemoC());
		}
		if (IsT_PSB_REM_S_CPassed) {
			TITABody.put("T.PSB.REM.S.C", getTPsbRemSC());
		}
		if (IsT_PSB_REM_F_CPassed) {
			TITABody.put("T.PSB.REM.F.C", getTPsbRemFC());
		}
		//2020/05/19 Modify by Ruling for ATM中文備註
		// Dictionary ---------------------------------------------------------------------
		for (Map.Entry<Integer, String> item : getTPsbRemFCMuti().entrySet()) {
			if (IsT_PSB_REM_F_CPassed) {
				if (TITABody.containsKey("T.PSB.REM.F.C")) {
					TITABody.remove("T.PSB.REM.F.C");
					IsT_PSB_REM_F_CPassed = false;
				}
			 }
			TITABody.put("T.PSB.REM.F.C_" + String.valueOf(item.getKey()), item.getValue().toString());
		}
		//If IsT_PSB_RINF_CPassed Then
		for (Map.Entry<Integer, String> item : getTPsbRinfC().entrySet()) {
			TITABody.put("T.PSB.RINF.C_" + String.valueOf(item.getKey()), item.getValue().toString());
		}
		//End If
		if (IsVR_AC_PREF_CPassed) {
			TITABody.put("VR.AC.PREF.C", getVrAcPrefC());
		}
		//2011-04-29 Modify by kyo for B5000 +欄位
		if (IsT_REG_TFR_TYPEPassed) {
			TITABody.put("T.REG.TFR.TYPE", getTRegTfrType());
		}
		//2012-01-06 Modify by Ruling for B5000 +欄位
		if (IsT_TXN_CODEPassed) {
			TITABody.put("T.TXN.CODE", getTTxnCode());
		}
		return false;
	}

}
