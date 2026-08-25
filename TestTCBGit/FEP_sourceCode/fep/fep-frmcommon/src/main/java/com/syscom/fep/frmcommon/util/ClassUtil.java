package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassUtil {
    private static final LogHelper logger = new LogHelper();

    private ClassUtil() {}

    /**
     * 取得類別位於哪個jar or war檔
     *
     * @param className 類別名稱
     * @return jar檔名稱
     */
    public static String getPackageName(String className) {
        try {
            return getPackageName(Class.forName(className));
        } catch (ClassNotFoundException e) {
            logger.warn(e, e.getMessage());
            return "";
        }
    }

    /**
     * 取得類別位於哪個jar檔
     *
     * @param clazz 類別
     * @return jar檔名稱
     */
    public static String getPackageName(Class<?> clazz) {
        String path = String.valueOf(clazz.getProtectionDomain().getCodeSource().getLocation());
        logger.debug("Path: ", path);
        List<String> nameList = Arrays.stream(path.split("/"))
                .filter(subPath -> subPath.endsWith(".jar!")
                        || subPath.endsWith(".jar")
                        || subPath.endsWith(".war!")
                        || subPath.endsWith(".war"))
                .collect(Collectors.toList());
        logger.debug("nameList: ", nameList);
        if (nameList.isEmpty()) {
            return "";
        }
        String packageName = nameList.get(nameList.size() - 1);
        return packageName.endsWith("!") ? packageName.substring(0, packageName.length() - 1) : packageName;
    }

    /**
     * 根據物件取得對應的Class名稱
     *
     * @param object
     * @return
     */
    public static Class<?> getClass(Object object) {
        Class<?> clazz = object.getClass();
        // if (clazz.getSimpleName().contains("$$EnhancerBySpringCGLIB$$")) {
        if (clazz.getSimpleName().contains("$$SpringCGLIB$$")) {
            return clazz.getSuperclass();
        }
        return clazz;
    }
}