package com.syscom.fep.configuration;

import java.util.List;

import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.mybatis.model.Sysconf;

public class ATMPConfig {
	public static int SubSystemNo = 3;
	private static ATMPConfig _instance = new ATMPConfig();
	private String _AD;
	private int _ADMLimit;
	private String _Atmmurl;
	private String _AppServiceUrl;
	private int _ATMAlertLimit;
	private String _ATMAlertMailFrom;
	private String _ATMAlertMailTo;
	private String _ATMAlertMessage;
	private String _ATMAlertWorkingDir;
	private String _ATMCBatchHKOutput;
	private String _ATMCBatchMOOutput;
	private String _ATMCBatchTWOutput;
	private double _BDMFee;
	private String _BDMFeeEffectDate;
	private int _CardRetryCount;
	private String _ChangeDateTime;
	private double _CNYRateDisc;
	private int _CNYTxLimit;
	private String _CreditATMNo;
	private String _CreditCash_COB_Output;
	private String _CreditCashOutput;
	private String _CreditMACKey;
	private String _CreditOption_COB_Output;
	private String _CreditOptionOutput;
	private String _CreditPINKey;
	private int _CrossChargeHKD;
	private int _CrossChargeMOP;
	private int _CrossChargeTWD;
	private String _DAppServiceUrl;
	private int _DGFTTxLimit;
	private int _ForeignDept;
	private int _FTLimit;
	private String _FunAppServiceUrl;
	private int _GetListRateFreq;
	private String _GetListRateStart;
	private String _GetListRateStop;
	private int _HeadOffice;
	private String _HK2410FeeAccount;
	private String _HKDCompanyCode;
	private String _HKDept;
	private String _HKDFeeAccount;
	private double _HKDMOPRate;
	private double _HKDRateDisc;
	private int _HKDTxLimit;
	private String _HKT24Sscode;
	private String _HKT24ServiceUrl;
	private String _HKT24UserName;
	private String _INVBank;
	private String _INVTroutAcct;
	private String _INVTroutBank;
	private String _IVRMACKey;
	private String _IVRPINKey;
	private double _JPYRateDisc;
	private int _JPYTxLimit;
	private String _MO2410FeeAccount;
	private String _MODept;
	private String _MONATMMailTo;
	private String _MOPCompanyCode;
	private String _MOPFeeAccount;
	private String _MOT24Sscode;
	private String _MOT24ServiceUrl;
	private String _MOT24UserName;
	private int _NCCNYAmtValue;
	private int _NCCNYLimit;
	private String _NCCustTime;
	private int _NCHKDAmtValue;
	private int _NCHKDLimit;
	private int _NCJPYAmtValue;
	private int _NCJPYLimit;
	private int _NCLimit;
	private int _NCTWDAmtValue;
	private int _NCUSDAmtValue;
	private int _NCUSDLimit;
	private int _NWDErrCount;
	private int _OEXCompareCount;
	private String _OEXLogInsertType;
	private String _OutATMIntActno;
	private int _ProcessCenter;
	private String _PVServiceUrl;
	private String _SVCSIntActno;
	private String _SVCSOutActno;
	private String _T24Sscode;
	private String _T24UserName;
	private String _TPBank;
	private String _TPMemo;
	private String _TWDCompanyCode;
	private int _TWDTxLimit;
	private double _USDRateDisc;
	private int _USDTxLimit;
	private int _INBKIDLimit;
	private String _ATMFIDOSecretKey;
	private String _FIDOServiceUrl;
	private String _ATMGrayListSecretKey;
	private String _CashDistributionUrl;

	private ATMPConfig() {
		fillDataToProperty();
	}

	/**
	 * 廣告促銷
	 * 
	 * @return
	 */
	public String getAD() {
		return _AD;
	}

	public void setAD(String value) {
		_AD = value;
	}

