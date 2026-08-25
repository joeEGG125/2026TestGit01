package com.syscom.fep.frmcommon.os.netstat;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import oshi.util.ParseUtil;

public class NetstatUnixConnection extends NetstatConnection implements NetstatConstant {
    private int receiveQueue;
    private int sendQueue;
    private String programName;

    public NetstatUnixConnection parse(String[] fields) throws Exception {
        try {
            this.setProtocol(fields[FIELD_UNIX_PROTOCOL]);
            this.receiveQueue = ParseUtil.parseIntOrDefault(fields[FIELD_UNIX_RECEIVEQUEUE], 0);
            this.sendQueue = ParseUtil.parseIntOrDefault(fields[FIELD_UNIX_SENDQUEUE], 0);
            this.setLocalAddress(fields[FIELD_UNIX_LOCALADDRESS]);
            this.setForeignAddress(fields[FIELD_UNIX_FOREIGNADDRESS]);
            try {
                this.setState(StringUtils.isBlank(fields[FIELD_UNIX_STATE]) ? NetstatState.NONE : NetstatState.valueOf(fields[FIELD_UNIX_STATE]));
            } catch (IllegalArgumentException e) {
                logger.warn(e, "Illegal argument in NetstatConnection.parse() for field " + fields[FIELD_UNIX_STATE]);
                this.setState(NetstatState.UNKNOWN);
            }
            this.setPid(Integer.parseInt(fields[FIELD_UNIX_PID_PROGRAM].substring(0, fields[FIELD_UNIX_PID_PROGRAM].indexOf("/"))));
            this.programName = fields[FIELD_UNIX_PID_PROGRAM].substring(fields[FIELD_UNIX_PID_PROGRAM].indexOf("/") + 1);
            if (this.programName.startsWith("./")) {
                this.programName = this.programName.substring(2);
            }
            return this;
        } catch (IndexOutOfBoundsException e) {
            throw new Exception("Invalid fields size: " + fields.length, e);
        }
    }

    public int getReceiveQueue() {
        return receiveQueue;
    }

    public void setReceiveQueue(int receiveQueue) {
        this.receiveQueue = receiveQueue;
    }

    public int getSendQueue() {
        return sendQueue;
    }

    public void setSendQueue(int sendQueue) {
        this.sendQueue = sendQueue;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("protocol", getProtocol())
                .append("receiveQueue", receiveQueue)
                .append("sendQueue", sendQueue)
                .append("localAddress", getLocalAddress())
                .append("foreignAddress", getForeignAddress())
                .append("state", getState())
                .append("pid", getPid())
                .append("programName", programName)
                .toString();
    }
}
