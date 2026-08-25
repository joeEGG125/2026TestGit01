package com.syscom.fep.vo.text.atm.response;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralResponse;
import com.syscom.fep.vo.text.atm.ATMTextBase;

import org.apache.commons.lang3.StringUtils;

public class TTSResponse extends ATMTextBase {
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

	// 裝鈔幣別
	@Field(length = 6, optional = true)
	private String _CURRENCY01 = "";

	// 裝鈔面額
	@Field(length = 10, optional = true)
	private String _UNIT01 = "";

	// 裝鈔張數
	@Field(length = 10, optional = true)
	private String _REFILL01 = "";

	// 裝鈔後吐鈔張數
	@Field(length = 10, optional = true)
	private String _PRSNT01 = "";

	// 上營業日剩餘張數
	@Field(length = 10, optional = true)
	private String _LLEFT01 = "";

	// 裝鈔幣別
	@Field(length = 6, optional = true)
	private String _CURRENCY02 = "";

	// 裝鈔面額
	@Field(length = 10, optional = true)
	private String _UNIT02 = "";

	// 裝鈔張數
	@Field(length = 10, optional = true)
	private String _REFILL02 = "";

	// 裝鈔張數
	@Field(length = 10, optional = true)
	private String _PRSNT02 = "";

	// 上營業日剩餘張數
	@Field(length = 10, optional = true)
	private String _LLEFT02 = "";

	// 裝鈔幣別
	@Field(length = 6, optional = true)
	private String _CURRENCY03 = "";

	// 裝鈔面額
	@Field(length = 10, optional = true)
	private String _UNIT03 = "";

	// 裝鈔張數
	@Field(length = 10, optional = true)
	private String _REFILL03 = "";

	// 裝鈔後吐鈔張數
	@Field(length = 10, optional = true)
	private String _PRSNT03 = "";

	// 上營業日剩餘張數
	@Field(length = 10, optional = true)
	private String _LLEFT03 = "";

	// 裝鈔幣別
	@Field(length = 6, optional = true)
	private String _CURRENCY04 = "";

	// 裝鈔面額
	@Field(length = 10, optional = true)
	private String _UNIT04 = "";

	// 裝鈔張數
	@Field(length = 10, optional = true)
	private String _REFILL04 = "";

	// 裝鈔後吐鈔張數
	@Field(length = 10, optional = true)
	private String _PRSNT04 = "";

	// 上營業日剩餘張數
	@Field(length = 10, optional = true)
	private String _LLEFT04 = "";

	// 裝鈔幣別
	@Field(length = 6, optional = true)
	private String _CURRENCY05 = "";

	// 裝鈔面額
	@Field(length = 10, optional = true)
	private String _UNIT05 = "";

	// 裝鈔張數
	@Field(length = 10, optional = true)
	private String _REFILL05 = "";

	// 裝鈔後吐鈔張數
	@Field(length = 10, optional = true)
	private String _PRSNT05 = "";

	// 上營業日剩餘張數
	@Field(length = 10, optional = true)
	private String _LLEFT05 = "";

	// 裝鈔幣別
	@Field(length = 6, optional = true)
	private String _CURRENCY06 = "";

	// 裝鈔面額
	@Field(length = 10, optional = true)
	private String _UNIT06 = "";

	// 裝鈔張數
	@Field(length = 10, optional = true)
	private String _REFILL06 = "";

	// 裝鈔後吐鈔張數
	@Field(length = 10, optional = true)
	private String _PRSNT06 = "";

	// 上營業日剩餘張數
	@Field(length = 10, optional = true)
	private String _LLEFT06 = "";

	// 裝鈔幣別
	@Field(length = 6, optional = true)
	private String _CURRENCY07 = "";

	// 裝鈔面額
	@Field(length = 10, optional = true)
	private String _UNIT07 = "";

	// 裝鈔張數
	@Field(length = 10, optional = true)
	private String _REFILL07 = "";

	// 裝鈔後吐鈔張數
	@Field(length = 10, optional = true)
	private String _PRSNT07 = "";

	// 上營業日剩餘張數
	@Field(length = 10, optional = true)
	private String _LLEFT07 = "";

	// 裝鈔幣別
	@Field(length = 6, optional = true)
	private String _CURRENCY08 = "";

	// 裝鈔面額
	@Field(length = 10, optional = true)
	private String _UNIT08 = "";

	// 裝鈔張數
	@Field(length = 10, optional = true)
	private String _REFILL08 = "";

	// 裝鈔後吐鈔張數
	@Field(length = 10, optional = true)
	private String _PRSNT08 = "";

	// 上營業日剩餘張數
	@Field(length = 10, optional = true)
	private String _LLEFT08 = "";

	// 上營業日裝鈔幣別
	@Field(length = 6, optional = true)
	private String _LCURRENCY01 = "";

	// 上營業日該幣別提領次數
	@Field(length = 8, optional = true)
	private String _LCWDCT01 = "";

	// 上營業日該幣別提領金額
	@Field(length = 20, optional = true)
	private String _LCWDAT01 = "";

	// 上營業日裝鈔幣別
	@Field(length = 6, optional = true)
	private String _LCURRENCY02 = "";

	// 上營業日該幣別提領次數
	@Field(length = 8, optional = true)
	private String _LCWDCT02 = "";

	// 上營業日該幣別提領金額
	@Field(length = 20, optional = true)
	private String _LCWDAT02 = "";

	// 上營業日裝鈔幣別
	@Field(length = 6, optional = true)
	private String _LCURRENCY03 = "";

	// 上營業日該幣別提領次數
	@Field(length = 8, optional = true)
	private String _LCWDCT03 = "";

	// 上營業日該幣別提領金額
	@Field(length = 20, optional = true)
	private String _LCWDAT03 = "";

	// 上營業日裝鈔幣別
	@Field(length = 6, optional = true)
	private String _LCURRENCY04 = "";

	// 上營業日該幣別提領次數
	@Field(length = 8, optional = true)
	private String _LCWDCT04 = "";

	// 上營業日該幣別提領金額
	@Field(length = 20, optional = true)
	private String _LCWDAT04 = "";

	// 上營業日裝鈔幣別
	@Field(length = 6, optional = true)
	private String _LCURRENCY05 = "";

	// 上營業日該幣別提領次數
	@Field(length = 8, optional = true)
	private String _LCWDCT05 = "";

	// 上營業日該幣別提領金額
	@Field(length = 20, optional = true)
	private String _LCWDAT05 = "";

	// 上營業日裝鈔幣別
	@Field(length = 6, optional = true)
	private String _LCURRENCY06 = "";

	// 上營業日該幣別提領次數
	@Field(length = 8, optional = true)
	private String _LCWDCT06 = "";

	// 上營業日該幣別提領金額
	@Field(length = 20, optional = true)
	private String _LCWDAT06 = "";

	// 上營業日裝鈔幣別
	@Field(length = 6, optional = true)
	private String _LCURRENCY07 = "";

	// 上營業日該幣別提領次數
	@Field(length = 8, optional = true)
	private String _LCWDCT07 = "";

	// 上營業日該幣別提領金額
	@Field(length = 20, optional = true)
	private String _LCWDAT07 = "";

	// 上營業日裝鈔幣別
	@Field(length = 6, optional = true)
	private String _LCURRENCY08 = "";

	// 上營業日該幣別提領次數
	@Field(length = 8, optional = true)
	private String _LCWDCT08 = "";

	// 上營業日該幣別提領金額
	@Field(length = 20, optional = true)
	private String _LCWDAT08 = "";

	// 上營業日裝鈔幣別
	@Field(length = 6, optional = true)
	private String _TCURRENCY01 = "";

	// 本營業日該幣別提領次數
	@Field(length = 8, optional = true)
	private String _TCWDCT01 = "";

	// 本營業日該幣別提領金額
	@Field(length = 20, optional = true)
	private String _TCWDAT01 = "";

