package com.syscom.fep.frmcommon.ims.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 回傳狀態說明碼
 *
 * @see <a href="https://www.ibm.com/support/knowledgecenter/en/SSEPH2_13.1.0/com.ibm.ims13.doc.msgs/compcodes/ims_hwssmpl01csl01codes.htm">詳細回傳碼說明</a>
 */
public enum ImsConnectReasonCode {
    CODE_4("N/A", 4, "Input data exceeds buffer size."),
    CODE_5("N/A", 5, "Negative length value."),
    CODE_6("N/A", 6, "IMS request message (IRM) length invalid."),
    CODE_7("N/A", 7, "Total message length invalid."),
    CODE_8("N/A", 8, "OTMA NAK with no sense code or RC."),
    CODE_9("N/A", 9, "Contents of buffer invalid."),
    CODE_10("N/A", 10, "Output data exceeds buffer size."),
    CODE_11("N/A", 11, "Invalid unicode definition."),
    CODE_12("N/A", 12, "Invalid message, no data."),
    CODE_16("N/A", 16, "Do not know who client is."),
    CODE_20("N/A", 20, "OTMA segment length error."),
    CODE_24("N/A", 24, "FIC missing."),
    CODE_28("N/A", 28, "LIC missing."),
    CODE_32("N/A", 32, "Sequence number error."),
    CODE_34("N/A", 34, "Unable to locate context token."),
    CODE_36("N/A", 36, "Protocol error."),
    CODE_40("N/A", 40, "Security violation."),
    CODE_44("N/A", 44, "Message incomplete."),
    CODE_48("N/A", 48, "Incorrect message length."),
    CODE_51("NOSECHDR", 51, "Security failure: no OTMA security header."),
    CODE_52("INVESECHL", 52, "Security failure: no security data in OTMA security header."),
    CODE_53("SECFNOPW", 53, "Security failure: no password in OTMA user data header."),
    CODE_54("SECFNUID", 54, "Security failure: no user ID in OTMA security header."),
    CODE_55("SECFNPUI", 55, "Security failure: no password in OTMA user data and no user ID in OTMA security header."),
    CODE_56("DUPECLNT", 56, "Duplicate Client ID used; the client ID is currently in use."),
    CODE_57("INVLDTOK", 57, "Invalid token is being used: internal error."),
    CODE_58("INVLDSTA", 58, "Invalid client status: internal error."),
    CODE_59("CANTIMER", 59, "Cancel Timer completed successfully."),
    CODE_70("NFNDCOMP", 70, "Component not found."),
    CODE_71("NFNDFUNC", 71, "Function not found."),
    CODE_72("NFNDDST", 72, "The data store was not found, or communication with the data store was stopped using an IMS Connect command."),
    CODE_73("DSCLOSE", 73, "IMS Connect in shutdown."),
    CODE_74("STP/CLSE", 74, "The data store or IMSplex was in a stop or close process, or the IMS data store has shut down or disconnected."),
    CODE_75("DSCERR", 75, "Data store communication error."),
    CODE_76("STOPCMD", 76, "The data store or IMSplex was stopped by command."),
    CODE_77("COMMERR", 77, "A data store or IMSplex communication error was sent to the pending client."),
    CODE_78("SECFAIL", 78, "Security failure. RACF® call failed, IMS Connect call failed. See IMS Connect error message on system console."),
    CODE_79("PROTOERR", 79, "IMS Connect protocol error. See IMS Connect error message on system console."),
    CODE_80("NOTACTV", 80, "The IMSplex connection is not active. The STOPIP command was issued or the SCI address space is not active."),
    CODE_81("CANCRTP", 81, "IMS cancelled the Resume Tpipe request as a result of an ACKTO timeout, or a /STO TMEMBER TPIPE command."),
    CODE_93("INVLDCM1", 93, "Invalid commit mode of 1 specified on the RESUME TPIPE request."),
    CODE_94("REQUEST", 94, "Request."),
    CODE_95("CONVER", 95, "Conversation."),
    CODE_96("REQ_CON", 96, "Request and conversation."),
    CODE_97("DEAL_CTD", 97, "Deallocate confirmed."),
    CODE_98("DEAL_ABT", 98, "Deallocate abnormal termination."),
    CODE_99("", 99, "Default reason code.");

    private String code;
    private int value;
    private String description;

    private ImsConnectReasonCode(String code, int value, String description) {
        this.code = code;
        this.value = value;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static ImsConnectReasonCode fromValue(int value) {
        for (ImsConnectReasonCode e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static ImsConnectReasonCode parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (ImsConnectReasonCode e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
