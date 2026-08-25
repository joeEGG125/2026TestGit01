package com.syscom.fep.common.util;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.delegate.ActionListener2;
import com.syscom.fep.frmcommon.esapi.ESAPIUtil;
import com.syscom.fep.frmcommon.esapi.ESAPIValidator;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.event.Level;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 獲取AP日誌檔
 *
 * @author Richard
 */
public class GetApLogFilesUtil {
    private static final LogHelper logger = LogHelperFactory.getGeneralLogger();

    private GetApLogFilesUtil() {}

    public enum ApLogType {
        aplog, waslog;
    }

    public enum ApLogAction {
        Compress, GetTotalSize;
    }

    /**
     * 入參
     */
    public static class GetApLogParamIn {
        OutputStream os;
        /**
         * LOG類型, aplog或waslog
         */
        ApLogType logType;
        /**
         * yyyy-MM-dd格式日期
         */
        String logDate;
        /**
         * 一般是指Standalone的log檔資料夾
         */
        String fepLogPath;
        /**
         * 一般是指WAS產出的log檔資料夾
         */
        String fepWasLogPath;
        /**
         * 一般是指Standalone的已經被Archive的log檔資料夾
         */
        String fepLogArchivesPath;
        /**
         * 一般是指WAS的已經被Archive的log檔資料夾
         */
        String fepWasLogArchivesPath;
        /**
         * log檔開始時間HH:mm:ss格式, 可為空
         */
        String logTimeBegin;
        /**
         * log檔結束時間HH:mm:ss格式, 可為空
         */
        String logTimeEnd;
        /**
         * 動作, 預設壓縮檔
         */
        ApLogAction action = ApLogAction.Compress;
        /**
         * 指定tar.gz檔
         */
        String selectGZLog;
        /**
         * 服務程式名稱, 例如fep-server-atm
         */
        String appName;

        public GetApLogParamIn(ApLogType logType, String logDate, String fepLogPath, String fepWasLogPath, String fepLogArchivesPath, String fepWasLogArchivesPath, String logTimeBegin, String logTimeEnd, ApLogAction action, String selectGZLog, String appName) {
            this(null, logType, logDate, fepLogPath, fepWasLogPath, fepLogArchivesPath, fepWasLogArchivesPath, logTimeBegin, logTimeEnd, action, selectGZLog, appName);
        }

        public GetApLogParamIn(OutputStream os, ApLogType logType, String logDate, String fepLogPath, String fepWasLogPath, String fepLogArchivesPath, String fepWasLogArchivesPath, String logTimeBegin, String logTimeEnd, ApLogAction action, String selectGZLog, String appName) {
            this.os = os;
            this.logType = logType;
            this.logDate = logDate;
            this.fepLogPath = fepLogPath;
            this.fepWasLogPath = fepWasLogPath;
            this.fepLogArchivesPath = fepLogArchivesPath;
            this.fepWasLogArchivesPath = fepWasLogArchivesPath;
            this.logTimeBegin = logTimeBegin;
            this.logTimeEnd = logTimeEnd;
            this.action = action;
            this.selectGZLog = selectGZLog;
            this.appName = appName;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("os", os)
                    .append("logType", logType)
                    .append("logDate", logDate)
                    .append("fepLogPath", fepLogPath)
                    .append("fepWasLogPath", fepWasLogPath)
                    .append("fepLogArchivesPath", fepLogArchivesPath)
                    .append("fepWasLogArchivesPath", fepWasLogArchivesPath)
                    .append("logTimeBegin", logTimeBegin)
                    .append("logTimeEnd", logTimeEnd)
                    .append("action", action)
                    .append("selectGZLog", selectGZLog)
                    .append("appName", appName)
                    .toString();
        }
    }

