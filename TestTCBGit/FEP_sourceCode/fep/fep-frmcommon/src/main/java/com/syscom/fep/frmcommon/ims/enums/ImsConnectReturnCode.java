package com.syscom.fep.frmcommon.ims.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 回傳狀態碼
 *
 * @see <a href="https://www.ibm.com/support/knowledgecenter/en/SSEPH2_13.1.0/com.ibm.ims13.doc.msgs/compcodes/ims_hwssmpl01csl01codes.htm">詳細回傳碼說明</a>
 */
public enum ImsConnectReturnCode {
    CODE_0x04(0x04, "Exit request error message sent to client before socket termination. The socket is disconnected for IMS™."),
    CODE_0x08(0x08, "Error detected by IMS Connect and the socket is disconnected for IMS."),
    CODE_0x0C(0x0C, "Error returned by IMS OTMA and the socket is disconnected for IMS."),
    CODE_0x10(0x10, "Error returned by IMS OTMA when an OTMA sense code is returned in the \"Reason Code\" field of the RSM. The socket is disconnected for IMS. See OTMA codes"),
    CODE_0x14(0x14, "Exit requests response message to HWSPWCH/PING request to be returned to client. IMS Connect keeps the socket connection because the PWCH/PING came in on a new socket connection or an existing persistent socket connection that is not in conversational mode or waiting for an ACK/NAK from the client application.\r\nIMS Connect can also determine whether to keep the connection depending upon the socket status after sending a user-defined message back to the client application."),
    CODE_0x18(0x18, "SCI error detected, see CSL codes for reason codes. The socket is disconnected for IMS."),
    CODE_0x1C(0x1C, "OM error detected, see CSL codes for reason codes. The socket is disconnected for IMS."),
    CODE_0x20(0x20, "IRM_TIMER value expired. When this return code is issued, the value of the corresponding reason code is not a code, but rather the time interval that was in effect for the IRM_TIMER. The socket is disconnected by IMS Connect."),
    CODE_0x24(0x24, "A default IRM_TIMER value expired. Either the IRM_TIMER value specified was X'00' or an invalid value. When this return code is issued, the value of the corresponding reason code is not a code, but rather the time interval that was in effect for the IRM_TIMER. The socket is disconnected by IMS Connect."),
    CODE_0x28(0x28, "IRM_TIMER value expired. When this return code is issued, the value of the corresponding reason code is not a code, but rather the time interval that was in effect for the IRM_TIMER. The connection is not disconnected. The socket remains connected."),
    CODE_0x2C(0x2C, "The DATASTORE in no longer available."),
    CODE_0x80(0x80, "DataPower® return codes. See the DataPower documentation for a description of these return and reason codes."),
    CODE_0x81(0x81, CODE_0x80.getDescription()),
    CODE_0x82(0x82, CODE_0x80.getDescription()),
    CODE_0x83(0x83, CODE_0x80.getDescription()),
    CODE_0x84(0x84, CODE_0x80.getDescription()),
    CODE_0x85(0x85, CODE_0x80.getDescription()),
    CODE_0x86(0x86, CODE_0x80.getDescription()),
    CODE_0x87(0x87, CODE_0x80.getDescription()),
    CODE_0x88(0x88, CODE_0x80.getDescription()),
    CODE_0x89(0x89, CODE_0x80.getDescription()),
    CODE_0x8A(0x8A, CODE_0x80.getDescription()),
    CODE_0x8B(0x8B, CODE_0x80.getDescription()),
    CODE_0x8C(0x8C, CODE_0x80.getDescription());

    private int value;
    private String description;

    private ImsConnectReturnCode(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static ImsConnectReturnCode fromValue(int value) {
        for (ImsConnectReturnCode e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static ImsConnectReturnCode parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (ImsConnectReturnCode e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
