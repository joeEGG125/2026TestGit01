package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.ReportdepMapper;
import com.syscom.fep.mybatis.model.Reportdep;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;

@Resource
public interface ReportdepExtMapper extends ReportdepMapper {

	public List<Reportdep> queryBankAll();
    public List<Reportdep> getBankByDepName(@Param("reportdepDepname") String reportdepDepname, @Param("direction") String direction);
    public int updateByDepNo(Reportdep record);
    public List<Reportdep> getBankData(Reportdep reportdep);
}