	/**
	 * 存提款機單筆限額
	 * 
	 * @return
	 */
	public int getADMLimit() {
		return _ADMLimit;
	}

	/**
	 * 無卡提款網銀推播用網址
	 * 
	 * @return
	 */
	public String getAppServiceUrl() {
		return _AppServiceUrl;
	}
	
	public String getAtmmurl() {
		return _Atmmurl;
	}

	/**
	 * ATM警示限額百分比
	 * 
	 * @return
	 */
	public int getATMAlertLimit() {
		return _ATMAlertLimit;
	}

	/**
	 * ATM警示寄件者Mail信箱
	 * 
	 * @return
	 */
	public String getATMAlertMailFrom() {
		return _ATMAlertMailFrom;
	}

	/**
	 * ATM警示收件者Mail信箱
	 * 
	 * @return
	 */
	public String getATMAlertMailTo() {
		return _ATMAlertMailTo;
	}

	/**
	 * ATM警示內容
	 * 
	 * @return
	 */
	public String getATMAlertMessage() {
		return _ATMAlertMessage;
	}

	/**
	 * ATM警示音程式放置的目錄
	 * 
	 * @return
	 */
	public String getATMAlertWorkingDir() {
		return _ATMAlertWorkingDir;
	}

	/**
	 * ATMCBatch香港輸出檔名
	 * 
	 * @return
	 */
	public String getATMCBatchHKOutput() {
		return _ATMCBatchHKOutput;
	}

	/**
	 * ATMCBatch澳門輸出檔名
	 * 
	 * @return
	 */
	public String getATMCBatchMOOutput() {
		return _ATMCBatchMOOutput;
	}

	/**
	 * ATMCBatch台灣輸出檔名
	 * 
	 * @return
	 */
	public String getATMCBatchTWOutput() {
		return _ATMCBatchTWOutput;
	}

	/**
	 * 菓菜市場現金存款加收手續費金額
	 * 
	 * @return
	 */
	public double getBDMFee() {
		return _BDMFee;
	}

	/**
	 * 菓菜市場現金存款加收手續費生效日
	 * 
	 * @return
	 */
	public String getBDMFeeEffectDate() {
		return _BDMFeeEffectDate;
	}

	/**
	 * 密碼錯誤重試次數(目前為 4次)
	 * 
	 * @return
	 */
	public int getCardRetryCount() {
		return _CardRetryCount;
	}

	/**
	 * 換日時間
	 * 
	 * @return
	 */
	public String getChangeDateTime() {
		return _ChangeDateTime;
	}

	/**
	 * 人民幣匯率優惠
	 * 
	 * @return
	 */
	public double getCNYRateDisc() {
		return _CNYRateDisc;
	}

	/**
	 * 自行提領人民幣單筆限額
	 * 
	 * @return
	 */
	public int getCNYTxLimit() {
		return _CNYTxLimit;
	}

	/**
	 * 送信用卡主機的ATM代號
	 * 
	 * @return
	 */
	public String getCreditATMNo() {
		return _CreditATMNo;
	}

	/**
	 * CreditCash_COB輸出檔名
	 * 
	 * @return
	 */
	public String getCreditCashCOBOutput() {
		return _CreditCash_COB_Output;
	}

	/**
	 * CreditCash輸出檔名
	 * 
	 * @return
	 */
	public String getCreditCashOutput() {
		return _CreditCashOutput;
	}

	/**
	 * 信用卡 MAC KEY代號
	 * 
	 * @return
	 */
	public String getCreditMACKey() {
		return _CreditMACKey;
	}

	/**
	 * CreditOption_COB輸出檔名
	 * 
	 * @return
	 */
	public String getCreditOptionCOBOutput() {
		return _CreditOption_COB_Output;
	}

	/**
	 * CreditOption輸出檔名
	 * 
	 * @return
	 */
	public String getCreditOptionOutput() {
		return _CreditOptionOutput;
	}

