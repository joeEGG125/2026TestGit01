package com.syscom.fep.gateway.cmd;

import com.syscom.fep.frmcommon.esapi.ESAPIConfiguration;
import com.syscom.fep.frmcommon.io.ConsoleIn;
import com.syscom.fep.frmcommon.util.FormatUtil;
import org.owasp.esapi.ESAPI;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Properties;


public abstract class GatewayCommand {
    public static final String PROP_FILENAME_INTEGRATION = "application-integration.properties";
    public static final String PROP_FILENAME_ATMGW = "application-gateway-atm.properties";
    public static final String PROP_FILENAME_ATMGW_AGENT = "application-gateway-atm-agent.properties";
    public static final String PROP_FILENAME_FISCGW = "application-gateway-fisc.properties";
    public static final String PROP_FILENAME_FISCGW_AGENT = "application-gateway-fisc-agent.properties";
    public static final String PROP_NAME_HOST_IP = "spring.fep.hostip";
    public static final String PROP_NAME_ATMGW_HOST = "spring.fep.gateway.transmission.atm.host";
    public static final String PROP_NAME_ATMGW_AGENT_HOST = "spring.fep.scheduler.job.app-monitor.serviceHostIp";
    public static final String PROP_NAME_FISCGW_HOST = "spring.fep.gateway.transmission.fisc.primary.fepap[0].host";
    public static final String PROP_NAME_FISCGW_AGENT_HOST = "spring.fep.scheduler.job.app-monitor.serviceHostIp";
    private static final String PROP_NAME_PORT = "server.port";
    private static final String PROP_CONTEXT_PATH = "server.servlet.context-path";
    private static final String DEFAULT_IP = "127.0.0.1";
    public static final String BORDER = "*";
    public static final int BORDER_REPEAT = 70;
    public static final String WELCOME = "Welcome to using {0} Gateway Command Utility Console v{1} ";
    private static Properties prop = null;

    protected abstract Gateway getGateway();

    protected abstract String displayUsage();

    protected abstract String function(String ip, int port, GatewayCommandFunc func, String[] args);

    protected abstract int getDefaultPort();

    static {
        ESAPIConfiguration.init();
    }

    protected void printWelcome(String version) {
        String border = "*";
        GatewayCommandUtil.printOutLn(GatewayCommandUtil.repeat(border, BORDER_REPEAT));
        for (int i = 0; i < 2; i++)
            GatewayCommandUtil.printOutLn(border, GatewayCommandUtil.repeat(" ", BORDER_REPEAT - 2), border);
//        2025-01-20 Joe modified for 【Race Condition Format Flaw】
//        String welcome = MessageFormat.format(WELCOME, this.getGateway().name());
        String welcome = FormatUtil.messageFormat(WELCOME, this.getGateway().name(), version);
        int num = (BORDER_REPEAT - 2 - welcome.length()) / 2;
        GatewayCommandUtil.printOutLn(border, GatewayCommandUtil.repeat(" ", num), welcome, GatewayCommandUtil.repeat(" ", num), border);
        for (int i = 0; i < 2; i++)
            GatewayCommandUtil.printOutLn(border, GatewayCommandUtil.repeat(" ", BORDER_REPEAT - 2), border);
        GatewayCommandUtil.printOutLn(GatewayCommandUtil.repeat(border, BORDER_REPEAT));
    }

    protected String getInput(ConsoleIn console, boolean sscode) {
        String input = console.readLine(sscode);
        if (GatewayCommandConsoleCmd.EXIT.getInput().equalsIgnoreCase(input)) {
            System.exit(6);
        }
        return input;
    }

    protected void execute(String[] args) {
        if (args == null || args.length == 0) {
            displayUsage();
        } else {
            try {
                String help = findArg(args, GatewayCommandArgs.Help.getParam());
                if (help != null) {
                    displayUsage();
                    return;
                }
                String ip = findArg(args, GatewayCommandArgs.IP.getParam());
                int port = findArg(args, GatewayCommandArgs.Port.getParam(), -1);
                GatewayCommandFunc func = Enum.valueOf(GatewayCommandFunc.class, findArg(args, GatewayCommandArgs.Function.getParam()));
                if (func != null) {
                    function(ip, port, func, args);
                    return;
                }
                // 輸入的參數不正確, 也直接顯示usage
                displayUsage();
            } catch (Exception e) {
                // 有異常, 直接顯示usage
                displayUsage();
            }
        }
    }

