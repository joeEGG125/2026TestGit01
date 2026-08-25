package com.syscom.fep.mybatis.enc.vo;

import org.apache.commons.lang3.StringUtils;

public interface MybatisConstant {

	public static final String ENTITY_PACKAGE_NAME = "com.syscom.fep.mybatis.enc.model";

	public static final String FILE_NAME_SUFFIX_EXT_MAPPER = "ExtMapper.class";
	public static final String PATH_EXT_MAPPER = StringUtils.join("com/syscom/fep/mybatis/enc/ext/mapper/*", FILE_NAME_SUFFIX_EXT_MAPPER);
	public static final String FILE_NAME_SUFFIX_EXT_MODEL = "Ext.class";
	public static final String PATH_EXT_MODEL = StringUtils.join("com/syscom/fep/mybatis/enc/ext/model/*", FILE_NAME_SUFFIX_EXT_MODEL);
	
	public static final String PROP_DEFAULT_VALUE_FOR_STRING_FIELD_CANNOT_BE_NULL_IN_DB = "defaultValueForStringFieldCannotBeNullInDb";

}
