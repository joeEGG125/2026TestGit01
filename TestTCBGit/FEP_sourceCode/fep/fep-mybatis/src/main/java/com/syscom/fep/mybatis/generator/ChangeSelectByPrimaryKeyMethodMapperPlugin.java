package com.syscom.fep.mybatis.generator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;

import java.util.List;

/**
 * 修改SelectByPrimaryKey方法, 如果只有一個參數, 則加入@Param註解, 主要用於AuditTrailInterceptor
 */
public class ChangeSelectByPrimaryKeyMethodMapperPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if (method.getParameters().size() == 1) {
            Parameter parameter = method.getParameters().get(0);
            if (CollectionUtils.isEmpty(parameter.getAnnotations())) {
                parameter.addAnnotation(StringUtils.join("@", Param.class.getSimpleName(), "(\"", parameter.getName(), "\")"));
            }
            // 如果原本沒有import Param, 則需要加入
            if (CollectionUtils.isEmpty(interfaze.getImportedTypes()) || interfaze.getImportedTypes().stream().noneMatch(t -> t.getShortName().equals(Param.class.getSimpleName()))) {
                interfaze.getImportedTypes().add(new FullyQualifiedJavaType(Param.class.getName()));
            }
        }
        return super.clientSelectByPrimaryKeyMethodGenerated(method, interfaze, introspectedTable);
    }
}
