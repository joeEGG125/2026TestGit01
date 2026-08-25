package com.syscom.fep.gateway.netty.fisc.client.sender;

import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
//import com.syscom.fep.gateway.entity.SendType;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayUtil;
import com.syscom.fep.gateway.netty.fisc.client.FISCGatewayClientProcessRequest;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;

public class FISCGatewayClientSenderProcessRequest extends FISCGatewayClientProcessRequest<FISCGatewayClientSenderConfiguration> {
	
	@Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {
        super.connStateChanged(channel, state, t);
        // 當連線成功後,如果是Recv Socket則發出Resume Tpipe電文
        if (state == NettyTransmissionConnState.CLIENT_CONNECTED) {
			//Sender連線成功後第一道交易電文加Cancel ClientID flg
            try {
                if (!clientIdRepeat) {
                    clientIdRepeat = true;
                    //this.send(channel, this.logContext, SendType.CR);
                
                }
            } catch (Exception e) {
                this.logContext.setProgramException(e);
                this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
                sendEMS(this.logContext);
            }
        } 
        
    }	
	
	/**
	 * Sender Socket接收資料流程
	 * 
	 * @param ctx
	 * @param logData
	 * @param message
	 * @throws Exception
	 */
	@Override
	protected void doProcess(ChannelHandlerContext ctx, LogData logData, String message) {
		if (message.length() == 40 && RSM_ID.equals(message.substring(8, 16 + 8))) {
			logData.setStan(StringUtils.EMPTY);
			logData.setRemark(StringUtils.join(
					"[", configuration.getSocketType(),
					" IP:", configuration.getHost(),
					",Port:", configuration.getPort(), "] Recv RSM Message"));
			logData.setMessage(message);
			logData.setProgramFlowType(ProgramFlow.FCSGWIn);
			logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
			sendEMS(logData);
			// 記錄內容至檔名為fep-gateway-fisc-FISCGW-RSM-yyyy-MM-dd
			FISCGatewayUtil.logRSMMessage(message);
			// 980214 FISC新規定
			// 當參加單位端之Sender收到Duplicate ClientID之RSM(08/38)後，財金公司端會主動斷線。
			switch (message.substring(30, 2 + 30)) {
				// 判斷RSM電文的Return code欄位
				case "08":
					// ClientID重覆
					if ("38".equals(message.substring(38, 2 + 38))) {
						logData.setStan(StringUtils.EMPTY);
						logData.setRemark(StringUtils.join(
								"[", configuration.getSocketType(),
								" IP:", configuration.getHost(),
								",Port:", configuration.getPort(), "] ClientID重覆"));
						logData.setMessage(message);
						logData.setProgramFlowType(ProgramFlow.FCSGWIn);
						logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
						sendEMS(logData);
						logMessage(logData);
						clientIdRepeat = true;
					}
					break;
			}
		}
	}
}
