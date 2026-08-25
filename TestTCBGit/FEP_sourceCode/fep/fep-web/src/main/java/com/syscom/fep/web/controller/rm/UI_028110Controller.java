package com.syscom.fep.web.controller.rm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ems.model.Feplog;
import com.syscom.fep.mybatis.ext.mapper.AllbankExtMapper;
import com.syscom.fep.mybatis.ext.mapper.MsginExtMapper;
import com.syscom.fep.mybatis.ext.mapper.MsgoutExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RminExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmoutExtMapper;
import com.syscom.fep.mybatis.ext.model.RmoutExt;
import com.syscom.fep.mybatis.model.Allbank;
import com.syscom.fep.mybatis.model.Msgin;
import com.syscom.fep.mybatis.model.Msgout;
import com.syscom.fep.mybatis.model.Rmin;
import com.syscom.fep.mybatis.model.Rmout;
import com.syscom.fep.mybatis.model.Rmoute;
import com.syscom.fep.server.common.business.rm.RM;
import com.syscom.fep.vo.constant.RMOUTStatus;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028110_Form;
import com.syscom.fep.web.form.rm.UI_028110_FormDetail;
import com.syscom.fep.web.service.EmsService;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 匯款交易查詢
 *
 * @author Chen_yu
 * @create 2021/10/29
 */
@Controller
public class UI_028110Controller extends BaseController {
	@Autowired
	RmService rmService;
	@Autowired
	RmoutExtMapper rmoutExtMapper;
	@Autowired
	MsgoutExtMapper msgoutExtMapper;
	@Autowired
	AllbankExtMapper allbankExtMapper;
	@Autowired
	private EmsService emsSvr;

	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單數據
		UI_028110_Form form = new UI_028110_Form();
		// 交易日期
		form.setTradingDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/rm/UI_028110/queryClick")
	public String queryClick(@ModelAttribute UI_028110_Form form, ModelMap mode) {
		// form.setTradingDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_2));
		this.infoMessage("查詢主檔數據, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		String rtnMsg = "";
		try {
			// _dtResult = null;
			if (checkAllField()) {
				// LogSqlCommandText = New List(Of String)
				// 'Modified by ChenLi on 2014/12/01 for 個資log紀錄查詢
				// LogSqlCommandText.Add("匯出/匯入=" & IOFLAG.SelectedItem.Text)
				String gridview = "";
				// 根據匯出匯入記號取得資料
				if (!"14".equals(form.getRemtypeddl())) {
					switch (form.getIoflag()) {
						case "1": // RMOUT
							gridview = "rmout";
							mode.addAttribute("gridview", gridview);
							getRmout(form, mode);
							break;
						case "2": // RMIN
							gridview = "rmin";
							mode.addAttribute("gridview", gridview);
							getRmin(form, mode);
							break;
					}
				} else {
					// Add by Jim, 2011/03/01, 加上一般通訊的查詢
					switch (form.getIoflag()) {
						case "1": // MSGOUT
							gridview = "msgout";
							mode.addAttribute("gridview", gridview);
							getMsgout(form, mode);
							break;
						case "2": // MSGIN
							gridview = "msgin";
							mode.addAttribute("gridview", gridview);
							getMsgin(form, mode);
							break;
					}
				}

				// 2020/10/05 Modify by Ruling for Fortify：修正Null Dereference問題
				// if (_dtResult != null) {
				// for (int i=0; i<_dtResult.getsize();i++) {
				// if (StringUtils.isBlank(_dtResult.getsize(i).StanBkno())) {
				//
				// }
				// }
				// }
			}
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.WARNING, programError);
		}
		return Router.UI_028110.getView();
	}

