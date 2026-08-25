package com.syscom.fep.mybatis.generator;

import com.syscom.fep.mybatis.vo.MybatisConstant;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.VisitableElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.*;

/**
 * 自定義替換SQL中的field
 */
public class ReplaceFieldInSQLPluginAdapter extends PluginAdapter {
    private final Map<String, Map<String, String>> replacementMap = new HashMap<String, Map<String, String>>() {
        {
            // FEPTXN
            put(MybatisConstant.TABLE_NAME_FEPTXN, new HashMap<String, String>() {
                {
                    // put("#{feptxnIcmark,jdbcType=CHAR}", "SUBSTR(#{feptxnIcmark,jdbcType=CHAR}, 1, 30)");
                    put("#{feptxnTrk2,jdbcType=VARCHAR}", "SUBSTR(#{feptxnTrk2,jdbcType=VARCHAR}, 1, 50)");
                    put("#{feptxnOrderNo,jdbcType=VARCHAR}", "SUBSTR(#{feptxnOrderNo,jdbcType=VARCHAR}, 1, 16)");
                    put("#{updateTime,jdbcType=TIMESTAMP}", "sysdate");
                }
            });
        }
    };
    private final Map<String, List<String>> removeIfMap = new HashMap<String, List<String>>() {
        {
            // FEPTXN
            put(MybatisConstant.TABLE_NAME_FEPTXN, Arrays.asList(
                    "updateTime != null"
            ));
        }
    };

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        String tableName = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();
        Map<String, String> fieldMap = replacementMap.get(tableName);
        List<String> ifList = removeIfMap.get(tableName);
        // 直接skip不需要處理的Table
        if (fieldMap == null && ifList == null) {
            return true;
        }
        List<VisitableElement> list = new ArrayList<>();
        List<VisitableElement> elementList = element.getElements();
        if (CollectionUtils.isNotEmpty(elementList)) {
            for (VisitableElement child : elementList) {
                if (child instanceof XmlElement) {
                    XmlElement xmlElement = (XmlElement) child;
                    if ("if".equalsIgnoreCase(xmlElement.getName())) {
                        if (ifList != null) {
                            List<Attribute> attrList = new ArrayList<>();
                            List<Attribute> attributes = xmlElement.getAttributes();
                            for (Attribute attribute : attributes) {
                                if ("test".equalsIgnoreCase(attribute.getName()) && ifList.contains(attribute.getValue())) {
                                    continue;
                                }
                                attrList.add(attribute);
                            }
                            xmlElement.getAttributes().clear();
                            // 如果屬性都移除掉了, 則不需要這個XmlElement, 那麼就要把XmlElement下所有的子Element層級往上提一層
                            if (attrList.isEmpty()) {
                                List<VisitableElement> elemList = xmlElement.getElements();
                                if (CollectionUtils.isNotEmpty(elementList)) {
                                    for (VisitableElement elem : elemList) {
                                        if (elem instanceof XmlElement) {
                                            sqlMapInsertElementGenerated((XmlElement) elem, introspectedTable);
                                        } else if (elem instanceof TextElement) {
                                            elem = this.replaceField(elem, fieldMap);
                                        }
                                        list.add(elem);
                                    }
                                }
                                continue;
                            } else {
                                xmlElement.getAttributes().addAll(attrList);
                            }
                        }
                    }
                    sqlMapInsertElementGenerated(xmlElement, introspectedTable);
                } else if (child instanceof TextElement) {
                    child = this.replaceField(child, fieldMap);
                }
                list.add(child);
            }
            element.getElements().clear();
            element.getElements().addAll(list);
        }
        return true;
    }

    private VisitableElement replaceField(VisitableElement elem, Map<String, String> fieldMap) {
        if (fieldMap != null) {
            TextElement textElement = (TextElement) elem;
            String content = textElement.getContent();
            for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
                if (content.contains(entry.getKey())) {
                    elem = new TextElement(StringUtils.replace(content, entry.getKey(), entry.getValue()));
                    break;
                }
            }
        }
        return elem;
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return this.sqlMapInsertElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return this.sqlMapInsertElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return this.sqlMapInsertElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return this.sqlMapInsertElementGenerated(element, introspectedTable);
    }
}
