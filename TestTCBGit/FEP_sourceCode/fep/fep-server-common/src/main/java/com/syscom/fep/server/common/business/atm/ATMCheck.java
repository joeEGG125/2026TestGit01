package com.syscom.fep.server.common.business.atm;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.thread.ThreadWrapper;
import com.syscom.fep.frmcommon.util.EnvPropertiesUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.ChannelExtMapper;
import com.syscom.fep.mybatis.ext.mapper.OdrcExtMapper;
import com.syscom.fep.mybatis.ext.model.BinExt;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.ext.model.GraylistRequestExt;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.common.business.host.Credit;
import com.syscom.fep.vo.constant.BINPROD;
import com.syscom.fep.vo.enums.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ATMCheck extends ATMHost {
	private String graylisturl = EnvPropertiesUtil.getProperty("graylist.check.url", "graylisturl");

	private ChannelExtMapper channelExtMapper = SpringBeanFactoryUtil.getBean(ChannelExtMapper.class);

	protected ATMCheck() {
		super();
	}

	protected ATMCheck(ATMData atmMsg, String api) {
		super(atmMsg, api);
	}

	protected ATMCheck(ATMData atmMsg) throws Exception {
		super(atmMsg);
	}

	/**
	 * 檢核繳費網繳本行信用卡費
	 * 
	 * @return
	 *         FEPReturnCode
	 * 
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Ruling</modifier>
	 *         <reason>繳費網繳信用卡款</reason>
	 *         <date>2017/03/24</date>
	 *         </modify>
	 *         </history>
	 */
	public FEPReturnCode checkEFTCCard(String W_ACTNO) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		Credit host = new Credit(getGeneralData());
		try {
			// 帳號前五碼=虛擬帳號(00598)為繳信用卡費
			if (W_ACTNO.substring(0, 5).equals(CMNConfig.getInstance().getVirtualActno()) && getFeptxn().getFeptxnPcode().substring(0, 3).equals("226")) {
				// 全國性繳費ID+ACC(2261~2264)交易輸入虛擬帳號時，後面的身份證號
				// 前二位數字編碼(A:01, B:02, C:03, … 以此類推)
				String W_IDNO1 = mappingFirstDigit(W_ACTNO.substring(5, 16));

				String W_IDNO2 = "";
				if (StringUtils.isNotBlank(getFeptxn().getFeptxnIdno())) {
					if (getFeptxn().getFeptxnIdno().trim().length() == 11) {
						W_IDNO2 = mappingFirstDigitIdno(getFeptxn().getFeptxnIdno().trim());
					} else {
						W_IDNO2 = getFeptxn().getFeptxnIdno().trim();
					}
				}

				// 比對身份證號與00598之後的身份證號是否相符
				if (!W_IDNO1.trim().equals(W_IDNO2.trim())) {
					getLogContext().setRemark("身份證號與銷帳編號00598之後的身份證號比對不符");
					this.logMessage(getLogContext());
					return FISCReturnCode.CheckIDNOError;
				}
			}

			// 組信用卡電文(B17)
			rtnCode = host.sendToCredit("B17", (byte) 1);

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".checkEFTCCard");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	public FEPReturnCode checkHeader() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		// 2010-06-01 by kyo 程式調整;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = null;
		Bin defBIN = new BinExt();
		String TwoZero = "00"; // 補兩個零
		try {
			// 1. 檢核 ATM 電文 Header
			Zone defZone = getZoneByZoneCode(atmMstr.getAtmZone());
			if (defZone == null) {
				return IOReturnCode.ZONENotFound;
			}
			// 2012/08/13 Modify by Ruling for 企業入金，BDR要檢核ATM是否為存提款機
			// 2012/09/06 Modify by Ruling for 新增硬幣機的業務:ICR、CCR要檢核ATM是否為存提款機
			// 2013/02/04 Modify by Ruling for 企業入金機入帳:BDF要檢核ATM是否為存提款機
			// 2016/07/28 Modify by Ruling for ATM新功能-跨行存款&現金捐款:ODR、DDR要檢核ATM是否為存提款機
			// 2018/11/28 Modify by Ruling for OKI硬幣機功能:CFT、ICW要檢核ATM是否為存提款機
			// 存款交易檢核ATM是否為存提款機
			if ((ATMTXCD.IDR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.CDR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.BDR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.ICR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.CCR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.BDF.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.ODR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.DDR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.CFT.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.ICW.name().equals(getFeptxn().getFeptxnTxCode()))
					&& atmMstr.getAtmAtmtype() != ATMType.ADM.getValue()) {
				rtnCode = ATMReturnCode.ATMNotSupportDepositTX;
				return rtnCode;
			}

			// 檢核是否為百年電文
			//--ben-20220922-//if (!"A".equals(atmReq.getAtmverN())) {
				// 2010-06-23 by kyo for 修改與SPEC一致
			//--ben-20220922-//	rtnCode = ATMReturnCode.OtherCheckError;
			//--ben-20220922-//	return rtnCode;
			//--ben-20220922-//}

			// 2012/08/13 Modify by Ruling for 企業入金，BDR要檢核ATM交易狀態
			// 2012/09/06 Modify by Ruling for 新增硬幣機的業務:ICR、CCR要檢核ATM交易狀態
			// 2013/01/28 Modify by Ruling for 新增硬幣機(提款):有卡硬幣提款(ICW)要檢核ATM交易狀態
			// 2013/02/05 Modify by Ruling for 企業入金機入帳:BDF要檢核ATM交易狀態
			// 2016/07/28 Modify by Ruling for ATM新功能-跨行存款&現金捐款:ODR、DDR要檢核ATM交易狀態
			// Fly 2016/01/21 for 菓菜市場BDM
			// 2017/11/10 Modify by Ruling for ATM提款暫停服務改用MSGCTL_TXTYPE2來判斷
			// 存/提款/預借現金交易, 檢核ATM交易狀態
			if (getAtmTxData().getMsgCtl().getMsgctlTxtype2() != null) {
				switch (getAtmTxData().getMsgCtl().getMsgctlTxtype2()) {
					case 1:
					case 9:
					case 10:
					case 11:
					case 12:
					case 13:
					case 14:
					case 16:
					case 17:
					case 21:
					case 22:
					case 23:
					case 24:
					case 25:
						if (atmStat.getAtmstatStopflg() == 1) {
							rtnCode = CommonReturnCode.WithdrawServiceStop;
							return rtnCode;
						}
						break;
				}
			}

			if (!defZone.getZoneTbsdy().equals(getFeptxn().getFeptxnTbsdy())) {
				rtnCode = ATMReturnCode.TBSDYError;
				return rtnCode;
			}

			/// * 更新營業日 */
			// BugReport(001B0431):重整ATM狀態檔的欄位搬移與歸零
			// BugReport(001B0017):三點半後無法自動換日的BUG修正
			/// * 12/8 修改, 台灣地區判斷財金營業日 */
			if ((ATMZone.TWN.name().equals(getFeptxn().getFeptxnAtmZone())
					&& !SysStatus.getPropertyValue().getSysstatTbsdyFisc().equals(atmStat.getAtmstatTbsdy()))
					|| ((ATMZone.HKG.name().equals(getFeptxn().getFeptxnAtmZone())
							|| ATMZone.MAC.name().equals(getFeptxn().getFeptxnAtmZone()))
							&& !defZone.getZoneTbsdy().equals(atmStat.getAtmstatTbsdy()))) {
				// 2010-06-01 by kyo 調整程式:transaction放IF外面會每次都跑
				txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
				// 將本營業日資料搬至上營業日
				atmStat.setAtmstatLbsdy(atmStat.getAtmstatTbsdy()); // 上營業日
				if (ATMZone.TWN.name().equals(getFeptxn().getFeptxnAtmZone())) {
					atmStat.setAtmstatTbsdy(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
				} else if (ATMZone.HKG.name().equals(getFeptxn().getFeptxnAtmZone())
						|| ATMZone.MAC.name().equals(getFeptxn().getFeptxnAtmZone())) {
					atmStat.setAtmstatTbsdy(defZone.getZoneTbsdy());// 本營業日
				}

				atmStat.setAtmstatTfrAmtLbsdy(atmStat.getAtmstatTfrAmtTbsdy());
				atmStat.setAtmstatTfrCntLbsdy(atmStat.getAtmstatTfrCntTbsdy());
				atmStat.setAtmstatDepCurLbsdy(atmStat.getAtmstatDepCurTbsdy());
				// 2010-07-22 by kyo for spec modify: 7/22 永豐通知修正
				// 2010-07-26 by kyo for 還原變更
				atmStat.setAtmstatDepAmtLbsdy(atmStat.getAtmstatDepAmtTbsdy());
				atmStat.setAtmstatDepCntLbsdy(atmStat.getAtmstatDepCntTbsdy());
				// 2012/09/06 Modify by Ruling for 新增硬幣機的業務
				atmStat.setAtmstatCdepAmtLbsdy(atmStat.getAtmstatCdepAmtTbsdy());
				atmStat.setAtmstatCdepCntLbsdy(atmStat.getAtmstatCdepCntTbsdy());
				// 2013/04/19 Modify by Ruling for 新增硬幣機(提款)
				atmStat.setAtmstatCcwdAmtLbsdy(atmStat.getAtmstatCcwdAmtTbsdy());
				atmStat.setAtmstatCcwdCntLbsdy(atmStat.getAtmstatCcwdCntTbsdy());
				atmStat.setAtmstatPayAmtLbsdy(atmStat.getAtmstatPayAmtTbsdy());
				atmStat.setAtmstatPayCntLbsdy(atmStat.getAtmstatPayCntTbsdy());
				atmStat.setAtmstatCwdAmtLbsdy(atmStat.getAtmstatCwdAmtTbsdy());
				atmStat.setAtmstatCwdCntLbsdy(atmStat.getAtmstatCwdCntTbsdy());// 台幣
				// BugReport(001B0527):CODING ERROR 遺漏將 前營業日的美金港幣日幣葡幣 搬移正確
				atmStat.setAtmstatCwdAmtUsdLbsdy(atmStat.getAtmstatCwdAmtUsdTbsdy());
				atmStat.setAtmstatCwdCntUsdLbsdy(atmStat.getAtmstatCwdCntUsdTbsdy());// 美金
				atmStat.setAtmstatCwdAmtHkdLbsdy(atmStat.getAtmstatCwdAmtHkdTbsdy());
				atmStat.setAtmstatCwdCntHkdLbsdy(atmStat.getAtmstatCwdCntHkdTbsdy());// 港幣
				atmStat.setAtmstatCwdAmtJpyLbsdy(atmStat.getAtmstatCwdAmtJpyTbsdy());
				atmStat.setAtmstatCwdCntJpyLbsdy(atmStat.getAtmstatCwdCntJpyTbsdy());// 日幣
				atmStat.setAtmstatCwdAmtMopLbsdy(atmStat.getAtmstatCwdAmtMopTbsdy());
				atmStat.setAtmstatCwdCntMopLbsdy(atmStat.getAtmstatCwdCntMopTbsdy());// 葡幣
				// 本營業日裝鈔金額搬至上營業日
				atmStat.setAtmstatRwtCntLbsdy(atmStat.getAtmstatRwtCntTbsdy());
				atmStat.setAtmstatRwtAmtLbsdy(atmStat.getAtmstatRwtAmtTbsdy());// 台幣
				atmStat.setAtmstatRwtCntUsdLbsdy(atmStat.getAtmstatRwtCntUsdTbsdy());
				atmStat.setAtmstatRwtAmtUsdLbsdy(atmStat.getAtmstatRwtAmtUsdTbsdy());// 美金
				atmStat.setAtmstatRwtCntHkdLbsdy(atmStat.getAtmstatRwtCntHkdTbsdy());
				atmStat.setAtmstatRwtAmtHkdLbsdy(atmStat.getAtmstatRwtAmtHkdTbsdy());// 港幣
				atmStat.setAtmstatRwtCntJpyLbsdy(atmStat.getAtmstatRwtCntJpyTbsdy());
				atmStat.setAtmstatRwtAmtJpyLbsdy(atmStat.getAtmstatRwtAmtJpyTbsdy());// 日幣
				atmStat.setAtmstatRwtCntMopLbsdy(atmStat.getAtmstatRwtCntMopTbsdy());
				atmStat.setAtmstatRwtAmtMopLbsdy(atmStat.getAtmstatRwtAmtMopTbsdy());// 葡幣

				// 本營業日繳庫金額搬至上營業日
				atmStat.setAtmstatBkCntLbsdy(atmStat.getAtmstatBkCntTbsdy());
				atmStat.setAtmstatBkAmtLbsdy(atmStat.getAtmstatBkAmtTbsdy());// 台幣
				// BugReport(001B0367):補上外幣搬移邏輯
				atmStat.setAtmstatBkCntUsdLbsdy(atmStat.getAtmstatBkCntUsdTbsdy());
				atmStat.setAtmstatBkAmtUsdLbsdy(atmStat.getAtmstatBkAmtUsdTbsdy());// 美金
				atmStat.setAtmstatBkCntHkdLbsdy(atmStat.getAtmstatBkCntHkdTbsdy());
				atmStat.setAtmstatBkAmtHkdLbsdy(atmStat.getAtmstatBkAmtHkdTbsdy()); // 港幣
				atmStat.setAtmstatBkCntJpyLbsdy(atmStat.getAtmstatBkCntJpyTbsdy());
				atmStat.setAtmstatBkAmtJpyLbsdy(atmStat.getAtmstatBkAmtJpyTbsdy());// 日幣
				atmStat.setAtmstatBkCntMopLbsdy(atmStat.getAtmstatBkCntMopTbsdy());
				atmStat.setAtmstatBkAmtMopLbsdy(atmStat.getAtmstatBkAmtMopTbsdy());// 葡幣

				atmStat.setAtmstatBkcCntLbsdy(atmStat.getAtmstatBkcCntTbsdy());
				atmStat.setAtmstatBkcAmtLbsdy(atmStat.getAtmstatBkcAmtTbsdy());

				// 將本營業日資料更新為 0
				atmStat.setAtmstatTfrCntTbsdy(0);
				atmStat.setAtmstatTfrAmtTbsdy(0L);
				atmStat.setAtmstatDepCntTbsdy(0);
				atmStat.setAtmstatDepAmtTbsdy(0L);
				// 2012/09/06 Modify by Ruling for 新增硬幣機的業務
				atmStat.setAtmstatCdepCntTbsdy(0);
				atmStat.setAtmstatCdepAmtTbsdy(0L);
				// 2013/04/19 Modify by Ruling for 新增硬幣機(提款)
				atmStat.setAtmstatCcwdCntTbsdy(0);
				atmStat.setAtmstatCcwdAmtTbsdy(0L);
				atmStat.setAtmstatPayCntTbsdy(0);
				atmStat.setAtmstatPayAmtTbsdy(0L);
				atmStat.setAtmstatCwdCntTbsdy(0);
				atmStat.setAtmstatCwdAmtTbsdy(0L); // 台幣
				atmStat.setAtmstatCwdCntUsdTbsdy(0);
				atmStat.setAtmstatCwdAmtUsdTbsdy(0L); // 美金
				atmStat.setAtmstatCwdCntHkdTbsdy(0);
				atmStat.setAtmstatCwdAmtHkdTbsdy(0L); // 港幣
				atmStat.setAtmstatCwdCntJpyTbsdy(0);
				atmStat.setAtmstatCwdAmtJpyTbsdy(0L); // 日幣
				atmStat.setAtmstatCwdCntMopTbsdy(0);
				atmStat.setAtmstatCwdAmtMopTbsdy(0L);// 葡幣

				// 本營業日裝鈔金額更新為 0
				atmStat.setAtmstatRwtCntTbsdy(0);
				atmStat.setAtmstatRwtAmtTbsdy(0L); // 台幣
				atmStat.setAtmstatRwtCntUsdTbsdy(0);
				atmStat.setAtmstatRwtAmtUsdTbsdy(0L); // 美金
				atmStat.setAtmstatRwtCntHkdTbsdy(0);
				atmStat.setAtmstatRwtAmtHkdTbsdy(0L); // 港幣
				atmStat.setAtmstatRwtCntJpyTbsdy(0);
				atmStat.setAtmstatRwtAmtJpyTbsdy(0L); // 日幣
				atmStat.setAtmstatRwtCntMopTbsdy(0);
				atmStat.setAtmstatRwtAmtMopTbsdy(0L);// 葡幣

				// 本營業日繳庫金額 更新為 0
				atmStat.setAtmstatBkCntTbsdy(0);
				atmStat.setAtmstatBkAmtTbsdy(0L);
				atmStat.setAtmstatBkcCntTbsdy(0);
				atmStat.setAtmstatBkcAmtTbsdy(0L);
				// BugReport(001B0367):補上外幣搬移邏輯
				atmStat.setAtmstatBkCntUsdTbsdy(0);
				atmStat.setAtmstatBkAmtUsdTbsdy(0L);
				atmStat.setAtmstatBkCntHkdTbsdy(0);
				atmStat.setAtmstatBkAmtHkdTbsdy(0L);
				atmStat.setAtmstatBkCntJpyTbsdy(0);
				atmStat.setAtmstatBkAmtJpyTbsdy(0L);
				atmStat.setAtmstatBkCntMopTbsdy(0);
				atmStat.setAtmstatBkAmtMopTbsdy(0L);// 葡幣
				// 更新該筆 ATMSTAT
				if (atmstatMapper.updateByPrimaryKeySelective(atmStat) != 1) {
					rtnCode = IOReturnCode.ATMSTATUpdateError;
					throw ExceptionUtil.createException("未更新ATM狀態檔或更新ATM狀態檔發生錯誤");
				}

				// 將本營業日資料更新至上營業日
				if (!FEPChannel.EAT.name().equals(getFeptxn().getFeptxnChannel())) {
					if (atmcashExtMapper.updateBusinessDay(getFeptxn().getFeptxnAtmno()) <= 0) {
						rtnCode = IOReturnCode.ATMCASHUpdateError;
						throw ExceptionUtil.createException("未更新鈔箱或更新鈔箱發生錯誤");
					}
				}

				// 2010-06-23 BugRepot(001B0745):寫入ATMC(結帳)，放在Transaction中跨介面發生例外，
				// 修改INSERTATMC放在Commit之後
				transactionManager.commit(txStatus);

				// 2010-06-22 by kyo for SPEC修改:先QUERY是否有TTI存在ATMC，若沒有才寫入
				// Tables.DBATMC dbATMC = new Tables.DBATMC(FEPConfig.DBName);
				Atmc defATMC = new Atmc();
				defATMC.setAtmcTbsdy(defZone.getZoneLbsdy());
				defATMC.setAtmcTbsdyFisc(SysStatus.getPropertyValue().getSysstatLbsdyFisc());
				// 2010-06-23 by kyo for ATM_BRNO_ST非INDEX且其他條件已經足夠，因此不需要當為查詢條件
				defATMC.setAtmcBrnoSt(null);
				defATMC.setAtmcAtmno(getFeptxn().getFeptxnAtmno());
				defATMC.setAtmcTxCode(ATMTXCD.TTI.name());
				defATMC.setAtmcDscpt("000");// 摘要
				defATMC.setAtmcSelfcd((short) 0); // 保留
				/// * 5/6 修改 */ WebATM不做TTI
				if (atmcExtMapper.getAtmcByConditions(
						defATMC.getAtmcTbsdy(),
						defATMC.getAtmcTbsdyFisc(),
						defATMC.getAtmcBrnoSt(),
						defATMC.getAtmcAtmno(),
						defATMC.getAtmcCur(),
						defATMC.getAtmcTxCode(),
						defATMC.getAtmcDscpt(),
						defATMC.getAtmcSelfcd()) == null
						&& !FEPChannel.EAT.name().equals(getFeptxn().getFeptxnChannel())) {
					rtnCode = insertATMC(ATMCTxType.ChangeDay.getValue());
					// 2012/08/06 Modify by Ruling for 傳回錯誤訊息
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}
				}
			}

			// 檢核地區檔的MODE(E980:非MMA金融信用卡無法執行交易)
			if (defZone.getZoneCbsMode() == ATMCBSMode.GoToOnline.getValue()) {
				rtnCode = ATMReturnCode.CBSModeIs5;
				return rtnCode;
			}

			// 3. 檢核系統狀態檔自行或跨行交易狀態
			// 2013/10/22 Modify by Ruling for 配合財金調整代理銀聯卡手續費
// 2024-03-06 Richard modified for SYSSTATE 調整
//			if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatCafA())
//					&& ATMTXCD.IQC.name().equals(getFeptxn().getFeptxnTxCode())) {
//				// 暫停代理國際提款-銀聯卡服務，一併拒絶銀聯卡餘額查詢(IQC2401)
//				getAtmTxData().setTxStatus(false);
//			}

			// MsgHandler 檢核交易連線狀態
			if (!getAtmTxData().isTxStatus()) {
				/// * 9/13修改, 取消跨行狀態檢核 */
				if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) // 跨行
				{
					rtnCode = FEPReturnCode.SenderBankServiceStop;
					// 2012/08/03 Modify by Ruling for 修正跨行交易EFT2562沒有擋交易仍送電文給財金的問題
					return rtnCode;
				} else {
					// 提款暫停服務 E948
					rtnCode = CommonReturnCode.WithdrawServiceStop;
					return rtnCode;
				}
			}

			// 2013/02/08 Modify by Ruling for 企業入金機入帳
			// 2012/09/06 Modify by Ruling for 新增硬幣機的業務，ICR、CCR要檢核轉入帳號
			// 2012/08/13 Modify by Ruling for 企業入金，BDR要檢核轉入帳號
			// 2011/08/17 modify by Ruling for AE卡要用2~7碼去讀BIN檔資料
			// 2011-01-12 update by kyo /* 10/21 修正, 繳費/預約轉帳交易(EFT/IPA/ATF/BFT), 寫入轉入帳號類別 */
			// Fly 2015/10/26 For菓菜市場BDM INI電文增加檢核BIN
			// 2020/10/19 Modify by Ruling for ATM提升分行離櫃率_全自助：掌靜脈交易(PCR、PDR、PFT)增加檢核轉入帳號是否有值
			// 轉帳/存款交易, 檢核轉入帳號
			if (ATMTXCD.IFT.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.IDR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.CDR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.EFT.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.IPA.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.ATF.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.BFT.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.BDR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.ICR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.CCR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.BDF.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.INI.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.PCR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.PDR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.PFT.name().equals(getFeptxn().getFeptxnTxCode())) {

				if (StringUtils.isBlank(getFeptxn().getFeptxnTrinActno())) {
					// 轉入帳號狀況檢核錯誤 E074
					rtnCode = FEPReturnCode.TranInACTNOError;
					return rtnCode;
				}

				// 2018/06/04 Modify by Ruling for MASTER DEBIT加悠遊：檢核轉入帳號是否為本行 Gift Card 或信用卡
				if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())
						&& !TwoZero.equals(getFeptxn().getFeptxnTrinActno().substring(0, 2))) {
					// AE卡號15位，帳號第1碼為'0'，讀取帳號第2~7碼
					if ("0".equals(getFeptxn().getFeptxnTrinActno().substring(0, 1))) {
						// 以行庫代號( SYSSTAT_HBKNO)及FEPTXN_TRIN_ACTNO[2:6]為 KEY
						defBIN.setBinBkno(SysStatus.getPropertyValue().getSysstatHbkno());
						defBIN.setBinNo(getFeptxn().getFeptxnTrinActno().substring(1, 7));
					} else {
						// 以行庫代號( SYSSTAT_HBKNO)及FEPTXN_TRIN_ACTNO[1:6]為 KEY
						defBIN.setBinBkno(SysStatus.getPropertyValue().getSysstatHbkno());
						defBIN.setBinNo(getFeptxn().getFeptxnTrinActno().substring(0, 6));
					}

					// 讀取 BIN 檔
					Bin bin = binMapper.selectByPrimaryKey(defBIN.getBinBkno(), defBIN.getBinNo());
					if (bin != null) {
						getFeptxn().setFeptxnTrinKind(bin.getBinProd());

						// 2018-05-22 Modify by Ruling for MASTER DEBIT加悠遊：不得轉出簽帳卡
						if (BINPROD.Debit.equals(getFeptxn().getFeptxnTrinKind())) {
							rtnCode = ATMReturnCode.TranInACTNOError; // 轉入帳號錯誤(E074)
							getLogContext().setRemark("轉入帳號為本行簽帳金融卡(DEBIT卡)");
							this.logMessage(getLogContext());
							return rtnCode;
						}

						// add by Maxine on 2011/08/18 for spec 8/17 修改 for 檢核轉入信用卡檢查碼 */
						rtnCode = checkDigit(getFeptxn().getFeptxnTrinActno());
						if (rtnCode != CommonReturnCode.Normal) {
							return rtnCode;
						}

					} else if (ATMTXCD.IDR.name().equals(getFeptxn().getFeptxnTxCode())
							|| ATMTXCD.ICR.name().equals(getFeptxn().getFeptxnTxCode())
							|| ATMTXCD.CDR.name().equals(getFeptxn().getFeptxnTxCode())
							|| ATMTXCD.CCR.name().equals(getFeptxn().getFeptxnTxCode())) {
						// 2017/07/10 Modify by Ruling for
						// 繳信用卡款，找不到BIN檔資料且為無卡現金存款(CDR)/無卡硬幣存款(CCR)時回E074轉入帳號錯誤
						// 2012/09/06 Modify by Ruling for 新增硬幣機的業務
						// 如轉入非本行信用卡，回給 ATM 錯誤訊息
						getLogContext().setRemark("轉入非本行信用卡");
						this.logMessage(getLogContext());
						rtnCode = FEPReturnCode.TranInACTNOError;
						// 2012/08/06 Modify by Ruling for 傳回錯誤訊息
						return rtnCode;
					}
				}
			}

			// Debit卡只能於ATM執行PNC交易
			// 2018-05-22 Modify by Ruling for MASTER DEBIT加悠遊：不得轉出簽帳卡
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())
					&& BINPROD.Debit.equals(getFeptxn().getFeptxnTroutKind())
					&& !ATMTXCD.PNC.name().equals(getFeptxn().getFeptxnTxCode())) {
				rtnCode = FEPReturnCode.CCardServiceNotAllowed; // 不允許信用卡交易
				getLogContext().setRemark("轉出帳號為本行簽帳金融卡(DEBIT卡)");
				this.logMessage(getLogContext());
				return rtnCode;
			}

			// 轉出/轉入類別為 GIFT卡或轉出類別為COMBO卡 檢核交易連線狀態
			// 2010-05-03 modified by kyo for SPEC修改邏輯:轉出帳號
			if (BINPROD.Gift.equals(getFeptxn().getFeptxnTroutKind())
					|| BINPROD.Combo.equals(getFeptxn().getFeptxnTroutKind())
					|| BINPROD.Gift.equals(getFeptxn().getFeptxnTrinKind())) {
// 2024-03-06 Richard modified for SYSSTATE 調整
//				if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatAsc())
//						|| !DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatGcard())) {
//					// 提款暫停服務 E948
//					rtnCode = CommonReturnCode.WithdrawServiceStop;
//					return rtnCode;
//				}
			}

			// 2010-09-13 by kyo for /* 9/13 修改, 取消跨行狀態檢核 */
			// 跨行記號 True:跨行;False:自行
			if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {// 跨行
				// 取消跨行狀態檢核
			} else {
				// 海外分行跨區提款
				if (ATMTXCD.IFW.name().equals(getFeptxn().getFeptxnTxCode())
						&& StringUtils.isNotBlank(getFeptxn().getFeptxnAtmnoVir())) {
					if (ATMZone.MAC.name().equals(getFeptxn().getFeptxnZoneCode())) {
// 2024-03-06 Richard modified for SYSSTATE 調整
//						if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatMoIssue())) {
//							rtnCode = FEPReturnCode.IntraBankServiceStop; // 自行暫停服務
//							return rtnCode;
//						}
					}
					if (ATMZone.HKG.name().equals(getFeptxn().getFeptxnZoneCode())) {
// 2024-03-06 Richard modified for SYSSTATE 調整
//						if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatHkIssue())) {
//							rtnCode = FEPReturnCode.IntraBankServiceStop; // 自行暫停服務
//							return rtnCode;
//						}
					}
				}
			}
			// Fly 2016/01/21 for 菓菜市場BDM
			// 2016/07/28 Modify by Rulinf for ATM新功能-跨行存款:ODT跨行存款轉帳交易不檢查ATM序號
			// 4. 檢核ATM 序號是否重複
			if ("ATM".equals(getFeptxn().getFeptxnChannel())) {
				if (getAtmTxData().getMsgCtl().getMsgctlTxtype2() != null && StringUtils.isNotBlank(getAtmTxData().getMsgCtl().getMsgctlTxtype2().toString())
						&& !ATMTXCD.ODT.name().equals(getFeptxn().getFeptxnTxCode())) {
					if (!checkATMSeq()) {
						getLogContext().setRemark("ATM 序號重複 !");
						this.logMessage(getLogContext());
						rtnCode = FEPReturnCode.ATMSeqNoDuplicate;
					}
				}
			}
			return rtnCode;
		} catch (Exception ex) {
			if (txStatus != null && !txStatus.isCompleted()) {
				transactionManager.rollback(txStatus);
			}
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	public FEPReturnCode checkBody() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		Busi defBUSI = new Busi();
		Corpay defCORPAY = new Corpay();
		BigDecimal rwt_amt = new BigDecimal(0);
		BigDecimal iwd_amt = new BigDecimal(0);
		try {
			// ATM提升分行離櫃率_全自助：掌靜脈轉帳(PFT)增加檢核轉入/轉出帳號是否相同
			switch (ATMTXCD.parse(getFeptxn().getFeptxnTxCode())) {
				case IFT:
				case IPA:
				case EFT:
				case PFT:
					/// * 檢核轉入/轉出帳號是否相同 */
					if (StringUtils.isBlank(getFeptxn().getFeptxnTrinActno())
							|| (getFeptxn().getFeptxnTroutActno().equals(getFeptxn().getFeptxnTrinActno())
									&& getFeptxn().getFeptxnTroutBkno().equals(getFeptxn().getFeptxnTrinBkno()))) {
						rtnCode = ATMReturnCode.TranInACTNOSameAsTranOut; /// * 轉出入帳號相同 */
						return rtnCode;
					}

					if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
						/// * 檢核是否轉入本行信用卡專戶 */
						switch (getFeptxn().getFeptxnTrinActno()) {
							case SpACTNO1:
							case SpACTNO2:
							case SpACTNO3:
							case SpACTNO4:
								rtnCode = ATMReturnCode.TranInACTNOError; /// * 轉入帳戶不存在 */
								return rtnCode;
						}
					}
					break;
				case SMS:
					/// * ACTION=2(OTP), MEMO 欄位必須有值 */
					//--ben-20220922-//if ("2".equals(atmReq.getACTION()) && "0".equals(atmReq.getMEMO())) {
					//--ben-20220922-//	rtnCode = ATMReturnCode.MsgContentError; /// * 收到訊息不符規格 */
					//--ben-20220922-//	return rtnCode;
					//--ben-20220922-//}
					/// * ACTION=5(變更), 新舊手機號碼欄位必須有值 */
					//--ben-20220922-//if ("5".equals(atmReq.getACTION()) && "".equals(atmReq.getMobileN())
					//--ben-20220922-//		&& "".equals(atmReq.getMobileO())) {
					//--ben-20220922-//	rtnCode = ATMReturnCode.MsgContentError; /// * 收到訊息不符規格 */
					//--ben-20220922-//	return rtnCode;
					//--ben-20220922-//}
					break;
				default:
					break;
			}

			// 2012/07/31 Modify by Ruling for 修改轉入交易檢核虛擬帳號
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())
					&& !"00".equals(getFeptxn().getFeptxnTrinActno().substring(0, 2))) {
				rtnCode = checkVirActno();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}

			// 有卡存款(IDR)/無卡存款(CDR)增加檢核轉入帳號
			// 新增數位分行別調整：配合數位分行未來會有大於200行的情形，判斷分行別的上限由200調整為300
			// 提升分行離櫃率_全自助：掌靜脈存款(PDR、PCR)增加檢核轉入帳號只限一般帳戶及598虛擬帳號
			if ((ATMTXCD.IDR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.CDR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.PDR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.PCR.name().equals(getFeptxn().getFeptxnTxCode()))
					&& StringUtils.isBlank(getFeptxn().getFeptxnTrinKind())) {
				if (getFeptxn().getFeptxnTrinActno().substring(2, 5).compareTo(CMNConfig.getInstance().getMaxBrno()) > 0
						&& !getFeptxn().getFeptxnTrinActno().substring(0, 5).equals(CMNConfig.getInstance().getVirtualActno())) {
					rtnCode = ATMReturnCode.TranInACTNOError; // 轉入帳號不存在(E074)
					getLogContext().setRemark("CheckBody只限一般帳戶及598虛擬帳號");
					this.logMessage(getLogContext());
					return rtnCode;
				}
			}

			// 企業入金，檢核企業入金銷帳編號資料
			// 檢核企業入金銷帳編號資料
			if (ATMTXCD.INI.name().equals(getFeptxn().getFeptxnTxCode())) {
				if (getFeptxn().getFeptxnTrinActno().trim().length() < 16) {
					rtnCode = ATMReturnCode.TranInACTNOError; // 轉入帳號不存在(E074)
					getLogContext().setRemark("CheckBody轉入帳號小於16位");
					this.logMessage(getLogContext());
					return rtnCode;
				}

				// 以企業統編為條件讀取BUSI檔
				defBUSI.setBusiIdno(getFeptxn().getFeptxnIdno().trim());
				Busi busi = busiExtMapper.selectById(defBUSI.getBusiIdno());
				if (busi == null) {
					rtnCode = ATMReturnCode.TranInACTNOError; // 轉入帳號不存在(E074)
					getLogContext().setRemark("CheckBody讀不到BUSI檔，BUSI_IDNO=" + defBUSI.getBusiIdno());
					this.logMessage(getLogContext());
					return rtnCode;
				} else {
					//ben20221118 	atmRes.setCNAME(busi.getBusiCompany());
				}

				// 以企業統編及銷帳編號為條件讀取CORPAY檔
				defCORPAY.setCorpayIdno(getFeptxn().getFeptxnIdno().trim());
				// 菓菜市場：銷帳編號由14位改為16位
				defCORPAY.setCorpayRecno(getFeptxn().getFeptxnTrinActno().trim());
				Corpay corpay = corpayMapper.selectByPrimaryKey(defCORPAY.getCorpayIdno(), defCORPAY.getCorpayRecno());
				if (corpay == null) {
					rtnCode = ATMReturnCode.TranInACTNOError; // 轉入帳號不存在(E074)
					getLogContext().setRemark("CheckBody讀不到CORPAY檔，CORPAY_IDNO=" + defCORPAY.getCorpayIdno()
							+ "，CORPAY_RECNO=" + defCORPAY.getCorpayRecno());
					this.logMessage(getLogContext());
					return rtnCode;
				} else {
					//ben20221118 	atmRes.setMARKETNM(corpay.getCorpayDepname());
				}
			}

			// 2012/08/21 modify by Ruling for 增加提款防呆處理
			// 2017/11/30 modify by Ruling for
			// 補上EMV卡26XX的防呆處理:加上MSGCTL_TXTYPE2=21,22,23,24,25
			// 4. 提款防呆處理
			if (getAtmTxData().getMsgCtl().getMsgctlTxtype2() != null) {
				switch (getAtmTxData().getMsgCtl().getMsgctlTxtype2()) {
					case 1:
					case 9:
					case 10:
					case 11:
					case 12:
					case 13:
					case 14:
					case 16:
					case 21:
					case 22:
					case 23:
					case 24:
					case 25:
						Map<String, Object> dtATMCASH =
								atmcashExtMapper.getAtmCashByCurAtmNoForIWD(getFeptxn().getFeptxnAtmno(), getFeptxn().getFeptxnTxCur());
						if (MapUtils.isEmpty(dtATMCASH)) {
							// 修正dtATMCASH是否有資料不能用Nothing來判斷要改用筆數
							// If dtATMCASH Is Nothing Then
							rtnCode = ATMReturnCode.ATMCashWithdrawError;
							getLogContext().setRemark("CheckBody-提款防呆處理-讀不到ATMCASH檔，atmno=" + getFeptxn().getFeptxnAtmno()
									+ "，cur=" + getFeptxn().getFeptxnTxCur());
							this.logMessage(getLogContext());
							return rtnCode;
						}
						// 硬幣提款(ICW)，不檢核鈔匣數字
						if (!ATMTXCD.ICW.name().equals(getFeptxn().getFeptxnTxCode())) {
							rwt_amt = (BigDecimal) (dtATMCASH.get("RWT_AMT")); // 累計裝鈔金額
							iwd_amt = (BigDecimal) (dtATMCASH.get("IWD_AMT")); // 累計提款金額
							if (atmMstr.getAtmAtmtype() == 2
									&& CurrencyType.TWD.name().equals(getFeptxn().getFeptxnTxCur())) {
								if (rwt_amt.subtract(iwd_amt).add(new BigDecimal(atmStat.getAtmstatDepAmt()))
										.subtract(getFeptxn().getFeptxnTxAmt()).compareTo(BigDecimal.ZERO) < 0) {
									rtnCode = ATMReturnCode.ATMCashWithdrawError;
									getLogContext().setRemark("CheckBody-提款防呆處理-存提款機鈔匣數字異常");
									this.logMessage(getLogContext());
									return rtnCode;
								}
							} else {
								if (rwt_amt.subtract(iwd_amt).subtract(getFeptxn().getFeptxnTxAmt())
										.compareTo(BigDecimal.ZERO) < 0) {
									rtnCode = ATMReturnCode.ATMCashWithdrawError;
									getLogContext().setRemark("CheckBody-提款防呆處理-提款機鈔匣數字異常");
									this.logMessage(getLogContext());
									return rtnCode;
								}
							}
						}
						break;
				}
			}

			// 2012/11/01 modify by Ruling for 增加有卡存款(IDR)防呆處理
			// 2013/01/28 modify by Ruling for 增加有卡硬幣提款(ICW)防呆處理
			// 2016/08/09 Modify by Ruling for ATM新功能:跨行存款(IDR)他行卡存入本行(807)拿掉IDR的防呆處理
			// 5. 有卡存款(IDR)/有卡硬幣提款(ICW)防呆處理
			if (ATMTXCD.ICW.name().equals(getFeptxn().getFeptxnTxCode())) {
				if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
					rtnCode = FEPReturnCode.ReceiverBankServiceStop;
					getLogContext().setRemark("CheckBody-有卡存款(IDR)/有卡硬幣提款(ICW)防呆處理-轉出行必須為自行");
					this.logMessage(getLogContext());
					return rtnCode;
				}
			}

			// 2012/09/06 modify by Ruling for 新增硬幣機的業務
			// 2013/01/28 modify by Ruling for 新增硬幣機(提款)
			// 2013/02/08 modify by Ruling for 企業入金機入帳
			// 2020/10/19 Modify by Ruling for ATM提升分行離櫃率_全自助：掌靜脈硬幣存款(PCR)增加紙鈔及硬幣金額的防呆處理
			// 6. 硬幣存/提款交易防呆處理
			if (ATMTXCD.ICR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.CCR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.BDR.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.BDF.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.ICW.name().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.PCR.name().equals(getFeptxn().getFeptxnTxCode())) {
				// 檢核企業入金機電文(BDF)
				if (ATMTXCD.BDF.name().equals(getFeptxn().getFeptxnTxCode())
						&& !DbHelper.toBoolean(atmMstr.getAtmCorp())) {
					rtnCode = ATMReturnCode.OtherCheckError;
					getLogContext().setRemark("ATM_CORP=0，非企業入金機");
					this.logMessage(getLogContext());
					return rtnCode;
				}

				// 檢核交易金額是否=紙鈔金額+硬幣金額
				if (getFeptxn().getFeptxnCoinAmt().compareTo(BigDecimal.ZERO) > 0) {
					// 檢核ATMMSTR硬幣模組欄位
					if (!DbHelper.toBoolean(atmMstr.getAtmCoin())) {
						// 無硬幣模組
						rtnCode = ATMReturnCode.OtherCheckError;
						getLogContext().setRemark("ATM_COIN=0，無硬幣模組");
						this.logMessage(getLogContext());
						return rtnCode;
					}

					if (!getFeptxn().getFeptxnTxAmt().equals((getFeptxn().getFeptxnCashAmt().add(getFeptxn().getFeptxnCoinAmt())))) {
						rtnCode = ATMReturnCode.OtherCheckError;
						getLogContext().setRemark("CheckBody-硬幣存/提款交易防呆處理-交易金額<>紙鈔金額+硬幣金額");
						this.logMessage(getLogContext());
						return rtnCode;
					}
				}

				// 檢核交易金額必須>0
				if (getFeptxn().getFeptxnTxAmt().compareTo(BigDecimal.ZERO) == 0) {
					rtnCode = ATMReturnCode.OtherCheckError;
					getLogContext().setRemark("CheckBody-硬幣存/提款交易防呆處理-交易金額必須>0");
					this.logMessage(getLogContext());
					return rtnCode;
				}
			}

			// 2012/12/10 modify by Ruling for 增加有卡硬幣存款(ICR)防呆處理
			// 2013/01/28 modify by Ruling for 增加有卡硬幣提款(ICW)防呆處理
			// 7. 檢核有卡硬幣存款(ICR)/有卡硬幣提款(ICW)
			// Fly 2018/10/02 修改 for OKI硬幣機功能
			if (ATMTXCD.ICR.name().equals(getFeptxn().getFeptxnTxCode())) {
				if (getFeptxn().getFeptxnTrinActno().trim().length() < 16) {
					rtnCode = ATMReturnCode.TranInACTNOError;
					getLogContext().setRemark("CheckBody-檢核有卡硬幣存款(ICR)-轉入帳號不足16位,FepTxn.FEPTXN_TRIN_ACTNO="
							+ getFeptxn().getFeptxnTrinActno());
					this.logMessage(getLogContext());
					return rtnCode;
				}
			}

			if (ATMTXCD.ICW.name().equals(getFeptxn().getFeptxnTxCode())) {
				if (getFeptxn().getFeptxnTroutActno().trim().length() < 16) {
					rtnCode = ATMReturnCode.TranOutACTNOError;
					getLogContext().setRemark("CheckBody-檢核有卡硬幣提款(ICW)-轉出帳號不足16位,FepTxn.FEPTXN_TROUT_ACTNO="
							+ getFeptxn().getFeptxnTroutActno());
					this.logMessage(getLogContext());
					return rtnCode;
				}

				// 新增硬幣機(提款):硬幣提款不能提領紙鈔
				if (getFeptxn().getFeptxnCashAmt().compareTo(BigDecimal.ZERO) > 0) {
					rtnCode = ATMReturnCode.OtherCheckError;
					getLogContext().setRemark("CheckBody-檢核有卡硬幣提款(ICW)-硬幣提款不能提領紙鈔");
					this.logMessage(getLogContext());
					return rtnCode;
				}
			}

			// Fly 2018/10/02 修改 for OKI硬幣機功能
			if (ATMTXCD.CFT.name().equals(getFeptxn().getFeptxnTxCode())) {
				if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
					rtnCode = ATMReturnCode.OtherCheckError;
					getLogContext().setRemark("CheckBody-檢核現金繳費找零(CFT), 轉入行必須為本行");
					this.logMessage(getLogContext());
					return rtnCode;
				}

				if (StringUtils.isBlank(getFeptxn().getFeptxnTrinActno())) {
					rtnCode = ATMReturnCode.OtherCheckError;
					getLogContext().setRemark("CheckBody-檢核現金繳費找零(CFT), 轉入帳號為NULL或空白");
					this.logMessage(getLogContext());
					return rtnCode;
				}

				if (!DbHelper.toBoolean(atmMstr.getAtmCoin())) {
					rtnCode = ATMReturnCode.OtherCheckError;
					getLogContext().setRemark("CheckBody-檢核現金繳費找零(CFT), 非硬幣機(ATM_COIN=0)");
					this.logMessage(getLogContext());
					return rtnCode;
				}

				if (getFeptxn().getFeptxnTxAmt().compareTo(BigDecimal.ZERO) == 0) {
					rtnCode = ATMReturnCode.OtherCheckError;
					getLogContext().setRemark("CheckBody-檢核現金繳費找零(CFT), 交易金額必須>0");
					this.logMessage(getLogContext());
					return rtnCode;
				}

				if (getFeptxn().getFeptxnTxAmt()
						.compareTo(getFeptxn().getFeptxnCashAmt().subtract(getFeptxn().getFeptxnCashWamt())
								.add(getFeptxn().getFeptxnCoinAmt()).subtract(getFeptxn().getFeptxnCoinWamt())) != 0) {
					rtnCode = ATMReturnCode.OtherCheckError;
					getLogContext().setRemark("CheckBody-檢核現金繳費找零(CFT), 交易金額<>紙鈔存入金額-紙鈔找零金額+硬幣存入金額-硬幣找零金額");
					this.logMessage(getLogContext());
					return rtnCode;
				}

				//--ben-20220922-//if (atmReq.getPAYTYPE().compareTo(BigDecimal.ONE) != 0) {
				//--ben-20220922-//	rtnCode = ATMReturnCode.OtherCheckError;
				//--ben-20220922-//	getLogContext()
				//--ben-20220922-//			.setRemark("CheckBody-檢核現金繳費找零(CFT), 繳費類別必須為繳信用卡費(atmReq.PAYTYPE<>1), ATMReq.PAYTYPE="
				//--ben-20220922-//					+ atmReq.getPAYTYPE().toString());
				//--ben-20220922-//	this.logMessage(getLogContext());
				//--ben-20220922-//	return rtnCode;
				//--ben-20220922-//}

				if (StringUtils.isBlank(getFeptxn().getFeptxnTrinKind()) && !getFeptxn().getFeptxnTrinActno()
						.substring(0, 5).equals(CMNConfig.getInstance().getVirtualActno())) {
					rtnCode = ATMReturnCode.TranInACTNOError;
					getLogContext().setRemark("CheckBody-檢核現金繳費找零(CFT), 只限繳信用卡及598虛擬帳號");
					this.logMessage(getLogContext());
					return rtnCode;
				}

				if (getFeptxn().getFeptxnCashWamt().compareTo(BigDecimal.ZERO) > 0) {
					Atmcash defCash = new Atmcash();
					defCash.setAtmcashAtmno(getFeptxn().getFeptxnAtmno());
					List<Atmcash> atmcashList = atmcashExtMapper.getAtmCashByAtmNo(getFeptxn().getFeptxnAtmno(), StringUtils.EMPTY);
					if (CollectionUtils.isEmpty(atmcashList)) {
						rtnCode = ATMReturnCode.OtherCheckError;
						getLogContext().setRemark(
								"CheckBody-檢核現金繳費找零(CFT), ATMCASH查無資料  ATMNO: " + getFeptxn().getFeptxnAtmno());
						this.logMessage(getLogContext());
						return rtnCode;
					}

					BigDecimal W_CASH_WAMT = new BigDecimal(0);
					if (getFeptxn().getFeptxnDspcnt1() > 0) {
						W_CASH_WAMT = W_CASH_WAMT.add(
								new BigDecimal(getFeptxn().getFeptxnDspcnt1() * atmcashList.get(0).getAtmcashUnit()));
					}
					if (getFeptxn().getFeptxnDspcnt2() > 0) {
						W_CASH_WAMT = W_CASH_WAMT.add(
								new BigDecimal(getFeptxn().getFeptxnDspcnt2() * atmcashList.get(0).getAtmcashUnit()));
					}
					if (getFeptxn().getFeptxnDspcnt3() > 0) {
						W_CASH_WAMT = W_CASH_WAMT.add(
								new BigDecimal(getFeptxn().getFeptxnDspcnt3() * atmcashList.get(0).getAtmcashUnit()));
					}
					if (getFeptxn().getFeptxnDspcnt4() > 0) {
						W_CASH_WAMT = W_CASH_WAMT.add(
								new BigDecimal(getFeptxn().getFeptxnDspcnt4() * atmcashList.get(0).getAtmcashUnit()));
					}
					if (getFeptxn().getFeptxnDspcnt5() > 0) {
						W_CASH_WAMT = W_CASH_WAMT.add(
								new BigDecimal(getFeptxn().getFeptxnDspcnt5() * atmcashList.get(0).getAtmcashUnit()));
					}
					if (getFeptxn().getFeptxnDspcnt6() > 0) {
						W_CASH_WAMT = W_CASH_WAMT.add(
								new BigDecimal(getFeptxn().getFeptxnDspcnt6() * atmcashList.get(0).getAtmcashUnit()));
					}
					if (getFeptxn().getFeptxnDspcnt7() > 0) {
						W_CASH_WAMT = W_CASH_WAMT.add(
								new BigDecimal(getFeptxn().getFeptxnDspcnt7() * atmcashList.get(0).getAtmcashUnit()));
					}
					if (getFeptxn().getFeptxnDspcnt8() > 0) {
						W_CASH_WAMT = W_CASH_WAMT.add(
								new BigDecimal(getFeptxn().getFeptxnDspcnt8() * atmcashList.get(0).getAtmcashUnit()));
					}

					if (W_CASH_WAMT.compareTo(getFeptxn().getFeptxnCashWamt()) != 0) {
						rtnCode = ATMReturnCode.OtherCheckError;
						getLogContext().setRemark("CheckBody-檢核現金繳費找零(CFT), 紙鈔找零金額<>ATMCASH的吐鈔張數*面額");
						this.logMessage(getLogContext());
						return rtnCode;
					}
				}
			}

			// 2013/04/29 modify by Ruling for ATM條碼
			// 8. 檢核ATM條碼電文(BCD)
			if (ATMTXCD.BCD.name().equals(getFeptxn().getFeptxnTxCode())) {
				rtnCode = processBCD();
				if (rtnCode != CommonReturnCode.Normal) {
					getLogContext().setRemark("CheckBody-檢核ATM條碼電文(BCD)-不等於Normal, rtnCode = " + rtnCode.toString());
					this.logMessage(getLogContext());
					return rtnCode;
				}
			}

			// 2013/08/22 modify by Ruling for 本行ATM電文欄位端末設備查核碼:1.WEB ATM 跳過端末設備查核碼的檢核
			// 2.暫時先不回應E711的ERROR給ATM
			// 2016/08/09 Modify by Ruling for ATM新功能:跨行存款轉帳(ODT2521)不需檢核端末設備查核碼
			// 9. 檢核本行ATM電文欄位端末設備查核碼
			if (FEPChannel.ATM.name().equals(getFeptxn().getFeptxnChannel())
					&& StringUtils.isNotBlank(getFeptxn().getFeptxnAtmChk())
					&& !ATMTXCD.ODT.name().equals(getFeptxn().getFeptxnTxCode())) {
				rtnCode = checkATMCHK();
				if (rtnCode != CommonReturnCode.Normal) {
					getLogContext()
							.setRemark("CheckBody-檢核本行ATM電文欄位端末設備查核碼-不等於Normal, rtnCode = " + rtnCode.toString());
					this.logMessage(getLogContext());
					// Return rtnCode
				}
			}

			// 2014/04/22 modify by Ruling for
			// 避免提款交易的吐鈔張數1~8均為0時，ATMC沒更新但已上T24扣帳成功，導致ATM現金日結表(BDRPTATMCASHBAL)FEP與T24資料不合
			// 2017/11/30 modify by Ruling for
			// 補上EMV卡26XX的防呆處理:加上MSGCTL_TXTYPE2=21,22,23,24,25
			// 10. 紙鈔提款交易吐鈔張數防呆處理
			// Fly 2018/10/09 For OKI硬幣機
			if ((getAtmTxData().getMsgCtl().getMsgctlTxtype2() != null
					&& (getAtmTxData().getMsgCtl().getMsgctlTxtype2() == 1
							|| getAtmTxData().getMsgCtl().getMsgctlTxtype2() == 9
							|| getAtmTxData().getMsgCtl().getMsgctlTxtype2() == 10
							|| getAtmTxData().getMsgCtl().getMsgctlTxtype2() == 11
							|| getAtmTxData().getMsgCtl().getMsgctlTxtype2() == 12
							|| getAtmTxData().getMsgCtl().getMsgctlTxtype2() == 13
							|| getAtmTxData().getMsgCtl().getMsgctlTxtype2() == 14
							|| getAtmTxData().getMsgCtl().getMsgctlTxtype2() == 16
							|| getAtmTxData().getMsgCtl().getMsgctlTxtype2() == 21
							|| getAtmTxData().getMsgCtl().getMsgctlTxtype2() == 22
							|| getAtmTxData().getMsgCtl().getMsgctlTxtype2() == 23
							|| getAtmTxData().getMsgCtl().getMsgctlTxtype2() == 24
							|| getAtmTxData().getMsgCtl().getMsgctlTxtype2() == 25)
					&& !ATMTXCD.ICW.name().equals(getFeptxn().getFeptxnTxCode()))
					|| (ATMTXCD.CFT.name().equals(getFeptxn().getFeptxnTxCode())
							&& getFeptxn().getFeptxnCashWamt().compareTo(BigDecimal.ZERO) > 0)) {
				if (getFeptxn().getFeptxnDspcnt1() == 0 && getFeptxn().getFeptxnDspcnt2() == 0
						&& getFeptxn().getFeptxnDspcnt3() == 0 && getFeptxn().getFeptxnDspcnt4() == 0
						&& getFeptxn().getFeptxnDspcnt5() == 0 && getFeptxn().getFeptxnDspcnt6() == 0
						&& getFeptxn().getFeptxnDspcnt7() == 0 && getFeptxn().getFeptxnDspcnt8() == 0) {
					rtnCode = ATMReturnCode.ATMCashWithdrawError;
					getLogContext().setRemark("CheckBody-檢核紙鈔提款交易的吐鈔張數1~8不能為0");
					this.logMessage(getLogContext());
					return rtnCode;
				}
			}

			// 2018/11/06 Modify by Ruling for 外幣無卡提款
			// 12. 檢核 NFW 匯率一定要有值
			if (ATMTXCD.NFW.name().equals(getFeptxn().getFeptxnTxCode())) {
				if (getFeptxn().getFeptxnExrate().compareTo(BigDecimal.ZERO) == 0) {
					rtnCode = ATMReturnCode.NoRateTable;
					getLogContext().setRemark("CheckBody-檢核外幣無卡提款交易的匯率不能為0");
					this.logMessage(getLogContext());
					return rtnCode;
				}
			}

			// Fly 2019/04/03 檢核中文附言欄不得超過14個中文字
			if (StringUtils.isNotBlank(getFeptxn().getFeptxnChrem())) {
				// Fly 2020/05/20 修改 for ATM新增中文備註
				if (getFeptxn().getFeptxnChannel().equals("ATM")) {
					if (getFeptxn().getFeptxnChrem().length() > 6) {
						rtnCode = ATMReturnCode.OtherCheckError;
						getLogContext().setRemark("CheckBody-中文附言欄不得超過6個中文字  CHREM:" + getFeptxn().getFeptxnChrem());
						this.logMessage(getLogContext());
						return rtnCode;
					}
				} else {
					if (getFeptxn().getFeptxnChrem().length() > 14) {
						rtnCode = ATMReturnCode.OtherCheckError;
						getLogContext().setRemark("CheckBody-中文附言欄不得超過14個中文字  CHREM:" + getFeptxn().getFeptxnChrem());
						this.logMessage(getLogContext());
						return rtnCode;
					}
				}
			}

			// WEBATM新增轉帳【給自己】中文附言欄
			if (StringUtils.isNotBlank(getFeptxn().getFeptxnFmrem())) {
				if (getFeptxn().getFeptxnFmrem().length() > 14) {
					rtnCode = ATMReturnCode.OtherCheckError;
					getLogContext().setRemark("CheckBody-轉出附言欄不得超過14個中文字  FMREM:" + getFeptxn().getFeptxnChrem());
					this.logMessage(getLogContext());
					return rtnCode;
				}
			}

			// 修改 for 轉帳繳納口罩費用免手續費
			if ("M".equals(getFeptxn().getFeptxnBenefit()) && ("ODR".equals(getFeptxn().getFeptxnTxCode())
					|| StringUtils.isNotBlank(getFeptxn().getFeptxnTroutKind()))) {
				rtnCode = ATMReturnCode.OtherCheckError;
				getLogContext().setRemark("跨行存款或預借現金轉帳不得繳納口罩費用");
				this.logMessage(getLogContext());
				return rtnCode;
			}

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	public boolean checkATMSeq() throws Exception {
		// 檢核改抓取財金營業日與財金上營業日
		boolean rtn = false;
		Feptxn oFEPTXN = new FeptxnExt();
		// DataTable dt = new DataTable();
		// Tables.DBFEPTXN dbFEPTXNMSTR = new
		// Tables.DBFEPTXN(Configuration.FEPConfig.DBName, 0,
		// SysStatus.PropertyValue.SYSSTAT_TBSDY_FISC.substring(6, 8));
		// 改用變數的方式，避免log顯示本筆資料而不是原交易資料
		FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");

		String oEJ = "";
		String oTX_CODE = "";
		String oTX_DATE = "";
		String oTX_TIME = "";
		String oREPLY_CODE = "";

		try {
			//ATM交易日期先傳換為西元年格式(電文資料是西元後兩碼+月日共6碼)W_ATM_DATE= 轉文字(轉數字(ATM_TITA.TRANDATE[1:2])+1911) + ATM_TITA.TRANDATE[3:4]
			oFEPTXN.setFeptxnTxDateAtm(String.valueOf(Integer.parseInt(atmReq.getTRANDATE().substring(1, 2)) + 1911) + Integer.parseInt(atmReq.getTRANDATE().substring(3, 4)));
			oFEPTXN.setFeptxnAtmno(atmReq.getWSID());
			oFEPTXN.setFeptxnAtmSeqno(atmReq.getTRANSEQ());
			oFEPTXN.setFeptxnEjfno(getAtmTxData().getEj());
			feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, ".checkATMSeq"));
			List<Feptxn> feptxnList = feptxnDao.selectFEPTXNForCheckATMSeq(oFEPTXN.getFeptxnTxDateAtm(),
					oFEPTXN.getFeptxnAtmno(), oFEPTXN.getFeptxnAtmSeqno());
			if (feptxnList.size() > 1) {
				for (int i = 0; i < feptxnList.size(); i++) {
					// 改用變數的方式，避免log顯示本筆資料而不是原交易資料
					oEJ = feptxnList.get(i).getFeptxnEjfno().toString();
					oTX_CODE = feptxnList.get(i).getFeptxnTxCode();
					oTX_DATE = feptxnList.get(i).getFeptxnTxDate();
					oTX_TIME = feptxnList.get(i).getFeptxnTxTime();
					oREPLY_CODE = feptxnList.get(i).getFeptxnReplyCode();
					if (feptxnList.get(i).getFeptxnEjfno().intValue() != getAtmTxData().getEj()) {
						// 企業入金補送機制，原判斷ATMMSTR.ATM_MARKETBDM改為ATMMSTR.ATM_CORP，只要是企業入金機，ATM序號重複，仍可執行
//						if (DbHelper.toBoolean(getATMMSTR().getAtmCorp())) {
							//--ben-20220922-//if ("BDF".equals(atmReq.getTXCD()) && "BDF".equals(feptxnList.get(i).getFeptxnTxCode())
							//--ben-20220922-//		&& "E948".equals(feptxnList.get(i).getFeptxnReplyCode())) {
							//--ben-20220922-//	rtn = true;
							//--ben-20220922-//}
//						}
					}
				}
			} else {
				rtn = true;
			}

			if (!rtn) {
				// 改用變數的方式，避免log顯示本筆資料而不是原交易資料
				getLogContext().setRemark(
						String.format("ATM 序號重複! 原交易EJFNO=%1$s,TX_CODE=%2$s,TX_DATE=%3$s,TX_TIME=%4$s,REPLY_CODE=%5$s",
								oEJ, oTX_CODE, oTX_DATE, oTX_TIME, oREPLY_CODE));
				this.logMessage(getLogContext());
				return rtn;
			}

			rtn = false;
			oFEPTXN.setFeptxnTxDateAtm(String.valueOf(Integer.parseInt(atmReq.getTRANDATE().substring(1, 2)) + 1911) + Integer.parseInt(atmReq.getTRANDATE().substring(3, 4)));
			oFEPTXN.setFeptxnAtmno(atmReq.getWSID());
			oFEPTXN.setFeptxnAtmSeqno(atmReq.getTRANSEQ());
			oFEPTXN.setFeptxnEjfno(getAtmTxData().getEj());
			feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, ".checkATMSeq"));
			feptxnList = feptxnDao.selectFEPTXNForCheckATMSeq(oFEPTXN.getFeptxnTxDateAtm(), oFEPTXN.getFeptxnAtmno(),
					oFEPTXN.getFeptxnAtmSeqno());
			if (feptxnList.size() > 0) {
				for (int i = 0; i < feptxnList.size(); i++) {
					// 2018/11/15 Modify by Ruling for 改用變數的方式，避免log顯示本筆資料而不是原交易資料
					oEJ = feptxnList.get(i).getFeptxnEjfno().toString();
					oTX_CODE = feptxnList.get(i).getFeptxnTxCode();
					oTX_DATE = feptxnList.get(i).getFeptxnTxDate();
					oTX_TIME = feptxnList.get(i).getFeptxnTxTime();
					oREPLY_CODE = feptxnList.get(i).getFeptxnReplyCode();
					if (feptxnList.get(i).getFeptxnEjfno().intValue() != getAtmTxData().getEj()) {
						// 企業入金補送機制，原判斷ATMMSTR.ATM_MARKETBDM改為ATMMSTR.ATM_CORP，只要是企業入金機，ATM序號重複，仍可執行
						if (DbHelper.toBoolean(getATMMSTR().getAtmCorp())) {
							//--ben-20220922-//if ("BDF".equals(atmReq.getTXCD()) && "BDF".equals(feptxnList.get(i).getFeptxnTxCode())
							//--ben-20220922-//		&& "E948".equals(feptxnList.get(i).getFeptxnReplyCode())) {
							//--ben-20220922-//	rtn = true;
							//--ben-20220922-//}
						}
					}
				}
			} else {
				rtn = true;
			}

			if (!rtn) {
				// 2018/11/15 Modify by Ruling for 改用變數的方式，避免log顯示本筆資料而不是原交易資料
				getLogContext().setRemark(
						String.format("ATM 序號重複! 原交易EJFNO=%1$s,TX_CODE=%2$s,TX_DATE=%3$s,TX_TIME=%4$s,REPLY_CODE=%5$s",
								oEJ, oTX_CODE, oTX_DATE, oTX_TIME, oREPLY_CODE));
				this.logMessage(getLogContext());
			}
			return rtn;

		} catch (Exception ex) {
			throw ex;
		}
	}

	public FEPReturnCode checkATMCHK() {
		BigDecimal value1 = new BigDecimal(0);
		BigDecimal value2 = new BigDecimal(0);
		BigDecimal value3 = new BigDecimal(0);
		BigDecimal value4 = new BigDecimal(0);
		BigDecimal value5 = new BigDecimal(0);
		BigDecimal value6 = new BigDecimal(0);
		String valueA = "";
		String valueB = "";
		String valueC = "";
		try {
			// 將晶片卡主帳號(16位)分成前後8位

			//--ben-20220922-//if (atmReq.getCHACT().length() < 16 && StringUtils.isNumeric(atmReq.getCHACT())) {
			//--ben-20220922-//	return ATMReturnCode.OtherCheckError;
			//--ben-20220922-//} else {
			//--ben-20220922-//	value1 = new BigDecimal(atmReq.getCHACT().substring(0, 8));
			//--ben-20220922-//	value2 = new BigDecimal(atmReq.getCHACT().substring(8));
			//--ben-20220922-//}

			// 交易日期DDMMMMDD(8位)
			// 取 ATM上行電文ICDTTM欄位(YYYYMMDDHHMMSS), 如05/25就變成25050525
			// 交易的時間轉換成秒(00000~86399)，右靠左補0補滿8位
			// 如交易時間為13:15:21轉成秒為13*3600 + 15*60 + 21 = 47721è00047721
			//--ben-20220922-//if (atmReq.getICDTTM().length() < 14 && StringUtils.isNumeric(atmReq.getICDTTM())) {
			//--ben-20220922-//	return ATMReturnCode.OtherCheckError;
			//--ben-20220922-//} else {
			//--ben-20220922-//	value3 = new BigDecimal(atmReq.getICDTTM().substring(6, 8) + atmReq.getICDTTM().substring(4, 6)
			//--ben-20220922-//			+ atmReq.getICDTTM().substring(4, 8));
			//--ben-20220922-//	value4 = new BigDecimal(atmReq.getICDTTM().substring(8, 10)).multiply(new BigDecimal(3600))
			//--ben-20220922-//			.add(new BigDecimal(atmReq.getICDTTM().substring(10, 12)).multiply(new BigDecimal(60))
			//--ben-20220922-//					.add(new BigDecimal(atmReq.getICDTTM().substring(12, 14))));
			//--ben-20220922-//}

			// 放兩次ATM交易序號右靠後4位共8位
			// 如ATM交易序號為00038665, 則此欄為86658665
			if (getFeptxn() != null && getFeptxn().getFeptxnAtmSeqno().length() == 8
					&& StringUtils.isNumeric(getFeptxn().getFeptxnAtmSeqno())) {
				value5 = new BigDecimal(
						getFeptxn().getFeptxnAtmSeqno().substring(4) + getFeptxn().getFeptxnAtmSeqno().substring(4));
			}

			// 放 秒數 * 帳號 後4位+0000 , 如交易時間為13:15:21 => 21 * 1549 => 32529 => 25290000
			//--ben-20220922-//value6 = new BigDecimal(atmReq.getICDTTM().substring(12, 14))
			//--ben-20220922-//		.multiply(new BigDecimal(atmReq.getCHACT().substring(12, 16)).multiply(new BigDecimal(10000)));

			if(value1.compareTo(BigDecimal.ZERO) != 0) {
				// 將上面的五個步驟欄位相加再乘以37之後取後8位得出A值
				valueA = value1.add(value2.add(value3.add(value4.add(value5.add(value6))))).multiply(new BigDecimal(37)).toString();
				valueA = valueA.substring(valueA.length() - 8);

				// 99999090減掉A的值，每個數字個別相減，0不處理得出B值
				// 99999090:
				// - 73790168
				// = 26209138:
				String tmpStr = "99999090";
				@SuppressWarnings("unused")
				int tmpInt = 0;
				for (int i = 0; i <= 7; i++) {
					if (!"0".equals(tmpStr.substring(i, i + 1))) {
						valueB += String.valueOf(Integer.valueOf(tmpStr.substring(i, i + 1))
								- Integer.valueOf(valueA.substring(i, i + 1)));
					} else {
						valueB += valueA.substring(i, i + 1);
					}
				}

				// 將B值與常數10100011 XOR得出C值, 26209138 XOR 10100011得出C值 36309129
				valueC = exclusiveOREncryption(valueB, "10100011");

				// 比對ATM電文端末設備查核碼欄位
				if (!valueC.equals(getFeptxn().getFeptxnAtmChk())) {
					// 2013/08/22 modify by Ruling for 本行ATM電文欄位端末設備查核碼:加log
					getLogContext().setRemark("ATMID=" + getFeptxn().getFeptxnAtmno() + " ATMReq 查核碼="
							+ getFeptxn().getFeptxnAtmChk() + ", FEP 計算的 ATM 查核碼=" + valueC);
					this.logMessage(getLogContext());
					return ATMReturnCode.OtherCheckError;
				}
			}


			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setRemark("CheckATMCHK exception: " + ex.getMessage());
			getLogContext().setReturnCode(CommonReturnCode.ProgramException);
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 2021-07-19 Richard add
	 * 
	 * 檢核跨行存款交易日限額
	 * New Function for ATM新功能：跨行存款和現金捐款
	 * 
	 * @return
	 */
	public FEPReturnCode checkODRDLimit() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		int limit = 0;
		String self = StringUtils.EMPTY;
		OdrcExtMapper odrcExtMapper = SpringBeanFactoryUtil.getBean(OdrcExtMapper.class);
		try {
			if (this.feptxn.getFeptxnTroutBkno().equals(this.feptxn.getFeptxnTrinBkno())
					&& this.feptxn.getFeptxnMajorActno().equals(this.feptxn.getFeptxnTrinActno())) {
				// 存入帳戶為卡片帳號
				limit = INBKConfig.getInstance().getODRDSLimit();
				self = "本人";
			} else {
				// 存入帳戶非卡片帳號
				limit = INBKConfig.getInstance().getODRDNLimit();
				self = "非本人";
			}
			if (limit == 0) {
				rtnCode = IOReturnCode.QueryNoData;
				this.logContext.setRemark("CheckODRDLimit-取得SYSCONF的限額資料為0");
				this.logMessage(this.logContext);
				return rtnCode;
			}

			// 累計跨行存款每日交易金額
			Odrc odrc = new Odrc();
			odrc.setOdrcTxDate(this.feptxn.getFeptxnTxDate());
			odrc.setOdrcBkno(this.feptxn.getFeptxnTroutBkno());
			odrc.setOdrcActno(this.feptxn.getFeptxnMajorActno());
			odrc.setOdrcCurcd(this.feptxn.getFeptxnTxCur());
			Map<String, Object> result = odrcExtMapper.getOdrcByDay(odrc);
			if (MapUtils.isNotEmpty(result)) {
				BigDecimal totalAmt = (BigDecimal) result.get("TOT_AMT");
				BigDecimal accumulateAmt = this.feptxn.getFeptxnTxAmt().add(totalAmt);
				if (MathUtil.compareTo(accumulateAmt, limit) > 0) {
					rtnCode = CommonReturnCode.OverLimit;
					this.logContext.setRemark(StringUtils.join(
							"CheckODRDLimit-", self, "超過", limit, "限額，本次交易金額=", this.feptxn.getFeptxnTxAmt().toString(), "，本日累計金額=", accumulateAmt.toString()));
					this.logMessage(this.logContext);
					return rtnCode;
				}
			} else {
				// 2021-07-20 Richard add mark 應該永遠不會走到這個else中
				rtnCode = IOReturnCode.QueryNoData;
				this.logContext.setRemark("CheckODRDLimit-查不到日累計資料");
				this.logMessage(this.logContext);
				return rtnCode;
			}
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".checkODRDLimit"));
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	public final FEPReturnCode checkTrfGCard() {
		try {
			if ((ATMTXCD.IPA.name().equals(getFeptxn().getFeptxnTxCode()))
					|| (ATMTXCD.EFT.name().equals(getFeptxn().getFeptxnTxCode()))) {
				/// * 預約轉帳/繳費/永豐錢卡不得轉入 GIFT 卡 */
				if (BINPROD.Gift.equals(getFeptxn().getFeptxnTrinKind())) {
					return FEPReturnCode.TranInACTNOError; /// * 轉入帳號錯誤 */
				}
				// 1/16 修改 */
				if (!"00".equals(StringUtils.rightPad(getFeptxn().getFeptxnTroutActno(), 2, ' ').substring(0, 2))) {
					// 轉出帳號前二碼<>’00’, 需回ATM錯誤代碼 */
					return FEPReturnCode.TranOutACTNOError; // 轉出帳號錯誤
				}

			} else if (ATMTXCD.BFT.name().equals(getFeptxn().getFeptxnTxCode())) {
				/// * 預約轉帳/繳費/永豐錢卡不得轉入 GIFT 卡 */
				if (BINPROD.Gift.equals(getFeptxn().getFeptxnTrinKind())) {
					return FEPReturnCode.TranInACTNOError; /// * 轉入帳號錯誤 */
				}

			} else if (ATMTXCD.IFT.toString().equals(getFeptxn().getFeptxnTxCode())) {
				/// * 預借現金轉帳不得轉入 GIFT 卡 */
				if ((BINPROD.Credit.equals(getFeptxn().getFeptxnTroutKind())
						|| BINPROD.Combo.equals(getFeptxn().getFeptxnTroutKind())
						|| BINPROD.Gift.equals(getFeptxn().getFeptxnTroutKind()))
						&& BINPROD.Gift.equals(getFeptxn().getFeptxnTrinKind())) {
					return FEPReturnCode.TranInACTNOError; /// * 轉入帳號錯誤 */
				}
			}

		} catch (Exception ex) {
			logContext.setProgramException(ex);
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	/**
	 * 檢核掌靜脈驗證資料
	 * 
	 * @return
	 *         FEPReturnCode
	 * 
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Ruling</modifier>
	 *         <reason>ATM提升分行離櫃率_全自助：掌靜脈</reason>
	 *         <date>2020/10/08</date>
	 *         </modify>
	 *         </history>
	 */
	public FEPReturnCode checkPVData() throws Exception {
		FEPReturnCode rtnCode = ATMReturnCode.OtherCheckError;
		Feptxn oFEPTXN = new FeptxnExt();
		FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
		List<Feptxn> dt = new ArrayList<>();

		try {
			feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), "ATM.checkPVData");
			// 本營業日
			//--ben-20220922-//oFEPTXN.setFeptxnTxDateAtm(atmReq.getOriAtmseq_1());
			//--ben-20220922-//oFEPTXN.setFeptxnAtmno(StringUtils.join(atmReq.getBRNO(), atmReq.getWSNO()));
			//--ben-20220922-//oFEPTXN.setFeptxnAtmSeqno(atmReq.getOriAtmseq_2());
			oFEPTXN.setFeptxnTxCode(ATMTXCD.PIQ.toString());
			//--ben-20220922-//oFEPTXN.setFeptxnIdno(atmReq.getIDNO());
			oFEPTXN.setFeptxnAscRc("0");
			dt = feptxnDao.queryFEPTXNForCheckPVDATA(oFEPTXN.getFeptxnTxDateAtm(), oFEPTXN.getFeptxnAtmno(), oFEPTXN.getFeptxnAtmSeqno(), oFEPTXN.getFeptxnTxCode(), oFEPTXN.getFeptxnIdno(),
					oFEPTXN.getFeptxnAscRc());
			if (dt != null) {
				if (dt.size() >= 1) {
					// 有找到原交易
					// 將該筆交易 EJFNO 寫入 PIQ 的 FEPTXN_CHANNEL_EJFNO
					oFEPTXN = new FeptxnExt();
					oFEPTXN.setFeptxnTxDate(dt.get(0).getFeptxnTxDate());
					oFEPTXN.setFeptxnEjfno(dt.get(0).getFeptxnEjfno());
					oFEPTXN.setFeptxnChannelEjfno(getFeptxn().getFeptxnEjfno().toString());
					if (feptxnDao.updateByPrimaryKeySelective(oFEPTXN) <= 0) {
						getLogContext().setRemark("檢核掌靜脈驗證資料-原PIQ更新FEPTXN失敗");
						this.logMessage(getLogContext());
						return IOReturnCode.UpdateFail;
					}

					// 將查到的PIQ EJFNO寫入FEPTXN
					getFeptxn().setFeptxnChannelEjfno(dt.get(0).getFeptxnEjfno().toString());
					rtnCode = CommonReturnCode.Normal;
				}
			} else {
				rtnCode = ATMReturnCode.OtherCheckError;
			}

			if (rtnCode == CommonReturnCode.Normal) {
				return rtnCode;
			}

			// 上營業日
			feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 8), "ATM.checkPVData");
			//--ben-20220922-//oFEPTXN.setFeptxnTxDateAtm(atmReq.getOriAtmseq_1());
			//--ben-20220922-//oFEPTXN.setFeptxnAtmno(StringUtils.join(atmReq.getBRNO(), atmReq.getWSNO()));
			//--ben-20220922-//oFEPTXN.setFeptxnAtmSeqno(atmReq.getOriAtmseq_2());
			oFEPTXN.setFeptxnTxCode(ATMTXCD.PIQ.toString());
			//--ben-20220922-//oFEPTXN.setFeptxnIdno(atmReq.getIDNO());
			oFEPTXN.setFeptxnAscRc("0");
			dt = feptxnDao.queryFEPTXNForCheckPVDATA(oFEPTXN.getFeptxnTxDateAtm(), oFEPTXN.getFeptxnAtmno(), oFEPTXN.getFeptxnAtmSeqno(), oFEPTXN.getFeptxnTxCode(), oFEPTXN.getFeptxnIdno(),
					oFEPTXN.getFeptxnAscRc());
			if (dt != null) {
				if (dt.size() >= 1) {
					// 有找到原交易
					// 將該筆交易 EJFNO 寫入 PIQ 的 FEPTXN_CHANNEL_EJFNO
					oFEPTXN = new FeptxnExt();
					oFEPTXN.setFeptxnTxDate(dt.get(0).getFeptxnTxDate());
					oFEPTXN.setFeptxnEjfno(dt.get(0).getFeptxnEjfno());
					oFEPTXN.setFeptxnChannelEjfno(getFeptxn().getFeptxnEjfno().toString());
					if (feptxnDao.updateByPrimaryKeySelective(oFEPTXN) <= 0) {
						getLogContext().setRemark("檢核掌靜脈驗證資料-原PIQ更新FEPTXN失敗");
						this.logMessage(getLogContext());
						return IOReturnCode.UpdateFail;
					}

					// 將查到的PIQ EJFNO寫入FEPTXN
					getFeptxn().setFeptxnChannelEjfno(dt.get(0).getFeptxnEjfno().toString());
					rtnCode = CommonReturnCode.Normal;
				}
			} else {
				rtnCode = ATMReturnCode.OtherCheckError;
			}

			if (rtnCode == ATMReturnCode.OtherCheckError) {
				getLogContext().setRemark(StringUtils.join("找不到檢核掌靜脈驗證資料, FEPTXN_TX_DATE_ATM=", oFEPTXN.getFeptxnTxDateAtm(), ", ATMNO=", oFEPTXN.getFeptxnAtmno(), ", ATM_SEQNO=",
						oFEPTXN.getFeptxnAtmSeqno(), ", TX_CODE=", oFEPTXN.getFeptxnTxCode(), ", IDNO=", oFEPTXN.getFeptxnIdno(), ", ASC_RC=", oFEPTXN.getFeptxnAscRc()));
				this.logMessage(getLogContext());
			}

			return rtnCode;
		} catch (Exception ex) {
			throw ex;
		}
	}

	private String exclusiveOREncryption(String a, String b) {
		String encrypRes = "";
		String aBin = null;
		String bBin = null;

		if (a.trim().length() == b.trim().length()) {
			for (int i = 0; i < a.length(); i++) {
				if (!"F".equals(a.substring(i, i + 1))) {
					aBin = toBinary(Integer.parseInt(a.substring(i, i + 1)));
				} else {
					aBin = "1111";
				}
				if (!"F".equals(b.substring(i, i + 1))) {
					bBin = toBinary(Integer.parseInt(b.substring(i, i + 1)));
				} else {
					bBin = "1111";
				}
				encrypRes = encrypRes + decimalToHex(toDecimal(sXORBinstr(aBin, bBin)));
			}
		}
		return encrypRes;
	}

	private String toBinary(int dec) {
		//// Declare a few variables we're going to need
		int BinaryHolder = 0;
		// char[] BinaryArray;
		@SuppressWarnings("unused")
		char[] BinaryArray = null;
		String BinaryResult = "";

		while (dec > 0) {

			BinaryHolder = dec % 2;
			BinaryResult = BinaryResult + BinaryHolder;
			dec = MathUtil.roundFloor(dec / 2, 0).intValue();
		}

		//// The algoritm gives us the binary number in reverse order (mirrored)
		//// We store it in an array so that we can reverse it back to normal
		BinaryResult = new StringBuilder(BinaryResult).reverse().toString();
		BinaryResult = StringUtils.leftPad(BinaryResult, 4, '0');
		return BinaryResult;
	}

	private String sXORBinstr(String sInputStr1, String sInputStr2) {
		String tempsXORBinstr = null;
		tempsXORBinstr = "";
		if (sInputStr1.trim().length() == sInputStr2.trim().length()) {
			for (int i = 1; i <= sInputStr1.trim().length(); i++) {
				tempsXORBinstr = tempsXORBinstr + (Long.parseLong(sInputStr1.substring(i - 1, i - 1 + 1))
						^ Long.parseLong(sInputStr2.substring(i - 1, i - 1 + 1)));
			}
		}
		return tempsXORBinstr;
	}

	private String toDecimal(String bin) {
		BigDecimal DecimalResult = new BigDecimal(0);

		for (int i = 0; i < bin.length(); i++) {
			DecimalResult = BigDecimal.valueOf(DecimalResult.intValue() + BigDecimal.valueOf(Integer.valueOf(bin.substring(i, i + 1)) * Math.pow(2, bin.length() - i - 1)).intValue());
		}

		return DecimalResult.toString();
	}

	private String decimalToHex(String a) {

		switch (a) {
			case "10":
				return "A";
			case "11":
				return "B";
			case "12":
				return "C";
			case "13":
				return "D";
			case "14":
				return "E";
			case "15":
				return "F";
			default:
				return a;
		}
	}
	
	public FEPReturnCode CheckATMData() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			// 1.	檢核ATM 入帳日與地區檔的本營業日是否相符
			Zone defZone = getZoneByZoneCode(atmMstr.getAtmZone());
			if (defZone == null) {
				return IOReturnCode.ZONENotFound;
			}
			if (!defZone.getZoneTbsdy().equals(getFeptxn().getFeptxnTbsdy())) {
				rtnCode = ATMReturnCode.TBSDYError;
				return rtnCode;
			}

			 //2.	檢核自行還未換日時，需執行換日
			LocalDate zoneTbsdy= LocalDate.parse(defZone.getZoneTbsdy(), DateTimeFormatter.ofPattern("yyyyMMdd"));
			LocalDate sysstatTbsdyFisc= LocalDate.parse(SysStatus.getPropertyValue().getSysstatTbsdyFisc(), DateTimeFormatter.ofPattern("yyyyMMdd"));
			LocalTime currentTime = LocalTime.now();
			LocalTime targetTime = LocalTime.of(18, 0);
			if(zoneTbsdy.isBefore(sysstatTbsdyFisc) && currentTime.isAfter(targetTime) && DbHelper.toBoolean(defZone.getZoneChgday())){
				rtnCode = ChangeCBSDate(defZone);
				if(rtnCode != FEPReturnCode.Normal){
					return rtnCode;
				}
			}
			
