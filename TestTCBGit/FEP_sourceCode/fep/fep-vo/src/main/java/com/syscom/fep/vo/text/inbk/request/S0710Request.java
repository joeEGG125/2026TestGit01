package com.syscom.fep.vo.text.inbk.request;

import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.TextBase;
import com.syscom.fep.vo.text.inbk.INBKGeneral;
import com.syscom.fep.vo.text.inbk.INBKGeneralRequest;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class S0710Request extends TextBase<INBKGeneral> {
    @XStreamAlias("RsHeader")
    private FEPRqHeader rqHeader;
    @XStreamAlias("SvcRq")
    private S0710SvcRq svcRq;


    public FEPRqHeader getRqHeader() {
        return rqHeader;
    }

    public void setRqHeader(FEPRqHeader rqHeader) {
        this.rqHeader = rqHeader;
    }

    public S0710SvcRq getSvcRq() {
        return svcRq;
    }

    public void setSvcRq(S0710SvcRq svcRqField) {
        this.svcRq = svcRqField;
    }

    @Override
    public int getTotalLength() {
        return 0;
    }

    @Override
    public String makeMessageFromGeneral(INBKGeneral inbkGeneral) throws Exception {
        INBKGeneralRequest tempVar = inbkGeneral.getRequest();
        getRqHeader().setChlName(tempVar.getChlName());
        getRqHeader().setChlEJNo(tempVar.getChlEJNo());
        getRqHeader().setBranchID(tempVar.getBranchID());
        getRqHeader().setChlSendTime(tempVar.getChlSendTime());
        getRqHeader().setMsgID(tempVar.getMsgID());
        getRqHeader().setMsgType(tempVar.getMsgType());
        getRqHeader().setTermID(tempVar.getTermID());
        getRqHeader().setTxnID(tempVar.getTxnID());
        getRqHeader().setUserID(tempVar.getUserID());
        getSvcRq().getRq().setUNITNO(tempVar.getUNITNO()); //委託單位代號

        return serializeToXml(this);

    }

    @Override
    public void toGeneral(INBKGeneral inbkGeneral) throws Exception {
        inbkGeneral.getRequest().setChlName(getRqHeader().getChlName());
        inbkGeneral.getRequest().setChlEJNo(getRqHeader().getChlEJNo());
        inbkGeneral.getRequest().setBranchID(getRqHeader().getBranchID());
        inbkGeneral.getRequest().setChlSendTime(getRqHeader().getChlSendTime());
        inbkGeneral.getRequest().setMsgID(getRqHeader().getMsgID());
        inbkGeneral.getRequest().setMsgType(getRqHeader().getMsgType());
        inbkGeneral.getRequest().setTermID(getRqHeader().getTermID());
        inbkGeneral.getRequest().setTxnID(getRqHeader().getTxnID());
        inbkGeneral.getRequest().setUserID(getRqHeader().getUserID());
        inbkGeneral.getRequest().setUNITNO(getSvcRq().getRq().getUNITNO()); //委託單位代號


    }

    @XStreamAlias("SvcRq")
    public static class S0710SvcRq{
        @XStreamAlias("Rq")
        private S0710Rq rq;
        public S0710Rq getRq() {
            return this.rq;
        }
        public void setRq(S0710Rq value) {
            this.rq = value;
        }

    }

    @XStreamAlias("Rq")
    public static class S0710Rq{
        //委託單位代號
        private String UNITNO = "";

        /**
         委託單位代號

         <remark></remark>
         */
        public String getUNITNO() {
            return UNITNO;
        }
        public void setUNITNO(String value) {
            UNITNO = value;
        }

    }
}