	// 本營業日裝鈔幣別
	@Field(length = 6, optional = true)
	private String _TCURRENCY02 = "";

	// 本營業日該幣別提領次數
	@Field(length = 8, optional = true)
	private String _TCWDCT02 = "";

	// 本營業日該幣別提領金額
	@Field(length = 20, optional = true)
	private String _TCWDAT02 = "";

	// 本營業日裝鈔幣別
	@Field(length = 6, optional = true)
	private String _TCURRENCY03 = "";

	// 本營業日該幣別提領次數
	@Field(length = 8, optional = true)
	private String _TCWDCT03 = "";

	// 本營業日該幣別提領金額
	@Field(length = 20, optional = true)
	private String _TCWDAT03 = "";

	// 本營業日裝鈔幣別
	@Field(length = 6, optional = true)
	private String _TCURRENCY04 = "";

	// 本營業日該幣別提領次數
	@Field(length = 8, optional = true)
	private String _TCWDCT04 = "";

	// 本營業日該幣別提領金額
	@Field(length = 20, optional = true)
	private String _TCWDAT04 = "";

	// 本營業日裝鈔幣別
	@Field(length = 6, optional = true)
	private String _TCURRENCY05 = "";

	// 本營業日該幣別提領次數
	@Field(length = 8, optional = true)
	private String _TCWDCT05 = "";

	// 本營業日該幣別提領金額
	@Field(length = 20, optional = true)
	private String _TCWDAT05 = "";

	// 本營業日裝鈔幣別
	@Field(length = 6, optional = true)
	private String _TCURRENCY06 = "";

	// 本營業日該幣別提領次數
	@Field(length = 8, optional = true)
	private String _TCWDCT06 = "";

	// 本營業日該幣別提領金額
	@Field(length = 20, optional = true)
	private String _TCWDAT06 = "";

	// 本營業日裝鈔幣別
	@Field(length = 6, optional = true)
	private String _TCURRENCY07 = "";

	// 本營業日該幣別提領次數
	@Field(length = 8, optional = true)
	private String _TCWDCT07 = "";

	// 本營業日該幣別提領金額
	@Field(length = 20, optional = true)
	private String _TCWDAT07 = "";

	// 本營業日裝鈔幣別
	@Field(length = 6, optional = true)
	private String _TCURRENCY08 = "";

	// 本營業日該幣別提領次數
	@Field(length = 8, optional = true)
	private String _TCWDCT08 = "";

	// 本營業日該幣別提領金額
	@Field(length = 20, optional = true)
	private String _TCWDAT08 = "";

	// 現金存款幣別
	@Field(length = 6, optional = true)
	private String _CSHCUR = "";

	// 裝鈔後現金存款次數
	@Field(length = 8, optional = true)
	private String _CASHCNT = "";

	// 裝鈔後現金存款金額
	@Field(length = 20, optional = true)
	private String _CASHAMT = "";

	// 上營業日現金存款幣別
	@Field(length = 6, optional = true)
	private String _LCSHCUR = "";

	// 上營業日現金存款次數
	@Field(length = 8, optional = true)
	private String _LCASHCNT = "";

	// 上營業日現金存款金額
	@Field(length = 20, optional = true)
	private String _LCASHAMT = "";

	// 本營業日現金存款幣別
	@Field(length = 6, optional = true)
	private String _TCSHCUR = "";

	// 本營業日現金存款次數
	@Field(length = 8, optional = true)
	private String _TCASHCNT = "";

	// 本營業日現金存款金額
	@Field(length = 20, optional = true)
	private String _TCASHAMT = "";

	// 留置卡張數
	@Field(length = 8, optional = true)
	private String _RETAIN = "";

	// 廠牌
	@Field(length = 2, optional = true)
	private String _VENDOR = "";

	// ATM種類
	@Field(length = 2, optional = true)
	private String _TYPE = "";

	// 此ATM所在區域國碼
	@Field(length = 6, optional = true)
	private String _COUNTRY = "";

	// 此區域ATM幣別組合序號
	@Field(length = 6, optional = true)
	private String _TABLE = "";

	// 裝鈔後轉帳筆數
	@Field(length = 8, optional = true)
	private String _TFRCNT = "";

	// 裝鈔後轉帳金額
	@Field(length = 20, optional = true)
	private String _TFRAMT = "";

	// 上營業日轉帳筆數
	@Field(length = 8, optional = true)
	private String _LTFRCNT = "";

	// 上營業日轉帳金額
	@Field(length = 20, optional = true)
	private String _LTFRAMT = "";

	// 本營業日轉帳筆數
	@Field(length = 8, optional = true)
	private String _TTFRCNT = "";

	// 本營業日轉帳金額
	@Field(length = 20, optional = true)
	private String _TTFRAMT = "";

	// 本營業日轉帳金額
	@Field(length = 270, optional = true)
	private String _FILLER1 = "";

	private static final int _TotalLength = 748;

	@Override
	public ATMGeneral parseFlatfile(String flatfile) throws Exception {
		return null;
	}

