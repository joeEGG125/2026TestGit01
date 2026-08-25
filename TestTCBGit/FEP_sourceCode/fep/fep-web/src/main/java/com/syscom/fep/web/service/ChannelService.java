package com.syscom.fep.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.mybatis.ext.mapper.ChannelExtMapper;
import com.syscom.fep.mybatis.model.Channel;

@Service
public class ChannelService extends BaseService{

	@Autowired
	private ChannelExtMapper channelExtMapper;
	
	/**
	 * Bruce add 取得通道下拉選單
	 * @return
	 * @throws Exception
	 */
	public List<Channel> queryAllData() throws Exception{
		try {
			return channelExtMapper.queryChannelOptions();
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}
	public int updateChannel(Channel channel) throws Exception{
		int res = -1;
		try {
			res = channelExtMapper.updateByPrimaryKeySelective(channel);
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
		return res;
	}
	/**
	 * Alma add
	 * 取得所有通道資料 order by CHANNEL_CHANNELNO
	 * @return
	 * @throws Exception
	 */
	public List<Channel> queryAllDataAsc() throws Exception{
		try {
			return channelExtMapper.queryChannelByControl();
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}
	
	public List<Channel> queryAllDataByCE() throws Exception{
		try {
			return channelExtMapper.queryChannelByCE();
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}
}
