//
// 此檔案是由 Eclipse Implementation of JAXB, v4.0.1 所產生 
// 請參閱 https://eclipse-ee4j.github.io/jaxb-ri 
// 一旦重新編譯來源綱要, 對此檔案所做的任何修改都將會遺失. 
//


package com.syscom.fep.ws.client.t24.wsdl;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>successIndicator 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * <pre>{@code
 * <simpleType name="successIndicator">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="Success"/>
 *     <enumeration value="TWSError"/>
 *     <enumeration value="T24Error"/>
 *     <enumeration value="T24Override"/>
 *     <enumeration value="T24Offline"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "successIndicator")
@XmlEnum
public enum SuccessIndicator {

    @XmlEnumValue("Success")
    SUCCESS("Success"),
    @XmlEnumValue("TWSError")
    TWS_ERROR("TWSError"),
    @XmlEnumValue("T24Error")
    T_24_ERROR("T24Error"),
    @XmlEnumValue("T24Override")
    T_24_OVERRIDE("T24Override"),
    @XmlEnumValue("T24Offline")
    T_24_OFFLINE("T24Offline");
    private final String value;

    SuccessIndicator(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SuccessIndicator fromValue(String v) {
        for (SuccessIndicator c: SuccessIndicator.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
