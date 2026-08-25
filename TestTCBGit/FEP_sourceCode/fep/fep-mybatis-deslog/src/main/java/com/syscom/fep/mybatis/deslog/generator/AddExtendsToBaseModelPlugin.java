package com.syscom.fep.mybatis.deslog.generator;

import com.syscom.fep.mybatis.deslog.vo.BaseModel;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

public class AddExtendsToBaseModelPlugin extends PluginAdapter {
	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		topLevelClass.getSuperInterfaceTypes().clear();
		// setSuperClass
		FullyQualifiedJavaType superClass = new FullyQualifiedJavaType(BaseModel.class.getName());
		topLevelClass.setSuperClass(superClass);
		// remove serializable import
		FullyQualifiedJavaType serializableImport = topLevelClass.getImportedTypes().stream().filter(x -> x.getFullyQualifiedName().equals("java.io.Serializable")).findFirst().orElse(null);
		if (serializableImport != null) {
			topLevelClass.getImportedTypes().remove(serializableImport);
		}
		// addImportedType
		FullyQualifiedJavaType baseModelImport = new FullyQualifiedJavaType(BaseModel.class.getName());
		topLevelClass.addImportedType(baseModelImport);
		return true;
	}
}