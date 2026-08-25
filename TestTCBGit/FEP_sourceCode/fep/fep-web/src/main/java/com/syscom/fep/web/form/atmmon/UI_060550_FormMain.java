package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.web.form.BaseForm;

public class UI_060550_FormMain extends BaseForm {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 營業日
	 */
	private String feptxnTbsdyFisc;
	/**
	 * 交易時間起
	 */
	private String feptxnTxTimeBegin;
	/**
	 * 交易時間訖
	 */
	private String feptxnTxTimeEnd;
	/**
	 * 交易日期
	 */
	private String feptxnTxDate;
	/**
	 * 交易結果
	 */
	private String feptxnTxrust;
	/**
	 * ATM代號
	 */
	private String feptxnAtmno;
	/**
	 * 發卡區T24序號
	 */
	private String feptxnCbsRrn;
	/**
	 * ATM交易序號
	 */
	private String feptxnAtmSeqno;
	/**
	 * ATM CON交易序號
	 */
	private String feptxnConAtmSeqno2;
	/**
	 * 交易區T24序號
	 */
	private String feptxnVirCbsRrn;
	/**
	 * 扣款帳號-銀行代號
	 */
	private String feptxnTroutBkno;
	/**
	 * 扣款帳號-使用者賬號
	 */
	private String feptxnTroutActno;
	/**
	 * 轉入帳號-銀行代號
	 */
	private String feptxnTrinBkno;
	/**
	 * 轉入帳號-使用者賬號
	 */
	private String feptxnTrinActno;
	/**
	 * EJ序號, 支持輸入多筆, 用逗號隔開
	 */
	private String feptxnEjfno;

	public String getFeptxnChannelEjfno() {
		return feptxnChannelEjfno;
	}

	public void setFeptxnChannelEjfno(String feptxnChannelEjfno) {
		this.feptxnChannelEjfno = feptxnChannelEjfno;
	}

	/**
	 * ClientTraceId
	 */
	private String feptxnChannelEjfno;
	/**
	 * CON EJ序號
	 */
	private String feptxnTraceEjfno;
	/**
	 * ATM交易代號
	 */
	private String feptxnTxCode;
	/**
	 * 訊息代號
	 */
	private String feptxnMsgid;
	/**
	 * 提領幣別
	 */
	private String feptxnTxCurAct;
	/**
	 * 交易金額
	 */
	private String feptxnTxAmt;
	/**
	 * 財金 STAN-銀行代號
	 */
	private String feptxnBkno;
	/**
	 * 財金 STAN
	 */
	private String feptxnStan;
	/**
	 * 多幣Debit卡checkbox, true表示有勾選
	 */
	private boolean checkFeptxnMulticur;
	/**
	 * 疫情減免手續費checkbox, true表示有勾選
	 */
	private boolean checkFeptxnCovid;
	/**
	 * 財金交易代號(PCODE)
	 */
	private String feptxnPcode;
	/**
	 * 自行checkbox, true表示有勾選
	 */
	private boolean checkTrin;
	/**
	 * 跨行checkbox, true表示有勾選
	 */
	private boolean checkTrout;
	/**
	 * 無卡提款(台外幣)checkbox, true表示有勾選
	 */
	private boolean checkCtrlnwd;
	/**
	 * 排除交易別, 多筆資料用;間隔
	 */
	private String feptxnExcludeTxCode;
	/**
	 * 記帳類別
	 */
	private String feptxnAccType;
	/**
	 * 小額跨轉每日優惠checkbox, true表示有勾選
	 */
	private boolean checkFeptxnBenefit;
	/**
	 * 手機門號轉帳checkbox, true表示有勾選
	 */
	private boolean checkFeptxnMtp;
	/**
	 * 卡片帳號
	 */
	private String feptxnTroutBknoForCard;
	private String feptxnMajorActno;
	/**
	 * 繳納口罩費用checkbox, true表示有勾選
	 */
	private boolean checkMask;
	/**
	 * 使用者權限, false表示為FEP_Service或FEP_Monitor
	 */
	private boolean hasFepService;
	
	private String feptxnCbsProc;

	/**
	 * 通道
	 */
	private String feptxnChannel;

	public String getFeptxnTbsdyFisc() {
		return feptxnTbsdyFisc;
	}

