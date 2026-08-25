package com.syscom.fep.frmcommon.log.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

public class MdcMatcherFilter extends AbstractMatcherFilter<ILoggingEvent> {

	// private String key;
    // private String denyValue;
    private Map<String, Set<String>> denyRules = new HashMap<>();
    private String keyword; // 用來設定要過濾的關鍵字
    private Pattern pattern;           // 編譯後的 Pattern
    private boolean caseInsensitive = false;   // 是否忽略大小寫,預設為 false
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
        compilePattern();
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
        compilePattern();
    }

    private void compilePattern() {
        if (keyword == null || keyword.isEmpty()) {
            pattern = null;
            return;
        }
        try {
            int flags = caseInsensitive ? Pattern.CASE_INSENSITIVE : 0;
            pattern = Pattern.compile(keyword, flags);
        } catch (PatternSyntaxException e) {
            addError("Invalid regex pattern: " + keyword, e);
            pattern = null;
        }
    }

    // 用於Logback配置中添加多組Deny規則,同一個key可以有多個denyValue
    // ex: 
    //<denyRule>Channel=NB</denyRule>
    //<denyRule>Channel=ATMP</denyRule>
    //<denyRule>userId=admin</denyRule>
    //<denyRule>userId=test</denyRule>
    public void addDenyRule(String keyValuePair) {
        if (keyValuePair != null && keyValuePair.contains("=")) {
            String[] parts = keyValuePair.split("=", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();

                // 如果該 key 還不存在，創建一個新的 Set
                denyRules.computeIfAbsent(key, k -> new HashSet<>()).add(value);
            }
        }
    }

    // public void setKey(String key) {
    //     this.key = key;
    // }

    // public void setDenyValue(String denyValue) {
    //     this.denyValue = denyValue;
    // }    

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event == null || event.getMDCPropertyMap() == null) {
            return FilterReply.NEUTRAL;
        }

        // 如果有設定訊息包含指定關鍵字，則不輸出
        String msg = event.getFormattedMessage();
        if (keyword != null && 
            msg != null && 
            pattern.matcher(msg).find()) {
            return FilterReply.DENY;
        }

        Map<String, String> mdcMap = event.getMDCPropertyMap();
        // 檢查多組規則
        for (Map.Entry<String, Set<String>> rule : denyRules.entrySet()) {
            String mdcValue = mdcMap.get(rule.getKey());
            // 檢查該 key 的值是否在 deny 列表中
            if (mdcValue != null && rule.getValue().contains(mdcValue)) {
                return FilterReply.DENY;
            }
        }

        //String value = event.getMDCPropertyMap().get(key);
        
        // if (denyValue != null && denyValue.equals(value)) {
        //     return FilterReply.DENY;
        // }

        return FilterReply.NEUTRAL;
    }
}
