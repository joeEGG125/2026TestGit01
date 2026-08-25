package com.syscom.fep.mybatis.deslog.vo;

import org.apache.commons.lang3.StringUtils;

public interface MybatisConstant {
    public static final String TABLE_NAME_DESLOG1 = "DESLOG1";

    public static final String ENTITY_PACKAGE_NAME = "com.syscom.fep.mybatis.deslog.model";
    public static final String ENTITY_NAME_DESLOG1 = "Deslog1";
    public static final String TABLE_NAME_PREFIX_DESLOG = "DESLOG";

    public static final String REPLACE_TABLE_NAME_FOR_DESLOG1 = "DESLOG${tableNameSuffix}";
    public static final String REPLACE_TABLE_NAME_FOR_DESLOG1_INCLUDE_REFID = "<include refid=\"chooseTableName\"/>";
    public static final String REPLACE_ENTITY_NAME_FOR_DESLOG1 = "Deslog";

    public static final String PARAMETER_NAME_TABLENAMESUFFIX = "tableNameSuffix";

    public static final String FILE_NAME_SUFFIX_EXT_MAPPER = "ExtMapper.class";
    public static final String PATH_EXT_MAPPER = StringUtils.join("com/syscom/fep/mybatis/deslog/ext/mapper/*", FILE_NAME_SUFFIX_EXT_MAPPER);
    public static final String FILE_NAME_SUFFIX_EXT_MODEL = "Ext.class";
    public static final String PATH_EXT_MODEL = StringUtils.join("com/syscom/fep/mybatis/deslog/ext/model/*", FILE_NAME_SUFFIX_EXT_MODEL);

    public static final String PROP_DEFAULT_VALUE_FOR_STRING_FIELD_CANNOT_BE_NULL_IN_DB = "defaultValueForStringFieldCannotBeNullInDb";
}
