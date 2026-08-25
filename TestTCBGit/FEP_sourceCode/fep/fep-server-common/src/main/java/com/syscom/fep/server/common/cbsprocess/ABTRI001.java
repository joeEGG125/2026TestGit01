package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.ims.AB_TR_I001;
import com.syscom.fep.vo.text.ims.AB_TR_O001;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.Calendar;

public class ABTRI001 extends ACBSAction {

    public ABTRI001(MessageBase txData) {
        super(txData, new AB_TR_O001());
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
        /* TITA 請參考合庫主機電文規格(AB_TR_I001) */
        // Header
        AB_TR_I001 cbsTita = new AB_TR_I001();
        if("EAT".equals(feptxn.getFeptxnChannel())){
            RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReq =this.getAtmData().getTxObject().getEatmrequest().getBody().getRq().getSvcRq();
            cbsTita.setMSG_CAT(atmReq.getMSGTYP());
            cbsTita.setCARDTYPE(atmReq.getPICCDID()); // 交易卡片型態
            cbsTita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
            // 合庫ATM代理:發卡行為006且交易卡片型態為”K，TAC須上送主機
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())
                    && "K".equals(atmReq.getPICCDID())
                    && "AA".equals(atmReq.getMSGTYP())) { // 第一次上送CBS
                cbsTita.setTXNICCTAC(atmReq.getATMDATA().substring(758,778)); // LL+DATA
            }else{
                cbsTita.setTXNICCTAC(StringUtils.repeat("40",10));
            }
            cbsTita.setTR_SPECIAL_FLAG(atmReq.getTACODE());
            /* 預約交易 */
            if ("RV".equals(atmReq.getLANGID())
                    && SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())) {
                cbsTita.setEATM_RESERVE_FLAG("Y");
                cbsTita.setORIGINAL_TX_DAYTIME(atmReq.getPIEODT());
            }
            //轉入方備註欄，帶EAT電文的EBCDIC值   2026-03-11調整
            if(StringUtils.isNotBlank(atmReq.getPIETNOTE())){
            cbsTita.setTO_ACT_MEMO(atmReq.getPIETNOTE());
            }else{
                cbsTita.setTO_ACT_MEMO(StringUtils.repeat("40",16));
            }
            //轉出方備註欄，帶EAT電文的EBCDIC值   2026-03-11調整
            if(StringUtils.isNotBlank(atmReq.getPIEFNOTE())) {
                cbsTita.setFROM_ACT_MEMO(atmReq.getPIEFNOTE());
            }else{
                cbsTita.setFROM_ACT_MEMO(StringUtils.repeat("40",16));
            }
            cbsTita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄 (未轉ASC，為原始電文)
            // ATM交易序號
            cbsTita.setATMTRANSEQ(atmReq.getTRANSEQ());
            //中文附言欄，給EBCDIC空白的編碼
            cbsTita.setTXMEMO(StringUtils.repeat("40",40));
            // 轉帳手續費
            if (StringUtils.isNotBlank(atmReq.getIPYDATA())) {
                cbsTita.setCHANNEL_CHARGE(new BigDecimal(atmReq.getIPYDATA().substring(8,10)));
            }
        }else{
            ATMGeneralRequest atmReq = this.getAtmRequest();
            cbsTita.setMSG_CAT(atmReq.getMSGTYP());
            cbsTita.setCARDTYPE(atmReq.getPICCDID()); // 交易卡片型態
            cbsTita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
            // 合庫ATM代理:發卡行為006且交易卡片型態為”K，TAC須上送主機
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())
                    && "K".equals(atmReq.getPICCDID())
                    && "AA".equals(atmReq.getMSGTYP())) { // 第一次上送CBS
                cbsTita.setTXNICCTAC(atmReq.getPICCTAC()); //TAC(未轉ASC，為原始電文值)
            }else{
                cbsTita.setTXNICCTAC(StringUtils.repeat("40",10));
            }
            cbsTita.setTR_SPECIAL_FLAG(atmReq.getTACODE());
            cbsTita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄 (未轉ASC，為原始電文)
            // ATM交易序號
            cbsTita.setATMTRANSEQ(atmReq.getTRANSEQ());
            //其他通道不需上送此兩個中文欄位，但要給滿40
            cbsTita.setTO_ACT_MEMO(StringUtils.repeat("40",16));
            cbsTita.setFROM_ACT_MEMO(StringUtils.repeat("40",16));
            //中文附言欄，給EBCDIC空白的編碼
            cbsTita.setTXMEMO(StringUtils.repeat("40",40));
            // 轉帳手續費
            if (StringUtils.isNotBlank(atmReq.getIPYDATA())) {
                cbsTita.setCHANNEL_CHARGE(new BigDecimal(atmReq.getIPYDATA().substring(8,10)));
            }
        }
        // 收款戶類型(01:個人,02:非個人)
        cbsTita.setTO_ACT_TYPE(feptxntcb.getFeptxntcbActtype());
        cbsTita.setSYSCODE("FEP");
        cbsTita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
        cbsTita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
        // TXN_FLOW
        if (0 == feptxn.getFeptxnFiscFlag()) {
        	cbsTita.setIMS_TRANS("MFEPAT00");
            cbsTita.setTXN_FLOW("C"); // 自行
        } else {
        	cbsTita.setIMS_TRANS("MFEPAF00");
            cbsTita.setTXN_FLOW("A"); // 代理
            if ("1".equals(txType)) {
            	cbsTita.setMSG_CAT("10");
            }
        }
        cbsTita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
        cbsTita.setPCODE(feptxn.getFeptxnPcode());
        cbsTita.setFSCODE(feptxn.getFeptxnTxCode().trim());
        // PROCESS TYPE
        if ("0".equals(txType)) { // 查詢、檢核
            cbsTita.setPROCESS_TYPE("CHK");
        } else if ("1".equals(txType)) { // 入扣帳
            cbsTita.setPROCESS_TYPE("ACCT");
            cbsTita.setI_ACT(feptxn.getFeptxnAcctSup());//財金response
        } else if ("2".equals(txType)) { // 沖正
            cbsTita.setPROCESS_TYPE("RVS");
        }
        // 財金營業日(西元年須轉民國年)
        String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
        if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
            feptxnTbsdyFisc = "000000";
        } else {
            feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
        }
        cbsTita.setBUSINESS_DATE(feptxnTbsdyFisc);
        cbsTita.setACQUIRER_BANK(feptxn.getFeptxnBkno());
        cbsTita.setTXNSTAN(feptxn.getFeptxnStan());
        cbsTita.setTERMINALID(feptxn.getFeptxnAtmno());
        cbsTita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType());
        cbsTita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno());
        if (feptxn.getFeptxnFiscFlag() == 0) {
        	cbsTita.setRESPONSE_CODE("0000"); // 正常才上送
        } else {
        	cbsTita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
        }

        // Detail
        cbsTita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
        cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
        cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt());
        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno());
        cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno() + "0000");
        cbsTita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno() + "0000");
        cbsTita.setTOACT(feptxn.getFeptxnTrinActno());
        
        this.setoTita(cbsTita);
        this.setTitaToString(cbsTita.makeMessage());
        this.setASCIItitaToString(cbsTita.makeMessageAscii());
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
        /* 電文內容格式請參照TOTA電文格式(AB_TR_O001) */
        /* 拆解主機回應電文 */
        AB_TR_O001 tota = new AB_TR_O001();
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
    private FEPReturnCode updateFEPTxn(AB_TR_O001 cbsTota) throws Exception {
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
    			|| (!cbsTota.getIMSRC4_FISC().equals("4001") && !cbsTota.getIMSRC4_FISC().equals("0000"))) {
			rtnCode = FEPReturnCode.CBSCheckError;
		}

        //20260514 修改轉出方為本行時取主機下送手續費
        if ( SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno()) ) {
            feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());
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
