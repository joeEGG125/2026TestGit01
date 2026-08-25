package com.syscom.fep.base.cnst.rcode;

import com.syscom.fep.base.enums.FEPReturnCode;

public interface ENCReturnCode {
	/**
	 * 呼叫encHelper發生異常(15000)
	 */
	public static FEPReturnCode ENCLibError = FEPReturnCode.ENCLibError;
	/**
	 * 客戶亂碼基碼不同步(15001)
	 */
	public static FEPReturnCode ENCCheckPPKeyError = FEPReturnCode.ENCCheckPPKeyError;
	/**
	 * 訊息押碼錯誤(15002)
	 */
	public static FEPReturnCode ENCCheckMACError = FEPReturnCode.ENCCheckMACError;
	/**
	 * 交易驗證碼檢核錯誤(15003)
	 */
	public static FEPReturnCode ENCCheckTACError = FEPReturnCode.ENCCheckTACError;
	/**
	 * 檢核密碼錯誤(15004)
	 */
	public static FEPReturnCode ENCCheckPasswordError = FEPReturnCode.ENCCheckPasswordError;
	/**
	 * ATM電文轉換 PIN BLOCK至信用卡電文失敗(15005)
	 */
	public static FEPReturnCode ENCPINBlockConvertError = FEPReturnCode.ENCPINBlockConvertError;
	/**
	 * 呼叫ENCHelper所傳入的引數檢查失敗(15006)
	 */
	public static FEPReturnCode ENCArgumentError = FEPReturnCode.ENCArgumentError;
	/**
	 * 檢核MAC或TAC錯誤(15007)
	 */
	public static FEPReturnCode ENCCheckMACTACError = FEPReturnCode.ENCCheckMACTACError;
	/**
	 * 變更密碼失敗(15008)
	 */
	public static FEPReturnCode ENCChangePasswordError = FEPReturnCode.ENCChangePasswordError;
	/**
	 * 換 PP KEY失敗(15009)
	 */
	public static FEPReturnCode ENCChangePPKeyError = FEPReturnCode.ENCChangePPKeyError;
	/**
	 * 產生MAC失敗(15010)
	 */
	public static FEPReturnCode ENCMakeMACError = FEPReturnCode.ENCMakeMACError;
	/**
	 * 產生TerminalAuthen失敗(15011)
	 */
	public static FEPReturnCode ENCMakeTerminalAuthenError = FEPReturnCode.ENCMakeTerminalAuthenError;
	/**
	 * 更新PPKey失敗(15012)
	 */
	public static FEPReturnCode ENCUpdatePPKeyError = FEPReturnCode.ENCUpdatePPKeyError;
	/**
	 * 產生全繳API交易加密失敗(15013)
	 */
	public static FEPReturnCode ENCEncrAPIError = FEPReturnCode.ENCEncrAPIError;
	/**
	 * 轉換全繳API交易加密失敗(15014)
	 */
	public static FEPReturnCode ENCTrenAPIError = FEPReturnCode.ENCTrenAPIError;
	/**
	 * 產生Paytax繳稅交易加密失敗(15019)
	 */
	public static FEPReturnCode ENCEncPTAXError = FEPReturnCode.ENCEncPTAXError;
	/**
	 * 產生Paytax繳稅交易解密失敗(15020)
	 */
	public static FEPReturnCode ENCDecPTAXError = FEPReturnCode.ENCDecPTAXError;
}
