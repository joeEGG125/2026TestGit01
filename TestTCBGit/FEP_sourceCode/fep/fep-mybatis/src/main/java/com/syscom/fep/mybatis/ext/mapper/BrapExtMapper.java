package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.BrapMapper;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @author xingyun_yang
 * @create 2021/9/15
 */
@Resource
public interface BrapExtMapper extends BrapMapper {

    /**
     * xy add
     * UI019302 引用
     * @param brapStDate
     * @param apId
     * @return
     */
    List<HashMap<String,Object>> getBRAPSumAmtByStDateAPIDKind(@Param("brapStDate") String brapStDate,
                                                                @Param("apId")String apId);

    /**
     * xy add
     * UI019400 引用
     * @param stDate
     * @param pcode
     * @param apId
     * @param txType
     * @param brno
     * @param deptCode
     * @return
     */
    List<HashMap<String,Object>> getBRAPBySTDateForUI(@Param("stDate") String stDate,
                                                      @Param("pcode") String pcode,
                                                      @Param("apId") String apId,
                                                      @Param("txType") String txType,
                                                      @Param("brno") String brno,
                                                      @Param("deptCode") String deptCode,
                                                      @Param("brapCur") String brapCur);

    /**
     * 批次 ProcBRAP 刪除分行清算日結檔BRAP 已存在資料
     *
     * @param beginDate
     * @param beginDate
     */
    public void deleteByDatePCode(@Param("beginDate") String beginDate, @Param("endDate") String endDate);
}
