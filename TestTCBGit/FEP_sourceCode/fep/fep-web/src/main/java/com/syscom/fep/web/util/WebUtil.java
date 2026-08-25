package com.syscom.fep.web.util;

import com.google.gson.Gson;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.mybatis.model.Fepuser;
import com.syscom.fep.web.base.WebConst;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.safeaa.mybatis.vo.SyscomGroupVo;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class WebUtil extends WebUtils {
    private static final LogHelper logger = LogHelperFactory.getFEPWebMessageLogger();
    private static final String hostName, hostIp;

    static {
        // try {
        //     hostName = InetAddress.getLocalHost().getCanonicalHostName();
        // } catch (UnknownHostException e) {
        //     hostName = "Unknown";
        // }
        // try {
        //     hostIp = InetAddress.getLocalHost().getHostAddress();
        // } catch (UnknownHostException e) {
        //     hostIp = "Unknown";
        // }
        hostName = FEPConfig.getInstance().getHostName();
        hostIp = FEPConfig.getInstance().getHostIp();
    }

    private WebUtil() {
    }

    /**
     * 從Session中獲取當前登入的使用者
     *
     * @return
     */
    public static User getUser() {
        return getFromSession(SessionKey.LogonUser);
    }

    /**
     * 從Session中獲取當前登入的Fepuser使用者
     *
     * @return
     */
    public static Fepuser getFepuser() {
        return getFromSession(SessionKey.Fepuser);
    }

    /**
     * 從Session中獲取當前登入的使用者群組
     *
     * @return
     */
    public static List<SyscomGroupVo> getGroup() {
        return getFromSession(SessionKey.Group);
    }

    /**
     * 取得Request對象
     *
     * @return
     */
    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    /**
     * 獲取Session對象
     *
     * @return
     */
    public static HttpSession getSession() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            return request.getSession();
        }
        return null;
    }

    /**
     * 從Session中獲取資料
     *
     * @param <T>
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFromSession(SessionKey key) {
        HttpSession session = getSession();
        if (session != null) {
            return (T) session.getAttribute(key.toString());
        }
        return null;
    }

    /**
     * session中塞入資料
     *
     * @param key
     * @param value
     * @param <T>
     */
    public static <T> void putInSession(SessionKey key, T value) {
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute(key.toString(), value);
        }
    }

    /**
     * session中移除資料
     *
     * @param key
     * @param <T>
     */
    public static <T> void removeFromSession(SessionKey key) {
        HttpSession session = getSession();
        if (session != null) {
            session.removeAttribute(key.toString());
        }
    }

    /**
     * request或者map中加入資料返回到前端頁面
     *
     * @param <T>
     * @param map
     * @param attributeName
     * @param value
     */
    public static <T> void putInAttribute(Map<String, Object> map, AttributeName attributeName, T value) {
        map.put(attributeName.toString(), value);
    }

    /**
     * @param map
     * @param attributeName
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFromAttribute(Map<String, Object> map, AttributeName attributeName) {
        return MapUtils.isNotEmpty(map) ? (T) map.get(attributeName.toString()) : null;
    }

    /**
     * @param map
     * @param attributeName
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T removeFromAttribute(Map<String, Object> map, AttributeName attributeName) {
        return MapUtils.isNotEmpty(map) ? (T) map.remove(attributeName.toString()) : null;
    }

    /**
     * 跳轉請求中加入資料返回到前端頁面
     *
     * @param redirectAttributes
     * @param attributeName
     * @param value
     * @param <T>
     */
    public static <T> void putInAttribute(RedirectAttributes redirectAttributes, AttributeName attributeName, T value) {
        redirectAttributes.addFlashAttribute(attributeName.toString(), value);
    }

    /**
     * 獲取遠程訪問的IP
     *
     * @return
     */
    public static String getRemoteClientIp() {
        // 取IP
        String srcIp = null;
        HttpServletRequest request = getRequest();
        if (request != null) {
            String HTTP_CLIENT_IP = "";
            String HTTP_X_CLIENT_IP = "";
            String HTTP_X_FORWARDED_FOR = "";
            String HTTP_X_FORWARDED = "";
            String HTTP_X_CLUSTER_CLIENT_IP = "";
            String HTTP_FORWARDED_FOR = "";
            String HTTP_FORWARDED = "";
            String REMOTE_ADDR = "";
            String HTTP_VIA = "";
            Enumeration<String> ite = request.getHeaderNames();
            while (ite.hasMoreElements()) {
                String key = (String) ite.nextElement();
                logger.debug("Header : ", key, " , value : ", request.getHeader(key));
                if ("CLIENT-IP".equalsIgnoreCase(key))
                    HTTP_CLIENT_IP = request.getHeader(key);
                else if ("X-CLIENT-IP".equalsIgnoreCase(key))
                    HTTP_X_CLIENT_IP = request.getHeader(key);
                else if ("X-FORWARDED-FOR".equalsIgnoreCase(key))
                    HTTP_X_FORWARDED_FOR = request.getHeader(key);
                else if ("X-FORWARDED".equalsIgnoreCase(key))
                    HTTP_X_FORWARDED = request.getHeader(key);
                else if ("X-CLUSTER-CLIENT-IP".equalsIgnoreCase(key))
                    HTTP_X_CLUSTER_CLIENT_IP = request.getHeader(key);
                else if ("FORWARDED-FOR".equalsIgnoreCase(key))
                    HTTP_FORWARDED_FOR = request.getHeader(key);
                else if ("FORWARDED".equalsIgnoreCase(key))
                    HTTP_FORWARDED = request.getHeader(key);
                else if ("REMOTE-ADDR".equalsIgnoreCase(key))
                    REMOTE_ADDR = request.getHeader(key);
                else if ("HTTP-VIA".equalsIgnoreCase(key))
                    HTTP_VIA = request.getHeader(key);
            }
            // 目前依照WAF導入建議，先取HTTP_X_FORWARDED_FOR
            if (srcIp == null)
                srcIp = HTTP_X_FORWARDED_FOR;
            if (StringUtils.isBlank(srcIp))
                srcIp = HTTP_X_CLIENT_IP;
            if (StringUtils.isBlank(srcIp))
                srcIp = HTTP_CLIENT_IP;
            if (StringUtils.isBlank(srcIp))
                srcIp = HTTP_X_FORWARDED;
            if (StringUtils.isBlank(srcIp))
                srcIp = HTTP_X_CLUSTER_CLIENT_IP;
            if (StringUtils.isBlank(srcIp))
                srcIp = HTTP_FORWARDED_FOR;
            if (StringUtils.isBlank(srcIp))
                srcIp = HTTP_FORWARDED;
            if (StringUtils.isBlank(srcIp))
                srcIp = REMOTE_ADDR;
            if (StringUtils.isBlank(srcIp))
                srcIp = HTTP_VIA;
            if (StringUtils.isBlank(srcIp))
                srcIp = request.getRemoteAddr();
        }
        return srcIp;
    }

    /**
     * 取得服務器hostname
     *
     * @return
     */
    public static String getServerHostName() {
        return hostName;
    }

    /**
     * 取得服務器ip
     *
     * @return
     */
    public static String getServerHostIp() {
        return hostIp;
    }

    /**
     * 在MENU增加demo程式
     *
     * @param menuList
     */
    public static void addDemoFunction(List<Menu> menuList) {
        if (WebConfiguration.getInstance().isShowDemo()) {
            String contextPath = getRequest().getContextPath();
            Menu parent = new Menu();
            parent.setId(Router.DEMO.getParentId());
            parent.setName(Router.DEMO.getParentName());
            menuList.add(parent);
            for (Router router : Router.values()) {
                if (router.ordinal() >= Router.DEMO.ordinal() && !router.isSub()) {
                    Menu child = new Menu();
                    child.setId(router.getUrl());
                    child.setName(router.getName());
                    child.setView(router.getView());
                    child.setUrl(contextPath + router.getUrl());
                    child.setParent(parent);
                    child.setLeaf(true);
                    child.setCode(router.getCode());
                    parent.getChildList().add(child);
                }
            }
        }
    }

    /**
     * 判斷是否是ajax請求
     * spring ajax 返回含有 ResponseBody 或者 RestController 註解
     *
     * @param request       HttpServletRequest
     * @param handlerMethod HandlerMethod
     * @return
     */
    public static boolean isAjax(HttpServletRequest request, HandlerMethod handlerMethod) {
        // 判斷方法是否有ResponseBody註解
        ResponseBody responseBody = handlerMethod.getMethodAnnotation(ResponseBody.class);
        if (null != responseBody) {
            return true;
        }
        // 獲取類上面的Annotation，可能包含組合注解，故采用spring的工具類
        Class<?> beanType = handlerMethod.getBeanType();
        responseBody = AnnotationUtils.getAnnotation(beanType, ResponseBody.class);
        if (null != responseBody) {
            return true;
        }
        // 判斷方法參數中是否有參數含有RequestBody註解
        MethodParameter[] parameters = handlerMethod.getMethodParameters();
        if (ArrayUtils.isNotEmpty(parameters)) {
            for (MethodParameter parameter : parameters) {
                RequestBody requestBody = parameter.getParameterAnnotation(RequestBody.class);
                if (requestBody != null) {
                    return true;
                }
            }
        }
        return isAjax(request);
    }

    /**
     * 判斷是否是ajax請求
     *
     * @param request HttpServletRequest
     * @return
     */
    public static boolean isAjax(HttpServletRequest request) {
        // 判斷Accept
        String accept = request.getHeader(WebConst.REQUEST_HEADER_KEY_ACCEPT);
        if (StringUtils.isNotBlank(accept) && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }
        // 判斷Header
        if (WebConst.REQUEST_HEADER_VALUE_XMLHTTPREQUEST.equals(request.getHeader(WebConst.REQUEST_HEADER_KEY_X_Requested_With))) {
            return true;
        }
        // 判斷請求ContentType是不是json
        if (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(request.getContentType()) ||
                MediaType.APPLICATION_JSON_UTF8_VALUE.equalsIgnoreCase(request.getContentType())) {
            return true;
        }
        return false;
    }

    public static String getCookieValue(String cookieName) {
        Cookie cookie = getCookie(getRequest(), cookieName);
        return cookie == null ? null : cookie.getValue();
    }

    public static void removeCookie(HttpServletResponse response, String cookieName) {
        setCookie(response, cookieName, null, 0);
    }

    public static void setCookie(HttpServletResponse response, String cookieName, String cookieValue,
                                 int defaultMaxAge) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setHttpOnly(true);
        cookie.setPath(getRequest().getContextPath());
        cookie.setMaxAge(defaultMaxAge);
        cookie.setSecure(true); // 2024-11-18 Richard add for 【Sensitive Cookie in HTTPS Session Without Secure Attribute】
        response.addCookie(cookie);
    }

    /**
     * 如果前端訪問是Ajax, 則通過Ajax方式重定向到登入頁面
     *
     * @param request
     * @param response
     * @throws IOException
     */
    public static void doAjaxRedirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BaseResp<String> baseResp = new BaseResp<>();
        baseResp.setRedirect(true);
        baseResp.setData(Router.LOGIN.getUrl());
        String jsonStr = new Gson().toJson(baseResp);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (WebConst.RESPONSE_TYPE_BLOB.equals(request.getHeader(WebConst.REQUEST_HEADER_KEY_RESPONSE_TYPE))) {
            response.setContentType(WebConst.RESPONSE_TYPE_BLOB);
            ResponseEntity<?> responseEntity = ResponseEntity.ok(baseResp);
            for (Map.Entry<String, List<String>> header : responseEntity.getHeaders().entrySet()) {
                String key = header.getKey();
                for (String valor : header.getValue()) {
                    response.addHeader(key, valor);
                }
            }
            response.setStatus(responseEntity.getStatusCodeValue());
        } else {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        }
        response.getWriter().write(jsonStr);
        response.getWriter().flush();
    }

    /**
     * 跳轉到登入頁面
     *
     * @param request
     * @param response
     * @throws IOException
     */
    public static void doRedirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String contextPath = request.getContextPath();
        response.sendRedirect(StringUtils.join(contextPath, Router.LOGIN.getUrl()));
    }

    /**
     * 獲取瀏覽器名稱
     *
     * @return
     */
    public static String getBrowser() {
        try {
            HttpServletRequest request = getRequest();
            UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
            ReadableUserAgent userAgent = (UserAgent) parser.parse(request.getHeader("User-Agent"));
            String browserName = StringUtils.join(userAgent.getFamily().getName(), userAgent.getVersionNumber().toVersionString());
            logger.debug("browserName : ", browserName);
            return browserName;
        } catch (Exception e) {
            logger.warn(e, e.getMessage());
        }
        return null;
    }
}