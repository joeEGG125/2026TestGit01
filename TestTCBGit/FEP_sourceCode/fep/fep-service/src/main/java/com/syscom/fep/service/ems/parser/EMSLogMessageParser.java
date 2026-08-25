package com.syscom.fep.service.ems.parser;

import com.google.gson.Gson;
import com.syscom.fep.base.FEPBaseMethod;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.mail.MailSender;
import com.syscom.fep.common.notify.NotifyHelper;
import com.syscom.fep.common.notify.NotifyHelperConstant;
import com.syscom.fep.common.notify.NotifyHelperTemplateId;
import com.syscom.fep.common.sms.hiair.HiairSmsOperator;
import com.syscom.fep.common.sms.mitake.MitakeSmsOperator;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.configuration.util.FEPNotifyUtil;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.mail.MailData;
import com.syscom.fep.frmcommon.mail.MailPriority;
import com.syscom.fep.frmcommon.scheduler.AbstractScheduledTask;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.deslog.dao.DeslogDao;
import com.syscom.fep.mybatis.deslog.model.Deslog;
import com.syscom.fep.mybatis.ems.dao.FeplogDao;
import com.syscom.fep.mybatis.ems.ext.model.FeplogExt;
import com.syscom.fep.mybatis.ems.model.Feplog;
import com.syscom.fep.mybatis.model.Alert;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.mybatis.model.Msgkb;
import com.syscom.fep.service.ems.configurer.EMSServiceConfiguration;
import com.syscom.fep.service.ems.vo.*;
import com.syscom.fep.service.ems.vo.EMSLogMessage.MDC;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EMSLogMessageParser extends FEPBaseMethod {
    private static final LogHelper SERVICELOGGER = LogHelperFactory.getServiceLogger();
    // @Autowired
    // private AlertExtMapper alertExtMapper;
    // @Autowired
    // private MsgkbExtMapper msgkbExtMapper;
    // @Autowired
    // private MsgctlExtMapper msgctlExtMapper;
    // 2025-09-03 Richard modified不再使用notify送mail, 改用FEP自己送
    // @Autowired
    // private NotifyHelper notifyHelper;
    @Qualifier("feplogDao")
    @Autowired
    private FeplogDao feplogDao;
    @Autowired
    private DeslogDao deslogDao;
    private EMSServiceConfiguration emsServiceConfiguration;
    private final ConcurrentHashMap<String, EMSErrData> errDic = new ConcurrentHashMap<>();
    // 2025-01-14 定時清理錯誤統計資料
    private final AbstractScheduledTask resetErrorCounterTask = new AbstractScheduledTask("ResetErrorCounterTask") {
        @Override
        public void execute() {
            resetErrorCounter();
        }
    };
    @Qualifier(DataSourceConstant.BEAN_NAME_JDBC_TEMPLATE)
    @Autowired
    private JdbcTemplate fepJdbcTemplate;

    @PostConstruct
    public void initParser() {
        this.emsServiceConfiguration = SpringBeanFactoryUtil.registerBean(EMSServiceConfiguration.class);
        resetErrorCounterTask.scheduleAtFixedRate(0, this.emsServiceConfiguration.getResetNotifyInterval(), TimeUnit.MINUTES);
    }

    @PreDestroy
    public void destroy() {
        resetErrorCounterTask.destroy();
    }

    public void parseLog(List<EMSLogMessage> emsLogMessageList) throws Exception {
        if (CollectionUtils.isNotEmpty(emsLogMessageList)) {
            List<EMSLogMessage> logFepMessageList = new ArrayList<>();
            List<EMSLogMessage> logHisMessageList = new ArrayList<>();
            List<EMSLogMessage> logDesMessageList = new ArrayList<>();
            List<EMSLogMessage> alertMessageList = new ArrayList<>();
            try {
                for (EMSLogMessage emsLogMessage : emsLogMessageList) {
                    // 排除一些不需要寫入FEPLOG的message
                    if (this.doExclude(emsLogMessage)) {
                        continue;
                    }
                    if (EMSLogMessageType.log.name().equals(emsLogMessage.getMessageType())) {
                        if (EMSLogMessageTarget.fep.name().equals(emsLogMessage.getMessageTarget())) {
                            logFepMessageList.add(emsLogMessage);
                        } else if (EMSLogMessageTarget.his.name().equals(emsLogMessage.getMessageTarget())) {
                            logHisMessageList.add(emsLogMessage);
                        } else if (EMSLogMessageTarget.des.name().equals(emsLogMessage.getMessageTarget())) {
                            logDesMessageList.add(emsLogMessage);
                        }
                    } else if (EMSLogMessageType.alert.name().equals(emsLogMessage.getMessageType())) {
                        alertMessageList.add(emsLogMessage);
                    }
                }
                this.parseLog(logFepMessageList, false);
                this.parseLog(logHisMessageList, true);
                this.parseDesLog(logDesMessageList);
                this.parseAlert(alertMessageList);
            } finally {
                logFepMessageList.clear();
                logHisMessageList.clear();
                logDesMessageList.clear();
                alertMessageList.clear();
                emsLogMessageList.clear();
            }
        }
    }

    /**
     * 排除一些不需要寫入FEPLOG的message
     *
     * @param emsLogMessage
     * @return
     */
    private boolean doExclude(EMSLogMessage emsLogMessage) {
        return CollectionUtils.isNotEmpty(emsServiceConfiguration.getExcludeMessageIdList()) &&
                emsServiceConfiguration.getExcludeMessageIdList().stream().filter(StringUtils::isBlank).count() != emsServiceConfiguration.getExcludeMessageIdList().size() &&
                emsServiceConfiguration.getExcludeMessageIdList().stream().filter(t -> t.equalsIgnoreCase(emsLogMessage.getMdc().getMessageID())).findFirst().orElse(null) != null;
    }

    private void parseLog(List<EMSLogMessage> emsLogMessageList, boolean isHistory) {
        if (CollectionUtils.isEmpty(emsLogMessageList)) {
            return;
        }
        try {
            List<Feplog> recordList = new ArrayList<>();
            for (EMSLogMessage emsLogMessage : emsLogMessageList) {
                if (emsLogMessage.getMdc() == null) {
                    continue;
                }
                MDC mdc = emsLogMessage.getMdc();
                if (StringUtils.isNotBlank(mdc.getTxDate()) && !"2".equals(mdc.getTxDate().substring(0, 1))) {
                    mdc.setTxDate(CalendarUtil.rocStringToADString(mdc.getTxDate()));
                }
                if (StringUtils.isNotBlank(mdc.getChannel()) && FEPChannel.NETBANK.name().equals(mdc.getChannel().trim())) {
                    if (StringUtils.isNotBlank(mdc.getTrinActno()) && mdc.getTrinActno().length() > 16) {
                        mdc.setTrinActno(mdc.getTrinActno().substring(0, 16));
                    }
                    if (StringUtils.isNotBlank(mdc.getTroutActno()) && mdc.getTroutActno().length() > 16) {
                        mdc.setTroutActno(mdc.getTroutActno().substring(0, 16));
                    }
                }
                Feplog record = new FeplogExt();
                record.setAtmno(mdc.getATMNo());
                record.setAtmseq(mdc.getATMSeq());
                record.setBkno(mdc.getBkno());
                record.setChannel(mdc.getChannel());
                try {
                    record.setEj(Long.parseLong(mdc.getEj()));
                } catch (Exception e) {
                    SERVICELOGGER.warn(e, "Parse EJ:", mdc.getEj(), " failed. ", e.getMessage());
                    continue; // 如果有異常表示記錄的log內容不正確, 則跳過, 避免後面寫入FEPLOG出現異常
                }
                try {
                    record.setLogdate(FormatUtil.parseDataTime(mdc.getLogDate(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
                } catch (Exception e) {
                    SERVICELOGGER.warn(e, "Parse LogDate:", mdc.getLogDate(), " failed. ", e.getMessage());
                    continue; // 如果有異常表示記錄的log內容不正確, 則跳過, 避免後面寫入FEPLOG出現異常
                }
                record.setMessageflow(mdc.getMessageFlow());
                record.setMessageid(mdc.getMessageID());
                record.setProgramflow(mdc.getProgramFlow());
                record.setProgramname(mdc.getProgramName());
                record.setRemark(emsLogMessage.getMessage());
                record.setStan(mdc.getStan());
                try {
                    record.setSteps(Long.parseLong(mdc.getStep()));
                } catch (Exception e) {
                    SERVICELOGGER.warn(e, "Parse Steps:", mdc.getStep(), " failed. ", e.getMessage());
                    continue; // 如果有異常表示記錄的log內容不正確, 則跳過, 避免後面寫入FEPLOG出現異常
                }
                record.setThreadid(emsLogMessage.getThread());
                record.setTrinactno(mdc.getTrinActno());
                record.setTrinbank(mdc.getTrinBank());
                record.setTroutactno(mdc.getTroutActno());
                record.setTroutbank(mdc.getTroutBank());
                record.setTxdate(mdc.getTxDate());
                record.setTxmessage(mdc.getTxMessage());
                record.setTxrquid(mdc.getTxRquid());
                record.setHostname(mdc.getHostname());
                recordList.add(record);
            }
            // 批量新增到db中
            // 2024-09-13 Richard modified feplogDao新增insertBatchEMS方法, 方法內部會根據每筆的Feplog.Logdate來決定要塞入哪個FEPLOG檔
            try {
                // feplogDao.insertBatchEMS(recordList);
                feplogDao.jdbcInsert(recordList);
            } catch (Exception e) {
                SERVICELOGGER.error(e, "jdbcInsert fail. ", e.getMessage());
            } finally {
                recordList.clear();
            }
        } catch (Exception e) {
            SERVICELOGGER.error(e, "parseLog fail. ", e.getMessage());
        }
    }

    private void parseDesLog(List<EMSLogMessage> logDesMessageList) {
        if (CollectionUtils.isEmpty(logDesMessageList)) {
            return;
        }
        List<Deslog> recordList = new ArrayList<>();
        try {
            for (EMSLogMessage emsLogMessage : logDesMessageList) {
                if (emsLogMessage.getMdc() == null) {
                    continue;
                }
                MDC mdc = emsLogMessage.getMdc();
                Deslog record = new Deslog();
                record.setDeslogCallobject(mdc.getCallObj());
                try {
                    record.setDeslogEj(Integer.parseInt(mdc.getEj()));
                } catch (NumberFormatException e) {
                    SERVICELOGGER.warn(e, "Parse EJ:", mdc.getEj(), " failed. ", e.getMessage());
                }
                record.setDeslogFunc(mdc.getFuncNo());
                record.setDeslogInputdata1(mdc.getInput1());
                record.setDeslogInputdata2(mdc.getInput2());
                record.setDeslogKeyid(mdc.getKeyId());
                record.setDeslogOutputdata1(mdc.getOutput1());
                record.setDeslogOutputdata2(mdc.getOutput2());
                record.setDeslogProgramflow(mdc.getProgramFlow());
                record.setDeslogProgramname(mdc.getProgramName());
                record.setDeslogRc(mdc.getRC());
                record.setDeslogRemark(emsLogMessage.getMessage());
                record.setDeslogSuipcommand(mdc.getSuipCommand());
                try {
                    record.setDeslogUpdatetime(FormatUtil.parseDataTime(mdc.getLogDate(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
                } catch (ParseException e) {
                    SERVICELOGGER.warn(e, "Parse LogDate:", mdc.getLogDate(), " failed. ", e.getMessage());
                }
                record.setTxrquid(mdc.getTxRquid());
                record.setHostname(mdc.getHostname());
                // 2024-09-13 Richard add 這裡根據上面塞入的DeslogUpdatetime(也就是mdc.getLogDate())來決定要塞入哪個ENCLOG檔
                record.setTableNameSuffix(String.valueOf(CalendarUtil.getDayOfWeek(record.getDeslogUpdatetime() == null ? Calendar.getInstance() : CalendarUtil.clone(record.getDeslogUpdatetime()))));
                recordList.add(record);
            }
            // 新增到db中
            // deslogDao.insertBatch(recordList);
            deslogDao.jdbcInsert(recordList);
        } catch (Exception e) {
            SERVICELOGGER.error(e, "parseDesLog fail. ", e.getMessage());
        } finally {
            recordList.clear();
        }
    }

    private void parseAlert(List<EMSLogMessage> alertMessageList) {
        if (CollectionUtils.isEmpty(alertMessageList)) {
            return;
        }
        try {
            for (EMSLogMessage emsLogMessage : alertMessageList) {
                if (emsLogMessage.getMdc() == null) {
                    continue;
                }
                insertEMSAR(emsLogMessage);
            }
        } finally {
            alertMessageList.clear();
        }
    }

    private void insertEMSAR(EMSLogMessage emsLogMessage) {
        try {
            Date ar_date = FormatUtil.parseDataTime(emsLogMessage.getMdc().getLogDate(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS);
            String erPkey = StringUtils.join(new String[] {emsLogMessage.getMdc().getHostname(), emsLogMessage.getMdc().getErrcode(), emsLogMessage.getMdc().getErrdescription()}, "#");
            // 該事件在3分鐘內出現的筆數
            EMSErrData lastErr = errDic.get(erPkey);
            if (lastErr != null) {
                lastErr.setEventData(emsLogMessage);
                lastErr.accumulateCount();
                // 之前有發生過此error, 判斷最後一筆的時間是否在3分鐘內
                if ((Calendar.getInstance().getTimeInMillis() - lastErr.getLastNotifyTime().getTimeInMillis()) < (long) emsServiceConfiguration.getRepeatInterval() * 60 * 1000) {
                    SERVICELOGGER.warn("[Repeated Alert Message][", erPkey, "][", lastErr.getCount(), "][", FormatUtil.dateTimeInMillisFormat(lastErr.getLastNotifyTime().getTimeInMillis()), "]", new Gson().toJson(emsLogMessage));
                    return;
                }
            }
            // 之前沒發生過此error, 從DB再判斷3分鐘內是否有發生
            else {
                lastErr = new EMSErrData();
                lastErr.setCount(1);
                lastErr.setEventData(emsLogMessage);
                lastErr.setLastNotifyTime(Calendar.getInstance());
                errDic.put(erPkey, lastErr);
                Alert alert = new Alert();
                alert.setArErcode(emsLogMessage.getMdc().getErrcode());
                alert.setArHostname(emsLogMessage.getMdc().getHostname());
                alert.setArErdescription(emsLogMessage.getMdc().getErrdescription());
                alert.setArDatetime(DateUtils.addMinutes(ar_date, -1 * emsServiceConfiguration.getRepeatInterval()));
                alert.setArSubsys(emsLogMessage.getMdc().getSubsys());
                // int iRtn = alertExtMapper.getAlertCounts(alert);
                int iRtn = this.getAlertCounts(alert);
                if (iRtn > 0) {
                    lastErr.accumulateCount(iRtn);
                    SERVICELOGGER.warn("[Repeated Alert Message][", erPkey, "][", lastErr.getCount(), "][", FormatUtil.dateTimeInMillisFormat(lastErr.getLastNotifyTime().getTimeInMillis()), "]", new Gson().toJson(emsLogMessage));
                    return;
                }
            }
            EMSApMessageData apData = this.parseAPMessage(emsLogMessage);
            Alert para = new Alert();
            para.setArDatetime(ar_date);
            para.setArHostname(emsLogMessage.getMdc().getHostname());
            para.setArHostip(emsLogMessage.getMdc().getHostip());
            para.setArLevel(emsLogMessage.getLevel());
            para.setArApplication(emsLogMessage.getMdc().getMessageGroup());
            para.setArLine(emsLogMessage.getMdc().getLinenumber());
            para.setArSys(emsLogMessage.getMdc().getSys());
            para.setArSubsys(emsLogMessage.getMdc().getSubsys());
            para.setArErcode(emsLogMessage.getMdc().getErrcode());
            para.setArErdescription(emsLogMessage.getMdc().getErrdescription());
            para.setArEj(apData.getEj());
            para.setArMessage(emsLogMessage.getMessage());
            para.setAtmno(apData.getAtmNo());
            // try {
            //     alertExtMapper.insertSelective(para);
            // } catch (Exception e) {
            //     SERVICELOGGER.error(e, "insertSelective Alert fail. ", e.getMessage());
            // }
            this.insertSelective(para);
            if (apData.isNotification()) {
                this.sendMail(apData, lastErr);
                this.sendSms(apData, lastErr);
            }
        } catch (Exception e) {
            SERVICELOGGER.error(e, "insertEMSAR fail. ", e.getMessage());
        }
    }

    private EMSApMessageData parseAPMessage(EMSLogMessage emsLogMessage) throws Exception {
        String body = emsLogMessage.getMessage();
        // 2025-05-20 Richard modified 這裡不用XML API解讀, 直接用String.subString來取節點的值, 以解決性能問題
        // Element root = XmlUtil.load("<EMS>" + body + "</EMS>");
        String root = StringUtils.join("<EMS>", body, "</EMS>");
        EMSApMessageData data = new EMSApMessageData();
        data.setTxErrDesc(XmlUtil.getChildElementValue(root, "TxErrDesc", StringUtils.EMPTY));
        data.setExSubCode(XmlUtil.getChildElementValue(root, "ExSubCode", StringUtils.EMPTY));
        data.setExStack(XmlUtil.getChildElementValue(root, "ExStack", StringUtils.EMPTY));
        data.setExSource(XmlUtil.getChildElementValue(root, "ExSource", StringUtils.EMPTY));
        data.setTxExternalCode(XmlUtil.getChildElementValue(root, "TxExternalCode", StringUtils.EMPTY));
        data.setTxPK(XmlUtil.getChildElementValue(root, "TxPK", StringUtils.EMPTY));
        data.setTxSource(XmlUtil.getChildElementValue(root, "TxSource", StringUtils.EMPTY));
        if (StringUtils.isNotBlank(data.getTxErrDesc())) {
            List<String> warningPattern = Arrays.asList(emsServiceConfiguration.getWarningPattern().split(";"));
            List<String> result = warningPattern.stream().filter(s -> data.getTxErrDesc().contains(s)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(result)) {
                data.setNotification(true);
                if (result.get(0).toUpperCase().indexOf("RC:") > -1) {
                    data.setRcString(this.getENCRC(result.get(0)));
                }
            }
        }
        // Element logData = root.getChild("LogData");
        String logData = root;
        if (logData != null) {
            data.setEj(XmlUtil.getChildElementValue(logData, "EJ", StringUtils.EMPTY));
            data.setAtmNo(XmlUtil.getChildElementValue(logData, "ATMNo", StringUtils.EMPTY));
            data.setAtmSeq(XmlUtil.getChildElementValue(logData, "ATMSeq", StringUtils.EMPTY));
            data.setStan(XmlUtil.getChildElementValue(logData, "STAN", StringUtils.EMPTY));
            data.setPcode(XmlUtil.getChildElementValue(logData, "PCode", StringUtils.EMPTY));
            data.setMessageId(XmlUtil.getChildElementValue(logData, "MessageId", StringUtils.EMPTY));
            data.setProgramName(XmlUtil.getChildElementValue(logData, "ProgramName", StringUtils.EMPTY));
            data.setBkno(XmlUtil.getChildElementValue(logData, "Bkno", StringUtils.EMPTY));
            data.setDesBkno(XmlUtil.getChildElementValue(logData, "DesBkno", StringUtils.EMPTY));
            data.setTroutBank(XmlUtil.getChildElementValue(logData, "TroutBank", StringUtils.EMPTY));
            data.setTrinBank(XmlUtil.getChildElementValue(logData, "TrinBank", StringUtils.EMPTY));
            data.setNotification(XmlUtil.getChildElementValue(logData, "Notification", false));
            data.setNotifyMail(XmlUtil.getChildElementValue(logData, "Responsible", StringUtils.EMPTY));
            // 2024-01-02 Richard modified start
            // by Ashiang 請改成如果訊息有帶ReturnCode再去parse它, 並且parse如果有例外不要丟Exception出來, 改成一般訊息說明即可, 比如parse FEPReturnCode XXX Failed because…
            String returnCode = XmlUtil.getChildElementValue(logData, "ReturnCode", StringUtils.EMPTY);
            if (StringUtils.isNotBlank(returnCode)) {
                try {
                    data.setRtnCode(FEPReturnCode.parse(returnCode));
                } catch (Exception e) {
                    SERVICELOGGER.warn(e, "parse FEPReturnCode:", returnCode, " failed. ", e.getMessage());
                }
            }
            // 2024-01-02 Richard modified end
            data.setRemark(XmlUtil.getChildElementValue(logData, "Remark", StringUtils.EMPTY));
            // 2025-08-14 Richard add
            data.setNotifyPhone(XmlUtil.getChildElementValue(logData, "NotifyPhone", StringUtils.EMPTY));
        }
        // Fly 2016/06/06 增加MSGID中文說明
        if (StringUtils.isNotBlank(data.getMessageId())) {
            // Msgctl dtCTL = msgctlExtMapper.selectByPrimaryKey(data.getMessageId());
            Msgctl dtCTL = this.selectByPrimaryKey(data.getMessageId());
            if (dtCTL != null) {
                data.setMessageName(StringUtils.join(data.getMessageId(), "[", dtCTL.getMsgctlMsgName(), "]"));
            }
        }
        // 判斷要不要通知由MSGFILE及MSGKB只要有一設定為true就通知
        // List<Msgkb> dtMsgKb = msgkbExtMapper.getMSGKB(emsLogMessage.getMdc().getErrcode(), data.getExSubCode());
        List<Msgkb> dtMsgKb = this.getMSGKB(emsLogMessage.getMdc().getErrcode(), data.getExSubCode());
        if (CollectionUtils.isNotEmpty(dtMsgKb)) {
            for (int i = 0; i < dtMsgKb.size(); i++) {
                if (data.getTxErrDesc() != null && StringUtils.isNotBlank(data.getTxErrDesc()) && data.getTxErrDesc().contains(dtMsgKb.get(i).getMsgpattern())) {
                    if (!data.isNotification()) {
                        data.setNotification(DbHelper.toBoolean(dtMsgKb.get(i).getNotify()));
                    }
                    if (StringUtils.isBlank(data.getNotifyMail())) {
                        data.setNotifyMail(dtMsgKb.get(i).getNotifymail());
                    }
                    // 2025-08-15 Richard add
                    if (StringUtils.isBlank(data.getNotifyPhone())) {
                        data.setNotifyPhone(dtMsgKb.get(i).getMsgkbNotifyphone());
                    }
                }
            }
        }
        // 如為轉出/扣款行及匯款行<>807, 不需寄送EMAIL
        // 轉出/扣款行及匯款行=807, 才需寄送EMAIL
        if (data.getTxErrDesc().indexOf("代號:6101") > -1) {
            if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(data.getTroutBank())) {
                // Fly 2016/04/20 取值前先判斷是否有值
                if (StringUtils.isNotBlank(data.getPcode()) && "2".equals(data.getPcode().substring(0, 1))) {
                    data.setNotification(false);
                }
            }
        }
        // Fly 2016/04/08 SQL timeout 與RM的exception 需寄mail通知
        if (data.getExStack().indexOf("org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection") >= 0) {
            data.setNotification(true);
            data.setRcString("CannotGetJdbcConnectionException: Failed to obtain JDBC Connection");
        }
        if ("RM".equals(emsLogMessage.getMdc().getSubsys()) && data.getTxErrDesc().indexOf(Const.MESSAGE_ERR_EXCEPTION_OCCUR) > -1) {
            data.setNotification(true);
        }
        // Fly 2016/11/25 證券整批轉即時批次出現異常時需寄MAIL
        if (data.getTxSource().indexOf("BT010300") > -1 && data.getTxErrDesc().indexOf(Const.MESSAGE_ERR_EXCEPTION_OCCUR) > -1) {
            data.setNotification(true);
            data.setRcString("全國繳費整批轉即時交易失敗");
        }
        // 2023-05-15 Richard add ATMGW憑證不正確，送EMS，EMS收到後判斷ReturnCode送mail
        if ("ATMGW".equalsIgnoreCase(data.getTxSource()) &&
                (data.getRtnCode() == FEPReturnCode.INVALID_CERTIFICATE ||
                        data.getRtnCode() == FEPReturnCode.NOT_SSL_RECORD ||
                        data.getRtnCode() == FEPReturnCode.INVALID_CERTIFICATE_ALIAS ||
                        data.getRtnCode() == FEPReturnCode.CERTIFICATE_NOT_MATCH ||
                        data.getRtnCode() == FEPReturnCode.CERTIFICATE_EXPIRED ||
                        data.getRtnCode() == FEPReturnCode.SSL_HANDSHAKE_NOT_COMPLETION)) {
            // data.setNotification(true);
            data.setRcString(data.getRemark());
        }
        return data;
    }

    private void sendMail(EMSApMessageData apMessage, EMSErrData lastErr) {
        try {
            if (!this.emsServiceConfiguration.isSendMail()) return;
            MailSender mailSender = SpringBeanFactoryUtil.getBean(MailSender.class, false);
            if (mailSender != null) {
                List<String> mailTo;
                if (StringUtils.isNotBlank(apMessage.getNotifyMail())) {
                    mailTo = StringUtil.split(apMessage.getNotifyMail(), ';', ',');
                } else {
                    mailTo = emsServiceConfiguration.getMailList();
                }
                // 2025-08-14 Richard add 這裡需要轉一次, 如果FEPNotifyMail_APD, FEPNotifyMail_SYS等這樣的值, 需要從sysconf檔中對應取出實際的值
                mailTo = FEPNotifyUtil.getNotifiyList(mailTo);
                final String endLine = "<br/>";
                StringBuilder mailBody = new StringBuilder();
                mailBody.append("發生時間：").append(lastErr.getEventData().getMdc().getLogDate()).append(endLine);
                mailBody.append("發生主機：").append(lastErr.getEventData().getMdc().getHostname()).append(endLine);
                mailBody.append("訊息代碼：").append(lastErr.getEventData().getMdc().getErrcode()).append(endLine);
                mailBody.append("ATM代號：").append(apMessage.getAtmNo()).append(endLine);
                mailBody.append("ATM序號：").append(apMessage.getAtmSeq()).append(endLine);
                mailBody.append("EJ：").append(apMessage.getEj()).append(endLine);
                mailBody.append("交易代號：").append(apMessage.getMessageName()).append(endLine);
                mailBody.append("轉出行：").append(apMessage.getTroutBank()).append(endLine);
                mailBody.append("轉入行：").append(apMessage.getTrinBank()).append(endLine);
                mailBody.append("財金Stan：").append(apMessage.getBkno()).append("-").append(apMessage.getStan()).append(endLine);
                mailBody.append("Dest. ID：").append(apMessage.getDesBkno()).append(endLine);
                mailBody.append("Source ID：").append(apMessage.getBkno()).append(endLine);
                mailBody.append("PCode：").append(apMessage.getPcode()).append(endLine);
                mailBody.append("服務名稱：").append(apMessage.getTxSource()).append(endLine);
                mailBody.append("函式名稱：").append(apMessage.getProgramName()).append(endLine);
                if (StringUtils.isNotBlank(apMessage.getRcString())) {
                    mailBody.append("訊息內容：").append(apMessage.getRcString()).append(endLine);
                    int c = apMessage.getTxErrDesc().indexOf(",INPUTSTR1");
                    if (c > -1) {
                        mailBody.append("參數內容：").append(apMessage.getTxErrDesc().substring(c + 1)).append(endLine);
                    } else {
                        if (StringUtils.isNotBlank(apMessage.getTxErrDesc())) {
                            mailBody.append(apMessage.getTxErrDesc()).append(endLine);
                        }
                    }
                } else {
                    mailBody.append("訊息內容：").append(apMessage.getTxErrDesc()).append(endLine);
                }
                Map<String, String> paramVars = new HashMap<>();
                // 主旨後綴
                paramVars.put(NotifyHelperConstant.NOTIFY_MESSAGE_CONTENT_SUBJECT_SUFFIX,
                        StringUtils.join(
                                Arrays.asList(
                                        lastErr.getEventData().getMdc().getHostname(), // HostName
                                        lastErr.getEventData().getMdc().getErrcode()), // ErrorCode
                                '-'));
                // Mail內容
                paramVars.put(NotifyHelperConstant.NOTIFY_MESSAGE_CONTENT_BODY, mailBody.toString());
                SERVICELOGGER.debug("Begin SendMail to [", StringUtils.join(mailTo, ','), "], Body:", paramVars.get(NotifyHelperConstant.NOTIFY_MESSAGE_CONTENT_BODY));
                // notifyHelper.sendSimpleMail(NotifyHelperTemplateId.EMS, StringUtils.join(mailTo, ','), paramVars, true);
                MailData mailData = new MailData();
                mailData.setFrom(this.emsServiceConfiguration.getMailSender());
                mailData.setTo(StringUtils.join(mailTo, ','));
                mailData.setSubject(StringUtils.join(
                        Arrays.asList(
                                "EMS異常事件監控系統警示通知", // Topic
                                lastErr.getEventData().getMdc().getHostname(), // HostName
                                lastErr.getEventData().getMdc().getErrcode()), // ErrorCode
                        '-'));
                mailData.setBody(mailBody.toString());
                mailData.setPriority(MailPriority.High);
                mailSender.sendHtmlEmail(mailData);
                SERVICELOGGER.info("SendMail ok");
            }
        } catch (Exception e) {
            SERVICELOGGER.error(e, "SendMail failed. ", e.getMessage());
        } finally {
            // 2025-01-14 Richard modified 搬到finally中, 避免上面出現異常沒有將lastErr的count重置
            lastErr.setLastNotifyTime(Calendar.getInstance());
            lastErr.setCount(0);
        }
    }

    private String getENCRC(String rc) {
        String rcStr = StringUtils.EMPTY;
        switch (rc) {
            case "RC:10,":
                rcStr = "Source Key Parity Error";
                break;
            case "RC:11,":
                rcStr = "Destination Key Parity Error";
                break;
            case "RC:17,":
                rcStr = "HSM需要授權";
                break;
            case "RC:81,":
                rcStr = "參數檔異常";
                break;
            case "RC:82,":
                rcStr = "參數檔異常";
                break;
            case "RC:83,":
                rcStr = "ATM Key File Access Error";
                break;
            case "RC:84,":
                rcStr = "RM Key File Access Error";
                break;
            case "RC:94,":
                rcStr = "ENCLib Call HSMSUIP失敗";
                break;
            case "RC:95,":
                rcStr = "ENCLib Call HSMSUIP回應失敗";
                break;
            case "RC:97,":
                rcStr = "ENCDB處理異常";
                break;
            case "RC:98,":
                rcStr = "無法連接HSM";
                break;
            case "RC:99,":
                rcStr = "無法連接HSM SUIP";
                break;
            case "RC:998,":
                rcStr = "無法取得Suip Socket物件";
                break;
            case "RC:999,":
                rcStr = "ENCLib發生異常";
                break;
        }
        return StringUtils.join(rc, rcStr);
    }

    /**
     * 檢查是否有未寄出的訊息一次寄出
     */
    public void resetErrorCounter() {
        SERVICELOGGER.debug("[ResetErrorCounter]begin");
        for (Map.Entry<String, EMSErrData> entry : errDic.entrySet()) {
            String key = entry.getKey();
            EMSErrData err = entry.getValue();
            SERVICELOGGER.debug("ResetErrorCounter Key:", key, ", Count:", err.getCount());
            if (err.getCount() > 0) {
                try {
                    EMSApMessageData apData = this.parseAPMessage(err.getEventData());
                    if (apData.isNotification()) {
                        this.sendMail(apData, err);
                        this.sendSms(apData, err);
                    } else {
                        err.setCount(0);
                    }
                } catch (Exception e) {
                    SERVICELOGGER.error(e, "resetErrorCounter failed. ", e.getMessage());
                }
            }
        }
        int size = errDic.size();
        SERVICELOGGER.debug("[ResetErrorCounter][clean] before errDic size:", size);
        boolean result = errDic.entrySet().removeIf(t -> t.getValue().getCount() == 0);
        SERVICELOGGER.debug("[ResetErrorCounter][clean] before errDic size:", size, ", after errDic size:", errDic.size(), ", clean result:", result);
        SERVICELOGGER.debug("[ResetErrorCounter]end");
    }

    private void sendSms(EMSApMessageData apMessage, EMSErrData lastErr) {
        if (!emsServiceConfiguration.isSendSms()) return;
        String notifyPhone = apMessage.getNotifyPhone();
        // 2025-08-14 Richard add NotifyPhone有值, 則進行簡訊通知
        if (StringUtils.isBlank(notifyPhone)) {
            SERVICELOGGER.warn("[sendSms]cannot send sms, cause empty notifyPhone!!!");
            return;
        }
        // 2025-08-14 Richard add 這裡需要轉一次, 如果FEPNotifyPhone_APD, FEPNotifyPhone_SYS等這樣的值, 需要從sysconf檔中對應取出實際的值
        List<String> dstaddrs = FEPNotifyUtil.getNotifiyList(notifyPhone);
        // 2025-09-03 Richard modified 簡訊改用三竹
        // HiairSmsOperator smsOperator = SpringBeanFactoryUtil.getBean(HiairSmsOperator.class, false);
        MitakeSmsOperator smsOperator = SpringBeanFactoryUtil.getBean(MitakeSmsOperator.class, false);
        if (smsOperator != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("異常事件監控系統警示通知");
            sb.append("(").append(lastErr.getEventData().getMdc().getHostname()).append(")");
            // 有ej和stan表示是交易類的
            if (StringUtils.isNotBlank(apMessage.getStan()) && StringUtils.isNotBlank(apMessage.getEj()) && !"0".equals(apMessage.getEj())) {
                sb.append(" Chl:").append(lastErr.getEventData().getMdc().getChannel());
                sb.append(" Stan:").append(apMessage.getStan());
                sb.append(" EJ:").append(apMessage.getEj());
                String erChl = StringUtils.EMPTY;
                // 預設放LogData.ReturnCode
                String erCode = StringUtils.EMPTY;
                if(apMessage.getRtnCode() != null){
                    erCode = apMessage.getRtnCode().name();
                }else{
                    erCode = lastErr.getEventData().getMdc().getErrcode();
                }
                // 從remark中獲取erChl和erCode
                String remark = apMessage.getRemark();
                if (StringUtils.isNotBlank(remark)) {
                    String found = "來源:";
                    int begin = remark.indexOf(found);
                    if (begin >= 0) {
                        int end = remark.indexOf(",", begin);
                        try {
                            erChl = StringUtils.substring(remark, begin + found.length(), end);
                        } catch (IndexOutOfBoundsException e) {
                            SERVICELOGGER.error(e, "fetch ErChl failed. ", e.getMessage(), ", remark:", remark);
                        }
                    }
                    found = "代號:";
                    begin = remark.indexOf(found);
                    if (begin >= 0) {
                        int end = remark.indexOf(",", begin);
                        try {
                            erCode = StringUtils.substring(remark, begin + found.length(), end);
                        } catch (IndexOutOfBoundsException e) {
                            SERVICELOGGER.error(e, "fetch ErCode failed. ", e.getMessage(), ", remark:", remark);
                        }
                    }
                }
                sb.append(" ErChl:").append(erChl);
                sb.append(" ErCode:").append(erCode);
            } else {
                sb.append(" ").append(apMessage.getTxErrDesc());
            }
            // 2025-08-14 Richard modified改用LogData中的NotifyPhone
            // String[] smsTelList = emsServiceConfiguration.getSmsTelList();
            // String[] smsTelList = notifiyPhoneList.toArray(new String[0]);
            String message = sb.toString();
            // 因簡訊有長度限制, 請在EMS Config加一個參數定義簡訊最大長度, 預設60, Description的字數若超過最大長度則substring掉即可
            if (message.length() > this.emsServiceConfiguration.getSmsMessageLengthLimit()) {
                message = message.substring(0, this.emsServiceConfiguration.getSmsMessageLengthLimit());
            }
            SERVICELOGGER.debug("Begin Send SMS to [", StringUtils.join(dstaddrs, ','), "], message:", message);
            smsOperator.send(dstaddrs, message, true);
            SERVICELOGGER.info("Send SMS finished");
        }
    }

    private Msgctl selectByPrimaryKey(String msgctlMsgid) {
        String sql = "select MSGCTL_MSGID, MSGCTL_MSG_NAME from MSGCTL where MSGCTL_MSGID = ?";
        try {
            return fepJdbcTemplate.query(sql, rs -> {
                if (rs.next()) {
                    Msgctl msgctl = new Msgctl();
                    msgctl.setMsgctlMsgid(rs.getString("MSGCTL_MSGID"));
                    msgctl.setMsgctlMsgName(rs.getString("MSGCTL_MSG_NAME"));
                    return msgctl;
                }
                return null;
            }, msgctlMsgid);
        } catch (Exception e) {
            SERVICELOGGER.error(e, "select Msgctl failed, msgctlMsgid:", msgctlMsgid);
        }
        return null;
    }

    private List<Msgkb> getMSGKB(String errcode, String exSubCode) {
        String sql = "select MSGKB_NO, NOTIFY, NOTIFYMAIL, MSGKB_NOTIFYPHONE, MSGPATTERN from MSGKB where ERRORCODE = ? AND EXSUBCODE = ?";
        try {
            return fepJdbcTemplate.query(sql, rs -> {
                List<Msgkb> result = new ArrayList<>();
                while (rs.next()) {
                    Msgkb msgkb = new Msgkb();
                    msgkb.setMsgkbNo(rs.getLong("MSGKB_NO"));
                    msgkb.setNotify(rs.getShort("NOTIFY"));
                    msgkb.setNotifymail(rs.getString("NOTIFYMAIL"));
                    msgkb.setMsgpattern(rs.getString("MSGPATTERN"));
                    msgkb.setMsgkbNotifyphone(rs.getString("MSGKB_NOTIFYPHONE"));
                    result.add(msgkb);
                }
                return result;
            }, errcode, exSubCode);
        } catch (Exception e) {
            SERVICELOGGER.error(e, "select Msgctl failed, errcode:", errcode, ", exSubCode:", exSubCode);
        }
        return null;
    }

    private int getAlertCounts(Alert alert) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ALERT");
        List<String> where = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        if (alert.getArErcode() != null) {
            where.add("AR_ERCODE = ?");
            args.add(alert.getArErcode());
        }
        if (alert.getArHostname() != null) {
            where.add("AR_HOSTNAME = ?");
            args.add(alert.getArHostname());
        }
        if (alert.getArErdescription() != null) {
            where.add("AR_ERDESCRIPTION = ?");
            args.add(alert.getArErdescription());
        }
        if (alert.getArDatetime() != null) {
            where.add("AR_DATETIME > ?");
            args.add(alert.getArDatetime());
        }
        if (alert.getArSubsys() != null) {
            where.add("AR_SUBSYS = ?");
            args.add(alert.getArSubsys());
        }
        if (CollectionUtils.isNotEmpty(where)) {
            sql.append(" WHERE ")
                    .append(StringUtils.join(where, " AND "));
        }
        try {
            return fepJdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        } catch (Exception e) {
            SERVICELOGGER.error(e, "select Alert count failed, ", StringUtil.replace(StringUtils.join(where, ", "), "\\?", args));
        }
        return -1;
    }

    private int insertSelective(Alert alert) {
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        if (alert.getArNo() != null) {
            columns.add("AR_NO");
            values.add(alert.getArNo());
        }
        if (alert.getArDatetime() != null) {
            columns.add("AR_DATETIME");
            values.add(alert.getArDatetime());
        }
        if (alert.getArHostname() != null) {
            columns.add("AR_HOSTNAME");
            values.add(alert.getArHostname());
        }
        if (alert.getArHostip() != null) {
            columns.add("AR_HOSTIP");
            values.add(alert.getArHostip());
        }
        if (alert.getArLevel() != null) {
            columns.add("AR_LEVEL");
            values.add(alert.getArLevel());
        }
        if (alert.getArApplication() != null) {
            columns.add("AR_APPLICATION");
            values.add(alert.getArApplication());
        }
        if (alert.getArLine() != null) {
            columns.add("AR_LINE");
            values.add(alert.getArLine());
        }
        if (alert.getArSys() != null) {
            columns.add("AR_SYS");
            values.add(alert.getArSys());
        }
        if (alert.getArSubsys() != null) {
            columns.add("AR_SUBSYS");
            values.add(alert.getArSubsys());
        }
        if (alert.getArErcode() != null) {
            columns.add("AR_ERCODE");
            if (alert.getArErcode().length() > 50) {
                values.add(alert.getArErcode().substring(0, 50)); // 2025-11-04 Richard modified 避免字串過長
            } else {
                values.add(alert.getArErcode());
            }
        }
        if (alert.getArErdescription() != null) {
            columns.add("AR_ERDESCRIPTION");
            if (alert.getArErdescription().length() > 2000) {
                values.add(alert.getArErdescription().substring(0, 2000)); // 2025-11-04 Richard modified 避免字串過長
            } else {
                values.add(alert.getArErdescription());
            }
        }
        if (alert.getArEj() != null) {
            columns.add("AR_EJ");
            values.add(alert.getArEj());
        }
        if (alert.getStatus() != null) {
            columns.add("STATUS");
            values.add(alert.getStatus());
        }
        if (alert.getRcvtime() != null) {
            columns.add("RCVTIME");
            values.add(alert.getRcvtime());
        }
        if (alert.getAtmno() != null) {
            columns.add("ATMNO");
            values.add(alert.getAtmno());
        }
        if (alert.getArMessage() != null) {
            columns.add("AR_MESSAGE");
            if (alert.getArMessage().length() > 1048576) {
                values.add(alert.getArMessage().substring(0, 1048576)); // 2025-11-04 Richard modified 避免字串過長
            } else {
                values.add(alert.getArMessage());
            }
        }
        if (CollectionUtils.isNotEmpty(columns)) {
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ALERT (").append(StringUtils.join(columns, ", ")).append(") ")
                    .append(" VALUES (").append(StringUtils.repeat("?", ", ", columns.size())).append(")");
            try {
                return fepJdbcTemplate.update(sql.toString(), values.toArray());
            } catch (Exception e) {
                SERVICELOGGER.error(e, "insertSelective Alert failed");
            }
        }
        return -1;
    }
}
