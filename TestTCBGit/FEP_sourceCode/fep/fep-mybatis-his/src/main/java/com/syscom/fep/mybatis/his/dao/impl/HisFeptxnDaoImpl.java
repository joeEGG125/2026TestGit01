package com.syscom.fep.mybatis.his.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.his.dao.HisFeptxnDao;
import com.syscom.fep.mybatis.his.ext.mapper.HisFeptxnExtMapper;
import com.syscom.fep.mybatis.his.ext.mapper.HisFeptxntcbExtMapper;
import com.syscom.fep.mybatis.his.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.his.model.Feptxn;
import com.syscom.fep.mybatis.his.model.Feptxntcb;

@Repository("hisfeptxnDao")
@Lazy
// 合庫合併了FEPTXN檔, 所以這個也不需要了, 單例化
// @Scope("prototype")
public class HisFeptxnDaoImpl implements HisFeptxnDao {
    @Autowired
    private HisFeptxnExtMapper feptxnExtMapper;
    @Autowired
    private HisFeptxntcbExtMapper feptxntcbMapper;

    /**
     * 合庫合併了FEPTXN檔, 所以這裡寫死為空字串
     */
    private final String tableNameSuffix = StringUtils.EMPTY;

    public static String getTableName(String tableNameSuffix) {
        return StringUtils.join("FEPTXN", tableNameSuffix);
    }

    private Feptxn setTableNameSuffix(Feptxn feptxn) {
        if (feptxn != null) {
            if (feptxn instanceof FeptxnExt) {
                ((FeptxnExt) feptxn).setTableNameSuffix(this.tableNameSuffix);
            } else {
                return this.setTableNameSuffix(new FeptxnExt(feptxn));
            }
        }
        return feptxn;
    }

    private List<Feptxn> setTableNameSuffix(List<Feptxn> list) {
        List<Feptxn> resultList = null;
        if (list != null) {
            resultList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                resultList.add(this.setTableNameSuffix(list.get(i)));
            }
        }
        return resultList;
    }

    @Override
    public void setTableNameSuffix(String tableNameSuffix, String invoker) {
        // 合庫合併了FEPTXN檔, 所以這裡不需要再塞入
        // this.tableNameSuffix = tableNameSuffix;
        // LogHelperFactory.getTraceLogger().info("Switch to [FEPTXN", tableNameSuffix, "] by [", invoker, "]");
    }


    /**
     * FEP Web 交易日誌(FEPTXN)查詢
     *
     * @param args
     * @return
     */
    @Override
    public PageInfo<Feptxn> getFeptxn(Map<String, Object> args) {
        Integer pageNum = (Integer) args.get("pageNum");
        Integer pageSize = (Integer) args.get("pageSize");
        pageNum = pageNum == null ? 0 : pageNum;
        pageSize = pageSize == null ? 0 : pageSize;
        // 千萬不要忘記塞入這個
        args.put("tableNameSuffix", this.tableNameSuffix);
        // 分頁查詢
        PageInfo<Feptxn> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                feptxnExtMapper.getFeptxn(args);
            }
        });
        // 這裡最後要再轉一次, 否則無法取到FeptxnExt物件
        pageInfo.setList(this.setTableNameSuffix(pageInfo.getList()));
        return pageInfo;
    }

    /**
     * FEP Web 交易日誌(FEPTXN)查詢
     *
     * @param args
     * @return
     */
    @Override
    public Map<String, Object> getFeptxnSummary(Map<String, Object> args) {
        args.put("tableNameSuffix", this.tableNameSuffix);
        return feptxnExtMapper.getFeptxnSummary(args);
    }

    /**
     * FEP Web 交易日誌(FEPTXN)查詢明細資料
     *
     * @param feptxnTxDate
     * @param feptxnEjfno
     * @return
     */
    @Override
    public Map<String, Object> getFeptxnIntltxn(String feptxnTxDate, Integer feptxnEjfno) {
        Map<String, Object> result = feptxnExtMapper.getFeptxnIntltxn(this.tableNameSuffix, feptxnTxDate, feptxnEjfno);
        return result;
    }

	@Override
	public Feptxntcb selectByPrimaryKeyForFeptxntcb(String feptxnTxDate, Integer feptxnEjfno) throws Exception {
		 return feptxntcbMapper.selectByPrimaryKey(feptxnTxDate, feptxnEjfno);
	}
    
}
