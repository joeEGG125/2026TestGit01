package com.syscom.fep.web.interceptor;

import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.WebauditExtMapper;
import com.syscom.fep.mybatis.model.Webaudit;
import com.syscom.fep.mybatis.util.DB2Util;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.base.FEPWebBase;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MappingURI;
import com.syscom.fep.web.entity.SessionKey;
import com.syscom.fep.web.entity.User;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 用來記錄WebAudit的攔截器
 * <p>
 * 當配置檔中設定spring.fep.web.interceptor.audit.enableAudit=true時, 這個攔截器才會起作用
 */
@Component
@ConfigurationProperties(prefix = "spring.fep.web.interceptor.audit")
@ConditionalOnProperty(prefix = "spring.fep.web.interceptor.audit", name = "enableAudit", havingValue = "true")
// @RefreshScope
public class WebAuditInterceptor extends FEPWebBase implements HandlerInterceptor {
    private final LogHelper logger = LogHelperFactory.getGeneralLogger();
    private boolean enableAudit = true;
    private boolean showAudit = false;
    private long flushIntervalInMilliseconds = 5000L;
    private int flushStatementsTotal = 1000;
    private int dbFieldLengthLimitAdjustment = 10;
    private InsertWebAuditThread insertWebAuditThread = null;
    private final List<String> excludeURIs = Arrays.asList(
            MappingURI.Prevpage, MappingURI.PrevpageAjax, MappingURI.Clear, MappingURI.DoPageChanged, MappingURI.SidebarCollapse, MappingURI.Change, MappingURI.ConditionPanelCollapse,
            MappingURI.DoColumnSort, MappingURI.CurrentPageAjax, MappingURI.CurrentPage, MappingURI.SelectMenu, MappingURI.Exit);
    private final List<String> excludeRegExURIs = Arrays.asList(
            MappingURI.SM
    );
    private static final int FIELD_AUDIT_DATA_LENGTH_LIMIT = 1048576;
    private static final int FIELD_SQL_COMMAND_TEXT_LENGTH_LIMIT = 1048576;

    @PostConstruct
    public void init() {
        logger.info("WebAuditInterceptor start to initialize...");
        insertWebAuditThread = new InsertWebAuditThread();
        logger.info(ConfigurationPropertiesUtil.info(this, "WebAuditInterceptor", "insertWebAuditThread"));
    }

