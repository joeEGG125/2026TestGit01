package com.syscom.fep.vo.communication;

import com.syscom.fep.frmcommon.util.XmlUtil;
import org.apache.commons.lang3.StringUtils;

public class BaseXmlCommu extends BaseCommu {

    @Override
    public String toString() {
        // 預設轉為XML字串
        return this.toXML();
    }

    /**
     * 1. 轉換成XML字串
     * 2. 若有壓縮則進行壓縮處理, 轉成壓縮後的hex字串
     *
     * @return
     */
    protected String toXML() {
        return serializedTo(serializedTo());
    }

    /**
     * 將序列化的XML字串轉為物件
     *
     * @param xml
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T extends BaseCommu> T fromXML(String xml) throws Exception {
        return fromXML(xml, null);
    }

    /**
     * 將序列化的XML字串轉為物件
     *
     * @param xml
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T extends BaseCommu> T fromXML(String xml, Class<T> clazz) throws Exception {
        return serializedFrom(xml, clazz, str -> XmlUtil.getChildElementValue(str, "classname", StringUtils.EMPTY), XmlUtil::fromXML);
    }

    /**
     * 是否對序列化後的字串, 轉為HEX字串
     *
     * @return
     */
    @Override
    protected boolean isSerializedToHex() {
        return isCompressed(); // 如果需要壓縮, 那肯定是要轉HEX的
    }

    /**
     * 是否對序列化的字串進行壓縮, 預設是false
     *
     * @return
     */
    @Override
    protected boolean isCompressed() {
        return false;
    }

    /**
     * 轉換成XML字串
     *
     * @return
     */
    public String serializedTo() {
        return XmlUtil.toXML(this);
    }
}