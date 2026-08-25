package com.syscom.fep.web.controller.atmmon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.atmmon.UI_060290_Form;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;

/**
 * FEP 服務/通路/線路控制
 *
 * @author Han
 */
@Controller
public class UI_060290Controller extends BaseController {

	@Autowired
	private InbkService inbkService;

	@Autowired
	private AtmService atmService;

	@Autowired
	private SysstatExtMapper sysstatExtMapper;

	@Override
	public void pageOnLoad(ModelMap mode) {
		this.showMessage(mode, MessageType.INFO, "");

		// 初始化表單資料
		UI_060290_Form form = new UI_060290_Form();

		if (!setSYSSTAT(mode, form)) {
			this.showMessage(mode, MessageType.DANGER, "無系統狀態資料, 請查閱資料庫");
		}
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/atmmon/UI_060290/confirm")
	public String doInquiryMain(@ModelAttribute UI_060290_Form form, ModelMap mode) {
		this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		String strLogSQL = "";

		try {
			Sysstat defSYSSTAT = new Sysstat();
			Sysstat dtSYSSTAT = atmService.getStatus();

			defSYSSTAT = atmService.getStatus();
			// 設定HBKNO, 更新資料庫使用 form.getSysstat().getSysstatHbkno()
			defSYSSTAT.setSysstatHbkno(form.getHbkno());
			// 1-1:更新自行20
//			defSYSSTAT.setSysstatIntra(DbHelper.toShort(form.isSysstatIntra()));
//			defSYSSTAT.setSysstatIwdI(DbHelper.toShort(form.isSysstatIwdI()));
//			defSYSSTAT.setSysstatIftI(DbHelper.toShort(form.isSysstatIftI()));
//			defSYSSTAT.setSysstatAdmI(DbHelper.toShort(form.isSysstatAdmI()));

			// 2016/02/15 Added By Nick SU For ARPC
//			defSYSSTAT.setSysstatIiqI(DbHelper.toShort(form.isSysstatIiqI()));
//			defSYSSTAT.setSysstatFwdI(DbHelper.toShort(form.isSysstatFwdI()));

			// 2017/01/25 Added By Nick SU For 跨行提款
//			defSYSSTAT.setSysstatNwdI(DbHelper.toShort(form.isSysstatNwdI()));

			// 2018/10/11 added by Ashiang for 外幣無卡提款
//			defSYSSTAT.setSysstatNfwI(DbHelper.toShort(form.isSysstatNfwI()));
//			defSYSSTAT.setSysstatIpyI(DbHelper.toShort(form.isSysstatIpyI()));
//			defSYSSTAT.setSysstatIccdpI(DbHelper.toShort(form.isSysstatIccdpI()));
//			defSYSSTAT.setSysstatEtxI(DbHelper.toShort(form.isSysstatEtxI()));
//			defSYSSTAT.setSysstatCaI(DbHelper.toShort(form.isSysstatCaI()));
//			defSYSSTAT.setSysstatCaaI(DbHelper.toShort(form.isSysstatCaaI()));

			// 2010/6/18 Add by Kitty for 「自行」增加欄位
			// 2013/11/14, ChenLi, 增加SYSSTAT_HK_FISCMQ、SYSSTAT_MO_FISCMQ欄位
//			defSYSSTAT.setSysstatAig(DbHelper.toShort(form.isSysstatAig()));
//			defSYSSTAT.setSysstatHkIssue(DbHelper.toShort(form.isSysstatHkIssue()));
//			defSYSSTAT.setSysstatMoIssue(DbHelper.toShort(form.isSysstatMoIssue()));
//			defSYSSTAT.setSysstatHkFiscmb(DbHelper.toShort(form.isSysstatHkFiscmb()));
//			defSYSSTAT.setSysstatHkFiscmq(DbHelper.toShort(form.isSysstatHkFiscmq()));
//			defSYSSTAT.setSysstatMoFiscmb(DbHelper.toShort(form.isSysstatMoFiscmb()));
//			defSYSSTAT.setSysstatMoFiscmq(DbHelper.toShort(form.isSysstatMoFiscmq()));
//			defSYSSTAT.setSysstatHkPlus(DbHelper.toShort(form.isSysstatHkPlus()));
//			defSYSSTAT.setSysstatMoPlus(DbHelper.toShort(form.isSysstatMoPlus()));

			// 1-2:更新代理23
//			defSYSSTAT.setSysstatAgent(DbHelper.toShort(form.isSysstatAgent()));
//			defSYSSTAT.setSysstatIwdA(DbHelper.toShort(form.isSysstatIwdA()));

			// ChenLi, 2015/03/02, 加入代理外幣提款控制
//			defSYSSTAT.setSysstatFawA(DbHelper.toShort(form.isSysstatFawA()));

//			if ((DbHelper.toBoolean(dtSYSSTAT.getSysstatIftA()) && !form.isSysstatIftA())
//					|| (!DbHelper.toBoolean(dtSYSSTAT.getSysstatIftA()) && form.isSysstatIftA())) {
//
//				// Fly 2017/08/23 有異動代理行轉帳FLAG，需檢查預約跨轉批次是否執行中
//
//				Batch defBATCH = inbkService.getSingleBATCHByDef("INBK_RETFR");
//				if ("0".equals(defBATCH.getBatchResult())) {// 0:執行中
//					this.showMessage(mode, MessageType.WARNING, "預約跨行轉帳交易批次處理執行中，無法變更代理行轉帳FLAG");
//					setSYSSTAT(mode, form);
//					return Router.UI_060290.getView();
//				}
//
//				defBATCH = inbkService.getSingleBATCHByDef("INBK_RETFR_RERUN");
//				if ("0".equals(defBATCH.getBatchResult())) {// 0:執行中
//					this.showMessage(mode, MessageType.WARNING, "預約跨行轉帳交易批次處理(重跑交易結果失敗的資料)執行中，無法變更代理行轉帳FLAG");
//					setSYSSTAT(mode, form);
//					return Router.UI_060290.getView();
//				}
//			}

//			defSYSSTAT.setSysstatIftA(DbHelper.toShort(form.isSysstatIftA()));

			// 2016/08/12 Added By Nick SU For ARPC start
//			defSYSSTAT.setSysstatAdmA(DbHelper.toShort(form.isSysstatAdmA()));
//			defSYSSTAT.setSysstatIpyA(DbHelper.toShort(form.isSysstatIpyA()));
//			defSYSSTAT.setSysstatIccdpA(DbHelper.toShort(form.isSysstatIccdpA()));
//			defSYSSTAT.setSysstatEtxA(DbHelper.toShort(form.isSysstatEtxA()));
			// 2016/02/15 Added By Nick SU For ARPC end

			// 2017/10/24 Modify by Ruling for 勾選代理行全國繳費(ID+ACC)，檢核整批轉即時相關批次，如為執行中不允許變更
//			if ((DbHelper.toBoolean(dtSYSSTAT.getSysstatCdpA()) && !form.isSysstatCdpA())
//					|| (!DbHelper.toBoolean(dtSYSSTAT.getSysstatCdpA()) && form.isSysstatCdpA())) {
//
//				Batch defBATCH = inbkService.getSingleBATCHByDef("INBK_NPS2262_01");
//				if ("0".equals(defBATCH.getBatchResult())) {// 0:執行中
//					this.showMessage(mode, MessageType.WARNING, "整批轉即時_批次1執行中，無法變更代理行全國繳費(ID+ACC) FLAG");
//					setSYSSTAT(mode, form);
//					return Router.UI_060290.getView();
//				}
//
//				defBATCH = inbkService.getSingleBATCHByDef("INBK_NPS2262_02");
//				if ("0".equals(defBATCH.getBatchResult())) {// 0:執行中
//					this.showMessage(mode, MessageType.WARNING, "整批轉即時_批次2執行中，無法變更代理行全國繳費(ID+ACC) FLAG");
//					setSYSSTAT(mode, form);
//					return Router.UI_060290.getView();
//				}
//
//				defBATCH = inbkService.getSingleBATCHByDef("INBK_NPS2262_03");
//				if ("0".equals(defBATCH.getBatchResult())) {// 0:執行中
//					this.showMessage(mode, MessageType.WARNING, "整批轉即時_批次3執行中，無法變更代理行全國繳費(ID+ACC) FLAG");
//					setSYSSTAT(mode, form);
//					return Router.UI_060290.getView();
//				}
//
//				defBATCH = inbkService.getSingleBATCHByDef("INBK_NPS2262_1A");
//				if ("0".equals(defBATCH.getBatchResult())) {// 0:執行中
//					this.showMessage(mode, MessageType.WARNING, "整批轉即時_批次1A執行中，無法變更代理行全國繳費(ID+ACC) FLAG");
//					setSYSSTAT(mode, form);
//					return Router.UI_060290.getView();
//				}
//
//				defBATCH = inbkService.getSingleBATCHByDef("INBK_NPS2262_2A");
//				if ("0".equals(defBATCH.getBatchResult())) {// 0:執行中
//					this.showMessage(mode, MessageType.WARNING, "整批轉即時_批次2A執行中，無法變更代理行全國繳費(ID+ACC) FLAG");
//					setSYSSTAT(mode, form);
//					return Router.UI_060290.getView();
//				}
//
//				defBATCH = inbkService.getSingleBATCHByDef("INBK_NPS2262_3A");
//				if ("0".equals(defBATCH.getBatchResult())) {// 0:執行中
//					this.showMessage(mode, MessageType.WARNING, "整批轉即時_批次3A執行中，無法變更代理行全國繳費(ID+ACC) FLAG");
//					setSYSSTAT(mode, form);
//					return Router.UI_060290.getView();
//				}
//			}

//			defSYSSTAT.setSysstatCdpA(DbHelper.toShort(form.isSysstatCdpA()));
//			defSYSSTAT.setSysstat2525A(DbHelper.toShort(form.isSysstat2525A()));
//			defSYSSTAT.setSysstatCpuA(DbHelper.toShort(form.isSysstatCpuA()));
//			defSYSSTAT.setSysstatCafA(DbHelper.toShort(form.isSysstatCafA())); // 2010/6/18 Moidfy by Kitty for
																				// 「代理行」增加欄位
//			defSYSSTAT.setSysstatCavA(DbHelper.toShort(form.isSysstatCavA()));
//			defSYSSTAT.setSysstatCamA(DbHelper.toShort(form.isSysstatCamA()));

			// defSYSSTAT.setSysstatCajA(DbHelper.toShort(form.isSysstatCajA()));
			// //2016/10/20 Moidfy by Nick for 剔除代理行JCB預借現金交易
//			defSYSSTAT.setSysstatCauA(DbHelper.toShort(form.isSysstatCauA()));
//			defSYSSTAT.setSysstatCwvA(DbHelper.toShort(form.isSysstatCwvA()));
//			defSYSSTAT.setSysstatCwmA(DbHelper.toShort(form.isSysstatCwmA()));

//			defSYSSTAT.setSysstatCamA(DbHelper.toShort(form.isSysstatCamA()));
//			defSYSSTAT.setSysstatCamA(DbHelper.toShort(form.isSysstatCamA()));

//			defSYSSTAT.setSysstatCamA(DbHelper.toShort(form.isSysstatHkFiscmb()));
//			defSYSSTAT.setSysstatCamA(DbHelper.toShort(form.isSysstatHkPlus()));
//			defSYSSTAT.setSysstatCamA(DbHelper.toShort(form.isSysstatMoFiscmb()));
//			defSYSSTAT.setSysstatCamA(DbHelper.toShort(form.isSysstatMoPlus()));

			// 2015/01/27 Added By Nick 增加EMV
//			defSYSSTAT.setSysstatEafA(DbHelper.toShort(form.isSysstatEafA()));

			// 2016/07/19 Added By Nick SU For EMV
//			defSYSSTAT.setSysstatEavA(DbHelper.toShort(form.isSysstatEavA()));
//			defSYSSTAT.setSysstatEamA(DbHelper.toShort(form.isSysstatEamA()));
//			defSYSSTAT.setSysstatEwvA(DbHelper.toShort(form.isSysstatEwvA()));
//			defSYSSTAT.setSysstatEwmA(DbHelper.toShort(form.isSysstatEwmA()));
//			defSYSSTAT.setSysstatNwdA(DbHelper.toShort(form.isSysstatNwdA()));// Fly 2018/01/31 增加代理行無卡提款
//			defSYSSTAT.setSysstatVaaA(DbHelper.toShort(form.isSysstatVaaA()));// Fly 2019/03/25 增加代理2566約定及核驗項目

			// 1-3:更新原存15
//			defSYSSTAT.setSysstatIssue(DbHelper.toShort(form.isSysstatIssue()));
//			defSYSSTAT.setSysstatIwdF(DbHelper.toShort(form.isSysstatIwdF()));
//			defSYSSTAT.setSysstatIftF(DbHelper.toShort(form.isSysstatIftF()));
//			defSYSSTAT.setSysstatIiqF(DbHelper.toShort(form.isSysstatIiqF())); // 2016/02/15 Added By Nick SU For ARPC
//			defSYSSTAT.setSysstatIpyF(DbHelper.toShort(form.isSysstatIpyF()));
//			defSYSSTAT.setSysstatIccdpF(DbHelper.toShort(form.isSysstatIccdpF()));
//			defSYSSTAT.setSysstatCdpF(DbHelper.toShort(form.isSysstatCdpF()));
//			defSYSSTAT.setSysstatEtxF(DbHelper.toShort(form.isSysstatEtxF()));
//			defSYSSTAT.setSysstat2525F(DbHelper.toShort(form.isSysstat2525F()));
//			defSYSSTAT.setSysstatCpuF(DbHelper.toShort(form.isSysstatCpuF()));
//			defSYSSTAT.setSysstatGpcadF(DbHelper.toShort(form.isSysstatGpcadF())); // 2010/6/18 Moidfy by Kitty for
																					// 「原存行」增加欄位
//			defSYSSTAT.setSysstatCauF(DbHelper.toShort(form.isSysstatCauF()));
//			defSYSSTAT.setSysstatGpcwdF(DbHelper.toShort(form.isSysstatGpcwdF()));
//			defSYSSTAT.setSysstatGpemvF(DbHelper.toShort(form.isSysstatGpemvF())); // 2016/4/19 Moidfy by Nick for EMV
																					// Plus/Cirrus國際提款
//			defSYSSTAT.setSysstatGpiwdF(DbHelper.toShort(form.isSysstatGpiwdF())); // 2017/5/23 Moidfy by Nick for
																					// 晶片卡跨國提款
//			defSYSSTAT.setSysstatGpobF(DbHelper.toShort(form.isSysstatGpobF()));
//			defSYSSTAT.setSysstatVaaF(DbHelper.toShort(form.isSysstatVaaF()));
//			defSYSSTAT.setSysstatHkIssue(DbHelper.toShort(form.isSysstatHkIssue()));
//			defSYSSTAT.setSysstatMoIssue(DbHelper.toShort(form.isSysstatMoIssue()));

			// 2016/02/15 Added By Nick SU For ARPC
			// 2016/07/26 Added By Nick SU For EMV
			// 1-4:純代理12
//			defSYSSTAT.setSysstatPure(DbHelper.toShort(form.isSysstatPure()));
//			defSYSSTAT.setSysstatIiqP(DbHelper.toShort(form.isSysstatIiqP()));
//			defSYSSTAT.setSysstatIftP(DbHelper.toShort(form.isSysstatIftP()));
//			defSYSSTAT.setSysstatIccdpP(DbHelper.toShort(form.isSysstatIccdpP()));
//			defSYSSTAT.setSysstatCdpP(DbHelper.toShort(form.isSysstatCdpP()));
//			defSYSSTAT.setSysstatIpyP(DbHelper.toShort(form.isSysstatIpyP()));
//			defSYSSTAT.setSysstatIqvP(DbHelper.toShort(form.isSysstatIqvP()));
//			defSYSSTAT.setSysstatIqmP(DbHelper.toShort(form.isSysstatIqmP()));
//			defSYSSTAT.setSysstatIqcP(DbHelper.toShort(form.isSysstatIqcP()));
//			defSYSSTAT.setSysstatEquP(DbHelper.toShort(form.isSysstatEquP()));
//			defSYSSTAT.setSysstatEqpP(DbHelper.toShort(form.isSysstatEqpP()));
//			defSYSSTAT.setSysstatEqcP(DbHelper.toShort(form.isSysstatEqcP()));

			// ChenLi, 2013/06/20, For NCB上線增加控制T24主機(台灣、香港、澳門)
			// 1-5:更新T24主機3
			defSYSSTAT.setSysstatT24Twn(DbHelper.toShort(form.isSysstatT24Twn()));
//			defSYSSTAT.setSysstatT24Hkg(DbHelper.toShort(form.isSysstatT24Hkg()));

			if (form.isSysstatT24Mac()) {
//				defSYSSTAT.setSysstatT24Mac(DbHelper.toShort(form.isSysstatT24Mac()));
			}

			// 2:更新其他通道
//			defSYSSTAT.setSysstatEqcP(DbHelper.toShort(form.isSysstatCbs()));
//			defSYSSTAT.setSysstatEqcP(DbHelper.toShort(form.isSysstatFedi()));
//			defSYSSTAT.setSysstatEqcP(DbHelper.toShort(form.isSysstatNb()));
//			defSYSSTAT.setSysstatEqcP(DbHelper.toShort(form.isSysstatWebatm()));
//			defSYSSTAT.setSysstatEqcP(DbHelper.toShort(form.isSysstatAscChannel()));
//			defSYSSTAT.setSysstatEqcP(DbHelper.toShort(form.isSysstatAsc()));
//			defSYSSTAT.setSysstatEqcP(DbHelper.toShort(form.isSysstatAscmd()));
//			defSYSSTAT.setSysstatEqcP(DbHelper.toShort(form.isSysstatGcard()));
//			defSYSSTAT.setSysstatEqcP(DbHelper.toShort(form.isSysstatSps()));
//			defSYSSTAT.setSysstatEqcP(DbHelper.toShort(form.isSysstatAscmac()));
//			defSYSSTAT.setSysstatEqcP(DbHelper.toShort(form.isSysstatSpsmac()));
//			defSYSSTAT.setSysstatEqcP(DbHelper.toShort(form.isSysstatSvcs()));
//			defSYSSTAT.setSysstatEqcP(DbHelper.toShort(form.isSysstatHkSms())); // Fly 2014/10/24 增加香港SMS通道
//			defSYSSTAT.setSysstatEqcP(DbHelper.toShort(form.isSysstatPv())); // Fly 2020/10/15 增加掌靜脈通道
//			defSYSSTAT.setSysstatEqcP(DbHelper.toShort(form.isSysstatSmtp())); // 2021/01/29 Modify by Ruling for
																				// 手機門號跨行轉帳：增加手機門號中台

			// 2010/09/07 modified by Ruling for INSERT一筆異動SYSSTAT的LOG到SyscomAuditTrail
			defSYSSTAT.setUpdateUserid(Integer.valueOf(WebUtil.getUser().getUserId()));
			defSYSSTAT.setUpdateTime(new Date());

			// add by Maxine on 2011/07/25 for 需顯示交易成功訊息於EMS
//			List<List<Object>> aryNeedSendEMS = prepareNeedSendEMSList(form);

			// ChenLi, 2014/05/05, 啟動/暫停自行服務才需更新EAINET

			Sysstat dt = inbkService.getStatus();

			if (null == dt) {
				logContext.setRemark("Query SYSSTAT Fail!");
				inbkService.inbkLogMessage(Level.INFO, logContext);
			}

			// todo 麗惠:不確定合庫或上海商銀有沒這需求, 就先點掉
			if (UpdateSYSSTAT(defSYSSTAT) == 1) {
//
//				sendOKEMS(aryNeedSendEMS); //add by Maxine on 2011/07/25 for 需顯示交易成功訊息於EMS
//
//                //2020/10/08 Modify by Ruling for Fortify：修正Null Dereference問題
//				if( null != drSYSSTAT && (DbHelper.toBoolean(drSYSSTAT.getSysstatIntra()) != form.isSysstatIntra())) {
//
//					//Modified by ChenLi on 2014/04/30 for SPEC修改：網銀優化
//
//					Boolean atmstopFlag = true;
//
//					if(DbHelper.toBoolean(drSYSSTAT.getSysstatIntra()) == false) {
//
//					}
//				}
//
			} else {
				this.showMessage(mode, MessageType.DANGER, UpdateFail);
			}

			// 2017/10/31 Modify by Ruling for 查詢重新讀取Cache
			FEPCache.reloadCache(CacheItem.SYSSTAT);
			this.showMessage(mode, MessageType.INFO, UpdateSuccess);

		} catch (Exception e) {

			// 'modified by ChenLi on 2014/05/02 for 增加紀錄發生Exception的SQL Script
			// 'modified By Maxine on 2011/09/14 for 出現Exception需要顯示詳細信息
			// 'QueryStatusBar.ShowMessage(UpdateFail, StatusBar.MessageType.ErrMsg)
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);

			LogData logContext = new LogData();

			if (StringUtils.isBlank(strLogSQL)) {
				logContext.setRemark(e + "SQLCommand-" + strLogSQL);
			}else {
				logContext.setRemark(e+"");
			}

			logContext.setMessageId("UI_060290");
			logContext.setMessageGroup("1");
			logContext.setProgramName("UI_060290");
			logContext.setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			logContext.setChannel(FEPChannel.FEP);
			inbkService.inbkLogMessage(Level.INFO, logContext);
		}

		return Router.UI_060290.getView();
	}

