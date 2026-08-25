package com.syscom.fep.web.controller.rm;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.server.aa.rm.RMAABase;
import com.syscom.fep.server.aa.rm.SyncFEDI;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.rm.RM;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.vo.text.FEPRequest;
import com.syscom.fep.vo.text.FEPResponse;
import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.rm.RMGeneral;
import com.syscom.fep.vo.text.rm.request.SyncFEDIRequest;
import com.syscom.fep.vo.text.rm.request.SyncFEDIRequest.SyncFEDIRq;
import com.syscom.fep.vo.text.rm.request.SyncFEDIRequest.SyncFEDISvcRq;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028230_Form;
import com.syscom.fep.web.form.rm.UI_028230_FormDetail;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FEDI轉通匯回饋監控啟動
 * @author jie
 * @create 2021/12/03
 */
@Controller
public class UI_028230Controller extends BaseController {

    @Autowired
    RmService rmService;

    @Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單數據
		UI_028230_Form form = new UI_028230_Form();
		queryClick(form, mode);
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

    @PostMapping(value = "/rm/UI_028230/queryClick")
	public String queryClick(@ModelAttribute UI_028230_Form form, ModelMap mode) {
    	this.doKeepFormData(mode, form);
		logContext.setRemark(StringUtils.join("UI_028230查詢"));
		RM rmBusiness = new RM();
		// '匯出
		try {
			PageInfo<HashMap<String, Object>> dtResult = rmService.getRMOUTForUI028230(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN), form.getPageNum(),
					form.getPageSize());
			if (dtResult == null || dtResult.getSize() == 0) {
				logContext.setRemark(StringUtils.join("無任何FEDI轉通匯資料"));
				logMessage(Level.INFO, logContext);
				this.showMessage(mode, MessageType.DANGER, "無任何FEDI轉通匯資料");
			} else {				
				for (int i = 0; i < dtResult.getSize(); i++) {
					HashMap<String, Object> map = dtResult.getList().get(i);
					if (map.containsKey("RMOUT_SENDTIME") && StringUtils.isNotBlank(map.get("RMOUT_SENDTIME").toString())) {
						map.put("RMOUT_SENDTIME", charTimeToTime(map.get("RMOUT_SENDTIME").toString()));
					}
					map.put("RMOUT_FEPNO_UI",StringUtils.join(map.get("RMOUT_BRNO").toString(), "-", map.get("RMOUT_ORIGINAL").toString(), "-", map.get("RMOUT_FEPNO").toString()));
					if (map.containsKey("RMOUT_STAT") && StringUtils.isNotBlank(map.get("RMOUT_STAT").toString())) {
						map.put("RMOUT_STAT_UI", rmBusiness.mapRMOUTStat(map.get("RMOUT_STAT").toString()));
					}
					if (map.containsKey("RMOUT_FISC_RTN_CODE") && StringUtils.isNotBlank(map.get("RMOUT_FISC_RTN_CODE").toString())) {
						map.put("RMOUT_FISC_RTNCODE_UI", StringUtils.join(map.get("RMOUT_FISC_RTN_CODE").toString(), ":",TxHelper.getMessageFromFEPReturnCode(map.get("RMOUT_FISC_RTN_CODE").toString(), FEPChannel.FISC )));
						
					} else {
						map.put("RMOUT_FISC_RTNCODE_UI", "");
					}				
				}
				PageData<UI_028230_Form, HashMap<String, Object>> pageData = new PageData<>(dtResult, form);
				WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			FEPBase.sendEMS(getLogContext());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_028230.getView();
	}
  
    //Gridview 第一列查詢單筆明細
    @PostMapping(value = "/rm/UI_028230/inquiryDetail")
	public String doInquiryDetail(@ModelAttribute UI_028230_FormDetail form, ModelMap mode) {
		try {
			RMGeneral rmGeneral = new RMGeneral();
			rmGeneral.getRequest().setKINBR(form.getRmoutBrno());
			rmGeneral.getRequest().setTRMSEQ("99");
			rmGeneral.getRequest().setBRSNO("");
			rmGeneral.getRequest().setENTTLRNO(WebUtil.getUser().getUserId());
			rmGeneral.getRequest()
					.setTIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
			rmGeneral.getRequest().setFEPNO(form.getRmoutFepno());
			rmGeneral.getRequest().setREMDATE(form.getRmoutTxDate());
			rmGeneral.getRequest().setFISCRC(form.getRmoutFiscRtnCode());
			if (StringUtils.isNotBlank(form.getRmoutFiscRtnCode())) {
				rmGeneral.getRequest().setCHLRC(
						TxHelper.getRCFromErrorCode(form.getRmoutFiscRtnCode(), FEPChannel.FISC, FEPChannel.FEP));
				rmGeneral.getRequest()
						.setCHLMSG(TxHelper.getMessageFromFEPReturnCode(form.getRmoutFiscRtnCode(), FEPChannel.FISC));
			}
			rmGeneral.getRequest().setSTATUS(form.getRmoutStat());
			rmGeneral.getRequest().setORIGINAL(form.getRmoutOriginal());
			RMData txData = new RMData();
			txData.setFepRequest(new FEPRequest());
			txData.getFepRequest().getRqHeader().setChlName("FEPMon");
			txData.setFepResponse(new FEPResponse());
			txData.setEj(TxHelper.generateEj());
			txData.setTxObject(rmGeneral);
			txData.setTxChannel(FEPChannel.FEP);
			txData.setTxSubSystem(SubSystem.RM);

			SyncFEDIRequest syncFEDIReq = new SyncFEDIRequest();
			syncFEDIReq.setRqHeader(new FEPRqHeader());
			syncFEDIReq.setSvcRq(new SyncFEDISvcRq());
			syncFEDIReq.getSvcRq().setRq(new SyncFEDIRq());
			txData.setTxRequestMessage(syncFEDIReq.makeMessageFromGeneral(rmGeneral));
			txData.setMessageID("SYNCFEDI");
			List<Msgctl> msgCtlTable = FEPCache.getMsgctlList();
			Map<String, Msgctl> map = msgCtlTable.stream()
					.collect(Collectors.toMap(Msgctl::getMsgctlMsgid, msgctl -> msgctl));
			txData.setMsgCtl(map.get("SYNCFEDI"));
			txData.setLogContext(new LogData());
			txData.getLogContext().setTxDate(
					FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
			txData.getLogContext().setSubSys(txData.getTxSubSystem());
			txData.getLogContext().setChannel(txData.getTxChannel());
			txData.getLogContext().setProgramFlowType(ProgramFlow.AAIn);
			txData.getLogContext().setMessageFlowType(MessageFlow.Request);
			txData.getLogContext().setEj(txData.getEj());
			txData.getLogContext().setMessage(txData.getTxRequestMessage());
			RMAABase aa = null;
			aa = new SyncFEDI(txData);
			aa.processRequestData();
			queryClick(new UI_028230_Form(), mode);
			String strMsg = "PK(RMOUT_TXDATE=" + form.getRmoutTxDate() + ";RMOUT_BRNO=" + form.getRmoutBrno()
					+ ";RMOUT_ORIGINAL=" + form.getRmoutOriginal() + ";RMOUT_FEPNO=" + form.getRmoutFepno() + ")";
			if ("SUCCESS".equals(txData.getFepResponse().getRsHeader().getRsStat().getRsStatCode())) {
				this.showMessage(mode, MessageType.SUCCESS, DealSuccess);
				strMsg = "傳送成功" + strMsg;
			} else {
				if (NormalRC.External_OK.equals(txData.getTxObject().getResponse().getRsStatRsStateCode())) {
					this.showMessage(mode, MessageType.SUCCESS, DealSuccess);
					strMsg = "傳送成功" + strMsg;
				} else {
					this.showMessage(mode, MessageType.DANGER, DealFail);
					strMsg = "FEDI轉通匯匯出狀態回饋失敗" + strMsg;
				}
			}
			prepareAndSendEMSData(strMsg);
		} catch (Exception e) {
			this.showMessage(mode, MessageType.DANGER, e.toString());
		}
		return Router.UI_028230.getView();
	}

	private void prepareAndSendEMSData(String strMsg) throws Exception {
		LogData logContext = new LogData();
		logContext.setChannel(FEPChannel.FEP);
		logContext.setSubSys(SubSystem.RM);
		logContext.setProgramName("UI_028230");
		logContext.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
		logContext.setDesBkno(SysStatus.getPropertyValue().getSysstatHbkno());
		logContext.setMessageId("UI028230");
		logContext.setMessageGroup("4");
		logContext.setRemark(strMsg);
		logContext.setTxUser(WebUtil.getUser().getUserId());
		logContext.setReturnCode(RMReturnCode.SendFEDIBackTele);
		TxHelper.getRCFromErrorCode(logContext.getReturnCode(), FEPChannel.FEP, logContext);
	}
}
