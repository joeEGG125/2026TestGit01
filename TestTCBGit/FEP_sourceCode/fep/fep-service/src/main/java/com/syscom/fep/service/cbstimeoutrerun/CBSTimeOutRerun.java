package com.syscom.fep.service.cbstimeoutrerun;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.INBKData;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.astarloadutils.AStarLoadUtils;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.jms.JmsReceiver;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.UUIDUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgSimpleOperator;
import com.syscom.fep.jms.queue.CBSPENDQueueConsumers;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.CbspendExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.model.Cbspend;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.CBSAdapter;
import com.syscom.fep.vo.text.ims.CB_IQTX_I001;
import com.syscom.fep.vo.text.ims.CB_IQTX_O001;
import com.syscom.fep.vo.text.ims.CB_IQTX_O002;
import jakarta.annotation.PostConstruct;
import jakarta.jms.Message;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@StackTracePointCut(caller = SvrConst.SVR_CBSTimeOutRerun)
public class CBSTimeOutRerun extends FEPBase implements JmsReceiver<String> {
    private CbspendExtMapper cbspendExtMapper;
    private SysstatExtMapper sysstatExtMapper;
    private FeptxnDao feptxnDao;
    private boolean isEnd = false;

    public void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_CBSTimeOutRerun);
    }

    @PostConstruct
    public void initialization() {
        putMDC();
        cbspendExtMapper = SpringBeanFactoryUtil.getBean(CbspendExtMapper.class);
        sysstatExtMapper = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
        feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
        SpringBeanFactoryUtil.registerBean(CBSPENDQueueConsumers.class).subscribe(this);
        SpringBeanFactoryUtil.registerController(CBSTimeOutRerunController.class);
    }

    /**
     * 接收訊息
     *
     * @param destination
     * @param payload
     * @param message
     */
    @Override
    public void messageReceived(String destination, String payload, Message message) {
        putMDC();
        String messageIn = payload;
        LogHelperFactory.getTraceLogger().trace(SvrConst.SVR_CBSTimeOutRerun, " Recv msg:", messageIn);
        String messageOut = StringUtils.EMPTY;
        logContext.clear();
        logContext.setTxRquid(UUIDUtil.randomUUID(true));
        logContext.setProgramFlowType(ProgramFlow.CBSTimeOutRerunIn);
        logContext.setMessageFlowType(MessageFlow.Request);
        logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        logContext.setMessage(messageIn);
        logContext.setRemark("CBSTimeOutRerun Receive Request");
        logContext.setEj(TxHelper.generateEj());
        this.logMessage(logContext);
        try {
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }

            //3. 讀取上傳主機逾時明細檔
            Cbspend cbspend = selectCBSPEND(messageIn);

            Object tota = null;
            //4. 交易狀態查詢
            if (cbspend != null) {
                tota = this.getTransationStatus(cbspend);
            } else {
                //取不到 Cbspend 印出log後離開，取下一筆資料
                logContext.setProgramName(ProgramName + ".dispatch");
                logContext.setMessage("cbspend is null");
                logMessage(logContext);
                return; // go to 2
            }

            if (tota == null) {
                logContext.setProgramName(ProgramName + ".dispatch");
                logContext.setMessage("tota is null");
                logMessage(logContext);
                if (cbspend != null) {
                    messageOut = String.format("%s:%s", cbspend.getCbspendTxDate(), Integer.toString(cbspend.getCbspendEjfno()));
                }
                if (StringUtils.isNotBlank(messageOut)) {
                    FeeBackSuccess(messageOut, logContext);
                }
                return; // go to 2
            }

            if (isEnd) {
                // 已處理完成 離開程式
                return;
            }

            //5. 重送入帳或沖正交易
            String rtn = this.resendAccount(cbspend, tota);
            if (rtn.equals("error")) {
                messageOut = String.format("%s:%s", cbspend.getCbspendTxDate(), Integer.toString(cbspend.getCbspendEjfno()));
            }

            if (StringUtils.isNotBlank(messageOut)) {
                FeeBackSuccess(messageOut, logContext);
            }


        } catch (Exception e) {
            // sendEMS
            logContext.setMessage(messageIn);
            logContext.setProgramName(StringUtils.join(ProgramName, ".messageReceived"));
            logContext.setRemark(e.getMessage());
            logContext.setProgramException(e);
            logContext.setServiceUrl("/CBSPEND/recv");
            logMessage(Level.ERROR, logContext);
            // 2025-06-04 Richard modified 這里不要再throw, 因為預設會包Transaction, 否則會rollback一直loop
            // throw ExceptionUtil.createRuntimeException(e);
        } finally {
            logContext.setMessage(messageOut);
            logContext.setProgramName(StringUtils.join(ProgramName, ".messageReceived"));
            logContext.setProgramFlowType(ProgramFlow.CBSTimeOutRerunOut);
            logContext.setMessageFlowType(MessageFlow.Response);
            logContext.setRemark("CBSTimeOutRerun Receive Response");
            this.logMessage(logContext);
            if (StringUtils.isNotBlank(messageOut))
                LogHelperFactory.getTraceLogger().trace(SvrConst.SVR_CBSTimeOutRerun, " Send msg:", messageOut);
        }
        return;
    }


    /**
     * 3. 讀取上傳主機逾時明細檔
     *
     * @param data
     * @return
     * @throws ParseException
     */
    public Cbspend selectCBSPEND(String data) throws ParseException {
        String[] datas = data.split(":");

        // Queue內容不符合預期，不處理
        if (datas.length != 2) {
            logContext.setProgramName(ProgramName + ".selectCBSPEND");
            logContext.setRemark("字串拆解不符合預期");
            logMessage(logContext);
            return null;
        }

        //拆解Queue
        String cbspendTxDate = datas[0];
        int cbspendEjfno = Integer.parseInt(datas[1]);

        // 讀取上傳主機逾時明細檔
        return this.loadCbspend(cbspendTxDate, cbspendEjfno);
    }

    /**
     * 讀取上傳主機逾時明細檔
     *
     * @param cbspendTxDate
     * @param cbspendEjfno
     * @return
     * @throws ParseException
     */
    private Cbspend loadCbspend(String cbspendTxDate, Integer cbspendEjfno) throws ParseException {
        Cbspend cbspend = this.cbspendExtMapper.selectByPrimaryKey(cbspendTxDate, cbspendEjfno);

        // 查無資料
        if (cbspend == null) {
            logContext.setProgramException(new RuntimeException("CBSPEND無資料"));
            logContext.setProgramName(ProgramName + ".loadCbspend");
            logMessage(Level.ERROR, logContext);
            return null;
        }

        // 判斷重送記號(CBSPEND_SUCCESS_FLAG)
        if (cbspend.getCbspendSuccessFlag() != 0) {
            logContext.setProgramName(ProgramName + ".loadCbspend");
            logContext.setRemark("CBSPEND 重送記號 不為0, Cbspend Success Flag:" + cbspend.getCbspendSuccessFlag());
            logMessage(logContext);
            return null;
        }

        // 判斷執行時間
        Date txDate = new SimpleDateFormat("yyyyMMdd").parse(cbspend.getCbspendTxDate());
        Sysstat sysstat = this.sysstatExtMapper.selectFirstByLbsdyFisc();
        Date lbsdyDate = new SimpleDateFormat("yyyyMMdd").parse(sysstat.getSysstatLbsdyFisc());
        //印出取得的時間
        logContext.setProgramName(ProgramName + ".loadCbspend");
        logContext.setRemark("Cbspend Tx Date:" + txDate + ", SysstatLbsdyFisc:" + lbsdyDate);
        logMessage(logContext);
        // 2021/2/25 判斷執行時間不得超過財金前營業日
        if (txDate.compareTo(lbsdyDate) < 0) {
            // 小於前一財金營業日, 不重送
            // 更新重送記號
            cbspend.setCbspendSuccessFlag((short) 2); // 不再重送
            logContext.setProgramName(ProgramName + ".loadCbspend");
            logContext.setRemark("EJ:" + cbspend.getCbspendEjfno() + ", 將重送記號更新為 2.");
            logMessage(logContext);
            int updateCount = this.cbspendExtMapper.updateByPrimaryKeySelective(cbspend);
            if (updateCount == 0) {
                logContext.setProgramException(new RuntimeException("CBSPEND更新失敗-不再重送"));
                logContext.setProgramName(ProgramName);
                logMessage(Level.ERROR, logContext);
            }
            return null;
        }

        // 判斷重送次數
        Short resendCnt = cbspend.getCbspendResendCnt();
        logContext.setProgramName(ProgramName + ".loadCbspend");
        logContext.setRemark("EJ:" + cbspend.getCbspendEjfno() + ", 目前重送次數為" + resendCnt);
        logMessage(logContext);
        if (resendCnt >= 5) {
            logContext.setProgramException(new RuntimeException("CBSPEND_RESEND_CNT=5 不回寫該筆 CBSPEND QUEUE"));
            logContext.setProgramName(ProgramName);
            logMessage(Level.ERROR, logContext);
            // 8/21 刪除, 執行到5次, 不回寫 CBSPEND Queue
            return null;
        }

        return cbspend;
    }

    /**
     * 4. 交易狀態查詢
     *
     * @param
     * @return tota
     * @throws Exception
     */
    private Object getTransationStatus(Cbspend cbspend) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;

        // 上送查詢電文
        String tita = this.makeCBSTita(cbspend);
        logContext.setProgramName(ProgramName + ".getTransationStatus");
        logContext.setMessage("tita:" + tita);
        logMessage(logContext);

        // 送IMS
        CBSAdapter adapter = new CBSAdapter(new INBKData());
        if (StringUtils.equalsAny(cbspend.getCbspendCbsKind(),"A0","A1","A2")) {
            adapter.setCbsType(CBSType.FISCTCB);
        }else {
            adapter.setCbsType(CBSType.CBS);
        }
        adapter.setCbsId(StringUtils.leftPad(cbspend.getCbspendEjfno().toString(), 8, '0'));

        adapter.setMessageToCBS(tita);
        rtnCode = adapter.sendReceive();
        logContext.setProgramName(ProgramName + ".getTransationStatus");
        logContext.setMessage("after send To CBS, RC:" + rtnCode.toString());
        logMessage(logContext);
        // CBS TIMEOUT OR OTHER ERROR
        if (rtnCode != FEPReturnCode.Normal) {
            // 執行處理TIMEOUT Routine
            logContext.setProgramName(ProgramName + ".getTransationStatus");
            logContext.setMessage("TIMEOUT Routine");
            logMessage(logContext);
            this.doTimeOutRoutine(cbspend);
            return null;
        }

        //拆解電文
        Object tota = null;
        tota = parseCbsTota(adapter.getMessageFromCBS());

        logContext.setProgramName(ProgramName + ".getTransationStatus");
        logContext.setMessage("tota:" + tota);
        logMessage(logContext);

        if (tota == null) {
            logContext.setProgramName(ProgramName + ".getTransationStatus");
            logContext.setMessage("tota is null");
            logMessage(logContext);
            return null;
        }

        isEnd = false; //初始化
        if (StringUtils.equals("4001", this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue()))) {
            // 取CBIQTXO001 欄位值
            // 以下更新 CBSPEND & FEPTXN 需做 TRANSACTION
            String TX_ACCT_FLAG = this.getImsPropertiesValue(tota, ImsMethodName.TX_ACCT_FLAG.getValue());
            String IMS_RC3_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC3_TCB.getValue());
            String IMS_RC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC4_FISC.getValue());
            String TX_RVS_FLAG = this.getImsPropertiesValue(tota, ImsMethodName.TX_RVS_FLAG.getValue());
            String IMS_PENDING = this.getImsPropertiesValue(tota, ImsMethodName.IMS_PENDING.getValue());

            logContext.setRemark("CbsKind: "+cbspend.getCbspendCbsKind()+",ReverseFlag: "+cbspend.getCbspendReverseFlag()+",TX_RVS_FLAG: "+TX_RVS_FLAG+",TX_ACCT_FLAG: "+ TX_ACCT_FLAG);
            logMessage(logContext);

            /* 2025/7/30 修改 */
            /*  原存Req扣帳交易, 只查詢交易結果，不需重送
                原存 Con入扣帳及沖正交易, 主機回覆交易結束, 不需重送 */
            if ( StringUtils.equals("A0", cbspend.getCbspendCbsKind()) ||
                    ((StringUtils.equals("A1", cbspend.getCbspendCbsKind()) || StringUtils.equals("A2", cbspend.getCbspendCbsKind())) &&
                    StringUtils.equals("N", IMS_PENDING))
            ) {
                cbspend.setCbspendSuccessFlag((short) 1); // 重送成功
                cbspend.setCbspendResendCnt((short) (cbspend.getCbspendResendCnt() + 1));
                cbspend.setCbspendAccType((short) 1); // 已記帳
                cbspend.setCbspendCbsRc(IMS_RC4_FISC); // 成功

                if ( StringUtils.equals(TX_ACCT_FLAG,"N") ) {
                    cbspend.setCbspendAccType((short) 0); // 未記帳
                }else{
                    if ( StringUtils.equals(TX_RVS_FLAG,"Y") ){
                        cbspend.setCbspendAccType((short) 2); // 已沖正
                    }else{
                        cbspend.setCbspendAccType((short) 1); // 已記帳
                    }
                }

                // 更新CBSPEND、執行更新 FEPTXN Routine
                String rtn = this.updateFeptxnRoutine(cbspend, tota);
                if (rtn.equals("error")) {
                    return null;
                }
                isEnd = true;
            }

            /* 代理類入扣帳交易, 只更新交易紀錄，不需重送 */
            if (StringUtils.equals("A3", cbspend.getCbspendCbsKind())) { /*代理類交易*/
                cbspend.setCbspendSuccessFlag((short) 1); /* 重送成功 */
                cbspend.setCbspendResendCnt((short) (cbspend.getCbspendResendCnt() + 1));
                cbspend.setCbspendCbsRc(IMS_RC4_FISC);

                if ( StringUtils.equals(TX_ACCT_FLAG,"N") ) {
                    cbspend.setCbspendAccType((short) 0); // 未記帳
                }else{
                    if ( StringUtils.equals(TX_RVS_FLAG,"Y") ){
                        cbspend.setCbspendAccType((short) 2); // 已沖正
                    }else{
                        cbspend.setCbspendAccType((short) 1); // 已記帳
                    }
                }

                // 更新CBSPEND、執行更新 FEPTXN Routine
                String rtn = this.updateFeptxnRoutine(cbspend, tota);
                if (rtn.equals("error")) {
                    return null;
                }
                isEnd = true;
            }

            /* 代理類沖正交易, 主機回覆交易結束不需重送,，更新交易紀錄 */
            if (StringUtils.equals("A4", cbspend.getCbspendCbsKind())
                    && StringUtils.equals(IMS_PENDING,"N")
            ) {
                cbspend.setCbspendSuccessFlag((short) 1); /* 重送成功 */
                cbspend.setCbspendResendCnt((short) (cbspend.getCbspendResendCnt() + 1));
                cbspend.setCbspendCbsRc(IMS_RC4_FISC);

                if (StringUtils.equals("N", TX_ACCT_FLAG)){
                    cbspend.setCbspendAccType((short) 0); //未記帳
                }else{
                    if (StringUtils.equals("Y", TX_RVS_FLAG)) {
                        cbspend.setCbspendAccType((short) 2); //已沖正
                    }else{
                        cbspend.setCbspendAccType((short) 1); //已記帳
                    }
                }
                // 更新CBSPEND、執行更新 FEPTXN Routine
                String rtn = this.updateFeptxnRoutine(cbspend, tota);
                if (rtn.equals("error")) {
                    return null;
                }
                isEnd = true;
            }
        }else{ //紀錄IMS回應的錯誤說明，取CBIQTXO002 欄位值
            String OUTRTC = this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue());
            String MEMO = this.getImsPropertiesValue(tota, ImsMethodName.MEMO.getValue());
            if ( MEMO!=null && StringUtils.isNotBlank(MEMO)  ){
                cbspend.setCbspendCbsRc(OUTRTC);
                /*MEMO使用字霸EBCDIC->ASCII*/
                byte[] MEMO_ebcdic_Bytes = hexStringToByteArray(MEMO);
                String MEMO_ASCII_String = AStarLoadUtils.convertIBM937BytesToUnicodeStr(MEMO_ebcdic_Bytes);
                if (MEMO_ASCII_String != null)
                    cbspend.setCbspendMemo( MEMO_ASCII_String.trim() );
                try {
                    int updateCount = this.cbspendExtMapper.updateByPrimaryKeySelective(cbspend);
                    if (updateCount == 0 ) {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    logContext.setProgramException(e);
                    logContext.setMessage("UPDATE CBSPEND ERROR.");
                    logContext.setProgramName(ProgramName);
                    logMessage(Level.ERROR, logContext);
                    return "error";
                }
            }
        }
        return tota;
    }

    public String makeCBSTita(Cbspend cbspend) throws Exception {
        CB_IQTX_I001 cbsTita = new CB_IQTX_I001();
        //header
        cbsTita.setINTRAN("EAII");
        String MSGID = StringUtils.rightPad(cbspend.getCbspendTxDate() + cbspend.getCbspendTxTime() + StringUtils.leftPad(String.valueOf(cbspend.getCbspendEjfno()), 8, "0"), 24, " ");
        cbsTita.setINMSGID(MSGID);
        cbsTita.setINDATE(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        cbsTita.setINTIME(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
        cbsTita.setINSERV("FEP1");
        cbsTita.setMQIDCNV("N");    // 讓主機回送FEP上送的cbsid
        cbsTita.setINTD("910A0037");
        cbsTita.setINAP("ATFEP");
        cbsTita.setINFF("F");
        cbsTita.setINPGNO("001");
        cbsTita.setINV1CT("0000");

        //body
        cbsTita.setINQ_ACQ_BID(cbspend.getCbspendBkno());
        cbsTita.setINQ_TX_STAN(cbspend.getCbspendStan());
        cbsTita.setINQ_FG_TXDATE(CalendarUtil.adStringToROCString(cbspend.getCbspendTbsdyFisc())); //轉民國年
//        cbsTita.setINSTAN(cbspend.getCbspendStan());

        String tita = cbsTita.makeMessage();
        String AsciiTita = cbsTita.makeMessageAscii();

        //LOG
        logContext.setProgramName(ProgramName + ".makeCBSTita");
        logContext.setMessage("MessageToCBS:" + tita);
        logMessage(logContext);

        logContext.setProgramName(ProgramName + ".makeCBSTita");
        logContext.setMessage("Ascii MessageToCBS:" + AsciiTita);
        logMessage(logContext);

        return tita;
    }

    public Object parseCbsTota(String message) throws Exception {
        if (StringUtils.isNotBlank(message)) {
            boolean is4001 = false;
            String TotaStr = StringUtils.EMPTY;
            String AsciiTotaStr = StringUtils.EMPTY;
            CB_IQTX_O001 tota = new CB_IQTX_O001();
            CB_IQTX_O002 tota2 = new CB_IQTX_O002();
            String OUTRTC = message.substring(176, 184);
            logContext.setMessage("OUTRTC:" + OUTRTC);
            logMessage(logContext);
            if (StringUtils.equals("F4F0F0F1", OUTRTC)) {
                tota.parseCbsTele(message);
                TotaStr = tota.makeMessage();
                AsciiTotaStr = tota.makeMessageAscii();
                is4001 = true;
            } else {
                tota2.parseCbsTele(message);
                TotaStr = tota2.makeMessage();
                AsciiTotaStr = tota2.makeMessageAscii();
            }
            logContext.setMessage("MessageFromCBS:" + TotaStr);
            logMessage(logContext);
            logContext.setMessage("Ascii MessageFromCBS:" + AsciiTotaStr);
            logMessage(logContext);
            if (is4001) {
                return tota;
            } else {
                return tota2;
            }
        }
        return null;
    }

    /**
     * 更新重送次數
     *
     * @throws Exception
     */
    private void doTimeOutRoutine(Cbspend cbspend) throws Exception {
        cbspend.setCbspendResendCnt((short) (cbspend.getCbspendResendCnt() + 1));
        cbspend.setCbspendAccType((short) 4); // 未明
        cbspend.setCbspendCbsRc(StringUtils.EMPTY);
        try {
            int updateCount = this.cbspendExtMapper.updateByPrimaryKeySelective(cbspend);
            if (updateCount == 0) {
                // 回寫該筆 CBSPEND QUEUE
                logContext.setProgramException(new RuntimeException("TIMEOUT Routine 更新 CBSPEND失敗"));
                logContext.setProgramName(ProgramName);
                logMessage(Level.ERROR, logContext);
            }
        } catch (Exception e) {
            // 回寫該筆 CBSPEND QUEUE
            logContext.setProgramException(new RuntimeException("TIMEOUT Routine 更新 CBSPEND失敗"));
            logContext.setProgramName(ProgramName);
            logMessage(Level.ERROR, logContext);
        }
    }

    /**
     * 更新 4FEPTXN Routine
     *
     * @param
     * @throws Exception
     */
    private String updateFeptxnRoutine(Cbspend cbspend, Object tota) throws Exception {
        Feptxn feptxn = null;
        feptxn = this.feptxnDao.selectByPrimaryKey(cbspend.getCbspendTxDate(), cbspend.getCbspendEjfno());
        if (feptxn == null) {
            logContext.setProgramException(new RuntimeException("FEPTXN NOT FOUND"));
            logContext.setProgramName(ProgramName);
            logMessage(Level.ERROR, logContext);
            return "error";
        }
        String imsbusinessDate = this.getImsPropertiesValue(tota, ImsMethodName.IMSBUSINESS_DATE.getValue());
        if ("0000000".equals(imsbusinessDate) || "000000".equals(imsbusinessDate)
                || (imsbusinessDate.length() != 6 && imsbusinessDate.length() != 7)) { // "0000000"不是日期格式，民國轉西元會轉成""，因此特殊處理
            imsbusinessDate = "00000000";
        } else {
            imsbusinessDate = CalendarUtil.rocStringToADString14(imsbusinessDate);
        }
        feptxn.setFeptxnTbsdy(imsbusinessDate);
        feptxn.setFeptxnBrno(this.getImsPropertiesValue(tota, ImsMethodName.IMS_FMMBR.getValue()));
        feptxn.setFeptxnTrinBrno(this.getImsPropertiesValue(tota, ImsMethodName.IMS_TMMBR.getValue()));
        feptxn.setFeptxnAccType(cbspend.getCbspendAccType()); // 記帳別
        feptxn.setFeptxnCbsTimeout((short) 0);
        feptxn.setFeptxnCbsRc(cbspend.getCbspendCbsRc());
        /* 2025/7/29 修改 for 原存扣帳類交易(Req) 主機已記帳, 須將交易改為 PENDING,發 2280 至財金 */
        if ( StringUtils.equals("A0", cbspend.getCbspendCbsKind()) && cbspend.getCbspendAccType()==1 ){
            feptxn.setFeptxnPending((short) 1);
            feptxn.setFeptxnMsgflow("F2");
            feptxn.setFeptxnRepRc("4001");
            feptxn.setFeptxnTxrust("B");
        }else if ( StringUtils.equals("A3", cbspend.getCbspendCbsKind()) ) {    //IMS REQ電文
            feptxn.setFeptxnPending((short) 1);
            if (cbspend.getCbspendAccType() == 1) {
                feptxn.setFeptxnTxrust("A");
            }else if (cbspend.getCbspendAccType() == 2){
                feptxn.setFeptxnTxrust("C");
            }else if (!StringUtils.equals( "ABWD",StringUtils.left(cbspend.getCbspendCbsTxCode(),4))){  //非提款
                feptxn.setFeptxnTxrust("R");
            }
        }else if ( StringUtils.equals("A4", cbspend.getCbspendCbsKind()) ){   //IMS提款CON電文
            feptxn.setFeptxnPending((short) 1);
            if (cbspend.getCbspendAccType() == 1) {
                feptxn.setFeptxnTxrust("A");
            }else if (cbspend.getCbspendAccType() == 2){
                feptxn.setFeptxnTxrust("C");
            }else {
                feptxn.setFeptxnTxrust("R");
            }
        }

        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            int updateCount = this.cbspendExtMapper.updateByPrimaryKeySelective(cbspend);
            int count = feptxnDao.updateByPrimaryKeySelective(feptxn);
            if (updateCount == 0 || count == 0) {
                // 回寫該筆 CBSPEND QUEUE
                throw new Exception();
            }
            transactionManager.commit(txStatus);
        } catch (Exception e) {
            // 回寫該筆 CBSPEND QUEUE
            transactionManager.rollback(txStatus);
            logContext.setProgramException(e);
            logContext.setMessage("UPDATE CBSPEND OR FEPTXN ERROR.");
            logContext.setProgramName(ProgramName);
            logMessage(Level.ERROR, logContext);
            return "error";
        }

        return "";
    }

    /**
     * 5. 重送入帳或沖正交易
     *
     * @param
     * @param tota
     * @throws Exception
     */
    private String resendAccount(Cbspend cbspend, Object tota) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.ProgramException;
        logContext.setProgramName(ProgramName + ".resendAccount");
        logContext.setMessage("重送入帳或沖正交易");
        logMessage(logContext);

        CBSAdapter adapter = new CBSAdapter(new INBKData());
        if (StringUtils.equalsAny(cbspend.getCbspendCbsKind(),"A0","A1","A2")) {
            adapter.setCbsType(CBSType.FISCTCB);
        }else {
            adapter.setCbsType(CBSType.CBS);
        }
        adapter.setCbsId(StringUtils.leftPad(cbspend.getCbspendEjfno().toString(), 8, '0'));

        String TX_ACCT_FLAG = this.getImsPropertiesValue(tota, ImsMethodName.TX_ACCT_FLAG.getValue());
        String TX_RVS_FLAG = this.getImsPropertiesValue(tota, ImsMethodName.TX_RVS_FLAG.getValue());
        String IMS_PENDING = this.getImsPropertiesValue(tota, ImsMethodName.IMS_PENDING.getValue());

        /*主機回覆PENDING, FEP重送入帳/沖正電文至CBS主機*/
        /* 2025/7/29 修改 */
        if (StringUtils.equals("Y", IMS_PENDING) &&
                (StringUtils.equals("A1", cbspend.getCbspendCbsKind()) || StringUtils.equals("A2", cbspend.getCbspendCbsKind())) ){
            // 需重送入帳電文至CBS主機

            String CBSPEND_EJFNO = StringUtils.leftPad(cbspend.getCbspendEjfno().toString(), 8, '0');
            if ( !StringUtils.equals( EbcdicConverter.toHex(CCSID.English, 8, CBSPEND_EJFNO),StringUtils.substring(cbspend.getCbspendTita(), 52, 68)) ) {
                /* 2025/7/22 修改, 將上送主機電文FEP_EJNO內容改為 CBSPEND_EJFNO */
                cbspend.setCbspendTita(StringUtils.substring(cbspend.getCbspendTita(), 0, 52) + EbcdicConverter.toHex(CCSID.English, 8, CBSPEND_EJFNO)
                        + StringUtils.substring(cbspend.getCbspendTita(), 68, cbspend.getCbspendTita().length()));

                /* CALL ENC 重押主機MAC資料(用EBCDIC編碼值) */
                /* 更新Tita MAC 電文第 116~120(ASCII) / 232~240(EBCDIC)位置值 */
                RefString cbsmac = new RefString("");
                rtnCode = this.makeCBSMac(cbsmac, cbspend.getCbspendTita().substring(0, 166));
                if (rtnCode != FEPReturnCode.Normal) {
                    this.logContext.setRemark("make cbsmac EBCDIC fail.");
                    logMessage(this.logContext);
                }
                this.logContext.setRemark("make cbsmac EBCDIC:" + cbsmac.get());
                logMessage(this.logContext);

                /* 更新Tita 電文第 116~120(ASCII) 位置值 */
                StringBuilder resultBuilder = new StringBuilder(cbspend.getCbspendTita());
                resultBuilder.replace(232, 240, cbsmac.substring(0, 8));
                cbspend.setCbspendTita(resultBuilder.toString());
                this.logContext.setMessage("after makeCBSMac RC:" + rtnCode.toString());
                logMessage(this.logContext);
            }

            adapter.setMessageToCBS(cbspend.getCbspendTita());
            rtnCode = adapter.sendReceive();
            logContext.setProgramName(ProgramName + ".resendAccount");
            logContext.setMessage("after send to CBS, RC:" + rtnCode);
            logContext.setRemark("重送入帳或沖正電文至CBS主機.");
            logMessage(logContext);
        }

        /*  ATM 提款沖正需重送沖正電文給主機 */
        if (StringUtils.equals("A4", cbspend.getCbspendCbsKind()) && StringUtils.equals("Y", IMS_PENDING)) {
            /* 需重送主機 */
            String CBSPEND_EJFNO = StringUtils.leftPad(cbspend.getCbspendEjfno().toString(), 8, '0');
            if ( !StringUtils.equals( EbcdicConverter.toHex(CCSID.English, 8, CBSPEND_EJFNO),StringUtils.substring(cbspend.getCbspendTita(), 52, 68)) ) {
                /* 2025/7/22 修改, 將上送主機電文FEP_EJNO內容改為 CBSPEND_EJFNO */
                cbspend.setCbspendTita(StringUtils.substring(cbspend.getCbspendTita(), 0, 52) + EbcdicConverter.toHex(CCSID.English, 8, CBSPEND_EJFNO)
                        + StringUtils.substring(cbspend.getCbspendTita(), 68, cbspend.getCbspendTita().length()));

                /* CALL ENC 重押主機MAC資料(用EBCDIC編碼值) */
                /* 更新Tita MAC 電文第 116~120(ASCII) / 232~240(EBCDIC)位置值 */
                RefString cbsmac = new RefString("");
                rtnCode = this.makeCBSMac(cbsmac, cbspend.getCbspendTita().substring(0, 166));
                if (rtnCode != FEPReturnCode.Normal) {
                    this.logContext.setRemark("make cbsmac EBCDIC fail.");
                    logMessage(this.logContext);
                }
                this.logContext.setRemark("make cbsmac EBCDIC:" + cbsmac.get());
                logMessage(this.logContext);

                /* 更新Tita 電文第 116~120(ASCII) 位置值 */
                StringBuilder resultBuilder = new StringBuilder(cbspend.getCbspendTita());
                resultBuilder.replace(232, 240, cbsmac.substring(0, 8));
                cbspend.setCbspendTita(resultBuilder.toString());
                this.logContext.setMessage("after makeCBSMac RC:" + rtnCode.toString());
                logMessage(this.logContext);
            }

            adapter.setMessageToCBS(cbspend.getCbspendTita());
            rtnCode = adapter.sendReceive();
            logContext.setProgramName(ProgramName + ".resendAccount");
            logContext.setMessage("after send to CBS, RC:" + rtnCode);
            logContext.setRemark("ATM 提款沖正需重送沖正電文給主機.");
            logMessage(logContext);
        }

        if (rtnCode != FEPReturnCode.Normal) {
            // 執行處理TIMEOUT Routine
            this.doTimeOutRoutine(cbspend);
            logContext.setProgramName(ProgramName + ".resendAccount");
            logContext.setMessage("執行處理TIMEOUT Routine");
            logMessage(logContext);
            return "error";
        }

        //拆解電文
        String response = adapter.getMessageFromCBS();
        if (response == null) {
            logContext.setProgramName(ProgramName + ".resendAccount");
            logContext.setMessage("取不到主機回應電文.");
            logMessage(logContext);
            return null;
        }

        // 重送電文的訊息
        String IMSRC4FISC = EbcdicConverter.fromHex(CCSID.English, StringUtils.substring(response, 72, 80)); // 4碼
        String IMSRCTCB = EbcdicConverter.fromHex(CCSID.English, StringUtils.substring(response, 80, 86)); // 3碼
        String IMSRC = StringUtils.EMPTY;
        String CbsKind = cbspend.getCbspendCbsKind();
        String ReSend_TX_ACCT_FLAG = EbcdicConverter.fromHex(CCSID.English, StringUtils.substring(response, 100, 102));
        String ReSend_TX_RVS_FLAG = EbcdicConverter.fromHex(CCSID.English, StringUtils.substring(response, 102, 104));

        // IMSRCTCB = "000" or empty 表交易成功
        if (StringUtils.isBlank(IMSRCTCB)) {
            IMSRCTCB = "000";
        }
        //CBS錯誤代碼依判斷放在暫存變數IMSRC
        if ( StringUtils.equals(cbspend.getCbspendChannel(),"ATM") && cbspend.getCbspendSubsys()==3 ) {
            if ( StringUtils.equals(IMSRC4FISC,"4001")){
                IMSRC = IMSRCTCB;   //ATM自行給3碼
            }else{
                IMSRC = IMSRC4FISC;
            }
        }else{
            IMSRC = IMSRC4FISC;
        }
        /* 2025/7/30 修改, 沖正交易主機回應代碼000/4001表交易成功 */
        if (StringUtils.equals(IMSRC, "000") || StringUtils.equals(IMSRC, "4001")) { /* 主機回應交易成功 */
            /*以下更新 CBSPEND & FEPTXN 需做 TRANSACTION*/
            cbspend.setCbspendSuccessFlag((short) 1); // 重送成功
            /* 2025/7/30 修改, 原存Con電文入帳交易 */
            if (StringUtils.equals("A1", CbsKind) || StringUtils.equals("A4", CbsKind)) { // 入帳類交易
                if (StringUtils.equals("Y", ReSend_TX_ACCT_FLAG) && StringUtils.equals("N", ReSend_TX_RVS_FLAG)){
                    cbspend.setCbspendAccType((short) 1); // 已記帳
                }
            }
        } else if ( "XXX".equals(IMSRC) || "XXXX".equals(IMSRC)) {      // 主機回應Garble不處理
            cbspend.setCbspendSuccessFlag((short) 1); // 重送成功
            cbspend.setCbspendAccType((short) 3);
            logContext.setProgramName(ProgramName + ".resendAccount");
            logContext.setMessage("主機下送Garble，不再重送.");
            logMessage(logContext);
        } else { // 失敗
            /* 2025/7/30 修改, 原存/代理Con電文沖正交易 */
            if( StringUtils.equals("A2", CbsKind) ||  StringUtils.equals("A4", CbsKind)){
                if( StringUtils.equals("Y", ReSend_TX_ACCT_FLAG) && StringUtils.equals("Y", ReSend_TX_RVS_FLAG) ){
                    cbspend.setCbspendAccType((short) 2);       // 已沖正
                    cbspend.setCbspendSuccessFlag((short) 1);   // 重送成功
                }else{
                    cbspend.setCbspendAccType((short) 3);       // 更正or轉入失敗
                }
            }
        }
        cbspend.setCbspendResendCnt((short) (cbspend.getCbspendResendCnt() + 1));
        cbspend.setCbspendCbsRc(IMSRC);
        // 執行更新 CBSPEND && FEPTXN Routine 需做 TRANSACTION

        String rtn = this.updateFeptxnRoutine(cbspend, response, IMSRC);
        if (rtn.equals("error")) {
            return "error";
        }
        return "";

    }

    /**
     * makeCBSMac
     *
     * @param
     * @throws Exception
     */
    private FEPReturnCode makeCBSMac(RefString cbsmac,String inputData) {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        FISCData txData = new FISCData();
        try {
            // 1.建立ENCHelper物件
            ENCHelper encHelper = new ENCHelper(txData);

            String inputData1 = inputData;
            inputData1 = remainderToF0(inputData1);
            // 3.呼叫
            RefString mac = new RefString(null);
            rtnCode = encHelper.makeCbsMacNew(inputData1, mac);
            // 4.若rtnCode=normal, 則cbsmac = mac
            if (rtnCode == FEPReturnCode.Normal) {
                cbsmac.set(mac.get());
            }
            this.logContext.setRemark("after makeCBSMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
        } catch (Exception e) {
//			rtnCode = handleException(e, "makeCBSMac");
        }
        return rtnCode;
    }

    /**
     * 把字串補足至 8 的倍數 ，不足以F0補足
     *
     * @return
     */
    private String remainderToF0(String inputData) {
        String rtnStr = "";
        int instr = inputData.length();
        instr = instr / 2;
        if (instr % 8 != 0) {
            int remainder = instr % 8;
            remainder = 8 - remainder;
            rtnStr = StringUtils.rightPad(rtnStr, remainder, '0');
            rtnStr = EbcdicConverter.toHex(CCSID.English, rtnStr.length(), rtnStr);
        }

        return inputData + rtnStr;
    }

    /**
     * 更新 5FEPTXN Routine
     *
     * @param
     * @throws Exception
     */
    private String updateFeptxnRoutine(Cbspend cbspend, String response, String IMSRC) throws Exception {
        // 讀取 FEPTXN (檔名 SEQ 為CBSPEND_TBSDY_FISC [7:2] )
        /* 如果CBSPEND_ CBS_ KIND = ‘A4’，UPDATE 條件請用 FEPTXN_TRACE_EJFNO */
        Feptxn feptxn = null;
        feptxn = this.feptxnDao.selectByPrimaryKey(cbspend.getCbspendTxDate(), cbspend.getCbspendEjfno());
        if (feptxn == null) {
            logContext.setProgramException(new RuntimeException("FEPTXN Routine Query FEPTXN 失敗"));
            logContext.setProgramName(ProgramName);
            logMessage(Level.ERROR, logContext);
            return "error";
        }
        String imsbusinessDate = EbcdicConverter.fromHex(CCSID.English, StringUtils.substring(response, 86, 100));
        if ("0000000".equals(imsbusinessDate) || "000000".equals(imsbusinessDate)
                || (imsbusinessDate.length() != 6 && imsbusinessDate.length() != 7)) { // "0000000"不是日期格式，民國轉西元會轉成""，因此特殊處理
            imsbusinessDate = "00000000";
        } else {
            imsbusinessDate = CalendarUtil.rocStringToADString14(imsbusinessDate);
        }
        feptxn.setFeptxnTbsdy(imsbusinessDate);
        feptxn.setFeptxnBrno(EbcdicConverter.fromHex(CCSID.English, StringUtils.substring(response, 136, 144)));
        feptxn.setFeptxnTrinBrno(EbcdicConverter.fromHex(CCSID.English, StringUtils.substring(response, 144, 152)));
        feptxn.setFeptxnAccType(cbspend.getCbspendAccType()); // 記帳別
        feptxn.setFeptxnCbsTimeout((short) 0);
        feptxn.setFeptxnCbsRc(cbspend.getCbspendCbsRc());

        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            int updateCount = this.cbspendExtMapper.updateByPrimaryKeySelective(cbspend); // 將原入帳或沖正的 cbspend 移入至此
            if (updateCount == 0) {
                logContext.setProgramException(new RuntimeException("T24回應OK, 更新 CBSPEND失敗"));
                logContext.setProgramName(ProgramName);
                logMessage(Level.ERROR, logContext);
                logMessage(logContext);
                return "error";
            }
            if ( "XXX".equals(IMSRC) || "XXXX".equals(IMSRC)) {
                //跳過 FEPTXN 更新
            }else{
                int count = feptxnDao.updateByPrimaryKeySelective(feptxn);
                if (count == 0) {
                    logContext.setProgramException(new RuntimeException("FEPTXN Routine 更新 FEPTXN失敗"));
                    logContext.setProgramName(ProgramName);
                    logMessage(Level.ERROR, logContext);
                    return "error";
                }
            }
            transactionManager.commit(txStatus);
        } catch (Exception e) {
            logContext.setProgramException(new RuntimeException("FEPTXN Routine 更新 FEPTXN失敗"));
            logContext.setProgramName(ProgramName);
            logMessage(Level.ERROR, logContext);
            return "error";
        }finally {
            if ( !txStatus.isCompleted()) {
                transactionManager.rollback(txStatus);
            }
        }

        return "";
    }

    private boolean FeeBackSuccess(String returnStr, LogData logContext) {
        int W_MS = 5000;
        try {
            Thread.sleep(W_MS); // 重送 Waiting wMinute 5s
            logContext.setRemark("重送 Waiting wMinute " + W_MS/1000 + "s");
            logMessage(logContext);

            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
            sender.sendQueue(configuration.getQueueNames().getCbspend().getDestination(), returnStr, null, null);
            return true;
        } catch (Exception ex) {
            // sendEMS
            logContext.setProgramName(StringUtils.join(ProgramName, ".FeeBackSuccess"));
            logContext.setRemark(ex.getMessage());
            logContext.setProgramException(ex);
            logMessage(Level.ERROR, logContext);
            return false;
        }
    }

    /**
     * 2025-03-07 Richard add
     *
     * @param value
     * @return
     */
    public String addCBSTimeoutRerunMessage(String value) {
        putMDC();
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.INBK);
        logData.setChannel(FEPChannel.CBS);
        logData.setProgramFlowType(ProgramFlow.ChannelGWOut);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setProgramName(StringUtils.join(ProgramName, ".addCBSTimeoutRerunMessage"));
        logData.setRemark(StringUtils.join(SvrConst.SVR_CBSTimeOutRerun, " begin add messages to cbspend"));
        this.logMessage(logData);
        JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
        JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
        String[] ary = value.split("\\|");
        int cnt = 0;
        // MsMessage<String> msg = new MsMessage<>("CBSPEND", null);
        for (String s : ary) {
            // msg.setBody(s);
            try {
                // sender.sendQueue(configuration.getQueueNames().getCbspend().getDestination(), msg, null, null);
                sender.sendQueue(configuration.getQueueNames().getCbspend().getDestination(), s, null, null);
                cnt++;
            } catch (Exception e) {
                logData.setProgramName(StringUtils.join(ProgramName, ".addCBSTimeoutRerunMessage"));
                logData.setProgramException(e);
                logMessage(Level.ERROR, logData);
            }
        }
        logData.setSubSys(SubSystem.INBK);
        logData.setChannel(FEPChannel.CBS);
        logData.setProgramFlowType(ProgramFlow.ChannelGWOut);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setProgramName(StringUtils.join(ProgramName, ".addCBSTimeoutRerunMessage"));
        logData.setRemark(StringUtils.join(SvrConst.SVR_CBSTimeOutRerun, " add ", cnt, " messages to cbspend"));
        this.logMessage(logData);
        return "OK";
    }
    /**
     * 將 16 進位字串轉為 byte 陣列
     */
    public static byte[] hexStringToByteArray(String s) {
        if (s == null) return new byte[0];
        int len = s.length();
        // 確保長度為偶數
        if (len % 2 != 0) {
            s = "0" + s;
            len++;
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
