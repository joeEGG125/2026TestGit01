package com.syscom.fep.server.aa.inbk;

import com.mchange.lang.IntegerUtils;
import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.INBKData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.InbkpendExtMapper;
import com.syscom.fep.mybatis.ext.mapper.MsgctlExtMapper;
import com.syscom.fep.mybatis.ext.mapper.ZoneExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Inbkpend;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.CBSAdapter;
import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.vo.text.ims.CB_IQTX_I001;
import com.syscom.fep.vo.text.ims.CB_IQTX_O001;
import com.syscom.fep.vo.text.ims.CB_IQTX_O002;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Richard
 */
public class PendingAskFromFISC extends INBKAABase {

	private Inbkpend defINBKPEND;
	private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
	private InbkpendExtMapper dbINBKPEND = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
	private ZoneExtMapper zoneExtMapper = SpringBeanFactoryUtil.getBean(ZoneExtMapper.class);
	private MsgctlExtMapper msgctlExtMapper = SpringBeanFactoryUtil.getBean(MsgctlExtMapper.class);
	private Feptxn Orifeptxn = null;
	private FeptxnExt desFEPTXN = new FeptxnExt();
	private Zone zone = new Zone();

	public PendingAskFromFISC(FISCData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() {
		FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
		try {
			defINBKPEND = new Inbkpend();

			// 1. 檢核財金電文 Header
			_rtnCode = getFiscBusiness().checkHeader(getFiscReq(), false);
			if ("10".equals(StringUtils.substring(Objects.toString(_rtnCode.getValue()), 0, 2))) {
				/* Garbled Message */
				getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), _rtnCode, getFiscReq());
				return StringUtils.EMPTY;
			}

			// 2. @Prepare : 交易記錄初始資料 INBKPEND& FEPTXN
			rtnCode2 = prepareINBKPEND();
			if(rtnCode2 != FEPReturnCode.Normal){
				getLogContext().setProgramName(ProgramName + ".addTxData");
                getLogContext().setMessage(rtnCode2.toString());
                getLogContext().setRemark("prepare INBKPEND 有誤!!");
                sendEMS(getLogContext());
				return StringUtils.EMPTY;
			}

			// 3. @AddTxData: 新增滯留交易記錄(INBKPEND)
			rtnCode2 = addINBKPEND();
			if (rtnCode2 != FEPReturnCode.Normal) {
				getLogContext().setProgramName(ProgramName + ".addTxData");
                getLogContext().setMessage(rtnCode2.toString());
                getLogContext().setRemark("新增 INBKPEND 有誤!!");
                sendEMS(getLogContext());
				return StringUtils.EMPTY;
			}

			if (_rtnCode == FEPReturnCode.Normal) {
				// 4. 檢核訊息押碼(MAC)
				this.checkFISCMACPend();
			}

			if (_rtnCode == FEPReturnCode.Normal) {
				// 5. 檢核原交易記錄 FEPTXN
				_rtnCode = checkFepTxn();
			}
			
			//6. 	label_END_OF_FUNC:

			// 7. 產生財金回應訊息電文
			_rtnCode = prepareForFISC();

			// 8. @更新交易記錄(INBKPEND &FEPTXN)
			this.updateInbkpendAndFeptxn();

			// 9. 送回覆電文到財金(SendToFISC)
			if (_rtnCode == FEPReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);
			}

			// 10. 送 Confirm 電文到財金(for 2270)
			rtnCode2 = sendConfirmToFISC();
			if (_rtnCode == FEPReturnCode.Normal) {
				_rtnCode = rtnCode2;
			}

