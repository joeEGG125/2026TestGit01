package com.syscom.fep.vo.text.atm.response;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralResponse;
import com.syscom.fep.vo.text.atm.ATMTextBase;

public class DDFResponse extends ATMTextBase {
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

	// 拒絕理由
	@Field(length = 8, optional = true)
	private String _REJCD = StringUtils.EMPTY;

	// 中心交易序號
	@Field(length = 14, optional = true)
	private String _TXSEQ = StringUtils.EMPTY;

	// 手續費-客戶負擔
	@Field(length = 6, optional = true)
	private String _HC = StringUtils.EMPTY;

	// 未登摺累計次數
	@Field(length = 4, optional = true)
	private String _NBCNT = StringUtils.EMPTY;

	// CDWAMT
	@Field(length = 12, optional = true)
	private String _CDWAMT = StringUtils.EMPTY;

	// 銀行別
	@Field(length = 6, optional = true)
	private String _BKNO = StringUtils.EMPTY;

	// 卡片帳號
	@Field(length = 32, optional = true)
	private String _CHACT = StringUtils.EMPTY;

	// 交易帳號
	@Field(length = 32, optional = true)
	private String _TXACT = StringUtils.EMPTY;

	// 交易金額
	@Field(length = 16, optional = true)
	private String _TXAMT = StringUtils.EMPTY;

	// 壓碼日期
	@Field(length = 12, optional = true)
	private String _YYMMDD = StringUtils.EMPTY;

	// 訊息押碼
	@Field(length = 16, optional = true)
	private String _MAC = StringUtils.EMPTY;

	// 金資序號
	@Field(length = 14, optional = true)
	private String _STAN = StringUtils.EMPTY;

	// 0:一般 1:語音密碼 2:無卡且不需輸入密碼
	@Field(length = 2, optional = true)
	private String _VPSWD = StringUtils.EMPTY;

	//
	@Field(length = 20, optional = true)
	private String _PID = StringUtils.EMPTY;

	// 幣別
	@Field(length = 4, optional = true)
	private String _CURCD = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT01 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT01 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT02 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT02 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT03 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT03 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT04 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT04 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT05 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT05 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT06 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT06 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT07 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT07 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT08 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT08 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT09 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT09 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT10 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT10 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT11 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT11 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT12 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT12 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT13 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT13 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT14 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT14 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT15 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT15 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT16 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT16 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT17 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT17 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT18 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT18 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT19 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT19 = StringUtils.EMPTY;

	//
	@Field(length = 10, optional = true)
	private String _UNIT20 = StringUtils.EMPTY;

	//
	@Field(length = 6, optional = true)
	private String _DSPCNT20 = StringUtils.EMPTY;

	//
	@Field(length = 896, optional = true)
	private String _FILLER1 = StringUtils.EMPTY;

	private static int _TotalLength = 755;

	/**
	 * 交易類別
	 * 
	 * <remark></remark>
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
	 * 0:一般 1:語音密碼 2:無卡且不需輸入密碼
	 * 
	 * <remark></remark>
	 */
	public String getVPSWD() {
		return _VPSWD;
	}

	public void setVPSWD(String value) {
		_VPSWD = value;
	}

	/**
	 * 
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
	 * 幣別
	 * 
	 * <remark></remark>
	 */
	public String getCURCD() {
		return _CURCD;
	}

	public void setCURCD(String value) {
		_CURCD = value;
	}

