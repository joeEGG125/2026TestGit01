package com.syscom.fep.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class CommandUtil {
// 2022-08-26 Richard marked start
// 暫時mark起來以通過 Fortify 弱掃
//    public static String run(String command) throws IOException {
//        Scanner input = null;
//        String result = "";
//        Process process = null;
//        try {
//            process = Runtime.getRuntime().exec(command);
//            try {
//
//                process.waitFor(10, TimeUnit.SECONDS);
//            } catch (InterruptedException e) {
//            }
//            InputStream is = process.getInputStream();
//            input = new Scanner(is);
//            while (input.hasNextLine()) {
//                result += input.nextLine() + "\n";
//            }
//            result = command + "\n" + result;
//        } finally {
//            if (input != null) {
//                input.close();
//            }
//            if (process != null) {
//                process.destroy();
//            }
//        }
//        return result;
//    }
// 2022-08-26 Richard marked start
}
