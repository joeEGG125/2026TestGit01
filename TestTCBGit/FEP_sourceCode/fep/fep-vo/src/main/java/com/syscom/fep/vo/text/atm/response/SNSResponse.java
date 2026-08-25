package com.syscom.fep.vo.text.atm.response;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralResponse;
import com.syscom.fep.vo.text.atm.ATMTextBase;

public class SNSResponse extends ATMTextBase {
	// 交易類別
	@Field(length = 6)
	private String _TXCD_O = StringUtils.EMPTY;

	// 交易日期
	@Field(length = 16)
	private String _DATE = StringUtils.EMPTY;

	// 交易時間
	@Field(length = 12)
	private String _TIME = StringUtils.EMPTY;

	// ATM ID
	@Field(length = 10)
	private String _ATMNO_O = StringUtils.EMPTY;

	// MODE
	@Field(length = 2)
	private String _MODE_O = StringUtils.EMPTY;

	// 本營業日
	@Field(length = 16)
	private String _DD_O = StringUtils.EMPTY;

	// 信封存款MODE
	@Field(length = 2)
	private String _DEPMODE_O = StringUtils.EMPTY;

	// ATM序號1
	@Field(length = 16)
	private String _ATMSEQ_O1 = StringUtils.EMPTY;

	// ATM序號2
	@Field(length = 16)
	private String _ATMSEQ_O2 = StringUtils.EMPTY;

	// 跑馬燈的訊息型態
	@Field(length = 4, optional = true)
	private String _MSGTYPE = StringUtils.EMPTY;

	// 播放
	@Field(length = 2, optional = true)
	private String _ACTION = StringUtils.EMPTY;

	// 預計暫停服務的開始時間
	@Field(length = 28, optional = true)
	private String _TIME_S = StringUtils.EMPTY;

	// 預計暫停服務的結束時間
	@Field(length = 28, optional = true)
	private String _TIME_E = StringUtils.EMPTY;

	// 要抓取的檔案,檔案內的文字會以跑馬燈顯示
	@Field(length = 48, optional = true)
	private String _FILENAME = StringUtils.EMPTY;

	//
	@Field(length = 1290, optional = true)
	private String _FILLER1 = StringUtils.EMPTY;

	private static int _TotalLength = 748;

	/**
	 * 交易類別
	 * 
	 * <remark>IAC</remark>
	 */
	public String getTxcdO() {
		return _TXCD_O;
	}

	public void setTxcdO(String value) {
		_TXCD_O = value;
	}

	/**
	 * 交易日期
	 * 
	 * <remark>YYYYMMDD</remark>
	 */
	public String getDATE() {
		return _DATE;
	}

	public void setDATE(String value) {
		_DATE = value;
	}

	/**
	 * 交易時間
	 * 
	 * <remark></remark>
	 */
	public String getTIME() {
		return _TIME;
	}

	public void setTIME(String value) {
		_TIME = value;
	}

	/**
	 * ATM ID
	 * 
	 * <remark></remark>
	 */
	public String getAtmnoO() {
		return _ATMNO_O;
	}

	public void setAtmnoO(String value) {
		_ATMNO_O = value;
	}

	/**
	 * MODE
	 * 
	 * <remark>1: Online MODE2: Half Online MODE3: Night MODE4: Reentry MODE5:Go to Online MODE</remark>
	 */
	public String getModeO() {
		return _MODE_O;
	}

	public void setModeO(String value) {
		_MODE_O = value;
	}

	/**
	 * 本營業日
	 * 
	 * <remark>YYYYMMDD</remark>
	 */
	public String getDdO() {
		return _DD_O;
	}

	public void setDdO(String value) {
		_DD_O = value;
	}

	/**
	 * 信封存款MODE
	 * 
	 * <remark>2:仍在次交票/現金分割時間內6:已不在次交票/現金分割時間內1,5 for 星期六上班使用</remark>
	 */
	public String getDepmodeO() {
		return _DEPMODE_O;
	}

	public void setDepmodeO(String value) {
		_DEPMODE_O = value;
	}

	/**
	 * ATM序號1
	 * 
	 * <remark>YYYYMMDD</remark>
	 */
	public String getAtmseqO1() {
		return _ATMSEQ_O1;
	}

	public void setAtmseqO1(String value) {
		_ATMSEQ_O1 = value;
	}

	/**
	 * ATM序號2
	 * 
	 * <remark>ATMSEQ</remark>
	 */
	public String getAtmseqO2() {
		return _ATMSEQ_O2;
	}

	public void setAtmseqO2(String value) {
		_ATMSEQ_O2 = value;
	}

