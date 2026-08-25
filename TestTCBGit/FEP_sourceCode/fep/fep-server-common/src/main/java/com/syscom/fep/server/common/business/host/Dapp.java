package com.syscom.fep.server.common.business.host;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
//import com.syscom.fep.mybatis.mapper.SmlparmMapper;
//import com.syscom.fep.mybatis.model.Smlparm;
import com.syscom.fep.server.common.adapter.NBAdapter;
import com.syscom.fep.vo.enums.DAPPAppMsg;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.app.APPGeneral;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

public class Dapp extends HostBase {
	private MessageBase _txData = null;

	public Dapp(MessageBase txData) {
		super(txData);
		_txData = txData;
	}

	public FEPReturnCode sendToDAPP(int action) {
		FEPReturnCode rtn = FEPReturnCode.Normal;
		@SuppressWarnings("unused")
		String rtnData = StringUtils.EMPTY;
		NBAdapter APPAdapter = new NBAdapter(_txData);
		@SuppressWarnings("unused")
		APPGeneral APPGeneralForReq = new APPGeneral();
		APPAdapter.setAction("DAPP");
		try {
// 2024-03-06 Richard modified for SYSSTATE 調整
//			if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatNb())) {// 暫停永豐網銀通道
//				if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
//					if (StringUtils.isBlank(getFeptxn().getFeptxnTxCode())) {// 原存行交易
//						getFeptxn().setFeptxnAscRc(String.valueOf(FEPReturnCode.InterBankServiceStop.getValue()));
//					} else {// 代理行交易
//						getFeptxn().setFeptxnAscRc(String.valueOf(FEPReturnCode.SenderBankServiceStop.getValue()));
//					}
//				} else {
//					getFeptxn().setFeptxnAscRc(String.valueOf(FEPReturnCode.WithdrawServiceStop.getValue()));
//				}
//				getLogContext().setRemark("暫停永豐網銀通道");
//				logMessage(Level.DEBUG, getLogContext());
//				this.feptxnDao.updateByPrimaryKeySelective(getFeptxn());
//				return FEPReturnCode.Normal;
//			}

//			SmlparmMapper dbSML = SpringBeanFactoryUtil.getBean(SmlparmMapper.class);
//			Smlparm defSML = new Smlparm();
//			defSML.setSmlparmType("N");
//			defSML.setSmlparmSeqno(action);
//			defSML = dbSML.selectByPrimaryKey(defSML.getSmlparmType(), defSML.getSmlparmSeqno());
//			if (defSML == null) {
//				getLogContext().setRemark("傳入值ACTION" + action + ", 查無推播內容");
//				logMessage(Level.DEBUG, getLogContext());
//				return FEPReturnCode.Normal;
//			}
//			StringBuffer date = new StringBuffer(getFeptxn().getFeptxnTxDate());
//			StringBuffer time = new StringBuffer(getFeptxn().getFeptxnTxTime());
//			String ds = date.insert(6, "/").insert(4, "/") + StringUtils.SPACE + time.insert(4, ":").insert(2, ":");
//			String text = StringUtils.EMPTY;
//			String custid = StringUtils.EMPTY;
//			String type = StringUtils.EMPTY;
//			String option_url = StringUtils.EMPTY;
//			if (action == DAPPAppMsg.Start.getValue() || action == DAPPAppMsg.ReStart.getValue() || action == DAPPAppMsg.Close.getValue() || action == DAPPAppMsg.SSCodeErrorLimit.getValue()) {
//				text = defSML.getSmlparmContent().replace("[PARM1]", ds);
//				custid = getFeptxn().getFeptxnIdno();
//				type = "5";
//				option_url = "sinopacaction:{cardless}{}";
//			} else if (action == DAPPAppMsg.Complete.getValue()) {
//				text = defSML.getSmlparmContent()
//						.replace("[PARM1]", ds)
//						.replace("[PARM2]", getFeptxn().getFeptxnTxCur())
//						.replace("[PARM3]", FormatUtil.doubleFormat(getFeptxn().getFeptxnTxAmt().intValue(), "#,##0"));
//				custid = getFeptxn().getFeptxnIdno();
//				type = "2";
//				option_url = "sinopacaction:{cardless}{}";
//			}
//			APPAdapter.setMessageToNB("text=" + text + "&custid=" + custid + "&type=" + type + "&option_url=" + option_url);

			getFeptxn().setFeptxnAscTimeout(DbHelper.toShort(true));/// * CBS 逾時 FLAG */
			if (this.feptxnDao.updateByPrimaryKeySelective(getFeptxn()) <= 0) {// RC = L013
				return IOReturnCode.FEPTXNUpdateError;
			}
			rtn = APPAdapter.sendReceive();

			if (rtn == FEPReturnCode.Normal) {
				getFeptxn().setFeptxnAscTimeout(DbHelper.toShort(false)); /// * CBS 逾時 FLAG */
			}

			if (!StringUtils.isBlank(APPAdapter.getMessageFromNB())) {
				if (APPAdapter.getMessageFromNB().contains("SUCCESS")) {
					getFeptxn().setFeptxnAscRc("0000");
				}
			}

			this.feptxnDao.updateByPrimaryKeySelective(getFeptxn());
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return FEPReturnCode.Normal;
		}
		return FEPReturnCode.Normal;
	}
}
