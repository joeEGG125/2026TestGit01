package com.syscom.fep.vo.text.atm.response;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralResponse;
import com.syscom.fep.vo.text.atm.ATMTextBase;

public class IFTResponse extends ATMTextBase {
	// 交易類別
	@Field(length = 6)
	private String _TXCD_O = "";

	// 交易日期
	@Field(length = 16)
	private String _DATE = "";

	// 交易時間
	@Field(length = 12)
	private String _TIME = "";

	// ATM ID
	@Field(length = 10)
	private String _ATMNO_O = "";

	// MODE
	@Field(length = 2)
	private String _MODE_O = "";

	// 本營業日
	@Field(length = 16)
	private String _DD_O = "";

	// 信封存款MODE
	@Field(length = 2)
	private String _DEPMODE_O = "";

	// ATM序號1
	@Field(length = 16)
	private String _ATMSEQ_O1 = "";

	// ATM序號2
	@Field(length = 16)
	private String _ATMSEQ_O2 = "";

	// 拒絕理由
	@Field(length = 8, optional = true)
	private String _REJCD = "";

	// 中心交易序號
	@Field(length = 14, optional = true)
	private String _TXSEQ = "";

	// 手續費-客戶負擔
	@Field(length = 6, optional = true)
	private String _HC = "";

	// 未登摺累計次數
	@Field(length = 4, optional = true)
	private String _NBCNT = "";

	// CDWAMT
	@Field(length = 12, optional = true)
	private String _CDWAMT = "";

	// 銀行別
	@Field(length = 6, optional = true)
	private String _BKNO = "";

	// 卡片帳號
	@Field(length = 32, optional = true)
	private String _CHACT = "";

	// 交易帳號
	@Field(length = 32, optional = true)
	private String _TXACT = "";

	// 轉入行
	@Field(length = 6, optional = true)
	private String _DPBK = "";

	// 轉入帳號
	@Field(length = 32, optional = true)
	private String _DPACT = "";

	// 交易金額
	@Field(length = 16, optional = true)
	private String _TXAMT = "";

	// 吐鈔張數 1(個拾位)
	@Field(length = 4, optional = true)
	private String _DSPCNT1 = "";

	// 吐鈔張數 2(個拾位)
	@Field(length = 4, optional = true)
	private String _DSPCNT2 = "";

	// 吐鈔張數 3(個拾位)
	@Field(length = 4, optional = true)
	private String _DSPCNT3 = "";

	// 吐鈔張數 4(個拾位)
	@Field(length = 4, optional = true)
	private String _DSPCNT4 = "";

	// OFFSET INCREMENT
	@Field(length = 8, optional = true)
	private String _OFFINC = "";

	// 正負號
	@Field(length = 2, optional = true)
	private String _BAL11_S = "";

	// 可用餘額
	@Field(length = 26, optional = true)
	private String _BAL11 = "";

	// 正負號
	@Field(length = 2, optional = true)
	private String _BAL12_S = "";

	// 帳戶餘額
	@Field(length = 26, optional = true)
	private String _BAL12 = "";

	// 正負號
	@Field(length = 2, optional = true)
	private String _BAL13_S = "";

	// 累計提款額
	@Field(length = 26, optional = true)
	private String _BAL13 = "";

	// 正負號
	@Field(length = 2, optional = true)
	private String _BAL14_S = "";

	// BAL14
	@Field(length = 26, optional = true)
	private String _BAL14 = "";

	// 需寫第三軌記號
	@Field(length = 2, optional = true)
	private String _WTRK3 = "";

	// 第三軌資料
	@Field(length = 208, optional = true)
	private String _TRACK3 = "";

	// 吃卡記號
	@Field(length = 2, optional = true)
	private String _RSCARD = "";

	// 壓碼日期
	@Field(length = 12, optional = true)
	private String _YYMMDD = "";

	// 訊息押碼
	@Field(length = 16, optional = true)
	private String _MAC = "";

	// 金資序號
	@Field(length = 14, optional = true)
	private String _STAN = "";

	// 促銷代碼
	@Field(length = 32, optional = true)
	private String _AD = "";

	// 吐鈔張數 1(千百位)
	@Field(length = 4, optional = true)
	private String _DSPCNT1T = "";

