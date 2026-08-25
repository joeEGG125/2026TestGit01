package com.syscom.fep.server.common.business.host;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.MsgctlMapper;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.server.common.adapter.UnisysAdapter;
import com.syscom.fep.server.common.business.BusinessBase;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.enums.*;

public class HostBase extends BusinessBase{

	public HostBase(MessageBase txData) {
		super(txData);
	}

	/**
	 取得送主機的TXID
	 TMO因為取得原交易，所以MSGCTL要重新QUERY

	 @return

	 <history>
	 <modify>
	 <modifier>Kyo</modifier>
	 <reason>Function Modify</reason>
	 <date>2009/11/27</date>
	 </modify>
	 <modify>
	 <modifier>Kyo</modifier>
	 <reason>移除沒使用的參數</reason>
	 <date>2010/04/12</date>
	 </modify>
	 <modify>
	 <modifier>Kyo</modifier>
	 <reason>BugReport(001B0475):台灣ATM+澳門卡查詢(IAC/IIQ)：在IAC有如下錯誤發生例外</reason>
	 <date>2010/05/18</date>
	 </modify>
	 <modify>
	 <modifier>Kyo</modifier>
	 <reason>BugReport(001B0492):台灣ATM+海外卡(跨區提款)，發生「Syscom.FEP10.Common.Business.ATM.GetTxidGetTxid Error」錯誤</reason>
	 <date>2010/05/19</date>
	 </modify>
	 </history>
	 */
	public String getTxid(Feptxn oFEPTXN, Msgctl oMSGCTL) {
		String wTxid = "";
		try {
			//BugReport(001B0475):修改程式與法 避開 取子字串時發生的例外
			if (oFEPTXN.getFeptxnTroutActno().length() >= 8) {
				switch (oFEPTXN.getFeptxnTroutActno().substring(5, 8)) {
					case "001":
					case "003":
					case "004":
						break;
					case "031":
					case "033":
						break;
					case "008":
						break;
					default:
						throw (ExceptionUtil.createException("GetTxid Error"));
				}
			} else {
				throw (ExceptionUtil.createException("GetTxid Error"));
			}

			//因為TMO會取得原交易，會造成adata.msgctl中的值錯誤，故另外判斷
			//BugReport(001B0475):修改程式與法 避開 取子字串時發生的例外
			//BugReport(001B0492):coding error 應該改為>=6才對
			if (ATMTXCD.TMO.name().equals(oMSGCTL.getMsgctlMsgid())) {
				Msgctl defMsgCtl = new Msgctl();
				MsgctlMapper dbMsgCtl = SpringBeanFactoryUtil.getBean(MsgctlMapper.class);
				defMsgCtl.setMsgctlMsgid(oFEPTXN.getFeptxnTxCode());
				defMsgCtl = dbMsgCtl.selectByPrimaryKey(defMsgCtl.getMsgctlMsgid());
				if ( defMsgCtl == null) {
					throw ExceptionUtil.createException("GetTxid Error");
				}
			}

			return wTxid;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}

	}

	/**
	 利用Adapter送主機，並做FEPTXN的更新

	 @return

	 <history>
	 <modify>
	 <modifier>Jim</modifier>
	 <reason>ATM Business</reason>
	 <date>2010/2/25</date>
	 </modify>
	 <modify>
	 <modifier>Kyo</modifier>
	 <reason>修改為與SPEC一致</reason>
	 <date>2010/6/22</date>
	 </modify>
	 </history>
	 */
	public FEPReturnCode sendToHostByAdapter(UnisysTXCD unysisTxcd, byte txType, UnisysAdapter adapter, String tita, UnisysType hostType) {

		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		//將組好的主機TITA電文送往香港優利主機(HKCSF3)
		adapter.setChannel(getGeneralData().getTxChannel());  //交易發動來源
		adapter.setTimeout(CMNConfig.getInstance().getUnisysTimeout()); //等待主機時間上限
		adapter.setTxId(getFeptxn().getFeptxnTxCode()); //交易代碼
		adapter.setFEPSubSystem(getGeneralData().getTxSubSystem());
		adapter.setMessageToUnisys(tita);
		adapter.setEj(getEj());
		adapter.setHostType(hostType);

		//2010-06-22 by kyo for 修改為與SPEC一致
		if (txType == (byte)CBSTxType.Accounting.getValue()) {
			if (unysisTxcd == UnisysTXCD.J00001 || unysisTxcd == UnisysTXCD.K00001 || unysisTxcd == UnisysTXCD.G08002) {//查詢及調匯率
				getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_INQ_Request); //查詢類交易
			} else {
				getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_Request);  //轉出類交易
			}
		}
		else if (txType == 0) {//沖正
			getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_EC_Request); //X1
		}

		getFeptxn().setFeptxnCbsTimeout(DbHelper.toShort(true));
		try {
			if (this.feptxnDao.updateByPrimaryKeySelective(getFeptxn()) <= 0) {
				return IOReturnCode.FEPTXNUpdateError; //L013
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		rtnCode = adapter.sendReceive();
		return rtnCode;
	}

}
