package com.syscom.fep.mybatis.interceptor;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.SyscomaudittrailMapper;
import com.syscom.fep.mybatis.model.Syscomaudittrail;
import com.syscom.fep.mybatis.vo.MybatisMethod;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 用來記錄異動資料
 *
 * @author Richard
 */
@Component
@ConfigurationProperties(prefix = "spring.fep.mybatis.interceptor.audittrail")
@ConditionalOnProperty(prefix = "spring.fep.mybatis.interceptor.audittrail", name = "enable", havingValue = "true")
// @RefreshScope
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class AuditTrailInterceptor implements Interceptor {
    private static final String CLASS_NAME = AuditTrailInterceptor.class.getSimpleName();
    private static final LogHelper log = LogHelperFactory.getGeneralLogger();
    private static final String FIELD_NAME_TABLENAMESUFFIX = "tableNameSuffix";
    private static final String FIELD_NAME_UPDATEUSER = "updateUser";
    private static final String FIELD_NAME_LOGAUDITTRAIL = "logAuditTrail";
    private static final String METHOD_NAME_FIELDSTOXML = "fieldsToXml";
    private InsertAuditTrailThread insertAuditTrailThread = null;
    @Value("#{'${spring.fep.mybatis.interceptor.audittrail.ignore-tablename-prefix-list:SYSCOMAUDITTRAIL}'.split(',')}")
    private List<String> auditIgnoreTableNamePrefixList;

    @PostConstruct
    public void initialization() {
        log.info("[", CLASS_NAME, "]start to intercept...");
        insertAuditTrailThread = new InsertAuditTrailThread();
    }

    @PreDestroy
    public void destroy() {
        log.warn("[", CLASS_NAME, "]start to destroy...");
        insertAuditTrailThread.terminate();
    }

    public List<String> getAuditIgnoreTableNamePrefixList() {
        return auditIgnoreTableNamePrefixList;
    }

    public void setAuditIgnoreTableNamePrefixList(List<String> auditIgnoreTableNamePrefixList) {
        this.auditIgnoreTableNamePrefixList = auditIgnoreTableNamePrefixList;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        insertAuditTrailThread.intercept(invocation);
        return invocation.proceed();
    }

    private class InsertAuditTrailThread extends Thread {
        private final List<Invocation> list = new ArrayList<>();
        private boolean running = true;

        public InsertAuditTrailThread() {
            super("InsertAuditTrailThread");
            setPriority(Thread.NORM_PRIORITY);
            start();
        }

        public void intercept(Invocation invocation) {
            synchronized (list) {
                list.add(invocation);
                if (list.size() == 1) {
                    list.notifyAll();
                }
            }
        }

        public void terminate() {
            this.running = false;
            // list要notifyAll一下
            synchronized (this.list) {
                this.list.notifyAll();
            }
        }

        @Override
        public void run() {
            while (this.running) {
                try {
                    Invocation[] invocations;
                    synchronized (list) {
                        if (list.isEmpty()) {
                            try {
                                list.wait(600000);
                            } catch (InterruptedException e) {
                                log.warn(e, e.getMessage());
                            }
                        }
                        invocations = new Invocation[list.size()];
                        list.toArray(invocations);
                        list.clear();
                    }
                    if (ArrayUtils.isNotEmpty(invocations)) {
                        for (Invocation invocation : invocations) {
                            this.process(invocation);
                        }
                        invocations = null;
                    }
                } catch (Throwable e) {
                    log.warn(e, e.getMessage());
                }
            }
        }

        private void process(Invocation invocation) {
            String methodNameForLogging = StringUtils.join("[", CLASS_NAME, ".process]");
            try {
                MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
                // 含有mapper類名和方法名
                String mapperClassNameAndMethod = ms.getId();
                // 取出mapper類名
                String mapperClassName = mapperClassNameAndMethod.substring(0, mapperClassNameAndMethod.lastIndexOf("."));
                // 獲取mapper類
                Class<?> mapplerClass = Class.forName(mapperClassName);
                // 忽略的表則不記錄
                if (CollectionUtils.containsAny(auditIgnoreTableNamePrefixList, this.getTableNamePrefix(mapplerClass.getSimpleName()))) {
                    return;
                }
                // 取出mapper方法名
                String mapperClassMethod = mapperClassNameAndMethod.substring(mapperClassNameAndMethod.lastIndexOf(".") + 1);
                // for logging
                String mapperClassNameForLogging = StringUtils.join("mapper: ", mapperClassNameAndMethod);
                // 目前只記錄這三種方法操作資料庫的異動日誌
                if (!(MybatisMethod.UPDATE_BY_PRIMARY_KEY.getName().equals(mapperClassMethod)
                        || MybatisMethod.UPDATE_BY_PRIMARY_KEY_SELECTIVE.getName().equals(mapperClassMethod)
                        || MybatisMethod.UPDATE_BY_PRIMARY_KEY_WITH_BLOBS.getName().equals(mapperClassMethod)
                        || MybatisMethod.DELETE_BY_PRIMARY_KEY.getName().equals(mapperClassMethod))) {
                    throw ExceptionUtil.createUnsupportedOperationException(mapperClassNameForLogging, ", cannot insert audit trail for method \"", mapperClassMethod, "\"");
                }
                // SQL指令
                SqlCommandType commandType = ms.getSqlCommandType();
                if (!(commandType == SqlCommandType.UPDATE || commandType == SqlCommandType.DELETE)) {
                    throw ExceptionUtil.createUnsupportedOperationException(mapperClassNameForLogging, ", cannot insert audit trail for sql command type \"", commandType.name(), "\"");
                }
                Method selectByPrimaryKeyMethod = this.getSelectByPrimaryKeyMethod(mapplerClass, mapperClassNameForLogging);
                // 取出mapper的selectByPrimaryKey方法中所有的參數
                Parameter[] parametersForSelectByPrimaryKeyMethod = selectByPrimaryKeyMethod.getParameters();
                if (ArrayUtils.isEmpty(parametersForSelectByPrimaryKeyMethod)) {
                    throw ExceptionUtil.createException(mapperClassNameForLogging, ", the parameter for method \"", MybatisMethod.SELECT_BY_PRIMARY_KEY.getName(), "\" is empty!!!");
                }
                // 更新操作 & 刪除操作
                if (MybatisMethod.UPDATE_BY_PRIMARY_KEY.getName().equals(mapperClassMethod)
                        || MybatisMethod.UPDATE_BY_PRIMARY_KEY_SELECTIVE.getName().equals(mapperClassMethod)
                        || MybatisMethod.UPDATE_BY_PRIMARY_KEY_WITH_BLOBS.getName().equals(mapperClassMethod)
                        || MybatisMethod.DELETE_BY_PRIMARY_KEY.getName().equals(mapperClassMethod)) {
                    this.processByEntity(invocation, methodNameForLogging, mapperClassNameForLogging, commandType, mapplerClass, selectByPrimaryKeyMethod, parametersForSelectByPrimaryKeyMethod, mapperClassMethod);
                }
                // 其他操作
                else {

                }
            } catch (Throwable e) {
                // log.warn(methodNameForLogging, e.getMessage());
            }
        }

        private Method getSelectByPrimaryKeyMethod(Class<?> mapplerClass, String mapperClassNameForLogging) throws Exception {
            // 透過反射取出mapper下所有的方法
            Method[] methods = ReflectionUtils.getAllDeclaredMethods(mapplerClass);
            if (ArrayUtils.isEmpty(methods)) {
                throw ExceptionUtil.createException(mapperClassNameForLogging, ", no any methods");
            }
            // 取出mapper的selectByPrimaryKey方法
            Method selectByPrimaryKeyMethod = null;
            for (Method method : methods) {
                if (MybatisMethod.SELECT_BY_PRIMARY_KEY.getName().equals(method.getName())) {
                    selectByPrimaryKeyMethod = method;
                    break;
                }
            }
            if (selectByPrimaryKeyMethod == null) {
                throw ExceptionUtil.createException(mapperClassNameForLogging, ", cannot find method \"", MybatisMethod.SELECT_BY_PRIMARY_KEY.getName(), "\"");
            }
            return selectByPrimaryKeyMethod;
        }

        /**
         * @param invocation
         * @param methodNameForLogging
         * @param mapperClassNameForLogging
         * @param commandType
         * @param mapplerClass
         * @param selectByPrimaryKeyMethod
         * @param parametersForSelectByPrimaryKeyMethod
         * @param mapperClassMethod
         * @throws Exception
         */
        private void processByEntity(
                Invocation invocation,
                String methodNameForLogging,
                String mapperClassNameForLogging,
                SqlCommandType commandType,
                Class<?> mapplerClass,
                Method selectByPrimaryKeyMethod,
                Parameter[] parametersForSelectByPrimaryKeyMethod,
                String mapperClassMethod) throws Exception {
            Object entityAfter = invocation.getArgs()[1];
            if (entityAfter == null) {
                throw ExceptionUtil.createException(mapperClassNameForLogging, ", entity is null!!!");
            }
            // 從實例類中取出updateUser和logAuditTrail欄位的值
            long updateUser = ReflectUtil.getFieldValue(entityAfter, FIELD_NAME_UPDATEUSER, 0L);
            boolean logAuditTrail = ReflectUtil.getFieldValue(entityAfter, FIELD_NAME_LOGAUDITTRAIL, false);
            // 如果updateUser沒有設置, 並且logAuditTrail為false, 則表示不需要記錄
            needInsertAuditTrail(updateUser, logAuditTrail, mapperClassNameForLogging, mapperClassMethod);
            Class<?> entityClass = entityAfter.getClass();
            String entityClassName = entityClass.getName();
            String entityClassNameForLogging = StringUtils.join("entity: ", entityClassName);
            log.info(methodNameForLogging, ", ", mapperClassNameForLogging, ", methodName: ", invocation.getMethod().getName(), ", commandType: ", commandType, ", ", entityClassNameForLogging);
            List<Field> fieldList = new ArrayList<Field>();
            for (Parameter parameterForSelectByPrimaryKeyMethod : parametersForSelectByPrimaryKeyMethod) {
                String fieldName = null;
                // 取出該參數的Param注入
                Param param = parameterForSelectByPrimaryKeyMethod.getAnnotation(Param.class);
                if (param != null) {
                    // 通過注入的名稱得到entity對應的property name
                    fieldName = param.value();
                    if (StringUtils.isBlank(fieldName)) {
                        throw ExceptionUtil.createException(mapperClassNameForLogging, "the value of annotation \"org.apache.ibatis.annotations.Param\" for parameter \"", parameterForSelectByPrimaryKeyMethod.getName(), "\" is empty!!");
                    }
                } else {
                    log.warn(methodNameForLogging, ", ", mapperClassNameForLogging, ", cannot find annotation \"org.apache.ibatis.annotations.Param\" for parameter \"", parameterForSelectByPrimaryKeyMethod.getName(), "\"");
                    fieldName = parameterForSelectByPrimaryKeyMethod.getName();
                }
                // 通過property name透過反射取得field
                Field field = ReflectionUtils.findField(entityClass, fieldName);
                if (field == null) {
                    throw ExceptionUtil.createException(mapperClassNameForLogging, ", ", entityClassNameForLogging, ", cannot find field \"", fieldName, "\"");
                }
                fieldList.add(field);
            }
            // 通過mapper的selectByPrimaryKey方法中所有的參數，從entityAfter取出對應的值，並通過selectByPrimaryKey方法取得更新前實例
            Object[] args = new Object[fieldList.size()];
            for (int i = 0; i < fieldList.size(); i++) {
                Field field = fieldList.get(i);
                ReflectionUtils.makeAccessible(field);
                args[i] = ReflectionUtils.getField(field, entityAfter);
            }
            // 取得mapper的實例
            Object mapper = SpringBeanFactoryUtil.getBean(mapplerClass);
            // 通過selectByPrimaryKey取得異動前的資料, 注意有可能查不到哦
            Object entityBefore = ReflectionUtils.invokeMethod(selectByPrimaryKeyMethod, mapper, args);
            // check一下entity是否有tableNameSuffix欄位
            String tableNameSuffix = ReflectUtil.getFieldValue(entityAfter, FIELD_NAME_TABLENAMESUFFIX, StringUtils.EMPTY);
            // 將異動寫入表中
            this.insertAuditTrail(mapplerClass.getSimpleName(), tableNameSuffix, commandType, args, entityBefore, entityAfter, updateUser);
        }

        // /**
        //  * @param invocation
        //  * @param methodNameForLogging
        //  * @param mapperClassNameForLogging
        //  * @param mapplerClass
        //  * @param selectByPrimaryKeyMethod
        //  * @param parameters
        //  * @param commandType
        //  * @throws Exception
        //  */
        // @SuppressWarnings("unused")
        // private void processByPrimaryKey(
        //         Invocation invocation,
        //         String methodNameForLogging,
        //         String mapperClassNameForLogging,
        //         Class<?> mapplerClass,
        //         Method selectByPrimaryKeyMethod,
        //         Parameter[] parameters,
        //         SqlCommandType commandType,
        //         String mapperClassMethod) throws Exception {
        //     @SuppressWarnings("unchecked")
        //     Map<String, Object> parameterMap = (Map<String, Object>) invocation.getArgs()[1];
        //     if (parameterMap == null) {
        //         throw ExceptionUtil.createException(mapperClassNameForLogging, ", method parameter map is null!!!");
        //     }
        //     log.info(methodNameForLogging, mapperClassNameForLogging, ", methodName; ", invocation.getMethod().getName(), ", commandType: ", commandType, ", method parameter map: ", parameterMap.toString());
        //     List<Object> primaryKeyList = new ArrayList<>();
        //     long updateUser = 0;
        //     boolean logAuditTrail = false;
        //     for (int i = 0; i < parameters.length; i++) {
        //         Parameter parameter = parameters[i];
        //         // 取出該參數的Param注入
        //         Param param = parameter.getAnnotation(Param.class);
        //         if (param == null) {
        //             throw ExceptionUtil.createException(mapperClassNameForLogging, ", cannot find annotation \"org.apache.ibatis.annotations.Param\" for parameter \"", parameter.getName(), "\"");
        //         }
        //         // 通過注入的名稱得到entity對應的property name
        //         String fieldName = param.value();
        //         if (StringUtils.isBlank(fieldName)) {
        //             throw ExceptionUtil.createException(mapperClassNameForLogging, ", the value of annotation \"org.apache.ibatis.annotations.Param\" for parameter \"", parameter.getName(), "\" is empty!!");
        //         }
        //         Object parameterValue = parameterMap.get(fieldName);
        //         if (parameterValue == null) {
        //             throw ExceptionUtil.createException("the value of \"", fieldName, "\" is not exist in Parameter Map");
        //         }
        //         if (MybatisParam.UPDATUSER.getName().equals(fieldName)) {
        //             updateUser = (long) parameterValue;
        //         } else if (MybatisParam.LOGAUDITTRAIL.getName().equals(fieldName)) {
        //             logAuditTrail = (boolean) parameterValue;
        //         } else {
        //             primaryKeyList.add(parameterValue);
        //         }
        //     }
        //     // 如果updateUser沒有設置, 並且logAuditTrail為false, 則表示不需要記錄
        //     needInsertAuditTrail(updateUser, logAuditTrail, mapperClassNameForLogging, mapperClassMethod);
        //     // 取得mapper的實例
        //     Object mapper = SpringBeanFactoryUtil.getBean(mapplerClass);
        //     // 通過selectByPrimaryKey取得異動前的資料
        //     Object[] args = new Object[primaryKeyList.size()];
        //     primaryKeyList.toArray(args);
        //     Object entityBefore = ReflectionUtils.invokeMethod(selectByPrimaryKeyMethod, mapper, args);
        //     // check一下entity是否有tableNameSuffix欄位
        //     String tableNameSuffix = (String) parameterMap.get(FIELD_NAME_TABLENAMESUFFIX);
        //     // 將異動寫入表中
        //     this.insertAuditTrail(mapplerClass.getSimpleName(), tableNameSuffix, commandType, args, entityBefore, null, updateUser);
        // }

        /**
         * 記錄異動
         *
         * @param mapperClassSimpleName
         * @param tableNameSuffix
         * @param commandType
         * @param args
         * @param entityBefore
         * @param entityAfter
         * @param updateUser
         */
        private void insertAuditTrail(String mapperClassSimpleName, String tableNameSuffix, SqlCommandType commandType, Object[] args, Object entityBefore, Object entityAfter, long updateUser) {
            String logId = UUID.randomUUID().toString();
            int userid = (int) updateUser;
            Date updateTime = Calendar.getInstance().getTime();
            String accessType = commandType.name();
            String updateTable = StringUtils.join(this.getTableNamePrefix(mapperClassSimpleName), StringUtils.isBlank(tableNameSuffix) ? StringUtils.EMPTY : tableNameSuffix);
            String updatePrimaryKey = StringUtils.join(args, ",");
            StringBuilder sb = new StringBuilder();
            sb.append("<Before>");
            if (commandType == SqlCommandType.INSERT) {
                // nop
            } else if (entityBefore != null) {
                sb.append(ReflectUtil.envokeMethod(entityBefore, METHOD_NAME_FIELDSTOXML, StringUtils.EMPTY));
            }
            sb.append("</Before>");
            sb.append("<After>");
            if (commandType == SqlCommandType.DELETE) {
                // nop
            } else if (entityAfter != null) {
                sb.append(ReflectUtil.envokeMethod(entityAfter, METHOD_NAME_FIELDSTOXML, StringUtils.EMPTY));
            }
            sb.append("</After>");
            String dataImage = StringUtils.join("<Data><Row>", sb.toString(), "</Row></Data>");
            Syscomaudittrail record = new Syscomaudittrail(logId, userid, updateTime, accessType, updateTable, updatePrimaryKey, dataImage);
            SyscomaudittrailMapper syscomaudittrailMapper = SpringBeanFactoryUtil.getBean(SyscomaudittrailMapper.class);
            syscomaudittrailMapper.insert(record);
        }

        private void needInsertAuditTrail(long updateUser, boolean logAuditTrail, String mapperClassNameForLogging, String mapperClassMethod) {
            boolean need = (updateUser > 0 || logAuditTrail);
            if (!need) {
                throw ExceptionUtil.createUnsupportedOperationException(
                        mapperClassNameForLogging, ", no need insert audit trail for method \"", mapperClassMethod, "\", updateUser = [", updateUser, "], logAuditTrail = [", logAuditTrail, "]");
            }
        }

        private String getTableNamePrefix(String mapperClassSimpleName) {
            int index = mapperClassSimpleName.lastIndexOf("ExtMapper");
            return index > 0 ? mapperClassSimpleName.substring(0, index).toUpperCase() : mapperClassSimpleName.substring(0, mapperClassSimpleName.lastIndexOf("Mapper")).toUpperCase();
        }
    }
}
