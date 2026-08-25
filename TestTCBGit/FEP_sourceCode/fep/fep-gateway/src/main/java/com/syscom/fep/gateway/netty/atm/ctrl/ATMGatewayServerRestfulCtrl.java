package com.syscom.fep.gateway.netty.atm.ctrl;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.ssl.SslKeyTrustType;
import com.syscom.fep.gateway.entity.AtmStatus;
import com.syscom.fep.gateway.netty.atm.ATMGatewayServerToFEPATM;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 透過Restful接收訊息下指令給ATMGateway
 *
 * @author Richard
 */
public class ATMGatewayServerRestfulCtrl extends ATMGatewayServerCtrl {

// 2022-08-23 Richard marked
// monhelper的程式可以都點掉, 目前ATMGW不會收ATMMon的資料
//	@RequestMapping(value = "/recv/atm", method = RequestMethod.POST, produces = "application/x-www-form-urlencoded")
//	@ResponseBody
//	public void onMessage(@RequestParam(value = "atmNo") String atmNo, @RequestParam(value = "data") String data) throws Exception {
// 		this.sendMessageToATM(atmNo, data);
//	}

//    /**
//     * 可以使用如下指令切換ssl key的index
//     * <p>
//     * curl -X POST http://localhost:8300/recv/atm/sslswitch
//     *
//     * @return
//     */
//    @RequestMapping(value = "/recv/atm/sslswitch", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//    @ResponseBody
//    public String onMessageSslKeySwitch() {
//        putMDC();
//        return this.onMessageSslKeyChange(null);
//    }

//    /**
//     * 可以使用如下指令設定ssl key的index
//     * <p>
//     * curl -d "index=1" -X POST http://localhost:8300/recv/atm/sslchange
//     *
//     * @param index
//     * @return
//     */
//    @RequestMapping(value = "/recv/atm/sslchange", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//    @ResponseBody
//    public String onMessageSslKeyChange(@RequestParam(value = "index") Integer index) {
//        putMDC();
//        try {
//            String sslInformation = this.sslKeySwitch(index);
//            return StringUtils.join("ATM Gateway SSL Certificate has been changed\r\n", sslInformation);
//        } catch (Exception e) {
//            return StringUtils.join("ATM Gateway SSL Certificate change failed cause \"", e.getMessage(), "\"\r\n");
//        }
//    }

//    /**
//     * 可以使用如下指令獲取當前使用中的憑證
//     * <p>
//     * curl -X POST http://localhost:8300/recv/atm/sslnow
//     *
//     * @return
//     */
//    @RequestMapping(value = "/recv/atm/sslnow", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//    @ResponseBody
//    public String onMessageSslKeyNow() {
//        putMDC();
//        try {
//            String sslInformation = this.getCurrentSslKey();
//            if (StringUtils.isBlank(sslInformation)) {
//                return "Empty SSL Certificate List!!!\r\n";
//            }
//            return StringUtils.join("ATM Gateway SSL Certificate in use now\r\n", sslInformation);
//        } catch (Exception e) {
//            return StringUtils.join("Get ATM Gateway SSL Certificate failed cause \"", e.getMessage(), "\"\r\n");
//        }
//    }

