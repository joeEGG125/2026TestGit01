package com.syscom.fep.web.controller.rm;

import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Clrdtl;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028290_Form;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Calendar;
import java.util.List;

/**
 * 調整匯款暫禁記號
 *
 * @author xingyun_yang
 * @create 2021/11/30
 */
@Controller
public class UI_028290Controller extends BaseController {
	@Autowired
	RmService rmService;
	@Autowired
	AtmService atmService;
	private String _ShowMessage;
	private String _SYSSTAT_HBKNO;

	@Override
	public void pageOnLoad(ModelMap mode) {
		UI_028290_Form form = new UI_028290_Form();
		this.setExecuteBtnEnabled(form);
		querySYSSTAT(mode);
		rMSTATGetAndBind(form, mode);
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}
	
	private void setExecuteBtnEnabled(UI_028290_Form form) {
		String webType = WebConfiguration.getInstance().getWebType();
		if (webType.equals("SSTQ")) {
			form.setExecuteBtnEnabled("false");
		} else {
			form.setExecuteBtnEnabled("true");
		}
	}

	private void querySYSSTAT(ModelMap mode) {
		try {
			Sysstat _dtSYSSTAT = atmService.getStatus();
			if (_dtSYSSTAT == null) {
				this.showMessage(mode, MessageType.DANGER, "SYSSTAT無資料!!");
				return;
			}
			_SYSSTAT_HBKNO = _dtSYSSTAT.getSysstatTbsdyFisc();
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
	}

	@PostMapping(value = "/rm/UI_028290/executeBtn_Click")
	public String executeBtn_Click(@ModelAttribute UI_028290_Form form, ModelMap mode) {
		FEPReturnCode rtnCode;
		this.setExecuteBtnEnabled(form);
		rtnCode = rMSTATUpdate(form, mode);
		if (rtnCode.equals(CommonReturnCode.Normal)) {
			this.showMessage(mode, MessageType.INFO, _ShowMessage);
		} else {
			this.showMessage(mode, MessageType.DANGER, _ShowMessage);
		}
		return Router.UI_028290.getView();
	}

	private FEPReturnCode rMSTATUpdate(UI_028290_Form form, ModelMap mode) {
		Clrdtl _defCLRDTL = new Clrdtl();
		String strMsg = "";
		try {
			_ShowMessage = StringUtils.EMPTY;
			_defCLRDTL.setClrdtlTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
			_defCLRDTL.setClrdtlApId("10000");
			_defCLRDTL.setClrdtlPaytype(" ");
			_defCLRDTL.setClrdtlRmstat(form.getDdlRM_STAT());

			switch (_defCLRDTL.getClrdtlRmstat()) {
				case "Y":
					if ("Y:匯出".equals(form.getrMFLAGTxt())) {
						_ShowMessage = "暫禁記號已為Y";
						return FEPReturnCode.Abnormal;
					}
					break;
				case "N":
					if ("N:暫停".equals(form.getrMFLAGTxt())) {
						_ShowMessage = "暫禁記號已為N";
						return FEPReturnCode.Abnormal;
					}
					break;
				default:
					break;
			}
			if (rmService.updateCLRDTLByPK(_defCLRDTL) < 0) {
				strMsg = "匯出暫禁記號更新失敗";
				_ShowMessage = UpdateFail;
				return IOReturnCode.FCRMSTATUpdateError;
			} else {
				rMSTATGetAndBind(form, mode);
				strMsg = String.format("調整匯出暫禁記號＝'{0}'調整成功", form.getrMFLAGTxt());
			}
			prepareAndSendEMSData(strMsg);
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			_ShowMessage = ex.toString();
			return CommonReturnCode.ProgramException;
		}

		_ShowMessage = DealSuccess;
		return CommonReturnCode.Normal;
	}

	private void rMSTATGetAndBind(UI_028290_Form form, ModelMap mode) {
		Clrdtl _defCLRDTL = new Clrdtl();
		try {
			_defCLRDTL.setClrdtlTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
			_defCLRDTL.setClrdtlApId("10000");
			_defCLRDTL.setClrdtlPaytype(" ");
			List<Clrdtl> dbClr = rmService.getCLRDTLByPK(_defCLRDTL);
			if (dbClr.size() > 0) {
				switch (dbClr.get(0).getClrdtlRmstat()) {
					case "Y":
						form.setrMFLAGTxt("Y:匯出");
						break;
					case "N":
						form.setrMFLAGTxt("N:暫停");
						break;
					case "R":
						form.setrMFLAGTxt("R:低於第二水位");
						break;
					default:
						break;
				}
			} else {
				_ShowMessage = QueryFail;
			}
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			_ShowMessage = ex.toString();
		} finally {
			WebUtil.putInAttribute(mode, AttributeName.Form, form);
		}
	}

	/**
	 * 執行成功送成功信息
	 */
	private void prepareAndSendEMSData(String strMsg) throws Exception {
		logContext.setChannel(FEPChannel.FEP);
		logContext.setSubSys(SubSystem.RM);
		logContext.setProgramName("UI_028290");
		logContext.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
		logContext.setDesBkno(_SYSSTAT_HBKNO);
		logContext.setMessageId("UI028290");
		/* Rm */
		logContext.setMessageGroup("4");
		logContext.setRemark(strMsg);
		logContext.setTxUser(WebUtil.getUser().getUserId());
		logContext.setReturnCode(RMReturnCode.ChangeCLRRMStatus);
		TxHelper.getRCFromErrorCode(logContext.getReturnCode(), FEPChannel.FEP, logContext);
	}
}
