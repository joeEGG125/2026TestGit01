package com.syscom.fep.gateway.util;

import com.syscom.fep.base.FEPBase;

// @Component
public class GatewayCommuHelper extends FEPBase {
    // @Autowired
    // private FEPInvoker invoker;

    // public String getSysconfValueFromFEPATM(LogData logData, Short sysconfSubsysno, String sysconfName, int timeout) {
    //     return this.getSysconfValue(logData, FEPChannel.ATM, sysconfSubsysno, sysconfName, timeout, (r, t) -> invoker.sendReceiveToFEPATM(r, t));
    // }

    // public String getSysconfValueFromFEPFISC(LogData logData, Short sysconfSubsysno, String sysconfName, int timeout) {
    //     return this.getSysconfValue(logData, FEPChannel.FISC, sysconfSubsysno, sysconfName, timeout, (r, t) -> invoker.sendReceiveToFEPFISC(r, t));
    // }

    // public ToGWCommuConfig getConfigFromFEPATM(LogData logData, int configType, int timeout) {
    //     return this.getConfig(logData, FEPChannel.ATM, configType, timeout, (r, t) -> invoker.sendReceiveToFEPATM(r, t));
    // }

    // public int getEjfnoFromFEPATM(LogData logData, int timeout) {
    //     return getEjfno(logData, FEPChannel.ATM, timeout, (r, t) -> invoker.sendReceiveToFEPATM(r, t));
    // }

    // public int getEjfnoFromFEPFISC(LogData logData, int timeout) {
    //     return getEjfno(logData, FEPChannel.FISC, timeout, (r, t) -> invoker.sendReceiveToFEPFISC(r, t));
    // }

    // public ToATMCommuAtmmstr getAtmmstrByAtmIp(LogData logData, String atmIp, int timeout) {
    //     ToFEPATMCommuAtmmstr request = new ToFEPATMCommuAtmmstr();
    //     request.setAtmIp(atmIp);
    //     return this.getAtmmstr(logData, request, timeout);
    // }

    // public ToATMCommuAtmmstr getAtmmstrByAtmNo(LogData logData, String atmNo, int timeout) {
    //     ToFEPATMCommuAtmmstr request = new ToFEPATMCommuAtmmstr();
    //     request.setAtmNo(atmNo);
    //     return this.getAtmmstr(logData, request, timeout);
    // }

    // public int updateAtmmstr(LogData logData, ToFEPATMCommuUpdateAtmmstr request, int timeout) {
    //     try {
    //         logData.setSubSys(SubSystem.GW);
    //         logData.setChannel(FEPChannel.ATM);
    //         logData.setMessageFlowType(MessageFlow.Request);
    //         logData.setProgramFlowType(ProgramFlow.ATMGatewayOut);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".updateAtmmstr"));
    //         logData.setMessage(request.toString());
    //         logData.setRemark("ATM Gateway Send Data to ATM Service");
    //         this.logMessage(logData);
    //         String response = invoker.sendReceiveToFEPATM(request, timeout);
    //         logData.setSubSys(SubSystem.GW);
    //         logData.setChannel(FEPChannel.ATM);
    //         logData.setMessageFlowType(MessageFlow.Response);
    //         logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".updateAtmmstr"));
    //         logData.setMessage(response);
    //         logData.setRemark("ATM Gateway Receive Data from ATM Service");
    //         this.logMessage(logData);
    //         ToGWCommuDbOptResult toGWCommuDbOptResult = ToGWCommuDbOptResult.fromXML(response);
    //         if (toGWCommuDbOptResult.getCode() == FEPReturnCode.Normal) {
    //             return toGWCommuDbOptResult.getResult();
    //         } else {
    //             throw ExceptionUtil.createException(toGWCommuDbOptResult.getErrmsg());
    //         }
    //     } catch (Exception e) {
    //         logData.setProgramException(e);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".updateAtmmstr"));
    //         sendEMS(logData);
    //     }
    //     return -1;
    // }

