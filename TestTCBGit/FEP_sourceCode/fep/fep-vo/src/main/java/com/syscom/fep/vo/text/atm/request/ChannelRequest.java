package com.syscom.fep.vo.text.atm.request;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.ATMTextBase;

public class ChannelRequest extends ATMTextBase {
	// 空白
	@Field(length = 4)
	private String _TA = StringUtils.EMPTY;

	// 交易別
	@Field(length = 6)
	private String _TXCD = StringUtils.EMPTY;

	// 新舊電文註記
	@Field(length = 2)
	private String _ATMVER_N = StringUtils.EMPTY;

	// ATM版本日期
	@Field(length = 16)
	private String _ATMVER = StringUtils.EMPTY;

	// ATM所屬分行
	@Field(length = 6)
	private String _BRNO = StringUtils.EMPTY;

	// ATM 代號
	@Field(length = 4)
	private String _WSNO = StringUtils.EMPTY;

	// 鈔箱1狀態
	@Field(length = 2)
	private String _NOTES1 = StringUtils.EMPTY;

	// 鈔箱2狀態
	@Field(length = 2)
	private String _NOTES2 = StringUtils.EMPTY;

	// 鈔箱3狀態
	@Field(length = 2)
	private String _NOTES3 = StringUtils.EMPTY;

	// 鈔箱4狀態
	@Field(length = 2)
	private String _NOTES4 = StringUtils.EMPTY;

	// 序時紙捲狀態
	@Field(length = 2)
	private String _JOUS = StringUtils.EMPTY;

	// 交易明細表狀態
	@Field(length = 2)
	private String _ADVS = StringUtils.EMPTY;

	// 存款模組狀態
	@Field(length = 2)
	private String _DEPS = StringUtils.EMPTY;

	// 磁條讀寫頭狀態
	@Field(length = 2)
	private String _MCRWS = StringUtils.EMPTY;

	// ENCRYPTOR狀態
	@Field(length = 2)
	private String _ENCRS = StringUtils.EMPTY;

	// 對帳單模組狀態
	@Field(length = 2)
	private String _STAMS = StringUtils.EMPTY;

	// 吐鈔模組狀態
	@Field(length = 2)
	private String _DISPEN = StringUtils.EMPTY;

	//
	@Field(length = 2)
	private String _FILLER1 = StringUtils.EMPTY;

	// ATM 服務記號
	@Field(length = 2)
	private String _SERVICE = StringUtils.EMPTY;

	// 營業日MODE
	@Field(length = 2)
	private String _MODE = StringUtils.EMPTY;

	// 入帳日
	@Field(length = 16)
	private String _DD = StringUtils.EMPTY;

	// 信封存款MODE
	@Field(length = 2)
	private String _DEPMODE = StringUtils.EMPTY;

	// ATM 系統日
	@Field(length = 16)
	private String _ATMSEQ_1 = StringUtils.EMPTY;

	// ATM交易序號
	@Field(length = 16)
	private String _ATMSEQ_2 = StringUtils.EMPTY;

	// 銀行別
	@Field(length = 6, optional = true)
	private String _BKNO = StringUtils.EMPTY;

	// 交易帳號
	@Field(length = 32, optional = true)
	private String _TXACT = StringUtils.EMPTY;

	// 轉入銀行別
	@Field(length = 6, optional = true)
	private String _BKNO_D = StringUtils.EMPTY;

	// 轉入帳號
	@Field(length = 32, optional = true)
	private String _ACT_D = StringUtils.EMPTY;

	// 交易金額
	@Field(length = 16, optional = true)
	private String _TXAMT = StringUtils.EMPTY;

	// 繳款種類
	@Field(length = 10, optional = true)
	private String _CLASS = StringUtils.EMPTY;

	// 銷帳編號
	@Field(length = 32, optional = true)
	private String _PAYCNO = StringUtils.EMPTY;

	// 繳款到期日
	@Field(length = 16, optional = true)
	private String _DUEDATE = StringUtils.EMPTY;

	// 稽徵機關別
	@Field(length = 6, optional = true)
	private String _UNIT = StringUtils.EMPTY;

	// 身分證字號
	@Field(length = 22, optional = true)
	private String _IDNO = StringUtils.EMPTY;