	/**
	 * <summary> 取得系統目前狀態。 </summary> <remarks> </remarks>
	 *
	 * @param mode
	 * @param form
	 *
	 * @return
	 */
	private boolean setSYSSTAT(ModelMap mode, UI_060290_Form form) {

		try {

			Sysstat dtSysstat = inbkService.getStatus();

			if (dtSysstat == null) {
				return false;
			}

			form.setSysstat(new Sysstat());

			if (dtSysstat != null) {

				// 存入取得的HBKNO, 以便更新使用
				form.getSysstat().setSysstatHbkno(dtSysstat.getSysstatHbkno().toString());
				form.setHbkno(dtSysstat.getSysstatHbkno().toString());

				// 1-1:自行21
//				form.setSysstatIntra(DbHelper.toBoolean(dtSysstat.getSysstatIntra()));
//				form.setSysstatIwdI(DbHelper.toBoolean(dtSysstat.getSysstatIwdI()));
//				form.setSysstatIftI(DbHelper.toBoolean(dtSysstat.getSysstatIftI()));
//				form.setSysstatAdmI(DbHelper.toBoolean(dtSysstat.getSysstatAdmI()));

				// 2016/02/15 Added By Nick SU For ARPC
//				form.setSysstatIiqI(DbHelper.toBoolean(dtSysstat.getSysstatIiqI()));
//				form.setSysstatFwdI(DbHelper.toBoolean(dtSysstat.getSysstatFwdI()));

				// 2017/01/25 Added By Nick SU For 跨行提款
//				form.setSysstatNwdI(DbHelper.toBoolean(dtSysstat.getSysstatNwdI()));
//				form.setSysstatIpyI(DbHelper.toBoolean(dtSysstat.getSysstatIpyI()));
//				form.setSysstatIccdpI(DbHelper.toBoolean(dtSysstat.getSysstatIccdpI()));
//				form.setSysstatEtxI(DbHelper.toBoolean(dtSysstat.getSysstatEtxI()));
//				form.setSysstatCaI(DbHelper.toBoolean(dtSysstat.getSysstatCaI()));
//				form.setSysstatCaaI(DbHelper.toBoolean(dtSysstat.getSysstatCaaI()));

				// 2018/10/11 Add by Ashiang for 「外幣無卡提款」增加欄位
//				form.setSysstatNfwI(DbHelper.toBoolean(dtSysstat.getSysstatNfwI()));

				// 2010/6/18 Add by Kitty for 「自行」增加欄位
				// 2013/11/14, ChenLi, 增加SYSSTAT_HK_FISCMQ、SYSSTAT_MO_FISCMQ欄位
//				form.setSysstatAig(DbHelper.toBoolean(dtSysstat.getSysstatAig()));
//				form.setSysstatHkIssue(DbHelper.toBoolean(dtSysstat.getSysstatHkIssue()));
//				form.setSysstatMoIssue(DbHelper.toBoolean(dtSysstat.getSysstatMoIssue()));
//				form.setSysstatHkFiscmb(DbHelper.toBoolean(dtSysstat.getSysstatHkFiscmb()));
//				form.setSysstatHkFiscmq(DbHelper.toBoolean(dtSysstat.getSysstatHkFiscmq()));
//				form.setSysstatHkFiscmb(DbHelper.toBoolean(dtSysstat.getSysstatHkFiscmb()));
//				form.setSysstatMoFiscmb(DbHelper.toBoolean(dtSysstat.getSysstatMoFiscmb()));
//				form.setSysstatMoFiscmb(DbHelper.toBoolean(dtSysstat.getSysstatMoFiscmb()));

//				form.setSysstatHkPlus(DbHelper.toBoolean(dtSysstat.getSysstatHkPlus()));
//				form.setSysstatMoPlus(DbHelper.toBoolean(dtSysstat.getSysstatMoPlus()));

				// 1-2:代理21
//				form.setSysstatAgent(DbHelper.toBoolean(dtSysstat.getSysstatAgent()));
//				form.setSysstatIwdA(DbHelper.toBoolean(dtSysstat.getSysstatIwdA()));

				// ChenLi, 2015/03/02, 加入代理外幣提款控制
//				form.setSysstatFawA(DbHelper.toBoolean(dtSysstat.getSysstatFawA()));
//				form.setSysstatIftA(DbHelper.toBoolean(dtSysstat.getSysstatIftA()));

				// 2016/08/12 Added By Nick SU For 跨行存款
//				form.setSysstatAdmA(DbHelper.toBoolean(dtSysstat.getSysstatAdmA()));
//				form.setSysstatIpyA(DbHelper.toBoolean(dtSysstat.getSysstatIpyA()));
//				form.setSysstatIccdpA(DbHelper.toBoolean(dtSysstat.getSysstatIccdpA()));
//				form.setSysstatEtxA(DbHelper.toBoolean(dtSysstat.getSysstatEtxA()));

				// 2016/02/15 Added By Nick SU For ARPC
//				form.setSysstatCdpA(DbHelper.toBoolean(dtSysstat.getSysstatCdpA()));
//				form.setSysstat2525A(DbHelper.toBoolean(dtSysstat.getSysstat2525A()));
//				form.setSysstatCpuA(DbHelper.toBoolean(dtSysstat.getSysstatCpuA()));

				// form.setSysstatEtxA(DbHelper.toBoolean(dtSysstat.getSysstatEtxA()));2010/6/18
				// Moidfy by Kitty for 「代理行」增加欄位
//				form.setSysstatCafA(DbHelper.toBoolean(dtSysstat.getSysstatCafA()));
//				form.setSysstatCavA(DbHelper.toBoolean(dtSysstat.getSysstatCavA()));
//				form.setSysstatCamA(DbHelper.toBoolean(dtSysstat.getSysstatCamA()));
//				form.setSysstatCajA(DbHelper.toBoolean(dtSysstat.getSysstatCajA()));

				// Moidfy by Nick for 剔除代理行JCB預借現金交易
//				form.setSysstatCauA(DbHelper.toBoolean(dtSysstat.getSysstatCauA()));
//				form.setSysstatCwvA(DbHelper.toBoolean(dtSysstat.getSysstatCwvA()));
//				form.setSysstatCwmA(DbHelper.toBoolean(dtSysstat.getSysstatCwmA()));

				// 2015/01/27 Added By Nick 增加EMV
//				form.setSysstatEafA(DbHelper.toBoolean(dtSysstat.getSysstatEafA()));

				// 2016/07/19 Added By Nick SU For EMV
//				form.setSysstatEavA(DbHelper.toBoolean(dtSysstat.getSysstatEavA()));
//				form.setSysstatEamA(DbHelper.toBoolean(dtSysstat.getSysstatEamA()));
//				form.setSysstatEwvA(DbHelper.toBoolean(dtSysstat.getSysstatEwvA()));
//				form.setSysstatEwmA(DbHelper.toBoolean(dtSysstat.getSysstatEwmA()));

				// Fly 2018/01/31 增加代理行無卡提款
//				form.setSysstatNwdA(DbHelper.toBoolean(dtSysstat.getSysstatNwdA()));

				// Fly 2019/03/25 增加代理2566約定及核驗項目
//				form.setSysstatVaaA(DbHelper.toBoolean(dtSysstat.getSysstatVaaA()));
				//20220912 Bruce add 依照Candy需求增加 Start
//				form.setSysstatEajA(DbHelper.toBoolean(dtSysstat.getSysstatEajA()));// EMV預借現金(JCB)
				//20220912 Bruce add 依照Candy需求增加 end

				// 1-3:原存15
//				form.setSysstatIssue(DbHelper.toBoolean(dtSysstat.getSysstatIssue()));
//				form.setSysstatIwdF(DbHelper.toBoolean(dtSysstat.getSysstatIwdF()));
//				form.setSysstatIftF(DbHelper.toBoolean(dtSysstat.getSysstatIftF()));

				// 2016/02/15 Added By Nick SU For ARPC
//				form.setSysstatIiqF(DbHelper.toBoolean(dtSysstat.getSysstatIiqF()));
//				form.setSysstatIpyF(DbHelper.toBoolean(dtSysstat.getSysstatIpyF()));
//				form.setSysstatIccdpF(DbHelper.toBoolean(dtSysstat.getSysstatIccdpF()));
//				form.setSysstatCdpF(DbHelper.toBoolean(dtSysstat.getSysstatCdpF()));
//				form.setSysstatEtxF(DbHelper.toBoolean(dtSysstat.getSysstatEtxF()));
//				form.setSysstat2525F(DbHelper.toBoolean(dtSysstat.getSysstat2525F()));
//				form.setSysstatCpuF(DbHelper.toBoolean(dtSysstat.getSysstatCpuF()));
//				form.setSysstatGpcadF(DbHelper.toBoolean(dtSysstat.getSysstatGpcadF()));
//				form.setSysstatCauF(DbHelper.toBoolean(dtSysstat.getSysstatCauF()));
//				form.setSysstatGpcwdF(DbHelper.toBoolean(dtSysstat.getSysstatGpcwdF()));

				// 2016/4/19 Moidfy by Nick for EMV Plus/Cirrus國際提款
//				form.setSysstatGpemvF(DbHelper.toBoolean(dtSysstat.getSysstatGpemvF()));

				// 2017/5/23 Moidfy by Nick for 晶片卡跨國提款
//				form.setSysstatGpiwdF(DbHelper.toBoolean(dtSysstat.getSysstatGpiwdF()));
//				form.setSysstatGpobF(DbHelper.toBoolean(dtSysstat.getSysstatGpobF()));
//				form.setSysstatVaaF(DbHelper.toBoolean(dtSysstat.getSysstatVaaF()));

				// 2016/02/15 Added By Nick SU For ARPC
				// 2016/07/26 Added By Nick SU For EMV
				// 1-4:純代理12
//				form.setSysstatPure(DbHelper.toBoolean(dtSysstat.getSysstatPure()));
//				form.setSysstatIiqP(DbHelper.toBoolean(dtSysstat.getSysstatIiqP()));
//				form.setSysstatIftP(DbHelper.toBoolean(dtSysstat.getSysstatIftP()));
//				form.setSysstatIccdpP(DbHelper.toBoolean(dtSysstat.getSysstatIccdpP()));
//				form.setSysstatCdpP(DbHelper.toBoolean(dtSysstat.getSysstatCdpP()));

//				form.setSysstatIpyP(DbHelper.toBoolean(dtSysstat.getSysstatIpyP()));
//				form.setSysstatIqvP(DbHelper.toBoolean(dtSysstat.getSysstatIqvP()));
//				form.setSysstatIqmP(DbHelper.toBoolean(dtSysstat.getSysstatIqmP()));
//				form.setSysstatIqcP(DbHelper.toBoolean(dtSysstat.getSysstatIqcP()));
//				form.setSysstatEquP(DbHelper.toBoolean(dtSysstat.getSysstatEquP()));
//				form.setSysstatEqpP(DbHelper.toBoolean(dtSysstat.getSysstatEqpP()));
//				form.setSysstatEqcP(DbHelper.toBoolean(dtSysstat.getSysstatEqcP()));

				// ChenLi, 2013/06/20, For NCB上線增加控制T24主機(台灣、香港、澳門)
				// 1-5:T24主機
				form.setSysstatT24Twn(DbHelper.toBoolean(dtSysstat.getSysstatT24Twn()));
//				form.setSysstatT24Hkg(DbHelper.toBoolean(dtSysstat.getSysstatT24Hkg()));

//				if ("U".equals(dtSysstat.getSysstatCbsMac().toString())){
//					form.setSysstatT24Mac(false);
//				} else {
//					form.setSysstatT24Mac(DbHelper.toBoolean(dtSysstat.getSysstatT24Mac()));
//				}

				// 2:其他通道
				form.setSysstatCbs(DbHelper.toBoolean(dtSysstat.getSysstatCbs()));
//				form.setSysstatFedi(DbHelper.toBoolean(dtSysstat.getSysstatFedi()));
//				form.setSysstatNb(DbHelper.toBoolean(dtSysstat.getSysstatNb()));
//				form.setSysstatWebatm(DbHelper.toBoolean(dtSysstat.getSysstatWebatm()));
//				form.setSysstatAscChannel(DbHelper.toBoolean(dtSysstat.getSysstatAscChannel()));
//				form.setSysstatAsc(DbHelper.toBoolean(dtSysstat.getSysstatAsc()));
//				form.setSysstatAscmd(DbHelper.toBoolean(dtSysstat.getSysstatAscmd()));
//				form.setSysstatGcard(DbHelper.toBoolean(dtSysstat.getSysstatGcard()));
//				form.setSysstatSps(DbHelper.toBoolean(dtSysstat.getSysstatSps()));
//				form.setSysstatAscmac(DbHelper.toBoolean(dtSysstat.getSysstatAscmac()));
//				form.setSysstatSpsmac(DbHelper.toBoolean(dtSysstat.getSysstatSpsmac()));
//				form.setSysstatSvcs(DbHelper.toBoolean(dtSysstat.getSysstatSvcs()));

				// Fly 2014/10/24 增加香港SMS通道
//				form.setSysstatHkSms(DbHelper.toBoolean(dtSysstat.getSysstatHkSms()));

				// Fly 2020/10/15 增加掌靜脈通道
//				form.setSysstatPv(DbHelper.toBoolean(dtSysstat.getSysstatPv()));

				// 2021/01/29 Modify by Ruling for 手機門號跨行轉帳：增加手機門號中台
//				form.setSysstatSmtp(DbHelper.toBoolean(dtSysstat.getSysstatSmtp()));
				this.showMessage(mode, MessageType.INFO, QuerySuccess);
				return true;
			}

			// 2017/10/31 Modify by Ruling for 查詢重新讀取Cache
//			FEPCache.reloadCache(CacheItem.SYSSTAT);
//
//			WebUtil.putInAttribute(mode, AttributeName.Options, form);

		} catch (Exception e) {
			//20220912 Bruce Modify 畫面顯示的錯誤訊息改成 程式出現錯誤！！請洽資訊人員 Start
			this.errorMessage(e, e.getMessage());
			//showMessage(mode, MessageType.DANGER, FEPReturnCode.QueryFail);
			this.showMessage(mode, MessageType.DANGER, programError);
			//20220912 Bruce Modify 畫面顯示的錯誤訊息改成 程式出現錯誤！！請洽資訊人員 end
		}
		return false;
	}

//	private void sendOKEMS(List<List<Object>> aryNeedSendEMS) {
//
//		LogData logContext = new LogData();
//		logContext.setChannel(FEPChannel.FEP);
//		logContext.setSubSys(SubSystem.INBK); // 2012/06/18 Modify by Ruling for EMS日誌查詢能依子系統做查詢
//		logContext.setProgramName("UI_060290");
//		logContext.setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
//		logContext.setMessage("UI_060290");
//		logContext.setMessageGroup("1"); /* OPC */
//
//		// add By Maxine on 2011/09/02 For EMS加上UserID
//		// modified by Maxine on 2011/12/13 for 用FEPUSER_LOGONID代替FEPUSER_TLRNO
//		logContext.setTxUser(WebUtil.getUser().getUserId());
//
//		for (List<Object> itemNeedSendEMS : aryNeedSendEMS) {
//			logContext.setMessageParm13(itemNeedSendEMS.get(0).toString());
//			logContext.setRemark(
//					TxHelper.getMessageFromFEPReturnCode((FEPReturnCode) itemNeedSendEMS.get(1), logContext));
//			inbkService.inbkLogMessage(Level.INFO, logContext);
//		}
//	}

