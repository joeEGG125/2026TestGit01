package com.syscom.fep.web.controller.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.syscom.fep.frmcommon.util.FormatUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.mybatis.enc.ext.mapper.EnckeyExtMapper;
import com.syscom.fep.mybatis.enc.model.Enckey;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.common.UI_080150_Form;
import com.syscom.fep.web.util.WebUtil;

@Controller
public class UI_080150Controller extends BaseController {
	@Autowired
	private EnckeyExtMapper enckeyExtMapper;
	
    @Override
    public void pageOnLoad(ModelMap mode) {
    	UI_080150_Form form = new UI_080150_Form();
    	
    	WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }
    
    @PostMapping(value = "/common/UI_080150/upload")
    public String upload(@RequestParam MultipartFile file, @ModelAttribute UI_080150_Form form, ModelMap mode) throws Exception {
    	LogData logData = new LogData();
    	logData.setProgramName(StringUtils.join(ProgramName, ".upload"));
    	if (file.isEmpty()) {
    		logData.setMessage(selectExcelError);
    		logMessage(logData);
            this.showMessage(mode, MessageType.DANGER, selectExcelError);
            return Router.UI_080150.getView();
        }
    	String resultMessage = "";
    	String fileName = file.getOriginalFilename();
	    String today = new SimpleDateFormat("yyyyMMdd").format(new Date()); //系統日期
	    int totalcnt = 0; 	// 總筆數
	    int okcnt = 0;		// 總成功筆數
	    int okcnt_i = 0; 	// 新增成功筆數
	    int okcnt_u = 0; 	// 更新成功筆數
	    int failcnt = 0; 	// 異常筆數
	    int errcnt = 0; 	// 總失敗筆數
	    int errcnt_i = 0; 	// 新增失敗筆數
	    int errcnt_u = 0; 	// 更新失敗筆數
	    InputStream is = null;
	    InputStreamReader isr = null;
	    BufferedReader br = null;
    	try {
    		// 開始讀取資料
    		logData.setMessage("開始處理匯入基碼檔.檔名:" + fileName);
    		logMessage(logData);
    		byte [] byteArr = file.getBytes();
    	    is = new ByteArrayInputStream(byteArr);
    	    isr = new InputStreamReader(is, StandardCharsets.UTF_8);
    	    br = new BufferedReader(isr);
    	    
    	    String line = "";
    	    // 迴圈取值
    	    while ((line = br.readLine()) != null) {
    	    	totalcnt ++;
    	    	if(StringUtils.isBlank(line)){
    	    		logData.setMessage("原始資料第" + totalcnt + "行，原始資料為空值結束處理.");
    	    		logMessage(logData);
    	    		totalcnt--;
    	    		break;
    	    	}
    	    	logData.setMessage("原始資料第" + totalcnt + "行，原始資料：" + line);
        		logMessage(logData);
    	    	String[] data = line.split(",");
    	    	int dataLen = data.length;
    	    	if(dataLen != 11) {
    	    		failcnt++;
    	    		logData.setMessage("原始資料第" + totalcnt + "行,欄位數：" + dataLen);
    	    		logData.setRemark("資料異常 欄位數不符合.");
    	    		logMessage(logData);
    	    		resultMessage += (line+ " | 原始資料第" + totalcnt + "行,欄位數異常(非11個欄位)<br>");
    	    		continue;
    	    	}
    	    	
    	    	// 將檔案資料取出並清除空字串
    	    	String bankid = StringUtils.trim(data[0]);
    	    	String keytype = StringUtils.trim(data[1]);
    	    	String keykind = StringUtils.trim(data[2]);
    	    	String keyfn = StringUtils.trim(data[3]);
    	    	String begindate = StringUtils.trim(data[4]);
    	    	String key = StringUtils.trim(data[5]);
    	    	
    	    	// 建立要新增更/新的Enckey資料
    	    	Enckey enckey = new Enckey();
	    		enckey.setBankid(bankid);
    	    	enckey.setKeytype(keytype);
    	    	enckey.setKeykind(keykind);
    	    	enckey.setKeyfn(keyfn);
    	    	enckey.setBegindate(begindate);
    	    	enckey.setUpdateuser(WebUtil.getFepuser().getFepuserUserid());
    	    	enckey.setUpdatedate(Calendar.getInstance().getTime());
				//系統日>=生效日, 代表key已生效, 為避免生效日設錯未來被換key程式用空白蓋掉curkey,所以pendingkey也一起更新
    	    	if(StringUtils.isBlank(begindate) || today.compareTo(begindate) >= 0) {
    	    		enckey.setCurkey(key);
					enckey.setPendingkey(key);
					enckey.setBegindate("");  //已生效生效日要清空
    	    	}else {
    	    		enckey.setPendingkey(key);  //未生效, 只更新pendingkey
    	    	}
    	    	
    	    	//查詢是否有 Enckey 資料
    	    	Enckey oldEnckey = enckeyExtMapper.selectByPrimaryKey(bankid, keytype, keykind, keyfn);
    	    	if(oldEnckey == null) {
    	    		logData.setMessage("開始新增 Enckey.");
    	    		logMessage(logData);
        	    	int res = enckeyExtMapper.insertSelective(enckey);
        	    	if(res > 0) {
        	    		okcnt_i++;
        	    		resultMessage += ("bankid:" + bankid + ", keytype:" + keytype + ", keykind:" + keykind + ", keyfn:" + keyfn + ". 新增 Enckey 成功<br>");
        	    		logData.setMessage("新增 Enckey 成功.");
        	    		logMessage(logData);
        	    	}else {
        	    		errcnt_i++;
        	    		resultMessage += ("bankid:" + bankid + ", keytype:" + keytype + ", keykind:" + keykind + ", keyfn:" + keyfn + ". 新增 Enckey 失敗<br>");
        	    		logData.setMessage("新增 Enckey 失敗.");
        	    		logMessage(logData);
        	    	}
    	    	}else {
    	    		logData.setMessage("開始更新 Enckey.");
    	    		logMessage(logData);
        	    	int res = enckeyExtMapper.updateByPrimaryKeySelective(enckey);
        	    	if(res > 0) {
        	    		okcnt_u++;
        	    		resultMessage += ("bankid:" + bankid + ", keytype:" + keytype + ", keykind:" + keykind + ", keyfn:" + keyfn + ". 更新 Enckey 成功<br>");
        	    		logData.setMessage("更新 Enckey 成功.");
        	    		logMessage(logData);
        	    	}else {
        	    		errcnt_u++;
        	    		resultMessage += ("bankid:" + bankid + ", keytype:" + keytype + ", keykind:" + keykind + ", keyfn:" + keyfn + ". 更新 Enckey 失敗<br>");
        	    		logData.setMessage("更新 Enckey 失敗.");
        	    		logMessage(logData);
        	    	}
    	    	}
    	    }
    	    
    	    //計算總成功筆數
    	    okcnt = okcnt_i + okcnt_u;
    	    //計算總失敗筆數
    	    errcnt = errcnt_i + errcnt_u;
    	    if(failcnt == 0) {
    	    	String re = FormatUtil.messageFormat(enckeyDetailedSuccess, totalcnt, okcnt, okcnt_i, okcnt_u, errcnt, errcnt_i, errcnt_u);
    	    	logData.setMessage(re);
    	    	logMessage(logData);
    	    	resultMessage = "本次匯入基碼檔處理結果：<br>" + re + "<br>" + resultMessage;
    	    	//回應前端處理結果訊息
    	    	this.showMessageWithArgs(mode, MessageType.SUCCESS, enckeyDetailedSuccess, totalcnt, okcnt, okcnt_i, okcnt_u, errcnt, errcnt_i, errcnt_u);
    	    }else {
    	    	String re = FormatUtil.messageFormat(enckeyDetailedSuccessErr, totalcnt, okcnt, okcnt_i, okcnt_u, errcnt, errcnt_i, errcnt_u, failcnt);
    	    	logData.setMessage(re);
    	    	logMessage(logData);
    	    	resultMessage = "本次匯入基碼檔處理結果：<br>" + re + "<br>" + resultMessage;
    	    	//回應前端處理結果訊息
    	    	this.showMessageWithArgs(mode, MessageType.SUCCESS, enckeyDetailedSuccessErr, totalcnt, okcnt, okcnt_i, okcnt_u, errcnt, errcnt_i, errcnt_u, failcnt);
    	    }
    	    form.setResultMessage(resultMessage);
    	    WebUtil.putInAttribute(mode, AttributeName.Form, form);
    	} catch (Exception e) {
    		this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
            return Router.UI_080150.getView();
        }finally{
        	if(br != null) {
        		br.close();
        	}
        	if(isr != null) {
        	    isr.close();
        	}
        	if(is != null) {
        		is.close();
        	}
        }
    	
    	return Router.UI_080150.getView();
    }
    
}
