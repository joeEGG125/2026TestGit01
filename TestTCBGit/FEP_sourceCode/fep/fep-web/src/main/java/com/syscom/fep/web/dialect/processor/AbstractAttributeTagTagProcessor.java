package com.syscom.fep.web.dialect.processor;

import org.apache.commons.lang3.ArrayUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.*;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.AbstractProcessor;
import org.thymeleaf.processor.element.IElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.processor.element.MatchingAttributeName;
import org.thymeleaf.processor.element.MatchingElementName;
import org.thymeleaf.standard.expression.FragmentExpression;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.NoOpToken;
import org.thymeleaf.standard.expression.StandardExpressionExecutionContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.EscapedAttributeUtils;
import org.thymeleaf.util.Validate;

public abstract class AbstractAttributeTagTagProcessor extends AbstractProcessor implements IElementTagProcessor {
    private static final int PRECEDENCE = 100;
    private final String dialectPrefix;
    private final StandardExpressionExecutionContext expressionExecutionContext;
    private final boolean removeIfNoop;

    public AbstractAttributeTagTagProcessor(final String dialectPrefix) {
        this(dialectPrefix, PRECEDENCE, true, StandardExpressionExecutionContext.NORMAL);
    }

    public AbstractAttributeTagTagProcessor(final String dialectPrefix, final int precedence, final boolean removeAttribute,
                                            final boolean restrictedExpressionExecution) {
        this(dialectPrefix, precedence, removeAttribute,
                (restrictedExpressionExecution ? StandardExpressionExecutionContext.RESTRICTED : StandardExpressionExecutionContext.NORMAL));
    }

    public AbstractAttributeTagTagProcessor(final String dialectPrefix, final int precedence, final boolean removeAttribute,
                                            final StandardExpressionExecutionContext expressionExecutionContext) {
        super(TemplateMode.HTML, precedence);
        this.dialectPrefix = dialectPrefix;
        this.removeIfNoop = !removeAttribute;
        this.expressionExecutionContext = expressionExecutionContext;
    }

    @Override
    public void process(final ITemplateContext context, final IProcessableElementTag tag, final IElementTagStructureHandler structureHandler) {
        IAttribute[] iAttributes = tag.getAllAttributes();
        if (ArrayUtils.isNotEmpty(iAttributes)) {
            for (IAttribute iAttribute : iAttributes) {
                if (iAttribute.getAttributeDefinition() != null && iAttribute.getAttributeDefinition().getAttributeName() != null) {
                    AttributeName attributeName = iAttribute.getAttributeDefinition().getAttributeName();
                    if (this.matches(context, tag, structureHandler, attributeName)) {
                        final String attributeValue = EscapedAttributeUtils.unescapeAttribute(context.getTemplateMode(), tag.getAttributeValue(attributeName));
                        Object expressionResult = null;
                        if (attributeValue != null) {
                            final IStandardExpression expression = EngineEventUtils.computeAttributeExpression(context, tag, attributeName, attributeValue);
                            if (expression != null) {
                                if (expression instanceof FragmentExpression) {
                                    final FragmentExpression.ExecutedFragmentExpression executedFragmentExpression = FragmentExpression.createExecutedFragmentExpression(context, (FragmentExpression) expression);
                                    expressionResult = FragmentExpression.resolveExecutedFragmentExpression(context, executedFragmentExpression, true);
                                } else {
                                    expressionResult = expression.execute(context, this.expressionExecutionContext);
                                }
                            }
                        }
                        if (expressionResult == NoOpToken.VALUE) {
                            if (this.removeIfNoop) {
                                structureHandler.removeAttribute(attributeName);
                            }
                            return;
                        }
                        doProcess(
                                context, tag,
                                attributeName, attributeValue,
                                expressionResult, structureHandler);
                    }
                }
            }
        }
    }

    private boolean matches(final ITemplateContext context, final IProcessableElementTag tag, final IElementTagStructureHandler structureHandler, final AttributeName attributeName) {
        Validate.notNull(attributeName, "Attributes name cannot be null");
        final TemplateMode templateMode = context.getTemplateMode();
        if (templateMode == TemplateMode.HTML && !(attributeName instanceof HTMLAttributeName)) {
            return false;
        } else if (templateMode == TemplateMode.XML && !(attributeName instanceof XMLAttributeName)) {
            return false;
        } else if (templateMode.isText() && !(attributeName instanceof TextAttributeName)) {
            return false;
        }
        final String attributeNamePrefix = attributeName.getPrefix();
        if (attributeNamePrefix == null) {
            return false;
        }
        return this.dialectPrefix.equals(attributeNamePrefix) && this.matches(attributeName);
    }

    /**
     * 匹配attributeName
     *
     * @param attributeName
     * @return
     */
    protected abstract boolean matches(final AttributeName attributeName);

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
    protected abstract void doProcess(
            final ITemplateContext context,
            final IProcessableElementTag tag,
            final AttributeName attributeName,
            final String attributeValue,
            final Object expressionResult,
            final IElementTagStructureHandler structureHandler);

    @Override
    public MatchingElementName getMatchingElementName() {
        return null;
    }

    @Override
    public MatchingAttributeName getMatchingAttributeName() {
        return null;
    }
}
