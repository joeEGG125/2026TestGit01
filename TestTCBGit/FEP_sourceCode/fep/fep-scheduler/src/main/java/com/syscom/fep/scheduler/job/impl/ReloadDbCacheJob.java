package com.syscom.fep.scheduler.job.impl;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;

import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.scheduler.job.SchedulerJob;

/**
 * 定時緩存DB資料
 * 
 * @author Richard
 *
 */
public class ReloadDbCacheJob extends SchedulerJob<ReloadDbCacheJobConfig> {
	/**
	 * 執行任務
	 *
	 * @param context
	 * @param config
	 */
	@Override
	protected void executeJob(JobExecutionContext context, ReloadDbCacheJobConfig config) throws Exception {
		String[] cacheItems = null;
		String cacheItem = config.getCacheItem();
		if (cacheItem.contains(",")) {
			cacheItems = StringUtils.split(cacheItem, ",");
		} else {
			cacheItems = new String[] { cacheItem };
		}
		// 如果有設置ALL, 則直接reload全部
		String foundItemAll = Arrays.stream(cacheItems).filter(t -> CacheItem.ALL.name().equalsIgnoreCase(t)).findFirst().orElse(null);
		if (StringUtils.isNotBlank(foundItemAll)) {
			this.reloadCache(CacheItem.ALL);
		} else {
			HashSet<String> avoidDuplicateCacheItemSet = new HashSet<>();
			for (String item : cacheItems) {
				if (avoidDuplicateCacheItemSet.contains(item)) {
					continue;
				}
				avoidDuplicateCacheItemSet.add(item);
				this.reloadCache(CacheItem.parse(item));
			}
		}
	}

	private void reloadCache(CacheItem cacheItem) {
		try {
			FEPCache.reloadCache(cacheItem);
		} catch (Exception e) {
			ScheduleLogger.warn(e, e.getMessage());
		}
	}
}
