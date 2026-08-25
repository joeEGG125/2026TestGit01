package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.util.Locale;
import java.util.zip.*;

public class CompressionUtil {
    private static final LogHelper logger = new LogHelper();

    private CompressionUtil() {}

    /**
     * 把檔案壓縮成zip格式
     *
     * @param files       需要壓縮的檔名
     * @param zipFilePath 壓縮後的zip檔案名   ,如"D:/test/aa.zip";
     */
    public static Boolean compressFiles2Zip(File[] files, String zipFilePath) throws Exception {
        boolean bRes = true;
        if (ArrayUtils.isNotEmpty(files)) {
            if (isEndsWithZip(zipFilePath)) {
                try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(new File(CleanPathUtil.cleanString(zipFilePath)))) {
                    // Use Zip64 extensions for all entries where they are required
                    zaos.setUseZip64(Zip64Mode.AsNeeded);
                    // 將每個檔案用ZipArchiveEntry封裝
                    // 再用ZipArchiveOutputStream寫到壓縮檔案中
                    for (File file : files) {
                        if (file != null) {
                            ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(file, file.getName());
                            zaos.putArchiveEntry(zipArchiveEntry);
                            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                                byte[] buffer = new byte[1024 * 5];
                                int len = -1;
                                while ((len = is.read(buffer)) != -1) {
                                    // 把緩沖區的字節寫入到ZipArchiveEntry
                                    zaos.write(buffer, 0, len);
                                }
                            } catch (Exception e) {
                                bRes = false;
                                throw e;
                            } finally {
                                // Writes all necessary data for this entry.
                                try {
                                    zaos.closeArchiveEntry();
                                } catch (IOException e) {
                                    logger.warn(e, e.getMessage());
                                }
                            }
                        }
                    }
                    zaos.finish();
                } catch (Exception e) {
                    bRes = false;
                    throw e;
                }
            }
        } else {
            bRes = false;
        }
        return bRes;
    }

    /**
     * 判斷檔案名是否為.zip
     *
     * @param fileName 需要判斷的檔案名
     * @return 是zip檔案返回true, 否則返回false
     */
    public static boolean isEndsWithZip(String fileName) {
        boolean flag = false;
        if (fileName != null && !"".equals(fileName.trim())) {
            if (fileName.toUpperCase(Locale.ENGLISH).endsWith(".ZIP")) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 壓縮檔案或資料夾（包括所有子目錄檔案）
     *
     * @param sourceFile 原始檔
     * @param zipFile    壓縮檔名稱
     * @throws IOException 異常資訊
     */
    public static void zipFileTree(File sourceFile, File zipFile) throws IOException {
        ZipOutputStream zipOutputStream = null;
        ZipInputStream zipInputStream = null;
        FileInputStream fileInputStream = null;
        try {
            // 壓縮輸出流
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            if (!zipFile.exists()) {
                zipFile.createNewFile();
            } else {
                fileInputStream = new FileInputStream(zipFile);
                zipInputStream = new ZipInputStream(fileInputStream);
            }

            zipOutputStream = new ZipOutputStream(fileOutputStream);
            if (null != zipInputStream) {
                ZipEntry zipEntry = zipInputStream.getNextEntry();
                if (null != zipEntry) {
                    zipOutputStream.putNextEntry(zipEntry);
                    int readLen;
                    byte[] buffer = new byte[1024];
                    while ((readLen = fileInputStream.read(buffer)) != -1) {
                        zipOutputStream.write(buffer, 0, readLen);
                    }
                }
            }
            zip(sourceFile, zipOutputStream, "");
        } finally {
            if (null != zipOutputStream) {
                // 關閉流
                try {
                    zipOutputStream.close();
                } catch (IOException ex) {
                    logger.warn(ex, ex.getMessage());
                }
            }
            if (null != zipInputStream) {
                // 關閉流
                try {
                    zipInputStream.close();
                } catch (IOException ex) {
                    logger.warn(ex, ex.getMessage());
                }
            }
        }
    }

    /**
     * 遞迴壓縮檔案
     *
     * @param file            當前檔案
     * @param zipOutputStream 壓縮輸出流
     * @param relativePath    相對路徑
     * @throws IOException IO異常
     */
    private static void zip(File file, ZipOutputStream zipOutputStream, String relativePath)
            throws IOException {
        FileInputStream fileInputStream = null;
        try {
            if (file.isDirectory()) { // 當前為資料夾
                // 當前資料夾下的所有檔案
                File[] list = file.listFiles();
                if (null != list) {
                    // 計算當前的相對路徑
                    relativePath += (relativePath.length() == 0 ? "" : File.separator) + file.getName();
                    // 遞迴壓縮每個檔案
                    for (File f : list) {
                        zip(f, zipOutputStream, relativePath);
                    }
                }
            } else { // 壓縮檔案
                // 計算檔案的相對路徑
                relativePath += (relativePath.length() == 0 ? "" : File.separator) + file.getName();
                // 寫入單個檔案
                zipOutputStream.putNextEntry(new ZipEntry(relativePath));
                fileInputStream = new FileInputStream(file);
                int readLen;
                byte[] buffer = new byte[1024];
                while ((readLen = fileInputStream.read(buffer)) != -1) {
                    zipOutputStream.write(buffer, 0, readLen);
                }
                zipOutputStream.closeEntry();
            }
        } finally {
            // 關閉流
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ex) {
                    logger.warn(ex, ex.getMessage());
                }
            }
        }
    }

    /**
     * compress ont times, faster and use less memory
     *
     * @param bDataAry byte[][]
     * @return byte[][]
     * @throws IOException
     */
    public static byte[][] compress(byte[][] bDataAry) throws IOException {
        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[][] bDataOutputAry = new byte[bDataAry.length][];
        byte[] bData = null;
        byte[] buf = null;
        for (int i = 0; i < bDataAry.length; i++) {
            bData = bDataAry[i];
            compressor.setInput(bData);
            compressor.finish();
            buf = new byte[bData.length + 256];
            while (!compressor.finished()) {
                try {
                    int count = compressor.deflate(buf);
                    bos.write(buf, 0, count);
                } catch (NullPointerException e) {
                    String errorMessage = logger.warn(e, "compressor error with exception occur, data = [", StringUtil.toHex(bData), "]");
                    throw ExceptionUtil.createIOException(errorMessage);
                }
            }
            bDataOutputAry[i] = bos.toByteArray();
            bos.reset();
            compressor.reset();
            bData = null;
            buf = null;
        }
        compressor.end();
        bos.close();
        return bDataOutputAry;

    }

    public static byte[] compress(byte[] bData) throws IOException {
        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);
        // Give the compressor the data to compress
        compressor.setInput(bData);
        compressor.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bData.length);
        // Compress the data
        byte[] buf = new byte[bData.length + 256];
        while (!compressor.finished()) {
            try {
                int count = compressor.deflate(buf);
                bos.write(buf, 0, count);
            } catch (Exception e) {
                String errorMessage = logger.warn(e, "compressor error with exception occur, data = [", StringUtil.toHex(bData), "]");
                throw ExceptionUtil.createIOException(errorMessage);
            }
        }
        compressor.end();
        bos.close();
        return bos.toByteArray();
    }

    public static byte[] decompress(byte[] bData) throws IOException, DataFormatException {
        Inflater decompressor = new Inflater();
        decompressor.setInput(bData);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bData.length);
        // Decompress the data
        byte[] buf = new byte[bData.length + 1024];
        decompress(decompressor, buf, bos);
        decompressor.end();
        bos.close();
        return bos.toByteArray();
    }

    private static void decompress(Inflater decompressor, byte[] buf, OutputStream os) throws IOException, DataFormatException {
        if (!decompressor.finished()) {
            int count = decompressor.inflate(buf);
            os.write(buf, 0, count);
            decompress(decompressor, buf, os);
        }
    }

    public static byte[] compressSerializable(Serializable serializable) throws Exception {
        byte[] bytes = SerializationUtils.serialize(serializable);
        return compress(bytes);
    }

    public static Serializable decompressSerializable(byte[] bytes) throws Exception {
        bytes = decompress(bytes);
        return SerializationUtils.deserialize(bytes);
    }
}
