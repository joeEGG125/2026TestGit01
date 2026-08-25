package com.syscom.fep.gateway.ims.cbs;

import org.springframework.beans.BeanUtils;

/**
 * CBSGateway相關數據統計
 *
 * @author Richard
 */
public class CBSGatewayStatistic {
    private long transactions; // 正在交易的筆數
    private long totalTransactions; // 總共交易的筆數

    public long getTransactions() {
        return transactions;
    }

    public void setTransactions(long transactions) {
        this.transactions = transactions;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public static CBSGatewayStatistic clone(CBSGatewayStatistic source, boolean reset) {
        CBSGatewayStatistic target = clone(source);
        if (reset) {
            target.setTransactions(0);
        }
        return target;
    }

    public static CBSGatewayStatistic accumulateTransactions(CBSGatewayStatistic source, long accumulate) {
        CBSGatewayStatistic target = clone(source);
        target.setTransactions(target.getTransactions() + accumulate);
        if (accumulate > 0)
            target.setTotalTransactions(target.getTotalTransactions() + accumulate);
        return target;
    }

    public static CBSGatewayStatistic clone(CBSGatewayStatistic source) {
        CBSGatewayStatistic target = new CBSGatewayStatistic();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
