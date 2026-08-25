package com.syscom.fep.server.common.adapter;

import com.ibm.ims.connect.*;
import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.pool.GenericPool;
import com.syscom.fep.frmcommon.pool.GenericPooledFactory;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.EnvPropertiesUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.CbstestdataExtMapper;
import com.syscom.fep.mybatis.model.Cbstestdata;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.poi.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.net.SocketException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * 負責接收電文並傳送至IMS主機
 *
 * @author Richard,Bruce
 */
public class IMSAdapter extends AdapterBase {

	private CbstestdataExtMapper cbstestdataExtMapper = SpringBeanFactoryUtil.getBean(CbstestdataExtMapper.class);

	private ConnectionFactory myCF;

	private Connection myConn;

	private TmInteraction myTmInteraction;

	private InputMessage inMsg;

	private OutputMessage outMsg;

	private String outStr = "";

	private static int CONNECT_TIMEOUT = ApiProperties.TIMEOUT_15_SECONDS;
	private static int FP_TIMEOUT_TIMES = 20;
	private static int INT_TIMEOUT_OFFSET = ApiProperties.TIMEOUT_45_SECONDS;

	private MessageBase txData;
	/**
	 * 送給IMS的訊息
	 */
	private String messageToIMS;
	/**
	 * IMS回應訊息
	 */
	private String messageFromIMS;
	/**
	 * 交易的TranCode
	 */
	private String trancode = "";

	private String CBSTxid;

	private int do_flag = 0;/* 20221117 */

	private boolean exit_flag = false;// 判斷是否跳出迴圈

	private static final  String _hostName;
	private static final int _hostPort;
	private static String _clientIds_tx; // 交易用的client id
	private static String _clientIds_pbo; // 補摺用的client id
	private static String _clientIds_ck; // 換key用的client id
	private static final int _timeout;
	private static final String _dsName;
	private static GenericPool<String> _clientIdsPooled_tx;
	private static GenericPool<String> _clientIdsPooled_pbo;
	private static GenericPool<String> _clientIdsPooled_ck;
	private boolean isPBO = false;
	private String _isCBSTest; // [20221003]新增程式碼
	private static ApiLoggingConfiguration apiLoggingConfig = new ApiLoggingConfiguration();
	private String _wsid;
	private String _atmSeq;
	private long startTime = 0;

	public void setTranCode(String tranCode) {
		this.trancode = tranCode;
	}

	public void setWSID(String wsid) {
		this._wsid = wsid;
	}

	public void setATMSeq(String atmseq) {
		this._atmSeq = atmseq;
	}

	public String getTrancode() {
		return trancode;
	}

	// static {
	// /**
	// * 1.取得IMS主機相關參數
	// * 主機相關參數為整個process共用且僅需設定一次, 請存入private static變數, 且在static constructor中讀取
	// */
	// _hostName = CMNConfig.getInstance().getIMSHostName();
	// _hostPort = CMNConfig.getInstance().getIMSPort();
	// _timeout = CMNConfig.getInstance().getCBSTimeout() * 1000;
	// _dsName = CMNConfig.getInstance().getIMSDatastoreName();
	// _isCBSTest = CMNConfig.getInstance().getIsCBSTest(); // [20221003]新增程式碼
	// }
	static {
		/**
		 * 1.取得IMS主機相關參數
		 * 主機相關參數為整個process共用且僅需設定一次, 請存入private static變數, 且在static constructor中讀取
		 */
		_hostName = CMNConfig.getInstance().getIMSHostName();
		_hostPort = CMNConfig.getInstance().getIMSPort();
		_timeout = CMNConfig.getInstance().getCBSTimeout() * 1000;
		_dsName = CMNConfig.getInstance().getIMSDatastoreName();

		_clientIds_tx = EnvPropertiesUtil.getProperty("spring.fep.ims.tx.clientid", "");
		_clientIds_pbo = EnvPropertiesUtil.getProperty("spring.fep.ims.pbo.clientid", "");
		_clientIds_ck = EnvPropertiesUtil.getProperty("spring.fep.ims.changekey.clientid", "");
		/**
		 * 2.建立client id的 object pool
		 * 在static constructor中將_clientId依, 分隔至陣列中, 並存入至object pool中
		 */

		@SuppressWarnings("rawtypes")
		GenericPooledFactory factory_tx = new GenericPooledFactory<>();
		GenericPooledFactory factory_pbo = new GenericPooledFactory<>();
		GenericPooledFactory factory_ck = new GenericPooledFactory<>();
		factory_tx.addAll(parseClientId(_clientIds_tx)); // [20221005]修改程式碼: Arrays.asList(_clientIds.split(",")) 修改成
		// parseClientId(_clientIds)
		factory_pbo.addAll(parseClientId(_clientIds_pbo));
		factory_ck.addAll(parseClientId(_clientIds_ck));
		GenericObjectPoolConfig<String> config_tx = new GenericObjectPoolConfig<>();
		config_tx.setMaxTotal(factory_tx.size());
		config_tx.setMaxIdle(factory_tx.size());
		// 從Pool中取得clientId, 若取不到時, 請等待1秒後再重試, 直到超過_timeout的秒數都取不到為止
		config_tx.setMaxWait(Duration.ofMillis(_timeout - 1000));
		GenericObjectPoolConfig<String> config_pbo = new GenericObjectPoolConfig<>();
		config_pbo.setMaxTotal(factory_pbo.size());
		config_pbo.setMaxIdle(factory_pbo.size());
		// 從Pool中取得clientId, 若取不到時, 請等待1秒後再重試, 直到超過_timeout的秒數都取不到為止
		config_pbo.setMaxWait(Duration.ofMillis(_timeout - 1000));
		GenericObjectPoolConfig<String> config_ck = new GenericObjectPoolConfig<>();
		config_ck.setMaxTotal(factory_ck.size());
		config_ck.setMaxIdle(factory_ck.size());
		config_ck.setMaxWait(Duration.ofMillis(_timeout - 1000));

		_clientIdsPooled_tx = new GenericPool<>(factory_tx, config_tx);
		_clientIdsPooled_tx.setBorrowObjectBySequence(); // 設定取element按照順序取
		_clientIdsPooled_pbo = new GenericPool<>(factory_pbo, config_pbo);
		_clientIdsPooled_pbo.setBorrowObjectBySequence(); // 設定取element按照順序取
		_clientIdsPooled_ck = new GenericPool<>(factory_ck, config_ck);
		_clientIdsPooled_ck.setBorrowObjectBySequence(); // 設定取element按照順序取
	}

