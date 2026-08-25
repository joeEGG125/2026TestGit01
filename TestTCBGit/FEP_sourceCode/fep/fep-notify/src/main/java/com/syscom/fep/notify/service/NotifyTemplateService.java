package com.syscom.fep.notify.service;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.NotifytemplateExtMapper;
import com.syscom.fep.mybatis.mapper.NotifytemplateMapper;
import com.syscom.fep.mybatis.model.Notifytemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotifyTemplateService extends FEPBase {
    // private static LogHelper logger = LogHelperFactory.getGeneralLogger();

    @CacheEvict(value = "notifyTemplateCache", allEntries = true)
    public void cleanNotifyTemplateCache(LogData logData) {
        // 清除全部的 Cache 不會動到 JPA
        // logger.info("清除全部的 NotifyTemplate Cache");
        logData.setRemark("清除全部的 NotifyTemplate Cache");
        logData.setProgramName(StringUtils.join(ProgramName, ".cleanNotifyTemplateCache"));
        logMessage(logData);
    }


    public void addNotifyTemplate() {
        // 資料新增
    }


    @CacheEvict(value = "notifyTemplateCache", allEntries = true)
    public void updateNotifyTemplate() {
        // 資料庫更新時，要清除全部的 Cache
    }


    @Cacheable(value = "notifyTemplateCache")
    public Notifytemplate getNotifyTemplateById(LogData logData, String inputTemplateId) {
        NotifytemplateMapper notifyTemplateMapper = SpringBeanFactoryUtil.getBean(NotifytemplateMapper.class);
        Notifytemplate notifyTemplate = notifyTemplateMapper.selectByPrimaryKey(inputTemplateId);
        // logger.info("根據 templateId:", inputTemplateId, " 獲取 Notifytemplate");
        logData.setRemark(StringUtils.join("根據 templateId:", inputTemplateId, " 獲取 Notifytemplate"));
        logData.setProgramName(StringUtils.join(ProgramName, ".cleanNotifyTemplateCache"));
        logMessage(logData);
        return notifyTemplate;
    }

    @Cacheable(value = "notifyTemplatesCache")
    public List<Notifytemplate> getNotifyTemplatesById(LogData logData, String inputTemplateId) {
        NotifytemplateExtMapper notifyTemplateExtMapper = SpringBeanFactoryUtil.getBean(NotifytemplateExtMapper.class);
        List<Notifytemplate> notifyTemplate = notifyTemplateExtMapper.getNotifyTemplatesById(inputTemplateId);
        // logger.info("根據 templateId:", inputTemplateId, " 獲取 Notifytemplate");
        logData.setRemark(StringUtils.join("根據 templateId:", inputTemplateId, " 獲取 Notifytemplate"));
        logData.setProgramName(StringUtils.join(ProgramName, ".cleanNotifyTemplateCache"));
        logMessage(logData);
        return notifyTemplate;
    }
}
