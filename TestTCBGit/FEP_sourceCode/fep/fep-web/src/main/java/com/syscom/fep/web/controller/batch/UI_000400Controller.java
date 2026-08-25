package com.syscom.fep.web.controller.batch;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.batch.base.enums.BatchResult;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Subsys;
import com.syscom.fep.mybatis.util.DB2Util;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SelectOption;
import com.syscom.fep.web.form.batch.UI_000400_Form;
import com.syscom.fep.web.service.BatchService;
import com.syscom.fep.web.util.WebUtil;

@Controller
public class UI_000400Controller extends BaseController {

    @Autowired
    private BatchService batchService;

    private final String pleaseChoose = "所有";        //批次名稱、系統別： 下拉選單的預設值
    private final String yyyyMMdd = "yyyy-MM-dd";    //批次啟動日期格式

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_000400_Form form = new UI_000400_Form();
        form.setUrl("/batch/UI_000400/queryClick");
        DateFormat dateTimeformat = new SimpleDateFormat(this.yyyyMMdd);
        form.setBatchStartDate(dateTimeformat.format(new Date()));
//        this.setBatchNameOptions(mode);
//        this.queryClick(form, mode);
        WebUtil.putInAttribute(mode,AttributeName.Form,form);
    }

    @PostMapping(value = "/batch/UI_000400/queryClick")
    private String queryClick(@ModelAttribute UI_000400_Form form, ModelMap mode) {
        bindGridData(form, mode);
        return Router.UI_000400.getView();
    }

    /**
     * 資料整理 依查詢條件查詢主程式
     */
    private void bindGridData(UI_000400_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        FileInputStream fis = null;
        try {
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper
                    .startPage(form.getPageNum(), form.getPageSize(), form.getPageNum() > 0 && form.getPageSize() > 0)
                    .doSelectPageInfo(new ISelect() {
                        @Override
                        public void doSelect() {
                            batchService.getTWSLOG(form.getTwsTaskname(), form.getBatchStartDate());
                        }
                    });
            if (pageInfo.getSize() == 0) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            PageData<UI_000400_Form, HashMap<String, Object>> pageData = new PageData<>(pageInfo, form);

            List<HashMap<String, Object>> dtlist = pageData.getList();
            List<HashMap<String, Object>> datalist = new ArrayList<>(pageData.getList().size());
            int tempVar = dtlist.size();
            for (int i = 0; i < tempVar; i++) {
                HashMap<String, Object> hashMap = new HashMap<>();
                //批次名稱(Map取值須先檢查是否為null)
                //批次名稱
                if (dtlist.get(i).get("TWS_TASKNAME") != null) {
                    hashMap.put("TWS_TASKNAME", dtlist.get(i).get("TWS_TASKNAME").toString());
                } else {
                    hashMap.put("TWS_TASKNAME", "");
                }
                //執行時間
                if (dtlist.get(i).get("TWS_DATETIME") != null) {
                    hashMap.put("TWS_DATETIME", dtlist.get(i).get("TWS_DATETIME").toString());
                } else {
                    hashMap.put("TWS_DATETIME", "");
                }
                //主機名稱
                if (dtlist.get(i).get("TWS_HOSTNAME") != null) {
                    hashMap.put("TWS_HOSTNAME", dtlist.get(i).get("TWS_HOSTNAME").toString());
                } else {
                    hashMap.put("TWS_HOSTNAME", "");
                }
                //執行秒數
				if (dtlist.get(i).get("TWS_DURATION") != null) {
					int millisecond = Integer.parseInt(dtlist.get(i).get("TWS_DURATION").toString());
					BigDecimal second = new BigDecimal(millisecond).divide(new BigDecimal(1000));
					hashMap.put("TWS_DURATION", new DecimalFormat("#0.000").format(second));
                } else {
                    hashMap.put("TWS_DURATION", "");
                }
                //執行結果
                if (dtlist.get(i).get("TWS_RESULT") != null) {
					BatchResult batchResult = BatchResult.parse(dtlist.get(i).get("TWS_RESULT").toString());
					switch (batchResult) {
					case Successful:
						hashMap.put("TWS_RESULT", "執行成功");
						break;
					case Failed:
						hashMap.put("TWS_RESULT", "執行失敗");
						break;
					case Running:
						hashMap.put("TWS_RESULT", "執行中");
						break;
					case PartialFailed:
						hashMap.put("TWS_RESULT", "部分執行失敗");
						break;
					default:
						hashMap.put("TWS_RESULT", "");
						break;
					}
                } else {
                    hashMap.put("TWS_RESULT", "");
                }
                //執行程式
                if (dtlist.get(i).get("TWS_JARFILE") != null) {
                    hashMap.put("TWS_JARFILE", dtlist.get(i).get("TWS_JARFILE").toString());
                } else {
                    hashMap.put("TWS_JARFILE", "");
                }
                //LOG檔案內容(處理邏輯：db的欄位HISTORY_LOGFILECONTENT如有值，則放此內容，若無則放HISTORY_LOGFILE欄位內容中所指定的檔案內容，再無則為空。
                String historyLogfilecontent = "";
				if (dtlist.get(i).get("TWS_LOGFILECONTENT") != null) {
					historyLogfilecontent = DB2Util.getClobValue(dtlist.get(i).get("TWS_LOGFILECONTENT"),
							StringUtils.EMPTY);
					historyLogfilecontent = historyLogfilecontent.replaceAll("\\r\\n", "\r\n<br />");
				} else {
                    historyLogfilecontent =StringUtils.EMPTY;
                }
                hashMap.put("TWS_LOGFILECONTENT", historyLogfilecontent);
                if (datalist != null) {
                    datalist.add(hashMap);
                }
            }
            if (datalist == null || datalist.size() == 0) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            } else {
                pageData.setList(datalist);
            }
            this.setBatchNameOptions(mode);
            this.setSubsysOptions(mode);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    /**
     * 設定[批次名稱]下拉選單內容
     *
     * @param mode
     */
    private void setBatchNameOptions(ModelMap mode) {
        try {
            List<Batch> batchList = batchService.getBatchAll();
            List<SelectOption<String>> selectOptionList = new ArrayList<SelectOption<String>>();
            selectOptionList.add(new SelectOption<String>(this.pleaseChoose, StringUtils.EMPTY));
            for (int i = 0; i < batchList.size(); i++) {
                selectOptionList.add(
                        new SelectOption<String>(batchList.get(i).getBatchName(), batchList.get(i).getBatchName()));
            }
            WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    /**
     * 設定[系統別]下拉選單內容
     *
     * @param mode
     */
    private void setSubsysOptions(ModelMap mode) {
        try {
            List<Subsys> subsysList = batchService.getSubsysAll();
            List<SelectOption<String>> selectOptionList = new ArrayList<SelectOption<String>>();
            selectOptionList.add(new SelectOption<String>(this.pleaseChoose, StringUtils.EMPTY));
            for (int i = 0; i < subsysList.size(); i++) {
                selectOptionList.add(new SelectOption<String>(subsysList.get(i).getSubsysNameS(),
                        subsysList.get(i).getSubsysSubsysno().toString()));
            }
            WebUtil.putInAttribute(mode, AttributeName.Options2, selectOptionList);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }
}
