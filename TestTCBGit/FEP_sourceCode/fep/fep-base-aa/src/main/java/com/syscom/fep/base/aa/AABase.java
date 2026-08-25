package com.syscom.fep.base.aa;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Feptxntcb;
import com.syscom.fep.mybatis.model.Inbk2160;

public abstract class AABase extends FEPBase {
    /**
     * 處理Feptxn開頭的DAO類，需要注意的是，在使用前一定要透過setTableNameSuffix方法塞入Feptxn開頭的表的後綴名，否則會有問題或者塞入數據到錯誤的表格
     */
    protected FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");

    /**
     * 與交易關聯的FEPTxn物件
     */
    protected Feptxn feptxn;
    
    /**
     *   TCB report 保留資料
     */
    protected Feptxntcb feptxntcb; //TCB report 保留資料

    /**
     * 與交易關聯的INBK2160物件
     */
    protected Inbk2160 inbk2160;

    /**
     * DB存儲的AA程式名稱, default等於程式的名稱, 在各自的AABase的Construct中再賦值
     */
    protected String aaName = ProgramName;

    /**
     * 處理電文
     *
     * @return
     */
    public abstract String processRequestData() throws Exception;

    public Feptxn getFeptxn() {
        return feptxn;
    }

    public void setFeptxn(Feptxn feptxn) {
        this.feptxn = feptxn;
    }

    public Inbk2160 getInbk2160() {
        return inbk2160;
    }

    public void setInbk2160(Inbk2160 inbk2160) {
        this.inbk2160 = inbk2160;
    }

    protected FEPReturnCode handleException(Exception e, String methodName) {
        getLogContext().setProgramName(StringUtils.join(this.aaName, ".", methodName));
        getLogContext().setProgramException(e);
        sendEMS(getLogContext());
        return FEPReturnCode.ProgramException;
    }
    
    /**
     * 20221007 Bruce add 動態取得ims電文物件
     * @param cbsProcessName
     * @return
     * @throws Exception
     */
	public Object getInstanceObject(String cbsProcessName,MessageBase atmData){
		Class<?> c = null;
		//java.lang.reflect.Field[] fields = null;
		//String imsPackageName = "";
		//Class<?> imsClassName = null;
		Class<?>[] params = {MessageBase.class};
		Object o = null;
		try {
			//獲得cbsProcessn物件名稱
			c = Class.forName("com.syscom.fep.server.common.cbsprocess."+cbsProcessName);
			//獲得cbsProcessn物件裡所有欄位名稱
			//fields = c.getDeclaredFields();
			//使用cbsProcessn以獲得ims電文物件名稱
			//imsPackageName = fields[0].toString().split(" ")[1];	
			//獲得ims電文物件名稱
			//imsClassName = Class.forName(imsPackageName);
			//獲得ims實例化電文物件
			o = c.getConstructor(params).newInstance(atmData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return o;
	}
	
	/**
	 * 20221216 Bruce add 取得需要實例化的ims物件名稱
	 * @param cbsprocess
	 * @return
	 */
	public String findAllFilesInFolder(String cbsProcessName) {
		String sFolder = "../fep-vo/src/main/java/com/syscom/fep/vo/text/ims";
        File folder = new File(sFolder);
		String imsCbsProcess = "";
		String className = "";
		for (File file : folder.listFiles()) {
			className = file.getName().split("\\.")[0].replace("_", "");
			//System.out.println(file.getName().split("\\.")[0].replace("_", ""));
			if (!file.isDirectory()) {
				if (file.getName().endsWith("java") && className.equals(cbsProcessName)) {
					imsCbsProcess = file.getName().split("\\.")[0];
					break;
                }
			}
		}
		return imsCbsProcess;
	}
}