	// 吐鈔張數 2(千百位)
	@Field(length = 4, optional = true)
	private String _DSPCNT2T = "";

	// 吐鈔張數 3(千百位)
	@Field(length = 4, optional = true)
	private String _DSPCNT3T = "";

	// 吐鈔張數 4(千百位)
	@Field(length = 4, optional = true)
	private String _DSPCNT4T = "";

	// 吐鈔張數 5
	@Field(length = 8, optional = true)
	private String _DSPCNT5 = "";

	// 吐鈔張數 6
	@Field(length = 8, optional = true)
	private String _DSPCNT6 = "";

	// 吐鈔張數 7
	@Field(length = 8, optional = true)
	private String _DSPCNT7 = "";

	// 吐鈔張數 8
	@Field(length = 8, optional = true)
	private String _DSPCNT8 = "";

	// 餘額顯示幣別
	@Field(length = 6, optional = true)
	private String _BAL_CUR = "";

	// 手續費顯示幣別
	@Field(length = 6, optional = true)
	private String _HC_CUR = "";

	//
	@Field(length = 750, optional = true)
	private String _FILLER1 = "";

	private static final int _TotalLength = 748;

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
	 * <remark>1: Online MODE2: Half Online MODE3: Night MODE4: Reentry MODE5:Go to
	 * Online MODE</remark>
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
	 * 拒絕理由
	 *
	 * <remark></remark>
	 */
	public String getREJCD() {
		return _REJCD;
	}

	public void setREJCD(String value) {
		_REJCD = value;
	}

	/**
	 * 中心交易序號
	 *
	 * <remark></remark>
	 */
	public String getTXSEQ() {
		return _TXSEQ;
	}

	public void setTXSEQ(String value) {
		_TXSEQ = value;
	}

	/**
	 * 手續費-客戶負擔
	 *
	 * <remark></remark>
	 */
	public String getHC() {
		return _HC;
	}

	public void setHC(String value) {
		_HC = value;
	}

	/**
	 * 未登摺累計次數
	 *
	 * <remark></remark>
	 */
	public String getNBCNT() {
		return _NBCNT;
	}

	public void setNBCNT(String value) {
		_NBCNT = value;
	}

	/**
	 * CDWAMT
	 *
	 * <remark>0</remark>
	 */
	public String getCDWAMT() {
		return _CDWAMT;
	}

