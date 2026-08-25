package com.syscom.fep.vo.text.atm.request;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.ATMTextBase;

public class CAMRequest extends ATMTextBase {
	//空白
	@Field(length = 4)
	private String _TA = "";

	//交易別
	@Field(length = 6)
	private String _TXCD = "";

	//新舊電文註記
	@Field(length = 2)
	private String _ATMVER_N = "";

	//ATM版本日期
	@Field(length = 16)
	private String _ATMVER = "";

	//ATM所屬分行
	@Field(length = 6)
	private String _BRNO = "";

	//ATM 代號
	@Field(length = 4)
	private String _WSNO = "";

	//鈔箱1狀態
	@Field(length = 2)
	private String _NOTES1 = "";

	//鈔箱2狀態
	@Field(length = 2)
	private String _NOTES2 = "";

	//鈔箱3狀態
	@Field(length = 2)
	private String _NOTES3 = "";

	//鈔箱4狀態
	@Field(length = 2)
	private String _NOTES4 = "";

	//序時紙捲狀態
	@Field(length = 2)
	private String _JOUS = "";

	//交易明細表狀態
	@Field(length = 2)
	private String _ADVS = "";

	//存款模組狀態
	@Field(length = 2)
	private String _DEPS = "";

	//磁條讀寫頭狀態
	@Field(length = 2)
	private String _MCRWS = "";

	//ENCRYPTOR狀態
	@Field(length = 2)
	private String _ENCRS = "";

	//對帳單模組狀態
	@Field(length = 2)
	private String _STAMS = "";

	//吐鈔模組狀態
	@Field(length = 2)
	private String _DISPEN = "";

	//FILLER1
	@Field(length = 2)
	private String _FILLER1 = "";

	//ATM 服務記號
	@Field(length = 2)
	private String _SERVICE = "";

	//營業日MODE
	@Field(length = 2)
	private String _MODE = "";

	//入帳日
	@Field(length = 16)
	private String _DD = "";

	//信封存款MODE
	@Field(length = 2)
	private String _DEPMODE = "";

	//ATM 系統日
	@Field(length = 16)
	private String _ATMSEQ_1 = "";

	//ATM交易序號
    @Field(length = 16)
	private String _ATMSEQ_2 = "";

	//信用卡號
	@Field(length = 38, optional = true)
	private String _PAN = "";

	//交易帳號
    @Field(length = 32, optional = true)
	private String _TXACT = "";

	//FOR MATED PIN BLOCK
    @Field(length = 32, optional = true)
	private String _FPB = "";

	//第二軌資料
	@Field(length = 80, optional = true)
	private String _TRACK2 = "";

	//FILLER2
	@Field(length = 128, optional = true)
	private String _FILLER2 = "";

	//交易金額
    @Field(length = 16, optional = true)
	private String _TXAMT = "";

	//吐鈔張數 1 (個拾位)
    @Field(length = 4, optional = true)
	private String _DSPCNT1 = "";

	//吐鈔張數 2 (個拾位)
    @Field(length = 4, optional = true)
	private String _DSPCNT2 = "";

	//吐鈔張數 3 (個拾位)
    @Field(length = 4, optional = true)
	private String _DSPCNT3 = "";

	//吐鈔張數 4 (個拾位)
    @Field(length = 4, optional = true)
	private String _DSPCNT4 = "";

	//壓碼日期
    @Field(length = 12, optional = true)
	private String _YYMMDD = "";

	//訊息押碼
    @Field(length = 16, optional = true)
	private String _MAC = "";

	//吐鈔張數 1 (千百位)
    @Field(length = 4, optional = true)
	private String _DSPCNT1T = "";

	//吐鈔張數 2 (千百位)
    @Field(length = 4, optional = true)
	private String _DSPCNT2T = "";

	//吐鈔張數 3 (千百位)
    @Field(length = 4, optional = true)
	private String _DSPCNT3T = "";

	//吐鈔張數 4 (千百位)
    @Field(length = 4, optional = true)
	private String _DSPCNT4T = "";



	//吐鈔張數 5 (千百個拾)
    @Field(length = 8, optional = true)
	private String _DSPCNT5 = "";

	//吐鈔張數 6 (千百個拾)
    @Field(length = 8, optional = true)
	private String _DSPCNT6 = "";

	//吐鈔張數 7 (千百個拾)
    @Field(length = 8, optional = true)
	private String _DSPCNT7 = "";

	//吐鈔張數 8 (千百個拾)
    @Field(length = 8, optional = true)
	private String _DSPCNT8 = "";

