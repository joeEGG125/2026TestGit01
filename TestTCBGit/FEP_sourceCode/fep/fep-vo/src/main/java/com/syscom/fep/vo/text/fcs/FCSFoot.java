package com.syscom.fep.vo.text.fcs;

import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * R1001_整批匯款格式檔尾 FCSFoot
 */
public class FCSFoot extends FCSDataBase {

	// 匯款總筆數
	private int _CNT;

	// 匯款總金額
	private BigDecimal _AMT = new BigDecimal(0);

	// 需匯出筆數
	private int _SUCESS_CNT;

	// 需匯款總金額
	private BigDecimal _SUCESS_AMT = new BigDecimal(0);

	private static final int _TotalLength = 36;

	public int get_CNT() {
		return _CNT;
	}

	public void set_CNT(int _CNT) {
		this._CNT = _CNT;
	}

	public BigDecimal get_AMT() {
		return _AMT;
	}

	public void set_AMT(BigDecimal _AMT) {
		this._AMT = _AMT;
	}

	public int get_SUCESS_CNT() {
		return _SUCESS_CNT;
	}

	public void set_SUCESS_CNT(int _SUCESS_CNT) {
		this._SUCESS_CNT = _SUCESS_CNT;
	}

	public BigDecimal get_SUCESS_AMT() {
		return _SUCESS_AMT;
	}

	public void set_SUCESS_AMT(BigDecimal _SUCESS_AMT) {
		this._SUCESS_AMT = _SUCESS_AMT;
	}

	public static BigDecimal get_TotalLength() {
		return new BigDecimal(_TotalLength);
	}

	public final String merge() {
		StringBuilder rtn = new StringBuilder("");
		rtn.append(StringUtils.leftPad(String.valueOf(_CNT), 5, '0'));
		rtn.append(StringUtils.leftPad(String.valueOf((_AMT.multiply(new BigDecimal(100))).toBigInteger()), 13, '0'));
		rtn.append(StringUtils.leftPad(String.valueOf(_SUCESS_CNT), 5, '0'));
		rtn.append(StringUtils.leftPad(String.valueOf((_SUCESS_AMT.multiply(new BigDecimal(100))).toBigInteger()), 13, '0'));
		return rtn.toString();
	}

	public final int parse(String sFileFootLine) {
		int tmpLen = ConvertUtil.toBytes(sFileFootLine, PolyfillUtil.toCharsetName("big5")).length;

		if (tmpLen < _TotalLength) {
			sFileFootLine = sFileFootLine + PolyfillUtil.space(_TotalLength - tmpLen);
		}
		_CNT = Integer.parseInt(subStr(sFileFootLine, 0, 5));
		_AMT = new BigDecimal(subStr(sFileFootLine, 5, 13)).divide(new BigDecimal(100)).setScale(1, RoundingMode.HALF_UP);
		_SUCESS_CNT = Integer.parseInt(subStr(sFileFootLine, 18, 5).trim());
		_SUCESS_AMT = new BigDecimal(subStr(sFileFootLine, 23, 13)).divide(new BigDecimal(100)).setScale(1, RoundingMode.HALF_UP);
		return 0;
	}
}