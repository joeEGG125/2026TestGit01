package com.syscom.fep.service.monitor.svr;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.common.http.HttpClientConfigureConstant;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.mail.MailSender;
import com.syscom.fep.common.monitor.MonitorDataCollector;
import com.syscom.fep.common.monitor.MonitorDataDisk;
import com.syscom.fep.common.sms.mitake.MitakeSmsOperator;
import com.syscom.fep.frmcommon.io.StreamGobbler;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.mail.MailData;
import com.syscom.fep.frmcommon.mail.MailPriority;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClient2;
import com.syscom.fep.frmcommon.os.OperationSystemDataCollector;
import com.syscom.fep.frmcommon.os.ps.Ps;
import com.syscom.fep.frmcommon.parse.GsonDateParser;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.deslog.configuration.DataSourceDeslogConstant;
import com.syscom.fep.mybatis.ems.configuration.DataSourceEmsConstant;
import com.syscom.fep.mybatis.enc.configuration.DataSourceEncConstant;
import com.syscom.fep.mybatis.ext.mapper.SmsExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.his.configuration.DataSourceHisConstant;
import com.syscom.fep.mybatis.model.Sms;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.service.monitor.controller.MonitorNetworkController;
import com.syscom.fep.service.monitor.job.MonitorSchedulerJobConfig;
import com.syscom.fep.service.monitor.vo.*;
import com.syscom.fep.vo.monitor.*;

