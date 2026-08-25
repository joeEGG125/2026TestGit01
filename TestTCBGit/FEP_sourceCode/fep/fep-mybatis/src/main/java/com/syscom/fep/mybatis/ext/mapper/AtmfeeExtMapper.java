package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.AtmfeeMapper;
import com.syscom.fep.mybatis.model.Atmfee;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;

@Resource
public interface AtmfeeExtMapper extends AtmfeeMapper {

    List<Atmfee> selectAllAtmfee();

    List<Atmfee> selectByPKLike(@Param("fiscFlag") String fiscFlag, @Param("ddlSEQ_NO") String ddlSEQ_NO, @Param("tbxTX_MM") String tbxTX_MM);

}
