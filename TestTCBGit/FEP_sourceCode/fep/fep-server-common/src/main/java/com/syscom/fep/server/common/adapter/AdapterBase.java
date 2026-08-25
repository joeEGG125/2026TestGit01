package com.syscom.fep.server.common.adapter;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;

/**
 * Adapter元件的基礎類別
 *
 * @author Richard
 *
 */
public abstract class AdapterBase extends FEPBase {
	private FEPChannel channel;
	private SubSystem FEPSubSystem;
	protected int timeout;
	private boolean noWait;

	public abstract FEPReturnCode sendReceive();

	public FEPChannel getChannel() {
		return channel;
	}

	public void setChannel(FEPChannel channel) {
		this.channel = channel;
	}

	public SubSystem getFEPSubSystem() {
		return FEPSubSystem;
	}

	public void setFEPSubSystem(SubSystem fepSubSystem) {
		this.FEPSubSystem = fepSubSystem;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean isNoWait() {
		return noWait;
	}

	public void setNoWait(boolean noWait) {
		this.noWait = noWait;
	}

	protected FEPReturnCode handleException(LogData logData, Exception e, String methodName) {
		logData.setProgramName(StringUtils.join(this.ProgramName, ".", methodName));
		logData.setProgramException(e);
		sendEMS(logData);
		return FEPReturnCode.ProgramException;
	}

    public void startBatch(int sleepInMillisecondsAfterStart) throws Exception {
        this.startBatch(StringUtils.EMPTY, sleepInMillisecondsAfterStart);
    }

    public void startBatch(String customParameters, int sleepInMillisecondsAfterStart) throws Exception {
//        if (sleepInMillisecondsAfterStart > 0) {
//            try {
//                Thread.sleep(sleepInMillisecondsAfterStart);
//            } catch (InterruptedException e) {}
//        }
    }
}
