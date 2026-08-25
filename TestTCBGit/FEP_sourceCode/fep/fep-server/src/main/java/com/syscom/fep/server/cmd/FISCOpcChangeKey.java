package com.syscom.fep.server.cmd;

import com.syscom.fep.frmcommon.io.Command;

public class FISCOpcChangeKey extends Command {

    public static void main(String[] args) {
        new FISCOpcChangeKey().execute(args);
    }

    /**
     * 顯示Usage
     */
    @Override
    protected void displayUsage() {
        System.out.println("USAGE:");
        System.out.println(" FISCOpcChangeKey Options");
        System.out.println();
        System.out.println(" Options:");
        System.out.println(" /? Display this help message.");
        System.out.println(" /Host Required.");
        System.out.println(" /Port Required.");
        System.out.println(" /KeyId Required.");
        System.out.println(" /Operator Optional.");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println(" FISCCmdOpcChangeKey /Host:127.0.0.1 /Port:8101 /KeyId:04");
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
        String keyId = this.findArg(args, "KeyId", "");
        if (checkEmpty("KeyId", keyId))
            return;
        String operator = this.findArg(args, "Operator", "");
        String result = this.httpPost("http://" + host + ":" + port + "/opc/changekey", new String[] {"keyId=" + keyId, "operator=" + operator});
        System.out.println(result);
    }
}
