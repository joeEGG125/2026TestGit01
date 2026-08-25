package com.syscom.fep.frmcommon.esapi;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.owasp.esapi.SafeFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.FileSystemResourceLoader;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ESAPIUtil {
    private static final LogHelper logger = new LogHelper();
    public static final String CHECK_AUTHORIZATION_ADMIN = "admin";

    private ESAPIUtil() {}

    public static File toFile(String path) {
        try {
            return new SafeFile(path);
        } catch (Exception e) {
            logger.warn("Error occurred while converting path to SafeFile, ", e.getMessage(), ", path:", path);
        }
        FileSystemResourceLoader loader = new FileSystemResourceLoader();
        return ((FileSystemResource) loader.getResource(path)).getFile();
    }

    public static File toFile(String parent, String child) {
        try {
            return new SafeFile(parent, child);
        } catch (Exception e) {
            logger.warn("Error occurred while converting path to SafeFile, ", e.getMessage(), ", parent:", parent, ", child:", child);
        }
        FileSystemResourceLoader loader = new FileSystemResourceLoader();
        return ((FileSystemResource) loader.getResource(Paths.get(parent, child).toString())).getFile();
    }

    public static File toFile(File parent, String child) {
        try {
            return new SafeFile(parent, child);
        } catch (Exception e) {
            logger.warn("Error occurred while converting path to SafeFile, ", e.getMessage(), ", parent:", parent.getAbsolutePath(), ", child:", child);
        }
        FileSystemResourceLoader loader = new FileSystemResourceLoader();
        return ((FileSystemResource) loader.getResource(Paths.get(parent.getAbsolutePath(), child).toString())).getFile();
    }

    public static File toFile(URI uri) {
        try {
            return new SafeFile(uri);
        } catch (Exception e) {
            logger.warn("Error occurred while converting path to SafeFile, ", e.getMessage(), ", uri:", uri.toString());
        }
        FileSystemResourceLoader loader = new FileSystemResourceLoader();
        return ((FileSystemResource) loader.getResource(Paths.get(uri).toString())).getFile();
    }

    public static boolean checkAuthorization(String userName) {
        return CHECK_AUTHORIZATION_ADMIN.equals(userName);
    }

    /**
     * for [Input Path Not Canonicalized]
     *
     * @param filename
     * @return
     */
    public static String sanitizePathTraversal(String filename) {
        Path p = Paths.get(filename);
        return p.getFileName().toString();
    }
}
