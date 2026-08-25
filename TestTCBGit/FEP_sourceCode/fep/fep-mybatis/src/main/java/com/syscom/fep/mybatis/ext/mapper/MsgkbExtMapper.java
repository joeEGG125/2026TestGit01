package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.MsgkbMapper;
import com.syscom.fep.mybatis.model.Msgkb;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@Resource
public interface MsgkbExtMapper extends MsgkbMapper {
    List<Msgkb> getDataTableByPrimaryKey(Msgkb msgkb);

    /**
     * ZK ADD 2021-12-09
     * */
    List<Msgkb> getMSGKB(@Param("errcode") String errcode, @Param("exSubCode") String exSubCode);
    
    List<Msgkb> getMsgkbList(@Param("argsMap") Map<String,Object> argsMap);

    Msgkb selectByMSGKB(@Param("errorcode") String errorcode, @Param("externalcode") String externalcode, @Param("exsubcode") String exsubcode);

}
