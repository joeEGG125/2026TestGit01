package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.NotifycontentMapper;
import com.syscom.fep.mybatis.model.Notifycontent;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;
@Resource
public interface NotifycontentExtMapper extends NotifycontentMapper {
    /**
     * 發送通知內容記錄檔List查詢
     *
     * @param var1
     * @return List<Notifycontent>
     */
    public List<Notifycontent> getNotifycontentsByRequestId(@Param("requestId") String var1);
}
