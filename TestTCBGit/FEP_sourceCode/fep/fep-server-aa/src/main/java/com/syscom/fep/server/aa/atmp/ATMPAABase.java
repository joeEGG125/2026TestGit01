package com.syscom.fep.server.aa.atmp;

import com.syscom.fep.base.aa.*;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.model.Feptxntcb;
import com.syscom.fep.mybatis.model.Paytype;
import com.syscom.fep.server.common.business.atm.ATM;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.ATMGeneralResponse;
import com.syscom.fep.vo.text.fcs.FCSGeneral;
import com.syscom.fep.vo.text.fcs.FCSGeneralRequest;
import com.syscom.fep.vo.text.fcs.FCSGeneralResponse;
import com.syscom.fep.vo.text.hce.HCEGeneral;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS;
import com.syscom.fep.vo.text.nb.NBFEPGeneral;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.VAFEPGeneral;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;

/**
 * ATMP子系統AA的基礎類別
 *
 * @author Richard
 */
public abstract class ATMPAABase extends AABase {
	private ATMGeneral mATMGeneral; // ATM電文通用物件
	private ATMData mTxData; // ATMP AA資料物件
	private FCSData mFCStxData; // FCS資料物件
	private HCEData mHCEtxData; // HCE資料物件
	private VAFEPGeneral mVAFEPGeneral;

	public VAFEPGeneral getmVAFEPGeneral() {
		return mVAFEPGeneral;
	}

	public void setmVAFEPGeneral(VAFEPGeneral mVAFEPGeneral) {
		this.mVAFEPGeneral = mVAFEPGeneral;
	}

	public RCV_VA_GeneralTrans_RQ getmVAReq() {
		return mVAReq;
	}

	public void setmVAReq(RCV_VA_GeneralTrans_RQ mVAReq) {
		this.mVAReq = mVAReq;
	}

	private RCV_VA_GeneralTrans_RQ mVAReq;

	private NBData mNBtxData;
	private NBFEPData mNBFEPtxData;

	private FCSGeneral mFCSGeneral; // FCS電文通用物件
	private HCEGeneral mHCEGeneral; // HCE電文通用物件
	private NBFEPGeneral mNBFEPGeneral;

	private FCSGeneralRequest mFCSReq; // FCS Request電文處理物件
	private FCSGeneralResponse mFCSRes; // FCS Response電文處理物件
	private ATMGeneralRequest mATMReq; // ATM Request電文
	private ATMGeneralResponse mATMRes; // ATM Response電文
    private RCV_HCE_GeneralTrans_RQ mHCEReq;
    private RCV_EATM_GeneralTrans_RQ mEATMReq;
    private SEND_HCE_GeneralTrans_RS mHCERes;
    private RCV_NB_GeneralTrans_RQ mNBReq;
	private ATM mATMBusiness; // ATM邏輯處理物件
	public static final String PAYTYPE30000Trans = "59999";
	private Paytype paytype; // add by henny 20100902
	// 國際簽帳金融卡購物RRN-三萬元移轉
	public static final String RRN30000Trans = "188888889999";
	private BigDecimal charge;
	private String brch;
	private String chargeFlag;

