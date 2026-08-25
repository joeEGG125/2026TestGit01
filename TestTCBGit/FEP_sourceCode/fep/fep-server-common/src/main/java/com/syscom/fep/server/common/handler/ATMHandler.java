package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Calendar;

/**
 * 處理來自ATMGW的電文Handler類
 *
 * @author Richard
 */
public class ATMHandler extends HandlerBase {
    private String atmText; // ATM原始電文(ASCII)
    private String atmNo; // ATM機台代號
    private String fsCode; // ATM交易代號
    private String atmSeq; // ATM交易流水號
    private String msgCategory; // Message category
    private String msgType; // Message type

    public String getAtmText() {
        return atmText;
    }

    public void setAtmText(String atmText) {
        this.atmText = atmText;
    }

    public String getAtmNo() {
        return atmNo;
    }

    public void setAtmNo(String atmNo) {
        this.atmNo = atmNo;
    }

    public String getFsCode() {
        return fsCode;
    }

    public void setFsCode(String fsCode) {
        this.fsCode = fsCode;
    }

    public String getAtmSeq() {
        return atmSeq;
    }

    public void setAtmSeq(String atmSeq) {
        this.atmSeq = atmSeq;
    }

    public String getMsgCategory() {
        return msgCategory;
    }

    public void setMsgCategory(String msgCategory) {
        this.msgCategory = msgCategory;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    /**
     * SPEC:TCB-FEP-SPC_Handler_ATMHandler(P1階段ATM交易控制程式)
     * 處理流程
     * 1.記錄LOG
     * 2.將ATM電文從EBCDIC轉成ASCII,並取出相關欄位
     * 3.讀取交易控制檔MsgCtl
     * 4.呼叫AA
     * 5.回傳Response電文
     */
    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        // 1.宣告變數
        // FEPReturnCode rtnCode = FEPReturnCode.ProgramException; //SPEC有，但無使用暫先mark
        ATMData atmData = new ATMData();
        ATMGeneral atmGeneral = new ATMGeneral(); // --ben--20220930Daniel指示:給小傑可順利測試用
        String methodName = StringUtils.join(ProgramName, ".dispatch");
        String atmRes = StringUtils.EMPTY;
        // 2.取EJ
        if (this.getEj() == 0) {
            this.setEj(TxHelper.generateEj());
        }
        if (StringUtils.isBlank(this.txRquid)) {
            this.txRquid = UUIDUtil.randomUUID(true);
        }
        // 3.記錄FEPLOG內容
        LogData logData = new LogData();
        logData.setEj(this.getEj());
        logData.setTxRquid(this.txRquid);
        logData.setChannel(channel);
        logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(methodName);
        logData.setMessage(data);
        logData.setAtmNo(atmNo);
        logData.setRemark("Enter dispatch");
        try {
            // 4.將Hex電文轉成ASCII, 並取出特定欄位供後續流程使用
            // this.setAtmText(StringUtil.fromHex(data));
            this.setAtmText(data);

            String ascText;
            // CK
            // if (data.length() < 100 && "C3D2".equals(data.substring(64, 68))) {
            if (data.length() > 64 && "C3D2".equals(data.substring(64, 68))) {
                // 換KEY電文
                ascText = EbcdicConverter.fromHex(CCSID.English, data.substring(0, 70));
                this.setAtmSeq(ascText.substring(26, 30));
                this.setFsCode(ascText.substring(32, 34));
                this.setMsgCategory(ascText.substring(11, 12));
                this.setMsgType(ascText.substring(12, 14));
                logData.setMessageId(this.getMsgCategory() + this.getMsgType() + "-" + this.getFsCode());
                atmData.setMessageID("ChangeKeyForATM");

            } else {
                // 一般交易電文
                // 原則上以Terminal ID作為ATMNo, 但某些交易沒有Terminal ID欄位, 作法待討論
                // 先轉前75byte
                // 補摺
                if ("40C5C2E3E2D7D4F0".equals(data.substring(0, 16))) {
                    logData.setMessageId("PBO");
                } else {
                    if (data.length() >= 150) {
                        ascText = EbcdicConverter.fromHex(CCSID.English, data.substring(0, 150));
                        this.setAtmSeq(ascText.substring(32, 36));
                        this.setFsCode(ascText.substring(73, 75));
                        this.setMsgCategory(ascText.substring(17, 18));
                        this.setMsgType(ascText.substring(18, 20));
                        logData.setMessageId(this.getMsgCategory() + this.getMsgType() + "-" + this.getFsCode());
                        logData.setAtmSeq(this.getAtmSeq());
                    }

                }
                // if (data.length() >= 150) {
                // 	ascText = EbcdicConverter.fromHex(CCSID.English, data.substring(0, 150));
                // 	this.setAtmSeq(ascText.substring(32, 36));
                // 	this.setFsCode(ascText.substring(73, 75));
                // 	this.setMsgCategory(ascText.substring(17, 18));
                // 	this.setMsgType(ascText.substring(18, 20));
                // } else {
                // 	// 補摺
                // 	if ("40C5C2E3E2D7D4F0".equals(data.substring(0, 16))) {
                // 		this.setAtmSeq(String.valueOf(this.getEj()));
                // 	}
                // }
                atmData.setMessageID("ATMTxForP1");
            }

            // 5.將相關欄位存入ATMData中
            atmData.setTxChannel(channel);
            atmData.setTxSubSystem(SubSystem.ATMP);
            atmData.setMessageFlowType(MessageFlow.Request);
            atmData.setAtmSeq(this.getAtmSeq());
            atmData.setTxRequestMessage(this.getAtmText());
            atmData.setEj(this.getEj());
            atmData.setTxRquid(this.txRquid);
            atmData.setMsgCategory(this.getMsgCategory());
            atmData.setMsgType(this.getMsgType());
            atmData.setFscode(this.getFsCode());
            atmData.setAtmNo(atmNo); // 改由atmservice controller帶進來
            atmData.setLogContext(logData);
            atmData.setTxObject(atmGeneral); // --ben--20220930Daniel指示:給小傑可順利測試用
            // 6.讀取MSGCTL,並存入atmData
            Msgctl msgctl = FEPCache.getMsgctrl(atmData.getMessageID());
            atmData.setMsgCtl(msgctl);
            // 讀不到MsgCtl先記EMS不回ATM
            if (atmData.getMsgCtl() == null) {
                logData.setReturnCode(CommonReturnCode.Abnormal);
                logData.setExternalCode("E551");
                logData.setRemark("於MSGCTL 找不到資料");
                sendEMS(logData);
                return atmRes;
            }
            // 7.呼叫 AA
            this.setChannel(atmData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
            atmData.setAaName(msgctl.getMsgctlAaName());
            atmData.setTxStatus(atmData.getMsgCtl().getMsgctlStatus() == 1);
            if (atmData.isTxStatus()) {
                atmRes = this.runAA(atmData);
            } else {
                return atmRes; // 不允許執行交易
            }
            // 8.回傳AA Response電文
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(methodName);
            logData.setMessage(atmRes);
            logData.setRemark("Exit dispatch");
            logMessage(logData);
            return atmRes;
        } catch (Throwable e) {
            logData.setProgramException(e);
            sendEMS(logData);
        }
        return atmRes;
    }

    private String runAA(ATMData atmData) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processRequestData", ATMData.class);
            String atmRes = (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, atmData);
            return atmRes;
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    @Override
    public String dispatch(FEPChannel channel, String data) {
        return "";
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) {
        return true;
    }

    private String getErrorResponseData(String tita, String errCode) {
        StringBuilder sb = new StringBuilder();
        // 從tita搬欄位組tota
        sb.append(tita.substring(4, 10)); // TXCD
        sb.append(tita.substring(84, 100)); // DATE
        sb.append(StringUtil
                .toHex(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN))); // TIME
        sb.append(tita.substring(28, 38)); // ATMNO
        sb.append(tita.substring(64, 66)); // MODE
        sb.append(tita.substring(66, 82)); // DD
        sb.append(tita.substring(82, 84)); // DEPMODE
        sb.append(tita.substring(84, 100)); // ATMSEQ_O1
        sb.append(tita.substring(100, 116)); // ATMSEQ_O2
        sb.append(StringUtil.toHex(errCode));
        return sb.toString();
    }

//	private String addHeader(String data, String xmtno) {
//		StringBuilder sb = new StringBuilder();
//		// 第0 – 2 BYTE ==Hex(0F),Hex(0F),Hex(0F)
//		sb.append("0F0F0F");
//		// 第3 – 5 BYTE長度
//		sb.append(StringUtils.leftPad(String.valueOf((data.length() / 2 + 12)), 6, '0'));
//		// 第6 BYTE 本筆資料中共含有幾筆TITA/TOTA,固定塞01
//		sb.append("01");
//		// 第7 – 9 BYTE XMTNO 從零起編
//		sb.append(xmtno);
//		// 第10 BYTE資料型態,0F 為DATA TITA,TOTA
//		sb.append("0F");
//		// 第 11 BYTE HEX 0F
//		sb.append("0F");
//		sb.append(data);
//		return sb.toString();
//	}

//	/**
//	 * 拆解ATM電文,並將欄位搬到大Class
//	 *
//	 * @param data
//	 * @param tota
//	 * @return
//	 * @throws Exception
//	 */
//	private ATMGeneral parseFlatfile(String data, RefString tota) throws Exception {
//		data = data.substring(24); // 去掉Control Header
//		ATMGeneral atmGeneral = new ATMGeneral();
//		// 檢查是否為A來判斷是否為百年電文
//		String checkATMVer = data.substring(10, 12);
//		if (!"41".equals(checkATMVer)) {
//			// Fly 2017/09/13 不合格式則回覆E552給ATMGW
//			tota.set("E552");
//			return atmGeneral;
//		}
//		ATMTextBase atmTextBase = ATMTextParser.getInstance().parse(data);
//		// modified By Maxine on 2012/01/04 for 找不到電文代碼全部回E551
//		if (atmTextBase == null) {
//			tota.set(this.getErrorResponseData(data, "E551"));
//			return null;
//		}
//		atmTextBase.toGeneral(atmGeneral);
//		// 為了方便check問題，這裡列印一下
//		LogHelperFactory.getTraceLogger().debug("[", ProgramName, ".parseFlatfile]ATM Request Data : ",
//				new Gson().toJson(atmGeneral.getRequest()));
//		return atmGeneral;
//	}

//	private String getMsgId(ATMData atmData, String txcd, String bkno, String bknoD) throws Exception {
//		String msgId = StringUtils.EMPTY;
//		if (atmData.getTxChannel() == FEPChannel.UATMP) {
//			return StringUtils.join(atmData.getTxChannel(), txcd);
//		}
//		ATMTXCD txid = null;
//		try {
//			txid = ATMTXCD.parse(txcd);
//		} catch (Exception e) {
//			return txcd;
//		}
//		if (txid.getCode() < 30) { // 控制類及結帳類交易直接取txcd=MsgId
//			msgId = txcd;
//		} else {
//			switch (txid) {
//				case IAC:
//				case IQ2:
//				case IFE:
//				case IFW:
//				case PNP:
//				case PN3:
//				case PNB:
//				case IFC:
//				case FWF:
//				case GIQ:
//				case B05:
//				case PNC:
//				case AAC:
//				case CDR:
//				case IDR:
//				case PNM:
//				case APP:
//					// 2011-05-09 by kyo for 開卡流程新電文
//				case AP1:
//				case PN0:
//				case PNX:
//				case ACF:
//				case ACW:
//				case AIN:
//				case IPA:
//				case IAF:
//				case CDF:
//				case B15:
//				case G51:
//				case BAK:
//				case BOX:
//				case CSH:
//				case DEX:
//				case ANB:
//				case INM: // add by maxine on 2011/11/01 for new 電文
//				case SMS:
//				case INI: // Modify by Ruling for 企業入金，新增INI電文
//				case BDR: // Modify by Ruling for 企業入金，新增BDR電文
//				case ICR: // 2012/09/03 Modify by Ruling for 硬幣機，新增ICR電文
//				case CCR: // 2012/09/03 Modify by Ruling for 硬幣機，新增CCR電文
//				case BDF: // 2013/02/08 Modify by Ruling for 企業入金機入帳
//				case ICW: // 2013/01/28 Modify by Ruling for 硬幣機提款，新增ICW電文
//					// case BAR: //2013/01/08 Modify by Ruling for 拉霸，新增BAR電文
//					// case BAC: //2013/01/08 Modify by Ruling for 拉霸，新增BAC電文
//				case BCD: // 2014/04/23 Modify by Ruling for ATM條碼
//				case CPN: // 2014/11/17 Modify by Ruling for Coupon
//				case G50: // 2016/06/21 Modify by Ruling for COMBO開卡作業優化
//				case ODE: // 2016-07-29 Modify by Ruiling for ATM新功能-跨行存款
//				case ODR: // 2016-07-29 Modify by Ruiling for ATM新功能-跨行存款
//				case ODF: // 2016-07-29 Modify by Ruiling for ATM新功能-跨行存款
//				case DDR: // 2016-07-29 Modify by Ruiling for ATM新功能-現金損款
//				case DDF: // 2016-07-29 Modify by Ruiling for ATM新功能-現金損款
//				case NCS: // 2017-02-14 Modify by Ruiling for 無卡提款
//					// Fly 2018/11/13 For OKI硬幣機
//				case CFT:
//				case CCF:
//				case CEX:
//					// 2018-10-16 Modify by Ruling for 外幣無卡提款
//				case NFE:
//				case NFF:
//				case NFW:
//					// 2019-05-06 Modify by Ruling for OKI硬幣機第二階段
//				case IQQ:
//					// 2020-09-18 Modify by Ruling for ATM提升分行離櫃率_全自助：掌靜脈
//				case PAC:
//				case PWD:
//				case PWF:
//				case PDR:
//				case PDF:
//				case PCR:
//				case PCF:
//					msgId = txcd;
//					break;
//				// 以下為需判斷是否為跨行的交易
//				// 2010-05-14 modified by kyo for 補上銀聯卡邏輯
//				case IIQ:
//					// 2015-03-26 Modify by Ruiling for 跨行外幣提款，新增FAE
//				case FAE:
//					msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno) ? txcd
//							: StringUtils.join(txcd, "2500");
//					break;
//				case IQC:
//					msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno) ? txcd
//							: StringUtils.join(txcd, "2401");
//					break;
//				// add by Maxine on 2011/09/21 for 國際卡餘額查詢
//				case IQV:
//					msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno) ? txcd
//							: StringUtils.join(txcd, "2411");
//					break;
//				case IQM:
//					msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno) ? txcd
//							: StringUtils.join(txcd, "2451");
//					break;
//				case IWD:
//				case IWF:
//					// 2010-11-01 by kyo for CWF須判斷自行跨行補上PCODE
//				case CWF:
//					// 2015-03-26 Modify by Ruiling for 跨行外幣提款，新增FAW、FAC
//				case FAW:
//				case FAC:
//				case NWD:// 2017-02-14 Modify by Ruiling for 無卡提款
//				case NWF:// 2017-02-14 Modify by Ruiling for 無卡提款
//					msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno) ? txcd
//							: StringUtils.join(txcd, "2510");
//					break;
//				case CWV:
//				case CFP:
//					msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno) ? txcd
//							: StringUtils.join(txcd, "2410");
//					break;
//				case CUP:
//				case CFU:
//					msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno) ? txcd
//							: StringUtils.join(txcd, "2400");
//					break;
//				case CWM:
//				case CFC:
//					msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno) ? txcd
//							: StringUtils.join(txcd, "2450");
//					break;
//				case CAV:
//				case CFV:
//					// msgId = atmData.getBin() != null ? txcd : StringUtils.join(txcd, "2420");
//					break;
//				case CAM:
//				case CFM:
//					// msgId = atmData.getBin() != null ? txcd : StringUtils.join(txcd, "2460");
//					break;
//				case CAJ:
//				case CFJ:
//					// msgId = atmData.getBin() != null ? txcd : StringUtils.join(txcd, "2480");
//					break;
//				case CAA:
//				case CFA:
//					// msgId = atmData.getBin() != null ? txcd : StringUtils.join(txcd, "2480");
//					break;
//				case AFF:
//				case ATF:
//					msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno)
//							&& !SysStatus.getPropertyValue().getSysstatHbkno().equals(bknoD)
//									? StringUtils.join(txcd, "2521")
//									: txcd;
//					break;
//				case IFT:
//				case IFF:
//					// 2020-09-18 Modify by Ruling for ATM提升分行離櫃率_全自助：掌靜脈
//				case PFT:
//				case PFF:
//					if (bkno.equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
//						msgId = !SysStatus.getPropertyValue().getSysstatHbkno().equals(bknoD)
//								? StringUtils.join(txcd, "2521")
//								: txcd;
//					} else {
//						if (bknoD.equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
//							msgId = StringUtils.join(txcd, "2522");
//						} else if (bkno.equals(bknoD)) {
//							msgId = StringUtils.join(txcd, "2523");
//						} else {
//							msgId = StringUtils.join(txcd, "2524");
//						}
//					}
//					break;
//				case EFF:
//				case EFT:
//				case OFF:// 2016-07-15 Modify by Ruling for ATM新功能-繳汽燃費
//				case OFT:// 2016-07-15 Modify by Ruling for ATM新功能-繳汽燃費
//					if (bkno.equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
//						if (!bknoD.equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
//							msgId = StringUtils.join(txcd, "2561");
//						} else {
//							msgId = txcd;
//						}
//					} else {
//						if (bknoD.equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
//							msgId = StringUtils.join(txcd, "2562");
//						} else if (bkno.equals(bknoD)) {
//							msgId = StringUtils.join(txcd, "2563");
//						} else {
//							msgId = StringUtils.join(txcd, "2564");
//						}
//					}
//					break;
//				case BFF:
//				case BFT:
//					// modified by maxine for bkno,bknod都=SYSSTAT_HBKNO時,msgid=BFT
//					msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno)
//							&& SysStatus.getPropertyValue().getSysstatHbkno().equals(bknoD) ? txcd
//									: StringUtils.join(txcd, "2521");
//					break;
//				case IPF:
//				case IPY:
//					msgId = SysStatus.getPropertyValue().getSysstatHbkno().equals(bkno) ? StringUtils.join(txcd, "2532")
//							: StringUtils.join(txcd, "2531");
//					break;
//				case INA:
//					// 2015-09-04 Modify by Fly for EMV 拒絕磁條卡交易
//					msgId = StringUtils.join(txcd, "2500");
//					break;
//				case EUP:
//				case EFU:
//					msgId = StringUtils.join(txcd, "2600");
//					break;
//				case EQC:
//					// 2016-12-22 Modify by Ruling for 拆自跨行
//					// msgId = atmData.getBin() != null ? txcd : StringUtils.join(txcd, "2631");
//					// msgId = txcd + "2631";
//					break;
//				case EQU:
//					msgId = txcd + "2601";
//					break;
//				case EFP:
//				case EWV:
//					// 2016-12-22 Modify by Ruling for 拆自跨行
//					// msgId = atmData.getBin() != null ? txcd : StringUtils.join(txcd, "2620");
//					break;
//				case EQP:
//					// 2016-12-22 Modify by Ruling for 拆自跨行
//					// msgId = atmData.getBin() != null ? txcd : StringUtils.join(txcd, "2621");
//					break;
//				case EAV:
//				case EFV:
//					// 2016-12-22 Modify by Ruling for 拆自跨行
//					// msgId = atmData.getBin() != null ? txcd : StringUtils.join(txcd, "2622");
//					break;
//				case EFC:
//				case EWM:
//					// 2016-12-22 Modify by Ruling for 拆自跨行
//					// msgId = atmData.getBin() != null ? txcd : StringUtils.join(txcd, "2630");
//					break;
//				case EAM:
//				case EFM:
//					// 2016-12-22 Modify by Ruling for 拆自跨行
//					// msgId = atmData.getBin() != null ? txcd : StringUtils.join(txcd, "2632");
//					break;
//				default:
//					break;
//			}
//		}
//		return msgId;
//	}
}