//			3.	檢核前端通道是否提供服務
			// 因EATM 的晶片密碼變更交易(P1)送電文前已完成變更，故無法在FEP控制
			if (StringUtils.equals("EAT", getFeptxn().getFeptxnChannel().toString()) && StringUtils.equals("P1", getFeptxn().getFeptxnTxCode())) {
			} else {
				Channel channel = this.getChannel(getFeptxn().getFeptxnChannel());
				if (channel == null) {
					rtnCode = FEPReturnCode.CHANNELNotFound;
					// CHANNEL資料不存在
					getLogContext().setRemark("CHANNEL資料不存在, CHANNEL =" + getFeptxn().getFeptxnChannel());
					this.logMessage(getLogContext());
					return rtnCode;
				} else if (channel.getChannelEnable() == 0) {
					rtnCode = FEPReturnCode.ChannelServiceStop;
					// {FEPTXN_CHANNEL}暫停服務
					getLogContext().setRemark(getFeptxn().getFeptxnChannel() + "暫停服務");
					this.logMessage(getLogContext());
					return rtnCode;
				}
			}
			
//			4.	檢核業務別交易是否提供服務
			// 因EATM 的晶片密碼變更交易(P1)送電文前已完成變更，故無法在FEP控制
			if (StringUtils.equals("EAT", getFeptxn().getFeptxnChannel().toString()) && StringUtils.equals("P1", getFeptxn().getFeptxnTxCode())) {
			}else if(getAtmTxData().getMsgCtl().getMsgctlStatus() == 0) {
				if(getFeptxn().getFeptxnFiscFlag() == 0) { // 自行交易
					//自行{MSGCTL_MSG_NAME }交易暫停服務
					getLogContext().setRemark("自行 "+ getAtmTxData().getMsgCtl().getMsgctlMsgName() +" 交易暫停服務");
					this.logMessage(getLogContext());
					rtnCode = FEPReturnCode.IntraBankSingleServiceStop;
				}else {
					//代理行{MSGCTL_MSG_NAME }交易暫停服務
					getLogContext().setRemark("代理行 "+ getAtmTxData().getMsgCtl().getMsgctlMsgName() +" 交易暫停服務");
					this.logMessage(getLogContext());
					rtnCode = FEPReturnCode.AgentBankSingleServiceStop;
				}
				return rtnCode;
			}
			
