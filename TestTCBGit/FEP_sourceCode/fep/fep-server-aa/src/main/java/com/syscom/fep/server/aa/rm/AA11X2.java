package com.syscom.fep.server.aa.rm;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefInt;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.AllbankExtMapper;
import com.syscom.fep.mybatis.ext.mapper.Rmfiscin1ExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RminExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RminsnoExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmintExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmoutExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmouttExtMapper;
import com.syscom.fep.mybatis.model.Allbank;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.mybatis.model.Rmfiscin1;
import com.syscom.fep.mybatis.model.Rmin;
import com.syscom.fep.mybatis.model.Rminsno;
import com.syscom.fep.mybatis.model.Rmint;
import com.syscom.fep.mybatis.model.Rmout;
import com.syscom.fep.mybatis.model.Rmoutt;
import com.syscom.fep.mybatis.util.SpCaller;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.RMINStatus;
import com.syscom.fep.vo.constant.RMOUTStatus;
import com.syscom.fep.vo.constant.RMOUT_ORIGINAL;
import com.syscom.fep.vo.constant.RMPCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.vo.text.rm.RMGeneral;

/**
 * 本支負責處理電文如下
 * 
 * @author ChenYang
 * @create 2021/9/28
 */
public class AA11X2 extends RMAABase {

	private String rtnMessage = "";
	// this _rtnCode is for FISC RC
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private Rmin _defRmin = new Rmin();
	private Rmint _defRmint = new Rmint();
	private String wkOrgFepno = "";
	private String fiscReqTxdate = "";

