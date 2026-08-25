package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.NotifytemplateMapper;
import com.syscom.fep.mybatis.model.Notifytemplate;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;

@Resource
public interface NotifytemplateExtMapper extends NotifytemplateMapper {
    List<Notifytemplate> getNotifyTemplatesById(@Param("templateId") String templateId);
}