//			5.	檢核手機門號GW是否提供服務
			if (StringUtils.equals(getFeptxn().getFeptxnMtp(), "Y")) {   // 手機門號轉帳交易
				if (SysStatus.getPropertyValue().getSysstatMtp() == 0) {    // 手機門號GW狀態
					//呼叫手機門號平台失敗
					rtnCode = FEPReturnCode.MobileAPIError;
					return rtnCode;
				}
			}

//			6.	檢核FIDO 平台GW是否提供服務
			if (StringUtils.equals(getFeptxn().getFeptxnTxCode(), "LF")) { // FIDO 金融帳戶核驗交易
				if(SysStatus.getPropertyValue().getSysstatFido() == 0) { // FIDO GW狀態
					//呼叫FIDO平台失敗
					rtnCode = FEPReturnCode.FIDOAPIError;
					return rtnCode;
				}
			}

//			7.	轉帳交易檢核轉入帳號
			if (getFeptxn().getFeptxnTxCode() != null 
					&& (getFeptxn().getFeptxnTxCode().substring(0, 1).equals("T"))
					&& !getFeptxn().getFeptxnPcode().substring(0, 3).equals("253")) {
				if (StringUtils.isBlank(getFeptxn().getFeptxnTrinActno())) {
					rtnCode = ATMReturnCode.TranInACTNOError;
					//轉入帳號狀況檢核錯誤, 轉入帳號={ FEPTXN_TRIN_ACTNO }
					getLogContext().setRemark("轉入帳號狀況檢核錯誤, 轉入帳號=" + getFeptxn().getFeptxnTrinActno());
					this.logMessage(getLogContext());
					return rtnCode;
				}
			}
			