    // public int updateAtmstat(LogData logData, ToFEPATMCommuUpdateAtmstat request, int timeout) {
    //     try {
    //         logData.setSubSys(SubSystem.GW);
    //         logData.setChannel(FEPChannel.ATM);
    //         logData.setMessageFlowType(MessageFlow.Request);
    //         logData.setProgramFlowType(ProgramFlow.ATMGatewayOut);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".updateAtmstat"));
    //         logData.setMessage(request.toString());
    //         logData.setRemark("ATM Gateway Send Data to ATM Service");
    //         this.logMessage(logData);
    //         String response = invoker.sendReceiveToFEPATM(request, timeout);
    //         logData.setSubSys(SubSystem.GW);
    //         logData.setChannel(FEPChannel.ATM);
    //         logData.setMessageFlowType(MessageFlow.Response);
    //         logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".updateAtmstat"));
    //         logData.setMessage(response);
    //         logData.setRemark("ATM Gateway Receive Data from ATM Service");
    //         this.logMessage(logData);
    //         ToGWCommuDbOptResult toGWCommuDbOptResult = ToGWCommuDbOptResult.fromXML(response);
    //         if (toGWCommuDbOptResult.getCode() == FEPReturnCode.Normal) {
    //             return toGWCommuDbOptResult.getResult();
    //         } else {
    //             throw ExceptionUtil.createException(toGWCommuDbOptResult.getErrmsg());
    //         }
    //     } catch (Exception e) {
    //         logData.setProgramException(e);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".updateAtmmstr"));
    //         sendEMS(logData);
    //     }
    //     return -1;
    // }

    // public ToATMCommuAtmstatList getAtmstatList(LogData logData, AtmStatus atmStatus) throws Exception {
    //     try {
    //         ToFEPATMCommuAtmstatList request = new ToFEPATMCommuAtmstatList();
    //         if (atmStatus != null) {
    //             request.setAtmstatStatus(atmStatus.getValue());
    //         }
    //         logData.setSubSys(SubSystem.GW);
    //         logData.setChannel(FEPChannel.ATM);
    //         logData.setMessageFlowType(MessageFlow.Request);
    //         logData.setProgramFlowType(ProgramFlow.ATMGatewayOut);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getAtmstatList"));
    //         logData.setMessage(request.toString());
    //         logData.setRemark("ATM Gateway Send Data to ATM Service");
    //         this.logMessage(logData);
    //         String response = invoker.sendReceiveToFEPATM(request, 120000);
    //         logData.setSubSys(SubSystem.GW);
    //         logData.setChannel(FEPChannel.ATM);
    //         logData.setMessageFlowType(MessageFlow.Response);
    //         logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getAtmstatList"));
    //         logData.setMessage(response);
    //         logData.setRemark("ATM Gateway Receive Data from ATM Service");
    //         this.logMessage(logData);
    //         ToATMCommuAtmstatList toATMCommuAtmstatList = ToATMCommuAtmstatList.fromXML(response);
    //         if (toATMCommuAtmstatList.getCode() == FEPReturnCode.Normal) {
    //             return toATMCommuAtmstatList;
    //         } else {
    //             throw ExceptionUtil.createException(toATMCommuAtmstatList.getErrmsg());
    //         }
    //     } catch (Exception e) {
    //         logData.setProgramException(e);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getAtmstatList"));
    //         sendEMS(logData);
    //         throw e;
    //     }
    // }

