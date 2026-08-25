package com.syscom.fep.vo.text.fcs;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.frmcommon.util.ConvertUtil;

/**
 * R1001_整批匯款格式明細 FCSDetail
 */
public class FCSDetail extends FCSDataBase {

	// 處理記號 v-登錄成功 x-登錄失敗(FEP判斷不送財金)
	private String _DATA_FLAG = "";

	// 匯款類別 跨行匯款-11 公庫匯款-12 匯款金額大於5000萬-18
	private String _REMTYPE = "";

	// 解款行代號 左補0
	private String _RECBANK = "";

	// 匯款金額 13位含2位小數
	private BigDecimal _REMAMT = new BigDecimal(0);

	// 匯款人名稱
	private String _NAME_SEND = "";

	// 解款人帳號
	private String _ACTNO = "";

	// 解款人名稱
	private String _NAME_RCV = "";

	// 手續費
	private BigDecimal _FEE = new BigDecimal(0);

	// 保留 固定為002002
	private String _Reserve1 = "002002";

	// 匯款人帳號
	private String _OUT_ACTNO = "";

	// 登錄序號
	private String _FEP_NO = "";

	// 登錄日期
	private String _REGDATE = "";

	// 匯款行
	private String _SENDBANK = "";

	// 附言
	private String _REMARK = "";

	// 財金回應RC
	private String _FEP_RC = "";

	// 錯誤訊息
	private String _ERRMSG = "";

	// FCS序號
	private String _FCS_Index = "";

	private final int _TotalLength = 367;

	/**
	 * 電文總長度
	 *
	 * @return 該組電文總長度
	 */
	public BigDecimal get_TotalLength() {
		return new BigDecimal(_TotalLength);
	}

	public String get_DATA_FLAG() {
		return _DATA_FLAG;
	}

	public void set_DATA_FLAG(String _DATA_FLAG) {
		this._DATA_FLAG = _DATA_FLAG;
	}

	public String get_REMTYPE() {
		return _REMTYPE;
	}

	public void set_REMTYPE(String _REMTYPE) {
		this._REMTYPE = _REMTYPE;
	}

	public String get_RECBANK() {
		return _RECBANK;
	}

	public void set_RECBANK(String _RECBANK) {
		this._RECBANK = _RECBANK;
	}

	public BigDecimal get_REMAMT() {
		return _REMAMT;
	}

	public void set_REMAMT(BigDecimal _REMAMT) {
		this._REMAMT = _REMAMT;
	}

	public String get_NAME_SEND() {
		return _NAME_SEND;
	}

	public void set_NAME_SEND(String _NAME_SEND) {
		this._NAME_SEND = _NAME_SEND;
	}

	public String get_ACTNO() {
		return _ACTNO;
	}

	public void set_ACTNO(String _ACTNO) {
		this._ACTNO = _ACTNO;
	}

	public String get_NAME_RCV() {
		return _NAME_RCV;
	}

	public void set_NAME_RCV(String _NAME_RCV) {
		this._NAME_RCV = _NAME_RCV;
	}

	public BigDecimal get_FEE() {
		return _FEE;
	}

	public void set_FEE(BigDecimal _FEE) {
		this._FEE = _FEE;
	}

	public String get_Reserve1() {
		return _Reserve1;
	}

	public void set_Reserve1(String _Reserve1) {
		this._Reserve1 = _Reserve1;
	}

	public String get_OUT_ACTNO() {
		return _OUT_ACTNO;
	}

	public void set_OUT_ACTNO(String _OUT_ACTNO) {
		this._OUT_ACTNO = _OUT_ACTNO;
	}

	public String get_FEP_NO() {
		return _FEP_NO;
	}

	public void set_FEP_NO(String _FEP_NO) {
		this._FEP_NO = _FEP_NO;
	}

	public String get_REGDATE() {
		return _REGDATE;
	}

	public void set_REGDATE(String _REGDATE) {
		this._REGDATE = _REGDATE;
	}

	public String get_SENDBANK() {
		return _SENDBANK;
	}

