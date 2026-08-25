package com.syscom.fep.web.controller.atmmon;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;

import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.FcrmstatExtMapper;
import com.syscom.fep.mybatis.model.Fcrmstat;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.atmmon.UI_060295_Form;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * FEP 服務/通路/線路查詢
 * 
 * @author bruce
 *
 */
@Controller
public class UI_060295Controller extends BaseController {

	@Autowired
	private AtmService atmService;

	private FcrmstatExtMapper fcrmstatExtMapper = SpringBeanFactoryUtil.getBean(FcrmstatExtMapper.class);

	private Sysstat sysStat = null;

	private final String FCRMSTAT_CURRENCY = "001";

	@Override
	public void pageOnLoad(ModelMap mode) {
		this.showMessage(mode, MessageType.INFO, "");
		UI_060295_Form form = new UI_060295_Form();
		// '進行查詢
		this.bindFormViewData(form, mode);
		// 查詢 SYSSTAT 表中各系統與財金的連線狀態
		this.tabPanel2BindData(this.sysStat, form);
		// 取得系統目前狀態
		this.tabPanel3GetSYSSTAT(this.sysStat, form, mode);
	}

	/**
	 * 資料整理
	 * 
	 * @return
	 */
	private void bindFormViewData(UI_060295_Form form, ModelMap mode) {
		try {
			this.sysStat = atmService.getStatus();
			if (this.sysStat == null) {
				this.showMessage(mode, MessageType.WARNING, QueryNoData);
			} else {
				// 取得基本資料
				this.tabPanel1BaseInfo(this.sysStat, form);
				this.showMessage(mode, MessageType.INFO, QuerySuccess);
			}
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			//20220912 Bruce Modify 畫面顯示的錯誤訊息改成 程式出現錯誤！！請洽資訊人員 Start
			//this.showMessage(mode, MessageType.DANGER, programError);
			this.showMessage(mode, MessageType.DANGER, programError);
			//20220912 Bruce Modify 畫面顯示的錯誤訊息改成 程式出現錯誤！！請洽資訊人員 end
		}
	}

	/**
	 * 取得基本資料
	 * 
	 * @param sysStat
	 * @param form
	 */
	private void tabPanel1BaseInfo(Sysstat sysStat, UI_060295_Form form) {
		form.setSysstatHbkno(sysStat.getSysstatHbkno()); // 銀行代號
		form.setSysstatFbkno(sysStat.getSysstatFbkno()); // 財金公司銀行代號
		form.setSysstatScbkno(sysStat.getSysstatScbkno()); // 信用卡虛擬代號
		form.setSysstatLbsdyFisc(sysStat.getSysstatLbsdyFisc()); // 財金上營業日
		form.setSysstatTbsdyFisc(sysStat.getSysstatTbsdyFisc()); // 財金本營業日
		form.setSysstatNbsdyFisc(sysStat.getSysstatNbsdyFisc()); // 財金下營業日
		form.setSysstatFopcsync(sysStat.getSysstatFopcsync()); // OPC FROM FISC 同步基碼
		form.setSysstatTopcsync(sysStat.getSysstatTopcsync()); // OPC TO FISC 同步基碼
		form.setSysstatFcdsync(sysStat.getSysstatFcdsync()); // CD/ATM FROM FISC 同步基碼
		form.setSysstatTcdsync(sysStat.getSysstatTcdsync()); // CD/ATM TO FISC 同步基碼
		form.setSysstatFrmsync(sysStat.getSysstatFrmsync()); // 通匯 FROM FISC 同步基碼
		form.setSysstatTrmsync(sysStat.getSysstatTrmsync()); // 通匯 TO FISC 同步基碼
		form.setSysstatFppsync(sysStat.getSysstatFppsync()); // PIN PROTECTON FROM FISC
		form.setSysstatTppsync(sysStat.getSysstatTppsync()); // PIN PROTECTON TO FISC
		form.setSysstatF3dessync(sysStat.getSysstatF3dessync()); // TRIPLE DES PIN PROTECTION KEY FROM FISC
		form.setSysstatT3dessync(sysStat.getSysstatT3dessync()); // TRIPLE DES PIN PROTECTION KEY TO FISC
		form.setSysstatOpckeyst(this.getKeystName(sysStat.getSysstatOpckeyst())); // OPC KEY STATUS
		form.setSysstatAtmkeyst(this.getKeystName(sysStat.getSysstatAtmkeyst())); // ATM KEY STATUS
		form.setSysstatRmkeyst(this.getKeystName(sysStat.getSysstatRmkeyst())); // RM KEY STATUS
		form.setSysstatPpkeyst(this.getKeystName(sysStat.getSysstatPpkeyst())); // PP KEY STATUS
		form.setSysstat3enckeyst(this.getKeystName(sysStat.getSysstat3enckeyst())); // 3DES KEY STATUS
	}

