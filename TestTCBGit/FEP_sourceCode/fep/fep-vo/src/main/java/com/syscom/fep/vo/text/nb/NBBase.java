package com.syscom.fep.vo.text.nb;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.annotation.Field;

public class NBBase {
	@Field(length = 10)
    private String NOTICE_TYPE;
    
	@Field(length = 8)
    private BigDecimal TXNCHARGE;

	@Field(length = 12)
    private String ACTIVITY_TYPE;
    
	@Field(length = 14)
    private BigDecimal AVAILABLE_BALANCE;

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

	public BigDecimal getAVAILABLE_BALANCE() {
		return AVAILABLE_BALANCE;
	}

	public void setAVAILABLE_BALANCE(BigDecimal aVAILABLE_BALANCE) {
		AVAILABLE_BALANCE = aVAILABLE_BALANCE;
	}

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
	 * @param tita 電文物件
	 * @param deductCode 扣除幾位電碼，若不需要扣除電碼請放字串0
	 * @return
	 * @throws Exception 
	 */
	public String makeMessage(Object tita, String deductCode) throws Exception {
		Class<?> cls = tita.getClass();
		Class<?>[] params = {};
		// 取出bean裡的所有方法
		java.lang.reflect.Field[] fields = cls.getDeclaredFields();		
		String rtnTita = "";//電文字串
		String fieldType = "";
		String fieldSetName = "";
		String fieldGetName = "";
		Method fieldSetMet = null;
		Method fieldGetMet = null;
		int size = 0;
		Integer intval = null;
		Long longTemp = null;
		Double doubleTemp = null;
		Boolean booleanTemp = null;
		String stringTemp = "";
		for (java.lang.reflect.Field field : fields) {
			size = field.getAnnotation(Field.class).length();
			fieldSetName = "set" + field.getName();
			fieldGetName = "get" + field.getName();
			fieldSetMet = cls.getMethod(fieldSetName, field.getType());
			fieldGetMet = cls.getMethod(fieldGetName, params);
			fieldType = field.getType().getSimpleName();
            if ("String".equals(fieldType)) {  
                fieldSetMet.invoke(tita, StringUtils.rightPad(fieldGetMet.invoke(tita).toString(),size," "));
                rtnTita += fieldGetMet.invoke(tita).toString();
            } else if ("Integer".equals(fieldType) || "int".equals(fieldType)) {  
                intval = Integer.parseInt(fieldGetMet.invoke(tita).toString());  
                fieldSetMet.invoke(tita, StringUtils.rightPad(intval.toString(),size," "));  
                rtnTita += fieldGetMet.invoke(tita).toString();
            } else if ("Long".equalsIgnoreCase(fieldType)) {  
            	longTemp = Long.parseLong(fieldGetMet.invoke(tita).toString());  
                fieldSetMet.invoke(tita, StringUtils.rightPad(longTemp.toString(),size," "));
                rtnTita += fieldGetMet.invoke(tita).toString();
            } else if ("Double".equalsIgnoreCase(fieldType)) {  
            	doubleTemp = Double.parseDouble(fieldGetMet.invoke(tita).toString());  
                fieldSetMet.invoke(tita, StringUtils.rightPad(doubleTemp.toString(),size," ")); 
                rtnTita += fieldGetMet.invoke(tita).toString();
            } else if ("Boolean".equalsIgnoreCase(fieldType)) {  
            	booleanTemp = Boolean.parseBoolean(fieldGetMet.invoke(tita).toString());  
                fieldSetMet.invoke(tita, StringUtils.rightPad(booleanTemp.toString(),size," "));
                rtnTita += fieldGetMet.invoke(tita).toString();
            } else if ("BigDecimal".equalsIgnoreCase(fieldType)){
            	stringTemp = fieldGetMet.invoke(tita).toString();  
                //fieldSetMet.invoke(tita, new BigDecimal(StringUtils.rightPad(stringTemp,size," ")));
                fieldSetMet.invoke(tita, new BigDecimal(StringUtils.leftPad(stringTemp.toString(),size,"0")));
                rtnTita += fieldGetMet.invoke(tita).toString();           	
            } 
		}
		//依照不同下送的方式扣除電碼
		int num = Integer.parseInt(deductCode);
		if(num > 0) {
			rtnTita = rtnTita.substring(0, rtnTita.length() - num);
		}
		return rtnTita;
	}
}
