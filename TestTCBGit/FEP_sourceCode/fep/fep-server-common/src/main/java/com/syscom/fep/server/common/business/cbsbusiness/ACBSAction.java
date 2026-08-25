package com.syscom.fep.server.common.business.cbsbusiness;

import com.syscom.fep.base.aa.*;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.server.common.business.BusinessBase;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ;
import com.syscom.fep.vo.text.ivr.RCV_VO_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ;

public abstract class ACBSAction extends BusinessBase{
	private IVRData ivrData;
	/**
	 * Nb所有資料
	 */
	private NBData nbData;

	/**
	 * Hce所有資料
	 */
	private HCEData hceData;
	
	/**
	 * Atm所有資料
	 */
	private ATMData atmData;

	/**
	 * Eatm所有資料
	 */
	private ATMData eatmData;
	
	/**
	 * 財經 所有資料
	 */
	private FISCData fiscData;
	
	/**
	 * 電文字串 request
	 */
	private String tita;

	/**
	 * ASCII電文字串 request
	 */
	private String ASCIItita;
	
	/**
	 * 電文物件 response
	 */
	private Object tota;
	
	/**
	 * 電文物件 Object
	 */
	private Object oTita;
	
	/**
	 * 初始化，傳入txData
	 * @param txData
	 */
	public ACBSAction (MessageBase txData,Object tota) {
		this.mGeneralData = txData;
		this.feptxn = txData.getFeptxn();
		this.feptxntcb = txData.getFeptxntcb();
		this.feptxnDao = txData.getFeptxnDao();
		this.mINTLTXN = txData.getIntltxn();
		this.logContext = txData.getLogContext();
		this.ej = txData.getEj();
		switch(txData.getTxChannel()) {
			case  POS:
			case  EAT :
			case  ATM : this.atmData = (ATMData) txData; break;
//			case IVR:
			case FISC : this.fiscData = (FISCData) txData; break;
			case  HCA :
			case  HCE : this.hceData = (HCEData) txData; break;
//			case  IVR : this.ivrData = (IVRData) txData; break;
			case NETBANK:
			case NAM:
			case MBQ:
			case MCH:
			case IVR:
			case BIZ:
			case DIG:
			case EIP:
			case EOI:
			case FID:
			case NONVIP:
			case VIP:
			case ONL:
			case SSO:
			case VO:
					this.nbData = (NBData) txData; break;
			default: break;
		}		
		this.tota = tota;
	}

	public Object getoTita() {
		return oTita;
	}

	public void setoTita(Object oTita) {
		this.oTita = oTita;
	}

	public FISCData getFiscData() {
		return fiscData;
	}


	public ATMData getAtmData() {
		return atmData;
	}

	public ATMData getEatmData() {
		return atmData;
	}

	public HCEData getHceData() {
		return hceData;
	}
	public IVRData getIvrData() {
		return ivrData;
	}

	public void setIvrData(IVRData ivrData) {
		this.ivrData = ivrData;
	}
	public NBData getNBData() {
		return nbData;
	}

	public void setHceData(HCEData hceData) {
		this.hceData = hceData;
	}

	public ATMGeneralRequest getAtmRequest() {
		return this.getAtmData().getTxObject().getRequest();
	}

	public RCV_VA_GeneralTrans_RQ getVARequest() {
		return this.getNBData().getTxVafepObject().getRequest();
//		return this.getNBData().getTxObject().getVaRequest();
	}

	public RCV_VO_GeneralTrans_RQ getVORequest() {
		return this.getIvrData().getTxObject().getRequest();
	}

	public RCV_EATM_GeneralTrans_RQ getEATMRequest() {
		return this.getEatmData().getTxObject().getEatmrequest();
	}
	
	public RCV_HCE_GeneralTrans_RQ getHCERequest() {
		return this.getHceData().getTxObject().getRequest();
	}

	public RCV_NB_GeneralTrans_RQ getNBRequest() {
		return this.getNBData().getTxNbfepObject().getRequest();
//		return this.getNBData().getTxObject().getNbRequest();
	}

	public FISC_INBK getInbkRequest() {
		if(this.getFiscData() == null) {
			fiscData = new FISCData();
			fiscData.setTxObject(new FISCGeneral());
			fiscData.getTxObject().setINBKRequest(new FISC_INBK());
		}
		return this.getFiscData().getTxObject().getINBKRequest();
	}

	public FISC_INBK getInbkConfirm() {
		if(this.getFiscData() == null) {
			fiscData = new FISCData();
			fiscData.setTxObject(new FISCGeneral());
			fiscData.getTxObject().setINBKConfirm(new FISC_INBK());
		}
		return this.getFiscData().getTxObject().getINBKConfirm();
	}

	protected String getTitaString() {
		return tita;
	}

	public void setASCIItitaToString(String ASCIItita) {
		this.ASCIItita = ASCIItita;
	}

	protected String getASCIItitaToString() {
		return ASCIItita;
	}

	public void setTitaToString(String tita) {
		this.tita = tita;
	}

	public Object getTota() {
		return tota;
	}

	public void setTota(Object tota) {
		this.tota = tota;
	}

	/**
	 * 組電文
	 * @param txtype
	 * @return
	 */
	public abstract FEPReturnCode getCbsTita(String txtype) throws Exception;

	/**
	 * 拆解電文
	 * @param tota
	 * @return
	 */
	public abstract FEPReturnCode processCbsTota(String cbsTota,String type) throws Exception;

}
