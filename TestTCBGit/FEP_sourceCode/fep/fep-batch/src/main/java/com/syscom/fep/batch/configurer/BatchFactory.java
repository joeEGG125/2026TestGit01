package com.syscom.fep.batch.configurer;

import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.stereotype.Component;

@Component
public class BatchFactory extends AdaptableJobFactory {
	private final AutowireCapableBeanFactory capableBeanFactory = SpringBeanFactoryUtil.getAutowireCapableBeanFactory();

	@Override
	protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
		Object jobInstance = super.createJobInstance(bundle);
		capableBeanFactory.autowireBean(jobInstance);
		return jobInstance;
	}
}