package com.syscom.fep.vo.enums;

import com.syscom.fep.base.enums.FEPReturnCode;

public interface ATMReturnCode {
	/**
	 * ATM無此交易，現金存款功能未支援(13001)
	 */
	public static FEPReturnCode ATMNotSupportDepositTX = FEPReturnCode.ATMNotSupportDepositTX;
	/**
	 * 無此ATM代號(13002)
	 */
	public static FEPReturnCode ATMNoIsNotExist = FEPReturnCode.ATMNoIsNotExist;
	/**
	 * 日期錯誤，不更新磁軌且不留置卡片(13003)
	 */
	public static FEPReturnCode TBSDYError = FEPReturnCode.TBSDYError;
	/**
	 * ATM MODE與地區檔的MODE是否不符(13004)
	 */
	public static FEPReturnCode ATMModeError = FEPReturnCode.ATMModeError;
	/**
	 * 非MMA金融信用卡，無法執行交易(13005)
	 */
	public static FEPReturnCode CBSModeIs5 = FEPReturnCode.CBSModeIs5;
	/**
	 * 卡片已註銷，不更新磁軌且不留置卡片(13101)
	 */
	public static FEPReturnCode CardCancelled = FEPReturnCode.CardCancelled;
	/**
	 * 失效卡片，不更新磁軌且留置卡片(13102)
	 */
	public static FEPReturnCode CardLoseEfficacy = FEPReturnCode.CardLoseEfficacy;
	/**
	 * 卡片尚未生效(13103)
	 */
	public static FEPReturnCode CardNotEffective = FEPReturnCode.CardNotEffective;
	/**
	 * 卡片已掛失(13104)
	 */
	public static FEPReturnCode CardLost = FEPReturnCode.CardLost;
	/**
	 * 檢核卡片Track2 BIN異常(13105)
	 */
	public static FEPReturnCode Track2Error = FEPReturnCode.Track2Error;
	/**
	 * 晶片卡備註欄位檢核有誤(13111)
	 */
	public static FEPReturnCode ICMARKError = FEPReturnCode.ICMARKError;
	/**
	 * 晶片卡交易序號重複(13112)
	 */
	public static FEPReturnCode ICSeqNoDuplicate = FEPReturnCode.ICSeqNoDuplicate;
	/**
	 * 晶片卡交易序號錯誤，回發卡單位處理(13113)
	 */
	public static FEPReturnCode ICSeqNoError = FEPReturnCode.ICSeqNoError;
	/**
	 * 金融信用卡狀態不合，無法交易(13121)
	 */
	public static FEPReturnCode ComboCardStatusNotMatch = FEPReturnCode.ComboCardStatusNotMatch;
	/**
	 * 金融信用卡未啟用(13122)
	 */
	public static FEPReturnCode ComboCardNotEffective = FEPReturnCode.ComboCardNotEffective;
	/**
	 * 尚未申請國際卡功能(13123)
	 */
	public static FEPReturnCode PlusCirrusNotApply = FEPReturnCode.PlusCirrusNotApply;
	/**
	 * 悠遊卡號錯誤(13124)
	 */
	public static FEPReturnCode EZCardError = FEPReturnCode.EZCardError;
	/**
	 * 問題帳戶，不更新磁軌且不留置卡片(13201)
	 */
	public static FEPReturnCode ACTNOError = FEPReturnCode.ACTNOError;
	/**
	 * 轉出入帳號相同，不更新磁軌且不留置卡片(13202)
	 */
	public static FEPReturnCode TranInACTNOSameAsTranOut = FEPReturnCode.TranInACTNOSameAsTranOut;
	/**
	 * 轉入帳號狀況檢核錯誤(13203)
	 */
	public static FEPReturnCode TranInACTNOError = FEPReturnCode.TranInACTNOError;
	/**
	 * 轉入帳戶已結清，不更新磁軌且不留置卡片(13204)
	 */
	public static FEPReturnCode TranInACTNOClosed = FEPReturnCode.TranInACTNOClosed;
	/**
	 * 該帳戶不允許做ATM存款或提領等交易(13205)
	 */
	public static FEPReturnCode ACTNONotAllowCashTX = FEPReturnCode.ACTNONotAllowCashTX;
	/**
	 * 該帳戶未申請轉帳約定(13206)
	 */
	public static FEPReturnCode NotApplyTransferActno = FEPReturnCode.NotApplyTransferActno;
	/**
	 * 問題帳戶，不更新磁軌且不留置卡片(13207)
	 */
	public static FEPReturnCode NotICCard = FEPReturnCode.NotICCard;
	/**
	 * 問題帳戶，不更新磁軌且不留置卡片(13208)
	 */
	public static FEPReturnCode ICCardNotFound = FEPReturnCode.ICCardNotFound;
	/**
	 * REATINCARD不合(13301)
	 */
	public static FEPReturnCode RetainCardCountNotMatch = FEPReturnCode.RetainCardCountNotMatch;
	/**
	 * 裝鈔後存款次數及金額不合(13302)
	 */
	public static FEPReturnCode DepositSettlementError = FEPReturnCode.DepositSettlementError;
	/**
	 * 裝鈔後轉帳次數及金額不合(13303)
	 */
	public static FEPReturnCode TransferSettlementError = FEPReturnCode.TransferSettlementError;
	/**
	 * 裝鈔後提領張數不合(13304)
	 */
	public static FEPReturnCode WithdrawSettlementError = FEPReturnCode.WithdrawSettlementError;
	/**
	 * 上營業日提領筆數不合(13305)
	 */
	public static FEPReturnCode LBSDYWithdrawSettlementError = FEPReturnCode.LBSDYWithdrawSettlementError;
	/**
	 * 本營業日提領筆數金額不合(13306)
	 */
	public static FEPReturnCode TBSDYWithdrawSettlementError = FEPReturnCode.TBSDYWithdrawSettlementError;
	/**
	 * 裝鈔後繳款次數及金額不合(13307)
	 */
	public static FEPReturnCode PayTaxSettlementError = FEPReturnCode.PayTaxSettlementError;
	/**
	 * 鈔匣數字異常(13308)
	 */
	public static FEPReturnCode ATMCashWithdrawError = FEPReturnCode.ATMCashWithdrawError;
	/**
	 * 裝幣序號不合(13309)
	 */
	public static FEPReturnCode CoinRWTSeqNoNotMatch = FEPReturnCode.CoinRWTSeqNoNotMatch;
	/**
	 * RWT 不合(13311)
	 */
	public static FEPReturnCode RWTNotMatch = FEPReturnCode.RWTNotMatch;
	/**
	 * RWT 裝鈔序號不合(13312)
	 */
	public static FEPReturnCode RWTSeqNoNotMatch = FEPReturnCode.RWTSeqNoNotMatch;
	/**
	 * ADM 錢箱個數為 0(13313)
	 */
	public static FEPReturnCode ADMCashboxCountError = FEPReturnCode.ADMCashboxCountError;
	/**
	 * ATM錢箱內容與中心帳務不合(13314)
	 */
	public static FEPReturnCode ATMBOXSettlementError = FEPReturnCode.ATMBOXSettlementError;
	/**
	 * 密碼錯誤次數已達上限(13401)
	 */
	public static FEPReturnCode OverPasswordErrorCount = FEPReturnCode.OverPasswordErrorCount;
	/**
	 * 查無匯率(13402)
	 */
	public static FEPReturnCode NoRateTable = FEPReturnCode.NoRateTable;
	/**
	 * 轉出帳號錯誤，不更新磁軌且不留置卡片(13403)
	 */
	public static FEPReturnCode TranOutACTNOError = FEPReturnCode.TranOutACTNOError;
	/**
	 * 身份證號不存在(13404)
	 */
	public static FEPReturnCode IDNotFound = FEPReturnCode.IDNotFound;
	/**
	 * 帳號不存在(13405)
	 */
	public static FEPReturnCode ACTNONotFound = FEPReturnCode.ACTNONotFound;
	/**
	 * 收到訊息不符規格(13406)
	 */
	public static FEPReturnCode MsgContentError = FEPReturnCode.MsgContentError;
	/**
	 * 輸入日期不得大於十二個月(13407)
	 */
	public static FEPReturnCode DueDateError = FEPReturnCode.DueDateError;
	/**
	 * 輸入日期不合理(13408)
	 */
	public static FEPReturnCode InputDateError = FEPReturnCode.InputDateError;
	/**
	 * 傳入幣別錯誤(13409)
	 */
	public static FEPReturnCode InputCurrencyCodeError = FEPReturnCode.InputCurrencyCodeError;
	/**
	 * ATM無此交易，IC卡讀寫設備未支援(13410)
	 */
	public static FEPReturnCode ICEMVError = FEPReturnCode.ICEMVError;
	/**
	 * 客戶不同意收取Access Fee，交易拒絕(13411)
	 */
	public static FEPReturnCode ACFeeReject = FEPReturnCode.ACFeeReject;
	/**
	 * ATM交易序號重複(13501)
	 */
	public static FEPReturnCode ATMSeqNoDuplicate = FEPReturnCode.ATMSeqNoDuplicate;
	/**
	 * 新台幣仟元剩餘張數不合(13601)
	 */
	public static FEPReturnCode CompareCountError = FEPReturnCode.CompareCountError;
	/**
	 * 企業戶無卡提款設定記號錯誤(13602)
	 */
	public static FEPReturnCode ApplyTypeError = FEPReturnCode.ApplyTypeError;
	/**
	 * 身份證或統一編號與卡檔不符(13603)
	 */
	public static FEPReturnCode IDError = FEPReturnCode.IDError;
	/**
	 * 無此無卡提款序號(13811)
	 */
	public static FEPReturnCode NWDSeqNotFound = FEPReturnCode.NWDSeqNotFound;
	/**
	 * 提款序號(已提款/已失效/已取消)(13812)
	 */
	public static FEPReturnCode NWDSeqNotMatch = FEPReturnCode.NWDSeqNotMatch;
	/**
	 * 類型錯誤(13813)
	 */
	public static FEPReturnCode TypeError = FEPReturnCode.TypeError;
	/**
	 * 輸入資料有誤(13814)
	 */
	public static FEPReturnCode NWDSeqError = FEPReturnCode.NWDSeqError;
	/**
	 * 電文格式錯誤，欄位缺少(13817)
	 */
	public static FEPReturnCode FieldError = FEPReturnCode.FieldError;
	/**
	 * 其他非預期錯誤(13818)
	 */
	public static FEPReturnCode UnExpectError = FEPReturnCode.UnExpectError;
	/**
	 * 無卡提款功能已失效或狀態有誤(13819)
	 */
	public static FEPReturnCode NCStatusNotMatch = FEPReturnCode.NCStatusNotMatch;
	/**
	 * 掌靜脈驗證有誤(13820)
	 */
	public static FEPReturnCode PVVerifyError = FEPReturnCode.PVVerifyError;
	/**
	 * 其他類檢核錯誤(13999)
	 */
	public static FEPReturnCode OtherCheckError = FEPReturnCode.OtherCheckError;
}