import jakarta.annotation.PostConstruct;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import oshi.software.os.OSProcess;
import oshi.util.Util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MonitorSchedulerService extends FEPBase implements MonitorConstant {
    private final LogHelper TRACELogger = LogHelperFactory.getTraceLogger();
    @Autowired
    private FEPConfig fepConfig;
    @Autowired
    private SmsExtMapper smsExtMapper;
    @Autowired
    private SysconfExtMapper sysconfExtMapper;
    @Autowired
    private MonitorSchedulerJobConfig monitorSchedulerJobConfig;
    // 2025-09-03 Richard modified 不透過fep-notify送mail, FEP自己送mail
    // @Autowired
    // private NotifyHelper notifyHelper;
    private String appName, hostIp, hostName, url;
    private double ruleRiskRate;
    private final List<Long> notifyIntervalList = new ArrayList<>();
    private final Map<String, List<Long>> alertMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> exceedThresholdTimesMap = new ConcurrentHashMap<>();
    private final Map<String, Date> processNameToStopTimeMap = new ConcurrentHashMap<>();
    private final SuipHsmMonitorData suipHsmMonitorData = new SuipHsmMonitorData();
    private final List<String> systemFileIncludeMountList = new ArrayList<>();
    private final Map<String, Integer> processNameToPidMap = new ConcurrentHashMap<>();  // Process名到PID的映射
    @Autowired
    @Qualifier(HttpClientConfigureConstant.BEAN_NAME_MONITOR)
    private HttpClient2 httpClient2;
    private String latestSystemHardDisk; // 上一次獲取的磁盤信息
    private long latestFetchSystemHardDisk; // 上一次獲取磁盤信息的時間

    @PostConstruct
    public void initialization() {
        TRACELogger.info(ProgramName, " initialization begin");
        appName = fepConfig.getApplicationName();
        hostIp = this.monitorSchedulerJobConfig.getSystem().getHostip();
        hostName = this.monitorSchedulerJobConfig.getSystem().getHostname();
        String protocol = this.monitorSchedulerJobConfig.getSystem().getProtocol();
        String port = this.monitorSchedulerJobConfig.getSystem().getPort();
        String contextPath = this.monitorSchedulerJobConfig.getSystem().getContextPath();
        url = StringUtils.join(protocol, "://", hostIp, ":", port, contextPath);
        ruleRiskRate = this.monitorSchedulerJobConfig.getRuleDiskRate();
        this.parseNotifyInterval();
        this.monitorSchedulerJobConfig.getSystemFileIncludeMountList().stream().filter(StringUtils::isNotBlank).forEach(systemFileIncludeMountList::add);
        TRACELogger.info("systemFileIncludeMountList=", this.systemFileIncludeMountList);
        TRACELogger.info(ProgramName, " initialization finish");
    }

    public void reloadMonitor() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_APPMON);
        this.logContext.clear();
        try {
            TRACELogger.info(ProgramName, " reloadMonitor begin");
            // 初始化
            init();
            TRACELogger.info(ProgramName, " init finish");
            // 系統信息
            fetchSystemInfo();
            TRACELogger.info(ProgramName, " fetchSystemInfo finish");
            // DB信息
            fetchDbInfo();
            TRACELogger.info(ProgramName, " fetchDbInfo finish");
            // MQ信息
            fetchMqInfo();
            TRACELogger.info(ProgramName, " fetchMqInfo finish");
            // Process信息
            fetchProcess();
            TRACELogger.info(ProgramName, " fetchProcess finish");
            // 服務信息
            fetchServicesInfo();
            TRACELogger.info(ProgramName, " fetchServicesInfo finish");
            // suip的網絡狀態
            fetchLocalSuipHsmMonitorData();
            TRACELogger.info(ProgramName, " fetchLocalSuipConnection finish");
        } catch (Throwable t) {
            TRACELogger.error(t, ProgramName, " reloadMonitor with Exception occur, ", t.getMessage());
        } finally {
            TRACELogger.info(ProgramName, " reloadMonitor end");
        }
    }

    /*
     * 初始化
     */
    private void init() {
        // 初始化系統信息
        Sms systemSms = smsExtMapper.selectByPrimaryKey(SERVICE_NAME_SYSTEM, hostIp);
        if (systemSms == null) {
            systemSms = createSms(SERVICE_NAME_SYSTEM, hostIp, hostName, this.monitorSchedulerJobConfig.getSystem().getThreshold());
            smsExtMapper.insert(systemSms);
        } else {
            if ("2".equals(systemSms.getSmsServicestate())) {
                systemSms.setSmsServicestate("0");
                systemSms.setSmsUpdatetime(Calendar.getInstance().getTime());
                smsExtMapper.updateByPrimaryKeySelective(systemSms);
            }
            // 更新threshold相關值
            if (this.monitorSchedulerJobConfig.getSystem().getThreshold() != null && this.monitorSchedulerJobConfig.getSystem().getThreshold().isNotification()) {
                Sms record = new Sms();
                record.setSmsServiceip(hostIp);
                record.setSmsServicename(SERVICE_NAME_SYSTEM);
                record.setSmsCpuThreshold(this.monitorSchedulerJobConfig.getSystem().getThreshold().getCpuThreshold());
                record.setSmsRamThreshold(this.monitorSchedulerJobConfig.getSystem().getThreshold().getRamThreshold());
                record.setSmsThreadsThreshold(this.monitorSchedulerJobConfig.getSystem().getThreshold().getThreadThreshold());
                smsExtMapper.updateThresholdByPrimaryKey(record);
            }
        }
        // 初始化DB信息
        Sms dbSms = smsExtMapper.selectByPrimaryKey(SERVICE_NAME_DB, hostIp);
        if (dbSms == null) {
            dbSms = createSms(SERVICE_NAME_DB, hostIp, hostName, null);
            smsExtMapper.insert(dbSms);
        } else {
            if ("2".equals(dbSms.getSmsServicestate())) {
                dbSms.setSmsServicestate("0");
                dbSms.setSmsUpdatetime(Calendar.getInstance().getTime());
                smsExtMapper.updateByPrimaryKeySelective(dbSms);
            }
        }
        MonitorServerInfo[] services = new MonitorServerInfo[this.monitorSchedulerJobConfig.getServices().size()];
        this.monitorSchedulerJobConfig.getServices().toArray(services);
        // 初始化服務信息
        for (MonitorServerInfo serverInfo : services) {
            Sms service = smsExtMapper.selectByPrimaryKey(serverInfo.getName(), serverInfo.getHostip());
            if (service == null) {
                if (StringUtils.isBlank(serverInfo.getHostname()))
                    serverInfo.setHostname(StringUtils.SPACE);
                service = createSms(serverInfo.getName(), serverInfo.getHostip(), serverInfo.getHostname(), serverInfo.getThreshold());
                smsExtMapper.insert(service);
            } else {
                if ("2".equals(service.getSmsServicestate())) {
                    if (StringUtils.isNotBlank(serverInfo.getHostname()))
                        service.setSmsHostname(serverInfo.getHostname());
                    service.setSmsServicestate("0");
                    service.setSmsUpdatetime(Calendar.getInstance().getTime());
                    smsExtMapper.updateByPrimaryKeySelective(service);
                }
                // 更新threshold相關值
                if (serverInfo.getThreshold() != null && serverInfo.getThreshold().isNotification()) {
                    Sms record = new Sms();
                    record.setSmsServiceip(service.getSmsServiceip());
                    record.setSmsServicename(service.getSmsServicename());
                    record.setSmsCpuThreshold(serverInfo.getThreshold().getCpuThreshold());
                    record.setSmsRamThreshold(serverInfo.getThreshold().getRamThreshold());
                    record.setSmsThreadsThreshold(serverInfo.getThreshold().getThreadThreshold());
                    smsExtMapper.updateThresholdByPrimaryKey(record);
                }
            }
        }
        // 初始化MQ訊息
        Sms mqservice = smsExtMapper.selectByPrimaryKey(SERVICE_NAME_MQ, hostIp);
        if (mqservice == null) {
            List<IBMMQStatus> ibmmqStatusList = getIBMMQStatusList();
            if (CollectionUtils.isNotEmpty(ibmmqStatusList)) {
                mqservice = createSms(SERVICE_NAME_MQ, hostIp, hostName, null);
                mqservice.setSmsOthers(new Gson().toJson(ibmmqStatusList));
                smsExtMapper.insert(mqservice);
            }
        }
        //是否停止提醒 true -停止提醒 / false -提醒
        Sysconf sysconf = sysconfExtMapper.selectByPrimaryKey(SYSCONF_VALUE_CMN, SYSCONF_NAME_STOPNOTIFICATION);
        if (sysconf != null) {
            this.monitorSchedulerJobConfig.setStopNotification(!Boolean.getBoolean("spring.fep.appmon.test") && Boolean.parseBoolean(sysconf.getSysconfValue()));
        }
        // 是否啟用自動重啟
        sysconf = sysconfExtMapper.selectByPrimaryKey(SYSCONF_VALUE_CMN, SYSCONF_NAME_ENABLEAUTORESTART);
        if (sysconf != null) {
            this.monitorSchedulerJobConfig.setEnableAutoRestart(Boolean.parseBoolean(sysconf.getSysconfValue()));
        }
        // SuipTimeout
        sysconf = sysconfExtMapper.selectByPrimaryKey(SYSCONF_VALUE_CMN, "SuipTimeout");
        if (sysconf != null) {
            try {
                this.monitorSchedulerJobConfig.setSuipTimeout(Integer.parseInt(sysconf.getSysconfValue()));
            } catch (NumberFormatException e) {
                TRACELogger.warn(ProgramName, "parse suipTimeout failed!!!");
            }
        }
    }

    /*
     * 系統信息
     */
    private void fetchSystemInfo() {
        // 2026-02-13 Richard modified 建立SMS檔資料時, 將Threshold資料寫入Table
        MonitorThreshold monitorThreshold = this.monitorSchedulerJobConfig.getSystem().getThreshold();
        Sms sms = createSms(SERVICE_NAME_SYSTEM, hostIp, hostName, this.monitorSchedulerJobConfig.getSystem().getThreshold());
        sms.setSmsServicestate("1");
        sms.setSmsCpu(fetchSystemCpuUsage());
        sms.setSmsRam(fetchSystemMemoryUsage());
        sms.setSmsOthers(fetchSystemHardDisk());
        sms.setSmsUpdatetime(Calendar.getInstance().getTime());
        try {
            smsExtMapper.updateByPrimaryKeySelective(sms);
        } catch (Throwable e) {
            TRACELogger.warn(ProgramName, " update SMS for System with exception occur, ", e.getMessage());
        }
        // RAM MB
        int value = (sms.getSmsRam() == null ? 0 : sms.getSmsRam()) / 1024;
        int threshold = monitorThreshold == null ? 0 : monitorThreshold.getRamThreshold();
        boolean alert = threshold > 0 && value > threshold;
        TRACELogger.debug("SYSTEM[", hostName, "-", hostIp, "][RamThreshold]value:", value, ",threshold:", threshold, ",threshold>0&&value>threshold:", alert);
        this.sendSystemAlert(sms, monitorThreshold, alert, "RamThreshold", StringUtils.join("使用的記憶體為[", FormatUtil.longFormat(value), "]MB, 已超過警示值[", FormatUtil.longFormat(threshold), "]MB"));
        // CpuThreshold
        value = (sms.getSmsCpu() == null ? 0 : sms.getSmsCpu()) / 100;
        threshold = monitorThreshold == null ? 0 : monitorThreshold.getCpuThreshold();
        alert = threshold > 0 && value > threshold;
        TRACELogger.debug("SYSTEM[", hostName, "-", hostIp, "][CpuThreshold]value:", value, ",threshold:", threshold, ",threshold>0&&value>threshold:", alert);
        this.sendSystemAlert(sms, monitorThreshold, alert, "CpuThreshold", StringUtils.join("使用的CPU%為[", value, "]%, 已超過警示值[", threshold, "]%"));
    }

    /**
     * 系統警示
     *
     * @param system
     * @param monitorThreshold
     * @param alert
     * @param alertKeySuffix
     * @param msgSuffix
     */
    private void sendSystemAlert(Sms system, MonitorThreshold monitorThreshold, boolean alert, String alertKeySuffix, String msgSuffix) {
        String alertKey = StringUtils.join(Arrays.asList(system.getSmsServiceip(), system.getSmsServicename(), alertKeySuffix), '@');
        // 是否提醒
        if (this.monitorSchedulerJobConfig.isStopNotification() || monitorThreshold == null || !monitorThreshold.isNotification()) {
            // 2025-03-07 Richard add如果不提醒, 則這裡要重置alert中的資料
            if (this.alertMap.remove(alertKey) != null)
                TRACELogger.debug("[REMOVE_ALERT_KEY]alertKey:", alertKey);
            return;
        }
        if (alert) {
            // 發送提醒
            List<Long> alertTimes = this.alertMap.getOrDefault(alertKey, new ArrayList<>());
            this.alertMap.put(alertKey, alertTimes);
            // 小於設定報警次數才會報警
            if (alertTimes.size() < this.notifyIntervalList.size()) {
                long lastTime = alertTimes.isEmpty() ? 0 : alertTimes.get(alertTimes.size() - 1);
                long nowTime = Calendar.getInstance().getTimeInMillis();
                // 第一次或者大於間隔時間才會報警
                if (lastTime == 0 || nowTime >= lastTime + this.notifyIntervalList.get(alertTimes.size())) {
                    alertTimes.add(nowTime);
                    // String msg = StringUtils.join("FEP在主機[", serverInfo.getHostname(), ":", serverInfo.getHostip(), "]上的服務[", serverInfo.getName(), "]", msgSuffix, "!");
                    // String body = StringUtils.join("服務異常時間:", FormatUtil.dateTimeInMillisFormat(CalendarUtil.clone(alertTimes.get(0)).getTime()), "\n", msg);
                    // String subject = StringUtils.join(APPNAME + "服務異常第", alertTimes.size(), "次通知-", msg);
                    // String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeInMillisFormat(CalendarUtil.clone(nowTime).getTime()));
                    String remark = StringUtils.join(
                            Arrays.asList(NOTIFY_SUBJECT, hostName,
                                    "第" + alertTimes.size() + "次通知",
                                    "主機[" + system.getSmsHostname() + ":" + system.getSmsServiceip() + "]" + msgSuffix + "!"
                            ), "-"
                    );
                    this.sendAlertMail(remark);
                    this.sendSms(remark);
                }
            }
        } else {
            // 2025-03-07 Richard add如果服務有重新恢復異常, 則這裡要重置alert中的資料
            if (this.alertMap.remove(alertKey) != null)
                TRACELogger.debug("[REMOVE_ALERT_KEY]alertKey:", alertKey);
        }
    }

    /*
     * 資料庫狀態---DB
     */
    private void fetchDbInfo() {
        String promQL = "/actuator/health";
        String jsonStr;
        try {
            jsonStr = httpClient2.getForObject(StringUtils.join(url, promQL), String.class);
        } catch (Throwable e) {
            if ("503".equals(e.getMessage().substring(0, 3))) {
                String msg = e.getMessage();
                int index = msg.indexOf("{");
                jsonStr = msg.substring(index, msg.length() - 1);
            } else {
                TRACELogger.warn(ProgramName, " fetchDbInfo with exception occur, ", e.getMessage());
                return;
            }
        }
        JSONObject dbObject = new JSONObject();
        dbObject.put(DB_NAME_FEP, Boolean.FALSE.toString());
        dbObject.put(DB_NAME_EMS, Boolean.FALSE.toString());
        dbObject.put(DB_NAME_ENC, Boolean.FALSE.toString());
        dbObject.put(DB_NAME_ENCLOG, Boolean.FALSE.toString());
        dbObject.put(DB_NAME_FEPHIS, Boolean.FALSE.toString());
        JSONObject rootObject = new JSONObject(jsonStr);
        if (rootObject != null) {
            JSONObject componentsJSONObject = rootObject.getJSONObject(JSON_FIELD_COMPONENTS);
            if (componentsJSONObject != null) {
                JSONObject dbJSONObject = componentsJSONObject.getJSONObject(SERVICE_NAME_DB.toLowerCase());
                if (dbJSONObject != null) {
                    JSONObject compJSONObject = dbJSONObject.getJSONObject(JSON_FIELD_COMPONENTS);
                    if (compJSONObject != null) {
                        // FEPDB
                        if (compJSONObject.has(DataSourceConstant.BEAN_NAME_DATASOURCE)) {
                            JSONObject fepJSONObject = compJSONObject.getJSONObject(DataSourceConstant.BEAN_NAME_DATASOURCE);
                            if (fepJSONObject != null && STATUS_UP.equals(fepJSONObject.getString(JSON_FIELD_STATUS))) {
                                dbObject.put(DB_NAME_FEP, Boolean.TRUE.toString());
                            }
                        }
                        // EMSDB
                        if (compJSONObject.has(DataSourceEmsConstant.BEAN_NAME_DATASOURCE)) {
                            JSONObject emsJSONObject = compJSONObject.getJSONObject(DataSourceEmsConstant.BEAN_NAME_DATASOURCE);
                            if (emsJSONObject != null && STATUS_UP.equals(emsJSONObject.getString(JSON_FIELD_STATUS))) {
                                dbObject.put(DB_NAME_EMS, Boolean.TRUE.toString());
                            }
                        }
                        // ENCDB
                        if (compJSONObject.has(DataSourceEncConstant.BEAN_NAME_DATASOURCE)) {
                            JSONObject desJSONObject = compJSONObject.getJSONObject(DataSourceEncConstant.BEAN_NAME_DATASOURCE);
                            if (desJSONObject != null && STATUS_UP.equals(desJSONObject.getString(JSON_FIELD_STATUS))) {
                                dbObject.put(DB_NAME_ENC, Boolean.TRUE.toString());
                            }
                        }
                        // DESLOGDB
                        if (compJSONObject.has(DataSourceDeslogConstant.BEAN_NAME_DATASOURCE)) {
                            JSONObject emsLogJSONObject = compJSONObject.getJSONObject(DataSourceDeslogConstant.BEAN_NAME_DATASOURCE);
                            if (emsLogJSONObject != null && STATUS_UP.equals(emsLogJSONObject.getString(JSON_FIELD_STATUS))) {
                                dbObject.put(DB_NAME_ENCLOG, Boolean.TRUE.toString());
                            }
                        }
                        // FEPHIS
                        if (compJSONObject.has(DataSourceHisConstant.BEAN_NAME_DATASOURCE)) {
                            JSONObject emsLogJSONObject = compJSONObject.getJSONObject(DataSourceHisConstant.BEAN_NAME_DATASOURCE);
                            if (emsLogJSONObject != null && STATUS_UP.equals(emsLogJSONObject.getString(JSON_FIELD_STATUS))) {
                                dbObject.put(DB_NAME_FEPHIS, Boolean.TRUE.toString());
                            }
                        }
                    }
                }
            }
        }
        // 是否觸發提醒FEPDB
        this.sendAlertForDB(dbObject, DB_NAME_FEP);
        // 否觸發提醒EMSDB
        this.sendAlertForDB(dbObject, DB_NAME_EMS);
        //是否觸發提醒ENCDB
        this.sendAlertForDB(dbObject, DB_NAME_ENC);
        //是否觸發提醒ENCLOGDB
        this.sendAlertForDB(dbObject, DB_NAME_ENCLOG);
        //是否觸發提醒FEPHIS
        this.sendAlertForDB(dbObject, DB_NAME_FEPHIS);
        Sms sms = createSms(SERVICE_NAME_DB, hostIp, hostName, null);
        sms.setSmsServicestate("1");
        sms.setSmsOthers(dbObject.toString());
        sms.setSmsUpdatetime(Calendar.getInstance().getTime());
        smsExtMapper.updateByPrimaryKeySelective(sms);
    }

    /**
     * 觸發提醒db
     *
     * @param dbObject
     * @param alertKey
     */
    private void sendAlertForDB(JSONObject dbObject, String alertKey) {
        if (this.monitorSchedulerJobConfig.isStopNotification()) {
            // 2025-03-07 Richard add如果不提醒, 則這裡要重置alert中的資料
            if (this.alertMap.remove(alertKey) != null)
                TRACELogger.debug("[REMOVE_ALERT_KEY]alertKey:", alertKey);
            return;
        }
        if (!Boolean.parseBoolean(dbObject.getString(alertKey))) {
            // 發送提醒
            List<Long> alertTimes = this.alertMap.getOrDefault(alertKey, new ArrayList<>());
            this.alertMap.put(alertKey, alertTimes);
            // 小於設定報警次數才會報警
            if (alertTimes.size() < this.notifyIntervalList.size()) {
                long lastTime = alertTimes.isEmpty() ? 0 : alertTimes.get(alertTimes.size() - 1);
                long nowTime = Calendar.getInstance().getTimeInMillis();
                // 第一次或者大於間隔時間才會報警
                if (lastTime == 0 || nowTime >= lastTime + this.notifyIntervalList.get(alertTimes.size())) {
                    alertTimes.add(nowTime);
                    // String msg = StringUtils.join("主機[", HOSTNAME, ":", HOSTIP, "]無法連線至資料庫", alertKey, "!");
                    // String body = StringUtils.join("停止時間:", FormatUtil.dateTimeInMillisFormat(CalendarUtil.clone(alertTimes.get(0)).getTime()), "\n", msg);
                    // String subject = StringUtils.join(APPNAME + "服務異常第", alertTimes.size(), "次通知-", msg);
                    // String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeInMillisFormat(CalendarUtil.clone(nowTime).getTime()));
                    String remark = StringUtils.join(
                            Arrays.asList(NOTIFY_SUBJECT, hostName,
                                    "第" + alertTimes.size() + "次通知",
                                    "主機[" + hostName + ":" + hostIp + "]無法連線至資料庫[" + alertKey + "]!"
                            ), "-"
                    );
                    this.sendAlertMail(remark);
                    this.sendSms(remark);
                }
            }
        } else {
            if (this.alertMap.remove(alertKey) != null)
                TRACELogger.debug("[REMOVE_ALERT_KEY]alertKey:", alertKey);
        }
    }

    /*
     * MQ信息
     */
    private void fetchMqInfo() {
        if (CollectionUtils.isEmpty(this.monitorSchedulerJobConfig.getMqs())) return;
        boolean alertForMQ, alertForMQManager;
        List<IBMMQStatus> ibmmqStatusList = new ArrayList<>();
        Sms sms = createSms(SERVICE_NAME_MQ, hostIp, hostName, null);
        // int openOptions = CMQC.MQOO_INQUIRE;
        int openOptions = CMQC.MQOO_INPUT_AS_Q_DEF | CMQC.MQOO_INQUIRE | CMQC.MQOO_FAIL_IF_QUIESCING; // 合庫取不到, 調整一下
        for (MonitorMQServerInfo serverInfo : this.monitorSchedulerJobConfig.getMqs()) {
            alertForMQManager = false;
            MQQueueManager queueManager = null;
            try {
                if (CollectionUtils.isNotEmpty(serverInfo.getQueueNames())) {
                    TRACELogger.info("start to fetch MQ, ",
                            "hostname = [", serverInfo.getHostip(), "], ",
                            "port = [", serverInfo.getPort(), "], ",
                            "queueManagerName = [", serverInfo.getQueueManagerName(), "], ",
                            "channel = [", serverInfo.getChannel(), "], ",
                            "userID = [", serverInfo.getUserID(), "], ",
                            "password = [", serverInfo.getSscode(), "]"
                    );
                    queueManager = JmsFactory.createMQQueueManager(
                            serverInfo.getHostip(),
                            Integer.parseInt(serverInfo.getPort()),
                            serverInfo.getQueueManagerName(),
                            serverInfo.getChannel(),
                            serverInfo.getUserID(),
                            serverInfo.getSscode());
                    for (MonitorMQNameInfo monitorMQNameInfo : serverInfo.getQueueNames()) {
                        alertForMQ = false;
                        IBMMQStatus status = new IBMMQStatus();
                        status.setServiceIP(serverInfo.getHostip());
                        status.setServiceHostName(serverInfo.getHostname());
                        status.setName(monitorMQNameInfo.getName());
                        status.setObjectType(monitorMQNameInfo.getType());
                        status.setStatus("0");
                        status.setQueueCount("0");
                        MQQueue queue = null;
                        try {
                            queue = queueManager.accessQueue(monitorMQNameInfo.getName(), openOptions);
                            status.setStatus("1");
                            status.setQueueCount(Integer.toString(queue.getCurrentDepth()));
                            // 如果Queue的數量超過設定值, 則需要發送mail
                            this.sendAlertForMQCountMax(serverInfo, monitorMQNameInfo, queue.getCurrentDepth());
                        } catch (Throwable e) {
                            TRACELogger.warn("accessQueue ip = [", serverInfo.getHostip(), "],",
                                    " port = [", serverInfo.getPort(), "],",
                                    " queueManagerName = [", serverInfo.getQueueManagerName(), "],",
                                    " channel = [", serverInfo.getChannel(), "],",
                                    " queueName = [", monitorMQNameInfo.getName(), "] with exception occur, ", e.getMessage());
                            alertForMQ = true; // 需要發送通知
                        } finally {
                            if (queue != null) {
                                queue.close();
                            }
                            ibmmqStatusList.add(status);
                            // 發送通知
                            sendAlertForMQ(serverInfo, monitorMQNameInfo, alertForMQ);
                        }
                    }
                }
            } catch (Throwable e) {
                TRACELogger.warn("fetchMqInfo ip = [", serverInfo.getHostip(), "],",
                        " port = [", serverInfo.getPort(), "],",
                        " queueManagerName = [", serverInfo.getQueueManagerName(), "],",
                        " channel = [", serverInfo.getChannel(), "] with exception occur, ", e.getMessage());
                // 如果取queueManager出現異常, 還是需要將監控的資料加入, 只是狀態都是0
                for (MonitorMQNameInfo nameInfo : serverInfo.getQueueNames()) {
                    IBMMQStatus status = new IBMMQStatus();
                    status.setServiceIP(serverInfo.getHostip());
                    status.setServiceHostName(serverInfo.getHostname());
                    status.setName(nameInfo.getName());
                    status.setObjectType(nameInfo.getType());
                    status.setStatus("0");
                    status.setQueueCount("0");
                    ibmmqStatusList.add(status);
                }
                alertForMQManager = true; // 需要發送通知
            } finally {
                if (queueManager != null) {
                    try {
                        queueManager.disconnect();
                    } catch (Throwable e) {
                        TRACELogger.warn(e, e.getMessage());
                    }
                }
                sendAlertForMQ(serverInfo, null, alertForMQManager);
            }
            sms.setSmsServicestate(ibmmqStatusList.stream().filter(t -> "1".equals(t.getStatus())).count() == ibmmqStatusList.size() ? "1" : "0");
            sms.setSmsOthers(new Gson().toJson(ibmmqStatusList));
            sms.setSmsUpdatetime(Calendar.getInstance().getTime());
            smsExtMapper.updateByPrimaryKeySelective(sms);
        }
    }

    /**
     * MQ送Alert
     *
     * @param serverInfo
     * @param monitorMQNameInfo
     * @param alert
     */
    private void sendAlertForMQ(MonitorMQServerInfo serverInfo, MonitorMQNameInfo monitorMQNameInfo, boolean alert) {
        String alertKey = StringUtils.join(
                Arrays.asList(
                        serverInfo.getHostname(),
                        serverInfo.getHostip(),
                        serverInfo.getPort(),
                        serverInfo.getQueueManagerName(),
                        serverInfo.getChannel(),
                        monitorMQNameInfo == null ? "QueueManager" : monitorMQNameInfo.getName()), '@');
        if (this.monitorSchedulerJobConfig.isStopNotification()) {
            // 2025-03-07 Richard add如果不提醒, 則這裡要重置alert中的資料
            if (this.alertMap.remove(alertKey) != null)
                TRACELogger.debug("[REMOVE_ALERT_KEY]alertKey:", alertKey);
            return;
        }
        if (alert) {
            // 發送提醒
            List<Long> alertTimes = this.alertMap.getOrDefault(alertKey, new ArrayList<>());
            this.alertMap.put(alertKey, alertTimes);
            // 小於設定報警次數才會報警
            if (alertTimes.size() < this.notifyIntervalList.size()) {
                long lastTime = alertTimes.isEmpty() ? 0 : alertTimes.get(alertTimes.size() - 1);
                long nowTime = Calendar.getInstance().getTimeInMillis();
                // 第一次或者大於間隔時間才會報警
                if (lastTime == 0 || nowTime >= lastTime + this.notifyIntervalList.get(alertTimes.size())) {
                    alertTimes.add(nowTime);
                    // String msg = StringUtils.join("FEP在主機[", serverInfo.getHostname(), ":", serverInfo.getHostip(), ":", serverInfo.getPort(), ":", serverInfo.getQueueManagerName(), ":", serverInfo.getChannel(), "]上的MQ[", monitorMQNameInfo.getName(), "]無法訪問!");
                    // String body = StringUtils.join("服務停止時間:", FormatUtil.dateTimeInMillisFormat(CalendarUtil.clone(alertTimes.get(0)).getTime()), "\n", msg);
                    // String subject = StringUtils.join(APPNAME + "服務異常第", alertTimes.size(), "次通知-", msg);
                    // String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeInMillisFormat(CalendarUtil.clone(nowTime).getTime()));
                    String remark = StringUtils.join(
                            Arrays.asList(NOTIFY_SUBJECT, hostName,
                                    "第" + alertTimes.size() + "次通知",
                                    "主機[" + StringUtils.join(
                                            Arrays.asList(serverInfo.getHostname(), serverInfo.getHostip(), serverInfo.getPort(), serverInfo.getQueueManagerName(), serverInfo.getChannel()), ":") +
                                            "]上的MQ" + (monitorMQNameInfo == null ? StringUtils.EMPTY : "[" + monitorMQNameInfo.getName() + "]") + "無法訪問!"
                            ), "-"
                    );
                    this.sendAlertMail(remark);
                    this.sendSms(remark);
                }
            }
        } else {
            if (this.alertMap.remove(alertKey) != null)
                TRACELogger.debug("[REMOVE_ALERT_KEY]alertKey:", alertKey);
        }
    }

    /**
     * MQ送Alert, Queue數量超過限制
     *
     * @param serverInfo
     * @param monitorMQNameInfo
     * @param currentDepth
     */
    private void sendAlertForMQCountMax(MonitorMQServerInfo serverInfo, MonitorMQNameInfo monitorMQNameInfo, int currentDepth) {
        String alertKey = StringUtils.join(
                Arrays.asList(
                        serverInfo.getHostname(),
                        serverInfo.getHostip(),
                        serverInfo.getPort(),
                        serverInfo.getQueueManagerName(),
                        serverInfo.getChannel(),
                        monitorMQNameInfo.getName(),
                        "QueueMax"), '@');
        if (this.monitorSchedulerJobConfig.isStopNotification()) {
            // 2025-03-07 Richard add如果不提醒, 則這裡要重置alert中的資料
            if (this.alertMap.remove(alertKey) != null)
                TRACELogger.debug("[REMOVE_ALERT_KEY]alertKey:", alertKey);
            return;
        }
        if (monitorMQNameInfo.getQueueMax() > 0 && currentDepth > monitorMQNameInfo.getQueueMax()) {
            // 發送提醒
            List<Long> alertTimes = this.alertMap.getOrDefault(alertKey, new ArrayList<>());
            this.alertMap.put(alertKey, alertTimes);
            // 小於設定報警次數才會報警
            if (alertTimes.size() < this.notifyIntervalList.size()) {
                long lastTime = alertTimes.isEmpty() ? 0 : alertTimes.get(alertTimes.size() - 1);
                long nowTime = Calendar.getInstance().getTimeInMillis();
                // 第一次或者大於間隔時間才會報警
                if (lastTime == 0 || nowTime >= lastTime + this.notifyIntervalList.get(alertTimes.size())) {
                    alertTimes.add(nowTime);
                    // String msg = StringUtils.join("FEP在主機[", serverInfo.getHostname(), ":", serverInfo.getHostip(), ":", serverInfo.getPort(), ":", serverInfo.getQueueManagerName(), ":", serverInfo.getChannel(), "]上的MQ[", monitorMQNameInfo.getName(), "]訊息數量為[", FormatUtil.longFormat(currentDepth), "]已經超過[", FormatUtil.longFormat(monitorMQNameInfo.getQueueMax()), "]警示值!");
                    // String body = StringUtils.join("服務異常時間:", FormatUtil.dateTimeInMillisFormat(CalendarUtil.clone(alertTimes.get(0)).getTime()), "\n", msg);
                    // String subject = StringUtils.join(APPNAME + "服務異常第", alertTimes.size(), "次通知-", msg);
                    // String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeInMillisFormat(CalendarUtil.clone(nowTime).getTime()));
                    String remark = StringUtils.join(
                            Arrays.asList(NOTIFY_SUBJECT, hostName,
                                    "第" + alertTimes.size() + "次通知",
                                    "主機[" + StringUtils.join(
                                            Arrays.asList(
                                                    serverInfo.getHostname(), serverInfo.getHostip(), serverInfo.getPort(), serverInfo.getQueueManagerName(), serverInfo.getChannel()
                                            ), ":") + "]上的MQ[" + monitorMQNameInfo.getName() + "]訊息數量為[" + FormatUtil.longFormat(currentDepth) + "]已經超過[" + FormatUtil.longFormat(monitorMQNameInfo.getQueueMax()) + "]警示值!"
                            ), "-"
                    );
                    this.sendAlertMail(remark);
                    this.sendSms(remark);
                }
            }
        } else {
            if (this.alertMap.remove(alertKey) != null)
                TRACELogger.debug("[REMOVE_ALERT_KEY]alertKey:", alertKey);
        }
    }

    /*
     * 服務信息
     */
    private void fetchServicesInfo() {
        TRACELogger.info("enter fetchServicesInfo");
        MonitorServerInfo[] services = new MonitorServerInfo[this.monitorSchedulerJobConfig.getServices().size()];
        this.monitorSchedulerJobConfig.getServices().toArray(services);
        boolean isError;
        for (MonitorServerInfo serverInfo : services) {
            Sms smsServices = smsExtMapper.selectByPrimaryKey(serverInfo.getName(), serverInfo.getHostip());
            if (smsServices == null) {
                continue;
            }
            // NO SERVICE
            if (SERVICE_NAME_SYSTEM.equals(smsServices.getSmsServicename())
                    || SERVICE_NAME_DB.equals(smsServices.getSmsServicename())
                    || SERVICE_NAME_MQ.equals(smsServices.getSmsServicename())
                    || SERVICE_NAME_NET_CLIENT.equals(smsServices.getSmsServicename())
                    || SERVICE_NAME_NET_SERVER.equals(smsServices.getSmsServicename())
                    || SERVICE_NAME_PROCESS.equals(smsServices.getSmsServicename())) {
                continue;
            }
            // 服務異常
            isError = false;
            if ("0".equals(smsServices.getSmsServicestate())) {
                //SERVICE STOP
                isError = true;
                // 2025-01-08 Richard add 若沒有停止時間, 則更新停止時間
                if (smsServices.getSmsStoptime() == null) {
                    smsServices.setSmsStoptime(Calendar.getInstance().getTime());
                    smsServices.setSmsUpdatetime(Calendar.getInstance().getTime());
                    smsExtMapper.updateByPrimaryKey(smsServices);
                }
            } else {
                // 如果超過2分鐘無更新資料，判定為服務異常(n為監控的取樣間隔時間)
                if (System.currentTimeMillis() - CalendarUtil.clone(smsServices.getSmsUpdatetime()).getTimeInMillis() > 2 * 60 * 1000L) {
                    // SERVICE STOP
                    if (!smsServices.getSmsServicename().equalsIgnoreCase(appName)) {
                        smsServices.setSmsServicestate("0");
                    }
                    smsServices.setSmsHostname(serverInfo.getHostname());
                    smsServices.setSmsUpdatetime(Calendar.getInstance().getTime());
                    // 2025-03-07 Richard modified 若沒有停止時間, 則更新停止時間
                    if (smsServices.getSmsStoptime() == null)
                        smsServices.setSmsStoptime(smsServices.getSmsUpdatetime());
                    smsExtMapper.updateByPrimaryKey(smsServices);
                    isError = true;
                } else {
                    // SERVICE RUNNING
                }
            }
            String alertKey = StringUtils.join(Arrays.asList(serverInfo.getHostip(), serverInfo.getName()), '@');
            if (isError) {
                // 是否提醒
                if (this.monitorSchedulerJobConfig.isStopNotification()) {
                    // 2025-03-07 Richard add如果不提醒, 則這裡要重置alert中的資料
                    if (this.alertMap.remove(alertKey) != null)
                        TRACELogger.debug("[REMOVE_ALERT_KEY]alertKey:", alertKey);
                } else {
                    // 發送提醒
                    List<Long> alertTimes = this.alertMap.getOrDefault(alertKey, new ArrayList<>());
                    this.alertMap.put(alertKey, alertTimes);
                    // 小於設定報警次數才會報警
                    if (alertTimes.size() < this.notifyIntervalList.size()) {
                        long lastTime = alertTimes.isEmpty() ? 0 : alertTimes.get(alertTimes.size() - 1);
                        long nowTime = Calendar.getInstance().getTimeInMillis();
                        // 第一次或者大於間隔時間才會報警
                        if (lastTime == 0 || nowTime >= lastTime + this.notifyIntervalList.get(alertTimes.size())) {
                            alertTimes.add(nowTime);
                            // String msg = StringUtils.join("FEP在主機[", serverInfo.getHostname(), ":", serverInfo.getHostip(), "]上的服務[", serverInfo.getName(), "]已停止!");
                            // String body = StringUtils.join("服務停止時間:", FormatUtil.dateTimeInMillisFormat(smsServices.getSmsStoptime()), "\n", msg);
                            // String subject = StringUtils.join(APPNAME + "服務異常第", alertTimes.size(), "次通知-", msg);
                            // String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeInMillisFormat(CalendarUtil.clone(nowTime).getTime()));
                            String remark = StringUtils.join(
                                    Arrays.asList(NOTIFY_SUBJECT, hostName,
                                            "第" + alertTimes.size() + "次通知",
                                            "主機[" + serverInfo.getHostname() + ":" + serverInfo.getHostip() + "]上的服務[" + serverInfo.getName() + "]已停止"
                                    ), "-"
                            );
                            this.sendAlertMail(remark);
                            this.sendSms(remark);
                        }
                    }
                }
                // 根據設定是否需要重啟服務
                if (this.monitorSchedulerJobConfig.isEnableAutoRestart()) {
                    this.notifyMonitorServerLauncher(serverInfo);
                }
            } else {
                // 2025-03-07 Richard add如果服務有重新恢復異常, 則這裡要重置alert中的資料
                if (this.alertMap.remove(alertKey) != null)
                    TRACELogger.debug("[REMOVE_ALERT_KEY]alertKey:", alertKey);
                if (this.monitorSchedulerJobConfig.isEnableAutoRestart()) {
                    MonitorServerLauncherInfo launcher =
                            this.monitorSchedulerJobConfig.getLaunchers().stream().filter(
                                    t -> t.getName().equalsIgnoreCase(serverInfo.getName()) && t.getHostip().equalsIgnoreCase(serverInfo.getHostip())).findFirst().orElse(null);
                    if (launcher != null) {
                        // 如果有啟動成功, 則將上一次重啟的日期時間設置為null
                        launcher.setLatestCmdStart(null);
                    }
                }
            }
            // 只有服務是正常運行的情況下, 才需要判斷是否需要送Alert
            if (!isError) {
                MonitorThreshold monitorThreshold = serverInfo.getThreshold();
                // RAM MB
                int value = (smsServices.getSmsRam() == null ? 0 : smsServices.getSmsRam()) / 1024;
                int threshold = monitorThreshold == null ? 0 : monitorThreshold.getRamThreshold();
                boolean alert = threshold > 0 && value > threshold;
                TRACELogger.debug("[", serverInfo.getHostname(), "-", serverInfo.getHostip(), "-", serverInfo.getName(), "][RamThreshold]value:", value, ",threshold:", threshold, ",threshold>0&&value>threshold:", alert);
                this.sendServicesAlert(serverInfo, alert, "RamThreshold", StringUtils.join("使用的記憶體為[", FormatUtil.longFormat(value), "]MB, 已超過警示值[", FormatUtil.longFormat(threshold), "]MB"));
                // CpuThreshold
                value = (smsServices.getSmsCpu() == null ? 0 : smsServices.getSmsCpu()) / 100;
                threshold = monitorThreshold == null ? 0 : monitorThreshold.getCpuThreshold();
                alert = threshold > 0 && value > threshold;
                TRACELogger.debug("[", serverInfo.getHostname(), "-", serverInfo.getHostip(), "-", serverInfo.getName(), "][CpuThreshold]value:", value, ",threshold:", threshold, ",threshold>0&&value>threshold:", alert);
                this.sendServicesAlert(serverInfo, alert, "CpuThreshold", StringUtils.join("使用的CPU%為[", value, "]%, 已超過警示值[", threshold, "]%"));
                // ThreadThreshold
                value = smsServices.getSmsThreads() == null ? 0 : smsServices.getSmsThreads();
                threshold = monitorThreshold == null ? 0 : monitorThreshold.getThreadThreshold();
                alert = threshold > 0 && value > threshold;
                TRACELogger.debug("[", serverInfo.getHostname(), "-", serverInfo.getHostip(), "-", serverInfo.getName(), "][ThreadThreshold]value:", value, ",threshold:", threshold, ",threshold>0&&value>threshold:", alert);
                this.sendServicesAlert(serverInfo, alert, "ThreadThreshold", StringUtils.join("使用的執行緒為[", FormatUtil.longFormat(value), "], 已超過警示值[", FormatUtil.longFormat(threshold), "]"));
            }
        }
        TRACELogger.info("exit fetchServicesInfo");
    }

    /**
     * 服務警示
     *
     * @param serverInfo
     * @param alert
     * @param alertKeySuffix
     * @param msgSuffix
     */
    private void sendServicesAlert(MonitorServerInfo serverInfo, boolean alert, String alertKeySuffix, String msgSuffix) {
        String alertKey = StringUtils.join(Arrays.asList(serverInfo.getHostip(), serverInfo.getName(), alertKeySuffix), '@');
        // 是否提醒
        if (this.monitorSchedulerJobConfig.isStopNotification() || serverInfo.getThreshold() == null || !serverInfo.getThreshold().isNotification()) {
            // 2025-03-07 Richard add如果不提醒, 則這裡要重置alert中的資料
            if (this.alertMap.remove(alertKey) != null)
                TRACELogger.debug("[REMOVE_ALERT_KEY][alertMap]alertKey:", alertKey);
            if (this.exceedThresholdTimesMap.remove(alertKey) != null)
                TRACELogger.debug("[REMOVE_ALERT_KEY][exceedThresholdTimesMap]alertKey:", alertKey);
            return;
        }
        if (alert) {
            // 當連續超過警示值幾次, 才觸發告警條件
            if (serverInfo.getThreshold() != null && serverInfo.getThreshold().getExceedThresholdTimes() > 0) {
                int times = this.exceedThresholdTimesMap.getOrDefault(alertKey, 1);
                if (times < serverInfo.getThreshold().getExceedThresholdTimes()) {
                    TRACELogger.debug("[sendServicesAlert][exceedThresholdTimesMap]alertKey:", alertKey, ", times:", times);
                    times++;
                    this.exceedThresholdTimesMap.put(alertKey, times);
                    return;
                }
            }
            // 發送提醒
            List<Long> alertTimes = this.alertMap.getOrDefault(alertKey, new ArrayList<>());
            this.alertMap.put(alertKey, alertTimes);
            // 小於設定報警次數才會報警
            if (alertTimes.size() < this.notifyIntervalList.size()) {
                long lastTime = alertTimes.isEmpty() ? 0 : alertTimes.get(alertTimes.size() - 1);
                long nowTime = Calendar.getInstance().getTimeInMillis();
                // 第一次或者大於間隔時間才會報警
                if (lastTime == 0 || nowTime >= lastTime + this.notifyIntervalList.get(alertTimes.size())) {
                    alertTimes.add(nowTime);
                    // String msg = StringUtils.join("FEP在主機[", serverInfo.getHostname(), ":", serverInfo.getHostip(), "]上的服務[", serverInfo.getName(), "]", msgSuffix, "!");
                    // String body = StringUtils.join("服務異常時間:", FormatUtil.dateTimeInMillisFormat(CalendarUtil.clone(alertTimes.get(0)).getTime()), "\n", msg);
                    // String subject = StringUtils.join(APPNAME + "服務異常第", alertTimes.size(), "次通知-", msg);
                    // String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeInMillisFormat(CalendarUtil.clone(nowTime).getTime()));
                    String remark = StringUtils.join(
                            Arrays.asList(NOTIFY_SUBJECT, hostName,
                                    "第" + alertTimes.size() + "次通知",
                                    "主機[" + serverInfo.getHostname() + ":" + serverInfo.getHostip() + "]上的服務[" + serverInfo.getName() + "]" + msgSuffix + "!"
                            ), "-"
                    );
                    this.sendAlertMail(remark);
                    this.sendSms(remark);
                }
            }
        } else {
            // 2025-03-07 Richard add如果服務有重新恢復異常, 則這裡要重置alert中的資料
            if (this.alertMap.remove(alertKey) != null)
                TRACELogger.debug("[REMOVE_ALERT_KEY][alertMap]alertKey:", alertKey);
            if (this.exceedThresholdTimesMap.remove(alertKey) != null)
                TRACELogger.debug("[REMOVE_ALERT_KEY][exceedThresholdTimesMap]alertKey:", alertKey);
        }
    }

    /**
     * 系統狀態---CPU
     *
     * @return
     */
    private int fetchSystemCpuUsage() {
        int cpu = 0;
        try {
            cpu = MonitorDataCollector.fetchSystemCpuUsage(httpClient2, url).intValue();
        } catch (Throwable e) {
            TRACELogger.warn(ProgramName, " fetchSystemCpuUsage with exception occur, ", e.getMessage());
        }
        return cpu;
    }

    /**
     * 系統狀態---RMA
     *
     * @return
     */
    private int fetchSystemMemoryUsage() {
        int memo = 0;
        try {
            memo = MonitorDataCollector.fetchSystemMemoryUsage().intValue();
        } catch (Throwable e) {
            TRACELogger.warn(ProgramName, " fetchSystemMemoryUsage with exception occur, ", e.getMessage());
        }
        return memo;
    }

    /**
     * 系統DISK USED
     *
     * @return
     */
    private String fetchSystemHardDisk() {
        if (monitorSchedulerJobConfig.getSystemFileIncludeMountInterval() > 0 && latestFetchSystemHardDisk > 0
                && System.currentTimeMillis() - latestFetchSystemHardDisk <= monitorSchedulerJobConfig.getSystemFileIncludeMountInterval() && StringUtils.isNotBlank(latestSystemHardDisk)) {
            return latestSystemHardDisk;
        }
        String disk = StringUtils.EMPTY;
        try {
            JSONArray disArray = new JSONArray();
            List<MonitorDataDisk> monitorDataDiskList;
            // 如果沒有設定需要監控哪些資料夾, 則預設取OS的磁盤空間使用情況
            if (CollectionUtils.isEmpty(this.systemFileIncludeMountList)) {
                monitorDataDiskList = MonitorDataCollector.fetchSystemHardDisk(hostName, hostIp);
            } else {
                monitorDataDiskList = new ArrayList<>();
                for (String systemMount : this.systemFileIncludeMountList) {
                    monitorDataDiskList.add(MonitorDataCollector.fetchMonitorDataDisk(hostName, hostIp, systemMount));
                }
            }
            for (MonitorDataDisk monitorDataDisk : monitorDataDiskList) {
                JSONObject object = this.analyseDiskData(monitorDataDisk);
                if (object != null)
                    disArray.put(object);
            }
            disk = disArray.toString();
        } catch (Throwable e) {
            TRACELogger.warn(ProgramName, " fetchSystemHardDisk with exception occur, ", e.getMessage());
        }
        if (monitorSchedulerJobConfig.getSystemFileIncludeMountInterval() > 0) {
            latestFetchSystemHardDisk = System.currentTimeMillis();
            latestSystemHardDisk = disk;
        }
        return disk;
    }

    /**
     * 分析處理磁盤信息
     *
     * @param monitorDataDisk
     * @return
     */
    public JSONObject analyseDiskData(MonitorDataDisk monitorDataDisk) {
        JSONObject disObject = new JSONObject();
        String diskName = monitorDataDisk.getName();
        // int gb = (int) Math.pow(1024, 3);
        String diskTotal = oshi.util.FormatUtil.formatBytes(monitorDataDisk.getTotal()); // FormatUtil.doubleFormat(monitorDataDisk.getTotal() / gb, "#,###G");
        String diskFree = oshi.util.FormatUtil.formatBytes(monitorDataDisk.getFree()); // FormatUtil.doubleFormat(monitorDataDisk.getFree() / gb, "#,###G");
        String diskUsed = oshi.util.FormatUtil.formatBytes(monitorDataDisk.getUsed()); // FormatUtil.doubleFormat(monitorDataDisk.getUsed() / gb, "#,###G");
        double total = monitorDataDisk.getTotal();
        double used = monitorDataDisk.getUsed();
        double diskRate = used / total;
        double diskFreeRate = Double.parseDouble(FormatUtil.doubleFormat(monitorDataDisk.getFree() / total, "0.00"));
        String diskFreeRateStr = FormatUtil.doubleFormat(diskFreeRate, "0%");
        String ruleRiskRateStr = FormatUtil.doubleFormat(ruleRiskRate, "0%");
        disObject.put(JSON_FIELD_HOSTNAME, monitorDataDisk.getHostName());
        disObject.put(JSON_FIELD_NAME, diskName);
        disObject.put(JSON_FIELD_IP, monitorDataDisk.getIp());
        disObject.put(JSON_FIELD_USED, diskUsed);
        disObject.put(JSON_FIELD_TOTAL, diskTotal);
        disObject.put(JSON_FIELD_DISK, FormatUtil.doubleFormat(diskRate, "0.##%"));
        //是否提醒
        String alertKey = StringUtils.join(Arrays.asList(monitorDataDisk.getIp(), diskName), '@');
        if (this.monitorSchedulerJobConfig.isStopNotification()) {
            // 2025-03-07 Richard add如果不提醒, 則這裡要重置alert中的資料
            if (this.alertMap.remove(alertKey) != null)
                TRACELogger.debug("[REMOVE_ALERT_KEY]alertKey:", alertKey);
        } else {
            //是否觸發提醒
            if (diskFreeRate < ruleRiskRate) {
                // 發送提醒
                List<Long> alertTimes = this.alertMap.getOrDefault(alertKey, new ArrayList<>());
                this.alertMap.put(alertKey, alertTimes);
                // 小於設定報警次數才會報警
                if (alertTimes.size() < this.notifyIntervalList.size()) {
                    long lastTime = alertTimes.isEmpty() ? 0 : alertTimes.get(alertTimes.size() - 1);
                    long nowTime = Calendar.getInstance().getTimeInMillis();
                    // 第一次或者大於間隔時間才會報警
                    if (lastTime == 0 || nowTime >= lastTime + this.notifyIntervalList.get(alertTimes.size())) {
                        alertTimes.add(nowTime);
                        // String msg = StringUtils.join("主機[", monitorDataDisk.getHostName(), ":", monitorDataDisk.getIp(), "]磁碟", diskName, "可用空間目前剩餘", diskFreeRateStr, "(", diskFree, "),已低於", ruleRiskRateStr, "!");
                        // String body = StringUtils.join("服務異常時間:", FormatUtil.dateTimeInMillisFormat(CalendarUtil.clone(alertTimes.get(0)).getTime()), "\n", msg);
                        // String subject = StringUtils.join(APPNAME + "服務異常第", alertTimes.size(), "次通知-", msg);
                        // String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeInMillisFormat(CalendarUtil.clone(nowTime).getTime()));
                        String remark = StringUtils.join(
                                Arrays.asList(NOTIFY_SUBJECT, hostName,
                                        "第" + alertTimes.size() + "次通知",
                                        "主機[" + monitorDataDisk.getHostName() + ":" + monitorDataDisk.getIp() + "]磁碟[" + diskName + "]可用空間目前剩餘[" + diskFreeRateStr + "(" + diskFree + ")], 已低於[" + ruleRiskRateStr + "]!"
                                ), "-"
                        );
                        this.sendAlertMail(remark);
                        this.sendSms(remark);
                    }
                }
            } else {
                if (this.alertMap.remove(alertKey) != null)
                    TRACELogger.debug("[REMOVE_ALERT_KEY]alertKey:", alertKey);
            }
        }
        return disObject;
    }

    /**
     * 建立SMS物件
     *
     * @param sname
     * @param ip
     * @param hname
     * @param monitorThreshold
     * @return
     */
    private Sms createSms(String sname, String ip, String hname, MonitorThreshold monitorThreshold) {
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
        if (monitorThreshold != null && monitorThreshold.isNotification()) {
            sms.setSmsCpuThreshold(monitorThreshold.getCpuThreshold());
            sms.setSmsRamThreshold(monitorThreshold.getRamThreshold());
            sms.setSmsThreadsThreshold(monitorThreshold.getThreadThreshold());
        }
        return sms;
    }

    /**
     * 發送提醒郵件
     *
     * @param remark
     */
    private void sendAlertMail(String remark) {
        MailSender mailSender = SpringBeanFactoryUtil.getBean(MailSender.class, false);
        if (mailSender != null) {
            TRACELogger.info("[sendAlertMail]", remark);
            try {
                // notifyHelper.sendSimpleMail(NotifyHelperTemplateId.APP_MONITOR, StringUtils.join(this.monitorSchedulerJobConfig.getMailList(), ','), StringUtils.join(subject, "\r\n", body), true);
                // 2025-09-03 Richard modified不再使用notify送mail, 改用FEP自己送
                // notifyHelper.sendSimpleMail(NotifyHelperTemplateId.APP_MONITOR, StringUtils.join(this.monitorSchedulerJobConfig.getMailList(), ','), remark, true);
                MailData mailData = new MailData();
                mailData.setFrom(this.monitorSchedulerJobConfig.getMailSender());
                mailData.setTo(StringUtils.join(this.monitorSchedulerJobConfig.getMailList(), ','));
                mailData.setSubject(NOTIFY_SUBJECT);
                mailData.setBody(remark);
                mailData.setPriority(MailPriority.High);
                mailSender.sendSimpleEmail(mailData);
            } catch (Throwable e) {
                TRACELogger.warn("sendAlertMail with exception occur, ", e.getMessage());
            }
        }
    }

    /*
     * 獲取提醒次數&間隔時間配置
     * 例如設定1,3,5, 表示一共通知四次, 分別在第0分鐘, 第1分鐘, 第3分鐘, 第5分鐘時發送提醒
     */
    private void parseNotifyInterval() {
        String notifyInterval = this.monitorSchedulerJobConfig.getNotifyInterval();
        if (StringUtils.isNotBlank(notifyInterval)) {
            String[] intervals = notifyInterval.split(",");
            if (ArrayUtils.isNotEmpty(intervals)) {
                this.notifyIntervalList.add(0L);
                try {
                    for (String inter : intervals) {
                        this.notifyIntervalList.add(Long.parseLong(inter.trim()) * 60 * 1000);
                    }
                } catch (NumberFormatException e) {
                    TRACELogger.warn("parseNotifyInterval with exception occur, ", e.getMessage());
                }
                TRACELogger.info("parseNotifyInterval, notifyIntervalList:", StringUtils.join(this.notifyIntervalList, ','));
            }
        }
    }

    private List<IBMMQStatus> getIBMMQStatusList() {
        List<IBMMQStatus> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(this.monitorSchedulerJobConfig.getMqs())) {
            for (MonitorMQServerInfo serverInfo : this.monitorSchedulerJobConfig.getMqs()) {
                if (CollectionUtils.isNotEmpty(serverInfo.getQueueNames())) {
                    for (MonitorMQNameInfo nameInfo : serverInfo.getQueueNames()) {
                        IBMMQStatus status = new IBMMQStatus();
                        status.setServiceIP(serverInfo.getHostip());
                        status.setServiceHostName(serverInfo.getHostname());
                        status.setName(nameInfo.getName());
                        status.setObjectType(nameInfo.getType());
                        status.setStatus("0");
                        status.setQueueCount("0");
                        list.add(status);
                    }
                }
            }
        }
        return list;
    }

    private void fetchProcess() {
        List<Sms> smsProcessList = new ArrayList<>();
        Map<String, OSProcess> existProcessMap = new HashMap<>();
        Function<Boolean, List<OSProcess>> fetchAllProcess = new Function<>() {
            List<OSProcess> allProcesses = null;

            @Override
            public List<OSProcess> apply(Boolean clear) {
                if (clear) {
                    allProcesses = null;
                } else {
                    if (CollectionUtils.isEmpty(allProcesses)) {
                        allProcesses = OperationSystemDataCollector.getOSProcesses();
                    }
                }
                return allProcesses;
            }
        };
        boolean needRefresh = false, isError;
        for (String processName : this.monitorSchedulerJobConfig.getProcessNameList()) {
            isError = false;
            OSProcess osProcess = this.fetchProcess(processName, fetchAllProcess);
            // 如果取到, 將process放入map中
            if (osProcess != null) {
                // 將pid存入map中, 以便下次優先依據pid取process
                this.processNameToPidMap.put(processName, osProcess.getProcessID());
                // 這裡存入map中, 以便後面再取一次用於計算CPU%
                existProcessMap.put(processName, osProcess);
            }
            // 如果還是取不到, 則使用ps指令check
            boolean psCheck = false;
            if (osProcess == null) {
                // 先sleep1秒
                Util.sleep(this.monitorSchedulerJobConfig.getSleepForPsCheckBefore());
                // 如果名字是suipsrv, 要把suipsrv1排除掉, 否則check的結果不準
                psCheck = Ps.processExists(processName, "suipsrv".equalsIgnoreCase(processName) ? Collections.singletonList("suipsrv1") : null);
                TRACELogger.warn("[", ProgramName, ".fetchProcess]check process via ps command, name:", processName, ",psCheck:", psCheck);
            }
            // 這裡使用ps指令, 取出信息, 暫時只是列印log, 方便check問題
            // 如果名字是suipsrv, 要把suipsrv1排除掉, 否則check的結果不準
            // List<PsAuxData> psAuxDatas = Ps.getPsAuxDataList(processName, "suipsrv".equalsIgnoreCase(processName) ? Collections.singletonList("suipsrv1") : null);
            // if (CollectionUtils.isNotEmpty(psAuxDatas)) {
            //     for (PsAuxData psAuxData : psAuxDatas) {
            //         TRACELogger.debug("[", ProgramName, ".fetchProcess]found process via ps command, ", psAuxData);
            //     }
            // }
            // 如果透過oshi api有取到process, 後面更新SMS檔
            // 或者是使用ps指令有check存在, 避免送警告提醒
            if (osProcess != null || psCheck) {
                // 如果有取到osProcess, 則根據osProcess建立Sms物件
                if (osProcess != null) {
                    needRefresh = true;
                    Sms sms = new Sms();
                    sms.setSmsServicename(processName);
                    sms.setSmsHostname(hostName);
                    sms.setSmsServiceip(hostIp);
                    sms.setSmsServicestate("1");
                    sms.setSmsStarttime(CalendarUtil.clone(osProcess.getStartTime()).getTime());
                    sms.setSmsUpdatetime(Calendar.getInstance().getTime());
                    sms.setSmsCpu((int) (10000L * (osProcess.getKernelTime() + osProcess.getUserTime()) / osProcess.getUpTime()));
                    sms.setSmsRam((int) (osProcess.getResidentSetSize() / 1024));
                    sms.setSmsThreads(osProcess.getThreadCount());
                    sms.setSmsPid(osProcess.getProcessID());
                    TRACELogger.debug("[", ProgramName, ".fetchProcess]found process by oshi api, name:", processName, ",sms:", sms);
                    smsProcessList.add(sms);
                }
                if (psCheck) {
                    TRACELogger.debug("[", ProgramName, ".fetchProcess]found process by ps command, name:", processName, ",psCheck:", psCheck);
                }
                processNameToStopTimeMap.remove(processName);
                if (this.monitorSchedulerJobConfig.isEnableAutoRestart()) {
                    MonitorServerLauncherInfo launcher =
                            this.monitorSchedulerJobConfig.getLaunchers().stream().filter(t -> t.getName().equalsIgnoreCase(processName)).findFirst().orElse(null);
                    if (launcher != null) {
                        // 如果有啟動成功, 則將上一次重啟的日期時間設置為null
                        launcher.setLatestCmdStart(null);
                    }
                }
            } else {
                isError = true;
                Date stopTime = processNameToStopTimeMap.get(processName);
                if (stopTime == null) {
                    stopTime = Calendar.getInstance().getTime();
                    processNameToStopTimeMap.put(processName, stopTime);
                }
                Sms sms = new Sms();
                sms.setSmsServicename(processName);
                sms.setSmsHostname(hostName);
                sms.setSmsServiceip(hostIp);
                sms.setSmsServicestate("0");
                sms.setSmsUpdatetime(Calendar.getInstance().getTime());
                sms.setSmsStoptime(stopTime);
                sms.setSmsCpu(0);
                sms.setSmsRam(0);
                sms.setSmsThreads(0);
                sms.setSmsPid(null);
                TRACELogger.warn("[", ProgramName, ".fetchProcess]cannot found process name:", processName, ",sms:", sms);
                smsProcessList.add(sms);
            }
            String alertKey = StringUtils.join(Arrays.asList(hostIp, processName), '@');
            if (isError) {
                // 是否提醒
                if (this.monitorSchedulerJobConfig.isStopNotification()) {
                    // 2025-03-07 Richard add如果不提醒, 則這裡要重置alert中的資料
                    if (this.alertMap.remove(alertKey) != null)
                        TRACELogger.debug("[REMOVE_ALERT_KEY]alertKey:", alertKey);
                } else {
                    // 發送提醒
                    List<Long> alertTimes = this.alertMap.getOrDefault(alertKey, new ArrayList<>());
                    this.alertMap.put(alertKey, alertTimes);
                    // 小於設定報警次數才會報警
                    if (alertTimes.size() < this.notifyIntervalList.size()) {
                        long lastTime = alertTimes.isEmpty() ? 0 : alertTimes.get(alertTimes.size() - 1);
                        long nowTime = Calendar.getInstance().getTimeInMillis();
                        // 第一次或者大於間隔時間才會報警
                        if (lastTime == 0 || nowTime >= lastTime + this.notifyIntervalList.get(alertTimes.size())) {
                            alertTimes.add(nowTime);
                            // String msg = StringUtils.join("FEP在主機[", HOSTNAME, ":", HOSTIP, "]上的服務[", processName, "]已停止!");
                            // String body = StringUtils.join("服務停止時間:", FormatUtil.dateTimeInMillisFormat(CalendarUtil.clone(alertTimes.get(0)).getTime()), "\n", msg);
                            // String subject = StringUtils.join(APPNAME + "服務異常第", alertTimes.size(), "次通知-", msg);
                            // String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeInMillisFormat(CalendarUtil.clone(nowTime).getTime()));
                            String remark = StringUtils.join(
                                    Arrays.asList(NOTIFY_SUBJECT, hostName,
                                            "第" + alertTimes.size() + "次通知",
                                            "主機[" + hostName + ":" + hostIp + "]上的服務[" + processName + "]已停止!")
                                    , "-"
                            );
                            this.sendAlertMail(remark);
                            this.sendSms(remark);
                        }
                    }
                }
                // 根據設定是否需要重啟服務
                if (this.monitorSchedulerJobConfig.isEnableAutoRestart()) {
                    this.notifyMonitorProcessLauncher(processName);
                }
            } else {
                // 2025-03-07 Richard add如果服務有重新恢復異常, 則這裡要重置alert中的資料
                if (this.alertMap.remove(alertKey) != null)
                    TRACELogger.debug("[REMOVE_ALERT_KEY]alertKey:", alertKey);
                if (this.monitorSchedulerJobConfig.isEnableAutoRestart()) {
                    MonitorServerLauncherInfo launcher =
                            this.monitorSchedulerJobConfig.getLaunchers().stream().filter(t -> t.getName().equalsIgnoreCase(processName)).findFirst().orElse(null);
                    if (launcher != null) {
                        // 如果有啟動成功, 則將上一次重啟的日期時間設置為null
                        launcher.setLatestCmdStart(null);
                    }
                }
            }
        }
        // 間隔一段時間後, 再取一次, 用來計算CPU使用率
        if (needRefresh) {
            Util.sleep(this.monitorSchedulerJobConfig.getProcessCpuRefreshInterval());
            fetchAllProcess.apply(true); // 刷新process列表, 以便後面計算CPU使用率
            for (Sms sms : smsProcessList) {
                String processName = sms.getSmsServicename();
                OSProcess beforeOSProcess = existProcessMap.get(processName);
                // 如果上一次沒有取到, 則跳過
                if (beforeOSProcess == null) {
                    continue;
                }
                OSProcess afterOSProcess = this.fetchProcess(processName, fetchAllProcess);
                if (afterOSProcess != null) {
                    sms.setSmsCpu((int) (10000L * ((afterOSProcess.getKernelTime() - beforeOSProcess.getKernelTime()) + (afterOSProcess.getUserTime() - beforeOSProcess.getUserTime())) / (afterOSProcess.getUpTime() - beforeOSProcess.getUpTime())));
                    sms.setSmsRam((int) (afterOSProcess.getResidentSetSize() / 1024));
                    sms.setSmsThreads(afterOSProcess.getThreadCount());
                }
            }
        }
        fetchAllProcess.apply(true); // 清空process列表, 讓gc回收
        Sms processSms = createSms(SERVICE_NAME_PROCESS, hostIp, hostName, null);
        processSms.setSmsServicestate(smsProcessList.stream().filter(t -> "1".equals(t.getSmsServicestate())).count() == smsProcessList.size() ? "1" : "0");
        try {
            // 注意這裡要用GsonDateParser產出json字串, 因為AIX下解讀Date字串有問題, 所以GsonDateParser中會特別處理Date類型的欄位, 轉為字串處理
            processSms.setSmsOthers(new GsonDateParser<List<Sms>>(new TypeToken<List<Sms>>() {}.getType()).writeOut(smsProcessList));
        } catch (Exception e) {
            TRACELogger.warn("parse smsProcessList to json string with exception occur, ", e.getMessage());
        }
        TRACELogger.debug("[", ProgramName, ".fetchProcess]write process data, processSms:", processSms);
        if (smsExtMapper.selectByPrimaryKey(SERVICE_NAME_PROCESS, hostIp) == null) {
            smsExtMapper.insert(processSms);
        } else {
            smsExtMapper.updateByPrimaryKeyWithBLOBs(processSms);
        }
    }

    /**
     * 依據processName取process
     *
     * @param processName
     * @param fetchOsProcess
     * @return
     */
    private OSProcess fetchProcess(String processName, Function<Boolean, List<OSProcess>> fetchOsProcess) {
        // 取到的process物件
        OSProcess osProcess = null;
        // 優先依據pid取
        Integer pid = this.processNameToPidMap.get(processName);
        if (pid != null) {
            osProcess = OperationSystemDataCollector.getOs().getProcess(pid);
            if (osProcess != null) {
                if (processName.equalsIgnoreCase(osProcess.getName())) {
                    TRACELogger.debug("[", ProgramName, ".fetchProcess]found process by pid:", pid, ", name:", processName);
                } else {
                    TRACELogger.warn("[", ProgramName, ".fetchProcess]found process by pid:", pid, ", but process name is:", osProcess.getName(), " which is not equal to", processName);
                    osProcess = null; // 重新設為null
                }
            } else {
                TRACELogger.warn("[", ProgramName, ".fetchProcess]cannot found process by pid:", pid, ", name:", processName);
            }
        }
        // 如果取不到, 則取出所有的process, 然後再依據名字filter
        if (osProcess == null) {
            List<OSProcess> osProcesses = fetchOsProcess.apply(false);
            osProcess = osProcesses.stream().filter(t -> t.getName().equalsIgnoreCase(processName)).findFirst().orElse(null);
            if (osProcess != null) {
                TRACELogger.debug("[", ProgramName, ".fetchProcess]found process from all filter by name:", processName);
            } else {
                TRACELogger.warn("[", ProgramName, ".fetchProcess]still cannot found process, name:", processName);
                TRACELogger.warn("[", ProgramName, ".fetchProcess]current process name List:[", StringUtils.join(osProcesses.stream().map(OSProcess::getName).collect(Collectors.toList()), ","), "]");
            }
        }
        // // 如果pid取不到, 則取出所有的process, 然後依據名字filter
        // if (osProcess == null) {
        //     // 先按照指定的名字取
        //     List<OSProcess> osProcesses = OperationSystemDataCollector.getOs().getProcesses(t -> t.getName().equalsIgnoreCase(processName), null, 0);
        //     if (CollectionUtils.isNotEmpty(osProcesses)) {
        //         osProcess = osProcesses.get(0); // 取第一個
        //         TRACELogger.debug("[", ProgramName, ".fetchProcess]found process by name:", processName);
        //     } else {
        //         TRACELogger.warn("[", ProgramName, ".fetchProcess]still cannot found process by name:", processName);
        //     }
        // }
        return osProcess;
    }

    private void notifyMonitorServerLauncher(MonitorServerInfo serverInfo) {
        if (CollectionUtils.isNotEmpty(this.monitorSchedulerJobConfig.getLaunchers())) {
            MonitorServerLauncherInfo launcher =
                    this.monitorSchedulerJobConfig.getLaunchers().stream().filter(
                            t -> t.getName().equalsIgnoreCase(serverInfo.getName()) && t.getHostip().equalsIgnoreCase(serverInfo.getHostip())).findFirst().orElse(null);
            if (launcher == null) {
                TRACELogger.warn("Cannot find launcher for name = [", serverInfo.getName(), "], hostIp = [", serverInfo.getHostip(), "]");
            } else if (launcher.getLatestCmdStart() != null) {
                TRACELogger.warn("Already restart name = [", serverInfo.getName(), "], hostIp = [", serverInfo.getHostip(), "] at [", FormatUtil.dateTimeInMillisFormat(launcher.getLatestCmdStart().getTime()), "]");
            } else {
                this.doLauncher(launcher);
            }
        }
    }

    private void notifyMonitorProcessLauncher(String processName) {
        if (CollectionUtils.isNotEmpty(this.monitorSchedulerJobConfig.getLaunchers())) {
            MonitorServerLauncherInfo launcher =
                    this.monitorSchedulerJobConfig.getLaunchers().stream().filter(t -> t.getName().equalsIgnoreCase(processName)).findFirst().orElse(null);
            if (launcher == null) {
                TRACELogger.warn("Cannot find launcher for processName = [", processName, "]");
            } else if (launcher.getLatestCmdStart() != null) {
                TRACELogger.warn("Already restart name = [", processName, "] at [", FormatUtil.dateTimeInMillisFormat(launcher.getLatestCmdStart().getTime()), "]");
            } else {
                this.doLauncher(launcher);
            }
        }
    }

    private void doLauncher(MonitorServerLauncherInfo launcher) {
        if (StringUtils.isNotBlank(launcher.getCmdStart())) {
            TRACELogger.info("Start to execute command = [", launcher.getCmdStart(), "]...");
            // ProcessBuilder processBuilder = new ProcessBuilder().command(launcher.getCmdStart().split("\\s+"));
            // processBuilder.redirectErrorStream(true);
            // 2024-08-20 Richard modified for Command Injection
            Process process = null;
            try {
                // Process process = processBuilder.start();
                // process = Runtime.getRuntime().exec(launcher.getCmdStart().split("\\s+"));
                process = CommandLineUtil.getProcess(launcher.getCmdStart().split("\\s+")); // 2024-08-29 Richard modified for Command Injection
                Consumer<String> consumer = launcher.isPrintInputStream() ? TRACELogger::debug : null;
                StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), consumer);
                new Thread(streamGobbler).start();
                TRACELogger.info("Execute successful, command = [", launcher.getCmdStart(), "]");
            } catch (Throwable e) {
                TRACELogger.exceptionMsg(e, "Execute command = [", launcher.getCmdStart(), "] with exception occur, ", e.getMessage());
            }
        } else if (StringUtils.isNotBlank(launcher.getHttpStart())) {
            TRACELogger.info("Start to do http post, url = [", launcher.getHttpStart(), "]...");
            try {
                Map<String, String> args = new HashMap<>();
                args.put("operator", appName.toUpperCase());
                String response = httpClient2.postForObject(launcher.getHttpStart(), MediaType.APPLICATION_FORM_URLENCODED, args, String.class);
                if (Const.REPLY_OK.equals(response)) {
                    TRACELogger.info("Do http post successful, url = [", launcher.getHttpStart(), "]");
                } else {
                    TRACELogger.error("Do http post url = [", launcher.getHttpStart(), "] failed, ", response);
                }
            } catch (Throwable e) {
                TRACELogger.exceptionMsg(e, "Do http post url = [", launcher.getHttpStart(), "] with exception occur, ", e.getMessage());
            }
        }
    }

    /**
     * 獲取本機的suip和HSM的狀態
     */
    private void fetchLocalSuipHsmMonitorData() {
        TRACELogger.info("enter fetchLocalSuipHsmMonitorData");
        this.fetchSuipHsmMonitorData(this.monitorSchedulerJobConfig.getLocalSuip(), this.suipHsmMonitorData.localSuip, this.suipHsmMonitorData.localHsm);
        TRACELogger.info("exit fetchLocalSuipHsmMonitorData");
    }

    /**
     * 獲取遠程機器上的suip和HSM的狀態
     */
    public void fetchRemoteSuipHsmMonitorData() {
        TRACELogger.info("enter fetchRemoteSuipHsmMonitorData");
        this.fetchSuipHsmMonitorData(this.monitorSchedulerJobConfig.getRemoteSuip(), this.suipHsmMonitorData.remoteSuip, this.suipHsmMonitorData.remoteHsm);
        TRACELogger.info("exit fetchRemoteSuipHsmMonitorData");
    }

    /**
     * @param suipConnectionInfos
     * @param suipMonitorDataList suip的監控數據
     * @param hsmMonitorDataList  HSM的監控數據
     */
    private void fetchSuipHsmMonitorData(List<MonitorSuipConnectionInfo> suipConnectionInfos, List<ClientNetworkStatus> suipMonitorDataList, List<ServerNetworkStatus> hsmMonitorDataList) {
        // suip的監控數據
        List<ClientNetworkStatus> clientNetworkStatusList = new ArrayList<>();
        // HSM的監控數據
        List<ServerNetworkStatus> serverNetworkStatusList = new ArrayList<>();
        Set<String> filteredIp = new HashSet<>();
        int timeout = -1;
        String messageIn = null, body = null;
        String[] section = null, each = null;
        if (CollectionUtils.isNotEmpty(suipConnectionInfos)) {
            // MonitorSuipConnectClientConfiguration configuration = SpringBeanFactoryUtil.registerBean(MonitorSuipConnectClientConfiguration.class);
            // MonitorSuipConnectClient suipConnectClient = SpringBeanFactoryUtil.registerBean(MonitorSuipConnectClient.class);
            for (MonitorSuipConnectionInfo info : suipConnectionInfos) {
                timeout = info.getTimeout() < 0 ? this.monitorSchedulerJobConfig.getSuipTimeout() * 1000 : (int) info.getTimeout();
                // prepare data
                ClientNetworkStatus base = new ClientNetworkStatus();
                base.setType(MONITOR_TYPE_SUIP_NET_CLIENT); // 這裡一定要設定Type, 以便後面MonitorNetworkController在進行merge時有針對性的處理
                base.setIdentity(info.getName());
                base.setServiceHostName(StringUtils.isNotBlank(info.getHostname()) ? info.getHostname() : hostName);
                base.setServiceIP(StringUtils.isNotBlank(info.getHostip()) ? info.getHostip() : hostIp);
                base.setServiceName(info.getName());
                base.setLocalEndPoint(StringUtils.EMPTY);
                base.setRemoteEndPoint(StringUtils.EMPTY);
                base.setSocketCount("0");
                base.setState(NET_CLIENT_STATE_DISCONNECT);
                base.setServiceState("0");
                try {
                    // fetch via Socket
                    // configuration.setHost(info.getHostip());
                    // configuration.setPort(Integer.parseInt(info.getPort()));
                    // receive
                    // messageIn = suipConnectClient.establishConnectionAndSendReceive(configuration, info.getCmd(), timeout);
                    messageIn = SocketUtil.sendReceive(info.getHostip(), Integer.parseInt(info.getPort()), info.getCmd(), timeout);
                    if (StringUtils.isNotBlank(messageIn)) {
                        // substring(24, 2)=00代表回應成功
                        if ("00".equals(messageIn.substring(24, 24 + 2))) {
                            // 取substring 32開始至00前為止
                            // 轉成ASCII後結果如 ID=1,IpADDR=127.0.0.1,port=1500,Status=1;
                            body = StringUtil.fromHex(messageIn.substring(32, messageIn.indexOf("00", 32)));
                            TRACELogger.info("Parse response message from HSM succeed, body = [", body, "]");
                            // 1個suip可以連多台HSM, 所以;分隔不同台HSM, IpADDR及port則是該HSM的IP與Port, Status=1代表連線中, 0代表斷線
                            section = body.split(";");
                            if (ArrayUtils.isNotEmpty(section)) {
                                for (int i = 0; i < section.length; i++) {
                                    if (StringUtils.isNotBlank(section[i])) {
                                        each = section[i].split(",");
                                        if (ArrayUtils.isNotEmpty(each)) {
                                            ClientNetworkStatus clientNetworkStatus = (ClientNetworkStatus) BeanUtils.cloneBean(base);
                                            ServerNetworkStatus serverNetworkStatus = new ServerNetworkStatus();
                                            serverNetworkStatus.setServiceHostName(StringUtils.isNotBlank(info.getHostname()) ? info.getHostname() : hostName);
                                            serverNetworkStatus.setServiceState("0");
                                            serverNetworkStatus.setUpdateDateTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
                                            String remoteIp = null, remotePort = null;
                                            for (int j = 0; j < each.length; j++) {
                                                // ID
                                                if (StringUtils.startsWithIgnoreCase(each[j], SUIP_RESP_FIELD_ID)) {
                                                    clientNetworkStatus.setIdentity(StringUtils.join(clientNetworkStatus.getIdentity(), "-", each[j].substring(SUIP_RESP_FIELD_ID.length())));
                                                }
                                                // Remote IP
                                                else if (StringUtils.startsWithIgnoreCase(each[j], SUIP_RESP_FIELD_IPADDR)) {
                                                    remoteIp = each[j].substring(SUIP_RESP_FIELD_IPADDR.length());
                                                }
                                                // Remote Port
                                                else if (StringUtils.startsWithIgnoreCase(each[j], SUIP_RESP_FIELD_PORT)) {
                                                    remotePort = each[j].substring(SUIP_RESP_FIELD_PORT.length());
                                                }
                                                // Status
                                                else if (StringUtils.startsWithIgnoreCase(each[j], SUIP_RESP_FIELD_STATUS)) {
                                                    if ("1".equals(each[j].substring(SUIP_RESP_FIELD_STATUS.length()))) {
                                                        clientNetworkStatus.setState(NET_CLIENT_STATE_CONNECT);
                                                        clientNetworkStatus.setServiceState("1");
                                                        clientNetworkStatus.setSocketCount("1");
                                                        serverNetworkStatus.setServiceState("1");
                                                    }
                                                }
                                            }
                                            if (StringUtils.isNotBlank(remoteIp) && StringUtils.isNotBlank(remotePort)) {
                                                clientNetworkStatus.setRemoteEndPoint(StringUtils.join(remoteIp, ":", remotePort));
                                                serverNetworkStatus.setServiceIP(remoteIp);
                                                serverNetworkStatus.setServicePort(remotePort);
                                                serverNetworkStatus.setServiceName(StringUtils.join("HSM-", remoteIp));
                                            }
                                            clientNetworkStatusList.add(clientNetworkStatus);
                                            // 過濾掉重複的IP
                                            if (!filteredIp.contains(serverNetworkStatus.getServiceIP())) {
                                                filteredIp.add(serverNetworkStatus.getServiceIP());
                                                // 根據IP依據配置檔重新塞名字
                                                MonitorHSMInfo hsmInfo = this.monitorSchedulerJobConfig.getHsm().stream().filter(t -> t.getIp().equals(serverNetworkStatus.getServiceIP())).findFirst().orElse(null);
                                                if (hsmInfo != null) serverNetworkStatus.setServiceName(hsmInfo.getName());
                                                serverNetworkStatusList.add(serverNetworkStatus);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    TRACELogger.warn("fetchSuipHsmMonitorData suip = [", info.getHostip(), ":", info.getPort(), "], timeout = [", timeout, "] with exception occur, ", e.getMessage());
                    // 如果從suip取不到, 則增加base資料, 否則監控畫面上就缺少這筆資料
                    clientNetworkStatusList.add(base);
                }
            }
        }
        // 將suip監控的數據, 加入到List中
        suipMonitorDataList.clear();
        suipMonitorDataList.addAll(clientNetworkStatusList);
        // 將suip監控數據寫入SMS檔
        MonitorNetworkController controller = SpringBeanFactoryUtil.getBean(MonitorNetworkController.class, false);
        if (controller != null) {
            controller.sendReceiveNetClient(this.suipHsmMonitorData.getSuipMonitorClientNetworkStatus(), false);
        }
        // 將HSM監控的數據, 加入到List中, 以供MonitorDumpJob使用
        hsmMonitorDataList.clear();
        hsmMonitorDataList.addAll(serverNetworkStatusList);
    }

    public List<ServerNetworkStatus> getHsmMonitorDataList() {
        return this.suipHsmMonitorData.getHsmMonitorServerNetworkStatus();
    }

    public void clearRemoteSuipHsmMonitorData() {
        this.suipHsmMonitorData.remoteSuip.clear();
        this.suipHsmMonitorData.remoteHsm.clear();
    }

    private class SuipHsmMonitorData {
        public final List<ClientNetworkStatus> localSuip = Collections.synchronizedList(new ArrayList<>());
        public final List<ClientNetworkStatus> remoteSuip = Collections.synchronizedList(new ArrayList<>());
        public final List<ServerNetworkStatus> localHsm = Collections.synchronizedList(new ArrayList<>());
        public final List<ServerNetworkStatus> remoteHsm = Collections.synchronizedList(new ArrayList<>());

        public List<ClientNetworkStatus> getSuipMonitorClientNetworkStatus() {
            List<ClientNetworkStatus> clientNetworkStatusList = new ArrayList<>();
            clientNetworkStatusList.addAll(localSuip);
            clientNetworkStatusList.addAll(remoteSuip);
            return clientNetworkStatusList;
        }

        public List<ServerNetworkStatus> getHsmMonitorServerNetworkStatus() {
            List<ServerNetworkStatus> serverNetworkStatusList = new ArrayList<>();
            serverNetworkStatusList.addAll(localHsm);
            serverNetworkStatusList.addAll(remoteHsm);
            return serverNetworkStatusList;
        }
    }

    public void removeAlertKeyForService(Sms sms) {
        String alertKey = StringUtils.join(Arrays.asList(sms.getSmsServiceip(), sms.getSmsServicename()), '@');
        // 2025-03-07 Richard add如果不提醒, 則這裡要重置alert中的資料
        if (this.alertMap.remove(alertKey) != null)
            TRACELogger.debug("[REMOVE_ALERT_KEY]alertKey:", alertKey);
    }

    private void sendSms(String message) {
        if (!monitorSchedulerJobConfig.isSendSms()) return;
        // 2025-09-03 Richard modified 簡訊改用三竹
        // HiairSmsOperator smsOperator = SpringBeanFactoryUtil.getBean(HiairSmsOperator.class, false);
        MitakeSmsOperator smsOperator = SpringBeanFactoryUtil.getBean(MitakeSmsOperator.class, false);
        if (smsOperator != null) {
            String[] smsTelList = monitorSchedulerJobConfig.getSmsTelList();
            TRACELogger.debug("[sendSms]Begin Send SMS to [", StringUtils.join(smsTelList, ','), "],message:", message);
            smsOperator.send(Arrays.asList(smsTelList), message, true);
            TRACELogger.info("[sendSms]Send SMS finished");
        }
    }

    public void analyseSystemData(Sms system) {
        List<MonitorServerInfo> othersSystems = this.monitorSchedulerJobConfig.getOthersSystems();
        if (CollectionUtils.isNotEmpty(othersSystems)) {
            MonitorServerInfo othersSystem = othersSystems.stream().filter(t -> t.getHostip().equals(system.getSmsServiceip())).findFirst().orElse(null);
            if (othersSystem != null) {
                MonitorThreshold monitorThreshold = othersSystem.getThreshold();
                // RAM MB
                int value = (system.getSmsRam() == null ? 0 : system.getSmsRam()) / 1024;
                int threshold = monitorThreshold == null ? 0 : monitorThreshold.getRamThreshold();
                boolean alert = threshold > 0 && value > threshold;
                TRACELogger.debug("Others SYSTEM[", system.getSmsHostname(), "-", system.getSmsServiceip(), "][RamThreshold]value:", value, ",threshold:", threshold, ",threshold>0&&value>threshold:", alert);
                this.sendSystemAlert(system, monitorThreshold, alert, "RamThreshold", StringUtils.join("使用的記憶體為[", FormatUtil.longFormat(value), "]MB, 已超過警示值[", FormatUtil.longFormat(threshold), "]MB"));
                // CpuThreshold
                value = (system.getSmsCpu() == null ? 0 : system.getSmsCpu()) / 100;
                threshold = monitorThreshold == null ? 0 : monitorThreshold.getCpuThreshold();
                alert = threshold > 0 && value > threshold;
                TRACELogger.debug("Others SYSTEM[", system.getSmsHostname(), "-", system.getSmsServiceip(), "][CpuThreshold]value:", value, ",threshold:", threshold, ",threshold>0&&value>threshold:", alert);
                this.sendSystemAlert(system, monitorThreshold, alert, "CpuThreshold", StringUtils.join("使用的CPU%為[", value, "]%, 已超過警示值[", threshold, "]%"));
                // 更新threshold相關值
                if (monitorThreshold != null && monitorThreshold.isNotification()) {
                    Sms record = new Sms();
                    record.setSmsServiceip(system.getSmsServiceip());
                    record.setSmsServicename(system.getSmsServicename());
                    record.setSmsCpuThreshold(monitorThreshold.getCpuThreshold());
                    record.setSmsRamThreshold(monitorThreshold.getRamThreshold());
                    record.setSmsThreadsThreshold(monitorThreshold.getThreadThreshold());
                    try {
                        smsExtMapper.updateThresholdByPrimaryKey(record);
                    } catch (Exception e) {
                        TRACELogger.warn("[analyseSystemData]updateThresholdByPrimaryKey with exception occur, ", e.getMessage());
                    }
                }
            }
        }
    }
}
