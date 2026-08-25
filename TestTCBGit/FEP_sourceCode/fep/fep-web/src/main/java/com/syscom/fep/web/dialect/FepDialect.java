package com.syscom.fep.web.dialect;

import com.syscom.fep.web.dialect.processor.impl.*;
import org.springframework.stereotype.Component;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.processor.StandardXmlNsTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.HashSet;
import java.util.Set;

@Component
public class FepDialect extends AbstractProcessorDialect {
    private static final String DIALECT_NAME = "FEP Dialect";

    public FepDialect() {
        super(DIALECT_NAME, "fep", StandardDialect.PROCESSOR_PRECEDENCE);
    }

    @Override
    public Set<IProcessor> getProcessors(final String dialectPrefix) {
        final Set<IProcessor> processors = new HashSet<IProcessor>();
        processors.add(new DateTimeFormatExpressionAttributeTagProcessor(dialectPrefix));
        processors.add(new CheckedAttributeTagProcessor(dialectPrefix));
        processors.add(new TableThCheckboxAttributeTagProcessor(dialectPrefix));
        processors.add(new TableTdCheckboxAttributeTagProcessor(dialectPrefix));
        processors.add(new TableThSortAttributeTagProcessor(dialectPrefix));
        processors.add(new LiNavItemAttributeTagProcessor(dialectPrefix));
        processors.add(new DivTabPaneAttributeTagProcessor(dialectPrefix));
        processors.add(new PrefixFieldAttributeTagProcessor(dialectPrefix));
        processors.add(new StandardXmlNsTagProcessor(TemplateMode.HTML, dialectPrefix));
        return processors;
    }
}
