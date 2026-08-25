package com.syscom.fep.mybatis.his.generator;

import com.syscom.fep.mybatis.his.vo.MybatisConstant;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.ArrayList;
import java.util.List;

/**
 * *修改查出的entity類
 * 
 * 1.根據Table的定義，給是String類型的主鍵或者是不可以為null的欄位，加上預設值，預設值為StringUtils.SPACE
 * 
 * @author Richard
 *
 */
public class AddDefaultValueToFieldModelPlugin extends PluginAdapter {

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		List<IntrospectedColumn> columnList = introspectedTable.getAllColumns();
		List<Method> methodList = topLevelClass.getMethods();
		// field
		List<Field> fieldList = new ArrayList<Field>(topLevelClass.getFields());
		topLevelClass.getFields().clear();
		boolean addStringUtilsImportFlag = false;
		for (Field field : fieldList) {
			addStringUtilsImportFlag |= this.setInitializationString(introspectedTable, field, columnList, methodList);
			topLevelClass.getFields().add(field);
		}
		// addImportedType
		if (addStringUtilsImportFlag) {
			FullyQualifiedJavaType stringUtilsImport = new FullyQualifiedJavaType(StringUtils.class.getName());
			topLevelClass.addImportedType(stringUtilsImport);
		}
		return true;
	}

	private boolean setInitializationString(IntrospectedTable introspectedTable, Field field, List<IntrospectedColumn> columnList, List<Method> methodList) {
		// find column
		IntrospectedColumn column = columnList.stream().filter(t -> t.getJavaProperty().equals(field.getName())).findFirst().orElse(null);
		if (column != null && (!column.isNullable() || column.isIdentity())) {
			if (String.class.getName().equals(field.getType().getFullyQualifiedName())) {
				field.setInitializationString(properties.getProperty(MybatisConstant.PROP_DEFAULT_VALUE_FOR_STRING_FIELD_CANNOT_BE_NULL_IN_DB, "StringUtils.SPACE"));
				return true;
			}
		}
		return false;
	}
}
