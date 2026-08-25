package com.syscom.fep.web.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * SQL排序物件
 * 
 * @author Richard
 */
public class SQLSortExpression implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 排列順序, 從1開始
	 */
	private int order;
	/**
	 * 相當於是PK, 唯一標識
	 */
	private int hash;
	/**
	 * 排序的資料庫欄位, 可以有多個欄位, 如果是多表關聯查詢, 注意要給欄位名稱前增加table的alias名
	 */
	private List<String> columnNameList = new ArrayList<>();
	/**
	 * 正序、倒序還是不排序
	 */
	private SQLSortOrder sortOrder = SQLSortOrder.NONE;

	public SQLSortExpression(String columnName) {
		this.addColumnName(columnName);
		this.hash = HashCodeBuilder.reflectionHashCode(this);
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * 增加資料庫中的欄位名, 多個欄位名用逗號分隔
	 * 
	 * @param columnName
	 */
	public final void addColumnName(String columnName) {
		if (StringUtils.isNotBlank(columnName)) {
			if (columnName.contains(",")) {
				StringTokenizer token = new StringTokenizer(columnName, ",");
				while (token.hasMoreElements()) {
					String element = (String) token.nextElement();
					this.columnNameList.add(element);
				}
			} else {
				this.columnNameList.add(columnName);
			}
		}
	}

	public int getHash() {
		return hash;
	}

	public SQLSortOrder getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(SQLSortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

	public static enum SQLSortOrder {
		NONE, ASC, DESC
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
		SQLSortExpression other = (SQLSortExpression) that;
		return CollectionUtils.isEqualCollection(this.columnNameList, other.columnNameList);
	}

	@Override
	public int hashCode() {
		return this.columnNameList.stream().mapToInt(String::hashCode).sum(); // any arbitrary constant will do
	}

	/**
	 * 轉成SQL表達式
	 */
	@Override
	public String toString() {
		if (columnNameList.size() > 0 && sortOrder != SQLSortOrder.NONE) {
			StringBuilder sb = new StringBuilder();
			for (String columnName : columnNameList) {
				sb.append(columnName).append(StringUtils.SPACE).append(sortOrder.toString()).append(", ");
			}
			sb.delete(sb.length() - 2, sb.length());
			return sb.toString();
		}
		return StringUtils.EMPTY;
	}
}
