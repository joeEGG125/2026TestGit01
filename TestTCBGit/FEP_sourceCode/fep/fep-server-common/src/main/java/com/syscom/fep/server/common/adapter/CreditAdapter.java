package com.syscom.fep.server.common.adapter;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.configuration.CMNConfig;

@SuppressWarnings("unused")
public class CreditAdapter extends AdapterBase {
	private MessageBase _txData;
	private static final String ProgramName = "CreditAdapter";
	private String privateMessageFromSC;

	public final String getMessageFromSC() {
		return privateMessageFromSC;
	}

	public final void setMessageFromSC(String value) {
		privateMessageFromSC = value;
	}

	private String privateMessageToSC;

	public final String getMessageToSC() {
		return privateMessageToSC;
	}

	public final void setMessageToSC(String value) {
		privateMessageToSC = value;
	}

	private String _mqServerIP = CMNConfig.getInstance().getCreditMQServerIP();
	private int _mqServerPort = CMNConfig.getInstance().getCreditMQServerPort();
	private String _mqChannelName = CMNConfig.getInstance().getCreditMQServerChannel();
	private String _mqQueueManagerName = CMNConfig.getInstance().getCreditMQServerQueueMgr();
	// 2014-08-18 Modify by Ruling for MQ Expiry 時間(單位1/10秒)設定在SYSCONF內
	private int _mqExpiry = CMNConfig.getInstance().getCreditMQExpiry();
	private String _outputQueueName; // = CMNConfig.Instance().CreditMQServerPutQueue07;
	private String _inputQueueName = CMNConfig.getInstance().getCreditMQServerResponseQueue();
	// 2013/04/22 Modify by Ruling for GiftCombo:add B21 Queue
	// 2015/02/17 Modify by Ruling for IPIN:add B23 Queue
	private static java.util.HashMap<String, String> _outputQueue = new java.util.HashMap<String, String>();
	static {
		_outputQueue.put("B01", CMNConfig.getInstance().getCreditMQServerPutQueue01());
		_outputQueue.put("B02", CMNConfig.getInstance().getCreditMQServerPutQueue02());
		_outputQueue.put("B03", CMNConfig.getInstance().getCreditMQServerPutQueue03());
		_outputQueue.put("B05", CMNConfig.getInstance().getCreditMQServerPutQueue05());
		_outputQueue.put("B06", CMNConfig.getInstance().getCreditMQServerPutQueue06());
		_outputQueue.put("B07", CMNConfig.getInstance().getCreditMQServerPutQueue07());
		_outputQueue.put("B09", CMNConfig.getInstance().getCreditMQServerPutQueue09());
		_outputQueue.put("B10", CMNConfig.getInstance().getCreditMQServerPutQueue10());
		_outputQueue.put("B11", CMNConfig.getInstance().getCreditMQServerPutQueue11());
		_outputQueue.put("B15", CMNConfig.getInstance().getCreditMQServerPutQueue15());
		_outputQueue.put("B16", CMNConfig.getInstance().getCreditMQServerPutQueue16());
		_outputQueue.put("B17", CMNConfig.getInstance().getCreditMQServerPutQueue17());
		_outputQueue.put("B20", CMNConfig.getInstance().getCreditMQServerPutQueue20());
		_outputQueue.put("B21", CMNConfig.getInstance().getCreditMQServerPutQueue21());
		_outputQueue.put("B23", CMNConfig.getInstance().getCreditMQServerPutQueue23());
	}
	private WMQConnection _mqCon;
	private int _scTimeout = CMNConfig.getInstance().getCreditTimeout() * 1000;

	public CreditAdapter(MessageBase txData) {
		_txData = txData;
		_mqCon = new WMQConnection(_mqServerIP, _mqServerPort, _mqChannelName);

	}

	@Override
	public FEPReturnCode sendReceive() {
		// TODO
		return FEPReturnCode.Normal;
	}
}
