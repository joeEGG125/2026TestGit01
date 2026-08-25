package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.NotifyrequestMapper;
import com.syscom.fep.mybatis.model.Notifyrequest;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;

@Resource
public interface NotifyrequestExtMapper extends NotifyrequestMapper {
    Notifyrequest byClientId(@Param("clientId") String clientId);

}
