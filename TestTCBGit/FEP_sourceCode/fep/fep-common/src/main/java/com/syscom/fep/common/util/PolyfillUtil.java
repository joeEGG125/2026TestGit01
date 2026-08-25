package com.syscom.fep.common.util;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.util.ExceptionUtil;

/**
 * 用於填補一些java沒有的.NET函數或者方法
 * 
 * @author Richard
 */
public class PolyfillUtil {

	private PolyfillUtil() {}

	/**
	 * 相當於VB中的CType函數
	 * 
	 * @param value
	 * @return
	 */
	public static boolean ctype(Object value) {
		if (value == null) {
			return false;
		} else if (value instanceof String) {
			return "0".equals(value) ? false : true;
		} else if (value instanceof Integer) {
			return ctype(((Integer) value).toString());
		} else if (value instanceof Long) {
			return ctype(((Long) value).toString());
		} else if (value instanceof Double) {
			return ctype(((Double) value).toString());
		} else if (value instanceof Short) {
			return ctype(((Short) value).toString());
		} else if (value instanceof Byte) {
			return ctype(((Byte) value).toString());
		} else if (value instanceof Character) {
			return ctype(((Character) value).toString());
		} else if (value instanceof Boolean) {
			return (Boolean) value;
		}
		throw ExceptionUtil.createIllegalArgumentException("cannot cast [", value.getClass().getName(), "] with value = [", value, "] to boolean");
	}

	/**
	 * Java中的byte轉為.NET中的byte
	 * 
	 * c#中字節byte的范圍是0~255，即無符號
	 * 
	 * java中字節byte的范圍是-128~127，即有符號
	 * 
	 * @param b
	 * @return
	 */
	public static int toInteger(byte b) {
		return Byte.toUnsignedInt(b);
	}


	/**
	 * 十六進制字串轉字節
	 *
	 * c#中字節byte的范圍是0~255，即無符號
	 *
	 * java中字節byte的范圍是-128~127，範圍不夠，所以轉爲int
	 *
	 * @param b
	 * @return
	 */
	public static int hexToInt(String b) {
		return Integer.parseInt(b,16);
	}

	/**
	 * 等同於.NET中的int.ToString("")方法，例如1.ToString("0000")，則等同於下面的方法toString(1， "0000")
	 * 
	 * @param value
	 * @param format
	 * @return
	 */
	public static String toString(int value, String format) {
		for (int i = 0; i < format.length(); i++) {
			if (format.charAt(i) != '0') {
				throw ExceptionUtil.createIllegalArgumentException("invalid format : [", format, "]");
			}
		}
		return StringUtils.leftPad(Integer.toString(value), format.length(), format.charAt(0));
	}

	/**
	 * .NET中的encoding name轉Java中的charset name
	 * 
	 * @param encodingName
	 * @return
	 */
	public static String toCharsetName(String encodingName) {
		if ("950".equals(encodingName)) {
			return "MS950";
		} else if ("Unicode".equalsIgnoreCase(encodingName)) {
			return "UTF-16LE";
		}
		return encodingName;
	}

	/**
	 * 此方法等同於VB.NET中的isNumeric方法
	 * 
	 * 包含科學計數, 首字符為+/-符號
	 *
	 * 2024-09-11 Richard modified for 【ReDoS In Pattern】
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		// Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*[\\.]?[\\d]*([e|E][-\\+]?\\d+)?$");
		// return pattern.matcher(str).matches();
		try {
			Double.parseDouble(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * 此方法等同於VB.NET中的Space方法
	 * 
	 * @param number
	 * @return
	 */
	public static String space(int number) {
		return StringUtils.repeat(' ', number);
	}

	/**
	 * 
	 * 摘要:
	 * 返回已轉換為指定形式的字符串。
	 * 
	 * 參數:
	 * str:
	 * 必需。要轉換的 String 表達式。
	 * 
	 * Conversion:
	 * 必需。Microsoft.VisualBasic.VbStrConv 成員。指定要執行的轉換類型的枚舉值。
	 * 
	 * LocaleID:
	 * 可選。LocaleID 值（如果與系統 LocaleID 值不同）。（系統 LocaleID 值為預設值。）
	 * 
	 * 返回結果:
	 * 返回已轉換為指定形式的字符串。
	 * 
	 * 異常:
	 * T:System.ArgumentException:
	 * 不支持的 LocaleID、Conversion < 0 或 > 2048，或指定的區域設置不支持的轉換。
	 * 
	 * @param value
	 * @param conversion
	 * @param localeID
	 * @return
	 */
	public static String strConv(String value, VbStrConv conversion, int localeID) {
		// TODO
		return value;
	}

	public enum VbStrConv {
		//
		// 摘要:
		// 不執行轉換。
		None(0),
		//
		// 摘要:
		// 將字符串轉換為大寫字符。該成員等效于 Visual Basic 常數 vbUpperCase。
		Uppercase(1),
		//
		// 摘要:
		// 將字符串轉換為小寫字符。該成員等效于 Visual Basic 常數 vbLowerCase。
		Lowercase(2),
		//
		// 摘要:
		// 將字符串中每個單詞的首字母轉換為大寫。該成員等效于 Visual Basic 常數 vbProperCase。
		ProperCase(3),
		//
		// 摘要:
		// 將字符串中的窄（單字節）字符轉換為寬（雙字節）字符。應用于亞洲區域設置。該成員等效于 Visual Basic 常數 vbWide。
		Wide(4),
		//
		// 摘要:
		// 將字符串中的寬（雙字節）字符轉換為窄（單字節）字符。應用于亞洲區域設置。該成員等效于 Visual Basic 常數 vbNarrow。
		Narrow(8),
		//
		// 摘要:
		// 將字符串中的平假名字符轉換為片假名字符。僅應用于日文區域設置。該成員等效于 Visual Basic 常數 vbKatakana。
		Katakana(16),
		//
		// 摘要:
		// 將字符串中的片假名字符轉換為平假名字符。僅應用于日文區域設置。該成員等效于 Visual Basic 常數 vbHiragana。
		Hiragana(32),
		//
		// 摘要:
		// 將字符串轉換為簡體中文字符。該成員等效于 Visual Basic 常數 vbSimplifiedChinese。
		SimplifiedChinese(256),
		//
		// 摘要:
		// 將字符串轉換為繁體中文字符。該成員等效于 Visual Basic 常數 vbTraditionalChinese。
		TraditionalChinese(512),
		//
		// 摘要:
		// 將字符串從大小寫檔案系統規則轉換為語義規則。該成員等效于 Visual Basic 常數 vbLinguisticCasing。
		LinguisticCasing(1024);

		private int value;

		private VbStrConv(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}
}
