package com.syscom.fep.server.common.business.host;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.server.common.adapter.NBAdapter;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.app.APPGeneral;
import com.syscom.fep.vo.text.app.APPGeneralResponse;
import com.syscom.fep.vo.text.app.request.ReplyDeviceInvoiceRequest;
import com.syscom.fep.vo.text.app.request.TaiwanPayRequest;
import com.syscom.fep.vo.text.app.response.ReplyDeviceInvoiceResponse;
import com.syscom.fep.vo.text.app.response.TaiwanPayResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class App extends HostBase {
    private MessageBase _txData;


    public App(MessageBase txData) {
        super(txData);
        this._txData = txData;
    }

    public FEPReturnCode sendToAPP(String action) {
        FEPReturnCode rtn = FEPReturnCode.Normal;
        @SuppressWarnings("unused")
		String rtnData = "";
        NBAdapter APPAdapter = new NBAdapter(_txData);
        APPGeneral APPGeneralForReq = new APPGeneral();
        APPAdapter.setAction("APP");
        try {
// 2024-03-06 Richard modified for SYSSTATE 調整
//            if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatNb())) {//暫停永豐網銀通道
//                if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
//                    if (StringUtils.isBlank(getFeptxn().getFeptxnTxCode())) {//原存行交易
//                        getFeptxn().setFeptxnAscRc(String.valueOf(FEPReturnCode.InterBankServiceStop.getValue()));
//                    } else {//代理行交易
//                        getFeptxn().setFeptxnAscRc(String.valueOf(FEPReturnCode.SenderBankServiceStop.getValue()));
//                    }
//                } else {
//                    getFeptxn().setFeptxnAscRc(String.valueOf(FEPReturnCode.WithdrawServiceStop.getValue()));
//                }
//                getLogContext().setRemark("暫停永豐網銀通道");
//                logMessage(Level.DEBUG, getLogContext());
//                this.feptxnDao.updateByPrimaryKeySelective(getFeptxn());
//                return FEPReturnCode.Normal;
//            }

            switch (action) {
                case "1": {//交易結果通知
                    if ("TWD".equals(getFeptxn().getFeptxnTxCur())) {
                        APPGeneralForReq.getmRequest().setAmt(getFeptxn().getFeptxnTxAmt().toString());
                    } else {
                        //Fly 2019/08/01 調整抓取欄位
                        APPGeneralForReq.getmRequest().setAmt(getFeptxn().getFeptxnCashAmt().toString());
                    }
                    APPGeneralForReq.getmRequest().setTerminalID(getFeptxn().getFeptxnAtmno());
                    APPGeneralForReq.getmRequest().setLocalDateTime(getFeptxn().getFeptxnTxDatetimeFisc());

                    //Fly 2019/10/16 增加確認交易日期時間
                    if (StringUtils.isBlank(getFeptxn().getFeptxnConTxTime())) {
                        APPGeneralForReq.getmRequest().setConfirmDateTime("");
                    } else {
                        if (Integer.parseInt(getFeptxn().getFeptxnConTxTime()) >= Integer.parseInt(getFeptxn().getFeptxnTxTime())) {
                            APPGeneralForReq.getmRequest().setConfirmDateTime(getFeptxn().getFeptxnTxDate() + getFeptxn().getFeptxnConTxTime());
                        } else {
                            SimpleDateFormat sdf =  new SimpleDateFormat( "yyyyMMdd" );
                            Date date = sdf.parse(getFeptxn().getFeptxnTxDate().replace("-","").replace("/",""));
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.add(Calendar.DAY_OF_MONTH,1);
                            sdf.format(calendar.getTime());
                            APPGeneralForReq.getmRequest().setConfirmDateTime(sdf.format(calendar.getTime()) + getFeptxn().getFeptxnConTxTime());
                        }
                    }

                    APPGeneralForReq.getmRequest().setOrderNumber(getFeptxn().getFeptxnOrderNo());
                    APPGeneralForReq.getmRequest().setMerchantID(getFeptxn().getFeptxnMerchantId());
                    APPGeneralForReq.getmRequest().setCardNumber(getFeptxn().getFeptxnTroutActno());
                    APPGeneralForReq.getmRequest().setSourceType(getFeptxn().getFeptxnAtmType());
                    APPGeneralForReq.getmRequest().setTXNDID(getFeptxn().getFeptxnDesBkno() + "0000");
                    APPGeneralForReq.getmRequest().setTXNSID(getFeptxn().getFeptxnBkno() + "0000");
                    APPGeneralForReq.getmRequest().setSTAN(getFeptxn().getFeptxnStan());
                    APPGeneralForReq.getmRequest().setSNUM(getFeptxn().getFeptxnIcSeqno());
                    APPGeneralForReq.getmRequest().setMti("0202");
                    APPGeneralForReq.getmRequest().setProcessingCode("00" + getFeptxn().getFeptxnPcode());
                    APPGeneralForReq.getmRequest().setResponseCode(getFeptxn().getFeptxnConRc());
                    //Fly 2020/5/7 (週四) 下午 03:53 QRP 2543交易SPEC更新 增加送ORI_STAN
                    if (!StringUtils.isBlank(getFeptxn().getFeptxnOriStan())) {
                        APPGeneralForReq.getmRequest().setOriSTAN(getFeptxn().getFeptxnOriStan());
                    }
                    TaiwanPayRequest reqClass = new TaiwanPayRequest();
                    switch (getFeptxn().getFeptxnPcode()) {
                        case "2542": //消費扣款沖正交易
                            APPAdapter.setAPPUrl("ReplyCancelResult");
                            break;
                        case "2543": //消費扣款退貨交易
                            APPAdapter.setAPPUrl("ReplyRefundResult");
                            break;
                        default:
                            APPAdapter.setAPPUrl("ReplyTransResult");
                            break;
                    }
                    APPAdapter.setMessageToNB(reqClass.makeMessageFromGeneral(APPGeneralForReq));
                    break;
                }
                case "2": {//查詢客戶設定的雲端發票手機條碼
                    APPGeneralForReq.getmRequest().setCardNumber(getFeptxn().getFeptxnTroutActno());
                    ReplyDeviceInvoiceRequest reqClass = new ReplyDeviceInvoiceRequest();
                    APPAdapter.setAPPUrl("ReplyDeviceInvoice");
                    APPAdapter.setMessageToNB(reqClass.makeMessageFromGeneral(APPGeneralForReq));
                    break;
                }
            }


            getFeptxn().setFeptxnAscTimeout(DbHelper.toShort(true)); ///* CBS 逾時 FLAG */
            if (this.feptxnDao.updateByPrimaryKeySelective(getFeptxn()) <= 0) {//RC = L013
                return IOReturnCode.FEPTXNUpdateError;
            }
            rtn = APPAdapter.sendReceive();

            if (rtn == FEPReturnCode.Normal) {
                getFeptxn().setFeptxnAscTimeout(DbHelper.toShort(false)); ///* CBS 逾時 FLAG */
            }

            if (!StringUtils.isBlank(APPAdapter.getMessageFromNB())) {
                switch (action) {
                    case "1": {//交易結果通知
                        TaiwanPayResponse appRes = new TaiwanPayResponse();
                        appRes.parseFlatfile(APPAdapter.getMessageFromNB(), APPGeneralForReq);
                        break;
                    }
                    case "2": {//查詢客戶設定的雲端發票手機條碼
                        ReplyDeviceInvoiceResponse appRes = new ReplyDeviceInvoiceResponse();
                        appRes.parseFlatfile(APPAdapter.getMessageFromNB(), APPGeneralForReq);
                        break;
                    }
                }
                APPGeneralResponse tempVar2 = APPGeneralForReq.getmResponse();
                if ("SUCCESS".equals(tempVar2.getSuccess()) && StringUtils.isBlank(tempVar2.getRetuenCode())) {
                    getFeptxn().setFeptxnAscRc("0000");
                    if (action.equals("2")) {
                        getFeptxn().setFeptxnTelephone(tempVar2.getDeviceInvoice());
                    }
                }

            }

            this.feptxnDao.updateByPrimaryKeySelective(getFeptxn());
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.Normal;
        }
        return FEPReturnCode.Normal;

    }
}
