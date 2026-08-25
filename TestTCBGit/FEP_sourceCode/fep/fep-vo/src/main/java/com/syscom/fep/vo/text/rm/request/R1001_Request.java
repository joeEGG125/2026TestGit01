package com.syscom.fep.vo.text.rm.request;

import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.TextBase;
import com.syscom.fep.vo.text.fcs.FCSGeneral;
import com.syscom.fep.vo.text.rm.RMGeneral;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class R1001_Request extends TextBase<FCSGeneral> {

    @XStreamAlias("RsHeader")
    private FEPRqHeader rqHeader;
    @XStreamAlias("R1001SvcRq")
    private R1001SvcRq r1001SvcRq;

    public FEPRqHeader getRqHeader() { return rqHeader; }

    public void setRqHeader(FEPRqHeader rqHeader) { this.rqHeader = rqHeader; }

    public R1001SvcRq getR1001SvcRq() { return r1001SvcRq; }

    public void setR1001SvcRq(R1001SvcRq r1001SvcRq) { this.r1001SvcRq = r1001SvcRq; }

    /**
     * 對應到General
     */
    @Override
    public void toGeneral(FCSGeneral general) throws Exception {
        general.getRequest().setChlName(rqHeader.getChlName());
        general.getRequest().setChlEJNo(rqHeader.getChlEJNo());
        general.getRequest().setBranchID(rqHeader.getBranchID());
        general.getRequest().setChlSendTime(rqHeader.getChlSendTime());
        general.getRequest().setMsgID(rqHeader.getMsgID());
        general.getRequest().setMsgType(rqHeader.getMsgType());
        general.getRequest().setTermID(rqHeader.getTermID());
        general.getRequest().setTxnID(rqHeader.getTxnID());
        general.getRequest().setUserID(rqHeader.getUserID());
        general.getRequest().setSignID(rqHeader.getSignID());
        //通知日期
        general.getRequest().setRefDate(getR1001SvcRq().getRq().get_REFDATE());
        //通知時間
        general.getRequest().setSendTime(getR1001SvcRq().getRq().get_SENDTIME());
        //批號
        general.getRequest().setBatchNo(getR1001SvcRq().getRq().get_BATCHNO());
    }

    /**
     * 從 General 對應回來
     */
    @Override
    public String makeMessageFromGeneral(FCSGeneral general) throws Exception {
        rqHeader.setChlName(general.getRequest().getChlName());
        rqHeader.setChlEJNo(general.getRequest().getChlEJNo());
        rqHeader.setBranchID(general.getRequest().getBranchID());
        rqHeader.setChlSendTime(general.getRequest().getChlSendTime());
        rqHeader.setMsgID(general.getRequest().getMsgID());
        rqHeader.setMsgType(general.getRequest().getMsgType());
        rqHeader.setTermID(general.getRequest().getTermID());
        rqHeader.setTxnID(general.getRequest().getTxnID());
        rqHeader.setUserID(general.getRequest().getUserID());
        rqHeader.setSignID(general.getRequest().getSignID());
        //通知日期
        r1001SvcRq.getRq().set_REFDATE(general.getRequest().getRefDate());
        //通知時間
        r1001SvcRq.getRq().set_SENDTIME(general.getRequest().getSendTime());
        //批號
        r1001SvcRq.getRq().set_BATCHNO(general.getRequest().getBatchNo());
        return serializeToXml(this);
    }

    @Override
    public int getTotalLength() {
        return 0;
    }

    @XStreamAlias("R1001SvcRq")
    public static class R1001SvcRq {
        @XStreamAlias("Rq")
        private R1001Rq rq;

        public R1001Rq getRq() {
            return rq;
        }

        public void setRq(R1001Rq rq) {
            this.rq = rq;
        }
    }

    @XStreamAlias("R1001Rq")
    public static class R1001Rq {
        //通知日期
        private String _REFDATE = "";

        //通知時間
        private String _SENDTIME = "";

        //批號
        private String _BATCHNO = "";

        public String get_REFDATE() {
            return _REFDATE;
        }

        public void set_REFDATE(String _REFDATE) {
            this._REFDATE = _REFDATE;
        }

        public String get_SENDTIME() {
            return _SENDTIME;
        }

        public void set_SENDTIME(String _SENDTIME) {
            this._SENDTIME = _SENDTIME;
        }

        public String get_BATCHNO() {
            return _BATCHNO;
        }

        public void set_BATCHNO(String _BATCHNO) {
            this._BATCHNO = _BATCHNO;
        }
    }
}
