package com.syscom.fep.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.mybatis.ext.mapper.Inbk2160ExtMapper;
import com.syscom.fep.mybatis.his.ext.mapper.HisInbk2160ExtMapper;
import com.syscom.fep.mybatis.model.Inbk2160;

@Service
public class HceService extends BaseService{

	@Autowired
	private Inbk2160ExtMapper inbk2160ExtMapper;
	@Autowired
	private HisInbk2160ExtMapper hisInbk2160ExtMapper;
	
	public Inbk2160 getINBK2160ByFeptxn(String inbk2160TxDate, Integer inbk2160Ejfno) throws Exception{
		try {
			return inbk2160ExtMapper.getINBK2160ByFeptxn(inbk2160TxDate, inbk2160Ejfno);
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}
	
	public com.syscom.fep.mybatis.his.model.Inbk2160 getHisINBK2160ByFeptxn(String inbk2160OriTxDate, String inbk2160OriStan) throws Exception{
		try {
			return hisInbk2160ExtMapper.getINBK2160ByFeptxn(inbk2160OriTxDate, inbk2160OriStan);
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}
}
