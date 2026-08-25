package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.MsgfileMapper;
import com.syscom.fep.mybatis.model.Msgfile;
import jakarta.annotation.Resource;

@Resource
public interface MsgfileExtMapper extends MsgfileMapper {
	/**
	 * ADD BY WJ 20210514
	 */
	List<Msgfile> selectByMsgfileErrorcode(@Param("msgfileErrorcode") String msgfileErrorcode);

	String selectShortMsgByATM(@Param("msgfileChannel") Integer msgfileChannel, @Param("msgfileErrorcode") String msgfileErrorcode);
	/**
	 * Bruce add 
	 * @return
	 */
	List<Msgfile> queryMsgFileByDef(@Param("args") Map<String, Object> args);
	
	/**
	 * Bruce add 
	 * @return
	 */
	List<Msgfile> queryMsgFileByDefLike(@Param("args") Map<String, Object> args);
	
	/**
	 * Bruce add
	 * @param msgfile
	 */
	public void updateMsgfile(@Param("msgfile")Msgfile msgfile);

	Msgfile select(@Param("msgfileExternal") String msgfileExternal);
	
	List<Msgfile> queryByDefJoinChannel(@Param("args") Map<String, Object> args);
	
	List<Msgfile> queryByLikeJoinChannel(@Param("args") Map<String, Object> args);
	
}