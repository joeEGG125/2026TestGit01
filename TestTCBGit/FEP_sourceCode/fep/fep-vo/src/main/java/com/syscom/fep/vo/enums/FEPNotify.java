package com.syscom.fep.vo.enums;

public enum FEPNotify {
    FEPNotifyMail_APD("設計一科"),
    FEPNotifyMail_SYS("系統科"),
    FEPNotifyMail_Customize("自定"),
    FEPNotifyPhone_APD("設計一科"),
    FEPNotifyPhone_SYS("系統科"),
    FEPNotifyPhone_Customize("自定");

    private final String description;

    FEPNotify(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static FEPNotify valueOfName(String name) {
        for (FEPNotify e : FEPNotify.values()) {
            if (e.name().equals(name)) {
                return e;
            }
        }
        return null;
    }
}
