package com.syscom.fep.vo.text.atm.request;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.ATMTextBase;

public class OFFRequest extends ATMTextBase {
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

	//銀行別
    @Field(length = 6, optional = true)
	private String _BKNO = "";

	//卡片帳號
    @Field(length = 32, optional = true)
	private String _CHACT = "";

	//交易帳號
    @Field(length = 32, optional = true)
	private String _TXACT = "";

	//轉入銀行別
    @Field(length = 6, optional = true)
	private String _BKNO_D = "";

	//轉入帳號
    @Field(length = 32, optional = true)
	private String _ACT_D = "";

	//FOR MATED PIN BLOCK
    @Field(length = 32, optional = true)
	private String _FPB = "";

	//第三軌資料
    @Field(length = 208, optional = true)
	private String _TRACK3 = "";

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

	//繳款種類
    @Field(length = 10, optional = true)
	private String _CLASS = "";

	//銷帳編號
    @Field(length = 32, optional = true)
	private String _PAYCNO = "";

	//繳款到期日
    @Field(length = 16, optional = true)
	private String _DUEDATE = "";

	//稽徵機關別
    @Field(length = 6, optional = true)
	private String _UNIT = "";

	//身分證字號
    @Field(length = 22, optional = true)
	private String _IDNO = "";

	//APPLY
    @Field(length = 2, optional = true)
	private String _APPLY = "";

	//預約轉帳入賬日期
    @Field(length = 16, optional = true)
	private String _DATE = "";

	//端末設備查核碼
    @Field(length = 16, optional = true)
	private String _ATMCHK = "";

	//YYYYMMDDHHMMSS
    @Field(length = 28, optional = true)
	private String _ICDTTM = "";

	//晶片主帳號
    @Field(length = 32, optional = true)
	private String _ICACT = "";

	//晶片卡REMARK欄位資料
    @Field(length = 60, optional = true)
	private String _ICMARK = "";

	//晶片卡交易序號
    @Field(length = 16, optional = true)
	private String _ICTXSEQ = "";

	//晶片卡交易驗證碼
    @Field(length = 32, optional = true)
	private String _ICTAC = "";

	//原REQ訊息中心交易序號
    @Field(length = 14, optional = true)
	private String _ORI_TXSEQ = "";

	//原REQ訊息ATM系統日
    @Field(length = 16, optional = true)
	private String _ORI_ATMSEQ_1 = "";

	//原REQ訊息ATM交易序號
    @Field(length = 16, optional = true)
	private String _ORI_ATMSEQ_2 = "";

	//拒絕理由
    @Field(length = 8, optional = true)
	private String _EXPCD = "";

	//本營業日 (DD)
    @Field(length = 4, optional = true)
	private String _ATMDD = "";

	//壓碼日期
    @Field(length = 12, optional = true)
	private String _YYMMDD = "";

	//訊息押碼
    @Field(length = 16, optional = true)
	private String _MAC = "";

	//提領幣別
    @Field(length = 4, optional = true)
	private String _CURCD = "";

	//調帳號記號
    @Field(length = 2, optional = true)
	private String _ACTCNT = "";

	//匯率
    @Field(length = 20, optional = true)
	private String _RATE = "";

	//折合台幣現幣
    @Field(length = 16, optional = true)
	private String _NTAMT = "";

	//台幣中價匯率
    @Field(length = 20, optional = true)
	private String _ACRAT = "";

	//匯差
    @Field(length = 20, optional = true)
	private String _DISRAT = "";

	//手續費 (台幣)
    @Field(length = 26, optional = true)
	private String _CHARGE = "";

	//手續費折原幣
    @Field(length = 26, optional = true)
	private String _EXP = "";

	//掛牌匯率
    @Field(length = 20, optional = true)
	private String _SCASH = "";

	//信封存款種類
    @Field(length = 2, optional = true)
	private String _DEP_TYPE = "";

	//存款信封序號
    @Field(length = 8, optional = true)
	private String _DEP_DEPNO = "";

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

	//委託單位代號
    @Field(length = 16, optional = true)
	private String _VPID = "";

