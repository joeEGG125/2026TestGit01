package com.syscom.fep.base.cnst.rcode;

import com.syscom.fep.base.enums.FEPReturnCode;

public interface CommonReturnCode {
	/**
	 * 檢核成功(0)
	 */
	public static FEPReturnCode Normal = FEPReturnCode.Normal;
	/**
	 * 處理失敗(10001)
	 */
	public static FEPReturnCode Abnormal = FEPReturnCode.Abnormal;
	/**
	 * 未知的MessageID(無法找到要叫那個AA)(10002)
	 */
	public static FEPReturnCode MessageIDError = FEPReturnCode.MessageIDError;
	/**
	 * 拆解電文錯誤(10003)
	 */
	public static FEPReturnCode ParseTelegramError = FEPReturnCode.ParseTelegramError;
	/**
	 * Mail通知失敗(10004)
	 */
	public static FEPReturnCode MailFail = FEPReturnCode.MailFail;
	/**
	 * 程式發生例外(10099)
	 */
	public static FEPReturnCode ProgramException = FEPReturnCode.ProgramException;
	/**
	 * 自行服務暫停(10101)
	 */
	public static FEPReturnCode IntraBankServiceStop = FEPReturnCode.IntraBankServiceStop;
	/**
	 * 提款暫停服務(10102)
	 */
	public static FEPReturnCode WithdrawServiceStop = FEPReturnCode.WithdrawServiceStop;
	/**
	 * 跨行暫停服務(10103)
	 */
	public static FEPReturnCode InterBankServiceStop = FEPReturnCode.InterBankServiceStop;
	/**
	 * 帳務主機暫停服務(10104)
	 */
	public static FEPReturnCode CBSStopService = FEPReturnCode.CBSStopService;
	/**
	 * 自行{業務別}交易暫停服務(10105)
	 */
	public static FEPReturnCode IntraBankSingleServiceStop = FEPReturnCode.IntraBankSingleServiceStop;
	/**
	 * 自行{業務別}交易開始服務(10106)
	 */
	public static FEPReturnCode IntraBankSingleServiceStart = FEPReturnCode.IntraBankSingleServiceStart;
	/**
	 * 代理行{業務別}交易暫停服務(10107)
	 */
	public static FEPReturnCode AgentBankSingleServiceStop = FEPReturnCode.AgentBankSingleServiceStop;
	/**
	 * 代理行{業務別}交易開始服務(10108)
	 */
	public static FEPReturnCode AgentBankSingleServiceStart = FEPReturnCode.AgentBankSingleServiceStart;
	/**
	 * 原存行{業務別}交易暫停服務(10109)
	 */
	public static FEPReturnCode IssueBankSingleServiceStop = FEPReturnCode.IssueBankSingleServiceStop;
	/**
	 * 原存行{業務別}交易開始服務(10110)
	 */
	public static FEPReturnCode IssueBankSingleServiceStart = FEPReturnCode.IssueBankSingleServiceStart;
	/**
	 * {通道或線路}暫停服務(10111)
	 */
	public static FEPReturnCode ChannelServiceStop = FEPReturnCode.ChannelServiceStop;
	/**
	 * {通道或線路}開始服務(10112)
	 */
	public static FEPReturnCode ChannelServiceStart = FEPReturnCode.ChannelServiceStart;
	/**
	 * 主機回覆格式錯誤(10200)
	 */
	public static FEPReturnCode CBSResponseFormatError = FEPReturnCode.CBSResponseFormatError;
	/**
	 * 主機回覆錯誤(10201)
	 */
	public static FEPReturnCode CBSResponseError = FEPReturnCode.CBSResponseError;
	/**
	 * 信用卡主機回覆錯誤(10202)
	 */
	public static FEPReturnCode ASCResponseError = FEPReturnCode.ASCResponseError;
	/**
	 * 主機回應逾時(10203)
	 */
	public static FEPReturnCode HostResponseTimeout = FEPReturnCode.HostResponseTimeout;
	/**
	 * 信用卡主機回應逾時(10204)
	 */
	public static FEPReturnCode CreditResponseTimeout = FEPReturnCode.CreditResponseTimeout;
	/**
	 * SMS主機回應逾時(10205)
	 */
	public static FEPReturnCode SMSResponseTimeout = FEPReturnCode.SMSResponseTimeout;
	/**
	 * SMS服務回應逾時(10206)
	 */
	public static FEPReturnCode SMSServiceResponseTimeout = FEPReturnCode.SMSServiceResponseTimeout;
	/**
	 * 傳送FISCGW_ATM WebService 錯誤(10207)
	 */
	public static FEPReturnCode FISCGWATMSendError = FEPReturnCode.FISCGWATMSendError;
	/**
	 * 接收FISCGW_ATM WebService 錯誤(10208)
	 */
	public static FEPReturnCode FISCGWATMRcvError = FEPReturnCode.FISCGWATMRcvError;
	/**
	 * 接收敲價系統 WebService 錯誤(10209)
	 */
	public static FEPReturnCode ETSRcvError = FEPReturnCode.ETSRcvError;
	/**
	 * 財金營業日已更換(10211)
	 */
	public static FEPReturnCode FISCBusinessDateChanged = FEPReturnCode.FISCBusinessDateChanged;
	/**
	 * 執行換日發生錯誤(10212)
	 */
	public static FEPReturnCode BusinessDateChangeError = FEPReturnCode.BusinessDateChangeError;
	/**
	 * 財金營業日 CHANGE TO (YYYYMMDD)(10213)
	 */
	public static FEPReturnCode FISCBusinessDateChangeToYMD = FEPReturnCode.FISCBusinessDateChangeToYMD;
	/**
	 * 自行營業日 CHANGE TO (YYYYMMDD)(10214)
	 */
	public static FEPReturnCode CBSBusinessDateChangeToYMD = FEPReturnCode.CBSBusinessDateChangeToYMD;
	/**
	 * 地區主機帳務狀態切換(10215)
	 */
	public static FEPReturnCode CBSModeChange = FEPReturnCode.CBSModeChange;
	/**
	 * EAINET財金營業日 CHANGE TO (YYYYMMDD)(10216)
	 */
	public static FEPReturnCode EAINETBusinessDateChangeToYMD = FEPReturnCode.EAINETBusinessDateChangeToYMD;
	/**
	 * EAINET全行暫停服務 CHANGE TO (Y/N)(10217)
	 */
	public static FEPReturnCode EAINETATMStopChangeToYN = FEPReturnCode.EAINETATMStopChangeToYN;
	/**
	 * 預約跨轉整批重發處理(10218)
	 */
	public static FEPReturnCode RETFRReRun = FEPReturnCode.RETFRReRun;
	/**
	 * 預約跨轉單筆重發(10219)
	 */
	public static FEPReturnCode RETFRSingle = FEPReturnCode.RETFRSingle;
	/**
	 * 原交易帳號有誤(10301)
	 */
	public static FEPReturnCode OriginalACTNONotMatch = FEPReturnCode.OriginalACTNONotMatch;
	/**
	 * 原交易金額有誤(10302)
	 */
	public static FEPReturnCode OriginalAmountNotMatch = FEPReturnCode.OriginalAmountNotMatch;
	/**
	 * 取ATM交易序號錯誤(10303)
	 */
	public static FEPReturnCode GetATMSeqNoError = FEPReturnCode.GetATMSeqNoError;
	/**
	 * 主機交易序號不符(10304)
	 */
	public static FEPReturnCode CBSTXSeqNoNotMatch = FEPReturnCode.CBSTXSeqNoNotMatch;
	/**
	 * 收到沖正交易，找無原交易(10305)
	 */
	public static FEPReturnCode OriginalMessageNotFound = FEPReturnCode.OriginalMessageNotFound;
	/**
	 * 超過限額(10311)
	 */
	public static FEPReturnCode OverLimit = FEPReturnCode.OverLimit;
	/**
	 * 超過EMV累計限額(10312)
	 */
	public static FEPReturnCode OverEMVLimit = FEPReturnCode.OverEMVLimit;
	/**
	 * 銷帳編號檢查碼有誤(10313)
	 */
	public static FEPReturnCode RECNOError = FEPReturnCode.RECNOError;
	/**
	 * 跨行存款，超過累計限額(10314)
	 */
	public static FEPReturnCode OverODTLimit = FEPReturnCode.OverODTLimit;
	/**
	 * 比對主機回應電文資料不符(10315)
	 */
	public static FEPReturnCode CompareTOTANotMatch = FEPReturnCode.CompareTOTANotMatch;
	/**
	 * Garbage資料(10316)
	 */
	public static FEPReturnCode GarbageData = FEPReturnCode.GarbageData;
	/**
	 * FTP上載錯誤(10401)
	 */
	public static FEPReturnCode FTPUpLoadError = FEPReturnCode.FTPUpLoadError;
	/**
	 * FTP下載錯誤(10402)
	 */
	public static FEPReturnCode FTPDownLoadError = FEPReturnCode.FTPDownLoadError;
	/**
	 * 您的理財密碼設定不成功，請洽本行客服專員(10501)
	 */
	public static FEPReturnCode IPINPwdError = FEPReturnCode.IPINPwdError;
	/**
	 * 向聯徵中心查驗P33資訊中(10612)
	 */
	public static FEPReturnCode P33Process = FEPReturnCode.P33Process;
	/**
	 * 依聯徵中心查驗P33之結果，拒絕交易。(10613)
	 */
	public static FEPReturnCode P33Reject = FEPReturnCode.P33Reject;
	/**
	 * 聯徵中心系統維護中，暫無法查驗P33資訊(10614)
	 */
	public static FEPReturnCode P33Maintain = FEPReturnCode.P33Maintain;
	/**
	 * 帳戶/卡片為第一次進行電子支付交易，應進行身分確認(10615)
	 */
	public static FEPReturnCode P33Validate = FEPReturnCode.P33Validate;
	/**
	 * 使用者身分確認結果有誤,，存款單位拒絕交易(10616)
	 */
	public static FEPReturnCode P33IssuerReject = FEPReturnCode.P33IssuerReject;
	/**
	 * 系統錯誤(19999)
	 */
	public static FEPReturnCode SystemError = FEPReturnCode.SystemError;
}