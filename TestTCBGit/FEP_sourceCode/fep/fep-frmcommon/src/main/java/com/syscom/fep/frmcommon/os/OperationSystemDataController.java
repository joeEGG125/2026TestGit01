package com.syscom.fep.frmcommon.os;

import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OSProcess;
import oshi.software.os.OSSession;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;
import oshi.util.Util;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Controller
@ConditionalOnProperty(prefix = "spring.fep.os.ctrl", name = "enable", havingValue = "true")
@Lazy
public class OperationSystemDataController {

    @RequestMapping(value = "/os/process", method = RequestMethod.GET)
    @ResponseBody
    public String getProcess() {
        List<String> oshi = new ArrayList<>();
        printProcess(oshi);
        return StringUtils.join(oshi, System.lineSeparator()) + System.lineSeparator();
    }

    public void printProcess(final List<String> oshi) {
        SystemInfo si = OperationSystemDataCollector.getSi();
        HardwareAbstractionLayer hal = OperationSystemDataCollector.getHal();
        OperatingSystem os = OperationSystemDataCollector.getOs();
        // printOperatingSystem(oshi, os);
        // printComputerSystem(oshi, hal.getComputerSystem());
        printMemory(oshi, hal.getMemory());
        printCpu(oshi, hal.getProcessor());
        printProcesses(oshi, os, hal.getMemory());
    }

    private void printOperatingSystem(final List<String> oshi, final OperatingSystem os) {
        oshi.add(String.valueOf(os));
        oshi.add("Booted: " + Instant.ofEpochSecond(os.getSystemBootTime()));
        oshi.add("Uptime: " + FormatUtil.formatElapsedSecs(os.getSystemUptime()));
        oshi.add("Running with" + (os.isElevated() ? "" : "out") + " elevated permissions.");
        oshi.add("Sessions:");
        for (OSSession s : os.getSessions()) {
            oshi.add(" " + s.toString());
        }
    }

    private void printComputerSystem(final List<String> oshi, final ComputerSystem computerSystem) {
        oshi.add("System: " + computerSystem.toString());
        oshi.add(" Firmware: " + computerSystem.getFirmware().toString());
        oshi.add(" Baseboard: " + computerSystem.getBaseboard().toString());
    }

    private void printMemory(final List<String> oshi, GlobalMemory memory) {
        oshi.add("Physical Memory via OSHI: \n " + memory.toString());
        oshi.add("Physical Total Memory via OSHI: \n " + memory.getTotal() + "/" + FormatUtil.formatBytes(memory.getTotal()));
        oshi.add("Physical Available Memory via OSHI: \n " + memory.getAvailable() + "/" + FormatUtil.formatBytes(memory.getAvailable()));
        oshi.add("Physical Used Memory via OSHI: \n " + (memory.getTotal() - memory.getAvailable()) + "/" + FormatUtil.formatBytes(memory.getTotal() - memory.getAvailable()));
        VirtualMemory vm = memory.getVirtualMemory();
        oshi.add("Virtual Memory via OSHI: \n " + vm.toString());
        List<PhysicalMemory> pmList = memory.getPhysicalMemory();
        if (!pmList.isEmpty()) {
            oshi.add("Physical Memory via OSHI: ");
            for (PhysicalMemory pm : pmList) {
                oshi.add(" " + pm.toString());
            }
        }

        OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        oshi.add("Total Memory via JDK: \n " + bean.getTotalPhysicalMemorySize() + "/" + FormatUtil.formatBytes(bean.getTotalPhysicalMemorySize()));
        oshi.add("Free Memory via JDK: \n " + bean.getFreePhysicalMemorySize() + "/" + FormatUtil.formatBytes(bean.getFreePhysicalMemorySize()));
        oshi.add("Used Memory via JDK: \n " + (bean.getTotalPhysicalMemorySize() - bean.getFreePhysicalMemorySize()) + "/" + FormatUtil.formatBytes(bean.getTotalPhysicalMemorySize() - bean.getFreePhysicalMemorySize()));
    }

