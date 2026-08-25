package com.syscom.fep.web.form;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.web.entity.SQLSortExpression;
import com.syscom.fep.web.entity.SQLSortExpression.SQLSortOrder;

/**
 * 表單對象的父類
 * 
 * @author Richard
 */
public class BaseForm implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 處理表單的URL
	 */
	private String url;
	/**
	 * 用於分頁查詢, 當前頁數
	 */
	private Integer pageNum = 1;
	/**
	 * 分頁大小
	 */
	private Integer pageSize = 20;
	/**
	 * 用於查詢SQL語句中的Order By
	 */
	private List<SQLSortExpression> sqlSortExpressionList = new ArrayList<>();
	/**
	 * 是否是按下分頁按鈕的重定向表單分頁查詢
	 */
	private boolean isRedirectFromPageChanged = false;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getPageNum() {
		return pageNum;
	}

	public void setPageNum(Integer pageNum) {
		this.pageNum = pageNum;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public boolean isRedirectFromPageChanged() {
		return isRedirectFromPageChanged;
	}

	public void setRedirectFromPageChanged(boolean isRedirectFromPageChanged) {
		this.isRedirectFromPageChanged = isRedirectFromPageChanged;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	/**
	 * map化
	 * 
	 * @return
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			if ("sqlSortExpressionList".equals(field.getName())) {
				continue;
			}
			map.put(field.getName(), ReflectUtil.getFieldValue(this, field, null));
		}
		// 以下兩個父類的屬性也要加入
		map.put("pageNum", pageNum);
		map.put("pageSize", pageSize);
		return map;
	}

	/**
	 * 增加要排序的資料庫欄位名, 多個欄位名用逗號間隔
	 * 
	 * @param sqlSortColumnName
	 * @return
	 */
	public SQLSortExpression addSqlSortExpression(String sqlSortColumnName, SQLSortOrder sqlSortOrder) {
		if (StringUtils.isNotBlank(sqlSortColumnName)) {
			SQLSortExpression sqlSortExpression = new SQLSortExpression(sqlSortColumnName);
			int index = this.sqlSortExpressionList.indexOf(sqlSortExpression);
			if (index == -1) {
				if (sqlSortOrder != null && sqlSortOrder != SQLSortOrder.NONE) {
					// 設置排序順序
					sqlSortExpression.setOrder((int) this.sqlSortExpressionList.stream().filter(t -> t.getSortOrder() != SQLSortOrder.NONE).count() + 1);
				}
				this.sqlSortExpressionList.add(sqlSortExpression);
			} else {
				sqlSortExpression = this.sqlSortExpressionList.get(index);
				if (sqlSortOrder != null) {
					// 原本未排序改為排序, 則設置排序順序為最大的值
					if (sqlSortExpression.getSortOrder() == SQLSortOrder.NONE && sqlSortOrder != null) {
						// 設置排序順序
						sqlSortExpression.setOrder((int) this.sqlSortExpressionList.stream().filter(t -> t.getSortOrder() != SQLSortOrder.NONE).count() + 1);
					}
					// 原本排序改為未排序, 則將大於此
					else if (sqlSortExpression.getSortOrder() != SQLSortOrder.NONE && sqlSortOrder != null) {
						// 調整該列之後的排列順序減1
						for (SQLSortExpression expression : this.sqlSortExpressionList) {
							if (expression.getOrder() > sqlSortExpression.getOrder()) {
								expression.setOrder(expression.getOrder() - 1);
							}
						}
						// 最後設置排序順序為0
						sqlSortExpression.setOrder(0);
					}
				}
			}
			if (sqlSortOrder != null) {
				sqlSortExpression.setSortOrder(sqlSortOrder);
			}
			return sqlSortExpression;
		}
		return null;
	}

	/**
	 * 根據hash取出SQLSortExpression, 改變SQLSortOrder
	 * 
	 * @param hash
	 */
	public void changeSqlSortOrder(int hash) {
		if (this.sqlSortExpressionList.size() == 0) {
			return;
		}
		SQLSortExpression sqlSortExpression = this.sqlSortExpressionList.stream().filter(t -> t.getHash() == hash).findFirst().orElse(null);
		if (sqlSortExpression != null) {
			// 新加入的排序
			if (sqlSortExpression.getSortOrder() == SQLSortOrder.NONE) {
				// 設置排序順序
				sqlSortExpression.setOrder((int) this.sqlSortExpressionList.stream().filter(t -> t.getSortOrder() != SQLSortOrder.NONE).count() + 1);
				// 修改為正序
				sqlSortExpression.setSortOrder(SQLSortOrder.ASC);
			}
			// 原本是正序排序
			else if (sqlSortExpression.getSortOrder() == SQLSortOrder.ASC) {
				// 修改為倒序
				sqlSortExpression.setSortOrder(SQLSortOrder.DESC);
			}
			// 原本是倒序
			else if (sqlSortExpression.getSortOrder() == SQLSortOrder.DESC) {
				// 改為不排序
				sqlSortExpression.setSortOrder(SQLSortOrder.NONE);
				// 同時調整該列之後的排列順序減1
				for (SQLSortExpression expression : this.sqlSortExpressionList) {
					if (expression.getOrder() > sqlSortExpression.getOrder()) {
						expression.setOrder(expression.getOrder() - 1);
					}
				}
				// 最後設置排序順序為0
				sqlSortExpression.setOrder(0);
			}
		}
	}

	/**
	 * 獲取SQL表達式中的Order By
	 * 
	 * @return
	 */
	public String getSqlSortExpression() {
		if (this.sqlSortExpressionList.size() > 0) {
			this.sqlSortExpressionList.sort(new Comparator<SQLSortExpression>() {
				@Override
				public int compare(SQLSortExpression o1, SQLSortExpression o2) {
					return o1.getOrder() - o2.getOrder();
				}
			});
			StringBuilder sb = new StringBuilder();
			for (SQLSortExpression sqlSortExpression : sqlSortExpressionList) {
				String expression = sqlSortExpression.toString();
				if (StringUtils.isNotBlank(expression)) {
					sb.append(expression).append(", ");
				}
			}
			if (sb.length() > 0) {
				sb.delete(sb.length() - 2, sb.length());
			}
			return sb.toString();
		}
		return StringUtils.EMPTY;
	}

	public int getSqlSortExpressionCount() {
		return sqlSortExpressionList.size();
	}
}
