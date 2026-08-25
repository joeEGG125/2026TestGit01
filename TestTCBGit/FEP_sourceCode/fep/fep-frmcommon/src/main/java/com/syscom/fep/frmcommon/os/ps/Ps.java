package com.syscom.fep.frmcommon.os.ps;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

import java.util.ArrayList;
import java.util.List;

public class Ps {
    private static final LogHelper logger = new LogHelper();

    private Ps() {}

    /**
     * 判斷進程是否存在
     *
     * @param processName
     * @return
     */
    public static boolean processExists(String processName) {
        return processExists(processName, null);
    }

    /**
     * 判斷進程是否存在
     *
     * @param processName
     * @return
     */
    public static boolean processExists(String processName, List<String> excludeProcessNames) {
        StringBuilder cmd = new StringBuilder();
        cmd.append("ps aux | grep ").append(processName).append(" | grep -v grep");
        if (CollectionUtils.isNotEmpty(excludeProcessNames)) {
            for (String excludeProcessName : excludeProcessNames) {
                cmd.append(" | grep -v ").append(excludeProcessName);
            }
        }
        cmd.append(" >/dev/null && echo true || echo false");
        String[] cmdArgs = {"/bin/sh", "-c", cmd.toString()};
        logger.debug("start to execute command: ", StringUtils.join(cmd, StringUtils.SPACE));
        List<String> result = ExecutingCommand.runNative(cmdArgs);
        logger.debug("execute command finish and get result: ", StringUtils.join(result, System.lineSeparator()));
        return CollectionUtils.isNotEmpty(result) && Boolean.parseBoolean(result.get(0));
    }

    /**
     * ps aux獲取進程列表
     *
     * @param processName
     * @return
     */
    public static List<PsAuxData> getPsAuxDataList(String processName) {
        return getPsAuxDataList(processName, null);
    }

    /**
     * ps aux獲取進程列表
     *
     * @param processName
     * @param excludeProcessNames
     * @return
     */
    public static List<PsAuxData> getPsAuxDataList(String processName, List<String> excludeProcessNames) {
        StringBuilder cmd = new StringBuilder();
        cmd.append("ps aux | grep ").append(processName).append(" | grep -v grep");
        if (CollectionUtils.isNotEmpty(excludeProcessNames)) {
            for (String excludeProcessName : excludeProcessNames) {
                cmd.append(" | grep -v ").append(excludeProcessName);
            }
        }
        String[] cmdArgs = {"/bin/sh", "-c", cmd.toString()};
        logger.debug("start to execute command: ", StringUtils.join(cmd, StringUtils.SPACE));
        List<String> result = ExecutingCommand.runNative(cmdArgs);
        logger.debug("execute command finish and get result: ", StringUtils.join(result, System.lineSeparator()));
        List<PsAuxData> psAuxDataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(result)) {
            String[] fields = null;
            for (String line : result) {
                fields = ParseUtil.whitespaces.split(line);
                if (ArrayUtils.isNotEmpty(fields)) {
                    try {
                        psAuxDataList.add(new PsAuxData(fields));
                    } catch (Exception e) {
                        logger.error(e, "parse line [", line, "] failed, ", e.getMessage());
                    }
                }
            }
        }
        return psAuxDataList;
    }

//    public static void main(String[] args) {
//        System.out.println("suipsrv : " + processExists("suipsrv", Collections.singletonList("suipsrv1")));
//        System.out.println("suipsrv1: " + processExists("suipsrv1"));
//        System.out.println("skype: " + processExists("skype"));
//        System.out.println("fifa: " + processExists("fifa"));
//        System.out.println("abc: " + processExists("abc"));
//        System.out.println("abcd: " + processExists("abcd"));
//
//        getPsAuxDataList("suipsrv", Collections.singletonList("suipsrv1")).forEach(System.out::println);
//        System.out.println("---------------------");
//        getPsAuxDataList("suipsrv1").forEach(System.out::println);
//    }
}