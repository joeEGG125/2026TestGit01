package com.syscom.fep.web.controller.rm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.mybatis.model.Allbank;
import com.syscom.fep.mybatis.model.Msgout;
import com.syscom.fep.mybatis.model.Rminsno;
import com.syscom.fep.mybatis.model.Rmoutsno;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.MSGOUTStatus;
import com.syscom.fep.vo.constant.RMCategory;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.rm.UI_028030_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 更換跨行通匯基碼
 * @author ZK
 * @create 2021/11/23
 */
@Controller
public class UI_028030Controller extends BaseController {

    private FEPReturnCode _rtnCode;
    private String _rtnMessage = "";
    private int _EJ;

    //add by maxine on 2011/06/24 for SYSSTAT自行查
    private List<Sysstat> _dtSYSSTAT;
    private String _SYSSTAT_HBKNO;


    @Autowired
    RmService rmService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        //add by maxine on 2011/06/24 for SYSSTAT自行查
        querySYSSTAT(mode);
        try {
            _SYSSTAT_HBKNO = SysStatus.getPropertyValue().getSysstatHbkno();
        } catch (Exception e) {
        	this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
        }
        WebUtil.putInAttribute(mode, AttributeName.Form, new UI_028030_Form());
    }

    private void querySYSSTAT(ModelMap mode) {
        try {
            _dtSYSSTAT = rmService.getStatus();
            if (_dtSYSSTAT.size() < 1) {
                this.showMessage(mode,MessageType.DANGER,"SYSSTAT無資料!!");
                return;
            }
            _SYSSTAT_HBKNO = _dtSYSSTAT.get(0).getSysstatHbkno();
        } catch (Exception ex) {
            this.showMessage(mode,MessageType.DANGER,ex.toString());
        }
    }


    @PostMapping(value = "/rm/UI_028030/executeClick")
    @ResponseBody
    private BaseResp<?> executeClick(@RequestBody UI_028030_Form form) {
        this.infoMessage("查詢明細數據, 條件 = [", form, "]");
        _rtnCode = CommonReturnCode.Normal;
        _rtnMessage = "";
        BaseResp<?> response = new BaseResp<>();
        response.setMessage(MessageType.INFO,_rtnMessage);

        if (!"4".equals(form.getTxkind())) {
            if (StringUtils.isBlank(form.getBkno())) {
                _rtnMessage = "「收信行總行代號」未輸入";
                response.setMessage(MessageType.DANGER,_rtnMessage + " ");
                return response;
            } else {
                //modified by maxine on 2011/06/24 for SYSSTAT自行查
                //If BKNOtxt.Text = SysStatus.PropertyValue.SYSSTAT_HBKNO Then
                if (_SYSSTAT_HBKNO.equals(form.getBkno())) {
                    _rtnMessage = "「收信行總行代號」不能為本行總行代號";
                    response.setMessage(MessageType.DANGER,_rtnMessage + " ");
                    return response;
                }
            }
        } else {
            if (!"ALL".equals(form.getConfiRm())) {
                _rtnMessage = "「交易項目確認」未輸入或不正確";
                response.setMessage(MessageType.DANGER,_rtnMessage + " ");
                return response;
            }
        }

        execute(form);
        if (_rtnCode != CommonReturnCode.Normal) {
            //modified by Maxine on 2011/08/02 for 補送EMS
            prepareAndSendEMSData(StringUtils.join(_rtnMessage , " " , TxHelper.getMessageFromFEPReturnCode(_rtnCode)),form.getBkno(),form.getTxkind(),response);
            response.setMessage(MessageType.DANGER,_rtnMessage + " " + TxHelper.getMessageFromFEPReturnCode(_rtnCode));
        } else {
            //modified by Maxine on 2011/08/02 for 補送EMS
            prepareAndSendEMSData(DealSuccess,form.getBkno(),form.getTxkind(),response);
            response.setMessage(MessageType.INFO,DealSuccess);
        }
        return response;
    }

    /**
     讀取資料主流程


     */
    private void execute(UI_028030_Form form) {
        String SenderBank = "";
        String ReceiverBank = "";

        try {
            switch (form.getTxkind()) {
                case "1":
                case "3":
                case "4":
                    ReceiverBank = form.getBkno(); //收信行(對方行)
                    //modified by maxine on 2011/06/24 for SYSSTAT自行查
                    SenderBank = _SYSSTAT_HBKNO; //發信行(本行)
                    //SenderBank = SysStatus.PropertyValue.SYSSTAT_HBKNO '發信行(本行)
                    break;
                case "2":
                    //modified by maxine on 2011/06/24 for SYSSTAT自行查
                    ReceiverBank = _SYSSTAT_HBKNO; //收信行(本行)
                    //ReceiverBank = SysStatus.PropertyValue.SYSSTAT_HBKNO '收信行(本行)
                    SenderBank = form.getBkno(); //發信行(對方行)
                    break;
            }

            switch (form.getTxkind()) {
                case "1": {//匯出 組新基碼送出
                    Rmoutsno defRMOUTSNO = new Rmoutsno();
                    //檢核銀行是否存在
                    isBankExist(form.getBkno());

                    //讀取匯出通匯序號檔
                    if (_rtnCode == CommonReturnCode.Normal) {
                        RefBase<Rmoutsno> refRmoutsno = new RefBase<>(defRMOUTSNO);
                        this.getRMOUTSNO(ReceiverBank, SenderBank, refRmoutsno);
                        defRMOUTSNO = refRmoutsno.get();
                    }

                    if ("1".equals(defRMOUTSNO.getRmoutsnoChgk())) {
                        _rtnCode = CommonReturnCode.Abnormal;
                        _rtnMessage = AlreadyChangedKey;
                    }

                    //換KEY 並儲存RMOUTSNO
                    if (_rtnCode == CommonReturnCode.Normal) {
                        changeCDKEY(ReceiverBank, SenderBank, defRMOUTSNO.getRmoutsno3des());
                    }

                    break;
                }
                case "2": {//匯入 取消新舊並存狀態

                    //檢查RMINSNO這筆資料是否存在
                    isRMINSNOExist(ReceiverBank, SenderBank);

                    //更新RMINSNO
                    if (_rtnCode == CommonReturnCode.Normal)
                    {
                        updateRMINSNO(ReceiverBank, SenderBank);
                    }

                    break;
                }
                case "3": {//取消本行換KEY進行中記號
                    Rmoutsno defRMOUTSNO = new Rmoutsno();

                    //檢查RMOUTSNO是否存在
                    RefBase<Rmoutsno> refRmoutsno = new RefBase<>(defRMOUTSNO);
                    getRMOUTSNO(ReceiverBank, SenderBank, refRmoutsno);
                    refRmoutsno.get();


                    //取消本行換KEY進行中記號
                    if (_rtnCode == CommonReturnCode.Normal) {
                        updateRMOUTSNO(ReceiverBank, SenderBank, "0");
                    }

                    break;
                }
                case "4": {
                    List<Rmoutsno> dtRMOUTSNO = new ArrayList<>();

                    //讀取RMOUTSNO裡的本行對應接收行
                    dtRMOUTSNO = getRMOUTSNOBySenderBank();

                    if (_rtnCode == CommonReturnCode.Normal) {
                        //只要一筆失敗就停止
                        for (Rmoutsno dr : dtRMOUTSNO) {
                            ReceiverBank = dr.getRmoutsnoReceiverBank();
                            changeCDKEY(ReceiverBank, SenderBank, dr.getRmoutsno3des());
                            if (_rtnCode != CommonReturnCode.Normal) {
                                _rtnMessage = "收信行" + ReceiverBank + "更換基碼失敗";
                                return;
                            }
                        }
                    }
                    break;
                }
            }

            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnMessage = DealSuccess;
            }

        } catch (Exception ex) {
            _rtnCode = CommonReturnCode.ProgramException;
            _rtnMessage = DealFail;
        }

    }

    private void prepareAndSendEMSData(String strMsg,String bkno,String txKind,BaseResp<?> response) {
        String txKindItem = "";
        switch (txKind) {
            case "1":
                txKindItem = "匯出-組新基碼送出";
                break;
            case "2":
                txKindItem = "匯入-取消新舊並存狀態";
                break;
            case "3":
                txKindItem = "取消本行換KEY進行中記號";
                break;
            case "4":
                txKindItem = "匯出-對所有銀行組新基碼送出";
                break;
            default:
                break;

        }
        LogData logContext = new LogData();
        List<Sysstat> dtSYSSTAT = new ArrayList<>();
        try {
            dtSYSSTAT = rmService.getStatus();
            if (dtSYSSTAT.size() < 1) {
                response.setMessage(MessageType.DANGER,"SYSSTAT無資料!!");
                return;
            }
        } catch (Exception ex) {
            response.setMessage(MessageType.DANGER,ex.toString());
            return;
        }

        logContext.setChannel(FEPChannel.FEP);
        logContext.setSubSys(SubSystem.RM);
        logContext.setProgramName("UI_028030");
        logContext.setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        logContext.setDesBkno(dtSYSSTAT.get(0).getSysstatHbkno());
        logContext.setMessageId("UI028030");
        logContext.setMessageGroup("4"); // /*RM*/
        logContext.setMessageParm13(StringUtils.join("本行更換通匯基碼" , "收信行=" , bkno , txKindItem , strMsg));
        logContext.setTxUser(WebUtil.getUser().getUserId());
        logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(RMReturnCode.MBankChangekey, logContext));
        rmService.logMessage(logContext, Level.INFO);

    }

    /**
     換KEY

     @param ReceiverBank 解款行
     @param SenderBank 匯款行
     @param wk3DES 3DES

     */
    private void changeCDKEY(String ReceiverBank, String SenderBank, String wk3DES) throws Exception {
        String repMAC = "";
        String repSYNC = "";
        _EJ = TxHelper.generateEj();

        //2011-04-08 by kyo for des介面修改新增txdata參數，web沒有用到傳nothing
        ENCHelper rmdes = new ENCHelper("", FEPChannel.FISC, SubSystem.RM, _EJ, "", "", null);


        //得到一組新的RMCDKey
        try {
            RefString refRepMAC = new RefString(repMAC);
            RefString refRepSYNC = new RefString(repSYNC);
            _rtnCode = rmdes.makeRMCDKey(ReceiverBank, wk3DES, refRepMAC, refRepSYNC);
            repMAC = refRepMAC.get();
            repSYNC = refRepSYNC.get();
        } catch (Exception ex) {
            _rtnCode = CommonReturnCode.ProgramException;
        }

        //新增訊息匯訊檔並RMOUTSNO_CHGK 標記更新為1
        if (_rtnCode == CommonReturnCode.Normal) {
            addMSGOUTandUpdateRMOUTSNO(ReceiverBank, SenderBank, repMAC, repSYNC, wk3DES);
        }

    }

    /**
     新增一筆MSGOUT記錄
     並更新RMOUTSNO_CHGK 為換KEY中

     @param repMAC
     @param repSYNC

     */
    private void addMSGOUTandUpdateRMOUTSNO(String ReceiverBank, String SenderBank, String repMAC, String repSYNC, String wk3DES) throws Exception {
        Msgout defMSGOUT = new Msgout();
        Rmoutsno defRMOUTSNO = new Rmoutsno();

        defMSGOUT.setMsgoutTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        defMSGOUT.setMsgoutBrno(VirBRNO);
        defMSGOUT.setMsgoutFepno(StringUtils.leftPad(String.valueOf(rmService.getRmNo(defMSGOUT.getMsgoutBrno(), MSGOUTStatus.Canceled)), 7, '0'));
        defMSGOUT.setMsgoutFepsubno(FEPSubNoZero);
        defMSGOUT.setMsgoutCategory(RMCategory.MSGOut); //一般通訊匯出類
        //modified by maxine on 2011/06/24 for SYSSTAT自行查
        defMSGOUT.setMsgoutSenderBank(StringUtils.rightPad(_SYSSTAT_HBKNO,7, '0'));
        //defMSGOUT.MSGOUT_SENDER_BANK = SysStatus.PropertyValue.SYSSTAT_HBKNO.PadRight(7, "0"c)

        defMSGOUT.setMsgoutReceiverBank(StringUtils.rightPad(ReceiverBank, 7, '0'));
        defMSGOUT.setMsgoutStat(MSGOUTStatus.Send); //已發訊
        //5/13 Modify by Matt
        defMSGOUT.setMsgoutRegdate(defMSGOUT.getMsgoutTxdate());
        defMSGOUT.setMsgoutRegtime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
        defMSGOUT.setMsgoutApdate(defMSGOUT.getMsgoutTxdate());
        defMSGOUT.setMsgoutAptime(defMSGOUT.getMsgoutRegtime());

        defMSGOUT.setMsgoutEjno(_EJ);
        defMSGOUT.setMsgoutChnmemo(SelfChangedKey);
        defMSGOUT.setMsgoutFiscSndCode("1411");

        //1:SingleDes 3:3DES
        if ("3".equals(wk3DES)) {
            defMSGOUT.setMsgoutEngmemo(StringUtils.join("CHG3" , repMAC , repSYNC));
        } else {
            defMSGOUT.setMsgoutEngmemo(StringUtils.join("CHGK" , repMAC , repSYNC));
        }

        defMSGOUT.setMsgoutRegTlrno(WebUtil.getUser().getUserId());


        defRMOUTSNO.setRmoutsnoChgk("1");
        defRMOUTSNO.setRmoutsnoSenderBank(SenderBank);
        defRMOUTSNO.setRmoutsnoReceiverBank(ReceiverBank); //BKNOtxt.Text

        _rtnCode = rmService.addMSGOUTandUpdateRMOUTSNO(defMSGOUT, defRMOUTSNO);
    }

    /**
     檢核是否有此銀行全國銀行檔

     @return

     */
    private int isBankExist(String bkno) throws Exception {
        int intRtn = 0;
        Allbank defALLBANK = new Allbank();
        defALLBANK.setAllbankBkno(bkno);
        defALLBANK.setAllbankBrno("000");
        intRtn = rmService.getALLBANKbyPK(defALLBANK);
        if (intRtn < 1) {
            _rtnCode = IOReturnCode.ALLBANKNotFound;
            _rtnMessage = NoBankID;
        }
        return intRtn;
    }

    /**
     以主鍵讀取匯出通匯序號檔

     @return

     */
    private int getRMOUTSNO(String receiverBank, String senderBank, RefBase<Rmoutsno> defRMOUTSNO) {
        int intRtn = 0;
        defRMOUTSNO.get().setRmoutsnoReceiverBank(receiverBank);
        defRMOUTSNO.get().setRmoutsnoSenderBank(senderBank);
        Rmoutsno  rmoutsno = rmService.getRMOUTSNObyPK(defRMOUTSNO.get());
        if (rmoutsno == null) {
            _rtnCode = IOReturnCode.RMOUTSNONotFound;
            _rtnMessage = QueryFail;
        } else {
            defRMOUTSNO.set(rmoutsno);
            intRtn = 1;
        }
        return intRtn;
    }

    /**
     檢核通匯序號檔是否有此筆資料

     @return

     */
    private int isRMINSNOExist(String receiverBank, String senderBank) {
        int intRtn = 0;
        Rminsno defRMINSNO = new Rminsno();
        defRMINSNO.setRminsnoReceiverBank(receiverBank);
        defRMINSNO.setRminsnoSenderBank(senderBank);
        intRtn = rmService.getRMINSNOByPK(defRMINSNO);
        if (intRtn <= 0) {
            _rtnCode = IOReturnCode.RMINSNONotFound;
            _rtnMessage = QueryFail;
        }
        return intRtn;
    }

    /**
     以主鍵儲存匯入通匯序號檔

     @return

     */
    private FEPReturnCode updateRMINSNO(String receiverBank, String senderBank) {
        Rminsno defRMINSNO = new Rminsno();
        int intRtn = 0;
        defRMINSNO.setRminsnoChgk("0");
        defRMINSNO.setRminsnoCdkeyFlag("0");
        defRMINSNO.setRminsnoReceiverBank(receiverBank);
        defRMINSNO.setRminsnoSenderBank(senderBank);
        intRtn = rmService.updateRMINSNOByPK(defRMINSNO);
        if (intRtn < 0) {
            _rtnCode = IOReturnCode.RMINSNOUpdateError;
            _rtnMessage = UpdateFail;
        }
        return _rtnCode;
    }

    /**
     以主鍵儲存匯出通匯序號檔

     @return

     */
    private int updateRMOUTSNO(String receiverBank, String senderBank, String changeFlag) throws Exception {
        Rmoutsno defRMOUTSNO = new Rmoutsno();
        int intRtn = 0;
        defRMOUTSNO.setRmoutsnoChgk(changeFlag);
        defRMOUTSNO.setRmoutsnoSenderBank(senderBank);
        defRMOUTSNO.setRmoutsnoReceiverBank(receiverBank); //BKNOtxt.Text
        intRtn = rmService.updateRMOUTSNOByPK(defRMOUTSNO);
        if (intRtn < 0) {
            _rtnCode = IOReturnCode.RMOUTSNOUPDATEOERROR;
            _rtnMessage = UpdateFail;
        }
        return intRtn;
    }

    /**
     以匯出行取得所有匯入通匯序號檔資料

     @return

     */
    private List<Rmoutsno> getRMOUTSNOBySenderBank() {
        Rmoutsno defRMOUTSNO = new Rmoutsno();
        List<Rmoutsno> dtRMOUTSNO = new ArrayList<>();
        int intRtn = 0;
        //modified by maxine on 2011/06/24 for SYSSTAT自行查
        defRMOUTSNO.setRmoutsnoSenderBank(_SYSSTAT_HBKNO);
        //defRMOUTSNO.RMOUTSNO_SENDER_BANK = SysStatus.PropertyValue.SYSSTAT_HBKNO
        defRMOUTSNO.setRmoutsnoChgk("0");
        dtRMOUTSNO = rmService.getRMOUTSNOByDef(defRMOUTSNO);
        dtRMOUTSNO = dtRMOUTSNO.stream().sorted(Comparator.comparing(Rmoutsno::getRmoutsnoReceiverBank)).collect(Collectors.toList());
        intRtn = dtRMOUTSNO.size();
        if (intRtn < 1) {
            _rtnMessage = QueryFail;
            _rtnCode = IOReturnCode.RMOUTSNONotFound;
        }
        return dtRMOUTSNO;

    }

}
