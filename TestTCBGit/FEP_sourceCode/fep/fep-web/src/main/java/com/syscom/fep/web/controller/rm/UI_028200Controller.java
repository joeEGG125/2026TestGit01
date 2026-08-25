package com.syscom.fep.web.controller.rm;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.net.ftp.FtpAdapter;
import com.syscom.fep.frmcommon.net.ftp.FtpAdapterFactory;
import com.syscom.fep.frmcommon.net.ftp.FtpProperties;
import com.syscom.fep.frmcommon.net.ftp.FtpProtocol;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Rmbtch;
import com.syscom.fep.mybatis.model.Rmbtchmtr;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.rm.RM;
import com.syscom.fep.server.common.handler.FCSHandler;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.vo.text.fcs.FCSFoot;
import com.syscom.fep.vo.text.fcs.FCSHead;
import com.syscom.fep.vo.text.rm.RMGeneral;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.form.rm.UI_028200_Form;
import com.syscom.fep.web.form.rm.UI_028200_Form_1;
import com.syscom.fep.web.form.rm.UI_028200_Form_2;
import com.syscom.fep.web.service.MemberShipService;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 大批匯款監控啟動
 *
 * @author xingyun_yang
 * @create 2021/11/30
 */
@Controller
public class UI_028200Controller extends BaseController {

    @Autowired
    RmService rmService;
    @Autowired
    MemberShipService memberShipService;
    @Autowired
    private FtpAdapterFactory factory;

