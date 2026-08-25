package com.syscom.fep.mybatis.deslog.dao.impl;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.deslog.configuration.DataSourceDeslogConstant;
import com.syscom.fep.mybatis.deslog.dao.DeslogDao;
import com.syscom.fep.mybatis.deslog.ext.mapper.DeslogExtMapper;
import com.syscom.fep.mybatis.deslog.mapper.DeslogMapper;
import com.syscom.fep.mybatis.deslog.model.Deslog;
import com.syscom.fep.mybatis.deslog.util.DB2DESLOGUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Calendar;
import java.util.List;

@Repository
@Lazy
public class DeslogDaoImpl implements DeslogDao, DataSourceDeslogConstant {
    @Autowired
    private DeslogMapper mapper;
    @Qualifier(BEAN_NAME_JDBC_TEMPLATE)
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * @param recordList
     * @param flushStatementsTotal
     * @return
     */
    @Override
    public int insertBatch(List<Deslog> recordList, int flushStatementsTotal) {
        if (CollectionUtils.isEmpty(recordList)) {
            return -1;
        }
        int result = 0;
        SqlSessionFactory sqlSessionFactory = SpringBeanFactoryUtil.getBean(BEAN_NAME_SQL_SESSION_FACTORY);
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
            DeslogExtMapper mapper = sqlSession.getMapper(DeslogExtMapper.class);
            int total = 1;
            for (Deslog record : recordList) {
                result += mapper.insert(record);
                if (flushStatementsTotal > 0 && total++ % flushStatementsTotal == 0) {
                    sqlSession.flushStatements();
                }
            }
            sqlSession.commit();
        } catch (Exception e) {
            DB2DESLOGUtil.handleBatchExecutorException(e);
            throw e;
        } finally {
            IOUtils.closeQuietly(sqlSession);
        }
        return result;
    }

    @Override
    public int insertBatch(List<Deslog> recordList) {
        if (CollectionUtils.isEmpty(recordList)) {
            return -1;
        }
        int result = 0;
        try {
            for (Deslog record : recordList) {
                result += mapper.insert(record);
            }
        } catch (Exception e) {
            LogHelperFactory.getRepositoryLogger().error(e, "insert error");
            throw e;
        }
        return result;
    }

    @Override
    public int jdbcInsert(List<Deslog> recordList) {
        if (CollectionUtils.isEmpty(recordList)) {
            return -1;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" insert into DESLOG{0} (DESLOG_FUNC, DESLOG_KEYID, DESLOG_INPUTDATA1, DESLOG_INPUTDATA2, DESLOG_OUTPUTDATA1, DESLOG_OUTPUTDATA2, DESLOG_RC, DESLOG_SUIPCOMMAND, DESLOG_CALLOBJECT, " +
                        "DESLOG_PROGRAMNAME, DESLOG_EJ, DESLOG_PROGRAMFLOW, DESLOG_REMARK, DESLOG_UPDATETIME, TXRQUID, HOSTNAME) values")
                .append("(").append(StringUtils.repeat("?", ", ", 16)).append(")");
        String sql = sb.toString();
        int result = 0;
        try {
            String tableNameSuffix;
            for (Deslog record : recordList) {
                tableNameSuffix = String.valueOf(CalendarUtil.getDayOfWeek(record.getDeslogUpdatetime() == null ? Calendar.getInstance() : CalendarUtil.clone(record.getDeslogUpdatetime())));
                result += jdbcTemplate.update(FormatUtil.messageFormat(sql, tableNameSuffix),
                        record.getDeslogFunc(),
                        record.getDeslogKeyid(),
                        record.getDeslogInputdata1(),
                        record.getDeslogInputdata2(),
                        record.getDeslogOutputdata1(),
                        record.getDeslogOutputdata2(),
                        record.getDeslogRc(),
                        record.getDeslogSuipcommand(),
                        record.getDeslogCallobject(),
                        record.getDeslogProgramname(),
                        record.getDeslogEj(),
                        record.getDeslogProgramflow(),
                        record.getDeslogRemark(),
                        record.getDeslogUpdatetime(),
                        record.getTxrquid(),
                        record.getHostname()
                );
            }
        } catch (Exception e) {
            LogHelperFactory.getRepositoryLogger().error(e, "insert error");
            throw e;
        }
        return result;
    }
}
