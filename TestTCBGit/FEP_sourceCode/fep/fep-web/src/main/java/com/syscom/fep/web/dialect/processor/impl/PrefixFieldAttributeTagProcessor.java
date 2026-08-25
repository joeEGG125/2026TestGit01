package com.syscom.fep.web.dialect.processor.impl;

import com.syscom.fep.web.dialect.processor.AbstractAttributeTagTagProcessor;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;

public class PrefixFieldAttributeTagProcessor extends AbstractAttributeTagTagProcessor {
    private static final String PREFIX_ATTRIBUTE_NAME = "field-";

    public PrefixFieldAttributeTagProcessor(String dialectPrefix) {
        super(dialectPrefix);
    }

    /**
     * 匹配attributeName
     *
     * @param attributeName
     * @return
     */
    @Override
    protected boolean matches(AttributeName attributeName) {
        return attributeName.getAttributeName().startsWith(PREFIX_ATTRIBUTE_NAME);
    }

    /**
     * 處理
     *
     * @param context
     * @param tag
     * @param attributeName
     * @param attributeValue
     * @param expressionResult
     * @param structureHandler
     */
    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, Object expressionResult, IElementTagStructureHandler structureHandler) {
        structureHandler.removeAttribute(attributeName);
        structureHandler.setAttribute(StringUtils.replace(attributeName.getAttributeName(), PREFIX_ATTRIBUTE_NAME, StringUtils.EMPTY), expressionResult.toString());
    }
}