	private RminExtMapper rminMapper = SpringBeanFactoryUtil.getBean(RminExtMapper.class);
	private RmintExtMapper rmintMapper = SpringBeanFactoryUtil.getBean(RmintExtMapper.class);
	private RmoutExtMapper rmoutMapper = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);
	private RmouttExtMapper rmouttMapper = SpringBeanFactoryUtil.getBean(RmouttExtMapper.class);
	private RminsnoExtMapper rminsnoExtMapper = SpringBeanFactoryUtil.getBean(RminsnoExtMapper.class);
	private Rmfiscin1ExtMapper rmfiscin1ExtMapper = SpringBeanFactoryUtil.getBean(Rmfiscin1ExtMapper.class);
	private AllbankExtMapper allBankMapper = SpringBeanFactoryUtil.getBean(AllbankExtMapper.class);
	private SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);
	/**
	 * AA的建構式,在這邊初始化及設定其他相關變數
	 * 
	 * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件)
	 * @throws Exception
	 */
	public AA11X2(FISCData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * AA進入點主程式
	 */

	/**
	 * 程式進入點
	 * 
	 * @return Response電文
	 */
	@Override
	public String processRequestData() throws Exception {
		String repMac = "";
		Integer wkRepk = 0;
		String wkRc = "";
		Boolean autoBack = false;
		String wkBackReason = "";
		String wkFepno = "";
		String repUnitBank = "";
		Rmout defRmout = new Rmout();
		Boolean ifFedi = false;
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		getLogContext().setBkno(SysStatus.getPropertyValue().getSysstatFbkno());

		try {

			// 1.檢核財金電文,若為Garble則組回覆訊息(SendGarbledMessage)，程式結束
			_rtnCode = this.checkFISCRMReq();
			getLogContext().setRemark(StringUtils.join("Begin AA11X2, SENDER_BANK=", getmFISCRMReq().getSenderBank(), ", RECEIVER_BANK=", getmFISCRMReq().getReceiverBank(), ", FISCNO=",
					getmFISCRMReq().getFiscNo(), ", BANKNO=", getmFISCRMReq().getBankNo(), ", CheckFISCRMReq rtnCode=", _rtnCode.toString()));
			logMessage(Level.DEBUG, getLogContext());

			// 2.判斷是否是Garbled Message
			if (_rtnCode != CommonReturnCode.Normal) {
				// 判斷是否是Garbled Message
				if ("10".equals(getmFISCBusiness().getFISCRCFromReturnCode(_rtnCode).substring(0, 2))) {
					getmFISCBusiness().sendGarbledMessage(getmFISCRMReq().getEj(), _rtnCode, getmFISCRMReq());
					return "";
				}
			}

			// 3.Prepare交易記錄初始資料, 新增交易記錄(FEPTXN)-除了電文中交易日期時間為非數字其他交易均需要記錄
			if (_rtnCode == CommonReturnCode.Normal || StringUtils.isNumeric(getmFISCRMReq().getTxnInitiateDateAndTime().substring(0, 6))) {
				// 2021-10-26 Richard modified
				// Candy建議參考別的系統進行如下的調整
				// _rtnCode = this.prepareAndInsertFEPTXN();
				this.prepareAndInsertFEPTXN();
			}

			// Fly 2015/06/15 若insert FEPTXN 失敗，則不應該繼續往下處理
			if (_rtnCode == CommonReturnCode.Normal) {

				RefString refRepMac = new RefString(repMac);
				RefInt refWkRepk = new RefInt(wkRepk);
				RefString refWkRc = new RefString(wkRc);
				RefString refRepUnitBank = new RefString(repUnitBank);
				RefBase<Rmout> refDefRmout = new RefBase<Rmout>(defRmout);
				RefBase<Boolean> refIfFedi = new RefBase<Boolean>(ifFedi);

				// 4.CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
				rtnCode = this.checkBusinessRule(refRepMac, refWkRepk, refWkRc, refRepUnitBank, refDefRmout, refIfFedi);

				repMac = refRepMac.get();
				wkRepk = refWkRepk.get();
				wkRc = refWkRc.get();
				repUnitBank = refRepUnitBank.get();
				defRmout = refDefRmout.get();
				ifFedi = refIfFedi.get();
				if (rtnCode != CommonReturnCode.Normal) {
					if (_rtnCode == CommonReturnCode.Normal) {
						_rtnCode = rtnCode;
					}
				}

				// 5.Prepare匯入主檔及匯入暫存檔資料, 新增匯入主檔及匯入暫存檔
				RefString refWkFepno = new RefString(wkFepno);
				rtnCode = this.insertRMIN(defRmout, refWkFepno);
				wkFepno = refWkFepno.get();
				if (rtnCode != CommonReturnCode.Normal) {
					if (_rtnCode == CommonReturnCode.Normal) {
						_rtnCode = rtnCode;
					}
				}
			}

			// Jim, 2012/3/1, 將對方行檢核移到回財金之前
			// Jim, 2012/3/14, 上面Check OK才需要check MBank data
			if (_rtnCode == CommonReturnCode.Normal) {
				if (!"1172".equals(getmFISCRMReq().getProcessingCode())) {
					// Jim, 2012/3/13, 不能用_rtnCode來接CheckBodyByMBank的return code, 因為_rtnCode會做為財金的RC
					FEPReturnCode tmpRtnCode = getmFISCBusiness().checkBodyByMBank(rminsnoExtMapper, repUnitBank, "0", autoBack, wkBackReason);

					getLogContext().setRemark(StringUtils.join("After CheckBodyByMBank rtnCode=", tmpRtnCode.getValue(), "; autoBack=", autoBack.toString(), "; wkBACK_REASON=", wkBackReason));
					logMessage(Level.DEBUG, getLogContext());
				}

				this.updateRMINSNO(rminsnoExtMapper, repUnitBank);
			}
			// End of 對方行檢核

			// 6.準備回財金的相關資料並送回覆電文到財金(SendToFISC)
			// Fly 2015/06/15 若insert FEPTXN 失敗，則不應該繼續往下處理
			// 2021-10-26 Richard modified
			// Candy建議修改
			// if (_rtnCode == CommonReturnCode.Normal) {
			if (_rtnCode != IOReturnCode.FEPTXNInsertError) {
				rtnCode = this.prepareAndSendForFISC(repMac);
				if (rtnCode != CommonReturnCode.Normal) {
					if (_rtnCode == CommonReturnCode.Normal) {
						_rtnCode = rtnCode;
					}
				}

				getLogContext().setRemark(StringUtils.join("After CheckBodyByMBank rtnCode=", rtnCode.getValue(), "; autoBack=", autoBack.toString(), "; wkBACK_REASON=", wkBackReason));
				logMessage(Level.DEBUG, getLogContext());
			}

			// Modify by Jim, 2011/02/22, 避免Check到一半還沒有更新RMINSNO序號, 下一筆就緊接著Check RMINSNO, 會造成後續的匯入都檢核錯誤, 造成自動退匯
			// Modify by Jim, 2011/06/07, 回財金OK時才需要更新RMINSNO
			// 7.對方行相關檢核
			if (_rtnCode == CommonReturnCode.Normal) {
				if (NormalRC.FISC_OK.equals(getmFISCRMRes().getResponseCode())) {
					if (!"1172".equals(getmFISCRMReq().getProcessingCode())) {
						if (autoBack) {
							_rtnCode = this.updateRMINForAutoBack(wkBackReason);
						}
					}
				}

				// 8.Prepare()匯出主檔及匯出暫存檔資料, 新增匯出主檔及匯出暫存檔
				if (autoBack) {
					FEPReturnCode tmpRtnCode = this.insertRMOUTAndRMOUTT(wkFepno, wkBackReason);
					getLogContext().setRemark(StringUtils.join("AA11X2-自動退匯 after 新增RMOUT & 新增RMOUTT, rtnCode=", tmpRtnCode));
					getLogContext().setReturnCode(tmpRtnCode);
					logMessage(Level.DEBUG, getLogContext());
				}

				if (NormalRC.FISC_OK.equals(getmFISCRMRes().getResponseCode())) {
					if (!RMPCode.PCode1172.equals(getmFISCRMReq().getProcessingCode())) {
						_rtnCode = this.insertRMINT();
					}
				}

				if (autoBack) {
					this.updateRMINAndRMINTForORGREGNO(wkOrgFepno);
				}

				// 更新跨行代收付/匯兌清算檔/匯兌統計日結檔
				if (NormalRC.FISC_OK.equals(getmFISCRMRes().getResponseCode())) {
					_rtnCode = getmFISCBusiness().processAPTOTAndRMTOTAndRMTOTAL("", getFeptxn());
				}

				// 10.FEDI轉通匯匯出狀態回饋
				if (ifFedi && _rtnCode == CommonReturnCode.Normal) {
					this.callSYNCFEDI(defRmout);
				}
			}
		} catch (Exception ex) {
			logContext.setProgramException(ex);
			sendEMS(logContext);
		} finally {
			getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getLogContext().setMessage(rtnMessage);
			getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getLogContext().setMessageFlowType(MessageFlow.Response);
			logMessage(Level.INFO, this.logContext);
		}
		return "";
	}

	/**
	 * 1.檢核財金Req電文
	 */
	private FEPReturnCode checkFISCRMReq() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			// (2) 檢核財金電文 Header
			rtnCode = getmFISCBusiness().checkRMHeader(true, MessageFlow.Request);
			fiscReqTxdate = CalendarUtil.rocStringToADString("1" + getmFISCRMReq().getTxDate());

			// (3) 財金中文轉換
			RefString refInName = new RefString(getmFISCRMReq().getInName());
			boolean flag = getmFISCBusiness().convertFiscDecode(getmFISCRMReq().getInName(), refInName);
			getmFISCRMReq().setInName(refInName.get());
			if (!flag) {
				getmFISCRMReq().setInName("ＸＸＸＸＸ");
			}

			RefString refOutName = new RefString(getmFISCRMReq().getOutName());
			flag = getmFISCBusiness().convertFiscDecode(getmFISCRMReq().getOutName(), refOutName);
			getmFISCRMReq().setOutName(refOutName.get());
			if (!flag) {
				getmFISCRMReq().setOutName("ＸＸＸＸＸ");
			}

			// Modify by Jim, 2011/05/19, CHINESE_MEMO非必要欄位，不一定會有值
			if (StringUtils.isNotBlank(getmFISCRMReq().getChineseMemo())) {
				RefString refChineseMemo = new RefString(getmFISCRMReq().getChineseMemo());
				flag = getmFISCBusiness().convertFiscDecode(getmFISCRMReq().getChineseMemo(), refChineseMemo);
				getmFISCRMReq().setChineseMemo(refChineseMemo.get());
				if (!flag) {
					getmFISCRMReq().setChineseMemo("ＸＸＸＸＸ");
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	/**
	 * 3.Prepare交易記錄初始資料, 新增交易記錄(FEPTXN)
	 */
	private FEPReturnCode prepareAndInsertFEPTXN() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			// 2.Prepare() 交易記錄初始資料, 新增交易記錄(FEPTXN)
			rtnCode = getmFISCBusiness().prepareFEPTXNByRM(getmTxFISCData().getMsgCtl(), "0");

			// 新增交易記錄(FEPTXN )
			if (rtnCode == CommonReturnCode.Normal) {
				rtnCode = getmFISCBusiness().insertFEPTxn();
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	/**
	 * 4.CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
	 */
	private FEPReturnCode checkBusinessRule(RefString refRepMac, RefInt refWkRepk, RefString refWkRc, RefString refRepUnitBank, RefBase<Rmout> refDefRmout, RefBase<Boolean> refIfFedi) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		getLogContext().setProgramName(StringUtils.join(ProgramName, "CheckBusinessRule"));

		try {
			rtnCode = getmFISCBusiness().checkBody("0", refRepMac, refWkRepk, refWkRc, refRepUnitBank);
			LogHelperFactory.getTraceLogger().trace(StringUtils.join("AA11X2-CheckBusinessRule After CheckBody, rtnCode = ", rtnCode.getValue()));

			if (rtnCode == CommonReturnCode.Normal) {
				// 被他行退匯匯入
				if ("1172".equals(getmFISCRMReq().getProcessingCode())) {
					rtnCode = this.checkORGData(refDefRmout, refIfFedi);

					if (rtnCode != CommonReturnCode.Normal) {
						// “無法讀取原匯出資料” & FISCData.tx_date & FISCData.ORG_BANK_NO & FISCData.RECEIVER_BANK 至EMS
						getLogContext().setRemark(StringUtils.join("無法讀取原匯出資料, FISCRMReq.TX_DATE=", getmFISCRMReq().getTxDate(), ",FISCRMReq.ORG_BANK_NO=", getmFISCRMReq().getOrgBankNo(),
								",FISCRMReq.RECEIVER_BANK=", getmFISCRMReq().getReceiverBank(), ",FISCRMReq.SENDER_BANK=", getmFISCRMReq().getSenderBank()));

						TxHelper.getRCFromErrorCode(rtnCode, FEPChannel.BRANCH, getLogContext());
						logMessage(Level.DEBUG, getLogContext());
						rtnCode = CommonReturnCode.Normal;
					}

					// 更新全國銀行檔之匯出財金FLAG暫停
					if ("01".equals(getmFISCRMReq().getSTATUS()) || "02".equals(getmFISCRMReq().getSTATUS())) {
						getLogContext().setReturnCode(RMReturnCode.BankReturnRemit);
						getLogContext().setRemark(StringUtils.join(TxHelper.getMessageFromFEPReturnCode(RMReturnCode.BankReturnRemit), ",TXDATE=", fiscReqTxdate, ",RMSNO=",
								getmFISCRMReq().getOrgBankNo(), ",unitBank=", refRepUnitBank.get(), ",解款行=", getmFISCRMReq().getSenderBank(), ",匯款行=", getmFISCRMReq().getReceiverBank()));

						TxHelper.getRCFromErrorCode(RMReturnCode.BankReturnRemit, FEPChannel.BRANCH, getLogContext());
						rtnCode = this.updateALLBANK_RMFORWARD();
					}
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	private FEPReturnCode checkORGData(RefBase<Rmout> refDefRmout, RefBase<Boolean> refIfFedi) {
		try {
			refDefRmout.get().setRmoutTxdate(fiscReqTxdate);
			refDefRmout.get().setRmoutRmsno(getmFISCRMReq().getOrgBankNo());
			refDefRmout.get().setRmoutSenderBank(getmFISCRMReq().getReceiverBank());
			refDefRmout.get().setRmoutReceiverBank(getmFISCRMReq().getSenderBank().substring(0, 3));
			refDefRmout.get().setRmoutTxamt(new BigDecimal(getmFISCRMReq().getTxAmt()));

			refDefRmout.set(rmoutMapper.getRMOUTByCheckORGData(refDefRmout.get()));
			if (refDefRmout.get() == null) {
				return FEPReturnCode.ReturnRemitOriginalDatanotExist;
			}

			if (RMOUT_ORIGINAL.FEDI.equals(refDefRmout.get().getRmoutOriginal())
					|| RMOUT_ORIGINAL.MMAB2B.equals(refDefRmout.get().getRmoutOriginal())
					|| RMOUT_ORIGINAL.PSP.equals(refDefRmout.get().getRmoutOriginal())) {
				refIfFedi.set(true);
			} else {
				refIfFedi.set(false);
			}

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	private FEPReturnCode updateALLBANK_RMFORWARD() {
		Allbank defAllbank = new Allbank();
		try {
			// Candy modify RECEIVER_BANK->SENDER_BANK
			defAllbank.setAllbankBkno(getmFISCRMReq().getSenderBank().substring(0, 3));
			defAllbank.setAllbankBrno("000");
			defAllbank = allBankMapper.selectByPrimaryKey(defAllbank.getAllbankBkno(), defAllbank.getAllbankBrno());
			if (defAllbank != null) {
				defAllbank.setAllbankRmforward("1");
				defAllbank.setAllbankBkno(defAllbank.getAllbankUnitBank());
				defAllbank.setAllbankBrno("000");

				allBankMapper.updateByPrimaryKey(defAllbank);
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return _rtnCode;
	}

	/**
	 * 5.Prepare匯入主檔及匯入暫存檔資料, 新增匯入主檔及匯入暫存檔
	 */
	private FEPReturnCode insertRMINT() {
		try {
			_defRmin = rminMapper.selectByPrimaryKey(_defRmin.getRminTxdate(), _defRmin.getRminBrno(), _defRmin.getRminFepno());
			if (_defRmin == null) {
				getLogContext().setRemark("AA11X2, 查無RMIN資料");
				logMessage(Level.DEBUG, this.logContext);
				return IOReturnCode.RMINNotFound;
			}

			RefBase<Rmint> refDefRmint = new RefBase<Rmint>(_defRmint);
			getmFISCBusiness().copyRMINToRMINT(_defRmin, refDefRmint);
			_defRmint = refDefRmint.get();
			if (rmintMapper.insert(_defRmint) != 1) {
				getLogContext().setRemark("AA11X2, 新增RMINT失敗");
				logMessage(Level.DEBUG, this.logContext);
				return IOReturnCode.InsertFail;
			}

		} catch (Exception ex) {
			getLogContext().setRemark(StringUtils.join("AA11X2, 新增RMINT資料發生例外: ", ex.toString()));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	private FEPReturnCode insertRMIN(Rmout defRmout, RefString refWkFepno) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			RefBase<Rmin> refDefRmin = new RefBase<Rmin>(_defRmin);
			RefBase<Rmint> refDefRmint = new RefBase<Rmint>(_defRmint);
			rtnCode = getmFISCBusiness().prepareRMIN("0", refDefRmin, refDefRmint, spCaller, defRmout.getRmoutFepno());
			_defRmin = refDefRmin.get();
			_defRmint = refDefRmint.get();
			if (rtnCode == CommonReturnCode.Normal) {
				rminMapper.insertSelective(_defRmin);
				refWkFepno.set(_defRmin.getRminFepno());
				// 被他行退匯匯入
				RefBase<Rmout> refDefRmout = new RefBase<Rmout>(defRmout);
				if ("1172".equals(getmFISCRMReq().getProcessingCode())) {
					this.updateORGData(refWkFepno.get(), refDefRmout);
					defRmout = refDefRmout.get();
				}
			}

			if (rtnCode == CommonReturnCode.Normal) {
				transactionManager.commit(txStatus);
			} else {
				transactionManager.rollback(txStatus);
				_defRmin = null;
				_defRmint = null;
			}

		} catch (Exception ex) {
			if (transactionManager != null) {
				transactionManager.rollback(txStatus);
			}
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	private FEPReturnCode updateORGData(String wkFepno, RefBase<Rmout> refDefRmont) {
		Rmout defRMOUTUpdate = new Rmout();
		String pk = "";
		try {
			defRMOUTUpdate.setRmoutTxdate(refDefRmont.get().getRmoutTxdate());
			defRMOUTUpdate.setRmoutBrno(refDefRmont.get().getRmoutBrno());
			defRMOUTUpdate.setRmoutOriginal(refDefRmont.get().getRmoutOriginal());
			defRMOUTUpdate.setRmoutFepno(refDefRmont.get().getRmoutFepno());

			defRMOUTUpdate.setRmoutStat(RMOUTStatus.BackExchange);
			defRMOUTUpdate.setRmoutOrgStat(RMOUTStatus.Passed);
			// defRMOUTUpdate.RMOUT_ORGDATE = FISCReqTxdate
			// Modify by Jim, 2011/10/3, 匯入退匯時原匯出主檔之原匯入日期寫錯了, 應是當天匯入時之日期, 不是原匯出日期了
			defRMOUTUpdate.setRmoutOrgdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
			defRMOUTUpdate.setRmoutOrgregFepno(wkFepno);
			// modify by Candy 2015-02-24 匯出主檔應是記錄1172電文此筆之通匯序號,不是原通匯序號
			// defRMOUTUpdate.RMOUT_ORGRMSNO = FISCRMReq.ORG_BANK_NO
			defRMOUTUpdate.setRmoutOrgrmsno(getmFISCRMReq().getBankNo());
			refDefRmont.get().setRmoutBackReason(getmFISCRMReq().getSTATUS());

			pk = "PK=: TXDATE = " + refDefRmont.get().getRmoutTxdate() + ", BRNO=" + refDefRmont.get().getRmoutBrno() + ", ORIGINAL=" + refDefRmont.get().getRmoutOriginal() + ", FEPNO="
					+ refDefRmont.get().getRmoutFepno();
			if (rmoutMapper.updateByPrimaryKey(defRMOUTUpdate) != 1) {
				getLogContext().setRemark("AA11X2-UpdateORGData, RMOUT UpdateORGData結果 <> 1筆, " + pk);
				logMessage(Level.DEBUG, getLogContext());
				return FEPReturnCode.ReturnRemitOriginalDatanotExist;
			}

		} catch (Exception ex) {
			getLogContext().setRemark("AA11X2-UpdateORGData exception, " + pk + ", exception=" + ex.toString());
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return _rtnCode;
	}

	/**
	 * 6.準備回財金的相關資料並送回覆電文到財金(SendToFISC)
	 */
	private FEPReturnCode prepareAndSendForFISC(String repMac) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// (1) 準備回財金的相關資料
		rtnCode = this.prepareForFISC(repMac);

		// (2) UpdateTxData: 更新交易記錄(FEPTXN )
		if (rtnCode == CommonReturnCode.Normal) {
			this.updateFEPTXN();
		}

		// 回寫RMIN/RMINT
		if (rtnCode == CommonReturnCode.Normal && _defRmin != null) {
			rtnCode = this.updateRMINBeforeSendToFISC();
		}

		// 送回覆電文到財金(SendToFISC)
		// If rtnCode = CommonReturnCode.Normal Then
		// Modify by Jim, 2011/03/03, 不論是否正確都需要回財金，才不會卡住
		RefString refRtnMessage = new RefString(rtnMessage);
		getmFISCBusiness().sendRMResponseToFISC(refRtnMessage);
		rtnMessage = refRtnMessage.get();
		return rtnCode;
	}

	/**
	 * 組送財金Response 電文
	 * 
	 * @param repMac
	 * @return
	 */
	private FEPReturnCode prepareForFISC(String repMac) {
		// (1) 判斷 RC
		getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
		getFeptxn().setFeptxnRepRc(getmFISCBusiness().getFISCRCFromReturnCode(_rtnCode));
		// (2) 產生 RESPONSE 電文訊息
		RefString refRepMac = new RefString(repMac);
		return getmFISCBusiness().prepareResponseForRM(refRepMac);
	}

	/**
	 * 更新FEPTXN
	 * 
	 * @return
	 */
	private FEPReturnCode updateFEPTXN() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		if (NormalRC.FISC_OK.equals(getFeptxn().getFeptxnRepRc())) {
			getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed);
		} else {
			getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse);
		}
		getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
		rtnCode = getmFISCBusiness().updateFepTxnForRM(getFeptxn());
		if (rtnCode != CommonReturnCode.Normal) {
			TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getmTxFISCData().getTxChannel(), getmTxFISCData().getLogContext());
			return rtnCode;
		}
		return rtnCode;
	}

	/**
	 * 回寫RMIN和RMINT,紀錄RMIN_FISC_RTN_CODE,RMINT_STAT
	 * 
	 * @return
	 */
	private FEPReturnCode updateRMINBeforeSendToFISC() {

		Rmin rmin = new Rmin();
		Rmfiscin1 rmfiscin1 = new Rmfiscin1();
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			rmin.setRminTxdate(_defRmin.getRminTxdate());
			rmin.setRminBrno(_defRmin.getRminBrno());
			rmin.setRminFepno(_defRmin.getRminFepno());
			rmin.setRminFiscRtnCode(getFeptxn().getFeptxnRepRc());

			if (!NormalRC.FISC_OK.equals(getFeptxn().getFeptxnRepRc())) {
				rmin.setRminStat(RMINStatus.FEPCheckError);
			}

			rminMapper.updateByPrimaryKeySelective(rmin);

			if (NormalRC.FISC_OK.equals(getFeptxn().getFeptxnRepRc())) {
				rmfiscin1.setRmfiscin1SenderBank(SysStatus.getPropertyValue().getSysstatFbkno());
				rmfiscin1.setRmfiscin1ReceiverBank(SysStatus.getPropertyValue().getSysstatHbkno());
				rmfiscin1.setRmfiscin1No(Integer.valueOf(_defRmin.getRminFiscsno()));

				if (rmfiscin1ExtMapper.updateByPrimaryKeySelective(rmfiscin1) != 1) {
					transactionManager.rollback(txStatus);
					return IOReturnCode.RMFISCIN1UpdateError;
				}
			}
			transactionManager.commit(txStatus);

		} catch (Exception ex) {
			if (transactionManager != null) {
				transactionManager.rollback(txStatus);
			}
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	/**
	 * 9.Prepare匯出主檔及匯出暫存檔資料, 新增匯出主檔及匯出暫存檔
	 */
	private FEPReturnCode insertRMOUTAndRMOUTT(String wkFepno, String wkBackReason) {

		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			RefBase<Rmout> refDefRmout = new RefBase<Rmout>(new Rmout());
			RefBase<Rmoutt> refDefRmoutt = new RefBase<Rmoutt>(new Rmoutt());
			if (CommonReturnCode.Normal == getmFISCBusiness().prepareRMOUT(wkFepno, wkBackReason, refDefRmout, refDefRmoutt, spCaller)) {
				rmoutMapper.insert(refDefRmout.get());
				rmouttMapper.insert(refDefRmoutt.get());
				transactionManager.commit(txStatus);
			} else {
				transactionManager.rollback(txStatus);
			}

			wkOrgFepno = refDefRmout.get().getRmoutFepno();
		} catch (Exception ex) {
			if (transactionManager != null) {
				transactionManager.rollback(txStatus);
			}
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	/**
	 * 10.FEDI轉通匯匯出狀態回饋
	 */
	private FEPReturnCode callSYNCFEDI(Rmout defRmout) {
		RMGeneral rmGeneral = new RMGeneral();
		RMAABase aa = null;
		RMData txData = new RMData();
		@SuppressWarnings("unused")
		List<Msgctl> msgctls = FEPCache.getMsgctlList();
		try {
			rmGeneral.getRequest().setKINBR(defRmout.getRmoutBrno());
			rmGeneral.getRequest().setTRMSEQ("99");
			rmGeneral.getRequest().setBRSNO(defRmout.getRmoutBrsno());
			rmGeneral.getRequest().setENTTLRNO("99");
			rmGeneral.getRequest().setSUPNO1("");
			rmGeneral.getRequest().setSUPNO2("");
			rmGeneral.getRequest().setTBSDY(defRmout.getRmoutTxdate());
			rmGeneral.getRequest().setTIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
			rmGeneral.getRequest().setFEPNO(defRmout.getRmoutFepno());
			rmGeneral.getRequest().setREMDATE(defRmout.getRmoutTxdate());
			rmGeneral.getRequest().setFISCRC(defRmout.getRmoutFiscRtnCode());
			rmGeneral.getRequest().setCHLRC(TxHelper.getRCFromErrorCode(defRmout.getRmoutFiscRtnCode(), FEPChannel.FEP, FEPChannel.BRANCH, getmTxFISCData().getLogContext()));
			rmGeneral.getRequest().setCHLMSG(TxHelper.getMessageFromFEPReturnCode(defRmout.getRmoutFiscRtnCode(), FEPChannel.FEP, getmTxFISCData().getLogContext()));
			rmGeneral.getRequest().setSTATUS(RMOUTStatus.BackExchange);
			rmGeneral.getRequest().setORGORIGINAL(defRmout.getRmoutOriginal());

			txData.setEj(TxHelper.generateEj());
			txData.setTxObject(rmGeneral);
			txData.setTxChannel(getmTxFISCData().getTxChannel());
			txData.setTxSubSystem(SubSystem.RM);
			txData.setTxRequestMessage(serializeToXml(rmGeneral.getRequest()).replace("&lt;", "<").replace("&gt;", ">"));
			txData.setMessageID("SYNCFEDI");
			// txData.setMsgCtl(new Msgctl());

			txData.setLogContext(new LogData());
			txData.getLogContext().setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
			txData.getLogContext().setSubSys(getLogContext().getSubSys());
			txData.getLogContext().setChannel(getLogContext().getChannel());
			txData.getLogContext().setProgramFlowType(ProgramFlow.AAIn);
			txData.getLogContext().setMessageFlowType(MessageFlow.Request);
			txData.getLogContext().setEj(txData.getEj());
			txData.getLogContext().setMessage(txData.getTxRequestMessage());

			aa = new SyncFEDI(txData);
			String resStr = aa.processRequestData();
			getLogContext().setRemark("After SyncFEDI, res=" + resStr);
			logMessage(Level.DEBUG, getLogContext());
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	private FEPReturnCode updateRMINSNO(RminsnoExtMapper rminsnoExtMapper, String repUnitBank) {
		try {
			Rminsno defRminsno = new Rminsno();
			defRminsno.setRminsnoSenderBank(repUnitBank);
			defRminsno.setRminsnoReceiverBank(_defRmin.getRminReceiverBank().substring(0, 3));
			defRminsno.setRminsnoNo(Integer.valueOf(_defRmin.getRminRmsno()));
			if (rminsnoExtMapper.updateByPrimaryKeySelective(defRminsno) != 1) {
				TxHelper.getRCFromErrorCode(String.valueOf(IOReturnCode.RMINSNOUpdateError.getValue()), FEPChannel.FEP, getmTxFISCData().getTxChannel(), getmTxFISCData().getLogContext());
				return IOReturnCode.RMINSNOUpdateError;
			}
			getLogContext().setRemark("After updateRMINSNO, Set RMINSNO_NO=" + defRminsno.getRminsnoNo());
			logMessage(Level.DEBUG, getLogContext());
		} catch (Exception ex) {
			getLogContext().setRemark("AA11X2-");
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	private FEPReturnCode updateRMINForAutoBack(String wkBackReason) {

		try {
			Rmin rmin = new Rmin();
			rmin.setRminTxdate(_defRmin.getRminTxdate());
			rmin.setRminBrno(_defRmin.getRminBrno());
			rmin.setRminFepno(_defRmin.getRminFepno());
			rmin.setRminStat(RMINStatus.AutoBackRemit);
			rmin.setRminBackReason(wkBackReason);
			rmin.setRminOrgdate(_defRmin.getRminTxdate());

			if (rminMapper.updateByPrimaryKeySelective(rmin) < 1) {
				getLogContext().setReturnCode(IOReturnCode.RMINUpdateError);
				getLogContext().setRemark("AA11X2-Function updateRMINForAutoBack Update RMIN no record");
				TxHelper.getRCFromErrorCode(getLogContext().getReturnCode(), FEPChannel.FISC);
				return IOReturnCode.RMINUpdateError;
			}

			LogHelperFactory.getTraceLogger().trace("After updateRMINForAutoBack OK");

		} catch (Exception ex) {
			LogHelperFactory.getTraceLogger().trace("After updateRMINForAutoBack Exception");
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	private FEPReturnCode updateRMINAndRMINTForORGREGNO(String wkFepno) {
		Rmin rmin = new Rmin();
		Rmint rmint = new Rmint();
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			rmin.setRminTxdate(_defRmin.getRminTxdate());
			rmin.setRminBrno(_defRmin.getRminBrno());
			rmin.setRminFepno(_defRmin.getRminFepno());
			rmin.setRminOrgregNo(wkFepno);

			rminMapper.updateByPrimaryKey(rmin, false);
			if (_defRmint != null) {
				rmint.setRmintTxdate(_defRmint.getRmintTxdate());
				rmint.setRmintBrno(_defRmint.getRmintBrno());
				rmint.setRmintFepno(_defRmint.getRmintFepno());
				rmint.setRmintOrgregNo(wkFepno);
				rmintMapper.updateByPrimaryKey(rmint, false);
			}

			LogHelperFactory.getTraceLogger().trace("After updateRminAndRminForOrgRegno OK");
			transactionManager.commit(txStatus);
		} catch (Exception ex) {
			LogHelperFactory.getTraceLogger().trace("After updateRminAndRminForOrgRegno Exception");
			if (transactionManager != null) {
				transactionManager.rollback(txStatus);
			}
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

}
