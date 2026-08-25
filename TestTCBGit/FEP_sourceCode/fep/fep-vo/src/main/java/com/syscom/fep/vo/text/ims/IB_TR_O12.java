package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import com.syscom.fep.frmcommon.annotation.Field;
public class IB_TR_O12 extends IMSTextBase {
    @Field(length =8)
    private String IMS_TRANS;

    @Field(length =4)
    private String SYSCODE;

    @Field(length =14)
    private String SYS_DATETIME;

    @Field(length =8)
    private String FEP_EJNO;

    @Field(length =1)
    private String TXN_FLOW;

    @Field(length =2)
    private String FSCODE;

    @Field(length =4)
    private String IMSRC4_FISC;

    @Field(length =3)
    private String IMSRC_TCB;

    @Field(length =7)
    private String IMSBUSINESS_DATE;

    @Field(length =1)
    private String IMSACCT_FLAG;

    @Field(length =1)
    private String IMSRVS_FLAG;

    @Field(length =6)
    private String IMS_TXN_TIME;

    @Field(length =42)
    private String HRVS;

    @Field(length =500)
    private String DRVS;

    public String getIMS_TRANS(){
        return IMS_TRANS;
    }
    public void setIMS_TRANS(String IMS_TRANS){
        this.IMS_TRANS=IMS_TRANS;
    }
    public String getSYSCODE(){
        return SYSCODE;
    }
    public void setSYSCODE(String SYSCODE){
        this.SYSCODE=SYSCODE;
    }
    public String getSYS_DATETIME(){
        return SYS_DATETIME;
    }
    public void setSYS_DATETIME(String SYS_DATETIME){
        this.SYS_DATETIME=SYS_DATETIME;
    }
    public String getFEP_EJNO(){
        return FEP_EJNO;
    }
    public void setFEP_EJNO(String FEP_EJNO){
        this.FEP_EJNO=FEP_EJNO;
    }
    public String getTXN_FLOW(){
        return TXN_FLOW;
    }
    public void setTXN_FLOW(String TXN_FLOW){
        this.TXN_FLOW=TXN_FLOW;
    }
    public String getFSCODE(){
        return FSCODE;
    }
    public void setFSCODE(String FSCODE){
        this.FSCODE=FSCODE;
    }
    public String getIMSRC4_FISC(){
        return IMSRC4_FISC;
    }
    public void setIMSRC4_FISC(String IMSRC4_FISC){
        this.IMSRC4_FISC=IMSRC4_FISC;
    }
    public String getIMSRC_TCB(){
        return IMSRC_TCB;
    }
    public void setIMSRC_TCB(String IMSRC_TCB){
        this.IMSRC_TCB=IMSRC_TCB;
    }
    public String getIMSBUSINESS_DATE(){
        return IMSBUSINESS_DATE;
    }
    public void setIMSBUSINESS_DATE(String IMSBUSINESS_DATE){
        this.IMSBUSINESS_DATE=IMSBUSINESS_DATE;
    }
    public String getIMSACCT_FLAG(){
        return IMSACCT_FLAG;
    }
    public void setIMSACCT_FLAG(String IMSACCT_FLAG){
        this.IMSACCT_FLAG=IMSACCT_FLAG;
    }
    public String getIMSRVS_FLAG(){
        return IMSRVS_FLAG;
    }
    public void setIMSRVS_FLAG(String IMSRVS_FLAG){
        this.IMSRVS_FLAG=IMSRVS_FLAG;
    }
    public String getIMS_TXN_TIME(){
        return IMS_TXN_TIME;
    }
    public void setIMS_TXN_TIME(String IMS_TXN_TIME){
        this.IMS_TXN_TIME=IMS_TXN_TIME;
    }
    public String getHRVS(){
        return HRVS;
    }
    public void setHRVS(String HRVS){
        this.HRVS=HRVS;
    }
    public String getDRVS(){
        return DRVS;
    }
    public void setDRVS(String DRVS){
        this.DRVS=DRVS;
    }
    public void parseCbsTele(String tota){
        BigDecimal bigdecimal;
        this.setIMS_TRANS(tota.substring(0,8));
        this.setSYSCODE(tota.substring(8,12));
        this.setSYS_DATETIME(tota.substring(12,26));
        this.setFEP_EJNO(tota.substring(26,34));
        this.setTXN_FLOW(tota.substring(34,35));
        this.setFSCODE(tota.substring(35,37));
        this.setIMSRC4_FISC(tota.substring(37,41));
        this.setIMSRC_TCB(tota.substring(41,44));
        this.setIMSBUSINESS_DATE(tota.substring(44,51));
        this.setIMSACCT_FLAG(tota.substring(51,52));
        this.setIMSRVS_FLAG(tota.substring(52,53));
        this.setIMS_TXN_TIME(tota.substring(53,59));
        this.setHRVS(tota.substring(59,101));
        this.setDRVS(tota.substring(101,601));
    }
}