package com.syscom.fep.vo.text.atm.request;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.ATMTextBase;

public class ODERequest extends ATMTextBase {
	// 空白
	@Field(length = 4)
	private String _TA = "";

	// 交易別
	@Field(length = 6)
	private String _TXCD = "";

	// 新舊電文註記
	@Field(length = 2)
	private String _ATMVER_N = "";

	// ATM版本日期
	@Field(length = 16)
	private String _ATMVER = "";

	// ATM所屬分行
	@Field(length = 6)
	private String _BRNO = "";

	// ATM 代號
	@Field(length = 4)
	private String _WSNO = "";

	// 鈔箱1狀態
	@Field(length = 2)
	private String _NOTES1 = "";

	// 鈔箱2狀態
	@Field(length = 2)
	private String _NOTES2 = "";

	// 鈔箱3狀態
	@Field(length = 2)
	private String _NOTES3 = "";

	// 鈔箱4狀態
	@Field(length = 2)
	private String _NOTES4 = "";

	// 序時紙捲狀態
	@Field(length = 2)
	private String _JOUS = "";

	// 交易明細表狀態
	@Field(length = 2)
	private String _ADVS = "";

	// 存款模組狀態
	@Field(length = 2)
	private String _DEPS = "";

	// 磁條讀寫頭狀態
	@Field(length = 2)
	private String _MCRWS = "";

	// ENCRYPTOR狀態
	@Field(length = 2)
	private String _ENCRS = "";

	// 對帳單模組狀態
	@Field(length = 2)
	private String _STAMS = "";

	// 吐鈔模組狀態
	@Field(length = 2)
	private String _DISPEN = "";

	//
	@Field(length = 2)
	private String _FILLER1 = "";

	// ATM 服務記號
	@Field(length = 2)
	private String _SERVICE = "";

	// 營業日MODE
	@Field(length = 2)
	private String _MODE = "";

	// 入帳日
	@Field(length = 16)
	private String _DD = "";

	// 信封存款MODE
	@Field(length = 2)
	private String _DEPMODE = "";

	// ATM 系統日
	@Field(length = 16)
	private String _ATMSEQ_1 = "";

	// ATM交易序號
	@Field(length = 16)
	private String _ATMSEQ_2 = "";

	// 銀行別
	@Field(length = 6, optional = true)
	private String _BKNO = "";

	// 卡片帳號
	@Field(length = 32, optional = true)
	private String _CHACT = "";

	// 轉入銀行別
	@Field(length = 6, optional = true)
	private String _BKNO_D = "";

	// 轉入帳號
	@Field(length = 32, optional = true)
	private String _ACT_D = "";

	//
	@Field(length = 1124, optional = true)
	private String _FILLER2 = "";

	// 鈔箱5狀態
	@Field(length = 2, optional = true)
	private String _NOTES5 = "";

	// 鈔箱6狀態
	@Field(length = 2, optional = true)
	private String _NOTES6 = "";

	// 鈔箱7狀態
	@Field(length = 2, optional = true)
	private String _NOTES7 = "";

	// 鈔箱8狀態
	@Field(length = 2, optional = true)
	private String _NOTES8 = "";

	//
	@Field(length = 116, optional = true)
	private String _FILLER3 = "";

	private static final int _TotalLength = 720;

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
	 * <remark>放卡片銀行別</remark>
	 */
	public String getBKNO() {
		return _BKNO;
	}

	public void setBKNO(String value) {
		_BKNO = value;
	}

	/**
	 * 卡片帳號
	 * 
	 * <remark>放卡片主帳號</remark>
	 */
	public String getCHACT() {
		return _CHACT;
	}

	public void setCHACT(String value) {
		_CHACT = value;
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
	public String getFILLER3() {
		return _FILLER3;
	}

	public void setFILLER3(String value) {
		_FILLER3 = value;
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
	public String makeMessageFromGeneral(ATMGeneral general) {
		return null;
	}

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {
		/*
		general.getRequest().setTA(this.toAscii(this.getTA())); // 空白
		general.getRequest().setTXCD(this.toAscii(this.getTXCD())); // 交易別
		general.getRequest().setAtmverN(this.toAscii(this.getAtmverN())); // 新舊電文註記
		general.getRequest().setATMVER(this.toAscii(this.getATMVER())); // ATM版本日期
		general.getRequest().setBRNO(this.toAscii(this.getBRNO())); // ATM所屬分行
		general.getRequest().setWSNO(this.toAscii(this.getWSNO())); // ATM 代號
		general.getRequest().setNOTES1(this.toAscii(this.getNOTES1())); // 鈔箱1狀態
		general.getRequest().setNOTES2(this.toAscii(this.getNOTES2())); // 鈔箱2狀態
		general.getRequest().setNOTES3(this.toAscii(this.getNOTES3())); // 鈔箱3狀態
		general.getRequest().setNOTES4(this.toAscii(this.getNOTES4())); // 鈔箱4狀態
		general.getRequest().setJOUS(this.toAscii(this.getJOUS())); // 序時紙捲狀態
		general.getRequest().setADVS(this.toAscii(this.getADVS())); // 交易明細表狀態
		general.getRequest().setDEPS(this.toAscii(this.getDEPS())); // 存款模組狀態
		general.getRequest().setMCRWS(this.toAscii(this.getMCRWS())); // 磁條讀寫頭狀態
		general.getRequest().setENCRS(this.toAscii(this.getENCRS())); // ENCRYPTOR狀態
		general.getRequest().setSTAMS(this.toAscii(this.getSTAMS())); // 對帳單模組狀態
		general.getRequest().setDISPEN(this.toAscii(this.getDISPEN())); // 吐鈔模組狀態
		general.getRequest().setFILLER1(this.toAscii(this.getFILLER1())); //
		general.getRequest().setSERVICE(this.toAscii(this.getSERVICE())); // ATM 服務記號
		general.getRequest().setMODE(this.toAscii(this.getMODE())); // 營業日MODE
		general.getRequest().setDD(this.toAscii(this.getDD())); // 入帳日
		general.getRequest().setDEPMODE(this.toAscii(this.getDEPMODE())); // 信封存款MODE
		general.getRequest().setAtmseq_1(this.toAscii(this.getAtmseq_1())); // ATM 系統日
		general.getRequest().setAtmseq_2(this.toAscii(this.getAtmseq_2())); // ATM交易序號
		general.getRequest().setBKNO(this.toAscii(this.getBKNO())); // 銀行別
		general.getRequest().setCHACT(this.toAscii(this.getCHACT())); // 卡片帳號
		general.getRequest().setBknoD(this.toAscii(this.getBknoD())); // 轉入銀行別
		general.getRequest().setActD(this.toAscii(this.getActD())); // 轉入帳號
		general.getRequest().setFILLER2(this.toAscii(this.getFILLER2()));//
		general.getRequest().setNOTES5(this.toAscii(this.getNOTES5()));// 鈔箱5狀態
		general.getRequest().setNOTES6(this.toAscii(this.getNOTES6()));// 鈔箱6狀態
		general.getRequest().setNOTES7(this.toAscii(this.getNOTES7()));// 鈔箱7狀態
		general.getRequest().setNOTES8(this.toAscii(this.getNOTES8()));// 鈔箱8狀態
		general.getRequest().setFILLER3(this.toAscii(this.getFILLER3()));//
		*/
	}
}