    private void printCpu(final List<String> oshi, CentralProcessor processor) {
        // oshi.add("Context Switches/Interrupts: " + processor.getContextSwitches() + " / " + processor.getInterrupts());
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        long[][] prevProcTicks = processor.getProcessorCpuLoadTicks();
        // oshi.add("CPU, IOWait, and IRQ ticks @ 0 sec:" + Arrays.toString(prevTicks));
        // Wait a second...
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        // oshi.add("CPU, IOWait, and IRQ ticks @ 1 sec:" + Arrays.toString(ticks));
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;
        oshi.add(String.format(Locale.ROOT,
                "User: %.1f%% Nice: %.1f%% System: %.1f%% Idle: %.1f%% IOwait: %.1f%% IRQ: %.1f%% SoftIRQ: %.1f%% Steal: %.1f%%",
                100d * user / totalCpu, 100d * nice / totalCpu, 100d * sys / totalCpu, 100d * idle / totalCpu,
                100d * iowait / totalCpu, 100d * irq / totalCpu, 100d * softirq / totalCpu, 100d * steal / totalCpu));
        // oshi.add(String.format(Locale.ROOT, "CPU load: %.1f%%", processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100));
        // double[] loadAverage = processor.getSystemLoadAverage(3);
        // oshi.add("CPU load averages:"
        //         + (loadAverage[0] < 0 ? " N/A" : String.format(Locale.ROOT, " %.2f", loadAverage[0]))
        //         + (loadAverage[1] < 0 ? " N/A" : String.format(Locale.ROOT, " %.2f", loadAverage[1]))
        //         + (loadAverage[2] < 0 ? " N/A" : String.format(Locale.ROOT, " %.2f", loadAverage[2])));
        // per core CPU
        // StringBuilder procCpu = new StringBuilder("CPU load per processor:");
        // double[] load = processor.getProcessorCpuLoadBetweenTicks(prevProcTicks);
        // for (double avg : load) {
        //     procCpu.append(String.format(Locale.ROOT, " %.1f%%", avg * 100));
        // }
        // oshi.add(procCpu.toString());
        // long freq = processor.getProcessorIdentifier().getVendorFreq();
        // if (freq > 0) {
        //     oshi.add("Vendor Frequency: " + FormatUtil.formatHertz(freq));
        // }
        // freq = processor.getMaxFreq();
        // if (freq > 0) {
        //     oshi.add("Max Frequency: " + FormatUtil.formatHertz(freq));
        // }
        // long[] freqs = processor.getCurrentFreq();
        // if (freqs[0] > 0) {
        //     StringBuilder sb = new StringBuilder("Current Frequencies: ");
        //     for (int i = 0; i < freqs.length; i++) {
        //         if (i > 0) {
        //             sb.append(", ");
        //         }
        //         sb.append(FormatUtil.formatHertz(freqs[i]));
        //     }
        //     oshi.add(sb.toString());
        // }
        // if (!processor.getFeatureFlags().isEmpty()) {
        //     oshi.add("CPU Features:");
        //     for (String features : processor.getFeatureFlags()) {
        //         oshi.add("  " + features);
        //     }
        // }
    }

    private void printProcesses(final List<String> oshi, OperatingSystem os, GlobalMemory memory) {
        OSProcess myProc = os.getProcess(os.getProcessId());
        // current process will never be null. Other code should check for null here
        oshi.add("My PID: " + myProc.getProcessID() + " with affinity " + Long.toBinaryString(myProc.getAffinityMask()));
        // oshi.add("My TID: " + os.getThreadId() + " with details " + os.getCurrentThread());
        // oshi.add("Processes: " + os.getProcessCount() + ", Threads: " + os.getThreadCount());
        // Sort by highest CPU
        // List<OSProcess> procs = os.getProcesses(OperatingSystem.ProcessFiltering.ALL_PROCESSES, OperatingSystem.ProcessSorting.CPU_DESC, 5);
        List<OSProcess> procs = Collections.singletonList(os.getProcess(os.getProcessId()));
        oshi.add("   PID  %CPU %MEM       VSZ       RSS Name");
        for (int i = 0; i < procs.size(); i++) {
            OSProcess p = procs.get(i);
            oshi.add(String.format(Locale.ROOT, " %5d %5.1f %4.1f %9s %9s %s", p.getProcessID(),
                    100d * (p.getKernelTime() + p.getUserTime()) / p.getUpTime(),
                    100d * p.getResidentSetSize() / memory.getTotal(), FormatUtil.formatBytes(p.getVirtualSize()),
                    FormatUtil.formatBytes(p.getResidentSetSize()), p.getName()));
        }
        // OSProcess p = os.getProcess(os.getProcessId());
        // oshi.add("Current process arguments: ");
        // for (String s : p.getArguments()) {
        //     oshi.add("  " + s);
        // }
        // oshi.add("Current process environment: ");
        // for (Map.Entry<String, String> e : p.getEnvironmentVariables().entrySet()) {
        //     oshi.add("  " + e.getKey() + "=" + e.getValue());
        // }
    }
}