	public void setCDWAMT(String value) {
		_CDWAMT = value;
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
	 * 轉入行
	 *
	 * <remark></remark>
	 */
	public String getDPBK() {
		return _DPBK;
	}

	public void setDPBK(String value) {
		_DPBK = value;
	}

	/**
	 * 轉入帳號
	 *
	 * <remark></remark>
	 */
	public String getDPACT() {
		return _DPACT;
	}

	public void setDPACT(String value) {
		_DPACT = value;
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
	 * 吐鈔張數 1 個拾位
	 *
	 * <remark></remark>
	 */
	public String getDSPCNT1() {
		return _DSPCNT1;
	}

	public void setDSPCNT1(String value) {
		_DSPCNT1 = value;
	}

	/**
	 * 吐鈔張數 2個拾位
	 *
	 * <remark></remark>
	 */
	public String getDSPCNT2() {
		return _DSPCNT2;
	}

	public void setDSPCNT2(String value) {
		_DSPCNT2 = value;
	}

	/**
	 * 吐鈔張數 3個拾位
	 *
	 * <remark></remark>
	 */
	public String getDSPCNT3() {
		return _DSPCNT3;
	}

	public void setDSPCNT3(String value) {
		_DSPCNT3 = value;
	}

	/**
	 * 吐鈔張數 4個拾位
	 *
	 * <remark></remark>
	 */
	public String getDSPCNT4() {
		return _DSPCNT4;
	}

	public void setDSPCNT4(String value) {
		_DSPCNT4 = value;
	}

	/**
	 * OFFSET INCREMENT
	 *
	 * <remark></remark>
	 */
	public String getOFFINC() {
		return _OFFINC;
	}

	public void setOFFINC(String value) {
		_OFFINC = value;
	}

	/**
	 * 正負號
	 *
	 * <remark></remark>
	 */
	public String getBal11S() {
		return _BAL11_S;
	}

	public void setBal11S(String value) {
		_BAL11_S = value;
	}

	/**
	 * 可用餘額
	 *
	 * <remark></remark>
	 */
	public String getBAL11() {
		return _BAL11;
	}

	public void setBAL11(String value) {
		_BAL11 = value;
	}

	/**
	 * 正負號
	 *
	 * <remark></remark>
	 */
	public String getBal12S() {
		return _BAL12_S;
	}

	public void setBal12S(String value) {
		_BAL12_S = value;
	}

	/**
	 * 帳戶餘額
	 *
	 * <remark></remark>
	 */
	public String getBAL12() {
		return _BAL12;
	}

	public void setBAL12(String value) {
		_BAL12 = value;
	}

	/**
	 * 正負號
	 *
	 * <remark></remark>
	 */
	public String getBal13S() {
		return _BAL13_S;
	}

	public void setBal13S(String value) {
		_BAL13_S = value;
	}

	/**
	 * 累計提款額
	 *
	 * <remark>0</remark>
	 */
	public String getBAL13() {
		return _BAL13;
	}

	public void setBAL13(String value) {
		_BAL13 = value;
	}

	/**
	 * 正負號
	 *
	 * <remark></remark>
	 */
	public String getBal14S() {
		return _BAL14_S;
	}

	public void setBal14S(String value) {
		_BAL14_S = value;
	}

	/**
	 * BAL14
	 *
	 * <remark>0</remark>
	 */
	public String getBAL14() {
		return _BAL14;
	}

	public void setBAL14(String value) {
		_BAL14 = value;
	}

	/**
	 * 需寫第三軌記號
	 *
	 * <remark></remark>
	 */
	public String getWTRK3() {
		return _WTRK3;
	}

	public void setWTRK3(String value) {
		_WTRK3 = value;
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
	 * 吃卡記號
	 *
	 * <remark></remark>
	 */
	public String getRSCARD() {
		return _RSCARD;
	}

	public void setRSCARD(String value) {
		_RSCARD = value;
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
	 * <remark></remark>
	 */
	public String getMAC() {
		return _MAC;
	}

	public void setMAC(String value) {
		_MAC = value;
	}

	/**
	 * 金資序號
	 *
	 * <remark></remark>
	 */
	public String getSTAN() {
		return _STAN;
	}

	public void setSTAN(String value) {
		_STAN = value;
	}

	/**
	 * 促銷代碼
	 *
	 * <remark></remark>
	 */
	public String getAD() {
		return _AD;
	}

	public void setAD(String value) {
		_AD = value;
	}

	/**
	 * 吐鈔張數 1千百位
	 *
	 * <remark></remark>
	 */
	public String getDSPCNT1T() {
		return _DSPCNT1T;
	}

	public void setDSPCNT1T(String value) {
		_DSPCNT1T = value;
	}

	/**
	 * 吐鈔張數 2千百位
	 *
	 * <remark></remark>
	 */
	public String getDSPCNT2T() {
		return _DSPCNT2T;
	}

	public void setDSPCNT2T(String value) {
		_DSPCNT2T = value;
	}

	/**
	 * 吐鈔張數 3千百位
	 *
	 * <remark></remark>
	 */
	public String getDSPCNT3T() {
		return _DSPCNT3T;
	}

	public void setDSPCNT3T(String value) {
		_DSPCNT3T = value;
	}

	/**
	 * 吐鈔張數 4千百位
	 *
	 * <remark></remark>
	 */
	public String getDSPCNT4T() {
		return _DSPCNT4T;
	}

	public void setDSPCNT4T(String value) {
		_DSPCNT4T = value;
	}

	/**
	 * 吐鈔張數 5
	 *
	 * <remark></remark>
	 */
	public String getDSPCNT5() {
		return _DSPCNT5;
	}

	public void setDSPCNT5(String value) {
		_DSPCNT5 = value;
	}

	/**
	 * 吐鈔張數 6
	 *
	 * <remark></remark>
	 */
	public String getDSPCNT6() {
		return _DSPCNT6;
	}

	public void setDSPCNT6(String value) {
		_DSPCNT6 = value;
	}

	/**
	 * 吐鈔張數 7
	 *
	 * <remark></remark>
	 */
	public String getDSPCNT7() {
		return _DSPCNT7;
	}

	public void setDSPCNT7(String value) {
		_DSPCNT7 = value;
	}

	/**
	 * 吐鈔張數 8
	 *
	 * <remark></remark>
	 */
	public String getDSPCNT8() {
		return _DSPCNT8;
	}

	public void setDSPCNT8(String value) {
		_DSPCNT8 = value;
	}

	/**
	 * 餘額顯示幣別
	 *
	 * <remark></remark>
	 */
	public String getBalCur() {
		return _BAL_CUR;
	}

	public void setBalCur(String value) {
		_BAL_CUR = value;
	}

	/**
	 * 手續費顯示幣別
	 *
	 * <remark></remark>
	 */
	public String getHcCur() {
		return _HC_CUR;
	}

	public void setHcCur(String value) {
		_HC_CUR = value;
	}

	/**
	 *
	 *
	 * <remark></remark>
	 */
	public String getFILLER1() {
		return _FILLER1;
	}

	public void setFILLER1(String value) {
		_FILLER1 = value;
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
		/*
		ATMGeneralResponse response = general.getResponse();
		this.setTxcdO(StringUtil.toHex(StringUtils.rightPad(response.getTxcdO(), 3, " ")));// 交易類別
		this.setDATE(StringUtil.toHex(StringUtils.leftPad(response.getDATE(), 8, "0")));// 交易日期
		this.setTIME(StringUtil.toHex(StringUtils.leftPad(response.getTIME(), 6, "0")));// 交易時間
		this.setAtmnoO(StringUtil.toHex(StringUtils.leftPad(response.getAtmnoO(), 5, "0")));// ATM ID
		this.setModeO(StringUtil.toHex(StringUtils.rightPad(response.getModeO(), 1, " ")));// MODE
		this.setDdO(StringUtil.toHex(StringUtils.leftPad(response.getDdO(), 8, "0")));// 本營業日
		this.setDepmodeO(StringUtil.toHex(StringUtils.rightPad(response.getDepmodeO(), 1, " ")));// 信封存款MODE
		this.setAtmseqO1(StringUtil.toHex(StringUtils.leftPad(response.getAtmseqO1(), 8, "0")));// ATM序號1
		this.setAtmseqO2(StringUtil.toHex(StringUtils.leftPad(response.getAtmseqO2(), 8, "0")));// ATM序號2
		this.setREJCD(StringUtil.toHex(StringUtils.rightPad(response.getREJCD(), 4, " ")));// 拒絕理由
		this.setTXSEQ(StringUtil.toHex(StringUtils.leftPad(response.getTXSEQ(), 7, "0")));// 中心交易序號
		this.setHC(this.toHex(response.getHC(), 3));// 手續費-客戶負擔
		this.setNBCNT(this.toHex(response.getNBCNT(), 2));// 未登摺累計次數
		this.setCDWAMT(this.toHex(response.getCDWAMT(), 6));// CDWAMT
		this.setBKNO(StringUtil.toHex(StringUtils.rightPad(response.getBKNO(), 3, " ")));// 銀行別
		this.setCHACT(StringUtil.toHex(StringUtils.rightPad(response.getCHACT(), 16, " ")));// 卡片帳號
		this.setTXACT(StringUtil.toHex(StringUtils.rightPad(response.getTXACT(), 16, " ")));// 交易帳號
		this.setDPBK(StringUtil.toHex(StringUtils.rightPad(response.getDPBK(), 3, " "))); // 轉入行
		this.setDPACT(StringUtil.toHex(StringUtils.rightPad(response.getDPACT(), 16, " "))); // 轉入帳號
		this.setTXAMT(this.toHex(response.getTXAMT(), 8));// 交易金額
		this.setDSPCNT1(this.toHex(response.getDSPCNT1(), 2));// 吐鈔張數 1(個拾位)
		this.setDSPCNT2(this.toHex(response.getDSPCNT2(), 2));// 吐鈔張數 2(個拾位)
		this.setDSPCNT3(this.toHex(response.getDSPCNT3(), 2));// 吐鈔張數 3(個拾位)
		this.setDSPCNT4(this.toHex(response.getDSPCNT4(), 2));// 吐鈔張數 4(個拾位)
		this.setOFFINC(this.toHex(response.getOFFINC(), 4));// OFFSET INCREMENT
		this.setBal11S(StringUtil.toHex(StringUtils.rightPad(response.getBal11S(), 1, " "))); // 正負號
		this.setBAL11(this.toHex(response.getBAL11(), 13, 2));// 可用餘額
		this.setBal12S(StringUtil.toHex(StringUtils.rightPad(response.getBal12S(), 1, " "))); // 正負號
		this.setBAL12(this.toHex(response.getBAL12(), 13, 2));// 帳戶餘額
		this.setBal13S(StringUtil.toHex(StringUtils.rightPad(response.getBal13S(), 1, " "))); // 正負號
		this.setBAL13(this.toHex(response.getBAL13(), 13, 2));// 累計提款額
		this.setBal14S(StringUtil.toHex(StringUtils.rightPad(response.getBal14S(), 1, " "))); // 正負號
		this.setBAL14(this.toHex(response.getBAL14(), 13, 2));// BAL14
		this.setWTRK3(StringUtil.toHex(StringUtils.rightPad(response.getWTRK3(), 1, " "))); // 需寫第三軌記號
		this.setTRACK3(StringUtil.toHex(StringUtils.rightPad(response.getTRACK3(), 104, " "))); // 第三軌資料
		this.setRSCARD(StringUtil.toHex(StringUtils.rightPad(response.getRSCARD(), 1, " "))); // 吃卡記號
		this.setYYMMDD(StringUtil.toHex(StringUtils.leftPad(response.getYYMMDD(), 6, "0")));// 壓碼日期
		this.setMAC(StringUtil.toHex(StringUtils.rightPad(response.getMAC(), 8, " ")));// 訊息押碼
		this.setSTAN(StringUtil.toHex(StringUtils.rightPad(response.getSTAN(), 7, "0")));// 金資序號
		this.setAD(StringUtil.toHex(StringUtils.rightPad(response.getAD(), 16, " ")));// 促銷代碼
		this.setDSPCNT1T(this.toHex(response.getDSPCNT1T(), 2));// 吐鈔張數 1(千百位)
		this.setDSPCNT2T(this.toHex(response.getDSPCNT2T(), 2));// 吐鈔張數 2(千百位)
		this.setDSPCNT3T(this.toHex(response.getDSPCNT3T(), 2));// 吐鈔張數 3(千百位)
		this.setDSPCNT4T(this.toHex(response.getDSPCNT4T(), 2));// 吐鈔張數 4(千百位)
		this.setDSPCNT5(this.toHex(response.getDSPCNT5(), 4));// 吐鈔張數 5
		this.setDSPCNT6(this.toHex(response.getDSPCNT6(), 4));// 吐鈔張數 6
		this.setDSPCNT7(this.toHex(response.getDSPCNT7(), 4));// 吐鈔張數 7
		this.setDSPCNT8(this.toHex(response.getDSPCNT8(), 4));// 吐鈔張數 8
		this.setBalCur(StringUtil.toHex(StringUtils.rightPad(response.getBalCur(), 3, " ")));// 餘額顯示幣別
		this.setHcCur(StringUtil.toHex(StringUtils.rightPad(response.getHcCur(), 3, " ")));// 手續費顯示幣別
		this.setFILLER1(StringUtil.toHex(StringUtils.leftPad("", 375, " ")));
		return _TXCD_O + _DATE + _TIME + _ATMNO_O + _MODE_O + _DD_O + _DEPMODE_O + _ATMSEQ_O1 + _ATMSEQ_O2 + _REJCD
				+ _TXSEQ + _HC + _NBCNT + _CDWAMT + _BKNO + _CHACT + _TXACT + _DPBK + _DPACT + _TXAMT + _DSPCNT1
				+ _DSPCNT2 + _DSPCNT3 + _DSPCNT4 + _OFFINC + _BAL11_S + _BAL11 + _BAL12_S + _BAL12 + _BAL13_S + _BAL13
				+ _BAL14_S + _BAL14 + _WTRK3 + _TRACK3 + _RSCARD + _YYMMDD + _MAC + _STAN + _AD + _DSPCNT1T + _DSPCNT2T
				+ _DSPCNT3T + _DSPCNT4T + _DSPCNT5 + _DSPCNT6 + _DSPCNT7 + _DSPCNT8 + _BAL_CUR + _HC_CUR + _FILLER1;
		*/return "";
    }

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {
		// TODO Auto-generated method stub

	}
}
