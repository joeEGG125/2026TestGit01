package com.syscom.fep.server.aa.rm;

import static com.syscom.fep.vo.constant.FEPTxnMessageFlow.BRS_Response;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.net.ftp.FtpAdapter;
import com.syscom.fep.frmcommon.net.ftp.FtpAdapterFactory;
import com.syscom.fep.frmcommon.net.ftp.FtpProperties;
import com.syscom.fep.frmcommon.net.ftp.FtpProtocol;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.AllbankExtMapper;
import com.syscom.fep.mybatis.ext.mapper.BatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmbtchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmbtchmtrExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmoutExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmouttExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmpostExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmsfpExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmtotExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmtotalExtMapper;
import com.syscom.fep.mybatis.ext.mapper.TaskExtMapper;
import com.syscom.fep.mybatis.ext.mapper.UserDefineExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.model.Allbank;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Rmbtch;
import com.syscom.fep.mybatis.model.Rmbtchmtr;
import com.syscom.fep.mybatis.model.Rmnoctl;
import com.syscom.fep.mybatis.model.Rmout;
import com.syscom.fep.mybatis.model.Rmoutt;
import com.syscom.fep.mybatis.model.Rmpost;
import com.syscom.fep.mybatis.model.Rmsfp;
import com.syscom.fep.mybatis.model.Rmtot;
import com.syscom.fep.mybatis.model.Rmtotal;
import com.syscom.fep.server.aa.AABaseFactory;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.rm.RM;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.REMTXTP;
import com.syscom.fep.vo.constant.RMCategory;
import com.syscom.fep.vo.constant.RMOUTStatus;
import com.syscom.fep.vo.constant.RMPending;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.vo.text.FEPResponse;
import com.syscom.fep.vo.text.fcs.FCSDetail;
import com.syscom.fep.vo.text.fcs.FCSFoot;
import com.syscom.fep.vo.text.fcs.FCSHead;
import com.syscom.fep.vo.text.rm.RMGeneral;

public class R1001_BTOutBatch implements Task {
	private static final String _programName = "BTOutBatch";
	private BatchJobLibrary job;
	private String _batchLogPath = "E:/sftp/Log/";
	private LogData _logData;

	private String _FCSFileName;
	private String _FCSInFilePath = RMConfig.getInstance().getFCSInPath();
	private String _FCSOutFilePath = RMConfig.getInstance().getFCSOutPath();
	@SuppressWarnings("unused")
	private String _FCSDealFilePath = RMConfig.getInstance().getFCSDealFilePath();

	private List<FCSDetail> _FCSDataS;
	private FCSDetail objFCSData;
	private FCSHead objFCSHead;
	private FCSFoot objFCSFoot;

	private FeptxnExt fepTxn;
	private RM TxRMBusiness;
	private LogData logContext = new LogData();

	private String wkFEPNO;
	private FEPReturnCode wkRtnCode = CommonReturnCode.Normal;

