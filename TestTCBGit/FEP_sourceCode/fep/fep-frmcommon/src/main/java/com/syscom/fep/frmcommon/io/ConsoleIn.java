package com.syscom.fep.frmcommon.io;

import java.io.Console;
import java.util.Scanner;

public class ConsoleIn {
    private Console console;
    private Scanner scanner;

    public ConsoleIn() {
        this.console = System.console();
        // 在IDE下, console會是null
        if (this.console == null) {
            this.scanner = new Scanner(System.in);
        }
    }

    public String readLine(boolean sscode) {
        if (isConsole()) {
            if (sscode) {
                char[] chars = this.console.readPassword();
                StringBuffer sb = new StringBuffer();
                for (char c : chars) {
                    sb.append(c);
                }
                return sb.toString();
            }
            return this.console.readLine();
        }
        return this.scanner.nextLine();
    }

    public boolean isConsole() {
        return this.console != null;
    }
}
