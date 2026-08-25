package com.syscom.fep.server.common.cbsprocess;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.ims.CB_VA_I001;
import com.syscom.fep.vo.text.ims.CB_VA_O001;

public class CBVAI001 extends ACBSAction {

    public CBVAI001(MessageBase txData) {
        super(txData, new CB_VA_O001());
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
        /* TITA 請參考合庫主機電文規格CB_VA_I001) */
    	// Header
        CB_VA_I001 cbstita = new CB_VA_I001();
        ATMGeneralRequest atmReq = this.getAtmRequest();
        cbstita.setIMS_TRANS("MFEPAT00");
		cbstita.setSYSCODE("FEP");
		cbstita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
		//TXN_FLOW
		cbstita.setTXN_FLOW("C"); // 自行
		cbstita.setMSG_CAT(atmReq.getMSGTYP());
		cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
		cbstita.setPCODE(feptxn.getFeptxnPcode());
		cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
		cbstita.setPROCESS_TYPE("CHK");
		// 財金營業日(西元年須轉民國年)
		String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
		if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
			feptxnTbsdyFisc = "000000";
		} else {
			feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
		}
		cbstita.setBUSINESS_DATE(feptxnTbsdyFisc);
		cbstita.setACQUIRER_BANK(feptxn.getFeptxnBkno());
		cbstita.setTXNSTAN(feptxn.getFeptxnStan());
		cbstita.setTERMINALID(feptxn.getFeptxnAtmno());
		cbstita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType());
		cbstita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno());
		//晶片卡
		cbstita.setCARDTYPE("K"); 
		cbstita.setRESPONSE_CODE("0000");
		// ATM交易序號
		cbstita.setATMTRANSEQ(atmReq.getTRANSEQ()); 
		// Detail
		cbstita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
		cbstita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
		cbstita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
		//IC卡備註欄(未轉ASC，為原始電文)
		cbstita.setICMEMO(atmReq.getPICCBI55());
		//TAC(未轉ASC，為原始電文值)
		cbstita.setTXNICCTAC(atmReq.getPICCTAC());
		//卡片帳號(核驗的帳號)
		cbstita.setFROMACT(feptxn.getFeptxnTroutActno());
		//2566 業務類別10：FIDO金融卡核驗服務
		cbstita.setLE_2566TYPE("10");
		//2566 交易類別00：晶片卡核驗
		cbstita.setLE_AEIPYTP("00");
		//金融帳號核驗資料 - 核驗用途01：金融FIDO，其他核驗不用放(空白)
		if(StringUtils.equals(feptxn.getFeptxnActivityCode(), "FD")) {
			cbstita.setLF_AEIPYUES("01");
		} else if (StringUtils.equals(feptxn.getFeptxnActivityCode(), "TT")) { //普發現金
			cbstita.setLF_AEIPYUES("99");
		}
		//金融帳號核驗資料 – 核驗項目：<2566-10>固定放 01(卡片+ID)
		cbstita.setLF_AELFTP("01");
		//<2566-10>：核驗之ID或統編
		cbstita.setLE_IDNO(feptxn.getFeptxnIdno());

		this.setoTita(cbstita);
		this.setTitaToString(cbstita.makeMessage());
		this.setASCIItitaToString(cbstita.makeMessageAscii());
		return FEPReturnCode.Normal;
    }

    /**
     * 拆解CBS回應電文
     *
     * @param cbsTota
     * @param type
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
        /* 電文內容格式請參照TOTA電文格式(CB_VA_O001) */
        /* 拆解主機回應電文 */
        CB_VA_O001 tota = new CB_VA_O001();
        tota.parseCbsTele(cbsTota);
        this.setTota(tota);

        /* 更新交易 */
        FEPReturnCode rtnCode = this.updateFEPTxn(tota);

        /* 回覆FEP */
        // 處理 CBS 回應
        return rtnCode;
    }

    /**
     * 更新交易
     *
     * @param cbsTota
     * @return
     * @throws Exception
     */
    private FEPReturnCode updateFEPTxn(CB_VA_O001 cbsTota) throws Exception {
        FEPReturnCode rtnCode;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);
        /* 變更FEPTXN交易記錄 */
        if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
            cbsTota.setIMSRC_TCB("000");
        }
        rtnCode = prepareUpdateIMSData(getTota());
        if(rtnCode != FEPReturnCode.Normal) {
    		getLogContext().setMessage("after prepare Update IMS Data, RC:" + rtnCode.toString());
    		sendEMS(getLogContext());
    	}else if (!cbsTota.getIMSRC_TCB().equals("000")
    			|| StringUtils.isBlank(cbsTota.getIMSRC4_FISC())
    			|| !cbsTota.getIMSRC4_FISC().equals("4001")) {
			rtnCode = FEPReturnCode.CBSCheckError;
		}

        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
        	int insertCount = this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
        	int insertCount2 = this.feptxnDao.updateByPrimaryKeySelective(feptxntcb);
        	
			if (insertCount <= 0 || insertCount2 <= 0) {
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

}
