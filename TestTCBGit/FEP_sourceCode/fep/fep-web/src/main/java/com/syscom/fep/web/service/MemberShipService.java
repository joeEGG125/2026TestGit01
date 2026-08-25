package com.syscom.fep.web.service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.mapper.FepgroupExtMapper;
import com.syscom.fep.mybatis.ext.mapper.FepuserExtMapper;
import com.syscom.fep.mybatis.ext.mapper.ProgramExtMapper;
import com.syscom.fep.mybatis.ext.mapper.PrortExtMapper;
import com.syscom.fep.mybatis.model.Fepgroup;
import com.syscom.fep.mybatis.model.Fepuser;

@Service
public class MemberShipService extends BaseService {

	@Autowired
	private FepuserExtMapper fepuserExtMapper;

	@Autowired
	private FepgroupExtMapper fepgroupExtMapper;
	
	@Autowired
	private PrortExtMapper prortExtMapper; //Bruce Add
	
	@Autowired
	private ProgramExtMapper programExtMapper;//Bruce add
	
	/**
	 * Bruce add 使用者資料查詢 權限群組下拉選單
	 * @return
	 * @throws Exception 
	 */
	public List<Fepgroup> getDistinctGroupIdName() throws Exception{
		try {
			return fepgroupExtMapper.getDistinctGroupIDName();
		}catch(Exception ex) {
			sendEMS(ex);
			throw ExceptionUtil.createException(ex, this.getInnerMessage(ex));
		}		
	}

	/**
	 * 取得FEPUser資料。 透過SAFE DAL處理
	 */
	public Boolean getUserInfo(Fepuser defFepuser, String errMsg) {
		try {
			// 2010-05-28 modified by Daniel for 人員輸入櫃員代號
			// 2010-06-21 modified by Daniel for 人員輸入LOGONID
			List<Fepuser> list = fepuserExtMapper.getFEPUSERByDef(defFepuser);
			if (list.size()>0) {
				defFepuser = list.get(0);
				// update LSUSER 最後登錄日及時間
				defFepuser.setFepuserLuDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
				defFepuser.setFepuserLuTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
				if (fepuserExtMapper.updateByPrimaryKeySelective(defFepuser) < 0) {
//					errMsg = "更新最後登錄日及時間失敗!!";
					return false;
				}
//				else {
//					errMsg = "ok";
//				}

			} else {
//				errMsg = "此使用者帳號不存在FEP系統!!";
				return false;
			}
			return true;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(ex);
//			errMsg = "系統異常，作業失敗!!!" + ex.getMessage();
			return false;
		}
	}

	public List<Fepgroup> getGroupData() {
		return fepgroupExtMapper.queryAllData("");
	}
	
	/**
	 * 群組與功能查詢 查詢
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public PageInfo<Fepgroup> queryFepGroupByPkLike(Map<String, Object> args) throws Exception {
		try {
			Integer pageNum = (Integer) args.get("pageNum") == null ? 0 : (Integer) args.get("pageNum");
			Integer pageSize = (Integer) args.get("pageSize") == null ? 0 : (Integer) args.get("pageSize");
			// 分頁查詢
			PageInfo<Fepgroup> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0)
					.doSelectPageInfo(new ISelect() {
						    @Override
							public void doSelect() {
						    	fepgroupExtMapper.getFepGroupByPkLike(args);
							}
			});
			return pageInfo;
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}
	
	/**
	 * 已授權程式
	 * @return
	 * @throws Exception 
	 */
	public List<Map<String, Object>> queryPrortByGroupIdLike(String fepgroupGroupId) throws Exception{		 
		try {
			return prortExtMapper.getPrortByGroupIdLike(fepgroupGroupId);
		}catch(Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}		
	}
	
	/**
	 * 已授權及未授權程式
	 * @return
	 * @throws Exception 
	 */
	public List<Map<String, Object>> queryProgramf() throws Exception {
		try {
			return programExtMapper.getProgramf();
		}catch(Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}		
	}
}