    // public ToGWCommuZone getZoneByZoneCodeFromFEPATM(LogData logData, String zoneCode, int timeout) {
    //     try {
    //         ToFEPCommuZone request = new ToFEPCommuZone();
    //         request.setZoneCode(zoneCode);
    //         logData.setChannel(FEPChannel.ATM);
    //         logData.setMessageFlowType(MessageFlow.Request);
    //         logData.setProgramFlowType(ProgramFlow.ATMGatewayOut);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getZoneByZoneCodeFromFEPATM"));
    //         logData.setMessage(request.toString());
    //         logData.setRemark("ATM Gateway Send Data to ATM Service");
    //         this.logMessage(logData);
    //         String response = invoker.sendReceiveToFEPATM(request, timeout);
    //         logData.setSubSys(SubSystem.GW);
    //         logData.setChannel(FEPChannel.ATM);
    //         logData.setMessageFlowType(MessageFlow.Response);
    //         logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getZoneByZoneCodeFromFEPATM"));
    //         logData.setMessage(response);
    //         logData.setRemark("ATM Gateway Receive Data from ATM Service");
    //         this.logMessage(logData);
    //         ToGWCommuZone toGWCommuZone = ToGWCommuZone.fromXML(response);
    //         if (toGWCommuZone.getCode() == FEPReturnCode.Normal) {
    //             return toGWCommuZone;
    //         } else {
    //             throw ExceptionUtil.createException(toGWCommuZone.getErrmsg());
    //         }
    //     } catch (Exception e) {
    //         logData.setProgramException(e);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getZoneByZoneCodeFromFEPATM"));
    //         sendEMS(logData);
    //     }
    //     return null;
    // }

    // private String getSysconfValue(LogData logData, FEPChannel channel, Short sysconfSubsysno, String sysconfName, int timeout, GatewayDbAction action) {
    //     try {
    //         ToFEPCommuSysconf request = new ToFEPCommuSysconf();
    //         request.setSysconfSubsysno(sysconfSubsysno);
    //         request.setSysconfName(sysconfName);
    //         logData.setSubSys(SubSystem.GW);
    //         logData.setMessageFlowType(MessageFlow.Request);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getSysconfValue"));
    //         logData.setMessage(request.toString());
    //         logData.setRemark(StringUtils.join(channel, " Gateway Send Data to ", channel, " Service"));
    //         this.logMessage(logData);
    //         String response = action.doAction(request, timeout);
    //         logData.setSubSys(SubSystem.GW);
    //         logData.setMessageFlowType(MessageFlow.Response);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getSysconfValue"));
    //         logData.setMessage(response);
    //         logData.setRemark(StringUtils.join(channel, " Gateway Receive Data from ", channel, " Service"));
    //         this.logMessage(logData);
    //         ToGWCommuSysconf toGWCommuSysconf = ToGWCommuSysconf.fromXML(response);
    //         if (toGWCommuSysconf.getCode() == FEPReturnCode.Normal) {
    //             return toGWCommuSysconf.getSysconfValue();
    //         } else {
    //             throw ExceptionUtil.createException(toGWCommuSysconf.getErrmsg());
    //         }
    //     } catch (Exception e) {
    //         logData.setProgramException(e);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getSysconfValue"));
    //         sendEMS(logData);
    //     }
    //     return null;
    // }

    // private ToGWCommuConfig getConfig(LogData logData, FEPChannel channel, int configType, int timeout, GatewayDbAction action) {
    //     try {
    //         ToFEPCommuConfig request = new ToFEPCommuConfig();
    //         request.setConfigType(configType);
    //         logData.setSubSys(SubSystem.GW);
    //         logData.setMessageFlowType(MessageFlow.Request);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getConfig"));
    //         logData.setMessage(request.toString());
    //         logData.setRemark(StringUtils.join(channel, " Gateway Send Data to ", channel, " Service"));
    //         this.logMessage(logData);
    //         String response = action.doAction(request, timeout);
    //         logData.setSubSys(SubSystem.GW);
    //         logData.setMessageFlowType(MessageFlow.Response);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getConfig"));
    //         logData.setMessage(response);
    //         logData.setRemark(StringUtils.join(channel, " Gateway Receive Data from ", channel, " Service"));
    //         this.logMessage(logData);
    //         ToGWCommuConfig toGWCommuConfig = ToGWCommuConfig.fromXML(response);
    //         if (toGWCommuConfig.getCode() == FEPReturnCode.Normal) {
    //             return toGWCommuConfig;
    //         } else {
    //             throw ExceptionUtil.createException(toGWCommuConfig.getErrmsg());
    //         }
    //     } catch (Exception e) {
    //         logData.setProgramException(e);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getConfig"));
    //         FEPBase.sendEMS(logData);
    //     }
    //     return null;
    // }

