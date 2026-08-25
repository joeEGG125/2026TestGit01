package com.syscom.fep.vo;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

/**
 * CodeGen後電文和物件互轉會用到的共用方法
 *
 * @author mickey
 *
 */
public class CodeGenUtil {
	// 測議用
//	public static void main(String[] args) {
//		System.out.println(bigDecimalToEbcdic(new BigDecimal(123), 5, true, 2, true));
//		String ebcdic = "4EF0F0F1F2F34BF0F0";
//		System.out.println(ebcdicToAsciiDefaultEmpty(ebcdic));
//		System.out.println(ebcdicToBigDecimal(ebcdic, true, 2, true));
//	}

	public static BigDecimal ebcdicToBigDecimal( //
			String ebcdic, //
			boolean hasPoint, //
			int floatLen, //
			boolean hasSign //
	) {
		// EBCDIC To String(ASCII)
		String numberStr = CodeGenUtil.ebcdicToAsciiDefaultEmpty(ebcdic);
		// String(ASCII) To BigDecimal
		return CodeGenUtil.asciiToBigDecimal(numberStr, hasSign, floatLen);
	}

	public static String bigDecimalToEbcdic( //
			BigDecimal number, //
			int numberLen, //
			boolean hasPoint, //
			int floatLen, //
			boolean hasSign //
	) {
		// BigDecimal To String(ASCII)
		if(number == null){
			number=BigDecimal.ZERO;
		}
		String numberStr = CodeGenUtil.bigDecimalToAscii(number, numberLen, hasPoint, floatLen, hasSign);
		// 總長度
		int totalLen = //
				(hasSign ? 1 : 0) + // +-符號
				numberLen + // 小數點前長度
				(hasPoint ? 1 : 0) + // 小數點
				floatLen; // 小數點後長度
		// String(ASCII) to EBCDIC
		return EbcdicConverter.toHex(CCSID.English, totalLen, numberStr);
	}

	/**
	 * String(ASCII) To BigDecimal</br>
	 * S9(11)V99，表示有+_符號、小數前長度為11、無小數點、小數後長度為2</br>
	 * S9(14).99，表示有+_符號、小數前長度為14、有小數點、小數後長度為2
	 *
	 * @param numberStr ascii
	 * @param hasPoint  是否有小數點
	 * @param floatLen  小數點後長度
	 * @return
	 * @throws ParseException BigDecimal轉String錯誤
	 */
	public static BigDecimal asciiToBigDecimal( //
			String numberStr, //
			boolean hasPoint, //
			int floatLen //
	) {
		// 字串為空，返回null
		if (numberStr == null || StringUtils.equals(numberStr.trim(), StringUtils.EMPTY)) {
			return null;
		}
		BigDecimal number = new BigDecimal(numberStr);
		// 小數點後長度，數值轉為小數
		for (int i = 0; i < floatLen && !hasPoint; i++) {
			number = number.divide(new BigDecimal(10));
		}
		return number;
	}

