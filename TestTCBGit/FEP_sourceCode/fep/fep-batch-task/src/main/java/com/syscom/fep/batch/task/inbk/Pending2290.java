package com.syscom.fep.batch.task.inbk;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.ext.mapper.FeptxnExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.ext.mapper.ZoneExtMapper;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


/**
 * 存款單位查詢交易處理結果(PCODE-2290)整批處理
 * @author Joseph
 *
 */
public class Pending2290 extends FEPBase implements Task {
	private String batchLogPath = StringUtils.EMPTY;
	private BatchJobLibrary job = null;
    private boolean batchResult = false;
    private SysstatExtMapper sysstatExtMapper = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
    private FeptxnExtMapper feptxnExtMapper = SpringBeanFactoryUtil.getBean(FeptxnExtMapper.class);
	private ZoneExtMapper zoneExtMapper = SpringBeanFactoryUtil.getBean(ZoneExtMapper.class);
	private BsdaysExtMapper bsdaysExtMapper = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
	private String wk_DATE = "";
	private String wk_TXDATE_S = "";
	private String wk_TXDATE_E = "";
	private Zone zone = new Zone();
	private Bsdays bsdays = new Bsdays();

	@Override
	public BatchReturnCode execute(String[] args) {
		try {
			// 1. 初始化相關批次物件及拆解傳入參數
			initialBatch(args);
			
			// 2. 檢核批次參數是否正確, 若正確則啟動批次工作
			job.writeLog("------------------------------------------------------------------");
			job.writeLog(ProgramName + "開始");
			job.startTask();

			// 3. 批次主要處理流程
			// 檢查有值再執行呼叫 AA
			if(checkTbsdyFisc() && check2290Data()) {
				// 3. 批次主要處理流程
				batchResult = mainProcess();
			}
			
			// 4. 通知批次作業管理系統工作正常結束
			if (batchResult) {
				job.writeLog(ProgramName + "正常結束!!");
				job.writeLog("------------------------------------------------------------------");
				job.endTask();
			} else {
				job.writeLog(ProgramName + "不正常結束，停止此批次作業!!");
				job.writeLog("------------------------------------------------------------------");
				job.abortTask();
			}
			return BatchReturnCode.Succeed;
		} catch (Exception e) {
			logContext.setProgramException(e);
			logContext.setProgramName(ProgramName);
			sendEMS(logContext);
			if (job != null) {
				job.writeErrorLog(e, e.getMessage());
				job.writeLog(ProgramName + "失敗!!");
				job.writeLog("------------------------------------------------------------------");
				// 通知批次作業管理系統工作失敗,暫停後面流程
				try {
					job.abortTask();
				} catch (Exception ex) {
					logContext.setProgramException(ex);
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
				}
			}
			return BatchReturnCode.ProgramException;
		} finally {
			if (job != null) {
				job.writeLog(ProgramName + "結束!!");
				job.writeLog("------------------------------------------------------------------");
				job.dispose();
				job = null;
			}
			if (logContext != null) {
				logContext = null;
			}
		}
	}

	private boolean mainProcess() {
		boolean rtnflag = true;
		FISCGeneral aData= new FISCGeneral();
		FEPHandler fepHandler = new FEPHandler();
		job.writeLog("FISCGeneral [AA 2290]");
		aData.setINBKRequest(new FISC_INBK());
		aData.setSubSystem(FISCSubSystem.INBK);
		aData.getINBKRequest().setMessageKind(MessageFlow.Request);
		aData.getINBKRequest().setProcessingCode("2290");
		aData.getINBKRequest().setMessageType("0200");
		try {
			job.writeLog("FEPHandler Dispatch [AA 2290]");
			fepHandler.dispatch(FEPChannel.FISC, aData);
			job.writeLog("Call PendingFromBank送2290完成");
//			// 將AA RC 顯示(參考 UI_012280Controller)
//            if (StringUtils.isBlank(aData.getDescription())) {
//                aData.setDescription("無此錯誤訊息!!請洽資訊人員!");
//            }else {
//            	job.writeLog(aData.getDescription());
//            }
            
		} catch (Exception e) {
			job.writeLog("FISCHandlerDispatch [AA 2290] Fail!!!");
			job.writeLog(e.getMessage());
			return false;
		}
		return rtnflag;
	}

