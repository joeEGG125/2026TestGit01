package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.web.form.BaseForm;

public class UI_060290_Form extends BaseForm {

	private static final long serialVersionUID = 1L;

	private Sysstat sysstat;
	
	private String hbkno;

	// 1-1:自行21
	private boolean sysstatIntra;
	private boolean sysstatIwdI;
	private boolean sysstatIftI;
	private boolean sysstatAdmI;
	private boolean sysstatIiqI;
	private boolean sysstatFwdI;
	private boolean sysstatNwdI;
	private boolean sysstatIpyI;
	private boolean sysstatIccdpI;
	private boolean sysstatEtxI;
	private boolean sysstatCaI;
	private boolean sysstatCaaI;
	private boolean sysstatNfwI;
	private boolean sysstatAig;
	private boolean sysstatHkIssue;
	private boolean sysstatMoIssue;
	private boolean sysstatHkFiscmb;
	private boolean sysstatHkFiscmq;
	private boolean sysstatMoFiscmb;
	private boolean sysstatMoFiscmq;
	private boolean sysstatHkPlus;
	private boolean sysstatMoPlus;

	// 1-2:代理21
	private boolean sysstatAgent;
	private boolean sysstatIwdA;
	private boolean sysstatFawA;
	private boolean sysstatIftA;
	private boolean sysstatAdmA;
	private boolean sysstatIpyA;
	private boolean sysstatIccdpA;
	private boolean sysstatEtxA;
	private boolean sysstatCdpA;
	private boolean sysstat2525A;
	private boolean sysstatCpuA;
	private boolean sysstatCafA;
	private boolean sysstatCavA;
	private boolean sysstatCamA;
	private boolean sysstatCajA;
	private boolean sysstatCauA;
	private boolean sysstatCwvA;
	private boolean sysstatCwmA;
	private boolean sysstatEafA;
	private boolean sysstatEavA;
	private boolean sysstatEamA;
	private boolean sysstatEwvA;
	private boolean sysstatEwmA;
	private boolean sysstatNwdA;
	private boolean sysstatVaaA;
	//20220912 Bruce add 依照Candy需求修改 Start
	private boolean sysstatEajA;
	//20220912 Bruce add 依照Candy需求修改 End
	// 1-3:原存15
	private boolean sysstatIssue;
	private boolean sysstatIwdF;
	private boolean sysstatIftF;
	private boolean sysstatIiqF;
	private boolean sysstatIpyF;
	private boolean sysstatIccdpF;
	private boolean sysstatCdpF;
	private boolean sysstatEtxF;
	private boolean sysstat2525F;
	private boolean sysstatCpuF;
	private boolean sysstatGpcadF;
	private boolean sysstatCauF;
	private boolean sysstatGpcwdF;
	private boolean sysstatGpemvF;
	private boolean sysstatGpiwdF;
	private boolean sysstatGpobF;
	private boolean sysstatVaaF;

	// 1-4:純代理12
	private boolean sysstatPure;
	private boolean sysstatIiqP;
	private boolean sysstatIftP;
	private boolean sysstatIccdpP;
	private boolean sysstatCdpP;
	private boolean sysstatIpyP;
	private boolean sysstatIqvP;
	private boolean sysstatIqmP;
	private boolean sysstatIqcP;
	private boolean sysstatEquP;
	private boolean sysstatEqpP;
	private boolean sysstatEqcP;

	// 1-5:T24主機
	private boolean sysstatT24Twn;
	private boolean sysstatT24Hkg;
	private boolean sysstatT24Mac;
	private boolean sysstatCbs;
	private boolean sysstatFedi;
	private boolean sysstatNb;
	private boolean sysstatWebatm;
	private boolean sysstatAscChannel;
	private boolean sysstatAsc;
	private boolean sysstatAscmd;
	private boolean sysstatGcard;
	private boolean sysstatSps;
	private boolean sysstatAscmac;
	private boolean sysstatSpsmac;
	private boolean sysstatSvcs;
	private boolean sysstatHkSms;
	private boolean sysstatPv;
	private boolean sysstatSmtp;

