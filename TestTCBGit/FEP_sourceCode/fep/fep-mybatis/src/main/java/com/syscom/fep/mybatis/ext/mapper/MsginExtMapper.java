package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.MsginMapper;
import com.syscom.fep.mybatis.model.Msgin;
import com.syscom.fep.mybatis.model.Msgout;

import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

@Resource
public interface MsginExtMapper extends MsginMapper {

    public Msgin getMSGINByCheckINData(Msgin defMsgin);

    List<Msgin> getMsgInByDef(Msgin oMsgin);
    
    List<HashMap<String,Object>> getMSGINDtByDef(@Param("msgin")Msgin msgin, @Param("msgout")Msgout msgout);

    List<HashMap<String,Object>> getMSGINByDateSendbankFISCRtnCodeEJ(@Param("txdate") String txdate,@Param("senderBank")String senderBank,@Param("fiscRtnCode")String fiscRtnCode,@Param("ej")String ej);
}