	/**
	 * ''' <summary> ''' 對應到General ''' </summary> ''' <remark></remark>
	 */
	@Override
	public String makeMessageFromGeneral(ATMGeneral general) throws Exception {
		/*
		ATMGeneralResponse tempVar = general.getResponse();
		_TXCD_O = StringUtil.toHex(StringUtils.rightPad(tempVar.getTxcdO(), 3, ' ')); // 交易類別
		_DATE = StringUtil.toHex(StringUtils.leftPad(tempVar.getDATE(), 8, '0')); // 交易日期
		_TIME = StringUtil.toHex(StringUtils.leftPad(tempVar.getTIME(), 6, '0')); // 交易時間
		_ATMNO_O = StringUtil.toHex(StringUtils.leftPad(tempVar.getAtmnoO(), 5, '0')); // ATM ID
		_MODE_O = StringUtil.toHex(StringUtils.rightPad(tempVar.getModeO(), 1, ' ')); // MODE
		_DD_O = StringUtil.toHex(StringUtils.leftPad(tempVar.getDdO(), 8, '0')); // 本營業日
		_DEPMODE_O = StringUtil.toHex(StringUtils.rightPad(tempVar.getDepmodeO(), 1, ' ')); // 信封存款MODE
		_ATMSEQ_O1 = StringUtil.toHex(StringUtils.leftPad(tempVar.getAtmseqO1(), 8, '0')); // ATM序號1
		_ATMSEQ_O2 = StringUtil.toHex(StringUtils.leftPad(tempVar.getAtmseqO2(), 8, '0')); // ATM序號2
		_REJCD = StringUtil.toHex(StringUtils.rightPad(tempVar.getREJCD(), 4, ' ')); // 拒絕理由
		_CURRENCY01 = StringUtil.toHex(StringUtils.rightPad(tempVar.getCURRENCY01(), 3, ' ')); // 裝鈔幣別
		_UNIT01 = this.toHex(tempVar.getUNIT01(), 5); // 裝鈔面額
		_REFILL01 = this.toHex(tempVar.getREFILL01(), 5); // 裝鈔張數額
		_PRSNT01 = this.toHex(tempVar.getPRSNT01(), 5); // 裝鈔後吐鈔張數
		_LLEFT01 = this.toHex(tempVar.getLLEFT01(), 5); // 上營業日剩餘張數
		_CURRENCY02 = StringUtil.toHex(StringUtils.rightPad(tempVar.getCURRENCY02(), 3, ' ')); // 裝鈔幣別
		_UNIT02 = this.toHex(tempVar.getUNIT02(), 5); // 裝鈔面額
		_REFILL02 = this.toHex(tempVar.getREFILL02(), 5); // 裝鈔張數額
		_PRSNT02 = this.toHex(tempVar.getPRSNT02(), 5); // 裝鈔後吐鈔張數
		_LLEFT02 = this.toHex(tempVar.getLLEFT02(), 5); // 上營業日剩餘張數
		_CURRENCY03 = StringUtil.toHex(StringUtils.rightPad(tempVar.getCURRENCY03(), 3, ' ')); // 裝鈔幣別
		_UNIT03 = this.toHex(tempVar.getUNIT03(), 5); // 裝鈔面額
		_REFILL03 = this.toHex(tempVar.getREFILL03(), 5); // 裝鈔張數額
		_PRSNT03 = this.toHex(tempVar.getPRSNT03(), 5); // 裝鈔後吐鈔張數
		_LLEFT03 = this.toHex(tempVar.getLLEFT03(), 5); // 上營業日剩餘張數
		_CURRENCY04 = StringUtil.toHex(StringUtils.rightPad(tempVar.getCURRENCY04(), 3, ' ')); // 裝鈔幣別
		_UNIT04 = this.toHex(tempVar.getUNIT04(), 5); // 裝鈔面額
		_REFILL04 = this.toHex(tempVar.getREFILL04(), 5); // 裝鈔張數額
		_PRSNT04 = this.toHex(tempVar.getPRSNT04(), 5); // 裝鈔後吐鈔張數
		_LLEFT04 = this.toHex(tempVar.getLLEFT04(), 5); // 上營業日剩餘張數
		_CURRENCY05 = StringUtil.toHex(StringUtils.rightPad(tempVar.getCURRENCY05(), 3, ' ')); // 裝鈔幣別
		_UNIT05 = this.toHex(tempVar.getUNIT05(), 5); // 裝鈔面額
		_REFILL05 = this.toHex(tempVar.getREFILL05(), 5); // 裝鈔張數額
		_PRSNT05 = this.toHex(tempVar.getPRSNT05(), 5); // 裝鈔後吐鈔張數
		_LLEFT05 = this.toHex(tempVar.getLLEFT05(), 5); // 上營業日剩餘張數
		_CURRENCY06 = StringUtil.toHex(StringUtils.rightPad(tempVar.getCURRENCY06(), 3, ' ')); // 裝鈔幣別
		_UNIT06 = this.toHex(tempVar.getUNIT06(), 5); // 裝鈔面額
		_REFILL06 = this.toHex(tempVar.getREFILL06(), 5); // 裝鈔張數額
		_PRSNT06 = this.toHex(tempVar.getPRSNT06(), 5); // 裝鈔後吐鈔張數
		_LLEFT06 = this.toHex(tempVar.getLLEFT06(), 5); // 上營業日剩餘張數
		_CURRENCY07 = StringUtil.toHex(StringUtils.rightPad(tempVar.getCURRENCY07(), 3, ' ')); // 裝鈔幣別
		_UNIT07 = this.toHex(tempVar.getUNIT07(), 5); // 裝鈔面額
		_REFILL07 = this.toHex(tempVar.getREFILL07(), 5); // 裝鈔張數額
		_PRSNT07 = this.toHex(tempVar.getPRSNT07(), 5); // 裝鈔後吐鈔張數
		_LLEFT07 = this.toHex(tempVar.getLLEFT07(), 5); // 上營業日剩餘張數
		_CURRENCY08 = StringUtil.toHex(StringUtils.rightPad(tempVar.getCURRENCY08(), 3, ' ')); // 裝鈔幣別
		_UNIT08 = this.toHex(tempVar.getUNIT08(), 5); // 裝鈔面額
		_REFILL08 = this.toHex(tempVar.getREFILL08(), 5); // 裝鈔張數額
		_PRSNT08 = this.toHex(tempVar.getPRSNT08(), 5); // 裝鈔後吐鈔張數
		_LLEFT08 = this.toHex(tempVar.getLLEFT08(), 5); // 上營業日剩餘張數
		_LCURRENCY01 = StringUtil.toHex(StringUtils.rightPad(tempVar.getLCURRENCY01(), 3, ' ')); // 上營業日裝鈔幣別
		_LCWDCT01 = this.toHex(tempVar.getLCWDCT01(), 4); // 裝鈔面額
		_LCWDAT01 = this.toHex(tempVar.getLCWDAT01(), 10); // 上營業日該幣別提領次數
		_LCURRENCY02 = StringUtil.toHex(StringUtils.rightPad(tempVar.getLCURRENCY02(), 3, ' ')); // 上營業日裝鈔幣別
		_LCWDCT02 = this.toHex(tempVar.getLCWDCT02(), 4); // 裝鈔面額
		_LCWDAT02 = this.toHex(tempVar.getLCWDAT02(), 10); // 上營業日該幣別提領次數
		_LCURRENCY03 = StringUtil.toHex(StringUtils.rightPad(tempVar.getLCURRENCY03(), 3, ' ')); // 上營業日裝鈔幣別
		_LCWDCT03 = this.toHex(tempVar.getLCWDCT03(), 4); // 裝鈔面額
		_LCWDAT03 = this.toHex(tempVar.getLCWDAT03(), 10); // 上營業日該幣別提領次數
		_LCURRENCY04 = StringUtil.toHex(StringUtils.rightPad(tempVar.getLCURRENCY04(), 3, ' ')); // 上營業日裝鈔幣別
		_LCWDCT04 = this.toHex(tempVar.getLCWDCT04(), 4); // 裝鈔面額
		_LCWDAT04 = this.toHex(tempVar.getLCWDAT04(), 10); // 上營業日該幣別提領次數
		_LCURRENCY05 = StringUtil.toHex(StringUtils.rightPad(tempVar.getLCURRENCY05(), 3, ' ')); // 上營業日裝鈔幣別
		_LCWDCT05 = this.toHex(tempVar.getLCWDCT05(), 4); // 裝鈔面額
		_LCWDAT05 = this.toHex(tempVar.getLCWDAT05(), 10); // 上營業日該幣別提領次數
		_LCURRENCY06 = StringUtil.toHex(StringUtils.rightPad(tempVar.getLCURRENCY06(), 3, ' ')); // 上營業日裝鈔幣別
		_LCWDCT06 = this.toHex(tempVar.getLCWDCT06(), 4); // 裝鈔面額
		_LCWDAT06 = this.toHex(tempVar.getLCWDAT06(), 10); // 上營業日該幣別提領次數
		_LCURRENCY07 = StringUtil.toHex(StringUtils.rightPad(tempVar.getLCURRENCY07(), 3, ' ')); // 上營業日裝鈔幣別
		_LCWDCT07 = this.toHex(tempVar.getLCWDCT07(), 4); // 裝鈔面額
		_LCWDAT07 = this.toHex(tempVar.getLCWDAT07(), 10); // 上營業日該幣別提領次數
		_LCURRENCY08 = StringUtil.toHex(StringUtils.rightPad(tempVar.getLCURRENCY08(), 3, ' ')); // 上營業日裝鈔幣別
		_LCWDCT08 = this.toHex(tempVar.getLCWDCT08(), 4); // 裝鈔面額
		_LCWDAT08 = this.toHex(tempVar.getLCWDAT08(), 10); // 上營業日該幣別提領次數
		_TCURRENCY01 = StringUtil.toHex(StringUtils.rightPad(tempVar.getTCURRENCY01(), 3, ' ')); // 本營業日裝鈔幣別
		_TCWDCT01 = this.toHex(tempVar.getTCWDCT01(), 4); // 本營業日該幣別提領次數
		_TCWDAT01 = this.toHex(tempVar.getTCWDAT01(), 10); // 本營業日該幣別提領金額
		_TCURRENCY02 = StringUtil.toHex(StringUtils.rightPad(tempVar.getTCURRENCY01(), 3, ' ')); // 本營業日裝鈔幣別
		_TCWDCT02 = this.toHex(tempVar.getTCWDCT02(), 4); // 本營業日該幣別提領次數
		_TCWDAT02 = this.toHex(tempVar.getTCWDAT02(), 10); // 本營業日該幣別提領金額
		_TCURRENCY03 = StringUtil.toHex(StringUtils.rightPad(tempVar.getTCURRENCY01(), 3, ' ')); // 本營業日裝鈔幣別
		_TCWDCT03 = this.toHex(tempVar.getTCWDCT03(), 4); // 本營業日該幣別提領次數
		_TCWDAT03 = this.toHex(tempVar.getTCWDAT03(), 10); // 本營業日該幣別提領金額
		_TCURRENCY04 = StringUtil.toHex(StringUtils.rightPad(tempVar.getTCURRENCY01(), 3, ' ')); // 本營業日裝鈔幣別
		_TCWDCT04 = this.toHex(tempVar.getTCWDCT04(), 4); // 本營業日該幣別提領次數
		_TCWDAT04 = this.toHex(tempVar.getTCWDAT04(), 10); // 本營業日該幣別提領金額
		_TCURRENCY05 = StringUtil.toHex(StringUtils.rightPad(tempVar.getTCURRENCY01(), 3, ' ')); // 本營業日裝鈔幣別
		_TCWDCT05 = this.toHex(tempVar.getTCWDCT05(), 4); // 本營業日該幣別提領次數
		_TCWDAT05 = this.toHex(tempVar.getTCWDAT05(), 10); // 本營業日該幣別提領金額
		_TCURRENCY06 = StringUtil.toHex(StringUtils.rightPad(tempVar.getTCURRENCY01(), 3, ' ')); // 本營業日裝鈔幣別
		_TCWDCT06 = this.toHex(tempVar.getTCWDCT06(), 4); // 本營業日該幣別提領次數
		_TCWDAT06 = this.toHex(tempVar.getTCWDAT06(), 10); // 本營業日該幣別提領金額
		_TCURRENCY07 = StringUtil.toHex(StringUtils.rightPad(tempVar.getTCURRENCY01(), 3, ' ')); // 本營業日裝鈔幣別
		_TCWDCT07 = this.toHex(tempVar.getTCWDCT07(), 4); // 本營業日該幣別提領次數
		_TCWDAT07 = this.toHex(tempVar.getTCWDAT07(), 10); // 本營業日該幣別提領金額
		_TCURRENCY08 = StringUtil.toHex(StringUtils.rightPad(tempVar.getTCURRENCY01(), 3, ' ')); // 本營業日裝鈔幣別
		_TCWDCT08 = this.toHex(tempVar.getTCWDCT08(), 4); // 本營業日該幣別提領次數
		_TCWDAT08 = this.toHex(tempVar.getTCWDAT08(), 10); // 本營業日該幣別提領金額
		_CSHCUR = StringUtil.toHex(StringUtils.rightPad(tempVar.getCSHCUR(), 3, ' ')); // 現金存款幣別
		_CASHCNT = this.toHex(tempVar.getCASHCNT(), 4); // 裝鈔後現金存款次數
		_CASHAMT = this.toHex(tempVar.getCASHAMT(), 10); // 裝鈔後現金存款金額
		_LCSHCUR = StringUtil.toHex(StringUtils.rightPad(tempVar.getLCSHCUR(), 3, ' ')); // 上營業日現金存款幣別
		_LCASHCNT = this.toHex(tempVar.getLCASHCNT(), 4); // 上營業日現金存款次數
		_LCASHAMT = this.toHex(tempVar.getLCASHAMT(), 10); // 上營業日現金存款金額
		_TCSHCUR = StringUtil.toHex(StringUtils.rightPad(tempVar.getTCSHCUR(), 3, ' ')); // 本營業日現金存款幣別
		_TCASHCNT = this.toHex(tempVar.getTCASHCNT(), 4); // 本營業日現金存款次數
		_TCASHAMT = this.toHex(tempVar.getTCASHAMT(), 10); // 本營業日現金存款金額
		_RETAIN = this.toHex(tempVar.getRETAIN(), 4); // 留置卡張數
		_VENDOR = StringUtil.toHex(StringUtils.rightPad(tempVar.getVENDOR(), 1, ' ')); // 廠牌
		_TYPE = StringUtil.toHex(StringUtils.rightPad(tempVar.getTYPE(), 1, ' ')); // ATM種類
		_COUNTRY = StringUtil.toHex(StringUtils.rightPad(tempVar.getCOUNTRY(), 3, ' ')); // 此ATM所在區域國碼
		_TABLE = StringUtil.toHex(StringUtils.leftPad(tempVar.getTABLE(), 3, '0')); // 此區域ATM幣別組合序號
		_TFRCNT = this.toHex(tempVar.getTFRCNT(), 4); // 裝鈔後轉帳筆數數
		_TFRAMT = this.toHex(tempVar.getTFRAMT(), 10); // 裝鈔後轉帳金額
		_LTFRCNT = this.toHex(tempVar.getLTFRCNT(), 4); // 上營業日轉帳筆數
		_LTFRAMT = this.toHex(tempVar.getLTFRAMT(), 10); // 上營業日轉帳金額
		_TTFRCNT = this.toHex(tempVar.getTTFRCNT(), 4); // 本營業日轉帳筆數
		_TTFRAMT = this.toHex(tempVar.getTTFRAMT(), 10); // 本營業日轉帳金額
		_FILLER1 = StringUtil.toHex(StringUtils.leftPad("", 135, ' '));

		return _TXCD_O + _DATE + _TIME + _ATMNO_O + _MODE_O + _DD_O + _DEPMODE_O + _ATMSEQ_O1 + _ATMSEQ_O2 + _REJCD
				+ _CURRENCY01 + _UNIT01 + _REFILL01 + _PRSNT01 + _LLEFT01 + _CURRENCY02 + _UNIT02 + _REFILL02 + _PRSNT02
				+ _LLEFT02 + _CURRENCY03 + _UNIT03 + _REFILL03 + _PRSNT03 + _LLEFT03 + _CURRENCY04 + _UNIT04 + _REFILL04
				+ _PRSNT04 + _LLEFT04 + _CURRENCY05 + _UNIT05 + _REFILL05 + _PRSNT05 + _LLEFT05 + _CURRENCY06 + _UNIT06
				+ _REFILL06 + _PRSNT06 + _LLEFT06 + _CURRENCY07 + _UNIT07 + _REFILL07 + _PRSNT07 + _LLEFT07
				+ _CURRENCY08 + _UNIT08 + _REFILL08 + _PRSNT08 + _LLEFT08 + _LCURRENCY01 + _LCWDCT01 + _LCWDAT01
				+ _LCURRENCY02 + _LCWDCT02 + _LCWDAT02 + _LCURRENCY03 + _LCWDCT03 + _LCWDAT03 + _LCURRENCY04 + _LCWDCT04
				+ _LCWDAT04 + _LCURRENCY05 + _LCWDCT05 + _LCWDAT05 + _LCURRENCY06 + _LCWDCT06 + _LCWDAT06 + _LCURRENCY07
				+ _LCWDCT07 + _LCWDAT07 + _LCURRENCY08 + _LCWDCT08 + _LCWDAT08 + _TCWDAT01 + _TCWDCT01 + _TCURRENCY01
				+ _TCWDAT02 + _TCWDCT02 + _TCURRENCY02 + _TCWDAT03 + _TCWDCT03 + _TCURRENCY03 + _TCWDAT04 + _TCWDCT04
				+ _TCURRENCY04 + _TCWDAT05 + _TCWDCT05 + _TCURRENCY05 + _TCWDAT06 + _TCWDCT06 + _TCURRENCY06 + _TCWDAT07
				+ _TCWDCT07 + _TCURRENCY07 + _TCWDAT08 + _TCWDCT08 + _TCURRENCY08 + _LCASHCNT + _LCSHCUR + _CASHAMT
				+ _CASHCNT + _CSHCUR + _LCASHAMT + _TCSHCUR + _TCASHCNT + _TCASHAMT + _RETAIN + _VENDOR + _TYPE
				+ _COUNTRY + _TABLE + _TFRCNT + _TFRAMT + _LTFRCNT + _LTFRAMT + _TTFRCNT + _TTFRAMT + _FILLER1;
			*/return "";
    }

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {

	}

