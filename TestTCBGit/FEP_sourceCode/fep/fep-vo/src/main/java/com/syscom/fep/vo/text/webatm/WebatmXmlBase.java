package com.syscom.fep.vo.text.webatm;

/**
 * 僅供Webatm XML類的電文vo繼承
 *
 * @author Ben
 *
 */
public abstract class WebatmXmlBase {
	public abstract void parseMessage(String data) throws Exception;
}
