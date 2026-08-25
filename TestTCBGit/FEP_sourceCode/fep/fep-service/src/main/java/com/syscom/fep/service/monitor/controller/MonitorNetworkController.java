package com.syscom.fep.service.monitor.controller;

import com.google.gson.reflect.TypeToken;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.parse.GsonParser;
import com.syscom.fep.mybatis.mapper.SmsMapper;
import com.syscom.fep.mybatis.model.Sms;
import com.syscom.fep.service.monitor.svr.MonitorChecker;
import com.syscom.fep.vo.monitor.ClientNetworkStatus;
import com.syscom.fep.vo.monitor.MonitorConstant;
import com.syscom.fep.vo.monitor.ServerNetworkStatus;
import com.syscom.fep.vo.monitor.ServiceStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class MonitorNetworkController implements MonitorConstant {
    private final LogHelper logger = LogHelperFactory.getServiceLogger();
    @Autowired
    private FEPConfig fepConfig;
    @Autowired
    private SmsMapper smsMapper;
    @Autowired
    private MonitorChecker checker;

    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_APPMON);
    }

    @RequestMapping(value = "/recv/net/server", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String sendReceiveNetServer(@RequestBody List<ServerNetworkStatus> serverNetworkStatusList) {
        return this.sendReceiveNetServer(serverNetworkStatusList, true);
    }

    @RequestMapping(value = "/recv/net/client", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String sendReceiveNetClient(@RequestBody List<ClientNetworkStatus> clientNetworkStatusList) {
        return this.sendReceiveNetClient(clientNetworkStatusList, true);
    }

    public String sendReceiveNetServer(@RequestBody List<ServerNetworkStatus> serverNetworkStatusList, boolean monitorDataArrived) {
        return this.sendReceiveNet(SERVICE_NAME_NET_SERVER, serverNetworkStatusList, new GsonParser<>(new TypeToken<List<ServerNetworkStatus>>() {}.getType()), monitorDataArrived);
    }

    public String sendReceiveNetClient(@RequestBody List<ClientNetworkStatus> clientNetworkStatusList, boolean monitorDataArrived) {
        return this.sendReceiveNet(SERVICE_NAME_NET_CLIENT, clientNetworkStatusList, new GsonParser<>(new TypeToken<List<ClientNetworkStatus>>() {}.getType()), monitorDataArrived);
    }

    @SuppressWarnings({"unchecked"})
    private synchronized <T extends List<E>, E extends ServiceStatus> String sendReceiveNet(String smsServiceName, T networkStatusList, GsonParser<T> parser, boolean monitorDataArrived) {
        putMDC();
        if (monitorDataArrived)
            checker.monitorDataArrived();
        return timeOccupied((args) -> {
            putMDC();
            try {
                if (CollectionUtils.isNotEmpty(networkStatusList)) {
                    logger.info("[", smsServiceName, "]Receive App Monitor Network Data = ", parser.writeOut(networkStatusList));
                    Sms sms = smsMapper.selectByPrimaryKey(smsServiceName, fepConfig.getHostIp());
                    if (sms == null) {
                        sms = createSms(smsServiceName, fepConfig.getHostIp(), fepConfig.getHostName());
                        if (networkStatusList.stream().filter(t -> "0".equalsIgnoreCase(t.getServiceState())).count() == networkStatusList.size()) {
                            sms.setSmsServicestate("0");
                            sms.setSmsStoptime(Calendar.getInstance().getTime());
                        } else {
                            sms.setSmsServicestate("1");
                            sms.setSmsStarttime(Calendar.getInstance().getTime());
                        }
                        sms.setSmsUpdatetime(Calendar.getInstance().getTime());
                        sms.setSmsOthers(parser.writeOut(networkStatusList));
                        smsMapper.insert(sms);
                    } else {
                        T list = null;
                        // 如果原本DB中存在資料, 則需要將新進來的merge進去
                        if (StringUtils.isNotBlank(sms.getSmsOthers())) {
                            list = parser.readIn(sms.getSmsOthers());
                            if (CollectionUtils.isNotEmpty(list)) {
                                // 這裡要針對特殊的資料做判斷
                                List<String> types = networkStatusList.stream().map(ServiceStatus::getType).distinct().collect(Collectors.toList());
                                for (String type : types) {
                                    if (StringUtils.isBlank(type))
                                        continue;
                                    // suip網絡連線的資料, 有可能會有不同, 所以在merge前, 要先刪除掉原本DB中的資料
                                    if (MONITOR_TYPE_SUIP_NET_CLIENT.equalsIgnoreCase(type)) {
                                        list = this.removedForOverwritten(list, networkStatusList, MONITOR_TYPE_SUIP_NET_CLIENT);
                                    }
                                }
                                List<E> filtered = new ArrayList<>();
                                boolean found;
                                for (E status : list) {
                                    found = false;
                                    for (int i = 0; i < networkStatusList.size(); i++) {
                                        E networkStatus = networkStatusList.get(i);
                                        if (StringUtils.isBlank(networkStatus.getServiceName()) ||
                                                StringUtils.isBlank(networkStatus.getServiceIP())) {
                                            logger.warn("Ignore incorrect received data, ", ReflectionToStringBuilder.toString(networkStatus, ToStringStyle.SHORT_PREFIX_STYLE));
                                            continue;
                                        }
                                        // 將新收進來的覆蓋掉DB中的
                                        if ((StringUtils.isBlank(networkStatus.getType()) && StringUtils.isBlank(status.getType())
                                                || networkStatus.getType().equalsIgnoreCase(status.getType()))
                                                && networkStatus.getServiceName().equalsIgnoreCase(status.getServiceName())
                                                && networkStatus.getServiceIP().equalsIgnoreCase(status.getServiceIP())) {
                                            filtered.add(networkStatusList.remove(i)); // 注意這裡要移除掉, 避免後面重複增加
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (found) continue; // 如果已經覆蓋掉, 則繼續下一筆
                                    filtered.add(status); // 如果沒有, 則保留DB中的
                                }
                                list.clear();
                                list.addAll(filtered);
                                // 注意上面可能還有未過濾掉的新進來的資料, 要增加進去
                                if (CollectionUtils.isNotEmpty(networkStatusList)) {
                                    list.addAll(networkStatusList);
                                }
                            }
                        }
                        if (CollectionUtils.isEmpty(list)) {
                            list = (T) new ArrayList<E>(networkStatusList);
                        }
                        if (list != null) {
                            list = (T) list.stream().filter(t -> t != null).collect(Collectors.toList());
                            if (list.stream().filter(t -> "0".equalsIgnoreCase(t.getServiceState())).count() == list.size()) {
                                sms.setSmsServicestate("0");
                            } else {
                                sms.setSmsServicestate("1");
                            }
                        }
                        sms.setSmsUpdatetime(Calendar.getInstance().getTime());
                        sms.setSmsOthers(parser.writeOut(list));
                        smsMapper.updateByPrimaryKeyWithBLOBs(sms);
                    }
                    return REPLY_SUCCESS;
                } else {
                    logger.warn("監控資料 EMPTY");
                    return REPLY_COMPLETE;
                }
            } catch (Exception e) {
                logger.exceptionMsg(e, e.getMessage());
                return REPLY_EXCEPTION_OCCUR;
            }
        }, REPLY_EXCEPTION_OCCUR, StringUtils.EMPTY);
    }

    /*
     * 初始化SMS
     */
    private Sms createSms(String smsServicename, String smsServiceip, String smsHostname) {
        Sms sms = new Sms();
        sms.setSmsServicename(smsServicename);
        sms.setSmsServiceip(smsServiceip);
        sms.setSmsHostname(smsHostname);
        sms.setSmsUpdatetime(Calendar.getInstance().getTime());
        sms.setSmsServicestate("0");
        sms.setSmsCpu(0);
        sms.setSmsCpuThreshold(0);
        sms.setSmsRam(0);
        sms.setSmsRamThreshold(0);
        sms.setSmsThreads(0);
        sms.setSmsThreadsActive(0);
        sms.setSmsThreadsThreshold(0);
        return sms;
    }

    /**
     * 有些資料是需要每次進行overwrite的, 故要先將db中對應monitorType的資料移除掉, 以方便後面進行覆蓋
     *
     * @param list
     * @param networkStatusList
     * @param monitorType
     * @param <T>
     * @param <E>
     */
    @SuppressWarnings({"unchecked"})
    private <T extends List<E>, E extends ServiceStatus> T removedForOverwritten(T list, T networkStatusList, String monitorType) {
        if (CollectionUtils.isEmpty(networkStatusList)) return list;
        // 這裡要針對suip的資料做特殊的判斷
        if (networkStatusList.stream().anyMatch(t -> monitorType.equals(t.getType()))) {
            return (T) list.stream().filter(t -> StringUtils.isBlank(t.getType()) || !monitorType.equals(t.getType())).collect(Collectors.toList());
        }
        return list;
    }
}
