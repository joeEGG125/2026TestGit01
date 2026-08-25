package com.syscom.fep.web.service;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.mybatis.ext.mapper.FepuserExtMapper;
import com.syscom.fep.mybatis.ext.mapper.ReportdepExtMapper;
import com.syscom.fep.mybatis.ext.mapper.ZoneExtMapper;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.mybatis.util.SpCaller;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.enums.SAFEMessageId;
import com.syscom.safeaa.mybatis.model.*;
import com.syscom.safeaa.mybatis.vo.*;
import com.syscom.safeaa.security.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CommonService extends BaseService{

    @Autowired
    private UserImpl userImpl;

    @Autowired
    private GroupImpl groupImpl;

    @Autowired
    private ResourceImpl resourceImpl;

    @Autowired
    private AuthenticationImpl authenticationImpl;

    @Autowired
    private RoleImpl roleImpl;

    @Autowired
    private RoleGroupImpl roleGroupImpl;

    @Autowired
    private RoleResourceImpl roleResourceImpl;

    @Autowired
    private FepuserExtMapper fepuserExtMapper;

    @Autowired
    private SpCaller spCaller;

    @Autowired
    private ZoneExtMapper zoneExtMapper;
    @Autowired
    private ReportdepExtMapper reportDepExtMapper;

    // 以登入帳號(LogOnId)查出該筆使用者的使用者序號
    public SyscomuserInfoVo getSyscomuserInfo(String logOnId) throws Exception {
        SyscomuserInfoVo vo = null;
        try {
            vo = userImpl.getSyscomuserInfo(logOnId);
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return vo;
    }

    public Syscomuser queryUsersById(Integer userId) throws Exception {
        Syscomuser user = null;
        try {
            Syscomuser syscomuser = new Syscomuser();
            syscomuser.setUserid(userId);
            user = userImpl.getUserById(syscomuser);
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return user;
    }

    public Fepuser queryFepUserInfo(Integer fepUserId) throws Exception {
        Fepuser fepuser = null;
        try {
            fepuser = fepuserExtMapper.selectByPrimaryKey(fepUserId);
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return fepuser;
    }

    public PageInfo<HashMap<String, Object>> queryUsersBy(String logOnId, String userName, String sort, Integer pageNum, Integer pageSize) throws Exception {
        try {
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    try {
                        userImpl.queryUsersBy(logOnId, userName, sort);
                    } catch (Exception e) {
                        throw ExceptionUtil.createRuntimeException(e, e.getMessage());
                    }
                }
            });
            return pageInfo;
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
    }

    public List<SyscomgroupInfoVo> getSyscomgroupInfoVoAll() throws Exception {
        List<SyscomgroupInfoVo> list = null;
        try {
            list = groupImpl.getSyscomgroupInfoVoAll();
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return list;
    }

    public boolean deleteGroup(Integer groupId) throws Exception {
        try {
            return groupImpl.deleteGroup(groupId);
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
    }

    public boolean deleteResource(Integer resourceId) throws Exception {
        try {
            return resourceImpl.deleteResource(resourceId);
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
    }

    public boolean deleteUserById(Integer userid, Integer empid) throws Exception {
        try {
            Fepuser fepuser = new Fepuser();
            fepuser.setFepuserUserid(empid);
            int r = fepuserExtMapper.deleteByPrimaryKey(fepuser);
            boolean flg = false;
            if (r > 0) {
                flg = userImpl.deleteUser(userid);
            }
            return flg;
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
    }

    public boolean deleteRole(Integer roleId) throws Exception {
        try {
            boolean flg = roleImpl.deleteRole(roleId);
            if (flg) {
                return true;
            }
            return false;
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
    }

    public boolean updateGroup(Syscomgroup syscomgroup, Syscomgroupculture syscomgroupculture) throws Exception {
        try {
            int rst = groupImpl.updateGroup(syscomgroup);
            if (rst > 0) {
                rst = groupImpl.updateGroupCulture(syscomgroupculture);
            }
            if (rst > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
    }

    public boolean updateResource(Syscomresource syscomresource, Syscomresourceculture syscomresourceculture) throws Exception {
        try {
            int rst = resourceImpl.updateResource(syscomresource);
            if (rst > 0) {
                rst = resourceImpl.updateResourceCulture(syscomresourceculture);
            }
            if (rst > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
    }

    public boolean updateGroupmembers(List<Syscomgroup> groupList, List<Syscomgroupmembers> groupmembersList) throws Exception {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            for (Syscomgroup group : groupList) {
                group.setUpdatetime(Calendar.getInstance().getTime());
                group.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));
                groupImpl.updateGroup(group);
                groupImpl.removeAllGroupMembers(group.getGroupid());
            }
            for (Syscomgroupmembers groupmembers : groupmembersList) {
                groupmembers.setChildtype(groupmembers.getChildtype());
                groupmembers.setEffectdate(simpleDateFormat.parse(simpleDateFormat.format(new Date())));
                groupmembers.setExpireddate(simpleDateFormat.parse("2039-12-31"));
                groupmembers.setUpdatetime(Calendar.getInstance().getTime());
                groupmembers.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));
                groupImpl.addGroupMembers(groupmembers);
            }
            return true;
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
    }

    public boolean updatetUser(Syscomuser syscomuser, Fepuser fepuser) throws Exception {
        try {
            int r = fepuserExtMapper.updateByPrimaryKeySelective(fepuser);
            int rst = 0;
            if (r > 0) {
                rst = userImpl.updateUser(syscomuser);
                if (rst > 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return false;
    }

    public boolean updateRole(Syscomrole syscomrole, Syscomroleculture syscomroleculture) throws Exception {
        try {
            int rst = roleImpl.updateRole(syscomrole);
            if (rst > 0) {
                rst = roleImpl.updateRoleCulture(syscomroleculture);
            }
            if (rst > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
    }

    public boolean insertGroup(Syscomgroup syscomgroup, Syscomgroupculture syscomgroupculture) throws Exception {
        try {
            int rst = groupImpl.createGroup(syscomgroup);
            if (rst > 0) {
                Integer groupId = groupImpl.getGroupIdByNo(syscomgroup.getGroupno());
                syscomgroupculture.setGroupid(groupId);
                rst = groupImpl.addGroupCulture(syscomgroupculture);
            }
            if (rst > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            sendEMS(e);
            return false;
        }
    }

    public boolean insertResource(Integer groupId, Syscomresource syscomresource, Syscomresourceculture syscomresourceculture) throws Exception {
        try {

            int rst = resourceImpl.createResource(syscomresource);
            if (rst > 0) {
                Integer resourceId = resourceImpl.getResourceIdByNo(syscomresource.getResourceno());
                syscomresourceculture.setResourceid(resourceId);
                rst = resourceImpl.addResourceCulture(syscomresourceculture);
            }
            if (rst > 0) {
                Syscomgroupmembers syscomgroupmembers = new Syscomgroupmembers();
                syscomgroupmembers.setGroupid(groupId);
                syscomgroupmembers.setChildid(syscomresource.getResourceid());
                syscomgroupmembers.setChildtype("R");
                syscomgroupmembers.setEffectdate(syscomresource.getEffectdate());
                syscomgroupmembers.setExpireddate(syscomresource.getExpireddate());
                syscomgroupmembers.setUpdatetime(syscomresource.getUpdatetime());
                syscomgroupmembers.setUpdateuserid(syscomresource.getUpdateuserid());
                rst = groupImpl.addGroupMembers(syscomgroupmembers);
            }
            if (rst > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            sendEMS(e);
            return false;
        }
    }

    public boolean insertUser(Syscomuser syscomuser, Fepuser fepuser) {
        try {
            int result = fepuserExtMapper.insertSelective(fepuser);
            if (result > 0) {
                syscomuser.setEmpid(fepuser.getFepuserUserid());
                userImpl.createUser(syscomuser, 5, "", "", syscomuser.getLogonid() + "000000");
            }
        } catch (Exception e) {
            sendEMS(e);
            return false;
        }
        return true;
    }

    public boolean insertRole(Syscomrole syscomrole, Syscomroleculture syscomroleculture) throws Exception {
        try {
            int rst = roleImpl.createRole(syscomrole);
            if (rst > 0) {
                Integer roleId = roleImpl.getRoleIdByNo(syscomrole.getRoleno());
                syscomroleculture.setRoleid(roleId);
                rst = roleImpl.addRoleCulture(syscomroleculture);
            }
            if (rst > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            sendEMS(e);
            return false;
        }
    }

    public boolean unlockAccount(Integer userId, String logOnId, String logOnIp, String logOnIdUnlocker) throws Exception {
        try {
            return authenticationImpl.unlockAccount(userId, logOnId, logOnIp, logOnIdUnlocker);
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
    }

    public boolean restPassword(Integer userId, String logOnId, String logOnIp) throws Exception {
        try {
            return authenticationImpl.restPassword(userId, logOnId, logOnIp, logOnId + "000000");
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public boolean changPassword(Integer userId, String logOnId, String oldSscod, String newSscod, String logOnIp) throws Exception {
        try {
            return authenticationImpl.changePassword(userId, logOnId, oldSscod, newSscod, logOnIp);
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
    }

    public Syscomgroup getGroupById(Syscomgroup group) throws Exception {
        Syscomgroup syscomgroup = null;
        try {
            syscomgroup = groupImpl.getGroupById(group);
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return syscomgroup;
    }

    public Integer getGroupIdByNo(String groupNo) throws Exception {
        Integer groupId = null;
        try {
            groupId = groupImpl.getGroupIdByNo(groupNo);
        } catch (SafeaaException e) {
            if (e.getMessageId() == SAFEMessageId.QueryNoRecord) {
                return null;
            } else {
                throw e;
            }
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return groupId;
    }

    public Integer getResourceIdByNo(String resourceNo) throws Exception {
        Integer resourceId = null;
        try {
            resourceId = resourceImpl.getResourceIdByNo(resourceNo);
        } catch (SafeaaException e) {
            if (e.getMessageId() == SAFEMessageId.QueryNoRecord) {
                return null;
            } else {
                throw e;
            }
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return resourceId;
    }

    public Integer getRoleIdByNo(String roleNo) throws Exception {
        Integer roleId = 0;
        try {
            roleId = roleImpl.getRoleIdByNo(roleNo);
        } catch (SafeaaException se) {
            return null;
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return roleId;
    }

    public List<SyscomroleAndCulture> getAllRoles() throws Exception {
        List<SyscomroleAndCulture> list = null;
        try {
            list = roleImpl.getAllRoles("");

        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return list;
    }

    public List<SyscomSelectResourcesVo> getSelectedResourcesByRoleId(Integer roleId) throws Exception {
        List<SyscomSelectResourcesVo> list = null;
        try {
            list = roleResourceImpl.getSelectedResourcesByRoleId(roleId, "");
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return list;
    }

    public boolean updateRoleGroupResource(Integer roleId, List<Integer> groupList, List<Integer> resourceList) throws Exception {
        try {
            roleResourceImpl.removeRoleResourcesByRoleId(roleId);
            roleGroupImpl.removeRoleGroupsByRoleId(roleId);
            if (groupList != null && groupList.size() > 0) {
                for (Integer gorupId : groupList) {
                    Syscomrolegroup group = new Syscomrolegroup();
                    group.setRoleid(roleId);
                    group.setGroupid(gorupId);
                    group.setChildtype("G");
                    group.setUpdatetime(Calendar.getInstance().getTime());
                    group.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));
                    roleGroupImpl.addRoleGroup(group);
                }
            }
            if (resourceList != null && resourceList.size() > 0) {
                for (Integer resourceId : resourceList) {
                    Syscomroleresource resource = new Syscomroleresource();
                    resource.setRoleid(roleId);
                    resource.setResourceid(resourceId);
                    resource.setUpdatetime(Calendar.getInstance().getTime());
                    resource.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));
                    roleResourceImpl.addRoleResource(resource);
                }
            }

            return true;
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
    }

    public List<SyscomgroupmembersAndGroupLevel> getSelectedGroupMembersById(Integer groupId) throws Exception {
        List<SyscomgroupmembersAndGroupLevel> list = null;
        try {
            list = groupImpl.getSelectedMembersById(groupId, null);
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return list;
    }

    public List<SyscomrolemembersAndCulture> getSelectedUserMembersById(Integer roleId) throws Exception {
        List<SyscomrolemembersAndCulture> list = null;
        try {
            list = roleImpl.getSelectedMembersById(roleId, null);
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return list;
    }

    public boolean deleteAllGroupMembers(Integer groupId) throws Exception {
        try {
            groupImpl.removeAllGroupMembers(groupId);
            return true;
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
    }

    public boolean deleteAllRoleMembers(Integer roleId) throws Exception {
        try {
            roleImpl.removeAllRoleMembers(roleId);
            return true;
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
    }

    public int insertGroupMember(Syscomgroupmembers member) {
        try {
            return groupImpl.addGroupMembers(member);
        } catch (Exception e) {
            sendEMS(e);
            return -1;
        }
    }

    public int insertRoleMember(Syscomrolemembers member) throws Exception {
        try {
            return roleImpl.addRoleMembers(member);
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
    }

    public List<SyscomresourceAndCulture> getAllResources() throws Exception {
        List<SyscomresourceAndCulture> list = null;
        try {
            list = resourceImpl.getAllResources("");
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return list;
    }

    public List<SyscomQueryAllUsers> getAllUsers() throws Exception {
        List<SyscomQueryAllUsers> list = null;
        try {
            list = userImpl.getAllUsers("");
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return list;
    }

    public List<SyscomroleInfoVo> getSyscomroleInfoVoAll() throws Exception {
        List<SyscomroleInfoVo> list = null;
        try {
            list = roleImpl.getSyscomroleInfoVoAll();
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return list;
    }

    public List<Fepuser> queryFepUserInfoAll() throws Exception {
        List<Fepuser> fepuser = null;
        try {
            fepuser = fepuserExtMapper.getFEPUSER();
        } catch (Exception e) {
            sendEMS(e);
            throw e;
        }
        return fepuser;
    }

    public List<Map<String, Object>> queryAllMembers(String loginID) {
        return spCaller.queryAllMembersByBossID(loginID);
    }

    public List<HashMap<String, Object>> queryAllAuditData(Webaudit audit, String dtbegin, String dtend, String bossid, String programid, String displayShowAudit, Integer pageNum, Integer pageSize, String sqlSortExpression) throws Exception {
        return spCaller.queryAllAuditData(audit, dtbegin, dtend, bossid, programid, displayShowAudit, pageNum, pageSize, sqlSortExpression);
    }

    /**
     * getAreaCode : 取得所有地區
     * @return
     */
    public List<Zone> getAreaCode() {
        return this.zoneExtMapper.selectAll();
    }

    public PageInfo<Reportdep> getBankData(Reportdep reportdep, Integer pageNum, Integer pageSize){
    	pageNum = pageNum == null ? 0 : pageNum;
        pageSize = pageSize == null ? 0 : pageSize;
        PageInfo<Reportdep> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
            	reportDepExtMapper.getBankData(reportdep);
            }
        });
        return pageInfo;
    }


    public List<Reportdep> getBankAll() {
        return reportDepExtMapper.queryBankAll();
    }


    public List<Reportdep> getBankByDepName(String reportdepDepname, String direction) {
        return reportDepExtMapper.getBankByDepName(reportdepDepname, direction);
    }

    public Reportdep getBankByDepNo(String DepNo) {
        return reportDepExtMapper.selectByPrimaryKey(DepNo);
    }

    public Integer deleteBank(Reportdep reportdep) {
        try {
            return reportDepExtMapper.deleteByPrimaryKey(reportdep);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    public Integer insertBank(Reportdep reportdep) {
        try {
            return reportDepExtMapper.insertSelective(reportdep);
//            return reportDepExtMapper.insert(reportdep);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    public Integer updateBank(Reportdep reportdep) {
        try {
//            return reportDepExtMapper.updateByPrimaryKey(reportdep);updateByDepNo
            return reportDepExtMapper.updateByDepNo(reportdep);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }
}
