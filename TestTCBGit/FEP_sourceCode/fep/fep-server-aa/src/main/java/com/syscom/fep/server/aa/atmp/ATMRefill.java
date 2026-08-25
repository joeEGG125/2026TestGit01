package com.syscom.fep.server.aa.atmp;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.AtmboxExtMapper;
import com.syscom.fep.mybatis.ext.mapper.AtmcExtMapper;
import com.syscom.fep.mybatis.ext.mapper.AtmcashExtMapper;
import com.syscom.fep.mybatis.ext.mapper.AtmcoinExtMapper;
import com.syscom.fep.mybatis.ext.mapper.InbkparmExtMapper;
import com.syscom.fep.mybatis.model.Atmc;
import com.syscom.fep.mybatis.model.Atmcash;
import com.syscom.fep.mybatis.model.Inbkparm;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.ATMReturnCode;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.enums.CurrencyType;
import com.syscom.fep.vo.text.atm.response.TTSResponse;

/**
 * <p>
 * (自行ATM)裝鈔及結帳
 * </p>
 * <ul>
 * <li>提款機結帳-<b>TTF</b></li>
 * <li>原AA程式{@link ATMRefill}</b></li>
 * </ul>
 *
 * <p>
 * 負責處理 ATM 送來的信用卡交易電文 RWT : 存提款機裝鈔 RWF : 提款機裝鈔 RWS : 多幣通提款機裝鈔 TTI :
 * 存提款機結帳 TTF : 提款機結帳 TTS : 多幣通提款機結帳 CSH : 存提款機錢箱裝鈔 BAK : 存提款機錢箱回鈔 BOX :
 * 存提款機錢箱結帳 比對TITA和ATMCASH的資料，若為裝鈔，則更新ATMCASH (存提款機需更新ATMBOX)
 * 結帳類(TTX):比對ATM_TITA和ATMSTAT以及ATMCASH欄位是否相符，若不符合，回傳
 * 裝鈔類(RWX):先更新ATMSTAT的裝鈔相關欄位，比對ATM_TITA和ATMCASH所記錄之面額和幣別是否相符，
 * 記錄下比對結果(比對錯誤不直接回傳)，更新ATMCASH資料
 * 存提款機錢箱動作類(BAK,BOX,CSH):先記錄ATMBOXLOG檔，細節則為比對ATM_TITA和ATMBOX欄位以及更新ATMBOX
 * </p>
 *
 * @author Han
 */
public class ATMRefill extends ATMPAABase {
	private FEPReturnCode rtnCode = CommonReturnCode.Normal;
	private String rtnMessage = StringUtils.EMPTY;
	private boolean needUpdateFEPTXN = false;

