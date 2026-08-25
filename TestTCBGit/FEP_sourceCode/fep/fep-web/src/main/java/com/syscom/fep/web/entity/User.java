package com.syscom.fep.web.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.syscom.fep.frmcommon.ref.RefBase;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.syscom.fep.web.form.BaseForm;

/**
 * 用來記錄登入者信息
 *
 * @author Richard
 */
public class User implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    /**
     * 登錄ID
     */
    private String loginId;
    /**
     * 使用者ID
     */
    private String userId;
    /**
     * 登入ssCode
     */
    private transient String ssCode;
    /**
     * 使用者名
     */
    private String userName;
    /**
     * 登入session
     */
    private String sessionId;
    /**
     * 登入IP
     */
    private String srcIp;
    /**
     * 登入後產出的token
     */
    private String token;
    /**
     * 獲取瀏覽器信息
     */
    private String browser;
    /**
     * MENU
     */
    private List<Menu> menuList = new ArrayList<>();
    /**
     * 此時正在訪問的Menu
     */
    private Menu selectedMenu;
    /**
     * 訪問的表單資料
     */
    private List<BaseForm> formHistoryList = new ArrayList<>();
    /**
     * 當前的表單資料
     */
    private BaseForm currentForm;
    /**
     * 訪問頁面資料
     */
    private List<PageView> pageViewHistoryList = new ArrayList<>();

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String logonId) {
        this.loginId = logonId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSsCode() {
        return ssCode;
    }

    public void setSsCode(String ssCode) {
        this.ssCode = ssCode;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public List<Menu> getMenuList() {
        return menuList;
    }

    public void setMenuList(List<Menu> menuList) {
        this.menuList.clear();
        if (CollectionUtils.isNotEmpty(menuList)) {
            this.menuList.addAll(menuList);
        }
    }

    public Menu getSelectedMenu() {
        return selectedMenu;
    }

    public void setSelectedMenu(Menu menu) {
        this.selectedMenu = menu;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    /**
     * 增加新的表單資料
     *
     * @param form
     */
    public <T extends BaseForm> void addForm(T form) {
        // 如果url已經在currentForm或者formHistoryList中存在, 則替換就好, 不要再增加了
        if (currentForm != null && currentForm.getUrl().equals(form.getUrl())) {
            currentForm = form;
            return;
        } else if (this.isHasPrevPageForm()) {
            for (int i = 0; i < formHistoryList.size(); i++) {
                if (formHistoryList.get(i).getUrl().equals(form.getUrl())) {
                    formHistoryList.set(i, form);
                    return;
                }
            }
        }
        // 第一次增加表單時, 先不要放進formHistoryList中
        if (currentForm == null) {
            currentForm = form;
        }
        // 再一次增加表單時, 把之前表單放進formHistoryList中
        else {
            formHistoryList.add(SerializationUtils.clone(currentForm));
            currentForm = form;
        }
    }

    /**
     * 是否含有前一頁的表單資料
     *
     * @return
     */
    public boolean isHasPrevPageForm() {
        return formHistoryList.size() > 0;
    }

    /**
     * 獲取前一頁的表單資料, 並判斷是否需要移除掉
     *
     * @param removed
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseForm> T getPrevPageForm(boolean removed) {
        T form = null;
        if (this.isHasPrevPageForm()) {
            if (removed) {
                form = (T) formHistoryList.remove(formHistoryList.size() - 1);
                if (this.isHasPrevPageForm()) {
                    currentForm = form;
                } else {
                    currentForm = null;
                }
            } else {
                form = (T) formHistoryList.get(formHistoryList.size() - 1);
            }
        }
        return form;
    }

    /**
     * 獲取前一頁的表單資料, 並且不移除掉
     *
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseForm> T getPrevPageForm() {
        return (T) this.getPrevPageForm(false);
    }

    /**
     * 獲取當前頁的表單資料
     *
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseForm> T getCurrentPageForm() {
        return (T) currentForm;
    }

    /**
     * 重置
     */
    public void reset() {
        // 清除表單
        formHistoryList.clear();
        currentForm = null;
        // homePage要保留
        PageView homePage = this.getHomePage();
        homePage.setSidebarCollapsed(false); // 預設menu不要縮合在一起
        pageViewHistoryList.clear();
        pageViewHistoryList.add(homePage);
    }

    /**
     * 獲取頁面
     *
     * @return
     */
    public PageView getPageView() {
        return this.pageViewHistoryList.get(pageViewHistoryList.size() - 1);
    }

    /**
     * 獲取主頁
     *
     * @return
     */
    public PageView getHomePage() {
        PageView homePage = pageViewHistoryList.stream().filter(t -> t.getView().equals(Router.HOME.getView())).findFirst().orElse(null);
        if (homePage == null) {
            this.addPageView(Router.HOME);
            return this.getHomePage();
        }
        return homePage;
    }

    /**
     * 移除頁面
     *
     * @return
     */
    public PageView removePageView() {
        if (CollectionUtils.isNotEmpty(pageViewHistoryList)) {
            return pageViewHistoryList.remove(pageViewHistoryList.size() - 1);
        }
        return null;
    }

    /**
     * 增加新的頁面
     *
     * @param router
     * @return
     */
    public PageView addPageView(Router router) {
        PageView pageView = pageViewHistoryList.stream().filter(t -> t.getView().equals(router.getView())).findFirst().orElse(null);
        if (pageView == null) {
            pageView = new PageView();
            pageView.setView(router.getView());
            pageView.setName(router.getName());
            pageViewHistoryList.add(pageView);
        }
        return pageView;
    }

    /**
     * 清除被選擇的menu
     */
    public void clearSelectedMenu() {
        this.setSelectedMenu(null);
        this.clearSelectedMenu(this.menuList);
    }

    /**
     * 清除被選擇的menu
     *
     * @param menuList
     */
    private void clearSelectedMenu(List<Menu> menuList) {
        if (CollectionUtils.isNotEmpty(menuList)) {
            for (Menu menu : menuList) {
                menu.setSelected(false);
                if (CollectionUtils.isNotEmpty(menu.getChildList())) {
                    this.clearSelectedMenu(menu.getChildList());
                }
            }
        }
    }

    /**
     * 根據code獲取menu
     *
     * @param code
     * @return
     */
    public Menu getMenuFromCode(String code) {
        return this.getMenuFromCode(this.menuList, code);
    }

    private Menu getMenuFromCode(List<Menu> menuList, String code) {
        if (CollectionUtils.isNotEmpty(menuList)) {
            for (Menu menu : menuList) {
                if (StringUtils.isNotBlank(menu.getCode()) && menu.getCode().equals(code)) {
                    return menu;
                }
                Menu foundMenu = this.getMenuFromCode(menu.getChildList(), code);
                if (foundMenu != null) {
                    return foundMenu;
                }
            }
        }
        return null;
    }

    /**
     * 設置並獲取選中的MENU
     *
     * @param view
     */
    public Menu getAndSetSelectedMenu(String view) {
        RefBase<Menu> refSelectedMenu = new RefBase<Menu>(null);
        for (int i = 0; i < this.getMenuList().size(); i++) {
            this.makeMenuSelected(this.getMenuList().get(i), view, refSelectedMenu);
        }
        Menu selectedMenu = refSelectedMenu.get();
        if (selectedMenu != null) {
            this.setSelectedMenu(selectedMenu);
        }
        return selectedMenu;
    }

    /**
     * 設置MENU被選中
     *
     * @param menu
     * @param view
     * @param selectedMenu
     */
    private void makeMenuSelected(Menu menu, String view, RefBase<Menu> selectedMenu) {
        menu.setSelected(false);
        if (CollectionUtils.isNotEmpty(menu.getChildList())) {
            List<Menu> childList = menu.getChildList();
            for (int i = 0; i < childList.size(); i++) {
                this.makeMenuSelected(childList.get(i), view, selectedMenu);
            }
        } else {
            if (menu.getView() != null && menu.getView().equals(view)) {
                if (menu.getParent() != null) {
                    menu.getParent().setSelected(true);
                }
                menu.setSelected(true);
                selectedMenu.set(menu);
            }
        }
    }
}