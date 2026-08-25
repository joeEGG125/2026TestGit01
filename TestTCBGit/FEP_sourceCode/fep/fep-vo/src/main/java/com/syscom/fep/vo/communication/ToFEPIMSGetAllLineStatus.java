package com.syscom.fep.vo.communication;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * WebAPI供fepweb呼叫取得各線路的狀態, 取得各線路目前的連線狀態及是否啟用(Enable屬性), 照以下json格式回傳前端=
 *
 * @author Richard
 */
public class ToFEPIMSGetAllLineStatus implements Serializable {
    @SerializedName("HostName")
    private String hostName;
    @SerializedName("Modes")
    private List<IMSGatewayMode> modes;
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

    public List<IMSGatewayMode> getModes() {
        return modes;
    }

    public void setModes(List<IMSGatewayMode> modes) {
        this.modes = modes;
    }

    public static ToFEPIMSGetAllLineStatus clone(ToFEPIMSGetAllLineStatus source) {
        ToFEPIMSGetAllLineStatus clone = null;
        if (source != null) {
            clone = new ToFEPIMSGetAllLineStatus();
            clone.setHostName(source.getHostName());
            clone.setShow(source.isShow());
            List<IMSGatewayMode> modes = source.getModes();
            if (CollectionUtils.isNotEmpty(modes)) {
                clone.setModes(new ArrayList<>());
                for (IMSGatewayMode mode : modes) {
                    if (mode != null) {
                        IMSGatewayMode cloneCbsType = new IMSGatewayMode();
                        cloneCbsType.setModeName(mode.getModeName());
                        List<Line> lines = mode.getLines();
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
                        clone.getModes().add(cloneCbsType);
                    }
                }
            }
        }
        return clone;
    }

    public static class IMSGatewayMode {
        @SerializedName("ModeName")
        private String modeName;
        @SerializedName("Lines")
        private List<Line> lines;

        public String getModeName() {
            return modeName;
        }

        public void setModeName(String modeName) {
            this.modeName = modeName;
        }

        public List<Line> getLines() {
            return lines;
        }

        public void setLines(List<Line> lines) {
            this.lines = lines;
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
}
