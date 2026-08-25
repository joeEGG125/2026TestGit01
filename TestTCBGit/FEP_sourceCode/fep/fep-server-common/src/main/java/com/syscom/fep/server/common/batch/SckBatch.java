package com.syscom.fep.server.common.batch;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.text.StringEscapeUtils;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class SckBatch extends FEPBase {
    private int loop = 0;
    private String serverIp;
    private int serverPort;
    private String localFilePath;
    private String remoteFilePath;
    private String serverSendIp;
    private int serverSendPort;
    private String localSendFilePath;
    private String remoteSendFilePath;

    private LogData log = new LogData();

    public void SimpleFileReceiver(String serverIp, int serverPort, String localFilePath, String remoteFilePath) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.localFilePath = localFilePath;
        this.remoteFilePath = remoteFilePath;
    }

    public void FileUploadClient(String serverSendIp, int serverSendPort, String localSendFilePath, String remoteSendFilePath) {
        this.serverSendIp = serverSendIp;
        this.serverSendPort = serverSendPort;
        this.localSendFilePath = localSendFilePath;
        this.remoteSendFilePath = remoteSendFilePath;

    }

    public void receiveSocketFile() {
        try (Socket socket = new Socket(serverIp, serverPort);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(localFilePath), "UTF-8"))) {

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            if (remoteFilePath == null) {
                throw new IOException("路徑為空");
            }
            dos.writeUTF(remoteFilePath);

            String line;

            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }
        } catch (SocketException e) {
            log.setSubSys(SubSystem.BATCH);
            log.setProgramName("Socket連線失敗");
            log.setProgramException(e);
            sendEMS(log);
        } catch (IOException e) {
            log.setSubSys(SubSystem.BATCH);
            log.setProgramName("IO Exception");
            log.setProgramException(e);
            sendEMS(log);
        }
    }

    public void receiveFTPFile() {
        try {
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(serverIp, serverPort);
            ftpClient.login("anonymous", ""); // 匿名登錄

            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // 開始下載
            try (OutputStream outputStream = new FileOutputStream(localFilePath)) {
                boolean success = ftpClient.retrieveFile(remoteFilePath, outputStream);
                if (success) {
                    System.out.println("檔案已成功下載。");
                    logContext.setMessage("成功下載");
                    logMessage(getLogContext());
                } else {
                    System.out.println("檔案下載失敗。");
                    logContext.setMessage("檔案下載失敗");
                    logMessage(getLogContext());
                }
            }
        } catch (Exception e) {
            log.setSubSys(SubSystem.BATCH);
            log.setProgramName("IO Exception");
            log.setProgramException(e);
            sendEMS(log);
        }
    }

    public void connectloops() {
        while (loop <= 3) {
            loop += 1;
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("/fep/fep-app/mft-batch.sh");
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                int exitCode = process.waitFor();
                System.out.println("Exit code: " + exitCode);
                receiveSocketFile();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadFile() {
        try (Socket socket = new Socket(serverSendIp, serverSendPort);
             BufferedReader br = new BufferedReader(new FileReader(localSendFilePath));
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(dos, "UTF-8"))) {

            // 首先發送目標檔案路徑
            dos.writeUTF(remoteSendFilePath);

            // 然後發送檔案內容
            String line;
            while ((line = br.readLine()) != null) {
                // 2024-11-05 Richard modified for 【Stored XSS】
                // bw.write(line);
                bw.write(StringEscapeUtils.escapeHtml4(line));
                bw.newLine();
            }

            System.out.println("File uploaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
