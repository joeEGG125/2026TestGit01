package com.syscom.fep.web.interceptor;

import com.syscom.fep.web.base.FEPWebBase;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.entity.Menu;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.User;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.mybatis.vo.SyscomresourceAndCulture;
import com.syscom.safeaa.security.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Component
public class ViewInterceptor extends FEPWebBase implements HandlerInterceptor {
    @Autowired
    private Resource resource;

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        String contextPath = request.getContextPath();
        User user = WebUtil.getUser();
        // 如果user為null, 則直接踢到登入畫面
        if (user == null) {
            // response.sendRedirect(StringUtils.join(contextPath, Router.LOGIN.getUrl()));
            return;
        }
        this.debugMessage("服務端響應使用者請求, modelAndView = [", modelAndView, "]");
        if (modelAndView == null) return;
        String view = modelAndView.getViewName();
        if (StringUtils.isNotBlank(view)) {
            // 如果是重定向, 則直接跳過
            if (view.startsWith("redirect:")) {
                return;
            }
            Menu orgSelectMenu = user.getSelectedMenu();
            Menu selectedMenu = user.getAndSetSelectedMenu(view);
            if (selectedMenu != null) {
                this.infoMessage("服務端返回頁面, 功能名稱 = [", selectedMenu.getName(), "], view = [", selectedMenu.getView(), "], link = [", selectedMenu.getUrl(), "]");
            } else {
                String resourceNo = Router.getCode(view);
                // 沒有權限, 先查一下db中是否存在
                List<SyscomresourceAndCulture> list = null;
                try {
                    list = resource.getResourceDataByNo(resourceNo, "zh-TW");
                    if (CollectionUtils.isNotEmpty(list)) {
                        // 忽略明細頁
                        if (list.stream().filter(t -> t.getResourceurl().equals(view)).findAny().orElse(null) != null) {
                            this.warnMessage("使用者無權限訪問頁面, view = [", view, "]");
                            // 沒有權限訪問該頁面, 則直接跳到403頁面
                            response.sendRedirect(StringUtils.join(contextPath, Router.PAGE_403.getUrl()));
                        } else {
                            // db中有資料, 並且不是完整的view名稱, 那可能是其他的明細頁的View, 則直接跳過
                            Router router = Router.fromView(view);
                            if (router != null) {
                                this.infoMessage("服務端返回頁面, 功能名稱 = [", router.getName(), "], view = [", router.getView(), "]");
                            }
                            // 這裡一定要重新設置之前選中的MENU
                            user.getAndSetSelectedMenu(orgSelectMenu.getView());
                        }
                        return;
                    }
                } catch (Exception e) {
                    this.warnMessage(e, "getResourceDataByNo failed, resource no = [", resourceNo, "]");
                }
                // db中沒有資料, 那可能是其他的明細頁的View, 則直接跳過
                if (CollectionUtils.isEmpty(list)) {
                    Router router = Router.fromView(view);
                    if (router != null) {
                        if (router.ordinal() >= Router.DEMO.ordinal() && !WebConfiguration.getInstance().isShowDemo()) {
                            this.warnMessage("不可以訪問, 功能名稱 = [", router.getName(), "], view = [", router.getView(), "]");
                            response.sendRedirect(StringUtils.join(contextPath, Router.PAGE_404.getUrl()));
                        } else {
                            this.infoMessage("服務端返回頁面, 功能名稱 = [", router.getName(), "], view = [", router.getView(), "]");
                            // 這裡一定要重新設置之前選中的MENU
                            user.getAndSetSelectedMenu(orgSelectMenu.getView());
                        }
                        return;
                    }
                }
                this.warnMessage("無法找到頁面, view = [", view, "]");
                // 無法找到頁面, 則直接跳到404頁面
                response.sendRedirect(StringUtils.join(contextPath, Router.PAGE_404.getUrl()));
                return;
            }
        }
    }
}
