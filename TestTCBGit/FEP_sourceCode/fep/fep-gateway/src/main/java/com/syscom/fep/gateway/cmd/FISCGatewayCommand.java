package com.syscom.fep.gateway.cmd;

/**
 * 通過執行本程序, 用來給FISC Gateway傳送不同的指令執行不同的動作
 * <p>
 * 1.獲取monitor資料
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-fisc.jar com.syscom.fep.gateway.cmd.FISCGatewayCommand -f monitor -d "action=get"
 * <p>
 * 2.start/stop Primary/Secondary
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-fisc.jar com.syscom.fep.gateway.cmd.FISCGatewayCommand -f operate -d "mode=primary&action=start"
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-fisc.jar com.syscom.fep.gateway.cmd.FISCGatewayCommand -f operate -d "mode=primary&action=stop"
 * <p>
 * 3.start/stop All
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-fisc.jar com.syscom.fep.gateway.cmd.FISCGatewayCommand -f operate -d "action=start"
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-fisc.jar com.syscom.fep.gateway.cmd.FISCGatewayCommand -f operate -d "action=stop"
 * <p>
 * 4.check
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-fisc.jar com.syscom.fep.gateway.cmd.FISCGatewayCommand -f operate -d "action=check"
 * <p>
 */
public class FISCGatewayCommand extends GatewayCommand {
    private static final String VER = "1.2.0";

    public static void main(String[] args) {
        new FISCGatewayCommand().execute(args);
    }

    @Override
    protected Gateway getGateway() {
        return Gateway.FISC;
    }

    @Override
    protected int getDefaultPort() {
        return 8301;
    }

    @Override
    protected String displayUsage() {
        printWelcome(VER);
        StringBuilder sb = new StringBuilder();
        sb.append("Usage:\r\n");
        sb.append("\t1 Show Help:\r\n\t\t").append(GatewayCommandArgs.Help.getParam()).append("\r\n");
        sb.append("\t2 Monitor Data:\r\n");
        sb.append("\t\t2-a) Get Monitor Data\r\n\t\t\t -f monitor -d action=get\r\n");
        sb.append("\t3 Operate Primary/Secondary Gateway:\r\n");
        sb.append("\t\t3-a) Start Primary Gateway\r\n\t\t\t -f operate -d mode=primary&action=start\r\n");
        sb.append("\t\t3-b) Stop Primary Gateway\r\n\t\t\t -f operate -d mode=primary&action=stop\r\n");
        sb.append("\t\t3-c) Start Secondary Gateway\r\n\t\t\t -f operate -d mode=secondary&action=start\r\n");
        sb.append("\t\t3-d) Stop Secondary Gateway\r\n\t\t\t -f operate -d mode=secondary&action=stop\r\n");
        sb.append("\t\t3-e) Start All Gateway\r\n\t\t\t -f operate -d action=start\r\n");
        sb.append("\t\t3-f) Stop All Gateway\r\n\t\t\t -f operate -d action=stop\r\n");
        sb.append("\t\t3-g) Check Gateway Status\r\n\t\t\t -f operate -d action=check\r\n");
        return GatewayCommandUtil.printOut(sb.toString());
    }

    @Override
    protected String function(String ip, int port, GatewayCommandFunc func, String[] args) {
        String url = null;
        if (func == GatewayCommandFunc.monitor
                || func == GatewayCommandFunc.operate) {
            url = getUrl(ip, port, PROP_FILENAME_FISCGW, PROP_NAME_FISCGW_HOST, func);
        } else {
            url = getUrl(PROP_FILENAME_FISCGW, PROP_NAME_FISCGW_HOST, func);
        }
        // 找data參數
        String data = findArg(args, GatewayCommandArgs.Data.getParam());
        switch (func) {
            case monitor:
                if (GatewayCommandUtil.isBlank(data)) {
                    data = "action=get";
                }
                return GatewayCommandUtil.printOut(httpPost(url, new String[] {data}));
            case operate:
                if (GatewayCommandUtil.isBlank(data)) {
                    data = "mode=primary&action=start";
                }
                return GatewayCommandUtil.printOut(httpPost(url, new String[] {data}));
            default:
                return GatewayCommandUtil.printOut(httpPost(url, null));
        }
    }
}
