package com.syscom.fep.web.configurer;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.web.entity.MappingURI;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.interceptor.AuthenticationInterceptor;
import com.syscom.fep.web.interceptor.ViewInterceptor;
import com.syscom.fep.web.interceptor.WebAuditInterceptor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Field;
import java.util.*;

@Configuration
@ConfigurationProperties(prefix = "spring.fep.web")
// @RefreshScope
public class WebConfiguration implements WebMvcConfigurer {
    @Value("${spring.fep.web.demo:false}")
    private boolean showDemo;
    @Value("${spring.fep.web.ldap:false}")
    private boolean ldapEnable;
    @Value("${spring.fep.web.ldap.simulator:false}")
    private boolean ldapSimulator;
    @Value("${spring.fep.web.title:FEP監控系統}")
    private String appTitle;
    @Value("${spring.fep.web.web-type:FEP}")
    private String webType;
    @Value("${spring.fep.web.query-only:0}")
    private String queryOnly;
    @Value("${spring.fep.web.time-out:30000}")
    private int reNewTime;
    @Value("#{'${spring.fep.web.subsys:1,2,3,4,5,6,7,8,9,10,11,12,13,14}'.split(',')}")
    private List<String> subsysList;
    @Value("${spring.fep.web.isFEPWebOrRMWeb:FEPWeb}")
    private String isWeb;
    @Value("${spring.fep.web.RemoteMQServiceFlag:ON}")
    private String RemoteMQServiceFlag;
    @Value("${spring.fep.web.RemoteMQServiceIP:serverIP}")
    private String serverIP;
    @Value("${spring.fep.web.mq.service.port:8161}")
    private String port;
    @Value("${spring.fep.web.mq.service.request:/api/CustomCommand}")
    private String mqServiceRequest;
    @Value("${spring.fep.web.system.principal:HWAFANG@tcbt.com}")
    private String principal;
    @Value("${spring.fep.web.system.webaddress:ldaps://10.0.6.2:636}")
    private String webaddress;
    @Value("${spring.fep.web.system.strtemp:dc=tcbt,dc=com}")
    private String strtemp;
    @Value("${spring.fep.web.system.username:HWAFANG}")
    private String username;
    @Value("${spring.fep.web.system.userinname:HWAFANG}")
    private String userinname;
    @Value("${spring.fep.web.system.chkid:G201450209}")
    private String chkid;
    @Value("${spring.fep.web.system.userid:0410}")
    private String unitid;
    @Value("${spring.fep.web.system.fileName:fepd.cer}")
    private String fileName;
    @Value("${spring.fep.web.system.filesscode:changeit}")
    private String filesscode;
    @Value("${spring.fep.web.system.certificateName:X.509}")
    private String certificateName;
    @Value("${spring.fep.web.system.fiddler:fiddler}")
    private String fiddler;
    @Value("${spring.fep.web.system.download.feplogpath:/fep/logs}")
    private String fepLogPath;
    @Value("${spring.fep.web.system.download.feplogarchivespath:/fep/logs/archives}")
    private String fepLogArchivesPath;
    @Value("${spring.fep.web.system.download.fepwaslogpath:/fep/waslogs}")
    private String fepWasLogPath;
    @Value("${spring.fep.web.system.download.fepwaslogarchivespath:/fep/waslogs/archives}")
    private String fepWasLogArchivesPath;
    @NestedConfigurationProperty
    private final List<WebApLogConfiguration> aplog = new ArrayList<>();
    @Value("${spring.fep.web.interceptor.audit.enableAudit:false}")
    private boolean auditEnable;
    @Value("${spring.fep.web.get.ap.log.total.size.limit:10}")
    private int getApLogTotalSizeLimit;
    @Value("${spring.fep.web.http.request.aysnc.timeout:600000}")
    private long httpRequestAsyncTimeout;
    @Value("${spring.fep.web.apikey:apiKey}")
    private String apikey;
    @Value("${spring.fep.web.ucd.apikey:ucdapiKey}")
    private String ucdapikey;
    @NestedConfigurationProperty
    private final Map<String, String> apChannelToAppName = new TreeMap<>();
    @NestedConfigurationProperty
    private final Map<String, String> gwChannelToAppName = new TreeMap<>();
    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;
    @Autowired
    private ViewInterceptor viewInterceptor;

    public static WebConfiguration getInstance() {
        return SpringBeanFactoryUtil.getBean(WebConfiguration.class);
    }

