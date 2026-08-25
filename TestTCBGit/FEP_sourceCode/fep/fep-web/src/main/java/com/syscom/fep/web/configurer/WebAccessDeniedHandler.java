package com.syscom.fep.web.configurer;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.web.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class WebAccessDeniedHandler implements AccessDeniedHandler {
    private final LogHelper logger = LogHelperFactory.getTraceLogger();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (WebUtil.isAjax(request)) {
            WebUtil.doAjaxRedirectToLogin(request, response);
        } else {
            WebUtil.doRedirectToLogin(request, response);
        }
        logger.warn(accessDeniedException != null ? accessDeniedException.getMessage() : StringUtils.EMPTY, " Session Token Expired, Redirect to login page!!!");
    }
}