    @PreDestroy
    public void destroy() {
        logger.info("WebAuditInterceptor start to destroy...");
        this.insertWebAuditThread.terminate();
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // ThreadLocal<AuditLog> auditLogThreadLocal = null;
        try {
            String requestURI = WebUtil.getRequest().getRequestURI();
            // 跳過一些不需要記錄的URL
            if (excludeURIs.stream().filter(requestURI::endsWith).findFirst().orElse(null) != null) {
                return;
            }
            if (excludeRegExURIs.stream().filter(requestURI::matches).findFirst().orElse(null) != null) {
                return;
            }
            // auditData
            String auditData = null;
            // 如果是Ajax
            if ((handler instanceof HandlerMethod && WebUtil.isAjax(request, (HandlerMethod) handler)) || WebUtil.isAjax(request)) {
                byte[] bytes = null;
                if (request instanceof ContentCachingRequestWrapper) {
                    bytes = ((ContentCachingRequestWrapper) request).getContentAsByteArray();
                } else {
                    Object requestWrapper = request;
                    while (true) {
                        requestWrapper = ReflectUtil.getFieldValue(requestWrapper, "request", null);
                        if (requestWrapper == null)
                            break;
                        if (requestWrapper instanceof ContentCachingRequestWrapper)
                            break;
                    }
                    if (requestWrapper == null)
                        logger.warn("Unrecognizable request = [", request.getClass(), "], cannot get request body.");
                    else
                        bytes = ((ContentCachingRequestWrapper) requestWrapper).getContentAsByteArray();
                }
                if (ArrayUtils.isNotEmpty(bytes)) {
                    String charsetName = request.getCharacterEncoding();
                    if (StringUtils.isNotBlank(charsetName)) {
                        try {
                            auditData = new String(bytes, charsetName);
                        } catch (UnsupportedEncodingException e) {
                            auditData = new String(bytes, StandardCharsets.UTF_8);
                        }
                    } else {
                        auditData = new String(bytes, StandardCharsets.UTF_8);
                    }
                }
            }
            // 如果是表單提交
            else {
                if (modelAndView != null) {
                    Map<String, Object> map = modelAndView.getModel();
                    Object form = map.get(AttributeName.Form.toString());
                    if (form == null) {
                        form = modelAndView.getModel();
                    }
                    if (form instanceof Map && (MapUtils.isEmpty((Map<?, ?>) form))) {
                        form = request.getParameterMap();
                    }
                    auditData = this.getAuditData(form);
                }
            }
            // auditUser
            String auditUser = WebUtil.getFepuser() != null ? WebUtil.getFepuser().getFepuserLogonid() : null;
            if (StringUtils.isBlank(auditUser)) {
                if (MappingURI.Logon.equals(requestURI)) {
                    auditUser = modelAndView != null ? WebUtil.getFromAttribute(modelAndView.getModel(), AttributeName.UserId) : null;
                } else if (MappingURI.Logout.equals(requestURI)) {
                    auditUser = modelAndView != null ? WebUtil.removeFromAttribute(modelAndView.getModel(), AttributeName.LogoutUserId) : null;
                }
            }
            // sqlcommandtext
            String sqlcommandtext = null;
            if (handler instanceof HandlerMethod) {
                // 根據handler取出對應的Controller程式, 進而取出ThreadLocal<AuditLog> auditLogThreadLocal物件
                // auditLogThreadLocal = ReflectUtil.getFieldValue(((HandlerMethod) handler).getBean(), "auditLogThreadLocal", null);
                // if (auditLogThreadLocal != null) {
                // AuditLog auditLog = auditLogThreadLocal.get();
                AuditLog auditLog = WebUtil.getFromSession(SessionKey.AuditLog);
                if (auditLog != null) {
                    sqlcommandtext = auditLog.toString();
                    // auditLogThreadLocal.remove(); // 這裡記得一定要移除掉
                }
                // }
            }
            User user = WebUtil.getUser();
            Webaudit audit = new Webaudit();
            audit.setAuditpage(requestURI);
            audit.setAudituser(auditUser); // 避免長度超過DB限制
            audit.setAudittime(Calendar.getInstance().getTime());
            audit.setAuditrequesthost(FEPConfig.getInstance().getHostName());
            audit.setAuditprogramname(user != null && user.getSelectedMenu() != null ? user.getSelectedMenu().getName() : null);
            audit.setAudituseraddress(user != null ? user.getSrcIp() : WebUtil.getRemoteClientIp());
            audit.setShowaudit(DbHelper.toShort(this.showAudit));
            audit.setShowquerydata(DbHelper.toShort(false)); // TODO 這個欄位原始.NET程式沒有找到哪裡塞入
            audit.setAuditdate(Integer.toString(CalendarUtil.dateValue(Calendar.getInstance())));
            audit.setTxclass("Q");
            audit.setProgramid(user != null && user.getSelectedMenu() != null ? user.getSelectedMenu().getCode() : null);
            audit.setSqlcommandtext(StringUtil.substring(sqlcommandtext, FIELD_SQL_COMMAND_TEXT_LENGTH_LIMIT - dbFieldLengthLimitAdjustment)); // 避免長度超過DB限制
            audit.setAuditbrowser(user != null ? user.getBrowser() : WebUtil.getBrowser());
            audit.setAuditdata(StringUtil.substring(auditData, FIELD_AUDIT_DATA_LENGTH_LIMIT - dbFieldLengthLimitAdjustment)); // 避免長度超過DB限制
            insertWebAuditThread.addWebaudit(audit);
        } finally {
            // 避免上面出現Exception, 造成auditLogThreadLocal沒有被移除
            // if (auditLogThreadLocal != null)
            //     auditLogThreadLocal.remove();
            WebUtil.removeFromSession(SessionKey.AuditLog);
        }
    }

