package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.ChannelMapper;
import com.syscom.fep.mybatis.model.Channel;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;

@Resource
public interface ChannelExtMapper extends ChannelMapper {
	/**
	 * ADD BY WJ 20210514
	 * 
	 * @param channelName
	 * @return
	 */
	List<Channel> selectByChannelName(@Param("channelName") String channelName);
	
	/**
	 * Bruce add 取得通道下拉選單
	 * @return
	 */
	public List<Channel> queryChannelOptions();
	/**
	 * Alma add 取得通道下拉選單排序by channel_channelno
	 * @return
	 */
	public List<Channel> queryChannelByControl();
	
	public List<Channel> queryChannelByCE();
}