	public void set_SENDBANK(String _SENDBANK) {
		this._SENDBANK = _SENDBANK;
	}

	public String get_REMARK() {
		return _REMARK;
	}

	public void set_REMARK(String _REMARK) {
		this._REMARK = _REMARK;
	}

	public String get_FEP_RC() {
		return _FEP_RC;
	}

	public void set_FEP_RC(String _FEP_RC) {
		this._FEP_RC = _FEP_RC;
	}

	public String get_ERRMSG() {
		return _ERRMSG;
	}

	public void set_ERRMSG(String _ERRMSG) {
		this._ERRMSG = _ERRMSG;
	}

	public String get_FCS_Index() {
		return _FCS_Index;
	}

	public void set_FCS_Index(String _FCS_Index) {
		this._FCS_Index = _FCS_Index;
	}

	public final String merge() {
		StringBuilder rtn = new StringBuilder("");

		rtn.append(StringUtils.rightPad(_DATA_FLAG, 1, ' '));
		rtn.append(StringUtils.rightPad(_REMTYPE, 2, ' '));
		rtn.append(StringUtils.leftPad(_REMTYPE, 7, '0'));
		rtn.append(StringUtils.leftPad(String.valueOf(_REMAMT.intValue() * 100), 13, '0'));
		rtn.append(padChineseBlankRight(_NAME_SEND, 50));
		rtn.append(StringUtils.leftPad(_ACTNO, 14, '0'));
		rtn.append(padChineseBlankRight(_NAME_RCV, 50));
		rtn.append(StringUtils.leftPad(String.valueOf(_FEE), 5, '0'));
		rtn.append("002002");
		rtn.append(StringUtils.leftPad(_OUT_ACTNO, 14, '0'));
		rtn.append(StringUtils.rightPad(_FEP_NO, 7, ' '));
		rtn.append(StringUtils.rightPad(_REGDATE, 7, ' '));
		rtn.append(StringUtils.rightPad(_SENDBANK, 7, ' '));
		rtn.append(padChineseBlankRight(_REMARK, 60));
		rtn.append(StringUtils.rightPad(_FEP_RC, 6, ' '));
		rtn.append(padChineseBlankRight(_ERRMSG, 80));
		rtn.append(StringUtils.rightPad(_FCS_Index, 38, ' '));
		return rtn.toString();
	}

	public final int parse(String sFileLine) {
		int tmpLen = ConvertUtil.toBytes(sFileLine, PolyfillUtil.toCharsetName("big5")).length;

		if (tmpLen < _TotalLength) {
			sFileLine = sFileLine + PolyfillUtil.space(_TotalLength - tmpLen);
		}

		_DATA_FLAG = subStr(sFileLine, 0, 1).trim();
		_REMTYPE = subStr(sFileLine, 1, 2).trim();
		_RECBANK = subStr(sFileLine, 3, 7).trim();
		_REMAMT = new BigDecimal((Integer.parseInt(subStr(sFileLine, 10, 13)) / 100));
		_NAME_SEND = subStr(sFileLine, 23, 50).trim();
		_ACTNO = subStr(sFileLine, 73, 14).trim();
		_NAME_RCV = subStr(sFileLine, 87, 50).trim();
		_FEE = new BigDecimal(subStr(sFileLine, 137, 5));
		_Reserve1 = subStr(sFileLine, 142, 6).trim();
		_OUT_ACTNO = subStr(sFileLine, 148, 14).trim();
		_FEP_NO = subStr(sFileLine, 162, 7).trim();
		_REGDATE = subStr(sFileLine, 169, 7).trim();
		_SENDBANK = subStr(sFileLine, 176, 7).trim();
		_REMARK = subStr(sFileLine, 183, 60).trim();
		_FEP_RC = subStr(sFileLine, 243, 6).trim();
		_ERRMSG = subStr(sFileLine, 249, 80).trim();
		_FCS_Index = subStr(sFileLine, 329, 38).trim();
		return 0;
	}
}
