package com.syscom.fep.web.controller;

import com.syscom.fep.frmcommon.esapi.ESAPIUtil;
import com.syscom.fep.frmcommon.esapi.ESAPIValidator;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class ArchiveFileController extends BaseController {
    private String archiveFileOnly = null;// 2024-05-15新增 archiveFileOnly
    // 批次相關參數
    private String sourceDir = StringUtils.EMPTY;// 備份的來源目錄
    private String targetDir = StringUtils.EMPTY;// 備份的目的目錄
    private Integer archiveDay = 0;// 壓縮幾天前的Log檔
    private Integer reserveDay = 0;// 保留幾天壓縮

    private String destPath = StringUtils.EMPTY;// 壓縮目錄
    private String sourcePath = StringUtils.EMPTY;// 壓縮原目錄
    private String destinationTarGzFile = StringUtils.EMPTY;// 壓縮詳細目錄
    private String compressTime = "00";//
    private Integer Maxtarsize = 500;
    private int destCount = 0; //
    private boolean comStat = false; //壓縮狀態
    private LogHelper log = new LogHelper();

    @RequestMapping(value = "/WebUtils/ArchivingLogFile", method = RequestMethod.GET)
    @ResponseBody
    public byte[] archivingLogFile(
            @RequestParam(value = "sourceDir", required = false, defaultValue = StringUtils.EMPTY) String sourceDir,
            @RequestParam(value = "targetDir") String targetDir,
            @RequestParam(value = "archiveDay") Integer archiveDay,
            @RequestParam(value = "reserveDay") Integer reserveDay,
            @RequestParam(value = "archiveFileOnly") String archiveFileOnly,
            @RequestParam(value = "maxTarSize") Integer maxTarSize
    ) {
        // 2025-01-24 Richard modified for 【Relative Path Traversal】
        this.sourceDir = ESAPIValidator.getValidDirectoryName(sourceDir);
        this.targetDir = targetDir;
        this.reserveDay = reserveDay;
        this.archiveDay = archiveDay;
        this.archiveFileOnly = archiveFileOnly;
        this.Maxtarsize = maxTarSize;
        log.info("Begin webApi ArchivingLogFile");
        if (ArchiveFolder()) {
            // 壓縮成功後刪除保留日期之前的壓縮檔
            if (this.reserveDay > 0) {
                log.info(String.format("Begin delete earlier than %s days archive file", this.reserveDay.toString()));
                DeleteArchiveFile();
            }
        }
        if ("Y".equals(archiveFileOnly)) {
            if (ArchiveFile()) {
                // 壓縮成功後刪除保留日期之前的壓縮檔
                if (this.reserveDay > 0) {
                    log.info(String.format("Begin delete earlier than %s days archive file", this.reserveDay.toString()));
                    DeleteArchiveFile();
                }
            }
        }
        return null;
    }

    private Boolean ArchiveFolder() {
        // Log檔目錄
        Object sourceDirFile = ESAPIUtil.toFile(CleanPathUtil.cleanString(sourceDir));
        File[] paths = ((File) sourceDirFile).listFiles();
        // 壓縮幾天前Log檔日期
        LocalDateTime archiveDate = LocalDateTime.now().minusDays(archiveDay);
        String begindate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(archiveDate);

        for (File path : paths) {
            String fname = path.getName();
            // 不符合log目錄格式(YYYY-MM-DD) 且 大於archiveDay指定日期，不處理
            if (!checkName(fname) || fname.compareTo(begindate) > 0) {
                continue;
            }
            Path source = Paths.get(sourceDir, path.getName());
            Path dest = Paths.get(targetDir, path.getName());
            try {
                destPath = dest.toString();
                File destFile = new File(destPath);
                if (!destFile.exists()) {
                    log.info("創建檔案夾" + destPath);
                    destFile.mkdirs();
                }
                destPath = destPath + "/" + fname;
                sourcePath = source.toString();
                // 壓縮檔案  按小時
                for (int i = 0; i <= 24; i++) {
                    if (i == 24) {
                        break;
                    }
                    if (i < 10) {
                        compressTime = "0" + i;
                    } else {
                        compressTime = String.valueOf(i);
                    }
                    compressFolder();

                }
                // 刪除Log檔目錄
//				path.delete(); // 只能刪除空資料夾
                if (comStat) {
                    FileUtils.forceDelete(path);
                }
                log.info(String.format("Delete archive file %s complete", path.getName()));
            } catch (IOException e) {
                comStat = false;
                log.info("Compress folder failed:" + e.getMessage());
            }
        }
        return true;
    }

    private void DeleteArchiveFile() {
        // 壓縮檔目錄
        Object targetDirFile = ESAPIUtil.toFile(targetDir);
        File[] paths = ((File) targetDirFile).listFiles();
        // 保留壓縮檔日期
        LocalDateTime reserveDate = LocalDateTime.now().minusDays(reserveDay);
        String begindate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(reserveDate);

        try {
            for (File path : paths) {
                String fname = getFileNameWithoutExtension(path.getName());
                // 壓縮檔日期大於保留壓縮檔日期，不處理
                if (fname.compareTo(begindate) >= 0) {
                    continue;
                }
                FileUtils.forceDelete(path);
                log.info(String.format("Delete archive file %s complete", path.getName()));
            }
        } catch (IOException e) {
            log.info("Compress folder failed:" + e.getMessage());
        }
    }

    private String getFileNameWithoutExtension(String fileName) {
        int pos = fileName.indexOf(".");
        if (pos > 0 && pos < (fileName.length() - 1))
            fileName = fileName.substring(0, pos);
        return fileName;

    }

    public Boolean checkName(String folder) {
        Pattern pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(folder);
        return matcher.find();
    }

    public void compressFolder() throws IOException {
        log.info(String.format("Begin compress folder %s", sourcePath));
        destCount = 0;
        destinationTarGzFile = destPath + "-" + compressTime + "00-" + destCount + ".tar.gz";
        Object tarGzFile = ESAPIUtil.toFile(destinationTarGzFile);
        if (((File) tarGzFile).exists()) {
            if (((File) tarGzFile).length() == 29) {
                ((File) tarGzFile).delete();
            }
        }
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(destinationTarGzFile)));
             GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
             TarArchiveOutputStream tos = new TarArchiveOutputStream(gzos);) {
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX); // 2026-06-04 Richard add 解決文件名過長的問題
            addFolderToTar("", sourcePath, tos);
        } catch (IOException e) {
            comStat = false;
            log.info("Compress folder failed:" + e.getMessage());
        }
        comStat = true;
        log.info(String.format("%s archive to %s completed", sourcePath, destinationTarGzFile));
    }

    private void addFolderToTar(String parent, String folder, TarArchiveOutputStream tos) throws IOException {
        Object dir = ESAPIUtil.toFile(folder);
        String[] files = ReflectUtil.envokeMethod(dir, "list", null);

        if (files == null) {
            return;
        }

        for (String file : files) {
            String path = folder + "/" + file;
            Object f = ESAPIUtil.toFile(path);
            if (((File) f).isDirectory()) {
                addFolderToTar(parent + ((File) f).getName() + "/", path, tos);
            } else {
                if (((File) f).length() != 0) {
                    long modifiedTime = ((File) f).lastModified();
                    Date date = new Date(modifiedTime);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH");
                    String formattedDate = sdf.format(date);

                    Object tarGzFile = ESAPIUtil.toFile(destinationTarGzFile);
                    double tarGzFileSize = ((File) tarGzFile).length() / 1024.0 / 1024.0;
                    if (tarGzFileSize > Maxtarsize) {
                        destCount++;
                        destinationTarGzFile = destPath + "-" + compressTime + "00-" + destCount + ".tar.gz";
                        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(destinationTarGzFile)));
                             GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);) {
                            tos.close();
                            tos = new TarArchiveOutputStream(gzos);
                            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX); // 2026-06-04 Richard add 解決文件名過長的問題
                            addFolderToTar("", sourcePath, tos);
                        } catch (IOException e) {
                            comStat = false;
                            log.info("Compress folder failed:" + e.getMessage());
                        }
                    } else {
                        if (compressTime.equals(formattedDate)) {
                            try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(((File) f).toPath()));) {
                                TarArchiveEntry tarEntry = new TarArchiveEntry(path);
                                tarEntry.setName(parent + ((File) f).getName());
                                tarEntry.setSize(((File) f).length());
                                tos.putArchiveEntry(tarEntry);
                                byte[] buffer = new byte[1024];
                                int count;
                                while ((count = bis.read(buffer)) != -1) {
                                    tos.write(buffer, 0, count);
                                }
                            } catch (Exception e) {
                                comStat = false;
                                log.info("addFolder to tar failed:" + e.getMessage());
                            }
                            tos.closeArchiveEntry();
                        }
                    }
                }
            }
        }
    }

    private Boolean ArchiveFile() {
        // BatchInput檔目錄
        Object sourceDirFile = ESAPIUtil.toFile(sourceDir);
        File[] paths = ((File) sourceDirFile).listFiles();
        // 壓縮幾天前Log檔日期
        LocalDateTime archiveDate = LocalDateTime.now().minusDays(archiveDay);
        String begindate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(archiveDate);
        List<File> toCompressFile = new ArrayList<>();

        //如果最後修改日期小於幾天前
        for (File file : paths) {
            long lastModifiedTime = file.lastModified();
            Date date = new Date(lastModifiedTime);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = simpleDateFormat.format(date);
            if (formattedDate.compareTo(begindate) > 0) {
                continue;
            }
            toCompressFile.add(file);
        }
        //壓縮資料
        //7天前然後再前1日的日期
        Path sourcePath = Paths.get(sourceDir);
        String sourceFolder = sourcePath.getFileName().toString();
        //壓縮檔案後的資料夾名稱為 "當前資料夾的名稱.tar.gz"
        Path dest = Paths.get(targetDir, begindate + "-" + sourceFolder + ".tar.gz");
        try {
            // 壓縮檔案
            compressFiles(toCompressFile, sourcePath.toString(), dest.toString());

        } catch (IOException e) {
            log.info("Compress file failed:" + e.getMessage());
//				e.printStackTrace();
        }
        return true;
    }

    //2024-05-15新增 ArchiveInputPath 的 Compress方法
    private void compressFiles(List<File> toCompressFiles, String sourcePath, String destinationTarGzFile) throws IOException {
        log.info(String.format("Begin compress files in %s", sourcePath));
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(destinationTarGzFile)));
             GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
             TarArchiveOutputStream tos = new TarArchiveOutputStream(gzos);) {
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX); // 2026-06-04 Richard add 解決文件名過長的問題
            for (File file : toCompressFiles) {
                addFileToTar(file, tos);
                log.info(String.format(" --Compressed file finished: %s/%s", file.getPath(), file.getName()));
                //2024-05-15刪除原位置檔案
                FileUtils.forceDelete(file);
            }
        }
    }

    private void addFileToTar(File file, TarArchiveOutputStream tos) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            TarArchiveEntry tarEntry = new TarArchiveEntry(file.getName());
            tarEntry.setSize(file.length());
            tos.putArchiveEntry(tarEntry);
            byte[] buffer = new byte[1024];
            int count;
            while ((count = bis.read(buffer)) != -1) {
                tos.write(buffer, 0, count);
            }
            tos.closeArchiveEntry();
        } catch (Exception e) {
            log.info("addFile to tar failed:" + e.getMessage());
        }
    }
}
