package com.syscom.fep.enchelper.vo;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.enchelper.enums.ENCKeyFunction;
import com.syscom.fep.enchelper.enums.ENCKeyKind;

public class KeyId {
	/**
	 * KeyKind
	 */
	private ENCKeyKind keyKind;
	/**
	 * KeyFunction
	 */
	private ENCKeyFunction keyFunction;
	/**
	 * KeySubCode
	 */
	private String keySubCode;
	/**
	 * KeyVersion
	 */
	private String keyVersion;

	public ENCKeyKind getKeyKind() {
		return keyKind;
	}

	public void setKeyKind(ENCKeyKind keyKind) {
		this.keyKind = keyKind;
	}

	public ENCKeyFunction getKeyFunction() {
		return keyFunction;
	}

	public void setKeyFunction(ENCKeyFunction keyFunction) {
		this.keyFunction = keyFunction;
	}

	public String getKeySubCode() {
		return keySubCode;
	}

	public void setKeySubCode(String keySubCode) {
		this.keySubCode = keySubCode;
	}

	public String getKeyVersion() {
		return keyVersion;
	}

	public void setKeyVersion(String keyVersion) {
		this.keyVersion = keyVersion;
	}

	@Override
	public String toString() {
		if (StringUtils.isBlank(this.keySubCode)) {
			return StringUtils.rightPad(StringUtils.EMPTY, 20, StringUtils.SPACE);
		} else if (StringUtils.isBlank(this.keyVersion)) {
			this.keyVersion = " 1";
		}
		return StringUtils.join(
				StringUtils.rightPad(this.keyKind.name(), 4, StringUtils.SPACE), 
				StringUtils.rightPad(this.keyFunction.name(), 6, StringUtils.SPACE), 
				StringUtils.rightPad(this.keySubCode, 8, StringUtils.SPACE),
				StringUtils.leftPad(this.keyVersion, 2, StringUtils.SPACE));
	}
}
