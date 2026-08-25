package com.syscom.fep.frmcommon.net.ftp;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CleanPathUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.file.remote.ClientCallback;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用於處理一般的ftp
 *
 * @author Richard
 */
public class FtpClient extends FtpAdapter {
    private LogHelper logger = new LogHelper();

    private FtpRemoteFileTemplate template;

    /**
     * 構建函式
     *
     * @param ftpProperties
     * @param beanFactory
     */
    public FtpClient(FtpProperties ftpProperties, BeanFactory beanFactory) {
        super(ftpProperties);
        // DefaultFtpSessionFactory
        DefaultFtpSessionFactory ftpSessionFactory = new DefaultFtpSessionFactory();
        ftpSessionFactory.setBufferSize(ftpProperties.getBufferSize());
        ftpSessionFactory.setClientMode(ftpProperties.getClientMode());
        ftpSessionFactory.setConnectTimeout(ftpProperties.getConnectTimeout());
        ftpSessionFactory.setControlEncoding(ftpProperties.getControlEncoding());
        ftpSessionFactory.setDataTimeout(ftpProperties.getDataTimeout());
        ftpSessionFactory.setDefaultTimeout(ftpProperties.getDefaultTimeout());
        ftpSessionFactory.setFileType(ftpProperties.getFileType());
        ftpSessionFactory.setHost(ftpProperties.getHost());
        ftpSessionFactory.setPassword(ftpProperties.getPassword());
        ftpSessionFactory.setPort(ftpProperties.getPort());
        ftpSessionFactory.setUsername(ftpProperties.getUsername());
        // CachingSessionFactory
        CachingSessionFactory<FTPFile> cachingSessionFactory = new CachingSessionFactory<FTPFile>(ftpSessionFactory, ftpProperties.getPoolSize());
        cachingSessionFactory.setSessionWaitTimeout(ftpProperties.getSessionWaitTimeout());
        cachingSessionFactory.setPoolSize(ftpProperties.getPoolSize());
        // FtpRemoteFileTemplate
        template = new FtpRemoteFileTemplate(cachingSessionFactory);
        template.setExistsMode(FtpRemoteFileTemplate.ExistsMode.NLST_AND_DIRS);
        template.setBeanFactory(beanFactory);
        template.setCharset(ftpProperties.getControlEncoding());
        logger.info("FtpClient is ready, ", ftpProperties.toString());
    }

