package com.syscom.fep.web.controller.rm;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.mybatis.model.Allbank;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_020060_Form;
import com.syscom.fep.web.form.rm.UI_020060_FormDetail;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 往來行庫資料查詢
 *
 * @author xingyun_yang
 * @create 2021/11/18
 */
@Controller
public class UI_020060Controller extends BaseController {

	@Autowired
	RmService rmService;

	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單數據
		UI_020060_Form form = new UI_020060_Form();
        form.setAllbankBkno("");
        form.setAllbankBrno("");
		form.setUrl("/rm/UI_020060/queryClick");
		this.queryClick(form, mode);
	}

	@PostMapping(value = "/rm/UI_020060/queryClick")
	private String queryClick(@ModelAttribute UI_020060_Form form, ModelMap mode) {
		this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		query(form, mode);
		return Router.UI_020060.getView();
	}

	/**
	 * 依查詢條件查詢的主程式。
	 */
	private void query(UI_020060_Form form, ModelMap mode) {
		this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		try {
			Allbank defAllbank = new Allbank();
			if (StringUtils.isNotBlank(form.getAllbankBkno().trim())) {
				defAllbank.setAllbankBkno(form.getAllbankBkno().trim());
			}
			if (StringUtils.isNotBlank(form.getAllbankBrno().trim())) {
				defAllbank.setAllbankBrno(form.getAllbankBrno().trim());
			}
			PageInfo<Allbank> pageInfo = PageHelper.startPage(form.getPageNum(), form.getPageSize(), form.getPageNum() > 0 && form.getPageSize() > 0).doSelectPageInfo(new ISelect() {
				@Override
				public void doSelect() {
					rmService.queryALLBANKByPKLike(defAllbank);
				}
			});
			if (pageInfo.getSize() == 0) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
				return;
			}
			PageData<UI_020060_Form, Allbank> pageData = new PageData<>(pageInfo, form);
			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
		}
	}

	@PostMapping(value = "/rm/UI_020060/showDetial")
	private String showDetial(@ModelAttribute UI_020060_FormDetail form, ModelMap mode) {
		this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
		return Router.UI_020060_Detail.getView();
	}

	/**
	 * 檢核資料正確後，即送入defALLBANK中，以供後續新增或修改之用 vb.net 未對其引用，所以未反寫
	 */
	// Private Function CheckAllField(ByRef defALLBANK As Tables.DefALLBANK) As Boolean
	// Dim strTemp As String
	// Dim intMaxLen As Integer
	//
	// Try
	// defALLBANK.ALLBANK_BKNO = CType(Me.EditFormFrmv.FindControl("ALLBANK_BKNO1Txt"), TextBox).Text
	// defALLBANK.ALLBANK_BRNO = CType(Me.EditFormFrmv.FindControl("ALLBANK_BRNO1Txt"), TextBox).Text
	// defALLBANK.ALLBANK_FISC_UNIT = Convert.ToByte(CType(Me.EditFormFrmv.FindControl("ALLBANK_FISC_UNITDdl"), DropDownList).SelectedValue)
	// defALLBANK.ALLBANK_UNIT_BANK = CType(Me.EditFormFrmv.FindControl("ALLBANK_UNIT_BANKTxt"), TextBox).Text
	//
	// strTemp = CType(Me.EditFormFrmv.FindControl("ALLBANK_ALIASNAMETxt"), TextBox).Text
	// intMaxLen = CType(Me.EditFormFrmv.FindControl("ALLBANK_ALIASNAMETxt"), TextBox).MaxLength
	// If Not String.IsNullOrEmpty(strTemp) Then
	// If Encoding.GetEncoding("Big5").GetByteCount(strTemp) > intMaxLen Then
	// EditStatusBar.ShowMessage("銀行之簡稱" & LenthMoreThenMax.Replace("MAX", intMaxLen.ToString()), StatusBar.MessageType.ErrMsg)
	// Return False
	// Else
	// defALLBANK.ALLBANK_ALIASNAME = strTemp
	// End If
	// End If
	//
	// strTemp = CType(Me.EditFormFrmv.FindControl("ALLBANK_FULLNAMETxt"), TextBox).Text
	// intMaxLen = CType(Me.EditFormFrmv.FindControl("ALLBANK_FULLNAMETxt"), TextBox).MaxLength
	// If Not String.IsNullOrEmpty(strTemp) Then
	// If Encoding.GetEncoding("Big5").GetByteCount(strTemp) > intMaxLen Then
	// EditStatusBar.ShowMessage("銀行之全名" & LenthMoreThenMax.Replace("MAX", intMaxLen.ToString()), StatusBar.MessageType.ErrMsg)
	// Return False
	// Else
	// defALLBANK.ALLBANK_FULLNAME = strTemp
	// End If
	// End If
	//
	// strTemp = CType(Me.EditFormFrmv.FindControl("ALLBANK_ENGNAMETxt"), TextBox).Text
	// intMaxLen = CType(Me.EditFormFrmv.FindControl("ALLBANK_ENGNAMETxt"), TextBox).MaxLength
	// If Not String.IsNullOrEmpty(strTemp) Then
	// If Encoding.GetEncoding("Big5").GetByteCount(strTemp) > intMaxLen Then
	// EditStatusBar.ShowMessage("銀行之英文名稱" & LenthMoreThenMax.Replace("MAX", intMaxLen.ToString()), StatusBar.MessageType.ErrMsg)
	// Return False
	// Else
	// defALLBANK.ALLBANK_ENGNAME = strTemp
	// End If
	// End If
	//
	// strTemp = CType(Me.EditFormFrmv.FindControl("ALLBANK_ADDRRESSTxt"), TextBox).Text
	// intMaxLen = CType(Me.EditFormFrmv.FindControl("ALLBANK_ADDRRESSTxt"), TextBox).MaxLength
	// If Not String.IsNullOrEmpty(strTemp) Then
	// If Encoding.GetEncoding("Big5").GetByteCount(strTemp) > intMaxLen Then
	// EditStatusBar.ShowMessage("銀行之地址" & LenthMoreThenMax.Replace("MAX", intMaxLen.ToString()), StatusBar.MessageType.ErrMsg)
	// Return False
	// Else
	// defALLBANK.ALLBANK_ADDRRESS = strTemp
	// End If
	// End If
	//
	// strTemp = CType(Me.EditFormFrmv.FindControl("ALLBANK_ZIPCODE1Txt"), TextBox).Text
	// intMaxLen = CType(Me.EditFormFrmv.FindControl("ALLBANK_ZIPCODE1Txt"), TextBox).MaxLength
	// If Not String.IsNullOrEmpty(strTemp) Then
	// If Encoding.GetEncoding("Big5").GetByteCount(strTemp) > intMaxLen Then
	// EditStatusBar.ShowMessage("郵遞區號1" & LenthMoreThenMax.Replace("MAX", intMaxLen.ToString()), StatusBar.MessageType.ErrMsg)
	// Return False
	// Else
	// defALLBANK.ALLBANK_ZIPCODE1 = strTemp
	// End If
	// End If
	//
	// strTemp = CType(Me.EditFormFrmv.FindControl("ALLBANK_ZIPCODE2Txt"), TextBox).Text
	// intMaxLen = CType(Me.EditFormFrmv.FindControl("ALLBANK_ZIPCODE2Txt"), TextBox).MaxLength
	// If Not String.IsNullOrEmpty(strTemp) Then
	// If Encoding.GetEncoding("Big5").GetByteCount(strTemp) > intMaxLen Then
	// EditStatusBar.ShowMessage("郵遞區號2" & LenthMoreThenMax.Replace("MAX", intMaxLen.ToString()), StatusBar.MessageType.ErrMsg)
	// Return False
	// Else
	// defALLBANK.ALLBANK_ZIPCODE2 = strTemp
	// End If
	// End If
	//
	// strTemp = CType(Me.EditFormFrmv.FindControl("ALLBANK_TELNOTxt"), TextBox).Text
	// intMaxLen = CType(Me.EditFormFrmv.FindControl("ALLBANK_TELNOTxt"), TextBox).MaxLength
	// If Not String.IsNullOrEmpty(strTemp) Then
	// If Encoding.GetEncoding("Big5").GetByteCount(strTemp) > intMaxLen Then
	// EditStatusBar.ShowMessage("電話" & LenthMoreThenMax.Replace("MAX", intMaxLen.ToString()), StatusBar.MessageType.ErrMsg)
	// Return False
	// Else
	// defALLBANK.ALLBANK_TELNO = strTemp
	// End If
	// End If
	//
	// defALLBANK.ALLBANK_RMFLAG = CType(Me.EditFormFrmv.FindControl("ALLBANK_RMFLAGDdl"), DropDownList).SelectedValue
	// defALLBANK.ALLBANK_RMFORWARD = CType(Me.EditFormFrmv.FindControl("ALLBANK_RMFORWARDDdl"), DropDownList).SelectedValue
	//
	// strTemp = CType(Me.EditFormFrmv.FindControl("ALLBANK_SET_CLOSE_TIMETime"), TimeTextBox).Text.Replace(":", "")
	// If Not String.IsNullOrEmpty(strTemp) Then
	// defALLBANK.ALLBANK_SET_CLOSE_TIME = Convert.ToDecimal(strTemp)
	// End If
	//
	// defALLBANK.ALLBANK_SET_CLOSE_FLAG = CType(Me.EditFormFrmv.FindControl("ALLBANK_SET_CLOSE_FLAGDdl"), DropDownList).SelectedValue
	// defALLBANK.ALLBANK_TYPE = CType(Me.EditFormFrmv.FindControl("ALLBANK_TYPEDdl"), DropDownList).SelectedValue
	//
	// SetALLBANK_BRNO_CHKCODE()
	// defALLBANK.ALLBANK_BRNO_CHKCODE = CType(Me.EditFormFrmv.FindControl("ALLBANK_BRNO_CHKCODETxt"), TextBox).Text
	//
	// defALLBANK.UPDATE_USERID = CType(Session("ID"), Tables.DefFEPUSER).FEPUSER_USERID
	// Return True
	//
	// Catch ex As Exception
	// EditStatusBar.ShowMessage(ex.ToString, StatusBar.MessageType.ErrMsg)
	// Return False
}
