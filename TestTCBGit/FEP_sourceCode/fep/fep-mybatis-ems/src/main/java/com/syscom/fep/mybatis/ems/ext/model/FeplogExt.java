package com.syscom.fep.mybatis.ems.ext.model;

import java.util.Date;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.BeanUtils;

import com.syscom.fep.mybatis.ems.model.Feplog;

public class FeplogExt extends Feplog {
	private static final long serialVersionUID = 1L;
	/**
	 * 用於標識操作的表名
	 */
	private String tableNameSuffix;

	public FeplogExt() {
		super();
	}

	public FeplogExt(Feplog feplog) {
		if (feplog == null)
			return;
		BeanUtils.copyProperties(feplog, this);
	}

	public String getTableNameSuffix() {
		return tableNameSuffix;
	}

	public void setTableNameSuffix(String tableNameSuffix) {
		this.tableNameSuffix = tableNameSuffix;
	}

	@Override
	public boolean equals(Object that) {
		if (super.equals(that)) {
			if (getClass() != that.getClass()) {
				return false;
			}
			FeplogExt other = (FeplogExt) that;
			return ((this.getTableNameSuffix() == null ? other.getTableNameSuffix() == null : this.getTableNameSuffix().equals(other.getTableNameSuffix())));
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getTableNameSuffix() == null) ? 0 : getTableNameSuffix().hashCode());
		return result;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
