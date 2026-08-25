package com.syscom.fep.mybatis.ems.dao.impl;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ems.configuration.DataSourceEmsConstant;
import com.syscom.fep.mybatis.ems.dao.FeplogDao;
import com.syscom.fep.mybatis.ems.ext.mapper.FeplogExtMapper;
import com.syscom.fep.mybatis.ems.ext.model.FeplogExt;
import com.syscom.fep.mybatis.ems.mapper.FeplogMapper;
import com.syscom.fep.mybatis.ems.model.Feplog;
import com.syscom.fep.mybatis.ems.util.DB2EMSUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository("feplogDao")
@Scope("prototype")
public class FeplogDaoImpl implements FeplogDao, DataSourceEmsConstant {
    private final LogHelper logger = LogHelperFactory.getRepositoryLogger();
    @Autowired
    private FeplogExtMapper feplogMapper;
    @Qualifier(BEAN_NAME_JDBC_TEMPLATE)
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String tableNameSuffix;

    private Feplog setTableNameSuffix(Feplog feplog) {
        if (feplog != null) {
            if (feplog instanceof FeplogExt) {
                ((FeplogExt) feplog).setTableNameSuffix(this.tableNameSuffix);
            } else {
                return this.setTableNameSuffix(new FeplogExt(feplog));
            }
        }
        return feplog;
    }

