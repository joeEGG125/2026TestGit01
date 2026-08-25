package com.syscom.fep.mybatis.ems.dao;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.ems.model.Feplog;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface FeplogDao {

    public void setTableNameSuffix(String tableNameSuffix, String invoker);

    public String getTableNameSuffix();

    int deleteByPrimaryKey(Feplog record);

    int insert(Feplog record);

    int insertSelective(Feplog record);

    Feplog selectByPrimaryKey(Long logno);

    int updateByPrimaryKeySelective(Feplog record);

    int updateByPrimaryKeyWithBLOBs(Feplog record);

    int updateByPrimaryKey(Feplog record);

    int insertBatch(List<Feplog> recordList, int flushStatementsTotal);

    int insertSelectiveBatch(List<Feplog> recordList, int flushStatementsTotal);

    /**
     * 2021-08-29 Richard add for FEPLOG
     *
     * @param feplog
     * @param logDateBegin
     * @param logDateEnd
     * @param ejfnoList
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<Feplog> getMultiFepLogByDef(Feplog feplog, Date logDateBegin, Date logDateEnd, List<Long> ejfnoList, Integer pageNum, Integer pageSize);

    public PageInfo<Feplog> getMultiFepLogByDef_1(Feplog feplog, Integer pageNum, Integer pageSize);

    public List<Feplog> getFeplogByDef(Feplog feplog);

    /**
     * @param argsMap
     * @return
     */
    public PageInfo<Feplog> getMultiFEPLogByDef(Map<String, Object> argsMap);

    public int insertBatchEMS(List<Feplog> recordList, int flushStatementsTotal);

    int insertBatchEMS(List<Feplog> recordList);

    int jdbcInsert(List<Feplog> recordList);
}
