package com.syscom.fep.web.dialect.processor.impl;

import com.syscom.fep.web.dialect.processor.AbstractStandardExpressionAttributeTagTagProcessor;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;

public class TableTdCheckboxAttributeTagProcessor extends AbstractStandardExpressionAttributeTagTagProcessor {
	private static final String ATTR_NAME = "table-td-check";
	private static final String ATTR_NAME_VALUE = "table-td-check-value";

	public TableTdCheckboxAttributeTagProcessor(final String dialectPrefix) {
		super(dialectPrefix, ATTR_NAME);
	}

	@Override
	protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, Object expressionResult,
			IElementTagStructureHandler structureHandler) {
		Object value = null;
		if (tag.hasAttribute(ATTR_NAME_VALUE)) {
			value = this.parseExpression(context, tag.getAttributeValue(ATTR_NAME_VALUE));
			structureHandler.removeAttribute(ATTR_NAME_VALUE);
		}
		String id = (String) expressionResult;
		structureHandler.setAttribute("align", "center");
		structureHandler.setBody(StringUtils.join(
				"<input ",
				"type=\"checkbox\" ",
				"id=\"", id, "Check\" ",
				"name=\"", id, "Check\" ",
				value != null && value instanceof String && StringUtils.isNotBlank((String) value) ? StringUtils.join("value=\"", value, "\" ") : StringUtils.EMPTY,
				"onclick=\"doCheckInTableColumn('", id, "');\" />"), false);
	}

}