    private List<Feplog> setTableNameSuffix(List<Feplog> list) {
        List<Feplog> resultList = null;
        if (list != null) {
            resultList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                resultList.add(this.setTableNameSuffix(list.get(i)));
            }
        }
        return resultList;
    }

    @Override
    public void setTableNameSuffix(String tableNameSuffix, String invoker) {
        this.tableNameSuffix = tableNameSuffix;
        logger.debug("Switch to [FEPLOG", tableNameSuffix, "] by [", invoker, "]");
    }

    @Override
    public String getTableNameSuffix() {
        return this.tableNameSuffix;
    }

    @Override
    public int deleteByPrimaryKey(Feplog record) {
        return feplogMapper.deleteByPrimaryKey(this.setTableNameSuffix(record));
    }

    @Override
    public int insert(Feplog record) {
        return feplogMapper.insert(this.setTableNameSuffix(record));
    }

    @Override
    public int insertSelective(Feplog record) {
        return feplogMapper.insertSelective(this.setTableNameSuffix(record));
    }

    @Override
    public Feplog selectByPrimaryKey(Long logno) {
        return this.setTableNameSuffix(feplogMapper.selectByPrimaryKey(this.tableNameSuffix, logno));
    }

    @Override
    public int updateByPrimaryKeySelective(Feplog record) {
        return feplogMapper.updateByPrimaryKeySelective(this.setTableNameSuffix(record));
    }

    @Override
    public int updateByPrimaryKeyWithBLOBs(Feplog record) {
        return feplogMapper.updateByPrimaryKeyWithBLOBs(this.setTableNameSuffix(record));
    }

    @Override
    public int updateByPrimaryKey(Feplog record) {
        return feplogMapper.updateByPrimaryKey(this.setTableNameSuffix(record));
    }

    @Override
    public int insertBatch(List<Feplog> recordList, int flushStatementsTotal) {
        if (CollectionUtils.isEmpty(recordList)) {
            return -1;
        }
        int result = 0;
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) SpringBeanFactoryUtil.getBean(DataSourceEmsConstant.BEAN_NAME_SQL_SESSION_FACTORY);
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
            int total = 1;
            FeplogMapper mapper = sqlSession.getMapper(FeplogMapper.class);
            for (Feplog record : recordList) {
                result += mapper.insert(this.setTableNameSuffix(record));
                if (flushStatementsTotal > 0 && total++ % flushStatementsTotal == 0) {
                    sqlSession.flushStatements();
                }
            }
            sqlSession.commit();
        } catch (Exception e) {
            DB2EMSUtil.handleBatchExecutorException(e);
            throw e;
        } finally {
            IOUtils.closeQuietly(sqlSession);
        }
        return result;
    }

    @Override
    public int insertSelectiveBatch(List<Feplog> recordList, int flushStatementsTotal) {
        if (CollectionUtils.isEmpty(recordList)) {
            return -1;
        }
        int result = 0;
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) SpringBeanFactoryUtil.getBean(DataSourceEmsConstant.BEAN_NAME_SQL_SESSION_FACTORY);
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
            FeplogMapper mapper = sqlSession.getMapper(FeplogExtMapper.class);
            int total = 1;
            for (Feplog record : recordList) {
                result += mapper.insertSelective(this.setTableNameSuffix(record));
                if (flushStatementsTotal > 0 && total++ % flushStatementsTotal == 0) {
                    sqlSession.flushStatements();
                }
            }
            sqlSession.commit();
        } catch (Exception e) {
            DB2EMSUtil.handleBatchExecutorException(e);
            throw e;
        } finally {
            IOUtils.closeQuietly(sqlSession);
        }
        return result;
    }

    /**
     * 2021-08-29 Richard add for FEPLOG查詢
     *
     * @param feplog
     * @param logDateBegin
     * @param logDateEnd
     * @param ejfnoList
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<Feplog> getMultiFepLogByDef(Feplog feplog, Date logDateBegin, Date logDateEnd, List<Long> ejfnoList, Integer pageNum, Integer pageSize) {
        pageNum = pageNum == null ? 0 : pageNum;
        pageSize = pageSize == null ? 0 : pageSize;
        // 分頁查詢
        PageInfo<Feplog> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                feplogMapper.getMultiFepLogByDef(
                        tableNameSuffix,
                        feplog,
                        logDateBegin,
                        logDateEnd,
                        ejfnoList);
            }
        });
        pageInfo.setList(this.setTableNameSuffix(pageInfo.getList()));
        return pageInfo;
    }

    @Override
    public PageInfo<Feplog> getMultiFepLogByDef_1(Feplog feplog, Integer pageNum, Integer pageSize) {
        pageNum = pageNum == null ? 0 : pageNum;
        pageSize = pageSize == null ? 0 : pageSize;
        // 分頁查詢
        PageInfo<Feplog> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                feplogMapper.getMultiFepLogByDef_019050(
                        tableNameSuffix,
                        feplog);
            }
        });
        pageInfo.setList(this.setTableNameSuffix(pageInfo.getList()));
        return pageInfo;
    }

    @Override
    public List<Feplog> getFeplogByDef(Feplog feplog) {
        return this.setTableNameSuffix(feplogMapper.getFeplogByDef(tableNameSuffix, feplog));
    }

    @Override
    public PageInfo<Feplog> getMultiFEPLogByDef(Map<String, Object> argsMap) {
        int pageNum = argsMap.get("pageNum") == null ? 0 : (int) argsMap.get("pageNum");
        int pageSize = argsMap.get("pageSize") == null ? 0 : (int) argsMap.get("pageSize");
        // 分頁查詢
        PageInfo<Feplog> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                feplogMapper.getFepLog(argsMap);
            }
        });
        pageInfo.setList(this.setTableNameSuffix(pageInfo.getList()));
        return pageInfo;
    }

    @Override
    public int insertBatchEMS(List<Feplog> recordList, int flushStatementsTotal) {
        if (CollectionUtils.isEmpty(recordList)) {
            return -1;
        }
        int result = 0;
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) SpringBeanFactoryUtil.getBean(DataSourceEmsConstant.BEAN_NAME_SQL_SESSION_FACTORY);
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
            FeplogMapper mapper = sqlSession.getMapper(FeplogExtMapper.class);
            int total = 1;
            for (Feplog record : recordList) {
                ((FeplogExt) record).setTableNameSuffix(String.valueOf(CalendarUtil.getDayOfWeek(record.getLogdate() == null ? Calendar.getInstance() : CalendarUtil.clone(record.getLogdate()))));
                result += mapper.insert(record);
                if (flushStatementsTotal > 0 && total++ % flushStatementsTotal == 0) {
                    sqlSession.flushStatements();
                }
            }
            sqlSession.commit();
        } catch (Exception e) {
            DB2EMSUtil.handleBatchExecutorException(e);
            throw e;
        } finally {
            IOUtils.closeQuietly(sqlSession);
        }
        return result;
    }

    @Override
    public int insertBatchEMS(List<Feplog> recordList) {
        if (CollectionUtils.isEmpty(recordList)) {
            return -1;
        }
        int result = 0;
        try {
            for (Feplog record : recordList) {
                ((FeplogExt) record).setTableNameSuffix(String.valueOf(CalendarUtil.getDayOfWeek(record.getLogdate() == null ? Calendar.getInstance() : CalendarUtil.clone(record.getLogdate()))));
                result += feplogMapper.insert(record);
            }
        } catch (Exception e) {
            logger.error(e, "insert error");
            throw e;
        }
        return result;
    }

    @Override
    public int jdbcInsert(List<Feplog> recordList) {
        if (CollectionUtils.isEmpty(recordList)) {
            return -1;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" insert into FEPLOG{0} (LOGDATE, THREADID, EJ, CHANNEL, MESSAGEID, MESSAGEFLOW, PROGRAMFLOW, PROGRAMNAME, STAN, ATMSEQ, ATMNO, TRINBANK, TRINACTNO, TROUTBANK, TROUTACTNO, TXDATE, BKNO, STEPS, TXRQUID, HOSTNAME, TXMESSAGE, REMARK) values")
                .append("(").append(StringUtils.repeat("?", ", ", 22)).append(")");
        String sql = sb.toString();
        int result = 0;
        try {
            String tableNameSuffix;
            for (Feplog record : recordList) {
                tableNameSuffix = String.valueOf(CalendarUtil.getDayOfWeek(record.getLogdate() == null ? Calendar.getInstance() : CalendarUtil.clone(record.getLogdate())));
                result += jdbcTemplate.update(FormatUtil.messageFormat(sql, tableNameSuffix),
                        record.getLogdate(),
                        record.getThreadid(),
                        record.getEj(),
                        record.getChannel(),
                        record.getMessageid(),
                        record.getMessageflow(),
                        record.getProgramflow(),
                        record.getProgramname(),
                        record.getStan(),
                        record.getAtmseq(),
                        record.getAtmno(),
                        record.getTrinbank(),
                        record.getTrinactno(),
                        record.getTroutbank(),
                        record.getTroutactno(),
                        record.getTxdate(),
                        record.getBkno(),
                        record.getSteps(),
                        record.getTxrquid(),
                        record.getHostname(),
                        record.getTxmessage(),
                        record.getRemark()
                );
            }
        } catch (Exception e) {
            logger.error(e, "insert error");
            throw e;
        }
        return result;
    }
}
