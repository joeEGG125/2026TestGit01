package com.syscom.fep.vo.communication;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * WebAPI供fepweb呼叫取得各線路的狀態, 取得各線路目前的連線狀態及是否啟用(Enable屬性), 照以下json格式回傳前端
 * {
 * "HostName": "fepap1T",
 * "CBSTypes": [
 * {
 * "TypeName": "CBS",
 * "Lines": [
 * {
 * "SClientId": "IFEPTA01",
 * "RClientId": "IFEPTB01",
 * "Enable": "true",
 * "IsConnected": "true",
 * "LineType": "primary"
 * },
 * {
 * "SClientId": "IFEPTC01",
 * "RClientId": "IFEPTD01",
 * "Enable": "true",
 * "IsConnected": "true",
 * "LineType": "alternative"
 * }
 * ]
 * },
 * {
 * "TypeName": "FISC",
 * "Lines": [
 * {
 * "SClientId": "IFEPTA11",
 * "RClientId": "IFEPTB11",
 * "Enable": "true",
 * "IsConnected": "true",
 * "LineType": "primary"
 * },
 * {
 * "SClientId": "IFEPTC11",
 * "RClientId": "IFEPTD11",
 * "Enable": "true",
 * "IsConnected": "true",
 * "LineType": "alternative"
 * }
 * ]
 * },
 * {
 * "TypeName": "473X",
 * "Lines": [
 * {
 * "SClientId": "IFEPTA21",
 * "RClientId": "IFEPTB21",
 * "Enable": "true",
 * "IsConnected": "true",
 * "LineType": "primary"
 * },
 * {
 * "SClientId": "IFEPTC21",
 * "RClientId": "IFEPTD21",
 * "Enable": "true",
 * "IsConnected": "true",
 * "LineType": "alternative"
 * }
 * ]
 * }
 * ]
 * }
 *
 * @author Richard
 */
public class ToFEPCBSGetAllLineStatus implements Serializable {
    @SerializedName("HostName")
    private String hostName;
    @SerializedName("CBSTypes")
    private List<CBSTypes> cbsTypes;
    private transient boolean show;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public List<CBSTypes> getCbsTypes() {
        return cbsTypes;
    }

    public void setCbsTypes(List<CBSTypes> cbsTypes) {
        this.cbsTypes = cbsTypes;
    }

    public static ToFEPCBSGetAllLineStatus clone(ToFEPCBSGetAllLineStatus source) {
        ToFEPCBSGetAllLineStatus clone = null;
        if (source != null) {
            clone = new ToFEPCBSGetAllLineStatus();
            clone.setHostName(source.getHostName());
            clone.setShow(source.isShow());
            List<CBSTypes> cbsTypes = source.getCbsTypes();
            if (CollectionUtils.isNotEmpty(cbsTypes)) {
                clone.setCbsTypes(new ArrayList<>());
                for (CBSTypes cbsType : cbsTypes) {
                    if (cbsType != null) {
                        CBSTypes cloneCbsType = new CBSTypes();
                        cloneCbsType.setTypeName(cbsType.getTypeName());
                        List<Line> lines = cbsType.getLines();
                        if (CollectionUtils.isNotEmpty(lines)) {
                            cloneCbsType.setLines(new ArrayList<>());
                            for (Line line : lines) {
                                if (line != null) {
                                    Line cloneLine = new Line();
                                    BeanUtils.copyProperties(line, cloneLine);
                                    cloneCbsType.getLines().add(cloneLine);
                                }
                            }
                        }
                        Server server = cbsType.getServer();
                        if (server != null) {
                            Server cloneServer = new Server();
                            BeanUtils.copyProperties(server, cloneServer);
                            cloneCbsType.setServer(cloneServer);
                        }
                        clone.getCbsTypes().add(cloneCbsType);
                    }
                }
            }
        }
        return clone;
    }

    public static class CBSTypes {
        @SerializedName("TypeName")
        private String typeName;
        @SerializedName("Lines")
        private List<Line> lines;
        @SerializedName("Server")
        private Server server;

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public List<Line> getLines() {
            return lines;
        }

        public void setLines(List<Line> lines) {
            this.lines = lines;
        }

        public Server getServer() {
            return server;
        }

        public void setServer(Server server) {
            this.server = server;
        }
    }

    public static class Line {
        @SerializedName("SClientId")
        private String sClientId;
        @SerializedName("RClientId")
        private String rClientId;
        @SerializedName("Enable")
        private boolean enable;
        @SerializedName("IsConnected")
        private boolean connected;
        @SerializedName("LineType")
        private LineType lineType;
        @SerializedName("Transactions")
        private long transactions; // 正在交易的筆數
        @SerializedName("TotalTransactions")
        private long totalTransactions; // 正在交易的總筆數
        @SerializedName("Pause")
        private boolean pause;
        @SerializedName("Status")
        private LineStatus status;

        public String getsClientId() {
            return sClientId;
        }

        public void setsClientId(String sClientId) {
            this.sClientId = sClientId;
        }

        public String getrClientId() {
            return rClientId;
        }

        public void setrClientId(String rClientId) {
            this.rClientId = rClientId;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public LineType getLineType() {
            return lineType;
        }

        public void setLineType(LineType lineType) {
            this.lineType = lineType;
        }

        public long getTransactions() {
            return transactions;
        }

        public void setTransactions(long transactions) {
            this.transactions = transactions;
        }

        public long getTotalTransactions() {
            return totalTransactions;
        }

        public void setTotalTransactions(long totalTransactions) {
            this.totalTransactions = totalTransactions;
        }

        public boolean isPause() {
            return pause;
        }

        public void setPause(boolean pause) {
            this.pause = pause;
        }

        public LineStatus getStatus() {
            return status;
        }

        public void setStatus(LineStatus status) {
            this.status = status;
        }
    }

    public static enum LineType {
        Primary("主要線路"), Alternative("備援線路");

        private final String description;

        LineType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static enum LineStatus {
        Enable("啟用"),
        Disable("停用"),
        Pause("暫停"),
        Connected("連線"),
        Disconnected("斷線");

        private final String description;

        LineStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class Server {
        private String endPoint;
        private boolean listen;
        private boolean enable;

        public String getEndPoint() {
            return endPoint;
        }

        public void setEndPoint(String endPoint) {
            this.endPoint = endPoint;
        }

        public boolean isListen() {
            return listen;
        }

        public void setListen(boolean listen) {
            this.listen = listen;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }
    }
}
