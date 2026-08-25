package com.syscom.fep.gateway.netty;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.roundrobin.RoundRobin;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyTransmissionSslHandler {
    private final LogHelper logger = LogHelperFactory.getTraceLogger();
    private final Map<String, RoundRobin<String>> clientIpToAliasMap = new ConcurrentHashMap<>(); // ATM IP為key, Alias為value
    private final Map<String, RoundRobin<Integer>> clientIpToCertIndexMap = new ConcurrentHashMap<>();  // ATM IP為key, 憑證檔序列為value
    private List<String> certAlias;
    private final List<Integer> certIndex = new ArrayList<>();

    public void setCertAlias(List<String> certAlias) {
        // 2026-03-18 Richard modified 這裡要判斷一下, 如果新設定的certAlias內容沒有變化, 則最後不需要call resetValue
        boolean resetValue = false;
        if (CollectionUtils.isEmpty(certAlias) && CollectionUtils.isNotEmpty(this.certAlias))
            resetValue = true;
        else if (CollectionUtils.isNotEmpty(certAlias) && CollectionUtils.isEmpty(this.certAlias))
            resetValue = true;
        else if (CollectionUtils.isNotEmpty(certAlias) && CollectionUtils.isNotEmpty(this.certAlias))
            resetValue = certAlias.size() != this.certAlias.size() || !CollectionUtils.containsAll(this.certAlias, certAlias);
        // 更新certAlias
        this.certAlias = certAlias;
        // 根據上面的判斷決定是否需要call resetValue
        if (resetValue)
            this.resetValue(this.clientIpToAliasMap, this.certAlias);
    }

    public void setCertCount(int certCount) {
        if (certCount == this.certIndex.size())
            return;
        for (int i = 0; i < certCount; i++) {
            this.certIndex.add(i);
        }
        this.resetValue(this.clientIpToCertIndexMap, this.certIndex);
    }

    public String getCertAlias(String clientIp) {
        return getCertValue(clientIpToAliasMap, certAlias, clientIp);
    }

    public Integer getCertIndex(String clientIp) {
        return getCertValue(clientIpToCertIndexMap, certIndex, clientIp);
    }

    /**
     * 是否有完整輪詢取一次
     *
     * @param clientIp
     * @return
     */
    public boolean isTryoutAllCert(String clientIp) {
        boolean isTryoutAllCert = true;
        RoundRobin<String> robinAlias = clientIpToAliasMap.get(clientIp);
        if (robinAlias == null) {
            RoundRobin<Integer> robinCertIndex = clientIpToCertIndexMap.get(clientIp);
            if (robinCertIndex != null) {
                isTryoutAllCert = robinCertIndex.isRoundRobinAll();
            }
        } else {
            isTryoutAllCert = robinAlias.isRoundRobinAll();
        }
        logger.debug("[NettyTransmissionSslHandler][isTryoutAllCert][", clientIp, "]isTryoutAllCert:", isTryoutAllCert);
        return isTryoutAllCert;
    }

    /**
     * 獲取當前的Alias並移除所有的cache
     *
     * @param clientIp
     * @return
     */
    public String getAliasAndRemoveCert(String clientIp) {
        String certAlias = null;
        try {
            RoundRobin<String> robinAlias = clientIpToAliasMap.remove(clientIp);
            certAlias = robinAlias == null ? null : robinAlias.get();
            return certAlias;
        } finally {
            clientIpToCertIndexMap.remove(clientIp);
            logger.debug("[NettyTransmissionSslHandler][getAliasAndRemoveCert][", clientIp, "]certAlias:", certAlias);
        }
    }

    private <T> T getCertValue(Map<String, RoundRobin<T>> map, List<T> list, String clientIp) {
        RoundRobin<T> robin = getValue(map, list, clientIp);
        T certValue = robin == null ? null : robin.select();
        logger.debug("[NettyTransmissionSslHandler][getCertValue][", clientIp, "]certValue:", certValue);
        return certValue;
    }

    private <T> RoundRobin<T> getValue(Map<String, RoundRobin<T>> map, List<T> list, String clientIp) {
        if (CollectionUtils.isEmpty(list))
            return null;
        return map.computeIfAbsent(clientIp, k -> new RoundRobin<>(list, true));
    }

    private <T> void resetValue(Map<String, RoundRobin<T>> map, List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            logger.debug("[NettyTransmissionSslHandler][resetValue]clear all, clientIp:", StringUtils.join(map.keySet(), ","));
            map.clear();
            return;
        }
        for (Map.Entry<String, RoundRobin<T>> entry : map.entrySet()) {
            logger.debug("[NettyTransmissionSslHandler][resetValue][", entry.getKey(), "]value:", StringUtils.join(list, ","));
            RoundRobin<T> robin = entry.getValue();
            robin.clear();
            robin.addAll(list);
        }
    }
}
