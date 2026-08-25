package com.syscom.fep.mybatis.interceptor;

import com.ibm.db2.jcc.DB2BaseDataSource;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;

@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class ConnectionPropertiesOverwriteInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Connection connection = (Connection) invocation.getArgs()[0];
        // ((ConnectionLogger) ((Proxy) connection).h).connection.connImpl.progressiveStreaming_
        if (connection instanceof Proxy) {
            Proxy proxy = (Proxy) connection;
            InvocationHandler handler = ReflectUtil.getFieldValue(proxy, "h", null);
            if (handler != null) {
                Object conn = ReflectUtil.getFieldValue(handler, "connection", null);
                if (conn != null) {
                    Object connImpl = ReflectUtil.getFieldValue(conn, "connImpl", null);
                    if (connImpl != null) {
                        int progressiveStreaming = ReflectUtil.getFieldValue(connImpl, "progressiveStreaming_", -1);
                        if (DB2BaseDataSource.NO != progressiveStreaming) {
                            ReflectUtil.setFieldValue(connImpl, "progressiveStreaming_", DB2BaseDataSource.NO);
                        }
                    }
                }
            }
        }
        return invocation.proceed();
    }
}
