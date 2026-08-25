package com.syscom.fep.gateway.cmd;

import com.syscom.fep.frmcommon.util.CompressionUtil;
import com.syscom.fep.gateway.entity.LogKind;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FISCGatewayAgentCommand extends GatewayCommand {
    private static final String VER = "1.2.0";
    private boolean isCompressedByte = false;

    public static void main(String[] args) {
        new FISCGatewayAgentCommand().execute(args);
    }

    @Override
    protected Gateway getGateway() {
        return Gateway.FISC;
    }

    @Override
    protected int getDefaultPort() {
        return 8302;
    }

    @Override
    protected String displayUsage() {
        printWelcome(VER);
        StringBuilder sb = new StringBuilder();
        sb.append("Usage:\r\n");
        sb.append("\t1 Show Help:\r\n\t\t").append(GatewayCommandArgs.Help.getParam()).append("\r\n");
        sb.append("\t2 Operate Primary/Secondary Gateway:\r\n");
        sb.append("\t\t2-a) Start Primary Gateway\r\n\t\t\t -f channel -d mode=primary&action=start\r\n");
        sb.append("\t\t2-b) Stop Primary Gateway\r\n\t\t\t -f channel -d mode=primary&action=stop\r\n");
        sb.append("\t\t2-c) Start Secondary Gateway\r\n\t\t\t -f channel -d mode=secondary&action=start\r\n");
        sb.append("\t\t2-d) Stop Secondary Gateway\r\n\t\t\t -f channel -d mode=secondary&action=stop\r\n");
        sb.append("\t\t2-e) Start All Gateway\r\n\t\t\t -f start\r\n");
        sb.append("\t\t2-f) Stop All Gateway\r\n\t\t\t -f stop\r\n");
        sb.append("\t\t2-g) Check Gateway Status\r\n\t\t\t -f check\r\n");
        sb.append("\t\t2-h) Show Gateway Log\r\n\t\t\t -f showlog -d logKind=").append(LogKind.showInfo()).append("&date=yyyyMMdd\r\n");
        sb.append("\t3 Change FEP FISC Service Host:\r\n");
        sb.append("\t\t3-a) Change by IP\r\n\t\t\t -f changefepap -d host=127.0.0.1\r\n");
        sb.append("\t\t3-b) Change by Index\r\n\t\t\t -f changefepap -d host=0\r\n");
        sb.append("\t\t3-b) Rest to RoundRobin\r\n\t\t\t -f changefepap\r\n");
        sb.append("\t4 Check FEP FISC Service Host:\r\n");
        sb.append("\t\t4-a) Check FEP FISC Service\r\n\t\t\t -f checkfepap\r\n");
        return GatewayCommandUtil.printOut(sb.toString());
    }

    @Override
    protected String function(String ip, int port, GatewayCommandFunc func, String[] args) {
        String url = getUrl(ip, port, PROP_FILENAME_FISCGW_AGENT, PROP_NAME_FISCGW_AGENT_HOST, func);
        String data = findArg(args, GatewayCommandArgs.Data.getParam());
        switch (func) {
            case channel:
                if (GatewayCommandUtil.isBlank(data)) {
                    data = "mode=primary&action=start";
                }
            case changefepap:
            case checkfepap:
                return GatewayCommandUtil.printOut(httpPost(url, new String[] {data}));
            case showlog:
                isCompressedByte = true;
                return this.showLog(url, data);
            default:
                return GatewayCommandUtil.printOut(httpPost(url, null));
        }
    }

    private String showLog(String url, String data) {
        LogKind logKind = null;
        final String logKindPrefix = "logKind=", intervalPrefix = "interval=";
        if (StringUtils.isBlank(data) || !data.contains(logKindPrefix)) {
            logKind = LogKind.txcurrent;
            data = logKindPrefix + logKind.name() + "&" + intervalPrefix + "3000";
        }
        if (logKind == null) {
            int start = data.indexOf(logKindPrefix) + logKindPrefix.length();
            int end = data.indexOf('&', start);
            end = end == -1 ? data.length() : end;
            try {
                logKind = Enum.valueOf(LogKind.class, StringUtils.substring(data, start, end));
            } catch (Exception e) {
                logKind = LogKind.txcurrent;
            }
        }
        if (logKind == LogKind.txcurrent) {
            long interval = 3000L;
            if (data.contains(intervalPrefix)) {
                int start = data.indexOf(intervalPrefix) + intervalPrefix.length();
                int end = data.indexOf('&', start);
                end = end == -1 ? data.length() : end;
                try {
                    interval = Long.parseLong(StringUtils.substring(data, start, end));
                } catch (NumberFormatException e) {}
            }
            final String finalData = data;
            final long finalInterval = interval;
            final ProcessBuilder builder = new ProcessBuilder(this.getProp("spring.fep.gateway.agent.fisc.cmd.clear","/bin/sh -c clear").split("\\s+")).inheritIO();
            new Thread(() -> {
                while (true) {
                    try {
                        builder.start().waitFor(); // 清屏命令
                    } catch (Exception e) {}
                    GatewayCommandUtil.printOut(httpPost(url, new String[] {finalData}));
                    GatewayCommandUtil.printOut("\r\n");
                    // GatewayCommandUtil.printOut("Show FISC Gateway Log after ", finalInterval, " milliseconds...");
                    GatewayCommandUtil.printOut("Press CTRL+C to exit..."); // 2024-06-24 Richard modified for 改成 "Press CTRL+C to exit..." by Ashiang
                    GatewayCommandUtil.printOut("\r\n");
                    try {
                        Thread.sleep(finalInterval);
                    } catch (InterruptedException e) {}
                }
            }).start();
            new Thread(() -> {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                    int read = -1;
                    while ((read = br.read()) != -1) {
                        if (read == 'q' || read == 'Q') {
                            System.exit(0);
                        }
                    }
                } catch (Exception e) {}
            }).start();
            return "";
        }
        return GatewayCommandUtil.printOut(httpPost(url, new String[] {data}));
    }

    @Override
    protected String parseHttp(InputStream in) throws Exception {
        if (isCompressedByte) {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                while (in.read(buffer) != -1) {
                    bos.write(buffer);
                }
                byte[] compressedBytes = bos.toByteArray();
                // 2024-06-24 Richard add for 檔不存在顯示空白就好 by Ashiang
                if (ArrayUtils.isEmpty(compressedBytes)) {
                    return StringUtils.EMPTY;
                } else if ("[ERROR]".equals(new String(ArrayUtils.subarray(compressedBytes, 0, 7), StandardCharsets.UTF_8))) {
                    return new String(compressedBytes, StandardCharsets.UTF_8).trim();
                }
                return new String(CompressionUtil.decompress(compressedBytes), StandardCharsets.UTF_8);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
        return super.parseHttp(in);
    }
}
