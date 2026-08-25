package com.syscom.fep.server.aa.inbk;


import java.util.Date;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.mapper.*;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * @author Richard
 */
public class INTWDRequestI extends INBKAABase {

    private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;

    private Intltxn defINTLTXN = new Intltxn();
    private Intltxn oriINTLTXN = new Intltxn();
    private IntltxnMapper dbINTLTXN = SpringBeanFactoryUtil.getBean(IntltxnMapper.class);
    private boolean isEC = false;
    private String W_TXRUST;

    /**
     * AA的建構式,在這邊初始化及設定其他相關變數
     *
     * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
     *                <p>
     *                初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
     *                ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
     *                FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
     * @throws Exception INTWDRequestIException
     */
    public INTWDRequestI(FISCData txnData) throws Exception {
        super(txnData);
    }

    /**
     * 程式進入點
     */
    @Override
    public String processRequestData() {
        try {
            // (1) 	檢核財金電文 Header
            _rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);
            String sFiscRc = TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext());
            if("10".equals(sFiscRc.substring(0, 2))) {
                /* 程式結束 FISC RC:Garbled Message */
                getFiscBusiness().setFeptxn(null);
                getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), _rtnCode, getFiscReq());
                return StringUtils.EMPTY;
            }

            //2.AddTxData:新增交易記錄(FEPTXN & FEPTXNTCB & INTLLTXN)
            _rtnCode2 = this.addTxData();
            /* 2025/2/19 修改 for 跨國提沖正(2430/2470)查無原交易, 送IMS主機 */
            if (_rtnCode2 != FEPReturnCode.Normal && ("N").equals(feptxn.getFeptxnCbsProc()) ) {
                // 新增 FEPTXN、FEPTXNTCB 失敗 就結束程式
                getLogContext().setProgramName(ProgramName + ".addTxData");
                getLogContext().setMessage(_rtnCode2.toString());
                getLogContext().setRemark("新增交易記錄有誤!!");
                sendEMS(getLogContext());
                return StringUtils.EMPTY;
            }

            //3.商業邏輯檢核 & 電文Body檢核
            if (_rtnCode == FEPReturnCode.Normal) { /*CheckHeader Error*/
                _rtnCode = checkBusinessRule();
                if (("Y").equals(feptxn.getFeptxnCbsProc())) { //走3-1流程，程式結束
                    return StringUtils.EMPTY;	//程式結束
                }
            }

            //4. SendToCBS: 帳務主機處理
            if (_rtnCode == FEPReturnCode.Normal) { /*CheckHeader Error*/
                _rtnCode2 = this.sendToCBS();
                /*若扣帳Timeout則不組回應電文給財金, 程式結束, 若主機回應扣帳失敗
				則仍需組回應電文給財金 */

                if (feptxn.getFeptxnCbsTimeout() == 1) {
                    /* 主機TimeOut 不組回應電文給財金, 程式結束 */
                    getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode2.getValue());
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("S");  /*Reject-abnormal*/
                    getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/
                    getFiscBusiness().updateTxData();//更新交易記錄
                    return StringUtils.EMPTY;
                }
            }

            //5. 	label_END_OF_FUNC
            //6.PrepareFISC:準備回財金的相關資料
            _rtnCode = this.prepareForFISC();

            //7.UpdateTxData: 更新交易記錄(FEPTXN & FEPTXNTCB & INTLLTXN)
            _rtnCode = this.updateTxData();
            //程式結束
            if(_rtnCode != FEPReturnCode.Normal) {
                getLogContext().setRemark("updateTxData error");
                logMessage(getLogContext());
                return StringUtils.EMPTY;
            }

            //8.ProcessAPTOT:更新跨行代收付
            _rtnCode = this.processAPTOT();

            //9.將組好的財金電文送給財金
            _rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);

            //10.判斷是否需傳送2160電文給財金
            /* 2025/7/1 修改 for 傳送 2160電文給財金 */
            /* "A" : 成功或失敗均需傳送 */
            if("A".equals(feptxn.getFeptxnSend2160())
                    && !"4001".equals( feptxn.getFeptxnRepRc()) ) {
                _rtnCode = insertINBK2160();
            }
        } catch (Exception e) {
            this._rtnCode = FEPReturnCode.ProgramException;
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            sendEMS(this.logContext);
        } finally {
            this.getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            this.getTxData().getLogContext().setMessage("FiscResponse:"+this.getFiscRes().getFISCMessage());
            this.getTxData().getLogContext().setProgramName(this.aaName);
            this.getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
            logMessage(Level.DEBUG, this.logContext);
        }
        // 2011/03/17 modified by Ruling for 若回rtnCode給Handler，FISCGW會將此值回給財金，但此時AA已結束不需在回財金，故改成回空白
        return StringUtils.EMPTY;
    }




    /**
     * 新增交易記錄
     *
     * @return FEPReturnCode
     */
    private FEPReturnCode addTxData() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            //(1) 	Prepare() 交易記錄初始資料
            rtnCode = getFiscBusiness().prepareFEPTXN();
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
                getLogContext().setRemark("PREPARE FEPTXN  ERROR");
                sendEMS(getLogContext());
                return rtnCode;
            }

            rtnCode = getFiscBusiness().prepareFEPTXNTCB();
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                getLogContext().setProgramName(ProgramName + ".prepareFEPTXNTCB");
                getLogContext().setRemark("PREPARE FEPTXNTCB ERROR");
                sendEMS(getLogContext());
                return rtnCode;
            }

            RefBase<Intltxn> intltxnRefBase = new RefBase<>(defINTLTXN);
            RefBase<Intltxn> oriintltxnRefBase = new RefBase<>(oriINTLTXN);
            rtnCode = getFiscBusiness().prepareIntltxn(intltxnRefBase, oriintltxnRefBase, MessageFlow.Request);
            defINTLTXN = intltxnRefBase.get();
            oriINTLTXN = oriintltxnRefBase.get();
            if (("N").equals(feptxn.getFeptxnCbsProc())) { /* 2025/2/19 修改 for 跨國提沖正(2430/2470)查無原交易, 送IMS主機 */
                if (rtnCode != FEPReturnCode.Normal) {
                    // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                    getLogContext().setProgramName(ProgramName + ".prepareIntltxn");
                    getLogContext().setRemark("PREPARE Intltxn ERROR");
                    sendEMS(getLogContext());
                    return rtnCode;
                }
            }

            //(2) 	以TRANSACTION 新增交易記錄
            PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
            TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

            try{
                rtnCode = getFiscBusiness().insertFEPTxn();
                if (rtnCode != FEPReturnCode.Normal) {
                    return rtnCode;
                }

                rtnCode = getFiscBusiness().insertFEPTXNTCB();
                if (rtnCode != FEPReturnCode.Normal) {
                    return rtnCode;
                }
                rtnCode = getFiscBusiness().insertINTLTxn(defINTLTXN);
                if (("N").equals(feptxn.getFeptxnCbsProc())) { /* 2025/2/19 修改 for 跨國提沖正(2430/2470)查無原交易, 不寫INTLTXN */
                    if (rtnCode != FEPReturnCode.Normal) {
                        return rtnCode;
                    }
                }

                transactionManager.commit(txStatus);
                return rtnCode;
            } catch (Exception ex) {
                getLogContext().setProgramException(ex);
                getLogContext().setProgramName(StringUtils.join(ProgramName, ".addTxData"));
                sendEMS(getLogContext());
                return FEPReturnCode.ProgramException;
            }finally {
                if ( !txStatus.isCompleted()) {
                    transactionManager.rollback(txStatus);
                }
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".addTxData"));
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 3. 	CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
     * @return FEPReturnCode
     */
    private FEPReturnCode checkBusinessRule() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
        try {
            //(3.1) 	檢核MAC
            rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
            getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
            getLogContext().setRemark("after checkFiscMac RC:" + rtnCode.toString());
            logMessage(getLogContext());
            if (("N").equals(feptxn.getFeptxnCbsProc())) {
                if (rtnCode != FEPReturnCode.Normal) {
                    return rtnCode;
                }
            }

            //(3.2) 	檢核&更新原始交易狀態  FOR  2430/2470
            if (("Y").equals(feptxn.getFeptxnCbsProc())) {
                /* 2025/2/19 修改 for 跨國提沖正(2430/2470)查無原交易, 送IMS主機 */
                try {
                    String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
                    ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
                    rtnCode = new CBS(hostAA, getTxData()).sendToIMS(getFiscReq().getFISCMessage().toString(),getFiscReq(),getFiscBusiness().getFiscINBKRes(),rtnCode);
                } catch (Exception ex) {
                    getLogContext().setProgramException(ex);
                    getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToIMS"));
                    sendEMS(getLogContext());
                    return FEPReturnCode.ProgramException;
                }
                if (rtnCode == FEPReturnCode.Normal) { /* 2025/2/26 修改, 在ＡＡ　將主機回覆電文傳回財金 */
                    rtnCode = getFiscBusiness().sendToFISCFromCBSRespons();
                    this.logContext.setRemark("sendToFISCFromCBSRespons complete");
                    logMessage(Level.DEBUG, this.logContext);
                }
                return rtnCode; /*程式結束*/
            }else{
                if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
                    /*檢核原交易是否成功*/
                    if ("A".equals(oriINTLTXN.getIntltxnTxrust()) || "B".equals(oriINTLTXN.getIntltxnTxrust())) {
                        /* 保留原交易狀態 */
                        W_TXRUST = oriINTLTXN.getIntltxnTxrust();
                    } else {
                        /* FISC RC: MAPPING 欄位資料不符 */
                        W_TXRUST = oriINTLTXN.getIntltxnTxrust();
                        return FEPReturnCode.CheckFieldError;
                    }
                    /*檢核原交易是否已沖正*/
                    if ("D".equals(oriINTLTXN.getIntltxnTxrust())) {
                        return FEPReturnCode.CheckFieldError; /* FISC RC: MAPPING 欄位資料不符 */
                    }

                    oriINTLTXN.setIntltxnTxrust("T"); /*沖銷或授權完成進行中*/
                    if (dbINTLTXN.updateByPrimaryKey(oriINTLTXN) < 1) {
                        rtnCode = FEPReturnCode.UpdateFail;
                        return rtnCode;
                    }

                    /* 2024/8/19 修改 for 跨國提款沖銷交易(2430/270)上傳告誡註記 */
                    /*2024/8/19 修改 for 將原交易日期 存入 FEPTXN_DUE_DATE */
                    feptxn.setFeptxnDueDate(oriINTLTXN.getIntltxnTxDate());
                    /*2024/8/19 修改 for 將原交易EJ 存入 FEPTXN_TRK3 */
                    feptxn.setFeptxnTrk3(oriINTLTXN.getIntltxnEjfno().toString());

                    /*2025/2/12 修改 for 將原交易INTLTXN存入 FEPTXN */
                    feptxn.setFeptxnTxAmtAct(oriINTLTXN.getIntltxnTxAmtAct());
                    feptxn.setFeptxnTxAmtSet(oriINTLTXN.getIntltxnSetAmt());
                    feptxn.setFeptxnExrate(oriINTLTXN.getIntltxnExrate());
                    /* 借用 INTLTXN_ARPC 存放 「沖銷使用欄位」 */
                    feptxn.setFeptxnMerchantId(oriINTLTXN.getIntltxnArpc());
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "checkBusinessRule");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 4. SendToCBS:送往CBS主機處理
     * return FEPReturnCode
     */
    private FEPReturnCode sendToCBS() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            String txType;
            txType = "1"; /* 扣帳 */

            //20221028一律使用MSGCTL_TWCBSTXID
            this.getTxData().setIntlTxn(defINTLTXN);
            String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
            rtnCode = new CBS(hostAA, getTxData()).sendToCBS(txType);

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".SendToCBSAndAsc");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 6. 	PrepareFISC:準備回財金的相關資料
     * @return FEPReturnCode
     */
    private FEPReturnCode prepareForFISC() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        //6.1 判斷 rtnCode 是否 Normal
        if (_rtnCode != FEPReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
            if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())){
                getLogContext().setProgramName(StringUtils.join(ProgramName));
                getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext()));
            }
        } else {
            if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())){
                getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK); /*+REP*/
            }
        }

        //6.2 產生 Response 電文Header:
        rtnCode = getFiscBusiness().prepareHeader("0210");
        if (rtnCode != FEPReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
        }

        //6.3 產生 Response 電文Body:
        String wk_BITMAP = null;
        if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
            /*跨行轉帳-轉入交易讀取第2組 Bit Map, 否則讀取第1組*/
            wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
        } else {
            /*-REP*/
            wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
        }

        // 依據wk_BITMAP(判斷是否搬值)
        for (int i = 2; i <= 63; i++) {
            if ("1".equals(wk_BITMAP.substring(i, i + 1))) {
                switch (i) {
                    case 5: { /* 代付單位 CD/ATM 代號 */
                        getFiscRes().setATMNO(StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnAtmno(), 8, "0"));
                        break;
                    }
                    case 6: { /* 可用餘額 */
                        getFiscRes().setBALA(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
                        break;
                    }
                    case 35: { /* CD/ATM 國際化交易之原始資料 */
                        getFiscRes().setOriData(getFiscReq().getOriData());
                        break;
                    }
                    /* 2024/8/22 修改 */
                    case 60: {  /*ＩＣ卡卡號或卡片國際組織網路識別資料 */
                        getFiscRes().setNetwkData(getFiscReq().getNetwkData());
                        break;
                    }
                    case 62: { /* 授權碼*/
                        getFiscRes().setAuthCode(getFiscBusiness().getFeptxn().getFeptxnAuthcd());
                        break;
                    }
                }
            }
        }

        //6.4 產生 MAC
        RefString refMac = new RefString(getFiscRes().getMAC());
        rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFiscMac(getFiscRes().getMessageType(), refMac);
        getFiscRes().setMAC(refMac.get());
        if (rtnCode != FEPReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
            getFiscRes().setMAC("00000000");
        }

        //6.5 產生Bit Map
        rtnCode = getFiscBusiness().makeBitmap(getFiscRes().getMessageType(), getFiscRes().getProcessingCode(), MessageFlow.Response);
        getLogContext().setRemark("after makeBitmap RC:" + rtnCode.toString());
        logMessage(getLogContext());
        if (rtnCode != FEPReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
            getFiscRes().setBitMapConfiguration("0000000000000000");
        }
        rtnCode = getFiscRes().makeFISCMsg();
        return rtnCode;
    }

    /**
     * 7. 	UpdateTxData: 更新交易記錄(FEPTXN )
     * @return FEPReturnCode
     */
    private FEPReturnCode updateTxData() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            //(1) 	更新 FEPTXN
            if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
                if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {// (3 way)
                    getFiscBusiness().getFeptxn().setFeptxnPending((short) 1); // Pending
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("B"); // Pending
                } else {// (2 way)
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 成功
                }
                if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
                    /* 8/18 修改, 非沖正交易 */
                    isEC = false;
                    getFiscBusiness().getFeptxn().setFeptxnClrType((short) 1);
                }
            } else if ("0".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) { /*初值:0*/
                getFiscBusiness().getFeptxn().setFeptxnTxrust("R");  /*拒絕-正常*/
                /* 1/30 跨行無卡提款交易失敗, 更新預約檔 */
            }
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); /*FISC Response*/
            getFiscBusiness().getFeptxn().setFeptxnAaComplete((short) 1); /*AA Close*/

            rtnCode = getFiscBusiness().updateTxData(); // 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2])
            if (rtnCode != FEPReturnCode.Normal) {// 若更新失敗則不送回應電文, 人工處理
                transactionManager.rollback(txStatus);
                return rtnCode;
            }

            //(2) 	判斷是否需更新 INTLTXN
            defINTLTXN.setIntltxnBrno(getFiscBusiness().getFeptxn().getFeptxnBrno());
            defINTLTXN.setIntltxnRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
            defINTLTXN.setIntltxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
            defINTLTXN.setIntltxnTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
            defINTLTXN.setIntltxnOriStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());
            defINTLTXN.setUpdateUserid(0);
            defINTLTXN.setUpdateTime(new Date());
            /* 2025/02/12 修改 for 原存跨國提款-主機電文規格修改 */
            defINTLTXN.setIntltxnTxAmtAct(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct());
            defINTLTXN.setIntltxnSetAmt(getFiscBusiness().getFeptxn().getFeptxnTxAmtSet());
            defINTLTXN.setIntltxnExrate(getFiscBusiness().getFeptxn().getFeptxnExrate());
            /* 借用 INTLTXN_ARPC 存放 「沖銷使用欄位」 */
            defINTLTXN.setIntltxnArpc(getFiscBusiness().getFeptxn().getFeptxnMerchantId());

            int iRes;
            iRes = dbINTLTXN.updateByPrimaryKeySelective(defINTLTXN);
            if (iRes <= 0) { /*若更新失敗則不送回應電文, 人工處理*/
                transactionManager.rollback(txStatus);
                rtnCode = IOReturnCode.UpdateFail;
                return rtnCode;
            }

            //(3) 	判斷是否需更新原始交易  for 2430, 2470
            if(StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())){
                if(NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())){
                    oriINTLTXN.setIntltxnTxrust("D");  /*已沖正成功*/
                    if(dbINTLTXN.updateByPrimaryKey(oriINTLTXN)<1){
                        rtnCode = FEPReturnCode.UpdateFail;
                        transactionManager.rollback(txStatus);
                        return rtnCode;
                    }else{ /*-REP*/
                        /* 若TXRUST = “T”進行中, 即可將原交易之狀態改回 Active*/
                        if("T".equals(oriINTLTXN.getIntltxnTxrust())){
                            oriINTLTXN.setIntltxnTxrust(W_TXRUST); /*將原始交易之狀態還原*/
                            iRes = dbINTLTXN.updateByPrimaryKey(oriINTLTXN);
                            if (iRes <= 0) { /*若更新失敗則不送回應電文, 人工處理*/
                                transactionManager.rollback(txStatus);
                                rtnCode = IOReturnCode.UpdateFail;
                                return rtnCode;
                            }
                        }
                    }
                }
            }
            transactionManager.commit(txStatus);
            return rtnCode;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "updateTxData");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 8. 	ProcessAPTOT:更新跨行代收付
     * @return FEPReturnCode
     */
    private FEPReturnCode processAPTOT() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())
                && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
            rtnCode = getFiscBusiness().processAptot(isEC);
            if (rtnCode != FEPReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                getFiscBusiness().updateTxData();
            }
        }
        return rtnCode;
    }

    /**
     * 10. 	判斷是否需傳送2160電文給財金
     * @return FEPReturnCode
     */
    private FEPReturnCode insertINBK2160() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            //檢核Header
            rtnCode = getFiscBusiness().prepareInbk2160();
            if(rtnCode != FEPReturnCode.Normal){
                getLogContext().setMessage(rtnCode.toString());
                getLogContext().setProgramName(ProgramName + ".insertINBK2160");
                getLogContext().setRemark("寫入INBK2160發生錯誤!!");
                logMessage(getLogContext());
                return FEPReturnCode.INBK2160InsertError;
            }else{
                return FEPReturnCode.Normal;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".insertINBK2160");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }
}

