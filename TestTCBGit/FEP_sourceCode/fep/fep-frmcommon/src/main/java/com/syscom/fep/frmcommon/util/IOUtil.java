package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.esapi.ESAPIUtil;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class IOUtil {
    private static final LogHelper logger = new LogHelper();

    private IOUtil() {}

    public static InputStream openInputStream(String path, boolean... log) throws Exception {
        boolean logEnable = !ArrayUtils.isNotEmpty(log) || log[0];
        InputStream in = null;
        if (StringUtils.isNotBlank(path)) {
            ClassPathResource classPathResource = new ClassPathResource(path);
            if (logEnable) logger.info("try to load in classpath = [", classPathResource.getPath(), "]...");
            try {
                in = classPathResource.getInputStream();
                if (logEnable) logger.info("load in classpath = [", classPathResource.getPath(), "] successful");
            } catch (Exception e) {
                if (logEnable) logger.warn("load in classpath = [", classPathResource.getPath(), "] failed with exception occur, ", e.getMessage());
            }
            if (in == null) {
                Object file = ESAPIUtil.toFile(CleanPathUtil.cleanString(path));
                if (logEnable) logger.info("try to load out file system = [", ((File) file).getAbsolutePath(), "]...");
                try {
                    in = FileUtils.openInputStream((File) file);
                    if (logEnable) logger.info("load out file system = [", ((File) file).getAbsolutePath(), "] successful");
                } catch (Exception e) {
                    if (logEnable) logger.error(e, "load out file system = [", ((File) file).getAbsolutePath(), "] failed with exception occur, ", e.getMessage());
                    throw e;
                }
            }
        }
        if (in == null) {
            if (logEnable) logger.error("cannot load path = [", path, "]");
            throw ExceptionUtil.createException("cannot load path = [", path, "]");
        }
        return in;
    }

    public static File openFile(String path, boolean... log) throws Exception {
        boolean logEnable = !ArrayUtils.isNotEmpty(log) || log[0];
        if (StringUtils.isNotBlank(path)) {
            ClassPathResource classPathResource = new ClassPathResource(path);
            if (classPathResource.exists()) {
                if (logEnable) logger.info("find file in classpath system = [", classPathResource.getPath(), "]...");
                try {
                    return classPathResource.getFile();
                } catch (FileNotFoundException e) {
                    if (logEnable) logger.warn("find file in classpath system = [", classPathResource.getPath(), "] failed with exception occur, ", e.getMessage());
                }
            }
            File file = ESAPIUtil.toFile(CleanPathUtil.cleanString(path));
            if (logEnable) logger.info("find file out file system = [", file.getAbsolutePath(), "]...");
            return file;
        }
        return null;
    }

    /**
     * 讀取檔案將每一行存入List
     *
     * @param file
     * @param charset
     * @return
     * @throws Exception
     */
    public static List<String> readLines(Object file, Charset charset) throws Exception {
        try (FileInputStream fis = FileUtils.openInputStream((File) file)) {
            return ReflectUtil.envokeStaticMethod(IOUtils.class, "readLines", new Class[] {InputStream.class, Charset.class}, new Object[] {fis, charset == null ? StandardCharsets.UTF_8 : charset}, null, true, true);
        }
    }

    /**
     * 讀取檔案將每一行存入List
     *
     * @param reader
     * @return
     * @throws Exception
     */
    public static List<String> readLines(Reader reader) throws Exception {
        return ReflectUtil.envokeStaticMethod(IOUtils.class, "readLines", new Class[] {Reader.class}, new Object[] {reader}, null, true, true);
    }

    /**
     * 寫入bytes
     *
     * @param os
     * @param bytes
     * @throws Exception
     */
    public static void write(OutputStream os, byte[] bytes) throws Exception {
        ReflectUtil.envokeMethod(os, "write", new Class[] {byte[].class}, new Object[] {bytes}, true);
    }
}
