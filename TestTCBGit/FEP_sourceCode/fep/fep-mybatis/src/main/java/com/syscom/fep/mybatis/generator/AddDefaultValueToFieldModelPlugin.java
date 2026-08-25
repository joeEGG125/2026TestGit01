package com.syscom.fep.mybatis.generator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import com.syscom.fep.mybatis.vo.MybatisConstant;

/**
 *
 * 修改查出的entity類
 *
 * 1.根據Table的定義，給是String類型的主鍵或者是不可以為null的欄位，加上預設值，預設值為StringUtils.SPACE
 * 2.如果是FEPTXN表，則還給BigDecimal類型的主鍵或者不可以為null的欄位，加上預設值，值為new BigDecimal(0)；同時給String類型的主鍵欄位對應的setter方法，加上是否是空字串的判斷
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
		boolean isFEPTXN01 = MybatisConstant.TABLE_NAME_FEPTXN01.equals(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName());
		boolean isFEPTXN = MybatisConstant.TABLE_NAME_FEPTXN.equals(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName());
		// find column
		IntrospectedColumn column = columnList.stream().filter(t -> t.getJavaProperty().equals(field.getName())).findFirst().orElse(null);
		if (column != null && (!column.isNullable() || column.isIdentity())) {
			if (String.class.getName().equals(field.getType().getFullyQualifiedName())) {
				field.setInitializationString(properties.getProperty(MybatisConstant.PROP_DEFAULT_VALUE_FOR_STRING_FIELD_CANNOT_BE_NULL_IN_DB, "StringUtils.SPACE"));
				if (isFEPTXN || isFEPTXN01) {
					Method setterMethod = methodList.stream().filter(
							t -> "set".equals(t.getName().substring(0, 3)) &&
							StringUtils.equalsIgnoreCase(t.getName().substring(3), field.getName()))
							.findFirst().orElse(null);
					if (setterMethod != null) {
						List<String> bodyLineList = new ArrayList<String>(setterMethod.getBodyLines());
						String parameterName = setterMethod.getParameters().get(0).getName();
						setterMethod.getBodyLines().clear();
						setterMethod.getBodyLines().add(StringUtils.join("if (StringUtils.isNotEmpty(", parameterName, ")) {"));
						setterMethod.getBodyLines().addAll(bodyLineList);
						setterMethod.getBodyLines().add("} else {");
						setterMethod.getBodyLines().add(StringUtils.join("this.", parameterName, " = StringUtils.SPACE;"));
						setterMethod.getBodyLines().add("}");
					}
				}
				return true;
			} else if ((isFEPTXN || isFEPTXN01) && BigDecimal.class.getName().equals(field.getType().getFullyQualifiedName())) {
				field.setInitializationString("new BigDecimal(0)");
			}
		}
		return false;
	}
}