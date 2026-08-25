package com.syscom.fep.frmcommon.os;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.os.data.*;
import com.syscom.fep.frmcommon.util.CommandLineUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.VirtualMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import javax.management.MBeanServer;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OperationSystemDataCollector {
    private static final LogHelper logger = new LogHelper();
    private static final SystemInfo si;
    private static final HardwareAbstractionLayer hal;
    private static final OperatingSystem os;
    private static final com.sun.management.OperatingSystemMXBean osMXBean;
    private static final ThreadMXBean threadBean;
    private static final RuntimeMXBean runtimeBean;
    private static final MBeanServer platformMBeanServer;

    static {
        logger.info("Initializing System...");
        si = new SystemInfo();
        hal = si.getHardware();
        os = si.getOperatingSystem();
        osMXBean = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class);
        threadBean = ManagementFactory.getThreadMXBean();
        runtimeBean = ManagementFactory.getRuntimeMXBean();
        platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
    }

    public static SystemInfo getSi() {
        return si;
    }

    public static HardwareAbstractionLayer getHal() {
        return hal;
    }

    public static OperatingSystem getOs() {
        return os;
    }

    public static MBeanServer getPlatformMBeanServer() {
        return platformMBeanServer;
    }

    /**
     * 獲取系統CPU相關訊息
     *
     * @return
     */
    public static CpuData getSysCpuData() {
        return getSysCpuData(1000L);
    }

    /**
     * 獲取系統CPU相關訊息
     *
     * @param interval
     * @return
     */
    public static CpuData getSysCpuData(long interval) {
        CentralProcessor processor = hal.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(interval <= 0 ? 1000L : interval);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
        CpuData cpuData = new CpuData();
        cpuData.setLogicalProcessorCount(processor.getLogicalProcessorCount()); // cpu邏輯核數
        cpuData.setPhysicalProcessorCount(processor.getPhysicalProcessorCount()); // cpu物理核數
        cpuData.setSystemUsage(totalCpu == 0 ? 0.0d : cSys * 1.0 / totalCpu); // cpu系統使用率
        cpuData.setUserUsage(totalCpu == 0 ? 0.0d : user * 1.0 / totalCpu); // cpu用戶使用率
        cpuData.setIoWait(totalCpu == 0 ? 0.0d : iowait * 1.0 / totalCpu); // cpu當前等待率
        cpuData.setIdle(totalCpu == 0 ? 0.0d : idle * 1.0 / totalCpu); // cpu當前空閑率
        return cpuData;
    }

    /**
     * 獲取系統CPU佔用率
     * <p>
     * 未測試
     *
     * @return
     */
//    public static double getSysCpuPercentForAix() {
//        logger.info("--------------------------------begin collection cpu");
//        double cpu = 0.0d, cpuidel;
//        Process p = null;
//        BufferedReader br = null;
//        StringBuilder sb = new StringBuilder();
//        try {
//            String[] cmds = {"/bin/sh", "-c", "vmstat 1 1| tail -n  7 | head -n 7|awk '{print   $16}'"};
//            p = Runtime.getRuntime().exec(cmds);
//            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            String line;
//            while ((line = br.readLine()) != null) {
//                sb.append(line).append("\n");
//                cpuidel = Double.parseDouble(line);
//                cpu = 100 - cpuidel;
//            }
//        } catch (Exception e) {
//            logger.error(e, e.getMessage());
//        } finally {
//            try {
//                if (br != null) {
//                    br.close();
//                }
//                if (p != null) {
//                    p.destroy();
//                }
//            } catch (Exception e) {
//                logger.warn(e, e.getMessage());
//            }
//        }
//        logger.debug("cpu idel out [", sb, "]");
//        logger.info("--------------------------------cpu:", cpu);
//        return cpu;
//    }

    /**
     * 獲取系統可用記憶體佔用比
     * <p>
     * 未測試
     *
     * @return
     */