	@Override
	public int getTotalLength() {
		return _TotalLength;
	}

	/**
	 * ''' <summary> ''' 交易時間 ''' </summary> ''' <remark></remark>
	 * 
	 * @return
	 */
	public String get_TXCD_O() {
		return _TXCD_O;
	}

	public String get_DATE() {
		return _DATE;
	}

	public String get_TIME() {
		return _TIME;
	}

	/**
	 * ''' <summary> ''' ATM ID ''' </summary> ''' <remark></remark>
	 * 
	 * @return
	 */
	public String get_ATMNO_O() {
		return _ATMNO_O;
	}

	/**
	 * ''' <summary> ''' MODE ''' </summary> ''' <remark>1: Online MODE2: Half
	 * Online MODE3: Night MODE4: Reentry MODE5:Go to Online MODE</remark>
	 * 
	 * @return
	 */
	public String get_MODE_O() {
		return _MODE_O;
	}

	/**
	 * ''' <summary> ''' 本營業日 ''' </summary> ''' <remark>YYYYMMDD</remark>
	 * 
	 * @return
	 */
	public String get_DD_O() {
		return _DD_O;
	}

	/**
	 * ''' <summary> ''' 信封存款MODE ''' </summary> '''
	 * <remark>2:仍在次交票/現金分割時間內6:已不在次交票/現金分割時間內1,5 for 星期六上班使用</remark>
	 * 
	 * @return
	 */
	public String get_DEPMODE_O() {
		return _DEPMODE_O;
	}

