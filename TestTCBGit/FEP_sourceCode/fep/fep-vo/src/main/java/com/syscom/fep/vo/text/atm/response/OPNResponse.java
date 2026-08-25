package com.syscom.fep.vo.text.atm.response;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralResponse;
import com.syscom.fep.vo.text.atm.ATMTextBase;

public class OPNResponse extends ATMTextBase {
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

	//
	@Field(length = 1400, optional = true)
	private String _FILLER1 = StringUtils.EMPTY;

	private static int _TotalLength = 748;

	/**
	 * 交易類別
	 * 
	 * IAC
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
	 * YYYYMMDD
	 */
	public String getDATE() {
		return _DATE;
	}

	public void setDATE(String value) {
		_DATE = value;
	}

	/**
	 * 交易時間
	 */
	public String getTIME() {
		return _TIME;
	}

	public void setTIME(String value) {
		_TIME = value;
	}

	/**
	 * ATM ID
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
	 * 1: Online MODE2: Half Online MODE3: Night MODE4: Reentry MODE5:Go to Online MODE
	 */
	public String getModeO() {
		return _MODE_O;
	}

	public void setModeO(String value) {
		_MODE_O = value;
	}

	/**
	 * 本營業日
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
	 * 2:仍在次交票/現金分割時間內6:已不在次交票/現金分割時間內1,5 for 星期六上班使用
	 */
	public String getDepmodeO() {
		return _DEPMODE_O;
	}

	public void setDepmodeO(String value) {
		_DEPMODE_O = value;
	}

	/**
	 * ATM序號1
	 */
	public String getAtmseqO1() {
		return _ATMSEQ_O1;
	}

	public void setAtmseqO1(String value) {
		_ATMSEQ_O1 = value;
	}

	/**
	 * ATM序號2
	 */
	public String getAtmseqO2() {
		return _ATMSEQ_O2;
	}

	public void setAtmseqO2(String value) {
		_ATMSEQ_O2 = value;
	}

	/**
	 * 
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
		this._FILLER1 = StringUtil.toHex(StringUtils.leftPad("", 700, StringUtils.SPACE));//
		return StringUtils.join(this._TXCD_O, this._DATE, this._TIME, this._ATMNO_O, this._MODE_O, this._DD_O, this._DEPMODE_O, this._ATMSEQ_O1, this._ATMSEQ_O2, this._FILLER1);
		*/return "";
    }

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {}

}
