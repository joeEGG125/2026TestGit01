package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.AptotExtMapper;
import com.syscom.fep.mybatis.ext.mapper.ClrdtlExtMapper;
import com.syscom.fep.mybatis.ext.mapper.ZoneExtMapper;
import com.syscom.fep.mybatis.mapper.ClrdtltxnMapper;
import com.syscom.fep.mybatis.mapper.ClrtotalMapper;
import com.syscom.fep.mybatis.mapper.FundlogMapper;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.CBSAdapter;
import com.syscom.fep.server.common.adapter.FISCAdapter;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class CLRequestFromFISC extends INBKAABase {
	private String MessageFromIMS = StringUtils.EMPTY;
	private Clrtotal CLRTOTAL = new Clrtotal();
	private ClrtotalMapper dbCLRTOTAL = SpringBeanFactoryUtil.getBean(ClrtotalMapper.class);
	private Fundlog FUNDLOG = new Fundlog();
	private FundlogMapper dbFUNDLOG = SpringBeanFactoryUtil.getBean(FundlogMapper.class);
	private boolean needResponseMsg;
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private FEPReturnCode strFISCRc = CommonReturnCode.Normal;
	private ZoneExtMapper zoneExtMapper = SpringBeanFactoryUtil.getBean(ZoneExtMapper.class);

	public CLRequestFromFISC(FISCData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() {
		try {
			// 財金電文檢核,含拆Bitmap
			_rtnCode = processRequestHeader();

			// 交易記錄初始資料 FEPTXN
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = prepareFeptxn();
			}

			// AddTxData: 新增交易記錄(FEPTXN)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().insertFEPTxn();
			}

			// 檢核訊息押碼(MAC)
			// todo
			if (_rtnCode == CommonReturnCode.Normal && strFISCRc == CommonReturnCode.Normal) {
				_rtnCode = FEPReturnCode.HostResponseTimeout;
				CBSAdapter adapter = new CBSAdapter(this.getTxData());
				adapter.setCbsType(CBSType.FISC);
				adapter.setCbsId(StringUtils.leftPad(this.getTxData().getStan(), 7, '0'));
				if(!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())){
					adapter.setTimeout(0);
					adapter.setMessageToCBS(getTxData().getTxObject().getCLRRequest().getFISCMessage());
					_rtnCode = adapter.sendReceive();
				}else{
					adapter.setMessageToCBS(getTxData().getTxObject().getCLRRequest().getFISCMessage());
					_rtnCode = adapter.sendReceive();
					if(StringUtils.isNotBlank(adapter.getMessageFromCBS())){
						MessageFromIMS = adapter.getMessageFromCBS();
					}
				}

				// 正確拆解之後財金警示訊息,送至 Monitor Console
				if (strFISCRc == CommonReturnCode.Normal) {
					// modified by maxine on 2011/07/07 for 20110621 需顯示成功訊息於EMS
					getLogContext().setProgramFlowType(ProgramFlow.AAIn);
					getLogContext().setProgramName(ProgramName);
					getLogContext().setMessageFlowType(MessageFlow.Response);
					// LogContext.Remark = "CLR 財金警示訊息通知交易 Request"
					// LogMessage(LogLevel.Debug, LogContext)

					sendOKEMS();
				}
			}

			// 判斷是否為 2-WAY 交易，是否送電文到財金
			if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way()) && StringUtils.isBlank(MessageFromIMS)) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(strFISCRc.getValue());
				// modified by Maxine for 9/28 修改, 寫入處理結果
				getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); // AA Complete
				if (strFISCRc != CommonReturnCode.Normal) {
					getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); // 處理結果=Reject
				} else {
					getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 處理結果=成功
				}
				getFiscBusiness().updateTxData();
			}else{
				_rtnCode = updateFEPTXN();
				if (_rtnCode == CommonReturnCode.Normal) {
					getTxData().getTxObject().getCLRResponse().setFISCMessage(MessageFromIMS);
					_rtnCode = sendtoFISC();
				}
			}
			Zone zone =zoneExtMapper.selectByPrimaryKey("TWN");
			if(zone.getZoneTbsdy().compareTo(SysStatus.getPropertyValue().getSysstatTbsdyFisc()) <0
					&& DbHelper.toBoolean(zone.getZoneChgday())){
				_rtnCode = getFiscBusiness().ChangeCBSDate(zone);
			}
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage(getFiscCLRRes().getFISCMessage());
			getTxData().getLogContext().setProgramName(this.aaName);
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logMessage(Level.DEBUG, getLogContext());
		}
		return "";
	}

	public FEPReturnCode sendtoFISC(){
		try {
			FISCAdapter fiscAdapter = new FISCAdapter(getTxData());
			fiscAdapter.setFEPSubSystem(getTxData().getTxSubSystem());
			fiscAdapter.setChannel(getTxData().getTxChannel());
			fiscAdapter.setEj(getTxData().getEj());
			if (getFeptxn() == null) {
				fiscAdapter.setStan(getLogContext().getStan()); // FISCTxData.Stan
			} else {
				if (StringUtils.isBlank(getFeptxn().getFeptxnStan())) {
					fiscAdapter.setStan(getLogContext().getStan()); // FISCTxData.Stan
				} else {
					fiscAdapter.setStan(getFeptxn().getFeptxnStan()); // FISCTxData.Stan
				}
			}
			fiscAdapter.setMessageToFISC(MessageFromIMS);
			fiscAdapter.setNoWait(false);
			_rtnCode = fiscAdapter.sendReceive();
			return _rtnCode;
		}catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendtoFISC"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 財金電文檢核,含拆Bitmap
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode processRequestHeader() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		rtnCode = getFiscBusiness().checkHeader(getFiscCLRReq(), true);

		if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError || rtnCode == FISCReturnCode.STANError
				|| rtnCode == FISCReturnCode.CheckBitMapError || rtnCode == FISCReturnCode.SenderIdError) {
			// 送garble rtmCode 用錯
			getFiscBusiness().sendGarbledMessage(getFiscCLRReq().getEj(), rtnCode, getFiscCLRReq());
			// fiscBusiness.SendGarbledMessage(fiscCLRReq.EJ, _rtnCode, fiscCLRReq)
			return rtnCode;
		}

		if (rtnCode != CommonReturnCode.Normal) {
			strFISCRc = rtnCode;
			return CommonReturnCode.Normal;
		}
		return rtnCode;
	}

	/**
	 * 由財金電文 Body 搬值
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode prepareFeptxn() {
		// 由財金電文 Header 搬值
		_rtnCode = getFiscBusiness().prepareFeptxnFromHeader();
		// 由財金電文 Body 搬值
		if (_rtnCode == CommonReturnCode.Normal) {
			_rtnCode = prepareFeptxnFromBody();
		}

		return _rtnCode;
	}

	/**
	 * 由財金電文 Body 搬值
	 * 
	 * @return
	 * 
	 *         </modify>
	 *         <modifier>Husan</modifier>
	 *         <reason>MSGCTL Schema修改 MSGCTL_2WAY變MSGCTL_FISC_2WAY</reason>
	 *         <date>2010/10/07</date>
	 *         </modify>
	 */
	private FEPReturnCode prepareFeptxnFromBody() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			if (StringUtils.isNotBlank(getFiscCLRReq().getFgAmt())) {
				getFiscBusiness().getFeptxn().setFeptxnTxAmt(new BigDecimal(getFiscCLRReq().getFgAmt())); // 跨行基金增減金額
			}
			if (StringUtils.isNotBlank(getFiscCLRReq().getFundAvail())) {
				getFiscBusiness().getFeptxn().setFeptxnBala(new BigDecimal(getFiscCLRReq().getFundAvail())); // 跨行預留基金可用餘額
			}
			if (StringUtils.isNotBlank(getFiscCLRReq().getFundBal())) {
				// 2015/07/14 Modify by Ruling for 清算電文之跨行預留基金的單位為新台幣元，需乘以100
				getFiscBusiness().getFeptxn().setFeptxnBalb(BigDecimal.valueOf((Double.parseDouble(getFiscCLRReq().getFundBal()) * 100))); // 跨行預留基金
				// .FEPTXN_BALB = CDec(fiscCLRReq.FUND_BAL) '跨行預留基金
			}
			if (StringUtils.isNotBlank(getFiscCLRReq().getFundLowBal())) {
				// 2015/07/14 Modify by Ruling for 清算電文之跨行業務基金低限的單位為新台幣元，需乘以100
				getFiscBusiness().getFeptxn().setFeptxnTxAmtPreauth(BigDecimal.valueOf((Double.parseDouble(getFiscCLRReq().getFundLowBal()) * 100))); // 跨行業務基金低限
			}
			getFiscBusiness().getFeptxn().setFeptxnTroutBkno(getFiscCLRReq().getTroutBkno()); // 被查詢參加單位代號
			getFiscBusiness().getFeptxn().setFeptxnRemark(getFiscCLRReq().getChequeNo()); // 支票號碼
			if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {
				getFiscBusiness().getFeptxn().setFeptxnPending((short) 1);
			}

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".prepareFeptxnFromBody"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 檢核訊息押碼
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode checkMAC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		if (getTxData().getMsgCtl().getMsgctlReqmacType() != null && StringUtils.isNotBlank(getTxData().getMsgCtl().getMsgctlReqmacType().toString())) {
			rtnCode = encHelper.checkFiscMac(getFiscCLRReq().getMessageType(), getFiscCLRReq().getMAC());
			if (rtnCode != CommonReturnCode.Normal) {
				// **訊息押碼錯誤
				needResponseMsg = true;
				return rtnCode;
			}
		}
		return rtnCode;
	}

	/**
	 * 檢核本行帳與財金代收付帳是否帳平及寫入 CLRTOTAL (for 5102)
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode addCLRTOTALData() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		int i = 0;
		BigDecimal sumAmtDR, sumAmtCR;
		AptotExtMapper dbAPTOT = SpringBeanFactoryUtil.getBean(AptotExtMapper.class);
		HashMap<String, Object> ds = new HashMap<>();
		try {
			if ("5102".equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
				/// * Prepare CLRTOTAL */
				RefBase<Clrtotal> clrtotalRefBase = new RefBase<>(CLRTOTAL);
				rtnCode = getFiscBusiness().prepareClrtotal(clrtotalRefBase, MessageFlow.Request);
				CLRTOTAL = clrtotalRefBase.get();
				if (rtnCode != CommonReturnCode.Normal) {
					needResponseMsg = true;
					return rtnCode;
				}
				/// *檢核本行帳與財金代收付帳是否帳平*/
				ds = dbAPTOT.getAPTOTSumAmtByStDate(CLRTOTAL.getClrtotalStDate());
				sumAmtDR = DbHelper.getMapValue(ds, "SumAmtDR", new BigDecimal(0));
				sumAmtCR = DbHelper.getMapValue(ds, "SumAmtCR", new BigDecimal(0));
				if (sumAmtDR.compareTo(CLRTOTAL.getClrtotalSumAmtDr()) != 0 || sumAmtCR.compareTo(CLRTOTAL.getClrtotalSumAmtCr()) != 0) {
					getFiscBusiness().getFeptxn().setFeptxnRepRc("6103");
				} else {
					getFiscBusiness().getFeptxn().setFeptxnRepRc("6102");
				}
				CLRTOTAL.setClrtotalRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());

				/// * Insert CLRTOTAL */
				// 先檢查該筆資料是否存在
				Clrtotal clrtotal = dbCLRTOTAL.selectByPrimaryKey(CLRTOTAL.getClrtotalStDate(), CLRTOTAL.getClrtotalCur(), CLRTOTAL.getClrtotalSource());
				if (clrtotal != null) {
					i = dbCLRTOTAL.updateByPrimaryKeySelective(CLRTOTAL);
				} else {
					i = dbCLRTOTAL.insertSelective(CLRTOTAL);
				}
				if (i < 1) {
					rtnCode = IOReturnCode.InsertFail;
				}
				if (rtnCode != CommonReturnCode.Normal) {
					needResponseMsg = true;
					return rtnCode;
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".addCLRTOTALData"));
			sendEMS(getLogContext());
			rtnCode = IOReturnCode.InsertFail;
		}

		return rtnCode;
	}

	/**
	 * 將跨行基金增減資料寫入跨行基金增減記錄檔FUNDLOG (for 5312 / 5314)
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode addFUNDLOG() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		int i = 0;
		try {
			if ("5312".equals(getFiscBusiness().getFeptxn().getFeptxnPcode()) || "5314".equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
				RefBase<Fundlog> fundlogRefBase = new RefBase<>(FUNDLOG);
				rtnCode = getFiscBusiness().prepareFundlog(fundlogRefBase);
				FUNDLOG = fundlogRefBase.get();
				if (rtnCode != CommonReturnCode.Normal) {
					needResponseMsg = true;
					return rtnCode;
				}
				i = dbFUNDLOG.insertSelective(FUNDLOG);
				if (i < 1) {
					needResponseMsg = true;
					return IOReturnCode.InsertFail;
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".addFUNDLOG"));
			sendEMS(getLogContext());
			rtnCode = IOReturnCode.InsertFail;
		}
		return rtnCode;
	}

	/**
	 * 產生財金回應訊息電文
	 *
	 * @return
	 *
	 */
	private FEPReturnCode prepareResponse() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		if (strFISCRc != CommonReturnCode.Normal) {
			getLogContext().setProgramName(ProgramName);
			getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(strFISCRc, FEPChannel.FISC, getLogContext()));
		}

		// 產生 RESPONSE電文訊息,其格式內容如下:
		rtnCode = getFiscBusiness().prepareHeader("0510");
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// 產生訊息押碼(MAC)
		RefString macRef = new RefString(getFiscCLRRes().getMAC());
		rtnCode = encHelper.makeFiscMac(getFiscCLRRes().getMessageType(), macRef);
		getFiscCLRRes().setMAC(macRef.get());
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// Make Bit Map
		rtnCode = getFiscBusiness().makeBitmap(getFiscCLRRes().getMessageType(), getFiscCLRRes().getProcessingCode(), MessageFlow.Response);
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// 將財金電文轉成
		rtnCode = getFiscCLRRes().makeFISCMsg();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		return rtnCode;
	}

	/**
	 * 更新FEPTXN
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode updateFEPTXN() {
		FEPReturnCode rtnCode2 = null;

		getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // FISC RESPONSE

		getFiscBusiness().getFeptxn().setFeptxnPending((short) 2); // 解除PENDING

		// modified by Maxine for 9/28 修改, 寫入處理結果
		getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); // AA Complete

		if (_rtnCode != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); // 處理結果=Reject
		} else {
			getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 處理結果=成功
		}
		rtnCode2 = getFiscBusiness().updateTxData();
		if (rtnCode2 != CommonReturnCode.Normal) {
			return rtnCode2;
		}

		if(_rtnCode == FEPReturnCode.HostResponseTimeout){
			sendEMS(getLogContext());
		}
		return rtnCode2;
	}

	private void sendOKEMS() {
		LogHelperFactory.getTraceLogger().trace("CLRequestFromFISC.SendOKEMS");
		FEPReturnCode InfoRC = CommonReturnCode.Normal;
		getLogContext().setpCode(getFeptxn().getFeptxnPcode());
		getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
		getLogContext().setMessageGroup("2"); // CLR
		LogHelperFactory.getTraceLogger().trace(StringUtils.join("CLRequestFromFISC.SendOKEMS, FepTxn.FEPTXN_PCODE:", getFeptxn().getFeptxnPcode()));

		// Modify by David Tai on 2011-07-08 for Enum用法錯誤
		// 2015/07/14 Modify by Ruling for EMS增加顯示跨行基金增加減少金額
		if (FISCPCode.PCode5001.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
			getLogContext().setFiscRC(getFeptxn().getFeptxnReqRc());
			InfoRC = FISCReturnCode.FISCAvailableBalanceUnderLimit;
			getLogContext().setMessageParm13("可用餘額:"+getFeptxn().getFeptxnBala()+"基金低限:"+getFeptxn().getFeptxnTxAmtPreauth()
					+"預留基金:"+getFeptxn().getFeptxnBalb());
		} else if (FISCPCode.PCode5312.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
			getLogContext().setFiscRC(getFeptxn().getFeptxnReqRc());
			InfoRC = FISCReturnCode.FISCBalanceIncrement;
			getLogContext().setMessageParm13(StringUtils.join("增加金額:", MathUtil.toString(getFeptxn().getFeptxnTxAmt(), "#,#.00",false)));
			if (!NormalRC.FISC_REQ_RC.equals(getFeptxn().getFeptxnReqRc())) {
				getLogContext().setMessageParm13("(沖正)"); // 沖正RC=0002
			}
		} else if (FISCPCode.PCode5314.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
			getLogContext().setFiscRC(getFeptxn().getFeptxnReqRc());
			getLogContext().setMessageParm13(StringUtils.join("減少金額:", MathUtil.toString(getFeptxn().getFeptxnTxAmt(), "#,#.00",false)));
			InfoRC = FISCReturnCode.FISCBalanceReduce;
		} else if (FISCPCode.PCode5102.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
			getLogContext().setFiscRC(getFeptxn().getFeptxnReqRc());
			InfoRC = FISCReturnCode.FISCCloseBalanceDeliver;
		} else if (FISCPCode.PCode5203.getValueStr().equals(getFeptxn().getFeptxnPcode())){
			getLogContext().setFiscRC(getFeptxn().getFeptxnReqRc());
			InfoRC = FISCReturnCode.FISCCloseBalanceSettle;
		}
		getLogContext().setProgramName(ProgramName);
		LogHelperFactory.getTraceLogger().trace(StringUtils.join("CLRequestFromFISC.SendOKEMS, InfoRC=", InfoRC.getValue()));
		getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(InfoRC, getLogContext()));
		getLogContext().setProgramName(ProgramName);
		logMessage(Level.DEBUG, getLogContext());
	}

	// Fly 2019/10/21 For 跨行餘額內控需求調整
	private void updateInsertCLRDTL() {
		Clrdtl clrdtl = new Clrdtl();
		clrdtl.setClrdtlTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		clrdtl.setClrdtlApId("10000");
		clrdtl.setClrdtlPaytype(" ");
		clrdtl.setClrdtlTime(getFeptxn().getFeptxnReqDatetime().substring(8));
		clrdtl.setClrdtlStan(getFeptxn().getFeptxnStan());
		if (FISCPCode.PCode5001.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
			clrdtl.setClrdtlRmstat("N");
			getLogContext().setRemark("更新CLRDTL_RMSTAT為N");
			logMessage(Level.DEBUG, getLogContext());
		} else if (FISCPCode.PCode5312.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
			clrdtl.setClrdtlRmstat("Y");
			getLogContext().setRemark("更新CLRDTL_RMSTAT為Y");
			logMessage(Level.DEBUG, getLogContext());
		}
		ClrdtlExtMapper dbCLR = SpringBeanFactoryUtil.getBean(ClrdtlExtMapper.class);
		if (dbCLR.updateByPrimaryKeySelective(clrdtl) <= 0) {
			if (dbCLR.insertSelective(clrdtl) <= 0) {
				getLogContext().setRemark(StringUtils.join("UPDATE/INSERT CLRDTL ERROR TXDATE=", clrdtl.getClrdtlTxdate(), " APID=", clrdtl.getClrdtlApId()));
				logMessage(Level.DEBUG, getLogContext());
			}
		}

		Clrdtltxn clrdtltxn = new Clrdtltxn();
		clrdtltxn.setClrdtltxnTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		clrdtltxn.setClrdtltxnEjfno(getFeptxn().getFeptxnEjfno());
		clrdtltxn.setClrdtltxnApId("10000");
		clrdtltxn.setClrdtltxnTime(getFeptxn().getFeptxnReqDatetime().substring(8));
		clrdtltxn.setClrdtltxnStan(getFeptxn().getFeptxnStan());
		clrdtltxn.setClrdtltxnRmstat(clrdtl.getClrdtlRmstat());
		ClrdtltxnMapper clrdtltxnMapper = SpringBeanFactoryUtil.getBean(ClrdtltxnMapper.class);
		if (clrdtltxnMapper.insertSelective(clrdtltxn) <= 0) {
			getLogContext().setRemark(StringUtils.join("INSERT CLRDTLTXN ERROR TXDATE=", clrdtltxn.getClrdtltxnTxdate(), " EJFNO=", clrdtltxn.getClrdtltxnEjfno()));
			logMessage(Level.DEBUG, getLogContext());
		}

	}

}