//    public static double getSysAvailableMemoryPercentForAix() {
//        logger.info("--------------------------------begin collection memory");
//        double percent = 0.0d;
//        Process p = null;
//        BufferedReader br = null;
//        StringBuilder sb = new StringBuilder();
//        try {
//            String[] cmds = {"/bin/sh", "-c", "svmon -G|grep memory|grep -v grep|awk '{print   $2 \"  \" $3}'"};
//            p = Runtime.getRuntime().exec(cmds);
//            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            String line;
//            while ((line = br.readLine()) != null) {
//                sb.append(line).append("\n");
//                percent = Double.parseDouble(line.split("\\s ")[1]) / Double.parseDouble(line.split("\\s ")[0]);
//            }
//        } catch (Exception e) {
//            logger.error(e, e.getMessage());
//        } finally {
//            try {
//                if (br != null) {
//                    br.close();
//                }
//                if (p != null) {
//                    p.destroy();
//                }
//            } catch (Exception e) {
//                logger.warn(e, e.getMessage());
//            }
//        }
//        logger.debug("mem related out [%s]", sb);
//        logger.info("--------------------------------mem:", percent * 100);
//        return percent * 100;
//    }

    /**
     * 通過iostat獲取系統CPU使用率
     *
     * @param shellFullPath 完整shell 路徑, 因為有多專案互叫路徑也不同, 無法用相對路徑
     * @param index         要的資料在 iostat result 找到行的第幾段
     */
    public static double getSysCpuUserUsage(final String shellFullPath, final int index) {
        String user = StringUtils.EMPTY;
        // AIX 環境變數喪失, 且測不出來對象怎麼帶參數. 很不好測, 所以以下為安全牌
        // AIX 必須 -c -i
        // 完整路徑 


        // 一次改太多設定檔, 且不一定改到, 安全用, 正常沒意義
        // in application-gateway-atm-agent.properties
        // spring.fep.scheduler.job.system-monitor.shell-full-path=/fepap/fep-app/fep-gateway-atm-agent/appmon_iostat.sh
        // 
        // in application-gateway-fisc-agent.properties
        // spring.fep.scheduler.job.system-monitor.shell-full-path=/fepap/fep-app/fep-gateway-fisc-agent/appmon_iostat.sh
        //
        // in application-service-appmon.properties
        // spring.fep.service.monitor.shell-full-path=/fepap/fep-app/fep-service-appmon/appmon_iostat.sh
        // 
        // dev, sit, uat, prod 環境的位置同, 以上僅示意
        String shellPath = shellFullPath == null ? "/fepap/fep-app/fep-service-appmon/appmon_iostat.sh" : shellFullPath;
        String[] command = new String[] {"bash", "-c", "-i", shellPath};
        Process process = null;
        try {
            logger.debug("[iostat]start to execute command [", StringUtils.join(command, StringUtils.SPACE), "]");
            // process = Runtime.getRuntime().exec(command);
            process = CommandLineUtil.getProcess(command); // 2024-08-29 Richard modified for Command Injection
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line, avgCpuLine = "", avgCpuDataLine = "";
                boolean found = false, assigned = false;
                int i = 0;
                while ((line = reader.readLine()) != null) {
                    if (found && !assigned) {
                        String[] datas = line.split("\\s+");
                        avgCpuDataLine = String.join(",", datas);
                        user = datas[index].trim();
                        found = false;
                        assigned = true;
                        logger.info("[iostat][", i, "]found avg-cpu line [", avgCpuLine, "]");
                        logger.info("[iostat][", i, "]found avg-cpu data line [", avgCpuDataLine, "]");
                    }
                    if (line.contains("avg-cpu:")) {
                        i++;
                        found = true;
                        assigned = false;
                        avgCpuLine = line;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e, "[iostat]execute command [", StringUtils.join(command, StringUtils.SPACE), "] failed, ", e.getMessage());
        }
        return Double.parseDouble(user) / 100.0d;
    }

    /**
     * 獲取當前進程的PID
     *
     * @return
     */
    public static int getProcessID() {
        // String name = ManagementFactory.getRuntimeMXBean().getName();
        // String pid = name.split("@")[0];
        // logger.debug("process [", name, "] pid is [", pid, "]");
        // return Integer.parseInt(pid)
        return os.getProcessId();
    }

    /**
     * 獲取所有進程
     *
     * @return
     */
    public static List<OSProcess> getOSProcesses() {
        return os.getProcesses();
    }

    /**
     * 根據PID獲取進程信息
     *
     * @param pid
     * @return
     */
    public static ProcessData getProcessData(final int pid) {
        return getProcessData(pid, 1000L);
    }

    /**
     * 根據PID獲取進程信息
     *
     * @param pid
     * @param interval
     * @return
     */
    public static ProcessData getProcessData(final int pid, final long interval) {
        logger.debug("get process data by pid [", pid, "], interval [", interval, "]");
        OSProcess osProcess = os.getProcess(pid);
        if (osProcess != null) {
            ProcessData processData = getProcessData(osProcess);
            setCpuPercent(pid, interval, processData, osProcess);
            return processData;
        }
        logger.warn("process [", pid, "] not found");
        return null;
    }

    /**
     * 根據Name獲取進程信息
     *
     * @param name
     * @return
     */
    public static ProcessData getProcessData(final String name) {
        return getProcessData(name, 1000L);
    }

    /**
     * 根據Name獲取進程信息
     *
     * @param name
     * @param interval
     * @return
     */
    public static ProcessData getProcessData(final String name, final long interval) {
        logger.debug("get process data by name [", name, "], interval [", interval, "]");
        List<OSProcess> osProcesses = os.getProcesses(t -> t.getName().equalsIgnoreCase(name), null, 0);
        if (CollectionUtils.isNotEmpty(osProcesses)) {
            OSProcess osProcess = osProcesses.get(0); // 取第一個
            ProcessData processData = getProcessData(osProcess);
            setCpuPercent(osProcess.getProcessID(), interval, processData, osProcess);
            return processData;
        }
        logger.warn("process [", name, "] not found");
        return null;
    }

    /**
     * 獲取ProcessData
     *
     * @param osProcess
     */
    public static ProcessData getProcessData(final OSProcess osProcess) {
        ProcessData processData = new ProcessData();
        processData.setName(osProcess.getName());
        processData.setAffinityMask(osProcess.getAffinityMask());
        processData.setArguments(osProcess.getArguments());
        processData.setBitness(osProcess.getBitness());
        processData.setBytesRead(osProcess.getBytesRead());
        processData.setBytesWritten(osProcess.getBytesWritten());
        processData.setCommandLine(osProcess.getCommandLine());
        processData.setContextSwitches(osProcess.getContextSwitches());
        processData.setCpuPercent(100.0d * (osProcess.getKernelTime() + osProcess.getUserTime()) / osProcess.getUpTime());
        processData.setCurrentWorkingDirectory(osProcess.getCurrentWorkingDirectory());
        processData.setEnvironmentVariables(osProcess.getEnvironmentVariables());
        processData.setGroup(processData.getGroup());
        processData.setGroupID(osProcess.getGroupID());
        processData.setHardOpenFileLimit(osProcess.getHardOpenFileLimit());
        processData.setKernelTime(osProcess.getKernelTime());
        processData.setMajorFaults(osProcess.getMajorFaults());
        processData.setMemPercent(100.0d * osProcess.getResidentSetSize() / si.getHardware().getMemory().getTotal());
        processData.setMinorFaults(osProcess.getMinorFaults());
        processData.setOpenFiles(osProcess.getOpenFiles());
        processData.setParentProcessID(osProcess.getParentProcessID());
        processData.setPath(processData.getPath());
        processData.setPriority(osProcess.getPriority());
        processData.setProcessCpuLoadCumulative(osProcess.getProcessCpuLoadCumulative());
        processData.setProcessID(osProcess.getProcessID());
        processData.setResidentSetSize(osProcess.getResidentSetSize());
        processData.setSoftOpenFileLimit(osProcess.getSoftOpenFileLimit());
        processData.setStartTime(osProcess.getStartTime());
        processData.setState(osProcess.getState());
        processData.setThreadCount(osProcess.getThreadCount());
        processData.setUpdateAttributes(processData.isUpdateAttributes());
        processData.setUpTime(osProcess.getUpTime());
        processData.setUser(processData.getUser());
        processData.setUserID(processData.getUserID());
        processData.setUserTime(osProcess.getUserTime());
        processData.setVirtualSize(osProcess.getVirtualSize());
        return processData;
    }

    /**
     * 間隔一段時間後, 再取一次, 用來計算CPU使用率
     *
     * @param pid
     * @param interval
     * @param processData
     * @param osProcess
     */
    private static void setCpuPercent(int pid, long interval, ProcessData processData, OSProcess osProcess) {
        // 間隔一段時間後, 再取一次, 用來計算CPU使用率
        if (interval != 0) {
            Util.sleep(interval < 0 ? 1000L : interval);
            OSProcess afterProcess = os.getProcess(pid);
            if (afterProcess != null) {
                processData.setCpuPercent(100.0d * ((afterProcess.getKernelTime() - osProcess.getKernelTime()) + (afterProcess.getUserTime() - osProcess.getUserTime())) / (afterProcess.getUpTime() - osProcess.getUpTime()));
            }
        }
    }

    /**
     * 獲取系統記憶體信息
     *
     * @return
     */
    public static MemoryData getSysMemory() {
        GlobalMemory memory = hal.getMemory();
        MemoryData memoryData = new MemoryData();
        memoryData.setAvailable(memory.getAvailable());
        memoryData.setPageSize(memory.getPageSize());
        memoryData.setTotal(memory.getTotal());
        memoryData.setUsed(memoryData.getTotal() - memoryData.getAvailable());
        memoryData.setUsedPercent(100.0d * memoryData.getUsed() / memoryData.getTotal());
        memoryData.setAvailablePercent(100.0d * memoryData.getAvailable() / memoryData.getTotal());
        VirtualMemory virtualMemory = memory.getVirtualMemory();
        if (virtualMemory != null) {
            memoryData.setSwapPagesIn(virtualMemory.getSwapPagesIn());
            memoryData.setSwapPagesOut(virtualMemory.getSwapPagesOut());
            memoryData.setSwapTotal(virtualMemory.getSwapTotal());
            memoryData.setSwapUsed(virtualMemory.getSwapUsed());
            memoryData.setVirtualMax(virtualMemory.getVirtualMax());
            memoryData.setVirtualInUse(virtualMemory.getVirtualInUse());
            memoryData.setVirtualPercent(100.0d * memoryData.getVirtualInUse() / memoryData.getVirtualMax());
            memoryData.setSwapPercent(100.0d * memoryData.getSwapUsed() / memoryData.getSwapTotal());
        }
        return memoryData;
    }

    /**
     * 獲取系統檔案系統信息
     *
     * @return
     */
    public static List<FileStoreData> getSysFileStoreData() {
        List<FileStoreData> result = new ArrayList<>();
        FileSystem fileSystem = os.getFileSystem();
        for (OSFileStore fs : fileSystem.getFileStores()) {
            FileStoreData fileStore = new FileStoreData();
            fileStore.setName(fs.getName());
            fileStore.setVolume(fs.getVolume());
            fileStore.setLabel(fs.getLabel());
            fileStore.setLogicalVolume(fs.getLogicalVolume());
            fileStore.setMount(fs.getMount());
            fileStore.setDescription(fs.getDescription());
            fileStore.setType(fs.getType());
            fileStore.setOptions(fs.getOptions());
            fileStore.setUuid(fs.getUUID());
            fileStore.setFreeSpace(fs.getFreeSpace());
            fileStore.setUsableSpace(fs.getUsableSpace());
            fileStore.setTotalSpace(fs.getTotalSpace());
            fileStore.setFreeInodes(fs.getFreeInodes());
            fileStore.setTotalInodes(fs.getTotalInodes());
            result.add(fileStore);
        }
        return result;
    }

    /**
     * 獲取JVM信息
     *
     * @return
     */
    public static JvmData getJvmData() {
        Runtime runtime = Runtime.getRuntime();
        JvmData jvmData = new JvmData();
        jvmData.setMemoryMax(runtime.maxMemory());
        jvmData.setMemoryCommitted(runtime.totalMemory()); // JVM總共分配的記憶體大小
        jvmData.setMemoryFree(runtime.freeMemory()); // JVM分配的記憶體中剩餘可用的記憶體大小
        jvmData.setMemoryUsed(jvmData.getMemoryCommitted() - jvmData.getMemoryFree()); // 獲取JVM實際已使用的記憶體大小
        jvmData.setMemoryAvailable(jvmData.getMemoryMax() - jvmData.getMemoryUsed()); // 獲取JVM實際可用的最大記憶體大小
        jvmData.setCpuUsage(getJvmCpuUsage());
        return jvmData;
    }

    /**
     * 獲取JVM CPU負載
     *
     * @return
     */
    public static double getJvmCpuUsage() {
        double cpuUsage = osMXBean != null ? osMXBean.getProcessCpuLoad() : 0.0d;
        // 因為獲取的CPU負載可能會小於0, 所以要設置為0.0d
        return Math.max(cpuUsage, 0.0d);
    }

    /**
     * 計算獲取JVM CPU負載
     *
     * @return
     */
    public static double calculateJvmCpuUsage() {
        return calculateJvmCpuUsage(1000L);
    }

    /**
     * 計算獲取JVM CPU負載
     *
     * @param interval
     * @return
     */
    public static double calculateJvmCpuUsage(long interval) {
        if (osMXBean == null) return 0.0d;
        if (interval <= 0) interval = 1000L;
        long prevUpTime = runtimeBean.getUptime();
        long prevProcessCpuTime = osMXBean.getProcessCpuTime();
        int processorCount = osMXBean.getAvailableProcessors();
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
            logger.warn(e, e.getMessage());
        }
        long processCpuTime = osMXBean.getProcessCpuTime();
        long upTime = runtimeBean.getUptime();
        // 因為時間是納秒數, 所以要除1000000
        return (processCpuTime - prevProcessCpuTime) * 1.0d / 1000000 / processorCount / (upTime - prevUpTime);
    }

    /**
     * 計算獲取JVM執行緒數
     *
     * @param states
     * @return
     */
    public static long calculateJvmThreadCount(Thread.State... states) {
        long result = 0L;
        if (ArrayUtils.isEmpty(states)) {
            result = threadBean.getThreadCount();
        } else {
            for (Thread.State state : states) {
                result += Arrays.stream(threadBean.getThreadInfo(threadBean.getAllThreadIds()))
                        .filter(t -> t != null && t.getThreadState() == state)
                        .count();
            }
        }
        return result;
    }

    /**
     * 獲取JVM啟動時間
     *
     * @return
     */
    public static long getJvmStartTime() {
        return runtimeBean.getStartTime();
    }

    /**
     * 計算資料夾大小
     *
     * @param directory
     * @return
     */
    public static long sizeOfDirectory(File directory) {
        long size = -1;
        String[] command = new String[] {"du", "-s", directory.getAbsolutePath()};
        Process process = null;
        try {
            logger.debug("[sizeOfDirectory][", StringUtils.join(command, StringUtils.SPACE), "]start to execute command...");
            process = CommandLineUtil.getProcess(command);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                List<String> lines = IOUtils.readLines(reader);
                logger.debug("[sizeOfDirectory][", StringUtils.join(command, StringUtils.SPACE), "]", StringUtils.join(lines, System.lineSeparator()));
                if (CollectionUtils.isNotEmpty(lines)) {
                    String lastLine = lines.get(lines.size() - 1);
                    String[] datas = lastLine.split("\\s+");
                    size = Long.parseLong(datas[0].trim()) * 512; // 2026-07-30 Richard modified 預設單位是block, 一個block=512byte, 所以這裡要乘以512
                }
            }
        } catch (Exception e) {
            logger.error(e, "[sizeOfDirectory]execute command [", StringUtils.join(command, StringUtils.SPACE), "] failed, ", e.getMessage());
        }
        return size;
    }
}
