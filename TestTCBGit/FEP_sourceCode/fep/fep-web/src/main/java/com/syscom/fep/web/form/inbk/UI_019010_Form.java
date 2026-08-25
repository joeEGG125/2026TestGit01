package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.mybatis.model.Sysstat;

public class UI_019010_Form extends BaseForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Sysstat sysstat;
	//自行
	private boolean intra; 
    private boolean iwdI; 
    private boolean iftI; 
    private boolean admI; 
    private boolean fwdI; 
    private boolean ipyI;
    private boolean iccdpI; 
    private boolean etxI; 
    private boolean caI; 
    private boolean caaI; 
    private boolean aig; 
    private boolean hkIssue; 
    private boolean moIssue; 
    private boolean hkFiscmb; 
    private boolean moFiscmb;
    private boolean hkPlus; 
    private boolean moPlus;
    //代理行
    private boolean agent; 
    private boolean iwdA; 
    private boolean iftA; 
    private boolean ipyA; 
    private boolean iccdpA; 
    private boolean etxA;
    private boolean t2525A; 
    private boolean cpuA; 
    private boolean cafA; 
    private boolean cavA; 
    private boolean camA; 
    private boolean cajA; 
    private boolean cauA; 
    private boolean cwvA; 
    private boolean cwmA;
    
    //原存
    private boolean issue;
    private boolean iwdF;
    private boolean iftF;
    private boolean ipyF;
    private boolean iccdpF;
    private boolean cdpF;
    private boolean etxF;
    private boolean t2525F;
    private boolean cpuF;
    private boolean gpcadF;
    private boolean cauF;
    private boolean gpcwdF;
    
    //其他通道
    private boolean cbs;
    private boolean fedi;
    private boolean nb;
    private boolean webatm;
    private boolean ascChannel;
    private boolean asc;
    private boolean ascmd;
    private boolean gcard;
    private boolean sps;
    private boolean ascmac;
    private boolean spsmac;
    
	public Sysstat getSysstat() {
		return sysstat;
	}

	public void setSysstat(Sysstat sysstat) {
		this.sysstat = sysstat;		
	}

	public boolean isIntra() {
		return intra;
	}

	public void setIntra(boolean intra) {
		this.intra = intra;
	}

	public boolean isIftI() {
		return iftI;
	}

	public void setIftI(boolean iftI) {
		this.iftI = iftI;
	}

	public boolean isIwdI() {
		return iwdI;
	}

	public void setIwdI(boolean iwdI) {
		this.iwdI = iwdI;
	}

	public boolean isAdmI() {
		return admI;
	}

	public void setAdmI(boolean admI) {
		this.admI = admI;
	}

	public boolean isFwdI() {
		return fwdI;
	}

	public void setFwdI(boolean fwdI) {
		this.fwdI = fwdI;
	}

	public boolean isIpyI() {
		return ipyI;
	}

	public void setIpyI(boolean ipyI) {
		this.ipyI = ipyI;
	}

	public boolean isIccdpI() {
		return iccdpI;
	}

	public void setIccdpI(boolean iccdpI) {
		this.iccdpI = iccdpI;
	}

	public boolean isEtxI() {
		return etxI;
	}

	public void setEtxI(boolean etxI) {
		this.etxI = etxI;
	}

	public boolean isCaI() {
		return caI;
	}

	public void setCaI(boolean caI) {
		this.caI = caI;
	}

	public boolean isCaaI() {
		return caaI;
	}

	public void setCaaI(boolean caaI) {
		this.caaI = caaI;
	}

	public boolean isHkIssue() {
		return hkIssue;
	}

	public void setHkIssue(boolean hkIssue) {
		this.hkIssue = hkIssue;
	}

	public boolean isMoIssue() {
		return moIssue;
	}

	public void setMoIssue(boolean moIssue) {
		this.moIssue = moIssue;
	}

	public boolean isMoFiscmb() {
		return moFiscmb;
	}

	public void setMoFiscmb(boolean moFiscmb) {
		this.moFiscmb = moFiscmb;
	}

	public boolean isHkFiscmb() {
		return hkFiscmb;
	}

	public void setHkFiscmb(boolean hkFiscmb) {
		this.hkFiscmb = hkFiscmb;
	}

	public boolean isHkPlus() {
		return hkPlus;
	}

	public void setHkPlus(boolean hkPlus) {
		this.hkPlus = hkPlus;
	}

	public boolean isMoPlus() {
		return moPlus;
	}

	public void setMoPlus(boolean moPlus) {
		this.moPlus = moPlus;
	}

	public boolean isAig() {
		return aig;
	}

	public void setAig(boolean aig) {
		this.aig = aig;
	}

	public boolean isIwdA() {
		return iwdA;
	}

	public void setIwdA(boolean iwdA) {
		this.iwdA = iwdA;
	}

	public boolean isAgent() {
		return agent;
	}

	public void setAgent(boolean agent) {
		this.agent = agent;
	}

	public boolean isIftA() {
		return iftA;
	}

	public void setIftA(boolean iftA) {
		this.iftA = iftA;
	}

	public boolean isIpyA() {
		return ipyA;
	}

	public void setIpyA(boolean ipyA) {
		this.ipyA = ipyA;
	}

	public boolean isIccdpA() {
		return iccdpA;
	}

	public void setIccdpA(boolean iccdpA) {
		this.iccdpA = iccdpA;
	}

	public boolean isEtxA() {
		return etxA;
	}

	public void setEtxA(boolean etxA) {
		this.etxA = etxA;
	}

	public boolean isT2525A() {
		return t2525A;
	}

	public void setT2525A(boolean t2525a) {
		t2525A = t2525a;
	}

	public boolean isCpuA() {
		return cpuA;
	}

	public void setCpuA(boolean cpuA) {
		this.cpuA = cpuA;
	}

	public boolean isCafA() {
		return cafA;
	}

	public void setCafA(boolean cafA) {
		this.cafA = cafA;
	}

	public boolean isCavA() {
		return cavA;
	}

	public void setCavA(boolean cavA) {
		this.cavA = cavA;
	}

	public boolean isCamA() {
		return camA;
	}

	public void setCamA(boolean camA) {
		this.camA = camA;
	}

	public boolean isCajA() {
		return cajA;
	}

	public void setCajA(boolean cajA) {
		this.cajA = cajA;
	}

	public boolean isCauA() {
		return cauA;
	}

	public void setCauA(boolean cauA) {
		this.cauA = cauA;
	}

	public boolean isCwvA() {
		return cwvA;
	}

	public void setCwvA(boolean cwvA) {
		this.cwvA = cwvA;
	}

	public boolean isCwmA() {
		return cwmA;
	}

	public void setCwmA(boolean cwmA) {
		this.cwmA = cwmA;
	}

	public boolean isWebatm() {
		return webatm;
	}

	public void setWebatm(boolean webatm) {
		this.webatm = webatm;
	}

	public boolean isFedi() {
		return fedi;
	}

	public void setFedi(boolean fedi) {
		this.fedi = fedi;
	}

	public boolean isCbs() {
		return cbs;
	}

	public void setCbs(boolean cbs) {
		this.cbs = cbs;
	}

	public boolean isNb() {
		return nb;
	}

	public void setNb(boolean nb) {
		this.nb = nb;
	}

	public boolean isAscChannel() {
		return ascChannel;
	}

	public void setAscChannel(boolean ascChannel) {
		this.ascChannel = ascChannel;
	}

	public boolean isAsc() {
		return asc;
	}

	public void setAsc(boolean asc) {
		this.asc = asc;
	}

	public boolean isGcard() {
		return gcard;
	}

	public void setGcard(boolean gcard) {
		this.gcard = gcard;
	}

	public boolean isAscmd() {
		return ascmd;
	}

	public void setAscmd(boolean ascmd) {
		this.ascmd = ascmd;
	}

	public boolean isSps() {
		return sps;
	}

	public void setSps(boolean sps) {
		this.sps = sps;
	}

	public boolean isAscmac() {
		return ascmac;
	}

	public void setAscmac(boolean ascmac) {
		this.ascmac = ascmac;
	}

	public boolean isSpsmac() {
		return spsmac;
	}

	public void setSpsmac(boolean spsmac) {
		this.spsmac = spsmac;
	}

	public boolean isIssue() {
		return issue;
	}

	public void setIssue(boolean issue) {
		this.issue = issue;
	}

	public boolean isIwdF() {
		return iwdF;
	}

	public void setIwdF(boolean iwdF) {
		this.iwdF = iwdF;
	}

	public boolean isIpyF() {
		return ipyF;
	}

	public void setIpyF(boolean ipyF) {
		this.ipyF = ipyF;
	}

	public boolean isCdpF() {
		return cdpF;
	}

	public void setCdpF(boolean cdpF) {
		this.cdpF = cdpF;
	}

	public boolean isIftF() {
		return iftF;
	}

	public void setIftF(boolean iftF) {
		this.iftF = iftF;
	}

	public boolean isIccdpF() {
		return iccdpF;
	}

	public void setIccdpF(boolean iccdpF) {
		this.iccdpF = iccdpF;
	}

	public boolean isEtxF() {
		return etxF;
	}

	public void setEtxF(boolean etxF) {
		this.etxF = etxF;
	}

	public boolean isT2525F() {
		return t2525F;
	}

	public void setT2525F(boolean t2525f) {
		t2525F = t2525f;
	}

	public boolean isCpuF() {
		return cpuF;
	}

	public void setCpuF(boolean cpuF) {
		this.cpuF = cpuF;
	}

	public boolean isGpcadF() {
		return gpcadF;
	}

	public void setGpcadF(boolean gpcadF) {
		this.gpcadF = gpcadF;
	}

	public boolean isCauF() {
		return cauF;
	}

	public void setCauF(boolean cauF) {
		this.cauF = cauF;
	}

	public boolean isGpcwdF() {
		return gpcwdF;
	}

	public void setGpcwdF(boolean gpcwdF) {
		this.gpcwdF = gpcwdF;
	}

}
