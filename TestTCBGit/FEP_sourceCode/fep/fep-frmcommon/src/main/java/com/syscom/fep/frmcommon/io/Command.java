package com.syscom.fep.frmcommon.io;

import com.syscom.fep.frmcommon.esapi.ESAPIConfiguration;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import org.owasp.esapi.ESAPI;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class Command {
    private final LogHelper logger = new LogHelper();
    protected final String ProgramName = this.getClass().getSimpleName();

    static {
        ESAPIConfiguration.init();
    }

    /**
     * 顯示Usage
     */
    protected abstract void displayUsage();

    /**
     * 執行
     *
     * @throws Exception
     */
    protected abstract void process(String[] args) throws Exception;

    /**
     * 開始執行
     *
     * @param args
     */
    protected void execute(String[] args) {
        if (args == null || args.length == 0 || "?".equals(args[0])) {
            displayUsage();
            return;
        }
        try {
            process(args);
        } catch (Exception e) {
            System.err.println(logger.error(e, ProgramName, " execute failed, ", e.getMessage()));
        }
    }

    /**
     * 尋找傳入的變數
     *
     * @param args
     * @param found
     * @param defaultValue
     * @return
     */
    protected String findArg(String[] args, String found, String defaultValue) {
        if (args == null) return defaultValue;
        try {
            for (String arg : args) {
                if (arg.startsWith("/" + found)) {
                    int index = arg.indexOf(":");
                    if (index != -1) {
                        String value = arg.substring(index + 1).trim();
                        if (value.isEmpty())
                            return defaultValue;
                        return value;
                    }
                    break;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println(logger.warn("find arg \"" + found + "\" failed", e));
        }
        return defaultValue;
    }

    /**
     * 檢核輸入的參數是否為空白
     *
     * @param name
     * @param value
     * @return
     */
    protected boolean checkEmpty(String name, String value) {
        if (value == null || value.trim().isEmpty()) {
            System.err.println(logger.info(name, " must not be empty!!!"));
            return true;
        }
        return false;
    }

    /**
     * 送Http Post請求
     *
     * @param uri
     * @param args
     * @return
     */
    protected String httpPost(String uri, String[] args) {
        HttpURLConnection conn = null;
        OutputStream os = null;
        try {
            URL url = null;
            try {
                String cURL = ESAPI.encoder().canonicalize(uri, false, false);
                if (ESAPI.validator().isValidInput("URLContext", cURL, "URL", cURL.length(), false)) {
                    url = new URL(cURL);
                } else {
                    return "";
                }
            } catch (MalformedURLException mue) {
                return "";
            }
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(Integer.parseInt(System.getProperty("http.connection.timeout", "30000")));
            conn.setReadTimeout(Integer.parseInt(System.getProperty("http.read.timeout", "60000")));
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            os = conn.getOutputStream();
            String arg = getArgs(args);
            if (!arg.trim().isEmpty()) {
                // 2025-04-10 Richard modified for [Missing HSTS Header]
                // os.write(arg.getBytes(StandardCharsets.UTF_8));
                ReflectUtil.envokeMethod(os, "write", new Class[] {byte[].class}, new Object[] {arg.getBytes(StandardCharsets.UTF_8)});
            }
            int statusCode = conn.getResponseCode();
            if (statusCode == 200) {
                return this.parseHttp(conn.getInputStream());
            } else {
                return "The http status was responded from Server is \"" + statusCode + "\"\r\n";
            }
        } catch (Exception e) {
            logger.error(e, "Http post failed, ", e.getMessage());
            return "";
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    os = null;
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String getArgs(String[] args) {
        StringBuilder sb = new StringBuilder();
        if (args != null) {
            for (String arg : args) {
                sb.append(arg).append("&");
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    protected String parseHttp(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        int bufferSize = 8 * 1024;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8), bufferSize)) {
            String readLine = null;
            while ((readLine = br.readLine()) != null) {
                sb.append(readLine).append("\r\n");
            }
            return sb.toString();
        }
    }

    protected void error(Throwable t, Object... messages) {
        System.err.println(logger.error(t, messages));
    }
}
