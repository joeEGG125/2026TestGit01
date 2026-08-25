package com.syscom.fep.web.interceptor;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.web.entity.User;
import com.syscom.fep.web.util.WebUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final LogHelper logger = LogHelperFactory.getTraceLogger();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_WEB);
        try {
            User user = WebUtil.getUser();
            if (user == null) {
                if ((handler instanceof HandlerMethod && WebUtil.isAjax(request, (HandlerMethod) handler))
                        || WebUtil.isAjax(request)) {
                    WebUtil.doAjaxRedirectToLogin(request, response);
                } else {
                    WebUtil.doRedirectToLogin(request, response);
                }
                logger.warn("Session Expired, there is no Logon User info which keep in session, Redirect to login page!!!");
                return false;
            }
        } catch (Throwable t) {
            logger.exceptionMsg(t, t.getMessage());
        }
        return true;
    }
}
