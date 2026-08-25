package com.syscom.fep.frmcommon.os.netstat;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import oshi.PlatformEnum;
import oshi.SystemInfo;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

import java.util.ArrayList;
import java.util.List;

public class Netstat {
    private static final LogHelper logger = new LogHelper();

    private Netstat() {}

    public static List<NetstatConnection> queryTcpNetStat(int port) {
        return queryTcpNetStat(null, port);
    }

    public static List<NetstatConnection> queryTcpNetStat(String processName, int port) {
        PlatformEnum platform = SystemInfo.getCurrentPlatform();
        switch (platform) {
            case WINDOWS:
                return queryWinTcpNetstat(port);
            case LINUX:
            case AIX:
                return queryUnixTcpNetstat(processName, port);
            default:
                return new ArrayList<>();
        }
    }

    public static List<NetstatConnection> queryUnixTcpNetstat(String processName, int port) {
        StringBuilder cmd = new StringBuilder();
        if (StringUtils.isNotBlank(processName)) {
            cmd.append(" | grep ").append(processName);
        }
        if (port > 0) {
            cmd.append(" | grep ").append(port);
        }
        return queryUnixTcpNetstat(cmd.toString());
    }

    public static List<NetstatConnection> queryUnixTcpNetstat(String args) {
        List<NetstatConnection> connections = new ArrayList<>();
        List<String> activeConnections = ExecutingCommand.runNative(new String[] {"netstat", "-antp", args});
        String[] fields = null;
        for (String activeConnection : activeConnections) {
            if (activeConnection.startsWith("tcp")) {
                fields = ParseUtil.whitespaces.split(activeConnection);
                if (ArrayUtils.isNotEmpty(fields)) {
                    try {
                        connections.add(new NetstatUnixConnection().parse(fields));
                    } catch (Exception e) {
                        logger.error(e, "parse line [", activeConnection, "] failed, ", e.getMessage());
                    }
                }
            }
        }
        return connections;
    }

    public static List<NetstatConnection> queryWinTcpNetstat(int port) {
        List<NetstatConnection> connections = new ArrayList<>();
        List<String> activeConnections = ExecutingCommand.runNative(new String[] {"netstat", "-p", "tcp", "-ano"});
        String[] fields = null;
        for (String activeConnection : activeConnections) {
            activeConnection = activeConnection.trim();
            if (activeConnection.startsWith("TCP")) {
                fields = ParseUtil.whitespaces.split(activeConnection);
                if (ArrayUtils.isNotEmpty(fields)) {
                    try {
                        NetstatConnection connection = new NetstatConnection().parse(fields);
                        if (connection.getLocalAddress().contains(":" + port) || connection.getForeignAddress().contains(":" + port)) {
                            connections.add(connection);
                        }
                    } catch (Exception e) {
                        logger.error(e, "parse line [", activeConnection, "] failed, ", e.getMessage());
                    }
                }
            }
        }
        return connections;
    }
}
