package com.syscom.fep.web.controller.rm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ems.ext.model.FeplogExt;
import com.syscom.fep.mybatis.ems.model.Feplog;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_RM;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SessionKey;
import com.syscom.fep.web.form.rm.UI_028080_Form;
import com.syscom.fep.web.service.EmsService;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 匯兌財金格式明細查詢
 *
 * @author Chenyu
 */
@Controller
public class UI_028080Controller extends BaseController {

	@Autowired
	RmService rmService;
	@Autowired
	EmsService emsService;

	String acpPCODE = "1111,1121,1131,1171,1181,1191,1112,1122,1172,1182,1192,1411,1412,1511,1512,1513,1514,1515";
	String outPCODE = "1111,1121,1131,1171,1181,1191,1411";
	FISC rmBuss;
	Integer ej = 0;
	List<Feplog> dtTemp;
	PageInfo<HashMap<Object, String>> hash;

	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單數據
		UI_028080_Form form = new UI_028080_Form();
		// 交易日期
		form.setTradingDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/rm/UI_028080/queryClick")
	public String queryClick(@ModelAttribute UI_028080_Form form, ModelMap mode, Feplog defFeplog) throws Exception {
		this.infoMessage("查詢主檔數據, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);

		// 2021-12-14 Richard modified
		// 如果是按下分頁按鈕, 則從session中取資料
		if (form.isRedirectFromPageChanged()) {
			dtTemp = WebUtil.getFromSession(SessionKey.TemporaryRestoreData);
		} else {
			ej = TxHelper.generateEj();
			logContext.setProgramName("UI_028080");
			logContext.setMessageId("UI_028080");
			logContext.setEj(ej);
			dtTemp = queryData(form, mode);
			WebUtil.putInSession(SessionKey.TemporaryRestoreData, dtTemp);
		}

		if (dtTemp != null) {
			logContext.setRemark("After Func. QueryData, Query Cnt=" + dtTemp.size());
			rmService.logMessage(logContext, Level.INFO);
		} else {
			logContext.setRemark("After Func. QueryData, Query Result is Nothing");
			rmService.logMessage(logContext, Level.INFO);
			this.showMessage(mode, MessageType.INFO, QueryNoData);
		}

		bindData(mode, form, defFeplog);
		checkData(mode);
		return Router.UI_028080.getView();
	}

	private List<Feplog> queryData(UI_028080_Form form, ModelMap mode) {
		Feplog defFEPLog = new FeplogExt();
		String tradingDate = StringUtils.replace(form.getTradingDate(), "-", StringUtils.EMPTY);
		try {
			if (acpPCODE.indexOf(form.getMsgPcode()) >= 0) {
				defFEPLog.setMessageid(StringUtils.rightPad(form.getMsgPcode(), 6, "0"));

				if ("1".equals(defFEPLog.getMessageid().substring(3, 4))) {
					if ("1".equals(form.getMsgflow())) {
						defFEPLog.setProgramflow(ProgramFlow.AdapterIn.toString());
					} else {
						defFEPLog.setMessageflow(ProgramFlow.AdapterOut.toString());
					}
				} else if ("2".equals(defFEPLog.getMessageid().substring(3, 4))) {
					if ("1".equals(form.getMsgflow())) {
						defFEPLog.setProgramflow(ProgramFlow.AAIn.toString());
					} else {
						defFEPLog.setMessageflow(ProgramFlow.AAOut.toString());
					}
				}
			} else {
				dtTemp = null;
				return null;
			}

			// 2012-07-06 Modify by Candy 匯出Channel 改用"FEP"
			if (outPCODE.indexOf(form.getMsgPcode()) >= 0) {
				defFEPLog.setChannel("FEP");
			} else {
				defFEPLog.setChannel("FISC");
			}

			if ("1".equals(form.getMsgflow())) {
				defFEPLog.setMessageflow("Request");
			} else {
				defFEPLog.setMessageflow("Response");
			}

			if (StringUtils.isNotBlank(form.getTradingDate().trim())) {
				defFEPLog.setTxdate(tradingDate);
			}

			if (StringUtils.isNotBlank(form.getTradingDate().trim())) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				defFEPLog.setLogdate(simpleDateFormat.parse(form.getTradingDate() + " 00:00:00"));

			}

			// add by maxine on 2011/06/21 for UI增加STAN查詢條件
			if (StringUtils.isNotBlank(form.getUiStan().trim())) {
				defFEPLog.setStan(form.getUiStan());
			}

			dtTemp = getFEPLogByDef(mode, form, defFEPLog);
			if (dtTemp.size() > 0) {
				if ("1".equals(form.getKind())) {
					dtTemp = dtTemp.stream().sorted(Comparator.comparing(Feplog::getTxdate).reversed().thenComparing(Comparator.comparing(Feplog::getEj).reversed())).collect(Collectors.toList());
				} else {
					dtTemp = dtTemp.stream().sorted(Comparator.comparing(Feplog::getTxdate).reversed().thenComparing(Comparator.comparing(Feplog::getStan).reversed())).collect(Collectors.toList());
				}
			}
			return dtTemp;
		} catch (Exception e) {
			this.showMessage(mode, MessageType.WARNING,programError);
		}
		return dtTemp;
	}

