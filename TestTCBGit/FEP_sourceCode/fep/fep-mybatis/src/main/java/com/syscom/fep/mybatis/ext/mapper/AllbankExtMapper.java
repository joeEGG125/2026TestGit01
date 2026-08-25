package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.AllbankMapper;
import com.syscom.fep.mybatis.model.Allbank;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;

@Resource
public interface AllbankExtMapper extends AllbankMapper {

    public List<Allbank> queryByPrimaryKey(Allbank oAllBank);

    public List<String> queryAllBankByBkno(Allbank oAllBank);

    public List<Allbank> getALLBANKByPKLike(Allbank oAllBank);

    public List<Allbank> getALLBANKByPKRm(Allbank oAllBank);

    public List<Allbank> getDataTableByPrimaryKey(Allbank oAllBank);

    public List<String> getCountyList();

    public List<Allbank> getALLBANKByAddressLike(String address);

    public Integer updateALLBANKByAddressLike(@Param("flag") String flag, @Param("dbflag")String dbflag,
                                              @Param("address") String address);

    public Integer updateALLBANKByBKNO(Allbank oAllBank);

    public Integer deleteALLBANKByExceptBKNO(String bkno);

    public Integer getALLBANKCnt();

    public Integer getFISCAndBANKCnt();

    /*
    * 2022-01-06
    * */
    public int updateAllbankRMforward();


    /**
     * xy add BtBatchDaily 調用
     */
    int updateALLBANKforBatchDaily();
}
