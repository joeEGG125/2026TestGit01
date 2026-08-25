package com.syscom.fep.frmcommon.parse;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.math.BigDecimal;

public class StringToFieldAnnotationParser<T> implements Parser<T, String> {
    private final LogHelper logger = new LogHelper();

    private final Class<T> genericType;

    public StringToFieldAnnotationParser(Class<T> genericType) {
        this.genericType = genericType;
    }

    @Override
    public T readIn(String in) throws Exception {
        Class<T> cls = this.genericType;
        T entity = cls.newInstance();
        java.lang.reflect.Field[] fields = cls.getDeclaredFields();
        int offset = 0;
        int length = 0;
        StringBuilder sb = new StringBuilder();
        for (java.lang.reflect.Field field : fields) {
            Field annotation = field.getAnnotation(Field.class);
            sb.setLength(0);
            sb.append(this.genericType.getName()).append("|")
                    .append(StringUtils.rightPad(field.getName(), 20, StringUtils.SPACE)).append("|")
                    .append(StringUtils.leftPad(Integer.toString(offset), 3, StringUtils.SPACE)).append("|");
            if (annotation == null) {
                sb.append(StringUtils.EMPTY).append("|")
                        .append(StringUtils.EMPTY).append("|")
                        .append(StringUtils.EMPTY).append("|")
                        .append(StringUtils.EMPTY).append("|")
                        .append("Ignore substring and set value to Field, annotation Field not define");
                continue;
            }
            try {
                if (annotation.columnVariable()) {
                    length = Integer.parseInt(in.substring(offset, offset + 2));
                    offset += 2;
                } else {
                    length = annotation.length() * 2;
                }
            } catch (Exception e) {
                LogHelper.getAdditionalLogger().warn("readIn", e.getMessage());
            }
            if (length > 0) {
                sb.append(StringUtils.leftPad(Integer.toString(offset + length), 3, StringUtils.SPACE)).append("|")
                        .append(StringUtils.leftPad(Integer.toString(length), 3, StringUtils.SPACE)).append("|");
                if (offset + length <= in.length()) {
                    String substr = in.substring(offset, offset + length);
                    if (annotation.trim()) {
                        substr = substr.trim();
                    }
                    sb.append(substr).append("|");
                    if (BigDecimal.class.isAssignableFrom(field.getType())) {
                        int sublen = substr.length();
                        if (StringUtils.isBlank(EbcdicConverter.fromHex(CCSID.English, substr))) {
                            substr = StringUtils.repeat("F0", (sublen / 2));
                        }
                        BigDecimal bigstr = new BigDecimal(EbcdicConverter.fromHex(CCSID.English, substr));
                        ReflectionUtils.makeAccessible(field);
                        ReflectionUtils.setField(field, entity, bigstr);
                    } else {
                        ReflectionUtils.makeAccessible(field);
                        ReflectionUtils.setField(field, entity, substr);
                        try {
                            if (annotation.chinese()) {
                                sb.append(EbcdicConverter.fromWholeHex(substr));
                            } else {
                                sb.append(EbcdicConverter.fromHex(CCSID.English, substr));
                            }
                        } catch (Exception e) {
                            LogHelper.getAdditionalLogger().warn("readIn", e.getMessage());
                            sb.append(StringUtils.EMPTY);
                        }
                        sb.append("|");
                    }
                    sb.append("Substring and set value to Field successful!!!");
                    logger.debug(sb.toString());
                } else {
                    sb.append(StringUtils.EMPTY).append("|")
                            .append(StringUtils.EMPTY).append("|")
                            .append("Ignore substring and set value to Field, there is no more string to substring!!!");
                    break;
                }
            } else {
                sb.append(StringUtils.EMPTY).append("|")
                        .append(StringUtils.EMPTY).append("|")
                        .append(StringUtils.EMPTY).append("|")
                        .append(StringUtils.EMPTY).append("|")
                        .append("Ignore substring and set value to Field, cause Length < 0, length = [").append(length).append("]");
                continue;
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
