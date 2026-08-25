package com.syscom.fep.mybatis.vo;

public enum MybatisMethod {

	SELECT_BY_PRIMARY_KEY("selectByPrimaryKey"),
	INSERT("insert"),
	INSERT_SELECTIVE("insertSelective"),
	UPDATE_BY_PRIMARY_KEY("updateByPrimaryKey"),
	UPDATE_BY_PRIMARY_KEY_SELECTIVE("updateByPrimaryKeySelective"),
	UPDATE_BY_PRIMARY_KEY_WITH_BLOBS("updateByPrimaryKeyWithBLOBs"),
	DELETE_BY_PRIMARY_KEY("deleteByPrimaryKey");

	private String name;

	private MybatisMethod(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