    // private ToATMCommuAtmmstr getAtmmstr(LogData logData, ToFEPATMCommuAtmmstr request, int timeout) {
    //     try {
    //         logData.setSubSys(SubSystem.GW);
    //         logData.setChannel(FEPChannel.ATM);
    //         logData.setMessageFlowType(MessageFlow.Request);
    //         logData.setProgramFlowType(ProgramFlow.ATMGatewayOut);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getAtmmstr"));
    //         logData.setMessage(request.toString());
    //         logData.setRemark("ATM Gateway Send Data to ATM Service");
    //         this.logMessage(logData);
    //         String response = invoker.sendReceiveToFEPATM(request, timeout);
    //         logData.setSubSys(SubSystem.GW);
    //         logData.setChannel(FEPChannel.ATM);
    //         logData.setMessageFlowType(MessageFlow.Response);
    //         logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getAtmmstr"));
    //         logData.setMessage(response);
    //         logData.setRemark("ATM Gateway Receive Data from ATM Service");
    //         this.logMessage(logData);
    //         ToATMCommuAtmmstr toATMCommuAtmmstr = ToATMCommuAtmmstr.fromXML(response);
    //         if (toATMCommuAtmmstr.getCode() == FEPReturnCode.Normal) {
    //             return toATMCommuAtmmstr;
    //         } else {
    //             throw ExceptionUtil.createException(toATMCommuAtmmstr.getErrmsg());
    //         }
    //     } catch (Exception e) {
    //         logData.setProgramException(e);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getAtmmstr"));
    //         FEPBase.sendEMS(logData);
    //     }
    //     return null;
    // }

    // private int getEjfno(LogData logData, FEPChannel channel, int timeout, GatewayDbAction action) {
    //     try {
    //         ToFEPCommuAction request = new ToFEPCommuAction();
    //         request.setAction(ToFEPCommuAction.ActionType.GENERATE_EJFNO);
    //         logData.setSubSys(SubSystem.GW);
    //         logData.setMessageFlowType(MessageFlow.Request);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getEjfno"));
    //         logData.setMessage(request.toString());
    //         logData.setRemark(StringUtils.join(channel, " Gateway Send Data to ", channel, " Service"));
    //         this.logMessage(logData);
    //         String response = action.doAction(request, timeout);
    //         logData.setSubSys(SubSystem.GW);
    //         logData.setMessageFlowType(MessageFlow.Response);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getEjfno"));
    //         logData.setMessage(response);
    //         logData.setRemark(StringUtils.join(channel, " Gateway Receive Data from ", channel, " Service"));
    //         this.logMessage(logData);
    //         ToGWCommuAction toGWCommuAction = ToGWCommuAction.fromXML(response);
    //         if (toGWCommuAction.getCode() == FEPReturnCode.Normal) {
    //             return toGWCommuAction.getEjfno();
    //         } else {
    //             throw ExceptionUtil.createException(toGWCommuAction.getErrmsg());
    //         }
    //     } catch (Exception e) {
    //         logData.setProgramException(e);
    //         logData.setProgramName(StringUtils.join(ProgramName, ".getEjfno"));
    //         FEPBase.sendEMS(logData);
    //     }
    //     return 0;
    // }

    // private interface GatewayDbAction {
    //     String doAction(BaseCommu baseCommu, int timeout) throws Exception;
    // }
}
