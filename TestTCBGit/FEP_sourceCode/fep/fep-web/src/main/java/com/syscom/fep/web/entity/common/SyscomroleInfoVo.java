package com.syscom.fep.web.entity.common;

import java.io.Serializable;
import java.util.List;

public class SyscomroleInfoVo implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer roleid;
	private String roleno;
	private String rolename;
	private List<Integer> resourceids;

	public Integer getRoleid() {
		return roleid;
	}

	public void setRoleid(Integer roleid) {
		this.roleid = roleid;
	}

	public String getRoleno() {
		return roleno;
	}

	public void setRoleno(String roleno) {
		this.roleno = roleno;
	}

	public String getRolename() {
		return rolename;
	}

	public void setRolename(String rolename) {
		this.rolename = rolename;
	}

	public List<Integer> getResourceids() {
		return resourceids;
	}

	public void setResourceids(List<Integer> resourceids) {
		this.resourceids = resourceids;
	}
}