//			8.	檢核銀行別及交易帳號
			if (getFeptxn().getFeptxnTxCode() != null 
					&& (getFeptxn().getFeptxnTxCode().substring(0, 1).equals("T"))) {
				/// * 檢核轉入/轉出帳號是否相同 */
				if ((getFeptxn().getFeptxnTroutActno().equals(getFeptxn().getFeptxnTrinActno())
								&& getFeptxn().getFeptxnTroutBkno().equals(getFeptxn().getFeptxnTrinBkno()))) {
					rtnCode = ATMReturnCode.TranInACTNOSameAsTranOut;
					//轉出入帳號相同，不更新磁軌且不留置卡片, 轉出帳號={ FEPTXN_TROUT_ACTNO }
					getLogContext().setRemark("轉出入帳號相同，不更新磁軌且不留置卡片, 轉出帳號=" + getFeptxn().getFeptxnTroutActno());
					this.logMessage(getLogContext());
					return rtnCode;
				}
			}
			
//			9.	檢核外幣提款匯率一定要有值
			if ( (ATMTXCD.US.name().equals(getFeptxn().getFeptxnTxCode())|| ATMTXCD.JP.name().equals(getFeptxn().getFeptxnTxCode())) 
					&& getFeptxn().getFeptxnExrate().compareTo(BigDecimal.ZERO) == 0) {
					rtnCode = ATMReturnCode.NoRateTable;
					getLogContext().setRemark("CheckBody-檢核外幣無卡提款交易的匯率不能為0");
					this.logMessage(getLogContext());
					return rtnCode;
			}

