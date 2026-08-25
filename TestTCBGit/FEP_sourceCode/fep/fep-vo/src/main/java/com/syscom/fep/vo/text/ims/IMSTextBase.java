package com.syscom.fep.vo.text.ims;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Objects;

import com.syscom.fep.vo.CodeGenUtil;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.annotation.Field;

public class IMSTextBase {

	@Field(length = 10)
	private String NOTICE_TYPE;

	@Field(length = 8)
	private BigDecimal TXNCHARGE;

	@Field(length = 12)
	private String ACTIVITY_TYPE;

//	@Field(length = 14)
//	private String AVAILABLE_BALANCE;

	@Field(length = 16)
	private BigDecimal ACT_BALANCE;

	@Field(length = 18)
	private String LUCKYNO;

	public String getNOTICE_TYPE() {
		return NOTICE_TYPE;
	}

	public void setNOTICE_TYPE(String nOTICE_TYPE) {
		NOTICE_TYPE = nOTICE_TYPE;
	}

	public BigDecimal getTXNCHARGE() {
		return TXNCHARGE;
	}

	public void setTXNCHARGE(BigDecimal tXNCHARGE) {
		TXNCHARGE = tXNCHARGE;
	}

	public String getACTIVITY_TYPE() {
		return ACTIVITY_TYPE;
	}

	public void setACTIVITY_TYPE(String aCTIVITY_TYPE) {
		ACTIVITY_TYPE = aCTIVITY_TYPE;
	}

//	public String getAVAILABLE_BALANCE() {
//		return AVAILABLE_BALANCE;
//	}
//
//	public void setAVAILABLE_BALANCE(String aVAILABLE_BALANCE) {
//		AVAILABLE_BALANCE = aVAILABLE_BALANCE;
//	}

	public BigDecimal getACT_BALANCE() {
		return ACT_BALANCE;
	}

	public void setACT_BALANCE(BigDecimal aCT_BALANCE) {
		ACT_BALANCE = aCT_BALANCE;
	}

	public String getLUCKYNO() {
		return LUCKYNO;
	}

	public void setLUCKYNO(String lUCKYNO) {
		LUCKYNO = lUCKYNO;
	}

//	public static void main(String[] args) {
//
//		    IMSTextBase ttt = new IMSTextBase();
//		    ttt.setNOTICE_TYPE("michael");
//		    ttt.setTXNCHARGE(new BigDecimal("27"));
//		    ttt.setACTIVITY_TYPE("173.5");
//		    ttt.setAVAILABLE_BALANCE(new BigDecimal("28"));
//		    ttt.setACT_BALANCE(new BigDecimal("29"));
//		    ttt.setLUCKYNO("測試");
//		   // IMSTextBase test = (IMSTextBase) IMSTextBase.setFieldValue(new IMSTextBase(),valMap);
//		    try {
//				String tita = IMSTextBase.makeMessage(ttt);
//				System.out.println(tita);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//            System.out.println("ttt");
//		 }

	/**
	 * 20221005 Bruce add 動態取得任意物件屬性的值並依長度回傳字串電文
	 *
	 * @param tita       電文物件
	 * @param deductCode 扣除幾位電碼，若不需要扣除電碼請放字串0
	 * @return
	 * @throws Exception
	 */
	public String makeMessage(Object tita, String deductCode) throws Exception {
		Class<?> cls = tita.getClass();
		Class<?>[] params = {};
		// 取出bean裡的所有方法
		java.lang.reflect.Field[] fields = cls.getDeclaredFields();
		String rtnTita = "";// 電文字串
		String fieldType = "";
		String fieldSetName = "";
		String fieldGetName = "";
		Method fieldSetMet = null;
		Method fieldGetMet = null;
		int size = 0;
		String value = null;
		for (java.lang.reflect.Field field : fields) {
			if ("_TotalLength".equals(field.getName())) {
				continue;
			}
			if(field.getAnnotation(Field.class) != null) {
				size = field.getAnnotation(Field.class).length();
			}
			fieldSetName = "set" + field.getName();
			fieldGetName = "get" + field.getName();
			fieldSetMet = cls.getDeclaredMethod(fieldSetName, field.getType());
			fieldGetMet = cls.getDeclaredMethod(fieldGetName, params);
			fieldType = field.getType().getSimpleName();
			value = Objects.toString(fieldGetMet.invoke(tita), "");
			if (StringUtils.isNotBlank(value)) {
				if ("Integer".equals(fieldType) || "int".equals(fieldType)) {
					value = Objects.toString(Integer.parseInt(value), "");
				} else if ("Long".equalsIgnoreCase(fieldType)) {
					value = Objects.toString(Long.parseLong(value), "");
				} else if ("Double".equalsIgnoreCase(fieldType)) {
					value = Objects.toString(Double.parseDouble(value), "");
				} else if ("Boolean".equalsIgnoreCase(fieldType)) {
					value = Objects.toString(Boolean.parseBoolean(value), "");
				}
			}
			if ("BigDecimal".equalsIgnoreCase(fieldType)) {
				fieldSetMet.invoke(tita, new BigDecimal(Objects.toString(StringUtils.trimToNull(value), "0")));
				rtnTita += StringUtils.leftPad(Objects.toString(fieldGetMet.invoke(tita)), size, "0");
			} else {
				fieldSetMet.invoke(tita, StringUtils.rightPad(Objects.toString(value, ""), size, " "));
				rtnTita += fieldGetMet.invoke(tita).toString();
			}
		}
		// 依照不同下送的方式扣除電碼
		int num = Integer.parseInt(deductCode);
		if (num > 0) {
			if(rtnTita.length() > num) {
				rtnTita = rtnTita.substring(0, rtnTita.length() - num);
			}
		}
		return rtnTita;
	}
	protected String addOEOFasciiToEbcdic(String str,Integer len){
		if (str==null ||StringUtils.isBlank(str)){
			return CodeGenUtil.asciiToEbcdicDefaultEmpty(str, len);
		}else {
			return "0E"+CodeGenUtil.asciiToEbcdicDefaultEmpty(str, len-4)+"0F";
		}
	}
}