	/**
	 * ''' <summary> ''' ATM序號1 ''' </summary> ''' <remark>YYYYMMDD</remark>
	 * 
	 * @return
	 */
	public String get_ATMSEQ_O1() {
		return _ATMSEQ_O1;
	}

	/**
	 * ''' <summary> ''' ATM序號2 ''' </summary> ''' <remark>ATMSEQ</remark>
	 * 
	 * @return
	 */
	public String get_ATMSEQ_O2() {
		return _ATMSEQ_O2;
	}

	/**
	 * ''' <summary> ''' 拒絕理由 ''' </summary> ''' <remark>SPACES為正常</remark>
	 * 
	 * @return
	 */
	public String get_REJCD() {
		return _REJCD;
	}

	/**
	 * ''' <summary> ''' 電文總長度 ''' </summary> ''' <remark>該組電文總長度</remark>
	 * 
	 * @return
	 */
	public static int getTotallength() {
		return _TotalLength;
	}

	/**
	 * * <summary> 裝鈔幣別 </summary> <remark>BY幣別/面額加總</remark>
	 * 
	 * @return
	 */
	public String get_CURRENCY01() {
		return _CURRENCY01;
	}

	public void set_CURRENCY01(String _CURRENCY01) {
		this._CURRENCY01 = _CURRENCY01;
	}

	/**
	 * * <summary> 裝鈔面額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_UNIT01() {
		return _UNIT01;
	}

	public void set_UNIT01(String _UNIT01) {
		this._UNIT01 = _UNIT01;
	}

	/**
	 * * <summary> 裝鈔張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_REFILL01() {
		return _REFILL01;
	}

	public void set_REFILL01(String _REFILL01) {
		this._REFILL01 = _REFILL01;
	}

	/**
	 * * <summary> 裝鈔後吐鈔張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_PRSNT01() {
		return _PRSNT01;
	}

	public void set_PRSNT01(String _PRSNT01) {
		this._PRSNT01 = _PRSNT01;
	}

	/**
	 * * <summary> 上營業日剩餘張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LLEFT01() {
		return _LLEFT01;
	}

	public void set_LLEFT01(String _LLEFT01) {
		this._LLEFT01 = _LLEFT01;
	}

	/**
	 * 
	 * <summary> 裝鈔幣別 </summary> <remark>BY幣別/面額加總</remark>
	 * 
	 * @return
	 */
	public String get_CURRENCY02() {
		return _CURRENCY02;
	}

	public void set_CURRENCY02(String _CURRENCY02) {
		this._CURRENCY02 = _CURRENCY02;
	}

	/**
	 * * <summary> 裝鈔面額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_UNIT02() {
		return _UNIT02;
	}

	public void set_UNIT02(String _UNIT02) {
		this._UNIT02 = _UNIT02;
	}

	/**
	 * * <summary> 裝鈔張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_REFILL02() {
		return _REFILL02;
	}

	public void set_REFILL02(String _REFILL02) {
		this._REFILL02 = _REFILL02;
	}

	/**
	 * * <summary> 裝鈔後吐鈔張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_PRSNT02() {
		return _PRSNT02;
	}

	public void set_PRSNT02(String _PRSNT02) {
		this._PRSNT02 = _PRSNT02;
	}

	/**
	 * ''' <summary> ''' 上營業日剩餘張數 ''' </summary> ''' <remark></remark>
	 * 
	 * @return
	 */
	public String get_LLEFT02() {
		return _LLEFT02;
	}

	public void set_LLEFT02(String _LLEFT02) {
		this._LLEFT02 = _LLEFT02;
	}

	/**
	 * * <summary> 裝鈔幣別 </summary> <remark>BY幣別/面額加總</remark>
	 * 
	 * @return
	 */
	public String get_CURRENCY03() {
		return _CURRENCY03;
	}

	public void set_CURRENCY03(String _CURRENCY03) {
		this._CURRENCY03 = _CURRENCY03;
	}

	/**
	 * ''' <summary>裝鈔面額</summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_UNIT03() {
		return _UNIT03;
	}

	public void set_UNIT03(String _UNIT03) {
		this._UNIT03 = _UNIT03;
	}

	/**
	 * * <summary> 裝鈔張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_REFILL03() {
		return _REFILL03;
	}

	public void set_REFILL03(String _REFILL03) {
		this._REFILL03 = _REFILL03;
	}

	/**
	 * * <summary> 裝鈔後吐鈔張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_PRSNT03() {
		return _PRSNT03;
	}

	public void set_PRSNT03(String _PRSNT03) {
		this._PRSNT03 = _PRSNT03;
	}

	/**
	 * * <summary> 上營業日剩餘張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LLEFT03() {
		return _LLEFT03;
	}

	public void set_LLEFT03(String _LLEFT03) {
		this._LLEFT03 = _LLEFT03;
	}

	/**
	 * * <summary> 裝鈔幣別 </summary> <remark> BY幣別/面額加總 </remark>
	 * 
	 * @return
	 */
	public String get_CURRENCY04() {
		return _CURRENCY04;
	}

	public void set_CURRENCY04(String _CURRENCY04) {
		this._CURRENCY04 = _CURRENCY04;
	}

	/**
	 * * <summary> 裝鈔面額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_UNIT04() {
		return _UNIT04;
	}

	public void set_UNIT04(String _UNIT04) {
		this._UNIT04 = _UNIT04;
	}

	/**
	 * * <summary> 裝鈔張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_REFILL04() {
		return _REFILL04;
	}

	public void set_REFILL04(String _REFILL04) {
		this._REFILL04 = _REFILL04;
	}

	/**
	 * * <summary> 裝鈔後吐鈔張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_PRSNT04() {
		return _PRSNT04;
	}

	public void set_PRSNT04(String _PRSNT04) {
		this._PRSNT04 = _PRSNT04;
	}

	/**
	 * * <summary> 上營業日剩餘張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LLEFT04() {
		return _LLEFT04;
	}

	public void set_LLEFT04(String _LLEFT04) {
		this._LLEFT04 = _LLEFT04;
	}

	/**
	 * * <summary> 裝鈔幣別 </summary> <remark> BY幣別/面額加總 </remark>
	 * 
	 * @return
	 */
	public String get_CURRENCY05() {
		return _CURRENCY05;
	}

