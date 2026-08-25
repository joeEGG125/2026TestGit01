package com.syscom.fep.server.common.business.rm;

import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.aa.T24Data;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.RmstatExtMapper;
import com.syscom.fep.mybatis.model.Rmstat;
import com.syscom.fep.vo.enums.IOReturnCode;

public class RMCheck extends RMHost {

	protected RMCheck() {
		super();
	}

	protected RMCheck(RMData rmTxMsg) {
		super(rmTxMsg);
	}

	protected RMCheck(T24Data t24Msg) {
		super(t24Msg);
	}

	public FEPReturnCode checkRmstatRmFlag() {
		Rmstat defRMSTAT = new Rmstat();
		RmstatExtMapper dbRMSTAT = SpringBeanFactoryUtil.getBean(RmstatExtMapper.class);
		try {
			defRMSTAT.setRmstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
			defRMSTAT = dbRMSTAT.selectByPrimaryKey(defRMSTAT.getRmstatHbkno());
			if (defRMSTAT != null) {
				if (defRMSTAT.getRmstatRmFlag().equalsIgnoreCase("N")) {
					return CommonReturnCode.ChannelServiceStop;
				}
			} else {
				return IOReturnCode.RMSTATNotFound;
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setRemark("CheckRMSTAT_RM_FLAG發生例外");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}
}
