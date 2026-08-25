package com.syscom.fep.vo.text.t24;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class T24TITAA0000 {
	// IsPassed Part-----------------------------------------

	// A1000
	public boolean IsDEBIT_ACCT_NOPassed;
	public boolean IsDEBIT_CURRENCYPassed;
	public boolean IsDEBIT_AMOUNTPassed;
	public boolean IsCREDIT_CURRENCYPassed;
	public boolean IsCREDIT_AMOUNTPassed;
	public boolean IsEXCH_RATEPassed;
	public boolean IsACCR_CHG_AMTPassed;
	public boolean IsIC_ACTNOPassed;
	public boolean IsCREDIT_ACCT_NOPassed;
	public boolean IsCHG_ACCOUNTPassed;
	public boolean IsT_PSB_MEMO_DPassed;
	public boolean IsT_PSB_REM_S_DPassed;
	public boolean IsT_PSB_REM_F_DPassed;
	public boolean IsT_PSB_REM_F_D_MUTIPassed;
	public boolean IsT_PSB_RINF_DPassed;
	public boolean IsT_REG_TFR_TYPEPassed;
	// A1010
	public boolean IsFISC_BKNOSTANPassed;
	public boolean IsCHG_CURRENCYPassed;

	// A1020
	public boolean IsFCY_CURRENCYPassed;
	public boolean IsFCY_AMOUNTPassed;
	public boolean IsPLUS_DATEPassed;
	public boolean IsT_CRS_COUNTRYPassed;

	// A1070    
	public boolean IsT_PSB_MEMO_CPassed;
	public boolean IsT_PSB_REM_S_CPassed;
	public boolean IsT_PSB_REM_F_CPassed;
	public boolean IsT_PSB_REM_F_C_MUTIPassed;
	public boolean IsT_PSB_RINF_CPassed;

	// A2110
	public boolean IsMBANK_ACTNOPassed;

	// A1270
	public boolean IsFEE_PAYERPassed;
	public boolean IsIDNOPassed;
	public boolean IsVPIDPassed;
	public boolean IsPAYTYPEPassed;
	public boolean IsPAYNOPassed;
	public boolean IsCDPFGPassed;
	public boolean IsT_INSU_IDE_NOPassed;

	// A2310
	public boolean IsCREDIT_CARD_NOPassed;

	// A2710
	public boolean IsORI_TX_DATEPassed;

	//A1051
	public boolean IsT_FOREIGNER_FLGPassed;

	//A2910
	public boolean IsT_BR_SEQNOPassed;

	//A2210
	public boolean IsT_SAVING_CHKPassed;

	// General
	public boolean IsBANK_CODE_DRPassed;
	public boolean IsACCT_TXN_DRPassed;
	public boolean IsBANK_CODE_CRPassed;
	public boolean IsACCT_TXN_CRPassed;
	public boolean IsAUTH_CODEPassed;
	public boolean IsT_IP_ADDRESSPassed;


	// Prviate part ----------------------------------------------------------
	// A1000
	private String _DEBIT_ACCT_NO;
	private String _DEBIT_CURRENCY;
	private BigDecimal _DEBIT_AMOUNT = new BigDecimal(0);
	private String _CREDIT_CURRENCY;
	private BigDecimal _CREDIT_AMOUNT = new BigDecimal(0);
	private BigDecimal _EXCH_RATE = new BigDecimal(0);
	private BigDecimal _ACCR_CHG_AMT = new BigDecimal(0);
	private String _IC_ACTNO;
	private String _CREDIT_ACCT_NO;
	private String _CHG_ACCOUNT;
	private String _T_PSB_MEMO_D;
	private String _T_PSB_REM_S_D;
	private String _T_PSB_REM_F_D;
	private HashMap<Integer, String> _T_PSB_REM_F_D_MUTI = new HashMap<Integer, String>();
	private HashMap<Integer, String> _T_PSB_RINF_D = new HashMap<Integer, String>();
	private String _T_REG_TFR_TYPE;

	// A1010
	private String _FISC_BKNOSTAN;
	private String _CHG_CURRENCY;

	// A1020
	private String _FCY_CURRENCY;
	private BigDecimal _FCY_AMOUNT = new BigDecimal(0);
	private String _PLUS_DATE;
	private String _T_CRS_COUNTRY;

	// A1070    
	private String _T_PSB_MEMO_C;
	private String _T_PSB_REM_S_C;
	private String _T_PSB_REM_F_C;
	private HashMap<Integer, String> _T_PSB_REM_F_C_MUTI = new HashMap<Integer, String>();
	private HashMap<Integer, String> _T_PSB_RINF_C = new HashMap<Integer, String>();

	// A2110
	private String _MBANK_ACTNO;

	// A1270
	private String _FEE_PAYER;
	private String _IDNO;
	private String _VPID;
	private String _PAYTYPE;
	private String _PAYNO;
	private String _CDPFG;
	private String _T_INSU_IDE_NO;

	// A2310
	private String _CREDIT_CARD_NO;

	// A2710
	private String _ORI_TX_DATE;

	// A1051
	private String _T_FOREIGNER_FLG;

	// A1170/A1110/A1210/A2210/A1310/A1050/A2910/A2920/A2930
	private String _T_BR_SEQNO;

	//A2210
	private String _T_SAVING_CHK;

	// General
	private String _BANK_CODE_DR;
	private String _ACCT_TXN_DR;
	private String _BANK_CODE_CR;
	private String _ACCT_TXN_CR;
	private String _AUTH_CODE;
	private String _T_IP_ADDRESS;


	// Property part -----------------------------------------------------------------

	public final String getDebitAcctNo()
	{
		return _DEBIT_ACCT_NO;
	}
	public final void setDebitAcctNo(String value) {
		_DEBIT_ACCT_NO = value;
		IsDEBIT_ACCT_NOPassed = true;
	}
	public final String getDebitCurrency()
	{
		return _DEBIT_CURRENCY;
	}
	public final void setDebitCurrency(String value) {
		_DEBIT_CURRENCY = value;
		IsDEBIT_CURRENCYPassed = true;
	}
	public final BigDecimal getDebitAmount()
	{
		return _DEBIT_AMOUNT;
	}
	public final void setDebitAmount(BigDecimal value) {
		_DEBIT_AMOUNT = value;
		IsDEBIT_AMOUNTPassed = true;
	}
	public final String getCreditCurrency()
	{
		return _CREDIT_CURRENCY;
	}
	public final void setCreditCurrency(String value) {
		_CREDIT_CURRENCY = value;
		IsCREDIT_CURRENCYPassed = true;
	}
	public final BigDecimal getCreditAmount()
	{
		return _CREDIT_AMOUNT;
	}
	public final void setCreditAmount(BigDecimal value) {
		_CREDIT_AMOUNT = value;
		IsCREDIT_AMOUNTPassed = true;
	}
	public final BigDecimal getExchRate()
	{
		return _EXCH_RATE;
	}
	public final void setExchRate(BigDecimal value) {
		_EXCH_RATE = value;
		IsEXCH_RATEPassed = true;
	}
	public final BigDecimal getAccrChgAmt()
	{
		return _ACCR_CHG_AMT;
	}
	public final void setAccrChgAmt(BigDecimal value) {
		_ACCR_CHG_AMT = value;
		IsACCR_CHG_AMTPassed = true;
	}
	public final String getIcActno()
	{
		return _IC_ACTNO;
	}
	public final void setIcActno(String value) {
		_IC_ACTNO = value;
		IsIC_ACTNOPassed = true;
	}
	public final String getCreditAcctNo()
	{
		return _CREDIT_ACCT_NO;
	}
	public final void setCreditAcctNo(String value) {
		_CREDIT_ACCT_NO = value;
		IsCREDIT_ACCT_NOPassed = true;
	}
	public final String getChgAccount()
	{
		return _CHG_ACCOUNT;
	}
	public final void setChgAccount(String value) {
		_CHG_ACCOUNT = value;
		IsCHG_ACCOUNTPassed = true;
	}
	public final String getTPsbMemoD()
	{
		return _T_PSB_MEMO_D;
	}
	public final void setTPsbMemoD(String value) {
		_T_PSB_MEMO_D = value;
		IsT_PSB_MEMO_DPassed = true;
	}
	public final String getTPsbRemSD()
	{
		return _T_PSB_REM_S_D;
	}
	public final void setTPsbRemSD(String value) {
		_T_PSB_REM_S_D = value;
		IsT_PSB_REM_S_DPassed = true;
	}
	public final String getTPsbRemFD()
	{
		return _T_PSB_REM_F_D;
	}
	public final void setTPsbRemFD(String value) {
		_T_PSB_REM_F_D = value;
		IsT_PSB_REM_F_DPassed = true;
	}
	public final HashMap<Integer, String> getTPsbRemFDMuti()
	{
		return _T_PSB_REM_F_D_MUTI;
	}
	public final void setTPsbRemFDMuti(HashMap<Integer, String> value) {
		_T_PSB_REM_F_D_MUTI = value;
		IsT_PSB_REM_F_D_MUTIPassed = true;
	}
	public final HashMap<Integer, String> getTPsbRinfD()
	{
		return _T_PSB_RINF_D;
	}
	public final void setTPsbRinfD(HashMap<Integer, String> value) {
		_T_PSB_RINF_D = value;
		IsT_PSB_RINF_DPassed = true;
	}

	// A1010
	public final String getFiscBknostan()
	{
		return _FISC_BKNOSTAN;
	}
	public final void setFiscBknostan(String value) {
		_FISC_BKNOSTAN = value;
		IsFISC_BKNOSTANPassed = true;
	}
	public final String getChgCurrency()
	{
		return _CHG_CURRENCY;
	}
	public final void setChgCurrency(String value) {
		_CHG_CURRENCY = value;
		IsCHG_CURRENCYPassed = true;
	}

	// A1020
	public final String getFcyCurrency()
	{
		return _FCY_CURRENCY;
	}
	public final void setFcyCurrency(String value) {
		_FCY_CURRENCY = value;
		IsFCY_CURRENCYPassed = true;
	}
	public final BigDecimal getFcyAmount()
	{
		return _FCY_AMOUNT;
	}
	public final void setFcyAmount(BigDecimal value) {
		_FCY_AMOUNT = value;
		IsFCY_AMOUNTPassed = true;
	}
	public final String getPlusDate()
	{
		return _PLUS_DATE;
	}
	public final void setPlusDate(String value) {
		_PLUS_DATE = value;
		IsPLUS_DATEPassed = true;
	}

	public final String getTCrsCountry()
	{
		return _T_CRS_COUNTRY;
	}
	public final void setTCrsCountry(String value) {
		_T_CRS_COUNTRY = value;
		IsT_CRS_COUNTRYPassed = true;
	}

	// A1070    
	public final String getTPsbMemoC()
	{
		return _T_PSB_MEMO_C;
	}
	public final void setTPsbMemoC(String value) {
		_T_PSB_MEMO_C = value;
		IsT_PSB_MEMO_CPassed = true;
	}
	public final String getTPsbRemSC()
	{
		return _T_PSB_REM_S_C;
	}
	public final void setTPsbRemSC(String value) {
		_T_PSB_REM_S_C = value;
		IsT_PSB_REM_S_CPassed = true;
	}
	public final String getTPsbRemFC()
	{
		return _T_PSB_REM_F_C;
	}
	public final void setTPsbRemFC(String value) {
		_T_PSB_REM_F_C = value;
		IsT_PSB_REM_F_CPassed = true;
	}
	public final HashMap<Integer, String> getTPsbRemFCMuti()
	{
		return _T_PSB_REM_F_C_MUTI;
	}
	public final void setTPsbRemFCMuti(HashMap<Integer, String> value) {
		_T_PSB_REM_F_C_MUTI = value;
		IsT_PSB_REM_F_C_MUTIPassed = true;
	}
	public final HashMap<Integer, String> getTPsbRinfC()
	{
		return _T_PSB_RINF_C;
	}
	public final void setTPsbRinfC(HashMap<Integer, String> value) {
		_T_PSB_RINF_C = value;
		IsT_PSB_RINF_CPassed = true;
	}

	public final String getTRegTfrType()
	{
		return _T_REG_TFR_TYPE;
	}
	public final void setTRegTfrType(String value) {
		_T_REG_TFR_TYPE = value;
		IsT_REG_TFR_TYPEPassed = true;
	}

	// A2110
	public final String getMbankActno()
	{
		return _MBANK_ACTNO;
	}
	public final void setMbankActno(String value) {
		_MBANK_ACTNO = value;
		IsMBANK_ACTNOPassed = true;
	}

	// A1270
	public final String getFeePayer()
	{
		return _FEE_PAYER;
	}
	public final void setFeePayer(String value) {
		_FEE_PAYER = value;
		IsFEE_PAYERPassed = true;
	}
	public final String getIDNO()
	{
		return _IDNO;
	}
	public final void setIDNO(String value) {
		_IDNO = value;
		IsIDNOPassed = true;
	}
	public final String getVPID()
	{
		return _VPID;
	}
	public final void setVPID(String value) {
		_VPID = value;
		IsVPIDPassed = true;
	}
	public final String getPAYTYPE()
	{
		return _PAYTYPE;
	}
	public final void setPAYTYPE(String value) {
		_PAYTYPE = value;
		IsPAYTYPEPassed = true;
	}
	public final String getPAYNO()
	{
		return _PAYNO;
	}
	public final void setPAYNO(String value) {
		_PAYNO = value;
		IsPAYNOPassed = true;
	}
	public final String getCDPFG()
	{
		return _CDPFG;
	}
	public final void setCDPFG(String value) {
		_CDPFG = value;
		IsCDPFGPassed = true;
	}
	public final String getTInsuIdeNo()
	{
		return _T_INSU_IDE_NO;
	}
	public final void setTInsuIdeNo(String value) {
		_T_INSU_IDE_NO = value;
		IsT_INSU_IDE_NOPassed = true;
	}

	// A2310
	public final String getCCardNo()
	{
		return _CREDIT_CARD_NO;
	}
	public final void setCCardNo(String value) {
		_CREDIT_CARD_NO = value;
		IsCREDIT_CARD_NOPassed = true;
	}

	// A2710
	public final String getOriTxDate()
	{
		return _ORI_TX_DATE;
	}
	public final void setOriTxDate(String value) {
		_ORI_TX_DATE = value;
		IsORI_TX_DATEPassed = true;
	}

	// A1051
	public final String getTForeignerFlg()
	{
		return _T_FOREIGNER_FLG;
	}
	public final void setTForeignerFlg(String value) {
		_T_FOREIGNER_FLG = value;
		IsT_FOREIGNER_FLGPassed = true;
	}

	// A2910
	public final String getTBrSeqno()
	{
		return _T_BR_SEQNO;
	}
	public final void setTBrSeqno(String value) {
		_T_BR_SEQNO = value;
		IsT_BR_SEQNOPassed = true;
	}

	//A2210
	public final String getTSavingChk()
	{
		return _T_SAVING_CHK;
	}
	public final void setTSavingChk(String value) {
		_T_SAVING_CHK = value;
		IsT_SAVING_CHKPassed = true;
	}

	// General 
	public final String getBankCodeDr()
	{
		return _BANK_CODE_DR;
	}
	public final void setBankCodeDr(String value) {
		_BANK_CODE_DR = value;
		IsBANK_CODE_DRPassed = true;
	}

	public final String getAcctTxnDr()
	{
		return _ACCT_TXN_DR;
	}
	public final void setAcctTxnDr(String value) {
		_ACCT_TXN_DR = value;
		IsACCT_TXN_DRPassed = true;
	}

	public final String getBankCodeCr()
	{
		return _BANK_CODE_CR;
	}
	public final void setBankCodeCr(String value) {
		_BANK_CODE_CR = value;
		IsBANK_CODE_CRPassed = true;
	}

	public final String getAcctTxnCr()
	{
		return _ACCT_TXN_CR;
	}
	public final void setAcctTxnCr(String value) {
		_ACCT_TXN_CR = value;
		IsACCT_TXN_CRPassed = true;
	}

	public String getAuthCode()
	{
		return _AUTH_CODE;
	}
	public void setAuthCode(String value) {
		_AUTH_CODE = value;
		IsAUTH_CODEPassed = true;
	}

	public String getTIpAddress()
	{
		return _T_IP_ADDRESS;
	}
	public void setTIpAddress(String value) {
		_T_IP_ADDRESS = value;
		IsT_IP_ADDRESSPassed = true;
	}

	public boolean genDictionary(HashMap<String, String> TITABody) {
		if (IsDEBIT_ACCT_NOPassed) {
			TITABody.put("DEBIT.ACCT.NO", getDebitAcctNo());
		}
		if (IsDEBIT_CURRENCYPassed) {
			TITABody.put("DEBIT.CURRENCY", getDebitCurrency());
		}
		if (IsDEBIT_AMOUNTPassed) {
			TITABody.put("DEBIT.AMOUNT", getDebitAmount().toString());
		}
		if (IsCREDIT_CURRENCYPassed) {
			TITABody.put("CREDIT.CURRENCY", getCreditCurrency());
		}
		if (IsCREDIT_AMOUNTPassed) {
			TITABody.put("CREDIT.AMOUNT", getCreditAmount().toString());
		}
		if (IsEXCH_RATEPassed) {
			TITABody.put("EXCH.RATE", getExchRate().toString());
		}
		if (IsACCR_CHG_AMTPassed) {
			TITABody.put("ACCR.CHG.AMT", getAccrChgAmt().toString());
		}
		if (IsIC_ACTNOPassed) {
			TITABody.put("IC.ACTNO", getIcActno());
		}
		if (IsCREDIT_ACCT_NOPassed) {
			TITABody.put("CREDIT.ACCT.NO", getCreditAcctNo());
		}
		if (IsCHG_ACCOUNTPassed) {
			TITABody.put("CHG.ACCOUNT", getChgAccount());
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

		//2014/11/28 Modify by Ruling for HKMA：T.PSB.REM.F.D有多組MutiValue的值        
		//2020/01/16 Modify by Ruling for WEBATM帶來的【給自己】中文備註：若有多筆Value，先將單筆Value移除再加多筆Value
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
		if (IsT_REG_TFR_TYPEPassed) {
			TITABody.put("T.REG.TFR.TYPE", getTRegTfrType());
		}
		// Dictionary ---------------------------------------------------------------------
		for (Map.Entry<Integer, String> item : getTPsbRinfD().entrySet()) {
			TITABody.put("T.PSB.RINF.D_" + String.valueOf(item.getKey()), item.getValue().toString());
		}

		// A1010
		if (IsFISC_BKNOSTANPassed) {
			TITABody.put("FISC.BKNOSTAN", getFiscBknostan());
		}
		if (IsCHG_CURRENCYPassed) {
			TITABody.put("CHG.CURRENCY", getChgCurrency());
		}

		// A1020
		if (IsFCY_CURRENCYPassed) {
			TITABody.put("FCY.CURRENCY", getFcyCurrency());
		}
		if (IsFCY_AMOUNTPassed) {
			TITABody.put("FCY.AMOUNT", getFcyAmount().toString());
		}

		// A1070    
		if (IsT_PSB_MEMO_CPassed) {
			TITABody.put("T.PSB.MEMO.C", getTPsbMemoC());
		}
		if (IsT_PSB_REM_S_CPassed) {
			TITABody.put("T.PSB.REM.S.C", getTPsbRemSC());
		}
		if (IsT_PSB_REM_F_CPassed) {
			TITABody.put("T.PSB.REM.F.C", getTPsbRemFC());
		}
		//2019/03/19 Modify by Ruling for 中文附言欄
		//2020/01/16 Modify by Ruling for WEBATM帶來的【給自己】中文備註：若有多筆Value，先將單筆Value移除再加多筆Value
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
		// Dictionary ---------------------------------------------------------------------
		for (Map.Entry<Integer, String> item : getTPsbRinfC().entrySet()) {
			TITABody.put("T.PSB.RINF.C_" + String.valueOf(item.getKey()), item.getValue().toString());
		}

		// A2110
		if (IsMBANK_ACTNOPassed) {
			TITABody.put("MBANK.ACTNO", getMbankActno());
		}

		// A1270
		if (IsFEE_PAYERPassed) {
			TITABody.put("FEE.PAYER", getFeePayer());
		}
		if (IsIDNOPassed) {
			TITABody.put("IDNO", getIDNO());
		}
		if (IsVPIDPassed) {
			TITABody.put("VPID", getVPID());
		}
		if (IsPAYTYPEPassed) {
			TITABody.put("PAYTYPE", getPAYTYPE());
		}
		if (IsPAYNOPassed) {
			TITABody.put("PAYNO", getPAYNO());
		}
		if (IsCDPFGPassed) {
			TITABody.put("CDPFG", getCDPFG());
		}
		if (IsT_INSU_IDE_NOPassed) {
			TITABody.put("T.INSU.IDE.NO", getTInsuIdeNo());
		}

		// A2310
		if (IsCREDIT_CARD_NOPassed) {
			TITABody.put("CREDIT.CARD.NO", getCCardNo());
		}

		// A2710
		if (IsORI_TX_DATEPassed) {
			TITABody.put("ORI.TX.DATE", getOriTxDate());
		}

		// A1051
		if (IsT_FOREIGNER_FLGPassed) {
			TITABody.put("T.FOREIGNER.FLG", getTForeignerFlg());
		}

		// A2910
		if (IsT_BR_SEQNOPassed) {
			TITABody.put("T.BR.SEQNO", getTBrSeqno());
		}

		//A2210
		if (IsT_SAVING_CHKPassed) {
			TITABody.put("T.SAVING.CHK", getTSavingChk());
		}

		// General 
		if (IsBANK_CODE_DRPassed) {
			TITABody.put("BANK.CODE.DR", getBankCodeDr());
		}

		if (IsACCT_TXN_DRPassed) {
			TITABody.put("ACCT.TXN.DR", getAcctTxnDr());
		}

		if (IsBANK_CODE_CRPassed) {
			TITABody.put("BANK.CODE.CR", getBankCodeCr());
		}

		if (IsACCT_TXN_CRPassed) {
			TITABody.put("ACCT.TXN.CR", getAcctTxnCr());
		}

		if (IsACCT_TXN_CRPassed) {
			TITABody.put("AUTH.CODE", getAuthCode());
		}

		if (IsT_IP_ADDRESSPassed) {
			TITABody.put("T.IP.ADDRESS", getTIpAddress());
		}

		//A1020/A1470
		if (IsPLUS_DATEPassed) {
			TITABody.put("PLUS.DATE", getPlusDate());
		}
		if (IsT_CRS_COUNTRYPassed) {
			TITABody.put("T.CRS.COUNTRY", getTCrsCountry());
		}

		return true;
	}

}