	/**
	 * 初始化相關批次物件及拆解傳入參數初始化相關批次物件及拆解傳入參數
	 *
	 * @param args
	 */
	private void initialBatch(String[] args) {
		// 0. 初始化logContext物件,傳入工作執行參數
		logContext = new LogData();
		logContext.setChannel(FEPChannel.BATCH);
		logContext.setEj(0);
		logContext.setProgramName(ProgramName);
		// 1. 檢查Batch Log目錄參數
		batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
		if (StringUtils.isBlank(batchLogPath)) {
			LogHelperFactory.getGeneralLogger().error("Batch Log目錄未設定，請修正");
			return;
		}
		// 2. 初始化BatchJob物件,傳入工作執行參數
		job = new BatchJobLibrary(this, args, batchLogPath);
	}
	
	private boolean check2290Data() {
		boolean check = false;
		try {
			List <Map <String, Object>>  _defSYSSTAT = sysstatExtMapper.getQueryAll();
			if (_defSYSSTAT.size() < 1) {
				job.writeLog(ProgramName + "-SYSSTAT No Record");
				logContext.setRemark(ProgramName + "-SYSSTAT No Record");
				sendEMS(logContext);
				return false;
			}
			
			String sysstatHbkno = _defSYSSTAT.get(0).get("SYSSTAT_HBKNO").toString();
			String sysstatLbsdy = _defSYSSTAT.get(0).get("SYSSTAT_LBSDY_FISC").toString();
			job.writeLog(ProgramName + " - SYSSTAT_LBSDY_FISC:" + wk_DATE + ";");
			job.writeLog(ProgramName + " - SYSSTAT_HBKNO:" + sysstatHbkno + ";");
			
			List <Map <String, Object>> feptxnCountsList = feptxnExtMapper.getFEPTXN2290Count(wk_TXDATE_S, wk_TXDATE_E, sysstatHbkno, wk_DATE);
			if(feptxnCountsList != null && feptxnCountsList.size() > 0) {
				for(Map <String, Object> map : feptxnCountsList) {
					job.writeLog(ProgramName + " - 發動行:" + map.get("FEPTXN_BKNO") + " 及PENDING筆數:" + map.get("COUNTS"));
				}
				check = true;
			}else {
				batchResult = true;//正常結束程式
				job.writeLog(ProgramName + " - LOG 無 PENDING 資料");
			}
		} catch (Exception e) {
			job.writeLog("FISCHandlerDispatch [AA 2290] Fail!!!");
			job.writeLog(e.getMessage());
		}
		return check;
	}

	private boolean checkTbsdyFisc() {
		/* 2025/9/5 修改 for  2290 判斷財金營業日 */
		boolean check = false;
		String wk_LLBSDY_FISC = "";
		String wk_LLLBSDY_FISC = "";

		try {
			zone = zoneExtMapper.selectByPrimaryKey("TWN");

			/*為了 getFEPTXN2290Count 調整 ，取得 BSDAYS_DATE */
			bsdays = bsdaysExtMapper.getLBsdays(SysStatus.getPropertyValue().getSysstatLbsdyFisc());
			if (bsdays == null) {
				job.writeLog("查無財金前前營業");
				return false;
			}
			wk_LLBSDY_FISC = bsdays.getBsdaysDate();

			/*為了 getFEPTXN2290Count 調整 ，取得 BSDAYS_DATE */
			bsdays = bsdaysExtMapper.getLBsdays(wk_LLBSDY_FISC);
			if (bsdays == null) {
				job.writeLog("查無財金前前前營業");
				return false;
			}
			wk_LLLBSDY_FISC = bsdays.getBsdaysDate();

			/* 判斷是否已換財金營業日 */
			if (Double.parseDouble(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmm"))) >= 1500
					&& Double.parseDouble(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmm"))) <= 1630
					&& DbHelper.toBoolean(zone.getZoneChgday())) { /* 已換日 */
				wk_DATE = wk_LLBSDY_FISC; // 財金前二個營業日
				wk_TXDATE_S = wk_LLLBSDY_FISC;	//財金前三個營業日
				wk_TXDATE_E = wk_DATE; //財金前二個營業日
			} else {
				wk_DATE = SysStatus.getPropertyValue().getSysstatLbsdyFisc(); // 財金前一個營業日
				wk_TXDATE_S = wk_LLBSDY_FISC;	//財金前二個營業日
				wk_TXDATE_E = wk_DATE; //財金前一個營業日
			}
			check = true;
		} catch (Exception e) {
			job.writeLog("checkTbsdyFisc [AA 2290] Fail!!!");
			job.writeLog(e.getMessage());
		}
		return check;
	}
}
