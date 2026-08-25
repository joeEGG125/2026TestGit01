package com.syscom.fep.service.svr.processor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.ClrdtlExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmoutExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmouttExtMapper;
import com.syscom.fep.mybatis.model.Clrdtl;
import com.syscom.fep.mybatis.model.Rmout;
import com.syscom.fep.mybatis.model.Rmoutt;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.service.svr.SvrProcessor;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@StackTracePointCut(caller = SvrConst.SVR_CLR_CHECK)
public class ServiceCLRCheckProcessor extends SvrProcessor {
	private final String ProgramName = this.getName();
	public boolean StopOKFlag = true;

	/**
	 * .NET版程式的constructor翻寫到此方法中
	 */
	@Override
	protected void initialization() throws Exception {
		try {
			setLogContext(new LogData());
			getLogContext().setRemark("CLRThread Start");
			logMessage(Level.INFO, getLogContext());
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
		}
	}

	/**
	 * .NET版doBusiness()方法翻寫到此方法中
	 */
	@Override
	protected FEPReturnCode doBusiness() throws Exception {
		clrCheckThread();
		return CommonReturnCode.Normal;
	}

	/**
	 * .NET版StopService()方法翻寫到此方法中
	 */
	@Override
	protected void doStop() throws Exception {
		getLogContext().setRemark("CLRThread Stop");
		logMessage(Level.INFO, getLogContext());
	}

	/**
	 * 此方法暫時不用實作
	 */
	@Override
	protected void doPause() throws Exception {}

	public void clrCheckThread() throws InterruptedException {
		getLogContext().setProgramName(ProgramName + "-CLRThread");
		int sleepTime = RMConfig.getInstance().getServiceCLRCheckInterval();
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			FEPCache.reloadCache(CacheItem.SYSSTAT);
			if ("1".equals(SysStatus.getPropertyValue().getSysstatAoct1000()) && "1".equals(SysStatus.getPropertyValue().getSysstatMbact1000())) {
				List<Rmoutt> dt = null;
				Clrdtl defCLR = new Clrdtl();
				defCLR.setClrdtlApId("10000");
				defCLR.setClrdtlTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

				ClrdtlExtMapper dbCLR = SpringBeanFactoryUtil.getBean(ClrdtlExtMapper.class);
				defCLR = dbCLR.queryTopCLRDTL(defCLR.getClrdtlTxdate(), defCLR.getClrdtlApId());
				if (defCLR == null) {
					getLogContext().setRemark("查無CLRDTL資料");
					logMessage(Level.INFO, getLogContext());
					StopOKFlag = true;
					Thread.sleep(sleepTime * 1000);
					StopOKFlag = false;
					return;
				}

				BigDecimal CLRPLUSAmout = BigDecimal.valueOf(RMConfig.getInstance().getCLRPLUSAmout());
				BigDecimal OutwardCheckAmount = BigDecimal.valueOf(RMConfig.getInstance().getOutwardCheckAmount());
				// Fly 2019/10/31 改定在SYSCONF中
				BigDecimal rate = BigDecimal.valueOf(RMConfig.getInstance().getCLRRate());
				BigDecimal FundLevel = BigDecimal.valueOf(defCLR.getClrdtlPreFund().doubleValue() * rate.doubleValue() + CLRPLUSAmout.doubleValue());

				RmouttExtMapper dbRMOUTT = SpringBeanFactoryUtil.getBean(RmouttExtMapper.class);
				dt = dbRMOUTT.getOverCLRFundLevel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), OutwardCheckAmount.toString(), SysStatus.getPropertyValue().getSysstatHbkno());

