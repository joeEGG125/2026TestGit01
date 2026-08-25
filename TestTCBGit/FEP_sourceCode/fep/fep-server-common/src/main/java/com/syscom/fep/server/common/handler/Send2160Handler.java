package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.INBKData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.server.common.TxHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class Send2160Handler extends HandlerBase {


    private String pCode;
    private String txDate;

    private int ejfNo;

    private String msgid = "ASK2160";

    public String getpCode() {
        return pCode;
    }

    public void setpCode(String pCode) {
        this.pCode = pCode;
    }

    public String getTxDate() {
        return txDate;
    }

    public void setTxDate(String txDate) {
        this.txDate = txDate;
    }

    public Integer getejfNo() {
        return ejfNo;
    }

    public void setEjfNo(Integer ejfNO) {
        this.ejfNo = ejfNO;
    }

    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        return null;
    }

    /**
     * SPEC:ATM暨FEP建置案系統
     * 處理流程
     * 1. 傳入值    Channel  varchar(10)  通道別
     * 2. 取得FEP電子日誌序號
     * 3. 取得ATM MSGID
     * 4. CALL AA- AskFromBANK2160
     * 5. 將AA組好Response Retrun 回MQ
     */


    @Override
    public String dispatch(FEPChannel channel, String data) throws Throwable {
        INBKData inbkData = new INBKData();
        String atmRes = StringUtils.EMPTY;

        //2.取得FEP電子日誌序號
        if (this.getEj() == 0) {
            this.setEj(TxHelper.generateEj());
        }
        //3.取得MSGID
        inbkData.setMessageID(msgid);
        Msgctl msgctl = FEPCache.getMsgctrl(inbkData.getMessageID());
        inbkData.setMsgCtl(msgctl);
        this.setChannel(inbkData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
        inbkData.setAaName(msgctl.getMsgctlAaName());
        inbkData.setTxStatus(inbkData.getMsgCtl().getMsgctlStatus() == 1);
        inbkData.setTxRequestMessage(data);
        inbkData.setTxSubSystem(SubSystem.INBK);
        inbkData.setTxChannel(FEPChannel.FEP);
        inbkData.setEj(this.getEj());
        //4. CALL AA
        if (msgctl != null) {
            atmRes = this.runAA(inbkData);
        }
        return atmRes;
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return false;
    }


    private String runAA(INBKData inbkData) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processInbkRequestData2160", INBKData.class);
            String atmRes = (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, inbkData);
            return atmRes;
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

}






