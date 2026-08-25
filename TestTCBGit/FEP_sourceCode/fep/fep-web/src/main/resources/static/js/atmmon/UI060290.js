var formId = "form-validator";

$(document).ready(function() {
	
	// 按下查詢按鈕
	$('#btnConfirm').click(function() {
		if (doValidateForm(formId)) {
			removeAttrDisabled(formId);
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});	
	
})

//選取全部
			function useAll() {

			};

			//全選點選函式
			$('#useCheck').click(function() {
				$(":checkbox").prop("checked", $(this).prop("checked"));
				CheckIntra(false);
				CheckAgent(false);
				CheckIssue(false);
				CheckPure(false);
			});

			//公用陣列設定
			//自行欄位20
			//2016/02/15 Added By Nick SU For ARPC
			//2017/01/25 Added By Nick SU For 無卡提款
			//1-1自行行為
			var IntraArray = new Array("sysstatIwdI1", "sysstatIftI1",
					"sysstatAdmI1", "sysstatIiqI1", "sysstatFwdI1",
					"sysstatNwdI1", "sysstatNfwI1", "sysstatIpyI1",
					"sysstatIccdpI1", "sysstatEtxI1", "sysstatCaI1",
					"sysstatCaaI1", "sysstatAig1", "sysstatHkIssue1",
					"sysstatHkFiscmb1", "sysstatHkFiscmq1", "sysstatHkPlus1",
					"sysstatMoIssue1", "sysstatMoFiscmb1", "sysstatMoFiscmq1",
					"sysstatMoPlus1");
			//var IntraArray = new Array("IWD_I", "IFT_I", "ADM_I", "IIQ_I", "FWD_I", "IPY_I", "ICCDP_I", "ETX_I", "CA_I", "CAA_I", "AIG", "HK_ISSUE", "HK_FISCMB", "HK_FISCMQ", "HK_PLUS", "MO_ISSUE", "MO_FISCMB", "MO_FISCMQ", "MO_PLUS");
			//代理行欄位21
			//ChenLi, 2015/03/02, 加入代理外幣提款控制
			//2016/02/15 Added By Nick SU For ARPC
			//2016/07/19 Added By Nick SU For EMV
			//2016/08/12 Added By Nick SU For 跨行存款
			//2016/10/20 Moidfy by Nick for 剔除代理行JCB預借現金交易
			//2017/01/25 Moidfy by Nick for 無卡跨行提款
			var AgentArray = new Array("sysstatIwdA1", "sysstatFawA1",
					"sysstatIftA1", "sysstatAdmA1", "sysstatIpyA1",
					"sysstatIccdpA1", "sysstatCdpA1", "sysstatEtxA1",
					"sysstat2525A1", "sysstatCpuA1", "sysstatCavA1",
					"sysstatCamA1", "sysstatCauA1", "sysstatCwvA1",
					"sysstatCwmA1", "sysstatCafA1", "sysstatEafA1",
					"sysstatEwvA1", "sysstatEwmA1", "sysstatEavA1",
					"sysstatEamA1", "sysstatNwdA1", "sysstatVaaA1");
			//var AgentArray = new Array("IWD_A", "FAW_A", "IFT_A", "ADM_A", "IPY_A", "ICCDP_A", "CDP_A", "ETX_A", "2525_A", "CPU_A", "CAV_A", "CAM_A", "CAU_A", "CWV_A", "CWM_A", "CAF_A", "EAF_A", "EWV_A", "EWM_A", "EAV_A", "EAM_A");
			//var AgentArray = new Array("IWD_A", "FAW_A", "IFT_A", "ADM_A", "IPY_A", "ICCDP_A", "CDP_A", "ETX_A", "2525_A", "CPU_A", "CAV_A", "CAM_A", "CAJ_A", "CAU_A", "CWV_A", "CWM_A", "CAF_A", "EAF_A", "EWV_A", "EWM_A", "EAV_A", "EAM_A");
			//原存欄位13
			//2016/02/15 Added By Nick SU For ARPC
			//2016/04/19 Modify By Nick SU for 原存EMV Plus/Cirrus提款
			var IssueArray = new Array("sysstatIwdF1", "sysstatIftF1",
					"sysstatIiqF1", "sysstatIpyF1", "sysstatIccdpF1",
					"sysstatCdpF1", "sysstatEtxF1", "sysstat2525F1",
					"sysstatCpuF1", "sysstatCdpF1", "sysstatGpcwdF1",
					"sysstatGpemvF1", "sysstatGpcadF1", "sysstatGpiwdF1",
					"sysstatGpobF1", "sysstatVaaF1","sysstatCauF1");
			//純代理欄位11
			//2016/02/15 Added By Nick SU For ARPC
			//2016/07/26 Added By Nick SU For EMV
			var PureArray = new Array("sysstatIiqP1", "sysstatIftP1",
					"sysstatIccdpP1", "sysstatCdpP1", "sysstatIpyP1",
					"sysstatIqvP1", "sysstatIqmP1", "sysstatIqcP1",
					"sysstatEquP1", "sysstatEqpP1", "sysstatEqcP1");

			function CheckIntra(isFirst) {
				for (var i = 0; i <= IntraArray.length - 1; i++) {
					if (document.getElementById("sysstatIntra1").checked) {
						document.getElementById(IntraArray[i]).disabled = false;
						if (!isFirst) {
							document.getElementById(IntraArray[i]).checked = true;
						}
					} else {
						document.getElementById(IntraArray[i]).disabled = true;
						document.getElementById(IntraArray[i]).checked = false;
					}

				}
			}

			//1-2代理行行為
			function CheckAgent(isFirst) {
				for (var i = 0; i <= AgentArray.length - 1; i++) {
					if (document.getElementById("sysstatAgent1").checked) {
						document.getElementById(AgentArray[i]).disabled = false;
						if (!isFirst) {
							document.getElementById(AgentArray[i]).checked = true;
						}
					} else {
						document.getElementById(AgentArray[i]).disabled = true;
						document.getElementById(AgentArray[i]).checked = false;
					}

				}
			}
			//1-3原存行為
			function CheckIssue(isFirst) {
				for (var i = 0; i <= IssueArray.length - 1; i++) {
					if (document.getElementById("sysstatIssue1").checked) {
						document.getElementById(IssueArray[i]).disabled = false;
						if (!isFirst) {
							document.getElementById(IssueArray[i]).checked = true;
						}
					} else {
						document.getElementById(IssueArray[i]).disabled = true;
						document.getElementById(IssueArray[i]).checked = false;
					}

				}
			}
			//1-4純代理行為
			function CheckPure(isFirst) {
				for (var i = 0; i <= PureArray.length - 1; i++) {
					if (document.getElementById("sysstatPure1").checked) {
						document.getElementById(PureArray[i]).disabled = false;
						if (!isFirst) {
							document.getElementById(PureArray[i]).checked = true;
						}
					} else {
						document.getElementById(PureArray[i]).disabled = true;
						document.getElementById(PureArray[i]).checked = false;
					}
				}
			}