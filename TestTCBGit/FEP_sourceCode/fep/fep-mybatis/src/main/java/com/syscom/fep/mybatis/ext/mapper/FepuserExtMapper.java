package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.FepuserMapper;
import com.syscom.fep.mybatis.model.Fepuser;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@Resource
public interface FepuserExtMapper extends FepuserMapper {
    public List<Fepuser> getFEPUSERByDef(Fepuser fepuser);

    /**
     * Bruce add 使用者資料查詢 查詢
     *
     * @param argsMap
     * @return
     */
    public List<Fepuser> getFepUser(@Param("argsMap") Map<String, Object> argsMap);

    /**
     * Bruce add 取得分行代號中文
     *
     * @param brNo
     * @return
     */
    public String getBrNoName(@Param("brNo") String brNo);

    public List<Fepuser> getFEPUSER();

    public List<Fepuser> getFepUserByLogonId(@Param("fepuserLogonid") String fepuserLogonid);

    public int updateByLogonIdSelective(Fepuser record);

    public int deleteByLogonId(Fepuser record);
}