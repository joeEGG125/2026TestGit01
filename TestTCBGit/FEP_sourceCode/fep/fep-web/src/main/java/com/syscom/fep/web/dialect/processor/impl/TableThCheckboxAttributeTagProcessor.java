package com.syscom.fep.web.dialect.processor.impl;

import com.syscom.fep.web.dialect.processor.AbstractStandardExpressionAttributeTagTagProcessor;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;

public class TableThCheckboxAttributeTagProcessor extends AbstractStandardExpressionAttributeTagTagProcessor {
	private static final String ATTR_NAME = "table-th-check";

	public TableThCheckboxAttributeTagProcessor(final String dialectPrefix) {
		super(dialectPrefix, ATTR_NAME);
	}

	@Override
	protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, Object expressionResult,
			IElementTagStructureHandler structureHandler) {
		String id = (String) expressionResult;
		structureHandler.setAttribute("width", "30px");
		structureHandler.setBody(StringUtils.join("<input type=\"checkbox\" id=\"", id, "CheckAll\" name=\"", id, "CheckAll\" onclick=\"doCheckAllInTableHeader(this,'", id, "');\" />"), false);
	}

}
