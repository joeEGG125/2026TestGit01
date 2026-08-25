package com.syscom.fep.base;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;

public class FEPBase extends FEPBaseMethod {
    protected LogData logContext;
    protected Integer ej;
    protected String txRquid;

    public FEPBase() {
        // 2021-10-12 Richard add to avoid NullPointException when using variable as follow, FISC default construction e.g
        this.logContext = new LogData();
        this.ej = 0;
    }

    public LogData getLogContext() {
        return logContext;
    }

    public void setLogContext(LogData logContext) {
        this.logContext = logContext;
    }

    public Integer getEj() {
        return ej;
    }

    public void setEj(Integer ej) {
        this.ej = ej;
    }

    public String getTxRquid() {
        return txRquid;
    }

    public void setTxRquid(String txRquid) {
        this.txRquid = txRquid;
    }

    /**
     * 20221007 Bruce add 動態取得 Tita或Tota屬性的值
     *
     * @param o
     * @param methodName
     * @return
     */
    public String getImsPropertiesValue(Object o, String methodName) {
        if (o == null) {
            return "";
        }
        Class<?> cls = o.getClass();
        // 取出bean裡的所有方法
        java.lang.reflect.Field[] fields = cls.getDeclaredFields();
        String fieldGetName = "";
        Method fieldGetMet = null;
        Class<?>[] params = {};
        try {
            for (java.lang.reflect.Field field : fields) {
                if (methodName.equals(field.getName())) {
                    fieldGetName = "get" + methodName;
                    fieldGetMet = cls.getMethod(fieldGetName, params);
                    return Objects.toString(fieldGetMet.invoke(o), "");
                }
            }
        } catch (Exception e) {
            LogHelperFactory.getTraceLogger().error(e, e.getMessage());
        }
        return "";
    }

    /**
     * 2025/3/17 xingyun add
     * map 轉 Object
     * @param map
     * @param o
     * @return
     * @param <T>
     * @throws Exception
     */
    public <T> Object mapToEntity(Map<String, Object> map, Object o) throws Exception {
        Class<?> cls = o.getClass();
        java.lang.reflect.Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (map.containsKey(field.getName())) {
                field.set(o, map.get(field.getName()));
            }
        }
        return o;
    }
}