	private void bindData(ModelMap mode, UI_028080_Form form, Feplog defFeplog) {
		@SuppressWarnings("unused")
		FISC_RM RMDATA = new FISC_RM();
		String txMessage = "";
		Integer rtncode = 0;
		String currentLogNo = "";

		try {

			if (dtTemp == null) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
			} else {

				dtTemp = dtTemp.stream().filter(item -> "1".equals(form.getMsgflow()) ? (item.getProgramflow().equals("AAIn") || item.getProgramflow().equals("AdapterIn"))
						: (item.getProgramflow().equals("AAOut") || item.getProgramflow().equals("AdapterOut"))).collect(Collectors.toList());

				List<HashMap<Object, String>> ds = new ArrayList<>();
				for (Feplog dtrow : dtTemp) {
					currentLogNo = dtrow.getLogno().toString();
					logContext.setRemark("LogNo=" + currentLogNo + ", ProgramFlow=" + dtrow.getProgramflow());

					// 'Pares RM Header
					if (StringUtils.isNotBlank(dtrow.getTxmessage())) {
						txMessage = dtrow.getTxmessage();
						RMDATA.setFISCMessage(txMessage);
					}
					try {
						rtncode = RMDATA.parseFISCMsg().getValue();
						if (rtncode != FEPReturnCode.Normal.getValue()) {
							logContext.setRemark("ParseFISCMsg失敗, rtncode=" + rtncode);
							rmService.logMessage(logContext, Level.INFO);
						}

						// Parse RM Body
						FISCData txFISCData = new FISCData();
						FISCGeneral fiscGeneral = new FISCGeneral();

						txFISCData.setTxObject(fiscGeneral);
						txFISCData.setMessageID(RMDATA.getProcessingCode());
						txFISCData.setFiscTeleType(FISCSubSystem.RM);
						if ("1".equals(form.getMsgflow())) {
							txFISCData.setMessageFlowType(MessageFlow.Request);
							txFISCData.getTxObject().setRMRequest(RMDATA);
						} else {
							txFISCData.setMessageFlowType(MessageFlow.Response);
							txFISCData.getTxObject().setRMResponse(RMDATA);
						}

						rmBuss= new FISC(txFISCData);
						LogData logData = new LogData();
						logData.setChannel(FEPChannel.FEP);
						logData.setMessageFlowType(MessageFlow.Request);
						rmBuss.setLogContext(logData);
						rmBuss.checkBitmap(RMDATA.getAPData());

						// 判斷解析出來的電文 發送行與接收行是否與UI一致
						if (StringUtils.isNotBlank(form.getSenderBankTxt())) {
							int sendBanklength = form.getSenderBankTxt().length();
							if (!RMDATA.getSenderBank().substring(0, sendBanklength).equals(form.getSenderBankTxt().substring(0, sendBanklength))) {
								logContext.setRemark("發送行不一致, 查詢發送行=" + form.getSenderBankTxt().substring(0, sendBanklength) + ", 電文發送行=" +
										RMDATA.getSenderBank().substring(0, sendBanklength));
								rmService.logMessage(logContext, Level.INFO);
							}
						}
						if (StringUtils.isNotBlank(form.getReceiverBankTxt())) {
							int receiverBanklength = form.getReceiverBankTxt().length();
							if (!RMDATA.getReceiverBank().substring(0, receiverBanklength).equals(form.getReceiverBankTxt().substring(0, receiverBanklength))) {
								logContext.setRemark("發送行不一致, 查詢收訊行=" + form.getReceiverBankTxt().substring(0, receiverBanklength) + ", 電文收訊行=" +
										RMDATA.getReceiverBank().substring(0, receiverBanklength));
								rmService.logMessage(logContext, Level.INFO);
							}
						}

						if (StringUtils.isNotBlank(RMDATA.getALIASNAME().trim())) {
							// 'logData.Remark = "FEBWeb UI028080 convert chinese ALIASNAME"
							// 'logData.Message = "RMDATA.ALIASNAME= " & RMDATA.ALIASNAME
							// 'rmSrv.LogMessage(logData, LogLevel.Info)
							RefString aliasnames = new RefString(RMDATA.getALIASNAME());
							if (!rmBuss.convertFiscDecode(RMDATA.getALIASNAME(), aliasnames)) {
								RMDATA.setALIASNAME("ＸＸＸＸＸ");
							}else{
								RMDATA.setALIASNAME(aliasnames.get());
							}
						}
						if (StringUtils.isNotBlank(RMDATA.getInName())) {
							// 'logData.Remark = "FEBWeb UI028080 convert chinese IN_NAME"
							// 'logData.Message = "RMDATA.IN_NAME = & RMDATA.IN_NAME"
							// 'rmSrv.LogMessage(LogData, LogLevel.Info)
							RefString innames = new RefString(RMDATA.getInName());
							if (!rmBuss.convertFiscDecode(RMDATA.getInName(), innames)) {
								RMDATA.setOutName("ＸＸＸＸＸ");
							}else{
								RMDATA.setInName(innames.get());
							}
						}
						if (StringUtils.isNotBlank(RMDATA.getOutName())) {
							// 'logData.Remark = "FEBWeb UI028080 convert chinese OUT_NAME"
							// 'logData.Message = "RMDATA.OUT_NAME= " & RMDATA.OUT_NAME
							// 'rmSrv.LogMessage(logData, LogLevel.Info)
							RefString outnames = new RefString(RMDATA.getOutName());
							if (!rmBuss.convertFiscDecode(RMDATA.getOutName(), outnames)) {
								RMDATA.setOutName("ＸＸＸＸＸ");
							}else{
								RMDATA.setOutName(outnames.get());
							}
						}
						if (StringUtils.isNotBlank(RMDATA.getChineseMemo())) {
							// 'logData.Remark = "FEBWeb UI028080 convert chinese CHINESE_MEMO"
							// 'logData.Message = "RMDATA.CHINESE_MEMO =" & RMDATA.CHINESE_MEMO
							// 'rmSrv.LogMessage(logData, LogLevel.Info)
							RefString chinesenames = new RefString(RMDATA.getChineseMemo());
							if (!rmBuss.convertFiscDecode(RMDATA.getChineseMemo(), chinesenames)) {
								RMDATA.setChineseMemo("ＸＸＸＸＸ");
							}else{
								RMDATA.setChineseMemo(chinesenames.get());
							}
						}
						// 'RMDATA.ALIASNAME = ConvertChinese(RMDATA.ALIASNAME, RMDATA.ALIASNAME) '發信行中文簡稱
						// 'RMDATA.IN_NAME = ConvertChinese(RMDATA.IN_NAME, RMDATA.IN_NAME) '收款人姓名
						// 'RMDATA.OUT_NAME = ConvertChinese(RMDATA.OUT_NAME, RMDATA.OUT_NAME) '匯款人姓名
						// 'RMDATA.CHINESE_MEMO = ConvertChinese(RMDATA.CHINESE_MEMO, RMDATA.CHINESE_MEMO) '附言
						// 'RMDATA.ENGLISH_MEMO = ConvertChinese(RMDATA.ENGLISH_MEMO, RMDATA.ENGLISH_MEMO)

						// 'Option欄位要補空白或0
						if (StringUtils.isBlank(RMDATA.getSTATUS())) {
							RMDATA.setSTATUS(" ");
						}
						if (StringUtils.isBlank(RMDATA.getOrgBankNo())) {
							RMDATA.setOrgBankNo(" ");
						}
						if (StringUtils.isBlank(RMDATA.getOrgPcode())) {
							RMDATA.setOrgPcode(" ");
						}
						if (StringUtils.isBlank(RMDATA.getALIASNAME())) {
							RMDATA.setALIASNAME(" ");
						}
					} catch (Exception ex) {
						logContext.setRemark("解析財金電文錯誤, 電文=" + RMDATA.getFISCMessage());
						rmService.logMessage(logContext, Level.ERROR);
					}

					HashMap<Object, String> mm = new HashMap<>();

					mm.put("TRNCD", RMDATA.getMessageType() + RMDATA.getProcessingCode());
					mm.put("SOURID", RMDATA.getTxnSourceInstituteId());
					mm.put("RC", RMDATA.getResponseCode());
					mm.put("STAN", RMDATA.getSystemTraceAuditNo());
					mm.put("DESTID", RMDATA.getTxnDestinationInstituteId());
					mm.put("TXTIME", RMDATA.getTxnInitiateDateAndTime().substring(6, 12));
					mm.put("ACTNO", RMDATA.getInActno());
					mm.put("FISCSNO", RMDATA.getFiscNo());
					mm.put("UI_SENDER_BANK", RMDATA.getSenderBank());
					mm.put("REMDATE", RMDATA.getTxDate());
					mm.put("UI_RECEIVER_BANK", RMDATA.getReceiverBank());
					mm.put("STAT", RMDATA.getSTATUS());
					mm.put("TXAMT", RMDATA.getTxAmt());
					mm.put("RMSNO", RMDATA.getBankNo());
					mm.put("ORGRMSNO", RMDATA.getOrgBankNo());
					mm.put("ORG_PCODE", RMDATA.getOrgPcode());
					mm.put("ALIASNAME", RMDATA.getALIASNAME());
					mm.put("IN_NAME", RMDATA.getInName());
					mm.put("OUT_NAME", RMDATA.getOutName());
					mm.put("CHNMEMO", RMDATA.getChineseMemo());
					mm.put("ENGMEMO", RMDATA.getEnglishMemo());
					mm.put("TxMessage", RMDATA.getFISCMessage());
					mm.put("RM_PENDING_CNT", RMDATA.getRmPendingCnt());
					mm.put("RM_PENDING_AMT", RMDATA.getRmPendingAmt());
					mm.put("ORG_STAN", RMDATA.getOrgStan());
					mm.put("ORG_TX_DATETIME", RMDATA.getOrgTxDatetime());

					ds.add(mm);
				}
				// 2021-12-14 Richard modified
				hash = this.clientPaged(ds, form.getPageNum(), 1);
				WebUtil.putInAttribute(mode, AttributeName.PageData, hash);
			}
			// WebUtil.putInAttribute(mode, AttributeName.PageData, dm);
		} catch (Exception ex) {
			logContext.setRemark("Parse 財金電文錯誤2, LogNo=" + currentLogNo);
			rmService.logMessage(logContext, Level.ERROR);
			String ExceptionPolicyName = "Business Policy";
			this.showMessage(mode, MessageType.DANGER, ExceptionPolicyName);
		}
	}

	private boolean checkData(ModelMap mode) {
		if (hash == null || hash.getList().size() < 1) {
			this.showMessage(mode, MessageType.INFO, QueryNoData);
			return false;
		}
		return true;
	}

	private List<Feplog> getFEPLogByDef(ModelMap mode, UI_028080_Form form, Feplog defFEPLog) throws Exception {
		// Dim dbFEPLog As New Tables.DBFEPLog(FEPConfig.EMSName)
		String tradingDate = StringUtils.replace(form.getTradingDate(), "-", StringUtils.EMPTY);
		Calendar cal = CalendarUtil.parseDateValue(Integer.parseInt(tradingDate));
		String tableNameSuffix = String.valueOf(CalendarUtil.getDayOfWeek(cal));
		List<Feplog> dtFEPLog = null;
		try {
			dtFEPLog = emsService.getFeplogByDef(defFEPLog, form, tableNameSuffix, form.getPageNum(), form.getPageSize());
		} catch (Exception ex) {
			this.showMessage(mode, MessageType.DANGER, "查詢失敗");
		}
		return dtFEPLog;
	}
}
