package com.syscom.fep.frmcommon.mail;

public enum MailPriority {
    // The email has high priority.
    High(1),
    // The email has normal priority.
    Normal(3),
    // The email has lowest priority.
    Lowest(5);

    private int value;

    private MailPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * 根據SMLPARM.SMLPARM_PRIORITY表的值轉換結果
     *
     * @param smlparmPriority
     * @return
     */
    public static MailPriority fromSmlparmPriority(Short smlparmPriority) {
        if (smlparmPriority == null) return Normal;
        switch (smlparmPriority) {
            case 0:
                return Lowest;
            case 9:
                return High;
            default:
                return Normal;
        }
    }
}
