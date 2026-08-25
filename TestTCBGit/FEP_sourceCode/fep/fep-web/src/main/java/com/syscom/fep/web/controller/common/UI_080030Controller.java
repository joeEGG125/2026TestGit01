package com.syscom.fep.web.controller.common;

import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.common.UI_080030_Form;
import com.syscom.fep.web.service.CommonService;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.mybatis.model.*;
import com.syscom.safeaa.mybatis.vo.SyscomgroupInfoVo;
import com.syscom.safeaa.mybatis.vo.SyscomgroupmembersAndGroupLevel;
import com.syscom.safeaa.mybatis.vo.SyscomresourceAndCulture;
import com.syscom.safeaa.utils.SyscomConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * 群組與功能維護
 *
 * @author ChenYang
 */
@Controller
public class UI_080030Controller extends BaseController {
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

    @PostMapping(value = "/common/UI_080030/select")
    @ResponseBody
    public UI_080030_Form selectData() {
        this.infoMessage("開始執行, 條件 = []");
        UI_080030_Form form = new UI_080030_Form();
        try {
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("查詢");
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            List<SyscomgroupInfoVo> list = commonService.getSyscomgroupInfoVoAll();
            if (list == null) {
                form.setMessage(MessageType.INFO, QueryNoData);
            } else {
                form.setDataList(list);
            }
        } catch (SafeaaException e) {
            this.errorMessage(e, e.getMessage());
            form.setMessage(MessageType.INFO, e.getMessage());
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            form.setMessage(MessageType.DANGER, QueryFail);
        }
        return form;
    }