    /**
     * 頁面跳轉
     *
     * @param registry
     */
    public void addViewControllers(ViewControllerRegistry registry) {
        for (Router route : Router.values()) {
            registry.addViewController(route.getUrl()).setViewName(route.getView());
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> baseExcludePathPatternList =
                Arrays.asList(
                        Router.DEFAULT.getUrl(),
                        Router.LOGIN.getUrl(),
                        "/error/**",
                        MappingURI.Logon,
                        MappingURI.Logout,
                        "/css/**",
                        "/images/**",
                        "/js/**",
                        "/plugins/**",
                        "/ping/**",
                        "/actuator/**",
                        "/UI060610/GetAPLog",
                        "/UI060610/GetAPLogNames",
                        "/WebUtils/ArchivingLogFile",
                        "/WebUtils/FEPTXNQry",
                        "/WebUtils/FEPUCDQry",
                        "/WebUtils/Transaction",
                        "/WebUtils/GRAYLISTQuery",
                        "/demo/Demo/post1",
                        "/demo/Demo/post2");
        // authenticationInterceptor
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(baseExcludePathPatternList);
        // viewInterceptor
        List<String> viewExcludePathPatternList = new ArrayList<>(baseExcludePathPatternList);
        viewExcludePathPatternList.add(Router.HOME.getUrl());
        registry.addInterceptor(viewInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(viewExcludePathPatternList);
        // webAuditInterceptor
        WebAuditInterceptor webAuditInterceptor = SpringBeanFactoryUtil.getBean(WebAuditInterceptor.class, false);
        if (webAuditInterceptor != null) {
            List<String> webAuditExcludePathPatternList = new ArrayList<>(baseExcludePathPatternList);
            webAuditExcludePathPatternList.add(Router.HOME.getUrl());
            // 以下動作也要攔截, 所以要移除掉
            webAuditExcludePathPatternList.remove(MappingURI.Logon);
            webAuditExcludePathPatternList.remove(MappingURI.Logout);
            registry.addInterceptor(webAuditInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns(webAuditExcludePathPatternList);
        } else {
            LogHelperFactory.getTraceLogger().warn("##########WebAuditInterceptor has been not register as Spring Object, so that no need to record Web Audit!!!");
        }
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(httpRequestAsyncTimeout);
    }

    public boolean isShowDemo() {
        return showDemo;
    }

    public boolean isLdapEnable() {
        return ldapEnable;
    }

    public boolean isLdapSimulator() {
        return ldapSimulator;
    }

    public String getApikey() {return apikey;}

    public String getUcdapikey() {return ucdapikey;}

    public String getAppTitle() {
        return appTitle;
    }

    public String getWebType() {
        return webType;
    }

    public String getQueryOnly() {
        return queryOnly;
    }

    public int getReNewTime() {
        return reNewTime;
    }

    public List<String> getSubsysList() {
        return subsysList;
    }

    public String getIsFEPWebOrRMWeb() {
        return isWeb;
    }

    public String getRemoteMQServiceFlag() {
        return RemoteMQServiceFlag;
    }

    public String getRemoteMQServiceIP() {
        return serverIP;
    }

    public String getMQServicePort() {
        return port;
    }

    public String getMqServiceRequest() {
        return mqServiceRequest;
    }

    public String getPrincipal() {
        return principal;
    }

    public String getWebaddress() {
        return webaddress;
    }

    public String getStrtemp() {
        return strtemp;
    }

    public String getUsername() {
        return username;
    }

    public String getUserinname() {
        return userinname;
    }

    public String getChkid() {
        return chkid;
    }

    public String getUnitid() {
        return unitid;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilesscode() {
        return filesscode;
    }

    public String getCertificateName() {
        return certificateName;
    }

    public String getFiddler() {
        return fiddler;
    }

    public String getFepLogPath() {
        return fepLogPath;
    }

    public String getFepWasLogPath() {
        return fepWasLogPath;
    }

    public String getFepWasLogArchivesPath() {return fepWasLogArchivesPath;}

    public String getFepLogArchivesPath() {
        return fepLogArchivesPath;
    }

    public List<WebApLogConfiguration> getAplog() {
        return aplog;
    }

    public boolean isAuditEnable() {
        return auditEnable;
    }

    public int getGetApLogTotalSizeLimit() {return getApLogTotalSizeLimit;}

    public Map<String, String> getApChannelToAppName() {
        return apChannelToAppName;
    }

    public Map<String, String> getGwChannelToAppName() {
        return gwChannelToAppName;
    }

    @PostConstruct
    public void print() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "Web Configuration", "authenticationInterceptor", "viewInterceptor"));
    }


    @PreDestroy
    public void destroy() {
        UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
        if (parser != null) {
            try {
                parser.shutdown();
            } catch (Throwable t) {
                com.syscom.fep.frmcommon.log.LogHelper.getAdditionalLogger().error(t, t.getMessage());
            }
        }
    }
}