	/**
	 * 信用卡 PIN KEY代號
	 * 
	 * @return
	 */
	public String getCreditPINKey() {
		return _CreditPINKey;
	}

	/**
	 * 香港跨區提款手續費
	 * 
	 * @return
	 */
	public int getCrossChargeHKD() {
		return _CrossChargeHKD;
	}

	/**
	 * 澳門跨區提款手續費
	 * 
	 * @return
	 */
	public int getCrossChargeMOP() {
		return _CrossChargeMOP;
	}

	/**
	 * 跨區提款手續費(BY地區/ 幣別) 台灣卡:台幣 80元 香港卡;港幣 20元 澳門卡:葡幣 20元
	 * 
	 * @return
	 */
	public int getCrossChargeTWD() {
		return _CrossChargeTWD;
	}

	/**
	 * 大戶無卡提款網銀推播用網址
	 * 
	 * @return
	 */
	public String getDAppServiceUrl() {
		return _DAppServiceUrl;
	}

	/**
	 * 數三之2轉帳單筆限額
	 * 
	 * @return
	 */
	public int getDGFTTxLimit() {
		return _DGFTTxLimit;
	}

	/**
	 * 國外部分行代號
	 * 
	 * @return
	 */
	public int getForeignDept() {
		return _ForeignDept;
	}

	/**
	 * 自行轉帳單筆限額
	 * 
	 * @return
	 */
	public int getFTLimit() {
		return _FTLimit;
	}

	/**
	 * 豐錢包APP交易結果網址
	 * 
	 * @return
	 */
	public String getFunAppServiceUrl() {
		return _FunAppServiceUrl;
	}

	/**
	 * 取敲價間隔時間(秒)
	 * 
	 * @return
	 */
	public int getGetListRateFreq() {
		return _GetListRateFreq;
	}

	public void setGetListRateFreq(int value) {
		_GetListRateFreq = value;
	}

	/**
	 * 敲價開始時間
	 * 
	 * @return
	 */
	public String getGetListRateStart() {
		return _GetListRateStart;
	}

	public void setGetListRateStart(String value) {
		_GetListRateStart = value;
	}

	/**
	 * 敲價結束時間
	 * 
	 * @return
	 */
	public String getGetListRateStop() {
		return _GetListRateStop;
	}

	public void setGetListRateStop(String value) {
		_GetListRateStop = value;
	}

	/**
	 * 總行代號
	 * 
	 * @return
	 */
	public int getHeadOffice() {
		return _HeadOffice;
	}

	/**
	 * 香港國際提款手續費帳號
	 * 
	 * @return
	 */
	public String getHK2410FeeAccount() {
		return _HK2410FeeAccount;
	}

	/**
	 * HK T24 Company Code
	 * 
	 * @return
	 */
	public String getHKDCompanyCode() {
		return _HKDCompanyCode;
	}

	/**
	 * 香港分行代號
	 * 
	 * @return
	 */
	public String getHKDept() {
		return _HKDept;
	}

	/**
	 * 香港手續費帳號
	 * 
	 * @return
	 */
	public String getHKDFeeAccount() {
		return _HKDFeeAccount;
	}

	/**
	 * 港幣對葡幣的比值1:1.03
	 * 
	 * @return
	 */
	public double getHKDMOPRate() {
		return _HKDMOPRate;
	}

	/**
	 * 港幣匯率優惠
	 * 
	 * @return
	 */
	public double getHKDRateDisc() {
		return _HKDRateDisc;
	}

	/**
	 * 自行提領港幣單筆限額
	 * 
	 * @return
	 */
	public int getHKDTxLimit() {
		return _HKDTxLimit;
	}

	/**
	 * T24上行電文內的  (HK)
	 * 
	 * @return
	 */
	public String getHKT24Password() {
		return _HKT24Sscode;
	}

