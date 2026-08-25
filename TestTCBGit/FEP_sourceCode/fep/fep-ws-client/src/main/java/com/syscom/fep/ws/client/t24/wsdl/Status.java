//
// 此檔案是由 Eclipse Implementation of JAXB, v4.0.1 所產生 
// 請參閱 https://eclipse-ee4j.github.io/jaxb-ri 
// 一旦重新編譯來源綱要, 對此檔案所做的任何修改都將會遺失. 
//


package com.syscom.fep.ws.client.t24.wsdl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Status complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>{@code
 * <complexType name="Status">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="transactionId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="messageId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="successIndicator" type="{http://temenos.com/FEP}successIndicator"/>
 *         <element name="application" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="messages" type="{http://temenos.com/FEP}ArrayOfString" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Status", propOrder = {
    "transactionId",
    "messageId",
    "successIndicator",
    "application",
    "messages"
})
public class Status {

    protected String transactionId;
    protected String messageId;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected SuccessIndicator successIndicator;
    protected String application;
    protected ArrayOfString messages;

    /**
     * 取得 transactionId 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * 設定 transactionId 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactionId(String value) {
        this.transactionId = value;
    }

    /**
     * 取得 messageId 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * 設定 messageId 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageId(String value) {
        this.messageId = value;
    }

    /**
     * 取得 successIndicator 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link SuccessIndicator }
     *     
     */
    public SuccessIndicator getSuccessIndicator() {
        return successIndicator;
    }

    /**
     * 設定 successIndicator 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link SuccessIndicator }
     *     
     */
    public void setSuccessIndicator(SuccessIndicator value) {
        this.successIndicator = value;
    }

    /**
     * 取得 application 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApplication() {
        return application;
    }

    /**
     * 設定 application 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApplication(String value) {
        this.application = value;
    }

    /**
     * 取得 messages 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    public ArrayOfString getMessages() {
        return messages;
    }

    /**
     * 設定 messages 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    public void setMessages(ArrayOfString value) {
        this.messages = value;
    }

}