	//費用代號
    @Field(length = 8, optional = true)
	private String _PAYID = "";

	//附言欄
    @Field(length = 80, optional = true)
	private String _MENO = "";

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

	//折合扣帳幣別金額
    @Field(length = 20, optional = true)
	private String _TXAMTDB = "";

	//扣帳幣別，海外卡專用
    @Field(length = 4, optional = true)
	private String _BAL_CUR = "";

	//手續費幣別，搭配EXP
    @Field(length = 4, optional = true)
	private String _HC_CUR = "";

	//FILLER2
    @Field(length = 102, optional = true)
	private String _FILLER2 = "";

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

	//FILLER3
    @Field(length = 116, optional = true)
	private String _FILLER3 = "";


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
	public String getBKNO() {
		return _BKNO;
	}
	public void setBKNO(String value) {
		_BKNO = value;
	}

	/**
	 卡片帳號

	 <remark></remark>
	 */
	public String getCHACT() {
		return _CHACT;
	}
	public void setCHACT(String value) {
		_CHACT = value;
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
	 轉入銀行別

	 <remark></remark>
	 */
	public String getBknoD() {
		return _BKNO_D;
	}
	public void setBknoD(String value) {
		_BKNO_D = value;
	}

	/**
	 轉入帳號

	 <remark></remark>
	 */
	public String getActD() {
		return _ACT_D;
	}
	public void setActD(String value) {
		_ACT_D = value;
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
	 第三軌資料

	 <remark></remark>
	 */
	public String getTRACK3() {
		return _TRACK3;
	}
	public void setTRACK3(String value) {
		_TRACK3 = value;
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
	 繳款種類

	 <remark>40005</remark>
	 */
	public String getCLASS() {
		return _CLASS;
	}
	public void setCLASS(String value) {
		_CLASS = value;
	}

	/**
	 銷帳編號

	 <remark>必填</remark>
	 */
	public String getPAYCNO() {
		return _PAYCNO;
	}
	public void setPAYCNO(String value) {
		_PAYCNO = value;
	}

	/**
	 繳款到期日

	 <remark>必填</remark>
	 */
	public String getDUEDATE() {
		return _DUEDATE;
	}
	public void setDUEDATE(String value) {
		_DUEDATE = value;
	}

	/**
	 稽徵機關別

	 <remark></remark>
	 */
	public String getUNIT() {
		return _UNIT;
	}
	public void setUNIT(String value) {
		_UNIT = value;
	}

	/**
	 身分證字號

	 <remark></remark>
	 */
	public String getIDNO() {
		return _IDNO;
	}
	public void setIDNO(String value) {
		_IDNO = value;
	}

	/**
	 APPLY

	 <remark>0</remark>
	 */
	public String getAPPLY() {
		return _APPLY;
	}
	public void setAPPLY(String value) {
		_APPLY = value;
	}

	/**
	 預約轉帳入賬日期

	 <remark>0</remark>
	 */
	public String getDATE() {
		return _DATE;
	}
	public void setDATE(String value) {
		_DATE = value;
	}

	/**
	 端末設備查核碼

	 <remark></remark>
	 */
	public String getATMCHK() {
		return _ATMCHK;
	}
	public void setATMCHK(String value) {
		_ATMCHK = value;
	}

	/**
	 YYYYMMDDHHMMSS

	 <remark></remark>
	 */
	public String getICDTTM() {
		return _ICDTTM;
	}
	public void setICDTTM(String value) {
		_ICDTTM = value;
	}

	/**
	 晶片主帳號

	 <remark></remark>
	 */
	public String getICACT() {
		return _ICACT;
	}
	public void setICACT(String value) {
		_ICACT = value;
	}

	/**
	 晶片卡REMARK欄位資料

	 <remark>若為卡片為807時,格式參考附錄一-2</remark>
	 */
	public String getICMARK() {
		return _ICMARK;
	}
	public void setICMARK(String value) {
		_ICMARK = value;
	}

	/**
	 晶片卡交易序號

	 <remark></remark>
	 */
	public String getICTXSEQ() {
		return _ICTXSEQ;
	}
	public void setICTXSEQ(String value) {
		_ICTXSEQ = value;
	}

	/**
	 晶片卡交易驗證碼

	 <remark></remark>
	 */
	public String getICTAC() {
		return _ICTAC;
	}
	public void setICTAC(String value) {
		_ICTAC = value;
	}

	/**
	 原REQ訊息中心交易序號

	 <remark></remark>
	 */
	public String getOriTxseq() {
		return _ORI_TXSEQ;
	}
	public void setOriTxseq(String value) {
		_ORI_TXSEQ = value;
	}

	/**
	 原REQ訊息ATM系統日

	 <remark></remark>
	 */
	public String getOriAtmseq_1() {
		return _ORI_ATMSEQ_1;
	}
	public void setOriAtmseq_1(String value) {
		_ORI_ATMSEQ_1 = value;
	}

	/**
	 原REQ訊息ATM交易序號

	 <remark></remark>
	 */
	public String getOriAtmseq_2() {
		return _ORI_ATMSEQ_2;
	}
	public void setOriAtmseq_2(String value) {
		_ORI_ATMSEQ_2 = value;
	}

	/**
	 拒絕理由

	 <remark></remark>
	 */
	public String getEXPCD() {
		return _EXPCD;
	}
	public void setEXPCD(String value) {
		_EXPCD = value;
	}

	/**
	 本營業日 (DD)

	 <remark></remark>
	 */
	public String getATMDD() {
		return _ATMDD;
	}
	public void setATMDD(String value) {
		_ATMDD = value;
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
	 提領幣別

	 <remark>00:台幣</remark>
	 */
	public String getCURCD() {
		return _CURCD;
	}
	public void setCURCD(String value) {
		_CURCD = value;
	}

	/**
	 調帳號記號

	 <remark>0</remark>
	 */
	public String getACTCNT() {
		return _ACTCNT;
	}
	public void setACTCNT(String value) {
		_ACTCNT = value;
	}

	/**
	 匯率

	 <remark>0</remark>
	 */
	public String getRATE() {
		return _RATE;
	}
	public void setRATE(String value) {
		_RATE = value;
	}

	/**
	 折合台幣現幣

	 <remark>0</remark>
	 */
	public String getNTAMT() {
		return _NTAMT;
	}
	public void setNTAMT(String value) {
		_NTAMT = value;
	}

	/**
	 台幣中價匯率

	 <remark>0</remark>
	 */
	public String getACRAT() {
		return _ACRAT;
	}
	public void setACRAT(String value) {
		_ACRAT = value;
	}

	/**
	 匯差

	 <remark>0</remark>
	 */
	public String getDISRAT() {
		return _DISRAT;
	}
	public void setDISRAT(String value) {
		_DISRAT = value;
	}

	/**
	 手續費 (台幣)

	 <remark>0</remark>
	 */
	public String getCHARGE() {
		return _CHARGE;
	}
	public void setCHARGE(String value) {
		_CHARGE = value;
	}

	/**
	 手續費折原幣

	 <remark>0</remark>
	 */
	public String getEXP() {
		return _EXP;
	}
	public void setEXP(String value) {
		_EXP = value;
	}

	/**
	 掛牌匯率

	 <remark>0</remark>
	 */
	public String getSCASH() {
		return _SCASH;
	}
	public void setSCASH(String value) {
		_SCASH = value;
	}

	/**
	 信封存款種類

	 <remark></remark>
	 */
	public String getDepType() {
		return _DEP_TYPE;
	}
	public void setDepType(String value) {
		_DEP_TYPE = value;
	}

	/**
	 存款信封序號

	 <remark>0</remark>
	 */
	public String getDepDepno() {
		return _DEP_DEPNO;
	}
	public void setDepDepno(String value) {
		_DEP_DEPNO = value;
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
	 委託單位代號

	 <remark>10000002</remark>
	 */
	public String getVPID() {
		return _VPID;
	}
	public void setVPID(String value) {
		_VPID = value;
	}

	/**
	 費用代號

	 <remark></remark>
	 */
	public String getPAYID() {
		return _PAYID;
	}
	public void setPAYID(String value) {
		_PAYID = value;
	}

	/**
	 附言欄

	 <remark></remark>
	 */
	public String getMENO() {
		return _MENO;
	}
	public void setMENO(String value) {
		_MENO = value;
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
	 折合扣帳幣別金額

	 <remark>0</remark>
	 */
	public String getTXAMTDB() {
		return _TXAMTDB;
	}
	public void setTXAMTDB(String value) {
		_TXAMTDB = value;
	}

	/**
	 扣帳幣別，海外卡專用

	 <remark>0</remark>
	 */
	public String getBalCur() {
		return _BAL_CUR;
	}
	public void setBalCur(String value) {
		_BAL_CUR = value;
	}

	/**
	 手續費幣別，搭配EXP

	 <remark>0</remark>
	 */
	public String getHcCur() {
		return _HC_CUR;
	}
	public void setHcCur(String value) {
		_HC_CUR = value;
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
		atmGeneral.getRequest().setBKNO(this.toAscii(_BKNO)); //銀行別
		atmGeneral.getRequest().setCHACT(this.toAscii(_CHACT)); //卡片帳號
		atmGeneral.getRequest().setTXACT(this.toAscii(_TXACT)); //交易帳號
		atmGeneral.getRequest().setBknoD(this.toAscii(_BKNO_D)); //轉入銀行別
		atmGeneral.getRequest().setActD(this.toAscii(_ACT_D)); //轉入帳號
		atmGeneral.getRequest().setFPB(this.toAscii(_FPB)); //FOR MATED PIN BLOCK
		atmGeneral.getRequest().setTRACK3(this.toAscii(_TRACK3)); //第三軌資料
		atmGeneral.getRequest().setTXAMT(this.toBigDecimal(_TXAMT));
		atmGeneral.getRequest().setDSPCNT1(this.toBigDecimal(_DSPCNT1));
		atmGeneral.getRequest().setDSPCNT2(this.toBigDecimal(_DSPCNT2));
		atmGeneral.getRequest().setDSPCNT3(this.toBigDecimal(_DSPCNT3));
		atmGeneral.getRequest().setDSPCNT4(this.toBigDecimal(_DSPCNT4));
		atmGeneral.getRequest().setCLASS(this.toAscii(_CLASS)); //繳款種類
		atmGeneral.getRequest().setPAYCNO(this.toAscii(_PAYCNO)); //銷帳編號
		atmGeneral.getRequest().setDUEDATE(this.toAscii(_DUEDATE)); //繳款到期日
		atmGeneral.getRequest().setUNIT(this.toAscii(_UNIT)); //稽徵機關別
		atmGeneral.getRequest().setIDNO(this.toAscii(_IDNO)); //身分證字號
		atmGeneral.getRequest().setAPPLY(this.toAscii(_APPLY)); //APPLY
		atmGeneral.getRequest().setDATE(this.toAscii(_DATE)); //預約轉帳入賬日期
		atmGeneral.getRequest().setATMCHK(this.toAscii(_ATMCHK)); //端末設備查核碼
		atmGeneral.getRequest().setICDTTM(this.toAscii(_ICDTTM)); //YYYYMMDDHHMMSS
		atmGeneral.getRequest().setICACT(this.toAscii(_ICACT)); //晶片主帳號
		atmGeneral.getRequest().setICMARK(_ICMARK); //晶片卡REMARK欄位資料
		atmGeneral.getRequest().setICTXSEQ(this.toAscii(_ICTXSEQ)); //晶片卡交易序號
		atmGeneral.getRequest().setICTAC(this.toAscii(_ICTAC)); //晶片卡交易驗證碼
		atmGeneral.getRequest().setOriTxseq(this.toAscii(_ORI_TXSEQ)); //原REQ訊息中心交易序號
		atmGeneral.getRequest().setOriAtmseq_1(this.toAscii(_ORI_ATMSEQ_1)); //原REQ訊息ATM系統日
		atmGeneral.getRequest().setOriAtmseq_2(this.toAscii(_ORI_ATMSEQ_2)); //原REQ訊息ATM交易序號
		atmGeneral.getRequest().setEXPCD(this.toAscii(_EXPCD)); //拒絕理由
		atmGeneral.getRequest().setATMDD(this.toAscii(_ATMDD)); //本營業日 (DD)
		atmGeneral.getRequest().setYYMMDD(this.toAscii(_YYMMDD)); //壓碼日期
		atmGeneral.getRequest().setMAC(this.toAscii(_MAC)); //訊息押碼
		atmGeneral.getRequest().setCURCD(this.toAscii(_CURCD)); //提領幣別
		atmGeneral.getRequest().setACTCNT(this.toAscii(_ACTCNT)); //調帳號記號
		atmGeneral.getRequest().setRATE(this.toBigDecimal(_RATE, 5));
		atmGeneral.getRequest().setNTAMT(this.toBigDecimal(_NTAMT));
		atmGeneral.getRequest().setACRAT(this.toBigDecimal(_ACRAT, 5));
		atmGeneral.getRequest().setDISRAT(this.toBigDecimal(_DISRAT, 5));
		atmGeneral.getRequest().setCHARGE(this.toBigDecimal(_CHARGE, 2));
		atmGeneral.getRequest().setEXP(this.toBigDecimal(_EXP, 2));
		atmGeneral.getRequest().setSCASH(this.toBigDecimal(_SCASH, 5));
		atmGeneral.getRequest().setDepType(this.toAscii(_DEP_TYPE)); //信封存款種類
		atmGeneral.getRequest().setDepDepno(this.toAscii(_DEP_DEPNO)); //存款信封序號
		atmGeneral.getRequest().setDSPCNT1T(this.toBigDecimal(_DSPCNT1T));
		atmGeneral.getRequest().setDSPCNT2T(this.toBigDecimal(_DSPCNT2T));
		atmGeneral.getRequest().setDSPCNT3T(this.toBigDecimal(_DSPCNT3T));
		atmGeneral.getRequest().setDSPCNT4T(this.toBigDecimal(_DSPCNT4T));
		atmGeneral.getRequest().setVPID(this.toAscii(_VPID)); //委託單位代號
		atmGeneral.getRequest().setPAYID(this.toAscii(_PAYID)); //費用代號
		atmGeneral.getRequest().setMENO(this.toAscii(_MENO)); //附言欄
		atmGeneral.getRequest().setDSPCNT5(this.toBigDecimal(_DSPCNT5));
		atmGeneral.getRequest().setDSPCNT6(this.toBigDecimal(_DSPCNT6));
		atmGeneral.getRequest().setDSPCNT7(this.toBigDecimal(_DSPCNT7));
		atmGeneral.getRequest().setDSPCNT8(this.toBigDecimal(_DSPCNT8));
		atmGeneral.getRequest().setTXAMTDB(this.toBigDecimal(_TXAMTDB, 2)); //折合扣帳幣別金額
		atmGeneral.getRequest().setBalCur(this.toAscii(_BAL_CUR)); //扣帳幣別，海外卡專用
		atmGeneral.getRequest().setHcCur(this.toAscii(_HC_CUR)); //手續費幣別，搭配EXP
		atmGeneral.getRequest().setFILLER2(this.toAscii(_FILLER2)); //FILLER2
		atmGeneral.getRequest().setNOTES5(this.toAscii(_NOTES5)); //鈔箱5狀態
		atmGeneral.getRequest().setNOTES6(this.toAscii(_NOTES6)); //鈔箱6狀態
		atmGeneral.getRequest().setNOTES7(this.toAscii(_NOTES7)); //鈔箱7狀態
		atmGeneral.getRequest().setNOTES8(this.toAscii(_NOTES8)); //鈔箱8狀態
		atmGeneral.getRequest().setFILLER3(this.toAscii(_FILLER3)); //FILLER3
		*/
	}
}