	/**
	 * 跑馬燈的訊息型態
	 * 
	 * <remark>= 1 : 24HR ATM = 2 : FISC = 3 : BSP = 4 : MSG FROM FILE </remark>
	 */
	public String getMSGTYPE() {
		return _MSGTYPE;
	}

	public void setMSGTYPE(String value) {
		_MSGTYPE = value;
	}

	/**
	 * 播放
	 * 
	 * <remark>"= 1 : 開始播放 = 2 : 結束播放</remark>
	 */
	public String getACTION() {
		return _ACTION;
	}

	public void setACTION(String value) {
		_ACTION = value;
	}

	/**
	 * 預計暫停服務的開始時間
	 * 
	 * <remark>西元年月日時分秒</remark>
	 */
	public String getTimeS() {
		return _TIME_S;
	}

	public void setTimeS(String value) {
		_TIME_S = value;
	}

	/**
	 * 預計暫停服務的結束時間
	 * 
	 * <remark>西元年月日時分秒</remark>
	 */
	public String getTimeE() {
		return _TIME_E;
	}

	public void setTimeE(String value) {
		_TIME_E = value;
	}

	/**
	 * 要抓取的檔案,檔案內的文字會以跑馬燈顯示
	 * 
	 * <remark>MSGTYPE = 4才會使用</remark>
	 */
	public String getFILENAME() {
		return _FILENAME;
	}

	public void setFILENAME(String value) {
		_FILENAME = value;
	}

	/**
	 * 
	 * <remark></remark>
	 */
	public String getFILLER1() {
		return _FILLER1;
	}

	public void setFILLER1(String value) {
		_FILLER1 = value;
	}

	@Override
	public ATMGeneral parseFlatfile(String flatfile) throws Exception {
		return null;
	}

	@Override
	public int getTotalLength() {
		return _TotalLength;
	}

	@Override
	public String makeMessageFromGeneral(ATMGeneral general) throws Exception {
		/*
		ATMGeneralResponse response = general.getResponse();
		this._TXCD_O = StringUtil.toHex(StringUtils.rightPad(response.getTxcdO(), 3, StringUtils.SPACE));// 交易類別
		this._DATE = StringUtil.toHex(StringUtils.leftPad(response.getDATE(), 8, "0"));// 交易日期
		this._TIME = StringUtil.toHex(StringUtils.leftPad(response.getTIME(), 6, "0"));// 交易時間
		this._ATMNO_O = StringUtil.toHex(StringUtils.leftPad(response.getAtmnoO(), 5, "0"));// ATM ID
		this._MODE_O = StringUtil.toHex(StringUtils.rightPad(response.getModeO(), 1, StringUtils.SPACE));// MODE
		this._DD_O = StringUtil.toHex(StringUtils.leftPad(response.getDdO(), 8, "0"));// 本營業日
		this._DEPMODE_O = StringUtil.toHex(StringUtils.rightPad(response.getDepmodeO(), 1, StringUtils.SPACE));// 信封存款MODE
		this._ATMSEQ_O1 = StringUtil.toHex(StringUtils.leftPad(response.getAtmseqO1(), 8, "0"));// ATM序號1
		this._ATMSEQ_O2 = StringUtil.toHex(StringUtils.leftPad(response.getAtmseqO2(), 8, "0"));// ATM序號2
		this._MSGTYPE = StringUtil.toHex(StringUtils.rightPad(response.getMSGTYPE(), 2, StringUtils.SPACE));// 跑馬燈的訊息型態
		this._ACTION = StringUtil.toHex(StringUtils.rightPad(response.getACTION(), 1, StringUtils.SPACE));// 播放
		this._TIME_S = StringUtil.toHex(StringUtils.leftPad(response.getTimeS(), 14, "0"));// 預計暫停服務的開始時間
		this._TIME_E = StringUtil.toHex(StringUtils.leftPad(response.getTimeE(), 14, "0"));// 預計暫停服務的結束時間
		this._FILENAME = StringUtil.toHex(StringUtils.leftPad(response.getFILENAME(), 24, StringUtils.SPACE));// 要抓取的檔案,檔案內的文字會以跑馬燈顯示
		this._FILLER1 = StringUtil.toHex(StringUtils.leftPad(StringUtils.EMPTY, 645, StringUtils.SPACE));//
		return StringUtils.join(this._TXCD_O, this._DATE, this._TIME, this._ATMNO_O, this._MODE_O, this._DD_O, this._DEPMODE_O, this._ATMSEQ_O1, this._ATMSEQ_O2, this._MSGTYPE, this._ACTION, this._TIME_S, this._TIME_E, this._FILENAME, this._FILLER1);
			*/return "";
    }

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {}

}
