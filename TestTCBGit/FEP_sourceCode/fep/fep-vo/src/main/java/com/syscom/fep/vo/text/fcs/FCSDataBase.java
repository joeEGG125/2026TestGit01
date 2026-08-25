package com.syscom.fep.vo.text.fcs;

import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.frmcommon.util.ConvertUtil;

/**
 * 中文字串處理函數
 */
public class FCSDataBase {

	protected String padChineseBlankRight(String strfSource, int intLen) {
		String space = PolyfillUtil.space(intLen - ConvertUtil.toBytes(strfSource, PolyfillUtil.toCharsetName("big5")).length);
		return strfSource + space;
	}

	protected String subStr(String strfSource, int intStart, int intLen) {
		byte[] byteAry = StrToByteAry(strfSource);
		byte[] newAry = new byte[intLen];
		int i = 0;
		int j = 0;
		while (j < intLen) {
			newAry[i] = byteAry[intStart];
			i = i + 1;
			j = j + 1;
			intStart = intStart + 1;
		}
		return ConvertUtil.toString(newAry, PolyfillUtil.toCharsetName("big5"));
	}

	protected byte[] StrToByteAry(String oriString) {
		return ConvertUtil.toBytes(oriString, PolyfillUtil.toCharsetName("big5"));
	}
}
