package com.syscom.fep.gateway.configuration;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

// @Validated
public class GatewayRegister {
	@NotNull
	private String gtwClassName;
	@NotNull
	private String hdrClassName;
	@NotNull
	private String cnfClassName;
	private String mgrClassName;
	private String prcClassName;
	private String ipfClassName;

	public String getGtwClassName() {
		return gtwClassName;
	}

	public void setGtwClassName(String gtwClassName) {
		this.gtwClassName = gtwClassName;
	}

	public String getHdrClassName() {
		return hdrClassName;
	}

	public void setHdrClassName(String hdrClassName) {
		this.hdrClassName = hdrClassName;
	}

	public String getCnfClassName() {
		return cnfClassName;
	}

	public void setCnfClassName(String cnfClassName) {
		this.cnfClassName = cnfClassName;
	}

	public String getMgrClassName() {
		return mgrClassName;
	}

	public void setMgrClassName(String mgrClassName) {
		this.mgrClassName = mgrClassName;
	}

	public String getPrcClassName() {
		return prcClassName;
	}

	public void setPrcClassName(String prcClassName) {
		this.prcClassName = prcClassName;
	}

	public String getIpfClassName() {
		return ipfClassName;
	}

	public void setIpfClassName(String ipfClassName) {
		this.ipfClassName = ipfClassName;
	}
}
