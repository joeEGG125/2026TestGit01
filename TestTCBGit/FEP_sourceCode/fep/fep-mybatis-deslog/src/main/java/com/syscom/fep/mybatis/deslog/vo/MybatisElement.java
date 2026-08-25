package com.syscom.fep.mybatis.deslog.vo;

public enum MybatisElement {
	ATTRIBUTE_PARAMETERTYPE("parameterType");

	private String name;

	private MybatisElement(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
