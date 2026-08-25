package com.syscom.fep.web.controller.common;

import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.common.SyscomroleInfoVo;
import com.syscom.fep.web.form.common.UI_080050_Form;
import com.syscom.fep.web.service.CommonService;
import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.mybatis.vo.SyscomSelectResourcesVo;
import com.syscom.safeaa.mybatis.vo.SyscomgroupInfoVo;
import com.syscom.safeaa.mybatis.vo.SyscomroleAndCulture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;


/**
 * For Safeaa
 *
 * @author ChenYang
 */
@Controller
public class UI_080050Controller extends BaseController {

    @Autowired
    private CommonService commonService;

    @PostMapping(value = "/common/UI_080050/select")
    @ResponseBody
    public UI_080050_Form selectData() {
        this.infoMessage("開始執行, 條件 = []");
        UI_080050_Form form = new UI_080050_Form();
        try {
            List<SyscomgroupInfoVo> groupList = commonService.getSyscomgroupInfoVoAll();
            form.setGroupInfoList(groupList);

            List<SyscomroleInfoVo> roleInfoList = new ArrayList<SyscomroleInfoVo>();
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("查詢");
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            List<SyscomroleAndCulture> roleList = commonService.getAllRoles();
            for(SyscomroleAndCulture role:roleList){
                SyscomroleInfoVo info = new SyscomroleInfoVo();
                info.setRoleid(role.getRoleid());
                info.setRolename(role.getRolename());
                info.setRoleno(role.getRoleno());

                List<Integer> resourceids = new ArrayList<Integer>();
                List<SyscomSelectResourcesVo> resourcesList=commonService.getSelectedResourcesByRoleId(role.getRoleid());
                for(SyscomSelectResourcesVo vo:resourcesList){
                    resourceids.add(vo.getResourceid());
                }
                info.setResourceids(resourceids);
                roleInfoList.add(info);
            }
            form.setRoleInfoList(roleInfoList);

        } catch (SafeaaException se) {
            form.setMessage(MessageType.INFO,se.getMessage());
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,QueryFail);
        }
        return form;
    }

    @PostMapping(value = "/common/UI_080050/updateRole")
    @ResponseBody
    public UI_080050_Form updateOrder(@RequestBody UI_080050_Form form) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        try {
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("更新送審");
            // 塞入auditLog.params
            if (org.apache.commons.lang3.StringUtils.isNotBlank(form.getRoleId()!=null?form.getRoleId().toString():""))
                auditLog.addParam("角色ID",  form.getRoleId());
            if (form.getGroupList()!=null)
                auditLog.addParam("群組列表",  form.getGroupList());
            if (form.getResourceList()!=null)
                auditLog.addParam("資源列表",  form.getResourceList());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            boolean flg = commonService.updateRoleGroupResource(form.getRoleId(),form.getGroupList(),form.getResourceList());
//            if(flg){
                form.setMessage(MessageType.SUCCESS,UpdateSuccess);
//            }else{
//                form.setMessage(MessageType.DANGER,UpdateFail);
//            }
        } catch (SafeaaException se) {
            form.setMessage(MessageType.INFO,se.getMessage());
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,UpdateFail);
        }
        return form;
    }
}