	/**
	 * 查詢 SYSSTAT 表中各系統與財金的連線狀態
	 * 
	 * @param sysStat
	 * @param form
	 */
	private void tabPanel2BindData(Sysstat sysStat, UI_060295_Form form) {
		if (sysStat != null) {
			form.setSysstatSoct(sysStat.getSysstatSoct() + "-" + this.getSoctName(sysStat.getSysstatSoct()));
			form.setSysstatMboct(sysStat.getSysstatMboct() + "-" + this.getMboctName(sysStat.getSysstatMboct()));
			form.setSysstatAoct1000(sysStat.getSysstatAoct1000() + "-" + this.getAoctName(sysStat.getSysstatAoct1000()));
			form.setSysstatAoct1100(sysStat.getSysstatAoct1100() + "-" + this.getAoctName(sysStat.getSysstatAoct1100()));
			form.setSysstatAoct1200(sysStat.getSysstatAoct1200() + "-" + this.getAoctName(sysStat.getSysstatAoct1200()));
			form.setSysstatAoct1300(sysStat.getSysstatAoct1300() + "-" + this.getAoctName(sysStat.getSysstatAoct1300()));
			form.setSysstatAoct1400(sysStat.getSysstatAoct1400() + "-" + this.getAoctName(sysStat.getSysstatAoct1400()));
			form.setSysstatAoct2000(sysStat.getSysstatAoct2000() + "-" + this.getAoctName(sysStat.getSysstatAoct2000()));
			form.setSysstatAoct2200(sysStat.getSysstatAoct2200() + "-" + this.getAoctName(sysStat.getSysstatAoct2200()));
			form.setSysstatAoct2200(sysStat.getSysstatAoct2200() + "-" + this.getAoctName(sysStat.getSysstatAoct2200()));
			form.setSysstatAoct2500(sysStat.getSysstatAoct2500() + "-" + this.getAoctName(sysStat.getSysstatAoct2500()));
			form.setSysstatAoct2510(sysStat.getSysstatAoct2510() + "-" + this.getAoctName(sysStat.getSysstatAoct2510()));
			form.setSysstatAoct2520(sysStat.getSysstatAoct2520() + "-" + this.getAoctName(sysStat.getSysstatAoct2520()));
			form.setSysstatAoct2530(sysStat.getSysstatAoct2530() + "-" + this.getAoctName(sysStat.getSysstatAoct2530()));
			form.setSysstatAoct2540(sysStat.getSysstatAoct2540() + "-" + this.getAoctName(sysStat.getSysstatAoct2540()));
			form.setSysstatAoct2550(sysStat.getSysstatAoct2550() + "-" + this.getAoctName(sysStat.getSysstatAoct2550()));
			form.setSysstatAoct2560(sysStat.getSysstatAoct2560() + "-" + this.getAoctName(sysStat.getSysstatAoct2560()));
			form.setSysstatAoct2570(sysStat.getSysstatAoct2570() + "-" + this.getAoctName(sysStat.getSysstatAoct2570()));
			form.setSysstatAoct7100(sysStat.getSysstatAoct7100() + "-" + this.getAoctName(sysStat.getSysstatAoct7100()));
			// 'ChenLi, 2012/10/25, 增加跨行付款交易顯示欄位
			form.setSysstatAoct7300(sysStat.getSysstatAoct7300() + "-" + this.getAoctName(sysStat.getSysstatAoct7300()));
			form.setSysstatMbact1000txt(sysStat.getSysstatMbact1000());
			form.setSysstatMbact1100txt(sysStat.getSysstatMbact1100());
			form.setSysstatMbact1200txt(sysStat.getSysstatMbact1200());
			form.setSysstatMbact1300txt(sysStat.getSysstatMbact1300());
			form.setSysstatMbact1400txt(sysStat.getSysstatMbact1400());
			form.setSysstatMbact2000txt(sysStat.getSysstatMbact2000());
			form.setSysstatMbact2200txt(sysStat.getSysstatMbact2200());
			form.setSysstatMbact2200txt(sysStat.getSysstatMbact2200());
			form.setSysstatMbact2500txt(sysStat.getSysstatMbact2500());
			form.setSysstatMbact2510txt(sysStat.getSysstatMbact2510());
			form.setSysstatMbact2520txt(sysStat.getSysstatMbact2520());
			form.setSysstatMbact2530txt(sysStat.getSysstatMbact2530());
			form.setSysstatMbact2540txt(sysStat.getSysstatMbact2540());
			form.setSysstatMbact2550txt(sysStat.getSysstatMbact2550());
			form.setSysstatMbact2560txt(sysStat.getSysstatMbact2560());
			form.setSysstatMbact2570txt(sysStat.getSysstatMbact2570());
			form.setSysstatMbact7100txt(sysStat.getSysstatMbact7100());
			// 'ChenLi, 2012/10/25, 增加跨行付款交易顯示欄位
			form.setSysstatMbact7300txt(sysStat.getSysstatMbact7300());
			form.setSysstatMbact1000(this.getMbactName(sysStat.getSysstatMbact1000()));
			form.setSysstatMbact1100(this.getMbactName(sysStat.getSysstatMbact1100()));
			form.setSysstatMbact1200(this.getMbactName(sysStat.getSysstatMbact1200()));
			form.setSysstatMbact1300(this.getMbactName(sysStat.getSysstatMbact1300()));
			form.setSysstatMbact1400(this.getMbactName(sysStat.getSysstatMbact1400()));
			form.setSysstatMbact2000(this.getMbactName(sysStat.getSysstatMbact2000()));
			form.setSysstatMbact2200(this.getMbactName(sysStat.getSysstatMbact2200()));
			form.setSysstatMbact2200(this.getMbactName(sysStat.getSysstatMbact2200()));
			form.setSysstatMbact2500(this.getMbactName(sysStat.getSysstatMbact2500()));
			form.setSysstatMbact2510(this.getMbactName(sysStat.getSysstatMbact2510()));
			form.setSysstatMbact2520(this.getMbactName(sysStat.getSysstatMbact2520()));
			form.setSysstatMbact2530(this.getMbactName(sysStat.getSysstatMbact2530()));
			form.setSysstatMbact2540(this.getMbactName(sysStat.getSysstatMbact2540()));
			form.setSysstatMbact2550(this.getMbactName(sysStat.getSysstatMbact2550()));
			form.setSysstatMbact2560(this.getMbactName(sysStat.getSysstatMbact2560()));
			form.setSysstatMbact2570(this.getMbactName(sysStat.getSysstatMbact2570()));
			form.setSysstatMbact7100(this.getMbactName(sysStat.getSysstatMbact7100()));
			// 'ChenLi, 2012/10/25, 增加跨行付款交易顯示欄位
			form.setSysstatMbact7300(this.getMbactName(sysStat.getSysstatMbact7300()));
		}
		//2022/09/12 Bruce modify 依照Candy需求取消Fcrmstat相關 Start
		//Fcrmstat fcrmstat = new Fcrmstat();
		//fcrmstat.setFcrmstatCurrency(FCRMSTAT_CURRENCY);
		//List<Fcrmstat> fcrmstatList = fcrmstatExtMapper.queryByPrimaryKey(fcrmstat);
		//if(fcrmstatList.size() > 0) {
			//form.setFcrmstatAoct1600(fcrmstatList.get(0).getFcrmstatAoctrm() + "-" + this.getAoctName(fcrmstatList.get(0).getFcrmstatAoctrm()));
			// '2020/10/05 modified by Carrie for Fortify修正:Cross-Site Scripting: Reflected
			//form.setFcrmstatMbact1600txt(fcrmstatList.get(0).getFcrmstatMbactrm());
			//form.setFcrmstatMbact1600(this.getMbactName(fcrmstatList.get(0).getFcrmstatMbactrm()));
		//}
		//2022/09/12 Bruce modify 依照Candy需求取消Fcrmstat相關 end
	}

