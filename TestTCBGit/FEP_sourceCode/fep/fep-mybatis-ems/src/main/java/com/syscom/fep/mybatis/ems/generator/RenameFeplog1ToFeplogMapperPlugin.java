package com.syscom.fep.mybatis.ems.generator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.VisitableElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.mybatis.ems.vo.MybatisConstant;
import com.syscom.fep.mybatis.ems.vo.MybatisElement;

public class RenameFeplog1ToFeplogMapperPlugin extends PluginAdapter {
	private LogHelper log = new LogHelper();

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public void initialized(IntrospectedTable introspectedTable) {
		if (MybatisConstant.TABLE_NAME_FEPLOG1.equals(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
			// change mapper name
			String mybatis3JavaMapperType = introspectedTable.getMyBatis3JavaMapperType();
			mybatis3JavaMapperType = StringUtils.replace(mybatis3JavaMapperType, MybatisConstant.ENTITY_NAME_FEPLOG1, MybatisConstant.REPLACE_ENTITY_NAME_FOR_FEPLOG1);
			introspectedTable.setMyBatis3JavaMapperType(mybatis3JavaMapperType);
			// change xml name
			String mybatis3XmlMapperFileName = introspectedTable.getMyBatis3XmlMapperFileName();
			mybatis3XmlMapperFileName = StringUtils.replace(mybatis3XmlMapperFileName, MybatisConstant.ENTITY_NAME_FEPLOG1, MybatisConstant.REPLACE_ENTITY_NAME_FOR_FEPLOG1);
			introspectedTable.setMyBatis3XmlMapperFileName(mybatis3XmlMapperFileName);
			// change module name
			String baseRecordType = introspectedTable.getBaseRecordType();
			baseRecordType = StringUtils.replace(baseRecordType, MybatisConstant.ENTITY_NAME_FEPLOG1, MybatisConstant.REPLACE_ENTITY_NAME_FOR_FEPLOG1);
			introspectedTable.setBaseRecordType(baseRecordType);
		}
	}

	@Override
	public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		if (MybatisConstant.TABLE_NAME_FEPLOG1.equals(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
			// 如果原本只有一個參數, 並且這個參數沒有annotation, 那麼需要給原本的參數加上@Param
			if (method.getParameters().size() == 1) {
				Parameter parameter = method.getParameters().get(0);
				if (CollectionUtils.isEmpty(parameter.getAnnotations())) {
					parameter.addAnnotation(StringUtils.join("@", Param.class.getSimpleName(), "(\"", parameter.getName(), "\")"));
				}
			}
			// 增加@Param("tableNameSuffix") String tableNameSuffix參數
			FullyQualifiedJavaType modelJavaType = new FullyQualifiedJavaType(String.class.getName());
			Parameter parameter = new Parameter(modelJavaType, MybatisConstant.PARAMETER_NAME_TABLENAMESUFFIX);
			parameter.addAnnotation(StringUtils.join("@", Param.class.getSimpleName(), "(\"", MybatisConstant.PARAMETER_NAME_TABLENAMESUFFIX, "\")"));
			method.getParameters().add(0, parameter);
			// 如果原本沒有import Param, 則需要加入
			if (CollectionUtils.isEmpty(interfaze.getImportedTypes()) || interfaze.getImportedTypes().stream().filter(t -> t.getShortName().equals(Param.class.getSimpleName())).count() == 0) {
				interfaze.getImportedTypes().add(new FullyQualifiedJavaType(Param.class.getName()));
			}
		}
		return true;
	}

	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		if (MybatisConstant.TABLE_NAME_FEPLOG1.equals(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
			this.replaceTableNameInSql(document.getRootElement());
			// 增加自定義的<sql id="chooseTableName"/>
			document.getRootElement().getElements().add(ceateChooseTableNameSqlElement());
		}
		return true;
	}

	@Override
	public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		String modelName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
		if (MybatisConstant.ENTITY_NAME_FEPLOG1.equals(modelName)) {
			modelName = MybatisConstant.REPLACE_ENTITY_NAME_FOR_FEPLOG1;
		}
		if (MybatisConstant.TABLE_NAME_FEPLOG1.equals(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName())) {
			List<Attribute> attributeList = element.getAttributes();
			if (CollectionUtils.isNotEmpty(attributeList)) {
				Attribute parameterTypeAttribute = attributeList.stream().filter(x -> x.getName().equals(MybatisElement.ATTRIBUTE_PARAMETERTYPE.getName())).findFirst().orElse(null);
				if (parameterTypeAttribute != null) {
					attributeList.remove(parameterTypeAttribute);
					attributeList.add(new Attribute(parameterTypeAttribute.getName(), "map"));
				} else {
					log.error("[", modelName, "] attribute \"parameterType\" of selectByPrimaryKey in xml is null!!!");
				}
			} else {
				log.error("[", modelName, "]attribute of selectByPrimaryKey in xml is null!!!");
			}
		}
		return true;
	}

	/**
	 * 替換xml檔中, sql中的表名
	 * 
	 * @param root
	 */
	private void replaceTableNameInSql(XmlElement root) {
		if (CollectionUtils.isNotEmpty(root.getElements())) {
			List<VisitableElement> list = new ArrayList<>();
			for (VisitableElement element : root.getElements()) {
				if (element instanceof XmlElement) {
					this.replaceTableNameInSql((XmlElement) element);
				} else if (element instanceof TextElement) {
					TextElement textElement = (TextElement) element;
					String content = textElement.getContent();
					if (content.contains(MybatisConstant.TABLE_NAME_FEPLOG1)) {
						// element = new TextElement(StringUtils.replace(content, MybatisConstant.TABLE_NAME_FEPLOG1, MybatisConstant.REPLACE_TABLE_NAME_FOR_FEPLOG1));
						element = new TextElement(StringUtils.replace(content, MybatisConstant.TABLE_NAME_FEPLOG1, MybatisConstant.REPLACE_TABLE_NAME_FOR_FEPLOG1_INCLUDE_REFID));
					}
				}
				list.add(element);
			}
			root.getElements().clear();
			root.getElements().addAll(list);
		}
	}

	/**
	 * 產出<sql id="chooseTableName"></>
	 *
	 * @return
	 */
	private VisitableElement ceateChooseTableNameSqlElement() {
		XmlElement choose = new XmlElement("choose");
		String tableNameSuffix = StringUtils.EMPTY;
		for (int i = 1; i <= 7; i++) {
			tableNameSuffix = StringUtils.leftPad(String.valueOf(i), 1, '0');
			XmlElement when = new XmlElement("when");
			when.addAttribute(new Attribute("test", StringUtils.join("tableNameSuffix == ", tableNameSuffix)));
			when.addElement(new TextElement(StringUtils.join(MybatisConstant.TABLE_NAME_PREFIX_FEPLOG, tableNameSuffix)));
			choose.addElement(when);
		}
		XmlElement root = new XmlElement("sql");
		root.addAttribute(new Attribute("id", "chooseTableName"));
		root.addElement(new TextElement(
				"<!--\n" +
						"      WARNING - @mbg.generated\n" +
						"      This element is automatically generated by MyBatis Generator, do not modify.\n" +
						"    -->"));
		root.addElement(choose);
		return root;
	}
}