	// 端末設備查核碼
	@Field(length = 16, optional = true)
	private String _ATMCHK = StringUtils.EMPTY;

	// 晶片卡REMARK欄位資料
	@Field(length = 60, optional = true)
	private String _ICMARK = StringUtils.EMPTY;

	// 晶片卡交易序號
	@Field(length = 16, optional = true)
	private String _ICTXSEQ = StringUtils.EMPTY;

	// 晶片卡交易驗證碼
	@Field(length = 32, optional = true)
	private String _ICTAC = StringUtils.EMPTY;

	// 拒絕理由
	@Field(length = 8, optional = true)
	private String _EXPCD = StringUtils.EMPTY;

	// 委託單位代號
	@Field(length = 16, optional = true)
	private String _VPID = StringUtils.EMPTY;

	// 費用代號
	@Field(length = 8, optional = true)
	private String _PAYID = StringUtils.EMPTY;

	// 附言欄
	@Field(length = 80, optional = true)
	private String _MENO = StringUtils.EMPTY;

	// 前端系統EJFNO
	@Field(length = 100, optional = true)
	private String _CHLEJNO_BODY = StringUtils.EMPTY;

	// 前端系統CHANNEL CODE
	@Field(length = 20, optional = true)
	private String _CHLCODE = StringUtils.EMPTY;

	// 存摺摘要_借方
	@Field(length = 32, optional = true)
	private String _PSBMEMO_D = StringUtils.EMPTY;

	// 存摺摘要_貸方
	@Field(length = 32, optional = true)
	private String _PSBMEMO_C = StringUtils.EMPTY;

	// 存摺備註_借方
	@Field(length = 32, optional = true)
	private String _PSBREM_S_D = StringUtils.EMPTY;

	// 存摺備註_貸方
	@Field(length = 32, optional = true)
	private String _PSBREM_S_C = StringUtils.EMPTY;

	// 往來明細_借方
	@Field(length = 130, optional = true)
	private String _PSBREM_F_D = StringUtils.EMPTY;

	// 往來明細_貸方
	@Field(length = 130, optional = true)
	private String _PSBREM_F_C = StringUtils.EMPTY;

	// 鈔箱5狀態
	@Field(length = 2, optional = true)
	private String _NOTES5 = StringUtils.EMPTY;

	// 鈔箱6狀態
	@Field(length = 2, optional = true)
	private String _NOTES6 = StringUtils.EMPTY;

	// 鈔箱7狀態
	@Field(length = 2, optional = true)
	private String _NOTES7 = StringUtils.EMPTY;

	// 鈔箱8狀態
	@Field(length = 2, optional = true)
	private String _NOTES8 = StringUtils.EMPTY;

	//
	@Field(length = 116, optional = true)
	private String _FILLER2 = StringUtils.EMPTY;

	private static int _TotalLength = 581;

	/**
	 * 空白
	 * 
	 * <remark></remark>
	 */
	public String getTA() {
		return _TA;
	}

	public void setTA(String value) {
		_TA = value;
	}

	/**
	 * 交易別
	 * 
	 * <remark> IWD</remark>
	 */
	public String getTXCD() {
		return _TXCD;
	}

	public void setTXCD(String value) {
		_TXCD = value;
	}

	/**
	 * 新舊電文註記
	 * 
	 * <remark> A</remark>
	 */
	public String getAtmverN() {
		return _ATMVER_N;
	}

	public void setAtmverN(String value) {
		_ATMVER_N = value;
	}

	/**
	 * ATM版本日期
	 * 
	 * <remark> YYYYMMDD</remark>
	 */
	public String getATMVER() {
		return _ATMVER;
	}

	public void setATMVER(String value) {
		_ATMVER = value;
	}

	/**
	 * ATM所屬分行
	 * 
	 * <remark></remark>
	 */
	public String getBRNO() {
		return _BRNO;
	}

	public void setBRNO(String value) {
		_BRNO = value;
	}

	/**
	 * ATM 代號
	 * 
	 * <remark></remark>
	 */
	public String getWSNO() {
		return _WSNO;
	}

	public void setWSNO(String value) {
		_WSNO = value;
	}

	/**
	 * 鈔箱1狀態
	 * 
	 * <remark></remark>
	 */
	public String getNOTES1() {
		return _NOTES1;
	}

