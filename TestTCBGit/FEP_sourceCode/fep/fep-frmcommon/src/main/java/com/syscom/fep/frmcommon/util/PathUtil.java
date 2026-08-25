package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ref.RefLong;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class PathUtil {
    private static final LogHelper logger = new LogHelper();

    private PathUtil() {}

    /**
     * 修正Stored Relative Path Traversal
     *
     * @param path
     * @return
     */
    public static String removeTraversal(String path) {
        if (StringUtils.isNotBlank(path)) {
            return path.replace("..", StringUtils.EMPTY)
                    .replace("/", StringUtils.EMPTY)
                    .replace("\\", StringUtils.EMPTY)
                    .replace("'", StringUtils.EMPTY);
        }
        return path;
    }

    /**
     * 計算指定資料夾的大小
     *
     * @param directory
     * @return
     */
    public static long sizeOfDirectory(Path directory) {
        final RefLong size = new RefLong(0);
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    size.set(size.get() + attrs.size());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    logger.warn("[sizeOfDirectory]Failed to access file: ", file.toString(), " cause ", exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });
            return size.get();
        } catch (IOException e) {
            logger.warn("[sizeOfDirectory]Failed to access directory: ", directory.toString(), " cause ", e.getMessage());
            return -1;
        }
    }
}
