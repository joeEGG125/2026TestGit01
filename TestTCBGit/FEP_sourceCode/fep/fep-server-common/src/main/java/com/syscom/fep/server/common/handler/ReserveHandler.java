package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.model.Bin;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.BINPROD;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Calendar;

/**
 * SelfODR會呼叫
 *
 * @author Richard
 */
public class ReserveHandler extends HandlerBase {
    private static final String ProgramName = ReserveHandler.class.getSimpleName();
    @SuppressWarnings("unused")
    private static final int StopTx = 0;
    private ATMData atmData;
    private String atmNo;
    private String atmSeq;

    public ReserveHandler() {}

    public String getAtmNo() {
        return atmNo;
    }

    public void setAtmNo(String atmNo) {
        this.atmNo = atmNo;
    }

    public String getAtmSeq() {
        return atmSeq;
    }

    public void setAtmSeq(String atmSeq) {
        this.atmSeq = atmSeq;
    }

    /**
     * 處理流程
     * 記錄電文LOG
     * 拆解電文成Object
     * 判斷電文中的交易代碼
     * 根據交易代碼初始化AA物件
     * 組AAMsgData
     * 呼叫AA Main function
     * 取得AA回傳電文後,記錄電文LOG
     * 回傳AA執行結果
     *
     * @param channel
     * @param generalText
     * @return
     */
    public ATMGeneral dispatch(FEPChannel channel, ATMGeneral generalText) {
        String methodName = StringUtils.join(ProgramName, ".dispatch");
        String atmRes = StringUtils.EMPTY;
        String xmt = StringUtils.EMPTY;
        ATMGeneral gal = null;
        // 取EJ
        if (this.ej == 0) {
            this.ej = TxHelper.generateEj();
        }
        // 2016-11-25 Modify by Ruling for 非實體ATM交易需避免因ATM交易序號為NULL而無法寫EMS的問題
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.ATMP);
        logData.setChannel(channel);
        logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(methodName);
        logData.setEj(this.ej);
        logData.setMessageId(this.getMessageId());
        logData.setAtmNo(this.atmNo);
        logMessage(Level.DEBUG, logData);
        try {
            gal = generalText;
            // 過濾ICMARK欄位的雜質第一位是"."就濾掉
            //--ben-20220922-//if (StringUtils.isNotBlank(gal.getRequest().getICMARK()) && ".".equals(gal.getRequest().getICMARK().substring(0, 1))) {
            //--ben-20220922-//	gal.getRequest().setICMARK(StringUtils.EMPTY);
            //--ben-20220922-//}
            this.atmData = new ATMData();
            // CheckBin
            Bin bin = this.checkBinForATM(gal);
            this.atmData.setBin(bin);
            this.atmData.setMessageCorrelationID(this.messageCorrelationId);
            logData.setChannel(channel);
            //--ben-20220922-//logData.setTrinActno(gal.getRequest().getActD());
            //--ben-20220922-//logData.setTroutActno(gal.getRequest().getTXACT());
            //--ben-20220922-//logData.setTrinBank(gal.getRequest().getBknoD());
            //--ben-20220922-//logData.setTroutBank(gal.getRequest().getBKNO());
            //--ben-20220922-//logData.setTxDate(gal.getRequest().getAtmseq_1());
            this.atmData.setTxChannel(channel);
            //--ben-20220922-//this.atmData.setMessageID(this.getMsgId(gal.getRequest().getTXCD(), gal.getRequest().getBKNO(), gal.getRequest().getBknoD()));
            logData.setMessageId(this.atmData.getMessageID());
            this.setMessageId(this.atmData.getMessageID());
            this.atmData.setLogContext(logData);
            this.atmData.setTxSubSystem(SubSystem.ATMP);
            this.atmData.setMessageFlowType(MessageFlow.Request);
            this.atmData.setAtmSeq(this.atmSeq);
            this.atmData.setTxObject(gal);
            this.atmData.setEj(this.ej);
            // 讀出交易控制檔本筆交易資料
            this.atmData.setMsgCtl(FEPCache.getMsgctrl(atmData.getMessageID()));
            if (this.atmData.getMsgCtl() == null) {
                throw ExceptionUtil.createException("讀不到MsgID:", atmData.getMessageID());
            }
            this.setChannel(atmData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
            this.atmData.setAaName(this.atmData.getMsgCtl().getMsgctlAaName());
            this.atmData.setTxStatus(this.atmData.getMsgCtl().getMsgctlStatus() == (short) 1);
            if (bin != null) {
                // 檢核信用卡主機連線狀態
// 2024-03-06 Richard modified for SYSSTATE 調整
//				if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatAsc())) {
//					this.atmData.getMsgCtl().setMsgctlStatus((short) 0); // 暫停此交易
//					this.atmData.setTxStatus(false);
//				} else {
                // COMBO卡信用卡, 檢核永豐晶片錢卡連線狀態
                if (BINPROD.Combo.equals(bin.getBinProd())) {
// 2024-03-06 Richard modified for SYSSTATE 調整
//						if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatAscmd())) {
//							this.atmData.getMsgCtl().setMsgctlStatus((short) 0); // 暫停此交易
//							this.atmData.setTxStatus(false);
//						}
                }
//				}
            }
            // call AA
            @SuppressWarnings("unused")
            String subsys = this.atmData.getMsgCtl().getMsgctlSubsys().toString();// 得到為哪個子系統
            atmRes = this.runAA();
            if (StringUtils.isNotBlank(atmRes) && !"0F0F0F".equals(atmRes.substring(0, 6))) {
                atmRes = this.addHeader(atmRes, xmt);
            }
        } catch (Throwable e) {
            // 2016-08-31 Modify by Ruling for ATM新功能
            if (channel == FEPChannel.ATM) {
                // ATM，ProgramException會轉為2999
                //ben20221118  gal.getResponse().setREJCD(AbnormalRC.ATM_Error);
            } else {
                // 批次-整批轉即時和預約，ProgramException會轉為EF0099
                //ben20221118  gal.getResponse().setCHLREJCD("EF0099");
            }
            logData.setProgramException(e);
            sendEMS(logData);
        } finally {
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, "dispatch"));
            logData.setMessage(atmRes);
            logMessage(Level.DEBUG, logData);
        }
        return this.atmData.getTxObject();
    }

    @Override
    public String dispatch(FEPChannel channel, String data) {
        return null;
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return false;
    }

    private String addHeader(String data, String xmtno) {
        StringBuilder sb = new StringBuilder();
        // 第0 – 2 BYTE ==Hex(0F),Hex(0F),Hex(0F)
        sb.append("0F0F0F");
        // 第3 – 5 BYTE長度
        sb.append(StringUtils.leftPad(String.valueOf((data.length() / 2 + 12)), 6, '0'));
        // 第6 BYTE 本筆資料中共含有幾筆TITA/TOTA,固定塞01
        sb.append("01");
        // 第7 – 9 BYTE XMTNO 從零起編
        sb.append(xmtno);
        // 第10 BYTE資料型態,0F 為DATA TITA,TOTA
        sb.append("0F");
        // 第 11 BYTE HEX 0F
        sb.append("0F");
        sb.append(data);
        return sb.toString();
    }

    private String getMsgId(String txcd, String bkno, String bknoD) throws Exception {
        String msgId = StringUtils.EMPTY;
        @SuppressWarnings("unused")
        String pcode = StringUtils.EMPTY;
//		if (atmData.getTxChannel() == FEPChannel.UATMP) {
//			return StringUtils.join(atmData.getTxChannel(), txcd);
//		}
        ATMTXCD txid = ATMTXCD.parse(txcd);
        if (txid.getCode() < 30) // 控制類及結帳類交易直接取txcd=MsgId
        {
            msgId = txcd;
        } else {
            switch (txid) {
                case IAC:
                case IQ2:
                case IFE:
                case IFW:
                case PNP:
                case PN3:
                case PNB:
                case IFC:
                case FWF:
                case GIQ:
                case B05:
                case PNC:
                case AAC:
                case CDR:
                case IDR:
                case PNM:
                case APP:
                case CWF:
                case ACF:
                case ACW:
                case AIN:
                case IPA:
                case IAF:
                case CDF:
                case B15:
                case G51:
                case BAK:
                case BOX:
                case CSH:
                case DEX:
                    msgId = txcd;
                    break;
                // 以下為需判斷是否為跨行的交易
                // 2010-05-14 modified by kyo for 補上銀聯卡邏輯
                case IIQ:
                    msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno) ? txcd : StringUtils.join(txcd, "2500");
                    break;
                case IQC:
                    msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno) ? txcd : StringUtils.join(txcd, "2401");
                    break;
                case IWD:
                case IWF:
                    msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno) ? txcd : StringUtils.join(txcd, "2510");
                    break;
                case CWV:
                case CFP:
                    msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno) ? txcd : StringUtils.join(txcd, "2410");
                    break;
                case CUP:
                case CFU:
                    msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno) ? txcd : StringUtils.join(txcd, "2400");
                    break;
                case CWM:
                case CFC:
                    msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno) ? txcd : StringUtils.join(txcd, "2450");
                    break;
                case CAV:
                case CFV:
                    msgId = atmData.getBin() != null ? txcd : StringUtils.join(txcd, "2420");
                    break;
                case CAM:
                case CFM:
                    msgId = atmData.getBin() != null ? txcd : StringUtils.join(txcd, "2460");
                    break;
                case CAJ:
                case CFJ:
                    msgId = atmData.getBin() != null ? txcd : StringUtils.join(txcd, "2480");
                    break;
                case CAA:
                case CFA:
                    msgId = atmData.getBin() != null ? txcd : StringUtils.join(txcd, "2480");
                    break;
                case AFF:
                case ATF:
                    msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno)
                            && !bknoD.equals(SysStatus.getPropertyValue().getSysstatHbkno()) ? StringUtils.join(txcd, "2521") : txcd;
                    break;
                case IFT:
                case IFF:
                case ODT:
                    // 2016-07-28 Modify by Ruling for ATM新功能：跨行存款轉入交易
                    if (SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno)) {
                        msgId = !SysStatus.getPropertyValue().getSysstatHbkno().equals(bknoD) ? StringUtils.join(txcd, "2521") : txcd;
                    } else {
                        if (SysStatus.getPropertyValue().getSysstatHbkno().equals(bknoD)) {
                            msgId = StringUtils.join(txcd, "2522");
                        } else if (bkno.equals(bknoD)) {
                            msgId = StringUtils.join(txcd, "2523");
                        } else {
                            msgId = StringUtils.join(txcd, "2524");
                        }
                    }
                    break;
                case EFF:
                case EFT:
                    // 2014-04-18 Modify by Ruling for 整批轉即時：FCS交易目前沒使用由256X改為226X
                    if (SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno)) {
                        if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(bknoD)) {
                            msgId = StringUtils.join(txcd, "2261");
                        } else {
                            msgId = txcd;
                        }
                    } else {
                        if (SysStatus.getPropertyValue().getSysstatHbkno().equals(bknoD)) {
                            msgId = StringUtils.join(txcd, "2262");
                        } else if (bkno.equals(bknoD)) {
                            msgId = StringUtils.join(txcd, "2263");
                        } else {
                            msgId = StringUtils.join(txcd, "2264");
                        }
                    }
                    break;
                case BFF:
                case BFT:
                    msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno)
                            && !SysStatus.getPropertyValue().getSysstatHbkno().equals(bknoD) ? StringUtils.join(txcd, "2521") : txcd;
                    break;
                case IPF:
                case IPY:
                    msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno) ? StringUtils.join(txcd, "2532") : StringUtils.join(txcd, "2531");
                    break;
                default:
                    break;
            }
        }
        return msgId;
    }

    private String runAA() throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processInbkRequestData", ATMData.class);
            // 呼叫AA將ATMData傳進去
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, this.atmData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    @SuppressWarnings("unused")
    private String getErrorResponseData(String tita, String errCode) {
        StringBuilder sb = new StringBuilder();
        // 從tita搬欄位組tota
        sb.append(tita.substring(4, 10)); // TXCD
        sb.append(tita.substring(84, 100)); // DATE
        sb.append(StringUtil.toHex(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN))); // TIME
        sb.append(tita.substring(28, 38)); // ATMNO
        sb.append(tita.substring(64, 66)); // MODE
        sb.append(tita.substring(66, 82)); // DD
        sb.append(tita.substring(82, 84)); // DEPMODE
        sb.append(tita.substring(84, 100)); // ATMSEQ_O1
        sb.append(tita.substring(100, 116)); // ATMSEQ_O2
        sb.append(StringUtil.toHex(errCode));
        return sb.toString();
    }

    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        // TODO Auto-generated method stub
        return null;
    }
}
