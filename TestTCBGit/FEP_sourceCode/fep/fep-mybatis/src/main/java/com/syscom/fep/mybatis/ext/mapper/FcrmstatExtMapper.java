package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.FcrmstatMapper;
import com.syscom.fep.mybatis.model.Fcrmstat;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;

@Resource
public interface FcrmstatExtMapper extends FcrmstatMapper {
    /*
     * zk add 2021/10/13
     * */
    int updateAOCTRMByCUR(@Param("cur") String cur);

    /*
     * zk add 2021/10/13
     * */
    int updateAOCTRM1AndRM4ByCUR(@Param("cur") String cur,@Param("val") String val);

    public List<Fcrmstat> queryByPrimaryKey(Fcrmstat fcrmstat);

}
