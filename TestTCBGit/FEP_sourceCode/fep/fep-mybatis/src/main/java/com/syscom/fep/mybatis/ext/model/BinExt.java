package com.syscom.fep.mybatis.ext.model;

import java.util.Date;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.BeanUtils;

import com.syscom.fep.mybatis.model.Bin;

public class BinExt extends Bin {
	private static final long serialVersionUID = 1L;

	private boolean binMulticur;

	public BinExt() {
		super();
	}

	public BinExt(String binNo, String binBkno, String binNet, String binZone, String binOrg, String binProd, Integer updateUserid, Date updateTime, Short binEmvflag) {
		super(binNo, binBkno, binNet, binZone, binOrg, binProd, updateUserid, updateTime, binEmvflag);
	}

	public BinExt(Bin bin) {
		if (bin == null)
			return;
		BeanUtils.copyProperties(bin, this);
	}

	public boolean isBinMulticur() {
		return binMulticur;
	}

	public void setBinMulticur(boolean binMulticur) {
		this.binMulticur = binMulticur;
	}

	@Override
	public boolean equals(Object that) {
		if (super.equals(that)) {
			if (getClass() != that.getClass()) {
				return false;
			}
			BinExt other = (BinExt) that;
			return this.isBinMulticur() == other.isBinMulticur();
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Boolean.valueOf(isBinMulticur()).hashCode();
		return result;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
