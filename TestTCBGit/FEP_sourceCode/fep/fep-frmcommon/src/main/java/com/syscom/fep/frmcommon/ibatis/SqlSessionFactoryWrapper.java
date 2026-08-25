package com.syscom.fep.frmcommon.ibatis;

import org.apache.ibatis.session.*;

public class SqlSessionFactoryWrapper implements SqlSessionFactory {
    private final SqlSessionFactory sqlSessionFactory;

    public SqlSessionFactoryWrapper(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public Configuration getConfiguration() {
        return sqlSessionFactory.getConfiguration();
    }

    @Override
    public SqlSession openSession() {
        return new SqlSessionWrapper(sqlSessionFactory.openSession());
    }

    @Override
    public SqlSession openSession(boolean autoCommit) {
        return new SqlSessionWrapper(sqlSessionFactory.openSession(autoCommit));
    }

    @Override
    public SqlSession openSession(java.sql.Connection connection) {
        return new SqlSessionWrapper(sqlSessionFactory.openSession(connection));
    }

    @Override
    public SqlSession openSession(TransactionIsolationLevel level) {
        return new SqlSessionWrapper(sqlSessionFactory.openSession(level));
    }

    @Override
    public SqlSession openSession(ExecutorType execType) {
        return new SqlSessionWrapper(sqlSessionFactory.openSession(execType));
    }

    @Override
    public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
        return new SqlSessionWrapper(sqlSessionFactory.openSession(execType, autoCommit));
    }

    @Override
    public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
        return new SqlSessionWrapper(sqlSessionFactory.openSession(execType, level));
    }

    @Override
    public SqlSession openSession(ExecutorType execType, java.sql.Connection connection) {
        return new SqlSessionWrapper(sqlSessionFactory.openSession(execType, connection));
    }
}