	public void setNOTES1(String value) {
		_NOTES1 = value;
	}

	/**
	 * 鈔箱2狀態
	 * 
	 * <remark></remark>
	 */
	public String getNOTES2() {
		return _NOTES2;
	}

	public void setNOTES2(String value) {
		_NOTES2 = value;
	}

	/**
	 * 鈔箱3狀態
	 * 
	 * <remark></remark>
	 */
	public String getNOTES3() {
		return _NOTES3;
	}

	public void setNOTES3(String value) {
		_NOTES3 = value;
	}

	/**
	 * 鈔箱4狀態
	 * 
	 * <remark></remark>
	 */
	public String getNOTES4() {
		return _NOTES4;
	}

	public void setNOTES4(String value) {
		_NOTES4 = value;
	}

	/**
	 * 序時紙捲狀態
	 * 
	 * <remark></remark>
	 */
	public String getJOUS() {
		return _JOUS;
	}

	public void setJOUS(String value) {
		_JOUS = value;
	}

	/**
	 * 交易明細表狀態
	 * 
	 * <remark></remark>
	 */
	public String getADVS() {
		return _ADVS;
	}

	public void setADVS(String value) {
		_ADVS = value;
	}

	/**
	 * 存款模組狀態
	 * 
	 * <remark></remark>
	 */
	public String getDEPS() {
		return _DEPS;
	}

	public void setDEPS(String value) {
		_DEPS = value;
	}

	/**
	 * 磁條讀寫頭狀態
	 * 
	 * <remark></remark>
	 */
	public String getMCRWS() {
		return _MCRWS;
	}

	public void setMCRWS(String value) {
		_MCRWS = value;
	}

	/**
	 * ENCRYPTOR狀態
	 * 
	 * <remark></remark>
	 */
	public String getENCRS() {
		return _ENCRS;
	}

	public void setENCRS(String value) {
		_ENCRS = value;
	}

	/**
	 * 對帳單模組狀態
	 * 
	 * <remark></remark>
	 */
	public String getSTAMS() {
		return _STAMS;
	}

	public void setSTAMS(String value) {
		_STAMS = value;
	}

	/**
	 * 吐鈔模組狀態
	 * 
	 * <remark></remark>
	 */
	public String getDISPEN() {
		return _DISPEN;
	}

	public void setDISPEN(String value) {
		_DISPEN = value;
	}

	/**
	 * 
	 * 
	 * <remark>SPACE</remark>
	 */
	public String getFILLER1() {
		return _FILLER1;
	}

	public void setFILLER1(String value) {
		_FILLER1 = value;
	}

	/**
	 * ATM 服務記號
	 * 
	 * <remark>0:ATM IN SERVICE1:ATM OFF </remark>
	 */
	public String getSERVICE() {
		return _SERVICE;
	}

	public void setSERVICE(String value) {
		_SERVICE = value;
	}

	/**
	 * 營業日MODE
	 * 
	 * <remark>根據上一筆下行電文MODE-O 而來</remark>
	 */
	public String getMODE() {
		return _MODE;
	}

	public void setMODE(String value) {
		_MODE = value;
	}

	/**
	 * 入帳日
	 * 
	 * <remark>YYYYMMDD</remark>
	 */
	public String getDD() {
		return _DD;
	}

	public void setDD(String value) {
		_DD = value;
	}

	/**
	 * 信封存款MODE
	 * 
	 * <remark>根據上一筆下行電文DEPMODE-O而來</remark>
	 */
	public String getDEPMODE() {
		return _DEPMODE;
	}

	public void setDEPMODE(String value) {
		_DEPMODE = value;
	}

	/**
	 * ATM 系統日
	 * 
	 * <remark>YYYYMMDD</remark>
	 */
	public String getAtmseq_1() {
		return _ATMSEQ_1;
	}

	public void setAtmseq_1(String value) {
		_ATMSEQ_1 = value;
	}

	/**
	 * ATM交易序號
	 * 
	 * <remark>ATM SEQ</remark>
	 */
	public String getAtmseq_2() {
		return _ATMSEQ_2;
	}

	public void setAtmseq_2(String value) {
		_ATMSEQ_2 = value;
	}

	/**
	 * 銀行別
	 * 
	 * <remark></remark>
	 */
	public String getBKNO() {
		return _BKNO;
	}