    /**
     * 出參
     */
    public static class GetApLogParamOut {
        byte[] compressedBytes = new byte[0];
        long totalSize = 0;
        long totalCount = 0;
        long totalCompressedCount = 0;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("totalSize", totalSize)
                    .append("totalCount", totalCount)
                    .append("totalCompressedCount", totalCompressedCount)
                    .append("compressedBytesLength", ArrayUtils.isNotEmpty(compressedBytes) ? compressedBytes.length : 0)
                    .toString();
        }
    }

    /**
     * 獲取AP日誌壓縮檔
     *
     * @param logType               LOG類型, aplog或waslog
     * @param logDate               yyyy-MM-dd格式日期
     * @param fepLogPath            一般是指Standalone的log檔資料夾
     * @param fepWasLogPath         一般是指WAS產出的log檔資料夾
     * @param fepLogArchivesPath    一般是指Standalone的已經被Archive的log檔資料夾
     * @param fepWasLogArchivesPath 一般是指WAS的已經被Archive的log檔資料夾
     * @param logTimeBegin          log檔開始時間HH:mm:ss格式, 可為空
     * @param logTimeEnd            log檔結束時間HH:mm:ss格式, 可為空
     * @param selectGZLog           指定tar.gz檔
     * @param appName               指定服務名稱, 例如fep-server-atm, 可為空
     * @return
     * @throws Exception
     */
    public static byte[] getApLogFiles(ApLogType logType, String logDate, String fepLogPath, String fepWasLogPath, String fepLogArchivesPath, String fepWasLogArchivesPath, String logTimeBegin, String logTimeEnd, String selectGZLog, String appName) throws Exception {
        return getApLogFiles(null, logType, logDate, fepLogPath, fepWasLogPath, fepLogArchivesPath, fepWasLogArchivesPath, logTimeBegin, logTimeEnd, selectGZLog, appName);
    }

    /**
     * 獲取AP日誌壓縮檔
     *
     * @param os                    將壓縮後的byte寫入指定的stream中
     * @param logType               LOG類型, aplog或waslog
     * @param logDate               yyyy-MM-dd格式日期
     * @param fepLogPath            一般是指Standalone的log檔資料夾
     * @param fepWasLogPath         一般是指WAS產出的log檔資料夾
     * @param fepLogArchivesPath    一般是指Standalone的已經被Archive的log檔資料夾
     * @param fepWasLogArchivesPath 一般是指WAS的已經被Archive的log檔資料夾
     * @param logTimeBegin          log檔開始時間HH:mm:ss格式, 可為空
     * @param logTimeEnd            log檔結束時間HH:mm:ss格式, 可為空
     * @param selectGZLog           指定tar.gz檔
     * @param appName               指定服務名稱, 例如fep-server-atm, 可為空
     * @return
     * @throws Exception
     */
    public static byte[] getApLogFiles(OutputStream os, ApLogType logType, String logDate, String fepLogPath, String fepWasLogPath, String fepLogArchivesPath, String fepWasLogArchivesPath, String logTimeBegin, String logTimeEnd, String selectGZLog, String appName) throws Exception {
        GetApLogParamOut out = new GetApLogParamOut();
        handleApLogFiles(new GetApLogParamIn(os, logType, logDate, fepLogPath, fepWasLogPath, fepLogArchivesPath, fepWasLogArchivesPath, logTimeBegin, logTimeEnd, ApLogAction.Compress, selectGZLog, appName), out);
        return out.compressedBytes;
    }

    /**
     * 獲取檔的總大小
     *
     * @param logType
     * @param logDate
     * @param fepLogPath
     * @param fepWasLogPath
     * @param fepLogArchivesPath
     * @param fepWasLogArchivesPath
     * @param logTimeBegin
     * @param logTimeEnd
     * @param appName
     * @return
     * @throws Exception
     */
    public static long getApLogFilesTotalSize(ApLogType logType, String logDate, String fepLogPath, String fepWasLogPath, String fepLogArchivesPath, String fepWasLogArchivesPath, String logTimeBegin, String logTimeEnd, String appName) throws Exception {
        GetApLogParamOut out = new GetApLogParamOut();
        handleApLogFiles(new GetApLogParamIn(logType, logDate, fepLogPath, fepWasLogPath, fepLogArchivesPath, fepWasLogArchivesPath, logTimeBegin, logTimeEnd, ApLogAction.GetTotalSize, null, appName), out);
        return out.totalSize;
    }

    /**
     * 處理獲取日誌檔相關信息
     *
     * @param in
     * @param out
     * @throws Exception
     */
    public static void handleApLogFiles(GetApLogParamIn in, GetApLogParamOut out) throws Exception {
        if (in.logType == ApLogType.aplog) {
            handleApLogFiles(in, out, in.fepLogPath, in.fepLogArchivesPath);
        } else if (in.logType == ApLogType.waslog) {
            handleApLogFiles(in, out, in.fepWasLogPath, in.fepWasLogArchivesPath);
        }
    }

    /**
     * 處理獲取日誌檔相關信息
     *
     * @param in
     * @param out
     * @param logPath
     * @param logArchivesPath
     * @throws Exception
     */
    private static void handleApLogFiles(GetApLogParamIn in, GetApLogParamOut out, String logPath, String logArchivesPath) throws Exception {
        try {
            int beginTime = 0;
            int endTime = 250000;
            // 資料夾為logPath/yyyy-MM-dd
            Object logDirectory = ESAPIUtil.toFile(CleanPathUtil.cleanString(logPath), CleanPathUtil.cleanString(in.logDate));
            // 如果logPath/yyyy-MM-dd資料夾存在, 說明log檔還沒有被archive, 則現場將符合條件的log檔全部打包進一個tar檔中
            if (((File) logDirectory).exists()) {
                logger.info("Directory exist, directory = [", ((File) logDirectory).getAbsolutePath(), "], compress now, ", in.toString());
                // 如果是log檔, 時間需要精確到時分秒
                if (StringUtils.isNotBlank(in.logTimeBegin)) {
                    try {
                        beginTime = Integer.parseInt(StringUtils.join(in.logTimeBegin.substring(0, 2), in.logTimeBegin.substring(3, 5), in.logTimeBegin.substring(6, 8)));
                    } catch (Exception e) {
                        logger.warn(e, "Invalid Format, logTimeBegin = [", in.logTimeBegin, "]");
                    }
                }
                if (StringUtils.isNotBlank(in.logTimeEnd)) {
                    try {
                        endTime = Integer.parseInt(StringUtils.join(in.logTimeEnd.substring(0, 2), in.logTimeEnd.substring(3, 5), in.logTimeEnd.substring(6, 8)));
                    } catch (Exception e) {
                        logger.warn(e, "Invalid Format, logTimeEnd = [", in.logTimeEnd, "]");
                    }
                }
                handleApLogFiles(in, out, logDirectory, beginTime, endTime, false);
            } else {
                logger.warn("Directory not exist, directory = [", ((File) logDirectory).getAbsolutePath(), "], fetch and compress all archives, ", in.toString());
                // 如果是tar.gz檔, 時間需要到時就好
                if (StringUtils.isNotBlank(in.logTimeBegin)) {
                    try {
                        beginTime = Integer.parseInt(in.logTimeBegin.substring(0, 2)) * 10000;
                    } catch (Exception e) {
                        logger.warn(e, "Invalid Format, logTimeBegin = [", in.logTimeBegin, "]");
                    }
                }
                if (StringUtils.isNotBlank(in.logTimeEnd)) {
                    try {
                        endTime = Integer.parseInt(in.logTimeEnd.substring(0, 2)) * 10000;
                    } catch (Exception e) {
                        logger.warn(e, "Invalid Format, logTimeEnd = [", in.logTimeEnd, "]");
                    }
                }
                Object archivesDirectory = ESAPIUtil.toFile(CleanPathUtil.cleanString(logArchivesPath), CleanPathUtil.cleanString(in.logDate));
                // 取logArchivesPath/yyyy-MM-dd中的壓縮檔
                handleApLogFiles(in, out, archivesDirectory, beginTime, endTime, true);
            }
        } catch (Exception e) {
            logger.exceptionMsg(e, "Get AP Log Files with exception occur, ", in.toString());
            throw e;
        }
    }

    /**
     * 處理指定的資料夾和資料夾下所有的子資料夾或者檔
     *
     * @param in
     * @param out
     * @param directory
     * @param beginTime
     * @param endTime
     * @throws Exception
     */
    private static void handleApLogFiles(GetApLogParamIn in, GetApLogParamOut out, Object directory, int beginTime, int endTime, boolean handleArchiveFile) throws Exception {
        if (((File) directory).isDirectory()) {
            logger.debug("Start to ", in.action.name(), ", Directory = [", ((File) directory).getAbsolutePath(), "]");
            // 壓縮檔
            if (in.action == ApLogAction.Compress) {
                OutputStream os = in.os;
                if (os == null)
                    os = new ByteArrayOutputStream();
                try (TarArchiveOutputStream tos = new TarArchiveOutputStream(new GzipCompressorOutputStream(os))) {
                    tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX); // 2026-06-04 Richard add 解決文件名過長的問題
                    handleApLogFiles(in, out, directory, beginTime, endTime, handleArchiveFile, tos);
                } catch (IOException e) {
                    throw ExceptionUtil.createException(e, in.action.name(), " Failed, Directory = [", ((File) directory).getAbsolutePath(), "]");
                }
                if (os instanceof ByteArrayOutputStream) {
                    byte[] bytes = ((ByteArrayOutputStream) os).toByteArray();
                    os.close(); // 這裡一定要close
                    os = null;
                    // return
                    out.compressedBytes = bytes;
                }
                if (out.totalCount == 0)
                    throw ExceptionUtil.createException("ERROR:", in.logType.name().toUpperCase(), "檔總數為0!!!");
                else if (out.totalCompressedCount == 0)
                    throw ExceptionUtil.createException("ERROR:成功打包", in.logType.name().toUpperCase(), "檔總數為0!!!");
            }
            // 只累加計算檔大小
            else if (in.action == ApLogAction.GetTotalSize) {
                handleApLogFiles(in, out, directory, beginTime, endTime, handleArchiveFile, null);
            }
        } else {
            logger.warn("[", ((File) directory).getAbsolutePath(), "] not exist!!!");
            throw ExceptionUtil.createException("ERROR:", in.logType.name().toUpperCase(), "檔不存在!!!");
        }
    }

    /**
     * 處理指定的資料夾和資料夾下所有的子資料夾或者檔
     *
     * @param in
     * @param out
     * @param directory
     * @param beginTime
     * @param endTime
     * @param handleArchiveFile
     * @param tos
     * @throws Exception
     */
    private static void handleApLogFiles(GetApLogParamIn in, GetApLogParamOut out, Object directory, int beginTime, int endTime, boolean handleArchiveFile, TarArchiveOutputStream tos) throws Exception {
        boolean handled = false;
        if (!handleArchiveFile) {
            // 2026-02-12 Richard add 如果有指定AppName, 則只壓縮AppName的log檔
            if (StringUtils.isNotBlank(in.appName)) {
                List<String> appNames = StringUtil.split(in.appName, ',', ';');
                for (String appName : appNames) {
                    Object appDirectory = ESAPIUtil.toFile(CleanPathUtil.cleanString(((File) directory).getAbsolutePath()), CleanPathUtil.cleanString(appName));
                    if (((File) appDirectory).isDirectory()) {
                        handleApLogFiles(in, out, tos, ((File) directory).getParentFile().getAbsolutePath(), appDirectory, beginTime, endTime);
                        logger.debug(in.action.name(), " Successful, Directory = [", ((File) appDirectory).getAbsolutePath(), "], ", out.toString());
                        handled = true;
                    } else {
                        logger.warn("[", ((File) appDirectory).getAbsolutePath(), "] not exist!!!");
                    }
                }
                if (!handled) {
                    throw ExceptionUtil.createException("ERROR:", in.logType.name().toUpperCase(), "檔不存在!!!");
                }
            }
        }
        if (!handled) {
            handleApLogFiles(in, out, tos, ((File) directory).getParentFile().getAbsolutePath(), directory, beginTime, endTime);
            logger.debug(in.action.name(), " Successful, Directory = [", ((File) directory).getAbsolutePath(), "], ", out.toString());
        }
    }

    /**
     * 處理指定的資料夾和資料夾下所有的子資料夾或者檔
     *
     * @param in
     * @param out
     * @param tos
     * @param parentPath
     * @param directory
     * @param beginTime
     * @param endTime
     * @throws Exception
     */
    private static void handleApLogFiles(GetApLogParamIn in, GetApLogParamOut out, TarArchiveOutputStream tos, String parentPath, Object directory, int beginTime, int endTime) throws Exception {
        File[] children = ((File) directory).listFiles();
        if (ArrayUtils.isEmpty(children)) {
            logger.warn("[", ((File) directory).getAbsolutePath(), "] is empty!!!");
            return;
        }
        for (Object child : children) {
            if (((File) child).isDirectory()) {
                handleApLogFiles(in, out, tos, parentPath, child, beginTime, endTime);
            } else {
                // 指定tar檔的名字
                if (StringUtils.isNotBlank(in.selectGZLog) && !((File) child).getName().equals(in.selectGZLog)) {
                    continue;
                }
                Calendar lastModified = CalendarUtil.clone(((File) child).lastModified());
                int childDate;
                // 如果是TAR檔直接取小時, 否則取時分秒
                if (((File) child).getName().contains(".tar.gz")) {
                    //2025/5/14 .tar.gz取小時
                    childDate = Integer.parseInt(((File) child).getName().substring(11, 13)) * 10000;
                } else {
                    childDate = CalendarUtil.timeValueInHourMinuteSecond(lastModified) / 1000;
                }
                if (childDate < beginTime || childDate > endTime) {
                    logger.warn("Time not match, [", childDate, "] not in [", beginTime, "~", endTime, "], directory = [", ((File) directory).getAbsolutePath(), "], file = [", ((File) child).getAbsolutePath(), "]");
                    continue;
                }
                long size = ((File) child).length();
                out.totalSize += size; //累加檔大小
                out.totalCount++; //累加檔數量
                // 壓縮檔
                if (in.action == ApLogAction.Compress) {
                    logger.debug("Start to Tar, directory = [", ((File) directory).getAbsolutePath(), "], file = [", ((File) child).getAbsolutePath(), "], size = [", size, "]");
                    byte[] buffer = new byte[1024];
                    // 2025-05-22 Richard modified for [Absolute Path Traversal]
                    RefBase<InputStream> rtn = new RefBase<>(null);
                    toStream(rtn, child);
                    try (BufferedInputStream bis = new BufferedInputStream(rtn.get())) {
                        TarArchiveEntry tarEntry = new TarArchiveEntry(((File) child).getAbsolutePath());
                        tarEntry.setName(StringUtils.substring(((File) child).getAbsolutePath(), parentPath.length()));
                        tarEntry.setSize(size);
                        tos.putArchiveEntry(tarEntry);
                        boolean flag = true;
                        int numToWrite, total = 0;
                        while ((numToWrite = bis.read(buffer)) != -1 && flag) {
                            // 這裡要判斷一下, 如果壓縮的檔正在動態變大, 則只壓縮tarEntry.getSize()大小就好, 否則下面tos.write時會有IOException
                            if (total + numToWrite > tarEntry.getSize()) {
                                logger.warn("Request to write '", numToWrite, "' bytes exceeds size in header of '", tarEntry.getSize(), "' bytes for entry '", tarEntry.getName(), "'");
                                numToWrite = (int) (tarEntry.getSize() - total);
                                flag = false;
                            }
                            tos.write(buffer, 0, numToWrite);
                            total += numToWrite;
                        }
                        out.totalCompressedCount++;
                    } catch (Exception e) {
                        logger.exceptionMsg(e, "Tar failed, directory = [", ((File) directory).getAbsolutePath(), "], file = [", ((File) child).getAbsolutePath(), "]");
                    } finally {
                        tos.closeArchiveEntry();
                    }
                }
                // 只累加計算檔大小
                else if (in.action == ApLogAction.GetTotalSize) {
                    logger.debug("Start to Get Total Size, directory = [", ((File) directory).getAbsolutePath(), "], file = [", ((File) child).getAbsolutePath(), "], size = [", size, "]");
                }
            }
        }
    }

    /**
     * 取得AP LOG檔, for Restful Method
     *
     * @param operator
     * @param logType
     * @param logDate
     * @param fepLogPath
     * @param fepWasLogPath
     * @param fepLogArchivesPath
     * @param fepWasLogArchivesPath
     * @param logTimeBegin
     * @param logTimeEnd
     * @param sizeLimit
     * @param defaultSizeLimit
     * @param logger
     * @param emsLogger
     * @param selectGZLog
     * @param appName
     * @return
     */
    public static ResponseEntity<StreamingResponseBody> getApLog(String operator, String logType, String logDate, String fepLogPath, String fepWasLogPath, String fepLogArchivesPath, String fepWasLogArchivesPath, String logTimeBegin, String logTimeEnd, String sizeLimit, int defaultSizeLimit, ActionListener2<Level, String> logger, ActionListener2<Exception, String> emsLogger, String selectGZLog, String appName) {
        String logSuffix = StringUtils.join(", operator = [", operator, "], logType = [", logType, "], logDate = [", logDate, "], fepLogPath = [", fepLogPath, "], fepWasLogPath = [", fepWasLogPath, "], fepLogArchivesPath = [", fepLogArchivesPath, "], fepWasLogArchivesPath = [", fepWasLogArchivesPath, "], logTimeBegin = [", logTimeBegin, "], logTimeEnd = [", logTimeEnd, "], sizeLimit = [", sizeLimit, "], defaultSizeLimit = [", defaultSizeLimit, "], selectGZLog = [", selectGZLog, "], appName = [", appName, "]");
        if (logger != null)
            logger.actionPerformed(Level.INFO, StringUtils.join("GetAPLog", logSuffix));
        try {
            // 先取出檔案的總大小
            long totalSize = GetApLogFilesUtil.getApLogFilesTotalSize(ApLogType.valueOf(logType), logDate, fepLogPath, fepWasLogPath, fepLogArchivesPath, fepWasLogArchivesPath, logTimeBegin, logTimeEnd, appName);
            // 如果總大小為0就回報錯誤訊息
            if (totalSize == 0) {
                String errorMessage = StringUtils.join("指定區間的LOG大小總共為", oshi.util.FormatUtil.formatBytes(totalSize), ", 請確認查詢區間是否正確");
                if (logger != null)
                    logger.actionPerformed(Level.WARN, StringUtils.join(errorMessage, logSuffix));
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                        .body(os -> os.write(StringUtils.join("ERROR:", errorMessage).getBytes(StandardCharsets.UTF_8)));
            }
            // 如果超過限制就回報錯誤訊息
            long totalSizeLimit = 0;
            try {
                totalSizeLimit = Integer.parseInt(sizeLimit);
            } catch (NumberFormatException e) {}
            if (totalSizeLimit <= 0)
                totalSizeLimit = defaultSizeLimit;
            totalSizeLimit = totalSizeLimit * 1024L * 1024L * 1024L;
            if (totalSize > totalSizeLimit) {
                String errorMessage = StringUtils.join("指定區間的LOG大小總共為", oshi.util.FormatUtil.formatBytes(totalSize), ", 已超過系統限制", oshi.util.FormatUtil.formatBytes(totalSizeLimit), ", 請縮小查詢區間");
                if (logger != null)
                    logger.actionPerformed(Level.WARN, StringUtils.join(errorMessage, logSuffix));
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                        .body(os -> os.write(StringUtils.join("ERROR:", errorMessage).getBytes(StandardCharsets.UTF_8)));
            }
        } catch (Exception e) {
            if (emsLogger != null)
                emsLogger.actionPerformed(e, StringUtils.join("GetAPLog TotalSize, ", logSuffix, " with exception occur, ", e.getMessage()));
            RefString errorMessage = new RefString(e.getMessage());
            String prefix = "ERROR:";
            if (StringUtils.isNotBlank(errorMessage.get()) && errorMessage.get().startsWith(prefix)) {
                errorMessage.set(e.getMessage().substring(prefix.length()));
            } else {
                errorMessage.set("取LOG檔案大小出現異常");
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .body(os -> os.write(StringUtils.join("ERROR:", errorMessage.get()).getBytes(StandardCharsets.UTF_8)));
        }
        if (StringUtils.isBlank(selectGZLog)) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, StringUtils.join("attachment; filename=", logDate, ".tar.gz"))
                    .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .body(os -> {
                        try {
                            // 開始壓縮取檔案內容
                            GetApLogFilesUtil.getApLogFiles(os, ApLogType.valueOf(logType), logDate, fepLogPath, fepWasLogPath, fepLogArchivesPath, fepWasLogArchivesPath, logTimeBegin, logTimeEnd, selectGZLog, appName);
                        } catch (Exception e) {
                            if (emsLogger != null)
                                emsLogger.actionPerformed(e, StringUtils.join("GetAPLog, ", logSuffix, " with exception occur, ", e.getMessage()));
                            os.write(StringUtils.join("ERROR:下載失敗, ", e.getMessage()).getBytes(StandardCharsets.UTF_8));
                        }
                    });
        } else {
            String logPath = fepLogArchivesPath + "/" + logDate + "/" + selectGZLog;
            String sanitizedPath = ESAPIValidator.getValidDirectoryName(logPath);
            Object file = ESAPIUtil.toFile(sanitizedPath);
            // 2025-06-17 Richard modified for [Absolute Path Traversal]
            // ActionListener1<OutputStream> actionListener = os -> IOUtils.copy(Files.newInputStream(((File) file).toPath()), os);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, StringUtils.join("attachment; filename=", logPath))
                    .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .body(os -> {
                        try {
                            // 2025-06-17 Richard modified for [Absolute Path Traversal]
                            RefBase<InputStream> rtn = new RefBase<>(null);
                            toStream(rtn, file);
                            IOUtils.copy(rtn.get(), os);
                        } catch (Exception e) {
                            if (e instanceof IOException)
                                throw (IOException) e;
                            throw ExceptionUtil.createIOException(e, e.getMessage());
                        }
                    });
        }
    }

    private static String sanitizePathTraversal(String filename) {
        Path p = Paths.get(filename);
        return p.toString();
    }

    private static void toStream(RefBase<InputStream> rtn, Object file) throws IOException {
        rtn.set(FileUtils.openInputStream((File) file));
    }

    public static List<String> getArchiveFileNames(ApLogType logType, String logDate, String fepLogPath, String fepWasLogPath, String fepLogArchivesPath, String fepWasLogArchivesPath, String logTimeBegin, String logTimeEnd) throws Exception {
        GetApLogParamOut out = new GetApLogParamOut();
        return getArchiveFileNames(new GetApLogParamIn(logType, logDate, fepLogPath, fepWasLogPath, fepLogArchivesPath, fepWasLogArchivesPath, logTimeBegin, logTimeEnd, ApLogAction.GetTotalSize, null, null), out);
    }

    private static List<String> getArchiveFileNames(GetApLogParamIn in, GetApLogParamOut out) throws Exception {
        if (in.logType == ApLogType.aplog) {
            return getArchiveFileNames(in, out, in.fepLogPath, in.fepLogArchivesPath);
        } else if (in.logType == ApLogType.waslog) {
            return getArchiveFileNames(in, out, in.fepWasLogPath, in.fepWasLogArchivesPath);
        }
        return new ArrayList<>();
    }

    private static List<String> getArchiveFileNames(GetApLogParamIn in, GetApLogParamOut out, String logPath, String logArchivesPath) throws Exception {
        try {
            int beginTime = 0;
            int endTime = 250000;
            // 資料夾為logPath/yyyy-MM-dd
            Object logDirectory = ESAPIUtil.toFile(CleanPathUtil.cleanString(logPath), CleanPathUtil.cleanString(in.logDate));
            // 如果logPath/yyyy-MM-dd資料夾存在, 說明log檔還沒有被archive, 則現場將符合條件的log檔全部打包進一個tar檔中
            if (((File) logDirectory).exists()) {
                logger.info(logDirectory, "资料夹還沒有被archive");
            } else {
                logger.warn("Directory not exist, directory = [", ((File) logDirectory).getAbsolutePath(), "], fetch and compress all archives, ", in.toString());
                // 如果是tar.gz檔, 時間需要到時就好
                if (StringUtils.isNotBlank(in.logTimeBegin)) {
                    try {
                        beginTime = Integer.parseInt(in.logTimeBegin.substring(0, 2)) * 10000;
                    } catch (Exception e) {
                        logger.warn(e, "Invalid Format, logTimeBegin = [", in.logTimeBegin, "]");
                    }
                }
                if (StringUtils.isNotBlank(in.logTimeEnd)) {
                    try {
                        endTime = Integer.parseInt(in.logTimeEnd.substring(0, 2)) * 10000;
                    } catch (Exception e) {
                        logger.warn(e, "Invalid Format, logTimeEnd = [", in.logTimeEnd, "]");
                    }
                }
                Object archivesDirectory = ESAPIUtil.toFile(CleanPathUtil.cleanString(logArchivesPath), CleanPathUtil.cleanString(in.logDate));
                // 取logArchivesPath/yyyy-MM-dd中的壓縮檔
                return getArchiveFileNames(archivesDirectory, beginTime, endTime);
            }
        } catch (Exception e) {
            logger.exceptionMsg(e, "Get AP Log Files with exception occur, ", in.toString());
        }
        return new ArrayList<>();
    }

    private static List<String> getArchiveFileNames(Object directory, int beginTime, int endTime) throws Exception {
        File[] children = ((File) directory).listFiles();
        if (ArrayUtils.isEmpty(children)) {
            logger.warn("[", ((File) directory).getAbsolutePath(), "] is empty!!!");
            new ArrayList<>();
        }
        List<String> fileNames = new ArrayList<>();
        for (Object child : children) {
            Calendar lastModified = CalendarUtil.clone(((File) child).lastModified());
            int childDate = Integer.parseInt(((File) child).getName().substring(11, 13)) * 10000;
            if (childDate < beginTime || childDate > endTime) {
                logger.warn("Time not match, [", childDate, "] not in [", beginTime, "~", endTime, "], directory = [", ((File) directory).getAbsolutePath(), "], file = [", ((File) child).getAbsolutePath(), "]");
                continue;
            }
            fileNames.add(((File) child).getName());
        }
        return fileNames;
    }
}