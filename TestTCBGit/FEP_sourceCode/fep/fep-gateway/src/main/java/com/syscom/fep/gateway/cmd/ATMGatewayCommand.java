package com.syscom.fep.gateway.cmd;

import com.syscom.fep.frmcommon.esapi.ESAPIConfiguration;
import com.syscom.fep.frmcommon.io.ConsoleIn;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import org.apache.commons.io.IOUtils;

import java.util.Calendar;
import java.util.List;

/**
 * 通過執行本程序, 用來給ATM Gateway傳送不同的指令執行不同的動作
 * <p>
 * 1.列舉當前所有的憑證
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f ssllist
 * <p>
 * 2.列舉當前啟用的憑證
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f ssllist -d "deactivated=false"
 * <p>
 * 3.列舉當前停用的憑證
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f ssllist -d "deactivated=true"
 * <p>
 * 4.停用憑證
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f ssldeactivate -d "index=1"
 * <p>
 * 5.啟用憑證
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f sslactivate -d "index=1"
 * <p>
 * 6.獲取monitor資料
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f monitor -d "action=get"
 * <p>
 * 7.獲取ATM Client的列表
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f clientlist -d "atmStatus=0"
 * <p>
 */
public class ATMGatewayCommand extends GatewayCommand {
    private static final String VER = "1.3.0";

    public static void main(String[] args) {
        new ATMGatewayCommand().execute(args);
    }

    @Override
    protected Gateway getGateway() {
        return Gateway.ATM;
    }

    @Override
    protected int getDefaultPort() {
        return 8300;
    }

    @Override
    protected String displayUsage() {
        printWelcome(VER);
        StringBuilder sb = new StringBuilder();
        sb.append("Usage:\r\n");
        sb.append("\t1 Show Help:\r\n\t\t-h\r\n");
        sb.append("\t2 SSL Certificate:\r\n");
        int a = 'a';
        sb.append("\t\t2-").append((char) (a++)).append(") Get SSL Certificate list\r\n\t\t\t -f ssllist -d deactivated={true|false}\r\n");
        // sb.append("\t\t2-").append((char) (a++)).append(") Add SSL Certificate\r\n\t\t\t -f ssladd -d file=atmgw-certificate.p12&sscode=syscom&type=PKCS12\r\n");
        sb.append("\t\t2-").append((char) (a++)).append(") Deactivate SSL Certificate by index {number}\r\n\t\t\t -f ssldeactivate -d index={number}\r\n");
        sb.append("\t\t2-").append((char) (a++)).append(") Activate SSL Certificate by index {number}\r\n\t\t\t -f sslactivate -d index={number}\r\n");
        sb.append("\t3 Monitor Data:\r\n");
        sb.append("\t\t3-a) Get Monitor Data\r\n\t\t\t -f monitor -d action=get&listClient=true\r\n");
        sb.append("\t\t3-b) List Client\r\n\t\t\t -f clientlist -d atmStatus={0|1}\r\n");
        return GatewayCommandUtil.printOut(sb.toString());
    }

    @Override
    protected String function(String ip, int port, GatewayCommandFunc func, String[] args) {
        String url = null;
        // 目前只有monitor可以查詢遠程GW資料
        // if (func == GatewayCommandFunc.monitor) {
        url = getUrl(ip, port, PROP_FILENAME_ATMGW, PROP_NAME_ATMGW_HOST, func);
        // } else {
        //     url = getUrl(PROP_FILENAME_ATMGW, PROP_NAME_ATMGW_HOST, func);
        // }
        // 找data參數
        String data = findArg(args, GatewayCommandArgs.Data.getParam());
        switch (func) {
            case monitor:
            case clientlist:
            case ssllist:
            case ssllistshort:
                // case ssladd:
            case ssldeactivate:
            case sslactivate:
                if (GatewayCommandUtil.isBlank(data)) {
                    // monitor
                    if (func == GatewayCommandFunc.monitor) {
                        data = "action=get&listClient=true";
                    }
                    // ssl add step by step
                    // else if (func == GatewayCommandFunc.ssladd) {
                    //     return this.executeSslAdd(ip, port, func);
                    // }
                    // ssl deactivate step by step
                    else if (func == GatewayCommandFunc.ssldeactivate) {
                        return this.executeSSLDeactivate(ip, port, func);
                    }
                    // ssl activate step by step
                    else if (func == GatewayCommandFunc.sslactivate) {
                        return this.executeSSLActivate(ip, port, func);
                    } else {
                        return GatewayCommandUtil.printOut(httpPost(url, null));
                    }
                }
                return GatewayCommandUtil.printOut(httpPost(url, new String[] {data}));
            default:
                // 如果沒找到, 直接顯示usage
                return displayUsage();
        }
    }

    private String executeSslAdd(String ip, int port, GatewayCommandFunc func) {
        printWelcome(VER);
        ConsoleIn console = new ConsoleIn();
        GatewayCommandUtil.printOutLn("Please enter new Certificate file name, or Press ", GatewayCommandConsoleCmd.EXIT.getDescription());
        String fileName = getInput(console, false);
        String ssCode = null;
        while (true) {
            GatewayCommandUtil.printOutLn("Please enter new Certificate password, or Press ", GatewayCommandConsoleCmd.EXIT.getDescription());
            ssCode = getInput(console, true);
            GatewayCommandUtil.printOutLn("Please confirm new Certificate password, or Press ", GatewayCommandConsoleCmd.EXIT.getDescription());
            String confirm = getInput(console, true);
            if (!ssCode.equals(confirm)) {
                GatewayCommandUtil.printOutLn("The password is entered twice inconsistently!!");
                continue;
            }
            if (console.isConsole()) GatewayCommandUtil.printOutLn("The new Certificate password in masking is [", GatewayCommandUtil.repeat('*', ssCode.length()), "]");
            break;
        }
        return this.function(ip, port, func, new String[] {
                GatewayCommandArgs.Data.getParam(),
                GatewayCommandUtil.join("file=", fileName, "&sscode=", ssCode)
        });
    }

