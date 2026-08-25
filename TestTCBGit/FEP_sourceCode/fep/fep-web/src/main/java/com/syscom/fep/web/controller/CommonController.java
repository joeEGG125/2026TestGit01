package com.syscom.fep.web.controller;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.form.BaseForm;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CommonController extends BaseController {

    /**
     * 按下清除按鈕
     *
     * @param redirectAttributes
     * @param request
     * @return
     */
    @PostMapping(value = MappingURI.Clear)
    public String doRedirectForClear(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        User user = WebUtil.getUser();
        // 如果user為null, 則直接踢到登入畫面
        if (user == null) {
            return this.redirectToUrl(Router.LOGIN.getUrl());
        }
        return this.redirectToUrl(user.getSelectedMenu().getUrl());
    }

    /**
     * 直接返回到當前的頁面
     *
     * @param redirectAttributes
     * @param request
     * @return
     */
    @PostMapping(value = MappingURI.SelectMenu)
    public String doRedirectToSelectedMenu(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        return super.doRedirectToSelectedMenu(redirectAttributes, request);
    }

    /**
     * 按下上一頁按鈕
     *
     * @param redirectAttributes
     * @param request
     * @return
     */
    @PostMapping(value = MappingURI.Prevpage)
    public String doRedirectForPrevPage(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        return super.doRedirectForPrevPage(redirectAttributes, request);
    }

    /**
     * 按下上一頁按鈕
     *
     * @param redirectAttributes
     * @param request
     * @return
     */
    @PostMapping(value = MappingURI.PrevpageAjax)
    public String doRedirectForPrevPageAjax(@ModelAttribute BaseResp<?> resp, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        return super.doRedirectForPrevPage(resp, redirectAttributes, request);
    }

    /**
     * 跳轉到當前頁面
     *
     * @param redirectAttributes
     * @param request
     * @return
     */
    @PostMapping(value = MappingURI.CurrentPage)
    public String doRedirectForCurrentPage(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        return super.doRedirectForCurrentPage(redirectAttributes, request);
    }

    /**
     * 跳轉到當前頁面
     *
     * @param redirectAttributes
     * @param request
     * @return
     */
    @PostMapping(value = MappingURI.CurrentPageAjax)
    public String doRedirectForCurrentPageAjax(@ModelAttribute BaseResp<?> resp, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        return super.doRedirectForCurrentPage(resp, redirectAttributes, request);
    }

    /**
     * 按下表頭排序
     *
     * @param redirectAttributes
     * @param request
     * @return
     */
    @PostMapping(value = MappingURI.DoColumnSort)
    public String doRedirectForColumnSort(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        User user = WebUtil.getUser();
        // 如果user為null, 則直接踢到登入畫面
        if (user == null) {
            return this.redirectToUrl(Router.LOGIN.getUrl());
        }
        int hash = Integer.parseInt(request.getParameter("hash"));
        BaseForm form = user.getCurrentPageForm();
        form.changeSqlSortOrder(hash);
        redirectAttributes.addFlashAttribute(form);
        request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
        return this.redirectToUrl(form.getUrl());
    }

    /**
     * 按翻頁按鈕
     *
     * @param redirectAttributes
     * @param request
     * @return
     */
    @PostMapping(value = MappingURI.DoPageChanged)
    public String doRedirectForPageChanged(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        User user = WebUtil.getUser();
        // 如果user為null, 則直接踢到登入畫面
        if (user == null) {
            return this.redirectToUrl(Router.LOGIN.getUrl());
        }
        int pageNum = Integer.parseInt(request.getParameter("pageNum"));
        BaseForm form = user.getCurrentPageForm();
        form.setPageNum(pageNum);
        form.setRedirectFromPageChanged(true);
        redirectAttributes.addFlashAttribute(form);
        request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
        return this.redirectToUrl(form.getUrl());
    }

    /**
     * 設置左側MENU是否展開或者縮合
     *
     * @param args
     * @return
     */
    @PostMapping(value = MappingURI.SidebarCollapse)
    @ResponseBody
    public BaseResp<?> doSidebarCollapse(@RequestBody Map<String, String> args) {
        BaseResp<?> response = new BaseResp<>();
        WebUtil.getUser().getHomePage().setSidebarCollapsed(Boolean.parseBoolean(args.get("sidebarCollapsed")));
        return response;
    }

    /**
     * 設置Condition Panel是否展開縮合
     *
     * @param args
     * @return
     */
    @PostMapping(value = MappingURI.ConditionPanelCollapse)
    @ResponseBody
    public BaseResp<?> doConditionPanelCollapse(@RequestBody Map<String, String> args) {
        BaseResp<?> response = new BaseResp<>();
        WebUtil.getUser().getPageView().setConditionPanelCollapsed(Boolean.parseBoolean(args.get("conditionPanelCollapsed")));
        return response;
    }

    /**
     * 輸入code跳轉頁面
     *
     * @param args
     * @return
     */
    @PostMapping(value = MappingURI.Change)
    @ResponseBody
    public BaseResp<HashMap<String, String>> doChange(@RequestBody Map<String, String> args) {
        BaseResp<HashMap<String, String>> response = new BaseResp<>();
        User user = WebUtil.getUser();
        // 如果user為null, 則直接踢到登入畫面
        if (user == null) {
            HashMap<String, String> data = new HashMap<>();
            data.put("url", Router.LOGIN.getUrl());
            response.setData(data);
        } else {
            String code = args.get("code");
            Menu menu = user.getMenuFromCode(code);
            if (menu == null) {
//                response.setMessage(MessageType.DANGER, MessageFormat.format(StringUtils.join("無法找到", Const.KEY_WORDS_IN_MESSAGE, "到對應的程式, 或者沒有權限連結"), code)); //modified for 【Race Condition Format Flaw】
                response.setMessage(MessageType.DANGER, String.format(StringUtils.join("無法找到", Const.KEY_WORDS_IN_MESSAGE_S, "到對應的程式, 或者沒有權限連結"), code));
            } else {
                HashMap<String, String> data = new HashMap<>();
                data.put("url", menu.getUrl());
                response.setData(data);
//                response.setMessage(MessageType.INFO, MessageFormat.format(StringUtils.join("確定要連結", Const.KEY_WORDS_IN_MESSAGE, "嗎?"), menu.getName()));  //modified for 【Race Condition Format Flaw】
                response.setMessage(MessageType.INFO, String.format(StringUtils.join("確定要連結", Const.KEY_WORDS_IN_MESSAGE_S, "嗎?"), menu.getName()));
            }
        }
        return response;
    }

    /**
     * 按下離開按鈕
     *
     * @param redirectAttributes
     * @param request
     * @return
     */
    @PostMapping(value = MappingURI.Exit)
    public String doRedirectForExit(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        User user = WebUtil.getUser();
        // 如果user為null, 則直接踢到登入畫面
        if (user == null) {
            return this.redirectToUrl(Router.LOGIN.getUrl());
        }
        user.clearSelectedMenu();
        this.clearForm(user);
        return this.redirectToUrl(Router.HOME.getUrl());
    }
}
