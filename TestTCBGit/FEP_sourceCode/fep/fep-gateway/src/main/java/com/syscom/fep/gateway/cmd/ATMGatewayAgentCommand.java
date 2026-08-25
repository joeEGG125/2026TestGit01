package com.syscom.fep.gateway.cmd;

/**
 * 通過執行本程序, 用來給ATM Gateway Agent傳送不同的指令執行不同的動作
 * <p>
 * 1.指定ATM Service的IP
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm-agent.jar com.syscom.fep.gateway.cmd.ATMGatewayAgentCommand -f changefepap -d "host=127.0.0.1"
 * <p>
 * 2.指定ATM Service的index
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm-agent.jar com.syscom.fep.gateway.cmd.ATMGatewayAgentCommand -f changefepap -d "host=0"
 * <p>
 * 3.重置回RoundRobin方式
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm-agent.jar com.syscom.fep.gateway.cmd.ATMGatewayAgentCommand -f changefepap
 * <p>
 * 4.取得當前打到哪個ATM Service
 * <p>
 * java -Dfile.encoding=UTF-8 -cp fep-gateway-atm-agent.jar com.syscom.fep.gateway.cmd.ATMGatewayAgentCommand -f checkfepap
 * <p>
 *
 * @author Richard
 */
public class ATMGatewayAgentCommand extends GatewayCommand {
    private static final String VER = "1.2.0";

    public static void main(String[] args) {
        new ATMGatewayAgentCommand().execute(args);
    }

    @Override
    protected Gateway getGateway() {
        return Gateway.ATM;
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
        sb.append("\t1 Show Help:\r\n\t\t-h\r\n");
        sb.append("\t2 Change FEP ATM Service Host:\r\n");
        sb.append("\t\t2-a) Change by IP\r\n\t\t\t -f changefepap -d host=127.0.0.1\r\n");
        sb.append("\t\t2-b) Change by Index\r\n\t\t\t -f changefepap -d host=0\r\n");
        sb.append("\t\t2-b) Rest to RoundRobin\r\n\t\t\t -f changefepap\r\n");
        sb.append("\t3 Check FEP ATM Service Host:\r\n");
        sb.append("\t\t3-a) Check FEP ATM Service\r\n\t\t\t -f checkfepap\r\n");
        return GatewayCommandUtil.printOut(sb.toString());
    }

    @Override
    protected String function(String ip, int port, GatewayCommandFunc func, String[] args) {
        // 獲取url
        String url = getUrl(ip, port, PROP_FILENAME_ATMGW_AGENT, PROP_NAME_ATMGW_AGENT_HOST, func);
        // 找data參數
        String data = findArg(args, GatewayCommandArgs.Data.getParam());
        switch (func) {
            case changefepap:
            case checkfepap:
                return GatewayCommandUtil.printOut(httpPost(url, new String[] {data}));
            default:
                return GatewayCommandUtil.printOut(httpPost(url, null));
        }
    }
}