    /**
     * 創建遠程目錄
     *
     * @param basePath 基礎目錄
     * @param folder   待創建目錄名稱
     * @return
     */
    @Override
    public boolean createRemotePath(String basePath, String folder) {
        boolean exists = this.isRemotePathExist(basePath, folder);
        if (exists) {
            logger.warn("No need create remote path cause remote path already exist, basePath = [", basePath, "], folder = [", folder, "]");
            return true;
        }
        try {
            final String remotePath = this.getRemotePath(basePath, folder);
            return template.executeWithClient(new ClientCallback<FTPClient, Boolean>() {
                @Override
                public Boolean doWithClient(FTPClient ftpClient) {
                    String[] directories = remotePath.split("/");
                    if (ArrayUtils.isNotEmpty(directories)) {
                        StringBuilder sb = new StringBuilder();
                        try {
                            for (String directory : directories) {
                                if (StringUtils.isBlank(directory)) {
                                    continue;
                                }
                                sb.append("/").append(directory);
                                if (!ftpClient.changeWorkingDirectory(directory)) {
                                    if (!ftpClient.makeDirectory(directory)) {
                                        logger.error("Make directory failed, directory = [", sb.toString(), "]");
                                        return false;
                                    }
                                    if (!ftpClient.changeWorkingDirectory(directory)) {
                                        logger.error("Change directory again failed, directory = [", sb.toString(), "]");
                                        return false;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            logger.error(e, "Change directory failed, directory = [", sb.toString(), "]");
                            return false;
                        } finally {
                            // 這裡要退回到主目錄下, 否則連續使用此方法會有問題
                            try {
                                ftpClient.changeWorkingDirectory("/");
                            } catch (IOException e) {
                                logger.warn("Back to main directory failed, error message = [", e.getMessage(), "]");
                            }
                        }
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            logger.error(e, "Create Remote path failed!! basePath = [", basePath, "], folder = [", folder, "]");
            return false;
        }
    }

    /**
     * 下載檔案
     *
     * @param local  本地檔案路徑
     * @param remote 遠程檔案路徑
     * @return
     */
    @Override
    public boolean download(String local, String remote) {
        boolean exists = this.isRemotePathExist(remote, null);
        if (!exists) {
            logger.warn("Cannot download cause file not exist, remote = [", remote, "]");
            return false;
        }
        File file = new File(CleanPathUtil.cleanString(local));
        try {
            FileUtils.forceMkdirParent(file);
        } catch (IOException e) {
            logger.error(e, e.getMessage());
            return false;
        }
        try {
            String remotePath = this.getRemotePath(remote, null);
            return template.get(remotePath, in -> {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    IOUtils.copy(in, fos);
                } catch (Exception e) {
                    logger.error(e, "Download remote file successful but copy to local failed!!");
                    throw e;
                }
            });
        } catch (Exception e) {
            logger.error(e, "Download failed!! remote = [", remote, "]");
            return false;
        }
    }

    /**
     * 獲取檔案列表
     *
     * @param remote             遠程目錄路徑
     * @param mask               用於模糊檔案查詢, 例如GibCobolConverter*, 就會找出檔案名含有GibCobolConverter字符串的所有檔案, 不傳入此參數則取所有的檔案名包括目錄名
     * @param isExcludeDirectory 可選參數, 是否不包括檔案夾
     * @return
     */
    @Override
    public List<String> getFileList(String remote, String mask, boolean... isExcludeDirectory) {
        boolean exists = this.isRemotePathExist(remote, null);
        if (!exists) {
            logger.warn("Cannot get file list cause remote path not exist, remote = [", remote, "]");
            return null;
        }
        try {
            String remotePath = this.getRemotePath(remote, null);
            FTPFile[] ftpFiles = template.list(remotePath);
            if (ArrayUtils.isNotEmpty(ftpFiles)) {
                List<String> list = new ArrayList<>();
                if (StringUtils.isNotBlank(mask)) {
                    int index = mask.indexOf("*");
                    if (index == -1) {
                        mask = StringUtils.join(mask, "*");
                        index = mask.indexOf("*");
                    }
                    String mask_ = mask.substring(0, index);
                    for (FTPFile ftpFile : ftpFiles) {
                        if (!"*.*".equals(mask.trim()) && ftpFile.getName().length() >= mask_.length()) {
                            if (ftpFile.getName().substring(0, mask_.length()).equals(mask_)) {
                                if (ArrayUtils.isNotEmpty(isExcludeDirectory) && isExcludeDirectory[0] && ftpFile.isDirectory()) {
                                    continue;
                                }
                                list.add(ftpFile.getName());
                            }
                        }
                    }
                } else {
                    for (FTPFile ftpFile : ftpFiles) {
                        list.add(ftpFile.getName());
                    }
                }
                return list;
            }
        } catch (Exception e) {
            logger.error(e, "Get file list file failed!! remote = [", remote, "], mask = [", mask, "]");
        }
        return null;
    }

    /**
     * 檢查遠端目錄下的目錄是否存在
     *
     * @param basePath 基礎目錄
     * @param folder   目錄名稱
     * @return
     */
    @Override
    public boolean isRemotePathExist(String basePath, String folder) {
        try {
            String remotePath = this.getRemotePath(basePath, folder);
            return template.exists(remotePath);
        } catch (Exception e) {
            logger.error(e, "Check path exist or not failed!! basePath = [", basePath, "], folder = [", folder, "]");
            return false;
        }
    }

    /**
     * 上傳檔案
     *
     * @param local                   待上傳本地檔案
     * @param remote                  上傳後遠程檔案
     * @param isRemoteExcludeFilename remote中是否不包含檔案名
     * @return
     */
    @Override
    public boolean upload(String local, String remote, boolean... isRemoteExcludeFilename) {
        boolean exists = this.isRemotePathExist(remote, null);
        if (exists) {
            logger.warn("Remote file exist and will be overwrite, remote = [", remote, "]");
        }
        File file = new File(CleanPathUtil.cleanString(local));
        if (!file.exists()) {
            logger.error("Cannot be upload cause local file not exist, local = [", local, "]");
            return false;
        }
        String remotePath = this.getRemotePath(remote, null);
        if (ArrayUtils.isEmpty(isRemoteExcludeFilename) || !isRemoteExcludeFilename[0]) {
            int index = remotePath.lastIndexOf("/");
            final String filename = remotePath.substring(index + 1, remotePath.length());
            remotePath = remotePath.substring(0, index);
            template.setFileNameGenerator(message -> {
                return filename;
            });
        }
        template.setRemoteDirectoryExpression(new LiteralExpression(remotePath));
        template.setAutoCreateDirectory(true);
        template.afterPropertiesSet();
        Message<File> message = MessageBuilder.withPayload(file).build();
        try {
            template.send(message, FileExistsMode.REPLACE);
        } catch (Exception e) {
            logger.error(e, "Upload failed!! local = [", local, "], remote = [", remote, "]");
            return false;
        } finally {
            template.setFileNameGenerator(null);
            template.setRemoteDirectoryExpression(new LiteralExpression("/"));
            template.setAutoCreateDirectory(false);
            template.afterPropertiesSet();
        }
        return true;
    }

    /**
     * 刪除遠程檔案
     *
     * @param remote 遠程檔案所在路徑
     * @return
     */
    @Override
    public boolean delete(String remote) {
        boolean exists = this.isRemotePathExist(remote, null);
        if (!exists) {
            logger.warn("Cannot delete cause file not exist, remote = [", remote, "]");
            return false;
        }
        try {
            String remotePath = this.getRemotePath(remote, null);
            return template.remove(remotePath);
        } catch (Exception e) {
            logger.error(e, "Delete failed!! remote = [", remote, "]");
            return false;
        }
    }

    /**
     * 斷開連線
     */
    @Override
    public boolean disconnect() {
        return template.executeWithClient((FTPClient ftpClient) -> {
            logger.info("try to logout from [", ftpProperties.getHost(), ":", ftpProperties.getPort(), "]...");
            try {
                boolean result = ftpClient.logout();
                logger.info("logout from [", ftpProperties.getHost(), ":", ftpProperties.getPort(), "], result = [", result, "]");
                return result;
            } catch (Exception e) {
                logger.error(e, "Logout failed from [", ftpProperties.getHost(), ":", ftpProperties.getPort(), "]");
            } finally {
                if (ftpClient.isConnected()) {
                    logger.info("try to disconnect from [", ftpProperties.getHost(), ":", ftpProperties.getPort(), "]...");
                    try {
                        ftpClient.disconnect();
                        logger.info("Disconnect from [", ftpProperties.getHost(), ":", ftpProperties.getPort(), "] successful");
                    } catch (IOException e) {
                        logger.error(e, "Disconnect failed from [", ftpProperties.getHost(), ":", ftpProperties.getPort(), "]");
                    }
                }
            }
            return false;
        });
    }
}