	/**
	 * 
	 * <summary> AA的建構式,在這邊初始化及設定好相關變數 </summary>
	 * <param name="txnData">AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件</param>
	 * <remarks></remarks>
	 * 
	 * @throws Exception
	 */
	public ATMRefill(ATMData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * AA進入點主程式
	 * 
	 * <summary> 程式進入點 </summary> <returns>Response電文</returns> <remarks></remarks>
	 * <modify> <modifier>Kyo</modifier> <reason>SPEC 調整流程，帳務主機皆等回應但移至最後面才送</reason>
	 * <date>2010/04/23</date> </modify>
	 */
	@Override
	public String processRequestData() {
		try {
			// 1.準備FEP交易記錄檔
			this.rtnCode = this.getATMBusiness().prepareFEPTXN();
			// 2.新增FEP交易記錄檔
			if (this.rtnCode == CommonReturnCode.Normal) {
				this.rtnCode = this.getATMBusiness().addTXData();
			}
			// 3.商業邏輯檢核
			if (this.rtnCode == CommonReturnCode.Normal) {
				this.needUpdateFEPTXN = true;
				this.rtnCode = this.checkBusinessRule();
			}

			// 2012/09/28 Modify by Ruling for 新增硬幣機的業務
			// 4.更新 ATM 繳庫/裝鈔/庫存現金資料
			if (this.rtnCode == CommonReturnCode.Normal) {
				if (ATMTXCD.COH.toString().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())) {
					UpdateATMCoinForRefill();
				}
			}

			// 2011/10/05 Modify by Ruling for SPEC修改裝鈔邏輯:RWS/RWT/RWF先組回應電文
			// 4.組 ATM 裝鈔(RWS/RWT/RWF)回應電文
			if (ATMTXCD.RWS.name().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.RWT.name().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.RWF.name().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())) {

				rtnMessage = PrepareATMResponseData();

				// 更新 ATM 繳庫/裝鈔/庫存現金資料
				UpdateATMCoinForRefill();
			}
			// 5.更新FEP交易記錄檔
			updateFEPTxn();
			this.updateFEPTxn();
		} catch (Exception e) {
			// 2010-04-23 modified by kyo for 防止程式中發生例外但沒CATCH到時，會回ATM正常的這種異常情形
			this.rtnCode = CommonReturnCode.ProgramException;

			// 2011-07-05 by kyo for exception不需要call GetRCFromErrorCode，避免送兩次EMS
			// ATMBusiness.FepTxn.FEPTXN_REPLY_CODE =
			// TxHelper.GetRCFromErrorCode(CInt(_rtnCode).ToString, FEPChannel.FEP,
			// TxData.TxChannel, TxData.LogContext)
			this.getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
		} finally {

			// 2011/10/05 modify by Ruling for SPEC修改裝鈔邏輯:移至第4點在組ATM裝鈔(RWS/RWT/RWF)回應電文
			// 5.組回應電文
			if (!ATMTXCD.RWS.toString().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())
					&& !ATMTXCD.RWT.toString().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())
					&& !ATMTXCD.RWF.toString().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())) {
				rtnMessage = PrepareATMResponseData();
			}
			// 2011/10/05 modify by Ruling for SPEC修改裝鈔邏輯:移至第4點在組ATM裝鈔(RWS/RWT/RWF)回應電文後UpdateATMCashForRefill
            // 2010-04-28 modified by kyo for SPEC修改裝鈔邏輯:先組回應電文後才更新鈔箱檔與狀態檔
            // 6.裝鈔類交易更新資料庫檔案
            // UpdateATMCashForRefill()

            // 7.帳務主機處理
			//  _rtnCode = SendToHost()

			this.getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			this.getTxData().getLogContext().setMessage(this.rtnMessage);
			this.getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			this.getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			this.logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(this.rtnCode));
			logMessage(Level.DEBUG, this.logContext);
		}
		return this.rtnMessage;
	}

	/**
	 * 3. 商業邏輯檢核 相關程式
	 * 
	 * @return
	 */
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			// 3.1 CheckHeader
			rtnCode = this.getATMBusiness().checkHeader();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			// 3.2 CheckBody
			rtnCode = this.getATMBusiness().checkBody();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			if (ATMTXCD.ODR.name().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())) {
				if (SysStatus.getPropertyValue().getSysstatHbkno()
						.equals(this.getATMBusiness().getFeptxn().getFeptxnTrinBkno())) {
					rtnCode = ATMReturnCode.OtherCheckError;
					this.logContext.setRemark(StringUtils.join("轉入行必須為他行, FEPTXN_TRIN_BKNO=",
							this.getATMBusiness().getFeptxn().getFeptxnTrinBkno()));
					logMessage(Level.INFO, this.logContext);
					return rtnCode;
				}
				if (StringUtils.isBlank(this.getATMBusiness().getFeptxn().getFeptxnTrinActno())) {
					rtnCode = ATMReturnCode.TranInACTNOError;
					this.logContext.setRemark("轉入帳號不能為NULL或空白");
					logMessage(Level.INFO, this.logContext);
					return rtnCode;
				}
				// 取得跨行轉帳手續費
				Inbkparm inbkparm = new Inbkparm();
				inbkparm.setInbkparmApid("2521");
				inbkparm.setInbkparmPcode(StringUtils.EMPTY);
				inbkparm.setInbkparmAcqFlag("A");
				inbkparm.setInbkparmEffectDate(this.getFeptxn().getFeptxnTxDate());
				inbkparm.setInbkparmCur(this.getFeptxn().getFeptxnTxCur());
				InbkparmExtMapper inbkparmExtMapper = SpringBeanFactoryUtil.getBean(InbkparmExtMapper.class);
				Inbkparm record = inbkparmExtMapper.queryByPK(inbkparm);
				if (record == null) {
					this.logContext.setRemark(StringUtils.join("INBKPARM 找不到跨行轉帳手續費,INBKPARM_APID=",
							inbkparm.getInbkparmApid(), " INBKPARM_PCODE=", inbkparm.getInbkparmPcode(),
							" INBKPARM_ACQ_FLAG=", inbkparm.getInbkparmAcqFlag(), " INBKPARM_EFFECT_DATE=",
							inbkparm.getInbkparmEffectDate(), " INBKPARM_CUR=", inbkparm.getInbkparmCur()));
					logMessage(Level.INFO, this.logContext);
					return ATMReturnCode.OtherCheckError;
				}
				inbkparm = record; // 這裡不要忘記賦值
				this.getATMBusiness().getFeptxn().setFeptxnActLoss(inbkparm.getInbkparmFeeCustpay());
				// 手續費收入=跨行存款手續(20)-跨行轉帳手續費(15)
				this.getATMBusiness().getFeptxn().setFeptxnAtmProfit(this.getATMBusiness().getFeptxn()
						.getFeptxnFeeCustpayAct().subtract(this.getATMBusiness().getFeptxn().getFeptxnActLoss()));
				this.logContext.setRemark(
						StringUtils.join("手續費收入=", this.getATMBusiness().getFeptxn().getFeptxnAtmProfit().intValue(),
								" 跨行存款手續費=", this.getATMBusiness().getFeptxn().getFeptxnFeeCustpayAct().intValue(),
								" 跨行轉帳手續費=", this.getATMBusiness().getFeptxn().getFeptxnActLoss()));
				logMessage(Level.INFO, this.logContext);
				// 交易金額必須大於手續費金額
				if (this.getATMBusiness().getFeptxn().getFeptxnTxAmt()
						.compareTo(this.getATMBusiness().getFeptxn().getFeptxnFeeCustpay()) < 0) {
					rtnCode = ATMReturnCode.OtherCheckError;
					this.logContext.setRemark(StringUtils.join("交易金額必須大於手續費金額, FEPTXN_TX_AMT=",
							this.getATMBusiness().getFeptxn().getFeptxnTxAmt(), " FEPTXN_FEE_CUSTPAY=",
							this.getATMBusiness().getFeptxn().getFeptxnFeeCustpay()));
					logMessage(Level.INFO, this.logContext);
					return rtnCode;
				}
			}
			if (ATMTXCD.DDR.name().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())) {
				if (!SysStatus.getPropertyValue().getSysstatHbkno()
						.equals(this.getATMBusiness().getFeptxn().getFeptxnTrinBkno())) {
					rtnCode = ATMReturnCode.OtherCheckError;
					this.logContext.setRemark(StringUtils.join("轉入行必須為本行, FEPTXN_TRIN_BKNO=",
							this.getATMBusiness().getFeptxn().getFeptxnTrinBkno()));
					logMessage(Level.INFO, this.logContext);
					return rtnCode;
				}
				if (StringUtils.isBlank(this.getATMBusiness().getFeptxn().getFeptxnTrinActno())) {
					rtnCode = ATMReturnCode.OtherCheckError;
					this.logContext.setRemark("轉入帳號不能為NULL或空白");
					logMessage(Level.INFO, this.logContext);
					return rtnCode;
				}
			}
			// 3.3 本行卡片，檢核卡片狀態
			if ((ATMTXCD.ODR.name().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.DDR.toString().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode()))
					&& SysStatus.getPropertyValue().getSysstatHbkno()
							.equals(this.getATMBusiness().getFeptxn().getFeptxnTroutBkno())) {
				rtnCode = this.getATMBusiness().checkCardStatus();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}
			// 3.4 更新 ATM 狀態
			rtnCode = this.getATMBusiness().updateATMStatus();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			// 3.5 檢核單筆限額
			if (getTxData().getMsgCtl().getMsgctlCheckLimit() == (short) 1) {
				rtnCode = this.getATMBusiness().checkODRDLimit();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}
			// 3.6 檢核ATM電文訊息押碼(MAC)
			if (this.getTxData().getMsgCtl().getMsgctlReqmacType() != null
					&& StringUtils.isNotBlank(this.getTxData().getMsgCtl().getMsgctlReqmacType().toString())) {
				ENCHelper encHelper = new ENCHelper(this.getATMBusiness().getFeptxn());
				//--ben-20220922-//rtnCode = encHelper.checkAtmMac(this.getATMRequest().getMAC());
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}
			// 3.7 取得跨行存款手續費
			if (ATMTXCD.ODE.name().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())) {
//				RefBase<BigDecimal> refFee = new RefBase<BigDecimal>(this._wFEE_AMT);
//				rtnCode = this.getATMBusiness().getODRFEE(refFee);
//				this._wFEE_AMT = refFee.get();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}
			return rtnCode;
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			this.logContext.setProgramName(StringUtils.join(ProgramName, "checkBusinessRule"));
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
	}


	/**
	 * 6. 更新交易記錄檔 *<summary> 判斷是否送主機，並決定送哪一類主機 </summary> <returns></returns>
	 * <remarks></remarks> <history> <modify> <modifier>Kyo</modifier>
	 * <reason>將sendtoHost加上try-catch避免發生例外後回給ATM正常</reason> <date>2010/5/18</date>
	 * </modify> <modify> <modifier>Kyo</modifier> <reason>補上遺漏的Return</reason>
	 * <date>2010/7/13</date> </modify> </history> 'Private Function SendToHost() As
	 * FEPReturnCode Try If ATMBusiness.AtmStr.ATM_ZONE = ATMZone.TWN.ToString Then
	 * Return ATMBusiness.SendToATMP(CByte(ATMPTxType.Accounting)) End If
	 * 
	 * '2010-07-13 by kyo for 補上遺漏的Return Return _rtnCode Catch ex As Exception
	 * LogContext.ProgramException = ex SendEMS(LogContext) Return
	 * CommonReturnCode.ProgramException End Try 'End Function
	 * 
	 * <summary> Update FEPTXN </summary> <remarks></remarks>
	 */
	private void updateFEPTxn() {
		this.getATMBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
		this.getATMBusiness().getFeptxn().setFeptxnPending((short) 0);

		if (StringUtils.isBlank(this.getATMBusiness().getFeptxn().getFeptxnReplyCode())) {

			// 2010-11-25 by kyo for normal不需Call Txhelper
			if (this.rtnCode == FEPReturnCode.Normal) {
				this.getATMBusiness().getFeptxn().setFeptxnReplyCode(NormalRC.ATM_OK);
			} else {
				// 2010-08-11 by kyo for 明祥通知若有修改程式GetRCFromErrorCoe要使用4個參數的版本
				this.getATMBusiness().getFeptxn()
						.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(this.rtnCode.getValue()),
								FEPChannel.FEP, this.getTxData().getTxChannel(), this.getTxData().getLogContext()));
			}
		}
		this.getATMBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());

		// 2010-10-18 by kyo for spec update/* 10/14 修改, 寫入處理結果 */
		this.getATMBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /* AA Complete */

		if (this.rtnCode == CommonReturnCode.Normal) {
			this.getATMBusiness().getFeptxn().setFeptxnTxrust("A"); // 處理結果=成功
		} else {
			this.getATMBusiness().getFeptxn().setFeptxnTxrust("B"); // 處理結果=Reject
		}

		// Modify by Kyo Lai on 2010-03-11 for updateTxData的rtnCode不可蓋掉之前的rtnCode
		if (this.needUpdateFEPTXN) {
			FEPReturnCode rtncode = CommonReturnCode.Normal;
			rtncode = this.getATMBusiness().updateTxData();
			if (rtncode != CommonReturnCode.Normal) {
				this.getATMBusiness().getFeptxn().setFeptxnReplyCode("L013"); // 回寫檔案(FEPTxn)發生錯誤
			}
		}
	}

	/**
	 * 
	 * ''' <summary> ''' Update ATMCOIN,ATMSTAT資料 ''' </summary> '''
	 * <remarks></remarks> ''' <history> ''' <modify> '''
	 * <modifier>Ruling</modifier> ''' <reason>NEW Funcion FOR 硬幣機</reason> '''
	 * <date>2012/09/27</date> ''' </modify> ''' </history>
	 */
	private void UpdateATMCoinForRefill() {

//		DbHelper mDB = new DbHelper(FEPConfig.DBName); todo	

	}

	/**
	 * 
	 * <summary> 處理PrepareATMResponseData的部分複雜電文 這裏只處理ATMRefill的電文的欄位值 </summary>
	 * <remarks></remarks> <history> <modify> <modifier>Kyo</modifier>
	 * <reason>降低程式碼複雜度</reason> <date>2010/2/24</date> </modify> <modify>
	 * <modifier>Kyo</modifier>
	 * <reason>bugreport(001B0239):海外ATM作TTS於PrepareATMResponseData發生例外</reason>
	 * <date>2010/4/15</date> </modify> <modify> <modifier>Kyo</modifier>
	 * <reason>SPEC修改:5/29 與永豐確認, 轉帳金額及次數需累加繳款金額及次數</reason> <date>2010/5/31</date>
	 * </modify> </history>
	 * 
	 * @return
	 */
	private String PrepareATMResponseData() {
		String atmResponseString = "";

		try {
			// 2011-06-02 by kyo for /* 組 ATM_TOTA HEADER */
			this.getATMBusiness().mapResponseFromRequest();
			//ben20221118  this.getATMResponse().setREJCD(this.getATMBusiness().getFeptxn().getFeptxnReplyCode());

			// ATM交易代號
			//--ben-20220922-//ATMTXCD txCode2 = ATMTXCD.parse(this.getATMRequest().getTXCD());
			ATMTXCD txCode2 = ATMTXCD.parse("");

			switch (txCode2) {
			case RWS: {

				AtmcashExtMapper dbAtmcash = SpringBeanFactoryUtil.getBean(AtmcashExtMapper.class);
				List<Atmcash> dtAtmcash = dbAtmcash.GetATMCashByATMNO(getFeptxn().getFeptxnAtmno(), "ATMCASH_BOXNO");
				String curcd[] = { "", "", "", "", "", "", "", "" };

				if (dtAtmcash.size() < 8) {
					this.logContext.setRemark("ATMCash筆數僅有" + dtAtmcash.size() + "筆,小於8筆");
					throw ExceptionUtil.createException(this.logContext.getRemark());
				}
				for (int i = 0; i < 8; i++) {
					if (StringUtils.isNotBlank(dtAtmcash.get(i).getAtmcashCur())) {
						curcd[i] = this.getATMBusiness()
								.getCurrencyByAlpha3(dtAtmcash.get(i).getAtmcashCur().toString()).getCurcdCurBsp();
					}
				}

				// 裝鈔幣別
				//ben20221118  this.getATMResponse().setCURRENCY01(curcd[0]);
				//ben20221118  this.getATMResponse().setCURRENCY02(curcd[1]);
				//ben20221118  this.getATMResponse().setCURRENCY03(curcd[2]);
				//ben20221118  this.getATMResponse().setCURRENCY04(curcd[3]);
				//ben20221118  this.getATMResponse().setCURRENCY05(curcd[4]);
				//ben20221118  this.getATMResponse().setCURRENCY06(curcd[5]);
				//ben20221118  this.getATMResponse().setCURRENCY07(curcd[6]);
				//ben20221118  this.getATMResponse().setCURRENCY08(curcd[7]);

				// 裝鈔面額
				//ben20221118  this.getATMResponse().setUNIT01(new BigDecimal(dtAtmcash.get(0).getAtmcashUnit()));
				//ben20221118  this.getATMResponse().setUNIT02(new BigDecimal(dtAtmcash.get(1).getAtmcashUnit()));
				//ben20221118  this.getATMResponse().setUNIT03(new BigDecimal(dtAtmcash.get(2).getAtmcashUnit()));
				//ben20221118  this.getATMResponse().setUNIT04(new BigDecimal(dtAtmcash.get(3).getAtmcashUnit()));
				//ben20221118  this.getATMResponse().setUNIT05(new BigDecimal(dtAtmcash.get(4).getAtmcashUnit()));
				//ben20221118  this.getATMResponse().setUNIT06(new BigDecimal(dtAtmcash.get(5).getAtmcashUnit()));
				//ben20221118  this.getATMResponse().setUNIT07(new BigDecimal(dtAtmcash.get(6).getAtmcashUnit()));
				//ben20221118  this.getATMResponse().setUNIT08(new BigDecimal(dtAtmcash.get(7).getAtmcashUnit()));

				// 裝鈔張數字
				//ben20221118  this.getATMResponse().setREFILL01(new BigDecimal(dtAtmcash.get(0).getAtmcashRefill()));
				//ben20221118  this.getATMResponse().setREFILL02(new BigDecimal(dtAtmcash.get(1).getAtmcashRefill()));
				//ben20221118  this.getATMResponse().setREFILL03(new BigDecimal(dtAtmcash.get(2).getAtmcashRefill()));
				//ben20221118  this.getATMResponse().setREFILL04(new BigDecimal(dtAtmcash.get(3).getAtmcashRefill()));
				//ben20221118  this.getATMResponse().setREFILL05(new BigDecimal(dtAtmcash.get(4).getAtmcashRefill()));
				//ben20221118  this.getATMResponse().setREFILL06(new BigDecimal(dtAtmcash.get(5).getAtmcashRefill()));
				//ben20221118  this.getATMResponse().setREFILL07(new BigDecimal(dtAtmcash.get(6).getAtmcashRefill()));
				//ben20221118  this.getATMResponse().setREFILL08(new BigDecimal(dtAtmcash.get(7).getAtmcashRefill()));

				// 裝鈔後吐鈔張數
				//ben20221118  this.getATMResponse().setPRSNT01(new BigDecimal(dtAtmcash.get(0).getAtmcashPresent()));
				//ben20221118  this.getATMResponse().setPRSNT02(new BigDecimal(dtAtmcash.get(1).getAtmcashPresent()));
				//ben20221118  this.getATMResponse().setPRSNT03(new BigDecimal(dtAtmcash.get(2).getAtmcashPresent()));
				//ben20221118  this.getATMResponse().setPRSNT04(new BigDecimal(dtAtmcash.get(3).getAtmcashPresent()));
				//ben20221118  this.getATMResponse().setPRSNT05(new BigDecimal(dtAtmcash.get(4).getAtmcashPresent()));
				//ben20221118  this.getATMResponse().setPRSNT06(new BigDecimal(dtAtmcash.get(5).getAtmcashPresent()));
				//ben20221118  this.getATMResponse().setPRSNT07(new BigDecimal(dtAtmcash.get(6).getAtmcashPresent()));
				//ben20221118  this.getATMResponse().setPRSNT08(new BigDecimal(dtAtmcash.get(7).getAtmcashPresent()));

				// 上營業日剩餘張數
				// modified By Maxine for 12/13 修改, 剩餘張數必須為正數, 不得為負數
				//ben20221118  this.getATMResponse().setLLEFT01(new BigDecimal(Math.abs(dtAtmcash.get(0).getAtmcashLeftLbsdy())));
				//ben20221118  this.getATMResponse().setLLEFT02(new BigDecimal(Math.abs(dtAtmcash.get(1).getAtmcashLeftLbsdy())));
				//ben20221118  this.getATMResponse().setLLEFT03(new BigDecimal(Math.abs(dtAtmcash.get(2).getAtmcashLeftLbsdy())));
				//ben20221118  this.getATMResponse().setLLEFT04(new BigDecimal(Math.abs(dtAtmcash.get(3).getAtmcashLeftLbsdy())));
				//ben20221118  this.getATMResponse().setLLEFT05(new BigDecimal(Math.abs(dtAtmcash.get(4).getAtmcashLeftLbsdy())));
				//ben20221118  this.getATMResponse().setLLEFT06(new BigDecimal(Math.abs(dtAtmcash.get(5).getAtmcashLeftLbsdy())));
				//ben20221118  this.getATMResponse().setLLEFT07(new BigDecimal(Math.abs(dtAtmcash.get(6).getAtmcashLeftLbsdy())));
				//ben20221118  this.getATMResponse().setLLEFT08(new BigDecimal(Math.abs(dtAtmcash.get(7).getAtmcashLeftLbsdy())));

				//ben20221118  this.getATMResponse().setCSHCUR(this.getATMBusiness().getAtmStat().getAtmstatDepCur());
				//ben20221118  this.getATMResponse().setCASHCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatDepCnt()));
				//ben20221118  this.getATMResponse().setCASHAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatDepAmt()));
				//ben20221118  this.getATMResponse().setLCSHCUR(this.getATMBusiness().getAtmStat().getAtmstatDepCurLbsdy());
				//ben20221118  this.getATMResponse()
				//ben20221118  		.setLCASHCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatDepCntLbsdy()));
				//ben20221118  this.getATMResponse()
				//ben20221118  		.setLCASHAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatDepAmtLbsdy()));
				//ben20221118  this.getATMResponse().setTCSHCUR(this.getATMBusiness().getAtmStat().getAtmstatDepCurTbsdy());
				//ben20221118  this.getATMResponse()
				//ben20221118  		.setTCASHCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatDepCntLbsdy()));
				//ben20221118  this.getATMResponse()
				//ben20221118  		.setTCASHAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatDepAmtLbsdy()));
				//ben20221118  this.getATMResponse().setRETAIN(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatEatCard()));
				//--ben-20220922-//this.getATMResponse().setVENDOR(this.getATMRequest().getVENDOR());
				//--ben-20220922-//this.getATMResponse().setTYPE(this.getATMRequest().getTYPE());
				//ben20221118  this.getATMResponse().setCOUNTRY(this.getATMBusiness().getAtmStr().getAtmZone());
				//ben20221118  this.getATMResponse().setTABLE(this.getATMBusiness().getAtmStat().getAtmstatTable());

				// bugreport(001B0239):2010-04-15 modified by kyo for 程式與SPEC不一致
				// 2010-04-15 modified by kyo for Object的值為NULL時使用CStr會發生例外，要改用tostring

				// 2012/10/29 Modify by Ruling for 人民幣提款:TTS電文以ATMC外幣資料傳回給ATM
				String lcurcd[] = { "", "", "", "", "", "", "", "" };
				String curcd2[] = { "", "", "", "", "", "", "", "" };
				BigDecimal lcnt[] = new BigDecimal[8];
				BigDecimal lamt[] = new BigDecimal[8];
				BigDecimal cnt[] = new BigDecimal[8];
				BigDecimal amt[] = new BigDecimal[8];

				Arrays.fill(lcnt, BigDecimal.ZERO);
				Arrays.fill(lamt, BigDecimal.ZERO);
				Arrays.fill(cnt, BigDecimal.ZERO);
				Arrays.fill(amt, BigDecimal.ZERO);

				// If txCode = ATMTXCD.TTS Then
				if (ZoneCode.TWN.toString().equals(this.getATMBusiness().getFeptxn().getFeptxnAtmZone())) {

					List<Atmcash> dtAtmcashTTS = dbAtmcash.GetATMCashByATMNOGroupBy(this.getFeptxn().getFeptxnAtmno());

					String atmcashlcurcd[] = { "", "", "", "", "", "", "", "" };
					String atmcashcurcd[] = { "", "", "", "", "", "", "", "" };

					for (int i = 0; i <= dtAtmcashTTS.size() - 1; i++) {
						atmcashlcurcd[i] = dtAtmcashTTS.get(i).getAtmcashRwtCurLbsdy().toString();
						atmcashcurcd[i] = dtAtmcashTTS.get(i).getAtmcashRwtCurTbsdy();
					}

					String atmclcurcd[] = { "", "", "", "", "", "", "", "" };
					String atmccurcd[] = { "", "", "", "", "", "", "", "" };
					BigDecimal atmclcnt[] = new BigDecimal[8];
					BigDecimal atmclamt[] = new BigDecimal[8];
					BigDecimal atmccnt[] = new BigDecimal[8];
					BigDecimal atmcamt[] = new BigDecimal[8];
					int countlbsdy = 0;
					int counttbsdy = 0;
					Arrays.fill(lcnt, BigDecimal.ZERO);
					Arrays.fill(atmclcnt, BigDecimal.ZERO);
					Arrays.fill(atmclamt, BigDecimal.ZERO);
					Arrays.fill(atmccnt, BigDecimal.ZERO);
					Arrays.fill(atmcamt, BigDecimal.ZERO);

					AtmcExtMapper dbAtmc = SpringBeanFactoryUtil.getBean(AtmcExtMapper.class);
					List<Atmc> dtAtmc = dbAtmc.GetATMCByAtmnoFiscTbsdySumDRAMT(
							this.getATMBusiness().getAtmStat().getAtmstatLbsdy(),
							this.getATMBusiness().getAtmStat().getAtmstatTbsdy(),
							this.getATMBusiness().getFeptxn().getFeptxnAtmno());

					for (int i = 0; i <= dtAtmc.size() - 1; i++) {
						if (this.getATMBusiness().getAtmStat().getAtmstatLbsdy()
								.equals(dtAtmc.get(i).getAtmcTbsdyFisc().toString())) {
							atmclcurcd[countlbsdy] = dtAtmc.get(i).getAtmcCur().toString();
							atmclcnt[countlbsdy] = new BigDecimal(dtAtmc.get(i).getAtmcDrCnt());
							atmclamt[countlbsdy] = dtAtmc.get(i).getAtmcDrAmt();
							countlbsdy += 1;
						}
						if (this.getATMBusiness().getAtmStat().getAtmstatTbsdy()
								.equals(dtAtmc.get(i).getAtmcTbsdyFisc().toString())) {
							atmccurcd[counttbsdy] = dtAtmc.get(i).getAtmcCur().toString();
							atmccnt[counttbsdy] = new BigDecimal(dtAtmc.get(i).getAtmcDrCnt());
							atmcamt[counttbsdy] = dtAtmc.get(i).getAtmcDrAmt();
							counttbsdy += 1;
						}
					}

					// 上營業日
					for (int i = 0; i <= atmcashlcurcd.length - 1; i++) {
						lcurcd[i] = atmcashlcurcd[i];

						if (CurrencyType.TWD.toString().equals(atmcashlcurcd[i])) {

							lcnt[i] = new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCwdCntLbsdy());
							lamt[i] = new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCwdAmtLbsdy());
						} else {

							for (int j = 0; j <= dtAtmc.size() - 1; j++) {
								if (atmcashlcurcd[i].equals(atmclcurcd[j])) {
									lcnt[i] = atmclcnt[j];
									lamt[i] = atmclamt[j];
								}
							}
						}
					}

					// 本營業日
					for (int i = 0; i <= atmcashcurcd.length - 1; i++) {
						curcd2[i] = atmcashcurcd[i];
						if (CurrencyType.TWD.toString().equals(atmcashcurcd[i])) {
							cnt[i] = new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCwdCntTbsdy());
							amt[i] = new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCwdAmtTbsdy());

						} else {
							for (int j = 0; j <= atmcashcurcd.length - 1; j++) {
								if (atmcashcurcd[i].equals(atmccurcd[j])) {
									cnt[i] = atmccnt[j];
									amt[i] = atmcamt[j];
								}
							}
						}
					}

					// 海外機台
					AtmcashExtMapper dbAtmcashTTS = SpringBeanFactoryUtil.getBean(AtmcashExtMapper.class);
					List<Atmcash> dtAtmcashTTS1 = dbAtmcashTTS.GetATMCashByATMNOGroupBy(getFeptxn().getFeptxnAtmno());

					for (int i = 0; i <= dtAtmcashTTS1.size() - i; i++) {
						lcurcd[i] = dtAtmcashTTS1.get(i).getAtmcashRwtCurLbsdy().toString();
						lcnt[i] = new BigDecimal(dtAtmcashTTS1.get(i).getAtmcashCwdCntLbsdy());
						lamt[i] = new BigDecimal(dtAtmcashTTS1.get(i).getAtmcashCwdAmtLbsdy());
						curcd2[i] = dtAtmcashTTS1.get(i).getAtmcashRwtCurLbsdy().toString();
						cnt[i] = new BigDecimal(dtAtmcashTTS1.get(i).getAtmcashCwdCntLbsdy());
						amt[i] = new BigDecimal(dtAtmcashTTS1.get(i).getAtmcashCwdAmtLbsdy());
					}
				}
				//ben20221118  
				/*
				this.getATMResponse().setLCURRENCY01(lcurcd[0]);
				this.getATMResponse().setLCURRENCY02(lcurcd[1]);
				this.getATMResponse().setLCURRENCY03(lcurcd[2]);
				this.getATMResponse().setLCURRENCY04(lcurcd[3]);
				this.getATMResponse().setLCURRENCY05(lcurcd[4]);
				this.getATMResponse().setLCURRENCY06(lcurcd[5]);
				this.getATMResponse().setLCURRENCY07(lcurcd[6]);
				this.getATMResponse().setLCURRENCY08(lcurcd[7]);

				this.getATMResponse().setLCWDCT01(lcnt[0]);
				this.getATMResponse().setLCWDCT02(lcnt[1]);
				this.getATMResponse().setLCWDCT03(lcnt[2]);
				this.getATMResponse().setLCWDCT04(lcnt[3]);
				this.getATMResponse().setLCWDCT05(lcnt[4]);
				this.getATMResponse().setLCWDCT06(lcnt[5]);
				this.getATMResponse().setLCWDCT07(lcnt[6]);
				this.getATMResponse().setLCWDCT08(lcnt[7]);

				this.getATMResponse().setLCWDAT01(lamt[0]);
				this.getATMResponse().setLCWDAT02(lamt[1]);
				this.getATMResponse().setLCWDAT03(lamt[2]);
				this.getATMResponse().setLCWDAT04(lamt[3]);
				this.getATMResponse().setLCWDAT05(lamt[4]);
				this.getATMResponse().setLCWDAT06(lamt[5]);
				this.getATMResponse().setLCWDAT07(lamt[6]);
				this.getATMResponse().setLCWDAT08(lamt[7]);

				this.getATMResponse().setTCURRENCY01(curcd2[0]);
				this.getATMResponse().setTCURRENCY02(curcd2[1]);
				this.getATMResponse().setTCURRENCY03(curcd2[2]);
				this.getATMResponse().setTCURRENCY04(curcd2[3]);
				this.getATMResponse().setTCURRENCY05(curcd2[4]);
				this.getATMResponse().setTCURRENCY06(curcd2[5]);
				this.getATMResponse().setTCURRENCY07(curcd2[6]);
				this.getATMResponse().setTCURRENCY08(curcd2[7]);

				this.getATMResponse().setTCWDCT01(cnt[0]);
				this.getATMResponse().setTCWDCT02(cnt[1]);
				this.getATMResponse().setTCWDCT03(cnt[2]);
				this.getATMResponse().setTCWDCT04(cnt[3]);
				this.getATMResponse().setTCWDCT05(cnt[4]);
				this.getATMResponse().setTCWDCT06(cnt[5]);
				this.getATMResponse().setTCWDCT07(cnt[6]);
				this.getATMResponse().setTCWDCT08(cnt[7]);

				this.getATMResponse().setTCWDAT01(amt[0]);
				this.getATMResponse().setTCWDAT02(amt[1]);
				this.getATMResponse().setTCWDAT03(amt[2]);
				this.getATMResponse().setTCWDAT04(amt[3]);
				this.getATMResponse().setTCWDAT05(amt[4]);
				this.getATMResponse().setTCWDAT06(amt[5]);
				this.getATMResponse().setTCWDAT07(amt[6]);
				this.getATMResponse().setTCWDAT08(amt[7]);

				// 2014/01/15 Modify by Ruling for 轉帳金額/次數需累加繳款金額/次數
				this.getATMResponse().setTFRAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatTfrAmt()
						+ this.getATMBusiness().getAtmStat().getAtmstatPayAmt()));
				this.getATMResponse().setTFRCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatTfrCnt()
						+ this.getATMBusiness().getAtmStat().getAtmstatPayCnt()));

				this.getATMResponse()
						.setLTFRAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatTfrAmtLbsdy()
								+ this.getATMBusiness().getAtmStat().getAtmstatPayAmtLbsdy()));
				this.getATMResponse()
						.setLTFRCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatTfrCntLbsdy()
								+ this.getATMBusiness().getAtmStat().getAtmstatPayCntLbsdy()));

				this.getATMResponse()
						.setTTFRAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatTfrAmtTbsdy()
								+ this.getATMBusiness().getAtmStat().getAtmstatPayAmtTbsdy()));
				this.getATMResponse()
						.setTTFRCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatTfrCntTbsdy()
								+ this.getATMBusiness().getAtmStat().getAtmstatPayCntTbsdy()));

				// todo destroy
//				dbAtmcash.Dispose();
				// 2012/10/29 Modify by Ruling for 人民幣提款:TTS電文以ATMC外幣資料傳回給ATM
//                dbAtmc.Dispose();
 */
				break;
			}
			case TTF:
			case TTI: {

				AtmcashExtMapper dbAtmcash = SpringBeanFactoryUtil.getBean(AtmcashExtMapper.class);
				List<Atmcash> dtAtmcash = dbAtmcash.GetATMCashByATMNO(getFeptxn().getFeptxnAtmno(), "ATMCASH_BOXNO");

				if (dtAtmcash.size() < 8) {
					this.logContext.setRemark("ATMCash筆數僅有" + dtAtmcash.size() + "筆,小於8筆");
					throw ExceptionUtil.createException(this.logContext.getRemark());
				}

				String curcd[] = { "", "", "", "", "", "", "", "" };

				if (txCode2 == ATMTXCD.TTF) {
					for (int i = 0; i < 8; i++) {
						if (StringUtils.isNotBlank(dtAtmcash.get(i).getAtmcashCur().toString())) {
							if (StringUtils.isNotBlank(dtAtmcash.get(i).getAtmcashCur().toString().trim())) {
								curcd[i] = "00" + this.getATMBusiness()
										.getCurrencyByAlpha3(dtAtmcash.get(i).getAtmcashCur().toString())
										.getCurcdCurBsp();
							}
						}
					}
					//ben20221118  
					/*
					this.getATMResponse().setTYPE01(curcd[0]);
					this.getATMResponse().setTYPE02(curcd[1]);
					this.getATMResponse().setTYPE03(curcd[2]);
					this.getATMResponse().setTYPE04(curcd[3]);
					this.getATMResponse().setTYPE05(curcd[4]);
					this.getATMResponse().setTYPE06(curcd[5]);
					this.getATMResponse().setTYPE07(curcd[6]);
					this.getATMResponse().setTYPE08(curcd[7]);
					*/
				}
				//ben20221118  
				/*
				this.getATMResponse().setUNIT01(new BigDecimal(dtAtmcash.get(0).getAtmcashUnit()));
				this.getATMResponse().setUNIT02(new BigDecimal(dtAtmcash.get(1).getAtmcashUnit()));
				this.getATMResponse().setUNIT03(new BigDecimal(dtAtmcash.get(2).getAtmcashUnit()));
				this.getATMResponse().setUNIT04(new BigDecimal(dtAtmcash.get(3).getAtmcashUnit()));
				this.getATMResponse().setUNIT05(new BigDecimal(dtAtmcash.get(4).getAtmcashUnit()));
				this.getATMResponse().setUNIT06(new BigDecimal(dtAtmcash.get(5).getAtmcashUnit()));
				this.getATMResponse().setUNIT07(new BigDecimal(dtAtmcash.get(6).getAtmcashUnit()));
				this.getATMResponse().setUNIT08(new BigDecimal(dtAtmcash.get(7).getAtmcashUnit()));

				this.getATMResponse().setREFILL01(new BigDecimal(dtAtmcash.get(0).getAtmcashRefill()));
				this.getATMResponse().setREFILL02(new BigDecimal(dtAtmcash.get(1).getAtmcashRefill()));
				this.getATMResponse().setREFILL03(new BigDecimal(dtAtmcash.get(2).getAtmcashRefill()));
				this.getATMResponse().setREFILL04(new BigDecimal(dtAtmcash.get(3).getAtmcashRefill()));
				this.getATMResponse().setREFILL05(new BigDecimal(dtAtmcash.get(4).getAtmcashRefill()));
				this.getATMResponse().setREFILL06(new BigDecimal(dtAtmcash.get(5).getAtmcashRefill()));
				this.getATMResponse().setREFILL07(new BigDecimal(dtAtmcash.get(6).getAtmcashRefill()));
				this.getATMResponse().setREFILL08(new BigDecimal(dtAtmcash.get(7).getAtmcashRefill()));
				
				// 2012/01/30 Modify by Ruling for PRSNT1~8 超過4位時由右到左取4位
				if (dtAtmcash.get(0).getAtmcashPresent().toString().length() > 4) {
					this.getATMResponse()
							.setPRSNT01(new BigDecimal(dtAtmcash.get(0).getAtmcashPresent().toString().substring(
									dtAtmcash.get(0).getAtmcashPresent().toString().length() - 4,
									dtAtmcash.get(0).getAtmcashPresent().toString().length() - 4 + 4)));
				} else {
					this.getATMResponse().setPRSNT01(new BigDecimal(dtAtmcash.get(0).getAtmcashPresent()));
				}

				if (dtAtmcash.get(1).getAtmcashPresent().toString().length() > 4) {
					this.getATMResponse()
							.setPRSNT02(new BigDecimal(dtAtmcash.get(1).getAtmcashPresent().toString().substring(
									dtAtmcash.get(1).getAtmcashPresent().toString().length() - 4,
									dtAtmcash.get(1).getAtmcashPresent().toString().length() - 4 + 4)));
				} else {
					this.getATMResponse().setPRSNT02(new BigDecimal(dtAtmcash.get(1).getAtmcashPresent()));
				}

				if (dtAtmcash.get(2).getAtmcashPresent().toString().length() > 4) {
					this.getATMResponse()
							.setPRSNT03(new BigDecimal(dtAtmcash.get(2).getAtmcashPresent().toString().substring(
									dtAtmcash.get(2).getAtmcashPresent().toString().length() - 4,
									dtAtmcash.get(2).getAtmcashPresent().toString().length() - 4 + 4)));
				} else {
					this.getATMResponse().setPRSNT03(new BigDecimal(dtAtmcash.get(2).getAtmcashPresent()));
				}

				if (dtAtmcash.get(3).getAtmcashPresent().toString().length() > 4) {
					this.getATMResponse()
							.setPRSNT04(new BigDecimal(dtAtmcash.get(3).getAtmcashPresent().toString().substring(
									dtAtmcash.get(3).getAtmcashPresent().toString().length() - 4,
									dtAtmcash.get(3).getAtmcashPresent().toString().length() - 4 + 4)));
				} else {
					this.getATMResponse().setPRSNT04(new BigDecimal(dtAtmcash.get(3).getAtmcashPresent()));
				}

				if (dtAtmcash.get(4).getAtmcashPresent().toString().length() > 4) {
					this.getATMResponse()
							.setPRSNT05(new BigDecimal(dtAtmcash.get(4).getAtmcashPresent().toString().substring(
									dtAtmcash.get(4).getAtmcashPresent().toString().length() - 4,
									dtAtmcash.get(4).getAtmcashPresent().toString().length() - 4 + 4)));
				} else {
					this.getATMResponse().setPRSNT05(new BigDecimal(dtAtmcash.get(4).getAtmcashPresent()));
				}

				if (dtAtmcash.get(5).getAtmcashPresent().toString().length() > 4) {
					this.getATMResponse()
							.setPRSNT06(new BigDecimal(dtAtmcash.get(5).getAtmcashPresent().toString().substring(
									dtAtmcash.get(4).getAtmcashPresent().toString().length() - 4,
									dtAtmcash.get(5).getAtmcashPresent().toString().length() - 4 + 4)));
				} else {
					this.getATMResponse().setPRSNT06(new BigDecimal(dtAtmcash.get(5).getAtmcashPresent()));
				}

				if (dtAtmcash.get(6).getAtmcashPresent().toString().length() > 4) {
					this.getATMResponse()
							.setPRSNT07(new BigDecimal(dtAtmcash.get(6).getAtmcashPresent().toString().substring(
									dtAtmcash.get(4).getAtmcashPresent().toString().length() - 4,
									dtAtmcash.get(6).getAtmcashPresent().toString().length() - 4 + 4)));
				} else {
					this.getATMResponse().setPRSNT07(new BigDecimal(dtAtmcash.get(6).getAtmcashPresent()));
				}

				if (dtAtmcash.get(7).getAtmcashPresent().toString().length() > 4) {
					this.getATMResponse()
							.setPRSNT08(new BigDecimal(dtAtmcash.get(7).getAtmcashPresent().toString().substring(
									dtAtmcash.get(4).getAtmcashPresent().toString().length() - 4,
									dtAtmcash.get(7).getAtmcashPresent().toString().length() - 4 + 4)));
				} else {
					this.getATMResponse().setPRSNT08(new BigDecimal(dtAtmcash.get(7).getAtmcashPresent()));
				}

				// 2012/01/30 Modify by Ruling for LLEFT1~8 超過4位時由右到左取4位
				// modified By Maxine for 12/13 修改, 剩餘張數必須為正數, 不得為負數
				BigDecimal leftLBSDY = new BigDecimal(Math.abs(dtAtmcash.get(0).getAtmcashLeftLbsdy()));
				if (leftLBSDY.toString().length() > 4) {
					this.getATMResponse().setLLEFT01(new BigDecimal(leftLBSDY.toString()
							.substring(leftLBSDY.toString().length() - 4, leftLBSDY.toString().length() - 4 + 4)));
				} else {
					this.getATMResponse().setLLEFT01(leftLBSDY);
				}

				leftLBSDY = new BigDecimal(Math.abs(dtAtmcash.get(1).getAtmcashLeftLbsdy()));
				if (leftLBSDY.toString().length() > 4) {
					this.getATMResponse().setLLEFT02(new BigDecimal(leftLBSDY.toString()
							.substring(leftLBSDY.toString().length() - 4, leftLBSDY.toString().length() - 4 + 4)));
				} else {
					this.getATMResponse().setLLEFT02(leftLBSDY);
				}
				leftLBSDY = new BigDecimal(Math.abs(dtAtmcash.get(2).getAtmcashLeftLbsdy()));
				if (leftLBSDY.toString().length() > 4) {
					this.getATMResponse().setLLEFT03(new BigDecimal(leftLBSDY.toString()
							.substring(leftLBSDY.toString().length() - 4, leftLBSDY.toString().length() - 4 + 4)));
				} else {
					this.getATMResponse().setLLEFT03(leftLBSDY);
				}
				leftLBSDY = new BigDecimal(Math.abs(dtAtmcash.get(3).getAtmcashLeftLbsdy()));
				if (leftLBSDY.toString().length() > 4) {
					this.getATMResponse().setLLEFT04(new BigDecimal(leftLBSDY.toString()
							.substring(leftLBSDY.toString().length() - 4, leftLBSDY.toString().length() - 4 + 4)));
				} else {
					this.getATMResponse().setLLEFT04(leftLBSDY);
				}
				leftLBSDY = new BigDecimal(Math.abs(dtAtmcash.get(4).getAtmcashLeftLbsdy()));
				if (leftLBSDY.toString().length() > 4) {
					this.getATMResponse().setLLEFT05(new BigDecimal(leftLBSDY.toString()
							.substring(leftLBSDY.toString().length() - 4, leftLBSDY.toString().length() - 4 + 4)));
				} else {
					this.getATMResponse().setLLEFT05(leftLBSDY);
				}
				leftLBSDY = new BigDecimal(Math.abs(dtAtmcash.get(5).getAtmcashLeftLbsdy()));
				if (leftLBSDY.toString().length() > 4) {
					this.getATMResponse().setLLEFT06(new BigDecimal(leftLBSDY.toString()
							.substring(leftLBSDY.toString().length() - 4, leftLBSDY.toString().length() - 4 + 4)));
				} else {
					this.getATMResponse().setLLEFT06(leftLBSDY);
				}
				leftLBSDY = new BigDecimal(Math.abs(dtAtmcash.get(6).getAtmcashLeftLbsdy()));
				if (leftLBSDY.toString().length() > 4) {
					this.getATMResponse().setLLEFT07(new BigDecimal(leftLBSDY.toString()
							.substring(leftLBSDY.toString().length() - 4, leftLBSDY.toString().length() - 4 + 4)));
				} else {
					this.getATMResponse().setLLEFT07(leftLBSDY);
				}
				leftLBSDY = new BigDecimal(Math.abs(dtAtmcash.get(7).getAtmcashLeftLbsdy()));
				if (leftLBSDY.toString().length() > 4) {
					this.getATMResponse().setLLEFT08(new BigDecimal(leftLBSDY.toString()
							.substring(leftLBSDY.toString().length() - 4, leftLBSDY.toString().length() - 4 + 4)));
				} else {
					this.getATMResponse().setLLEFT08(leftLBSDY);
				}

				this.getATMResponse().setCWDAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCwdAmt())); // 裝鈔後提款金額
				this.getATMResponse().setCWDCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCwdCnt()));

				// 2010-05-31 by kyo for SPEC修改:5/29 與永豐確認, 轉帳金額及次數需累加繳款金額及次數
				this.getATMResponse().setTFRAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatTfrAmt()
						+ this.getATMBusiness().getAtmStat().getAtmstatPayCnt()));
				this.getATMResponse().setTFRCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatTfrCnt()
						+ this.getATMBusiness().getAtmStat().getAtmstatPayCnt()));
				this.getATMResponse().setDEPAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatDepAmt()));
				this.getATMResponse().setDEPCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatDepCnt()));
				this.getATMResponse().setPAYAMT(new BigDecimal(0));
				this.getATMResponse().setPAYCNT(new BigDecimal(0));
				this.getATMResponse()
						.setLCWDAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCwdAmtLbsdy()));
				this.getATMResponse()
						.setLCWDCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCwdCntLbsdy()));
				this.getATMResponse()
						.setLDEPAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatDepAmtLbsdy()));
				this.getATMResponse()
						.setLDEPCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatDepCntLbsdy()));

				// 2010-05-31 by kyo for SPEC修改:5/29 與永豐確認, 轉帳金額及次數需累加繳款金額及次數
				this.getATMResponse()
						.setLTFRAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatTfrAmtLbsdy()
								+ this.getATMBusiness().getAtmStat().getAtmstatPayAmtLbsdy()));
				this.getATMResponse()
						.setLTFRCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatTfrCntLbsdy()
								+ this.getATMBusiness().getAtmStat().getAtmstatPayCntLbsdy()));
				this.getATMResponse().setLPAYAMT(new BigDecimal(0));
				this.getATMResponse().setLPAYCNT(new BigDecimal(0));
				this.getATMResponse()
						.setTCWDAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCwdAmtTbsdy()));
				this.getATMResponse()
						.setTCWDCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCwdCntTbsdy()));
				this.getATMResponse()
						.setTDEPAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatDepAmtTbsdy()));
				this.getATMResponse()
						.setTDEPCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatDepCntTbsdy()));

				// 2010-05-31 by kyo for SPEC修改:5/29 與永豐確認, 轉帳金額及次數需累加繳款金額及次數
				this.getATMResponse()
						.setTTFRAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatTfrAmtTbsdy()
								+ this.getATMBusiness().getAtmStat().getAtmstatPayAmtTbsdy()));
				this.getATMResponse()
						.setTTFRCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatTfrCntTbsdy()
								+ this.getATMBusiness().getAtmStat().getAtmstatPayCntTbsdy()));
				this.getATMResponse().setTPAYAMT(new BigDecimal(0));
				this.getATMResponse().setTPAYCNT(new BigDecimal(0));
				this.getATMResponse().setRETAIN(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatEatCard()));
				this.getATMResponse().setRETAIN(new BigDecimal(this.getATMBusiness().getAtmStr().getAtmZone()));

				// TODO:{Jim} 修改電文定義檔
				//--ben-20220922-//this.getATMResponse().setTABLE(this.getATMRequest().getTABLE());

				if (ATMTXCD.TTF == txCode2) {
					this.getATMResponse().setRFNTCNT(new BigDecimal(0));
					this.getATMResponse().setRFUSCNT(new BigDecimal(0));
					this.getATMResponse().setRFHKCNT(new BigDecimal(0));
					this.getATMResponse().setRFJPCNT(new BigDecimal(0));

					// 2012/10/22 Modify by Ruling for 新增人民幣的業務:外幣提領次數及金額，改抓ATMC
					GetATMCForForeignCurcd();
				}
//              dbAtmcash.Dispose()  todo
 * 
 */
				break;
			}
			case TTC: {
				//ben20221118  
				/*
				// 2012/09/04 Modify by Ruling for 新增硬幣機的業務
				this.getATMResponse()
						.setDCOINAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCdepAmt()));
				this.getATMResponse()
						.setDCOINCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCdepCnt()));
				this.getATMResponse()
						.setWCOINAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCcwdAmt()));
				this.getATMResponse()
						.setWCOINCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCcwdCnt()));
				this.getATMResponse()
						.setLDCOINAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCdepAmtLbsdy()));
				this.getATMResponse()
						.setLDCOINCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCdepCntLbsdy()));
				this.getATMResponse()
						.setLWCOINAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCcwdAmtLbsdy()));
				this.getATMResponse()
						.setLWCOINCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCcwdCntLbsdy()));

				this.getATMResponse()
						.setTDCOINAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCdepAmtTbsdy()));
				this.getATMResponse()
						.setTDCOINCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCdepCntTbsdy()));
				this.getATMResponse()
						.setTWCOINAMT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCcwdAmtTbsdy()));
				this.getATMResponse()
						.setTWCOINCNT(new BigDecimal(this.getATMBusiness().getAtmStat().getAtmstatCcwdCntTbsdy()));

				// 2018/11/29 Modify by Ruling for OKI硬幣機功能，TTC增加回給ATM之硬幣機裝幣資料
				AtmcoinExtMapper dbAtmcoin = SpringBeanFactoryUtil.getBean(AtmcoinExtMapper.class);
				List<Map<String, String>> dtAtmcoin = dbAtmcoin
						.GetATMCoinByAtmnoRwtseqnoSettleforTTC(getFeptxn().getFeptxnAtmno());

				if (dtAtmcoin.size() > 0) {
					for (int i = 0; i < dtAtmcoin.size(); i++) {
						switch (i) {
						case 0: {
							this.getATMResponse().setBOXID01(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse()
									.setUNIT01(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT").toString()));
							this.getATMResponse()
									.setCURID01(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse()
									.setREFILL01(new BigDecimal(dtAtmcoin.get(i).get("refillSum").toString()));
						}
						case 1: {
							this.getATMResponse().setBOXID02(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse()
									.setUNIT02(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT").toString()));
							this.getATMResponse()
									.setCURID02(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse()
									.setREFILL02(new BigDecimal(dtAtmcoin.get(i).get("refillSum").toString()));
						}
						case 2: {
							this.getATMResponse().setBOXID03(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse()
									.setUNIT03(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT").toString()));
							this.getATMResponse()
									.setCURID03(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse()
									.setREFILL03(new BigDecimal(dtAtmcoin.get(i).get("refillSum").toString()));
						}
						case 3: {
							this.getATMResponse().setBOXID04(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse()
									.setUNIT04(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT").toString()));
							this.getATMResponse()
									.setCURID04(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse()
									.setREFILL04(new BigDecimal(dtAtmcoin.get(i).get("refillSum").toString()));
						}
						case 4: {
							this.getATMResponse().setBOXID05(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse()
									.setUNIT05(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT").toString()));
							this.getATMResponse()
									.setCURID05(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse()
									.setREFILL05(new BigDecimal(dtAtmcoin.get(i).get("refillSum").toString()));
						}
						case 5: {
							this.getATMResponse().setBOXID06(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse()
									.setUNIT06(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT").toString()));
							this.getATMResponse()
									.setCURID06(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse()
									.setREFILL06(new BigDecimal(dtAtmcoin.get(i).get("refillSum").toString()));
						}
						case 6: {
							this.getATMResponse().setBOXID07(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse()
									.setUNIT07(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT").toString()));
							this.getATMResponse()
									.setCURID07(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse()
									.setREFILL07(new BigDecimal(dtAtmcoin.get(i).get("refillSum").toString()));
						}
						case 7: {
							this.getATMResponse().setBOXID08(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse()
									.setUNIT08(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT").toString()));
							this.getATMResponse()
									.setCURID08(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse()
									.setREFILL08(new BigDecimal(dtAtmcoin.get(i).get("refillSum").toString()));
						}
						case 8: {
							this.getATMResponse().setBOXID09(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse()
									.setUNIT09(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT").toString()));
							this.getATMResponse()
									.setCURID09(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse()
									.setREFILL09(new BigDecimal(dtAtmcoin.get(i).get("refillSum").toString()));
						}
						case 9: {
							this.getATMResponse().setBOXID10(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse()
									.setUNIT10(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT").toString()));
							this.getATMResponse()
									.setCURID10(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse()
									.setREFILL10(new BigDecimal(dtAtmcoin.get(i).get("refillSum").toString()));
						}
						case 10: {
							this.getATMResponse().setBOXID11(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse()
									.setUNIT11(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT").toString()));
							this.getATMResponse()
									.setCURID11(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse()
									.setREFILL11(new BigDecimal(dtAtmcoin.get(i).get("refillSum").toString()));
						}
						case 11: {
							this.getATMResponse().setBOXID12(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse()
									.setUNIT12(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT").toString()));
							this.getATMResponse()
									.setCURID12(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse()
									.setREFILL12(new BigDecimal(dtAtmcoin.get(i).get("refillSum").toString()));
						}
						case 12: {
							this.getATMResponse().setBOXID13(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse()
									.setUNIT13(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT").toString()));
							this.getATMResponse()
									.setCURID13(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse()
									.setREFILL13(new BigDecimal(dtAtmcoin.get(i).get("refillSum").toString()));
						}
						case 13: {
							this.getATMResponse().setBOXID14(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse()
									.setUNIT14(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT").toString()));
							this.getATMResponse()
									.setCURID14(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse()
									.setREFILL14(new BigDecimal(dtAtmcoin.get(i).get("refillSum").toString()));
						}
						case 14: {
							this.getATMResponse().setBOXID15(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse()
									.setUNIT15(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT").toString()));
							this.getATMResponse()
									.setCURID15(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse()
									.setREFILL15(new BigDecimal(dtAtmcoin.get(i).get("refillSum").toString()));
						}
						}
					}
				}
				if (dtAtmcoin != null) {
					dtAtmcoin = null; // dtAtmcoin.Dispose() todo
				}
				*/
			}
			case BOX:
			case BAK:
			case CSH: {
				AtmboxExtMapper dbAtmbox = SpringBeanFactoryUtil.getBean(AtmboxExtMapper.class);
				List<Map<String, String>> dtAtmbox = null;

				if (StringUtils.isNotBlank(getFeptxn().getFeptxnAtmno())) {
					dtAtmbox = dbAtmbox.GetATMBoxByAtmnoRwtseqnoSettle3(getFeptxn().getFeptxnAtmno(),
							//--ben-20220922-//this.getATMRequest().getRWTSEQNO().substring(0, 4), 0);
							"",0);
				} else {
					dtAtmbox = dbAtmbox
							//--ben-20220922-//.GetATMBoxByAtmnoRwtseqnoSettle2(this.getATMRequest().getRWTSEQNO().substring(0, 4), 0);
					.GetATMBoxByAtmnoRwtseqnoSettle2("", 0);
				}
				
				//ben20221118  
				/*
				if (dtAtmbox.size() > 0) {
					for (int i = 0; i < dtAtmbox.size(); i++) {
						switch (i) {
						case 0: {
							this.getATMResponse().setBOXID01(dtAtmbox.get(i).get("ATMBOX_BOXNO").toString());
							this.getATMResponse().setBOXUNIT01(new BigDecimal(dtAtmbox.get(i).get("ATMBOX_UNIT")));
							this.getATMResponse()
									.setCURID01(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmbox.get(i).get("ATMBOX_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL01(new BigDecimal(dtAtmbox.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT01(new BigDecimal(dtAtmbox.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT01(new BigDecimal(dtAtmbox.get(i).get("presentSum")));
							this.getATMResponse().setREJECT01(new BigDecimal(dtAtmbox.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW01(new BigDecimal(dtAtmbox.get(i).get("unknowSum")));
							break;
						}
						case 1: {
							this.getATMResponse().setBOXID02(dtAtmbox.get(i).get("ATMBOX_BOXNO").toString());
							this.getATMResponse().setBOXUNIT02(new BigDecimal(dtAtmbox.get(i).get("ATMBOX_UNIT")));
							this.getATMResponse()
									.setCURID02(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmbox.get(i).get("ATMBOX_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL02(new BigDecimal(dtAtmbox.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT02(new BigDecimal(dtAtmbox.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT02(new BigDecimal(dtAtmbox.get(i).get("presentSum")));
							this.getATMResponse().setREJECT02(new BigDecimal(dtAtmbox.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW02(new BigDecimal(dtAtmbox.get(i).get("unknowSum")));
							break;
						}
						case 2: {
							this.getATMResponse().setBOXID03(dtAtmbox.get(i).get("ATMBOX_BOXNO").toString());
							this.getATMResponse().setBOXUNIT03(new BigDecimal(dtAtmbox.get(i).get("ATMBOX_UNIT")));
							this.getATMResponse()
									.setCURID03(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmbox.get(i).get("ATMBOX_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL03(new BigDecimal(dtAtmbox.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT03(new BigDecimal(dtAtmbox.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT03(new BigDecimal(dtAtmbox.get(i).get("presentSum")));
							this.getATMResponse().setREJECT03(new BigDecimal(dtAtmbox.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW03(new BigDecimal(dtAtmbox.get(i).get("unknowSum")));
							break;
						}
						case 3: {
							this.getATMResponse().setBOXID04(dtAtmbox.get(i).get("ATMBOX_BOXNO").toString());
							this.getATMResponse().setBOXUNIT04(new BigDecimal(dtAtmbox.get(i).get("ATMBOX_UNIT")));
							this.getATMResponse()
									.setCURID04(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmbox.get(i).get("ATMBOX_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL04(new BigDecimal(dtAtmbox.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT04(new BigDecimal(dtAtmbox.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT04(new BigDecimal(dtAtmbox.get(i).get("presentSum")));
							this.getATMResponse().setREJECT04(new BigDecimal(dtAtmbox.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW04(new BigDecimal(dtAtmbox.get(i).get("unknowSum")));
							break;
						}
						case 4: {
							this.getATMResponse().setBOXID05(dtAtmbox.get(i).get("ATMBOX_BOXNO").toString());
							this.getATMResponse().setBOXUNIT05(new BigDecimal(dtAtmbox.get(i).get("ATMBOX_UNIT")));
							this.getATMResponse()
									.setCURID05(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmbox.get(i).get("ATMBOX_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL05(new BigDecimal(dtAtmbox.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT05(new BigDecimal(dtAtmbox.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT05(new BigDecimal(dtAtmbox.get(i).get("presentSum")));
							this.getATMResponse().setREJECT05(new BigDecimal(dtAtmbox.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW05(new BigDecimal(dtAtmbox.get(i).get("unknowSum")));
							break;
						}
						case 5: {
							this.getATMResponse().setBOXID06(dtAtmbox.get(i).get("ATMBOX_BOXNO").toString());
							this.getATMResponse().setBOXUNIT06(new BigDecimal(dtAtmbox.get(i).get("ATMBOX_UNIT")));
							this.getATMResponse()
									.setCURID06(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmbox.get(i).get("ATMBOX_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL06(new BigDecimal(dtAtmbox.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT06(new BigDecimal(dtAtmbox.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT06(new BigDecimal(dtAtmbox.get(i).get("presentSum")));
							this.getATMResponse().setREJECT06(new BigDecimal(dtAtmbox.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW06(new BigDecimal(dtAtmbox.get(i).get("unknowSum")));
							break;
						}
						case 6: {
							this.getATMResponse().setBOXID07(dtAtmbox.get(i).get("ATMBOX_BOXNO").toString());
							this.getATMResponse().setBOXUNIT07(new BigDecimal(dtAtmbox.get(i).get("ATMBOX_UNIT")));
							this.getATMResponse()
									.setCURID07(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmbox.get(i).get("ATMBOX_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL07(new BigDecimal(dtAtmbox.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT07(new BigDecimal(dtAtmbox.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT07(new BigDecimal(dtAtmbox.get(i).get("presentSum")));
							this.getATMResponse().setREJECT07(new BigDecimal(dtAtmbox.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW07(new BigDecimal(dtAtmbox.get(i).get("unknowSum")));
							break;
						}
						case 7: {
							this.getATMResponse().setBOXID08(dtAtmbox.get(i).get("ATMBOX_BOXNO").toString());
							this.getATMResponse().setBOXUNIT08(new BigDecimal(dtAtmbox.get(i).get("ATMBOX_UNIT")));
							this.getATMResponse()
									.setCURID08(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmbox.get(i).get("ATMBOX_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL08(new BigDecimal(dtAtmbox.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT08(new BigDecimal(dtAtmbox.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT08(new BigDecimal(dtAtmbox.get(i).get("presentSum")));
							this.getATMResponse().setREJECT08(new BigDecimal(dtAtmbox.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW08(new BigDecimal(dtAtmbox.get(i).get("unknowSum")));
							break;
						}
						case 8: {
							this.getATMResponse().setBOXID09(dtAtmbox.get(i).get("ATMBOX_BOXNO").toString());
							this.getATMResponse().setBOXUNIT09(new BigDecimal(dtAtmbox.get(i).get("ATMBOX_UNIT")));
							this.getATMResponse()
									.setCURID09(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmbox.get(i).get("ATMBOX_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL09(new BigDecimal(dtAtmbox.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT09(new BigDecimal(dtAtmbox.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT09(new BigDecimal(dtAtmbox.get(i).get("presentSum")));
							this.getATMResponse().setREJECT09(new BigDecimal(dtAtmbox.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW09(new BigDecimal(dtAtmbox.get(i).get("unknowSum")));
							break;
						}
						case 9: {
							this.getATMResponse().setBOXID10(dtAtmbox.get(i).get("ATMBOX_BOXNO").toString());
							this.getATMResponse().setBOXUNIT10(new BigDecimal(dtAtmbox.get(i).get("ATMBOX_UNIT")));
							this.getATMResponse()
									.setCURID10(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmbox.get(i).get("ATMBOX_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL10(new BigDecimal(dtAtmbox.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT10(new BigDecimal(dtAtmbox.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT10(new BigDecimal(dtAtmbox.get(i).get("presentSum")));
							this.getATMResponse().setREJECT10(new BigDecimal(dtAtmbox.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW10(new BigDecimal(dtAtmbox.get(i).get("unknowSum")));
							break;
						}
						case 10: {
							this.getATMResponse().setBOXID11(dtAtmbox.get(i).get("ATMBOX_BOXNO").toString());
							this.getATMResponse().setBOXUNIT11(new BigDecimal(dtAtmbox.get(i).get("ATMBOX_UNIT")));
							this.getATMResponse()
									.setCURID11(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmbox.get(i).get("ATMBOX_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL11(new BigDecimal(dtAtmbox.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT11(new BigDecimal(dtAtmbox.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT11(new BigDecimal(dtAtmbox.get(i).get("presentSum")));
							this.getATMResponse().setREJECT11(new BigDecimal(dtAtmbox.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW11(new BigDecimal(dtAtmbox.get(i).get("unknowSum")));
							break;
						}
						case 11: {
							this.getATMResponse().setBOXID12(dtAtmbox.get(i).get("ATMBOX_BOXNO").toString());
							this.getATMResponse().setBOXUNIT12(new BigDecimal(dtAtmbox.get(i).get("ATMBOX_UNIT")));
							this.getATMResponse()
									.setCURID12(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmbox.get(i).get("ATMBOX_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL12(new BigDecimal(dtAtmbox.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT12(new BigDecimal(dtAtmbox.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT12(new BigDecimal(dtAtmbox.get(i).get("presentSum")));
							this.getATMResponse().setREJECT12(new BigDecimal(dtAtmbox.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW12(new BigDecimal(dtAtmbox.get(i).get("unknowSum")));
							break;
						}
						case 12: {
							this.getATMResponse().setBOXID13(dtAtmbox.get(i).get("ATMBOX_BOXNO").toString());
							this.getATMResponse().setBOXUNIT13(new BigDecimal(dtAtmbox.get(i).get("ATMBOX_UNIT")));
							this.getATMResponse()
									.setCURID13(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmbox.get(i).get("ATMBOX_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL13(new BigDecimal(dtAtmbox.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT13(new BigDecimal(dtAtmbox.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT13(new BigDecimal(dtAtmbox.get(i).get("presentSum")));
							this.getATMResponse().setREJECT13(new BigDecimal(dtAtmbox.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW13(new BigDecimal(dtAtmbox.get(i).get("unknowSum")));
							break;
						}
						case 13: {
							this.getATMResponse().setBOXID14(dtAtmbox.get(i).get("ATMBOX_BOXNO").toString());
							this.getATMResponse().setBOXUNIT14(new BigDecimal(dtAtmbox.get(i).get("ATMBOX_UNIT")));
							this.getATMResponse()
									.setCURID14(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmbox.get(i).get("ATMBOX_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL14(new BigDecimal(dtAtmbox.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT14(new BigDecimal(dtAtmbox.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT14(new BigDecimal(dtAtmbox.get(i).get("presentSum")));
							this.getATMResponse().setREJECT14(new BigDecimal(dtAtmbox.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW14(new BigDecimal(dtAtmbox.get(i).get("unknowSum")));
							break;
						}
						case 14: {
							this.getATMResponse().setBOXID15(dtAtmbox.get(i).get("ATMBOX_BOXNO").toString());
							this.getATMResponse().setBOXUNIT15(new BigDecimal(dtAtmbox.get(i).get("ATMBOX_UNIT")));
							this.getATMResponse()
									.setCURID15(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmbox.get(i).get("ATMBOX_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL15(new BigDecimal(dtAtmbox.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT15(new BigDecimal(dtAtmbox.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT15(new BigDecimal(dtAtmbox.get(i).get("presentSum")));
							this.getATMResponse().setREJECT15(new BigDecimal(dtAtmbox.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW15(new BigDecimal(dtAtmbox.get(i).get("unknowSum")));
							break;
						}
						}
					}
				}

				if (txCode2 != ATMTXCD.CSH) {
					//--ben-20220922-//this.getATMResponse().setRWTSEQNO(this.getATMRequest().getRWTSEQNO());
					this.getATMResponse().setCOUNT(new BigDecimal(dtAtmbox.size()));
				}
				*/
				dbAtmbox = null;
			}
			case BOK:
			case COX:
			case COH: {
				AtmcoinExtMapper dbAtmcoin = SpringBeanFactoryUtil.getBean(AtmcoinExtMapper.class);
				List<Map<String, String>> dtAtmcoin = null;

				if (StringUtils.isBlank(this.getFeptxn().getFeptxnAtmno())) {
					//--ben-20220922-//dtAtmcoin = dbAtmcoin.GetATMCoinByAtmnoRwtseqnoSettle2(this.getATMRequest().getCRWTSEQNO(), 0);
				} else {
					//--ben-20220922-//dtAtmcoin = dbAtmcoin.GetATMCoinByAtmnoRwtseqnoSettle3(this.getFeptxn().getFeptxnAtmno(),
					//--ben-20220922-//		this.getATMRequest().getCRWTSEQNO(), 0);
				}

				//ben20221118  
				/*
				if (dtAtmcoin.size() > 0) {
					for (int i = 0; i < dtAtmcoin.size(); i++) {
						switch (i) {
						case 0: {
							this.getATMResponse().setBOXID01(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse().setBOXUNIT01(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT")));
							this.getATMResponse()
									.setCURID01(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL01(new BigDecimal(dtAtmcoin.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT01(new BigDecimal(dtAtmcoin.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT01(new BigDecimal(dtAtmcoin.get(i).get("presentSum")));
							this.getATMResponse().setREJECT01(new BigDecimal(dtAtmcoin.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW01(new BigDecimal(dtAtmcoin.get(i).get("unknowSum")));
							break;
						}
						case 1: {
							this.getATMResponse().setBOXID02(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse().setBOXUNIT02(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT")));
							this.getATMResponse()
									.setCURID02(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL02(new BigDecimal(dtAtmcoin.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT02(new BigDecimal(dtAtmcoin.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT02(new BigDecimal(dtAtmcoin.get(i).get("presentSum")));
							this.getATMResponse().setREJECT02(new BigDecimal(dtAtmcoin.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW02(new BigDecimal(dtAtmcoin.get(i).get("unknowSum")));
							break;
						}
						case 2: {
							this.getATMResponse().setBOXID03(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse().setBOXUNIT03(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT")));
							this.getATMResponse()
									.setCURID03(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL03(new BigDecimal(dtAtmcoin.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT03(new BigDecimal(dtAtmcoin.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT03(new BigDecimal(dtAtmcoin.get(i).get("presentSum")));
							this.getATMResponse().setREJECT03(new BigDecimal(dtAtmcoin.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW03(new BigDecimal(dtAtmcoin.get(i).get("unknowSum")));
							break;
						}
						case 3: {
							this.getATMResponse().setBOXID04(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse().setBOXUNIT04(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT")));
							this.getATMResponse()
									.setCURID04(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL04(new BigDecimal(dtAtmcoin.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT04(new BigDecimal(dtAtmcoin.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT04(new BigDecimal(dtAtmcoin.get(i).get("presentSum")));
							this.getATMResponse().setREJECT04(new BigDecimal(dtAtmcoin.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW04(new BigDecimal(dtAtmcoin.get(i).get("unknowSum")));
							break;
						}
						case 4: {
							this.getATMResponse().setBOXID05(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse().setBOXUNIT05(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT")));
							this.getATMResponse()
									.setCURID05(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL05(new BigDecimal(dtAtmcoin.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT05(new BigDecimal(dtAtmcoin.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT05(new BigDecimal(dtAtmcoin.get(i).get("presentSum")));
							this.getATMResponse().setREJECT05(new BigDecimal(dtAtmcoin.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW05(new BigDecimal(dtAtmcoin.get(i).get("unknowSum")));
							break;
						}
						case 5: {
							this.getATMResponse().setBOXID06(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse().setBOXUNIT06(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT")));
							this.getATMResponse()
									.setCURID06(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL06(new BigDecimal(dtAtmcoin.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT06(new BigDecimal(dtAtmcoin.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT06(new BigDecimal(dtAtmcoin.get(i).get("presentSum")));
							this.getATMResponse().setREJECT06(new BigDecimal(dtAtmcoin.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW06(new BigDecimal(dtAtmcoin.get(i).get("unknowSum")));
							break;
						}
						case 6: {
							this.getATMResponse().setBOXID07(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse().setBOXUNIT07(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT")));
							this.getATMResponse()
									.setCURID07(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL07(new BigDecimal(dtAtmcoin.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT07(new BigDecimal(dtAtmcoin.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT07(new BigDecimal(dtAtmcoin.get(i).get("presentSum")));
							this.getATMResponse().setREJECT07(new BigDecimal(dtAtmcoin.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW07(new BigDecimal(dtAtmcoin.get(i).get("unknowSum")));
							break;
						}
						case 7: {
							this.getATMResponse().setBOXID08(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse().setBOXUNIT08(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT")));
							this.getATMResponse()
									.setCURID08(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL08(new BigDecimal(dtAtmcoin.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT08(new BigDecimal(dtAtmcoin.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT08(new BigDecimal(dtAtmcoin.get(i).get("presentSum")));
							this.getATMResponse().setREJECT08(new BigDecimal(dtAtmcoin.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW08(new BigDecimal(dtAtmcoin.get(i).get("unknowSum")));
							break;
						}
						case 8: {
							this.getATMResponse().setBOXID09(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse().setBOXUNIT09(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT")));
							this.getATMResponse()
									.setCURID09(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL09(new BigDecimal(dtAtmcoin.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT09(new BigDecimal(dtAtmcoin.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT09(new BigDecimal(dtAtmcoin.get(i).get("presentSum")));
							this.getATMResponse().setREJECT09(new BigDecimal(dtAtmcoin.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW09(new BigDecimal(dtAtmcoin.get(i).get("unknowSum")));
							break;
						}
						case 9: {
							this.getATMResponse().setBOXID10(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse().setBOXUNIT10(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT")));
							this.getATMResponse()
									.setCURID10(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL10(new BigDecimal(dtAtmcoin.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT10(new BigDecimal(dtAtmcoin.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT10(new BigDecimal(dtAtmcoin.get(i).get("presentSum")));
							this.getATMResponse().setREJECT10(new BigDecimal(dtAtmcoin.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW10(new BigDecimal(dtAtmcoin.get(i).get("unknowSum")));
							break;
						}
						case 10: {
							this.getATMResponse().setBOXID11(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse().setBOXUNIT11(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT")));
							this.getATMResponse()
									.setCURID11(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL11(new BigDecimal(dtAtmcoin.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT11(new BigDecimal(dtAtmcoin.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT11(new BigDecimal(dtAtmcoin.get(i).get("presentSum")));
							this.getATMResponse().setREJECT11(new BigDecimal(dtAtmcoin.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW11(new BigDecimal(dtAtmcoin.get(i).get("unknowSum")));
							break;
						}
						case 11: {
							this.getATMResponse().setBOXID12(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse().setBOXUNIT12(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT")));
							this.getATMResponse()
									.setCURID12(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL12(new BigDecimal(dtAtmcoin.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT12(new BigDecimal(dtAtmcoin.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT12(new BigDecimal(dtAtmcoin.get(i).get("presentSum")));
							this.getATMResponse().setREJECT12(new BigDecimal(dtAtmcoin.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW12(new BigDecimal(dtAtmcoin.get(i).get("unknowSum")));
							break;
						}
						case 12: {
							this.getATMResponse().setBOXID13(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse().setBOXUNIT13(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT")));
							this.getATMResponse()
									.setCURID13(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL13(new BigDecimal(dtAtmcoin.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT13(new BigDecimal(dtAtmcoin.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT13(new BigDecimal(dtAtmcoin.get(i).get("presentSum")));
							this.getATMResponse().setREJECT13(new BigDecimal(dtAtmcoin.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW13(new BigDecimal(dtAtmcoin.get(i).get("unknowSum")));
							break;
						}
						case 13: {
							this.getATMResponse().setBOXID14(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse().setBOXUNIT14(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT")));
							this.getATMResponse()
									.setCURID14(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL14(new BigDecimal(dtAtmcoin.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT14(new BigDecimal(dtAtmcoin.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT14(new BigDecimal(dtAtmcoin.get(i).get("presentSum")));
							this.getATMResponse().setREJECT14(new BigDecimal(dtAtmcoin.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW14(new BigDecimal(dtAtmcoin.get(i).get("unknowSum")));
							break;
						}
						case 14: {
							this.getATMResponse().setBOXID15(dtAtmcoin.get(i).get("ATMCOIN_BOXNO").toString());
							this.getATMResponse().setBOXUNIT15(new BigDecimal(dtAtmcoin.get(i).get("ATMCOIN_UNIT")));
							this.getATMResponse()
									.setCURID15(this.getATMBusiness()
											.getCurrencyByAlpha3(dtAtmcoin.get(i).get("ATMCOIN_CUR").toString())
											.getCurcdCurBsp());
							this.getATMResponse().setREFILL15(new BigDecimal(dtAtmcoin.get(i).get("refillSum")));
							this.getATMResponse().setDEPOSIT15(new BigDecimal(dtAtmcoin.get(i).get("depositSum")));
							this.getATMResponse().setPRESENT15(new BigDecimal(dtAtmcoin.get(i).get("presentSum")));
							this.getATMResponse().setREJECT15(new BigDecimal(dtAtmcoin.get(i).get("rejectSum")));
							this.getATMResponse().setUNKNOW15(new BigDecimal(dtAtmcoin.get(i).get("unknowSum")));
							break;
						}
						}
					}
				}
				*/
				//--ben-20220922-//this.getATMResponse().setCRWTSEQNO(this.getATMRequest().getCRWTSEQNO());
				//ben20221118  this.getATMResponse().setCOUNT(new BigDecimal(dtAtmcoin.size()));
				dtAtmcoin = null;
			}
			default:
				break;
			}
			// 將大class搬至特定電文類別並轉成flatfile
			// 整段switch寫好了，todo 電文檔還沒建立 建好解開就好
			switch (txCode2) {
			case TTS: {
				TTSResponse atmRsp = new TTSResponse();
				atmResponseString = atmRsp.makeMessageFromGeneral(getTxData().getTxObject());
				break;
			}
//			case TTF:{
//				TTFResponse atmRsp = new TTFResponse();
//                atmResponseString = atmRsp.makeMessageFromGeneral(getTxData().getTxObject());
//                break;
//			}
//			case TTI:{
//				TTIResponse atmRsp = new TTIResponse();
//                atmResponseString = atmRsp.makeMessageFromGeneral(getTxData().getTxObject());
//                break;
//			}
//			case TTC:{
//				TTCResponse atmRsp = new TTCResponse();
//                atmResponseString = atmRsp.makeMessageFromGeneral(getTxData().getTxObject());
//                break;
//			}
//			case BOX:{
//				BOXResponse atmRsp = new BOXResponse();
//                atmResponseString = atmRsp.makeMessageFromGeneral(getTxData().getTxObject());
//                break;
//			}
//			case BAK:{
//				BAKResponse atmRsp = new BAKResponse();
//                atmResponseString = atmRsp.makeMessageFromGeneral(getTxData().getTxObject());
//                break;
//			}
//			case CSH:{
//				CSHResponse atmRsp = new CSHResponse();
//                atmResponseString = atmRsp.makeMessageFromGeneral(getTxData().getTxObject());
//                break;
//			}
//			case COX:{
//				COXResponse atmRsp = new COXResponse();
//                atmResponseString = atmRsp.makeMessageFromGeneral(getTxData().getTxObject());
//                break;
//			}
//			case BOX:{
//				BOXResponse atmRsp = new BOXResponse();
//                atmResponseString = atmRsp.makeMessageFromGeneral(getTxData().getTxObject());
//                break;
//			}
//			case COH:{
//				COHResponse atmRsp = new COHResponse();
//                atmResponseString = atmRsp.makeMessageFromGeneral(getTxData().getTxObject());
//                break;
//			}
			}

		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return "";
		}
		return atmResponseString;
	}

	/**
	 * 組ATM回應電文(共同) ''' <summary> ''' 組ATM回應電文(共同) ''' </summary> '''
	 * <remarks>回應電文共同的部分T</remarks>
	 */
	private void loadCommonATMResponse() {
		//ben20221118  
		/*
		ENCHelper encHelper = new ENCHelper(this.getATMBusiness().getFeptxn(), this.getTxData());
		RefString desMACData = new RefString(StringUtils.EMPTY);
		// 2010-06-15 by kyo 新增一個rtncode變數 來承接MAKEMAC是否成功，避免前面有檢核錯誤，但是被MAKEMAC給蓋回NORMAL
		FEPReturnCode rtnCode = FEPReturnCode.Normal;

		this.getATMResponse().setREJCD(this.getATMBusiness().getFeptxn().getFeptxnReplyCode());
		this.getATMResponse().setTXSEQ(this.getATMBusiness().getFeptxn().getFeptxnTxseq());
		//--ben-20220922-//this.getATMResponse().setYYMMDD(this.getATMRequest().getYYMMDD()); // 壓碼日期

		// 跨行轉帳STAN
		if (StringUtils.isNotBlank(this.getATMBusiness().getFeptxn().getFeptxnStan())) {
			this.getATMResponse().setSTAN(this.getATMBusiness().getFeptxn().getFeptxnStan());
		}

		// 手續費
		this.getATMResponse().setHC(this.getATMBusiness().getFeptxn().getFeptxnFeeCustpayAct());

		// 產生ATM電文MAC資料
		if (this.getTxData().getMsgCtl().getMsgctlRepmacType() != null) {
			rtnCode = encHelper.makeAtmMac("", desMACData);
			if (rtnCode != CommonReturnCode.Normal) {
				this.getATMResponse().setMAC(StringUtils.EMPTY);
			} else {
				this.getATMResponse().setMAC(desMACData.get()); // 壓碼訊息
			}

		}

		// BugReport(001B0097):加上絕對值避開金額欄位
		this.getATMResponse()
				.setBal11S(MathUtil.compareTo(this.getATMBusiness().getFeptxn().getFeptxnBala(), 0) >= 0 ? "+" : "-");
		this.getATMResponse().setBAL11(this.getATMBusiness().getFeptxn().getFeptxnBala().abs());

		this.getATMResponse()
				.setBal12S(MathUtil.compareTo(this.getATMBusiness().getFeptxn().getFeptxnBalb(), 0) >= 0 ? "+" : "-");
		this.getATMResponse().setBAL12(this.getATMBusiness().getFeptxn().getFeptxnBalb().abs());

		//--ben-20220922-//this.getATMResponse().setBKNO(this.getATMRequest().getBKNO());
		//--ben-20220922-//this.getATMResponse().setCHACT(this.getATMRequest().getCHACT());
		//--ben-20220922-//this.getATMResponse().setTXACT(this.getATMRequest().getTXACT());
		//--ben-20220922-//this.getATMResponse().setTXAMT(this.getATMRequest().getTXAMT());
		//--ben-20220922-//this.getATMResponse().setTRACK3(this.getATMRequest().getTRACK3());
		this.getATMResponse().setBalCur(this.getFeptxn().getFeptxnTxCurAct()); // 主帳戶幣別
		*/
	}

	private void GetATMCForForeignCurcd() {
		AtmcExtMapper dbfATMC = SpringBeanFactoryUtil.getBean(AtmcExtMapper.class);
		List<Atmc> dtATMC = dbfATMC.GetATMCByAtmnoFiscTbsdySumDRAMT(
				this.getATMBusiness().getAtmStat().getAtmstatLbsdy(),
				this.getATMBusiness().getAtmStat().getAtmstatTbsdy(),
				this.getATMBusiness().getFeptxn().getFeptxnAtmno());
		//ben20221118  
		/*
		if (dtATMC.size() > 0) {
			for (int i = 0; i < dtATMC.size(); i++) {

				// 上營業日
				if (dtATMC.get(i).getAtmcTbsdyFisc().toString()
						.equals(this.getATMBusiness().getAtmStat().getAtmstatLbsdy())) {
					if (dtATMC.get(i).getAtmcCur().toString().equals(CurrencyType.USD.toString())) {
						this.getATMResponse().setLUSDAMT(dtATMC.get(i).getAtmcDrAmt());
						this.getATMResponse().setLUSDCNT(new BigDecimal(dtATMC.get(i).getAtmcDrCnt()));
					} else if (dtATMC.get(i).getAtmcCur().toString().equals(CurrencyType.HKD.toString())) {
						this.getATMResponse().setLHKDAMT(dtATMC.get(i).getAtmcDrAmt());
						this.getATMResponse().setLHKDCNT(new BigDecimal(dtATMC.get(i).getAtmcDrCnt()));
					} else if (dtATMC.get(i).getAtmcCur().toString().equals(CurrencyType.JPY.toString())) {
						this.getATMResponse().setLJPYAMT(dtATMC.get(i).getAtmcDrAmt());
						this.getATMResponse().setLJPYCNT(new BigDecimal(dtATMC.get(i).getAtmcDrCnt()));
					} else if (dtATMC.get(i).getAtmcCur().toString().equals(CurrencyType.CNY.toString())) {
						this.getATMResponse().setLCNYAMT(dtATMC.get(i).getAtmcDrAmt());
						this.getATMResponse().setLCNYCNT(new BigDecimal(dtATMC.get(i).getAtmcDrCnt()));
					}
				}
				// 本營業日
				if (dtATMC.get(i).getAtmcTbsdyFisc().toString().equals(dtATMC)) {
					if (dtATMC.get(i).getAtmcTbsdyFisc().toString()
							.equals(this.getATMBusiness().getAtmStat().getAtmstatLbsdy())) {
						if (dtATMC.get(i).getAtmcCur().toString().equals(CurrencyType.USD.toString())) {
							this.getATMResponse().setTUSDAMT(dtATMC.get(i).getAtmcDrAmt());
							this.getATMResponse().setTUSDCNT(new BigDecimal(dtATMC.get(i).getAtmcDrCnt()));
						} else if (dtATMC.get(i).getAtmcCur().toString().equals(CurrencyType.HKD.toString())) {
							this.getATMResponse().setTHKDAMT(dtATMC.get(i).getAtmcDrAmt());
							this.getATMResponse().setTHKDCNT(new BigDecimal(dtATMC.get(i).getAtmcDrCnt()));
						} else if (dtATMC.get(i).getAtmcCur().toString().equals(CurrencyType.JPY.toString())) {
							this.getATMResponse().setTJPYAMT(dtATMC.get(i).getAtmcDrAmt());
							this.getATMResponse().setTJPYCNT(new BigDecimal(dtATMC.get(i).getAtmcDrCnt()));
						} else if (dtATMC.get(i).getAtmcCur().toString().equals(CurrencyType.CNY.toString())) {
							this.getATMResponse().setTCNYAMT(dtATMC.get(i).getAtmcDrAmt());
							this.getATMResponse().setTCNYCNT(new BigDecimal(dtATMC.get(i).getAtmcDrCnt()));
						}
					}
				}
			}
		}
		*/
	}
	
	
	
}
