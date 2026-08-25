package com.syscom.fep.web.form.common;

import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.BaseForm;
import com.syscom.safeaa.mybatis.model.Syscomgroup;
import com.syscom.safeaa.mybatis.model.Syscomgroupmembers;
import com.syscom.safeaa.mybatis.vo.SyscomgroupInfoVo;
import com.syscom.safeaa.mybatis.vo.SyscomgroupmembersAndGroupLevel;

import java.util.HashMap;
import java.util.List;

public class UI_080030_Form extends BaseForm {

    private Integer pid;

    private Integer id;

    private String no;

    private String name;

    private String resourceUrl;

    private String effectdate;

    private String expireddate;

    private String type;

    private List<SyscomgroupInfoVo> dataList;

    private List<Syscomgroup> groupList;

    private List<Syscomgroupmembers> groupmembersList;

    private List<HashMap<String,String>> allList;

    private List<SyscomgroupmembersAndGroupLevel> selectList;

    private List<Integer> ids;

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

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getEffectdate() {
        return effectdate;
    }

    public void setEffectdate(String effectdate) {
        this.effectdate = effectdate;
    }

    public String getExpireddate() {
        return expireddate;
    }

    public void setExpireddate(String expireddate) {
        this.expireddate = expireddate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<SyscomgroupInfoVo> getDataList() {
        return dataList;
    }

    public void setDataList(List<SyscomgroupInfoVo> dataList) {
        this.dataList = dataList;
    }

    public List<Syscomgroup> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<Syscomgroup> groupList) {
        this.groupList = groupList;
    }

    public List<Syscomgroupmembers> getGroupmembersList() {
        return groupmembersList;
    }

    public void setGroupmembersList(List<Syscomgroupmembers> groupmembersList) {
        this.groupmembersList = groupmembersList;
    }

    public List<HashMap<String, String>> getAllList() {
        return allList;
    }

    public void setAllList(List<HashMap<String, String>> allList) {
        this.allList = allList;
    }

    public List<SyscomgroupmembersAndGroupLevel> getSelectList() {
        return selectList;
    }

    public void setSelectList(List<SyscomgroupmembersAndGroupLevel> selectList) {
        this.selectList = selectList;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
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

    public void setMessage(MessageType messageType, String message) {
        this.setMessageType(messageType);
        this.setMessage(message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
