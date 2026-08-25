package com.syscom.fep.mybatis.his.ext.model;

import com.syscom.fep.mybatis.his.model.Feptxn;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.Date;

public class FeptxnExt extends Feptxn {
    private static final long serialVersionUID = 1L;
    /**
     * 用於標識操作的表名
     * 合庫合併了FEPTXN檔, 所以這裡寫死為空字串
     */
    private final String tableNameSuffix = StringUtils.EMPTY;
    /**
     * 手機門號轉帳記號
     * Y:手機門號轉帳
     */
    private String feptxnMtp;
    /**
     * 多幣Debit卡跨國提款註記
     * Y:多幣Debit卡跨國
     */
    private String feptxnMulticur = StringUtils.SPACE;
    /**
     * 疫情減免手續費
     */
    private String feptxnCovid;

    private String feptxnTxAmtStr;

    public FeptxnExt() {
        super();
        this.setInitializationValue();
    }

    public FeptxnExt(Feptxn feptxn) {
        if (feptxn == null)
            return;
        this.setInitializationValue();
        BeanUtils.copyProperties(feptxn, this);
    }

    private void setInitializationValue() {
        this.setFeptxnIcmark(StringUtils.SPACE);
        this.setFeptxnNpsClr((short) 0);
        this.setFeptxnNpsAgbFee(new BigDecimal(0));
        this.setFeptxnTroutKind(StringUtils.SPACE);
    }

    public String getFeptxnMtp() {
        return feptxnMtp;
    }

    public void setFeptxnMtp(String feptxnMtp) {
        this.feptxnMtp = feptxnMtp;
    }

    public String getTableNameSuffix() {
        return tableNameSuffix;
    }

    public void setTableNameSuffix(String tableNameSuffix) {
        // 合庫合併了FEPTXN檔, 所以這裡不需要再塞入
        // this.tableNameSuffix = tableNameSuffix;
    }

    public String getFeptxnMulticur() {
        return feptxnMulticur;
    }

    public void setFeptxnMulticur(String feptxnMulticur) {
        this.feptxnMulticur = feptxnMulticur;
    }

    public String getFeptxnCovid() {
        return feptxnCovid;
    }

    public void setFeptxnCovid(String feptxnCovid) {
        this.feptxnCovid = feptxnCovid;
    }

    public String getFeptxnTxAmtStr() {
        return feptxnTxAmtStr;
    }

    public void setFeptxnTxAmtStr(String feptxnTxAmtStr) {
        this.feptxnTxAmtStr = feptxnTxAmtStr;
    }

    @Override
    public boolean equals(Object that) {
        if (super.equals(that)) {
            if (getClass() != that.getClass()) {
                return false;
            }
            FeptxnExt other = (FeptxnExt) that;
            return ((this.getTableNameSuffix() == null ? other.getTableNameSuffix() == null : this.getTableNameSuffix().equals(other.getTableNameSuffix()))
                    && this.getFeptxnMtp() == null ? other.getFeptxnMtp() == null : this.getFeptxnMtp().equals(other.getFeptxnMtp()))
                    && (this.getFeptxnMulticur() == null ? other.getFeptxnMulticur() == null : this.getFeptxnMulticur().equals(other.getFeptxnMulticur()))
                    && (this.getFeptxnCovid() == null ? other.getFeptxnCovid() == null : this.getFeptxnCovid().equals(other.getFeptxnCovid()))
                    && (this.getFeptxnTxAmtStr() == null ? other.getFeptxnTxAmtStr() == null : this.getFeptxnTxAmtStr().equals(other.getFeptxnTxAmtStr()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getTableNameSuffix() == null) ? 0 : getTableNameSuffix().hashCode());
        result = prime * result + ((getFeptxnMtp() == null) ? 0 : getFeptxnMtp().hashCode());
        result = prime * result + ((getFeptxnMulticur() == null) ? 0 : getFeptxnMulticur().hashCode());
        result = prime * result + ((getFeptxnCovid() == null) ? 0 : getFeptxnCovid().hashCode());
        result = prime * result + ((getFeptxnTxAmtStr() == null) ? 0 : getFeptxnTxAmtStr().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}