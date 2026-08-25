package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import com.syscom.fep.mybatis.mapper.MsgctlMapper;
import com.syscom.fep.mybatis.model.Msgctl;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;

@Resource
public interface MsgctlExtMapper extends MsgctlMapper {

	List<Msgctl> selectAll();


	int selectCountOfMsgctl(String msgctlMsgid);

	Msgctl selectOne(String msgctlMsgid);

	Msgctl select(@Param("msgctlMsgid") String msgctlMsgid, @Param("msgctltxtype1") Short msgctltxtype1, @Param("msgctltxtype2") Short msgctltxtype2);

	List<Msgctl> selectByTxType(@Param("txType1") Short txType1,@Param("txType2") Short txType2);

	/**
	 * 查詢MSGCTL_TXTYPE1跟MSGCTL_TXTYPE2值不等於null資料
	 * @return
	 */
	List<Msgctl> selectAllTxTypeMsgctl();
	
	Msgctl selectByLikeMsgidNotFisc(@Param("msgctlMsgid")String msgctlMsgid);

	String selectMsgName(@Param("msgctlMsgid")String msgctlMsgid);

	int updateMsgctlStatus(Msgctl msgctl);
	
	int updateMsgctlCbsProc(Msgctl msgctl);
}