	/**
	 * 取得系統目前狀態
	 * 
	 * @param sysStat
	 * @param form
	 */
	private String tabPanel3GetSYSSTAT(Sysstat sysStat, UI_060295_Form form, ModelMap mode) {
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
		if (sysStat == null) {
			return Router.UI_060295.getView();
		}

		// '1-1:自行21
// 2024-03-06 Richard modified for SYSSTATE 調整
//		form.setSysstatIntra(DbHelper.toBoolean(sysStat.getSysstatIntra()));// 自行
//		form.setSysstatIwdI(DbHelper.toBoolean(sysStat.getSysstatIwdI()));// 提款
//		form.setSysstatIftI(DbHelper.toBoolean(sysStat.getSysstatIftI()));// 轉帳
//		form.setSysstatAdmI(DbHelper.toBoolean(sysStat.getSysstatAdmI()));// 存款
//		// '2016/02/15 Added By Nick SU For ARPC
//		form.setSysstatIiqI(DbHelper.toBoolean(sysStat.getSysstatIiqI()));// 餘額查詢
//		form.setSysstatFwdI(DbHelper.toBoolean(sysStat.getSysstatFwdI()));// 外幣提款
//		// '2017/01/25 Added By Nick SU For 無卡提款
//		form.setSysstatNwdI(DbHelper.toBoolean(sysStat.getSysstatNwdI()));// 無卡提款
//		form.setSysstatNfwI(DbHelper.toBoolean(sysStat.getSysstatNfwI()));// 外幣無卡提款
//		form.setSysstatIpyI(DbHelper.toBoolean(sysStat.getSysstatIpyI()));// 繳款
//		form.setSysstatIccdpI(DbHelper.toBoolean(sysStat.getSysstatIccdpI()));// 全國繳費
//		form.setSysstatEtxI(DbHelper.toBoolean(sysStat.getSysstatEtxI()));// 全國繳稅
//		form.setSysstatCaI(DbHelper.toBoolean(sysStat.getSysstatCaI()));// 預借現金
//		form.setSysstatCaaI(DbHelper.toBoolean(sysStat.getSysstatCaaI()));// AE預借現金
//		// '2010/6/18 Add by Kitty for 「自行」增加欄位
//		// '2013/11/14, ChenLi, 增加SYSSTAT_HK_FISCMQ、SYSSTAT_MO_FISCMQ欄位
//		form.setSysstatAig(DbHelper.toBoolean(sysStat.getSysstatAig()));// 南山人壽保單繳款
//		form.setSysstatHkIssue(DbHelper.toBoolean(sysStat.getSysstatHkIssue()));// 香港分行原存行
//		form.setSysstatHkFiscmb(DbHelper.toBoolean(sysStat.getSysstatHkFiscmb()));// 香港分行財金提款
//		form.setSysstatHkFiscmq(DbHelper.toBoolean(sysStat.getSysstatHkFiscmq()));// 香港分行財金餘額查詢
//		form.setSysstatHkPlus(DbHelper.toBoolean(sysStat.getSysstatHkPlus()));// 香港分行PLUS
//		form.setSysstatMoIssue(DbHelper.toBoolean(sysStat.getSysstatMoIssue()));// 澳門分行原存行
//		form.setSysstatMoFiscmb(DbHelper.toBoolean(sysStat.getSysstatMoFiscmb()));// 澳門分行財金財金提款
//		form.setSysstatMoFiscmq(DbHelper.toBoolean(sysStat.getSysstatMoFiscmq()));// 澳門分行財金餘額查詢
//		form.setSysstatMoPlus(DbHelper.toBoolean(sysStat.getSysstatMoPlus())); // 澳門分行PLUS
//
//		// 1-2:代理23
//		form.setSysstatAgent(DbHelper.toBoolean(sysStat.getSysstatAgent()));// 代理行
//		form.setSysstatIwdA(DbHelper.toBoolean(sysStat.getSysstatIwdA()));// 台幣提款
//		// 'ChenLi, 2015/03/02, 加入代理外幣提款狀態顯示
//		form.setSysstatFawA(DbHelper.toBoolean(sysStat.getSysstatFawA()));// 外幣提款
//		form.setSysstatIftA(DbHelper.toBoolean(sysStat.getSysstatIftA()));// 轉帳
//		// '2016/08/12 Added By Nick SU For 跨行存款
//		form.setSysstatAdmA(DbHelper.toBoolean(sysStat.getSysstatAdmA()));// 跨行存款
//		form.setSysstatIpyA(DbHelper.toBoolean(sysStat.getSysstatIpyA()));// 繳款
//		form.setSysstatIccdpA(DbHelper.toBoolean(sysStat.getSysstatIccdpA()));// 全國繳費(晶片)
//		// '2016/02/15 Added By Nick SU For ARPC
//		form.setSysstatCdpA(DbHelper.toBoolean(sysStat.getSysstatCdpA()));// 全國繳費(ID+ACC)
//		form.setSysstatEtxA(DbHelper.toBoolean(sysStat.getSysstatEtxA()));// 全國繳稅
//		form.setSysstat2525A(DbHelper.toBoolean(sysStat.getSysstat2525A()));// 消費扣款（固定費率）
//		form.setSysstatCpuA(DbHelper.toBoolean(sysStat.getSysstatCpuA()));// 消費扣款（變動費率）
//		form.setSysstatCavA(DbHelper.toBoolean(sysStat.getSysstatCavA()));// 預借現金 VISA
//		form.setSysstatCamA(DbHelper.toBoolean(sysStat.getSysstatCamA()));// 預借現金 MASTER
//		form.setSysstatCauA(DbHelper.toBoolean(sysStat.getSysstatCauA()));// 預授交易
//		form.setSysstatCwvA(DbHelper.toBoolean(sysStat.getSysstatCwvA()));// 國際提款PLUS
//		form.setSysstatCwmA(DbHelper.toBoolean(sysStat.getSysstatCwmA()));// 國際提款CIRRUS
//		// '2016/01/27 Added By Nick SU 增加EMV狀態
//		form.setSysstatCafA(DbHelper.toBoolean(sysStat.getSysstatCafA()));// 國際提款銀聯卡
//		// '2016/07/20 Added By Nick SU for EMV
//		form.setSysstatEafA(DbHelper.toBoolean(sysStat.getSysstatEafA()));// EMV 國際提款銀聯卡
//		// '2016/07/20 Added By Nick SU for EMV
//		form.setSysstatEwvA(DbHelper.toBoolean(sysStat.getSysstatEwvA()));// EMV國際提款(PLUS)
//		form.setSysstatEwmA(DbHelper.toBoolean(sysStat.getSysstatEwmA()));// EMV國際提款(CIRRUS)
//		form.setSysstatEavA(DbHelper.toBoolean(sysStat.getSysstatEavA()));// EMV預借現金(VISA)
//		form.setSysstatEamA(DbHelper.toBoolean(sysStat.getSysstatEamA()));// EMV預借現金(MASTER)
//		form.setSysstatEajA(DbHelper.toBoolean(sysStat.getSysstatEajA()));// EMV預借現金(JCB)
//		// 'Fly 2018/01/31 增加代理行無卡提款
//		form.setSysstatNwdA(DbHelper.toBoolean(sysStat.getSysstatNwdA()));// 無卡跨行提款
//		// 'Fly 2019/03/25 增加代理2566約定及核驗項目
//		form.setSysstatVaaA(DbHelper.toBoolean(sysStat.getSysstatVaaA()));// 約定及核驗服務
//
//		// '1-3:原存15
//		form.setSysstatIssue(DbHelper.toBoolean(sysStat.getSysstatIssue()));// 原存
//		form.setSysstatIwdF(DbHelper.toBoolean(sysStat.getSysstatIwdF()));// 提款
//		form.setSysstatIftF(DbHelper.toBoolean(sysStat.getSysstatIftF()));// 轉帳
//		// '2016/02/15 Added By Nick SU For ARPC
//		form.setSysstatIiqF(DbHelper.toBoolean(sysStat.getSysstatIiqF()));// 餘額查詢
//		form.setSysstatIpyF(DbHelper.toBoolean(sysStat.getSysstatIpyF()));// 繳款
//		form.setSysstatIccdpF(DbHelper.toBoolean(sysStat.getSysstatIccdpF()));// 全國繳費(晶片)
//		form.setSysstatCdpF(DbHelper.toBoolean(sysStat.getSysstatCdpF()));// 全國繳費(ID+ACT)
//		form.setSysstatEtxF(DbHelper.toBoolean(sysStat.getSysstatEtxF()));// 全國繳稅
//		form.setSysstat2525F(DbHelper.toBoolean(sysStat.getSysstat2525F()));// 消費扣款(固定費率)
//		form.setSysstatCpuF(DbHelper.toBoolean(sysStat.getSysstatCpuF()));// 消費扣款(變動費率)
//		form.setSysstatCauF(DbHelper.toBoolean(sysStat.getSysstatCauF()));// 預授交易
//		form.setSysstatGpcwdF(DbHelper.toBoolean(sysStat.getSysstatGpcwdF()));// 國際提款PLUS/CIRRUS
//		// '2016/04/19 Added By Nick SU For EMV Plus/Cirrus國際提款
//		form.setSysstatGpemvF(DbHelper.toBoolean(sysStat.getSysstatGpemvF()));// EMV國際提款PLUS/CIRRUS
//		form.setSysstatGpcadF(DbHelper.toBoolean(sysStat.getSysstatGpcadF()));// 預借現金VISA/MASTER
//		form.setSysstatGpiwdF(DbHelper.toBoolean(sysStat.getSysstatGpiwdF()));// 晶片卡跨國提款
//		form.setSysstatGpobF(DbHelper.toBoolean(sysStat.getSysstatGpobF()));// 跨境支付交易
//		form.setSysstatVaaF(DbHelper.toBoolean(sysStat.getSysstatVaaF()));// 約定及核驗服務
//		// '2016/02/15 Added By Nick SU For ARPC
//		// '1-4:純代理12
//		form.setSysstatPure(DbHelper.toBoolean(sysStat.getSysstatPure()));// 純代理
//		form.setSysstatIiqP(DbHelper.toBoolean(sysStat.getSysstatIiqP()));// 晶片卡餘額查詢
//		form.setSysstatIftP(DbHelper.toBoolean(sysStat.getSysstatIftP()));// 轉帳
//		form.setSysstatIccdpP(DbHelper.toBoolean(sysStat.getSysstatIccdpP()));// 全國繳費(晶片)
//		form.setSysstatCdpP(DbHelper.toBoolean(sysStat.getSysstatCdpP()));// 全國繳費(ID+ACC)
//		// 第二列
//		form.setSysstatIpyP(DbHelper.toBoolean(sysStat.getSysstatIpyP()));// 繳款-跨行(2531)
//		form.setSysstatIqvP(DbHelper.toBoolean(sysStat.getSysstatIqvP()));// PLUS卡餘額查詢
//		form.setSysstatIqmP(DbHelper.toBoolean(sysStat.getSysstatIqmP()));// CIRRUS卡餘額查詢
//		form.setSysstatIqcP(DbHelper.toBoolean(sysStat.getSysstatIqcP()));// 銀聯卡餘額查詢
//		form.setSysstatEquP(DbHelper.toBoolean(sysStat.getSysstatEquP()));// EMV 銀聯卡餘額查詢
//		// '2016/07/26 Added By Nick SU for EMV
//		form.setSysstatEqpP(DbHelper.toBoolean(sysStat.getSysstatEqpP()));// EMV PLUS餘額查詢
//		form.setSysstatEqcP(DbHelper.toBoolean(sysStat.getSysstatEqcP()));// EMV CIRRUS卡餘額查詢
		// 'ChenLi, 2013/06/20, For NCB上線增加控制T24主機(台灣、香港、澳門)
		// '1-5:T24主機
		form.setSysstatT24Twn(DbHelper.toBoolean(sysStat.getSysstatT24Twn()));// 台灣T24主機
//		form.setSysstatT24Hkg(DbHelper.toBoolean(sysStat.getSysstatT24Hkg()));// 香港T24主機
		// 澳門T24主機
//		if (sysStat.getSysstatCbsMac().equals("U")) {
//			form.setSysstatT24Mac(false);
//		} else {
//			form.setSysstatT24Mac(DbHelper.toBoolean(sysStat.getSysstatT24Mac()));
//		}

		// '2:其他通道
		form.setSysstatCbs(DbHelper.toBoolean(sysStat.getSysstatCbs()));// CBS-T24
//		form.setSysstatFedi(DbHelper.toBoolean(sysStat.getSysstatFedi()));// FEDI
//		form.setSysstatNb(DbHelper.toBoolean(sysStat.getSysstatNb()));// 網銀
//		form.setSysstatWebatm(DbHelper.toBoolean(sysStat.getSysstatWebatm()));// WEB ATM服務
//		form.setSysstatAscChannel(DbHelper.toBoolean(sysStat.getSysstatAscChannel()));// 永豐信用卡
//		form.setSysstatAsc(DbHelper.toBoolean(sysStat.getSysstatAsc()));// 永豐信用卡線路
//		form.setSysstatAscmd(DbHelper.toBoolean(sysStat.getSysstatAscmd()));// 永豐晶片錢卡線路
//		form.setSysstatGcard(DbHelper.toBoolean(sysStat.getSysstatGcard()));// 永豐GIFT卡線路
//		form.setSysstatSps(DbHelper.toBoolean(sysStat.getSysstatSps()));// 永豐證券線路
//		form.setSysstatAscmac(DbHelper.toBoolean(sysStat.getSysstatAscmac()));// 與ASC壓碼線路
//		form.setSysstatSpsmac(DbHelper.toBoolean(sysStat.getSysstatSpsmac()));// 與 SPS 壓碼線路
//		form.setSysstatSvcs(DbHelper.toBoolean(sysStat.getSysstatSvcs()));// 悠遊Debit卡
//		// 'Fly 2014/10/24 增加香港SMS通道
//		form.setSysstatHkSms(DbHelper.toBoolean(sysStat.getSysstatHkSms()));// 香港SMS
//		// 'Fly 2020/10/15 增加掌靜脈通道
//		form.setSysstatPv(DbHelper.toBoolean(sysStat.getSysstatPv()));// 掌靜脈
//		// '2021/01/29 Modify by Ruling for 手機門號跨行轉帳：增加手機門號中台
//		form.setSysstatSmtp(DbHelper.toBoolean(sysStat.getSysstatSmtp()));// 手機門號轉帳中台
		return Router.UI_060295.getView();
	}
}