	public void getRmout(UI_028110_Form form, ModelMap mode) {
		Rmout defRmout = new Rmout();
		Boolean isOrderByFISCSNO = true;
		LogData _logContext = new LogData();

		try {
			// Modified by ChenLi on 2014/12/01 for 個資log紀錄查詢
			// 匯款日期
			if (StringUtils.isNotBlank(form.getTradingDate())) {
				// LogSqlCommandText.Add("交易日=" & DATEtxt.Text)
				String tradingDate = StringUtils.replace(form.getTradingDate(), "-", StringUtils.EMPTY);
				defRmout.setRmoutTxdate(tradingDate);
			}
			// 匯款種類
			if (StringUtils.isNotBlank(form.getRemtypeddl())) {
				// LogSqlCommandText.Add("匯款類別=" & REMTYPEddl.SelectedItem.Text)
				defRmout.setRmoutRemtype(form.getRemtypeddl());
			}

			// add by maxine on 2011/06/17 for 增加查詢條件
			// 匯款行
			if (StringUtils.isNotBlank(form.getSenderBankTxt())) {
				// LogSqlCommandText.Add("匯款行=" & SenderBankTxt.Text)
				defRmout.setRmoutSenderBank(form.getSenderBankTxt());
			}

			// 解款行
			if (StringUtils.isNotBlank(form.getReceiverBankTxt())) {
				// LogSqlCommandText.Add("解款行=" & ReceiverBankTxt.Text)
				defRmout.setRmoutReceiverBank(form.getReceiverBankTxt());
			}

			// 根據 查詢類別 將 查詢序號 填到 財金電文序號 或 跨行通匯序號
			// If Not String.IsNullOrEmpty(SNOTxt.Text.Trim) AndAlso Me.SNOTxt.Text <> "0" Then
			if (StringUtils.isNotBlank(form.getSnoTxt()) && !"0".equals(form.getSnoTxt())) {
				// 2020/10/08 modified by Carrie for Fortify修正:Cross-Site Scripting: Reflected
				// LogSqlCommandText.Add("序號=" & Server.HtmlEncode(SNOTxt.Text))
				// LogSqlCommandText.Add("查詢類別=" & KINDddl.SelectedItem.Text)
				switch (form.getKindddl()) {
					case "1":
						defRmout.setRmoutFiscsno(StringUtils.leftPad(form.getSnoTxt(), 7, "0"));
						break;
					case "2":
						isOrderByFISCSNO = false;
						defRmout.setRmoutRmsno(form.getSnoTxt());
						break;
					case "3":
						defRmout.setRmoutFepno(StringUtils.leftPad(form.getSnoTxt(), 7, "0"));
						break;
					case "4": // STAN
						defRmout.setRmoutStan(StringUtils.leftPad(form.getSnoTxt(), 7, "0"));
						break;
					case "5": // EJ
						defRmout.setRmoutEjno1(Integer.parseInt(form.getSnoTxt()));
						break;
				}
			}

			if (!"".equals(form.getOrginalddl())) {
				// LogSqlCommandText.Add("來源別=" & Server.HtmlEncode(ORGINALDDL.SelectedItem.Text))
				defRmout.setRmoutOriginal(form.getOrginalddl());
				if ("1".equals(form.getOrginalddl()) && !"".equals(form.getBatchnoTxt())) { // FCS
					// 2020/10/05 modified by Carrie for Fortify修正:Cross-Site Scripting: Reflected
					// LogSqlCommandText.Add("批號=" & Server.HtmlEncode(BATCHNOTxt.Text.Trim))
					defRmout.setRmoutBatchno(form.getBatchnoTxt());
				}
			}
			if (!"".equals(form.getBrnoTxt())) {
				// LogSqlCommandText.Add("登錄分行=" & Server.HtmlEncode(BRNOTxt.Text.Trim))
				defRmout.setRmoutBrno(form.getBrnoTxt());
			}
			if (!"".equals(form.getRmstatddl()) && !"00".equals(form.getRmstatddl())) {
				// LogSqlCommandText.Add("匯款狀態=" & RMSTATDDL.SelectedItem.Text)
				defRmout.setRmoutStat(form.getRmstatddl());
			}

			// Fly 2019/05/08 For 跨行餘額內控 增加匯出優先順序
			if (!"".equals(form.getOwpriorityddl())) {
				// LogSqlCommandText.Add("匯出優先順序=" & OWPRIORITYDDL.SelectedItem.Text)
				defRmout.setRmoutOwpriority(form.getOwpriorityddl());
			}

			// modified by Maxine for QueryRMOUT Union RMOUTE
			// _dtResult = rmService.getRMOUTUnionRMOUTEByDef(defRmout);
			Boolean finsllOrderByFISCSNO = isOrderByFISCSNO;
			// 2021-12-17 Richard modified
			List<RmoutExt> _dtResult = rmService.getRMOUTUnionRMOUTEByDef(defRmout, finsllOrderByFISCSNO);

			if (CollectionUtils.isEmpty(_dtResult)) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
				return;
			} else {
				// Dim tmpDr() As DataRow
				List<Rmout> tmpDr = null;
				// 有key金額的話再判斷查詢金額的條件
				if (!"".equals(form.getTxamtTxt())) {
					// LogSqlCommandText.Add("匯款金額=" & TXAMTTxt.Text)
					// LogSqlCommandText.Add("匯款金額類別=" & TXAMT_KINDDDL.SelectedItem.Text)
					switch (form.getTxamtkindddl()) {
						case "1":
							tmpDr = _dtResult.stream().filter(item -> Integer.valueOf(item.getRmoutTxamt().toString()) > Integer.valueOf(form.getTxamtTxt())).collect(Collectors.toList());
							if (tmpDr.size() > 0) {
								_dtResult = _dtResult.stream().filter(item -> Integer.valueOf(item.getRmoutTxamt().toString()) > Integer.valueOf(form.getTxamtTxt())).collect(Collectors.toList());
							} else {
								_dtResult = null;
							}
							break;
						case "2":
							tmpDr = _dtResult.stream().filter(item -> Integer.valueOf(item.getRmoutTxamt().toString()) <= Integer.valueOf(form.getTxamtTxt())).collect(Collectors.toList());
							if (tmpDr.size() > 0) {
								_dtResult = _dtResult.stream().filter(item -> Integer.valueOf(item.getRmoutTxamt().toString()) <= Integer.valueOf(form.getTxamtTxt())).collect(Collectors.toList());
							} else {
								_dtResult = null;
							}
							break;
						case "3":
							tmpDr = _dtResult.stream().filter(item -> Integer.valueOf(form.getTxamtTxt()).equals(Integer.valueOf(item.getRmoutTxamt().toString()))).collect(Collectors.toList());
							if (tmpDr.size() > 0) {
								_dtResult =
										_dtResult.stream().filter(item -> Integer.valueOf(form.getTxamtTxt()).equals(Integer.valueOf(item.getRmoutTxamt().toString()))).collect(Collectors.toList());
							} else {
								_dtResult = null;
							}
							break;
					}
				}
			}
			if (CollectionUtils.isEmpty(_dtResult)) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
				return;
			} else {
				Allbank tmpALLBANK = new Allbank();
				if(_dtResult != null) {
					for (int i = 0; i < _dtResult.size(); i++) {
						if (_dtResult.get(i).getRmoutReceiverBank() != null) {
							tmpALLBANK.setAllbankBkno(_dtResult.get(i).getRmoutReceiverBank().substring(0, 3));
							tmpALLBANK.setAllbankBrno("000");
							Allbank aLLBANK = allbankExtMapper.selectByPrimaryKey(tmpALLBANK.getAllbankBkno(), tmpALLBANK.getAllbankBrno());
							if (aLLBANK != null) {
								// UnioBank 用RmoutGlUnit1代替
								_dtResult.get(i).setRmoutGlUnit1(aLLBANK.getAllbankUnitBank());
							} else {
								_dtResult.get(i).setRmoutGlUnit1("000");
							}
						}
						if (RMOUTStatus.Transfered.equals(_dtResult.get(i).getRmoutStat())) {
							_dtResult.get(i).setRmoutFiscRtnCode("N REP");
						} else if ("04".equals(_dtResult.get(i).getRmoutStat()) || "07".equals(_dtResult.get(i).getRmoutStat()) || "99".equals(_dtResult.get(i).getRmoutStat())) {
							tmpALLBANK = new Allbank();
							tmpALLBANK.setAllbankBkno(_dtResult.get(i).getRmoutSenderBank().substring(0, 3));
							tmpALLBANK.setAllbankBrno(_dtResult.get(i).getRmoutSenderBank().substring(3, 6));
							Allbank aLLBANK = allbankExtMapper.selectByPrimaryKey(tmpALLBANK.getAllbankBkno(), tmpALLBANK.getAllbankBrno());
							if (aLLBANK != null) {
								if ("1".equals(tmpALLBANK.getAllbankRmforward())) {
									_dtResult.get(i).setRmoutFiscRtnCode("暫停匯出");
								} else {
									// 'original value 'UI.STAT = RMOUT_FISC_RTN_CODE
								}
							}
						} else {
							_dtResult.get(i).setRmoutFiscRtnCode(getRMOUTStatusName(_dtResult.get(i).getRmoutStat()));
						}
					}					
				}
			}

			// 查詢總金額
			BigDecimal sumOfFeptxnTxAmt = new BigDecimal("0");
			for (RmoutExt rmout : _dtResult) {
				if (rmout != null && rmout.getRmoutTxamt() != null)
					sumOfFeptxnTxAmt = sumOfFeptxnTxAmt.add(rmout.getRmoutTxamt());
			}
			mode.addAttribute("sumOfFeptxnTxAmt", sumOfFeptxnTxAmt);
			mode.addAttribute("totalCount", _dtResult.size());