				for (Rmoutt dr : dt) {
					Rmoutt defRMOUTT = new Rmoutt();
					defRMOUTT.setRmouttTxdate(dr.getRmouttTxdate());
					defRMOUTT.setRmouttBrno(dr.getRmouttBrno());
					defRMOUTT.setRmouttOriginal(dr.getRmouttOriginal());
					defRMOUTT.setRmouttFepno(dr.getRmouttFepno());
					Rmoutt rmoutt = null;
					try {
						rmoutt = dbRMOUTT.queryByPrimaryKeyWithUpdLockWithTimeOut(defRMOUTT.getRmouttTxdate(), defRMOUTT.getRmouttBrno(), defRMOUTT.getRmouttOriginal(), defRMOUTT.getRmouttFepno());
					} catch (Exception ex) {
						SERVICELOGGER.info("ServiceCLRCheckProcessor--clrCheckThread 異常"); // ZK ADD
						continue;
					}
					if (rmoutt == null) {
						SERVICELOGGER.info("rmoutt == null"); // ZK ADD
						continue;
					}
					if (!"04".equals(rmoutt.getRmouttStat()) && !"99".equals(rmoutt.getRmouttStat())) {
						SERVICELOGGER.info("RmouttStat：" +rmoutt.getRmouttStat()); // ZK ADD
						continue;
					}
					String[] stats = {"04","99"};
					if (defCLR.getClrdtlUseBal().doubleValue() - rmoutt.getRmouttTxamt().doubleValue() < FundLevel.doubleValue()) {
						if (!"8".equals(rmoutt.getRmouttOwpriority())) {
							rmoutt.setRmouttOwpriority("8");
							rmoutt.setRmouttUseBal(defCLR.getClrdtlUseBal());

							dbRMOUTT.updateByPrimaryKeySelectiveWithStat(rmoutt, stats);

							Rmout defRMOUT = new Rmout();
							defRMOUT.setRmoutTxdate(rmoutt.getRmouttTxdate());
							defRMOUT.setRmoutBrno(rmoutt.getRmouttBrno());
							defRMOUT.setRmoutOriginal(rmoutt.getRmouttOriginal());
							defRMOUT.setRmoutFepno(rmoutt.getRmouttFepno());
							defRMOUT.setRmoutOwpriority("8");
							defRMOUT.setRmoutUseBal(defCLR.getClrdtlUseBal());
							RmoutExtMapper dbRMOUT = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);
							if (dbRMOUT.updateByPrimaryKeyWithStat(defRMOUT, stats) > 0) {
								getLogContext().setRemark(StringUtils.join("該筆匯出超過水位 RMOUTT_FEPNO[", rmoutt.getRmouttFepno(), "] RMOUTT_BRNO[", rmoutt.getRmouttBrno(), "] RMOUTT_TXAMT[",
										FormatUtil.decimalFormat(rmoutt.getRmouttTxamt(),"#,###.###"), "] CLRDTL_USE_BAL[",FormatUtil.decimalFormat(defCLR.getClrdtlUseBal(),"#,###.###"), "] FundLevel[", FormatUtil.decimalFormat(FundLevel,"#,###.###"), "]"));
								logMessage(Level.INFO, getLogContext());
								FEPReturnCode rc = null;
								getLogContext().setChannel(FEPChannel.FEP);
								getLogContext().setMessageGroup("4");
								getLogContext().setMessageParm13(StringUtils.join("匯款日期:", rmoutt.getRmouttTxdate(), " 來源別:", rmoutt.getRmouttOriginal(), " 登錄分行:", rmoutt.getRmouttBrno(), " FEP登錄序號:",
										rmoutt.getRmouttFepno(), " 匯款金額", FormatUtil.decimalFormat(rmoutt.getRmouttTxamt(),"#,###.###"), " 可用餘額", FormatUtil.decimalFormat(defCLR.getClrdtlUseBal(),"#,###.###"), " 水位", FormatUtil.decimalFormat(FundLevel,"#,###.###")));
								rc = FEPReturnCode.ExceetCLRFundLevel;
								TxHelper.getRCFromErrorCode(rc, FEPChannel.FEP, getLogContext());
							} else {
								getLogContext().setRemark("RMOUT_STAT已被修改，不更新此筆");
								logMessage(Level.INFO, getLogContext());
							}

						}
					} else {
						if ("8".equals(rmoutt.getRmouttOwpriority())) {
							rmoutt.setRmouttOwpriority("0");
							rmoutt.setRmouttUseBal(defCLR.getClrdtlUseBal());

							dbRMOUTT.updateByPrimaryKeySelectiveWithStat(rmoutt, stats);

							Rmout defRMOUT = new Rmout();
							defRMOUT.setRmoutTxdate(rmoutt.getRmouttTxdate());
							defRMOUT.setRmoutBrno(rmoutt.getRmouttBrno());
							defRMOUT.setRmoutOriginal(rmoutt.getRmouttOriginal());
							defRMOUT.setRmoutFepno(rmoutt.getRmouttFepno());
							defRMOUT.setRmoutOwpriority("0");
							defRMOUT.setRmoutUseBal(defCLR.getClrdtlUseBal());

							RmoutExtMapper dbRMOUT = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);
							if (dbRMOUT.updateByPrimaryKeyWithStat(defRMOUT, stats) > 0) {
								getLogContext().setRemark(StringUtils.join("update OWPRIORITY為0 RMOUTT_FEPNO[", rmoutt.getRmouttFepno(), "] RMOUTT_BRNO[", rmoutt.getRmouttBrno(), "] RMOUTT_TXAMT[",
										FormatUtil.decimalFormat(rmoutt.getRmouttTxamt(),"#,###.###"), "] CLRDTL_USE_BAL[", FormatUtil.decimalFormat(defCLR.getClrdtlUseBal(),"#,###.###"), "] FundLevel[", FormatUtil.doubleFormat(Math.round(FundLevel.doubleValue()),"#,###.###"), "]"));
								logMessage(Level.INFO, getLogContext());
							} else {
								getLogContext().setRemark("RMOUT_STAT已被修改，不更新此筆");
								logMessage(Level.INFO, getLogContext());
							}

						}
					}
				}
			}
			transactionManager.commit(txStatus);
			StopOKFlag = true;
			Thread.sleep(sleepTime * 1000);
			StopOKFlag = false;
		} catch (InterruptedException ex) {
			transactionManager.rollback(txStatus);
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			StopOKFlag = true;
			Thread.sleep(sleepTime * 1000);
			StopOKFlag = false;
		}
		StopOKFlag = true;
	}

}