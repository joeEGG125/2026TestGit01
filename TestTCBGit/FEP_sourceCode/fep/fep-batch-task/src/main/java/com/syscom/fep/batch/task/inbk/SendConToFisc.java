package com.syscom.fep.batch.task.inbk;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.INBKData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.FeptxnExtMapper;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.server.aa.inbk.SendConfirmByManual;
import com.syscom.fep.server.common.adapter.CBSAdapter;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.vo.text.ims.CB_IQTX_I001;
import com.syscom.fep.vo.text.ims.CB_IQTX_O001;
import com.syscom.fep.vo.text.ims.CB_IQTX_O002;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SendConToFisc extends FEPBase implements Task {
    private BatchJobLibrary job = null;
    private String batchLogPath = StringUtils.EMPTY;
    private boolean batchResult = false;

    private FeptxnExtMapper feptxnExtMapper = SpringBeanFactoryUtil.getBean(FeptxnExtMapper.class);

    @Override
    public BatchReturnCode execute(String[] args) {
        try {
            // 1. 初始化相關批次物件及拆解傳入參數
            initialBatch(args);

            // 2. 檢核批次參數是否正確, 若正確則啟動批次工作
            job.writeLog("------------------------------------------------------------------");
            job.writeLog(ProgramName + "開始");
            job.startTask();

            if (!this.checkConfig()) {
                job.stopBatch();
                return BatchReturnCode.ProgramException;
            }

            // 3. 批次主要處理流程
            batchResult = mainProcess();

            // 4. 通知批次作業管理系統工作正常結束
            if (batchResult) {
                job.writeLog(ProgramName + "正常結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.endTask();
            } else {
                job.writeLog(ProgramName + "不正常結束，停止此批次作業!!");
                job.writeLog("------------------------------------------------------------------");
                job.abortTask();
            }
            return BatchReturnCode.Succeed;
        } catch (Exception e) {
            logContext.setProgramException(e);
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
            if (job != null) {
                job.writeErrorLog(e, e.getMessage());
                job.writeLog(ProgramName + "失敗!!");
                job.writeLog("------------------------------------------------------------------");
                // 通知批次作業管理系統工作失敗,暫停後面流程
                try {
                    job.abortTask();
                } catch (Exception ex) {
                    logContext.setProgramException(ex);
                    logContext.setProgramName(ProgramName);
                    sendEMS(logContext);
                }
            }
            return BatchReturnCode.ProgramException;
        } finally {
            if (job != null) {
                job.writeLog(ProgramName + "結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.dispose();
                job = null;
            }
            if (logContext != null) {
                logContext = null;
            }
        }
    }

    private boolean mainProcess() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String txDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String txTimeStr = now.format(DateTimeFormatter.ofPattern("HHmm"));
        int txTime = Integer.parseInt(txTimeStr);
        job.writeLog("系統日期(TXDATE): " + txDate + ", 系統時間(TXTIME): " + txTimeStr);

//        if (txTime > 1600) { /*下午4點以後不能執行*/
//            job.writeLog("下午4點以後不能執行，批次結束!!");
//            return true;
//        }

        String sysstatHbkno = SysStatus.getPropertyValue().getSysstatHbkno();
        String sysstatLbsdyFisc = SysStatus.getPropertyValue().getSysstatLbsdyFisc();
        List<Feptxn> feptxnList = feptxnExtMapper.getFeptxnForSendConToFisc(sysstatLbsdyFisc, sysstatHbkno);

        if (feptxnList == null || feptxnList.isEmpty()) {
            job.writeLog("無符合條件的交易，批次結束!!");
            return true; //查無資料批次正常結束
        }

        job.writeLog("批次開始:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        int TOT_CNT = 0;  //總筆數
        int SKIP_CNT = 0; //未處理筆數
        int SEND_CNT = 0; //補送CON 筆數

        String timeThreshold = now.minusMinutes(5).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        for (Feptxn feptxn : feptxnList) {
            TOT_CNT ++;

            String txnDateTime = StringUtils.trimToEmpty(feptxn.getFeptxnTxDate()) + StringUtils.trimToEmpty(feptxn.getFeptxnTxTime());
            if (StringUtils.isNotBlank(txnDateTime) && txnDateTime.compareTo(timeThreshold) > 0) {
                //補con 限5分鐘前的交易
                SKIP_CNT++;
                job.writeLog("5分鐘內交易暫不補Con交易，交易時間:" + feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime() +
                        "，財金營業日：" + feptxn.getFeptxnTbsdyFisc() +
                        "，STAN：" + feptxn.getFeptxnBkno() + feptxn.getFeptxnStan() +
                        "，PCODE：" + feptxn.getFeptxnPcode());
                continue;
            }

            if (StringUtils.isNotBlank(feptxn.getFeptxnCbsTxCode())) {
                //組交易查詢Request電文
                String tita = makeCBSTita(feptxn);

                CBSAdapter adapter = new CBSAdapter(new INBKData());
                adapter.setCbsType(CBSType.CBS); // FISCTCB or NONATM
                adapter.setCbsId(StringUtils.leftPad(feptxn.getFeptxnEjfno().toString(), 8, '0'));
                adapter.setMessageToCBS(tita);

                FEPReturnCode rtnCode = adapter.sendReceive();
                if (rtnCode != FEPReturnCode.Normal) {
                    SKIP_CNT++;
                    job.writeLog("查詢主機交易結果逾時，暫不補Con，交易時間:" + feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime() +
                            "，財金營業日：" + feptxn.getFeptxnTbsdyFisc() +
                            "，STAN：" + feptxn.getFeptxnBkno() + feptxn.getFeptxnStan() +
                            "，PCODE：" + feptxn.getFeptxnPcode());
                    continue;
                }

                //拆解主機回覆的 Tota 電文
                Object tota = parseCbsTota(adapter.getMessageFromCBS());
                if (tota == null) {
                    job.writeLog("無法解析 CBS 回覆電文 (Tota is null)");
                    SKIP_CNT++;
                    continue;
                }
                String outrtc = this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue());
                String imsRc4Fisc = this.getImsPropertiesValue(tota, ImsMethodName.IMS_RC4_FISC.getValue());
                String txRvsFlag = this.getImsPropertiesValue(tota, ImsMethodName.TX_RVS_FLAG.getValue());
                String txAcctFlag = this.getImsPropertiesValue(tota, ImsMethodName.TX_ACCT_FLAG.getValue());
                job.writeLog("check<" + imsRc4Fisc + ">");
                if("4001".equals(outrtc)){ //查詢成功
                    job.writeLog("4001查詢成功. stan:" + feptxn.getFeptxnStan());
                    feptxn.setFeptxnCbsRc(imsRc4Fisc);
                    feptxn.setFeptxnPending((short) 2); //解除PENDING
                    if ("Y".equals(txRvsFlag)) {
                        feptxn.setFeptxnAccType((short) 2); /*已沖正*/
                        feptxn.setFeptxnTxrust("C");
                    }else if ("Y".equals(txAcctFlag)) {
                        feptxn.setFeptxnAccType((short) 1); /*已記帳*/
                        feptxn.setFeptxnTxrust("A");
                    }
                }else{//查詢失敗
                    job.writeLog(outrtc + "查詢失敗. stan:" + feptxn.getFeptxnStan());
                    feptxn.setFeptxnCbsRc(outrtc);
                    feptxn.setFeptxnTxrust("R");
                    feptxn.setFeptxnPending((short) 2); //解除PENDING
                }
//                feptxn.setFeptxnCbsTimeout((short) 0);
                //更新此筆交易紀錄
                try {
                    int updateCount = feptxnExtMapper.updateByPrimaryKeySelective(feptxn);
                    if (updateCount == 0) {
                        job.writeLog("警告：FEPTXN 更新筆數為 0，EJFNO: " + feptxn.getFeptxnEjfno());
                    }
                } catch (Exception e) {
                    job.writeLog("更新 FEPTXN 失敗: " + e.getMessage());
                    SKIP_CNT++;
                    continue; // 資料庫更新失敗，不發送 Confirm
                }

                // 2. 補送Con 給財金 執行完繼續做下一筆
                boolean isSent = processSingleTransaction(feptxn);
                if (isSent) {
                    SEND_CNT++;
                } else {
                    SKIP_CNT++;
                }

            } else {
                SKIP_CNT++;
                job.writeLog("未送主機交易，請依FEP交易結果於FEPWEB執行人工補Confirm，交易時間:" + feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime() +
                        "，財金營業日：" + feptxn.getFeptxnTbsdyFisc() +
                        "，STAN：" + feptxn.getFeptxnBkno() + feptxn.getFeptxnStan() +
                        "，PCODE：" + feptxn.getFeptxnPcode());
            }
        }

        job.writeLog("批次結束:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                "，處理筆數:" + TOT_CNT +
                "，不需補送CON筆數:" + SKIP_CNT +
                "，補送CON筆數: " + SEND_CNT );
        return true;
    }

    /**
     * 2. 補送Con 給財金
     */
    private boolean processSingleTransaction(Feptxn feptxn) throws Exception {
        /*將資料按畫面給予，欄位如下*/
        // 以主機回應結果判斷，主機逾時不回覆財金
        ConfirmUIData uiData = new ConfirmUIData();

        if ("4001".equals(feptxn.getFeptxnCbsRc()) || "000".equals(feptxn.getFeptxnCbsRc())) {
            uiData.setUiRc(1); //+Con
        } else if ("0601".equals(feptxn.getFeptxnCbsRc())) {
            uiData.setUiRc(3);
            uiData.setUiFailReason(feptxn.getFeptxnCbsRc());
        } else {
            uiData.setUiRc(2);
            uiData.setUiFailReason(feptxn.getFeptxnCbsRc());
        }
        uiData.setUiBkno(feptxn.getFeptxnBkno());
        uiData.setUiStan(feptxn.getFeptxnStan());
        uiData.setUiTbsdyFisc(feptxn.getFeptxnTbsdyFisc());
        uiData.setUiPcode(feptxn.getFeptxnPcode());
//        /* 跨行台幣交易 */
//        uiData.setUiTxAmt(feptxn.getFeptxnTxAmt());
//        /* for 國際卡交易, 轉出行要抓取 FEPTXN_DES_BKNO */
//        String troutBkno = "000".equals(feptxn.getFeptxnTroutBkno()) ? feptxn.getFeptxnDesBkno() : feptxn.getFeptxnTroutBkno();
//        uiData.setUiTroutActno(troutBkno + feptxn.getFeptxnTroutActno()); //扣款帳號
//
//        uiData.setUiTrinActno(feptxn.getFeptxnTrinBkno() + feptxn.getFeptxnTrinActno()); //轉入帳號
//        uiData.setUiMajorActno(feptxn.getFeptxnMajorActno()); //卡片帳號
//        uiData.setUiTrinActnoActual(feptxn.getFeptxnTrinActnoActual()); //入帳帳號
//        uiData.setUiAtmno(feptxn.getFeptxnAtmno());
//        uiData.setUiRc1(feptxn.getFeptxnRepRc());
        job.writeLog("補Con交易，交易時間：" + feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime() +
                "，財金營業日：" + uiData.getUiTbsdyFisc() +
                "，STAN：" + uiData.getUiBkno() + uiData.getUiStan() +
                "，PCODE：" + uiData.getUiPcode() +
                "，RC：" + uiData.getUiRc() + "(1:4001，2:0501，3:0601)" +
                "，失敗原因：" + (StringUtils.isBlank(uiData.getUiFailReason()) ? "無" : uiData.getUiFailReason()));

        //CALL AA SendConfirmByManual 人工補 Confirm 電文, 再發送 Confirm 電文給財金發動
        try {
            FISCData fiscData = new FISCData();
            fiscData.setLogContext(logContext);
            fiscData.setFiscTeleType(FISCSubSystem.INBK);

            fiscData.setMessageID(feptxn.getFeptxnPcode()); // 底層會切割字串
            fiscData.setTxChannel(FEPChannel.BATCH);
            fiscData.setTxSubSystem(SubSystem.INBK);
            fiscData.setMessageFlowType(MessageFlow.Request);
            fiscData.setTxRequestMessage("");
            fiscData.setFeptxn(feptxn);

            fiscData.setEj(feptxn.getFeptxnEjfno());
            fiscData.setStan(feptxn.getFeptxnStan());
            Msgctl mockMsgCtl = new Msgctl();
            mockMsgCtl.setMsgctlStatus((short) 1); // 1: 代表交易啟用正常
            fiscData.setMsgCtl(mockMsgCtl);

            FISC_INBK fiscReq = new FISC_INBK(); //模擬網頁傳入
            // 將判斷出來的 RC (1, 2, 3) 塞入電文
            fiscReq.setRsCode(String.valueOf(uiData.getUiRc()));
            fiscReq.setEj(feptxn.getFeptxnEjfno());

            // AA 的 searchFeptxn 會將日期做 substring(0,7) 然後轉西元年
            String rocDate = CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDate());
            fiscReq.setTxnInitiateDateAndTime(rocDate + feptxn.getFeptxnTxTime());
            fiscReq.setTxDatetimeFisc(feptxn.getFeptxnTxDate());
            fiscData.setFiscreq(fiscReq);
            
            FISCGeneral fiscGeneral = new FISCGeneral();
            fiscGeneral.setEJ(feptxn.getFeptxnEjfno());
            fiscGeneral.setSubSystem(FISCSubSystem.INBK);
            fiscGeneral.setINBKRequest(fiscReq); // 讓 AA 讀取模擬參數
            fiscGeneral.setINBKConfirm(fiscReq);
            fiscData.setTxObject(fiscGeneral);
            // 呼叫 AA 執行發送
            SendConfirmByManual manualConfirmAA = new SendConfirmByManual(fiscData);
            manualConfirmAA.processRequestData();

            job.writeLog("呼叫 SendConfirmByManual 成功！EJFNO: " + feptxn.getFeptxnEjfno());
        } catch (Exception ex) {
            job.writeErrorLog(ex, "呼叫 SendConfirmByManual 發生異常: " + ex.getMessage());
            return false;
        }

        return true;
    }

    /**
     * 初始化相關批次物件及拆解傳入參數初始化相關批次物件及拆解傳入參數
     *
     * @param args
     */
    private void initialBatch(String[] args) {
        // 初始化logContext物件,傳入工作執行參數
        logContext = new LogData();
        logContext.setChannel(FEPChannel.BATCH);
        logContext.setEj(0);
        logContext.setProgramName(ProgramName);
        // 檢查Batch Log目錄參數
        batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
        if (StringUtils.isBlank(batchLogPath)) {
            System.out.println("Batch Log目錄未設定，請修正");
            return;
        }

        // 初始化BatchJob物件,傳入工作執行參數
        job = new BatchJobLibrary(this, args, batchLogPath);
        job.writeLog("SendConToFisc start!");
    }

    private boolean checkConfig() throws Exception {


        return true;
    }

    @Getter
    @Setter
    public class ConfirmUIData {
        private int uiRc;
        private String uiFailReason;
        private String uiBkno;
        private String uiStan;
        private String uiTbsdyFisc;
        private String uiPcode;
        private java.math.BigDecimal uiTxAmt;
        private String uiTroutActno; // 扣款帳號
        private String uiTrinActno;  // 轉入帳號
        private String uiMajorActno; // 卡片帳號
        private String uiTrinActnoActual; // 入帳帳號
        private String uiAtmno;
        private String uiRc1;
    }

    /**
     * 依照 Feptxn 內容組裝 CBS 查詢 Request 電文
     */
    private String makeCBSTita(Feptxn feptxn) throws Exception {
        CB_IQTX_I001 cbsTita = new CB_IQTX_I001();
        // header
        cbsTita.setINTRAN("EAII");
        String msgId = StringUtils.rightPad(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime() + StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()), 8, "0"), 24, " ");
        cbsTita.setINMSGID(msgId);
        cbsTita.setINDATE(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        cbsTita.setINTIME(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
        cbsTita.setINSERV("FEP1");
        cbsTita.setMQIDCNV("N");
        cbsTita.setINTD("910A0037");
        cbsTita.setINAP("ATFEP");
        cbsTita.setINFF("F");
        cbsTita.setINPGNO("001");
        cbsTita.setINV1CT("0000");

        // body
        cbsTita.setINQ_ACQ_BID(feptxn.getFeptxnBkno());
        cbsTita.setINQ_TX_STAN(feptxn.getFeptxnStan());
        cbsTita.setINQ_FG_TXDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTbsdyFisc())); // 轉民國年

        return cbsTita.makeMessage();
    }

    /**
     * 拆解主機回覆的 Tota 電文
     */
    private Object parseCbsTota(String message) throws Exception {
        if (StringUtils.isNotBlank(message) && message.length() >= 184) {
            boolean is4001 = false;
            CB_IQTX_O001 tota = new CB_IQTX_O001();
            CB_IQTX_O002 tota2 = new CB_IQTX_O002();

            String outrtc = message.substring(176, 184);
            if (StringUtils.equals("F4F0F0F1", outrtc)) { // 主機 EBCDIC 編碼的 4001
                tota.parseCbsTele(message);
                is4001 = true;
            } else {
                tota2.parseCbsTele(message);
            }

            if (is4001) {
                return tota;
            } else {
                return tota2;
            }
        }
        return null;
    }
}
