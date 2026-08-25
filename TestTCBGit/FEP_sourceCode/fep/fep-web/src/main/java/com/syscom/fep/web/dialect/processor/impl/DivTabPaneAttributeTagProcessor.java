package com.syscom.fep.web.dialect.processor.impl;

import com.syscom.fep.web.dialect.processor.AbstractStandardExpressionAttributeTagTagProcessor;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;

public class DivTabPaneAttributeTagProcessor extends AbstractStandardExpressionAttributeTagTagProcessor {
	private static final String ATTR_NAME = "tab-pane";
	private static final String ATTR_NAME_SELECTED = "tab-pane-selected";
	private static final String ATTR_NAME_CLASS = "tab-pane-class";

	public DivTabPaneAttributeTagProcessor(final String dialectPrefix) {
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
		String clazz = null;
		if (tag.hasAttribute(ATTR_NAME_CLASS)) {
			clazz = (String) this.parseExpression(context, tag.getAttributeValue(ATTR_NAME_CLASS));
			structureHandler.removeAttribute(ATTR_NAME_CLASS);
		}
		
		String id = (String) expressionResult;
		structureHandler.setAttribute("class", 
				StringUtils.join("tab-pane fade", 
						(selected ? " show active" : StringUtils.EMPTY), 
						StringUtils.isNotEmpty(clazz) ? StringUtils.join(StringUtils.SPACE, clazz) : StringUtils.EMPTY));
		structureHandler.setAttribute("id", id);
		structureHandler.setAttribute("role", "tabpanel");
		structureHandler.setAttribute("aria-labelledby=", StringUtils.join(id, "-tab"));
	}
}
