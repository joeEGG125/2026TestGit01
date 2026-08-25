package com.syscom.fep.mybatis.his.dao;

import java.util.Map;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.his.model.Feptxn;
import com.syscom.fep.mybatis.his.model.Feptxntcb;

public interface HisFeptxnDao {

    public void setTableNameSuffix(String tableNameSuffix, String invoker);
    
    /**
     * FEP Web 交易日誌(FEPTXN)查詢
     *
     * @param args
     * @return
     */
    public PageInfo<Feptxn> getFeptxn(Map<String, Object> args);

    /**
     * FEP Web 交易日誌(FEPTXN)查詢
     *
     * @param args
     * @return
     */
    public Map<String, Object> getFeptxnSummary(Map<String, Object> args);

    /**
     * FEP Web 交易日誌(FEPTXN)查詢明細資料
     *
     * @param feptxnTxDate
     * @param feptxnEjfno
     * @return
     */
    public Map<String, Object> getFeptxnIntltxn(String feptxnTxDate, Integer feptxnEjfno);
    
    public Feptxntcb selectByPrimaryKeyForFeptxntcb(String feptxnTxDate, Integer feptxnEjfno) throws Exception;

}
