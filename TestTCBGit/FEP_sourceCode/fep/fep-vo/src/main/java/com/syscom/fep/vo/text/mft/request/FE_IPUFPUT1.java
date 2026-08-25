package com.syscom.fep.vo.text.mft.request;

import com.syscom.fep.vo.text.mft.MFTGeneral;
import com.syscom.fep.vo.text.mft.MFTGeneralRequest;
import com.syscom.fep.vo.text.mft.MFTTextBase;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.frmcommon.annotation.Field;

public class FE_IPUFPUT1 extends MFTTextBase {
    @Field(length =2)
    private String LL = StringUtils.EMPTY;

    @Field(length =2)
    private String ZZ = StringUtils.EMPTY;

    @Field(length =4)
    private String TRAN_CODE = StringUtils.EMPTY;

    @Field(length =4)
    private String TD_CODE = StringUtils.EMPTY;

    @Field(length =4)
    private String BRNO = StringUtils.EMPTY;

    @Field(length =6)
    private String SEQ = StringUtils.EMPTY;

    @Field(length =10)
    private String STAN = StringUtils.EMPTY;

    @Field(length =4)
    private String RESULT = StringUtils.EMPTY;

    @Field(length =1)
    private String CNTRL = StringUtils.EMPTY;

    @Field(length =3)
    private String RESOURCE_CHANNEL = StringUtils.EMPTY;

    @Field(length =7)
    private String InDate = StringUtils.EMPTY;

    @Field(length =6)
    private String InTime = StringUtils.EMPTY;

    @Field(length =24)
    private String MQ_MSG_ID = StringUtils.EMPTY;

    @Field(length =1)
    private String TYPE = StringUtils.EMPTY;

    @Field(length =20)
    private String FILENAME = StringUtils.EMPTY;

    @Field(length =10)
    private String TOTALCNT = StringUtils.EMPTY;

    @Field(length =16)
    private String TOTALAMT = StringUtils.EMPTY;

    @Field(length =53)
    private String FILLER = StringUtils.EMPTY;

    private static final int _TotalLength = 177;

    public String getLL(){
        return LL;
    }
    public void setLL(String value) {
        this.LL = value;
    }
    public String getZZ(){
        return ZZ;
    }
    public void setZZ(String value) {
        this.ZZ = value;
    }
    public String getTRAN_CODE(){
        return TRAN_CODE;
    }
    public void setTRAN_CODE(String value) {
        this.TRAN_CODE = value;
    }
    public String getTD_CODE(){
        return TD_CODE;
    }
    public void setTD_CODE(String value) {
        this.TD_CODE = value;
    }
    public String getBRNO(){
        return BRNO;
    }
    public void setBRNO(String value) {
        this.BRNO = value;
    }
    public String getSEQ(){
        return SEQ;
    }
    public void setSEQ(String value) {
        this.SEQ = value;
    }
    public String getSTAN(){
        return STAN;
    }
    public void setSTAN(String value) {
        this.STAN = value;
    }
    public String getRESULT(){
        return RESULT;
    }
    public void setRESULT(String value) {
        this.RESULT = value;
    }
    public String getCNTRL(){
        return CNTRL;
    }
    public void setCNTRL(String value) {
        this.CNTRL = value;
    }
    public String getRESOURCE_CHANNEL(){
        return RESOURCE_CHANNEL;
    }
    public void setRESOURCE_CHANNEL(String value) {
        this.RESOURCE_CHANNEL = value;
    }
    public String getInDate(){
        return InDate;
    }
    public void setInDate(String value) {
        this.InDate = value;
    }
    public String getInTime(){
        return InTime;
    }
    public void setInTime(String value) {
        this.InTime = value;
    }
    public String getMQ_MSG_ID(){
        return MQ_MSG_ID;
    }
    public void setMQ_MSG_ID(String value) {
        this.MQ_MSG_ID = value;
    }
    public String getTYPE(){
        return TYPE;
    }
    public void setTYPE(String value) {
        this.TYPE = value;
    }
    public String getFILENAME(){
        return FILENAME;
    }
    public void setFILENAME(String value) {
        this.FILENAME = value;
    }
    public String getTOTALCNT(){
        return TOTALCNT;
    }
    public void setTOTALCNT(String value) {
        this.TOTALCNT = value;
    }
    public String getTOTALAMT(){
        return TOTALAMT;
    }
    public void setTOTALAMT(String value) {
        this.TOTALAMT = value;
    }
    public String getFILLER(){
        return FILLER;
    }
    public void setFILLER(String value) {
        this.FILLER = value;
    }

    @Override
    public int getTotalLength() {
       return _TotalLength;
    }

    @Override
    public MFTGeneral parseFlatfile(String flatfile) throws Exception {
       return this.parseFlatfile(this.getClass(), flatfile);
    }

    @Override
    public String makeMessageFromGeneral(MFTGeneral general) {
       return null;
    }

    public String makeMessage() {
       return null;
    }

    @Override
    public void toGeneral(MFTGeneral general) throws Exception {
        MFTGeneralRequest request = general.getRequest();
        request.setLL(this.getLL());
        request.setZZ(this.getZZ());
        request.setTRAN_CODE(this.getTRAN_CODE());
        request.setTD_CODE(this.getTD_CODE());
        request.setBRNO(this.getBRNO());
        request.setSEQ(this.getSEQ());
        request.setSTAN(this.getSTAN());
        request.setRESULT(this.getRESULT());
        request.setCNTRL(this.getCNTRL());
        request.setRESOURCE_CHANNEL(this.getRESOURCE_CHANNEL());
        request.setInDate(this.getInDate());
        request.setInTime(this.getInTime());
        request.setMQ_MSG_ID(this.getMQ_MSG_ID());
        request.setTYPE(this.getTYPE());
        request.setFILENAME(this.getFILENAME());
        request.setTOTALCNT(this.getTOTALCNT());
        request.setTOTALAMT(this.getTOTALAMT());
        request.setFILLER(this.getFILLER());
    }
}