	public void set_CURRENCY05(String _CURRENCY05) {
		this._CURRENCY05 = _CURRENCY05;
	}

	/**
	 * * <summary> 裝鈔面額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_UNIT05() {
		return _UNIT05;
	}

	public void set_UNIT05(String _UNIT05) {
		this._UNIT05 = _UNIT05;
	}

	/**
	 * * <summary> 裝鈔張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_REFILL05() {
		return _REFILL05;
	}

	public void set_REFILL05(String _REFILL05) {
		this._REFILL05 = _REFILL05;
	}

	/**
	 * * <summary> 裝鈔後吐鈔張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_PRSNT05() {
		return _PRSNT05;
	}

	public void set_PRSNT05(String _PRSNT05) {
		this._PRSNT05 = _PRSNT05;
	}

	/**
	 * * <summary> 上營業日剩餘張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LLEFT05() {
		return _LLEFT05;
	}

	public void set_LLEFT05(String _LLEFT05) {
		this._LLEFT05 = _LLEFT05;
	}

	/**
	 * * <summary> 裝鈔幣別 </summary> <remark> BY幣別/面額加總 </remark>
	 * 
	 * @return
	 */
	public String get_CURRENCY06() {
		return _CURRENCY06;
	}

	public void set_CURRENCY06(String _CURRENCY06) {
		this._CURRENCY06 = _CURRENCY06;
	}

	/**
	 * * <summary> 裝鈔面額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_UNIT06() {
		return _UNIT06;
	}

	public void set_UNIT06(String _UNIT06) {
		this._UNIT06 = _UNIT06;
	}

	/**
	 * * <summary> 裝鈔張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_REFILL06() {
		return _REFILL06;
	}

	public void set_REFILL06(String _REFILL06) {
		this._REFILL06 = _REFILL06;
	}

	/**
	 * * <summary> 裝鈔後吐鈔張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_PRSNT06() {
		return _PRSNT06;
	}

	public void set_PRSNT06(String _PRSNT06) {
		this._PRSNT06 = _PRSNT06;
	}

	/**
	 * * <summary> 上營業日剩餘張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LLEFT06() {
		return _LLEFT06;
	}

	public void set_LLEFT06(String _LLEFT06) {
		this._LLEFT06 = _LLEFT06;
	}

	/**
	 * * <summary> 裝鈔幣別 </summary> <remark>BY幣別/面額加總</remark>
	 * 
	 * @return
	 */
	public String get_CURRENCY07() {
		return _CURRENCY07;
	}

	public void set_CURRENCY07(String _CURRENCY07) {
		this._CURRENCY07 = _CURRENCY07;
	}

	/**
	 * * <summary> 裝鈔面額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_UNIT07() {
		return _UNIT07;
	}

	public void set_UNIT07(String _UNIT07) {
		this._UNIT07 = _UNIT07;
	}

	/**
	 * * <summary> 裝鈔張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_REFILL07() {
		return _REFILL07;
	}

	public void set_REFILL07(String _REFILL07) {
		this._REFILL07 = _REFILL07;
	}

	/**
	 * * <summary> 裝鈔後吐鈔張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_PRSNT07() {
		return _PRSNT07;
	}

	public void set_PRSNT07(String _PRSNT07) {
		this._PRSNT07 = _PRSNT07;
	}

	/**
	 * * <summary> 上營業日剩餘張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LLEFT07() {
		return _LLEFT07;
	}

	public void set_LLEFT07(String _LLEFT07) {
		this._LLEFT07 = _LLEFT07;
	}

	/**
	 * * <summary> 裝鈔幣別 </summary> <remark>BY幣別/面額加總</remark>
	 * 
	 * @return
	 */
	public String get_CURRENCY08() {
		return _CURRENCY08;
	}

	public void set_CURRENCY08(String _CURRENCY08) {
		this._CURRENCY08 = _CURRENCY08;
	}

