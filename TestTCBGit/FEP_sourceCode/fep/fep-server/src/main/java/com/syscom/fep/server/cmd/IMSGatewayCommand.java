package com.syscom.fep.server.cmd;

import com.syscom.fep.frmcommon.io.Command;
import com.syscom.fep.server.gateway.ims.IMSGatewayCmdAction;
import com.syscom.fep.server.gateway.ims.IMSGatewayMode;
import org.apache.commons.lang3.StringUtils;

public class IMSGatewayCommand extends Command {

    public static void main(String[] args) {
        new IMSGatewayCommand().execute(args);
    }

    /**
     * 顯示Usage
     */
    @Override
    protected void displayUsage() {
        System.out.println("USAGE:");
        System.out.println(" IMSGatewayCommand Options");
        System.out.println();
        System.out.println(" Options:");
        System.out.println(" /? Display this help message.");
        System.out.println(" /Host Required.");
        System.out.println(" /Port Required.");
        System.out.println(" /Mode Optional. primary/secondary");
        System.out.println(" /Action Required. start/stop/check");
        System.out.println(" /Operator Optional.");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println(" IMSGatewayCommand /Host:127.0.0.1 /Port:8213 /Mode:primary /Action:start"); ;
    }

    /**
     * 執行
     *
     * @param args
     * @throws Exception
     */
    @Override
    protected void process(String[] args) throws Exception {
        String host = this.findArg(args, "Host", "");
        if (checkEmpty("Host", host))
            return;
        String port = this.findArg(args, "Port", "");
        if (checkEmpty("Port", port))
            return;
        String action = this.findArg(args, "Action", "");
        if (checkEmpty("Action", action))
            return;
        IMSGatewayCmdAction imsGatewayCmdAction = null;
        try {
            imsGatewayCmdAction = Enum.valueOf(IMSGatewayCmdAction.class, action);
        } catch (IllegalArgumentException e) {
            this.error(e, "Invalid Input Action:", action);
            return;
        }
        IMSGatewayMode imsGatewayMode = null;
        // 如果是check是不需要mode的
        if (imsGatewayCmdAction != IMSGatewayCmdAction.check) {
            String mode = this.findArg(args, "Mode", "");
            if (checkEmpty("Mode", mode))
                return;
            try {
                imsGatewayMode = Enum.valueOf(IMSGatewayMode.class, mode);
            } catch (IllegalArgumentException e) {
                this.error(e, "Invalid Input Mode:", action);
                return;
            }
        }
        String operator = this.findArg(args, "Operator", "");
        String result = this.httpPost(StringUtils.join("http://", host, ":", port, "/recv/ims/channel"),
                new String[] {
                        "mode=" + (imsGatewayMode == null ? "" : imsGatewayMode.name()),
                        "action=" + imsGatewayCmdAction.name(),
                        "operator=" + operator});
        System.out.println(result);
    }
}