package com.syscom.fep.frmcommon.xs.converter;

import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.frmcommon.xs.entity.XsOriginalNode;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.mapper.Mapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.List;

public class XsDuplicateFieldReflectionConverter extends XsDefaultReflectionConverter {
    public XsDuplicateFieldReflectionConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
        super(mapper, reflectionProvider);
    }

    public XsDuplicateFieldReflectionConverter(Mapper mapper, ReflectionProvider reflectionProvider, Class type) {
        super(mapper, reflectionProvider, type);
    }

    @Override
    protected String getFieldName(Object entity, String originalNodeName, Class fieldDeclaringClass, XsOriginalNode xsOriginalNode) {
        boolean found = false;
        List<Field> fieldList = ReflectUtil.getAllFields(entity);
        if (CollectionUtils.isNotEmpty(fieldList)) {
            // 如果found為true, 表示有多個欄位的註解XStreamAlias, value值是一樣的
            found = fieldList.stream()
                    .filter(t -> t.getAnnotation(XStreamAlias.class) != null && originalNodeName.equals(t.getAnnotation(XStreamAlias.class).value()))
                    .count() > 0;
        }
        if (found) {
            return StringUtils.join(xsOriginalNode.getName(), StringUtils.leftPad(String.valueOf(xsOriginalNode.getCount()), 2, '0'));
        }
        return super.getFieldName(entity, originalNodeName, fieldDeclaringClass, xsOriginalNode);
    }
}
