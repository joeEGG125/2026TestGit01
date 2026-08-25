package com.syscom.fep.frmcommon.restful.processcommand;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProcessCommandArgsExtractor implements Serializable {
    @JsonProperty("argsKey")
    private String argsKey;
    @JsonProperty("newArgsKey")
    private String newArgsKey;

    /**
     * 這個構建函式不可以刪除
     */
    public ProcessCommandArgsExtractor() {
        this(null, null);
    }

    public ProcessCommandArgsExtractor(String argsKey) {
        this(argsKey, null);
    }

    public ProcessCommandArgsExtractor(String argsKey, String newArgsKey) {
        this.argsKey = argsKey;
        this.newArgsKey = newArgsKey;
    }

    public List<String> extract(ProcessCommandArgsFormat argsFormat, Map<String, String> args) {
        List<String> argsList = new ArrayList<>();
        if (StringUtils.isNotBlank(newArgsKey)) {
            argsList.add(newArgsKey);
        }
        if (StringUtils.isBlank(argsKey)) {
            StringBuilder sb = new StringBuilder();
            sb.append("\"");
            for (Map.Entry<String, String> entry : args.entrySet()) {
                sb.append(argsFormat != null ? argsFormat.format(entry.getKey(), entry.getValue()) : ProcessCommandArgsFormat.VALUE.format(entry.getValue()));
            }
            sb.append("\"");
            argsList.add(sb.toString());
            args.clear();
        } else {
            String argsValue = args.remove(argsKey);
            if (argsValue == null) {
                return Collections.emptyList();
            }
            argsList.add(argsValue);
        }
        return argsList;
    }

    public String getArgsKey() {
        return argsKey;
    }

    @JsonProperty("argsKey")
    public void setArgsKey(String argsKey) {
        this.argsKey = argsKey;
    }

    public String getNewArgsKey() {
        return newArgsKey;
    }

    @JsonProperty("newArgsKey")
    public void setNewArgsKey(String newArgsKey) {
        this.newArgsKey = newArgsKey;
    }
}
