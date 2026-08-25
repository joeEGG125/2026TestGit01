package com.syscom.fep.vo.text.rm.request;

import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.TextBase;
import com.syscom.fep.vo.text.rm.RMGeneral;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class SyncFEDIRequest extends TextBase<RMGeneral> {

    @XStreamAlias("RsHeader")
    private FEPRqHeader rqHeader;
    @XStreamAlias("SvcRq")
    private SyncFEDISvcRq svcRq;

    public FEPRqHeader getRqHeader() {
        return rqHeader;
    }

    public void setRqHeader(FEPRqHeader rqHeader) {
        this.rqHeader = rqHeader;
    }

    public SyncFEDISvcRq getSvcRq() {
        return svcRq;
    }

    public void setSvcRq(SyncFEDISvcRq svcRq) {
        this.svcRq = svcRq;
    }

    @Override
    public int getTotalLength() {
        return 0;
    }

    @Override
	public String makeMessageFromGeneral(RMGeneral general) throws Exception {
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
		// 輸入行
		svcRq.getRq().setKINBR(general.getRequest().getKINBR());
		// 櫃台機號
		svcRq.getRq().setTRMSEQ(general.getRequest().getTRMSEQ());
		// 分行登錄序號
		svcRq.getRq().setBRSNO(general.getRequest().getBRSNO());
		// 登錄櫃員
		svcRq.getRq().setENTTLRNO(general.getRequest().getENTTLRNO());
		// 主管代號1
		svcRq.getRq().setSUPNO1(general.getRequest().getSUPNO1());
		// 主管代號2
		svcRq.getRq().setSUPNO2(general.getRequest().getSUPNO2());
		// 登錄日期
		svcRq.getRq().setTBSDY(general.getRequest().getTBSDY());
		// 登錄時間
		svcRq.getRq().setTIME(general.getRequest().getTIME());
		// FEP登錄序號
		svcRq.getRq().setFEPNO(general.getRequest().getFEPNO());
		// 原外圍EJNO
		svcRq.getRq().setORGCHLEJNO(general.getRequest().getORGCHLEJNO());
		// 匯款日期
		svcRq.getRq().setREMDATE(general.getRequest().getREMDATE());
		// 財金回應訊息代號
		svcRq.getRq().setFISCRC(general.getRequest().getFISCRC());
		// 回覆外圍系統回應代碼
		svcRq.getRq().setCHLRC(general.getRequest().getCHLRC());
		// 回覆外圍系統回應訊息
		svcRq.getRq().setCHLMSG(general.getRequest().getCHLMSG());
		// 狀態
		svcRq.getRq().setSTATUS(general.getRequest().getSTATUS());
		// 來源別
		svcRq.getRq().setORIGINAL(general.getRequest().getORIGINAL());
		return serializeToXml(this);
	}

    @Override
    public void toGeneral(RMGeneral general) throws Exception {

    }

    @XStreamAlias("SvcRq")
    public static class SyncFEDISvcRq{
        @XStreamAlias("Rq")
        private SyncFEDIRequest.SyncFEDIRq rq;
        public SyncFEDIRequest.SyncFEDIRq getRq() {
            return this.rq;
        }
        public void setRq(SyncFEDIRequest.SyncFEDIRq value) {
            this.rq = value;
        }

    }

    @XStreamAlias("Rq")
    public static class SyncFEDIRq{
        //輸入行
        private String KINBR = "";
        //櫃台機號
        private String TRMSEQ = "";
        //分行登錄序號
        private String BRSNO = "";
        //登錄櫃員
        private String ENTTLRNO = "";
        //主管代號1
        private String SUPNO1 = "";
        //主管代號2
        private String SUPNO2 = "";
        //登錄日期
        private String TBSDY = "";
        //登錄時間
        private String TIME = "";
        //FEP登錄序號
        private String FEPNO = "";
        //原外圍EJNO
        private String ORGCHLEJNO = "";
        //匯款日期
        private String REMDATE = "";
        //財金回應訊息代號
        private String FISCRC = "";
        //回覆外圍系統回應代碼
        private String CHLRC = "";
        //回覆外圍系統回應訊息
        private String CHLMSG = "";
        //狀態
        private String STATUS = "";
        //來源別
        private String ORIGINAL = "";

        /**
         輸入行

         <remark>行員資訊-分行</remark>
         */
        public String getKINBR() {
            return KINBR;
        }
        public void setKINBR(String KINBR) {
            this.KINBR = KINBR;
        }

        /**
         櫃台機號

         <remark>行員資訊-機台號</remark>
         */
        public String getTRMSEQ() {
            return TRMSEQ;
        }
        public void setTRMSEQ(String TRMSEQ) {
            this.TRMSEQ = TRMSEQ;
        }

        /**
         分行登錄序號

         <remark>分行系統自編</remark>
         */
        public String getBRSNO() {
            return BRSNO;
        }
        public void setBRSNO(String BRSNO) {
            this.BRSNO = BRSNO;
        }

        /**
         登錄櫃員

         <remark></remark>
         */
        public String getENTTLRNO() {
            return ENTTLRNO;
        }
        public void setENTTLRNO(String ENTTLRNO) {
            this.ENTTLRNO = ENTTLRNO;
        }

        /**
         主管代號1

         <remark>視交易才需要放值	RT1010(緊急匯款登錄):	匯款金額超過50萬需主管A刷卡覆核	RT1101(更正)/RT1300(匯出確認)/RT1600確認取消:	不論金額皆需主管A刷卡覆核</remark>
         */
        public String getSUPNO1() {
            return SUPNO1;
        }
        public void setSUPNO1(String SUPNO1) {
            this.SUPNO1 = SUPNO1;
        }

        /**
         主管代號2

         <remark>視交易才需要放值	臨櫃放行RT1300(匯出確認)/ /FEDI轉通匯RT1301:	匯款金額超過1000萬需二位主管A,B刷卡覆核(主管代號1,2不能相同)	大批匯款放行需第二位主管覆核規定如下:	 126分行不論金額	 作業中心整批總金額超過1000萬	 一般分行單筆金額超過1000萬</remark>
         */
        public String getSUPNO2() {
            return SUPNO2;
        }
        public void setSUPNO2(String SUPNO2) {
            this.SUPNO2 = SUPNO2;
        }

        /**
         登錄日期

         <remark></remark>
         */
        public String getTBSDY() {
            return TBSDY;
        }
        public void setTBSDY(String TBSDY) {
            this.TBSDY = TBSDY;
        }

        /**
         登錄時間

         <remark></remark>
         */
        public String getTIME() {
            return TIME;
        }
        public void setTIME(String TIME) {
            this.TIME = TIME;
        }

        /**
         FEP登錄序號

         <remark></remark>
         */
        public String getFEPNO() {
            return FEPNO;
        }
        public void setFEPNO(String FEPNO) {
            this.FEPNO = FEPNO;
        }

        /**
         原外圍EJNO

         <remark></remark>
         */
        public String getORGCHLEJNO() {
            return ORGCHLEJNO;
        }
        public void setORGCHLEJNO(String ORGCHLEJNO) {
            this.ORGCHLEJNO = ORGCHLEJNO;
        }

        /**
         匯款日期

         <remark></remark>
         */
        public String getREMDATE() {
            return REMDATE;
        }
        public void setREMDATE(String REMDATE) {
            this.REMDATE = REMDATE;
        }

        /**
         財金回應訊息代號

         <remark></remark>
         */
        public String getFISCRC() {
            return FISCRC;
        }
        public void setFISCRC(String FISCRC) {
            this.FISCRC = FISCRC;
        }

        /**
         回覆外圍系統回應代碼

         <remark></remark>
         */
        public String getCHLRC() {
            return CHLRC;
        }
        public void setCHLRC(String CHLRC) {
            this.CHLRC = CHLRC;
        }

        /**
         回覆外圍系統回應訊息

         <remark></remark>
         */
        public String getCHLMSG() {
            return CHLMSG;
        }
        public void setCHLMSG(String CHLMSG) {
            this.CHLMSG = CHLMSG;
        }

        /**
         狀態

         <remark>04-放行	06-已解款	09-被退匯</remark>
         */
        public String getSTATUS() {
            return STATUS;
        }
        public void setSTATUS(String STATUS) {
            this.STATUS = STATUS;
        }

        /**
         來源別

         <remark>2：FEDI	4：MMAB2B</remark>
         */
        public String getORIGINAL() {
            return ORIGINAL;
        }
        public void setORIGINAL(String ORIGINAL) {
            this.ORIGINAL = ORIGINAL;
        }
    }
}
