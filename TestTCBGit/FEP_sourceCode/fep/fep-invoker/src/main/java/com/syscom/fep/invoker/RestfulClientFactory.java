package com.syscom.fep.invoker;

import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.invoker.restful.impl.*;
import com.syscom.fep.vo.communication.BaseCommu;
import com.syscom.fep.vo.communication.ToFISCCommu;
import com.syscom.fep.vo.enums.ClientType;
import com.syscom.fep.vo.enums.RestfulResultCode;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Component
@Lazy
public class RestfulClientFactory {
    /**
     * 送Restful訊息
     *
     * @param <Request>
     * @param clientType
     * @param uri
     * @param request
     * @param timeout
     * @return
     * @throws Exception
     */
    public <Request> String sendReceive(ClientType clientType, String uri, Request request, int timeout) throws Exception {
        try {
            switch (clientType) {
                case TO_FISC_GW:
                    ToFISCRestfulClient toFiscClient = new ToFISCRestfulClient(uri);
                    return toFiscClient.sendReceive((ToFISCCommu) request, timeout);
                case TO_FEP_FISC:
                    ToFEPFISCRestfulClient toFepFiscClient = new ToFEPFISCRestfulClient(uri);
                    return toFepFiscClient.sendReceive((BaseCommu) request, timeout);
                case TO_FEP_ATM:
                    ToFEPATMRestfulClient fepAtmClient = new ToFEPATMRestfulClient(uri);
                    return fepAtmClient.sendReceive((BaseCommu) request, timeout);
                case InQueue11X1:
                    InQueue11X1Client inqueue11X1 = new InQueue11X1Client(uri);
                    return inqueue11X1.sendReceive((String) request, timeout);
                case TO_FEP_ATMMON:
                	ToFEPATMMONRestfulClient fepAtmmonClient = new ToFEPATMMONRestfulClient(uri);
                    return fepAtmmonClient.sendReceive((BaseCommu) request, timeout);
                default:
                    throw ExceptionUtil.createIllegalArgumentException("Unsupported ClientType = [", clientType.name(), "]");
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private Exception handleException(Exception e) {
        if (e instanceof RestClientException) {
            if (e.getCause() instanceof ConnectException) {
                return ExceptionUtil.createException(e, RestfulResultCode.CONNECTION_REFUSED);
            } else if (e.getCause() instanceof SocketTimeoutException) {
                return ExceptionUtil.createException(e, RestfulResultCode.READ_TIMED_OUT);
            }
            return ExceptionUtil.createException(e, e.getMessage());
        } else {
            return e;
        }
    }
}
