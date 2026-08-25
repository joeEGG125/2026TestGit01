package com.syscom.fep.frmcommon.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

@Component
public class StringValueResolverUtil implements BeanFactoryAware {

    @Nullable
    private static StringValueResolver embeddedValueResolver;

    private StringValueResolverUtil() {
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (embeddedValueResolver == null)
            if (beanFactory instanceof ConfigurableBeanFactory) {
                embeddedValueResolver = new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory);
            }
    }

    public static String resolve(String value) {
        return (embeddedValueResolver != null ? embeddedValueResolver.resolveStringValue(value) : value);
    }
}
