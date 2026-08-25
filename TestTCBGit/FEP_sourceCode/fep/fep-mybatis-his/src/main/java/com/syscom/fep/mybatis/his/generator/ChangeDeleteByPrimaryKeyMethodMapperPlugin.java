package com.syscom.fep.mybatis.his.generator;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.mybatis.his.vo.MybatisConstant;
import com.syscom.fep.mybatis.his.vo.MybatisElement;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

public class ChangeDeleteByPrimaryKeyMethodMapperPlugin extends PluginAdapter {
	private LogHelper log = new LogHelper();

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		String modelName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
		method.getParameters().clear();
		FullyQualifiedJavaType modelJavaType = new FullyQualifiedJavaType(StringUtils.join(MybatisConstant.ENTITY_PACKAGE_NAME, ".", modelName));
		Parameter parameter = new Parameter(modelJavaType, "record");
		method.getParameters().add(parameter);
		return true;
	}

	@Override
	public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		String modelName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
		List<Attribute> attributeList = element.getAttributes();
		if (CollectionUtils.isNotEmpty(attributeList)) {
			Attribute parameterTypeAttribute = attributeList.stream().filter(x -> x.getName().equals(MybatisElement.ATTRIBUTE_PARAMETERTYPE.getName())).findFirst().orElse(null);
			if (parameterTypeAttribute != null) {
				attributeList.remove(parameterTypeAttribute);
				attributeList.add(new Attribute(parameterTypeAttribute.getName(), StringUtils.join(MybatisConstant.ENTITY_PACKAGE_NAME, ".", modelName)));
			} else {
				log.error("[", modelName, "] attribute \"parameterType\" of deleteByPrimaryKey in xml is null!!!");
			}
		} else {
			log.error("[", modelName, "]attribute of deleteByPrimaryKey in xml is null!!!");
		}
		return true;
	}
}