	/**
	 * 
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
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT01() {
		return _DSPCNT01;
	}

	public void setDSPCNT01(String value) {
		_DSPCNT01 = value;
	}

	/**
	 * 
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
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT02() {
		return _DSPCNT02;
	}

	public void setDSPCNT02(String value) {
		_DSPCNT02 = value;
	}

	/**
	 * 
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
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT03() {
		return _DSPCNT03;
	}

	public void setDSPCNT03(String value) {
		_DSPCNT03 = value;
	}

	/**
	 * 
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
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT04() {
		return _DSPCNT04;
	}

	public void setDSPCNT04(String value) {
		_DSPCNT04 = value;
	}

	/**
	 * 
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
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT05() {
		return _DSPCNT05;
	}

	public void setDSPCNT05(String value) {
		_DSPCNT05 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getUNIT06() {
		return _UNIT06;
	}

	public void setUNIT06(String value) {
		_UNIT06 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT06() {
		return _DSPCNT06;
	}

	public void setDSPCNT06(String value) {
		_DSPCNT06 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getUNIT07() {
		return _UNIT07;
	}

	public void setUNIT07(String value) {
		_UNIT07 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT07() {
		return _DSPCNT07;
	}

	public void setDSPCNT07(String value) {
		_DSPCNT07 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getUNIT08() {
		return _UNIT08;
	}

	public void setUNIT08(String value) {
		_UNIT08 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT08() {
		return _DSPCNT08;
	}

	public void setDSPCNT08(String value) {
		_DSPCNT08 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getUNIT09() {
		return _UNIT09;
	}

	public void setUNIT09(String value) {
		_UNIT09 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT09() {
		return _DSPCNT09;
	}

	public void setDSPCNT09(String value) {
		_DSPCNT09 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getUNIT10() {
		return _UNIT10;
	}

	public void setUNIT10(String value) {
		_UNIT10 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT10() {
		return _DSPCNT10;
	}

	public void setDSPCNT10(String value) {
		_DSPCNT10 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getUNIT11() {
		return _UNIT11;
	}

	public void setUNIT11(String value) {
		_UNIT11 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT11() {
		return _DSPCNT11;
	}

	public void setDSPCNT11(String value) {
		_DSPCNT11 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getUNIT12() {
		return _UNIT12;
	}

	public void setUNIT12(String value) {
		_UNIT12 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT12() {
		return _DSPCNT12;
	}

	public void setDSPCNT12(String value) {
		_DSPCNT12 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getUNIT13() {
		return _UNIT13;
	}

	public void setUNIT13(String value) {
		_UNIT13 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT13() {
		return _DSPCNT13;
	}

	public void setDSPCNT13(String value) {
		_DSPCNT13 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getUNIT14() {
		return _UNIT14;
	}

	public void setUNIT14(String value) {
		_UNIT14 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT14() {
		return _DSPCNT14;
	}

	public void setDSPCNT14(String value) {
		_DSPCNT14 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getUNIT15() {
		return _UNIT15;
	}

	public void setUNIT15(String value) {
		_UNIT15 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT15() {
		return _DSPCNT15;
	}

	public void setDSPCNT15(String value) {
		_DSPCNT15 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getUNIT16() {
		return _UNIT16;
	}

	public void setUNIT16(String value) {
		_UNIT16 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT16() {
		return _DSPCNT16;
	}

	public void setDSPCNT16(String value) {
		_DSPCNT16 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getUNIT17() {
		return _UNIT17;
	}

	public void setUNIT17(String value) {
		_UNIT17 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT17() {
		return _DSPCNT17;
	}

	public void setDSPCNT17(String value) {
		_DSPCNT17 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getUNIT18() {
		return _UNIT18;
	}

	public void setUNIT18(String value) {
		_UNIT18 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT18() {
		return _DSPCNT18;
	}

	public void setDSPCNT18(String value) {
		_DSPCNT18 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getUNIT19() {
		return _UNIT19;
	}

	public void setUNIT19(String value) {
		_UNIT19 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT19() {
		return _DSPCNT19;
	}

	public void setDSPCNT19(String value) {
		_DSPCNT19 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getUNIT20() {
		return _UNIT20;
	}

	public void setUNIT20(String value) {
		_UNIT20 = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getDSPCNT20() {
		return _DSPCNT20;
	}

	public void setDSPCNT20(String value) {
		_DSPCNT20 = value;
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
		/*
		ATMGeneralResponse response = general.getResponse();
		this._TXCD_O = StringUtil.toHex(StringUtils.rightPad(response.getTxcdO(), 3, StringUtils.SPACE)); // 交易類別
		this._DATE = StringUtil.toHex(StringUtils.leftPad(response.getDATE(), 8, '0')); // 交易日期
		this._TIME = StringUtil.toHex(StringUtils.leftPad(response.getTIME(), 6, '0')); // 交易時間
		this._ATMNO_O = StringUtil.toHex(StringUtils.leftPad(response.getAtmnoO(), 5, '0')); // ATM ID
		this._MODE_O = StringUtil.toHex(StringUtils.rightPad(response.getModeO(), 1, StringUtils.SPACE)); // MODE
		this._DD_O = StringUtil.toHex(StringUtils.leftPad(response.getDdO(), 8, '0')); // 本營業日
		this._DEPMODE_O = StringUtil.toHex(StringUtils.rightPad(response.getDepmodeO(), 1, StringUtils.SPACE)); // 信封存款MODE
		this._ATMSEQ_O1 = StringUtil.toHex(StringUtils.leftPad(response.getAtmseqO1(), 8, '0')); // ATM序號1
		this._ATMSEQ_O2 = StringUtil.toHex(StringUtils.leftPad(response.getAtmseqO2(), 8, '0')); // ATM序號2
		this._REJCD = StringUtil.toHex(StringUtils.rightPad(response.getREJCD(), 4, StringUtils.SPACE)); // 拒絕理由
		this._TXSEQ = StringUtil.toHex(StringUtils.leftPad(response.getTXSEQ(), 7, '0')); // 中心交易序號
		this._HC = this.toHex(response.getHC(), 3, 0); // 手續費-客戶負擔
		this._NBCNT = this.toHex(response.getNBCNT(), 2, 0); // 未登摺累計次數
		this._CDWAMT = this.toHex(response.getCDWAMT(), 6, 0); // CDWAMT
		this._BKNO = StringUtil.toHex(StringUtils.rightPad(response.getBKNO(), 3, StringUtils.SPACE)); // 銀行別
		this._CHACT = StringUtil.toHex(StringUtils.rightPad(response.getCHACT(), 16, StringUtils.SPACE)); // 卡片帳號
		this._TXACT = StringUtil.toHex(StringUtils.rightPad(response.getTXACT(), 16, StringUtils.SPACE)); // 交易帳號
		this._TXAMT = this.toHex(response.getTXAMT(), 8, 0); // 交易金額
		this._YYMMDD = StringUtil.toHex(StringUtils.leftPad(response.getYYMMDD(), 6, '0')); // 壓碼日期
		this._MAC = StringUtil.toHex(StringUtils.rightPad(response.getMAC(), 8, StringUtils.SPACE)); // 訊息押碼
		this._STAN = StringUtil.toHex(StringUtils.leftPad(response.getSTAN(), 7, '0')); // 金資序號
		this._VPSWD = StringUtil.toHex(StringUtils.rightPad(response.getVPSWD(), 1, StringUtils.SPACE)); // 0:一般 1:語音密碼 2:無卡且不需輸入密碼
		this._PID = StringUtil.toHex(StringUtils.rightPad(response.getPID(), 10, StringUtils.SPACE));
		this._CURCD = StringUtil.toHex(StringUtils.leftPad(response.getCURCD(), 2, '0')); // 幣別
		this._UNIT01 = this.toHex(response.getUNIT01(), 5, 0);
		this._DSPCNT01 = this.toHex(response.getDSPCNT01(), 3, 0);
		this._UNIT02 = this.toHex(response.getUNIT02(), 5, 0);
		this._DSPCNT02 = this.toHex(response.getDSPCNT02(), 3, 0);
		this._UNIT03 = this.toHex(response.getUNIT03(), 5, 0);
		this._DSPCNT03 = this.toHex(response.getDSPCNT03(), 3, 0);
		this._UNIT04 = this.toHex(response.getUNIT04(), 5, 0);
		this._DSPCNT04 = this.toHex(response.getDSPCNT04(), 3, 0);
		this._UNIT05 = this.toHex(response.getUNIT05(), 5, 0);
		this._DSPCNT05 = this.toHex(response.getDSPCNT05(), 3, 0);
		this._UNIT06 = this.toHex(response.getUNIT06(), 5, 0);
		this._DSPCNT06 = this.toHex(response.getDSPCNT06(), 3, 0);
		this._UNIT07 = this.toHex(response.getUNIT07(), 5, 0);
		this._DSPCNT07 = this.toHex(response.getDSPCNT07(), 3, 0);
		this._UNIT08 = this.toHex(response.getUNIT08(), 5, 0);
		this._DSPCNT08 = this.toHex(response.getDSPCNT08(), 3, 0);
		this._UNIT09 = this.toHex(response.getUNIT09(), 5, 0);
		this._DSPCNT09 = this.toHex(response.getDSPCNT09(), 3, 0);
		this._UNIT10 = this.toHex(response.getUNIT10(), 5, 0);
		this._DSPCNT10 = this.toHex(response.getDSPCNT10(), 3, 0);
		this._UNIT11 = this.toHex(response.getUNIT11(), 5, 0);
		this._DSPCNT11 = this.toHex(response.getDSPCNT11(), 3, 0);
		this._UNIT12 = this.toHex(response.getUNIT12(), 5, 0);
		this._DSPCNT12 = this.toHex(response.getDSPCNT12(), 3, 0);
		this._UNIT13 = this.toHex(response.getUNIT13(), 5, 0);
		this._DSPCNT13 = this.toHex(response.getDSPCNT13(), 3, 0);
		this._UNIT14 = this.toHex(response.getUNIT14(), 5, 0);
		this._DSPCNT14 = this.toHex(response.getDSPCNT14(), 3, 0);
		this._UNIT15 = this.toHex(response.getUNIT15(), 5, 0);
		this._DSPCNT15 = this.toHex(response.getDSPCNT15(), 3, 0);
		this._UNIT16 = this.toHex(response.getUNIT16(), 5, 0);
		this._DSPCNT16 = this.toHex(response.getDSPCNT16(), 3, 0);
		this._UNIT17 = this.toHex(response.getUNIT17(), 5, 0);
		this._DSPCNT17 = this.toHex(response.getDSPCNT17(), 3, 0);
		this._UNIT18 = this.toHex(response.getUNIT18(), 5, 0);
		this._DSPCNT18 = this.toHex(response.getDSPCNT18(), 3, 0);
		this._UNIT19 = this.toHex(response.getUNIT19(), 5, 0);
		this._DSPCNT19 = this.toHex(response.getDSPCNT19(), 3, 0);
		this._UNIT20 = this.toHex(response.getUNIT20(), 5, 0);
		this._DSPCNT20 = this.toHex(response.getDSPCNT20(), 3, 0);
		this._FILLER1 = StringUtil.toHex(StringUtils.leftPad(StringUtils.EMPTY, 448, StringUtils.SPACE));

		return StringUtils.join(this._TXCD_O, this._DATE, this._TIME, this._ATMNO_O, this._MODE_O, this._DD_O, this._DEPMODE_O, this._ATMSEQ_O1, this._ATMSEQ_O2, this._REJCD, this._TXSEQ, this._HC,
				this._NBCNT, this._CDWAMT, this._BKNO, this._CHACT, this._TXACT, this._TXAMT, this._YYMMDD, this._MAC, this._STAN, this._VPSWD, this._PID, this._CURCD, this._UNIT01, this._DSPCNT01,
				this._UNIT02, this._DSPCNT02, this._UNIT03, this._DSPCNT03, this._UNIT04, this._DSPCNT04, this._UNIT05, this._DSPCNT05, this._UNIT06, this._DSPCNT06, this._UNIT07, this._DSPCNT07,
				this._UNIT08, this._DSPCNT08, this._UNIT09, this._DSPCNT09, this._UNIT10, this._DSPCNT10, this._UNIT11, this._DSPCNT11, this._UNIT12, this._DSPCNT12, this._UNIT13, this._DSPCNT13,
				this._UNIT14, this._DSPCNT14, this._UNIT15, this._DSPCNT15, this._UNIT16, this._DSPCNT16, this._UNIT17, this._DSPCNT17, this._UNIT18, this._DSPCNT18, this._UNIT19, this._DSPCNT19,
				this._UNIT20, this._DSPCNT20, this._FILLER1);
		*/return "";
    }

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {}

}