    @PostMapping(value = "/common/UI_080030/update")
    @ResponseBody
    public UI_080030_Form updateData(@RequestBody UI_080030_Form form) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        try {
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            if (form.getPid() == 0L) {
                // Syscomgroup
                Syscomgroup syscomgroup = new Syscomgroup();
                syscomgroup.setGroupid(form.getId());
                syscomgroup.setGroupno(form.getNo());
//                syscomgroup.setEffectdate(simpleDateFormat.parse(form.getEffectdate()));
//                syscomgroup.setExpireddate(simpleDateFormat.parse(form.getExpireddate()));
                syscomgroup.setEffectdate(safeSettings.getEffectDate(form.getEffectdate()));
                syscomgroup.setExpireddate(safeSettings.getExpiredDate(form.getExpireddate()));
                syscomgroup.setUpdatetime(Calendar.getInstance().getTime());
                syscomgroup.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));
                // Syscomgroupculture
                Syscomgroupculture syscomgroupculture = new Syscomgroupculture();
                syscomgroupculture.setGroupid(form.getId());
                syscomgroupculture.setGroupname(form.getName());
                // 記錄auditLog
                AuditLog auditLog = new AuditLog();
                // 塞入auditLog.action
                auditLog.setAction("修改");
                // 最後塞入ThreadLocal變量中
                setAuditLog(auditLog);
                Integer groupId = commonService.getGroupIdByNo(form.getNo());
                if (groupId != null && groupId.intValue() != form.getId().intValue()) {
                    form.setMessage(MessageType.DANGER, "該群組編號已存在");
                    return form;
                }
                boolean flag = commonService.updateGroup(syscomgroup, syscomgroupculture);
                if (!flag) {
                    form.setMessage(MessageType.DANGER, "群組資源修改失敗");
                    return form;
                }
            } else {
                // Syscomresource
                Syscomresource syscomresource = new Syscomresource();
                syscomresource.setResourceid(form.getId());
                syscomresource.setResourceno(form.getNo());
                syscomresource.setResourceurl(form.getResourceUrl());
//                syscomresource.setEffectdate(simpleDateFormat.parse(form.getEffectdate()));
//                syscomresource.setExpireddate(simpleDateFormat.parse(form.getExpireddate()));
                syscomresource.setEffectdate(safeSettings.getEffectDate(form.getEffectdate()));
                syscomresource.setExpireddate(safeSettings.getExpiredDate(form.getExpireddate()));
                syscomresource.setUpdatetime(Calendar.getInstance().getTime());
                syscomresource.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));
                // Syscomresourceculture
                Syscomresourceculture syscomresourceculture = new Syscomresourceculture();
                syscomresourceculture.setResourceid(form.getId());
                syscomresourceculture.setResourcename(form.getName());
                // do query
                Integer resourceId = commonService.getResourceIdByNo(form.getNo());
                if (resourceId != null && resourceId.intValue() != form.getId().intValue()) {
                    form.setMessage(MessageType.DANGER, "該資源編號已存在");
                    return form;
                }
                // do update
                boolean flag = commonService.updateResource(syscomresource, syscomresourceculture);
                if (!flag) {
                    form.setMessage(MessageType.DANGER, "資源修改失敗");
                    return form;
                }
            }
            List<SyscomgroupInfoVo> list = commonService.getSyscomgroupInfoVoAll();
            if (list == null) {
                form.setMessage(MessageType.INFO, QueryNoData);
            } else {
                form.setDataList(list);
            }
            form.setMessage(MessageType.SUCCESS, UpdateSuccess);
        } catch (SafeaaException e) {
            this.errorMessage(e, e.getMessage());
            form.setMessage(MessageType.INFO, e.getMessage());
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            form.setMessage(MessageType.DANGER, UpdateFail);
        }
        return form;
    }

    @PostMapping(value = "/common/UI_080030/insert")
    @ResponseBody
    public UI_080030_Form insertData(@RequestBody UI_080030_Form form) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        try {
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if ("G".equals(form.getType())) {
                Syscomgroup syscomgroup = new Syscomgroup();
                syscomgroup.setGroupno(form.getNo());
//                syscomgroup.setEffectdate(simpleDateFormat.parse(form.getEffectdate()));
//                syscomgroup.setExpireddate(simpleDateFormat.parse(form.getExpireddate()));
                syscomgroup.setEffectdate(safeSettings.getEffectDate(form.getEffectdate()));
                syscomgroup.setExpireddate(safeSettings.getExpiredDate(form.getExpireddate()));
                syscomgroup.setUpdatetime(Calendar.getInstance().getTime());
                syscomgroup.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));
                Syscomgroupculture syscomgroupculture = new Syscomgroupculture();
                syscomgroupculture.setGroupid(form.getId());
                syscomgroupculture.setGroupname(form.getName());
                Integer groupId = commonService.getGroupIdByNo(form.getNo());
                if (groupId != null) {
                    form.setMessage(MessageType.DANGER, "該群組編號已存在");
                    return form;
                }
                boolean flag = commonService.insertGroup(syscomgroup, syscomgroupculture);
                if (!flag) {
                    form.setMessage(MessageType.DANGER, "群組資源新增失敗");
                    return form;
                }
                if (form.getPid() != 0L) {
                    Syscomgroupmembers member = new Syscomgroupmembers();
                    Integer id = commonService.getGroupIdByNo(form.getNo());
                    member.setGroupid(form.getPid());
                    member.setChildid(id);
                    member.setChildtype("G");
//                    member.setEffectdate(sdf1.parse(simpleDateFormat.format(new Date()) + " 00:00:00"));
//                    member.setExpireddate(sdf1.parse("2039-12-31 00:00:00"));
                    member.setEffectdate(safeSettings.getEffectDate(form.getEffectdate()));
                    member.setExpireddate(safeSettings.getExpiredDate(form.getExpireddate()));
                    member.setUpdatetime(Calendar.getInstance().getTime());
                    member.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));
                    commonService.insertGroupMember(member);
                }
            } else {
                Syscomresource syscomresource = new Syscomresource();
                syscomresource.setResourceid(form.getId());
                syscomresource.setResourceno(form.getNo());
                syscomresource.setResourceurl(form.getResourceUrl());
//                syscomresource.setEffectdate(simpleDateFormat.parse(form.getEffectdate()));
//                syscomresource.setExpireddate(simpleDateFormat.parse(form.getExpireddate()));
                syscomresource.setEffectdate(safeSettings.getEffectDate(form.getEffectdate()));
                syscomresource.setExpireddate(safeSettings.getExpiredDate(form.getExpireddate()));
                syscomresource.setUpdatetime(Calendar.getInstance().getTime());
                syscomresource.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));
                Syscomresourceculture syscomresourceculture = new Syscomresourceculture();
                syscomresourceculture.setResourceid(form.getId());
                syscomresourceculture.setResourcename(form.getName());
                Integer resourceId = commonService.getResourceIdByNo(form.getNo());
                if (resourceId != null) {
                    form.setMessage(MessageType.DANGER, "該資源編號已存在");
                    return form;
                }
                boolean flag = commonService.insertResource(form.getId(), syscomresource, syscomresourceculture);
                if (!flag) {
                    form.setMessage(MessageType.DANGER, "資源新增失敗");
                    return form;
                }
            }
            List<SyscomgroupInfoVo> list = commonService.getSyscomgroupInfoVoAll();
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

    @PostMapping(value = "/common/UI_080030/delete")
    @ResponseBody
    public UI_080030_Form deleteData(@RequestBody UI_080030_Form form) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        try {
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("刪除");
            if (form.getPid() == 0L) {
                // 塞入auditLog.params
                auditLog.addParam("群組ID", form.getId());
                // 最後塞入ThreadLocal變量中
                setAuditLog(auditLog);
                boolean flag = commonService.deleteGroup(form.getId());
                if (!flag) {
                    form.setMessage(MessageType.DANGER, DeleteFail);
                }
            } else {
                // 塞入auditLog.params
                auditLog.addParam("功能ID", form.getId());
                // 最後塞入ThreadLocal變量中
                setAuditLog(auditLog);
                boolean flag = commonService.deleteResource(form.getId());
                if (!flag) {
                    form.setMessage(MessageType.DANGER, DeleteFail);
                }
            }
            List<SyscomgroupInfoVo> list = commonService.getSyscomgroupInfoVoAll();
            if (list == null) {
                form.setMessage(MessageType.INFO, QueryNoData);
            } else {
                form.setDataList(list);
            }
            form.setMessage(MessageType.SUCCESS, DeleteSuccess);
        } catch (SafeaaException se) {
            this.errorMessage(se, se.getMessage());
            form.setMessage(MessageType.INFO, se.getMessage());
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            form.setMessage(MessageType.DANGER, DeleteFail);
        }
        return form;
    }

    @PostMapping(value = "/common/UI_080030/updateOrder")
    @ResponseBody
    public UI_080030_Form updateOrder(@RequestBody UI_080030_Form form) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        try {
            boolean flag = commonService.updateGroupmembers(form.getGroupList(), form.getGroupmembersList());
            if (!flag) {
                form.setMessage(MessageType.DANGER, UpdateFail);
            }
            List<SyscomgroupInfoVo> list = commonService.getSyscomgroupInfoVoAll();
            if (list == null) {
                form.setMessage(MessageType.INFO, QueryNoData);
            } else {
                form.setDataList(list);
            }
            form.setGroupmembersList(null);
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

    /**
     * 查詢按鈕
     */
    @PostMapping(value = "/common/UI_080030/checkTreeNode")
    @ResponseBody
    public UI_080030_Form checkTreeNode(@RequestBody UI_080030_Form form) {
        List<HashMap<String, String>> allList = new ArrayList<>();
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            List<SyscomresourceAndCulture> infoList = commonService.getAllResources();
            if (infoList != null) {
                for (SyscomresourceAndCulture vo : infoList) {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", vo.getResourceid().toString());
                    hashMap.put("pNo", vo.getResourceno());
                    hashMap.put("url", vo.getResourceurl());
                    hashMap.put("name", vo.getResourcename());
                    hashMap.put("startDate", simpleDateFormat.format(vo.getEffectdate()));
                    hashMap.put("endDate", simpleDateFormat.format(vo.getExpireddate()));
                    allList.add(hashMap);
                }
                form.setAllList(allList);
            }
            List<SyscomgroupmembersAndGroupLevel> selecteList = commonService.getSelectedGroupMembersById(form.getId());
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
    @PostMapping(value = "/common/UI_080030/queryClick")
    @ResponseBody
    public UI_080030_Form queryClick(@RequestBody UI_080030_Form form) {
        try {
//            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            boolean flg = commonService.deleteAllGroupMembers(form.getPid());
//            if(flg){
            if (form.getIds() != null) {
                for (int i = 0; i < form.getIds().size(); i++) {
                    Syscomgroupmembers member = new Syscomgroupmembers();
                    member.setGroupid(form.getPid());
                    member.setChildid(form.getIds().get(i));
                    member.setChildtype("R");
//                    member.setEffectdate(sdf1.parse(sdf2.format(new Date()) + " 00:00:00"));
//                    member.setExpireddate(sdf1.parse("2039-12-31 00:00:00"));
                    member.setEffectdate(safeSettings.getEffectDate(null));
                    member.setExpireddate(safeSettings.getExpiredDate(null));
                    member.setLocationno(i);
                    member.setUpdatetime(Calendar.getInstance().getTime());
                    member.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));
                    commonService.insertGroupMember(member);
                }
            }
//            }
            List<SyscomgroupInfoVo> list = commonService.getSyscomgroupInfoVoAll();
            if (list == null) {
                form.setMessage(MessageType.INFO, QueryNoData);
            } else {
                form.setDataList(list);
            }
            form.setMessage(MessageType.SUCCESS, QuerySuccess);
        } catch (SafeaaException se) {
            this.errorMessage(se, se.getMessage());
            form.setMessage(MessageType.INFO, se.getMessage());
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            form.setMessage(MessageType.DANGER, QueryFail);
        }
        return form;
    }
}
