package com.syscom.fep.frmcommon.xs.entity;

import java.util.Objects;

public class XsOriginalNode {
    /**
     * 名字
     */
    private String name;
    /**
     * 出現的次數
     */
    private int count;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void accumulateCount(int count) {
        this.count += count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XsOriginalNode that = (XsOriginalNode) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