    private List<HashMap<String, Object>> _dtResult;
    private LogData _LogContext;
    private String _FCSInPath;
    private String _FCSInPath_FTP;
    private String _FCSInFileName;
    private String _FCSFTPServer;
    private int _FCSFTPPort;
    private String _FCSFTPUserId;
    private String _FCSFTPSscode; // 2024-11-18 Richard modified for 【Heap Inspection】
    private String _FTPPath;
    private String _FileMask;
    private String _FCSB2BInFileName;
    private String FCSELNInFileName;
    private String _FileMask1;
    private String FileMask2;
    private List<String> _files;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_028200_Form form = new UI_028200_Form();
        _LogContext = new LogData();
        // _FCSInPath ="E://sftpDownload/";
        // 本地下載路徑
        _FCSInPath = RMConfig.getInstance().getFCSInPath();
        _FCSInPath_FTP = RMConfig.getInstance().getFCSInPathFTP();
        _FCSInFileName = RMConfig.getInstance().getFCSFileName();
        _FCSFTPUserId = RMConfig.getInstance().getFCSFTPUserId();
        _FCSFTPSscode = RMConfig.getInstance().getFCSFTPSscode();
        _FCSB2BInFileName = "B2BR1300";
        _FileMask = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN) + _FCSInFileName + "*";
        _FileMask1 = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN) + _FCSB2BInFileName + "*";
        _FCSFTPServer = RMConfig.getInstance().getFCSFTPServer();
        //20210512 add for PCR:R1100調RIM新增FT交易序號+信貸整批匯款
        FCSELNInFileName = "ELNR1300";
        FileMask2 = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN) +
                FCSELNInFileName + "*";

        try {
            _FCSFTPPort = Integer.parseInt(RMConfig.getInstance().getFCSFTPPort());
        } catch (Exception e) {
            _FCSFTPPort = 22;
        }
        // _FTPPath = "ftp://" + _FCSFTPServer + "/" + + "/";
        // _FTPPath = "/FEPIN/"; //test
        _FTPPath = "/" + _FCSInPath_FTP + "/";
        String queryOnly = WebConfiguration.getInstance().getQueryOnly();

        if ("1".equals(queryOnly)) {
            form.setQueryOnly("false");
        } else {
            form.setQueryOnly("true");
        }
        logContext.setProgramName("UI_028200");
        logContext.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        // 交易日期
        form.setDatetime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/rm/UI_028200/index")
    public String onload(@ModelAttribute UI_028200_Form form, ModelMap mode) {
        this.doKeepFormData(mode, form);
        pageOnLoad(mode);
        return Router.UI_028200.getView();
    }

    @PostMapping(value = "/rm/UI_028200/ctrlQueryClick")
    public String ctrlQueryClick(@ModelAttribute UI_028200_Form form, ModelMap mode) throws Exception {
        this.infoMessage("執行UI_028200, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        if ("1".equals(form.getQueryTypeDdl())) {
            queryClick(form, mode);
            return Router.UI_028200_1.getView();
        } else {
            bindResultGrdv2(form, mode);
            return Router.UI_028200_2.getView();
        }
    }

    @PostMapping(value = "/rm/UI_028200_2/queryAllClick")
    public String queryAllClick_UI_028200_2(@ModelAttribute UI_028200_Form form, ModelMap mode) throws Exception {
        this.infoMessage("UI_028200_2_1查詢, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            if ("all".equals(form.getQueryType())) {
                form.setRmbtchmtrRemDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            }
            bindResultGrdv2(form, mode);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_028200_2.getView();
    }

    @PostMapping(value = "/rm/UI_028200_2/queryClick")
    public String resultGrdv2_RowCommand(@ModelAttribute UI_028200_Form form, ModelMap mode) throws Exception {
        this.infoMessage("UI_028200_2_1查詢, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            if ("all".equals(form.getQueryType())) {
                form.setRmbtchRemDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
                form.setRmbtchTimes(null);
            }
            form.setRmbtchRemDate(form.getRmbtchmtrRemDate());
            form.setRmbtchTimes(form.getRmbtchmtrTimes());
            bindResultGrdv3(form, mode);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_028200_2_1.getView();
    }

    @PostMapping(value = "/rm/UI_028200_2_1/queryClick")
    public String bindFormViewData(@ModelAttribute UI_028200_Form form, ModelMap mode) throws Exception {
        this.infoMessage("UI_028200_2_1查詢, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        Rmbtch defRmbtch;
        List<HashMap<String, Object>> dtTable;
        try {

            defRmbtch = new Rmbtch();
            // Fly 2017/06/14 增加日期查詢條件
            defRmbtch.setRmbtchTimes(form.getRmbtchTimes());
            defRmbtch.setRmbtchRemdate(form.getRmbtchRemDate());
            defRmbtch.setRmbtchSenderBank(form.getRmbtchSenderBank());
            defRmbtch.setRmbtchFepno(form.getRmbtchFepNo());
            dtTable = rmService.getRMBTCHbyDef_UI028200(defRmbtch);
            if (dtTable == null || dtTable.size() == 0) {
                this.showMessage(mode, MessageType.DANGER, "大批匯款回饋明細檔(RMBTCHMTR)無資料");
                return Router.UI_028200_2_1.getView();
            }
            List<HashMap<String, Object>> dtNewRmout = new ArrayList<>();
            for (int i = 0; i < dtTable.size(); i++) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("RMBTCH_FLAG", String.valueOf(dtTable.get(i).get("RMBTCH_FLAG")).equals("null") ? "" : getRMBTCHMTR_FLAGName(String.valueOf(dtTable.get(i).get("RMBTCH_FLAG"))));
                hashMap.put("RMBTCH_RECBANK", String.valueOf(dtTable.get(i).get("RMBTCH_RECBANK")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_RECBANK")));
                hashMap.put("RMBTCH_INBKHC", String.valueOf(dtTable.get(i).get("RMBTCH_INBKHC")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_INBKHC")));
                hashMap.put("RMBTCH_REGDATE", String.valueOf(dtTable.get(i).get("RMBTCH_REGDATE")).equals("null") ? "" : charDateToDate(String.valueOf(dtTable.get(i).get("RMBTCH_REGDATE")), ""));
                hashMap.put("RMBTCH_REMTYPE", String.valueOf(dtTable.get(i).get("RMBTCH_REMTYPE")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_REMTYPE")));
                hashMap.put("REMTYPE", String.valueOf(dtTable.get(i).get("REMTYPE")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("REMTYPE")));
                hashMap.put("RMBTCH_SUPNO1", String.valueOf(dtTable.get(i).get("RMBTCH_SUPNO1")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_SUPNO1")));
                hashMap.put("RMBTCH_SUPNO2", String.valueOf(dtTable.get(i).get("RMBTCH_SUPNO2")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_SUPNO2")));
                hashMap.put("RMBTCH_SENDER_BANK", String.valueOf(dtTable.get(i).get("RMBTCH_SENDER_BANK")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_SENDER_BANK")));
                hashMap.put("RMBTCH_NAME_RCV", String.valueOf(dtTable.get(i).get("RMBTCH_NAME_RCV")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_NAME_RCV")));
                hashMap.put("RMBTCH_REMTXTP", String.valueOf(dtTable.get(i).get("RMBTCH_REMTXTP")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_REMTXTP")));
                hashMap.put("RMBTCH_FCS_INDEX", String.valueOf(dtTable.get(i).get("RMBTCH_FCS_INDEX")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_FCS_INDEX")));
                hashMap.put("UPDATE_USERID", String.valueOf(dtTable.get(i).get("UPDATE_USERID")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("UPDATE_USERID")));
                hashMap.put("RMBTCH_FEP_RC", String.valueOf(dtTable.get(i).get("RMBTCH_FEP_RC")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_FEP_RC")));
                hashMap.put("RMBTCH_ERRMSG", String.valueOf(dtTable.get(i).get("RMBTCH_ERRMSG")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_ERRMSG")));
                hashMap.put("RMBTCH_OUT_ACTNO", String.valueOf(dtTable.get(i).get("RMBTCH_OUT_ACTNO")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_OUT_ACTNO")));
                hashMap.put("UPDATE_TIME", String.valueOf(dtTable.get(i).get("UPDATE_TIME")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("UPDATE_TIME")));
                hashMap.put("RMBTCH_REMARK", String.valueOf(dtTable.get(i).get("RMBTCH_REMARK")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_REMARK")));
                hashMap.put("RMBTCH_TLRNO", String.valueOf(dtTable.get(i).get("RMBTCH_TLRNO")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_TLRNO")));
                hashMap.put("RMBTCH_HCTXTP", String.valueOf(dtTable.get(i).get("RMBTCH_HCTXTP")).equals("null") ? "" : getAMT_TYPEName(String.valueOf(dtTable.get(i).get("RMBTCH_HCTXTP"))));
                hashMap.put("RMBTCH_NAME_SEND", String.valueOf(dtTable.get(i).get("RMBTCH_NAME_SEND")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_NAME_SEND")));
                hashMap.put("RMBTCH_FEE", String.valueOf(dtTable.get(i).get("RMBTCH_FEE")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_FEE")));
                hashMap.put("RMBTCH_ACTNO", String.valueOf(dtTable.get(i).get("RMBTCH_ACTNO")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_ACTNO")));
                hashMap.put("RMBTCH_KINBRNO", String.valueOf(dtTable.get(i).get("RMBTCH_KINBRNO")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_KINBRNO")));
                hashMap.put("RMBTCH_DATA_FLAG",
                        String.valueOf(dtTable.get(i).get("RMBTCH_DATA_FLAG")).equals("null") ? "" : getRMBTCH_DATA_FLAGName(String.valueOf(dtTable.get(i).get("RMBTCH_DATA_FLAG"))));
                hashMap.put("RMBTCH_REMDATE", String.valueOf(dtTable.get(i).get("RMBTCH_REMDATE")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_REMDATE")));
                hashMap.put("RMBTCH_TIMES", String.valueOf(dtTable.get(i).get("RMBTCH_TIMES")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_TIMES")));
                hashMap.put("RMBTCH_FEPNO", String.valueOf(dtTable.get(i).get("RMBTCH_FEPNO")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_FEPNO")));
                hashMap.put("RMBTCH_REMAMT", String.valueOf(dtTable.get(i).get("RMBTCH_REMAMT")).equals("null") ? "" : String.valueOf(dtTable.get(i).get("RMBTCH_REMAMT")));
                dtNewRmout.add(hashMap);
            }
            WebUtil.putInAttribute(mode, AttributeName.DetailEntity, dtNewRmout.get(0));
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_028200_2_1_Detail.getView();
    }

    private void queryClick(UI_028200_Form form, ModelMap mode) {
        Boolean hasFile = false;
        int index = 0;
        try {
            if (!FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH).equals(form.getDatetime())) {
                this.showMessage(mode, MessageType.WARNING, "尚未上傳FEP 僅能查詢當日資料");
                return;
            }
            // 20140812 ADD BY Candy for 增加B2BR1300 監控

            _dtResult = new ArrayList<>();
            ftpWork(_FileMask, "Query", mode);
            //20210512 modify 三種都Nothing才顯示無任何檔案需要重送
            if (_files == null || _files.size() == 0) {
                ftpWork(_FileMask1, "Query", mode);
                if (_files == null) {
                    this.showMessage(mode, MessageType.WARNING, "FCS之FTPServer無任何檔案需要重送");
                } else {
                    // 將目錄中的檔案資訊寫入dtResult
                    for (String fileName : _files) {
                        logContext.setRemark("UI_028200, Find B2BR1300File: " + _FCSInPath + fileName);
                        rmService.logMessage(logContext, Level.INFO);
                        hasFile = true;
                        HashMap<String, Object> dr = new HashMap<>();
                        dr.put("index", index);
                        dr.put("FILENAME", fileName);
                        // Fly 2018/09/03 Fortify修正:Path Manipulation
                        File file = new File(CleanPathUtil.cleanString(_FCSInPath + "/" + fileName));
                        Path path = file.toPath();
                        BasicFileAttributes attr = null;
                        attr = Files.readAttributes(path, BasicFileAttributes.class);
                        dr.put("FILEDATETIME", attr.creationTime().toInstant());
                        dr.put("RESULT", "未處理");
                        _dtResult.add(dr);
                        index++;
                    }
                }
                //20210512 add for PCR:R1100調RIM新增FT交易序號+信貸整批匯款
                ftpWork(FileMask2, "Query", mode);
                if (_files != null) {
                    // 將目錄中的檔案資訊寫入dtResult
                    for (String fileName : _files) {
                        logContext.setRemark("UI_028200, Find ELNR1300 File: " + _FCSInPath + fileName);
                        rmService.logMessage(logContext, Level.INFO);
                        hasFile = true;
                        HashMap<String, Object> dr = new HashMap<>();
                        dr.put("index", index);
                        dr.put("FILENAME", fileName);
                        // Fly 2018/09/03 Fortify修正:Path Manipulation
                        File file = new File(CleanPathUtil.cleanString(_FCSInPath + "/" + fileName));
                        Path path = file.toPath();
                        BasicFileAttributes attr = null;
                        attr = Files.readAttributes(path, BasicFileAttributes.class);
                        dr.put("FILEDATETIME", attr.creationTime().toInstant());
                        dr.put("RESULT", "未處理");
                        _dtResult.add(dr);
                        index++;
                    }
                }
            } else {
                // 將目錄中的檔案資訊寫入dtResult
                for (String fileName : _files) {
                    logContext.setRemark("UI_028200, Find FEPR1300File:" + _FCSInPath + fileName);
                    rmService.logMessage(logContext, Level.INFO);
                    hasFile = true;
                    HashMap<String, Object> dr = new HashMap<>();
                    dr.put("index", index);
                    dr.put("FILENAME", fileName);
                    // Fly 2018/09/03 Fortify修正:Path Manipulation
                    File file = new File(CleanPathUtil.cleanString(_FCSInPath + "/" + fileName));
                    Path path = file.toPath();
                    BasicFileAttributes attr = null;
                    attr = Files.readAttributes(path, BasicFileAttributes.class);
                    dr.put("FILEDATETIME", attr.creationTime().toInstant());
                    dr.put("RESULT", "未處理");
                    _dtResult.add(dr);
                    index++;
                }
                ftpWork(_FileMask1, "Query", mode);
                if (_files == null) {
                    this.showMessage(mode, MessageType.WARNING, "FCS之FTPServer無任何檔案需要重送");
                } else {
                    // 將目錄中的檔案資訊寫入dtResult
                    for (String fileName : _files) {
                        logContext.setRemark("UI_028200, Find B2BR1300 File:" + _FCSInPath + fileName);
                        rmService.logMessage(logContext, Level.INFO);
                        hasFile = true;
                        HashMap<String, Object> dr = new HashMap<>();
                        dr.put("index", index);
                        dr.put("FILENAME", fileName);
                        // Fly 2018/09/03 Fortify修正:Path Manipulation
                        File file = new File(CleanPathUtil.cleanString(_FCSInPath + "/" + fileName));
                        Path path = file.toPath();
                        BasicFileAttributes attr = null;
                        attr = Files.readAttributes(path, BasicFileAttributes.class);
                        dr.put("FILEDATETIME", attr.creationTime().toInstant());
                        dr.put("RESULT", "未處理");
                        _dtResult.add(dr);
                        index++;
                    }
                }
                //20210512 add for PCR:R1100調RIM新增FT交易序號+信貸整批匯款
                ftpWork(FileMask2, "Query", mode);
                if (_files != null) {
                    // 將目錄中的檔案資訊寫入dtResult
                    for (String fileName : _files) {
                        logContext.setRemark("UI_028200, Find ELNR1300 File:" + _FCSInPath + fileName);
                        rmService.logMessage(logContext, Level.INFO);
                        hasFile = true;
                        HashMap<String, Object> dr = new HashMap<>();
                        dr.put("index", index);
                        dr.put("FILENAME", fileName);
                        // Fly 2018/09/03 Fortify修正:Path Manipulation
                        File file = new File(CleanPathUtil.cleanString(_FCSInPath + "/" + fileName));
                        Path path = file.toPath();
                        BasicFileAttributes attr = null;
                        attr = Files.readAttributes(path, BasicFileAttributes.class);
                        dr.put("FILEDATETIME", attr.creationTime().toInstant());
                        dr.put("RESULT", "未處理");
                        _dtResult.add(dr);
                        index++;
                    }
                }
            }
            if (hasFile) {
                bindGridData(mode);
            } else {
                this.showMessage(mode, MessageType.INFO, "FCS之FTPServer無任何檔案需要重送");
            }
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, ex.toString());
        }
    }

    // Authentication 暫時不翻寫
//	private Boolean checkSUPID(ModelMap mode) {
//		// Fly 2018/09/05 Fortify:Cross-Site Scripting: Reflected
//
//		String supno2 = "";
//		String supno3 = "";
//		String supno2Pw = "";
//
//		if (!supno2.equals(supno3)) {
//			String errMsg = "";
//			Fepuser defFepuser = new Fepuser();
//			Fepuser loginDefFEPUSER = new Fepuser();
//			String ssoMode = "";
//			String ssoUrl = "";
//			// String ssoMode = WebConfiguration.getInstance().getSsoMode();
//			// String ssoUrl = WebConfiguration.getInstance().getSsoURL();
//
//			logContext.setRemark("SSOURL =" + ssoUrl + "; Start to Check SUPNO2---------------------------");
//			rmService.logMessage(logContext, Level.INFO);
//			loginDefFEPUSER.setFepuserLogonid(supno2);
//			if (memberShipService.getUserInfo(defFepuser, errMsg)) {
//				String strreturn = StringUtils.EMPTY;
//				if (Boolean.parseBoolean(ssoMode)) {
//					strreturn = checkAuthentication(ssoUrl, supno2, supno2Pw);
//					if (!"".equals(strreturn)) {
//						// return true;
//					} else {
//						this.showMessage(mode, MessageType.WARNING, "授權主管代號二" + strreturn);
//						return false;
//					}
//				}
//			} else {
//				logContext.setRemark("SUPNO2=" + supno2 + ", Query FEPUSER no data");
//				rmService.logMessage(logContext, Level.INFO);
//				this.showMessage(mode, MessageType.WARNING, "授權主管代號二" + TxHelper.getMessageFromFEPReturnCode(IOReturnCode.QueryNoData));
//				return false;
//			}
//			logContext.setRemark("Start to Check SUPNO3---------------------------");
//			rmService.logMessage(logContext, Level.INFO);
//			loginDefFEPUSER.setFepuserLogonid(supno3);
//			if (memberShipService.getUserInfo(defFepuser, errMsg)) {
//				String strreturn = StringUtils.EMPTY;
//				if (Boolean.parseBoolean(ssoMode)) {
//					strreturn = checkAuthentication(ssoUrl, supno3, supno2Pw);
//					if (!"".equals(strreturn)) {
//						return true;
//					} else {
//						this.showMessage(mode, MessageType.WARNING, "授權主管代號三" + strreturn);
//						return false;
//					}
//				} else {
//					return true;
//				}
//			} else {
//				logContext.setRemark("SUPNO3=" + supno3 + ", Query FEPUSER no data");
//				rmService.logMessage(logContext, Level.INFO);
//				this.showMessage(mode, MessageType.WARNING, "授權主管代號三" + TxHelper.getMessageFromFEPReturnCode(IOReturnCode.QueryNoData));
//				return false;
//			}
//
//		} else {
//			this.showMessage(mode, MessageType.WARNING, "授權主管代號二, 授權主管代號三不能相同");
//			return false;
//
//		}
//	}

    @PostMapping(value = "/rm/UI_028200_1/execute")
    public String resultGrdv_RowCommand(@ModelAttribute UI_028200_Form_1 form, ModelMap mode) {
        this.infoMessage("UI_028200_2_1查詢, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        RM txRMBusiness = new RM();
        RefBase<ArrayList> fCSDataList = new RefBase<>(new ArrayList());
        RefBase<FCSHead> objFCSHead = new RefBase<>(new FCSHead());
        RefBase<FCSFoot> objFCSFoot = new RefBase<>(new FCSFoot());
        FEPReturnCode rtnCode = null;
        _LogContext.setProgramName("UI_028200");
        // 取得修改資料的PK
        Integer index = Integer.parseInt(form.getIndex());
        HashMap<String, Object> row = _dtResult.get(index);
        _LogContext.setRemark("準備進入明細畫面,第" + index + "筆, file path=" + _FCSInPath + row.get("FILENAME").toString());
        rmService.logMessage(_LogContext, Level.INFO);

        try {
            UI_028200_Form_2 form2 = new UI_028200_Form_2();
            rtnCode = txRMBusiness.readAndParseFCSFile(_FCSInPath + row.get("FILENAME").toString(), objFCSHead, fCSDataList, objFCSFoot, false);
            _LogContext.setRemark("After ReadAndParseFCSFile , rtnCode=" + rtnCode.toString());
            rmService.logMessage(_LogContext, Level.INFO);
            if (CommonReturnCode.Normal.equals(rtnCode)) {
                form2.setFileName(row.get("FILENAME").toString());
                _LogContext.setRemark("After Set FILENAME value");
                rmService.logMessage(_LogContext, Level.INFO);
                form2.setRemDate(objFCSHead.get().getRemDate());
                form2.setBatchNo(row.get("FILENAME").toString());
                form2.setTlrNo(objFCSHead.get().getTlrNo());
                form2.setSupNo(objFCSHead.get().getSupNo1());
            }
            this.clearMessage(mode);
            WebUtil.putInAttribute(mode, AttributeName.Form, form2);
            return Router.UI_028200_1_Detail.getView();
        } catch (Exception ex) {
            _LogContext.setProgramException(ex);
            _LogContext.setRemark("ResultGrdv_RowCommand exception: " + ex.toString());
            rmService.logMessage(_LogContext, Level.INFO);
            this.showMessage(mode, MessageType.DANGER, QueryFail);
            return null;
        }
    }

    // 暫時不翻寫
    protected String checkAuthentication(String strURL, String strID, String strSscode) throws Exception {
        HttpURLConnection conn = null;
        OutputStream os = null;
        InputStream in = null;
        String strRtnResult = StringUtils.EMPTY;
        logContext.setProgramName("UI_028200");
        Integer errorStep = 0;
        try {
            // 送至企業平台主機
            // 組合傳輸字元
            String wsData = "ChkUserID=" + strID.trim() + "&ChkUserPwd=" + strSscode.trim(); // 2024-11-06 Richard modified for 【Cleartext Submission of Sensitive Information】
            URL httpUrl = new URL(strURL);
            conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/x-www-form-urlencoded");
            os = conn.getOutputStream();
            errorStep = 1;
            // 參數編碼(用http ppost傳輸的方式)
            os.write(wsData.getBytes(StandardCharsets.UTF_8));
            os.flush();
            logContext.setRemark("After GetRequestStream, wsData=" + wsData + ", bs=" + conn.getContentLength());
            rmService.logMessage(logContext, Level.INFO);
            errorStep = 2;
            // 取得 WebResponse 的物件 然後把回傳的資料讀出
            in = conn.getInputStream();
            String responseFormServer = IOUtils.toString(in, StandardCharsets.UTF_8);
            errorStep = 3;
            if (responseFormServer.length() < 3) {
                // 企業平台驗證之回覆資料不足(至少要為"Y,,"-->所以是3碼)
                strRtnResult = "企業平台驗證之回覆資料不足";
            } else {
                // 拆解回傳字串
                String[] strRtn = new String[2];
                // Y成功或N失敗
                strRtn[0] = responseFormServer.substring(0, 1);
                // 用於取第二個逗點的位置, 以取回傳錯誤值
                Integer intPos = responseFormServer.indexOf(",", 2);

                if (intPos < 0) {
                    // 表示可能少第三個的逗點
                    strRtnResult = "企業平台驗證之回覆資料不足";
                } else {
                    if (!",".equals(responseFormServer.substring(1, 2))) {
                        // 第二碼一定要是逗點
                        strRtnResult = "企業平台驗證之回覆資料不足";
                    } else {
                        strRtn[1] = responseFormServer.substring(2, intPos - 2 + 2);
                        if ((intPos + 1) == responseFormServer.length() && strRtn[0].equalsIgnoreCase("Y")) {
                            strRtn[1] = "";
                        } else {
                            if (strRtn[0].equalsIgnoreCase("Y")) {
                                // 第三個回傳值
                                strRtnResult = responseFormServer.substring(intPos + 1, responseFormServer.length() - intPos - 1 + intPos + 1);
                                // 若企業平台的"帳號尚未啟用(-1)"或"帳號不存在(-2)"時, 直接檢核平台身份
                                // If (strRtn(1) = "-1" Or strRtn(1) = "-2") Then
                                // strRtnResult = ""
                                // Else
                                // strRtnResult = strRtn(1) + strRtn(2)
                                // End If
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logContext.setRemark("CheckAuthentication Exception:" + ex.toString() + "; step=" + errorStep);
            rmService.logMessage(logContext, Level.INFO);
            strRtnResult = "驗證錯誤: 連接SSO主機失敗";
            return strRtnResult;
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(os);
            if (conn != null) {
                conn.disconnect();
            }
        }
        return strRtnResult;
    }

    /**
     * 資料庫查詢
     */
    protected void initDtResult() {

    }

    /**
     * 依查詢條件查詢的主程式 Bind 資料至 SyscomGridView 中
     */
    private void bindGridData(ModelMap mode) {
        WebUtil.putInAttribute(mode, AttributeName.DetailEntity, _dtResult);
    }

    // 設定畫面Execute Button的Event。
    @PostMapping(value = "/rm/UI_028200_1_Detail/btnConfirm")
    public String confirmClick(@ModelAttribute UI_028200_Form_2 form, ModelMap mode) {
        _LogContext.setRemark("ConfirmClick Start---------------------------");
        rmService.logMessage(_LogContext, Level.INFO);

        Boolean result = false;
        RMGeneral rmData = new RMGeneral();
        FCSHandler handler = new FCSHandler();
        _LogContext.setProgramName("UI_028200");
        try {
            // 暫時不反寫
            // if (!form.getRemDate().equals(form.getFcsRemDate())){
            // this.showMessage(mode,MessageType.WARNING,"匯款日期與檔案不符");
            // _LogContext.setRemark("REMDATE="+form.getRemDate()+", FCS_REMDATE="+form.getFcsRemDate());
            // rmService.logMessage(_LogContext,Level.INFO);
            // WebUtil.putInAttribute(mode, AttributeName.Form, form);
            // return Router.UI_028200_2_1_Detail.getView();
            // }else if (!form.getBatchNo().equals(form.getFcsBatchNo())){
            // this.showMessage(mode,MessageType.WARNING,"批號與檔案不符");
            // _LogContext.setRemark("BATCHNO="+form.getBatchNo()+", FCS_BATCHNO="+form.getFcsBatchNo());
            // rmService.logMessage(_LogContext,Level.INFO);
            // WebUtil.putInAttribute(mode, AttributeName.Form, form);
            // return Router.UI_028200_2_1_Detail.getView();
            // }else if (!form.getSupNo().equals(form.getFcsSupNo())){
            // this.showMessage(mode,MessageType.WARNING,"放行主管代號與檔案不符");
            // _LogContext.setRemark("SUPNO="+form.getSupNo()+", FCS_SUPNO="+form.getFcsSupNo());
            // rmService.logMessage(_LogContext,Level.INFO);
            // WebUtil.putInAttribute(mode, AttributeName.Form, form);
            // return Router.UI_028200_2_1_Detail.getView();
            // }else if (!form.getRemDate().equals(form.getFcsRemDate())){
            // this.showMessage(mode,MessageType.WARNING,"匯款日期與檔案不符");
            // _LogContext.setRemark("REMDATE="+form.getRemDate()+", FCS_REMDATE="+form.getFcsRemDate());
            // rmService.logMessage(_LogContext,Level.INFO);
            // WebUtil.putInAttribute(mode, AttributeName.Form, form);
            // return Router.UI_028200_2_1_Detail.getView();
            // }else if (!form.getTlrNo().equals(form.getFcsTlrNo())){
            // this.showMessage(mode,MessageType.WARNING,"登錄櫃員代號與檔案不符");
            // _LogContext.setRemark("TLRNO="+form.getTlrNo()+", FCS_TLRNO="+form.getFcsTlrNo());
            // rmService.logMessage(_LogContext,Level.INFO);
            // WebUtil.putInAttribute(mode, AttributeName.Form, form);
            // return Router.UI_028200_2_1_Detail.getView();
            // }

            _LogContext.setRemark("ConfirmClick, Check FCS File TLRNO & SUPNO OK!!!----");
            rmService.logMessage(_LogContext, Level.INFO);

            // Single Sign On Verify
            // if (checkSUPID()) checkSUPID()暫時不翻寫 預設為true
            _LogContext.setRemark("ConfirmClick, Check SUPNO OK!!!---------------------");
            rmService.logMessage(_LogContext, Level.INFO);

            rmData.getRequest().setKINBR("666");
            rmData.getRequest().setTRMSEQ("99");
            rmData.getRequest().setBRSNO("");
            rmData.getRequest().setENTTLRNO(WebUtil.getUser().getUserId());
            rmData.getRequest().setSUPNO1(form.getSupNo2());
            rmData.getRequest().setSUPNO2(form.getSupNo3());
            rmData.getRequest().setTBSDY(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            rmData.getRequest().setTIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));

            rmData.getRequest().setREMDATE(form.getRemDate());
            rmData.getRequest().setSENDTIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            rmData.getRequest().setBATCHNO(form.getBatchNo());

            result = handler.dispatch(FEPChannel.FEP, rmData, "R1001");
            _LogContext.setRemark("After Call R1001, result = " + result);
            rmService.logMessage(_LogContext, Level.INFO);
            String strMsg = form.getFileName();

            if (result) {
                this.showMessage(mode, MessageType.INFO, "傳送成功");
                strMsg = "傳送成功" + strMsg;
            } else {
                this.showMessage(mode, MessageType.INFO, "傳送失敗:" +
                        rmData.getResponse().getRsStatRsStateCode() + rmData.getResponse().getRsStatDesc());
                strMsg = "傳送成功" + rmData.getResponse().getRsStatRsStateCode() +
                        rmData.getResponse().getRsStatDesc() + "_" + strMsg;
            }
            prepareAndSendEMSData(strMsg);
            UI_028200_Form form1 = new UI_028200_Form();
            form1.setQueryTypeDdl("1");
            form1.setDatetime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
            queryClick(form1, mode);
            WebUtil.putInAttribute(mode, AttributeName.Form, form);
            return Router.UI_028200_1.getView();
        } catch (Exception ex) {
            _LogContext.setRemark("執行 ConfirmClick 時發生錯誤, REMDATE=" + form.getRemDate() + ",BATCHNO=" + form.getBatchNo());
            rmService.logMessage(_LogContext, Level.ERROR);
            this.showMessage(mode, MessageType.DANGER, ex);
            return Router.UI_028200_1_Detail.getView();
        }
    }

    /**
     * FTP
     */
    private FEPReturnCode ftpWork(String mask, String action, ModelMap mode) {
        String errorFile = "";
        try {
            // 下載檔案
            FtpAdapter ftpAp = WebUtil.getFromSession(SessionKey.TemporaryRestoreData);
            if (ftpAp == null) {
                FtpProperties ftpProperties = new FtpProperties();
                ftpProperties.setUsername(_FCSFTPUserId);
                ftpProperties.setPassword(_FCSFTPSscode);
                ftpProperties.setPort(_FCSFTPPort);
                ftpProperties.setHost(_FCSFTPServer);
                ftpProperties.setProtocol(FtpProtocol.SFTP);
                ftpAp = factory.createFtpAdapter(ftpProperties);
                WebUtil.putInSession(SessionKey.TemporaryRestoreData, ftpAp);
            }
            if ("Query".equals(action)) {
                _LogContext.setRemark("FTP Path=" + _FTPPath + ", File Mask=" + mask);
                rmService.logMessage(_LogContext, Level.INFO);
                _files = ftpAp.getFileList(_FCSInPath_FTP, mask);
                if (_files != null) {
                    _LogContext.setRemark("After GetFileList, files.length=" + _files.size());
                    rmService.logMessage(_LogContext, Level.INFO);
                    for (int i = 0; i < _files.size(); i++) {
                        //Fly 2020/10/06 Path Manipulation
                        //If (Path.IsPathRooted(_files(i))) Then
                        //                            Throw New ArgumentNullException("error")
                        //                        End If
                        // if下載不成功
                        if (!ftpAp.download(_FCSInPath + _files.get(i), _FTPPath + _files.get(i))) {
                            errorFile += _files.get(i) + ",";
                        }
                    }
                } else {
                    _LogContext.setRemark("After GetFileList, No File");
                    rmService.logMessage(_LogContext, Level.INFO);
                    this.showMessage(mode, MessageType.INFO, "FTP無檔案");
                }
            }
            if (!"".equals(errorFile)) {
                _LogContext.setRemark(errorFile + "下載錯誤");
                rmService.logMessage(_LogContext, Level.INFO);
                this.showMessage(mode, MessageType.DANGER, errorFile + "下載錯誤");
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            _LogContext.setRemark(errorFile + "UI_028200, FTPWork exception: " + ex.toString());
            rmService.logMessage(_LogContext, Level.INFO);
            return CommonReturnCode.ProgramException;
        }
    }

    private void bindResultGrdv2(UI_028200_Form form, ModelMap mode) {
        Rmbtchmtr defRmbtchmtr = new Rmbtchmtr();
        PageInfo<HashMap<String, Object>> dtTable;
        String txtStDate = StringUtils.replace(form.getDatetime(), "-", StringUtils.EMPTY);
        // Fly 2017/06/14 增加日期查詢條件
        defRmbtchmtr.setRmbtchmtrRemdate(txtStDate);
        try {

            dtTable = rmService.getRMBTCHMTRbyDef(defRmbtchmtr, form.getPageNum(), form.getPageSize());
            if (dtTable == null || dtTable.getList().size() == 0) {
                this.showMessage(mode, MessageType.DANGER, "大批匯款回饋主檔(RMBTCHMTR)無資料");
                return;
            }
            PageInfo<HashMap<String, Object>> dv = dtTable;
            // sql中已經排序 正序
            // If (Not String.IsNullOrEmpty(sortExpression)) Then
            // dv.Sort = sortExpression + " " + direction
            // ResultGrdv2.CustomSortExpression = sortExpression
            // ResultGrdv2.CustomSortDirection = GridViewSortDirection
            // End If
            PageData<UI_028200_Form, HashMap<String, Object>> pageData = new PageData<>(dv, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, ex.toString());
        }
    }

    private void bindResultGrdv3(UI_028200_Form form, ModelMap mode) {
        Rmbtch defRmbtch = new Rmbtch();
        PageInfo<HashMap<String, Object>> dtTable;
        // Fly 2017/06/14 增加日期查詢條件
        defRmbtch.setRmbtchTimes(form.getRmbtchTimes());
        defRmbtch.setRmbtchRemdate(form.getRmbtchRemDate());
        try {
            dtTable = rmService.getRMBTCHbyDef(defRmbtch, form.getPageNum(), form.getPageSize());
            if (dtTable == null || dtTable.getList().size() == 0) {
                this.showMessage(mode, MessageType.DANGER, "大批匯款回饋明細檔(RMBTCHMTR)無資料");
                return;
            }
            PageData<UI_028200_Form, HashMap<String, Object>> pageData = new PageData<>(dtTable, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, ex.toString());
        }
    }

    /**
     * 執行成功送成功信息
     */
    private void prepareAndSendEMSData(String strMsg) throws Exception {
        logContext.setChannel(FEPChannel.FEP);
        logContext.setSubSys(SubSystem.RM);
        logContext.setProgramName("UI_028200");
        logContext.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        logContext.setDesBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        logContext.setMessageId("UI028200");
        /* Rm */
        logContext.setMessageGroup("4");
        logContext.setRemark(strMsg);
        logContext.setTxUser(WebUtil.getUser().getUserId());
        logContext.setReturnCode(RMReturnCode.SendRMBTCHTele);
        TxHelper.getRCFromErrorCode(logContext.getReturnCode(), FEPChannel.FEP, logContext);
    }

//	private String formatDecimal(Object field) {
//		if ("".equals(field.toString())) {
//			return "";
//		} else {
//			return String.format("{0:n0}", Integer.parseInt(field.toString()));
//		}
//	}

    public String getHHMMSS(Date input) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss");
        return dateFormat.format(input);
    }
}