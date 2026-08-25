package com.syscom.fep.web.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.mybatis.ext.mapper.MsgctlExtMapper;
import com.syscom.fep.mybatis.ext.mapper.TxtypeExtMapper;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.mybatis.model.Txtype;

@Service
public class MsgctlService extends BaseService {
	@Autowired
	private TxtypeExtMapper txtypeExtMapper;
	@Autowired
	private MsgctlExtMapper msgctlExtMapper;

	
	public List<Txtype> getTxtypeList(){
		return txtypeExtMapper.getAllData();
	}
	
	public PageInfo<HashMap<String, Object>> queryPCodeData(Map<String, Object> argsMap) throws Exception{
		try {
			Integer pageNum = (Integer) argsMap.get("pageNum") == null ? 0 : (Integer) argsMap.get("pageNum");
			Integer pageSize = (Integer) argsMap.get("pageSize") == null ? 0 : (Integer) argsMap.get("pageSize");
			// 分頁查詢
			PageInfo<HashMap<String, Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0)
					.doSelectPageInfo(new ISelect() {
						    @Override
							public void doSelect() {
						    	txtypeExtMapper.getTxtypeForPcode(argsMap);
							}
			});
			return pageInfo;
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}

	public String selectMsgName(String msgctlMsgid){
		return msgctlExtMapper.selectMsgName(msgctlMsgid);
	}

	public int updateMsgtlCbsProc(Msgctl msgctl) {
		return msgctlExtMapper.updateMsgctlCbsProc(msgctl);
	}
}