	//該卡是否通過初步檢核
	@Field(length = 2, optional = true)
	private String _ICEMV = "";

	//客戶是否同意收Access Fee
	@Field(length = 2, optional = true)
	private String _ACFEE = "";

	//
	@Field(length = 778, optional = true)
	private String _FILLER3 = "";

	//鈔箱5狀態
    @Field(length = 2, optional = true)
	private String _NOTES5 = "";

	//鈔箱6狀態
    @Field(length = 2, optional = true)
	private String _NOTES6 = "";

	//鈔箱7狀態
    @Field(length = 2, optional = true)
	private String _NOTES7 = "";

	//鈔箱8狀態
    @Field(length = 2, optional = true)
	private String _NOTES8 = "";

	//
	@Field(length = 116, optional = true)
	private String _FILLER4 = "";


	private static final int _TotalLength = 720;

	/**
	 空白

	 <remark></remark>
	 */
	public String getTA() {
		return _TA;
	}
	public void setTA(String value) {
		_TA = value;
	}

	/**
	 交易別

	 <remark>IWD</remark>
	 */
	public String getTXCD() {
		return _TXCD;
	}
	public void setTXCD(String value) {
		_TXCD = value;
	}

	/**
	 新舊電文註記

	 <remark>A</remark>
	 */
	public String getAtmverN() {
		return _ATMVER_N;
	}
	public void setAtmverN(String value) {
		_ATMVER_N = value;
	}

	/**
	 ATM版本日期

	 <remark>YYYYMMDD</remark>
	 */
	public String getATMVER() {
		return _ATMVER;
	}
	public void setATMVER(String value) {
		_ATMVER = value;
	}

	/**
	 ATM所屬分行

	 <remark></remark>
	 */
	public String getBRNO() {
		return _BRNO;
	}
	public void setBRNO(String value) {
		_BRNO = value;
	}

	/**
	 ATM 代號

	 <remark></remark>
	 */
	public String getWSNO() {
		return _WSNO;
	}
	public void setWSNO(String value) {
		_WSNO = value;
	}

	/**
	 鈔箱1狀態

	 <remark></remark>
	 */
	public String getNOTES1() {
		return _NOTES1;
	}
	public void setNOTES1(String value) {
		_NOTES1 = value;
	}

	/**
	 鈔箱2狀態

	 <remark></remark>
	 */
	public String getNOTES2() {
		return _NOTES2;
	}
	public void setNOTES2(String value) {
		_NOTES2 = value;
	}

	/**
	 鈔箱3狀態

	 <remark></remark>
	 */
	public String getNOTES3() {
		return _NOTES3;
	}
	public void setNOTES3(String value) {
		_NOTES3 = value;
	}

	/**
	 鈔箱4狀態

	 <remark></remark>
	 */
	public String getNOTES4() {
		return _NOTES4;
	}
	public void setNOTES4(String value) {
		_NOTES4 = value;
	}

	/**
	 序時紙捲狀態

	 <remark></remark>
	 */
	public String getJOUS() {
		return _JOUS;
	}
	public void setJOUS(String value) {
		_JOUS = value;
	}

	/**
	 交易明細表狀態

	 <remark></remark>
	 */
	public String getADVS() {
		return _ADVS;
	}
	public void setADVS(String value) {
		_ADVS = value;
	}

	/**
	 存款模組狀態

	 <remark></remark>
	 */
	public String getDEPS() {
		return _DEPS;
	}
	public void setDEPS(String value) {
		_DEPS = value;
	}

	/**
	 磁條讀寫頭狀態

	 <remark></remark>
	 */
	public String getMCRWS() {
		return _MCRWS;
	}
	public void setMCRWS(String value) {
		_MCRWS = value;
	}

	/**
	 ENCRYPTOR狀態

	 <remark></remark>
	 */
	public String getENCRS() {
		return _ENCRS;
	}
	public void setENCRS(String value) {
		_ENCRS = value;
	}

	/**
	 對帳單模組狀態

	 <remark></remark>
	 */
	public String getSTAMS() {
		return _STAMS;
	}
	public void setSTAMS(String value) {
		_STAMS = value;
	}

	/**
	 吐鈔模組狀態

	 <remark></remark>
	 */
	public String getDISPEN() {
		return _DISPEN;
	}
	public void setDISPEN(String value) {
		_DISPEN = value;
	}

	/**
	 FILLER1

	 <remark>SPACE</remark>
	 */
	public String getFILLER1() {
		return _FILLER1;
	}
	public void setFILLER1(String value) {
		_FILLER1 = value;
	}

