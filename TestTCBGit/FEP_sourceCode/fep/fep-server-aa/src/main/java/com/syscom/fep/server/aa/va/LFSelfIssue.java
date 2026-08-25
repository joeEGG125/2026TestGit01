package com.syscom.fep.server.aa.va;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.mapper.VatxnMapper;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_SENDDATA;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS;
import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author Jeff
 */
public class LFSelfIssue extends ATMPAABase {
    private Object tota = null;
    private RCV_VA_GeneralTrans_RQ tita = this.getmVAReq();
    private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
    private String rtnMessage = "";
    private VatxnMapper vatxnMapper = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
    private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);

    private SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);

    public LFSelfIssue(NBData txnData) throws Exception {
        super(txnData);
    }

    @Override
    public String processRequestData() {
        Vatxn vatxn = new Vatxn();
        RCV_VA_GeneralTrans_RQ tita = this.getmVAReq();
        RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header header = tita.getBody().getRq().getHeader();
        RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq nbbody = tita.getBody().getRq().getSvcRq();
        RCV_VA_GeneralTrans_RQ_Body_MsgRq_SENDDATA senddata = tita.getBody().getRq().getSvcRq().getSENDDATA();
        try {
            getFeptxn().setFeptxnStan(getATMBusiness().getStan());/*先取 STAN 以供主機電文使用*/
            getLogContext().setStan(getFeptxn().getFeptxnStan());

            // 1. 交易記錄初始資料
            _rtnCode = getATMBusiness().VAPrepareFEPTXN();
            if (_rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
                getLogContext().setRemark("PREPARE FEPTXN ERROR");
                sendEMS(getLogContext());
                return rtnMessage;
            }

            _rtnCode = getATMBusiness().other_prepareFEPTXNTCB();
            if (_rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                getLogContext().setProgramName(ProgramName + ".prepareFEPTXNTCB");
                getLogContext().setRemark("PREPARE FEPTXNTCB ERROR");
                sendEMS(getLogContext());
                return rtnMessage;
            }

            // 2. AddTxData: 新增交易記錄(FEPTXN)
            addTxData();
            if (_rtnCode != FEPReturnCode.Normal) {
                return rtnMessage;
            }

            // 3. 商業邏輯檢核(ATM電文)
            if(_rtnCode == FEPReturnCode.Normal){
                //檢核外圍Channel
                _rtnCode = getATMBusiness().checkChannelEJFNO();
                if (_rtnCode != FEPReturnCode.Normal) {
                    getFeptxn().setFeptxnTxrust("S");/* Reject-abnormal */
                    getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM));
                    getFeptxn().setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP));
                    //GO TO  6  更新交易記錄
                }
                if (_rtnCode == FEPReturnCode.Normal) {
                    _rtnCode = getATMBusiness().checkRequestFromOtherChannel(getmNBtxData(), nbbody.getINTIME());
                    if (_rtnCode != FEPReturnCode.Normal) {
                        getFeptxn().setFeptxnTxrust("S");/* Reject-abnormal */
                        getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM));
                        getFeptxn().setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP));
                        //GO TO  6  更新交易記錄
                    }
                }
            }


            // 4. 新增約定及核驗交易(VATXN)記錄(if need)
            if (_rtnCode == CommonReturnCode.Normal) {
                RefBase<Vatxn> vatxnRefBase = new RefBase<>(vatxn);
                prepareVATXNforVALE(vatxnRefBase, tita);
                vatxn = vatxnRefBase.get();
                vatxnMapper.insertSelective(vatxn);
                // writeLog("Prepare約定及核驗交易(VATXN)記錄", ProgramFlow.AAIn);
            }

            // 5. SendToCBS/ASC: 帳務主機處理
            if (_rtnCode == CommonReturnCode.Normal) {
                /*一般金融卡帳號*/
                /*進CBS主機檢核*/
                /* CBSProcess_ABVAI001(組送CBS 主機撤銷通知Request交易電文).doc*/
                String AATxTYPE = "0"; // 上CBS入扣帳
                String AA = getmNBtxData().getMsgCtl().getMsgctlTwcbstxid();
                feptxn.setFeptxnCbsTxCode(AA);
                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmNBtxData());
                this.getmNBtxData().setVatxn(vatxn);
                _rtnCode = new CBS(hostAA, getmNBtxData()).sendToCBS(AATxTYPE);

                tota = hostAA.getTota();

                if (feptxn.getFeptxnCbsTimeout() == 1) { // HostResponseTimeout
                    getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM));
                } else {
                    if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbImsrc4Fisc()) && !"4001".equals(feptxntcb.getFeptxntcbImsrc4Fisc())) {
                        feptxn.setFeptxnCbsRc(feptxntcb.getFeptxntcbImsrc4Fisc());
                        getFeptxn().setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(getFeptxn().getFeptxnCbsRc(), FEPChannel.FISC));
                    }
                    feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
                }
            }

            // 6. UpdateTxData:更新交易記錄(FEPTXN )
            updateTxData();

            // 7. 更新約定及核驗交易(VATXN)記錄(if need)
            if (_rtnCode == FEPReturnCode.Normal) {
                vatxn.setVatxnTxrust(getFeptxn().getFeptxnTxrust());
//                Vatxn po = vatxnMapper.selectByPrimaryKey(vatxn.getVatxnTxDate(), vatxn.getVatxnEjfno());
                vatxnMapper.updateByPrimaryKeySelective(vatxn);
            }

            // 8. 組ATM回應電文 & 回 ATMMsgHandler
            this.response(vatxn);

            // 9. 交易通知 (if NEED)
            getATMBusiness().sendToNotify();
        } catch (Exception ex) {
            rtnMessage = getResVAStr(header);
            _rtnCode = FEPReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            sendEMS(getLogContext());
        } finally {
            getmNBtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getmNBtxData().getLogContext().setMessage(rtnMessage);
            getmNBtxData().getLogContext().setProgramName(this.aaName);
            getmNBtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            getLogContext().setProgramName(this.aaName);

            // 2026.3.16 LeYun 修正交易異常通知邏輯
            String codeToMsg = String.valueOf(_rtnCode.getValue());
            FEPChannel channel = FEPChannel.FEP;

            if (getFeptxn() != null) {
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc()) && !"4001".equals(getFeptxn().getFeptxnRepRc())) {
                    codeToMsg = getFeptxn().getFeptxnRepRc();
                }
                else if (_rtnCode != FEPReturnCode.ProgramException && getFeptxn().getFeptxnAaRc() != null) {
                    codeToMsg = String.valueOf(getFeptxn().getFeptxnAaRc());
                }
            }

            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(codeToMsg, channel, getLogContext()));
            logMessage(Level.DEBUG, getLogContext());
        }
        // 先組回應ATM 故最後return空字串m
        return rtnMessage;
    }

    /**
     * 2. AddTxData: 新增交易記錄( FEPTxn)
     *
     * @return
     * @throws Exception
     */
    private void addTxData() throws Exception {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // 新增交易記錄(FEPTxn) Returning FEPReturnCode
            /* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            int insertCount = feptxnDao.insertSelective(this.feptxn); // 新增資料
            int insertCount2 = feptxnDao.insertSelective(feptxntcb);
            if (insertCount <= 0 || insertCount2 <= 0) { // 更新失敗
                throw new Exception(); //2025.07.15 Transaction call review 調整
            }
            transactionManager.commit(txStatus);
            this.logContext.setRemark("transaction sucess.");
            logMessage(Level.INFO, logContext);
        } catch (Exception ex) { // 新增失敗
            transactionManager.rollback(txStatus);
            this.logContext.setRemark("transaction fail.");
            logMessage(Level.INFO, logContext);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".addTxData");
            sendEMS(getLogContext());
            _rtnCode = FEPReturnCode.FEPTXNInsertError;
        }
    }

    /**
     * 4. Prepare約定及核驗交易(VATXN)記錄
     *
     * @param vatxn
     * @param tita
     * @return
     */
    public FEPReturnCode prepareVATXNforVALE(RefBase<Vatxn> vatxn, RCV_VA_GeneralTrans_RQ tita) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            vatxn.get().setVatxnTxDate(getFeptxn().getFeptxnTxDate());
            vatxn.get().setVatxnEjfno(getFeptxn().getFeptxnEjfno());
            vatxn.get().setVatxnBkno(getFeptxn().getFeptxnBkno());
            vatxn.get().setVatxnPcode(getFeptxn().getFeptxnPcode());
            vatxn.get().setVatxnTxTime(getFeptxn().getFeptxnTxTime());
            vatxn.get().setVatxnTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
            vatxn.get().setVatxnReqRc(getFeptxn().getFeptxnReqRc());
            vatxn.get().setVatxnRepRc(getFeptxn().getFeptxnRepRc());
            vatxn.get().setVatxnConRc(getFeptxn().getFeptxnConRc());
            vatxn.get().setVatxnTxrust(getFeptxn().getFeptxnTxrust());
            String VACATE = tita.getBody().getRq().getSvcRq().getVACATE();//'業務類別代號’
            String AEIPYTP = tita.getBody().getRq().getSvcRq().getAEIPYTP();//‘交易類別’
            vatxn.get().setVatxnCate(VACATE);//'業務類別代號’
            vatxn.get().setVatxnType(AEIPYTP);//‘交易類別’
            vatxn.get().setVatxnTroutBkno(getFeptxn().getFeptxnTroutBkno());
            vatxn.get().setVatxnTroutActno(getFeptxn().getFeptxnTroutActno());
            vatxn.get().setVatxnBrno(getFeptxn().getFeptxnBrno());
            vatxn.get().setVatxnZoneCode(getFeptxn().getFeptxnZoneCode());
            vatxn.get().setVatxnItem(tita.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP());

            switch (vatxn.get().getVatxnItem()) {
                case "00": //'除卡片及帳號外，無其他核驗項目

                case "01": // 身份證號或外國人統一編號
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    break;
                case "02": // 持卡人之行動電話號碼
                    vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                    break;
                case "03": // 持卡人之出生年月日
                    vatxn.get().setVatxnBirthday(tita.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY());
                    break;
                case "04": // 持卡人之住家電話號碼
                    vatxn.get().setVatxnHphone(tita.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME());
                    break;
                case "11": // 持卡人之身分證號及行動電話號碼
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                    break;
                case "12": // 持卡人之身分證號、行動電話號碼及出生年月
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                    vatxn.get().setVatxnBirthday(tita.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY());
                    break;
                case "13": // 持卡人之身分證號、行動電話號碼及住家電話
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                    vatxn.get().setVatxnHphone(tita.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME());
                    break;
                case "14": // 持卡人之身分證號、行動電話號碼、出生年月日及住家電話號碼
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                    vatxn.get().setVatxnBirthday(tita.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY());
                    vatxn.get().setVatxnHphone(tita.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME());
                    break;
                case "15": // 持卡人之身份證號或外國人統一編號
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
            }
            vatxn.get().setVatxnUse(tita.getBody().getRq().getSvcRq().getSENDDATA().getAEIPYUES());
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareVATXN");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    // 帳務主機處理
    // 6. UpdateTxData:更新交易記錄(FEPTXN)
    private void updateTxData() {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response); /* ATM Response*/
            feptxn.setFeptxnAaComplete((short)1); /* AA Complete */
            feptxn.setFeptxnAaRc(_rtnCode.getValue());
            if (_rtnCode == FEPReturnCode.Normal) {
                feptxn.setFeptxnTxrust("A"); /* 處理結果=成功 */
            } else if ( "0".equals(feptxn.getFeptxnTxrust()) ) { //預設值
                feptxn.setFeptxnTxrust("R"); /* 處理結果=Reject */
            }

            // 兩個Table 同步更新，如有任何錯誤，請ROLLBACK & 寫EMS
            try {
                int updateCount = feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
                int updateCount2 = feptxnDao.updateByPrimaryKeySelective(this.feptxntcb);
                if (updateCount <= 0 || updateCount2 <= 0) { // 更新失敗
                    throw new Exception(); //2025.07.15 Transaction call review 調整
                }
                transactionManager.commit(txStatus);
            } catch (Exception ex) {
                transactionManager.rollback(txStatus);
                feptxn.setFeptxnReplyCode("T452"); //FEPTXNUpdateError
                _rtnCode = FEPReturnCode.FEPTXNUpdateError;
                getLogContext().setProgramException(ex);
                getLogContext().setProgramName(ProgramName + ".updateTxData");
                getLogContext().setRemark("FEPTXN UPDATE ERROR");
                sendEMS(getLogContext());
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToConfirm"));
            sendEMS(getLogContext());
        }
    }

    /**
     * 8. 組ATM回應電文 & 回 ATMMsgHandler
     *
     * @param vatxn
     * @return
     * @throws Exception
     */
    private String response(Vatxn vatxn) throws Exception {
        try {
            RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq titaSvcRq = tita.getBody().getRq().getSvcRq();
            RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header titaheader = tita.getBody().getRq().getHeader();
            SEND_VA_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VA_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs();
            SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA replydata = new SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA();

            header.setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
            header.setCHANNEL(feptxn.getFeptxnChannel());
            header.setMSGID(titaheader.getMSGID());
            header.setCLIENTDT(titaheader.getCLIENTDT());
            if (feptxn.getFeptxnCbsTimeout() != null && feptxn.getFeptxnCbsTimeout() == 1) {
                header.setSYSTEMID("ATM");
                header.setSTATUSCODE("T203");  //主機回應逾時
                header.setSEVERITY("ERROR");
                header.setSTATUSDESC("主機回應逾時");
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"4001".equals(feptxntcb.getFeptxntcbImsrc4Fisc())) { //主機錯誤
                //自行回應結果，主機失敗，提供3碼RC，否則給空白或000
                header.setSYSTEMID("ATM");
                header.setSTATUSCODE(feptxntcb.getFeptxntcbImsrc4Fisc());
                body.setRSPRESULT(feptxntcb.getFeptxntcbImsrcTcb());
                header.setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else if (!"A".equals(feptxn.getFeptxnTxrust())) { //FEP檢核錯誤
                header.setSYSTEMID("ATM");
                header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
                header.setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else {
                header.setSYSTEMID("ATM");
                header.setSTATUSCODE(NormalRC.FISC_ATM_OK);
                header.setSEVERITY("INFO");
                header.setSTATUSDESC("");
            }

            body.setOUTDATE(feptxn.getFeptxnTxDate());
            body.setOUTTIME(feptxn.getFeptxnTxTime());
            body.setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
            body.setTXNSTAN(feptxn.getFeptxnStan());
            body.setCUSTOMERID(feptxn.getFeptxnIdno());
            body.setTXNTYPE(titaSvcRq.getTXNTYPE());
            body.setFSCODE(feptxn.getFeptxnTxCode());
            body.setTRANSAMT(titaSvcRq.getTRANSAMT() == null ? "0" : titaSvcRq.getTRANSAMT().multiply(new BigDecimal(100)).toString());
            body.setAEIPYTP(titaSvcRq.getAEIPYTP());
            body.setTAXIDNO(vatxn.getVatxnIdno());
            if (StringUtils.equals(titaheader.getCHANNEL(), FEPChannel.FID.getNameS())){
                body.setTXN_DATETIME(feptxn.getFeptxnTxDatetimeFisc());
                body.setTERMINAL_TYPE(titaSvcRq.getTERMINAL_TYPE());
                body.setTERMINALID(!StringUtils.equals(titaSvcRq.getAEIPCRBK(), "006") ? "T0560FID" : "T0000FID");
            }
            String AEIPCRBK = null;
            if (StringUtils.equals(titaSvcRq.getAEIPCRBK(), "006"))
                AEIPCRBK = titaSvcRq.getAEIPCRBK() + (feptxn.getFeptxnBrno() != null ? feptxn.getFeptxnBrno() : "0000");
            else
                AEIPCRBK = titaSvcRq.getAEIPCRBK() + "0000";
            body.setAEIPCRBK(AEIPCRBK);
            body.setCLACTNO(feptxn.getFeptxnTroutActno());
            if(StringUtils.isBlank(body.getRSPRESULT())){
                body.setRSPRESULT("");
            }

            replydata.setAELFTP(vatxn.getVatxnItem());
            replydata.setACRESULT(vatxn.getVatxnAcresult());
            replydata.setRESULT(vatxn.getVatxnResult());
            replydata.setACSTAT(vatxn.getVatxnAcstat());
            replydata.setCHKCELL(vatxn.getVatxnTelresult());
            replydata.setAEIPYUES(titaSvcRq.getSENDDATA().getAEIPYUES());

            body.setREPLYDATA(replydata);
            SEND_VA_GeneralTrans_RS va_rs = new SEND_VA_GeneralTrans_RS();
            va_rs.setBody(new SEND_VA_GeneralTrans_RS_Body());
            va_rs.getBody().setRs(new SEND_VA_GeneralTrans_RS_Body_MsgRs());
            va_rs.getBody().getRs().setHeader(header);
            va_rs.getBody().getRs().setSvcRs(body);

            getLogContext().setProgramName("prepareResponseData");
            getLogContext().setRemark("SYSTEMID:" + header.getSYSTEMID());
            logMessage(Level.INFO,getLogContext());

            rtnMessage = XmlUtil.toXML(va_rs);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    public void writeLog(String msg, ProgramFlow flow) {
        LogData logData = new LogData();
        logData.setProgramFlowType(flow);
        logData.setSubSys(SubSystem.NB);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(ProgramName);
        logData.setMessage(msg);
        // logData.setRemark("MBService Receive Request");
        logData.setServiceUrl("/mb/recv");
        logData.setEj(TxHelper.generateEj());
        this.logMessage(logData);
    }

    private String getResVAStr(RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header vaheader) {
        String ResStr = "";
        SEND_VA_GeneralTrans_RS nbRs = new SEND_VA_GeneralTrans_RS();
        SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body rsbody = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body();
        SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs();
        SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_Header();
        SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs();
        msgrs.setHeader(header);
        body.setOUTDATE(" ");
        body.setOUTTIME(" ");
        body.setFEP_EJNO(" ");
        body.setTXNSTAN(" ");
        body.setCUSTOMERID(" ");
        body.setTXNTYPE("RQ");
        body.setFSCODE(" ");
        body.setTRANSAMT("0");
        body.setAEIPYTP(" ");
        body.setTAXIDNO(" ");
        body.setAEIPCRBK(" ");
        body.setCLACTNO(" ");
        body.setRSPRESULT(" ");
        body.setTXN_DATETIME(" ");
        body.setTERMINAL_TYPE(" ");
        body.setTERMINALID(" ");
        RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmVAReq().getBody().getRq().getSvcRq();
        body.setREPLYDATA(new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA());
        if (tita.getVACATE() != null && "10".equals(tita.getVACATE())){
            //2566-10
            body.getREPLYDATA().setAELFTP(" ");
            body.getREPLYDATA().setRESULT(" ");
            body.getREPLYDATA().setACRESULT(" ");
            body.getREPLYDATA().setACSTAT(" ");
            body.getREPLYDATA().setAEIPYUES(" ");
            body.getREPLYDATA().setCHKCELL(" ");
            body.getREPLYDATA().setFILLER(" ");
        }else {
            //2566-02
            body.getREPLYDATA().setPAYUNTNO(" ");
            body.getREPLYDATA().setTAXTYPE(" ");
            body.getREPLYDATA().setPAYFEENO(" ");
            body.getREPLYDATA().setFILLER1(" ");
        }
        msgrs.setSvcRs(body);
        rsbody.setRs(msgrs);
        nbRs.setBody(rsbody);

        header.setCLIENTTRACEID(vaheader.getCLIENTTRACEID());
        header.setCHANNEL(vaheader.getCHANNEL());
        header.setMSGID(vaheader.getMSGID());
        header.setCLIENTDT(vaheader.getCLIENTDT());
        header.setSYSTEMID("ATM");
        header.setSTATUSCODE("T099");
        header.setSEVERITY("ERROR");
        header.setSTATUSDESC("發生exception");

        ResStr = XmlUtil.toXML(nbRs);
        return ResStr;
    }
}