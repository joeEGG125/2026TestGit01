package com.syscom.fep.frmcommon.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CommandLineUtil {

    private static final String[] dangerousCommands = {
            "rm", "del", "mv", "move", "cp", "copy", "chmod", "icacls",
            "touch", "mkdir", "type nul >", "reboot", "shutdown",
            "sysctl", "regedit", "useradd", "usermod", "sh", "bash", "cmd",
            "eval", "&&", "|", ">", ">>", "<", "sudo", "su", "runas",
            "export", "set", "cat", "type", "less", "more", "notepad"
    };

    private CommandLineUtil() {
    }

    public static boolean existArg(String[] args, String found) {
        try {
            for (int i = 0; i < args.length; i++) {
                if (found.equals(args[i])) {
                    return true;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw e;
        }
        return false;
    }

    public static String findArg(String[] args, String found) {
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

    /**
     * 如果使用危險命令，則返回false，否則返回true
     *
     * @param command
     * @return
     */
    public static boolean isCommandSafe(String command) {
        String[] words = command.split("\\s+");
        for (String word : words) {
            for (String comd : dangerousCommands) {
                if (word.equals(comd)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Process getProcess(String[] cmdarray) {
        Runtime runtime = ReflectUtil.envokeStaticMethod(Runtime.class, "getRuntime", Runtime.getRuntime(), true, true);
        return ReflectUtil.envokeMethod(runtime, "exec", new Class[] {String[].class}, new Object[] {cmdarray}, null, true);
    }

    public static List<String> execute(String[] cmdarray) throws Exception {
        Process process = getProcess(cmdarray);
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
            process.waitFor();
        }
        return result;
    }

//    public static void main(String[] args) throws IOException {
//        Process p = getProcess(new String[] {"cmd", "/c", "ping 127.0.0.1"});
//        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GBK"));
//        String line = null;
//        while ((line = br.readLine()) != null) {
//            System.out.println(line);
//        }
//    }
}
