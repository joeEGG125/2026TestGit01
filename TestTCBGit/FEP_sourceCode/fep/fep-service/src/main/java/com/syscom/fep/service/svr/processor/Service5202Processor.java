package com.syscom.fep.service.svr.processor;

import static com.syscom.fep.vo.constant.NormalRC.FISC_OK;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.server.common.handler.FEPHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.ClrdtlExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.mapper.ClrdtltxnMapper;
import com.syscom.fep.mybatis.model.Clrdtl;
import com.syscom.fep.mybatis.model.Clrdtltxn;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.FISCHandler;
import com.syscom.fep.service.svr.SvrProcessor;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_CLR;

@StackTracePointCut(caller = SvrConst.SVR_5202)
public class Service5202Processor extends SvrProcessor {
	private final String ProgramName = this.getName();
	public boolean stopOKFlag = false;
	public int sleepTime = 10000;

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
		clrThread();
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

	private void clrThread() throws InterruptedException {
		getLogContext().setProgramName(StringUtils.join(ProgramName, "-CLRThread"));
		try {
			FEPCache.reloadCache(CacheItem.SYSSTAT);
			if ("1".equals(SysStatus.getPropertyValue().getSysstatAoct1000()) && "1".equals(SysStatus.getPropertyValue().getSysstatMbact1000())) {
				getLogContext().setEj(TxHelper.generateEj());
				FISCGeneral aData = new FISCGeneral();
				FEPHandler fepHandler = new FEPHandler();
				String[] message = null;
				aData.setCLRRequest(new FISC_CLR());
				aData.setSubSystem(FISCSubSystem.CLR);
				aData.getCLRRequest().setMessageKind(MessageFlow.Request);
				aData.getCLRRequest().setProcessingCode("5202");
				aData.getCLRRequest().setMessageType("0500");
				aData.getCLRRequest().setAPID5("10000");
				aData.getCLRRequest().setLogContext(new LogData());
				// Call AA
				getLogContext().setRemark("Call AA5202 START");
				logMessage(Level.INFO, getLogContext());
				fepHandler.dispatch(FEPChannel.FEP, aData);
				getLogContext().setRemark(StringUtils.join("Call AA5202 FINISH |text ", aData.getDescription()));
				logMessage(Level.INFO, getLogContext());
				message = aData.getDescription().split("[-]", -1);
				if (message.length == 2 && message[0].equals(FISC_OK)) {

					ClrdtlExtMapper clrdtlExtMapper = SpringBeanFactoryUtil.getBean(ClrdtlExtMapper.class);
					Clrdtl clrdtl = new Clrdtl();
					clrdtl.setClrdtlTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
					clrdtl.setClrdtlApId("10000");
					List<Clrdtl> clrdtls = clrdtlExtMapper.selectByPrimaryKeyThread(clrdtl.getClrdtlTxdate(), clrdtl.getClrdtlApId());
					if (StringUtils.isBlank(aData.getCLRResponse().getFundBal())) {
						clrdtl.setClrdtlPreFund(BigDecimal.valueOf(0));
					} else {
						clrdtl.setClrdtlPreFund(BigDecimal.valueOf(Double.parseDouble(aData.getCLRResponse().getFundBal()) * 100));
					}
					clrdtl.setClrdtlPaytype(" ");
					clrdtl.setClrdtlTime(aData.getCLRRequest().getTxnInitiateDateAndTime().substring(6, 12));
					clrdtl.setClrdtlStan(aData.getCLRRequest().getSystemTraceAuditNo());

					clrdtl.setClrdtlUseBal(new BigDecimal(StringUtils.isBlank(aData.getCLRResponse().getFundAvail()) ? "0" : aData.getCLRResponse().getFundAvail()));
					clrdtl.setClrdtlTotDbcnt(Integer.parseInt(StringUtils.isBlank(aData.getCLRResponse().getSumCntDr()) ? "0" : aData.getCLRResponse().getSumCntDr()));
					clrdtl.setClrdtlTotDbamt(new BigDecimal(StringUtils.isBlank(aData.getCLRResponse().getSumAmtDr()) ? "0" : aData.getCLRResponse().getSumAmtDr()));
					clrdtl.setClrdtlTotCrcnt(Integer.parseInt(StringUtils.isBlank(aData.getCLRResponse().getSumCntCr()) ? "0" : aData.getCLRResponse().getSumCntCr()));
					clrdtl.setClrdtlTotCramt(new BigDecimal(StringUtils.isBlank(aData.getCLRResponse().getSumAmtCr()) ? "0" : aData.getCLRResponse().getSumAmtCr()));
					clrdtl.setClrdtlFeeDbamt(new BigDecimal(StringUtils.isBlank(aData.getCLRResponse().getFeeAmtDr()) ? "0" : aData.getCLRResponse().getFeeAmtDr()));
					clrdtl.setClrdtlFeeCramt(new BigDecimal(StringUtils.isBlank(aData.getCLRResponse().getFeeAmtCr()) ? "0" : aData.getCLRResponse().getFeeAmtCr()));
					int count = 0;

					if (clrdtls.size() > 0) {
						getLogContext().setRemark("UPDATE CLRDTL");
						logMessage(Level.INFO, getLogContext());
						count = clrdtlExtMapper.updateByPrimaryKeySelectiveThread(clrdtl);
					} else {
						getLogContext().setRemark("INSERT CLRDTL");
						logMessage(Level.INFO, getLogContext());
						count = clrdtlExtMapper.insertSelectiveThread(clrdtl);
					}
					if (count <= 0) {
						getLogContext().setRemark("INSERT/UPDATE CLRDTL 失敗");
						logMessage(Level.INFO, getLogContext());
						sendEMS(getLogContext());
					}

					Clrdtl _defCLRDTL = new Clrdtl();
					_defCLRDTL.setClrdtlTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
					_defCLRDTL.setClrdtlApId("10000");
					_defCLRDTL.setClrdtlPaytype(" ");
					Clrdtl clrdtl1 = clrdtlExtMapper.selectByPrimaryKey(_defCLRDTL.getClrdtlTxdate(), _defCLRDTL.getClrdtlApId(), _defCLRDTL.getClrdtlPaytype());
					if (clrdtl1 != null) {
						_defCLRDTL = clrdtl1;
					}
					Clrdtltxn def = new Clrdtltxn();
					def.setClrdtltxnTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
					def.setClrdtltxnEjfno(getLogContext().getEj());
					def.setClrdtltxnApId("10000");
					def.setClrdtltxnTime(aData.getCLRRequest().getTxnInitiateDateAndTime().substring(6, 12));
					def.setClrdtltxnStan(aData.getCLRRequest().getSystemTraceAuditNo());
					def.setClrdtltxnPreFund(_defCLRDTL.getClrdtlPreFund());
					def.setClrdtltxnUseBal(_defCLRDTL.getClrdtlUseBal());
					def.setClrdtltxnTotDbcnt(_defCLRDTL.getClrdtlTotDbcnt());
					def.setClrdtltxnTotDbamt(_defCLRDTL.getClrdtlTotDbamt());
					def.setClrdtltxnTotCrcnt(_defCLRDTL.getClrdtlTotCrcnt());
					def.setClrdtltxnTotCramt(_defCLRDTL.getClrdtlTotCramt());
					def.setClrdtltxnFeeDbamt(_defCLRDTL.getClrdtlFeeDbamt());
					def.setClrdtltxnFeeCramt(_defCLRDTL.getClrdtlFeeCramt());
					def.setClrdtltxnRmstat(_defCLRDTL.getClrdtlRmstat());
					ClrdtltxnMapper clrdtltxnMapper = SpringBeanFactoryUtil.getBean(ClrdtltxnMapper.class);
					if (clrdtltxnMapper.insertSelective(def) <= 0) {
						getLogContext().setRemark("INSERT CLRDTLTXN 失敗");
						logMessage(Level.INFO, getLogContext());
						sendEMS(getLogContext());
					}

				}
			}
			stopOKFlag = true;
			Sysconf defSys = new Sysconf();
			defSys.setSysconfSubsysno((short) 2);
			defSys.setSysconfName("Service5202_INTERVAL");
			SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
			defSys = sysconfExtMapper.selectByPrimaryKey(defSys.getSysconfSubsysno(), defSys.getSysconfName());
			if (defSys != null) {
				// Fly 2019/11/20 單位改為ms，因此不需*1000
				// sleepTime = Convert.ToInt32(defSys.SYSCONF_VALUE) * 1000
				sleepTime = Integer.parseInt(defSys.getSysconfValue());
			}

			Thread.sleep(sleepTime);
			stopOKFlag = false;
		} catch (InterruptedException ex) {

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			stopOKFlag = true;
			Thread.sleep(sleepTime);
			stopOKFlag = false;
		}
		stopOKFlag = true;
	}

}