    /**
     * 可以使用如下指令獲取所有的憑證
     * <p>
     * curl -d "deactivated=false" -X POST http://localhost:8300/recv/atm/ssllist
     *
     * @return
     */
    @RequestMapping(value = "/recv/atm/ssllist", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageSslKeyList(@RequestParam(value = "deactivated", required = false, defaultValue = StringUtils.EMPTY) String deactivated) {
        putMDC();
        String message;
        LogData logData = new LogData();
        try {
            String sslInformation = this.listSslKey(logData, deactivated);
            logData.setSubSys(SubSystem.GW);
            logData.setChannel(FEPChannel.ATM);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramFlowType(ProgramFlow.ATMGatewayOut);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageSslKeyList"));
            if (StringUtils.isBlank(sslInformation)) {
                message = "Empty SSL Certificate List!!!\r\n";
                logData.setRemark(message);
                logMessage(Level.WARN, logData);
            } else {
                message = StringUtils.join("ATM Gateway SSL Certificate List\r\n", sslInformation);
                logData.setRemark(message);
                logMessage(logData);
            }
        } catch (Exception e) {
            message = StringUtils.join("Get ATM Gateway SSL Certificate List failed cause \"", e.getMessage(), "\"\r\n");
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageSslKeyList"));
            logData.setRemark(message);
            logMessage(Level.WARN, logData);
        }
        return message;
    }

    /**
     * 可以使用如下指令獲取所有的憑證
     * <p>
     * curl -d "deactivated=false" -X POST http://localhost:8300/recv/atm/ssllistshort
     *
     * @return
     */
    @RequestMapping(value = "/recv/atm/ssllistshort", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageSslKeyListShort(@RequestParam(value = "deactivated", required = false, defaultValue = StringUtils.EMPTY) String deactivated) {
        putMDC();
        String message;
        LogData logData = new LogData();
        try {
            String sslInformation = this.listSslKeyShort(logData, deactivated);
            logData.setSubSys(SubSystem.GW);
            logData.setChannel(FEPChannel.ATM);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramFlowType(ProgramFlow.ATMGatewayOut);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageSslKeyListShort"));
            if (StringUtils.isBlank(sslInformation)) {
                message = "Empty SSL Certificate List!!!\r\n";
                logData.setRemark(message);
                logMessage(Level.WARN, logData);
            } else {
                message = StringUtils.join("ATM Gateway SSL Certificate List\r\n", sslInformation);
                logData.setRemark(message);
                logMessage(logData);
            }
        } catch (Exception e) {
            message = StringUtils.join("Get ATM Gateway SSL Certificate List failed cause \"", e.getMessage(), "\"\r\n");
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageSslKeyListShort"));
            logData.setRemark(message);
            logMessage(Level.WARN, logData);
        }
        return message;
    }

    /**
     * 可以使用如下指令停用ssl key的index
     * <p>
     * curl -d "index=1" -X POST http://localhost:8300/recv/atm/ssldeactivate
     *
     * @param index
     * @return
     */
    @RequestMapping(value = "/recv/atm/ssldeactivate", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageSslKeyDeactivate(@RequestParam(value = "index") Integer index) {
        putMDC();
        String message;
        LogData logData = new LogData();
        try {
            String sslInformation = this.sslKeyDeactivate(logData, index);
            message = StringUtils.join("ATM Gateway SSL Certificate has been deactivated\r\n", sslInformation);
            logData.setSubSys(SubSystem.GW);
            logData.setChannel(FEPChannel.ATM);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramFlowType(ProgramFlow.ATMGatewayOut);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageSslKeyDeactivate"));
            logData.setRemark(message);
            logMessage(logData);
        } catch (Exception e) {
            message = StringUtils.join("ATM Gateway SSL Certificate deactivate failed cause \"", e.getMessage(), "\"\r\n");
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageSslKeyDeactivate"));
            logData.setRemark(message);
            logMessage(Level.WARN, logData);
        }
        return message;
    }

    /**
     * 可以使用如下指令啟用ssl key的index
     * <p>
     * curl -d "index=1" -X POST http://localhost:8300/recv/atm/sslactivate
     *
     * @param index
     * @return
     */
    @RequestMapping(value = "/recv/atm/sslactivate", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageSslKeyActivate(@RequestParam(value = "index") Integer index) {
        putMDC();
        String message;
        LogData logData = new LogData();
        try {
            String sslInformation = this.sslKeyActivate(logData, index);
            message = StringUtils.join("ATM Gateway SSL Certificate has been activated\r\n", sslInformation);
            logData.setSubSys(SubSystem.GW);
            logData.setChannel(FEPChannel.ATM);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramFlowType(ProgramFlow.ATMGatewayOut);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageSslKeyActivate"));
            logData.setRemark(message);
            logMessage(logData);
        } catch (Exception e) {
            message = StringUtils.join("ATM Gateway SSL Certificate activate failed cause \"", e.getMessage(), "\"\r\n");
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageSslKeyActivate"));
            logData.setRemark(message);
            logMessage(Level.WARN, logData);
        }
        return message;
    }

    /**
     * 可以使用如下指令增加ssl key
     * <p>
     * curl -d "file=root.p12&sscode=syscom" -X POST http://localhost:8300/recv/atm/ssladd
     *
     * @param filename
     * @param sscode
     * @param sslKeyTrustType
     * @return
     */
    @RequestMapping(value = "/recv/atm/ssladd", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageSslKeyAdd(@RequestParam(value = "file") String filename,
                                     @RequestParam(value = "sscode", required = false, defaultValue = StringUtils.EMPTY) String sscode,
                                     @RequestParam(value = "type", required = false, defaultValue = "PKCS12") SslKeyTrustType sslKeyTrustType) {
        putMDC();
        String message;
        LogData logData = new LogData();
        try {
            String sslInformation = this.sslKeyAdd(logData, filename, sscode, sslKeyTrustType);
            message = StringUtils.join("ATM Gateway SSL Certificate has been add\r\n", sslInformation);
            logData.setSubSys(SubSystem.GW);
            logData.setChannel(FEPChannel.ATM);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramFlowType(ProgramFlow.ATMGatewayOut);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageSslKeyAdd"));
            logData.setRemark(message);
            logMessage(logData);
        } catch (Exception e) {
            message = StringUtils.join("ATM Gateway SSL Certificate add failed cause \"", e.getMessage(), "\"\r\n");
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageSslKeyAdd"));
            logData.setRemark(message);
            logMessage(Level.WARN, logData);
        }
        return message;
    }

//    /**
//     * 可以使用如下指令控制ssl alias
//     * <p>
//     * curl -d "action=set&atmIp=127.0.0.1&alias=1.0" -X POST http://localhost:8300/recv/atm/sslalias
//     * <p>
//     * curl -d "action=get&atmIp=127.0.0.1" -X POST http://localhost:8300/recv/atm/sslalias
//     * <p>
//     * curl -d "action=remove&atmIp=127.0.0.1" -X POST http://localhost:8300/recv/atm/sslalias
//     * <p>
//     * curl -d "action=list" -X POST http://localhost:8300/recv/atm/sslalias
//     *
//     * @param action
//     * @param atmIp
//     * @param alias
//     * @return
//     */
//    @RequestMapping(value = "/recv/atm/sslalias", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//    @ResponseBody
//    public String onMessageSslAlias(@RequestParam(value = "action") String action,
//                                    @RequestParam(value = "atmIp", required = false, defaultValue = StringUtils.EMPTY) String atmIp,
//                                    @RequestParam(value = "alias", required = false, defaultValue = StringUtils.EMPTY) String alias) {
//        putMDC();
//        try {
//            String sslInformation = this.sslAlias(action, atmIp, alias);
//            return StringUtils.join("ATM Gateway SSL Alias has been ", action, "\r\n", sslInformation);
//        } catch (Exception e) {
//            return StringUtils.join("ATM Gateway SSL Alias ", action, " failed cause \"", e.getMessage(), "\"\r\n");
//        }
//    }

    /**
     * 可以使用如下指令, 將application-gateway-atm-ssl.properties檔從備份的application-gateway-atm-ssl.properties.bak恢復
     * <p>
     * curl -X POST http://localhost:8300/recv/atm/sslrestore
     *
     * @return
     */
    @RequestMapping(value = "/recv/atm/sslrestore", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageSslRestore() {
        putMDC();
        String message;
        LogData logData = new LogData();
        try {
            String sslInformation = this.sslRestore(logData);
            logData.setSubSys(SubSystem.GW);
            logData.setChannel(FEPChannel.ATM);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramFlowType(ProgramFlow.ATMGatewayOut);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageSslRestore"));
            if (StringUtils.isBlank(sslInformation)) {
                message = "Empty SSL Certificate List!!!\r\n";
                logData.setRemark(message);
                logMessage(Level.WARN, logData);
            } else {
                message = StringUtils.join("ATM Gateway SSL Certificate List\r\n", sslInformation);
                logData.setRemark(message);
                logMessage(logData);
            }
        } catch (Exception e) {
            message = StringUtils.join("ATM Gateway SSL Certificate restore failed cause \"", e.getMessage(), "\"\r\n");
            logData.setRemark(message);
            logMessage(Level.WARN, logData);
        }
        return message;
    }

    /**
     * 獲取GW的Monitor資料
     * <p>
     * curl -d "action=get&listClient=true" -X POST http://localhost:8300/recv/atm/monitor
     *
     * @param action
     * @return
     */
    @RequestMapping(value = "/recv/atm/monitor", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageMonitor(@RequestParam(value = "action", required = false, defaultValue = StringUtils.EMPTY) String action,
                                   @RequestParam(value = "listClient", required = false, defaultValue = "true") String listClient) {
        putMDC();
        switch (action) {
            case "get":
                String monitorData = this.getMonitorData(Boolean.parseBoolean(listClient));
                return monitorData;
            default:
                return this.onMessageMonitor("get", Boolean.TRUE.toString());
        }
    }

    /**
     * 獲取ATM Client的列表
     * <p>
     * curl -d "atmStatus=0" -X POST http://localhost:8300/recv/atm/clientlist
     *
     * @param atmStatus
     * @return
     */
    @RequestMapping(value = "/recv/atm/clientlist", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageMonitor(@RequestParam(value = "atmStatus", required = false, defaultValue = StringUtils.EMPTY) String atmStatus) {
        putMDC();
        AtmStatus status = null;
        if (StringUtils.isNotBlank(atmStatus)) {
            if (AtmStatus.validate(atmStatus)) {
                status = AtmStatus.fromValue(Integer.parseInt(atmStatus));
            } else {
                return StringUtils.join("atmStatus must be ", AtmStatus.Connected.getValue(), " or ", AtmStatus.Disconnected.getValue(), " !!!");
            }
        }
        return this.clientList(status, StringUtils.EMPTY);
    }

    /**
     * 指定需要將電文打到那台ATM Service
     * <p>
     * curl -d "host=127.0.0.1" -X POST http://localhost:8300/recv/atm/changefepap
     *
     * @param host
     * @return
     */
    @RequestMapping(value = "/recv/atm/changefepap", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageChangeFEPAP(@RequestParam(value = "host", required = false, defaultValue = StringUtils.EMPTY) String host) {
        putMDC();
        ATMGatewayServerToFEPATM toFEPATM = ATMGatewayServerToFEPATM.getInstance(); // SpringBeanFactoryUtil.getBean(ATMGatewayServerToFEPATM.class);
        return toFEPATM.setToFEPATM(host);
    }

    /**
     * 取得當前將電文打到哪台ATM Service
     * <p>
     * curl -X POST http://localhost:8300/recv/atm/checkfepap
     *
     * @return
     */
    @RequestMapping(value = "/recv/atm/checkfepap", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageCheckFEPAP() {
        putMDC();
        ATMGatewayServerToFEPATM toFEPATM = ATMGatewayServerToFEPATM.getInstance(); // SpringBeanFactoryUtil.getBean(ATMGatewayServerToFEPATM.class);
        return toFEPATM.getToFEPATM();
    }
}
