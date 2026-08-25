package com.syscom.fep.vo.text.fcs;

import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * R1001_整批匯款格式檔首 FCSHead
 */
public class FCSHead extends FCSDataBase {
	// 匯款日期
	private String remDate = "";

	// 批號
	private String times = "";

	// 登錄行
	private String kinBrno = "";

	// 櫃員代號
	private String tlrNo = "";

	// 主管代號1
	private String supNo1 = "";

	// 主管代號2
	private String supNo2 = "";

	// 處理狀態錯誤原因
	private String fepRc = "";

	// 錯誤訊息
	private String errMsg = "";

	private static final int totalLength = 147;

	public String getRemDate() {
		return remDate;
	}

	public void setRemDate(String remDate) {
		this.remDate = remDate;
	}

	public String getTimes() {
		return times;
	}

	public void setTimes(String times) {
		this.times = times;
	}

	public String getKinBrno() {
		return kinBrno;
	}

	public void setKinBrno(String kinBrno) {
		this.kinBrno = kinBrno;
	}

	public String getTlrNo() {
		return tlrNo;
	}

	public void setTlrNo(String tlrNo) {
		this.tlrNo = tlrNo;
	}

	public String getSupNo1() {
		return supNo1;
	}

	public void setSupNo1(String supNo1) {
		this.supNo1 = supNo1;
	}

	public String getSupNo2() {
		return supNo2;
	}

	public void setSupNo2(String supNo2) {
		this.supNo2 = supNo2;
	}

	public String getFepRc() {
		return fepRc;
	}

	public void setFepRc(String fepRc) {
		this.fepRc = fepRc;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	/**
	 * Header電文總長度
	 * 
	 * @return 該組Header電文總長度
	 */
	public static BigDecimal getTotalLength() {
		return new BigDecimal(totalLength);
	}

	public String merge() {
		StringBuilder rtn = new StringBuilder("");
		rtn.append(StringUtils.rightPad(getRemDate(), 8));
		rtn.append(StringUtils.rightPad(getTimes(), 32));
		rtn.append(StringUtils.rightPad(getRemDate(), 3));
		rtn.append(StringUtils.rightPad(getTlrNo(), 6));
		rtn.append(StringUtils.rightPad(getSupNo1(), 6));
		rtn.append(StringUtils.rightPad(getSupNo2(), 6));
		rtn.append(StringUtils.rightPad(getFepRc(), 6));
		rtn.append(StringUtils.rightPad(getErrMsg(), 80));
		return rtn.toString();
	}

	public int parse(String sFileHeaderLine) {
		int tmpLen = ConvertUtil.toBytes(sFileHeaderLine, PolyfillUtil.toCharsetName("big5")).length;

		if (tmpLen < getTotalLength().intValue()) {
			sFileHeaderLine = sFileHeaderLine + PolyfillUtil.space(getTotalLength().intValue() - tmpLen);
		}
		setRemDate(subStr(sFileHeaderLine, 0, 8).trim());
		setTimes(subStr(sFileHeaderLine, 8, 32).trim());
		setKinBrno(subStr(sFileHeaderLine, 40, 3).trim());
		setTlrNo(subStr(sFileHeaderLine, 43, 6).trim());
		setSupNo1(subStr(sFileHeaderLine, 49, 6).trim());
		setSupNo2(subStr(sFileHeaderLine, 55, 6).trim());
		setFepRc(subStr(sFileHeaderLine, 61, 6).trim());
		setErrMsg(subStr(sFileHeaderLine, 67, 80).trim());
		return 0;
	}
}
