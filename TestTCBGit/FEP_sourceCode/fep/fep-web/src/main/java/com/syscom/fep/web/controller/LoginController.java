package com.syscom.fep.web.controller;

import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.service.LoginService;
import com.syscom.fep.web.util.WebUtil;
import org.owasp.esapi.ESAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController extends BaseController {
    @Autowired
    private LoginService loginService;

    @PostMapping(value = "/logon")
    public String logon(@RequestParam("userId") String userId, @RequestParam("ssCode") String ssCode, ModelMap mode) {
        this.infoMessage("使用者帳號 = [", userId, "]登入");
        // 這裡記錄輸入過的使用者賬號
        WebUtil.putInAttribute(mode, AttributeName.UserId, userId);
        try {
            // String encodeUserId = ESAPI.encoder().encodeForLDAP(userId, false);
            // loginService.processLogon(encodeUserId, ssCode);
            loginService.processLogon(userId, ssCode);
            return this.redirectToUrl(Router.HOME.getUrl());
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            WebUtil.putInAttribute(mode, AttributeName.ErrorMessage, e.getMessage());
        }
        return Router.LOGIN.getView();
    }
    
    /**
     * 為了讓F5查看web是否活著
     * @return
     */
    @GetMapping(value = "/ping")
    public String ping() {
        //避免佔用session先點掉
        //this.infoMessage("Ping Success");
        return Router.PING.getView();
    }
}