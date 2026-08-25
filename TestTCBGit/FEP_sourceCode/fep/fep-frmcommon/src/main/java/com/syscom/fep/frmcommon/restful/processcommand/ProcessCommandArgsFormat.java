package com.syscom.fep.frmcommon.restful.processcommand;

public enum ProcessCommandArgsFormat {
    VALUE("{value}"),
    KEY_VALUE_COLON("/{key}:{value}"),
    KEY_VALUE_EQUALS("{key}={value}");

    private final String pattern;

    private ProcessCommandArgsFormat(String pattern) {
        this.pattern = pattern;
    }

    public String format(String value) {
        return pattern.replace("{value}", value);
    }

    public String format(String key, String value) {
        return pattern.replace("{key}", key).replace("{value}", value);
    }
}
