package com.syscom.fep.web.entity;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PageView implements Serializable, Cloneable {
	private static final long serialVersionUID = 6620084075815983164L;
	/**
	 * 視圖
	 */
	private String view;
	/**
	 * 名稱
	 */
	private String name;
	/**
	 * 左側MENU欄是否縮合
	 */
	private boolean sidebarCollapsed;
	/**
	 * 條件區Panel是否縮合
	 */
	private boolean conditionPanelCollapsed;
	/**
	 * 是否需要顯示Collapse Button
	 */
	private boolean collapseButtonShownForConditionPanel = true;

	public String getView() {
		return view;
	}

	public void setView(String path) {
		this.view = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isSidebarCollapsed() {
		return sidebarCollapsed;
	}

	public void setSidebarCollapsed(boolean sidebarCollapsed) {
		this.sidebarCollapsed = sidebarCollapsed;
	}

	public boolean isConditionPanelCollapsed() {
		return conditionPanelCollapsed;
	}

	public void setConditionPanelCollapsed(boolean conditionPanelCollapsed) {
		this.conditionPanelCollapsed = conditionPanelCollapsed;
	}

	public boolean isCollapseButtonShownForConditionPanel() {
		return collapseButtonShownForConditionPanel;
	}

	public void setCollapseButtonShownForConditionPanel(boolean collapseButtonShownForConditionPanel) {
		this.collapseButtonShownForConditionPanel = collapseButtonShownForConditionPanel;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		if (that == null) {
			return false;
		}
		if (getClass() != that.getClass()) {
			return false;
		}
		PageView other = (PageView) that;
		return this.getView().equals(other.getView());
	}

	@Override
	public int hashCode() {
		return this.getView().hashCode(); // any arbitrary constant will do
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
