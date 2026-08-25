package com.syscom.fep.web.configurer;

import com.google.gson.Gson;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.web.base.WebConst;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.interceptor.WebAuditInterceptor;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.StandardCharsets;

@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(Exception.class)
    public Object globalException(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Throwable t) {
        try {
            LogHelperFactory.getTraceLogger().error(t, t.getMessage());
            if (WebUtil.isAjax(request, handlerMethod)) {
                BaseResp<String> baseResp = new BaseResp<>();
                baseResp.setAjaxErr(true);
                baseResp.setMessage(MessageType.DANGER, ExceptionUtil.getStackTrace(t));
                if (WebConst.RESPONSE_TYPE_BLOB.equals(request.getHeader(WebConst.REQUEST_HEADER_KEY_RESPONSE_TYPE))) {
                    baseResp.setAjaxErr(false);
                    baseResp.setMessage(MessageType.DANGER, t.getMessage());
                    return ResponseEntity.ok()
                            // 這裡一定要加入IF_MATCH, 用於前端js判斷是否有異常訊息
                            .header(HttpHeaders.IF_MATCH, "Blob-Response-Error")
                            .body(new Gson().toJson(baseResp).getBytes(StandardCharsets.UTF_8));
                } else {
                    return baseResp;
                }
            } else {
                ModelAndView myModelAndView = new ModelAndView();
                myModelAndView.setViewName(Router.PAGE_500.getView());
                WebUtil.putInAttribute(myModelAndView.getModelMap(), AttributeName.ExecStackTrace, ExceptionUtil.getStackTrace(t));
                return myModelAndView;
            }
        } finally {
            // ThreadLocal<AuditLog> auditLogThreadLocal = ReflectUtil.getFieldValue(handlerMethod.getBean(), "auditLogThreadLocal", null);
            // 避免出現Exception, 造成auditLogThreadLocal沒有被移除
            // if (auditLogThreadLocal != null)
            //     auditLogThreadLocal.remove();
            // 2026-02-14 Richard modified 如果這裡catch到異常, 則也需要記錄AuditLog
            WebAuditInterceptor webAuditInterceptor = SpringBeanFactoryUtil.getBean(WebAuditInterceptor.class, false);
            if (webAuditInterceptor != null) {
                try {
                    webAuditInterceptor.postHandle(request, response, handlerMethod, null);
                } catch (Exception e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
            }
        }
    }
}