	/**
	 * BigDecimal To String(ASCII)</br>
	 * S9(11)V99，表示有+_符號、小數前長度為11、無小數點、小數後長度為2</br>
	 * S9(14).99，表示有+_符號、小數前長度為14、有小數點、小數後長度為2
	 *
	 * @param number
	 * @param numberLen 小數點前長度
	 * @param hasPoint  是否有小數點
	 * @param floatLen  小數點後長度
	 * @param hasSign   是否有+-符號
	 * @return
	 */
	public static String bigDecimalToAscii( //
			BigDecimal number, //
			int numberLen, //
			boolean hasPoint, //
			int floatLen, //
			boolean hasSign //
	) {
		// 數字為null，返回空字串
		if (number == null) {
			return StringUtils.EMPTY;
		}
		// 小數點後長度
		String floatLenFormatStr = StringUtils.EMPTY;
		for (int i = 0; i < floatLen; i++) {
			floatLenFormatStr = floatLenFormatStr + "0";
			// 如果需要顯示小數，則不需要乘上相應的小數位數
			if (!hasPoint) {
				number = number.multiply(new BigDecimal(10));
			}
		}
		// 小數點前長度
		String numberLenFormatStr = StringUtils.EMPTY;
		for (int i = 0; i < numberLen; i++) {
			numberLenFormatStr = numberLenFormatStr + "0";
		}
		// +0000000;-0000000
		// +00000.00;-00000.00
		String format = "%s%s%s%s;%s%s%s%s";
		String decimalFormatStr = String.format( //
				format, //
				hasSign ? "+" : StringUtils.EMPTY, // 正數符號
				numberLenFormatStr, // 小數點前
				hasPoint ? "." : StringUtils.EMPTY, // 小數點
				floatLenFormatStr, // 小數點後
				hasSign ? "-" : StringUtils.EMPTY, // 負數符號
				numberLenFormatStr, // 小數點前
				hasPoint ? "." : StringUtils.EMPTY, // 小數點
				floatLenFormatStr // 小數點後
		);
		// 當不顯示符號，取絕對值，否則因為位數不對導致Exception
		if (!hasSign) {
			number = number.abs();
		}
		// BigDecimal to String
		DecimalFormat decimalFormat = new DecimalFormat(decimalFormatStr);
		return decimalFormat.format(number);
	}

	public static String bigDecimalToAsciiCBS( //
											BigDecimal number, //
											int numberLen, //
											boolean hasPoint, //
											int floatLen, //
											boolean hasSign //
	) {
		// 數字為null，返回空字串
		if (number == null) {
			number =BigDecimal.ZERO;
		}
		// 小數點後長度
		String floatLenFormatStr = StringUtils.EMPTY;
		for (int i = 0; i < floatLen; i++) {
			floatLenFormatStr = floatLenFormatStr + "0";
			// 如果需要顯示小數，則不需要乘上相應的小數位數
			if (!hasPoint) {
				number = number.multiply(new BigDecimal(10));
			}
		}
		// 小數點前長度
		String numberLenFormatStr = StringUtils.EMPTY;
		for (int i = 0; i < numberLen; i++) {
			numberLenFormatStr = numberLenFormatStr + "0";
		}
		// +0000000;-0000000
		// +00000.00;-00000.00
		String format = "%s%s%s%s;%s%s%s%s";
		String decimalFormatStr = String.format( //
				format, //
				hasSign ? "+" : StringUtils.EMPTY, // 正數符號
				numberLenFormatStr, // 小數點前
				hasPoint ? "." : StringUtils.EMPTY, // 小數點
				floatLenFormatStr, // 小數點後
				hasSign ? "-" : StringUtils.EMPTY, // 負數符號
				numberLenFormatStr, // 小數點前
				hasPoint ? "." : StringUtils.EMPTY, // 小數點
				floatLenFormatStr // 小數點後
		);
		// 當不顯示符號，取絕對值，否則因為位數不對導致Exception
		if (!hasSign) {
			number = number.abs();
		}
		// BigDecimal to String
		DecimalFormat decimalFormat = new DecimalFormat(decimalFormatStr);
		return decimalFormat.format(number);
	}

	/**
	 * String(ASCII) to EBCDIC
	 *
	 * @param value
	 * @param length
	 * @return
	 */
	public static String asciiToEbcdicDefaultEmpty(String value, int length) {
		return (value == null) ? //
				EbcdicConverter.toHex(CCSID.English, length, StringUtils.EMPTY) : //
				EbcdicConverter.toHex(CCSID.English, length, value);
	}

	/**
	 * EBCDIC to String(ASCII)
	 *
	 * @param value
	 * @return
	 */
	public static String ebcdicToAsciiDefaultEmpty(String value) {
		return (value == null) ? //
				null : //
				EbcdicConverter.fromHex(CCSID.English, value);
	}
}
