package com.syscom.fep.scheduler.job.impl;

import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.syscom.fep.scheduler.job.SchedulerJobConfig;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties(prefix = "spring.fep.scheduler.job.reload-db-cache")
// @RefreshScope
public class ReloadDbCacheJobConfig extends SchedulerJobConfig {
	@NotNull
	private String cacheItem;
	
	public String getCacheItem() {
		return cacheItem;
	}

	public void setCacheItem(String cacheItem) {
		this.cacheItem = cacheItem;
	}
}
