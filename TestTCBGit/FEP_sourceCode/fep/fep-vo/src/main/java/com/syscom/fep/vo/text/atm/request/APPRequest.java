package com.syscom.fep.vo.text.atm.request;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.annotation.Record;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMTextBase;

@Record
public class APPRequest extends ATMTextBase {
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

	// 交易帳號
	@Field(length = 32, optional = true)
	private String _TXACT = "";

	// FOR MATED PIN BLOCK
	@Field(length = 32, optional = true)
	private String _FPB = "";

	// 第三軌資料
	@Field(length = 208, optional = true)
	private String _TRACK3 = "";

	// 押碼日期
	@Field(length = 12, optional = true)
	private String _YYMMDD = "";

	// 訊息押碼
	@Field(length = 16, optional = true)
	private String _MAC = "";

	//
	@Field(length = 862, optional = true)
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
	 * <remark></remark>
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
	 * <remark></remark>
	 */
	public String getCHACT() {
		return _CHACT;
	}

	public void setCHACT(String value) {
		_CHACT = value;
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
	 * FOR MATED PIN BLOCK
	 * 
	 * <remark></remark>
	 */
	public String getFPB() {
		return _FPB;
	}

	public void setFPB(String value) {
		_FPB = value;
	}

	/**
	 * 第三軌資料
	 * 
	 * <remark></remark>
	 */
	public String getTRACK3() {
		return _TRACK3;
	}

	public void setTRACK3(String value) {
		_TRACK3 = value;
	}

	/**
	 * 押碼日期
	 * 
	 * <remark></remark>
	 */
	public String getYYMMDD() {
		return _YYMMDD;
	}

	public void setYYMMDD(String value) {
		_YYMMDD = value;
	}

	/**
	 * 訊息押碼
	 * 
	 * <remark></remark>
	 */
	public String getMAC() {
		return _MAC;
	}

	public void setMAC(String value) {
		_MAC = value;
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
	public int getTotalLength() {
		return _TotalLength;
	}

	@Override
	public ATMGeneral parseFlatfile(String flatfile) {
		// TODO 自動生成的方法存根
		return null;
	}

	@Override
	public String makeMessageFromGeneral(ATMGeneral general) {
		// TODO 自動生成的方法存根
		return null;
	}

	@Override
	public void toGeneral(ATMGeneral general) {
		// TODO 自動生成的方法存根
	}
}