//			10.	檢核跨行存款/預借現金轉帳不得繳納口罩費用
			/* 跨行存款/預借現金轉帳不得繳納口罩費用 */
			if ("M".equals(getFeptxn().getFeptxnBenefit()) && (
					("D5".equals(getFeptxn().getFeptxnTxCode()) || "D6".equals(getFeptxn().getFeptxnTxCode()) || "D7".equals(getFeptxn().getFeptxnTxCode()))
					|| StringUtils.isNotBlank(getFeptxn().getFeptxnTroutKind()))) {
				rtnCode = ATMReturnCode.OtherCheckError;
				getLogContext().setRemark("跨行存款或預借現金轉帳不得繳納口罩費用");
				this.logMessage(getLogContext());
				return rtnCode;
			}
			
//			11.	檢核EATM 資料
			if ("EAT".equals(getFeptxn().getFeptxnChannel())) {/* EATM*/
//				if (StringUtils.isNotBlank(getFeptxn().getFeptxnAtmChk())) {
//					/* 檢核ATM電文端末設備查核碼 */
//					rtnCode = checkATMCHK();
//					if (rtnCode != CommonReturnCode.Normal) {
//						getLogContext()
//								.setRemark("CheckBody-檢核本行ATM電文欄位端末設備查核碼-不等於Normal, rtnCode = " + rtnCode.toString());
//						this.logMessage(getLogContext());
//						return rtnCode;
//					}
//				}
			}

