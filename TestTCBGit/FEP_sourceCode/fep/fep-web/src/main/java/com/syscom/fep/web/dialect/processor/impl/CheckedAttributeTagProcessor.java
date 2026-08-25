package com.syscom.fep.web.dialect.processor.impl;

import com.syscom.fep.web.dialect.processor.AbstractStandardExpressionAttributeTagTagProcessor;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;

public class CheckedAttributeTagProcessor extends AbstractStandardExpressionAttributeTagTagProcessor {
	private static final String ATTR_NAME = "checked";

	public CheckedAttributeTagProcessor(final String dialectPrefix) {
		super(dialectPrefix, ATTR_NAME);
	}

	@Override
	protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, Object expressionResult,
			IElementTagStructureHandler structureHandler) {
		Object value = tag.getAttributeValue("value");
		if (value == null && tag.hasAttribute("th:value")) {
			value = this.parseExpression(context, tag.getAttributeValue("th:value"));
		}
		if (value.equals(expressionResult)) {
			structureHandler.setAttribute("checked", "true");
		} else {
			structureHandler.removeAttribute("checked");
		}
	}
}
