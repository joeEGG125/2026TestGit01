package com.syscom.fep.batch.task.cmn;

import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.task.TaskBase;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.frmcommon.ref.RefString;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArchivingLogFile extends TaskBase {
    private String _programName = ArchivingLogFile.class.getSimpleName();
    //    private BatchJobLibrary _job = null;
    private String _BatchLogPath = StringUtils.EMPTY;
    private String archiveFileOnly = null;// 2024-05-15新增 archiveFileOnly
    private String deleteArchiveOnly = null; // 只刪除過期的gz檔
    private LogData _logData = null;
    // 批次相關參數
    private String sourceDir = StringUtils.EMPTY;// 備份的來源目錄
    private String targetDir = StringUtils.EMPTY;// 備份的目的目錄
    private Integer archiveDay = 0;// 壓縮幾天前的Log檔
    private Integer reserveDay = 0;// 保留幾天壓縮檔
    private Boolean callBatchJob = true;// 用來指出是否要與Batch Job Service互動

    private String destPath = StringUtils.EMPTY;// 壓縮目錄
    private String sourcePath = StringUtils.EMPTY;// 壓縮原目錄
    private String destinationTarGzFile = StringUtils.EMPTY;// 壓縮詳細目錄
    private String compressTime = "00";//
    private Integer Maxtarsize = 500;
    private Integer Maxfilesize = 100; //原始檔案最大100MB, 超過不壓縮直接刪除
    private int destCount = 0; //
    private String webUrl = "";
    private int webTimeout = 30000;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH");
    private List<ArchiveFile> archiveFiles = new ArrayList<ArchiveFile>();

    private boolean comStat = false; // 壓縮狀態

    public static void main(String[] args) {
        new ArchivingLogFile().executeMain(args);
    }

    /**
     * 執行
     *
     * @param args
     * @param resultMessage
     * @return
     * @throws Exception
     */
    @Override
    protected boolean process(String[] args, RefString resultMessage) throws Exception {
        // 初始化相關批次物件及拆解傳入參數
        if (initialBatch(args)) {
            // 執行批次主要工作
            Process();
            return true;
        }
        return false;
    }

//    /**
//     * 將來源資料夾下的檔案在指定天數以前的檔壓縮備份至目的資料夾,並刪除目的資料夾保留天數之前的壓縮檔
//     * 傳入參數:
//     * SourceDir-來源目錄
//     * TargetDir-目的目錄,可以加上日期格式化字元例如{0:yyyyMMdd}做為目錄名稱一部分
//     * ReserveDay-保留天數
//     *
//     * @param args args
//     */
//    @Override
//    public BatchReturnCode execute(String[] args) {
//        File jarFile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
//        try {
//            // 初始化相關批次物件及拆解傳入參數
//            initialBatch(args);
//            // 2. 檢核批次參數是否正確, 若正確則啟動批次工作
//            log("------------------------------------------------------------------");
//            log(_programName + "開始");
//            // 回報批次平台開始工作
//            if (callBatchJob)
//                _job.startTask();
//            // 執行批次主要工作
//            Process();
//            System.out.println("-------------Finished Process()---------------");
//            log(_programName + "正常結束!");
//            // 回報批次平台結束工作
//            if (callBatchJob)
//                _job.endTask();
//            // 將此執行結果寫至TWSLOG
//            // _job.InsertTWSLog( //
//            // FEPConfig.getInstance().getHostName(), //
//            // Integer.valueOf(BatchResult.Successful.getValue()).byteValue(), //
//            // _programName, //
//            // jarFile.getName() //
//            // );
//            return BatchReturnCode.Succeed;
//        } catch (Exception ex) {
//            if (_job != null) {
//
//                // 回報批次平台工作失敗,暫停後面流程
//                try {
//                    if (callBatchJob)
//                        _job.abortTask();
//                } catch (Exception e) {
//                    log(_programName + "失敗!");
//                }
//                log(_programName + "失敗!");
//                log(ex.toString());
//                _logData.setProgramException(ex);
//                // 不得直接呼叫FEPBase.SendEMS
//                BatchJobLibrary.sendEMS(_logData);
//            }
//            // 將此執行結果寫至TWSLOG
//            // _job.InsertTWSLog( //
//            // FEPConfig.getInstance().getHostName(), //
//            // Integer.valueOf(BatchResult.Failed.getValue()).byteValue(), //
//            // _programName, //
//            // jarFile.getName() //
//            // );
//            return BatchReturnCode.ProgramException;
//        } finally {
//            if (_job != null) {
//                log(_programName + "結束!!");
//                log("------------------------------------------------------------------");
//                _job.dispose();
//                _job = null;
//            }
//            if (_logData != null) {
//                _logData = null;
//            }
//        }
//    }

    private boolean initialBatch(String[] args) {
//        _logData = new LogData();
//        _logData.setChannel(FEPChannel.BATCH);
//        _logData.setEj(0);
//        _logData.setProgramName(_programName);
//        // 2023-10-05 Richard add 如果沒有傳入BatchLogPath, 則需要從db中獲取設定值
//        if (Arrays.stream(args).noneMatch(t -> StringUtils.startsWithIgnoreCase(t, "/BatchLogPath"))) {
//            _BatchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
//        }
//        if (StringUtils.isBlank(_BatchLogPath)) {
//            _BatchLogPath = "/fep/logs";
//        }
//        // 初始化BatchJob物件,傳入工作執行參數
//        _job = new BatchJobLibrary(this, args, _BatchLogPath);
//        // 顯示help說明
//        if (getArguments().containsKey("?")) {
//            DisplayUsage();
//            return;
//        }
//        // 檢查目錄
//        if (StringUtils.isBlank(getArguments().get("BatchLogPath"))) {
//            System.out.println("Batch Log目錄未設定，請修正");
//            return;
//        }
        if (getArguments().containsKey("deleteArchiveOnly")) {
            deleteArchiveOnly = getArguments().get("deleteArchiveOnly");
        }
        // 拆解傳入的參數並存入變數
        if (getArguments().containsKey("SourceDir")) {
            sourceDir = getArguments().get("SourceDir");
        } else {
            System.out.println("必須傳入參數SourceDir");
            this.log(Level.SEVERE, "必須傳入參數SourceDir");
            return false;
        }
        if (getArguments().containsKey("TargetDir")) {
            targetDir = getArguments().get("TargetDir");
        } else {
            System.out.println("必須傳入參數TargetDir");
            this.log(Level.SEVERE, "必須傳入參數TargetDir");
            return false;
        }
        if (getArguments().containsKey("ReserveDay")) {
            reserveDay = Integer.parseInt(getArguments().get("ReserveDay"));
        }
        if (getArguments().containsKey("ArchiveDay")) {
            archiveDay = Integer.parseInt(getArguments().get("ArchiveDay"));
        } else {
            System.out.println("必須傳入參數ArchiveDay");
            this.log(Level.SEVERE, "必須傳入參數ArchiveDay");
            return false;
        }
        if (getArguments().containsKey("CallBatchJob")) {
            callBatchJob = Boolean.parseBoolean(getArguments().get("CallBatchJob"));
        }
        // 2024-05-15 新增 ArchiveFileOnly
        if (getArguments().containsKey("ArchiveFileOnly")) {
            archiveFileOnly = getArguments().get("ArchiveFileOnly");
        }
        if (getArguments().containsKey("WebUrl") && getArguments().containsKey("WebPort")) {
            String url = getArguments().get("WebUrl");
            int port = -1;
            try {
                port = Integer.parseInt(getArguments().get("WebPort"));
            } catch (NumberFormatException e) {
                log(Level.WARNING, e, "Invalid Parameter 'WebPort'");
            }
            if (StringUtils.isNotBlank(url) && port > 0) {
                String protocol = getArguments().get("WebProtocol");
                if (StringUtils.isBlank(protocol)) {
                    protocol = "https";
                }
                webUrl = protocol + "://";
                webUrl += url + ":" + port;
                webUrl += getArguments().getOrDefault("WebContextPath", StringUtils.EMPTY);
            }
        }
        try {
            webTimeout = Integer.parseInt(getArguments().getOrDefault("WebTimeout", Integer.toString(webTimeout)));
        } catch (NumberFormatException e) {
            log(Level.WARNING, e, "Invalid Parameter 'WebTimeout'");
        }
        if (webTimeout <= 0) {
            webTimeout = 30000;
        }
        if (getArguments().containsKey("Maxtarsize")) {
            Maxtarsize = Integer.parseInt(getArguments().get("Maxtarsize"));
        }
        if (getArguments().containsKey("Maxfilesize")) {
            Maxfilesize = Integer.parseInt(getArguments().get("Maxfilesize"));
        }
        Maxfilesize = Maxfilesize * 1024 * 1024;
        return true;
    }

    private void Process() throws Exception {
        // 新增webUrl 如果未輸入則不調用web service
        // 2024/12/09 xingyun add WebProt程式引數 如 /WebUrl:127.0.0.1 /WebPort:8081
        if (StringUtils.isNotBlank(webUrl)) {
            StringBuilder sb = new StringBuilder();
            sb.append(webUrl)
                    .append("/WebUtils/ArchivingLogFile")
                    .append("?").append("sourceDir=").append(sourceDir)
                    .append("&").append("targetDir=").append(targetDir)
                    .append("&").append("archiveDay=").append(archiveDay)
                    .append("&").append("reserveDay=").append(reserveDay)
                    .append("&").append("archiveFileOnly=").append(archiveFileOnly)
                    .append("&").append("maxTarSize=").append(Maxtarsize);
            String url = sb.toString();
            // send Restful request
            RestTemplate restTemplate = new RestTemplate();
            try {
                restTemplate.setRequestFactory(HttpClientConfiguration.createHttpComponentsClientHttpRequestFactory(webTimeout));
            } catch (Exception e) {
                log(Level.SEVERE, e, "SSL驗證失敗");
                throw e;
            }
            log("Begin execute WebApi:" + url);
            byte[] bytes = restTemplate.getForObject(HttpClient.toUriString(url), byte[].class);
            log("WebApi執行成功");
        } else {
            // if (ArchiveFolder()) {
            if (deleteArchiveOnly != null && deleteArchiveOnly.equals("Y")) {
                // 只刪除過期的gz檔
                DeleteArchiveFile();
            } else {
                if (ProcessArchive()) {
                    // 壓縮成功後刪除保留日期之前的壓縮檔
                    if (reserveDay > 0) {
                        log(
                                String.format("Begin delete earlier than %s days archive file", reserveDay.toString()));
                        DeleteArchiveFile();
                    }
                }
            }
        }
        // 2024-05-15新增ArchiveFile()
        if ("Y".equals(archiveFileOnly)) {
            if (ArchiveFile()) {
                // 壓縮成功後刪除保留日期之前的壓縮檔
                if (reserveDay > 0) {
                    log(
                            String.format("Begin delete earlier than %s days archive file", reserveDay.toString()));
                    DeleteArchiveFile();
                }
            }
        }
    }

    private Boolean ProcessArchive() {
        Maxtarsize = Maxtarsize * 1024 * 1024;
        File sourceDirFile = new File(sourceDir);
        File[] paths = sourceDirFile.listFiles();
        ArrayList<File> fileList = new ArrayList<File>(); // 符合處理的資料夾
        // 壓縮幾天前Log檔日期
        LocalDateTime archiveDate = LocalDateTime.now().minusDays(archiveDay);
        String begindate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(archiveDate);
        for (File path : paths) {
            String fname = path.getName(); // 取出日期名稱
            // 不符合log目錄格式(YYYY-MM-DD) 且 大於archiveDay指定日期，不處理
            if (!checkName(fname) || fname.compareTo(begindate) > 0) {
                continue;
            }
            fileList.add(path);
            String archiveName = path.getName();
            File destFile = new File(targetDir + File.separator + fname);
            if (!destFile.exists()) {
                log("建立資料夾-" + destFile.getAbsolutePath());
                destFile.mkdirs();
            }

            addFilesToArchiveList(path, archiveName);
        }
        try {
            log("準備處理符合條件的壓縮檔數-" + archiveFiles.size());
            // 壓縮檔案
            CompressFolder();
            // 清空已Archive後的空目錄
            for (File path : fileList) {
                deleteEmptyFolders(path);
            }
            return true;
        } catch (IOException e) {
            log("Compress file failed:" + e.getMessage());
        }
        return false;
    }

    private void addFilesToArchiveList(File file, String archiveName) {
        if (file == null) {
            return;
        }
        if (file.isFile() && (file.getName().endsWith("log") || file.getName().endsWith("txt"))) {
            long modifiedTime = file.lastModified();

            if (file.length() > Maxfilesize) {
                file.delete();
                log("刪除檔案:" + file.getAbsolutePath() + "，因為檔案大小超過" + Maxfilesize);
                return;
            }
            // 依小時壓縮檔案
            String fileHour = sdf.format(new Date(modifiedTime));
            // String arName = String.format("backup.tar.%s.gz", fileHour);

            ArchiveFile archiveFile = getArchiveFileByHour(fileHour, archiveName, archiveFiles);
            archiveFile.AddFile(file);
            //log("加入待處理清單:" + file.getAbsolutePath());
        } else if (file.isDirectory()) {
            if (file.listFiles() == null) {
                return;
            }
            for (File child : file.listFiles()) {
                addFilesToArchiveList(child, archiveName);
            }
        }
    }

    private ArchiveFile getArchiveFileByHour(
            String hour, String archiveName, List<ArchiveFile> files) {

        ArchiveFile archiveFile = null;
        int index = -1;
        for (ArchiveFile f : files) {
            if (f.fileName.substring(0, 10).equals(archiveName) &&
                    f.getFileHour().equals(hour)) {
                // 如果檔案大小超過最大值，則建立新的檔案
                if (f.getFileSize() <= Maxtarsize) {
                    return f;
                } else {
                    index = f.getFileIndex();
                }
            }
        }

        if (archiveFile == null && index == -1) {
            archiveFile = new ArchiveFile(targetDir + File.separator + archiveName,
                    String.format("%s-%s00-%d.tar.gz", archiveName, hour, 0), hour, 0);
            archiveFiles.add(archiveFile);
        } else {
            archiveFile = new ArchiveFile(targetDir + File.separator + archiveName,
                    String.format("%s-%s00_%d.tar.gz", archiveName, hour, index + 1), hour, index + 1);
            archiveFiles.add(archiveFile);
        }
        return archiveFile;
    }

    private void CompressFolder() throws IOException {
        int i = 0;
        for (ArchiveFile archiveFile : archiveFiles) {
            log(String.format("開始壓縮檔案-%d:%s", i + 1, archiveFile.getArchiveFullPath()));
            try (
                    FileOutputStream fos = new FileOutputStream(archiveFile.getArchiveFullPath());
                    BufferedOutputStream bos = new BufferedOutputStream(fos, 64 * 1024);
                    GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
                    TarArchiveOutputStream taos = new TarArchiveOutputStream(gzos)) {
                taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX); // 2026-06-04 Richard add 解決文件名過長的問題

                for (File file : archiveFile.getFiles().values()) {
                    String path = "";

                    if (file.getParentFile().getName().equals(archiveFile.getArchiveFileName().substring(0, 10))) {
                        path = file.getName();
                    } else {
                        path = file.getParentFile().getName() + "/" + file.getName();
                    }
                    TarArchiveEntry entry = new TarArchiveEntry(file, path);
                    taos.putArchiveEntry(entry);

                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[64 * 1024];
                        int count;
                        while ((count = fis.read(buffer)) != -1) {
                            taos.write(buffer, 0, count);
                        }
                    }

                    taos.closeArchiveEntry();
                }
                log(String.format("壓縮檔案完成-%d:%s", i + 1, archiveFile.getArchiveFullPath()));
                // 為了避免檔案太多, 壓一個壓縮檔就砍相對應的檔案
                int iDeleted = 0;
                for (File file : archiveFile.getFiles().values()) {
                    if (!file.delete()) {
                        log("刪除檔案失敗:" + file.getAbsolutePath());
                    } else {
                        log("刪除檔案成功:" + file.getAbsolutePath());
                        iDeleted++;
                    }
                }
                log("刪除已加入" + archiveFile.getArchiveFileName() + "內的檔案完成,總共應刪除" +
                        archiveFile.getFileCount() + "個檔案,實際刪除" + iDeleted + "個檔案");
            }
            i++;
        }

    }

    public boolean deleteEmptyFolders(File folder) {
        // 檢查是否為資料夾
        if (!folder.isDirectory()) {
            return false;
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 遞迴處理子資料夾
                    deleteEmptyFolders(file);
                } else if (file.isFile() && file.length() == 0) {
                    // 刪除 0 byte 檔案
                    log("刪除 0 byte檔案: " + file.getAbsolutePath());
                    file.delete();
                }
            }
        }

        // 再次取得內容，因為可能子資料夾已被刪除
        files = folder.listFiles();
        if (files != null && files.length == 0) {

            Boolean result = folder.delete();
            log("刪除資料夾: " +
                    folder.getAbsolutePath() + " " + (result ? "成功" : "失敗"));

        } else {
            log(folder.getName() + " 不是空資料夾,無法刪除");
        }

        return false;
    }

    // 2024-05-15針對BatchInputFile打包壓縮
    private Boolean ArchiveFile() {
        // BatchInput檔目錄
        File sourceDirFile = new File(sourceDir);
        File[] paths = sourceDirFile.listFiles();
        // 壓縮幾天前Log檔日期
        LocalDateTime archiveDate = LocalDateTime.now().minusDays(archiveDay);
        String begindate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(archiveDate);
        List<File> toCompressFile = new ArrayList<>();

        // 如果最後修改日期小於幾天前
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
        // 壓縮資料
        // 7天前然後再前1日的日期
        Path sourcePath = Paths.get(sourceDir);
        String sourceFolder = sourcePath.getFileName().toString();
        // 壓縮檔案後的資料夾名稱為 "當前資料夾的名稱.tar.gz"
        Path dest = Paths.get(targetDir, begindate + "-" + sourceFolder + ".tar.gz");
        try {
            // 壓縮檔案
            compressFiles(toCompressFile, sourcePath.toString(), dest.toString());

        } catch (IOException e) {
            log(Level.SEVERE, e, "Compress file failed:" + e.getMessage());
            // e.printStackTrace();
        }
        return true;
    }

    // private Boolean ArchiveFolder() {
    // // Log檔目錄
    // File sourceDirFile = new File(sourceDir);
    // File[] paths = sourceDirFile.listFiles();
    // // 壓縮幾天前Log檔日期
    // LocalDateTime archiveDate = LocalDateTime.now().minusDays(archiveDay);
    // String begindate =
    // DateTimeFormatter.ofPattern("yyyy-MM-dd").format(archiveDate);

    // for (File path : paths) {
    // String fname = path.getName();
    // // 不符合log目錄格式(YYYY-MM-DD) 且 大於archiveDay指定日期，不處理
    // if (!checkName(fname) || fname.compareTo(begindate) > 0) {
    // continue;
    // }
    // Path source = Paths.get(sourceDir, path.getName());
    // Path dest = Paths.get(targetDir, path.getName());
    // try {
    // destPath = dest.toString();
    // File destFile = new File(destPath);
    // if (!destFile.exists()) {
    // log("創建檔案夾" + destPath);
    // destFile.mkdirs();
    // }
    // destPath = destPath + "/" + fname;
    // sourcePath = source.toString();
    // // 壓縮檔案 按小時
    // for (int i = 0; i <= 24; i++) {
    // File tarGzFile = new File(destinationTarGzFile);
    // if (tarGzFile.exists()) {
    // if (tarGzFile.length() == 29) {
    // tarGzFile.delete();
    // }
    // }
    // if (i == 24) {
    // break;
    // }
    // if (i < 10) {
    // compressTime = "0" + i;
    // } else {
    // compressTime = String.valueOf(i);
    // }
    // compressFolder();
    // }
    // // 刪除Log檔目錄
    // // path.delete(); // 只能刪除空資料夾
    // if (comStat) {
    // FileUtils.forceDelete(path);
    // }
    // } catch (IOException e) {
    // comStat = false;
    // log("Compress folder failed:" + e.getMessage());
    // // e.printStackTrace();
    // }
    // }
    // return true;
    // }

    // private Boolean ArchiveFiles() {
    // Boolean bRes = false;
    // File f = new File(sourceDir);
    // FilenameFilter fileNameFilter = new FilenameFilter() {
    // @Override
    // public boolean accept(File dir, String name) {
    // if (name.lastIndexOf('.') > 0) {
    // int lastIndex = name.lastIndexOf('.');// get last index for '.' char
    // String str = name.substring(lastIndex);// get extension
    // if (str.equals(".txt"))
    // return true;// match path name extension
    // }
    // return false;
    // }
    // };
    // // 取得目錄下所有的.txt檔案
    // File[] paths = f.listFiles(fileNameFilter);

    // SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    // ArrayList<String> rarFileName = new ArrayList<>();// 存放欲產生的壓縮檔
    // ArrayList<String> rarFiles = new ArrayList<>();// 存放欲壓縮的檔案清單,以";"區隔,存放多個檔名
    // // 壓縮檔規則:將檔案之最後修改時間在指定日期之前的檔案,依主要檔名(不含日期)壓成一個檔
    // try {
    // // for each pathname in pathname array
    // for (File path : paths) {
    // // prints file and directory paths
    // long lastmodifydate = path.lastModified();
    // Date filedate = new Date();
    // filedate.setTime(lastmodifydate);
    // long lmodifyday = Long.valueOf(format.format(filedate)).longValue();//
    // 取得檔案的最後編輯日期
    // long lastArchiveday = getDate(archiveDay);// 取得指定壓縮天數的日期
    // // 判斷檔案最後編輯日期是否小於等於欲壓縮的天數日期
    // if (lmodifyday <= lastArchiveday) {
    // String name = path.getName();// 取得欲壓縮之檔名(主要檔名(不含日期)壓成一個檔)
    // Integer seq = name.indexOf("_");
    // if (seq > -1)
    // name = name.substring(0, 0 + seq);
    // else {
    // seq = name.indexOf(".");
    // name = name.substring(0, 0 + seq);
    // }
    // String desPath = targetDir + File.separator + format.format(filedate);//
    // 目的路徑= targetDir + \\ +
    // // 檔案的最後編輯日期(ex:
    // // d:\\feplog\backup\20220512)
    // File file = new File(desPath);// 此為壓縮檔目的地目錄
    // if (file.exists() == false) {
    // if (file.mkdirs()) {// 此為壓縮檔目的地目錄,目錄不存在時,建立目錄
    // log("無符合條件的檔案可供壓縮");
    // }
    // }

    // file = new File(desPath, name + ".zip");// 此為壓縮檔
    // if (rarFileName.contains(file.getPath()) == false) {
    // rarFileName.add(file.getPath());// 記錄壓縮檔名
    // rarFiles.add(path.getPath());// 記錄欲壓縮之清案清單
    // } else {
    // // 記錄欲壓縮之清案清單
    // for (int x = 0; x < rarFileName.size(); x = x + 1) {
    // if (rarFileName.get(x).equalsIgnoreCase(file.getPath())) {
    // String files = rarFiles.get(x);
    // files += ";" + path.getPath();
    // rarFiles.set(x, files);
    // }
    // }
    // }
    // }
    // }
    // if (rarFileName.size() == 0) {
    // log("無符合條件的檔案可供壓縮");
    // return true;
    // }
    // for (int x = 0; x < rarFileName.size(); x = x + 1) {
    // String rarName = rarFileName.get(x);
    // String[] rarfiles = rarFiles.get(x).split(";");
    // File[] aryFiles = new File[rarfiles.length];
    // int i = 0;
    // try {
    // for (String aa : rarfiles) {
    // File f1 = new File(aa);
    // aryFiles[i] = f1;
    // i = i + 1;
    // }
    // log(String.format("開始壓縮{0},來源檔案{1}", rarName, rarFiles.get(x)));
    // if (CompressionUtil.compressFiles2Zip(aryFiles, rarName))// 壓縮檔案
    // {
    // for (File f1 : aryFiles) {
    // if (f1.delete()) {// 刪除壓縮來源檔
    // log("刪除失敗");
    // }
    // }
    // }
    // log(String.format("壓縮完成{0}", rarName));
    // } catch (Exception ex) {
    // log(String.format("壓縮失敗{0},原因:{1}", rarName, ex.getMessage()));
    // }
    // }
    // bRes = true;
    // } catch (Exception ex) {
    // log(ex.getMessage());
    // }
    // return bRes;
    // }

    public long getDate(int day) {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(calendar.DATE, -1 * day);
        date = calendar.getTime();
        long ldate = Long.valueOf(format.format(date)).longValue();
        return ldate;
    }

    public static Boolean checkName(String folder) {
        Pattern pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(folder);
        return matcher.find();
    }

    private void DeleteArchiveFile() {
        // 壓縮檔目錄
        File targetDirFile = new File(targetDir);
        File[] paths = targetDirFile.listFiles();
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
                // path.delete(); // 只能刪除空資料夾
                FileUtils.forceDelete(path);
                log(String.format("Delete archive folder %s complete", path.getName()));
            }
        } catch (IOException e) {
            log(Level.SEVERE, e, "Compress folder failed:" + e.getMessage());
            // e.printStackTrace();
        }
    }

    public void compressFolder() throws IOException {
        log(String.format("Begin compress folder %s", sourcePath));
        destCount = 0;
        destinationTarGzFile = destPath + "-" + compressTime + "00-" + destCount + ".tar.gz";
        try (FileOutputStream fos = new FileOutputStream(destinationTarGzFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
             TarArchiveOutputStream tos = new TarArchiveOutputStream(gzos);) {
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX); // 2026-06-04 Richard add 解決文件名過長的問題
            addFolderToTar("", sourcePath, tos);
        } catch (IOException e) {
            comStat = false;
            log(Level.SEVERE, e, "Compress folder failed:" + e.getMessage());
            // e.printStackTrace();
        }
        comStat = true;
        log(String.format("%s archive to %s completed", sourcePath, destinationTarGzFile));
    }

    private void addFolderToTar(String parent, String folder, TarArchiveOutputStream tos) throws IOException {
        File dir = new File(folder);
        String[] files = dir.list();

        if (files == null) {
            return;
        }

        for (String file : files) {
            String path = folder + "/" + file;
            File f = new File(path);
            if (f.isDirectory()) {
                addFolderToTar(parent + f.getName() + "/", path, tos);
            } else {
                if (f.length() != 0) {
                    long modifiedTime = f.lastModified();
                    Date date = new Date(modifiedTime);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH");
                    String formattedDate = sdf.format(date);

                    File tarGzFile = new File(destinationTarGzFile);
                    double tarGzFileSize = tarGzFile.length() / 1024.0 / 1024.0;
                    if (tarGzFileSize > Maxtarsize) {
                        destCount++;
                        destinationTarGzFile = destPath + "-" + compressTime + "00-" + destCount + ".tar.gz";
                        try (FileOutputStream fos = new FileOutputStream(destinationTarGzFile);
                             BufferedOutputStream bos = new BufferedOutputStream(fos);
                             GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);) {
                            tos.close();
                            tos = new TarArchiveOutputStream(gzos);
                            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX); // 2026-06-04 Richard add 解決文件名過長的問題
                            addFolderToTar("", sourcePath, tos);
                        } catch (IOException e) {
                            comStat = false;
                            log(Level.SEVERE, e, "Compress folder failed:" + e.getMessage());
                        }
                    } else {
                        if (compressTime.equals(formattedDate)) {
                            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));) {
                                TarArchiveEntry tarEntry = new TarArchiveEntry(path);
                                tarEntry.setName(parent + f.getName());
                                tarEntry.setSize(f.length());
                                tos.putArchiveEntry(tarEntry);
                                byte[] buffer = new byte[1024];
                                int count;
                                while ((count = bis.read(buffer)) != -1) {
                                    tos.write(buffer, 0, count);
                                }
                            } catch (Exception e) {
                                comStat = false;
                                log(Level.SEVERE, e, "addFolder to tar failed:" + e.getMessage());
                            }
                            tos.closeArchiveEntry();
                            // FileUtils.forceDelete(f);
                        }
                    }
                }
            }
        }
    }

    // 2024-05-15新增 ArchiveInputPath 的 Compress方法
    private void compressFiles(List<File> toCompressFiles, String sourcePath, String destinationTarGzFile)
            throws IOException {
        log(String.format("Begin compress files in %s", sourcePath));
        try (FileOutputStream fos = new FileOutputStream(destinationTarGzFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
             TarArchiveOutputStream tos = new TarArchiveOutputStream(gzos);) {
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX); // 2026-06-04 Richard add 解決文件名過長的問題
            for (File file : toCompressFiles) {
                addFileToTar(file, tos);
                log(String.format(" --Compressed file finished: %s/%s", file.getPath(), file.getName()));
                // 2024-05-15刪除原位置檔案
                FileUtils.forceDelete(file);
            }
        }
    }

    // 2024-05-15新增 ArchiveInputPath 的 addFileToTar方法
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
            log(Level.SEVERE, e, "addFile to tar failed:" + e.getMessage());
        }
    }

    private String getFileNameWithoutExtension(String fileName) {
        int pos = fileName.indexOf(".");
        if (pos > 0 && pos < (fileName.length() - 1))
            fileName = fileName.substring(0, pos);
        return fileName;

    }

    @Override
    public void displayUsage() {
        System.out.println("USAGE:");
        System.out.println(" ArchivingLogFile Options");
        System.out.println();
        System.out.println(" Options:");
        System.out.println(" /? Display this help message.");
        System.out.println(" /SourceDir Required.");
        System.out.println(" /TargetDir Required");
        System.out.println(" /ReserveDay Optional.");
        System.out.println(" /ArchiveDay Required.");
        System.out.println(" /CallBatchJob Optional, true or false, default true");
        System.out.println(" /BatchLogPath Optional");
        System.out.println(" /Maxtarsize 500");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println(" ArchivingLogFile /SourceDir:G:\\FEP10\\Log /TargetDir:G:\\FEPLog\\Backup\\ /ArchiveDay:2 /ReserveDay:7 /WebUrl:127.0.0.1 /WebPort:8081");
    }

    class ArchiveFile {
        private String fileName;

        private int fileSize;
        private String dest;
        private String fileHour;
        private Map<String, File> fileContent = new TreeMap<String, File>();
        private int fileIndex = 0;

        public ArchiveFile(String archiveDest, String name, String hour, int index) {
            fileName = name;
            fileHour = hour;
            fileIndex = index;
            dest = archiveDest + File.separator + fileName;
        }

        public void AddFile(File file) {
            fileContent.put(file.getAbsolutePath(), file);
            fileSize += file.length();
        }

        public void RemoveFile(String file) {
            fileContent.remove(file);
        }

        public int getFileCount() {
            return fileContent.size();
        }

        public int getFileSize() {
            return fileSize;
        }

        public String getArchiveFileName() {
            return fileName;
        }

        public String getFileHour() {
            return fileHour;
        }

        public String getArchiveFullPath() {
            return dest;
        }

        public int getFileIndex() {
            return fileIndex;
        }

        public void setFileHour(String hour) {
            fileHour = hour;
        }

        public Map<String, File> getFiles() {
            return fileContent;
        }
    }

}