    private String getAuditData(Object form) {
        StringBuilder sb = new StringBuilder();
        if (form != null) {
            if (form instanceof Map) {
                ((Map<?, ?>) form).forEach((k, v) -> {
                    sb.append(k).append("=");
                    if (v == null) {
                        sb.append("null");
                    } else if (v instanceof List) {
                        sb.append("[").append(StringUtils.join((List<?>) v, ",")).append("]");
                    } else if (v.getClass().isArray()) {
                        if (Array.getLength(v) == 1) {
                            sb.append(Array.get(v, 0));
                        } else if (Array.getLength(v) > 1) {
                            sb.append("[");
                            for (int i = 0; i < Array.getLength(v); i++) {
                                sb.append(Array.get(v, i)).append(",");
                            }
                            sb.deleteCharAt(sb.length() - 1);
                            sb.append("]");
                        } else {
                            sb.append("[]");
                        }
                    } else {
                        sb.append(v);
                    }
                    sb.append("&");
                });
            } else {
                List<Field> fieldList = ReflectUtil.getAllFields(form);
                for (Field field : fieldList) {
                    if (Modifier.isPrivate(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }
                    try {
                        ReflectionUtils.makeAccessible(field);
                    } catch (Throwable t) {
                        logger.warn("ignore field: ", field.getName(), " cause exception occur, ", t.getMessage());
                        continue;
                    }
                    Object value = ReflectionUtils.getField(field, form);
                    if (value == null) {
                        sb.append(field.getName()).append("=").append("null").append("&");
                    } else if ("java.lang".equals(value.getClass().getPackage().getName()) || value instanceof Map) {
                        sb.append(field.getName()).append("=").append(value).append("&");
                    } else if (value instanceof List) {
                        sb.append(field.getName()).append("=[").append(StringUtils.join((List<?>) value, ",")).append("]&");
                    } else if (value.getClass().isArray()) {
                        sb.append(field.getName()).append("=[");
                        if (Array.getLength(value) > 0) {
                            for (int i = 0; i < Array.getLength(value); i++) {
                                sb.append(Array.get(value, i)).append(",");
                            }
                            sb.deleteCharAt(sb.length() - 1);
                        }
                        sb.append("]&");
                    }
                }
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
        }
        return sb.toString();
    }

    public boolean isEnableAudit() {
        return enableAudit;
    }

    public void setEnableAudit(boolean enableAudit) {
        this.enableAudit = enableAudit;
    }

    public boolean isShowAudit() {
        return showAudit;
    }

    public void setShowAudit(boolean showAudit) {
        this.showAudit = showAudit;
    }

    public long getFlushIntervalInMilliseconds() {
        return flushIntervalInMilliseconds;
    }

    public void setFlushIntervalInMilliseconds(long flushIntervalInMilliseconds) {
        this.flushIntervalInMilliseconds = flushIntervalInMilliseconds;
    }

    public int getDbFieldLengthLimitAdjustment() {
        return dbFieldLengthLimitAdjustment;
    }

    public void setDbFieldLengthLimitAdjustment(int dbFieldLengthLimitAdjustment) {
        this.dbFieldLengthLimitAdjustment = dbFieldLengthLimitAdjustment;
    }

    public int getFlushStatementsTotal() {
        return flushStatementsTotal;
    }

    public void setFlushStatementsTotal(int flushStatementsTotal) {
        this.flushStatementsTotal = flushStatementsTotal;
    }

    private class InsertWebAuditThread extends Thread {
        private final List<Webaudit> list = new ArrayList<>();
        private boolean running = true;

        public InsertWebAuditThread() {
            super("InsertWebAuditThread");
            setPriority(Thread.NORM_PRIORITY);
            start();
        }

        public void addWebaudit(Webaudit webaudit) {
            synchronized (list) {
                list.add(webaudit);
                if (list.size() == 1) {
                    list.notifyAll();
                }
            }
        }

        public void terminate() {
            this.running = false;
            // list要notifyAll一下
            synchronized (list) {
                this.list.notifyAll();
            }
        }

        @Override
        public void run() {
            while (this.running) {
                try {
                    Webaudit[] webaudits;
                    synchronized (list) {
                        if (list.isEmpty()) {
                            try {
                                list.wait(600000);
                            } catch (InterruptedException e) {
                                logger.warn(e, e.getMessage());
                            }
                        }
                        // 如果有收到訊息, 並且flushIntervalInMilliseconds大於0, 則再wait上一段時間, 收更多的訊息進來一起notify
                        else if (flushIntervalInMilliseconds > 0) {
                            try {
                                list.wait(flushIntervalInMilliseconds);
                            } catch (InterruptedException e) {
                                logger.warn(e, e.getMessage());
                            }
                        }
                        webaudits = new Webaudit[list.size()];
                        list.toArray(webaudits);
                        list.clear();
                    }
                    if (ArrayUtils.isNotEmpty(webaudits)) {
                        this.process(webaudits);
                    }
                } catch (Throwable e) {
                    logger.warn(e, e.getMessage());
                }
            }
        }

        private void process(Webaudit[] webaudits) {
            if (ArrayUtils.isEmpty(webaudits)) return;
            SqlSessionFactory sqlSessionFactory = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_SQL_SESSION_FACTORY);
            SqlSession sqlSession = null;
            try {
                sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
                WebauditExtMapper webauditExtMapper = sqlSession.getMapper(WebauditExtMapper.class);
                int total = 1, result = 0;
                for (Webaudit webaudit : webaudits) {
                    result += webauditExtMapper.insert(webaudit);
                    if (flushStatementsTotal > 0 && total++ % flushStatementsTotal == 0) {
                        sqlSession.flushStatements();
                    }
                }
                sqlSession.commit();
            } catch (Exception e) {
                DB2Util.handleBatchExecutorException(e);
                logger.warn(e, "[", this.getName(), "][run]Insert Web Audit failed with exception occur, ", e.getMessage());
            } finally {
                IOUtils.closeQuietly(sqlSession);
            }
        }
    }
}
