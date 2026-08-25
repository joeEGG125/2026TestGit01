package com.syscom.fep.web.dialect;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

public class DialectProcessorUtil {

	private DialectProcessorUtil() {}

	/**
	 * @param context
	 * @param attributeValue
	 * @return
	 */
	public static Object parseExpression(ITemplateContext context, String attributeValue) {
		IEngineConfiguration configuration = context.getConfiguration();
		IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);
		IStandardExpression expression = parser.parseExpression(context, attributeValue);
		return expression.execute(context);
	}
}