	// Private db As DBHelper
	private FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
	private RmoutExtMapper rmoutExtMapper = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);
	private RmouttExtMapper rmouttExtMapper = SpringBeanFactoryUtil.getBean(RmouttExtMapper.class);
	private RmbtchmtrExtMapper rmbtchmtrExtMapper = SpringBeanFactoryUtil.getBean(RmbtchmtrExtMapper.class);
	private RmtotExtMapper rmtotExtMapper = SpringBeanFactoryUtil.getBean(RmtotExtMapper.class);
	private RmtotalExtMapper rmtotalExtMapper = SpringBeanFactoryUtil.getBean(RmtotalExtMapper.class);
	private RmbtchExtMapper rmbtchExtMapper = SpringBeanFactoryUtil.getBean(RmbtchExtMapper.class);
	@SuppressWarnings("unused")
	private TaskExtMapper taskExtMapper = SpringBeanFactoryUtil.getBean(TaskExtMapper.class);
	@SuppressWarnings("unused")
	private BatchExtMapper batchExtMapper = SpringBeanFactoryUtil.getBean(BatchExtMapper.class);
	private AllbankExtMapper allbankExtMapper = SpringBeanFactoryUtil.getBean(AllbankExtMapper.class);
	private BsdaysExtMapper bsdaysExtMapper = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
	private RmpostExtMapper rmpostExtMapper = SpringBeanFactoryUtil.getBean(RmpostExtMapper.class);
	private RmsfpExtMapper rmsfpExtMapper = SpringBeanFactoryUtil.getBean(RmsfpExtMapper.class);
	private UserDefineExtMapper userDefineExtMapper = SpringBeanFactoryUtil.getBean(UserDefineExtMapper.class);
	private FtpAdapterFactory factory = SpringBeanFactoryUtil.getBean(FtpAdapterFactory.class);

	private BigDecimal WK_SAMT = new BigDecimal(0);
	private int wkTotalCnt = 0;
	private int wkTotalCnt2 = 0;

	@SuppressWarnings("unused")
	private static final String UploadFCSFileTask = "UploadFCSRMFile";
	@SuppressWarnings("unused")
	private static final String SyncOutBatchTask = "RM_SyncOutBatch";
	@SuppressWarnings("unused")
	private static final String SyncOutBatch = "RM_SyncOutBatch";

	@Override
	public BatchReturnCode execute(String[] args) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			_batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
			if (StringUtils.isBlank(_batchLogPath)) {
				LogHelperFactory.getTraceLogger().warn("Batch Log目錄未設定，請修正");
				return BatchReturnCode.Succeed;
			}
			// 1. 初始化BatchJob物件,傳入工作執行參數, 檢核Batch所需參數
			job = new BatchJobLibrary(this, args, _batchLogPath);
			// 2. 開始工作內容
			// job.StartTask()
			job.writeLog(_programName + "開始!");
			if (!checkConfig()) {
				job.abortTask();
				return BatchReturnCode.Succeed;
			}
			// 主流程
			rtnCode = doBusiness();
			job.writeLog("doBusiness執行結果, rtn=" + rtnCode.toString());
			// 4. 通知批次作業管理系統工作正常結束
			job.writeLog(_programName + "結束!!");
			job.writeLog("------------------------------------------------------------------");
			return BatchReturnCode.Succeed;
			// job.EndTask()
		} catch (Exception e) {
			if (job == null) {
				// Send to System Event
				LogHelperFactory.getTraceLogger().error(e, e.getMessage());
			} else {
				job.writeErrorLog(e, e.getMessage());
				// 通知批次作業管理系統工作失敗,暫停後面流程
				job.writeLog(_programName + "失敗!!");
				job.writeLog("------------------------------------------------------------------");
				_logData.setProgramException(e);
				BatchJobLibrary.sendEMS(_logData);
				// job.AbortTask()
			}
			return BatchReturnCode.ProgramException;
		} finally {
			if (job != null) {
				job.dispose();
				job = null;
			}
			if (_logData != null) {
				_logData = null;
			}
		}
	}

	/**
	 * 檢核Batch所需參數是否足夠
	 */
	private boolean checkConfig() {
		try {
			// 初始化logData物件,傳入工作執行參數
			_logData = new LogData();
			_logData.setChannel(FEPChannel.BATCH);
			_logData.setEj(0);
			_logData.setProgramName(_programName);
			if (StringUtils.isBlank(_FCSInFilePath)) {
				_logData.setRemark("SYSCONF FCSInFilePath未設定");
				job.writeLog(_logData.getRemark());
				BatchJobLibrary.sendEMS(_logData);
				return false;
			} else {
				File file = new File(CleanPathUtil.cleanString(_FCSInFilePath));
				if (!file.exists()) {
					file.mkdirs();
				}
			}
			if (StringUtils.isBlank(_FCSOutFilePath)) {
				_logData.setRemark("參數FCSOutFilePath未設定");
				job.writeLog(_logData.getRemark());
				BatchJobLibrary.sendEMS(_logData);
				return false;
			} else {
				File file = new File(CleanPathUtil.cleanString(_FCSOutFilePath));
				if (!file.exists()) {
					file.mkdirs();
				}
			}
			if (StringUtils.isNotBlank(job.getArguments().get("FCSInFile"))) {
				_FCSFileName = job.getArguments().get("FCSInFile");
			} else {
				job.writeLog("FCSInFile未輸入");
				return false;
			}
			File file = new File(CleanPathUtil.cleanString(_FCSInFilePath + _FCSFileName));
			if (!file.exists()) {
				_logData.setRemark(_FCSInFilePath + _FCSFileName + " 檔案不存在");
				job.writeLog(_logData.getRemark());
				BatchJobLibrary.sendEMS(_logData);
				return false;
			}
			return true;
		} catch (Exception ex) {
			throw ex;
		}
	}

	private FEPReturnCode doBusiness() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			// 讀取回饋檔資料
			wkRtnCode = CommonReturnCode.Normal;

			rtnCode = readFCSFile();
			job.writeLog("ReadFCSFile, rtnCode=" + rtnCode.toString() + ", wkRtnCode=" + wkRtnCode.toString() + ", wkwkTotalCnt=" + wkTotalCnt);

			// modify by Candy , not check , check by Service11X1
			// 1.檢核AP 是否CHECKIN
			// If rtnCode = CommonReturnCode.Normal Then rtnCode = checkSYSSTAT()

			// 2 LOOP 以ＦＣＳ明細筆數
			if (rtnCode == CommonReturnCode.Normal) {
				rtnCode = dealFCSFile();
				job.writeLog("DealFCSFile, rtnCode=" + rtnCode.toString() + ", wkRtnCode=" + wkRtnCode.toString());
			}

			if (rtnCode != CommonReturnCode.Normal && rtnCode != CommonReturnCode.ProgramException) {
				job.writeLog(TxHelper.getMessageFromFEPReturnCode(rtnCode));
			}

			// Modify by Jim, 2011/05/23, 先寫檔，後續再更新TASK參數和通知FCS
			if (wkRtnCode != CommonReturnCode.Normal || wkTotalCnt == 0) {
				job.writeLog("wkRtnCode <> CommonReturnCode.Normal or wkTotalCnt = 0, Call WriteFCSResult");
				writeFCSResult();
			}

			// Modify by Jim, 2011/03/07, If 所有明細筆 R1001.TXT[244:6] <> “000000” (以就是 wkTotalCnt=0), 全部明細筆皆錯也要回饋
			// rtnCode = UpdateSyncOutBatchTaskParameters()
			// job.WriteLog("更新SyncOutBatch的Task參數, rtnCode = " & rtnCode.ToString)
			// If rtnCode <> CommonReturnCode.Normal Then
			// Return rtnCode
			// ElseIf wkRtnCode <> CommonReturnCode.Normal OrElse wkTotalCnt = 0 Then
			// '通知FCS
			// CallSyncOutBatch()
			// End If

			moveFCSFile();

			return rtnCode;
		} catch (Exception ex) {
			job.writeLog("BTOutBatch-doBusiness Exception-" + ex.toString());
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch-doBusiness 發生例外");
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 讀取回饋檔資料
	 */
	private FEPReturnCode readFCSFile() {
		int iSendCNT = 0;
		BigDecimal decSendAmt = new BigDecimal(0);
		int iTotalCNT = 0;
		BigDecimal decTotalAmt = new BigDecimal(0);
		@SuppressWarnings("unused")
		boolean normalFlag = true;
		Rmbtch defRMBTCH = new Rmbtch();
		List<HashMap<String, Object>> dbRMBTCH = new ArrayList<>();
		BufferedReader sr = null;
		try {

			File file = new File(CleanPathUtil.cleanString(_FCSInFilePath + _FCSFileName));
			InputStreamReader read = new InputStreamReader(
					new FileInputStream(file), "Big5");// 考慮到編碼格式
			sr = new BufferedReader(read);
			String line = null;

			// 讀取header
			objFCSHead = new FCSHead();
			line = sr.readLine();
			objFCSHead.parse(line);

			_FCSDataS = new ArrayList<>();

			line = sr.readLine();
			if (StringUtils.isBlank(line)) {
				line = sr.readLine();
			}

			do {
				objFCSData = new FCSDetail();
				BigDecimal bigDecimal = new BigDecimal(ConvertUtil.toBytes(line, PolyfillUtil.toCharsetName("big5")).length);
				if (bigDecimal.compareTo(objFCSData.get_TotalLength()) != 0) {
					break;
				}
				objFCSData.parse(line);

				if (objFCSData.get_DATA_FLAG().equalsIgnoreCase("V")) {
					// Add by Jim, 2011/03/07, SPEC新增FEP檢核邏輯
					if (objFCSData.get_SENDBANK().length() < 7 || !objFCSData.get_SENDBANK().substring(0, 3).equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
						objFCSData.set_FEP_RC(TxHelper.getRCFromErrorCode(RMReturnCode.SenderBankNotBranch, FEPChannel.BRANCH, logContext));
						objFCSData.set_ERRMSG(TxHelper.getMessageFromFEPReturnCode(RMReturnCode.SenderBankNotBranch, logContext));
					} else if (objFCSData.get_SENDBANK().substring(0, 3).equals(objFCSData.get_RECBANK().substring(0, 3))) {
						// 2011/9/21, 檢核匯款行總行不能等於解款行總行
						objFCSData.set_FEP_RC(TxHelper.getRCFromErrorCode(RMReturnCode.SenderBankAndReceiverBankthesame, FEPChannel.BRANCH, logContext));
						objFCSData.set_ERRMSG(TxHelper.getMessageFromFEPReturnCode(RMReturnCode.SenderBankAndReceiverBankthesame, logContext));
					} else {

						Allbank defALLBANK = new Allbank();
						defALLBANK.setAllbankBkno(SysStatus.getPropertyValue().getSysstatHbkno());
						defALLBANK.setAllbankBrno(objFCSData.get_SENDBANK().substring(3, 6));
						List<Allbank> allbankList = allbankExtMapper.queryByPrimaryKey(defALLBANK);
						if (allbankList.size() < 1) {
							objFCSData.set_FEP_RC(TxHelper.getRCFromErrorCode(RMReturnCode.SenderBankNotBranch, FEPChannel.BRANCH, logContext));
							objFCSData.set_ERRMSG(TxHelper.getMessageFromFEPReturnCode(RMReturnCode.SenderBankNotBranch, logContext));
						} else {
							// Fly 2018/07/24 檢核匯出行是否颱風天暫停匯出
							// 2-颱風天暫停匯出
							if ("2".equals(defALLBANK.getAllbankRmforward())) {
								objFCSData.set_FEP_RC(TxHelper.getRCFromErrorCode(RMReturnCode.SenderBankRMNotAttendBusiness, FEPChannel.BRANCH, logContext));
								objFCSData.set_ERRMSG(TxHelper.getMessageFromFEPReturnCode(RMReturnCode.SenderBankRMNotAttendBusiness, logContext));
							}
						}
					}

					// 2011/9/28, 檢核RMBTCH_FCS_INDEX是否已存在RMBTCH
					defRMBTCH.setRmbtchRemdate(objFCSHead.getRemDate());
					defRMBTCH.setRmbtchFcsIndex(objFCSData.get_FCS_Index());
					dbRMBTCH = rmbtchExtMapper.getRMBTCHByDef(defRMBTCH);
					if (dbRMBTCH.size() > 0) {
						// 序號錯誤
						objFCSData.set_FEP_RC(TxHelper.getRCFromErrorCode(RMReturnCode.RecordNumberError, FEPChannel.BRANCH, logContext));
						objFCSData.set_ERRMSG(TxHelper.getMessageFromFEPReturnCode(RMReturnCode.RecordNumberError, logContext));
						// 2011/10/11, 這個欄位是給分行系統用的，FEP檢核錯誤只需要寫FEP_RC和ERRMSG
						// objFCSData.DATA_FLAG = "x"
					}
				}

				_FCSDataS.add(objFCSData);

				if (objFCSData.get_DATA_FLAG().equalsIgnoreCase("V")) {
					iSendCNT = iSendCNT + 1;
					decSendAmt = decSendAmt.add(objFCSData.get_REMAMT());
				}

				iTotalCNT = iTotalCNT + 1;
				decTotalAmt = decTotalAmt.add(objFCSData.get_REMAMT());

				line = sr.readLine();
				if (StringUtils.isBlank(line)) {
					line = sr.readLine();
				}
			} while (line != null);

			// 讀取Foot
			objFCSFoot = new FCSFoot();
			objFCSFoot.parse(line);

			sr.close();

			if (!FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN).equals(objFCSHead.getRemDate())) {
				wkRtnCode = RMReturnCode.RemitDateError;
				return RMReturnCode.RemitDateError;
			}

			if (objFCSFoot.get_SUCESS_CNT() != iSendCNT || objFCSFoot.get_SUCESS_AMT().toBigInteger().compareTo(decSendAmt.toBigInteger()) != 0 ||
					objFCSFoot.get_CNT() != iTotalCNT || objFCSFoot.get_AMT().toBigInteger().compareTo(decTotalAmt.toBigInteger()) != 0) {
				wkRtnCode = RMReturnCode.TotalamtNotMatch;
				return RMReturnCode.TotalamtNotMatch;
			}
			return CommonReturnCode.Normal;

		} catch (Exception ex) {
			job.writeLog("RM_OutBatch-ReadFCSFile Exception-" + ex.toString());
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch, 讀取回饋檔資料發生例外");
			BatchJobLibrary.sendEMS(_logData);
			wkRtnCode = CommonReturnCode.ProgramException;
			return CommonReturnCode.ProgramException;
		} finally {
			if (sr != null) {
				 safeClose(sr);
			}
		}
	}
	
    public void safeClose(BufferedReader fis) {
		 if (fis != null) {
			 try {
				 fis.close();
			 } catch (IOException ex) {
				 ex.printStackTrace();
			 }
		 }
	}

	/**
	 * 檢核AP 是否CHECKIN
	 */
	@SuppressWarnings("unused")
	private FEPReturnCode checkSYSSTAT() {
		try {
			if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact1000())) {
				return RMReturnCode.RMOutOfService;
			}

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			job.writeLog("BTOutBatch-checkSYSSTAT Exception-" + ex.getMessage());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * LOOP以ＦＣＳ明細筆數
	 *
	 * @return FEPReturnCode
	 */
	private FEPReturnCode dealFCSFile() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		@SuppressWarnings("unused")
		String wkREMDATE = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
		@SuppressWarnings("unused")
		String wkBATCHNO = "";
		BigDecimal wkTotalAmt = new BigDecimal(0);

		BigDecimal wkPOSTAMT = new BigDecimal(0);
		int processCnt = 0;

		String FEP_RC = "";
		String ERROR_MSG = "";

		try {

			// 2011/11/14, 寫RMBTCHMTR獨立出來不用跟detail每一筆一起包transaction
			for (FCSDetail obj : _FCSDataS) {
				objFCSData = (FCSDetail) obj;
				wkTotalAmt = wkTotalAmt.add(objFCSData.get_REMAMT());

				// If objFCSData.DATA_FLAG.ToUpper() = "V" AndAlso objFCSData.FEP_RC = NormalRC.FCSFile_OK Then
				// If objFCSData.FEP_RC = NormalRC.FCSFile_OK Then
				// wkTotalCnt = wkTotalCnt + 1
				// End If

				// Fly 2018/08/03 全部失敗也需要回饋
				if (objFCSData.get_DATA_FLAG().equalsIgnoreCase("V") && NormalRC.FCSFile_OK.equals(objFCSData.get_FEP_RC())) {
					wkTotalCnt = wkTotalCnt + 1;
				}
				wkTotalCnt2 = wkTotalCnt2 + 1;

			}

			job.writeLog("wkTotalCnt=" + wkTotalCnt + ", wkTotalAmt=" + wkTotalAmt);
			if (wkTotalCnt2 > 0) {
				rtnCode = prepareAndInsertRMBTCHMTR(wkTotalCnt, wkTotalCnt2, wkTotalAmt);
			} else {
				job.writeLog("wkTotalCnt2=0, 不寫RMBTCHMTR");
			}
			if (rtnCode == CommonReturnCode.Normal) {

				for (FCSDetail obj : _FCSDataS) {
					job.writeLog("開始處理第" + processCnt + "筆資料, DATA_FLAG=" +
							objFCSData.get_DATA_FLAG().toUpperCase() + ". FEP_RC=" + objFCSData.get_FEP_RC());
					PlatformTransactionManager transactionManager = null;
					TransactionStatus txStatus = null;
					try {

						processCnt += 1;
						objFCSData = (FCSDetail) obj;

						// (1) 初始化Business對象
						rtnCode = initBusiness();

						wkREMDATE = objFCSHead.getRemDate();
						wkBATCHNO = objFCSHead.getTimes();

						// wkTotalAmt = wkTotalAmt + objFCSData.REMAMT
						FEP_RC = objFCSData.get_FEP_RC();
						ERROR_MSG = objFCSData.get_ERRMSG();
						if (objFCSData.get_DATA_FLAG().equalsIgnoreCase("V") && objFCSData.get_FEP_RC().equals(NormalRC.FCSFile_OK)) {

							// wkTotalCnt = wkTotalCnt + 1
							// (2) 檢核FCS檔案內容
							RefBase<BigDecimal> bigDecimalRefBase = new RefBase<>(wkPOSTAMT);
							rtnCode = checkData(bigDecimalRefBase);
							wkPOSTAMT = bigDecimalRefBase.get();
							if (rtnCode != CommonReturnCode.Normal) {
								FEP_RC = TxHelper.getRCFromErrorCode(rtnCode, FEPChannel.BRANCH, logContext);
								ERROR_MSG = TxHelper.getMessageFromFEPReturnCode(rtnCode, logContext);
							}

							// Insert RMBTCH start--------------------------------------------------
							// Prepare()大批匯款回饋明細檔, 新增大批匯款回饋明細檔(RMBTCH)
							// 2011/11/15, RMBTCH獨立, 每筆明細一筆資料, 如果新稱RMBTCH成功才需要新增RMOUT & RMOUTT
							// 在這邊先取序號，RMNOCTL需要跟RMBTCH 包 transaction

							Rmnoctl defRMNOCTL = new Rmnoctl();

							defRMNOCTL.setRmnoctlBrno(objFCSHead.getKinBrno());
							// "01"
							defRMNOCTL.setRmnoctlCategory(RMCategory.RMOutTBSDY);
							wkFEPNO = StringUtils.leftPad(String.valueOf(getRmNo(defRMNOCTL.getRmnoctlBrno(), defRMNOCTL.getRmnoctlCategory())), 7, '0');
							transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
							txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

							if (StringUtils.isNumeric(wkFEPNO) && Integer.parseInt(wkFEPNO) > 0) {
								rtnCode = prepareAndInsertRMBTCH(FEP_RC, ERROR_MSG);
							} else {
								rtnCode = IOReturnCode.RMNOCTLUpdateError;
								transactionManager.rollback(txStatus);
							}
							// Insert RMBTCH end--------------------------------------------------

							// Prepare() 交易記錄初始資料, 新增交易記錄(FEPTXN )
							// 2011/11/15, FEPTXN獨立, 每筆明細一筆資料
							prepareAndInsertFEPTXN(rtnCode);

							if (rtnCode == CommonReturnCode.Normal && NormalRC.FCSFile_OK.equals(FEP_RC)) {

								// Insert RMOUT start--------------------------------------------------
								Rmbtch tmpDefRMBTCH = new Rmbtch();
								// dbRMTOT = New DBRMTOT(db)
								// dbRMTOTAL = New DBRMTOTAL(db)

								// (4) Prepare()匯出主檔, 新增匯出主檔 (RMOUT)
								rtnCode = prepareAndInsertRMOUT(wkPOSTAMT);
								if (rtnCode != CommonReturnCode.Normal) {
									FEP_RC = TxHelper.getRCFromErrorCode(rtnCode, FEPChannel.BRANCH, logContext);
									ERROR_MSG = TxHelper.getMessageFromFEPReturnCode(rtnCode, logContext);

									// (7) UpdateTxData: 更新交易記錄(FEPTXN )
									fepTxn.setFeptxnAaRc(rtnCode.getValue());
									fepTxn.setFeptxnReplyCode(FEP_RC);
									updateFEPTXN();

									tmpDefRMBTCH.setRmbtchSenderBank(objFCSData.get_SENDBANK());
									tmpDefRMBTCH.setRmbtchRemdate(objFCSHead.getRemDate());
									tmpDefRMBTCH.setRmbtchTimes(objFCSHead.getTimes());
									tmpDefRMBTCH.setRmbtchFepno(wkFEPNO);
									tmpDefRMBTCH.setRmbtchFepRc(FEP_RC);
									tmpDefRMBTCH.setRmbtchErrmsg(ERROR_MSG);
									if (rmbtchExtMapper.updateByPrimaryKeySelective(tmpDefRMBTCH) < 1) {
										rtnCode = IOReturnCode.RMBTCHUpdateError;
										transactionManager.rollback(txStatus);
									}
								}
								// Insert RMOUT end--------------------------------------------------

								// 2011/11/15, 先不用更新
								// (5) 更新跨行代收付/匯兌清算檔/匯兌統計日結檔
								// If rtnCode = CommonReturnCode.Normal Then
								// rtnCode = TxRMBusiness.ProcessRMTOTAndRMTOTALWithTrans(RMTXCode.R1600, objFCSData.SENDBANK.Substring(3, 3), "002", _
								// objFCSHead.REMDATE, objFCSData.REMAMT, _
								// dbRMTOT, dbRMTOTAL)
								// End If
							}

						}

						if (rtnCode != CommonReturnCode.Normal) {
							job.writeLog("處理第" + processCnt + "筆資料錯誤, rtnCode = " + rtnCode.toString());
						} else {

							Rmnoctl defRMNOCTL = new Rmnoctl();

							defRMNOCTL.setRmnoctlBrno(objFCSHead.getKinBrno());
							// "01"
							defRMNOCTL.setRmnoctlCategory(RMCategory.RMOutTBSDY);
							wkFEPNO = StringUtils.leftPad(String.valueOf(getRmNo(defRMNOCTL.getRmnoctlBrno(), defRMNOCTL.getRmnoctlCategory())), 7, '0');
							transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
							txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

							if (StringUtils.isNumeric(wkFEPNO) && Integer.parseInt(wkFEPNO) > 0) {
								rtnCode = prepareAndInsertRMBTCH(FEP_RC, ERROR_MSG);
								if (rtnCode == CommonReturnCode.Normal) {
									transactionManager.commit(txStatus);
								} else {
									transactionManager.rollback(txStatus);
								}
							} else {
								rtnCode = IOReturnCode.RMNOCTLUpdateError;
								transactionManager.rollback(txStatus);
							}

						}

					} catch (Exception ex) {
						if (transactionManager != null) {
							transactionManager.rollback(txStatus);
						}
						job.writeLog("RM_OutBatch-DealFCSFile, 批號:" + _FCSFileName + ", 處理第" + processCnt + "筆資料例外, exception = " + ex.toString());
						_logData.setProgramException(ex);
						_logData.setRemark("RM_OutBatch-DealFCSFile, 批號:" + _FCSFileName + ", 處理第" + processCnt + "筆資料例外");
						BatchJobLibrary.sendEMS(_logData);
					}
				}
			}

			// Prepare()大批匯款回饋主檔, 新增大批匯款回饋主檔(RMBTCHMTR)
			// If wkTotalCnt > 0 Then
			// rtnCode = PrepareAndInsertRMBTCHMTR(wkTotalCnt, wkTotalAmt)
			// End If

			// If rtnCode = CommonReturnCode.Normal Then
			// db.CommitTransaction()
			// Else
			// db.RollbackTransaction()
			// job.WriteLog("DealFCSFile rtnCode<>Normal, rtnCode = " & rtnCode & ", RollbackTransaction")
			// End If
			return rtnCode;
		} catch (Exception ex) {
			wkRtnCode = CommonReturnCode.ProgramException;
			// If db.Transaction IsNot Nothing Then db.RollbackTransaction()
			job.writeLog("RM_OutBatch-DealFCSFile, out for loop Exception-" + ex.toString());
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch, 批號:" + _FCSFileName + ", 處理回饋檔資料發生例外");
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 初始化對象
	 */
	private FEPReturnCode initBusiness() {
		int iNewEJ = 0;
		RMData TxRMData = new RMData();
		try {
			iNewEJ = TxHelper.generateEj();

			if (TxRMBusiness == null) {
				// 準備FEPTXN對象 FepTxn = New DefFEPTXN
				// dbFEPTXN = New Tables.DBFEPTXN(FEPConfig.DBName, 0, objFCSHead.REMDATE.Substring(6, 2))
				fepTxn = new FeptxnExt();
				feptxnDao.setTableNameSuffix(objFCSHead.getRemDate().substring(6, 8), "0");
				TxRMData = new RMData();
				TxRMData.setAaName("BTOutBatch");
				TxRMData.setTxSubSystem(SubSystem.RM);
				TxRMData.setTxChannel(FEPChannel.FCS);
				TxRMData.setLogContext(new LogData());
				TxRMData.setTxObject(new RMGeneral());

				TxRMData.getLogContext().setEj(iNewEJ);
				TxRMData.getLogContext().setMessage("");
				TxRMData.getLogContext().setRemark("");

				TxRMData.setEj(iNewEJ);
				TxRMData.setFeptxn(fepTxn);
				TxRMData.setFeptxnDao(feptxnDao);

				TxRMBusiness = new RM(TxRMData);

				TxRMBusiness.setFeptxnDao(feptxnDao);
				TxRMBusiness.setFeptxn(fepTxn);
				TxRMBusiness.setEj(iNewEJ);
				TxRMBusiness.setLogContext(TxRMData.getLogContext());
			} else {
				TxRMData.setLogContext(new LogData());
				TxRMData.getLogContext().setEj(iNewEJ);
				TxRMData.setEj(iNewEJ);
				TxRMBusiness.setEj(iNewEJ);
				TxRMBusiness.setLogContext(TxRMData.getLogContext());
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			job.writeLog("BTOutBatch-InitBusiness Exception-" + ex.toString());
			return CommonReturnCode.ProgramException;
		}
	}

	private FEPReturnCode checkData(RefBase<BigDecimal> wkPOSTAMT) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		int wkCnt = 0;

		Rmpost defRMPOST = new Rmpost();

		try {

			if (!"11,12,13,18,19".contains(objFCSData.get_REMTYPE())) {
				return RMReturnCode.RemitKindError;
			}

			if (StringUtils.isBlank(objFCSData.get_NAME_RCV()) || StringUtils.isBlank(objFCSData.get_NAME_SEND())) {
				return RMReturnCode.RemitNameReceiverNameNotBlank;
			}

			/// *126分行不論金額, 作業中心整批總金額超過1000萬 */
			/// * 一般分行單筆金額超過1000萬 */
			if (objFCSHead.getKinBrno().equals("126")) {
				if (StringUtils.isBlank(objFCSHead.getSupNo1())) {
					return RMReturnCode.SuperintendentCodeNoBlank;
				}
			} else {
				if (objFCSData.get_REMAMT().intValue() > 10000000 & StringUtils.isBlank(objFCSHead.getSupNo2())) {
					return RMReturnCode.SuperintendentCodeNoBlank;
				}
			}

			if (objFCSData.get_RECBANK().substring(0, 3).equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
				if (objFCSData.get_SENDBANK().substring(0, 3).equals(objFCSData.get_RECBANK().substring(0, 3))) {
					return RMReturnCode.SenderBankAndReceiverBankthesame;
				}
			}

			rtnCode = checkAllBank();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			rtnCode = checkRMSFP();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			RefBase<Rmpost> rmpostRefBase = new RefBase<>(defRMPOST);
			rtnCode = checkRMPOST(rmpostRefBase);
			defRMPOST = rmpostRefBase.get();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			if ("11,18,19".contains(objFCSData.get_REMTYPE())) {
				/// *計算財金郵電費*/
				if (objFCSData.get_REMAMT().intValue() > defRMPOST.getRmpostBaseamtLimit().intValue()) {
					// 不整除時WK_CNT必須再加1
					wkCnt = (int) Math.ceil(((defRMPOST.getRmpostBaseamtLimit() - defRMPOST.getRmpostBaseamt()) / defRMPOST.getRmpostAddbamt()));
					wkPOSTAMT.set(new BigDecimal(defRMPOST.getRmpostPostamt() + wkCnt * defRMPOST.getRmpostAddamt()));
				} else {
					if (objFCSData.get_REMAMT().longValue() > defRMPOST.getRmpostBaseamt()) {
						// 不整除時WK_CNT必須再加1
						wkCnt = (int) Math.ceil(((objFCSData.get_REMAMT().longValue() - defRMPOST.getRmpostBaseamt()) / defRMPOST.getRmpostAddbamt()));
						wkPOSTAMT.set(new BigDecimal(defRMPOST.getRmpostPostamt() + wkCnt * defRMPOST.getRmpostAddamt()));
					} else {
						wkPOSTAMT.set(new BigDecimal(defRMPOST.getRmpostPostamt()));
					}
				}
			} else {
				wkPOSTAMT.set(new BigDecimal(defRMPOST.getRmpostPostamt()));
			}
			return rtnCode;
		} catch (Exception ex) {
			job.writeLog("RM_OutBatch-CheckData Exception-" + ex.toString());
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch, 批號:" + _FCSFileName + ", CheckData發生例外");
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
	}

	private FEPReturnCode checkAllBank() {
		Allbank defALLBANK = new Allbank();

		try {
			defALLBANK.setAllbankBkno(objFCSData.get_SENDBANK().substring(0, 3));
			defALLBANK.setAllbankBrno("000");

			if (allbankExtMapper.queryByPrimaryKey(defALLBANK).size() < 1) {
				// /*匯款單位無此分行 */
				return RMReturnCode.SenderBankNotBranch;
			}

			defALLBANK.setAllbankBkno(objFCSData.get_RECBANK().substring(0, 3));
			defALLBANK.setAllbankBrno("000");

			if (allbankExtMapper.queryByPrimaryKey(defALLBANK).size() < 1) {
				// /*解款單位無此分行 */
				return RMReturnCode.ReciverBankNotBranch;
			}

		} catch (Exception ex) {
			job.writeLog("RM_OutBatch-CheckAllBank Exception-" + ex.toString());
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch, 批號:" + _FCSFileName + ", Check AllBank發生例外");
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	private FEPReturnCode checkRMSFP() {
		Rmsfp defRMSFP = new Rmsfp();
		int WK_CNT = 0;
		BigDecimal rMReqTxAmt = objFCSData.get_REMAMT();

		try {
			defRMSFP.setRmsfpId(objFCSData.get_REMTYPE());
			// RMSFP_CHARGETYPE.Transmit 收費標準 02
			defRMSFP.setRmsfpChargetype("02");
			defRMSFP.setRmsfpBrno("999");
			defRMSFP.setRmsfpEffectdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
			defRMSFP = rmsfpExtMapper.getRMSFPByPKBeforeTbsdy(defRMSFP);
			if (defRMSFP == null) {
				return IOReturnCode.RMSFPNotFound;
			}

			if ("11,18,19".contains(objFCSData.get_REMTYPE())) // 計算應收客戶手續費*/
			{
				if (rMReqTxAmt.compareTo(new BigDecimal(defRMSFP.getRmsfpBaseamtLimit())) > 0) {
					WK_CNT = (int) Math.ceil((double) ((defRMSFP.getRmsfpBaseamtLimit() - defRMSFP.getRmsfpBaseamt()) / defRMSFP.getRmsfpAddbamt())); // 不整除時WK_CNT必須再加1
					WK_SAMT = new BigDecimal(defRMSFP.getRmsfpServamt() + WK_CNT * defRMSFP.getRmsfpAddamt());
				} else {
					if (rMReqTxAmt.compareTo(new BigDecimal(defRMSFP.getRmsfpBaseamt())) > 0) {
						// 不整除時WK_CNT必須再加1
						WK_CNT = (int) Math.ceil(rMReqTxAmt.subtract(new BigDecimal(defRMSFP.getRmsfpBaseamt())).divide(new BigDecimal(defRMSFP.getRmsfpAddamt())).doubleValue());
						WK_SAMT = new BigDecimal(defRMSFP.getRmsfpServamt() + WK_CNT * defRMSFP.getRmsfpAddamt());
					} else {
						WK_SAMT = new BigDecimal(defRMSFP.getRmsfpServamt());
					}
				}
			} else {
				WK_SAMT = new BigDecimal(defRMSFP.getRmsfpServamt());
			}

			// Modify by Jim, 2011/03/01, FCS帶上來的手續費是優惠過的，不需檢核
			// If objFCSData.FEE <= WK_SAMT Then
			// Return RMReturnCode.HandleChargeNotMatch '應收手續費輸入不符
			// End If

			return CommonReturnCode.Normal;

		} catch (Exception ex) {
			job.writeLog("RM_OutBatch-CheckRMPOST Exception-" + ex.toString());
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch, 批號:" + _FCSFileName + ", Check RMSFP發生例外");
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
	}

	private FEPReturnCode checkRMPOST(RefBase<Rmpost> defRMPOST) {

		try {
			defRMPOST.set(new Rmpost());

			defRMPOST.get().setRmpostPostid(objFCSData.get_REMTYPE());
			defRMPOST.get().setRmpostEffectdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
			List<Rmpost> defRMPOSTList = rmpostExtMapper.getRMPOSTByPKBeforeTbsdy(defRMPOST.get());
			if (defRMPOSTList.size() > 0) {
				defRMPOST.set(defRMPOSTList.get(0));
				if (objFCSData.get_REMAMT().compareTo(new BigDecimal(defRMPOST.get().getRmpostLimitamt())) > 0) {
					return RMReturnCode.RemitAmoutExceed; /// * "5002" =匯款金額超過限額 */
				}
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			job.writeLog("RM_OutBatch-CheckRMPOST Exception-" + ex.toString());
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch, 批號:" + _FCSFileName + ", Check RMPOST發生例外");
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
	}

	private FEPReturnCode prepareAndInsertFEPTXN(FEPReturnCode wkRtnCode) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		try {
			rtnCode = prepareFEPTXNByFCS(wkRtnCode);
			if (rtnCode == CommonReturnCode.Normal) {
				if (feptxnDao.insertSelective(fepTxn) != 1) {
					rtnCode = IOReturnCode.FEPTXNInsertError;
				}
			}
			return rtnCode;
		} catch (Exception ex) {
			job.writeLog("RM_OutBatch-PrepareAndInsertFEPTXN Exception-" + ex.toString());
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch-PrepareAndInsertFEPTXN, 批號:" + _FCSFileName + ", 新增FEPTXN發生例外");
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
	}

	private FEPReturnCode prepareFEPTXNByFCS(FEPReturnCode wkRtnCode) {
		Bsdays defBSDAYS = new Bsdays();

		try {
			fepTxn.setFeptxnTxDate(objFCSHead.getRemDate());
			fepTxn.setFeptxnTxTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
			fepTxn.setFeptxnEjfno(TxRMBusiness.getEj());
			fepTxn.setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno()); // 交易啟動銀行

			defBSDAYS.setBsdaysZoneCode(ZoneCode.TWN);
			defBSDAYS.setBsdaysDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
			defBSDAYS = bsdaysExtMapper.selectByPrimaryKey(defBSDAYS.getBsdaysZoneCode(), defBSDAYS.getBsdaysDate());
			if (defBSDAYS != null) {
				// 財金營業日'
				fepTxn.setFeptxnTbsdyFisc(defBSDAYS.getBsdaysStDateRm());
				// 本行營業日
				fepTxn.setFeptxnTbsdy(defBSDAYS.getBsdaysStDateRm());
			} else {
				// “無法取得日曆檔(BSDAYS)之RM清算日,請查明原因” 之訊息至 EMS
				TxHelper.getRCFromErrorCode(FEPReturnCode.BSDAYSNotFound, FEPChannel.FCS);
				fepTxn.setFeptxnTbsdyFisc(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
				fepTxn.setFeptxnTbsdy(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
			}

			fepTxn.setFeptxnSubsys((short) SubSystem.RM.getValue());
			fepTxn.setFeptxnChannel(FEPChannel.FCS.name());
			// B1’
			fepTxn.setFeptxnMsgflow(FEPTxnMessageFlow.BRS_Request);
			// FepTxn.FEPTXN_REQ_TIME = Now
			fepTxn.setFeptxnTrinActno("00" + objFCSData.get_ACTNO());
			fepTxn.setFeptxnTxAmt(objFCSData.get_REMAMT());
			fepTxn.setFeptxnSenderBank(objFCSData.get_SENDBANK());
			fepTxn.setFeptxnReceiverBank(objFCSData.get_RECBANK());
			fepTxn.setFeptxnMsgid("BTOUTBATCH");
			short tr = 1;
			fepTxn.setFeptxnFiscFlag(tr);

			fepTxn.setFeptxnAaRc(wkRtnCode.getValue());
			fepTxn.setFeptxnChannelEjfno(_FCSFileName + objFCSHead.getKinBrno() + wkFEPNO);
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			job.writeLog("RM_OutBatch-PrepareFEPTXNByFCS Exception-" + ex.toString());
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch-PrepareFEPTXNByFCS, 批號:" + _FCSFileName + ", 準備FEPTXN資料發生例外");
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
	}

	private FEPReturnCode prepareAndInsertRMOUT(BigDecimal wkPOSTAMT) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		Rmout defRMOUT = new Rmout();
		Rmoutt defRMOUTT = new Rmoutt();
		RefBase<Rmout> rmoutRefBase = new RefBase<>(defRMOUT);
		RefBase<Rmoutt> rmouttRefBase = new RefBase<>(defRMOUTT);
		try {
			rtnCode = prepareRMOUT(wkPOSTAMT, rmoutRefBase, rmouttRefBase);
			defRMOUT = rmoutRefBase.get();
			defRMOUTT = rmouttRefBase.get();
			if (rtnCode == CommonReturnCode.Normal) {

				if (rmoutExtMapper.insertSelective(defRMOUT) != 1) {
					return IOReturnCode.RMOUTInsertError;
				}

				if (rmouttExtMapper.insertSelective(defRMOUTT) != 1) {
					return IOReturnCode.RMOUTInsertError;
				}

			}
			return rtnCode;
		} catch (Exception ex) {
			SQLException exSql = (SQLException) ex.getCause();
			String exStr = "";
			if (exSql.getErrorCode() == 2627) {
				// PK Violation
				exStr = "RM_OutBatch-PrepareAndInsertRMOUT, 批號:" + _FCSFileName +
						", exception exSql.number=" + exSql.getErrorCode() +
						", PK Violation, 此筆資料已存在, RMOUT_TXDATE=" + defRMOUT.getRmoutTxdate() + ",RMOUT_BRNO=" + defRMOUT.getRmoutBrno() +
						",RMOUT_ORIGINAL=" + defRMOUT.getRmoutOriginal() + ",RMOUT_FEPNO=" + defRMOUT.getRmoutFepno();
			} else {
				exStr = "RM_OutBatch-PrepareAndInsertRMOUT, 批號:" + _FCSFileName + ", 新增RMOUT資料發生例外, RMOUT_TXDATE="
						+ defRMOUT.getRmoutTxdate() + ",RMOUT_BRNO=" + defRMOUT.getRmoutBrno() + ",RMOUT_ORIGINAL=" +
						defRMOUT.getRmoutOriginal() + ",RMOUT_FEPNO=" + defRMOUT.getRmoutFepno();
			}
			job.writeLog(exStr);
			_logData.setRemark(exStr);
			_logData.setProgramException(ex);
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
			// job.WriteLog("RM_OutBatch-PrepareAndInsertRMOUT Exception-" & ex.ToString)
			// _logData.ProgramException = ex
			// _logData.Remark = "RM_OutBatch-PrepareAndInsertRMOUT, 批號:" & _FCSFileName & ", 新增RMOUT資料發生例外, RMOUT_TXDATE=" & defRMOUT.RMOUT_TXDATE & ",RMOUT_BRNO=" & defRMOUT.RMOUT_BRNO &
			// ",RMOUT_ORIGINAL=" & defRMOUT.RMOUT_ORIGINAL & ",RMOUT_FEPNO=" & defRMOUT.RMOUT_FEPNO
			// BatchJobLibrary.SendEMS(_logData)
			// Return CommonReturnCode.ProgramException
		}
	}

	private FEPReturnCode prepareRMOUT(BigDecimal wkPOSTAMT, RefBase<Rmout> defRMOUT, RefBase<Rmoutt> defRMOUTT) {
		try {
			defRMOUT.get().setRmoutTxdate(objFCSHead.getRemDate());
			defRMOUT.get().setRmoutBrno(objFCSHead.getKinBrno());
			// 1=FCS
			defRMOUT.get().setRmoutOriginal("1");
			defRMOUT.get().setRmoutBatchno(objFCSHead.getTimes());
			// 取FEP登錄序號, 若是被LOCK retry 直到ok '
			// defRMNOCTL.RMNOCTL_BRNO = defRMOUT.RMOUT_BRNO
			// defRMNOCTL.RMNOCTL_CATEGORY = RMCategory.RMOutTBSDY '"01"
			defRMOUT.get().setRmoutFepno(wkFEPNO);
			// wkFEPNO = .RMOUT_FEPNO
			defRMOUT.get().setRmoutFepsubno("00");
			defRMOUT.get().setRmoutRegdate(CalendarUtil.rocStringToADString("0" + objFCSData.get_REGDATE()));
			defRMOUT.get().setRmoutRegtime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
			defRMOUT.get().setRmoutApdate(defRMOUT.get().getRmoutTxdate());
			defRMOUT.get().setRmoutAptime(defRMOUT.get().getRmoutRegtime());
			defRMOUT.get().setRmoutRemtype(objFCSData.get_REMTYPE());
			defRMOUT.get().setRmoutTxamt(objFCSData.get_REMAMT());
			// Modify by Jim, 2010/12/22, 整批匯款&緊急匯款的帳務別改為003
			// 連動轉帳
			defRMOUT.get().setRmoutAmtType(REMTXTP.SeriesOfTransfer);
			// 連動轉帳
			defRMOUT.get().setRmoutServamtType(REMTXTP.SeriesOfTransfer);
			defRMOUT.get().setRmoutPostamt(wkPOSTAMT.intValue());
			defRMOUT.get().setRmoutActfee(objFCSData.get_FEE().intValue());
			// Modify by Jim, 2011/03/01, FEP根據RMSFP算出來的
			defRMOUT.get().setRmoutRecfee(WK_SAMT.intValue());
			// "03" '03=媒體當日匯款匯出類*/
			defRMOUT.get().setRmoutCategory(RMCategory.MediaRMOutTBSDY);
			defRMOUT.get().setRmoutSenderBank(objFCSData.get_SENDBANK());
			defRMOUT.get().setRmoutReceiverBank(objFCSData.get_RECBANK());
			// 04'=放行
			defRMOUT.get().setRmoutStat(RMOUTStatus.Passed);
			defRMOUT.get().setRmoutOutName(objFCSData.get_NAME_SEND());
			defRMOUT.get().setRmoutInName(objFCSData.get_NAME_RCV());
			defRMOUT.get().setRmoutMemo(objFCSData.get_REMARK());
			defRMOUT.get().setRmoutInAccIdNo(objFCSData.get_ACTNO());
			defRMOUT.get().setRmoutFiscSndCode("1" + objFCSData.get_REMTYPE() + "1");
			defRMOUT.get().setRmoutRemcif(objFCSData.get_OUT_ACTNO());
			defRMOUT.get().setRmoutRegTlrno(objFCSHead.getTlrNo());
			defRMOUT.get().setRmoutSupno1(objFCSHead.getSupNo1());
			defRMOUT.get().setRmoutSupno2(objFCSHead.getSupNo2());
			// Modify by Jim, 2011/04/07, EJ1都是給對財金用，應該和R1000一樣用RMOUT_EJNO2
			defRMOUT.get().setRmoutEjno2(fepTxn.getFeptxnEjfno());
			// "P"
			defRMOUT.get().setRmoutPending(RMPending.Pending);
			defRMOUT.get().setRmoutOwpriority("0");
			defRMOUT.set(defRMOUT.get());
			defRMOUTT.set(TxRMBusiness.copyDataToRMOUTT(defRMOUT.get()));
		} catch (Exception ex) {
			job.writeLog("RM_OutBatch-PrepareRMOUT Exception-" + ex.toString());
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch-PrepareRMOUT, 批號:" + _FCSFileName + ", 準備RMOUT資料發生例外, RMOUT_TXDATE="
					+ defRMOUT.get().getRmoutTxdate() + ",RMOUT_BRNO=" + defRMOUT.get().getRmoutBrno() +
					",RMOUT_ORIGINAL=" + defRMOUT.get().getRmoutOriginal() + ",RMOUT_FEPNO=" + defRMOUT.get().getRmoutFepno());
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	public final FEPReturnCode processRMTOTAndRMTOTAL(Rmtot defRMTOT, Rmtotal defRMTOTAL) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		try {
			// 依txCode更新RMTOT不同欄位
			defRMTOT.setRmtotTxdate(objFCSHead.getRemDate());
			defRMTOT.setRmtotBrno("000");

			defRMTOT.setRmtotRegCount(1);
			defRMTOT.setRmtotRegAmt(objFCSData.get_REMAMT());
			if (rmtotExtMapper.updateForProcessRMTOT(defRMTOT) == 0) {
				rmtotExtMapper.insertSelective(defRMTOT);
			}

			// 依txCode更新RMTOTAL不同欄位()
			defRMTOTAL.setRmtotalDate(objFCSHead.getRemDate());
			defRMTOTAL.setRmtotalBrno(objFCSData.get_SENDBANK().substring(3, 6));

			defRMTOTAL.setRmtotalFROTransfNo(1);
			defRMTOTAL.setRmtotalFROTransfAmt(objFCSData.get_REMAMT());

			if (rmtotalExtMapper.updateForProcessRMTOTAL(defRMTOTAL) == 0) {
				rmtotalExtMapper.insertSelective(defRMTOTAL);
			}
		} catch (Exception ex) {
			job.writeLog("RM_OutBatch-ProcessRMTOTAndRMTOTAL Exception-" + ex.toString());
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch-ProcessRMTOTAndRMTOTAL, 批號:" + _FCSFileName + ", 準備RMTOTAndRMTOTAL資料發生例外");
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	private FEPReturnCode prepareAndInsertRMBTCH(String FEP_RC, String ERROR_MSG) {

		Rmbtch defRMBTCH = new Rmbtch();
		try {
			defRMBTCH.setRmbtchDataFlag(objFCSData.get_DATA_FLAG());
			defRMBTCH.setRmbtchSenderBank(objFCSData.get_SENDBANK());
			defRMBTCH.setRmbtchRemdate(objFCSHead.getRemDate());
			defRMBTCH.setRmbtchTimes(objFCSHead.getTimes());
			defRMBTCH.setRmbtchFepno(wkFEPNO);
			defRMBTCH.setRmbtchRecbank(objFCSData.get_RECBANK());
			defRMBTCH.setRmbtchRemtype(objFCSData.get_REMTYPE());
			// Modify by Candy, 2010/12/30, 整批匯款&緊急匯款的帳務別改為003
			defRMBTCH.setRmbtchRemtxtp(REMTXTP.SeriesOfTransfer);
			defRMBTCH.setRmbtchRemamt(String.valueOf(objFCSData.get_REMAMT()));
			defRMBTCH.setRmbtchHctxtp(REMTXTP.SeriesOfTransfer);
			// Modify by Jim, 2011/06/02
			defRMBTCH.setRmbtchFee(objFCSData.get_FEE().toString());
			// .RMBTCH_FEE = wkPOSTAMT.ToString()
			defRMBTCH.setRmbtchActno(objFCSData.get_ACTNO());
			defRMBTCH.setRmbtchRegdate(objFCSData.get_REGDATE());
			defRMBTCH.setRmbtchNameRcv(objFCSData.get_NAME_RCV());
			defRMBTCH.setRmbtchNameSend(objFCSData.get_NAME_SEND());
			defRMBTCH.setRmbtchRemark(objFCSData.get_REMARK());
			defRMBTCH.setRmbtchKinbrno(objFCSHead.getKinBrno());
			defRMBTCH.setRmbtchSupno1(objFCSHead.getSupNo1());
			defRMBTCH.setRmbtchSupno2(objFCSHead.getSupNo2());
			defRMBTCH.setRmbtchTlrno(objFCSHead.getTlrNo());
			defRMBTCH.setRmbtchFlag("0");
			defRMBTCH.setRmbtchFepRc(FEP_RC);
			defRMBTCH.setRmbtchOutActno(objFCSData.get_OUT_ACTNO());
			// If objFCSData.DATA_FLAG.ToUpper = "X" Then
			defRMBTCH.setRmbtchErrmsg(ERROR_MSG);
			// Else
			// .RMBTCH_ERRMSG = ""
			// End If
			defRMBTCH.setRmbtchFcsIndex(objFCSData.get_FCS_Index());
			defRMBTCH.setUpdateUserid(0);
			Date date = new Date();
			defRMBTCH.setUpdateTime(date);

			if (rmbtchExtMapper.insertSelective(defRMBTCH) != 1) {
				return IOReturnCode.RMBTCHInsertError;
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			SQLException exSql = (SQLException) ex.getCause();
			String exStr = "";
			if (exSql.getErrorCode() == 2627) {
				// PK Violation
				exStr = "RM_OutBatch-PrepareAndInsertRMBTCH, 批號:" + _FCSFileName + ", exception exSql.number=" + exSql.getErrorCode() + ", PK Violation, 此筆資料已存在";
			} else {
				exStr = "RM_OutBatch-PrepareAndInsertRMBTCH, 批號:" + _FCSFileName + ", exception" + ex.toString();
			}
			job.writeLog(exStr);
			_logData.setProgramException(ex);
			_logData.setRemark(exStr);
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
	}

	private FEPReturnCode updateFEPTXN() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			fepTxn.setFeptxnMsgflow(BRS_Response);

			if (fepTxn.getFeptxnAaRc() == CommonReturnCode.Normal.getValue()) {
				fepTxn.setFeptxnTxrust(FeptxnTxrust.Successed);
			} else {
				fepTxn.setFeptxnTxrust(FeptxnTxrust.Reverse);
				// FepTxn.FEPTXN_ERR_MF = FepTxn.FEPTXN_MSGFLOW
				// FepTxn.FEPTXN_ERR_RC = FepTxn.FEPTXN_AA_RC.ToString()
			}
			if (feptxnDao.updateByPrimaryKey(fepTxn) != 1) {
				rtnCode = IOReturnCode.FEPTXNUpdateNotFound;
			}
		} catch (Exception ex) {
			job.writeLog("RM_OutBatch-UpdateFEPTXN Exception-" + ex.toString());
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch-UpdateFEPTXN, 批號:" + _FCSFileName + ", 更新FEPTXN資料發生例外");
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	// Fly 2018/08/03 全部失敗也需要回饋
	private FEPReturnCode prepareAndInsertRMBTCHMTR(int wkTotalCnt, int wkTotalCnt2, BigDecimal wkTotalAmt) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		Rmbtchmtr rmbtchmtr = new Rmbtchmtr();
		RefBase<Rmbtchmtr> defRMBTCHMTR = new RefBase<>(rmbtchmtr);
		try {
			rtnCode = prepareRMBTCHMTR(wkTotalCnt, wkTotalCnt2, wkTotalAmt, defRMBTCHMTR);
			if (rtnCode == CommonReturnCode.Normal) {
				if (rmbtchmtrExtMapper.insertSelective(defRMBTCHMTR.get()) != 1) {
					return IOReturnCode.RMBTCHMTRInsertError;
				}
			}
			return rtnCode;
		} catch (Exception ex) {
			SQLException exSql = (SQLException) ex.getCause();
			if (exSql.getErrorCode() == 2627) {
				// PK Violation
				job.writeLog("RM_OutBatch-PrepareAndInsertRMBTCHMTR Exception-, exSql.number=" + exSql.getErrorCode() + ", PK Violation, 此筆資料已存在");
			} else {
				job.writeLog("RM_OutBatch-PrepareAndInsertRMBTCHMTR Exception=" + ex.toString());
			}
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch-PrepareAndInsertRMBTCHMTR, 批號:" + _FCSFileName + ", 準備RMBTCHMTR資料發生例外");
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
	}

	// Fly 2018/08/03 全部失敗也需要回饋
	private FEPReturnCode prepareRMBTCHMTR(int wkTotalCnt, int wkTotalCnt2, BigDecimal wkTotalAmt, RefBase<Rmbtchmtr> defRMBTCHMTR) {
		try {
			defRMBTCHMTR.get().setRmbtchmtrSdn(objFCSData.get_SENDBANK().substring(3, 6));
			defRMBTCHMTR.get().setRmbtchmtrRemdate(objFCSHead.getRemDate());
			defRMBTCHMTR.get().setRmbtchmtrTimes(objFCSHead.getTimes());

			defRMBTCHMTR.get().setRmbtchmtrCnt(wkTotalCnt);
			defRMBTCHMTR.get().setRmbtchmtrAmt(wkTotalAmt);
			// .RMBTCHMTR_FLAG = "0"
			defRMBTCHMTR.get().setRmbtchmtrKinbr(objFCSHead.getKinBrno());
			// 2022-02-08 xy add setupdateUserid 0 setRmbtchmtrKinbr 778 setRmbtchmtrCbsRc E050 setUpdateTime datetime
			defRMBTCHMTR.get().setUpdateUserid(0);
			defRMBTCHMTR.get().setRmbtchmtrKinbr("778");
			defRMBTCHMTR.get().setRmbtchmtrCbsRc("E050");
			Date date = new Date();
			defRMBTCHMTR.get().setUpdateTime(date);

			if (wkTotalCnt == 0) {
				defRMBTCHMTR.get().setRmbtchmtrFlag("1");
				defRMBTCHMTR.get().setRmbtchmtrFailCnt(wkTotalCnt2);
				defRMBTCHMTR.get().setRmbtchmtrFailAmt(wkTotalAmt);
				defRMBTCHMTR.get().setRmbtchmtrSucessCnt(0);
				defRMBTCHMTR.get().setRmbtchmtrSucessAmt(BigDecimal.ZERO);
			} else {
				defRMBTCHMTR.get().setRmbtchmtrFlag("0");
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			job.writeLog("RM_OutBatch-PrepareRMBTCHMTR Exception-" + ex.toString());
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch-PrepareRMBTCHMTR, 批號:" + _FCSFileName + ", 準備RMBTCHMTR資料發生例外");
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 將結果寫入回饋檔中
	 */
	private FEPReturnCode writeFCSResult() {

		BufferedWriter sw = null;
		// 不用加附檔名了
		String FCSOutFileName = _FCSFileName.replace(".TPR", "");
		LogData logData = new LogData();
		int errorStep = 0;
		try {
			logData.setChannel(FEPChannel.FCS);
			logData.setProgramName("BTOutBatch");
			logData.setReturnCode(wkRtnCode);
			logData.setSubSys(SubSystem.RM);
			logData.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));

			sw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(CleanPathUtil.cleanString(_FCSOutFilePath + FCSOutFileName))), "big5"));

			errorStep = 1;
			if (wkRtnCode != CommonReturnCode.Normal) {
				objFCSHead.setFepRc(TxHelper.getRCFromErrorCode(String.valueOf(wkRtnCode), FEPChannel.FEP, FEPChannel.BRANCH, logData));
				objFCSHead.setErrMsg(TxHelper.getMessageFromFEPReturnCode(wkRtnCode, logData));
			} else // 如果wkRtnCode <> CommonReturnCode.Normal就代表: 全部明細筆皆錯
			{
				objFCSHead.setFepRc("EF9999");
				objFCSHead.setFepRc("明細內容拆解有誤");
				// 注意FEPRC及ERRMSG與原TEXT FILE不同, 所以不能再用原TEXTFIE
			}

			errorStep = 2;
			sw.write(String.valueOf(objFCSHead.merge()) + System.lineSeparator());

			errorStep = 3;
			for (Object obj : _FCSDataS) {
				objFCSData = (FCSDetail) obj;
				sw.write(objFCSData.merge() + System.lineSeparator());
			}
			errorStep = 4;
			sw.write(objFCSFoot.merge() + System.lineSeparator());
			sw.close();

			errorStep = 5;
			if (TxRMBusiness == null) {
				TxRMBusiness = new RM();
				// Fly 2014/11/20 除了new物件外給需另外給logContext
				TxRMBusiness.setLogContext(logContext);
			}

			// Modify by Jim, 2011/05/23, 移到更新TASK參數之後再獨立做
			// Modify by Jim, 2011/03/29, 改由另外呼叫SyncOutBatch批次來執行
			// CallSyncOutBatch()

			job.writeLog("準備上傳回饋檔至FCS, 上傳檔名:" + FCSOutFileName);
			if (TxRMBusiness.upLoadFCS(FCSOutFileName) != CommonReturnCode.Normal) {
				job.writeLog(_FCSOutFilePath + FCSOutFileName + "上傳失敗");
			} else {
				job.writeLog("上傳回饋檔至FCS OK");
			}
			errorStep = 6;
			return callFFP001();

		} catch (Exception ex) {
			job.writeLog("RM_OutBatch-WriteFCSResult Exception-" + ex.getMessage() + "; step = " + errorStep);
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch-WriteFCSResult, 批號:" + _FCSFileName + ", 寫回饋檔發生例外");
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		} finally {
			if (sw != null) {
				 safeClose(sw);
			}
		}
	}
	
    public void safeClose(BufferedWriter fis) {
		 if (fis != null) {
			 try {
				 fis.close();
			 } catch (IOException ex) {
				 ex.printStackTrace();
			 }
		 }
	}

	// private FEPReturnCode callSyncOutBatch() {
	// FEPReturnCode rtnCode = CommonReturnCode.Normal;
	//
	// Batch defBATCH = null;
	// try {
	//
	// defBATCH = new Batch();
	// // 如果批次平台檔名修改記得也要一併修改程式
	// defBATCH.setBatchName(SyncOutBatch);
	// defBATCH = batchExtMapper.getSingleBATCHByDef(defBATCH.getBatchName());
	// if (defBATCH != null) {
	// job.writeLog("查詢不到名稱為" + defBATCH.getBatchName() + "的資料列 FROM BATCH Table");
	// return IOReturnCode.QueryNoData;
	// }
	//
	// BatchJobLibrary job2 = new BatchJobLibrary();
	// job2.startBatch(defBATCH.getBatchBatchid().toString(), defBATCH.getBatchStartjobid().toString());
	//
	// job.writeLog("Already call SyncOutBatch");
	//
	// } catch (Exception ex) {
	// job.writeLog("RM_OutBatch-CallSyncOutBatch發生例外, exception=" + ex.toString());
	// _logData.setRemark("RM_OutBatch-CallSyncOutBatch, 批號:" + _FCSFileName + ", 發生例外, ex=" + ex.toString());
	// BatchJobLibrary.sendEMS(_logData);
	// return CommonReturnCode.ProgramException;
	// }
	// return rtnCode;
	// }

	// private FEPReturnCode updateSyncOutBatchTaskParameters() {
	// FEPReturnCode rtnCode = CommonReturnCode.Normal;
	// Task defTASK = null;
	// try {
	// defTASK = new Task();
	//
	// defTASK.setTaskName(UploadFCSFileTask);
	// defTASK.setTaskCommandargs("/FTPParameter:FTPServer4" + " /dir:" + RMConfig.getInstance().getFCSOutPathFTP() + " /uploadfile:"
	// + RMConfig.getInstance().getFCSOutPath() + _FCSFileName);
	//
	// int iRes = taskExtMapper.updateTASKByNAME(defTASK);
	// defTASK = taskExtMapper.selectTaskforName(defTASK);
	// if (iRes != 1) {
	// logContext.setTableDescription(defTASK.getTaskDescription());
	// logContext.setReturnCode(IOReturnCode.UpdateFail);
	// job.writeLog("更新TASK:UploadFCSRMFile的參數失敗, rollback");
	// return IOReturnCode.UpdateFail;
	// }
	// job.writeLog("更新TASK:UploadFCSRMFile的參數: " + defTASK.getTaskCommandargs());
	//
	// defTASK.setTaskName(SyncOutBatchTask);
	// defTASK.setTaskCommandargs("/KINBR:" + objFCSHead.getKinBrno() + " /ENTTLRNO:" + objFCSHead.getTlrNo() +
	// " /SUPNO1:" + objFCSHead.getSupNo1() + " /SUPNO2:" + objFCSHead.getSupNo2() +
	// " /TBSDY:" + objFCSHead.getRemDate() + " /BATCHNO:" + objFCSHead.getTimes());
	//
	// iRes = taskExtMapper.updateTASKByNAME(defTASK);
	// defTASK = taskExtMapper.selectTaskforName(defTASK);
	// if (iRes != 1) {
	// logContext.setTableDescription(defTASK.getTaskDescription());
	// logContext.setReturnCode(IOReturnCode.UpdateFail);
	// job.writeLog("更新TASK:RM_SyncOutBatch的參數失敗, rollback");
	// return IOReturnCode.UpdateFail;
	// }
	// job.writeLog("更新TASK:RM_SyncOutBatch的參數: " + defTASK.getTaskCommandargs());
	//
	// } catch (Exception ex) {
	// job.writeLog("SetSyncOutBatchParameters發生例外, exception=" + ex.toString());
	// return CommonReturnCode.ProgramException;
	// }
	// return rtnCode;
	// }

	private FEPReturnCode moveFCSFile() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String desDirectory = RMConfig.getInstance().getFCSDealFilePath() + FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
		@SuppressWarnings("unused")
		String desFileName = _FCSFileName;

		String url = RMConfig.getInstance().getFCSInPathFTP() + "/" + _FCSFileName;
		// Dim desUrl As String = "ftp://" + RMConfig.Instance.FCSFTPServer + "/" + RMConfig.Instance.FCSDealFilePath_FTP & "/" & _FCSFileName
		try {
			if (TxRMBusiness == null) {
				TxRMBusiness = new RM();
			}
			if (!(new File(CleanPathUtil.cleanString(desDirectory))).isDirectory()) {
				(new File(CleanPathUtil.cleanString(desDirectory))).mkdirs();
			}
			File file = new File(CleanPathUtil.cleanString(desDirectory + "/" + _FCSFileName));
			if (!file.exists()) {
				job.writeLog("Start to Copy from " + RMConfig.getInstance().getFCSInPath() + _FCSFileName + " to " + RMConfig.getInstance().getFCSDealFilePathFTP() + _FCSFileName);
				Path pathIn = (Path) Paths.get(RMConfig.getInstance().getFCSInPath(), _FCSFileName);
				Path pathOut = (Path) Paths.get(desDirectory, _FCSFileName);
				Files.copy(pathIn, pathOut, StandardCopyOption.REPLACE_EXISTING);
			} else {
				job.writeLog("File already Exists in FEP: " + Paths.get(RMConfig.getInstance().getFCSDealFilePath()).resolve(_FCSFileName).toString());
			}

			job.writeLog("Start to delete FTP FILE: from " + url);
			FtpProperties ftpProperties = new FtpProperties();
			ftpProperties.setUsername(RMConfig.getInstance().getFCSFTPUserId());
			ftpProperties.setPassword(RMConfig.getInstance().getFCSFTPSscode());
			ftpProperties.setPort(22);

			ftpProperties.setHost(RMConfig.getInstance().getFCSFTPServer());
			ftpProperties.setProtocol(FtpProtocol.SFTP);
			FtpAdapter ftpAdapter = factory.createFtpAdapter(ftpProperties);
			ftpAdapter.delete(url);
			job.writeLog("After delete FTP FILE: ftpResponse=");

		} catch (Exception ex) {
			job.writeLog("BTOutBatch-MoveFCSFile Exception-" + ex.toString());
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch-MoveFCSFile, 批號:" + _FCSFileName + ", 移除FTP檔案發生例外");
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	private FEPReturnCode callFFP001() {
		RMGeneral rmGeneral = new RMGeneral();
		RMData txData = new RMData();

		try {
			rmGeneral.getRequest().setKINBR(objFCSHead.getKinBrno());
			rmGeneral.getRequest().setENTTLRNO(objFCSHead.getTlrNo());
			rmGeneral.getRequest().setSUPNO1(objFCSHead.getSupNo1());
			rmGeneral.getRequest().setSUPNO2(objFCSHead.getSupNo2());
			rmGeneral.getRequest().setTBSDY(objFCSHead.getRemDate());
			rmGeneral.getRequest().setREMDATE(objFCSHead.getRemDate());
			rmGeneral.getRequest().setBATCHNO(objFCSHead.getTimes());

			txData.setEj(TxHelper.generateEj());
			txData.setTxObject(rmGeneral);
			txData.setTxChannel(FEPChannel.FEP);
			txData.setTxSubSystem(SubSystem.RM);
			// txData.TxRequestMessage = SerializeToXml(rmGeneral.Request).Replace("&lt;", "<").Replace("&gt;", ">")
			txData.setMessageID("FFB001");
			txData.setFepResponse(new FEPResponse());
			txData.setAaName("FFB001");

			txData.setLogContext(new LogData());
			txData.getLogContext().setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
			txData.getLogContext().setSubSys(SubSystem.RM);
			txData.getLogContext().setChannel(FEPChannel.FEP);
			txData.getLogContext().setProgramFlowType(ProgramFlow.AAIn);
			txData.getLogContext().setMessageFlowType(MessageFlow.Request);
			txData.getLogContext().setEj(txData.getEj());
			txData.getLogContext().setMessage(txData.getTxRequestMessage());
			txData.getLogContext().setProgramName("FFB001");

			// aa = new FFB001(txData);
			//
			// aa.processRequestData();
			this.runRMAA("FFB001", txData);

			job.writeLog("After Call FFB001, RsStateCode = " + txData.getTxObject().getResponse().getRsStatRsStateCode());
			if (!NormalRC.External_OK.equals(txData.getTxObject().getResponse().getRsStatRsStateCode())) {
				return CommonReturnCode.Abnormal;
			} else {
				return CommonReturnCode.Normal;
			}

		} catch (Exception ex) {
			job.writeLog("RM_OutBatch-CallFEP001 Exception-" + ex.toString());
			_logData.setProgramException(ex);
			_logData.setRemark("RM_OutBatch-CallFEP001, 回饋FCS發生例外");
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}

	}

	/**
	 * 將物件序列化成XML,此方法為有沒有定義Namespace的物件使用(如FEPMessage)
	 *
	 * @param object
	 */
	protected static String serializeToXml(Object object) {
		return XmlUtil.toXML(object);
	}

	private String runRMAA(String msgId, RMData fData) throws Exception {
		AABaseFactory aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
		return aaBaseFactory.processRmRequestData(msgId, fData);
	}

	public int getRmNo(String brno, String category) throws Exception {
		// 呼叫storeprocedure
		Rmnoctl defRmnoctl = new Rmnoctl();
		int rmno = 0;
		try {
			defRmnoctl.setRmnoctlBrno(brno);
			defRmnoctl.setRmnoctlCategory(category);
			int nextId = 0;
			Map<String, Object> pars = new HashMap<>();
			pars.put("brno", defRmnoctl.getRmnoctlBrno());
			pars.put("category", defRmnoctl.getRmnoctlCategory());
			userDefineExtMapper.getRMNO(pars);
			String str = pars.toString();
			int begin = str.indexOf("=") + 1;
			int end = str.indexOf(",");
			nextId = Integer.parseInt(str.substring(begin, end));
			rmno = nextId;
			defRmnoctl.setRmnoctlNo(rmno);
			return rmno;
		} catch (Exception e) {
			logContext.setProgramException(e);
			return -1;
		}
	}
}
