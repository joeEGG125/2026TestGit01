package com.syscom.fep.server.common.cbsprocess;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Objects;

import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.vo.text.ims.CB_APIDSTAT_I001;
import com.syscom.fep.vo.text.ims.CB_APIDSTAT_O001;
import com.syscom.fep.vo.text.ims.IB_WD_TR_O001;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 組送CBS主機同步3106/3107系統狀態
 * 
 * 
 */
public class CBAPIDSTAT extends ACBSAction {

	public CBAPIDSTAT(MessageBase txData) {
		super(txData, new CB_APIDSTAT_I001());
	}
	
	/**
	 * 組CBS TITA電文
	 * 
	 * @param txType
	 * @return
	 * @throws Exception
	 */
    @Override
    public FEPReturnCode getCbsTita(String txType) throws Exception {
    	CB_APIDSTAT_I001 cbstita = new CB_APIDSTAT_I001();
    	cbstita.setIMS_TRANS("IBPBM000");
    	if(StringUtils.equals("3106", feptxn.getFeptxnPcode())) {
    		cbstita.setAPIDTYPE("26");
    	}else if(StringUtils.equals("3107", feptxn.getFeptxnPcode())) {
    		cbstita.setAPIDTYPE("27");
    	}
		cbstita.setSYSCODE("FEP");
		cbstita.setMSGTYPE("F");
		cbstita.setPCODE(feptxn.getFeptxnPcode());
		cbstita.setTXNSTAN(feptxn.getFeptxnBkno()+feptxn.getFeptxnStan());
		//格式:YYYYMMDDHHMMSS西元年月日
		cbstita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		//應用系統代號
		cbstita.setAPID(StringUtils.trim(feptxn.getFeptxnApid()));
		cbstita.setFEP_EJNO(StringUtils.leftPad(Objects.toString(feptxn.getFeptxnEjfno()), 8, "0"));
		
		this.setoTita(cbstita);
		this.setTitaToString(cbstita.makeMessage());
		this.setASCIItitaToString(cbstita.makeMessageAscii());
		return FEPReturnCode.Normal;
    }

	/**
	 * 拆解CBS TOTA電文
	 *
	 * @param cbsTota
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@Override
	public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
		/* 電文內容格式請參照TOTA電文格式(CB_APIDSTAT_O001) */
		/* 拆解主機回應電文 */
		CB_APIDSTAT_O001 tota = new CB_APIDSTAT_O001();
		tota.parseCbsTele(cbsTota);
		this.setTota(tota);

		/* 更新FEPTXN */
		FEPReturnCode rtnCode = this.updateFEPTxn(tota, type);

		/* 回覆FEP */
		// 處理 CBS 回應
		return rtnCode;
	}

	/**
	 * 更新FEPTXN
	 *
	 * @param cbsTota
	 * @param type
	 * @return
	 * @throws Exception
	 */
	private FEPReturnCode updateFEPTxn(CB_APIDSTAT_O001 cbsTota, String type) throws Exception {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 1, 1) + "2");
		feptxn.setFeptxnCbsTimeout((short) 0);
		// IMSRC_TCB = "000" or empty表交易成功

		/* 變更FEPTXN交易記錄 */
		if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
			cbsTota.setIMSRC_TCB("000");
		}
		if ( !("000").equals(cbsTota.getIMSRC_TCB())
				&& !(("4001").equals(cbsTota.getIMSRC4_FISC()) ||
				("4002").equals(cbsTota.getIMSRC4_FISC()) ||
				("4007").equals(cbsTota.getIMSRC4_FISC() )) ){
			rtnCode = FEPReturnCode.CBSCheckError;
		}
		// 主機回應代碼：三碼為空(更新為000)或跨行交易給四碼，否則給三碼
		if(cbsTota.getIMSRC_TCB().equals("000")) {
			feptxn.setFeptxnCbsRc(cbsTota.getIMSRC4_FISC());
		}else {
			feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
		}

		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			int insertCount = this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
			if (insertCount <= 0) {
				rtnCode = FEPReturnCode.FEPTXNUpdateError;
				transactionManager.rollback(txStatus);
			}else {
				transactionManager.commit(txStatus);
			}

		} catch (Exception ex) { // 新增失敗
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateFEPTxn");
			sendEMS(getLogContext());
			rtnCode = FEPReturnCode.FEPTXNUpdateError;
		}
		return rtnCode;
	}

    //@Override
    //public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
		//return null;
	//}
}
