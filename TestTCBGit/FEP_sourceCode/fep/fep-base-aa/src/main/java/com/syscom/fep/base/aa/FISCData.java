package com.syscom.fep.base.aa;

import com.syscom.fep.vo.text.fisc.FISC_INBK;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.mybatis.model.Ictltxn;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.mybatis.model.Merchant;
import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.mybatis.model.Obtltxn;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.enums.FISCTxType;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.t24.T24PreClass;

/**
 * 財金交易共用資料物件
 * 
 * @author Richard
 */
public class FISCData extends MessageBase {
	/**
	 * 財金STAN
	 */
	private String stan = StringUtils.EMPTY;
	/**
	 * 代理種類
	 */
	private FISCTxType acquire;
	/**
	 * 財金電文通用物件
	 */
	private FISCGeneral txObject;
	/**
	 * 財金電文種類
	 */
	private FISCSubSystem fiscTeleType;
	private Merchant merchant;
	private Npsunit npsunit;
	private T24PreClass t24Response;
	private Zone atmZone;
	
	//Bruce add ICTLTXN
	private Ictltxn ictlTxn;
	private Intltxn intlTxn;

	private Obtltxn obtlTxn;

	private Vatxn vatxn;

	private String RClientID;

	public FISC_INBK getFiscreq() {
		return fiscreq;
	}

	public void setFiscreq(FISC_INBK fiscreq) {
		this.fiscreq = fiscreq;
	}

	private FISC_INBK fiscreq;
	public Obtltxn getObtlTxn() {
		return obtlTxn;
	}

	public void setObtlTxn(Obtltxn obtlTxn) {
		this.obtlTxn = obtlTxn;
	}

	public void setVatxn(Vatxn vatxn) {
		this.vatxn = vatxn;
	}

	public void setIctlTxn(Ictltxn ictlTxn) {
		this.ictlTxn = ictlTxn;
	}	

	public Ictltxn getIctlTxn() {
		return ictlTxn;
	}
	
	public Vatxn getVatxn() {
		return vatxn;
	}

	public String getStan() {
		return stan;
	}

	public void setStan(String stan) {
		this.stan = stan;
	}

	public FISCTxType getAcquire() {
		return acquire;
	}

	public void setAcquire(FISCTxType acquire) {
		this.acquire = acquire;
	}

	public FISCGeneral getTxObject() {
		return txObject;
	}

	public void setTxObject(FISCGeneral txObject) {
		this.txObject = txObject;
	}

	public FISCSubSystem getFiscTeleType() {
		return fiscTeleType;
	}

	public void setFiscTeleType(FISCSubSystem fiscTeleType) {
		this.fiscTeleType = fiscTeleType;
	}

	public Merchant getMerchant() {
		return merchant;
	}

	public void setMerchant(Merchant merchant) {
		this.merchant = merchant;
	}

	public Npsunit getNpsunit() {
		return npsunit;
	}

	public void setNpsunit(Npsunit npsunit) {
		this.npsunit = npsunit;
	}

	public T24PreClass getT24Response() {
		return t24Response;
	}

	public void setT24Response(T24PreClass t24Response) {
		this.t24Response = t24Response;
	}

	public Intltxn getIntlTxn() {
		return intlTxn;
	}

	public void setIntlTxn(Intltxn intlTxn) {
		this.intlTxn = intlTxn;
	}

	public Zone getAtmZone() {
		return atmZone;
	}

	public void setAtmZone(Zone atmZone) {
		this.atmZone = atmZone;
	}

	public String getRClientID() {
		return RClientID;
	}

	public void setRClientID(String RClientID) {
		this.RClientID = RClientID;
	}
}
