package com.syscom.fep.web;

import com.syscom.fep.frmcommon.cryptography.Jasypt;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.syscom.fep", "com.syscom.safeaa"})
public class SyscomFepWebApplication extends SpringBootServletInitializer {
    private static final String CLASS_NAME = SyscomFepWebApplication.class.getSimpleName();
    private static LogHelper logger = new LogHelper();

    static {
        Jasypt.loadEncryptorKey(null);
    }

    public static void main(String[] args) {
        try {
            disableWarning();
            SpringApplication.run(SyscomFepWebApplication.class, args);
        } catch (Exception e) {
            if ("org.springframework.boot.devtools.restart.SilentExitExceptionHandler$SilentExitException".equals(e.getClass().getName())) {
                // ignore
            } else {
                logger.exceptionMsg(e, CLASS_NAME, " start failed!!!");
                System.exit(6);
            }
        }
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SyscomFepWebApplication.class);
    }

    private static void disableWarning() {
//		try {
//			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
//			ReflectionUtils.makeAccessible(theUnsafe);
//			Unsafe u = (Unsafe) theUnsafe.get(null);
//			Class<?> cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
//			Field logger = cls.getDeclaredField("logger");
//			u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
//		} catch (Exception e) {}
    }
}
