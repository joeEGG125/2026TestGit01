package com.syscom.fep.web.controller;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.entity.Menu;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SessionKey;
import com.syscom.fep.web.entity.User;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.mybatis.vo.SyscomresourceAndCulture;
import com.syscom.safeaa.security.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.owasp.esapi.ESAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class RouterController extends BaseController {
    @Autowired
    private Resource resource;
    @Autowired
    private ResourceLoader resourceLoader;
    private static final List<String> programWhiteList = new ArrayList<>();
    private static final List<String> controllerClassnameList = new ArrayList<>();

    @PostConstruct
    public void init() {
        // 2024-05-07 Richard add start for 【Spring View SPEL Injection】
        for (Router router : Router.values()) {
            programWhiteList.add(router.getProgram());
        }
        // 2024-05-07 Richard add end for 【Spring View SPEL Injection】
        // 2024-04-24 Richard add start for 【Unsafe Reflection】
        String controllerPackage = "com/syscom/fep/web/controller/" + (char) 0x2A + "/" + (char) 0x2A + "Controller.class";
        try {
            controllerClassnameList.addAll(ReflectUtil.listClassname(resourceLoader, controllerPackage));
        } catch (IOException e) {
            LogHelperFactory.getTraceLogger().warn(e, "GetResources failed, packagePattern = [", controllerPackage, "], ", e.getMessage());
        }
        // 2024-04-24 Richard add end for 【Unsafe Reflection】
    }

    @GetMapping(value = "/")
    public String defaultPage() {
        this.infoMessage("使用者訪問頁面, 功能名稱 = [", Router.DEFAULT.getName(), "], view = [", Router.LOGIN.getView(), "], link = [", Router.DEFAULT.getUrl(), "]");
        return Router.LOGIN.getView();
    }

    @GetMapping(value = "/{parentId}/card/{program}/index")
    public String cardView(ModelMap mode, @PathVariable("parentId") String parentId, @PathVariable("program") String program) {
        // 2024-09-24 Richard add start for 【Spring View SPEL Injection】
        String decodedParentId = ESAPI.encoder().decodeForHTML(parentId);
        String decodedProgram = ESAPI.encoder().decodeForHTML(program);
        RefString rtnView = new RefString();
        ReflectUtil.envokeMethod(this, "getView",
                new Class[] {ModelMap.class, String.class, String.class, String.class, RefString.class},
                new Object[] {mode, decodedParentId, "card", decodedProgram, rtnView}, Router.PAGE_500.getView());
        return ESAPI.encoder().decodeForHTML(rtnView.get());
        // 2024-09-24 Richard add end for 【Spring View SPEL Injection】
    }

    @GetMapping(value = "/{parentId}/rm/{program}/index")
    public String rmView(ModelMap mode, @PathVariable("parentId") String parentId, @PathVariable("program") String program) {
        // 2024-09-24 Richard add start for 【Spring View SPEL Injection】
        String decodedParentId = ESAPI.encoder().decodeForHTML(parentId);
        String decodedProgram = ESAPI.encoder().decodeForHTML(program);
        RefString rtnView = new RefString();
        ReflectUtil.envokeMethod(this, "getView",
                new Class[] {ModelMap.class, String.class, String.class, String.class, RefString.class},
                new Object[] {mode, decodedParentId, "rm", decodedProgram, rtnView}, Router.PAGE_500.getView());
        return ESAPI.encoder().decodeForHTML(rtnView.get());
        // 2024-09-24 Richard add end for 【Spring View SPEL Injection】
    }

    @GetMapping(value = "/{parentId}/inbk/{program}/index")
    public String inbkView(ModelMap mode, @PathVariable("parentId") String parentId, @PathVariable("program") String program) {
        // 2024-09-24 Richard add start for 【Spring View SPEL Injection】
        String decodedParentId = ESAPI.encoder().decodeForHTML(parentId);
        String decodedProgram = ESAPI.encoder().decodeForHTML(program);
        RefString rtnView = new RefString();
        ReflectUtil.envokeMethod(this, "getView",
                new Class[] {ModelMap.class, String.class, String.class, String.class, RefString.class},
                new Object[] {mode, decodedParentId, "inbk", decodedProgram, rtnView}, Router.PAGE_500.getView());
        return ESAPI.encoder().decodeForHTML(rtnView.get());
        // 2024-09-24 Richard add end for 【Spring View SPEL Injection】
    }

    @GetMapping(value = "/{parentId}/atmmon/{program}/index")
    public String atmmonView(ModelMap mode, @PathVariable("parentId") String parentId, @PathVariable("program") String program) {
        // 2024-09-24 Richard add start for 【Spring View SPEL Injection】
        String decodedParentId = ESAPI.encoder().decodeForHTML(parentId);
        String decodedProgram = ESAPI.encoder().decodeForHTML(program);
        RefString rtnView = new RefString();
        ReflectUtil.envokeMethod(this, "getView",
                new Class[] {ModelMap.class, String.class, String.class, String.class, RefString.class},
                new Object[] {mode, decodedParentId, "atmmon", decodedProgram, rtnView}, Router.PAGE_500.getView());
        return ESAPI.encoder().decodeForHTML(rtnView.get());
        // 2024-09-24 Richard add end for 【Spring View SPEL Injection】
    }

    @GetMapping(value = "/{parentId}/common/{program}/index")
    public String commonView(ModelMap mode, @PathVariable("parentId") String parentId, @PathVariable("program") String program) {
        // 2024-09-24 Richard add start for 【Spring View SPEL Injection】
        String decodedParentId = ESAPI.encoder().decodeForHTML(parentId);
        String decodedProgram = ESAPI.encoder().decodeForHTML(program);
        RefString rtnView = new RefString();
        ReflectUtil.envokeMethod(this, "getView",
                new Class[] {ModelMap.class, String.class, String.class, String.class, RefString.class},
                new Object[] {mode, decodedParentId, "common", decodedProgram, rtnView}, Router.PAGE_500.getView());
        return ESAPI.encoder().decodeForHTML(rtnView.get());
        // 2024-09-24 Richard add end for 【Spring View SPEL Injection】
    }

    @GetMapping(value = "/{parentId}/demo/{program}/index")
    public String demoView(ModelMap mode, @PathVariable("parentId") String parentId, @PathVariable("program") String program) {
        // 2024-09-24 Richard add start for 【Spring View SPEL Injection】
        String decodedParentId = ESAPI.encoder().decodeForHTML(parentId);
        String decodedProgram = ESAPI.encoder().decodeForHTML(program);
        RefString rtnView = new RefString();
        ReflectUtil.envokeMethod(this, "getView",
                new Class[] {ModelMap.class, String.class, String.class, String.class, RefString.class},
                new Object[] {mode, decodedParentId, "demo", decodedProgram, rtnView}, Router.PAGE_500.getView());
        return ESAPI.encoder().decodeForHTML(rtnView.get());
        // 2024-09-24 Richard add end for 【Spring View SPEL Injection】
    }

    @GetMapping(value = "/{parentId}/auth/{program}/index")
    public String authView(ModelMap mode, @PathVariable("parentId") String parentId, @PathVariable("program") String program) {
        // 2024-09-24 Richard add start for 【Spring View SPEL Injection】
        String decodedParentId = ESAPI.encoder().decodeForHTML(parentId);
        String decodedProgram = ESAPI.encoder().decodeForHTML(program);
        RefString rtnView = new RefString();
        ReflectUtil.envokeMethod(this, "getView",
                new Class[] {ModelMap.class, String.class, String.class, String.class, RefString.class},
                new Object[] {mode, decodedParentId, "auth", decodedProgram, rtnView}, Router.PAGE_500.getView());
        return ESAPI.encoder().decodeForHTML(rtnView.get());
        // 2024-09-24 Richard add end for 【Spring View SPEL Injection】
    }

    @GetMapping(value = "/{parentId}/batch/{program}/index")
    public String batchView(ModelMap mode, @PathVariable("parentId") String parentId, @PathVariable("program") String program) {
        // 2024-09-24 Richard add start for 【Spring View SPEL Injection】
        String decodedParentId = ESAPI.encoder().decodeForHTML(parentId);
        String decodedProgram = ESAPI.encoder().decodeForHTML(program);
        RefString rtnView = new RefString();
        ReflectUtil.envokeMethod(this, "getView",
                new Class[] {ModelMap.class, String.class, String.class, String.class, RefString.class},
                new Object[] {mode, decodedParentId, "batch", decodedProgram, rtnView}, Router.PAGE_500.getView());
        return ESAPI.encoder().decodeForHTML(rtnView.get());
        // 2024-09-24 Richard add end for 【Spring View SPEL Injection】
    }

    @GetMapping(value = "/{parentId}/dbmaintain/{program}/index")
    public String dbmaintainView(ModelMap mode, @PathVariable("parentId") String parentId, @PathVariable("program") String program) {
        // 2024-09-24 Richard add start for 【Spring View SPEL Injection】
        String decodedParentId = ESAPI.encoder().decodeForHTML(parentId);
        String decodedProgram = ESAPI.encoder().decodeForHTML(program);
        RefString rtnView = new RefString();
        ReflectUtil.envokeMethod(this, "getView",
                new Class[] {ModelMap.class, String.class, String.class, String.class, RefString.class},
                new Object[] {mode, decodedParentId, "dbmaintain", decodedProgram, rtnView}, Router.PAGE_500.getView());
        return ESAPI.encoder().decodeForHTML(rtnView.get());
        // 2024-09-24 Richard add end for 【Spring View SPEL Injection】
    }

    @GetMapping(value = "/{parentId}/osm/{program}/index")
    public String osmView(ModelMap mode, @PathVariable("parentId") String parentId, @PathVariable("program") String program) {
        // 2024-09-24 Richard add start for 【Spring View SPEL Injection】
        String decodedParentId = ESAPI.encoder().decodeForHTML(parentId);
        String decodedProgram = ESAPI.encoder().decodeForHTML(program);
        RefString rtnView = new RefString();
        ReflectUtil.envokeMethod(this, "getView",
                new Class[] {ModelMap.class, String.class, String.class, String.class, RefString.class},
                new Object[] {mode, decodedParentId, "osm", decodedProgram, rtnView}, Router.PAGE_500.getView());
        return ESAPI.encoder().decodeForHTML(rtnView.get());
        // 2024-09-24 Richard add end for 【Spring View SPEL Injection】
    }

    /**
     * 注意, 這個方法不能刪除, 否則會導致頁面無法正常運作
     *
     * @param mode
     * @param parentId
     * @param path
     * @param program
     * @param rtnView
     */
    private void getView(ModelMap mode, String parentId, String path, String program, RefString rtnView) {
        String view = this.getView(mode, parentId, path, program);
        String decodedView = ESAPI.encoder().decodeForHTML(view);
        rtnView.set(decodedView);
    }

    /**
     * 返回View
     *
     * @param mode
     * @param parentId
     * @param path
     * @param program
     * @return
     */
    private String getView(ModelMap mode, String parentId, String path, String program) {
        User user = WebUtil.getUser();
        // 如果user為null, 則直接踢到登入畫面
        if (user == null) {
            return this.redirectToUrl(Router.LOGIN.getUrl());
        }
        this.clearForm(user);
        this.clearSessionData();
        String view = StringUtils.join(path, "/", program);
        // 2024-05-07 Richard add start for 【Spring View SPEL Injection】
        if (!programWhiteList.contains(program)) {
            this.warnMessage("無法找到頁面, url = [", parentId, "/", view, "/index", "], ");
            // 無法找到頁面, 則直接跳到404頁面
            return this.redirectToUrl(Router.PAGE_404.getUrl());
        }
        // 2024-05-07 Richard add end for 【Spring View SPEL Injection】
        Menu selectedMenu = user.getAndSetSelectedMenu(view);
        if (selectedMenu != null) {
            this.infoMessage("使用者訪問頁面, 功能名稱 = [", selectedMenu.getName(), "], view = [", selectedMenu.getView(), "], link = [", selectedMenu.getUrl(), "]");
            user.getHomePage().setSidebarCollapsed(true); // 左側menu縮合在一起
            return this.callPageOnLoad(mode, path, program) ? view : this.redirectToUrl(Router.PAGE_406.getUrl());
        } else {
            String resourceNo = Router.getCode(view);
            // 沒有權限, 先查一下db中是否存在
            try {
                List<SyscomresourceAndCulture> list = resource.getResourceDataByNo(resourceNo, "zh-TW");
                if (CollectionUtils.isNotEmpty(list)) {
                    this.warnMessage("使用者無權限訪問頁面, url = [", parentId, "/", view, "/index]");
                    // 沒有權限訪問該頁面, 則直接跳到403頁面
                    return this.redirectToUrl(Router.PAGE_403.getUrl());
                }
            } catch (Exception e) {
                this.warnMessage(e, "getResourceDataByNo failed, resource no = [", resourceNo, "]");
            }
            this.warnMessage("無法找到頁面, url = [", parentId, "/", view, "/index", "], ");
            // 無法找到頁面, 則直接跳到404頁面
            return this.redirectToUrl(Router.PAGE_404.getUrl());
        }
    }

    /**
     * 呼叫頁面初始化方法
     *
     * @param mode
     * @param path
     * @param program
     */
    private boolean callPageOnLoad(ModelMap mode, String path, String program) {
        try {
            String className = StringUtils.join("com.syscom.fep.web.controller.", path, ".", program, "Controller");
            // 2024-04-24 Richard modified start for 【Unsafe Reflection】
            if (controllerClassnameList != null && !controllerClassnameList.contains(className)) {
                throw new IllegalArgumentException("Illegal className: " + className);
            }
            // 2024-04-24 Richard modified end for 【Unsafe Reflection】
            Class<?> controllerClass = Class.forName(className);
            BaseController controller = (BaseController) SpringBeanFactoryUtil.getBean(controllerClass);
            controller.pageOnLoad(mode);
            // 記錄auditLog
            AuditLog auditLog = this.getAuditLog();
            if (auditLog == null) {
                auditLog = new AuditLog();
                // 最後塞入ThreadLocal變量中
                setAuditLog(auditLog);
            }
            // 塞入auditLog.action
            auditLog.setAction("訪問頁面");
            return true;
        } catch (Exception e) {
            this.errorMessage(e, "Cannot find controller for ", path, "/", program);
        }
        return false;
    }

    /**
     * 清除一些session的資料
     */
    private void clearSessionData() {
        WebUtil.putInSession(SessionKey.TemporaryRestoreData, null);
    }
}