	public void setBKNO(String value) {
		_BKNO = value;
	}

	/**
	 * 交易帳號
	 * 
	 * <remark></remark>
	 */
	public String getTXACT() {
		return _TXACT;
	}

	public void setTXACT(String value) {
		_TXACT = value;
	}

	/**
	 * 轉入銀行別
	 * 
	 * <remark></remark>
	 */
	public String getBknoD() {
		return _BKNO_D;
	}

	public void setBknoD(String value) {
		_BKNO_D = value;
	}

	/**
	 * 轉入帳號
	 * 
	 * <remark></remark>
	 */
	public String getActD() {
		return _ACT_D;
	}

	public void setActD(String value) {
		_ACT_D = value;
	}

	/**
	 * 交易金額
	 * 
	 * <remark></remark>
	 */
	public String getTXAMT() {
		return _TXAMT;
	}

	public void setTXAMT(String value) {
		_TXAMT = value;
	}

	/**
	 * 繳款種類
	 * 
	 * <remark></remark>
	 */
	public String getCLASS() {
		return _CLASS;
	}

	public void setCLASS(String value) {
		_CLASS = value;
	}

	/**
	 * 銷帳編號
	 * 
	 * <remark></remark>
	 */
	public String getPAYCNO() {
		return _PAYCNO;
	}

	public void setPAYCNO(String value) {
		_PAYCNO = value;
	}

	/**
	 * 繳款到期日
	 * 
	 * <remark></remark>
	 */
	public String getDUEDATE() {
		return _DUEDATE;
	}

	public void setDUEDATE(String value) {
		_DUEDATE = value;
	}

	/**
	 * 稽徵機關別
	 * 
	 * <remark></remark>
	 */
	public String getUNIT() {
		return _UNIT;
	}

	public void setUNIT(String value) {
		_UNIT = value;
	}

	/**
	 * 身分證字號
	 * 
	 * <remark></remark>
	 */
	public String getIDNO() {
		return _IDNO;
	}

	public void setIDNO(String value) {
		_IDNO = value;
	}

	/**
	 * 端末設備查核碼
	 * 
	 * <remark></remark>
	 */
	public String getATMCHK() {
		return _ATMCHK;
	}

	public void setATMCHK(String value) {
		_ATMCHK = value;
	}

	/**
	 * 晶片卡REMARK欄位資料
	 * 
	 * <remark></remark>
	 */
	public String getICMARK() {
		return _ICMARK;
	}

	public void setICMARK(String value) {
		_ICMARK = value;
	}

	/**
	 * 晶片卡交易序號
	 * 
	 * <remark></remark>
	 */
	public String getICTXSEQ() {
		return _ICTXSEQ;
	}

	public void setICTXSEQ(String value) {
		_ICTXSEQ = value;
	}

	/**
	 * 晶片卡交易驗證碼
	 * 
	 * <remark></remark>
	 */
	public String getICTAC() {
		return _ICTAC;
	}

	public void setICTAC(String value) {
		_ICTAC = value;
	}

	/**
	 * 拒絕理由
	 * 
	 * <remark></remark>
	 */
	public String getEXPCD() {
		return _EXPCD;
	}

	public void setEXPCD(String value) {
		_EXPCD = value;
	}

	/**
	 * 委託單位代號
	 * 
	 * <remark></remark>
	 */
	public String getVPID() {
		return _VPID;
	}

	public void setVPID(String value) {
		_VPID = value;
	}

	/**
	 * 費用代號
	 * 
	 * <remark></remark>
	 */
	public String getPAYID() {
		return _PAYID;
	}

	public void setPAYID(String value) {
		_PAYID = value;
	}

	/**
	 * 附言欄
	 * 
	 * <remark></remark>
	 */
	public String getMENO() {
		return _MENO;
	}

	public void setMENO(String value) {
		_MENO = value;
	}

	/**
	 * 前端系統EJFNO
	 * 
	 * <remark></remark>
	 */
	public String getChlejnoBody() {
		return _CHLEJNO_BODY;
	}

	public void setChlejnoBody(String value) {
		_CHLEJNO_BODY = value;
	}

	/**
	 * 前端系統CHANNEL CODE
	 * 
	 * <remark></remark>
	 */
	public String getCHLCODE() {
		return _CHLCODE;
	}

