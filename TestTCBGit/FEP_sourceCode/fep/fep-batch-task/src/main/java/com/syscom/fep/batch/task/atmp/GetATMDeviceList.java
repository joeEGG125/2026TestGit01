package com.syscom.fep.batch.task.atmp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import com.google.gson.Gson;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.GWConfig;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.AtmmstrExtMapper;
import com.syscom.fep.mybatis.ext.mapper.AtmstatExtMapper;
import com.syscom.fep.mybatis.ext.mapper.VendorExtMapper;
import com.syscom.fep.mybatis.model.Atmmstr;
import com.syscom.fep.mybatis.model.Atmstat;

/**
 *
 */
public class GetATMDeviceList extends FEPBase implements Task {

    private boolean batchResult = false;
    private String _batchInputPath = StringUtils.EMPTY;
    private String _batchInputFile = StringUtils.EMPTY;
    private String _batchLogPath = StringUtils.EMPTY;

    private BatchJobLibrary job = null;
    private String effDate = "";

    private AtmmstrExtMapper atmmstrExtMapper = SpringBeanFactoryUtil.getBean(AtmmstrExtMapper.class);
    private VendorExtMapper vendorExtMapper = SpringBeanFactoryUtil.getBean(VendorExtMapper.class);
    private AtmstatExtMapper atmstatExtMapper = SpringBeanFactoryUtil.getBean(AtmstatExtMapper.class);


