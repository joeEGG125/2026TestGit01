package com.syscom.fep.web.form.common;

import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.common.SyscomroleInfoVo;
import com.syscom.fep.web.form.BaseForm;
import com.syscom.safeaa.mybatis.vo.SyscomgroupInfoVo;

import java.util.List;

public class UI_080050_Form extends BaseForm {
    private Integer roleId;

    private List<Integer> groupList;

    private List<Integer> resourceList;

    private List<SyscomgroupInfoVo> groupInfoList;

    private List<SyscomroleInfoVo> roleInfoList;

    /**
     * 執行結果
     */
    private boolean result = true;
    /**
     * 訊息類別
     */
    private MessageType messageType;
    /**
     * 訊息內容
     */
    private String message = "";

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public List<Integer> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<Integer> groupList) {
        this.groupList = groupList;
    }

    public List<Integer> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<Integer> resourceList) {
        this.resourceList = resourceList;
    }

    public List<SyscomgroupInfoVo> getGroupInfoList() {
        return groupInfoList;
    }

    public void setGroupInfoList(List<SyscomgroupInfoVo> groupInfoList) {
        this.groupInfoList = groupInfoList;
    }

    public List<SyscomroleInfoVo> getRoleInfoList() {
        return roleInfoList;
    }

    public void setRoleInfoList(List<SyscomroleInfoVo> roleInfoList) {
        this.roleInfoList = roleInfoList;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
        if (messageType == MessageType.DANGER) {
            this.result = false;
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessage(MessageType messageType, String message) {
        this.setMessageType(messageType);
        this.setMessage(message);
    }
}
