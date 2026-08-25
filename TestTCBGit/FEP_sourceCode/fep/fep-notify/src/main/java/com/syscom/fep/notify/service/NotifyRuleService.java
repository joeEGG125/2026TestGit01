package com.syscom.fep.notify.service;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.NotifyruleMapper;
import com.syscom.fep.mybatis.model.Notifyrule;
import com.syscom.fep.notify.exception.NotifyException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class NotifyRuleService extends FEPBase {
    // private static LogHelper logger = LogHelperFactory.getGeneralLogger();

    @CacheEvict(value = "notifyRuleCache", allEntries = true)
    public void cleanNotifyRuleCache(LogData logData) {
        // 清除全部的 Cache 不動到 JPA
        // logger.info("清除全部的 NotifyRule Cache");
        logData.setRemark("清除全部的 NotifyRule Cache");
        logData.setProgramName(StringUtils.join(ProgramName, ".cleanNotifyRuleCache"));
        logMessage(logData);
    }

    @Cacheable(value = "notifyRuleCache")
    public Notifyrule getNotifyRuleById(LogData logData, Long inputRuleId) throws NotifyException {
        // 資料從 DB 取得
        NotifyruleMapper notifyruleMapper = SpringBeanFactoryUtil.getBean(NotifyruleMapper.class);
        Notifyrule notifyrule = notifyruleMapper.selectByPrimaryKey(inputRuleId);
        // logger.info("根據 ruleId:", inputRuleId, "獲取 Rule");
        logData.setRemark(StringUtils.join("根據 ruleId:", inputRuleId, "獲取 Rule"));
        logData.setProgramName(StringUtils.join(ProgramName, ".getNotifyRuleById"));
        logMessage(logData);
        return notifyrule;
    }
}