	/**
	 * * <summary> 裝鈔面額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_UNIT08() {
		return _UNIT08;
	}

	public void set_UNIT08(String _UNIT08) {
		this._UNIT08 = _UNIT08;
	}

	/**
	 * * <summary> 裝鈔張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_REFILL08() {
		return _REFILL08;
	}

	public void set_REFILL08(String _REFILL08) {
		this._REFILL08 = _REFILL08;
	}

	/**
	 * * <summary> 裝鈔後吐鈔張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_PRSNT08() {
		return _PRSNT08;
	}

	public void set_PRSNT08(String _PRSNT08) {
		this._PRSNT08 = _PRSNT08;
	}

	/**
	 * * <summary> 上營業日剩餘張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LLEFT08() {
		return _LLEFT08;
	}

	public void set_LLEFT08(String _LLEFT08) {
		this._LLEFT08 = _LLEFT08;
	}

	/**
	 * * <summary> 上營業日裝鈔幣別數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCURRENCY01() {
		return _LCURRENCY01;
	}

	public void set_LCURRENCY01(String _LCURRENCY01) {
		this._LCURRENCY01 = _LCURRENCY01;
	}

	/**
	 * * <summary> 上營業日裝鈔幣別數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCWDCT01() {
		return _LCWDCT01;
	}

	public void set_LCWDCT01(String _LCWDCT01) {
		this._LCWDCT01 = _LCWDCT01;
	}

	/**
	 * * <summary> 上營業日該幣別提領金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCWDAT01() {
		return _LCWDAT01;
	}

	public void set_LCWDAT01(String _LCWDAT01) {
		this._LCWDAT01 = _LCWDAT01;
	}

	/**
	 * * <summary> 上營業日裝鈔幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCURRENCY02() {
		return _LCURRENCY02;
	}

	public void set_LCURRENCY02(String _LCURRENCY02) {
		this._LCURRENCY02 = _LCURRENCY02;
	}

	/**
	 * * <summary> 上營業日該幣別提領次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCWDCT02() {
		return _LCWDCT02;
	}

	public void set_LCWDCT02(String _LCWDCT02) {
		this._LCWDCT02 = _LCWDCT02;
	}

	/**
	 * * <summary> 上營業日該幣別提領金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCWDAT02() {
		return _LCWDAT02;
	}

	public void set_LCWDAT02(String _LCWDAT02) {
		this._LCWDAT02 = _LCWDAT02;
	}

	/**
	 * * <summary> 上營業日裝鈔幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCURRENCY03() {
		return _LCURRENCY03;
	}

	public void set_LCURRENCY03(String _LCURRENCY03) {
		this._LCURRENCY03 = _LCURRENCY03;
	}

	/**
	 * * <summary> 上營業日該幣別提領次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCWDCT03() {
		return _LCWDCT03;
	}

	public void set_LCWDCT03(String _LCWDCT03) {
		this._LCWDCT03 = _LCWDCT03;
	}

	/**
	 * * <summary> 上營業日該幣別提領金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCWDAT03() {
		return _LCWDAT03;
	}

	public void set_LCWDAT03(String _LCWDAT03) {
		this._LCWDAT03 = _LCWDAT03;
	}

	/**
	 * * <summary> 上營業日裝鈔幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCURRENCY04() {
		return _LCURRENCY04;
	}

	public void set_LCURRENCY04(String _LCURRENCY04) {
		this._LCURRENCY04 = _LCURRENCY04;
	}

	/**
	 * * <summary> 上營業日該幣別提領次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCWDCT04() {
		return _LCWDCT04;
	}

	public void set_LCWDCT04(String _LCWDCT04) {
		this._LCWDCT04 = _LCWDCT04;
	}

	/**
	 * * <summary> 上營業日該幣別提領金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCWDAT04() {
		return _LCWDAT04;
	}

	public void set_LCWDAT04(String _LCWDAT04) {
		this._LCWDAT04 = _LCWDAT04;
	}

	/**
	 * * <summary> 上營業日裝鈔幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCURRENCY05() {
		return _LCURRENCY05;
	}

	public void set_LCURRENCY05(String _LCURRENCY05) {
		this._LCURRENCY05 = _LCURRENCY05;
	}

	/**
	 * * <summary> 上營業日該幣別提領次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCWDCT05() {
		return _LCWDCT05;
	}

	public void set_LCWDCT05(String _LCWDCT05) {
		this._LCWDCT05 = _LCWDCT05;
	}

	/**
	 * * <summary> 上營業日該幣別提領金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCWDAT05() {
		return _LCWDAT05;
	}

	public void set_LCWDAT05(String _LCWDAT05) {
		this._LCWDAT05 = _LCWDAT05;
	}

	/**
	 * * <summary> 上營業日裝鈔幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCURRENCY06() {
		return _LCURRENCY06;
	}

	public void set_LCURRENCY06(String _LCURRENCY06) {
		this._LCURRENCY06 = _LCURRENCY06;
	}

	/**
	 * * <summary> 上營業日該幣別提領次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCWDCT06() {
		return _LCWDCT06;
	}

	public void set_LCWDCT06(String _LCWDCT06) {
		this._LCWDCT06 = _LCWDCT06;
	}

	/**
	 * * <summary> 上營業日該幣別提領金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCWDAT06() {
		return _LCWDAT06;
	}

	public void set_LCWDAT06(String _LCWDAT06) {
		this._LCWDAT06 = _LCWDAT06;
	}

	/**
	 * * <summary> 上營業日裝鈔幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCURRENCY07() {
		return _LCURRENCY07;
	}

	public void set_LCURRENCY07(String _LCURRENCY07) {
		this._LCURRENCY07 = _LCURRENCY07;
	}

	/**
	 * * <summary> 上營業日該幣別提領次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCWDCT07() {
		return _LCWDCT07;
	}

	public void set_LCWDCT07(String _LCWDCT07) {
		this._LCWDCT07 = _LCWDCT07;
	}

	/**
	 * * <summary> 上營業日該幣別提領金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCWDAT07() {
		return _LCWDAT07;
	}

	public void set_LCWDAT07(String _LCWDAT07) {
		this._LCWDAT07 = _LCWDAT07;
	}

	/**
	 * * <summary> 上營業日裝鈔幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCURRENCY08() {
		return _LCURRENCY08;
	}

	public void set_LCURRENCY08(String _LCURRENCY08) {
		this._LCURRENCY08 = _LCURRENCY08;
	}

	/**
	 * * <summary> 上營業日該幣別提領次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCWDCT08() {
		return _LCWDCT08;
	}

	public void set_LCWDCT08(String _LCWDCT08) {
		this._LCWDCT08 = _LCWDCT08;
	}

	/**
	 * * <summary> 上營業日該幣別提領金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCWDAT08() {
		return _LCWDAT08;
	}

	public void set_LCWDAT08(String _LCWDAT08) {
		this._LCWDAT08 = _LCWDAT08;
	}

	/**
	 * * <summary> 本營業日裝鈔幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCURRENCY01() {
		return _TCURRENCY01;
	}

	public void set_TCURRENCY01(String _TCURRENCY01) {
		this._TCURRENCY01 = _TCURRENCY01;
	}

	/**
	 * * <summary> 本營業日該幣別提領次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCWDCT01() {
		return _TCWDCT01;
	}

	public void set_TCWDCT01(String _TCWDCT01) {
		this._TCWDCT01 = _TCWDCT01;
	}

	/**
	 * * <summary> 本營業日該幣別提領金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCWDAT01() {
		return _TCWDAT01;
	}

	public void set_TCWDAT01(String _TCWDAT01) {
		this._TCWDAT01 = _TCWDAT01;
	}

	/**
	 * * <summary> 本營業日裝鈔幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCURRENCY02() {
		return _TCURRENCY02;
	}

	public void set_TCURRENCY02(String _TCURRENCY02) {
		this._TCURRENCY02 = _TCURRENCY02;
	}

	/**
	 * * <summary> 本營業日該幣別提領次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCWDCT02() {
		return _TCWDCT02;
	}

	public void set_TCWDCT02(String _TCWDCT02) {
		this._TCWDCT02 = _TCWDCT02;
	}

	/**
	 * * <summary> 本營業日該幣別提領金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCWDAT02() {
		return _TCWDAT02;
	}

	public void set_TCWDAT02(String _TCWDAT02) {
		this._TCWDAT02 = _TCWDAT02;
	}

	/**
	 * * <summary> 本營業日裝鈔幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCURRENCY03() {
		return _TCURRENCY03;
	}

	public void set_TCURRENCY03(String _TCURRENCY03) {
		this._TCURRENCY03 = _TCURRENCY03;
	}

	/**
	 * * <summary> 本營業日該幣別提領次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCWDCT03() {
		return _TCWDCT03;
	}

	public void set_TCWDCT03(String _TCWDCT03) {
		this._TCWDCT03 = _TCWDCT03;
	}

	/**
	 * * <summary> 本營業日該幣別提領金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCWDAT03() {
		return _TCWDAT03;
	}

	public void set_TCWDAT03(String _TCWDAT03) {
		this._TCWDAT03 = _TCWDAT03;
	}

	/**
	 * * <summary> 本營業日裝鈔幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCURRENCY04() {
		return _TCURRENCY04;
	}

	public void set_TCURRENCY04(String _TCURRENCY04) {
		this._TCURRENCY04 = _TCURRENCY04;
	}

	/**
	 * * <summary> 本營業日該幣別提領次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCWDCT04() {
		return _TCWDCT04;
	}

	public void set_TCWDCT04(String _TCWDCT04) {
		this._TCWDCT04 = _TCWDCT04;
	}

	/**
	 * * <summary> 本營業日該幣別提領金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCWDAT04() {
		return _TCWDAT04;
	}

	public void set_TCWDAT04(String _TCWDAT04) {
		this._TCWDAT04 = _TCWDAT04;
	}

	/**
	 * * <summary> 本營業日裝鈔幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCURRENCY05() {
		return _TCURRENCY05;
	}

	public void set_TCURRENCY05(String _TCURRENCY05) {
		this._TCURRENCY05 = _TCURRENCY05;
	}

	/**
	 * * <summary> 本營業日該幣別提領次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCWDCT05() {
		return _TCWDCT05;
	}

	public void set_TCWDCT05(String _TCWDCT05) {
		this._TCWDCT05 = _TCWDCT05;
	}

	/**
	 * * <summary> 本營業日該幣別提領金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCWDAT05() {
		return _TCWDAT05;
	}

	public void set_TCWDAT05(String _TCWDAT05) {
		this._TCWDAT05 = _TCWDAT05;
	}

	/**
	 * * <summary> 本營業日裝鈔幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCURRENCY06() {
		return _TCURRENCY06;
	}

	public void set_TCURRENCY06(String _TCURRENCY06) {
		this._TCURRENCY06 = _TCURRENCY06;
	}

	/**
	 * * <summary> 本營業日該幣別提領次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCWDCT06() {
		return _TCWDCT06;
	}

	public void set_TCWDCT06(String _TCWDCT06) {
		this._TCWDCT06 = _TCWDCT06;
	}

	/**
	 * * <summary> 本營業日該幣別提領金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCWDAT06() {
		return _TCWDAT06;
	}

	public void set_TCWDAT06(String _TCWDAT06) {
		this._TCWDAT06 = _TCWDAT06;
	}

	/**
	 * * <summary> 本營業日裝鈔幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCURRENCY07() {
		return _TCURRENCY07;
	}

	public void set_TCURRENCY07(String _TCURRENCY07) {
		this._TCURRENCY07 = _TCURRENCY07;
	}

	/**
	 * * <summary> 本營業日該幣別提領次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCWDCT07() {
		return _TCWDCT07;
	}

	public void set_TCWDCT07(String _TCWDCT07) {
		this._TCWDCT07 = _TCWDCT07;
	}

	/**
	 * * <summary> 本營業日該幣別提領金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCWDAT07() {
		return _TCWDAT07;
	}

	public void set_TCWDAT07(String _TCWDAT07) {
		this._TCWDAT07 = _TCWDAT07;
	}

	/**
	 * * <summary> 本營業日裝鈔幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCURRENCY08() {
		return _TCURRENCY08;
	}

	public void set_TCURRENCY08(String _TCURRENCY08) {
		this._TCURRENCY08 = _TCURRENCY08;
	}

	/**
	 * * <summary> 本營業日該幣別提領次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCWDCT08() {
		return _TCWDCT08;
	}

	public void set_TCWDCT08(String _TCWDCT08) {
		this._TCWDCT08 = _TCWDCT08;
	}

	/**
	 * * <summary> 本營業日該幣別提領金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCWDAT08() {
		return _TCWDAT08;
	}

	public void set_TCWDAT08(String _TCWDAT08) {
		this._TCWDAT08 = _TCWDAT08;
	}

	/**
	 * * <summary> 現金存款幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_CSHCUR() {
		return _CSHCUR;
	}

	public void set_CSHCUR(String _CSHCUR) {
		this._CSHCUR = _CSHCUR;
	}

	/**
	 * * <summary> 裝鈔後現金存款次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_CASHCNT() {
		return _CASHCNT;
	}

	public void set_CASHCNT(String _CASHCNT) {
		this._CASHCNT = _CASHCNT;
	}

	/**
	 * * <summary> 裝鈔後現金存款金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_CASHAMT() {
		return _CASHAMT;
	}

	public void set_CASHAMT(String _CASHAMT) {
		this._CASHAMT = _CASHAMT;
	}

	/**
	 * * <summary> 上營業日現金存款幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCSHCUR() {
		return _LCSHCUR;
	}

	public void set_LCSHCUR(String _LCSHCUR) {
		this._LCSHCUR = _LCSHCUR;
	}

	/**
	 * * <summary> 上營業日現金存款次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCASHCNT() {
		return _LCASHCNT;
	}

	public void set_LCASHCNT(String _LCASHCNT) {
		this._LCASHCNT = _LCASHCNT;
	}

	/**
	 * * <summary> 上營業日現金存款金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_LCASHAMT() {
		return _LCASHAMT;
	}

	public void set_LCASHAMT(String _LCASHAMT) {
		this._LCASHAMT = _LCASHAMT;
	}

	/**
	 * * <summary> 本營業日現金存款幣別 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCSHCUR() {
		return _TCSHCUR;
	}

	public void set_TCSHCUR(String _TCSHCUR) {
		this._TCSHCUR = _TCSHCUR;
	}

	/**
	 * * <summary> 本營業日現金存款次數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCASHCNT() {
		return _TCASHCNT;
	}

	public void set_TCASHCNT(String _TCASHCNT) {
		this._TCASHCNT = _TCASHCNT;
	}

	/**
	 * * <summary> 本營業日現金存款金額 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TCASHAMT() {
		return _TCASHAMT;
	}

	public void set_TCASHAMT(String _TCASHAMT) {
		this._TCASHAMT = _TCASHAMT;
	}

	/**
	 * * <summary> 留置卡張數 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_RETAIN() {
		return _RETAIN;
	}

	public void set_RETAIN(String _RETAIN) {
		this._RETAIN = _RETAIN;
	}

	/**
	 * * <summary> 廠牌 </summary> <remark>0:NCR 1:Diebold 2:Hitachi 9:WEB</remark>
	 * 
	 * @return
	 */
	public String get_VENDOR() {
		return _VENDOR;
	}

