package com.syscom.fep.vo.enums;

import com.syscom.fep.base.enums.FEPReturnCode;

public interface FISCReturnCode {

	/**
	 * 訊息格式或內容編輯錯誤(11011)
	 */
	public static FEPReturnCode MessageFormatError = FEPReturnCode.MessageFormatError;
	/**
	 * 檢核電文的BITMAP錯誤(11012)
	 */
	public static FEPReturnCode CheckBitMapError = FEPReturnCode.CheckBitMapError;
	/**
	 * 發信單位未參加該跨行業務(11021)
	 */
	public static FEPReturnCode SenderBankNotAttendBusiness = FEPReturnCode.SenderBankNotAttendBusiness;
	/**
	 * 發信單位該項跨行業務停止或暫停營業(11022)
	 */
	public static FEPReturnCode SenderBankServiceStop = FEPReturnCode.SenderBankServiceStop;
	/**
	 * 發信單位主機未在跨行作業運作狀態(11023)
	 */
	public static FEPReturnCode SenderBankOperationStop = FEPReturnCode.SenderBankOperationStop;
	/**
	 * 收信單位未參加該跨行業務(11024)
	 */
	public static FEPReturnCode ReceiverBankNotAttendBusiness = FEPReturnCode.ReceiverBankNotAttendBusiness;
	/**
	 * 收信單位該項跨行業務停止或暫停營業(11025)
	 */
	public static FEPReturnCode ReceiverBankServiceStop = FEPReturnCode.ReceiverBankServiceStop;
	/**
	 * 收信單位主機未在跨行作業運作狀態(11026)
	 */
	public static FEPReturnCode ReceiverBankOperationStop = FEPReturnCode.ReceiverBankOperationStop;
	/**
	 * 銀行內部停止該項跨行業務(11027)
	 */
	public static FEPReturnCode StopServiceByInternal = FEPReturnCode.StopServiceByInternal;
	/**
	 * 不允許信用卡交易(11028)
	 */
	public static FEPReturnCode CCardServiceNotAllowed = FEPReturnCode.CCardServiceNotAllowed;
	/**
	 * 押碼基碼( KEY )不同步(11031)
	 */
	public static FEPReturnCode KeySyncError = FEPReturnCode.KeySyncError;
	/**
	 * CD/ATM 共用系統交易之客戶亂碼基碼不同步(11033)
	 */
	public static FEPReturnCode PPKeySyncError = FEPReturnCode.PPKeySyncError;
	/**
	 * 訊息欄位資料與原交易資料不相符(11041)
	 */
	public static FEPReturnCode OriginalMessageDataError = FEPReturnCode.OriginalMessageDataError;
	/**
	 * 端末機故障(11051)
	 */
	public static FEPReturnCode TerminalError = FEPReturnCode.TerminalError;
	/**
	 * 財金回覆逾時(11061)
	 */
	public static FEPReturnCode FISCTimeout = FEPReturnCode.FISCTimeout;
	/**
	 * 財金公司該項跨行業務暫停或停止營業(11071)
	 */
	public static FEPReturnCode FISCServiceStop = FEPReturnCode.FISCServiceStop;
	/**
	 * 財金公司主機未在跨行作業運作狀態(11072)
	 */
	public static FEPReturnCode FISCOperationStop = FEPReturnCode.FISCOperationStop;
	/**
	 * 無效之訊息類別代碼(MESSAGE TYPE)或交易類別代碼(PROCESSING CODE)(11101)
	 */
	public static FEPReturnCode MessageTypeError = FEPReturnCode.MessageTypeError;
	/**
	 * Trace Number 重覆(11102)
	 */
	public static FEPReturnCode TraceNumberDuplicate = FEPReturnCode.TraceNumberDuplicate;
	/**
	 * 訊息與原跨行交易之執行狀態不符(11103)
	 */
	public static FEPReturnCode OriginalMessageError = FEPReturnCode.OriginalMessageError;
	/**
	 * STAN 非數字(11104)
	 */
	public static FEPReturnCode STANError = FEPReturnCode.STANError;
	/**
	 * 訊息發送者ID與訊息內之TXN. SOURCEINSTITUTE ID. 或TXN. DESTINATIONID. 不符(11105)
	 */
	public static FEPReturnCode SenderIdError = FEPReturnCode.SenderIdError;
	/**
	 * 訊息內容錯誤(11106)
	 */
	public static FEPReturnCode BitFlagError = FEPReturnCode.BitFlagError;
	/**
	 * 無效之回應代碼(11107)
	 */
	public static FEPReturnCode InvalidResponseCode = FEPReturnCode.InvalidResponseCode;
	/**
	 * 跨行作業系統或應用系統狀態已更新(11301)
	 */
	public static FEPReturnCode INBKStatusUpdateAlready = FEPReturnCode.INBKStatusUpdateAlready;
	/**
	 * 交易訊息內之代碼( ID )欄錯誤(11302)
	 */
	public static FEPReturnCode APIDError = FEPReturnCode.APIDError;
	/**
	 * 跨行作業系統或應用系統狀態不符(11303)
	 */
	public static FEPReturnCode INBKStatusError = FEPReturnCode.INBKStatusError;
	/**
	 * 參加單位該項跨行業務不可PRECHECK-OUT(11321)
	 */
	public static FEPReturnCode PrecheckOutNotAllowed = FEPReturnCode.PrecheckOutNotAllowed;
	/**
	 * 財金公司之STOP STRVICE 不可執行(11327)
	 */
	public static FEPReturnCode StopServiceToFISCNotAllowed = FEPReturnCode.StopServiceToFISCNotAllowed;
	/**
	 * 參加單位AP CHECKOUT 時間未到，該交易不可執行(11328)
	 */
	public static FEPReturnCode CheckOutTimingError = FEPReturnCode.CheckOutTimingError;
	/**
	 * 訊息內AP DATA ELEMENT 之SYNC.CHECK ITEM 不相符(11331)
	 */
	public static FEPReturnCode KeySyncNotMatch = FEPReturnCode.KeySyncNotMatch;
	/**
	 * CHANGE KEY CALL 正進行中(11332)
	 */
	public static FEPReturnCode ChangeKeyCallIsProcessing = FEPReturnCode.ChangeKeyCallIsProcessing;
	/**
	 * CHANGE KEY CALL 訊息內之RAN-DOM NUMBER 錯誤(11333)
	 */
	public static FEPReturnCode RandomNumberError = FEPReturnCode.RandomNumberError;
	/**
	 * 交易金額或金融卡國際提款交易金額錯誤(11423)
	 */
	public static FEPReturnCode TxAmountError = FEPReturnCode.TxAmountError;
	/**
	 * 財金回覆-無此交易(11471)
	 */
	public static FEPReturnCode TransactionNotFound = FEPReturnCode.TransactionNotFound;
	/****
	 * 交易狀態不明或 Protocal 錯誤(11472)
	 */
	public static FEPReturnCode ProtocalError = FEPReturnCode.ProtocalError;
	/**
	 * 交易狀態有誤(11473)
	 */
	public static FEPReturnCode StatusNotMatch = FEPReturnCode.StatusNotMatch;
	/**
	 * 拒絕交易，需進行人工授權交易(11476)
	 */
	public static FEPReturnCode RejectFallBack = FEPReturnCode.RejectFallBack;
	/**
	 * 國外提款超過當日累計限制次數(11477)
	 */
	public static FEPReturnCode OverOWDCnt = FEPReturnCode.OverOWDCnt;
	/**
	 * 身份証號／營利事業統一編號有誤(11486)
	 */
	public static FEPReturnCode CheckIDNOError = FEPReturnCode.CheckIDNOError;
	/**
	 * 無此帳戶(依據委託單位、繳費類別及費用代號)(11488)
	 */
	public static FEPReturnCode NPSNotFound = FEPReturnCode.NPSNotFound;
	/**
	 * 特店檔不存在(11489)
	 */
	public static FEPReturnCode MerchentIDNotFound = FEPReturnCode.MerchentIDNotFound;
	/**
	 * 繳稅類別不存在(11501)
	 */
	public static FEPReturnCode PayTypeNotFound = FEPReturnCode.PayTypeNotFound;
	/**
	 * 通道EJ重複(11502)
	 */
	public static FEPReturnCode ChannelEJFNODuplicate = FEPReturnCode.ChannelEJFNODuplicate;
	/**
	 * 中文欄位或變動長度欄位長度錯誤(11504)
	 */
	public static FEPReturnCode LengthError = FEPReturnCode.LengthError;
	/**
	 * 財金公司訊息通知 -- (NOTICE DATA)(11601)
	 */
	public static FEPReturnCode FISCNoticeCall = FEPReturnCode.FISCNoticeCall;
	/**
	 * 財金公司不明訊息通知 -- (GARBLED HEADER)(11602)
	 */
	public static FEPReturnCode FISCGarbledMessage = FEPReturnCode.FISCGarbledMessage;
	/**
	 * 財金公司 KEY CHANGE CALL 完成(11603)
	 */
	public static FEPReturnCode FISCKeyChangeCall = FEPReturnCode.FISCKeyChangeCall;
	/**
	 * 財金公司 KEY SYNC CALL 完成(11604)
	 */
	public static FEPReturnCode FISCKeySyncCall = FEPReturnCode.FISCKeySyncCall;
	/**
	 * 財金公司(系統名稱) PRE-CHECK OUT 完成(11605)
	 */
	public static FEPReturnCode FISCPreCheckOut = FEPReturnCode.FISCPreCheckOut;
	/**
	 * 財金公司(系統名稱) CHECK OUT 完成(11606)
	 */
	public static FEPReturnCode FISCCheckOut = FEPReturnCode.FISCCheckOut;
	/**
	 * 會員單位訊息通知 -- (NOTICE DATA)(11607)
	 */
	public static FEPReturnCode MBankNoticeCall = FEPReturnCode.MBankNoticeCall;
	/**
	 * 會員單位 CHANGE KEY REQUEST 完成(11608)
	 */
	public static FEPReturnCode MBankChangeKeyRequest = FEPReturnCode.MBankChangeKeyRequest;
	/**
	 * (系統名稱) CHECK IN 完成(11609)
	 */
	public static FEPReturnCode MBankCheckIn = FEPReturnCode.MBankCheckIn;
	/**
	 * (系統名稱) EX. CHECK IN 完成(11610)
	 */
	public static FEPReturnCode MBankExceptionalCheckIn = FEPReturnCode.MBankExceptionalCheckIn;
	/**
	 * (系統名稱) EX. CHECK OUT 完成(11611)
	 */
	public static FEPReturnCode MBankExceptionalCheckOut = FEPReturnCode.MBankExceptionalCheckOut;
	/**
	 * 跨行可用餘額低於最低限額(11621)
	 */
	public static FEPReturnCode FISCAvailableBalanceUnderLimit = FEPReturnCode.FISCAvailableBalanceUnderLimit;
	/**
	 * 增加跨行業務基金通知(或沖正)(11622)
	 */
	public static FEPReturnCode FISCBalanceIncrement = FEPReturnCode.FISCBalanceIncrement;
	/**
	 * 減少跨行業務基金通知(11623)
	 */
	public static FEPReturnCode FISCBalanceReduce = FEPReturnCode.FISCBalanceReduce;
	/**
	 * 財金公司跨行結帳資料相符(或不符)(11624)
	 */
	public static FEPReturnCode FISCCloseBalanceDeliver = FEPReturnCode.FISCCloseBalanceDeliver;
	/**
	 * 會員單位跨行作業總計查詢完成(11625)
	 */
	public static FEPReturnCode MBankCloseBalanceInquery = FEPReturnCode.MBankCloseBalanceInquery;
	/**
	 * 會員單位減少跨行業務基金請求(11626)
	 */
	public static FEPReturnCode MBankBalanceReduceRequest = FEPReturnCode.MBankBalanceReduceRequest;
	/**
	 * 財金公司傳送未完成交易(11631)
	 */
	public static FEPReturnCode FISCUnfinishedTransaction = FEPReturnCode.FISCUnfinishedTransaction;
	/**
	 * CD/ATM 交易人工沖正結果(11632)
	 */
	public static FEPReturnCode FISCErrorCorrectionResult = FEPReturnCode.FISCErrorCorrectionResult;
	/**
	 * 卡號有誤不更新磁軌且不留卡(11633)
	 */
	public static FEPReturnCode UPBinError = FEPReturnCode.UPBinError;
	/**
	 * 通道名稱錯誤(11634)
	 */
	public static FEPReturnCode ChannelNameError = FEPReturnCode.ChannelNameError;
	/**
	 * 跨行業務基金可用餘額通知(11635)
	 */
	public static FEPReturnCode FISCUseBalance = FEPReturnCode.FISCUseBalance;
	/**
	 * 跨國沖正結果通知(11636)
	 */
	public static FEPReturnCode FISCICReverseNotice = FEPReturnCode.FISCICReverseNotice;
	/**
	 * 國外提領限制記號關閉(11637)
	 */
	public static FEPReturnCode OWDGPClose = FEPReturnCode.OWDGPClose;
	/**
	 * 購貨日期有誤(11638)
	 */
	public static FEPReturnCode MerchantDateError = FEPReturnCode.MerchantDateError;
	/**
	 * FISCCloseBalanceSettle
	 */
	public static FEPReturnCode FISCCloseBalanceSettle = FEPReturnCode.FISCCloseBalanceSettle;
	/**
	 * 非約定之轉帳帳戶(13206)
	 */
	public static FEPReturnCode NotApplyTransferActno = FEPReturnCode.NotApplyTransferActno;
}
