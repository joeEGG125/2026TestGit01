package com.syscom.fep.frmcommon.os.netstat;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.lang.StringUtils;

public class NetstatConnection implements NetstatConstant {
    protected final LogHelper logger = new LogHelper();
    private String protocol;
    private String localAddress;
    private String foreignAddress;
    private NetstatState state;
    private int pid;

    public NetstatConnection parse(String[] fields) throws Exception {
        try {
            this.setProtocol(fields[FIELD_WIN_PROTOCOL]);
            this.setLocalAddress(fields[FIELD_WIN_LOCALADDRESS]);
            this.setForeignAddress(fields[FIELD_WIN_FOREIGNADDRESS]);
            try {
                this.setState(StringUtils.isBlank(fields[FIELD_WIN_STATE]) ? NetstatState.NONE : NetstatState.valueOf(fields[FIELD_WIN_STATE]));
            } catch (IllegalArgumentException e) {
                logger.warn(e, "Illegal argument in NetstatConnection.parse() for field " + fields[FIELD_WIN_STATE]);
                this.setState(NetstatState.UNKNOWN);
            }
            this.setPid(Integer.parseInt(fields[FIELD_WIN_PID]));
            return this;
        } catch (IndexOutOfBoundsException e) {
            throw new Exception("Invalid fields size: " + fields.length, e);
        }
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public String getForeignAddress() {
        return foreignAddress;
    }

    public void setForeignAddress(String foreignAddress) {
        this.foreignAddress = foreignAddress;
    }

    public NetstatState getState() {
        return state;
    }

    public void setState(NetstatState state) {
        this.state = state;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }
}