	/**
	 * add by Maxine on 2011/07/25 for 需顯示交易成功訊息於EMS
	 *
	 * @param form
	 *
	 * @return List<List<Object>>
	 */
//	private List<List<Object>> prepareNeedSendEMSList(UI_060290_Form form) {
//
//		List<List<Object>> aryNeedSendEMS = new ArrayList();
//		List<Object> itemNeedSendEMS = null;
//		AtmService obj = new AtmService();
//
//		try {
//			Sysstat dt = obj.getStatus();
//			Sysstat drSYSSTAT = null;
//
//			if (null == dt) {
//				return aryNeedSendEMS;
//			} else {
//				drSYSSTAT = new Sysstat();
//				drSYSSTAT = dt;
//			}
//
//			// 1-1:自行20
//			if (form.isSysstatIntra() != DbHelper.toBoolean(drSYSSTAT.getSysstatIntra())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("自行交易");
//
//				if (form.isSysstatIntra()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIwdI() != DbHelper.toBoolean(drSYSSTAT.getSysstatIwdI())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("自行提款交易");
//
//				if (form.isSysstatIwdI()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIftI() != DbHelper.toBoolean(drSYSSTAT.getSysstatIftI())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("自行轉帳交易");
//
//				if (form.isSysstatIftI()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatAdmI() != DbHelper.toBoolean(drSYSSTAT.getSysstatAdmI())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("自行存款交易");
//
//				if (form.isSysstatAdmI()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// 2016/02/15 Added By Nick SU For ARPC
//			if (form.isSysstatIiqI() != DbHelper.toBoolean(drSYSSTAT.getSysstatIiqI())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("自行查詢");
//
//				if (form.isSysstatIiqI()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatFwdI() != DbHelper.toBoolean(drSYSSTAT.getSysstatFwdI())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("自行外幣提款交易");
//
//				if (form.isSysstatFwdI()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatNwdI() != DbHelper.toBoolean(drSYSSTAT.getSysstatNwdI())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("無卡提款交易");
//
//				if (form.isSysstatNwdI()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatNfwI() != DbHelper.toBoolean(drSYSSTAT.getSysstatNfwI())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("外幣無卡提款交易");
//
//				if (form.isSysstatNfwI()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIpyI() != DbHelper.toBoolean(drSYSSTAT.getSysstatIpyI())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("自行繳款-本行客戶(跨行)交易");
//
//				if (form.isSysstatIpyI()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIccdpI() != DbHelper.toBoolean(drSYSSTAT.getSysstatIccdpI())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("自行晶片全國繳費交易");
//
//				if (form.isSysstatIccdpI()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatEtxI() != DbHelper.toBoolean(drSYSSTAT.getSysstatEtxI())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("自行全國繳稅-本行客戶(跨行)交易");
//
//				if (form.isSysstatEtxI()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatCaI() != DbHelper.toBoolean(drSYSSTAT.getSysstatCaI())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("自行預借現金交易");
//
//				if (form.isSysstatCaI()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatCaaI() != DbHelper.toBoolean(drSYSSTAT.getSysstatCaaI())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("自行AE預借現金交易");
//
//				if (form.isSysstatCaaI()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatAig() != DbHelper.toBoolean(drSYSSTAT.getSysstatAig())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("南山人壽保單繳款交易");
//
//				if (form.isSysstatAig()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatHkIssue() != DbHelper.toBoolean(drSYSSTAT.getSysstatHkIssue())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("香港分行原存行交易");
//
//				if (form.isSysstatHkIssue()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatMoIssue() != DbHelper.toBoolean(drSYSSTAT.getSysstatMoIssue())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("澳門分行原存行交易");
//
//				if (form.isSysstatMoIssue()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatHkFiscmb() != DbHelper.toBoolean(drSYSSTAT.getSysstatHkFiscmb())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("香港分行財金提款交易"); // ChenLi, 2013/11/14, 修改說明 For SPEC修改
//
//				if (form.isSysstatHkFiscmb()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// ChenLi, 2013/11/14, 新增判斷 For SPEC修改
//			if (form.isSysstatHkFiscmq() != DbHelper.toBoolean(drSYSSTAT.getSysstatHkFiscmq())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("香港分行財金餘額查詢交易");
//
//				if (form.isSysstatHkFiscmq()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatMoFiscmb() != DbHelper.toBoolean(drSYSSTAT.getSysstatMoFiscmb())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("澳門分行財金提款交易");// ChenLi, 2013/11/14, 修改說明 For SPEC修改
//
//				if (form.isSysstatMoFiscmb()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// 'ChenLi, 2013/11/14, 新增判斷 For SPEC修改
//			if (form.isSysstatMoFiscmq() != DbHelper.toBoolean(drSYSSTAT.getSysstatMoFiscmq())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("澳門分行財金餘額查詢交易");
//
//				if (form.isSysstatMoFiscmq()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatHkPlus() != DbHelper.toBoolean(drSYSSTAT.getSysstatHkPlus())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("香港分行PLUS交易");
//
//				if (form.isSysstatHkPlus()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatHkPlus() != DbHelper.toBoolean(drSYSSTAT.getSysstatHkPlus())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("香港分行PLUS交易");
//
//				if (form.isSysstatHkPlus()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatMoPlus() != DbHelper.toBoolean(drSYSSTAT.getSysstatMoPlus())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("澳門分行PLUS交易");
//
//				if (form.isSysstatMoPlus()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// 1-2:代理23
//			if (form.isSysstatAgent() != DbHelper.toBoolean(drSYSSTAT.getSysstatAgent())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("代理行交易");
//
//				if (form.isSysstatAgent()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIwdA() != DbHelper.toBoolean(drSYSSTAT.getSysstatIwdA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("代理台幣提款交易");
//
//				if (form.isSysstatIwdA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatFawA() != DbHelper.toBoolean(drSYSSTAT.getSysstatFawA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("代理外幣提款交易");
//
//				if (form.isSysstatFawA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// ChenLi, 2015/03/02, 加入代理外幣提款控制
//			if (form.isSysstatIftA() != DbHelper.toBoolean(drSYSSTAT.getSysstatIftA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("代理轉帳交易");
//
//				if (form.isSysstatIftA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// 2016/08/12 Added By Nick SU For 跨行存款
//			if (form.isSysstatAdmA() != DbHelper.toBoolean(drSYSSTAT.getSysstatAdmA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("跨行存款交易");
//
//				if (form.isSysstatAdmA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatAdmA() != DbHelper.toBoolean(drSYSSTAT.getSysstatAdmA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("跨行存款交易");
//
//				if (form.isSysstatAdmA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIpyA() != DbHelper.toBoolean(drSYSSTAT.getSysstatIpyA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("代理繳款交易");
//
//				if (form.isSysstatIpyA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIccdpA() != DbHelper.toBoolean(drSYSSTAT.getSysstatIccdpA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("代理晶片全國繳費交易");
//
//				if (form.isSysstatIccdpA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatEtxA() != DbHelper.toBoolean(drSYSSTAT.getSysstatEtxA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("代理全國繳稅交易");
//
//				if (form.isSysstatEtxA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// 2016/02/15 Added By Nick SU For ARPC
//			if (form.isSysstatCdpA() != DbHelper.toBoolean(drSYSSTAT.getSysstatCdpA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("代理全國繳費(ID+ACC)");
//
//				if (form.isSysstatCdpA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstat2525A() != DbHelper.toBoolean(drSYSSTAT.getSysstat2525A())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("代理消費扣款(固定費率)交易");
//
//				if (form.isSysstat2525A()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatCafA() != DbHelper.toBoolean(drSYSSTAT.getSysstatCafA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("代理國際提款-銀聯卡交易");
//
//				if (form.isSysstatCafA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatCavA() != DbHelper.toBoolean(drSYSSTAT.getSysstatCavA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("代理VISA預借現金交易");
//
//				if (form.isSysstatCavA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatCamA() != DbHelper.toBoolean(drSYSSTAT.getSysstatCamA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("理MASTER預借現金交易");
//
//				if (form.isSysstatCamA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
////			'2016/10/20 Moidfy by Nick for 剔除代理行JCB預借現金交易
////	        'If SYSSTAT_CAJ_A.Checked <> Convert.ToBoolean(drSYSSTAT("SYSSTAT_CAJ_A")) Then
////	        '    itemNeedSendEMS = New ArrayList()
////	        '    itemNeedSendEMS.Add("代理JCB預借現金交易")
////
////	        '    If SYSSTAT_CAJ_A.Checked Then
////	        '        itemNeedSendEMS.Add(CommonReturnCode.ChannelServiceStart)
////	        '    Else
////	        '        itemNeedSendEMS.Add(CommonReturnCode.ChannelServiceStop)
////	        '    End If
////
////	        '    aryNeedSendEMS.Add(itemNeedSendEMS)
////	        'End If
//
//			if (form.isSysstatCauA() != DbHelper.toBoolean(drSYSSTAT.getSysstatCauA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("代理預授交易交易");
//
//				if (form.isSysstatCauA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatCwvA() != DbHelper.toBoolean(drSYSSTAT.getSysstatCwvA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("代理國際提款PLUS交易");
//
//				if (form.isSysstatCwvA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatCwmA() != DbHelper.toBoolean(drSYSSTAT.getSysstatCwmA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("代理國際提款CIRRUS交易");
//
//				if (form.isSysstatCwmA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// 2016/01/27 Added By Nick 增加EMV
//			if (form.isSysstatEafA() != DbHelper.toBoolean(drSYSSTAT.getSysstatEafA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("EMV國際提款_銀聯卡");
//
//				if (form.isSysstatEafA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// 2016/07/20 Added By Nick for EMV
//			if (form.isSysstatEwvA() != DbHelper.toBoolean(drSYSSTAT.getSysstatEwvA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("EMV國際提款(PLUS)");
//
//				if (form.isSysstatEwvA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatEwmA() != DbHelper.toBoolean(drSYSSTAT.getSysstatEwmA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("EMV國際提款(CIRRUS)");
//
//				if (form.isSysstatEwmA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatEavA() != DbHelper.toBoolean(drSYSSTAT.getSysstatEavA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("EMV預借現金(VISA)");
//
//				if (form.isSysstatEavA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatEamA() != DbHelper.toBoolean(drSYSSTAT.getSysstatEamA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("EMV預借現金(MASTER)");
//
//				if (form.isSysstatEamA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// Fly 2018/01/31 增加代理行無卡提款
//			if (form.isSysstatNwdA() != DbHelper.toBoolean(drSYSSTAT.getSysstatNwdA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("無卡跨行提款");
//
//				if (form.isSysstatNwdA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// Fly 2019/03/25 增加代理2566約定及核驗項目
//			if (form.isSysstatVaaA() != DbHelper.toBoolean(drSYSSTAT.getSysstatVaaA())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("代理約定及核驗項目");
//
//				if (form.isSysstatVaaA()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// 1-3:原存15
//			if (form.isSysstatIssue() != DbHelper.toBoolean(drSYSSTAT.getSysstatIssue())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("原存交易");
//
//				if (form.isSysstatIssue()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIwdF() != DbHelper.toBoolean(drSYSSTAT.getSysstatIwdF())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("原存提款交易");
//
//				if (form.isSysstatIwdF()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIftF() != DbHelper.toBoolean(drSYSSTAT.getSysstatIftF())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("原存轉帳交易");
//
//				if (form.isSysstatIftF()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// 2016/02/15 Added By Nick SU For ARPC
//			if (form.isSysstatIiqF() != DbHelper.toBoolean(drSYSSTAT.getSysstatIiqF())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("原存查詢");
//
//				if (form.isSysstatIiqF()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIpyF() != DbHelper.toBoolean(drSYSSTAT.getSysstatIpyF())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("原存繳款交易");
//
//				if (form.isSysstatIpyF()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIccdpF() != DbHelper.toBoolean(drSYSSTAT.getSysstatIccdpF())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("原存晶片全國繳費交易");
//
//				if (form.isSysstatIccdpF()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatCdpF() != DbHelper.toBoolean(drSYSSTAT.getSysstatCdpF())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("ID+ACT原存全國繳費交易");
//
//				if (form.isSysstatCdpF()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatEtxF() != DbHelper.toBoolean(drSYSSTAT.getSysstatEtxF())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("原存全國繳稅交易");
//
//				if (form.isSysstatEtxF()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstat2525F() != DbHelper.toBoolean(drSYSSTAT.getSysstat2525F())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("原存消費扣款(固定費率)交易");
//
//				if (form.isSysstat2525F()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatCpuF() != DbHelper.toBoolean(drSYSSTAT.getSysstatCpuF())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("原存消費扣款(變動費率)交易");
//
//				if (form.isSysstatCpuF()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatGpcadF() != DbHelper.toBoolean(drSYSSTAT.getSysstatGpcadF())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("原存預借現金VISA/MASTER交易");
//
//				if (form.isSysstatGpcadF()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatCauF() != DbHelper.toBoolean(drSYSSTAT.getSysstatCauF())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("原存預授交易");
//
//				if (form.isSysstatCauF()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatGpcwdF() != DbHelper.toBoolean(drSYSSTAT.getSysstatGpcwdF())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("原存國際提款PLUS/CIRRUS交易");
//
//				if (form.isSysstatGpcwdF()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// 2016/04/19 Added By Nick SU For EMV Plus/Cirrus國際提款
//			if (form.isSysstatGpemvF() != DbHelper.toBoolean(drSYSSTAT.getSysstatGpemvF())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("EMV國際提款PLUS/CIRRUS");
//
//				if (form.isSysstatGpemvF()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// 2017/5/23 Moidfy by Nick for 晶片卡跨國提款
//			if (form.isSysstatGpiwdF() != DbHelper.toBoolean(drSYSSTAT.getSysstatGpiwdF())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("晶片卡跨國提款");
//
//				if (form.isSysstatGpiwdF()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatGpobF() != DbHelper.toBoolean(drSYSSTAT.getSysstatGpobF())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("跨境支付交易");
//
//				if (form.isSysstatGpobF()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatVaaF() != DbHelper.toBoolean(drSYSSTAT.getSysstatVaaF())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("原存約定及核驗項目"); // Fly 2019/03/25 修正名稱
//
//				if (form.isSysstatVaaF()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// '2016/02/15 Added By Nick SU For ARPC
//			// '2016/07/26 Added By Nick SU For EMV
//			// '1.4:純代理14
//			if (form.isSysstatPure() != DbHelper.toBoolean(drSYSSTAT.getSysstatPure())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("純代理交易");
//
//				if (form.isSysstatPure()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIiqP() != DbHelper.toBoolean(drSYSSTAT.getSysstatIiqP())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("純代理查詢");
//
//				if (form.isSysstatIiqP()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIftP() != DbHelper.toBoolean(drSYSSTAT.getSysstatIftP())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("純代理轉帳");
//
//				if (form.isSysstatIftP()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIccdpP() != DbHelper.toBoolean(drSYSSTAT.getSysstatIccdpP())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("純代理晶片全國繳費");
//
//				if (form.isSysstatIccdpP()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatCdpP() != DbHelper.toBoolean(drSYSSTAT.getSysstatCdpP())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("純代理全國繳費(ID+ACC)");
//
//				if (form.isSysstatCdpP()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIpyP() != DbHelper.toBoolean(drSYSSTAT.getSysstatIpyP())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("純代理繳款");
//
//				if (form.isSysstatIpyP()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIqvP() != DbHelper.toBoolean(drSYSSTAT.getSysstatIqvP())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("PLUS卡餘額查詢");
//
//				if (form.isSysstatIqvP()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIqmP() != DbHelper.toBoolean(drSYSSTAT.getSysstatIqmP())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("CIRRUS卡餘額查詢");
//
//				if (form.isSysstatIqmP()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatIqcP() != DbHelper.toBoolean(drSYSSTAT.getSysstatIqcP())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("銀聯卡餘額查詢");
//
//				if (form.isSysstatIqcP()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatEquP() != DbHelper.toBoolean(drSYSSTAT.getSysstatEquP())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("EMV 銀聯卡餘額查詢");
//
//				if (form.isSysstatEquP()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatEqpP() != DbHelper.toBoolean(drSYSSTAT.getSysstatEqpP())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("EMV PLUS卡餘額查詢");
//
//				if (form.isSysstatEqpP()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatEqcP() != DbHelper.toBoolean(drSYSSTAT.getSysstatEqcP())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("EMV CIRRUS卡餘額查詢");
//
//				if (form.isSysstatEqcP()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// 'ChenLi, 2013/06/20, For NCB上線增加控制T24主機(台灣、香港、澳門)
//			// '1.5:T24主機
//			if (form.isSysstatT24Twn() != DbHelper.toBoolean(drSYSSTAT.getSysstatT24Twn())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("台灣T24主機");
//
//				if (form.isSysstatT24Twn()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatT24Hkg() != DbHelper.toBoolean(drSYSSTAT.getSysstatT24Hkg())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("香港T24主機");
//
//				if (form.isSysstatT24Hkg()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatT24Mac() != DbHelper.toBoolean(drSYSSTAT.getSysstatT24Mac())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("澳門T24主機");
//
//				if (form.isSysstatT24Mac()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// 2:其他通道
//			if (form.isSysstatCbs() != DbHelper.toBoolean(drSYSSTAT.getSysstatCbs())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("CBS-T24");
//
//				if (form.isSysstatCbs()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatFedi() != DbHelper.toBoolean(drSYSSTAT.getSysstatFedi())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("FEDI");
//
//				if (form.isSysstatFedi()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatNb() != DbHelper.toBoolean(drSYSSTAT.getSysstatNb())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("網銀");
//
//				if (form.isSysstatNb()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatWebatm() != DbHelper.toBoolean(drSYSSTAT.getSysstatWebatm())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("WEB ATM服務");
//
//				if (form.isSysstatWebatm()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatAscChannel() != DbHelper.toBoolean(drSYSSTAT.getSysstatAscChannel())) {
//				itemNeedSendEMS = new ArrayList();
//
//				// 2021/01/29 Modify by Ruling for 手機門號跨行轉帳：調整顯示説明
//				// itemNeedSendEMS.Add("永豐信用卡通道")
//				itemNeedSendEMS.add("永豐信用卡");
//
//				if (form.isSysstatAscChannel()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatAsc() != DbHelper.toBoolean(drSYSSTAT.getSysstatAsc())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("永豐信用卡線路");
//
//				if (form.isSysstatAsc()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatAscmd() != DbHelper.toBoolean(drSYSSTAT.getSysstatAscmd())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("永豐錢卡線路");
//
//				if (form.isSysstatAscmd()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatGcard() != DbHelper.toBoolean(drSYSSTAT.getSysstatGcard())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("永豐GIFT卡線路");
//
//				if (form.isSysstatGcard()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatSps() != DbHelper.toBoolean(drSYSSTAT.getSysstatSps())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("永豐證券線路");
//
//				if (form.isSysstatSps()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatAscmac() != DbHelper.toBoolean(drSYSSTAT.getSysstatAscmac())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("與ASC壓碼線路");
//
//				if (form.isSysstatAscmac()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatSpsmac() != DbHelper.toBoolean(drSYSSTAT.getSysstatSpsmac())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("與SPS壓碼線路");
//
//				if (form.isSysstatSpsmac()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			if (form.isSysstatSvcs() != DbHelper.toBoolean(drSYSSTAT.getSysstatSvcs())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("悠遊卡"); // 2021/01/29 Modify by Ruling for 手機門號跨行轉帳：調整顯示説明
//				// itemNeedSendEMS.Add("悠遊卡通道")
//
//				if (form.isSysstatSvcs()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// Fly 2020/10/15 增加掌靜脈通道
//			if (form.isSysstatHkSms() != DbHelper.toBoolean(drSYSSTAT.getSysstatHkSms())) {
//				itemNeedSendEMS = new ArrayList();
//
//				// 2021/01/29 Modify by Ruling for 手機門號跨行轉帳：調整顯示説明
//				itemNeedSendEMS.add("香港SMS");
//				// itemNeedSendEMS.Add("香港SMS通道")
//
//				if (form.isSysstatHkSms()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// Fly 2014/10/24 增加掌靜脈通道
//			if (form.isSysstatPv() != DbHelper.toBoolean(drSYSSTAT.getSysstatPv())) {
//				itemNeedSendEMS = new ArrayList();
//
//				// 2021/01/29 Modify by Ruling for 手機門號跨行轉帳：調整顯示説明
//				itemNeedSendEMS.add("掌靜脈");
//				// itemNeedSendEMS.Add("掌靜脈通道")
//
//				if (form.isSysstatPv()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//			// 2021/01/29 Modify by Ruling for 手機門號跨行轉帳：增加手機門號中台
//			if (form.isSysstatSmtp() != DbHelper.toBoolean(drSYSSTAT.getSysstatSmtp())) {
//				itemNeedSendEMS = new ArrayList();
//				itemNeedSendEMS.add("手機門號轉帳中台");
//
//				if (form.isSysstatSmtp()) {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStart);
//				} else {
//					itemNeedSendEMS.add(CommonReturnCode.ChannelServiceStop);
//				}
//				itemNeedSendEMS.add(itemNeedSendEMS);
//			}
//
//		} catch (Exception e) {
//			this.errorMessage(e, e.getMessage());
//		}
//
//		return aryNeedSendEMS;
//	}