	/**
	 ATM 服務記號

	 <remark>0:ATM IN SERVICE1:ATM OFF</remark>
	 */
	public String getSERVICE() {
		return _SERVICE;
	}
	public void setSERVICE(String value) {
		_SERVICE = value;
	}

	/**
	 營業日MODE

	 <remark>根據上一筆下行電文MODE-O 而來</remark>
	 */
	public String getMODE() {
		return _MODE;
	}
	public void setMODE(String value) {
		_MODE = value;
	}

	/**
	 入帳日

	 <remark>YYYYMMDD</remark>
	 */
	public String getDD() {
		return _DD;
	}
	public void setDD(String value) {
		_DD = value;
	}

	/**
	 信封存款MODE

	 <remark>根據上一筆下行電文DEPMODE-O而來</remark>
	 */
	public String getDEPMODE() {
		return _DEPMODE;
	}
	public void setDEPMODE(String value) {
		_DEPMODE = value;
	}

	/**
	 ATM 系統日

	 <remark>YYYYMMDD</remark>
	 */
	public String getAtmseq_1() {
		return _ATMSEQ_1;
	}
	public void setAtmseq_1(String value) {
		_ATMSEQ_1 = value;
	}

	/**
	 ATM交易序號

	 <remark>ATM SEQ</remark>
	 */
	public String getAtmseq_2() {
		return _ATMSEQ_2;
	}
	public void setAtmseq_2(String value) {
		_ATMSEQ_2 = value;
	}

	/**
	 銀行別

	 <remark></remark>
	 */
	public String getPAN() {
		return _PAN;
	}
	public void setPAN(String value) {
		_PAN = value;
	}

	/**
	 第二軌資料

	 <remark></remark>
	 */
	public String getTRACK2() {
		return _TRACK2;
	}
	public void setTRACK2(String value) {
		_TRACK2 = value;
	}

	/**
	 交易帳號

	 <remark>若轉出為807時,帳號格式參考附錄一-1</remark>
	 */
	public String getTXACT() {
		return _TXACT;
	}
	public void setTXACT(String value) {
		_TXACT = value;
	}

	/**
	 FOR MATED PIN BLOCK

	 <remark></remark>
	 */
	public String getFPB() {
		return _FPB;
	}
	public void setFPB(String value) {
		_FPB = value;
	}

	/**
	 交易金額

	 <remark></remark>
	 */
	public String getTXAMT() {
		return _TXAMT;
	}
	public void setTXAMT(String value) {
		_TXAMT = value;
	}

	/**
	 吐鈔張數 1 (個拾位)

	 <remark>0</remark>
	 */
	public String getDSPCNT1() {
		return _DSPCNT1;
	}
	public void setDSPCNT1(String value) {
		_DSPCNT1 = value;
	}

	/**
	 吐鈔張數 2 (個拾位)

	 <remark>0</remark>
	 */
	public String getDSPCNT2() {
		return _DSPCNT2;
	}
	public void setDSPCNT2(String value) {
		_DSPCNT2 = value;
	}

	/**
	 吐鈔張數 3 (個拾位)

	 <remark>0</remark>
	 */
	public String getDSPCNT3() {
		return _DSPCNT3;
	}
	public void setDSPCNT3(String value) {
		_DSPCNT3 = value;
	}

	/**
	 吐鈔張數 4 (個拾位)

	 <remark>0</remark>
	 */
	public String getDSPCNT4() {
		return _DSPCNT4;
	}
	public void setDSPCNT4(String value) {
		_DSPCNT4 = value;
	}

	/**
	 該卡是否通過初步檢核

	 <remark>40005</remark>
	 */
	public String getICEMV() {
		return _ICEMV;
	}
	public void setICEMV(String value) {
		_ICEMV = value;
	}

	/**
	 客戶是否同意收Access Fee

	 <remark>必填</remark>
	 */
	public String getACFEE() {
		return _ACFEE;
	}
	public void setACFEE(String value) {
		_ACFEE = value;
	}

	/**
	 壓碼日期

	 <remark></remark>
	 */
	public String getYYMMDD() {
		return _YYMMDD;
	}
	public void setYYMMDD(String value) {
		_YYMMDD = value;
	}

	/**
	 訊息押碼

	 <remark>公式參考附錄一-5</remark>
	 */
	public String getMAC() {
		return _MAC;
	}
	public void setMAC(String value) {
		_MAC = value;
	}

	/**
	 吐鈔張數 1 (千百位)

	 <remark>0</remark>
	 */
	public String getDSPCNT1T() {
		return _DSPCNT1T;
	}
	public void setDSPCNT1T(String value) {
		_DSPCNT1T = value;
	}