	public Sysstat getSysstat() {
		return sysstat;
	}

	public void setSysstat(Sysstat sysstat) {
		this.sysstat = sysstat;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public boolean isSysstatIntra() {
		return sysstatIntra;
	}

	public void setSysstatIntra(boolean sysstatIntra) {
		this.sysstatIntra = sysstatIntra;
	}

	public boolean isSysstatIwdI() {
		return sysstatIwdI;
	}

	public void setSysstatIwdI(boolean sysstatIwdI) {
		this.sysstatIwdI = sysstatIwdI;
	}

	public boolean isSysstatIftI() {
		return sysstatIftI;
	}

	public void setSysstatIftI(boolean sysstatIftI) {
		this.sysstatIftI = sysstatIftI;
	}

	public boolean isSysstatAdmI() {
		return sysstatAdmI;
	}

	public void setSysstatAdmI(boolean sysstatAdmI) {
		this.sysstatAdmI = sysstatAdmI;
	}

	public boolean isSysstatIiqI() {
		return sysstatIiqI;
	}

	public void setSysstatIiqI(boolean sysstatIiqI) {
		this.sysstatIiqI = sysstatIiqI;
	}

	public boolean isSysstatFwdI() {
		return sysstatFwdI;
	}

	public void setSysstatFwdI(boolean sysstatFwdI) {
		this.sysstatFwdI = sysstatFwdI;
	}

	public boolean isSysstatNwdI() {
		return sysstatNwdI;
	}

	public void setSysstatNwdI(boolean sysstatNwdI) {
		this.sysstatNwdI = sysstatNwdI;
	}

	public boolean isSysstatIccdpI() {
		return sysstatIccdpI;
	}

	public void setSysstatIccdpI(boolean sysstatIccdpI) {
		this.sysstatIccdpI = sysstatIccdpI;
	}

	public boolean isSysstatEtxI() {
		return sysstatEtxI;
	}

	public void setSysstatEtxI(boolean sysstatEtxI) {
		this.sysstatEtxI = sysstatEtxI;
	}

	public boolean isSysstatCaI() {
		return sysstatCaI;
	}

	public void setSysstatCaI(boolean sysstatCaI) {
		this.sysstatCaI = sysstatCaI;
	}

	public boolean isSysstatCaaI() {
		return sysstatCaaI;
	}

	public void setSysstatCaaI(boolean sysstatCaaI) {
		this.sysstatCaaI = sysstatCaaI;
	}

	public boolean isSysstatNfwI() {
		return sysstatNfwI;
	}

	public void setSysstatNfwI(boolean sysstatNfwI) {
		this.sysstatNfwI = sysstatNfwI;
	}

	public boolean isSysstatAig() {
		return sysstatAig;
	}

	public void setSysstatAig(boolean sysstatAig) {
		this.sysstatAig = sysstatAig;
	}

	public boolean isSysstatHkIssue() {
		return sysstatHkIssue;
	}

	public void setSysstatHkIssue(boolean sysstatHkIssue) {
		this.sysstatHkIssue = sysstatHkIssue;
	}

	public boolean isSysstatMoIssue() {
		return sysstatMoIssue;
	}

	public void setSysstatMoIssue(boolean sysstatMoIssue) {
		this.sysstatMoIssue = sysstatMoIssue;
	}

	public boolean isSysstatHkFiscmb() {
		return sysstatHkFiscmb;
	}

	public void setSysstatHkFiscmb(boolean sysstatHkFiscmb) {
		this.sysstatHkFiscmb = sysstatHkFiscmb;
	}

	public boolean isSysstatHkFiscmq() {
		return sysstatHkFiscmq;
	}

	public void setSysstatHkFiscmq(boolean sysstatHkFiscmq) {
		this.sysstatHkFiscmq = sysstatHkFiscmq;
	}

	public boolean isSysstatMoFiscmb() {
		return sysstatMoFiscmb;
	}

	public void setSysstatMoFiscmb(boolean sysstatMoFiscmb) {
		this.sysstatMoFiscmb = sysstatMoFiscmb;
	}

	public boolean isSysstatMoFiscmq() {
		return sysstatMoFiscmq;
	}

	public void setSysstatMoFiscmq(boolean sysstatMoFiscmq) {
		this.sysstatMoFiscmq = sysstatMoFiscmq;
	}

	public boolean isSysstatHkPlus() {
		return sysstatHkPlus;
	}

	public void setSysstatHkPlus(boolean sysstatHkPlus) {
		this.sysstatHkPlus = sysstatHkPlus;
	}

	public boolean isSysstatMoPlus() {
		return sysstatMoPlus;
	}

	public void setSysstatMoPlus(boolean sysstatMoPlus) {
		this.sysstatMoPlus = sysstatMoPlus;
	}

	public boolean isSysstatIpyI() {
		return sysstatIpyI;
	}

	public void setSysstatIpyI(boolean sysstatIpyI) {
		this.sysstatIpyI = sysstatIpyI;
	}

	public boolean isSysstatAgent() {
		return sysstatAgent;
	}

	public void setSysstatAgent(boolean sysstatAgent) {
		this.sysstatAgent = sysstatAgent;
	}

	public boolean isSysstatIwdA() {
		return sysstatIwdA;
	}

	public void setSysstatIwdA(boolean sysstatIwdA) {
		this.sysstatIwdA = sysstatIwdA;
	}

	public boolean isSysstatFawA() {
		return sysstatFawA;
	}

	public void setSysstatFawA(boolean sysstatFawA) {
		this.sysstatFawA = sysstatFawA;
	}

	public boolean isSysstatIftA() {
		return sysstatIftA;
	}

	public void setSysstatIftA(boolean sysstatIftA) {
		this.sysstatIftA = sysstatIftA;
	}

	public boolean isSysstatAdmA() {
		return sysstatAdmA;
	}

	public void setSysstatAdmA(boolean sysstatAdmA) {
		this.sysstatAdmA = sysstatAdmA;
	}

	public boolean isSysstatIpyA() {
		return sysstatIpyA;
	}

	public void setSysstatIpyA(boolean sysstatIpyA) {
		this.sysstatIpyA = sysstatIpyA;
	}

	public boolean isSysstatIccdpA() {
		return sysstatIccdpA;
	}

	public void setSysstatIccdpA(boolean sysstatIccdpA) {
		this.sysstatIccdpA = sysstatIccdpA;
	}

	public boolean isSysstatEtxA() {
		return sysstatEtxA;
	}

	public void setSysstatEtxA(boolean sysstatEtxA) {
		this.sysstatEtxA = sysstatEtxA;
	}

	public boolean isSysstatCdpA() {
		return sysstatCdpA;
	}

	public void setSysstatCdpA(boolean sysstatCdpA) {
		this.sysstatCdpA = sysstatCdpA;
	}

	public boolean isSysstat2525A() {
		return sysstat2525A;
	}

	public void setSysstat2525A(boolean sysstat2525a) {
		sysstat2525A = sysstat2525a;
	}

	public boolean isSysstatCpuA() {
		return sysstatCpuA;
	}

	public void setSysstatCpuA(boolean sysstatCpuA) {
		this.sysstatCpuA = sysstatCpuA;
	}

	public boolean isSysstatCafA() {
		return sysstatCafA;
	}

	public void setSysstatCafA(boolean sysstatCafA) {
		this.sysstatCafA = sysstatCafA;
	}

	public boolean isSysstatCavA() {
		return sysstatCavA;
	}

	public void setSysstatCavA(boolean sysstatCavA) {
		this.sysstatCavA = sysstatCavA;
	}

	public boolean isSysstatCamA() {
		return sysstatCamA;
	}

	public void setSysstatCamA(boolean sysstatCamA) {
		this.sysstatCamA = sysstatCamA;
	}

	public boolean isSysstatCajA() {
		return sysstatCajA;
	}

	public void setSysstatCajA(boolean sysstatCajA) {
		this.sysstatCajA = sysstatCajA;
	}

	public boolean isSysstatCauA() {
		return sysstatCauA;
	}

	public void setSysstatCauA(boolean sysstatCauA) {
		this.sysstatCauA = sysstatCauA;
	}

	public boolean isSysstatCwvA() {
		return sysstatCwvA;
	}

	public void setSysstatCwvA(boolean sysstatCwvA) {
		this.sysstatCwvA = sysstatCwvA;
	}

	public boolean isSysstatCwmA() {
		return sysstatCwmA;
	}

	public void setSysstatCwmA(boolean sysstatCwmA) {
		this.sysstatCwmA = sysstatCwmA;
	}

	public boolean isSysstatEafA() {
		return sysstatEafA;
	}

	public void setSysstatEafA(boolean sysstatEafA) {
		this.sysstatEafA = sysstatEafA;
	}

	public boolean isSysstatEavA() {
		return sysstatEavA;
	}

	public void setSysstatEavA(boolean sysstatEavA) {
		this.sysstatEavA = sysstatEavA;
	}

	public boolean isSysstatEamA() {
		return sysstatEamA;
	}

	public void setSysstatEamA(boolean sysstatEamA) {
		this.sysstatEamA = sysstatEamA;
	}

	public boolean isSysstatEwvA() {
		return sysstatEwvA;
	}

	public void setSysstatEwvA(boolean sysstatEwvA) {
		this.sysstatEwvA = sysstatEwvA;
	}

	public boolean isSysstatEwmA() {
		return sysstatEwmA;
	}

	public void setSysstatEwmA(boolean sysstatEwmA) {
		this.sysstatEwmA = sysstatEwmA;
	}

	public boolean isSysstatNwdA() {
		return sysstatNwdA;
	}

	public void setSysstatNwdA(boolean sysstatNwdA) {
		this.sysstatNwdA = sysstatNwdA;
	}

	public boolean isSysstatVaaA() {
		return sysstatVaaA;
	}

	public void setSysstatVaaA(boolean sysstatVaaA) {
		this.sysstatVaaA = sysstatVaaA;
	}
	//20220912 Bruce add 依照Candy需求修改 Start
	public boolean isSysstatEajA() {
		return sysstatEajA;
	}

	public void setSysstatEajA(boolean sysstatEajA) {
		this.sysstatEajA = sysstatEajA;
	}
	//20220912 Bruce add 依照Candy需求修改 end
	public boolean isSysstatIssue() {
		return sysstatIssue;
	}

	public void setSysstatIssue(boolean sysstatIssue) {
		this.sysstatIssue = sysstatIssue;
	}

	public boolean isSysstatIwdF() {
		return sysstatIwdF;
	}

	public void setSysstatIwdF(boolean sysstatIwdF) {
		this.sysstatIwdF = sysstatIwdF;
	}

	public boolean isSysstatIftF() {
		return sysstatIftF;
	}

	public void setSysstatIftF(boolean sysstatIftF) {
		this.sysstatIftF = sysstatIftF;
	}

	public boolean isSysstatIiqF() {
		return sysstatIiqF;
	}

	public void setSysstatIiqF(boolean sysstatIiqF) {
		this.sysstatIiqF = sysstatIiqF;
	}

	public boolean isSysstatIpyF() {
		return sysstatIpyF;
	}

	public void setSysstatIpyF(boolean sysstatIpyF) {
		this.sysstatIpyF = sysstatIpyF;
	}

	public boolean isSysstatIccdpF() {
		return sysstatIccdpF;
	}

	public void setSysstatIccdpF(boolean sysstatIccdpF) {
		this.sysstatIccdpF = sysstatIccdpF;
	}

	public boolean isSysstatCdpF() {
		return sysstatCdpF;
	}

	public void setSysstatCdpF(boolean sysstatCdpF) {
		this.sysstatCdpF = sysstatCdpF;
	}

	public boolean isSysstatEtxF() {
		return sysstatEtxF;
	}

	public void setSysstatEtxF(boolean sysstatEtxF) {
		this.sysstatEtxF = sysstatEtxF;
	}

	public boolean isSysstat2525F() {
		return sysstat2525F;
	}

	public void setSysstat2525F(boolean sysstat2525f) {
		sysstat2525F = sysstat2525f;
	}

	public boolean isSysstatCpuF() {
		return sysstatCpuF;
	}

	public void setSysstatCpuF(boolean sysstatCpuF) {
		this.sysstatCpuF = sysstatCpuF;
	}

	public boolean isSysstatGpcadF() {
		return sysstatGpcadF;
	}

	public void setSysstatGpcadF(boolean sysstatGpcadF) {
		this.sysstatGpcadF = sysstatGpcadF;
	}

	public boolean isSysstatCauF() {
		return sysstatCauF;
	}

	public void setSysstatCauF(boolean sysstatCauF) {
		this.sysstatCauF = sysstatCauF;
	}

	public boolean isSysstatGpcwdF() {
		return sysstatGpcwdF;
	}

	public void setSysstatGpcwdF(boolean sysstatGpcwdF) {
		this.sysstatGpcwdF = sysstatGpcwdF;
	}

	public boolean isSysstatGpemvF() {
		return sysstatGpemvF;
	}

	public void setSysstatGpemvF(boolean sysstatGpemvF) {
		this.sysstatGpemvF = sysstatGpemvF;
	}

	public boolean isSysstatGpiwdF() {
		return sysstatGpiwdF;
	}

	public void setSysstatGpiwdF(boolean sysstatGpiwdF) {
		this.sysstatGpiwdF = sysstatGpiwdF;
	}

	public boolean isSysstatGpobF() {
		return sysstatGpobF;
	}

	public void setSysstatGpobF(boolean sysstatGpobF) {
		this.sysstatGpobF = sysstatGpobF;
	}

	public boolean isSysstatVaaF() {
		return sysstatVaaF;
	}

	public void setSysstatVaaF(boolean sysstatVaaF) {
		this.sysstatVaaF = sysstatVaaF;
	}

	public boolean isSysstatPure() {
		return sysstatPure;
	}

	public void setSysstatPure(boolean sysstatPure) {
		this.sysstatPure = sysstatPure;
	}

	public boolean isSysstatIiqP() {
		return sysstatIiqP;
	}

	public void setSysstatIiqP(boolean sysstatIiqP) {
		this.sysstatIiqP = sysstatIiqP;
	}

	public boolean isSysstatIftP() {
		return sysstatIftP;
	}

	public void setSysstatIftP(boolean sysstatIftP) {
		this.sysstatIftP = sysstatIftP;
	}

	public boolean isSysstatIccdpP() {
		return sysstatIccdpP;
	}

	public void setSysstatIccdpP(boolean sysstatIccdpP) {
		this.sysstatIccdpP = sysstatIccdpP;
	}

	public boolean isSysstatCdpP() {
		return sysstatCdpP;
	}

	public void setSysstatCdpP(boolean sysstatCdpP) {
		this.sysstatCdpP = sysstatCdpP;
	}

	public boolean isSysstatIpyP() {
		return sysstatIpyP;
	}

	public void setSysstatIpyP(boolean sysstatIpyP) {
		this.sysstatIpyP = sysstatIpyP;
	}

	public boolean isSysstatIqvP() {
		return sysstatIqvP;
	}

	public void setSysstatIqvP(boolean sysstatIqvP) {
		this.sysstatIqvP = sysstatIqvP;
	}

	public boolean isSysstatIqmP() {
		return sysstatIqmP;
	}

	public void setSysstatIqmP(boolean sysstatIqmP) {
		this.sysstatIqmP = sysstatIqmP;
	}

	public boolean isSysstatIqcP() {
		return sysstatIqcP;
	}

	public void setSysstatIqcP(boolean sysstatIqcP) {
		this.sysstatIqcP = sysstatIqcP;
	}

	public boolean isSysstatEquP() {
		return sysstatEquP;
	}

	public void setSysstatEquP(boolean sysstatEquP) {
		this.sysstatEquP = sysstatEquP;
	}

	public boolean isSysstatEqpP() {
		return sysstatEqpP;
	}

	public void setSysstatEqpP(boolean sysstatEqpP) {
		this.sysstatEqpP = sysstatEqpP;
	}

	public boolean isSysstatEqcP() {
		return sysstatEqcP;
	}

	public void setSysstatEqcP(boolean sysstatEqcP) {
		this.sysstatEqcP = sysstatEqcP;
	}

	public boolean isSysstatT24Twn() {
		return sysstatT24Twn;
	}

	public void setSysstatT24Twn(boolean sysstatT24Twn) {
		this.sysstatT24Twn = sysstatT24Twn;
	}

	public boolean isSysstatT24Hkg() {
		return sysstatT24Hkg;
	}

	public void setSysstatT24Hkg(boolean sysstatT24Hkg) {
		this.sysstatT24Hkg = sysstatT24Hkg;
	}

	public boolean isSysstatT24Mac() {
		return sysstatT24Mac;
	}

	public void setSysstatT24Mac(boolean sysstatT24Mac) {
		this.sysstatT24Mac = sysstatT24Mac;
	}

	public boolean isSysstatCbs() {
		return sysstatCbs;
	}

	public void setSysstatCbs(boolean sysstatCbs) {
		this.sysstatCbs = sysstatCbs;
	}

	public boolean isSysstatFedi() {
		return sysstatFedi;
	}

	public void setSysstatFedi(boolean sysstatFedi) {
		this.sysstatFedi = sysstatFedi;
	}

	public boolean isSysstatNb() {
		return sysstatNb;
	}

	public void setSysstatNb(boolean sysstatNb) {
		this.sysstatNb = sysstatNb;
	}

	public boolean isSysstatWebatm() {
		return sysstatWebatm;
	}

	public void setSysstatWebatm(boolean sysstatWebatm) {
		this.sysstatWebatm = sysstatWebatm;
	}

	public boolean isSysstatAscChannel() {
		return sysstatAscChannel;
	}

	public void setSysstatAscChannel(boolean sysstatAscChannel) {
		this.sysstatAscChannel = sysstatAscChannel;
	}

	public boolean isSysstatAsc() {
		return sysstatAsc;
	}

	public void setSysstatAsc(boolean sysstatAsc) {
		this.sysstatAsc = sysstatAsc;
	}

	public boolean isSysstatAscmd() {
		return sysstatAscmd;
	}

	public void setSysstatAscmd(boolean sysstatAscmd) {
		this.sysstatAscmd = sysstatAscmd;
	}

	public boolean isSysstatGcard() {
		return sysstatGcard;
	}

	public void setSysstatGcard(boolean sysstatGcard) {
		this.sysstatGcard = sysstatGcard;
	}

	public boolean isSysstatAscmac() {
		return sysstatAscmac;
	}

	public void setSysstatAscmac(boolean sysstatAscmac) {
		this.sysstatAscmac = sysstatAscmac;
	}

	public boolean isSysstatSvcs() {
		return sysstatSvcs;
	}

	public void setSysstatSvcs(boolean sysstatSvcs) {
		this.sysstatSvcs = sysstatSvcs;
	}

	public boolean isSysstatHkSms() {
		return sysstatHkSms;
	}

	public void setSysstatHkSms(boolean sysstatHkSms) {
		this.sysstatHkSms = sysstatHkSms;
	}

	public boolean isSysstatPv() {
		return sysstatPv;
	}

	public void setSysstatPv(boolean sysstatPv) {
		this.sysstatPv = sysstatPv;
	}

	public boolean isSysstatSmtp() {
		return sysstatSmtp;
	}

	public void setSysstatSmtp(boolean sysstatSmtp) {
		this.sysstatSmtp = sysstatSmtp;
	}

	public boolean isSysstatSps() {
		return sysstatSps;
	}

	public void setSysstatSps(boolean sysstatSps) {
		this.sysstatSps = sysstatSps;
	}

	public boolean isSysstatSpsmac() {
		return sysstatSpsmac;
	}

	public void setSysstatSpsmac(boolean sysstatSpsmac) {
		this.sysstatSpsmac = sysstatSpsmac;
	}

	public String getHbkno() {
		return hbkno;
	}

	public void setHbkno(String hbkno) {
		this.hbkno = hbkno;
	}
	
}
