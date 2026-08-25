package com.syscom.fep.server.common.cbspend;

import com.syscom.fep.base.aa.INBKData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.CbspendExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.model.Cbspend;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.adapter.CBSAdapter;
import com.syscom.fep.server.common.handler.HandlerBase;
import com.syscom.fep.vo.text.ims.CB_IQTX_I001;
import com.syscom.fep.vo.text.ims.CB_IQTX_O001;
import com.syscom.fep.vo.text.ims.CB_IQTX_O002;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CBSPEND extends HandlerBase {
    private String ProgramName = StringUtils.join(this.getClass().getSimpleName(), ".");
    private LogData logData = getLogContext();
    private CbspendExtMapper cbspendExtMapper = SpringBeanFactoryUtil.getBean(CbspendExtMapper.class);
    private SysstatExtMapper sysstatExtMapper = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
    private FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
    public CBSPEND() {
    }

    @Override
    public String dispatch(FEPChannel channel, String data) throws Exception {
        Cbspend cbspend = new Cbspend();
        String Response = "";
        try {
            //1.讀取上傳主機逾時明細檔
            cbspend = selectCBSPEND(data);

            Object tota = null;
            //2.交易狀態查詢
            if (cbspend != null) {
                tota = this.getTransationStatus(cbspend);
            }
            if (tota == null) {
                logData.setProgramName(ProgramName + ".dispatch");
                logData.setRemark("tota is null" );
                logMessage(logData);
                if (cbspend != null) {
                    return String.format("%s:%s", cbspend.getCbspendTxDate(), Integer.toString(cbspend.getCbspendEjfno()));
                }else{
                    logData.setProgramName(ProgramName + ".dispatch");
                    logData.setRemark("cbspend is null" );
                    logMessage(logData);
                    return data;
                }
            }

            String rtn = this.resendAccount(cbspend,tota);
            if (rtn.equals("error")) {
                return String.format("%s:%s", cbspend.getCbspendTxDate(), Integer.toString(cbspend.getCbspendEjfno()));
            }
            return Response;
        } catch (Throwable ex) {
            LogHelperFactory.getTraceLogger().trace("Dispatch發生例外 : " + ex.toString());
            LogHelperFactory.getTraceLogger().trace("Dispatch發生例外 : " + ex.getStackTrace());
            // logData = new LogData();
            logData.setChannel(channel);
            logData.setMessage(data);
            logData.setRemark("Dispatch發生例外");
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setSubSys(SubSystem.CBSPEND);
            logData.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            logData.setProgramException(ex);
            sendEMS(logData);
            logMessage(logData);
            return data;
        }
    }

    /**
     * 重送入帳或沖正交易
     *
     * @param
     * @param tota
     * @throws Exception
     */
    private String resendAccount(Cbspend cbspend,Object tota) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.ProgramException;
        logData.setProgramName(ProgramName + ".resendAccount");
        logData.setRemark("重送入帳或沖正交易" );
        logMessage(logData);
        CBSAdapter adapter = new CBSAdapter(new INBKData());
        adapter.setCbsType(CBSType.CBS);
        adapter.setCbsId(StringUtils.leftPad(cbspend.getCbspendEjfno().toString(), 8, '0'));
        if(EbcdicConverter.fromHex(CCSID.English, cbspend.getCbspendTita()).startsWith("EAII"))
            adapter.setCbsId(StringUtils.leftPad(cbspend.getCbspendStan(), 8, '0'));

        if (StringUtils.equals("N", this.getImsPropertiesValue(tota, ImsMethodName.TX_ACCT_FLAG.getValue())) && // 未記帳
                StringUtils.equals("A2", cbspend.getCbspendCbsKind()) // 入帳類交易
        ) {
            // 需重送入帳電文至CBS主機
            adapter.setMessageToCBS(cbspend.getCbspendTita());
            rtnCode = adapter.sendReceive();
        }

        if (StringUtils.equals("Y", this.getImsPropertiesValue(tota, ImsMethodName.TX_ACCT_FLAG.getValue())) && // 已記帳
                StringUtils.equals("A1", cbspend.getCbspendCbsKind()) // 扣帳交易
        ) {
            // 查詢交易成功, 需將沖正電文至CBS主機
            // 將 CBSPEND_TITA電文PROCESS_TYPE內容 改為 ‘RVS’其餘欄位相同, 將沖正電文至CBS主機
            // CBSPEND.CBSPEND_TITA，PROCESS_TYPE 第46位，取4位
            cbspend.setCbspendTita(StringUtils.substring(cbspend.getCbspendTita(), 0, 92) + EbcdicConverter.toHex(CCSID.English, 4, " RVS")
                    + StringUtils.substring(cbspend.getCbspendTita(), 100, cbspend.getCbspendTita().length()));
            // 需重送入帳電文至CBS主機
            adapter.setMessageToCBS(cbspend.getCbspendTita());
            rtnCode = adapter.sendReceive();
        }

        // 自行轉帳, 改為不再重送
        // ATM 入/扣帳Pending，更新CBS記帳結果，不重送入/扣帳
        if (StringUtils.equals("A3", cbspend.getCbspendCbsKind())) {
            // 將查詢結果回寫 CBSPEND / FEPTXN
            if (StringUtils.equals("Y", this.getImsPropertiesValue(tota, ImsMethodName.TX_ACCT_FLAG.getValue()))) {
                cbspend.setCbspendAccType((short) 1); // 已記帳
            } else {
                cbspend.setCbspendAccType((short) 0); // 未記帳
            }
            cbspend.setCbspendSuccessFlag((short) 2); // 不再重送
            int updateCount = this.cbspendExtMapper.updateByPrimaryKey(cbspend);
            if (updateCount == 0) {
                logContext.setProgramException(new RuntimeException("自行轉帳交易不再重送，更新CBSPEND_SUCCESS_FLAG=2失敗"));
                logContext.setProgramName(ProgramName);
                sendEMS(logContext);
                logMessage(logData);
                return "error";
            }
            return "";
        }

        if (rtnCode != FEPReturnCode.Normal) {
            // 執行處理TIMEOUT Routine
            this.doTimeOutRoutine(cbspend);
            logData.setProgramName(ProgramName + ".resendAccount");
            logData.setRemark("執行處理TIMEOUT Routine" );
            logMessage(logData);
            return "error";
        }

        //拆解電文
        String response = adapter.getMessageFromCBS();
        if(response == null){
            return null;
        }

        String IMSRC_TCB = EbcdicConverter.fromHex(CCSID.English,StringUtils.substring(response,80,86));
        // 收到CBS主機回應
        if (StringUtils.isBlank(IMSRC_TCB)) {
            IMSRC_TCB = "000";
        }
        cbspend.setCbspendCbsRc(IMSRC_TCB);
        if (IMSRC_TCB.equals("000")) { // 成功
            // 以下更新 CBSPEND & FEPTXN 需做 TRANSACTION
            cbspend.setCbspendSuccessFlag((short) 1); // 重送成功
            cbspend.setCbspendResendCnt((short) (cbspend.getCbspendResendCnt() + 1));
            cbspend.setCbspendCbsRc("0000"); // 成功
            if (StringUtils.equals("A2", cbspend.getCbspendCbsKind())) { // 入帳類交易
                cbspend.setCbspendAccType((short) 1); // 已記帳
            } else if (StringUtils.equals("A1", cbspend.getCbspendCbsKind())) { // 扣帳交易
                // 扣帳類交易含跨行存款
                cbspend.setCbspendAccType((short) 2); // 已沖正
            }
            // BEGIN TRANSACTION
            int updateCount = this.cbspendExtMapper.updateByPrimaryKey(cbspend);
            if (updateCount == 0) {
                logContext.setProgramException(new RuntimeException("T24回應OK, 更新 CBSPEND失敗"));
                logContext.setProgramName(ProgramName);
                sendEMS(logContext);
                logMessage(logData);
                return "error";
            }
            // 執行更新 FEPTXN Routine
            String rtn = this.updateFeptxnRoutine(cbspend, response);
            if (rtn.equals("error")) {
                return "error";
            }
            return "";
        } else { // 失敗
            // 8/21 修改,移至執行完 unlock, 再做 TRANSACTION
            // 以下更新 CBSPEND & FEPTXN 需做 TRANSACTION
            cbspend.setCbspendResendCnt((short) (cbspend.getCbspendResendCnt() + 1));
            cbspend.setCbspendCbsRc(this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue()));
            cbspend.setCbspendAccType((short) 3); // 更正or轉入失敗
            // BEGIN TRANSACTION
            int updateCount = this.cbspendExtMapper.updateByPrimaryKey(cbspend);
            if (updateCount == 0) {
                logContext.setProgramException(new RuntimeException("CBS Return Error 更新 CBSPEND失敗"));
                logContext.setProgramName(ProgramName);
                sendEMS(logContext);
                logMessage(logData);
                return "error";
            }
            // 執行更新 FEPTXN Routine
            String rtn = this.updateFeptxnRoutine(cbspend, response);
            if (rtn.equals("error")) {
                return "error";
            }
            return "";
        }
    }

    public String makeCBSTita(Cbspend cbspend) throws Exception {
        FEPReturnCode _rtnCode = FEPReturnCode.ProgramException;
        CB_IQTX_I001 cbsTita = new CB_IQTX_I001();
        //header
        cbsTita.setINTRAN("EAII");
        String MSGID = StringUtils.rightPad(cbspend.getCbspendTxDate() + cbspend.getCbspendTxTime() + StringUtils.leftPad(String.valueOf(cbspend.getCbspendEjfno()), 8, "0"), 24, " ");
        cbsTita.setINMSGID(MSGID);
        cbsTita.setINDATE(cbspend.getCbspendTxDate());
        cbsTita.setINTIME(cbspend.getCbspendTxTime());
        cbsTita.setINSERV("FEP1");
        cbsTita.setMQIDCNV("N");
        cbsTita.setINTD("910A0037");
        cbsTita.setINAP("ATFEP");
        cbsTita.setINFF("F");
        cbsTita.setINPGNO("001");
        cbsTita.setINV1CT("0000");

        //body
        cbsTita.setINQ_ACQ_BID(cbspend.getCbspendBkno());
        cbsTita.setINQ_TX_STAN(cbspend.getCbspendStan());
        cbsTita.setINQ_FG_TXDATE(CalendarUtil.adStringToROCString(cbspend.getCbspendTbsdyFisc())); //轉民國年
        cbsTita.setINSTAN(cbspend.getCbspendStan());

        String tita = null;
        tita = cbsTita.makeMessage();
        String AsciiTita = cbsTita.makeMessageAscii();

        //LOG
        this.logContext.setRemark("MessageToCBS Text");
        this.logContext.setMessage("MessageToCBS:" + tita);
        logMessage(Level.INFO, logContext);
        this.logContext.setRemark("Ascii MessageToCBS Text");
        this.logContext.setMessage("Ascii MessageToCBS:" + AsciiTita);
        logMessage(Level.INFO, logContext);

        return tita;
    }

    /**
     * 交易狀態查詢
     *
     * @param
     * @return tota
     * @throws Exception
     */
    private Object getTransationStatus(Cbspend cbspend) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;

        // 電文 Sample
        String tita = null;
        tita = this.makeCBSTita(cbspend);

        // 送IMS
        CBSAdapter adapter = new CBSAdapter(new INBKData());
        adapter.setCbsType(CBSType.CBS);
        adapter.setCbsId(StringUtils.leftPad(cbspend.getCbspendEjfno().toString(), 8, '0'));
        if(EbcdicConverter.fromHex(CCSID.English, tita).startsWith("EAII"))
            adapter.setCbsId(StringUtils.leftPad(cbspend.getCbspendStan(), 8, '0'));

        adapter.setMessageToCBS(tita);
        rtnCode = adapter.sendReceive();
        // CBS TIMEOUT OR OTHER ERROR
        if (rtnCode != FEPReturnCode.Normal) {
            // 執行處理TIMEOUT Routine
            logData.setProgramName(ProgramName + ".getTransationStatus");
            logData.setRemark("TIMEOUT Routine" );
            logMessage(logData);
            this.doTimeOutRoutine(cbspend);
            return null;
        }

        //拆解電文
        Object tota = null;
        tota = parseCbsTota(adapter.getMessageFromCBS());
        if(tota == null){
            logData.setProgramName(ProgramName + ".getTransationStatus");
            logData.setRemark("tota is null" );
            logMessage(logData);
            return null;
        }

        //LOG
        CB_IQTX_O001 cbsTota = new CB_IQTX_O001();
        String AsciiTota = cbsTota.makeMessageAscii();

        this.logContext.setRemark("MessageToCBS Text");
        this.logContext.setMessage("MessageToCBS:" + tota);
        logMessage(Level.INFO, logContext);
        this.logContext.setRemark("Ascii MessageToCBS Text");
        this.logContext.setMessage("Ascii MessageToCBS:" + AsciiTota);
        logMessage(Level.INFO, logContext);

        if (StringUtils.equals("4001", this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue()))) {
            // 以下更新 CBSPEND & FEPTXN 需做 TRANSACTION
            if (StringUtils.equals("A2", cbspend.getCbspendCbsKind()) && // 入帳類交易
                    StringUtils.equals("Y", this.getImsPropertiesValue(tota, ImsMethodName.TX_ACCT_FLAG.getValue())) // 已記帳
            ) {
                // 入帳類交易已記帳, 不需重送
                cbspend.setCbspendSuccessFlag((short) 1); // 重送成功
                cbspend.setCbspendResendCnt((short) (cbspend.getCbspendResendCnt() + 1));
                cbspend.setCbspendAccType((short) 1); // 已記帳
                cbspend.setCbspendCbsRc(this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC3_TCB.getValue())); // 成功
                int updateCount = this.cbspendExtMapper.updateByPrimaryKey(cbspend);
                if (updateCount == 0) {
                    // 回寫該筆 CBSPEND QUEUE
                    logContext.setProgramException(new RuntimeException("查詢成功 更新 CBSPEND失敗"));
                    logContext.setProgramName(ProgramName);
                    sendEMS(logContext);
                    return null;
                }
                // 執行更新 FEPTXN Routine
                String rtn = this.updateFeptxnRoutine(cbspend, tota);
                if (rtn.equals("error")) {
                    return null;
                }
            }
            // 扣帳類交易未記帳, 不需重送
            if (StringUtils.equals("A1", cbspend.getCbspendCbsKind()) && // 扣帳類交易
                    StringUtils.equals("N", this.getImsPropertiesValue(tota, ImsMethodName.TX_ACCT_FLAG.getValue())) // 未記帳
            ) {
                // 以下更新 CBSPEND & FEPTXN 需做 TRANSACTION
                cbspend.setCbspendSuccessFlag((short) 1); // 重送成功
                cbspend.setCbspendResendCnt((short) (cbspend.getCbspendResendCnt() + 1));
                // 判斷是否需沖正
                if (DbHelper.toBoolean(cbspend.getCbspendReverseFlag()) && // 沖正記號
                        StringUtils.equals("Y", this.getImsPropertiesValue(tota, ImsMethodName.TX_RVS_FLAG.getValue())) // 已沖正
                ) {
                    cbspend.setCbspendAccType((short) 2); // 沖正成功
                    cbspend.setCbspendCbsRc(this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC3_TCB.getValue())); // 成功
                } else {
                    cbspend.setCbspendAccType((short) 0); // 未記帳
                    cbspend.setCbspendCbsRc(this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC3_TCB.getValue())); // 成功
                }
                // UPDATE (CBSPEND)
                int updateCount = this.cbspendExtMapper.updateByPrimaryKey(cbspend);
                if (updateCount == 0) {
                    // 回寫該筆 CBSPEND QUEUE
                    logContext.setProgramException(new RuntimeException("NOT FOUND 更新 CBSPEND失敗"));
                    logContext.setProgramName(ProgramName);
                    sendEMS(logContext);
                    logMessage(logData);
                    return null;
                }
                // 執行更新 FEPTXN Routine
                String rtn = this.updateFeptxnRoutine(cbspend, tota);
                if (rtn.equals("error")) {
                    return null;
                }
            }
        }
        return tota;
    }

    /**
     * 更新 4FEPTXN Routine
     *
     * @param
     * @throws Exception
     */
    private String updateFeptxnRoutine(Cbspend cbspend,Object tota) throws Exception {
        // 讀取 FEPTXN (檔名 SEQ 為CBSPEND_TBSDY_FISC [7:2] )
        String tbsdy = cbspend.getCbspendTbsdyFisc().substring(6, 8);
        this.feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
        Feptxn feptxn = this.feptxnDao.selectByPrimaryKey(cbspend.getCbspendTxDate(), cbspend.getCbspendEjfno());
        if (feptxn == null) {
            logContext.setProgramException(new RuntimeException("FEPTXN Routine Query FEPTXN 失敗"));
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
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
        try {
            int count = feptxnDao.updateByPrimaryKeySelective(feptxn);
            if (count == 0) {
                // 回寫該筆 CBSPEND QUEUE
                logContext.setProgramException(new RuntimeException("FEPTXN Routine 更新 FEPTXN失敗"));
                logContext.setProgramName(ProgramName);
                sendEMS(logContext);
                return "error";
            }
        } catch (Exception e) {
            // 回寫該筆 CBSPEND QUEUE
            logContext.setProgramException(new RuntimeException("FEPTXN Routine 更新 FEPTXN失敗"));
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
            return "error";
        }
        return "";
    }

    /**
     * 更新 5FEPTXN Routine
     *
     * @param
     * @throws Exception
     */
    private String updateFeptxnRoutine(Cbspend cbspend,String tota) throws Exception {
        // 讀取 FEPTXN (檔名 SEQ 為CBSPEND_TBSDY_FISC [7:2] )
        String tbsdy = cbspend.getCbspendTbsdyFisc().substring(6, 8);
        this.feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
        Feptxn feptxn = this.feptxnDao.selectByPrimaryKey(cbspend.getCbspendTxDate(), cbspend.getCbspendEjfno());
        if (feptxn == null) {
            logContext.setProgramException(new RuntimeException("FEPTXN Routine Query FEPTXN 失敗"));
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
            return "error";
        }
        String imsbusinessDate = EbcdicConverter.fromHex(CCSID.English,StringUtils.substring(tota,86,100));
        if ("0000000".equals(imsbusinessDate) || "000000".equals(imsbusinessDate)
                || (imsbusinessDate.length() != 6 && imsbusinessDate.length() != 7)) { // "0000000"不是日期格式，民國轉西元會轉成""，因此特殊處理
            imsbusinessDate = "00000000";
        } else {
            imsbusinessDate = CalendarUtil.rocStringToADString14(imsbusinessDate);
        }
        feptxn.setFeptxnTbsdy(imsbusinessDate);
        feptxn.setFeptxnBrno(EbcdicConverter.fromHex(CCSID.English,StringUtils.substring(tota,136,144)));
        feptxn.setFeptxnTrinBrno(EbcdicConverter.fromHex(CCSID.English,StringUtils.substring(tota,144,152)));
        feptxn.setFeptxnAccType(cbspend.getCbspendAccType()); // 記帳別
        feptxn.setFeptxnCbsTimeout((short) 0);
        feptxn.setFeptxnCbsRc(cbspend.getCbspendCbsRc());
        try {
            int count = feptxnDao.updateByPrimaryKeySelective(feptxn);
            if (count == 0) {
                // 回寫該筆 CBSPEND QUEUE
                logContext.setProgramException(new RuntimeException("FEPTXN Routine 更新 FEPTXN失敗"));
                logContext.setProgramName(ProgramName);
                sendEMS(logContext);
                return "error";
            }
        } catch (Exception e) {
            // 回寫該筆 CBSPEND QUEUE
            logContext.setProgramException(new RuntimeException("FEPTXN Routine 更新 FEPTXN失敗"));
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
            return "error";
        }
        return "";
    }

    public Object parseCbsTota(String message) throws Exception {
        if (StringUtils.isNotBlank(message)) {
            if (message.length() == 634) {
                CB_IQTX_O001 tota = new CB_IQTX_O001();
                tota.parseCbsTele(message);
                return tota;
            } else {
                CB_IQTX_O002 tota = new CB_IQTX_O002();
                tota.parseCbsTele(message);
                return tota;
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
            int updateCount = this.cbspendExtMapper.updateByPrimaryKey(cbspend);
            if (updateCount == 0) {
                // 回寫該筆 CBSPEND QUEUE
                logContext.setProgramException(new RuntimeException("TIMEOUT Routine 更新 CBSPEND失敗"));
                logContext.setProgramName(ProgramName);
                sendEMS(logContext);
            }
        } catch (Exception e) {
            // 回寫該筆 CBSPEND QUEUE
            logContext.setProgramException(new RuntimeException("TIMEOUT Routine 更新 CBSPEND失敗"));
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
        }
    }

    public Cbspend selectCBSPEND(String data) throws ParseException {
        String[] datas = data.split(":");

        // Queue內容不符合預期，不處理
        if (datas.length < 2) {
            return null;
        }

        //拆解Queue
        String cbspendTxDate = datas[0];
        int cbspendEjfno = Integer.parseInt(datas[1]);

        // 3. 讀取上傳主機逾時明細檔
        Cbspend cbspend = this.loadCbspend(cbspendTxDate, cbspendEjfno);
        return cbspend;
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
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
            return null;
        }

        // 判斷重送記號(CBSPEND_SUCCESS_FLAG)
        if (cbspend.getCbspendSuccessFlag() != 0) {
            return null;
        }

        // 判斷執行時間
        Date txDate = new SimpleDateFormat("yyyyMMdd").parse(cbspend.getCbspendTxDate());
        Sysstat sysstat = this.sysstatExtMapper.selectFirstByLbsdyFisc();
        Date lbsdyDate = new SimpleDateFormat("yyyyMMdd").parse(sysstat.getSysstatLbsdyFisc());
        // 2021/2/25 判斷執行時間不得超過財金前營業日
        if (txDate.compareTo(lbsdyDate) < 0) {
            // 小於前一財金營業日, 不重送
            // 更新重送記號
            cbspend.setCbspendSuccessFlag((short) 2); // 不再重送
            int updateCount = this.cbspendExtMapper.updateByPrimaryKey(cbspend);
            if (updateCount == 0) {
                logContext.setProgramException(new RuntimeException("CBSPEND更新失敗-不再重送"));
                logContext.setProgramName(ProgramName);
                sendEMS(logContext);
            }
            return null;
        }

        // 判斷重送次數
        Short resendCnt = cbspend.getCbspendResendCnt();
        if (resendCnt >= 5) {
            logContext.setProgramException(new RuntimeException("CBSPEND_RESEND_CNT=5 不回寫該筆 CBSPEND QUEUE"));
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
            // 8/21 刪除, 執行到5次, 不回寫 CBSPEND Queue
            return null;
        }

        return cbspend;
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return true;
    }

    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        // TODO Auto-generated method stub
        return null;
    }

}
