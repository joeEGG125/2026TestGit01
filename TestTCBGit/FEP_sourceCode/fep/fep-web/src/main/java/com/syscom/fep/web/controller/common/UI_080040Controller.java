package com.syscom.fep.web.controller.common;

import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Fepuser;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.common.UI_080040_Form;
import com.syscom.fep.web.service.CommonService;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.mybatis.model.Syscomrole;
import com.syscom.safeaa.mybatis.model.Syscomroleculture;
import com.syscom.safeaa.mybatis.model.Syscomrolemembers;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.mybatis.vo.SyscomQueryAllUsers;
import com.syscom.safeaa.mybatis.vo.SyscomroleInfoVo;
import com.syscom.safeaa.mybatis.vo.SyscomrolemembersAndCulture;
import com.syscom.safeaa.utils.SyscomConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 角色使用者維護
 *
 * @author ChenYang
 */
@Controller
public class UI_080040Controller extends BaseController {
    @Autowired
    private CommonService commonService;
    @Autowired
    private SyscomConfig safeSettings;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 載入預設的起訖日期
        mode.addAttribute("defaultEffectDate", FormatUtil.dateTimeFormat(safeSettings.getEffectDate(null), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        mode.addAttribute("defaultExpiredDate", FormatUtil.dateTimeFormat(safeSettings.getExpiredDate(null), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
    }

    @PostMapping(value = "/common/UI_080040/select")
    @ResponseBody
    public UI_080040_Form selectData() {
        this.infoMessage("開始執行, 條件 = []");
        UI_080040_Form form = new UI_080040_Form();
        try {
            List<SyscomroleInfoVo> list = commonService.getSyscomroleInfoVoAll();
            if (list == null) {
                form.setMessage(MessageType.INFO, QueryNoData);
            } else {
                form.setDataList(list);
            }
        } catch (SafeaaException se) {
            this.errorMessage(se, se.getMessage());
            form.setMessage(MessageType.INFO, programError);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            form.setMessage(MessageType.DANGER, QueryFail);
        }
        return form;
    }

    @PostMapping(value = "/common/UI_080040/insert")
    @ResponseBody
    public UI_080040_Form insertData(@RequestBody UI_080040_Form form) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        try {
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Syscomrole role = new Syscomrole();
            role.setRoleno(form.getNo());
            role.setRoletype("1");
//            role.setEffectdate(simpleDateFormat.parse(form.getEffectdate()));
//            role.setExpireddate(simpleDateFormat.parse(form.getExpireddate()));
            role.setEffectdate(safeSettings.getEffectDate(form.getEffectdate()));
            role.setExpireddate(safeSettings.getExpiredDate(form.getExpireddate()));
            role.setUpdatetime(Calendar.getInstance().getTime());
            role.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));
            Syscomroleculture roleculture = new Syscomroleculture();
            roleculture.setRoleid(form.getId());
            roleculture.setRolename(form.getName());
            Integer roleId = commonService.getRoleIdByNo(form.getNo());
            if (roleId != null) {
                form.setMessage(MessageType.DANGER, "該角色編號已存在");
                return form;
            }
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("新增角色");
            if (StringUtils.isNotBlank(form.getNo().toString()))
                auditLog.addParam("角色編號", form.getNo());
            if (StringUtils.isNotBlank(form.getName()))
                auditLog.addParam("角色名稱", form.getName());
            if (StringUtils.isNotBlank(form.getEffectdate()))
                auditLog.addParam("有效起日", form.getEffectdate());
            if (StringUtils.isNotBlank(form.getExpireddate()))
                auditLog.addParam("有效迄日", form.getExpireddate());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            boolean flag = commonService.insertRole(role, roleculture);
            if (!flag) {
                form.setMessage(MessageType.DANGER, "角色新增失敗");
                return form;
            }
            List<SyscomroleInfoVo> list = commonService.getSyscomroleInfoVoAll();
            if (list == null) {
                form.setMessage(MessageType.INFO, QueryNoData);
            } else {
                form.setDataList(list);
            }
            form.setMessage(MessageType.SUCCESS, InsertSuccess);
        } catch (SafeaaException se) {
            this.errorMessage(se, se.getMessage());
            form.setMessage(MessageType.INFO, se.getMessage());
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            form.setMessage(MessageType.DANGER, InsertFail);
        }
        return form;
    }

