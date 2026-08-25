package com.syscom.fep.web.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.mybatis.ext.mapper.HotbinExtMapper;
import com.syscom.fep.mybatis.model.Hotbin;
import com.syscom.fep.web.form.dbmaintain.UI_070510_Form;

@Service
public class HostService extends BaseService {

	@Autowired
	private HotbinExtMapper hotbinExtMapper;

	private static final String ProgramName = HostService.class.getSimpleName();

	/**
	 * Han add 2022/06/17
	 * 
	 * @param form
	 * @param integer2
	 * @param integer
	 * @return PageInfo<List<Hotbin>>
	 * @throws Exception
	 */
	public PageInfo<List<Hotbin>> getAlarmByPKLike(UI_070510_Form form, int pageNum, int pageSize) throws Exception {
		try {
			// 分頁查詢
			PageInfo<List<Hotbin>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0)
					.doSelectPageInfo(new ISelect() {
						@Override
						public void doSelect() {
							hotbinExtMapper.queryHOTBINByPKLike(form.getTxtBinNo(), form.getTxtBinOrg());
						}
					});
			return pageInfo;
		} catch (Exception e) {
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}

	}

	public int insertHOTBIN(String txtBinNoInsert, String txtBinOrgInsert, String userId) {

		Hotbin hotbin = new Hotbin();

		try {

			hotbin.setBinNo(txtBinNoInsert);
			hotbin.setBinOrg(txtBinOrgInsert);
			hotbin.setUpdateUserid(Integer.parseInt(userId.trim()));
			hotbin.setUpdateTime(new Date());

		} catch (Exception e) {
			sendEMS(e);
		}
		return hotbinExtMapper.insert(hotbin);
	}

	public void deleteHOTBIN(String binNo) {

		hotbinExtMapper.deleteHOTBIN(binNo);
	}

	public Hotbin selectByPrimaryKey(String txtBinNoInsert) {
		return hotbinExtMapper.selectByPrimaryKey(txtBinNoInsert);
	}

}
