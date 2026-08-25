package com.syscom.fep.mybatis.deslog.dao;

import com.syscom.fep.mybatis.deslog.model.Deslog;

import java.util.List;

public interface DeslogDao {
    int insertBatch(List<Deslog> recordList, int flushStatementsTotal);

    int insertBatch(List<Deslog> recordList);

    int jdbcInsert(List<Deslog> recordList);
}