	/**
	 * T24 WebService Url(HK)
	 * 
	 * @return
	 */
	public String getHKT24ServiceUrl() {
		return _HKT24ServiceUrl;
	}

	/**
	 * T24上行電文內的 User Name(HK)
	 * 
	 * @return
	 */
	public String getHKT24UserName() {
		return _HKT24UserName;
	}

	/**
	 * 統一發票代理單位
	 * 
	 * @return
	 */
	public String getINVBank() {
		return _INVBank;
	}

	/**
	 * 統一發票轉出帳號
	 * 
	 * @return
	 */
	public String getINVTroutAcct() {
		return _INVTroutAcct;
	}

	/**
	 * 統一發票轉出行
	 * 
	 * @return
	 */
	public String getINVTroutBank() {
		return _INVTroutBank;
	}

	/**
	 * 語音 MAC KEY代號
	 * 
	 * @return
	 */
	public String getIVRMACKey() {
		return _IVRMACKey;
	}

	/**
	 * 語音 PIN KEY代號
	 * 
	 * @return
	 */
	public String getIVRPINKey() {
		return _IVRPINKey;
	}

	/**
	 * 日圓匯率優惠
	 * 
	 * @return
	 */
	public double getJPYRateDisc() {
		return _JPYRateDisc;
	}

	/**
	 * 自行提領日元單筆限額
	 * 
	 * @return
	 */
	public int getJPYTxLimit() {
		return _JPYTxLimit;
	}

	/**
	 * 澳門國際提款手續費帳號
	 * 
	 * @return
	 */
	public String getMO2410FeeAccount() {
		return _MO2410FeeAccount;
	}

	/**
	 * 澳門分行代號
	 * 
	 * @return
	 */
	public String getMODept() {
		return _MODept;
	}

	/**
	 * 監控ATM故障狀態收件者Mail信箱
	 * 
	 * @return
	 */
	public String getMONATMMailTo() {
		return _MONATMMailTo;
	}

	/**
	 * MO T24 Company Code
	 * 
	 * @return
	 */
	public String getMOPCompanyCode() {
		return _MOPCompanyCode;
	}

	/**
	 * 澳門手續費帳號
	 * 
	 * @return
	 */
	public String getMOPFeeAccount() {
		return _MOPFeeAccount;
	}

	/**
	 * T24上行電文內的  (MO)
	 * 
	 * @return
	 */
	public String getMOT24Password() {
		return _MOT24Sscode;
	}

	/**
	 * T24 WebService Url(MO)
	 * 
	 * @return
	 */
	public String getMOT24ServiceUrl() {
		return _MOT24ServiceUrl;
	}

	/**
	 * T24上行電文內的 User Name(MO)
	 * 
	 * @return
	 */
	public String getMOT24UserName() {
		return _MOT24UserName;
	}

	/**
	 * 無卡人民幣提款面額
	 * 
	 * @return
	 */
	public int getNCCNYAmtValue() {
		return _NCCNYAmtValue;
	}

	/**
	 * 無卡人民幣提款單筆限額
	 * 
	 * @return
	 */
	public int getNCCNYLimit() {
		return _NCCNYLimit;
	}

	/**
	 * 無卡提款交易檢核前一日的截止時間(hhmmss)
	 * 
	 * @return
	 */
	public String getNCCustTime() {
		return _NCCustTime;
	}

	/**
	 * 無卡港幣提款面額
	 * 
	 * @return
	 */
	public int getNCHKDAmtValue() {
		return _NCHKDAmtValue;
	}

	/**
	 * 無卡港幣提款單筆限額
	 * 
	 * @return
	 */
	public int getNCHKDLimit() {
		return _NCHKDLimit;
	}

	/**
	 * 無卡日幣提款面額
	 * 
	 * @return
	 */
	public int getNCJPYAmtValue() {
		return _NCJPYAmtValue;
	}

	/**
	 * 無卡日幣提款單筆限額
	 * 
	 * @return
	 */
	public int getNCJPYLimit() {
		return _NCJPYLimit;
	}

