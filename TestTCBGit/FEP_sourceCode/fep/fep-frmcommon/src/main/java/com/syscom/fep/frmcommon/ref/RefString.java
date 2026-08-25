package com.syscom.fep.frmcommon.ref;

import org.apache.commons.lang3.StringUtils;

/**
 * 針對C#中的ref String，建立按照地址傳值的String類
 * 
 * @author Richard
 */
public class RefString extends RefBase<String> {
	private static final long serialVersionUID = -3459745738879502212L;

	public RefString() {
		this(StringUtils.EMPTY);
	}

	public RefString(String value) {
		super(value);
	}

	public boolean isBlank() {
		return StringUtils.isBlank(this.value);
	}

	public boolean isNotBlank() {
		return StringUtils.isNotBlank(this.value);
	}

	public String substring(int beginIndex) {
		return this.value.substring(beginIndex);
	}

	public String substring(int beginIndex, int endIndex) {
		return this.value.substring(beginIndex, endIndex);
	}
}
