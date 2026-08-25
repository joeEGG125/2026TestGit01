package com.syscom.fep.frmcommon.log;

import com.syscom.fep.frmcommon.delegate.ActionListener3;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class LogHelper {
    private final String loggerName;
    private final AtomicInteger stackFrameLevel = new AtomicInteger(0);
    private static final LogHelper ADDITIONALLOGGER = new LogHelper("AdditionalLogger");

    public LogHelper() {
        this((String) null);
    }

    public LogHelper(Class<?> clazz) {
        this(clazz.getName());
    }

    public LogHelper(String loggerName) {
        this.loggerName = loggerName;
    }

    /**
     * 這個logger在配置檔中設定level為OFF, 主要是for Insufficient Logging of Exceptions的弱掃修正而建立
     *
     * @return
     */
    public static LogHelper getAdditionalLogger() {
        return ADDITIONALLOGGER;
    }

    protected Logger getLogger() {
        //  System.out.println("===============[" + Thread.currentThread().getName() + "]logEnable is " + LogMDC.get("logEnable"));
        if (StringUtils.isBlank(this.loggerName)) {
            StackTraceElement[] stack = (new Throwable()).getStackTrace();
            return LoggerFactory.getLogger(stack[this.stackFrameLevel.get() + 3].getClassName());
        } else {
            return LoggerFactory.getLogger(this.loggerName);
        }
    }

    public String debug(Object... messages) {
        // String message = this.logForging(StringUtils.join(messages));
        // this.getLogger().debug(message);
        // ReflectUtil.envokeMethod(this.getLogger(), "debug", new Class[] {String.class}, new Object[] {message}); // 2025-02-25 Richard modified for 【Log Forging】
        // return message;
        // 2025-05-20 Richard modified
        return this.logForging((l, th, m) -> l.debug(m), null, messages);
    }

    public String info(Object... messages) {
        // String message = this.logForging(StringUtils.join(messages));
        // this.getLogger().info(message);
        // ReflectUtil.envokeMethod(this.getLogger(), "info", new Class[] {String.class}, new Object[] {message}); // 2025-02-25 Richard modified for 【Log Forging】
        // return message;
        // 2025-05-20 Richard modified
        return this.logForging((l, th, m) -> l.info(m), null, messages);
    }

    public String warn(Object... messages) {
        String message = this.logForging(StringUtils.join(messages));
        // this.getLogger().warn(message);
        // ReflectUtil.envokeMethod(this.getLogger(), "warn", new Class[] {String.class}, new Object[] {message}); // 2025-02-25 Richard modified for 【Log Forging】
        // return message;
        // 2025-05-20 Richard modified
        return this.logForging((l, th, m) -> l.warn(m), null, messages);
    }

    public String warn(Throwable t, Object... messages) {
        // String message = this.logForging(StringUtils.join(messages));
        // this.getLogger().warn(message, t);
        // ReflectUtil.envokeMethod(this.getLogger(), "warn", new Class[] {String.class, Throwable.class}, new Object[] {message, t}); // 2025-02-25 Richard modified for 【Log Forging】
        // return message;
        // 2025-05-20 Richard modified
        return this.logForging((l, th, m) -> l.warn(m, th), t, messages);
    }

    public String trace(Object... messages) {
        // String message = this.logForging(StringUtils.join(messages));
        // this.getLogger().trace(message);
        // ReflectUtil.envokeMethod(this.getLogger(), "trace", new Class[] {String.class}, new Object[] {message}); // 2025-02-25 Richard modified for 【Log Forging】
        // return message;
        // 2025-05-20 Richard modified
        return this.logForging((l, th, m) -> l.trace(m), null, messages);
    }

    public String error(Object... messages) {
        // String message = this.logForging(StringUtils.join(messages));
        // this.getLogger().error(message);
        // ReflectUtil.envokeMethod(this.getLogger(), "error", new Class[] {String.class}, new Object[] {message}); // 2025-02-25 Richard modified for 【Log Forging】
        // return message;
        // 2025-05-20 Richard modified
        return this.logForging((l, th, m) -> l.error(m), null, messages);
    }

    public String error(Throwable t, Object... messages) {
        // String message = this.logForging(StringUtils.join(messages));
        // this.getLogger().error(message, t);
        // ReflectUtil.envokeMethod(this.getLogger(), "error", new Class[] {String.class, Throwable.class}, new Object[] {message, t}); // 2025-02-25 Richard modified for 【Log Forging】
        // return message;
        // 2025-05-20 Richard modified
        return this.logForging((l, th, m) -> l.error(m, th), t, messages);
    }

    public String exceptionMsg(Throwable t, Object... messages) {
        // String message = this.logForging(StringUtils.join(messages));
        // this.getLogger().error(message, t);
        // ReflectUtil.envokeMethod(this.getLogger(), "error", new Class[] {String.class, Throwable.class}, new Object[] {message, t}); // 2025-02-25 Richard modified for 【Log Forging】
        // return message;
        // 2025-05-20 Richard modified
        return this.error(t, messages);
    }

    public int getStackFrameLevel() {
        return stackFrameLevel.getAndSet(0);
    }

    public void setStackFrameLevel(int stackFrameLevel) {
        this.stackFrameLevel.set(stackFrameLevel);
    }

    public void accumulateStackFrameLevel() {
        this.stackFrameLevel.incrementAndGet();
    }

    /**
     * Log Forging漏洞校驗
     *
     * @param logs
     * @return
     */
    public static String logForging(String logs) {
        // if (StringUtils.isBlank(logs)) {
        //     return logs;
        // }
        // String normalize = Normalizer.normalize(logs, Normalizer.Form.NFKC);
        // for (String str : FORGING_LIST) {
        //     normalize = StringUtils.replace(normalize, str, "");
        // }
        // for (LogRepStr logRepStr : LogRepStr.values()) {
        //     normalize = StringUtils.replace(normalize, logRepStr.getReplace(), logRepStr.getReal());
        // }
        // return normalize;
        // return StripXssLogForgingUtils.stripXssLogForging(logs); // 2025-02-25 Richard modified for 【Log Forging】
        return logs;
    }

    private String logForging(ActionListener3<Logger, Throwable, String> listener, Throwable t, Object... messages) {
        String message = StringUtils.join(messages);
        String logForging = logForging(message);
        listener.actionPerformed(this.getLogger(), t, logForging);
        return message;
    }

    public boolean isDebugEnabled() {
        return this.getLogger().isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return this.getLogger().isErrorEnabled();
    }

    public boolean isInfoEnabled() {
        return this.getLogger().isInfoEnabled();
    }

    public boolean isTraceEnabled() {
        return this.getLogger().isTraceEnabled();
    }

    public boolean isWarnEnabled() {
        return this.getLogger().isWarnEnabled();
    }

    public static String deb(LogHelper logger, Object... messages) {
        return logForging(logger, (l, th, m) -> l.debug(m), null, messages);
    }

    public static String inf(LogHelper logger, Object... messages) {
        return logForging(logger, (l, th, m) -> l.info(m), null, messages);
    }

    public static String war(LogHelper logger, Object... messages) {
        return logForging(logger, (l, th, m) -> l.warn(m), null, messages);
    }

    public static String war(LogHelper logger, Throwable t, Object... messages) {
        return logForging(logger, (l, th, m) -> l.warn(m, th), t, messages);
    }

    public static String tra(LogHelper logger, Object... messages) {
        return logForging(logger, (l, th, m) -> l.trace(m), null, messages);
    }

    public static String err(LogHelper logger, Object... messages) {
        return logForging(logger, (l, th, m) -> l.error(m), null, messages);
    }

    public static String err(LogHelper logger, Throwable t, Object... messages) {
        return logForging(logger, (l, th, m) -> l.error(m, th), t, messages);
    }

    public static String exec(LogHelper logger, Throwable t, Object... messages) {
        return err(logger, t, messages);
    }

    private static String logForging(LogHelper logger, ActionListener3<Logger, Throwable, String> listener, Throwable t, Object... messages) {
        return logger.logForging(listener, t, messages);
    }
}