	/**
	 * 無卡提款單筆限額
	 * 
	 * @return
	 */
	public int getNCLimit() {
		return _NCLimit;
	}

	/**
	 * 無卡台幣提款面額
	 * 
	 * @return
	 */
	public int getNCTWDAmtValue() {
		return _NCTWDAmtValue;
	}

	/**
	 * 無卡美金提款面額
	 * 
	 * @return
	 */
	public int getNCUSDAmtValue() {
		return _NCUSDAmtValue;
	}

	/**
	 * 無卡美金提款單筆限額
	 * 
	 * @return
	 */
	public int getNCUSDLimit() {
		return _NCUSDLimit;
	}

	/**
	 * 無卡提款密碼錯誤次數(目前為 5次)
	 * 
	 * @return
	 */
	public int getNWDErrCount() {
		return _NWDErrCount;
	}

	/**
	 * OEX電文比對新台幣仟元剩餘張數不合發送EMAIL
	 * 
	 * @return
	 */
	public int getOEXCompareCount() {
		return _OEXCompareCount;
	}

	/**
	 * OEXLog寫入資料方式(0:全部 1:失敗)
	 * 
	 * @return
	 */
	public String getOEXLogInsertType() {
		return _OEXLogInsertType;
	}

	/**
	 * 行外ATM庫現內部帳號
	 * 
	 * @return
	 */
	public String getOutATMIntActno() {
		return _OutATMIntActno;
	}

	/**
	 * 綜作分行代號
	 * 
	 * @return
	 */
	public int getProcessCenter() {
		return _ProcessCenter;
	}

	/**
	 * 掌靜脈API網址
	 * 
	 * @return
	 */
	public String getPVServiceUrl() {
		return _PVServiceUrl;
	}

	/**
	 * 悠遊 Debit卡自動加值內部帳號
	 * 
	 * @return
	 */
	public String getSVCSIntActno() {
		return _SVCSIntActno;
	}

	/**
	 * 悠遊 Debit卡餘額轉置內部帳號
	 * 
	 * @return
	 */
	public String getSVCSOutActno() {
		return _SVCSOutActno;
	}

	/**
	 * T24上行電文內的  (ATM)
	 * 
	 * @return
	 */
	public String getT24Password() {
		return _T24Sscode;
	}

	/**
	 * T24上行電文內的 User Name(ATM)
	 * 
	 * @return
	 */
	public String getT24UserName() {
		return _T24UserName;
	}

	/**
	 * 多元加值服務代理單位
	 * 
	 * @return
	 */
	public String getTPBank() {
		return _TPBank;
	}

	/**
	 * 即時性代發業務存摺備註
	 * 
	 * @return
	 */
	public String getTPMemo() {
		return _TPMemo;
	}

	/**
	 * T24 Company Code
	 * 
	 * @return
	 */
	public String getTWDCompanyCode() {
		return _TWDCompanyCode;
	}

	/**
	 * 自行提領台幣單筆限額
	 * 
	 * @return
	 */
	public int getTWDTxLimit() {
		return _TWDTxLimit;
	}

	/**
	 * 美元匯率優惠
	 * 
	 * @return
	 */
	public double getUSDRateDisc() {
		return _USDRateDisc;
	}

	/**
	 * 自行提領美金單筆限額
	 * 
	 * @return
	 */
	public int getUSDTxLimit() {
		return _USDTxLimit;
	}

	public static ATMPConfig getInstance() {
		return _instance;
	}
	
	/**
	 * 跨行全國繳費ID+Account單筆限額
	 * 
	 * @return
	 */
	public final int getINBKIDLimit() {
		return _INBKIDLimit;
	}
	/**
	 * 送FIDO用SecretKey
	 * 
	 * @return
	 */
	public String getATMFIDOSecretKey() {
		return _ATMFIDOSecretKey;
	}
	/**
	 * 送FIDO用FIDOServiceUrl
	 *
	 * @return
	 */
	public String getFIDOServiceUrl() {
		return _FIDOServiceUrl;
	}

