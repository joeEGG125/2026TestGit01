package com.syscom.fep.vo.text.atm.request;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.ATMTextBase;

public class ODFRequest extends ATMTextBase {
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

	// 轉入帳號
	@Field(length = 32, optional = true)
	private String _ACT_D = "";

	// 交易金額
	@Field(length = 16, optional = true)
	private String _TXAMT = "";

	// 原REQ訊息中心交易序號
	@Field(length = 14, optional = true)
	private String _ORI_TXSEQ = "";

	// 原REQ訊息ATM交易序號YYYYMMDD
	@Field(length = 16, optional = true)
	private String _ORI_ATMSEQ_1 = "";

	// 原REQ訊息ATM交易序號
	@Field(length = 16, optional = true)
	private String _ORI_ATMSEQ_2 = "";

	// 拒絕理由
	@Field(length = 8, optional = true)
	private String _EXPCD = "";

	// 本營業日
	@Field(length = 4, optional = true)
	private String _ATMDD = "";

	// 壓碼日期
	@Field(length = 12, optional = true)
	private String _YYMMDD = "";

	// 訊息押碼
	@Field(length = 16, optional = true)
	private String _MAC = "";

	// 語音密碼
	@Field(length = 2, optional = true)
	private String _VPSWD = "";

	// 身分證號
	@Field(length = 20, optional = true)
	private String _PID = "";

	// R= RESEND
	@Field(length = 2, optional = true)
	private String _RESEND = "";

	// 錢箱資料
	@Field(length = 886, optional = true)
	private String _ADMBOX_AREA = "";

	// 轉入銀行別
	@Field(length = 6, optional = true)
	private String _BKNO_D = "";

	// 紙鈔存入金額
	@Field(length = 12, optional = true)
	private String _CASHAMT = "";

	// 硬幣存入金額
	@Field(length = 12, optional = true)
	private String _COINAMT = "";

	// 硬幣裝鈔序號
	@Field(length = 8, optional = true)
	private String _CRWTSEQ = "";

	// 硬幣面額
	@Field(length = 8, optional = true)
	private String _UNIT01 = "";

	// 硬幣存入個數
	@Field(length = 8, optional = true)
	private String _DEPOSIT01 = "";

	// 硬幣面額
	@Field(length = 8, optional = true)
	private String _UNIT02 = "";

	// 硬幣存入個數
	@Field(length = 8, optional = true)
	private String _DEPOSIT02 = "";

	// 硬幣面額
	@Field(length = 8, optional = true)
	private String _UNIT03 = "";

	// 硬幣存入個數
	@Field(length = 8, optional = true)
	private String _DEPOSIT03 = "";

	// 硬幣面額
	@Field(length = 8, optional = true)
	private String _UNIT04 = "";

	// 硬幣存入個數
	@Field(length = 8, optional = true)
	private String _DEPOSIT04 = "";

	// 硬幣面額
	@Field(length = 8, optional = true)
	private String _UNIT05 = "";

	// 硬幣存入個數
	@Field(length = 8, optional = true)
	private String _DEPOSIT05 = "";

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
	private String _FILLER2 = "";

	private static int _TotalLength = 720;

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
	 * 轉入帳號
	 * 
	 * <remark>放跨行存入帳號</remark>
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
	 * 原REQ訊息中心交易序號
	 * 
	 * <remark></remark>
	 */
	public String getOriTxseq() {
		return _ORI_TXSEQ;
	}

	public void setOriTxseq(String value) {
		_ORI_TXSEQ = value;
	}

	/**
	 * 原REQ訊息ATM交易序號YYYYMMDD
	 * 
	 * <remark></remark>
	 */
	public String getOriAtmseq_1() {
		return _ORI_ATMSEQ_1;
	}

	public void setOriAtmseq_1(String value) {
		_ORI_ATMSEQ_1 = value;
	}

	/**
	 * 原REQ訊息ATM交易序號
	 * 
	 * <remark></remark>
	 */
	public String getOriAtmseq_2() {
		return _ORI_ATMSEQ_2;
	}

