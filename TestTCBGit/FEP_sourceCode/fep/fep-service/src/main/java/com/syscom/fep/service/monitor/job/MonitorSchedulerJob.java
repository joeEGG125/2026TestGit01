package com.syscom.fep.service.monitor.job;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.SmsExtMapper;
import com.syscom.fep.mybatis.util.DB2Util;
import com.syscom.fep.scheduler.job.SchedulerJob;
import com.syscom.fep.service.monitor.svr.MonitorSchedulerService;
import com.syscom.fep.service.monitor.vo.MonitorServerInfo;
import com.syscom.fep.vo.monitor.MonitorConstant;
import org.apache.commons.io.IOUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.quartz.JobExecutionContext;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Calendar;
import java.util.Date;

public class MonitorSchedulerJob extends SchedulerJob<MonitorSchedulerJobConfig> implements MonitorConstant {
    @Override
    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_APPMON);
    }

    @Override
    protected void executeJob(JobExecutionContext context, MonitorSchedulerJobConfig config) throws Exception {
        MonitorSchedulerService service = SpringBeanFactoryUtil.getBean(MonitorSchedulerService.class);
        service.reloadMonitor();
    }

    @PostConstruct
    @Override
    public void init() {
        putMDC();
        super.init();
        try {
            ScheduleLogger.info("try to clear sms table...");
            SmsExtMapper smsExtMapper = SpringBeanFactoryUtil.getBean(SmsExtMapper.class);
            int count = smsExtMapper.deleteAll();
            ScheduleLogger.info("clear sms table successful, row count is ", count);
        } catch (Exception e) {
            ScheduleLogger.warn(e, "clear sms table failed");
        }
    }

    @PreDestroy
    public void terminate() {
        putMDC();
        // 更新狀態
        MonitorSchedulerJobConfig config = this.getJobConfig();
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_SQL_SESSION_FACTORY);
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
            SmsExtMapper smsExtMapper = sqlSession.getMapper(SmsExtMapper.class);
            String smsServicestate = null;
            Date smsStoptime = null;
            // 各個FEP服務
            MonitorServerInfo[] services = new MonitorServerInfo[config.getServices().size()];
            config.getServices().toArray(services);
            for (MonitorServerInfo service : services) {
                smsServicestate = "2";
                smsStoptime = null;
                if (service.getName().equalsIgnoreCase(FEPConfig.getInstance().getApplicationName())) {
                    smsServicestate = "0";
                    smsStoptime = Calendar.getInstance().getTime();
                }
                smsExtMapper.updateFEPMonitorServiceWhenTerminate(service.getName(), service.getHostip(), smsServicestate, smsStoptime);
            }
            smsServicestate = "2";
            smsStoptime = null;
            // 主機
            smsExtMapper.updateFEPMonitorServiceWhenTerminate(SERVICE_NAME_SYSTEM, config.getSystem().getHostip(), smsServicestate, smsStoptime);
            // DB
            smsExtMapper.updateFEPMonitorServiceWhenTerminate(SERVICE_NAME_DB, config.getSystem().getHostip(), smsServicestate, smsStoptime);
            // MQ
            smsExtMapper.updateFEPMonitorServiceWhenTerminate(SERVICE_NAME_MQ, config.getSystem().getHostip(), smsServicestate, smsStoptime);
            // NET SERVER
            smsExtMapper.updateFEPMonitorServiceWhenTerminate(SERVICE_NAME_NET_SERVER, config.getSystem().getHostip(), smsServicestate, smsStoptime);
            // NET CLIENT
            smsExtMapper.updateFEPMonitorServiceWhenTerminate(SERVICE_NAME_NET_CLIENT, config.getSystem().getHostip(), smsServicestate, smsStoptime);
            // PROCESS
            smsExtMapper.updateFEPMonitorServiceWhenTerminate(SERVICE_NAME_PROCESS, config.getSystem().getHostip(), smsServicestate, smsStoptime);
            // commit
            sqlSession.commit();
        } catch (Exception e) {
            DB2Util.handleBatchExecutorException(e);
            ScheduleLogger.warn(e, "update sms failed");
        } finally {
            IOUtils.closeQuietly(sqlSession);
        }
    }
}