    @PostMapping(value = "/common/UI_080040/updateRole")
    @ResponseBody
    public UI_080040_Form updateRole(@RequestBody UI_080040_Form form) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        try {
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Syscomrole role = new Syscomrole();
            role.setRoleid(form.getId());
            role.setRoleno(form.getNo());
            role.setRoletype("1");
//            role.setEffectdate(simpleDateFormat.parse(form.getEffectdate()));
//            role.setExpireddate(simpleDateFormat.parse(form.getExpireddate()));
            role.setEffectdate(safeSettings.getEffectDate(form.getEffectdate()));
            role.setExpireddate(safeSettings.getExpiredDate(form.getExpireddate()));
            role.setUpdatetime(Calendar.getInstance().getTime());
            role.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));
            Syscomroleculture roleculture = new Syscomroleculture();
            roleculture.setRoleid(form.getId());
            roleculture.setRolename(form.getName());
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("修改角色");
            if (StringUtils.isNotBlank(form.getId().toString()))
                auditLog.addParam("角色編號", form.getNo());
            if (StringUtils.isNotBlank(form.getNo()))
                auditLog.addParam("角色名稱", form.getName());
            if (StringUtils.isNotBlank(form.getEffectdate()))
                auditLog.addParam("有效起日", form.getEffectdate());
            if (StringUtils.isNotBlank(form.getExpireddate()))
                auditLog.addParam("有效迄日", form.getExpireddate());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            Integer roleId = commonService.getRoleIdByNo(form.getNo());
            if (roleId != null && roleId.intValue() != form.getId().intValue()) {
                form.setMessage(MessageType.DANGER, "該角色編號已存在");
                return form;
            }
            boolean flag = commonService.updateRole(role, roleculture);
            if (!flag) {
                form.setMessage(MessageType.DANGER, "角色修改失敗");
                return form;
            }
            List<SyscomroleInfoVo> list = commonService.getSyscomroleInfoVoAll();
            if (list == null) {
                form.setMessage(MessageType.INFO, QueryNoData);
            } else {
                form.setDataList(list);
            }
            form.setMessage(MessageType.SUCCESS, UpdateSuccess);
        } catch (SafeaaException se) {
            this.errorMessage(se, se.getMessage());
            form.setMessage(MessageType.INFO, se.getMessage());
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            form.setMessage(MessageType.DANGER, UpdateFail);
        }
        return form;
    }

    @PostMapping(value = "/common/UI_080040/updateUser")
    @ResponseBody
    public UI_080040_Form updateUser(@RequestBody UI_080040_Form form) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        try {
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Syscomuser syscomuser = new Syscomuser();
            syscomuser.setUserid(Integer.valueOf(form.getId()));
            syscomuser.setLogonid(form.getNo());
            syscomuser.setUsername(form.getName());
            syscomuser.setEmailaddress(form.getUsermail());
//            syscomuser.setEffectdate(simpleDateFormat.parse(form.getEffectdate()));
//            syscomuser.setExpireddate(simpleDateFormat.parse(form.getExpireddate()));
            syscomuser.setEffectdate(safeSettings.getEffectDate(form.getEffectdate()));
            syscomuser.setExpireddate(safeSettings.getExpiredDate(form.getExpireddate()));
            syscomuser.setUpdatetime(Calendar.getInstance().getTime());
            syscomuser.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));
            Fepuser fepuser = new Fepuser();
            fepuser.setFepuserUserid(form.getEmpid());
            fepuser.setFepuserLogonid(form.getNo());
            fepuser.setFepuserName(form.getName());
            fepuser.setUpdateTime(Calendar.getInstance().getTime());
            fepuser.setFepuserUserid(Integer.parseInt(WebUtil.getUser().getUserId()));
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("確認(使用者)");
            if (StringUtils.isNotBlank(form.getId().toString()))
                auditLog.addParam("使用者帳號", form.getNo());
            if (StringUtils.isNotBlank(form.getNo()))
                auditLog.addParam("使用者名稱", form.getName());
            if (StringUtils.isNotBlank(form.getUsermail()))
                auditLog.addParam("電子郵件", form.getUsermail());
            if (StringUtils.isNotBlank(form.getEffectdate()))
                auditLog.addParam("有效起日", form.getEffectdate());
            if (StringUtils.isNotBlank(form.getExpireddate()))
                auditLog.addParam("有效迄日", form.getExpireddate());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            boolean rst = commonService.updatetUser(syscomuser, fepuser);
            if (!rst) {
                form.setMessage(MessageType.SUCCESS, UpdateFail);
                return form;
            }
            List<SyscomroleInfoVo> list = commonService.getSyscomroleInfoVoAll();
            if (list == null) {
                form.setMessage(MessageType.INFO, QueryNoData);
            } else {
                form.setDataList(list);
                form.setMessage(MessageType.SUCCESS, UpdateSuccess);
            }
        } catch (SafeaaException se) {
            this.errorMessage(se, se.getMessage());
            form.setMessage(MessageType.INFO, se.getMessage());
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            form.setMessage(MessageType.DANGER, UpdateFail);
        }
        return form;
    }

    @PostMapping(value = "/common/UI_080040/deleteRole")
    @ResponseBody
    public UI_080040_Form deleteData(@RequestBody UI_080040_Form form) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        try {
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("刪除");
            auditLog.addParam("角色編號", form.getId());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            boolean flag = commonService.deleteRole(form.getId());
            if (!flag) {
                form.setMessage(MessageType.DANGER, DeleteFail);
                return form;
            }
            List<SyscomroleInfoVo> list = commonService.getSyscomroleInfoVoAll();
            if (list == null) {
                form.setMessage(MessageType.INFO, QueryNoData);
            } else {
                form.setDataList(list);
                form.setMessage(MessageType.SUCCESS, DeleteSuccess);
            }
        } catch (SafeaaException se) {
            this.errorMessage(se, se.getMessage());
            form.setMessage(MessageType.INFO, se.getMessage());
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            form.setMessage(MessageType.DANGER, DeleteFail);
        }
        return form;
    }

    /**
     * 查詢按鈕
     */
    @PostMapping(value = "/common/UI_080040/checkTreeNode")
    @ResponseBody
    public UI_080040_Form checkTreeNode(@RequestBody UI_080040_Form form) {
        List<Map<String, String>> allList = new ArrayList<>();
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            List<SyscomQueryAllUsers> infoList = commonService.getAllUsers();
            if (infoList != null) {
                for (SyscomQueryAllUsers user : infoList) {
                    Map<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", user.getUserId().toString());
                    hashMap.put("pNo", user.getLogonId());
                    hashMap.put("name", user.getUserName());
                    hashMap.put("startDate", simpleDateFormat.format(user.getEffectDate()));
                    hashMap.put("endDate", simpleDateFormat.format(user.getExpiredDate()));
                    allList.add(hashMap);
                }
                form.setAllList(allList);
            }
            List<SyscomrolemembersAndCulture> selecteList = commonService.getSelectedUserMembersById(form.getId());
            form.setSelectList(selecteList);
            return form;
        } catch (SafeaaException se) {
            this.errorMessage(se, se.getMessage());
            form.setMessage(MessageType.INFO, se.getMessage());
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            form.setMessage(MessageType.DANGER, QueryFail);
        }
        return form;
    }

    /**
     * 查詢按鈕
     */
    @PostMapping(value = "/common/UI_080040/queryClick")
    @ResponseBody
    public UI_080040_Form queryClick(@RequestBody UI_080040_Form form) {
        try {
//            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            boolean flg = commonService.deleteAllRoleMembers(form.getPid());
//            if(flg){
            if (form.getIds() != null) {
                for (int i = 0; i < form.getIds().size(); i++) {
                    Syscomrolemembers member = new Syscomrolemembers();
                    member.setRoleid(form.getPid());
                    member.setChildid(form.getIds().get(i));
                    member.setChildtype("U");
//                    member.setEffectdate(sdf1.parse(sdf2.format(new Date()) + " 00:00:00"));
//                    member.setExpireddate(sdf1.parse("2039-12-31 00:00:00"));
                    member.setEffectdate(safeSettings.getEffectDate(null));
                    member.setExpireddate(safeSettings.getExpiredDate(null));
                    member.setUpdatetime(Calendar.getInstance().getTime());
                    member.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));
                    commonService.insertRoleMember(member);
                }
            }
//            }
            List<SyscomroleInfoVo> list = commonService.getSyscomroleInfoVoAll();
            if (list == null) {
                form.setMessage(MessageType.INFO, QueryNoData);
            } else {
                form.setDataList(list);
                form.setMessage(MessageType.SUCCESS, UpdateSuccess);
            }
        } catch (SafeaaException se) {
            this.errorMessage(se, se.getMessage());
            form.setMessage(MessageType.INFO, se.getMessage());
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            form.setMessage(MessageType.DANGER, UpdateFail);
        }
        return form;
    }
}
