package com.syscom.fep.batch.task.inbk;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.syscom.fep.configuration.INBKConfig;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.FeptxnExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;

/**
 * 
 * 
 * @author Han
 */
public class FISCRecovery extends FEPBase implements Task {
	private String _programName = FISCRecovery.class.getSimpleName(); // 程式名稱

	protected ATMGeneralRequest atmReq;
	private BatchJobLibrary job = null;
	private LogData _logData;
	private String _batchLogPath = StringUtils.EMPTY;
	private String remotePath = StringUtils.EMPTY;
	private String filelocalPath = StringUtils.EMPTY;

	private String txdate_b = StringUtils.EMPTY;	//起始日期
	private String txdate_e = StringUtils.EMPTY;	//截止日期
	private String txtime_b = StringUtils.EMPTY;	//起始時間
	private String txtime_e = StringUtils.EMPTY;	//截止時間

	private FEPReturnCode _batchResult = CommonReturnCode.Abnormal;

	private String _BatchOutputPath = CMNConfig.getInstance().getBatchOutputPath();
	private List<Map<String, Object>> _defSYSSTAT = null;

	private String strFileName_CD;
	private String strFileName_TF;
	private String strFileName_TA;
	private String strFileName_BT;
	private String strFileName_BF;
	private int _cntCD = 0;
	private int _cntTF = 0;
	private int _cntTA = 0;
	private int _cntBT = 0;
	private int _cntBF = 0;
	private Boolean Bcd = false;
	private Boolean Btf = false;
	private Boolean Bta = false;
	private Boolean Bbt = false;
	private Boolean Bbf = false;

	private SysstatExtMapper sysstatExtMapper = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
	private FeptxnExtMapper feptxnExtMapper = SpringBeanFactoryUtil.getBean(FeptxnExtMapper.class);
//	暫時註解 待網頁功能開發
//	private InbkdatavalidExtMapper inbkdatavalidExtMapper =SpringBeanFactoryUtil.getBean(InbkdatavalidExtMapper.class);