    protected String findArg(String[] args, String found) {
        if (args == null) return null;
        try {
            for (int i = 0; i < args.length; i++) {
                if (found.equals(args[i])) {
                    if (i + 1 < args.length) {
                        return args[i + 1];
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw e;
        }
        return null;
    }

    protected int findArg(String[] args, String found, int defaultValue) {
        try {
            String value = this.findArg(args, found);
            if (GatewayCommandUtil.isNumeric(value)) {
                return Integer.parseInt(value);
            }
        } catch (Exception e) {
            return -1;
        }
        return defaultValue;
    }

    protected String getUrl(String propFilename, String propHost, GatewayCommandFunc func) {
        return this.getUrl(null, -1, propFilename, propHost, func);
    }

    protected String getUrl(String ip, int port, String propFilename, String propHost, GatewayCommandFunc func) {
        try {
            prop = loadProperties(propFilename, propHost);
        } catch (IOException e) {
            prop = new Properties();
            GatewayCommandUtil.printErrLn("loadProperties failed, propFilename = [", propFilename, "], error message = [", e.getMessage(), "]");
        }
        String contextPath = prop.getProperty(PROP_CONTEXT_PATH, "");
        if (GatewayCommandUtil.isEmpty(ip) && port == -1) {
            ip = prop.getProperty(propHost, DEFAULT_IP);
            port = Integer.parseInt(prop.getProperty(PROP_NAME_PORT, "-1"));
        }
        if (GatewayCommandUtil.isEmpty(ip)) {
            ip = DEFAULT_IP;
        }
        if (port == -1) {
            port = this.getDefaultPort();
        }
        String url = "http://{0}:{1}{2}/recv/{3}/{4}";
        // 2025-01-20 Joe modified for 【Race Condition Format Flaw】
//        return String.format(url, ip, String.valueOf(port), contextPath, this.getGateway().name().toLowerCase(), func.name().toLowerCase());
        return FormatUtil.messageFormat(url, ip, String.valueOf(port), contextPath, this.getGateway().name().toLowerCase(), func.name().toLowerCase());
    }

    protected String httpPost(String uri, String[] args) {
        HttpURLConnection conn = null;
        OutputStream os = null;
        try {
            // URL url = new URL(uri);
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
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/x-www-form-urlencoded");
            // 設定 HSTS 標頭
            conn.setRequestProperty("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            os = conn.getOutputStream();
            String arg = getArgs(args);
            if (arg != null && !"".equals(arg.trim())) {
                os.write(arg.getBytes(StandardCharsets.UTF_8));
            }
            int statusCode = conn.getResponseCode();
            if (statusCode == 200) {
                return this.parseHttp(conn.getInputStream());
            } else {
                return GatewayCommandUtil.join("The http status was responded from ", this.getGateway().name().toUpperCase(), " Gateway server is \"", statusCode, "\"\r\n");
            }
        } catch (Exception e) {
            GatewayCommandUtil.printErrLn("Http post failed, ", e.getMessage());
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

    private String getArgs(String[] args) {
        StringBuilder sb = new StringBuilder();
        if (args != null && args.length > 0) {
            for (String arg : args) {
                sb.append(arg).append("&");
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private Properties loadProperties(String propFilename, String propHost) throws IOException {
        Properties propGw = new Properties();
        if (propFilename != null && !propFilename.trim().isEmpty()) {
            ClassLoader classLoader = GatewayCommand.class.getClassLoader();
            if (classLoader != null) {
                try (InputStream integration = classLoader.getResourceAsStream(GatewayCommandUtil.cleanString(PROP_FILENAME_INTEGRATION));
                     InputStream gw = classLoader.getResourceAsStream(GatewayCommandUtil.cleanString(propFilename))) {
                    // Integration
                    Properties propIntegration = new Properties();
                    if (integration != null) {
                        propIntegration.load(integration);
                    }
                    // Gw
                    if (gw != null)
                        propGw.load(gw);
                    String host = DEFAULT_IP;
                    if (propGw != null) {
                        host = propGw.getProperty(propHost);
                    }
                    if (host != null) {
                        if (host.contains("${")) {
                            int begin = host.indexOf("${") + 2;
                            int end = host.indexOf("}");
                            if (propIntegration != null) {
                                host = propIntegration.getProperty(host.substring(begin, end), DEFAULT_IP);
                            }
                        }
                    } else {
                        if (propIntegration != null) {
                            host = propIntegration.getProperty(PROP_NAME_HOST_IP, DEFAULT_IP);
                        }
                    }
                    if (propGw != null) {
                        propGw.put(propHost, host);
                    }
                } catch (IOException e) {
                    GatewayCommandUtil.printErrLn("load properties file \"", PROP_FILENAME_INTEGRATION, "\" or \"", propFilename, "\" failed, ", e.getMessage(), "\r\n");
                    throw e;
                }
            }
        }
        return propGw;
    }

    protected String getProp(String key, String defaultValue) {
        return prop.getProperty(key, defaultValue);
    }
}
