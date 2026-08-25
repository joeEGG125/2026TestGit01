package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.InbkpendMapper;
import com.syscom.fep.mybatis.model.Inbkpend;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author xingyun_yang
 * @create 2021/8/13
 */
@Resource
public interface InbkpendExtMapper extends InbkpendMapper {
	List<Inbkpend> getINBKPend2270(@Param("inbkpendOriTbsdyFisc") String ORI_TBSDY,
								   @Param("inbkpendTxDate")String TX_DATE,
								   @Param("inbkpendBkno")String BKNO,
								   @Param("inbkpendStan")String STAN,
								   @Param("inbkpendOriPcode")String OPCODE,
								   @Param("inbkpendOriStan")String OSTAN,
								   @Param("inbkpendOriBkno")String SYSSTATHBKNO,
								   @Param("sqlSortExpression") String sqlSortExpression);

	List<Map<String, Object>> getINBKPend2270csv(@Param("inbkpendOriTbsdyFisc") String ORI_TBSDY,
								   @Param("inbkpendTxDate")String TX_DATE,
								   @Param("inbkpendBkno")String BKNO,
								   @Param("inbkpendStan")String STAN,
								   @Param("inbkpendOriPcode")String OPCODE,
								   @Param("inbkpendOriStan")String OSTAN,
								   @Param("inbkpendOriBkno")String SYSSTATHBKNO,
								   @Param("sqlSortExpression") String sqlSortExpression);


	/**
     * xingyun add
     */
   List<Inbkpend> getINBKPendList(@Param("datetime") String datetime,
                                      @Param("inbkpendPcode")String inbkpendPcode
    );

    /**
     * 2021/08/20
     * ChenYu ADD
     */
    List<Inbkpend> getOriDataByPcodes(Inbkpend inbkpend);

    Inbkpend getpendingDateStanBkno(Inbkpend inbkpend);

    /**
     * Han add 2022/0/7/29
     * @param iNBKPEND_TX_DATE
     * @param iNBKPEND_PCODE
     * @param
     * @return
     */
	List<Map<String, Object>> getTodayFinishTradeData(
			@Param("iNBKPEND_TX_DATE")String iNBKPEND_TX_DATE, 
			@Param("iNBKPEND_PCODE")String iNBKPEND_PCODE,
			@Param("SYSSTAT_FBKNO")String SYSSTAT_FBKNO,
			@Param("oriBkno")String oriBkno,
			@Param("oriStan")String oriStan);

	/**
     * Han add 2022/0/7/29
     * @param INBKPEND_PCODE 
     * @param INBKPEND_ORI_BKNO
     * @param INBKPEND_ORI_STAN
     * @return
     */
	List<Map<String, Object>> getIsSendTrade(
			@Param("iNBKPEND_TX_DATE")String iNBKPEND_TX_DATE, 
			@Param("INBKPEND_PCODE")String INBKPEND_PCODE, 
			@Param("INBKPEND_ORI_BKNO")String INBKPEND_ORI_BKNO, 
			@Param("INBKPEND_ORI_STAN")String INBKPEND_ORI_STAN);
}
