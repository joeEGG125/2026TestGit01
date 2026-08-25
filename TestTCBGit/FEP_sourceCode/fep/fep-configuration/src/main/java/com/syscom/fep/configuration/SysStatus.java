package com.syscom.fep.configuration;

import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.mybatis.model.Sysstat;

public class SysStatus {

	private SysStatus() {}

	public static Sysstat getPropertyValue() throws Exception {
		return FEPCache.getSysstat();
	}
}