	@Override
	public BatchReturnCode execute(String[] args) {
		// 自動生成的方法存根
		try {
			// 0. 檢查Batch Log Path參數

			_batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();

			if (StringUtils.isBlank(_batchLogPath)) {
				job.writeLog("Batch Log目錄未設定，請修正");
				return BatchReturnCode.Succeed;
			}

			//檢查MFT檔案目錄參數
			remotePath = INBKConfig.getInstance().getFiscRecoveryMFTPath().trim();
			if (StringUtils.isBlank(remotePath)) {
				job.writeLog("MFT 檔案目錄未設定，請修正");
				return BatchReturnCode.ProgramException;
			}
			//取得BatchOutputPath
			filelocalPath = CMNConfig.getInstance().getBatchOutputPath().trim();

			// 1. 初始化BatchJob物件,傳入工作執行參數, 檢核Batch所需參數
			job = new BatchJobLibrary(this, args, _batchLogPath);

			// 2. 開始工作內容
			job.writeLog("------------------------------------------------------------------");
			job.writeLog(ProgramName + "開始");
			job.startTask();

			// 檢核失敗,則終止Batch
			if (!CheckConfig()) {
				job.writeLog(ProgramName + " 處理參數設定失敗");
				job.stopBatch();
				return BatchReturnCode.Succeed;
			}

			// 3. 開始執行商業邏輯
			_batchResult = DoBusiness();
			if (_batchResult != FEPReturnCode.Normal) {
				//執行產擋有發生異常
				try {
					job.abortTask();
					job.writeLog(ProgramName + "不正常結束，停止此批次作業!!");
					job.writeLog("------------------------------------------------------------------");
					return BatchReturnCode.ProgramException;
				} catch (Exception ex) {
					logContext.setProgramException(ex);
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
				}
			}

			// 4. 上傳檔案到MFT
			if(_batchResult == FEPReturnCode.Normal){
				if(Bcd || Btf || Bta || Bbt || Bbf){
					_batchResult = upLoadFile();
					if (_batchResult != FEPReturnCode.Normal) {
						//執行上傳檔案有發生異常
						try {
							job.abortTask();
							job.writeLog(ProgramName + "不正常結束，停止此批次作業!!");
							job.writeLog("------------------------------------------------------------------");
							return BatchReturnCode.ProgramException;
						} catch (Exception ex) {
							logContext.setProgramException(ex);
							logContext.setProgramName(ProgramName);
							sendEMS(logContext);
						}
					}
				}else{
					job.writeLog("無檔案需要上傳!!");
				}
			}



			// 5. 通知批次作業管理系統工作正常結束
			job.writeLog("ProgramName: " + ProgramName + "完成!!");
			job.writeLog("------------------------------------------------------------------");
			job.endTask();
			return BatchReturnCode.Succeed;
		} catch (Exception e) {
			logContext.setProgramException(e);
			logContext.setProgramName(ProgramName);
			sendEMS(logContext);
			if (job != null) {
				job.writeErrorLog(e, e.getMessage());
				job.writeLog(ProgramName + "失敗!!");
				job.writeLog("------------------------------------------------------------------");
				// 通知批w作業管理系統工作失敗,暫停後面流程
				try {
					job.abortTask();
					logContext.setProgramException(e);
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
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
			_logData = null;
		}
	}

	private boolean CheckConfig() {
		try {
			job.writeLog(ProgramName + " 開始處理參數設定");
			job.writeLog("------------------------------------------------------------------");
			_logData = new LogData();
			_logData.setChannel(FEPChannel.BATCH);
			_logData.setEj(0);
			_logData.setProgramName(_programName);
			_logData.setSubSys(SubSystem.INBK);

			// 2021/09/11 Modify by Ruling for 財金營運資料補遺
			_logData.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
			
			_defSYSSTAT = sysstatExtMapper.getQueryAll();
			if (_defSYSSTAT.size() < 1) {
				job.writeLog(ProgramName + "-SYSSTAT No Record");
				_logData.setRemark(ProgramName + "-SYSSTAT No Record");
				sendEMS(logContext);
				return false;
			}

			job.writeLog(ProgramName + "-_defSYSSTAT.SYSSTAT_LBSDY_FISC=" + _defSYSSTAT.get(0).get("SYSSTAT_LBSDY_FISC") + ";");
			job.writeLog(ProgramName + "-_defSYSSTAT.SYSSTAT_HBKNO=" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + ";");
			
			//判斷是否有值 無值用預設
			if (job.getArguments().containsKey("TXDATE_B") && StringUtils.isNotBlank(job.getArguments().get("TXDATE_B"))) {
				txdate_b = job.getArguments().get("TXDATE_B");
			}else {
				txdate_b = new SimpleDateFormat("yyyyMMdd").format(new Date());
			}
			if (job.getArguments().containsKey("TXDATE_E") && StringUtils.isNotBlank(job.getArguments().get("TXDATE_E"))) {
				txdate_e=job.getArguments().get("TXDATE_E");
			}else {
				txdate_e = new SimpleDateFormat("yyyyMMdd").format(new Date());
			}
			if (job.getArguments().containsKey("TXTIME_B") && StringUtils.isNotBlank(job.getArguments().get("TXTIME_B"))) {	//未指定時預設為系統時間前35分鐘，必須有值且為6碼
				txtime_b = job.getArguments().get("TXTIME_B");
			} else {
				Calendar beginTime = Calendar.getInstance();
				beginTime.add(Calendar.MINUTE, -35); //35分鐘前	
				Date date = beginTime.getTime();
				txtime_b = new SimpleDateFormat("HHmm").format(date) + "00";
			}
			if (job.getArguments().containsKey("TXTIME_E") && StringUtils.isNotBlank(job.getArguments().get("TXTIME_E"))) {	//未指定時預設為系統時間前5分鐘，必須有值且為6碼
				txtime_e = job.getArguments().get("TXTIME_E");
			} else {
				Calendar endTime = Calendar.getInstance();
				endTime.add(Calendar.MINUTE, -5); 	//5分鐘前
				Date date = endTime.getTime();
				txtime_e = new SimpleDateFormat("HHmm").format(date) + "59";
			}
			job.writeLog(ProgramName + "參數: TXDATE_B={" + txdate_b + "}, TXDATE_E={" + txdate_e + "}, TXTIME_B={" + txtime_b + "}, TXTIME_E={" + txtime_e + "}");
		} catch (Exception e) {
			e.printStackTrace();
			job.writeLog(ProgramName + "-CheckConfig Exception=" + e.getMessage());
			job.writeLog(e.getMessage());
			_logData.setProgramException(e);
			sendEMS(_logData);
			return false;
		} finally {
			if (job != null) {
				job.writeLog("------------------------------------------------------------------");
				job.dispose();
			}
		}
		return true;
	}

	private FEPReturnCode DoBusiness() throws Exception {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		job.writeLog(ProgramName + " 開始處理排程工作");
		job.writeLog("------------------------------------------------------------------");
		try {

			Path p = Paths.get(_BatchOutputPath); // 路徑設定
			job.writeLog(ProgramName + " 檔案放置位址:" + p.toString());
			if (!Files.exists(p)) {
				/* 不存在的話,直接建立資料夾 */
				Files.createDirectory(p);
			}
			
//			//刪除INBK資料驗證檔
//			if(!DeleteINBKDATAVALID()){
//				return IOReturnCode.DeleteFail;
//			}
//			
//			//寫入INBK資料驗證檔
//			if(!InsertINBKDATAVALID()){
//				return IOReturnCode.InsertFail;
//			}
			
			String txdateb = txdate_b + txtime_b;
			String txdatee = txdate_e + txtime_e;
			//3. 	產生跨行提款交易資料補全明細檔(B***CD.TXT)
			List<Map<String, Object>> dtFEPTXN = feptxnExtMapper.GetFEPTXNForRecoveryCd(txdateb,txdatee);
			strFileName_CD = "B" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + "CD.TXT";
			File file = new File(CleanPathUtil.cleanString(_BatchOutputPath + strFileName_CD));
			if (file.exists()) {
				file.delete();
			}
			if(dtFEPTXN != null && dtFEPTXN.size() > 0) {
				file.createNewFile();
				GenerateBXXXCDFile(dtFEPTXN);
				Bcd = true;
				job.writeLog("跨行提款交易資料補全明細檔 " + strFileName_CD + "產擋成功!");
			}else {
				file = null;
				job.writeLog("沒有跨行提款交易資料補全明細檔資料!");
			}
			
			//4. 	產生跨行轉帳交易資料補全明細檔(B***TF.TXT)
			dtFEPTXN = feptxnExtMapper.GetFEPTXNForRecoveryTf(txdateb,txdatee);
			strFileName_TF = "B" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + "TF.TXT";
			file = new File(CleanPathUtil.cleanString(_BatchOutputPath + strFileName_TF));
			if (file.exists()) {
				file.delete();
			}
			if(dtFEPTXN != null && dtFEPTXN.size() > 0) {
				file.createNewFile();
				GenerateBXXXTFFile(dtFEPTXN);
				Btf = true;
				job.writeLog("跨行轉帳交易資料補全明細檔 " + strFileName_TF + "產擋成功!");
			}else {
				file = null;
				job.writeLog("沒有跨行轉帳交易資料補全明細檔資料!");
			}
			
			//5. 	產生繳納稅費交易資料補全明細檔(B***TA.TXT)
			dtFEPTXN = feptxnExtMapper.GetFEPTXNForRecoveryTa(txdateb,txdatee);
			strFileName_TA = "B" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + "TA.TXT";
			file = new File(CleanPathUtil.cleanString(_BatchOutputPath + strFileName_TA));
			if (file.exists()) {
				file.delete();
			}
			if(dtFEPTXN != null && dtFEPTXN.size() > 0) {
				file.createNewFile();
				GenerateBXXXTAFile(dtFEPTXN);
				Bta = true;
				job.writeLog("繳納稅費交易資料補全明細檔 " + strFileName_TA + "產擋成功!");
			}else {
				file = null;
				job.writeLog("沒有繳納稅費交易資料補全明細檔資料!");
			}
			
			//6. 	產生全國性繳稅交易資料補全明細檔(B***BT.TXT)
			dtFEPTXN = feptxnExtMapper.GetFEPTXNForRecoveryBt(txdateb,txdatee);
			strFileName_BT = "B" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + "BT.TXT";
			file = new File(CleanPathUtil.cleanString(_BatchOutputPath + strFileName_BT));
			if (file.exists()) {
				file.delete();
			}
			if(dtFEPTXN != null && dtFEPTXN.size() > 0) {
				file.createNewFile();
				GenerateBXXXBTFile(dtFEPTXN);
				Bbt = true;
				job.writeLog("全國性繳稅交易資料補全明細檔 " + strFileName_BT + "產擋成功!");
			}else {
				file = null;
				job.writeLog("沒有全國性繳稅交易資料補全明細檔資料!");
			}
			
			//7. 	產生全國性繳費交易資料補全明細檔(B***BF.TXT)
			dtFEPTXN = feptxnExtMapper.GetFEPTXNForRecoveryBf(txdateb,txdatee);
			strFileName_BF = "B" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + "BF.TXT";
			file = new File(CleanPathUtil.cleanString(_BatchOutputPath + strFileName_BF));
			if (file.exists()) {
				file.delete();
			}
			if(dtFEPTXN != null && dtFEPTXN.size() > 0) {
				file.createNewFile();
				GenerateBXXXBFFile(dtFEPTXN);
				Bbf = true;
				job.writeLog("全國性繳費交易資料補全明細檔 " + strFileName_BF + "產擋成功!");
			}else {
				file = null;
				job.writeLog("沒有全國性繳費交易資料補全明細檔資料!");
			}


//			//寫入INBK資料驗證檔
//			if(!updateINBKDATAVALID()){
//				return IOReturnCode.UpdateFail;
//			}
		} catch (Exception e) {
			rtnCode = FEPReturnCode.ProgramException;
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
		} finally {
			if (job != null) {
				job.writeLog("------------------------------------------------------------------");
				job.dispose();
			}
		}
		
		return rtnCode;
	}

	//MFT傳檔步驟
	private FEPReturnCode upLoadFile(){
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		if(Bcd){
			if(uploadMftFile(strFileName_CD)){
				job.writeLog(strFileName_CD +"跨行提款交易資料補全明細檔資料 上傳成功!");
			}else{
				job.writeLog(strFileName_CD +"跨行提款交易資料補全明細檔資料 上傳失敗!");
				rtnCode = FEPReturnCode.ProgramException;
			}
		}
		if(Btf){
			if(uploadMftFile(strFileName_TF)){
				job.writeLog(strFileName_TF +"跨行轉帳交易資料補全明細檔資料 上傳成功!");
			}else{
				job.writeLog(strFileName_TF +"跨行轉帳交易資料補全明細檔資料 上傳失敗!");
				rtnCode = FEPReturnCode.ProgramException;
			}
		}
		if(Bta){
			if(uploadMftFile(strFileName_TA)){
				job.writeLog(strFileName_TA +"繳納稅費交易資料補全明細檔資料 上傳成功!");
			}else{
				job.writeLog(strFileName_TA +"繳納稅費交易資料補全明細檔資料 上傳失敗!");
				rtnCode = FEPReturnCode.ProgramException;
			}
		}
		if(Bbt){
			if(uploadMftFile(strFileName_BT)){
				job.writeLog(strFileName_BT +"全國性繳稅交易資料補全明細檔資料 上傳成功!");
			}else{
				job.writeLog(strFileName_BT +"全國性繳稅交易資料補全明細檔資料 上傳失敗!");
				rtnCode = FEPReturnCode.ProgramException;
			}
		}
		if(Bbf){
			if(uploadMftFile(strFileName_BF)){
				job.writeLog(strFileName_BF +"跨行提款交易資料補全明細檔資料 上傳成功!");
			}else{
				job.writeLog(strFileName_BF +"跨行提款交易資料補全明細檔資料 上傳失敗!");
				rtnCode = FEPReturnCode.ProgramException;
			}
		}
		return rtnCode;
	}


	//MFT 下命令執行shellScript取檔
	private boolean uploadMftFile(String fileName) {
		// Shell script
//		String shellScript = "/mft/recvfile.sh";        //開發套是recvfile.sh
			String shellScript = "/mft/transferfile.sh";  //測試套是transferfile.sh
		String remoteFilePath = remotePath + fileName;
		String filePath = filelocalPath + fileName;
		String activation = "put";

		String[] command = {"sudo", "-u", "mftfepap", shellScript, filePath, remoteFilePath, activation};
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.contains("Progress...")) {
					job.writeLog(line);
				}
			}

			// 等待執行結果
			int exitCode = process.waitFor();
			job.writeLog("Exited with code: " + exitCode);

			if(exitCode != 0){
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

//	暫時註解 待網頁功能開發
//	private Boolean DeleteINBKDATAVALID() {
//		String tdate = new SimpleDateFormat("yyyyMMdd").format(new Date());
//
//		List<Inbkdatavalid> inbkdatavalid1 = inbkdatavalidExtMapper.select(tdate,_programName);
//		if(inbkdatavalid1 != null){
//			inbkdatavalidExtMapper.deleteINBKDATAVALID(tdate,_programName);
//			return true;
//		}
//		return true;
//	}

//	暫時註解 待網頁功能開發
//	private Boolean InsertINBKDATAVALID(){
//		try{
//			String tdate = new SimpleDateFormat("yyyyMMdd").format(new Date());
//			Inbkdatavalid inbkdatavalid=new Inbkdatavalid();
//			inbkdatavalid.setInbkdatavalidDate(tdate);
//			inbkdatavalid.setInbkdatavalidProgram(_programName);
//			inbkdatavalid.setInbkdatavalidFilename(strFileName_CD);
//			inbkdatavalid.setInbkdatavalidCompleteflag((long)0);
//			inbkdatavalid.setInbkdatavalidRecord(0);
//			//CD
//			inbkdatavalidExtMapper.insertSelective(inbkdatavalid);
//			//TF
//			inbkdatavalid.setInbkdatavalidFilename(strFileName_TF);
//			inbkdatavalidExtMapper.insertSelective(inbkdatavalid);
//			//TA
//			inbkdatavalid.setInbkdatavalidFilename(strFileName_TA);
//			inbkdatavalidExtMapper.insertSelective(inbkdatavalid);
//			//BT
//			inbkdatavalid.setInbkdatavalidFilename(strFileName_BT);
//			inbkdatavalidExtMapper.insertSelective(inbkdatavalid);
//			//BF
//			inbkdatavalid.setInbkdatavalidFilename(strFileName_BF);
//			inbkdatavalidExtMapper.insertSelective(inbkdatavalid);
//			return true;
//		}catch (Exception e) {
//			e.printStackTrace();
//			job.writeLog("寫入INBK資料驗證檔發生異常錯誤");
//			job.writeLog(e.getMessage());
//			_logData.setProgramException(e);
//			_logData.setRemark("寫入INBK資料驗證檔發生異常錯誤");
//			sendEMS(_logData);
//			if (job != null) {
//				job.writeErrorLog(e, e.getMessage());
//				job.writeLog(ProgramName + "失敗!!");
//				job.writeLog("------------------------------------------------------------------");
//				// 通知批次作業管理系統工作失敗,暫停後面流程
//				try {
//					job.abortTask();
//					job.stopBatch();
//					if (job != null) {
//						job.writeLog(ProgramName + "結束!!");
//						job.writeLog("------------------------------------------------------------------");
//						job.dispose();
//					}
//					if (logContext != null) {
//						logContext = null;
//					}
//				} catch (Exception ex) {
//					logContext.setProgramException(ex);
//					logContext.setProgramName(ProgramName);
//					sendEMS(logContext);
//				}
//			}
//			return false;
//		}
//	}
	
//	暫時註解 待網頁功能開發
//	private Boolean updateINBKDATAVALID(){
//		try{
//			String tdate = new SimpleDateFormat("yyyyMMdd").format(new Date());
//			Inbkdatavalid inbkdatavalid=new Inbkdatavalid();
//			inbkdatavalid.setInbkdatavalidRecord(_cntCD);
//			inbkdatavalid.setInbkdatavalidCompleteflag((long)1);
//			inbkdatavalid.setUpdateTime(new Date());
//			inbkdatavalid.setInbkdatavalidDate(tdate);
//			inbkdatavalid.setInbkdatavalidProgram(_programName);
//			inbkdatavalid.setInbkdatavalidFilename(strFileName_CD);
//			//CD
//			inbkdatavalidExtMapper.updateByPrimaryKeySelective(inbkdatavalid);
//			//TF
//			inbkdatavalid.setInbkdatavalidRecord(_cntTF);
//			inbkdatavalid.setInbkdatavalidFilename(strFileName_TF);
//			inbkdatavalidExtMapper.updateByPrimaryKeySelective(inbkdatavalid);
//			//TA
//			inbkdatavalid.setInbkdatavalidRecord(_cntTA);
//			inbkdatavalid.setInbkdatavalidFilename(strFileName_TA);
//			inbkdatavalidExtMapper.updateByPrimaryKeySelective(inbkdatavalid);
//			//BT
//			inbkdatavalid.setInbkdatavalidRecord(_cntBT);
//			inbkdatavalid.setInbkdatavalidFilename(strFileName_BT);
//			inbkdatavalidExtMapper.updateByPrimaryKeySelective(inbkdatavalid);
//			//BF
//			inbkdatavalid.setInbkdatavalidRecord(_cntBF);
//			inbkdatavalid.setInbkdatavalidFilename(strFileName_BF);
//			inbkdatavalidExtMapper.updateByPrimaryKeySelective(inbkdatavalid);
//			return true;
//		}catch (Exception e) {
//			e.printStackTrace();
//			job.writeLog("更新INBK資料驗證檔發生異常錯誤");
//			job.writeLog(e.getMessage());
//			_logData.setProgramException(e);
//			_logData.setRemark("更新INBK資料驗證檔發生異常錯誤");
//			sendEMS(_logData);
//			if (job != null) {
//				job.writeErrorLog(e, e.getMessage());
//				job.writeLog(ProgramName + "失敗!!");
//				job.writeLog("------------------------------------------------------------------");
//				// 通知批次作業管理系統工作失敗,暫停後面流程
//				try {
//					job.abortTask();
//					job.stopBatch();
//					if (job != null) {
//						job.writeLog(ProgramName + "結束!!");
//						job.writeLog("------------------------------------------------------------------");
//						job.dispose();
//					}
//					if (logContext != null) {
//						logContext = null;
//					}
//				} catch (Exception ex) {
//					logContext.setProgramException(ex);
//					logContext.setProgramName(ProgramName);
//					sendEMS(logContext);
//				}
//			}
//			return false;
//		}
//	}
	
	// 3.
	private void GenerateBXXXCDFile(List<Map<String, Object>> drResult) {
		StringBuilder sLine = null;
		try {
			if (null == drResult || drResult.size() < 1) {
				job.writeLog("沒有跨行提款交易資料補全明細檔資料" + strFileName_CD+"共0筆");
				return;
			} else {
				_cntCD += drResult.size();
				job.writeLog("跨行提款交易資料補全明細檔資料" + strFileName_CD + "共" + _cntCD + "筆");
			}

			OutputStream os = new FileOutputStream(_BatchOutputPath + strFileName_CD);
			Writer sw = null;

			for (int i = 0; i < drResult.size(); i++) {
				sw = new OutputStreamWriter(os);
				sLine = new StringBuilder("");

				if(null == drResult.get(i).get("FEPTXN_BKNO")) {
					drResult.get(i).put("FEPTXN_BKNO", "");
				}
				// 代理單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_BKNO").toString(), 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 4, ' '));

				if(null == drResult.get(i).get("FEPTXN_TROUT_BKNO")) {
					drResult.get(i).put("FEPTXN_TROUT_BKNO", "");
				}
				// 存款單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_BKNO").toString(), 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 4, ' '));

				if(null == drResult.get(i).get("FEPTXN_PCODE")) {
					drResult.get(i).put("FEPTXN_PCODE", "");
				}
				// 交易代號 文數字 4 FEPTXN_PCODE
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 3, ' '));
				
				if(null == drResult.get(i).get("FEPTXN_STAN")) {
					drResult.get(i).put("FEPTXN_STAN", "");
				}
				// 交易序號 文數字 7 FEPTXN_BKNO + FEPTXN_STAN
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_STAN").toString(), 7, '0'));

				
				if(null == drResult.get(i).get("FEPTXN_TX_TIME")) {
					drResult.get(i).put("FEPTXN_TX_TIME", "");
				}
				// 交易時間 文數字 6 FEPTXN_TX_TIME
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_TX_TIME").toString(), 6, '0'));

				if(null == drResult.get(i).get("FEPTXN_TROUT_ACTNO")) {
					drResult.get(i).put("FEPTXN_TROUT_ACTNO", "");
				}
				// 帳號 文數字 16 FEPTXN_TROUT_ACTNO
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_ACTNO").toString(), 16, ' '));

				if(null == drResult.get(i).get("FEPTXN_BKNO")) {
					drResult.get(i).put("FEPTXN_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_ATMNO")) {
					drResult.get(i).put("FEPTXN_ATMNO", "");
				}
				// 自動付款機代號 文數字 8 FEPTXN_ATMNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, '0'));
				} else {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, ' '));
				}

