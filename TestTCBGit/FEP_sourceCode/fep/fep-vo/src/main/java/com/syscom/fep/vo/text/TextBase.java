package com.syscom.fep.vo.text;

import java.math.BigDecimal;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;

public abstract class TextBase<General> {

	public abstract int getTotalLength();

	public abstract String makeMessageFromGeneral(General general) throws Exception;

	public abstract void toGeneral(General general) throws Exception;

	/**
	 * 轉ASCII
	 *
	 * @param value
	 * @return
	 * @throws DecoderException
	 */
	protected String toAscii(String value) throws DecoderException {
		return this.toAscii(value, true);
	}

	/**
	 * 轉ASCII，是否需要trim
	 *
	 * @param value
	 * @param trim
	 * @return
	 * @throws DecoderException
	 */
	protected String toAscii(String value, boolean trim) throws DecoderException {
		String ret = StringUtil.fromHex(value);
		return trim ? ret.trim() : ret;
	}

	/**
	 * 轉十六進制，並用0左補齊一共length長度
	 *
	 * @param value
	 * @param length
	 * @return
	 */
	protected String toHex(BigDecimal value, int length) {
		return StringUtil.toHex(this.toString(value, length));
	}

	/**
	 * 轉字串，並用0左補齊一共length長度
	 *
	 * @param value
	 * @param length
	 * @return
	 */
	protected String toString(BigDecimal value, int length) {
		return this.toString(value, length, 0);
	}

	/**
	 * 轉十六進制，並用0左補齊一共length長度，並且保留decLength個小數位
	 *
	 * @param value
	 * @param length
	 * @param decLength
	 * @return
	 */
	protected String toHex(BigDecimal value, int length, int decLength) {
		return StringUtil.toHex(this.toString(value, length, decLength));
	}

	/**
	 * 轉字串，並用0左補齊一共length長度，並且保留decLength個小數位
	 *
	 * @param value
	 * @param length
	 * @param decLength
	 * @return
	 */
	protected String toString(BigDecimal value, int length, int decLength) {
		return StringUtils.leftPad(MathUtil.roundFloor(value.multiply(new BigDecimal("10").pow(decLength)), 0).toString(), length, '0');
	}

	/**
	 * 電文中的內容轉ascii之後再轉為int型的BigDecimal
	 *
	 * @param value
	 * @return
	 * @throws DecoderException
	 */
	protected BigDecimal toBigDecimal(String value) throws DecoderException {
		return this.toBigDecimal(value, "0");
	}

	/**
	 * 電文中的內容轉ascii之後再轉為int型的BigDecimal
	 *
	 * @param value
	 * @param defaultValue
	 * @return
	 * @throws DecoderException
	 */
	protected BigDecimal toBigDecimal(String value, String defaultValue) throws DecoderException {
		try {
			return new BigDecimal(this.toAscii(value, false));
		} catch (NumberFormatException e) {
			return new BigDecimal(defaultValue);
		}
	}

	/**
	 * 電文中的內容轉ascii之後再轉為double型的BigDecimal，並且保留decLength個小數位
	 *
	 * @param value
	 * @param decLength
	 * @return
	 * @throws DecoderException
	 */
	protected BigDecimal toBigDecimal(String value, int decLength) throws DecoderException {
		return this.toBigDecimal(value, decLength, "0");
	}

	/**
	 * 電文中的內容轉ascii之後再轉為double型的BigDecimal，並且保留decLength個小數位
	 *
	 * @param value
	 * @param decLength
	 * @param defaultValue
	 * @return
	 * @throws DecoderException
	 */
	protected BigDecimal toBigDecimal(String value, int decLength, String defaultValue) throws DecoderException {
		try {
			BigDecimal ret = new BigDecimal(this.toAscii(value, false));
			return decLength > 0 ? ret.divide(new BigDecimal("10").pow(decLength)) : ret;
		} catch (NumberFormatException e) {
			return new BigDecimal(defaultValue);
		}
	}

	protected String serializeToXml(Object object, boolean... formatted) {
		return XmlUtil.toXML(object, formatted);
	}
}
