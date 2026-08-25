package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.AtmboxMapper;
import com.syscom.fep.mybatis.model.Atmbox;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@Resource
public interface AtmboxExtMapper extends AtmboxMapper {
	/*
	 * 2021/04/30 ZhaoKai Add
	 */
	Atmbox queryByCandidateKey(Atmbox record);

	/*
	 * 2021/04/30 ZhaoKai Add
	 */
	int updateByCandidateKey(Atmbox record);

	/*
	 * 2022/05/24 Han Add
	 */
	List<Map<String,String>> GetATMBoxByAtmnoRwtseqnoSettle3(@Param("ATMBOX_ATMNO")String ATMBOX_ATMNO, @Param("ATMBOX_RWT_SEQNO")String string, @Param("ATMBOX_SETTLE") int ATMBOX_SETTLE);

	/*
	 * 2022/05/24 Han Add
	 */
	List<Map<String,String>> GetATMBoxByAtmnoRwtseqnoSettle2(@Param("ATMBOX_RWT_SEQNO")String substring, @Param("ATMBOX_SETTLE")int ATMBOX_SETTLE);
	
	List<Map<String, Object>> queryATMBOXByDef(Atmbox record);
}