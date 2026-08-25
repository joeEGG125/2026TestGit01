package com.syscom.fep.mybatis.interceptor;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import jakarta.annotation.PostConstruct;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.jdbc.BaseJdbcLogger;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.ArrayUtil;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.sql.Array;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "spring.fep.mybatis.interceptor.logger")
@ConditionalOnProperty(prefix = "spring.fep.mybatis.interceptor.logger", name = "enable", havingValue = "true")
// @RefreshScope
@Intercepts({
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
})
public class LoggerInterceptor implements Interceptor {
    private final LogHelper trace = LogHelperFactory.getTraceLogger();
    private final LogHelper jdbc = LogHelperFactory.getJdbcLogger();
    private final String TABLE_NAME_SYSCOMAUDITTRAIL = "SYSCOMAUDITTRAIL";
    /**
     * SQL中哪些欄位名字的值需要進行遮蔽處理
     */
    @Value("#{'${spring.fep.mybatis.interceptor.logger.maskFieldNames:}'.split(',')}")
    private List<String> maskFieldNames;
    private String maskString;
    private String maskCharacter = "*";
    private int maskCharacterLength = 8;
    private static final JdbcTemplate jdbcTemplate = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_JDBC_TEMPLATE);
    private boolean maskFieldInSQL;

    @PostConstruct
    public void postConstruct() {
        // 從Sysconf檔取出MaskFieldInSQL設定, 如果是true, 才會遮蔽處理
        String sysconfValue = null;
        try {
            sysconfValue = jdbcTemplate.queryForObject("SELECT SYSCONF_VALUE FROM SYSCONF WHERE SYSCONF_SUBSYSNO = 9 AND SYSCONF_NAME = 'MaskFieldInSQL'", String.class);
        } catch (Exception e) {
            LogHelperFactory.getTraceLogger().warn("[Get SYSCONF Value] ==> [MaskFieldInSQL] ==> [Fail]");
        }
        this.maskFieldInSQL = Boolean.parseBoolean(sysconfValue);
        LogHelperFactory.getTraceLogger().debug("[LoggerInterceptor]maskFieldInSQL:", maskFieldInSQL);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        if (statementHandler != null) {
            BoundSql boundSql = statementHandler.getBoundSql();
            if (boundSql != null) {
                String sql = SqlSourceBuilder.removeExtraWhitespaces(boundSql.getSql());
                Object args0 = invocation.getArgs()[0];
                sql = this.parseSQL(sql, args0, this.maskFieldInSQL);
                try {
                    BaseStatementHandler baseStatementHandler = ReflectUtil.getFieldValue(statementHandler, "delegate", null);
                    MappedStatement mappedStatement = ReflectUtil.getFieldValue(baseStatementHandler, "mappedStatement", null);
                    jdbc.debug("[", mappedStatement.getId(), "] ==> ", sql);
                } catch (Throwable t) {
                    jdbc.debug("[Execute SQL] ==> ", sql);
                }
            }
        }
        return invocation.proceed();
    }

    /**
     * 解讀SQL, 並針對欄位進行遮蔽處理
     *
     * @param sql
     * @param args0
     * @param maskFieldInSQL
     * @return
     */
    private String parseSQL(String sql, Object args0, boolean maskFieldInSQL) {
        List<Integer> maskFieldIndexes = new ArrayList<>();
        RefBoolean modifiedSQL = new RefBoolean(false);
        boolean isSyscomaudittrail = false;
        if (CollectionUtils.isNotEmpty(maskFieldNames)) {
            try {
                net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(sql);
                if (statement instanceof Select) {
                    Select select = (Select) statement;
                    PlainSelect selectBody = (PlainSelect) select.getSelectBody();
                    Expression where = selectBody.getWhere();
                    if (where != null) {
                        where.accept(new WhereAdapter(maskFieldIndexes, modifiedSQL, maskFieldInSQL));
                    }
                } else if (statement instanceof Insert) {
                    Insert insert = (Insert) statement;
                    List<Column> columns = insert.getColumns();
                    if (CollectionUtils.isNotEmpty(columns)) {
                        if (TABLE_NAME_SYSCOMAUDITTRAIL.equalsIgnoreCase(insert.getTable().getName())) {
                            for (int i = 0; i < columns.size(); i++) {
                                if ("DATAIMAGE".equalsIgnoreCase(columns.get(i).getColumnName())) {
                                    maskFieldIndexes.add(i);
                                    break;
                                }
                            }
                            isSyscomaudittrail = true;
                        } else {
                            for (int i = 0; i < columns.size(); i++) {
                                if (maskFieldNames.contains(columns.get(i).getColumnName())) {
                                    maskFieldIndexes.add(i);
                                }
                            }
                        }
                    }
                } else if (statement instanceof Update) {
                    Update update = (Update) statement;
                    Expression where = update.getWhere();
                    if (where != null) {
                        where.accept(new WhereAdapter(maskFieldIndexes, modifiedSQL, maskFieldInSQL));
                    }
                    List<UpdateSet> updateSets = update.getUpdateSets();
                    if (CollectionUtils.isNotEmpty(updateSets)) {
                        for (UpdateSet updateSet : updateSets) {
                            List<Column> columns = updateSet.getColumns();
                            ExpressionList<?> expressions = updateSet.getValues();
                            if (CollectionUtils.isNotEmpty(columns) && CollectionUtils.isNotEmpty(expressions) && columns.size() == expressions.size()) {
                                for (int i = 0; i < columns.size(); i++) {
                                    Column column = columns.get(i);
                                    Expression expression = expressions.get(i);
                                    this.handleSQLExpression(column, expression, maskFieldIndexes, modifiedSQL, maskFieldInSQL);
                                }
                            } else {
                                trace.warn("cannot handle UpdateSet = [", updateSet.toString(), "]");
                            }
                        }
                    }
                } else if (statement instanceof Delete) {
                    Delete delete = (Delete) statement;
                    Expression where = delete.getWhere();
                    if (where != null) {
                        where.accept(new WhereAdapter(maskFieldIndexes, modifiedSQL, maskFieldInSQL));
                    }
                } else {
                    trace.warn("Ignore Statement:", statement.getClass().getSimpleName(), ", sql=[", sql, "]");
                }
                if (modifiedSQL.get())
                    sql = statement.toString();
            } catch (JSQLParserException e) {
                trace.warn(e, "Parse SQL Failed, sql = [", sql, "]", e.getMessage());
            }
        }
        sql = replaceColumnValue(maskFieldIndexes, args0, sql, isSyscomaudittrail, maskFieldInSQL);
        return sql;
    }

    private String replaceColumnValue(List<Integer> maskFieldIndexes, Object args0, String sql, boolean isSyscomaudittrail, boolean maskFieldInSQL) {
        if (args0 == null)
            return sql;
        try {
            List<Object> columnValues = null;
            if (Proxy.isProxyClass(args0.getClass())) {
                BaseJdbcLogger preparedStatementLogger = (BaseJdbcLogger) Proxy.getInvocationHandler(args0);
                columnValues = ReflectUtil.getFieldValue(preparedStatementLogger, "columnValues", null);
            } else {
                trace.warn(args0.getClass(), " was not Proxy Class!!!");
            }
            if (CollectionUtils.isNotEmpty(columnValues)) {
                for (int i = 0; i < columnValues.size(); i++) {
                    Object columnValue = columnValues.get(i);
                    if (maskFieldInSQL && CollectionUtils.isNotEmpty(maskFieldIndexes) && maskFieldIndexes.contains(i)) {
                        // if (isSyscomaudittrail) {
                        //     sql = StringUtils.replace(sql, "?", this.replaceElementValueForSyscomaudittrailImage(getColumnValue(columnValue)), 1);
                        // } else {
                        sql = StringUtils.replace(sql, "?", this.getMaskString(true), 1);
                        // }
                    } else {
                        sql = StringUtils.replace(sql, "?", this.getColumnValue(columnValue), 1);
                    }
                }
            }
        } catch (Throwable t) {
            trace.warn(t, t.getMessage());
        }
        return sql;
    }

    // private String replaceElementValueForSyscomaudittrailImage(String columnValue) {
    //     RefBoolean found = new RefBoolean(false);
    //     try {
    //         Element root = XmlUtil.load(columnValue.substring(1, columnValue.length() - 1));
    //         if (root != null) {
    //             this.replaceElementValueForSyscomaudittrailImage(root, found);
    //             return found.get() ? StringUtils.join("'", XmlUtil.toString(root), "'") : columnValue;
    //         }
    //     } catch (Exception e) {
    //         trace.warn(e, e.getMessage());
    //     }
    //     return this.getMaskString(true);
    // }

    // private void replaceElementValueForSyscomaudittrailImage(Element element, RefBoolean found) {
    //     List<Element> children = element.getChildren();
    //     if (CollectionUtils.isNotEmpty(children)) {
    //         for (Element child : children) {
    //             this.replaceElementValueForSyscomaudittrailImage(child, found);
    //         }
    //     } else {
    //         if (maskFieldNames.contains(element.getName())) {
    //             element.setText(this.getMaskString(false));
    //             found.set(true);
    //         }
    //     }
    // }

    private String getColumnValue(Object columnValue) {
        if (columnValue == null) {
            return "null";
        }
        String value = null;
        if (columnValue instanceof Array) {
            try {
                value = ArrayUtil.toString(((Array) columnValue).getArray());
            } catch (SQLException e) {
                trace.warn(e, e.getMessage());
            }
        } else if (columnValue instanceof StringReader) {
            // StringReader reader = (StringReader) columnValue;
            // try {
            //     reader.reset();
            // } catch (IOException e) {
            //     trace.warn(e, e.getMessage());
            // }
            // try {
            //     value = IOUtils.toString(reader);
            // } catch (IOException e) {
            //     trace.warn(e, e.getMessage());
            // }
            value = StringUtils.join("<", columnValue.toString(), ">");
        }
        if (value == null) {
            value = columnValue.toString();
        }
        if (columnValue instanceof String || columnValue instanceof Character || columnValue instanceof StringBuilder || columnValue instanceof Timestamp || columnValue instanceof StringReader) {
            return StringUtils.join("'", value, "'");
        }
        return value;
    }

    public List<String> getMaskFieldNames() {
        return maskFieldNames;
    }

    public void setMaskFieldNames(List<String> maskFieldNames) {
        this.maskFieldNames = maskFieldNames;
    }

    public String getMaskString(boolean quote) {
        if (StringUtils.isBlank(maskString)) {
            maskString = StringUtils.join("MASK[", StringUtils.repeat(maskCharacter, maskCharacterLength), "]");
        }
        if (quote)
            return StringUtils.join("'", maskString, "'");
        return maskString;
    }

    public void setMaskString(String maskString) {
        this.maskString = maskString;
    }

    public String getMaskCharacter() {
        return maskCharacter;
    }

    public void setMaskCharacter(String maskCharacter) {
        this.maskCharacter = maskCharacter;
    }

    public int getMaskCharacterLength() {
        return maskCharacterLength;
    }

    public void setMaskCharacterLength(int maskCharacterLength) {
        this.maskCharacterLength = maskCharacterLength;
    }

    private class WhereAdapter extends ExpressionVisitorAdapter {
        private final List<Integer> maskFieldIndexes;
        private final RefBoolean modifiedSQL;
        private final boolean maskFieldInSQL;

        public WhereAdapter(List<Integer> maskFieldIndexes, RefBoolean modifiedSQL, boolean maskFieldInSQL) {
            this.maskFieldIndexes = maskFieldIndexes;
            this.modifiedSQL = modifiedSQL;
            this.maskFieldInSQL = maskFieldInSQL;
        }

        @Override
        public void visit(EqualsTo expr) {
            this.handleExpression(expr);
        }

        @Override
        public void visit(NotEqualsTo expr) {this.handleExpression(expr);}

        @Override
        public void visit(GreaterThan expr) {
            this.handleExpression(expr);
        }

        @Override
        public void visit(GreaterThanEquals expr) {
            this.handleExpression(expr);
        }

        @Override
        public void visit(MinorThan expr) {
            this.handleExpression(expr);
        }

        @Override
        public void visit(MinorThanEquals expr) {
            this.handleExpression(expr);
        }

        @Override
        public void visit(LikeExpression expr) {
            this.handleExpression(expr);
        }

        @Override
        public void visit(InExpression expr) {
            Expression left = expr.getLeftExpression();
            Expression right = expr.getRightExpression();
            if (left instanceof Column && right instanceof ExpressionList<?>) {
                ExpressionList<?> expressionList = (ExpressionList<?>) right;
                handleSQLExpression((Column) left, expressionList, maskFieldIndexes, modifiedSQL, maskFieldInSQL);
            } else {
                trace.warn("cannot handle InExpression = [", expr.toString(), "]");
            }
        }

        private void handleExpression(BinaryExpression expr) {
            Expression left = expr.getLeftExpression();
            Expression right = expr.getRightExpression();
            if (left instanceof Column) {
                handleSQLExpression((Column) left, right, maskFieldIndexes, modifiedSQL, maskFieldInSQL);
            } else if (right instanceof Column) {
                handleSQLExpression((Column) right, left, maskFieldIndexes, modifiedSQL, maskFieldInSQL);
            } else {
                trace.warn("cannot handle BinaryExpression = [", expr.toString(), "]");
            }
        }
    }

    private void handleSQLExpression(Column column, Expression expression, List<Integer> maskFieldIndexes, RefBoolean modifiedSQL, boolean maskFieldInSQL) {
        this.handleSQLExpression(column, new ArrayList<>(Collections.singletonList(expression)), maskFieldIndexes, modifiedSQL, maskFieldInSQL);
    }

    private void handleSQLExpression(Column column, List<Expression> expressions, List<Integer> maskFieldIndexes, RefBoolean modifiedSQL, boolean maskFieldInSQL) {
        if (maskFieldInSQL && maskFieldNames.contains(column.getColumnName())) {
            for (Expression expression : expressions) {
                if (expression instanceof JdbcParameter) {
                    maskFieldIndexes.add(((JdbcParameter) expression).getIndex() - 1);
                } else if (expression instanceof StringValue) {
                    ((StringValue) expression).setValue(getMaskString(false));
                    modifiedSQL.set(true);
                } else {
                    trace.warn("Cannot handle [", column.getColumnName(), "] with value [", StringUtils.join(expressions, ","), "]");
                }
            }
        }
    }
}
