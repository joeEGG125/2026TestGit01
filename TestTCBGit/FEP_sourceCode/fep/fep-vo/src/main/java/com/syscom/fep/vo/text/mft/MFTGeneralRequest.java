package com.syscom.fep.vo.text.mft;

import org.apache.commons.lang3.StringUtils;
import java.math.BigDecimal;
import com.syscom.fep.frmcommon.annotation.Field;

public class MFTGeneralRequest{
    @Field(encryption = "")
    private String LL = StringUtils.EMPTY;
    @Field(encryption = "")
    private String ZZ = StringUtils.EMPTY;
    @Field(encryption = "")
    private String STAN = StringUtils.EMPTY;
    @Field(encryption = "")
    private String MQ_MSG_ID = StringUtils.EMPTY;
    @Field(encryption = "")
    private String FILLER = StringUtils.EMPTY;
    @Field(encryption = "")
    private String BRNO = StringUtils.EMPTY;
    @Field(encryption = "")
    private String RESOURCE_CHANNEL = StringUtils.EMPTY;
    @Field(encryption = "")
    private String FIRSTDATE = StringUtils.EMPTY;
    @Field(encryption = "")
    private String TD_CODE = StringUtils.EMPTY;
    @Field(encryption = "")
    private String InDate = StringUtils.EMPTY;
    @Field(encryption = "")
    private String InTime = StringUtils.EMPTY;
    @Field(encryption = "")
    private String CNTRL = StringUtils.EMPTY;
    @Field(encryption = "")
    private String TOTALCNT = StringUtils.EMPTY;
    @Field(encryption = "")
    private String TOTALAMT = StringUtils.EMPTY;
    @Field(encryption = "")
    private String PCODE = StringUtils.EMPTY;
    @Field(encryption = "")
    private String RESULT = StringUtils.EMPTY;
    @Field(encryption = "")
    private String TYPE = StringUtils.EMPTY;
    @Field(encryption = "")
    private String FILENAME = StringUtils.EMPTY;
    @Field(encryption = "")
    private String SEQ = StringUtils.EMPTY;
    @Field(encryption = "")
    private String TRAN_CODE = StringUtils.EMPTY;
    public String getLL(){
        return LL;
    }
    public void setLL(String value){
        this.LL = value;
    }
    public String getZZ(){
        return ZZ;
    }
    public void setZZ(String value){
        this.ZZ = value;
    }
    public String getSTAN(){
        return STAN;
    }
    public void setSTAN(String value){
        this.STAN = value;
    }
    public String getMQ_MSG_ID(){
        return MQ_MSG_ID;
    }
    public void setMQ_MSG_ID(String value){
        this.MQ_MSG_ID = value;
    }
    public String getFILLER(){
        return FILLER;
    }
    public void setFILLER(String value){
        this.FILLER = value;
    }
    public String getBRNO(){
        return BRNO;
    }
    public void setBRNO(String value){
        this.BRNO = value;
    }
    public String getRESOURCE_CHANNEL(){
        return RESOURCE_CHANNEL;
    }
    public void setRESOURCE_CHANNEL(String value){
        this.RESOURCE_CHANNEL = value;
    }
    public String getFIRSTDATE(){
        return FIRSTDATE;
    }
    public void setFIRSTDATE(String value){
        this.FIRSTDATE = value;
    }
    public String getTD_CODE(){
        return TD_CODE;
    }
    public void setTD_CODE(String value){
        this.TD_CODE = value;
    }
    public String getInDate(){
        return InDate;
    }
    public void setInDate(String value){
        this.InDate = value;
    }
    public String getInTime(){
        return InTime;
    }
    public void setInTime(String value){
        this.InTime = value;
    }
    public String getCNTRL(){
        return CNTRL;
    }
    public void setCNTRL(String value){
        this.CNTRL = value;
    }
    public String getTOTALCNT(){
        return TOTALCNT;
    }
    public void setTOTALCNT(String value){
        this.TOTALCNT = value;
    }
    public String getTOTALAMT(){
        return TOTALAMT;
    }
    public void setTOTALAMT(String value){
        this.TOTALAMT = value;
    }
    public String getPCODE(){
        return PCODE;
    }
    public void setPCODE(String value){
        this.PCODE = value;
    }
    public String getRESULT(){
        return RESULT;
    }
    public void setRESULT(String value){
        this.RESULT = value;
    }
    public String getTYPE(){
        return TYPE;
    }
    public void setTYPE(String value){
        this.TYPE = value;
    }
    public String getFILENAME(){
        return FILENAME;
    }
    public void setFILENAME(String value){
        this.FILENAME = value;
    }
    public String getSEQ(){
        return SEQ;
    }
    public void setSEQ(String value){
        this.SEQ = value;
    }
    public String getTRAN_CODE(){
        return TRAN_CODE;
    }
    public void setTRAN_CODE(String value){
        this.TRAN_CODE = value;
    }
}
