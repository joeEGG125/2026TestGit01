package com.syscom.fep.web.dialect.processor.impl;

import com.syscom.fep.web.dialect.processor.AbstractStandardExpressionAttributeTagTagProcessor;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;

public class LiNavItemAttributeTagProcessor extends AbstractStandardExpressionAttributeTagTagProcessor {
	private static final String ATTR_NAME = "nav-item";
	private static final String ATTR_NAME_SELECTED = "nav-item-selected";
	private static final String ATTR_NAME_CAPTION = "nav-item-caption";

	public LiNavItemAttributeTagProcessor(final String dialectPrefix) {
		super(dialectPrefix, ATTR_NAME);
	}

	@Override
	protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, Object expressionResult,
			IElementTagStructureHandler structureHandler) {
		Boolean selected = false;
		if (tag.hasAttribute(ATTR_NAME_SELECTED)) {
			selected = (Boolean) this.parseExpression(context, tag.getAttributeValue(ATTR_NAME_SELECTED));
			structureHandler.removeAttribute(ATTR_NAME_SELECTED);
		}
		Object caption = null;
		if (tag.hasAttribute(ATTR_NAME_CAPTION)) {
			caption = this.parseExpression(context, tag.getAttributeValue(ATTR_NAME_CAPTION));
			structureHandler.removeAttribute(ATTR_NAME_CAPTION);
		}
		String id = (String) expressionResult;
		structureHandler.setAttribute("class", "nav-item");
		String body = StringUtils.join(
				"<a class=\"nav-link", (selected ? " active" : StringUtils.EMPTY), "\"",
				" id=\"", id, "-tab\"",
				" data-toggle=\"pill\"",
				" href=\"#", id, "\"",
				" role=\"tab\"",
				" aria-controls=\"", id, "\"",
				" aria-selected=\"", selected, "\">", caption, "</a>");
		structureHandler.setBody(body, false);
	}
}
