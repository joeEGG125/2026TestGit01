package com.syscom.fep.base.configurer;

import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;

@Configuration
@ConfigurationProperties(prefix = "spring.register")
// @RefreshScope
public class RegisterConfig {

	private List<String> bean;
	private List<String> controller;

	public void setBean(List<String> bean) {
		this.bean = bean;
	}

	public void setController(List<String> controller) {
		this.controller = controller;
	}

	@PostConstruct
	public void register() {
		if (CollectionUtils.isNotEmpty(bean)) {
			for (String beanClassName : bean) {
				SpringBeanFactoryUtil.registerBean(beanClassName);
			}
		}
		if (CollectionUtils.isNotEmpty(controller)) {
			for (String controllerClassName : controller) {
				SpringBeanFactoryUtil.registerController(controllerClassName);
			}
		}
	}

	@PreDestroy
	public void unregister(){
		unregisterController();
		unregisterBean();
	}
	
	public void unregisterController() {
		if (CollectionUtils.isNotEmpty(controller)) {
			for (String controllerClassName : controller) {
				SpringBeanFactoryUtil.unregisterController(controllerClassName);
			}
		}
	}
	
	public void unregisterBean() {
		if (CollectionUtils.isNotEmpty(bean)) {
			for (String beanClassName : bean) {
				SpringBeanFactoryUtil.unregisterBean(beanClassName);
			}
		}
	}
}
