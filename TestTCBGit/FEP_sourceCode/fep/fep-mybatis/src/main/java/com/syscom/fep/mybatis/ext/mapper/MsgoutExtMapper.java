package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.MsgoutMapper;
import com.syscom.fep.mybatis.model.Msgin;
import com.syscom.fep.mybatis.model.Msgout;
import com.syscom.fep.mybatis.model.Rmout;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Resource
public interface MsgoutExtMapper extends MsgoutMapper {
    /**
     * xy add AA1514引用
     * 查詢MSGOUT by 欄位(TXDATE,SENDER_BANK,FISC_NO)
     * @return 查詢結果筆數
     */
    Msgout getMsgOutForCheckOutData(Msgout oMsgout);

    List<Msgout> getMsgOutByDef(Msgout oMsgout);

    /**
     * zk add
     * 讀取一般通訊匯出檔MSGOUT by MSGOUT_TXDATE,MSGOUT_OWPRIORITY,MSGOUT_STAT
     */
    ArrayList<Msgout> getArrayListForService1411(@Param("msgoutTxdate")String msgoutTxdate , @Param("msgoutOwpriority")String msgoutOwpriority);

    /**
     * cy add
     *  查詢當日最後一筆FISCSNO
     */
    List<Msgout> getmsgoutByMaxFiscsno(@Param("msgoutTxdate")String msgoutTxdate , @Param("msgoutFiscsno")String msgoutFiscsno);

    int updateMSGOUTByFISCNO(Msgout msgout);
    
    List<HashMap<String,Object>> getMSGOUTByDef2(Msgout msgin);
}
