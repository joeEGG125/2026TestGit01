package com.syscom.fep.vo.enums;

import com.syscom.fep.base.enums.FEPReturnCode;

public interface RMReturnCode {

    /**
     * 電文序號錯誤(12101)
     */
    public static FEPReturnCode FISCNumberError = FEPReturnCode.FISCNumberError;
    /**
     * 匯款金額超過限額(12102)
     */
    public static FEPReturnCode RemitAmoutExceed = FEPReturnCode.RemitAmoutExceed;
    /**
     * 發(收)信單位與發(收)信行代號不符(12106)
     */
    public static FEPReturnCode SenderBankWithAcceptCodeNotMatch = FEPReturnCode.SenderBankWithAcceptCodeNotMatch;
    /**
     * 原交易訊息類別錯誤(12107)
     */
    public static FEPReturnCode TradePcodeError = FEPReturnCode.TradePcodeError;
    /**
     * 解款單位無此分行(12113)
     */
    public static FEPReturnCode ReciverBankNotBranch = FEPReturnCode.ReciverBankNotBranch;
    /**
     * 收信行未參加該跨行業務(12118)
     */
    public static FEPReturnCode ReceiverBankRMNotAttendBusiness = FEPReturnCode.ReceiverBankRMNotAttendBusiness;
    /**
     * 發信行未參加該跨行業務(12119)
     */
    public static FEPReturnCode SenderBankRMNotAttendBusiness = FEPReturnCode.SenderBankRMNotAttendBusiness;
    /**
     * 通匯序號重覆(12120)
     */
    public static FEPReturnCode RemitsNumberRepeated = FEPReturnCode.RemitsNumberRepeated;
    /**
     * 退還匯款之原匯款交易不存在(12121)
     */
    public static FEPReturnCode ReturnRemitOriginalDatanotExist = FEPReturnCode.ReturnRemitOriginalDatanotExist;
    /**
     * 退還匯款與原匯款交易資料比對不符(12122)
     */
    public static FEPReturnCode ReturnRemitRemitOriginalDataNotMatch = FEPReturnCode.ReturnRemitRemitOriginalDataNotMatch;
    /**
     * 退還匯款已退還(12123)
     */
    public static FEPReturnCode ReturnRemithasreturned = FEPReturnCode.ReturnRemithasreturned;
    /**
     * 必要輸入項目值不得為空白或為零(12201)
     */
    public static FEPReturnCode NotBlankOrnot0 = FEPReturnCode.NotBlankOrnot0;
    /**
     * 輸入查詢日期不得大於目前日期(12202)
     */
    public static FEPReturnCode QueryDateMustLessThanToday = FEPReturnCode.QueryDateMustLessThanToday;
    /**
     * 無此銀行代號(12203)
     */
    public static FEPReturnCode BankNumberNotFound = FEPReturnCode.BankNumberNotFound;
    /**
     * 指定批號錯誤(12204)
     */
    public static FEPReturnCode AppointedBatchNumberError = FEPReturnCode.AppointedBatchNumberError;
    /**
     * 媒體批號錯誤(12205)
     */
    public static FEPReturnCode MediaBatchNumberError = FEPReturnCode.MediaBatchNumberError;
    /**
     * 匯款種類錯誤(12206)
     */
    public static FEPReturnCode RemitCategoryError = FEPReturnCode.RemitCategoryError;
    /**
     * 整批匯款媒體檔案錯誤(12207)
     */
    public static FEPReturnCode RemitMediaFileError = FEPReturnCode.RemitMediaFileError;
    /**
     * 序號錯誤(12208)
     */
    public static FEPReturnCode RecordNumberError = FEPReturnCode.RecordNumberError;
    /**
     * 匯出記錄狀態錯誤(12209)
     */
    public static FEPReturnCode RemitStatusError = FEPReturnCode.RemitStatusError;
    /**
     * 記錄狀態錯誤(12210)
     */
    public static FEPReturnCode RecordStatusError = FEPReturnCode.RecordStatusError;
    /**
     * 匯款金額,現金/轉帳類別,不在定義範圍內(12211)
     */
    public static FEPReturnCode RemitAmountNotCashNotTransfer = FEPReturnCode.RemitAmountNotCashNotTransfer;
    /**
     * 匯款手續費,現金/轉帳類別,不在定義範圍內(12212)
     */
    public static FEPReturnCode RemitServicechargeNotCashNotTransfer = FEPReturnCode.RemitServicechargeNotCashNotTransfer;
    /**
     * 匯款人名稱, 收款人名稱不得空白(12213)
     */
    public static FEPReturnCode RemitNameReceiverNameNotBlank = FEPReturnCode.RemitNameReceiverNameNotBlank;
    /**
     * 中文欄位長度錯誤(12214)
     */
    public static FEPReturnCode ChineseRowError = FEPReturnCode.ChineseRowError;
    /**
     * 交易金額小於或等於零(12215)
     */
    public static FEPReturnCode TradeAmountMoneyLessThan0 = FEPReturnCode.TradeAmountMoneyLessThan0;
    /**
     * 收款人帳號輸入錯誤(12216)
     */
    public static FEPReturnCode ReceiverAccountError = FEPReturnCode.ReceiverAccountError;
    /**
     * EJFNO取號有誤(12217)
     */
    public static FEPReturnCode EJFNOTakeNumberError = FEPReturnCode.EJFNOTakeNumberError;
    /**
     * 此交易必須主管授權,主管代號不得為空白(12218)
     */
    public static FEPReturnCode SuperintendentCodeNoBlank = FEPReturnCode.SuperintendentCodeNoBlank;
    /**
     * 匯款日期輸入錯誤(12219)
     */
    public static FEPReturnCode RemitDateError = FEPReturnCode.RemitDateError;
    /**
     * 匯款類別輸入錯誤(12220)
     */
    public static FEPReturnCode RemitKindError = FEPReturnCode.RemitKindError;
    /**
     * 發送確認別輸入錯誤(12221)
     */
    public static FEPReturnCode KindError = FEPReturnCode.KindError;
    /**
     * 帳務類別輸入錯誤(12222)
     */
    public static FEPReturnCode RemitTXKindError = FEPReturnCode.RemitTXKindError;
    /**
     * 收款人帳號必須輸入(12223)
     */
    public static FEPReturnCode RemitInAccountIsMandatory = FEPReturnCode.RemitInAccountIsMandatory;
    /**
     * 暫收付科目必須輸入(12224)
     */
    public static FEPReturnCode SuspendAPNOIsMandatory = FEPReturnCode.SuspendAPNOIsMandatory;
    /**
     * 退匯理由輸入錯誤(12225)
     */
    public static FEPReturnCode BackReasonError = FEPReturnCode.BackReasonError;
    /**
     * 此交易必須主管授權,主管代號1,2必須有值且不能相同(12226)
     */
    public static FEPReturnCode SuperintendentCodeNotAvailable = FEPReturnCode.SuperintendentCodeNotAvailable;
    /**
     * 暫收付科目實際來源帳號必須輸入(12227)
     */
    public static FEPReturnCode SuspendAccountIsMandatory = FEPReturnCode.SuspendAccountIsMandatory;
    /**
     * 備註必須輸入(12228)
     */
    public static FEPReturnCode RemarkIsMandatory = FEPReturnCode.RemarkIsMandatory;
    /**
     * 虛擬帳號必須輸入(12229)
     */
    public static FEPReturnCode DummyAccountIsMandatory = FEPReturnCode.DummyAccountIsMandatory;
    /**
     * 組RM回應電文錯誤(12230)
     */
    public static FEPReturnCode PrepareRMResError = FEPReturnCode.PrepareRMResError;
    /**
     * 帳號必須輸入(12231)
     */
    public static FEPReturnCode AccountIsMandatory = FEPReturnCode.AccountIsMandatory;
    /**
     * 輸入的交易類別代碼不符合(12301)
     */
    public static FEPReturnCode TradePcodeNotMatch = FEPReturnCode.TradePcodeNotMatch;
    /**
     * 匯款金額 + 實收手續費不等於金額合計(12302)
     */
    public static FEPReturnCode AmountNotMatch = FEPReturnCode.AmountNotMatch;
    /**
     * 輸入的交易類別代碼與匯款主檔的匯款種類不符合(12303)
     */
    public static FEPReturnCode TradePcodeAndHostNotMatch = FEPReturnCode.TradePcodeAndHostNotMatch;
    /**
     * 作業代號不符(12304)
     */
    public static FEPReturnCode HomeworkCodeNotMatch = FEPReturnCode.HomeworkCodeNotMatch;
    /**
     * 輸入的匯款交易類別代碼不符合(12305)
     */
    public static FEPReturnCode RemitPcodeNotMatch = FEPReturnCode.RemitPcodeNotMatch;
    /**
     * 輸入的序號類別代碼不符合(12306)
     */
    public static FEPReturnCode TradeCodeNotMatch = FEPReturnCode.TradeCodeNotMatch;
    /**
     * 輸入的設定FLAG不符合(12307)
     */
    public static FEPReturnCode FlagNotMatch = FEPReturnCode.FlagNotMatch;
    /**
     * 應付財金手續費輸入不符(12308)
     */
    public static FEPReturnCode FISCHandleChargeNotMatch = FEPReturnCode.FISCHandleChargeNotMatch;
    /**
     * 應收手續費輸入不符(12309)
     */
    public static FEPReturnCode HandleChargeNotMatch = FEPReturnCode.HandleChargeNotMatch;
    /**
     * 匯款金額與主檔不符(12340)
     */
    public static FEPReturnCode RemitAmoutNotMach = FEPReturnCode.RemitAmoutNotMach;
    /**
     * 解款單位與主檔不符(12341)
     */
    public static FEPReturnCode ReceiverBankNotMatch = FEPReturnCode.ReceiverBankNotMatch;
    /**
     * 合計筆數金額與明細不符(12342)
     */
    public static FEPReturnCode TotalamtNotMatch = FEPReturnCode.TotalamtNotMatch;
    /**
     * 本交易不得沖正(12401)
     */
    public static FEPReturnCode TransactionNotAllowReverse = FEPReturnCode.TransactionNotAllowReverse;
    /**
     * 跨行匯出，解款行不得為本銀行(12402)
     */
    public static FEPReturnCode InterBankRemitNotOriginallyBank = FEPReturnCode.InterBankRemitNotOriginallyBank;
    /**
     * 本行未參加財金匯款，不得進行此交易(12403)
     */
    public static FEPReturnCode NotParticipationRemittanceNotTransaction = FEPReturnCode.NotParticipationRemittanceNotTransaction;
    /**
     * 本行禁止匯款匯出，不得進行此交易(12404)
     */
    public static FEPReturnCode ProhibitionCollectsNotTransaction = FEPReturnCode.ProhibitionCollectsNotTransaction;
    /**
     * 本行禁止匯款櫃員登錄，不得進行此交易(12405)
     */
    public static FEPReturnCode ProhibtionRemittanceRegisteringNotTransaction = FEPReturnCode.ProhibtionRemittanceRegisteringNotTransaction;
    /**
     * 本行匯兌已結帳，不得進行此交易(12406)
     */
    public static FEPReturnCode RemittancePayinNotTransaction = FEPReturnCode.RemittancePayinNotTransaction;
    /**
     * 國庫及同業匯款只收基本郵電費(12407)
     */
    public static FEPReturnCode TreasuryAndRemittanceAcceptBasicexpenses = FEPReturnCode.TreasuryAndRemittanceAcceptBasicexpenses;
    /**
     * 已經匯出放行或取消之匯款資料不可再修正(12409)
     */
    public static FEPReturnCode RemitRemittanceDataNotRevise = FEPReturnCode.RemitRemittanceDataNotRevise;
    /**
     * 暫停FLAG已在暫停狀態,不能再設定暫停(12410)
     */
    public static FEPReturnCode PauseFlagPauseNotPause = FEPReturnCode.PauseFlagPauseNotPause;
    /**
     * 暫停FLAG已在啟動狀態,不能再設定啟動(12411)
     */
    public static FEPReturnCode PauseFlagStartNotStart = FEPReturnCode.PauseFlagStartNotStart;
    /**
     * 已有分行結帳,不得進行此交易(12412)
     */
    public static FEPReturnCode RingItUpNotTrade = FEPReturnCode.RingItUpNotTrade;
    /**
     * CBS主機營業中不能執行此交易(12413)
     */
    public static FEPReturnCode CBSInservice = FEPReturnCode.CBSInservice;
    /**
     * RM AP not Checkin 不能執行此交易(12414)
     */
    public static FEPReturnCode RMOutOfService = FEPReturnCode.RMOutOfService;
    /**
     * 匯出優先權FLAG不符合(12415)
     */
    public static FEPReturnCode RemitPriorityFlagNotMatch = FEPReturnCode.RemitPriorityFlagNotMatch;
    /**
     * 通匯類系統正常不需再復原(12416)
     */
    public static FEPReturnCode RemitsSystemNotNeedRecover = FEPReturnCode.RemitsSystemNotNeedRecover;
    /**
     * 一般通訊系統正常不需再復原(12417)
     */
    public static FEPReturnCode CommunicationSysteNotRecover = FEPReturnCode.CommunicationSysteNotRecover;
    /**
     * 禁止跨行登錄(12418)
     */
    public static FEPReturnCode RemitStop = FEPReturnCode.RemitStop;
    /**
     * 匯款單位無此分行(12419)
     */
    public static FEPReturnCode SenderBankNotBranch = FEPReturnCode.SenderBankNotBranch;
    /**
     * 此筆序號已下傳(12420)
     */
    public static FEPReturnCode OrdinalNumberDownload = FEPReturnCode.OrdinalNumberDownload;
    /**
     * 此筆批號重覆(12421)
     */
    public static FEPReturnCode BatchNumberRepeated = FEPReturnCode.BatchNumberRepeated;
    /**
     * 發信單位該項跨行業務停止或暫停營業(12422)
     */
    public static FEPReturnCode SenderBankStopOrPauseBusiness = FEPReturnCode.SenderBankStopOrPauseBusiness;
    /**
     * 原交易訊息類別錯誤或尚未傳送財金公司(12423)
     */
    public static FEPReturnCode TradePcodeErrorOrNotSendFISC = FEPReturnCode.TradePcodeErrorOrNotSendFISC;
    /**
     * 匯款單位別與解款單位別相同(12424)
     */
    public static FEPReturnCode SenderBankAndReceiverBankthesame = FEPReturnCode.SenderBankAndReceiverBankthesame;
    /**
     * 解款行方可執行本交易(12425)
     */
    public static FEPReturnCode TransferFundCanTrade = FEPReturnCode.TransferFundCanTrade;
    /**
     * BRS登錄序號重覆(12426)
     */
    public static FEPReturnCode BRSRegisterNumberRepeated = FEPReturnCode.BRSRegisterNumberRepeated;
    /**
     * 實收手續費大於應收手續費(12427)
     */
    public static FEPReturnCode HandleChargeNotOK = FEPReturnCode.HandleChargeNotOK;
    /**
     * 尚有匯出中資料, 請先查詢財金交易處理狀況(12428)
     */
    public static FEPReturnCode RemitDataNotComplete = FEPReturnCode.RemitDataNotComplete;
    /**
     * 本行匯出被退暫停此行庫之匯出(12429)
     */
    public static FEPReturnCode BankReturnRemit = FEPReturnCode.BankReturnRemit;
    /**
     * 借貸不平(12450)
     */
    public static FEPReturnCode CrDrNotBalance = FEPReturnCode.CrDrNotBalance;
    /**
     * 非本日解款不能執行解款狀態變更(12451)
     */
    public static FEPReturnCode ProcessingDateNotToday = FEPReturnCode.ProcessingDateNotToday;
    /**
     * 資深櫃員不能確認或覆核原匯出登錄交易(12452)
     */
    public static FEPReturnCode TellerNotAudit = FEPReturnCode.TellerNotAudit;
    /**
     * FEP執行確認取消(12453)
     */
    public static FEPReturnCode FEPCancel = FEPReturnCode.FEPCancel;
    /**
     * 本行更換通匯基碼(12454)
     */
    public static FEPReturnCode MBankChangekey = FEPReturnCode.MBankChangekey;
    /**
     * 調整跨行電文序號(12455)
     */
    public static FEPReturnCode ChangeFISCSno = FEPReturnCode.ChangeFISCSno;
    /**
     * 調整跨行通匯序號(12456)
     */
    public static FEPReturnCode ChangeRMSno = FEPReturnCode.ChangeRMSno;
    /**
     * 調整匯兌收送狀態(12457)
     */
    public static FEPReturnCode ChangeServiceStatus = FEPReturnCode.ChangeServiceStatus;
    /**
     * 調整待匯出匯款順序(12458)
     */
    public static FEPReturnCode ChangePriority = FEPReturnCode.ChangePriority;
    /**
     * 匯入補送中心(12459)
     */
    public static FEPReturnCode SendRMINTX = FEPReturnCode.SendRMINTX;
    /**
     * 啟動/暫停CBS匯出入狀態(12460)
     */
    public static FEPReturnCode ChangeCBSRMStatus = FEPReturnCode.ChangeCBSRMStatus;
    /**
     * 匯款確認取消交易輸入(12461)
     */
    public static FEPReturnCode SendRMOUTCancel = FEPReturnCode.SendRMOUTCancel;
    /**
     * 一般通訊確認取消交易(12462)
     */
    public static FEPReturnCode SendMSGOUTCancel = FEPReturnCode.SendMSGOUTCancel;
    /**
     * 大批匯款監控啟動(12463)
     */
    public static FEPReturnCode SendRMBTCHTele = FEPReturnCode.SendRMBTCHTele;
    /**
     * 整批人工解款監控啟動(12464)
     */
    public static FEPReturnCode SendSUnisysTele = FEPReturnCode.SendSUnisysTele;
    /**
     * 大批匯款監控回饋檔啟動(12465)
     */
    public static FEPReturnCode SendRMBTCHBackTele = FEPReturnCode.SendRMBTCHBackTele;
    /**
     * FEDI轉通匯回饋監控啟動(12466)
     */
    public static FEPReturnCode SendFEDIBackTele = FEPReturnCode.SendFEDIBackTele;
    /**
     * 超過查詢限制筆數(12467)
     */
    public static FEPReturnCode ExceedMaxCount = FEPReturnCode.ExceedMaxCount;
    /**
     * 往來行庫已設定暫停匯出(12468)
     */
    public static FEPReturnCode AllbankStopService = FEPReturnCode.AllbankStopService;
    /**
     * 此筆已解款,請查明原因(12469)
     */
    public static FEPReturnCode RMINComplete = FEPReturnCode.RMINComplete;
    /**
     * 修改匯出退匯資料(12470)
     */
    public static FEPReturnCode ModifyRMOUTStat = FEPReturnCode.ModifyRMOUTStat;
    /**
     * 待解筆數超過警示(12471)
     */
    public static FEPReturnCode WarningREMAIN_CNT = FEPReturnCode.WarningREMAIN_CNT;
    /**
     * T24 有Pending筆數未處理(12472)
     */
    public static FEPReturnCode T24Pending = FEPReturnCode.T24Pending;
    /**
     * FCS(FEPR1300)有未上傳筆數未處理(12473)
     */
    public static FEPReturnCode FCSFEPPending = FEPReturnCode.FCSFEPPending;
    /**
     * FCS(B2BR1300)有未上傳筆數未處理(12474)
     */
    public static FEPReturnCode FCSB2BPending = FEPReturnCode.FCSB2BPending;
    /**
     * 匯入交易時間超過警示值(12475)
     */
    public static FEPReturnCode Warning_CalcFISCTime = FEPReturnCode.Warning_CalcFISCTime;
    /**
     * 原匯出行才可執行本交易(12808)
     */
    public static FEPReturnCode RemitJustcanTrade = FEPReturnCode.RemitJustcanTrade;
    /**
     * AML傳送狀態設定(12810)
     */
    public static FEPReturnCode ChangeAMLRMStatus = FEPReturnCode.ChangeAMLRMStatus;
    /**
     * 電文ResendAML人工作業(12811)
     */
    public static FEPReturnCode ResendAML = FEPReturnCode.ResendAML;
    /**
     * 電文BypassAML人工作業(12812)
     */
    public static FEPReturnCode BypassAML = FEPReturnCode.BypassAML;
    /**
     * 匯出金額大於跨行基金水位(12813)
     */
    public static FEPReturnCode ExceetCLRFundLevel = FEPReturnCode.ExceetCLRFundLevel;
    /**
     * 跨行餘額低於跨行基金水位二(12814)
     */
    public static FEPReturnCode LowCLRFundLevel2 = FEPReturnCode.LowCLRFundLevel2;
    /**
     * 匯出暫禁記號狀態設定(12815)
     */
    public static FEPReturnCode ChangeCLRRMStatus = FEPReturnCode.ChangeCLRRMStatus;
    /**
     * AML異常通知(12816)
     */
    public static FEPReturnCode AMLErrorNotice = FEPReturnCode.AMLErrorNotice;
}
