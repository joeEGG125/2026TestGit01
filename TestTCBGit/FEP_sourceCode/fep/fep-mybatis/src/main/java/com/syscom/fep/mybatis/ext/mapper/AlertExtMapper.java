package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.AlertMapper;
import com.syscom.fep.mybatis.model.Alert;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Resource
public interface AlertExtMapper extends AlertMapper {
    List<HashMap<String,Object>> queryAlert( @Param("alert")Alert alert, @Param("arDatetimeB") String arDatetimeB, @Param("arDatetimeE")String arDatetimeE);

    List<Alert> getDataTableByPrimaryKey(Alert alert);

    /**
     * ZK add 2021-12-09
    * */
    int getAlertCounts(Alert alert);
}