	/**
	 吐鈔張數 2 (千百位)

	 <remark>0</remark>
	 */
	public String getDSPCNT2T() {
		return _DSPCNT2T;
	}
	public void setDSPCNT2T(String value) {
		_DSPCNT2T = value;
	}

	/**
	 吐鈔張數 3 (千百位)

	 <remark>0</remark>
	 */
	public String getDSPCNT3T() {
		return _DSPCNT3T;
	}
	public void setDSPCNT3T(String value) {
		_DSPCNT3T = value;
	}

	/**
	 吐鈔張數 4 (千百位)

	 <remark>0</remark>
	 */
	public String getDSPCNT4T() {
		return _DSPCNT4T;
	}
	public void setDSPCNT4T(String value) {
		_DSPCNT4T = value;
	}

	/**
	 吐鈔張數 5 (千百個拾)

	 <remark>0</remark>
	 */
	public String getDSPCNT5() {
		return _DSPCNT5;
	}
	public void setDSPCNT5(String value) {
		_DSPCNT5 = value;
	}

	/**
	 吐鈔張數 6 (千百個拾)

	 <remark>0</remark>
	 */
	public String getDSPCNT6() {
		return _DSPCNT6;
	}
	public void setDSPCNT6(String value) {
		_DSPCNT6 = value;
	}

	/**
	 吐鈔張數 7 (千百個拾)

	 <remark>0</remark>
	 */
	public String getDSPCNT7() {
		return _DSPCNT7;
	}
	public void setDSPCNT7(String value) {
		_DSPCNT7 = value;
	}

	/**
	 吐鈔張數 8 (千百個拾)

	 <remark>0</remark>
	 */
	public String getDSPCNT8() {
		return _DSPCNT8;
	}
	public void setDSPCNT8(String value) {
		_DSPCNT8 = value;
	}

	/**

	 <remark></remark>
	 */
	public String getFILLER4() {
		return _FILLER4;
	}
	public void setFILLER4(String value) {
		_FILLER4 = value;
	}

	/**
	 FILLER2

	 <remark></remark>
	 */
	public String getFILLER2() {
		return _FILLER2;
	}
	public void setFILLER2(String value) {
		_FILLER2 = value;
	}

	/**
	 鈔箱5狀態

	 <remark></remark>
	 */
	public String getNOTES5() {
		return _NOTES5;
	}
	public void setNOTES5(String value) {
		_NOTES5 = value;
	}

	/**
	 鈔箱6狀態

	 <remark></remark>
	 */
	public String getNOTES6() {
		return _NOTES6;
	}
	public void setNOTES6(String value) {
		_NOTES6 = value;
	}

	/**
	 鈔箱7狀態

	 <remark></remark>
	 */
	public String getNOTES7() {
		return _NOTES7;
	}
	public void setNOTES7(String value) {
		_NOTES7 = value;
	}

	/**
	 鈔箱8狀態

	 <remark></remark>
	 */
	public String getNOTES8() {
		return _NOTES8;
	}
	public void setNOTES8(String value) {
		_NOTES8 = value;
	}

