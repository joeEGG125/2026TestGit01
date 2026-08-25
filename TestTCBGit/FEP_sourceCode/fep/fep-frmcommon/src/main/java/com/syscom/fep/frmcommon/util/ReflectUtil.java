package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.esapi.ESAPIUtil;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ReflectionUtils;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ReflectUtil {
    private static final LogHelper logger = new LogHelper();

    private ReflectUtil() {}

    public static <T> T getFieldValue(Object entity, String fieldName, T defaultValue) {
        return getFieldValue(entity, fieldName, defaultValue, false);
    }

    public static <T> T getFieldValue(Object entity, String fieldName, T defaultValue, boolean throwException) {
        try {
            if (entity != null) {
                Field field = ReflectionUtils.findField(entity.getClass(), fieldName);
                if (field != null) {
                    return getFieldValue(entity, field, defaultValue);
                } else {
                    String errorMessage = StringUtils.join("Cannot find field \"", fieldName, "\" in class \"", entity.getClass().getName(), "\"");
                    if (throwException)
                        throw ExceptionUtil.createRuntimeException(errorMessage);
                    else logger.warn(errorMessage);
                }
            }
        } catch (Throwable e) {
            if (throwException)
                throw ExceptionUtil.createRuntimeException(ExceptionUtil.reflectionInvokeExceptionOccur(e), e.getMessage());
            else logger.warn(e, e.getMessage());
        }
        return defaultValue;
    }

    public static <T> void setFieldValue(Object entity, String fieldName, T fieldValue) {
        setFieldValue(entity, fieldName, fieldValue, false);
    }

    public static <T> void setFieldValue(Object entity, String fieldName, T fieldValue, boolean throwException) {
        try {
            if (entity != null) {
                Field field = ReflectionUtils.findField(entity.getClass(), fieldName);
                if (field != null) {
                    setFieldValue(entity, field, fieldValue);
                } else {
                    String errorMessage = StringUtils.join("Cannot find field \"", fieldName, "\" in class \"", entity.getClass().getName(), "\"");
                    if (throwException)
                        throw ExceptionUtil.createRuntimeException(errorMessage);
                    else logger.warn(errorMessage);
                }
            }
        } catch (Throwable e) {
            if (throwException)
                throw ExceptionUtil.createRuntimeException(ExceptionUtil.reflectionInvokeExceptionOccur(e), e.getMessage());
            else logger.warn(e, e.getMessage());
        }
    }

    public static <T> T getFieldValue(Object entity, Field field, T defaultValue) {
        return getFieldValue(entity, field, defaultValue, false);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object entity, Field field, T defaultValue, boolean throwException) {
        try {
            if (entity != null && field != null) {
                ReflectionUtils.makeAccessible(field);
                return (T) ReflectionUtils.getField(field, entity);
            }
        } catch (Throwable e) {
            if (throwException)
                throw ExceptionUtil.createRuntimeException(ExceptionUtil.reflectionInvokeExceptionOccur(e), e.getMessage());
            else logger.warn(e, e.getMessage());
        }
        return defaultValue;
    }

    public static <T> void setFieldValue(Object entity, Field field, T fieldValue) {
        setFieldValue(entity, field, fieldValue, false);
    }

    public static <T> void setFieldValue(Object entity, Field field, T fieldValue, boolean throwException) {
        try {
            if (entity != null && field != null) {
                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field, entity, fieldValue);
            }
        } catch (Throwable e) {
            if (throwException)
                throw ExceptionUtil.createRuntimeException(ExceptionUtil.reflectionInvokeExceptionOccur(e), e.getMessage());
            else logger.warn(e, e.getMessage());
        }
    }

    public static <T> T envokeMethod(Object entity, String methodName, T defaultValue) {
        return envokeMethod(entity, methodName, defaultValue, false);
    }

    public static <T> T envokeMethod(Object entity, String methodName, Class<?>[] argsCls, Object[] args, T defaultValue) {
        return envokeMethod(entity, methodName, argsCls, args, defaultValue, false);
    }

    public static <T> T envokeMethod(Object entity, String methodName, T defaultValue, boolean throwException) {
        return envokeMethod(entity, methodName, null, null, defaultValue, throwException);
    }

    @SuppressWarnings("unchecked")
    public static <T> T envokeMethod(Object entity, String methodName, Class<?>[] argsCls, Object[] args, T defaultValue, boolean throwException) {
        try {
            if (entity != null) {
                Method method = ReflectionUtils.findMethod(entity.getClass(), methodName, argsCls);
                if (method != null) {
                    ReflectionUtils.makeAccessible(method);
                    return (T) ReflectionUtils.invokeMethod(method, entity, args);
                } else {
                    String errorMessage = StringUtils.join("Cannot find method \"", methodName, "\" in class \"", entity.getClass().getName(), "\"");
                    if (throwException)
                        throw ExceptionUtil.createRuntimeException(errorMessage);
                    else logger.warn(errorMessage);
                }
            }
        } catch (Throwable e) {
            if (throwException)
                throw ExceptionUtil.createRuntimeException(ExceptionUtil.reflectionInvokeExceptionOccur(e), e.getMessage());
            else logger.warn(e, e.getMessage());
        }
        return defaultValue;
    }

    public static void envokeMethod(Object entity, String methodName) {
        envokeMethod(entity, methodName, false);
    }

    public static void envokeMethod(Object entity, String methodName, Class<?>[] argsCls, Object[] args) {
        envokeMethod(entity, methodName, argsCls, args, false);
    }

    public static void envokeMethod(Object entity, String methodName, boolean throwException) {
        envokeMethod(entity, methodName, null, null, throwException);
    }

    public static void envokeMethod(Object entity, String methodName, Class<?>[] argsCls, Object[] args, boolean throwException) {
        try {
            if (entity != null) {
                Method method = ReflectionUtils.findMethod(entity.getClass(), methodName, argsCls);
                if (method != null) {
                    ReflectionUtils.makeAccessible(method);
                    ReflectionUtils.invokeMethod(method, entity, args);
                } else {
                    String errorMessage = StringUtils.join("Cannot find method \"", methodName, "\" in class \"", entity.getClass().getName(), "\"");
                    if (throwException)
                        throw ExceptionUtil.createRuntimeException(errorMessage);
                    else logger.warn(errorMessage);
                }
            }
        } catch (Throwable e) {
            if (throwException)
                throw ExceptionUtil.createRuntimeException(ExceptionUtil.reflectionInvokeExceptionOccur(e), e.getMessage());
            else logger.warn(e, e.getMessage());
        }
    }

    public static <T> T envokeStaticMethod(Class<?> clazz, String methodName, T defaultValue) {
        return envokeStaticMethod(clazz, methodName, defaultValue, true);
    }

    public static <T> T envokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] argsCls, Object[] args, T defaultValue) {
        return envokeStaticMethod(clazz, methodName, argsCls, args, defaultValue, true);
    }

    public static <T> T envokeStaticMethod(Class<?> clazz, String methodName, T defaultValue, boolean printException) {
        return envokeStaticMethod(clazz, methodName, defaultValue, printException, false);
    }

    public static <T> T envokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] argsCls, Object[] args, T defaultValue, boolean printException) {
        return envokeStaticMethod(clazz, methodName, argsCls, args, defaultValue, printException, false);
    }

    public static <T> T envokeStaticMethod(Class<?> clazz, String methodName, T defaultValue, boolean printException, boolean throwException) {
        return envokeStaticMethod(clazz, methodName, null, null, defaultValue, printException, throwException);
    }

    @SuppressWarnings("unchecked")
    public static <T> T envokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] argsCls, Object[] args, T defaultValue, boolean printException, boolean throwException) {
        try {
            if (clazz != null) {
                Method method = ReflectionUtils.findMethod(clazz, methodName, argsCls);
                if (method != null) {
                    ReflectionUtils.makeAccessible(method);
                    return (T) ReflectionUtils.invokeMethod(method, null, args);
                } else {
                    String errorMessage = StringUtils.join("Cannot find method \"", methodName, "\" in class \"", clazz.getName(), "\"");
                    if (printException)
                        logger.warn(errorMessage);
                    if (throwException)
                        throw ExceptionUtil.createRuntimeException(errorMessage);
                    else if (!printException)
                        logger.warn(errorMessage);
                }
            }
        } catch (Throwable e) {
            if (printException)
                logger.warn(e, e.getMessage());
            if (throwException)
                throw ExceptionUtil.createRuntimeException(ExceptionUtil.reflectionInvokeExceptionOccur(e), e.getMessage());
            else if (!printException)
                logger.warn(e, e.getMessage());
        }
        return defaultValue;
    }

    public static void envokeStaticMethod(Class<?> clazz, String methodName) {
        envokeStaticMethod(clazz, methodName, false);
    }

    public static void envokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] argsCls, Object[] args) {
        envokeStaticMethod(clazz, methodName, argsCls, args, false);
    }

    public static void envokeStaticMethod(Class<?> clazz, String methodName, boolean throwException) {
        envokeStaticMethod(clazz, methodName, null, null, throwException);
    }

    public static void envokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] argsCls, Object[] args, boolean throwException) {
        try {
            if (clazz != null) {
                Method method = ReflectionUtils.findMethod(clazz, methodName, argsCls);
                if (method != null) {
                    ReflectionUtils.makeAccessible(method);
                    ReflectionUtils.invokeMethod(method, null, args);
                } else {
                    String errorMessage = StringUtils.join("Cannot find method \"", methodName, "\" in class \"", clazz.getName(), "\"");
                    if (throwException)
                        throw ExceptionUtil.createRuntimeException(errorMessage);
                    else logger.warn(errorMessage);
                }
            }
        } catch (Throwable e) {
            if (throwException)
                throw ExceptionUtil.createRuntimeException(ExceptionUtil.reflectionInvokeExceptionOccur(e), e.getMessage());
            else logger.warn(e, e.getMessage());
        }
    }

    public static List<Field> getAllFields(Object entity) {
        return getAllFields(entity.getClass());
    }

    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(0, Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fieldList;
    }

    /**
     * 動態載入Jar檔中的某個Class程式
     *
     * @param jarPath
     * @param className
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T dynamicLoadClass(String jarPath, String className) throws Exception {
        // 先列出jar檔中所有的class
        Set<String> classNameSet = listClassname(jarPath);
        if (CollectionUtils.isEmpty(classNameSet)) {
            throw ExceptionUtil.createClassNotFoundException("Cannot find className = [", className, "], cause [", jarPath, "] is empty!!!");
        }
        logger.debug("try to dynamic load class [", className, "] in file [", jarPath, "]...");
        Object file = ESAPIUtil.toFile(CleanPathUtil.cleanString(jarPath));
        if (!((File) file).exists() || ((File) file).isDirectory()) {
            throw ExceptionUtil.createFileNotFoundException("Cannot find className = [", className, "], cause [", jarPath, "] is not exist, or it is directory!!!");
        }
        URLClassLoader classLoader = null;
        try {
            URL url = ((File) file).toURI().toURL();
            classLoader = new URLClassLoader(new URL[] {url}, ReflectUtil.class.getClassLoader());
            // 注意這裡要將jar檔中所有的class檔全部載入到ClassLoader中
            for (String clazzName : classNameSet) {
                if (clazzName.equals(className)) {
                    continue;
                }
                // 2025-02-24 Richard modified for [Unsafe Reflection]
                // 2025-02-25 Richard modified for [Download of Code Without Integrity Check]
                // classLoader.loadClass(clazzName);
                loadClazz(classLoader, clazzName, true);
                logger.debug("success dynamic load class = [", clazzName, "] in file [", jarPath, "]");
            }
            // 2025-02-24 Richard modified for [Unsafe Reflection]
            // 2025-02-25 Richard modified for [Download of Code Without Integrity Check]
            // @SuppressWarnings("unchecked")
            // Class<T> clazz = (Class<T>) classLoader.loadClass(className);
            Class<T> clazz = loadClazz(classLoader, className, true);
            logger.debug("success dynamic load class [", className, "] in file [", jarPath, "]");
            return instance(clazz, true);
        } catch (Throwable e) {
            logger.error(e, "Cannot load [", className, "] in file [", jarPath, "]!!!");
            if (e instanceof Error) {
                throw ExceptionUtil.createException(e, e.getMessage());
            }
            throw e;
        } finally {
            IOUtils.closeQuietly(classLoader);
        }
    }

    /**
     * 列舉jar檔中所有的classname
     *
     * @param jarPath
     * @return
     * @throws Exception
     */
    public static Set<String> listClassname(String jarPath) throws Exception {
        return listClassname(jarPath, null);
    }

    /**
     * 依據predicateForClassname列舉jar檔中所有的classname
     *
     * @param jarPath
     * @param predicateForClassname
     * @return
     * @throws Exception
     */
    public static Set<String> listClassname(String jarPath, Predicate<String> predicateForClassname) throws Exception {
        Object file = ESAPIUtil.toFile(CleanPathUtil.cleanString(jarPath));
        if (!((File) file).exists() || ((File) file).isDirectory()) {
            throw ExceptionUtil.createFileNotFoundException("Cannot list className, cause [", jarPath, "] is not exist, or it is directory!!!");
        }
        logger.debug("try to list className in file [", jarPath, "]...");
        Set<String> classNames = new HashSet<>();
        try (JarFile jarFile = new JarFile((File) file)) {
            // 2025-07-10 Richard modified start for [Unchecked Input for Loop Condition]
            // Enumeration<JarEntry> e = jarFile.entries();
            // while (e.hasMoreElements()) {
            //     JarEntry jarEntry = e.nextElement();
            RefBase<Enumeration<JarEntry>> ref = new RefBase<>(null);
            RefBase.set(ref, jarFile, JarFile::entries);
            while (ref.get().hasMoreElements()) {
                JarEntry jarEntry = ref.get().nextElement();
                // 2025-07-10 Richard modified end for [Unchecked Input for Loop Condition]
                if (jarEntry.getName().toLowerCase().endsWith(".class")) {
                    String className = StringUtils.replace(FilenameUtils.removeExtension(jarEntry.getName()), "/", ".");
                    if (predicateForClassname == null || predicateForClassname.test(className))
                        classNames.add(className);
                }
            }
            logger.debug("success list className in file [", jarPath, "]");
            return classNames;
        } catch (Throwable e) {
            logger.error(e, "Cannot list className in file [", jarPath, "]!!!");
            if (e instanceof Error) {
                throw ExceptionUtil.createException(e, e.getMessage());
            }
            throw e;
        }
    }

    /**
     * 獲取指定package下所有的類名
     *
     * @param loader
     * @param packagePattern
     * @return
     * @throws IOException
     */
    public static List<String> listClassname(ResourceLoader loader, String packagePattern) throws IOException {
        List<String> list = new ArrayList<>();
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(loader);
        MetadataReaderFactory factory = new CachingMetadataReaderFactory(loader);
        Resource[] resources = resolver.getResources(StringUtils.join("classpath*:", packagePattern));
        if (ArrayUtils.isNotEmpty(resources)) {
            for (Resource resource : resources) {
                MetadataReader reader = factory.getMetadataReader(resource);
                String className = reader.getClassMetadata().getClassName();
                list.add(className);
            }
        }
        return list;
    }

    /**
     * Get Annotation
     *
     * @param clazz
     * @param annotationClazz
     * @param <A>
     * @return
     */
    public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationClazz) {
        return getAnnotation(clazz, annotationClazz, false);
    }

    public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationClazz, boolean throwException) {
        A annotation = null;
        try {
            annotation = clazz.getAnnotation(annotationClazz);
        } catch (Throwable t) {
            if (throwException)
                throw ExceptionUtil.createRuntimeException(t, t.getMessage());
            else logger.warn(t, t.getMessage());
        }
        return annotation;
    }

    /**
     * 根據clazz實例化
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T instance(Class<T> clazz) {
        return instance(clazz, false);
    }

    /**
     * 根據clazz實例化
     *
     * @param clazz
     * @param throwException
     * @param <T>
     * @return
     */
    public static <T> T instance(Class<T> clazz, boolean throwException) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            if (throwException)
                throw ExceptionUtil.createRuntimeException(e, e.getMessage());
            else logger.warn(e, e.getMessage());
        }
        return null;
    }

    /**
     * 根據clazz實例化
     *
     * @param clazz
     * @param parameterTypes
     * @param initargs
     * @param <T>
     * @return
     */
    public static <T> T instance(Class<T> clazz, Class<?>[] parameterTypes, Object[] initargs) {
        return instance(clazz, parameterTypes, initargs, false);
    }

    /**
     * 根據clazz實例化
     *
     * @param clazz
     * @param parameterTypes
     * @param initargs
     * @param <T>
     * @return
     */
    public static <T> T instance(Class<T> clazz, Class<?>[] parameterTypes, Object[] initargs, boolean throwException) {
        try {
            return clazz.getConstructor(parameterTypes).newInstance(initargs);
        } catch (Exception e) {
            if (throwException)
                throw ExceptionUtil.createRuntimeException(e, e.getMessage());
            else logger.warn(e, e.getMessage());
        }
        return null;
    }

    /**
     * 轉換成Class
     *
     * @param clazzName
     * @param <T>
     * @return
     */
    public static <T> Class<T> toClazz(String clazzName) {
        return toClazz(clazzName, true);
    }

    /**
     * 轉換成Class
     *
     * @param clazzName
     * @param printException
     * @param <T>
     * @return
     */
    public static <T> Class<T> toClazz(String clazzName, boolean printException) {
        return toClazz(clazzName, printException, false);
    }

    /**
     * 轉換成Class
     *
     * @param clazzName
     * @param printException
     * @param throwException
     * @param <T>
     * @return
     */
    public static <T> Class<T> toClazz(String clazzName, boolean printException, boolean throwException) {
        return ReflectUtil.envokeStaticMethod(Class.class, "forName", new Class[] {String.class}, new Object[] {clazzName}, null, printException, throwException);
    }

    /**
     * 載入Class
     *
     * @param classLoader
     * @param clazzName
     * @param <T>
     * @return
     */
    public static <T> Class<T> loadClazz(ClassLoader classLoader, String clazzName) {
        return loadClazz(classLoader, clazzName, false);
    }

    /**
     * 載入Class
     *
     * @param classLoader
     * @param clazzName
     * @param throwException
     * @param <T>
     * @return
     */
    public static <T> Class<T> loadClazz(ClassLoader classLoader, String clazzName, boolean throwException) {
        return ReflectUtil.envokeMethod(classLoader, "loadClass", new Class<?>[] {String.class}, new Object[] {clazzName}, null, throwException);
    }

    /**
     * 讀取文件將每一行存入List
     *
     * @param file
     * @param charset
     * @return
     * @throws IOException
     */
    public static List<String> readLines(Object file, Charset charset) throws IOException {
        try (FileInputStream fis = FileUtils.openInputStream((File) file)) {
            return ReflectUtil.envokeStaticMethod(IOUtils.class, "readLines", new Class[] {InputStream.class, Charset.class}, new Object[] {fis, charset == null ? StandardCharsets.UTF_8 : charset}, null, true, true);
        }
    }

    /**
     * 讀取Reader將每一行存入List
     *
     * @param reader
     * @return
     * @throws IOException
     */
    public static List<String> readLines(Reader reader) throws IOException {
        return ReflectUtil.envokeStaticMethod(IOUtils.class, "readLines", new Class[] {Reader.class}, new Object[] {reader}, null, true, true);
    }

//    public static void main(String[] args) {
//        try(Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\Richard\\Desktop\\Trigger&Sequence.sql"), StandardCharsets.UTF_8))) {
//            List<String> lines = ReflectUtil.readLines(reader);
//            for (String line : lines) {
//                System.out.println(line);
//            }
//        } catch (IOException e) {
//            logger.error(e, e.getMessage());
//        }
//    }
}