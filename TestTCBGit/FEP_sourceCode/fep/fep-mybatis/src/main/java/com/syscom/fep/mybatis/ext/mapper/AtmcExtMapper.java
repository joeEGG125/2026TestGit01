package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.AtmcMapper;
import com.syscom.fep.mybatis.model.Atmc;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@Resource
public interface AtmcExtMapper extends AtmcMapper {
    /**
     * 2021/04/29
     * <p>
     * ZhaoKai ADD
     */
    Atmc getAtmcByConditions(
            @Param("atmcTbsdy") String atmcTbsdy,
            @Param("atmcTbsdyFisc") String atmcTbsdyFisc,
            @Param("atmcBrnoSt") String atmcBrnoSt,
            @Param("atmcAtmno") String atmcAtmno,
            @Param("atmcCur") String atmcCur,
            @Param("atmcTxCode") String atmcTxCode,
            @Param("atmcDscpt") String atmcDscpt,
            @Param("atmcSelfcd") Short atmcSelfcd);

    /**
     * FEP Web Bruce add ATM(ATMC)收付累計查詢
     *
     * @param args
     * @return
     */
    public List<Atmc> getATMCbyDef(@Param("args") Map<String, Object> args);

    /**
     * 2022/05/23
     * <p>
     * Han ADD
     */
    List<Atmc> GetATMCByAtmnoFiscTbsdySumDRAMT(@Param("atmstatLbsdy") String atmstatLbsdy,
                                               @Param("atmstatTbsdy") String atmstatTbsdy, @Param("feptxnAtmno") String feptxnAtmno);
}