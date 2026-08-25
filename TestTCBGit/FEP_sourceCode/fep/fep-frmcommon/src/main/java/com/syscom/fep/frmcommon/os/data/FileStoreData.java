package com.syscom.fep.frmcommon.os.data;

public class FileStoreData {
    private String name;
    private String volume;
    private String label;
    private String logicalVolume;
    private String mount;
    private String description;
    private String type;
    private String options;
    private String uuid;
    private long freeSpace;
    private long usableSpace;
    private long totalSpace;
    private long freeInodes;
    private long totalInodes;

    public long getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(long freeSpace) {
        this.freeSpace = freeSpace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLogicalVolume() {
        return logicalVolume;
    }

    public void setLogicalVolume(String logicalVolume) {
        this.logicalVolume = logicalVolume;
    }

    public String getMount() {
        return mount;
    }

    public void setMount(String mount) {
        this.mount = mount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getUsableSpace() {
        return usableSpace;
    }

    public void setUsableSpace(long usableSpace) {
        this.usableSpace = usableSpace;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public long getFreeInodes() {
        return freeInodes;
    }

    public void setFreeInodes(long freeInodes) {
        this.freeInodes = freeInodes;
    }

    public long getTotalInodes() {
        return totalInodes;
    }

    public void setTotalInodes(long totalInodes) {
        this.totalInodes = totalInodes;
    }
}
