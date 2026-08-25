package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Richard
 */
public class ExceptionUtil {
    public static final String EXCEPTION_OCCUR = "EXCEPTION_OCCUR";

    private ExceptionUtil() {}

    public static Exception createException(Object... messages) {
        return new Exception(StringUtils.join(messages));
    }

    public static Exception createException(Throwable cause, Object... messages) {
        return new Exception(StringUtils.join(messages), cause);
    }

    public static IllegalArgumentException createIllegalArgumentException(Object... messages) {
        return new IllegalArgumentException(StringUtils.join(messages));
    }

    public static ArrayIndexOutOfBoundsException createArrayIndexOutOfBoundsException(Object... messages) {
        return new ArrayIndexOutOfBoundsException(StringUtils.join(messages));
    }

    public static UnsupportedOperationException createUnsupportedOperationException(Object... messages) {
        return new UnsupportedOperationException(StringUtils.join(messages));
    }

    public static FileNotFoundException createFileNotFoundException(Object... messages) {
        return new FileNotFoundException(StringUtils.join(messages));
    }

    public static RuntimeException createRuntimeException(Object... messages) {
        return createRuntimeException(null, messages);
    }

    public static RuntimeException createRuntimeException(Throwable cause, Object... messages) {
        return new RuntimeException(StringUtils.join(messages), cause);
    }

    public static SocketTimeoutException createSocketTimeoutException(Object... messages) {
        return new SocketTimeoutException(StringUtils.join(messages));
    }

    public static NotImplementedException createNotImplementedException(Object... messages) {
        return new NotImplementedException(StringUtils.join(messages));
    }

    public static ClassNotFoundException createClassNotFoundException(Object... messages) {
        return new ClassNotFoundException(StringUtils.join(messages));
    }

    public static SocketException createSocketException(Object... messages) {
        return new SocketException(StringUtils.join(messages));
    }

    public static SQLException createSQLException(Object... messages) {
        return new SQLException(StringUtils.join(messages));
    }

    public static SQLException createSQLException(Throwable cause, Object... messages) {
        return new SQLException(StringUtils.join(messages), cause);
    }

    public static NullPointerException createNullPointException(Object... messages) {
        return new NullPointerException(StringUtils.join(messages));
    }

    public static IOException createIOException(Object... messages) {
        return new IOException(StringUtils.join(messages));
    }

    public static IOException createIOException(Throwable cause, Object... messages) {
        return new IOException(StringUtils.join(messages), cause);
    }

    public static String getStackTrace(Throwable t, boolean... notFormatted) {
        return getStackTrace(t, null, notFormatted);
    }

    public static String getStackTrace(Throwable t, Map<String, String> regexMap, boolean... notFormatted) {
        // 2024-09-29 Richard modified for 【Incorrect Permission Assignment For Critical Resources】
        // try (Writer w = new StringWriter()) {
        Object w = ReflectUtil.instance(StringWriter.class);
        try {
            // 2024-09-29 Richard modified for 【Incorrect Permission Assignment For Critical Resources】
            // PrintWriter pw = new PrintWriter(w);
            // Object pw = ReflectUtil.instance(PrintWriter.class, new Class[] {Writer.class}, new Object[] {w});
            // 2024-10-28 Richard modified for 【Incorrect Permission Assignment For Critical Resources】
            Object pw = ReflectUtil.instance(Class.forName("java.io.PrintWriter"), new Class[] {Class.forName("java.io.Writer")}, new Object[] {w});
            // 2024-09-27 Richard modified for 【Information Exposure Through an Error Message】
            // t.printStackTrace(pw);
            // ReflectUtil.envokeMethod(t, "printStackTrace", new Class[] {PrintWriter.class}, new Object[] {pw});
            // 2024-10-28 Richard modified for 【Incorrect Permission Assignment For Critical Resources】
            ReflectUtil.envokeMethod(t, "printStackTrace", new Class[] {Class.forName("java.io.PrintWriter")}, new Object[] {pw});
            String stackTrace = w.toString();
            stackTrace = StringUtil.replace(stackTrace, regexMap);
            if (ArrayUtils.isNotEmpty(notFormatted) && notFormatted[0]) {
                Pattern p = Pattern.compile("\t|\r|\n");
                Matcher m = p.matcher(stackTrace);
                stackTrace = m.replaceAll(StringUtils.EMPTY);
            }
            return stackTrace;
        } catch (Exception e) {
            return StringUtils.EMPTY;
        } finally {
            if (w != null) {
                try {
                    ReflectUtil.envokeMethod(w, "close");
                } catch (Throwable e) {
                    LogHelper.getAdditionalLogger().error(e, e.getMessage());
                }
            }
        }
    }

    // public static void main(String[] args) {
    //     System.out.println(getStackTrace(new Exception("Error")));
    // }

    public static Throwable find(Throwable t, Predicate<Throwable> predicate) {
        Throwable cause = t;
        while (cause != null) {
            if (predicate.test(cause)) {
                return cause;
            }
            cause = cause.getCause();
        }
        return null;
    }

    /**
     * 透過反射呼叫method後如果有catch到異常, 則判定從異常中取出真正的Throwable
     *
     * @param t
     * @return
     */
    public static Throwable reflectionInvokeExceptionOccur(Throwable t) {
        if ((t instanceof UndeclaredThrowableException)) {
            return t.getCause();
        }
        return t;
    }
}
