package com.syscom.fep.web.dialect.processor.impl;

import com.syscom.fep.web.dialect.processor.AbstractStandardExpressionAttributeTagTagProcessor;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;

import com.syscom.fep.web.entity.SQLSortExpression;
import com.syscom.fep.web.form.BaseForm;

public class TableThSortAttributeTagProcessor extends AbstractStandardExpressionAttributeTagTagProcessor {
	private static final String ATTR_NAME = "sort";
	private static final String ATTR_NAME_DATA_SORT_COLUMN = "data-sort-column";

	public TableThSortAttributeTagProcessor(final String dialectPrefix) {
		super(dialectPrefix, ATTR_NAME);
	}

	@Override
	protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, Object expressionResult,
			IElementTagStructureHandler structureHandler) {
		if (expressionResult instanceof BaseForm) {
			BaseForm form = (BaseForm) expressionResult;
			if (tag.hasAttribute(ATTR_NAME_DATA_SORT_COLUMN)) {
				String sqlSortColumnName = tag.getAttributeValue(ATTR_NAME_DATA_SORT_COLUMN);
				structureHandler.removeAttribute(ATTR_NAME_DATA_SORT_COLUMN);
				SQLSortExpression sqlSortExpression = form.addSqlSortExpression(sqlSortColumnName, null);
				if (sqlSortExpression != null) {
					switch (sqlSortExpression.getSortOrder()) {
						case ASC:
							structureHandler.setAttribute("class", "fas fa-sort-up");
							break;
						case DESC:
							structureHandler.setAttribute("class", "fas fa-sort-down");
							break;
						case NONE:
							structureHandler.setAttribute("class", "fas fa-sort");
							break;
						default:
							structureHandler.setAttribute("class", "fas fa-sort");
							break;
					}
					structureHandler.setAttribute("data-sort-hash", String.valueOf(sqlSortExpression.getHash()));
					if (sqlSortExpression.getOrder() > 0) {
						structureHandler.setBody(StringUtils.leftPad(String.valueOf(sqlSortExpression.getOrder()), 2), false);
					}
				}
			}
		} else {
			throw new TemplateProcessingException(StringUtils.join("cannot parse attributeName = [", attributeName, "] with attributeValue = [", attributeValue, "], cause expressionResult = [",
					expressionResult, "] is not instanceof BaseForm"));
		}
	}

}
