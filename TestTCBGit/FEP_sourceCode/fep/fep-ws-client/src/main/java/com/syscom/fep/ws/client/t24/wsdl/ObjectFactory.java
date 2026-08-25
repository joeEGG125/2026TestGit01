//
// 此檔案是由 Eclipse Implementation of JAXB, v4.0.1 所產生 
// 請參閱 https://eclipse-ee4j.github.io/jaxb-ri 
// 一旦重新編譯來源綱要, 對此檔案所做的任何修改都將會遺失. 
//


package com.syscom.fep.ws.client.t24.wsdl;

import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.syscom.fep.ws.client.t24.wsdl package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _OfsRequest_QNAME = new QName("", "OfsRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.syscom.fep.ws.client.t24.wsdl
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CallOfs }
     * 
     * @return
     *     the new instance of {@link CallOfs }
     */
    public CallOfs createCallOfs() {
        return new CallOfs();
    }

    /**
     * Create an instance of {@link CallOfsResponse }
     * 
     * @return
     *     the new instance of {@link CallOfsResponse }
     */
    public CallOfsResponse createCallOfsResponse() {
        return new CallOfsResponse();
    }

    /**
     * Create an instance of {@link Status }
     * 
     * @return
     *     the new instance of {@link Status }
     */
    public Status createStatus() {
        return new Status();
    }

    /**
     * Create an instance of {@link ArrayOfString }
     * 
     * @return
     *     the new instance of {@link ArrayOfString }
     */
    public ArrayOfString createArrayOfString() {
        return new ArrayOfString();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "OfsRequest")
    public JAXBElement<String> createOfsRequest(String value) {
        return new JAXBElement<>(_OfsRequest_QNAME, String.class, null, value);
    }

}