			PageInfo<RmoutExt> pageInfo = this.clientPaged(_dtResult, form.getPageNum(), form.getPageSize());
			PageData<UI_028110_Form, RmoutExt> pageData = new PageData<>(pageInfo, form);
			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);

		} catch (Exception ex) {
			_logContext.setProgramException(ex);
			_logContext.setRemark("UI_028110, GetRMOUT exception");
			logMessage(Level.INFO, _logContext);

			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
	}

	public void getMsgout(UI_028110_Form form, ModelMap mode) throws Exception {
		RmService rmsrv = new RmService();
		Msgout defMsgout = new Msgout();
		// 匯款日期
		// Modified by ChenLi on 2014/12/01 for 個資log紀錄查詢
		if (StringUtils.isNotBlank(form.getTradingDate())) {
			// LogSqlCommandText.Add("交易日=" & DATEtxt.Text)
			String tradingDate = StringUtils.replace(form.getTradingDate(), "-", StringUtils.EMPTY);
			defMsgout.setMsgoutTxdate(tradingDate);
		}

		// add by maxine on 2011/06/17 for 增加查詢條件
		// 匯款行
		if (StringUtils.isNotBlank(form.getSenderBankTxt())) {
			// LogSqlCommandText.Add("匯款行=" & SenderBankTxt.Text)
			defMsgout.setMsgoutSenderBank(form.getSenderBankTxt());
		}

		// 解款行
		if (StringUtils.isNotBlank(form.getSenderBankTxt())) {
			// LogSqlCommandText.Add("解款行=" & ReceiverBankTxt.Text)
			defMsgout.setMsgoutReceiverBank(form.getReceiverBankTxt());
		}

		// 根據 查詢類別 將 查詢序號 填到 財金電文序號 或 跨行通匯序號
		if (StringUtils.isNotBlank(form.getSnoTxt()) && !"0".equals(form.getSnoTxt())) {
			// LogSqlCommandText.Add("序號=" & SNOTxt.Text)
			// LogSqlCommandText.Add("查詢類別=" & KINDddl.SelectedItem.Text)
			switch (form.getKindddl()) {
				case "1":
					// Modify by Jim, 2011/03/04, 一般通訊幾乎是查當天, 應不會只查某一筆, 所以序號可以不用輸入
					defMsgout.setMsgoutFiscsno(StringUtils.leftPad(form.getSnoTxt(), 7, "0"));
					break;
				case "2":
				case "3":
					defMsgout.setMsgoutFepno(StringUtils.leftPad(form.getSnoTxt(), 7, "0"));
					break;
				case "4":
					defMsgout.setMsgoutStan(StringUtils.leftPad(form.getSnoTxt(), 7, "0"));
					break;
				case "5":
					defMsgout.setMsgoutEjno(Integer.parseInt(form.getSnoTxt()));
					break;
			}
		}
		if (!"".equals(form.getRmstatddl()) && !"00".equals(form.getRmstatddl())) {
			// LogSqlCommandText.Add("匯款狀態=" & RMSTATDDL.SelectedItem.Text)
			defMsgout.setMsgoutStat(form.getRmstatddl());
		}

		PageInfo<Msgout> pageInfo = rmService.getMsgOutByDef(defMsgout, form.getPageNum(), form.getPageSize());

		List<Msgout> _dtResult = pageInfo.getList();

		if (CollectionUtils.isEmpty(_dtResult)) {
			this.showMessage(mode, MessageType.INFO, QueryNoData);
			return;
		}

		// 查詢總金額
		// BigDecimal sumOfFeptxnTxAmt = new BigDecimal("0");
		String sumOfFeptxnTxAmt = "0";
		for (Msgout msgout : _dtResult) {
			sumOfFeptxnTxAmt = "0";
		}
		mode.addAttribute("sumOfFeptxnTxAmt", sumOfFeptxnTxAmt);
		mode.addAttribute("totalCount", _dtResult.size());

		for (int i = 0; i < _dtResult.size(); i++) {
			if ("0001".equals(_dtResult.get(i).getMsgoutFiscRtnCode())) {
				_dtResult.get(i).setMsgoutFiscRtnCode("OK");
			} else if ("05".equals(_dtResult.get(i).getMsgoutStat()) || "06".equals(_dtResult.get(i).getMsgoutStat())) {
				// UI.STAT = MSGOUT_FISC_RTN_CODE
			} else if ("04".equals(_dtResult.get(i).getMsgoutStat())) {
				_dtResult.get(i).setMsgoutFiscRtnCode("N REP");
			} else {
				_dtResult.get(i).setMsgoutFiscRtnCode(_dtResult.get(i).getMsgoutStat());
			}
			_dtResult.get(i).setMsgoutSupno2(SysStatus.getPropertyValue().getSysstatHbkno());
		}

		pageInfo.setList(_dtResult);
		PageData<UI_028110_Form, Msgout> pageData = new PageData<>(pageInfo, form);
		WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);

	}

	public void getRmin(UI_028110_Form form, ModelMap mode) {
		Rmin defRmin = new Rmin();
		boolean orderByFISCSNO = true;
		LogData _logContext = new LogData();
		try {
			// 'Modified by ChenLi on 2014/12/01 for 個資log紀錄查詢
			// '匯款日期
			if (StringUtils.isNotBlank(form.getTradingDate())) {
				// LogSqlCommandText.Add("交易日=" & DATEtxt.Text)
				String tradingDate = StringUtils.replace(form.getTradingDate(), "-", StringUtils.EMPTY);
				defRmin.setRminTxdate(tradingDate);
			}
			// 匯款種類
			if (StringUtils.isNotBlank(form.getRemtypeddl())) {
				// LogSqlCommandText.Add("匯款類別=" & REMTYPEddl.SelectedItem.Text)
				defRmin.setRminFiscSndCode(form.getRemtypeddl());
			}

			// add by maxine on 2011/06/17 for 增加查詢條件
			// 匯款行
			if (StringUtils.isNotBlank(form.getSenderBankTxt())) {
				// LogSqlCommandText.Add("匯款行=" & SenderBankTxt.Text)
				defRmin.setRminSenderBank(form.getSenderBankTxt());
			}

			// 解款行
			if (StringUtils.isNotBlank(form.getReceiverBankTxt())) {
				// LogSqlCommandText.Add("解款行=" & ReceiverBankTxt.Text)
				defRmin.setRminReceiverBank(form.getReceiverBankTxt());
			}

			// 根據 查詢類別 將 查詢序號 填到 財金電文序號 或 跨行通匯序號
			if (StringUtils.isNotBlank(form.getSnoTxt()) && !"0".equals(form.getSnoTxt())) {
				// LogSqlCommandText.Add("序號=" & SNOTxt.Text)
				// LogSqlCommandText.Add("查詢類別=" & KINDddl.SelectedItem.Text)
				switch (form.getKindddl()) {
					case "1":
						defRmin.setRminFiscsno(StringUtils.leftPad(form.getSnoTxt(), 7, "0"));
						break;
					case "2":
						orderByFISCSNO = false;
						defRmin.setRminRmsno(StringUtils.leftPad(form.getSnoTxt(), 7, "0"));
						break;
					case "3":
						defRmin.setRminFepno(StringUtils.leftPad(form.getSnoTxt(), 7, "0"));
						break;
					case "4":
						defRmin.setRminStan(StringUtils.leftPad(form.getSnoTxt(), 7, "0"));
						break;
					case "5":
						defRmin.setRminEjno1(Integer.parseInt(form.getSnoTxt()));
						break;
				}
			}
			if (!"".equals(form.getRmstatddl()) && !"00".equals(form.getRmstatddl())) {
				// LogSqlCommandText.Add("匯款狀態=" & RMSTATDDL.SelectedItem.Text)
				defRmin.setRminStat(form.getRmstatddl());
			}
			// 2021-12-17 Richard modified
			List<Rmin> _dtResult = rmService.queryRMINForUI028110(defRmin);

			// _dtResult = rmService.queryRMINForUI028110(defRmin);
			if (CollectionUtils.isNotEmpty(_dtResult)) {
				if (!"".equals(form.getTxamtTxt())) {
					List<Rmin> tmpDr = null;
					switch (form.getTxamtkindddl()) {
						case "1":
							tmpDr = _dtResult.stream().filter(item -> Integer.valueOf(item.getRminTxamt().toString()) > Integer.valueOf(form.getTxamtTxt())).collect(Collectors.toList());
							if (tmpDr.size() > 0) {
								_dtResult = _dtResult.stream().filter(item -> Integer.valueOf(item.getRminTxamt().toString()) > Integer.valueOf(form.getTxamtTxt())).collect(Collectors.toList());
							} else {
								_dtResult = null;
							}
							break;
						case "2":
							tmpDr = _dtResult.stream().filter(item -> Integer.valueOf(item.getRminTxamt().toString()) <= Integer.valueOf(form.getTxamtTxt())).collect(Collectors.toList());
							if (tmpDr.size() > 0) {
								_dtResult = _dtResult.stream().filter(item -> Integer.valueOf(item.getRminTxamt().toString()) <= Integer.valueOf(form.getTxamtTxt())).collect(Collectors.toList());
							} else {
								_dtResult = null;
							}
							break;
						case "3":
							tmpDr = _dtResult.stream().filter(item -> Integer.valueOf(form.getTxamtTxt()).equals(Integer.valueOf(item.getRminTxamt().toString()))).collect(Collectors.toList());
							if (tmpDr.size() > 0) {
								_dtResult = _dtResult.stream().filter(item -> Integer.valueOf(form.getTxamtTxt()).equals(Integer.valueOf(item.getRminTxamt().toString()))).collect(Collectors.toList());
							} else {
								_dtResult = null;
							}
							break;
					}
				}
			} else {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
				return;
			}
			if (CollectionUtils.isEmpty(_dtResult)) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
				return;
			} else {
				Allbank tmpALLBANK = new Allbank();
				if(_dtResult != null) {
					for (int i = 0; i < _dtResult.size(); i++) {
						if (_dtResult.get(i).getRminSenderBank() != null) {
							tmpALLBANK.setAllbankBkno(_dtResult.get(i).getRminSenderBank().substring(0, 3));
							tmpALLBANK.setAllbankBrno("000");
							Allbank aLLBANK = allbankExtMapper.selectByPrimaryKey(tmpALLBANK.getAllbankBkno(), tmpALLBANK.getAllbankBrno());
							if (aLLBANK != null) {
								//// UnioBank 用RminSupno1代替
								_dtResult.get(i).setRminSupno1(tmpALLBANK.getAllbankUnitBank());
							} else {
								_dtResult.get(i).setRminSupno1("000");
							}
						}
						if ("0001".equals(_dtResult.get(i).getRminFiscRtnCode())) {
							_dtResult.get(i).setRminFiscRtnCode("OK");
						} else {
							// original value
						}
					}					
				}
			}
			// 查詢總金額
			BigDecimal sumOfFeptxnTxAmt = new BigDecimal("0");
			for (Rmin rmin : _dtResult) {
				if (rmin != null && rmin.getRminTxamt() != null)
					sumOfFeptxnTxAmt = sumOfFeptxnTxAmt.add(rmin.getRminTxamt());
			}

			mode.addAttribute("sumOfFeptxnTxAmt", sumOfFeptxnTxAmt);
			mode.addAttribute("totalCount", _dtResult.size());

			PageInfo<Rmin> pageInfo = this.clientPaged(_dtResult, form.getPageNum(), form.getPageSize());
			PageData<UI_028110_Form, Rmin> pageData = new PageData<>(pageInfo, form);
			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		} catch (Exception ex) {
			_logContext.setProgramException(ex);
			_logContext.setRemark("UI_028110, GetRMIN exception");
			logMessage(Level.INFO, _logContext);

			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, ex.getMessage());
		}
	}

	public void getMsgin(UI_028110_Form form, ModelMap mode) throws Exception {
		Msgin defMsgin = new Msgin();
		RmService rmsrv = new RmService();
		// 'Modified by ChenLi on 2014/12/01 for 個資log紀錄查詢
		// '匯款日期
		if (StringUtils.isNotBlank(form.getTradingDate())) {
			// LogSqlCommandText.Add("交易日=" & DATEtxt.Text)
			String tradingDate = StringUtils.replace(form.getTradingDate(), "-", StringUtils.EMPTY);
			defMsgin.setMsginTxdate(tradingDate);
		}

		// add by maxine on 2011/06/17 for 增加查詢條件
		// 匯款行
		if (StringUtils.isNotBlank(form.getSenderBankTxt())) {
			// LogSqlCommandText.Add("匯款行=" & SenderBankTxt.Text)
			defMsgin.setMsginSenderBank(form.getSenderBankTxt());
		}
		// 解款行
		if (StringUtils.isNotBlank(form.getReceiverBankTxt())) {
			// LogSqlCommandText.Add("解款行=" & ReceiverBankTxt.Text)
			defMsgin.setMsginReceiverBank(form.getReceiverBankTxt());
		}

		// 根據 查詢類別 將 查詢序號 填到 財金電文序號 或 跨行通匯序號
		if (StringUtils.isNotBlank(form.getSnoTxt()) && !"0".equals(form.getSnoTxt())) {
			// LogSqlCommandText.Add("序號=" & SNOTxt.Text)
			// LogSqlCommandText.Add("查詢類別=" & KINDddl.SelectedItem.Text)
			switch (form.getKindddl()) {
				case "1":
					if (StringUtils.isNotBlank(form.getSnoTxt())) {
						defMsgin.setMsginFiscsno(StringUtils.leftPad(form.getSnoTxt(), 7, "0"));
					}
					break;
				case "2":
				case "3":
					defMsgin.setMsginFepno(StringUtils.leftPad(form.getSnoTxt(), 7, "0"));
					break;
				case "4":
					defMsgin.setMsginStan(StringUtils.leftPad(form.getSnoTxt(), 7, "0"));
					break;
				case "5":
					defMsgin.setMsginEjno(Integer.parseInt(form.getSnoTxt()));
					break;
			}
		}
		if (!"".equals(form.getRmstatddl()) && !"00".equals(form.getRmstatddl())) {
			// LogSqlCommandText.Add("匯款狀態=" & RMSTATDDL.SelectedItem.Text)
			defMsgin.setMsginStan(form.getRmstatddl());
		}

		PageInfo<Msgin> pageInfo = rmService.getMsgInByDef(defMsgin, form.getPageNum(), form.getPageSize());
		List<Msgin> _dtResult = pageInfo.getList();

		if (CollectionUtils.isEmpty(_dtResult)) {
			this.showMessage(mode, MessageType.INFO, QueryNoData);
			return;
		}

		// 查詢總金額
		// BigDecimal sumOfFeptxnTxAmt = new BigDecimal("0");
		String sumOfFeptxnTxAmt = "0";
		for (Msgin msgin : _dtResult) {
			sumOfFeptxnTxAmt = "0";
		}
		mode.addAttribute("sumOfFeptxnTxAmt", sumOfFeptxnTxAmt);
		mode.addAttribute("totalCount", _dtResult.size());

		// _dtResult = rmsrv.getMsgInByDef(defMsgin);

		for (int i = 0; i < _dtResult.size(); i++) {
			if ("0001".equals(_dtResult.get(i).getMsginFiscRtnCode())) {
				_dtResult.get(i).setMsginFiscRtnCode("OK");
			} else {
				// UI.STAT = MSGIN_FISC_RTN_CODE
			}
			_dtResult.get(i).setMsginSupno2(SysStatus.getPropertyValue().getSysstatFbkno());
		}

		pageInfo.setList(_dtResult);
		PageData<UI_028110_Form, Msgin> pageData = new PageData<>(pageInfo, form);
		WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);

	}

	public boolean checkAllField() {
		// 'Dim rtn As Boolean = True
		// ''有選擇匯款類別就必須填寫序號
		// 'If Not String.IsNullOrEmpty(REMTYPEddl.SelectedValue.Trim) And _
		// ' String.IsNullOrEmpty(SNOTxt.Text.ToString) AndAlso Me.REMTYPEddl.SelectedValue <> "14" Then
		// ' rtnMsg = rtnMsg & " 序號"
		// ' rtn = False
		// 'End If
		// 'Return rtn
		return true;
	}

	// Gridview 第一列查詢單筆明細
	@PostMapping(value = "/rm/UI_028110/inquiryDetail")
	public String doInquiryDetail(@ModelAttribute UI_028110_FormDetail form, ModelMap mode) {
		this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);

		boolean isFromFEPTXN = true;
		RM rmBusiness = new RM();
		if (!"14".equals(form.getRemtypeddl())) {
			if ("1".equals(form.getIoflag())) {
				try {
					List<Rmout> detailDt = null;
					List<Rmoute> detailDte = null;
					if ("1".equals(form.getTabSrc())) {
						Rmout defRMOUT = new Rmout();
						defRMOUT.setRmoutTxdate(form.getRmoutTxdate());
						defRMOUT.setRmoutOriginal(form.getRmoutOriginal());
						defRMOUT.setRmoutBrno(form.getRmoutBrno());
						defRMOUT.setRmoutFepno(form.getRmoutFepno());
						detailDt = rmService.getRmoutByDef(defRMOUT);
						for (Rmout dr : detailDt) {
							dr.setRmoutStat(rmBusiness.mapRMOUTStat(dr.getRmoutStat()));
							dr.setRmoutOriginal(rmBusiness.mapRMOUTOriginal(dr.getRmoutOriginal()));
							if (StringUtils.isNotBlank(dr.getRmoutBackReason())) {
								dr.setRmoutBackReason(rmBusiness.mapRMINBackReason(dr.getRmoutBackReason()));
							}
							if (StringUtils.isNotBlank(dr.getRmoutPending())) {
								dr.setRmoutPending(rmBusiness.mapRMPending(dr.getRmoutPending()));
							}
						}
						if (detailDt == null) {
							this.showMessage(mode, MessageType.DANGER, QueryFail);
						}

						RminExtMapper rminext = SpringBeanFactoryUtil.getBean(RminExtMapper.class);
						Rmin defRMIN = new Rmin();
						defRMIN.setRminTxdate(form.getRmoutOrgdate());
						defRMIN.setRminReceiverBank(form.getRmoutSenderBank().substring(0, 3));
						defRMIN.setRminFepno(form.getRmoutOrgregFepno());
						List<Rmin> iRes = rminext.getSingleRMIN(defRMIN);
						if (iRes.size() == 1) {
							String date = iRes.get(0).getRminSendtime();
							mode.addAttribute("ORG_RMIN_TIME", date);
						} else {

						}
						this.showMessage(mode, MessageType.INFO, DealSuccess);
						WebUtil.putInAttribute(mode, AttributeName.DetailEntity, detailDt.get(0));
						return Router.UI_028110_Detail.getView();
					} else {
						Rmoute defRMOUTE = new Rmoute();
						defRMOUTE.setRmouteTxdate(form.getRmoutTxdate());
						defRMOUTE.setRmouteOriginal(form.getRmoutOriginal());
						defRMOUTE.setRmouteBrno(form.getRmoutBrno());
						defRMOUTE.setRmouteFepno(form.getRmoutFepno());
						defRMOUTE.setRmouteFepsubno(form.getRmoutFepsubno());
						detailDte = rmService.getRMOUTEDataTableByPK(defRMOUTE);
						if (detailDte == null) {
							this.showMessage(mode, MessageType.DANGER, QueryFail);
						}
						for (Rmoute dr : detailDte) {
							dr.setRmouteStat(rmBusiness.mapRMOUTStat(dr.getRmouteStat()));
							dr.setRmouteOriginal(rmBusiness.mapRMOUTOriginal(dr.getRmouteOriginal()));
							if (StringUtils.isNotBlank(dr.getRmouteBackReason())) {
								dr.setRmouteBackReason(rmBusiness.mapRMINBackReason(dr.getRmouteBackReason()));
							}
							if (StringUtils.isNotBlank(dr.getRmoutePending())) {
								dr.setRmoutePending(rmBusiness.mapRMPending(dr.getRmoutePending()));
							}
						}

						RminExtMapper rminext = SpringBeanFactoryUtil.getBean(RminExtMapper.class);
						Rmin defRMIN = new Rmin();
						defRMIN.setRminTxdate(form.getRmoutOrgdate());
						defRMIN.setRminReceiverBank(form.getRmoutSenderBank().substring(0, 3));
						defRMIN.setRminFepno(form.getRmoutOrgregFepno());
						List<Rmin> iRes = rminext.getSingleRMIN(defRMIN);
						if (iRes.size() == 1) {
							String date = iRes.get(0).getRminSendtime();
							mode.addAttribute("ORG_RMIN_TIME", date);
						} else {

						}
						this.showMessage(mode, MessageType.INFO, DealSuccess);
						WebUtil.putInAttribute(mode, AttributeName.DetailEntity, detailDte.get(0));
						return Router.UI_028110_Detaile.getView();
					}
				} catch (Exception ex) {
					logContext.setRemark("查詢RMOUT明細發生例外, ex=" + ex.toString());
					logContext.setProgramException(ex);
					logMessage(Level.ERROR, getLogContext());
					this.showMessage(mode, MessageType.DANGER, QueryFail);
				}
			} else {
				try {
					List<Rmin> detailDt = null;
					Rmin defRMIN = new Rmin();
					defRMIN.setRminTxdate(form.getRminTxdate());
					defRMIN.setRminFepno(form.getRminFepno());
					defRMIN.setRminBrno(form.getRminBrno());
					detailDt = rmService.getRminByDef(defRMIN);
					if (detailDt == null) {
						this.showMessage(mode, MessageType.DANGER, QueryFail);
					}

					for (Rmin dr : detailDt) {
						dr.setRminStat(rmBusiness.mapRMINStat(dr.getRminStat()));
						if (StringUtils.isNotBlank(dr.getRminTmpStat())) {
							dr.setRminTmpStat(rmBusiness.mapRMINTMPStat(dr.getRminTmpStat()));
						}
						if (StringUtils.isNotBlank(dr.getRminBackReason())) {
							dr.setRminBackReason(rmBusiness.mapRMINBackReason(dr.getRminBackReason()));
						}
						if (StringUtils.isNotBlank(dr.getRminPending())) {
							dr.setRminPending(rmBusiness.mapRMPending(dr.getRminPending()));
						}
						// 2019/04/08 Modify by Ruling for 匯入檢核AML：匯入明細資料增加顯示欄位（RMIN_AMLSTAT、RMIN_AMLBypass）
						if (StringUtils.isNotBlank(dr.getRminAmlstat())) {
							dr.setRminAmlstat(dr.getRminAmlstat() + "-" + dr.getRminAmlstat());
						}
						if (StringUtils.isNotBlank(dr.getRminPending())) {
							dr.setRminAmlbypass(dr.getRminAmlbypass() + "-" + dr.getRminAmlbypass());
						}
					}

					Rmout defRMOUT = new Rmout();
					RmoutExtMapper dbRMOUT = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);
					defRMOUT.setRmoutOrgdate(detailDt.get(0).getRminTxdate());
					defRMOUT.setRmoutOrgregFepno(detailDt.get(0).getRminFepno());
					Rmout rmoutm = dbRMOUT.getSingleRMOUT(defRMOUT);
					if (rmoutm != null) {
						// 2020/10/05 modified by Carrie for Fortify修正:Cross-Site Scripting: Reflected
						mode.addAttribute("RMOUT_REMTYPE", rmoutm.getRmoutRemtype());
						mode.addAttribute("RMOUT_SENDTIME", rmoutm.getRmoutSendtime());
						mode.addAttribute("RMOUT_AMT_TYPE", rmoutm.getRmoutAmtType());
						mode.addAttribute("RMOUT_TXAMT", rmoutm.getRmoutTxamt());
						mode.addAttribute("RMOUT_SERVAMT_TYPE", rmoutm.getRmoutServamtType());
						mode.addAttribute("RMOUT_RECFEE", rmoutm.getRmoutRecfee());
						mode.addAttribute("RMOUT_ACTFEE", rmoutm.getRmoutActfee());
						mode.addAttribute("RMOUT_ORIGINAL", rmoutm.getRmoutOriginal());
					} else {

					}
					this.showMessage(mode, MessageType.INFO, DealSuccess);
					WebUtil.putInAttribute(mode, AttributeName.DetailEntity, detailDt.get(0));
					return Router.UI_028110_RminView.getView();
				} catch (Exception ex) {
					logContext.setRemark("查詢RMIN明細發生例外, ex=" + ex.toString());
					logContext.setProgramException(ex);
					logMessage(Level.ERROR, getLogContext());
					this.showMessage(mode, MessageType.DANGER, QueryFail);
				}
			}
		} else {
			// 一般通訊
			if ("1".equals(form.getIoflag())) {
				try {
					Msgout defMSGOUT = new Msgout();
					defMSGOUT.setMsgoutTxdate(form.getMsgoutTxdate());
					defMSGOUT.setMsgoutBrno(form.getMsgoutBrno());
					defMSGOUT.setMsgoutFepno(form.getMsgoutFepno());
					PageInfo<Msgout> detailDt = rmService.getMsgOutByDef(defMSGOUT, form.getPageNum(), form.getPageSize());
					if (detailDt.getList() == null) {
						this.showMessage(mode, MessageType.DANGER, QueryFail);
					}

					for (Msgout dr : detailDt.getList()) {
						dr.setMsgoutStat(rmBusiness.mapMSGOUTStat(dr.getMsgoutStat()));
						// modified by maxine on 2011/06/24 for SYSSTAT自行查
						dr.setMsgoutStan(SysStatus.getPropertyValue().getSysstatHbkno() + "-" + dr.getMsgoutStan());
						// 'dr("MSGOUT_STAN") = Configuration.SysStatus.PropertyValue.SYSSTAT_HBKNO & "-" & dr("MSGOUT_STAN").ToString
						// 'dr("RMOUT_ORIGINAL") = rmBusiness.MapRMOUTOriginal(dr("RMOUT_ORIGINAL").ToString)
						// 'dr("RMOUT_BACK_REASON") = rmBusiness.MapRMINBackReason(dr("RMOUT_BACK_REASON").ToString)
					}
					this.showMessage(mode, MessageType.INFO, DealSuccess);
					WebUtil.putInAttribute(mode, AttributeName.DetailEntity, detailDt.getList().get(0));
					return Router.UI_028110_MsgoutView.getView();
				} catch (Exception ex) {
					logContext.setRemark("查詢MSGOUT明細發生例外, ex=" + ex.toString());
					logContext.setProgramException(ex);
					logMessage(Level.ERROR, getLogContext());
					this.showMessage(mode, MessageType.DANGER, QueryFail);
				}
			} else {
				try {
					PageInfo<Msgin> detailDt = null;
					Msgin defMSGIN = new Msgin();
					defMSGIN.setMsginTxdate(form.getMsginTxdate());
					defMSGIN.setMsginBrno(form.getMsginBrno());
					defMSGIN.setMsginFepno(form.getMsginFepno());
					detailDt = rmService.getMsgInByDef(defMSGIN, form.getPageNum(), form.getPageSize());
					if (detailDt.getList() == null) {
						this.showMessage(mode, MessageType.DANGER, QueryFail);
					}
					for (Msgin dr : detailDt.getList()) {
						dr.setMsginStat(rmBusiness.mapMSGINStat(dr.getMsginStat()));
						// modified by maxine on 2011/06/24 for SYSSTAT自行查
						dr.setMsginStan(SysStatus.getPropertyValue().getSysstatFbkno() + "-" + dr.getMsginStan());
						// dr("MSGIN_STAN") = Configuration.SysStatus.PropertyValue.SYSSTAT_FBKNO & "-" & dr("MSGIN_STAN").ToString
					}
					this.showMessage(mode, MessageType.INFO, DealSuccess);
					WebUtil.putInAttribute(mode, AttributeName.DetailEntity, detailDt.getList().get(0));
					return Router.UI_028110_MsginView.getView();
				} catch (Exception ex) {
					logContext.setRemark("查詢MSGIN明細發生例外, ex=" + ex.toString());
					logContext.setProgramException(ex);
					logMessage(Level.ERROR, getLogContext());
					this.showMessage(mode, MessageType.DANGER, QueryFail);
				}
			}
		}
		return "";
	}

	// 查詢FEPLOG（UI_060550）
	@PostMapping(value = "/rm/UI_028110/inquiryLog")
	private String queryFEPLogClick(@ModelAttribute UI_028110_FormDetail form, ModelMap mode) throws Exception {
		Long ej1 = null;
		Long ej2 = null;
		Long ej3 = null;
		Long ej4 = null;
		Long ej5 = null;

		if (!form.getRmoutEjno1().equals("null")) {
			ej1 = Long.valueOf(form.getRmoutEjno1());
		}
		if (!form.getRmoutEjno2().equals("null")) {
			ej2 = Long.valueOf(form.getRmoutEjno2());
		}
		if (!form.getRmoutEjno3().equals("null")) {
			ej3 = Long.valueOf(form.getRmoutEjno3());
		}

		if ("".equals(ej1) && "".equals(ej2) && "".equals(ej3) && "".equals(ej4)) {
			this.showMessage(mode, MessageType.WARNING, "EJFNO沒值不能查詢");
		}

		List<Long> ejfnoList = new ArrayList<>();
		if (ej1 != null) {
			ejfnoList.add(ej1);
		}
		if (ej2 != null) {
			ejfnoList.add(ej2);
		}
		if (ej3 != null) {
			ejfnoList.add(ej3);
		}

		if (ej1 == null && ej2 == null && ej3 == null && ej4 == null) {
			this.showMessage(mode, MessageType.WARNING, "EJFNO沒值不能查詢");
		}
		Long feptxnTraceEjfno = null;

		PageInfo<Feplog> pageInfo = emsSvr.getFeplog_UI060550(ejfnoList, feptxnTraceEjfno, form.getRmoutTxdate(), form.getPageNum(), form.getPageSize());
		if (pageInfo.getSize() == 0) {
			this.showMessage(mode, MessageType.INFO, QueryNoData);
		}
		PageData<UI_028110_FormDetail, Feplog> pageData = new PageData<UI_028110_FormDetail, Feplog>(pageInfo, form);
		WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		return Router.UI_060610_A.getView();
	}

	@PostMapping(value = "/rm/UI_028110/inquiryLog1")
	private String queryFEPLogClick1(@ModelAttribute UI_028110_FormDetail form, ModelMap mode) throws Exception {

		Long ej1 = null;
		Long ej2 = null;
		Long ej3 = null;
		Long ej4 = null;
		Long ej5 = null;

		if (!form.getRminEjno1().equals("null")) {
			ej1 = Long.valueOf(form.getRminEjno1());
		}
		if (!form.getRminEjno2().equals("null")) {
			ej2 = Long.valueOf(form.getRminEjno2());
		}
		if (!form.getRminEjno3().equals("null")) {
			ej3 = Long.valueOf(form.getRminEjno3());
		}

		// Fly 2019/07/12 增加顯示AML LOG
		if (!form.getRminEjnoAml().equals("null")) {
			ej5 = Long.valueOf(form.getRminEjnoAml());
		}

		if (ej1 == null && ej2 == null && ej3 == null && ej4 == null) {
			this.showMessage(mode, MessageType.WARNING, "EJFNO沒值不能查詢");
		}

		List<Long> ejfnoList = new ArrayList<>();
		if (ej1 != null) {
			ejfnoList.add(ej1);
		}
		if (ej2 != null) {
			ejfnoList.add(ej2);
		}
		if (ej3 != null) {
			ejfnoList.add(ej3);
		}
		if (ej4 != null) {
			ejfnoList.add(ej4);
		}
		if (ej5 != null) {
			ejfnoList.add(ej5);
		}

		if (ej1 == null && ej2 == null && ej3 == null && ej4 == null) {
			this.showMessage(mode, MessageType.WARNING, "EJFNO沒值不能查詢");
		}
		Long feptxnTraceEjfno = null;

		PageInfo<Feplog> pageInfo = emsSvr.getFeplog_UI060550(ejfnoList, feptxnTraceEjfno, form.getRminTxdate(), form.getPageNum(), form.getPageSize());
		if (pageInfo.getSize() == 0) {
			this.showMessage(mode, MessageType.INFO, QueryNoData);
		}
		PageData<UI_028110_FormDetail, Feplog> pageData = new PageData<UI_028110_FormDetail, Feplog>(pageInfo, form);
		WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		return Router.UI_060610_A.getView();
	}

	@PostMapping(value = "/rm/UI_028110/inquiryLog2")
	private String queryFEPLogClick2(@ModelAttribute UI_028110_FormDetail form, ModelMap mode) throws Exception {

		Long ej1 = null;
		Long ej2 = null;
		Long ej3 = null;
		Long ej4 = null;
		Long ej5 = null;

		if (!form.getMsgoutEjno().equals("null")) {
			ej1 = Long.valueOf(form.getMsgoutEjno());
		}

		if (ej1 == null && ej2 == null && ej3 == null && ej4 == null) {
			this.showMessage(mode, MessageType.WARNING, "EJFNO沒值不能查詢");
		}

		List<Long> ejfnoList = new ArrayList<>();
		if (ej1 != null) {
			ejfnoList.add(ej1);
		}

		if (ej1 == null && ej2 == null && ej3 == null && ej4 == null) {
			this.showMessage(mode, MessageType.WARNING, "EJFNO沒值不能查詢");
		}

		Long feptxnTraceEjfno = null;

		PageInfo<Feplog> pageInfo = emsSvr.getFeplog_UI060550(ejfnoList, feptxnTraceEjfno, form.getMsgoutTxdate(), form.getPageNum(), form.getPageSize());
		if (pageInfo.getSize() == 0) {
			this.showMessage(mode, MessageType.INFO, QueryNoData);
		}
		PageData<UI_028110_FormDetail, Feplog> pageData = new PageData<UI_028110_FormDetail, Feplog>(pageInfo, form);
		WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		return Router.UI_060610_A.getView();
	}

	@PostMapping(value = "/rm/UI_028110/inquiryLog3")
	private String queryFEPLogClick3(@ModelAttribute UI_028110_FormDetail form, ModelMap mode) throws Exception {

		Long ej1 = null;
		Long ej2 = null;
		Long ej3 = null;
		Long ej4 = null;
		Long ej5 = null;

		if (!form.getMsginEjno().equals("null")) {
			ej1 = Long.valueOf(form.getMsginEjno());
		}

		if (ej1 == null && ej2 == null && ej3 == null && ej4 == null) {
			this.showMessage(mode, MessageType.WARNING, "EJFNO沒值不能查詢");
		}

		List<Long> ejfnoList = new ArrayList<>();
		if (ej1 != null) {
			ejfnoList.add(ej1);
		}

		if (ej1 == null && ej2 == null && ej3 == null && ej4 == null) {
			this.showMessage(mode, MessageType.WARNING, "EJFNO沒值不能查詢");
		}

		Long feptxnTraceEjfno = null;

		PageInfo<Feplog> pageInfo = emsSvr.getFeplog_UI060550(ejfnoList, feptxnTraceEjfno, form.getMsginTxdate(), form.getPageNum(), form.getPageSize());
		if (pageInfo.getSize() == 0) {
			this.showMessage(mode, MessageType.INFO, QueryNoData);
		}
		PageData<UI_028110_FormDetail, Feplog> pageData = new PageData<UI_028110_FormDetail, Feplog>(pageInfo, form);
		WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		return Router.UI_060610_A.getView();
	}

	// 查詢EJ序號單筆明細
	private String getDetailData(@ModelAttribute UI_028110_FormDetail form, ModelMap mode) {
		this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);

		Rmout defrmout = new Rmout();
		Rmin defrmin = new Rmin();
		Msgout defmsgout = new Msgout();
		Msgin defmsgin = new Msgin();
		String MsgID = "";
		String PCode = "";
		try {
			if (false) {
				List<Rmout> dt = null;
				List<Rmin> dt1 = null;
				PageInfo<Msgout> dt2 = null;
				PageInfo<Msgin> dt3 = null;
				if (!"".equals(MsgID)) {
					switch (MsgID) {
						case "R1000":
						case "RT1301":
						case "BTOUTBATCH":
							RmoutExtMapper dbRMOUT = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);
							// 2020/10/08 modified by Carrie for Fortify修正:Cross-Site Scripting: Reflected
							// todo
							// defrmout.setRmoutTxdate();

							dt = rmService.getRmoutByDef(defrmout);
							form.setIoflag("匯出");
							break;
						case "RT1101":
							// todo
							// RmoutExtMapper dbRMOUT = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);
							// defrmout.setRmoutTxdate();
							// defrmout.setRmoutEjno3();
							dt = rmService.getRmoutByDef(defrmout);
							form.setIoflag("匯出");
							break;
						case "R2300":
						case "BTINBATCH":
							RminExtMapper dbRMIN = SpringBeanFactoryUtil.getBean(RminExtMapper.class);
							// defrmin.setRminTxdate();
							// defrmin.setRminEjno3();
							dt1 = dbRMIN.getRminByDef(defrmin);
							form.setIoflag("匯入");
							break;
						case "R2400":
							RminExtMapper dbRMIN1 = SpringBeanFactoryUtil.getBean(RminExtMapper.class);
							// defrmin.setRminTxdate();
							// defrmin.setRminEjno4();
							dt1 = rmService.getRminByDef(defrmin);
							form.setIoflag("匯入");
							break;
					}
				} else if (!"".equals(PCode)) {
					switch (PCode) {
						case "1111":
						case "1121":
						case "1131":
						case "1171":
						case "1181":
						case "1191":
							RmoutExtMapper dbRMOUT = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);
							// defrmout.setRmoutTxdate();
							// defrmout.setRmoutEjno1();
							dt = rmService.getRmoutByDef(defrmout);
							form.setIoflag("匯出");
							break;
						case "1112":
						case "1122":
						case "1132":
						case "1172":
						case "1182":
						case "1192":
							RminExtMapper dbRMIN1 = SpringBeanFactoryUtil.getBean(RminExtMapper.class);
							// defrmin.setRminTxdate();
							// defrmin.setRminEjno1();
							dt1 = rmService.getRminByDef(defrmin);
							form.setIoflag("匯入");
							break;
						case "1412":
							MsginExtMapper dbMSGIN = SpringBeanFactoryUtil.getBean(MsginExtMapper.class);
							// defmsgin.setMsginTxdate();
							// defmsgin.setMsginEjno();
							dt3 = rmService.getMsgInByDef(defmsgin, form.getPageNum(), form.getPageSize());
							form.setRemtypeddl("14");
							form.setIoflag("匯入");
							break;
						case "1411":
							MsgoutExtMapper dbMSGOUT = SpringBeanFactoryUtil.getBean(MsgoutExtMapper.class);
							// defmsgout.setMsgoutTxdate();
							// defmsgout.setMsgoutEjno();
							dt2 = rmService.getMsgOutByDef(defmsgout, form.getPageNum(), form.getPageSize());
							form.setRemtypeddl("14");
							form.setIoflag("匯入");
							break;
					}
				}
				// todo
			} else {
				getDetailData(form, mode);
			}
		} catch (Exception ex) {
			getDetailData(form, mode);
		}
		return "";
	}
}
