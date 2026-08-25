package com.syscom.fep.mybatis.deslog.generator;

import com.syscom.fep.mybatis.deslog.vo.MybatisConstant;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaElement;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.VisitableElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 移除schema
 *
 * @author Richard
 */
public class RemoveSchemaPluginAdapter extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        String schema = introspectedTable.getTableConfiguration().getSchema();
        if (StringUtils.isNotBlank(schema)) {
            String tableName = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();
            String searchString = StringUtils.join(Arrays.asList(schema, tableName), '.');
            this.removeSchema(document.getRootElement(), tableName, searchString);
            searchString = StringUtils.join(Arrays.asList(schema, MybatisConstant.REPLACE_TABLE_NAME_FOR_DESLOG1_INCLUDE_REFID), ".");
            this.removeSchema(document.getRootElement(), MybatisConstant.REPLACE_TABLE_NAME_FOR_DESLOG1_INCLUDE_REFID, searchString);
        }
        return true;
    }

    /**
     * 如果config檔有設定schema, 則從xml檔中移除掉
     *
     * @param root
     * @param tableName
     * @param searchString
     */
    private void removeSchema(XmlElement root, String tableName, String searchString) {
        if (CollectionUtils.isNotEmpty(root.getElements())) {
            List<VisitableElement> list = new ArrayList<>();
            for (VisitableElement element : root.getElements()) {
                if (element instanceof XmlElement) {
                    this.removeSchema((XmlElement) element, tableName, searchString);
                } else if (element instanceof TextElement) {
                    TextElement textElement = (TextElement) element;
                    String content = textElement.getContent();
                    if (content.contains(searchString)) {
                        element = new TextElement(StringUtils.replace(content, searchString, tableName));
                    }
                }
                list.add(element);
            }
            root.getElements().clear();
            root.getElements().addAll(list);
        }
    }


    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        String schema = introspectedTable.getTableConfiguration().getSchema();
        if (StringUtils.isNotBlank(schema)) {
            String tableName = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();
            String searchString = StringUtils.join(Arrays.asList(schema, tableName), '.');
            // Methods
            replaceJavaDoc(interfaze.getMethods(), tableName, searchString);
        }
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String schema = introspectedTable.getTableConfiguration().getSchema();
        if (StringUtils.isNotBlank(schema)) {
            String tableName = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();
            String searchString = StringUtils.join(Arrays.asList(schema, tableName), '.');
            // Fields
            replaceJavaDoc(topLevelClass.getFields(), tableName, searchString);
            // Methods
            replaceJavaDoc(topLevelClass.getMethods(), tableName, searchString);
        }
        return true;
    }

    private  <T extends JavaElement> void replaceJavaDoc(List<T> list, String tableName, String searchString) {
        if (CollectionUtils.isNotEmpty(list)) {
            List<T> modifiedList = new ArrayList<>();
            for (T t : list) {
                List<String> javaDocLineList = t.getJavaDocLines();
                if (CollectionUtils.isNotEmpty(javaDocLineList)) {
                    List<String> modifiedJavaDocLineList = new ArrayList<>();
                    for (String javaDocLine : javaDocLineList) {
                        if (javaDocLine.contains(searchString)) {
                            modifiedJavaDocLineList.add(StringUtils.replace(javaDocLine, searchString, tableName));
                        } else {
                            modifiedJavaDocLineList.add(javaDocLine);
                        }
                    }
                    t.getJavaDocLines().clear();
                    t.getJavaDocLines().addAll(modifiedJavaDocLineList);
                }
                modifiedList.add(t);
            }
            list.clear();
            list.addAll(modifiedList);
        }
    }
}
