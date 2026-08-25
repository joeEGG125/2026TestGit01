package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.CardMapper;
import com.syscom.fep.mybatis.model.Card;
import jakarta.annotation.Resource;

@Resource
public interface CardExtMapper extends CardMapper {
	/**
	 * ZhaoKai add 2021/05/14
	 */
	List<Card> getNcCardByStatus(@Param("cardActno") String cardActno, @Param("cardNcstatus") Short cardNcstatus);

	/**
	 * ZhaoKai add 2021/05/14
	 */
	Card queryNcCardByStatus(@Param("cardActno") String cardActno, @Param("cardNcstatus") Short cardNcstatus);

	/**
	 * ZhaoKai add 2021/05/19
	 */
	Card queryByCreditNoWithMaxCardSeq(@Param("cardCreditno") String cardCreditno);

	/**
	 * ZhaoKai add 2021/05/19
	 */
	Card getSingleCard(@Param("cardCardNo") String cardCardNo);

	/**
	 * ZhaoKai add 2021/05/19
	 */
	int queryStatusNot5678ByActno(@Param("cardActno") String cardActno, @Param("cardCardSeq") Short cardCardSeq);

	/**
	 * ZhaoKai add 2021/05/19
	 */
	Short getMaxCardSeq(@Param("cardActno") String cardActno, @Param("cardType") String cardType);

	/**
	 * ZhaoKai add 2021/05/19
	 */
	Card getOldCardByMaxCardSeq(Card record);

	/**
	 * ZhaoKai add 2021/05/19
	 */
	Card getCardByCreditNoStatus(Card record);

	Card getCardByActno(@Param("cardActno")String cardActno);
}