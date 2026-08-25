package com.syscom.fep.frmcommon.annotation.util;

import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.RegexUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StackTracePointCutUtil {
    private static final LogHelper logger = new LogHelper();
    private static final ThreadLocal<List<String>> threadLocal = new ThreadLocal<>();

    private StackTracePointCutUtil() {}

    public static void setCaller(List<String> callerObjList) {
        threadLocal.set(callerObjList);
    }

    public static void remove() {
        threadLocal.remove();
    }

    public static List<String> getCaller(Object invoker, int stackFrameLevel) {
        List<String> callerObjList = new ArrayList<>();
        List<String> callerObjListInThreadLocal = threadLocal.get();
        if (CollectionUtils.isNotEmpty(callerObjListInThreadLocal)) {
            callerObjList.addAll(callerObjListInThreadLocal);
        }
        StackTraceElement[] elements = new Throwable().getStackTrace();
        if (ArrayUtils.isNotEmpty(elements)) {
            for (int i = 1; i < elements.length; i++) {
                StackTraceElement element = elements[i];
                String className = element.getClassName();
                // 只看fep的程式
                if (!className.startsWith("com.syscom.fep"))
                    continue;
                // 有一些特殊的caller, 後面Class.forName會出異常, 這裡先filter出來
                String callObj = filterSpecialCallObj(className);
                if (StringUtils.isNotBlank(callObj)) {
                    callerObjList.add(callObj);
                    return callerObjList;
                }
                Class<?> clazz;
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    logger.warn(e, e.getMessage());
                    break;
                }
                StackTracePointCut annotation = clazz.getAnnotation(StackTracePointCut.class);
                if (annotation == null) {
                    Class<?> superClazz = clazz.getSuperclass();
                    while (superClazz != null) {
                        annotation = superClazz.getAnnotation(StackTracePointCut.class);
                        if (annotation == null) {
                            superClazz = superClazz.getSuperclass();
                        } else {
                            break;
                        }
                    }
                    if (annotation == null) {
                        continue;
                    }
                }
                if (!callerObjList.contains(annotation.caller())) {
                    callerObjList.add(0, annotation.caller());
                }
            }
            if (!callerObjList.isEmpty()) {
                return callerObjList;
            }
            if (invoker.getClass().getName().startsWith("com.syscom.fep")) {
                String jarPath = invoker.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                try {
                    String warName = RegexUtil.group0("fep-\\S+.war", jarPath);
                    if (StringUtils.isNotBlank(warName)) {
                        return Collections.singletonList(warName);
                    }
                    String jarName = RegexUtil.group0("fep-\\S+.jar", jarPath);
                    if (StringUtils.isNotBlank(warName)) {
                        int index = jarName.lastIndexOf("fep-");
                        if (index > 0) {
                            jarName = StringUtils.substring(jarName, index);
                        }
                        return Collections.singletonList(jarName);
                    }
                } catch (Exception e) {
                    logger.error(e, e.getMessage());
                }
            }
            return Collections.singletonList(elements[stackFrameLevel].getClassName());
        }
        return Collections.emptyList();
    }

    private static String filterSpecialCallObj(String className) {
        // 批次的task程式, 因為採用動態class, 所以Class.forName是找不到的, 所以這裡預設就是task程式的名字
        if (className.startsWith("com.syscom.fep.batch.task")) {
            int beginIndex = className.lastIndexOf(".");
            // 找到是Task程式, 則不再繼續往下找了
            return className.substring(beginIndex + 1);
        }
        return null;
    }
}
