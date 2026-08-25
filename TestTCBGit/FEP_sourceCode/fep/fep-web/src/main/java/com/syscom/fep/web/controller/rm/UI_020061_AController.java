package com.syscom.fep.web.controller.rm;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.esapi.ESAPIUtil;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.mybatis.model.Allbank;
import com.syscom.fep.mybatis.model.Rminsno;
import com.syscom.fep.mybatis.model.Rmoutsno;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.rm.RM;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_020061_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 往來行庫資料維護
 *
 * @author xingyun_yang
 * @create 2021/11/23
 */
@Controller
public class UI_020061_AController extends BaseController {
    @Autowired
    RmService rmService;

    //路徑暫時這樣寫，可隨時改
    String rMFileUploadPath = "UI_020061_A/";
    String rMFileUploadLogPath = "UI_020061_A/Log/";
    String exceptionMsg = "UI_020061_A, 往來行庫資料轉檔發生例外";

    //add by maxine on 2011/06/24 for SYSSTAT自行查
    private String _SYSSTAT_HBKNO;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_020061_Form form = new UI_020061_Form();
        querySYSSTAT(mode);
        this.showMessage(mode, MessageType.DANGER, "注意!!轉檔作業請在匯款未CHECKIN狀態時執行");
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    private void querySYSSTAT(ModelMap mode) {
        try {
            List<Sysstat> _dtSYSSTAT = rmService.getStatus();
            if (_dtSYSSTAT.size() < 1) {
                this.showMessage(mode, MessageType.DANGER, "注意!!轉檔作業請在匯款未CHECKIN狀態時執行");
                return;
            }
            _SYSSTAT_HBKNO = _dtSYSSTAT.get(0).getSysstatHbkno();
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    @PostMapping(value = "/rm/UI_020061_A/index")
    private String index(@ModelAttribute UI_020061_Form form, ModelMap mode) {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        querySYSSTAT(mode);
        this.showMessage(mode, MessageType.DANGER, "注意!!轉檔作業請在匯款未CHECKIN狀態時執行");
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        return Router.UI_020061_A.getView();
    }

    @PostMapping(value = "/rm/UI_020061_A/btnExecute")
    private String btnExecute(@ModelAttribute UI_020061_Form form, ModelMap mode, HttpServletRequest request) {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        //在根目錄下創建檔案
        File path = new File(CleanPathUtil.cleanString(""));// 參數為空 獲取項目根目錄
        String startPath = "";
        try {
            startPath = path.getCanonicalPath();
        } catch (IOException e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
            return Router.UI_020061_A.getView();
        }
        File uI_020061_A = new File(CleanPathUtil.cleanString(startPath + "/" + "UI_020061_A"));
        if (!uI_020061_A.exists()) {
            if (uI_020061_A.mkdirs()) {
                LogHelperFactory.getTraceLogger().warn(uI_020061_A.getPath(), " mkdirs failed!!!");
            }
        }
        File log = new File(CleanPathUtil.cleanString(startPath + "/" + "UI_020061_A/Log"));
        if (!log.exists()) {
            if (log.mkdirs()) {
                LogHelperFactory.getTraceLogger().warn(log.getPath(), " mkdirs failed!!!");
            }
        }

        String serverLogPath = rMFileUploadLogPath + FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN) + "/";
        File dir = new File(CleanPathUtil.cleanString(serverLogPath));
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("fileName");
        String upLoadFileName = files.get(0).getOriginalFilename().substring(files.get(0).getOriginalFilename().lastIndexOf("/") + 1);
        String upLoadFileName2 = files.get(1).getOriginalFilename().substring(files.get(1).getOriginalFilename().lastIndexOf("/") + 1);
        BufferedWriter sw = null;
        if (files.isEmpty()) {
            this.showMessage(mode, MessageType.WARNING, "找不到上傳檔案,請稍後再試");
            return Router.UI_020061_A.getView();
        }
        try {
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    LogHelperFactory.getTraceLogger().warn(dir.getPath(), " mkdirs failed!!!");
                }
            }
            this.showMessage(mode, MessageType.INFO, "轉檔中");
            sw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(CleanPathUtil.cleanString(serverLogPath + "UI_020061_" + FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN) + ".txt"))), "big5"));
            writeSw(sw, "#.............開始上傳...........");
            writeSw(sw, "#上傳檔案:上傳" + upLoadFileName);

            try {
                // 參數為空 獲取項目根目錄
                File directory = new File(CleanPathUtil.cleanString(""));
                String courseFile = "";
                courseFile = directory.getCanonicalPath();
                // 2024-11-06 Richard modified for 【Relative Path Traversal】
                // File file = new File(CleanPathUtil.cleanString(courseFile + "/" + rMFileUploadPath + upLoadFileName));
                Object file = ESAPIUtil.toFile(CleanPathUtil.cleanString(courseFile + "/" + rMFileUploadPath + upLoadFileName));
                if (!((File) file).getParentFile().exists()) {
                    if (((File) file).getParentFile().mkdirs()) {
                        LogHelperFactory.getTraceLogger().warn(((File) file).getParentFile().getPath(), " mkdirs failed!!!");
                    }
                }
                files.get(0).transferTo((File) file);
                writeSw(sw, "#.............上傳'財金file'成功...........");
            } catch (Exception ex) {
                writeSw(sw, "#.............上傳'財金file'失敗...........");
                this.showMessage(mode, MessageType.INFO, "上傳'財金file'失敗");
                logContext.setRemark("上傳'財金file'失敗., ex=" + ex.toString());
                rmService.logMessage(logContext, Level.INFO);
                sw.close();
                return Router.UI_020061_A.getView();
            }
            writeSw(sw, "#上傳檔案:上傳" + upLoadFileName2);
            try {
                // 參數為空 獲取項目根目錄
                File directory = new File(CleanPathUtil.cleanString(""));
                String courseFile = "";
                courseFile = directory.getCanonicalPath();
                // 2024-11-06 Richard modified for 【Relative Path Traversal】
                // File file = new File(CleanPathUtil.cleanString(courseFile + "/" + rMFileUploadPath + upLoadFileName2));
                Object file = ESAPIUtil.toFile(CleanPathUtil.cleanString(courseFile + "/" + rMFileUploadPath + upLoadFileName2));
                if (!((File) file).getParentFile().exists()) {
                    if (((File) file).getParentFile().mkdir()) {
                        LogHelperFactory.getTraceLogger().warn(((File) file).getParentFile().getPath(), " mkdirs failed!!!");
                    }
                }
                files.get(1).transferTo((File) file);
                writeSw(sw, "#.............上傳'轉參加證券匯款銀行代號檔'成功...........");
            } catch (Exception ex) {
                writeSw(sw, "##.............上傳'轉參加證券匯款銀行代號檔'失敗...........");
                this.showMessage(mode, MessageType.INFO, "上傳'轉參加證券匯款銀行代號檔'失敗");
                logContext.setRemark("上傳'轉參加證券匯款銀行代號檔'失敗., ex=" + ex.toString());
                rmService.logMessage(logContext, Level.INFO);
                sw.close();
                return Router.UI_020061_A.getView();
            }
            sw.newLine();
            sw.write(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS) +
                    "#.............開始刪除非826之bankcode...........");
            Integer deleteCnt = rmService.deleteALLBANKByExceptBKNO("826");
            sw.newLine();
            sw.write(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS) +
                    "#.............刪除非826之bankcode, 筆數=" + deleteCnt.toString() + "...........");
            sw.newLine();
            sw.write(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS) +
                    "#.............開始轉檔...........");
            sw.newLine();
            sw.write(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS) +
                    "#.............開始轉檔('財金file')...........");
            List<String> resultMsg = new LinkedList<>();
            if (transData(sw, rMFileUploadPath + upLoadFileName, resultMsg, mode)) {
                Integer totalCnt = 0;
                totalCnt = rmService.getALLBANKCnt();
                sw.newLine();
                sw.write(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS) +
                        "#.............'財金file'總筆數" + totalCnt + "...........");
                sw.newLine();
                sw.write(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS) +
                        "#.............'財金file'轉檔成功...........");
            } else {
                sw.newLine();
                sw.write(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS) +
                        "#.............'財金file'轉檔完成,轉檔部份失敗...........");

                for (String s : resultMsg) {
                    sw.newLine();
                    sw.write(s);
                }
                sw.close();
                this.showMessage(mode, MessageType.INFO, "'財金file'轉檔完成,轉檔部份失敗");
                return Router.UI_020061_A.getView();
            }
            this.showMessage(mode, MessageType.INFO, "'財金file'轉檔完成");
            sw.newLine();
            sw.write(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS) +
                    "#.............開始轉檔('轉參加證券匯款銀行代號檔')...........");
            List<String> resultMsg2 = new LinkedList<>();
            if (addRmBank(sw, rMFileUploadPath + upLoadFileName2, resultMsg2, mode)) {
                Integer totalCnt = 0;
                totalCnt = rmService.getFISCAndBANKCnt();
                sw.newLine();
                sw.write(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS) +
                        "#.............'轉參加證券匯款銀行代號檔'總筆數 " + totalCnt + "...........");
                sw.newLine();
                sw.write(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS) +
                        "#.............'轉參加證券匯款銀行代號檔'轉檔成功...........");
            } else {
                sw.newLine();
                sw.write(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS) +
                        "#.............'轉參加證券匯款銀行代號檔'上傳成功，轉檔部分失敗...........");
                for (String s : resultMsg2) {
                    sw.newLine();
                    sw.write(s);
                }
                this.showMessage(mode, MessageType.INFO, "'轉參加證券匯款銀行代號檔'上傳成功，轉檔部分失敗");
                return Router.UI_020061_A.getView();
            }
            this.showMessage(mode, MessageType.INFO, "'轉參加證券匯款銀行代號檔'轉檔完成");
            sw.close();
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.INFO, ex.getMessage());
            logContext.setProgramException(ex);
            logContext.setRemark(exceptionMsg);
            rmService.logMessage(logContext, Level.INFO);
            sendEMS(logContext);
            return Router.UI_020061_A.getView();
        } finally {
            if (sw != null) {
                try {
                    safeCloseBufferedWriter(sw);
                } catch (Exception e) {
                    this.errorMessage(e, e.getMessage());
                    this.showMessage(mode, MessageType.DANGER, programError);
                }
            }
        }
        return Router.UI_020061_A.getView();
    }

    public void safeCloseBufferedWriter(BufferedWriter fis) throws Exception {
        if (fis != null) {
            fis.close();
        }
    }

    /**
     * 依查詢條件查詢的主程式。
     */
    @PostMapping(value = "/rm/UI_020061_A/getLog")
    private String getLog(@ModelAttribute UI_020061_Form form, ModelMap mode) {
        this.infoMessage("執行UI_020061_A, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        bindGridData(form, mode);
        return Router.UI_020061_A.getView();
    }

    private void bindGridData(UI_020061_Form form, ModelMap mode) {
        @SuppressWarnings("unused")
        String[] files;
        String fileName;
        String fileTime;
        String serverLogPath =
                rMFileUploadLogPath + FormatUtil.dateTimeFormat(Calendar.getInstance(),
                        FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN) + "/";
        String logPath;

        if ("~".equals(rMFileUploadLogPath.substring(0, 1))) {
            logPath = rMFileUploadLogPath.substring(1);
        } else {
            logPath = rMFileUploadLogPath;
        }
        List<HashMap<String, Object>> dtResult = new ArrayList<>();
//        @SuppressWarnings("unused")
//        File directoryFile = new File(CleanPathUtil.cleanString(serverLogPath));
        File[] list = new File(CleanPathUtil.cleanString(serverLogPath)).listFiles();
        if (list == null) {
            this.showMessage(mode, MessageType.WARNING, "並無Log檔案，請先轉檔後再查看Log");
            return;
        }
        for (File file : list) {
            if (file.isFile()) {
                if (file.getName().startsWith("UI_020061_") && file.getName().endsWith(".txt")) {
                    HashMap<String, Object> dr = new HashMap<>();
                    fileName = file.toString().substring(file.toString().lastIndexOf("/") + 1);
                    dr.put("LogName", fileName);

                    fileTime = fileName.substring(fileName.lastIndexOf("_") + 1, fileName.lastIndexOf("_") + 15);
                    dr.put("LogTime", fileTime.substring(0, 4) + "/" +
                            fileTime.substring(4, 6) + "/" +
                            fileTime.substring(6, 8) + " " +
                            fileTime.substring(8, 10) + ":" +
                            fileTime.substring(10, 12) + ":" +
                            fileTime.substring(12, 14));

                    dr.put("LogUrl", logPath + FormatUtil.dateTimeFormat(Calendar.getInstance(),
                            FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN) + "/" + fileName);

                    dtResult.add(dr);
                }
            }
        }

        if (dtResult.size() == 0) {
            this.showMessage(mode, MessageType.WARNING, "若轉檔結果尚未出現,請30秒後再查閱");
        } else {
            this.clearMessage(mode);
//            @SuppressWarnings("unused")
//            Integer pageNum = form.getPageNum();
//            @SuppressWarnings("unused")
//            Integer pageSize = form.getPageSize();
            PageInfo<HashMap<String, Object>> pageInfo = new PageInfo<>();
            pageInfo.setList(dtResult);
            PageData<UI_020061_Form, HashMap<String, Object>> pageData = new PageData<>(pageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        }
    }

    private Boolean transData(BufferedWriter sw, String fileName, List<String> resultMsg, ModelMap mode) {
        try {

            List<HashMap<String, Object>> list = readTxtFile(fileName, mode);

            Allbank defAllbank = null;
            Allbank defAllbankQuery = null;
            String line = "1";
            LogData objLogData = new LogData();
            Boolean rtn = true;
            RM txRmBusiness = new RM();
            Integer lineNo = 0;
            Boolean isUpdate = false;
            do {
                line = list.get(lineNo).get("logDetail").toString();
                if ("1".equals(line)) {
                    continue;
                }
                defAllbank = new Allbank();
                defAllbankQuery = new Allbank();

                defAllbank.setAllbankBkno(line.substring(1, 4));
                if ("380".equals(defAllbank.getAllbankBkno())) {
//                Trace.Write("")
                }
                if ("085".equals(defAllbank.getAllbankBkno())) {
                    defAllbank.setAllbankBkno(line.substring(1, 4));
                }

                //101, 102, 103,108, 118, 147行庫應該當作合作社
                if ("3".equals(line.substring(0, 1)) || "101,102,103,108,118,147".contains(line.substring(1, 4))) {
                    // 2:合作社
                    defAllbank.setAllbankType("2");
                } else {
                    //modified by maxine on 2011/06/24 for SYSSTAT自行查
                    //If line.Substring(1, 3) = SysStatus.PropertyValue.SYSSTAT_HBKNO Then
                    if (_SYSSTAT_HBKNO.equals(line.substring(1, 4))) {
                        // 1:自行
                        defAllbank.setAllbankType("0");
                    } else {
                        // 1:一般他行
                        defAllbank.setAllbankType("1");
                    }
                }

                if ("".equals(line.substring(4, 8).trim())) {
                    defAllbank.setAllbankBrno("000");
                    txRmBusiness = new RM();
                    RefString refBkno = new RefString(defAllbank.getAllbankBkno());
                    RefString refBrno = new RefString(defAllbank.getAllbankBrno());
                    RefString refType = new RefString(defAllbank.getAllbankType());
                    defAllbank.setAllbankBrnoChkcode(txRmBusiness.getBankDigit(refBkno, refBrno, refType));
                    isUpdate = false;
                } else {
                    defAllbank.setAllbankBrno(line.substring(4, 7));
                    defAllbank.setAllbankBrnoChkcode(line.substring(7, 8));
                    //Jim, 2011/12/26, 因BankCode =075,085, 皆有分行代號=000而非總行, 故000那筆也要update
//                    if ("000".equals(defAllbank.getAllbankBrno())) {
//                        isUpdate = true;
//                    }
                    isUpdate = true;
                }
                if ("1".equals(line.substring(0, 1))) {
                    // 1 : 共用中心
                    defAllbank.setAllbankFiscUnit(Short.parseShort("1"));
                } else {
                    // 0: 一般行庫
                    defAllbank.setAllbankFiscUnit(Short.parseShort("0"));
                }
                defAllbank.setAllbankUnitBank(defAllbank.getAllbankBkno());
                defAllbank.setAllbankFullname(subStr(line, 9, 70));
                defAllbank.setAllbankAliasname(subStr(line, 79, 10));
                defAllbank.setAllbankAddrress(subStr(line, 89, 40));

//  'If defALLBANK.ALLBANK_BKNO ="618" And  defALLBANK.ALLBANK_BRNO  = "009"
//                '    resultMsg.Add("ALLBANK_BKNO=" & defALLBANK.ALLBANK_BKNO & ";ALLBANK_BRNO=" & defALLBANK.ALLBANK_BRNO & "地址 = " & defALLBANK.ALLBANK_ADDRRESS & "isUpdate =" & isUpdate )
//                'End If
                defAllbank.setAllbankTelno(subStr(line, 130, 3) + subStr(line, 133, 8));
                if ("RM".equals(subStr(line, 151, 2))) {
                    defAllbank.setAllbankRmflag("1");
                } else {
                    defAllbank.setAllbankRmflag("0");
                }
                defAllbank.setAllbankRmforward("0");
//                if ("506,507,608".contains(defAllbank.getAllbankBkno())) {
//                    defAllbank.setAllbankUnitBank("910");
//                }
//                if ("901,912,916,928".contains(defAllbank.getAllbankBkno())) {
//                    defAllbank.setAllbankUnitBank("928");
//                }
//                if ("503,504,505,603,606,607,609,610,611,623".contains(defAllbank.getAllbankBkno())) {
//                    defAllbank.setAllbankUnitBank("951");
//                }
//                if ("512,515,517,518,520,521,523,524,525,612,613,614,616,617,618,619,620,621,622,624,627,952".contains(defAllbank.getAllbankBkno())) {
//                    defAllbank.setAllbankUnitBank("952");
//                }
//                if ("954".contains(defAllbank.getAllbankBkno())) {
//                    defAllbank.setAllbankUnitBank("954");
//                }
//                //Modify by Jim, 2011/12/23, 信合社改成加入共用中心BANKCODE = 127
//                if ("104,120,124,127,158,161,178,179,188,222,223,224".contains(defAllbank.getAllbankBkno())) {
//                    defAllbank.setAllbankUnitBank("997");
//                }
                defAllbank.setAllbankSetCloseFlag("0");
                defAllbank.setUpdateUserid(Integer.parseInt(WebUtil.getUser().getUserId()));

                defAllbankQuery.setAllbankBkno(defAllbank.getAllbankBkno());
                defAllbankQuery.setAllbankBrno(defAllbank.getAllbankBrno());
                if ("".equals(subStr(line, 161, 3).trim())) {
                    defAllbank.setAllbankUnitBank(defAllbank.getAllbankBkno());
                } else {
                    defAllbank.setAllbankUnitBank(subStr(line, 161, 3));
                }

                //ALLBANK  Update_TIME 之DATE = TXT_生效日期轉成西元年
                //ALLBANK  Update_Time 之 TIME = SYSTEM TIME
                String d = CalendarUtil.rocStringToADString(subStr(line, 142, 7));
                d = d.substring(0, 4) + "-" + d.substring(4, 6) + "-" + d.substring(6, 8);
                String updateTime = d + " " + FormatUtil.dateTimeFormat(Calendar.getInstance(),
                        FormatUtil.FORMAT_TIME_HH_MM_SSSSS);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                defAllbank.setUpdateTime(simpleDateFormat.parse(updateTime));
                //defALLBANK.UPDATE_TIME = CDate(DateLib.ROCStringToADString(SubStr(line, 112, 7)) & Now.ToString("HH:mm:ss.zzz"))

                try {
                    //If obj.GetALLBANKbyPK(defAllBankQuery) > 0 Then
                    //    If SubStr(line, 119, 1) = "R" _
                    //        AndAlso DateLib.ROCStringToADString(SubStr(line, 112, 7)) > defAllBankQuery.UPDATE_TIME.ToString("yyyyMMdd") Then
                    //        obj.UpdateALLBANK(defALLBANK)
                    //    End If
                    //Else
                    if (defAllbank.getAllbankBkno().equals(defAllbank.getAllbankUnitBank()) && !isUpdate) {
                        if (rmService.getALLBANKDataTableByPK(defAllbank).size() < 1) {
                            rmService.insertAllBank(defAllbank);
                        } else {
                            rmService.updateALLBANK(defAllbank);
                        }
                        this.checkRMSNO(defAllbank);
                    } else {
                        List<Allbank> dtAllbank = rmService.getALLBANKDataTableByPK(defAllbank);
                        if (dtAllbank.size() < 1) {
                            rmService.insertAllBank(defAllbank);
                        } else {
                            Date date = dtAllbank.get(0).getUpdateTime();
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                            String dateString = formatter.format(date);
                            if (Integer.parseInt(CalendarUtil.rocStringToADString(subStr(line, 142, 7)))
                                    > Integer.parseInt(dateString)
                                    || isUpdate) {
                                rmService.updateALLBANK(defAllbank);
                            }
                        }
                    }
                } catch (Exception ex) {
                    //WriteLog(sw, "ALLBANK_BKNO=" & defALLBANK.ALLBANK_BKNO & ";ALLBANK_BRNO=" & defALLBANK.ALLBANK_BRNO & "轉檔出錯,Exception=" & ex.Message)
                    objLogData.setTableName("ALLBANK");
                    objLogData.setPrimaryKeys("ALLBANK_BKNO=" + defAllbank.getAllbankBkno() + ";ALLBANK_BRNO=" + defAllbank.getAllbankBrno());
                    objLogData.setRemark("ALLBANK轉檔失敗");
                    objLogData.setProgramName("UI_020061");
                    objLogData.setProgramException(ex);
                    TxHelper.getRCFromErrorCode(IOReturnCode.ALLBANKUpdateError, FEPChannel.BRANCH, new LogData());
                    sendEMS(logContext);
                    resultMsg.add("失敗資料: ALLBANK_BKNO=" + defAllbank.getAllbankBkno() + ";ALLBANK_BRNO=" + defAllbank.getAllbankBrno());
                    getLogContext().setProgramException(ex);
                    logContext.setRemark(exceptionMsg);
                    rmService.logMessage(logContext, Level.INFO);
                    rtn = false;
                }
                lineNo++;
            } while (lineNo < list.size());
            return rtn;
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            logContext.setProgramException(ex);
            logContext.setRemark(exceptionMsg);
            rmService.logMessage(logContext, Level.INFO);
            sendEMS(logContext);
            return false;
        }
    }

    /**
     * 檢查是否有參加財金證券匯款
     */
    public Boolean addRmBank(BufferedWriter sw, String fileName, List<String> resultMsg, ModelMap mode) {
        Allbank defAllbank = new Allbank();
        String cursor;
        try {
            List<HashMap<String, Object>> list = readTxtFile(fileName, mode);
            if (list.size() > 0) {
                cursor = list.get(0).get("logDetail").toString();
            } else {
                cursor = null;
            }

            if (cursor != null) {
                //  defALLBANK.ALLBANK_BKNO = cursor.Trim
                defAllbank.setAllbankBkno(cursor.trim());
                defAllbank.setAllbankSetCloseFlag("1");
                defAllbank.setAllbankRmflag("1");
            }
            if (rmService.updateALLBANKByBKNO(defAllbank) < 1) {
                sw.newLine();
                sw.write(FormatUtil.dateTimeFormat(Calendar.getInstance(),
                        FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS) + "#.............更新ALLBANK失敗...........");
                return false;
            } else {
                sw.newLine();
                sw.write(FormatUtil.dateTimeFormat(Calendar.getInstance(),
                        FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS) + "#.............更新ALLBANK成功...........");

                return true;
            }
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            logContext.setProgramException(ex);
            logContext.setRemark(exceptionMsg);
            rmService.logMessage(logContext, Level.INFO);
            sendEMS(logContext);
            return false;
        }
    }

    public static String subStr(String strfSource, Integer intStart, Integer intLen) {
        byte byteAry[] = strToByteAry(strfSource);
        byte[] newAry = new byte[intLen];
        Integer i = 0, j = 0;
        while (j < intLen) {
            newAry[i] = byteAry[intStart];
            i = i + 1;
            j = j + 1;
            intStart = intStart + 1;
        }
        return ConvertUtil.toString(newAry, PolyfillUtil.toCharsetName("big5"));
    }

    public static byte[] strToByteAry(String oriString) {
        return ConvertUtil.toBytes(oriString, PolyfillUtil.toCharsetName("big5"));
    }

    private void writeSw(BufferedWriter sw, String logMsg) {
        try {
            sw.newLine();
            // 2024-11-18 Richard modified for 【Unrestricted File Upload】
            // sw.write(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS) + logMsg);
            ReflectUtil.envokeMethod(sw, "write", new Class[] {String.class}, new Object[] {FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS) + logMsg});
        } catch (IOException e) {
            this.errorMessage(e, e.getMessage());
        }
    }

    public void safeClose(BufferedWriter fis) throws Exception {
        if (fis != null) {
            fis.close();
        }
    }

    private void checkRMSNO(Allbank defAllbank) throws Exception {
        Rmoutsno defRmoutsno = new Rmoutsno();
        Rminsno defRminsno = new Rminsno();
        try {
            defRmoutsno.setRmoutsnoSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
            defRmoutsno.setRmoutsnoReceiverBank(defAllbank.getAllbankBkno());
            if (rmService.getRmoutsnoQueryByPrimaryKey(defRmoutsno) == null) {
                defRmoutsno.setRmoutsnoSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
                defRmoutsno.setRmoutsnoReceiverBank(defAllbank.getAllbankBkno());
                defRmoutsno.setRmoutsnoNo(0);
                defRmoutsno.setRmoutsnoRepNo(0);
                defRmoutsno.setRmoutsnoChgk("0");
                defRmoutsno.setRmoutsnoChgkTimes(0);
                defRmoutsno.setRmoutsnoCdkeyFlag("0");
                defRmoutsno.setRmoutsno3des("3");
                defRmoutsno.setRmoutsnoDesDate("");
                if (rmService.insertRmoutsno(defRmoutsno) < 1) {
                    logContext.setRemark("ALLBANK_BKNO=" + defAllbank.getAllbankBkno() + ", 新增RMOUTSNO失敗");
                } else {
                    logContext.setRemark("ALLBANK_BKNO=" + defAllbank.getAllbankBkno() + ", 新增RMOUTSNO成功");
                }
                rmService.logMessage(logContext, Level.INFO);
            }
            rmService.logMessage(logContext, Level.INFO);
            defRminsno.setRminsnoSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
            defRminsno.setRminsnoReceiverBank(defAllbank.getAllbankBkno());
            if (rmService.getRminsnoQueryByPrimaryKey(defRminsno) == null) {
                defRminsno.setRminsnoSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
                defRminsno.setRminsnoReceiverBank(defAllbank.getAllbankBkno());
                defRminsno.setRminsnoNo(0);
                defRminsno.setRminsnoChgk("0");
                defRminsno.setRminsnoChgkTimes(0);
                defRminsno.setRminsnoCdkeyFlag("0");
                defRminsno.setRminsno3des("3");
                defRminsno.setRminsnoDesDate("");
                if (rmService.insertRminsno(defRminsno) < 1) {
                    logContext.setRemark("ALLBANK_BKNO=" + defAllbank.getAllbankBkno() + ", 新增RMINSNO失敗");
                } else {
                    logContext.setRemark("ALLBANK_BKNO=" + defAllbank.getAllbankBkno() + ", 新增RMINSNO成功");
                }
                rmService.logMessage(logContext, Level.INFO);
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            logContext.setRemark(exceptionMsg);
            rmService.logMessage(logContext, Level.INFO);
            sendEMS(logContext);
        }
    }

    @PostMapping(value = "/rm/UI_020061_A_Detail/getLogDetail")
    private String getLogDetail(@ModelAttribute UI_020061_Form form, ModelMap mode) {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        //在根目錄下創建檔案
        File path = new File(CleanPathUtil.cleanString(""));// 參數為空 獲取項目根目錄
        String startPath = "";
        try {
            startPath = path.getCanonicalPath();
        } catch (IOException e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        File uI_020061_A = new File(CleanPathUtil.cleanString(startPath + "/" + "UI_020061_A"));
        if (!uI_020061_A.exists()) {
            if (uI_020061_A.mkdirs()) {
                LogHelperFactory.getTraceLogger().warn(uI_020061_A.getPath(), " mkdirs failed!!!");
            }
        }
        File log = new File(CleanPathUtil.cleanString(startPath + "/" + "UI_020061_A/Log"));
        if (!log.exists()) {
            if (log.mkdirs()) {
                LogHelperFactory.getTraceLogger().warn(log.getPath(), " mkdirs failed!!!");
            }
        }
        BaseResp<UI_020061_Form> resp = new BaseResp<>();
        List<HashMap<String, Object>> logDetail = readTxtFile(form.getLogUrl(), mode);
        if (resp.getMessage() != null && !"".equals(resp.getMessage())) {
            this.showMessage(mode, MessageType.DANGER, resp.getMessage());
            return Router.UI_020061_A_Detail.getView();
        }
        PageInfo<HashMap<String, Object>> pageInfo = new PageInfo<>();
        pageInfo.setList(logDetail);
        PageData<UI_020061_Form, HashMap<String, Object>> pageData = new PageData<>(pageInfo, form);
        WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        return Router.UI_020061_A_Detail.getView();
    }

    private List<HashMap<String, Object>> readTxtFile(String filePath, ModelMap mode) {
        List<HashMap<String, Object>> logDetail = new ArrayList<>();
        FileInputStream fis = null;
        try {
            String encoding = "Big5";
            File directory = new File(CleanPathUtil.cleanString(""));// 參數為空 獲取項目根目錄
            String courseFile = "";
            try {
                courseFile = directory.getCanonicalPath();
            } catch (IOException e) {
                this.errorMessage(e, e.getMessage());
                this.showMessage(mode, MessageType.DANGER, programError);
            }
            // 2024-11-06 Richard modified for 【Relative Path Traversal】
            // File file = new File(CleanPathUtil.cleanString(courseFile + "/" + filePath));
            Object file = ESAPIUtil.toFile(CleanPathUtil.cleanString(courseFile + "/" + filePath));
            if (((File) file).isFile() && ((File) file).exists()) { //判斷檔案是否存在
                fis = FileUtils.openInputStream((File) file);
                List<String> lineList = IOUtils.readLines(fis, encoding);//考慮到編碼格式
                for (String lineTxt : lineList) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("logDetail", lineTxt);
                    logDetail.add(hashMap);
                }
            } else {
                this.showMessage(mode, MessageType.WARNING, "找不到指定的檔案");
            }
        } catch (Exception e) {
            this.showMessage(mode, MessageType.WARNING, "讀取檔案內容出錯");
        } finally {
            IOUtils.closeQuietly(fis);
        }
        return logDetail;
    }
}