	public void set_VENDOR(String _VENDOR) {
		this._VENDOR = _VENDOR;
	}

	/**
	 * * <summary> ATM種類 </summary> <remark>1:提款機,2:存款機,3:Recycle</remark>
	 * 
	 * @return
	 */
	public String get_TYPE() {
		return _TYPE;
	}

	public void set_TYPE(String _TYPE) {
		this._TYPE = _TYPE;
	}

	/**
	 * * <summary> 此ATM所在區域國碼 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_COUNTRY() {
		return _COUNTRY;
	}

	public void set_COUNTRY(String _COUNTRY) {
		this._COUNTRY = _COUNTRY;
	}

	/**
	 * * <summary> 此區域ATM幣別組合序號 </summary> <remark></remark>
	 * 
	 * @return
	 */
	public String get_TABLE() {
		return _TABLE;
	}

	public void set_TABLE(String _TABLE) {
		this._TABLE = _TABLE;
	}

	/**
	 * * <summary> 裝鈔後轉帳筆數 </summary> <remark>(含繳稅及繳費)</remark>
	 * 
	 * @return
	 */
	public String get_TFRCNT() {
		return _TFRCNT;
	}

	public void set_TFRCNT(String _TFRCNT) {
		this._TFRCNT = _TFRCNT;
	}

	/**
	 * * <summary> 裝鈔後轉帳金額 </summary> <remark>(含繳稅及繳費)</remark>
	 * 
	 * @return
	 */
	public String get_TFRAMT() {
		return _TFRAMT;
	}

	public void set_TFRAMT(String _TFRAMT) {
		this._TFRAMT = _TFRAMT;
	}

	/**
	 * * <summary> 上營業日轉帳筆數 </summary> <remark>(含繳稅及繳費)</remark>
	 * 
	 * @return
	 */
	public String get_LTFRCNT() {
		return _LTFRCNT;
	}

	public void set_LTFRCNT(String _LTFRCNT) {
		this._LTFRCNT = _LTFRCNT;
	}

	/**
	 * * <summary> 上營業日轉帳金額 </summary> <remark>(含繳稅及繳費)</remark>
	 * 
	 * @return
	 */
	public String get_LTFRAMT() {
		return _LTFRAMT;
	}

	public void set_LTFRAMT(String _LTFRAMT) {
		this._LTFRAMT = _LTFRAMT;
	}

	/**
	 * * <summary> 本營業日轉帳筆數 </summary> <remark>(含繳稅及繳費)</remark>
	 * 
	 * @return
	 */
	public String get_TTFRCNT() {
		return _TTFRCNT;
	}

	public void set_TTFRCNT(String _TTFRCNT) {
		this._TTFRCNT = _TTFRCNT;
	}

	/**
	 * * <summary> 本營業日轉帳金額 </summary> <remark>(含繳稅及繳費)</remark>
	 * 
	 * @return
	 */
	public String get_TTFRAMT() {
		return _TTFRAMT;
	}

	public void set_TTFRAMT(String _TTFRAMT) {
		this._TTFRAMT = _TTFRAMT;
	}

	/**
	 * <summary></summary><remark></remark>
	 * 
	 * @return
	 */
	public String get_FILLER1() {
		return _FILLER1;
	}

	public void set_FILLER1(String _FILLER1) {
		this._FILLER1 = _FILLER1;
	}

	public void set_TXCD_O(String _TXCD_O) {
		this._TXCD_O = _TXCD_O;
	}

	public void set_DATE(String _DATE) {
		this._DATE = _DATE;
	}

	public void set_TIME(String _TIME) {
		this._TIME = _TIME;
	}

	public void set_ATMNO_O(String _ATMNO_O) {
		this._ATMNO_O = _ATMNO_O;
	}

	public void set_MODE_O(String _MODE_O) {
		this._MODE_O = _MODE_O;
	}

	public void set_DD_O(String _DD_O) {
		this._DD_O = _DD_O;
	}

	public void set_DEPMODE_O(String _DEPMODE_O) {
		this._DEPMODE_O = _DEPMODE_O;
	}

	public void set_ATMSEQ_O1(String _ATMSEQ_O1) {
		this._ATMSEQ_O1 = _ATMSEQ_O1;
	}

	public void set_ATMSEQ_O2(String _ATMSEQ_O2) {
		this._ATMSEQ_O2 = _ATMSEQ_O2;
	}

	public void set_REJCD(String _REJCD) {
		this._REJCD = _REJCD;
	}

}