    /**
     * 停用憑證
     *
     * @param ip
     * @param port
     * @param func
     * @return
     */
    private String executeSSLDeactivate(String ip, int port, GatewayCommandFunc func) {
        printWelcome(VER);
        String response = function(ip, port, GatewayCommandFunc.ssllistshort, new String[] {GatewayCommandArgs.Data.getParam(), "deactivated=false"});
        if (!GatewayCommandUtil.isBlank(response) && response.contains("ATM Gateway SSL Certificate List")) {
            List<String> list = IOUtils.readLines(response);
            // 移除掉第一行的Title
            list = list.subList(1, list.size());
            ConsoleIn console = new ConsoleIn();
            String input;
            int index;
            String selected;
            while (true) {
                GatewayCommandUtil.printOutLn("Please enter the Certificate index to deactivate, or Press ", GatewayCommandConsoleCmd.EXIT.getDescription());
                input = getInput(console, false);
                if (!GatewayCommandUtil.isNumeric(input)) {
                    GatewayCommandUtil.printOutLn("The Certificate index must be numeric!!");
                    continue;
                }
                index = Integer.parseInt(input);
                if (index < 0 || index >= list.size()) {
                    GatewayCommandUtil.printOutLn("The Certificate index must between 0 and ", list.size() - 1, "!!");
                    continue;
                }
                selected = list.get(index);
                // 再次確認是否停用
                GatewayCommandUtil.printOutLn("The selected SSL Certification:\r\n", selected, "\r\n\r\nInput ", GatewayCommandConsoleCmd.YES.getDescription(), " to deactivate, or ", GatewayCommandConsoleCmd.NO.getDescription(), " to discard, or Press ", GatewayCommandConsoleCmd.EXIT.getDescription());
                input = getInput(console, false);
                if (GatewayCommandConsoleCmd.YES.getInput().equalsIgnoreCase(input)) {
                    break;
                }
            }
            response = this.function(ip, port, func, new String[] {
                    GatewayCommandArgs.Data.getParam(),
                    GatewayCommandUtil.join("index=", index)
            });
        }
        return response;
    }

    /**
     * 啟用憑證
     *
     * @param ip
     * @param port
     * @param func
     * @return
     */
    private String executeSSLActivate(String ip, int port, GatewayCommandFunc func) {
        printWelcome(VER);
        String response = function(ip, port, GatewayCommandFunc.ssllistshort, new String[] {GatewayCommandArgs.Data.getParam(), "deactivated=true"});
        if (!GatewayCommandUtil.isBlank(response) && response.contains("ATM Gateway SSL Certificate List")) {
            List<String> list = IOUtils.readLines(response);
            // 移除掉第一行的Title
            list = list.subList(1, list.size());
            ConsoleIn console = new ConsoleIn();
            String input;
            int index;
            String selected;
            String[] certs;
            int validDateBegin, validDateEnd, currentDate;
            while (true) {
                GatewayCommandUtil.printOutLn("Please enter certificate index to activate, or Press ", GatewayCommandConsoleCmd.EXIT.getDescription());
                input = getInput(console, false);
                if (!GatewayCommandUtil.isNumeric(input)) {
                    GatewayCommandUtil.printOutLn("The certificate index must be numeric!!");
                    continue;
                }
                index = Integer.parseInt(input);
                if (index < 0 || index >= list.size()) {
                    GatewayCommandUtil.printOutLn("The certificate index must between 0 and ", list.size() - 1, "!!");
                    continue;
                }
                selected = list.get(index);
                certs = selected.split("\\|");
                try {
                    validDateBegin = Integer.parseInt(certs[4].replaceAll("/", ""));
                    validDateEnd = Integer.parseInt(certs[5].replaceAll("/", ""));
                    currentDate = CalendarUtil.dateValue(Calendar.getInstance());
                    if (currentDate < validDateBegin || currentDate > validDateEnd) {
                        GatewayCommandUtil.printOutLn("Cannot activate, Certificate was expired");
                        continue;
                    }
                    // 再次確認是否啟用
                    GatewayCommandUtil.printOutLn("The selected SSL Certification:\r\n", selected, "\r\n\r\nInput ", GatewayCommandConsoleCmd.YES.getDescription(), " to activate, or ", GatewayCommandConsoleCmd.NO.getDescription(), " to discard, or Press ", GatewayCommandConsoleCmd.EXIT.getDescription());
                    input = getInput(console, false);
                    if (GatewayCommandConsoleCmd.YES.getInput().equalsIgnoreCase(input)) {
                        break;
                    }
                } catch (Exception e) {
                    return GatewayCommandUtil.join("Cannot activate cause parse SSL Certification with exception occur, ", e.getMessage());
                }
            }
            response = this.function(ip, port, func, new String[] {
                    GatewayCommandArgs.Data.getParam(),
                    GatewayCommandUtil.join("index=", index)
            });
        }
        return response;
    }
}
