package com.syscom.fep.web.service;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.frmcommon.ref.RefInt;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.UUIDUtil;
import com.syscom.fep.mybatis.ext.mapper.FepuserExtMapper;
import com.syscom.fep.mybatis.model.Fepuser;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.entity.Menu;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SessionKey;
import com.syscom.fep.web.ldap.AdConnect;
import com.syscom.fep.web.ldap.AdUser;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.enums.EnumAuditType;
import com.syscom.safeaa.enums.EnumSscodeFormat;
import com.syscom.safeaa.mybatis.extmapper.SyscomauditlogExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomuserExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomauditlog;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.mybatis.vo.SyscomGroupVo;
import com.syscom.safeaa.mybatis.vo.SyscomresourceInfoVo;
import com.syscom.safeaa.mybatis.vo.SyscomroleResourceVo;
import com.syscom.safeaa.mybatis.vo.SyscomuserInfoVo;
import com.syscom.safeaa.security.Authentication;
import com.syscom.safeaa.security.Role;
import com.syscom.safeaa.security.User;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.owasp.esapi.ESAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class LoginService extends BaseService {

    @Autowired
    private Authentication authService;

    @Autowired
    private User userService;

    @Autowired
    private Role roleService;

    @Autowired
    private FepuserExtMapper fepuserExtMapper;

    @Autowired
    private PlatformTransactionManager fepdbTransactionManager;

    @Autowired
    private SyscomuserExtMapper syscomuserExtMapper;

    @Autowired
    private BatchService batchService;

    @Autowired
    private SyscomauditlogExtMapper auditlogExtMapper;

    /**
     * 登入檢核
     *
     * @param loginId
     * @param ssCode
     * @throws Exception
     */
    public void processLogon(String loginId, String ssCode) throws Exception {
        RefInt syscomUserid = new RefInt(-1); // 用來塞入SyscomUser中的userId欄位
        try {
            if (StringUtils.isBlank(loginId)) {
                throw ExceptionUtil.createException("帳號不可以是空白!!");
            } else if (StringUtils.isBlank(ssCode)) {
                throw ExceptionUtil.createException("密碼不可以是空白!!");
            }
            // AD流程
            if (WebConfiguration.getInstance().isLdapEnable()) {
                // String encodeLoginId = ESAPI.encoder().encodeForLDAP(loginId, false);
                // processLogonForLDAP(encodeLoginId, ssCode, syscomUserid);
                processLogonForLDAP(loginId, ssCode, syscomUserid);
                // 登入成功, 記錄auditlog, safeaa會記錄, 所以這裡只針對LDAP登錄進行記錄
                this.addLog(syscomUserid.get(), EnumAuditType.LogOn.getValue(), WebUtil.getRemoteClientIp(), true, StringUtils.join("User ", loginId, " login OK"));
            }
            // Safeaa流程
            else {
                processLogonForSafeaa(loginId, ssCode, syscomUserid);
            }
        } catch (Exception e) {
            // 登入失敗, 記錄auditlog
            this.addLog(syscomUserid.get(), EnumAuditType.LogOn.getValue(), WebUtil.getRemoteClientIp(), false, StringUtils.substring(e.getMessage(), 0, 200)); // 避免訊息超過字段長度導致寫入失敗
            // 清除session
            WebUtil.removeFromSession(SessionKey.LogonUser);
            WebUtil.removeFromSession(SessionKey.Fepuser);
            WebUtil.removeFromSession(SessionKey.Group);
            // 丟異常
            // String encodeLoginId = ESAPI.encoder().encodeForLDAP(loginId, false);
//            Exception exception = ExceptionUtil.createException(e, MessageFormat.format(StringUtils.join("登入失敗, 帳號", Const.KEY_WORDS_IN_MESSAGE, ", ", e.getMessage()), encodeLoginId));    //modified for 【Race Condition Format Flaw】
            // Exception exception = ExceptionUtil.createException(e, String.format(StringUtils.join("登入失敗, 帳號", Const.KEY_WORDS_IN_MESSAGE_S, ", ", e.getMessage()), encodeLoginId));
            Exception exception = ExceptionUtil.createException(e, String.format(StringUtils.join("登入失敗, 帳號", Const.KEY_WORDS_IN_MESSAGE_S, ", ", e.getMessage()), loginId));
            this.errorMessage(exception, exception.getMessage());
//            this.sendEMS(exception.getMessage());
            throw exception;
        }
    }

    /**
     * AD流程
     *
     * @param loginId
     * @param ssCode
     * @param syscomUserid
     * @throws Exception
     */
    private void processLogonForLDAP(String loginId, String ssCode, RefInt syscomUserid) throws Exception {
        // 取AD資料
        AdUser adUser = this.queryAdUser(loginId, ssCode);
        if (adUser == null) {
            throw ExceptionUtil.createException("LDAP資料不存在!!");
        }
        // 判斷AD中的Group在Safeaa中是否存在
        //boolean roleExist = false;
        List<SyscomGroupVo> SyscomGroupVoList = new ArrayList<SyscomGroupVo>();
        if (CollectionUtils.isNotEmpty(adUser.getGroupList())) {
            // for (String group : adUser.getGroupList()) {
            //     if (roleService.getRoleIdByNo(group) != null) {
            //         roleExist = true;
            //         //break;
            //     } else {
            //     }
            // }
            for (String group : adUser.getGroupList()) {
                SyscomGroupVo syscomGroupVo = new SyscomGroupVo();
                syscomGroupVo.setRoleNo(group);
                Integer roleId = roleService.getRoleIdByNo(group);
                if (roleId != null) {
                    syscomGroupVo.setRoleId(roleId.toString());
                    SyscomGroupVoList.add(syscomGroupVo);
                }
                // Integer roleId = batchService.getRoleID(group);
                // syscomGroupVo.setRoleId(roleId.toString());
                // SyscomGroupVoList.add(syscomGroupVo);
            }
        }
        if (SyscomGroupVoList.size() == 0) {
            throw ExceptionUtil.createException("此帳號AD角色不存在FEP系統!!");
        }
        // 判斷Safeaa中使用者是否存在
        SyscomuserInfoVo vo = this.getSyscomuserInfoVo(loginId);
        // 不存在則創建FEP使用者
        if (vo == null) {
            this.warnMessage("FEP系統帳號loginId=[", loginId, "]不存在, 自動創建...");
            this.createFepuser(loginId, ssCode, adUser);
        }
        // 上面創建完了, 再嘗試查詢一次
        vo = this.getSyscomuserInfoVo(loginId);
        if (vo == null) {
            throw ExceptionUtil.createException("FEP系統帳號不存在!!");
        }
        // 塞入UserId, 用來最後面寫入AudioLog檔
        syscomUserid.set(vo.getUserid());
        // 根據Group取Resource
        List<SyscomroleResourceVo> resourceList;
        try {
            // 20230321 Bruce add 在首頁顯示group名稱清單
            // List<SyscomGroupVo> SyscomGroupVoList = new ArrayList<>();
            // for (String group : adUser.getGroupList()) {
            //     SyscomGroupVo syscomGroupVo = new SyscomGroupVo();
            //     syscomGroupVo.setRoleNo(group);
            //     Integer roleId = batchService.getRoleID(group);
            //     syscomGroupVo.setRoleId(roleId.toString());
            //     SyscomGroupVoList.add(syscomGroupVo);
            // }
            WebUtil.putInSession(SessionKey.Group, SyscomGroupVoList);
            // 查詢MENU
            // String[] roles = new String[SyscomGroupVoList.size()];
            // SyscomGroupVoList.toArray(roles);
            String[] roles = SyscomGroupVoList.stream().map(SyscomGroupVo::getRoleNo).toArray(String[]::new);
            resourceList = roleService.queryMenuListByRoles(roles);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            throw ExceptionUtil.createException(e, StringUtils.join("查詢帳號權限出現錯誤"));
        }
        // 組建MENU
        List<Menu> menuList = buildMenuForLDAP(resourceList);
        // 最後將User存在Session中
        this.createUser(ssCode, vo, menuList);
    }

    /**
     * Safeaa流程
     *
     * @param loginId
     * @param ssCode
     * @param syscomUserid
     * @throws Exception
     */
    private void processLogonForSafeaa(String loginId, String ssCode, RefInt syscomUserid) throws Exception {
        try {
//            boolean flag = authService.checkLogOn(loginId, ssCode, WebUtil.getRemoteClientIp(), false);
            boolean flag = true;
            if (!flag) {
                throw ExceptionUtil.createException("帳號檢核失敗!!");
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            String errorMessage = "帳號檢核出現異常";
            if (e instanceof SafeaaException) {
                SafeaaException exec = (SafeaaException) e;
                if (StringUtils.isNotBlank(exec.getMessage())) {
                    errorMessage = exec.getMessage();
                }
            }
            throw ExceptionUtil.createException(e, errorMessage);
        }
        SyscomuserInfoVo vo = this.getSyscomuserInfoVo(loginId);
        if (vo == null) {
            throw ExceptionUtil.createException("FEP系統帳號不存在!!");
        } else {
            // 塞入UserId, 用來最後面寫入AudioLog檔
            syscomUserid.set(vo.getUserid());
            try {
                // SHA(5)
                // 2024-11-18 Richard modified for 【Heap Inspection】
                EnumSscodeFormat enumSscodeFormat = EnumSscodeFormat.getEnumPasswordFormatByValue(5);
                if (vo.getSscodeformat() != null) {
                    EnumSscodeFormat.getEnumPasswordFormatByValue(vo.getSscodeformat().intValue());
                }
                String encryptSsCode = authService.encryptPassword(ssCode, enumSscodeFormat);
                if (!encryptSsCode.equals(vo.getSscode())) {
                    throw ExceptionUtil.createException("密碼不正確!!");
                }
            } catch (Exception e) {
                this.errorMessage(e, e.getMessage());
                throw ExceptionUtil.createException(e, StringUtils.join("驗證使用者密碼出現異常"));
            }
        }
        List<SyscomresourceInfoVo> resourceList = null;
        try {
            resourceList = userService.querySyscomresourceByLogOnId(loginId);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            throw ExceptionUtil.createException(e, StringUtils.join("查詢權限出現錯誤"));
        }
        List<Menu> menuList = buildMenuForSafeaa(resourceList);
        this.createUser(ssCode, vo, menuList);
        // 在首頁顯示group名稱清單
        List<SyscomGroupVo> syscomGroupVoList = this.syscomuserExtMapper.queryUserAndGroup(vo.getUserid().toString());
        WebUtil.putInSession(SessionKey.Group, syscomGroupVoList);
    }

    private void createFepuser(String loginId, String ssCode, AdUser adUser) throws Exception {
        Syscomuser syscomuser = new Syscomuser();
        syscomuser.setLogonid(loginId);
        syscomuser.setUsername(adUser.getName());
        syscomuser.setEmailaddress(adUser.getMail());
        syscomuser.setEmployeeid(loginId);
        syscomuser.setEffectdate(Calendar.getInstance().getTime());
        syscomuser.setExpireddate(CalendarUtil.parseDateValue(20991231).getTime());
        syscomuser.setUpdatetime(Calendar.getInstance().getTime());
        syscomuser.setUpdateuserid(1);
        Fepuser fepuser = new Fepuser();
        fepuser.setFepuserLogonid(loginId);
        fepuser.setFepuserName(adUser.getName());
        fepuser.setUpdateTime(Calendar.getInstance().getTime());
        fepuser.setUpdateUserid(1);
        TransactionStatus fepTxStatus = fepdbTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            int resultForFepuser = 0;
            List<Fepuser> fepuserList = fepuserExtMapper.getFepUserByLogonId(loginId);
            if (CollectionUtils.isEmpty(fepuserList)) {
                fepuser.setFepuserJob(StringUtils.EMPTY); //TODO 這個塞什麼???
                fepuser.setFepuserTlrno(StringUtils.EMPTY); //TODO 這個塞什麼???
                fepuser.setFepuserLevel((short) 1);
                fepuser.setFepuserStatus((short) 0);
                resultForFepuser = fepuserExtMapper.insertSelective(fepuser);
            } else {
                fepuser.setUpdateUserid(fepuserList.get(0).getUpdateUserid());
                resultForFepuser = fepuserExtMapper.updateByLogonIdSelective(fepuser);
            }
            if (resultForFepuser > 0) {
                syscomuser.setEmpid(fepuser.getFepuserUserid());
                int ret = userService.createUser(syscomuser, 5, StringUtils.EMPTY, StringUtils.EMPTY, ssCode);
                if (ret == -1) {
                    throw ExceptionUtil.createException("創建FEP系統帳號失敗!!");
                }
            } else {
                throw ExceptionUtil.createException("創建FEP系統帳號失敗!!");
            }
            fepdbTransactionManager.commit(fepTxStatus);
        } catch (Exception e) {
            fepdbTransactionManager.rollback(fepTxStatus);
            this.errorMessage(e, e.getMessage());
            throw ExceptionUtil.createException(e, "創建FEP系統帳號失敗!!");
        }
    }

    private void createUser(String ssCode, SyscomuserInfoVo vo, List<Menu> menuList) throws Exception {
        // User
        com.syscom.fep.web.entity.User user = new com.syscom.fep.web.entity.User();
        user.setSsCode(ssCode);
        user.setSrcIp(WebUtil.getRemoteClientIp());
        user.setSessionId(WebUtil.getSession().getId());
        user.addPageView(Router.HOME);
        user.setUserName(vo.getUsername());
        user.setUserId(vo.getUserid().toString());
        user.setLoginId(vo.getLogonid());
        user.setMenuList(menuList);
        user.setToken(UUIDUtil.randomUUID(true)); // TODO just do this
        user.setBrowser(WebUtil.getBrowser());
        WebUtil.putInSession(SessionKey.LogonUser, user);
        // Fepuser
        Fepuser fepUser = this.queryFepuser(user.getLoginId(), user.getUserName());
        WebUtil.putInSession(SessionKey.Fepuser, fepUser);
    }

    private SyscomuserInfoVo getSyscomuserInfoVo(String loginId) throws Exception {
        try {
            return userService.getSyscomuserInfo(loginId);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            throw ExceptionUtil.createException(e, StringUtils.join("查詢FEP系統帳號出現錯誤"));
        }
    }

    private AdUser queryAdUser(String loginId, String ssCode) throws Exception {
        if (WebConfiguration.getInstance().isLdapSimulator()) { // just for test
            AdUser adUser = new AdUser();
            // adUser.setName(ESAPI.encoder().encodeForLDAP("AdUser", false));
            adUser.setName("AdUser");
            adUser.setMail("aduser@anonymous.com");
            adUser.setGroupList(Arrays.asList("FEPAdmin"));
            return adUser;
        }
        try {
            String webaddress = WebConfiguration.getInstance().getWebaddress();
            String strtemp = WebConfiguration.getInstance().getStrtemp();
            String principal = WebConfiguration.getInstance().getPrincipal();
            // String encodeLoginId = ESAPI.encoder().encodeForLDAP(loginId, false);
            // AdConnect adConnect = new AdConnect(encodeLoginId, ssCode, webaddress, strtemp, principal);
            AdConnect adConnect = new AdConnect(loginId, ssCode, webaddress, strtemp, principal);
            Map<String, Object> userInfoMap = adConnect.getUserInfo();
            List<String> groupList = adConnect.getUserGroups();
            if (groupList != null) {
                String[] roles = groupList.toArray(new String[0]);
                this.infoMessage(StringUtils.join("Get group list from AD: ", String.join(",", roles)));
            }
            AdUser adUser = new AdUser();
            if (MapUtils.isNotEmpty(userInfoMap)) {
                adUser.setName(MapUtils.getString(userInfoMap, "description", StringUtils.EMPTY));
                adUser.setMail(MapUtils.getString(userInfoMap, "mail", StringUtils.EMPTY));
                adUser.setDepartment(MapUtils.getString(userInfoMap, "department", StringUtils.EMPTY));
                adUser.setDepartmentNumber(MapUtils.getString(userInfoMap, "departmentNumber", StringUtils.EMPTY));
                this.infoMessage(StringUtils.join("department: ", adUser.getDepartment(), ", departmentNumber:", adUser.getDepartmentNumber()));
            }
            adUser.setGroupList(groupList);
            return adUser;
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            if (ExceptionUtil.find(e, t -> t instanceof java.net.ConnectException) != null) {
                throw ExceptionUtil.createException(e, StringUtils.join("建立LDAP連線失敗"));
            }
            throw ExceptionUtil.createException(e, StringUtils.join(loginId + "查詢/驗證LDAP資料出現錯誤"));
        }
    }

    private List<Menu> buildMenuForLDAP(List<SyscomroleResourceVo> resourceList) throws Exception {
        List<Menu> menuList = new ArrayList<>();
        try {
            if (resourceList != null) {
                String contextPath = WebUtil.getRequest().getContextPath();
                for (SyscomroleResourceVo vo : resourceList) {
                    if (vo.getResourcepid() == null || vo.getResourcepid() == 0) {
                        boolean isContains = false;
                        for (Menu m : menuList) {
                            if (m.getId().equals(vo.getResourceid().toString())) {
                                isContains = true;
                                break;
                            }
                        }
                        if (isContains) {
                            continue;
                        }
                        if (vo.getResourceid() == null || StringUtils.isBlank(vo.getResourcename())) {
                            continue;
                        }
                        Menu parent = new Menu();
                        parent.setId(vo.getResourceid().toString());
                        parent.setName(vo.getResourcename());
                        menuList.add(parent);
                    } else {
                        for (Menu parent : menuList) {
                            if (parent.getId().equals(vo.getResourcepid().toString())) {
                                boolean isContains = false;
                                for (Menu m : parent.getChildList()) {
                                    if (m.getCode().equals(vo.getResourcepno())) {
                                        isContains = true;
                                        break;
                                    }
                                }
                                if (isContains) {
                                    continue;
                                }
                                if (vo.getResourcepid() == null
                                        || StringUtils.isBlank(vo.getResourceurl())
                                        || StringUtils.isBlank(vo.getResourcename())
                                        || StringUtils.isBlank(vo.getResourcepno())) {
                                    continue;
                                }
                                Menu child = new Menu();
                                child.setId(StringUtils.join("/", vo.getResourcepid().toString(), "/", vo.getResourceurl(), "/index"));
                                child.setName(vo.getResourcename());
                                child.setView(vo.getResourceurl());
                                child.setUrl(contextPath + child.getId());
                                child.setParent(parent);
                                child.setLeaf(true);
                                child.setCode(vo.getResourcepno());
                                parent.getChildList().add(child);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            throw ExceptionUtil.createException(e, "創建MENU出現異常!!");
        } finally {
            this.afterBuildMenu(menuList);
        }
        return menuList;
    }

    private List<Menu> buildMenuForSafeaa(List<SyscomresourceInfoVo> resourceList) throws Exception {
        List<Menu> menuList = new ArrayList<>();
        try {
            if (resourceList != null) {
                String contextPath = WebUtil.getRequest().getContextPath();
                for (SyscomresourceInfoVo vo : resourceList) {
                    if (vo.getResourcepid() == null || vo.getResourcepid() == 0) {
                        boolean isContains = false;
                        for (Menu m : menuList) {
                            if (m.getId().equals(vo.getResourceid().toString())) {
                                isContains = true;
                                break;
                            }
                        }
                        if (isContains) {
                            continue;
                        }
                        if (vo.getResourceid() == null || StringUtils.isBlank(vo.getResourcename())) {
                            continue;
                        }
                        Menu parent = new Menu();
                        parent.setId(vo.getResourceid().toString());
                        parent.setName(vo.getResourcename());
                        menuList.add(parent);
                    } else {
                        for (Menu parent : menuList) {
                            if (parent.getId().equals(vo.getResourcepid().toString())) {
                                boolean isContains = false;
                                for (Menu m : parent.getChildList()) {
                                    if (m.getCode().equals(vo.getResourceno())) {
                                        isContains = true;
                                        break;
                                    }
                                }
                                if (isContains) {
                                    continue;
                                }
                                if (vo.getResourcepid() == null
                                        || StringUtils.isBlank(vo.getResourceurl())
                                        || StringUtils.isBlank(vo.getResourcename())
                                        || StringUtils.isBlank(vo.getResourceno())) {
                                    continue;
                                }
                                Menu child = new Menu();
                                child.setId(StringUtils.join("/", vo.getResourcepid().toString(), "/", vo.getResourceurl(), "/index"));
                                child.setName(vo.getResourcename());
                                child.setView(vo.getResourceurl());
                                child.setUrl(contextPath + child.getId());
                                child.setParent(parent);
                                child.setLeaf(true);
                                child.setCode(vo.getResourceno());
                                parent.getChildList().add(child);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            throw ExceptionUtil.createException(e, "創建MENU出現異常!!");
        } finally {
            this.afterBuildMenu(menuList);
        }
        return menuList;
    }

    private void afterBuildMenu(List<Menu> menuList) {
        // 2022-02-11 Richard add
        // 在MENU增加demo程式
        WebUtil.addDemoFunction(menuList);
        // 2022-02-28 這裡要再過濾一下, 把沒有child的parent拿掉
        if (CollectionUtils.isNotEmpty(menuList)) {
            Stream<Menu> stream = menuList.stream().filter(t -> CollectionUtils.isNotEmpty(t.getChildList()));
            if (stream != null) {
                List<Menu> filteredList = stream.collect(Collectors.toList());
                menuList.clear();
                menuList.addAll(filteredList);
            }
        }
    }

    private Fepuser queryFepuser(String fepuserLogonid, String fepuserName) throws Exception {
        try {
            List<Fepuser> fepuserList = fepuserExtMapper.getFepUserByLogonId(fepuserLogonid);
            // 如果沒有查到, 則new一個
            Fepuser fepuser = null;
            if (CollectionUtils.isNotEmpty(fepuserList)) {
                fepuser = fepuserList.get(0); // 預設取第一筆吧, 按理應該不會有多筆才對
            }
            // 按理這裡應該不會為null
            if (fepuser == null) {
                this.warnMessage("Fepuser is not exist!! fepuserLogonid = [", fepuserLogonid, "], fepuserName= [", fepuserName, "]");
                fepuser = new Fepuser();
                fepuser.setFepuserLogonid(fepuserLogonid);
                fepuser.setFepuserName(fepuserName);
            }
            if (StringUtils.isBlank(fepuser.getFepuserGroup())) {
                fepuser.setFepuserGroup(StringUtils.EMPTY);
            }
            return fepuser;
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            throw ExceptionUtil.createException(e, "查詢FEP系統帳號出現錯誤");
        }
    }

    private void addLog(Integer userid, int audittype, String logonip, boolean flag, String audittarget) {
        String logId = UUID.randomUUID().toString();
        Syscomauditlog auditlog = new Syscomauditlog();
        auditlog.setLogid(logId);
        auditlog.setUserid(userid);
        auditlog.setAudittype(audittype);
        auditlog.setUpdatetime(new Date());
        auditlog.setUpdatelogonip(logonip);
        if (flag) {
            auditlog.setResult((short) 1);
        } else {
            auditlog.setResult((short) 0);
        }
        auditlog.setAudittarget(audittarget);
        auditlogExtMapper.insert(auditlog);
    }
}
