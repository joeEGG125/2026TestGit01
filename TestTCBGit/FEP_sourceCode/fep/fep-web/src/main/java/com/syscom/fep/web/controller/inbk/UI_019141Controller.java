package com.syscom.fep.web.controller.inbk;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019141_Form;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.ReserveHandler;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 預約跨轉單筆重發處理
 *
 * @author Kai
 */
@Controller
public class UI_019141Controller extends BaseController {
	Fwdrst tempFWDRST = new Fwdrst();
	int EJ = 0;

	@Autowired
	public InbkService inbkService;

	@Override
	public void pageOnLoad(ModelMap mode) {
		UI_019141_Form form = new UI_019141_Form();
		form.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
		mode.addAttribute("lblFail", "0");
		mode.addAttribute("lblFailSys", "0");
		mode.addAttribute("lblFailCust", "0");
		mode.addAttribute("lblFailOther", "0");
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/inbk/UI_019141/getFwdtxns", produces = "application/json;charset=utf-8")
	public String getFwdtxns(@ModelAttribute UI_019141_Form form, ModelMap mode) {
		this.infoMessage("查詢主檔數據, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		this.checkParameters(form, mode);
		return Router.UI_019141.getView();
	}

	@PostMapping(value = "/inbk/UI_019141/resultGrdvRowCommand", produces = "application/json;charset=utf-8")
	public String resultGrdvRowCommands(@ModelAttribute UI_019141_Form form, ModelMap mode) {
		this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		this.resultGrdvRowCommand(form, mode);
		return Router.UI_019141.getView();
	}

	public void checkParameters(UI_019141_Form form, ModelMap mode) {
		if (StringUtils.isBlank(form.getTxTroutActno())) {
			this.showMessage(mode, MessageType.DANGER, "轉出帳號未輸入");
			return;
		}
		if (StringUtils.isBlank(form.getTxTrinBkno())) {
			this.showMessage(mode, MessageType.DANGER, "轉入行未輸入");
			return;
		}
		if (StringUtils.isBlank(form.getTxTrinActno())) {
			this.showMessage(mode, MessageType.DANGER, "轉入帳號未輸入");
			return;
		}
		if (StringUtils.isBlank(form.getTxtTxAmt())) {
			this.showMessage(mode, MessageType.DANGER, "交易金額未輸入");
			return;
		}
		cleanLbl(mode);
		bindGridData(form, mode);

	}

	private void bindGridData(UI_019141_Form form, ModelMap mode) {
		try {
			Fwdtxn def = new Fwdtxn();
			def.setFwdtxnTxDate(form.getTxDate().replace("-", ""));
			if (StringUtils.isNotBlank(form.getTxTroutActno())) {
				def.setFwdtxnTroutActno(StringUtils.leftPad(form.getTxTroutActno(), 16, '0'));
			}
			def.setFwdtxnTrinBkno(form.getTxTrinBkno());
			if (StringUtils.isNotBlank(form.getTxTrinActno())) {
				def.setFwdtxnTrinActno(StringUtils.leftPad(form.getTxTrinActno(), 16, '0'));
			}
			if (StringUtils.isNotBlank(form.getTxtTxAmt())) {
				def.setFwdtxnTxAmt(new BigDecimal(form.getTxtTxAmt()));
			}
			// 記錄auditLog
			AuditLog auditLog = new AuditLog();
			// 塞入auditLog.action
			auditLog.setAction("查詢");
			// 塞入auditLog.params
			if (StringUtils.isNotBlank(String.valueOf(form.getTxDate())))
				auditLog.addParam("預約檔日期", String.valueOf(form.getTxDate()));
			if (StringUtils.isNotBlank(String.valueOf(form.getTxTroutActno())))
				auditLog.addParam("轉出帳號", String.valueOf(form.getTxTroutActno()));		// 最後塞入ThreadLocal變量中
			if (StringUtils.isNotBlank(String.valueOf(form.getTxTrinBkno())))
				auditLog.addParam("轉入帳號:Bkno", String.valueOf(form.getTxTrinBkno()));		// 最後塞入ThreadLocal變量中
			if (StringUtils.isNotBlank(String.valueOf(form.getTxTrinActno())))
				auditLog.addParam("轉入帳號:Actno", String.valueOf(form.getTxTrinActno()));		// 最後塞入ThreadLocal變量中
			if (StringUtils.isNotBlank(String.valueOf(form.getTxtTxAmt())))
				auditLog.addParam("交易金額", String.valueOf(form.getTxtTxAmt()));		// 最後塞入ThreadLocal變量中
			setAuditLog(auditLog);
			PageInfo<HashMap<String, Object>> pageInfo = inbkService.getFwdtxn(def, form.getPageNum(), form.getPageSize());
			PageData<UI_019141_Form, HashMap<String, Object>> pageData = new PageData<>(pageInfo, form);
			int i = 0;
			String wFWDTXN_TX_DATE = "";
			String wFWDTXN_TX_ID = "";
			List<HashMap<String, Object>> fwdtxns = new ArrayList<>(pageData.getList().size());
			if (pageData.getList().size() > 0) {
				wFWDTXN_TX_DATE = pageData.getList().get(0).get("FWDTXN_TX_DATE").toString();
				wFWDTXN_TX_ID = pageData.getList().get(0).get("FWDTXN_TX_ID").toString();
				fwdtxns.add(pageData.getList().get(0));
				int tempVar = pageData.getList().size();
				for (i = 1; i < tempVar; i++) {
					HashMap<String, Object> hashMap = new HashMap<>();
					if (wFWDTXN_TX_DATE.equals(pageData.getList().get(i).get("FWDTXN_TX_DATE").toString()) && wFWDTXN_TX_ID.equals(pageData.getList().get(i).get("FWDTXN_TX_ID").toString())) {
						hashMap.put("FWDTXN_TX_DATE", pageData.getList().get(i).get("FWDTXN_TX_DATE").toString());
						hashMap.put("FWDTXN_TX_ID", "");
						hashMap.put("FWDRST_TX_ID", "");
						hashMap.put("FWDRST_TX_DATE", pageData.getList().get(i).get("FWDTXN_TX_DATE").toString());
						hashMap.put("FWDTXN_PCODE", "");
						hashMap.put("FWDTXN_TROUT_ACTNO", "");
						hashMap.put("FWDTXN_TRIN_BKNO", "");
						hashMap.put("FWDTXN_TRIN_ACTNO", "");
						hashMap.put("FWDTXN_TX_AMT", 0);
						hashMap.put("FWDRST_RUN_NO", pageData.getList().get(i).get("FWDRST_RUN_NO"));
						hashMap.put("FWDRST_EJFNO", pageData.getList().get(i).get("FWDRST_EJFNO"));
						hashMap.put("FWDRST_TXRUST", pageData.getList().get(i).get("FWDRST_TXRUST"));
						hashMap.put("FWDRST_REPLY_CODE", pageData.getList().get(i).get("FWDRST_REPLY_CODE"));
						hashMap.put("FWDRST_ERR_MSG", pageData.getList().get(i).get("FWDRST_ERR_MSG"));
						fwdtxns.add(hashMap);
					} else {
						hashMap = pageData.getList().get(i);
						fwdtxns.add(hashMap);
						wFWDTXN_TX_DATE = pageData.getList().get(i).get("FWDTXN_TX_DATE").toString();
						wFWDTXN_TX_ID = pageData.getList().get(i).get("FWDTXN_TX_ID").toString();
					}
				}
			}

			if (fwdtxns == null || fwdtxns.size() == 0) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
				return;
			} else {
				pageData.setList(fwdtxns);
				HashMap<String, Object> failTimes = inbkService.getFailTimes(def);
				// 失敗筆數
				mode.addAttribute("lblFail", failTimes.get("FAILTIMES").toString());
				// 系統
				mode.addAttribute("lblFailSys", failTimes.get("SYSFAILTIMES").toString());
				// 客戶
				mode.addAttribute("lblFailCust", failTimes.get("CUSTFAILTIMES").toString());
				// 其他
				mode.addAttribute("lblFailOther", failTimes.get("OTHERFAILTIMES").toString());
				WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
			}
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, QueryFail);
		}
	}

