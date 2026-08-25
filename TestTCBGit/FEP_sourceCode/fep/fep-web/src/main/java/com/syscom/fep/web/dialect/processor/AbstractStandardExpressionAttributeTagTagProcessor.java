package com.syscom.fep.web.dialect.processor;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.standard.processor.AbstractStandardExpressionAttributeTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;

import com.syscom.fep.web.dialect.DialectProcessorUtil;

public abstract class AbstractStandardExpressionAttributeTagTagProcessor extends AbstractStandardExpressionAttributeTagProcessor {
    private static final int PRECEDENCE = 100;

    public AbstractStandardExpressionAttributeTagTagProcessor(final String dialectPrefix, final String attrName) {
        super(TemplateMode.HTML, dialectPrefix, attrName, PRECEDENCE, true);
    }

    /**
     * @param context
     * @param attributeValue
     * @return
     */
    protected Object parseExpression(ITemplateContext context, String attributeValue) {
        return DialectProcessorUtil.parseExpression(context, attributeValue);
    }
}
