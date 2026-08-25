package com.syscom.fep.mybatis.deslog.generator;

import org.apache.ibatis.type.JdbcType;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

import java.sql.Types;

public class MyJavaTypeResolverDefaultImpl extends JavaTypeResolverDefaultImpl {
	public MyJavaTypeResolverDefaultImpl() {
		super();
		super.typeMap.put(Types.OTHER, new JdbcTypeInformation(JdbcType.NVARCHAR.name(), new FullyQualifiedJavaType(String.class.getName())));
	}
}