	public int UpdateSYSSTAT(Sysstat sysstat) throws Exception {
		int iRes = 0;
		try {
			// 參照.NET要求, 以下三行要加入, 因為要記錄audit trail log
			sysstat.setLogAuditTrail(true);
			sysstat.setUpdateUserid(Integer.parseInt(WebUtil.getUser().getUserId()));
			sysstat.setUpdateUser(sysstat.getUpdateUserid());
			iRes = sysstatExtMapper.updateByPrimaryKeySelective(sysstat);
//			iRes = sysstatExtMapper.updateByHbkno(
//					sysstat.getSysstatHbkno(),
//					sysstat.getSysstatIntra(),
//					sysstat.getSysstatAgent(),
//					sysstat.getSysstatIssue(),
//					sysstat.getSysstatIwdI(),
//					sysstat.getSysstatIwdA(),
//					sysstat.getSysstatIwdF(),
//					sysstat.getSysstatIftI(),
//					sysstat.getSysstatIftA(),
//					sysstat.getSysstatIftF(),
//					sysstat.getSysstatIpyI(),
//					sysstat.getSysstatIpyA(),
//					sysstat.getSysstatIpyF(),
//					sysstat.getSysstatCdpF(),
//					sysstat.getSysstatIccdpI(),
//					sysstat.getSysstatIccdpA(),
//					sysstat.getSysstatIccdpF(),
//					sysstat.getSysstatEtxI(),
//					sysstat.getSysstatEtxA(),
//					sysstat.getSysstatEtxF(),
//					sysstat.getSysstat2525A(),
//					sysstat.getSysstat2525F(),
//					sysstat.getSysstatCpuA(),
//					sysstat.getSysstatCpuF(),
//					sysstat.getSysstatCauA(),
//					sysstat.getSysstatCauF(),
//					sysstat.getSysstatCwvA(),
//					sysstat.getSysstatCwmA(),
//					sysstat.getSysstatGpcwdF(),
//					sysstat.getSysstatCaI(),
//					sysstat.getSysstatCafA(),
//					sysstat.getSysstatCavA(),
//					sysstat.getSysstatCamA(),
//					sysstat.getSysstatGpcadF(),
//					sysstat.getSysstatCaaI(),
//					sysstat.getSysstatFwdI(),
//					sysstat.getSysstatAdmI(),
//					sysstat.getSysstatAig(),
//					sysstat.getSysstatHkIssue(),
//					sysstat.getSysstatHkFiscmb(),
//					sysstat.getSysstatHkPlus(),
//					sysstat.getSysstatMoIssue(),
//					sysstat.getSysstatMoFiscmb(),
//					sysstat.getSysstatMoPlus(),
//					sysstat.getSysstatT24Twn(),
//					sysstat.getSysstatT24Hkg(),
//					sysstat.getSysstatT24Mac(),
//					sysstat.getSysstatHkFiscmq(),
//					sysstat.getSysstatMoFiscmq(),
//					sysstat.getSysstatFawA(),
//					sysstat.getSysstatEafA(),
//					sysstat.getSysstatEavA(),
//					sysstat.getSysstatEamA(),
//					sysstat.getSysstatEwvA(),
//					sysstat.getSysstatEwmA(),
//					sysstat.getSysstatGpemvF(),
//					sysstat.getSysstatPure(),
//					sysstat.getSysstatIiqI(),
//					sysstat.getSysstatIiqF(),
//					sysstat.getSysstatIiqP(),
//					sysstat.getSysstatIftP(),
//					sysstat.getSysstatIccdpP(),
//					sysstat.getSysstatIpyP(),
//					sysstat.getSysstatCdpA(),
//					sysstat.getSysstatCdpP(),
//					sysstat.getSysstatIqvP(),
//					sysstat.getSysstatIqmP(),
//					sysstat.getSysstatIqcP(),
//					sysstat.getSysstatEqpP(),
//					sysstat.getSysstatEqcP(),
//					sysstat.getSysstatEquP(),
//					sysstat.getSysstatAdmA(),
//					sysstat.getSysstatNwdI(),
//					sysstat.getSysstatNwdA(),
//					sysstat.getSysstatGpiwdF(),
//					sysstat.getSysstatGpobF(),
//					sysstat.getSysstatNfwI(),
//					sysstat.getSysstatVaaF(),
//					sysstat.getSysstatVaaA()
//					);
			return iRes;
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			getLogContext().setProgramException(e);
//			sendEMS(ex);
//			throw ExceptionUtil.createException(this.getInnerMessage(ex));
		}
		return iRes;
	}

}
