package com.syscom.fep.frmcommon.net.ftp;

import com.syscom.fep.frmcommon.util.CleanPathUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * FTP處理抽象類
 *
 * @author Richard
 */
public abstract class FtpAdapter {
    protected FtpProperties ftpProperties;

    /**
     * 構建函式
     *
     * @param ftpProperties
     */
    public FtpAdapter(FtpProperties ftpProperties) {
        this.ftpProperties = ftpProperties;
    }

    /**
     * 創建遠程目錄
     *
     * @param basePath 基礎目錄
     * @param folder   待創建目錄名稱
     * @return
     */
    public abstract boolean createRemotePath(String basePath, String folder);

    /**
     * 下載檔案
     *
     * @param local  本地檔案路徑
     * @param remote 遠程檔案路徑
     * @return
     */
    public abstract boolean download(String local, String remote);

    /**
     * 獲取檔案列表
     *
     * @param remote             遠程目錄路徑
     * @param mask               用於模糊檔案查詢, 例如GibCobolConverter*, 就會找出檔案名含有GibCobolConverter字符串的所有檔案, 不傳入此參數則取所有的檔案名包括目錄名
     * @param isExcludeDirectory 可選參數, 是否不包括檔案夾
     * @return
     */
    public abstract List<String> getFileList(String remote, String mask, boolean... isExcludeDirectory);

    /**
     * 檢查遠端目錄下的目錄是否存在
     *
     * @param basePath 基礎目錄
     * @param folder   目錄名稱
     * @return
     */
    public abstract boolean isRemotePathExist(String basePath, String folder);

    /**
     * 上傳檔案
     *
     * @param local                   待上傳本地檔案
     * @param remote                  上傳後遠程檔案
     * @param isRemoteExcludeFilename remote中是否不包含檔案名
     * @return
     */
    public abstract boolean upload(String local, String remote, boolean... isRemoteExcludeFilename);

    /**
     * 刪除遠程檔案
     *
     * @param remote 遠程檔案所在路徑
     * @return
     */
    public abstract boolean delete(String remote);

    /**
     * 斷開連線
     *
     * @return
     */
    public abstract boolean disconnect();

    /**
     * 根據basePath和folder, 獲取遠程目錄
     *
     * @param basePath
     * @param folder
     * @return
     */
    protected String getRemotePath(String basePath, String folder) {
        basePath = this.getDirectoryPath(basePath);
        String path = basePath;
        if (StringUtils.isNotBlank(folder)) {
            File file = new File(CleanPathUtil.cleanString(basePath), CleanPathUtil.cleanString(folder));
            path = file.getPath();
        }
        path = StringUtils.replace(path, "\\", "/");
        return path.startsWith("/") ? path : StringUtils.join("/", path);
    }

    /**
     * 如果basePath是ftp://domain:port/directory的格式, 則取出/directory
     *
     * @param basePath
     * @return
     */
    private String getDirectoryPath(String basePath) {
        try {
            URL url = new URL(basePath);
            return url.getPath();
        } catch (MalformedURLException e) {
            return basePath;
        }
    }
}