	public void setCHLCODE(String value) {
		_CHLCODE = value;
	}

	/**
	 * 存摺摘要_借方
	 * 
	 * <remark></remark>
	 */
	public String getPsbmemoD() {
		return _PSBMEMO_D;
	}

	public void setPsbmemoD(String value) {
		_PSBMEMO_D = value;
	}

	/**
	 * 存摺摘要_貸方
	 * 
	 * <remark></remark>
	 */
	public String getPsbmemoC() {
		return _PSBMEMO_C;
	}

	public void setPsbmemoC(String value) {
		_PSBMEMO_C = value;
	}

	/**
	 * 存摺備註_借方
	 * 
	 * <remark></remark>
	 */
	public String getPsbremSD() {
		return _PSBREM_S_D;
	}

	public void setPsbremSD(String value) {
		_PSBREM_S_D = value;
	}

	/**
	 * 存摺備註_貸方
	 * 
	 * <remark></remark>
	 */
	public String getPsbremSC() {
		return _PSBREM_S_C;
	}

	public void setPsbremSC(String value) {
		_PSBREM_S_C = value;
	}

	/**
	 * 往來明細_借方
	 * 
	 * <remark></remark>
	 */
	public String getPsbremFD() {
		return _PSBREM_F_D;
	}

	public void setPsbremFD(String value) {
		_PSBREM_F_D = value;
	}

	/**
	 * 往來明細_貸方
	 * 
	 * <remark></remark>
	 */
	public String getPsbremFC() {
		return _PSBREM_F_C;
	}

	public void setPsbremFC(String value) {
		_PSBREM_F_C = value;
	}

	/**
	 * 鈔箱5狀態
	 * 
	 * <remark></remark>
	 */
	public String getNOTES5() {
		return _NOTES5;
	}

	public void setNOTES5(String value) {
		_NOTES5 = value;
	}

	/**
	 * 鈔箱6狀態
	 * 
	 * <remark></remark>
	 */
	public String getNOTES6() {
		return _NOTES6;
	}

	public void setNOTES6(String value) {
		_NOTES6 = value;
	}

	/**
	 * 鈔箱7狀態
	 * 
	 * <remark></remark>
	 */
	public String getNOTES7() {
		return _NOTES7;
	}

	public void setNOTES7(String value) {
		_NOTES7 = value;
	}

	/**
	 * 鈔箱8狀態
	 * 
	 * <remark></remark>
	 */
	public String getNOTES8() {
		return _NOTES8;
	}