	public ATMPAABase(ATMData txnData) throws Exception {
		super();
		this.aaName = txnData.getAaName();
		this.mTxData = txnData;
		this.logContext = this.mTxData.getLogContext();
		this.ej = this.mTxData.getEj();
		// 將ATMData中的Request及Response物件取出存放在內部變數中
		this.mATMGeneral = this.mTxData.getTxObject();
		this.mATMReq = this.mATMGeneral.getRequest();
		this.mATMRes = this.mATMGeneral.getResponse();
		//--ben-20220922-//this.logContext.setTxDate(this.mATMReq.getAtmseq_1());
		//--ben-20220922-//this.logContext.setTrinBank(this.mATMReq.getBknoD());
		//--ben-20220922-//this.logContext.setTrinActno(this.mATMReq.getActD());
		//--ben-20220922-//this.logContext.setTroutBank(this.mATMReq.getBKNO());
		// 2011-03-15 by kyo for 若卡片帳號沒值就用交易帳號來顯示在LOG中
		//--ben-20220922-//if (StringUtils.isBlank(this.mATMReq.getTXACT())) {
		//--ben-20220922-//	this.logContext.setTroutActno(this.mATMReq.getCHACT());
		//--ben-20220922-//} else {
		//--ben-20220922-//	this.logContext.setTroutActno(this.mATMReq.getTXACT());
		//--ben-20220922-//}
		//--ben-20220922-//this.logContext.setChact(this.mATMReq.getCHACT());
		this.logContext.setMessageFlowType(MessageFlow.Request);
		String mTxData_TxRequestMessage = this.mTxData.getTxRequestMessage();
		this.logContext.setMessage(mTxData_TxRequestMessage); // EBCDIC
		this.logContext.setProgramFlowType(ProgramFlow.AAIn);
		this.logContext.setProgramName(this.mTxData.getAaName());
		// 寫文字檔LOG
		logMessage(Level.DEBUG, this.logContext);
		try {
			this.logContext.setMessage(EbcdicConverter.fromHex(CCSID.English, mTxData_TxRequestMessage)); // ASCII
		} catch (Exception e) {
			this.logContext.setMessage("EBCDIC轉ASCII失敗");
			this.logContext.setProgramException(e);
		}
		// 寫文字檔LOG
		logMessage(Level.DEBUG, this.logContext);
		// 準備FEPTxn相關物件
		this.feptxn = new FeptxnExt();
		this.feptxntcb = new Feptxntcb();
		this.feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "(ATMData)"));
		this.feptxn.setFeptxnEjfno(this.ej);
		this.mTxData.setFeptxn(this.feptxn);
		this.mTxData.setFeptxntcb(this.feptxntcb);
		this.mTxData.setFeptxnDao(this.feptxnDao);
		this.mTxData.setTbsdyFISC(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
		// 建立ATM Business物件,傳入ATMData物件,將EJ,FepTxn及DBFepTxn也帶給Business
		if(mTxData.getTxChannel() == FEPChannel.ATM) {
			this.mATMBusiness = new ATM(this.mTxData);	
			this.mATMBusiness.updateATMStatus();				
		} else {
			this.mATMBusiness = new ATM(this.mTxData, mTxData.getTxChannel().toString());
		}
		
			
	}
	public ATMPAABase(ATMData txnData,String eatm) throws Exception {
		super();
		this.aaName = txnData.getAaName();
		this.mTxData = txnData;
		this.logContext = this.mTxData.getLogContext();
		this.ej = this.mTxData.getEj();
		// 將ATMData中的Request及Response物件取出存放在內部變數中
		this.mATMGeneral = this.mTxData.getTxObject();
		this.mATMReq = this.mATMGeneral.getRequest();
		this.mATMRes = this.mATMGeneral.getResponse();

		this.logContext.setMessageFlowType(MessageFlow.Request);
		String mTxData_TxRequestMessage = this.mTxData.getTxRequestMessage();
		this.logContext.setMessage(mTxData_TxRequestMessage); // EBCDIC
		this.logContext.setProgramFlowType(ProgramFlow.AAIn);
		this.logContext.setProgramName(this.mTxData.getAaName());
		// 寫文字檔LOG
		logMessage(Level.DEBUG, this.logContext);
		try {
			this.logContext.setMessage(EbcdicConverter.fromHex(CCSID.English, mTxData_TxRequestMessage)); // ASCII
		} catch (Exception e) {
			this.logContext.setMessage("EBCDIC轉ASCII失敗");
			this.logContext.setProgramException(e);
		}
		// 寫文字檔LOG
		logMessage(Level.DEBUG, this.logContext);
		// 準備FEPTxn相關物件
		this.feptxn = new FeptxnExt();
		this.feptxntcb = new Feptxntcb();
		this.feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "(ATMData)"));
		this.feptxn.setFeptxnEjfno(this.ej);
		this.mTxData.setFeptxn(this.feptxn);
		this.mTxData.setFeptxntcb(this.feptxntcb);
		this.mTxData.setFeptxnDao(this.feptxnDao);
		this.mTxData.setTbsdyFISC(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
		// 建立ATM Business物件,傳入ATMData物件,將EJ,FepTxn及DBFepTxn也帶給Business
		this.mATMBusiness = new ATM(this.mTxData,eatm);
		// ATMP的AA在New時就先UpdateATMStatus
		this.mATMBusiness.updateATMStatus();
	}

	public ATMPAABase(HCEData txnData) throws Exception {
		super();
		this.aaName = txnData.getAaName();
		this.mHCEtxData = txnData;
		this.logContext = this.mHCEtxData.getLogContext();
		this.ej = this.mHCEtxData.getEj();
		this.mHCEGeneral = this.mHCEtxData.getTxObject();
		this.mHCEReq = this.mHCEtxData.getTxObject().getRequest();
		this.mHCERes = this.mHCEtxData.getTxObject().getResponse();
		// 準備FEPTxn相關物件
		this.feptxn = new FeptxnExt();
		this.feptxntcb = new Feptxntcb();
		this.feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "(HCEData)"));
		this.feptxn.setFeptxnEjfno(this.ej);
		this.mHCEtxData.setFeptxnDao(this.feptxnDao);
		// 寫文字檔LOG
		this.logContext.setProgramFlowType(ProgramFlow.AAIn);
		this.logContext.setProgramName(this.mHCEtxData.getAaName());
		txnData.setFeptxn(feptxn);
		txnData.setFeptxntcb(feptxntcb);
		txnData.setFeptxnDao(feptxnDao);
		logMessage(Level.DEBUG, this.logContext);
		
		this.mTxData = new ATMData();
		this.mATMGeneral = new ATMGeneral();
		this.mTxData.setTxObject(mATMGeneral);
		this.mATMReq = this.mTxData.getTxObject().getRequest();
		this.mATMRes = this.mTxData.getTxObject().getResponse();
		this.mTxData.setTbsdyFISC(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
		this.mTxData.setFeptxn(this.feptxn);
		this.mTxData.setFeptxntcb(this.feptxntcb);
		this.mTxData.setFeptxnDao(this.feptxnDao);
		this.mTxData.setEj(this.getEj());
		this.mATMBusiness = new ATM(this.mTxData, "");
		this.mATMBusiness.setmHCEtxData(txnData);
	}

	public ATMPAABase(NBData txnData) throws Exception {
		super();
		this.aaName = txnData.getAaName();
		this.mNBtxData = txnData;
		this.logContext = this.mNBtxData.getLogContext();
		this.ej = this.mNBtxData.getEj();


		//Aster調整需求 多了一個va
		if (this.mNBtxData.getTxNbfepObject() != null) {
			this.mNBFEPGeneral = this.mNBtxData.getTxNbfepObject();
			this.mNBReq = this.mNBtxData.getTxNbfepObject().getRequest();
		} else {
			this.mVAFEPGeneral = this.mNBtxData.getTxVafepObject();
			this.mVAReq = this.mNBtxData.getTxVafepObject().getRequest();
		}
		// 準備FEPTxn相關物件
		this.feptxn = new FeptxnExt();
		this.feptxntcb = new Feptxntcb();
		this.feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "(NBFEPData)"));
		this.feptxn.setFeptxnEjfno(this.ej);
		this.mNBtxData.setFeptxnDao(this.feptxnDao);
		txnData.setFeptxn(feptxn);
		txnData.setFeptxntcb(feptxntcb);

		// 寫文字檔LOG
		this.logContext.setProgramFlowType(ProgramFlow.AAIn);
		this.logContext.setProgramName(this.mNBtxData.getAaName());
		logMessage(Level.DEBUG, this.logContext);
		
		this.mTxData = new ATMData();
		this.mATMGeneral = new ATMGeneral();
		this.mTxData.setTxObject(mATMGeneral);
		this.mATMReq = this.mTxData.getTxObject().getRequest();
		this.mATMRes = this.mTxData.getTxObject().getResponse();
		this.mTxData.setTbsdyFISC(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
		this.mTxData.setFeptxn(this.feptxn);
		this.mTxData.setFeptxntcb(this.feptxntcb);
		this.mTxData.setFeptxnDao(this.feptxnDao);
		this.mTxData.setEj(this.getEj());
		this.mATMBusiness = new ATM(this.mTxData, "");
		this.mATMBusiness.setmNBtxData(txnData);
	}

	public ATMPAABase(FCSData txnData) throws Exception {
		super();
		this.aaName = txnData.getAaName();
		this.mFCStxData = txnData;
		this.logContext = this.mFCStxData.getLogContext();
		this.ej = this.mFCStxData.getEj();
		this.mFCSGeneral = this.mFCStxData.getTxObject();
		this.mFCSReq = this.mFCStxData.getTxObject().getRequest();
		this.mFCSRes = this.mFCStxData.getTxObject().getResponse();
		// 準備FEPTxn相關物件
		this.feptxn = new FeptxnExt();
		this.feptxntcb = new Feptxntcb();
		this.feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "(FCSData)"));
		// 寫文字檔LOG
		this.logContext.setProgramFlowType(ProgramFlow.AAIn);
		this.logContext.setProgramName(this.mFCStxData.getAaName());
		logMessage(Level.DEBUG, this.logContext);
		
		this.mTxData = new ATMData();
		this.mATMGeneral = new ATMGeneral();
		this.mTxData.setTxObject(mATMGeneral);
		this.mATMReq = this.mTxData.getTxObject().getRequest();
		this.mATMRes = this.mTxData.getTxObject().getResponse();
		this.mTxData.setTbsdyFISC(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
		this.mTxData.setFeptxn(this.feptxn);
		this.mTxData.setFeptxntcb(this.feptxntcb);
		this.mTxData.setFeptxnDao(this.feptxnDao);
		this.mTxData.setEj(this.getEj());
		this.mATMBusiness = new ATM(this.mTxData, "");
	}

	protected ATM getATMBusiness() {
		return mATMBusiness;
	}

	public ATMGeneralRequest getATMRequest() {
		return mATMReq;
	}

	public ATMGeneralResponse getATMResponse() {
		return mATMRes;
	}

	public ATMData getTxData() {
		return mTxData;
	}

	public FCSData getFCStxData() {
		return mFCStxData;
	}

	public ATMGeneral getATMGeneral() {
		return mATMGeneral;
	}

	public FCSGeneral getFCSGeneral() {
		return mFCSGeneral;
	}

	public HCEGeneral getmHCEGeneral() {
		return mHCEGeneral;
	}

	public void setmHCEGeneral(HCEGeneral mHCEGeneral) {
		this.mHCEGeneral = mHCEGeneral;
	}

	public RCV_HCE_GeneralTrans_RQ getmHCEReq() {
		return mHCEReq;
	}

	public void setmHCEReq(RCV_HCE_GeneralTrans_RQ mHCEReq) {
		this.mHCEReq = mHCEReq;
	}

	public SEND_HCE_GeneralTrans_RS getmHCERes() {
		return mHCERes;
	}

	public void setmHCERes(SEND_HCE_GeneralTrans_RS mHCERes) {
		this.mHCERes = mHCERes;
	}

	public RCV_NB_GeneralTrans_RQ getmNBReq() {
		return mNBReq;
	}

	public void setmNBReq(RCV_NB_GeneralTrans_RQ mNBReq) {
		this.mNBReq = mNBReq;
	}

	public HCEData getmHCEtxData() {
		return mHCEtxData;
	}

	public NBData getmNBtxData() {
		return mNBtxData;
	}

	public void setmHCEtxData(HCEData mHCEtxData) {
		this.mHCEtxData = mHCEtxData;
	}

	public Paytype getPaytype() {
		return paytype;
	}

	public void setPaytype(Paytype paytype) {
		this.paytype = paytype;
	}

	public BigDecimal getCharge() {
		return charge;
	}

	public void setCharge(BigDecimal charge) {
		this.charge = charge;
	}

	public String getBrch() {
		return brch;
	}

	public void setBrch(String brch) {
		this.brch = brch;
	}

	public String getChargeFlag() {
		return chargeFlag;
	}

	public void setChargeFlag(String chargeFlag) {
		this.chargeFlag = chargeFlag;
	}

	public RCV_EATM_GeneralTrans_RQ getmEATMReq() {
		return mEATMReq;
	}

	public void setmEATMReq(RCV_EATM_GeneralTrans_RQ mEATMReq) {
		this.mEATMReq = mEATMReq;
	}
}