	protected void cleanLbl(ModelMap mode) {
		// 失敗筆數
		mode.addAttribute("lblFail", "0");
		// 系統
		mode.addAttribute("lblFailSys", "0");
		// 客戶
		mode.addAttribute("lblFailCust", "0");
		// 其他
		mode.addAttribute("lblFailOther", "0");
		this.clearMessage(mode);
	}

	public void resultGrdvRowCommand(UI_019141_Form form, ModelMap mode) {
		try {
			Batch defBATCH = inbkService.getSingleBATCHByDef("INBK_RETFR");

			if ("0".equals(defBATCH.getBatchResult())) {// 0:執行中
				this.showMessage(mode, MessageType.DANGER, "預約跨行轉帳交易批次處理執行中，無法執行");
				return;
			}
			defBATCH = inbkService.getSingleBATCHByDef("INBK_RETFR_RERUN");
			if ("0".equals(defBATCH.getBatchResult())) {// 0:執行中
				this.showMessage(mode, MessageType.DANGER, "預約跨行轉帳交易批次處理(重跑交易結果失敗的資料)執行中，無法執行");
				return;
			}

			FEPReturnCode rtn = FEPReturnCode.Normal;
			ATMGeneral tita = null;
			ATMGeneral tota = new ATMGeneral();
			RefString errmsg = new RefString("");
			Fwdtxn def = new Fwdtxn();
			def.setFwdtxnTxDate(form.getTxDate().replace("-", ""));
			def.setFwdtxnTxId(form.getTxID());
			def = inbkService.getFwdtxn(def.getFwdtxnTxDate(), def.getFwdtxnTxId());

			if ("0000".equals(def.getFwdtxnReplyCode()) || "    ".equals(def.getFwdtxnReplyCode())) {
				this.showMessage(mode, MessageType.DANGER, "成功交易不得重發");
				return;
			}

			rtn = checkFWDTXN(def, errmsg);

			// Fly 2017/11/14 增加檢核該筆是否執行中且將該筆鎖定
			if (inbkService.lockFwdtxn(def) <= 0) {
				this.showMessage(mode, MessageType.DANGER, "執行失敗，該筆已正在執行中，無法重複執行");
				return;
			}

			if (rtn == CommonReturnCode.Normal) {
				tita = prepareATMTITA(def, mode);
			}
			if (tita != null) {
				// 記錄auditLog
				AuditLog auditLog = new AuditLog();
				// 塞入auditLog.action
				auditLog.setAction("執行");
				// 塞入auditLog.params
				if (StringUtils.isNotBlank(String.valueOf(form.getTxDate())))
					auditLog.addParam("預約檔日期", String.valueOf(form.getTxDate()));
				if (StringUtils.isNotBlank(String.valueOf(form.getTxID())))
					auditLog.addParam("預約交易序號", String.valueOf(form.getTxID()));		// 最後塞入ThreadLocal變量中
				// 最後塞入ThreadLocal變量中
				setAuditLog(auditLog);
				ReserveHandler handler = new ReserveHandler();
				try {
					tota = handler.dispatch(FEPChannel.FCS, tita);
					EJ = handler.getEj();
					if (tota == null) {
						this.showMessage(mode, MessageType.DANGER, "Handler回應電文為空");
					} else {
						this.showMessage(mode, MessageType.SUCCESS, "發送完成");
					}
				} catch (Exception ex) {
					this.showMessage(mode, MessageType.DANGER, QueryFail);
				}
			}
			String message = updateFWDRSTAndUpdateFWDTXN(def, rtn, tota, errmsg.get());
			if (StringUtils.isNotBlank(message)) {
				this.showMessage(mode, MessageType.DANGER, message);
			}

			// 2017/11/06 Modify by Ruling for 寫一筆執行預約跨轉單筆重發處理的記錄
			LogData logContext = new LogData();
			logContext.setChannel(FEPChannel.FEP);
			logContext.setSubSys(SubSystem.INBK);
			logContext.setProgramName("UI_019141");
			logContext.setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			logContext.setMessageId("UI019141");
			logContext.setTxUser(WebUtil.getUser().getUserId());
			logContext.setRemark(TxHelper.getRCFromErrorCode(FEPReturnCode.RETFRSingle, FEPChannel.FEP, logContext));
			inbkService.inbkLogMessage(Level.INFO, logContext);
			bindGridData(form, mode);
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, QueryFail);
		}
	}

	private ATMGeneral prepareATMTITA(Fwdtxn def, ModelMap mode) {
		ATMGeneral tita = new ATMGeneral();
		try {
			if (StringUtils.isNotBlank(def.getFwdtxnOrderDate())) {
				//--ben-20220922-//tita.getRequest().setAtmseq_1(StringUtils.leftPad(CalendarUtil.adStringToROCString(def.getFwdtxnOrderDate()), 8, '0'));
			} else {
				//--ben-20220922-//tita.getRequest().setAtmseq_1("00000000"); // 如為空值補滿八個0
			}
			//--ben-20220922-//tita.getRequest().setAtmseq_2(StringUtils.leftPad("", 8, '0'));
			//--ben-20220922-//tita.getRequest().setBRNO(def.getFwdtxnAtmno().substring(0, 3));
			//--ben-20220922-//tita.getRequest().setWSNO(def.getFwdtxnAtmno().substring(3, 5));
			if (FISCPCode.PCode2521.getValueStr().equals(def.getFwdtxnPcode())) {
				//--ben-20220922-//tita.getRequest().setTXCD(ATMTXCD.IFT.toString());
			} else if (FISCPCode.PCode2561.getValueStr().equals(def.getFwdtxnPcode())) {
				//--ben-20220922-//tita.getRequest().setTXCD(ATMTXCD.EFT.toString());
			} else if (FISCPCode.PCode2532.getValueStr().equals(def.getFwdtxnPcode())) {
				//--ben-20220922-//tita.getRequest().setTXCD(ATMTXCD.IPY.toString());
			}
			if (StringUtils.isNotBlank(def.getFwdtxnAtmChk())) {
				//--ben-20220922-//tita.getRequest().setATMCHK(def.getFwdtxnAtmChk());
			}
			//--ben-20220922-//tita.getRequest().setMODE("1");
			//--ben-20220922-//tita.getRequest().setBKNO(SysStatus.getPropertyValue().getSysstatHbkno());
			//--ben-20220922-//tita.getRequest().setTXACT(def.getFwdtxnTroutActno());
			//--ben-20220922-//tita.getRequest().setBknoD(def.getFwdtxnTrinBkno());
			//--ben-20220922-//tita.getRequest().setActD(def.getFwdtxnTrinActno());
			if (StringUtils.isNotBlank(def.getFwdtxnIcMark())) {
				//--ben-20220922-//tita.getRequest().setICMARK(StringUtil.toHex(def.getFwdtxnIcMark()));
			}
			if (StringUtils.isNotBlank(def.getFwdtxnIcSeqno())) {
				//--ben-20220922-//tita.getRequest().setICTXSEQ(def.getFwdtxnIcSeqno());
			}
			//--ben-20220922-//tita.getRequest().setICTAC("");
			//--ben-20220922-//tita.getRequest().setEXPCD("0000");
			//--ben-20220922-//tita.getRequest().setTXAMT(def.getFwdtxnTxAmt());
			if (StringUtils.isNotBlank(def.getFwdtxnPaytype())) {
				//--ben-20220922-//tita.getRequest().setCLASS(def.getFwdtxnPaytype());
			}
			if (StringUtils.isNotBlank(def.getFwdtxnTaxUnit())) {
				//--ben-20220922-//tita.getRequest().setUNIT(def.getFwdtxnTaxUnit());
			}
			if (StringUtils.isNotBlank(def.getFwdtxnIdno())) {
				//--ben-20220922-//tita.getRequest().setIDNO(def.getFwdtxnIdno());
			}
			if (StringUtils.isNotBlank(def.getFwdtxnDueDate())) {
				//--ben-20220922-//tita.getRequest().setDUEDATE(StringUtils.leftPad(CalendarUtil.adStringToROCString(def.getFwdtxnDueDate()), 8, '0'));
			} else {
				//--ben-20220922-//tita.getRequest().setDUEDATE("00000000"); // 補滿8個0
			}
			if (StringUtils.isNotBlank(def.getFwdtxnReconSeqno())) {
				//--ben-20220922-//tita.getRequest().setPAYCNO(def.getFwdtxnReconSeqno());
			}
			if (StringUtils.isNotBlank(def.getFwdtxnBusinessUnit())) {
				//--ben-20220922-//tita.getRequest().setVPID(def.getFwdtxnBusinessUnit());
			}
			if (StringUtils.isNotBlank(def.getFwdtxnPayno())) {
				//--ben-20220922-//tita.getRequest().setPAYID(def.getFwdtxnPayno());
			}
			if (def.getFwdtxnChannelS() != null) {
				if (def.getFwdtxnChannelS().trim().equals(FEPChannel.IVR.toString())) {
					//--ben-20220922-//tita.getRequest().setMENO("6535");
				} else if (def.getFwdtxnChannelS().trim().equals(FEPChannel.NETBANK.toString())) {
					//--ben-20220922-//tita.getRequest().setMENO("6530");
				} else if ((def.getFwdtxnChannelS().trim().equals(FEPChannel.MOBILBANK.toString()))
//						|| (def.getFwdtxnChannelS().trim().equals(FEPChannel.MMAB2C.toString()))
				) {
					//--ben-20220922-//tita.getRequest().setMENO("6538");
				} else if (def.getFwdtxnChannelS().trim().equals(FEPChannel.ATM.toString())) {
					//--ben-20220922-//tita.getRequest().setMENO("6011");
				}
			}
			//--ben-20220922-//tita.getRequest().setChlEJNo(def.getFwdtxnTxId());
			//--ben-20220922-//tita.getRequest().setCHLCODE(def.getFwdtxnChannelS());
			if (StringUtils.isNotBlank(def.getFwdtxnPsbmemoD())) {
				//--ben-20220922-//tita.getRequest().setPsbmemoD(def.getFwdtxnPsbmemoD());
			}
			if (StringUtils.isNotBlank(def.getFwdtxnPsbmemoC())) {
				//--ben-20220922-//tita.getRequest().setPsbmemoC(def.getFwdtxnPsbmemoC());
			}
			if (StringUtils.isNotBlank(def.getFwdtxnPsbremSD())) {
				//--ben-20220922-//tita.getRequest().setPsbremSD(def.getFwdtxnPsbremSD());
			}
			if (StringUtils.isNotBlank(def.getFwdtxnPsbremSC())) {
				//--ben-20220922-//tita.getRequest().setPsbremSC(def.getFwdtxnPsbremSC());
			}
			if (StringUtils.isNotBlank(def.getFwdtxnPsbremFD())) {
				//--ben-20220922-//tita.getRequest().setPsbremFD(def.getFwdtxnPsbremFD());
			}
			if (StringUtils.isNotBlank(def.getFwdtxnPsbremFC())) {
				//--ben-20220922-//tita.getRequest().setPsbremFC(def.getFwdtxnPsbremFC());
			}
			if (StringUtils.isNotBlank(def.getFwdtxnTfrType())) {
				//--ben-20220922-//tita.getRequest().setRegTfrType(def.getFwdtxnTfrType());
			}
			// Fly 2018/07/11 For 往來明細新增IP欄位分析
			if (StringUtils.isNotBlank(def.getFwdtxnClientip())) {
				//--ben-20220922-//tita.getRequest().setIPADDR(def.getFwdtxnClientip());
			}
			return tita;
		} catch (Exception ex) {
			this.showMessage(mode, MessageType.DANGER, QueryFail);
			return null;
		}
	}

	private FEPReturnCode checkFWDTXN(Fwdtxn def, RefString errmsg) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		Atmmstr defAtmStr = new Atmmstr();
		@SuppressWarnings("unused")
		int i = 0;
		try {
			// 判斷是否為自行交易
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(def.getFwdtxnTrinBkno())) {
				errmsg.set("無法執行自行交易");
				return FEPReturnCode.Abnormal;
			}

			// 檢核 ATM 代號是否存在於 ATM 主檔
			defAtmStr.setAtmAtmno(def.getFwdtxnAtmno());
			defAtmStr = inbkService.getAtmmstr(defAtmStr.getAtmAtmno());
			if (defAtmStr == null) {
				errmsg.set("ATM 代號不存在");
				return IOReturnCode.ATMMSTRNotFound;
			}

			// 檢核 PCODE
			if (!FISCPCode.PCode2521.getValueStr().equals(def.getFwdtxnPcode()) && !FISCPCode.PCode2532.getValueStr().equals(def.getFwdtxnPcode())
					&& !FISCPCode.PCode2561.getValueStr().equals(def.getFwdtxnPcode())) {
				errmsg.set("PCODE 不正確");
				return FEPReturnCode.Abnormal;
			}
			return rtnCode;
		} catch (Exception ex) {
			return FEPReturnCode.ProgramException;
		}
	}

	private String updateFWDRSTAndUpdateFWDTXN(Fwdtxn def, FEPReturnCode rtn, ATMGeneral tota, String errmsg) {
		@SuppressWarnings("unused")
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		Fwdrst defFWDRST = new Fwdrst();
		Fwdtxn defFWDTXN = new Fwdtxn();
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			if (StringUtils.isNotBlank(def.getFwdtxnRunNo().toString()) && PolyfillUtil.isNumeric(def.getFwdtxnRunNo().toString())) {
				defFWDTXN.setFwdtxnRunNo((short) Byte.parseByte(String.valueOf(def.getFwdtxnRunNo().byteValue() + 1)));
			}
			if (rtn != CommonReturnCode.Normal) {
				defFWDTXN.setFwdtxnReplyCode(AbnormalRC.External_Error);
			} else {
				//ben20221118  defFWDTXN.setFwdtxnReplyCode(tota.getResponse().getCHLREJCD());
			}
			defFWDTXN.setFwdtxnTxDate(def.getFwdtxnTxDate());
			defFWDTXN.setFwdtxnTxId(def.getFwdtxnTxId());
			defFWDTXN.setFwdtxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
			defFWDTXN.setFwdtxnRerunFg("Y");
			if (inbkService.updateFwdtxnByPrimaryKey(defFWDTXN) < 1) {
				transactionManager.rollback(txStatus);
				return "更新FWDTXN失敗";
			}
			// modify 20110113
			transactionManager.commit(txStatus);
			txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
			defFWDRST.setFwdrstTxDate(def.getFwdtxnTxDate());
			defFWDRST.setFwdrstTxId(def.getFwdtxnTxId());
			defFWDRST.setFwdrstRunNo((short) Byte.parseByte(String.valueOf(def.getFwdtxnRunNo().byteValue() + 1)));
			tempFWDRST.setFwdrstTxDate(defFWDRST.getFwdrstTxDate());
			tempFWDRST.setFwdrstTxId(defFWDRST.getFwdrstTxId());
			tempFWDRST.setFwdrstRunNo(defFWDRST.getFwdrstRunNo());

			//ben20221118  
			/*
			if (rtn == CommonReturnCode.Normal) {
				if (PolyfillUtil.isNumeric(tota.getResponse().getEJNo())) {
					defFWDRST.setFwdrstEjfno(tota.getResponse().getFepEjno());
				}
				defFWDRST.setFwdrstReplyCode(tota.getResponse().getCHLREJCD());
				defFWDRST.setFwdrstTxrust(tota.getResponse().getTXRUST());
				defFWDRST.setFwdrstCbsRrn(tota.getResponse().getCbsTxid());
				defFWDRST.setFwdrstCbsTxCode(tota.getResponse().getCbsTxcd());
				defFWDRST.setFwdrstErrMsg(tota.getResponse().getERRMSG());
			} else {
				defFWDRST.setFwdrstEjfno(" ");
				defFWDRST.setFwdrstReplyCode(AbnormalRC.External_Error); // 外圍系統錯誤代碼EF2999
				defFWDRST.setFwdrstTxrust("E");
				defFWDRST.setFwdrstCbsRrn(" ");
				defFWDRST.setFwdrstCbsTxCode(" ");
				defFWDRST.setFwdrstErrMsg(errmsg);
			}
			*/
			Fwdrst fwdrst = inbkService.getFwdrst(defFWDRST.getFwdrstTxDate(), defFWDRST.getFwdrstTxId(), defFWDRST.getFwdrstRunNo());
			if (fwdrst != null) {
				transactionManager.rollback(txStatus);
				return "新增FWDRST失敗";
			}else {
				inbkService.insertFwdrst(defFWDRST);
			}
			
			/*
			if (inbkService.insertFwdrst(defFWDRST) < 1) {
				transactionManager.rollback(txStatus);
				return "新增FWDRST失敗";
			}
			*/
			
			transactionManager.commit(txStatus);
			return updateT24();
		} catch (Exception ex) {
			if (!txStatus.isCompleted()) {
				transactionManager.rollback(txStatus);
			}
			return "UpdateFWDRSTAndUpdateFWDTXN失敗";
		}
	}

	private String updateT24() {
		try {
			ATMData TxData = new ATMData();
			TxData.setLogContext(new LogData());
			TxData.getLogContext().setEj(EJ);
//			T24 hostT24 = new T24(TxData);
			Fwdrst fwdrst = inbkService.getFwdrst(tempFWDRST.getFwdrstTxDate(), tempFWDRST.getFwdrstTxId(), tempFWDRST.getFwdrstRunNo());
			if (fwdrst != null) {
//				hostT24.sendToT24forRETFR(fwdrst);
			}
			return "";
		} catch (Exception ex) {
			return QueryFail;
		}

	}
}
