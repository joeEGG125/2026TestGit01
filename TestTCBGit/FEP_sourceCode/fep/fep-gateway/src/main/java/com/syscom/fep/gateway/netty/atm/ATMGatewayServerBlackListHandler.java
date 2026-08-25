package com.syscom.fep.gateway.netty.atm;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.thread.ThreadPoolFactory;
import com.syscom.fep.gateway.entity.Gateway;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 用來處理黑名單
 *
 * @author Richard
 */
public class ATMGatewayServerBlackListHandler extends FEPBase {
    private final LogHelper logger = LogHelperFactory.getTraceLogger();
    @Autowired
    private ATMGatewayServerConfiguration configuration;
    private final Map<String, ConnectFailedData> atmIpToFailedTimesLimitMap = new ConcurrentHashMap<>(); // 2026-03-19 Richard modified 改用ConcurrentHashMap避免高並發時出現異常
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new SimpleThreadFactory(StringUtils.join(Gateway.ATMGW.name(), "-BlackListHandler")));

    @PostConstruct
    public void init() {
        // 每隔1分鐘檢查一次黑名單, 超過clearBlackListInterval分鐘則從黑名單中移除
        executor.scheduleAtFixedRate(() -> {
            try {
                logger.info("[", ProgramName, "][CheckBlackList][before]ATM IP in Black List:", StringUtils.join(atmIpToFailedTimesLimitMap.keySet(), ","));
                long currentTimeMillis = System.currentTimeMillis();
                long maximumDurationTimeMillis = (long) configuration.getClearBlackListInterval() * 60 * 1000; // 在黑名單中的最長時間
                for (Iterator<String> it = atmIpToFailedTimesLimitMap.keySet().iterator(); it.hasNext(); ) {
                    String atmIp = it.next();
                    ConnectFailedData connectFailedData = atmIpToFailedTimesLimitMap.get(atmIp);
                    long passedTimeMillis = currentTimeMillis - connectFailedData.getLastTime(); // 已經在黑名單中的時間
                    if (passedTimeMillis > maximumDurationTimeMillis) { // 已經在黑名單中的時間超過最大時間限制, 則從黑名單中移除
                        it.remove();
                        logger.warn("[", ProgramName, "][CheckBlackList]Removed from Black List after ", passedTimeMillis, " milliseconds, ATM IP = [", atmIp, "]");
                    } else {
                        long remainTimeMillis = maximumDurationTimeMillis - passedTimeMillis; // 還有多少時間才可以從黑名單中移除
                        logger.warn("[", ProgramName, "][CheckBlackList]", atmIp, " still in Black List, remain time:", remainTimeMillis, " milliseconds");
                    }
                }
            } catch (Throwable e) {
                logger.warn(e,"[", ProgramName, "][CheckBlackList][ExceptionOccur]ATM IP in Black List:", StringUtils.join(atmIpToFailedTimesLimitMap.keySet(), ","));
            } finally {
                logger.info("[", ProgramName, "][CheckBlackList][after]ATM IP in Black List:", StringUtils.join(atmIpToFailedTimesLimitMap.keySet(), ","));
            }
        }, 5, configuration.getCheckBlackListInterval(), TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        logger.trace(ProgramName, " start to destroy...");
        ThreadPoolFactory.shutdown(this.executor, ProgramName);
    }

    /**
     * 累加錯誤次數, 並更新錯誤時間
     *
     * @param atmIp
     */
    public void incrementFailedTimes(String atmIp) {
        ConnectFailedData connectFailedData = atmIpToFailedTimesLimitMap.computeIfAbsent(atmIp, k -> new ConnectFailedData());
        connectFailedData.setLastTime(Calendar.getInstance().getTimeInMillis());
        connectFailedData.accumulateCount();
        if (connectFailedData.getCount() >= configuration.getCheckClientFailedTimesLimit()) {
            logger.warn("[", ProgramName, "][incrementFailedTimes]Error Count was >= ", configuration.getCheckClientFailedTimesLimit(), ", ATM IP = [", atmIp, "]");
        }
    }

    /**
     * 從黑名單中依據ATM IP移除
     *
     * @param atmIp
     */
    public void remove(String atmIp) {
        ConnectFailedData connectFailedData = atmIpToFailedTimesLimitMap.remove(atmIp);
        if (connectFailedData != null) {
            long passedTimeMillis = System.currentTimeMillis() - connectFailedData.getLastTime();
            logger.warn("[", ProgramName, "][remove]Removed from Black List after ", passedTimeMillis, " milliseconds, ATM IP = [", atmIp, "]");
        } else {
            logger.warn("[", ProgramName, "][remove]Not exist in Black List, ATM IP = [", atmIp, "]");
        }
    }

    /**
     * 是否達到最大錯誤次數
     *
     * @param atmIp
     * @return
     */
    public boolean isExceededFailuresCount(String atmIp) {
        boolean isExceededFailuresCount = false;
        ConnectFailedData connectFailedData = atmIpToFailedTimesLimitMap.get(atmIp);
        if (connectFailedData == null) {
            logger.warn("[", ProgramName, "][isExceededFailuresCount]Not exist in Black List, ATM IP = [", atmIp, "]");
        } else {
            isExceededFailuresCount = connectFailedData.getCount() >= configuration.getCheckClientFailedTimesLimit();
            logger.warn("[", ProgramName, "][isExceededFailuresCount]Black List, ATM IP = [", atmIp, "], isExceededFailuresCount = [", isExceededFailuresCount, "], connectFailedData = [", connectFailedData.toString(), "]");
        }
        return isExceededFailuresCount;
    }

    private class ConnectFailedData {
        private long lastTime;
        private int count;

        public long getLastTime() {
            return lastTime;
        }

        public void setLastTime(long lastTime) {
            this.lastTime = lastTime;
        }

        public int getCount() {
            return count;
        }

        public void accumulateCount() {
            this.count += 1;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("lastTime", lastTime)
                    .append("count", count)
                    .toString();
        }
    }
}