	public void setNOTES8(String value) {
		_NOTES8 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getFILLER2() {
		return _FILLER2;
	}

	public void setFILLER2(String value) {
		_FILLER2 = value;
	}

	/**
	 * 電文總長度
	 * 
	 * <remark>該組電文總長度</remark>
	 */
	@Override
	public int getTotalLength() {
		return _TotalLength;
	}

	@Override
	public ATMGeneral parseFlatfile(String flatfile) throws Exception {
		return this.parseFlatfile(this.getClass(), flatfile);
	}

	@Override
	public String makeMessageFromGeneral(ATMGeneral general) throws Exception {
		ATMGeneralRequest request = general.getRequest();
		/*
		this._TA = StringUtil.toHex(StringUtils.rightPad(request.getTA(), 2, StringUtils.SPACE)); // 空白
		this._TXCD = StringUtil.toHex(StringUtils.rightPad(request.getTXCD(), 3, StringUtils.SPACE)); // 交易別
		this._ATMVER_N = StringUtil.toHex(StringUtils.rightPad(request.getAtmverN(), 1, StringUtils.SPACE)); // 新舊電文註記
		this._ATMVER = StringUtil.toHex(StringUtils.rightPad(request.getATMVER(), 8, StringUtils.SPACE)); // ATM版本日期
		this._BRNO = StringUtil.toHex(StringUtils.rightPad(request.getBRNO(), 3, StringUtils.SPACE)); // ATM所屬分行
		this._WSNO = StringUtil.toHex(StringUtils.rightPad(request.getWSNO(), 2, StringUtils.SPACE)); // ATM 代號
		this._NOTES1 = StringUtil.toHex(StringUtils.rightPad(request.getNOTES1(), 1, StringUtils.SPACE)); // 鈔箱1狀態
		this._NOTES2 = StringUtil.toHex(StringUtils.rightPad(request.getNOTES2(), 1, StringUtils.SPACE)); // 鈔箱2狀態
		this._NOTES3 = StringUtil.toHex(StringUtils.rightPad(request.getNOTES3(), 1, StringUtils.SPACE)); // 鈔箱3狀態
		this._NOTES4 = StringUtil.toHex(StringUtils.rightPad(request.getNOTES4(), 1, StringUtils.SPACE)); // 鈔箱4狀態
		this._JOUS = StringUtil.toHex(StringUtils.rightPad(request.getJOUS(), 1, StringUtils.SPACE)); // 序時紙捲狀態
		this._ADVS = StringUtil.toHex(StringUtils.rightPad(request.getADVS(), 1, StringUtils.SPACE)); // 交易明細表狀態
		this._DEPS = StringUtil.toHex(StringUtils.rightPad(request.getDEPS(), 1, StringUtils.SPACE)); // 存款模組狀態
		this._MCRWS = StringUtil.toHex(StringUtils.rightPad(request.getMCRWS(), 1, StringUtils.SPACE)); // 磁條讀寫頭狀態
		this._ENCRS = StringUtil.toHex(StringUtils.rightPad(request.getENCRS(), 1, StringUtils.SPACE)); // ENCRYPTOR狀態
		this._STAMS = StringUtil.toHex(StringUtils.rightPad(request.getSTAMS(), 1, StringUtils.SPACE)); // 對帳單模組狀態
		this._DISPEN = StringUtil.toHex(StringUtils.rightPad(request.getDISPEN(), 1, StringUtils.SPACE)); // 吐鈔模組狀態
		this._FILLER1 = StringUtil.toHex(StringUtils.leftPad(StringUtils.EMPTY, 1, StringUtils.SPACE));
		this._SERVICE = StringUtil.toHex(StringUtils.rightPad(request.getSERVICE(), 1, StringUtils.SPACE)); // ATM 服務記號
		this._MODE = StringUtil.toHex(StringUtils.rightPad(request.getMODE(), 1, StringUtils.SPACE)); // 營業日MODE
		this._DD = StringUtil.toHex(StringUtils.rightPad(request.getDD(), 8, StringUtils.SPACE)); // 入帳日
		this._DEPMODE = StringUtil.toHex(StringUtils.rightPad(request.getDEPMODE(), 1, StringUtils.SPACE)); // 信封存款MODE
		this._ATMSEQ_1 = StringUtil.toHex(StringUtils.rightPad(request.getAtmseq_1(), 8, StringUtils.SPACE)); // ATM 系統日
		this._ATMSEQ_2 = StringUtil.toHex(StringUtils.rightPad(request.getAtmseq_2(), 8, StringUtils.SPACE)); // ATM交易序號
		this._BKNO = StringUtils.rightPad(request.getBKNO(), 3, StringUtils.SPACE); // 銀行別
		this._TXACT = StringUtils.rightPad(request.getTXACT(), 16, StringUtils.SPACE); // 交易帳號
		this._BKNO_D = StringUtils.rightPad(request.getBknoD(), 3, StringUtils.SPACE); // 轉入銀行別
		this._ACT_D = StringUtils.rightPad(request.getActD(), 16, StringUtils.SPACE); // 轉入帳號
		this._TXAMT = this.toString(request.getTXAMT(), 8, 0); // 交易金額
		this._CLASS = StringUtils.rightPad(request.getCLASS(), 5, StringUtils.SPACE); // 繳款種類
		this._PAYCNO = StringUtils.rightPad(request.getPAYCNO(), 16, StringUtils.SPACE); // 銷帳編號
		this._DUEDATE = StringUtils.rightPad(request.getDUEDATE(), 8, StringUtils.SPACE); // 繳款到期日
		this._UNIT = StringUtils.rightPad(request.getUNIT(), 3, StringUtils.SPACE); // 稽徵機關別
		this._IDNO = StringUtils.rightPad(request.getIDNO(), 11, StringUtils.SPACE); // 身分證字號
		this._ATMCHK = StringUtils.rightPad(request.getATMCHK(), 8, StringUtils.SPACE); // 端末設備查核碼
		this._ICMARK = StringUtils.rightPad(request.getICMARK(), 30, StringUtils.SPACE); // 晶片卡REMARK欄位資料
		this._ICTXSEQ = StringUtils.rightPad(request.getICTXSEQ(), 8, StringUtils.SPACE); // 晶片卡交易序號
		this._ICTAC = StringUtils.rightPad(request.getICTAC(), 16, StringUtils.SPACE); // 晶片卡交易驗證碼
		this._EXPCD = StringUtils.rightPad(request.getEXPCD(), 4, StringUtils.SPACE); // 拒絕理由
		this._VPID = StringUtils.rightPad(request.getVPID(), 8, StringUtils.SPACE); // 委託單位代號
		this._PAYID = StringUtils.rightPad(request.getPAYID(), 4, StringUtils.SPACE); // 費用代號
		this._MENO = StringUtils.rightPad(request.getMENO(), 40, StringUtils.SPACE); // 附言欄
		this._CHLEJNO_BODY = StringUtils.rightPad(request.getChlejnoBody(), 50, StringUtils.SPACE); // 前端系統EJFNO
		this._CHLCODE = StringUtils.rightPad(request.getCHLCODE(), 10, StringUtils.SPACE); // 前端系統CHANNEL CODE
		this._PSBMEMO_D = StringUtils.rightPad(request.getPsbmemoD(), 16, StringUtils.SPACE); // 存摺摘要_借方
		this._PSBMEMO_C = StringUtils.rightPad(request.getPsbmemoC(), 16, StringUtils.SPACE); // 存摺摘要_貸方
		this._PSBREM_S_D = StringUtils.rightPad(request.getPsbremSD(), 16, StringUtils.SPACE); // 存摺備註_借方
		this._PSBREM_S_C = StringUtils.rightPad(request.getPsbremSC(), 16, StringUtils.SPACE); // 存摺備註_貸方
		this._PSBREM_F_D = StringUtils.rightPad(request.getPsbremFD(), 65, StringUtils.SPACE); // 往來明細_借方
		this._PSBREM_F_C = StringUtils.rightPad(request.getPsbremFC(), 65, StringUtils.SPACE); // 往來明細_貸方
		this._NOTES5 = StringUtil.toHex(StringUtils.rightPad(request.getNOTES5(), 1, StringUtils.SPACE)); // 鈔箱5狀態
		this._NOTES6 = StringUtil.toHex(StringUtils.rightPad(request.getNOTES6(), 1, StringUtils.SPACE)); // 鈔箱6狀態
		this._NOTES7 = StringUtil.toHex(StringUtils.rightPad(request.getNOTES7(), 1, StringUtils.SPACE)); // 鈔箱7狀態
		this._NOTES8 = StringUtil.toHex(StringUtils.rightPad(request.getNOTES8(), 1, StringUtils.SPACE)); // 鈔箱8狀態
		this._FILLER2 = StringUtil.toHex(StringUtils.leftPad("", 58, StringUtils.SPACE));
		*/
		return StringUtils.join(this._TA, this._TXCD, this._ATMVER_N, this._ATMVER, this._BRNO, this._WSNO, this._NOTES1, this._NOTES2, this._NOTES3, this._NOTES4, this._JOUS, this._ADVS, this._DEPS,
				this._MCRWS, this._ENCRS, this._STAMS, this._DISPEN, this._FILLER1, this._SERVICE, this._MODE, this._DD, this._DEPMODE, this._ATMSEQ_1, this._ATMSEQ_2, this._BKNO, this._TXACT,
				this._BKNO_D, this._ACT_D, this._TXAMT, this._CLASS, this._PAYCNO, this._DUEDATE, this._UNIT, this._IDNO, this._ATMCHK, this._ICMARK, this._ICTXSEQ, this._ICTAC, this._EXPCD,
				this._VPID, this._PAYID, this._MENO, this._CHLEJNO_BODY, this._CHLCODE, this._PSBMEMO_D, this._PSBMEMO_C, this._PSBREM_S_D, this._PSBREM_S_C, this._PSBREM_F_D, this._PSBREM_F_C,
				this._NOTES5, this._NOTES6, this._NOTES7, this._NOTES8, this._FILLER2);
	}

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {}
}
