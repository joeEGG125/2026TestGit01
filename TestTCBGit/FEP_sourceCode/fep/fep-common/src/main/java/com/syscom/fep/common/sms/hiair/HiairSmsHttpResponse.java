package com.syscom.fep.common.sms.hiair;

public class HiairSmsHttpResponse extends HiairSmsBaseResponse {
    private int httpStatusCode;
    private HiairSmsHttpResponseContent content;

    public HiairSmsHttpResponse(Throwable error) {
        super(error);
    }

    public HiairSmsHttpResponse(int httpStatusCode, HiairSmsHttpResponseContent content) {
        this.httpStatusCode = httpStatusCode;
        this.content = content;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public HiairSmsHttpResponseContent getContent() {
        return content;
    }

    public void setContent(HiairSmsHttpResponseContent content) {
        this.content = content;
    }
}