			//11. 組2130電文到財金(for 2120)
			//2025.8.4 由執行批次Pending2130 改成直接Call AA2130
			if("2120".equals(defINBKPEND.getInbkpendPcode())
					&& !"000".equals(defINBKPEND.getInbkpendOriBkno())
					&& !"0000000".equals(defINBKPEND.getInbkpendOriStan())){
				String INBKPEND_TX_DATE = defINBKPEND.getInbkpendTxDate();
				String INBKPEND_PCODE = "2120"; //檢核是否收到財金未完成交易明細
				String oriBkno = defINBKPEND.getInbkpendOriBkno();
				String oriStan = defINBKPEND.getInbkpendOriStan();
				InbkpendExtMapper inbkpendExtMapper = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
				FeptxnDao _dbFEPTXN = SpringBeanFactoryUtil.getBean(FeptxnDao.class);
				logContext.setMessage("撈取 INBKPEND2120資料，批次參數日期: " + INBKPEND_TX_DATE);
				logMessage(logContext);

				List<Map<String,Object>> INBKPEND2120 = inbkpendExtMapper.getTodayFinishTradeData(INBKPEND_TX_DATE,INBKPEND_PCODE,SysStatus.getPropertyValue().getSysstatFbkno(),oriBkno,oriStan);
				if(null == INBKPEND2120 || INBKPEND2120.size() < 1) {
					logContext.setMessage("今日無2120 PENDING 交易");
					logMessage(logContext);
					return StringUtils.EMPTY;
				}
				INBKPEND_PCODE = "2130"; //檢核是否已傳送交易處理結果給財金
				List<Map<String,Object>> INBKPEND2130 = inbkpendExtMapper.getIsSendTrade(INBKPEND_TX_DATE,INBKPEND_PCODE,INBKPEND2120.get(0).get("INBKPEND_ORI_BKNO").toString(), INBKPEND2120.get(0).get("INBKPEND_ORI_STAN").toString());
				if(null != INBKPEND2130 && !INBKPEND2130.isEmpty()) {
					if("F2".equals(INBKPEND2130.get(0).get("INBKPEND_MSGFLOW").toString())
							&& "0001".equals(INBKPEND2130.get(0).get("INBKPEND_REP_RC").toString())
							&& "0".equals(INBKPEND2130.get(0).get("INBKPEND_AA_RC").toString())) {
						logContext.setMessage("已傳送交易處理結果, 不可再次傳送");
						logMessage(logContext);
					}
				}

				// /* 檢核是否為 3-2已上線PCODE */  2025/01/13
				Feptxn oriFEPTXN = _dbFEPTXN.getFEPTXNMSTRByStan(INBKPEND2120.get(0).get("INBKPEND_ORI_TX_DATE").toString(),INBKPEND2120.get(0).get("INBKPEND_ORI_BKNO").toString(), INBKPEND2120.get(0).get("INBKPEND_ORI_STAN").toString());
				if(null == oriFEPTXN){
					logContext.setMessage(INBKPEND2120.get(0).get("INBKPEND_ORI_TX_DATE").toString()+" : "+INBKPEND2120.get(0).get("INBKPEND_ORI_BKNO").toString()+"-"+INBKPEND2120.get(0).get("INBKPEND_ORI_STAN").toString()+" 查無原交易");
					logMessage(logContext);
					return StringUtils.EMPTY;
				}
				if("Y".equals(oriFEPTXN.getFeptxnCbsProc().toString())){
					logContext.setMessage("分PCode未上線交易，由IMS主機處理");
					logMessage(logContext);
				}else{
					logContext.setMessage("組CALL AA2130 電文內容");
					logMessage(logContext);
					FISCGeneral aData = new FISCGeneral();
					aData.setINBKRequest(new FISC_INBK());
					aData.setSubSystem(FISCSubSystem.INBK);
					aData.getINBKRequest().setProcessingCode("2130");
					aData.getINBKRequest().setMessageType("0200");
					aData.getINBKRequest().setMessageKind(MessageFlow.Request);
					aData.getINBKRequest().setTxAmt(INBKPEND2120.get(0).get("INBKPEND_TX_AMT").toString());
					aData.getINBKRequest().setATMNO(INBKPEND2120.get(0).get("INBKPEND_ATMNO").toString());
                    INBKPEND2120.get(0).putIfAbsent("INBKPEND_ORI_PCODE", "");
					List<Map<String,Object>> callAAData = _dbFEPTXN.getgetCallAa2130Data(
							INBKPEND_TX_DATE.substring(6, 8),
							INBKPEND2120.get(0).get("INBKPEND_ORI_TX_DATE").toString(),
							INBKPEND2120.get(0).get("INBKPEND_ORI_BKNO").toString(),
							INBKPEND2120.get(0).get("INBKPEND_ORI_STAN").toString(),
							INBKPEND2120.get(0).get("INBKPEND_ORI_PCODE").toString()
					);

					if(callAAData.size() != 0) {
						//20220624合庫提供規則 : 有記帳未沖正視為交易成功，未記帳或記帳已沖正視為交易失敗
						//FEPTXN_ACC_TYPE 0:未記帳 1:已記帳 2:已更正 3:更正/轉入失敗 4:未明 5:待解
						BigDecimal accType = (BigDecimal) callAAData.get(0).get("FEPTXN_ACC_TYPE");
						if(accType != null && accType.intValue() == 1) {
							aData.getINBKRequest().setRsCode("00");
						}else {
							aData.getINBKRequest().setRsCode("01");
						}
					}else {
						aData.getINBKRequest().setRsCode("01");
					}

					if(null != INBKPEND2120.get(0).get("INBKPEND_ORI_TX_DATE")) {	//原交易日期
						aData.getINBKRequest().setDueDate(CalendarUtil.adStringToROCString(INBKPEND2120.get(0).get("INBKPEND_ORI_TX_DATE").toString()));
					}
					if(null != INBKPEND2120.get(0).get("INBKPEND_ORI_STAN") && null != INBKPEND2120.get(0).get("INBKPEND_ORI_BKNO")) {
						aData.getINBKRequest().setOriStan(INBKPEND2120.get(0).get("INBKPEND_ORI_BKNO").toString()+StringUtils.leftPad(INBKPEND2120.get(0).get("INBKPEND_ORI_STAN").toString(), 7, '0'));
					}
					if(null != INBKPEND2120.get(0).get("INBKPEND_MAJOR_ACTNO")) {
						aData.getINBKRequest().setICMARK(StringUtils.leftPad(INBKPEND2120.get(0).get("INBKPEND_MAJOR_ACTNO").toString(), 16, ' '));
					}
					if(null != INBKPEND2120.get(0).get("INBKPEND_TROUT_BKNO")) {
						aData.getINBKRequest().setTroutBkno(INBKPEND2120.get(0).get("INBKPEND_TROUT_BKNO").toString());
					}
					if(null != INBKPEND2120.get(0).get("INBKPEND_TROUT_ACTNO")) {
						aData.getINBKRequest().setTroutActno(INBKPEND2120.get(0).get("INBKPEND_TROUT_ACTNO").toString());
					}
					if(null != INBKPEND2120.get(0).get("INBKPEND_TRIN_BKNO")) {
						aData.getINBKRequest().setTrinBkno(INBKPEND2120.get(0).get("INBKPEND_TRIN_BKNO").toString());
					}
					if(null != INBKPEND2120.get(0).get("INBKPEND_TRIN_ACTNO")) {
						aData.getINBKRequest().setTrinActno(INBKPEND2120.get(0).get("INBKPEND_TRIN_ACTNO").toString());
					}

					//暫存入REMARK傳給AA
					String INBKPEND_TRIN_ACTNO = "";

					if( INBKPEND2120.get(0).get("INBKPEND_TRIN_ACTNO") == null) {
						INBKPEND_TRIN_ACTNO = StringUtils.leftPad("", 16, ' ');
					}else {
						INBKPEND_TRIN_ACTNO = StringUtils.leftPad(INBKPEND2120.get(0).get("INBKPEND_TRIN_ACTNO").toString().trim(), 16, ' ');
					}
					logContext.setRemark("INBKPEND_TRIN_ACTNO: " + INBKPEND_TRIN_ACTNO + ", 將INBKRequest.REMARK傳給AA");
					logMessage(logContext);
					if(null != INBKPEND2120.get(0).get("INBKPEND_ORI_PCODE") ) {
						aData.getINBKRequest().setREMARK( INBKPEND_TRIN_ACTNO + StringUtils.leftPad(INBKPEND2120.get(0).get("INBKPEND_ORI_PCODE").toString().trim(), 4, '0'));
					}
					logContext.setMessage("Start to Call AA  by condition = " + INBKPEND2120.get(0).toString());
					logMessage(logContext);
					FEPHandler fepHandler = new FEPHandler();
					fepHandler.dispatch(FEPChannel.FEP, aData);

					// 將AA RC 顯示在UI上
					if (StringUtils.isBlank(aData.getDescription())) {
						logContext.setMessage("無此錯誤訊息!!請洽資訊人員!");
						logMessage(logContext);
						aData.setDescription("無此錯誤訊息!!請洽資訊人員!");
					}
				}
			}
		} catch (Exception ex) {
			_rtnCode = FEPReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName + ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getFiscRes().setResponseCode(MathUtil.toString(BigDecimal.valueOf(_rtnCode.getValue()), "0000"));
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage(getFiscRes().getFISCMessage());
			getTxData().getLogContext().setProgramName(this.aaName);
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
			logMessage(Level.DEBUG, getLogContext());
		}
		return "";
	}

	/**
	 * 交易記錄初始資料 INBKPEND& FEPTXN
	 * 
	 * @return
	 */
	private FEPReturnCode prepareINBKPEND() {
		// PrepareINBKPEND: 跨行延遲交易檔初始資料(INBKPEND)
		try {
			String TX_DATETIME = getFiscReq().getTxnInitiateDateAndTime();
			if (IntegerUtils.parseInt(TX_DATETIME, 0) < 90) {
				defINBKPEND.setInbkpendTxDate(String.valueOf(20110000+Integer.valueOf(StringUtils.substring(TX_DATETIME, 0, 6))));
			} else {
				defINBKPEND.setInbkpendTxDate(String.valueOf(19110000+Integer.valueOf(StringUtils.substring(TX_DATETIME, 0, 6))));
			}
			defINBKPEND.setInbkpendTxTime(StringUtils.substring(TX_DATETIME, 6, 12)); // 交易時間
			defINBKPEND.setInbkpendEjfno(getTxData().getEj()); // 電子日誌序號
			defINBKPEND.setInbkpendStan(getFiscReq().getSystemTraceAuditNo()); // 財金交易序號
			defINBKPEND.setInbkpendBkno(getFiscReq().getTxnSourceInstituteId().substring(0, 3)); // 交易啟動銀行
			defINBKPEND.setInbkpendDesBkno(getFiscReq().getTxnDestinationInstituteId().substring(0, 3)); // 交易接收銀行
			defINBKPEND.setInbkpendAtmno(getFiscReq().getATMNO()); // 櫃員機代號
			defINBKPEND.setInbkpendSubsys(getTxData().getMsgCtl().getMsgctlSubsys()); // 系統別
			defINBKPEND.setInbkpendMsgflow(FEPTxnMessageFlow.FISC_Request); // ‘F1’ (FISC REQUEST)
			defINBKPEND.setInbkpendPcode(getFiscReq().getProcessingCode());
			defINBKPEND.setInbkpendReqRc(getFiscReq().getResponseCode());
			if (StringUtils.isNotBlank(getFiscReq().getTxAmt())) {
				defINBKPEND.setInbkpendTxAmt(new BigDecimal(getFiscReq().getTxAmt())); // 交易金額
			}
			if (StringUtils.isNotBlank(getFiscReq().getOriStan())) { // 原始交易序號
				defINBKPEND.setInbkpendOriBkno(getFiscReq().getOriStan().substring(0, 3));
				defINBKPEND.setInbkpendOriStan(getFiscReq().getOriStan().substring(3, 10));
			}
			if (StringUtils.isNotBlank(getFiscReq().getTroutBkno())) {
				defINBKPEND.setInbkpendTroutbkno(getFiscReq().getTroutBkno().substring(0, 3)); // 原交易存款單位代號
			}
			defINBKPEND.setInbkpendTroutActno(getFiscReq().getTrinActno());
			/* PS:原交易帳號/卡號存於 INBK.TRIN_ACTNO */
			if (StringUtils.isNotBlank(getFiscReq().getDueDate())) {
				/* 若.DUE_DATE 為0 則不需轉西元年 */
				if ("000000".equals(getFiscReq().getDueDate())) {
					defINBKPEND.setInbkpendOriTxDate(StringUtils.repeat( '0',8));
				} else {
					defINBKPEND.setInbkpendOriTxDate(
							CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscReq().getDueDate(), 7, '0'))); // 轉西元年)
				}
			}
			defINBKPEND.setInbkpendPrcResult(getFiscReq().getRsCode()); // 處理結果
			if (StringUtils.isNotBlank(getFiscReq().getCOUNT()))
				defINBKPEND.setInbkpendCount(Integer.parseInt(getFiscReq().getCOUNT())); // 件數
			defINBKPEND.setInbkpendEcInstruction(getFiscReq().getMODE()); // 沖正指示
			/* 2021/1/13 應永豐要求, 增加寫入 FEPTXN */
			String inbkpendTxDate = defINBKPEND.getInbkpendTxDate(); // 交易日期(西元年)
			String inbkpendTxTime = defINBKPEND.getInbkpendTxTime(); // 交易時間
			getFiscBusiness().getFeptxn().setFeptxnTxDate(defINBKPEND.getInbkpendTxDate());// SYSSTM DATE 西元 	06/18 調整同交易日期與時間
			getFiscBusiness().getFeptxn().setFeptxnTxTime(defINBKPEND.getInbkpendTxTime());// SYSTEM TIME 		06/18 調整同交易日期與時間
			getFiscBusiness().getFeptxn().setFeptxnReqDatetime(inbkpendTxDate + inbkpendTxTime);
			getFiscBusiness().getFeptxn().setFeptxnBkno(defINBKPEND.getInbkpendBkno());
			getFiscBusiness().getFeptxn().setFeptxnStan(defINBKPEND.getInbkpendStan());
			/* 2025/5/22 修改 for  2120/2150/2270 還原財金營業日*/
			getFiscBusiness().getFeptxn().setFeptxnTbsdyAct(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc());
			getFiscBusiness().getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
			getFiscBusiness().getFeptxn().setFeptxnAtmno(defINBKPEND.getInbkpendAtmno());
			getFiscBusiness().getFeptxn().setFeptxnTxAmt(defINBKPEND.getInbkpendTxAmt());
			getFiscBusiness().getFeptxn().setFeptxnEjfno(defINBKPEND.getInbkpendEjfno());
			getFiscBusiness().getFeptxn().setFeptxnPcode(defINBKPEND.getInbkpendPcode());
			getFiscBusiness().getFeptxn().setFeptxnReqRc(defINBKPEND.getInbkpendReqRc());
			getFiscBusiness().getFeptxn().setFeptxnSubsys((short) 1);
			getFiscBusiness().getFeptxn().setFeptxnFiscFlag((short) 1);
			// (條件ZONE_CODE = “TWN”)
			zone = zoneExtMapper.selectByPrimaryKey("TWN");
			getFiscBusiness().getFeptxn().setFeptxnTbsdy(zone.getZoneTbsdy());
			getFiscBusiness().getFeptxn().setFeptxnChannel(getFiscBusiness().getFISCTxData().getTxChannel().name()); // 通道別
			getFiscBusiness().getFeptxn().setFeptxnMsgid(getTxData().getMsgCtl().getMsgctlMsgid());
			getFiscBusiness().getFeptxn().setFeptxnTxrust("0");
		} catch (Exception ex) {
			_rtnCode = FEPReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".prepareINBKPEND"));
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
		return FEPReturnCode.Normal;
	}

	/**
	 * 新增滯留交易記錄(INBKPEND & FEPTXN)
	 * 
	 * @return
	 * @throws Exception
	 * 
	 */
	private FEPReturnCode addINBKPEND() throws Exception {
		if (dbINBKPEND.insertSelective(defINBKPEND) < 1) {
			return IOReturnCode.INBKPENDInsertError;
		}
		/* 2021/1/13 應永豐要求, 增加寫入 FEPTXN */
		return getFiscBusiness().insertFEPTxn();
	}

	/**
	 * 檢核訊息押碼(MAC)
	 * 
	 */
	private void checkFISCMACPend() {
		/* Prepare FEPTXN for DES, pls new FEPTXN just for DES */
		desFEPTXN.setFeptxnTxDate(defINBKPEND.getInbkpendTxDate());
		desFEPTXN.setFeptxnBkno(defINBKPEND.getInbkpendBkno());
		desFEPTXN.setFeptxnStan(defINBKPEND.getInbkpendStan());
		desFEPTXN.setFeptxnDesBkno(defINBKPEND.getInbkpendOriBkno());
		desFEPTXN.setFeptxnOriStan(defINBKPEND.getInbkpendOriStan());
		if(StringUtils.isNotBlank(defINBKPEND.getInbkpendOriTxDate())) {
			desFEPTXN.setFeptxnTbsdyFisc(defINBKPEND.getInbkpendOriTxDate());
		}
		desFEPTXN.setFeptxnRsCode(defINBKPEND.getInbkpendPrcResult());
		desFEPTXN.setFeptxnRemark(defINBKPEND.getInbkpendEcInstruction());
		desFEPTXN.setFeptxnReqRc(defINBKPEND.getInbkpendReqRc());
		ENCHelper encHelper = new ENCHelper(desFEPTXN, getTxData());
		FEPReturnCode rtnCode = encHelper.checkFISCMACPend(getFiscReq().getMessageType().substring(2, 4), getFiscReq().getMAC());
		if (rtnCode != FEPReturnCode.Normal) {
			_rtnCode = FEPReturnCode.ENCCheckMACError; /* 訊息押碼錯誤 */
		}
	}

	/**
	 * 檢核原交易記錄 FEPTXN
	 * 
	 * @return
	 */
	private FEPReturnCode checkFepTxn() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		String qryimsflag = "0";
		try {
			String pCode = getFiscReq().getProcessingCode();
			String dueDate = getFiscReq().getDueDate();
			//for select
			String oriStan = getFiscReq().getOriStan();
			String stan = null;
			String bkno = null;
			String txDate = null;
			if (StringUtils.isNotBlank(oriStan)) {
				bkno = oriStan.substring(0, 3);
				stan = oriStan.substring(3, 10);
				txDate = CalendarUtil.rocStringToADString(StringUtils.leftPad(String.valueOf(dueDate), 7, '0')); //民國轉西元
			}

			if ("2270".equals(pCode)) {
				String tbsdyfisc = SysStatus.getPropertyValue().getSysstatTbsdyFisc();/*本營業日*/
				Orifeptxn = feptxnDao.getoriFEPTXNData2270(tbsdyfisc, bkno, stan);
				if (Orifeptxn == null) {
					 /* 往前找上營業日 */
					tbsdyfisc = SysStatus.getPropertyValue().getSysstatLbsdyFisc();/*前營業日*/
					Orifeptxn = feptxnDao.getoriFEPTXNData2270(tbsdyfisc, bkno, stan);
					if (Orifeptxn == null) {
						logContext.setProgramName(ProgramName + ".checkFepTxn");
						logContext.setRemark("2270找無原交易, 財金營業日=" + tbsdyfisc + ",發動行=" + bkno + ",交易序號=" + stan);
						logMessage(logContext);
						return FISCReturnCode.TransactionNotFound; /* 4701:無此交易 */
					} else {
						// 2014/05/16 Modify by Ruling for
						// 修正測試整批轉即時發現的問題：找前一營業日的DBFEPTXN要同步到Business否則無法更新原交易
						getFiscBusiness().setFeptxnDao(feptxnDao);
					}
				}
				if (FEPTxnMessageFlow.FISC_Request.equals(Orifeptxn.getFeptxnMsgflow())) { // "F1"
					logContext.setProgramName(ProgramName + ".checkFepTxn");
					logContext.setRemark("2270 原交易, 財金營業日=" + tbsdyfisc + ",發動行=" + bkno + ",交易序號=" + stan + ",FEPTXN_MSGFLOW = ‘F1’, 交易狀態不明");
					logMessage(logContext);
					return FISCReturnCode.ProtocalError; /* 4702:交易狀態不明或 Protocal 錯誤 */
				}

				/* 5/16 修改 FOR 2270, 將原交易資料回寫 INBKPEND */
				defINBKPEND.setInbkpendTroutbkno(Orifeptxn.getFeptxnTroutBkno());
				defINBKPEND.setInbkpendTroutActno(Orifeptxn.getFeptxnTroutActno());
				defINBKPEND.setInbkpendMajorActno(Orifeptxn.getFeptxnMajorActno());
				defINBKPEND.setInbkpendTrinBkno(Orifeptxn.getFeptxnTrinBkno());
				defINBKPEND.setInbkpendTrinActno(Orifeptxn.getFeptxnTrinActno());
				defINBKPEND.setInbkpendTrinActnoActual(Orifeptxn.getFeptxnTrinActnoActual());
				defINBKPEND.setInbkpendFiscTimeout(Orifeptxn.getFeptxnFiscTimeout());
				defINBKPEND.setInbkpendTxAmt(Orifeptxn.getFeptxnTxAmt());
				/* 1/9 修改 */
				defINBKPEND.setInbkpendOriTxDate(Orifeptxn.getFeptxnTxDate());
				defINBKPEND.setInbkpendOriPcode(Orifeptxn.getFeptxnPcode());
				defINBKPEND.setInbkpendOriTbsdyFisc(Orifeptxn.getFeptxnTbsdyFisc());
				defINBKPEND.setInbkpendOriEjfno(Orifeptxn.getFeptxnEjfno());
				defINBKPEND.setInbkpendOriReqRc(Orifeptxn.getFeptxnReqRc());
				defINBKPEND.setInbkpendOriRepRc(Orifeptxn.getFeptxnRepRc());
				defINBKPEND.setInbkpendOriConRc(Orifeptxn.getFeptxnConRc());
				if (Orifeptxn.getFeptxnClrType() == 1) {
					defINBKPEND.setInbkpendOriTxFlag(DbHelper.toShort(true)); /* 己更新跨行代收付 */
				}
				if ("A".equals(Orifeptxn.getFeptxnTxrust())) { /* 成功 */
					defINBKPEND.setInbkpendPrcResult("00"); /* 處理結果=成功 */
				} else {
					defINBKPEND.setInbkpendPrcResult("01"); /* 處理結果=失敗 */
					qryimsflag = "1";
				}
			} else if ("2120".equals(pCode) && Double.parseDouble(getFiscReq().getCOUNT()) != 0) {
				Orifeptxn = feptxnDao.getFeptxnByStan(txDate, bkno, stan);
				if (Orifeptxn == null) {
					logContext.setProgramName(ProgramName + ".checkFepTxn");
					logContext.setRemark("2120找無原交易, 交易日=" + txDate + ",交易發動行=" + bkno + ",交易序號=" + stan);
					logMessage(logContext);
					return FISCReturnCode.TransactionNotFound; /* 4701:無此交易 */
				}

				//以下比對邏輯請註記先不刪除
//				/* 因為送財金之 ATMNO 已右補0, 故不再判斷ATMNO 是否相同 */
//				if (ATMTXCD.US.toString().equals(Orifeptxn.getFeptxnTxCode()) ||
//						ATMTXCD.JP.toString().equals(Orifeptxn.getFeptxnTxCode())) {
//					/* 跨行提領外幣交易(US/JP) */
//					if (!Orifeptxn.getFeptxnTxDate().equals(
//							CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscReq().getDueDate(), 7, '0')))
//							|| Orifeptxn.getFeptxnTxAmtAct().doubleValue() != Double.parseDouble(getFiscReq().getTxAmt())
//							|| !getFiscReq().getTroutBkno().substring(0, 3).equals(Orifeptxn.getFeptxnTroutBkno())
//							|| !getFiscReq().getTrinActno().trim().equals(StringUtils.trim(Orifeptxn.getFeptxnMajorActno()))) {
//						/* PS:原交易帳號存於 ReqINBK.TRIN_ACTNO */
//						return FISCReturnCode.TransactionNotFound; /* 4701:無此交易 */
//					}
//				} else if (ATMTXCD.DA.toString().equals(Orifeptxn.getFeptxnTxCode()) ||
//						ATMTXCD.DC.toString().equals(Orifeptxn.getFeptxnTxCode())) {
//					/* 跨行台幣存款交易 */
//					if (!Orifeptxn.getFeptxnTxDate().equals(
//							CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscReq().getDueDate(), 7, '0')))
//							|| Orifeptxn.getFeptxnTxAmt().doubleValue() != Double.parseDouble(getFiscReq().getTxAmt())
//							|| !getFiscReq().getTroutBkno().substring(0, 3).equals(Orifeptxn.getFeptxnTroutBkno7().substring(0, 3))
//							|| !getFiscReq().getTrinActno().trim().equals(StringUtils.trim(Orifeptxn.getFeptxnMajorActno()))) {
//						/* PS:原交易帳號存於 ReqINBK.TRIN_ACTNO */
//						return FISCReturnCode.TransactionNotFound; /* 4701:無此交易 */
//					}
//				} else {
//					/* 跨行台幣交易 */
//					if (!Orifeptxn.getFeptxnTxDate().equals(
//							CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscReq().getDueDate(), 7, '0')))
//							|| Orifeptxn.getFeptxnTxAmt().doubleValue() != Double.parseDouble(getFiscReq().getTxAmt())
//							|| !getFiscReq().getTroutBkno().substring(0, 3).equals(Orifeptxn.getFeptxnTroutBkno())
//							|| !getFiscReq().getTrinActno().trim().equals(StringUtils.trim(Orifeptxn.getFeptxnMajorActno()))) {
//						/* PS:原交易帳號存於 ReqINBK.TRIN_ACTNO */
//						return FISCReturnCode.TransactionNotFound; /* 4701:無此交易 */
//					}
//				}
				defINBKPEND.setInbkpendMajorActno(Orifeptxn.getFeptxnMajorActno());
				defINBKPEND.setInbkpendTrinBkno(Orifeptxn.getFeptxnTrinBkno());
				defINBKPEND.setInbkpendTrinActno(Orifeptxn.getFeptxnTrinActno());
				defINBKPEND.setInbkpendTrinActnoActual(Orifeptxn.getFeptxnTrinActnoActual());
				defINBKPEND.setInbkpendOriPcode(Orifeptxn.getFeptxnPcode());
				defINBKPEND.setInbkpendOriTbsdyFisc(Orifeptxn.getFeptxnTbsdyFisc());
				defINBKPEND.setInbkpendOriEjfno(Orifeptxn.getFeptxnEjfno());
				defINBKPEND.setInbkpendOriReqRc(Orifeptxn.getFeptxnReqRc());
				defINBKPEND.setInbkpendOriRepRc(Orifeptxn.getFeptxnRepRc());
				defINBKPEND.setInbkpendOriConRc(Orifeptxn.getFeptxnConRc());
				defINBKPEND.setInbkpendFiscTimeout(Orifeptxn.getFeptxnFiscTimeout());
				if (Orifeptxn.getFeptxnClrType() == 1) {
					defINBKPEND.setInbkpendOriTxFlag(DbHelper.toShort(true)); /* 己更新跨行代收付 */
				}
				if ("A".equals(Orifeptxn.getFeptxnTxrust())) { /* 成功 */
					defINBKPEND.setInbkpendPrcResult("00"); /* 處理結果=成功 */
				} else {
					defINBKPEND.setInbkpendPrcResult("01"); /* 處理結果=失敗 */
					qryimsflag = "1";
				}
			}

			/* 6/9 新增 */
			if("1".equals(qryimsflag)){  /* 查詢主機的交易狀態 */
				// 上送查詢電文
				String tita = this.makeCBSTita(Orifeptxn);
				logContext.setProgramName(ProgramName + ".getTransationStatus");
				logContext.setMessage("tita:" + tita);
				logContext.setRemark("tita");
				logMessage(logContext);

				// 送IMS
				CBSAdapter adapter = new CBSAdapter(new INBKData());
				adapter.setCbsType(CBSType.CBS);
				adapter.setCbsId(StringUtils.leftPad(Orifeptxn.getFeptxnEjfno().toString(), 8, '0'));

				adapter.setMessageToCBS(tita);
				rtnCode = adapter.sendReceive();
				logContext.setProgramName(ProgramName + ".checkFepTxn");
				logContext.setRemark("after send To CBS, RC:" + rtnCode.toString());
				logMessage(logContext);

				//拆解電文
				Object tota = null;
				tota = parseCbsTota(adapter.getMessageFromCBS());

				logContext.setProgramName(ProgramName + ".getTransationStatus");
				logContext.setMessage("tota:" + tota);
				logContext.setRemark("tota");
				logMessage(logContext);
				
				if(rtnCode == FEPReturnCode.Normal && tota != null){
					String TOTA_OUTRTC = this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue());
					String TOTA_IMS_PENDING = this.getImsPropertiesValue(tota, ImsMethodName.IMS_PENDING.getValue());
					String TOTA_IMS_RC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC4_FISC.getValue());
					String TOTA_TX_RVS_FLAG = this.getImsPropertiesValue(tota, ImsMethodName.TX_RVS_FLAG.getValue());
					String TOTA_TX_ACCT_FLAG = this.getImsPropertiesValue(tota, ImsMethodName.TX_ACCT_FLAG.getValue());

					if ( StringUtils.equals("4001", TOTA_OUTRTC) ){  //主機查詢成功
						if ( StringUtils.equals("2270",pCode) && StringUtils.equals("N",TOTA_IMS_PENDING ) ) {  //交易已結束
							getFiscBusiness().getFeptxn().setFeptxnConRc( TOTA_IMS_RC4_FISC );
						}else if ( StringUtils.equals("2120",pCode) ) {
							getFiscBusiness().getFeptxn().setFeptxnConRc( TOTA_IMS_RC4_FISC );
						}

						if ( getFiscBusiness().getFeptxn().getFeptxnConRc() != null ) {
							getFiscBusiness().getFeptxn().setFeptxnPending((short) 2);		//解除PENDING
							if ( StringUtils.equals( NormalRC.FISC_ATM_OK, getFiscBusiness().getFeptxn().getFeptxnConRc()) ) {
								defINBKPEND.setInbkpendPrcResult("00"); /* 處理結果=成功 */
								getFiscBusiness().getFeptxn().setFeptxnTxrust("A");
							}else{
								getFiscBusiness().getFeptxn().setFeptxnTxrust("R");
							}
							if ( StringUtils.equals( "Y", TOTA_TX_RVS_FLAG) ){	//已沖正
								getFiscBusiness().getFeptxn().setFeptxnAccType((short) 2);
							}else if ( StringUtils.equals( "Y", TOTA_TX_ACCT_FLAG) ) {	//已記帳
								getFiscBusiness().getFeptxn().setFeptxnAccType((short) 1);
							}
						}
					}
				}
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkFepTxn"));
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	/**
	 * 產生財金回應訊息電文
	 * 
	 * @return
	 */
	private FEPReturnCode prepareForFISC() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		ENCHelper encHelper = null;
		try {
			// (1) 判斷 FEPReturnCod
			if (_rtnCode != FEPReturnCode.Normal) {
				getLogContext().setProgramName(ProgramName);
				defINBKPEND.setInbkpendRepRc(TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext()));
			} else {
				defINBKPEND.setInbkpendRepRc(NormalRC.FISC_OK);
			}
			// (2) 產生 RESPONSE (CDPF02, CDHI02, CDHI08)電文訊息,其格式內容如下:
			// Header (RepHEAD):
			rtnCode = getFiscBusiness().preparePendHeader("0210", defINBKPEND);
			if (rtnCode != FEPReturnCode.Normal) {
				return rtnCode;
			}
			// Application Data Elements(RepINBK) :
			if ("2120".equals(getFiscReq().getProcessingCode()) || "2150".equals(getFiscReq().getProcessingCode())) {
				getFiscRes().setDueDate(getFiscReq().getDueDate());
			}
			getFiscRes().setOriStan(getFiscReq().getOriStan());

			// (3) 產生訊息押碼(MAC)
			/* Prepare FEPTXN for DES */
			desFEPTXN.setFeptxnTxDate(defINBKPEND.getInbkpendTxDate());
			desFEPTXN.setFeptxnBkno(defINBKPEND.getInbkpendBkno());
			desFEPTXN.setFeptxnStan(defINBKPEND.getInbkpendStan());
			desFEPTXN.setFeptxnDesBkno(defINBKPEND.getInbkpendOriBkno());
			desFEPTXN.setFeptxnOriStan(defINBKPEND.getInbkpendOriStan());
			desFEPTXN.setFeptxnTbsdyFisc(defINBKPEND.getInbkpendOriTxDate());
			desFEPTXN.setFeptxnRepRc(defINBKPEND.getInbkpendRepRc());
			encHelper = new ENCHelper(desFEPTXN, getTxData());
			RefString refString = new RefString();
			rtnCode = encHelper.makeFISCMACPend(getFiscRes().getMessageType().substring(2, 4), refString);
			getFiscRes().setMAC(refString.get());
			if (rtnCode != FEPReturnCode.Normal) {
				getFiscRes().setMAC("00000000");
			}

			// (4) Make Bit Map
			rtnCode = getFiscBusiness().makeBitmap(getFiscRes().getMessageType(), getFiscRes().getProcessingCode(),
					MessageFlow.Response);
			if (rtnCode != FEPReturnCode.Normal) {
				getFiscRes().setBitMapConfiguration(StringUtils.leftPad("0", 16, '0'));
			}

			_rtnCode=getFiscRes().makeFISCMsg();

			// (5) SendEMS
			if (("2120".equals(defINBKPEND.getInbkpendPcode()) || "2150".equals(defINBKPEND.getInbkpendPcode()))
					&& NormalRC.FISC_OK.equals(defINBKPEND.getInbkpendRepRc())) {
				// 20110621 需顯示成功訊息於EMS, 為此LogData新增欄位如下
				FEPReturnCode InfoRC = null;
				getLogContext().setpCode(defINBKPEND.getInbkpendPcode());
				getLogContext().setDesBkno(defINBKPEND.getInbkpendDesBkno());
				getLogContext().setFiscRC(NormalRC.FISC_OK);
				getLogContext().setMessageGroup("3"); // INBK
				if (defINBKPEND.getInbkpendPcode().equals("2120")) {
					InfoRC = FISCReturnCode.FISCUnfinishedTransaction;
				} else {
					InfoRC = FISCReturnCode.FISCErrorCorrectionResult;
				}
				getLogContext().setMessageParm13(getFiscReq().getCOUNT()); /* 筆數 */
				getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(InfoRC, getLogContext()));
				/* 由GetMessageFromFEPReturnCode執行 SendEMS */
				sendEMS(Level.INFO, getLogContext());
			}
			return FEPReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".prepareForFISC"));
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	/**
	 * 更新交易記錄(INBKPEND &FEPTXN)
	 * 
	 * @throws Exception
	 */
	private void updateInbkpendAndFeptxn() throws Exception {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			defINBKPEND.setInbkpendAaRc(String.valueOf(_rtnCode.getValue())); 
			defINBKPEND.setInbkpendMsgflow(FEPTxnMessageFlow.FISC_Response); // ‘F2’(FISC RESPONSE)
			defINBKPEND.setInbkpendPending((short) 2); // (解除PENDING)
			int updCount = dbINBKPEND.updateByPrimaryKeySelective(defINBKPEND);
			if (updCount < 1) {
				return;
			}
			/* 2021/1/13 應永豐要求, 增加寫入 FEPTXN */
			getFiscBusiness().getFeptxn().setFeptxnPending(defINBKPEND.getInbkpendPending());
			getFiscBusiness().getFeptxn().setFeptxnMsgflow(defINBKPEND.getInbkpendMsgflow());
			getFiscBusiness().getFeptxn().setFeptxnFiscTimeout(defINBKPEND.getInbkpendFiscTimeout());
			getFiscBusiness().getFeptxn().setFeptxnRepRc(defINBKPEND.getInbkpendRepRc());
			getFiscBusiness().getFeptxn().setFeptxnAaRc(IntegerUtils.parseInt(defINBKPEND.getInbkpendAaRc(), 0));
			/* 2025/5/22 修改 for  2120/2150/2270 還原財金營業日 */
			getFiscBusiness().getFeptxn().setFeptxnTbsdyFisc(feptxn.getFeptxnTbsdyAct());
			/* 9/28 修改, 寫入處理結果 */
			getFiscBusiness().getFeptxn().setFeptxnAaComplete((short) 1); /* AA Complete */
			if ("0001".equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); /* 處理結果=成功 */
			} else {
				getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); /* 處理結果=Reject */
			}
			
			rtnCode = getFiscBusiness().updateTxData();
			if (rtnCode != FEPReturnCode.Normal ) {
				return;
			}
			transactionManager.commit(txStatus);
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "updateInbkpendAndFeptxn");
			sendEMS(getLogContext());
		}finally {
			if ( !txStatus.isCompleted()) {
				transactionManager.rollback(txStatus);
			}
		}
	}

	/**
	 * 送 Confirm 電文到財金(for 2270)
	 * 
	 * @return
	 */
	private FEPReturnCode sendConfirmToFISC() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		try {
			if ("2270".equals(defINBKPEND.getInbkpendPcode())
					&& NormalRC.FISC_OK.equals(defINBKPEND.getInbkpendRepRc())) {
				if (feptxn != null) {
					logContext.setProgramName(ProgramName + ".sendConfirmToFISC");
					logContext.setRemark("sendConfirmToFISC, ConRc=" + defINBKPEND.getInbkpendOriConRc() + ",Timeout=" + DbHelper.toBoolean(defINBKPEND.getInbkpendFiscTimeout()));
					logMessage(logContext);

					getFiscBusiness().setFeptxn(Orifeptxn);
					getFiscBusiness().getFISCTxData().setMsgCtl(getMsgCtl(Orifeptxn.getFeptxnMsgid()));
					if (StringUtils.isNotBlank(defINBKPEND.getInbkpendOriConRc())) {
						rtnCode = getFiscBusiness().sendConfirmToFISC();
					} else if (DbHelper.toBoolean(defINBKPEND.getInbkpendFiscTimeout())) {
						getLogContext().setProgramName(ProgramName);
						getFiscBusiness().getFeptxn().setFeptxnConRc("0601"); //TIMEOUT
						getFiscBusiness().getFeptxn().setFeptxnPending((short) 2); //解除 PENDING
						getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); //Reverse
						rtnCode = getFiscBusiness().sendConfirmToFISC();
					}
				}
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendConfirmToFISC"));
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}

	}
	/**
	 * 查詢主機的交易狀態,組cbstita(qryimsflag == "1")
	 *
	 * @return
	 */
	public String makeCBSTita(Feptxn orifeptxn) throws Exception {
		CB_IQTX_I001 cbsTita = new CB_IQTX_I001();
		//header
		cbsTita.setINTRAN("EAII");
		String MSGID = StringUtils.rightPad(orifeptxn.getFeptxnTxDate() + orifeptxn.getFeptxnTxTime() + StringUtils.leftPad(String.valueOf(orifeptxn.getFeptxnEjfno()), 8, "0"), 24, " ");
		cbsTita.setINMSGID(MSGID);
		cbsTita.setINDATE(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		cbsTita.setINTIME(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
		cbsTita.setINSERV("FEP1");
		cbsTita.setMQIDCNV("N");    // 讓主機回送FEP上送的cbsid
		cbsTita.setINTD("910A0037");
		cbsTita.setINAP("ATFEP");
		cbsTita.setINFF("F");
		cbsTita.setINPGNO("001");
		cbsTita.setINV1CT("0000");

		//body
		cbsTita.setINQ_ACQ_BID(orifeptxn.getFeptxnBkno());
		cbsTita.setINQ_TX_STAN(orifeptxn.getFeptxnStan());
		cbsTita.setINQ_FG_TXDATE(CalendarUtil.adStringToROCString(orifeptxn.getFeptxnTbsdyFisc())); //轉民國年

		String tita = cbsTita.makeMessage();
		String AsciiTita = cbsTita.makeMessageAscii();

		//LOG
		logContext.setProgramName(ProgramName + ".makeCBSTita");
		logContext.setMessage("MessageToCBS:" + tita);
		logContext.setRemark("MessageToCBS");
		logMessage(logContext);

		logContext.setProgramName(ProgramName + ".makeCBSTita");
		logContext.setMessage("Ascii MessageToCBS:" + AsciiTita);
		logContext.setRemark("Ascii MessageToCBS");
		logMessage(logContext);

		return tita;
	}

	/**
	 * 查詢主機的交易狀態, 解 cbstota(qryimsflag == "1")
	 *
	 * @return
	 */
	public Object parseCbsTota(String message) throws Exception {
		if (StringUtils.isNotBlank(message)) {
			boolean is4001 = false;
			String TotaStr = StringUtils.EMPTY;
			String AsciiTotaStr = StringUtils.EMPTY;
			CB_IQTX_O001 tota = new CB_IQTX_O001();
			CB_IQTX_O002 tota2 = new CB_IQTX_O002();
			String OUTRTC = message.substring(176, 184);
			logContext.setRemark("OUTRTC:" + OUTRTC);
			logMessage(logContext);
			if (StringUtils.equals("F4F0F0F1", OUTRTC)) {
				tota.parseCbsTele(message);
				TotaStr = tota.makeMessage();
				AsciiTotaStr = tota.makeMessageAscii();
				is4001 = true;
			} else {
				tota2.parseCbsTele(message);
				TotaStr = tota2.makeMessage();
				AsciiTotaStr = tota2.makeMessageAscii();
			}
			logContext.setRemark("MessageFromCBS");
			logContext.setMessage("MessageFromCBS:" + TotaStr);
			logMessage(logContext);
			logContext.setRemark("Ascii MessageFromCBS");
			logContext.setMessage("Ascii MessageFromCBS:" + AsciiTotaStr);
			logMessage(logContext);
			if (is4001) {
				return tota;
			} else {
				return tota2;
			}
		}
		return null;
	}

	public Msgctl getMsgCtl(String msgId){
		return msgctlExtMapper.selectByPrimaryKey(msgId);
	}
}
