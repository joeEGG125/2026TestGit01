package com.syscom.fep.server.common.adapter;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import org.apache.commons.lang3.StringUtils;

//@SuppressWarnings("unused")
public class WMQConnection extends FEPBase{
//	private String _ServerIP;
//    private int _ServerPort;
//    private String _ChannelName;
	public WMQConnection(String serverIP, int serverPort, String channelName)
	{

		if (StringUtils.isBlank(serverIP))
		{
			throw ExceptionUtil.createRuntimeException("WMQ Server IP is NULL.");
		}

		if (serverPort <= 0)
		{
			throw ExceptionUtil.createRuntimeException("WMQ Server Port is illegel");
		}

		if (StringUtils.isBlank(channelName))
		{
			throw ExceptionUtil.createRuntimeException("WMQ Channel Name is NULL.");
		}
//		_ChannelName = channelName.trim();
//		_ServerIP = serverIP.trim();
//		_ServerPort = serverPort;

		// TODO
//		MQEnvironment.Hostname = _ServerIP;
//		MQEnvironment.Port = _ServerPort;
//		MQEnvironment.Channel = _ChannelName;
	}

}
