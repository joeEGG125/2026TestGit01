package com.syscom.fep.vo.communication;

public enum ToCBSChangeLineStatusAction {
    Enable("啟用"),
    Disable("停用"),
    Pause("暫停");

    private final String description;

    ToCBSChangeLineStatusAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ToCBSChangeLineStatusAction from(String name) {
        try {
            return ToCBSChangeLineStatusAction.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}