	public void setOriAtmseq_2(String value) {
		_ORI_ATMSEQ_2 = value;
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
	 * 本營業日
	 * 
	 * <remark></remark>
	 */
	public String getATMDD() {
		return _ATMDD;
	}

	public void setATMDD(String value) {
		_ATMDD = value;
	}

	/**
	 * 壓碼日期
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
	 * <remark>公式參考附錄一-5</remark>
	 */
	public String getMAC() {
		return _MAC;
	}

	public void setMAC(String value) {
		_MAC = value;
	}

	/**
	 * 語音密碼
	 * 
	 * <remark>0:一般1:語音密碼2:無卡且不需輸入密碼</remark>
	 */
	public String getVPSWD() {
		return _VPSWD;
	}

	public void setVPSWD(String value) {
		_VPSWD = value;
	}

	/**
	 * 身分證號
	 * 
	 * <remark></remark>
	 */
	public String getPID() {
		return _PID;
	}

	public void setPID(String value) {
		_PID = value;
	}

	/**
	 * R= RESEND
	 * 
	 * <remark></remark>
	 */
	public String getRESEND() {
		return _RESEND;
	}

	public void setRESEND(String value) {
		_RESEND = value;
	}

	/**
	 * 錢箱資料
	 * 
	 * <remark></remark>
	 */
	public String getAdmboxArea() {
		return _ADMBOX_AREA;
	}

	public void setAdmboxArea(String value) {
		_ADMBOX_AREA = value;
	}

	/**
	 * 轉入銀行別
	 * 
	 * <remark>放跨行存入銀行別</remark>
	 */
	public String getBknoD() {
		return _BKNO_D;
	}

	public void setBknoD(String value) {
		_BKNO_D = value;
	}

	/**
	 * 紙鈔存入金額
	 * 
	 * <remark></remark>
	 */
	public String getCASHAMT() {
		return _CASHAMT;
	}

	public void setCASHAMT(String value) {
		_CASHAMT = value;
	}

	/**
	 * 硬幣存入金額
	 * 
	 * <remark></remark>
	 */
	public String getCOINAMT() {
		return _COINAMT;
	}

	public void setCOINAMT(String value) {
		_COINAMT = value;
	}

	/**
	 * 硬幣裝鈔序號
	 * 
	 * <remark>1~9999 循環使用 同一個裝幣區間使用同一個序號</remark>
	 */
	public String getCRWTSEQ() {
		return _CRWTSEQ;
	}

	public void setCRWTSEQ(String value) {
		_CRWTSEQ = value;
	}

	/**
	 * 硬幣面額
	 * 
	 * <remark></remark>
	 */
	public String getUNIT01() {
		return _UNIT01;
	}

	public void setUNIT01(String value) {
		_UNIT01 = value;
	}

	/**
	 * 硬幣存入個數
	 * 
	 * <remark></remark>
	 */
	public String getDEPOSIT01() {
		return _DEPOSIT01;
	}

	public void setDEPOSIT01(String value) {
		_DEPOSIT01 = value;
	}

	/**
	 * 硬幣面額
	 * 
	 * <remark></remark>
	 */
	public String getUNIT02() {
		return _UNIT02;
	}

	public void setUNIT02(String value) {
		_UNIT02 = value;
	}

	/**
	 * 硬幣存入個數
	 * 
	 * <remark></remark>
	 */
	public String getDEPOSIT02() {
		return _DEPOSIT02;
	}

	public void setDEPOSIT02(String value) {
		_DEPOSIT02 = value;
	}

	/**
	 * 硬幣面額
	 * 
	 * <remark></remark>
	 */
	public String getUNIT03() {
		return _UNIT03;
	}

	public void setUNIT03(String value) {
		_UNIT03 = value;
	}

	/**
	 * 硬幣存入個數
	 * 
	 * <remark></remark>
	 */
	public String getDEPOSIT03() {
		return _DEPOSIT03;
	}

	public void setDEPOSIT03(String value) {
		_DEPOSIT03 = value;
	}

	/**
	 * 硬幣面額
	 * 
	 * <remark></remark>
	 */
	public String getUNIT04() {
		return _UNIT04;
	}

	public void setUNIT04(String value) {
		_UNIT04 = value;
	}

	/**
	 * 硬幣存入個數
	 * 
	 * <remark></remark>
	 */
	public String getDEPOSIT04() {
		return _DEPOSIT04;
	}

	public void setDEPOSIT04(String value) {
		_DEPOSIT04 = value;
	}

	/**
	 * 硬幣面額
	 * 
	 * <remark></remark>
	 */
	public String getUNIT05() {
		return _UNIT05;
	}

	public void setUNIT05(String value) {
		_UNIT05 = value;
	}

	/**
	 * 硬幣存入個數
	 * 
	 * <remark></remark>
	 */
	public String getDEPOSIT05() {
		return _DEPOSIT05;
	}

	public void setDEPOSIT05(String value) {
		_DEPOSIT05 = value;
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

	@Override
	public ATMGeneral parseFlatfile(String flatfile) throws Exception {
		return this.parseFlatfile(this.getClass(), flatfile);
	}

	@Override
	public int getTotalLength() {
		return _TotalLength;
	}

	@Override
	public String makeMessageFromGeneral(ATMGeneral general) throws Exception {
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
		general.getRequest().setFILLER1(this.toAscii(this.getFILLER1()));
		general.getRequest().setSERVICE(this.toAscii(this.getSERVICE())); // ATM 服務記號
		general.getRequest().setMODE(this.toAscii(this.getMODE())); // 營業日MODE
		general.getRequest().setDD(this.toAscii(this.getDD())); // 入帳日
		general.getRequest().setDEPMODE(this.toAscii(this.getDEPMODE())); // 信封存款MODE
		general.getRequest().setAtmseq_1(this.toAscii(this.getAtmseq_1())); // ATM 系統日
		general.getRequest().setAtmseq_2(this.toAscii(this.getAtmseq_2())); // ATM交易序號
		general.getRequest().setBKNO(this.toAscii(this.getBKNO())); // 銀行別
		general.getRequest().setCHACT(this.toAscii(this.getCHACT())); // 卡片帳號
		general.getRequest().setActD(this.toAscii(this.getActD())); // 轉入帳號
		general.getRequest().setTXAMT(this.toBigDecimal(this.getTXAMT())); // 交易金額
		general.getRequest().setOriTxseq(this.toAscii(this.getOriTxseq())); // 原REQ訊息中心交易序號
		general.getRequest().setOriAtmseq_1(this.toAscii(this.getOriAtmseq_1())); // 原REQ訊息ATM交易序號YYYYMMDD
		general.getRequest().setOriAtmseq_2(this.toAscii(this.getOriAtmseq_2())); // 原REQ訊息ATM交易序號
		general.getRequest().setEXPCD(this.toAscii(this.getEXPCD())); // 拒絕理由
		general.getRequest().setATMDD(this.toAscii(this.getATMDD())); // 本營業日
		general.getRequest().setYYMMDD(this.toAscii(this.getYYMMDD())); // 壓碼日期
		general.getRequest().setMAC(this.toAscii(this.getMAC())); // 訊息押碼
		general.getRequest().setVPSWD(this.toAscii(this.getVPSWD())); // 語音密碼
		general.getRequest().setPID(this.toAscii(this.getPID())); // 身分證號
		general.getRequest().setRESEND(this.toAscii(this.getRESEND())); // R= RESEND
		general.getRequest().setAdmboxArea(this.toAscii(this.getAdmboxArea())); // 錢箱資料
		general.getRequest().setBknoD(this.toAscii(this.getBknoD())); // 轉入銀行別
		general.getRequest().setCASHAMT(this.toBigDecimal(this.getCASHAMT())); // 紙鈔存入金額
		general.getRequest().setCOINAMT(this.toBigDecimal(this.getCOINAMT())); // 硬幣存入金額
		general.getRequest().setCRWTSEQ(this.toAscii(this.getCRWTSEQ())); // 硬幣裝鈔序號
		general.getRequest().setUNIT01(this.toBigDecimal(this.getUNIT01())); // 硬幣面額
		general.getRequest().setDEPOSIT01(this.toBigDecimal(this.getDEPOSIT01())); // 硬幣存入個數
		general.getRequest().setUNIT01(this.toBigDecimal(this.getUNIT02())); // 硬幣面額
		general.getRequest().setDEPOSIT01(this.toBigDecimal(this.getDEPOSIT02())); // 硬幣存入個數
		general.getRequest().setUNIT01(this.toBigDecimal(this.getUNIT03())); // 硬幣面額
		general.getRequest().setDEPOSIT01(this.toBigDecimal(this.getDEPOSIT03())); // 硬幣存入個數
		general.getRequest().setUNIT01(this.toBigDecimal(this.getUNIT04())); // 硬幣面額
		general.getRequest().setDEPOSIT01(this.toBigDecimal(this.getDEPOSIT04())); // 硬幣存入個數
		general.getRequest().setUNIT01(this.toBigDecimal(this.getUNIT05())); // 硬幣面額
		general.getRequest().setDEPOSIT01(this.toBigDecimal(this.getDEPOSIT05())); // 硬幣存入個數
		general.getRequest().setNOTES5(this.toAscii(this.getNOTES5())); // 鈔箱5狀態
		general.getRequest().setNOTES6(this.toAscii(this.getNOTES6())); // 鈔箱6狀態
		general.getRequest().setNOTES7(this.toAscii(this.getNOTES7())); // 鈔箱7狀態
		general.getRequest().setNOTES8(this.toAscii(this.getNOTES8())); // 鈔箱8狀態
		general.getRequest().setFILLER2(this.toAscii(this.getFILLER2()));
		*/
	}
}