	/**
	 FILLER3

	 <remark></remark>
	 */
	public String getFILLER3() {
		return _FILLER3;
	}
	public void setFILLER3(String value) {
		_FILLER3 = value;
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
	public String makeMessageFromGeneral(ATMGeneral atmGeneral) throws Exception {
		return null;
	}

	@Override
	public void toGeneral(ATMGeneral atmGeneral) throws Exception {
		/*
		atmGeneral.getRequest().setTA(this.toAscii(_TA)); //空白
		atmGeneral.getRequest().setTXCD(this.toAscii(_TXCD)); //交易別
		atmGeneral.getRequest().setAtmverN(this.toAscii(_ATMVER_N)); //新舊電文註記
		atmGeneral.getRequest().setATMVER(this.toAscii(_ATMVER)); //ATM版本日期
		atmGeneral.getRequest().setBRNO(this.toAscii(_BRNO)); //ATM所屬分行
		atmGeneral.getRequest().setWSNO(this.toAscii(_WSNO)); //ATM 代號
		atmGeneral.getRequest().setNOTES1(this.toAscii(_NOTES1)); //鈔箱1狀態
		atmGeneral.getRequest().setNOTES2(this.toAscii(_NOTES2)); //鈔箱2狀態
		atmGeneral.getRequest().setNOTES3(this.toAscii(_NOTES3)); //鈔箱3狀態
		atmGeneral.getRequest().setNOTES4(this.toAscii(_NOTES4)); //鈔箱4狀態
		atmGeneral.getRequest().setJOUS(this.toAscii(_JOUS)); //序時紙捲狀態
		atmGeneral.getRequest().setADVS(this.toAscii(_ADVS)); //交易明細表狀態
		atmGeneral.getRequest().setDEPS(this.toAscii(_DEPS)); //存款模組狀態
		atmGeneral.getRequest().setMCRWS(this.toAscii(_MCRWS)); //磁條讀寫頭狀態
		atmGeneral.getRequest().setENCRS(this.toAscii(_ENCRS)); //ENCRYPTOR狀態
		atmGeneral.getRequest().setSTAMS(this.toAscii(_STAMS)); //對帳單模組狀態
		atmGeneral.getRequest().setDISPEN(this.toAscii(_DISPEN)); //吐鈔模組狀態
		atmGeneral.getRequest().setFILLER1(this.toAscii(_FILLER1)); //FILLER1
		atmGeneral.getRequest().setSERVICE(this.toAscii(_SERVICE)); //ATM 服務記號
		atmGeneral.getRequest().setMODE(this.toAscii(_MODE)); //營業日MODE
		atmGeneral.getRequest().setDD(this.toAscii(_DD)); //入帳日
		atmGeneral.getRequest().setDEPMODE(this.toAscii(_DEPMODE)); //信封存款MODE
		atmGeneral.getRequest().setAtmseq_1(this.toAscii(_ATMSEQ_1)); //ATM 系統日
		atmGeneral.getRequest().setAtmseq_2(this.toAscii(_ATMSEQ_2)); //ATM交易序號
		atmGeneral.getRequest().setPAN(this.toAscii(_PAN)); //信用卡號
		atmGeneral.getRequest().setTXACT(this.toAscii(_TXACT)); //交易帳號
		atmGeneral.getRequest().setFPB(this.toAscii(_FPB)); //FOR MATED PIN BLOCK
		atmGeneral.getRequest().setTRACK2(this.toAscii(_TRACK2)); //第二軌資料
		atmGeneral.getRequest().setFILLER2(this.toAscii(_FILLER2)); //FILLER2
		atmGeneral.getRequest().setTXAMT(this.toBigDecimal(_TXAMT));
		atmGeneral.getRequest().setDSPCNT1(this.toBigDecimal(_DSPCNT1));
		atmGeneral.getRequest().setDSPCNT2(this.toBigDecimal(_DSPCNT2));
		atmGeneral.getRequest().setDSPCNT3(this.toBigDecimal(_DSPCNT3));
		atmGeneral.getRequest().setDSPCNT4(this.toBigDecimal(_DSPCNT4));
		atmGeneral.getRequest().setYYMMDD(this.toAscii(_YYMMDD)); //壓碼日期
		atmGeneral.getRequest().setMAC(this.toAscii(_MAC)); //訊息押碼
		atmGeneral.getRequest().setDSPCNT1T(this.toBigDecimal(_DSPCNT1T));
		atmGeneral.getRequest().setDSPCNT2T(this.toBigDecimal(_DSPCNT2T));
		atmGeneral.getRequest().setDSPCNT3T(this.toBigDecimal(_DSPCNT3T));
		atmGeneral.getRequest().setDSPCNT4T(this.toBigDecimal(_DSPCNT4T));
		atmGeneral.getRequest().setDSPCNT5(this.toBigDecimal(_DSPCNT5));
		atmGeneral.getRequest().setDSPCNT6(this.toBigDecimal(_DSPCNT6));
		atmGeneral.getRequest().setDSPCNT7(this.toBigDecimal(_DSPCNT7));
		atmGeneral.getRequest().setDSPCNT8(this.toBigDecimal(_DSPCNT8));
		atmGeneral.getRequest().setICEMV(this.toAscii(_ICEMV));
		atmGeneral.getRequest().setACFEE(this.toAscii(_ACFEE));
		atmGeneral.getRequest().setFILLER3(this.toAscii(_FILLER3)); //FILLER3
		atmGeneral.getRequest().setNOTES5(this.toAscii(_NOTES5)); //鈔箱5狀態
		atmGeneral.getRequest().setNOTES6(this.toAscii(_NOTES6)); //鈔箱6狀態
		atmGeneral.getRequest().setNOTES7(this.toAscii(_NOTES7)); //鈔箱7狀態
		atmGeneral.getRequest().setNOTES8(this.toAscii(_NOTES8)); //鈔箱8狀態
		atmGeneral.getRequest().setFILLER4(this.toAscii(_FILLER4));
		*/
	}
}