	public void setFeptxnTbsdyFisc(String feptxnTbsdyFisc) {
		this.feptxnTbsdyFisc = feptxnTbsdyFisc;
	}

	public String getFeptxnTxTimeBegin() {
		return feptxnTxTimeBegin;
	}

	public void setFeptxnTxTimeBegin(String feptxnTxTimeBegin) {
		this.feptxnTxTimeBegin = feptxnTxTimeBegin;
	}

	public String getFeptxnTxTimeEnd() {
		return feptxnTxTimeEnd;
	}

	public void setFeptxnTxTimeEnd(String feptxnTxTimeEnd) {
		this.feptxnTxTimeEnd = feptxnTxTimeEnd;
	}

	public String getFeptxnTxDate() {
		return feptxnTxDate;
	}

	public void setFeptxnTxDate(String feptxnTxDate) {
		this.feptxnTxDate = feptxnTxDate;
	}

	public String getFeptxnTxrust() {
		return feptxnTxrust;
	}

	public void setFeptxnTxrust(String feptxnTxrust) {
		this.feptxnTxrust = feptxnTxrust;
	}

	public String getFeptxnAtmno() {
		return feptxnAtmno;
	}

	public void setFeptxnAtmno(String feptxnAtmno) {
		this.feptxnAtmno = feptxnAtmno;
	}

	public String getFeptxnCbsRrn() {
		return feptxnCbsRrn;
	}

	public void setFeptxnCbsRrn(String feptxnCbsRrn) {
		this.feptxnCbsRrn = feptxnCbsRrn;
	}

	public String getFeptxnAtmSeqno() {
		return feptxnAtmSeqno;
	}

	public void setFeptxnAtmSeqno(String feptxnAtmSeqno) {
		this.feptxnAtmSeqno = feptxnAtmSeqno;
	}

	public String getFeptxnConAtmSeqno2() {
		return feptxnConAtmSeqno2;
	}

	public void setFeptxnConAtmSeqno2(String feptxnConAtmSeqno2) {
		this.feptxnConAtmSeqno2 = feptxnConAtmSeqno2;
	}

	public String getFeptxnVirCbsRrn() {
		return feptxnVirCbsRrn;
	}

	public void setFeptxnVirCbsRrn(String feptxnVirCbsRrn) {
		this.feptxnVirCbsRrn = feptxnVirCbsRrn;
	}

	public String getFeptxnTroutBkno() {
		return feptxnTroutBkno;
	}

	public void setFeptxnTroutBkno(String feptxnTroutBkno) {
		this.feptxnTroutBkno = feptxnTroutBkno;
	}

	public String getFeptxnTroutActno() {
		return feptxnTroutActno;
	}

	public void setFeptxnTroutActno(String feptxnTroutActno) {
		this.feptxnTroutActno = feptxnTroutActno;
	}

	public String getFeptxnTrinBkno() {
		return feptxnTrinBkno;
	}

	public void setFeptxnTrinBkno(String feptxnTrinBkno) {
		this.feptxnTrinBkno = feptxnTrinBkno;
	}

	public String getFeptxnTrinActno() {
		return feptxnTrinActno;
	}

	public void setFeptxnTrinActno(String feptxnTrinActno) {
		this.feptxnTrinActno = feptxnTrinActno;
	}

	public String getFeptxnEjfno() {
		return feptxnEjfno;
	}

	public void setFeptxnEjfno(String feptxnEjfno) {
		this.feptxnEjfno = feptxnEjfno;
	}

	public String getFeptxnTraceEjfno() {
		return feptxnTraceEjfno;
	}

	public void setFeptxnTraceEjfno(String feptxnTraceEjfno) {
		this.feptxnTraceEjfno = feptxnTraceEjfno;
	}

	public String getFeptxnTxCode() {
		return feptxnTxCode;
	}

	public void setFeptxnTxCode(String feptxnTxCode) {
		this.feptxnTxCode = feptxnTxCode;
	}

	public String getFeptxnMsgid() {
		return feptxnMsgid;
	}

	public void setFeptxnMsgid(String feptxnMsgid) {
		this.feptxnMsgid = feptxnMsgid;
	}

