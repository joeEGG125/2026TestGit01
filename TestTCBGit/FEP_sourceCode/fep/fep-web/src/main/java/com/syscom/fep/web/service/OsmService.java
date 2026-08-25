package com.syscom.fep.web.service;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.mybatis.ext.mapper.AtmfeeExtMapper;
import com.syscom.fep.mybatis.model.Atmfee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OsmService extends BaseService {
	
	@Autowired
	private AtmfeeExtMapper atmfeeMapper;//Bruce add
	
	public Atmfee selectByPrimaryKey(String atmfeeTxMm,String atmfeeSeqNo) throws Exception {
		try {
			return atmfeeMapper.selectByPrimaryKey(atmfeeTxMm, atmfeeSeqNo);
		}catch(Exception ex) {
			sendEMS(ex);
			throw ExceptionUtil.createException(ex, this.getInnerMessage(ex));
		}	
	}
	
	public int deleteByPrimaryKey(Atmfee atmfee) throws Exception {
		try {
			return atmfeeMapper.deleteByPrimaryKey(atmfee);
		}catch(Exception ex) {
			sendEMS(ex);
			throw ExceptionUtil.createException(ex, this.getInnerMessage(ex));
		}	
	}	
	
	/**
	 * add 取得下拉選單
	 * @return
	 */
	public List<Atmfee> selectAllAtmfee(){
		return atmfeeMapper.selectAllAtmfee();
	}
	
	public PageInfo<Atmfee> selectByPKLike(String fiscFlag, String ddlSEQ_NO, String tbxTX_MM, Integer pageNum, Integer pageSize) throws Exception{
		PageInfo<Atmfee> pageInfo = null;
		try {
			pageNum = pageNum == null ? 0 : pageNum;
			pageSize = pageSize == null ? 0 : pageSize;
			// 分頁查詢
			pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
				@Override
				public void doSelect() {
					atmfeeMapper.selectByPKLike(fiscFlag, ddlSEQ_NO, tbxTX_MM);
				}
			});
			return pageInfo;
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}
	
	public Integer insertSelective(Atmfee atmfee) {
		try {
			return atmfeeMapper.insertSelective(atmfee);
		}catch (Exception ex) {
			getLogContext().setProgramException(ex);
			return 0;
		}
	}
	
	public Integer updateByPrimaryKeySelective(Atmfee atmfee) {
		try {
			return atmfeeMapper.updateByPrimaryKeySelective(atmfee);
		}catch (Exception ex) {
			getLogContext().setProgramException(ex);
			return 0;
		}
	}

}
