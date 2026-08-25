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
import com.syscom.fep.vo.text.ims.AB_TAX_I001;
import com.syscom.fep.vo.text.ims.AB_TAX_O001;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Calendar;

public class ABTAXI001 extends ACBSAction {

    public ABTAXI001(MessageBase txData) {
        super(txData, new AB_TAX_O001());
    }

//    private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);

    /**
     * 組CBS TITA電文
     *
     * @param txType
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode getCbsTita(String txType) throws Exception {
        /* TITA 請參考合庫主機電文規格(AB_TAX_I001) */
        //Header
        AB_TAX_I001 cbsTita = new AB_TAX_I001();
        String msgtyp;
        if ("EAT".equals(feptxn.getFeptxnChannel())) {
        	RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReq =this.getEATMRequest().getBody().getRq().getSvcRq();
        	msgtyp = atmReq.getMSGTYP();
            cbsTita.setCARDTYPE(atmReq.getPICCDID()); // 交易卡片型態
            cbsTita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())
                    && "K".equals(atmReq.getPICCDID())
                    && "AA".equals(atmReq.getMSGTYP())) { // 第一次上送CBS
                cbsTita.setTXNICCTAC(atmReq.getATMDATA().substring(758,778)); // LL+DATA
            }else{
                cbsTita.setTXNICCTAC(StringUtils.repeat("40",10));
            }
            if ("T6".equals(feptxn.getFeptxnTxCode()) || "T8".equals(feptxn.getFeptxnTxCode())) {
                cbsTita.setTAX_END_DATE(atmReq.getFADATA().substring(7,13));
                cbsTita.setTAX_BILL_NO(feptxn.getFeptxnReconSeqno());
            }
            cbsTita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄 (未轉ASC，為原始電文)
            cbsTita.setATMTRANSEQ(atmReq.getTRANSEQ());// ATM交易序號
        } else {
            ATMGeneralRequest atmReq = this.getAtmRequest();
            msgtyp = atmReq.getMSGTYP();
            cbsTita.setCARDTYPE(atmReq.getPICCDID()); // 交易卡片型態
            cbsTita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
            // 合庫ATM代理:發卡行為006且交易卡片型態為”K，TAC須上送主機
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())
                    && "K".equals(atmReq.getPICCDID())
                    && "AA".equals(atmReq.getMSGTYP())) { // 第一次上送CBS
                cbsTita.setTXNICCTAC(atmReq.getPICCTAC()); 
            }else{
                cbsTita.setTXNICCTAC(StringUtils.repeat("40",10));
            }
            if ("T6".equals(feptxn.getFeptxnTxCode()) || "T8".equals(feptxn.getFeptxnTxCode())) {
                cbsTita.setTAX_END_DATE(atmReq.getFADATA().substring(7,13));
                cbsTita.setTAX_BILL_NO(feptxn.getFeptxnReconSeqno());
            }
            cbsTita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄 (未轉ASC，為原始電文)
            cbsTita.setATMTRANSEQ(atmReq.getTRANSEQ());// ATM交易序號
        }
        cbsTita.setIMS_TRANS("MFEPAF00");
        cbsTita.setSYSCODE("FEP");
        cbsTita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
        cbsTita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
        cbsTita.setTXN_FLOW("A"); // 代理
        cbsTita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
        cbsTita.setPCODE(feptxn.getFeptxnPcode());
        cbsTita.setFSCODE(feptxn.getFeptxnTxCode().trim());
        // PROCESS TYPE
        if ("0".equals(txType)) { // 查詢、檢核
        	cbsTita.setMSG_CAT(msgtyp);
            cbsTita.setPROCESS_TYPE("CHK");
            cbsTita.setRESPONSE_CODE("0000");
        } else if ("1".equals(txType)) { // 入扣帳
        	cbsTita.setMSG_CAT("10");
            cbsTita.setPROCESS_TYPE("ACCT");
            cbsTita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
            if("2532".equals(feptxn.getFeptxnPcode())) {
            	cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno7());//轉入單位
            	cbsTita.setTOACT(feptxn.getFeptxnTrinActno());//轉入帳號
            }
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
        // Detail
        cbsTita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
        cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
        cbsTita.setTXNAMT(feptxn.getFeptxnTxAmtAct());
        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno());
        cbsTita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno7());
        cbsTita.setTAX_TYPE(feptxn.getFeptxnPaytype()); // 繳款類別
        
        if ("T5".equals(feptxn.getFeptxnTxCode()) || "T7".equals(feptxn.getFeptxnTxCode())) {
            cbsTita.setTAX_ORGAN(feptxn.getFeptxnTaxUnit());
            cbsTita.setTAX_CID(feptxn.getFeptxnIdno());
        }
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
        /* 電文內容格式請參照TOTA電文格式(AB_TAX_O001) */
        /* 拆解主機回應電文 */
        AB_TAX_O001 tota = new AB_TAX_O001();
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
    private FEPReturnCode updateFEPTxn(AB_TAX_O001 cbsTota) throws Exception {
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
    	
    	// 轉出分行(BBB+ 財稅代號)，代理行交易2532使用，主機檢核後下送，0200 放入 BIT #14
		if(StringUtils.isNotBlank(cbsTota.getTAX_FROM_BRANCH())) {
			feptxn.setFeptxnTroutBkno7(cbsTota.getTAX_FROM_BRANCH());
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