				// 空白
				sLine.append(" ");

				if(null == drResult.get(i).get("FEPTXN_REP_RC")) {
					drResult.get(i).put("FEPTXN_REP_RC", "");
				}
				// 存款單位送財金回應訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 財金送代理單位回應訊息 文數字 4 IF FEPTXN_BKNO ＝SYSSTAT_HBKNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}
				
				// 代理單位送財金確認訊息 文數字 4 IF FEPTXN_BKNO ＝ SYSSTAT_HBKNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 財金送存款單位確認訊息 文數字 4 IF FEPTXN_BKNO <> SYSSTAT_HBKNO
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}
				
				if(null == drResult.get(i).get("FEPTXN_TX_CUR")) {
					drResult.get(i).put("FEPTXN_TX_CUR", "");
				}
				
				if(null == drResult.get(i).get("FEPTXN_TX_AMT")) {
					drResult.get(i).put("FEPTXN_TX_AMT", "");
				}
				
				if(null == drResult.get(i).get("FEPTXN_TX_AMT_ACT")) {
					drResult.get(i).put("FEPTXN_TX_AMT_ACT", "");
				}
				
				// 提款金額 數字 7 FEPTXN_TX_AMT(右靠左補0)
				if("TWD".equals(drResult.get(i).get("FEPTXN_TX_CUR").toString())) {
					sLine.append(
							StringUtils.leftPad(
									(new BigDecimal(Math.abs(Math.floor(Double.parseDouble(
											drResult.get(i).get("FEPTXN_TX_AMT").toString().replace(",", "")))))) + "",
									7, '0'));
				}else {
					sLine.append(
							StringUtils.leftPad(
									(new BigDecimal(Math.abs(Math.floor(Double.parseDouble(
											drResult.get(i).get("FEPTXN_TX_AMT_ACT").toString().replace(",", "")))))) + "",
									7, '0'));
				}
				

				if(null == drResult.get(i).get("FEPTXN_TX_DATE")) {
					drResult.get(i).put("FEPTXN_TX_DATE", "");
				}
				// 交易日期 文數字 6 FEPTXN_TX_DATE
				sLine.append(
						StringUtils.right(
							StringUtils.leftPad(CalendarUtil.adStringToROCString(drResult.get(i).get("FEPTXN_TX_DATE").toString()), 6, '0'),
						6)
				);

				// 代理單位分行代號 文數字 4 空白
				sLine.append(StringUtils.leftPad(" ", 4, ' '));

				// 存款單位分行代號 文數字 4 空白
				sLine.append(StringUtils.leftPad(" ", 4, ' '));
				sLine.append("\r\n");
				sw.write(sLine.toString());
				sw.flush();
				if(i == (drResult.size() - 1)) {
					if (sw != null) {
						sw.close();
						sw = null;
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			job.writeLog("產生跨行提款交易資料補全明細檔發生異常錯誤");
			job.writeLog(e.getMessage());
			_logData.setProgramException(e);
			_logData.setRemark("產生跨行提款交易資料補全明細檔發生異常錯誤");
			sendEMS(_logData);
			if (job != null) {
				job.writeErrorLog(e, e.getMessage());
				job.writeLog(ProgramName + "失敗!!");
				job.writeLog("------------------------------------------------------------------");
				// 通知批次作業管理系統工作失敗,暫停後面流程
				try {
					job.abortTask();
					job.stopBatch();
				} catch (Exception ex) {
					logContext.setProgramException(ex);
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
				}
			}
		}
	}

	// 4.
	private void GenerateBXXXTFFile(List<Map<String, Object>> drResult) {
		StringBuilder sLine = null;

		try {
			if (null == drResult || drResult.size() < 1) {
				job.writeLog("沒有跨行轉帳交易資料補全明細檔資料" + strFileName_TF + "共0筆");
				return;
			} else {
				_cntTF += drResult.size();
				job.writeLog("跨行轉帳交易資料補全明細檔資料" + strFileName_TF + "共" + _cntTF + "筆");
			}

			OutputStream os = new FileOutputStream(_BatchOutputPath + strFileName_TF);
			Writer sw = null;

			for (int i = 0; i < drResult.size(); i++) {
				sw = new OutputStreamWriter(os);
				sLine = new StringBuilder("");

				if(null == drResult.get(i).get("FEPTXN_BKNO")) {
					drResult.get(i).put("FEPTXN_BKNO", "");
				}
				// 1.代理單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_BKNO").toString(), 3, ' '));

				// 2.空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				if(null == drResult.get(i).get("FEPTXN_TROUT_BKNO")) {
					drResult.get(i).put("FEPTXN_TROUT_BKNO", "");
				}
				// 3..轉出單位代號
				if("2521".equals(drResult.get(i).get("FEPTXN_PCODE"))
					&& !drResult.get(i).get("FEPTXN_TROUT_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))){
					sLine.append(StringUtils.rightPad("006", 3, ' '));
				}else {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_BKNO").toString(), 3, ' '));
				}
				// 4.空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				if(null == drResult.get(i).get("FEPTXN_TRIN_BKNO")) {
					drResult.get(i).put("FEPTXN_TRIN_BKNO", "");
				}
				// 5.轉入單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TRIN_BKNO").toString(), 3, ' '));

				// 6.空白
				sLine.append(StringUtils.leftPad(" ", 10, ' '));

				if(null == drResult.get(i).get("FEPTXN_STAN")) {
					drResult.get(i).put("FEPTXN_STAN", "");
				}
				// 7.交易序號 文數字 7 FEPTXN_STAN
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_STAN").toString(), 7, '0'));

				if(null == drResult.get(i).get("FEPTXN_PCODE")) {
					drResult.get(i).put("FEPTXN_PCODE", "");
				}
				// 8.交易代號 文數字 4 FEPTXN_PCODE
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' '));

				if(null == drResult.get(i).get("FEPTXN_TX_DATE")) {
					drResult.get(i).put("FEPTXN_TX_DATE", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TX_TIME")) {
					drResult.get(i).put("FEPTXN_TX_TIME", "");
				}
				// 9.交易日期及時間 文數字 6 FEPTXN_TX_DATE(轉成民國年取6位) + FEPTXN_TX_TIME
				sLine.append(
						StringUtils.right(
								StringUtils.leftPad(CalendarUtil.adStringToROCString(drResult.get(i).get("FEPTXN_TX_DATE").toString()), 6, '0'),
								6)
				);
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_TX_TIME").toString(), 6, '0'));

				if(null == drResult.get(i).get("FEPTXN_ATMNO")) {
					drResult.get(i).put("FEPTXN_ATMNO", "");
				}
				// 10.自動付款機代號 文數字 8 FEPTXN_ATMNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, '0'));
				} else {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, ' '));
				}

				if(null == drResult.get(i).get("FEPTXN_TRIN_ACTNO")) {
					drResult.get(i).put("FEPTXN_TRIN_ACTNO", "");
				}
				// 11.轉入帳號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TRIN_ACTNO").toString(), 16, ' '));

				// 12.轉出帳號 文數字 16 FEPTXN_TROUT_ACTNO
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_ACTNO").toString(), 16, ' '));

				// 13.空白
				sLine.append(StringUtils.rightPad(" ", 23, ' '));

				if(null == drResult.get(i).get("FEPTXN_TROUT_BKNO")) {
					drResult.get(i).put("FEPTXN_TROUT_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_PCODE")) {
					drResult.get(i).put("FEPTXN_PCODE", "");
				}
				if(null == drResult.get(i).get("FEPTXN_REP_RC")) {
					drResult.get(i).put("FEPTXN_REP_RC", "");
				}
				// 14.轉出(轉入)送財金回應訊息
				if (!drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))
						&&
						(drResult.get(i).get("FEPTXN_TROUT_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))
						|| !"4".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(),4,' ').substring(3, 4)))
				) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 15.財金送代理單位回應訊息 文數字 4 IF FEPTXN_BKNO ＝SYSSTAT_HBKNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 因 object 轉字串會出線null跳錯誤 加上
				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}

				// 16.代理單位送財金確認訊息 文數字 4 IF FEPTXN_BKNO ＝ SYSSTAT_HBKNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 因 object 轉字串會出線null跳錯誤 加上
				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}

				// 17.財金送轉出(轉入)確認訊息 文數字 4 IF FEPTXN_BKNO <> SYSSTAT_HBKNO
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
						&& (drResult.get(i).get("FEPTXN_TROUT_BKNO").toString()
								.equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))
								|| !("4".equals(drResult.get(i).get("FEPTXN_PCODE").toString().substring(3, 4))))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 18.轉入單位送財金回應訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
						&& drResult.get(i).get("FEPTXN_TRIN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))
						&& "4".equals(drResult.get(i).get("FEPTXN_PCODE").toString().substring(3, 4))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 因 object 轉字串會出線null跳錯誤 加上
				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}

				// 19.財金送轉入單位確認訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
						&& drResult.get(i).get("FEPTXN_TRIN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))
						&& "4".equals(drResult.get(i).get("FEPTXN_PCODE").toString().substring(3, 4))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 20.空白
				sLine.append(StringUtils.rightPad(" ", 4, ' '));

				if(null == drResult.get(i).get("FEPTXN_TX_AMT")) {
					drResult.get(i).put("FEPTXN_TX_AMT", "");
				}
				// 21.轉帳金額 數字 7 FEPTXN_TX_AMT(右靠左補0)
				sLine.append(
						StringUtils.leftPad(
								(new BigDecimal(Math.abs(Math.floor(Double.parseDouble(
										drResult.get(i).get("FEPTXN_TX_AMT").toString().replace(",", "")))))) + "",
								7, '0'));
				sLine.append("\r\n");
				sw.write(sLine.toString());
				sw.flush();
				if(i == (drResult.size()-1)) {
					if (sw != null) {
						sw.close();
						sw = null;
					}
				}

				sLine = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			job.writeLog("產生跨行轉帳交易資料補全明細檔發生異常錯誤");
			job.writeLog(e.getMessage());
			_logData.setProgramException(e);
			_logData.setRemark("產生跨行轉帳交易資料補全明細檔發生異常錯誤");
			sendEMS(_logData);
			if (job != null) {
				job.writeErrorLog(e, e.getMessage());
				job.writeLog(ProgramName + "失敗!!");
				job.writeLog("------------------------------------------------------------------");
				// 通知批次作業管理系統工作失敗,暫停後面流程
				try {
					job.abortTask();
					job.stopBatch();
				} catch (Exception ex) {
					logContext.setProgramException(ex);
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
				}
			}
		}
	}

	/**
	 * <summary> 5. 產生繳納稅費交易資料補全明細檔 </summary> <param name="dtFEPTXN"></param>
	 * <remarks></remarks>
	 *
	 * @param dtFEPTXN
	 */
	private void GenerateBXXXTAFile(List<Map<String, Object>> drResult) {
		StringBuilder sLine = null;

		try {

			if (null == drResult || drResult.size() < 1) {
				job.writeLog("沒有繳納稅費交易資料補全明細檔資料" + strFileName_TA + "共0筆");
				return;
			} else {
				_cntTA += drResult.size();
				job.writeLog("繳納稅費交易資料補全明細檔資料" + strFileName_TA + "共" + _cntTA + "筆");
			}
			OutputStream os = new FileOutputStream(_BatchOutputPath + strFileName_TA);
			Writer sw = null;

			for (int i = 0; i < drResult.size(); i++) {
				sw = new OutputStreamWriter(os);
				sLine = new StringBuilder("");

				if(null == drResult.get(i).get("FEPTXN_BKNO")) {
					drResult.get(i).put("FEPTXN_BKNO", "");
				}
				// 代理單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_BKNO").toString(), 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				if(null == drResult.get(i).get("FEPTXN_TROUT_BKNO")) {
					drResult.get(i).put("FEPTXN_TROUT_BKNO", "");
				}
				// 轉出單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_BKNO").toString(), 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				// 轉入單位代號
				sLine.append(StringUtils.leftPad("000", 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 10, ' '));

				if(null == drResult.get(i).get("FEPTXN_STAN")) {
					drResult.get(i).put("FEPTXN_STAN", "");
				}
				// 跨行交易序號
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_STAN").toString(), 7, '0'));

				if(null == drResult.get(i).get("FEPTXN_PCODE")) {
					drResult.get(i).put("FEPTXN_PCODE", "");
				}
				// 交易代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' '));

				if(null == drResult.get(i).get("FEPTXN_TBSDY_FISC")) {
					drResult.get(i).put("FEPTXN_TBSDY_FISC", "");
				}
				// 交易營業日期
				sLine.append(
						StringUtils.right(
								StringUtils.leftPad(CalendarUtil.adStringToROCString(drResult.get(i).get("FEPTXN_TBSDY_FISC").toString()), 6, '0'),
								6)
				);


				if(null == drResult.get(i).get("FEPTXN_TX_DATE")) {
					drResult.get(i).put("FEPTXN_TX_DATE", "");
				}
				// 交易日期
				sLine.append(
						StringUtils.right(
								StringUtils.leftPad(CalendarUtil.adStringToROCString(drResult.get(i).get("FEPTXN_TX_DATE").toString()), 6, '0'),
								6)
				);

				if(null == drResult.get(i).get("FEPTXN_TX_TIME")) {
					drResult.get(i).put("FEPTXN_TX_TIME", "");
				}
				// 交易時間
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_TX_TIME").toString(), 6, '0'));

				if(null == drResult.get(i).get("FEPTXN_ATMNO")) {
					drResult.get(i).put("FEPTXN_ATMNO", "");
				}
				// 自動付款機代號 文數字 8 FEPTXN_ATMNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, '0'));
				} else {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, ' '));
				}

				// 轉入帳號
				sLine.append(StringUtils.rightPad(" ", 16, ' '));

				if(null == drResult.get(i).get("FEPTXN_TROUT_ACTNO")) {
					drResult.get(i).put("FEPTXN_TROUT_ACTNO", "");
				}
				// 轉出帳號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_ACTNO").toString(), 16, ' '));

				if(null == drResult.get(i).get("FEPTXN_REP_RC")) {
					drResult.get(i).put("FEPTXN_REP_RC", "");
				}
				// 轉出送財金回應訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				if(null == drResult.get(i).get("FEPTXN_REP_RC")) {
					drResult.get(i).put("FEPTXN_REP_RC", "");
				}
				// 財金送代理單位回應訊息
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 因 object 轉字串會出線null跳錯誤 加上
				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}

				// 代理單位送財金確認訊息
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 因 object 轉字串會出線null跳錯誤 加上
				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}

				if(null == drResult.get(i).get("FEPTXN_BKNO")) {
					drResult.get(i).put("FEPTXN_BKNO", "");
				}
				// 財金送轉出確認訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 空白
				sLine.append(StringUtils.leftPad(" ", 4, ' '));

				if(null == drResult.get(i).get("FEPTXN_TX_AMT")) {
					drResult.get(i).put("FEPTXN_TX_AMT", "");
				}
				// 轉帳金額
				sLine.append(StringUtils.leftPad((new BigDecimal(Math.abs(Math.floor(
						(Double.parseDouble(drResult.get(i).get("FEPTXN_TX_AMT").toString().replace(",", "")) * 100)))))
						+ "", 13, '0'));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 15, ' '));

				if(null == drResult.get(i).get("FEPTXN_PAYTYPE")) {
					drResult.get(i).put("FEPTXN_PAYTYPE", "");
				}
				// 繳款類別
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString(), 5, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 6, ' '));

				if(null == drResult.get(i).get("FEPTXN_PAYTYPE")) {
					drResult.get(i).put("FEPTXN_PAYTYPE", "");
				}
				if(null == drResult.get(i).get("FEPTXN_RECON_SEQNO")) {
					drResult.get(i).put("FEPTXN_RECON_SEQNO", "");
				}
				// 銷帳編號
				if (!"15".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString().substring(0, 2),
						2, ' '))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_RECON_SEQNO").toString(), 16, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 16, ' '));
				}

				if(null == drResult.get(i).get("FEPTXN_PAYTYPE")) {
					drResult.get(i).put("FEPTXN_PAYTYPE", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TAX_UNIT")) {
					drResult.get(i).put("FEPTXN_TAX_UNIT", "");
				}
				// 機關代號
				if ("15".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString().substring(0, 2),
						2, ' '))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TAX_UNIT").toString(), 3, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 3, ' '));
				}

				if(null == drResult.get(i).get("FEPTXN_IDNO")) {
					drResult.get(i).put("FEPTXN_IDNO", "");
				}
				// 身分證字號
				if ("15".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString().substring(0, 2),
						2, ' '))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_IDNO").toString(), 11, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 11, ' '));
				}
				sLine.append("\r\n");
				sw.write(sLine.toString());
				sw.flush();
				if(i == (drResult.size()-1)) {
					if (sw != null) {
						sw.close();
						sw = null;
					}
				}

				sLine = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			job.writeLog("產生跨行提款交易資料補全明細檔發生異常錯誤");
			job.writeLog(e.getMessage());
			_logData.setProgramException(e);
			_logData.setRemark("產生跨行提款交易資料補全明細檔發生異常錯誤");
			sendEMS(_logData);
			if (job != null) {
				job.writeErrorLog(e, e.getMessage());
				job.writeLog(ProgramName + "失敗!!");
				job.writeLog("------------------------------------------------------------------");
				// 通知批次作業管理系統工作失敗,暫停後面流程
				try {
					job.abortTask();
					job.stopBatch();
				} catch (Exception ex) {
					logContext.setProgramException(ex);
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
				}
			}

		} finally {
			
		}
	}

	/**
	 * 6. <summary> 產生全國性繳稅交易資料補全 </summary> <param name="dtFEPTXN"></
	 * <remarks></remarks>
	 * 
	 * @param dtFEPTXN
	 */
	private void GenerateBXXXBTFile(List<Map<String, Object>> drResult) {
		StringBuilder sLine = null;

		try {
			if (null == drResult || drResult.size() < 1) {
				job.writeLog("沒有全國性繳稅交易資料補全明細檔資料" + strFileName_BT+"共0筆");
				return;
			} else {
				_cntBT += drResult.size();
				job.writeLog("全國性繳稅交易資料補全明細檔資料" + strFileName_BT + "共" + _cntBT + "筆");
			}
			OutputStream os = new FileOutputStream(_BatchOutputPath + strFileName_BT);
			Writer sw = null;

			for (int i = 0; i < drResult.size(); i++) {
				sw = new OutputStreamWriter(os);
				sLine = new StringBuilder("");

				if(null == drResult.get(i).get("FEPTXN_BKNO")) {
					drResult.get(i).put("FEPTXN_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TROUT_BKNO")) {
					drResult.get(i).put("FEPTXN_TROUT_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_STAN")) {
					drResult.get(i).put("FEPTXN_STAN", "");
				}
				if(null == drResult.get(i).get("FEPTXN_PCODE")) {
					drResult.get(i).put("FEPTXN_PCODE", "");
				}
				
				if(null == drResult.get(i).get("FEPTXN_TBSDY_FISC")) {
					drResult.get(i).put("FEPTXN_TBSDY_FISC", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TX_DATE")) {
					drResult.get(i).put("FEPTXN_TX_DATE", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TX_TIME")) {
					drResult.get(i).put("FEPTXN_TX_TIME", "");
				}
				if(null == drResult.get(i).get("FEPTXN_ATMNO")) {
					drResult.get(i).put("FEPTXN_ATMNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TROUT_ACTNO")) {
					drResult.get(i).put("FEPTXN_TROUT_ACTNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_REP_RC")) {
					drResult.get(i).put("FEPTXN_REP_RC", "");
				}
				if(null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TX_AMT")) {
					drResult.get(i).put("FEPTXN_TX_AMT", "");
				}
				if(null == drResult.get(i).get("FEPTXN_RECON_SEQNO")) {
					drResult.get(i).put("FEPTXN_RECON_SEQNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TAX_UNIT")) {
					drResult.get(i).put("FEPTXN_TAX_UNIT", "");
				}
				if(null == drResult.get(i).get("FEPTXN_IDNO")) {
					drResult.get(i).put("FEPTXN_IDNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_ATM_TYPE")) {
					drResult.get(i).put("FEPTXN_ATM_TYPE", "");
				}
				
				// 代理單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_BKNO").toString(), 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				// 轉出單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_BKNO").toString(), 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				// 5.轉入單位代號
				sLine.append("000");

				// 6.空白
				sLine.append(StringUtils.leftPad(" ", 10, ' '));

				// 7.跨行交易序號
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_STAN").toString(), 7, '0'));

				// '8.交易代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' '));

				// '9.交易營業日期 todo
				sLine.append(
						StringUtils.right(
								StringUtils.leftPad(CalendarUtil.adStringToROCString(drResult.get(i).get("FEPTXN_TBSDY_FISC").toString()), 6, '0'),
								6)
				);

				// '10.交易日期 todo
				sLine.append(
						StringUtils.right(
								StringUtils.leftPad(CalendarUtil.adStringToROCString(drResult.get(i).get("FEPTXN_TX_DATE").toString()), 6, '0'),
								6)
				);

				// '11 交易時間
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_TX_TIME").toString(), 6, '0'));

				// 12 自動付款機代號 文數字 8 FEPTXN_ATMNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, '0'));
				} else {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, ' '));
				}

				// 13 轉入帳號
				sLine.append(StringUtils.rightPad(" ", 16, ' '));

				// 14 轉出帳號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_ACTNO").toString(), 16, ' '));

				// 15 轉出送財金回應訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 16 財金送代理單位回應訊息
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 因 object 轉字串會出線null跳錯誤 加上
				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}

				// 17 代理單位送財金確認訊息
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 18 財金送轉出確認訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 19 空白
				sLine.append(StringUtils.leftPad(" ", 4, ' '));

				// 20 轉帳金額
				sLine.append(StringUtils.leftPad((new BigDecimal(Math.abs(Math.floor(
						Double.parseDouble(drResult.get(i).get("FEPTXN_TX_AMT").toString().replace(",", "")) * 100))))
						+ "", 13, '0'));
				
				// 21 空白
				sLine.append(StringUtils.leftPad(" ", 15, ' '));

				// 22 繳款類別
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString(), 5, ' '));

				// 23 空白
				sLine.append(StringUtils.leftPad(" ", 6, ' '));

				// 24 銷帳編號
				if (!"15".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString(), 2, ' ')
						.substring(0, 2))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_RECON_SEQNO").toString(), 16, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 16, ' '));
				}
				
				// 25 機關代號
				if ("15".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString(), 2, ' ')
						.substring(0, 2))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TAX_UNIT").toString(), 3, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 3, ' '));
				}

				// 26 身分證字號
				if ("15".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString(), 2, ' ')
						.substring(0, 2))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_IDNO").toString(), 11, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 11, ' '));
				}

				// 27 空白
				sLine.append(StringUtils.leftPad(" ", 27, ' '));

				// 28 終端機設備型態
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATM_TYPE").toString(), 4, ' '));
				sLine.append("\r\n");
				sw.write(sLine.toString());
				sw.flush();
				if(i == (drResult.size()-1)) {
					if (sw != null) {
						sw.close();
						sw = null;
					}
				}

				sLine = null;

			}

		} catch (Exception e) {
			e.printStackTrace();
			job.writeLog("產生全國性繳稅交易資料補全明細檔發生異常錯誤");
			job.writeLog(e.getMessage());
			_logData.setProgramException(e);
			_logData.setRemark("產生全國性繳稅交易資料補全明細檔發生異常錯誤");
			sendEMS(_logData);
			if (job != null) {
				job.writeErrorLog(e, e.getMessage());
				job.writeLog(ProgramName + "失敗!!");
				job.writeLog("------------------------------------------------------------------");
				// 通知批次作業管理系統工作失敗,暫停後面流程
				try {
					job.abortTask();
					job.stopBatch();
				} catch (Exception ex) {
					logContext.setProgramException(ex);
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
				}
			}

		}
	}

	/**
	 * <summary> 7. 產生全國性繳費交易資料補全明細檔 </summary> <param name="dtFEPTXN"></param>
	 * <remarks></remarks>
	 *
	 * @param dtFEPTXN
	 */
	private void GenerateBXXXBFFile(List<Map<String, Object>> drResult) {
		StringBuilder sLine = null;

		try {
			if (null == drResult || drResult.size() < 1) {
				job.writeLog("沒有全國性繳費交易資料補全明細檔資料" + strFileName_BF + "共0筆");
				return;
			} else {
				_cntBF += drResult.size();
				job.writeLog("全國性繳費交易資料補全明細檔資料" + strFileName_BF + "共" + _cntBF + "筆");
			}
			OutputStream os = new FileOutputStream(_BatchOutputPath + strFileName_BF);
			Writer sw = null;

			for (int i = 0; i < drResult.size(); i++) {
				sw = new OutputStreamWriter(os);
				sLine = new StringBuilder("");

				
				if(null == drResult.get(i).get("FEPTXN_BKNO")) {
					drResult.get(i).put("FEPTXN_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TROUT_BKNO")) {
					drResult.get(i).put("FEPTXN_TROUT_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TRIN_BKNO")) {
					drResult.get(i).put("FEPTXN_TRIN_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_STAN")) {
					drResult.get(i).put("FEPTXN_STAN", "");
				}
				if(null == drResult.get(i).get("FEPTXN_PCODE")) {
					drResult.get(i).put("FEPTXN_PCODE", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TBSDY_FISC")) {
					drResult.get(i).put("FEPTXN_TBSDY_FISC", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TX_DATE")) {
					drResult.get(i).put("FEPTXN_TX_DATE", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TX_TIME")) {
					drResult.get(i).put("FEPTXN_TX_TIME", "");
				}
				if(null == drResult.get(i).get("FEPTXN_ATMNO")) {
					drResult.get(i).put("FEPTXN_ATMNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TRIN_ACTNO")) {
					drResult.get(i).put("FEPTXN_TRIN_ACTNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TROUT_BKNO")) {
					drResult.get(i).put("FEPTXN_TROUT_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_REP_RC")) {
					drResult.get(i).put("FEPTXN_REP_RC", "");
				}
				if(null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TX_AMT")) {
					drResult.get(i).put("FEPTXN_TX_AMT", "");
				}
				if(null == drResult.get(i).get("FEPTXN_PAYTYPE")) {
					drResult.get(i).put("FEPTXN_PAYTYPE", "");
				}
				if(null == drResult.get(i).get("FEPTXN_BUSINESS_UNIT")) {
					drResult.get(i).put("FEPTXN_BUSINESS_UNIT", "");
				}
				if(null == drResult.get(i).get("FEPTXN_PAYNO")) {
					drResult.get(i).put("FEPTXN_PAYNO", "");
				}
				
				// 1.代理單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_BKNO").toString(), 3, ' '));

				// 2.空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				// 3.轉出單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_BKNO").toString(), 3, ' '));

				// 4.空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				// 5.轉入單位代號
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_TRIN_BKNO").toString(), 3, ' '));

				// 6 空白
				sLine.append(StringUtils.leftPad(" ", 10, ' '));

				// 7 跨行交易序號
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_STAN").toString(), 7, '0'));

				// 8 交易代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' '));

				// 9 交易營業日期
				sLine.append(
						StringUtils.right(
								StringUtils.leftPad(CalendarUtil.adStringToROCString(drResult.get(i).get("FEPTXN_TBSDY_FISC").toString()), 6, '0'),
								6)
				);

				// 10 交易日期
				sLine.append(
						StringUtils.right(
								StringUtils.leftPad(CalendarUtil.adStringToROCString(drResult.get(i).get("FEPTXN_TX_DATE").toString()), 6, '0'),
								6)
				);

				// 11 交易時間
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_TX_TIME").toString(), 6, '0'));

				// '12 自動付款機代號 文數字 8 FEPTXN_ATMNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, '0'));
				} else {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, ' '));
				}
				
				// 13 轉入帳號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TRIN_ACTNO").toString(), 16, ' '));

				// 14 轉出帳號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_ACTNO").toString(), 16, ' '));

				// 15 轉出(轉入)送財金回應訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
						&& ((drResult.get(i).get("FEPTXN_TROUT_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
							|| (!"4".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' ').substring(3, 4)))
							)) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 16 財金送代理單位回應訊息
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 因 object 轉字串會出線null跳錯誤 加上
				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}

				// 17 代理單位送財金確認訊息
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}
				
				// 18 財金送轉出(轉入)確認訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
						&& (drResult.get(i).get("FEPTXN_TROUT_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))
							|| (!"4".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' ').substring(3, 4)))
							)
				) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 19 空白
				sLine.append(StringUtils.leftPad(" ", 4, ' '));

				// 20 轉帳金額
				sLine.append(StringUtils.leftPad((new BigDecimal(Math.abs(Math.floor(
						Double.parseDouble(drResult.get(i).get("FEPTXN_TX_AMT").toString().replace(",", "")) * 100))))
						+ "", 13, '0'));
				
				// 21 空白
				sLine.append(" ");

				// 22 轉入單位送財金回應訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
					&& (drResult.get(i).get("FEPTXN_TRIN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
					&& ("4".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' ').substring(3, 4)))
				) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 23 財金送轉入單位確認訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
						&& (drResult.get(i).get("FEPTXN_TRIN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
						&& ("4".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' ').substring(3, 4)))
				) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 24 空白
				sLine.append(StringUtils.leftPad(" ", 6, ' '));

				// 25 繳款類別
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString(), 5, ' '));

				// 26 委託單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_BUSINESS_UNIT").toString(), 8, ' '));

				// 27 費用代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYNO").toString(), 4, ' '));
				sLine.append("\r\n");
				sw.write(sLine.toString());
				sw.flush();
				if(i == (drResult.size()-1)) {
					if (sw != null) {
						sw.close();
						sw = null;
					}
				}

				sLine = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			job.writeLog("產生全國性繳費交易資料補全明細檔發生異常錯誤");
			job.writeLog(e.getMessage());
			_logData.setProgramException(e);
			_logData.setRemark("產生全國性繳費交易資料補全明細檔發生異常錯誤");
			sendEMS(_logData);
			if (job != null) {
				job.writeErrorLog(e, e.getMessage());
				job.writeLog(ProgramName + "失敗!!");
				job.writeLog("------------------------------------------------------------------");
				// 通知批次作業管理系統工作失敗,暫停後面流程
				try {
					job.abortTask();
					job.stopBatch();
				} catch (Exception ex) {
					logContext.setProgramException(ex);
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
				}
			}

		}


	}
}
