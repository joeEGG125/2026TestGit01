package com.syscom.fep.web.controller;

import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.service.LogoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LogoutController extends BaseController {
    @Autowired
    private LogoutService logoutService;

    @PostMapping(value = "/logout")
    public String logout(ModelMap mode) {
        logoutService.processLogout(mode);
        return Router.LOGIN.getView();
    }
}