	@SuppressWarnings("unchecked")
	public IMSAdapter(MessageBase txData) {
		this.txData = txData;

		// String logPath = EnvPropertiesUtil.getProperty("spring.fep.log.path", "");
		// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		// String apName =
		// EnvPropertiesUtil.getProperty("management.metrics.tags.application", "");
		// String apiLog = Paths.get(logPath, LocalDate.now().format(formatter), apName,
		// "IMSConnect.log").toString();
		_isCBSTest = CMNConfig.getInstance().getIsCBSTest(); // [20221003]新增程式碼
		// try {
		// apiLoggingConfig.configureApiLogging(apiLog,
		// ApiProperties.TRACE_LEVEL_INTERNAL);
		// } catch (ImsConnectApiException e) {
		// LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
		// }
	}

	/**
	 * 20221201 Bruce add 傳訊訊息給IMS並取得回應 TODO
	 *
	 * @return
	 */
	@Override
	public FEPReturnCode sendReceive() {
		FEPReturnCode rtn = CommonReturnCode.Normal;
		String clientId = null;
		boolean successFul = false;
		String wsid = "";
		String atmSeq = "";
		try {
			// 若為PBO電文則抓不同的clientID
			if (this.getTrancode() != null && "EBTSPM00".equals(this.getTrancode())) {
				isPBO = true;
			}
			startTime = System.currentTimeMillis();

			// 先從Pool取出一筆ClientId
			clientId = this.getClientIdFromPool(isPBO); // [20221226]clientId修改成_clientIds
			if (StringUtils.isEmpty(clientId)) {
				throw new Exception("Get ClientId Fail");
			}
			// 3.記錄FEPLOG內容
			this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterIn);
			this.txData.getLogContext().setMessageFlowType(MessageFlow.Request);
			this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
			this.txData.getLogContext().setMessage(this.messageToIMS);
			this.txData.getLogContext()
					.setRemark(StringUtils.join("Ready Send data to IMS, hostName = [", _hostName, "], hostPort = [",
							_hostPort, "], clientId = [", clientId, "], timeout = [", _timeout, "], dsName = [",
							_dsName, "]"));
			logMessage(this.txData.getLogContext());
			FEPReturnCode fepReturnCode = this.getCbtestdataResponseToMessageFromIMS();// 從CBSTESTDATA(ByCBSTxid)取出Response賦值給messageFromIMS
			// // [20221003]新增程式碼
			if (StringUtils.isNotBlank(this.messageFromIMS) || (fepReturnCode != FEPReturnCode.Normal)) {
				return fepReturnCode;
			}

			successFul = this.connect(clientId);
			if (successFul) {
				// successFul = sendReceiveFPTrade(_clientId, _dsName, _trancode, inputData);
				successFul = sendReceiveFPTrade(clientId);
				if (successFul && StringUtils.isNotBlank(this.getMessageFromIMS())) {
					rtn = FEPReturnCode.Normal;
				} else {
					rtn = FEPReturnCode.HostResponseTimeout;
				}
			}
		} catch (Exception e) {
			rtn = handleException(this.txData.getLogContext(), e, "sendReceive");
		} finally {
			// 10.與IMS斷線
			if (myConn != null) {
				try {
					myConn.disconnect();
				} catch (ImsConnectApiException e) {
					LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
				}
			}
			// 斷線後歸還clientId至Pool
			if (StringUtils.isNotEmpty(clientId)) {
				this.putClientIdToPool(isPBO, clientId);
			}
			// 11.記錄FEPLOG內容
			this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterOut);
			this.txData.getLogContext().setMessageFlowType(MessageFlow.Response);
			this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
			this.txData.getLogContext().setMessage(this.messageFromIMS);
			this.txData.getLogContext()
					.setRemark(StringUtils.join("Get data from IMS, hostName = [", _hostName, "], hostPort = [",
							_hostPort, "], clientId = [", clientId, "], timeout = [", _timeout, "], dsName = [",
							_dsName, "]"));
			logMessage(this.txData.getLogContext());
		}
		// 12.回傳ReturnCode
		return rtn;
	}

	/**
	 *
	 * @return
	 */
	public boolean connect(String clientId) {
		LogHelperFactory.getGeneralLogger().info(" [MethodName:connectInfo] start ");
		boolean yes_no = true;

		try {
			if (myCF == null) {
				myCF = new ConnectionFactory();
				myCF.setHostName(_hostName);
				myCF.setPortNumber(_hostPort);
				myCF.setClientId(clientId);
			}
		} catch (ImsConnectApiException e) {
			LogHelperFactory.getGeneralLogger().error(":" + e.getMessage());
			yes_no = false;
			return yes_no;
		}
		LogHelperFactory.getGeneralLogger()
				.info(" [MethodName:connectInfo] insert Connection info Success ");
		LogHelperFactory.getGeneralLogger().info(" [MethodName:connectInfo] end ");
		return yes_no;
	}

	// TODO
	public boolean sendReceiveFPTrade(String clientId) {
		byte[] inputData = hexToBytes(messageToIMS);
		boolean successful = true;
		// int timeout = CONNECT_TIMEOUT * FP_TIMEOUT_TIMES;

		do_flag = 0; /* 20221117 */
		LogHelperFactory.getGeneralLogger().info(" [MethodName:sendRecvFPTrade] start ");
		// 0829 build connecction fail return
		if (false == buildConnection(CONNECT_TIMEOUT)) {
			return false;
		}

		// 0822 for clear tpipe(要先清queue的殘留資料)
		LogHelperFactory.getGeneralLogger().info(" [MethodName:sendRecvFPTrade] Begin resumeClearTpipe ");
		resumeClearTpipe(clientId, ApiProperties.RETRIEVE_SYNC_OR_ASYNC_MESSAGE,
				ApiProperties.TIMEOUT_100_MILLISECONDS);
		this.messageFromIMS = ""; // 避免收前到前筆的response回傳先resume後先清掉
		LogHelperFactory.getGeneralLogger().info(" [MethodName:sendRecvFPTrade] Begin interaction ");
		// 根據4/3會議將commitMode調整為0，僅補摺交易維持1
		if (isPBO)
			successful &= interaction(clientId, ApiProperties.INTERACTION_TYPE_DESC_SENDRECV,
					ApiProperties.COMMIT_MODE_1);
		else
			// 20250205: 因為新的resumeTpipe寫法統一用commit mode 0
			successful &= interaction(clientId, ApiProperties.INTERACTION_TYPE_DESC_SENDONLY,
					ApiProperties.COMMIT_MODE_0);
		LogHelperFactory.getGeneralLogger().info(" [MethodName:sendRecvFPTrade] interaction successful:" + successful);
		LogHelperFactory.getGeneralLogger().info(" [MethodName:sendRecvFPTrade] Begin input ");
		successful &= input(inputData, ApiProperties.INPUT_MESSAGE_DATA_SEGMENTS_DO_INCLUDE_LLZZ_AND_TRANCODE); // ????
		LogHelperFactory.getGeneralLogger().info(" [MethodName:sendRecvFPTrade] input successful:" + successful);
		LogHelperFactory.getGeneralLogger().info(" [MethodName:sendRecvFPTrade] Begin execute ");
		successful &= execute();
		LogHelperFactory.getGeneralLogger().info(" [MethodName:sendRecvFPTrade] execute successful:" + successful);

		if (!successful) // execute 失敗就不用再往下做了
			return false;

		LogHelperFactory.getGeneralLogger().info(" [MethodName:sendRecvFPTrade] Begin output ");
		successful &= output();
		LogHelperFactory.getGeneralLogger().info(" [MethodName:sendRecvFPTrade] output successful:" + successful);

		if (StringUtils.isBlank(this.messageFromIMS)) {

			/* 20221117 begin */
			do_flag = 1;
			LogHelperFactory.getGeneralLogger().info(" [MethodName:sendRecvFPTrade] Begin resumeClearTpipe");
			try {
				// 當connectionTimeout > interactionTimeout時, 會引發receive Timeout例外
				// 若未超過, 則到connectionTimeout時間未收到資料會getImsConnectReturnCode=40的rc
				
				if (isPBO) {
					// 若為補摺交易, 因exccute應已取得電文,這裏把timeout時間改為1秒再確認一次無資料可取得
					successful &= resumeClearTpipe(clientId, ApiProperties.RETRIEVE_SYNC_OR_ASYNC_MESSAGE,
							ApiProperties.TIMEOUT_1_SECOND);
					
				}
				else  { 
					//一般交易維持原來timeout時間
					successful &= resumeClearTpipe(clientId, ApiProperties.RETRIEVE_SYNC_OR_ASYNC_MESSAGE,
						   _timeout);
				}
					
				// long start = System.currentTimeMillis();
				// while ((myTmInteraction.getOutputMessage()
				// .getImsConnectReturnCode() == ApiProperties.IMS_CONNECT_RETURN_CODE_SUCCESS
				// ||
				// myTmInteraction.getOutputMessage().getImsConnectReturnCode() == 40)
				// && StringUtils.isEmpty(this.getMessageFromIMS())) {
				// successful &= resumeClearTpipe(clientId,
				// ApiProperties.RETRIEVE_SYNC_OR_ASYNC_MESSAGE,
				// ApiProperties.TIMEOUT_10_MILLISECONDS);
				// if (System.currentTimeMillis() - start > _timeout) {
				// break;
				// }
				// try {
				// Thread.sleep(1000);
				// } catch (InterruptedException e) {
				// }
				// }
			} catch (Exception e) {
				LogHelperFactory.getGeneralLogger().info(" [MethodName:sendRecvFPTrade] exception:" + e.getMessage());
			}
		}
		LogHelperFactory.getGeneralLogger()
				.info(" [MethodName:sendRecvFPTrade] getFromIMS:" + this.getMessageFromIMS());
		LogHelperFactory.getGeneralLogger()
				.info(" [MethodName:sendRecvFPTrade] resumeClearTpipe successful:" + successful);
		return successful;
	}

	
	public boolean execute() {
		LogHelperFactory.getGeneralLogger().info(" [MethodName:execute] start ");
		boolean yes_no = true;

		try {
			myTmInteraction.execute();
		} catch (Exception e) {
			LogHelperFactory.getGeneralLogger().error(e.getMessage());
			yes_no = false;
		}
		LogHelperFactory.getGeneralLogger().info(" [MethodName:execute] TmInteraction execute Success ");
		LogHelperFactory.getGeneralLogger().info(" [MethodName:execute] end ");
		return yes_no;
	}

	
	public boolean output() {
		LogHelperFactory.getGeneralLogger().info(" [MethodName:output] start ");
		boolean yes_no = true;

		try {
			// myTmInteraction.setInteractionTimeout(-1);
			outMsg = myTmInteraction.getOutputMessage();
			this.outStr = this.getOutputString(outMsg);
			if (outStr == null) {
				// yes_no = false;
			} else {

				// 若aa有傳wsid及atmseq進來,則需比對IMS的response是否符合
				if (StringUtils.isNotEmpty(_wsid) && StringUtils.isNotEmpty(_atmSeq)) {
					String wsid = this.outStr.substring(0, 10);
					String atmSeq = this.outStr.substring(46, 54);
					LogHelperFactory.getGeneralLogger()
							.info(" [MethodName:output] get wsid:" + wsid + ",atmSeq:" + atmSeq + " from IMS");
					if (this._wsid.equals(_wsid) && this._atmSeq.equals(atmSeq)) {
						this.setMessageFromIMS(this.outStr);
						LogHelperFactory.getGeneralLogger()
								.info(" [MethodName:output] compare AA wsid and atmSeq match ,the outStr value set MessageFromIMS ");
					} else {
						LogHelperFactory.getGeneralLogger()
								.info(" [MethodName:output] compare AA data not match, AA wsid:" + _wsid + ",atmSeq:"
										+ _atmSeq);
					}
				} else {
					this.setMessageFromIMS(this.outStr);
					LogHelperFactory.getGeneralLogger()
							.info(" [MethodName:output] outStr is not empty,the value set MessageFromIMS ");
				}

			}
		} catch (ImsConnectApiException e) {
			LogHelperFactory.getGeneralLogger().error(e.getMessage());
			yes_no = false;
		}
		LogHelperFactory.getGeneralLogger().info(" [MethodName:output] end ");
		return yes_no;
	}

	// TODO
	public String getOutputString(OutputMessage outMsg) throws ImsConnectApiException {
		String result;
		LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString] start ");
		// try
		// {
		// result = new String(outMsg.getDataAsByteArray(), "IBM-1047");
		// System.out.println("getOutputString ASCII:"+result);
		result = ConvertUtil.toHex(outMsg.getDataAsByteArray());
		// System.out.println("getOutputString ConvertUtil toHex:" + result);
		LogHelperFactory.getGeneralLogger()
				.info(" [MethodName:getOutputString] getOutputString ConvertUtil toHex:" + result);
		// System.out.println(this.dateFormat.format(new Date()) + " Receive data
		// length:" + result.length());
		// System.out.println(this.dateFormat.format(new Date()) + " Receive data
		// content:" + result);
		// int csmPos = result.indexOf("*CSMOKY*");
		int csmPos = result.indexOf("5CC3E2D4D6D2E85C");
		LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString] csmPos: " + csmPos);
		
		int dfs2082 = result.indexOf("C4C6E2");
	    LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString] dfs2082: " + dfs2082);
		if (dfs2082 <= 2 && dfs2082 >=0) {  //若DFS出現在第3個字代表是error code, 則回傳null
			LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString] dfs2082 <= 2 return null ");
			return null;
		} else {
			if (csmPos >= 0) {
				this.exit_flag = true;
				LogHelperFactory.getGeneralLogger()
						.info(" [MethodName:getOutputString] csmPos >= 0 return result.substring(0, csmPos) end ");
				return result.substring(0, csmPos);
			} else {
				LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString] csmPos < 0 return null ");
				return null;
			}
		}
		// }
		// catch (UnsupportedEncodingException e)
		// {
		// //System.out.println(this.dateFormat.format(new Date()) + " getOutputString
		// failed: " + e.getMessage());
		// return null;
		// }
	}

	// TODO
	public boolean input(byte[] _inputData, boolean isInputMessageDataSegmentsIncludeLlzzAndTrancode) {
		LogHelperFactory.getGeneralLogger().info(" [MethodName:input] start ");
		boolean yes_no = _inputData.length > 0 ? true : false;

		// System.out.println(this.dateFormat.format(date) + " _content=" + _inputData);

		if (!yes_no) {
		} else {
			// InputMessage inMsg;

			try {
				myTmInteraction.setSyncLevel((byte) 0);
				// myTmInteraction.setSyncLevel(new Integer(0).byteValue());
				inMsg = myTmInteraction.getInputMessage();

				inMsg.setInputMessageData(_inputData);
				// inMsg.setInputMessageDataSegmentsIncludeLlzzAndTrancode(isInputMessageDataSegmentsIncludeLlzzAndTrancode);
				_inputData = inMsg.getDataAsByteArray();

				String fileContent = "Hex=";
				for (int i = 0; i < _inputData.length; i++) {
					fileContent += String.format("%02x", _inputData[i]);
				}
				LogHelperFactory.getGeneralLogger().info(" [MethodName:input] Hex Telegram Content: " + fileContent);
				LogHelperFactory.getGeneralLogger()
						.info(" [MethodName:input] ASC Telegram Content: " + inMsg.getDataAsString());
			} catch (ImsConnectApiException e) {
				// System.out.println(this.dateFormat.format(new Date()) + ":"+ e.getMessage());
				yes_no = false;
			}
		}
		LogHelperFactory.getGeneralLogger().info(yes_no == true ? " [MethodName:input] set InputMessageData Success "
				: " [MethodName:input] set InputMessageData Failed ");
		LogHelperFactory.getGeneralLogger().info(" [MethodName:input] end ");
		return yes_no;
	}

	//
	public boolean interaction(String clientId, String _itd, byte _commitMode) {
		LogHelperFactory.getGeneralLogger()
				.info(" [MethodName:interaction] start, commit mode: " + (_commitMode == 64 ? "0" : "1"));
		boolean yes_no = true;

		try {
			myTmInteraction = myConn.createInteraction();
			myTmInteraction.setLtermOverrideName(clientId);
			myTmInteraction.setImsDatastoreName(_dsName);
			// myTmInteraction.setImsConnectTimeout(ApiProperties.TIMEOUT_15_SECONDS);
			myTmInteraction.setInteractionTimeout(ApiProperties.TIMEOUT_15_SECONDS);
			// myConn.setSocketConnectTimeout(100000);
			// myConn.setInteractionTimeout(ApiProperties.TIMEOUT_1_MINUTE);
			if (StringUtils.isNotBlank(trancode) && trancode.length() > 0) {
				myTmInteraction.setTrancode(trancode);
			} else {
				myTmInteraction.setTrancode("");
			}

			myTmInteraction.setInteractionTypeDescription(_itd);

			myTmInteraction.setCommitMode(_commitMode);
		} catch (ImsConnectApiException e) {
			LogHelperFactory.getGeneralLogger().error(e.getMessage());
			yes_no = false;
		}
		LogHelperFactory.getGeneralLogger()
				.info(" [MethodName:interaction] insert TmInteraction Success ");
		LogHelperFactory.getGeneralLogger().info(" [MethodName:interaction] end -");
		return yes_no;
	}

	// 0822 for resume clear tpipe
	public boolean resumeClearTpipe(String clientId, byte _retrieveType, int timeout) {
		boolean successful = true;
		LogHelperFactory.getGeneralLogger().info(" [MethodName:resumeClearTpipe] start , timeout:" + timeout);
		try {
			// System.out.println(this.dateFormat.format(new Date()) + " RESUMETPIPE for
			// clear tpipe Start");
			/*
			 * 20221117
			 * if( do_flag == 1 )
			 * myConn.setInteractionTimeout(-1);
			 */
			myTmInteraction = myConn.createInteraction();
			myTmInteraction.setImsDatastoreName(_dsName);// Change "MYDATASTORENAME" to your corresponding datastore
			// name
			myTmInteraction.setLtermOverrideName(clientId);

			// myTmInteractionAttr.setInputMessageDataSegmentsIncludeLlzzAndTrancode(ApiProperties.INPUT_MESSAGE_DATA_SEGMENTS_DO_NOT_INCLUDE_LLZZ_AND_TRANCODE);
			// myTmInteraction.setTrancode("");
			if (StringUtils.isNotBlank(trancode) && trancode.length() > 0) {
				myTmInteraction.setTrancode(trancode);
			} else {
				myTmInteraction.setTrancode("");
			}
			myTmInteraction.setCommitMode(ApiProperties.COMMIT_MODE_0);
			myTmInteraction.setAckNakProvider(ApiProperties.CLIENT_ACK_NAK);

			// RESUMETPIPE 0815 for clear response
			resumeTPipe(clientId, trancode, ApiProperties.RESUME_TPIPE_NOAUTO, _retrieveType, timeout);

			// int i = 0;
			// if (no == 0) {
			// while (myTmInteraction.getOutputMessage()
			// .getImsConnectReturnCode() == ApiProperties.IMS_CONNECT_RETURN_CODE_SUCCESS)
			// {
			// LogHelperFactory.getGeneralLogger().info(" [MethodName:resumeClearTpipe]
			// While Start ");
			// LogHelperFactory.getGeneralLogger().info(
			// " [MethodName:resumeClearTpipe]
			// myTmInteraction.getOutputMessage().getImsConnectReturnCode(): "
			// + myTmInteraction.getOutputMessage().getImsConnectReturnCode());
			// if (myTmInteraction.isAckNakNeeded()) {
			// myTmInteraction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_ACK);
			// // Execute interaction
			// myTmInteraction.execute();
			// }
			// //
			// // try {
			// // Thread.sleep(1000);
			// // } catch (InterruptedException e) {
			// // }
			// resumeTPipe(clientId, trancode, ApiProperties.RESUME_TPIPE_NOAUTO,
			// _retrieveType, FP_TIMEOUT_TIMES,
			// i);
			// // if (this.exit_flag)
			// // break;
			// i++;
			// LogHelperFactory.getGeneralLogger().info(
			// " [MethodName:resumeClearTpipe] Loop last
			// myTmInteraction.getOutputMessage().getImsConnectReturnCode():"
			// + myTmInteraction.getOutputMessage().getImsConnectReturnCode());
			// }
			// } else {
			// long start = System.currentTimeMillis();
			// while ((myTmInteraction.getOutputMessage()
			// .getImsConnectReturnCode() == ApiProperties.IMS_CONNECT_RETURN_CODE_SUCCESS
			// ||
			// myTmInteraction.getOutputMessage().getImsConnectReturnCode() == 40)
			// && StringUtils.isEmpty(this.getMessageFromIMS())) {
			// LogHelperFactory.getGeneralLogger().info(" [MethodName:resumeClearTpipe]
			// While Start ");
			// LogHelperFactory.getGeneralLogger().info(
			// " [MethodName:resumeClearTpipe]
			// myTmInteraction.getOutputMessage().getImsConnectReturnCode(): "
			// + myTmInteraction.getOutputMessage().getImsConnectReturnCode());
			// if (myTmInteraction.isAckNakNeeded()) {
			// myTmInteraction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_ACK);
			// // Execute interaction
			// myTmInteraction.execute();
			// }
			// try {
			// Thread.sleep(1000);
			// } catch (InterruptedException e) {
			// }
			// resumeTPipe(clientId, trancode, ApiProperties.RESUME_TPIPE_NOAUTO,
			// _retrieveType, FP_TIMEOUT_TIMES,
			// i);
			// // if (this.exit_flag)
			// // break;
			// i++;
			// LogHelperFactory.getGeneralLogger().info(
			// " [MethodName:resumeClearTpipe] Loop last
			// myTmInteraction.getOutputMessage().getImsConnectReturnCode():"
			// + myTmInteraction.getOutputMessage().getImsConnectReturnCode());
			// if (System.currentTimeMillis() - start > _timeout) {
			// break;
			// }
			// }
			// }
			// LogHelperFactory.getGeneralLogger().info(" [MethodName:resumeClearTpipe] end
			// ");
			LogHelperFactory.getGeneralLogger().info(" [MethodName:resumeClearTpipe] myTmInteraction.isAckNakNeeded(): "
					+ myTmInteraction.isAckNakNeeded());
			// 0816 NOAUTO need ack after receive success
			if (myTmInteraction.isAckNakNeeded()) {
				String intType;
				myTmInteraction.setImsConnectTimeout(ApiProperties.TIMEOUT_100_MILLISECONDS);
				if ((myTmInteraction.getOutputMessage()
						.getImsConnectReturnCode() == ApiProperties.IMS_CONNECT_RETURN_CODE_SUCCESS)) {
					intType = ApiProperties.INTERACTION_TYPE_DESC_ACK;
				} else {
					intType = ApiProperties.INTERACTION_TYPE_DESC_NAK;
				}
				// myTmInteraction.setInteractionTimeout(timeout +
				// ApiProperties.TIMEOUT_50_MILLISECONDS);
				myTmInteraction.setInteractionTypeDescription(intType);
				// Execute interaction
				myTmInteraction.execute();
				// outMsg = myTmInteraction.getOutputMessage();
			}
			LogHelperFactory.getGeneralLogger().info(" [MethodName:resumeClearTpipe] end ");
		} catch (ImsConnectApiException e) {
			// System.out.println("resumeClearTpipe error:" + e.getMessage());
			LogHelperFactory.getGeneralLogger().error(" [MethodName:resumeClearTpipe] error:" + e.getMessage());
			successful = false;
		} catch (Exception e) {
			// System.out.println("resumeClearTpipe error:" + e.getMessage());
			LogHelperFactory.getGeneralLogger().error(" [MethodName:resumeClearTpipe] error:" + e.getMessage());
			successful = false;
		}
		return successful;
	}

	// 0817 for loop resumetpipe
	private void resumeTPipe(String _ovn, String _trancode, int _resumeType, byte _retrieveType, int timeout)
			throws Exception {
		LogHelperFactory.getGeneralLogger().info(" [MethodName:resumeTPipe] start ");
		LogHelperFactory.getGeneralLogger().info(" [MethodName:resumeTPipe] timeout: " + timeout);

		byte[] emptyByteArray = {};
		// OutputMessage outMsg;
		int connTimeout;
		int interTimeout;
		// switch (_resumeType) {
		// case ApiProperties.RESUME_TPIPE_NOAUTO: {
		// connTimeout = 10;
		// interTimeout = 2000;/* 20221117 */
		// break;
		// }
		// case ApiProperties.RESUME_TPIPE_SINGLE_WAIT:
		// default: {
		// connTimeout = getTimeout(FP_TIMEOUT_TIMES);
		// interTimeout = connTimeout + INT_TIMEOUT_OFFSET;
		// break;
		// }
		// }
		// 當connectionTimeout > interactionTimeout時, 會引發receive Timeout例外
		// 若未超過, 則到connectionTimeout時間未收到資料會getImsConnectReturnCode=40的rc
		connTimeout = timeout;
		interTimeout = timeout + ApiProperties.TIMEOUT_1_SECOND;
		;
		myTmInteraction.setInputMessageDataSegmentsIncludeLlzzAndTrancode(
				ApiProperties.INPUT_MESSAGE_DATA_SEGMENTS_DO_INCLUDE_LLZZ_AND_TRANCODE);
		myTmInteraction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_RESUMETPIPE);
		// myTmInteraction.setImsConnectTimeout(timeout);
		// myTmInteraction.setInteractionTimeout(timeout +
		// ApiProperties.TIMEOUT_5_SECONDS);
		myTmInteraction.setImsConnectTimeout(connTimeout);
		myTmInteraction.setInteractionTimeout(interTimeout);
		myTmInteraction.setResumeTpipeAlternateClientId(_ovn);
		myTmInteraction.setTrancode(_trancode);
		myTmInteraction.setResumeTpipeProcessing(ApiProperties.RESUME_TPIPE_SINGLE_WAIT);
		myTmInteraction.setResumeTpipeRetrievalType(_retrieveType);
		// System.out.println(this.dateFormat.format(new Date()) + ":"+
		// ApiProperties.INTERACTION_TYPE_DESC_RESUMETPIPE + " set Timeout as " +
		// myTmInteraction.getImsConnectTimeout() + "ms");
		myTmInteraction.setAckNakProvider(ApiProperties.CLIENT_ACK_NAK);

		inMsg = (InputMessage) myTmInteraction.getInputMessage();
		inMsg.setInputMessageData(emptyByteArray);
		myTmInteraction.execute();
		// System.out.println(this.dateFormat.format(new Date()) + " :
		// myTmInteraction.getOutputMessage().getImsConnectReturnCode() :
		// "+myTmInteraction.getOutputMessage().getImsConnectReturnCode());
		// outMsg = myTmInteraction.getOutputMessage();
		// String outData = outMsg.getDataAsString();
		output();
		LogHelperFactory.getGeneralLogger().info(" [MethodName:resumeTPipe] ResumeTpipe outData as String:" + outStr);
		LogHelperFactory.getGeneralLogger().info(" [MethodName:resumeTPipe] end ");
		// LogHelperFactory.getGeneralLogger().info("resumeTpipe outData as String:" +
		// this.outStr);
		// System.out.println("outMsg getDataAsString:"+outData);
		// boolean succ = output();

		// this.setMessageFromIMS(outMsg.getDataAsString());
		/* 20221117 */
		// if( do_flag == 0 )
		// System.out.println(this.dateFormat.format(new Date()) + ":1.resumeClearTpipe
		// resumeTpipe outData as String: \n" + outData.replace((char)0x00, ' '));
		// else
		// System.out.println(this.dateFormat.format(new Date()) + ":2.getoutput
		// resumeTpipe outData as String: \n" + outData.replace((char)0x00, ' '));

	}

	public int getTimeout(int times) {
		int result = CONNECT_TIMEOUT * times;
		if (result > 30000) {
			if (result % 60000 > 0) {
				result = ((result / 60000) + 1) * 60000;
			}
		}
		return result;
	}

	//
	public boolean buildConnection(int timeout) {
		LogHelperFactory.getGeneralLogger().info(" [MethodName:buildConnection] start ");
		boolean yes_no = true;
		try {
			if (myConn == null || !myConn.isConnected()) {
				myConn = myCF.getConnection();
				myConn.connect(); // not required with Java API
			}
			myConn.setSocketConnectTimeout(timeout);
			// myConn.setInteractionTimeout(timeout);
		} catch (ImsConnectApiException e) {
			LogHelperFactory.getGeneralLogger().error(e.getMessage());
			yes_no = false;
		} catch (SocketException e) {
			LogHelperFactory.getGeneralLogger().error(e.getMessage());
			yes_no = false;
		}
		LogHelperFactory.getGeneralLogger()
				.info(" [MethodName:buildConnection] create Connection Success ");
		LogHelperFactory.getGeneralLogger().info(" [MethodName:buildConnection] end ");
		return yes_no;
	}

	/**
	 * 十六進制字串轉字節數組
	 *
	 * @param hexString
	 * @return
	 */
	private static final String HEX_STRING = "0123456789ABCDEF";

	public static byte[] hexToBytes(String hexString) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(hexString.length() / 2);
		for (int i = 0; i < hexString.length(); i += 2) {
			baos.write((HEX_STRING.indexOf(hexString.charAt(i)) << 4 | HEX_STRING.indexOf(hexString.charAt(i + 1))));
		}
		return baos.toByteArray();
	}

	/**
	 * 傳訊訊息給IMS並取得回應
	 *
	 * @return
	 */
	// @Override
	// public FEPReturnCode sendReceive1() {
	// FEPReturnCode rtn = CommonReturnCode.Normal;
	// String clientId = null;
	// Connection myConn = null;
	// try {
	// // 先從Pool取出一筆ClientId
	// clientId = this.getClientIdFromPool();
	// // 3.記錄FEPLOG內容
	// this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterIn);
	// this.txData.getLogContext().setMessageFlowType(MessageFlow.Request);
	// this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName,
	// ".sendReceive"));
	// this.txData.getLogContext().setMessage(this.messageToIMS);
	// this.txData.getLogContext()
	// .setRemark(StringUtils.join("Ready Send data to IMS, hostName = [",
	// _hostName, "], hostPort = [",
	// _hostPort, "], clientId = [", clientId, "], timeout = [", _timeout, "],
	// dsName = [",
	// _dsName, "]"));
	// logMessage(this.txData.getLogContext());
	// FEPReturnCode fepReturnCode = this.getCbtestdataResponseToMessageFromIMS();//
	// 從CBSTESTDATA(ByCBSTxid)取出Response賦值給messageFromIMS
	// // // [20221003]新增程式碼
	// if (StringUtils.isNotBlank(this.messageFromIMS) || (fepReturnCode !=
	// FEPReturnCode.Normal)) {
	// return fepReturnCode;
	// }
	// // 4.建立IMS ConnectionFactory物件
	// ConnectionFactory myCF = new ConnectionFactory();
	// myCF.setHostName(_hostName);
	// myCF.setPortNumber(_hostPort);
	// myCF.setClientId(clientId);
	// // 5.建立IMS Connection物件
	// // 分別設定連線逾時及傳輸逾時屬性
	// myConn = myCF.getConnection();
	// myConn.connect();
	// myConn.setSocketConnectTimeout(5000); // 連線逾時,單位毫秒
	// myConn.setInteractionTimeout(_timeout - 5000); // 傳輸逾時,單位毫秒
	// // 6.建立IMS Interaction物件
	// TmInteraction myInteraction = myConn.createInteraction();
	// myInteraction.setLtermOverrideName(clientId);
	// myInteraction.setImsDatastoreName(_dsName);
	// myInteraction.setTrancode(trancode);
	// myInteraction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_SENDRECV);
	// // 同步模式
	// myInteraction.setCommitMode(ApiProperties.COMMIT_MODE_1);
	// // 7.建立InputMessage物件準備傳送資料
	// InputMessage inMsg = myInteraction.getInputMessage();
	// byte[] inputData = messageToIMS.getBytes();
	// inMsg.setInputMessageData(inputData);
	// // 8.傳送至IMS
	// myInteraction.execute();
	// // 9.接收IMS Response
	// OutputMessage outMsg = myInteraction.getOutputMessage();
	// this.messageFromIMS = outMsg.getDataAsString();
	// } catch (Exception e) {
	// rtn = handleException(this.txData.getLogContext(), e, "sendReceive");
	// } finally {
	// // 10.與IMS斷線
	// if (myConn != null) {
	// try {
	// myConn.disconnect();
	// } catch (ImsConnectApiException e) {
	// LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
	// }
	// }
	// // 斷線後歸還clientId至Pool
	// if (clientId != null) {
	// this.putClientIdToPool(clientId);
	// }
	// // 11.記錄FEPLOG內容
	// this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterOut);
	// this.txData.getLogContext().setMessageFlowType(MessageFlow.Response);
	// this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName,
	// ".sendReceive"));
	// this.txData.getLogContext().setMessage(this.messageFromIMS);
	// this.txData.getLogContext()
	// .setRemark(StringUtils.join("Get data from IMS, hostName = [", _hostName, "],
	// hostPort = [",
	// _hostPort, "], clientId = [", clientId, "], timeout = [", _timeout, "],
	// dsName = [",
	// _dsName, "]"));
	// logMessage(this.txData.getLogContext());
	// }
	// // 12.回傳ReturnCode
	// return rtn;
	// }

	/**
	 * 取得送給IMS的訊息
	 *
	 * @return
	 */
	public String getMessageToIMS() {
		return messageToIMS;
	}

	/**
	 * 設定送給IMS的訊息
	 *
	 * @param messageToIMS
	 */
	public void setMessageToIMS(String messageToIMS) {
		this.messageToIMS = messageToIMS;
	}

	/**
	 * 取得IMS回應訊息
	 *
	 * @return
	 */
	public String getMessageFromIMS() {
		return messageFromIMS;
	}

	/**
	 * 設定IMS回應訊息
	 *
	 * @param messageFromIMS
	 */
	public void setMessageFromIMS(String messageFromIMS) {
		this.messageFromIMS = messageFromIMS;
	}

	/**
	 * 取得CBSTxid // [20221003]新增程式碼
	 *
	 * @return
	 */
	public String getCBSTxid() {
		return CBSTxid;
	}

	/**
	 * 設定CBSTxid // [20221003]新增程式碼
	 *
	 * @param CBSTxid
	 */
	public void setCBSTxid(String CBSTxid) {
		this.CBSTxid = CBSTxid;
	}

	/**
	 * 交易的TranCode
	 *
	 * @param trancode
	 */
	public void setIMSTrancode(String trancode) {
		this.trancode = trancode;
	}

	/**
	 * 拆clientId
	 *
	 * @param clientId
	 * @return rtnList
	 */
	private static List<String> parseClientId(String clientId) { // [20221005]新增方法: parseClientId
		List<String> rtnList = new ArrayList<String>();
		String[] clinetIdStrArr = clientId.split(";");
		for (String clinetIdStr : clinetIdStrArr) {
			String[] clinetIdDetailArr = clinetIdStr.split(",");
			String pattern = clinetIdDetailArr[0]; // Pattern
			int min = Integer.parseInt(clinetIdDetailArr[1]); // 最小值
			int max = Integer.parseInt(clinetIdDetailArr[2]); // 最大值
			for (int i = min; i <= max; i++) {
				String value = String.format(pattern.replace("{0}", "%s"),
						StringUtils.leftPad(Objects.toString(i), 2, "0"));
				rtnList.add(value);
			}
		}
		return rtnList;
	}

	/**
	 * 從CBSTESTDATA(ByCBSTxid)取出Response賦值給messageFromIMS
	 *
	 * @return
	 */
	private FEPReturnCode getCbtestdataResponseToMessageFromIMS() {
		if (StringUtils.isNotBlank(CBSTxid) && "Y".equals(_isCBSTest)) { // [20221003]新增"Y".equals(_isCBSTest)
			// Cbstestdata cbstestdata = cbstestdataExtMapper.selectByPrimaryKey(CBSTxid);
			// if (cbstestdata == null) {
			// // 查無資料時，寫出誤訊息至log，且丟出例外
			// this.logContext.setRemark("CBSTESTDATA not found");
			// this.txData.setLogContext(logContext);
			// return handleException(this.txData.getLogContext(), null, "sendReceive");
			// } else {
			// this.messageFromIMS = cbstestdata.getResponse();
			// return FEPReturnCode.Normal;
			// }
			// 以下for壓測
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {

			}
			switch (this.getCBSTxid()) {
				// case "FAA-I1": // 晶片卡餘額查詢第1道
				// this.messageFromIMS =
				// "F9F9F7F6F140F140C6D7D5F2F3F0F7F2F4F1F0F5F8F4F8F4F9F2F6F0F1F4C4F0F0F2F4F0F0F3F040404040405BF76BF7F9F56BF1F9F04BF0F05E7BE2F0F1F9F0F0F0F0F0F1F0F1F1F261F0F761F2F45E61F1F07AF5F97AF0F25E61F5F95E61E3F9F9F9F7E2F0F15E614040404040404040405BF05E6140405BF05E6140404040405BF76BF7F9F56BF1F9F04BF0F05E61F0F0F0F0F0F0F0F7F15C5C5CF7F5F0F05E61404040404040404040404040404040405E61F0F0F65E61F1F1F0F3F5F0F65E6140404040404040405E61F4F0F0F15E6140405E61F0F1F75E614040405E6140404040404040404040405E61404040404040404040404040404040405E7BF0F2C2F6F8F3C5C6";
				// break;
				case "FAA-F2": // 晶片卡提款第1道
					this.messageFromIMS = "F9F9F7F9F140F140C6D7D5F2F3F0F8F2F8F1F4F5F4F2F4F9F5F2F5F0F1F4E2F0F1F9F0F0F0F0F0F1F0F1F1F261F0F861F2F85E61F1F47AF5F57AF0F65E61F5F15E61E3F9F9F9F7E7F0F15E6140404040405BF16BF0F0F05E6140405BF55E6140404040405BF26BF8F6F66BF4F7F44BF0F05E61F0F0F0F0F0F9F3F2F15C5C5CF3F1F0F85E61404040404040404040404040404040405E61F0F0F65E61C1F0F0F0F1C2C35E6140404040404040405E61F4F0F0F15E6140405E61F0F1F65E614040405E6140404040404040404040405E61404040404040404040404040404040405E7BF9C1F2F1F1C2F4F1";
					break;
				case "FSN-F2": // 晶片卡提款第3道
					this.messageFromIMS = "F9F9F7F9F140F040C6D7C3F2F3F0F8F2F8F1F4F5F4F5F0F9F5F2F5F0F2F0F4F1F2F5C6C4F9F1";
					break;
				case "FAA-TA": // 晶片卡轉帳第1道
					this.messageFromIMS = "F9F9F7F9F140F140C6D7D5F2F3F0F8F2F8F1F5F1F5F4F9F9F5F4F4F0F1F4E2F0F1F9F0F0F0F0F0F1F0F1F1F261F0F861F2F85E61F1F57AF1F67AF3F15E61F5F35E61E3F9F9F9F7E7F0F15E614040404040404040405BF15E61405BF1F05E6140404040405BF26BF8F2F86BF3F3F74BF0F05E61F0F0F0F0F0F9F3F2F15C5C5CF3F1F0F85E61F0F0F0F0F0F9F3F2F1F0F0F0F5F0F9F75E61F0F0F65E61C1F0F0F0F1C3C15E61F1F261F0F861F2F85E61F4F0F0F15E6140405E61F0F1F65E61F0F1F65E6140404040404040404040405E61404040404040404040404040404040405E7BC1F8F8C3C1C5C5F5";
					break;
				case "FSN-TA": // 晶片卡轉帳第3道
					this.messageFromIMS = "F9F9F7F9F140F040C6D7C3F2F3F0F8F2F8F1F5F1F6F0F9F9F5F4F4F0F2F0C3C5F3F3F5F6F2F2";
					break;
			}
			if (StringUtils.isNotEmpty(_wsid) && StringUtils.isNotEmpty(_atmSeq)) {
				// String wsid = this.messageFromIMS.substring(0, 10);
				// String atmSeq = this.messageFromIMS.substring(46, 54);
				// 換WSID與atmSeq
				this.messageFromIMS = _wsid + this.messageFromIMS.substring(10);
				this.messageFromIMS = this.messageFromIMS.substring(0, 46) + _atmSeq +
						this.messageFromIMS.substring(54);
			}
		}
		return FEPReturnCode.Normal;
	}

	/**
	 * 從Pool中取得clientId
	 * <p>
	 * 若_timeout都取不到clientId直接throw ProgramException(“Get ClientId Fail”)
	 *
	 * @return
	 */
	private String getClientIdFromPool(boolean isPBO) throws Exception {
		try {
			if (isPBO)
				return _clientIdsPooled_pbo.borrowObject();
			else {
				if (this.CBSTxid.equals("ChangeKey"))
					return _clientIdsPooled_ck.borrowObject();
				else
					return _clientIdsPooled_tx.borrowObject();
			}

		} catch (Exception e) {
			throw ExceptionUtil.createException(e, "Get ClientId Fail");
		}
	}

	/**
	 * 將傳入的clientId放回Pool中
	 *
	 * @param clientId
	 */
	private void putClientIdToPool(boolean isPBO, String clientId) {
		if (isPBO)
			_clientIdsPooled_pbo.returnObject(clientId);
		else {
			if (this.CBSTxid.equals("ChangeKey"))
				_clientIdsPooled_ck.returnObject(clientId);
			else
				_clientIdsPooled_tx.returnObject(clientId);
		}

	}
}
