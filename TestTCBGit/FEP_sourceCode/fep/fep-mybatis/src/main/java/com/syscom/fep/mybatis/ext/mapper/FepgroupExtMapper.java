package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.FepgroupMapper;
import com.syscom.fep.mybatis.model.Fepgroup;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@Resource
public interface FepgroupExtMapper extends FepgroupMapper {
    /*
     * zk 2022-1-12
     * */
    List<Fepgroup> queryAllData(@Param("orderBy")String orderBy);
    
    /**
     * Bruce add 群組與功能查詢
     * @return
     */
    List<Fepgroup> getFepGroupByPkLike(@Param("args") Map<String, Object> args);
    
    /**
     * Bruce add 使用者資料查詢 權限群組下拉選單
     * @return
     */
    List<Fepgroup> getDistinctGroupIDName();
    
    /**
     * Bruce add 取得權限群組中文名稱
     * @param groupId
     * @return
     */
    public String  getGroupIDName(@Param("groupId") String groupId);
}