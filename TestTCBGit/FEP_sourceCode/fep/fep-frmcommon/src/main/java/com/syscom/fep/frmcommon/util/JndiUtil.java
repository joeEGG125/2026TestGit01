package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class JndiUtil {
    private static final LogHelper logger = new LogHelper();

    private JndiUtil() {}

    /**
     * 給JNDI綁定的DataSource設定屬性值
     *
     * @param bean
     * @param properties
     * @return
     */
    public static boolean setPropertyForDataSource(JndiObjectFactoryBean bean, Properties properties) {
        // ((JndiObjectTargetSource) ((JdkDynamicAopProxy) ((Proxy) dataSource).h).advised.targetSource).cachedObject.dsConfig.value.vendorProps.setProperty("progressiveStreaming", "1")
        try {
            DataSource dataSource = (DataSource) bean.getObject();
            if (dataSource instanceof Proxy) {
                Proxy proxy = (Proxy) dataSource;
                InvocationHandler handler = ReflectUtil.getFieldValue(proxy, "h", null);
                if (handler != null) {
                    Object advised = ReflectUtil.getFieldValue(handler, "advised", null);
                    if (advised != null) {
                        Object targetSource = ReflectUtil.getFieldValue(advised, "targetSource", null);
                        if (targetSource != null) {
                            Object cachedObject = ReflectUtil.getFieldValue(targetSource, "cachedObject", null);
                            if (cachedObject != null) {
                                AtomicReference<?> dsConfig = ReflectUtil.getFieldValue(cachedObject, "dsConfig", (AtomicReference<?>) null);
                                if (dsConfig != null) {
                                    Object value = dsConfig.get();
                                    if (value != null) {
                                        Properties vendorProps = ReflectUtil.getFieldValue(value, "vendorProps", null);
                                        if (vendorProps != null) {
                                            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                                                vendorProps.put(entry.getKey(), entry.getValue());
                                                logger.debug("[", bean.getJndiName(), "]put {", entry.getKey(), ":", entry.getValue(), "} to DataSource");
                                            }
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn(e, "[", bean.getJndiName(), "]cannot set property for DataSource, ", e.getMessage());
        }
        return false;
    }
}