	public String getATMGrayListSecretKey() {
		return _ATMGrayListSecretKey;
	}

	public String getCashDistributionUrl() {
		return _CashDistributionUrl;
	}

	private void fillDataToProperty() {
		List<Sysconf> sysconfList = FEPCache.getSysconfList(SubSystemNo);
		String sysconfValue = null;
		for (Sysconf sysconf : sysconfList) {
			sysconfValue = sysconf.getSysconfValue();
			switch (sysconf.getSysconfName()) {
				case "AD":
					_AD = DbHelper.toString(sysconfValue);
					break;
				case "ADMLimit":
					_ADMLimit = DbHelper.toInteger(sysconfValue);
					break;
				case "AppServiceUrl":
					_AppServiceUrl = DbHelper.toString(sysconfValue);
					break;
				case "ATMMUrl":
					_Atmmurl = DbHelper.toString(sysconfValue);
					break;	
				case "ATMAlertLimit":
					_ATMAlertLimit = DbHelper.toInteger(sysconfValue);
					break;
				case "ATMAlertMailFrom":
					_ATMAlertMailFrom = DbHelper.toString(sysconfValue);
					break;
				case "ATMAlertMailTo":
					_ATMAlertMailTo = DbHelper.toString(sysconfValue);
					break;
				case "ATMAlertMessage":
					_ATMAlertMessage = DbHelper.toString(sysconfValue);
					break;
				case "ATMAlertWorkingDir":
					_ATMAlertWorkingDir = DbHelper.toString(sysconfValue);
					break;
				case "ATMCBatchHKOutput":
					_ATMCBatchHKOutput = DbHelper.toString(sysconfValue);
					break;
				case "ATMCBatchMOOutput":
					_ATMCBatchMOOutput = DbHelper.toString(sysconfValue);
					break;
				case "ATMCBatchTWOutput":
					_ATMCBatchTWOutput = DbHelper.toString(sysconfValue);
					break;
				case "BDMFee":
					_BDMFee = DbHelper.toDouble(sysconfValue);
					break;
				case "BDMFeeEffectDate":
					_BDMFeeEffectDate = DbHelper.toString(sysconfValue);
					break;
				case "CardRetryCount":
					_CardRetryCount = DbHelper.toInteger(sysconfValue);
					break;
				case "ChangeDateTime":
					_ChangeDateTime = DbHelper.toString(sysconfValue);
					break;
				case "CNYRateDisc":
					_CNYRateDisc = DbHelper.toDouble(sysconfValue);
					break;
				case "CNYTxLimit":
					_CNYTxLimit = DbHelper.toInteger(sysconfValue);
					break;
				case "CreditATMNo":
					_CreditATMNo = DbHelper.toString(sysconfValue);
					break;
				case "CreditCash_COB_Output":
					_CreditCash_COB_Output = DbHelper.toString(sysconfValue);
					break;
				case "CreditCashOutput":
					_CreditCashOutput = DbHelper.toString(sysconfValue);
					break;
				case "CreditMACKey":
					_CreditMACKey = DbHelper.toString(sysconfValue);
					break;
				case "CreditOption_COB_Output":
					_CreditOption_COB_Output = DbHelper.toString(sysconfValue);
					break;
				case "CreditOptionOutput":
					_CreditOptionOutput = DbHelper.toString(sysconfValue);
					break;
				case "CreditPINKey":
					_CreditPINKey = DbHelper.toString(sysconfValue);
					break;
				case "CrossChargeHKD":
					_CrossChargeHKD = DbHelper.toInteger(sysconfValue);
					break;
				case "CrossChargeMOP":
					_CrossChargeMOP = DbHelper.toInteger(sysconfValue);
					break;
				case "CrossChargeTWD":
					_CrossChargeTWD = DbHelper.toInteger(sysconfValue);
					break;
				case "DAppServiceUrl":
					_DAppServiceUrl = DbHelper.toString(sysconfValue);
					break;
				case "DGFTTxLimit":
					_DGFTTxLimit = DbHelper.toInteger(sysconfValue);
					break;
				case "ForeignDept":
					_ForeignDept = DbHelper.toInteger(sysconfValue);
					break;
				case "FTLimit":
					_FTLimit = DbHelper.toInteger(sysconfValue);
					break;
				case "FunAppServiceUrl":
					_FunAppServiceUrl = DbHelper.toString(sysconfValue);
					break;
				case "GetListRateFreq":
					_GetListRateFreq = DbHelper.toInteger(sysconfValue);
					break;
				case "GetListRateStart":
					_GetListRateStart = DbHelper.toString(sysconfValue);
					break;
				case "GetListRateStop":
					_GetListRateStop = DbHelper.toString(sysconfValue);
					break;
				case "HeadOffice":
					_HeadOffice = DbHelper.toInteger(sysconfValue);
					break;
				case "HK2410FeeAccount":
					_HK2410FeeAccount = DbHelper.toString(sysconfValue);
					break;
				case "HKDCompanyCode":
					_HKDCompanyCode = DbHelper.toString(sysconfValue);
					break;
				case "HKDept":
					_HKDept = DbHelper.toString(sysconfValue);
					break;
				case "HKDFeeAccount":
					_HKDFeeAccount = DbHelper.toString(sysconfValue);
					break;
				case "HKDMOPRate":
					_HKDMOPRate = DbHelper.toDouble(sysconfValue);
					break;
				case "HKDRateDisc":
					_HKDRateDisc = DbHelper.toDouble(sysconfValue);
					break;
				case "HKDTxLimit":
					_HKDTxLimit = DbHelper.toInteger(sysconfValue);
					break;
				case "HKT24Password":
					_HKT24Sscode = DbHelper.toString(sysconfValue);
					break;
				case "HKT24ServiceUrl":
					_HKT24ServiceUrl = DbHelper.toString(sysconfValue);
					break;
				case "HKT24UserName":
					_HKT24UserName = DbHelper.toString(sysconfValue);
					break;
				case "INVBank":
					_INVBank = DbHelper.toString(sysconfValue);
					break;
				case "INVTroutAcct":
					_INVTroutAcct = DbHelper.toString(sysconfValue);
					break;
				case "INVTroutBank":
					_INVTroutBank = DbHelper.toString(sysconfValue);
					break;
				case "IVRMACKey":
					_IVRMACKey = DbHelper.toString(sysconfValue);
					break;
				case "IVRPINKey":
					_IVRPINKey = DbHelper.toString(sysconfValue);
					break;
				case "JPYRateDisc":
					_JPYRateDisc = DbHelper.toDouble(sysconfValue);
					break;
				case "JPYTxLimit":
					_JPYTxLimit = DbHelper.toInteger(sysconfValue);
					break;
				case "MO2410FeeAccount":
					_MO2410FeeAccount = DbHelper.toString(sysconfValue);
					break;
				case "MODept":
					_MODept = DbHelper.toString(sysconfValue);
					break;
				case "MONATMMailTo":
					_MONATMMailTo = DbHelper.toString(sysconfValue);
					break;
				case "MOPCompanyCode":
					_MOPCompanyCode = DbHelper.toString(sysconfValue);
					break;
				case "MOPFeeAccount":
					_MOPFeeAccount = DbHelper.toString(sysconfValue);
					break;
				case "MOT24Password":
					_MOT24Sscode = DbHelper.toString(sysconfValue);
					break;
				case "MOT24ServiceUrl":
					_MOT24ServiceUrl = DbHelper.toString(sysconfValue);
					break;
				case "MOT24UserName":
					_MOT24UserName = DbHelper.toString(sysconfValue);
					break;
				case "NCCNYAmtValue":
					_NCCNYAmtValue = DbHelper.toInteger(sysconfValue);
					break;
				case "NCCNYLimit":
					_NCCNYLimit = DbHelper.toInteger(sysconfValue);
					break;
				case "NCCustTime":
					_NCCustTime = DbHelper.toString(sysconfValue);
					break;
				case "NCHKDAmtValue":
					_NCHKDAmtValue = DbHelper.toInteger(sysconfValue);
					break;
				case "NCHKDLimit":
					_NCHKDLimit = DbHelper.toInteger(sysconfValue);
					break;
				case "NCJPYAmtValue":
					_NCJPYAmtValue = DbHelper.toInteger(sysconfValue);
					break;
				case "NCJPYLimit":
					_NCJPYLimit = DbHelper.toInteger(sysconfValue);
					break;
				case "NCLimit":
					_NCLimit = DbHelper.toInteger(sysconfValue);
					break;
				case "NCTWDAmtValue":
					_NCTWDAmtValue = DbHelper.toInteger(sysconfValue);
					break;
				case "NCUSDAmtValue":
					_NCUSDAmtValue = DbHelper.toInteger(sysconfValue);
					break;
				case "NCUSDLimit":
					_NCUSDLimit = DbHelper.toInteger(sysconfValue);
					break;
				case "NWDErrCount":
					_NWDErrCount = DbHelper.toInteger(sysconfValue);
					break;
				case "OEXCompareCount":
					_OEXCompareCount = DbHelper.toInteger(sysconfValue);
					break;
				case "OEXLogInsertType":
					_OEXLogInsertType = DbHelper.toString(sysconfValue);
					break;
				case "OutATMIntActno":
					_OutATMIntActno = DbHelper.toString(sysconfValue);
					break;
				case "ProcessCenter":
					_ProcessCenter = DbHelper.toInteger(sysconfValue);
					break;
				case "PVServiceUrl":
					_PVServiceUrl = DbHelper.toString(sysconfValue);
					break;
				case "SVCSIntActno":
					_SVCSIntActno = DbHelper.toString(sysconfValue);
					break;
				case "SVCSOutActno":
					_SVCSOutActno = DbHelper.toString(sysconfValue);
					break;
				case "T24Password":
					_T24Sscode = DbHelper.toString(sysconfValue);
					break;
				case "T24UserName":
					_T24UserName = DbHelper.toString(sysconfValue);
					break;
				case "TPBank":
					_TPBank = DbHelper.toString(sysconfValue);
					break;
				case "TPMemo":
					_TPMemo = DbHelper.toString(sysconfValue);
					break;
				case "TWDCompanyCode":
					_TWDCompanyCode = DbHelper.toString(sysconfValue);
					break;
				case "TWDTxLimit":
					_TWDTxLimit = DbHelper.toInteger(sysconfValue);
					break;
				case "USDRateDisc":
					_USDRateDisc = DbHelper.toDouble(sysconfValue);
					break;
				case "USDTxLimit":
					_USDTxLimit = DbHelper.toInteger(sysconfValue);
					break;
				case "INBKIDLimit":
					_INBKIDLimit = DbHelper.toInteger(sysconfValue);
					break;	
				case "ATMFIDOSecretKey":
					_ATMFIDOSecretKey = DbHelper.toString(sysconfValue);
					break;
				case "FIDOServiceUrl":
					_FIDOServiceUrl = DbHelper.toString(sysconfValue);
					break;
				case "ATMGrayListSecretKey":
					_ATMGrayListSecretKey = DbHelper.toString(sysconfValue);
					break;
				case "CashDistributionUrl":
					_CashDistributionUrl = DbHelper.toString(sysconfValue);
					break;
			}
		}
	}
}
