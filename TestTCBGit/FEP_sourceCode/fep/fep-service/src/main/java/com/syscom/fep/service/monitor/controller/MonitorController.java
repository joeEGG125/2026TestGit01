package com.syscom.fep.service.monitor.controller;

import com.google.gson.Gson;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.SmsExtMapper;
import com.syscom.fep.mybatis.mapper.SmsMapper;
import com.syscom.fep.mybatis.model.Sms;
import com.syscom.fep.service.monitor.job.MonitorSchedulerJobConfig;
import com.syscom.fep.service.monitor.svr.MonitorChecker;
import com.syscom.fep.service.monitor.svr.MonitorSchedulerService;
import com.syscom.fep.service.monitor.vo.MonitorServerInfo;
import com.syscom.fep.vo.monitor.MonitorConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Calendar;
import java.util.List;

public class MonitorController implements MonitorConstant {
    private final LogHelper logger = LogHelperFactory.getServiceLogger();
    @Autowired
    private SmsExtMapper smsExtMapper;
    @Autowired
    private MonitorChecker checker;

    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_APPMON);
    }

    @RequestMapping(value = "/api/mon/SendMessage")
    @ResponseBody
    public String sendMessage(Sms sms) {
        checker.monitorDataArrived();
        return timeOccupied((args) -> {
            putMDC();
            if (sms != null) {
                Gson gson = new Gson();
                logger.info("[MonitorController]Receive App Monitor Data = [", gson.toJson(sms), "]");
                String smsServiceName = sms.getSmsServicename().toUpperCase();
                String smsServiceip = sms.getSmsServiceip();
                String smsHostname = sms.getSmsHostname();
                if (StringUtils.isBlank(smsServiceName) || StringUtils.isBlank(smsServiceip)) {
                    logger.error("[MonitorController]監控資料 skip, name or ip was empty, smsServiceName = [", smsServiceName, "], smsServiceip = [", smsServiceip, "]");
                    return REPLY_COMPLETE;
                }
                Sms smsServices = null;
                try {
                    smsServices = smsExtMapper.selectByPrimaryKey(smsServiceName, smsServiceip);
                    if (smsServices == null) {
                        smsServices = createSms(smsServiceName, smsServiceip, smsHostname);
                        smsExtMapper.insert(smsServices);
                    }
                    // 視為停止
                    if (sms.getSmsStarttime() == null || "0".equals(sms.getSmsServicestate())) {
                        if (sms.getSmsStoptime() != null) {
                            smsServices.setSmsStoptime(sms.getSmsStoptime());
                        } else {
                            smsServices.setSmsStoptime(Calendar.getInstance().getTime());
                        }
                        smsServices.setSmsStarttime(null);
                        smsServices.setSmsServicestate("0");
                    } else {
                        if (sms.getSmsStarttime() != null) {
                            smsServices.setSmsStarttime(sms.getSmsStarttime());
                        } else {
                            smsServices.setSmsStarttime(Calendar.getInstance().getTime());
                        }
                        smsServices.setSmsStoptime(null);
                        smsServices.setSmsServicestate("1");
                        // 2025-03-10 Richard add 如果服務活著, 這裡直接就remove掉alertKey
                        this.removeAlertKey(smsServices);
                    }
                    smsServices.setSmsHostname(smsHostname);
                    smsServices.setSmsRam(sms.getSmsRam());
                    smsServices.setSmsCpu(sms.getSmsCpu());
                    smsServices.setSmsThreads(sms.getSmsThreads());
                    smsServices.setSmsUpdatetime(Calendar.getInstance().getTime());
                    smsServices.setSmsPid(sms.getSmsPid());
                    smsServices.setSmsOthers(sms.getSmsOthers());
                    smsExtMapper.updateByPrimaryKeyWithBLOBs(smsServices);
                } catch (Exception e) {
                    logger.exceptionMsg(e, e.getMessage());
                    return REPLY_EXCEPTION_OCCUR;
                } finally {
                    MonitorServerInfo foundMonitorServerInfo = null;
                    // config中有, 則更新hostname
                    MonitorSchedulerJobConfig monitorSchedulerJobConfig = SpringBeanFactoryUtil.getBean(MonitorSchedulerJobConfig.class);
                    List<MonitorServerInfo> serviceList = monitorSchedulerJobConfig.getServices();
                    if (CollectionUtils.isNotEmpty(serviceList)) {
                        MonitorServerInfo[] services = new MonitorServerInfo[serviceList.size()];
                        monitorSchedulerJobConfig.getServices().toArray(services);
                        for (MonitorServerInfo service : services) {
                            if (service.getHostip().equalsIgnoreCase(smsServiceip) && service.getName().equalsIgnoreCase(smsServiceName)) {
                                service.setHostname(smsHostname);
                                service.setUpdateTime(Calendar.getInstance().getTime());
                                foundMonitorServerInfo = service;
                                break;
                            }
                        }
                    }
                    // config中沒有, 則增加到config中
                    if (foundMonitorServerInfo == null) {
                        MonitorServerInfo monitorServerInfo = new MonitorServerInfo();
                        monitorServerInfo.setName(smsServiceName);
                        monitorServerInfo.setHostip(smsServiceip);
                        monitorServerInfo.setHostname(smsHostname);
                        monitorServerInfo.setUpdateTime(Calendar.getInstance().getTime());
                        serviceList.add(monitorServerInfo);
                        logger.warn("[MonitorController][Add New MonitorServerInfo]", gson.toJson(monitorServerInfo));
                    } else {
                        // 更新threshold相關值
                        if (foundMonitorServerInfo.getThreshold() != null && foundMonitorServerInfo.getThreshold().isNotification()) {
                            Sms record = new Sms();
                            record.setSmsServiceip(smsServiceip);
                            record.setSmsServicename(smsServiceName);
                            record.setSmsCpuThreshold(foundMonitorServerInfo.getThreshold().getCpuThreshold());
                            record.setSmsRamThreshold(foundMonitorServerInfo.getThreshold().getRamThreshold());
                            record.setSmsThreadsThreshold(foundMonitorServerInfo.getThreshold().getThreadThreshold());
                            smsExtMapper.updateThresholdByPrimaryKey(record);
                        }
                    }
                }
                return REPLY_SUCCESS;
            } else {
                logger.warn("[MonitorController]監控資料 EMPTY");
                return REPLY_COMPLETE;
            }
        }, REPLY_EXCEPTION_OCCUR, StringUtils.EMPTY);
    }


    /*
     * 初始化SMS
     */
    private Sms createSms(String sname, String ip, String hname) {
        Sms sms = new Sms();
        sms.setSmsServicename(sname);
        sms.setSmsServiceip(ip);
        sms.setSmsHostname(hname);
        sms.setSmsUpdatetime(Calendar.getInstance().getTime());
        // 0-停止 1-正常
        sms.setSmsServicestate("0");
        sms.setSmsCpu(0);
        sms.setSmsCpuThreshold(0);
        sms.setSmsRam(0);
        sms.setSmsRamThreshold(0);
        sms.setSmsThreads(0);
        sms.setSmsThreadsActive(0);
        sms.setSmsThreadsThreshold(0);
        sms.setSmsPid(null);
        return sms;
    }

    /**
     * 移除alertKey
     *
     * @param sms
     */
    private void removeAlertKey(Sms sms) {
        MonitorSchedulerService service = SpringBeanFactoryUtil.getBean(MonitorSchedulerService.class, false);
        if (service != null)
            service.removeAlertKeyForService(sms);
    }
}
