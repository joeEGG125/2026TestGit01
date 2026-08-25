package com.syscom.fep.web.audit;

import org.apache.commons.lang.StringUtils;

import java.util.*;

public class AuditLog {
    /**
     * 執行動作的名稱, 一般指按下的button名稱
     * ex: 查詢, 新增, 修改, 刪除
     */
    private String action;
    /**
     * 執行動作的說明
     */
    private final List<String> actionDescriptions = new ArrayList<>();
    /**
     * 執行動作的參數
     * ex: 查詢條件, 新增資料, 修改資料, 刪除資料
     */
    private final Map<String, Object> param = new HashMap<>();
    /**
     * 執行動作的參數, 多組
     * ex: 查詢條件, 新增資料, 修改資料, 刪除資料
     */
    private final List<Map<String, Object>> params = new ArrayList<>();

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void addActionDescription(String description) {
        actionDescriptions.add(Objects.requireNonNull(description));
    }

    public List<String> getActionDescriptions() {
        return actionDescriptions;
    }

    public void addParam(String key, Object value) {
        param.put(Objects.requireNonNull(key), value == null ? "[null]" : value);
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public void addParams(Map<String, Object> param) {
        params.add(Objects.requireNonNull(param));
    }

    public List<Map<String, Object>> getParams() {
        return params;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("「").append(action).append("」");
        sb.append(StringUtils.join(actionDescriptions, "|"));
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }
        if (!param.isEmpty())
            sb.deleteCharAt(sb.length() - 1);
        if (!params.isEmpty()) {
            sb.append("[");
            for (Map<String, Object> param : params) {
                for (Map.Entry<String, Object> entry : param.entrySet()) {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
                }
                if (!param.isEmpty()) {
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append("|");
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }
}