    @Override
    public BatchReturnCode execute(String[] args) {
        try {
            //初始化相關批次物件及拆解傳入參數
            this.initialBatch(args);
            job.writeLog("---------------------------------------------------------------------------------------");
            job.writeLog(ProgramName + "開始!");
            //回報批次平台開始工作
            job.startTask();
//            if(!this.checkConfig()) {
//                job.stopBatch();
//                return;
//            }
            //批次主要處理流程
            batchResult = this.mainProcess();
            //通知批次作業管理系統工作正常結束
            if (batchResult) {
                job.writeLog(ProgramName + "正常結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.endTask();
            } else {
                job.writeLog(ProgramName + "不正常結束，停止此批次作業!!");
                job.writeLog("------------------------------------------------------------------");
                job.abortTask();
            }
            return BatchReturnCode.Succeed;
        } catch (Exception ex) {
            //通知批次作業管理系統工作失敗,暫停後面流程
            try {
                job.abortTask();
                job.writeLog(ProgramName + "失敗!!");
                job.writeLog(ex.toString());
                job.writeLog("------------------------------------------------------------------");
            } catch (Exception e) {
                logContext.setProgramException(e);
                logContext.setProgramName(ProgramName);
                sendEMS(logContext);
            }
            return BatchReturnCode.ProgramException;
        } finally {
            if (job != null) {
                job.writeLog(ProgramName + "結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.dispose();
                job = null;
            }
            if (logContext != null) {
                logContext = null;
            }
        }
    }

    /**
     * 初始化相關批次物件及拆解傳入參數
     *
     * @param args
     */
    private void initialBatch(String[] args) {
        logContext = new LogData();
        logContext.setChannel(FEPChannel.BATCH);
        logContext.setEj(0);
        logContext.setProgramName(ProgramName);
        //檢查Batch Log目錄參數
        _batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();

        if (StringUtils.isBlank(_batchLogPath)) {
            LogHelperFactory.getGeneralLogger().error("Batch Log目錄未設定，請修正");
            return;
        }

        //初始化BatchJob物件,傳入工作執行參數
        job = new BatchJobLibrary(this, args, _batchLogPath);
    }

    /**
     * 檢查是否為營業日
     *
     * @return
     * @throws Exception
     */
//    private boolean checkConfig() throws Exception {
//        //P1階段暫時不檢查
////        if(!job.getArguments().containsKey("FORCERUN")) {
////            if(!job.isBsDay(ATMZone.TWN.toString())) {
////                job.writeLog("非營業日不執行批次");
////                return false;
////            }
////        }
////        else {
////            if("Y".equals(job.getArguments().get("FORCERUN"))) {
////                job.writeLog("參數FORCERUN=Y, 強制執行此批次!");
////            }
////
////        }
//
//        //'最大執行Thread數
////        _batchInputPath = CMNConfig.getInstance().getBatchInputPath();
////        _batchInputFile = CMNConfig.getInstance().getBT010020File();
//
////        if (StringUtils.isBlank(_batchInputPath) || StringUtils.isBlank(_batchInputFile)) {
////            LogHelperFactory.getGeneralLogger().error("參數BatchInputPath未設定");
////            return false;
////        }
//
////        job.writeLog("effDate:" + this.effDate);
//        return true;
//    }

    /**
     * 批次主要處理流程
     *
     * @return
     */
    private boolean mainProcess() throws Exception {
        boolean rtn = true;//是否正常跑完
        try {
            GWConfig config = GWConfig.getInstance();
            String uri = config.getATMMUrl();
            job.writeLog("get uri success" + uri);
            String _atmmUrl = "";
            if (StringUtils.isNotBlank(uri)) {
                _atmmUrl = GWConfig.getInstance().getATMMUrl().trim();
            }
            job.writeLog("get _atmmUrl success");
            String methodname = job.getArguments().get("methodname");
            String _atmNo = job.getArguments().get("ATMNO");

            Gson gson = new Gson();
            Map<String, String> reqmap = new HashMap<>();
            reqmap.put("GroupID", "ALL");
            reqmap.put("Model", "");


            String url = _atmmUrl + "/" + methodname;
            job.writeLog("Begin send to url:" + url);
            String rtnn = "";
            if (StringUtils.isNotBlank(_atmNo)) {
                reqmap.put("MCNo", _atmNo);
            } else {
                reqmap.put("MCNo", "");
            }
            String request = gson.toJson(reqmap);
            job.writeLog("Begin send data:" + request);
            rtnn = sendReceive(url, request);
            job.writeLog("Get ATMM API Response: " + rtnn);
            if (!StringUtils.isNotBlank(rtnn)) {
                return false;
            }
            //Gson gson = new Gson();
            Map map = gson.fromJson(rtnn, Map.class);
            double d_val = Double.valueOf(String.valueOf(map.get("StatusCode")));
            int i_val = (int) d_val;
            String statusCode = String.valueOf(i_val);
            String msg = String.valueOf(map.get("Msg"));
            List<Map> list = (List<Map>) map.get("ResultData");
            job.writeLog("呼叫ATMM API, " + _atmmUrl + methodname + ", statusCode:" + statusCode + ", msg: " + msg);
            if(list!=null)
            job.writeLog("list SIZE: " + list.size());
            //成功
            if ("1".equals(statusCode)) {
                if (list != null && list.size() > 0) {
                    for (Map inmap : list) {
                        String atmBrnoSt = (String) inmap.get("GroupID");//DB長度:4
                        String atmModelno = (String) inmap.get("Model");//DB長度:20
                        String brand = (String) inmap.get("Brand");
                        String atmIp = (String) inmap.get("IPAddress");//DB長度:17
                        String atmNo = (String) inmap.get("MCNo");//DB長度:8
                        String atmstatEnable = (String) inmap.get("cMonitorStatus");// 啟用狀態 ATMSTAT_ENABLE
                        
                        //將超過長度的ATM_ATMNO過濾掉
                        if(atmNo.length() > 8 || atmBrnoSt.length() > 4 || atmModelno.length() > 20) {
                        	job.writeLog("Data length is too long. GroupID(atmBrnoSt):" + atmBrnoSt + ", MCNo(atmNo):" + atmNo + ", Model(atmModelno):" + atmModelno);
                        	continue;
                        }
                        
                        //檢查ip是否正確
                        if(!checkIP(atmIp)) {
                        	job.writeLog("IP information is incorrect. MCNo(atmNo):" + atmNo + "IPAddress(atmIp):" + atmIp);
                        	continue;
                        }
                        
                        String atmStartDate = null; //啟用日期 //DB長度:8
                        String atmCityC = null;  // 所在城市中文名稱 //DB長度:10
                        String atmAddressC = null; // 所在中文地址或位置 //DB長度:50
                        String atmLocation = null; // 裝設地點 //DB長度:14
                        
                        Map<String, Object> infoMap = getDeviceInfo(_atmmUrl, atmBrnoSt, atmNo, atmModelno);
                        if(infoMap != null) {
                        	atmStartDate = String.valueOf(infoMap.get("OpenDate"));
                        	atmCityC = String.valueOf(infoMap.get("City"));
                        	atmAddressC = String.valueOf(infoMap.get("DetailAddress"));
                        	atmLocation = String.valueOf(infoMap.get("cAddress"));
                        	
                        	//判斷為空值，則將值改成 null 避免被 update
                        	if(StringUtils.isBlank(atmStartDate)) {
                    			atmStartDate = null;
                    		} else {
                        		atmStartDate = atmStartDate.trim();
                        		if("null".equalsIgnoreCase(atmStartDate)) {
                        			atmStartDate = null;
                        		}else {
                        			atmStartDate = dateClear(atmStartDate);
                            		if(StringUtils.isBlank(atmStartDate)) {
                            			atmStartDate = null;
                            			job.writeLog("get OpenDate to dateClear is empty or null.");
                            		}else {
                            			//DB長度:8
                            			if(atmStartDate.length() > 8) {
                            				atmStartDate = atmStartDate.substring(0, 8);
                            			}
                            		}
                        		}
                        	}
                        	
                        	//判斷為空值，則將值改成 null 避免被 update
                        	if(StringUtils.isBlank(atmCityC)) {
                        		atmCityC = null;
                    		}else {
                    			//DB長度:10
                    			if(atmCityC.length() > 10) {
                    				atmCityC = atmCityC.substring(0, 10);
                    			}
                    		}
                        	
                        	//判斷為空值，則將值改成 null 避免被 update
                        	if(StringUtils.isBlank(atmAddressC)) {
                        		atmAddressC = null;
                    		}else {
                    			//DB長度:50
                    			if(atmAddressC.length() > 50) {
                    				atmAddressC = atmAddressC.substring(0, 50);;
                    			}
                    		}
                        	
                        	//判斷為空值，則將值改成 null 避免被 update
                        	if(StringUtils.isBlank(atmLocation)) {
                        		atmLocation = null;
                    		}else {
                    			//DB長度:14
                    			if(atmLocation.length() > 14) {
                    				atmLocation = atmLocation.substring(0, 14);
                    			}
                    		}
                        }
                        
                        job.writeLog(" atmBrnoSt:" + atmBrnoSt + " atmModelno:" + atmModelno + " brand:" + brand + " atmIp:" + atmIp + " atmStartDate:" + atmStartDate);
                        job.writeLog(" atmCityC:" + atmCityC + " atmAddressC:" + atmAddressC + " atmLocation:" + atmLocation + " atmstatEnable:" + atmstatEnable);
                        //刪除左側多餘的0
                        //atmBrnoSt = atmBrnoSt.replaceAll("^(0+)", "");

                        String atmVendor = "";
                        if(StringUtils.isNotEmpty(brand) && brand.length() <= 3) {
                            
                            atmVendor = String.valueOf(Integer.parseInt(brand));  //廠牌只取1碼
                            
                        }
                        
                        int cunt = atmmstrExtMapper.updateAtmpByAtmmon(atmBrnoSt, atmModelno, atmVendor, atmIp, atmNo, atmStartDate, atmCityC, atmAddressC, atmLocation);
                        if (cunt == 0) {
                            //insert
                            Atmmstr atmmstr = new Atmmstr();
                            atmmstr.setAtmAtmno(atmNo);
                            atmmstr.setAtmSno("");
                            atmmstr.setAtmBrnoSt(atmBrnoSt);  //帳務分行
                            atmmstr.setAtmBrnoMa(atmBrnoSt);  //管理分行
                            atmmstr.setAtmModelno(atmModelno);  //型別
                            atmmstr.setAtmVendor(atmVendor);    //廠牌
                            atmmstr.setAtmIp(atmIp);

                            atmmstr.setAtmZone("TWN");
                            atmmstr.setAtmAtmpIp("");
                            atmmstr.setAtmCurSt("");
                            atmmstr.setAtmBrnoSt("");
                            atmmstr.setAtmBrnoSt("");
                            atmmstr.setAtmBrnoMa("");
                            atmmstr.setAtmAtmtype((short) 0);
                            atmmstr.setAtmLoc((short) 0);
                            atmmstr.setAtm24Service((short) 0);
                            atmmstr.setAtmGuardCash("");
                            atmmstr.setAtmGuardSecure("");
                            atmmstr.setAtmDeleteFg((short) 0);
                            atmmstr.setAtmChannelType("");
                            atmmstr.setAtmRecording("");
                            if(!checkIP(atmIp)) {
                                atmmstr.setAtmFepConnection((short) 0); //checkIP回傳 false， 是否連接FEP系統 設為0
                            }
                            atmmstr.setAtmFepConnection((short) 1); //checkIP回傳 True， 是否連接FEP系統 設為1
                            atmmstr.setAtmCheckMac((short) 0);
                            atmmstr.setUpdateUserid(0);
                            atmmstr.setUpdateTime(new Date());
                            atmmstr.setAtmBrnoCl("");
                            atmmstr.setAtmCoin((short) 0);
                            atmmstr.setAtmCoinVendor("");
                            atmmstr.setAtmCorp((short) 0);
                            atmmstr.setUserUpdateTime(new Date());
                            atmmstr.setAtmGroupNo("");
                            atmmstr.setAtmLocNo((short) 0);
                            atmmstr.setAtmDistC("");
                            atmmstr.setAtmDistE("");
                            atmmstr.setAtmLongitude(BigDecimal.ZERO);
                            atmmstr.setAtmLatitude(BigDecimal.ZERO);
                            atmmstr.setAtmMtype("");
                            atmmstr.setAtmInsBrno("");
                            atmmstr.setAtmDivide("");
                            atmmstr.setAtmPassbookNo("");
                            atmmstr.setAtmHilifeNo("");
                            atmmstr.setAtmProDate("");
                            atmmstr.setAtmArriveHour("");
                            atmmstr.setAtmKind("");
                            atmmstr.setAtmMarketbdm((short) 0);
                            atmmstr.setAtmEmv((short) 0);
                            atmmstr.setAtmOs("");
                            atmmstr.setAtmStartDate(atmStartDate);

                            atmmstrExtMapper.insert(atmmstr);
                        }
                        //2023-09-08 祥哥需求 Atmstat如果沒有也要insert
                        Atmstat atmstat = atmstatExtMapper.selectByPrimaryKey(atmNo);
                        if (atmstat == null) {
                        	 atmstat = new Atmstat();
                        	 atmstat.setAtmstatAtmno(atmNo);
                        	 atmstat.setAtmstatSocket((short) 3);
                        	 atmstatExtMapper.insertSelective(atmstat);
                        }else {
                        	if(StringUtils.isNotBlank(atmstatEnable)) {
                            	atmstatExtMapper.updateAtmstatEnable(atmNo, atmstatEnable);
                    		}
                        }
                    }
                }
            } else {
                job.writeLog("呼叫ATMM API失敗, 異常代碼:" + statusCode + "異常原因:" + msg);
                rtn = false;

            }

        } catch (Exception ex) {
            job.writeLog(ex.getMessage());
            rtn = false;
            job.abortTask();
        }

        return rtn;
    }

	private Map<String, Object> getDeviceInfo(String _atmmUrl, String atmBrnoSt, String atmNo, String atmModelno){//GroupID, MCNo, Model
		String methodname = "DeviceInfo";
		try {
			Gson gson = new Gson();
			Map<String, String> reqmap = new HashMap<>();
			reqmap.put("GroupID", atmBrnoSt);// (必填) 分行代碼
			reqmap.put("MCNo", atmNo); // (必填) 機號
			reqmap.put("Model", atmModelno); // (必填) 設備機型

			// 任一值為空回傳null
			if (StringUtils.isBlank(atmNo) || StringUtils.isBlank(atmBrnoSt) || StringUtils.isBlank(atmNo)) {
				return null;
			}

			String url = _atmmUrl + "/" + methodname;
			job.writeLog("Begin send to url:" + url);

			String rtnn = "";
			String request = gson.toJson(reqmap);
			job.writeLog("Begin send data:" + request);

			rtnn = sendReceive(url, request);
			job.writeLog("Get ATMM API Response: " + rtnn);

			if (StringUtils.isBlank(rtnn)) {
				return null;
			}

			Map map = gson.fromJson(rtnn, Map.class);
			Double d_val = Double.valueOf(String.valueOf(map.get("StatusCode")));
			String statusCode = String.valueOf(d_val.intValue());
			String msg = String.valueOf(map.get("Msg"));
			
			job.writeLog("get DeviceInfo ResultData to List.");
			
			List<Map> list = (List<Map>) map.get("ResultData");
			
			job.writeLog("呼叫ATMM API, " + _atmmUrl + "/" + methodname + ", statusCode:" + statusCode + ", msg: " + msg);

			if (list != null)
				job.writeLog("list SIZE: " + list.size());

			if ("1".equals(statusCode) && (list != null && list.size() > 0)) {
				return list.get(0);
			}
			
		} catch (Exception ex) {
			job.writeLog(ex.getMessage());
		}
		return null;
    }
    
    private String sendReceive(String url, String data) {
        try {
            HttpClient httpClient = new HttpClient();
            return httpClient.doPost(url, MediaType.APPLICATION_JSON, data);
        } catch (Exception e) {
            job.writeLog(e.getMessage());
            return StringUtils.EMPTY;
        }
    }
    
	/**
	 * 處理取得的日期，清除日期符號，若輸入資料異常回傳null
	 * @param dateStr
	 * @return
	 */
	private String dateClear(String dateStr) {
		if(dateStr == null) {
			return null;
		}
		int length = dateStr.length();
		//判斷是否超過8位數
		if(length <= 8) {
			//判斷是否有日期的符號
			if(dateStr.indexOf("/") == -1 && dateStr.indexOf("-") == -1 ) {
				return dateStr;
			}
		}
		
		String newStr = "";
		String splitStr = "";
		
		//判斷日期的符號為何
		if(dateStr.indexOf("/") > -1) {
			splitStr = "/";
		}else if(dateStr.indexOf("-") > -1) {
			splitStr = "-";
		}
		
		String[] strs = dateStr.split(splitStr);
		
		//判斷拆解的數量是否為3(年、月、日3個)
		if(strs.length != 3) {
			return null;
		}
		//迴圈組成西元日期
		for(String s:strs) {
			//位數小於2要補0
			if(s.length() < 2) {
				s = StringUtils.leftPad(s, 2, "0");
			}
			newStr = newStr.concat(s);
		}
		return newStr;
	}
	
	private boolean checkIP(String ip) {
		if (ip == null)
			return false;
		if ("0.0.0.0".equals(ip.trim()))
			return false;
		String ipRegex = "^([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])"
				+ ".([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])"
				+ ".([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])"
				+ ".([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
		return Pattern.matches(ipRegex, ip);
	}
}