	public String getFeptxnTxCurAct() {
		return feptxnTxCurAct;
	}

	public void setFeptxnTxCurAct(String feptxnTxCurAct) {
		this.feptxnTxCurAct = feptxnTxCurAct;
	}

	public String getFeptxnTxAmt() {
		return feptxnTxAmt;
	}

	public void setFeptxnTxAmt(String feptxnTxAmt) {
		this.feptxnTxAmt = feptxnTxAmt;
	}

	public String getFeptxnBkno() {
		return feptxnBkno;
	}

	public void setFeptxnBkno(String feptxnBkno) {
		this.feptxnBkno = feptxnBkno;
	}

	public String getFeptxnStan() {
		return feptxnStan;
	}

	public void setFeptxnStan(String feptxnStan) {
		this.feptxnStan = feptxnStan;
	}

	public boolean isCheckFeptxnMulticur() {
		return checkFeptxnMulticur;
	}

	public void setCheckFeptxnMulticur(boolean feptxnMulticur) {
		this.checkFeptxnMulticur = feptxnMulticur;
	}

	public boolean isCheckFeptxnCovid() {
		return checkFeptxnCovid;
	}

	public void setCheckFeptxnCovid(boolean feptxnCovid) {
		this.checkFeptxnCovid = feptxnCovid;
	}

	public String getFeptxnPcode() {
		return feptxnPcode;
	}

	public void setFeptxnPcode(String feptxnPcode) {
		this.feptxnPcode = feptxnPcode;
	}

	public boolean isCheckTrin() {
		return checkTrin;
	}

	public void setCheckTrin(boolean checkTrin) {
		this.checkTrin = checkTrin;
	}

	public boolean isCheckTrout() {
		return checkTrout;
	}

	public void setCheckTrout(boolean checkTrout) {
		this.checkTrout = checkTrout;
	}

	public boolean isCheckCtrlnwd() {
		return checkCtrlnwd;
	}

	public void setCheckCtrlnwd(boolean checkCtrlnwd) {
		this.checkCtrlnwd = checkCtrlnwd;
	}

	public String getFeptxnExcludeTxCode() {
		return feptxnExcludeTxCode;
	}

	public void setFeptxnExcludeTxCode(String feptxnExcludeTxCode) {
		this.feptxnExcludeTxCode = feptxnExcludeTxCode;
	}

	public String getFeptxnAccType() {
		return feptxnAccType;
	}

	public void setFeptxnAccType(String feptxnAccType) {
		this.feptxnAccType = feptxnAccType;
	}

	public boolean isCheckFeptxnBenefit() {
		return checkFeptxnBenefit;
	}

	public void setCheckFeptxnBenefit(boolean checkFeptxnBenefit) {
		this.checkFeptxnBenefit = checkFeptxnBenefit;
	}

	public boolean isCheckFeptxnMtp() {
		return checkFeptxnMtp;
	}

	public void setCheckFeptxnMtp(boolean feptxnMtp) {
		this.checkFeptxnMtp = feptxnMtp;
	}

	public String getFeptxnTroutBknoForCard() {
		return feptxnTroutBknoForCard;
	}

	public void setFeptxnTroutBknoForCard(String feptxnTroutBknoForCard) {
		this.feptxnTroutBknoForCard = feptxnTroutBknoForCard;
	}

	public String getFeptxnMajorActno() {
		return feptxnMajorActno;
	}

	public void setFeptxnMajorActno(String feptxnMajorActno) {
		this.feptxnMajorActno = feptxnMajorActno;
	}

	public boolean isCheckMask() {
		return checkMask;
	}

	public void setCheckMask(boolean checkMask) {
		this.checkMask = checkMask;
	}

	public String getFeptxnCbsProc() {
		return feptxnCbsProc;
	}

	public void setFeptxnCbsProc(String feptxnCbsProc) {
		this.feptxnCbsProc = feptxnCbsProc;
	}

	public String getFeptxnChannel() {
		return feptxnChannel;
	}

	public void setFeptxnChannel(String feptxnChannel) {
		this.feptxnChannel = feptxnChannel;
	}

    public boolean isHasFepService() {
        return hasFepService;
    }

    public void setHasFepService(boolean hasFepService) {
        this.hasFepService = hasFepService;
    }
}
