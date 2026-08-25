package com.syscom.fep.base.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 表示交易發動的來源
 *
 * @author Richard
 */
public enum FEPChannel {
    Unknown(0, ""),
    ATM(1, "ATM"),
    FISC(2, "FISC"),
    EAT(3, "EAT"),
    SINOCARD(4, "SINOCARD"),
    BRANCH(5, "BRANCH"),
    T24(6, "T24"),
    FEP(7, "FEP"),
    FCS(8, "FCS"),
    RM(9, "RM"),
    CBS(10, "CBS"),
    FEDI(11, "FEDI"),
    ETS(12, "ETS"),
    NETBANK(13, "NB"),
    IVR(14, "IVR"),
    EPORTAL(15, "EPORTAL"),
    SQL(16, "SQL"),
    BATCH(17, "BATCH"),
    CARDTP(18, "CARDTP"),
    GL(19, "GL"),
    EBILL(20, "EBILL"),
    PFS(21, "PFS"),
    MMAB2C(22, "MMAB2C"),
    MOBILBANK(23, "MBQ"),
    CSF3(24, "CSF3"),
    HSM(25, "HSM"),
    SVCS(26, "SVCS"),
    FUNCASH(27, "FUNCASH"),
    PSPTSM(28, "PSPTSM"),
    MMAB2B(29, "MMAB2B"),
    IPIN(30, "IPIN"),
    P33(31, "P33"),
    APPLYBIZ(32, "APPLYBIZ"),
    EWEB(33, "EWEB"),
    DAPP(34, "DAPP"),
    MDAWHO(35, "MDAWHO"),
    ELOAN(37, "ELOAN"),
    POS(40, "POS"),
    HCE(41, "HCA"),
    NBQ(51, "NBQ"),
    NBP(52, "NBP"),
    NBB(53, "NBB"),
    MBQ(54, "MBQ"),
    MQQ(55, "MQQ"),
    MSQ(56, "MSQ"),
    EOI(57, "EOI"),
    NAM(58, "NAM"),
    EIP(59, "EIP"),
    MFT(60, "MFT"),
    FID(61, "FID"),
    SSO(62, "SSO"),
    ONL(63, "ONL"),
    DIG(64, "DIG"),
    BIZ(65, "BIZ"),
    HCA(66, "HCA"),
    VO(67, "VO"),
    MCH(68, "MCH"),
    EDI(69, "EDI"),
    HLN(70, "HLN"),
    NONVIP(71, "NONVIP"),
    VIP(72, "VIP"),
    Send2160Handler(2160, "Send2160Handler"),
    OPN(73, "OPN"),
    AMPAYFL(74,"AMPAYFL");

    private final int code;
    private final String nameS;

    private FEPChannel(int code, String nameS) {
        this.code = code;
        this.nameS = nameS;
    }

    public int getCode() {
        return code;
    }

    public String getNameS() {
        return nameS;
    }

    public static FEPChannel fromCode(int code) {
        for (FEPChannel e : values()) {
            if (e.getCode() == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid code = [" + code + "]!!!");
    }

    public static FEPChannel parse(Object nameOrCode) {
        if (nameOrCode instanceof Number) {
            return fromCode(((Number) nameOrCode).intValue());
        } else if (nameOrCode instanceof String) {
            String nameOrCodeStr = (String) nameOrCode;
            if (StringUtils.isNumeric(nameOrCodeStr)) {
                return fromCode(Integer.parseInt(nameOrCodeStr));
            }
            for (FEPChannel e : values()) {
                if (e.name().equalsIgnoreCase(nameOrCodeStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or code = [" + nameOrCode + "]!!!");
    }

    public String toDescription() {
        return StringUtils.join(name(), "(", getCode(), ")");
    }
}