//			12.	轉出帳號Call 防詐API
			if ( "ATM".equals(getFeptxn().getFeptxnChannel()) && StringUtils.isNotBlank(getFeptxn().getFeptxnPcode())) {
				if ((getFeptxn().getFeptxnPcode().startsWith("251") || getFeptxn().getFeptxnPcode().startsWith("252")
						|| "D5".equals(getFeptxn().getFeptxnTxCode()) || "D6".equals(getFeptxn().getFeptxnTxCode())
						|| "D7".equals(getFeptxn().getFeptxnTxCode()) || "WF".equals(getFeptxn().getFeptxnTxCode()))
						&& !StringUtils.equals(getFeptxn().getFeptxnTxCode(), "W2")
						&& (!StringUtils.isBlank(getFeptxn().getFeptxnTroutBkno()) && !StringUtils.isBlank(getFeptxn().getFeptxnTroutActno()))) {

					// 這裡先將預設的MDC取出來, 下面再put進去, 否則log檔名會跑掉
					final Map<String, String> map = getMDCKeptValue();
					//將轉出帳號丟到警政署防詐平台，不須等待API回覆
					Thread thread = new Thread(ThreadWrapper.wrap(() -> {
						LogMDC.put(map);
						try {
							String secK = ATMPConfig.getInstance().getATMGrayListSecretKey();
							String txType = "";
							if (getFeptxn().getFeptxnPcode().startsWith("251") || "WF".equals(getFeptxn().getFeptxnTxCode())) {
								txType = "提款";
							} else if (getFeptxn().getFeptxnPcode().startsWith("252")) {
								txType = "轉帳";
							} else {
								txType = "存款";
							}
							
							String i_troutactno = StringUtils.EMPTY;
							String i_txDateTime = StringUtils.join(getFeptxn().getFeptxnTxDate(), getFeptxn().getFeptxnTxTime());
							
							if (StringUtils.equals(getFeptxn().getFeptxnTxCode(), "W3") 
									&& StringUtils.equals(getFeptxn().getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())
									&& StringUtils.isNotBlank(getFeptxn().getFeptxnIcmark())) {
								i_troutactno = StringUtils.leftPad(StringUtil.fromHex(StringUtils.substring(getFeptxn().getFeptxnIcmark(), 0, 26)), 16, "0"); // FEPTXN_ICMARK[1 :26] 十六進制字串轉ASCII
							}else if(StringUtils.equals(getFeptxn().getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
								if(StringUtils.equals(StringUtils.substring(getFeptxn().getFeptxnTroutActno(), 0, 3), "000")) {
									i_troutactno = getFeptxn().getFeptxnTroutActno();
								}else {
									i_troutactno =  StringUtils.leftPad(StringUtils.substring(getFeptxn().getFeptxnTroutActno(), 0, 13), 16, "0") ; // FEPTXN_TROUT_ACTNO[1 :13] 左補 0 滿
								}
							}else {
								i_troutactno = getFeptxn().getFeptxnTroutActno();
							}

							String result = callGrayListAPI(getFeptxn().getFeptxnTroutBkno(), i_troutactno, getFeptxn().getFeptxnAtmno(),
									i_txDateTime, "ATM", secK, txType, getLogContext().getEj());
							if (result != null) {
								getLogContext().setRemark("防詐API查詢成功: " + result);
								this.logMessage(getLogContext());
							} else {
								getLogContext().setRemark("防詐API查詢失敗");
								this.logMessage(getLogContext());
							}
						} catch (Exception e) {
							getLogContext().setProgramException(e);
							getLogContext().setProgramName(ProgramName + ".callGrayListAPI");
							this.logMessage(getLogContext());
							sendEMS(getLogContext());
						}
					}));
					thread.start();
					try {
						thread.join(10 * 1000); // 10秒
						if (thread.isAlive()) { // 強制中斷
							thread.interrupt();
							getLogContext().setRemark("Call 防詐API執行緒超時，強制中斷");
							this.logMessage(getLogContext());
						}
					} catch (InterruptedException e) {
						getLogContext().setProgramException(e);
						getLogContext().setProgramName(ProgramName + ".shutdownThread");
						this.logMessage(getLogContext());
					}
				}
			}

			//13. 檢核ATM金融帳戶核驗業務
			/* FIDO(FD)、全民普發一萬(TT) */
			if ("LF".equals(feptxn.getFeptxnTxCode()) && !"FD".equals(feptxn.getFeptxnActivityCode())
					&& !"TT".equals(feptxn.getFeptxnActivityCode())) {
				rtnCode = FEPReturnCode.OtherCheckError; /* E711:其他類檢核錯誤 */
				getLogContext().setRemark("ATM無此金融帳戶核驗內容: " + feptxn.getFeptxnActivityCode());
				this.logMessage(getLogContext());
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".checkEFTCCard");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

    private String callGrayListAPI(String bankNo, String actNo, String atmNo, String txDateTime, String systemID, String secK, String txType, int logEj) {
        // 1. 建立請求主體物件
        GraylistRequestExt requestBody = new GraylistRequestExt();
        requestBody.setI_ALLBANK_BKNO(bankNo);
        requestBody.setI_TROUT_ACTNO(actNo);
        requestBody.setI_ATM_ATMNO(atmNo);
        requestBody.setI_TxDateTime(txDateTime);
        requestBody.setI_SystemID(systemID);
        requestBody.setI_SecK(secK);
        requestBody.setI_TxType(txType);
		requestBody.setI_LogEj(logEj);

        // 2. 設定請求標頭 (指定內容類型為 JSON)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3. 建立 HttpEntity，包含請求主體和標頭
        HttpEntity<GraylistRequestExt> requestEntity = new HttpEntity<>(requestBody, headers);
        getLogContext().setMessage("Request AllHeaders:" + headers);
        getLogContext().setRemark("Calling GrayList API at URL: " + graylisturl);
        this.logMessage(getLogContext());

        try {
            // 4. 發送 POST 請求並取得回應
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
//					"http://localhost:8083/WebUtils/GRAYLISTQuery",
//					ATMPConfig.getInstance().getGrayListCheckUrl(),
                    graylisturl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            // 5. 處理回應
            if (response.getStatusCode().is2xxSuccessful()) {
                // 請求成功 (HTTP 狀態碼 2xx)
                return response.getBody(); // 返回回應"OK"
            } else {
                // 請求失敗 (HTTP 狀態碼非 2xx)
                getLogContext().setRemark("API call failed. Status Code: " + response.getStatusCode() + ", Response Body: " + response.getBody());
                this.logMessage(getLogContext());
                return null;
            }
        } catch (Exception e) {
            getLogContext().setProgramException(e);
            getLogContext().setRemark("Call 防詐API失敗: " + e.getMessage());
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".callGrayListAPI"));
            this.logMessage(getLogContext());
            return null;
        }
    }

	public Channel getChannel(String channelName) {
		Channel channel = null;
		if(StringUtils.isNotBlank(channelName)) {
			List<Channel> channelList = channelExtMapper.selectByChannelName(channelName);
			if(channelList.size() > 0) {
				channel = channelList.get(0);
			}
		}
		return channel;
	}
}
