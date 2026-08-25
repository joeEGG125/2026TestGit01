package com.syscom.fep.frmcommon.parse;

import org.springframework.util.ReflectionUtils;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.log.LogHelper;

public class MftStringToFieldAnnotationParser<T> implements Parser<T, String> {
    private LogHelper logger = new LogHelper();

    private final Class<T> genericType;

    public MftStringToFieldAnnotationParser(Class<T> genericType) {
        this.genericType = genericType;
    }

    @Override
    public T readIn(String in) throws Exception {
        Class<T> cls = this.genericType;
        @SuppressWarnings("deprecation")
        T entity = cls.newInstance();
        java.lang.reflect.Field[] fields = cls.getDeclaredFields();
        int offset = 0;
        for (java.lang.reflect.Field field : fields) {
            Field annotation = field.getAnnotation(Field.class);
            if (annotation == null) {
                continue;
            }
            int length = annotation.length(); // 和StringToFieldAnnotationParser差別
            if (length > 0) {
                if (offset + length <= in.length()) {
                    String substr = in.substring(offset, offset + length);
                    if (annotation.trim()) {
                        substr = substr.trim();
                    }
                    ReflectionUtils.makeAccessible(field);
                    ReflectionUtils.setField(field, entity, substr);
                    logger.debug(
                            "set hex value = [", substr, "]",
                            // (annotation.toAscii() ? StringUtils.join("(ascii value = [", StringUtil.fromHex(substr), "])") : StringUtils.EMPTY),
                            " at offset = [", offset, "] with length = [", length, "] for field = [", field.getName(), "] of [", this.genericType.getName(), "]");
                } else {
                    logger.warn("ignore to set string value at offset = [", offset, "] with length = [", length, "] for field = [", field.getName(), "] of [", this.genericType.getName(), "]");
                    break;
                }
            }
            offset += length;
        }
        return entity;
    }

    @Override
    public String writeOut(T out) throws Exception {
        return null;
    }
}
